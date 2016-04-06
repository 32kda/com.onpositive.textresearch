package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class HasAnotherGoodParent extends AbstractRelationEstimator{

	private WikiEngine2 engine;
	CompositeEstimator estimator;

	public HasAnotherGoodParent(WikiEngine2 eng) {
		super("a","a","b");
		this.engine=eng;
		estimator=new CompositeEstimator(eng){
			protected AbstractRelationEstimator[] createPack(WikiEngine2 eng) {
				return new AbstractRelationEstimator[]{
					new SimpleNameBasedEstimator(eng),										
					new ByCategorizerCriteria(eng),
					new PrimaryArticleMembershipCriteria(eng)
				};
			}
		};
	}

	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		int[] direct2 = engine.getCategoryToParentCategories().getInverse(c2);
		for (int q:direct2){
			if (q!=c1){
				byte relation = estimator.relation(q, c2, null);
				if(relation<5){
					byte relation1 = estimator.relation(c1, c2, null);
					if (relation1>=10){					
					return 7;
					}
				}
			}
		}
		return 7;
	}
}
