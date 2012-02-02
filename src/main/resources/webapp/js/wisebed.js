var Wisebed = new function() {

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

	this.getReservations = function(testbedId, from, to, callbackDone, callbackError) {
		var queryUrl = "/rest/2.3/" + testbedId + "/reservations?" +
				(from ? ("from=" + from.toISOString() + "&") : "") +
				(to ? ("to="+to.toISOString() + "&") : "");
		console.log(queryUrl);
		$.ajax({
			url: queryUrl,
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
};