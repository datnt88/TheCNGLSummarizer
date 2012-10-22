package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

/**
 * Scores sentences with too many terms negatively.
 * @author Shane
 *
 */
public class LongSentenceFeature extends Feature {
	
	private int maxSentenceTerms;

	/**
	 * Creates short sentence feature with default minimum sentence
	 * length of 5.
	 * @throws IOException
	 */
	public LongSentenceFeature() throws IOException {
		this.maxSentenceTerms = SummarizerUtils.maxSentenceLength == -1 ? 1000 : SummarizerUtils.maxSentenceLength;
	}
	
	/**
	 * Sets the minimum number of sentence terms. If the number of terms 
	 * in a sentence is below this figure it is weighted negatively.
	 * @param minimum The minimum number of terms.
	 */
	public void setMinimumSentenceTerms(int minimum) {
		this.maxSentenceTerms = minimum;
	}

	@Override
	public Double[] getWeights() {
		final int numSentences = structure.getNumSentences();
		Double[] weights = new Double[numSentences];
		
		ArrayList<TokenInfo> tokens;
		for(int i = 0; i < numSentences; i++) {
			tokens = structure.getSentenceTokens(i);
			tokens = filterPunctuation(tokens);
			weights[i] = tokens.size() > this.maxSentenceTerms ? -1.0 : 0.0;
		}
		
		return weights;
	}

	private ArrayList<TokenInfo> filterPunctuation(ArrayList<TokenInfo> tokens) {
		ArrayList<TokenInfo> alphaNumericTokens = new ArrayList<TokenInfo>();
		
		for(TokenInfo token : tokens) {
			if(StringUtils.isAlphanumeric(token.getValue())) {
				alphaNumericTokens.add(token);
			}
		}
		
		return alphaNumericTokens;
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.longSentenceMultiplier != 0 ? 1 : 0;
	}

	@Override
	public Double[] calculateRawWeights(Double[] weights) {
		return weights;
	}

}
