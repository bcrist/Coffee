package com.magicmoremagic.coffee.parser;

import java.io.IOException;
import java.io.PrintWriter;

public interface Lexer {
	Token peek() throws IOException;
	Token next() throws IOException;
	void push(Token token);
	
	void onParseError(String what, Throwable cause) throws ParseException;
	
	void serializeToken(PrintWriter pw, Token t);
	String getTokenName(Token t);
}
