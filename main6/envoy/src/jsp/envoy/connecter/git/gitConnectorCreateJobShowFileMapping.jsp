<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
	com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
	java.util.ResourceBundle"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
<head>
<title>Check File Mapping</title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT TYPE="text/javascript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
</head>

<body>

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;width:96%;">
<DIV ID="idHeading" CLASS="mainHeading">Check File Mapping</DIV>
<BR>

<DIV ID="idStatistics">
<TABLE id="idTable" width="100%"
  CELLPADDING=1 CELLSPACING=0 BORDER=1 CLASS="standardText"
  style="border-collapse: collapse">
  <THEAD>
    <TR style="background-color: #b6c8de; border-top-color: #b6c8de">
      <TD width="300px" valign="top" align="left" nowrap>File Path</TD>
      <TD valign="top" align="left" nowrap>Not Mapped Locales</TD>
    </TR>
  </THEAD>
  <TBODY id="idTableBody"></TBODY>
</TABLE>
</DIV>
</DIV>
</body>
<SCRIPT TYPE="text/javascript">
$.ajaxSettings.async = false;

var gcId = window.opener.document.getElementById("gcId").value;
var checkFileMappingUrl = window.opener.document.getElementById("checkFileMappingUrl").value;

var fileProfiles = window.opener.document.getElementsByName("fileProfile");
var fileProfileId = "";	
for(var i=0;i<fileProfiles.length;i++)
{
	fileProfileId = fileProfiles[i].value;
}

var targetLocales = window.opener.document.getElementsByName("targetLocale");
var targetLocaleIds = "";	
for(var i=0;i<targetLocales.length;i++)
{
	if(targetLocales[i].checked)
	{
		targetLocaleIds = targetLocaleIds + " " + targetLocales[i].value;
	}
}
targetLocaleIds = targetLocaleIds.substr(1);

var jobFilePaths = window.opener.document.getElementsByName("jobFilePath");
for(var i=0;i<jobFilePaths.length;i++)
{
	var random = Math.random();
	$.get(checkFileMappingUrl, 
    {action:"action",filePath:jobFilePaths[i].value,fileProfileId:fileProfileId,gcId:gcId,targetLocaleIds:targetLocaleIds,"no":Math.random()}, 
    function(data)
    {
    	if(data != "")
    	{
    		html = "<tr><td>" + jobFilePaths[i].value + "</td><td>" + data + "</td></tr>";
	  		$("#idTableBody").append(html);
    	}
    });
}
</script>
</html>
