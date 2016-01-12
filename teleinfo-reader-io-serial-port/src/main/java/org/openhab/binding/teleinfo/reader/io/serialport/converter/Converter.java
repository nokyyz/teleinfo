package org.openhab.binding.teleinfo.reader.io.serialport.converter;

import org.openhab.binding.teleinfo.reader.io.serialport.ConvertionException;

public interface Converter {

    public Object convert(String value) throws ConvertionException;

}
