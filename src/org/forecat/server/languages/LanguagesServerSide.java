package org.forecat.server.languages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.collections.map.MultiValueMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.server.translation.cachetrans.Cachetrans;
import org.forecat.server.utils.PropertiesServer;
import org.forecat.shared.SessionShared;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.languages.LanguagesShared;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class LanguagesServerSide extends LanguagesShared {

	public List<LanguagesOutput> languagesService(List<LanguagesInput> inputList,
			SessionShared session) throws ForecatException {

		// No callbacks here: this code executes synchronously on the server

		session.setAttribute("engines", inputList);

		List<LanguagesOutput> outputList = new ArrayList<LanguagesOutput>();
		session.setAttribute("languages", outputList);

		// Call all the engines (order is not important in server-side); these
		// methods do nothing if the corresponding engine is not in the list.
		if (PropertiesServer.apertiumLocation == PropertiesServer.ApertiumLocations.NET_APERTIUM) {
			obtainLanguagesApertiumAPI(outputList, inputList);
		} else if (PropertiesServer.apertiumLocation == PropertiesServer.ApertiumLocations.LOCAL_APERTIUM) {
			obtainLanguagesApertiumLocalInstallation(outputList, inputList);
		}
		obtainLanguagesBingAPI(outputList, inputList);
		obtainLanguagesMesplaTrans(outputList, inputList);
		obtainLanguagesDictionarium(outputList, inputList);
		obtainLanguagesPhraseum(outputList, inputList);
		obtainLanguagesDud(outputList, inputList);

		// If any of the engines worked, throw the exception.
		if (outputList.isEmpty()) {
			throw new ForecatException("could not get any language from any of the engines");
		}

		LanguagesShared.sortLanguagesList(outputList);
		return outputList;
	}

	private void obtainLanguagesDictionarium(List<LanguagesOutput> outputList,
			List<LanguagesInput> inputList) {

		if ((LanguagesInput.searchEngine(inputList, Engine.DICTIONARIUM.toString())) == -1) {
			return;
		}

		addLanguagePair(outputList, "en", "es", Engine.DICTIONARIUM.toString());
		addLanguagePair(outputList, "es", "en", Engine.DICTIONARIUM.toString());
	}

	private void obtainLanguagesPhraseum(List<LanguagesOutput> outputList,
			List<LanguagesInput> inputList) {

		if ((LanguagesInput.searchEngine(inputList, Engine.PHRASEUM.toString())) == -1) {
			return;
		}

		addLanguagePair(outputList, "en", "es", Engine.PHRASEUM.toString());
		addLanguagePair(outputList, "es", "en", Engine.PHRASEUM.toString());
	}

	private void obtainLanguagesDud(List<LanguagesOutput> outputList, List<LanguagesInput> inputList) {

		if ((LanguagesInput.searchEngine(inputList, Engine.DUD.toString())) == -1) {
			return;
		}

		addLanguagePair(outputList, "en", "es", Engine.DUD.toString());
		addLanguagePair(outputList, "es", "en", Engine.DUD.toString());
	}

	private void obtainLanguagesMesplaTrans(List<LanguagesOutput> outputList,
			List<LanguagesInput> inputList) {

		if ((LanguagesInput.searchEngine(inputList, Engine.CACHETRANS.toString())) == -1) {
			return;
		}

		MultiValueMap langs = Cachetrans.getLanguages();

		for (Object s : langs.keySet()) {
			for (Object t : langs.getCollection(s)) {
				addLanguagePair(outputList, s.toString(), t.toString(),
						Engine.CACHETRANS.toString());
			}
		}

		return;
	}

	private void obtainLanguagesApertiumAPI(List<LanguagesOutput> outputList,
			List<LanguagesInput> inputList) {
		int pos;

		if ((pos = LanguagesInput.searchEngine(inputList, Engine.APERTIUM.toString())) != -1) {
			String key = inputList.get(pos).getKey();

			try {
				String url = "http://api.apertium.org/json/listPairs";
				WebResource wr = Client.create().resource(url);

				MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
				queryParams.add("key", key);
				wr = wr.queryParams(queryParams);
				String response = wr.get(String.class);

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readValue(response, JsonNode.class);

				Iterator<JsonNode> it = rootNode.get("responseData").getElements();
				while (it.hasNext()) {
					JsonNode pair = it.next();
					String sourceCode = pair.get("sourceLanguage").asText();
					String targetCode = pair.get("targetLanguage").asText();
					addLanguagePair(outputList, sourceCode, targetCode, Engine.APERTIUM.toString());
				}
			} catch (Exception e) {
				return;
			}
		}
	}

	private void addLanguagePair(List<LanguagesOutput> outputList, String sourceCode,
			String targetCode, String engine) {
		// Only add pairs whose both languages are in LanguageShared.languageNames
		if (LanguagesShared.languageNames.containsKey(sourceCode)
				&& LanguagesShared.languageNames.containsKey(targetCode)) {
			String sourceName = LanguagesShared.languageNames.get(sourceCode);
			String targetName = LanguagesShared.languageNames.get(targetCode);
			LanguagesOutput output = new LanguagesOutput(engine, sourceName, sourceCode,
					targetName, targetCode);
			outputList.add(output);
		}
	}

	private void obtainLanguagesBingAPI(List<LanguagesOutput> outputList,
			List<LanguagesInput> inputList) throws ForecatException {
		int pos;
		if ((pos = LanguagesInput.searchEngine(inputList, Engine.BING.toString())) != -1) {
			// Client ID and client secret are separated by the first comma:
			String[] s = inputList.get(pos).getKey().split(",", 2);

			if (s.length != 2) {
				throw new ForecatException(
						"LanguagesServer: wrong formatted id and secret for Bing");
			}
			Translate.setClientId(s[0]);
			Translate.setClientSecret(s[1]);

			// Translate.setClientId("bbcat");
			// TODO: do not publish keys in the repository
			// Translate.setClientSecret("lndluhebDf++b0eouRJrHw5NRww80RaxfCwdV0yjIts=");

			// Getting language names from Bing:
			// for(Language lang : Language.values()) System.out.println(lang.name() + " : " +
			// lang.toString() + " : " + lang.getName(Language.ENGLISH));

			// Note that generating all possible combinations raises the number of language pairs
			// over 1000 and consequently serializing all of them in development mode will take some
			// time:
			// http://stackoverflow.com/questions/4799501/why-gwt-devmode-serialization-is-hundred-times-slower
			for (Language lang : Language.values()) {
				for (Language lang2 : Language.values()) {
					String sourceCode = lang.toString();
					String targetCode = lang2.toString();
					// Bing translates between all the possible language
					// combinations
					if (!sourceCode.equals(targetCode)) {
						addLanguagePair(outputList, sourceCode, targetCode, Engine.BING.toString());
					}
				}
			}
		}
	}

	private void obtainLanguagesApertiumLocalInstallation(List<LanguagesOutput> outputList,
			List<LanguagesInput> inputList) {

		if ((LanguagesInput.searchEngine(inputList, Engine.APERTIUM.toString())) != -1) {

			// TODO: get the list of installed pairs in a cleaner way by using Apertium DBus from
			// Java or a script using it.

			// Call Apertium with a wrong mode and get the list of installed modes:
			ProcessBuilder pb = new ProcessBuilder("sh", "-c",
					"apertium xxxxx|sed '1d'|sed 's/^[ ]*//'|tr '\n' ','|sed 's/,$//'");
			Process proc = null;

			try {
				proc = pb.start();
			} catch (IOException e) {
				return;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String resultLine = null;
			try {
				resultLine = in.readLine();
			} catch (IOException e) {
				return;
			}

			String[] pairs = resultLine.split(",");

			for (int i = 0, n = pairs.length; i < n; ++i) {
				String[] langs = pairs[i].split("-");
				addLanguagePair(outputList, langs[0], langs[1], Engine.APERTIUM.toString());
			}

			proc.destroy();
		}
	}
}
