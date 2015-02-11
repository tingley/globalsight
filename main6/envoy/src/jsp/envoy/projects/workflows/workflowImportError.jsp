<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
				 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
				 java.util.ResourceBundle" session="true" %>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_workflow_import_error") + 
        bundle.getString("lb_colon") +  " "  +
        request.getAttribute(JobManagementHandler.TRGT_LOCALE_SCRIPTLET);
	String lb_previous = bundle.getString("lb_previous");
	String lb_job = bundle.getString("lb_job");
%>		 
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "myJobs";
var objectName = "";
var helpFile = "<%=bundle.getString("help_main")%>";

function submitForm()
{
   if ( !isRadioChecked(document.JobForm.page) ) 
   {
      return false; 
   }
   else
   {
      document.JobForm.submit();      
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

<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
<TR CLASS="standardText">
<TD NOWRAP><B><%=lb_job%>:</B></TD>
<TD ROWSPAN="8" WIDTH="10">&nbsp;</TD>
<TD><%=request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_source_locale")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.SRC_LOCALE_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_target_locale")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.TRGT_LOCALE_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_initiator")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.JOB_INITIATOR_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_project")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.PROJECT_NAME_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_loc_profile")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.L10NPROFILE_NAME_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_date_created")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.JOB_DATE_CREATED_SCRIPTLET)%></TD>
</TR>

<TR CLASS="standardText">
<TD NOWRAP><B><%=bundle.getString("lb_data_source")%>:</B></TD>
<TD><%=request.getAttribute(JobManagementHandler.DATA_SOURCE_SCRIPTLET)%></TD>
</TR>
</TABLE>

<P>
 
<SPAN CLASS=standardText><B>Pages</B></SPAN>
<BR>

<FORM NAME="JobForm" METHOD="POST">
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
    <COL>               <!-- checkbox -->
    <COL WIDTH="200">   <!-- pages -->
    <COL>               <!-- status -->
    <COL WIDTH="300">   <!-- message -->
    <TBODY>
    <TR CLASS="tableHeadingBasic">
        <TD></TD>
        <TD><%=bundle.getString("lb_page")%></TD>
        <TD><%=bundle.getString("lb_status")%></TD>
        <TD><%=bundle.getString("lb_message")%></TD>
    </TR>
<%=request.getAttribute(JobManagementHandler.JOB_CONTENT_SCRIPTLET)%>
    </TBODY>
</TABLE>
<P>
<INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" 
    ONCLICK="history.go(-1)">       
</FORM>
</DIV>
</BODY>
</HTML>
