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

	var self = this;
	this.showAjaxError = function(jqXHR, textStatus, errorThrown) {
		var message = $('<h2>Error while loading data!</h2>'
				+ '<h3>jqXHR</h3>'
				+ '<pre>'+JSON.stringify(jqXHR, null, '  ')+'</pre>'
				+ '<h3>textStatus</h3>'
				+ '<pre>'+textStatus+'</pre>'
				+ '<h3>errorThrown</h3>'
				+ '<pre>'+errorThrown+'</pre>');
		self.showErrorBlockAlert(message);
	};
};

var WiseGuiNavigationViewer = function(navigationData) {

	this.navigationData       = navigationData;

	this.view                 = null;
	this.primaryMenu          = null;
	this.secondaryMenu        = null;

	this.loginButton          = null;
	this.loginButtonLi        = null;
	this.logoutButton         = null;
	this.logoutButtonLi       = null;
	this.reservationsButton   = null;
	this.reservationsButtonLi = null;

	this.buildView();
};

WiseGuiNavigationViewer.prototype.buildViewForTestbed = function() {

	// create all buttons and attach them
	this.primaryMenu.append(
			  '<li class="WiseGuiNavBackButton"><a href="#">Back</a></li>'
	);

	this.backButtonLi         = this.primaryMenu.find('li.WiseGuiNavBackButton').first();
	this.backButton           = this.backButtonLi.find('a').first();
	this.experimentDropDown   = new WiseGuiExperimentDropDown(this.navigationData.testbedId);
	this.primaryMenu.append(this.experimentDropDown.view);

	this.secondaryMenu.append(
			  '<li class="WiseGuiNavReservationsButton"><a href="#">My Reservations</a></li>'
			+ '<li class="WiseGuiNavLogoutButton"><a href="#">Logout</a></li>'
			+ '<li class="WiseGuiNavLoginButton"><a href="#">Login</a></li>'
	);

	this.reservationsButtonLi = this.secondaryMenu.find('li.WiseGuiNavReservationsButton').first();
	this.reservationsButton   = this.reservationsButtonLi.find('a').first();
	this.loginButtonLi        = this.secondaryMenu.find('li.WiseGuiNavLoginButton').first();
	this.loginButton          = this.loginButtonLi.find('a').first();
	this.logoutButtonLi       = this.secondaryMenu.find('li.WiseGuiNavLogoutButton').first();
	this.logoutButton         = this.logoutButtonLi.find('a').first();

	// hide all buttons
	this.experimentDropDown.view.hide();
	this.loginButtonLi.hide();
	this.logoutButtonLi.hide();
	this.reservationsButtonLi.hide();

	// bind actions to buttons
	var self = this;

	this.backButton.bind('click', function(e) {
		e.preventDefault();
		if (self.navigationData.experimentId) {
			navigateTo(self.navigationData.testbedId);
		} else {
			navigateTo();
		}
	});

	this.reservationsButton.bind('click', function(e) {
		e.preventDefault();
		showReservationsDialog(self.navigationData.testbedId);
	});

	this.logoutButton.bind('click', function(e) {
		e.preventDefault();
		getLoginDialog(self.navigationData.testbedId).doLogout();
	});

	this.loginButton.bind('click', function(e) {
		e.preventDefault();
		getLoginDialog(self.navigationData.testbedId).show();
	});

	// bind to login and logout events
	$(window).bind('wisegui-logged-in', function(e, data) {
		if (data.testbedId == self.navigationData.testbedId) {
			self.onLoggedInEvent();
		}
	});

	$(window).bind('wisegui-logged-out', function(e, data) {
		if (data.testbedId == self.navigationData.testbedId) {
			self.stopObservationOf();
		}
	});
};

WiseGuiNavigationViewer.prototype.onLoggedInEvent = function() {
	this.loginButtonLi.hide();
	this.logoutButtonLi.show();
	this.reservationsButtonLi.show();
	this.experimentDropDown.view.show();
};

WiseGuiNavigationViewer.prototype.stopObservationOf = function() {
	this.loginButtonLi.show();
	this.logoutButtonLi.hide();
	this.reservationsButtonLi.hide();
	this.experimentDropDown.view.hide();
};

WiseGuiNavigationViewer.prototype.buildViewForTestbedOverview = function() {
	this.primaryMenu.append('<li class="active"><a href="#nav=overview">Testbed Overview</a></li>');
};

WiseGuiNavigationViewer.prototype.buildView = function() {

	this.view = $(
			  '<div class="topbar-wrapper" style="z-index: 5;">'
			+ '	<div class="topbar" data-dropdown="dropdown">'
			+ '		<div class="topbar-inner">'
			+ '			<div class="container">'
			+ '				<ul class="nav"/>'
			+ '				<ul class="nav secondary-nav"/>'
			+ '			</div>'
			+ '		</div>'
			+ '	</div>'
			+ '</div>'
	);

	this.primaryMenu   = this.view.find('ul.nav:not(ul.secondary-nav)').first();
	this.secondaryMenu = this.view.find('ul.secondary-nav').first();

	if (this.navigationData.nav == 'overview') {
		this.buildViewForTestbedOverview();
	} else if (this.navigationData.nav == 'testbed') {
		this.buildViewForTestbed();
	}
};

/**
 * #################################################################
 * WiseGuiLoginObserver
 * #################################################################
 *
 * Listens to WiseGui events 'wisegui-logged-in' and 'wisegui-logged-out'. The former carries an object
 *
 * {
 *   testbedId : "uzl",
 *   loginData : {
 * 	   authenticationData :
 * 	   [
 *       {
 *         urnPrefix : 'urn:wisebed:uzl1:',
 *         username  : 'bla',
 *         password  : 'blub'
 *       }
 * 	   ]
 *   }
 * }
 */
var WiseGuiLoginObserver = function() {
	this.isObserving = false;
	this.loginData   = {};
	this.schedules   = {};
	this.interval    = 10 * 1000;
};

WiseGuiLoginObserver.prototype.renewLogin = function(testbedId) {

	console.log('WiseGuiLoginObserver trying to renew login for ' + testbedId);

	Wisebed.login(
			testbedId,
			this.loginData[testbedId],
			function(){
				console.log('WiseGuiLoginObserver successfully renewed login for ' + testbedId);
			},
			function(jqXHR, textStatus, errorThrown) {
				console.log('WiseGuiLoginObserver failed renewing login for ' + testbedId);
				this.stopObservationOf(testbedId);
				WiseGui.showAjaxError();
			}
	);
};

