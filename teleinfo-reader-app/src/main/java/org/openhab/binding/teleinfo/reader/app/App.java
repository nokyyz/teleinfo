package org.openhab.binding.teleinfo.reader.app;

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
import java.util.logging.LogManager;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.openhab.binding.teleinfo.reader.context.ApplicationContext;
import org.openhab.binding.teleinfo.reader.context.ApplicationContextListenerAdapter;
import org.openhab.binding.teleinfo.reader.context.ApplicationContextProvider;
import org.openhab.binding.teleinfo.reader.plugin.broadcast.BroadcastService;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.InvalidConfigurationException;
import org.openhab.binding.teleinfo.reader.plugin.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends ApplicationContextListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String DAEMON_NAME = "teleinfo-reader-deamon";

    private Options options;
    private ApplicationContext appContext;


    public App(final Options options) {
        this.options = options;
    }

    public void start() throws Exception {
        System.out.println("Teleinfo Reader - " + AppResources.APP_VERSION.getLocalized() + "  (https://github.com/nokyyz/teleinfo-reader)");
        System.out.println();
        
        ApplicationContextProvider appContextProvider = ApplicationContextProvider.getProvider();
        if (appContextProvider == null) {
        	printFatalErrorMessage("No applicationContext provider found");
        	return;
        }
        this.appContext = appContextProvider.createApplicationContext();
        appContext.addListener(this);

        appContext.setSerialPortName(options.getSerialPort());
        appContext.setRefreshInterval(options.getRefreshInterval());
        appContext.setPluginsFolder(new File("./plugins"));
        appContext.setPluginsConfigurationHandler(new PropertiesConfigurationPluginHandler(new File("./conf/plugins/")) {
			@Override
			public void onInvalidConfiguration(InvalidConfigurationException error, String pluginId) {
				final String errorMessage = "The configuration of '" + pluginId + "' plugin is invalid";
				LOGGER.error(errorMessage, error);
				printErrorMessage(errorMessage + " (see log for more details)");
			}
		});

    	LogManager.getLogManager().reset(); // trick to disable log message from JPF

        appContext.init();
    }

    @Override
	public void onInitializing(ApplicationContext appContext) {
    	printInfoMessage("Initialization...");
	}

	@Override
	public void onInitialized(ApplicationContext appContext) {
		printInfoMessage("Started !");
	}

	@Override
	public void onBroadcastPluginsLoading(ApplicationContext appContext) {
		printInfoMessage("Broadcast plugins loading...");
	}

	@Override
	public void onBroadcastPluginLoading(ApplicationContext appContext, String pluginFilename) {
		printInfoMessage("'" + pluginFilename + "' broadcast plugin loading...");
	}

	@Override
	public void onBroadcastPluginLoaded(ApplicationContext appContext, final String serviceId, BroadcastService broadcastServiceLoaded) {
		printInfoMessage("'" + serviceId + "' broadcast plugin loaded");
	}

	@Override
	public void onPersistencePluginsLoading(ApplicationContext appContext) {
		printInfoMessage("Persistence plugins loading...");
	}

	@Override
	public void onPersistencePluginLoading(ApplicationContext appContext, String pluginFilename) {
		printInfoMessage("'" + pluginFilename + "' persistence plugin loading...");
	}

	@Override
	public void onPersistencePluginLoaded(ApplicationContext appContext, String serviceId,
			PersistenceService persistenceServiceLoaded) {
		printInfoMessage("'" + serviceId + "' persistence plugin loaded");
	}
	
	@Override
	public void onSerialPortOpening(ApplicationContext appContext, String serialPortName) {
		printInfoMessage("Serial port " + serialPortName + " opening...");
	}

	@Override
	public void onSerialPortStarted(ApplicationContext appContext, String serialPortName) {
		printInfoMessage("Serial port started");
	}

	@Override
	public void onBroadcastPluginStopping(ApplicationContext appContext, String serviceId) {
		printInfoMessage("'" + serviceId + "' broadcast plugin stopping...");
	}

	@Override
	public void onBroadcastPluginStopped(ApplicationContext appContext, String serviceId) {
		printInfoMessage("'" + serviceId + "' broadcast plugin stopped");
	}

	public void stop() throws Exception {
		printInfoMessage("Stopping...");
    	if (appContext != null) {
        	appContext.destroy();
    	}
    }

	
    /**
     * MAIN function
     * @param args
     */
    public static void main(String[] args) {
        CmdLineParser parser = null;
        try {
            Options options = new Options();
            parser = new CmdLineParser(options);
            parser.parseArgument(args);

            final App app = new App(options);

            if (options.isInstallAsService()) {
            	printInfoMessage("Installing Teleinfo Reader service...");
                installAsService(options, args);
                printInfoMessage("Teleinfo Reader service installed !");
                printInfoMessage("To start automatically this service, execute the following commands:");
                printInfoMessage("sudo chmod a+x /etc/init.d/" + DAEMON_NAME);
                printInfoMessage("sudo update-rc.d " + DAEMON_NAME + " defaults");
                System.out.println();

                exit(ReturnCode.SUCCESS_RETURN_CODE);
            } else {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        try {
                            app.stop();
                            
                            exit(ReturnCode.SUCCESS_RETURN_CODE);
                        } catch (Exception e) {
                            LOGGER.warn("An error during the application shutdown", e);
                            exit(ReturnCode.ERROR_RETURN_CODE);
                        }
                    }
                });

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
        printErrorMessage("Example 1: ./start.sh -serialPort /dev/ttyS80 -refreshInterval 4000");
        printErrorMessage(
                "Example 2: ./start.sh -serialPort /dev/ttyS80 -refreshInterval 4000 -installAsService -serviceRunAs root -useSerialPortSymbolicLink -serialPortSymbolicLinkTarget /dev/ttyAMA0");
        printErrorMessage(
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

        System.out.println("Bye !");
        System.exit(exit);
    }

    
    @Override
	public void onWarning(ApplicationContext appContext, String warning) {
    	printWarningMessage(warning);
	}

	@Override
	public void onError(ApplicationContext appContext, String errorMessage, Throwable trace) {
		LOGGER.error(errorMessage, trace);
		printErrorMessage(errorMessage + " - Error: \"" + trace.getClass().getName() + ":" + trace.getMessage() + "\"");
	}

	@Override
	public void onFatalError(ApplicationContext appContext, String errorMessage, Throwable fatalError) {
		LOGGER.error(errorMessage, fatalError);
		printFatalErrorMessage(errorMessage);
		exit(ReturnCode.ERROR_RETURN_CODE);
	}

	private static void printInfoMessage(String message) {
    	//System.out.println("INFO : " + message);
    	LOGGER.info(message);
	}

	private static void printWarningMessage(String message) {
		LOGGER.warn(message);
    	//System.err.println("WARN : " + message);
	}

	private static void printErrorMessage(String message) {
		LOGGER.error(message);
    	//System.err.println("ERROR: " + message);
	}

	private static void printFatalErrorMessage(String message) {
		LOGGER.error(message);
    	//System.err.println("FATAL: " + message);
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
