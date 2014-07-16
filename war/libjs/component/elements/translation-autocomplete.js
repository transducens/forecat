
var haystack= [];
var skipSearch = false;
var keyboardSelect = -1;
var poly= null;
var readygwt= false;
var readypolymer= false;

var languagesAvailable= null;
var sourceCode= null;
var targetCode= null;

// Show log information on console:
var CONSOLE_LOG= true;
    
// Input list for the Languages service:
//var inputLanguagesService= [{engine:"apertium", key:apertiumApiKey},
//                            {engine:"bing", key:bingApiKeyId+","+bingApiKeySecret},
//                            {engine:"google", key:googleApiKey}];
//var inputLanguagesService= [{engine:"apertium", key:apertiumApiKey}];

var keystrokes= 0;  // Count number of keystrokes
var firstStroke= true;  // False after the users presses the first 'real' key

var currentRequestId= 0; // a different number is assigned to every request to Suggestions so that
 						 // responses for older requests are ignored if newer request have been done

var currentSegmentStart;  // Start position (word level) of the current segment in target language
var suggestions = [];  // Suggestion list for the current segment

    
function readycheck () {
  if (readygwt && readypolymer) {
    completely_ready();
  }
}
    
function init () {
  readygwt= true;
  readycheck();
}

// Languages service ----------------------------------------------------------------------------

function languagesServiceSuccess_callback (languagesOutput) {
  if (CONSOLE_LOG) {
    console.log("Response from Languages service: ",JSON.stringify(languagesOutput));
  }  
  // Populate select list and merge equal pairs for different engines:	
  var i=0;
  var found= false;
  languagesAvailable= [];
  while (i<languagesOutput.length) {
    var pair= languagesOutput[i];
	var optionText= pair.sourceName+" to "+pair.targetName;
	var optionValue= pair.sourceCode+"-"+pair.targetCode;
	var optionEngines= [pair.engine];
	var samePair= true;
	
	// Group the same pairs for different engines:
	while (samePair && i<languagesOutput.length-1) {
	  var nextPair= languagesOutput[i+1];
	  if (nextPair.sourceCode==pair.sourceCode && nextPair.targetCode==pair.targetCode) {
		optionEngines.push(nextPair.engine);
		++i;
	  }
	  else {
	    samePair= false;
	  }
	}
	var element= {'source': pair.sourceCode, 'target': pair.targetCode, 'engines': optionEngines};
	languagesAvailable.push(element);
	++i;
  }
  // 'this' is not the polymer object here; use 'poly' instead 
  poly.fire('languagesReady',languagesAvailable);
}
   
function languagesServiceFailure_callback (s) {
  console.error("Error in Languages service: " + s);
}

function completely_ready() {
	// var input= [{engine:"apertium", key:apertiumApiKey},
	//             {engine:"bing", key:bingApiKeyId+","+bingApiKeySecret},
	//             {engine:"google", key:googleApiKey}];
	var input= [{engine:"apertium", key:"FORECATKEY"}];
	if (CONSOLE_LOG) {
	  console.log("Request to Languages service: ",JSON.stringify(input));
	}
	languagesService_java(input,languagesServiceSuccess_callback,languagesServiceFailure_callback);
}


// Translation service -----------------------------------------------------------------

function translationService (sourceText) {
  translationResetGlobalVariables();
  //var input= {sourceText:sourceText,sourceCode:sourceCode,targetCode:targetCode,maxSegmentLength:maxSegmentLength};
  var input= {sourceText:sourceText,sourceCode:sourceCode,targetCode:targetCode,maxSegmentLength:+poly.maxSegmentLength,minSegmentLength:1};
  if (CONSOLE_LOG) {
	console.log("Request to Translation service: ",input);
  }
  translationService_java(input,translationServiceSuccess_callback,translationServiceFailure_callback);
}

function translationServiceSuccess_callback (result) {
	if (CONSOLE_LOG) {
		console.log("Response from Translation service: ",result);
	}
	if (result.numberSegments<result.maxNumberSegments) {
		console.log("Restrictions applied!");
	}
	poly.fire('translationReady');
}

function translationServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Translation service: " + s);
	}
}

// Reset global variables:
function translationResetGlobalVariables() {
	currentSegmentStart= 0;
	suggestions= [];
	firstStroke= true;
	keystrokes= 0;
}


