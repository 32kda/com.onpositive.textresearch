package com.onpositive.text.analisys.tests.euristics;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {

    Map<K, V> map;

    public ValueComparator(Map<K, V> base) {
        this.map = base;
    }

    @Override
    public int compare(K o1, K o2) {
         return map.get(o2).compareTo(map.get(o1));
    }
}