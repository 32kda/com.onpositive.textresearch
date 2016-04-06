package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.PrintStream;

import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class CompositeEstimator extends AbstractRelationEstimator{

	protected AbstractRelationEstimator[] estimators;
	
	public CompositeEstimator(WikiEngine2 eng) {
		super("all", "all", "all");
		estimators=createPack(eng);
	}



	protected AbstractRelationEstimator[] createPack(WikiEngine2 eng) {
		return new AbstractRelationEstimator[]{
			//если совпадает главное слово (либо, одно главное слово окончание другого, либо главные слава в семантическом отношени
			new SimpleNameBasedEstimator(eng),
			//если содержит стем главного слова родителя в неглавном слове
			new NotACritery(eng),
			//ecли родитель один
			new OnlyParentCriteria(eng),
			//если дите категаризационной категории
			new ByCategorizerCriteria(eng),
			//если основная статья категории наше дите
			new PrimaryArticleMembershipCriteria(eng),
			//если есть сильно более хорошая ветка то увеличиваем стоимость
			new HasAnotherGoodParent(eng)
		};
	}

	

	@Override
	public byte relation(int c1, int c2, PrintStream log) {
		byte val=Byte.MAX_VALUE;
		for (AbstractRelationEstimator q:estimators){
			byte relation = q.relation(c1, c2, log);
			if (relation>10){
				val=relation;
				break;
			}
			if (relation<val){
				val=relation;
				if( val<=2){
					break;
				}
			}
		}
		
		return val;
	}
}
