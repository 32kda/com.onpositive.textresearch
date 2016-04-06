package com.onpositive.text.analysis.lexic;

import java.util.Stack;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.words3.MetaLayer;
import com.onpositive.text.analysis.BasicParser;
import com.onpositive.text.analysis.IToken;

public class NumericsParser extends BasicParser {

	AbstractWordNet net;
	private MetaLayer<Object> layer;
	private MetaLayer<Object> scaleLayer;

	public NumericsParser(AbstractWordNet net) {
		super();
		this.net = net;
		layer = net.getMetaLayers().getLayer("numbers");
		scaleLayer = net.getMetaLayers().getLayer("numbers.scale");
	}

	@Override
	protected void combineTokens(Stack<IToken> sample, ProcessingData processingData) {
		boolean allScalar=true;
		for(IToken t : sample){
			allScalar &= (t.getType() == IToken.TOKEN_TYPE_SCALAR);
			if(!allScalar){
				break;
			}
		}
		if(allScalar){
			return;
		}
		double value = 0;
		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;
		int lastEnd = Integer.MIN_VALUE;
		WordFormToken lastWordFormToken = null;
		boolean lastScalar = false;
		for (IToken q : sample) {
			lastEnd = end;
			start = Math.min(start, q.getStartPosition());
			end = Math.max(end, q.getEndPosition());
			if (q instanceof ScalarToken) {
				if (!lastScalar) {
					ScalarToken tk = (ScalarToken) q;
					if (value!=0){
						int m1 = (int) value;
						int m2 = (int) tk.getValue();
						if (value == 0 || differentOrder(m1, m2)) {
							value += tk.getValue();
						} else {
							processingData.addReliableToken(new ScalarToken(value,lastWordFormToken, null,
									start, lastEnd));
							start = tk.getStartPosition();
							value = tk.getValue();
						}
					}
					else{
					value = tk.getValue();
					}
					lastScalar = true;
				} else {
					value = 0;
					start = Integer.MAX_VALUE;
					end = Integer.MIN_VALUE;
				}
				continue;
			}
			if (q instanceof WordFormToken) {				
				WordFormToken tk = (WordFormToken) q;
				lastWordFormToken = tk;
				MeaningElement[] meaningElement = tk.getMeaningElements();
				if (true) {
					Double value2 = (Double) layer.getFirstValue(meaningElement);
					if (value2 == null) {
						if (end > 0) {
							processingData.addReliableToken(new ScalarToken(value,lastWordFormToken, null, start,
									lastEnd));
							value = 0;
							start = Integer.MAX_VALUE;
							end = Integer.MIN_VALUE;
						}
						lastScalar = false;
						continue;
					}
					Object scale = scaleLayer.getFirstValue(meaningElement);
					if (scale != null && scale.equals(true)) {
						value *= value2;
						lastScalar=false;
					} else {
						if (lastScalar) {
							
							processingData.addReliableToken(new ScalarToken(value, lastWordFormToken, null,
									start, lastEnd));
							value = value2;
							start =tk.getStartPosition();
							end = tk.getEndPosition();
							lastScalar=false;
						} else {
							int m1 = (int) value;
							int m2 = (int) value2.doubleValue();
							if (value == 0 || differentOrder(m1, m2)) {
								value += value2;
							} else {
								processingData.addReliableToken(new ScalarToken(value, lastWordFormToken, null,
										start, lastEnd));
								start = tk.getStartPosition();
								value = value2;
							}
						}
					}
				}
				
			} else {
				if (end > 0) {
					processingData.addReliableToken(new ScalarToken(value, lastWordFormToken, null, start,
							lastEnd));
					value = 0;
					start = Integer.MAX_VALUE;
					end = Integer.MIN_VALUE;
				}
				//end = lastEnd;
				//break;
			}
		}
		if (end != Integer.MIN_VALUE) {
			processingData.addReliableToken(new ScalarToken(value, lastWordFormToken, null, start, end));
		}
	}

	boolean differentOrder(int m1, int m2) {
		return (""+m1).length()>(""+m2).length();
	}

	@Override
	protected ProcessingResult checkToken(IToken newToken) {
		if (newToken instanceof ScalarToken) {
			return CONTINUE_PUSH;
		}
		return isNumeral(newToken);
	}

	public ProcessingResult isNumeral(IToken newToken) {
		if (newToken instanceof WordFormToken) {
			WordFormToken tk = (WordFormToken) newToken;
			if (layer!=null&&layer.hasValue(tk.getMeaningElements())) {
				return CONTINUE_PUSH;
			}
		}
		return DO_NOT_ACCEPT_AND_BREAK;
	}
	
	@Override
	protected boolean keepInputToken() {
		return false;
	}
}
