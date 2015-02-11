<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.ResourceBundle,
            java.util.Enumeration"
    session="true" %>

<jsp:useBean id="editPages" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%    
          
    ResourceBundle bundle = PageHandler.getBundle(session);
    String editPagesURL = editPages.getPageURL();
    String detailsURL = jobDetails.getPageURL();

    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String jobName = (String)sessionMgr.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET);
    String jobId = ((Long)sessionMgr.getAttribute(JobManagementHandler.JOB_ID)).toString();
    String pages = (String)request.getAttribute(JobManagementHandler.PAGES_IN_JOB);
    String title = bundle.getString("lb_enter_pages") + " " + jobName;

    detailsURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
    editPagesURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
  var needWarning = false;
  var objectName = "";
  var guideNode = "myJobs";
  var helpFile = "<%=bundle.getString("help_edit_pages")%>";

  function submitForm(action)
  {
     if (action == "cancel")
     {
        document.jobForm.formAction.value = "cancel";
        document.jobForm.submit();
     }
     else
     {
        document.jobForm.formAction.value = "save";
        // Make sure the Job Name is not null
        if (document.jobForm.pages.value == "")
        {
           alert("<%=bundle.getString("jsmsg_enter_pages")%>"); 
           jobForm.pages.focus();
           return false;
        }

        // Make sure the value entered is a number
        if (isNaN(jobForm.pages.value))
        {
           alert("<%=bundle.getString("jsmsg_enter_pages_number")%>"); 
           jobForm.pages.focus();
           return false;
        }

        document.jobForm.submit();
     }
  }


  function loadPage()
  {
     loadGuides();
     jobForm.pages.focus();
  }

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_costing_pages")%>
</TD>
</TR>
</TABLE>
<P>

<FORM NAME="jobForm" ONSUBMIT="submitForm(); return false;" METHOD="POST" 
    ACTION="<%=editPagesURL%>">

<SPAN CLASS="standardText"><%=bundle.getString("lb_pages")%>: </SPAN>
<INPUT TYPE="TEXT" SIZE=4 MAXLENGTH=10 NAME="pages" VALUE="<%=pages%>"><P>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ONCLICK="submitForm('cancel')">
<INPUT TYPE="SUBMIT" VALUE="<%=bundle.getString("lb_save")%>">
<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
</FORM>

</DIV>
</BODY>
</HTML>
