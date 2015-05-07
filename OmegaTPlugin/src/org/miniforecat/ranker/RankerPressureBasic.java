package org.miniforecat.ranker;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.suggestions.SuggestionsOutput;

public class RankerPressureBasic extends RankerShared {

	protected static double[][] pressures;
	// List of (((x,y),(xx,yy)),(pressure)
	protected static ArrayList<Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>> alignments;

	public static void setPressures(double[][] p) {
		pressures = p;
	}

	public static double[][] getPressures() {
		return pressures;
	}

	public static void setAlignments(
			ArrayList<Pair<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, Double>> a) {
		alignments = a;

	}

	@Override
	public List<SuggestionsOutput> rankerService(RankerInput rankinp, List<SuggestionsOutput> input)
			throws BboxcatException {
		ArrayList<SuggestionsOutput> outputSuggestionsList = new ArrayList<SuggestionsOutput>();
		ArrayList<Integer> sortList = new ArrayList<Integer>();
		SuggestionsOutput so;
		double acumPressure = 0;

		for (int index = 0; index < input.size(); index++) {
			sortList.add(index);
			so = input.get(index);
			acumPressure = 0.0;

			for (int i = 0; i < so.getNumberWords() && so.getPosition() + i < pressures.length; i++) {
				acumPressure += pressures[so.getPosition() + i][rankinp.getPosition()];
			}

			so.setSuggestionFeasibility(acumPressure);
		}
		Quicksort q = new Quicksort();
		q.sort(sortList, input);

		for (int index = 0; index < maxSuggestions && index < input.size(); index++) {
			outputSuggestionsList.add(input.get(sortList.get(sortList.size() - index - 1)));
		}

		return outputSuggestionsList;
	}

}
