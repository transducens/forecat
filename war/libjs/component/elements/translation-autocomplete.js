
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

function languagesServiceSuccess_callback (languagesOutput) {
  if (CONSOLE_LOG) {
    console.log("Response from Languages service: ",JSON.stringify(languagesOutput));
  }
  languagesAvailable= languagesOutput;
  // this is not the polymer object here; use poly instead 
  poly.fire('languagesReady',languagesOutput);
 }
   
function languagesServiceFailure_callback (s) {
  console.error("Error in Languages service: " + s);
}

function completeley_ready() {
	  languagesService_java([{engine:"apertium", key:"FORECATKEY"}],languagesServiceSuccess_callback,languagesServiceFailure_callback);
}

Polymer('translation-autocomplete', {
  pair: null,
  maxSegmentLength: "4",
  apertiumApiKey:  null,
  bingApiKeyId: null,
  bingApiKeySecret: null,
  googleApiKey: null,
  
  ready: function() {
    this.search= "Teclea"; 
    this.results= [];
    this.$.textInput.focus();
    this.$.translation.textContent= "Type...";
      
    var dataSource = this.querySelector('.data-source');
    if (dataSource == null) {
      console.log("WARNING: expected to find a .data-source <ul> as a child");
      return;
    }
    var elements= dataSource.getElementsByTagName("li");
    for (var i = 0; i < elements.length; i++ ) {
      haystack.push(elements[i].textContent);
    }
    
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
    
  keyup: function(e) {
    var prefix;
    if (e.target.id=="textInput") {
      prefix= this.search; 
      console.log("Tecleando en input");
    }
    else if (e.target.id="translation") {
      prefix= e.target.textContent;
      console.log("Tecleando en div");
    }
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
        this.performSearch(prefix);
        break;
    }
  },
    
  select: function() {
    var lis = this.shadowRoot.querySelectorAll('ul li');
    this.search = lis[keyboardSelect].textContent;
    this.$.translation.textContent= lis[keyboardSelect].textContent;
    skipSearch = true;
    this.reset();
  },
    
  clear: function() {
    this.reset();
    this.search= "";
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
    // Clear results
    while (this.results.length >0) {
      this.results.pop();
    }
    if (prefix.trim()==="") return;
    var lower = prefix.toLowerCase();
    for (var i = 0; i < haystack.length; i++ ) {
      if (haystack[i].toLowerCase().slice(0,lower.length) == lower) {
        this.results.push(haystack[i]);
      }
    }
  }

});

