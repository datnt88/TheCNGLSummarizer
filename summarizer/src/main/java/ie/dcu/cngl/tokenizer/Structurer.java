package ie.dcu.cngl.tokenizer;

import java.util.ArrayList;

/**
 * A Structurer uses a Paragrapher and Sentenizer
 * to provide 3-dimensional content structure.
 * @author Shane
 *
 */
public class Structurer implements IStructurer {

	private ISentenizer sentenizer;
	private IParagrapher paragrapher;

	/**
	 * Creates a new Struturer.
	 */
	public Structurer() {
		this.sentenizer = Sentenizer.getInstance();
		this.paragrapher = Paragrapher.getInstance();
	}
	
	/**
	 * Gets content structure. A vector of paragraphs, containing a vector
	 * of sentences, containing a vector of tokens.
	 * @param content
	 * @return PageStructure of provided String
	 */
	public PageStructure getStructure(String content) {
		ArrayList<ArrayList<ArrayList<TokenInfo>>> structure = new ArrayList<ArrayList<ArrayList<TokenInfo>>>();
		ArrayList<String> paragraphs = TokenizerUtils.recombineTokens2d(paragrapher.paragraph(content));
		for(String paragraph : paragraphs) {
			ArrayList<ArrayList<TokenInfo>> sentences = sentenizer.sentenize(paragraph);
			structure.add(sentences);
		}
		return new PageStructure(structure);
	}
}
