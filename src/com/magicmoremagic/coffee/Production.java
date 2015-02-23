package com.magicmoremagic.coffee;

import java.io.*;
import java.util.*;

import com.magicmoremagic.coffee.parser.DefaultLexer;
import com.magicmoremagic.coffee.parser.Lexer;
import com.magicmoremagic.coffee.parser.Token;
import com.magicmoremagic.coffee.parser.TokenType;

public class Production extends AbstractList<Symbol> implements SerializableSymbol {

	private static final double EPSILON = 0.0001;
	
	private Nonterminal owner;
	private Production tail;
	private double lowerBound;
	private double upperBound;
	private List<Symbol> symbols;
	private int cachedHashCode = -1;
	
	public Production() {
		symbols = new ArrayList<>();
	}
	
	public Production(double weight) {
		checkWeight(weight);
		symbols = new ArrayList<>();
		upperBound = weight;
	}
	
	public Production(Iterable<Symbol> symbols) {
		this.symbols = new ArrayList<>();
		for (Symbol s : symbols) {
			if (s == null) {
				throw new NullPointerException();
			}
			this.symbols.add(s);
		}
	}
	
	public Production(Collection<Symbol> symbols) {
		this.symbols = new ArrayList<>();
		addAll(symbols);
	}
	
	public Production(Symbol... symbols) {
		this.symbols = new ArrayList<>();
		addAll(symbols);
	}
	
	public Production(double weight, Collection<Symbol> symbols) {
		checkWeight(weight);
		this.upperBound = weight;
		this.symbols = new ArrayList<>();
		addAll(symbols);
	}
	
	public Production(double weight, Symbol... symbols) {
		checkWeight(weight);
		upperBound = weight;
		this.symbols = new ArrayList<>();
		addAll(symbols);
	}
	
	public Production(Production other) {
		upperBound = other.getWeight();
		this.symbols = new ArrayList<>();
		for (Symbol s : other.symbols) {
			this.symbols.add(s.clone());
		}
	}
	
	@Override
	public Production clone() {
		return new Production(this);
	}
	
	public void setWeight(double weight) {
		checkWeight(weight);
		modified();
		upperBound = lowerBound + weight;
		if (tail != null) {
			tail.updateLowerBound(upperBound);
		}
	}
	
	public double getWeight() {
		return upperBound - lowerBound;
	}
	
	/**
     * Appends the specified symbol to the end of this production.
     *
     * @param symbol The symbol to add to the production.
     * @return {@code true} (as specified by {@link Collection#add})
     */
	@Override
	public boolean add(Symbol symbol) {
		if (symbol == null)
    		throw new NullPointerException();
		
		modified();
		return symbols.add(symbol);
	}
	
	/**
     * Inserts the specified symbol at the specified index in this production.
     *
     * @param symbol The symbol to add to the production.
     * @return {@code true} (as specified by {@link Collection#add})
     * 
     * @throws IndexOutOfBoundsException if {@code index > size()} or {@code index < 0}. 
     */
	@Override
	public void add(int index, Symbol symbol) {
		if (symbol == null)
    		throw new NullPointerException();
		
		modified();
		symbols.add(index, symbol);
	}
	
	public boolean addAll(Symbol... symbols) {
		for (Symbol s : symbols) {
			if (s == null)
				throw new NullPointerException();
		}
		boolean mod = this.symbols.addAll(Arrays.asList(symbols));
		if (mod) {
			modified();
		}
		return mod;
	}
	
	public boolean addAll(int index, Symbol... symbols) {
		for (Symbol s : symbols) {
			if (s == null)
				throw new NullPointerException();			
		}
		boolean mod = this.symbols.addAll(index, Arrays.asList(symbols));
		if (mod) {
			modified();
		}
		return mod;
	}

    /**
     * Appends all of the Symbols in the specified collection to the end of
     * this production, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (Note that this will occur if the specified collection is this list,
     * and it's nonempty.)
     *
     * @param collection collection containing Symbols to be added to this Production
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #add(Object)
     */
	@Override
	public boolean addAll(Collection<? extends Symbol> collection) {
		for (Symbol s : collection) {
			if (s == null)
				throw new NullPointerException();
		}
		modified();
		return symbols.addAll(collection);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends Symbol> collection) {
		for (Symbol s : collection) {
			if (s == null)
				throw new NullPointerException();
		}
		modified();
		return symbols.addAll(index, collection);
	}
	
    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
	@Override
	public Symbol get(int index) {
		return symbols.get(index);
	}
	
    /**
     * {@inheritDoc}
     *  
     * @throws NullPointerException          {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
    public Symbol set(int index, Symbol symbol) {
    	if (symbol == null)
    		throw new NullPointerException();
    	
        return symbols.set(index, symbol);
    }
	    
    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */
	@Override
	public Symbol remove(int index) {
		modified();
		return symbols.remove(index);
	}
	
    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
	@Override
	public void clear() {
		modified();
		symbols.clear();
	}

