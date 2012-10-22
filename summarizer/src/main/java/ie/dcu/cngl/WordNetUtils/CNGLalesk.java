//package edu.sussex.nlp.jws;
/*
fix sense: -1 for error
fix others: not -1
addRelatedTerms(CNGLtermList tl) : add WN related terms to tl
Vector WSD(String type, Vector terms, Vector context) : perform WSD on Vector of terms
- String most_frequent_sense: use first sense
- String LESK: adapted Lesk
- String LESK_BRF: use BRF terms as context

WNpairs WNrelatedTerms(String wn_sense):
return List of pairs (wn_sense , sim) for WSD term

Vector String2WNstring: return all possible WN senses

check WNtest: accuracy of WSD
 */


package ie.dcu.cngl.WordNetUtils;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import java.net.*;
import edu.mit.jwi.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Hashtable;
import java.util.TreeMap;
import java.text.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;
import java.io.*;
import edu.mit.jwi.morph.WordnetStemmer;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;

// 'AdaptedLeskTanimotoNoHyponyms' -- !
// David Hope, 2008, University Of Sussex
public class CNGLalesk {
    private IDictionary dict =	null;	// WordNet
    private NumberFormat formatter = new DecimalFormat("0.0000");// pretty up the numbers
    private Pattern     p     = null; // word finding
    private Matcher     m     = null;
    private WordnetStemmer stemmer  = null;
    private Set stoplist   = null;
    public HashMap<String, Double> cache_map;
// have the (Ted Pedersen) stop list here!)
//    private String     list    = "a aboard about above across after against all along alongside although amid amidst among amongst an and another anti any anybody anyone anything around as astride at aught bar barring because before behind below beneath beside besides between beyond both but by circa concerning considering despite down during each either enough everybody everyone except excepting excluding few fewer following for from he her hers herself him himself his hisself i idem if ilk in including inside into it its itself like many me mine minus more most myself naught near neither nobody none nor nothing notwithstanding of off on oneself onto opposite or other otherwise our ourself ourselves outside over own past pending per plus regarding round save self several she since so some somebody someone something somewhat such suchlike sundry than that the thee theirs them themselves there they thine this thou though through throughout thyself till to tother toward towards twain under underneath unless unlike until up upon us various versus via vis-a-vis we what whatall whatever whatsoever when whereas wherewith wherewithal which whichever whichsoever while who whoever whom whomever whomso whomsoever whose whosoever with within without worth ye yet yon yonder you you-all yours yourself";
    
    public String cache_map_file="data/cache.db";

    public CNGLalesk(IDictionary dict, Set stopwords) {
	System.out.println("... Adapted Lesk (2)");
	
	this.dict   =  dict;
	//System.out.println("WordNet " + dict.getVersion());
	p = Pattern.compile("[a-zA-Z-_]+"); // word finder, not perfect, but what is?!
	stemmer   = new WordnetStemmer(dict); // get 'base' form of a word
	stoplist   = stopwords; // new ArrayList<String>();
//	getStopWords();
	cache_map=new HashMap<String, Double>();
	// load from db file
	try {
	    BufferedReader reader=new BufferedReader(new FileReader(cache_map_file));
	    String line="";
	    while (null!=(line=reader.readLine())) {
		if (line.charAt(0)=='#') {
		    ; // skip
		} else {
		    String[] keyval=StringUtils.split(line, "=");
		    String key=keyval[0];
		    String val=keyval[1];
		    cache_map.put(key, Double.parseDouble(val));
		}
	    }
	}
	catch (Exception e) {
	    System.out.println("ERROR: loading file " +e);
	}

    }

