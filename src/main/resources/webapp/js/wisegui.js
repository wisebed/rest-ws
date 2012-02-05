var WiseGui = new function() {

	this.showAlert = function(message, severity) {
		$(window).trigger('wisegui-notification',
				{
					type     : 'alert',
					severity : severity,
					message  : message
				}
		);
	};
	this.showWarningAlert = function(message) { this.showAlert(message, 'warning'); };
	this.showErrorAlert = function(message) { this.showAlert(message, 'error'); };
	this.showSuccessAlert = function(message) { this.showAlert(message, 'success'); };
	this.showInfoAlert = function(message) { this.showAlert(message, 'info'); };

	this.showBlockAlert = function(message, actions, severity) {
		$(window).trigger('wisegui-notification',
				{
					type     : 'block-alert',
					severity : severity,
					message  : message,
					actions  : actions
				}
		);
	};
	this.showWarningBlockAlert = function(message, actions) { this.showBlockAlert(message, actions, 'warning'); };
	this.showErrorBlockAlert = function(message, actions) { this.showBlockAlert(message, actions, 'error'); };
	this.showSuccessBlockAlert = function(message, actions) { this.showBlockAlert(message, actions, 'success'); };
	this.showInfoBlockAlert = function(message, actions) { this.showBlockAlert(message, actions, 'info'); };
};

/**
 * #################################################################
 * WiseGuiLoginDialog
 * #################################################################
 */
var WiseGuiLoginDialog = new function() {

	var loginFormRows = {};

	this.createLoginDialogIfNotExisting = function(testbedId, callbackError) {

		if ($('#WisebedLoginDialog-'+testbedId).length == 0) {

			Wisebed.getTestbeds(function(testbeds){

				function addRowToLoginForm (testbedId, tbody, urnPrefix, username, password) {

					var tr = $('<tr/>');

					if (!loginFormRows[testbedId]) {loginFormRows[testbedId] = [];}

					var i = loginFormRows[testbedId].length;

					var inputUrnPrefix = $('<input type="text" id="urnprefix'+i+'" name="urnprefix'+i+'" value="'+urnPrefix+'" readonly/>');
					var inputUsername = $('<input type="text" id="username'+i+'" name="username'+i+'" value="'+username+'"/>');
					var inputPassword = $('<input type="password" id="password'+i+'" name="password'+i+'" value="'+password+'"/>');

					loginFormRows[testbedId][loginFormRows[testbedId].length] = {
						"tr" : tr,
						"inputUrnPrefix" : inputUrnPrefix[0],
						"inputUsername" : inputUsername[0],
						"inputPassword" : inputPassword[0]
					};

					var tdUrnPrefix = $('<td/>');
					var tdUsername = $('<td/>');
					var tdPassword = $('<td/>');

					tdUrnPrefix.append(inputUrnPrefix);
					tdUsername.append(inputUsername);
					tdPassword.append(inputPassword);

					tr.append($('<td>'+(loginFormRows[testbedId].length)+'</td>'));
					tr.append(tdUrnPrefix);
					tr.append(tdUsername);
					tr.append(tdPassword);

					tbody.append(tr);
				}

				var dialog = $('<div id="WisebedLoginDialog-'+testbedId+'" class="modal hide"></div>');

				var dialogHeader = $('<div class="modal-header"><h3>Login to Testbed ' + testbedId + '</h3></div>');

				var dialogBody = $('<div class="modal-body WiseGuiLoginDialog"/>'
						+ '		<form id="WisebedLoginDialogForm-'+testbedId+'">'
						+ '		<table id="WisebedLoginDialogFormTable-'+testbedId+'">'
						+ '			<thead>'
						+ '				<tr>'
						+ '					<th>Testbed</th>'
						+ '					<th>URN Prefix</th>'
						+ '					<th>Username</th>'
						+ '					<th>Password</th>'
						+ '				</tr>'
						+ '			</thead>'
						+ '			<tbody>'
						+ '			</tbody>'
						+ '		</table>'
						+ '		</form>'
						+ '	</div>');

				var cancelButton = $('<a class="btn secondary">Cancel</a>');
				var okButton = $('<a class="btn primary">OK</a>');

				cancelButton.bind('click', {testbedId:testbedId}, function(event) {
					WiseGuiLoginDialog.hide(event.data.testbedId);
				});
				okButton.bind('click', {testbedId:testbedId}, function(event) {
					WiseGuiLoginDialog.login(event.data.testbedId);
				});

				var dialogFooter = $('<div class="modal-footer"/>');
				dialogFooter.append(cancelButton, okButton);
				dialog.append(dialogHeader, dialogBody, dialogFooter);

				$('#WisebedContainer').append(dialog);

				var loginFormTableBody = $('#WisebedLoginDialogFormTable-'+testbedId+' tbody');
				var urnPrefixes = testbeds.testbedMap[testbedId].urnPrefixes;

				for (var i=0; i<urnPrefixes.length; i++) {
					addRowToLoginForm(testbedId, loginFormTableBody, urnPrefixes[i], "", "");
				}

			}, callbackError);
		}
	};

	this.show = function(testbedId) {
		$('#WisebedLoginDialog-'+testbedId).show();
	};

	this.hide = function(testbedId) {
		$('#WisebedLoginDialog-'+testbedId).hide();
	};

	function readLoginDataFromForm(testbedId) {

		loginData = {
			authenticationData : []
		};

		for (var i=0; i<loginFormRows[testbedId].length; i++) {

			loginData.authenticationData[i] = new Object();
			loginData.authenticationData[i].urnPrefix = loginFormRows[testbedId][i].inputUrnPrefix.value;
			loginData.authenticationData[i].username = loginFormRows[testbedId][i].inputUsername.value;
			loginData.authenticationData[i].password = loginFormRows[testbedId][i].inputPassword.value;
		}

		return loginData;
	}

	this.login = function(testbedId) {
		$.ajax({
			url			:	"/rest/2.3/" + testbedId + "/login",
			type		:	"POST",
			data		:	JSON.stringify(readLoginDataFromForm(testbedId), null, '  '),
			contentType	:	"application/json; charset=utf-8",
			dataType	:	"json",
			success		: 	function(data, textStatus, jqXHR) {
								WiseGuiLoginDialog.hide(testbedId);
								$(window).trigger('hashchange');
							},
			error		: 	function(jqXHR, textStatus, errorThrown){
								console.log(jqXHR);
								console.log(textStatus);
								console.log(errorThrown);
								alert("Error logging in: " + jqXHR.responseText);
							}
		});
	};
};

