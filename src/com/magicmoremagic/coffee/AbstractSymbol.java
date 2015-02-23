package com.magicmoremagic.coffee;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Random;

import com.magicmoremagic.coffee.parser.DefaultLexer;
import com.magicmoremagic.coffee.parser.Lexer;

public abstract class AbstractSymbol implements SerializableSymbol {

	@Override
	public abstract AbstractSymbol clone();

	@Override
	public abstract void serialize(PrintWriter pw, Lexer lexer);
	
	protected Lexer getDefaultLexer() {
		return DefaultLexer.DEFAULT;
	}
	
	@Override
	public String brew() {
		StringBuilder sb = new StringBuilder();
		brew(new Random(), sb);
		return sb.toString();
	}
	
	@Override
	public String brew(Random rnd) {
		StringBuilder sb = new StringBuilder();
		brew(rnd, sb);
		return sb.toString();
	}
	
	@Override
	public String serialize() {
		StringWriter sw = new StringWriter();
		serialize(new PrintWriter(sw, false), getDefaultLexer());
		return sw.toString();
	}
	
	@Override
	public String serialize(Lexer lexer) {
		StringWriter sw = new StringWriter();
		serialize(new PrintWriter(sw, false), lexer);
		return sw.toString();
	}
	
	@Override
	public void serialize(OutputStream os) {
		try {
			serialize(new PrintWriter(new OutputStreamWriter(os, "UTF-8"), false), getDefaultLexer());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	@Override
	public void serialize(OutputStream os, Lexer lexer) {
		try {
			serialize(new PrintWriter(new OutputStreamWriter(os, "UTF-8"), false), lexer);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	@Override
	public void serialize(Writer w) {
		serialize(new PrintWriter(w, false), getDefaultLexer());
	}
	
	@Override
	public void serialize(Writer w, Lexer lexer) {
		serialize(new PrintWriter(w, false), lexer);
	}
	
	@Override
	public void serialize(PrintWriter pw) {
		serialize(pw, getDefaultLexer());
	}

}
