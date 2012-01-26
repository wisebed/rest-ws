var loginFormElements = [
	{
		"name"    : "urnprefix",
		"id"      : "urnprefix",
		"caption" : "URN prefix",
		"type"    : "text"
	},
	{
		"name"    : "username",
		"id"      : "username",
		"caption" : "Username",
		"type"    : "text"
	},
	{
		"name"    : "password",
		"id"      : "id",
		"caption" : "Password",
		"type"    : "password"
	}
];

var loginFormRows = new Array();

function addNewRowToLoginForm() {
	addRowToLoginForm($("#firstButtonsRow"), loginFormRows.length);
}

function removeRowFromLoginForm() {

	if (loginFormRows.length > 1) {

		for (var i=0; i<loginFormRows[loginFormRows.length-1].length; i++) {
			loginFormRows[loginFormRows.length-1][i].remove();
		}

		loginFormRows.splice(loginFormElements.length-1, 1);
	}
}

function addRowToLoginForm(beforeElement, i) {

	loginFormRows[i] = new Array();

	for (var e=0; e<loginFormElements.length; e++) {

		var row = $('<div class="row"/>');

		var columnLabel = $('<div class="span4"/>');
		var columnField = $('<div class="span12"/>');

		var label = $('<label for="'+loginFormElements[e].id.concat(i)+'">'+loginFormElements[e].caption.concat('&nbsp;'+i)+'</label>');
		var field = $('<input type="'+loginFormElements[e].type+'" id="'+loginFormElements[e].id.concat(i)+'" name="'+loginFormElements[e].name.concat(i)+'"/>');

		columnLabel.append(label);
		row.append(columnLabel);
		columnField.append(field);
		row.append(columnField);
		beforeElement.before(row);

		loginFormRows[i][e] = row;
	}
}

$(document).ready(function () {
			addRowToLoginForm($("#firstButtonsRow"), 1);
			addRowToLoginForm($("#firstButtonsRow"), 2);
		}
);