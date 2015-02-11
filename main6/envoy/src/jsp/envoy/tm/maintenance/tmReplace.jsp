<%@page import="com.globalsight.everest.permission.Permission"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.foundation.User,
            com.globalsight.util.progress.ProcessStatus,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.everest.tm.searchreplace.TmConcordanceResult,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.webapp.pagehandler.tm.maintenance.TmSearchHelper,
            java.util.ResourceBundle,
            java.util.Locale"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="replace" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="deleteTuv" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="deleteTu" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

// links
String replaceUrl = replace.getPageURL();
String cancelUrl = cancel.getPageURL();
String searchUrl = search.getPageURL();
String selfUrl = self.getPageURL();
String deleteTuvUrl = deleteTuv.getPageURL();
String deleteTuUrl = deleteTu.getPageURL();

// labels
String pagetitle= bundle.getString("lb_globalsight") +
    bundle.getString("lb_colon") + " " + bundle.getString("lb_results");
String title = bundle.getString("lb_results");
String lbBackBtn = bundle.getString("lb_previous");
String lbReplaceBtn = bundle.getString("lb_replace") +
    bundle.getString("lb_dots");
String lbCancel = bundle.getString("lb_cancel");
String lbFindIn = bundle.getString("lb_tm_search_find_in");
String lbDisplayTargetLocale =
    bundle.getString("lb_tm_search_display_target_locale");
String lbReplace = bundle.getString("lb_replace");
String lbReplaceColon = bundle.getString("lb_replace") +
    bundle.getString("lb_colon");
String lbReplaceIn = bundle.getString("lb_tm_search_replace_in") +
    bundle.getString("lb_colon");
String lbReplaceWith = bundle.getString("lb_tm_search_replace_with") +
    bundle.getString("lb_colon");
String lbMatchCase = bundle.getString("lb_tm_search_match_case");
String lbId = bundle.getString("lb_id");
String lbSource = bundle.getString("lb_tm_search_source_locale") +
    bundle.getString("lb_colon");
String lbTarget = bundle.getString("lb_tm_search_target_locale") +
    bundle.getString("lb_colon");
String lbTMName = bundle.getString("lb_tm_name");
String lbSid = bundle.getString("lb_sid");
String lbDeleteTuvBtn = bundle.getString("lb_delete_tuv");
String lbDeleteTuBtn = bundle.getString("lb_delete_tu");

String lbNumberOfSegFound =
    bundle.getString("lb_tm_search_number_of_seg_found") +
    bundle.getString("lb_colon");
String checkAllLinkText = bundle.getString("lb_check_all");
String clearAllLinkText = bundle.getString("lb_clear_all");

// Get control/session value names
String targetFindText = WebAppConstants.TM_TARGET_FIND_TEXT;
String targetLocaleSelector = WebAppConstants.TM_TARGET_SEARCH_LOCALE_SELECTOR;
String targetReplaceText = WebAppConstants.TM_TARGET_REPLACE_TEXT;
String targetFindMatchCase = WebAppConstants.TM_TARGET_FIND_MATCH_CASE;
String replaceSegmentCheckbox = WebAppConstants.TM_REPLACE_SEGMENT_CHKBOX;
String highlight = WebAppConstants.TM_SEARCH_STATE_HIGHLIGHT;
String stateParam = WebAppConstants.TM_SEARCH_STATE_PARAM;
String stateNormal = WebAppConstants.TM_SEARCH_STATE_NORMAL;
String stateHighlight = WebAppConstants.TM_SEARCH_STATE_HIGHLIGHT;
String stateShowNext = WebAppConstants.TM_SEARCH_STATE_NEXT;
String stateShowPrev = WebAppConstants.TM_SEARCH_STATE_PREV;
String selections = WebAppConstants.TM_REPLACE_SEGMENT_CHKBOX;

// Get search form values
GlobalSightLocale sourceSearchLocale =
    (GlobalSightLocale)sessionMgr.getAttribute(
        WebAppConstants.TM_SOURCE_SEARCH_LOCALE);

String sourceSearchLocaleDisplayName =
    sourceSearchLocale != null ?
    sourceSearchLocale.getDisplayName(uiLocale) : "null";

GlobalSightLocale targetSearchLocale =
    (GlobalSightLocale)sessionMgr.getAttribute(
        WebAppConstants.TM_TARGET_SEARCH_LOCALE);

String targetSearchLocaleDisplayName =
    targetSearchLocale != null ?
    targetSearchLocale.getDisplayName(uiLocale) : "null";

