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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.server.languages.LanguagesServerSide;
import org.forecat.server.translation.TranslationServerSide;
import org.forecat.server.translation.cachetrans.Cachetrans;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.server.utils.PropertiesServer.ApertiumLocations;
import org.forecat.shared.SessionBrowserSideConsole;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.languages.LanguagesShared.Engine;
import org.forecat.shared.ranker.RankerComposite;
import org.forecat.shared.ranker.RankerLongestFirst;
import org.forecat.shared.ranker.RankerLongestShortestFirst;
import org.forecat.shared.ranker.RankerLongestShortestOnly;
import org.forecat.shared.ranker.RankerPosition;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.ranker.RankerShortestFirst;
import org.forecat.shared.ranker.RankerShortestLongestFirst;
import org.forecat.shared.selection.SelectionContainsShared;
import org.forecat.shared.selection.SelectionEqualsShared;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionInvPrefixShared;
import org.forecat.shared.selection.SelectionNoneShared;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.selection.SelectionPositionShared;
import org.forecat.shared.selection.SelectionPrefixShared;
import org.forecat.shared.selection.SelectionPrefixSuffixShared;
import org.forecat.shared.selection.SelectionShared;
import org.forecat.shared.selection.SelectionSuffixShared;
import org.forecat.shared.suggestions.SuggestionsBasic;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.suggestions.SuggestionsRanker;
import org.forecat.shared.suggestions.SuggestionsShared;
import org.forecat.shared.suggestions.SuggestionsTorchShared;
import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationOutput;
import org.forecat.shared.utils.SubIdProvider;
import org.forecat.shared.utils.UtilsShared;

public class Main {

	private static int max_suggestions = Integer.MAX_VALUE;
	private static String targetLang = "es";
	private static String sourceLang = "en";
	private static String targetFile = "/corpus/intermediate/dgt-tm/10/es.tok";
	private static String sourceFile = "/corpus/intermediate/dgt-tm/10/en.tok";
	private static final String PROP_FILE = "forecat.console.properties";
	private static int segmentLength = 4;
	private static int minSegmentLength = 1;
	private static int suggestionSelectPenalty = 1;

	private static RankerShared ranker = null;
	private static SuggestionsShared suggestions = null;
	private static SelectionShared selection = null;

	private static int margin = 0;

	private static String outFile = "";

	private static boolean onlyTrusted = false;

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
		manageOptions(args, inputLanguagesList, prop);

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

		scannerSource = new Scanner(Main.class.getResourceAsStream(sourceFile));
		scannerTarget = new Scanner(Main.class.getResourceAsStream(targetFile));

		int numsentence = 0;
		while (scannerSource.hasNextLine()) {
			String source = scannerSource.nextLine();
			String target = scannerTarget.nextLine();

			System.out.println("---------------------------------------");
			System.out.println("Source: " + source + "\nTarget: " + target + "\n");

			numsentence++;
			evaluateOneSentence(session, source, target, numsentence);
		}

		scannerSource.close();
		scannerTarget.close();