// Suggestions service ------------------------------------------------------------

function suggestionsService(term,responseFunction) {
	
	suggestions = [];
	
	var prefix= term.slice(currentSegmentStart);

	// Only call suggestionsService if current segment is non empty:
	if (prefix.trim()==="") {
		responseFunction([]);
		return;
	}

	var alreadyAccepted= term.slice(0,currentSegmentStart);
	var words = alreadyAccepted.split(/[\s]+/);
	wordLevelStart= words.length;
	
	// TODO: decide whether to send the whole current translation (or send the total string length instead)
	//var inputSuggestionsService= {targetText:term,prefixStart:wordLevelStart,prefixText:prefix};
	var inputSuggestionsService= {targetText:term,position:wordLevelStart,prefixText:prefix};
	currentRequestId++;

	if (CONSOLE_LOG) {
		console.log("Request to Suggestions service: ",JSON.stringify(inputSuggestionsService));
	}
	suggestionsService_java(inputSuggestionsService,suggestionsServiceSuccess_callback,
		suggestionsServiceFailure_callback,
		{autocompleteSourceResponseFunction:responseFunction,prefix:prefix,requestId:currentRequestId});
}

function suggestionsServiceSuccess_callback (result,context) {
	
	if (CONSOLE_LOG) {
		console.log("Response from Suggestions service: ",JSON.stringify(result));
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

	// TODO: allow for shortcuts with control key

	context.autocompleteSourceResponseFunction(suggestions);
}

function suggestionsServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Suggestions service: " + s);
	}
}

function responseAutocomplete (suggestions) {
	// Clear results
    while (poly.results.length >0) {
      poly.results.pop();
    }    
    for (var i = 0; i < suggestions.length; i++ ) {
      poly.results.push(suggestions[i].value);
    }    
}


// Selection Service ---------------------------------------------------------

function selectionService(object,selectedText) {

	var prefix= object.textContent.slice(0,currentSegmentStart);
	object.textContent = prefix + selectedText;
	currentSegmentStart= object.textContent.length;

	var inputSelectionService= {selectionText:selectedText};
	if (CONSOLE_LOG) {
		console.log("Request to Selection service: ",JSON.stringify(inputSelectionService));
	}
	selectionService_java(inputSelectionService,selectionServiceSuccess_callback,selectionServiceFailure_callback);
	
	suggestions= [];
}

function selectionServiceSuccess_callback (result) {
	if (CONSOLE_LOG) {
		console.log("Response from Selection service: ",JSON.stringify(result));
	}
}

function selectionServiceFailure_callback (s) {
	if (CONSOLE_LOG) {
		console.error("Error in Selection service: " + s);
	}
}


// Others -----------------------------------------------------------------
	
var ie = (typeof document.selection != "undefined" && document.selection.type != "Control") && true;
var w3 = (typeof window.getSelection != "undefined") && true;

function getCaretPosition(element) {
    var caretOffset = 0;
    if (w3) {
    	/* Selection in Chrome:
    	   https://code.google.com/p/chromium/issues/detail?id=380690
    	   http://stackoverflow.com/questions/23882272/contenteditable-in-shadow-dom
    	 */
    	// TODO: improve this
    	// TODO: http://www.quirksmode.org/dom/range_intro.html
    	// TODO: caret position only works in Firefox now
    	// TODO: try to use rangy js library
        var range = window.getSelection().getRangeAt(0);
        var preCaretRange = range.cloneRange();
        preCaretRange.selectNodeContents(element);
        preCaretRange.setEnd(range.endContainer, range.endOffset);
        caretOffset = preCaretRange.toString().length;
    } else if (ie) {
        var textRange = document.selection.createRange();
        var preCaretTextRange = document.body.createTextRange();
        preCaretTextRange.moveToElementText(element);
        preCaretTextRange.setEndPoint("EndToEnd", textRange);
        caretOffset = preCaretTextRange.text.length;
    }
    return caretOffset;
}

