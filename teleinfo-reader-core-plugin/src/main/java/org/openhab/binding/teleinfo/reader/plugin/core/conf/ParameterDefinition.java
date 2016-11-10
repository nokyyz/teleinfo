package org.openhab.binding.teleinfo.reader.plugin.core.conf;

public interface ParameterDefinition {

	/**
	 * Parameter's id
	 * @return
	 */
	String getId();
	
	String getDescription();
	
	boolean isRequired();
	
	Class<?> getType();
	
}
