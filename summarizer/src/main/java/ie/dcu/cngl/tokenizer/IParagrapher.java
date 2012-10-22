package ie.dcu.cngl.tokenizer;

import java.util.ArrayList;

/**
 * Provides interface for separating content at paragraph level
 * @author Shane
 *
 */
public interface IParagrapher {

	/**
	 * Tokenizes entire content and creates an array for each paragraphs' tokens.
	 * @param content Content to be tokenized
	 * @return 2-dimensional array of each paragraph, with each token.  
	 */
	public ArrayList<ArrayList<TokenInfo>> paragraph(String content);
	
}
