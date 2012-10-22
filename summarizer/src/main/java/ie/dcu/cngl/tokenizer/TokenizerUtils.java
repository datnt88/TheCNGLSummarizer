package ie.dcu.cngl.tokenizer;

import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 * Static class containing shared variables and methods.
 * @author Shane
 *
 */
public class TokenizerUtils {
	
	public static final String COMMENT = "#";
	public static final String WHITE_SPACE = " ";
	public static String abbreviations;
	public static String badSentenceStart;
	public static String possibleSentenceEnd;
	public static String badSentenceEnd;
	
	static {
		try {
			XMLConfiguration config = new XMLConfiguration("src/main/resources/config/tokeniser.xml");
			abbreviations = config.getString("word.abbreviations");
			badSentenceStart = config.getString("sentence.badStart");
			possibleSentenceEnd = config.getString("sentence.possibleEnd");
			badSentenceEnd = config.getString("sentence.badEnd");
		} catch (ConfigurationException e) {}
	}
	
	/**
	 * Recombines 2d array of tokens into a String array. Does this based on location information
	 * derived during tokenization.
	 * @param sections
	 * @return
	 */
	public static ArrayList<String> recombineTokens2d(ArrayList<ArrayList<TokenInfo>> sections) {
		ArrayList<String> strSections = new ArrayList<String>();		
		for(ArrayList<TokenInfo> tokens : sections) {
			strSections.add(recombineTokens1d(tokens));
		}		
		return strSections;
	}

	/**
	 * Recombines array of tokens into string form. Does this based on location information
	 * derived during tokenization.
	 * @param sections
	 * @return
	 */
	public static String recombineTokens1d(ArrayList<TokenInfo> tokens) {
		String combined;
		TokenInfo current, next;
		
		current = tokens.get(0);
		combined = current.getValue();	//Presumes the first character has no spaces at the beginning
		final int numTokens = tokens.size();
		for(int i = 1; i < numTokens; i++) {
			next = tokens.get(i);
			combined+=StringUtils.repeat(WHITE_SPACE, next.getStart()-(current.getStart()+current.getLength()));
			combined+=next.getValue();
			current = next;
		}
		
		return combined;
	}
	
}
