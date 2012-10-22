package ie.dcu.cngl.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Encapsulates the 3-dimensional structure of a page. The content's
 * paragraphs, the paragraphs' sentences, the sentences' tokens.
 * @author Shane
 *
 */
public class PageStructure {
	
	private ArrayList<ArrayList<ArrayList<TokenInfo>>> structure;
	private ArrayList<SectionInfo> sentences;
	private ArrayList<SectionInfo> paragraphs;
	private HashMap<Integer, Integer> sentenceToParagraph;
	private HashMap<Integer, Integer> sentenceToRelativePosition;

	public PageStructure(ArrayList<ArrayList<ArrayList<TokenInfo>>> structure) {
		this.structure = structure;
		this.sentences = getSentencesPriv();
		this.paragraphs = getParagraphsPriv();
		this.sentenceToParagraph = new HashMap<Integer, Integer>();
		this.sentenceToRelativePosition = new HashMap<Integer, Integer>();
		mapSentencesToParagraphs();
	}

	/**
	 * Maps each sentence to its paragraph to aid later retrieval
	 */
	private void mapSentencesToParagraphs() {
		int sentenceNumber = 0, paragraphNumber = 0, sentenceParagraphStarter = 0;
		for(ArrayList<ArrayList<TokenInfo>> paragraph : structure) {
			int numSentences = paragraph.size();
			for(int i = 0; i < numSentences; i++) {
				sentenceToParagraph.put(sentenceNumber, paragraphNumber);
				sentenceToRelativePosition.put(sentenceNumber, sentenceParagraphStarter);
				sentenceNumber++;
			}
			sentenceParagraphStarter = sentenceNumber;
			paragraphNumber++;
		}
	}

	private ArrayList<SectionInfo> getSentencesPriv() {
		return getSectionInfo(structure);
	}
	
	private ArrayList<SectionInfo> getParagraphsPriv() {
		//Prior to calling getSectionInfo we need all tokens of each paragraph in one array
		ArrayList<TokenInfo> individualParagraphTokens;
		ArrayList<ArrayList<TokenInfo>> allParagraphTokens = new ArrayList<ArrayList<TokenInfo>>();
		for(ArrayList<ArrayList<TokenInfo>> paragraph : structure) {
			individualParagraphTokens = new ArrayList<TokenInfo>();
			for(ArrayList<TokenInfo> sentence : paragraph) {
				for(TokenInfo token : sentence) {
					individualParagraphTokens.add(token);
				}
			}
			allParagraphTokens.add(individualParagraphTokens);
		}
		ArrayList<ArrayList<ArrayList<TokenInfo>>> paragraphsHolder = new ArrayList<ArrayList<ArrayList<TokenInfo>>>();
		paragraphsHolder.add(allParagraphTokens);
		
		return getSectionInfo(paragraphsHolder);
	}
	
	/**
	 * Gets the tokens of the specified tokens in the specified paragraph.
	 * @param sentenceNumber The sentence number of the desired tokens.
	 * @param paragraphNumber The paragraph number containing the desired sentence.
	 * @return The tokens of the desired sentence, or null if it doesn't exist.
	 */
	public ArrayList<TokenInfo> getSentenceFromParagraphTokens(int sentenceNumber, int paragraphNumber) {
		try {
			return structure.get(paragraphNumber).get(sentenceNumber);
		} catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * Retrieve the tokens of the specified sentence.
	 * @param sentenceNumber The absolute sentence number of the desired sentence.
	 * @return
	 */
	public ArrayList<TokenInfo> getSentenceTokens(int sentenceNumber) {
		return getSentenceFromParagraphTokens(sentenceNumber-sentenceToRelativePosition.get(sentenceNumber), sentenceToParagraph.get(sentenceNumber));
	}
	
	/**
	 * Get the raw 3-demensional page structure.
	 * @return The 3-dimensional page structure.
	 */
	public ArrayList<ArrayList<ArrayList<TokenInfo>>> getStructure() {
		return this.structure;
	}
	
	/**
	 * Get the content sentences.
	 * @return An array of the content sentences with their absolute positions.
	 */
	public ArrayList<SectionInfo> getSentences() {
		return this.sentences;
	}
	
	/**
	 * Get the content paragraphs.
	 * @return An array of the content paragraphs with their absolute positions.
	 */
	public ArrayList<SectionInfo> getParagraphs(){
		return this.paragraphs;
	}
	
	/**
	 * @return The number of sentences in the content.
	 */
	public int getNumSentences() {
		return this.sentences.size();
	}

	/**
	 * @return The number of paragraphs in the content.
	 */
	public int getNumParagraphs() {
		return this.paragraphs.size();
	}
	
	private ArrayList<SectionInfo> getSectionInfo(ArrayList<ArrayList<ArrayList<TokenInfo>>> rawSections) {
		ArrayList<SectionInfo> sections = new ArrayList<SectionInfo>();
		int sectionCount = 0;
		for(ArrayList<ArrayList<TokenInfo>> paragraph : rawSections) {
			ArrayList<String> strSentences = TokenizerUtils.recombineTokens2d(paragraph);
			for(String sentence : strSentences) {
				SectionInfo sentenceInfo = new SectionInfo(sentence, sectionCount);
				sections.add(sentenceInfo);
				sectionCount++;
			}
		}
		return sections;
	}

}
