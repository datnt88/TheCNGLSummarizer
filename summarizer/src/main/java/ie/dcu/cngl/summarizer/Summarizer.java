package ie.dcu.cngl.summarizer;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import ie.dcu.cngl.tokenizer.IStructurer;
import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.Tokenizer;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Attribute;
import java.util.ArrayList;

import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.Structurer;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.summarizer.feature.MyTokenStream;
import ie.dcu.cngl.summarizer.feature.NumericTokenFilter;
import ie.dcu.cngl.summarizer.feature.PunctuationTokenFilter;
import ie.dcu.cngl.tokenizer.TokenInfo;

/**
 * Provides a sentence extracted summary of provided content. There are three
 * stages in the summarization pipeline - </br> Tokenization: The summarizer
 * uses a Structurer to not only tokenize the content, but to structure the
 * content by sentences and paragraphs.</br> Weighting: Using the content
 * structure and chosen features the weighter assigns weights to each
 * sentence.</br> Aggregation: The aggregator combines the weights from each
 * feature. It may decide to completely discount those negatively weighted,
 * combine them linearly, logarithmically, or in any other fashion chosen. The
 * sentences are then ranked. </br> The summarizer finishes by outputting the
 * number of sentences desired according to their score.
 *
 * @author Shane and Dat Tien Nguyen
 *
 */
public class Summarizer {

    private Tokenizer tokenizer;
    private IStructurer structurer;
    private IWeighter weighter;
    private IAggregator aggregator;
    private int numSentences;
    private String title;
    private String query;
    private String[] phraseQuery;
    private ArrayList<Double[]> weights;

    /**
     * Creates new summarizer with provided components.
     *
     * @param structurer Extracts content structure
     * @param weighter Weights sentences and paragraphs
     * @param aggregator Combines scores and ranks sentences
     */
    public Summarizer(IStructurer structurer, IWeighter weighter, IAggregator aggregator) {
        this.tokenizer = Tokenizer.getInstance();
        this.weighter = weighter;
        this.aggregator = aggregator;
        this.structurer = structurer;
        this.numSentences = 2;	//Default number of sentences
        this.weights = new ArrayList<Double[]>();



    }

    /**
     * Sets the number sentences to be returned by the summarizer.
     *
     * @param numSentences
     */
    public void setNumSentences(int numSentences) {
        this.numSentences = numSentences;
    }

    /**
     * Provides sentence extracted summary of provided content.
     *
     * @param content to be summarized
     * @return summary of provided content
     */
    public String summarize(String content) throws Exception {
        if (StringUtils.isEmpty(content)) {
            return StringUtils.EMPTY;
        }

        PageStructure structure = structurer.getStructure(content);
        weighter.setStructure(structure);        
        weighter.setTitle(StringUtils.isNotEmpty(title) ? tokenizer.tokenize(title) : null);
        weighter.setQuery(StringUtils.isNotEmpty(query) ? ignoreStopList(query) : null);

        //Set phrase Query
        ArrayList<TokenInfo> phraseQuery_ = new ArrayList<TokenInfo>();
        if (phraseQuery != null) {
            for (int i = 0; i < phraseQuery.length; i++) {
                phraseQuery_.add(new TokenInfo(phraseQuery[i].trim()));
            }
        }
        
        //System.out.println("Phrase Query terms are shown below: ");
        
        //for(int i =0; i< phraseQuery_.size(); i++){
          //  System.out.println(phraseQuery_.get(i));
       // }
        
        weighter.setPhraseQuery(!phraseQuery_.isEmpty() ? phraseQuery_ : null);


        aggregator.setSentences(structure.getSentences());

        if (weights.isEmpty()) { //If weights is Empty, re-calculate weights else weights loaded to aggregate
            weighter.calculateWeights(weights);
            for (Double[] featureWeight : weights) {	//Correct internal state?
                assert (featureWeight.length == structure.getNumSentences());
            }
        }

        SentenceScore[] scores = aggregator.aggregate(weights);

        String summary = StringUtils.EMPTY;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i].getScore() < numSentences) {
                summary += (scores[i].getSentence() + "\n");
            }
        }

        return summary;
    }

    /**
     * Set content title
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set query related to the content
     *
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Set phrase query related to the content
     *
     * @param query
     */
    public void setPhraseQuery(String[] phraseQuery) {
        this.phraseQuery = phraseQuery;
    }

    /**
     * Provide pre-calculated weights that will be combined with weights
     * calculated on new run.
     *
     * @param weights Pre-calculated weights
     */
    public void setWeights(ArrayList<Double[]> weights) {
        this.weights = weights;
    }

    /****************
     * Igonre stopword, Punctuation in a sentence
     */
    public ArrayList<TokenInfo> ignoreStopList(String sentence) throws Exception {

        ArrayList<TokenInfo> token_array = new ArrayList<TokenInfo>();
        token_array = tokenizer.tokenize(sentence);

        int numTerms = 0;
        ArrayList<TokenInfo> results = new ArrayList<TokenInfo>();

        Set<Object> stopset;
        String[] stopwords = filterComments(StringUtils.split(FileUtils.readFileToString(new File(SummarizerUtils.stopwords), "UTF-8")));
        stopset = StopFilter.makeStopSet(Version.LUCENE_36, stopwords, true);


        Iterator<TokenInfo> iter = token_array.iterator();

        TokenStream tokenStream =
                new StopFilter(Version.LUCENE_36,
                new StandardFilter(Version.LUCENE_36,
                new NumericTokenFilter(
                new PunctuationTokenFilter(
                new LowerCaseFilter(Version.LUCENE_36, new MyTokenStream(iter))))), stopset);

        OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
        TermAttribute termAttribute = tokenStream.getAttribute(TermAttribute.class);

        /*
        while (tokenStream.incrementToken()) {
        int startOffset = offsetAttribute.startOffset();
        int endOffset = offsetAttribute.endOffset();
        //System.out.print(termAttribute.term().toString() + " , ");
        //results.add(new TokenInfo(termAttribute.term()));
        //results.add(new TokenInfo("investigation"));

        }


        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        while (tokenStream.incrementToken()) {
        int startOffset = offsetAttribute.startOffset();
        int endOffset = offsetAttribute.endOffset();
        String term = charTermAttribute.toString();
        System.out.print(charTermAttribute.toString() + " + ");
        results.add(new TokenInfo(term));
        }
         */


        CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {

            results.add(new TokenInfo(cattr.toString()));
        }

        //Testing
       // System.out.println("*** Query is: " + sentence);
        //System.out.println("Query tokens are shown below: ");
        //for(int i = 0; i < results.size(); i++){
            //System.out.println(results.get(i));
       // }

        //System.out.println();
        return results;
    }

    public static String[] filterComments(String[] input) {
        List<String> stopwords = new ArrayList<String>();
        for (String stopword : input) {
            if (!stopword.startsWith("#")) {
                stopwords.add(stopword);
            }
        }
        return stopwords.toArray(new String[0]);
    }
}
