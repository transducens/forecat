package org.forecat.shared.translation;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class TranslationOutput implements Serializable, IsSerializable {

	int numberSegments;
	int maxNumberSegments;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
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
