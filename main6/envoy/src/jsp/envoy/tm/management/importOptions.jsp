<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.tm.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.tm.importer.ImportUtil,
        com.globalsight.everest.webapp.pagehandler.tm.management.TmImportPageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_IMPORT_OPTIONS);

String urlNext   = next.getPageURL();
String urlPrev   = prev.getPageURL();
String urlCancel = cancel.getPageURL();

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_import_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
String importopts = "";
%>

<%@page import="java.text.MessageFormat"%>
<HTML XMLNS:gs>
<!-- This is \envoy\tm\management\importOptions.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_tm_import_options")%></TITLE>
<STYLE>
FORM         { display: inline; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/tm/management/importOptions.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_importoptions")%>";
</SCRIPT>
<SCRIPT language="Javascript">
eval("<%=errorScript%>");

var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doPrev()
{
    var result = buildImportOptions();

    if (result.message != null && result.message != "")
    {
        showError(result);
        if (result.element != null)
        {
            result.element.focus();
        }
    }
    else
    {
        oForm.action = "<%=urlPrev +
            "&" + WebAppConstants.TM_ACTION +
            "=" + WebAppConstants.TM_ACTION_SET_IMPORT_OPTIONS%>";

        if(isIE) {
          oForm.importoptions.value = oImportOptions.xml;
        } else {
          oForm.importoptions.value = XML.getDomString(result.dom);
        }
        oForm.submit();
    }
}

function doNext()
{
    var result = buildImportOptions();

    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        oForm.action = "<%=urlNext +
          "&" + WebAppConstants.TM_ACTION +
          "=" + WebAppConstants.TM_ACTION_START_IMPORT%>";

        if(isIE) {
          oForm.importoptions.value = oImportOptions.xml;
        } else {
          oForm.importoptions.value = XML.getDomString(result.dom);
        }
        oForm.submit();
    }
}

function checkAnalysisError()
{
    var dom;
    //Mozilla compatibility  
    var xmlStr = "<%importopts = xmlImportOptions.replaceAll("\\\\","\\\\\\\\");out.print(importopts);%>";

    if(isIE)
    {
      dom = oImportOptions.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlStr,"text/xml");
    }
    
    var node = dom.selectSingleNode("/importOptions/fileOptions/errorMessage");
    var errorMessage = node.text;
    if (errorMessage != "")
    {
        // clear error in options object
        node.text = "";

        showWarning("<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_failed_analyze"))%>" + errorMessage);

        oForm.action = "<%=urlPrev +
            "&" + WebAppConstants.TM_ACTION +
            "=" + WebAppConstants.TM_ACTION_SET_IMPORT_OPTIONS%>";

        if(isIE) {
          oForm.importoptions.value = oImportOptions.xml;
        }
        else {
          oForm.importoptions.value = xmlStr;
        }
        oForm.submit();
    }
}

function Result(message, element, dom)
{
    this.message = message;
    this.description = message;
    this.element = element;
    this.dom = dom;
}

function buildImportOptions()
{
    var result = new Result("", null,null);

    var dom;
    
    var xmlStr = "<%importopts = xmlImportOptions.replaceAll("\\\\","\\\\\\\\");out.print(importopts);%>";

    if(isIE)
    {
      dom = oImportOptions.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlStr,"text/xml");
    }
    
    var node;

    var form = document.oDummyForm;

    node = dom.selectSingleNode("/importOptions/syncOptions");
    while (node.hasChildNodes())
    {
        node.removeChild(node.firstChild);
    }

    var syncMode = dom.createElement("syncMode");

    for (var i=0; i<3; i++)
    {
        if (document.oDummyForm.oSync[i].checked)
        {
            syncMode.text = Action[i];
        }
    }

    node.appendChild(syncMode);

    node = dom.selectSingleNode("//localeOptions/selectedSource");
    node.text = form.sourceLocale.options[
      form.sourceLocale.selectedIndex].value;

    node = dom.selectSingleNode("//localeOptions/selectedTargets");
    while (node.hasChildNodes())
    {
        node.removeChild(node.firstChild);
    }

    var options = form.targetLocales.options;
    for (var i = 0; i < options.length; i++)
    {
       var option;
       if(isIE)
       {
        option = options.item(i);
       } else {
        option = options[i];
       }

        if (option.selected)
        {
            var locale = dom.createElement("locale");
            locale.text = option.value;
            node.appendChild(locale);

            // If this is the "all" option, stop.
            if (i == 0) break;
        }
    }
    
    result.dom = dom;

    return result;
}

