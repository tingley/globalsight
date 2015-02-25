<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
				 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.permission.Permission,
				 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
				 java.util.ResourceBundle" session="true" %>
<jsp:useBean id="pending" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
   	ResourceBundle bundle = PageHandler.getBundle(session);
	String pendingURL = pending.getPageURL();
	String detailsURL = jobDetails.getPageURL() + "&fromImpErr=true";
	String subTitle= "";
	String title= bundle.getString("lb_import_error");
   String lbCancel= bundle.getString("lb_cancel");
   String lbClearErrors= bundle.getString("lb_clear_errors");
   String lbDiscardJob= bundle.getString("lb_discard_job");
   String allPageError = (String) request.getAttribute("allPageError");
   String cleanMsg = "true".equals(allPageError) ? bundle.getString("msg_clean_all_file") : bundle.getString("jsmsg_import_error_clear_errors");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "myJobs";
var objectName = "";
var helpFile = "<%=bundle.getString("help_import_error")%>";

function cleanError()
{
	if(confirm('<%=cleanMsg%>')){
		location.replace('<%=pendingURL%>&<%=JobManagementHandler.CANCEL_IMPORT_ERROR_PAGES_PARAM%>=<%=request.getParameter(JobManagementHandler.JOB_ID)%>');
	}
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P>

    <!-- Details table -->
                <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_job")%>:<B></TD>
                        <TD ROWSPAN="9" WIDTH="10">&nbsp;</TD>
                        <TD><%=request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_initiator")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.JOB_INITIATOR_SCRIPTLET)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_source_locale")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.SRC_LOCALE_SCRIPTLET)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_project")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.PROJECT_NAME_SCRIPTLET)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_loc_profile")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.L10NPROFILE_NAME_SCRIPTLET)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_date_created")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.JOB_DATE_CREATED_SCRIPTLET)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_source_word_count_total")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.TOTAL_SOURCE_PAGE_WC)%></TD>
                    </TR>
                    <TR CLASS="standardText">
                        <TD NOWRAP><B><%=bundle.getString("lb_priority")%>:<B></TD>
                        <TD><%=request.getAttribute(JobManagementHandler.JOB_PRIORITY_SCRIPTLET)%></TD>
                     </TR>
                </TABLE>
            <!-- End Details table -->
		<P>
        <!-- Pages table -->
		<TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" style="width:100%;">
		<TR CLASS="tableHeadingBasic">
			<TD NOWRAP style="width:44%"><%=bundle.getString("lb_pages")%></TD>
			<TD ALIGN="CENTER" NOWRAP style="width:12%"><%=bundle.getString("lb_source_word_count")%></TD>
			<TD NOWRAP style="width:9%"><%=bundle.getString("lb_status")%></TD>
			<TD NOWRAP style="width:35%"><%=bundle.getString("lb_message")%></TD>
		</TR>
		<%=request.getAttribute(JobManagementHandler.JOB_CONTENT_SCRIPTLET)%>
		<TR>
			<TD COLSPAN="4" NOWRAP><IMG SRC="/globalsight/images/spacer.gif" WIDTH="1" HEIGHT="5"></TD>
		</TR>
		</TABLE>
        <!-- End Pages table -->
        <P>
<% if (request.getParameter("fromDetails") != null) { %>
        <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
            ONCLICK="location.replace('<%=detailsURL%>')">   
<% } else { %>
        <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
            ONCLICK="location.replace('<%=pendingURL%>')">   
<% } %>
    <amb:permission name="<%=Permission.JOBS_CLEAR_ERRORS%>" >
        <INPUT TYPE="BUTTON" NAME="<%=lbClearErrors%>" VALUE="<%=lbClearErrors%>" 
            ONCLICK="cleanError()">    
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DISCARD%>" >
        <INPUT TYPE="BUTTON" NAME="<%=lbDiscardJob%>" VALUE="<%=lbDiscardJob%>" 
            ONCLICK="if(confirm('<%=bundle.getString("jsmsg_import_error_discard_job")%>')){location.replace('<%=pendingURL%>&<%=JobManagementHandler.DISCARD_JOB_PARAM%>=<%=request.getParameter(JobManagementHandler.JOB_ID)%>')}">  
    </amb:permission>
</DIV>
</BODY>
</HTML>
