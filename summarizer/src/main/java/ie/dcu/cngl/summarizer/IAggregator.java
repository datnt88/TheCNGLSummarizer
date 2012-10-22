package ie.dcu.cngl.summarizer;

import ie.dcu.cngl.tokenizer.SectionInfo;

import java.util.ArrayList;

/**
 * Provides interface for aggregating sentences scores and ranking them.
 * @author Shane
 *
 */
public interface IAggregator {
	
	/**
	 * Compiles 2-dimensional array of weights to 1-dimensional array
	 * of combined results.
	 * @param allWeights 2d array of all weights
	 * @return Sentence and overall score
	 */
	public SentenceScore[] aggregate(ArrayList<Double[]> allWeights);

	/**
	 * Set sentences
	 * @param sentences
	 */
	public void setSentences(ArrayList<SectionInfo> sentences);

}
