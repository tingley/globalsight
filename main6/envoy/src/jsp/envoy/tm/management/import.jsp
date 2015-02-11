<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        java.text.MessageFormat,
        com.globalsight.everest.tm.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String tmName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);
String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_DEFINITION);
String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_IMPORT_OPTIONS);

String urlNext   = next.getPageURL();
String urlCancel = cancel.getPageURL() + "&"
		+ WebAppConstants.TM_ACTION + "="
		+ WebAppConstants.TM_ACTION_CANCEL_VALIDATION;
String urlRefresh = next.getPageURL() + "&"
        + WebAppConstants.TM_ACTION + "="
        + WebAppConstants.TM_ACTION_VALIDATION_REFRESH;

String str_databaseName = (String) sessionMgr
.getAttribute(WebAppConstants.TM_TM_NAME);
String str_databaseId = (String) sessionMgr
.getAttribute(WebAppConstants.TM_TM_ID);

Object[] args = { str_databaseName };
MessageFormat format = new MessageFormat(bundle
.getString("lb_import_into_tm"));
String lb_import_into_database = format.format(args);
String opts = "";
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=bundle.getString("lb_tm_import")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_import")%>";
</SCRIPT>
<STYLE>
#idProgressContainer {
    border: solid 1px <%=skin.getProperty("skin.list.borderColor") %>;
    z-index: 1;
    position: absolute;
    top: 82;
    left: 20;
    width: 400;
}

#idProgressBar {
    background-color: #a6b8ce;
    z-index: 0;
    border: solid 1px <%=skin.getProperty("skin.list.borderColor") %>;
    position: absolute;
    top: 82;
    left: 20;
    width: 0;
}

#idProgress {
    text-align: center;
    z-index: 2;
    font-weight: bold;
}

#idMessagesHeader {
    position: absolute;
    top: 72;
    left: 20;
    font-weight: bold;
}

#idMessages {
    position: absolute;
    overflow: auto;
    z-index: 0;
    top: 62;
    left: 20;
    height: 80;
    width: 400;
    font-weight: bold;
}

#idLinks {
    position: absolute;
    left: 20;
    top: 314;
    z-index: 1;
}
</STYLE>
<SCRIPT language="Javascript">
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
var xmlHttp = getXmlHttp();

function getXmlHttp()
{
    var xmlHttp = false;
    try {
      xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (e2) {
        xmlHttp = false;
      }
    }
    
    if (!xmlHttp && typeof XMLHttpRequest != 'undefined') {
      xmlHttp = new XMLHttpRequest();
    }
    
    return xmlHttp;
}

function callServer(url)
{
  xmlHttp.open("POST", url, true);
  xmlHttp.onreadystatechange = updatePage;
  xmlHttp.send(null);
}

function updatePage() 
{
  if (xmlHttp.readyState == 4) 
  {
   if (xmlHttp.status == 200) 
   {
       var response = xmlHttp.responseText;
       
       if (response != "end")
       {
           var status = response.split("|");
           showProgress(status[0], status[1], status[2]);
           if (status[3] == "true")
           {
               done();
           }
           else
           {
               setTimeout('callServer("<%=urlRefresh%>")',500);
           }
       }
   }
  }
}

var WIDTH = 400;

function showProgress(entryCount, percentage, message)
{
   idProgress.innerHTML = entryCount.toString(10) + " (" +
   percentage.toString(10) + "%)";

   idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
   //if (isFirefox || window.navigator.userAgent.indexOf("Chrome")>0)
   //{
     idProgressBar.style.height = 15;
     idProgressBar.innerHTML='&nbsp';
   //}

   if (message != null && message != "")
   {
      idMessages.innerHTML = message;
   }
}


function Result(message, description, element, dom)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = dom;
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doNext()
{
    var result = buildFileOptions();

    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        var url = "<%=urlNext +
            "&" + WebAppConstants.TM_ACTION +
            "=" + WebAppConstants.TM_ACTION_UPLOAD_FILE%>";

        oForm.action = url;
        oForm.importoptions.value = getDomString(result.dom);
        oForm.submit();
        
        document.getElementById("selectFile").style.display = "none";
        document.getElementById("processDiv").style.display = "block";
         
        callServer("<%=urlRefresh%>");
    }
}

