/**
 * 
 */
package org.forecat.client.exceptions;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author japerez
 * 
 */
public class ForecatException extends Exception implements Serializable, IsSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1270850604041273140L;

	/**
	 * 
	 */
	public ForecatException() {
	}

	/**
	 * @param message
	 */
	public ForecatException(String message) {
		super(message);
	}

}
