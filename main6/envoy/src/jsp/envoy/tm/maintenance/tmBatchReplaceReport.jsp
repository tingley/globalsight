<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.Collection,
            java.util.ResourceBundle" 
    session="true"
%>
<jsp:useBean id="search" scope="request" 
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request" 
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // links   
    String doneUrl = done.getPageURL();
    String searchUrl = search.getPageURL();

    // labels    
    String pagetitle= bundle.getString("lb_globalsight")
                      + bundle.getString("lb_colon") + " "
                      + bundle.getString("lb_tm_replace_results_title");
    String title = bundle.getString("lb_tm_replace_results_title");   
    String lbDoneBtn = bundle.getString("lb_done");
    String lbSearchAndReplaceBtn = bundle.getString("lb_search_and_replace") + 
        bundle.getString("lb_dots");
    String msgReplaceSuccessful = bundle.getString("msg_replace_successful");

    String lbId = bundle.getString("lb_id");
    String lbSource = bundle.getString("lb_tm_search_source_locale") +
        bundle.getString("lb_colon");
    String lbTarget = bundle.getString("lb_tm_search_target_locale") +
        bundle.getString("lb_colon");

    // control name
    String stateParam = WebAppConstants.TM_SEARCH_STATE_PARAM;
    String stateNormal = WebAppConstants.TM_SEARCH_STATE_NORMAL;

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

    Collection replaceResults = (Collection)sessionMgr.getAttribute(
        WebAppConstants.TM_CONCORDANCE_REPLACE_RESULTS);

    String replaceResultsHtml = (String)request.getAttribute(
      WebAppConstants.TM_CONCORDANCE_REPLACE_RESULTS_HTML);
%>
<HTML>
<HEAD>
<TITLE><%=pagetitle%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tmSearch";
var isMac = (navigator.appVersion.indexOf("Mac") != -1) ? true : false;
var helpFile = "<%=bundle.getString("help_tm_maintenance3")%>";

function submitForm(buttonClicked) 
{
    if (buttonClicked=="<%= lbSearchAndReplaceBtn %>")
    {
        TmBatchReplaceResultsForm.action = "<%=searchUrl%>" + 
            "&" + "<%=stateParam%>" + "=" + "<%=stateNormal%>";
    }

    if (buttonClicked=="<%=lbDoneBtn%>")
    {
        TmBatchReplaceResultsForm.action = "<%=doneUrl%>";                
    }

    TmBatchReplaceResultsForm.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>                
<P></P>

<!-- Lower table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
  <TR>
    <TD VALIGN="TOP">
      <SPAN CLASS="standardTextBold"><%=msgReplaceSuccessful%></SPAN>
    </TD>
  </TR>
</TABLE>
<P></P>

<FORM NAME="TmBatchReplaceResultsForm" ACTION="<%=doneUrl%>" METHOD="POST">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"
  STYLE="border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>">
  <THEAD>
    <COL VALIGN="top" ALIGN="right"
    STYLE="padding-left: 4px; padding-right: 4px"> <!-- ID -->
    <COL VALIGN="top"> <!-- Source -->
    <COL VALIGN="top"> <!-- Target -->
    <TR CLASS="tableHeadingBasic">
      <TD HEIGHT="20" ALIGN="center"><%=lbId%></TD>
      <TD HEIGHT="20"><%=lbSource%> <%=sourceSearchLocaleDisplayName%></TD>
      <TD HEIGHT="20"><%=lbTarget%> <%=targetSearchLocaleDisplayName%></TD>
    </TR>
  </THEAD>
  <TBODY>
    <%=replaceResultsHtml%>
  </TBODY>
</TABLE>
</FORM>

<DIV>
<INPUT TYPE="BUTTON" NAME="Search" VALUE="<%= lbSearchAndReplaceBtn %>"
 onClick="submitForm('<%= lbSearchAndReplaceBtn %>');">&nbsp;
<INPUT TYPE="BUTTON" NAME="Search" VALUE="<%= lbDoneBtn %>"
 onClick="submitForm('<%= lbDoneBtn %>');">
</DIV>

</DIV>
</BODY>
</HTML>
