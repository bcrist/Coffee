package com.magicmoremagic.coffee;

import java.io.PrintWriter;
import java.util.Random;

import com.magicmoremagic.coffee.parser.Lexer;
import com.magicmoremagic.coffee.parser.StringUtil;
import com.magicmoremagic.coffee.parser.Token;
import com.magicmoremagic.coffee.parser.TokenType;

public class Terminal extends AbstractSymbol {

	private String content;
	
	public Terminal() {
	}
	
	public Terminal(String content) {
		this.content = content;
	}
	
	public Terminal(Terminal other) {
		this.content = other.content;
	}
	
	@Override
	public Terminal clone() {
		return new Terminal(this);
	}
	
	public void set(String content) {
		this.content = content;
	}
	
	public String get() {
		return content;
	}
	
	@Override
	public void brew(Random rnd, StringBuilder dest) {
		dest.append(content);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Terminal) {
			Terminal terminalOther = (Terminal)other;
			
			if (content == null) {
				return terminalOther.content == null;
			} else {
				return content.equals(terminalOther.content);
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return content.hashCode();
	}
	
	@Override
	public String toString() {
		return StringUtil.toLiteral(content);
	}
	
	@Override
	public void serialize(PrintWriter pw, Lexer lexer) {
		if (lexer == null) {
			lexer = getDefaultLexer();
		}
		
		lexer.serializeToken(pw, new Token(TokenType.LITERAL, content));
		pw.flush();
	}
	
}
