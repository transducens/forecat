package org.miniforecat.languages;

import java.util.ArrayList;
import java.util.List;

import org.miniforecat.exceptions.BboxcatException;
import org.miniforecat.SessionShared;
import org.miniforecat.languages.LanguagesInput;
import org.miniforecat.languages.LanguagesOutput;

public class LanguagesServerSide {

	public List<LanguagesOutput> languagesService(
			ArrayList<LanguagesInput> inputList,
			SessionShared session) throws BboxcatException{

		session.setAttribute("engines", inputList);

		List<LanguagesOutput> outputList = new ArrayList<LanguagesOutput>();
		session.setAttribute("languages", outputList);
		
		outputList.add(new LanguagesOutput("cachetrans", "es", "es", "en", "en"));

		return outputList;
	}

}
