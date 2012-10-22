package ie.dcu.cngl.summarizer;

import ie.dcu.cngl.summarizer.feature.Feature;
import ie.dcu.cngl.tokenizer.PageStructure;
import ie.dcu.cngl.tokenizer.TokenInfo;

import java.util.ArrayList;

/**
 * Provides interface for weighting sentences being considered for inclusion in end summary.
 * @author Shane
 *
 */
public interface IWeighter {
	
	/**
	 * Adds the weights returned by each feature to the provided weights list.
	 * @param weights Target location for calculated weights (may contain pre-calculated weights)
	 */
	public void calculateWeights(ArrayList<Double[]> weights);
	
	/**
	 * Adds feature to feature list
	 * @param feature Feature to be executed
	 */
	public void addFeature(Feature feature);

	/**
	 * Sets title tokens for features that require it.
	 * @param title Title tokens
	 */
	public void setTitle(ArrayList<TokenInfo> title);

	/**
	 * Sets query tokens for features that require it.
	 * @param query Tokens of query
	 */
	public void setQuery(ArrayList<TokenInfo> query);
        
        /**
	 * Sets phrase query tokens for features that require it.
	 * @param query Tokens of query
	 */
	public void setPhraseQuery(ArrayList<TokenInfo> query);

	/**
	 * Sets page structure for features that require it.
	 * @param structure PageStructure of content
	 */
	public void setStructure(PageStructure structure);

}