function parseImportOptions()
{
    var dom;
    var xmlStr = "<%=xmlImportOptions.replace("\\","\\\\")%>";

    if(isIE)
    {
      dom = oImportOptions.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlStr,"text/xml");
    }
    var nodes, node, fileName, fileType, fileEncoding;

    var form = document.oDummyForm;

    var count = dom.selectSingleNode("//fileOptions/entryCount").text;
    idEntryCount.innerText = count;

    nodes = dom.selectNodes("/importOptions/syncOptions");
    if (nodes.length > 0)
    {
        if(isIE)
	    {
	      node = nodes.item(0);
	    } 
	    else 
	    {
	      node = nodes[0];
	    }

        if (node.selectSingleNode("syncMode")) {
            syncMode = node.selectSingleNode("syncMode").text;
        }

        var id = MapMode[syncMode];
        if (id)
        {
            form.oSync[id].checked = 'true';
        }
    }

    nodes = dom.selectNodes("//localeOptions/sourceLocales/locale");
    for (var i = 0; i < nodes.length; i++)
    {
        if(isIE)
	    {
	      node = nodes.item(i);
	    } 
	    else 
	    {
	      node = nodes[i];
	    }
        addSelectOption(form.sourceLocale, node.text);
    }

    nodes = dom.selectNodes("//localeOptions/targetLocales/locale");
    for (var i = 0; i < nodes.length; i++)
    {
        if(isIE)
	    {
	      node = nodes.item(i);
	    } 
	    else 
	    {
	      node = nodes[i];
	    }
        addSelectOption(form.targetLocales, node.text);
    }
}

function addSelectOption(select, value)
{
    var options = select.options;
    var option = document.createElement('OPTION');
    option.text = value + ' ';
    option.value = value;
    if(isIE) {
      var options = select.options;
      options.add(option);
    } else {
      select.appendChild(option);
    }
}

function doOnLoad()
{
   checkAnalysisError();


   // Load the Guides
   loadGuides();

   parseImportOptions();
}

