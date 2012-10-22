package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;

import java.io.IOException;

/**
 * Applies a linear deboost function to simulate the manual heuristic of
 * summarizing by skimming the first few sentences off a paragraph.
 */
public class SkimmingFeature extends LuceneFeature {

	// these two values are used to implement a simple linear deboost. If 
	// a different algorithm is desired, these variables are likely to be
	// no longer required.
	private float sentenceDeboost;
	private float sentenceDeboostBase = 0.5F;

	public SkimmingFeature() throws IOException {
		super();
	}
	
	/**
	 * Applies a index-time deboost to the sentences after the first
	 * one in all the paragraphs after the first one. This attempts to
	 * model the summarization heuristic that a summary can be generated
	 * by reading the first paragraph (in full) of a document, followed
	 * by the first sentence in every succeeding paragraph. The first 
	 * paragraph is not deboosted at all. For the second and succeeding
	 * paragraphs, the deboost is calculated as (1 - sentence_pos * deboost)
	 * until the value reaches sentenceDeboostBase (default 0.5) or less, 
	 * and then no more deboosting occurs. 
	 * @param sentenceDeboost the deboost value to set. Must be between 
	 *        0 and 1. Default is no deboosting, ie sentenceDeboost == 0.
	 */
	public void setSentenceDeboost(float sentenceDeboost) {
		if (sentenceDeboost < 0.0F || sentenceDeboost > 1.0F) {
			throw new IllegalArgumentException(
			"Invalid value: 0.0F <= sentenceDeboost <= 1.0F");
		}
		this.sentenceDeboost = sentenceDeboost;
	}

	/**
	 * This parameter is used in conjunction with sentenceDeboost. This
	 * value defines the base until which deboosting will occur and then
	 * stop. Default is set to 0.5 if not set. Must be between 0 and 1.
	 * @param sentenceDeboostBase the sentenceDeboostBase to set.
	 */
	public void setSentenceDeboostBase(float sentenceDeboostBase) {
		if (sentenceDeboostBase < 0.0F || sentenceDeboostBase > 1.0F) {
			throw new IllegalArgumentException(
			"Invalid value: 0.0F <= sentenceDeboostBase <= 1.0F");
		}
		this.sentenceDeboostBase = sentenceDeboostBase;
	}
	
	@Override
	protected float computeBoost(int paragraphNumber, int sentenceNumber) {
		if (paragraphNumber > 0) {
			if (sentenceNumber > 0) {
				float deboost = 1.0F - (sentenceNumber * sentenceDeboost);
				deboost = (deboost < sentenceDeboostBase) ? sentenceDeboostBase : deboost; 
				return deboost;
			}
		}

		return 1.0F;
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.skimmingMultiplier;
	}

}
