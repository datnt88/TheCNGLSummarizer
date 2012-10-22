package ie.dcu.cngl.summarizer.feature;

import java.io.IOException;


import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Filters out numeric tokens from the TokenStream.
 * @author Sujit Pal
 */
public class NumericTokenFilter extends TokenFilter {

	private CharTermAttribute termAttribute;

	public NumericTokenFilter(TokenStream input) {
		super(input);
		this.termAttribute = (CharTermAttribute) addAttribute(CharTermAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		while (input.incrementToken()) {
			String term = new String(termAttribute.buffer(), 0, termAttribute.length());
			term = term.replaceAll(",", "");
			if (! NumberUtils.isNumber(term)) {
				return true;
			}
		}
		return false;
	}

}
