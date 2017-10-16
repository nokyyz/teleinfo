package org.openhab.binding.teleinfo.reader.context;

import java.nio.file.Path;

import org.openhab.binding.teleinfo.reader.context.conf.ConfigurationPluginHandler;

public interface ApplicationContext {

	void init() throws Exception;
	
	void destroy() throws Exception;
	
	void setSerialPortName(String value);
	
	void setRefreshInterval(long value);

	void setPluginsFolder(Path value);

	void setPluginsConfigurationHandler(final ConfigurationPluginHandler handler);
	
	void addListener(final ApplicationContextListener appContextListener);

	void removeListener(final ApplicationContextListener appContextListener);

}
