package ie.dcu.cngl.summarizer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Static class containing shared variables and methods.
 * @author Shane
 *
 */
public class SummarizerUtils {
	
	public static String stopwords;
	public static String cuePhrasesFile;
	public static String affixesFile;
	public static String basicWordsFile;
	public static String sectionsFile;
	public static String importantTermsFile;
        public static String datetimeTermFile;
	
	//Feature multipliers
	public static double skimmingMultiplier;
	public static double namedEntityMultiplier;
	public static double TFISFMultiplier;
	public static double titleTermMultiplier;
	public static double cuePhraseMultiplier;
	public static double shortSentenceMultiplier;
	public static double longSentenceMultiplier;
	public static double queryBiasMultiplier;
	public static double globalBushyMultiplier;
	public static double punctuationMultiplier;
	public static double affixPresenceMultiplier;
	public static double basicWordsMultiplier;
	public static double sectionImportanceMultiplier;
	public static double clusterKeywordMultiplier;
        //Feature added by Dat Tien Nguyen
        public static double ambiguationMultiplier;
        public static double conceptHierarchyMultiplier;
        public static double datetimeTermMultiplier;
        public static double querySynsetMultiplier;
	
	//Other settings
	public static int extraLettersForMatch;
	public static int minSignificantWordSeparation;
	public static double minimumSimilarity;
	public static double maximumSimilarity;
	public static int minimumSentenceQueryLength;
	public static double topTermCutOff;
	public static double maxPunctuationRatio;
	public static int minSentenceLength;
	public static int maxSentenceLength;
	
	static {
		try {
			XMLConfiguration config = new XMLConfiguration("src/main/resources/config/summarizer.xml");
			
			stopwords = config.getString("files.stopwords");
			cuePhrasesFile = config.getString("files.cuephrases");
			affixesFile = config.getString("files.affixes");
			basicWordsFile = config.getString("files.basicWords");
			sectionsFile = config.getString("files.sections");
			importantTermsFile = config.getString("files.importantTerms");
			datetimeTermFile = config.getString("files.datetimeTerms");

			skimmingMultiplier = config.getDouble("multipliers.skimming");
			namedEntityMultiplier = config.getDouble("multipliers.namedEntity");
			TFISFMultiplier = config.getDouble("multipliers.TSISF");
			titleTermMultiplier = config.getDouble("multipliers.titleTerm");
			cuePhraseMultiplier = config.getDouble("multipliers.cuePhrase");
			shortSentenceMultiplier = config.getDouble("multipliers.shortSentence");
			longSentenceMultiplier = config.getDouble("multipliers.longSentence");
			queryBiasMultiplier = config.getDouble("multipliers.queryBias");
			globalBushyMultiplier = config.getDouble("multipliers.globalBushy");
			punctuationMultiplier = config.getDouble("multipliers.punctuation");
			affixPresenceMultiplier = config.getDouble("multipliers.affixPresence");
			basicWordsMultiplier = config.getDouble("multipliers.basicWords");
			sectionImportanceMultiplier = config.getDouble("multipliers.sectionImportance");
			clusterKeywordMultiplier = config.getDouble("multipliers.clusterKeyword");
                        ambiguationMultiplier = config.getDouble("multipliers.ambiguation");
                        conceptHierarchyMultiplier = config.getDouble("multipliers.conceptHierarchy");
                        datetimeTermMultiplier = config.getDouble("multipliers.datetimeTerm");
			querySynsetMultiplier = config.getDouble("multipliers.querySynset");
                        
			extraLettersForMatch = config.getInt("featureSettings.affixPresence.extraLettersForMatch", -1);
			minSignificantWordSeparation = config.getInt("featureSettings.clusterKeyword.minSignificantWordSeparation", -1);
			minimumSimilarity = config.getDouble("featureSettings.globalBushy.minimumSimilarity", -1);
			maximumSimilarity = config.getDouble("featureSettings.globalBushy.maximumSimilarity", -1);
			minimumSentenceQueryLength = config.getInt("featureSettings.globalBushy.minimumSentenceQueryLength", -1);
			topTermCutOff = config.getDouble("featureSettings.lucene.topTermCutOff", -1);
			maxPunctuationRatio = config.getDouble("featureSettings.punctuation.maxRatio", -1);
			minSentenceLength = config.getInt("featureSettings.shortSentence.minLength", -1);
			maxSentenceLength = config.getInt("featureSettings.longSentence.maxLength", -1);
		} catch (ConfigurationException e) {
			System.err.println("Configuration retrieval error");
			e.printStackTrace();
		}
	}

}
