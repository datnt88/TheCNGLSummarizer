package ie.dcu.cngl.summarizer.feature;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Filters out punctuation tokens from the TokenStream.
 * @author Shane  
 */
public class PunctuationTokenFilter extends TokenFilter {
	
	private CharTermAttribute termAttribute;

	public PunctuationTokenFilter(TokenStream input) {
		super(input);
		this.termAttribute = (CharTermAttribute) addAttribute(CharTermAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		while (input.incrementToken()) {
			String term = new String(termAttribute.buffer(), 0, termAttribute.length());
			if (StringUtils.isAlphanumeric(term)) {
				return true;
			}
		}
		return false;
	}
	
}
