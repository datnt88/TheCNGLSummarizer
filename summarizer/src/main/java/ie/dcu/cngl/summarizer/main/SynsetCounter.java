/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ie.dcu.cngl.summarizer.main;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.*;
import edu.mit.jwi.data.*;
import edu.mit.jwi.morph.*;

import edu.smu.tspell.wordnet.*;
import edu.smu.tspell.wordnet.impl.*;
import edu.smu.tspell.wordnet.impl.file.*;
import edu.smu.tspell.wordnet.impl.file.synset.*;
import ie.dcu.cngl.WordNetUtils.WordNetWSD;
import ie.dcu.cngl.tokenizer.TokenInfo;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author Kid
 */
public class SynsetCounter {

    public static void main(String[] args) {

        WordNetWSD wnw;
        IDictionary dict = null;
        String wnhome = "";
        String icfile = "";
        URL url = null;

        // *your* WordNet(vers.) is here ...
        wnhome = "WN/3.0/dict";

        try {
            url = new URL("file", null, wnhome);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {
            return;
        }

        dict = new Dictionary(url);
        dict.open();

        ArrayList<String> newQuery = new ArrayList<String>();
        String word_string = "great_wall";
        newQuery.add(word_string);
        IIndexWord idxWord;
        
        
        
        idxWord = dict.getIndexWord(word_string, POS.NOUN);
        if (idxWord != null) {

            for (int i = 0; i < idxWord.getWordIDs().size(); i++) {
                IWordID wordID = idxWord.getWordIDs().get(i);
                IWord word = dict.getWord(wordID);
                ISynset synset = word.getSynset();

                for (IWord w : synset.getWords()) {
                    if (!newQuery.contains(w.getLemma())) {
                        newQuery.add(w.getLemma());
                        System.out.println(w.getLemma());
                    }
                }
            }
        } else {
            System.out.println("There is no sysnset");
        }

       


        




        /*
         NounSynset nounSynset;
         NounSynset[] hyponyms;

         System.setProperty("wordnet.database.dir", "WN/3.0/dict");
         WordNetDatabase database = WordNetDatabase.getFileInstance();
        
         String word = "prize";
       
         Synset[] synsetsList = database.getSynsets(word);
        
         int numSynset = synsetsList.length;
         Synset tmp;
         for(int i =0; i < numSynset; i++){
         tmp = synsetsList[i];
            
            
            
         }

         System.out.println("There are: " + numSynset + " synsets of word '" + word + "'" );

         // for (int i = 0; i < synsets.length; i++) {
         // nounSynset = (NounSynset) (synsets[i]);
         //  hyponyms = nounSynset.getHyponyms();
         //System.err.println(nounSynset.getWordForms()[0]
         //         + ": " + nounSynset.getDefinition() + ") has " + hyponyms.length + " hyponyms");

         //  }
         */
    }
}
