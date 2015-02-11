<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.tm.maintenance.TmSearchHelper,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            java.util.Locale,
            java.util.ResourceBundle,
            java.util.Iterator"
    session="true"
%>
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="replace" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // links
    String searchUrl = search.getPageURL();
    String replaceUrl = replace.getPageURL();
    String cancelUrl = cancel.getPageURL();

    // labels
    String pagetitle = bundle.getString("lb_globalsight") +
      bundle.getString("lb_colon") + " " +
      bundle.getString("lb_search_and_replace");
    String title = bundle.getString("lb_search_and_replace");
    String lbNext = bundle.getString("lb_next");
    String lbCancel = bundle.getString("lb_cancel");
    String lbFind = bundle.getString("lb_find") + bundle.getString("lb_colon");
    String lbFindIn = bundle.getString("lb_tm_search_find_in") +
      bundle.getString("lb_colon");
    String lbSourceLocale = bundle.getString("lb_tm_search_source_locale") +
      bundle.getString("lb_colon");
    String lbDisplayTargetLocale =
      bundle.getString("lb_tm_search_display_target_locale") +
      bundle.getString("lb_colon");
    String lbMatchCase = bundle.getString("lb_tm_search_match_case");

    // error message

    // control name
    String sourceFindText = WebAppConstants.TM_SOURCE_FIND_TEXT;
    String sourceLocaleSelector = WebAppConstants.TM_SOURCE_SEARCH_LOCALE_SELECTOR;
    String sourceFindMatchCase = WebAppConstants.TM_SOURCE_FIND_MATCH_CASE;
    String targetLocaleSelector = WebAppConstants.TM_TARGET_SEARCH_LOCALE_SELECTOR;
    String stateParam = WebAppConstants.TM_SEARCH_STATE_PARAM;
    String stateNormal = WebAppConstants.TM_SEARCH;

    // Option value names
    //   locale selectors
    List sourceLocales = (List)request.getAttribute(
      WebAppConstants.TM_SOURCE_SEARCH_LOCALES);
    List targetLocales = (List)request.getAttribute(
      WebAppConstants.TM_TARGET_SEARCH_LOCALES);

    // Option text names

    // Get session values from last search (if any)
    String sessionSourceFindTextValue =
        (String)session.getAttribute(WebAppConstants.TM_SOURCE_FIND_TEXT);
    if (sessionSourceFindTextValue == null)
    {
        sessionSourceFindTextValue = "";
    }
    Boolean sessionSourceCaseSensitive = (Boolean)session.getAttribute(
      WebAppConstants.TM_SOURCE_FIND_MATCH_CASE);
    String sourceCaseSensitiveCHECKED =
        ((sessionSourceCaseSensitive != null) &&
         sessionSourceCaseSensitive.booleanValue()) ? " CHECKED" : "";
    Long sessionSourceLocaleId =
      (Long)session.getAttribute(WebAppConstants.TM_SOURCE_SEARCH_LOCALE);
    Long sessionTargetLocaleId =
      (Long)session.getAttribute(WebAppConstants.TM_TARGET_SEARCH_LOCALE);

    String str_emptyTm = "false";
    if (sourceLocales.size() == 0)
    {
        str_emptyTm = "true";
    }
%>
<HTML>
<HEAD>
<TITLE><%=pagetitle%></TITLE>
<STYLE>
#idHiddenCheckBox {visibility: hidden;}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_maintenance1")%>";

var b_emptyTm = eval("<%=str_emptyTm%>");

function submitForm()
{
    if (isFindEmpty())
    {
        alert("<%=bundle.getString("msg_enter_search_text")%>")
        return false;
    }

    ShowStatusMessage("<%=bundle.getString("msg_search_in_progress")%>");

    return true;
}

function doCancel()
{
    TmSearchForm.action = "<%=cancelUrl%>";
    TmSearchForm.submit();
}

function isFindEmpty()
{
    if (document.all.TmSearchForm.<%=sourceFindText%>.value == "")
    {
       document.all.TmSearchForm.<%=sourceFindText%>.focus();
       return true;
    }
    else
    {
       return false;
    }
}

function ShowStatusMessage(p_msg)
{
    statusMessage.innerHTML = p_msg;
}

