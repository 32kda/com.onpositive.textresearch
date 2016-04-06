package com.onpositive.semantic.wikipedia.abstracts;

public class Section extends CompositeTextElement implements ISimpleContentElement{

	protected String header;
	protected int level;
	
	public Section(String header, int level,TextAbstractElement parent) {
		super(parent);
		this.header =OnelineTextElement.innerClear(header);
		this.level = level;		
	}
	
	@Override
	public void printElement(TextAbstractsPrinter printer) {
		boolean hasValid=false;
		for(TextAbstractElement q:elements){
			if (q.valid){
				hasValid=true;
				break;
			}
		}
		if (!hasValid){
			return;
		}
		printer.println();
		String header2 = header;
		if (!header2.equals(":")){
			header2+=":";
		}
		printer.println(header2);
		//printer.println();
		super.printElement(printer);
	}
	

	@Override
	public int level() {
		return level;
	}
	
	@Override
	public String toString() {
		return header+":"+level;
	}
}
