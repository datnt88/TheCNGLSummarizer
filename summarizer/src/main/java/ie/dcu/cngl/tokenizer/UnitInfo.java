package ie.dcu.cngl.tokenizer;

/**
 * Basic unit info.
 * @author Shane
 *
 */
public class UnitInfo {
	
    protected String value;
    
    public UnitInfo(String value) {
    	this.value = value;
    }
    
    public void setValue(String value) {
    	this.value = value;
    }
    
    public String getValue() {
    	return this.value;
    }
    
}
