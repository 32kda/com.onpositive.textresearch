package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.WikiCat;
import com.onpositive.semantic.wikipedia2.WikiDoc;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.catrelations.SimpleNameBasedEstimator;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel;
import com.onpositive.semantic.wikipedia2.catrelations.TitleModel.WordModel;

public class ClusterNodes {

	private static final int MAX_LEVEL = 5;
	HashMap<String, ClusterNode> nodes = new HashMap<String, ClusterNode>();

	public ArrayList<ClusterNode> build(HashSet<WikiDoc> ds) {
		for (WikiDoc d : ds) {
			ICategory[] categories = d.getCategories();
			for (ICategory c : categories) {
				getOrCreateNode(c, d);
			}
		}
		ClusterNode[] all = nodes.values().toArray(
				new ClusterNode[nodes.values().size()]);
		Arrays.sort(all);
		// HashSet<WikiDoc>ns=new HashSet<WikiDoc>();
		ArrayList<ClusterNode> nm = new ArrayList<ClusterNode>();
		for (ClusterNode q : all) {
			if (isOk(q.cat.getTitle())) {
				for (ClusterNode qz : nm) {
					for (WikiDoc m : qz.docs.keySet()) {
						if (q.docs.containsKey(m)) {
							Integer integer = q.docs.get(m);
							Integer integer2 = qz.docs.get(m);
							if (integer2 < integer) {
								q.docs.remove(m);
							}
						}
					}
				}
				if (!q.docs.isEmpty()) {
					nm.add(q);
				}
			}
		}
		nm = tryTransform(nm);
		Collections.sort(nm);
		ClusterNode chooseBest = chooseBest(nm);
		joinPossible(chooseBest, nm);
		ArrayList<ClusterNode> allCandidates = new ArrayList<ClusterNode>();
		if (chooseBest != null) {
			allCandidates.add(chooseBest);
		}
		for (ClusterNode q : nm) {
			String title = q.cat.getTitle();
			if (!title.contains(",") && !title.contains("_статьи")
					&& !title.contains("_по_")) {
				TitleModel titleModel = TitleModel.get(title, false);
				if (titleModel.hasSingleCore()) {
					if (titleModel.getSingleCore().isPlural()
							&& q.docs.size() > 3 && q.hasZero()) {

						allCandidates.add(q);
					}
				}
			}
		}
		ArrayList<ClusterNode> summarizedCands = new ArrayList<ClusterNode>();
		int sum = 0;
		int dsize = ds.size();
		double averageLevel = 0;
		HashSet<WikiDoc> kk = new HashSet<WikiDoc>();
		for (int a = 0; a < allCandidates.size(); a++) {
			ClusterNode clusterNode = allCandidates.get(a);
			HashSet<WikiDoc> totalSet = clusterNode.getTotalSet();
			totalSet.removeAll(kk);
			double averageDistance = clusterNode.average();
			if (totalSet.size() > 5) {
				if (kk.size() == 0 || (!totalSet.isEmpty())
						&& kk.size() / totalSet.size() < 5) {
					if (averageDistance > (averageLevel - 0.3)) {
						kk.addAll(totalSet);
						summarizedCands.add(clusterNode);

						sum = kk.size();

						if (sum * 10 > dsize * 9&&averageDistance<3) {
							break;
						}
					}
				}
			}

			averageLevel = averageDistance;
		}
		return summarizedCands;
	}

	private ArrayList<ClusterNode> tryTransform(ArrayList<ClusterNode> nm) {
		HashMap<WikiCat, ArrayList<ClusterNode>> ss = new HashMap<WikiCat, ArrayList<ClusterNode>>();

		for (ClusterNode q : new ArrayList<ClusterNode>(nm)) {
			TitleModel titleModel = q.getTitleModel();
			WikiEngine2 engine = q.cat.getEngine();
			if (titleModel.hasSingleCore()) {
				WordModel singleCore = titleModel.getSingleCore();
				if (singleCore.isPlural() && singleCore.isGoodNoun()) {
					String text = singleCore.getText();
					int categoryId = engine.getCategoryId(Character
							.toUpperCase(text.charAt(0)) + text.substring(1));
					if (categoryId > 0) {
						WikiCat category = (WikiCat) engine
								.getCategory(categoryId);
						ArrayList<ClusterNode> ls = ss.get(category);
						if (ls == null) {
							ls = new ArrayList<ClusterNode>();

							ss.put(category, ls);
						}
						
						ls.add(q);
					}
				}
			}
		}
		for (WikiCat c : ss.keySet()) {
			ArrayList<ClusterNode> i = ss.get(c);
			if (i.size() > 3) {
				ClusterNode newNode = new ClusterNode();
				newNode.cat = c;
				
				for (ClusterNode nma : i) {
					nm.remove(nma);	
					newNode.docs.putAll(nma.docs);
				}
				nm.add(0, newNode);
			}
		}
		return nm;
	}

