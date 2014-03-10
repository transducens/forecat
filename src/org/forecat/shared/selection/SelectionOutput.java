package org.forecat.shared.selection;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class SelectionOutput implements Serializable, IsSerializable {

	int numberSegments;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
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
