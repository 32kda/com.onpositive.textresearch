package com.onpositive.semantic.wikipedia2.catrelations.isa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlackList implements Predicate<String>{

	
	public static abstract class BlackListCriteria{
		public abstract boolean match(String title);
	}
	
	public static class ColonBlackLister extends BlackListCriteria{
		public boolean match(String title){
			int indexOf = title.indexOf(':');
			if (indexOf!=-1){
				for (int a=0;a<indexOf;a++){
					if (title.charAt(a)=='_'){
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}
	
	public static class NotLetterOrEnglish extends BlackListCriteria{
		
		
		public boolean match(String title){
			if(title.length()==0){
				return true;
			}
			if (!Character.isLetter(title.charAt(0))){
				return true;
			}
			char c=Character.toLowerCase(title.charAt(0));
			if (c>='a'&&c<='z'){
				return true;
			}
			return false;
		}
	}
	
	public static class StartsWith extends BlackListCriteria{

		protected String pattern;
		
		public StartsWith(String pattern) {
			super();
			this.pattern = pattern;
		}

		@Override
		public boolean match(String title) {
			return title.startsWith(this.pattern);
		}
		
	}
	public static class EndsWith extends BlackListCriteria{

		protected String pattern;
		
		public EndsWith(String pattern) {
			super();
			this.pattern = pattern;
		}

		@Override
		public boolean match(String title) {
			return title.endsWith(this.pattern);
		}
		
	}
	public static class Contains extends BlackListCriteria{

		protected String pattern;
		
		public Contains(String pattern) {
			super();
			this.pattern = pattern;
		}

		@Override
		public boolean match(String title) {
			return title.indexOf(this.pattern)!=-1;
		}
		
	}
	ArrayList<BlackListCriteria>criterias=new ArrayList<>();
	public BlackList ends(String... pattern){
		for (String p:pattern){
			this.criterias.add(new EndsWith(p));
		}
		return this;
	}
	public BlackList contains(String... pattern){
		for (String p:pattern){
			this.criterias.add(new Contains(p));
		}
		return this;
	}
	public BlackList starts(String... pattern){
		for (String p:pattern){
			this.criterias.add(new StartsWith(p));
		}
		return this;
	}
	public BlackList happenedIn(String... pattern){
		for (String p:pattern){
			this.criterias.add(new StartsWith(p+"_в_"));
			this.criterias.add(new StartsWith(p+"_по_"));
			this.criterias.add(new StartsWith(p+"_на_"));
			this.criterias.add(new StartsWith(p+"_во_"));
			this.criterias.add(new StartsWith(p+"_от_"));
		}
		return this;
	}
	
	
	public Boolean apply(String s){
		s=s.toLowerCase();
		for (BlackListCriteria c:this.criterias){
			if (c.match(s)){
				return true;
			}
		}
		return false;
	}
	
	public static BlackList getDefault(){
		BlackList blackList = new BlackList();
		//Служебное
		blackList.criterias.add(new ColonBlackLister());
		//Всякая английская фигня
		blackList.criterias.add(new NotLetterOrEnglish());
		//История это все история
		blackList.starts("история_","события_");
		//Категориия статей и списков
		blackList.starts("статьи_","списки");
		blackList.contains("_шаблоны:");
		blackList.contains("статьи_проекта_","_статьи_о_","_статьи_по_","_списки_проекта_");
		//Хронология
		blackList.contains(
				"_год_в","_в_годы_","_год_на_","-е_годы_","-е_в_",
				"-е_во_","-е_годы_по_","-е_годы_в_","_год_до_","век_в").
		ends("_году","года","_января",
				"_февраля","_марта","_апреля","_июня","_июля","_августа",
				"_cентября","_октября","_ноября","_декабря");
		//Частые причастия
		blackList.happenedIn("родившиеся","казнённые","умершие","похороненные","появились","исчезли","погибшие");
		return blackList;
	}
	@Override
	public boolean test(String t) {
		return !apply(t);
	}
	
	public static Stream<String> filter(Collection<String>s){
		return s.stream().filter(getDefault());
	}
}
