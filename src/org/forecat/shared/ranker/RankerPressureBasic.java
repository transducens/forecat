package org.forecat.shared.ranker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.utils.Quicksort;

/**
 * Chooses the suggestions according to the light aligning model described in
 * 
 * Miquel Esplà-Gomis, Felipe Sánchez-Martínez, Mikel L Forcada. A Simple Approach to Use Bilingual
 * Information Sources for Word Alignment. En Procesamiento del Lenguaje Natural, 49 (XXVIII
 * Conferència de la Sociedad Española de Procesamiento del Lenguaje Natural, 5-7.9.2012, Castelló
 * de la Plana), p. 93–100.
 * 
 * @author Daniel Torregrosa
 * 
 */
public class RankerPressureBasic extends RankerShared {

	private static final long serialVersionUID = -943641721379662135L;

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
			throws ForecatException {
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
