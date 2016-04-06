package com.onpositive.text.analysis.rules;

import java.util.ArrayList;
import java.util.List;

import com.onpositive.text.analysis.IToken;

public class RuleExecutionEngine {
	
	private List<TokenRule> rules = new ArrayList<TokenRule>();;
	
//	private ArrayList<ConditionRule> unaryRules = new ArrayList<ConditionRule>();
//	
//	private ArrayList<ConditionRule> binaryRules = new ArrayList<ConditionRule>();
//	
//	private ArrayList<ConditionRule> ternaryRules = new ArrayList<ConditionRule>();
	
	public RuleExecutionEngine(Iterable<TokenRule> ruleCollection) {
		for(TokenRule rule : ruleCollection){
			rules.add(rule);//addRule(rule);
		}
	}
	
	public RuleExecutionEngine(TokenRule[] ruleCollection) {
		for(TokenRule rule : ruleCollection){
			rules.add(rule);//addRule(rule);
		}
	}

//	private void addRule(ConditionRule rule) {
//		if(rule.getDimension()==1){
//			unaryRules.add(rule);
//		}
//		else if(rule.getDimension()==2){
//			binaryRules.add(rule);
//		}
//		else if(rule.getDimension()==3){
//			ternaryRules.add(rule);
//		}
//	}
	
	public void applyRules(List<IToken> tokenList){
		for(TokenRule rule : rules){
			rule.setTokens(tokenList);
		}
		int size = tokenList.size();
		for(int i = 0 ; i < size ; i++){
			for(TokenRule rule : rules){
				rule.execute(i);
			}
		}
	}
	
//	public void applyRules(List<IToken> tokenList){
//		
//		if(tokenList==null||tokenList.isEmpty()){
//			return;
//		}
//		
//		ArrayList<IToken> tokens = new ArrayList<IToken>(tokenList);
//		for(int i = 0 ; i < 2 ; i++){
//			tokens.add(null);
//		}
//		
//		int size = tokens.size();		
//		IToken token0 = tokens.get(0);
//		IToken token1 = tokens.get(1);
//		IToken token2 = null;
//		
//		for (int i = 0; i < size; i++) {
//			token2 = tokens.get(i + 2);
//			for (ConditionRule rule : unaryRules) {
//				executeUnaryRule(rule, token0);
//			}
//
//			for (ConditionRule rule : binaryRules) {
//				if(token1!=null||rule.acceptsNull()){
//					executeBinaryRule(rule, token0, token1);
//				}
//			}
//
//			for (ConditionRule rule : ternaryRules) {
//				if(token2!=null||rule.acceptsNull()){
//					executeTernaryRule(rule, token0, token1, token2);
//				}
//			}
//			token0 = token1;
//			token1 = token2;
//		}
//	}
	
//	@SuppressWarnings("unchecked")
//	private void executeUnaryRule(ConditionRule rule,IToken token){
//		UnaryCondition<?> unaryCondition = (UnaryCondition<?>)rule.getCondition();
//		Object result = unaryCondition.compute(token);
//		if(result!=null){
//			rule.getCallBack().execute(result);
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	private void executeBinaryRule(ConditionRule rule,IToken token0,IToken token1){
//		BinaryCondition<?> binaryCondition = (BinaryCondition<?>)rule.getCondition();
//		Object result = binaryCondition.compute(token0,token1);
//		if(result!=null){
//			rule.getCallBack().execute(result);
//		}
//	}
//	
//	@SuppressWarnings("unchecked")
//	private void executeTernaryRule(ConditionRule rule,IToken token0,IToken token1,IToken token2){
//		TernaryCondition<?> ternaryCondition = (TernaryCondition<?>)rule.getCondition();
//		Object result = ternaryCondition.compute(token0,token1,token2);
//		if(result!=null){
//			rule.getCallBack().execute(result);
//		}
//	}
	
//	private final List<ConditionRule[]> rules;
//	
//	private final int[] dimensions;
//	
//	public CompositeRule(Iterable<ConditionRule> ruleCollection) {
//		super();
//		
//		HashMap<Integer,ArrayList<ConditionRule>> map = new HashMap<Integer, ArrayList<ConditionRule>>() ;
//		for(ConditionRule rule : ruleCollection){
//			
//			int dim = rule.getDimension();
//			ArrayList<ConditionRule> list = map.get(dim);
//			if(list==null){
//				list = new ArrayList<ConditionRule>();
//				map.put(dim, list);
//			}
//			list.add(rule);			
//		}
//		ArrayList<Entry<Integer, ArrayList<ConditionRule>>> entryList
//				= new ArrayList<Map.Entry<Integer,ArrayList<ConditionRule>>>(map.entrySet());
//		
//		Collections.sort(entryList, new Comparator<Map.Entry<Integer,ArrayList<ConditionRule>>>() {
//
//			@Override
//			public int compare(
//					Entry<Integer, ArrayList<ConditionRule>> o1,
//					Entry<Integer, ArrayList<ConditionRule>> o2) {
//				
//				return o1.getKey() - o2.getKey();
//			}
//		});
//		dimensions = new int[entryList.size()];
//		int ind = 0 ;
//		rules = new ArrayList<ConditionRule[]>(entryList.size()); 
//		for(Map.Entry<Integer, ArrayList<ConditionRule>> entry : entryList){
//			dimensions[ind++] = entry.getKey();
//			ArrayList<ConditionRule> value = entry.getValue();
//			rules.add(value.toArray(new ConditionRule[value.size()]));
//		}
//	}

}
