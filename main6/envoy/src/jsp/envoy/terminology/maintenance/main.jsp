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
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="searchResult" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String str_tbid =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);
String str_tbname =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);
String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_DEFINITION);

String urlCancel = cancel.getPageURL();
String urlSearch = searchResult.getPageURL();

String lb_title = bundle.getString("lb_termbase_maintenance"); 
String lb_helptext = bundle.getString("helper_text_tb_maintenance_main"); 
String lb_search = bundle.getString("lb_search");
String lb_browse = bundle.getString("lb_browse_termbase");
String lb_cancel = bundle.getString("lb_previous");
sessionMgr.removeElement(WebAppConstants.TERMBASE_STATUS);
%>
<HTML>
<!-- This is envoy\src\jsp\envoy\terminology\maintenance\main.jsp -->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
FORM { display: inline; }
</STYLE>
<link type="text/css" rel="stylesheet" href="/globalsight/dijit/themes/tundra/tundra.css">
<link type="text/css" rel="stylesheet" href="/globalsight/includes/dojo.css">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/dojo/dojo.js"></SCRIPT>

<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_maintenance_main")%>";

var tbid = "<%=str_tbid%>";
var name = "<%=str_tbname%>";
var aLanguages = new Array();
var aFields = new Array();
var xmlStr = "<%=xmlDefinition%>";

//dojo.require("dojo.parser");
//dojo.require("dijit.dijit");
 //dojo.require("dojo.widget.ComboBox");
dojo.require("dijit.form.ComboBox");


function setup(select, level, definedFields)
{
  g_currentFields = new Array().concat(definedFields);

  if (g_currentFields.length == 0)
  {
    if (level == 'concept')
    {
      g_currentFields = g_currentFields.concat(g_conceptFields);
    }
    else if (level == 'language')
    {
      g_currentFields = g_currentFields.concat(g_languageFields);
    }
    else if (level == 'term')
    {
      g_currentFields = g_currentFields.concat(g_termFields);
    }
    else if (level == 'field')
    {
      g_currentFields = g_currentFields.concat(g_fieldFields);
    }
    else if (level == 'source')
    {
      g_currentFields = g_currentFields.concat(g_sourceFields);
    }
  }

  // override field and source level fields
  if (level == 'field')
  {
    g_currentFields = g_fieldFields;
  }
  else if (level == 'source')
  {
    g_currentFields = g_sourceFields;
  }

  order(g_currentFields) ;
  fillSelect(select, g_currentFields);
}

function clearSelect(select)
{
    var options = select.options;

    if (options && options.length)
    {
        for (var i = options.length; i >= 1; --i)
        {
            //options.remove(i-1);
            options[i-1] = null;
        }
    }
}

function fillSelect(select, values)
{
    clearSelect(select);

    var options = select.options;
    
    var option = document.createElement('OPTION');
        option.text = ' ';
        option.value = '';
     options.add(option);

    for (var i = 0; i < values.length; i++)
    {
        var value = values[i];

        var option = document.createElement('OPTION');
        option.text = value.getDisplayName() + ' ';
        option.value = value.getType();
        options.add(option);
    }
}

function showLanguages()
{
    showLanguages1(document.getElementById('idLanguage3'));
    showLanguages1(document.getElementById('idLanguage4'));
}

function showLanguages1(select)
{
    for (var i = 0; i < aLanguages.length; i++)
    {
        var lang = aLanguages[i];

        var option = document.createElement("OPTION");
        option.text = lang.name;
        option.value = lang.name;
        select.add(option);
    }
}

function showFields1(select1, select2)
{
    var value = select1.options[select1.selectedIndex].value;

    var index = parseInt(select1.id.charAt(select1.id.length - 1));
    var fieldTypes;

    switch (index)
    {
    case 1: fieldTypes = g_entryFields; break;    // entry
    case 2: fieldTypes = g_conceptFields; break;  // concept
    case 3: fieldTypes = g_languageFields; break; // language
    case 4: fieldTypes = g_termFields; break;     // term
    }

    // TODO: take user-defined fields (and their field types) into account.

    if (value == 'attr')
    {
        fillSelect(select2, getAttributeFields(fieldTypes));
    }
    else if (value == 'text')
    {
        fillSelect(select2, getTextFields(fieldTypes));
    }
    else if (value.startsWith('all'))
    {
        clearSelect(select2);

        var option = document.createElement("OPTION");
        option.text = 'N/A';
        option.value = '';
        select2.add(option);
    }
}

function getAttributeFields(fieldTypes)
{
    var result = new Array();

    for (var i = 0; i < fieldTypes.length; i++)
    {
        var fieldType = fieldTypes[i];

        if (fieldType.isAttribute())
        {
            result.push(fieldType);
        }
    }

    return result;
}

