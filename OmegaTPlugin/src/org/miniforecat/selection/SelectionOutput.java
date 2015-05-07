package org.miniforecat.selection;

public class SelectionOutput {

	int numberSegments;

	protected SelectionOutput() {
	}

	public SelectionOutput(int numberSegments) {
		this.numberSegments = numberSegments;
	}

	public int getNumberSegments() {
		return numberSegments;
	}

	public void setNumberSegments(int numberSegments) {
		this.numberSegments = numberSegments;
	}
}
