package com.magicmoremagic.coffee.parser;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class DefaultLexer implements Lexer {
	
	private Reader reader;
	private List<Token> tokens;
	private boolean canPush;
	
	public static final DefaultLexer DEFAULT = new DefaultLexer();
	
	private DefaultLexer() {
		this(new StringReader(""));
		canPush = false;
	}
	
	public DefaultLexer(String input) {
		this(new StringReader(input));
	}
	
	public DefaultLexer(InputStream is) {
		this(makeInputStreamReader(is));
	}
	
	private static Reader makeInputStreamReader(InputStream is) {
		try {
			return new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	public DefaultLexer(Reader reader) {
		if (reader.markSupported()) {
			this.reader = reader;
		} else {
			this.reader = new BufferedReader(reader);
		}
		
		tokens = new LinkedList<>();
		canPush = true;
	}
	
	@Override
	public Token peek() throws IOException {
		if (tokens.isEmpty()) {
			Token t = readToken();
			if (t != null) {
				tokens.add(t);
				return t;
			}
			return null;
		} else {
			return tokens.get(0);
		}
	}
	
	@Override
	public Token next() throws IOException {
		if (tokens.isEmpty()) {
			return readToken();
		} else {
			return tokens.remove(0);
		}
	}
	
	@Override
	public void push(Token token) {
		if (!canPush) {
			throw new UnsupportedOperationException();
		}
		
		if (token == null) {
			throw new NullPointerException();
		}
		tokens.add(0, token);
	}
	
	@Override
	public void onParseError(String what, Throwable cause) throws ParseException {
		if (cause != null) {
			throw new ParseException(what, cause);
		}
		throw new ParseException(what);
	}
	
	@Override
	public void serializeToken(PrintWriter pw, Token t) {
		switch (t.getType()) {
			case PRIMARY_NT_OP:	pw.print(":::"); break;
			case NT_OP:			pw.print("::"); break;
			case PROD_OP:		pw.print("]"); break;
			case COMMAND_OP:	pw.print("!"); break;
			case IDENTIFIER:	pw.print(StringUtil.toIdentifier(t.getContent())); break;
			case LITERAL:		pw.print(StringUtil.toLiteral(t.getContent())); break;
			case NUMBER:
				if (Math.abs((int)t.getValue() - t.getValue()) <= Math.ulp(t.getValue()) * 2) {
					pw.print(Integer.toString((int)t.getValue()));
				} else {
					pw.print(Double.toString(t.getValue()));
				}
				break;
			default:
				throw new AssertionError("Unknown TokenType!");
		}
	}
	
	@Override
	public String getTokenName(Token t) {
		switch (t.getType()) {
			case PRIMARY_NT_OP:	return "':::'";
			case NT_OP:			return "'::'";
			case PROD_OP:		return "']'";
			case COMMAND_OP:	return "'!'";
			default:
				return t.toString();
		}
	}
	
	private Token readToken() throws IOException {
		Token token = null;
		do {
			int cui = reader.read();
			if (cui < 0) {
				return null;
			}
			
			char cu = (char)cui;
			
			if (cu >= 'a' && cu <= 'z' ||
				cu >= 'A' && cu <= 'Z' ||
				cu == '_' || cu == '$' || cu == '\\') {
				StringBuilder sb = new StringBuilder();
				sb.append(cu);
				readIdentifier(sb);
				token = new Token(TokenType.IDENTIFIER, sb.toString());
			} else if (cu >= '0' && cu <= '9' || cu == '.') {
				StringBuilder sb = new StringBuilder();
				sb.append(cu);
				readNumber(sb);
				token = new Token(Double.valueOf(sb.toString()));
			} else if (cu <= ' ') {
				continue;
			} else switch(cu) {
				case '"':
				{
					StringBuilder sb = new StringBuilder();
					readLiteral(sb);
					token = new Token(TokenType.LITERAL, sb.toString());
					break;
				}
				
				case ':':
				{
					int cu2 = reader.read();
					if (cu2 == ':') {
						reader.mark(1);
						int cu3 = reader.read();
						if (cu3 == ':') {
							token = Token.PRIMARY_NT_OP;
						} else {
							reader.reset();
							token = Token.NT_OP;
						}
					} else {
						throw new UnrecognizedTokenException("Unexpected ':'; expected '::' or ':::'!");
					}
					break;
				}
				
				case ']':
					token = Token.PROD_OP;
					break;
					
				case '!':
					token = Token.COMMAND_OP;
					break;
					
				case '#':
					skipLine();
					break;
					
				case '-':
				{
					int cu2 = reader.read();
					if (cu2 == '-') {
						skipLine();
					} else {
						throw new UnrecognizedTokenException("Unexpected '-'; expected '--'!");
					}
					break;
				}
					
				case '/':
				{
					int cu2 = reader.read();
					if (cu2 == '/') {
						skipLine();
					} else if (cu2 == '*') {
						skipMultiLineComment();
					} else {
						throw new UnrecognizedTokenException("Unexpected '/'; expected '//' or '/*'!");
					}
					break;
				}
				
				default:
					throw new UnrecognizedTokenException("Unexpected '" + Character.toString(cu) + "'!");
			}
		
		} while (token == null);
		
		return token;
	}

	private void skipMultiLineComment() throws IOException  {
		for (;;) {
			int cu = reader.read();
			if (cu < 0) {
				throw new UnrecognizedTokenException("Unexpected end-of-input while parsing multi-line comment!");
			}
			
			if (cu == '*') {
				reader.mark(1);
				int cu2 = reader.read();
				if (cu2 == '/') {
					return;
				} else {
					reader.reset();
				}
			}
		}
	}

	private void skipLine() throws IOException {
		for (;;) {
			int cu = reader.read();
			if (cu < 0 || cu == '\n') {
				return;
			}
		}
	}

	private void readLiteral(StringBuilder sb) throws IOException  {
		for (;;) {
			int cu = reader.read();
			if (cu < 0) {
				throw new UnrecognizedTokenException("Unexpected end-of-input while parsing string literal!");
			}
			
			if (cu == '"') {
				return;
			} else if (cu == '\\') {
				readEscape(sb);
			} else {
				sb.append((char)cu);
			}
		}
	}

	private void readEscape(StringBuilder sb) throws IOException {
		int cu = reader.read();
		if (cu < 0) {
			throw new UnrecognizedTokenException("Unexpected end-of-input while parsing escape sequence!");
		}

		switch (cu) {	
			case 'n':
				sb.append('\n');
				return;
				
			case 'r':
				sb.append('\r');
				return;
				
			case 't':
				sb.append('\t');
				return;
				
			case 'v':
				sb.append((char)11);
				return;
				
			case 'b':
				sb.append('\b');
				return;
				
			case 'f':
				sb.append('\f');
				return;
				
			case 'x':
				readCodepoint(sb, 2);
				return;
			
			case 'u':
				readCodepoint(sb, 4);
				return;
				
			case 'U':
				readCodepoint(sb, 8);
				return;
				
			default:
				if (cu < 0) {
					throw new UnrecognizedTokenException("Unexpected end-of-input while parsing escape sequence!");
				} else {
					sb.append((char)cu);
				}
		}
	}
	
	private void readCodepoint(StringBuilder sb, final int nibbles) throws IOException {
		int cp = 0;
		for (int i = 0; i < nibbles; ++i) {
			int cu = reader.read();
			if (cu >= '0' && cu <= '9') {
				cu -= '0';
			} else if (cu >= 'a' && cu <= 'f') {
				cu -= 'a';
				cu += 10;
			} else if (cu >= 'A' && cu <= 'F') {
				cu -= 'A';
				cu += 10;
			} else {
				throw new UnrecognizedTokenException();
			}
			cp = (cp << 4) | cu;
		}
		sb.append(Character.toChars(cp));
	}

	private void readNumber(StringBuilder sb) throws IOException  {
		int state = sb.charAt(0) == '.' ? 1 : 0;
		for (;;) {
			reader.mark(1);
			int cu = reader.read();
			switch (state) {	
			case 0:
				// digits before .
				if (cu >= '0' && cu <= '9') {
					sb.append((char)cu);
					continue;
				} else if (cu == '.') {
					sb.append((char)cu);
					state = 1;
					continue;
				} else if (cu == 'e' || cu == 'E') {
					sb.append((char)cu);
					state = 2;
				}
				break;
			
			case 1:
				// digits after .
				if (cu >= '0' && cu <= '9') {
					sb.append((char)cu);
					continue;
				} else if (cu == 'e' || cu == 'E') {
					sb.append((char)cu);
					state = 2;
				}
				break;
			
			case 2:
				// +/- or exponent digit
				if (cu >= '0' && cu <= '9' || cu == '+' || cu == '-') {
					sb.append((char)cu);
					state = 3;
					continue;
				}
				break;
			
			case 3:
				// exponent digit
				if (cu >= '0' && cu <= '9') {
					sb.append((char)cu);
					continue;
				}
				break;
			
			}
			reader.reset();
			return;
		}
	}

	private void readIdentifier(StringBuilder sb) throws IOException  {
		if (sb.charAt(0) == '\\') {
			readEscape(sb);
		}
		
		for (;;) {
			reader.mark(1);
			int cu = reader.read();
			
			if (cu >= 'a' && cu <= 'z' ||
				cu >= 'A' && cu <= 'Z' ||
				cu >= '0' && cu <= '9' ||
				cu == '_' || cu == '$') {
				sb.append((char)cu);
			} else if (cu == '\\') {
				readEscape(sb);
			} else {
				reader.reset();
				return;
			}
		}
	}
	
}