function getTextFields(fieldTypes)
{
    var result = new Array();

    for (var i = 0; i < fieldTypes.length; i++)
    {
        var fieldType = fieldTypes[i];

        if (fieldType.isText())
        {
            result.push(fieldType);
        }
    }

    return result;
}

function parseDefinition()
{
    var nodes, node;
    var dom = $.parseXML(xmlStr);;

    nodes = $(dom).find("definition languages language");
    for (var i = 0; i < nodes.length; i++)
    {
        node = nodes[i];

        var name = $(node).find("name").text();
        var locale = $(node).find("locale").text();
        var hasterms = $(node).find("hasterms").text();
        hasterms = (hasterms == "true" ? true : false);
        var exists = true;

        aLanguages.push(new Language(name, locale, hasterms, exists));
    }

    nodes = $(dom).find("definition fields field");
    for (var i = 0; i < nodes.length; i++)
    {
        node = nodes[i];

        var name = $(node).find("name").text();
        var type = $(node).find("type").text();
        var system = ($(node).find("system").text() == "true" ? true : false);
        var values = $(node).find("values").text();
        var format = getFieldFormatByType(type);

        aFields.push(new Field(name, type, format, system, values));
    }

    var idFieldConcept = document.getElementById("idFieldConcept");
    var idFieldLanguage = document.getElementById("idFieldLanguage");
    var idFieldTerm = document.getElementById("idFieldTerm");
    setup(idFieldConcept, 'concept', aFields);
    setup(idFieldLanguage, 'language', aFields);
    setup(idFieldTerm, 'term', aFields);
}

function order(arrayList) 
{
    for (var i = 1; i < arrayList.length; i++) {
        for (var j = 0; j < arrayList.length - i; j++) {
            
            if (arrayList[j].getDisplayName().toLowerCase() > arrayList[j + 1].getDisplayName().toLowerCase()) {
                swap(arrayList, j, j + 1);
            }
        }
    }
}

function swap(arrayList, x, y) {
    var temp = arrayList[x];
    arrayList[x] = arrayList[y];
    arrayList[y] = temp;
}

function initUI()
{
    showLanguages();
    document.getElementById('idSearch').focus();
}

function fieldSelected(select)
{
    var fselect = select;
    var index = parseInt(select.id.charAt(select.id.length - 1));
    var fnselect = document.getElementById('idFieldName' + index);

    showFields1(fselect, fnselect);
}

function doCancel()
{
    window.location.href = '<%=urlCancel%>';
}

function doSearch()
{
    var form = document.getElementById('idForm');
    var urlPara = '<%=urlSearch%>';
    var ctrl, val, index;

    ctrl = document.getElementById('idSearch');
    val = ctrl.value;
    if (!val)
    {
        alert("<%=bundle.getString("jsmsg_tb_maintenance_enter_value") %>");
        ctrl.focus();
        return;
    }

    var data = encodeURIComponent(document.getElementById('idSearch').value).replace(/%C2%A0/g, "%20");
    urlPara = urlPara + '&<%=WebAppConstants.TERMBASE_SEARCH%>=' + data;
    var type;

    if (document.getElementById('idLevelConcept').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_CONCEPT%>';
        index = 2;
        type = document.getElementById("idFieldConcept").value;
    }
    else if (document.getElementById('idLevelLanguage').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_LANGUAGE%>';
        index = 3;
        type = document.getElementById("idFieldLanguage").value;
    }
    else if (document.getElementById('idLevelTerm').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_TERM%>';
        index = 4;
        type = document.getElementById("idFieldTerm").value;
    }
    else
    {
        alert("<%=bundle.getString("jsmsg_tb_maintenance_select_search_level") %>");
        return;
    }

    urlPara = urlPara + '&<%=WebAppConstants.TERMBASE_LEVEL%>=' + val;

    if (index == 3 || index == 4)
    {
      fselect  = document.getElementById('idLanguage' + index);
      urlPara = urlPara + '&<%=WebAppConstants.TERMBASE_LANGUAGE%>=' + fselect.options[fselect.selectedIndex].value;
    }
    
    

    urlPara = urlPara + '&<%=WebAppConstants.TERMBASE_ACTION%>=' + '<%=WebAppConstants.TERMBASE_ACTION_SEARCH%>';
    urlPara = urlPara + '&type=' + type;

    urlPara = urlPara + "&caseinsensitive="+ searchForm.caseinsensitive.checked;
    urlPara = urlPara + "&wordOnly="+ searchForm.wordOnly.checked;

    if(navigator.userAgent.indexOf("Firefox")>0)
    {
        idFrame.src= urlPara;
    }
    else
    {
        document.getElementById("idFrame").src= urlPara;
    }
    
    document.getElementById("replaceButton").disabled = false;

}

function browseTermbase()
{
    ShowTermbase(tbid);
}

