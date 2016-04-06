package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;
import java.util.HashSet;

import com.onpositive.semantic.wikipedia2.WikiDoc;

public class BasicPropertyStat {

	public boolean isIdent;
	private ArrayList<PropertyValues> values;

	private ArrayList<String> names = new ArrayList<String>();

	public ArrayList<PropertyValues> getValues() {
		return values;
	}

	public BasicPropertyStat(ArrayList<SourceInformation> vals) {
		SourceData data = new SourceData(vals);
		values = new PropertyParser().prepare(data);
		HashSet<WikiDoc> ds = new HashSet<WikiDoc>();
		int count = 0;
		int hasLinkCount = 0;
		for (PropertyValues v : values) {
			if (!v.values.isEmpty()) {
				count++;
			}
			boolean inChl = false;
			for (PropertyValue<?> q : v.values) {
				if (q instanceof LinkValue) {
					if (!inChl) {
						hasLinkCount++;
						inChl = true;
					}
					LinkValue l = (LinkValue) q;
					WikiDoc d = l.value.document;
					ds.add(d);
				}
			}
		}
		if (hasLinkCount * 2 > count) {
			isIdent = true;
		}
		ClusterNodes clusterNodes = new ClusterNodes();
		ArrayList<ClusterNode> build = clusterNodes.build(ds);
		if (build.size() > 0) {
			for (ClusterNode q : build) {
				this.names.add(q.cat.getTitle());
			}
		}
	}
}