function lookFile(url)
{
  window.open(url);
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad()" LEFTMARGIN="0" RIGHTMARGIN="0"
        TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
        CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<%
String errorTusUrl = (String)sessionMgr.getAttribute(TmImportPageHandler.ERROR_URL);
String logUrl = (String)sessionMgr.getAttribute(TmImportPageHandler.LOG_URL);

        int index = errorTusUrl.indexOf("_Imports_");
        if (index > 0)
        {
            errorTusUrl = errorTusUrl.substring(index);
        }
        
        errorTusUrl = errorTusUrl.replace('\\','/');
        
        index = logUrl.indexOf("_Imports_");
        if (index > 0)
        {
            logUrl = logUrl.substring(index);
        }
        
        logUrl = logUrl.replace('\\','/');
        
String errorTus = (String)sessionMgr.getAttribute(ImportUtil.ERROR_COUNT);
String totalTus = (String)sessionMgr.getAttribute(ImportUtil.TOTAL_COUNT);
%>

<%if (logUrl != null && logUrl.length() > 0 && !errorTus.equals("0")) {%>
<%boolean isOne = errorTus.equals("1"); %>
<DIV CLASS="mainHeading">
<%=bundle.getString("lb_message") %>
</DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
  <TR>
    <TD style="color: red;" WIDTH=500><%=MessageFormat.format(bundle.getString("lb_import_ignored_tus"), errorTus) %></TD>
  </TR>
  <TR>
    <TD>&nbsp;</TD>
  </TR>
  <!-- 
  <%if (errorTusUrl != null && errorTusUrl.length() > 0) {%>
  <TR>
    <TD>    
    <A HREF="#" CLASS="standardHREF" onClick="lookFile('<%=errorTusUrl%>');"><%=bundle.getString("lb_click_here_to_view")%> <%=isOne?"it":"them" %>.</A>
    </TD>
  </TR>
  <%}%>
   -->
  <TR>
    <TD>    
    <A CLASS="standardHREF" HREF="#" onClick="lookFile('<%=logUrl%>');"><%=bundle.getString("lb_click_here_to_view")%> <%=isOne?"it":"them" %>.</A>
    </TD>
 </TR>
     
</TABLE>
<BR>
<%}%>

<DIV CLASS="mainHeading" id="idHeading">
<%=bundle.getString("lb_terminology_set_import_options")%>
</DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500>
      <%=bundle.getString("helper_text_tm_import_options")%>
    </TD>
  </TR>
</TABLE>
<BR>

<XML id="oImportOptions" style="display:none"><%=xmlImportOptions%></XML>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="importoptions"
 VALUE="ImportOptions XML goes here"></INPUT>
</FORM>

<DIV style="display:none">
File contains <SPAN id="idEntryCount" style="font-weight: bold"></SPAN> TUs.
<BR>
</DIV>

<FORM NAME="oDummyForm">
<DIV>
  <table>
    <col CLASS="standardText" valign="top">
    <col CLASS="standardText" valign="top">
    <col CLASS="standardText" valign="top">
    <col CLASS="standardText" valign="top">
    <tr>
      <td valign="top"><font class="standardText"><%=bundle.getString("lb_import_source_locale") %>:</font></td>
      <td>
	<select name="sourceLocale" id="idSourceLocale" size="3">
	  <option value="all" selected><%=bundle.getString("lb_all") %></option>
	</select>
      </td>
      <td valign="top"><font class="standardText">&nbsp;&nbsp; <%=bundle.getString("lb_import_target_locales") %>:</font></td>
      <td>
	<select name="targetLocales" id="idTargetLocale" size="3" multiple>
	  <option value="all" selected><%=bundle.getString("lb_all") %></option>
	</select>
      </td>
    </tr>
  </table>
</DIV>
<BR>
<DIV>
  <%=bundle.getString("lb_synchronization_options") %>:<BR>
  <input type="radio" name="oSync" id="idSync1" CHECKED="true">
  <label for="idSync1">
    <%=bundle.getString("lb_tm_merge_tu_with_existing_tu")%>
  </label></input><BR>
  <input type="radio" name="oSync" id="idSync2" style="display:none">
  <label for="idSync2" style="display:none">
    <%=bundle.getString("lb_tm_overwrite_existing_tus")%>
  </label></input>
  <input type="radio" name="oSync" id="idSync3">
  <label for="idSync3">
    <%=bundle.getString("lb_tm_discard_tus")%>
  </label></input>
</DIV>
</FORM>

<P>
<DIV id="idButtons">
<INPUT TYPE="BUTTON" NAME="Previous" VALUE="<%=bundle.getString("lb_previous")%>"
 ONCLICK="doPrev()">
<INPUT TYPE="BUTTON" NAME="Cancel" VALUE="<%=bundle.getString("lb_cancel")%>"
 ONCLICK="doCancel()">
<INPUT TYPE="BUTTON" NAME="Import" VALUE="<%=bundle.getString("lb_import")%>"
 ONCLICK="doNext()">
</DIV>

</FORM>
<BR><BR>
<TABLE>
<TR><TD ALIGN="LEFT"><IMG SRC="/globalsight/images/TMX.gif"></TD></tr>
<TR><TD ALIGN="LEFT"><SPAN CLASS="smallText"><%=bundle.getString("lb_tmx_logo_text1")%><BR><%=bundle.getString("lb_tmx_logo_text2")%></SPAN></TD></TR>
</TABLE>
</DIV>
</BODY>
</HTML>
