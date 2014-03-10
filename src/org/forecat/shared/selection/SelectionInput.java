package org.forecat.shared.selection;

import java.io.Serializable;

import org.forecat.client.selection.SelectionInputJso;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class SelectionInput implements Serializable, IsSerializable {

	private String selectionText;
	private int position;
	private String id;

	/**
	 * From Serializable documentation
	 * (http://docs.oracle.com/javase/1.5.0/docs/api/java/io/Serializable.html): "To allow subtypes
	 * of non-serializable classes to be serialized, the subtype may assume responsibility for
	 * saving and restoring the state of the supertype's public, protected, and (if accessible)
	 * package fields. The subtype may assume this responsibility only if the class it extends has
	 * an accessible no-arg constructor to initialize the class's state. It is an error to declare a
	 * class Serializable if this is not the case. The error will be detected at runtime."
	 */
	protected SelectionInput() {
	}

	public SelectionInput(String selectionText, int position) {
		this.selectionText = selectionText;
		this.position = position;
	}

	public SelectionInput(SelectionInputJso jso) {
		this.selectionText = jso.getSelectionText();
		this.position = jso.getPosition();
		this.id = jso.getId();
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
