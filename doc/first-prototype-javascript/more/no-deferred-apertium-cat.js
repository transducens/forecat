
// The Apertium's Cat Tool
// (c) 2012 Juan Antonio Perez-Ortiz, Mikel L. Forcada
// Transducens Research Group, Universitat d'Alacant


var maxLength;  // Length of longest suggestion
var maxSegments= 400;  // Maximum number of segments to translate via Apertium API
var sourceLang= "en";  // Source language
var targetLang= "es";  // Target language
var keystrokes= 0;  // Count number of keystrokes
var apertiumApiKey= "MYAPIKEY";

var segmentPairs= [];  // Segment pairs collected for the source text
var currentSegmentStart;  // Start position of the current srgment in target language
var firstStroke= true;  // False after the users presses the firt 'real' key
var suggestions = [];  // Suggestion list for the current segment

var langName= new Array();
// Comment lines for languages that you do not want to show to the user.
langName["ast"]= "Asturian";
langName["bg"]= "Bulgarian";
langName["br"]= "Breton";
langName["ca"]="Catalan";
langName["ca_valencia"]= "Valencian Catalan";
langName["cy"]= "Welsh";
langName["da"]= "Danish";
langName["en"]="English";
langName["en_US"]="US English";
langName["eo"]= "Esperanto";
langName["es"]="Spanish";
langName["eu"]= "Basque";
langName["fr"]="French";
langName["gl"]= "Galician";
langName["is"]= "Icelandic";
langName["it"]="Italian";
langName["mk"]= "Macedonian";
langName["nb"]= "Norwegian Bokmål";
langName["nn"]= "Norwegian Nynorsk";
langName["oc"]="Occitan";
langName["oc_aran"]= "Aranese";
langName["pt"]="Portuguese";
langName["pt_BR"]= "Brazilian Portuguese";
langName["ro"]= "Romanian";
langName["sv"]= "Swedish";