/**
 * #################################################################
 * WiseGuiNodeTable
 * #################################################################
 */
var WiseGuiNodeTable = function (wiseML, parent, showCheckboxes, showFilter) {
	this.checkboxes = [];
	this.wiseML = wiseML;
	this.showCheckboxes = showCheckboxes;
	this.lastWorkingFilterExpr = null;

	this.html = $("<div></div>");
	parent.append(this.html);
	this.filter = null;
	this.table = null;

	if(showFilter) this.generateHeader();
	this.generateTable(null);
};

WiseGuiNodeTable.prototype.generateHeader = function (f) {
	that = this;

	// Filter
	this.filter = $("<p></p>");

	filter_input = $('<input type"text" style="width:100%;padding-left:0px;padding-right:0px;">');
	// Key up event if enter is pressed
	filter_input.keyup(function(event) {
		that.generateTable(filter_input.val());
	});

	this.filter.append(filter_input);
	this.html.append(this.filter);
}

h = 0;
WiseGuiNodeTable.prototype.generateTable = function (f) {

	// TODO: use buildTable(...)

	var that = this;
	var nodes = this.wiseML.setup.node;

	if(f != null && f.length > 0) {
		// Filter
		var errorOccured = false;
		nodes = $(nodes).filter(function(index) {
			e = this;
			ret = true;
			try {
				ret = eval(f);
			} catch (ex) {
				errorOccured = true;
				ret = null;
			}

			if(typeof(ret) != "boolean") {
				if(that.lastWorkingFilterExpr != null) {
					ret = eval(that.lastWorkingFilterExpr);
				} else {
					return true;
				}
			}

			return ret;
		});
		if(errorOccured) {
			//alert("Filter expression not valid.");
			return;
		} else {
			this.lastWorkingFilterExpr = f;
		}
	}

	if(this.table != null) {
		this.table.remove();
	}

	this.table = $('<table class="bordered-table zebra-striped"></table>');

	// Generate table header
	var thead = $('<thead></thead>');
	var thead_tr = $('<tr></tr>');
	if(this.showCheckboxes) {
		var thead_th_checkbox = $('<th class="header"></th>');
		var thead_th_checkbox_checkbox = $('<input type="checkbox"/>');

		thead_th_checkbox_checkbox.click(function() {
			var checked = $(this).is(':checked');
			if(that.table != null) {
				var inputs = that.table.find("input");
				inputs.each(function() {
					$(this).attr('checked', checked);
				});
			}
		});
		thead_th_checkbox.append(thead_th_checkbox_checkbox);

		thead_tr.append(thead_th_checkbox);
	}

	var thead_th_node_urn = $('<th class="header">Node URN</th>');
	var thead_th_type = $('<th class="header">Type</th>');
	var thead_th_position = $('<th class="header">Position</th>');
	var thead_th_sensors = $('<th class="header">Sensors</th>');
	thead_tr.append(thead_th_node_urn);
	thead_tr.append(thead_th_type);
	thead_tr.append(thead_th_position);
	thead_tr.append(thead_th_sensors);
	thead.append(thead_tr);
	this.table.append(thead);

	// Generate table body
	var tbody = $('<tbody></tbody>');
	this.table.append(tbody);

	// Iterate all nodes and add the to the table
	this.checkboxes = [];
	for(i = 0; i < nodes.length; i++) {
		var n = nodes[i];

		var cap = [];
		for(j = 0; j < n.capability.length; j++) {
			parts = explode(":", n.capability[j].name);
			cap[j] = parts[parts.length-1];
		}

		if(this.showCheckboxes) {
			var checkbox = $('<input type="checkbox" name="' + n.id + '"/>');
			this.checkboxes[i] = checkbox;
			var td_checkbox = $('<td></td>');
			td_checkbox.append(checkbox);
		}

		var td_id = $('<td>' + n.id + '</td>')
		var td_type = $('<td>' + n.nodeType + '</td>')
		var td_position = $('<td>(' + n.position.x + ',' + n.position.x + ',' + n.position.x + ')</td>')
		var td_sensors = $('<td>' + implode(",", cap) + '</td>')

		var tr = $("<tr></tr>");
		tr.append(td_checkbox);
		tr.append(td_id);
		tr.append(td_type);
		tr.append(td_position);
		tr.append(td_sensors);

		tbody.append(tr);
	}

	this.html.append(this.table);

	if(this.showCheckboxes) {
		$(this.table).tablesorter({headers:{0:{sorter:false}}});
	} else {
		$(this.table).tablesorter();
	}
};

