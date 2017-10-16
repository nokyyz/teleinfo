package org.openhab.binding.teleinfo.reader.io.stream.internal.converter;

public interface Converter {

    public Object convert(String value) throws ConvertionException;

}
