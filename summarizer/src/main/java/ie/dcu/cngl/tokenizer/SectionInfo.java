package ie.dcu.cngl.tokenizer;

/**
 * Holds content value and location.
 */
public class SectionInfo extends UnitInfo {
	
	private int location;

	public SectionInfo(String value, int location) {
		super(value);
		this.location = location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public int getLocation() {
		return location;
	}
	
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

}
