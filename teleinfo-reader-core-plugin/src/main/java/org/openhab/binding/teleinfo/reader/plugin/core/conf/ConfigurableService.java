package org.openhab.binding.teleinfo.reader.plugin.core.conf;

public interface ConfigurableService {

	ConfigurationDefinition getConfigurationDefinition();
	
	void setConfiguration(final Configuration conf) throws InvalidConfigurationException;
	
}
