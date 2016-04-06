package com.onpositive.semantic.wikipedia2.search;


public class ResultComposer {

	static class ThemeLayer{
	
		static final int IDEAL_THEME_MATCH=1;
		
		static final int GOOD_THEME_MATCH=2;
		
		static final int JUNKY_MATCH=3;

	}
	
	static class DateMatchLayer{
		static final int IT_IS_ABOUT_THIS_DATE=1;
	
		static final int DATE_MENTIONED=2;
	}

	static class LocationMatchLayer{
		
		static final int IT_IS_ABOUT_THIS_LOCATION=1;
		
		static final int LOCATION_MENTIONED=2;
	}
	
	static class HasImage{
		
		static final int HAS_IMAGE=1;
		
		static final int NO_IMAGE=2;
	}
	
	
}