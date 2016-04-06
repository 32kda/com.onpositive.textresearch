package com.onpositive.semantic.wikipedia2.catrelations;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.catrelations.isa.BlackList;

public abstract class AbstractConnectionFinder {
	
	private int maxIterations = 5;
	
	private static final int MAX_PARENT_CATS = 20;
	
	private List<IConnectionFactory> connectionFactories = new ArrayList<>();
	
	/**
	 * Find new connections for current entities
	 * Override if you want to use some other walkthrough (stack, recursive, etc.)
	 * 
	 * @param entities Entities to process. 
	 */
	public void findConnections(ICategorizable[] entities) {
		boolean foundNewConnections = true;
		for (int j = 0; foundNewConnections && j < maxIterations; j++) {
			foundNewConnections = false;
			for (ICategorizable categorizable : entities) {
				ICategory[] categories = categorizable.getCategories();
				for (int i = 0; i < categories.length; i++) {
					ICategory curCategory = categories[i];
					if (isSpecial(curCategory)) {
						continue;
					}
					foundNewConnections = doAnalyze(categorizable, curCategory) || foundNewConnections ;
					List<ICategory> possibleParents = new ArrayList<ICategory>();
					collectPossibleParents(curCategory, possibleParents);
					for (ICategory parentCategory : possibleParents) {
						foundNewConnections = doAnalyze(categorizable, parentCategory) || foundNewConnections;
						foundNewConnections = doAnalyze(curCategory, parentCategory) || foundNewConnections;
					}
				}
			}
			iterationFinished(j);
		}
	}
	
	protected void iterationFinished(int interation) {
		// Do nothing; Iterate if necessary
	}

	protected boolean isSpecial(ICategory curCategory) {
		String title = curCategory.getTitle();
		return !BlackList.getDefault().test(title) || title.contains(":");
	}

	private void collectPossibleParents(ICategory childCat,
			List<ICategory> possibleParents) {
		if (possibleParents.size() > MAX_PARENT_CATS) {
			return;
		}
		ICategory[] categories = childCat.getCategories();
		List<ICategory> collected = new ArrayList<>();
		for (ICategory currentCategory : categories) {
			if (currentCategory.getTitle().contains(":") || possibleParents.contains(currentCategory)) {
				continue;
			}
			possibleParents.add(currentCategory);
			collected.add(currentCategory);
		}
		for (ICategory category : collected) {
			collectPossibleParents(category, possibleParents);
		}
	}

	/**
	 * Find new connections for given entities against given categories
	 * Override if you want to use some other walkthrough (stack, recursive, etc.)
	 * 
	 * @param entities Entities to process. 
	 * @param possibleParents Possible parent categories
	 */
	public void findConnections(ICategorizable[] entities, ICategory[] possibleParents) {
		boolean foundNewConnections = true;
		for (int j = 0; foundNewConnections && j < maxIterations; j++) {
			foundNewConnections = false;
			for (ICategorizable categorizable : entities) {
				for (int i = 0; i < possibleParents.length; i++) {
					ICategory curCategory = possibleParents[i];
					foundNewConnections = foundNewConnections || doAnalyze(categorizable, curCategory);
				}
			}
		}
	}

	protected boolean doAnalyze(ICategorizable categorizable, ICategory curCategory) {
		if (getFoundConnection(categorizable, curCategory) == null) {
			for (IConnectionFactory connectionFactory : connectionFactories) {
				Connection newConnection = connectionFactory.createConnection(categorizable, curCategory);
				if (newConnection != null) {
					handleNewConnection(newConnection);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Handle new connection found. In most common case - store it in some collection/storage
	 * @param newConnection Connection found
	 */
	protected abstract void handleNewConnection(Connection newConnection);

	//Don't analyze if some result is already found
	protected abstract Connection.Type getFoundConnection(ICategorizable categorizable,
			ICategory curCategory);

	public boolean addConnectionFactory(IConnectionFactory connectionFactory) {
		return connectionFactories.add(connectionFactory);
	}

	public boolean removeConnectionFactory(Object connectionFactory) {
		return connectionFactories.remove(connectionFactory);
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

}