WiseGuiNodeTable.prototype.getSelectedNodes = function () {
	var selected = [];
	if(this.table != null) {
		this.table.find("input:checked").each(function() {
			var name = $(this).attr('name');

			// Ignore the checkbox from the header, which doesn't have any name
			if(typeof(name) != "undefined") {
				selected.push(name);
			}
		});
	}
	return selected;
};

/**
 * #################################################################
 * WiseGuiReservationObserver
 * #################################################################
 */

var WiseGuiReservationObserver = function(testbedId) {
	this.testbedId = testbedId;
	this.lastKnownReservations = [];
	this.isObserving = false;
	this.interval = null;
};

WiseGuiReservationObserver.prototype.fetchReservationsAndProcess = function() {
	var self = this;
	Wisebed.reservations.getPersonal(
			this.testbedId,
			null,
			null,
			function(reservations) {self.processReservationsFetched(reservations.reservations)},
			null
	);
};

WiseGuiReservationObserver.prototype.processReservationsFetched = function(reservations) {

	var newReservations = [];

	for (var i=0; i<reservations.length; i++) {

		var knownReservation = false;

		for (var j=0; j<this.lastKnownReservations.length; j++) {
			if (this.reservationEquals(reservations[i], this.lastKnownReservations[j])) {
				knownReservation = true;
				break;
			}
		}

		if (!knownReservation) {
			newReservations.push(reservations[i]);
		}
	}

	for (var k=0; k<newReservations.length; k++) {

		$(window).trigger('wisegui-reservation-added', newReservations[k]);

		// schedule events for reservation started and ended in order to e.g. display user notifications
		var nowInMillis = new Date().valueOf();
		if (nowInMillis < newReservations[k].from) {
			setTimeout(
					function() {$(window).trigger('wisegui-reservation-started', newReservations[k]);},
					(newReservations[k].from - nowInMillis)
			);
		}

		if (nowInMillis < newReservations[k].to) {
			setTimeout(
					function() {$(window).trigger('wisegui-reservation-ended', newReservations[k]);},
					(newReservations[k].to - nowInMillis)
			)
		}

		this.lastKnownReservations.push(newReservations[k]);
	}
};

