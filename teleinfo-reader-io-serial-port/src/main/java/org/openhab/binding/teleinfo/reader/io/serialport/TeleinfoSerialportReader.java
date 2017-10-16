package org.openhab.binding.teleinfo.reader.io.serialport;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReader;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReaderAdaptor;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReaderListener;
import org.openhab.binding.teleinfo.reader.io.stream.TeleinfoInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * {@link TeleinfoReader} for serial port.
 */
public class TeleinfoSerialportReader extends TeleinfoReaderAdaptor {

    private static Logger LOGGER = LoggerFactory.getLogger(TeleinfoSerialportReader.class);

    private String serialPortName;
    private long refreshInterval;

    private Timer teleinfoSerialportReaderTimer;
    private SerialPort serialPort;
    private long waitNextHeaderFrameTimeoutInMs = TeleinfoInputStream.DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME;
    private long readingFrameTimeoutInMs = TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME;

    public TeleinfoSerialportReader(String serialPortName, long refreshInterval) {
        this.serialPortName = serialPortName;
        this.refreshInterval = refreshInterval;
    }

    @Override
    public void open() throws IOException {
        LOGGER.debug("open() [start]");

        CommPortIdentifier portIdentifier;
        try {
            LOGGER.trace("serialPortName = " + serialPortName);
            System.setProperty("gnu.io.rxtx.SerialPorts", serialPortName); // Workaround to force serial port detection
                                                                           // on Linux

            portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
        } catch (NoSuchPortException e) {
            throw new IOException("Serial port with given name does not exist", e);
        }

        if (portIdentifier.isCurrentlyOwned()) {
            throw new IOException("Serial port is currently in use.");
        }

        // fixed issue as rxtx library originally used in j62056 does use
        // different version of rxtx
        // com port in their version is using gnu.io.CommPort
        RXTXPort commPort;
        try {
            commPort = portIdentifier.open(this.getClass().getName(), 2000);
        } catch (PortInUseException e) {
            throw new IOException("Serial port is currently in use.", e);
        }

        if (!(commPort instanceof SerialPort)) {
            commPort.close();
            throw new IOException("The specified CommPort is not a serial port");
        }

        serialPort = commPort;

        try {
            LOGGER.debug("set serial port parameters...");
            serialPort.setSerialPortParams(1200, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);

            LOGGER.trace("getListeners().size() = " + getListeners().size());
            if (getListeners().size() > 0) {
                enableScheduler();
            }
        } catch (UnsupportedCommOperationException e) {
            close();
            throw new IOException("Unable to set the given serial comm parameters", e);
        }

        LOGGER.debug("open() [end]");
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("close() [start]");
        disableScheduler();

        if (serialPort == null) {
            return;
        }
        serialPort.close();
        serialPort = null;
        LOGGER.debug("close() [end]");
    }

    @Override
    public void addListener(TeleinfoReaderListener listener) {
        super.addListener(listener);

        if (getListeners().size() == 1) {
            enableScheduler();
        }
    }

    @Override
    public void removeListener(TeleinfoReaderListener listener) {
        super.removeListener(listener);

        if (getListeners().size() == 0) {
            disableScheduler();
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

    private void enableScheduler() {
        LOGGER.trace("enableScheduler() [start]");

        teleinfoSerialportReaderTimer = new Timer("TeleinfoSerialportReader-timer", true);
        teleinfoSerialportReaderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LOGGER.trace("reading next frame...");
                try {
                    TeleinfoInputStream teleinfoInputStream = new TeleinfoInputStream(serialPort.getInputStream(),
                            waitNextHeaderFrameTimeoutInMs, readingFrameTimeoutInMs) {
                        @Override
                        public void close() {
                            // don't close underlying input stream
                        }
                    };

                    Frame frame = teleinfoInputStream.readNextFrame();
                    frame.setId(UUID.randomUUID());
                    
                    teleinfoInputStream = null; // don't close this stream

                    fireOnFrameReceivedEvent(frame);
                } catch (Throwable t) {
                    LOGGER.error("An error occurred during teleinfo frame reading", t);
                }
            }
        }, 10, refreshInterval);

        LOGGER.trace("enableScheduler() [end]");
    }

    private void disableScheduler() {
        LOGGER.trace("disableScheduler() [start]");

        teleinfoSerialportReaderTimer.cancel();

        LOGGER.trace("disableScheduler() [end]");
    }
}
