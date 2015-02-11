<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.terminology.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prevXml" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prevCsv" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_DEFINITION);
String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_IMPORT_OPTIONS);

String urlNext    = next.getPageURL();
String urlPrevXml = prevXml.getPageURL();
String urlPrevCsv = prevCsv.getPageURL();
String urlCancel  = cancel.getPageURL();

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_import_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);

String lb_heading = bundle.getString("lb_terminology_set_import_options");
String lb_helptext = bundle.getString("helper_text_terminology_import_options");
String lb_failedToAnalyze = bundle.getString("jsmsg_tb_import_failed_analyze");

String lb_addAsNew = bundle.getString("lb_terminology_add_all_concepts_as_new");
String lb_syncConcept = bundle.getString("lb_terminology_synchronize_on_concept_id");
String lb_syncLanguage = bundle.getString("lb_terminology_synchronize_on_language");
String lb_overwrite = bundle.getString("lb_terminology_overwrite_existing_concepts");
String lb_merge = bundle.getString("lb_terminology_merge_new_and_existing_concepts");
String lb_discard = bundle.getString("lb_terminology_discard_new_concepts");

String lb_cancel = bundle.getString("lb_cancel");
String lb_previous = bundle.getString("lb_previous");
String lb_import = bundle.getString("lb_import");
%>
<HTML XMLNS:gs>
<!-- This is envoy\terminology\management\importOptions.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_terminology_import_options")%></TITLE>
<STYLE>
FORM         { display: inline; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/importOptions.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_import_options")%>";
var xmlDefinition = 
				'<%=xmlDefinition.replace("'", "\\'").trim()%>';
var xmlImportOptions = 
				'<%=xmlImportOptions.replace("\\", "\\\\").replace("'", "\\'").trim()%>';

eval("<%=errorScript%>");

function disableOtherOptions(obj)
{
	if (obj.id == "idSync1")
	{

		document.getElementById('idSync2O').checked = false;
		document.getElementById('idSync2M').checked = false;
		document.getElementById('idSync2D').checked = false;
		document.getElementById('idSync3O').checked = false;
		document.getElementById('idSync3M').checked = false;
		document.getElementById('idSync3D').checked = false;
		document.getElementById('idNosync3D').checked = false;
		document.getElementById('idNosync3A').checked = false;
	}
	else if (obj.id == "idSync2")
	{
		document.getElementById('idSync2O').checked = true;
		document.getElementById('idSync3O').checked = false;
		document.getElementById('idSync3M').checked = false;
		document.getElementById('idSync3D').checked = false;
		if (document.getElementById('idNosync3A').checked == false 
				&& document.getElementById('idNosync3D').checked == false)
		{
			document.getElementById('idNosync3A').checked = true;
		}
	}
	else if (obj.id == "idSync3")
	{
		document.getElementById('idSync3O').checked = true;
		document.getElementById('idSync2O').checked = false;
		document.getElementById('idSync2M').checked = false;
		document.getElementById('idSync2D').checked = false;
		if (document.getElementById('idNosync3A').checked == false 
				&& document.getElementById('idNosync3D').checked == false)
		{
			document.getElementById('idNosync3A').checked = true;
		}
	}
	else if (obj.id == "idSync2O" || obj.id == "idSync2M" || obj.id == "idSync2D")
	{
		document.getElementById('idSync2').checked = true;
		document.getElementById('idSync3O').checked = false;
		document.getElementById('idSync3M').checked = false;
		document.getElementById('idSync3D').checked = false;
		if (document.getElementById('idNosync3A').checked == false 
				&& document.getElementById('idNosync3D').checked == false)
		{
			document.getElementById('idNosync3A').checked = true;
		}
	}
	else if (obj.id == "idSync3O" || obj.id == "idSync3M" || obj.id == "idSync3D")
	{
		document.getElementById('idSync3').checked = true;
		document.getElementById('idSync2O').checked = false;
		document.getElementById('idSync2M').checked = false;
		document.getElementById('idSync2D').checked = false;
		if (document.getElementById('idNosync3A').checked == false 
				&& document.getElementById('idNosync3D').checked == false)
		{
			document.getElementById('idNosync3A').checked = true;
		}
	}
}

function doCancel()
{
    window.navigate("<%=urlCancel%>");
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
        var url;
        var dom;
        dom = $.parseXML(xmlImportOptions);
        
        var node = $(dom).find("importOptions fileOptions fileType");
		alert("doPrev--node.text()="+node.text());
        if (node.text() == "<%=ImportOptions.TYPE_XML%>" ||
            node.text() == "<%=ImportOptions.TYPE_MTF%>")
        {
            url = "<%=urlPrevXml%>";
        }
        else if (node.text() == "<%=ImportOptions.TYPE_CSV%>")
        {
            url = "<%=urlPrevCsv%>";
        }
        else if (node.text() == "<%=ImportOptions.TYPE_TBX%>")
        {
        	url = "<%=urlPrevXml%>";
        }

        oForm.action = url +
            "&<%=WebAppConstants.TERMBASE_ACTION%>" +
            "=<%=WebAppConstants.TERMBASE_ACTION_SET_IMPORT_OPTIONS%>";

        oForm.importoptions.value = getDomString(result.domImportOptions);
        
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
    	var dom;
        dom = $.parseXML(xmlImportOptions);
        
    	var node = $(dom).find("importOptions fileOptions fileType");
    	
    	if (node.text() == "<%=ImportOptions.TYPE_TBX%>")
    	{
    		oForm.action = "<%=urlNext +
  	          "&" + WebAppConstants.TERMBASE_ACTION +
  	          "=" + WebAppConstants.TERMBASE_ACTION_START_IMPORT%>";
    	}
    	else
    	{
    		oForm.action = "<%=urlNext +
    	          "&" + WebAppConstants.TERMBASE_ACTION +
    	          "=" + WebAppConstants.TERMBASE_ACTION_START_IMPORT%>";
    	}

         oForm.importoptions.value = getDomString(result.domImportOptions);

         oForm.submit();
    }
}