WiseGuiReservationObserver.prototype.reservationEquals = function(res1, res2) {

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
};

WiseGuiReservationObserver.prototype.startObserving = function() {
	this.isObserving = true;
	var self = this;
	this.interval = window.setInterval(function() {self.fetchReservationsAndProcess()}, 60 * 1000);
	this.fetchReservationsAndProcess();
};

WiseGuiReservationObserver.prototype.stopObserving = function() {
	this.isObserving = false;
	window.clearInterval(this.interval);
};

/**
 * #################################################################
 * WiseGuiNotificationsViewer
 * #################################################################
 *
 * Consumes wisegui events of type 'wisegui-notification' and displays them in a notification area.
 * A 'wisegui-notification' event has to carry data of the following type:
 *
 * {
 *  type     : "alert"|"block-alert"
 *  severity : "warning"|"error"|"success"|"info"
 *  message  : "Oh snap! Change this and that and try again."
 *  actions  : an array of buttons (only for block-alerts)
 * }
 *
 */

var WiseGuiNotificationsViewer = function() {

	this.view = null;
	this.buildView();

	var self = this;
	$(window).bind('wisegui-notification', function(e, data) {
		self.showNotification(data);
	});
};

WiseGuiNotificationsViewer.prototype.showNotification = function(notification) {
	if (notification.type == 'alert') {
		this.showAlert(notification);
	} else if (notification.type == 'block-alert') {
		this.showBlockAlert(notification);
	}
};

WiseGuiNotificationsViewer.prototype.showAlert = function(alert) {
	var alertDiv = $('<div class="alert-message '+alert.severity+'">'
			+ '<a class="close" href="#">&times;</a>'
			+ '<p>'+alert.message+'</p>'
			+ '</div>');
	this.view.append(alertDiv);
	alertDiv.alert();
};

WiseGuiNotificationsViewer.prototype.showBlockAlert = function(alert) {
	var blockAlertDiv = $('<div class="alert-message block-message '+alert.severity+'">'
			+ '	<a class="close" href="#">&times;</a>'
			+ '	<p>'+alert.message+'</p>'
			+ '	<div class="alert-actions">'
			+ '	</div>'
			+ '</div>');
	var actionsDiv = blockAlertDiv.find('.alert-actions');
	for (var i=0; i<alert.actions.length; i++) {
		actionsDiv.append(alert.actions[i]);
		actionsDiv.append(' ');
	}
	this.view.append(blockAlertDiv);
	blockAlertDiv.alert();
};

WiseGuiNotificationsViewer.prototype.buildView = function() {
	this.view = $('<div id="WiseGuiNotificationsDiv"></div>');
};

/**
 * #################################################################
 * WiseGuiExperimentDropDown
 * #################################################################
 *
 * Consumes wisegui events of type 'wisegui-reservation-ended', 'wisegui-reservation-started', 'wisegui-reservation-added'.
 *
 */

var WiseGuiExperimentDropDown = function(testbedId) {

	this.pastExperimentsDropdown = null;
	this.currentExperimentsDropDown = null;

	var self = this;
	$(window).bind('wisegui-reservation-started', function(e, data) {self.onExperimentStartedEvent(data)} );
	$(window).bind('wisegui-reservation-ended',   function(e, data) {self.onExperimentEndedEvent(data)}   );
	$(window).bind('wisegui-reservation-added',  function(e, data) {self.onExperimentEndedEvent(data)}   );

	this.buildPastExperimentsDropDown();
	this.buildCurrentExperimentsDropDown();
};

WiseGuiExperimentDropDown.prototype.onReservationAddedEvent = function(wiseguiEvent) {
	console.log('TODO implement handling of wiseguiEvent "reservation-added": ' + JSON.stringify(wiseguiEvent));
}

WiseGuiExperimentDropDown.prototype.onExperimentEndedEvent = function(wiseguiEvent) {
	console.log('TODO implement handling of wiseguiEvent "experiment-ended": ' + JSON.stringify(wiseguiEvent));
};

WiseGuiExperimentDropDown.prototype.onExperimentStartedEvent = function(wiseguiEvent) {
	console.log('TODO implement handling of wiseguiEvent "experiment-started": ' + JSON.stringify(wiseguiEvent));
};

