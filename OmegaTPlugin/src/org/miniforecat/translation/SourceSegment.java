package org.miniforecat.translation;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.translation.SourceSegment;

public class SourceSegment {

	private String sourceSegmentText;
	private int position;
	private List<String> engineList;
	private boolean used;
	private int id = -1;
	private int charPosition;

	protected SourceSegment() {
	}

	public SourceSegment(String sourceSegmentText, int position, boolean used, int id,
			int charposition) {
		this.sourceSegmentText = sourceSegmentText;
		this.position = position;
		this.used = used;
		this.engineList = new ArrayList<String>();
		this.id = id;
		this.charPosition = charposition;
	}

	public SourceSegment(SourceSegment s) {
		this.sourceSegmentText = s.sourceSegmentText;
		this.position = s.position;
		this.used = s.used;
		this.engineList = new ArrayList<String>(s.engineList);
		this.id = s.id;
	}

	public String getSourceSegmentText() {
		return sourceSegmentText;
	}

	public void setSourceSegmentText(String sourceSegmentText) {
		this.sourceSegmentText = sourceSegmentText;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public void addEngine(String engine) {
		engineList.add(engine);
	}

	public List<String> getEngineList() {
		return engineList;
	}

	public static SourceSegment searchByTextAndPosition(List<SourceSegment> list,
			String sourceSegment, int position) {
		for (int i = 0, n = list.size(); i < n; ++i) {
			if (list.get(i).getSourceSegmentText().equals(sourceSegment)
					&& list.get(i).getPosition() == position) {
				return list.get(i);
			}
		}
		return null;
	}

	public String getId() {
		return id + "";
	}

	public int getCharPosition() {
		return charPosition;
	}

	public void setCharPosition(int charPosition) {
		this.charPosition = charPosition;
	}
}
