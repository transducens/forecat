package org.forecat.server.translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.server.translation.cachetrans.Cachetrans;
import org.forecat.shared.SessionShared;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.languages.LanguagesShared.Engine;
import org.forecat.shared.translation.SourceSegment;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationOutput;
import org.forecat.shared.translation.TranslationShared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TranslationServerSide extends TranslationShared
		implements IsSerializable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6786263404467413690L;
	public static int phraseumAlpha = 3;
	public static boolean localApertiumShowUnknown = true;

	public static String phraseumBin;
	public static String phraseumData;
	public static String dictionariumBin;
	public static String dictionariumData;

	public TranslationOutput translationService(TranslationInput input, SessionShared session)
			throws ForecatException {

		int currentId = 1;
		Object aux = session.getAttribute("SuggestionId");
		if (aux != null) {
			currentId = (Integer) aux;
		}

		session.setAttribute("translation", input);

		@SuppressWarnings("unchecked")
		List<LanguagesInput> languagesInput = (List<LanguagesInput>) session
				.getAttribute("engines");
		if (languagesInput == null) {
			throw new ForecatException("engines could not be obtained from session");
		}

		@SuppressWarnings("unchecked")
		List<LanguagesOutput> languagesOutput = (List<LanguagesOutput>) session
				.getAttribute("languages");
		if (languagesOutput == null) {
			throw new ForecatException("languages could not be obtained from session");
		}

		final Map<String, List<SourceSegment>> segmentPairs = new HashMap<String, List<SourceSegment>>();
		final Map<String, Integer> segmentCounts = new HashMap<String, Integer>();

		session.setAttribute("segmentPairs", segmentPairs);
		session.setAttribute("segmentCounts", segmentCounts);

		String[] words = slice(input.getSourceText());
		List<SourceSegment> sourceSegments = sliceIntoSegments(words, input.getMaxSegmentLenth(),
				input.getMinSegmentLenth(), currentId);

		session.setAttribute("SuggestionId", sourceSegments.size() + currentId);

		translateApertiumAPY(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput);
		translateApertiumLocalInstallation(sourceSegments, input.getSourceCode(),
				input.getTargetCode(), segmentPairs, segmentCounts, languagesInput,
				languagesOutput);
		translateBingAPI(sourceSegments, input.getSourceCode(), input.getTargetCode(), segmentPairs,
				segmentCounts, languagesInput, languagesOutput);
		translateCachetrans(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput);
		translateDictionarium(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput);
		translatePhraseum(sourceSegments, input.getSourceCode(), input.getTargetCode(),
				segmentPairs, segmentCounts, languagesInput, languagesOutput);

		TranslationOutput output = new TranslationOutput(segmentPairs.size(), words.length);
		return output;
	}

	protected void translateApertiumAPI(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) {

		int pos;

		// Use this engine it if was in Languages input and the engine supports the requested
		// language pair:
		if (((pos = LanguagesInput.searchEngine(languagesInput, Engine.APERTIUM.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.APERTIUM.toString(), sourceCode, targetCode))) {
			String key = languagesInput.get(pos).getKey();

			try {
				String url = "http://api.apertium.org/json/translate";
				WebResource wr = Client.create().resource(url);

				Iterator<SourceSegment> it = sourceSegments.iterator();
				MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
				// TODO: make multiple requests to the server
				while (it.hasNext()) {
					queryParams.add("q", it.next().getSourceSegmentText());
				}
				queryParams.add("langpair", sourceCode + "|" + targetCode);
				queryParams.add("key", key);
				wr = wr.queryParams(queryParams);

				// GET request:
				// String response = wr.get(String.class);

				// POST request:
				String response = wr.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
						.post(String.class, queryParams);

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readValue(response, JsonNode.class);

				for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
					String targetText = rootNode.get("responseData").get(i).get("responseData")
							.get("translatedText").asText();
					if (rootNode.get("responseData").get(i).get("responseStatus").asText()
							.equals("200")) {
						addSegments(segmentPairs, segmentCounts, targetText,
								Engine.APERTIUM.toString(), sourceSegments.get(i));
					}
				}
			} catch (JsonMappingException e) {
				// TODO: decide whether to throw the Forecat exception or simply return and move to
				// the next translation service
				return;
			} catch (JsonParseException e) {
				return;
			} catch (IOException e) {
				return;
			}
		}
	}

	protected void translateApertiumAPY(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) {

		int pos;

		// Use this engine it if was in Languages input and the engine supports the requested
		// language pair:
		if (((pos = LanguagesInput.searchEngine(languagesInput, Engine.APERTIUM.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.APERTIUM.toString(), sourceCode, targetCode))) {
			// String key = languagesInput.get(pos).getKey();

			try {
				String url = "http://apy.projectjj.com/translate?";
				WebResource wr = Client.create().resource(url);

				Iterator<SourceSegment> it = sourceSegments.iterator();
				MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
				queryParams.add("langpair", sourceCode + "|" + targetCode);

				// queryParams.add("key", key);

				String toTranslate = "";
				int index = 0;
				while (it.hasNext()) {
					SourceSegment ss = it.next();
					toTranslate += "<apertium-notrans>" + index + "</apertium-notrans>"
							+ ss.getSourceSegmentText() + "<br>";
					index++;
				}
				queryParams.add("q", toTranslate);

				wr = wr.queryParams(queryParams);

				// GET request:
				// String response = wr.get(String.class);

				// POST request:
				String response = wr.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
						.post(String.class, queryParams);

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readValue(response, JsonNode.class);

				// System.out.println(wr.getURI());
				// System.out.println(response);

				if (rootNode.get("responseStatus").asText().equals("200")) {
					String[] segments = rootNode.get("responseData").get("translatedText").asText()
							.split("<br>");
					for (String s : segments) {
						if (s.equals("")) {
							continue;
						}
						String[] parts = s.split("</apertium-notrans>");
						String targetText = parts[1];
						int position = Integer.parseInt(parts[0].split("<apertium-notrans>")[1]);

						addSegments(segmentPairs, segmentCounts, targetText,
								Engine.APERTIUM.toString(), sourceSegments.get(position));

						// System.out.println(sourceSegments.get(position).getSourceSegmentText()
						// + " " + targetText);
					}
				}

			} catch (JsonMappingException e) {
				// TODO: decide whether to throw the Forecat exception or simply return and move to
				// the next translation service
				e.printStackTrace();
				return;
			} catch (JsonParseException e) {
				return;
			} catch (IOException e) {
				return;
			}
		}
	}

	public void translateBingAPI(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) throws ForecatException {

		int pos;

		// Use this engine it if was in Languages input and the engine supports the requested
		// language pair:
		if (((pos = LanguagesInput.searchEngine(languagesInput, Engine.BING.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.BING.toString(), sourceCode, targetCode))) {
			// Client ID and client secret are separated by the first comma:
			String[] s = languagesInput.get(pos).getKey().split(",", 2);

			if (s.length != 2) {
				throw new ForecatException(
						"TranslationServer: wrong formatted id and secret for Bing");
			}
			Translate.setClientId(s[0]);
			Translate.setClientSecret(s[1]);

			String[] sourceTexts = new String[sourceSegments.size()];
			for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
				sourceTexts[i] = sourceSegments.get(i).getSourceSegmentText();
			}

			// Call the translate.execute method, passing an array of source texts
			String[] translatedTexts = null;
			try {
				translatedTexts = Translate.execute(sourceTexts, Language.fromString(sourceCode),
						Language.fromString(targetCode));
			} catch (Exception e) {
				// TODO: decide whether to throw the Forecat exception or simply return and move to
				// the next translation service
				return;
			}

			for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
				String targetText = translatedTexts[i];
				addSegments(segmentPairs, segmentCounts, targetText, Engine.BING.toString(),
						sourceSegments.get(i));
			}
		}
	}

	/*
	 * translateApertiumLocalInstallation() uses a local installation of Apertium for translating;
	 * since it uses ProcessBuilder and this class is not supported by GAE, a compiler error is
	 * obtained.
	 * 
	 * 
	 * If you just want to avoid the compiler error, go to Window/Preferences/Google/Error,warnings
	 * and in the App Engine section set to "Warning" the option "Use of non-whitelisted JRE type".
	 * 
	 * If you want to run the code from Eclipse without getting an exception, you need to go to
	 * project properties and uncheck the option for using GAE; you will also need to make a small
	 * change in the build path (by going into Properties->”Java Build Path”->”Order and Export” and
	 * then used “Up” to move the “GWT SDK” up above the “App Engine SDK”; see
	 * http://blog.elitecoderz.net/
	 * org-mortbay-thread-timeout-exception-on-new-gwt-webapplication-with
	 * -instantiations-gwt-designer-or-windowbuilder-pro/2010/05/). Remember to undo these changes
	 * if you go back to GAE (put back GWT SDK just before JRE System Library).
	 */

	protected void translateApertiumLocalInstallation(List<SourceSegment> sourceSegments,
			String sourceCode, String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) throws ForecatException {

		// TODO: check system resources and only translate if system is under a particular
		// CPU/memory load

		if (((LanguagesInput.searchEngine(languagesInput, Engine.LOCALAPERTIUM.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.LOCALAPERTIUM.toString(), sourceCode, targetCode))) {

			String delimiter = "\n\n\n";

			ProcessBuilder pb = null;
			if (localApertiumShowUnknown) {
				pb = new ProcessBuilder("apertium", "-u", sourceCode + "-" + targetCode);
			} else {
				pb = new ProcessBuilder("apertium", "", sourceCode + "-" + targetCode);
			}
			Process proc = null;
			try {
				proc = pb.start();
			} catch (IOException e) {
				return;
			}
			PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			Iterator<SourceSegment> it = sourceSegments.iterator();

			StringBuilder q = new StringBuilder();
			while (it.hasNext()) {
				SourceSegment ss = it.next();
				q.append(ss.getSourceSegmentText());
				q.append(delimiter);
			}
			out.println(q.toString());
			out.flush();
			out.close();

			StringBuilder output = new StringBuilder();
			try {
				String resultLine = in.readLine();
				while (resultLine != null) {
					output.append(resultLine + "\n");
					resultLine = in.readLine();
				}
			} catch (IOException e) {
				// TODO: decide whether to throw the Forecat exception or simply return and move
				// to the next translation service
				return;
			}

			String[] targetSegments = output.toString().split(delimiter);

			for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
				String targetText = targetSegments[i];

				if (!localApertiumShowUnknown && (targetText.contains("@")
						|| targetText.contains("#") || targetText.contains("*"))) {

				} else {
					addSegments(segmentPairs, segmentCounts, targetText, Engine.APERTIUM.toString(),
							sourceSegments.get(i));
				}
			}

			proc.destroy();
		}
	}

	protected void translateCachetrans(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) throws ForecatException {

		// TODO: check system resources and only translate if system is under a particular
		// CPU/memory load

		if (((LanguagesInput.searchEngine(languagesInput, Engine.CACHETRANS.toString())) != -1)
		/*
		 * && (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
		 * Engine.CACHETRANS.toString(), sourceCode, targetCode))
		 */) {

			List<String> targetSegments = Cachetrans.getTranslation(sourceCode, targetCode,
					sourceSegments);

			for (int i = 0, n = sourceSegments.size(); i < n; ++i) {
				String targetText = targetSegments.get(i);
				addSegments(segmentPairs, segmentCounts, targetText, Engine.CACHETRANS.toString(),
						sourceSegments.get(i));
			}
		}
	}

	protected void translateDictionarium(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) throws ForecatException {

		if (((LanguagesInput.searchEngine(languagesInput, Engine.DICTIONARIUM.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.DICTIONARIUM.toString(), sourceCode, targetCode))) {

			/*
			 * InputStream file = null; InputStream dictionarium = Main.class
			 * .getResourceAsStream("/dictionarium/dictionarium.sh");
			 * 
			 * file = Main.class.getResourceAsStream( "/dictionarium/" + sourceCode + "-" +
			 * targetCode + ".dict");
			 * 
			 * FileOutputStream destination = null; try { destination = new
			 * FileOutputStream("/tmp/dictionarium.sh"); IOUtils.copy(dictionarium, destination);
			 * destination.close(); destination = new FileOutputStream("/tmp/dictionarium.data");
			 * IOUtils.copy(file, destination); destination.close(); } catch (Exception ex) {
			 * ex.printStackTrace(); return; }
			 */
			ProcessBuilder pb = null;

			pb = new ProcessBuilder("bash", dictionariumBin, dictionariumData, "1");

			Process proc = null;
			try {
				proc = pb.start();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			Iterator<SourceSegment> it = sourceSegments.iterator();
			String resultLine = "";
			SourceSegment ss;

			while (it.hasNext()) {

				ss = it.next();
				out.println(ss.getSourceSegmentText());
				// System.out.println(ss.getSourceSegmentText());
				out.flush();

				try {
					resultLine = in.readLine();
					while (resultLine != null && !resultLine.equals("")) {
						resultLine = resultLine.toLowerCase();
						// System.err.println("#D#" + ss.getSourceSegmentText() + " | " +
						// resultLine);
						addSegments(segmentPairs, segmentCounts, resultLine,
								Engine.DICTIONARIUM.toString(), ss);
						// System.err.println(resultLine);
						resultLine = in.readLine().toLowerCase();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			out.close();

			proc.destroy();
		}
	}

	private void translatePhraseum(List<SourceSegment> sourceSegments, String sourceCode,
			String targetCode, Map<String, List<SourceSegment>> segmentPairs,
			Map<String, Integer> segmentCounts, List<LanguagesInput> languagesInput,
			List<LanguagesOutput> languagesOutput) {

		if (((LanguagesInput.searchEngine(languagesInput, Engine.PHRASEUM.toString())) != -1)
				&& (LanguagesOutput.engineTranslatesLanguagePair(languagesOutput,
						Engine.PHRASEUM.toString(), sourceCode, targetCode))) {

			/*
			 * boolean direction = false; try { InputStream f = Main.class.getResourceAsStream(
			 * "/phraseum/" + sourceCode + "-" + targetCode + ".dict"); if (f != null) { direction =
			 * true; } else { f = Main.class.getResourceAsStream( "/phraseum/" + targetCode + "-" +
			 * sourceCode + ".dict"); if (f != null) { direction = false; } else { return; } } }
			 * catch (Exception ex) { System.err.println(ex.toString()); }
			 * 
			 * InputStream file = null; InputStream phraseum =
			 * Main.class.getResourceAsStream("/phraseum/phraseum.sh");
			 * 
			 * if (direction) { file = Main.class.getResourceAsStream( "/phraseum/" + sourceCode +
			 * "-" + targetCode + ".dict"); } else { file = Main.class.getResourceAsStream(
			 * "/phraseum/" + targetCode + "-" + sourceCode + ".dict"); }
			 * 
			 * FileOutputStream destination = null; try { destination = new
			 * FileOutputStream("/tmp/phraseum.sh"); IOUtils.copy(phraseum, destination);
			 * destination.close(); destination = new FileOutputStream("/tmp/phraseum.data");
			 * IOUtils.copy(file, destination); destination.close(); } catch (Exception ex) {
			 * ex.printStackTrace(); return; }
			 * 
			 */

			ProcessBuilder pb = null;

			// if (direction)
			// pb = new ProcessBuilder("bash", "/tmp/phraseum.sh", "/tmp/phraseum.data", "2",
			// "" + phraseumAlpha);
			// else
			pb = new ProcessBuilder("bash", phraseumBin, phraseumData, "1", "" + phraseumAlpha);

			Process proc = null;
			try {
				proc = pb.start();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			Iterator<SourceSegment> it = sourceSegments.iterator();
			String resultLine = "";
			SourceSegment ss;

			while (it.hasNext()) {

				ss = it.next();
				// if (direction) {
				// out.println("^"
				// + ss.getSourceSegmentText().replace(".", "\\.").replace("*", "\\*")
				// + " |||");
				// } else {
				// out.println(" ||| "
				// + ss.getSourceSegmentText().replace(".", "\\.").replace("*", "\\*")
				// + " |||");
				// }
				out.println(ss.getSourceSegmentText());
				out.flush();

				try {
					resultLine = in.readLine();

					while (resultLine != null && !resultLine.equals("")) {
						resultLine = resultLine.trim();
						// System.err.println("#P# " + ss.getSourceSegmentText() + "|" +
						// resultLine);
						addSegments(segmentPairs, segmentCounts, resultLine,
								Engine.PHRASEUM.toString(), ss);
						resultLine = in.readLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			out.close();

			proc.destroy();
		}

	}
}