String searchResultsHtml = (String)request.getAttribute(
    WebAppConstants.TM_CONCORDANCE_SEARCH_RESULTS_HTML);

// get actual concordance results on the session manager
ProcessStatus status = (ProcessStatus)sessionMgr.getAttribute(
    WebAppConstants.TM_TM_STATUS);
TmConcordanceResult searchResults = (TmConcordanceResult)status.getResults();

int numRecords = 0;
int min = 0;
int max = 0;
if (searchResults != null)
{
    numRecords = searchResults.getTotal();
    min = searchResults.getMin();
    max = searchResults.getMax();
}

// Get session values from last search (if any)
String targetFindTextValue =
    (String)request.getAttribute(WebAppConstants.TM_TARGET_FIND_TEXT);
if (targetFindTextValue == null )
{
    targetFindTextValue = "";
}
else
{
    targetFindTextValue = EditUtil.encodeHtmlEntities(targetFindTextValue);
}
String targetReplaceTextValue =
    (String)request.getAttribute(WebAppConstants.TM_TARGET_REPLACE_TEXT);
if (targetReplaceTextValue == null )
{
    targetReplaceTextValue = "";
}
else
{
    targetReplaceTextValue = EditUtil.encodeHtmlEntities(targetReplaceTextValue);
}
String targetCaseSensitive =
    (String)request.getParameter(WebAppConstants.TM_TARGET_FIND_MATCH_CASE);
String targetCaseSensitiveCHECKED =
    (targetCaseSensitive == null) ? "" : " CHECKED";

// error message
String str_error =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML>
<HEAD>
<TITLE><%=pagetitle%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<STYLE type="text/css">
.clickable { color: blue; cursor: hand; }
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var isMac = (navigator.appVersion.indexOf("Mac") != -1) ? true : false;
var helpFile = "<%=bundle.getString("help_tm_maintenance2")%>";

function submitForm(buttonClicked)
{
    if (buttonClicked=="highlight")
    {
        if (isTargetFindEmpty())
        {
            alert("<%=EditUtil.toJavascript(bundle.getString("msg_enter_replacement_text"))%>")
            return false;
        }

        ReplaceForm.action = "<%=selfUrl%>" +
            "&" + "<%=stateParam%>" + "=" + "<%=stateHighlight%>";
    }
    else if (buttonClicked=="back")
    {
        ReplaceForm.action = "<%=searchUrl%>";
    }
    else if (buttonClicked=="left")
    {
        ReplaceForm.action = "<%=selfUrl%>" +
            "&" + "<%=stateParam%>" + "=" + "<%=stateShowPrev%>";
    }
    else if (buttonClicked=="right")
    {
        ReplaceForm.action = "<%=selfUrl%>" +
            "&" + "<%=stateParam%>" + "=" + "<%=stateShowNext%>";
    }
    else if (buttonClicked=="replace")
    {
        if (isTargetFindEmpty())
        {
            alert("<%=EditUtil.toJavascript(bundle.getString("msg_enter_replacement_text"))%>")
            return false;
        }

        if (!isSegmentSelection())
        {
            alert("<%=EditUtil.toJavascript(bundle.getString("msg_select_item_to_be_replaced"))%>");
            return false;
        }

        if (isReplaceWithEmpty())
        {
            if(!confirm("<%=EditUtil.toJavascript(bundle.getString("msg_replace_delete_target_text"))%>" + " [ " + document.all.ReplaceForm.<%= targetFindText %>.value + " ]"))
            {
               return false;
            }
        }
        else if (!confirm("<%=EditUtil.toJavascript(bundle.getString("msg_proceed_with_replace"))%>"))
        {
           return false;
        }

        ReplaceForm.action = "<%=replaceUrl%>";
        ShowStatusMessage("<%=EditUtil.toJavascript(bundle.getString("msg_replacing_selected_items"))%>");
        disableButtons();
    }
    else if (buttonClicked=="cancel")
    {
        ReplaceForm.action = "<%=cancelUrl%>";
    }
    else if (buttonClicked=="deleteTuv" || buttonClicked=="deleteTu")
    {
        var confirmMsg;
        var url;
        if(buttonClicked=="deleteTuv")
        {
            confirmMsg = "<%=EditUtil.toJavascript(
                bundle.getString("msg_proceed_with_delete_tuv"))%>";
            url = "<%=deleteTuvUrl + "&" + WebAppConstants.TM_ACTION +
                "=" + WebAppConstants.TM_ACTION_DELETE_TUV %>";
        }
        else
        {
            confirmMsg = "<%=EditUtil.toJavascript(
                bundle.getString("msg_proceed_with_delete_tu"))%>";
            url = "<%=deleteTuUrl + "&" + WebAppConstants.TM_ACTION +
                "=" + WebAppConstants.TM_ACTION_DELETE_TU %>";
        }        

        if (!isSegmentSelection())
        {
            alert("<%=EditUtil.toJavascript(
                bundle.getString("msg_select_item_to_be_deleted"))%>");
            return false;
        }
        else if (!confirm(confirmMsg))
        {
           return false;
        }

        ReplaceForm.action = url;
        ShowStatusMessage("<%=EditUtil.toJavascript(
            bundle.getString("msg_deleting_selected_items"))%>");
        disableButtons();
    }
    
    ReplaceForm.submit();
}

