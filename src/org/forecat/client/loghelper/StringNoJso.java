package org.forecat.client.loghelper;

public class StringNoJso {
	String text;

	public StringNoJso(String text) {
		this.text = text;
	}

	public StringNoJso(StringJso sj) {
		text = sj.getText();
	}

	public String getText() {
		return text;
	}
}
