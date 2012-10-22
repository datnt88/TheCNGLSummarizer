package ie.dcu.cngl.WordNetUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.Set;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Iterator;
import java.io.*;
import java.util.TreeMap;

import edu.sussex.nlp.jws.*;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.mit.jwi.item.*;
import edu.mit.jwi.data.*;
import edu.mit.jwi.morph.*;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.Dictionary;
import org.apache.commons.lang.StringUtils;

public class WordNetWSD {
    private static SimpleStemmer ss;
    private static WordnetStemmer ws;
    private static PorterStemmer ps;

    public static int num_oov=0;
    public static int num_query_complexity=0;
    public static Set exclusion_list;
    public static String qid;
    private static JWS jws;
    private Path path;
    private static IDictionary dict;

    //    private static AdaptedLeskTanimotoNoHyponyms altnh;
    private static CNGLalesk altnh;
    private static Set stopwords;
    
    private static String wn_dir ="./WN/";
    private static String wn_home="./WN/3.0/";

    public WordNetWSD(String stopfile) {
        System.out.println("WordNetWSD");
        String dir = wn_dir;
        jws  = new JWS(dir, "3.0");
        //old: System.getenv("WNHOME");
        String path = wn_home + File.separator + "dict";
        URL url = null;
        try{
            url = new URL("file", null, path); 
	} catch (MalformedURLException e) { 
	    e.printStackTrace(); 
	}
        if(url == null) 
	    return;
	// construct the dictionary object and open it
        dict = new Dictionary(url);
        dict.open();
        ws = new WordnetStemmer(dict);
        ss = new SimpleStemmer();
	ps = new PorterStemmer();
//	altnh= jws.getAdaptedLeskTanimotoNoHyponyms();
	// 
	// use own list of stopwords
	if ((stopfile != null) && (!stopfile.equals(""))) { // always
	    System.out.println("# stop: initializing stopwords");
	    HashSet nwords=new HashSet();
	    try {
		BufferedReader reader=new BufferedReader(new FileReader(stopfile));
		String line="";
		while (null!=(line=reader.readLine())) {
		    if (line.charAt(0)=='#') {
			; // skip
		    } else {
			nwords.add(line.trim());
		    }
		}
	    }
	    catch (Exception e) {
		System.out.println("ERROR: loading file " +e);
	    }
	    
	    stopwords = nwords;
	    System.out.println("# stop: using " + stopwords.size() + " stopwords");
	}

	altnh=new CNGLalesk(dict, stopwords);
	//System.out.println("altnh:" + altnh.max("apple", "bananae", "n"));
        //WNsaveGEO(dict);
	//this.WNtest("wn_gold2.txt");
        //CNGLutils.pressKey();
	//altnh.fill_cache("data/cache_file.db");
        System.out.println("WordNetWSD init");
    }

    public static Vector string2vector(String str) {
	String delims="{}[],./;'#<>?:@~!\"£$%^&*()-_=+ ";
	Vector<String> result=new Vector<String>();
	String[] elts=StringUtils.split(str, delims);
	for (int i=0; i<elts.length; i++) {
	    result.add(elts[i]);
	}
	// System.out.println("vec:" + result);
	return result;
    }

    public static String Strings2SenseWord(String wrd, String pos, String sense) {
	String s=wrd+"#"+pos+"#"+sense;
	return s;
    }

    public static String[] SenseWord2Strings(String s) {
	return StringUtils.split(s, "#");
    }


