var c_xmlhttpUrl;
var c_progressBar;
var c_progressBarWidth;
var c_progressBarText;
var c_replacingMessage;
var c_appendingMessage;
var c_interval;
var c_done;
var c_progressTextFunc;
var xmlhttp;

function initProgressBar(url, bar, width, text, replacingMessage,
	appendingMessage, interval, done, progressTextFunc)
{
    c_xmlhttpUrl = url;
    c_progressBar = bar;
    c_progressBarWidth = width;
    c_progressBarText = text;
    c_replacingMessage = replacingMessage;
    c_appendingMessage = appendingMessage;
    c_interval = interval;
    c_done = done;
    c_progressTextFunc = progressTextFunc;
}

    
function progressBarUpdate()
{
    xmlhttp = getXmlHttpRequest();
    
    if(xmlhttp)
    {
        xmlhttp.open("GET", c_xmlhttpUrl, true);
        xmlhttp.onreadystatechange = redrawProgressBar;
        xmlhttp.send(null);
    }
}


function redrawProgressBar()
{
    if (xmlhttp.readyState == 4)
    {
        var done = "false";
        
        if (xmlhttp.status == 200)
        {
            // get data from a returned XML
            var dataXml = xmlhttp.responseXML;
        
            var counter = dataXml.getElementsByTagName("counter")[0];
            var percentage = dataXml.getElementsByTagName("percentage")[0];
            var error = dataXml.getElementsByTagName("error")[0];
            var doneNode = dataXml.getElementsByTagName("done")[0];
            done = getText(doneNode);
            
            var replacingMessage
                = dataXml.getElementsByTagName("replacingMessage")[0];
            var appendingMessages
                = dataXml.getElementsByTagName("appendingMessage");

            // set data to the page
            var messageArray = new Array();
            for(var i = 0; i < appendingMessages.length; i++)
            {
                messageArray[i] = getText(appendingMessages[i]);
            }
        
            setProgressData(getText(counter), getText(percentage),
		getText(error), getText(replacingMessage), messageArray);
        }
        
        if(done != "true")
        {
            setTimeout("progressBarUpdate()", c_interval);
        }
        else
        {
            c_done();
        }
    }
}

    
function getText(node)
{
    var text = null;

    if (node.firstChild)
    {
        text = node.firstChild.nodeValue;
    }

    return text;
}


function setProgressData(
    counter, percentage, error, replacingMessage, appendingMessage)
{
	//var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
	//var isChrome = window.navigator.userAgent.indexOf("Chrome")>0;
    // set data to the page
    c_progressBarText.innerHTML = c_progressTextFunc(counter, percentage);

    c_progressBar.style.width
        = Math.round((percentage / 100) * c_progressBarWidth);
        
   // if(isFirefox || isChrome) {
     c_progressBar.style.height = 16;
    //}     

    if(replacingMessage && c_replacingMessage)
    {
        c_replacingMessage.innerHTML = replacingMessage;

	if(error == "true")
	{
	    c_replacingMessage.style.color = "red";
	}
    }

    if(appendingMessage && c_appendingMessage)
    {
        for (var i = 0; i < appendingMessage.length; i++)
        {
            var div = docement.createElement("DIV");
            div.innerHTML = appendingMessage[i];
            c_appendingMessage.appendChild(div);
        }
    }
}
