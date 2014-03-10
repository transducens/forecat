// Forecat Tool
// (c) 2012 Juan Antonio Perez-Ortiz
// Transducens Research Group, Universitat d'Alacant

// Services must be called in this order: Languages; if Languages successes, Translation; 
// if Translation successes, Suggestions as many times as needed; however, it is not necessary 
// to wait for Suggestions callback to call Selection


// Global variables ------------------------------------------------------------

// Show log information on console:
var CONSOLE_LOG= true;

// Retrieve API keys from cookies:
var apertiumApiKey=  getCookie("apertiumkey") || "FORECATKEY";
var bingApiKeyId= getCookie("bingkeyid") || "";
var bingApiKeySecret= getCookie("bingkeysecret") || "";
var googleApiKey= getCookie("googlekey") || "";

// Input list for the Languages service:
var inputLanguagesService= [{engine:"apertium", key:apertiumApiKey},
                            {engine:"bing", key:bingApiKeyId+","+bingApiKeySecret},
                            {engine:"google", key:googleApiKey}];
// var inputLanguagesService= [{engine:"apertium", key:apertiumApiKey}];

var maxSegmentLength;  // Length of longest suggestion
var sourceCode;  // Source language
var targetCode;  // Target language

var keystrokes= 0;  // Count number of keystrokes
var firstStroke= true;  // False after the users presses the first 'real' key

var currentRequestId= 0; // a different number is assigned to every request to Suggestions so that
						 // responses for older requests are ignored if newer request have been done

var currentSegmentStart;  // Start position (word level) of the current segment in target language
var suggestions = [];  // Suggestion list for the current segment


// General functions -----------------------------------------------------------

// Function 'init' must be called after Forecat.onModuleLoad to ensure that GWT initialization has ended.
function init () {
	languagesService();
}

function setMaxSegmentLength (value) {
	maxSegmentLength= parseInt(value);
}

function setLanguagePair (value) {
	sourceCode= value.split("-")[0]; 
	targetCode= value.split("-")[1];
}

// Function code for setCookie from http://www.w3schools.com/js/js_cookies.asp
function setCookie(c_name,value,exdays) {
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}

// Function code for getCookie from http://www.w3schools.com/js/js_cookies.asp
function getCookie(c_name) {
	var i,x,y,ARRcookies=document.cookie.split(";");
	for (i=0;i<ARRcookies.length;i++) {
		x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		x=x.replace(/^\s+|\s+$/g,"");
		if (x==c_name) {
			return unescape(y);
		}
	}
}

function setApiKey (c_name,message) {
	var key= getCookie(c_name);
	if (key==null) {
		key="Key";
	}
	key= window.prompt(message+" Please, note that this key will be stored unencrypted as a cookie browser.",key);
	if (key!=null) {
		if (c_name=="apertiumkey") {
			apertiumApiKey= key;
			setCookie(c_name,key,500);  // Keep cookie for these days
			return true;
		}
		else if (c_name=="bingkeyid") {
			apertiumApiKeyId= key;
			setCookie(c_name,key,500);
			return true;
		}
		else if (c_name="bingkeysecret") {
			apertiumApiKeySecret= key;
			setCookie(c_name,key,500);
			return true;
		}
		return false;
	}
	return false;
}


// Languages service -----------------------------------------------------------

// Make a request to get the array of available pairs:
function languagesService () {
	if (CONSOLE_LOG) {
		console.log("Request to Languages service: ",inputLanguagesService);
	}
	languagesService_java(inputLanguagesService,languagesServiceSuccess_callback,languagesServiceFailure_callback);
}

function languagesServiceSuccess_callback (languagesOutput) {
	
	if (CONSOLE_LOG) {
		console.log("Response from Languages service: ",languagesOutput);
	}

	languagesInterfaceEmpty();
	
	// Populate select list and merge equal pairs for different engines:	
	var i=0;
	var found= false;
	while (i<languagesOutput.length) {
		var pair= languagesOutput[i];
		var optionText= pair.sourceName+" to "+pair.targetName;
		var optionValue= pair.sourceCode+"-"+pair.targetCode;
		var optionEngines= pair.engine;
		var samePair= true;
	
		// Group the same pairs for different engines:
		while (samePair && i<languagesOutput.length-1) {
			var nextPair= languagesOutput[i+1];
			if (nextPair.sourceCode==pair.sourceCode && nextPair.targetCode==pair.targetCode) {
				optionEngines+= ","+nextPair.engine;
				++i;
			}
			else {
				samePair= false;
			}
		}
		if ((pair.sourceCode=="en" && pair.targetCode=="es") || (!found && i>=languagesOutput.length-1)) {
			// Set en-es as default (or the last pair if en-es is not available):
			languagesInterfaceAddOption(optionValue,optionText,optionEngines,true);
			sourceCode= pair.sourceCode;
			targetCode= pair.targetCode;
			found= true;
		}
		else {
			languagesInterfaceAddOption(optionValue,optionText,optionEngines,false);
		}
		++i;
	}
}

function languagesServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Languages service: " + s);
	}
}

function languagesInterfaceEmpty () {
	$("#lang").empty();
}

function languagesInterfaceAddOption (optionValue,optionText,optionEngines,selected) {
	if (selected) {
		$("#lang").append($("<option selected='selected'>").val(optionValue).text(optionText+" ("+optionEngines+")"));
	}
	else {
		$("#lang").append($("<option>").val(optionValue).text(optionText+" ("+optionEngines+")"));
	}
}


// Translation service ---------------------------------------------------------

function translationService (sourceText) {

	if (!translationInterfaceRetranslate()) {
		return;
	}
		
	translationResetGlobalVariables();
	
	translationInterfaceInitInput();
	
	var input= {sourceText:sourceText,sourceCode:sourceCode,targetCode:targetCode,maxSegmentLength:maxSegmentLength};
	if (CONSOLE_LOG) {
		console.log("Request to Translation service: ",input);
	}
	translationService_java(input,translationServiceSuccess_callback,translationServiceFailure_callback);
}

function translationServiceSuccess_callback (result) {
	
	if (CONSOLE_LOG) {
		console.log("Response from Translation service: ",result);
	}

	translationInterfaceUpdateNumberSegments(result.numberSegments);
	if (result.numberSegments<result.maxNumberSegments) {
		translationInterfaceRestrictionsApplied();
	}
	translationInterfaceHideMessages();
}

function translationServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Translation service: " + s);
	}
}

function translationInterfaceRetranslate () {
	if ($.trim($("#to").val())!="") {
		var question = confirm("Are you sure you want to proceed? Existing translation will be deleted!");
		return question;
	}
	return true;
}

function translationInterfaceInitInput () {
	$("#wait1").html("&nbsp;").show();
	$("#wait2").html("&nbsp;").show();
	$("#count").html(0);

	$("#to").val("Type here your translation.").focus().select();
	$("#wait2").html("<img src='../img/Throbber-small.gif'/> Gathering translations. You may start typing now, though...").fadeIn();
}

function translationInterfaceUpdateNumberSegments(numberSegments) {
	$("#count").html(numberSegments);
}

function translationInterfaceRestrictionsApplied() {
	$("#wait1").html("Size restrictions exceeded! Only translations for the first words will be available.")
		.fadeIn().delay(6000).fadeOut(3000, 
			function () {
				$(this).html("&nbsp;").show(); // avoid changes in the space allocated for the div
			});
}

function translationInterfaceHideMessages() {
	$("#wait2").fadeOut(1000, function () {
		$(this).html("&nbsp;").show(); 
	});
}

// Reset global variables:
function translationResetGlobalVariables() {
	currentSegmentStart= 0;
	suggestions= [];
	firstStroke= true;
	keystrokes= 0;
}


// Suggestions service ---------------------------------------------------------

function suggestionsService(term,responseFunction) {
	
	suggestions = [];
	
	suggestionsInterfaceClearMessages();
	var prefix= term.slice(currentSegmentStart);

	// Only call suggestionsService if current segment is non empty:
	if ($.trim(prefix).length==0) {
		responseFunction([]);
		return;
	}

	var alreadyAccepted= term.slice(0,currentSegmentStart);
	var words = alreadyAccepted.split(/[\s]+/);
	wordLevelStart= words.length;
	
	// TODO: decide whether to send the whole current translation (or send the total string length instead)
	var inputSuggestionsService= {targetText:term,prefixStart:wordLevelStart,prefixText:prefix};
	currentRequestId++;

	if (CONSOLE_LOG) {
		console.log("Request to Suggestions service: ",inputSuggestionsService);
	}
	suggestionsService_java(inputSuggestionsService,suggestionsServiceSuccess_callback,
		suggestionsServiceFailure_callback,
		{autocompleteSourceResponseFunction:responseFunction,prefix:prefix,requestId:currentRequestId});
}

function suggestionsServiceSuccess_callback (result,context) {
	
	if (CONSOLE_LOG) {
		console.log("Response from Suggestions service: ",result);
	}
	// Reject responses for old requests:
	if (context.requestId<currentRequestId) {
		return;
	}

	for (i=0,n=result.length;i<n;++i) {
		// Suggestions take the form {label, value}; 'label' is the text to
		// be shown in the suggestion list; 'value' is the text to insert in
		// text area. Initially both are equal, but the function 
		// suggestionsInterfaceAddShortcuts will add the 'Ctrl+1,2...' text to 
		// the beginning.
		suggestions.push({"label":result[i].suggestionText,"value":result[i].suggestionText});
	}

	if (suggestions.length==0) {

		suggestionsInterfaceNoSuggestion(context.prefix);
		
		/* JQuery autocomplete documentation (http://jqueryui.com/demos/autocomplete/):
		 * "You must always call the response callback even if you encounter an error. This 
		 * ensures that the widget always has the correct state." 
		 * Therefore, do not make return here.
		 */
	}

	// Add the text with the shortcut combination:
	suggestionsInterfaceAddShortcuts(suggestions);

	context.autocompleteSourceResponseFunction(suggestions);
}

function suggestionsServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Suggestions service: " + s);
	}
}

// Add text with shortcut keys to the beginning of the label of each suggestion:
function suggestionsInterfaceAddShortcuts (suggestions) {
	for (var i=0;i<suggestions.length;i++) {
		if (i<5) {
			suggestions[i].label= "<span class='ctrl'>Ctrl+"+(i+1)+"</span>" + suggestions[i].label;
		}
		else {
			suggestions[i].label= "<span class='noctrl'>Ctrl+0</span>" + suggestions[i].label;	
		}
	}
}

function suggestionsInterfaceClearMessages() {
	$("#wait1").html("&nbsp;");
}


function suggestionsInterfaceNoSuggestion (prefix) {
	$("#wait1").html("No suggestion for '"+prefix+"'");
}


// Selection service -----------------------------------------------------------

function selectionService(object,selectedText) {

	var prefix= object.value.slice(0,currentSegmentStart);
	object.value = prefix + selectedText;
	currentSegmentStart= object.value.length;

	var inputSelectionService= {selectionText:selectedText};
	if (CONSOLE_LOG) {
		console.log("Request to Selection service: ",inputSelectionService);
	}
	selectionService_java(inputSelectionService,selectionServiceSuccess_callback,selectionServiceFailure_callback);
	
	suggestions= [];
}

function selectionServiceSuccess_callback (result) {
	if (CONSOLE_LOG) {
		console.log("Response from Selection service: ",result);
	}
	
	selectionInterfaceUpdateNumberSegments(result.numberSegments);
}

function selectionServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Selection service: " + s);
	}
}

function selectionInterfaceUpdateNumberSegments(numberSegments) {
	$("#count").html(numberSegments);
}

// Function for JKey events:
function ctrlShortcut (object,pos) {
	if(suggestions.length>0) {
		selectionService(object,suggestions[pos].value);
		$(object).autocomplete("disable");
		$(object).autocomplete("close");
		//event.preventDefault();
	}
}


// Start program ---------------------------------------------------------------

$(function() {

	InterfaceInit();
	
	setMaxSegmentLength(4);

	$("#to")
		// Don't navigate away from the field on tab when selecting an item:
		.bind("keydown", function( event ) {
			if (event.keyCode != $.ui.keyCode.TAB && !event.ctrlKey && !event.shiftKey) {
				keystrokes++;
			}
			if (CONSOLE_LOG) {
				console.log("Keystrokes: ",keystrokes);
			}
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

			// TODO: selectionStart does not work on IE<9 (caret position)
			
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
			delay: 200,  // default 300; use a higher value for remote proposals
			autoFocus: true,  // first suggestion in the list always starts selected
			source: function(request,response) {
				// Avoid trying to process the shift, ctrl... keys when one of these is the first key 
				// pressed by the user (this is to prevent a message like 
				// "No suggestion for 'Type your translation here'")
				if (firstStroke) {
					response([]); 
					return;
				}
				suggestionsService(request.term,response);
			},

			focus: function() {
				return false;  // prevent value inserted on focus
			},

			select: function(event,ui) {
				selectionService(this,ui.item.value);
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


function InterfaceInit () {

	$('.slidetoogleTitle').click(function () {
		$(this).parent().next().slideToggle('fast', function() {
			($(this).prev().children(':first-child').html() == "hide") ? 
				$(this).prev().children(':first-child').html("show") : 
				$(this).prev().children(':first-child').html("hide");
		});
	});

	$("#translate").button();  // Comment this line for a regular button, instead of a JQuery UI one
	$("#apertium-key").button();  // Comment this line for a regular button, instead of a JQuery UI one
	$("#bing-key").button();  // Comment this line for a regular button, instead of a JQuery UI one
	
	var apiKeyMessage= "API keys retrieved from cookies for: ";
	if (apertiumApiKey!="") {
		apiKeyMessage+= "apertium ";
	}
	if (bingApiKeyId!="" && bingApiKeySecret!="") {
		apiKeyMessage+= "bing ";
	}
	if (googleApiKey!="") {
		apiKeyMessage+= "google ";
	}
	if (CONSOLE_LOG) {
		console.log(apiKeyMessage);
	}
	
	var s = ["Put here the text to","translate."].join(" ");
	document.getElementById("from").value= s;
	$("#from").focus().select();  // In the beginning set the focus in the source text textarea 	
}
