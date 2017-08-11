package org.forecat.shared.suggestions;

import java.io.Serializable;

import org.forecat.client.suggestions.SuggestionsInputJso;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class SuggestionsInput implements Serializable, IsSerializable {

	private String fixedPrefix;
	private String lastWordPrefix;
	private String sourceText;
	private int position;
	private boolean fromUsed;
	private int lastUsedStart, lastUsedEnd;

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

	public SuggestionsInput(String fixedPrefix, String lastWordPrefix, int position,
			String sourceText, boolean fromUsed, int lastUsedStart, int lastUsedEnd) {
		this.fixedPrefix = fixedPrefix;
		this.lastWordPrefix = lastWordPrefix;
		this.position = position;
		this.sourceText = sourceText;
		this.fromUsed = fromUsed;
		this.lastUsedStart = lastUsedStart;
		this.lastUsedEnd = lastUsedEnd;
	}

	public String getFixedPrefix() {
		return fixedPrefix;
	}

	public void setFixedPrefix(String fixedPrefix) {
		this.fixedPrefix = fixedPrefix;
	}

	public int getFixedPrefixWordLength() {
		if (fixedPrefix.equals(""))
			return 0;
		return fixedPrefix.split(" ").length;
	}

	public int getFixedPrefixCharLength() {
		return fixedPrefix.length();
	}

	public String getLastWordPrefix() {
		return lastWordPrefix;
	}

	public void setPrefixText(String lastWordPrefix) {
		this.lastWordPrefix = lastWordPrefix;
	}

	public int getPosition() {
		return position;
	}

	public String getSourceText() {
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}

	public int getSourceWordLength() {
		if ("".equals(sourceText))
			return 0;
		return sourceText.split(" ").length;
	}

	public int getSourceCharLength() {
		return sourceText.length();
	}

	public boolean getFromused() {
		return fromUsed;
	}

	public int getLastUsedStart() {
		return lastUsedStart;
	}

	public int getLastUsedEnd() {
		return lastUsedEnd;
	}

	/**
	 * Constructor to be used in Main.
	 * 
	 * @param jso
	 */
	public SuggestionsInput(SuggestionsInputJso jso) {
		this.fixedPrefix = jso.getTargetText();
		this.lastWordPrefix = jso.getPrefixText();
		this.position = jso.getPosition();
	}

}
