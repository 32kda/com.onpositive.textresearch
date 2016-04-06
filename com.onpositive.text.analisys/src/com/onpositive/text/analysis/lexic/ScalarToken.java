package com.onpositive.text.analysis.lexic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;
import com.onpositive.text.analysis.syntax.SyntaxToken;

public class ScalarToken extends SyntaxToken {
	
	private static final List<GrammemSet> scalarGrammems = new ArrayList<GrammemSet>();
	{
		List<Grammem> grammems = new ArrayList<Grammem>();
		grammems.add(PartOfSpeech.NUMR);
		grammems.addAll(uniformGrammems.get(0).grammems());
		scalarGrammems.add(new GrammemSet(grammems));
	}
	
	public ScalarToken(double value, SyntaxToken mainGroup, Collection<GrammemSet> grammemSets, int startPosition, int endPosition) {
		super(TOKEN_TYPE_SCALAR, mainGroup,  grammemSets, startPosition, endPosition);
		if (mainGroup!=null&&mainGroup.getBasicForm().equals("иду")){
			System.out.println("a");
		}
		this.value1 = value;
		this.value2 = Integer.MIN_VALUE;
		this.isFracture = false;
		this.isDecimal = false;
	}
	
	public ScalarToken(int value1, int value2, boolean isDecimal, SyntaxToken mainGroup, Collection<GrammemSet> grammemSets, int startPosition, int endPosition) {
		super(TOKEN_TYPE_SCALAR, mainGroup,  grammemSets, startPosition, endPosition);
		this.value1 = value1;
		this.value2 = value2;
		this.isFracture = true;
		this.isDecimal = isDecimal;
	}
	private final boolean isFracture;
	
	private final boolean isDecimal;

	private final double value1;
	
	private final double value2;
	
	@Override
	public List<GrammemSet> getGrammemSets() {
		if(this.mainGroup!=null){
			return super.getGrammemSets();
		}
		else{
			return scalarGrammems;
		}
	}

	@Override
	public String getStringValue() {
		
		if(isFracture()){
			if(isDecimal){
				return "" + (int)value1 + "." + (int)value2;
			}
			else{
				return "" + (int)value1 + "/" + (int)value2;
			}
		}
		else{
			return "" + value1;			
		}		
	}

	public double getValue1() {
		return value1;
	}

	public double getValue2() {
		return value2;
	}

	public boolean isFracture() {
		return isFracture;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isDecimal ? 1231 : 1237);
		result = prime * result + (isFracture ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(value1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(value2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScalarToken other = (ScalarToken) obj;
		if (isDecimal != other.isDecimal)
			return false;
		if (isFracture != other.isFracture)
			return false;
		if (Double.doubleToLongBits(value1) != Double
				.doubleToLongBits(other.value1))
			return false;
		if (Double.doubleToLongBits(value2) != Double
				.doubleToLongBits(other.value2))
			return false;
		return true;
	}

	public double getValue() {
		if (!isFracture){
			return value1;
		}
		if (!isDecimal){
			return value1/value2;
		}
		return Double.parseDouble((""+value1).replace((CharSequence)".0", "")+"."+(""+value2).replace((CharSequence)".", ""));
	}

	public boolean isDecimal() {
		return isDecimal;
	}

}
