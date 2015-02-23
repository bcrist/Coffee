package com.magicmoremagic.coffee;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.magicmoremagic.coffee.parser.DefaultLexer;
import com.magicmoremagic.coffee.parser.Lexer;
import com.magicmoremagic.coffee.parser.StringUtil;
import com.magicmoremagic.coffee.parser.Token;

public class Nonterminal extends AbstractSet<Production> implements SerializableSymbol {

	private static final AtomicLong nextID = new AtomicLong(0);
	
	private String name;
	private List<Production> productions;
	private int cachedHashCode = -1;
	
	public Nonterminal() {
		name = "$" + Long.toString(nextID.getAndIncrement());
		productions = new ArrayList<Production>();
	}
	
	public Nonterminal(String name) {
		this.name = name;
		productions = new ArrayList<Production>();
	}
	
	public Nonterminal(Nonterminal other) {
		name = "$" + Long.toString(nextID.getAndIncrement());
		productions = new ArrayList<Production>();
		addAll(other.productions);
	}
	
	@Override
	public Nonterminal clone() {
		return new Nonterminal(this);
	}	
	
	public void setName(String name) {
		if (name == null) {
			this.name = "$" + Long.toString(nextID.getAndIncrement());
		} else {
			this.name = name;
		}
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int size() {
		return productions.size();
	}
	
	@Override
	public void clear() {
		modified();
		productions.clear();
	}
	
	@Override
	public boolean add(Production production) {
		if (production.hasOwner()) {
			production = new Production(production);
		}
		
		modified();
		production.claim(this);
		if (!productions.isEmpty()) {
			int i = productions.size() - 1;
			productions.get(i).setTail(production);
		}
		return productions.add(production);
	}
	
	public boolean addAll(Production... productions) {
		boolean mod = false;
		for (Production p : productions) {
			if (add(p))
				mod = true;
		}
		return mod;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Production) {
			Production production = (Production)o;
			
			int index = findProductionIndex(production.getLowerBound());
			if (index < productions.size()) {
				productions.remove(index);
				doRemoveCleanup(index);
				return true;
			}
		}
		return false;
	}
	
    /**
     * {@inheritDoc}
     */
    public boolean contains(Object o) {
    	if (o instanceof Production) {
    		Production prod = (Production)o;
    		if (prod.getOwner() == this) {
    			return findProduction(prod.getLowerBound()) == prod;
    		}
    	}
    	return false;
    }
	
    @Override
	public Iterator<Production> iterator() {
		return new NTIterator(productions.listIterator());
	}

	/**
	 * Returns a collection containing all the nonterminals that this symbol depends on (including itself).
	 * Serializing all returned Nonterminals using getSchema() will create a file that can be parsed back in
	 * using Parser.parse().
	 */
	public Set<Nonterminal> getDependencies() {
		Set<Nonterminal> set = new HashSet<>();
		addDependencies(set);
		return set;
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
	public void brew(Random rnd, StringBuilder dest) {
		double roll = rnd.nextDouble() * getTotalWeight();
		Production production = findProduction(roll);
		if (production != null) {
			production.brew(rnd, dest);
		}
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
	
	@Override
	public void serialize(PrintWriter pw, Lexer lexer) {
		serialize(pw, lexer, true);
	}

	public String serializeAll() {
		return serializeAll(getDefaultLexer());
	}

	public String serializeAll(Lexer lexer) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, false);
		serializeAll(pw, lexer);
		return sw.toString();
	}

	public void serializeAll(OutputStream os) {
		try {
			serializeAll(new PrintWriter(new OutputStreamWriter(os, "UTF-8"), false), getDefaultLexer());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public void serializeAll(OutputStream os, Lexer lexer) {
		try {
			serializeAll(new PrintWriter(new OutputStreamWriter(os, "UTF-8"), false), lexer);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public void serializeAll(Writer w) {
		serializeAll(new PrintWriter(w, false), getDefaultLexer());
	}

	public void serializeAll(Writer w, Lexer lexer) {
		serializeAll(new PrintWriter(w, false), lexer);
	}
	
	public void serializeAll(PrintWriter pw) {
		serializeAll(pw, getDefaultLexer());
	}
	
	public void serializeAll(PrintWriter pw, Lexer lexer) {
		serialize(pw, lexer, true);
		
		for (Nonterminal nt : getDependencies()) {
			if (nt == this)
				continue;
			
			pw.println();
			nt.serialize(pw, lexer, false);
		}
	}
	
	@Override
	public String toString() {
		return StringUtil.toIdentifier(name);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Nonterminal) {
			Nonterminal ntOther = (Nonterminal)other;
			return name.equals(ntOther.name) && productions.equals(ntOther.productions);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (cachedHashCode == -1) {
			cachedHashCode = 47 * name.hashCode() + productions.hashCode();
		}
		return cachedHashCode;
	}
	
	protected Lexer getDefaultLexer() {
		return DefaultLexer.DEFAULT;
	}
	
	void serialize(PrintWriter pw, Lexer lexer, boolean primary) {
		if (lexer == null) {
			lexer = getDefaultLexer();
		}
		
		lexer.serializeToken(pw, primary ? Token.PRIMARY_NT_OP : Token.NT_OP);
		pw.print(' ');
		pw.println(StringUtil.toIdentifier(name));
		
		for (Production p : productions) {
			pw.print("  ");
			p.serialize(pw, lexer);
			pw.println();
		}
		
		pw.flush();
	}
	
	void addDependencies(Set<Nonterminal> set) {
		set.add(this);
		for (Production p : productions) {
			p.addDependencies(set);
		}
	}
	
	private Production findProduction(double weight) {
		int begin = 0;
		int end = productions.size();
		while (begin < end) {
			int pivot = begin + (end - begin) / 2;
			
			Production production = productions.get(pivot);
			int compareResult = production.compareWeight(weight);
			if (compareResult > 0) {
				begin = pivot + 1;
			} else if (compareResult < 0) {
				end = pivot;
			} else {
				return production;
			}
		}
		
		return null;
	}
	
	private int findProductionIndex(double weight) {
		int begin = 0;
		int end = productions.size();
		while (begin < end) {
			int pivot = begin + (end - begin) / 2;
			
			Production production = productions.get(pivot);
			int compareResult = production.compareWeight(weight);
			if (compareResult > 0) {
				begin = pivot + 1;
			} else if (compareResult < 0) {
				end = pivot;
			} else {
				return pivot;
			}
		}
		
		return productions.size();
	}

	private double getTotalWeight() {
		if (productions.isEmpty())
			return 0.0;
		
		return productions.get(productions.size() - 1).getUpperBound();
	}
	
	private void doRemoveCleanup(int removedIndex) {
		modified();
		if (removedIndex > 0) {
			Production tail = removedIndex < productions.size() ? productions.get(removedIndex) : null;
			productions.get(removedIndex - 1).setTail(tail);
		} else if (!productions.isEmpty()) {
			productions.get(0).updateLowerBound(0.0);
		}
	}
	
	private void modified() {
		cachedHashCode = -1;
	}
	
	private class NTIterator implements Iterator<Production> {
		
		private ListIterator<Production> inner;

		public NTIterator(ListIterator<Production> inner) {
			this.inner = inner;
		}
		
		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public Production next() {
			return inner.next();
		}

		@Override
		public void remove() {
			int index = inner.previousIndex();
			inner.remove();
			doRemoveCleanup(index);
		}
	}
}