		if (outFile.isEmpty()) {
			System.err.println(TestOutput.getPlot());
		} else {
			File f = new File(outFile);

			f.delete();
			try {
				System.out.println(f.getCanonicalPath());
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				fw.write("#$ ");
				for (int i = 0; i < args.length; i++)
					fw.write(args[i] + " ");
				fw.write("\n");
				fw.write(TrustedSegments.text());
				fw.write(TestOutput.getPlot());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(TrustedSegments.getSize());
	}

	private static void manageOptions(String[] args, List<LanguagesInput> inputLanguagesList,
			Properties prop) {
		Options opt = new Options();
		opt.addOption("h", false, "Show this help");
		opt.addOption("o", true, "Output file");
		opt.addOption("sl", true, "Segment length");
		opt.addOption("msl", true, "Minimum segment length");
		opt.addOption("l", false, "Use basic selection");
		opt.addOption("w", false, "Use window selection");
		opt.addOption("s", true, "Source language");
		opt.addOption("t", true, "Target language");
		opt.addOption("n", true, "Size of the window");
		opt.addOption("m", true, "Maximum number of suggestions");
		opt.addOption("is", true, "Source language input file");
		opt.addOption("it", true, "Target language input file");
		opt.addOption("A", false, "Use apertium");
		opt.addOption("B", false, "Use bing");
		opt.addOption("M", true, "Use cachetrans");
		opt.addOption("D", false, "Use dictionarium");
		opt.addOption("P", true, "Use phraseum, with freq value.");
		opt.addOption("S", true, "Sorting method (s|l|sl|ls|sm|sc)");
		opt.addOption("d", true, "Deleting method (n|p|ip|ps|e)");
		opt.addOption("T", false, "Use only trusted segments");
		opt.addOption("ssp", false, "Penalty for selecting a suggestion");
		CommandLineParser clp = new GnuParser();
		CommandLine cl = null;
		try {
			cl = clp.parse(opt, args);

			if (cl.hasOption("o")) {
				outFile = cl.getOptionValue("o");
			}

			if (cl.hasOption("l")) {
				suggestions = new SuggestionsBasic();
			}

			// if (cl.hasOption("p")) {
			// selectionOutputChooser = 2;
			// suggestionsServerSideChooser = 2;
			// }

			if (cl.hasOption("w")) {
				suggestions = new SuggestionsTorchShared();
			}

			if (cl.hasOption("n")) {
				margin = Integer.parseInt(cl.getOptionValue("n"));
				suggestions.setFrame(margin);
			}

			if (cl.hasOption("s")) {
				sourceLang = cl.getOptionValue("s");
			}

			if (cl.hasOption("t")) {
				targetLang = cl.getOptionValue("t");
			}

			if (cl.hasOption("is")) {
				sourceFile = cl.getOptionValue("is");
			}

			if (cl.hasOption("it")) {
				targetFile = cl.getOptionValue("it");
			}

			if (cl.hasOption("sl")) {
				segmentLength = Integer.parseInt(cl.getOptionValue("sl"));
			}

			if (cl.hasOption("msl")) {
				minSegmentLength = Integer.parseInt(cl.getOptionValue("msl"));
			}

			if (cl.hasOption("ssp")) {
				suggestionSelectPenalty = Integer.parseInt(cl.getOptionValue("ssp"));
			}

			if (cl.hasOption("A")) {
				// System.out.println("Using Apertium");
				LanguagesInput languagesInput = new LanguagesInput(Engine.APERTIUM.toString(),
						prop.getProperty("apertiumkey", ""));
				inputLanguagesList.add(languagesInput);
			}

			if (cl.hasOption("B")) {
				// System.out.println("Using Bing");
				LanguagesInput languagesInput = new LanguagesInput(Engine.BING.toString(),
						prop.getProperty("bingkeyid", "") + ","
								+ prop.getProperty("bingkeysecret", ""));
				inputLanguagesList.add(languagesInput);
			}

			if (cl.hasOption("D")) {
				LanguagesInput languagesInput = new LanguagesInput(Engine.DICTIONARIUM.toString(),
						"");
				inputLanguagesList.add(languagesInput);
			}

			if (cl.hasOption("P")) {
				LanguagesInput languagesInput = new LanguagesInput(Engine.PHRASEUM.toString(), "");
				TranslationServerSide.phraseumAlpha = Integer.parseInt(cl.getOptionValue("P"));
				inputLanguagesList.add(languagesInput);
			}

			if (cl.hasOption("Google")) {
				// System.out.println("Using google");
			}

			if (cl.hasOption("M")) {
				LanguagesInput languagesInput = new LanguagesInput(Engine.CACHETRANS.toString(), "");
				// System.out.println("Using cachetrans");
				String aux = cl.getOptionValue("M");
				char c;
				for (int i = 0; i < aux.length(); i++) {
					c = aux.charAt(i);

					switch (c) {
					case '1':
						Cachetrans.addSegmentLenght(1);
						break;
					case '2':
						Cachetrans.addSegmentLenght(2);
						break;
					case '3':
						Cachetrans.addSegmentLenght(3);
						break;
					case '4':
						Cachetrans.addSegmentLenght(4);
						break;
					case '5':
						Cachetrans.addSegmentLenght(5);
						break;
					case 'A':
						Cachetrans.setUseApertium(true);
						break;
					case 'G':
						Cachetrans.setUseGoogle(true);
						break;
					default:
						break;
					}
				}
				inputLanguagesList.add(languagesInput);
			}

			if (cl.hasOption("S")) {
				String val = cl.getOptionValue("S");

				ranker = getRanker(val);
				suggestions = new SuggestionsRanker(suggestions, ranker);

			}

			if (cl.hasOption("m")) {
				max_suggestions = Integer.parseInt(cl.getOptionValue("m"));
				RankerShared.setMaxSuggestions(max_suggestions);
			}

			if (cl.hasOption("d")) {
				String val = cl.getOptionValue("d");

				if (val.equals("n")) {
					selection = new SelectionNoneShared();
				}
				if (val.equals("p")) {
					selection = new SelectionPrefixShared();
				}
				if (val.equals("ip")) {
					selection = new SelectionInvPrefixShared();
				}
				if (val.equals("s")) {
					selection = new SelectionSuffixShared();
				}
				if (val.equals("ps")) {
					selection = new SelectionPrefixSuffixShared();
				}
				if (val.equals("e")) {
					selection = new SelectionEqualsShared();
				}
				if (val.equals("pos")) {
					selection = new SelectionPositionShared();
				}
				if (val.equals("c")) {
					selection = new SelectionContainsShared();
				}

			}

			if (cl.hasOption("T")) {
				onlyTrusted = true;
			}
		} catch (Exception ex) {
			HelpFormatter hf = new HelpFormatter();
			System.err.println(ex.toString());
			for (String s : args) {
				System.err.println(s);
			}
			hf.printHelp("Console mode of forecat.", opt);
			System.exit(1);
		}
	}

	public static RankerShared getRanker(String val) {
		RankerShared rs = null;
		if (val.equals("s")) {
			rs = new RankerShortestFirst();
		}
		if (val.equals("l")) {
			rs = new RankerLongestFirst();
		}
		if (val.equals("sl")) {
			rs = new RankerShortestLongestFirst();
		}
		if (val.equals("ls")) {
			rs = new RankerLongestShortestFirst();
		}
		if (val.equals("sm")) {
			// sortingMethod = 6;
		}
		if (val.equals("sc")) {
			// sortingMethod = 7;
		}
		if (val.equals("p")) {
			rs = new RankerPosition();
		}
		if (val.equals("cp")) {
			rs = new RankerComposite(new RankerPosition(), new RankerLongestShortestOnly());
		}
		return rs;
	}

	private static void getSegmentsCoverage(SessionBrowserSideConsole session, String target) {

		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");
		Integer bySegmentOk[] = new Integer[segmentLength];
		Integer bySegmentTotal[] = new Integer[segmentLength];
		int ok = 0;
		int total = 0;
		int index = 0;

		for (int i = 0; i < segmentLength; i++) {
			bySegmentOk[i] = 0;
			bySegmentTotal[i] = 0;
		}

		for (Entry<String, List<SourceSegment>> e : segmentPairs.entrySet()) {
			for (SourceSegment s : e.getValue()) {
				index = s.getSourceSegmentText().split(" ").length - 1;
				total++;
				bySegmentTotal[index]++;

				if (target.contains(s.getSourceSegmentText())) {
					ok++;
					bySegmentOk[index]++;
				}
			}
		}

		TestOutput.addCoverages(ok, total, bySegmentOk, bySegmentTotal);
	}

	private static void getPrecAndRecall(ArrayList<SuggestionsOutput> offers,
			SessionBrowserSideConsole session, String prefix, String target, Event event) {
		@SuppressWarnings("unchecked")
		Map<String, List<SourceSegment>> segmentPairs = (Map<String, List<SourceSegment>>) session
				.getAttribute("segmentPairs");

		int ok = 0;
		int nok = 0;
		int sugOk = 0;
		int sugNok = 0;
		boolean isOk = false;

		for (Entry<String, List<SourceSegment>> e : segmentPairs.entrySet()) {
			isOk = false;

			if (UtilsShared.isPrefix(prefix, e.getKey())) {
				// System.err.println(prefix + "|" + e.getKey() + "|" + target + "|"
				// + UtilsShared.isPrefix(prefix, e.getKey()) + " "
				// + UtilsShared.isPrefix(e.getKey(), target));
				if (UtilsShared.isPrefix(e.getKey(), target)
						&& (e.getKey().length() == target.length() || Character.isWhitespace(target
								.charAt(e.getKey().length())))) {
					for (SourceSegment sg : e.getValue()) {
						event.addPotentialSuggestion(sg.getId());
					}
					ok++;
					isOk = true;
				} else {
					nok++;
				}

				for (SuggestionsOutput so : offers) {
					if (so.getSuggestionText().equals(e.getKey())) {
						if (isOk) {
							sugOk++;
						} else {
							sugNok++;
						}
					}
				}
			}
		}

		double precision = (double) sugOk / (double) (sugOk + sugNok);
		double recall = (double) sugOk / (double) ok;

		if (ok == 0) {
			if ((sugOk + sugNok) == 0) {
				precision = 1;
				recall = 1;
			} else {
				precision = 0;
				recall = 0;
			}
		} else {
			if ((sugOk + sugNok) == 0) {
				precision = 0;
				recall = 0;
			}
		}
		if (event.getChar() != ' ') {
			TestOutput.addAllOk(ok);
			TestOutput.addAllNok(nok);
			TestOutput.addAllSugOk(sugOk);
			TestOutput.addAllSugNok(sugNok);
			TestOutput.addPrecision(precision);
			TestOutput.addRecall(recall);
		}

		event.setPrecision(precision);
		event.setRecall(recall);
	}

	private static void evaluateOneSentence(SessionBrowserSideConsole session, String source,
			String target, int numsentence) {

		int keypress = 0;
		int suggestions_offered = 0;
		int suggestions_used = 0;
		int sourceWords = source.split(" ").length;

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

		getSegmentsCoverage(session, target);

		if (onlyTrusted) {
			session.setAttribute("segmentPairs", TrustedSegments.getOk(segmentPairs));
		}

		int currentSegmentStart = 0; // Character-level start position of the current prefix
		int i = 0; // Current character
		int words = 0; // Current word
		int currentSegmentWordStart = 0; // Word-level start position of the current prefix

		while (i < target.length()) {
			keypress++;
			char c = target.charAt(i);
			Event event = new Event(c, numsentence, i, words);
			TestOutput.addEvent(event);

			if (Character.isWhitespace(c)) {
				currentSegmentStart = i + 1;
				++i;
				words++;
				// System.out.println("Space found at position " + Integer.toString(i) + " Word "
				// + words);
				continue;
			}

			String currentSegment = target.substring(currentSegmentStart, i + 1);
			// System.out.println("Current prefix: " + currentSegment);
			System.out.println(numsentence + ": " + currentSegment);

			// SuggestionsInput inputSuggestions = new SuggestionsInput(target.substring(0,
			// currentSegmentStart), words, currentSegment);

			SuggestionsInput inputSuggestions = new SuggestionsInput(target.substring(0,
					currentSegmentStart), currentSegment.toLowerCase(), words);

			ArrayList<SuggestionsOutput> outputSuggestionsList = null;
			try {
				outputSuggestionsList = (ArrayList<SuggestionsOutput>) suggestions
						.suggestionsService(inputSuggestions, session);
			} catch (ForecatException e) {
				// Only fatal errors are caught here.
				e.printStackTrace();
				System.out.println(e.getMessage());
				System.exit(1);
			}

			String typed = currentSegment;
			String newString;
			int atchar;
			for (SuggestionsOutput sg : outputSuggestionsList) {
				newString = "";
				for (atchar = 0; atchar < typed.length()
						&& atchar < sg.getSuggestionText().length(); atchar++) {
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

				sg.setSuggestionText(newString);
			}

			getPrecAndRecall(outputSuggestionsList, session, currentSegment,
					target.substring(currentSegmentStart), event);

			int position = -1;
			int index = 0;
			SuggestionsOutput match = null;
			StringBuilder sb2 = new StringBuilder();
			suggestions_offered += outputSuggestionsList.size();
			for (SuggestionsOutput s : outputSuggestionsList) {
				index = index + 1;
				event.addSuggestion(s.getId());

				if (match == null
						|| s.getSuggestionText().length() > match.getSuggestionText().length()) {
					int end = currentSegmentStart + s.getSuggestionText().length();
					if (end > target.length()) {
						continue;
					}
					String targetSubstring = target.substring(currentSegmentStart, end);
					// Things to avoid: suggestion "Mi vida es", reference text:
					// "Mi vida está", user types "M" and selects
					// "Mi vida es"

					// System.out.println(s.getSuggestionText() + "||" + targetSubstring);
					if (s.getSuggestionText().equals(targetSubstring)
							&& (end == target.length() || Character
									.isWhitespace(target.charAt(end)))) {
						match = s;
						position = index;
					}
				}
				sb2.append(s.getSuggestionText());
				sb2.append("; ");
			}
			if (sb2.toString().isEmpty()) {
				// System.out.println("No suggestions available");
			} else {
				// System.out.println("Suggestions: " + sb2.toString());
				if (match != null) {
					// System.out.println("Selected: " + match.getSuggestionText());

					TestOutput.addSuggestionPosition(position);

					event.useSuggestion(match.getId(), match.getSuggestionText());

					SelectionInput inputSelection = new SelectionInput(match.getSuggestionText(),
							match.getPosition());
					// /inputSelection.setCurrentPos(words);
					// System.out.println("POSITION " + words);
					SelectionOutput outputSelection = null;
					try {
						outputSelection = selection.selectionService(inputSelection, session);
					} catch (ForecatException e) {
						// Only fatal errors are caught here.
						e.printStackTrace();
						System.out.println(e.getMessage());
						System.exit(1);
					}
					// System.out.println("Number of available segments: "
					// + outputSelection.getNumberSegments());

					// Add penalty keypress for choosing a suggestion
					keypress += suggestionSelectPenalty;

					suggestions_used++;
					TestOutput.addMatch(words, match.getPosition());

					i += -currentSegment.length() + 1 + match.getSuggestionText().length();
					words += match.getSuggestionText().split(" ").length - 1;
					currentSegmentStart = i;
					continue;
				}
			}
			++i;
		}

		TrustedSegments.addTranslationMemory(target, segmentPairs);
		TestOutput.addOutput("#TMS# " + TrustedSegments.getSize() + "\n");
		TestOutput.addSentence(target.length(), keypress, suggestions_offered, suggestions_used);
	}
}
