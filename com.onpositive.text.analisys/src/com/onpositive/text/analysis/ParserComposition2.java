package com.onpositive.text.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.onpositive.text.analysis.IToken.Direction;
import com.onpositive.text.analysis.syntax.SyntaxToken;
import com.onpositive.text.analysis.utils.DummyLogger;
import com.onpositive.text.analysis.utils.ILogger;

public class ParserComposition2 extends ParserComposition {
	
	public ParserComposition2(boolean isGloballyRecursive, IParser... parsers) {
		super(isGloballyRecursive,parsers);
		setHandleBoundsFalse();
		initRegistry();
	}
	
	public ParserComposition2(IParser... parsers) {
		super(parsers);
		setHandleBoundsFalse();
		initRegistry();
	}
	
	protected TokenModificationRegistry reg;


	protected void initRegistry() {
		reg = new TokenModificationRegistry(this.parsers);
		reg.setLogger(this.logger);
	}
	
	@Override
	public void setLogger(ILogger logger) {
		super.setLogger(logger);
		if(reg!= null){
			reg.setLogger(this.logger);
		}
	}
	
	
	@Override
	public void setErrorLogger(ILogger logger) {
		super.setLogger(logger);
		if(reg!= null){
			reg.setErrorLogger(this.logger);
		}
	}
	
	
	private void setHandleBoundsFalse() {
		for(IParser ap : this.parsers){
			ap.setHandleBounds(false);
		}
	}
	
	
	@Override
	public List<IToken> process(List<IToken> tokens) {
		
		List<IToken> result = reg.process(tokens);
		return result;
	}

	@Override
	public boolean hasTriggered() {
		return this.reg.hasTriggered();
	}
	
	protected static class ParserData{
		
		
		public ParserData(TokenModificationRegistry reg, IParser ap, int id) {
			super();
			this.reg = reg;
			this.parser = ap;
			this.parserId = id;
		}

		private final TokenModificationRegistry reg;
		
		private final IParser parser;
		
		private final int parserId;
		
		private List<IToken> result;

		private boolean isFinished = false;
		
		protected ILogger logger = new DummyLogger();

		private ILogger errorLogger = new DummyLogger();
		
		public List<IToken> process(List<IToken> tokens, IntOpenHashSet currenIdSet){			
			
			result = parser.process(tokens);
			dumpSuggestedTokens();
			processModificationData(currenIdSet);
			return result;
		}

		private void dumpSuggestedTokens() {
			logger.writelnString(getParserName());
			StringBuilder bld = new StringBuilder();
			List<IToken> newTokens = parser.getNewTokens();
			Collections.sort(newTokens, new Comparator<IToken>() {
				@Override
				public int compare(IToken o1, IToken o2) {
					List<IToken> children1 = o1.getChildren();
					List<IToken> children2 = o2.getChildren();
					
					if(children1==null){
						if(children2==null){
							return 0;
						}
						return -1;
					}
					else if(children2==null){
						return 1;
					}
					
					int size1 = children1.size();
					int size2 = children2.size();
					int size = Math.min(size1, size2);
					for(int i = 0 ; i < size;i++){
						IToken ch1 = children1.get(i);
						IToken ch2 = children2.get(i);
						int id1 = ch1.id();
						int id2 = ch2.id();
						if(id1!=id2){
							return id1-id2; 
						}
					}
					return size1-size2;
				}
			});
			for(IToken t : newTokens){
				bld.delete(0, bld.length());
				bld.append(t.id()).append("(");
				
				IToken maingGroup = null;
				if(t instanceof SyntaxToken){
					maingGroup = ((SyntaxToken)t).getMainGroup();
				}
				
				List<IToken> children = t.getChildren();
				if(children!=null){
					for(IToken ch : children){
						bld.append(ch.id());												
						if(ch==maingGroup){
							bld.append("(e)");
						}
						IntArrayList childrenIDs = collectChildrenIDs(ch);
						int size = childrenIDs.size();
						if(size>0){
							bld.append("(");
							bld.append(childrenIDs.get(0));
							for(int i = 1 ; i < size ; i++){
								bld.append(", ").append(childrenIDs.get(i));
							}
							bld.append(")");
						}
						bld.append(", ");
					}
					bld.delete(bld.length() - ", ".length(), bld.length() );
				}
				bld.append("): ");
				String str = bld.toString();
				this.logger.writeString(str).writelnToken(t);
			}			
		}

		private IntArrayList collectChildrenIDs(IToken ch) {
			IntArrayList result = new IntArrayList();
			List<IToken> children = ch.getChildren();
			if(children != null){
				for(IToken t : children){
					result.add(t.id());
					result.addAll(collectChildrenIDs(t));
				}
			}
			return result;
		}

		protected String getParserName() {
			return this.parser.getClass().getSimpleName();
		}
		