	private void joinPossible(ClusterNode chooseBest, ArrayList<ClusterNode> nm) {
		if (chooseBest == null) {
			return;
		}
		SimpleNameBasedEstimator simpleNameBasedEstimator = new SimpleNameBasedEstimator(
				chooseBest.cat.getEngine());
		for (ClusterNode q : new ArrayList<ClusterNode>(nm)) {
			if (q != chooseBest) {
				if (q.cat.getEngine() == chooseBest.cat.getEngine()) {
					if (simpleNameBasedEstimator.isA(chooseBest.cat, q.cat)) {
						chooseBest.joined.add(q);
						nm.remove(q);
					}
				}
			}
		}
		nm.remove(chooseBest);
	}

	ClusterNode chooseBest(ArrayList<ClusterNode> sortedList) {
		ClusterNode chooseBest = null;
		for (ClusterNode c : sortedList) {
			TitleModel titleModel = TitleModel.get(c.cat.getTitle(), false);
			if (titleModel.hasSingleCore()
					&& c.cat.getTitle().indexOf(',') == -1
					&& c.cat.getTitle().indexOf("_по_") == -1) {
				WordModel singleCore = titleModel.getSingleCore();
				if (singleCore.isPlural() && singleCore.isGoodNoun()) {
					chooseBest = c;
					break;
				}
			}
		}
		if (chooseBest != null) {
			SimpleNameBasedEstimator simpleNameBasedEstimator = new SimpleNameBasedEstimator(
					chooseBest.cat.getEngine());

			for (ClusterNode q : new ArrayList<ClusterNode>(sortedList)) {
				if (q != chooseBest) {
					if (q.cat.getEngine() == chooseBest.cat.getEngine()) {
						if (simpleNameBasedEstimator.isA(chooseBest.cat, q.cat)) {
							String title = q.cat.getTitle();
							if (title.indexOf("_по_") != -1) {
								continue;
							}
							TitleModel titleModel = TitleModel
									.get(title, false);
							if (titleModel.hasSingleCore()
									&& titleModel.getSingleCore().isPlural()
									&& titleModel.getSingleCore().isGoodNoun()) {
								String title2 = chooseBest.cat.getTitle();
								if (titleModel.models.size() < TitleModel.get(
										title2, false).models.size()) {
									if (isSub(q, chooseBest)
											|| title2.startsWith(title)) {
										// if
										// (q.docs.size()*3>chooseBest.docs.size()){
										chooseBest = q;
										// }
									}
								}
							}
						}
					}
				}
			}
		}
		return chooseBest;
	}

	private boolean isSub(ClusterNode q, ClusterNode chooseBest) {
		HashSet<ICategory> cm = new HashSet<ICategory>();
		gatherChildren(cm, q.cat, 0);
		boolean contains = cm.contains(chooseBest.cat);
		return contains;
	}

	private void gatherChildren(HashSet<ICategory> cm, WikiCat cat, int i) {
		if (i < 6) {
			ICategory[] subCategories = cat.getSubCategories();
			for (ICategory z : subCategories) {
				if (cm.add(z)) {
					gatherChildren(cm, (WikiCat) z, i + 1);
				}
			}
		}
	}

	private boolean isOk(String title) {
		if (title.contains(":")) {
			return false;
		}
		if (title.startsWith("Скрытые_")) {
			return false;
		}
		if (title.toLowerCase().contains("cтатьи")) {
			return false;
		}
		if (title.toLowerCase().contains("cтатьи")) {
			return false;
		}
		if (title.startsWith("Статьи")) {
			return false;
		}
		if (title.startsWith("Классификация")) {
			return false;
		}
		if (title.startsWith("Категории_")) {
			return false;
		}
		if (title.equals("Всё")) {
			return false;
		}
		if (title.startsWith("All_articles_with")) {
			return false;
		}
		return true;
	}

	private void getOrCreateNode(ICategory c, WikiDoc d) {
		int level = 0;
		register(c, d, level);
	}

	void register(ICategory c, WikiDoc d, int level) {
		ClusterNode clusterNode = nodes.get(c.getTitle());
		if (clusterNode == null) {
			clusterNode = new ClusterNode();
			clusterNode.cat = (WikiCat) c;
			nodes.put(c.getTitle(), clusterNode);
		}
		Integer integer = clusterNode.docs.get(d);
		if (integer == null) {
			integer = Integer.MAX_VALUE;
		}
		int lvl = Math.min(integer, level);
		if (lvl < integer) {
			clusterNode.docs.put(d, lvl);
			if (lvl < MAX_LEVEL) {
				registerChilds(d, c, lvl + 1);
			}
		}

	}

	private void registerChilds(WikiDoc d, ICategory c, int i) {
		ICategory[] parentCategories = c.getParentCategories();
		for (ICategory q : parentCategories) {
			register(q, d, i);
		}
	}
}