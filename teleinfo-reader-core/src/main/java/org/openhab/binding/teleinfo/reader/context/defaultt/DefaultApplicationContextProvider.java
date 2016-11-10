package org.openhab.binding.teleinfo.reader.context.defaultt;

import java.io.IOException;

import org.openhab.binding.teleinfo.reader.context.ApplicationContext;
import org.openhab.binding.teleinfo.reader.context.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultApplicationContextProvider extends ApplicationContextProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationContextProvider.class);

    public DefaultApplicationContextProvider() {
    	LOGGER.debug("DefaultApplicationContextProvider instantiation...");
    }

    @Override
	public ApplicationContext createApplicationContext() throws IOException {
		return new DefaultApplicationContext();
	}
}
