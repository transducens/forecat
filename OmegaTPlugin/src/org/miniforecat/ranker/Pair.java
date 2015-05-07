package org.miniforecat.ranker;

public class Pair<T1, T2> {
	T1 left;
	public Pair(T1 left, T2 right) {
		super();
		this.left = left;
		this.right = right;
	}
	T2 right;
	public T1 getKey() {
		return left;
	}
	public void setKey(T1 left) {
		this.left = left;
	}
	public T2 getValue() {
		return right;
	}
	public void setValue(T2 right) {
		this.right = right;
	}
}