    public static Vector<Vector<String>> getWordForms(Vector<String> words) {
	Vector result=new Vector();
	Vector tl_vec=words; // was: string2vector(s);
	for (int i=0; i < words.size(); i++) {
	    String wterm   =(String)tl_vec.elementAt(i);
	    String wordselt=(String)words.elementAt(i);
	    String word    =WNnormalize(wordselt);
	    System.out.println("W:" + word + " " + " " + wterm);
	    List<String> wstems=ws.findStems(word); // wordnet stemmer
	    List<String> sstems=ss.findStems(word); // s stemmer
	    String s0=WNsstem(word);
	    String s1=ps.stem(word); // unused
	    String s4=WNsstem(wterm);
	    Vector reselt=new Vector();
	    if (!reselt.contains(word))
		reselt.add(word);
	    for (int j=0; j < wstems.size(); j++) {
		String sj=(String)wstems.get(j);
		if (!reselt.contains(sj))
		    reselt.add(sj);
	    }
	    //
	    if (reselt.size()==0) { // alternative
		for (int j=0; j < sstems.size(); j++) {
		    String sj=(String)sstems.get(j);
		    if (!reselt.contains(sj))
			reselt.add(sj);
		}
	    }
	    //
	    if (word.endsWith("'s") || // ' cases
		word.endsWith("s'")) {
		String s2=word.substring(0, word.length()-2);
		System.out.println("s2:" + s2);
		if (!reselt.contains(s2))
		    reselt.add(s2);
	    }
	    //
	    if (word.endsWith("'")) {
		String s3=word.substring(0, word.length()-2);
		System.out.println("s3:" + s3);
		if (!reselt.contains(s3))
		    reselt.add(s3);
	    }
	    //
	    if (!reselt.contains(s0)) // 's cases (?)
		reselt.add(s0);
	    if (!reselt.contains(wterm))
		reselt.add(wterm);
	    //
	    if (!reselt.contains(s4))
		reselt.add(s4);
	    if (reselt.size()==0) // aggressive porter stemmer
	    	reselt.add(s1);
	    // System.out.println("reselt:" + reselt);
	    result.add(reselt);
	}
	return result;
    }
    
    public static Vector<Vector<String>> getPossibleSenses(Vector<Vector<String>> words) {
	Vector result=new Vector();
	String[] spos={"n", "a", "v", "r"};
	IIndexWord idxWord=null;
	String wordform="";
	for (int i=0; i < words.size(); i++) {
	    Vector<String> wordforms=(Vector<String>)words.elementAt(i);
	    Vector reselt=new Vector();
	    for (int j=0; j < wordforms.size(); j++) {
		wordform=(String)wordforms.elementAt(j);
		//System.out.println("--- " + wordform);
		// try all four POS
		for (int q=0; q<spos.length; q++) {
		    if (spos[q].equals("n")) {
			idxWord=dict.getIndexWord(wordform, POS.NOUN);
		    } else if (spos[q].equals("a")) {
			idxWord=dict.getIndexWord(wordform, POS.ADJECTIVE);
		    } else if (spos[q].equals("v")) {
			idxWord=dict.getIndexWord(wordform, POS.VERB);
		    } else { // if (spos[q].equals("R")) {
			idxWord=dict.getIndexWord(wordform, POS.ADVERB);
		    }
		    if (idxWord!=null) {
			List<IWordID> wordIDs = idxWord.getWordIDs();
			for (int k=0; k<wordIDs.size(); k++) {
			    IWordID iwordID=wordIDs.get(k);
			    ISynset synset =dict.getWord(iwordID).getSynset();
			    String gloss   =synset.getGloss();
			    String sense_word=Strings2SenseWord(wordform, spos[q], ""+k);
			    if (!reselt.contains(sense_word)) {
				//System.out.println(sense_word+
				//			   " "+iwordID+
				//			   " "+synset+
				//			   " | "+gloss);
				reselt.add(sense_word);
			    }
			}
		    }

		}
	    }
	    // fallback
	    if (reselt.size()==0) { // assume unknown words are propoer nouns
		String sense_word=Strings2SenseWord(wordform, "n", "?");
		reselt.add(sense_word);
	    }
	    result.add(reselt);
	}
	return result;
    }

    public static String SenseWord2Gloss(String sense) {
	System.out.println("# sense:" + sense);
	String gloss="-";
	IWord iword=getIWord(sense);
	System.out.println("iword:" + iword);
	if (iword!=null) {
	    ISynset synset =iword.getSynset();
	    System.out.println("iword:" + synset);
	    gloss   =synset.getGloss();
	}
	// String gloss="";
	// String[] winf=SenseWord2Strings(sense);
	// String swrd=winf[0];
	// String wpos=winf[1];
	// String wsen=winf[2];
	// int isen=Integer.parseInt(wsen)-1; //-
	// IIndexWord idxWord=null;
	// if (wpos.equals("n")) {
	//     idxWord=dict.getIndexWord(swrd, POS.NOUN);
	// } else if (wpos.equals("a")) {
	//     idxWord=dict.getIndexWord(swrd, POS.ADJECTIVE);
	// } else if (wpos.equals("v")) {
	//     idxWord=dict.getIndexWord(swrd, POS.VERB);
	// } else { // if (spos[q].equals("R")) {
	//     idxWord=dict.getIndexWord(swrd, POS.ADVERB);
	// }
	// if (idxWord!=null) {
	//     List<IWordID> wordIDs = idxWord.getWordIDs();
	//     IWordID iwordID=wordIDs.get(isen);
	    
	//     //	    for (int k=0; k<wordIDs.size(); k++) {
	//     //		IWordID iwordID=wordIDs.get(k);
	//     //		ISynset synset =dict.getWord(iwordID).getSynset();
	//     //		String gloss   =synset.getGloss();
	// }
	return gloss;
    }


