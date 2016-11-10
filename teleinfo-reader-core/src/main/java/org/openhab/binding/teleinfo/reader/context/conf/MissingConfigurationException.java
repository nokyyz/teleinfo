package org.openhab.binding.teleinfo.reader.context.conf;

public class MissingConfigurationException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7426053822558191120L;

	private String serviceId;

	public MissingConfigurationException(String serviceId) {
		super("The configuration for '" + serviceId + "' service is missing");
	}

	public String getServiceId() {
		return serviceId;
	}

}
