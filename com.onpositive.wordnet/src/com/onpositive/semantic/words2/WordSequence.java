package com.onpositive.semantic.words2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.onpositive.semantic.wordnet.RelationTarget;
import com.onpositive.semantic.wordnet.SemanticRelation;

public class WordSequence {

	RelationTarget[] targets;

	protected HashMap<RelationTarget, SemanticRelation[]> sequence = new HashMap<RelationTarget, SemanticRelation[]>();

	public WordSequence(ArrayList<RelationTarget> targets) {
		this.targets = targets.toArray(new RelationTarget[targets.size()]);
	}

	@Override
	public String toString() {
		return Arrays.toString(targets);
	}

	/*public RelationTarget getCore() {
		LinkedHashSet<RelationTarget> ns = new LinkedHashSet<RelationTarget>();
		for (RelationTarget t : targets) {
			if (t instanceof Word) {
				Word w = (Word) t;
				if (w.isAdjective()) {
					continue;
				}
				String basicForm = w.getBasicForm();
				if (basicForm.equals("классификация")) {
					continue;
				}
				if (basicForm.equals("по")) {
					continue;
				}
				if (basicForm.equals("и")) {
					continue;
				}
				if (basicForm.equals("вид")) {
					continue;
				}
				if (w.getBasicForm().equals("типы")) {
					continue;
				}
				if (w.isNoun()) {
					ns.add(w);
				}
			}
		}
		if (ns.size() == 1) {
			return ns.iterator().next();
		}
		if (ns.size() == 0) {
			for (RelationTarget q : targets) {
				if (q instanceof SimpleSequence) {
					Word[] words = ((SimpleSequence) q).getWords();
					for (Word qa : words) {
						if (qa.isNoun()) {
							ns.add(qa);
						}
					}
				}
				if (q instanceof SimpleWord) {
					if (((SimpleWord) q).isNoun()) {
						ns.add(q);
					}
				}
			}
		}
		if (ns.size() == 1) {
			return ns.iterator().next();
		}
		RelationTarget cand = null;
		HashSet<RelationTarget> ns1 = new HashSet<RelationTarget>();
		for (RelationTarget t : ns) {
			if (t instanceof SimpleWord) {
				String basicForm = ((SimpleWord) t).getBasicForm();
				if (basicForm.equals("история")) {
					continue;
				}
				if (basicForm.equals("год")) {
					continue;
				}
				if (basicForm.equals("вид")) {
					continue;
				}
			}
			ns1.add(t);
		}
		if (ns1.size() == 1) {
			return ns1.iterator().next();
		}
		l2: for (RelationTarget t : ns) {

			AbstractRelation[] r = sequence.get(t);
			if (r == null) {
				return null;
			}
			RelationTarget c = null;
			for (AbstractRelation q : r) {
				if (q.relation == NounFormRule.NOM_PL
						|| q.relation == NounFormRule.NOM_SG || q.relation == 0) {
					if (q.relation == NounFormRule.NOM_PL) {
						if (t instanceof SimpleWord) {
							SimpleWord z = (SimpleWord) t;
							if ((z.features & MeaningElement.FEATURE_NAME) != 0) {
								continue;
							}
							if ((z.features & MeaningElement.FEATURE_TOPONIM) != 0) {
								continue;
							}
						}
					}
					if (cand == null) {
						cand = t;
					} else {
						c = cand;
						cand = null;
					}
				}
				if (q.relation == NounFormRule.GEN_SG
						|| q.relation == NounFormRule.GEN_PL) {
					if (c != null) {
						cand = c;
						continue l2;
					}
				}
			}
		}
		return cand;
	}

	public int matchRelated(WordSequence sequence) {
		AbstractRelationTarget core = (AbstractRelationTarget) getCore();
		AbstractRelationTarget core2 = (AbstractRelationTarget) sequence
				.getCore();
		if (core != null && core2 != null) {
			if (core.equals(core2)) {
				return 2;
			}
			AbstractRelation[] relations = core.getRelations();
			AbstractRelation[] relations2 = core2.getRelations();
			IntOpenHashSet m = new IntOpenHashSet();
			for (AbstractRelation q : relations2) {
				q.setOwner((SimpleWordNet) WordNetProvider.getInstance());
				m.add(q.conceptId);
			}
			for (AbstractRelation z : relations) {
				z.setOwner((SimpleWordNet) WordNetProvider.getInstance());
				if (m.contains(z.conceptId)) {
					return 7;
				}
			}
			if (m.contains(core.id())) {
				return 6;
			}
		}
		// if (core2==null){
		for (RelationTarget t : sequence.targets) {
			if (t.equals(core)) {
				return 10;
			}
		}
		// }
		return -1;
	}*/

}