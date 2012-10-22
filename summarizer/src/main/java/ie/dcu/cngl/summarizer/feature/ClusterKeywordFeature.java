package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The ClusterKeywordFeature is a method proposed by Luhn. Luhn specified that two significant words are considered significantly
 * related if they are separated by not more than five insignificant words. </br>
 * sentence score = number of significant words in largest cluster/total number of terms in largest cluster
 * @author Shane
 *
 */
public class ClusterKeywordFeature extends LuceneFeature {
	
	private int minSigWordSeparationCount;

	/**
	 * Creates ClusterKeywordFeature with a default for the minimum
	 * significant words separation count of 5.
	 * @throws IOException
	 */
	public ClusterKeywordFeature() throws IOException {
		super();
		this.minSigWordSeparationCount = SummarizerUtils.minSignificantWordSeparation == -1 ? 5 : SummarizerUtils.minSignificantWordSeparation;		
		//default
	}
	
	@Override
	public Double[] calculateRawWeights(Double[] weights) {
		try {
			buildIndex();
			ArrayList<String> topTerms = retrieveTopTerms();
			HashMap<String, Integer> topTermsAndRank = new HashMap<String, Integer>();
			int termNum = 0;
			for(String term : topTerms) {
				topTermsAndRank.put(term, termNum++);
			}
			
			int sentenceNumber = 0;
			for(ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
				for(ArrayList<TokenInfo> sentence : paragraph) {
					weights[sentenceNumber++]+=keywordClusterScore(topTermsAndRank, sentence);
				}
			}
		} catch(Exception e) {
			System.err.println("Lucene stuff failed");
			e.printStackTrace();
		}
		
		return weights;
	}

	private Double keywordClusterScore(HashMap<String, Integer> topTerms, ArrayList<TokenInfo> sentence) {
		boolean[] significanceRecord = new boolean[sentence.size()];
		int tokenNum = 0;
		for(TokenInfo token : sentence) {
			if(topTerms.get(token.getValue()) != null || topTerms.get(token.getValue().toLowerCase()) != null) {
				significanceRecord[tokenNum] = true;
			}
			tokenNum++;
		}
		
		int maxClusterSize = 0, numTermsInMaxCluster = 0, lengthBetweenSigWords = 0, numCurrentSigWords = 0, currentNumberOfTerms = 0;
		for(int i = 0; i < significanceRecord.length; i++) {
			if(lengthBetweenSigWords <= minSigWordSeparationCount) {
				if(significanceRecord[i]) {
					numCurrentSigWords++;
					lengthBetweenSigWords = 0;
				} else {
					if(numCurrentSigWords > 0) lengthBetweenSigWords++;
				}
				currentNumberOfTerms = numCurrentSigWords > 0 ? currentNumberOfTerms+1 : currentNumberOfTerms;
			} else {
				numCurrentSigWords = 0;
				lengthBetweenSigWords = 0;
				currentNumberOfTerms = 0;
			}
			
			if(numCurrentSigWords > maxClusterSize) {
				maxClusterSize = numCurrentSigWords;
				numTermsInMaxCluster = currentNumberOfTerms;
			}
		}
		
		return maxClusterSize > 0 ? Math.pow(maxClusterSize, 2)/numTermsInMaxCluster : 0;
	}

	@Override
	protected float computeBoost(int paragraphNumber, int sentenceNumber) {
		return 1;
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.clusterKeywordMultiplier;
	}

}
