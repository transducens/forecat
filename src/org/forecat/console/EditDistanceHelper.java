package org.forecat.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.forecat.shared.utils.QuadList;

public class EditDistanceHelper {

	/**
	 * Cost of accepting the longest, matching suggestion using the TAB key. Each time the user
	 * presses tab, one word of the suggestion gets added to the translation
	 * 
	 * @param suffix
	 *            Suffix of the typed prefix
	 * @param suggestion
	 *            Suggestion to manage
	 * @param newLength
	 *            New length of the suggestion
	 * @return
	 */
	private static int getSimpleTabAcceptDistance(String suffix, String suggestion, int newLength) {

		if (suffix.length() < suggestion.length()) {
			return Integer.MAX_VALUE;
		}

		if (suffix.equals(suggestion)) {
			return 0;
		}
		int suggestionLength = suggestion.length();
		int i = 0, numberSpaces = 0, lastSpace = 0;

		// System.out.println(suffix + "|" + suggestion);

		while (i < suggestionLength && suffix.charAt(i) == suggestion.charAt(i)) {
			if (suggestion.charAt(i) == ' ') {
				numberSpaces++;
				lastSpace = i;
			}
			i++;
		}

		if (lastSpace > 0) {
			newLength = lastSpace;
		}

		if (i == suggestionLength)
			return 1;
		else
			return numberSpaces;
	}

	/**
	 * Edit distance with a limited amount of actions
	 * 
	 * @param suffix
	 *            Suffix of the typed prefix
	 * @param suggestion
	 *            Suggestion to manage
	 * @param maxInserts
	 *            Maximum number of inserts
	 * @param maxDeletes
	 *            Maximum number of deletes
	 * @param maxReplacements
	 *            Maximum number of replacements
	 * @return
	 */
	static int getLimitedActionsEditDistance(String suffix, String suggestion, int maxInserts,
			int maxDeletes, int maxReplacements) {
		int X = suffix.length() + 1, Y = suggestion.length() + 1;
		QuadList[][] d = new QuadList[X][Y];
		int x = 0, y = 0, currentBest;

		for (x = 0; x < X; x++) {
			if (x <= maxInserts)
				d[x][0] = new QuadList(x, x, 0, 0);
			else
				d[x][0] = new QuadList();
		}
		for (y = 0; y < Y; y++) {
			if (y <= maxDeletes)
				d[0][y] = new QuadList(y, 0, y, 0);
			else
				d[0][y] = new QuadList();
		}
		// System.out.println(suffix + " > " + suggestion + " " + maxInserts + "," + maxDeletes +
		// ","
		// + maxReplacements);
		// for (int xx = 0; xx < X; xx++) {
		// for (int yy = 0; yy < Y; yy++) {
		// System.out
		// .print((d[xx][yy] != null && !d[xx][yy].elements.isEmpty() ? d[xx][yy].elements
		// .get(0).cost : "-")
		// + "\t");
		// }
		// System.out.println();
		// }
		// System.out.println("--");

		// We need to keep track not only of the best score for every cell,
		// but also the number of actions we took for getting there.
		for (x = 1; x < X; x++) {
			for (y = 1; y < Y; y++) {

				// for (int xx = 0; xx < X; xx++) {
				// for (int yy = 0; yy < Y; yy++) {
				// System.out
				// .print((d[xx][yy] != null && !d[xx][yy].elements.isEmpty() ? d[xx][yy].elements
				// .get(0).cost : "-")
				// + "\t");
				// }
				// System.out.println();
				// }
				// System.out.println("--");

				d[x][y] = new QuadList();
				if (suffix.charAt(x - 1) == (suggestion.charAt(y - 1))) {
					for (QuadList.Quad q : d[x - 1][y - 1].elements) {
						d[x][y].addSame(q);
					}
				} else {
					currentBest = Integer.MAX_VALUE;
					// Check for replacements
					if (!d[x - 1][y - 1].elements.isEmpty()) {
						for (QuadList.Quad q : d[x - 1][y - 1].elements) {
							// For every element in the previous cell
							// System.out.println("REPLACE " + q.replace + "|" + maxReplacements);
							if (q.replace < maxReplacements) {
								if (q.cost + 1 < currentBest) {
									// If we found one with better cost, clean the current list
									currentBest = q.cost + 1;
									d[x][y].elements.clear();
								}
								// If we found one with the same cost as the best, add it
								if (q.cost + 1 == currentBest)
									d[x][y].addReplace(q);
							}
						}
					}
					// Check for deletions
					if (!d[x][y - 1].elements.isEmpty()) {
						for (QuadList.Quad q : d[x][y - 1].elements) {
							// System.out.println("DELETE " + q.delete + "|" + maxDeletes);
							if (q.delete < maxDeletes) {
								if (q.cost + 1 < currentBest) {
									currentBest = q.cost + 1;
									d[x][y].elements.clear();
								}
								if (q.cost + 1 == currentBest)
									d[x][y].addDelete(q);
							}
						}
					}
					// Check for insertions
					if (!d[x - 1][y].elements.isEmpty()) {
						for (QuadList.Quad q : d[x - 1][y].elements) {
							// System.out.println("INSERT " + q.insert + "|" + maxInserts);
							if (q.insert < maxInserts) {
								if (q.cost + 1 < currentBest) {
									currentBest = q.cost + 1;
									d[x][y].elements.clear();
								}
								if (q.cost + 1 == currentBest)
									d[x][y].addInsert(q);
							}
						}
					}
				}
			}
		}

		// for (int xx = 0; xx < X; xx++) {
		// for (int yy = 0; yy < Y; yy++) {
		// System.out
		// .print((d[xx][yy] != null && !d[xx][yy].elements.isEmpty() ? d[xx][yy].elements
		// .get(0).cost : "-")
		// + "\t");
		// }
		// System.out.println();
		// }
		// System.out.println("--");

		return d[X - 1][Y - 1].elements.isEmpty() ? Integer.MAX_VALUE : d[X - 1][Y - 1].elements
				.get(0).cost;
	}