    public static Vector<String> WNwsd_MostFrequentSense(String s) {
	Vector result=new Vector();
	Vector<String> tl_words=string2vector(s);
	System.out.println("Words:" + tl_words);
	Vector<Vector<String>> tl_wordforms=getWordForms(tl_words);
	System.out.println("Wordforms:" + tl_wordforms);
	Vector<Vector<String>> tl_sensewords=getPossibleSenses(tl_wordforms);
	System.out.println("Word senses:" + tl_sensewords);
	for (int i=0; i < tl_sensewords.size(); i++) {
	    Vector<String> words=(Vector<String>)tl_sensewords.elementAt(i);
	    result.add((String)words.elementAt(0)); // pick first
	}
	System.out.println("WSD (MFS):" + result);
	return result;
    }

    public static Vector<String> WNwsd_LESK(String s) {
	Vector res=new Vector();
	String[] spos={"n", "a", "v", "r"};
	Vector<String> tl_words=string2vector(s);
	System.out.println("Words:" + tl_words);
	Vector<Vector<String>> tl_wordforms=getWordForms(tl_words);
	System.out.println("Wordforms:" + tl_wordforms);
	Vector<Vector<String>> tl_sensewords=getPossibleSenses(tl_wordforms);
	System.out.println("Word senses:" + tl_sensewords);
	//	num_query_complexity=0;
	//for (int i=0; i<tl_sensewords.size(); i++) {
	//    Vector<String> senses=tl_sensewords.elementAt(i);
	//    num_query_complexity*=senses.size();
	//}
	Double sum_score;
	Double best_score;
	String best_word="";
	String last_word="";
	// for each word variant
	for (int w1=0; w1 < tl_wordforms.size(); w1++) {
	    Vector<String> wforms1=(Vector<String>)tl_wordforms.elementAt(w1);
	    Vector<String> senses=(Vector<String>)tl_sensewords.elementAt(w1);
	    best_score=-1.0D;
	    best_word=(String)wforms1.elementAt(0); // assuming one element exists!
	    best_word=Strings2SenseWord(best_word, "n", "?");
	    for (int i1=0; i1 < wforms1.size(); i1++) {
		String wform1=(String)wforms1.elementAt(i1);
		Double wform1_score=0D;
		if (stopwords.contains(wform1.toLowerCase())) {
		    System.out.println("skipping stopword w1:" + wform1);
		    ; // skip
		} else {
		    System.out.println("processing word w1:" + wform1);		    
		    for (int w2=0; w2 < tl_wordforms.size(); w2++) {
			if (w1!=w2) { // not for same word
			    Vector<String> wforms2=(Vector<String>)tl_wordforms.elementAt(w2);
			    // for each POS in N, A, V, Adv
			    for (int p=0; p < 4; p++) { // spos.length=4
				sum_score=0D;
				for (int i2=0; i2 < wforms2.size(); i2++) {
				    String wform2=(String)wforms2.elementAt(i2);
				    // compute similarity (WNlesk) to all other word variants
				    // System.out.println("-- " + wform1 + " " + wform2 + " " + spos[p]);
				    if (stopwords.contains(wform2.toLowerCase())) {
					// System.out.println("skipping stopword w2:" + wform2);
					;
				    } else {
					TreeMap<String, Double> scores=altnh.lesk(wform1, wform2, spos[p]);
					// System.out.println("ok0" + scores);
					for(String sk : scores.keySet()) {
					    String[] selts=StringUtils.split(sk, ",");
					    String wrd1=selts[0];
					    String wrd2=selts[1];
					    String[] winf1=SenseWord2Strings(wrd1);
					    String swrd=winf1[0];
					    String wpos=winf1[1];
					    String wsen=winf1[2];
					    int isen=Integer.parseInt(wsen)-1; // ok
					    Double score=scores.get(sk);
					    if (!wrd1.equals(last_word)) { // new word
						if (sum_score >= best_score) {
						    System.out.println("-" + 
								       Strings2SenseWord(swrd, wpos, ""+isen) +
								       " "+
						    //		       " | " + 
						    //		       SenseWord2Gloss(wrd1) + " " + 
						    //		       //w1 + " " + // s + "\t" + 
						    //		       //w2 + " " + score + " " + 
						    		       sum_score);
						    best_word =Strings2SenseWord(swrd, wpos, ""+isen); // best sense
						    best_score=sum_score;
						    sum_score=0D;
						}
					    }
					    sum_score+=score;
					    last_word=wrd1;
					    //System.out.println("ok1a");
					} // for sk
				    } // else
				} // for i2
			    } // for p
			} // if w1/w2
		    } // for w2
		} // else
	    }
	    // fallback to first sense
	    // System.out.println("ok1");
	    String[] binf=SenseWord2Strings(best_word);
	    String bwrd=binf[0];
	    String bpos=binf[1];
	    String bsen=binf[2];
	    if (bsen.equals("?")) {
		num_oov++;
		if (senses.size() > 0) {
		    best_word=(String)senses.elementAt(0);
		    //System.out.println("?-> " + best_word + " " + best_score);
		    // CNGLutils.pressKey();
		}
	    }
	    // System.out.println("ok2");
	    //System.out.println("L-> " + best_word + " " + best_score);
	    // CNGLutils.pressKey();
	    res.add(best_word);
	    String gloss="-";
	    if (!bpos.equals("?") &&
		!bsen.equals("?")) {
		gloss=SenseWord2Gloss(best_word);
	    }
	    // System.out.println("ok3");
	    System.out.println("--" + best_word + " | " + gloss);
	}
// save result with highest similarity
	System.out.println("WSD (LESK):" + res);
	return res;
    }

