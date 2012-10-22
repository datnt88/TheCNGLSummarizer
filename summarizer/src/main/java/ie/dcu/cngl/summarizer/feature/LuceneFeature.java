package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.SectionInfo;

import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.TokenInfo;
import ie.dcu.cngl.tokenizer.TokenizerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * A LuceneFeature uses Lucene to aid sentence scoring. All sentences are treated
 * like documents for indexing, and each can be boosted according to certain criteria.
 * The index of sentences can then be queried as desired.
 * @author Shane
 *
 */
public abstract class LuceneFeature extends Feature {

	protected RAMDirectory ramdir;
	protected HashMap<String, Integer> sentenceMap;

	private double topTermCutoff;

	public LuceneFeature() throws IOException {
		this.ramdir = new RAMDirectory();
		this.topTermCutoff = SummarizerUtils.topTermCutOff == -1 ? 0.3 : SummarizerUtils.topTermCutOff;
	}

	/**
	 * Sentences can be boosted/deboosted based on certain criteria.
	 * @param paragraphNumber The paragraph containing the sentence
	 * @param sentenceNumber The sentence's position within the paragraph
	 * @return Calculated boost/deboost for that sentence.
	 */
	protected abstract float computeBoost(int paragraphNumber, int sentenceNumber);

	/**
	 * This value specifies where to cutoff the term list for query.
	 * The text is loaded into an in-memory index, a sentence per
	 * Lucene Document. Then the index is queried for terms and their
	 * associated frequency in the index. The topTermCutoff is a 
	 * ratio from 0 to 1 which specifies how far to go down the 
	 * frequency ordered list of terms. The terms considered have 
	 * a frequency greater than topTermCutoff * topFrequency. 
	 * @param topTermCutoff a ratio specifying where the term list
	 *        will be cut off. Must be between 0 and 1. Default is
	 *        to consider all terms if this variable is not set,
	 *        ie topTermCutoff == 0. But it is recommended to set
	 *        an appropriate value (such as 0.5). 
	 */
	public void setTopTermCutoff(double topTermCutoff) {
		if (topTermCutoff < 0.0F || topTermCutoff > 1.0F) {
			throw new IllegalArgumentException(
			"Invalid value: 0.0F <= topTermCutoff <= 1.0F");
		}
		this.topTermCutoff = topTermCutoff;
	}

	@Override
	public Double[] calculateRawWeights(Double[] weights) {
		try {
			buildIndex();
			Query topTermQuery = computeTopTermQuery();
			weights = searchIndex(topTermQuery, weights);
		} catch (Exception e) {
			System.err.println("Lucene stuff failed");
			e.printStackTrace();
		}
		return weights;
	}

	/**
	 * Builds an in-memory index of the sentences in the text with the
	 * appropriate document boosts if specified.
	 * @throws Exception if one is thrown.
	 */
	protected void buildIndex() throws Exception {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		IndexWriter writer = new IndexWriter(ramdir, config);

		int pno = 0;
		for(ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
			ArrayList<String> strSentences = TokenizerUtils.recombineTokens2d(paragraph);
			int sno = 0;
			for(String sentence : strSentences) {
				Document doc = new Document();
				doc.add(new Field("text", sentence, Store.YES, Index.ANALYZED));
				doc.setBoost(computeBoost(pno, sno));
				writer.addDocument(doc);
				sno++;
			}
			pno++;
		}

		writer.commit();
		writer.close();
	}

	/**
	 * Builds a Boolean OR query out of the "most frequent" terms in the index 
	 * and returns it.
	 * @return a Boolean OR query.
	 * @throws Exception if one is thrown.
	 */	
	protected Query computeTopTermQuery() throws Exception {
		ArrayList<String> topTerms = retrieveTopTerms();
		BooleanQuery query = new BooleanQuery();
		BooleanQuery.setMaxClauseCount(10000);
		for (String topTerm : topTerms) {
			query.add(new TermQuery(new Term("text", topTerm)), Occur.SHOULD);
		}

		return query;
	}

	/**
	 * Computes a term frequency map for the index at the specified location.
	 * "Most Frequent" is defined as the terms whose frequencies
	 * are greater than or equal to the topTermCutoff * the frequency of the
	 * top term, where the topTermCutoff is number between 0 and 1.
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	protected ArrayList<String> retrieveTopTerms() throws CorruptIndexException, IOException {
		final Map<String,Integer> frequencyMap = new HashMap<String,Integer>();
		List<String> termlist = new ArrayList<String>();
		IndexReader reader = IndexReader.open(ramdir);
		TermEnum terms = reader.terms();
		while (terms.next()) {
			Term term = terms.term();
			String termText = term.text();
			int frequency = reader.docFreq(term);
			frequencyMap.put(termText, frequency);
			termlist.add(termText);
		}
		reader.close();

		// sort the term map by frequency descending
		Collections.sort(termlist, new Comparator<String>() {
			@Override
			public int compare(String term1, String term2) {
				int term1Freq = frequencyMap.get(term1);
				int term2Freq = frequencyMap.get(term2);

				if(term1Freq < term2Freq) return 1;
				if(term1Freq > term2Freq) return -1;
				return 0;
			}
		});

		// retrieve the top terms based on topTermCutoff
		ArrayList<String> topTerms = new ArrayList<String>();
		double topFreq = -1.0F;
		for (String term : termlist) {
			if (topFreq < 0.0F) {
				// first term, capture the value
				topFreq = (double) frequencyMap.get(term);
				topTerms.add(term);
			} else {
				// not the first term, compute the ratio and discard if below
				// topTermCutoff score
				double ratio = (double) ((double) frequencyMap.get(term) / topFreq);
				if (ratio >= topTermCutoff) {
					topTerms.add(term);
				} else {
					break;
				}
			}
		}

		return topTerms;
	}

	/**
	 * Executes the query against the specified index, and returns a bounded
	 * collection of sentences ordered by document id (so the sentence ordering
	 * is preserved in the collection).
	 * @param query the Boolean OR query computed from the top terms.
	 * @param weights the target location for the calculated weights.
	 * @return an array of sentences.
	 * @throws Exception if one is thrown.
	 */
	protected Double[] searchIndex(Query query, Double[] weights) throws Exception {
		IndexReader reader = IndexReader.open(ramdir);
		IndexSearcher searcher = new IndexSearcher(reader);

		TopDocs topDocs = searcher.search(query, structure.getNumSentences());

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			String sentence = StringUtils.chomp(doc.get("text"));
			weights[sentenceMap.get(sentence)] = (double)scoreDoc.score;
		}

		searcher.close();
		return weights;
	}

	@Override
	public void setStructure(PageStructure structure) {
		this.structure = structure;
		this.sentenceMap = new HashMap<String, Integer>();
		for(SectionInfo sentence : structure.getSentences()) {
			sentenceMap.put(sentence.getValue(), sentence.getLocation());
		}
	}

}
