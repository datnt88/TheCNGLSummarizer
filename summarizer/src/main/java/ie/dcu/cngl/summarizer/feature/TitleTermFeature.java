package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The title of an article often reveals the major subject of that document.
 * Sentences containing terms from the title are likely to be good summarization
 * candidates.</br> sentence score = number of title terms found in
 * sentence/total number of title terms
 *
 * @author Shane
 *
 */
public class TitleTermFeature extends Feature {

    private ArrayList<TokenInfo> titleTokens;

    public TitleTermFeature(ArrayList<TokenInfo> titleTokens) throws IOException {
        this.titleTokens = titleTokens;
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {
        final double numTitleTerms = numberOfTerms(titleTokens);
        //System.out.println(numTitleTerms);

        int sentenceNumber = 0;
        ArrayList<TokenInfo> tokenHolder;
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                double numSentenceTerms = numberOfTerms(sentence);
                double numOccurences = 0;
                
                for (TokenInfo titleToken : titleTokens) {
                    tokenHolder = new ArrayList<TokenInfo>();
                    tokenHolder.add(titleToken);
                    numOccurences += getNumOccurrences(tokenHolder, sentence);
                }
               
                //System.out.println("sentencen term: " + numSentenceTerms);
                //System.out.println("Overlap: " + numOccurences);
                
                if (numSentenceTerms != 0) {
                    if (numTitleTerms >= numSentenceTerms) {
                        weights[sentenceNumber++] = numOccurences / numTitleTerms;
                    } else {
                        weights[sentenceNumber++] = numOccurences / numSentenceTerms;
                    }
                }
                

            }
        }
        return weights;
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.titleTermMultiplier;
    }
}