    public static Vector<String> WNwsd_GOLD(String s) {
	Vector result=new Vector();
	String gold_file="wn_wsd_gold.txt";
	HashMap wsenses=new HashMap();
	try {
	    BufferedReader reader=new BufferedReader(new FileReader(gold_file));
	    String line="";
	    while (null!=(line=reader.readLine())) {
		if (line.charAt(0)=='#') {
		    ; // skip
		} else {
		    String[] lelts=StringUtils.split(line, " ");
		    String w1=lelts[0]; // "QID":qid
		    String w2=lelts[1]; // orig
		    String w3=lelts[2]; // sense word
		    String[] w1elts=StringUtils.split(w1, ":");
		    String wqid=w1elts[1];
		    String wsense=w3;
		    String[] welts=SenseWord2Strings(wsense);
		    String wword=welts[0];
		    // String wsen =welts[2];
		    if (wqid.equals(qid)) {
			System.out.println(wqid + " qid:" + qid + " s:" + wsense + " w:" + wword + " " );
			//senses.put(wword, wsense);
			wsenses.put(wword, wsense);
			//if (wsen.equals("?")) { // add OOV directly
			//    result.add(wsense);
			//}
		    }
		}
	    }
	} catch (Exception e) {
	    System.out.println("ERROR: " + e);
	}
	Vector<String> tl_words=string2vector(s);
	System.out.println("Words:" + tl_words);
	Vector<Vector<String>> tl_wordforms=getWordForms(tl_words);
	System.out.println("Wordforms:" + tl_wordforms);
	Vector<Vector<String>> tl_sensewords=getPossibleSenses(tl_wordforms);
	System.out.println("Word senses:" + tl_sensewords);
	// for each word variant
	for (int w1=0; w1 < tl_wordforms.size(); w1++) {
	    Vector<String> wforms1=(Vector<String>)tl_wordforms.elementAt(w1);
	    Vector<String> senses=(Vector<String>)tl_sensewords.elementAt(w1);
	    System.out.println("w:" + senses);
	    String gold_sense=null;
	    for (int j=0; j<wforms1.size(); j++) {
		String cform=(String)wforms1.elementAt(j);
		String wsense=(String)wsenses.get(cform);
		if (wsense!=null) {
		    gold_sense=wsense;
		    break;
		}
	    }
	    if (gold_sense==null) {
		System.out.println(qid + " wword:" + senses + " not found");
		CNGLutils.pressKey();
	    }
	    result.add(gold_sense);
	}
	System.out.println("WSD (GOLD):" + result);
	return result;

}


