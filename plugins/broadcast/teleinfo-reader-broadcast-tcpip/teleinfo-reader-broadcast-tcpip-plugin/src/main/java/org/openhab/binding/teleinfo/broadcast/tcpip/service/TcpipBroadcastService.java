package org.openhab.binding.teleinfo.broadcast.tcpip.service;

import java.net.InetAddress;

import org.openhab.binding.teleinfo.broadcast.tcpip.core.TeleinfoTcpipServer;
import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.plugin.broadcast.BroadcastService;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ConfigurableService;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.Configuration;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.ConfigurationDefinition;
import org.openhab.binding.teleinfo.reader.plugin.core.conf.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpipBroadcastService implements BroadcastService, ConfigurableService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpipBroadcastService.class);
	
	private static TcpipBroadcastServiceConfigurationDefinition configurationDefinition;
	static {
		configurationDefinition = new TcpipBroadcastServiceConfigurationDefinition();
	}
	
	private TeleinfoTcpipServer teleinfoTcpipServer;
	private Configuration configuration;
	
	public TcpipBroadcastService() {
		// default constructor
	}
	
	@Override
	public void broadcast(Frame teleinfoFrame) {
		LOGGER.debug("broadcast() [start]");

		teleinfoTcpipServer.broadcast(teleinfoFrame);
		
		LOGGER.debug("broadcast() [end]");
	}
	
	@Override
	public void init() {
		LOGGER.debug("init() [start]");
		
		if (configuration == null) {
			throw new IllegalArgumentException("Configuration is missing. The configuration must be defined");
		}
		
		InetAddress serverAddress = (InetAddress) configuration.getParameterValue(TcpipBroadcastServiceConfigurationDefinition.PARAMETER_HOSTNAME);
		int serverPort = (Integer) configuration.getParameterValue(TcpipBroadcastServiceConfigurationDefinition.PARAMETER_PORT);
		LOGGER.debug("serverAddress = " + serverAddress);
		LOGGER.debug("serverPort = " + serverPort);
		teleinfoTcpipServer = new TeleinfoTcpipServer(serverAddress, serverPort);
		
		try {
			teleinfoTcpipServer.start();
		} catch (Exception e) {
			final String error = "An error occurred during TCP/IP server start-up";
			throw new IllegalStateException(error, e);
		}

		LOGGER.debug("init() [end]");
	}

	@Override
	public void destroy() {
		LOGGER.debug("destroy() [start]");

		teleinfoTcpipServer.stop();
		
		LOGGER.debug("destroy() [end]");
	}
	
	@Override
	public ConfigurationDefinition getConfigurationDefinition() {
		return configurationDefinition;
	}

	@Override
	public void setConfiguration(Configuration conf) throws InvalidConfigurationException {
		configurationDefinition.validate(conf);
		
		this.configuration = conf;
	}
}