// Caret position moves to the beginning after update; use this to move it to the beginning
// See http://stackoverflow.com/questions/16230720/set-the-caret-position-always-to-end-in-contenteditable-div
function placeCaretAtEnd(el) {
    el.focus();
    if (w3) {
        var range = document.createRange();
        range.selectNodeContents(el);
        range.collapse(false);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (ie) {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.collapse(false);
        textRange.select();
    }
}


// Component registration --------------------------------------------------------------

Polymer('translation-autocomplete', {
  pair: null,
  maxSegmentLength: "4",
  sourceText: null,
  apertiumApiKey:  null,
  bingApiKeyId: null,
  bingApiKeySecret: null,
  googleApiKey: null,
  
  ready: function() { 
    this.results= [];
    this.$.translationBox.focus();
    this.$.translationBox.textContent= "Type...";
    
    poly= this;
    readypolymer= true;
    readycheck();
  },
    
  pairChanged: function (oldValue,newValue) {
	// TODO: check value is correct
    sourceCode= newValue.split("-")[0]; 
    targetCode= newValue.split("-")[1];
    if (CONSOLE_LOG) {
    	console.log("New pair: "+newValue);
    }
  },
  
  sourceTextChanged: function (oldValue,newValue) {
	// TODO: check value is correct
    if (CONSOLE_LOG) {
    	console.log("New source text: "+newValue);
    }
    translationService(newValue);
  },
    
  keyup: function(e) { 
    switch (e.keyCode) {
      case 27: // escape
        this.clear();
        break;
      case 38: // up arrow
        this.moveUp();
        break;
      case 40: // down arrow
        this.moveDown();
        break;
      case 13: // enter
        this.select();
        break;
      default: // TO-DO; use change event instead of keyup
    	  
    	//console.log(getCaretPosition(this.$.translationBox));
    	
    	var prefix= e.target.textContent;
    	  
      	// Check whether user is typing at the end or it is the first character in the very beginning:
		if ((getCaretPosition(this.$.translationBox)==prefix.length) || firstStroke) {
			console.log("caret="+getCaretPosition(this.$.translationBox)+"; prefix length="+prefix.length);
			firstStroke= false;  // This is to solve the problem of caret position pointing to
								 // the end of the text "Type your translation here" the first time
			
			// This is to prevent the autocomplete not showing after deleting back until
			// the first letter of a word (included) and typing again after a blank.
			if (prefix.length==0 || prefix[prefix.length-1]==" ") {
				currentSegmentStart= prefix.length;
			}

			// Reset after pressing the space bar:
			if (e.keyCode === 32) {
				// TODO: check +1-1
				currentSegmentStart= prefix.length+1-1;
			}
			this.performSearch(prefix);
		} 
		break;
    }
  },
    
  /* Some code for the following functions was originally taken from 
  https://github.com/sethladd/dart-polymer-dart-examples-unmaintained/tree/master/web/auto_complete */
  
  select: function() {
    var lis = this.shadowRoot.querySelectorAll('ul li');
    selectionService(this.$.translationBox,lis[keyboardSelect].textContent);
    placeCaretAtEnd(this.$.translationBox);
    skipSearch = true;
    this.reset();
  },
    
  clear: function() {
    this.reset();
    this.$.translationBox.textContent= "";
    skipSearch= true;
  },
    
  reset: function() {
    keyboardSelect= -1;
    while (this.results.length >0) {
      this.results.pop();
    }
  },
    
  moveDown: function() {
    // Be careful with compatibility issues with HTML5 classList:
    // see http://stackoverflow.com/a/196038
    var lis = this.shadowRoot.querySelectorAll('ul li');
    if (keyboardSelect >= 0) lis[keyboardSelect].classList.remove('selecting');
    keyboardSelect = ++keyboardSelect == lis.length ? 0 : keyboardSelect;
    lis[keyboardSelect].classList.add('selecting');
  },
    
  moveUp: function() {
    lis = this.shadowRoot.querySelectorAll('ul li');
    if (keyboardSelect >= 0) lis[keyboardSelect].classList.remove('selecting');
    if (keyboardSelect == -1) keyboardSelect = lis.length;
    keyboardSelect = --keyboardSelect == -1 ? lis.length-1 : keyboardSelect;
    lis[keyboardSelect].classList.add('selecting');
  },
    
  performSearch: function(prefix) {
    if (skipSearch) {
      skipSearch = false;
      return;
    }
    suggestionsService(prefix, responseAutocomplete);
  }

});

