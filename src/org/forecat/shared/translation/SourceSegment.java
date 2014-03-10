package org.forecat.shared.translation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

// Classes must be serializable if they are to be stored in the HttpSession object

@SuppressWarnings("serial")
public class SourceSegment implements Serializable, IsSerializable {
	/**
	 * Text of the source segment.
	 */
	private String sourceSegmentText;

	/**
	 * Word-level position of the source segment in the input text.
	 */
	private int position;

	/**
	 * List of engines which have been used to translate this source segment into a particular
	 * target segment.
	 */
	private List<String> engineList;
	private boolean used;

	/**
	 * Id of the Sourcesegment
	 */
	private int id = -1;

	/**
	 * Translation of the source segment
	 */
	private String translation;

	/**
	 * Char position of the suggestion
	 */
	private int charPosition;

	// /**
	// * Method of getting the next id. Linked to session
	// */
	// static private SessionShared session;
	//
	// public static void setSession(SessionShared ss) {
	// session = ss;
	// }
	//
	// private int getNextId() {
	// Object theId = session.getAttribute("SourceSegmentId");
	// if (theId == null) {
	// session.setAttribute("SourceSegmentId", 1);
	// return 1;
	// }
	//
	// Integer id = (Integer) theId;
	// session.setAttribute("SourceSegmentId", id + 1);
	// return id;
	// }

	// A constructor to make this class serializable for GWT.
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

	// TODO: implement this as a real "clone" method?
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