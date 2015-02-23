package com.magicmoremagic.coffee.parser;

public class UnrecognizedTokenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnrecognizedTokenException() {
	}
	    
	public UnrecognizedTokenException(String what) {
		super(what);
	}
	  
	public UnrecognizedTokenException(String what, Throwable source) {
		super(what, source);
	}
	  
	public UnrecognizedTokenException(Throwable source) {
		super(source);
	}

}