function checkAnalysisError()
{
    var dom;
    dom = $.parseXML(xmlImportOptions);
    
    var node = $(dom).find("importOptions fileOptions errorMessage");
    var errorMessage = $(node).text();
    if (errorMessage != "")
    {
        node.text("");

        alert("<%=EditUtil.toJavascript(lb_failedToAnalyze)%>" + errorMessage);
        
        var url;
        node = $(dom).find("importOptions fileOptions fileType");

        if ($(node).text() == "<%=ImportOptions.TYPE_XML%>" ||
        	$(node).text() == "<%=ImportOptions.TYPE_MTF%>" ||
        	$(node).text() == "<%=ImportOptions.TYPE_TBX%>")
        {
            url = "<%=urlPrevXml%>";
        }
        else if ($(node).text() == "<%=ImportOptions.TYPE_CSV%>")
        {
            url = "<%=urlPrevCsv%>";
        }

        oForm.action = url + "&<%=WebAppConstants.TERMBASE_ACTION%>=<%=WebAppConstants.TERMBASE_ACTION_IMPORT%>&<%=WebAppConstants.RADIO_BUTTON%>=<%=(String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID)%>";

        oForm.submit();
    }
}

function Result(message, element)
{
    this.message = message;
    this.description = message;
    this.element = element;
    this.domImportOptions = null;
}

function buildImportOptions()
{
    var dom,node;
    
    dom = $.parseXML(xmlImportOptions);
    
    var result = new Result("", null);
    node = $(dom).find("importOptions syncOptions");
    var len = node.children().length;
    while(len)
    {
		node.children().eq(0).remove();
		len = node.children().length;
	}
    
	var syncMode,syncLanguage,syncAction,nosyncAction;
    syncMode = dom.createElement("syncMode");
    syncLanguage = dom.createElement("syncLanguage");
    syncAction = dom.createElement("syncAction");
    nosyncAction = dom.createElement("nosyncAction");
    
    node.append(syncMode);
    node.append(syncLanguage);
    node.append(syncAction);
    node.append(nosyncAction);
    
    syncMode = $(node).find("syncMode");
    syncLanguage = $(node).find("syncLanguage");
    syncAction = $(node).find("syncAction");
    nosyncAction = $(node).find("nosyncAction");
    
    var form = document.oDummyForm;

    for (var i = 0; i < 3; i++)
    {
        if (form.oSync[i].checked)
        {
            syncMode.text(Mode[i]);
            if (Mode[i] == "add_as_new")
            {
            	syncAction.text("");
            	syncLanguage.text("");
            }
            if (Mode[i] == "sync_on_concept")
            {
                syncLanguage.text("");

                for (var j = 0; j < 3; j++)
                {
                    if (form.oSyncId[j].checked)
                    {
                    	syncAction.text(Action[j]);
                    }
                }
                
                for (var j = 0; j < 2; j++)
                {
                    if (form.oNosyncLang[j].checked)
                    {
                    	nosyncAction.text(NoAction[j]);
                    }
                }
            }
            if (Mode[i] == "sync_on_language")
            {
                var lang = form.oLanguage.options[form.oLanguage.selectedIndex].value;
                syncLanguage.text(lang);

                for (var j = 0; j < 3; j++)
                {
                    if (form.oSyncLang[j].checked)
                    {
                    	syncAction.text(Action[j]);
                    }
                }

                for (var j = 0; j < 2; j++)
                {
                    if (form.oNosyncLang[j].checked)
                    {
                    	nosyncAction.text(NoAction[j]);
                    }
                }
            }
        }
    }

    result.domImportOptions=dom;
    return result;
}

