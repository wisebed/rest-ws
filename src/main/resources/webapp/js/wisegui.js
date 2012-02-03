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