    /**
     * {@inheritDoc}
     */
    public int indexOf(Object o) {
        return o == null ? -1 : symbols.indexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    public int lastIndexOf(Object o) {
    	return o == null ? -1 : symbols.lastIndexOf(o);
    }
	
	@Override
	public int size() {
		return symbols.size();
	}
		
    /**
     * Returns an iterator over the symbols in this production in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<Symbol> iterator() {
        return new ProdIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @see #listIterator(int)
     */
    public ListIterator<Symbol> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<Symbol> listIterator(final int index) {
        return new ProdListIterator(index);
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
		for (Symbol s : symbols) {
			s.brew(rnd, dest);
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
		if (lexer == null) {
			lexer = getDefaultLexer();
		}

		double weight = getWeight();
		int intWeight = (int)weight;
		if (Math.abs(weight - (double)intWeight) < EPSILON) {
			if (intWeight == 1) {
				pw.print(' ');
			} else {
				lexer.serializeToken(pw, new Token(intWeight));
			}
		} else {
			lexer.serializeToken(pw, new Token(getWeight()));
		}
		
		lexer.serializeToken(pw, Token.PROD_OP);
		serializeSymbols(pw, lexer);
		pw.flush();
	}
	
	@Override
	public String toString() {
		return serialize();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Production) {
			Production prodOther = (Production)other;
			return Math.abs(getWeight() - prodOther.getWeight()) < EPSILON && symbols.equals(prodOther.symbols);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (cachedHashCode == -1) {
			cachedHashCode = 47 * (int)getWeight() + symbols.hashCode();
		}
		return cachedHashCode;
	}
	
	protected Lexer getDefaultLexer() {
		return DefaultLexer.DEFAULT;
	}
	
	boolean hasOwner() {
		return owner != null;
	}
	
	Nonterminal getOwner() {
		return owner;
	}
	
	void claim(Nonterminal owner) {
		if (this.owner != null) {
			throw new IllegalStateException();
		}
		
		this.owner = owner;
	}
	
	void setTail(Production tail) {
		this.tail = tail;
		if (tail != null) {
			tail.updateLowerBound(upperBound);
		}
	}
	
	void updateLowerBound(double lowerBound) {
		upperBound -= this.lowerBound;
		this.lowerBound = lowerBound;
		upperBound += lowerBound;
		
		if (tail != null) {
			tail.updateLowerBound(upperBound);
		}
	}
	
	double getLowerBound() {
		return upperBound;
	}
	
	double getUpperBound() {
		return upperBound;
	}
	
	int compareWeight(double weight) {
		if (weight >= lowerBound) {
			if (weight < upperBound) {
				return 0;
			}
			
			return 1;
		}
		
		return -1;
	}

	void addDependencies(Set<Nonterminal> set) {
		for (Symbol s : symbols) {
			if (s instanceof Nonterminal) {
				((Nonterminal)s).addDependencies(set);
			} else if (s instanceof Production) {
				((Production)s).addDependencies(set);
			}
		}
	}
	
	private void serializeSymbols(PrintWriter pw, Lexer lexer) {
		for (Symbol s : symbols) {
			if (s instanceof Production) {
				((Production)s).serializeSymbols(pw, lexer);
			} else if (s instanceof Nonterminal) {
				pw.print(' ');
				lexer.serializeToken(pw, new Token(TokenType.IDENTIFIER, ((Nonterminal)s).getName()));
			} else if (s instanceof AbstractSymbol) {
				pw.print(' ');
				((AbstractSymbol)s).serialize(pw, lexer);
			} else {
				pw.print(' ');
				pw.print(s);
			}
		}
	}
	
	private void checkWeight(double weight) {
		if (weight < 0 || Double.isNaN(weight) || Double.isInfinite(weight))
			throw new IllegalArgumentException("Weight must be a finite non-negative number.");
	}
	
	private void modified() {
		cachedHashCode = -1;
	}

	private class ProdIterator implements Iterator<Symbol> {

    	private Iterator<Symbol> inner;
    	
    	public ProdIterator() {
    		inner = symbols.iterator();
    	}
    	
		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public Symbol next() {
			return inner.next();
		}

		@Override
		public void remove() {
			modified();
			inner.remove();
		}
    }
    
    private class ProdListIterator implements ListIterator<Symbol> {

    	private ListIterator<Symbol> inner;
    	
    	public ProdListIterator(int index) {
    		inner = symbols.listIterator(index);
    	}
    	
		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public Symbol next() {
			return inner.next();
		}

		@Override
		public boolean hasPrevious() {
			return inner.hasPrevious();
		}

		@Override
		public Symbol previous() {
			return inner.previous();
		}

		@Override
		public int nextIndex() {
			return inner.nextIndex();
		}

		@Override
		public int previousIndex() {
			return inner.previousIndex();
		}

		@Override
		public void remove() {
			modified();
			inner.remove();
		}

		@Override
		public void set(Symbol e) {
			modified();
			inner.set(e);
		}

		@Override
		public void add(Symbol e) {
			modified();
			inner.add(e);
		}   	
    }
	
}
