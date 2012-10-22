/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;


import ie.dcu.cngl.WordNetUtils.WordNetWSD;
import edu.mit.jwi.morph.WordnetStemmer;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.net.*;
import edu.mit.jwi.Dictionary;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.PhraseQuery;

/**
 * Finding the all words in all synsets of all term in query Using term overlap
 * Assign score to each sentence = the number of term ovelap /num query term
 *
 *
 * @author Dat Tien Nguyen
 */
public class QuerySynsetFeature extends Feature {

    private ArrayList<TokenInfo> query;
    private ArrayList<TokenInfo> phraseQuery;

    public QuerySynsetFeature(ArrayList<TokenInfo> query, ArrayList<TokenInfo> phraseQuery) throws IOException {
        this.query = query;
        this.phraseQuery = phraseQuery;
    }

    @Override
    public Double[] calculateRawWeights(Double[] weights) {

        //recompute the query
        //term overlap
        ArrayList<String> newQuery = new ArrayList<String>();
        newQuery = renewQuery(query, phraseQuery);

        int numQueryToken = newQuery.size();
        final double numQueryTerms = (double) numQueryToken;

        /*
         for (int i = 0; i < newQuery.size(); i++) {
         System.out.println(newQuery.get(i));
         }
         */
        /////////////////////////////////////////////

        int numOfConcepts = 0;
        int sentenceNumber = 0;
        double numSentenceTerms = 0;
        int numToken = 0;

        for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
            for (ArrayList<TokenInfo> sentence : paragraph) {
                numToken = sentence.size();
                numSentenceTerms = numberOfTerms(sentence);

                String sentenceString = "";
                for (int i = 0; i < numToken; i++) {
                    sentenceString += sentence.get(i).getValue().toLowerCase().trim() + " ";
                }

                //System.out.println(sentenceString);
                int count_overlap = 0;
                for (int i = 0; i < numQueryToken; i++) {
                    count_overlap += StringUtils.countMatches(sentenceString, newQuery.get(i).toLowerCase().trim() + " ");

                }

                if (numQueryTerms >= numSentenceTerms) {
                    weights[sentenceNumber++] = Math.pow(count_overlap, 2) / numQueryTerms;
                } else {
                    weights[sentenceNumber++] = Math.pow(count_overlap, 2) / numSentenceTerms;
                }
                //System.out.println("Counting synset overlap result is: " + count_overlap);

            }
        }