	/**
	 * Cost of accepting the sugestion and deleting the non-matching suffix
	 * 
	 * @param suffix
	 *            Suffix of the typed prefix
	 * @param suggestion
	 *            Suggestion to manage
	 * @return
	 */
	static int getSimpleEditDistance(String suffix, String suggestion) {

		if (suffix.length() < suggestion.length()) {
			return Integer.MAX_VALUE;
		}

		if (suffix.equals(suggestion)) {
			return 0;
		}
		int suffixLength = suffix.length(), suggestionLength = suggestion.length();
		int i = 0;

		// System.out.println(suffix + "|" + suggestion);

		while (i < suggestionLength && suffix.charAt(i) == suggestion.charAt(i))
			i++;

		if (i == suggestionLength)
			return suffixLength - i;
		else
			return suffixLength - i + 1;
	}

	/**
	 * Implementation of the Levenshtein distance algorithm
	 * 
	 * @param suffix
	 *            Suffix of the typed prefix
	 * @param suggestion
	 *            Suggestion to manage
	 * @return
	 */
	private static Pair<Integer, Integer> getEditDistance(String suffix, String suggestion) {
		int X = suffix.length(), Y = suggestion.length();
		int[][] d = new int[X][Y];
		int x = 0, y = 0;
		int goBackTo = X;
		// List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> skipPoints =
		// EditDistanceHelper
		// .getSkipPoints(suffix, suggestion);

		while (x < X && y < Y && suffix.charAt(x) == (suggestion.charAt(y))) {
			d[x][0] = x;
			d[0][y] = y;
			x++;
			y++;
			goBackTo--;
		}

		for (; x < X; x++) {
			d[x][0] = x;
		}
		for (; y < Y; y++) {
			d[0][y] = y;
		}

		for (x = 1; x < X; x++) {
			for (y = 1; y < Y; y++) {
				if (suffix.charAt(x - 1) == (suggestion.charAt(y - 1))) {
					d[x][y] = d[x - 1][y - 1] + 1;
				} else {
					d[x][y] = Math.min(d[x - 1][y], d[x][y - 1]) + 1;
				}
			}
		}

		return new Pair<Integer, Integer>(d[x][Y - 1] + goBackTo, x);
	}

