package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel;

class ClusterNode implements Comparable<ClusterNode> {
	protected WikiCat cat;
	protected HashMap<WikiDoc, Integer> docs = new HashMap<WikiDoc, Integer>();
	protected ArrayList<ClusterNode> joined = new ArrayList<ClusterNode>();

	@Override
	public int compareTo(ClusterNode o) {
		return (int) (-(docs.size() * averageDistance() - o.docs.size()
				* o.averageDistance()) * 10);
	}

	public TitleModel getTitleModel() {
		if (titleModel == null) {
			titleModel = TitleModel.get(cat.getTitle(), false);
		}
		return titleModel;
	}

	Double dd = null;
	private TitleModel titleModel;

	double averageDistance() {
		if (dd != null) {
			return dd;
		}
		double ss = 0;
		for (Integer q : docs.values()) {
			ss += 6 - q;
		}
		double d = ss / docs.size();
		dd = d;
		return d;
	}
	double average() {
		if (dd != null) {
			return dd;
		}
		double ss = 0;
		for (Integer q : docs.values()) {
			ss += q;
		}
		double d = ss / docs.size();
		dd = d;
		return d;
	}

	@Override
	public String toString() {
		HashSet<WikiDoc> ds = getTotalSet();
		return cat.getTitle() + ":" + ds.size();
	}

	HashSet<WikiDoc> getTotalSet() {
		HashSet<WikiDoc> ds = new HashSet<WikiDoc>(docs.keySet());
		for (ClusterNode q : joined) {
			ds.addAll(q.docs.keySet());
		}
		return ds;
	}

	public boolean hasZero() {
		for (Integer q : docs.values()) {
			if (q == 0) {
				return true;
			}
		}
		return false;
	}
}