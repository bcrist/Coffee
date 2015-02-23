package com.magicmoremagic.coffee;

import java.io.*;
import java.util.*;

import com.magicmoremagic.coffee.parser.DefaultLexer;
import com.magicmoremagic.coffee.parser.Lexer;

public class NonterminalSet extends AbstractSet<Nonterminal> implements SerializableSymbol {

	private Lexer defaultLexer;
	protected Map<String, Nonterminal> nonterminals;
	protected Map<String, Nonterminal> primaryNonterminals;
	
	public NonterminalSet() {
		nonterminals = new HashMap<>();
		primaryNonterminals = new HashMap<>();
	}
	
	public NonterminalSet(Nonterminal nt) {
		nonterminals = new HashMap<>();
		primaryNonterminals = new HashMap<>();
		add(nt);
	}
	
	public NonterminalSet(Nonterminal... nt) {
		nonterminals = new HashMap<>();
		primaryNonterminals = new HashMap<>();
		addAll(nt);
	}
	
	public NonterminalSet(Collection<Nonterminal> collection) {
		nonterminals = new HashMap<>();
		primaryNonterminals = new HashMap<>();
		addAll(collection);
	}
	
	public NonterminalSet(NonterminalSet other) {
		rebuildNames(other);
	}
	
	public NonterminalSet clone() {
		return new NonterminalSet(this);
	}

	/**
	 * This should be called if any of the Nonterminals have their names changed
	 * after being parsed or added to ensure that get(), contains(), etc. return correct results. 
	 */
	public void rebuildNames() {
		rebuildNames(this);
	}
	
	public Nonterminal get(String name) {
		return primaryNonterminals.get(name);
	}
	
	public Collection<Nonterminal> getAll() {
		return nonterminals.values();
	}
	
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     */
	@Override
	public boolean add(Nonterminal nt) {
		String name = nt.getName();
		if (!primaryNonterminals.containsKey(name)) {
			primaryNonterminals.put(name, nt);
			for (Nonterminal dnt : nt.getDependencies()) {
				String dntName = dnt.getName();
				if (!nonterminals.containsKey(dntName)) {
					nonterminals.put(dntName, dnt);
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean addAll(Nonterminal... nts) {
		return addAll(Arrays.asList(nts));
	}
	
    /**
     * {@inheritDoc}
     */
    public boolean remove(Object o) {
    	if (o instanceof Nonterminal) {
    		Nonterminal nt = (Nonterminal)o;
    		Nonterminal removed = primaryNonterminals.remove(nt.getName());
    		
    		if (removed != null) {
    			rebuildDependencies();
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public void clear() {
		nonterminals.clear();
		primaryNonterminals.clear();
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean contains(Object o) {
		if (o instanceof Nonterminal) {
			Nonterminal nt = (Nonterminal)o;
			return primaryNonterminals.containsKey(nt.getName());
		}
		return false;
	}
	
	@Override
	public Iterator<Nonterminal> iterator() {
		return new NTSIterator();
	}

	@Override
	public int size() {
		return nonterminals.size();
	}
	
    /**
     * {@inheritDoc}
     */
    public Object[] toArray() {
    	return primaryNonterminals.values().toArray();
    }

    /**
     * {@inheritDoc}
     *
     * @throws ArrayStoreException  {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public <T> T[] toArray(T[] a) {
        return primaryNonterminals.values().toArray(a);
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
		if (primaryNonterminals.isEmpty())
			return;
		
		int index = rnd.nextInt(primaryNonterminals.size());
		
		Iterator<Nonterminal> iter = primaryNonterminals.values().iterator();
		while (index > 0) {
			iter.next();
			--index;
		}
		
		Nonterminal nt = iter.next();
		nt.brew(rnd, dest);
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
			lexer = DefaultLexer.DEFAULT;
		}
		
		boolean first = true;
		for (Nonterminal nt : primaryNonterminals.values()) {
			if (first) {
				first = false;
			} else {
				pw.println();
			}
			nt.serialize(pw, lexer, true);
		}
		
		for (Nonterminal nt : nonterminals.values()) {
			if (primaryNonterminals.containsKey(nt.getName())) {
				continue;
			}
			
			if (first) {
				first = false;
			} else {
				pw.println();
			}
			
			nt.serialize(pw, lexer, false);
		}
		pw.flush();
	}
	
    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
	@Override
	public String toString() {
        Iterator<Nonterminal> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            Nonterminal nt = it.next();
            sb.append(nt);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof NonterminalSet) {
			NonterminalSet ntsOther = (NonterminalSet)other;
			return primaryNonterminals.equals(ntsOther.primaryNonterminals) &&
					nonterminals.equals(ntsOther.nonterminals);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 47 * primaryNonterminals.hashCode() + nonterminals.hashCode();
	}
	
	protected void setDefaultLexer(Lexer lexer) {
		defaultLexer = lexer;
	}
	
	protected Lexer getDefaultLexer() {
		return defaultLexer;
	}
	
	private void rebuildNames(NonterminalSet ntc) {
		Map<String, Nonterminal> newNTs = new HashMap<>();
		Map<String, Nonterminal> newPriNTs = new HashMap<>();
		
		for (Nonterminal nt : ntc.nonterminals.values()) {
			for (Nonterminal dnt : nt.getDependencies()) {
				newNTs.put(dnt.getName(), dnt);
			}
		}
		
		for (Nonterminal nt : ntc.primaryNonterminals.values()) {
			newPriNTs.put(nt.getName(), nt);
			for (Nonterminal dnt : nt.getDependencies()) {
				newNTs.put(dnt.getName(), dnt);
			}
		}
		
		nonterminals = newNTs;
		primaryNonterminals = newPriNTs;
	}
	
	private void rebuildDependencies() {
		nonterminals.clear();
		for (Nonterminal nt : primaryNonterminals.values()) {
			for (Nonterminal dnt : nt.getDependencies()) {
				nonterminals.put(dnt.getName(), dnt);
			}
		}
	}
	
	private class NTSIterator implements Iterator<Nonterminal> {
		
		private Iterator<Nonterminal> inner;
		
		public NTSIterator() {
			inner = primaryNonterminals.values().iterator();
		}

		@Override
		public boolean hasNext() {
			return inner.hasNext();
		}

		@Override
		public Nonterminal next() {
			return inner.next();
		}

		@Override
		public void remove() {
			inner.remove();
			rebuildDependencies();
		}
	}
}
