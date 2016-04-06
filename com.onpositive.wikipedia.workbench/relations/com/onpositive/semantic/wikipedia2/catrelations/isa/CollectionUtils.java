package com.onpositive.semantic.wikipedia2.catrelations.isa;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.onpositive.semantic.wikipedia2.properties.parsing.MultiMap;

public class CollectionUtils {

	public static <T,K> MultiMap<K, T> 
	groupBy(Function<T,K>f,Stream<T>arg){
		MultiMap<K, T>res=MultiMap.withSet();
		arg.forEach(x->{
			res.add(f.apply(x), x);
		});
		return res;
	}
	public static <T> T last(List<T>list){
		if (list.isEmpty()){
			return null;
		}
		return list.get(list.size()-1);
	}
	public static <T> T first(List<T>list){
		if (list.isEmpty()){
			return null;
		}
		return list.get(0);
	}
}
