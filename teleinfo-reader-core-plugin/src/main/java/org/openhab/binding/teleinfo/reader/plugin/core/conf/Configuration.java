package org.openhab.binding.teleinfo.reader.plugin.core.conf;

public interface Configuration {

	ConfigurationDefinition getDefinition();
	
	Object getParameterValue(String parameterId);
	
	void setParameterValue(String parameterId, String stringValue) throws InvalidParameterValueException;
	
}
