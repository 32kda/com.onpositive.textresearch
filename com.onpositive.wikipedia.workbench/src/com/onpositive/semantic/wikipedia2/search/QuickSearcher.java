package com.onpositive.semantic.wikipedia2.search;
import com.onpositive.semantic.search.core.ISearchResultCallback;
import com.onpositive.semantic.search.core.SearchRequest;
import com.onpositive.semantic.wikipedia2.WikiEngine2;

public class QuickSearcher {

	private ByThemeSearcher searcher;

	public QuickSearcher(WikiEngine2 engine) {
		
	}

	protected ThemeProvider provider;
	
	public void search(WikiEngine2 engine, SearchRequest request,
			ISearchResultCallback callback) {
		long l0=System.currentTimeMillis();
		if (searcher==null){
		searcher=new ByThemeSearcher(engine);
		}
		searcher.search(request, callback);
		long l1=System.currentTimeMillis();
		System.out.println("Search time:"+(l1-l0));
	}
	
	public void clearPopularity(){
		if (searcher!=null){
		searcher.clearPopularity();
		}
	}

	public void clear() {
		if (searcher!=null){
		searcher.clear();
		}
	}
}