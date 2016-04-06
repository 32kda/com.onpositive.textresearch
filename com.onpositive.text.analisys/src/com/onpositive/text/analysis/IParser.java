package com.onpositive.text.analysis;

import java.util.List;

import com.onpositive.text.analysis.utils.ILogger;

/**
 * Represents Generic Parser base structure
 */
public interface IParser {

	/**
	 * Process a list of tokens.
	 * This is main method of Parser.
	 *
	 * @param tokens Tokens list
	 * @return Processed result.
	 */
	List<IToken> process(List<IToken> tokens);

	/**
	 * Gets unparsed text.
	 * This is an input data for parser.
	 *
	 * @return the text
	 */
	String getText();

	/**
	 * Sets unparsed text.
	 * This is an input data for parser.
	 * 
	 * @param text the new text
	 */
	void setText(String text);

	/**
	 * Reset trigger.
	 * Trigger is a flag that shows if parser changed any tokens while processing.
	 */
	void resetTrigger();

	/**
	 * Checks if parser was triggered.
	 *
	 * @return true, if successful
	 */
	boolean hasTriggered();

	/**
	 * Checks if is recursive.
	 *
	 * @return true, if is recursive
	 */
	boolean isRecursive();

	/**
	 * Sets the handle bounds.
	 *
	 * @param b the new handle bounds
	 */
	
	void setHandleBounds(boolean b);

	/**
	 * Gets the new tokens.
	 *
	 * @return the new tokens
	 */
	List<IToken> getNewTokens();
	
	/**
	 * Sets the token id provider.
	 *
	 * @param tokenIdProvider the new token id provider
	 */
	void setTokenIdProvider(TokenIdProvider tokenIdProvider);

	/**
	 * Gets the token id provider.
	 *
	 * @return the token id provider
	 */
	TokenIdProvider getTokenIdProvider();
	
	/**
	 * Gets the base tokens.
	 *
	 * @return the base tokens
	 */
	List<IToken> getBaseTokens();
	
	/**
	 * Sets the base tokens.
	 *
	 * @param baseTokens the new base tokens
	 */
	void setBaseTokens(List<IToken> baseTokens);

	/**
	 * Clean - empty different structures, arrays and lists used by parsing process.  
	 */
	void clean();
	
	void setLogger(ILogger logger);
	
	void setErrorLogger(ILogger logger);

	/**
	 * Token Id Provider
	 */
	public class TokenIdProvider{
		
		private int currentTokenId;
		private boolean blocked;

		/**
		 * Process Token list to determine first vacant Token Id unless blocked.
		 *
		 * @param tokens Token list
		 */
		public void prepare(List<IToken> tokens){
			if(blocked){
				return;
			}
			currentTokenId = 0;
			for(IToken token : tokens){
				currentTokenId = Math.max(currentTokenId, token.id());
			}
		}

		/**
		 * Gets the vacant id.
		 *
		 * @return the vacant id
		 */
		public int getVacantId() {
			return TokenRegistry.getVacantId();
			// return ++currentTokenId;
		}

		/**
		 * Put Id Provider into blocked state.
		 * Blocked Id provider doesn't check a list of tokens for vacant Id. 
		 * This functionality is used if one instance of Id provider is used in all parsers.
		 */
		public void block() {
			this.blocked = true;
		}
		
		/**
		 * Put Id Provider out of blocked state.
		 * Blocked Id provider doesn't check a list of tokens for vacant Id. 
		 * This functionality is used if one instance of Id provider is used in all parsers.
		 */		
		public void unblock() {
			this.blocked = false;
		}
	}

}