function ShowStatusMessage(p_msg)
{
    statusMessageTop.innerHTML = p_msg;
    statusMessageButtom.innerHTML = p_msg;
}

function setAllSegmentReplaceCheckBoxes(state)
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.ReplaceForm;
    }
    else
    {
        theForm = document.all.ReplaceForm;
    }

    for (var i=0; i < theForm.length; i++)
    {
        if (theForm.elements[i].type == "checkbox" &&
            theForm.elements[i].name == "<%=replaceSegmentCheckbox%>")
        {
            theForm.elements[i].checked = state;
        }
    }
    return false;
}


function isSegmentSelection()
{
    var isSelection = false;
    theForm = document.all.ReplaceForm;

    if (theForm.<%=replaceSegmentCheckbox%>.value)
    {
        if (theForm.<%=replaceSegmentCheckbox%>.checked)
        {
            isSelection = true;
        }
    }
    else
    {
        for (var i = 0; i < theForm.<%=replaceSegmentCheckbox%>.length; i++)
        {
            if (theForm.<%=replaceSegmentCheckbox%>[i].checked == true)
            {
                isSelection = true;
                break;
            }
        }
    }
    return isSelection;
}

function isReplaceWithEmpty()
{
    if (document.all.ReplaceForm.<%=targetReplaceText%>.value == "")
    {
       return true;
    }
    else
    {
       return false;
    }
}

function isTargetFindEmpty()
{
    if (document.all.ReplaceForm.<%= targetFindText %>.value == "")
    {
       return true;
    }
    else
    {
       return false;
    }
}

function disableButtons()
{
	if(document.all.ReplaceBtnTop)
	{
		document.all.ReplaceBtnTop.disabled = true;
	    document.all.ReplaceBtnBottom.disabled = true;
	}
	
	if(document.all.DeleteTuvBtnTop)
	{
		document.all.DeleteTuvBtnTop.disabled = true;
	    document.all.DeleteTuvBtnBottom.disabled = true;
	}
	
	if(document.all.DeleteTuBtnTop)
	{
		document.all.DeleteTuBtnTop.disabled = true;
	    document.all.DeleteTuBtnBottom.disabled = true;
	} 
}

function doOnLoad()
{
    loadGuides();
<%
    if(searchResultsHtml != null && searchResultsHtml.length() > 0)
    {
%>
    document.all.LoadingMessage.style.display = "none";
    document.all.ReplaceForm.<%=targetFindText%>.focus();
<%
    }
%>
}

