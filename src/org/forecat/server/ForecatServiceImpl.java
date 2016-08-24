package org.forecat.server;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.forecat.client.ForecatService;
import org.forecat.client.exceptions.ForecatException;
import org.forecat.server.languages.LanguagesServerSide;
import org.forecat.server.translation.TranslationMixedServerSide;
import org.forecat.server.translation.TranslationServerSide;
import org.forecat.shared.languages.LanguagesInput;
import org.forecat.shared.languages.LanguagesOutput;
import org.forecat.shared.ranker.RankerComposite;
import org.forecat.shared.ranker.RankerLongestShortestFromPosition;
import org.forecat.shared.ranker.RankerPosition;
import org.forecat.shared.ranker.RankerShared;
import org.forecat.shared.selection.SelectionInput;
import org.forecat.shared.selection.SelectionOutput;
import org.forecat.shared.selection.SelectionPositionShared;
import org.forecat.shared.selection.SelectionShared;
import org.forecat.shared.suggestions.SuggestionsBasic;
import org.forecat.shared.suggestions.SuggestionsInput;
import org.forecat.shared.suggestions.SuggestionsOutput;
import org.forecat.shared.suggestions.SuggestionsRanker;
import org.forecat.shared.suggestions.SuggestionsShared;
import org.forecat.shared.translation.TranslationInput;
import org.forecat.shared.translation.TranslationMixedOutput;
import org.forecat.shared.translation.TranslationOutput;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ForecatServiceImpl extends RemoteServiceServlet implements ForecatService {

	/**
	 * Returns the current session or a new session if there is no current one
	 * 
	 * @return The current Session or null
	 */
	protected SessionServerSide createOrGetSession() {
		SessionServerSide session = new SessionServerSide();
		// Get the current request, optionally create a new session for it, and then return its
		// session
		session.setSession(getThreadLocalRequest().getSession(true));
		return session;
	}

	/**
	 * Returns the current session or creates one if there is no current one
	 * 
	 * @return The current Session or creates one
	 */
	protected SessionServerSide getCurrentSession() {
		// Access the current session if it exist
		HttpSession httpSession = getThreadLocalRequest().getSession(false);

		if (httpSession != null) {
			SessionServerSide session = new SessionServerSide();
			session.setSession(httpSession);
			return session;
		} else {
			return null;
		}
	}

	@Override
	public List<LanguagesOutput> languagesRPCServer(List<LanguagesInput> inputList)
			throws ForecatException {
		LanguagesServerSide langs = new LanguagesServerSide();
		SessionServerSide session = createOrGetSession();

		List<LanguagesOutput> outputList = langs.languagesService(inputList, session);
		return outputList;
	}

	@Override
	public TranslationOutput translationRPCServer(TranslationInput input) throws ForecatException {
		TranslationServerSide tr = new TranslationServerSide();
		SessionServerSide session = getCurrentSession();
		if (session == null) {
			throw new ForecatException("session is null");
		}
		TranslationOutput output = tr.translationService(input, session);
		return output;
	}

	@Override
	public List<SuggestionsOutput> suggestionsRPCServer(SuggestionsInput input)
			throws ForecatException {
		SessionServerSide session = getCurrentSession();
		if (session == null) {
			throw new ForecatException("session is null");
		}
		SuggestionsShared su = (SuggestionsShared) session.getAttribute("Suggestions");

		// HACAT2013
		su = new SuggestionsRanker(new SuggestionsBasic(), new RankerComposite(
				new RankerPosition(), new RankerLongestShortestFromPosition()));
		RankerShared.setMaxSuggestions(4);
		// HACAT2013

		// Can't happen, getCurrentSession creates a new session if none is available
		// if (session == null) {
		// throw new ForecatException("session is null");
		// }
		List<SuggestionsOutput> output = su.suggestionsService(input, session);
		return output;
	}

	@Override
	public SelectionOutput selectionRPCServer(SelectionInput input) throws ForecatException {
		SessionServerSide session = getCurrentSession();
		if (session == null) {
			throw new ForecatException("session is null");
		}
		SelectionShared sel = (SelectionShared) session.getAttribute("Selection");

		// HACAT2013
		sel = new SelectionPositionShared();
		// HACAT2013

		// Can't happen, getCurrentSession creates a new session if none is available
		// if (session == null) {
		// throw new ForecatException("session is null");
		// }
		SelectionOutput output = sel.selectionService(input, session);
		return output;
	}

	@Override
	public TranslationMixedOutput translationMixedRPCServer(TranslationInput input)
			throws ForecatException {
		// TODO: throw exception under error condition
		TranslationMixedServerSide tr = new TranslationMixedServerSide();
		SessionServerSide session = getCurrentSession();
		if (session == null) {
			throw new ForecatException("session is null");
		}
		TranslationMixedOutput output = tr.translationMixedService(input, session);
		return output;
	}

}
