package org.forecat.console;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import org.forecat.shared.ranker.RankerPressureNegativeEvidence;
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
import org.forecat.shared.suggestions.SuggestionsLM;
import org.forecat.shared.suggestions.SuggestionsLMBernoulli;
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
		opt.addOption("e", true,
				"Engines to use [A{pertium},B{ing},C{ache},D{ictionarium},P{hraseum},U{dud}] (more than one is possible)");
		opt.addOption("M", true, ".ini file for cache translator");
		opt.addOption("dictionariumbinary", true, "Location of the dictionarium binary");
		opt.addOption("dictionariumdata", true, "Location of the dictionarium data");
		opt.addOption("phraseumbinary", true, "Location of the phraseum binary");
		opt.addOption("phraseumdata", true, "Location of the phraseum data");
		opt.addOption("palpha", true, "Phraseum alpha");
		opt.addOption("S", true, "Sorting method (s|l|sl|ls|lm|sm|sc|pr|ph)");
		opt.addOption("d", true, "Deleting method (n|p|ip|ps|e)");
		opt.addOption("T", false, "Use only trusted segments");
		opt.addOption("ssp", false, "Penalty for selecting a suggestion");
		opt.addOption("c", false, "Use coverage");
		opt.addOption("z", false, "Give suggestions with nothing typed");
		opt.addOption("L", true, "Show SOLR suggestions");
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
		opt.addOption("lmTimeOut", true,
				"Timeout for the language model. Set it higher if you are using slow LM models. Probably indicates some buffering problem with the pipes.");
		opt.addOption("lines", true, "Lines to process");
		opt.addOption("allScores", false, "Output all scores");
		opt.addOption("featuresWinning", true,
				"Output features to a file in Weka arff style; winning features are classified as 1, rest 0");
		opt.addOption("featuresViable", true,
				"Output features to a file in Weka arff style; viable features are classified as 1, rest 0");
		opt.addOption("featuresTempFolder", true,
				"Temporary folder for storing the features file (2 passes are needed due some aggregate values)");
		opt.addOption("FANNstyle", false,
				"Output the features file in FANN style (else, Weka arff style is used)");
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
			if (cl.hasOption("lmTimeOut")) {
				IRSTLMscorer.setTimeOut(Integer.parseInt(cl.getOptionValue("lmTimeOut")));
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

			if (cl.hasOption("dictionariumbinary")) {
				TranslationServerSide.dictionariumBin = cl.getOptionValue("dictionariumbinary");
			}

			if (cl.hasOption("dictionariumdata")) {
				TranslationServerSide.dictionariumData = cl.getOptionValue("dictionariumdata");
			}

			if (cl.hasOption("phraseumbinary")) {
				TranslationServerSide.phraseumBin = cl.getOptionValue("phraseumbinary");
			}

			if (cl.hasOption("phraseumdata")) {
				TranslationServerSide.phraseumData = cl.getOptionValue("phraseumdata");
			}

			if (cl.hasOption("palpha")) {
				TranslationServerSide.phraseumAlpha = Integer.parseInt(cl.getOptionValue("palpha"));
			}

			if (cl.hasOption("S")) {
				String val = cl.getOptionValue("S");

				Main.ranker = OptionsHelper.getRanker(val);
				if (val.equals("lmd")) {
					Main.suggestions = new SuggestionsLMBernoulli(Main.suggestions, Main.ranker);
				} else if (val.equals("lms")) {
					Main.suggestions = new SuggestionsLMSimpleBernoulli(Main.suggestions,
							Main.ranker);
				} else if (val.equals("lm")) {
					Main.suggestions = new SuggestionsLM(Main.suggestions, Main.ranker);
				} else {
					Main.suggestions = new SuggestionsRanker(Main.suggestions, Main.ranker);
				}

			}

			if (cl.hasOption("m")) {
				Main.max_suggestions = Integer.parseInt(cl.getOptionValue("m"));
				RankerShared.setMaxSuggestions(Main.max_suggestions);
			}

			if (cl.hasOption("lines")) {
				String aux = cl.getOptionValue("lines").toString();
				Main.lines = new ArrayList<Integer>();
				for (String s : aux.split(",")) {
					Main.lines.add(Integer.parseInt(s));
				}
			}

			if (cl.hasOption("d")) {
				String val = cl.getOptionValue("d");
				getSelection(val);
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

			if (cl.hasOption("e")) {
				String val = cl.getOptionValue("e");
				getTranslationEngine(val, prop, inputLanguagesList);
			}
			if (cl.hasOption("M")) {
				String aux = cl.getOptionValue("M");
				Cachetrans.setConfigFile(aux);
			}

			if (cl.hasOption("allScores")) {
				Main.outputAllScores = true;
			}

			if (cl.hasOption("featuresWinning")) {
				File f = new File(cl.getOptionValue("featuresWinning"));

				f.delete();
				try {
					System.out.println(f.getCanonicalPath());
					f.createNewFile();
					Main.featuresFileWinning = new FileWriter(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (cl.hasOption("featuresViable")) {
				File f = new File(cl.getOptionValue("featuresViable"));

				f.delete();
				try {
					System.out.println(f.getCanonicalPath());
					f.createNewFile();
					Main.featuresFileViable = new FileWriter(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			File dir = null;
			if (cl.hasOption("featuresTempFolder")) {
				dir = new File(cl.getOptionValue("featuresTempFolder"));
			}

			Main.featuresTempF = File.createTempFile("forecat.", ".tmp", dir);
			Main.featuresTempFile = new FileWriter(Main.featuresTempF);

			if (cl.hasOption("FANNstyle")) {
				Main.useFannStyle = true;
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

	private static void getSelection(String val) {
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

	public static void getTranslationEngine(String val, Properties prop,
			List<LanguagesInput> inputLanguagesList) {

		for (String s : val.split(",")) {
			if (s.equals("A") || s.equals("Apertium")) {
				System.out.println("Using web Apertium");
				LanguagesInput languagesInput = new LanguagesInput(Engine.APERTIUM.toString(),
						prop.getProperty("apertiumkey", ""));
				inputLanguagesList.add(languagesInput);
			} else if (s.equals("L") || s.equals("LocalApertium")) {
				System.out.println("Using local Apertium");
				LanguagesInput languagesInput = new LanguagesInput(Engine.LOCALAPERTIUM.toString(),
						"No key needed");
				inputLanguagesList.add(languagesInput);
			} else if (s.equals("B") || s.equals("Bing")) {
				System.out.println("Using Bing");
				LanguagesInput languagesInput = new LanguagesInput(Engine.BING.toString(),
						prop.getProperty("bingkeyid", "") + ","
								+ prop.getProperty("bingkeysecret", ""));
				inputLanguagesList.add(languagesInput);
			} else if (s.equals("C") || s.equals("Cache")) {
				System.out.println("Using Cache");
				LanguagesInput languagesInput = new LanguagesInput(Engine.CACHETRANS.toString(),
						"No key needed");
				inputLanguagesList.add(languagesInput);
			} else if (s.equals("D") || s.equals("Dictionarium")) {
				System.out.println("Using Dictionarium");
				LanguagesInput languagesInput = new LanguagesInput(Engine.DICTIONARIUM.toString(),
						"No key needed");
				inputLanguagesList.add(languagesInput);
			} else if (s.equals("P") || s.equals("Phraseum")) {
				System.out.println("Using Phraseum");
				LanguagesInput languagesInput = new LanguagesInput(Engine.PHRASEUM.toString(),
						"No key needed");
				inputLanguagesList.add(languagesInput);
			} else if (s.equals("U") || s.equals("Udud")) {
				System.out.println("Using Dud");
				LanguagesInput languagesInput = new LanguagesInput(Engine.DUD.toString(),
						"No key needed");
				inputLanguagesList.add(languagesInput);
			}
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
		} else if (val.equals("l")) {
			rs = new RankerLongestFirst();
		} else if (val.equals("sl")) {
			rs = new RankerShortestLongestFirst();
		} else if (val.equals("ls")) {
			rs = new RankerLongestShortestFirst();
		} else if (val.equals("p")) {
			rs = new RankerPosition();
		} else if (val.equals("cp")) {
			rs = new RankerComposite(new RankerPosition(), new RankerLongestShortestFromPosition());
		} else if (val.equals("pr")) {
			rs = new RankerPressureBasic();
			Main.useAlignments = true;
		} else if (val.equals("ph")) {
			rs = new RankerPressureHeuristic();
			Main.useAlignments = true;
		} else if (val.equals("pn")) {
			rs = new RankerPressureNegativeEvidence();
			Main.useAlignments = true;
		} else if (val.equals("lm") || val.equals("lmd")) {
			rs = new RankerScore();
		}
		return rs;
	}

}
