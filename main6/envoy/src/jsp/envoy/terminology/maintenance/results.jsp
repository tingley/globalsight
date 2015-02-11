<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.terminology.searchreplace.SearchReplaceParams,
        com.globalsight.terminology.searchreplace.SearchResults,
        com.globalsight.terminology.Definition,
        com.globalsight.terminology.util.MappingContext,
        com.globalsight.terminology.util.HtmlUtil,
        com.globalsight.util.edit.EditUtil,
        org.dom4j.Document,
        org.dom4j.Element,
        java.util.ResourceBundle,
        java.util.List"
    session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="back" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="replace" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="replaceall" scope="request"
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

SearchReplaceParams params = (SearchReplaceParams)sessionMgr.getAttribute(
  WebAppConstants.TERMBASE_SEARCHCONDITION);
ProcessStatus m_status = (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.TERMBASE_STATUS);
SearchResults results = (SearchResults)m_status.getResults();

String urlCancel = cancel.getPageURL();
String urlBack = back.getPageURL();
String urlSelf = self.getPageURL();
String urlReplace = replace.getPageURL();
String urlReplaceAll = replaceall.getPageURL();

String lb_title = bundle.getString("lb_termbase_search_results");
String lb_help_text = bundle.getString("helper_text_tb_search_check_result");
String lb_nothing_found = bundle.getString("msg_search_results_nothing_found");
String lb_checkAll = bundle.getString("lb_check_all");
String lb_clearAll = bundle.getString("lb_clear_all");
String lb_cancel = bundle.getString("lb_cancel");
String lb_replace = bundle.getString("lb_replace");
String lb_replaceall = bundle.getString("lb_replace_all");
String lb_previous = bundle.getString("lb_previous");
String lb_id = bundle.getString("lb_id");

int windowMin = results.getWindowStart();
int windowMax = results.getWindowEnd();
int numRecords = results.getNumResults();

%>
<HTML xmlns:m="http://www.w3.org/1998/Math/MathML">
<HEAD>
<TITLE><%=lb_title%></TITLE>
<OBJECT ID="MathPlayer" CLASSID="clsid:32F66A20-7614-11D4-BD11-00104BD3F987"></OBJECT>
<?IMPORT NAMESPACE="m" IMPLEMENTATION="#MathPlayer" ?>
<STYLE>
@import url("/globalsight/envoy/terminology/viewer/viewer.css");
FORM { display: inline; }
.clickable { color: blue; cursor: hand; }
#idHits {
          background-color: #ffffff;
          /*table-layout: fixed;*/
          behavior: url(/globalsight/includes/rowover.htc);
        }

#idHits A {
            color: blue;
            cursor: default;
          }

#idHits .xref {
                behavior: url(/globalsight/envoy/terminology/viewer/bhvrNoXref.htc)
              }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script src="/globalsight/includes/xmlextras.js"></script>
<SCRIPT src="/globalsight/envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_maintenance_results")%>";

var tbid = "<%=str_tbid%>";
var tbname = "<%=str_tbname%>";

function doBack()
{
    window.location.href = '<%=urlBack%>';
}

function doCancel()
{
    window.location.href = '<%=urlCancel%>';
}

function doShowPrevious()
{
  var form = document.getElementById('idNavigationForm');
  fillForm(form);
  form.<%=WebAppConstants.TERMBASE_ACTION%>.value =
    '<%=WebAppConstants.TERMBASE_ACTION_SHOWPREVIOUS%>';
  form.submit();
}

function doShowNext()
{
  var form = document.getElementById('idNavigationForm');
  fillForm(form);
  form.<%=WebAppConstants.TERMBASE_ACTION%>.value =
    '<%=WebAppConstants.TERMBASE_ACTION_SHOWNEXT%>';
  form.submit();
}

function doReplace()
{
  var val = idReplace.value;
  if (!val)
  {
    if (!confirm("<%=bundle.getString("jsmsg_tb_maintenance_replace_empty") %>"))
    {
      idReplace.focus();
      return;
    }
  }

  var form = document.getElementById('idReplaceForm');
  fillForm(form);
  form.<%=WebAppConstants.TERMBASE_ACTION%>.value =
    '<%=WebAppConstants.TERMBASE_ACTION_REPLACE%>';
  form.submit();
}

