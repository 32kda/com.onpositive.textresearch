package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.lexic.WordFormParser;
import com.onpositive.text.analysis.lexic.WordFormToken;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.syntax.SyntaxToken.GrammemSet;

public class Euristic {

	private static final int EURISTIC_WORD = 40001;
	private static final int EURISTIC_GRAMMEM = 40002;
	private static final int EURISTIC_CONCAT = 40003;
	private static final int EURISTIC_OR = 40004;
	private static final int EURISTIC_ALL = 40005;
	private static final int EURISTIC_CONFLICTING = 40006;
	private static final int EURISTIC_AND = 40007;
	private static final int EURISTIC_NOT = 40008;
	
	private static HashMap<Class<? extends BasicParser>, List<Euristic>> registered = new HashMap<>();
	private String word = null;
	private Grammem[] grammems = null;
	private int type;
	private Euristic[] euristics = null;
	private String id;
	
	public static void register(Class<? extends BasicParser> clazz, Euristic ... euristics)
	{
		Euristic eur = concat(euristics);
		
		if (!registered.containsKey(clazz)) registered.put(clazz, new ArrayList<Euristic>());
		
		List<Euristic> list = registered.get(clazz);
		list.add(eur);
	}

	public static Euristic concat(Euristic... euristics) {
		Euristic eur = new Euristic(euristics);
		eur.type = EURISTIC_CONCAT;
		return eur;
	}
	
	public static List<Euristic> match(Class<? extends BasicParser> clazz) {  
		if (!registered.containsKey(clazz)) 
			registered.put(clazz, new ArrayList<Euristic>());
		
		return registered.get(clazz);
	}
	
	private boolean matchWord(IToken token) {
		if (!(token instanceof WordFormToken)) 
			return false;
		SyntaxToken wft = (SyntaxToken) token;
		if (!wft.getBasicForm().equals(this.word)) 
			return false;
		
		return matchAny(token);
	}
	
	private boolean matchAny(IToken token) {
		if (!(token instanceof WordFormToken)) return false;
		WordFormToken wft = (WordFormToken) token;
		
		if (this.grammems.length == 0) return true;
		if (wft.getMeaningElements().length == 0) return false;
		List<GrammemSet> grammemSets = wft.getGrammemSets();
		for (GrammemSet grammemSet : grammemSets) {
			for (Grammem gr : this.grammems) {
				if (grammemSet.hasGrammem(gr)) 
					return true;
			}
		}
		return false;
	}
	
	private boolean matchAll(IToken token) {
		if (!(token instanceof WordFormToken)) return false;
		WordFormToken wft = (WordFormToken) token;
		
		if (this.grammems.length == 0) return true;
		if (wft.getMeaningElements().length == 0) return false;
		List<GrammemSet> grammemSets = wft.getGrammemSets();
		for (GrammemSet grammemSet : grammemSets) {
			boolean sucess = true;
			for (Grammem gr : this.grammems) {
				if (!grammemSet.hasGrammem(gr)) {
					sucess = false;
					break;
				}
			}
			if (sucess) {
				return true;
			}
		}
		return false;
	}
	
	private boolean matchOr(IToken token) {
		if (euristics == null) return true;
		for (Euristic eur : euristics)
			if (eur.match(token)) return true;
		return false;
	}
	
	private boolean matchAnd(IToken token) {
		if (euristics == null) return false;
		for (Euristic eur : euristics)
			if (!eur.match(token)) return false;
		return true;
	}
	
	private boolean matchConcat(IToken[] tokens) {
		if (euristics == null) return true;
		if (tokens == null) return false;
		if (euristics.length > tokens.length) return false;
		
		for (int i = 0; i < euristics.length; i++) {
			if (euristics[i].match(tokens[i]) == false) return false;
		}
		
		return true;
	}
	
