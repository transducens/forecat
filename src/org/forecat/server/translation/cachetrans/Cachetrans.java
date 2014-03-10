package org.forecat.server.translation.cachetrans;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.MultiValueMap;
import org.forecat.console.Main;
import org.forecat.shared.translation.SourceSegment;

public class Cachetrans {

	static boolean useGoogle = false;
	static boolean useApertium = false;
	static ArrayList<Integer> segmentLengths = new ArrayList<Integer>();
	static MultiValueMap langs = null;
	static HashMap<String, HashMap<String, String>> segments = new HashMap<String, HashMap<String, String>>();

	public static boolean isUseGoogle() {
		return useGoogle;
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

	public static MultiValueMap getLanguages() {
		if (langs == null) {
			langs = new MultiValueMap();

			if (useApertium) {
				langs.put("en", "es");
				langs.put("es", "en");
				langs.put("es", "fr");
				langs.put("fr", "es");
				langs.put("esc", "ca");
				langs.put("ca", "esc");
			}

			if (useGoogle) {
				langs.put("de", "en");
				langs.put("en", "de");
				langs.put("en", "es");
				langs.put("es", "en");
				langs.put("en", "fi");
				langs.put("fi", "en");
				langs.put("en", "fr");
				langs.put("fr", "en");
				langs.put("es", "fr");
				langs.put("fr", "es");
			}
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
			sSegment = s.getSourceSegmentText().toLowerCase();
			tSegment = transSegments.get(sSegment);
			tSegment = (tSegment != null) ? tSegment : "";
			tSegment = tSegment.toLowerCase();
			ret.add(tSegment);
		}

		return ret;
	}

	public static String matchUpper(String source, String target) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int slen = source.length();
		int tlen = target.length();
		int min = Math.min(slen, tlen);
		while (i < min) {
			if (Character.isUpperCase(source.charAt(i))) {
				sb.append(Character.toUpperCase(target.charAt(i)));
			} else {
				sb.append(Character.toLowerCase(target.charAt(i)));
			}
			i++;
		}
		if (min < tlen) {
			sb.append(target.subSequence(min, tlen));
		}

		return sb.toString();
	}

	public static void loadSegments(String slang, String tlang) throws IOException {
		InputStream sSource = null, tSource = null;
		String sSeg, tSeg;
		BufferedReader sBr, tBr;
		String lang = slang + "-" + tlang;
		String ordLang = slang.compareTo(tlang) < 0 ? slang + "-" + tlang : tlang + "-" + slang;
		HashMap<String, String> auxSegments = new HashMap<String, String>();
		if (!segments.containsKey(slang + "-" + tlang)) {
			for (int i : segmentLengths) {
				if (useGoogle) {
					System.out.println("Loading Google " + i + "-segments...");
					System.out.println("/corpus/original.subsegments/l" + i + "/" + slang + "/"
							+ slang + ".ssegs.uniq.l" + i);
					System.out.println("/corpus/subsegment.translations/l" + i + "/" + ordLang
							+ "/google/" + slang + ".l" + i + ".clean");
					sSource = Main.class.getResourceAsStream("/corpus/original.subsegments/l" + i
							+ "/" + slang + "/" + slang + ".ssegs.uniq.l" + i);
					tSource = Main.class.getResourceAsStream("/corpus/subsegment.translations/l"
							+ i + "/" + ordLang + "/google/" + slang + ".l" + i + ".clean");
					if (tSource == null) {
						sSource.close();
						System.out.println("ERROR");
						break;
					}

					sBr = new BufferedReader(new InputStreamReader(new DataInputStream(sSource)));
					tBr = new BufferedReader(new InputStreamReader(new DataInputStream(tSource)));

					while ((sSeg = sBr.readLine()) != null && (tSeg = tBr.readLine()) != null) {
						sSeg = sSeg.toLowerCase();
						tSeg = tSeg.toLowerCase();

						auxSegments.put(sSeg, tSeg);
					}

					sBr.close();
					tBr.close();

				}
				if (useApertium) {
					System.out.println("Loading Apertium " + i + "-segments...");
					System.out.println("/corpus/original.subsegments/l" + i + "/" + slang + "/"
							+ slang + ".ssegs.uniq.l" + i);
					System.out.println("/corpus/subsegment.translations/l" + i + "/" + ordLang
							+ "/apertium/" + slang + ".l" + i + ".clean");
					sSource = Main.class.getResourceAsStream("/corpus/original.subsegments/l" + i
							+ "/" + slang + "/" + slang + ".ssegs.uniq.l" + i);
					tSource = Main.class.getResourceAsStream("/corpus/subsegment.translations/l"
							+ i + "/" + ordLang + "/apertium/" + slang + ".l" + i + ".clean");
					if (tSource == null || sSource == null) {
						sSource.close();
						System.out.println("ERROR");
						System.out.println("/corpus/original.subsegments/l" + i + "/" + slang + "/"
								+ slang + ".segs.l" + i + ".low");
						System.out.println("/corpus/subsegment.translations/l" + i + "/" + ordLang
								+ "/apertium/" + slang + ".ssegs.l" + i + ".html.clean.low");
						break;
					}
					sBr = new BufferedReader(new InputStreamReader(new DataInputStream(sSource)));
					tBr = new BufferedReader(new InputStreamReader(new DataInputStream(tSource)));

					while ((sSeg = sBr.readLine()) != null && (tSeg = tBr.readLine()) != null) {
						sSeg = sSeg.toLowerCase();
						tSeg = tSeg.toLowerCase();
						auxSegments.put(sSeg, tSeg);
					}
					System.out.println("Loaded " + auxSegments.keySet().size() + " segments");

					sBr.close();
					tBr.close();
				}
			}
			System.out.println("Segments loaded " + auxSegments.keySet().size());
			segments.put(lang, auxSegments);
		}
	}
}
