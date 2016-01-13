package org.openhab.binding.teleinfo.reader.app;

import static org.openhab.binding.teleinfo.reader.io.serialport.TeleinfoInputStream.*;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openhab.binding.teleinfo.network.tcp.TeleinfoServer;
import org.openhab.binding.teleinfo.reader.Frame;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReader;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReaderListenerAdaptor;
import org.openhab.binding.teleinfo.reader.io.serialport.TeleinfoSerialportReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static Logger LOGGER = LoggerFactory.getLogger(App.class);

    private Options options;

    private TeleinfoServer teleinfoServer = null;
    private TeleinfoSerialportReader serialPortReader;

    public App(final Options options) {
        System.out.println("Teleinfo Serial Port version " + AppResources.APP_VERSION.getLocalized());

        this.options = options;
    }

    public void start() throws Exception {
        System.out.println("Starting...");
        teleinfoServer = new TeleinfoServer(9999);

        serialPortReader = new TeleinfoSerialportReader(options.getSerialPort(), options.getRefreshInterval());
        serialPortReader.setWaitNextHeaderFrameTimeoutInMs(DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME * 1000); // FIXME
                                                                                                           // rendre
                                                                                                           // paramétrable
        serialPortReader.setReadingFrameTimeoutInMs(DEFAULT_TIMEOUT_READING_FRAME * 1000); // FIXME rendre paramétrable

        serialPortReader.addListener(new TeleinfoReaderListenerAdaptor() {
            @Override
            public void onFrameReceived(TeleinfoReader reader, Frame frame) {
                teleinfoServer.broadcast(frame);
            }
        });

        teleinfoServer.start();
        serialPortReader.open();
    }

    public void stop() throws Exception {
        System.out.println("Stopping...");
        if (serialPortReader != null) {
            serialPortReader.close();
            serialPortReader = null;
        }
        if (teleinfoServer != null && teleinfoServer.isOnline()) {
            teleinfoServer.stop();
            teleinfoServer = null;
        }
        System.out.println("Bye");
    }

    public static void main(String[] args) {
        CmdLineParser parser = null;
        try {
            Options options = new Options();
            parser = new CmdLineParser(options);
            parser.parseArgument(args);

            final App app = new App(options);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        app.stop();
                        // exit(ReturnCode.SUCCESS_RETURN_CODE);
                    } catch (Exception e) {
                        LOGGER.warn("An error during the application shutdown", e);
                    }
                }
            });

            app.start();
        } catch (final CmdLineException e) {
            displayHelp(parser);
            exit(ReturnCode.ERROR_RETURN_CODE);
        } catch (final Throwable e) {
            LOGGER.error("An fatal error occurred during runtime", e);
            exit(ReturnCode.ERROR_RETURN_CODE);
        }
    }

    private static void displayHelp(final CmdLineParser parser) {
        parser.printUsage(System.err);
        System.err.println("Example: ./start.sh -serialPort /dev/ttyS80 -refreshInterval 4000");
        System.err.println(
                "Tips: sometimes the serial port can require a symbolic link (e.g: 'sudo ln -s /dev/ttyAMA0 /dev/ttyS80')");
    }

    private static void exit(final ReturnCode returnCode) {
        final int exit;
        switch (returnCode) {
            case ERROR_RETURN_CODE:
                exit = -1;
                break;
            case SUCCESS_RETURN_CODE:
                exit = 0;
                break;
            default:
                exit = -1;
                break;
        }

        System.exit(exit);
    }
}
