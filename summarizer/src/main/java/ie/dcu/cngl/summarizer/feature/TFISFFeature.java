package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;

import java.io.IOException;

/**
 * Like TFIDF, except we're working at sentence level. Every sentence is treated like a document.
 * @author Shane
 *
 */
public class TFISFFeature extends LuceneFeature {

	public TFISFFeature() throws IOException {
		super();
	}

	@Override
	protected float computeBoost(int paragraphNumber, int sentenceNumber) {
		return 1;	//All will be treated the same
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.TFISFMultiplier;
	}

}
