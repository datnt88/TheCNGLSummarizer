package ie.dcu.cngl.summarizer.feature;

import org.apache.commons.lang.StringUtils;

/**
 * An affix contains the substring of the actual affix, as
 * well as the affix type.
 * @author Shane
 *
 */
public class Affix {
	
	public enum AffixType {
		PREFIX, SUFFIX, INFIX;
	}
	
	private AffixType type;
	private String affix;
	
	public Affix(String affix) throws Exception {
		boolean hyphenAtStart = affix.startsWith("-");
		boolean hyphenAtEnd = affix.endsWith("-");
		if(hyphenAtStart && hyphenAtEnd) {
			this.type = AffixType.INFIX;
		} else if(hyphenAtStart) {
			this.type = AffixType.SUFFIX;
		} else if(hyphenAtEnd) {
			this.type = AffixType.PREFIX;
		} else {
			throw new Exception("Incorrect affix format, \"" + affix + "\" requires appropriate hyphenation.");
		}
		this.affix = affix.replace("-", StringUtils.EMPTY);
	}

	public AffixType getType() {
		return this.type;
	}
	
	public String getAffix() {
		return this.affix;
	}
	
}
