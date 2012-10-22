/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Calculates the number of special words (date, time ..) that occur in each sentence.
 * This may be useful for a user that is not overly familiar with more technical language</br>
 * sentence score = number of occurrence/number of terms in sentence
 * @author Dat Tien Nguyen
 */
public class DatetimeTermFeature extends TermCheckingFeature {

    private HashMap<String, ArrayList<ArrayList<TokenInfo>>> datetimeTerm;

    public DatetimeTermFeature() throws IOException {
        ArrayList<ArrayList<TokenInfo>> wordsList = new ArrayList<ArrayList<TokenInfo>>();
        for (String line : terms) {
            line = line.toLowerCase().trim();
            ArrayList<TokenInfo> tokenLine = new ArrayList<TokenInfo>();
            tokenLine.add(new TokenInfo(line));
            wordsList.add(tokenLine);
        }
        this.datetimeTerm = generateMultiMap(wordsList);
        //System.out.println(datetimeTerm.size());
        //System.out.println("Datetime term feature loaded sucessfully!");
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {

        int sentenceNumber = 0;
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                double numOccurences = 0;
                numOccurences += getCrossoverCount(datetimeTerm, sentence);
                weights[sentenceNumber++] = numOccurences;
                
            }
        }
        return weights;
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.datetimeTermMultiplier;
    }

    @Override
    public String getTermsFileName() {
        return SummarizerUtils.datetimeTermFile;
    }
}
