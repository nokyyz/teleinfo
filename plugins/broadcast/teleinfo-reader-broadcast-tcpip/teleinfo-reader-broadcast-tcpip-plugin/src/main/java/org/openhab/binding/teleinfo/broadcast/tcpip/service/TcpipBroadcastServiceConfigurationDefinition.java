package org.openhab.binding.teleinfo.broadcast.tcpip.service;

import org.openhab.binding.teleinfo.reader.plugin.core.conf.Configuration;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.InvalidConfigurationException;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.defaultt.AbstractConfigurationDefinition;

public class TcpipBroadcastServiceConfigurationDefinition extends AbstractConfigurationDefinition {

	public static final String PARAMETER_HOSTNAME = "server.hostname";
	public static final String PARAMETER_PORT = "server.port";
	
	public TcpipBroadcastServiceConfigurationDefinition() {
		addParameterDefinition(PARAMETER_HOSTNAME, "A null address will assign the wildcard address", false, String.class);
		addParameterDefinition(PARAMETER_PORT, "A valid port value is between 0 and 65535. A port number of zero will let the system pick up an ephemeral port in a bind operation", true, Integer.class);
	}

	@Override
	public void validate(Configuration configurationToValidate) throws InvalidConfigurationException {
		InvalidConfigurationException invalidConfigurationException = new InvalidConfigurationException();
		
		// validate port range
		Integer port = (Integer) configurationToValidate.getParameterValue(PARAMETER_PORT);
		if (port < 0 || port > 65535) {
			invalidConfigurationException.addInvalidMessage("Port range is 0-65535 (actual: " + port +")");
		}
		
		if (invalidConfigurationException.hasInvalidMessages()) {
			throw invalidConfigurationException;
		}
	}

}
