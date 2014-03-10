

var apertium = new function() {
    this.url = "http://api.apertium.org/json/translate";
    var a = 0;
    this.key = "YOURAPIKEY";
    this.supported_pairs = [{"source": "ro","target": "es"}, {"source": "es","target": "fr"}, {"source": "en","target": "gl"}, {"source": "oc","target": "es"}, {"source": "es","target": "ro"}, {"source": "es","target": "ca_valencia"}, {"source": "mk","target": "bg"}, {"source": "fr","target": "es"}, {"source": "oc_aran","target": "ca"}, {"source": "pt","target": "gl"}, {"source": "en","target": "ca"}, {"source": "an","target": "es"}, {"source": "eu","target": "es"}, {"source": "es","target": "gl"}, {"source": "es","target": "ca"}, {"source": "fr","target": "eo"}, {"source": "nb","target": "nn_a"}, {"source": "ca","target": "pt"}, {"source": "mk","target": "en"}, {"source": "ca","target": "en_US"}, {"source": "is","target": "en"}, {"source": "pt","target": "ca"}, {"source": "fr","target": "ca"}, {"source": "gl","target": "en"}, {"source": "gl","target": "es"}, {"source": "ca","target": "oc_aran"}, {"source": "nn","target": "nb"}, {"source": "ca","target": "oc"}, {"source": "en","target": "es"}, {"source": "es","target": "pt"}, {"source": "oc_aran","target": "es"}, {"source": "es","target": "eo"}, {"source": "es","target": "en"}, {"source": "oc","target": "ca"}, {"source": "cy","target": "en"}, {"source": "ca","target": "fr"}, {"source": "br","target": "fr"}, {"source": "en","target": "eo"}, {"source": "bg","target": "mk"}, {"source": "ca","target": "eo"}, {"source": "ca","target": "en"}, {"source": "es","target": "oc_aran"}, {"source": "sv","target": "da"}, {"source": "pt","target": "es"}, {"source": "nn","target": "nn_a"}, {"source": "es","target": "pt_BR"}, {"source": "es","target": "oc"}, {"source": "es","target": "an"}, {"source": "da","target": "sv"}, {"source": "gl","target": "pt"}, {"source": "it","target": "ca"}, {"source": "eo","target": "en"}, {"source": "ca","target": "es"}, {"source": "nn_a","target": "nn"}, {"source": "es","target": "en_US"}, {"source": "nb","target": "nn"}];
    this.getJSON = function(d, h) {
        var c = "_json" + a++;
        d = d + "&callback=apertium." + c;
        var g = document.createElement("script");
        g.type = "text/javascript";
        g.src = d;
        apertium[c] = function(j) {
            delete apertium[c];
            if (e) {
                e.removeChild(g)
            }
            h(j)
        };
        var e = document.getElementsByTagName("head")[0];
        e.insertBefore(g, e.firstChild);
        var f = f || 20000;
        window.setTimeout(function() {
            if (typeof apertium[c] == "function") {
                apertium[c] = function(j) {
                    delete jsonp[c]
                };
                h({responseData: null,responseDetails: "timeout",responseStatus: 509});
                window.setTimeout(function() {
                    if (typeof apertium[c] == "function") {
                        delete apertium[c]
                    }
                }, 60000)
            }
        }, f)
    };
    this.translate = function(j, h, d, k) {
        var f, g = "txt";
        if (j.type) {
            g = j.type;
            f = j.text
        } else {
            f = j
        }
	// Original library modified to allow batch processing of translations
	// It also returns now the source segment
	// Now, j can contain an array of strings to be translated, besides a single string
	var query= "";
	if ($.isArray(f)) {  // Use of JQuery isArray
		for (var i=0; i < f.length; i++) {
			query+= "q=" + encodeURIComponent(f[i])+"&";
		}
	}
	else {
		query+= "q=" + encodeURIComponent(f)+"&";
	}
        var c = "?" + query + "format=" + g + "&langpair=" + h + encodeURIComponent("|") + d + "&key=" + apertium.key + "&markUnknown=no";
        var e = apertium.url + c;
        apertium.getJSON(e, function(m) {
		var l;
		// Note that when translating an array with a single segment, responseData in the response
		// is not an array since the query is not treated as a batch translation.
		if ($.isArray(m.responseData)) {
			for (var i=0; i < m.responseData.length;i++) {
				if (m.responseData[i].responseStatus == 200) {
					l = {source: f[i], translation: m.responseData[i].responseData.translatedText}

				} else {
					l = {source: f[i], translation: null,error: {code: m.responseData[i].responseStatus,
					     message: m.responseData[i].responseDetails}}
				}
            			k(l); // Callback is invoked for every different text in the input array	
			}
		} else {
			if (m.responseStatus == 200) {
				l = {source: f, translation: m.responseData.translatedText}
			} else {
				l = {source: f, translation: null,error: {code: m.responseStatus,message: m.responseDetails}}
			}
            		k(l);
		}
		
        })
    };
    this.isTranslatablePair = function(e, f) {
        var c = false;
        for (var d = 0; d < this.supported_pairs.length; d++) {
            pair = this.supported_pairs[d];
            if (pair.source == e && pair.target == f) {
                return true
            }
        }
        return c
    };
    this.isTranslatable = function(e) {
        var c = false;
        for (var d = 0; d < this.supported_pairs.length; d++) {
            pair = this.supported_pairs[d];
            if (pair.source == e) {
                return true
            }
        }
        return c
    };
    this.getsources = function() {
        var c = Array();
        for (var e = 0; e < this.supported_pairs.length; e++) {
            pair = this.supported_pairs[e];
            var f = false;
            for (var d = 0; d < c.length; d++) {
                if (c[d] == pair.source) {
                    f = true
                }
            }
            if (!f) {
                c[c.length] = pair.source
            }
        }
        return c
    };
    this.gettargets = function(e) {
        var d = Array();
        for (var c = 0; c < this.supported_pairs.length; c++) {
            pair = this.supported_pairs[c];
            if (e == pair.source) {
                d[d.length] = pair.target
            }
        }
        return d
    };
    this.getSupportedLanguagePairs = function() {
        return this.supported_pairs
    }
};
apertium.ContentType = {TEXT: "txt",HTML: "html"};
