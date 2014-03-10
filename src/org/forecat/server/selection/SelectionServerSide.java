package org.forecat.server.selection;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.SessionShared;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.selection.SelectionPrefixShared;

public class SelectionServerSide extends SelectionPrefixShared {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2129990064220856583L;

	@Override
	public SelectionOutput selectionService(SelectionInput input, SessionShared session)
			throws ForecatException {

		SelectionOutput output = super.selectionService(input, session);

		return output;
	}

	// This code is left just to show how to use different regular expression libraries in
	// client/server sides:
	/*
	 * @Override protected boolean isPrefix(String possiblePrefix, String string) { return
	 * UtilsServerSide.isPrefix(possiblePrefix, string); }
	 */
}
