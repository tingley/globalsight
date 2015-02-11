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
                com.globalsight.everest.cvsconfig.*,
                java.util.ArrayList"
		session="true"
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:useBean id="done" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="fileImport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GB18030">
<title>CVS Job Upload Result</title>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

</head>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = 
    (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

String doneUrl = done.getPageURL();
String fImportUrl = fileImport.getPageURL();
String files = request.getParameter("selectFiles");
String jobName = (String)session.getAttribute("jobName");
String notes = (String)session.getAttribute("notes");
String projectId = (String)session.getAttribute("projectId");
String projectName = (String)session.getAttribute("projectName");
String sourceLocale = (String)session.getAttribute("sourceLocale");
CVSServer server = (CVSServer)session.getAttribute("cvsServer");
CVSModule module = (CVSModule)session.getAttribute("cvsModule");

String currentFolder = "";
%>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "myJobs";
var objectName = "";
var helpFile = "<%=bundle.getString("help_customer_upload")%>";
function submitForm(action) {
	if (action == "done")
	{
		uploadResult.action = "<%=doneUrl%>";
	}
	else
	{
		uploadResult.action = "<%=fImportUrl%>" + "&jobType=cvsJob";
	}
	uploadResult.submit();
}
</script>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
    ONLOAD="loadGuides()">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" WIDTH=400 CLASS="detailText" >
        <TR CLASS="tableHeadingBasic">
            <TD COLSPAN="3" NOWRAP>&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_upload_summary") %></TD>
        </TR>        
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_job_name") %>:<B></TD>
            <TD><%=jobName%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_cvs_mm_source_locale") %>:<B></TD>
            <TD><%=sourceLocale%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_cvs_job_notes") %>:<B></TD>
            <TD><%=notes%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_project") %>:<B></TD>
            <TD><%=projectName%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_cvs_server") %>:<B></TD>
            <TD><%=server.getName()%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_cvs_repository") %>:<B></TD>
            <TD><%=server.getRepository()%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_cvs_module") %>:<B></TD>
            <TD><%=module.getName()%></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD nowrap><B><%=bundle.getString("lb_cvs_job_files") %><B></TD>
            <TD>
            	<%
            	if (files != null && !files.trim().equals("")) {
            		ArrayList value = CVSUtil.saveData(module, jobName, sourceLocale, projectId, projectName, notes, files, userHeader);
            		currentFolder = (String)value.get(0);
            		HashSet fileList = new HashSet();
	            	ArrayList<String> results = (ArrayList<String>)value.get(1);
		              for (String file : results) {
		            	  out.println(file);
		              }
		              sessionMgr.setAttribute("fileList", (HashSet<String>)value.get(2));
		              sessionMgr.setAttribute("jobType", "cvsJob");
            	}
            	%>
            </TD>
        </TR>
     </TABLE>
<!-- End Details table -->

</TR>
</TABLE>

<FORM NAME="uploadResult" METHOD="post">
<input type="hidden" name="currentFolder" value="<%=currentFolder %>"></input>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD><input type="button" name="done" value="<%=bundle.getString("lb_done") %>" onclick="submitForm('done')"></input>&nbsp;&nbsp;
<input type="button" name="import" value="<%=bundle.getString("lb_import") %>" onclick="submitForm('import')"></input>&nbsp;&nbsp;
</TD>
</TR>
<TR>
</TR>
</TABLE>
</FORM>
	                                     
</BODY>
</HTML>
