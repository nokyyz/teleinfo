package org.openhab.binding.teleinfo.reader.io.stream.internal.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerConverter implements Converter {

    private static Logger LOGGER = LoggerFactory.getLogger(IntegerConverter.class);

    @Override
    public Object convert(String value) throws ConvertionException {
        LOGGER.debug("convert(String) [start]");
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("value = " + value);
        }

        Object convertedValue = null;
        try {
            convertedValue = Integer.parseInt(value, 10);
        } catch (NumberFormatException e) {
            throw new ConvertionException(value, e);
        }

        LOGGER.debug("convert(String) [end]");
        return convertedValue;
    }

}
