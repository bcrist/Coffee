package com.magicmoremagic.coffee.parser;

public class Token {
	private TokenType type;
	private String content;
	private double value = Double.NaN;
	
	public static final Token NULL = new Token(null);
	
	public static final Token PRIMARY_NT_OP = new Token(TokenType.PRIMARY_NT_OP);
	public static final Token NT_OP = new Token(TokenType.NT_OP);
	public static final Token PROD_OP = new Token(TokenType.PROD_OP);
	public static final Token COMMAND_OP = new Token(TokenType.COMMAND_OP);
	public static final Token LITERAL = new Token(TokenType.LITERAL);
	public static final Token IDENTIFIER = new Token(TokenType.IDENTIFIER);
	public static final Token NUMBER = new Token(TokenType.NUMBER);
	
	public Token(TokenType type) {
		this.type = type;
	}
	
	public Token(TokenType type, String content) {
		if (type == null) {
			throw new NullPointerException();
		}
		
		this.type = type;
		this.content = content;
	}
	
	public Token(double value) {
		this.type = TokenType.NUMBER;
		this.value = value;
	}
	
	public TokenType getType() {
		return type;
	}
	
	public String getContent() {
		return content;
	}
	
	public double getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Token) {
			Token tokenOther = (Token)other;
			
			return type == tokenOther.type &&
					(content == null && tokenOther.content == null || content.equals(tokenOther)) &&
					value == tokenOther.value;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		long c = type.ordinal();
		c = 47 * c + Double.doubleToLongBits(c);
		c = 31 * c + (content == null ? 0 : content.hashCode());
		
		return (int) (c ^ (c >> 32));
	}
	
	@Override
	public String toString() {
		switch (type) {
		case PRIMARY_NT_OP:
			return "primary_nt_op";
		case NT_OP:
			return "nt_op";
		case PROD_OP:
			return "prod_op";
		case COMMAND_OP:
			return "command_op";
		case IDENTIFIER:
			if (content == null) {
				return "identifier";
			} else {
				return "identifier( " + StringUtil.toIdentifier(content) + " )";
			}
		case LITERAL:
			if (content == null) {
				return "literal";
			} else {
				return "literal( " + StringUtil.toLiteral(content) + " )";
			}
		case NUMBER:
			if (Double.isNaN(value)) {
				return "number";
			} else {
				return "number( " + Double.toString(value) + " )";
			}
		default:
			throw new AssertionError("Unknown token type!");
		}
	}
}
