package org.forecat.console;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.console.utils.UtilsConsole;
import org.forecat.server.languages.LanguagesServerSide;
import org.forecat.server.translation.TranslationServerSide;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.server.utils.PropertiesServer.ApertiumLocations;
import org.forecat.shared.SessionBrowserSideConsole;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionShared;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.suggestions.SuggestionsShared;
import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationOutput;
import org.forecat.shared.utils.SubIdProvider;

public class Main {

	static int max_suggestions = Integer.MAX_VALUE;
	static String targetLang = "es";
	static String sourceLang = "en";
	static String targetFile = "/corpus/intermediate/dgt-tm/10/es.tok";
	static String sourceFile = "/corpus/intermediate/dgt-tm/10/en.tok";
	private static final String PROP_FILE = "bboxcat.console.properties";
	static int segmentLength = 4;
	static int minSegmentLength = 1;
	public static int suggestionSelectPenalty = 1;
	static boolean useCoverage = false;
	static boolean useAlignments = false;
	public static boolean useManageCaps = true;
	static boolean outputAlignments = false;
	static boolean outputGrow = false;
	static double dampenFactor = 0;
	static double dampenFactorInv = 1;
	static boolean suggestWithNoWord = false;
	static boolean doNotUseSuggestions = false;
	static boolean useEditDistance = false;
	static int maxReplaces = 0;
	static int maxInserts = 0;
	static int maxDeletes = 0;
	static int maxEdits = Integer.MAX_VALUE;
	static boolean useSimpleEditDistance = false;
	static boolean computeOptimalEditCost = false;

	static RankerShared ranker = null;
	static SuggestionsShared suggestions = null;
	static SelectionShared selection = null;

	static int margin = 0;

	static String outFile = "";

	static boolean onlyTrusted = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SubIdProvider.isWorking = true;

