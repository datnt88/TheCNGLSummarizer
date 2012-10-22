package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.SectionInfo;
import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/**
 * The GlobalBushyFeature was proposed by Salton et al - "The  bushiness  of a  node  on  a  map  is  
 * defined as  the  number of links  connecting it to other nodes  on  the map.  Since  a  highly  bushy  
 * node is  linked to  a  number of other nodes,  it has  an overlapping vocabulary with several paragraphs 
 * and is likely to discuss topics covered in many other paragraphs". It is used here at sentence level.</br>
 * sentence score = &#8721; similarity to each other sentence
 * @author Shane
 *
 */
public class GlobalBushyFeature extends LuceneFeature {

	private double minimumSimilarity;
	private double maximumSimilarity;
	private int sentenceQueryMinLength;

	/**
	 * Create GlobalBushyFeature with defaults 0.8 for minimum similarity,
	 * and 1.0 for maximum simlarity. Any similarity scores outside these
	 * boundaries will not be counted.
	 * The default minimum sentence query length is set to 20.
	 * @throws IOException
	 */
	public GlobalBushyFeature() throws IOException {
		//Defaults
		this.minimumSimilarity = SummarizerUtils.minimumSimilarity == -1 ? 0.8 : SummarizerUtils.minimumSimilarity;
		this.maximumSimilarity = SummarizerUtils.maximumSimilarity == -1 ? 1.0 : SummarizerUtils.maximumSimilarity;
		this.sentenceQueryMinLength = SummarizerUtils.minimumSentenceQueryLength == -1 ? 20 : SummarizerUtils.minimumSentenceQueryLength;
	}
	
	/**
	 * Set the minimum similarity limit. Any sentence comparisons below this level
	 * will not be counted. They are deemed to be too dissimilar.
	 * @param minimumSimilarity
	 */
	public void setMinimumSimilarity(double minimumSimilarity) {
		this.minimumSimilarity = minimumSimilarity;
	}

	/**
	 * Set the maximum similarity limit. Any sentence comparisons above this level
	 * will not be counted. Some sentences are almost carbon copies, and will score
	 * artificially high. These high scores are not good indicators of central nodes.
	 * @param minimumSimilarity
	 */
	public void setMaximumSimilarity(double maximumSimilarity) {
		this.maximumSimilarity = maximumSimilarity;
	}

	/**
	 * Set the minimum sentence query length. To calculate the similarity of each sentence
	 * a query is provided to a lucene index containing all sentences. Sentences with few
	 * query terms score artifically high, and are not good indicators of central nodes.
	 * Increasing this value improves central node detection, as well as performance (due
	 * to a reduction in the number of queries).
	 * @param sentenceQueryMinLength
	 */
	public void setSentenceQueryMinLength(int sentenceQueryMinLength) {
		this.sentenceQueryMinLength = sentenceQueryMinLength;
	}

	@Override
	public Double[] calculateRawWeights(Double[] weights) {
		try {
			buildIndex();
			weights = calculateNodeValues(weights);
		} catch (Exception e) {
			System.err.println("Lucene stuff failed");
			e.printStackTrace();
		}
		return weights;
	}

	private Double[] calculateNodeValues(Double[] weights) throws CorruptIndexException, IOException, ParseException {
		int sentenceNumber = 0;
		for(SectionInfo sentence : structure.getSentences()) {
			try {
				IndexReader reader = IndexReader.open(ramdir);
				IndexSearcher searcher = new IndexSearcher(reader);
				BooleanQuery query = new BooleanQuery();
				
				StringReader strReader = new StringReader(sentence.getValue());
				TokenStream tokenStream = analyzer.tokenStream("text", strReader);
				CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
				while (tokenStream.incrementToken()) {
				    String term = charTermAttribute.toString();
				    query.add(new TermQuery(new Term("text", term)), Occur.SHOULD);
				}	
				
				if(query.getClauses().length > sentenceQueryMinLength) {
					TopDocs topDocs = searcher.search(query, structure.getNumSentences());
					for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
						Document doc = searcher.doc(scoreDoc.doc);
						String searchSentence = StringUtils.chomp(doc.get("text"));
						double score = (double)scoreDoc.score;
						if(score > minimumSimilarity && score < maximumSimilarity) {
							weights[sentenceMap.get(searchSentence)]+=score;
							weights[sentenceNumber]+=score;
						}
					}
				}
				sentenceNumber++;
			} catch(Exception ignore) {}
		}

//	Calling my own cosine similarity formula
//		ArrayList<SectionInfo> sentences = structure.getSentences();
//
//		final int numSentences = sentences.size();
//		for(int i = 0; i < numSentences-1; i++) {
//			ArrayList<TokenInfo> baseSentence = structure.getSentenceTokens(i);
//			HashMap<String, Double> vector1 = getFreqMap(baseSentence);
//			for(int j = i+1; j < numSentences; j++) {
//				ArrayList<TokenInfo> compareSentence = structure.getSentenceTokens(j);
//				HashMap<String, Double> vector2 = getFreqMap(compareSentence);
//				double score = cosineSimilarity(vector1, vector2);
//				weights[i]+=score;
//				weights[j]+=score;
//			}
//		}


		return weights;
	}

// My own cosineSimilarity formula. Performs badly, but it may be possible to improve.
//	private double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
//		Set<String> both = new HashSet<String>();
//		for(String key : vector1.keySet()) both.add(key);
//		both.retainAll(vector2.keySet());
//		double sclar = 0, norm1 = 0, norm2 = 0;
//		for (String k : both) sclar += vector1.get(k) * vector2.get(k);
//		for (String k : vector1.keySet()) norm1 += vector1.get(k) * vector1.get(k);
//		for (String k : vector2.keySet()) norm2 += vector2.get(k) * vector2.get(k);
//		return sclar / Math.sqrt(norm1 * norm2);
//	}
//
//	private HashMap<String, Double> getFreqMap(ArrayList<TokenInfo> tokens) {
//		HashMap<String, Double> map = new HashMap<String, Double>();
//		for(TokenInfo token : tokens) {
//			Double freq = map.get(token.getValue());
//			map.put(token.getValue(), freq == null ? 1 : freq+1);
//		}
//		return map;
//	}

	@Override
	protected float computeBoost(int paragraphNumber, int sentenceNumber) {
		return 1;
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.globalBushyMultiplier;
	}

}
