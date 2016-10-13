// Form validation scripts

// isNotEmptyString: return false if the string is null or empty
function isNotEmptyString(theString) {
	if (theString == "" || theString == null) {
		return false;
	}
	return true;
}

// isNotLongerThan checks maximum length of text box data
function isNotLongerThan(theField,maxLength) {
	if (theField.length > maxlength) return false;
	else return true;
}

// isNumber: checks to make sure contents of field are numeric
function isNumber(theField) {
	for (var i = 0; i < theField.value.length; i++) {
		var aChar = theField.value.substring(i, i + 1)
		if (aChar < "0" || aChar > "9") {
			return false;
		}
	}
	return true;
}

function isRange(theField, min, max) {
        if (theField.value == null || theField.value == "")
        {
		return false;
        }
	for (var i = 0; i < theField.value.length; i++) {
		var aChar = theField.value.substring(i, i + 1)
		if (aChar < "0" || aChar > "9") {
			return false;
		}
	}
	var value = parseInt(theField.value);
	if (value < min || value > max)
        {
		return false;
	}
	return true;
}

