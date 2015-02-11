<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import= "com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.util.progress.ProcessStatus,
            com.globalsight.everest.edit.offline.OfflineEditManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.everest.workflowmanager.Workflow,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.modules.Modules,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="startdownload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);

    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String urlStartDownload = startdownload.getPageURL() +
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_START_DOWNLOAD;

    // labels
    String pagetitle = bundle.getString("msg_download_progress_title");
    String lbDone = bundle.getString("lb_done");
    String lbCancel = bundle.getString("lb_cancel");
    String lbJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");

    // normal UI messages
    String msgInProgress =  bundle.getString("msg_download_in_progress");
    String msgActivityAccepted =  bundle.getString("msg_download_activity_accepted");
    String msgPleaseWait =  bundle.getString("msg_download_please_wait");
    String msgChangeDefaults =  bundle.getString("msg_download_change_defaults");


    Task task = (Task)TaskHelper.retrieveObject(
      session, WebAppConstants.WORK_OBJECT);
    Workflow workflow = task.getWorkflow();
    Job job = workflow.getJob();
    String jobName = job.getJobName();

    // Get parameter names
    String requestParamFileFormat = OfflineConstants.FORMAT_SELECTOR;
    String requestParamEditor = OfflineConstants.EDITOR_SELECTOR;
    String requestParamEncoding = OfflineConstants.ENCODING_SELECTOR;
    String requestParamPtagFormat = OfflineConstants.PTAG_SELECTOR;
    String requestParamEditExact = OfflineConstants.TM_EDIT_TYPE;
    String requestParamResInsMode = OfflineConstants.RES_INS_SELECTOR;
    String requestParamAcceptDownloadRequest = OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD;
    String requestParamDownloadAction = WebAppConstants.DOWNLOAD_ACTION;
    String startDownload = WebAppConstants.DOWNLOAD_ACTION_START_DOWNLOAD;

    // Get cookie names
    String cookieNameFileFormat = OfflineConstants.COOKIE_FILE_FORMAT;
    String cookieNameEditor = OfflineConstants.COOKIE_EDITOR;
    String cookieNameEncoding = OfflineConstants.COOKIE_ENCODING;
    String cookieNamePtagFormat = OfflineConstants.COOKIE_PTAG_FORMAT;
    String cookieNameEditExact = OfflineConstants.COOKIE_EDIT_EXACT;
    String cookieNameResInsMode = OfflineConstants.COOKIE_RES_INS_MODE;

    String fileFormatValue = (String)request.getParameter(requestParamFileFormat);
    String editorValue = (String)request.getParameter(requestParamEditor);
    String encodingValue = (String)request.getParameter(requestParamEncoding);
    String ptagFormatValue = (String)request.getParameter(requestParamPtagFormat);
    String editExactValue = (String)request.getParameter(requestParamEditExact);
    String resInsModeValue = (String)request.getParameter(requestParamResInsMode);
%>
<HTML>
<HEAD>
<TITLE><%= pagetitle %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;

function doDownloadRequest()
{
    // Pass the download request to the back end and show a progress bar.
    document.location.replace("<%= urlStartDownload %>" +
        "&<%= requestParamAcceptDownloadRequest %>=true" +
        "&<%= requestParamDownloadAction %>=<%= startDownload %>" +
        "&<%= requestParamFileFormat %>=<%= fileFormatValue %>" +
        "&<%= requestParamEditor %>=<%= editorValue %>" +
        "&<%= requestParamEncoding %>=<%= encodingValue %>" +
        "&<%= requestParamPtagFormat %>=<%= ptagFormatValue %>" +
        "&<%= requestParamResInsMode %>=<%= resInsModeValue %>" +
        "&<%= requestParamEditExact %>=<%= editExactValue %>");
}

function doOnLoad()
{
   loadGuides();
   doDownloadRequest();
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad();"
 LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE  width="100%"CELLPADDING="2" CELLSPACING="0" BORDER="0">
  <TR>
    <TD NOWRAP><SPAN CLASS="mainHeading"><B><%= msgInProgress %></B></SPAN></TD>
  </TR>
  <TR>
    <TD><SPAN CLASS="standardText">
      <%= msgActivityAccepted %><BR>
      <i><%= msgPleaseWait %></i></SPAN>
    </TD>
  </TR>
  <TR>
    <TD>
      <SPAN CLASS="standardText"><%= msgChangeDefaults %></SPAN>
    </TD>
  </TR>
</TABLE>

</BODY>
</HTML>