    public void fill_cache(String filename) {
	try {
            FileWriter bfos    = new FileWriter(filename, false); // append
            BufferedWriter bout = new BufferedWriter(bfos);
	    Iterator<IIndexWord> iwi1;
	    Iterator<IIndexWord> iwi2;
	    for (iwi1=dict.getIndexWordIterator(POS.NOUN); iwi1.hasNext(); ) {
		IIndexWord iword1=iwi1.next();
		String word1=iword1.getLemma();
		for (iwi2=dict.getIndexWordIterator(POS.NOUN); iwi2.hasNext(); ) {
		    IIndexWord iword2=iwi2.next();
		    String word2=iword2.getLemma();
		    TreeMap<String, Double> scores=lesk(word1, word2, "n");
		    for(String sk : scores.keySet()) {
			String[] selts=StringUtils.split(sk, ",");
			String wrd1=selts[0];
			String wrd2=selts[1];
			String[] winf1=StringUtils.split(wrd1, "#"); // SenseWord2Strings(wrd1);
			String swrd1=winf1[0];
			String wpos1=winf1[1];
			String wsen1=winf1[2];
			String[] winf2=StringUtils.split(wrd2, "#"); // SenseWord2Strings(wrd1);
			String swrd2=winf2[0];
			String wpos2=winf2[1];
			String wsen2=winf2[2];
			// int isen=Integer.parseInt(wsen)-1; // ok
			Double score=scores.get(sk);
			if (score > 0) {
			    System.out.println("## " + wrd1+"," + // "#"+"n"+"#"+wsen1+","+
					       wrd2+// "#"+"n"+"#"+wsen2+
					       "="+score);
			    bout.write(wrd1+//"#"+"n"+"#"+wsen1+","+
				       ","+ 
				       wrd2+//"#"+"n"+"#"+wsen2+
				       "="+score+"\n");
			}
		    }
		}
	    }
			    
	    bout.close();
	    bfos.close();
	} catch (Exception e) {
	    ;
	}
    }
	
    

    
// lesk(1)
    public double lesk(String w1, int s1, String w2, int s2, String pos) {
	double    lesk   = 0.0;
	IIndexWord word1 = null;
	IIndexWord  word2 = null;
// get the WordNet words in the right part of speech
	if(pos.equalsIgnoreCase("n")) {
	    word1 = dict.getIndexWord(w1, POS.NOUN);
	    word2 = dict.getIndexWord(w2, POS.NOUN);
	}
	if(pos.equalsIgnoreCase("v")) {
	    word1 = dict.getIndexWord(w1, POS.VERB);
	    word2 = dict.getIndexWord(w2, POS.VERB);
	}
	if(pos.equalsIgnoreCase("a")) {
	    word1 = dict.getIndexWord(w1, POS.ADJECTIVE);
	    word2 = dict.getIndexWord(w2, POS.ADJECTIVE);
	}
	if(pos.equalsIgnoreCase("r")) {
	    word1 = dict.getIndexWord(w1, POS.ADVERB);
	    word2 = dict.getIndexWord(w2, POS.ADVERB);
	}
// [error check]: check that the words exist in WordNet
	if(word1 == null) {
	    System.out.println(w1 + "(" + pos + ") not found in WordNet " + dict.getVersion());
	    return(0); // 0 is an error code
	}
	if(word2 == null) {
	    System.out.println(w2 + "(" + pos + ") not found in WordNet " + dict.getVersion());
	    return(0); // 0 is an error code
	}
// [error check]: check the sense numbers are not greater than the true number of senses in WordNet
	List<IWordID> word1IDs = word1.getWordIDs();
	List<IWordID> word2IDs = word2.getWordIDs();
	if(s1 >  word1IDs.size()) {
	    System.out.println(w1 + " sense: " + s1 + " not found in WordNet " + dict.getVersion());
	    return(0); // 0 is an error code
	}
	if(s2 > word2IDs.size()) {
	    System.out.println(w2 + " sense: " + s2 + " not found in WordNet " + dict.getVersion());
	    return(0); // 0 is an error code
	}
// ...........................................................................................................................................
// get the {synsets}
	IWordID word1ID = word1.getWordIDs().get(s1 - 1); // get the right sense of word 1
	ISynset  synset1  = dict.getWord(word1ID).getSynset();
	
	IWordID word2ID = word2.getWordIDs().get(s2 - 1); // get the right sense of word 2
	ISynset  synset2  = dict.getWord(word2ID).getSynset();
// ...........................................................................................................................................
// get *all* the 'Pointers': both 'lexical' and 'semantic Pointers. These are the synsets that are
// related to a synset Include the synset itself in this set
	
// set 1.
	HashSet<ISynsetID>  set1 = new HashSet<ISynsetID>();
	set1.add(synset1.getID());
	set1.addAll(getPointers(synset1));
	
// set 2
	HashSet<ISynsetID>  set2 = new HashSet<ISynsetID>();
	set2.add(synset2.getID());
	set2.addAll(getPointers(synset2));
// ...........................................................................................................................................
// get all the words in each 'Pointer' set, for each word(sense) -- **allow duplicates**
// these are checke against the stop list and also 'lemmatised'
	//ArrayList<String> supergloss1 = getSuperGloss(set1);
	//ArrayList<String> supergloss2 = getSuperGloss(set2);
	Hashtable<String, Integer> supergloss1 = getSuperGloss(set1);
	Hashtable<String, Integer> supergloss2 = getSuperGloss(set2);
// ...........................................................................................................................................
// create a 'basis' set for the vectors
	HashSet<String> basis    = new HashSet<String>();
	basis.addAll(supergloss1.keySet());
	basis.addAll(supergloss2.keySet());
// build {vectors} for each word(sense)
	Vector<Double> v1    = getVector(basis, supergloss1);
	Vector<Double> v2    = getVector(basis, supergloss2);
// get score
	lesk           = jaccard_tanimoto(v1,v2);
	return ( lesk );
    }
    
