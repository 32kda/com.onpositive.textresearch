package com.onpositive.semantic.search.core;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashSet;

public class SearchRequest {

	protected Integer count;
	protected int offset;
	protected String keyword;
	protected String searchMode;
	protected boolean showBlackList;
	public boolean isShowBlackList() {
		return showBlackList;
	}

	public void setShowBlackList(boolean showBlackList) {
		this.showBlackList = showBlackList;
	}

	public static final String MODE_POPULARITY="POPULARITY";
	public static final String MODE_MIXED="MIXED";
	public static final String MODE_RELEVANCE="RELEVANCE";
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((engines == null) ? 0 : engines.hashCode());
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + maxRelevancy;
		result = prime * result + offset;
		result = prime * result + (onlyEvents ? 1231 : 1237);
		result = prime * result
				+ ((searchMode == null) ? 0 : searchMode.hashCode());
		result = prime * result + (useQuickSearch ? 1231 : 1237);
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
		SearchRequest other = (SearchRequest) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (count == null) {
			if (other.count != null)
				return false;
		} else if (!count.equals(other.count))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (engines == null) {
			if (other.engines != null)
				return false;
		} else if (!engines.equals(other.engines))
			return false;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (maxRelevancy != other.maxRelevancy)
			return false;
		if (offset != other.offset)
			return false;
		if (onlyEvents != other.onlyEvents)
			return false;
		if (searchMode == null) {
			if (other.searchMode != null)
				return false;
		} else if (!searchMode.equals(other.searchMode))
			return false;
		if (useQuickSearch != other.useQuickSearch)
			return false;
		return true;
	}

	public String getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	protected int maxRelevancy=1200;
	public int getMaxRelevancy() {
		return maxRelevancy;
	}

	public void setMaxRelevancy(int maxRelevancy) {
		this.maxRelevancy = maxRelevancy;
	}

	protected String category;
	protected String location;
	protected String date;
	protected boolean useQuickSearch=true;
	
	public boolean isUseQuickSearch() {
		return useQuickSearch;
	}

	public void setUseQuickSearch(boolean useQuickSearch) {
		this.useQuickSearch = useQuickSearch;
	}

	protected HashSet<String>engines;
	
	public HashSet<String> getEngines() {
		return engines;
	}

	public void setEngines(HashSet<String> engines) {
		this.engines = engines;
	}

	protected boolean onlyEvents;
	
	public boolean isOnlyEvents() {
		return onlyEvents;
	}

	public void setOnlyEvents(boolean onlyEvents) {
		this.onlyEvents = onlyEvents;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	
	
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count=count;
	}

	public void setMaxInRelevancy(int maxRelevancy) {
		this.maxRelevancy=maxRelevancy;
	}

	public void print(PrintStream logStream) {
		logStream.print("#"+System.identityHashCode(this)+' '+new Date());
		logStream.print(" category:"+category+" "+" date:"+date+" "+"location: "+location+" keywords: "+keyword+" mode:"+searchMode);
		logStream.print(" sources:"+this.engines+" onlyEvents:"+isOnlyEvents()+" relevencyLimit:"+maxRelevancy);
		logStream.println(" offset:"+offset+" "+"count:"+count);
	}
}
