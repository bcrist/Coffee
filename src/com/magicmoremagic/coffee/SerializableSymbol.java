package com.magicmoremagic.coffee;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Random;

import com.magicmoremagic.coffee.parser.Lexer;

public interface SerializableSymbol extends Symbol {
	String brew();
	String brew(Random rnd);
	
	String serialize();
	String serialize(Lexer lexer);
	void serialize(OutputStream os);
	void serialize(OutputStream os, Lexer lexer);
	void serialize(Writer w);
	void serialize(Writer w, Lexer lexer);
	void serialize(PrintWriter pw);
	void serialize(PrintWriter pw, Lexer lexer);
}
