package com.onpositive.semantic.wikipedia2.catrelations;

import java.io.File;
import java.io.IOException;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.onpositive.compactdata.TwoIntToByteMap;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.WikiEngineService;

public class ConstrainedCategoryGraph extends WikiEngineService {

	TwoIntToByteMap vec;

	public ConstrainedCategoryGraph(WikiEngine2 engine) {
		super(engine);
	}

	@Override
	protected void doLoad(File fl) throws IOException {
		vec = new TwoIntToByteMap();
		vec.read(fl.getAbsolutePath());
	}

	static class MergeNode {
		protected int cid;

		protected IntObjectOpenHashMap<MergeNode> toMerge;

		protected IntObjectOpenHashMap<MergeNode> subMerge;

		protected MergeNode appendMerge(int q) {
			if (toMerge == null) {
				toMerge = new IntObjectOpenHashMap<ConstrainedCategoryGraph.MergeNode>();
			}
			MergeNode mergeNode = toMerge.get(q);
			if (mergeNode == null) {
				mergeNode = new MergeNode();
				toMerge.put(q, mergeNode);
			}
			return mergeNode;
		}

		protected MergeNode appendSub(int q) {
			if (subMerge == null) {
				subMerge = new IntObjectOpenHashMap<ConstrainedCategoryGraph.MergeNode>();
			}
			MergeNode mergeNode = subMerge.get(q);
			if (mergeNode == null) {
				mergeNode = new MergeNode();
				subMerge.put(q, mergeNode);
			}
			return mergeNode;
		}
	}

	@Override
	protected void build(WikiEngine2 enfine) {
		int[] ids = enfine.getCategoryRenumberer().getIds();
		SimpleNameBasedEstimator simpleNameBasedEstimator = new SimpleNameBasedEstimator(
				enfine);
		MergeNode root = new MergeNode();

		ByCategorizerCriteria bc = new ByCategorizerCriteria(enfine);
		for (int c : ids) {
			MergeNode categoryMergeable = root.appendMerge(c);
			ICategory cat = engine.getCategory(c);
			ICategory[] subCategories = cat.getSubCategories();
			for (ICategory child : subCategories) {
				if (mergeable(simpleNameBasedEstimator, bc, c, cat, child)) {
					categoryMergeable.appendMerge(((WikiCat) child).getIntId());
				}
			}
		}
	}

	boolean mergeable(SimpleNameBasedEstimator simpleNameBasedEstimator,
			ByCategorizerCriteria bc, int c, ICategory cat, ICategory child) {
		return simpleNameBasedEstimator.isA(cat, child)
				|| bc.relation(c, ((WikiCat) child).getIntId(), null) < 10;
	}

	@Override
	protected void doSave(File fl) throws IOException {
		// vec.write(fl.getAbsolutePath());
	}

	@Override
	public String getFileName() {
		return "ccg.dat";
	}

}