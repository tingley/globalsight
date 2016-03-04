<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.page.TargetPage,
         com.globalsight.everest.page.UnextractedFile,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.taskmanager.TaskAssignee,
         com.globalsight.everest.taskmanager.TaskInfo,
         com.globalsight.everest.util.system.SystemConfiguration,
         com.globalsight.everest.util.system.SystemConfigParamNames,
         com.globalsight.util.modules.Modules,
         com.globalsight.everest.costing.Currency,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.workflowmanager.Workflow,
         com.globalsight.everest.workflow.WfTaskInfo,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.everest.foundation.Timestamp,
         java.text.DateFormat, 
         java.text.NumberFormat,
         java.util.Vector,
         java.util.Date,
         java.util.TimeZone,
         java.io.File,
         java.util.ResourceBundle" session="true" %>
<jsp:useBean id="self"   class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
   SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
   ResourceBundle bundle = PageHandler.getBundle(session);
   String title = bundle.getString("lb_workflow_details");
   String history = bundle.getString("lb_workflow_history");
   String labelCompletionDate = "Planned Completion Date (mm/dd/yyyy)"; //bundle.getString("lb_planned_completion_date");
   String lbSave = bundle.getString("lb_save");   
   String lbClose = bundle.getString("lb_close");   
   
   String dayField = "dayField";
   String monthField = "monthField";
   String yearField = "yearField";
      
   Workflow wf = (Workflow)request.getAttribute("workflow"
                    /*JobManagementHandler.WORKFLOW*/);
   Timestamp chosenTimestamp = null;
   int finalYear = 2020;

   String wfIdParam = (String)request.getAttribute(JobManagementHandler.WF_ID);
   String jobIdParam = (String)request.getAttribute(JobManagementHandler.JOB_ID);
   String selfURL = self.getPageURL() + "&" 
                     + JobManagementHandler.WF_ID + "=" 
                     + wfIdParam + "&"
                     + JobManagementHandler.JOB_ID + "="
                     + jobIdParam + "&"
                     + "action=save&" 
                     + "wfId="+wf.getId();
   String viewUrl = self.getPageURL() + "&"
                     + JobManagementHandler.WF_ID + "="
                     + wfIdParam + "&"
                     + JobManagementHandler.JOB_ID + "="
                     + jobIdParam + "&"
                     + "wfId="+wf.getId();
   boolean enableDateFields = Workflow.DISPATCHED.equals(wf.getState());
   String disabled = enableDateFields ? "" : "DISABLED";
   boolean edit = false; // needed in activities.jspIncl

   // get locale and time zone for date formatting
   Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
   Timestamp ts = new Timestamp(timeZone);
   ts.setLocale(uiLocale);
   
   //DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(
     //  DateFormat.SHORT, DateFormat.SHORT, Locale.US  );
       
   NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
   numberFormat.setMaximumFractionDigits(1);

   SystemConfiguration sysConfig = SystemConfiguration.getInstance();
    boolean useSSL = sysConfig.getBooleanParameter(SystemConfigParamNames.USE_SSL);
    String httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
    if (useSSL == true)
    {
        httpProtocolToUse = WebAppConstants.PROTOCOL_HTTPS;
    }
    else
    {
        httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
    }

%>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<HTML>
<!-- This JSP is: envoy/projects/workflows/workflowActivities.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>

<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/css/workflow/ui.css">
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/css/workflow/view.css">
<%@ include file="/includes/workflow/l18n.js"%>
<SCRIPT SRC="/globalsight/jquery/jquery-1.6.4.min.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/ajax.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/ui.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/line.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/shape.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/Utils.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/model.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/workflowInstance/view.js" type="text/javascript"></SCRIPT>

<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var w_viewer;
</SCRIPT>
<STYLE type="text/css">
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
	padding: 0px;
}
</STYLE>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" CLASS=standardText>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 20px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS=mainHeading>
<%= title %>: <%=request.getAttribute(JobManagementHandler.WF_NAME)%>
</SPAN>
<P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
<TR>
<TD WIDTH="100%" HEIGHT="410">
    <div id="viewport"> 
    <DIV id="canvasDiv">
        <CANVAS id="canvas" style="position: absolute;"></CANVAS>
        <CANVAS id="snapLineCanvas" style="position: absolute;"></CANVAS>
     </DIV>
    </div>
    
</TD>
</TR>
</TABLE>



<amb:permission name="<%=Permission.JOB_WORKFLOWS_ESTCOMPDATE%>" >
<%@ include file="activities.jspIncl" %>
</amb:permission>
<P>

<SPAN CLASS="standardTextBold">
<%=history%>
</SPAN>
<!-- Workflow History table -->
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" CLASS="list">
    <TR CLASS=tableHeadingBasic>
        <TD><%=bundle.getString("lb_activity")%></TD>
        <TD><%=bundle.getString("lb_role")%></TD>
        <TD><%=bundle.getString("lb_acceptor")%></TD>
        <TD><%=bundle.getString("lb_duration")%></TD>
        <TD><%=bundle.getString("lb_date_completed")%></TD>
        <TD><%=bundle.getString("status")%></TD>
    </TR>
    <%=request.getAttribute(JobManagementHandler.WORKFLOW_ACTIVITIES_SCRIPTLET)%>
