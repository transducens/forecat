package org.forecat.webservices;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;

import org.forecat.client.exceptions.ForecatException;
import org.forecat.server.ForecatServiceImpl;
import org.forecat.server.SessionServerSide;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.ranker.RankerLongestFirst;
import org.forecat.shared.ranker.RankerLongestShortestFirst;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.ranker.RankerShortestFirst;
import org.forecat.shared.ranker.RankerShortestLongestFirst;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.selection.SelectionPositionShared;
import org.forecat.shared.selection.SelectionPrefixShared;
import org.forecat.shared.suggestions.SuggestionsBasic;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.suggestions.SuggestionsTorchShared;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationOutput;

import com.sun.jersey.api.json.JSONWithPadding;

@Path("/services")
public class WebServices extends ForecatServiceImpl {

	private static final long serialVersionUID = -8353374761703059680L;

	// / Current HTTP request header
	private HttpServletRequest currentSesssion;

	/**
	 * Returns the current session or a new session if there is no current one, based on the
	 * currentSession field.
	 * 
	 * @return The current Session or null
	 */
	@Override
	protected SessionServerSide createOrGetSession() {
		SessionServerSide session = new SessionServerSide();
		session.setSession(currentSesssion.getSession(true));
		return session;
	}

	/**
	 * Returns the current session or null if there is no current one
	 * 
	 * @return The current Session or null
	 */
	@Override
	protected SessionServerSide getCurrentSession() {
		SessionServerSide session = new SessionServerSide();
		session.setSession(currentSesssion.getSession(false));
		return session;
	}

	/**
	 * Web service that lists the languages of each of the engines
	 * 
	 * @param req
	 *            HTTP Request
	 * @param engines
	 *            List with the engines to use.
	 * @param keys
	 *            List with the keys of the engines. Should match the length of engines.
	 * @param sugMethod
	 *            Method for generating the suggestions
	 * @param width
	 *            Width to be used by the suggestion method, if applicable
	 * @param sortMethod
	 *            Method for sorting the suggestions
	 * @param maxSuggestions
	 *            Maximum number of suggestions to return
	 * @param selMethod
	 *            Method for removing used suggestions
	 * @return List of supported languages for each engine
	 */
	@GET
	@Produces("application/json")
	@Path("/languagesService")
	public List<LanguagesOutput> languages(@Context HttpServletRequest req,
			@QueryParam("engine") List<String> engines, @QueryParam("key") List<String> keys,
			@QueryParam("sugMethod") String sugMethod, @QueryParam("width") Integer width,
			@QueryParam("sortMethod") String sortMethod,
			@QueryParam("maxSuggestions") Integer maxSuggestions,
			@QueryParam("selMethod") String selMethod) {
		currentSesssion = req;

		List<LanguagesOutput> ret = null;
		try {
			List<LanguagesInput> langInput = new ArrayList<LanguagesInput>();
			for (int i = 0; i < engines.size() && i < keys.size(); i++) {
				langInput.add(new LanguagesInput(engines.get(i), keys.get(i)));
			}
			ret = languagesRPCServer(langInput);
		} catch (ForecatException e) {
			e.printStackTrace();
		}

		SessionServerSide sss = getCurrentSession();

		if (sugMethod != null && sugMethod.equals("lamp")) {
			SuggestionsTorchShared st = new SuggestionsTorchShared();
			sss.setAttribute("Suggestions", st);
			if (width != null) {
				st.setFrame(width);
			}
		} else {
			sss.setAttribute("Suggestions", new SuggestionsBasic());
		}

		if (sortMethod != null) {
			// Should implement RankerBrowserSideI
			RankerShared rb = null;
			if (sortMethod.equals("short")) {
				rb = new RankerShortestFirst();
			} else if (sortMethod.equals("long")) {
				rb = new RankerLongestFirst();
			} else if (sortMethod.equals("shortlong")) {
				rb = new RankerShortestLongestFirst();
			} else if (sortMethod.equals("longshort")) {
				rb = new RankerLongestShortestFirst();
			} else {
				rb = new RankerShortestFirst();
			}
			if (maxSuggestions != null) {
				RankerShared.setMaxSuggestions(maxSuggestions);
			}
			sss.setAttribute("Ranker", rb);
		} else {
			RankerShared rb = null;
			rb = new RankerLongestShortestFirst();
			RankerShared.setMaxSuggestions(10);
			sss.setAttribute("Ranker", rb);
		}

		if (selMethod != null) {
			if (selMethod.equals("position")) {
				sss.setAttribute("Selection", new SelectionPositionShared());
			} else {
				sss.setAttribute("Selection", new SelectionPrefixShared());
			}
		} else {
			sss.setAttribute("Selection", new SelectionPrefixShared());
		}

		return ret;
	}