function doReplaceAll()
{
  var val = idReplace.value;
  if (!val)
  {
    if (!confirm("<%=bundle.getString("jsmsg_tb_maintenance_replace_empty") %>"))
    {
      idReplace.focus();
      return;
    }
  }

  var form = document.getElementById('idReplaceAllForm');
  fillForm(form);
  form.<%=WebAppConstants.TERMBASE_ACTION%>.value =
    '<%=WebAppConstants.TERMBASE_ACTION_REPLACEALL%>';
  form.submit();
}

function submitForm(action)
{
  if (action == 'right')
  {
    doShowNext();
  }
  else if (action == 'left')
  {
    doShowPrevious();
  }
  else if (action == 'cancel')
  {
    doCancel();
  }
  else if (action == 'back')
  {
    doBack();
  }
  else if (action == 'replace')
  {
    doReplace();
  }
  else if (action == 'replaceall')
  {
    doReplaceAll();
  }
}

function fillForm(form)
{
  form.<%=WebAppConstants.TERMBASE_REPLACE%>.value = idReplace.value;
  form.<%=WebAppConstants.TERMBASE_SMARTREPLACE%>.value =
    idSmartReplace.checked;
  form.<%=WebAppConstants.TERMBASE_REPLACEINDEX%>.value = '';

  var indexes = '';
  var dummyform = document.all.idDummyForm;
  for (var i = 0; i < dummyform.length; i++)
  {
    if (dummyform.elements[i].type == 'checkbox')
    {
      if (dummyform.elements[i].checked)
      {
        indexes += ',' + dummyform.elements[i].index;
      }
    }
  }

  form.<%=WebAppConstants.TERMBASE_REPLACEINDEX%>.value = indexes;
}

function setAllCheckBoxes(state)
{
    var form = document.all.idDummyForm;

    for (var i = 0; i < form.length; i++)
    {
        if (form.elements[i].type == 'checkbox')
        {
            form.elements[i].checked = state;
        }
    }

    return false;
}

function browseTermbase()
{
  ShowTermbase(tbid);
}

function browseEntry(conceptid)
{
  ShowTermbaseConcept(tbid, conceptid);
}

function browseTerm(conceptid, termid)
{
  ShowTermbaseConceptTerm(tbid, conceptid, termid);
}


function selectRow()
{
  var index = event.srcRow.rowIndex;

  if (index > 0)
  {
    var radios = termbaseForm.termbaseId;

    if (radios.length)
    {
      radios[index-1].checked = true;
    }
    else
    {
      radios.checked = true;
    }
  }
}

var g_on = false;
function highlightMatches()
{
   g_on = !g_on;
   highlight(idSearch.value, eval('<%=params.isCaseInsensitive()%>'), g_on);
}

function highlight(p_str, p_caseInsensitive, p_on)
{
  var txt, _case;

  if (p_caseInsensitive)
  {
    _case = 0;
  }
  else
  {
    _case = 4;
  }

  txt = document.body.createTextRange();
  txt.moveToElementText(idHitsBody);

  while (txt.findText(p_str, 1, _case))
  {
    if (p_on)
    {
      txt.execCommand('BackColor', 0, 'YELLOW');
    }
    else
    {
      txt.execCommand('BackColor', 0, '');
    }

    txt.collapse(false);
    txt.moveEnd('textedit');
  }
}

