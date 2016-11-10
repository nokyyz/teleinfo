package org.openhab.binding.teleinfo.reader.plugin.core.conf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvalidConfigurationException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7923401935216985514L;
	
	private Configuration faultyConfiguration;
	private List<String> invalidMessages;
	
	public InvalidConfigurationException() {
		// default constructor
		invalidMessages = new ArrayList<>();
	}

	public InvalidConfigurationException(Configuration faultyConfiguration) {
		this.faultyConfiguration = faultyConfiguration;
	}
	
	public Configuration getFaultyConfiguration() {
		return faultyConfiguration;
	}
	
	public void addInvalidMessage(final String invalidMessage) {
		invalidMessages.add(invalidMessage);
	}
	
	public List<String> getInvalidMessages() {
		return Collections.unmodifiableList(invalidMessages);
	}
	
	public boolean hasInvalidMessages() {
		return !invalidMessages.isEmpty();
	}

	@Override
	public String getMessage() {
		if (invalidMessages.size() == 1) {
			return invalidMessages.get(0);
		} else {
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append("The configuration is invalid. Many errors occurred:");
			for (String invalidMessage : invalidMessages) {
				strBuffer.append("- " + invalidMessage);
			}
			return strBuffer.toString();
		}
	}
}
