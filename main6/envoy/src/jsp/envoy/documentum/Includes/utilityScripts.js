function stripBlanks(theString) {
	var result = theString;
	while (result.substring(0,1) == " ") {
		result = result.substring(1,result.length);
	}
	return result;
}

function isEmptyString(theString) {
	if (theString == "" || theString == null) return true;
	else return false;
}

function isSelectionMade(theSelectField) {
	if (theSelectField.selectedIndex == -1 || theSelectField.options[theSelectField.selectedIndex].value == "-1") return false;
	else return true;
}

function isAllDigits(theString) {
	for (var i = 0; i < theString.length; i++) {
		var aChar = theString.substring(i, i + 1)
		if (aChar < "0" || aChar > "9") {
			return false;
		}
	}
	return true;
}

function isNotLongerThan(theField,maxLength) {
	if (theField.length > maxLength) return false;
	else return true;
}