		// Properties file containing the keys for the translation services:
		Properties prop = new Properties();
		try {
			prop.load(Main.class.getResourceAsStream(PROP_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Override value in PropertiesServer class:
		PropertiesServer.apertiumLocation = ApertiumLocations.LOCAL_APERTIUM;

		List<LanguagesInput> inputLanguagesList = new ArrayList<LanguagesInput>();
		OptionsHelper.manageOptions(args, inputLanguagesList, prop);

		SessionBrowserSideConsole session = new SessionBrowserSideConsole();

		LanguagesServerSide la = new LanguagesServerSide();
		List<LanguagesOutput> outputLanguagesList = null;
		try {
			outputLanguagesList = la.languagesService(inputLanguagesList, session);
		} catch (ForecatException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		StringBuilder sb = new StringBuilder();
		for (LanguagesOutput s : outputLanguagesList) {
			sb.append(s.getSourceName());
			sb.append(" to ");
			sb.append(s.getTargetName());
			sb.append("; ");
		}
		System.out.println("Languages: " + sb.toString());

		Scanner scannerSource = null, scannerTarget = null;

		System.out.println(sourceFile + " " + targetFile);

		scannerSource = new Scanner(UtilsConsole.openFile(sourceFile));
		scannerTarget = new Scanner(UtilsConsole.openFile(targetFile));

		File f;
		FileWriter fw = null;

		if (outFile.isEmpty()) {
			// System.err.println(TestOutput.getPlot());
		} else {
			f = new File(outFile);

			f.delete();
			try {
				System.out.println(f.getCanonicalPath());
				f.createNewFile();
				fw = new FileWriter(f);
				fw.write("#$ ");
				for (int i = 0; i < args.length; i++)
					fw.write(args[i] + " ");
				fw.write("\n");
				TestOutput.setOut(fw);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		int numsentence = 0;
		int costs = 0, lengths = 0;
		while (scannerSource.hasNextLine()) {
			String source = scannerSource.nextLine();
			String target = scannerTarget.nextLine();

			System.out.println("---------------------------------------");
			System.out.println("Source: " + source + "\nTarget: " + target + "\n");

			numsentence++;
			if (computeOptimalEditCost) {
				int optimalCost = getOptimalCost(session, source, target, numsentence);
				System.out.println("#" + numsentence + ":" + optimalCost);
				costs += optimalCost;
				lengths += target.length();
			} else {
				evaluateOneSentence(session, source, target, numsentence);
			}
		}

		if (computeOptimalEditCost) {
			System.out.println("#KSR " + ((double) (costs) / (double) (lengths)));
			System.exit(0);
		}

		scannerSource.close();
		scannerTarget.close();

		try {
			fw.write(TrustedSegments.text());
			TestOutput.getPlot();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println(TrustedSegments.getSize());
	}

	/**
	 * Get the greedy cost for a given source-target sentence with current suggestions
	 * 
	 * @param session
	 * @param source
	 * @param target
	 * @param numsentence
	 */
	private static void evaluateOneSentence(SessionBrowserSideConsole session, String source,
			String target, int numsentence) {

		int keypress = -1;
		int suggestions_offered = 0;
		int suggestions_used = 0;
		String[] sourceSplit = source.split(" ");
		int sourceWords = sourceSplit.length;
		String[] targetSplit = target.split(" ");
		int targetWords = targetSplit.length;
		int[][] coverage = new int[targetWords][targetWords];
		int[][] sources = new int[targetWords][targetWords];
		Integer[][][] previous = new Integer[targetWords][targetWords][2];
		int[] offered = new int[targetWords];
		String[][] bestSuggestionId = new String[targetWords][targetWords];
		String[] decisions = new String[targetWords];
		String sufixToType = "";
		int targetLengths[] = new int[targetWords];

		for (int i = 0; i < targetWords; i++) {
			targetLengths[i] = targetSplit[i].length();
			decisions[i] = null;
		}

		for (int j = 0; j < targetWords; j++) {
			offered[j] = 0;
			for (int k = 0; k < targetWords && j + k < targetWords; k++) {
				sources[j][k] = k;
				bestSuggestionId[j][k] = "";
				coverage[j][k] = 0;
			}
		}

		SubIdProvider.clear();

		TranslationInput inputTranslation = new TranslationInput(source, sourceLang, targetLang,
				segmentLength, minSegmentLength);
		TranslationServerSide tr = new TranslationServerSide();
		TranslationOutput outputTranslation = null;
		try {
			outputTranslation = tr.translationService(inputTranslation, session);
		} catch (ForecatException e) {
			// Only fatal errors are caught here.
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		System.out.println("Number of segments: " + outputTranslation.getNumberSegments());

		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");

		for (Entry<String, List<SourceSegment>> entry : segmentPairs.entrySet()) {
			for (SourceSegment s : entry.getValue()) {
				TestOutput
						.addOutput("#% " + s.getId() + "."
								+ SubIdProvider.getSubId(entry.getKey(), s) + "|" + numsentence
								+ "|" + s.getPosition() + "|" + s.getCharPosition() + "|"
								+ entry.getKey() + "\n");
			}
		}

		TestHelper.getSegmentsCoverage(session, target);

		if (onlyTrusted) {
			session.setAttribute("segmentPairs", TrustedSegments.getOk(segmentPairs));
		}

		// //////////////////////////////////////////////////

		int shortestUsed = 0;
		int shortestOffered = 0;

		if (useCoverage) {
			OptimalCoverage.fillOptimalCoverage(session, target, targetSplit, coverage, sources,
					previous, offered, bestSuggestionId, decisions, targetLengths, shortestUsed,
					shortestOffered);
		}
		int currentSegmentStart = 0; // Character-level start position of the current prefix
		int i = -1; // Current character
		int words = -1; // Current word
		char c;
		Event event;

		while (i < target.length()) {
			keypress++;
			if (i < 0)
				c = ' ';
			else
				c = target.charAt(i);
			event = new Event(c, numsentence, i, words);
			TestOutput.addEvent(event);

			if (Character.isWhitespace(c)) {
				words++;
				currentSegmentStart = i + 1;
				sufixToType = target.substring(i + 1);
				if (!suggestWithNoWord) {

					++i;
					continue;
				}
			}

			if (useAlignments) {
				AlignmentsHelper.computeAlignments(target, sourceWords, targetSplit, segmentPairs,
						i + 1);
			}

			if (!useCoverage && decisions[words] == null)
				decisions[words] = "";

			String currentSegment = target.substring(currentSegmentStart, i + 1);
			// System.out.println("Current prefix: " + currentSegment);
			System.out.println(numsentence + ":" + currentSegment + "| " + i + " | " + keypress);

			SuggestionsInput inputSuggestions = new SuggestionsInput(target.substring(0,
					currentSegmentStart), currentSegment, words);

			ArrayList<SuggestionsOutput> outputSuggestionsList = null;
			try {
				outputSuggestionsList = (ArrayList<SuggestionsOutput>) suggestions
						.suggestionsService(inputSuggestions, session);
			} catch (ForecatException e) {
				// Only fatal errors are caught here.
				e.printStackTrace();
				System.err.println(e.getMessage());
				System.exit(1);
			}

			String typed = currentSegment;

			if (useManageCaps) {
				manageCaps(outputSuggestionsList, typed);
			}

			TestHelper.getPrecAndRecall(outputSuggestionsList, session, currentSegment,
					target.substring(currentSegmentStart), event);

			int position = -1;
			int index = 0;
			int editDistanceCost = 0;
			String fittedTarget = "";
			SuggestionsOutput match = null;
			StringBuilder sb2 = new StringBuilder();
			suggestions_offered += outputSuggestionsList.size();
			for (SuggestionsOutput s : outputSuggestionsList) {
				index = index + 1;
				event.addSuggestion(s.getId(), s.getSuggestionFeasibility());

				// Pair<Integer, Integer> editDistance = getEditDistance(sufixToType,
				// s.getSuggestionText());
				// System.out.println("#E#" + s.getId() + ":" + editDistance.getFirst() + ":"
				// + editDistance.getSecond());
				String fittedSufixToType = "";
				int editDistance = Integer.MAX_VALUE;

				if (useEditDistance) {
					fittedSufixToType = EditDistanceHelper.fitToSuggestionSize(sufixToType,
							s.getSuggestionText());

					if (useSimpleEditDistance) {
						editDistance = EditDistanceHelper.getSimpleEditDistance(fittedSufixToType,
								s.getSuggestionText());
					} else {
						editDistance = EditDistanceHelper.getLimitedActionsEditDistance(
								fittedSufixToType, s.getSuggestionText(), maxInserts, maxDeletes,
								maxReplaces);
					}

					System.out.println("#EE# " + fittedSufixToType + "|" + s.getSuggestionText()
							+ "|" + editDistance);

					System.out.println("#E#" + s.getId() + ":" + editDistance);
				}

				if (useCoverage) {
					if (s.getId().equals(decisions[words])) {
						match = s;
					}
				} else {
					int end = currentSegmentStart + s.getSuggestionText().length();
					if (end > target.length()) {
						continue;
					}
					String targetSubstring = target.substring(currentSegmentStart, end);
					offered[words]++;

					if ((useEditDistance && editDistance < Integer.MAX_VALUE
							&& editDistance < fittedSufixToType.length() && editDistance <= maxEdits)
							|| (s.getSuggestionText().equals(targetSubstring) && (end == target
									.length() || Character.isWhitespace(target.charAt(end))))) {

						if (match == null
								|| s.getSuggestionText().length() > match.getSuggestionText()
										.length()) {
							if (s.getSuggestionText().split(" ").length > 1
									|| targetSplit[words].length() - typed.length() >= suggestionSelectPenalty) {
								match = s;
								position = index;
								editDistanceCost = editDistance;
								fittedTarget = fittedSufixToType;
							}
						}

					}
				}

				sb2.append(s.getSuggestionText());
				sb2.append("; ");
			}

			if (sb2.toString().isEmpty() || doNotUseSuggestions) {
				// System.out.println("No suggestions available");
			} else {
				// System.out.println("Suggestions: " + sb2.toString());
				if (match != null) {
					// System.out.println("Selected: " + match.getSuggestionText());

					decisions[words] = match.getId();

					TestOutput.addSuggestionPosition(position);

					event.useSuggestion(match.getId(), match.getSuggestionText());

					SelectionInput inputSelection = new SelectionInput(match.getSuggestionText(),
							match.getPosition());
					try {
						selection.selectionService(inputSelection, session);
					} catch (ForecatException e) {
						// Only fatal errors are caught here.
						e.printStackTrace();
						System.out.println(e.getMessage());
						System.exit(1);
					}
					// System.out.println("Number of available segments: "
					// + outputSelection.getNumberSegments());

					suggestions_used++;
					System.out.println(" MATCH " + words + " " + " " + match.getPosition() + " "
							+ match.getSuggestionText());
					TestOutput.addMatch(words, match.getPosition());

					if (useEditDistance) {
						System.out.println("#EEE# " + fittedTarget + " "
								+ match.getSuggestionText() + " " + editDistanceCost);
						keypress += editDistanceCost + 1;
						i += -currentSegment.length() + 1 + fittedTarget.length();
						words += fittedTarget.split(" ").length - 1;
					} else {
						// Add penalty keypress for choosing a suggestion
						keypress += suggestionSelectPenalty;
						i += -currentSegment.length() + 1 + match.getSuggestionText().length();
						words += match.getSuggestionText().split(" ").length - 1;
					}

					currentSegmentStart = i;
					continue;
				}
			}
			++i;
		}

		System.out.println();

		System.out.print("Actions :");
		for (int k = 0; k < targetWords; k++) {
			System.out.print(decisions[k] + "|");
		}
		System.out.println();

		System.out.println("KSR " + i + " " + target.length() + " " + keypress);

		TrustedSegments.addTranslationMemory(target, segmentPairs);
		TestOutput.addOutput("#TMS# " + TrustedSegments.getSize() + "\n");
		if (useCoverage) {
			TestOutput.addSentence(target.length(), coverage[0][targetWords - 1], shortestOffered,
					shortestUsed);
		} else {
			TestOutput
					.addSentence(target.length(), keypress, suggestions_offered, suggestions_used);
		}

		// smoothPressures(target, numsentence, sourceWords, targetWords);

	}

	public static void manageCaps(ArrayList<SuggestionsOutput> outputSuggestionsList, String typed) {
		String newString = "";
		int atchar = 0;
		for (SuggestionsOutput sg : outputSuggestionsList) {
			newString = "";
			for (atchar = 0; atchar < typed.length() && atchar < sg.getSuggestionText().length(); atchar++) {
				if (Character.isUpperCase(typed.charAt(atchar))) {
					newString += Character.toUpperCase(sg.getSuggestionText().charAt(atchar));
				} else {
					newString += sg.getSuggestionText().charAt(atchar);
				}
			}

			while (atchar < sg.getSuggestionText().length()) {
				newString += sg.getSuggestionText().charAt(atchar);
				atchar++;
			}

			if (!sg.getSuggestionText().equals(newString))
				System.out.println("###+" + sg.getSuggestionText() + ">>>" + newString + "|"
						+ typed);

			sg.setSuggestionText(newString);
		}
	}

	/**
	 * Get optimal cost for a given source-target sentence with current suggestions
	 * 
	 * @param session
	 * @param source
	 * @param target
	 * @param numsentence
	 * @return
	 */
	public static int getOptimalCost(SessionBrowserSideConsole session, String source,
			String target, int numsentence) {

		String[] targetSplit = target.split(" ");
		int targetWords = targetSplit.length;
		String sufixToType = target;
		int[] targetLengths = new int[targetWords];
		int currentSegmentStart = 0; // Character-level start position of the current prefix
		int i = 0; // Current character
		int words = 0; // Current word
		int costs[] = new int[target.length() + 1];
		String[] used = new String[target.length() + 1];
		int[] usedPosition = new int[target.length() + 1];
		int charsInCurrentSuffix = 0;
		int totalCost;
		Event event;
		Event events[] = new Event[target.length() + 1];

		for (i = 0; i < targetWords; i++) {
			targetLengths[i] = targetSplit[i].length();
		}

		for (i = 0; i <= target.length(); i++) {
			costs[i] = i;
			used[i] = "-1";
			usedPosition[i] = i - 1;
		}

		SubIdProvider.clear();

		TranslationInput inputTranslation = new TranslationInput(source, sourceLang, targetLang,
				segmentLength, minSegmentLength);
		TranslationServerSide tr = new TranslationServerSide();
		try {
			tr.translationService(inputTranslation, session);
		} catch (ForecatException e) {

		}

		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");
		for (Entry<String, List<SourceSegment>> entry : segmentPairs.entrySet()) {
			for (SourceSegment s : entry.getValue()) {
				System.out.println("#% " + s.getId() + "."
						+ SubIdProvider.getSubId(entry.getKey(), s) + "|" + numsentence + "|"
						+ s.getPosition() + "|" + s.getCharPosition() + "|" + entry.getKey());
			}
		}

		i = 0;
		while (i < target.length()) {
			costs[i + 1] = Math.min(costs[i] + 1, costs[i + 1]);
			if (Character.isWhitespace(target.charAt(i))) {
				words++;
				currentSegmentStart = i + 1;
				sufixToType = target.substring(i + 1);
				charsInCurrentSuffix = 0;

				events[i] = new Event(target.charAt(i), numsentence, i, words);
				i++;
				continue;
			}

			event = new Event(target.charAt(i), numsentence, i, words);

			String currentSegment = target.substring(currentSegmentStart, i + 1);

			SuggestionsInput inputSuggestions = new SuggestionsInput(target.substring(0,
					currentSegmentStart), currentSegment, words);

			ArrayList<SuggestionsOutput> outputSuggestionsList = null;
			try {
				outputSuggestionsList = (ArrayList<SuggestionsOutput>) suggestions
						.suggestionsService(inputSuggestions, session);
			} catch (ForecatException e) {
				e.printStackTrace();
			}

			if (useManageCaps) {
				manageCaps(outputSuggestionsList, currentSegment);
			}

			for (SuggestionsOutput s : outputSuggestionsList) {

				String fittedSufixToType = "";
				int editDistance = Integer.MAX_VALUE;

				fittedSufixToType = EditDistanceHelper.fitToSuggestionSizeLeftSpace(sufixToType,
						s.getSuggestionText());

				editDistance = EditDistanceHelper.getSimpleEditDistance(s.getSuggestionText(),
						fittedSufixToType);

				totalCost = costs[i - charsInCurrentSuffix] < 0 ? Integer.MAX_VALUE : editDistance
						+ suggestionSelectPenalty + costs[i - charsInCurrentSuffix];

				event.addSuggestion(s.getId(), editDistance == Integer.MAX_VALUE ? -1
						: editDistance);

				if (costs[i + fittedSufixToType.length() - charsInCurrentSuffix] > totalCost) {
					costs[i + fittedSufixToType.length() - charsInCurrentSuffix] = totalCost;
					used[i + fittedSufixToType.length() - charsInCurrentSuffix] = s.getId();
					usedPosition[i + fittedSufixToType.length() - charsInCurrentSuffix] = i;
				}

				/*
				 * int newLenght = -1;
				 * 
				 * editDistance = getSimpleTabAcceptDistance(sufixToType, s.getSuggestionText(),
				 * newLenght);
				 * 
				 * event.addSuggestion(s.getId(), editDistance == Integer.MAX_VALUE ? -1 :
				 * editDistance);
				 * 
				 * if (costs[i + newLenght - charsInCurrentSuffix] > editDistance) { costs[i +
				 * newLenght - charsInCurrentSuffix] = editDistance; used[i + newLenght -
				 * charsInCurrentSuffix] = s.getId(); usedPosition[i + fittedSufixToType.length() -
				 * charsInCurrentSuffix] = i; }
				 */

			}
			events[i] = event;
			charsInCurrentSuffix++;
			++i;
		}

		i = target.length();
		while (i > 0) {
			if (used[i].equals("-1")) {
				i--;
			} else {
				events[usedPosition[i]].useSuggestion(used[i], used[i]);
				i = usedPosition[i];
			}
		}

		for (i = 0; i < target.length(); i++) {
			System.out.println("#%% " + events[i].toString());
		}

		return costs[target.length()];
	}
}