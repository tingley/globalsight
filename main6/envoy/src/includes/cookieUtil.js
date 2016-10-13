
// parse the document cookie.
// look for a 'name=value' pair, and return the value.
// @param name a named value
function readCookieValue(name)
{
	var thecookie = document.cookie;

	// make sure we've got 'name' in the cookie
	var firstchar = thecookie.indexOf(name);

	if (firstchar != -1)  
	{
		firstchar += name.length + 1;   
		var lastchar = thecookie.indexOf(';',firstchar); 
			
        //just in case we're reading the last value in a cookie
		if (lastchar == -1) 
		{
		  lastchar = thecookie.length;
		}		
		//extract the value associated with 'name'
		return thecookie.substring(firstchar, lastchar); 
	} 
	else 
	{
    	//if 'name' doesn't exist, return false
		return false; 
	}
	
}

/* 
* set a 'name=value' cookie.
* @param name cookie to set
* @param value the value of the cookie
* @param duration is the number of  months until the cookie expires 
*/
function setCookieValue(name, value, duration)
{
	// first set the expiration to today's date
	var expiration = new Date(); 
	
	// add duration
	expiration.setMonth(expiration.getMonth() + duration);
	
	// set the cookie string
	document.cookie = name + '=' + value + ';' + 'expires=' + expiration.toGMTString() + ';' ;
}


