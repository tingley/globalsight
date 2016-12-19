/**
 * This javascript file is used for GlobalSight and it supply some additional functions
 * Please import jQuery defination before you use this javascript.
 * Author: Vincent Yan
 *   Date: 11/09/2016
 */

function isEmpty(str) {
	return "" == $.trim(str);
}

function isSelected(id, defaultValue) {
	var $select = $(id);
	if ($select[0].selectedIndex == 0 || $select.val() == defaultValue)
		return false;
	else
		return true;
}

function isInt(v) {
	return /^-?[1-9]\d*$/.test(v);
}

function isUnsignedInt(v) {
	return /^[1-9]\d*$/.test(v);
}

function isAllDigits(str) {
	return /^[0-9]*$/.test(str);
}

function hasHtmlSpecialChars(str) {
	return /[<>\\"&]/.test(str);
}

function hasSpecialChars(str)
{
	return /[~!@#$%\^&\*()+=\[\]\\\';,\./{}|\":<>\?]+/.test(str);
}

function selectAll(form) {
	$(form).find("input[type=checkbox]").attr("checked", true);
}

function unselectAll(form) {
	$(form).find("input[type=checkbox]").attr("checked", false);
}

function  validIP(ip){
    var reg =/^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])((\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])){2}){0,1}$/;
    return ip.match(reg);
} 

function validInput(s) {
	return /^[0-9a-zA-Z\_]+$/.test(s);
}

function validEmail(s) {
	var pattern = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
	return pattern.test(s);
}

var regLen = 3;
var pwdStrength = 0;
var passChecking = false;

function passwordChecking(val) {
	var value = $.trim(val);
	if (value == "")
		return false;
	
	var len = value.length;
	pwdStrength = strongPassword(value);
	if (len > 0 && pwdStrength == 0)
	  pwdStrength = 1;
	if (pwdStrength == 4)
		pwdStrength = 3;
	var result = (pwdStrength / regLen) * 100;
	var $progress = $(".vali_pass_inner_progress");
	$progress.css("width", result + "%");
	if(result >= 0 && result <= 50){
		$progress.attr("class", "vali_pass_inner_progress error");
		$("#pvText").val("Weak");
		passChecking = false;
	} else if (result > 50 && result < 100) {
		$progress.attr("class", "vali_pass_inner_progress psmiddle");
        $("#pvText").val("Good");
		passChecking = false;
	} else if (result == 100) {
		$progress.attr("class", "vali_pass_inner_progress strong");
        $("#pvText").val("Strong");
		passChecking = true;
	}
	return passChecking;
}

function strongPassword(sValue) {
	var modes = 0;
	if (sValue.length < 8) return modes;
	if (/\d/.test(sValue)) modes++; //digit
	if (/[a-z]/.test(sValue)) modes++; //lower case
	if (/[A-Z]/.test(sValue)) modes++; //upper case
	if (/\W/.test(sValue)) modes++; //special characters
	    
	switch (modes) {
	    case 1:
	        return 1;
	        break;
	    case 2:
	        return 2;
	    case 3:
	    case 4:
	        return sValue.length < 12 ? 3 : 4;
	        break;
	}
}



