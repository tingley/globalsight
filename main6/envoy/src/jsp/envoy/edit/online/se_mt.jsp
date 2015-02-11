<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,
            com.globalsight.everest.webapp.WebAppConstants,
	        com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.machineTranslation.MachineTranslator,
            com.globalsight.machineTranslation.MTHelper2"
    session="true"
%>
<jsp:useBean id="mtTranslation" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
//Get MT translation URL for dojo
String mtTranslationURL = mtTranslation.getPageURL()
        + "&action=" + MTHelper2.ACTION_GET_MT_TRANSLATION;

// Get "state" and "view"
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

//if show mt in segment editor
boolean show_in_editor = false;
try 
{
    String showInEditor = (String) sessionMgr.getAttribute(MTHelper2.SHOW_IN_EDITOR);
    show_in_editor = (new Boolean(showInEditor)).booleanValue();
} 
catch (Exception e) { }

%>
<HTML>
<HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></SCRIPT>
<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none; }
.clickable { font-family:Arial, Helvetica, sans-serif; font-size: 9pt; 
	     color: blue; cursor: hand;cursor:pointer; }
.label     { font-family:Arial, Helvetica, sans-serif; font-size: 9pt;
	     font-weight: bold; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript">
function doClick()
{
	var dd = document.getElementById('<%=MTHelper2.MT_TRANSLATION_DIV%>').innerHTML;
	copyToTarget(dd);
}

function copyToTarget(value)
{
    parent.parent.SetSegment(value);
}

function doLoad()
{
	// Try to get MT translation after page is loaded.
    if (<%=show_in_editor%>)
    {
    	getMtTranslation();
    }
}

function getMtTranslation()
{
    dojo.xhrPost(
    {
       url:"<%=mtTranslationURL%>",
       handleAs: "text",
       load:function(data)
       {
           var returnData = eval(data);
           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	   //for "'" in IE, should be "&#39" instead of "&apos"
        	   var rData = returnData.mtMatch.replace(/&apos;/g,"&#39;");
        	   document.getElementById("mtTranslation").innerHTML = rData;
           }
       },
       error:function(error)
       {
    	   // do not display XHR error
           //alert(error.message);
       }
   });

}
</SCRIPT>
</HEAD>

<BODY VLINK="#0000FF" onLoad="doLoad();">

    <div id="mtTranslation" style="display:block;"></div>

</BODY>
</HTML>
