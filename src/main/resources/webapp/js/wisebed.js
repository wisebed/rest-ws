
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

	this.reservations = new function() {

		this.getPersonal = function(testbedId, from, to, callbackDone, callbackError) {
			var queryUrl = "/rest/2.3/" + testbedId + "/reservations?userOnly=true" +
					(from ? ("&from=" + from.toISOString()) : "") +
					(to ? ("&to="+to.toISOString()) : "");
			$.ajax({
				url: queryUrl,
				success: callbackDone,
				error: callbackError,
				context: document.body,
				dataType: "json"
			});
		};

		this.getPublic = function(testbedId, from, to, callbackDone, callbackError) {
			var queryUrl = "/rest/2.3/" + testbedId + "/reservations?" +
					(from ? ("from=" + from.toISOString() + "&") : "") +
					(to ? ("to="+to.toISOString() + "&") : "");
			$.ajax({
				url: queryUrl,
				success: callbackDone,
				error: callbackError,
				context: document.body,
				dataType: "json"
			});
		};

		this.make = function(testbedId, from, to, userData, nodeURNs, callbackDone, callbackError) {

			// Generate JSON
			var content = {
				"from" : from.toISOString(),
				"nodeURNs" : nodeURNs,
				"to" : to.toISOString(),
				"userData" : userData
			};

			var queryUrl = "/rest/2.3/" + testbedId + "/reservations";
			$.ajax({
				type: "POST",
				url: queryUrl,
				success: callbackDone,
				error: callbackError,
				data: content,
				dataType: "json"
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

			$.ajax({
				url			:	"/rest/2.3/" + testbedId + "/experiments",
				type		:	"POST",
				data		:	JSON.stringify(secretReservationKeys, null, '  '),
				contentType	:	"application/json; charset=utf-8",
				dataType	:	"json",
				success		: 	function(data, textStatus, jqXHR) {callbackDone(jqXHR.getResponseHeader("Location"))},
				error		: 	callbackError
			});
		};

		this.resetNodes = function(testbedId, experimentId, nodeUrns, callbackDone, callbackError) {

			$.ajax({
				url			:	"/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/resetNodes",
				type		:	"POST",
				data		:	JSON.stringify({nodeUrns:nodeUrns}, null, '  '),
				contentType	:	"application/json; charset=utf-8",
				dataType	:	"json",
				success		: 	function(data) {callbackDone(data.operationStatus);},
				error		: 	callbackError
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

			var requestSuccessCallback = function(data, textStatus, jqXHR){

				var flashRequestStatusURL = jqXHR.getResponseHeader("Location");

				var schedule = setInterval(function() {

					var onProgressRequestSuccess = function(data) {

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
						dataType    : "json",
						success     : onProgressRequestSuccess,
						error       : onProgressRequestError
					});

				}, 2 * 1000);
			};

			$.ajax({
				url         : "/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/flash",
				type        : "POST",
				data        : JSON.stringify(data, null, '  '),
				contentType : "application/json; charset=utf-8",
				dataType    : "json",
				success     : requestSuccessCallback,
				error       : callbackError
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
					    "/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/network" :
					    "/rest/2.3/" + testbedId + "/experiments/network"),
			context  : document.body,
			success  : callbackDone,
			error    : callbackError,
			dataType : (!jsonOrXml ? "json" : jsonOrXml)
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
			url: "/rest/2.3/testbeds",
			success: callbackDone,
			error: callbackError,
			context: document.body,
			dataType: "json"
		});
	};

	this.deleteSecretAuthenticationKeyCookie = function(testbedId) {
		$.cookie('wisebed-secret-authentication-key-' + testbedId, null);
	};

	this.hasSecretAuthenticationKeyCookie = function(testbedId) {
		return $.cookie('wisebed-secret-authentication-key-' + testbedId) != null;
	};

	this.isLoggedIn = function(testbedId, callbackDone, callbackError) {
		$.ajax({
			url      : "/rest/2.3/" + testbedId + "/isLoggedIn",
			context  : document.body,
			dataType : "json",
			success  : function() {callbackDone(true);},
			error    : function(jqXHR, textStatus, errorThrown) {
				if (jqXHR.status == 403) {
					callbackDone(false);
				} else {
					callbackError(jqXHR, textStatus, errorThrown);
				}
			}
		});
	};

	this.login = function(testbedId, credentials, callbackDone, callbackError) {
		$.ajax({
			url			: "/rest/2.3/" + testbedId + "/login",
			type		: "POST",
			data		: JSON.stringify(credentials, null, '  '),
			contentType	: "application/json; charset=utf-8",
			dataType	: "json",
			error		: callbackError,
			success		: callbackDone
		});
	};

};