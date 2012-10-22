package ie.dcu.cngl.tokenizer;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.*;

import org.apache.commons.lang.StringUtils;

/**
 * Separates the tokenized content into sentences.
 * @author Johannes Levelling
 *
 */
public class Sentenizer implements ISentenizer {
	
	private static Sentenizer instance;
	
    private HashSet<String> bss, pse, bse;
    private Tokenizer tokenizer = null;
    
    private Sentenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
		String line;
		bss = new HashSet<String>();
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(TokenizerUtils.badSentenceStart));
	        while ((line = reader.readLine()) != null) {
				if (line.equals(StringUtils.EMPTY) || line.startsWith(TokenizerUtils.COMMENT)) {
				    ;
				} else if (line.length() < 3) {
				    System.out.println("ERR: invalid line " + line);
				} else {
				    // remove quotes
				    line = line.substring(1, line.length()-1);
				    bss.add(line.toLowerCase());
				}
	        }
	        reader.close();
	    } catch (IOException e) {
	        System.out.println("ERROR: " + e);
	    }
		
		pse = new HashSet<String>();
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(TokenizerUtils.possibleSentenceEnd));
	        while (null != (line = reader.readLine())) {
				if (line.equals(StringUtils.EMPTY) || line.startsWith(TokenizerUtils.COMMENT)) {
					;
				} else if (line.length() < 3) {
				    System.out.println("ERR: invalid line " + line);
				} else {
				    // remove quotes
				    line = line.substring(1, line.length()-1);
				    pse.add(line.toLowerCase());
				}
	        }
	        reader.close();
	    } catch (IOException e) {
	        System.out.println("ERROR: " + e);
	    }
		
		bse = new HashSet<String>();
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(TokenizerUtils.badSentenceEnd));
	        while (null != (line = reader.readLine())) {
				if (line.equals(StringUtils.EMPTY) || line.startsWith(TokenizerUtils.COMMENT)) {
					;
				} else if (line.length() < 3) {
				    System.out.println("ERR: invalid line " + line);
				    ;
				} else {
				    // remove quotes
				    line = line.substring(1, line.length()-1);
				    bse.add(line.toLowerCase());
				}
	        }
	        reader.close();
	    } catch (IOException e) {
	        System.out.println("ERROR: " + e);
	    }
    }

    /**
     * Initializing a sentenizer is computationally expensive, so it exists as a singleton.
     * @return Sentenizer singleton.
     */
    public static Sentenizer getInstance() {
    	if(instance == null) {
    		synchronized(Sentenizer.class) {
	    		Tokenizer tokenizer = Tokenizer.getInstance();
	    		instance = new Sentenizer(tokenizer);
    		}
    	}
    	return instance;
    }

    private boolean isBadSentenceStart(String s) {
    	return bss.contains(s);
    }
    
    private boolean isPossibleSentenceEnd(String s) {
    	return pse.contains(s);
    }
    
    private boolean isBadSentenceEnd(String s) {
    	return bse.contains(s);
    }

    /**
     * Tokenize the content, and divide the tokens by sentence.
     * @return A 2-dimensional array of each sentence and its tokens.
     */  
    public synchronized ArrayList<ArrayList<TokenInfo>> sentenize(String s) {
		ArrayList<TokenInfo> tokens = tokenizer.tokenize(s);
		if (tokens == null)
		    return null;
		
		int numTokens = tokens.size();
		int tokenIndex = 0;
		ArrayList<TokenInfo> sentence = new ArrayList<TokenInfo>();
		ArrayList<ArrayList<TokenInfo>> sentences = new ArrayList<ArrayList<TokenInfo>>();
		TokenInfo prevTokInfo = null, currentTokInfo = null, nextTokInfo = null;
		String previousToken = StringUtils.EMPTY, currentToken = StringUtils.EMPTY, nextToken = StringUtils.EMPTY;
	    boolean inQuotes = false, isSentence = false;
		while (tokenIndex < numTokens) {
	    	//Update all token info
		    prevTokInfo = currentTokInfo;
		    currentTokInfo = nextTokInfo;
		    nextTokInfo = tokens.get(tokenIndex);
		    
		    //Update token str values
	        previousToken = currentToken;
	        currentToken = nextToken;
	        nextToken = nextTokInfo.getValue();

		    if(currentTokInfo != null)
				sentence.add(currentTokInfo);
		    
		    if(numTokens == 1)
		    	sentence.add(nextTokInfo);

		    if(currentTokInfo != null && currentTokInfo.getLineNum() < nextTokInfo.getLineNum() && nextTokInfo.getStart()-(currentTokInfo.getStart()+currentTokInfo.getLength()) > 1) {
		    	isSentence = true;		//New line
			} else if(currentToken.equals("\"")) {	//Opening quotes
				//Starting quotes must follow a space. ([he said "hi there"] is allowed, [5'6"] is not) 
				if(inQuotes || prevTokInfo == null || (currentTokInfo != null && currentTokInfo.getStart() - (prevTokInfo.getStart() + prevTokInfo.getLength()) > 1)) {
					inQuotes = !inQuotes;	//Switches between true and false
				}
		    	isSentence = false; 
	    	} else if(currentTokInfo != null && !isPossibleSentenceEnd(currentToken)) {
		    	isSentence = false; 	// do not break if end token is not recognized
		    } else if(prevTokInfo != null && isBadSentenceEnd(previousToken)) {
		    	isSentence = false; 	// do not break if last token would be bad sentence end
		    } else if(nextTokInfo != null && isBadSentenceStart(nextToken)) {
		    	isSentence = false; 	// do not break if next token is bad sentence start
		    } else if(currentTokInfo != null && nextTokInfo != null && (currentTokInfo.getStart() + currentTokInfo.getLength() > nextTokInfo.getStart()-1)) {
		    	isSentence = false; 	// only break after whitespace
		    } else if(currentTokInfo != null && Character.isLowerCase(nextToken.charAt(0))) {
		    	isSentence = false; 	// don't break before lower cased next token
		    } else {
		    	isSentence = true;
		    }
		    
		    if(sentence.size() > 0 && currentToken != null && isSentence) { 
		    	if(!inQuotes) {
					sentences.add(sentence);
					sentence = new ArrayList<TokenInfo>();
		    	}
		    }
		    tokenIndex++;
		}
		
	    if (sentence.size() > 0 && currentToken != null) { 
		    sentence.add(nextTokInfo);
		    sentences.add(sentence);
		}
	    
	    return sentences;
    }

}

