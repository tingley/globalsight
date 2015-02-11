<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.permission.Permission,
                com.globalsight.everest.webapp.pagehandler.administration.imp.SelectFileHandler,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler, 
                java.util.ResourceBundle, 
                java.util.ArrayList"
		session="true"
%>
<jsp:useBean id="done" scope="request"
  class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="fileImport" scope="request"
  class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String doneURL = done.getPageURL();
    String importURL = fileImport.getPageURL();
    String lb_done = bundle.getString("lb_done");
    String lb_import = bundle.getString("lb_import");
    String title = bundle.getString("lb_upload_summary");     
%>
<HTML>
<!-- This JSP is: envoy/administration/customer/uploadResult.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "Upload";
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_customer_upload_result")%>";


function submitForm(action)
{
   form = document.uploadResult;
   if (action == "<%=WebAppConstants.DONE%>")
   {
      form.action = "<%=doneURL%>";
      form.submit();
   }
   else if (action == "<%=WebAppConstants.IMPORT%>")
   {
 <%
      session.setAttribute(SelectFileHandler.FOLDER_SELECTED,
                           sessionMgr.getAttribute("path"));
 %>
      form.action = "<%=importURL%>";
      form.submit();
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
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" WIDTH=400 CLASS="detailText" >
        <TR CLASS="tableHeadingBasic">
            <TD COLSPAN="3" NOWRAP>&nbsp;&nbsp;&nbsp;<%=title%></TD>
        </TR>        
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_date_upload_completed")%>:<B></TD>
            <TD><%=sessionMgr.getAttribute("uploadTime")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_job_name")%>:<B></TD>
            <TD><%=sessionMgr.getAttribute("jobName")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_source_locale")%>:<B></TD>
            <TD><%=sessionMgr.getAttribute("srcLocale")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_job_notes") %>:<B></TD>
            <TD><%=sessionMgr.getAttribute("notes")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=sessionMgr.getAttribute(WebAppConstants.PROJECT_LABEL)%>:<B></TD>
            <TD><%=sessionMgr.getAttribute(WebAppConstants.PROJECT_NAME)%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_uploaded_by")%>:<B></TD>
            <TD><%=sessionMgr.getAttribute("uploader")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_path")%>:<B></TD>
            <TD><%=sessionMgr.getAttribute("path")%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%=bundle.getString("lb_num_of_files_uploaded")%>:<B></TD>
            <TD><%=sessionMgr.getAttribute("numOfFiles")%></TD>
        </TR>                
     </TABLE>
<!-- End Details table -->

</TR>
</TABLE>

<FORM NAME="uploadResult" METHOD="post">
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS="standardText">
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD><B><%=bundle.getString("msg_customer_upload")%><B></TD>
</TR>
<amb:permission name="<%=Permission.IMPORT%>" >
<TR>
<TD><B><%=bundle.getString("msg_customer_import")%><B></TD>
</TR>
</amb:permission>
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD>
  <INPUT TYPE="BUTTON" NAME="<%=lb_done%>" VALUE="<%=lb_done%>" 
  onClick="submitForm('<%=WebAppConstants.DONE%>');">
<amb:permission name="<%=Permission.IMPORT%>" >
  <INPUT TYPE="BUTTON" NAME="<%=lb_import%>" VALUE="<%=lb_import%>"
   onClick="submitForm('<%=WebAppConstants.IMPORT%>');">
</amb:permission>
</TD>
</TR>
</TABLE>
</FORM>
	                                     
</BODY>
</HTML>