WiseGuiExperimentDropDown.prototype.buildPastExperimentsDropDown = function() {
	this.pastExperimentsDropDown = $('<li class="dropdown">'
			+ '	<a href="#" class="dropdown-toggle">Past Experiments</a>'
			+ '	<ul class="dropdown-menu">'
			+ '		<li><a href="#">Secondary link</a></li>'
			+ '		<li><a href="#">Something else here</a></li>'
			+ '		<li><a href="#">Another link</a></li>'
			+ '	</ul>'
			+ '</li>');
};

WiseGuiExperimentDropDown.prototype.buildCurrentExperimentsDropDown = function() {
	this.pastExperimentsDropDown = $('<li class="dropdown">'
			+ '	<a href="#" class="dropdown-toggle">Current Experiments</a>'
			+ '	<ul class="dropdown-menu">'
			+ '		<li><a href="#">Secondary link</a></li>'
			+ '		<li><a href="#">Something else here</a></li>'
			+ '		<li><a href="#">Another link</a></li>'
			+ '	</ul>'
			+ '</li>');
};


/**
 * #################################################################
 * WiseGuiNodeSelectionDialog
 * #################################################################
 */

var WiseGuiNodeSelectionDialog = function(testbedId, experimentId, headerHtml, bodyHtml) {

	this.testbedId = testbedId;
	this.experimentId = experimentId;
	this.table = null;

	this.dialogDivId = 'WiseGuiNodeSelectionDialog-' + Math.random();

	this.dialogDiv = $('<div id="'+this.dialogDivId+'" class="modal hide WiseGuiNodeSelectionDialog">'
			+ '	<div class="modal-header">'
			+ '		<h3>' + headerHtml + '</h3>'
			+ '	</div>'
			+ '	<div class="modal-body">'
			+ '		<p>' + bodyHtml + '</p>'
			+ '		<img class="ajax-loader" src="img/ajax-loader-big.gif" width="32" height="32"/>'
			+ '	</div>'
			+ ' <div class="modal-footer">'
			+ '		<a class="btn secondary">Cancel</a>'
			+ '		<a class="btn primary">OK</a>'
			+ '	</div>'
			+ '</div>');
};

WiseGuiNodeSelectionDialog.prototype.show = function(callbackOK, callbackCancel) {

	$(document.body).append(this.dialogDiv);
	var self = this;

	function showDialogInternal(wiseML) {

		self.dialogDiv.show();

		self.dialogDiv.find('.ajax-loader').attr('hidden', 'true');
		self.table = new WiseGuiNodeTable(wiseML, self.dialogDiv.find('.modal-body').first(), true);

		self.dialogDiv.find('.modal-footer .secondary').first().bind(
				'click',
				{dialog : self},
				function(event) {
					event.data.dialog.dialogDiv.hide();
					event.data.dialog.dialogDiv.remove();
					if (callbackCancel) {
						callbackCancel();
					}
				}
		);

		self.dialogDiv.find('.modal-footer .primary').first().bind(
				'click',
				self,
				function(event) {
					event.data.dialogDiv.hide();
					event.data.dialogDiv.remove();
					callbackOK(event.data.table.getSelectedNodes());
				}
		);
	}

	Wisebed.getWiseMLAsJSON(this.testbedId, this.experimentId, showDialogInternal,
			function(jqXHR, textStatus, errorThrown) {
				console.log('TODO handle error in WiseGuiNodeSelectionDialog');
			}
	);
};

/**
 * #################################################################
 * WiseGuiExperimentationView
 * #################################################################
 */

var WiseGuiExperimentationView = function(testbedId, experimentId) {

	this.testbedId = testbedId;
	this.experimentId = experimentId;

	this.experimentationDivId    = 'WisebedExperimentationDiv-'+testbedId+'-'+experimentId;
	this.tabsControlsDivId       = this.experimentationDivId+'-tabs-controls';
	this.tabsOutputsDivId        = this.experimentationDivId+'-tabs-outputs';
	this.outputsDivId            = this.experimentationDivId+'-outputs';
	this.notificationsDivId      = this.experimentationDivId+'-notifications';
	this.outputsTextAreaId       = this.experimentationDivId+'-outputs-textarea';
	this.notificationsTextAreaId = this.experimentationDivId+'-notifications-textarea';
	this.sendDivId               = this.experimentationDivId+'-send';
	this.flashDivId              = this.experimentationDivId+'-flash';
	this.resetDivId              = this.experimentationDivId+'-reset';
	this.scriptingDivId          = this.experimentationDivId+'-scripting';

	this.view = $('<div id="'+this.experimentationDivId+'"/>');

	this.resetSelectedNodeUrns = null;
	this.socket = null;

	this.buildView();
	this.connectToExperiment();
};

