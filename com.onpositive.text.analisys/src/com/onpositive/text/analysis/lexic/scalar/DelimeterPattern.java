package com.onpositive.text.analysis.lexic.scalar;

class DelimeterPattern{
	
	public DelimeterPattern(
			String decimalDelimeter,
			String fractureDelimeter,
			String valueDelimeter, int weight) {
		super();
		this.decimalDelimeter = decimalDelimeter;
		this.fractureDelimeter = fractureDelimeter;
		this.valueDelimeter = valueDelimeter;
		this.weight = weight;
	}
	
	int rank = 0 ;
	
	final String decimalDelimeter;
	
	final String fractureDelimeter;
	
	final String valueDelimeter;	
	
	final int weight;


	public String getDecimalDelimeter() {
		return decimalDelimeter;
	}

	public String getFractureDelimeter() {
		return fractureDelimeter;
	}

	public String getValueDelimeter() {
		return valueDelimeter;
	}
	
	protected void cancel(){
		this.rank = Math.max(0, this.rank);
		this.rank++;
	}
	
	protected void vote(){
		if(this.rank>0){
			return;
		}
		this.rank--;
	}
	
	protected void reset(){
		this.rank = 0;
	}
	
	protected int getRank(){
		return this.rank;
	}
}