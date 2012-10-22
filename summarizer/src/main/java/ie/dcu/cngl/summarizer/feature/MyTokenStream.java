package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.tokenizer.TokenInfo;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * MyTokenStream allows use of our own customized tokenizer
 * @author Shane
 *
 */
public class MyTokenStream extends TokenStream {
    
	private CharTermAttribute charTermAtt;
    private OffsetAttribute offsetAtt;
    private final Iterator<TokenInfo> listOfTokens;

    /**
     * Initializes tokenstream with an iterator over our own tokens
     * @param tokenList our own pretokenized tokens
     */
    public MyTokenStream(Iterator<TokenInfo> tokenList) {
        listOfTokens = tokenList;
        charTermAtt = addAttribute(CharTermAttribute.class);
        offsetAtt = addAttribute(OffsetAttribute.class);

    }

    @Override
    public boolean incrementToken() throws IOException {
        if(listOfTokens.hasNext()) {
            super.clearAttributes();
            TokenInfo myToken = listOfTokens.next();
            charTermAtt.setLength(0);
            charTermAtt.append(myToken.getValue());
            offsetAtt.setOffset(myToken.getStart(), myToken.getStart()+myToken.getLength());
            return true;
        }
        return false;
    }
    
}