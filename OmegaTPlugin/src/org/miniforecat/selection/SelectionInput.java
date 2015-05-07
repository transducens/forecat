package org.miniforecat.selection;

public class SelectionInput {

	private String selectionText;
	private int position;
	private String id;

	protected SelectionInput() {
	}

	public SelectionInput(String selectionText, int position) {
		this.selectionText = selectionText;
		this.position = position;
	}

	public String getSelectionText() {
		return selectionText;
	}

	public int getPosition() {
		return position;
	}

	public void setSelectionText(String selectionText) {
		this.selectionText = selectionText;
	}

	public String getId() {
		return id;
	}

}