    public static String WNstem(String word) {
        List<String> stems=ws.findStems(word);
	String stem;
        if (stems!=null &&
            stems.size() > 0) {
	    System.out.println("stems:" + stems);
            stem=stems.get(0);
        } else {
            stem=word;

        }
        return stem;
    }

    public static String WNsstem(String word) {
        String wsuf="";
        int wordLength = word.length();
        if (wordLength <= 3) {
            return word;
        }
        // stemmer
        if (word.endsWith("ies") &&
            wordLength > 4 &&
            !(word.endsWith("eies") ||
              word.endsWith("aies")) ) {
            wordLength-=3;
            wsuf ="y"; // return word.substring(0, wordLength-3)+"y";
        } else if (word.endsWith("es") &&
                   wordLength > 3 &&
                   !(word.endsWith("aes") ||
                     word.endsWith("ees") ||
                     word.endsWith("oes")) ) {
            wordLength-=1;
        } else if (word.endsWith("s") &&
                   wordLength > 2 &&
                   !(word.endsWith("us") ||
                     word.endsWith("ss")) ) {
            wordLength-=1;
        }
        word=word.substring(0, wordLength)+wsuf;
        return word;
    }

    public static Vector<String> WNgetWordForms(String wrd) {
	Vector wforms=new Vector();
	String word1=WNnormalize(wrd);// original; 
	// keep 's for cases such as Alzheimer's	
	String word2=WNsstem(word1);  // S stemmer
	String word3;
	wforms.add(word1);
	if (!wforms.contains(word2))
	    wforms.add(word2);
        List<String> stems=ws.findStems(word1);
        if (stems!=null &&
            stems.size() > 0) {
	    for (int i=0; i < stems.size(); i++) {
		word3=stems.get(i);
		if (!wforms.contains(word3))
		    wforms.add(word3);
	    }
	}
	System.out.println("- word forms:" + wrd + " -> "+ wforms);
	return wforms;
    }

//     public static Vector WNwsdLESK(Vector words) {
// 	Vector res=new Vector();
// 	String[] spos={"n", "a", "v", "r"};
// 	Vector word_vars=new Vector(); // save all variants
// 	for (int i=0; i < words.size(); i++) {
// 	    Vector<String> wforms=WNgetWordForms((String)words.elementAt(i));
// 	    word_vars.add(wforms);
// 	}
// 	Double sum_score;
// 	Double best_score;
// 	String best_word="";
// 	String best_glss="";
// 	String last_word="";
// 	// for each word variant
// 	for (int i1=0; i1 < word_vars.size(); i1++) {
// 	    Vector<String> wforms1=(Vector<String>)word_vars.elementAt(i1);
// 	    best_score=0D;
// 	    best_word="";
// 	    for (int j1=0; j1 < wforms1.size(); j1++) {
// 		String wform1=(String)wforms1.elementAt(j1);
// 		Double wform1_score=0D;
// 		for (int i2=0; i2 < word_vars.size(); i2++) {
// 		    if (i1!=i2) { // not for same word
// 			Vector<String> wforms2=(Vector<String>)word_vars.elementAt(i2);
// 			// for each POS in N, A, V, Adv
// 			for (int p=0; p < 4; p++) {
// 			    sum_score=0D;
// 			    for (int j2=0; j2 < wforms2.size(); j2++) {
// 				String wform2=(String)wforms2.elementAt(j2);
// 				// compute similarity (WNlesk) to all other word variants
// 				// System.out.println("-- " + wform1 + " " + wform2 + " " + spos[p]);
// 				TreeMap<String, Double> scores=altnh.lesk(wform1, wform2, spos[p]);
// 				for(String s : scores.keySet()) {
// 				    String[] selts=StringUtils.split(s, ",");
// 				    String w1=selts[0];
// 				    String w2=selts[1];
// 				    Double score=scores.get(s);
// 				    //System.out.println("-" + w1 + " " + // s + "\t" + 
// 					//		       w2 + " " + score + " " + sum_score);
// 				    if (w1.equals(last_word)) { // sum up
// 					sum_score+=score;
// 					if (sum_score >= best_score) {
// 					    best_word =w1; // best sense
// 					    best_score=sum_score;
// 					}
// 				    } else { // save best
// 					sum_score=score; // for next sense
// 					last_word=w1;
// 					if (sum_score >= best_score) {
// 					    best_word =w1; // best sense
// 					    best_score=sum_score;
// 					}
// 				    }
				    
// 				}
// 			    }
// 			}
// 		    }
// 		}
// 	    }
// 	    //
	    
// 	    IIndexWord bword = null;
// 	    String[] binf=SenseWord2Strings(best_word);
// 	    String bwrd=binf[0];
// 	    String bpos=binf[1];
// 	    String bsen=binf[2];
// 	    int isense =Integer.parseInt(bsen); //-
// 	    System.out.println(""+ bwrd + " " + bpos + " " + bsen);
// 	    if (bpos.equalsIgnoreCase("n")) {
// 		bword = dict.getIndexWord(bwrd, POS.NOUN);
// 	    }
// 	    if (bpos.equalsIgnoreCase("v")) {
// 		bword = dict.getIndexWord(bwrd, POS.VERB);
// 	    }
// 	    if (bpos.equalsIgnoreCase("a")) {
// 		bword = dict.getIndexWord(bwrd, POS.ADJECTIVE);
// 	    }
// 	    if (bpos.equalsIgnoreCase("r")) {
// 		bword = dict.getIndexWord(bwrd, POS.ADVERB);
// 	    }
// 	    if (!bsen.equals("?")) {
// 		List<IWordID> bwordIDs = bword.getWordIDs();
// 		// System.out.println(bwordIDs.size() + "  " + bwordIDs);
// 		IWordID bwordID=bwordIDs.get(isense-1); //-
// 		// System.out.println(bwordID);
// 		IWord iword;
// 		ISynset synos;
// //		iwordID = idxWord.getWordIDs().get(sense); // first sense
// 		if (bwordID!=null) {
// 		    iword= dict.getWord(bwordID);
// 		    synos = iword.getSynset();
// 		    System.out.println(synos);
// 		    best_glss=synos.getGloss(); 
// 		}


// 	    }
// // 	    IWordID iwordID=wordIDs.get(k);
// 	    System.out.println("-> " + best_word + " " + best_score + " | " + best_glss);
// 	    res.add(best_word);
// 	}
//         // save result with highest similarity
// 	return res;
//     }