function checkExtension(path)
{
  if (path != null && path != "")
  {
    var index = path.lastIndexOf(".");
    if (index < 0)
    {
      return;
    }

    var form = document.oDummyForm;
    var ext = path.substring(index + 1);

    if (ext.toLowerCase() == "<%=ImportOptions.TYPE_XML%>")
    {
        form.oType[0].click();
    }
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TMX1%>")
    {
        form.oType[1].click();
    }
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TMX2%>")
    {
        form.oType[2].click();
    }
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TTMX_RTF%>")
    {
        form.oType[3].click();
    }
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TTMX_HTML%>")
    {
        form.oType[4].click();
    }
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TTMX_FM%>")
    {
        form.oType[5].click();
    }
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TTMX_FM_SGML%>")
    {
        form.oType[6].click();
    }
<%--
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TTMX_IL%>")
    {
        form.oType[7].click();
    }
--%>
    else if (ext.toLowerCase() == "<%=ImportOptions.TYPE_TTMX_XPTAG%>")
    {
        form.oType[7].click();
    }
    else
    {
        // default is TMX level 2
        form.oType[2].click();
    }
  }
}

function setFileType()
{
   var form = document.oDummyForm;

   if (form.oType[0].checked)
   {
   }
}

function buildFileOptions()
{
    var result = new Result("", "", null, null);
    var dom;    
    var xmlStr = "<%opts = xmlImportOptions.replaceAll("\\\\","\\\\\\\\");out.print(opts);%>";
    dom = $.parseXML(xmlStr);
    var node;

    node = $(dom).find("importOptions fileOptions");
    
    node.find("fileName").text(document.oForm.filename.value);
    
    if (document.oDummyForm.oType[0].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_XML%>");
    }
    else if (document.oDummyForm.oType[1].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TMX1%>");
    }
    else if (document.oDummyForm.oType[2].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TMX2%>");
    }
    else if (document.oDummyForm.oType[3].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TTMX_RTF%>");
    }
    else if (document.oDummyForm.oType[4].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TTMX_HTML%>");
    }
    else if (document.oDummyForm.oType[5].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TTMX_FM%>");
    }
    else if (document.oDummyForm.oType[6].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TTMX_FM_SGML%>");
    }
    else if (document.oDummyForm.oType[8].checked)
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TMX_WORLD_SERVER%>");
    }
<%--
    else if (document.oDummyForm.oType[7].checked)
    {
       node.find("fileType").text = "<%=ImportOptions.TYPE_TTMX_IL%>";
    }
--%>
    else
    {
       node.find("fileType").text("<%=ImportOptions.TYPE_TTMX_XPTAG%>");
    }
    
    node = $(dom).find("importOptions sourceTmOptions sourceTmName");
    node.text(document.oForm.sourceTmName.value);

    if (oForm.filename.value == "")
    {
        return new Result(
          "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_missing_filename"))%>",
          "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_specify_filename"))%>",
          oForm.filename);
    }
    
    result.dom = dom;
    return result;
}

