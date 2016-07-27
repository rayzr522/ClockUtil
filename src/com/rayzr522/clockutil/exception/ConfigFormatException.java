package com.rayzr522.clockutil.exception;


public class ConfigFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2614407193914009531L;
	
	
	public ConfigFormatException(String type, String field) {
		
		super("The field '" + field + "' for the type '" + type + "' was missing");
		
	}

}
