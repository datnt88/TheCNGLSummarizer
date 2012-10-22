/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.WordNetUtils.DepthFinder;
import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

import ie.dcu.cngl.WordNetUtils.WordNetWSD;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.Dictionary;

/**
 * Finding the best senses of Nouns and Verbs in sentence
 * calculate depth of that senses
 * Assign score to each sentence = Sum of depths / the number terms in sentence
 * @author Dat Tien Nguyen
 */
public class ConceptHierarchyFeature extends Feature {

    private IDictionary dict = null;
    private String icfile = "";
    String vers = "";
    String wnhome = "";
    URL url = null;

    public ConceptHierarchyFeature() throws IOException {
        super();
        vers = "3.0";
        wnhome = "WN/3.0/dict";
        icfile = "WN/3.0/WordNet-InfoContent-3.0/ic-semcor.dat";
        url = null;
        try {
            url = new URL("file", null, wnhome);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {
            return;
        }
        dict = new Dictionary(url);
        try {
            dict.open();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {
        //Find the best sence of each concept in document by using Adapted lesk
        Vector tv1 = findConceptSensesInDocument();

        int tokenIndex = 0;
        int numOfConcepts = 0;
        int sentenceNumber = 0;
        Vector list_of_concepts;
        String[] word_pos_sence;
        String senseId = "";
        String word_pos = "";
        String word_form = "";
        DepthFinder depthfinder = new DepthFinder(dict, icfile);        

        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                list_of_concepts = listOfConcepts(sentence);
                numOfConcepts = list_of_concepts.size();
                double heirarchy_depth = 0;
                for (int i = 0; i < list_of_concepts.size(); i++) {
                    word_pos_sence = tv1.get(i + tokenIndex).toString().split("#");
                    senseId = word_pos_sence[2];
                    word_pos = word_pos_sence[1];
                    word_form = word_pos_sence[0];
                    
                    //Calculate depth of concept 
                    if ((senseId.compareTo("?") != 0) && (word_pos.compareTo("a") != 0) && (word_pos.compareTo("r") != 0)) {
                        heirarchy_depth += depthfinder.getSynsetDepth(word_pos_sence[0], Integer.parseInt(senseId) + 1, word_pos);
                    }
                }
                weights[sentenceNumber++] = heirarchy_depth / tv1.size();
                System.out.println(heirarchy_depth / tv1.size());
                tokenIndex += numOfConcepts;
            }
        }
        return weights;
    }

    public Vector findConceptSensesInDocument() {
        //Context is all sentence in document
        String document_string = "";
        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                for (int i = 0; i < sentence.size(); i++) {
                    document_string += sentence.get(i).getValue().toString() + " ";
                }
            }
        }
        WordNetWSD wnw = new WordNetWSD("data/stoplist.txt");
        return wnw.WNwsd_LESK(document_string);
    }

    @Override
    public double getMultiplier() {
        return SummarizerUtils.conceptHierarchyMultiplier;
    }
}
