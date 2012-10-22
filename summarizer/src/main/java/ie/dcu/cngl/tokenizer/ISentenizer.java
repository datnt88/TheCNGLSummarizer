package ie.dcu.cngl.tokenizer;

import java.util.ArrayList;

/**
 * Provides interface for separating content at sentence level
 * @author Shane
 *
 */
public interface ISentenizer {

	/**
	 * Tokenizes entire content and creates an array for each sentences' tokens.
	 * @param content Content to be tokenized
	 * @return 2-dimensional array of each sentence with each token.  
	 */	
    public ArrayList<ArrayList<TokenInfo>> sentenize(String content);
    
}