    public int getOOV() {
	return this.num_oov;
    }

    public static String WNnormalize(String word) {
        word=word.toLowerCase();
        word=word.replace(' ', '_');
        int olen  =word.length();
        int br_pos=olen;
//        for (int i=olen-1; i > 0; i--) {
//              if (word.charAt(i)=='-' ||
//                  word.charAt(i)=='_' ||
//                  word.charAt(i)=='.' ||
//                  word.charAt(i)==' ') {
//                  br_pos=i+1;
//                  break;
//              }
//        }
//        if (word.endsWith("'s") ||
//            word.endsWith("s'")) {
//            br_pos=word.length()-2;
//        }
        String left=word.substring(0, br_pos);
        return left;
    }




    public void WNtest(String filename) {
	String line;
	int cnt_all=0, cnt_mpos=0, cnt_mneg=0;
	int cnt_ppos=0, cnt_pneg=0, cnt_spos=0, cnt_sneg=0;
        try {
	    BufferedReader reader=new BufferedReader(new FileReader(filename));
            while (null!=(line=reader.readLine())) {
		cnt_all++;
		String[] elts=StringUtils.split(line, " ");
		System.out.println("elt:" + line);
		String qid    = elts[0].trim();
                String orig   = elts[1].trim();
                String wrd    = elts[2].trim();
                String wn_elt = elts[3].trim();
		String[] welts=SenseWord2Strings(wn_elt);
		String wn_wrd =welts[0];
		wn_wrd=WNnormalize(wn_wrd);
		String wn_pos =welts[1];
		String wn_sen =welts[2];
		String melt   =""; // WNwsd(orig);
		String[] melts=SenseWord2Strings(melt);
		String m_wrd  =melts[0];
		String m_pos  =melts[1];
		String m_sen  =melts[2];
		System.out.println("p1:" + wn_pos + " p2:" + m_pos +
				   " s1:" + wn_sen + " s2:" + m_sen);
		if (wn_wrd.equals(m_wrd)) {
		    cnt_mpos++;
		    if (wn_pos.equals(m_pos)) {
			// check pos and sense
			cnt_ppos++;
			if (wn_sen.equals(m_sen)) {
			    cnt_spos++;
			} else {
			    cnt_sneg++;
			}
		    } else {
			cnt_pneg++;
		    }
		} else {
		    System.out.println("MISMATCH orig:" + wn_wrd + " m:" + m_wrd + " " + melt);
		    cnt_mneg++;
		}
		System.out.println("qid:" + qid +
				   " o:" + orig +
				   " w:" + wn_wrd +
				   " p:" + wn_pos +
				   " s:" + wn_sen);
	    }
            reader.close();
	} catch (IOException e) {
	    System.out.println("ERROR e" + e);
        }
	System.out.println("all:" + cnt_all +
			   " mp:" + cnt_mpos +
			   " mn:" + cnt_mneg + "\n" +
			   " pp:" + cnt_ppos +
			   " pn:" + cnt_pneg + "\n" +
			   " sp:" + cnt_spos + 
			   " sn:" + cnt_sneg +
			   "");
    }

