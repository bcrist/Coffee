package com.magicmoremagic.coffee.parser;

import java.io.IOException;

public interface LexerFactory {

	Lexer getLexer(String resourceName) throws IOException;
	
}
