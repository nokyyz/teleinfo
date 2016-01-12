package org.openhab.binding.teleinfo.reader.io.serialport;

public class ConvertionException extends Exception {

    private static final long serialVersionUID = -1109821041874271681L;
    private static final String ERROR_MESSAGE = "Unable to convert '%1$s' value";

    private String valueToConvert;

    public ConvertionException(String valueToConvert) {
        this(valueToConvert, null);
    }

    public ConvertionException(String valueToConvert, Throwable cause) {
        super(String.format(ERROR_MESSAGE, valueToConvert), cause);
    }

    public String getValueToConvert() {
        return valueToConvert;
    }
}