	/**
	 * Gets the origin-destination pair of points we can "skip" between by using a suggestion
	 * 
	 * @param suffix
	 * @param suggestion
	 * @return
	 */
	static List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> getSkipPoints(String suffix,
			String suggestion) {
		ArrayList<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> ret = new ArrayList<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>();
		String[] suffixWords = suffix.split(" "), suggestionWords = suggestion.split(" ");
		int X = suffixWords.length, Y = suggestionWords.length, x, y;
		int[] suffixSpaces = new int[X], suggestionSpaces = new int[Y];

		for (x = 0; x < X; x++) {
			if (x == 0) {
				suffixSpaces[x] = suffixWords[x].length();
			} else {
				suffixSpaces[x] = suffixSpaces[x - 1] + 1 + suffixWords[x].length();
			}
		}
		for (y = 0; y < Y; y++) {
			if (y == 0) {
				suggestionSpaces[y] = suggestionWords[y].length();
			} else {
				suggestionSpaces[y] = suggestionSpaces[y - 1] + 1 + suggestionWords[y].length();
			}
		}

		for (x = 1; x < X; x++) {
			for (y = 1; y < Y; y++) {
				if (suffixWords[x].equals(suggestionWords[y])) {
					ret.add(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(
							new Pair<Integer, Integer>(suffixSpaces[x], suggestionSpaces[y]),
							new Pair<Integer, Integer>(suffixSpaces[x - 1], suggestionSpaces[y - 1])));
				}
			}
		}

		return ret;
	}

	public static String fitToSuggestionSize(String sufixToType, String s) {
		int closestSpace = s.length();
		int searchPlus = 0, searchMinus = 0;

		if (s.length() > sufixToType.length()) {
			return sufixToType;
		}

		while (closestSpace + searchPlus < sufixToType.length()
				&& sufixToType.charAt(closestSpace + searchPlus) != ' ') {
			searchPlus++;
		}
		while (closestSpace - searchMinus >= 0 && closestSpace - searchMinus < sufixToType.length()
				&& sufixToType.charAt(closestSpace - searchMinus) != ' ') {
			searchMinus++;
		}

		// System.out.println(closestSpace);
		// System.out.println(searchPlus + ">" + searchMinus);
		String fittedSufixToType = "";
		if (closestSpace + searchPlus == sufixToType.length()
				|| (closestSpace + searchPlus < sufixToType.length() && sufixToType
						.charAt(closestSpace + searchPlus) == ' ')) {
			if (closestSpace - searchMinus > 0 && closestSpace - searchMinus < sufixToType.length()
					&& sufixToType.charAt(closestSpace - searchMinus) == ' ') {
				if (searchPlus < searchMinus) {
					fittedSufixToType = sufixToType.substring(0, closestSpace + searchPlus);
				} else {
					fittedSufixToType = sufixToType.substring(0, closestSpace - searchMinus);
				}
			} else {
				fittedSufixToType = sufixToType.substring(0, closestSpace + searchPlus);
			}
		} else if (closestSpace - searchMinus > 0
				&& closestSpace - searchMinus < sufixToType.length()
				&& sufixToType.charAt(closestSpace - searchMinus) == ' ') {
			fittedSufixToType = sufixToType.substring(0, closestSpace - searchMinus);
		} else {
			fittedSufixToType = "";
		}

		// System.out.println("--------");
		// System.out.println(s);
		// System.out.println(sufixToType);
		// System.out.println(fittedSufixToType);
		// System.out.println("========");
		return fittedSufixToType;
	}

	public static String fitToSuggestionSizeLeftSpace(String sufixToType, String s) {
		int closestSpace = s.length();
		int searchMinus = 0;

		// System.out.println("$" + sufixToType + ">>>" + s);

		if (s.length() >= sufixToType.length()) {
			return sufixToType;
		}

		while (closestSpace - searchMinus >= 0 && closestSpace - searchMinus < sufixToType.length()
				&& sufixToType.charAt(closestSpace - searchMinus) != ' ') {
			searchMinus++;
		}

		String fittedSufixToType = "";

		if (closestSpace - searchMinus > 0 && closestSpace - searchMinus < sufixToType.length()
				&& sufixToType.charAt(closestSpace - searchMinus) == ' ') {
			fittedSufixToType = sufixToType.substring(0, closestSpace - searchMinus);
		} else {
			fittedSufixToType = "";
		}

		return fittedSufixToType;
	}

}