    private Vector<Double> getVector(HashSet<String> basis, Hashtable<String, Integer> supergloss) { 
	Vector<Double> vector = new Vector<Double>();
	for(String w : basis) {
	    if(supergloss.containsKey(w)) {
		vector.add((double)supergloss.get(w));
	    } else {
		vector.add(0.0);
	    } 
	}
	return ( vector );
    }
    
// Jaccard Tanimoto methods .......................................................................................................................................
    private double dot_product(Vector<Double> v1, Vector<Double> v2) {
	    double dot   = 0.0;
	    double v1Value =  0.0;
	    double v2Value = 0.0;
	    
	    for (int i = 0; i < v1.size(); i++) {
		v1Value = v1.get(i);
		v2Value = v2.get(i);
		if(v1Value> 0.0 && v2Value > 0.0)
		    dot    += ( v1Value * v2Value );
	    }
	    return ( dot );
    }
    private double lengthOfVector(Vector<Double> v) {
	double  length = 0.0;
	for (int i = 0; i < v.size(); i++) {
	    double value = v.get(i);
	    if(value > 0.0)
		length += ( value * value );
	}
	if(length == 0.0) {
	    return ( 0.0 );
	}
	return ( Math.sqrt(length) );
    }
    public double jaccard_tanimoto(Vector<Double> v1, Vector<Double> v2) {
	double dot_product = dot_product(v1, v2);
	double lengthV1  = Math.pow(lengthOfVector(v1), 2d);
	double lengthV2  = Math.pow(lengthOfVector(v2), 2d);
	if(dot_product == 0.0)
	    return ( 0.0 );
	if((lengthV1 + lengthV2 - ( dot_product )) == 0.0)
	    return ( 0.0 );
	return ( dot_product / (lengthV1 + lengthV2 - ( dot_product )) );
    }
// Jaccard Tanimoto methods .......................................................................................................................................
    
// get *all* the words in *all* [glosses] related to a word(sense) -- allow duplicate words
// *no stop words*; lemmatise words to 'base' form
    private Hashtable<String, Integer> getSuperGloss(HashSet<ISynsetID> set) {
	Hashtable<String, Integer> supergloss = new Hashtable<String, Integer>();
	for(ISynsetID i : set) {
	    String gloss = dict.getSynset(i).getGloss();
	    //System.out.println("gloss:" + gloss);
	    m = p.matcher(gloss);
	    while(m.find()) {
		String word = m.group().trim();
		//
		if(!stoplist.contains(word)) { // check the stoplist
		    List<String> baseforms = stemmer.findStems(word); // check the base forms; convert to base forms
		    if(!baseforms.isEmpty()) {
			if(baseforms.contains(word)) {
			    //System.out.println("w:" + word);
			    if(supergloss.containsKey(word)) {
				int c = supergloss.get(word);
				c++;
				supergloss.put(word, c);
			    } else {
				supergloss.put(word, 1);
			    }
			} else {
			    for(String bw : baseforms) {
				if(supergloss.containsKey(bw)) {
				    int c = supergloss.get(bw);
				    c++;
				    supergloss.put(bw, c);
				} else {
				    supergloss.put(bw, 1);
				}
			    }
			}
		    }
		}
	    }
	}
	return ( supergloss );
    }
    
// get *all* Pointers for a synset
// if a Pointer is of type: <hypernym> the get all the immediate <hyponyms> of that <hypernym>
// i.e. *all* the sub types
    private HashSet<ISynsetID> getPointers(ISynset synset) {
	HashSet<ISynsetID>        pointers = new HashSet<ISynsetID>();
// 1. lexical
	pointers.addAll(synset.getRelatedSynsets());
// 2. semantic
	Map<IPointer, List<ISynsetID>>    map   = synset.getRelatedMap(); // !!!
	for(IPointer p : map.keySet()) {
	    pointers.addAll(map.get(p));
	}
	return ( pointers );
    }
    
