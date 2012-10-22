package ie.dcu.cngl.tokenizer;

import java.util.*;

import org.apache.commons.lang.StringUtils;

/**
 * Separates the tokenized content into paragraphs. A paragraph is marked by a double newline.
 * @author Shane
 *
 */
public class Paragrapher implements IParagrapher {
	
	private static Paragrapher instance;
    private Tokenizer tokenizer;
    
    private Paragrapher(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
    }

    /**
     * Initializing a paragrapher is computationally expensive, so it exists as a singleton.
     * @return Paragrapher singleton.
     */
    public static Paragrapher getInstance() {
    	if(instance == null) {
    		synchronized(Paragrapher.class) {
	    		Tokenizer tokenizer = Tokenizer.getInstance();
	    		instance = new Paragrapher(tokenizer);
    		}
    	}
    	return instance;
    }

    /**
     * Tokenize the content, and divide the tokens by paragraph.
     * @return A 2-dimensional array of each paragraph and its tokens.
     */
    public synchronized ArrayList<ArrayList<TokenInfo>> paragraph(String s) {
		ArrayList<TokenInfo> tok_vec = tokenizer.tokenize(s);
		if (tok_vec == null)
		    return null;
		
		ArrayList<TokenInfo> sentence = new ArrayList<TokenInfo>();
		ArrayList<ArrayList<TokenInfo>> sentences = new ArrayList<ArrayList<TokenInfo>>();
		
		TokenInfo currentTokInfo = null;
		TokenInfo nextTokInfo = null;
		String currentToken = StringUtils.EMPTY, nextToken = StringUtils.EMPTY;
		
		int tok_pos = 0;
		int tok_len = tok_vec.size();
	    while (tok_pos < tok_len) {
	    	//Update all token info
		    currentTokInfo = nextTokInfo;
		    nextTokInfo = (TokenInfo)tok_vec.get(tok_pos);
		    
		    //Update token str values
	        currentToken = nextToken;
	        nextToken = nextTokInfo.getValue();

		    if (currentTokInfo != null)
				sentence.add(currentTokInfo);

		    if (currentTokInfo != null && nextTokInfo != null && (currentTokInfo.getStart() + currentTokInfo.getLength() > nextTokInfo.getStart()-1)) {
		    	; // only break after whitespace
		    } else if (sentence.size() > 0 && currentToken != null
		    		&& (currentTokInfo.getLineNum() < nextTokInfo.getLineNum() 	//New line
		    				&& nextTokInfo.getStart()-(currentTokInfo.getStart()+currentTokInfo.getLength()) > 1)) {
				sentences.add(sentence);
				sentence = new ArrayList<TokenInfo>();
		    }
		    tok_pos++;
		}
		
	    if (sentence.size() > 0 && currentToken != null) { 
		    sentence.add(nextTokInfo);
		    sentences.add(sentence);
		}
	    
	    return sentences;
    }

}

