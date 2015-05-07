package org.miniforecat.translation.cachetrans;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.miniforecat.utils.UtilsConsole;
import org.miniforecat.translation.SourceSegment;

public class Cachetrans {

	static boolean useGoogle = false;
	static boolean useApertium = false;
	static ArrayList<Integer> segmentLengths = new ArrayList<Integer>();
	static HashMap<String, HashMap<String, String>> segments = new HashMap<String, HashMap<String, String>>();
	static String configFile = "cachetrans.txt";

	public static boolean isUseGoogle() {
		return useGoogle;
	}

	public static void setConfigFile(String c) {
		configFile = c;
	}

	public static void setUseGoogle(boolean useGoogle) {
		Cachetrans.useGoogle = useGoogle;
	}

	public static boolean isUseApertium() {
		return useApertium;
	}

	public static void setUseApertium(boolean useApertium) {
		Cachetrans.useApertium = useApertium;
	}

	public static ArrayList<Integer> getSegmentLengths() {
		return segmentLengths;
	}

	public static void addSegmentLenght(int length) {
		segmentLengths.add(length);
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
			ret.add(matchUpper(s.getSourceSegmentText(), tSegment.trim()));
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
		HashMap<String, String> auxSegments = new HashMap<String, String>();
		if (!segments.containsKey(lang)) {

			InputStream config = UtilsConsole.openFile(configFile);
			BufferedReader configReader = new BufferedReader(new InputStreamReader(
					new DataInputStream(config)));
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

			System.out.println("Segments loaded " + auxSegments.keySet().size());
			segments.put(lang, auxSegments);
		}
	}
}
