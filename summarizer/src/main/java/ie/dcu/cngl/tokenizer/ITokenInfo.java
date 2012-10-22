/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ie.dcu.cngl.tokenizer;

import org.apache.commons.lang.StringUtils;


/**
 *
 * @author Kid
 */
public class ITokenInfo extends TokenInfo {

    private int synsetId;

    public ITokenInfo(String s){
        super(s);
        this.synsetId = 0;
    }

    public void setSynset(int synset_){
        this.synsetId = synset_;
    }

    public int getSynset(){
        return this.synsetId;
    }
    

}
