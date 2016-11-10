package org.openhab.binding.teleinfo.reader.plugin.core.conf;

public class InvalidParameterValueException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 600377521904837319L;

	private String parameterId;

	public InvalidParameterValueException(String parameterId, String message) {
		this(parameterId, message, null);
	}

	public InvalidParameterValueException(String parameterId, String message, Throwable t) {
		super(message, t);
		this.parameterId = parameterId;
	}

	public String getParameterId() {
		return parameterId;
	}

}
