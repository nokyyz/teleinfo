package org.openhab.binding.teleinfo.reader.context.defaultt;

import static org.openhab.binding.teleinfo.reader.context.defaultt.ApplicationContextListenerEventName.onWarning;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.standard.StandardPluginLocation;
import org.java.plugin.util.ExtendedProperties;
import org.openhab.binding.teleinfo.reader.context.ApplicationContext;
import org.openhab.binding.teleinfo.reader.context.ApplicationContextListener;
import org.openhab.binding.teleinfo.reader.context.conf.ConfigurationPluginHandler;
import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReader;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReaderListenerAdaptor;
import org.openhab.binding.teleinfo.reader.io.serialport.TeleinfoSerialportReader;
import org.openhab.binding.teleinfo.reader.io.stream.TeleinfoInputStream;
import org.openhab.binding.teleinfo.reader.plugin.broadcast.BroadcastService;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ConfigurableService;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.Configuration;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.InvalidConfigurationException;
import org.openhab.binding.teleinfo.reader.plugin.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class DefaultApplicationContext implements ApplicationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationContext.class);

	private Path pluginsFolder;
	private String serialPortName;
	private long refreshInterval;
	private ConfigurationPluginHandler configurationPluginHandler;
	private TeleinfoSerialportReader serialPortReader;
	private PersistenceService defaultPersistenceService;
	private Map<String, PersistenceService> persistenceServices;
	private Map<String, PersistenceService> startedPersistenceServices;
	private Map<String, BroadcastService> broadcastServices;
	private Map<String, BroadcastService> startedBroadcastServices;
	private Set<WeakReference<ApplicationContextListener>> appContextListeners;
	private ExecutorService frameDispatcherExecutorService = Executors.newFixedThreadPool(5);

	public DefaultApplicationContext() {
		appContextListeners = new HashSet<>();
	}
	
	@Override
	public void init() throws Exception {
		LOGGER.debug("init() [start]");
		
		Preconditions.checkNotNull(configurationPluginHandler, "ConfigurationPlugin handler must be defined in Application context");
		Preconditions.checkNotNull(serialPortName, "Serial port name must be defined in Application context");
		Preconditions.checkNotNull(refreshInterval, "Refresh interval must be defined in Application context");
		Preconditions.checkNotNull(pluginsFolder, "Plugins folder must be defined in Application context");
		Preconditions.checkArgument(Files.exists(pluginsFolder), "Plugins folder must exist");
		Preconditions.checkArgument(Files.isReadable(pluginsFolder), "Plugins folder must be readable (please check access rights)");
		Preconditions.checkArgument(Files.isDirectory(pluginsFolder), "Plugins folder must be a folder (not a file)");

		fireEvent(ApplicationContextListenerEventName.onPersistencePluginsLoading);
		persistenceServices = new HashMap<>();
		startedPersistenceServices = new HashMap<>();
		loadPersistencePlugins();
		if (persistenceServices.size() == 0) {
			fireEvent(onWarning, "No persistence service loaded");
		}
		
		if (persistenceServices.size() > 0) {
			if (defaultPersistenceService == null) {
				fireFatalError("The default persistence service is not set", null);
				return;
			}
			
			LOGGER.info("Persistence services initialization...");
			try {
				LOGGER.info("Default persistence services initialization...");
				fireEvent(ApplicationContextListenerEventName.onPersistencePluginLoading, "default");
				defaultPersistenceService.init();
				LOGGER.info("Default persistence services initialized");
				fireEvent(ApplicationContextListenerEventName.onPersistencePluginLoaded, "default");
			} catch (Throwable t) {
				fireFatalError("An error occurred during default persistence service initialization", t);
				return;
			}
			
			for (Entry<String, PersistenceService> persistenceServiceEntry : persistenceServices.entrySet()) {
				String serviceId = persistenceServiceEntry.getKey();
				try {
					fireEvent(ApplicationContextListenerEventName.onPersistencePluginLoading, serviceId);
					persistenceServiceEntry.getValue().init();
					startedPersistenceServices.put(serviceId, persistenceServiceEntry.getValue());
					fireEvent(ApplicationContextListenerEventName.onPersistencePluginLoaded, serviceId);
				} catch (Throwable t) {
					fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during '" + serviceId + "' persistence service initialization", t);
				}
			}
			LOGGER.info("Persistence services initialized");
			fireEvent(ApplicationContextListenerEventName.onPersistencePluginsLoaded);
		}

		fireEvent(ApplicationContextListenerEventName.onBroadcastPluginsLoading);
		broadcastServices = new HashMap<>();
		loadBroadcastPlugins();
		if (broadcastServices.size() == 0) {
			fireFatalError("No broadcast service loaded. At least one broadcast service is required !", null);
			return;
		}

		LOGGER.info("Broadcast services initialization...");
		startedBroadcastServices = new HashMap<>();
		for (Entry<String, BroadcastService> broadcastServiceEntry : broadcastServices.entrySet()) {
			String serviceId = broadcastServiceEntry.getKey();
			BroadcastService service = broadcastServiceEntry.getValue();
			try {
				fireEvent(ApplicationContextListenerEventName.onBroadcastPluginLoading, serviceId);
				if (service instanceof ConfigurableService) {
					ConfigurableService configurationService = (ConfigurableService) service;
					Configuration conf = configurationPluginHandler.getConfiguration(serviceId, configurationService.getConfigurationDefinition());
					try {
						configurationService.setConfiguration(conf);
					} catch (InvalidConfigurationException e) {
						LOGGER.error("The configuration of '" + serviceId + "' service is invalid", e);
						configurationPluginHandler.onInvalidConfiguration(e, serviceId);
						throw e;
					}
				}
				service.init();
				startedBroadcastServices.put(serviceId, broadcastServiceEntry.getValue());
				fireEvent(ApplicationContextListenerEventName.onBroadcastPluginLoaded, serviceId, service);
			} catch (Throwable t) {
				fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during '" + serviceId + "' broadcast service initialization", t);
			}
		}
		LOGGER.info("Broadcast services initialized");
		fireEvent(ApplicationContextListenerEventName.onBroadcastPluginsLoaded);
		
		LOGGER.info("Serial port initialization...");
		fireEvent(ApplicationContextListenerEventName.onSerialPortOpening, serialPortName);
		try {
			serialPortReader = new TeleinfoSerialportReader(serialPortName, refreshInterval);
			serialPortReader.setWaitNextHeaderFrameTimeoutInMs(TeleinfoInputStream.DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME * 1000); // FIXME
																												// rendre
																												// paramétrable
			serialPortReader.setReadingFrameTimeoutInMs(TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME * 1000); // FIXME
																								// rendre
																								// paramétrable
	
			serialPortReader.addListener(new TeleinfoReaderListenerAdaptor() {
				@Override
				public void onFrameReceived(TeleinfoReader reader, final Frame frame) {
					LOGGER.debug("onFrameReceived(TeleinfoReader, Frame) [start]");
					
					frameDispatcherExecutorService.execute(new Runnable() {
						@Override
						public void run() {
							persist(frame);
							broadcast(frame);
						}
					});
					
					LOGGER.debug("onFrameReceived(TeleinfoReader, Frame) [end]");
				}
			});
			
			
			LOGGER.info("Teleinfo serial port opening...");
			serialPortReader.open();
			LOGGER.info("Teleinfo serial port opened");
			LOGGER.info("Serial port initialized");
			fireEvent(ApplicationContextListenerEventName.onSerialPortStarted, serialPortName);
		} catch (Throwable t) {
			final String errorMessage = "A fatal error occurred during '" + serialPortName + "' Teleinfo serial port opening ('"+t.getMessage()+"')";
			LOGGER.error(errorMessage, t);
			fireFatalError(errorMessage, t);
			return;
		}
		
		LOGGER.debug("init() [end]");
	}

	@Override
	public void destroy() throws Exception {
		LOGGER.debug("destroy() [start]");

		frameDispatcherExecutorService.shutdownNow();
		
        if (serialPortReader != null) {
            serialPortReader.close();
            serialPortReader = null;
        }
        
        if (startedBroadcastServices != null) {
            for (Entry<String, BroadcastService> startedBroadcastServiceEntry : startedBroadcastServices.entrySet()) {
            	String serviceId = startedBroadcastServiceEntry.getKey();
    			try {
    				fireEvent(ApplicationContextListenerEventName.onBroadcastPluginStopping, serviceId);
    				startedBroadcastServiceEntry.getValue().destroy();
    				fireEvent(ApplicationContextListenerEventName.onBroadcastPluginStopped, serviceId);
    			} catch (Throwable t) {
    				fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during the '" + startedBroadcastServiceEntry.getKey() + "' service destroy", t);
    			}
    		}
            startedBroadcastServices.clear();
            startedBroadcastServices = null;        	
        }

        if (startedPersistenceServices != null) {
			for (Entry<String, PersistenceService> startedPersistenceServiceEntry : startedPersistenceServices.entrySet()) {
				try {
					startedPersistenceServiceEntry.getValue().destroy();
				} catch (Throwable t) {
					fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during frame persist with '" + startedPersistenceServiceEntry.getKey() + "' service", t);
				}
			}
	        startedPersistenceServices.clear();
	        startedPersistenceServices = null;
        }
        
        LOGGER.debug("destroy() [end]");
	}

	@Override
	public void setSerialPortName(String value) {
		this.serialPortName = value;
	}

	@Override
	public void setRefreshInterval(long value) {
		refreshInterval = value;
	}
	
	@Override
	public void setPluginsFolder(Path value) {
		this.pluginsFolder = value;
	}
	
	@Override
	public void setPluginsConfigurationHandler(ConfigurationPluginHandler handler) {
		this.configurationPluginHandler = handler;
	}
	
	@Override
	public void addListener(ApplicationContextListener appContextListener) {
		appContextListeners.add(new WeakReference<ApplicationContextListener>(appContextListener));
	}

	@Override
	public void removeListener(ApplicationContextListener listener) {
		// TODO to be improved in Java 8 ? (ArrayList#stream().filter(...))
		WeakReference<ApplicationContextListener> weakappContextListenerToRemove = null;
		
		for (WeakReference<ApplicationContextListener> weakAppContextListener : appContextListeners) {
			ApplicationContextListener appContextListener = weakAppContextListener.get();
			if (appContextListener != null && appContextListener.equals(listener)) {
				weakappContextListenerToRemove = weakAppContextListener;
			}
		}
		
		appContextListeners.remove(weakappContextListenerToRemove);	
	}

	/**
	 * Broadcast the given Teleinfo frame to all started broadcast services.
	 * @param frameToBroadcast Teleinfo frame to broadcast
	 */
	private void broadcast(final Frame frameToBroadcast) {
		LOGGER.debug("broadcast(Frame) [start]");
		
		for (Entry<String, BroadcastService> startedBroadcastServiceEntry : startedBroadcastServices.entrySet()) {
			try {
				startedBroadcastServiceEntry.getValue().broadcast(frameToBroadcast);
			} catch (Throwable t) {
				fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during broadcast with '" + startedBroadcastServiceEntry.getKey() + "' service", t);
			}
		}
		
		LOGGER.debug("broadcast(Frame) [end]");
	}
	
	/**
	 * Persist the given Teleinfo frame into the default persistence service, then into the other started persistence services.
	 * @param frameToPersist Teleinfo frame to persist
	 */
	private void persist(final Frame frameToPersist) {
		LOGGER.debug("persist(Frame) [start]");
		
		if (frameToPersist.getId() == null) {
			frameToPersist.setId(UUID.randomUUID());
		}
		
		// persist into default persistence
		try {
			if (defaultPersistenceService != null) {
				defaultPersistenceService.insert(frameToPersist);				
			}
		} catch (Throwable t) {
			fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during frame persist with default service", t);
		}

		// ... and others persistence services
		for (Entry<String, PersistenceService> startedPersistenceServiceEntry : startedPersistenceServices.entrySet()) {
			try {
				startedPersistenceServiceEntry.getValue().insert(frameToPersist);
			} catch (Throwable t) {
				fireEvent(ApplicationContextListenerEventName.onError, "An error occurred during frame persist with '" + startedPersistenceServiceEntry.getKey() + "' service", t);
			}
		}
		
		LOGGER.debug("persist(Frame) [end]");
	}
	
    private Map<String, Class<?>> getExtensions(String extensionPointId) throws Exception {
    	LOGGER.debug("getExtensions(String) [start]");
    	
    	Map<String, Class<?>> extensions = new HashMap<>();
    	
		List<PluginLocation> pluginLocations = new ArrayList<>();

		Enumeration<URL> pluginUrls = DefaultApplicationContext.class.getClassLoader().getResources("plugin");
		while (pluginUrls.hasMoreElements()) {
			URL pluginUrl = pluginUrls.nextElement();
			URL pluginXmlFileUrl = new URL(pluginUrl, "plugin/plugin.xml");
			StandardPluginLocation extensionPointLocation = new StandardPluginLocation(pluginUrl, pluginXmlFileUrl);
			pluginLocations.add(extensionPointLocation);
		}

		// Load addons from the "plugins" folder
		List<URL> pluginPaths = new ArrayList<>();
		pluginPaths.add(pluginsFolder.toFile().toURI().toURL());

		if (! Files.exists(pluginsFolder) || ! Files.isReadable(pluginsFolder)) {
			throw new InvalidPathException(pluginsFolder.toAbsolutePath().toString(), "Plugins folder doesn't exist or can't be read");
		}
		
		File[] pluginFiles = pluginsFolder.toFile().listFiles();
		LOGGER.debug("pluginFiles.length = " + pluginFiles.length);

		for (File pluginFile : pluginFiles) {
			LOGGER.debug("pluginFile = " + pluginFile);
			pluginLocations.add(new StandardPluginLocation(pluginFile, "plugin.xml"));
			pluginPaths.add(pluginFile.toURI().toURL());
		}

		// publication
		PluginLocation[] pluginLocationsArray = new PluginLocation[pluginLocations.size()];
		pluginLocations.toArray(pluginLocationsArray);
		ExtendedProperties jpfProperties = new ExtendedProperties();
		jpfProperties.put("org.java.plugin.PathResolver", "org.java.plugin.standard.ShadingPathResolver");
		jpfProperties.put("org.java.plugin.standard.ShadingPathResolver.shadowFolder", "${java.io.tmpdir}/.teleinfo-reader_plugins_cache");
		jpfProperties.put("org.java.plugin.standard.ShadingPathResolver.unpackMode", "smart");
		PluginManager pluginManager = ObjectFactory.newInstance(jpfProperties).createManager(
				ObjectFactory.newInstance(jpfProperties).createRegistry(),
                ObjectFactory.newInstance(jpfProperties).createPathResolver()
		);
		pluginManager.publishPlugins(pluginLocationsArray);

		URL[] pluginPathArray = new URL[pluginPaths.size()];
		pluginPaths.toArray(pluginPathArray);
		
		ExtensionPoint extensionPt = pluginManager.getRegistry().getExtensionPoint("org.openhab.binding.teleinfo.reader.plugin." + extensionPointId, extensionPointId);
		List<Extension> connectedExtensions = new ArrayList<>(extensionPt.getConnectedExtensions());
		LOGGER.debug("connectedExtensions.size() = " + connectedExtensions.size());
		if (connectedExtensions.isEmpty()) {
			fireEvent(ApplicationContextListenerEventName.onWarning, "No extension founded for '" + extensionPointId + "' in 'plugins' folder");
		} 

		for (int i = 0; i < connectedExtensions.size(); i++) {
			Extension extension = connectedExtensions.get(i);

			String extensionId = null;
			try {
				pluginManager.activatePlugin(extension.getDeclaringPluginDescriptor().getId());
				PluginClassLoader pluginClassLoader = pluginManager
						.getPluginClassLoader(extension.getDeclaringPluginDescriptor());
				extensionId = extension.getDeclaringPluginDescriptor().getId();
				extensionId = extensionId.toLowerCase();
				// checks the validity of persistenceAddonId
				try {
					Paths.get(extensionId);
				} catch (InvalidPathException e) {
					throw new IllegalStateException("Invalid plugin identifier for '" + extensionId + "' plugin");
				}
				
				if (extensions.containsKey(extensionId)) {
					throw new IllegalStateException("A ' + extensionPointId + ' plugin already exists with '" + extensionId + "' identifier");
				}

				Class<?> extensionClazz = pluginClassLoader.loadClass(extension.getParameter("class").valueAsString());
				
				extensions.put(extensionId, extensionClazz);
			} catch (Throwable t) {
				final String errorMessage = "An error occurred during '" + extensionId + "' extension loading";
				LOGGER.error(errorMessage, t);
				fireEvent(ApplicationContextListenerEventName.onError, errorMessage, t);
			}
		}
		
		LOGGER.debug("getExtensions(String) [end]");
		return extensions;
	}
			
	private void loadPersistencePlugins() throws Exception {
    	LOGGER.debug("loadPersistenceAddons() [start]");
    	
    	Map<String, Class<?>> persistenceExtensions = getExtensions("persistence");

		for (Entry<String, Class<?>> persistenceExtension : persistenceExtensions.entrySet()) {
			try {
				PersistenceService persistenceService = (PersistenceService) persistenceExtension.getValue().newInstance();
				persistenceServices.put(persistenceExtension.getKey(), persistenceService);
			} catch (Throwable t) {
				final String errorMessage = "An error occurred during '" + persistenceExtension.getKey() + "' persistence service instanciation";
				LOGGER.error(errorMessage, t);
				fireEvent(ApplicationContextListenerEventName.onError, errorMessage, t);
			}
		}
					
		LOGGER.debug("loadPersistenceAddons() [end]");
    }

	private void loadBroadcastPlugins() throws Exception {
    	LOGGER.debug("loadBroadcastPlugins() [start]");

    	Map<String, Class<?>> broadcastExtensions = getExtensions("broadcast");

		for (Entry<String, Class<?>> broadcastExtension : broadcastExtensions.entrySet()) {
			try {
				BroadcastService broadcastService = (BroadcastService) broadcastExtension.getValue().newInstance();
				broadcastServices.put(broadcastExtension.getKey(), broadcastService);
			} catch (Throwable t) {
				final String errorMessage = "An error occurred during '" + broadcastExtension.getKey() + "' broadcast service instanciation";
				LOGGER.error(errorMessage, t);
				fireEvent(ApplicationContextListenerEventName.onError, errorMessage, t);
			}
		}
					
		LOGGER.debug("loadBroadcastPlugins() [end]");
    }

    private void fireEvent(ApplicationContextListenerEventName eventName, final Object... args) {
    	LOGGER.trace("fireEvent(ApplicationContextListenerEventName, Object...) [start]");
    	for (WeakReference<ApplicationContextListener> weakReferenceAppContextListener : appContextListeners) {
    		ApplicationContextListener appContextListener = weakReferenceAppContextListener.get();
    		if (appContextListener != null) {
    			Object[] appContextAndArgs = new Object[args.length + 1];
    			appContextAndArgs[0] = this;
    			for (int i = 0; i < args.length; i++) {
    				appContextAndArgs[i+1] = args[i];
    			}
    			
				try {
					Method eventMethod = null;
					for (Method currentMethod : ApplicationContextListener.class.getMethods()) {
						if (currentMethod.getName().equals(eventName.name())) {
							eventMethod = currentMethod;
							break;
						}
					}
					
					if (eventMethod == null) {
						throw new IllegalStateException(ApplicationContextListener.class.getName() + "#" + eventName.name() + " not founded !");
					}
					
					eventMethod.invoke(appContextListener, appContextAndArgs);
				} catch (Exception e) {
					final String errorMessage = "A fatal error occurred on fire '" + eventName.name() + "' event";
					LOGGER.error(errorMessage, e);
					fireFatalError(errorMessage, e);
					return;
				}
    		}
    	}
    	LOGGER.trace("fireEvent(ApplicationContextListenerEventName, Object...) [end]");
    }
    
    private void fireFatalError(final String errorMessage, final Throwable fatalError) {
    	LOGGER.trace("fireFatalError(Throwable) [start]");

    	try {
			destroy();
		} catch (Exception e) {
			fireEvent(ApplicationContextListenerEventName.onWarning, "An error occurred during app context destroy");
		}

    	for (WeakReference<ApplicationContextListener> weakReferenceAppContextListener : appContextListeners) {
    		ApplicationContextListener appContextListener = weakReferenceAppContextListener.get();
    		if (appContextListener != null) {
    			appContextListener.onFatalError(this, errorMessage, fatalError);
    		}
    	}
    	
    	LOGGER.trace("fireFatalError(Throwable) [end]");
    }

}