function doLoad()
{
  loadGuides();

<% if (numRecords > 0) { %>

  idSearch.value = '<%=EditUtil.toJavascript(params.getSearchText())%>';
  idReplace.value = '<%=EditUtil.toJavascript(params.getReplaceText())%>';
  idSmartReplace.checked = eval('<%=params.isSmartReplace()%>');

  idHits.Format();

  idReplace.focus();

<% } %>
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading"><%=lb_title%></DIV>
<BR>

<FORM id="idNavigationForm" method="post" action="<%=urlSelf%>">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_ACTION%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_SMARTREPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACEINDEX%>" value="">
</FORM>

<FORM id="idReplaceForm" method="post" action="<%=urlReplace%>">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_ACTION%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_SMARTREPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACEINDEX%>" value="">
</FORM>

<FORM id="idReplaceAllForm" method="post" action="<%=urlReplaceAll%>">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_ACTION%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_SMARTREPLACE%>" value="">
<INPUT type="hidden" name="<%=WebAppConstants.TERMBASE_REPLACEINDEX%>" value="">
</FORM>

<% if (numRecords == 0) { %>
<P CLASS="standardText"><%=lb_nothing_found%></P>
<INPUT type="button" VALUE="<%=bundle.getString("lb_back") %>" onclick="submitForm('back');">
<% } else { %>

<!-- search form -->
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538><%=lb_help_text%></TD>
  </TR>
  <TR style="padding-top: 12px">
    <TD WIDTH=538><B><%=bundle.getString("lb_number_hits") %>: <%=results.getNumResults()%></B></TD>
  </TR>
  <TR style="padding-top: 12px">
    <TD WIDTH=538>
      <table>
	<tr>
	  <td align="right" class="standardText"><%=bundle.getString("lb_search_for") %>:</td>
	  <td>
	    <INPUT type="text" size="50" id="idSearch" disabled>
	    <span onclick="highlightMatches()" CLASS="standardText clickable"
	    style="color:blue;"><%=bundle.getString("lb_tm_search_replace_highlight") %></span>
	  </td>
	</tr>
	<tr>
	  <td align="right" class="standardText"><%=bundle.getString("lb_replace_with") %>:</td>
	  <td>
	    <INPUT type="text" size="50" id="idReplace">
	    <INPUT type="button" id="idReplaceAll" value="<%=lb_replaceall%>"
	    onclick="submitForm('replaceall')">
	  </td>
	</tr>
	<tr>
	  <td>&nbsp;</td>
	  <td class="standardText">
	    <INPUT type="checkbox" id="idSmartReplace">
	    <label for="idSmartReplace"><%=bundle.getString("lb_smart_replace") %></label>
	  </td>
	</tr>
      </table>
    </TD>
  </TR>
</TABLE>
<BR>
<!-- end search form -->

<!-- results outer table -->
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0>
  <TR>
    <TD>
      <!-- Top Utility bar -->
      <TABLE CELLSPACING="0" CELLPADDING="0" WIDTH="100%" BGCOLOR="#DFDFDF">
	<TR>
	  <TD HEIGHT=30>
	    <A CLASS="standardHREF" HREF="#"
	    onclick="setAllCheckBoxes(true); return false;">
	    <%=lb_checkAll%></A>
	    <SPAN CLASS="standardText"> | </SPAN>
	    <A CLASS="standardHREF" HREF="#"
	    onclick="setAllCheckBoxes(false); return false">
	    <%=lb_clearAll%></A>
	  </TD>
	  <TD COLSPAN=2 ALIGN="right">
	    <P id="statusMessageTop" CLASS="standardText">
	    <IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idLeft"
	    <% if (windowMin > 0) { %>
	    class="clickable" onclick="submitForm('left');"
	    <%}%>
	    >
	    <%=windowMin + 1%>-<%=windowMax%> of <%=numRecords%>
	    <IMG SRC="/globalsight/images/nextMatchArrow.gif" id="idRight"
	    <% if (windowMax < numRecords) { %>
	    class="clickable" onclick="submitForm('right');"
	    <%}%>
	    >
	    </P>
	  </TD>
	  <TD ALIGN="right">
	    <INPUT TYPE="BUTTON" ID="idCancelBtnTop" VALUE="<%=lb_cancel%>"
	    onclick="submitForm('cancel');" CLASS="detailText">
	    <INPUT TYPE="BUTTON" ID="idBackBtnTop" VALUE="<%=lb_previous%>"
	    onclick="submitForm('back');" CLASS="detailText">
	    <INPUT TYPE="BUTTON" ID="idReplaceBtnTop" VALUE="<%=lb_replace%>"
	    onclick="submitForm('replace');" CLASS="detailText">
	  </TD>
	</TR>
      </TABLE>
      <!-- End Top Utility bar -->
    </TD>
  </TR>
  <TR>
    <TD>
      <!-- results table -->
      <form id="idDummyForm">
      <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0"
	id="idHits" STRIPED="true"
	STYLE="border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>">
	  <COL VALIGN="top"> <!-- checkbox -->
	  <COL VALIGN="top" ALIGN="center" class="standardText"
	  STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
	  <COL VALIGN="top" ALIGN="center"
	  STYLE="padding-left: 4px; padding-right: 4px"> <!-- Browser -->
	  <COL VALIGN="top" class="standardText" width="80%"> <!-- Entry -->
	<THEAD>
	  <TR CLASS="tableHeadingBasic">
	    <TD HEIGHT="20">&nbsp;</TD>
	    <TD HEIGHT="20" ALIGN="CENTER"><%=lb_id%></TD>
	    <TD HEIGHT="20" ALIGN="CENTER">Browse</TD>
	    <TD HEIGHT="20" ALIGN="LEFT">Hit</TD>
	  </TR>
	</THEAD>
	<TBODY id="idHitsBody">
	  <%
	  MappingContext ctxt = new MappingContext(xmlDefinition);
	  
	  List entryIds = results.getWindowEntryIds();
	  List levelIds = results.getWindowLevelIds();
	  List entryDom = results.getWindowDom();
	  
	  for (int i = 0, max = entryIds.size(); i < max; i++)
	  {
	    Long entryId = (Long)entryIds.get(i);
	    Long levelId = (Long)levelIds.get(i);
	    Document dom = (Document)entryDom.get(i);

	    // EditUtil.encodeXmlEntities(dom.getRootElement().asXML())
	    
	    String html = HtmlUtil.xmlToHtml(dom.getRootElement(), ctxt);
	  %>
	  <tr>
	    <td><input type="checkbox" index="<%=i%>"></td>
	    <td><%=entryId%></td>
	    <td>
	      <img src="/globalsight/images/termbase_icon.gif"
	      onclick="browseEntry(<%=entryId%>)" class="clickable">
	    </td>
	    <td><%=html%></td>
	  </tr>
	  <%
	  } // end for
	  %>
	</TBODY>
      </TABLE>
      </form>
      <!-- end results table -->
    </TD>
  </TR>
  <TR>
    <TD>
      <!-- Bottom Utility bar -->
      <TABLE CELLSPACING="0" CELLPADDING="0" WIDTH="100%" BGCOLOR="#DFDFDF">
	<TR>
	  <TD HEIGHT=30>
	    <A CLASS="standardHREF" HREF="#"
	    onclick="setAllCheckBoxes(true); return false;">
	    <%=lb_checkAll%></A>
	    <SPAN CLASS="standardText"> | </SPAN>
	    <A CLASS="standardHREF" HREF="#"
	    onclick="setAllCheckBoxes(false); return false">
	    <%=lb_clearAll%></A>
	  </TD>
	  <TD COLSPAN=2 ALIGN="right">
	    <P id="statusMessageTop" CLASS="standardText">
	    <IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idLeft"
	    <% if (windowMin > 0) { %>
	    class="clickable" onclick="submitForm('left');"
	    <%}%>
	    >
	    <%=windowMin + 1%>-<%=windowMax%> of <%=numRecords%>
	    <IMG SRC="/globalsight/images/nextMatchArrow.gif" id="idRight"
	    <% if (windowMax < numRecords) { %>
	    class="clickable" onclick="submitForm('right');"
	    <%}%>
	    >
	    </P>
	  </TD>
	  <TD ALIGN="right">
	    <INPUT TYPE="BUTTON" ID="idCancelBtnBottom" VALUE="<%=lb_cancel%>"
	    onclick="submitForm('cancel');" CLASS="detailText">
	    <INPUT TYPE="BUTTON" ID="idBackBtnBottom" VALUE="<%=lb_previous%>"
	    onclick="submitForm('back');" CLASS="detailText">
	    <INPUT TYPE="BUTTON" ID="idReplaceBtnBottom" VALUE="<%=lb_replace%>"
	    onclick="submitForm('replace');" CLASS="detailText">
	  </TD>
	</TR>
      </TABLE>
      <!-- End Bottom Utility bar -->
    </TD>
  </TR>  
</TABLE>
<BR>

<% } %>

</DIV>
</BODY>
</HTML>