var corpuswins= new Array();
var numcorpuswins = 0;
function showCorpus(p_tuvId, p_srcLocaleId)
{
   var url = "/globalsight/ControlServlet?activityName=viewCorpusMatches&tuvId=" + p_tuvId + "&localeDbId=" + p_srcLocaleId + "&showDelete=true";
   var name = "corpus" + numcorpuswins;
   corpuswins[numcorpuswins++] = window.open(url, name,
   'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
}

function doOnUnload()
{
   for (var i=0; i < numcorpuswins; i++)
   {
      try { corpuswins[i].close(); } catch (ignore) {}
   }
}

//for GBS-2599
function handleSelectAll() {
    if (ReplaceForm.selectAll.checked) {
    	setAllSegmentReplaceCheckBoxes(true);
    }
    else {
    	setAllSegmentReplaceCheckBoxes(false);
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="doOnLoad()" onunload="doOnUnload()"
 CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<FORM NAME="ReplaceForm" METHOD="POST">
<% if (searchResultsHtml == null || searchResultsHtml.length() <= 0)
  {
%>
<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P><%=bundle.getString("msg_search_results_nothing_found")%></P>
<TABLE CELLSPACING="0" CELLPADDING="0" >
<TR>
<%}
  else
  {
%>
 <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
  <TR>
   <TD>
    <SPAN CLASS="mainHeading"><%=title%></SPAN>
    <P><%=bundle.getString("helper_text_replace")%><P>
    <P><B><%=lbNumberOfSegFound%> <%=numRecords%></B></P>
    <!-- Lower table -->
    <amb:permission name="<%=Permission.TM_SEARCH_ADVANCED%>" >
    <TABLE CELLPADDING="2" CELLSPACING="2" BORDER="0" CLASS="standardText">
     <TR>
      <TD><%=lbReplaceColon%></TD>
      <TD>
       <INPUT TYPE="TEXT" NAME="<%=targetFindText%>"
	VALUE="<%=targetFindTextValue%>">
       <INPUT TYPE="BUTTON" NAME="Highlight replacement text"
        VALUE="<%=bundle.getString("lb_tm_search_replace_highlight") %>" onClick="submitForm('highlight');"
        CLASS="detailText">
      </TD>
     </TR>
     <TR>
      <TD><%=lbReplaceIn%></TD>
      <TD>
       <SELECT onChange="" NAME="<%=targetLocaleSelector%>" DISABLED>
        <OPTION VALUE="English (US)" SELECTED>
	<%= targetSearchLocaleDisplayName %></OPTION>
        </SELECT>
      </TD>
     </TR>
     <TR VALIGN="TOP">
       <TD><%= lbReplaceWith%></TD>
      <TD>
       <INPUT TYPE="TEXT" NAME="<%=targetReplaceText%>"
	VALUE="<%=targetReplaceTextValue%>"><BR>
       <INPUT TYPE="Checkbox" id="idMatchCase"
	NAME="<%=targetFindMatchCase%>"	<%=targetCaseSensitiveCHECKED%>>
	<label for="idMatchCase"><%=lbMatchCase%></label>
      </TD>
     </TR>
    </TABLE>
    </amb:permission>
    <!-- end lower table -->
   </TD>
  </TR>
  <TR>
    <TD>
      <div id="LoadingMessage" CLASS="standardText" style="color: #D3D3D3">
      <B><I><%=bundle.getString("msg_loading_search_results")%></B></I>
      </div>&nbsp;
    </TD>
  </TR>
 </TABLE>

 <!-- results outer table -->

 <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0>
 <TR>
 <TD>

  <!-- Top Utility bar -->
 <TABLE CELLSPACING="0" CELLPADDING="0" WIDTH="100%" BGCOLOR="#DFDFDF">
  <TR>
   <TD HEIGHT=30>
    <!--A CLASS="standardHREF" HREF="#"
     onclick="setAllSegmentReplaceCheckBoxes(true); return false">
     <%=checkAllLinkText%></A><SPAN CLASS="standardText"> | </SPAN>
    <A CLASS="standardHREF" HREF="#"
     onclick="setAllSegmentReplaceCheckBoxes(false); return false">
     <%=clearAllLinkText%></A-->
   </TD>
   <TD COLSPAN=2 ALIGN="right">
     <P id="statusMessageTop" CLASS="standardText">
     <IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idLeft"
     <% if (min > 0) { %>
     class="clickable" onclick="submitForm('left');"
     <%}%>
     >
     <%=min + 1%>-<%=max%> of <%=numRecords%>
     <IMG SRC="/globalsight/images/nextMatchArrow.gif" id="idRight"
     <% if (max < numRecords) { %>
     class="clickable" onclick="submitForm('right');"
     <%}%>
     >
     </P>
   </TD>
   <TD ALIGN="right">
    <INPUT TYPE="BUTTON" NAME="CancelBtnTop" VALUE="<%=lbCancel%>"
     onclick="submitForm('cancel');" CLASS="detailText">
    <INPUT TYPE="BUTTON" NAME="BackBtnTop" VALUE="<%=lbBackBtn%>"
     onclick="submitForm('back');" CLASS="detailText">
    <amb:permission name="<%=Permission.TM_SEARCH_ADVANCED%>" >
    <INPUT TYPE="BUTTON" NAME="ReplaceBtnTop" VALUE="<%=lbReplaceBtn%>"
     onclick="submitForm('replace');" CLASS="detailText">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_DELETE_ENTRY%>" >
      <INPUT TYPE="BUTTON" NAME="DeleteTuvBtnTop" VALUE="<%=lbDeleteTuvBtn%>"
     onclick="submitForm('deleteTuv');" CLASS="detailText">
     <INPUT TYPE="BUTTON" NAME="DeleteTuBtnTop" VALUE="<%=lbDeleteTuBtn%>"
     onclick="submitForm('deleteTu');" CLASS="detailText">
    </amb:permission>
   </TD>
  </TR>
 </TABLE>
  <!-- End Top Utility bar -->


 </TD>
 </TR>
 <TR>
 <TD>
 <!-- results table -->
 <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0"
    STYLE="border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>">
  <THEAD>
  <COL VALIGN="top"> <!-- checkbox -->
  <COL VALIGN="top" ALIGN="center"
    STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
  <% if (b_corpus) {%>
  <COL VALIGN="top" ALIGN="center"
    STYLE="padding-left: 4px; padding-right: 4px"> <!-- Corpus Context -->
  <%}%>  
  <COL VALIGN="top"> <!-- Source -->
  <COL VALIGN="top"> <!-- Target -->
  <TR CLASS="tableHeadingBasic">
   <TD HEIGHT="20"><input type="checkbox" onclick="handleSelectAll()" name="selectAll" checked="true"/></TD>
   <TD HEIGHT="20" ALIGN="CENTER"><%=lbId%></TD>
   <% if (b_corpus) {%>
   <TD HEIGHT="20" ALIGN="CENTER"><%=bundle.getString("lb_corpus_context")%></TD>
   <%}%>
   <TD HEIGHT="20" ALIGN="LEFT"><%=lbSource%> <%=sourceSearchLocaleDisplayName%></TD>
   <TD HEIGHT="20" ALIGN="LEFT"><%=lbTarget%> <%=targetSearchLocaleDisplayName%></TD>
   <TD HEIGHT="20" ALIGN="LEFT" width=100><%=lbTMName%></TD>
   <TD HEIGHT="20" ALIGN="LEFT" width=50><%=lbSid%></TD>
  </TR>
  </THEAD>
  <TBODY>
  <%= searchResultsHtml %>
  </TBODY>
  </TABLE>
  <!-- end results table -->
  </TD>
  </TR>
  <TR>
  <TD>
  <!-- Bottom Utility bar -->
<%}%>


<% if (searchResultsHtml != null && searchResultsHtml.length() > 0)
{
%>
 <TABLE CELLSPACING="0" CELLPADDING="0" WIDTH="100%" BGCOLOR="#DFDFDF">
  <TR>
   <TD HEIGHT=30>
    <!--A CLASS="standardHREF" HREF="#"
     ONCLICK="setAllSegmentReplaceCheckBoxes(true); return false">
     <%=checkAllLinkText%></A><SPAN CLASS="standardText"> | </SPAN>
    <A CLASS="standardHREF" HREF="#"
     ONCLICK="setAllSegmentReplaceCheckBoxes(false); return false">
     <%=clearAllLinkText%></A-->
   </TD>
<%}%>
   <TD COLSPAN=2 ALIGN="right">
     <P id="statusMessageButtom" CLASS="standardText">&nbsp;</P>
   </TD>
   <TD ALIGN="right">
    <INPUT TYPE="BUTTON" NAME="CancelBtnBottom" VALUE="<%=lbCancel%>"
     onclick="submitForm('cancel');" CLASS="detailText">
    <INPUT TYPE="BUTTON" NAME="BackBtnBottom" VALUE="<%=lbBackBtn%>"
     onclick="submitForm('back');" CLASS="detailText">
<% if (searchResultsHtml != null && searchResultsHtml.length() > 0)
{
%>
    <INPUT TYPE="BUTTON" NAME="ReplaceBtnBottom" VALUE="<%=lbReplaceBtn%>"
     onclick="submitForm('replace');" CLASS="detailText">
    <INPUT TYPE="BUTTON" NAME="DeleteTuvBtnBottom" VALUE="<%=lbDeleteTuvBtn%>"
     onclick="submitForm('deleteTuv');" CLASS="detailText">
    <INPUT TYPE="BUTTON" NAME="DeleteTuBtnBottom" VALUE="<%=lbDeleteTuBtn%>"
     onclick="submitForm('deleteTu');" CLASS="detailText">
   </TD>
  </TR>
 </TABLE>
 <!-- End Bottom Utility bar -->
<%}%>
 </TD>
 </TR>
</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
