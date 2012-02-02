var Wisebed = new function() {

	this.getNodeUrnArray = function(testbedId, experimentId, callbackDone, callbackError) {

		this.getWiseML(
				testbedId,
				experimentId,
				function(wiseML, textStatus, jqXHR) {
					callbackDone(Wisebed.getNodeUrnArrayFromWiseML(wiseML), textStatus, jqXHR);
				},
				callbackError
		);
	};

	this.getWiseML = function(testbedId, experimentId, callbackDone, callbackError) {

		$.ajax({
			url      : (experimentId ?
					    "/rest/2.3/" + testbedId + "/experiments/" + experimentId + "/network" :
					    "/rest/2.3/" + testbedId + "/experiments/network"),
			context  : document.body,
			success  : callbackDone,
			error    : callbackError,
			dataType : "json"
		});
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
};