function doLoad()
{
    loadGuides();

    if (b_emptyTm)
    {
        document.all.TmSearchForm.Search.disabled = true;
        document.all.TmSearchForm.Cancel.focus();
    }
    else
    {
        document.all.TmSearchForm.<%=sourceFindText%>.focus();
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
    MARGINHEIGHT="0" ONLOAD="doLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
  <TR>
    <TD ALIGN="LEFT">
      <SPAN CLASS="mainHeading"><%=title%></SPAN>
      <P></P>
      <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
	<%
	if (sourceLocales.size() == 0)
	{
	%>
	<TR>
	  <TD WIDTH=538 CLASS="warningText"><%=bundle.getString("lb_this_tm_is_empty")%></TD>
	</TR>
	<TR><TD><%-- spacer --%>&nbsp;</TD></TR>
	<%
	}
	%>
	<TR>
	  <TD WIDTH=538>
	    <%=bundle.getString("helper_text_search")%>
	  </TD>
	</TR>
      </TABLE>
      <P></P>
      <!-- Lower table -->
      <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
	<TR>
	  <TD VALIGN="TOP">
	    <FORM NAME="TmSearchForm" METHOD="POST" onsubmit="return submitForm()"
	    ACTION="<%=replaceUrl%>&<%=stateParam%>=<%=stateNormal%>">
	    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
	      <TR>
		<TD VALIGN="TOP">
		  <!-- Search table -->
		  <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
		    <TR>
		      <TD>
			<SPAN CLASS="standardText"><%=lbFind%></SPAN>
		      </TD>
		      <TD>
			<INPUT TYPE="TEXT" NAME="<%=sourceFindText%>"
			VALUE="<%=sessionSourceFindTextValue%>" SIZE="30">
		      </TD>
		      <TR>
			<TD>
			  <SPAN CLASS="standardText"><%=lbFindIn%></SPAN>
			</TD>
			<TD>
			  <SELECT onChange="" NAME="<%=sourceLocaleSelector%>"
			    CLASS="standardText">
             <%
             Iterator it1 = sourceLocales.iterator();
             while (it1 != null && it1.hasNext())
             {
                GlobalSightLocale gl = (GlobalSightLocale)it1.next();
                long id = gl.getId();
                String displayName = gl.getDisplayName(uiLocale);
                String selected = (sessionSourceLocaleId == null ||
		  id != sessionSourceLocaleId.longValue()) ? "" : " SELECTED";
             %>
			    <OPTION VALUE="<%=id%>"<%=selected%>><%=displayName%></OPTION>
             <%
             }
             %>
			  </SELECT>
			</TD>
		      </TR>
		      <TR>
			<TD></TD>
			<TD>
			<span id="idHiddenCheckBox">
			  <INPUT TYPE="Checkbox" id="idCaseSensitive"
			  NAME="<%=sourceFindMatchCase%>"
			  VALUE="<%=sourceFindMatchCase %>"
			  <%=sourceCaseSensitiveCHECKED%> >
			  <label for="idCaseSensitive"><%=lbMatchCase%></label>
			  </INPUT>
			</span>
			</TD>
		      </TR>
<%--
		      <TR>
			<TD COLSPAN=2><!-- spacer-->&nbsp;</TD>
		      </TR>
--%>
		      <TR>
			<TD>
			  <SPAN CLASS="standardText"><%=lbDisplayTargetLocale%></SPAN>
			</TD>
			<TD>
			  <SELECT onChange="" NAME="<%=targetLocaleSelector%>"
			    CLASS="standardText">
             <%
             Iterator it2 = targetLocales.iterator();
             while(it2 != null && it2.hasNext())
             {
                GlobalSightLocale gl = (GlobalSightLocale)it2.next();
                long id = gl.getId();
                String displayName = gl.getDisplayName(uiLocale);
                String selected = (sessionTargetLocaleId == null ||
		  id != sessionTargetLocaleId.longValue()) ? "" : " SELECTED";
             %>
			    <OPTION VALUE="<%=id%>"<%=selected %>><%=displayName%></OPTION>
             <%
             }
             %>
			  </SELECT>
			</TD>
		      </TR>
		    </TABLE>
		  </TD>
		</TR>
		<TR>
		  <TD>
		    <P id="statusMessage" CLASS="standardText" >&nbsp;</P>
		  </TD>
		  <TD>
		    <DIV ID="ButtonLayer" ALIGN="RIGHT">
		    <BR>
		    <INPUT TYPE="BUTTON" NAME="Cancel" VALUE="<%= lbCancel %>"
		    onclick="doCancel();">
		    <INPUT TYPE="SUBMIT" NAME="Search" VALUE="<%= lbNext %>">
		    </DIV>
		  </TD>
		</TR>
	      </TABLE>
	      </FORM>
	    </TD>
	  </TR>
	</TABLE>
      </TD>
    </TR>
  </TABLE>
</DIV>
</BODY>
</HTML>
