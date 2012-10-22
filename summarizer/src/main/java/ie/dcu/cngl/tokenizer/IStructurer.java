package ie.dcu.cngl.tokenizer;

/**
 * Provides interface for structuring content at token, sentence and paragraph level
 * @author Shane
 *
 */
public interface IStructurer {
	
	/**
	 * Tokenizes entire content and creates an array for each paragraphs'
	 * sentences, and each sentences' tokens.
	 * @param content Content to be structures
	 * @return PageStructure containing each paragraph, with each sentence, with each token.  
	 */	
	public PageStructure getStructure(String content);
	
}
