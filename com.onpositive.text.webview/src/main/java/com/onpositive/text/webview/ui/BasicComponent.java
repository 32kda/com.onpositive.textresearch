package com.onpositive.text.webview.ui;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicComponent implements IComponent{

	
	protected ArrayList<IComponent>children=new ArrayList<>();
	private String _tag;
	private String text="";
	private HashMap<String,String>attrs=new HashMap<>();
	
	public String tag(){
		return _tag;
	}
	public void attr(String name,String val){
		this.attrs.put(name, val);
	}
	
	public BasicComponent(String _tag) {
		super();
		this._tag = _tag;
	}

	public void add(IComponent ch){
		this.children.add(ch);
	}
	public void remove(IComponent ch){
		this.children.remove(ch);
	}
	
	public String render(){
		StringBuilder bld=new StringBuilder();
		bld.append("<"+tag()+" ");
		this.attrs.keySet().forEach(x->{
			bld.append(x);
			bld.append("=");
			bld.append("'");
			bld.append(this.attrs.get(x));
			bld.append("'");
			bld.append(' ');
		});
		bld.append(">");
		bld.append(text);
		children.forEach(x->bld.append(x.render()));
		bld.append("</"+tag()+">");
		return bld.toString();
	}
	BasicComponent text(String txt){
		this.text=txt;
		return this;
	}
	
	public BasicComponent tag(String tag,String text){
		return new BasicComponent(tag).text(text);
	}
	protected BasicComponent h2(String text){
		return tag("h2",text);
	}
	protected BasicComponent h1(String text){
		return tag("h1",text);
	}
	protected BasicComponent h3(String text){
		return tag("h3",text);
	}
	protected BasicComponent h4(String text){
		return tag("h4",text);
	}
	protected BasicComponent span(String text){
		return tag("span",text);
	}
	protected BasicComponent a(String text,String ref){
		BasicComponent tag = tag("a",text);
		tag.attr("href", ref);
		return tag;
	}
	protected BasicComponent li(String text){
		return tag("li",text);
	}
}