$(function() {

	// TO-DO: use JQuery UI icons instead of hide/show words
	$('.slidetoogleTitle').click(function () {
		$(this).parent().next().slideToggle('fast', function() {
			($(this).prev().children(':first-child').html() == "hide") ? 
				$(this).prev().children(':first-child').html("show") : 
				$(this).prev().children(':first-child').html("hide");
		});
	});

	$("#translate").button();  // Comment this line for a regular button, instead of a JQuery UI one

	readLanguagePairs();
	
	var s = ["Put here the text to","translate."].join(" ");
	document.getElementById("from").value= s;
	$("#from").focus().select();  // In the beginning set the focus in the source text textarea 

	setMaxLength(4);

	$("#to")
		// Don't navigate away from the field on tab when selecting an item:
		.bind("keydown", function( event ) {
			if (event.keyCode != $.ui.keyCode.TAB && !event.ctrlKey && !event.shiftKey) {
				keystrokes++;
			}
			console.log("Keystrokes: ",keystrokes);

			if (event.keyCode === $.ui.keyCode.TAB &&
					$(this).data( "autocomplete").menu.active) {
				event.preventDefault();
			}
		})
	
		// Using JKey (https://github.com/OscarGodson/jKey) for processing key combinations.
		.jkey('ctrl+1',function() {ctrlShortcut(this,0)})
		.jkey('ctrl+2',function() {ctrlShortcut(this,1)})
		.jkey('ctrl+3',function() {ctrlShortcut(this,2)})
		.jkey('ctrl+4',function() {ctrlShortcut(this,3)})
		.jkey('ctrl+5',function() {ctrlShortcut(this,4)})

		.bind( "keypress", function(event) {
			// Keydown and keyup are triggered for all the keys (including shift, ctrl, tab...); 
			// keypress only for "real" keys (alphanumeric, symbols...); the sequence is
			// keydown, then keypress (optional), then keyup. In Google Chrome the character is 
			// appended to the input area after the keypress and before the keyup event; 
			// the caret position (whichs starts at zero) is updated at the same time.

			// TO-DO: selectionStart does not work on IE<9 (caret position)
			// Check whether user is typing at the end or it is the first character in the very beginning:
			if (($(this)[0].selectionStart==$(this).val().length) || firstStroke) {
				
				firstStroke= false;  // This is to solve the problem of caret position pointing to
									 // the end of the text "Type your translation here" the first time
				
				// This is to prevent the autocomplete not showing after deleting back until
				// the first letter of a word (included) and typing again after a blank.
				if ($(this).val().length==0 || $(this).val()[$(this).val().length-1]==" ") {
					currentSegmentStart= $(this).val().length;
				}

				// Reset after pressing the space bar:
				if (event.keyCode === $.ui.keyCode.SPACE) {
					currentSegmentStart= $(this).val().length+1;
				}

				$(this).autocomplete("enable");
			} 
			else {
				$(this).autocomplete("disable");
				$(this).autocomplete("close");	
			}
		})

		// JQuery autocomplete:
		.autocomplete({
			minLength: 0,
			delay: 100,  // default 300; use a higher value for remote proposals
			autoFocus: true,  // first suggestion in the list always starts selected
			source: function(request,response) {

				// Avoid trying to process the shift, ctrl... keys when one of these is the first key 
				// pressed by the user (this is to prevent a message like 
				// "No suggestion for 'Type your translation here'"...
				if (firstStroke) {
					response([]); 
					return;
				}
				
				suggestions = [];
				$("#wait1").html("&nbsp;");
				var currentSegment= request.term.slice(currentSegmentStart);

				// Search for suggestions:
				if (!/^\s*$/.test(currentSegment)) { 
					// If current segment is not completely empty, search for matches.
					// Note that special characters the input string must be escaped
					// before using it in RegExp. Library XRegExp (http://xregexp.com/) 
					// is used for that
					for (var i=0; i<segmentPairs.length;i++) {
						if (segmentPairs[i].tl.search(new RegExp("^"+XRegExp.escape(currentSegment)))!=-1
							&& segmentPairs[i].count>0) {
								// Suggestions take the form {label, value}; 'label' is the text to
								// be shown in the suggestion list; 'value' is the text to insert in
								// text area. Initially both are equal, but the function addShortcuts
								// will add the 'Ctrl+0' text to the beginning.
							suggestions.push({"label":segmentPairs[i].tl,"value": segmentPairs[i].tl});
						}
					}
				}
				if (suggestions.length==0 && $.trim(currentSegment)!="") {
					// Show a message if no match found
					// TO-DO: remove this?
					$("#wait1").html("No suggestion for '"+currentSegment+"'");
				}

				// Sort suggestions:
				suggestions= sortSuggestions(suggestions);

				// After sorting, add the text with the shortcut combination:
				addShortcuts(suggestions);

				response(suggestions);
			},

			focus: function() {
				return false;  // prevent value inserted on focus
			},

			select: function(event,ui) {
				var prefix= this.value.slice(0,currentSegmentStart);
				this.value = prefix + ui.item.value;
				currentSegmentStart= this.value.length;
				useSegment(ui.item.value);
				suggestions= [];
				// debugger;
				return false;
			}
		})

		// See http://forum.jquery.com/topic/using-html-in-autocomplete.
		// Allow for HTML in the labels in the suggestion list:
		.data("autocomplete")._renderItem = function(ul, item) {
       			return $("<li></li>")
					.data("item.autocomplete", item)
					.append("<a>"+ item.label + "</a>")
					.appendTo(ul);
       	}
});


// Function for JKey events:
function ctrlShortcut (object,pos) {
	if( suggestions.length>0) {
		var prefix= object.value.slice(0,currentSegmentStart);
		object.value = prefix + suggestions[pos].value;
		currentSegmentStart= object.value.length;
		useSegment(suggestions[pos].value);
		$(object).autocomplete("disable");
		$(object).autocomplete("close");
		suggestions= [];
		//event.preventDefault();
	}
}

// Reset global variables:
function resetAutocomplete() {
	currentSegmentStart= 0;
	firstStroke= true;
	segmentPairs= [];
	suggestions= [];
	keystrokes= 0;
}

// Returns the same string with the first letter lowecase:
function uncapitaliseFirstLetter(string) {
	return string.charAt(0).toLowerCase() + string.slice(1);
}

// Search segment pairs by source and target text:
function searchSegmentPairBySourceAndTarget (stext,ttext) {
	var pos= -1;
	for (var i=0;i<segmentPairs.length;i++) {
		if (segmentPairs[i].sl==stext && segmentPairs[i].tl==ttext) {
			pos= i;
			break;
		}
	}
	return pos;
}

// Search segment pairs by target text:
function searchSegmentPairByTarget(ttext) {
	var pos= -1;
	for (var i=0;i<segmentPairs.length;i++) {
		if (segmentPairs[i].tl==ttext) {
			pos= i;
			break;
		}
	}
	return pos;
}

// Output a segment through the console
function logSegment(pair) {
	console.log(pair.sl+" »» "+pair.tl+"   (start: "+pair.start+", count: "+pair.count+
		", total: "+segmentPairs.length+")");
}

