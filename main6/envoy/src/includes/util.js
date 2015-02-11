function trim(s)
{
	return s.replace(/(^\s*)|(\s*$)/g, "");
}

function fadeOut(id)
{
	var fadeout = dojo.fadeOut({node: id,duration: 1000}); 
	fadeout.play(); 
	document.getElementById(id).style.display="none";
}

function fadeOut2(id)
{
	var fadeout = dojo.fadeOut({node: id,duration: 1000}); 
	fadeout.play(); 
}

function quickFadeOut(id)
{
	var item = document.getElementById(id);
	if (item)
	{
		var fadeout = dojo.fadeOut({node: id,duration: 1}); 
		fadeout.play(); 
		item.style.display="none";
	}
}

function fadeIn(id)
{
    var item = document.getElementById(id);
	if (item)
	{
		var fadein = dojo.fadeIn({node: id,duration: 1000}); 
		fadein.play(); 
		item.style.display="";
	}
}

function allowChars(oTextbox, oEvent)
{
	if(document.all)
	{ 
	   var key = window.event.keyCode; 
	   var allowChars = oTextbox.getAttribute("validchars");
       var sChar = String.fromCharCode(key);	
       var bIsValidChar = allowChars.indexOf(sChar) > -1;
       return bIsValidChar || oEvent.ctrlKey;
	}
	
	return true;
}

function blockChars(oTextbox, oEvent)
{
	if(document.all)
	{ 
	   var key = window.event.keyCode; 
	   var allowChars = oTextbox.getAttribute("invalidchars");
       var sChar = String.fromCharCode(key);	
       var bIsValidChar = allowChars.indexOf(sChar) < 0;
       return bIsValidChar || oEvent.ctrlKey;
	}
	
	return true;
}