package org.omegat.plugins.autocomplete.forecat.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.miniforecat.suggestions.SuggestionsInput;
import org.miniforecat.suggestions.SuggestionsOutput;
import org.omegat.gui.editor.autocompleter.AutoCompleterItem;
import org.omegat.gui.editor.autocompleter.AutoCompleterListView;
import org.omegat.plugins.autocomplete.forecat.ForecatPTS;
import org.omegat.plugins.autocomplete.forecat.adapter.ForecatMiniInterface;

public class ForecatAutoCompleteView extends AutoCompleterListView {

	public ForecatAutoCompleteView() {
		super("Forecat");
	}
	
	@Override
    public AutoCompleterItem getSelectedValue()
	{
		AutoCompleterItem toRet = super.getSelectedValue();
		if (toRet != null)
		{
			ForecatPTS.useSuggestion(toRet);
		}
		return toRet;
	}

	@Override
    public boolean shouldCloseOnSelection() {
        return false;
    }

	@Override
	public List<AutoCompleterItem> computeListData(String prevText) {

		int currentSegmentStart = prevText.length() - 1;
		int numWords = StringUtils.countMatches(prevText, " ");

		while (currentSegmentStart > 0
				&& prevText.charAt(currentSegmentStart) != ' ') {
			currentSegmentStart--;
		}

		if (currentSegmentStart != 0)
			currentSegmentStart++;

		String currentPrefix = prevText.substring(0, currentSegmentStart);
		String currentSegment = prevText.substring(currentSegmentStart);

		if ("".equals(currentSegment))
		{
			return new ArrayList<AutoCompleterItem>();
		}
		
		List<AutoCompleterItem> result = new ArrayList<AutoCompleterItem>();

		List<SuggestionsOutput> entries = ForecatMiniInterface
				.getForecatInterface().getSuggestions(
						new SuggestionsInput(currentPrefix, currentSegment,
								numWords));
		for (SuggestionsOutput sug : entries) {
			result.add(new AutoCompleterItem(sug.getSuggestionText() + " ",
					new String[] { sug.getId(),
							"" + sug.getSuggestionFeasibility(),
							"" + sug.getPosition() }, currentSegment.length()));
		}

		return result;
	}

	@Override
	public String itemToString(AutoCompleterItem item) {
		return item.payload;
	}
}
