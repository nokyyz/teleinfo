package org.openhab.binding.teleinfo.reader.plugin.core.conf.defaultt;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.openhab.binding.teleinfo.reader.plugin.core.conf.Configuration;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ConfigurationDefinition;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.InvalidParameterValueException;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ParameterDefinition;

public class DefaultConfiguration implements Configuration {

	private final ConfigurationDefinition definition;
	private Map<String, Object> parameterValues;
	
	public DefaultConfiguration(final ConfigurationDefinition definition) {
		this.definition = definition;
		this.parameterValues = new HashMap<>();
	}
	
	@Override
	public ConfigurationDefinition getDefinition() {
		return definition;
	}

	@Override
	public Object getParameterValue(String parameterId) {
		return parameterValues.get(parameterId);
	}

	@Override
	public void setParameterValue(String parameterId, String stringValue) throws InvalidParameterValueException {
		ParameterDefinition def = getParameterDefinitionById(parameterId);
		if (def == null) {
			throw new InvalidParameterValueException(parameterId, "Unknown parameter id");
		}
		
		parameterValues.put(parameterId, convertValue(parameterId, stringValue));
	}
	
	protected Object convertValue(String parameterId, String stringValue) throws InvalidParameterValueException {
		Class<?> parameterType = getParameterDefinitionById(parameterId).getType();
		
		if (Integer.class.equals(parameterType)) {
			return convertIntegerValue(parameterId, stringValue);
		} else if (String.class.equals(parameterType)) {
			return stringValue;
		} else if (Double.class.equals(parameterType)) {
			return convertDoubleValue(parameterId, stringValue);
		} else if (Float.class.equals(parameterType)) {
			return convertFloatValue(parameterId, stringValue);
		} else if (Boolean.class.equals(parameterType)) {
			return convertBooleanValue(parameterId, stringValue);
		} else {
			throw new UnsupportedOperationException("No converter for the '" + parameterType + "' parameter type");
		}
	}

	protected String convertRegExpStringValue(String parameterId, String stringValue, String regExp) throws InvalidParameterValueException {
		if (Pattern.matches(regExp, stringValue)) {
			throw new InvalidParameterValueException(parameterId, "String value does not match to expected pattern (" + regExp + ")");
		}
		
		return stringValue;
	}

	private Integer convertIntegerValue(String parameterId, String stringValue) throws InvalidParameterValueException {
		try {
			return Integer.decode(stringValue);			
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException(parameterId, "Invalid integer value", e);
		}
	}

	private Double convertDoubleValue(String parameterId, String stringValue) throws InvalidParameterValueException {
		try {
			return Double.valueOf(stringValue);			
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException(parameterId, "Invalid double value", e);
		}
	}

	private Float convertFloatValue(String parameterId, String stringValue) throws InvalidParameterValueException {
		try {
			return Float.valueOf(stringValue);			
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException(parameterId, "Invalid float value", e);
		}
	}

	private Boolean convertBooleanValue(String parameterId, String stringValue) throws InvalidParameterValueException {
		try {
			return Boolean.valueOf(stringValue);			
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException(parameterId, "Invalid boolean value", e);
		}
	}

	private ParameterDefinition getParameterDefinitionById(String parameterId) {
		for (ParameterDefinition parameterDef : definition.getParametersDefinition()) {
			if (parameterDef.getId().equals(parameterId)) {
				return parameterDef;
			}
		}
		
		return null;
	}
}