		public List<IToken> getNewTokens(){
			return parser.getNewTokens();
		}
		
		
		public void processModificationData(IntOpenHashSet currenIdSet) {
			
			List<IToken> newTokens = getNewTokens();
			
			for( IToken token : newTokens){
				
				IToken mainGroup = null;
				if(token instanceof SyntaxToken){
					mainGroup = ((SyntaxToken)token).getMainGroup();
				}
				List<IToken> children = token.getChildren();
				if (children!=null)
				for(IToken ch : children){
					if(!currenIdSet.contains(ch.id())){						
						continue;
					}
					int modType = ( mainGroup == null || ch !=mainGroup )
							? TokenModificationData.CONSUMED : TokenModificationData.ENRICHED;					
					
					List<TokenModificationData> list = collectModificationData(ch,token,modType, new ArrayList<TokenModificationData>(),currenIdSet);
					for(TokenModificationData md : list){
						reg.registerData(md.token.id(),md);
					}
				}
			}
			
		}

		private List<TokenModificationData> collectModificationData(IToken modified, IToken produced, int modType, List<TokenModificationData> list, IntOpenHashSet currenIdSet) {
			
			TokenModificationData md = new TokenModificationData(reg, modified, produced, parserId, modType);
			list.add(md);
			List<IToken> children = modified.getChildren();
			if(children!=null){
				for(IToken ch: children){
					if(!currenIdSet.contains(ch.id())){
						continue;
					}
					collectModificationData(ch,produced,modType, list, currenIdSet);
				}
			}			
			return list;
		}

		public List<IToken> getResult() {
			return result;
		}

		public void setResult(List<IToken> result) {
			this.result = result;
		}

		public boolean isFinished() {
			return isFinished ;
		}
		
		public void setFinished(boolean isFinished) {
			this.isFinished = isFinished;
		}

		public void setNeedRepeat(){
			this.isFinished = false;
		}
		
		@Override
		public String toString() {
			String name = getParserName();
			StringBuilder bld = new StringBuilder(name);
			if(isFinished){
				bld.append(", finished");
			}
			bld.append(", new tokens:\n");
			for(IToken t : parser.getNewTokens()){
				bld.append(t.toString()).append("\n");
			}
			return bld.toString();
		}

		public void clean() {
			this.parser.clean();
		}

		public void setLogger(ILogger logger) {
			this.logger = logger;
		}
		
		public void setErrorLogger(ILogger logger) {
			this.errorLogger = logger;
		}
	}
	
	
	protected static class TokenModificationData{
		
		public TokenModificationData(TokenModificationRegistry reg, IToken token, IToken producedToken, int parserId, int modType) {
			super();
			this.reg = reg;
			this.parserId = parserId;
			this.producedToken = producedToken;
			this.modType = modType;
			this.token = token;
		}
		
		
		private static int CONSUMED = 0;
		
		private static int ENRICHED = 1;
		
		private TokenModificationRegistry reg;
		
		private int parserId;
		
		private IToken token;
		
		private IToken producedToken;

		private int modType;
		
		private boolean isCanceled = false;

		public int getParserId() {
			return parserId;
		}

		public void setParserId(int parserId) {
			this.parserId = parserId;
		}

		public int getModType() {
			return modType;
		}

		public void setModType(int modType) {
			this.modType = modType;
		}

		public IToken getProducedToken() {
			return producedToken;
		}

		public boolean isCanceled() {
			return isCanceled;
		}

		public void setCanceled(boolean isCanceled, int validParserId) {
			this.isCanceled = isCanceled;
			if(isCanceled && validParserId != parserId){
				reg.enableParserRepeat(parserId);
				reg.cancelProducedToken(producedToken.id());
			}
		}

		public boolean isEnriched() {
			return getModType() == ENRICHED;
		}
		
		@Override
		public String toString() {
			return token.toString() + " -> " + producedToken.toString();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TokenModificationData)) return false;
			TokenModificationData tmd = (TokenModificationData) obj;
			
			if (parserId != tmd.getParserId()) return false;
			
			if (producedToken.getType() != tmd.getProducedToken().getType()) return false;
			if (!producedToken.getChildren().equals(tmd.getProducedToken().getChildren())) return false;
			
