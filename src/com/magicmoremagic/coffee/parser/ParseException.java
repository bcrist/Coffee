package com.magicmoremagic.coffee.parser;

import java.io.IOException;

public class ParseException extends IOException {
	
	private static final long serialVersionUID = 1L;

	public ParseException() {
	}
	    
	public ParseException(String what) {
		super(what);
	}
	  
	public ParseException(String what, Throwable source) {
		super(what, source);
	}
	  
	public ParseException(Throwable source) {
		super(source);
	}
}
