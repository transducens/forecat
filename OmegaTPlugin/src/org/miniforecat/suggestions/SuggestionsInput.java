package org.miniforecat.suggestions;

public class SuggestionsInput {

	String targetText;
	String prefixText;
	int position;

	protected SuggestionsInput() {
	}

	public SuggestionsInput(String targetText, String prefixText, int position) {
		this.targetText = targetText;
		this.prefixText = prefixText;
		this.position = position;
	}

	public String getTargetText() {
		return targetText;
	}

	public void setTargetText(String targetText) {
		this.targetText = targetText;
	}

	public int getPrefixStart() {
		if (targetText.equals(""))
			return 0;
		return targetText.split(" ").length;
	}

	public String getPrefixText() {
		return prefixText;
	}

	public void setPrefixText(String prefixText) {
		this.prefixText = prefixText;
	}

	public int getPosition() {
		return position;
	}

}