    public static void WNdisplaySenses(String qid, String word, String pos, IIndexWord idxWord) {
	String orig=word; // t.getOrigTerm();
	boolean isOrig=true; // t.getOrig();
	int sense=0;
	IWordID iwordID = null;
	IWord   iword   = null;
	// synset
	if (isOrig==true &&
	    idxWord!=null) {
	    for (int k=0; k < idxWord.getTagSenseCount() ; k++) {
		iwordID = idxWord.getWordIDs().get(k); // first sense
		if (iwordID!=null) {
		    iword= dict.getWord(iwordID);
		}
		ISynset synos = iword.getSynset();
		// System.out.print("\n__ " + wrd + " " + wordID + " (");
		String lemma=iword.getLemma();
		System.out.print("\n__ "  + qid + " " + isOrig + " '" +
				 orig + "' " + word + " " + lemma + "#" + pos + "#" + k +
				 " | " + synos.getGloss() + "\n");
	      
	    }
	} else {
	    System.out.print("\n__ "  + qid + " " + isOrig + " '" +
			     orig + "' " + word + " " + word + "#" + pos + "#" + "0" +
			     " | " + "--" + "\n");
	}
    }


    public static Vector<String> getGlossVector(IWord iword) {
	ISynset synos=iword.getSynset();
	String gloss =synos.getGloss();
	return string2vector(gloss);
    }

    public static Vector<String> getSynonymVector(IWord iword) {
	Vector<String> result=new Vector<String>();
	ISynset synos=iword.getSynset();
	for (IWord w : synos.getWords()) {
	    System.out.print(w.getLemma() + " ");
	    String var=w.getLemma();
	    var=WNsstem(var); // was: snowball_stem(var);
	    result.add(var);
	} 
	return result;
    }

    public static Vector<String> getHypernymVector(IWord iword) {
	return getRelated(iword, Pointer.HYPERNYM);
    }

    public static Vector<String> getHyponymVector(IWord iword) {
	return getRelated(iword, Pointer.HYPONYM);
    }

    public static Vector<String> getLexicalVector(IWord iword) {
	return getRelated(iword, Pointer.DERIVATIONALLY_RELATED);
    }

    public static Vector<String> getRelated(IWord iword, Pointer type) {
	Vector<String> result=new Vector<String>();
	ISynset synos=iword.getSynset();
	List<ISynsetID> hypernyms=
	    synos.getRelatedSynsets(type);
	List<IWord> words;
	for (ISynsetID sid : hypernyms) {
	    words = dict.getSynset(sid).getWords();
	    for (Iterator<IWord> i=words.iterator(); i.hasNext();) {
		String var=i.next().getLemma();
		var=WNsstem(var); // was: snowball_stem(var);
		result.add(var);
	    }
	}
	return result;
    }


    public static IWord getIWord(String sense_word) {
	IIndexWord idxWord;
	IWordID wordID = null;
	IWord   word   = null;
	String[] sw =SenseWord2Strings(sense_word);
	String wrd  =sw[0];
	String pos  =sw[1];
	String sense=sw[2];
	int isense=Integer.parseInt(sense); // -1; //?
	if (pos.equals("n"))
	    idxWord=dict.getIndexWord(wrd, POS.NOUN);
	else if (pos.equals("v"))
	    idxWord=dict.getIndexWord(wrd, POS.VERB);
	else if (pos.equals("a"))
	    idxWord=dict.getIndexWord(wrd, POS.ADJECTIVE);
	else if (pos.equals("r"))
	    idxWord=dict.getIndexWord(wrd, POS.ADVERB);
	else 
	    idxWord=null;

	if (idxWord!=null) {
	    wordID = idxWord.getWordIDs().get(isense);
	    if (wordID!=null) {
		word= dict.getWord(wordID);
	    }
	}
	return word;
    }

