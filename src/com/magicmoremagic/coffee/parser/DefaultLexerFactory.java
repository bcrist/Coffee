package com.magicmoremagic.coffee.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public enum DefaultLexerFactory implements LexerFactory {

	INSTANCE;
	
	@Override
	public Lexer getLexer(String resourceName) throws IOException {
		InputStream is = DefaultLexerFactory.class.getResourceAsStream("/" + resourceName);
		if (is == null) {
			is = new FileInputStream(resourceName);
		}
		
		return new DefaultLexer(is);
	}

}