    // get stop words ( Ted Pedersens's list)
//    private void getStopWords() {
//	String[] editor = list.split("\\s");
//	for(int i = 0; i < editor.length; i++) {
//	    stoplist.add(editor[i]);
//	}
    //   }
    
    // lesk(2) all senses
    public TreeMap<String, Double> lesk(String w1, String w2, String pos) {
	// sour#pos#sense sour#pos#sense  leskscore
	TreeMap<String, Double> map = new TreeMap<String, Double>();
	
	IIndexWord word1 = null;
	IIndexWord  word2 = null;
// get the WordNet words
// get the WordNet words in the right part of speech
	if(pos.equalsIgnoreCase("n")) {
	    word1 = dict.getIndexWord(w1, POS.NOUN);
	    word2 = dict.getIndexWord(w2, POS.NOUN);
	}
	if(pos.equalsIgnoreCase("v")) {
	    word1 = dict.getIndexWord(w1, POS.VERB);
	    word2 = dict.getIndexWord(w2, POS.VERB);
	}
	if(pos.equalsIgnoreCase("a")) {
	    word1 = dict.getIndexWord(w1, POS.ADJECTIVE);
	    word2 = dict.getIndexWord(w2, POS.ADJECTIVE);
	}
	if(pos.equalsIgnoreCase("r")) {
	    word1 = dict.getIndexWord(w1, POS.ADVERB);
	    word2 = dict.getIndexWord(w2, POS.ADVERB);
	}
// [error check]: check the words exist in WordNet
	if(word1 != null && word2 != null) {
// get the lesk scores for the (sense pairs)
	    List<IWordID> word1IDs = word1.getWordIDs(); // all senses of word 1
	    List<IWordID> word2IDs = word2.getWordIDs(); // all senses of word 2
	    int sx = 1;
	    ISynset synset1 = null;
	    ISynset synset2 = null;
	    for(IWordID idX : word1IDs) {
		int sy = 1;
		for(IWordID idY : word2IDs) {
		    String s1=w1+"#"+pos+"#"+sx;
		    //System.out.println("s1:" + s1);
		    String s2=w2+"#"+pos+"#"+sy;
		    //System.out.println("s2:" + s2);
		    Double leskscore = cache_map.get(s1+","+s2);
		    //System.out.println("cs:" + leskscore);
		    if (leskscore==null) {
			leskscore=lesk(w1, sx, w2, sy, pos);
			cache_map.put(s1+","+s2, leskscore); // save for this session
			try { // write to file for next session
			    FileWriter bfos    = new FileWriter(cache_map_file, true); // append
			    BufferedWriter bout = new BufferedWriter(bfos);
			    bout.write(s1+","+s2+"="+leskscore+"\n");
			    bout.close();
			    bfos.close();
			} catch (Exception e) {
			    System.out.println("ERROR: writing file " + cache_map_file);
			}
		    }
		    map.put((w1 + "#" + pos + "#" + sx + "," + w2 + "#" + pos + "#" + sy), leskscore);
		    sy++;
		}
		sx++;
	    }
	} else {
	    return ( map );
	}
	// System.out.println("ok");
	return ( map );
    }
    
    
// // lesk(3) all senses of word 1 vs. a specific sense of word 2
//     public TreeMap<String, Double> lesk(String w1, String w2, int s2, String pos) {
// 	// sour#pos#sense sour#pos#sense  leskscore
// 	TreeMap<String, Double> map = new TreeMap<String, Double>();
	
// 	IIndexWord word1 = null;
// 	IIndexWord  word2 = null;
// // get the WordNet words
// // get the WordNet words in the right part of speech
// 	if(pos.equalsIgnoreCase("n")) {
// 	    word1 = dict.getIndexWord(w1, POS.NOUN);
// 	    word2 = dict.getIndexWord(w2, POS.NOUN);
// 	}
// 	if(pos.equalsIgnoreCase("v")) {
// 	    word1 = dict.getIndexWord(w1, POS.VERB);
// 	    word2 = dict.getIndexWord(w2, POS.VERB);
// 	}
// 	if(pos.equalsIgnoreCase("a")) {
// 	    word1 = dict.getIndexWord(w1, POS.ADJECTIVE);
// 	    word2 = dict.getIndexWord(w2, POS.ADJECTIVE);
// 	}
// 	if(pos.equalsIgnoreCase("r")) {
// 	    word1 = dict.getIndexWord(w1, POS.ADVERB);
// 	    word2 = dict.getIndexWord(w2, POS.ADVERB);
// 	}
// // [error check]: check the words exist in WordNet
// 	if(word1 != null && word2 != null) {
// // get the lesk scores for the (sense pairs)
// 	    List<IWordID> word1IDs = word1.getWordIDs(); // all senses of word 1
// 	    int movingsense = 1;
// 	    for(IWordID idX : word1IDs) {
// 		double leskscore = lesk(w1, movingsense, w2, s2, pos);
// 		map.put((w1 + "#" + pos + "#" + movingsense + "," + w2 + "#" + pos + "#" + s2), leskscore);
// 		movingsense++;
// 	    }
// 	} else {
// 	    return ( map);
// 	}
// 	return ( map );
//     }
    
    
// // lesk(4) a specific sense of word 1 vs. all senses of word 2
//     public TreeMap<String, Double> lesk(String w1, int s1, String w2, String pos) {
// 	// (key)sour#pos#sense sour#pos#sense  (value)leskscore
// 	TreeMap<String, Double> map = new TreeMap<String, Double>();
// 	IIndexWord word1 = null;
// 	IIndexWord  word2 = null;
// // get the WordNet words
// // get the WordNet words in the right part of speech
// 	if(pos.equalsIgnoreCase("n")) {
// 	    word1 = dict.getIndexWord(w1, POS.NOUN);
// 	    word2 = dict.getIndexWord(w2, POS.NOUN);
// 	}
// 	if(pos.equalsIgnoreCase("v")) {
// 	    word1 = dict.getIndexWord(w1, POS.VERB);
// 	    word2 = dict.getIndexWord(w2, POS.VERB);
// 	}
// 	if(pos.equalsIgnoreCase("a")) {
// 	    word1 = dict.getIndexWord(w1, POS.ADJECTIVE);
// 	    word2 = dict.getIndexWord(w2, POS.ADJECTIVE);
// 	}
// 	if(pos.equalsIgnoreCase("r")) {
// 	    word1 = dict.getIndexWord(w1, POS.ADVERB);
// 	    word2 = dict.getIndexWord(w2, POS.ADVERB);
// 	}
// // [error check]: check the words exist in WordNet
// 	if(word1 != null && word2 != null) {
// // get the lesk scores for the (sense pairs)
// 	    List<IWordID> word2IDs = word2.getWordIDs(); // all senses of word 2
// 	    int movingsense = 1;
// 	    for(IWordID idX : word2IDs) {
// 		double leskscore = lesk(w1, s1, w2, movingsense, pos);
// 		map.put((w1 + "#" + pos + "#" + s1 + "," + w2 + "#" + pos + "#" + movingsense), leskscore);
// 		movingsense++;
// 	    }
// 	} else {
// 	    return ( map );
// 	}
// 	return ( map );
//     }
    
// Utilities _________________________________________________________________________
    
// get max score for all sense pairs
    public double max(String w1, String w2, String pos) {
	double max = 0.0;
	TreeMap<String, Double> pairs = lesk(w1, w2, pos);
	for(String p : pairs.keySet()) {
	    double current = pairs.get(p);
	    if(current > max) {
		max = current;
	    }
	}
	return ( max );
    }    
}
