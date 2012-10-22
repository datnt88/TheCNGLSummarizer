package ie.dcu.cngl.tokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * Holds derived during tokenization.
 * @author Johannes Levelling
 *
 */
public class TokenInfo extends UnitInfo {
    private String repl;
    private String poS;
    private String lemmaSuf;
    private int lemmaOff;
    private int start;
    private int len;
    private int lineNum;
    private boolean flag;
    
    public TokenInfo(String s) {
    	super(s);
		this.repl = null;
		this.poS = null;
		this.lemmaSuf = StringUtils.EMPTY;
		this.lemmaOff = 0;
		this.start = -1;
		this.len = -1;
		this.flag = false;
    }

    public void setRepl(String r) {
		this.repl = r;
    }

    public void setPoS(String p) {
		this.poS = p;
    }

    public void setLemmaSuffix(String l) {
		this.lemmaSuf = l;
    }

    public void setLemmaOffset(int o) {
		this.lemmaOff = o;
    }

    public void setStart(int n) {
		this.start = n;
    }

    public void setLength(int l) {
		this.len = l;
    }

    public void setFlag(boolean b) {
		this.flag = b;
    }

    public String getRepl() {
		return this.repl;
    }

    public String getPoS() {
		return this.poS;
    }

    public String getLemmaSuffix() {
		return this.lemmaSuf;
    }

    public int getLemmaOffset() {
		return this.lemmaOff;
    }

    public String getLemma() {
		String token = getValue();
		String base = token.substring(0, token.length() + getLemmaOffset());
		String suffix = getLemmaSuffix();
		return base + suffix;
    }

    public int getStart() {
		return this.start;
    }

    public int getLength() {
		return this.len;
    }

    public boolean getFlag() {
		return this.flag;
    }
    
    public String toString() {
        return value; 
    }

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public int getLineNum() {
		return lineNum;
	}


}