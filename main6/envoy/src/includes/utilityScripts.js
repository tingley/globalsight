function stripBlanks(theString) {
   var result = theString;
   result = ATrim(theString);
   return result;
}

function LATrim(str)
{
   var whitespace = new String(" \t\n\r");
   var s = str;

   if (whitespace.indexOf(s.charAt(0)) != -1) {
      // We have a string with leading blank(s)...

      var j=0, i = s.length;

      // Iterate from the far left of string until we
      // don't have any more whitespace...
      while (j < i && whitespace.indexOf(s.charAt(j)) != -1)
         j++;

      // Get the substring from the first non-whitespace
      // character to the end of the string...
      s = s.substring(j, i);
   }
   return s;
}

function RATrim(str)
{
   // We don't want to trip JUST spaces, but also tabs,
   // line feeds, etc.  Add anything else you want to
   // "trim" here in Whitespace
   var whitespace = new String(" \t\n\r");
   var s = str;

   if (whitespace.indexOf(s.charAt(s.length-1)) != -1) {
      // We have a string with trailing blank(s)...

      var i = s.length - 1;       // Get length of string

      // Iterate from the far right of string until we
      // don't have any more whitespace...
      while (i >= 0 && whitespace.indexOf(s.charAt(i)) != -1)
         i--;


      // Get the substring from the front of the string to
      // where the last non-whitespace character is...
      s = s.substring(0, i+1);
   }

   return s;
}

function ATrim(str)
{
   return RATrim(LATrim(str));
}


function isEmptyString(theString) {
   if (ATrim(theString) == "" || theString == null) return true;
   else return false;
}

function isSelectionMade(theSelectField) {
   if (theSelectField.selectedIndex == -1 || theSelectField.options[theSelectField.selectedIndex].value == "-1") return false;
   else return true;
}

function isAllDigits(theString) {
   for (var i = 0; i < theString.length; i++)
   {
      var aChar = theString.substring(i, i + 1)
      if (aChar < "0" || aChar > "9")
      {
         return false;
      }
   }
   return true;
}

function isNotLongerThan(theField,maxLength) {
   if (theField.length > maxLength) return false;
   else return true;
}

function hasHtmlSpecialChars(theField) {
	var iChars = "<>\"&";
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars.indexOf(theField.charAt(i)) != -1)
        {
            return true;
        }
    }
    return false;
}

function hasSpecialChars(theField)
{
    var iChars = "~!@#$%^&*()+=[]\\\';,./{}|\":<>?";
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars.indexOf(theField.charAt(i)) != -1)
        {
            return true;
        }
    }
    return false;
}

// has the same special characters as the method "hasSpecialChars"
// expect for the period and at sign (. @)
// these are ok for theField to contain.
function hasSomeSpecialChars(theField)
{
    var iChars = "~!#$%^&*()+=[]\\\';,/{}|\":<>?";
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars.indexOf(theField.charAt(i)) != -1)
        {
            return true;
        }
    }
    return false;
}

function checkSpecialChars(theField)
{
    var iChars = "~!@#$%^&*()_+=[]\\\';./{}|\":<>?";
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars.indexOf(theField.charAt(i)) != -1)
        {
            return true;
        }
    }
    return false;
}

function checkSomeSpecialChar(theField)
{
    var iChars = "~!@#$%^&*()-_+=[]\\\';./{}|\":<>?";
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars.indexOf(theField.charAt(i)) != -1)
        {
            return true;
        }
    }
    return false;
}

function checkSomeSpecialChars(theField)
{
    var iChars = "#%^&+\\\'\"<>";
    for (var i = 0; i < theField.length; i++)
    {
        if (iChars.indexOf(theField.charAt(i)) != -1)
        {
            return true;
        }
    }
    return false;
}

function checkAll(form)
{    
   form = eval("document." + form);
   for (var i = 0; i < form.elements.length; i++)
   {
	if (form.elements[i].type == "checkbox" &&
	    !form.elements[i].disabled)
      {
         form.elements[i].checked = true;
      }
   }
}

/**
function checkAllWithName(form, name)
{    
   form = eval("document." + form);
   for (var i = 0; i < form.elements.length; i++)
   {
	if (form.elements[i].type == "checkbox" &&
        form.elements[i].name == name &&
	    !form.elements[i].disabled)
      {
         form.elements[i].checked = true;
      }
   }
}
*/

function checkAllWithName(form, name)
{    
   form = eval("document." + form);
   var eles = document.getElementsByName(name);
   for (var i = 0; i < eles.length; i++)
   {
	if (eles[i].type == "checkbox" &&
        eles[i].name == name &&
	    !eles[i].disabled)
      {
         eles[i].checked = true;
      }
   }
}


/**
function clearAll(form)
{    
   form = eval("document." + form);
   for (var i = 0; i < form.elements.length; i++)
   {
      if (form.elements[i].type == "checkbox")
      {
         form.elements[i].checked = false;
      }
   }
}
*/
function clearAll(form)
{    
   form = eval("document." + form);
   var eles = document.getElementsByTagName("*");
   for (var i = 0; i < eles.length; i++)
   {
      if (eles[i].type == "checkbox")
      {
         eles[i].checked = false;
      }
   }
}


function  isValidIP(ip){
    var reg =/^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])((\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[0-9]{1}[0-9]{1}|[0-9])){2}){0,1}$/;
    return ip.match(reg);
} 

function validInput(s) {
	var regu = "^[0-9a-zA-Z\_]+$";
	var re = new RegExp(regu);
	if (re.test(s)) {
		return true;
	}else{
		return false;
	}
}

function validEmail(s) {
	var pattern = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
	return pattern.test(s);
}
