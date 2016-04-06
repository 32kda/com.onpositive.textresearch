package com.onpositive.semantic.wikipedia2;

import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.RatedCat;
import com.onpositive.semantic.search.core.WordStat;
import com.onpositive.semantic.wikipedia2.catrelations.CompositeEstimator;
import com.onpositive.semantic.wikipedia2.services.DistanceMarker;

public class WikiCat implements ICategory{

	int id;
	WikiEngine2 engine;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((engine == null) ? 0 : engine.hashCode());
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WikiCat other = (WikiCat) obj;
		if (engine == null) {
			if (other.engine != null)
				return false;
		} else if (!engine.equals(other.engine))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	String title;
	
	public WikiCat(int q, WikiEngine2 engine2) {
		this.id=q;
		this.engine=engine2;
	}
	public final int getIntId(){
		return id;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getTitle() {
		if (title==null){
			title=engine.getCategoryTitles().get(id);
		}
		return title;
	}

	@Override
	public ICategory[] getSubCategories() {
		int[] direct = engine.getCategoryToParentCategories().getDirect(id);
		ICategory[] c=new ICategory[direct.length];
		int a=0;
		for (int q:direct){
			c[a++]=new WikiCat(q,engine);
		}
		return c;
	}
	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public RatedCat[] getSameThemeChildren() {
		ICategory[] subCategories = getSubCategories();
		RatedCat[] rr=new RatedCat[subCategories.length];
		int a=0;
		for (ICategory m:subCategories){
			WikiCat w=(WikiCat) m;
			rr[a++]=new RatedCat(m,new CompositeEstimator(engine).relation(id, w.id, null));
		}
		return rr;
	}

	@Override
	public WikiDoc[] getPages() {
		int[] direct = engine.getPageToParentCategories().getDirect(id);
		WikiDoc[] c=new WikiDoc[direct.length];
		int a=0;
		for (int q:direct){
			c[a++]=new WikiDoc(engine,q);
		}
		return c;
	}

	@Override
	public ICategory[] getParentCategories() {
		int[] direct = engine.getCategoryToParentCategories().getInverse(id);
		ICategory[] c=new ICategory[direct.length];
		int a=0;
		for (int q:direct){
			c[a++]=new WikiCat(q,engine);
		}
		return c;
	}

	@Override
	public int getRootDistance() {
		return engine.getIndex(DistanceMarker.class).getDistanceFromRoot(id);
	}

	@Override
	public WordStat getStatistics() {
		return null;
	}

	@Override
	public double textDistance(ICategory c) {
		return 0;
	}

	public WikiEngine2 getEngine() {
		return engine;
	}
	@Override
	public ICategory[] getCategories() {
		return getParentCategories();
	}
}