	private boolean matchConflicting(IToken token) {
		if (!(token instanceof WordFormToken)) return false;
		SyntaxToken wft = (SyntaxToken) token;
		List<IToken> conflicts = wft.getConflicts();
		if (conflicts == null || conflicts.isEmpty()) {
			return false;
		}
		List<IToken> matchList = new ArrayList<IToken>(conflicts);
		Set<Grammem> matchedGrammems = new HashSet<Grammem>();
		matchList.add(0, token);
		int matchedCount = 0;
		for (IToken curToken : matchList) {
			Set<Grammem> gs = ((WordFormToken) curToken).getMeaningElements()[0].getGrammems();
			for (Grammem gr : this.grammems) {
				if (gs.contains(gr) && !matchedGrammems.contains(gr)) {
					matchedCount++;
					matchedGrammems.add(gr);
					if (matchedCount == 2) {
						return true;
					}
					break;
				}
			}
		}
		return false;
	}

	public boolean match(IToken ... tokens) {
		switch (type) {
			case EURISTIC_WORD:
				if (tokens.length != 1) return false;
				else return matchWord(tokens[0]);
			case EURISTIC_GRAMMEM:
				if (tokens.length != 1) return false;
				else return matchAny(tokens[0]);
			case EURISTIC_ALL:
				if (tokens.length != 1) return false;
				else return matchAll(tokens[0]);
			case EURISTIC_OR:
				if (tokens.length != 1) return false;
				return matchOr(tokens[0]);
			case EURISTIC_AND:
				if (tokens.length != 1) return false;
				return matchAnd(tokens[0]);
			case EURISTIC_CONCAT:
				return matchConcat(tokens);
			case EURISTIC_CONFLICTING:
				if (tokens.length != 1) return false;
				else return matchConflicting(tokens[0]);
			case EURISTIC_NOT:
				return !euristics[0].match(tokens);
			default:
				return false; // TODO: add other classes
		}
	}
	
	public static Euristic word(String word, Grammem ... gr) {
		return new Euristic(word, gr);
	}
	
	public static Euristic any(Grammem ... gr) {
		return new Euristic(gr);
	}
	
	public static Euristic all(Grammem ... gr) {
		Euristic euristic = new Euristic(gr);
		euristic.type = EURISTIC_ALL;
		return euristic;
	}
	
	public static Euristic conflicting(Grammem... grammems) {
		Euristic euristic = new Euristic(grammems);
		euristic.type = EURISTIC_CONFLICTING;
		return euristic;
	}
	
	
	public Euristic then(Euristic a) {
		Euristic eur = new Euristic(Arrays.asList(this, a).toArray(new Euristic[0]));
		eur.type = EURISTIC_CONCAT;
		return eur;
	}
	
	public static Euristic or (Euristic ... euristics) { 
		Euristic eur = new Euristic(euristics);
		eur.type = EURISTIC_OR;
		return eur; 
	}
	
	public static Euristic and (Euristic ... euristics) { 
		Euristic eur = new Euristic(euristics);
		eur.type = EURISTIC_AND;
		return eur; 
	}
	
	public static Euristic not (Euristic euristic) {
		Euristic eur = new Euristic(new Euristic[]{euristic});
		eur.type = EURISTIC_NOT;
		return eur;
	}
	
	public static Euristic createConflictChecker(PartOfSpeech right, PartOfSpeech wrong) {
		return Euristic.and(Euristic.conflicting(right, wrong), Euristic.any(right));
	}
		
	private Euristic(String word, Grammem[] grammems) {
		this.type = EURISTIC_WORD;
		this.word = word;
		this.grammems = grammems;
	}
	
	private Euristic(Grammem[] grammems) {
		this.type = EURISTIC_GRAMMEM;
		this.grammems = grammems;
	}
	
	private Euristic(Euristic[] euristics) {
		this.euristics = euristics; 
	}
	
	public static void Example() {

		
		Euristic.register(
				WordFormParser.class,
				
				word("части", PartOfSpeech.NOUN),
				any(PartOfSpeech.ADJF),
				or(any(PartOfSpeech.ADJF), any(PartOfSpeech.ADJS))
		);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		if (id != null) {
			return "Euristic " + id; 
		}
		return super.toString();
	}
}




