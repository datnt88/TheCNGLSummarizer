package ie.dcu.cngl.tokenizer;

import java.util.ArrayList;

/**
 * Provides interface for tokenizing content
 * @author Shane
 *
 */
public interface ITokenizer {
	
	/**
	 * Tokenizes entire content.
	 * @param content Content to be tokenized
	 * @return Array of tokens.  
	 */
	public ArrayList<TokenInfo> tokenize(String content);
	
}
