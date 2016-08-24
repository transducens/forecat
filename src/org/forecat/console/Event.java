package org.forecat.console;

import java.util.ArrayList;

public class Event {

	static int nextid = 0;
	int id;
	String sugUsed = "";
	String idused = "";
	int sentence;
	int curr;
	int word;
	char key;
	ArrayList<String> sug;
	ArrayList<String> potSug;

	double precision;
	double recall;

	public double getPrecision() {
		return precision;
	}

	public char getChar() {
		return key;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public boolean hasSuggestions() {
		return !sug.isEmpty();
	}

	public boolean usedSuggestion() {
		return !sugUsed.equals("");
	}

	public int getId() {
		return id;
	}

	public int getSentence() {
		return sentence;
	}

	public Event(char k, int s, int curr, int word) {
		key = k;
		id = nextid;
		nextid++;
		sug = new ArrayList<String>();
		potSug = new ArrayList<String>();
		sentence = s;
		this.curr = curr;
		this.word = word;
		recall = 0;
		precision = 0;
	}

	public void addSuggestion(String id, String score) {
		sug.add(id + ":" + score);
	}

	public void addPotentialSuggestion(String id) {
		potSug.add(id);
	}

	public void useSuggestion(String id, String sug) {
		sugUsed = sug;
		idused = id;
	}

	@Override
	public String toString() {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		sb.append(sentence);
		sb.append("|");
		sb.append(curr);
		sb.append("|");
		sb.append(word);
		sb.append("|");
		sb.append(key);
		sb.append("|{");
		for (String i : sug) {
			if (!first)
				sb.append(";");
			else
				first = false;
			sb.append(i);
		}
		sb.append("}");
		sb.append("|{");
		first = true;
		for (String i : potSug) {
			if (!first)
				sb.append(";");
			else
				first = false;
			sb.append(i);
		}
		sb.append("}");
		sb.append("|");
		sb.append(precision);
		sb.append("|");
		sb.append(recall);
		if (idused != "") {
			sb.append("|");
			sb.append(idused);
			sb.append("|");
			sb.append(sugUsed);
		}
		return sb.toString();
	}
}
