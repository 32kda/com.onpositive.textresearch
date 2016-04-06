package com.onpositive.semantic.words2.builder;



public class TempWordInfo {

	protected WordFormTemplate template;
	
	protected String foundation;
	protected String foundation1;
	protected String foundation2;
	protected boolean pt;
	protected boolean st;

	public String getFoundation(int number) {
		if (number==0){
			return foundation;
		}
		if (number==1){
			return foundation1;
		}
		if (number==2){
			return foundation2;
		}
		return null;
	}

	public void setPluralTantum(boolean pt) {
		this.pt=pt;
	}
	public void setSingularTantum(boolean st) {
		this.st=st;
	}

	public void registerFoundation(int number, String foundation) {
		if (foundation!=null&&foundation.length()>0&&!foundation.equals("-")){
			if (number==0){
				if (this.foundation==null){
				this.foundation=foundation;
				}
			}
			if (number==1){
				if (this.foundation1==null){
				this.foundation1=foundation;
				}
			}
			if (number==2){
				if (this.foundation2==null){
				this.foundation2=foundation;
				}
			}
		}
	}
	public void setTemplate(WordFormTemplate findTemplate) {
		this.template=findTemplate;
	}

}
