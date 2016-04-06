package com.onpositive.text.analysis;

import com.onpositive.text.analysis.IToken.Direction;

public abstract class TokenVisitor {
	
	private boolean leafToRoot = true;
	
	public TokenVisitor(boolean leafToRoot) {
		this.leafToRoot = leafToRoot;
	}
	
	public TokenVisitor() {}

	protected static final int CONTINUE = 0;
	
	protected static final int STOP = 0;
	
	protected static final int STOP_PARENT_BRANCH = 0;
	
	
	public void visit(IToken token){		
		visitRecursively(token,null, Direction.START);
	}
	
	public void visit(IToken token, IToken parent, Direction dir){
		
		visitRecursively(token,parent,dir);
		
	}

	private int visitRecursively(IToken token, IToken parent, Direction dir) {
		
		if(!leafToRoot){
			int code = inspectNode(token,parent);
			if(code!=CONTINUE){
				return code;
			}
		}
		
		if(needVisitChildren(token,parent))
		{		
			int childrenCount = token.childrenCount();
			for(int i = 0 ; i < childrenCount ; i++){
				
				IToken child = token.getChild(i, dir);
				int result = visitRecursively(child, token, dir);
				if(result != CONTINUE){
					if(result==STOP_PARENT_BRANCH){
						return CONTINUE;
					}
					else if(result == STOP){
						return STOP;
					}
				}
			}
		}
		
		if(leafToRoot){
			return inspectNode(token,parent);
		}
		else{
			return CONTINUE;
		}
		
	}

	protected abstract boolean needVisitChildren(IToken token, IToken parent);

	protected abstract int inspectNode(IToken token, IToken parent);

}
