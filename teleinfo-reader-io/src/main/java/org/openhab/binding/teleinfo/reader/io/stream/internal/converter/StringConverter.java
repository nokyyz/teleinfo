package org.openhab.binding.teleinfo.reader.io.stream.internal.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringConverter implements Converter {

    private static Logger LOGGER = LoggerFactory.getLogger(StringConverter.class);

    @Override
    public Object convert(String value) throws ConvertionException {
        LOGGER.debug("convert(String) [start]");
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("value = " + value);
        }

        LOGGER.debug("convert(String) [end]");
        return value;
    }

}