	/**
	 * Web service that lists the languages of each of the engines, using JSONP
	 * 
	 * @param req
	 *            HTTP Request
	 * @param engines
	 *            List with the engines to use.
	 * @param keys
	 *            List with the keys of the engines. Should match the length of engines.
	 * @param sugMethod
	 *            Method for generating the suggestions
	 * @param width
	 *            Width to be used by the suggestion method, if applicable
	 * @param sortMethod
	 *            Method for sorting the suggestions
	 * @param maxSuggestions
	 *            Maximum number of suggestions to return
	 * @param selMethod
	 *            Method for removing used suggestions
	 * @param callback
	 *            Callback for JSONP
	 * @return List of supported languages for each engine
	 */
	@GET
	@Path("/languagesService/jsonp")
	@Produces({ "application/javascript" })
	public JSONWithPadding languagesJSONP(@Context HttpServletRequest req,
			@QueryParam("engine") List<String> engines, @QueryParam("key") List<String> keys,
			@QueryParam("sugMethod") String sugMethod, @QueryParam("width") Integer width,
			@QueryParam("sortMethod") String sortMethod,
			@QueryParam("maxSuggestions") Integer maxSuggestions,
			@QueryParam("selMethod") String selMethod, @QueryParam("callback") String callback) {
		List<LanguagesOutput> ret = languages(req, engines, keys, sugMethod, width, sortMethod,
				maxSuggestions, selMethod);
		return new JSONWithPadding(new GenericEntity<List<LanguagesOutput>>(ret) {
		}, callback);
	}

