<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.costing.Currency,
            com.globalsight.everest.jobhandler.Job,
            java.util.Vector,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            java.text.MessageFormat,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="allStatus" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="archived" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="changeCurr" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="complete" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exported" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="progress" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ready" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	String DEFAULT_PARAM = "&jobListStart=0";
	Object param = request.getAttribute(JobManagementHandler.JOB_LIST_START_PARAM);
	String startIndex = param == null ? DEFAULT_PARAM : "&jobListStart="+param;
	String thisSearch = (String) request.getAttribute("searchType");
	if (thisSearch == null)
	    thisSearch = (String) session.getAttribute("searchType");
	
	ResourceBundle bundle = PageHandler.getBundle(session);
	
	String allStatusURL = allStatus.getPageURL()+ startIndex + "&searchType=" + thisSearch; 
	String archivedURL = archived.getPageURL()+ DEFAULT_PARAM;
    String completeURL = complete.getPageURL()+ DEFAULT_PARAM;
    String detailsURL = jobDetails.getPageURL();
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM;
    String modifyURL = modify.getPageURL();
    String pendingURL = pending.getPageURL()+ DEFAULT_PARAM;
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    
    String title = bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_all_status");
    String lbPending= bundle.getString("lb_pending");
    String lbReady= bundle.getString("lb_ready");
    String lbInProgress= bundle.getString("lb_inprogress");
    String lbLocalized= bundle.getString("lb_localized");
    String lbExported= bundle.getString("lb_exported");
    String lbArchived= bundle.getString("lb_archived");
    String lbAllStatus= bundle.getString("lb_all_status");
    
    String refreshUrl = allStatusURL;
    String helperText = bundle.getString("helper_text_job_all_status");
    
    int myJobsDaysRetrieved = 0;
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        myJobsDaysRetrieved =
               sc.getIntParameter(SystemConfigParamNames.MY_JOBS_DAYS_RETRIEVED);
       
        if ("stateOnly".equals(thisSearch) && myJobsDaysRetrieved > 0)
        {
            String[] messageArgs = {String.valueOf(myJobsDaysRetrieved)};
            helperText = helperText + MessageFormat.format(
               bundle.getString("helper_text_recent_jobs"), messageArgs);
        }

    }
    catch (Exception ge)
    {
        // assume 0
    }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_exported_tab")%>";

function loadPage() 
{
   // Only show the download button if something is available to download
   //if (JobForm.jobId)
   //{
       //document.all.ButtonLayer.style.visibility = "visible";
       //document.all.CheckAllLayer.style.visibility = "visible";
   //}
   // Load the Guide
   loadGuides();
}

function setButtonState()
{
	// no buttons for this page for now
}

function ShowStatusMessage(p_msg)
{
    if (document.layers)
    {
        document.menu.document.statusMessage.innerHTML = p_msg;
    }
    else
    {
       statusMessage.innerHTML = p_msg;
    }
}

function submitForm(buttonClicked)
{
    // no buttons for this page for now
}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/projects/workflows/jobSort.jspIncl" %>
<STYLE>
<%--
This stylesheet should be in the HEAD element but the skin bean
is defined in header.jspIncl which must be included in the body.
--%>
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
.headerCell {
    padding-right: 10px; 
    padding-top: 2px; 
    padding-bottom: 2px;
}
</STYLE>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<TABLE BORDER=0>
    <TR VALIGN="TOP">
        <TD COLSPAN=2>
            <SPAN CLASS="mainHeading">
            <%=title%>
            </SPAN>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText>    
        <TD WIDTH=500><%=helperText%>
        </TD>
        <TD ALIGN="RIGHT">
        </TD>
    </TR>
</TABLE>

<%@ include file="miniSearch.jspIncl" %>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR>
    	<TD ALIGN="RIGHT">
			<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
			<%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>   
			</DIV>
    	</TD>
    </TR>
    <TR>
        <TD COLSPAN=3>
        
<FORM NAME="JobForm" METHOD="POST">

<!-- Data Table  -->             
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" CLASS="list">
<TBODY>
<COL> <!-- Radio button -->
<COL> <!-- Priority -->
<COL> <!-- Job ID -->
<COL WIDTH=130> <!-- Job Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Word Count -->
<COL> <!-- Date Created -->
<COL> <!-- Job Status -->

<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
	<TD CLASS="headerCell"></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PLANNED_DATE%>"><%=bundle.getString("lb_planned_completion_date")%></A><%=jobPlannedDateSortArrow%></TD>
	<TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_STATUS%>"><%=bundle.getString("lb_job_status")%></A><%=jobStatusSortArrow%></TD> 
</TR>
<%=request.getAttribute(JobManagementHandler.JOB_SCRIPTLET)%>
</TABLE>
<!-- End Data Table  -->

<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</FORM>
 
</DIV>
</BODY>
</HTML>