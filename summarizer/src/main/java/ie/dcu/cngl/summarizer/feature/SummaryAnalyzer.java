package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import ie.dcu.cngl.tokenizer.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

/**
 * Special purpose analyzer that uses a chain of PorterStemFilter, StopFilter,
 * LowercaseFilter and StandardFilter to wrap a StandardTokenizer. The StopFilter
 * uses a custom stop word set.
 * @author Sujit Pal
 */
public class SummaryAnalyzer extends Analyzer {

	private Set<Object> stopset;
	private String[] stopwords = filterComments(StringUtils.split(FileUtils.readFileToString(new File(SummarizerUtils.stopwords), "UTF-8")));
	
	public SummaryAnalyzer() throws IOException {
		this.stopset = StopFilter.makeStopSet(Version.LUCENE_36, stopwords, true);
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		Tokenizer tokenizer = Tokenizer.getInstance();
		String text = getText(reader);
		ArrayList<TokenInfo> tokens = tokenizer.tokenize(text);
		Iterator<TokenInfo> iter = tokens.iterator();

		return new PorterStemFilter(
				new StopFilter(Version.LUCENE_36,
					new StandardFilter(Version.LUCENE_36,
							new NumericTokenFilter(
									new PunctuationTokenFilter(
											new LowerCaseFilter(Version.LUCENE_36,
													new MyTokenStream(iter))))), stopset));
	}

	/**
	 * Dynamically reads text from reader.
	 * @param reader
	 * @return the entire string read
	 */
	private String getText(Reader reader) {
		final int INITIAL_SIZE = 4096;
		char [] buffer = new char[INITIAL_SIZE], finalArray = new char[INITIAL_SIZE], tempArray;
		int numRead = 0, currentRead = 0;
		
		try {
			while((currentRead = reader.read(buffer)) != -1) {
				//Temporarily store previously read chars
				tempArray = new char[numRead];
				System.arraycopy(finalArray, 0, tempArray, 0, numRead);
				//Combine extra chars with temporary chars.
				numRead+=currentRead;
				finalArray = new char[numRead];
				System.arraycopy(tempArray, 0, finalArray, 0, tempArray.length);
				System.arraycopy(buffer, 0, finalArray, tempArray.length, currentRead);
			}
		} catch (IOException e) {}
		
		return new String(finalArray, 0, numRead);
	}

	private String[] filterComments(String[] input) {
		List<String> stopwords = new ArrayList<String>();
		for (String stopword : input) {
			if (! stopword.startsWith("#")) {
				stopwords.add(stopword);
			}
		}
		return stopwords.toArray(new String[0]);
	}
}
