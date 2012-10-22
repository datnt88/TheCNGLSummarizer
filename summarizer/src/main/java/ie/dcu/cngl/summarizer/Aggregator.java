package ie.dcu.cngl.summarizer;

import ie.dcu.cngl.tokenizer.SectionInfo;

import java.util.ArrayList;

/**
 * Combines scores from 2 dimensional array into one and ranks sentences accordingly.
 * Simple linear addition is used. Any sentences receiving individual negative
 * scores are discounted completely.
 * @author Shane
 *
 */
public class Aggregator implements IAggregator {
	
	private ArrayList<SectionInfo> sentences; 

	/**
	 * Combines provided weights.
	 * @param allWeights 2-dimensional array of all weights computed for each sentence
	 * @return Array of sentences with their ranking.
	 */
	public SentenceScore[] aggregate(ArrayList<Double[]> allWeights) {
		final int numSentences = sentences.size();
		double[] totalWeights = new double[numSentences];
		SentenceScore[] scores = new SentenceScore[numSentences];
		boolean[] flaggedAsBad = new boolean[numSentences];
		
		//Calculating all weights
		for(Double[] featureWeights : allWeights) {
			for(int i = 0; i < featureWeights.length; i++) {
				if(featureWeights[i] >= 0 && !flaggedAsBad[i]) {
					totalWeights[i]+=featureWeights[i];
				} else {
					flaggedAsBad[i] = true;
					totalWeights[i] = -1;
				}
			}
		}
		
		//Changing any negative scores to zero
		for(int i = 0; i < numSentences; i++) {
			if(totalWeights[i] < 0) {
				totalWeights[i] = 0;
			}
		}
		
		//Pairing weights with sentences for ranking
		for(int i = 0; i < numSentences; i++) {
			scores[i] = new SentenceScore(sentences.get(i).getValue(), totalWeights[i]);
		}
		
		rank(scores);
		
		return scores;
	}

	private void rank(SentenceScore[] scores) {
		int [] rank = new int[scores.length];
		for(int i = 0; i < rank.length; i++) {
			int best = getMaxIndex(scores);
			rank[best] = i;
			scores[best].setScore(-1);
		}
		
		for(int i = 0; i < rank.length; i++) {
			scores[i].setScore(rank[i]);
		}
	}

	private int getMaxIndex(SentenceScore[] array) {
		int maxIndex = 0;
		for(int i = 1; i < array.length; i++) {
			if(array[i].getScore() > array[maxIndex].getScore()) {
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	/**
	 * Sets sentences to be ranked.
	 * @param sentences Array of sentences being considered for summarization
	 */
	@Override
	public void setSentences(ArrayList<SectionInfo> sentences) {
		this.sentences = sentences;
	}
}
