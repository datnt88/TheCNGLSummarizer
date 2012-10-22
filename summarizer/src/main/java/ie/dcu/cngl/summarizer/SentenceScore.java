package ie.dcu.cngl.summarizer;

/**
 * Simple tuple containing sentence and its score.
 * @author Shane
 *
 */
public class SentenceScore implements Comparable<SentenceScore> {
	
	private String sentence;
	private double score;

	public SentenceScore(String sentence, double score) {
		this.setSentence(sentence);
		this.score = score;
	}

	@Override
	public int compareTo(SentenceScore other) {
		if(this.score > other.getScore()) return -1;
		if(this.score < other.getScore()) return 1;
		return 0;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getSentence() {
		return sentence;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
}
