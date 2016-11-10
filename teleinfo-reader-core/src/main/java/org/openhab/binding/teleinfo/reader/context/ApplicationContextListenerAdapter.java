package org.openhab.binding.teleinfo.reader.context;

import org.openhab.binding.teleinfo.reader.plugin.broadcast.BroadcastService;
import org.openhab.binding.teleinfo.reader.plugin.persistence.PersistenceService;

public class ApplicationContextListenerAdapter implements ApplicationContextListener {

	@Override
	public void onInitializing(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onInitialized(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onDestroying(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onDestroyed(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onPersistencePluginsLoading(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onPersistencePluginLoading(ApplicationContext appContext, String pluginFilename) {
		// NOP
	}

	@Override
	public void onPersistencePluginLoaded(ApplicationContext appContext, final String serviceId, PersistenceService persistenceServiceLoaded) {
		// NOP
	}

	@Override
	public void onPersistencePluginsLoaded(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onBroadcastPluginsLoading(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onBroadcastPluginLoading(ApplicationContext appContext, String pluginFilename) {
		// NOP
	}

	@Override
	public void onBroadcastPluginLoaded(ApplicationContext appContext, final String serviceId, BroadcastService broadcastServiceLoaded) {
		// NOP
	}

	@Override
	public void onBroadcastPluginsLoaded(ApplicationContext appContext) {
		// NOP
	}

	@Override
	public void onWarning(ApplicationContext appContext, String warning) {
		// NOP
	}

	@Override
	public void onError(ApplicationContext appContext, String errorMessage, Throwable trace) {
		// NOP
	}

	@Override
	public void onFatalError(ApplicationContext appContext, String errorMessage, Throwable fatalError) {
		// NOP
	}

	@Override
	public void onSerialPortOpening(ApplicationContext appContext, String serialPortName) {
		// NOP
	}

	@Override
	public void onSerialPortStarted(ApplicationContext appContext, String serialPortName) {
		// NOP
	}

	@Override
	public void onBroadcastPluginStopping(ApplicationContext appContext, String serviceId) {
		// NOP
	}

	@Override
	public void onBroadcastPluginStopped(ApplicationContext appContext, String serviceId) {
		// NOP
	}
}
