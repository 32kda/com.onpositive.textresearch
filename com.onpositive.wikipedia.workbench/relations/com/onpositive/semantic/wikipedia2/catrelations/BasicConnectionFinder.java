package com.onpositive.semantic.wikipedia2.catrelations;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.wikipedia2.catrelations.Connection.Type;

public class BasicConnectionFinder extends AbstractConnectionFinder {
	
	protected Map<Long, Connection> foundConnections = new HashMap<>();

	public BasicConnectionFinder() {
		super();
	}

	@Override
	protected void handleNewConnection(Connection newConnection) {
		int id1 = getId(newConnection.getEntity());
		int id2 = newConnection.getCategoryId();
		Long key = encodeIds(id1, id2);
		foundConnections.put(key, newConnection);
	}

	protected int getId(ICategorizable entity) {
		if (entity instanceof ICategory) {
			return -entity.getIntId();
		}
		return entity.getIntId();
	}

	@Override
	protected Type getFoundConnection(ICategorizable categorizable,
			ICategory curCategory) {
		int id1 = getId(categorizable);
		int id2 = curCategory.getIntId();
		Long key = encodeIds(id1, id2);
		Connection connection = foundConnections.get(key);
		if (connection != null) {
			return connection.getType();
		}
		return null;
	}

	private long encodeIds(int id1, int id2) {
		return id1 << 32 | id2 & 0xFFFFFFFFL;
	}
	
	public String getStringRepresenation() {
		TreeMap<Long, Connection> sortedMap = new TreeMap<Long, Connection>((Long l1, Long l2) -> 
			foundConnections.get(l1).toString().compareTo(foundConnections.get(l2).toString()));
		sortedMap.putAll(foundConnections);
		StringBuilder builder = new StringBuilder();
		sortedMap.keySet().forEach(key -> builder.append(foundConnections.get(key).toString() + "\n"));
		return builder.toString();
	}

}