// Add a new segment pair
function addSegment (stext,ttext,startPos) {
	var j;
	if ((j=searchSegmentPairBySourceAndTarget(stext,ttext))!=-1) {
		segmentPairs[j].count++;
		segmentPairs[j].start.push(startPos);
		logSegment(segmentPairs[j]);
	}
	else {
		var pair= {sl:stext,tl:ttext,count:1,start:[startPos]};
		segmentPairs.push(pair);
		logSegment(pair);
		updateCount();
	}
}

// After selecting a segment, remove current segment pair and some others:
function useSegment (ttext) {
	// TO-DO: decide which pair to remove when more than one pair shares the same ttext
	var j= searchSegmentPairByTarget(ttext);
	segmentPairs[j].count--;
	// Segment pairs are not removed actually; just set their counter to zero.
	filterOutSegments(ttext)
	updateCount();
}

// Sort suggestion list. Currently, shortest first, longest second.
function sortSuggestions (suggestions) {
	var newSuggestions= [];
	var min= 1000000;
	var max= 0;
	var minpos= 0;
	var maxpos= 0;
	for (var i=0;i<suggestions.length;i++) {
		if (suggestions[i].value.length<min) {
			min= suggestions[i].value.length;
			minpos= i;
		}
		else if (suggestions[i].value.length>max) {
			max= suggestions[i].value.length;
			maxpos= i;
		}
	}
	// Put shortest first, longest second, then the rest...
	if (suggestions.length>0) {
		newSuggestions.push(suggestions[minpos]);
		if (minpos!=maxpos) {
			newSuggestions.push(suggestions[maxpos]);
		}
	}
	for (var i=0;i<suggestions.length;i++) {
		if (i!=minpos && i!=maxpos) {
			newSuggestions.push(suggestions[i]);
		}
	}
	return newSuggestions;
}

// Add text with shortcut keys to the beginning of the label of each suggestion:
function addShortcuts (suggestions) {
	for (var i=0;i<suggestions.length;i++) {
		if (i<5) {
			suggestions[i].label= "<span class='ctrl'>Ctrl+"+(i+1)+"</span>" + suggestions[i].label;
		}
		else {
			suggestions[i].label= "<span class='noctrl'>Ctrl+0</span>" + suggestions[i].label;	
		}
	}
}

// Prune/remove (decrement counter) additional segment pairs:
function filterOutSegments (ttext) {
	var start= $("#to").val().length-ttext.length;
	// TO-DO: if two equivalent segments are found remove (decrement) the one with start 
	// closer to this 'start'
	// TO-DO: dramatically improve the following heuristics
	for (var i=0;i<segmentPairs.length;i++) {
		// Hide segments which are prefix of ttext:
		// Note the extra white space to avoid 'la' being deleted because of 'lampara'
		if (segmentPairs[i].count>0 &&
			ttext.search(new RegExp("^"+XRegExp.escape(segmentPairs[i].tl)+" "))!=-1) {
			// TO-DO: remove "closest" segment and not the first one
			segmentPairs[i].count--;
			console.log("Segment hidden because of being a prefix of '"+ttext+"':");
			logSegment(segmentPairs[i]);
		}
		// Hide segments which have ttext as a prefix:
		if (segmentPairs[i].count>0 &&
			(segmentPairs[i].tl+" ").search(new RegExp("^"+XRegExp.escape(ttext)))!=-1) {
			// TO-DO: remove "closest" segment and not the first one
			segmentPairs[i].count--;
			console.log("Segment hidden because of having '"+ttext+"' as a prefix:");
			logSegment(segmentPairs[i]);
		}
	}
}

// Translate an array of source segments by calling the batch Apertium API (sourceSegments
// will always be an array --possibly of size one):
function translate(sourceSegments,startPos) {
	$.ajax({
		type: "GET",
		url: "http://api.apertium.org/json/translate",
		data: {q:sourceSegments, langpair:sourceLang+"|"+targetLang,key: apertiumApiKey},
		dataType: "jsonp",
		traditional: true,  // this is necessary so that the query is not of the form
							// q[]=string1&q[]=string2...
		success: function(data) {
			var pair;
			// Note that when translating an array with a single segment, responseData in the response
			// is not an array since the query is not treated as a batch translation.
			if ($.isArray(data.responseData)) {
				for (var i=0; i < data.responseData.length;i++) {
					processNewSegment(sourceSegments[i],data.responseData[i].responseData.translatedText,startPos);
				}
			} else {
				processNewSegment(sourceSegments[0],data.responseData.translatedText,startPos);
			}
		},
		error: function(data) {
			console.error("Error ",data.responseStatus+": "+sourceSegments.toString());
    	}
	});
}

