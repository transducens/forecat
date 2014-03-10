package org.forecat.shared.suggestions;

import java.io.Serializable;

import org.forecat.client.suggestions.SuggestionsInputJso;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class SuggestionsInput implements Serializable, IsSerializable {

	String targetText;
	/**
	 * Word-level start position of the current prefix.
	 */
	String prefixText;

	/**
	 * Number of words we have typed
	 */
	int position;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
	protected SuggestionsInput() {
	}

	/**
	 * Constructor to be used in org.forecat.client.suggestions.SuggestionsBridge.
	 * 
	 * @param targetText
	 * @param prefixStart
	 * @param prefixText
	 */
	public SuggestionsInput(String targetText, String prefixText, int position) {
		this.targetText = targetText;
		this.prefixText = prefixText;
		this.position = position;
	}

	/**
	 * Constructor to be used in Main.
	 * 
	 * @param jso
	 */
	public SuggestionsInput(SuggestionsInputJso jso) {
		this.targetText = jso.getTargetText();
		this.prefixText = jso.getPrefixText();
		this.position = jso.getPosition();
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
