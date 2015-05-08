package org.forecat.shared.ranker;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RankerInput implements Serializable, IsSerializable {

	private static final long serialVersionUID = -4156758184297340851L;
	int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public RankerInput(int position) {
		super();
		this.position = position;
	}

	public RankerInput() {
		super();
	}

}