function parseImportOptions()
{
    var dom;
    dom = $.parseXML(xmlImportOptions);
    
    var nodes, node, fileName, fileType, fileEncoding, separater, ignoreHeader;
    nodes = $(dom).find("importOptions syncOptions");
    
    if (nodes.length > 0)
    {
        node = nodes[0];//node = nodes.item(0);

        if ($(node).find("syncMode"))
        {
            syncMode = $(node).find("syncMode").text();
        }
        if ($(node).find("syncLanguage"))
        {
            syncLanguage = $(node).find("syncLanguage").text();
        }
        if ($(node).find("syncAction"))
        {
            syncAction = $(node).find("syncAction").text();
        }
        if ($(node).find("nosyncAction"))
        {
            nosyncAction = $(node).find("nosyncAction").text();
        }

        var form = document.oDummyForm;
        var id = MapMode[syncMode];
        if (id)
        {
            form.oSync[id].checked = 'true';

            id = MapMode[syncAction];
            if (syncMode == "sync_on_concept")
            {
                form.oSyncId[id].checked = 'true';
            }
            else
            {
                form.oSyncLang[id].checked = 'true';

                selectValue(form.oLanguage, syncLanguage);

                if (nosyncAction == NoAction[0])
                {
                    form.oNosyncLang[0].checked = 'true';
                }
                else
                {
                    form.oNosyncLang[1].checked = 'true';
                }
            }
        }
    }
}

function selectValue(select, value)
{
    var options = select.options;
    for (var i = 0; i < options.length; ++i)
    {
        if (options[i].value == value)
        {
            select.selectedIndex = i;
            return;
        }
    }
}

function doOnLoad()
{
    checkAnalysisError();

    // Load the Guides
    loadGuides();

    var dom;
    dom = $.parseXML(xmlDefinition);
    var names = $(dom).find("definition languages language name");

    for (var i = 0; i < names.length; ++i)
    {
        var name = $(names[i]).text();

        oOption = document.createElement("OPTION");
        oOption.text = name;
        oOption.value = name;

        oDummyForm.oLanguage.add(oOption);
    }

    parseImportOptions();
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

<SPAN CLASS="mainHeading" id="idHeading"><%=lb_heading%></SPAN>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500><%=lb_helptext%></TD>
</TR>
</TABLE>
<P>

<DIV style="display:none">
<XML id="oDefinition"><%=xmlDefinition%></XML>
<XML id="oImportOptions"><%=xmlImportOptions%></XML>
</DIV>

<DIV>
<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="importoptions" VALUE="ImportOptions XML goes here">
</FORM>
<FORM NAME="oDummyForm">
<input type="radio" name="oSync" id="idSync1" CHECKED onclick="disableOtherOptions(this)">
<label for="idSync1"><%=lb_addAsNew%></label>
</DIV>
<DIV>
<input type="radio" name="oSync" id="idSync2" onclick="disableOtherOptions(this)">
<label for="idSync2"><%=lb_syncConcept%></label>
  <DIV style="margin-left: 40px">
  <input type="radio" name="oSyncId" id="idSync2O" onclick="disableOtherOptions(this)">
  <label for="idSync2O"><%=lb_overwrite%></label><br>
  <input type="radio" name="oSyncId" id="idSync2M" onclick="disableOtherOptions(this)">
  <label for="idSync2M"><%=lb_merge%></label><br>
  <input type="radio" name="oSyncId" id="idSync2D" onclick="disableOtherOptions(this)">
  <label for="idSync2D"><%=lb_discard%></label><br>
  </DIV>
</DIV>
<DIV>
<input type="radio" name="oSync" id="idSync3" onclick="disableOtherOptions(this)">
<label for="idSync3"><%=lb_syncLanguage%></label>
  <select name="oLanguage" id="idLanguageList" ></select>
  <DIV style="margin-left: 40px">
    <input type="radio" name="oSyncLang" id="idSync3O" onclick="disableOtherOptions(this)">
    <label for="idSync3O"><%=lb_overwrite%></label><br>
    <input type="radio" name="oSyncLang" id="idSync3M" onclick="disableOtherOptions(this)">
    <label for="idSync3M"><%=lb_merge%></label><br>
    <input type="radio" name="oSyncLang" id="idSync3D" onclick="disableOtherOptions(this)">
    <label for="idSync3D"><%=lb_discard%></label><br>
  </DIV>
<input type="radio" name="foo" id="idFoo" style="visibility: hidden">
<%=bundle.getString("lb_unsynchronized_entries") %>: &nbsp;
    <input type="radio" name="oNosyncLang" id="idNosync3D">
    <label for="idNosync3D"><%=bundle.getString("lb_discard") %></label>
    <input type="radio" name="oNosyncLang" id="idNosync3A">
    <label for="idNosync3A"><%=bundle.getString("lb_add") %></label> &nbsp;
</DIV>
</FORM>

<P>
<DIV id="idButtons">
<INPUT TYPE="BUTTON" NAME="Cancel" VALUE="<%=lb_cancel%>" onclick="doCancel()">
<INPUT TYPE="BUTTON" NAME="Previous" VALUE="<%=lb_previous%>" onclick="doPrev()">
<INPUT TYPE="BUTTON" NAME="Import" VALUE="<%=lb_import%>" onclick="doNext()">
</DIV>

</FORM>

</DIV>
</BODY>
</HTML>
