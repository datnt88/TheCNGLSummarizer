package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Calculates the number of supplied basic words that occur in each sentence.
 * This may be useful for a user that is not overly familiar with more technical
 * language</br> sentence score = number of basic words present/number of terms
 *
 * @author Shane
 *
 */
public class BasicWordsFeature extends TermCheckingFeature {

    private HashMap<String, ArrayList<ArrayList<TokenInfo>>> basicWords;

    public BasicWordsFeature() throws IOException {
        ArrayList<ArrayList<TokenInfo>> wordsList = new ArrayList<ArrayList<TokenInfo>>();
        for (String line : terms) {
            line = line.toLowerCase().trim();
            ArrayList<TokenInfo> tokenLine = new ArrayList<TokenInfo>();
            tokenLine.add(new TokenInfo(line));
            wordsList.add(tokenLine);
        }
        this.basicWords = generateMultiMap(wordsList);

    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {
        int sentenceNumber = 0;
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                double numOccurences = 0, numTerms = numberOfTerms(sentence);
                if (numTerms > 0.0) {
                    numOccurences += getCrossoverCount(basicWords, sentence);
                    weights[sentenceNumber++] = numOccurences / numTerms;
                } else {
                    weights[sentenceNumber++] = 0.000;
                }
            }
        }
        return weights;
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.basicWordsMultiplier;
    }

    @Override
    public String getTermsFileName() {
        return SummarizerUtils.basicWordsFile;
    }
}
