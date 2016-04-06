package com.onpositive.wikipedia.workbench;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class TreeBuilder {
	
	private Map<String, Node> nodeMap= new HashMap<String,Node>();
//	private List<Node> rootNodes= new ArrayList<>();
	
	private String[] SPEC_ITEMS = new String[] {"NOUN","ALL","STM"};
	
	class Node {
		@Expose(deserialize=false,serialize=false) 
		public List<Node> parents = new ArrayList<>();
		@Expose
		public String name;
		@Expose
		public List<Node> children = new ArrayList<>();
		
		public Node(String name) {
			this.name = name; 
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		
		private TreeBuilder getOuterType() {
			return TreeBuilder.this;
		}
	}
	
	public List<Node> build() {
		try {
			Stream<String> streamFromFiles = Files.lines(Paths.get("categorized0.txt"), StandardCharsets.UTF_8);
			processStream(streamFromFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void processStream(Stream<String> stringStream)
			throws IOException {
		Node rootNode = calculateRootNode(stringStream);
		Writer writer = new BufferedWriter(new FileWriter("categorized.json"));
		Gson gson = new GsonBuilder()
		.excludeFieldsWithoutExposeAnnotation()
		.setPrettyPrinting().create();
		gson.toJson(rootNode, writer);
		writer.close();
	}

	public Node calculateRootNode(Stream<String> stringStream) {
		stringStream.forEach(str -> processLine(str));
		stringStream.close();
		int i = 0;
		Node rootNode = new Node("root");
		for (Node value: nodeMap.values()) {
			if (value.parents.isEmpty() && !value.children.isEmpty()) {
				rootNode.children.add(value);
				i++;
			}
			if (i > 200) {
				break;
			}
		}
		return rootNode;
	}

	private void processLine(String str) {
		str = checkSpecItem(str);
		String[] splitted = str.split("->");
		if (splitted.length < 2) {
			return;
		}
		String childName = splitted[0].trim();
		String parentName = splitted[1].trim();
		Node childNode = getNode(childName);
		Node parentNode = getNode(parentName);
		if (canAdd(parentNode, childNode)) {
			parentNode.children.add(childNode);
			childNode.parents.add(parentNode);
		}
	}
	
	private boolean canAdd(Node parentNode, Node childNode) {
		if (parentNode == childNode) {
			return false;
		}
		for (Node node : childNode.children) {
			if (!canAdd(parentNode, node)) {
				return false;
			}
		}
		return true;
	}

	private Node getNode(String nodeName) {
		Node node = nodeMap.get(nodeName);
		if (node == null) {
			node = new Node(nodeName);
			nodeMap.put(nodeName, node);
		}
		return node;
	}

	private String checkSpecItem(String str) {
		for (String item : SPEC_ITEMS) {
			if (str.endsWith(item)) {
				return str.substring(0, str.length() - item.length());
			}
		}
		return str;
	}
	
	public static void main(String[] args) {
		new TreeBuilder().build();
	}

}