        return weights;


    }

    //Calculate new query token with all synonyms
    public ArrayList<String> renewQuery(ArrayList<TokenInfo> query, ArrayList<TokenInfo> phraseQuery) {
        //4 wordnet
        IDictionary dict = null;
        String wnhome = "";
        String icfile = "";
        URL url = null;


        // *your* WordNet(vers.) is here ...
        wnhome = "WN/3.0/dict";
// *your* IC files are here ... (assumes that you have downloaded the IC files which correspond to the WordNet(vers.)
        //icfile = "WN/3.0/WordNet-InfoContent-3.0/ic-semcor.dat";
        url = null;
        try {
            url = new URL("file", null, wnhome);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {
            return null;
        }
        dict = new Dictionary(url);
        dict.open();

        WordnetStemmer stemmer = null;
        stemmer = new WordnetStemmer(dict);



        //Find all synset of all term in query
        //add all words in all synset to new query
        //ArrayList<TokenInfo> newQuery = new ArrayList<TokenInfo>();

        ArrayList<TokenInfo> combined_tmp = new ArrayList<TokenInfo>();

        if (phraseQuery.size() > 0) {
            combined_tmp.addAll(query);
            combined_tmp.addAll(phraseQuery);
        } else {
            combined_tmp.addAll(query);
            System.out.println("checked");
        }


        ArrayList<String> newQuery = new ArrayList<String>();

        String word_string = "";
        IIndexWord idxWord;

        //stemmer.findStems();
        //List<String> baseform ;

        for (TokenInfo queryToken : combined_tmp) {
            word_string = queryToken.getValue().toString().trim();
            if (!newQuery.contains(word_string.toLowerCase())) {
                newQuery.add(word_string.toLowerCase());
            }


            List<String> baseforms = stemmer.findStems(word_string);
            //Stem word

            if (!baseforms.isEmpty()) {
                for (int k = 0; k < baseforms.size(); k++) {
                    String base_word = baseforms.get(k);

                    idxWord = dict.getIndexWord(base_word, POS.NOUN);
                    if (idxWord != null) {

                        for (int i = 0; i < idxWord.getWordIDs().size(); i++) {
                            IWordID wordID = idxWord.getWordIDs().get(i);
                            IWord word = dict.getWord(wordID);
                            ISynset synset = word.getSynset();

                            for (IWord w : synset.getWords()) {
                                if (!newQuery.contains(w.getLemma().toLowerCase().replaceAll("_", " "))) {
                                    newQuery.add(w.getLemma().toLowerCase().replaceAll("_", " "));
                                    //System.out.println(w.getLemma());
                                }
                            }
                        }
                    }

                    //Pos is VERB
                    idxWord = dict.getIndexWord(base_word, POS.VERB);
                    if (idxWord != null) {

                        for (int i = 0; i < idxWord.getWordIDs().size(); i++) {
                            IWordID wordID = idxWord.getWordIDs().get(i);
                            IWord word = dict.getWord(wordID);
                            ISynset synset = word.getSynset();

                            for (IWord w : synset.getWords()) {
                                if (!newQuery.contains(w.getLemma().toLowerCase().replaceAll("_", " "))) {
                                    newQuery.add(w.getLemma().toLowerCase().replaceAll("_", " "));
                                    //System.out.println(w.getLemma());
                                }
                            }
                        }
                    }

                    //Pos is ADJECTIVE
                    idxWord = dict.getIndexWord(base_word, POS.ADJECTIVE);
                    if (idxWord != null) {

                        for (int i = 0; i < idxWord.getWordIDs().size(); i++) {
                            IWordID wordID = idxWord.getWordIDs().get(i);
                            IWord word = dict.getWord(wordID);
                            ISynset synset = word.getSynset();

                            for (IWord w : synset.getWords()) {
                                if (!newQuery.contains(w.getLemma().toLowerCase().replaceAll("_", " "))) {
                                    newQuery.add(w.getLemma().toLowerCase().replaceAll("_", " "));
                                    //System.out.println(w.getLemma());
                                }
                            }
                        }
                    }

                    //POS is ADVERB
                    idxWord = dict.getIndexWord(base_word, POS.ADVERB);
                    if (idxWord != null) {

                        for (int i = 0; i < idxWord.getWordIDs().size(); i++) {
                            IWordID wordID = idxWord.getWordIDs().get(i);
                            IWord word = dict.getWord(wordID);
                            ISynset synset = word.getSynset();

                            for (IWord w : synset.getWords()) {
                                if (!newQuery.contains(w.getLemma().toLowerCase().replaceAll("_", " "))) {
                                    newQuery.add(w.getLemma().toLowerCase().replaceAll("_", " "));
                                    //System.out.println(w.getLemma());
                                }
                            }
                        }
                    }///end finding

                }

            }



            //Finding all sysset of this token
            //Pos is Noun

        }

        //Process character '_' in each concept if it has
        System.out.println("\nNew query tokens are shown below: ");
        for (int i = 0; i < newQuery.size(); i++) {

            String tmp = newQuery.get(i);

            if (tmp.contains("_")) {
                newQuery.set(i, tmp.trim().replaceAll("_", " "));
            }
            System.out.println(tmp);
        }

        return newQuery;

    }

    /*
     @Override
     public Double[] calculateRawWeights(Double[] weights) {
     //Find the sence of each concept in query
     // Vector query_synsets = findConceptSynsetsInQuery();
     //Compute the sence of each concept in document
     //Synset overlap
     //Score to each sentence
     ArrayList<ISynset> query_synsets = findConceptSynsetsInQuery();
     ArrayList<ISynset> document_synsets = findConceptSynsetsInDocument();
     System.out.println("Finding synses in query and document completed!");

     int tokenIndex = 0;
     int numOfConcepts = 0;
     int sentenceNumber = 0;
     Vector list_of_concepts;

     ISynset synset_sentence;
     ISynset synset_query;


     for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
     for (ArrayList<TokenInfo> sentence : paragraph) {
     list_of_concepts = listOfConcepts(sentence);
     numOfConcepts = list_of_concepts.size();
     int count_overlap = 0;
     for (int i = 0; i < list_of_concepts.size(); i++) {
     synset_sentence = document_synsets.get(i + tokenIndex);
     for (int j = 0; j < query_synsets.size(); j++) {
     synset_query = query_synsets.get(j);
     //Find synsetId of word in query
     //Synset overllap
     if ((synset_query!= null) && (synset_sentence!= null) && (synset_sentence.equals(synset_query))) {
     count_overlap += 1;
     }
     }
     //System.out.println(list_of_concepts.get(i).toString() + " has: " + word_form + " ---- " + word_pos + " ---- " + senseId);
     }
     weights[sentenceNumber++] = (double) count_overlap / numOfConcepts;
     System.out.println("Counting synset overlap result is: " + count_overlap);
     tokenIndex += numOfConcepts;
     }
     }

     return weights;
     }
     */
    /*
     public ArrayList<ISynset> findConceptSynsetsInQuery() {
     String query_string = "";
     for (int i = 0; i < query.size(); i++) {
     query_string += query.get(i).getValue().toString() + " ";
     }

     Vector query_senses = wnw.WNwsd_LESK(query_string);

     String[] word_pos_senseId;
     String pos = "";
     int senseId = 0;
     String senseId_string = "";
     String word_form = "";
     IIndexWord word1 = null;
     ArrayList<ISynset> query_synsets = new ArrayList<ISynset>();
     check text node in dom java
     for (int i = 0; i < query_senses.size(); i++) {

     System.out.println(query_senses.get(i).toString());
     word_pos_senseId = query_senses.get(i).toString().split("#");
     pos = word_pos_senseId[1];
     word_form = word_pos_senseId[0];
     senseId_string = word_pos_senseId[2];

     if (senseId_string.equals("?")) {
     query_synsets.add(i, null);
     } else {
     senseId = Integer.parseInt(senseId_string);
     if (pos.equalsIgnoreCase("n")) {
     word1 = dict.getIndexWord(word_form, POS.NOUN);
     }
     if (pos.equalsIgnoreCase("v")) {
     word1 = dict.getIndexWord(word_form, POS.VERB);
     }
     if (pos.equalsIgnoreCase("a")) {
     word1 = dict.getIndexWord(word_form, POS.ADJECTIVE);
     }
     if (pos.equalsIgnoreCase("r")) {
     word1 = dict.getIndexWord(word_form, POS.ADVERB);
     }
     if (word1 != null) {
     IWordID word1ID = word1.getWordIDs().get(senseId);
     query_synsets.add(i, dict.getWord(word1ID).getSynset());
     } else {
     query_synsets.add(i, null);
     }
     }
     }

     return query_synsets;
     }

     public ArrayList<ISynset> findConceptSynsetsInDocument() {
     //Context is all sentence in document
     String document_string = "";
     for (ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
     for (ArrayList<TokenInfo> sentence : paragraph) {
     for (int i = 0; i < sentence.size(); i++) {
     document_string += sentence.get(i).getValue().toString() + " ";
     }
     }

     }

     //Find 
     Vector documnet_senses = wnw.WNwsd_LESK(document_string);

     String[] word_pos_senseId;
     String pos = "";
     int senseId = 0;
     String senseId_string = "";
     String word_form = "";
     IIndexWord word1 = null;
     ArrayList<ISynset> documnet_synsets = new ArrayList<ISynset>();

     for (int i = 0; i < documnet_senses.size(); i++) {

     System.out.println(documnet_senses.get(i).toString());

     word_pos_senseId = documnet_senses.get(i).toString().split("#");
     pos = word_pos_senseId[1];
     word_form = word_pos_senseId[0];
     senseId_string = word_pos_senseId[2];

     if (senseId_string.equals("?")) {
     documnet_synsets.add(i, null);
     } else {

     senseId = Integer.parseInt(senseId_string);

     if (pos.equalsIgnoreCase("n")) {
     word1 = dict.getIndexWord(word_form, POS.NOUN);
     }
     if (pos.equalsIgnoreCase("v")) {
     word1 = dict.getIndexWord(word_form, POS.VERB);
     }
     if (pos.equalsIgnoreCase("a")) {
     word1 = dict.getIndexWord(word_form, POS.ADJECTIVE);
     }
     if (pos.equalsIgnoreCase("r")) {
     word1 = dict.getIndexWord(word_form, POS.ADVERB);
     }

     if (word1 != null) {
     IWordID word1ID = word1.getWordIDs().get(senseId);
     documnet_synsets.add(i, dict.getWord(word1ID).getSynset());
     } else {
     documnet_synsets.add(i, null);
     }
     //get synset
     }
     }
     return documnet_synsets;
     }
     */
    @Override
    public double getMultiplier() {
        return SummarizerUtils.querySynsetMultiplier;
    }
}
