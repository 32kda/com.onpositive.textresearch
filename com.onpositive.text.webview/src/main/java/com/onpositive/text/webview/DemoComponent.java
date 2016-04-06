package com.onpositive.text.webview;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.onpositive.text.webview.ui.FreeMarkerComponent;
import com.onpositive.text.webview.ui.annotations.Option;
import com.onpositive.text.webview.ui.annotations.View;

@View("demo.html")
public abstract class DemoComponent extends FreeMarkerComponent{

	private boolean active;
	private String _text;
	private String _globalPath;
	
	public DemoComponent() {
		this._text=Context.getContext().text;
		this._globalPath=Context.getContext().globalPath;
		this.parseOptions(Context.getContext().parameters);
		if (this._text==null){
			this._text="";
		}
	}

	private void parseOptions(HashMap<String, String> parameters) {
		for (Field f: this.getClass().getDeclaredFields()){
			Option o=f.getAnnotation(Option.class);
			if (o!=null){
				Object val=parameters.get(f.getName());
				if (f.getType()==Boolean.class){
					val=Boolean.parseBoolean(""+val);
				}
				try{
				f.set(this, val);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public String getText() {
		return _text;
	}
	public void setText(String text) {
		this._text = text;
	}
	public String getGlobalPath() {
		return _globalPath;
	}
	public abstract String getDescription();
	public abstract String getTitle();
	public abstract String getId();
	public DemoComponent[] getSubmenu(){return null;};
	
	public boolean isActive(){
		return this.active;
	}
	
	public abstract String getOutput();
	
	public void setActive(boolean active){
		this.active=active;
	}
	
	public String getStyleClass(){
		if (this.active){
		return "active";
		}
		return "";
	}
}
