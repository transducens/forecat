package org.forecat.console;

import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.forecat.server.translation.TranslationServerSide;
import org.forecat.server.translation.cachetrans.Cachetrans;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesShared.Engine;
import org.forecat.shared.ranker.RankerComposite;
import org.forecat.shared.ranker.RankerLongestFirst;
import org.forecat.shared.ranker.RankerLongestShortestFirst;
import org.forecat.shared.ranker.RankerLongestShortestFromPosition;
import org.forecat.shared.ranker.RankerPosition;
import org.forecat.shared.ranker.RankerPressureBasic;
import org.forecat.shared.ranker.RankerPressureHeuristic;
import org.forecat.shared.ranker.RankerScore;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.ranker.RankerShortestFirst;
import org.forecat.shared.ranker.RankerShortestLongestFirst;
import org.forecat.shared.selection.SelectionContainsShared;
import org.forecat.shared.selection.SelectionEqualsShared;
import org.forecat.shared.selection.SelectionInvPrefixShared;
import org.forecat.shared.selection.SelectionNoneShared;
import org.forecat.shared.selection.SelectionPositionShared;
import org.forecat.shared.selection.SelectionPrefixShared;
import org.forecat.shared.selection.SelectionPrefixSuffixShared;
import org.forecat.shared.selection.SelectionSuffixShared;
import org.forecat.shared.suggestions.SuggestionsBasic;
import org.forecat.shared.suggestions.SuggestionsLMSimpleBernoulli;
import org.forecat.shared.suggestions.SuggestionsRanker;
import org.forecat.shared.suggestions.SuggestionsSolr;
import org.forecat.shared.suggestions.SuggestionsTorchShared;
import org.forecat.shared.suggestions.LM.IRSTLMscorer;

public class OptionsHelper {

