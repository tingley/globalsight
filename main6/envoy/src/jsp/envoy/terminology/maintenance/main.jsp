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
<jsp:useBean id="search" scope="request"
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
String urlSearch = search.getPageURL();

String lb_title = bundle.getString("lb_termbase_maintenance"); 
String lb_helptext = bundle.getString("helper_text_tb_maintenance_main"); 
String lb_search = bundle.getString("lb_search");
String lb_browse = bundle.getString("lb_browse_termbase");
String lb_cancel = bundle.getString("lb_cancel");
sessionMgr.removeElement(WebAppConstants.TERMBASE_STATUS);
%>
<HTML>
<!-- This is envoy\src\jsp\envoy\terminology\maintenance\main.jsp -->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
FORM { display: inline; }
</STYLE>

<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/objects_js.jsp"></SCRIPT>
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

function showFields()
{
    var fselect, fnselect;

    fselect = document.getElementById('idField1');
    fnselect = document.getElementById('idFieldName1');
    showFields1(fselect, fnselect);

    fselect = document.getElementById('idField2');
    fnselect = document.getElementById('idFieldName2');
    showFields1(fselect, fnselect);

    fselect = document.getElementById('idField3');
    fnselect = document.getElementById('idFieldName3');
    showFields1(fselect, fnselect);

    fselect = document.getElementById('idField4');
    fnselect = document.getElementById('idFieldName4');
    showFields1(fselect, fnselect);
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

function compareLanguages(p_a, p_b)
{
    var aname = p_a.name;
    var bname = p_b.name;
    if (aname == bname) return 0;
    if (aname > bname) return 1;
    if (aname < bname) return -1;
}

function parseDefinition()
{
    var nodes, node;
    var dom ;

    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
      dom = oDefinition.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlStr,"text/xml");
    }
    nodes = dom.selectNodes("/definition/languages/language");
    for (var i = 0; i < nodes.length; i++)
    {
        //node = nodes.item(i);
        node = nodes[i];

        var name = node.selectSingleNode("name").text;
        var locale = node.selectSingleNode("locale").text;
        var hasterms = node.selectSingleNode("hasterms").text;
        hasterms = (hasterms == "true" ? true : false);
        var exists = true;

        aLanguages.push(new Language(name, locale, hasterms, exists));
    }
    aLanguages.sort(compareLanguages);

    nodes = dom.selectNodes("/definition/fields/field");
    for (var i = 0; i < nodes.length; i++)
    {
        //node = nodes.item(i);
        node = nodes[i];

        var name = node.selectSingleNode("name").text;
        var type = node.selectSingleNode("type").text;
        var system =
            (node.selectSingleNode("system").text == "true" ? true : false);
        var values = node.selectSingleNode("values").text;
        var format = getFieldFormatByType(type);

        aFields.push(new Field(name, type, format, system, values));
    }
}

