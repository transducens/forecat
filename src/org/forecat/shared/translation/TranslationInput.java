package org.forecat.shared.translation;

import java.io.Serializable;

import org.forecat.client.translation.TranslationInputJso;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class TranslationInput implements Serializable, IsSerializable {

	String sourceText;
	String sourceCode;
	String targetCode;
	int maxSegmentLength;
	int minSegmentLength;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
	protected TranslationInput() {
	}

	public TranslationInput(String sourceText, String sourceCode, String targetCode,
			int maxSegmentLength, int minSegmentLength) {
		this.sourceText = sourceText;
		this.sourceCode = sourceCode;
		this.targetCode = targetCode;
		this.maxSegmentLength = maxSegmentLength;
		this.minSegmentLength = minSegmentLength;
	}

	public TranslationInput(TranslationInputJso jso) {
		this.sourceText = jso.getSourceText();
		this.sourceCode = jso.getSourceCode();
		this.targetCode = jso.getTargetCode();
		this.maxSegmentLength = jso.getMaxSegmentLength();
		this.minSegmentLength = jso.getMinSegmentLength();
	}

	public String getSourceText() {
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public int getMaxSegmentLenth() {
		return maxSegmentLength;
	}

	public void setMaxSegmentLength(int maxSegmentLength) {
		this.maxSegmentLength = maxSegmentLength;
	}

	public int getMinSegmentLenth() {
		return minSegmentLength;
	}

	public void setMinSegmentLength(int maxSegmentLength) {
		this.minSegmentLength = maxSegmentLength;
	}
}