// Preprocess segments before adding them to the segment list:
function processNewSegment (stext,ttext,startPos) {
	// Trim possible newlines at the end added by Apertium API and convert to 
	// lowercase the first character if it wasn't uppercase in the original 
	// sentence (undesirable side-effect done by the API)
	var translation= $.trim(ttext);

	// Add both: first character lowercase and uppercase; with this,
	// the number of available suggestions is twice the number of
	// segments translated.
	addSegment(stext,translation,startPos);
	addSegment(stext,uncapitaliseFirstLetter(translation),startPos);  
}

// Update the counter of available translation proposals on the screen:
function updateCount () {
	var j=0;
	// TO-DO: have a global counter for the active number of translation units/segment 
	// pairs available to avoid executing the loop every time
	for (var i=0;i<segmentPairs.length;i++) {
		if (segmentPairs[i].count>0) {
			j++;
		}
	}
	$("#count").html(j);
}

function setMaxLength (value) {
	maxLength= parseInt($("#maxlength").val());
}

function setLanguagePair (value) {
	sourceLang= value.split("-")[0]; 
	targetLang= value.split("-")[1];
}

// Make a request to the Apertium API to get the array of available pairs:
function readLanguagePairs () {
	$.ajax({
		type: "GET",
		url: "http://api.apertium.org/json/listPairs",
		data: {key: apertiumApiKey},
		dataType: "jsonp",
		success: function(data) {
			processLanguagePairs(data.responseData);
		}
	});
}

// Populate the available language pairs in the select combo:
function processLanguagePairs (langs) {
	var langPairs= [];
	// Fill langPairs with both codes and names:
	$.each(langs,function () {
		var sourceName, targetName;
		// Use the code if no language name available in langName:
		if (langName[this.sourceLanguage]) {
			sourceName= langName[this.sourceLanguage];
		}
		else {
			// sourceName= this.sourceLanguage;
			return; // If name for the language code is not defined, ignore this pair
		}
		if (langName[this.targetLanguage]) {
			targetName= langName[this.targetLanguage];
		}
		else {
			// targetName= this.target;
			return;  // If name for the language code is not defined, ignore this pair
		}
		langPairs.push({value:this.sourceLanguage+"-"+this.targetLanguage,text:sourceName+" to "+targetName});
	});
	
	// Sort available pairs by name:
	langPairs.sort(function (a,b) {
		return a.text.localeCompare(b.text);
	});

	$("#lang").empty();
	// Populate select list:
	$.each(langPairs, function() {
		if (this.value=="en-es") {
			// Set en-es as default:
			$("#lang").append($("<option selected='selected'>").val("en-es")
				.text("English to Spanish"));
		}
		else {
			$("#lang").append($("<option>").val(this.value).text(this.text));
		}
	});
}

// Preprocessing: extract all the possible segments and translate them using batch API:
function sliceAndTranslate() {
	if ($.trim($("#to").val())!="") {
		var question = confirm("Are you sure you want to proceed? Existing translation will be deleted!");
		if (!question) {
			return;
		}
	}

	resetAutocomplete();
	$("#wait1").html("&nbsp;").show();
	$("#wait2").html("&nbsp;").show();
	updateCount();

	$("#to").val("Type here your translation.").focus().select();

	var words = document.getElementById("from").value.split(/[\s]+/);
	var wordsCount= words.length;

	if (wordsCount*maxLength>maxSegments) {
		$("#wait1").html("Size restrictions exceeded! Only translations for the first words will be available.")
			.fadeIn().delay(8000).fadeOut(3000, 
				function () {
    				$(this).html("&nbsp;").show(); // avoid stretching of the space
				});
		wordsCount= maxSegments/maxLength;
	} 

	$("#wait2").html("<img src='Throbber-small.gif'/> Gathering translations. You may start typing though.")
		.fadeIn().delay(3000).fadeOut(1000, 
			function () {
    			$(this).html("&nbsp;").show(); 
			});

	for (var i=0;i<wordsCount;i++) {
		var sourceSegments= [];
		for (var j=1;j<=maxLength;j++) {
			if (i+j<=words.length) {
				sourceSegments.push(words.slice(i,i+j).join(" "));
			}
			// Batch version of the API is used so that all the different spans starting from a 
			// particular word are translated together.
			// TO-DO: group more spans until the limit of 2KB supported by Safari
		}
		// API requests are made sequentially (although each request is still asynchronous so that
		// the user can start typing as soon as she wants) to avoid overloading the server and also
		// to ensure that segments for the word i are available before segments for the word i+1
		// (when multiple Ajax requests are made in rapid succession, the results can be returned out of 
		// order). It is a non-critical race condition, but it seems positive to deal with it.

		// To avoid sequential calls, just do this:
		translate(sourceSegments,i);
	}
}