			return true;
		}
	}
	
	protected static class TokenModificationRegistry{
		
		private ArrayList<ParserData> parserDataList = new ArrayList<ParserData>();
		
		private IntObjectOpenHashMap<List<TokenModificationData>> map
			= new IntObjectOpenHashMap<List<TokenModificationData>>();
		private IntObjectOpenHashMap<List<TokenModificationData>> bmap
		= new IntObjectOpenHashMap<List<TokenModificationData>>();
		
		private boolean hasTriggered = false;
		
		private List<IToken> original;
		
		private List<IToken> currentTokensArray;
		
		private IntOpenHashSet cancelledProducedTokens = new IntOpenHashSet();
		
		private HashSet<IToken> modifiedTokens = new HashSet<IToken>();

		private ILogger logger = new DummyLogger();
		
		private ILogger errorLogger = new DummyLogger();
		
		private IntObjectOpenHashMap<IToken> newTokens = new IntObjectOpenHashMap<IToken>();

		
		public void cancelProducedToken(int id){
			cancelledProducedTokens.add(id);
		}

		public TokenModificationRegistry(IParser[] parsers) {
			

			for(int i = 0 ; i < parsers.length ; i++){
				ParserData pd = new ParserData(this,parsers[i],i);
				parserDataList.add(pd);
			}
		}

		public void enableParserRepeat(int parserId) {
			
			int size = parserDataList.size();
			if(parserId<0||parserId>size-1){
				return;
			}
			parserDataList.get(parserId).setNeedRepeat();
			
		}

		public List<IToken> process(List<IToken> tokens) {
			
			dumpInputTokens(tokens);
			prepare(tokens);
			
			this.original = tokens;
			this.currentTokensArray = this.original;
			
			int count = 0;
			while(true){
				if(count>100){
					throw new RuntimeException("Infinite cycle in Verb Composite Parser.");
				}
				this.clean();
				this.logger.newLine().writelnString("Iteration " + count);
				
				IntOpenHashSet currenIdSet = getCurrenIdSet();

				for( ParserData pd : parserDataList){
					
//					if(pd.isFinished()){
//						continue;
//					}
					pd.process(this.currentTokensArray,currenIdSet);
					pd.setFinished(true);
				}
								
				resolveConflicts(map.equals(bmap));					
				
				this.currentTokensArray = collectTokens();
				dumpNewTokens();
				boolean finished = finished();
				if(finished){
					break;
				}				
				boolean gotNewTokens = !newTokens.isEmpty();
				this.hasTriggered |= gotNewTokens;
				if(!gotNewTokens){
					break;
				}
				count++;
			}
			return this.currentTokensArray;
		}

		private IntOpenHashSet getCurrenIdSet() {
			IntOpenHashSet result = new IntOpenHashSet();
			for(IToken t : this.currentTokensArray){
				result.add(t.id());
			}
			return result;
		}

		private void dumpNewTokens() {
			
			logger.newLine().writelnString("Obtained tokens array:");
			StringBuilder bld = new StringBuilder();
			for(ObjectCursor<IToken> cur : newTokens.values()){
				IToken t = cur.value;
				bld.delete(0, bld.length());
				bld.append(t.id()).append("(");
				
				List<IToken> children = t.getChildren();
				if(children!=null){
					for(IToken ch : children){
						bld.append(ch.id()).append(", ");
					}
					bld.delete(bld.length() - ", ".length(), bld.length() );
				}
				bld.append("): ");
				String str = bld.toString();
				this.logger.writeString(str).writelnToken(t);
			}			
		}

		private void dumpInputTokens(List<IToken> tokens) {
			
			for(IToken t : tokens){
				this.logger.writeString("" + t.id() + ": ").writelnToken(t);
			}
			
		}

		private List<IToken> collectTokens() {
			
			IntOpenHashSet ignoreSet = new IntOpenHashSet();
			IntObjectOpenHashMap<IToken> resultTokensMap = new IntObjectOpenHashMap<IToken>();
			
			for(ParserData pd : parserDataList){
				List<IToken> newTokensList = pd.getNewTokens();
				for(IToken token : newTokensList){
					int id = token.id();
					if(cancelledProducedTokens.contains(id)){
						ignoreSet.add(id);
						continue;
					}
					else{
						newTokens.put(id,token);
						//if(!token.isDoubtful()){
							List<IToken> children = token.getChildren();
							if (children!=null){
							modifiedTokens.addAll(children);
							}
						//}
					}
				}
			}
			
			markPreservedTokens(newTokens,resultTokensMap);
			
			ArrayList<IToken> result = new ArrayList<IToken>();
			result.addAll(Arrays.asList(resultTokensMap.values().toArray(IToken.class)));
			result.addAll(Arrays.asList(newTokens.values().toArray(IToken.class)));
			Collections.sort(result, new Comparator<IToken>() {

				@Override
				public int compare(IToken o1, IToken o2) {
					return o1.getStartPosition()-o2.getStartPosition();
				}
			});
			
			resultTokensMap.putAll(newTokens);
			
			TokenBoundsHandler tbh = new TokenBoundsHandler();
			tbh.setNewTokens(newTokens);
			tbh.setResultTokens(resultTokensMap);
			tbh.handleBounds(result,true);
			
			TokenBoundsHandler.discardTokens(new ArrayList<IToken>(modifiedTokens));			
			return result;
		}

		protected void markPreservedTokens(	IntObjectOpenHashMap<IToken> newTokensMap, IntObjectOpenHashMap<IToken> resultTokensMap) {
			for(IToken t : currentTokensArray){
				if(!modifiedTokens.contains(t)){
					resultTokensMap.put(t.id(), t);
				}
			}
			
			int size = 0;
			while(size != resultTokensMap.size()){
				size = resultTokensMap.size();
				for(IToken newToken : newTokensMap.values().toArray(IToken.class))
				{
					List<IToken> children = newToken.getChildren();
					if (children!=null){
					int childrenCount = children.size();
					if(childrenCount==1){
						continue;
					}
					
					IToken first = children.get(0);
					IToken last = children.get(childrenCount-1);
					checkNeighbours(resultTokensMap, first, Direction.END);
					for(int i = 1; i < childrenCount-1 ; i++ ){
						IToken ch = children.get(i);
						checkNeighbours(resultTokensMap, ch,Direction.END);
						checkNeighbours(resultTokensMap, ch,Direction.START);
					}
					checkNeighbours(resultTokensMap, last, Direction.START);
					}
				}
			}
		}

		private void checkNeighbours(IntObjectOpenHashMap<IToken> resultTokensMap, IToken token, Direction dir) {
			
			IToken neighbour = token.getNeighbour(dir);
			if(neighbour!=null){
				if(!modifiedTokens.contains(neighbour)){
					resultTokensMap.put(token.id(), token);
					modifiedTokens.remove(token);
				}
			}
			else{
				List<IToken> neighbours = token.getNeighbours(dir);
				if(neighbours!=null){
					for(IToken n : neighbours){
						if(!modifiedTokens.contains(n)){
							resultTokensMap.put(token.id(), token);
							modifiedTokens.remove(token);
							break;
						}						
					}
				}
			}
			
		}

		private void prepare(List<IToken> tokens) {
			
			clean();
			this.hasTriggered = false;
			for(ParserData pd : this.parserDataList){
				pd.setFinished(false);
			}
		}

		protected void clean() {

			this.bmap.clear();
			
			IntObjectOpenHashMap<List<TokenModificationData>> temp = this.bmap;
			this.bmap = this.map;
			this.map = temp;

			this.cancelledProducedTokens.clear();
			this.modifiedTokens.clear();
			this.newTokens.clear();
			for( ParserData pd : this.parserDataList){
				pd.clean();
			}
		}
		
		protected void resolveConflicts(boolean force) {
			processedTokens.clear();
			
			for(IToken t : currentTokensArray){
				
				List<TokenModificationData> dataList = map.get(t.id());
				if(dataList==null||dataList.size()<2){
					continue;
				}
				resolveConflict(dataList, force);
			}
		}

		private Set<Integer> processedTokens = new HashSet<Integer>();
		
		protected void resolveConflict(List<TokenModificationData> dataList, boolean force) {
			
			
			
			List<TokenModificationData> enriched = new ArrayList<TokenModificationData>();
			for(TokenModificationData tmd : dataList) {
				int pId = tmd.getProducedToken().id();
			
				if (force && processedTokens.contains(pId)) return;
				else processedTokens.add(pId);
				
				
				if(tmd.isEnriched()){
					enriched.add(tmd);
				}
			}
			if(enriched.size()==0){
				resolveConflictPrecisely(dataList);
			}
			else{
				for(TokenModificationData tmd : dataList){
					if(!tmd.isEnriched()){
						tmd.setCanceled(true,-1);
					}
				}
				if(enriched.size()>1){
					resolveConflictPrecisely(enriched);
				}
			}
			
		}

		protected void resolveConflictPrecisely(List<TokenModificationData> dataList) {
			int validParserId = dataList.get(0).getParserId();
			for(int i = 2 ; i < dataList.size() ; i++){
				dataList.get(i).setCanceled(true, validParserId);
			}
		}

		protected boolean finished() {
			
			boolean done = true;
			for(ParserData pd : parserDataList){
				
				if(!pd.isFinished()){
					done = false;
					break;
				}
			}
			return done;
		}

		public void registerData(int id, TokenModificationData md) {
			
			List<TokenModificationData> list = map.get(id);
			if(list==null){
				list = new ArrayList<TokenModificationData>();
				map.put(id,list);				
			}
			list.add(md);
			
		}
		
		public void setLogger(ILogger logger) {
			this.logger = logger;
			for(ParserData pd : parserDataList){
				pd.setLogger(this.logger);
			}
		}
		
		public void setErrorLogger(ILogger logger) {
			this.errorLogger = logger;
			for(ParserData pd : parserDataList){
				pd.setErrorLogger(this.logger);
			}
		}
		
		

		public boolean hasTriggered() {
			return hasTriggered;
		}
	}

}
