package org.openhab.binding.teleinfo.reader.plugin.core.conf.defaultt;

import org.openhab.binding.teleinfo.reader.plugin.core.conf.ParameterDefinition;

public class DefaultParameterDefinition implements ParameterDefinition {

	private String id;
	private String description;
	private boolean isRequired;
	private Class<?> type;

	public DefaultParameterDefinition() {
		// default constructor
	}

	public DefaultParameterDefinition(String id, boolean isRequired, Class<?> type) {
		this(id, null, isRequired, type);
	}

	public DefaultParameterDefinition(String id, String description, boolean isRequired, Class<?> type) {
		this.id = id;
		this.description = description;
		this.isRequired = isRequired;
		this.type = type;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
}
