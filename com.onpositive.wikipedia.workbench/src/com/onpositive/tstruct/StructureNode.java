package com.onpositive.tstruct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StructureNode {

	public static enum ChildAddMode{
		NEVER,AUTO,ALWAYS
	}

	@XmlAttribute
	protected String sourceId;
	@XmlAttribute
	protected String id;
	public String getId() {
		return id;
	}
	
	@XmlAttribute(required=false)
	protected boolean toTop=true;

	public boolean isToTop() {
		return toTop;
	}

	public void setToTop(boolean toTop) {
		this.toTop = toTop;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlTransient
	protected ChildAddMode mode=ChildAddMode.AUTO;
	
	public StructureNode(){
		
	}
	
	public HashSet<String> getRootMappings(String category,String engine) {
		HashSet<String>root=new HashSet<String>();
		if (category!=null){
		if ((id!=null&&category.equals(id.replace(' ', ' ')))&&(sourceId==null||sourceId.equals(engine))){
			root.add(category);
			//rec add child;
			recAddChild(root);
		}
		else{
			for (StructureNode n:inclusions){
				HashSet<String> rootMappings = n.getRootMappings(category, engine);
				root.addAll(rootMappings);
			}
		}
		}
		return root;
	}
	
	private void recAddChild(HashSet<String> root) {
		root.add(id);
		for (StructureNode n:inclusions){
			n.recAddChild(root);
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Entry{
		public Entry(){}
		
		public Entry(String id, String id2) {
			this.parentId=id.replace(' ', '_');
			this.childId=id2.replace(' ', '_');
		}
		protected String parentId;
		protected String childId;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((childId == null) ? 0 : childId.hashCode());
			result = prime * result
					+ ((parentId == null) ? 0 : parentId.hashCode());
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
			Entry other = (Entry) obj;
			if (childId == null) {
				if (other.childId != null)
					return false;
			} else if (!childId.equals(other.childId))
				return false;
			if (parentId == null) {
				if (other.parentId != null)
					return false;
			} else if (!parentId.equals(other.parentId))
				return false;
			return true;
		}
	}
	@XmlElement(name="exlusions")
	protected LinkedHashSet<Entry>exclusions=new LinkedHashSet<Entry>();
	@XmlElement(name="inclusions")
	protected LinkedHashSet<Entry>forceinclusions=new LinkedHashSet<Entry>();
	@XmlElement(name="children")
	protected ArrayList<StructureNode>inclusions=new ArrayList<StructureNode>();
	private String descritption;
	
	@XmlElement(name="blockedDocs")
	protected LinkedHashSet<String>blockedElemens=new LinkedHashSet<String>();

	public void blackList(String title) {
		blockedElemens.add(title);
	}
	public void unblackList(String title) {
		blockedElemens.remove(title);
	}
	
	public LinkedHashSet<String> getBlockedElemens() {
		return blockedElemens;
	}

	public void setBlockedElemens(LinkedHashSet<String> blockedElemens) {
		this.blockedElemens = blockedElemens;
	}

	public StructureNode(String engine, String name) {
		this.sourceId=engine;
		this.id=name;
	}

	public boolean isExcluded(String id2, String id22) {
		for (Entry e:exclusions){
			if (e.parentId.equals(id2)){
				if (e.childId.equals(id22)){
					return true;
				}
			}
		}
		return false;
	}
	public boolean isIncluded(String id2, String id22) {
		for (Entry e:forceinclusions){
			if (e.parentId.equals(id2)){
				if (e.childId.equals(id22)){
					return true;
				}
			}
		}
		return false;
	}


	public ArrayList<StructureNode> getElements() {
		return inclusions;
	}

	public String getDescription() {
		return descritption;
	}

	public String getTitle() {
		return id;
	}
	public HashSet<Entry>gatherExclusions() {
		HashSet<Entry>ee=new HashSet<Entry>();
		ee.addAll(exclusions);
		for (StructureNode n:inclusions){
			ee.addAll(n.gatherExclusions());
		}
		return ee;
	}
	public HashSet<Entry>gatherInclusions() {
		HashSet<Entry>ee=new HashSet<Entry>();
		ee.addAll(forceinclusions);
		for (StructureNode n:inclusions){
			ee.addAll(n.gatherInclusions());
		}
		return ee;
	}
}