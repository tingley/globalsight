function Validate()
{

}

Validate.prototype.isNumber = function(num)
{
	var number = "1234567890"; 
	for(var i=0; i < num.length; i++){ 
		if (number.indexOf(num.charAt(i)) < 0) { 
			return false; 
		} 
	} 
	return true; 
}

Validate.prototype.isPositiveInteger = function(num)
{
	var nnn = "1" + num;
	
	if (nnn.length == 1)
	{
		return false;
	}
	
	var number = "1234567890"; 
	for(var i=0; i < nnn.length; i++){ 
		if (number.indexOf(nnn.charAt(i)) < 0) { 
			return false; 
		} 
	} 
	return true; 
}

Validate.prototype.isEmptyStr = function(str)
{
	return (str == null || str.length <= 0 || this.trim(str) == "");
}

Validate.prototype.isEmptyArray = function(array)
{
	for(var i = 0; i < array.length; i++)
	{
		if(array[i] && array[i] != "")
		{
			return true;
		}
	}
	return false;
}

Validate.prototype.trim = function(strSrc)
{ 
	return strSrc.replace(/(^\s*)|(\s*$)/g, ""); 
}  

Validate.prototype.containsWhitespace = function(str)
{
	str = this.trim(str);
	return (str.indexOf(" ") != -1) || (str.indexOf("&nbsp;") != -1);
}

Validate.prototype.containSpecialChar = function(str)
{
	return (str.indexOf("<") != -1 || 
	   str.indexOf(">") != -1 || 
	   str.indexOf("<") != -1 || 
	   str.indexOf("'") != -1 ||
	   str.indexOf("\"") != -1 ||
	   str.indexOf("&") != -1)
}

Validate.prototype.containSpecialChars = function(str, specialChar)
{
	for(var i=0; i<specialChar.length; i++)
	{
		if (str.indexOf(specialChar.charAt(i)) != -1)
		{
			return true;
		}
	}
	
	return false;
}

Validate.prototype.isExceedMaxCount = function (str, max)
{
    str = this.trim(str);
    return str.length > max;
}