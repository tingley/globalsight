var baseUrlTB = "";
var baseUrlUserdata = "";

function init()
{
    var url = window.location.href;
    url = url.toLowerCase();
    var re = new RegExp("^http(s)?:\/\/.+\/", "g");
    var res = url.match(re);

    //res is an array in Firefox instread of string in IE
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
      //var baseUrl = url.substring(res.index, res.lastIndex - 1);
    	var baseUrl = res;
    }
    else
    {
      var baseUrl = res[0].substring(0, res[0].length);
    }
    
    baseUrlTB = baseUrl + "TerminologyServlet";
    baseUrlUserdata = baseUrl + "UserdataServlet";
}

// initialize this module
init();

function sendTermbaseManagementRequest(action, id, s)
{
    var xmlhttp = XmlHttp.create();
    if (!xmlhttp)
    {
        showError("Fatal Error: XMLHTTP object not found.");
        return;
    }

    xmlhttp.open("POST",
        baseUrlTB + "?action=" + action + "&id=" + id, false);
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
    var xmlhttp = XmlHttp.create();
    if (!xmlhttp)
    {
        showError("Fatal Error: XMLHTTP object not found.");
        return;
    }

    xmlhttp.open("POST", baseUrlTB + "?action=" + action, false);
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

function sendUserdataRequest(action, type, user, name, value)
{
    var xmlhttp = XmlHttp.create();
    if (!xmlhttp)
    {
        showError("Fatal Error: XMLHTTP object not found.");
        return;
    }

    // alert(baseUrlUserdata + "?action=" + action +
    //   "&object_type=" + type + "&object_user=" + user +
    //   "&object_name=" + name + "\nvalue=`" + value + "'");

    xmlhttp.open("POST", baseUrlUserdata + "?action=" + action, false);
    xmlhttp.setRequestHeader("contentType", "text/xml; charset=UTF-8");

    var xml = "<userdatarequest><action>" + action +
        "</action><type>" + type + "</type><username>" + user +
        "</username><name>" + name + "</name><value>" + value +
        "</value></userdatarequest>";

    //var objDom = XmlDocument.create();
    //objDom.async = false;
    //objDom.loadXML(xml);
    //alert(objDom.xml);

    xmlhttp.send(/*objDom.*/xml);

    if (xmlhttp.status < 200 || xmlhttp.status >= 300)
    {
        // alert("Status " + xmlhttp.status);
        // alert("StatusText " + xmlhttp.statusText);

        throw new Error(xmlhttp.status, xmlhttp.responseText);
    }

    return xmlhttp.responseXML;
}

function showError(error)
{
    window.showModalDialog("/globalsight/envoy/terminology/management/error.jsp",
        error,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}

// This should be turned into a MsgBox-style window with different
// icons for warnings, informational messages, errors etc.
function showWarning(message)
{
    window.showModalDialog("/globalsight/envoy/terminology/management/warning.jsp",
        message,
        "center:yes; help:no; resizable:yes; status:no; " +
        "dialogWidth: 450px; dialogHeight: 300px; ");
}
