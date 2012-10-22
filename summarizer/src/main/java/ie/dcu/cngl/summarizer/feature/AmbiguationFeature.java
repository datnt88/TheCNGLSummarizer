/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.io.IOException;
import java.util.ArrayList;

import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.*;
import edu.smu.tspell.wordnet.impl.file.*;
import edu.smu.tspell.wordnet.impl.file.synset.*;

/**
 * Calculate the number of synsets of all term in sentences sentence score =
 * number of synset/number of terms in document
 *
 * @author Dat Tien Nguyen
 */
public class AmbiguationFeature extends Feature {

    private int numberTermsOfDocument;

    public AmbiguationFeature() throws IOException {
        super();
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {
        int sentenceNumber = 0;
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                double numSynset = 0;
                double numTerms = numberOfTerms(sentence);
                if (numTerms > 0.0) {

                    for (int i = 0; i < sentence.size(); i++) {
                        String token = sentence.get(i).getValue();
                        System.setProperty("wordnet.database.dir", "WN/3.0/dict");
                        WordNetDatabase database = WordNetDatabase.getFileInstance();
                        //numSynset *= Math.max(1,database.getSynsets(token.toLowerCase()).length);
                        numSynset += database.getSynsets(token.toLowerCase()).length;
                    }
                    weights[sentenceNumber++] = 2 * (double) numSynset / numTerms;
                    //System.out.println(numSynset);

                } else {
                    weights[sentenceNumber++] = 0.0000;
                }
            }
        }
        return weights;
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.ambiguationMultiplier;
    }
}
