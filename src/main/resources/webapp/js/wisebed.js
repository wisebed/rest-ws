
if( typeof wisebedBaseUrl === 'undefined' )
	var wisebedBaseUrl = "";

if( typeof wisebedWebSocketBaseUrl === 'undefined' ) {
	hostname = document.location.hostname;
	port     = document.location.port;
	if(port == "") {
		port = 80;
	}
	var wisebedWebSocketBaseUrl = 'ws://' + hostname + ':' + port;
}

Array.prototype.compareArrays = function(arr) {
	if (this.length != arr.length) return false;
	for (var i = 0; i < arr.length; i++) {
		if (this[i].compareArrays) { //likely nested array
			if (!this[i].compareArrays(arr[i])) return false;
			else continue;
		}
		if (this[i] != arr[i]) return false;
	}
	return true;
};

var Wisebed = new function() {

	this.testCookie = function (callbackOK, callbackError) {
		// Check cookie
		var getCookieCallbackDone = function() {
			$.ajax({
				url: wisebedBaseUrl + "/rest/2.3/cookies/check",
				success: callbackOK,
				error: callbackError,
				xhrFields: { withCredentials: true }
			});
		};

		// Get cookie
		$.ajax({
			url: wisebedBaseUrl + "/rest/2.3/cookies/get",
			success: getCookieCallbackDone,
			error: callbackError,
			xhrFields: { withCredentials: true }
		});
	}

	this.reservations = new function() {

		this.getPersonal = function(testbedId, from, to, callbackDone, callbackError) {
			var queryUrl = wisebedBaseUrl + "/rest/2.3/" + testbedId + "/reservations?userOnly=true" +
					(from ? ("&from=" + from.toISOString()) : "") +
					(to ? ("&to="+to.toISOString()) : "");
			$.ajax({
				url: queryUrl,
				success: callbackDone,
				error: callbackError,
				context: document.body,
				dataType: "json",
				xhrFields: { withCredentials: true }
			});
		};

		this.getPublic = function(testbedId, from, to, callbackDone, callbackError) {
			var queryUrl = wisebedBaseUrl + "/rest/2.3/" + testbedId + "/reservations?" +
					(from ? ("from=" + from.toISOString() + "&") : "") +
					(to ? ("to="+to.toISOString() + "&") : "");
			$.ajax({
				url: queryUrl,
				success: callbackDone,
				error: callbackError,
				context: document.body,
				dataType: "json",
				xhrFields: { withCredentials: true }
			});
		};

		this.make = function(testbedId, from, to, userData, nodeURNs, callbackDone, callbackError) {

			// Generate JavaScript object
			var content = {
				"from" : from.toISOString(),
				"nodeURNs" : nodeURNs,
				"to" : to.toISOString(),
				"userData" : userData
			};

			$.ajax({
				url			:	wisebedBaseUrl + "/rest/2.3/" + testbedId + "/reservations/create",
				type		:	"POST",
				data		:	JSON.stringify(content, null, '  '),
				contentType	:	"application/json; charset=utf-8",
				dataType	:	"json",
				success		: 	callbackDone,
				error		: 	callbackError,
				xhrFields: { withCredentials: true }
			});

		};

		this.equals = function(res1, res2) {

			function subsetOf(set1, set2, compare) {
				for (var i=0; i<set1.length; i++) {
					for (var j=0; j<set2.length; j++) {
						if (!compare(set1[i], set2[j])) {
							return false;
						}
					}
				}
				return true;
			}

			function setEquals(set1, set2, compare) {

				if (set1.length != set2.length) {
					return false;
				}

				return subsetOf(set1, set2, compare) && subsetOf(set2, set1, compare);
			}

			return setEquals(res1.data, res2.data, function(dataElem1, dataElem2) {
				return  dataElem1.secretReservationKey == dataElem2.secretReservationKey &&
						dataElem1.urnPrefix            == dataElem2.urnPrefix;
			});
		}
	};

	this.experiments = new function() {

		this.getUrl = function(testbedId, reservation, callbackDone, callbackError) {

			var secretReservationKeys = {
				reservations : []
			};

			$(reservation.data).each(function(index, elem) {
				secretReservationKeys.reservations[index] = {
					urnPrefix : elem.urnPrefix,
					secretReservationKey : elem.secretReservationKey
				}
			});

			var succ = function(data, textStatus, jqXHR) {
				// Headers are empty in Cross-Site-Environment
				// callbackDone(jqXHR.getResponseHeader("Location"))
				callbackDone(jqXHR.responseText);
			}

			$.ajax({
				url			:	wisebedBaseUrl + "/rest/2.3/" + testbedId + "/experiments",
				type		:	"POST",
				data		:	JSON.stringify(secretReservationKeys, null, '  '),
				contentType	:	"application/json; charset=utf-8",
				success		: 	succ,
				error		: 	callbackError,
				xhrFields: { withCredentials: true }
			});
		};

		this.resetNodes = function(testbedId, experimentId, nodeUrns, callbackDone, callbackError) {

			$.ajax({
				url			:	wisebedBaseUrl + "/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/resetNodes",
				type		:	"POST",
				data		:	JSON.stringify({nodeUrns:nodeUrns}, null, '  '),
				contentType	:	"application/json; charset=utf-8",
				dataType	:	"json",
				success		: 	function(data) {callbackDone(data.operationStatus);},
				error		: 	callbackError,
				xhrFields: { withCredentials: true }
			});
		};

		this.flashNodes = function(testbedId, experimentId, data, callbackDone, callbackProgress, callbackError) {

			function getAllNodeUrnsFromRequestData(data) {

				var allNodeUrns = [];

				for (var i=0; i<data.configurations.length; i++) {
					var configuration = data.configurations[i];
					for (var j=0; j<configuration.nodeUrns.length; j++) {
						allNodeUrns.push(configuration.nodeUrns[j]);
					}
				}

				allNodeUrns.sort();
				return allNodeUrns;
			}

			var allNodeUrns = getAllNodeUrnsFromRequestData(data);

			var requestSuccessCallback = function(d, textStatus, jqXHR){

				// Headers are empty in Cross-Site-Environment
				//var flashRequestStatusURL = jqXHR.getResponseHeader("Location");
				var flashRequestStatusURL = jqXHR.responseText;

				var schedule = setInterval(function() {

					var onProgressRequestSuccess = function(data) {

						//var data = JSON.parse(d);
						var completeNodeUrns = [];

						$.each(data.operationStatus, function(nodeUrn, nodeStatus) {
							if (nodeStatus.status != 'RUNNING') {
								completeNodeUrns.push(nodeUrn);
							}
						});
						completeNodeUrns.sort();

						if (allNodeUrns.compareArrays(completeNodeUrns)) {
							callbackDone(data.operationStatus);
							clearInterval(schedule);
						} else {
							callbackProgress(data.operationStatus);
						}
					};

					var onProgressRequestError = function(jqXHR, textStatus, errorThrown) {
						clearInterval(schedule);
						callbackError(jqXHR, textStatus, errorThrown);
					};

					$.ajax({
						url         : flashRequestStatusURL,
						type        : "GET",
						success     : onProgressRequestSuccess,
						error       : onProgressRequestError,
						xhrFields: { withCredentials: true }
					});

				}, 2 * 1000);
			};

			$.ajax({
				url         : wisebedBaseUrl + "/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/flash",
				type        : "POST",
				data        : JSON.stringify(data, null, '  '),
				contentType : "application/json; charset=utf-8",
				success     : requestSuccessCallback,
				error       : callbackError,
				xhrFields: { withCredentials: true }
			});
		};
	};

	this.getNodeUrnArray = function(testbedId, experimentId, callbackDone, callbackError) {

		this.getWiseML(
				testbedId,
				experimentId,
				function(wiseML, textStatus, jqXHR) {
					callbackDone(this.getNodeUrnArrayFromWiseML(wiseML), textStatus, jqXHR);
				},
				callbackError
		);
	};

	this.getWiseML = function(testbedId, experimentId, callbackDone, callbackError, jsonOrXml) {

		$.ajax({
			url      : (experimentId ?
						wisebedBaseUrl + "/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/network" :
						wisebedBaseUrl + "/rest/2.3/" + testbedId + "/experiments/network"),
			context  : document.body,
			success  : callbackDone,
			error    : callbackError,
			dataType : (!jsonOrXml ? "json" : jsonOrXml),
			xhrFields: { withCredentials: true }
		});
	};

	this.getWiseMLAsJSON = function(testbedId, experimentId, callbackDone, callbackError) {
		this.getWiseML(testbedId, experimentId, callbackDone, callbackError, "json");
	};

	this.getWiseMLAsXML = function(testbedId, experimentId, callbackDone, callbackError) {
		this.getWiseML(testbedId, experimentId, callbackDone, callbackError, "xml");
	};

	this.getNodeUrnArrayFromWiseML = function(wiseML) {
		var nodeUrns = new Array();
		var nodes = wiseML.setup.node;
		for (var i=0; i<nodes.length; i++) {
			nodeUrns[i] = nodes[i].id;
		}
		return nodeUrns;
	};

	this.getTestbeds = function(callbackDone, callbackError) {
		$.ajax({
			url: wisebedBaseUrl + "/rest/2.3/testbeds",
			success: callbackDone,
			error: callbackError,
			context: document.body,
			dataType: "json",
			xhrFields: { withCredentials: true }
		});
	};

	this.hasSecretAuthenticationKeyCookie = function(testbedId) {
		return $.cookie('wisebed-secret-authentication-key-' + testbedId) != null;
	};

	this.isLoggedIn = function(testbedId, callbackDone, callbackError) {
		$.ajax({
			url      : wisebedBaseUrl + "/rest/2.3/" + testbedId + "/isLoggedIn",
			context  : document.body,
			dataType : "json",
			success  : function() {callbackDone(true);},
			error    : function(jqXHR, textStatus, errorThrown) {
				if (jqXHR.status == 403) {
					callbackDone(false);
				} else {
					callbackError(jqXHR, textStatus, errorThrown);
				}
			},
			xhrFields: { withCredentials: true }
		});
	};

	this.login = function(testbedId, credentials, callbackDone, callbackError) {
		$.ajax({
			url			: wisebedBaseUrl + "/rest/2.3/" + testbedId + "/login",
			type		: "POST",
			data		: JSON.stringify(credentials, null, '  '),
			contentType	: "application/json; charset=utf-8",
			dataType	: "json",
			error		: callbackError,
			success		: callbackDone,
			xhrFields: { withCredentials: true }
		});
	};

	this.logout = function(testbedId, callbackDone, callbackError) {
		$.ajax({
			url      : wisebedBaseUrl + "/rest/2.3/" + testbedId + "/logout",
			success  : callbackDone,
			error    : callbackError,
			xhrFields: { withCredentials: true }
		});
	};

};