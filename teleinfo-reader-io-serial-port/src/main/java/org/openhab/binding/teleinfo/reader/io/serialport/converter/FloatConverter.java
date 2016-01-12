package org.openhab.binding.teleinfo.reader.io.serialport.converter;

import org.openhab.binding.teleinfo.reader.io.serialport.ConvertionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloatConverter implements Converter {

    private static Logger LOGGER = LoggerFactory.getLogger(FloatConverter.class);

    @Override
    public Object convert(String value) throws ConvertionException {
        LOGGER.debug("convert(String) [start]");
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("value = " + value);
        }

        Object convertedValue = null;
        try {
            convertedValue = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new ConvertionException(value, e);
        }

        LOGGER.debug("convert(String) [end]");
        return convertedValue;
    }

}
