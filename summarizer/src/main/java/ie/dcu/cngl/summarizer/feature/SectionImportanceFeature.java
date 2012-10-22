package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.summarizer.SentenceScore;
import ie.dcu.cngl.summarizer.SummarizerUtils;
import ie.dcu.cngl.tokenizer.TokenInfo;
import ie.dcu.cngl.tokenizer.TokenizerUtils;

import java.util.ArrayList;

/**
 * Sentences within certain sections are scored differently. The section names and scores are configurable. 
 * The feature gives weight to any sentences (except the first) that are in the same paragraph and to 
 * those that are in the next paragraph.</br>
 * sentence score = section score
 * @author Shane
 *
 */
public class SectionImportanceFeature extends TermCheckingFeature {
	
	private ArrayList<SentenceScore> sections;

	public SectionImportanceFeature() throws Exception {
		super();
		this.sections = new ArrayList<SentenceScore>();
		try {
			for(String line : terms) {
			    line = line.toLowerCase();
			    String [] sectionAndWeight = line.split(",");
			    SentenceScore section = new SentenceScore(sectionAndWeight[0].trim(), Integer.parseInt(sectionAndWeight[1].trim()));
			    sections.add(section);
			}
        } catch (IndexOutOfBoundsException e) {
        	throw new Exception("Incorrect data format. Should be \"<name>,<weight>\" eg. \"introduction,5\"");
        }
	}

	@Override
	public double getMultiplier() {
		return SummarizerUtils.sectionImportanceMultiplier;
	}

	@Override
	public Double[] calculateRawWeights(Double[] weights) {
		int paragraphNumber = 0, sentenceNumber = 0;
		for(ArrayList<ArrayList<TokenInfo>> paragraph : structure.getStructure()) {
			for(SentenceScore section : sections) {
				if(TokenizerUtils.recombineTokens1d(paragraph.get(0)).equalsIgnoreCase(section.getSentence())) {
					//Give weight to any sentences (except the first) that are in the same paragraph
					//And to those that are in the next paragraph
					int paragraphEnd = sentenceNumber + paragraph.size();
					int numSentencesInNextParagraph = structure.getStructure().get(paragraphNumber+1).size();
					for(int i = sentenceNumber+1; i < paragraphEnd+numSentencesInNextParagraph; i++) {
						weights[i] = section.getScore();
					}
				}
			}
			sentenceNumber+=paragraph.size();
			paragraphNumber++;
		}
		return weights;
	}

	@Override
	public String getTermsFileName() {
		return SummarizerUtils.sectionsFile;
	}

}
