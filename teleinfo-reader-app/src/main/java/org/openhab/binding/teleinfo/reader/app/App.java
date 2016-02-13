package org.openhab.binding.teleinfo.reader.app;

import static org.openhab.binding.teleinfo.reader.io.serialport.TeleinfoInputStream.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    private static final String DAEMON_NAME = "teleinfo-deamon";

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

            if (options.isInstallAsService()) {
                System.out.println("Installing Teleinfo Reader service...");
                installAsService(options, args);
                System.out.println("Teleinfo Reader service installed !");
                System.out.println("To start automatically this service, execute the following commands:");
                System.out.println("sudo chmod a+x /etc/init.d/" + DAEMON_NAME);
                System.out.println("sudo update-rc.d " + DAEMON_NAME + " defaults");
                System.out.println();

                System.out.println("Bye");
                exit(ReturnCode.SUCCESS_RETURN_CODE);
            } else {
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

                LOGGER.debug("Args: " + args);
                app.start();
            }
        } catch (final CmdLineException e) {
            displayHelp(parser);
            exit(ReturnCode.ERROR_RETURN_CODE);
        } catch (final Throwable e) {
            LOGGER.error("An fatal error occurred during runtime", e);
            e.printStackTrace();
            exit(ReturnCode.ERROR_RETURN_CODE);
        }
    }

    private static void displayHelp(final CmdLineParser parser) {
        parser.printUsage(System.err);
        System.err.println("Example 1: ./start.sh -serialPort /dev/ttyS80 -refreshInterval 4000");
        System.err.println(
                "Example 2: ./start.sh -serialPort /dev/ttyS80 -refreshInterval 4000 -installAsService -serviceRunAs root -useSerialPortSymbolicLink -serialPortSymbolicLinkTarget /dev/ttyAMA0");
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

    private static void installAsService(final Options options, String[] appArgs) throws Exception {
        String currentUser = System.getProperty("user.name");
        if (!"root".equals(currentUser)) {
            throw new IllegalStateException("To install service, you must be logged with 'root' user");
        }

        String osName = System.getProperty("os.name");
        if (!osName.contains("nix") && !osName.contains("nux") && !osName.contains("aix")) {
            throw new IllegalStateException("'Install service' option is only on Linux");
        }

        Path serviceScriptFile = Paths.get("/etc/init.d/" + DAEMON_NAME);
        if (Files.exists(serviceScriptFile)) {
            throw new IllegalStateException("Service already exists. To reinstall service, remove '" + DAEMON_NAME
                    + "' file (" + serviceScriptFile.toAbsolutePath().toString() + ")");
        }

        Map<String, String> variables = new HashMap<>();
        variables.put("@@APP_HOME@@", new File("").getAbsolutePath());
        StringBuilder argsWithSpace = new StringBuilder();
        // suppress '-installAsService' option
        for (String appArg : appArgs) {
            if (!"-installAsService".equalsIgnoreCase(appArg) && !"-serviceRunAs".equalsIgnoreCase(appArg)
                    && !options.getServiceRunAs().equalsIgnoreCase(appArg)
                    && !"-useSerialPortSymbolicLink".equalsIgnoreCase(appArg)
                    && !"-serialPortSymbolicLinkTarget".equalsIgnoreCase(appArg)
                    && !options.getSerialPortSymbolicLinkTarget().equalsIgnoreCase(appArg)) {
                argsWithSpace.append(appArg);
                argsWithSpace.append(" ");
            }
        }
        variables.put("@@DAEMON_ARGS@@", argsWithSpace.toString());
        variables.put("@@RUN_AS@@", options.getServiceRunAs());
        if (options.isUseSerialPortSymbolicLink()) {
            variables.put("@@USE_SERIAL_PORT_SYMBOLIC_LINK@@", "1");
            variables.put("@@SERIAL_PORT_PATH@@", options.getSerialPortSymbolicLinkTarget());
            variables.put("@@SERIAL_PORT_SYMBOLIC_LINK_PATH@@", options.getSerialPort());
        } else {
            variables.put("@@USE_SERIAL_PORT_SYMBOLIC_LINK@@", "0");
            variables.put("@@SERIAL_PORT_PATH@@", "");
            variables.put("@@SERIAL_PORT_SYMBOLIC_LINK_PATH@@", "");
        }

        final Charset utf8Charset = Charset.forName("UTF-8");

        try (BufferedReader templateScriptReader = new BufferedReader(new InputStreamReader(
                App.class.getClassLoader().getResourceAsStream("misc/linux/install-as-service-template.sh"),
                utf8Charset));
                BufferedWriter scriptWriter = Files.newBufferedWriter(serviceScriptFile, utf8Charset,
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);) {
            String line = null;
            while ((line = templateScriptReader.readLine()) != null) {
                for (Entry<String, String> variableEntry : variables.entrySet()) {
                    // line = line.replaceAll(variableEntry.getKey(), variableEntry.getValue());
                    line = line.replace(variableEntry.getKey(), variableEntry.getValue());
                }

                scriptWriter.write(line);
                scriptWriter.newLine();
            }

        }

        // using PosixFilePermission to set file permissions 777
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        Files.setPosixFilePermissions(serviceScriptFile, perms);
    }
}
