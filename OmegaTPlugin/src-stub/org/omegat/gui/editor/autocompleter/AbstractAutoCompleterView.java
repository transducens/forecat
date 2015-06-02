package org.omegat.gui.editor.autocompleter;

public interface AbstractAutoCompleterView {

	AutoCompleterItem getSelectedValue();

	boolean shouldCloseOnSelection();

}
