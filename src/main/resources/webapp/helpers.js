var WBHelper = new function() {

	this.populateSelectBox = function(select, options) {
		this.emptySelectOptions(select);
		this.addSelectOptions(select, options);
	};

	this.addSelectOptions = function(select, options) {
		var initialLength = select.options.length;
		$.each(options, function(index, elem) {
			select.options[initialLength + index] = new Option(elem.text, elem.value, false, true);
		});
	};


	this.removeSelectOptionByValue = function(select, value) {
		for (var i=0; i<select.options.length; i++) {
			if (select.options[i].value == value) {
				select.remove(i);
				return;
			}
		}
	};

	this.emptySelectOptions = function(selectBox) {
		selectBox.options.length = 0;
	};
};