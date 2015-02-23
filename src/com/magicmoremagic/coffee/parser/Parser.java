package com.magicmoremagic.coffee.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.magicmoremagic.coffee.Nonterminal;
import com.magicmoremagic.coffee.NonterminalSet;
import com.magicmoremagic.coffee.Production;
import com.magicmoremagic.coffee.Terminal;

public class Parser extends NonterminalSet {
	
	private LexerFactory lexerFactory;
	private Lexer lex;
	private List<Task> tasks;
	private Set<String> imports;
	private int inProgress;
	
	public Parser() { }
	
	public void parse(String input) throws IOException {
		parse(new DefaultLexer(input));
	}
	
	public void parse(String input, LexerFactory lf) throws IOException {
		parse(new DefaultLexer(input), lf);
	}

	public void parse(InputStream is) throws IOException {
		parse(new DefaultLexer(is));
	}
	
	public void parse(InputStream is, LexerFactory lf) throws IOException {
		parse(new DefaultLexer(is), lf);
	}
	
	public void parse(Reader reader) throws IOException {
		parse(new DefaultLexer(reader));
	}
	
	public void parse(Reader reader, LexerFactory lf) throws IOException {
		parse(new DefaultLexer(reader), lf);
	}
	
	public void parseResource(String resourceName) throws IOException {
		parseResource(DefaultLexerFactory.INSTANCE, resourceName);
	}
	
	public void parseResource(LexerFactory lf, String resourceName) throws IOException {
		parse(lf.getLexer(resourceName), lf);
	}
	
	public void parse(Lexer lexer) throws IOException {
		parse(lexer, DefaultLexerFactory.INSTANCE);
	}
	
	public void parse(Lexer lexer, LexerFactory lf) throws IOException {
		init(lexer, lf);
		
		try {
			content();
		} catch (Exception e) {
			deinit();
			throw e;
		}
		
		tryDeinit();
	}
	
	private void init(Lexer lexer, LexerFactory lf) {
		setDefaultLexer(lexer);
		lex = lexer;
		lexerFactory = lf;
		
		if (tasks == null) {
			tasks = new ArrayList<>();
		}
		if (imports == null) {
			imports = new HashSet<>();
		}
		++inProgress;
	}
	
	private void tryDeinit() throws IOException {
		--inProgress;
		if (inProgress == 0) {
			try {
				for (Task t : tasks) {
					t.run();
				}
			} catch (Exception e) {
				throw e;
			} finally {
				deinit();
			}
		}
	}
	
	private void deinit() {
		if (tasks != null) {
			tasks.clear();
			tasks = null;
		}
		
		if (imports != null) {
			imports.clear();
			imports = null;
		}
		
		inProgress = 0;
		lex = null;
		lexerFactory = null;
	}
	
	private String getTokenName(Token t) {
		if (t == null) {
			return "end-of-input";
		} else {
			return lex.getTokenName(t);
		}
	}
	
	/*
	 * content = expression content |
	 *           ;
	 *           
	 * expression = command |
	 *              nonterminal ;
	 *              
	 * command = "!" identifier literal ;
	 * 
	 * nonterminal = ("::" | ":::") identifier production_list ;
	 * 
	 * production_list = production production_list |
	 *                   ;
	 * 
	 * production = number "]" symbol_list |
	 *              "]" symbol_list ;
	 *              
	 * symbol_list = identifier symbol_list |
	 *               literal symbol_list |
	 *               ;
	 */
	
	private void content() throws IOException {
		while (lex.peek() != null) {
			if (!expression()) {
				StringBuilder err = new StringBuilder();
				err.append("Syntax error: expected ");
				err.append(getTokenName(Token.PRIMARY_NT_OP));
				err.append(", ");
				err.append(getTokenName(Token.NT_OP));
				err.append(", or ");
				err.append(getTokenName(Token.COMMAND_OP));
				err.append("; found ");
				err.append(getTokenName(lex.peek()));
				error(err.toString());
			}
		}
	}
	
	private boolean expression() throws IOException {
		return command() || nonterminal();
	}
	
	private boolean command() throws IOException {
		if (accept(Token.COMMAND_OP)) {
			Token cmdToken = lex.peek();
			String cmd = requireIdentifier();
			
			if (cmd.equalsIgnoreCase("import") || cmd.equalsIgnoreCase("require") || cmd.equalsIgnoreCase("include")) {
				String resource = requireLiteral();
				if (resource == null)
					return false;
				
				importResource(resource);
				return true;
			} else if (cmd.equalsIgnoreCase("optional")) {
				String resource = requireLiteral();
				if (resource == null)
					return false;
				
				optionalImportResource(resource);
				return true;
			} else {
				StringBuilder err = new StringBuilder();
				err.append("Unrecognized command: ");
				err.append(getTokenName(Token.COMMAND_OP));
				err.append(getTokenName(cmdToken));
				error(err.toString());
			}
		}
		
		return false;
	}
	