WiseGuiExperimentationView.prototype.onWebSocketMessageEvent = function(event) {

	var message = JSON.parse(event.data);

	if (!message.type) {
		console.log('Received message with unknown content: ' + event.data);
		return;
	}

	if (message.type == 'upstream') {

		this.outputsTextArea.append(
				message.timestamp           + " | " +
				message.sourceNodeUrn       + " | " +
				atob(message.payloadBase64) + '\n'
		);

		this.outputsTextArea.scrollTop(this.outputsTextArea[0].scrollHeight);

	} else if (message.type == 'notification') {

		this.notificationsTextArea.append(
				message.timestamp + " | " +
				message.message   + '\n'
		);
	}
};

WiseGuiExperimentationView.prototype.onWebSocketOpen = function(event) {

	this.outputsTextArea.attr('disabled', false);
	this.notificationsTextArea.attr('disabled', false);
};

WiseGuiExperimentationView.prototype.onWebSocketClose = function(event) {

	this.outputsTextArea.attr('disabled', true);
	this.notificationsTextArea.attr('disabled', true);
};

WiseGuiExperimentationView.prototype.connectToExperiment = function() {

	if (!window.WebSocket) {
		window.WebSocket = window.MozWebSocket;
	}

	if (window.WebSocket) {

		var self = this;

		this.socket = new WebSocket('ws://localhost:8880/ws/experiments/'+this.experimentId);
		this.socket.onmessage = function(event) {self.onWebSocketMessageEvent(event)};
		this.socket.onopen = function(event) {self.onWebSocketOpen(event)};
		this.socket.onclose = function(event) {self.onWebSocketClose(event)};

	} else {
		alert("Your browser does not support Web Sockets.");
	}

	/*function send(message) {
		if (!window.WebSocket) { return; }
		if (socket.readyState == WebSocket.OPEN) {
			socket.send(message);
		} else {
			alert("The socket is not open.");
		}
	}

	sendMessagesDiv.find('form').first().submit(function(event){
		var message = {
			targetNodeUrn : event.target.elements.nodeUrn.value,
			payloadBase64 : btoa(event.target.elements.message.value)
		};
		socket.send(JSON.stringify(message));
	});*/

};

WiseGuiExperimentationView.prototype.updateResetSelectNodeUrns = function(selectedNodeUrns) {
	this.resetSelectedNodeUrns = selectedNodeUrns;
	if (selectedNodeUrns.length > 0) {
		this.setResetButtonDisabled(false);
	}
	var selectNodeUrnsDiv = this.view.find('.selectedNodeUrnsDiv').first();
	selectNodeUrnsDiv.empty();
	selectNodeUrnsDiv.append(selectedNodeUrns.join(","));
};

WiseGuiExperimentationView.prototype.showResetNodeSelectionDialog = function() {

	var self = this;
	Wisebed.getWiseMLAsJSON(
			this.testbedId,
			this.experimentId,
			function(wiseML) {

				var selectionDialog = new WiseGuiNodeSelectionDialog(
						self.testbedId,
						self.experimentId,
						'Reset Nodes',
						'Please select the nodes you want to reset.'
				);

				selectionDialog.show(function(selectedNodeUrns) {
						self.updateResetSelectNodeUrns(selectedNodeUrns);

				});

			}, function(jqXHR, textStatus, errorThrown) {
				console.log('TODO handle error in WiseGuiExperimentationView');
			}
	);
};

WiseGuiExperimentationView.prototype.setResetButtonDisabled = function(disabled) {
	this.view.find('#'+this.resetDivId + ' button.resetNodeUrns').first().attr('disabled', disabled);
};

WiseGuiExperimentationView.prototype.executeResetNodes = function() {

	this.setResetButtonDisabled(true);
	var self = this;
	Wisebed.experiments.resetNodes(
			this.testbedId,
			this.experimentId,
			this.resetSelectedNodeUrns,
			function(result) {
				self.setResetButtonDisabled(false);
			},
			function(jqXHR, textStatus, errorThrown) {
				self.setResetButtonDisabled(false);
				alert('TODO handle error in WiseGuiExperimentationView');
			}
	);
};

