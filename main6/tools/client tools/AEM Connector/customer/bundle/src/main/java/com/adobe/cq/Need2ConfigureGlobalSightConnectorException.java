package com.adobe.cq;

public class Need2ConfigureGlobalSightConnectorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = null;
	
	public Need2ConfigureGlobalSightConnectorException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}
