package org.openhab.binding.teleinfo.reader.plugin.core.conf;

import java.net.URL;
import java.util.List;

public interface ConfigurationDefinition {

	List<ParameterDefinition> getParametersDefinition();
	
	URL getDocumentationURL();
	
	void validate(final Configuration configurationToValidate) throws InvalidConfigurationException;
	
}
