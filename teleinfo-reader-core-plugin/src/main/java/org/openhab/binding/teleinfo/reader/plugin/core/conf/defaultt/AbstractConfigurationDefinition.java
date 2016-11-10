package org.openhab.binding.teleinfo.reader.plugin.core.conf.defaultt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openhab.binding.teleinfo.reader.plugin.core.conf.ConfigurationDefinition;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ParameterDefinition;

public abstract class AbstractConfigurationDefinition implements ConfigurationDefinition {
	
	private List<ParameterDefinition> parameterDefinitions;
	private URL documentationURL;

	public AbstractConfigurationDefinition() {
		this(null);
	}

	public AbstractConfigurationDefinition(String documentationURL) {
		this.parameterDefinitions = new ArrayList<>();
		
		if (documentationURL != null) {
			try {
				this.documentationURL = new URL(documentationURL);
			} catch (MalformedURLException e) {
				// NOP
				throw new IllegalStateException(e);
			}			
		}
	}

	public void addParameterDefinition(final ParameterDefinition parameterDef) {
		parameterDefinitions.add(parameterDef);
	}
	
	public void addParameterDefinition(String parameterId, boolean isRequired, Class<?> type) {
		addParameterDefinition(parameterId, null, isRequired, type);
	}

	public void addParameterDefinition(String parameterId, String description, boolean isRequired, Class<?> type) {
		parameterDefinitions.add(new DefaultParameterDefinition(parameterId, description, isRequired, type));
	}

	@Override
	public List<ParameterDefinition> getParametersDefinition() {
		return Collections.unmodifiableList(parameterDefinitions);
	}
	
	@Override
	public URL getDocumentationURL() {
		return documentationURL;
	}
}
