package org.miniforecat.suggestions;


public class SuggestionsOutput implements Comparable<SuggestionsOutput>{

	String suggestionText;
	double suggestionFeasibility;
	int position;
	int numberWords;
	String id;

	protected SuggestionsOutput() {
	}

	public SuggestionsOutput(String suggestionText, double suggestionFeasibility, String id,
			int position, int numberWords) {
		this.suggestionText = suggestionText;
		this.suggestionFeasibility = suggestionFeasibility;
		this.id = id;
		this.position = position;
		this.numberWords = numberWords;
	}

	public void setSuggestionText(String suggestionText) {
		this.suggestionText = suggestionText;
	}

	public String getSuggestionText() {
		return suggestionText;
	}

	public void setSuggestionFeasibility(double suggestionFeasibility) {
		this.suggestionFeasibility = suggestionFeasibility;
	}

	public double getSuggestionFeasibility() {
		return suggestionFeasibility;
	}

	public String getId() {
		return id;
	}

	public int getPosition() {
		return position;
	}

	public int getNumberWords() {
		return numberWords;
	}

	@Override
	public int compareTo(SuggestionsOutput s) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (this == s) {
			return EQUAL;
		}

		int comparison = Double.compare(this.suggestionFeasibility, s.suggestionFeasibility);
		if (comparison != EQUAL) {
			return -comparison;
		}

		if (this.suggestionText.length() < s.suggestionText.length()) {
			return BEFORE;
		}
		if (this.suggestionText.length() > s.suggestionText.length()) {
			return AFTER;
		}

		return this.suggestionText.compareTo(s.suggestionText);

	}
}
