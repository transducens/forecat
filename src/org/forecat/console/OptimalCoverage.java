package org.forecat.console;

import java.util.ArrayList;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.shared.SessionBrowserSideConsole;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;

public class OptimalCoverage {

	/**
	 * Fills the optimal coverage matrix, returning the optimal sequence of actions
	 * 
	 * @param session
	 *            Current session
	 * @param target
	 *            Target sentence
	 * @param targetSplit
	 *            Target sentence split at word level
	 * @param coverage
	 *            Matrix with the cost of each position
	 * @param sources
	 *            Previous cost of each cell
	 * @param previous
	 *            Previous position of each cell
	 * @param offered
	 *            Offered suggestions for each cell
	 * @param bestSuggestionId
	 *            Best suggestion for each cell
	 * @param decisions
	 *            Sequence of optimal decisions
	 * @param targetLengths
	 *            Length of each target word
	 * @param shortestUsed
	 *            Number of used suggestions
	 * @param shortestOffered
	 *            Number of offered suggestions
	 */
	public static void fillOptimalCoverage(SessionBrowserSideConsole session, String target,
			String[] targetSplit, int[][] coverage, int[][] sources, Integer[][][] previous,
			int[] offered, String[][] bestSuggestionId, String[] decisions, int[] targetLengths,
			int shortestUsed, int shortestOffered) {
		int i = 0;
		int charCount = 0;
		String auxSegment = "";
		int targetWords = targetSplit.length;
		while (i < targetWords) {

			String currentSegment = "" + targetSplit[i].charAt(0);

			SuggestionsInput inputSuggestions = new SuggestionsInput(auxSegment, currentSegment, i);

			ArrayList<SuggestionsOutput> outputSuggestionsList = null;
			try {
				outputSuggestionsList = (ArrayList<SuggestionsOutput>) Main.suggestions
						.suggestionsService(inputSuggestions, session);
			} catch (ForecatException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				System.exit(1);
			}

			if (Main.useManageCaps) {
				Main.manageCaps(outputSuggestionsList, currentSegment);
			}

			for (SuggestionsOutput s : outputSuggestionsList) {

				int end = charCount + s.getSuggestionText().length();

				if (end > target.length()) {
					continue;
				}
				String targetSubstring = target.substring(charCount, end);

				// System.out.println("> " + targetSubstring);
				if (s.getSuggestionText().equals(targetSubstring)
						&& (end == target.length() || Character.isWhitespace(target.charAt(end)))) {

					if (currentSegment.length() == 1) {
						int sugWords = s.getSuggestionText().split(" ").length - 1;

						if (sugWords > 0 || targetSplit[i].length() > Main.suggestionSelectPenalty) {
							coverage[i][sugWords] = currentSegment.length()
									+ Main.suggestionSelectPenalty;

							bestSuggestionId[i][sugWords] = s.getId();

							previous[i][sugWords][0] = -1;
							previous[i][sugWords][1] = -1;
						}

					}
				}
			}
			auxSegment += targetSplit[i] + " ";
			charCount = auxSegment.length();
			i++;
		}

		for (int j = 0; j < targetWords; j++) {
			for (int k = 0; k < targetWords && j + k < targetWords; k++) {
				if (coverage[j][k] == 0) {
					if (k > 0) {
						coverage[j][k] = coverage[j][k - 1] + targetLengths[j + k] + 1;
					} else {
						coverage[j][k] = targetLengths[j + k];
					}
					previous[j][k][0] = j;
					previous[j][k][1] = k - 1;
				}
			}

		}

		for (int k = 1; k < targetWords; k++) {
			for (int j = 0; j < targetWords && j + k < targetWords; j++) {
				for (int l = 0; l < k; l++) {
					if (coverage[j][k] > 1 + (coverage[j][k - 1 - l] + coverage[k - l + j][l])) {
						coverage[j][k] = 1 + coverage[j][k - 1 - l] + coverage[k - l + j][l];
						sources[j][k] = k - l;
						previous[j][k][0] = k - l + j;
						previous[j][k][1] = l;
					}
				}
			}
		}

		ArrayList<Integer[]> pq2 = new ArrayList<Integer[]>();

		Integer[] pair = { 0, targetWords - 1 }, prevPair;
		pq2.add(pair);

		while (!pq2.isEmpty()) {
			pair = pq2.get(0);
			pq2.remove(0);

			if (pair[0] == (-1) || pair[1] == (-1)) {

			} else {
				prevPair = previous[pair[0]][pair[1]];
				if (pair[0].compareTo(prevPair[0]) == 0 && pair[1].compareTo(prevPair[1] + 1) == 0) {
					// Viene de la posición anterior, escrito a mano
					if (offered[pair[0] + pair[1]] > 0)
						shortestOffered += offered[pair[0] + pair[1]];
					targetSplit[pair[0] + pair[1]] = "|" + targetSplit[pair[0] + pair[1]];
					if (decisions[pair[0] + pair[1]] == null) {
						decisions[pair[0] + pair[1]] = "";
					} else {
						System.err.println("ERROR");
						System.exit(-1);
					}
					// System.out.println("ADD \t" + prevPair[0] + "\t" + prevPair[1]);
					pq2.add(prevPair);
				} else {

					if (prevPair[0] == (-1)) {
						// Se ha usado una sugerencia
						shortestOffered++;
						shortestUsed++;
						targetSplit[pair[0] + pair[1]] += "]";
						targetSplit[pair[0]] = "[" + targetSplit[pair[0]];
						if (decisions[pair[0]] == null) {
							decisions[pair[0]] = bestSuggestionId[pair[0]][pair[1]];
						} else {
							System.err.println("ERROR");
							System.exit(-1);
						}
						continue;
					} else {
						// Se ha saltado porque otra rama del árbol tenía mejor coste
						targetSplit[pair[0] + pair[1]] += ">";
						targetSplit[pair[1] - prevPair[1]] = "<"
								+ targetSplit[pair[1] - prevPair[1]];
					}

					pq2.add(prevPair);

					pair[1] = pair[1] - prevPair[1] - 1;

					pq2.add(pair);
				}
			}

		}
	}
}