	/**
	 * Manages the args
	 * 
	 * @param args
	 *            Args for main
	 * @param inputLanguagesList
	 *            Possible languages
	 * @param prop
	 *            Properties from the properties file
	 */
	static void manageOptions(String[] args, List<LanguagesInput> inputLanguagesList,
			Properties prop) {
		Options opt = new Options();
		opt.addOption("h", false, "Show this help");
		opt.addOption("o", true, "Output file");
		opt.addOption("sl", true, "Segment length");
		opt.addOption("msl", true, "Minimum segment length");
		opt.addOption("l", false, "Use basic selection");
		// opt.addOption("p", false, "Use position selection");
		opt.addOption("w", false, "Use window selection");
		opt.addOption("s", true, "Source language");
		opt.addOption("t", true, "Target language");
		opt.addOption("N", true, "Size of the window");
		opt.addOption("m", true, "Maximum number of suggestions");
		opt.addOption("is", true, "Source language input file");
		opt.addOption("it", true, "Target language input file");
		opt.addOption("A", false, "Use apertium");
		opt.addOption("B", false, "Use bing");
		opt.addOption("U", false, "Use empty translator");
		// opt.addOption("Google", false, "Use google");
		opt.addOption("M", true, "Use cachetrans");
		opt.addOption("D", false, "Use dictionarium");
		opt.addOption("P", true, "Use phraseum, with freq value.");
		opt.addOption("S", true, "Sorting method (s|l|sl|ls|lm|sm|sc|pr)");
		opt.addOption("d", true, "Deleting method (n|p|ip|ps|e)");
		opt.addOption("T", false, "Use only trusted segments");
		opt.addOption("ssp", false, "Penalty for selecting a suggestion");
		opt.addOption("c", false, "Use coverage");
		opt.addOption("z", false, "Give suggestions with nothing typed");
		opt.addOption("L", true, "Use SOLR suggestions");
		opt.addOption("noSuggestions", false, "Do not use any suggestions");
		opt.addOption("ed", false, "Use edit distance");
		opt.addOption("sed", false, "Use simple edit distance");
		opt.addOption("mi", true, "Maximum number of inserts in edit distance");
		opt.addOption("md", true, "Maximum number of deletes in edit distance");
		opt.addOption("mr", true, "Maximum number of replaces in edit distance");
		opt.addOption("med", true, "Maximum edit distance accepted for accepting a suggestion");
		opt.addOption("oec", false, "Output optimal edit cost");
		opt.addOption("lmbinary", true, "Location of the irstlm_scorer binary");
		opt.addOption("lmfile", true, "Location of the language model in plaintext");
		opt.addOption("lmvocab", true, "Location of the vocabulary for the language model");
		CommandLineParser clp = new GnuParser();
		CommandLine cl = null;
		try {
			cl = clp.parse(opt, args);

			if (cl.hasOption("h")) {
				throw (new Exception("Help requested"));
			}

			if (cl.hasOption("o")) {
				Main.outFile = cl.getOptionValue("o");
			}

			if (cl.hasOption("lmbinary")) {
				IRSTLMscorer.irstlm_scorer = cl.getOptionValue("lmbinary");
			}
			if (cl.hasOption("lmfile")) {
				IRSTLMscorer.lm_location = cl.getOptionValue("lmfile");
			}
			if (cl.hasOption("lmvocab")) {
				IRSTLMscorer.vocab_location = cl.getOptionValue("lmvocab");
			}

			if (cl.hasOption("l")) {
				Main.suggestions = new SuggestionsBasic();
			}

			if (cl.hasOption("w")) {
				Main.suggestions = new SuggestionsTorchShared();
			}

			if (cl.hasOption("L")) {
				SuggestionsSolr sugAux = new SuggestionsSolr();
				sugAux.setRange(cl.getOptionValue("L"));

				Main.suggestions = sugAux;
			}

			if (cl.hasOption("n")) {
				Main.margin = Integer.parseInt(cl.getOptionValue("n"));
				Main.suggestions.setFrame(Main.margin);
			}

			if (cl.hasOption("s")) {
				Main.sourceLang = cl.getOptionValue("s");
			}

			if (cl.hasOption("t")) {
				Main.targetLang = cl.getOptionValue("t");
			}

			if (cl.hasOption("is")) {
				Main.sourceFile = cl.getOptionValue("is");
			}

			if (cl.hasOption("it")) {
				Main.targetFile = cl.getOptionValue("it");
			}

			if (cl.hasOption("sl")) {
				Main.segmentLength = Integer.parseInt(cl.getOptionValue("sl"));
			}

			if (cl.hasOption("msl")) {
				Main.minSegmentLength = Integer.parseInt(cl.getOptionValue("msl"));
			}

			if (cl.hasOption("ssp")) {
				Main.suggestionSelectPenalty = Integer.parseInt(cl.getOptionValue("ssp"));
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

			if (cl.hasOption("U")) {
				// System.out.println("Using Bing");
				LanguagesInput languagesInput = new LanguagesInput(Engine.DUD.toString(), null);
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
				String aux = cl.getOptionValue("M");
				Cachetrans.setConfigFile(aux);
				Cachetrans.setUseApertium(true);
				inputLanguagesList.add(languagesInput);
			}

			if (cl.hasOption("S")) {
				String val = cl.getOptionValue("S");

				Main.ranker = OptionsHelper.getRanker(val);
				if (val.equals("lm")) {
					Main.suggestions = new SuggestionsLMSimpleBernoulli(Main.suggestions,
							Main.ranker);
				} else {
					Main.suggestions = new SuggestionsRanker(Main.suggestions, Main.ranker);
				}

			}

			if (cl.hasOption("m")) {
				Main.max_suggestions = Integer.parseInt(cl.getOptionValue("m"));
				RankerShared.setMaxSuggestions(Main.max_suggestions);
			}

			if (cl.hasOption("d")) {
				String val = cl.getOptionValue("d");

				if (val.equals("n")) {
					Main.selection = new SelectionNoneShared();
				}
				if (val.equals("p")) {
					Main.selection = new SelectionPrefixShared();
				}
				if (val.equals("ip")) {
					Main.selection = new SelectionInvPrefixShared();
				}
				if (val.equals("s")) {
					Main.selection = new SelectionSuffixShared();
				}
				if (val.equals("ps")) {
					Main.selection = new SelectionPrefixSuffixShared();
				}
				if (val.equals("e")) {
					Main.selection = new SelectionEqualsShared();
				}
				if (val.equals("pos")) {
					Main.selection = new SelectionPositionShared();
				}
				if (val.equals("c")) {
					Main.selection = new SelectionContainsShared();
				}

			}

			if (cl.hasOption("T")) {
				Main.onlyTrusted = true;
			}
			if (cl.hasOption("c")) {
				Main.useCoverage = true;
			}
			if (cl.hasOption("z")) {
				Main.suggestWithNoWord = true;
			}
			if (cl.hasOption("noSuggestions")) {
				Main.doNotUseSuggestions = true;
			}
			if (cl.hasOption("ed")) {
				Main.useEditDistance = true;
			}
			if (cl.hasOption("sed")) {
				Main.useEditDistance = true;
				Main.useSimpleEditDistance = true;
			}
			if (cl.hasOption("mi")) {
				Main.maxInserts = Integer.parseInt(cl.getOptionValue("mi"));
			}
			if (cl.hasOption("md")) {
				Main.maxDeletes = Integer.parseInt(cl.getOptionValue("md"));
			}
			if (cl.hasOption("mr")) {
				Main.maxReplaces = Integer.parseInt(cl.getOptionValue("mr"));
			}
			if (cl.hasOption("med")) {
				Main.maxEdits = Integer.parseInt(cl.getOptionValue("med"));
			}
			if (cl.hasOption("oec")) {
				Main.computeOptimalEditCost = true;
			}

		} catch (Exception ex) {
			HelpFormatter hf = new HelpFormatter();
			System.err.println(ex.toString());
			for (String s : args) {
				System.err.println(s);
			}
			hf.printHelp("Console mode of blackboxcat.", opt);
			System.exit(1);
		}
	}

	/**
	 * Gets the referred ranker
	 * 
	 * @param val
	 *            Value of the parameter
	 * @return
	 */
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
			rs = new RankerComposite(new RankerPosition(), new RankerLongestShortestFromPosition());
		}
		if (val.equals("pr")) {
			rs = new RankerPressureBasic();
			Main.useAlignments = true;
		}
		if (val.equals("ph")) {
			rs = new RankerPressureHeuristic();
			Main.useAlignments = true;
		}
		if (val.equals("lm")) {
			rs = new RankerScore();
		}
		return rs;
	}

}
