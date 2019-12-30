package com.bill.onitama.engine;

public class OnitamaException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ExceptionType {
		InvalidInitialLayout
	}
	
	private final ExceptionType type;

	public OnitamaException(ExceptionType type) {
		super();
		this.type = type;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ExceptionType getType() {
		return type;
	}
	
	
}