	private void optionalImportResource(String resourceName) throws IOException {
		if (imports.contains(resourceName) || lexerFactory == null) {
			return;
		}
		
		Lexer oldLexer = lex;
		try {
			Lexer lexer = lexerFactory.getLexer(resourceName);
			parse(lexer, lexerFactory);
		} catch (Exception e) { }
		setDefaultLexer(oldLexer);
		lex = oldLexer;
	}
	
	private void importResource(String resourceName) throws IOException {
		if (imports.contains(resourceName))
			return;
		
		if (lexerFactory == null) {
			error("Cannot import resource; no LexerFactory provided!");
			return;
		}
		
		Lexer oldLexer = lex;
		Lexer lexer = null;
		try {
			lexer = lexerFactory.getLexer(resourceName);
		} catch (FileNotFoundException e) {
			error("Resource not found: " + resourceName);
		}
		
		try {
			parse(lexer, lexerFactory);
		} catch (ParseException e) {
			error("Parse error in imported resource: " + resourceName, e);
		}
		setDefaultLexer(oldLexer);
		lex = oldLexer;
	}

	private boolean nonterminal() throws IOException {
		if (accept(Token.PRIMARY_NT_OP)) {
			String name = requireIdentifier();
			if (name == null)
				return false;
			
			Nonterminal nt = new Nonterminal(name);
			nonterminals.put(name, nt);
			primaryNonterminals.put(name, nt);
			productionList(nt);
			return true;
			
		} else if (accept(Token.NT_OP)) {
			String name = requireIdentifier();
			if (name == null)
				return false;
			
			Nonterminal nt = new Nonterminal(name);
			nonterminals.put(name, nt);
			productionList(nt);
			return true;
		}
		
		return false;
	}
	
	private void productionList(Nonterminal nt) throws IOException {
		while (production(nt));
	}
	
	private boolean production(Nonterminal nt) throws IOException {
		double weight = 1.f;
		
		if (peekType() == TokenType.NUMBER) {
			weight = requireNumber();
			if (require(Token.PROD_OP) == null) {
				return false;
			}
			Production prod = new Production(weight);
			nt.add(prod);
			symbolList(prod);
			return true;
			
		} else if (accept(Token.PROD_OP)) {
			Production prod = new Production(weight);
			nt.add(prod);
			symbolList(prod);
			return true;
		}
		
		return false;
	}
	
	private void symbolList(final Production p) throws IOException {
		boolean defer = false;
		for (;;) {
			TokenType nextType = peekType();
			if (nextType == TokenType.IDENTIFIER) {
				final String name = requireIdentifier();
				if (!defer) {
					Nonterminal nt = nonterminals.get(name);
					if (nt == null) {
						defer = true;
					} else {
						p.add(nt);
					}
				}
				
				if (defer) {
					tasks.add(new Task() {
						@Override
						public void run() throws ParseException {
							Nonterminal nt = nonterminals.get(name);
							if (nt != null) {
								p.add(nt);
							} else {
								StringBuilder err = new StringBuilder();
								err.append("Nonterminal not found: ");
								err.append(getTokenName(new Token(TokenType.IDENTIFIER, name)));
								error(err.toString());
							}
						}
					});
				}
				
			} else if (nextType == TokenType.LITERAL) {
				final String name = requireLiteral();
				if (defer) {
					tasks.add(new Task() {
						@Override
						public void run() {
							p.add(new Terminal(name));
						}
					});
				} else {
					p.add(new Terminal(name));
				}
				
			} else {
				return;
			}
		}
	}
	
	private TokenType peekType() throws IOException {
		Token next = lex.peek();
		return next == null ? null : next.getType();
	}
	
	private String requireIdentifier() throws IOException {
		return require(Token.IDENTIFIER).getContent();
	}
	
	private String requireLiteral() throws IOException {
		return require(Token.LITERAL).getContent();
	}
	
	private double requireNumber() throws IOException {
		return require(Token.NUMBER).getValue();
	}
	
	private boolean accept(Token t) throws IOException {
		if (peekType() == t.getType()) {
			lex.next();
			return true;
		}
		return false;
	}
	
	private Token require(Token t) throws IOException {
		if (peekType() == t.getType()) {
			return lex.next();
		}
		StringBuilder err = new StringBuilder();
		err.append("Syntax error: expected ");
		err.append(getTokenName(t));
		err.append("; found ");
		err.append(getTokenName(lex.peek()));
		
		error(err.toString());
		return Token.NULL;
	}
	
	private void error(String what) throws ParseException {
		if (lex != null) {
			lex.onParseError(what, null);
		} else {
			throw new ParseException(what);
		}
	}
	
	private void error(String what, Throwable cause) throws ParseException {
		if (lex != null) {
			lex.onParseError(what, cause);
		} else {
			throw new ParseException(what, cause);
		}
	}
	
	private interface Task {
		void run() throws ParseException;
	}
}