function doLoad()
{
    // This loads the guides in guides.js and the
    loadGuides();
    //alert("Function loadGuides:\tFinished");
    parseDefinition();
    //alert("Function parseDefinition:\tFinished");
    initUI();
}

function replace() {
    var value = document.getElementById('idReplace').value;
    if(value == "") {
        alert("Please input the replace content!")
        return;
    }
    
	if(navigator.userAgent.indexOf("Firefox")>0)
	{
        idFrame.contentWindow.doReplace(value);
    }
	else
	{
		document.getElementById("idFrame").contentWindow.doReplace(value);
	}
 
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<XML id="oDefinition" style="display:none"><%=xmlDefinition%></XML>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538><%=lb_helptext%></TD>
  </TR>
</TABLE>

<BR>

<FORM id="idForm" name="form" method="post" action="<%=urlSearch%>">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_ACTION%>"
 value="<%=WebAppConstants.TERMBASE_ACTION_SEARCH%>">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_SEARCH%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_CASEINSENSITIVE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_SMARTREPLACE%>" value="false">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_LEVEL%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_LANGUAGE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_FIELD%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_FIELDNAME%>" value="">
</FORM>

<FORM id="searchForm" name="searchForm" method="post" action="<%=urlSearch%>">
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="2">
  <TR>
    <TD>
      <table>
        <col class="standardText">
        <col class="standardText">

        <tr>
            <td align="right" style="padding-right: 3px" class="standardText"><%=bundle.getString("lb_termbase1")%></td>
            <td class="standardText"><%=str_tbname%></td>
        </tr>
        <tr>
          <td align="right" style="padding-right: 3px" class="standardText"><%=bundle.getString("lb_search_for") %>:</td>
          <td align="left">
            <input class="standardText" type="text" size="50" id="idSearch" name="search">
          </td>
        </tr>
        <tr>
          <td align="right" style="padding-right: 3px" class="standardText">&nbsp;</td>
          <td align="left">
            <input class="standardText" type="checkbox" id="caseinsensitive"
            name="caseinsensitive">
            <label for="idCaseSensitive" class="standardText"><%=bundle.getString("lb_tm_search_match_case") %></label>
            <input class="standardText" type="checkbox" id="wordOnly"
            name="wordOnly">
            <label for="lableWordOnly" class="standardText"><%=bundle.getString("lb_tm_search_word_only") %></label>
          </td>
        </tr>
      </table>
      <table>
        <col class="standardText">
        <col class="standardText">

        <tr>
          <td align="left">
            <input type="radio" name="level" value="concept" id="idLevelConcept">
            <label for="idLevelConcept" class="standardText"><%=bundle.getString("lb_termbase_filter_concept_level") %></label>
          </td>
          <td align="left">
            <select id="idFieldConcept" name="idFieldsConcept"></select>
          </td>
        </tr>
        <tr>
          <td align="left">
            <input type="radio" name="level" value="language" id="idLevelLanguage">
            <label for="idLevelLanguage" class="standardText"><%=bundle.getString("lb_termbase_filter_lang_level") %></label>
            <select id="idLanguage3" name="language"
              onchange="idLevelLanguage.click()"></select>
          </td>
          <td align="left">
            <select id="idFieldLanguage" name="idFieldLanguage"></select>
          </td>
        </tr>
        <tr>
          <td align="left">
            <input type="radio" name="level" value="term" id="idLevelTerm">
            <label for="idLevelTerm" class="standardText"><%=bundle.getString("lb_termbase_filter_term_level") %></label>
            <select id="idLanguage4" name="language"
              onchange="idLevelTerm.click()"></select>
          </td>
          <td align="left">
            <select  dojoType="dijit.form.ComboBox" id="idFieldTerm" name="idFieldTerm" autocomplete="true"
      hasDownArrow="true"></select>
            
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD>&nbsp;</TD>
  </TR>
  <TR>
    <TD>

    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_cancel%>"
     ID="idCancelBtn" onclick="doCancel()">
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_browse%>"
     ID="idBrowseBtn" onclick="browseTermbase()">
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_search%>"
     ID="idSearchBtn" onclick="doSearch()">
    </TD>
  </TR>
</TABLE>
</FORM>
<br>
<div>
  <table border=0>
    <tr>
      <td align="left" style="padding-right: 3px" class="standardText"><%=bundle.getString("lb_replace_with") %>:
         <input class="standardText" type="text" size="50" id="idReplace" name="replace">
         <input type="button" id="replaceButton" value="OK" disabled="true"
         onclick="replace();">
      </td>
    </tr>
   <tr><td>
     <IFRAME id="idFrame" src="" style="" width="500"></IFRAME> 
   </td></tr>
</table>
</div>
</DIV>

</BODY>
</HTML>