WiseGuiLoginObserver.prototype.startObservationOf = function(testbedId, loginData) {

	console.log('WiseGuiLoginObserver starting observation of testbed ' + testbedId);

	var self = this;
	this.loginData[testbedId] = loginData;
	this.schedules[testbedId] = window.setInterval(
			function() { self.renewLogin(testbedId); },
			self.interval
	);
};

WiseGuiLoginObserver.prototype.stopObservationOf = function(testbedId) {

	if (this.schedules[testbedId]) {

		console.log('WiseGuiLoginObserver stopping observation of testbed ' + testbedId);
		window.clearInterval(this.schedules[testbedId]);
		delete this.schedules[testbedId];
	}
};

WiseGuiLoginObserver.prototype.startObserving = function() {

	this.isObserving = true;

	var self = this;

	$(window).bind('wisegui-logged-in', function(e, data) {
		if (data.loginData) {
			self.startObservationOf(data.testbedId, data.loginData);
		}
	});

	$(window).bind('wisegui-logged-out', function(e, data) { self.stopObservationOf(data); });

	$(window).bind('wisegui-navigation-event', function(e, data) {
		if (data.testbedId) {
			getLoginDialog(data.testbedId).isLoggedIn(function(isLoggedIn) {
				if (isLoggedIn) {
					$(window).trigger('wisegui-logged-in', {testbedId:data.testbedId});
				} else {
					$(window).trigger('wisegui-logged-out', {testbedId:data.testbedId});
				}
			});
		}
	});
};

WiseGuiLoginObserver.prototype.stopObserving = function() {

	this.isObserving = false;

	$(this.schedules, function(testbedId, schedule) {
		window.clearInterval(schedule);
	});
	this.schedules = {};

	console.log('LoginObserver stopped observing');
};

/**
 * #################################################################
 * WiseGuiLoginDialog
 * #################################################################
 */
var WiseGuiLoginDialog = function(testbedId) {

	this.testbedId = testbedId;
	this.loginFormRows = [];
	this.loginData = { authenticationData : [] };

	this.view = $('<div id="WisebedLoginDialog-'+this.testbedId+'" class="modal hide"></div>');

	var self = this;
	Wisebed.getTestbeds(function(testbeds){self.buildView(testbeds)}, WiseGui.showAjaxError);
};

WiseGuiLoginDialog.prototype.doLogin = function() {

	var self = this;

	var callbackError = function(jqXHR, textStatus, errorThrown) {
		if (jqXHR.status == 403) {
			self.onLoginError();
		} else {
			console.log(jqXHR);
			WiseGui.showAjaxError(jqXHR, textStatus, errorThrown);
		}
	};

	var callbackDone = function() {
		self.onLoginSuccess();
		window.setTimeout(function() {self.hide()}, 1000);
		$(window).trigger('wisegui-logged-in', {testbedId : self.testbedId, loginData : self.loginData});
		$(window).trigger('hashchange');
	};

	Wisebed.login(self.testbedId, self.loginData, callbackDone, callbackError);
};

WiseGuiLoginDialog.prototype.onLoginError = function() {
	$.each(this.loginFormRows, function(index, elem) {
		$(elem.inputUsername).removeClass('success');
		$(elem.inputPassword).removeClass('success');
		$(elem.inputUsername).addClass('error');
		$(elem.inputPassword).addClass('error');
	});
};

WiseGuiLoginDialog.prototype.onLoginSuccess = function() {
	$.each(this.loginFormRows, function(index, elem) {
		$(elem.inputUsername).removeClass('error');
		$(elem.inputPassword).removeClass('error');
		$(elem.inputUsername).addClass('success');
		$(elem.inputPassword).addClass('success');
	});
};

WiseGuiLoginDialog.prototype.isLoggedIn = function(callback) {
	Wisebed.isLoggedIn(this.testbedId, callback, WiseGui.showAjaxError);
};

WiseGuiLoginDialog.prototype.doLogout = function() {
	Wisebed.deleteSecretAuthenticationKeyCookie(this.testbedId);
	delete loginDialogs[this.testbedId];
	$(window).trigger('wisegui-logged-out', {testbedId : this.testbedId});
};

WiseGuiLoginDialog.prototype.hide = function() {
	this.view.hide();
	this.view.remove();
};

WiseGuiLoginDialog.prototype.show = function() {
	$(document.body).append(this.view);
	this.view.show();
};

WiseGuiLoginDialog.prototype.updateLoginDataFromForm = function() {

	for (var i=0; i<this.loginFormRows.length; i++) {

		this.loginData.authenticationData[i] = {
			urnPrefix : this.loginFormRows[i].inputUrnPrefix.value,
			username  : this.loginFormRows[i].inputUsername.value,
			password  : this.loginFormRows[i].inputPassword.value
		};
	}
};

