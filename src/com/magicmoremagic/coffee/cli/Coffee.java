package com.magicmoremagic.coffee.cli;

import java.io.IOException;
import java.util.Random;

import com.magicmoremagic.coffee.Nonterminal;
import com.magicmoremagic.coffee.parser.Parser;

public final class Coffee {
	
	private enum Mode {
		GENERATE,
		LIST,
		DUMP,
		;
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			Parser parser = new Parser();
			try {
				parser.parseResource(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
				usage();
				System.exit(0);
			}
			
			String generator = null;
			Mode mode = Mode.GENERATE;
			if (args.length > 1) {
				if (args[1].equals("--list")) {
					mode = Mode.LIST;
				} else if (args[1].equals("--dump")) {
					mode = Mode.DUMP;
				} else if (!args[1].equals("?") && !args[1].equals("--random")) {
					generator = args[1];
				}
			}
			
			int n = 1;
			if (args.length > 2) {
				if (args[2].equals("--dump")) {
					mode = Mode.DUMP;
				} else {
					n = Integer.parseInt(args[2]);
				}
			}
			
			Random rnd;
			if (args.length > 3) {
				rnd = new Random(Long.parseLong(args[3]));
			} else {
				rnd = new Random();
			}
			
			switch (mode) {
			case GENERATE:
				if (generator == null) {
					for (int i = 0; i < n; ++i) {
						System.out.println(parser.brew(rnd));
					}
				} else {
					Nonterminal nt = parser.get(generator);
					if (nt == null) {
						System.err.println("Generator not found: " + generator);
						System.exit(1);
					}
					
					for (int i = 0; i < n; ++i) {
						System.out.println(nt.brew(rnd));
					}
				}
				break;
				
			case LIST:
				for (Nonterminal nt : parser) {
					System.out.println(nt);
				}
				break;
				
			case DUMP:
				if (generator == null) {
					parser.serialize(System.out);
					
				} else {
					Nonterminal nt = parser.get(generator);
					if (nt == null) {
						System.err.println("Generator not found: " + generator);
						System.exit(1);
					}
			
					nt.serializeAll(System.out);					
				}
				break;
			}
			
			System.exit(0);
		}
		usage();
	}
	
	private static void usage() {
		System.out.println();
		System.out.println("COFFEE: Random string generator utilizing weighted context-free grammar");
		System.out.println("(c) 2007 - 2015 Benjamin Crist");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("   Coffee <filename> [generator] [count] [seed]");
		System.out.println("   Coffee <filename> [generator] --dump");
		System.out.println("   Coffee <filename> --random [count] [seed]");
		System.out.println("   Coffee <filename> --list");
		System.out.println();
		System.out.println("If `generator` is not provided or is '--random' or '?', a primary nonterminal");
		System.out.println("generator will be selected randomly for each result from the file.  If `count`");
		System.out.println("is not provided, a single result will be generated.  If it is greater than 1,");
		System.out.println("each result will be output on a new line.  A 64-bit integer may be provided to");
		System.out.println("seed the PRNG before generating the first result");
		System.out.println();
		System.out.println("Grammar & Theory:");
		System.out.println("   - ':::' begins a new primary nonterminal generator.  It must be followed by");
		System.out.println("     an identifier naming it, and an optional list of productions.  When the");
		System.out.println("     nonterminal is referenced, it is replaced by the content of one its");
		System.out.println("     productions, chosen randomly.");
		System.out.println("   - ']' is the \"new production\" operator.");
		System.out.println("   - On the left side of the ']' operator is an optional number specifying the");
		System.out.println("     production's weight.  If not provided, a weight of 1.0 is assumed.  The");
		System.out.println("     probability of a production being selected when a nonterminal is resolved");
		System.out.println("     is equal to its weight divided by the total weight of all productions on");
		System.out.println("     the nonterminal.");
		System.out.println("   - On the right-hand side of the ']' operator is a list of zero or more");
		System.out.println("     symbols.  When the production is used, each symbol in the list is");
		System.out.println("     evaluated in turn, and the results are concatenated together.");
		System.out.println("   - A symbol can be either an identifier naming a nonterminal, or a literal");
		System.out.println("     string.");
		System.out.println("   - '::' works exactly like ':::', but defines a secondary nonterminal, which");
		System.out.println("     will not appear in the results of a '--list' command.");
		System.out.println("   - The ordering of definitions is not important.  A nonterminal may be");
		System.out.println("     referenced before it is defined, and may recursively reference itself.");
		System.out.println("     Parsing is slightly more efficient when nonterminals are defined before");
		System.out.println("     they are used.");
		System.out.println("   - '!' indicates a meta-command.  It must be followed by an identifier naming");
		System.out.println("     the command.  Recognized commands are:");
		System.out.println("        !import   - Must be followed by a literal containing the name of a file");
		System.out.println("                    to load and parse.  May also be invoked using the aliases");
		System.out.println("                    !require or !include.");
		System.out.println("        !optional - Works like !import but if the file can't be loaded or if a");
		System.out.println("                    parsing error occurs, the active file will continue parsing");
		System.out.println("                    the rest of its contents.");
		System.out.println("   - Identifiers must match the regular expression:");
		System.out.println("        [A-Za-z_$\\\\][A-Za-z0-9_$\\\\]*");
		System.out.println("   - Literals are enclosed in double quotes. All unicode characters are");
		System.out.println("     accepted inside literals.");
		System.out.println("   - A backslash ('\\') in an identifier or literal denotes an escape sequence.");
		System.out.println("     Double quotes inside an identifier or literal must be escaped, or they");
		System.out.println("     will be treated as the beginning of a literal.  Recognized escapes are:");
		System.out.println("        \\\\ => '\\'");
		System.out.println("        \\\" => '\"'");
		System.out.println("        \\n => [LF]  (U+000D");
		System.out.println("        \\r => [CR]  (U+000A");
		System.out.println("        \\t => [TAB] (U+0009");
		System.out.println("        \\v => [VT]  (U+000B)");
		System.out.println("        \\b => [BS]  (U+0008)");
		System.out.println("        \\f => [FF]  (U+000C)");
		System.out.println("        \\xXX => U+00XX (ASCII/latin-1)");
		System.out.println("        \\uXXXX => U+XXXX (Unicode BMP)");
		System.out.println("        \\UXXXXXXXX => U+XXXXXXXX (converted to UTF-16 surrogate pair)");
		System.out.println("     If any other character appears directly following a backslash, the");
		System.out.println("     backslash and any special significance of that character will be ignored.");
		System.out.println("     This allows, for instance, identifiers to begin with a number, or contain");
		System.out.println("     non-alphanumeric characters without resorting to ASCII escapes (\\xXX).");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("     Use `Coffee examples --list` for a list of included example nonterminals.");
	}

	private Coffee() { }
	
}