	/**
	 * Web service that translates a sentence. Suggestions are stored server side.
	 * 
	 * @param req
	 *            HTTP Request
	 * @param sourceText
	 *            Text to translate
	 * @param sourceCode
	 *            Code of the source language
	 * @param targetCode
	 *            Code of the target language
	 * @param maxLength
	 *            Maximum length of the subsegments
	 * @return Number of segments generated
	 */
	@GET
	@Produces("application/json")
	@Path("/translationService")
	public TranslationOutput translation(@Context HttpServletRequest req,
			@QueryParam("sourceText") String sourceText,
			@QueryParam("sourceCode") String sourceCode,
			@QueryParam("targetCode") String targetCode, @QueryParam("maxLength") String maxLength,
			@QueryParam("maxLength") String minLength) {
		currentSesssion = req;
		TranslationOutput ret = null;
		int minlength = 1;

		try {
			minlength = Integer.parseInt(minLength);
		} catch (Exception ex) {
			minlength = 1;
		}

		TranslationInput input = new TranslationInput(sourceText, sourceCode, targetCode,
				Integer.parseInt(maxLength), minlength);
		try {
			ret = translationRPCServer(input);
		} catch (ForecatException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Web service that translates a sentence, using JSONP. Suggestions are stored server side.
	 * 
	 * @param req
	 *            HTTP Request
	 * @param sourceText
	 *            Text to translate
	 * @param sourceCode
	 *            Code of the source language
	 * @param targetCode
	 *            Code of the target language
	 * @param maxLength
	 *            Maximum length of the subsegments
	 * @param callback
	 *            Callback for JSONP
	 * @return Number of segments generated
	 */
	@GET
	@Produces({ "application/javascript" })
	@Path("/translationService/jsonp")
	public JSONWithPadding translationJSONP(@Context HttpServletRequest req,
			@QueryParam("sourceText") String sourceText,
			@QueryParam("sourceCode") String sourceCode,
			@QueryParam("targetCode") String targetCode, @QueryParam("maxLength") String maxLength,
			@QueryParam("maxLength") String minLength, @QueryParam("callback") String callback) {
		TranslationOutput ret = translation(req, sourceText, sourceCode, targetCode, maxLength,
				minLength);
		return new JSONWithPadding(ret, callback);
	}

	/**
	 * Web service that offers the suggestion of a prefix.
	 * 
	 * @param req
	 *            HTTP Request
	 * @param targetText
	 *            Currently written translation
	 * @param prefixText
	 *            Current typed prefix
	 * @return List of suggestions
	 */
	@GET
	@Produces("application/json")
	@Path("/suggestionService")
	public List<SuggestionsOutput> suggestions(@Context HttpServletRequest req,
			@QueryParam("targetText") String targetText,
			@QueryParam("prefixText") String prefixText, @QueryParam("numwords") int numwords) {
		currentSesssion = req;
		List<SuggestionsOutput> ret = null;
		// PATCH
		SuggestionsInput input = new SuggestionsInput(targetText, prefixText, numwords);
		try {
			ret = suggestionsRPCServer(input);
		} catch (ForecatException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Web service that offers the suggestion of a prefix, using JSONP.
	 * 
	 * @param req
	 *            HTTP Request
	 * @param targetText
	 *            Currently written translation
	 * @param prefixText
	 *            Current typed prefix
	 * @param callback
	 *            Callback for JSONP
	 * @return List of suggestions
	 */
	@GET
	@Produces({ "application/javascript" })
	@Path("/suggestionService/jsonp")
	public JSONWithPadding suggestionsJSONP(@Context HttpServletRequest req,
			@QueryParam("targetText") String targetText,
			@QueryParam("prefixText") String prefixText, @QueryParam("numwords") int numwords,
			@QueryParam("callback") String callback) {
		List<SuggestionsOutput> ret = suggestions(req, targetText, prefixText, numwords);
		return new JSONWithPadding(new GenericEntity<List<SuggestionsOutput>>(ret) {
		}, callback);
	}

	/**
	 * Web service that deletes suggestions based on the selected one.
	 * 
	 * @param req
	 *            HTTP Request
	 * @param text
	 *            Text of the selected suggestion.
	 * @param position
	 *            Position of origin of the suggestion
	 * @return Number of suggestions left
	 */
	@GET
	@Produces("application/json")
	@Path("/selectionService")
	public SelectionOutput selection(@Context HttpServletRequest req,
			@QueryParam("text") String text, @QueryParam("position") int position) {
		currentSesssion = req;
		SelectionOutput ret = null;
		SelectionInput input = new SelectionInput(text, position);
		try {
			ret = selectionRPCServer(input);
		} catch (ForecatException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Web service that deletes suggestions based on the selected one, using JSONP
	 * 
	 * @param req
	 *            HTTP Request
	 * @param text
	 *            Text of the selected suggestion.
	 * @param position
	 *            Position of origin of the suggestion
	 * @param callback
	 *            Callback for JSONP
	 * @return Number of suggestions left
	 */
	@GET
	@Produces({ "application/javascript" })
	@Path("/selectionService/jsonp")
	public JSONWithPadding selectionJSONP(@Context HttpServletRequest req,
			@QueryParam("text") String text, @QueryParam("position") int position,
			@QueryParam("callback") String callback) {
		SelectionOutput ret = selection(req, text, position);
		return new JSONWithPadding(new GenericEntity<SelectionOutput>(ret) {
		}, callback);
	}

}