WiseGuiExperimentationView.prototype.buildView = function() {

	var controlsTabsDiv = $('<div id="'+this.tabsControlsDivId+'">'
			+ '	<ul class="tabs">'
			+ '		<li class="active"><a href="#'+this.sendDivId+'">Send Message</a></li>'
			+ '		<li><a href="#'+this.flashDivId+'">Flash</a></li>'
			+ '		<li><a href="#'+this.resetDivId+'">Reset</a></li>'
			+ '		<li><a href="#'+this.scriptingDivId+'">Scripting</a></li>'
			+ '	</ul>'
			+ '	<div class="tab-content">'
			+ '		<div class="active tab-pane" id="'+this.sendDivId+'"></div>'
			+ '		<div class="tab-pane" id="'+this.flashDivId+'"></div>'
			+ '		<div class="tab-pane" id="'+this.resetDivId+'">'
			+ '			<div class="well" style="padding: 14px 19px;">'
			+ '				<button class="btn selectNodeUrns span4">Select Nodes</button> <button class="btn primary resetNodeUrns span4" disabled>Reset Nodes</button>'
			+ '				<h4>Selected Nodes:</h4> <div class="selectedNodeUrnsDiv" style="overflow:auto;"></div>'
			+ '			</div>'
			+ '		</div>'
			+ '		<div class="tab-pane" id="'+this.scriptingDivId+'"></div>'
			+ '	</div>'
			+ '</div>');

	var outputsTabsDiv = $('<div id="'+this.tabsOutputsDivId+'">'
			+ '	<ul class="tabs">'
			+ '		<li class="active"><a href="#'+this.outputsDivId+'">Node Outputs</a></li>'
			+ '		<li><a href="#'+this.notificationsDivId+'">Backend Notifications</a></li>'
			+ '	</ul>'
			+ '	<div class="tab-content">'
			+ '		<div class="active tab-pane" id="'+this.outputsDivId+'">'
			+ '			<textarea id="'+this.outputsTextAreaId+'" style="width: 100%; height:300px;" readonly disabled></textarea>'
			+ '		</div>'
			+ '		<div class="tab-pane" id="'+this.notificationsDivId+'">'
			+ '			<textarea id="'+this.notificationsTextAreaId+'" style="width: 100%; height:300px;" readonly disabled></textarea>'
			+ '		</div>'
			+ '	</div>'
			+ '</div>');

	var self = this;

	controlsTabsDiv.find('#'+this.resetDivId + ' button.selectNodeUrns').first().bind(
			'click', self, function(e) {e.data.showResetNodeSelectionDialog()}
	);

	controlsTabsDiv.find('#'+this.resetDivId + ' button.resetNodeUrns').first().bind(
			'click', self, function(e) {e.data.executeResetNodes()}
	);

	var controlsDiv = $('<div class="WiseGuiExperimentationViewControlsDiv"><h2>Controls</h2></div>');
	controlsDiv.append(controlsTabsDiv);

	var outputsDiv = $('<div class="WiseGuiExperimentationViewOutputsDiv"><h2>Live Data</h2></div>');
	outputsDiv.append(outputsTabsDiv);

	this.view.append(outputsDiv, controlsDiv);

	this.outputsTextArea = this.view.find('#'+this.outputsTextAreaId);
	this.notificationsTextArea = this.view.find('#'+this.notificationsTextAreaId);

	/*var sendMessagesDiv = $('<div id="'+sendMessagesDivId+'">'
	 + '	<h3>Send Messages</h3>#'
	 + '	Message must consist of comma-separated bytes in base_10 (no prefix), base_2 (prefix 0b) or base_16 (prefix 0x).<br/>'
	 + '	Example: <code>0x0A,0x1B,0b11001001,40,40,0b11001001,0x1F</code>'
	 + '	<form>'
	 + '		<fieldset>'
	 + '			<select name="nodeUrn" id="nodeUrn" class="span4"></select>'
	 + '			<input type="text" id="message"  name="message" value="" class="span8"/>'
	 + '			<input type="submit" value="Send to Node" class="span4"/>'
	 + '		</fieldset>'
	 + '	</form>'
	 + '</div>');*/
};