package org.forecat.client.utils;

public final class PropertiesBrowserSide {

	private PropertiesBrowserSide() {
	}

	public enum ExecutionModes {
		BROWSER_MODE, SERVER_MODE, MIXED_MODE
	};

	/**
	 * Execution mode. This is only used in client code.
	 */
	// public static final ExecutionModes executionMode = ExecutionModes.SERVER_MODE;
	public static final ExecutionModes executionMode = ExecutionModes.MIXED_MODE;

}
