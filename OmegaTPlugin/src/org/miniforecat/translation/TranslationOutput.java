package org.miniforecat.translation;

public class TranslationOutput {

	int numberSegments;
	int maxNumberSegments;

	protected TranslationOutput() {
	}

	public TranslationOutput(int numberSegments, int maxNumberSegments) {
		this.numberSegments = numberSegments;
		this.maxNumberSegments = maxNumberSegments;
	}

	public int getNumberSegments() {
		return numberSegments;
	}

	public void setNumberSegments(int numberSegments) {
		this.numberSegments = numberSegments;
	}

	public int getMaxNumberSegments() {
		return maxNumberSegments;
	}

	public void setMaxNumberSegments(int maxNumberSegments) {
		this.maxNumberSegments = maxNumberSegments;
	}

}
