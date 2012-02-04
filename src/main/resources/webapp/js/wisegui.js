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

				var dialogBody = $('<div class="modal-body"/>'
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
var WiseGuiNodeTable = function (wiseML, parent, showCheckboxes) {
	this.checkboxes = [];
	this.wiseML = wiseML;
	this.parent = parent;
	this.showCheckboxes = showCheckboxes;
	this.table = null;
	this.html = null;
	this.generateTable(null);
};

WiseGuiNodeTable.prototype.generateTable = function (f) {
	// TODO: use buildTable(...)

	var that = this;
	var nodes = this.wiseML.setup.node;
	this.html = $("<div></div>");

	// Filter
	var filter = $("<p></p>");
	var filter_submit = $('<input type="submit" value="GO" style="width:10%;float:right;">');
	filter_submit.click(
		function () {
			var f = this;
			var expr = $(this).prev().val();
			that.parent.empty();
			that.generateTable(expr);
		}
	);

	var filter_input = $('<input type"text" style="width:87%">');
	filter.append(filter_input);
	filter.append(filter_submit);
	this.html.append(filter);


	if(f != null && f.length > 0) {
		filter_input.attr("value", f);
		// Filter
		nodes = $(nodes).filter(function(index) {
			e = this;
			return (eval(f));
		});
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

	// Add to the parent elemenet and add the sorter
	this.parent.append(this.html);

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
			selected.push($(this).attr('name'));
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
	this.interval = window.setInterval(function() {self.fetchReservationsAndProcess()}, 10 * 1000);
	this.fetchReservationsAndProcess();
};

WiseGuiReservationObserver.prototype.stopObserving = function() {
	this.isObserving = false;
	window.clearInterval(this.interval);
};

/**
 * #################################################################
 * WiseGuiExperimentDropDown
 * #################################################################
 *
 * Consumes wiseguiEvents of type 'wisegui-experiment-ended', 'wisegui-experiment-started', 'wisegui-reservation-added'.
 *
 */

var WiseGuiExperimentDropDown = function(testbedId) {

	this.pastExperimentsDropdown = null;
	this.currentExperimentsDropDown = null;

	var self = this;
	$(window).bind('wisegui-experiment-started', function(e, data) {self.onExperimentStartedEvent(data)} );
	$(window).bind('wisegui-experiment-ended',   function(e, data) {self.onExperimentEndedEvent(data)}   );
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

	this.buildView();
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
			+ '		<div class="active tab-pane" id="'+this.sendDivId+'">sendMessage</div>'
			+ '		<div class="tab-pane" id="'+this.flashDivId+'">flash</div>'
			+ '		<div class="tab-pane" id="'+this.resetDivId+'-reset">reset</div>'
			+ '		<div class="tab-pane" id="'+this.scriptingDivId+'-scripting">Scripting</div>'
			+ '	</div>'
			+ '</div>');

	var outputsTabsDiv = $('<div id="'+this.tabsOutputsDivId+'">'
			+ '	<ul class="tabs">'
			+ '		<li class="active"><a href="#'+this.outputsDivId+'">Node Outputs</a></li>'
			+ '		<li><a href="#'+this.notificationsDivId+'">Backend Notifications</a></li>'
			+ '	</ul>'
			+ '	<div class="tab-content">'
			+ '		<div class="active tab-pane" id="'+this.outputsDivId+'">'
			+ '			<textarea class="'+this.outputsTextAreaId+'" style="width: 100%; height:300px;" readonly disabled></textarea>'
			+ '		</div>'
			+ '		<div class="tab-pane" id="'+this.notificationsDivId+'">'
			+ '			<textarea class="'+this.notificationsTextAreaId+'" style="width: 100%; height:300px;" readonly disabled></textarea>'
			+ '		</div>'
			+ '	</div>'
			+ '</div>');

	var resetDiv = controlsTabsDiv.find('#'+this.resetDivId);

	this.view.append(controlsTabsDiv, outputsTabsDiv);

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