package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Calculates the number of named entities that occur in each sentence. As NE taggers
 * are quite slow a more naive approach is taken here. Any tokens (except the first), that
 * start with a capital letter are assumed to be named entities.</br>
 * sentence boost = (number of NEs present)^2/number of terms
 * @author Shane
 *
 */
public class NamedEntityFeature extends LuceneFeature {

	public NamedEntityFeature() throws IOException {
		super();
	}

	@Override
	protected float computeBoost(int paragraphNumber, int sentenceNumber) {
		ArrayList<TokenInfo> sentence = structure.getSentenceFromParagraphTokens(sentenceNumber, paragraphNumber);
		int numNamedEntities = calculateNumNamedEntities(sentence);
		float boost = (float) (Math.pow(numNamedEntities, 2)/numberOfTerms(sentence));
		return boost;
	}

	private int calculateNumNamedEntities(ArrayList<TokenInfo> sentence) {
		int numNamedEntities = 0;
		for(int i = 1; i < sentence.size(); i++) {	//Ignore first token
			String token = sentence.get(i).getValue();
			if(Character.isUpperCase(token.charAt(0))) {
				numNamedEntities++;
			}
		}
		return numNamedEntities;
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.namedEntityMultiplier;
	}

}