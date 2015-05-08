package org.forecat.shared.utils;

import java.util.ArrayList;

/**
 * List of 4-tuples
 * 
 * @author Daniel Torregrosa
 * 
 */
public class QuadList {

	public class Quad {
		public int cost, insert, delete, replace;

		public Quad() {
			cost = insert = delete = replace = 0;
		}

		public Quad(int cost, int insert, int delete, int replace) {
			this.cost = cost;
			this.insert = insert;
			this.delete = delete;
			this.replace = replace;
		}

		public Quad(Quad q) {
			this.cost = q.cost;
			this.insert = q.insert;
			this.delete = q.delete;
			this.replace = q.replace;
		}
	}

	public ArrayList<Quad> elements;

	public QuadList() {
		elements = new ArrayList<Quad>();
	}

	public QuadList(Quad q) {
		elements = new ArrayList<Quad>();
		elements.add(q);
	}

	public QuadList(int cost, int insert, int delete, int replace) {
		elements = new ArrayList<Quad>();
		elements.add(new Quad(cost, insert, delete, replace));
	}

	public void addSame(Quad q) {
		elements.add(new Quad(q.cost, q.insert, q.delete, q.replace));
	}

	public void addReplace(Quad q) {
		elements.add(new Quad(q.cost + 1, q.insert, q.delete, q.replace + 1));
	}

	public void addInsert(Quad q) {
		elements.add(new Quad(q.cost + 1, q.insert + 1, q.delete, q.replace));
	}

	public void addDelete(Quad q) {
		elements.add(new Quad(q.cost + 1, q.insert, q.delete + 1, q.replace));
	}
}
