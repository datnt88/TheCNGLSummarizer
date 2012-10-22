package ie.dcu.cngl.summarizer.feature;

import ie.dcu.cngl.tokenizer.TokenizerUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Feature checking for the existence of terms within sentences.
 * @author Shane
 *
 */
public abstract class TermCheckingFeature extends Feature {
	
	protected ArrayList<String> terms;

	public TermCheckingFeature() throws IOException {
		this.terms = new ArrayList<String>();
		List<String> tempTerms = FileUtils.readLines(new File(getTermsFileName()));
		for(int i = 0; i < tempTerms.size(); i++) {
			String line = tempTerms.get(i);
            if (!(line.equals(StringUtils.EMPTY) || line.startsWith(TokenizerUtils.COMMENT))) {
            	terms.add(line);
            }
		}
	}
	
	/**
	 * @return The name of the file containing the necessary terms.
	 */
	protected abstract String getTermsFileName();

}
