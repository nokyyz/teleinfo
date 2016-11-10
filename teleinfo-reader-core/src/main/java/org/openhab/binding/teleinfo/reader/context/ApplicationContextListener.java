package org.openhab.binding.teleinfo.reader.context;

import org.openhab.binding.teleinfo.reader.plugin.broadcast.BroadcastService;
import org.openhab.binding.teleinfo.reader.plugin.persistence.PersistenceService;

public interface ApplicationContextListener {

	void onInitializing(final ApplicationContext appContext);

	void onInitialized(final ApplicationContext appContext);

	void onDestroying(final ApplicationContext appContext);

	void onDestroyed(final ApplicationContext appContext);
	
	void onWarning(final ApplicationContext appContext, final String warningMessage);

	void onError(final ApplicationContext appContext, final String errorMessage, final Throwable trace);

	void onFatalError(final ApplicationContext appContext, final String errorMessage, final Throwable fatalError);
	
	void onPersistencePluginsLoading(final ApplicationContext appContext);

	void onPersistencePluginLoading(final ApplicationContext appContext, final String pluginFilename);

	void onPersistencePluginLoaded(final ApplicationContext appContext, final String serviceId, final PersistenceService persistenceServiceLoaded);

	void onPersistencePluginsLoaded(final ApplicationContext appContext);

	void onBroadcastPluginsLoading(final ApplicationContext appContext);

	void onBroadcastPluginLoading(final ApplicationContext appContext, final String pluginFilename);

	void onBroadcastPluginLoaded(final ApplicationContext appContext, final String serviceId, final BroadcastService broadcastServiceLoaded);

	void onBroadcastPluginStopping(final ApplicationContext appContext, final String serviceId);

	void onBroadcastPluginStopped(final ApplicationContext appContext, final String serviceId);

	void onBroadcastPluginsLoaded(final ApplicationContext appContext);

	void onSerialPortOpening(final ApplicationContext appContext, String serialPortName);

	void onSerialPortStarted(final ApplicationContext appContext, String serialPortName);
	
}
