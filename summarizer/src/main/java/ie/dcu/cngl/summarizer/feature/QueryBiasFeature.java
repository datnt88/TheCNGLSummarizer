package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;

/**
 * Bias factor to score sentences containing query terms more highly.</br>
 * sentence score = (number of query terms in the sentence)^2/number of terms in
 * query
 *
 * @author Shane
 *
 */
public class QueryBiasFeature extends Feature {

    private ArrayList<TokenInfo> query;
    private ArrayList<TokenInfo> phraseQuery;

    public QueryBiasFeature(ArrayList<TokenInfo> query, ArrayList<TokenInfo> phraseQuery) throws IOException {
        this.query = query;
        this.phraseQuery = phraseQuery;

        //this.query.addAll(phraseQuery);
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {
        final double numQueryTerms = numberOfTerms(query) + phraseQuery.size();
        //System.out.println(numberOfTerms(query));
        //System.out.println(phraseQuery.size());
        
        
        int sentenceNumber = 0;
        double numSentenceTerms = 0;
        int numToken;

        ArrayList<TokenInfo> tokenHolder;
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                numToken = sentence.size();
                String sentenceString = "";
                numSentenceTerms = numberOfTerms(sentence);
                
                for (int i = 0; i < numToken; i++) {
                    sentenceString += sentence.get(i).getValue().toLowerCase().trim() + " ";
                }
                //System.out.println(sentenceString);
                
                double numOccurences = 0;
                for (TokenInfo queryToken : query) {
                    tokenHolder = new ArrayList<TokenInfo>();
                    tokenHolder.add(queryToken);
                    numOccurences += getNumOccurrences(tokenHolder, sentence);
                    
                }
                //System.out.println("Token occurences: " + numOccurences); 
                ///////////////////
                
                for (int i = 0; i < phraseQuery.size(); i++) {
                    numOccurences += StringUtils.countMatches(sentenceString, phraseQuery.get(i).getValue().toLowerCase());
                }
                
                //System.out.println(sentenceString);
                
                if (numQueryTerms >= numSentenceTerms) {
                    weights[sentenceNumber++] = Math.pow(numOccurences, 2) / numQueryTerms;
                    //System.out.println("From query bias: " + numOccurences + " and "+numQueryTerms);
                } else {
                    weights[sentenceNumber++] = Math.pow(numOccurences, 2) / numSentenceTerms;
                    //System.out.println("From sentences term: " + numOccurences + " and " + numSentenceTerms);
                }
                
                
            }
        }

        return weights;
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.queryBiasMultiplier;
    }
}
