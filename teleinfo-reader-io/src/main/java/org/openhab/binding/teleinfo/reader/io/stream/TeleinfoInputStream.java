package org.openhab.binding.teleinfo.reader.io.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.dsl.Frame.PeriodeTarifaire;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionBase;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionHeuresCreuses;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionHeuresCreuses.GroupeHoraire;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionTempo;
import org.openhab.binding.teleinfo.reader.io.stream.internal.FrameUtil;
import org.openhab.binding.teleinfo.reader.io.stream.internal.Label;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.Converter;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.ConvertionException;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.FloatConverter;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.GroupeHoraireConverter;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.IntegerConverter;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.PeriodeTarifaireConverter;
import org.openhab.binding.teleinfo.reader.io.stream.internal.converter.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InputStream for Teleinfo {@link Frame} in serial port format.
 */
public class TeleinfoInputStream extends InputStream {

    public static long DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME = 33400;
    public static long DEFAULT_TIMEOUT_READING_FRAME = 33400;

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoInputStream.class);
    private static final Map<Class<?>, Converter> LABEL_VALUE_CONVERTERS;

    private BufferedReader bufferedReader = null;
    private InputStream teleinfoInputStream = null;
    private String groupLine = null;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private long waitNextHeaderFrameTimeoutInMs;
    private long readingFrameTimeoutInMs;

    static {
        LABEL_VALUE_CONVERTERS = new HashMap<>();
        LABEL_VALUE_CONVERTERS.put(Integer.class, new IntegerConverter());
        LABEL_VALUE_CONVERTERS.put(String.class, new StringConverter());
        LABEL_VALUE_CONVERTERS.put(Float.class, new FloatConverter());
        LABEL_VALUE_CONVERTERS.put(PeriodeTarifaire.class, new PeriodeTarifaireConverter());
        LABEL_VALUE_CONVERTERS.put(GroupeHoraire.class, new GroupeHoraireConverter());
    }

    public TeleinfoInputStream(final InputStream teleinfoInputStream) {
        this(teleinfoInputStream, DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME, DEFAULT_TIMEOUT_READING_FRAME);
    }

    public TeleinfoInputStream(final InputStream teleinfoInputStream, long waitNextHeaderFrameTimeoutInMs,
            long readingFrameTimeoutInMs) {
        if (teleinfoInputStream == null) {
            throw new IllegalArgumentException("Teleinfo inputStream not null");
        }

        this.waitNextHeaderFrameTimeoutInMs = waitNextHeaderFrameTimeoutInMs;
        this.readingFrameTimeoutInMs = readingFrameTimeoutInMs;

        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(teleinfoInputStream, "ASCII"));
            this.teleinfoInputStream = teleinfoInputStream;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        groupLine = null;
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("close() [start]");
        bufferedReader.close();
        executorService.shutdownNow();
        super.close();
        LOGGER.debug("close() [end]");
    }

    /**
     * Returns the next frame.
     *
     * @return the next frame or null if end of stream
     * @throws InvalidFrameException if the read data from
     * @throws TimeoutException if the delay to read a complete frame is expired (33,4 ms) or if the delay to find the
     *             header of next frame is expired (33,4 ms)
     * @throws IOException
     */
    public synchronized Frame readNextFrame() throws InvalidFrameException, TimeoutException, IOException {
        LOGGER.debug("readNextFrame() [start]");

        // seek the next header frame
        Future<Void> seekNextHeaderFrameTask = executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                while (!isHeaderFrame(groupLine)) {
                    groupLine = bufferedReader.readLine();
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("groupLine = {}", groupLine);
                    }
                    if (groupLine == null) { // end of stream
                        LOGGER.trace("end of stream reached !");
                        return null;
                    }
                }

                LOGGER.trace("header frame found !");
                return null;
            }
        });
        try {
            LOGGER.debug("seeking the next header frame...");
            LOGGER.trace("waitNextHeaderFrameTimeoutInMs = {}", waitNextHeaderFrameTimeoutInMs);
            seekNextHeaderFrameTask.get(waitNextHeaderFrameTimeoutInMs, TimeUnit.MICROSECONDS);

            if (groupLine == null) { // end of stream
                return null;
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            rethrowTaskExecutionException(e);
            return null; // FIXME best way ?
        }

        Future<Map<Label, Object>> nextFrameFuture = executorService.submit(new Callable<Map<Label, Object>>() {
            @Override
            public Map<Label, Object> call() throws Exception {
                // read label values
                Map<Label, Object> frameValues = new HashMap<>();
                while ((groupLine = bufferedReader.readLine()) != null && !isHeaderFrame(groupLine)) {
                    LOGGER.trace("groupLine = {}", groupLine);

                    String[] groupLineTokens = groupLine.split("\\s");
                    if (groupLineTokens.length != 2 && groupLineTokens.length != 3) {
                        final String error = String.format("The groupLine '%1$s' is incomplete", groupLine);
                        throw new InvalidFrameException(error);
                    }
                    String labelStr = groupLineTokens[0];
                    String valueString = groupLineTokens[1];

                    // verify integrity (through checksum)
                    char checksum = (groupLineTokens.length == 3 ? groupLineTokens[2].charAt(0) : ' ');
                    char computedChecksum = FrameUtil.computeGroupLineChecksum(labelStr, valueString);
                    if (computedChecksum != checksum) {
                        LOGGER.trace("computedChecksum = {}", computedChecksum);
                        LOGGER.trace("checksum = {}", checksum);
                        throw new InvalidFrameException("The groupLine seems corrupted (integrity not checked)");
                    }

                    Label label = Label.valueOf(labelStr);
                    if (label == null) {
                        final String error = String.format("The label '%1$s' is unknown", labelStr);
                        throw new InvalidFrameException(error);
                    }

                    Class<?> labelType = label.getType();
                    Converter converter = LABEL_VALUE_CONVERTERS.get(labelType);
                    if (converter == null) {
                        final String error = String.format("No converter founded for '%1$s' label type", labelType);
                        throw new IllegalStateException(error);
                    }
                    try {
                        Object value = converter.convert(valueString);
                        // FIXME checks constraints value

                        frameValues.put(label, value);
                    } catch (ConvertionException e) {
                        final String error = String.format("An error occurred during '%1$s' value conversion",
                                valueString);
                        throw new InvalidFrameException(error, e);
                    }
                }

                return frameValues;
            }
        });

        try {
            LOGGER.debug("reading data frame...");
            LOGGER.trace("readingFrameTimeoutInMs = {}", readingFrameTimeoutInMs);
            Map<Label, Object> frameValues = nextFrameFuture.get(readingFrameTimeoutInMs, TimeUnit.MICROSECONDS);

            // build the frame from map values
            Frame frame = null;

            String optionTarif = (String) frameValues.get(Label.OPTARIF);
            switch (optionTarif) {
                case "BASE":
                    frame = buildFrameOptionBase(frameValues);
                    break;
                case "HC..":
                    frame = buildFrameOptionHeuresCreuses(frameValues);
                    break;
                case "EJP.":
                    frame = buildFrameOptionBase(frameValues); // FIXME
                    break;
                case "BBRx":
                    frame = buildFrameOptionTempo(frameValues);
                    break;
                default:
                    final String error = String.format("The option Tarif '%1$c' is not supported", optionTarif);
                    throw new InvalidFrameException(error);
            }

            LOGGER.debug("readNextFrame() [end]");
            return frame;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            rethrowTaskExecutionException(e);
            return null; // FIXME best way ?
        }
    }

    public long getWaitNextHeaderFrameTimeoutInMs() {
        return waitNextHeaderFrameTimeoutInMs;
    }

    public void setWaitNextHeaderFrameTimeoutInMs(long waitNextHeaderFrameTimeoutInMs) {
        this.waitNextHeaderFrameTimeoutInMs = waitNextHeaderFrameTimeoutInMs;
    }

    public long getReadingFrameTimeoutInMs() {
        return readingFrameTimeoutInMs;
    }

    public void setReadingFrameTimeoutInMs(long readingFrameTimeoutInMs) {
        this.readingFrameTimeoutInMs = readingFrameTimeoutInMs;
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("The 'read()' is not supported");
    }

    private boolean isHeaderFrame(final String line) {
        // A new teleinfo trame begin with '3' and '2' bytes (END OF TEXT et START OF TEXT)
        return (line != null && line.length() > 1 && line.codePointAt(0) == 3 && line.codePointAt(1) == 2);
    }

    private FrameOptionBase buildFrameOptionBase(final Map<Label, Object> frameValues) throws InvalidFrameException {
        LOGGER.trace("buildFrameOptionBase(Map<Label, Object>) [start]");
        FrameOptionBase frame = new FrameOptionBase();
        setCommonFrameFields(frame, frameValues);
        frame.setIndexBase(getRequiredLabelValue(Label.BASE, Integer.class, frameValues));
        // FIXME TBD

        LOGGER.trace("buildFrameOptionBase(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameOptionHeuresCreuses buildFrameOptionHeuresCreuses(final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        LOGGER.trace("buildFrameOptionHeuresCreuses(Map<Label, Object>) [start]");
        FrameOptionHeuresCreuses frame = new FrameOptionHeuresCreuses();
        setCommonFrameFields(frame, frameValues);
        frame.setGroupeHoraire((GroupeHoraire) frameValues.get(Label.HHPHC));
        frame.setIndexHeuresCreuses(getRequiredLabelValue(Label.HCHC, Integer.class, frameValues));
        frame.setIndexHeuresPleines(getRequiredLabelValue(Label.HCHP, Integer.class, frameValues));

        LOGGER.trace("buildFrameOptionHeuresCreuses(Map<Label, Object>) [end]");
        return frame;
    }

    private FrameOptionTempo buildFrameOptionTempo(final Map<Label, Object> frameValues) throws InvalidFrameException {
        FrameOptionTempo frame = new FrameOptionTempo();
        setCommonFrameFields(frame, frameValues);
        // FIXME TBD

        return frame;
    }

    private void setCommonFrameFields(final Frame frame, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        LOGGER.trace("setCommonFrameFields(Frame, Map<Label, Object>) [start]");
        frame.setADCO(getRequiredLabelValue(Label.ADCO, String.class, frameValues));
        frame.setIntensiteInstantanee(getRequiredLabelValue(Label.IINST, Integer.class, frameValues));
        frame.setIntensiteSouscrite(getRequiredLabelValue(Label.ISOUSC, Integer.class, frameValues));
        frame.setPeriodeTarifaireEnCours(getRequiredLabelValue(Label.PTEC, PeriodeTarifaire.class, frameValues));
        frame.setPuissanceApparente(getRequiredLabelValue(Label.PAPP, Integer.class, frameValues));
        frame.setMotEtat(getRequiredLabelValue(Label.MOTDETAT, String.class, frameValues));

        frame.setIntensiteMaximale(getOptionalLabelValue(Label.IMAX, Integer.class, frameValues));
        frame.setAvertissementDepassementPuissanceSouscrite(
                getOptionalLabelValue(Label.ADPS, Integer.class, frameValues));

        frame.setTimestamp(new Date());
        LOGGER.trace("setCommonFrameFields(Frame, Map<Label, Object>) [end]");
    }

    @SuppressWarnings("unchecked")
    private <T> T getRequiredLabelValue(Label label, Class<T> dataType, final Map<Label, Object> frameValues)
            throws InvalidFrameException {
        if (!frameValues.containsKey(label)) {
            final String error = String.format("The required label '%1$s' is missing in frame", label);
            throw new InvalidFrameException(error);
        }

        return (T) frameValues.get(label);
    }

    @SuppressWarnings("unchecked")
    private <T> T getOptionalLabelValue(Label label, Class<T> dataType, final Map<Label, Object> frameValues) {
        return (T) frameValues.get(label);
    }

    private Exception rethrowTaskExecutionException(ExecutionException e)
            throws InvalidFrameException, IOException, TimeoutException {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFrameException) {
            throw (InvalidFrameException) cause;
        } else if (cause instanceof IOException) {
            throw (IOException) cause;
        } else if (cause instanceof TimeoutException) {
            throw (TimeoutException) cause;
        } else {
            throw new IllegalStateException(e);
        }
    }
}