    public static Vector<String> getAllHyponymsVector(IWord iword, int level) {
	Vector result=new Vector();
	Vector hypos=getHyponymVector(iword);
	ISynset synos=iword.getSynset();
	List<ISynsetID> hypernyms=
	    synos.getRelatedSynsets(Pointer.HYPONYM);
	if (hypernyms==null) {
	    return null;
	} else {
	    List<IWord> words;
	    for (ISynsetID sid : hypernyms) {
		words = dict.getSynset(sid).getWords();
		for (Iterator<IWord> i=words.iterator(); i.hasNext();) {
		    IWord hyper=i.next();
		    String var=hyper.getLemma();
		    var=WNsstem(var); // was: snowball_stem(var);
		    Double weight=100*Math.pow(0.7, level);
		    int iweight=weight.intValue();
		    if (!result.contains(var))
			result.add(var+" "+iweight);
		    Vector subhypers=getAllHyponymsVector(hyper, level+1);
		    for (int j=0; j < subhypers.size(); j++) {
			String hypervar=(String)subhypers.elementAt(j);
			if (!result.contains(hypervar))
			    result.add(hypervar);
		    }
		}
	    }
	}
	return result;
    }
	    
    //
    public static void WNsave_glosses(IDictionary dict) {
	int cnt=0;
	try {
            FileWriter bfos    = new FileWriter("gloss_words.txt", true); // append
            BufferedWriter bout = new BufferedWriter(bfos);
	    for (Iterator<ISynset> i=dict.getSynsetIterator(POS.NOUN); i.hasNext();) {
		ISynset synset = i.next(); 
		cnt++;
		String glss   =synset.getGloss();
		Vector words=string2vector(glss);
		try {
		    for (int i1=0; i1 < words.size(); i1++) {
			String word=(String)words.elementAt(i1);
			word=word.replace(' ', '_');
			bout.write(word+"\n");
		    }
		} catch (Exception e) {
		    ;
		}
	    }
	    //
	    for (Iterator<ISynset> i=dict.getSynsetIterator(POS.ADJECTIVE); i.hasNext();) {
		ISynset synset = i.next();
		cnt++;
		String glss   =synset.getGloss();
		Vector words=string2vector(glss);
		try {
		    for (int i2=0; i2 < words.size(); i2++) {
			String word=(String)words.elementAt(i2);
			word=word.replace(' ', '_');
			bout.write(word+"\n");
		    }
		} catch (Exception e) {
		    ;
		}
	    }
	    //
	    for (Iterator<ISynset> i=dict.getSynsetIterator(POS.VERB); i.hasNext();) {
		ISynset synset = i.next();
		cnt++;
		String glss   =synset.getGloss();
		Vector words=string2vector(glss);
		try {
		    for (int i2=0; i2 < words.size(); i2++) {
			String word=(String)words.elementAt(i2);
			word=word.replace(' ', '_');
			bout.write(word+"\n");
		    }
		} catch (Exception e) {
		    ;
		}
	    }
	    for (Iterator<ISynset> i=dict.getSynsetIterator(POS.ADVERB); i.hasNext();) {
		ISynset synset = i.next();
		cnt++;
		String glss   =synset.getGloss();
		Vector words=string2vector(glss);
		try {
		    for (int i2=0; i2 < words.size(); i2++) {
			String word=(String)words.elementAt(i2);
			word=word.replace(' ', '_');
			bout.write(word+"\n");
		    }
		} catch (Exception e) {
		    ;
		}
		
	    }
	    bout.close();
            bfos.close();
	} catch (IOException e) {
            System.out.println("ERROR: saveVocab:" + e);
        }
	System.out.println("gloss count:" + cnt);
	CNGLutils.pressKey();

    }

    public static void main (String[] args) throws IOException {
	WordNetWSD wnw=new WordNetWSD("data/stoplist.txt");
	// wnw.WNtest("wn_wsd_gold.txt");
	// WNsave_glosses(dict);
	String t1="I made an 90 appointment, with #, ?, 20/11\2012 my bank manager to discuss the credit card application. the manager approved the application form and I can withdraw money now";
	Vector<String> t1words=string2vector(t1);
	Vector t1v=wnw.WNwsd_LESK(t1);
	System.out.println(t1words);
	System.out.println(t1v);

    }
}
