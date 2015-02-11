// parse the cookie. look for a 'name=value' pair, and return the value.

function readTheCookie(name)
{

	var thecookie = document.cookie;
	
	//make sure we've got 'name' in the cookie
	var firstchar = thecookie.indexOf(name);
	
	if (firstchar != -1)  
	{
		firstchar += name.length + 1;   //set firstchar to the first letter after '='
		var lastchar = thecookie.indexOf(';',firstchar); //set lastchar to the next semicolon
		
		if (lastchar == -1) { lastchar = thecookie.length; } //just in case we're reading the last value in a cookie
		
		return thecookie.substr(firstchar, lastchar); //extract the value associated with 'name'
	} else 
	{
		return false; //if 'name' doesn't exist, return false
	}
	
}


/* set a 'name=value' cookie. duration is the number of 
	months until the cookie expires set path to root ('/') 
	if the cookie should be readable from all scripts */
	
function setTheCookie(name, value, duration, path)
{
	// first set the expiration to today's date
	var expiration = new Date(); 
	
	// add 2 months
	expiration.setMonth(expiration.getMonth() + duration);
	
	// set the cookie string
	document.cookie = name + '=' + value + ';' + 'path=' + path + ';' + 'expires=' + expiration.toGMTString() + ';' ;
}


// ultrasimple check for prior user registration

function tryIt(product_name)
{
	
	// get da cookie
	var product = readTheCookie('tryit');
	
	
	// check for existing registration
	if(product)
	{
		document.location = '/forms/thank_you2.html'; // let the user know we've got them on file
		
	} else 
	{
		setTheCookie('tryit', product_name, 2, '/');
		document.location = '/forms/try_it.html';
	}	
}


// again, ultrasimple check for prior user registration

function buyIt(product_name)
{
	
	// get da cookie
	var product = readTheCookie('buyit');
	
	
	// check for existing registration
	if(product)
	{
		document.location = '/forms/thank_you2.html'; // let the user know we've got them on file
		
	} else 
	{
		setTheCookie('buyit', product_name, 2, '/'); // otherwise, create a cookie and send them to the form
		document.location = '/forms/buy_it.html';
	}	
}

// read the cookie, check the box

function formCheck(try_or_buy)
{
	var product = readTheCookie(try_or_buy);
	
	if (try_or_buy == 'tryit')
	{
		document.tryform[product].checked = true;
	} else
	{
		document.buyform[product].checked = true;
	}
	
} 
