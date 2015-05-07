package org.omegat.plugins.autocomplete.forecat.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.editor.autocompleter.AutoCompleter;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.plugins.autocomplete.forecat.ForecatPTS;

public class ForecatAutocomplete extends AutoCompleter {

	public ForecatAutocomplete(EditorTextArea3 editor, AutoCompleter parent) {
		super(editor);


		Field jPopup, editorTextArea, onMacBool, visibleBool, viewsList, currentViewInt, scrollJscroll, labelJlabel;
		try {
			jPopup 			= AutoCompleter.class.getDeclaredField("popup");
			editorTextArea 	= AutoCompleter.class.getDeclaredField("editor");
			onMacBool 		= AutoCompleter.class.getDeclaredField("onMac");
			visibleBool 	= AutoCompleter.class.getDeclaredField("visible");
			viewsList 		= AutoCompleter.class.getDeclaredField("views");
			currentViewInt 	= AutoCompleter.class
					.getDeclaredField("currentView");
			scrollJscroll 	= AutoCompleter.class.getDeclaredField("scroll");
			labelJlabel 	= AutoCompleter.class.getDeclaredField("viewLabel");

			jPopup			.setAccessible(true);
			editorTextArea	.setAccessible(true);
			onMacBool		.setAccessible(true);
			visibleBool		.setAccessible(true);
			viewsList		.setAccessible(true);
			currentViewInt	.setAccessible(true);
			scrollJscroll	.setAccessible(true);
			labelJlabel		.setAccessible(true);
			
			jPopup			.set(this, jPopup			.get(parent));
			editorTextArea	.set(this, editorTextArea   .get(parent));
			onMacBool		.set(this, onMacBool		.get(parent));
			visibleBool		.set(this, visibleBool		.get(parent));
			viewsList		.set(this, viewsList		.get(parent));
			currentViewInt	.set(this, currentViewInt	.get(parent));
			scrollJscroll	.set(this, scrollJscroll	.get(parent));
			labelJlabel		.set(this, labelJlabel		.get(parent));
			
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException nsfe) {
			nsfe.printStackTrace(System.err);
			System.exit(-1);
		}

	}

	@Override
	public void doSelection() {
		Method getSelectedValueMethod = null;
		try {
			getSelectedValueMethod = AutoCompleter.class.getDeclaredMethod("getSelectedValue");
			getSelectedValueMethod.setAccessible(true);
			ForecatPTS.useSuggestion((AutoCompleterItem) getSelectedValueMethod.invoke(this));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		super.doSelection();
		setVisible(true);
	}

}
