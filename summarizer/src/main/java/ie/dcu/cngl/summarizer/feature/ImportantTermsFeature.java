package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import ie.dcu.cngl.tokenizer.Tokenizer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Calculates the number of supplied technical terms that occur in each sentence. This
 * may be useful for a user that is familiar with the domain.</br>
 * sentence score = number of important terms present/number of terms
 * @author Shane
 */
public class ImportantTermsFeature extends TermCheckingFeature {
	
	private HashMap<String, ArrayList<ArrayList<TokenInfo>>> importantTerms;

	public ImportantTermsFeature() throws IOException {
		ArrayList<ArrayList<TokenInfo>> termsList = new ArrayList<ArrayList<TokenInfo>>();
		Tokenizer tokenizer = Tokenizer.getInstance();
		for(String line : terms) {
		    line = line.toLowerCase();
		    ArrayList<TokenInfo> tokens = tokenizer.tokenize(line);
		    termsList.add(tokens);
        }
        this.importantTerms = generateMultiMap(termsList);
	}

	@Override
	public Double[] calculateRawWeights(Double[] weights) {
		int sentenceNumber = 0;
		for(ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
			for(ArrayList<TokenInfo> sentence : paragraph) {
				weights[sentenceNumber++]+=getCrossoverCount(importantTerms, sentence);
			}
		}
		return weights;
	}
	
	@Override
	public double getMultiplier() {
		return SummarizerUtils.cuePhraseMultiplier;
	}

	@Override
	public String getTermsFileName() {
		return SummarizerUtils.importantTermsFile;
	}
	
}
