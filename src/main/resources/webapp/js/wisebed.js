var Wisebed = new function() {

	this.getAvailableNodeUrns = function(callbackDone, callbackError, experimentId) {

		this.getNetwork(
				function(data, textStatus, jqXHR) {
					callbackDone(Wisebed.getNodeList(data));
				},
				function(jqXHR, textStatus, errorThrown) {callbackError(jqXHR, textStatus, errorThrown);},
				experimentId
		);
	};

	this.getNetwork = function(callbackDone, callbackError, experimentId) {

		var request = $.ajax({
			url: (experimentId !== undefined ?
					"/rest/2.3/experiments/" + experimentId + "/network" :
					"/rest/2.3/experiments/network"),
			context: document.body
		});

		request.done(function(data, textStatus, jqXHR) {
			callbackDone(data, textStatus, jqXHR);
		});

		if (callbackError) {
			request.fail(function(jqXHR, textStatus, errorThrown) {
				callbackError(jqXHR, textStatus, errorThrown);
			});
		}
	};

	this.getNodeList = function(network) {
		var nodeUrns = new Array();
		var nodes = network.setup.node;
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

	this.deleteSecretAuthenticationKeyCookie = function() {
		$.cookie('wisebed-secret-authentication-key', null);
	};

	this.hasSecretAuthenticationKeyCookie = function() {
		return $.cookie('wisebed-secret-authentication-key') != null;
	};
};