</TABLE>
<!-- End Workflow History table -->
<P>

<!-- Primary Unextracted Target Files -->
<% 
ArrayList targetPages = (ArrayList)request.getAttribute(
                    JobManagementHandler.WORKFLOW_PRIMARY_UNEXTRACTED_TARGET_FILES);
if(targetPages != null && targetPages.size() > 0)
{
%>
    <SPAN CLASS="standardTextBold">
    <%=bundle.getString("lb_primary_unextracted_target_files")%>
    </SPAN>
    <BR>
    <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0>
    <%
    Iterator itTargetPages = targetPages.iterator();
    while (itTargetPages.hasNext()) 
    {%>
    <TR VALIGN="TOP">
    <TD>
    <IMG SRC="/globalsight/images/file_unextracted.gif" 
        ALT="<%=bundle.getString("lb_file_unextracted")%>" 
        WIDTH=13 HEIGHT=15>  
    </TD>
    <TD CLASS="standardText">    
    <%
    TargetPage targetPage = (TargetPage)itTargetPages.next();

    // "putf" is Primary Unextracted Target File
    UnextractedFile putf = (UnextractedFile)targetPage.getPrimaryFile();

    // Get the Last Modified date and format it
    Date dt = putf.getLastModifiedDate();
    ts.setDate(dt);
    String lastModified = dt == null ? "--" : ts.toString();
    
    // Get the file size and format it
    long sizePutf = putf.getLength();
    sizePutf = sizePutf < 3 ? 0 : sizePutf; // adjust
    if (sizePutf != 0)
    {
        long r = sizePutf%1024;
        sizePutf = (r!=0) ? ((sizePutf/1024)+1) : sizePutf;  //round up
    }
    String sizePutfStr = numberFormat.format(sizePutf);
    String strhref=WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING + putf.getStoragePath();
    %> 
    <A HREF="<%out.print(strhref.replace("\\", "/"));%>" 
        CLASS="standardHREF"><%out.print(putf.getStoragePath());%></A>  
    <%out.print(sizePutfStr);%>K<BR> 
    <SPAN CLASS="smallText">
    <%out.print(bundle.getString("lb_last_modified") +  ": " + lastModified);%><BR>
    <%out.print(bundle.getString("lb_modified_by") +  ": " + UserUtil.getUserNameById(putf.getLastModifiedBy()));%>
    </SPAN>
    </TD>
    </TR>

    <%}%>
    </TABLE>
    <P>
<%}%>

<!-- Secondary Target Files -->
<%
ArrayList stfArrayList = (ArrayList)request.getAttribute(
                    JobManagementHandler.WORKFLOW_SECONDARY_TARGET_FILES);
if(stfArrayList != null && stfArrayList.size() > 0)
{
%>
    <SPAN CLASS="standardTextBold">
    <%=bundle.getString("lb_secondary_target_files")%>
    </SPAN>
    <BR>
    <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0>
    <%
    Iterator itStf = stfArrayList.iterator();
    while (itStf.hasNext()) 
    {
    %>
    <TR VALIGN="TOP">
    <TD>
    <IMG SRC="/globalsight/images/file_unextracted.gif" 
        ALT="<%=bundle.getString("lb_file_unextracted")%>" 
        WIDTH=13 HEIGHT=15>
    </TD>
    <TD CLASS="standardText">
    <%
    SecondaryTargetFile stf = (SecondaryTargetFile)itStf.next();

    // Get the Last Modified date and format it
    ts.setDate(new Date(stf.getLastUpdatedTime()));
    
    // Get the file size and format it
    long sizeStf = stf.getFileSize();
    sizeStf = sizeStf < 3 ? 0 : sizeStf; // adjust
    if(sizeStf != 0)
    {
        long rStf = sizeStf%1024;
        sizeStf = (rStf!=0) ? ((sizeStf/1024)+1) : sizeStf;  //round up
    }
    String sizeStfStr = numberFormat.format(sizeStf);
    String stfHref = WebAppConstants.STF_FILES_URL_MAPPING + stf.getStoragePath();
    %>
    <A HREF="<%out.print(stfHref.replace("\\", "/"));%>" 
         target="_blank" CLASS="standardHREF"><%out.print(stf.getStoragePath());%></A>  
    <%out.print(sizeStfStr);%>K<BR>
    <SPAN CLASS="smallText">
    <%out.print(bundle.getString("lb_last_modified") +  ": " + ts.toString());%><BR>
    <%out.print(bundle.getString("lb_modified_by") +  ": " + UserUtil.getUserNameById(stf.getModifierUserId()));%>
    </SPAN>
    </TD>
    </TR>
    <%}%>
    </TABLE>
<%}%>

<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
  <TR>
    <TD align="center">
      <input type="button" name="<%=lbClose%>" value="<%=lbClose%>"
        onclick="window.close()">
    </TD>
  </TR>
</TABLE>
</DIV>
</BODY>
</HTML>
