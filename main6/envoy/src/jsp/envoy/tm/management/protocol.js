var baseUrl = "";

function __protocol_init()
{
    var url = window.location.href;
    url = url.toLowerCase();
    var re = new RegExp("^http(s)?:\/\/.+\/", "g");
    var res = url.match(re);

    baseUrl = url.substring(res.index, res.lastIndex - 1);
    baseUrl += "/globalsight/TMServlet";
}

// initialize this module
__protocol_init();

/*
function sendTMManagementRequest(action, id, s)
{
    var xmlhttp;
    try
    { // MSXML 3 and 4
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    }
    catch (exception)
    { // MSXML 1 and 2 - installed with IE 5.0/5.5
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    xmlhttp.open("POST",
        baseUrl + "?action=" + action + "&id=" + id, false);
    xmlhttp.setRequestHeader("contentType", "text/xml; charset=UTF-8");
    xmlhttp.send(s);

    if (xmlhttp.status < 200 || xmlhttp.status >= 300)
    {
        // alert("Status " + xmlhttp.status);
        // alert("StatusText " + xmlhttp.statusText);
        // alert("responseText " + xmlhttp.responseText);

        throw new Error(xmlhttp.status, xmlhttp.responseText);
    }

    return xmlhttp.responseText;
}

function sendXmlRequest(action, s)
{
    var xmlhttp;
    try
    { // MSXML 3 and 4
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    }
    catch (exception)
    { // MSXML 1 and 2 - installed with IE 5.0/5.5
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    xmlhttp.open("POST", baseUrl + "?action=" + action, false);
    xmlhttp.setRequestHeader("contentType", "text/xml; charset=UTF-8");
    xmlhttp.send(s);

    if (xmlhttp.status < 200 || xmlhttp.status >= 300)
    {
        // alert("Status " + xmlhttp.status);
        // alert("StatusText " + xmlhttp.statusText);
        // alert("responseText " + xmlhttp.responseText);

        throw new Error(xmlhttp.status, xmlhttp.responseText);
    }

    return xmlhttp.responseXML;
}
*/

function showError(error)
{
    window.showModalDialog("/globalsight/envoy/tm/management/error.jsp",
        error,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}

// This should be turned into a MsgBox-style window with different
// icons for warnings, informational messages, errors etc.
function showWarning(message)
{
    window.showModalDialog("/globalsight/envoy/tm/management/warning.jsp",
        message,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}
