package org.forecat.server.translation.cachetrans;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.map.MultiValueMap;
import org.forecat.console.utils.UtilsConsole;
import org.forecat.shared.translation.SourceSegment;

public class Cachetrans {

	static ArrayList<Integer> segmentLengths = new ArrayList<Integer>();
	static MultiValueMap langs = null;
	static HashMap<String, HashMap<String, String>> segments = new HashMap<String, HashMap<String, String>>();
	static String configFile = "cachetrans.txt";

	public static void setConfigFile(String c) {
		configFile = c;
	}

	public static ArrayList<Integer> getSegmentLengths() {
		return segmentLengths;
	}

	public static void addSegmentLenght(int length) {
		segmentLengths.add(length);
	}

	public static MultiValueMap getLanguages() {
		if (langs == null) {
			langs = new MultiValueMap();

			langs.put("IN", "OUT");

			// if (useApertium) {
			// langs.put("en", "es");
			// langs.put("es", "en");
			// langs.put("es", "fr");
			// langs.put("fr", "es");
			// langs.put("esc", "ca");
			// langs.put("ca", "esc");
			// langs.put("eu", "es");
			// langs.put("br", "fr");
			// langs.put("cz", "en");
			// langs.put("en", "cz");
			// }
			//
			// if (useGoogle) {
			// langs.put("de", "en");
			// langs.put("en", "de");
			// langs.put("en", "es");
			// langs.put("es", "en");
			// langs.put("en", "fi");
			// langs.put("fi", "en");
			// langs.put("en", "fr");
			// langs.put("fr", "en");
			// langs.put("es", "fr");
			// langs.put("fr", "es");
			// }
		}

		return langs;
	}

	public static List<String> getTranslation(String slang, String tlang,
			List<SourceSegment> sSegments) {

		try {
			loadSegments(slang, tlang);
		} catch (IOException e) {
			System.err.println("Cant read files");
		}
		ArrayList<String> ret = new ArrayList<String>();
		HashMap<String, String> transSegments = segments.get(slang + "-" + tlang);
		String sSegment, tSegment;

		for (SourceSegment s : sSegments) {
			sSegment = s.getSourceSegmentText();// .toLowerCase();
			tSegment = transSegments.get(sSegment);
			tSegment = (tSegment != null) ? tSegment : "";
			// tSegment = tSegment.toLowerCase();
			ret.add(matchUpper(s.getSourceSegmentText(), tSegment.trim()));
			//
			// if (!tSegment.trim().equals(matchUpper(s.getSourceSegmentText(), tSegment)))
			// System.err.println(s.getSourceSegmentText() + "|" + tSegment + "|"
			// + matchUpper(s.getSourceSegmentText(), tSegment) + "|");

			// ret.add(matchUpper(s.getSourceSegmentText(), tSegment));
		}

		return ret;
	}

	public static String matchUpper(String source, String target) {
		StringBuilder sb = new StringBuilder();

		String[] sourceSplit = source.split(" ");
		String[] targetSplit = target.split(" ");

		int sourceLength = sourceSplit.length;

		String sourceWord;
		String targetWord;

		for (int i = 0; i < targetSplit.length; i++) {
			if (sb.length() > 0)
				sb.append(" ");

			if (i >= sourceLength) {
				sb.append(targetSplit[i]);
				continue;
			}
			sourceWord = sourceSplit[i];
			targetWord = targetSplit[i];

			if (Character.isUpperCase(sourceWord.charAt(0))) {
				if (sourceWord.length() > 1 && Character.isUpperCase(sourceWord.charAt(1))
						|| targetWord.length() == 0) {
					sb.append(targetWord.toUpperCase());
				} else {
					sb.append(Character.toUpperCase(targetWord.charAt(0)));
					sb.append(targetWord.substring(1));
				}
			} else {
				sb.append(targetWord);
			}
		}

		return sb.toString();
	}