WiseGuiLoginDialog.prototype.addRowToLoginForm = function(tbody, urnPrefix, username, password) {

	var tr = $('<tr/>');

	var i = this.loginFormRows.length;

	var inputUrnPrefix = $('<input type="text" id="urnprefix'+i+'" name="urnprefix'+i+'" value="'+urnPrefix+'" readonly/>');
	var inputUsername = $('<input type="text" id="username'+i+'" name="username'+i+'" value="'+username+'"/>');
	var inputPassword = $('<input type="password" id="password'+i+'" name="password'+i+'" value="'+password+'"/>');

	this.loginFormRows[this.loginFormRows.length] = {
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

	tr.append($('<td>'+(this.loginFormRows.length)+'</td>'));
	tr.append(tdUrnPrefix);
	tr.append(tdUsername);
	tr.append(tdPassword);

	tbody.append(tr);
};

WiseGuiLoginDialog.prototype.buildView = function(testbeds) {

	var dialogHeader = $('<div class="modal-header"><h3>Login to Testbed ' + this.testbedId + '</h3></div>');

	var dialogBody = $('<div class="modal-body WiseGuiLoginDialog"/>'
			+ '		<form id="WisebedLoginDialogForm-'+this.testbedId+'">'
			+ '		<table id="WisebedLoginDialogFormTable-'+this.testbedId+'">'
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

	cancelButton.bind('click', this, function(e) {
		e.data.hide();
	});

	okButton.bind('click', this, function(e) {
		e.data.updateLoginDataFromForm();
		e.data.doLogin();
	});

	var dialogFooter = $('<div class="modal-footer"/>');
	dialogFooter.append(cancelButton, okButton);
	this.view.append(dialogHeader, dialogBody, dialogFooter);

	var loginFormTableBody = this.view.find('#WisebedLoginDialogFormTable-'+this.testbedId+' tbody');
	var urnPrefixes = testbeds.testbedMap[this.testbedId].urnPrefixes;

	for (var i=0; i<urnPrefixes.length; i++) {
		this.addRowToLoginForm(loginFormTableBody, urnPrefixes[i], "", "");
	}
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
	this.helpTooltipIsVisable = false;
	this.filter_checkbox = null;
	if(showFilter) this.generateHeader();
	this.generateTable(null);
};

WiseGuiNodeTable.prototype.generateHeader = function (f) {
	that = this;

	// Filter
	this.filter = $("<p></p>");
	help_image = $('<img class="WiseGuiNodeTable" style="float:right;cursor:pointer;margin-top:5px;" src="img/famfamfam/help.png">');
	help_div = $('<div style="margin-right:105px;"></div>');
	this.filter_checkbox = $('<input type="checkbox" style="float:right;margin-top:7px;margin-right:3px;">');
	this.filter.append(help_image);
	this.filter.append('<div style="float:right;margin-top:3px;">(Advanced)</div>');
	this.filter.append(this.filter_checkbox);
	this.filter.append(help_div);

	filter_input = $('<input type"text" style="width:100%;padding-left:0px;padding-right:0px;">');
	// Key up event if enter is pressed
	filter_input.keyup(function(e) {
		if ((e.keyCode || e.which) == 13) {
			that.generateTable(filter_input.val());
		}
	});

	help_image.click(function() {
		if(!that.helpTooltipIsVisable) {
			help_image.popover("show");
		} else {
			help_image.popover("hide");
		}
		// Invert
		that.helpTooltipIsVisable = !that.helpTooltipIsVisable;
	});

	var helpText = '<h3>Normal mode</h3>';
	helpText += 'In normal mode, the filter is a full text search.';
	helpText += '<h3>Advanced mode</h3>';
	helpText += 'In advacned mode, the filter is using <a href="http://api.jquery.com/filter/" target="_blank">jQuery.filter()</a> on the given data structure.';

	if(this.wiseML.setup.node.length > 0) {
		helpText += '<br>The data structure looks as follows:';
		helpText += "<pre style=\"overflow:auto;height:50px;margin:0px;\">" + JSON.stringify(this.wiseML.setup.node[0], null, '  ') + "</pre>";
	}

	helpText += '<h5>Some examples:</h5>';

	helpText += '<ul style="margin-bottom:0px;font-family: monospace;">';
	helpText += '<li>e.nodeType == "isense"';
	helpText += '<li>e.position.x == 25';
	helpText += '<li>e.id.indexOf("0x21") > 0';
	helpText += '<li>($(e.capability).filter(function (i) {return this.name.indexOf("temperature") > 0;}).length > 0)';
	helpText += '</ul>';

	var pop = help_image.popover({placement:'left', animate:true, html: true, trigger: 'manual', content: helpText, title: function() {return "Help";}});
	help_div.append(filter_input);
	this.html.append(this.filter);
}

h = 0;
WiseGuiNodeTable.prototype.generateTable = function (f) {

	// TODO: use buildTable(...)

	var that = this;
	var nodes = this.wiseML.setup.node;

	if(f != null && f.length > 0 && this.filter_checkbox.is(':checked')) {
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
			WiseGui.showErrorAlert("Filter expression invalid.");
			return;
		} else {
			this.lastWorkingFilterExpr = f;
		}
	}

	var data = this.getData(nodes);

	// Simple filter
	if(f != null && f.length > 0 && !this.filter_checkbox.is(':checked')) {
		data = $(data).filter(function(i) {
			return implode(" ", this).toLowerCase().indexOf(f.toLowerCase()) > 0;
		});
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

	this.checkboxes = [];
	for(i = 0; i < data.length; i++) {
		var n = data[i];

		if(this.showCheckboxes) {
			var checkbox = $('<input type="checkbox" name="' + n[0] + '"/>');
			this.checkboxes[i] = checkbox;
			var td_checkbox = $('<td></td>');
			td_checkbox.append(checkbox);
		}

		var td_id = $('<td>' + n[0] + '</td>')
		var td_type = $('<td>' + n[1] + '</td>')
		var td_position = $('<td>' + n[2] + '</td>')
		var td_sensors = $('<td>' + n[3] + '</td>')

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

WiseGuiNodeTable.prototype.getData = function (nodes) {

	var returnData = [];

	for(i = 0; i < nodes.length; i++) {
		var n = nodes[i];

		var cap = [];
		for(j = 0; j < n.capability.length; j++) {
			parts = explode(":", n.capability[j].name);
			cap[j] = parts[parts.length-1];
		}

		data = [];
		data.push(n.id);
		data.push(n.nodeType);
		data.push('(' + n.position.x + ',' + n.position.y + ',' + n.position.z + ')');
		data.push(implode(",", cap));
		returnData.push(data);
	}

	return returnData;
}



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

var WiseGuiReservationObserver = function() {
	this.lastKnownReservations = {};
	this.isObserving           = false;
	this.schedules             = {};
};

WiseGuiReservationObserver.prototype.fetchReservationsAndProcess = function(testbedId) {
	var self = this;
	Wisebed.reservations.getPersonal(
			testbedId,
			null,
			null,
			function(reservations) {self.processReservationsFetched(testbedId, reservations.reservations)},
			null
	);
};

WiseGuiReservationObserver.prototype.processReservationsFetched = function(testbedId, reservations) {

	var newReservations = [];

	for (var i=0; i<reservations.length; i++) {

		var knownReservation = false;

		if (!this.lastKnownReservations[testbedId]) {
			this.lastKnownReservations[testbedId] = [];
		}

		for (var j=0; j<this.lastKnownReservations[testbedId].length; j++) {
			if (Wisebed.reservations.equals(reservations[i], this.lastKnownReservations[testbedId][j])) {
				knownReservation = true;
				break;
			}
		}

		if (!knownReservation) {
			newReservations.push(reservations[i]);
		}
	}

	if (newReservations.length > 0) {
		$(window).trigger('wisegui-reservations-changed-'+testbedId, {reservations:reservations});
	}

	for (var k=0; k<newReservations.length; k++) {

		$(window).trigger('wisegui-reservation-added-'+testbedId, newReservations[k]);

		// schedule events for reservation started and ended in order to e.g. display user notifications
		var nowInMillis = new Date().valueOf();
		if (nowInMillis < newReservations[k].from) {

			var triggerReservationStarted = (function(reservation) {
				return function() {$(window).trigger('wisegui-reservation-started-'+testbedId, reservation);}
			})(newReservations[k]);

			setTimeout(triggerReservationStarted, (newReservations[k].from - nowInMillis));
		}

		if (nowInMillis < newReservations[k].to) {

			var triggerReservationEnded = (function(reservation) {
				return function() {$(window).trigger('wisegui-reservation-ended-'+testbedId, reservation);}
			})(newReservations[k]);

			setTimeout(triggerReservationEnded, (newReservations[k].to - nowInMillis));
		}

		this.lastKnownReservations[testbedId].push(newReservations[k]);
	}
};

WiseGuiReservationObserver.prototype.startObserving = function() {

	var self = this;

	$(window).bind('wisegui-logged-in', function(e, data) {
		self.startObservationOf(data.testbedId);
	});

	$(window).bind('wisegui-logged-out', function(e, data) {
		self.stopObservationOf(data.testbedId);
	});
};

WiseGuiReservationObserver.prototype.stopObserving = function() {

	var self = this;
	$.each(this.schedules, function(testbedId, schedule) { self.stopObservationOf(testbedId) });

	console.log('WiseGuiReservationObserver stopped observing');
};
WiseGuiReservationObserver.prototype.startObservationOf = function(testbedId) {
	var self = this;
	this.schedules[testbedId] = window.setInterval(function() {self.fetchReservationsAndProcess(testbedId)}, 60 * 1000);
	this.fetchReservationsAndProcess(testbedId);
	console.log('WiseGuiReservationObserver beginning to observe reservations for testbedId "'+testbedId+'"');
};

WiseGuiReservationObserver.prototype.stopObservationOf = function(testbedId) {
	if (this.schedules[testbedId]) {
		window.clearInterval(this.schedules[testbedId]);
		delete this.schedules[testbedId];
		console.log('WiseGuiReservationObserver stopped to observe reservations for testbedId "'+testbedId+'"');
	}
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
			+ '	<p></p>'
			+ '	<div class="alert-actions">'
			+ '	</div>'
			+ '</div>');
	if (alert.message instanceof Array) {
		for (var i=0; i<alert.message.length; i++) {
			blockAlertDiv.find('p').append(alert.message[i]);
		}
	} else {
		blockAlertDiv.find('p').append(alert.message);
	}
	var actionsDiv = blockAlertDiv.find('.alert-actions');
	if (alert.actions) {
		for (var i=0; i<alert.actions.length; i++) {
			actionsDiv.append(alert.actions[i]);
			actionsDiv.append(' ');
		}
	}
	this.view.append(blockAlertDiv);
	blockAlertDiv.alert();
};

WiseGuiNotificationsViewer.prototype.buildView = function() {
	this.view = $('<div class="WiseGuiNotificationsContainer"></div>');
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

	this.testbedId = testbedId;
	this.view = null;

	var self = this;

	$(window).bind('wisegui-reservations-changed-'+testbedId, function(e, reservations) {
		self.onReservationsChangedEvent(reservations.reservations);
	});

	$(window).bind('wisegui-navigation-event', function(e, navigationData) {

		var sameTestbedId = navigationData.testbedId == self.testbedId;
		if (sameTestbedId) {
			getLoginDialog(self.testbedId).isLoggedIn(function(isLoggedIn) {
				if (isLoggedIn) {
					self.update();
				}
			});
		}
	});

	this.buildView();
};

WiseGuiExperimentDropDown.prototype.update = function() {
	var self = this;
	Wisebed.reservations.getPersonal(this.testbedId, null, null, function(reservations) {
		self.onReservationsChangedEvent(reservations.reservations);
	});
};

WiseGuiExperimentDropDown.prototype.onReservationsChangedEvent = function(reservations) {

	this.view.find('.dropdown-menu li').remove();

	for (var i=0; i<reservations.length; i++) {

		var reservation = reservations[i];
		var fromStr = $.format.date(new Date(reservation.from), "yyyy-MM-dd HH:mm");
		var toStr = $.format.date(new Date(reservation.to), "yyyy-MM-dd HH:mm");

		var li = $('<li><a href="#">' + fromStr + ' - ' + toStr + ' | ' + reservation.userData + '</a></li>');
		var self = this;
		li.find('a').bind('click', reservation, function(e) {
			e.preventDefault();
			navigateToExperiment(self.testbedId, e.data);
		});

		this.view.find('.dropdown-menu').append(li);
	}
};

WiseGuiExperimentDropDown.prototype.buildView = function() {
	this.view = $('<li class="dropdown">'
			+ '	<a href="#" class="dropdown-toggle">Experiments</a>'
			+ '	<ul class="dropdown-menu">' 
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


var WiseGuiTestbedsView = function(testbeds) {

	this.testbeds = testbeds;
	this.view = $('<table class="WisebedOverviewTable zebra-striped">'
			+ '	<thead>'
			+ '		<tr>'
			+ '			<td>Name</td>'
			+ '			<td>URN prefixes</td>'
			+ '			<td>Session Management Endpoint URL</td>'
			+ '		</tr>'
			+ '	</thead>'
			+ '	<tbody>'
			+ '	</tbody>'
			+ '</table>');
	this.buildView();
};

WiseGuiTestbedsView.prototype.buildView = function() {
	var self = this;
	$.each(testbeds.testbedMap, function(key, value) {
		var tr = $('<tr/>');
		var tdName = $('<td><a href="#nav=testbed&testbedId='+key+'">'+value.name+'</a></td>');
		var tdUrnPrefixes = $('<td>'+value.urnPrefixes+'</td>');
		var tdSessionManagementEndpointUrl = $('<td>'+value.sessionManagementEndpointUrl+'</td>');
		tr.append(tdName, tdUrnPrefixes, tdSessionManagementEndpointUrl);
		self.view.find('tbody').append(tr);
	});
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
	this.outputsTextAreaId       = this.experimentationDivId+'-outputs-textarea';
	this.sendDivId               = this.experimentationDivId+'-send';
	this.flashDivId              = this.experimentationDivId+'-flash';
	this.resetDivId              = this.experimentationDivId+'-reset';
	this.scriptingDivId          = this.experimentationDivId+'-scripting';

	this.flashConfigurations = [];

	this.view = $('<div class="WiseGuiExperimentationView"/>');

	this.flashSelectedNodeUrns = null;
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

		var blockAlertMessage = $(
				  '<div>'
				+ '	<strong>Backend notification from testbed "' + this.testbedId + '" at ' + message.timestamp + ':</strong><br/>'
				+  	message.message
				+ '</div>');

		if (getNavigationData().experimentId != this.experimentId) {

			var goToExperimentButton = $('<button class="btn primary">Go to experiment</button>');
			var blockAlertActions = [goToExperimentButton];

			var self = this;
			goToExperimentButton.bind('click', this, function(e, data) {
				navigateTo(self.testbedId, self.experimentId);
			});

		}

		WiseGui.showInfoBlockAlert(blockAlertMessage, blockAlertActions || null);
	}
};

WiseGuiExperimentationView.prototype.onWebSocketOpen = function(event) {

	this.outputsTextArea.attr('disabled', false);
};

WiseGuiExperimentationView.prototype.onWebSocketClose = function(event) {

	this.outputsTextArea.attr('disabled', true);
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

/**********************************************************************************************************************/

WiseGuiExperimentationView.prototype.buildView = function() {

	this.view.append('<div class="WiseGuiExperimentationViewOutputs">'
			+ '	<h2>Live Data</h2>'
			+ '	<textarea id="'+this.outputsTextAreaId+'" style="width: 100%; height:300px;" readonly disabled></textarea>'
			+ '</div>'
			+ '<div class="WiseGuiExperimentationViewControls">'
			+ '	<h2>Controls</h2></div>'
			+ '	<div>'
			+ '		<ul class="tabs">'
			+ '			<li class="active"><a href="#'+this.flashDivId+'">Flash</a></li>'
			+ '			<li><a href="#'+this.resetDivId+'">Reset</a></li>'
			+ '			<li><a href="#'+this.sendDivId+'">Send Message</a></li>'
			+ '			<li><a href="#'+this.scriptingDivId+'">Scripting</a></li>'
			+ '		</ul>'
			+ '		<div class="tab-content">'
			+ '			<div class="active tab-pane" id="'+this.flashDivId+'">'
			+ '				<div class="row">'
			+ '					<div class="span10">'
			+ '						<button class="btn addSet span1"> + </button>'
			+ '						<button class="btn removeSet span1"> - </button>'
			+ '						<button class="btn loadConfiguration span2" disabled>Load</button>'
			+ '						<button class="btn saveConfiguration span2" disabled>Save</button>'
			+ '						<button class="btn primary flashNodes span3">Flash</button>'
			+ '					</div>'
			+ '				</div>'
			+ '				<div class="row">'
		  	+ '					<div class="span16">'
			+ '						<table class="zebra-striped">'
			+ '							<thead>'
			+ '								<tr>'
			+ '									<th class="span1">Set</th>'
			+ '									<th class="span4">Node URNs</th>'
			+ '									<th class="span5">Image File</th>'
			+ '									<th class="span5">File Information</th>'
			+ '								</tr>'
			+ '							</thead>'
			+ '							<tbody>'
			+ '							</tbody>'
			+ '						</table>'
		  	+ '					</div>'
			+ '				</div>'
			+ '			</div>'
			+ '			<div class="tab-pane" id="'+this.resetDivId+'">'
		  	+ '				<div class="row">'
		  	+ '					<div class="span16">'
		  	+ '						<button class="btn selectNodeUrns span4">Select Nodes</button> <button class="btn primary resetNodeUrns span4" disabled>Reset Nodes</button>'
		  	+ '					</div>'
		  	+ '				</div>'
		  	+ '				<div class="row">'
		  	+ '					<div class="span16">'
		  	+ '						<h4>Selected Nodes:</h4> <div class="selectedNodeUrnsDiv" style="overflow:auto;"></div>'
		  	+ '					</div>'
		  	+ '				</div>'
			+ '			</div>'
		 	+ '			<div class="tab-pane" id="'+this.sendDivId+'">'
		  	+ '				<div class="row">'
		  	+ '					<div class="span16">'
		  	+ '						<p>Message must consist of comma-separated bytes in base_10 (no prefix), base_2 (prefix 0b) or base_16 (prefix 0x).</p>'
		  	+ '						<p>Example: <code>0x0A,0x1B,0b11001001,40,40,0b11001001,0x1F</code></p>'
		  	+ '					</div>'
		  	+ '				</div>'
		  	+ '				<div class="row">'
		  	+ '					<div class="span16">'
		  	+ '						<button class="btn selectNodeUrns span4">Select Nodes</button>'
		  	+ '						<input type="text" class="sendMessageMessageInput span8"/>'
		  	+ '						<button class="btn primary sendMessage span4">Send message</button><br/>'
		  	+ '					</div>'
		  	+ '				</div>'
		  	+ '			</div>'
			+ '			<div class="tab-pane" id="'+this.scriptingDivId+'">'
			+ '				Not yet implemented. Please see <a href="https://github.com/wisebed/rest-ws/issues/7" target="_blank">issue #7</a> for more details!'
			+ '			</div>'
			+ '		</div>'
			+ '	</div>'
			+ '</div>');

	this.outputsTextArea              = this.view.find('#' + this.outputsTextAreaId).first();

	this.flashAddSetButton            = this.view.find('#'+this.flashDivId + ' button.addSet').first();
	this.flashRemoveSetButton         = this.view.find('#'+this.flashDivId + ' button.removeSet').first();
	this.flashLoadConfigurationButton = this.view.find('#'+this.flashDivId + ' button.loadConfiguration').first();
	this.flashSaveConfigurationButton = this.view.find('#'+this.flashDivId + ' button.saveConfiguration').first();
	this.flashFlashButton             = this.view.find('#'+this.flashDivId + ' button.flashNodes').first();
	this.flashConfigurationsTableBody = this.view.find('#'+this.flashDivId + ' table tbody').first();

	this.resetNodeSelectionButton     = this.view.find('#'+this.resetDivId + ' button.selectNodeUrns').first();
	this.resetResetButton             = this.view.find('#'+this.resetDivId + ' button.resetNodeUrns').first();

	this.sendNodeSelectionButton      = this.view.find('#'+this.sendDivId + ' button.selectNodeUrns').first();
	this.sendSendButton               = this.view.find('#'+this.sendDivId + ' button.sendMessage').first();

	var self = this;

	// bind actions for flash tab buttons
	this.flashAddSetButton.bind('click', self, function(e) {
		self.addFlashConfiguration();
	});

	this.flashRemoveSetButton.bind('click', self, function(e) {
		self.removeFlashConfiguration();
	});

	this.flashLoadConfigurationButton.bind('click', self, function(e) {
		alert('TODO loadConfiguration');
	});

	this.flashSaveConfigurationButton.bind('click', self, function(e) {
		alert('TODO saveConfiguration');
	});

	this.flashFlashButton.bind('click', self, function(e) {
		self.executeFlashNodes();
	});

	// bind actions for reset tab buttons
	this.resetNodeSelectionButton.bind('click', self, function(e) {
		e.data.showResetNodeSelectionDialog()
	});

	this.resetResetButton.bind('click', self, function(e) {
		e.data.executeResetNodes()
	});

	// bind actions for send message tab buttons
	this.sendNodeSelectionButton.bind('click', self, function(e) {
		alert('TODO selectNodeUrns');
	});

	this.sendSendButton.bind('click', self, function(e) {
		alert('TODO sendMessage');
	});

	this.addFlashConfiguration();
};

/**********************************************************************************************************************/

WiseGuiExperimentationView.prototype.getFlashFormData = function() {

	var flashFormData = {
		configurations : []
	};

	for (var i=0; i<this.flashConfigurations.length; i++) {
		flashFormData.configurations.push(this.flashConfigurations[i].config);
	}

	return flashFormData;
};

WiseGuiExperimentationView.prototype.addFlashConfiguration = function() {

	// build and append the gui elements
	var nodeSelectionButton = $('<button class="btn nodeSelectionButton">Select Nodes</button>');
	var imageFileInput      = $('<input type="file" style="opacity: 0; width: 0px; position:absolute; top:-100px;"/>');
	var imageFileButton     = $('<button class="btn fileSelectionButton">Select Image</button>');
	var imageFileInfoLabel  = $('<div/>');
	var tr                  = $('<tr/>');

	var setNumberTd           = $('<td>' + (this.flashConfigurations.length + 1) + '</td>');
	var nodeSelectionButtonTd = $('<td/>');
	var imageFileInputTd      = $('<td/>');
	var imageFileInfoLabelTd  = $('<td/>');

	tr.append(setNumberTd, nodeSelectionButtonTd, imageFileInputTd, imageFileInfoLabelTd);
	nodeSelectionButtonTd.append(nodeSelectionButton);
	imageFileInputTd.append(imageFileInput);
	imageFileInputTd.append(imageFileButton);
	imageFileInfoLabelTd.append(imageFileInfoLabel);

	this.flashConfigurationsTableBody.append(tr);

	// build and remember the configuration
	var configuration = {
		nodeSelectionButton : nodeSelectionButton,
		imageFileInput      : imageFileInput,
		imageFileButton     : imageFileButton,
		imageFileLabel      : imageFileInfoLabel,
		tr                  : tr,
		config              : { nodeUrns : null, image : null }
	};

	this.flashConfigurations.push(configuration);

	// bind actions to buttons
	imageFileButton.bind('click', function() {
		configuration.imageFileInput.click();
	});

	imageFileInput.bind('change', function() {

		var imageFile       = imageFileInput[0].files[0];
		var imageFileReader = new FileReader();

		imageFileReader.onerror = function(progressEvent) {
			console.log(progressEvent);
			configuration.config.image = null;
		};

		imageFileReader.onloadend = function(progressEvent) {
			console.log(progressEvent);
			configuration.config.image = imageFileReader.result;
			imageFileInfoLabel.empty();
			imageFileInfoLabel.append(
					'<strong>' + imageFile.name + '</strong> (' + (imageFile.type || 'n/a') + ')<br/>'
					+ imageFile.size + ' bytes, last modified: ' + imageFile.lastModifiedDate.toLocaleDateString()
			);
		};

		imageFileReader.readAsDataURL(imageFile);

	});

	return configuration;
};

WiseGuiExperimentationView.prototype.removeFlashConfiguration = function() {
	if (this.flashConfigurations.length > 1) {
		var configuration = this.flashConfigurations.pop();
		configuration.tr.remove();
		return configuration;
	}
	return null;
};

WiseGuiExperimentationView.prototype.showFlashNodeSelectionDialog = function() {

	this.setFlashSelectNodesButtonDisabled(true);
	var self = this;
	Wisebed.getWiseMLAsJSON(
			this.testbedId,
			this.experimentId,
			function(wiseML) {

				self.setFlashSelectNodesButtonDisabled(false);

				var selectionDialog = new WiseGuiNodeSelectionDialog(
						self.testbedId,
						self.experimentId,
						'Flash Nodes',
						'Please select the nodes you want to flash.'
				);

				selectionDialog.show(function(selectedNodeUrns) { self.updateFlashSelectNodeUrns(selectedNodeUrns); });

			}, function(jqXHR, textStatus, errorThrown) {
				self.setFlashSelectNodesButtonDisabled(false);
				self.showAjaxError(jqXHR, textStatus, errorThrown);
			}
	);
};

WiseGuiExperimentationView.prototype.updateFlashSelectNodeUrns = function(selectedNodeUrns) {
	this.flashSelectedNodeUrns = selectedNodeUrns;
	if (selectedNodeUrns.length > 0) {
		this.setFlashButtonDisabled(false);
	}
	var selectNodeUrnsDiv = this.view.find('#'+this.flashDivId+' .selectedNodeUrnsDiv').first();
	selectNodeUrnsDiv.empty();
	selectNodeUrnsDiv.append(selectedNodeUrns.join(","));
};

WiseGuiExperimentationView.prototype.setFlashSelectNodesButtonDisabled = function(disabled) {
	this.view.find('#'+this.flashDivId + ' button.selectNodeUrns').first().attr('disabled', disabled);
};

WiseGuiExperimentationView.prototype.setFlashButtonDisabled = function(disabled) {
	this.view.find('#'+this.flashDivId + ' button.flashNodeUrns').first().attr('disabled', disabled);
};

WiseGuiExperimentationView.prototype.executeFlashNodes = function() {

	this.setFlashButtonDisabled(true);
	var self = this;
	Wisebed.experiments.flashNodes(
			this.testbedId,
			this.experimentId,
			this.flashSelectedNodeUrns,
			function(result) {
				self.setFlashButtonDisabled(false);
				WiseGui.showInfoAlert(JSON.stringify(result, null, '  '));
			},
			function(jqXHR, textStatus, errorThrown) {
				self.setResetButtonDisabled(false);
				alert('TODO handle error in WiseGuiExperimentationView');
			}
	);
};

/**********************************************************************************************************************/

WiseGuiExperimentationView.prototype.updateResetSelectNodeUrns = function(selectedNodeUrns) {
	this.resetSelectedNodeUrns = selectedNodeUrns;
	if (selectedNodeUrns.length > 0) {
		this.setResetButtonDisabled(false);
	}
	var selectNodeUrnsDiv = this.view.find('#'+this.resetDivId+' .selectedNodeUrnsDiv').first();
	selectNodeUrnsDiv.empty();
	selectNodeUrnsDiv.append(selectedNodeUrns.join(","));
};

WiseGuiExperimentationView.prototype.showResetNodeSelectionDialog = function() {

	this.setResetSelectNodesButtonDisabled(true);
	var self = this;
	Wisebed.getWiseMLAsJSON(
			this.testbedId,
			this.experimentId,
			function(wiseML) {

				self.setResetSelectNodesButtonDisabled(false);

				var selectionDialog = new WiseGuiNodeSelectionDialog(
						self.testbedId,
						self.experimentId,
						'Reset Nodes',
						'Please select the nodes you want to reset.'
				);

				selectionDialog.show(function(selectedNodeUrns) { self.updateResetSelectNodeUrns(selectedNodeUrns); });

			}, function(jqXHR, textStatus, errorThrown) {
				self.setResetSelectNodesButtonDisabled(false);
				self.showAjaxError(jqXHR, textStatus, errorThrown);
			}
	);
};

WiseGuiExperimentationView.prototype.setResetSelectNodesButtonDisabled = function(disabled) {
	this.view.find('#'+this.resetDivId + ' button.selectNodeUrns').first().attr('disabled', disabled);
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

/**
 * #################################################################
 * Global Functions
 * #################################################################
 */

function loadTestbedDetailsContainer(navigationData, parentDiv) {

	parentDiv.append($('<h1>Testbed Details '+navigationData.testbedId+'</h1>'));

	var tabs = $('<ul class="tabs">'
			+ '	<li class="active"><a href="#WisebedTestbedDetailsDescription-'+navigationData.testbedId+'">Description</a></li>'
			+ '	<li><a href="#WisebedTestbedDetailsNodes-'+navigationData.testbedId+'">Nodes</a></li>'
			+ '	<li><a href="#WisebedTestbedDetailsReservations-'+navigationData.testbedId+'">Reservations</a></li>'
			+ '	<li><a href="#WisebedTestbedDetailsWiseMLJSON-'+navigationData.testbedId+'">WiseML (JSON)</a></li>'
			+ '	<li><a href="#WisebedTestbedDetailsWiseMLXML-'+navigationData.testbedId+'">WiseML (XML)</a></li>'
			+ '</ul>'
			+ '<div class="tab-content">'
			+ '	<div class="tab-pane active" id="WisebedTestbedDetailsDescription-'+navigationData.testbedId+'"/>'
			+ '	<div class="tab-pane" id="WisebedTestbedDetailsNodes-'+navigationData.testbedId+'"/>'
			+ '	<div class="tab-pane" id="WisebedTestbedDetailsReservations-'+navigationData.testbedId+'"/>'
			+ '	<div class="tab-pane" id="WisebedTestbedDetailsWiseMLJSON-'+navigationData.testbedId+'"/>'
			+ '	<div class="tab-pane" id="WisebedTestbedDetailsWiseMLXML-'+navigationData.testbedId+'"/>'
			+ '</div>');

	parentDiv.append(tabs);

	Wisebed.getWiseMLAsJSON(
			navigationData.testbedId,
			null,
			function(wiseML) {

				var jsonTab = $('#WisebedTestbedDetailsWiseMLJSON-'+navigationData.testbedId);
				jsonTab.append($('<pre>'+JSON.stringify(wiseML, null, '  ')+'</pre>'));

				var descriptionTab = $('#WisebedTestbedDetailsDescription-'+navigationData.testbedId);
				descriptionTab.append(wiseML.setup.description);

				var nodesTab = $('#WisebedTestbedDetailsNodes-'+navigationData.testbedId);
				new WiseGuiNodeTable(wiseML, nodesTab, false, true);
			},
			WiseGui.showAjaxError
	);

	Wisebed.getWiseMLAsXML(
			navigationData.testbedId,
			null,
			function(wiseML) {
				var xmlTab = $('#WisebedTestbedDetailsWiseMLXML-'+navigationData.testbedId);
				xmlTab.append($('<pre lang="xml">'+new XMLSerializer().serializeToString(wiseML).replace(/</g,"&lt;")+'</pre>'));
			},
			WiseGui.showAjaxError
	);

	var now = new Date();
	var tomorrowSameTime = new Date();
	tomorrowSameTime.setDate(now.getDate() + 1);

	Wisebed.reservations.getPublic(
			navigationData.testbedId,
			now,
			tomorrowSameTime,
			function(data) {

				var reservations = data.reservations;
				var reservationsTab = $('#WisebedTestbedDetailsReservations-'+navigationData.testbedId);

				var tableHead = ["From", "Until", "User Data", "Node URNs"];
				var tableRows = [];
				for (var i=0; i<reservations.length; i++) {
					tableRows[i] = [];
					tableRows[i][0] = new Date(reservations[i].from).toString();
					tableRows[i][1] = new Date(reservations[i].to).toString();
					tableRows[i][2] = reservations[i].userData;

					var nodesContainer = $('<div>'
							+ reservations[i].nodeURNs.length + ' nodes.<br/>'
							+ '<a href="#">Show URNs</a>'
							+ '</div>');

					var nodesLink = nodesContainer.find('a');
					nodesLink.first().bind(
							'click',
							{link:nodesLink, container:nodesContainer, reservation:reservations[i]},
							function(e) {
								e.preventDefault();
								e.data.link.remove();
								e.data.container.append(
										e.data.reservation.nodeURNs.join("<br/>")
								);
							}
					);
					tableRows[i][3] = nodesContainer;
				}

				var table = buildTable(tableHead, tableRows);
				reservationsTab.append(table);
				if (tableRows.length > 0) {
					table.tablesorter({ sortList: [[0,0]] });
				}
			},
			WiseGui.showAjaxError
	);
}

function buildTable(tableHead, tableRows) {

	var table = $('<table class="zebra-striped"/>"');
	var thead = $('<thead/>');
	var theadRow = $('<tr/>');
	thead.append(theadRow);

	for (var i=0; i<tableHead.length; i++) {
		theadRow.append('<th>'+tableHead[i]+'</th>');
	}

	var tbody = $('<tbody/>');
	for (var k=0; k<tableRows.length; k++) {
		var row = $('<tr/>');
		tbody.append(row);
		for (var l=0; l<tableRows[k].length; l++) {
			var td = $('<td/>');
			row.append(td);
			td.append(tableRows[k][l]);
		}
	}

	table.append(thead, tbody);

	return table;
}

function loadExperimentContainer(navigationData, parentDiv) {

	var experimentationView = new WiseGuiExperimentationView(navigationData.testbedId, navigationData.experimentId);
	parentDiv.append(experimentationView.view);
}

function loadTestbedOverviewContainer(navigationData, parentDiv) {

	var testbedsView = new WiseGuiTestbedsView(testbeds);
	parentDiv.append(testbedsView.view);
}

function getNavigationKey(navigationData) {
	if (navigationData.nav == 'overview') {
		return 'overview';
	} else if (navigationData.nav == 'testbed' && navigationData.experimentId == '') {
		return 'testbedId=' + navigationData.testbedId;
	} else if (navigationData.nav == 'testbed' && navigationData.experimentId != '') {
		return 'testbedId=' + navigationData.testbedId + '&experimentId=' + navigationData.experimentId;
	}
	return undefined;
}

function getCreateContentFunction(navigationData) {
	if (navigationData.nav == 'overview') {return loadTestbedOverviewContainer;}
	if (navigationData.nav == 'testbed' && navigationData.experimentId == '') {return loadTestbedDetailsContainer;}
	if (navigationData.nav == 'testbed' && navigationData.experimentId != '') {return loadExperimentContainer;}
	return undefined;
}

function showReservationsDialog(testbedId) {
	alert('TODO reservation dialog for ' + testbedId);
}

function getLoginDialog(testbedId) {
	var loginDialog = loginDialogs[testbedId];
	if (!loginDialog) {
		loginDialog = new WiseGuiLoginDialog(testbedId);
		loginDialogs[testbedId] = loginDialog;
	}
	return loginDialog;
}

function navigateToExperiment(testbedId, reservation) {

	Wisebed.experiments.getUrl(
			testbedId,
			reservation,
			function(experimentUrl){

				var experimentId = experimentUrl.substr(experimentUrl.lastIndexOf('/') + 1);
				navigateTo(testbedId, experimentId);

			},
			WiseGui.showAjaxError
	);
}

function navigateTo(testbedId, experimentId) {
	var navigationData = {
		nav          : (testbedId ? 'testbed' : 'overview'),
		testbedId    : testbedId || '',
		experimentId : experimentId || ''
	};
	$.bbq.pushState(navigationData);
}

function getNavigationData(fragment) {

	var parsedFragment = $.deparam.fragment(fragment ? fragment : window.location.fragment);

	return {
		nav          : parsedFragment['nav']          || 'overview',
		testbedId    : parsedFragment['testbedId']    || '',
		experimentId : parsedFragment['experimentId'] || ''
	};
}

function switchNavigationContainer(navigationData, navigationKey) {

	$('#WisebedContainer .WiseGuiNavigationContainer').hide();

	if (!navigationContainers[navigationKey]) {
		navigationContainers[navigationKey] = createNavigationContainer(navigationData);
	}

	$(navigationContainers[navigationKey]).show();
}

function createNavigationContainer(navigationData) {

	var containerDivId = 'WiseGuiNavigationContainer-' + (navigationData.testbedId ?
			('testbed-' + navigationData.testbedId) : 'testbeds');
	var container = $('<div class="WiseGuiNavigationContainer" id="'+containerDivId+'"/>');

	container.hide();

	$('#WisebedContainer .WiseGuiNotificationsContainer').before(container);

	var navigationViewer = new WiseGuiNavigationViewer(navigationData);
	container.append(navigationViewer.view);

	return container;
}

function switchContentContainer(navigationData, navigationKey) {

	$('#WisebedContainer .WisebedContentContainer').hide();

	if (!contentContainers[navigationKey]) {
		contentContainers[navigationKey] = createContentContainer(navigationData);
	}

	$(contentContainers[navigationKey]).show();
}

function createContentContainer(navigationData) {

	var containerDivId = 'WisebedContentContainer-' + (navigationData.testbedId ?
			('testbed-' + navigationData.testbedId) : 'testbeds');
	var container = $('<div class="WisebedContentContainer" id="'+containerDivId+'"/>');

	container.hide();

	$('#WisebedContainer .WiseGuiNotificationsContainer').after(container);

	var createContentFunction = getCreateContentFunction(navigationData);
	createContentFunction(navigationData, container);

	$('.tabs').tabs();

	return container;
}

function onHashChange(e) {

	var navigationData = getNavigationData(e.fragment);
	var navigationKey  = getNavigationKey(navigationData);

	switchNavigationContainer(navigationData, navigationKey);
	switchContentContainer(navigationData, navigationKey);

	$(window).trigger('wisegui-navigation-event', navigationData);
}

var navigationContainers = {};
var contentContainers    = {};
var loginDialogs         = {};

var loginObserver        = new WiseGuiLoginObserver();
var reservationObserver  = new WiseGuiReservationObserver();
var notificationsViewer  = new WiseGuiNotificationsViewer();

var testbeds             = null;

$(function () {

	$('#WisebedContainer').append(notificationsViewer.view);

	Wisebed.getTestbeds(
			function(testbedsLoaded) {

				testbeds = testbedsLoaded;

				reservationObserver.startObserving();
				loginObserver.startObserving();

				$(window).bind('hashchange', onHashChange);
				$(window).trigger('hashchange');
			},
			WiseGui.showAjaxError
	);
});
