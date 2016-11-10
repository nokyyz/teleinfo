package org.openhab.binding.teleinfo.reader.context.conf;

import java.io.IOException;

import org.openhab.binding.teleinfo.reader.plugin.core.conf.Configuration;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ConfigurationDefinition;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.InvalidConfigurationException;

public interface ConfigurationPluginHandler {

	Configuration getConfiguration(String pluginId, final ConfigurationDefinition configurationDefinition) throws InvalidConfigurationException, MissingConfigurationException, IOException;
	
	void onInvalidConfiguration(final InvalidConfigurationException error, String pluginId);
	
}