function parseFileOptions()
{
    var form = document.oDummyForm;
    var dom;    
    var xmlStr = "<%opts = xmlImportOptions.replaceAll("\\\\","\\\\\\\\");out.print(opts);%>";
    dom = $.parseXML(xmlStr);    
    
    var node, fileName, fileType;
    node = $(dom).find("importOptions fileOptions");
    
    fileName = node.find("fileName").text();
    fileType = node.find("fileType").text();

    if (fileType == "<%=ImportOptions.TYPE_XML%>")
    {
        form.oType[0].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TMX1%>")
    {
        form.oType[1].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TMX2%>")
    {
        form.oType[2].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TTMX_RTF%>")
    {
        form.oType[3].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TTMX_HTML%>")
    {
        form.oType[4].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TTMX_FM%>")
    {
        form.oType[5].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TTMX_FM_SGML%>")
    {
        form.oType[6].checked = 'true';
    }
<%--
    else if (fileType == "<%=ImportOptions.TYPE_TTMX_IL%>")
    {
        form.oType[7].checked = 'true';
    }
--%>
    else if (fileType == "<%=ImportOptions.TYPE_TTMX_XPTAG%>")
    {
        form.oType[7].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_TMX_WORLD_SERVER%>")
    {
        form.oType[8].checked = 'true';
    }
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();

    parseFileOptions();
    setFileType();
    oForm.filename.focus();
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad()" LEFTMARGIN="0" RIGHTMARGIN="0"
        TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
        CLASS="standardText">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    
<div id="selectFile">   
<SPAN CLASS="mainHeading" ID="idHeading"><%=bundle.getString("lb_tm_import")%></SPAN>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=bundle.getString("lb_select_file_to_tm") %>&nbsp;<B><%=tmName%></B>.</TD>
  </TR>
</TABLE>
<BR>

<XML id="oDefinition" style="display:none"><%=xmlDefinition%></XML>
<XML id="oImportOptions" style="display:none"><%=xmlImportOptions%></XML>

<%-- We wrap the form around the file only. --%>
<FORM NAME="oForm" ACTION="" ENCTYPE="multipart/form-data" METHOD="post">
<INPUT TYPE="hidden" NAME="importoptions" VALUE="ImportOptions XML goes here" />
<DIV style="margin-bottom: 12px"><%=bundle.getString("lb_select_import_file")%><BR>
<INPUT TYPE="file" NAME="filename" id="idFilename"
 onchange="checkExtension(this.value)" SIZE=40></INPUT>
<BR><BR>
</DIV>

<DIV style="margin-bottom: 12px"><%=bundle.getString("lb_enter_source_tm_name")%><BR>
<INPUT TYPE="text" NAME="sourceTmName" SIZE=30></INPUT>
<BR><BR>
</DIV>

</FORM>

<FORM NAME="oDummyForm">
<B><%=bundle.getString("lb_file_options")%></B>
<DIV>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR VALIGN="TOP">
    <TD WIDTH="100"><%=bundle.getString("lb_terminology_import_format")%></TD>
    <TD WIDTH="500">
      <input type="radio" name="oType" id="idXml" CHECKED="true"
      onclick="setFileType()"><label for="idXml">
      <%=bundle.getString("lb_tm_export_format_gtmx")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTmx1"
      onclick="setFileType()"><label for="idTmx1">
      <%=bundle.getString("lb_tm_export_format_tmx1")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTmx2"
      onclick="setFileType()"><label for="idTmx2">
      <%=bundle.getString("lb_tm_export_format_tmx2")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmxRtf"
      onclick="setFileType()"><label for="idTtmxRtf">
      <%=bundle.getString("lb_tm_export_format_ttmx_rtf")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmxHtml"
      onclick="setFileType()"><label for="idTtmxHtml">
      <%=bundle.getString("lb_tm_export_format_ttmx_html")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmxFm"
      onclick="setFileType()"><label for="idTtmxFm">
      <%=bundle.getString("lb_tm_export_format_ttmx_fm")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmxFmSgml"
      onclick="setFileType()"><label for="idTtmxFmSgml">
      <%=bundle.getString("lb_tm_export_format_ttmx_fm_sgml")%>      
      </label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmxXptag"
      onclick="setFileType()"><label for="idTtmxXptag">
      <%=bundle.getString("lb_tm_export_format_ttmx_xptag")%></label></input>
      <BR>
      <input type="radio" name="oType" id="idTtmxWorldServer"
      onclick="setFileType()"><label for="idTtmxWorldServer">
      <%=bundle.getString("lb_tm_export_format_worldserver")%></label>
    </TD>
  </TR>
</TABLE>
</DIV>
</FORM>

<DIV id="idButtons" >
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_next")%>"
 onclick="doNext()">
</DIV>
<BR>
<TABLE>
<TR><TD ALIGN="LEFT"><IMG SRC="/globalsight/images/TMX.gif"></TD></tr>
<TR><TD ALIGN="LEFT"><SPAN CLASS="smallText"><%=bundle.getString("lb_tmx_logo_text1")%><BR><%=bundle.getString("lb_tmx_logo_text2")%></SPAN></TD></TR>
</TABLE>
</div>

<div id="processDiv" style="display:none">

<SPAN CLASS="mainHeading" id="idHeading"><%=lb_import_into_database%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"> <%=bundle
                                    .getString("msg_please_wait_untill_upload_finished")%>
</SPAN> <BR>

<DIV id="idMessages" class="header"></DIV>
<DIV id="idProgressContainer">
<DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idLinks" style="width: 500px"><INPUT TYPE="BUTTON"
    NAME="CANCEL" style="width: 60px;"
    VALUE="<%=bundle.getString("lb_cancel")%>" id="idCancelOk"
    onclick="doCancel()"><BR>
<BR>
<TABLE>
    <TR>
        <TD ALIGN="LEFT"><IMG SRC="/globalsight/images/TMX.gif"></TD>
    </TR>
    <TR>
        <TD ALIGN="LEFT"><SPAN CLASS="smallText"><%=bundle.getString("lb_tmx_logo_text1")%><BR>
        <%=bundle.getString("lb_tmx_logo_text2")%> </SPAN></TD>
    </TR>
</TABLE>
</DIV>
</div>


</DIV>
</BODY>
</HTML>
