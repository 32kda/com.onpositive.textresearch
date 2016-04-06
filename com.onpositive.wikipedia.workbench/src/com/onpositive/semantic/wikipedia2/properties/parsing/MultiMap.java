package com.onpositive.semantic.wikipedia2.properties.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MultiMap<K, V> extends LinkedHashMap<K, Collection<V>> {

	protected final Supplier<Collection<V>> suplier;

	public MultiMap(Supplier<Collection<V>> suplier) {
		super();
		this.suplier = suplier;
	}

	public class StatEntry<K> implements Comparable<StatEntry<K>>{

		protected K value;
		
		
		public StatEntry(K value) {
			super();
			this.value = value;
		}


		@Override
		public int compareTo(StatEntry<K> o) {
			return count()-o.count();
		}


		public int count() {
			return MultiMap.this.get(value).size();
		}
		
		public String toString(){
			return value+":"+this.count();
		}
		
	}
	
	public static <K, V> MultiMap<K, V> withSet() {
		return new MultiMap<K, V>(HashSet::new);
	}
	public static <K, V> MultiMap<K, V> withList() {
		return new MultiMap<K, V>(ArrayList::new);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void add(K key, V value) {
		Collection<V> list = get(key);
		if (list == null) {
			list = suplier.get();
			put(key, list);
		}
		list.add(value);
	}
	public ArrayList<StatEntry<K>>toStat(){
		ArrayList<StatEntry<K>>result=new ArrayList<>();
		for (K k:this.keySet()){
			result.add(new StatEntry<K>(k));
		}
		return result;
	}
	
}