function initUI()
{
    showLanguages();
    showFields();
    idSearch.focus();
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

    var ctrl, val, index;

    ctrl = document.getElementById('idSearch');
    val = ctrl.value;
    if (!val)
    {
        alert("<%=bundle.getString("jsmsg_tb_maintenance_enter_value") %>");
        ctrl.focus();
        return;
    }

    ctrl = document.getElementById('idReplace');
    val = ctrl.value;
    /*
    if (!val)
    {
        alert("Please enter a value");
        ctrl.focus();
        return;
    }
    */

    form.<%=WebAppConstants.TERMBASE_CASEINSENSITIVE%>.value =
        !document.getElementById('idCaseSensitive').checked;

    form.<%=WebAppConstants.TERMBASE_SEARCH%>.value =
        document.getElementById('idSearch').value;
    form.<%=WebAppConstants.TERMBASE_REPLACE%>.value =
        document.getElementById('idReplace').value;

    if (document.getElementById('idLevelEntry').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_ENTRY%>';
        index = 1;
    }
    else if (document.getElementById('idLevelConcept').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_CONCEPT%>';
        index = 2;
    }
    else if (document.getElementById('idLevelLanguage').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_LANGUAGE%>';
        index = 3;
    }
    else if (document.getElementById('idLevelTerm').checked)
    {
        val = '<%=WebAppConstants.TERMBASE_LEVEL_TERM%>';
        index = 4;
    }
    else
    {
        alert("<%=bundle.getString("jsmsg_tb_maintenance_select_search_level") %>");
        return;
    }

    form.<%=WebAppConstants.TERMBASE_LEVEL%>.value = val;

    if (index == 3 || index == 4)
    {
      fselect  = document.getElementById('idLanguage' + index);
      form.<%=WebAppConstants.TERMBASE_LANGUAGE%>.value =
          fselect.options[fselect.selectedIndex].value;
    }

    fselect  = document.getElementById('idField' + index);
    fnselect = document.getElementById('idFieldName' + index);

    form.<%=WebAppConstants.TERMBASE_FIELD%>.value =
        fselect.options[fselect.selectedIndex].value;
    form.<%=WebAppConstants.TERMBASE_FIELDNAME%>.value =
        fnselect.options[fnselect.selectedIndex].value;

    form.submit();
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

<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="2">
  <TR>
    <TD>
      <table>
        <col class="standardText">
        <col class="standardText">

        <tr>
          <td align="right" style="padding-right: 3px" class="standardText"><%=bundle.getString("lb_search_for") %>:</td>
          <td align="left">
            <input class="standardText" type="text" size="50" id="idSearch" name="search">
          </td>
        </tr>
        <tr>
          <td align="right" style="padding-right: 3px" class="standardText"><%=bundle.getString("lb_replace_with") %>:</td>
          <td align="left">
            <input class="standardText" type="text" size="50" id="idReplace" name="replace">
          </td>
        </tr>
        <tr>
          <td align="right" style="padding-right: 3px" class="standardText">&nbsp;</td>
          <td align="left">
            <input class="standardText" type="checkbox" id="idCaseSensitive"
            name="casesensitive">
            <label for="idCaseSensitive" class="standardText"><%=bundle.getString("lb_tm_search_match_case") %></label>
          </td>
        </tr>
      </table>
      <table>
        <col class="standardText">
        <col class="standardText">

        <!-- TODO: disable this row -->
        <tr style="padding-top: 6px">
          <td align="left" DISABLED style="color:grey;">
            <input type="radio" name="level" value="entry" id="idLevelEntry" DISABLED>
            <label for="idLevelEntry" class="standardText"><%=bundle.getString("lb_termbase_filter_entire_level") %></label>
          </td>
          <td style="padding-left:5px" DISABLED class="standardText"><%=bundle.getString("lb_termbase_in") %>
            <select id="idField1" name="field" DISABLED
              onchange="fieldSelected(this); idLevelEntry.click();">
              <option value="text"><%=bundle.getString("lb_termbase_filter_text") %> </option>
              <option value="attr"><%=bundle.getString("lb_termbase_filter_attr") %> </option>
              <option value="alltexts"><%=bundle.getString("lb_termbase_filter_alltext") %> </option>
              <option value="allattrs"><%=bundle.getString("lb_termbase_filter_allattr") %> </option>
              <option value="allsources"><%=bundle.getString("lb_termbase_filter_allsrc") %> </option>
              <option value="allnotes"><%=bundle.getString("lb_termbase_filter_allnotes") %> </option>
              <option value="all"><%=bundle.getString("lb_termbase_filter_all") %> </option>
            </select>
            <%=bundle.getString("lb_select_field") %>
            <select id="idFieldName1" name="fieldName" DISABLED
              onchange="idLevelEntry.click();">
            </select>
          </td>
        </tr>
        <tr>
          <td align="left">
            <input type="radio" name="level" value="concept" id="idLevelConcept">
            <label for="idLevelConcept" class="standardText"><%=bundle.getString("lb_termbase_filter_concept_level") %></label>
          </td>
          <td style="padding-left:5px" class="standardText"><%=bundle.getString("lb_termbase_in") %>
            <select id="idField2" name="field"
              onchange="fieldSelected(this); idLevelConcept.click();">
              <option value="text"><%=bundle.getString("lb_termbase_filter_text") %> </option>
              <option value="attr"><%=bundle.getString("lb_termbase_filter_attr") %> </option>
              <option value="alltexts"><%=bundle.getString("lb_termbase_filter_alltext") %> </option>
              <option value="allattrs"><%=bundle.getString("lb_termbase_filter_allattr") %> </option>
              <option value="allsources"><%=bundle.getString("lb_termbase_filter_allsrc") %> </option>
              <option value="allnotes"><%=bundle.getString("lb_termbase_filter_allnotes") %> </option>
              <option value="all"><%=bundle.getString("lb_termbase_filter_all") %> </option>
            </select>
            <%=bundle.getString("lb_select_field") %>
            <select id="idFieldName2" name="fieldName"
              onchange="idLevelConcept.click();">
            </select>
          </td>
        </tr>
        <tr>
          <td align="left">
            <input type="radio" name="level" value="language" id="idLevelLanguage">
            <label for="idLevelLanguage" class="standardText"><%=bundle.getString("lb_termbase_filter_lang_level") %></label>
            <select id="idLanguage3" name="language"
              onchange="idLevelLanguage.click()"></select>
          </td>
          <td style="padding-left:5px" class="standardText"><%=bundle.getString("lb_termbase_in") %>
            <select id="idField3" name="field"
              onchange="fieldSelected(this); idLevelLanguage.click();">
              <option value="text"><%=bundle.getString("lb_termbase_filter_text") %> </option>
              <option value="attr"><%=bundle.getString("lb_termbase_filter_attr") %> </option>
              <option value="alltexts"><%=bundle.getString("lb_termbase_filter_alltext") %> </option>
              <option value="allattrs"><%=bundle.getString("lb_termbase_filter_allattr") %> </option>
              <option value="allsources"><%=bundle.getString("lb_termbase_filter_allsrc") %> </option>
              <option value="allnotes"><%=bundle.getString("lb_termbase_filter_allnotes") %> </option>
              <option value="all"><%=bundle.getString("lb_termbase_filter_all") %> </option>
            </select>
            <%=bundle.getString("lb_select_field") %>
            <select id="idFieldName3" name="fieldName"
              onchange="idLevelLanguage.click();">
            </select>
          </td>
        </tr>
        <tr>
          <td align="left">
            <input type="radio" name="level" value="term" id="idLevelTerm">
            <label for="idLevelTerm" class="standardText"><%=bundle.getString("lb_termbase_filter_term_level") %></label>
            <select id="idLanguage4" name="language"
              onchange="idLevelTerm.click()"></select>
          </td>
          <td style="padding-left:5px" class="standardText"><%=bundle.getString("lb_termbase_in") %>
            <select id="idField4" name="field"
              onchange="fieldSelected(this); idLevelTerm.click();">
              <option value="text"><%=bundle.getString("lb_termbase_filter_text") %> </option>
              <option value="attr"><%=bundle.getString("lb_termbase_filter_attr") %> </option>
              <option value="alltexts"><%=bundle.getString("lb_termbase_filter_alltext") %> </option>
              <option value="allattrs"><%=bundle.getString("lb_termbase_filter_allattr") %> </option>
              <option value="allsources"><%=bundle.getString("lb_termbase_filter_allsrc") %> </option>
              <option value="allnotes"><%=bundle.getString("lb_termbase_filter_allnotes") %> </option>
              <option value="all"><%=bundle.getString("lb_termbase_filter_all") %> </option>
            </select>
            <%=bundle.getString("lb_select_field") %>
            <select id="idFieldName4" name="fieldName"
              onchange="idLevelTerm.click();">
            </select>
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

</DIV>
</BODY>
</HTML>