	public static void loadSegments(String slang, String tlang) throws IOException {
		InputStream sSource = null, tSource = null;
		String sSeg, tSeg;
		BufferedReader sBr, tBr;
		String lang = slang + "-" + tlang;
		// String ordLang = slang.compareTo(tlang) < 0 ? slang + "-" + tlang : tlang + "-" + slang;
		HashMap<String, String> auxSegments = new HashMap<String, String>();
		if (!segments.containsKey(lang)) {

			InputStream config = UtilsConsole.openFile(configFile);
			BufferedReader configReader = new BufferedReader(
					new InputStreamReader(new DataInputStream(config)));
			String configLine;
			while ((configLine = configReader.readLine()) != null) {
				String[] files = configLine.split("\t");
				for (int i = 0; i < files.length; i += 2) {
					System.out.println(files[i] + " " + files[i + 1]);
					sSource = UtilsConsole.openFile(files[i]);
					tSource = UtilsConsole.openFile(files[i + 1]);
					sBr = new BufferedReader(new InputStreamReader(new DataInputStream(sSource)));
					tBr = new BufferedReader(new InputStreamReader(new DataInputStream(tSource)));
					while ((sSeg = sBr.readLine()) != null && (tSeg = tBr.readLine()) != null) {
						auxSegments.put(sSeg, tSeg);
					}
				}
			}

			// for (int i : segmentLengths) {
			// if (useGoogle) {
			// System.out.println("Loading Google " + i + "-segments...");
			// System.out.println("/corpus/original.subsegments/l" + i + "/" + slang + "/"
			// + slang + ".ssegs.uniq.l" + i);
			// System.out.println("/corpus/subsegment.translations/l" + i + "/" + ordLang
			// + "/google/" + slang + ".l" + i + ".clean");
			// sSource = UtilsConsole.openFile("/corpus/original.subsegments/l" + i + "/"
			// + slang + "/" + slang + ".ssegs.uniq.l" + i);
			// tSource = UtilsConsole.openFile("/corpus/subsegment.translations/l" + i + "/"
			// + ordLang + "/google/" + slang + ".l" + i + ".clean");
			// if (tSource == null) {
			// sSource.close();
			// System.out.println("ERROR");
			// break;
			// }
			//
			// sBr = new BufferedReader(new InputStreamReader(new DataInputStream(sSource)));
			// tBr = new BufferedReader(new InputStreamReader(new DataInputStream(tSource)));
			//
			// while ((sSeg = sBr.readLine()) != null && (tSeg = tBr.readLine()) != null) {
			// auxSegments.put(sSeg, tSeg);
			// }
			//
			// sBr.close();
			// tBr.close();
			//
			// }
			// if (useApertium) {
			// System.out.println("Loading Apertium " + i + "-segments...");
			// System.out.println("/corpus/original.subsegments/l" + i + "/" + slang + "/"
			// + slang + ".ssegs.uniq.l" + i);
			// System.out.println("/corpus/subsegment.translations/l" + i + "/" + ordLang
			// + "/apertium/" + slang + ".l" + i + ".clean");
			// sSource = UtilsConsole.openFile("/corpus/original.subsegments/l" + i + "/"
			// + slang + "/" + slang + ".ssegs.uniq.l" + i);
			// tSource = UtilsConsole.openFile("/corpus/subsegment.translations/l" + i + "/"
			// + ordLang + "/apertium/" + slang + ".l" + i + ".clean");
			// if (tSource == null || sSource == null) {
			// sSource.close();
			// System.out.println("ERROR");
			// System.out.println("/corpus/original.subsegments/l" + i + "/" + slang + "/"
			// + slang + ".segs.l" + i + ".low");
			// System.out.println("/corpus/subsegment.translations/l" + i + "/" + ordLang
			// + "/apertium/" + slang + ".ssegs.l" + i + ".html.clean.low");
			// break;
			// }
			// sBr = new BufferedReader(new InputStreamReader(new DataInputStream(sSource)));
			// tBr = new BufferedReader(new InputStreamReader(new DataInputStream(tSource)));
			//
			// while ((sSeg = sBr.readLine()) != null && (tSeg = tBr.readLine()) != null) {
			// auxSegments.put(sSeg, tSeg);
			// }
			// System.out.println("Loaded " + auxSegments.keySet().size() + " segments");
			//
			// sBr.close();
			// tBr.close();
			// }
			// }
			System.out.println("Segments loaded " + auxSegments.keySet().size());
			segments.put(lang, auxSegments);
		}
	}
}
