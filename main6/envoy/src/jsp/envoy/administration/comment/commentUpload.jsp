<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
      com.globalsight.util.edit.EditUtil,
	  com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.everest.comment.CommentUpload,
      com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
      com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.foundation.WorkObject,
      com.globalsight.everest.comment.Comment,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
      com.globalsight.util.resourcebundle.ResourceBundleConstants,
      com.globalsight.util.resourcebundle.SystemResourceBundle,
      com.globalsight.util.GlobalSightLocale,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.foundation.WorkObject,
      java.util.Locale,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="referenceUpload" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancelJob" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="done" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>

<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
// Use this information to create a temporary directory.
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
User userWelcome = (User)sessionMgr.getAttribute(WebAppConstants.USER);
String userId = userWelcome.getUserId();

WorkObject wo = (WorkObject)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
String wid = "";
String cancelUrl = null;
if(wo != null)
{
    if( wo instanceof Task )
    {
        Task task = (Task)wo;
        wid = (new Long(task.getId())).toString();
        cancelUrl = cancel.getPageURL() +
                    "&" + WebAppConstants.TASK_ACTION + 
                    "=" + WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL  +
                    "&" + CommentConstants.DELETE +
                    "=" + WebAppConstants.COMMENT_REFERENCE_NO_DELETE;
    }
    else if( wo instanceof Job )
    {
        Job job = (Job)wo;
        wid = (new Long(job.getId())).toString();
        cancelUrl = cancelJob.getPageURL() +
                    "&" + WebAppConstants.TASK_ACTION + 
                    "=" + WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL  +
                    "&" + CommentConstants.DELETE +
                    "=" + WebAppConstants.COMMENT_REFERENCE_NO_DELETE;
    }
    else if( wo instanceof Workflow )
    {
        Workflow wf = (Workflow)wo;
        wid = (new Long(wf.getId())).toString();
        cancelUrl = cancel.getPageURL() +
                    "&" + WebAppConstants.TASK_ACTION + 
                    "=" + WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL  +
                    "&" + CommentConstants.DELETE +
                    "=" + WebAppConstants.COMMENT_REFERENCE_NO_DELETE;
    }
}
String tmpDir = WebAppConstants.COMMENT_REFERENCE_TEMP_DIR + wid + userId;

String doneUrl = done.getPageURL() + 
            "&" + WebAppConstants.TASK_ACTION + 
            "=" + WebAppConstants.TASK_ACTION_SAVECOMMENT  +
            "&" + CommentConstants.DELETE +
            "=" + WebAppConstants.COMMENT_REFERENCE_NO_DELETE;

String comments = (String)sessionMgr.getAttribute(WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT);
String sampleComments="";
int partialComment = WebAppConstants.PARTIAL_COMMENT_LENGTH;

if(comments == null)
{
    comments = request.getParameter(WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT);
    sessionMgr.setAttribute(WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT, comments);
}
if(comments.length() < partialComment)
{
    sampleComments = comments;
}
else
{
    sampleComments = comments.substring(0, partialComment - 1 ) + "...";
}

String url_upload = referenceUpload.getPageURL();
String url_upload_true = referenceUpload.getPageURL() +
            "&" + WebAppConstants.COMMENT_REFERENCE_RESTRICTED +
            "=" + WebAppConstants.COMMENT_REFERENCE_TRUE;

String lb_title = bundle.getString("lb_upload_comment_reference");
String lb_help = bundle.getString("lb_help");
String lb_cancel = bundle.getString("lb_cancel");
String lb_done = bundle.getString("lb_done");
String lb_attach = bundle.getString("lb_attach");
String lb_comments = bundle.getString("lb_comments");
String lb_upload = bundle.getString("lb_upload");

String lb_selectFile = bundle.getString("lb_select_file");
String lb_selectLanguages = bundle.getString("lb_select_languages");

String msg_enter_data = bundle.getString("lb_please_enter_data"); 
String lb_pleaseEnterData = msg_enter_data;

%>
<!-- This JSP is: envoy/administration/comment/commentUpload.jsp -->
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var helpFile = "<%=bundle.getString("help_job_comment_attach_files")%>";
var needWarning = false;
var guideNode = "myActivitiesComments";


reload = false;
function prepToClose()
{
  if (!reload)
  {
    window.close();
  }
}

function isReady(form)
{
    var field = form.filename;
    var filename = field.value;

    if (filename == null || filename == "")
    {
        alert("<%=lb_pleaseEnterData%>");
        field.focus();
        return false;
    }
    else
    {
        if(uploadForm.restricted.checked==true)
        {
           uploadForm.action = "<%=url_upload_true%>";
        }else
        {
        }
        idBody.style.cursor = "wait";
        uploadForm.idSubmit.style.cursor = "wait";
        return true;
    }
}

function showMessage(message)
{
    if (message != "")
    {
        window.showModalDialog("/globalsight/envoy/administration/comment/commentUploadMessage.jsp",
            message,
            "center:yes; help:no; resizable:yes; status:no; " +
            "dialogWidth: 300px; dialogHeight: 180px; ");
    }
}

function doOnload()
{
    self.focus();
}

function doOnunload()
{
    prepToClose();
}

function submitForm()
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.deleteForm;
    }
    else
    {
        theForm = document.all.deleteForm;
    }

    if (optionTest(theForm))
    {
        theForm.submit();
    }
}

</SCRIPT>

<SCRIPT LANGUAGE="JavaScript">
    function helpSwitch() 
    {  
       // The variable helpFile is defined in each JSP
       helpWindow = window.open(helpFile,'helpWindow','resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
       helpWindow.focus();
    }


function saveForm(p_action)
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.uploadForm;
    }
    else
    {
        theForm = document.all.uploadForm;
    }
	if (p_action == "<%=WebAppConstants.COMMENT_REFERENCE_ACTION_CANCEL%>")
	{
       uploadForm.action = "<%=cancelUrl%>";
	}
	theForm.submit();
}
</SCRIPT>
</HEAD>


<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
    id="idBody" onLoad="loadGuides(); doOnload()" >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 WIDTH="100%">
    <TR>
        <TD><SPAN CLASS="mainHeading"><%=lb_title%></SPAN></TD>
    </TR>
    <TR>
    <TD WIDTH=500 CLASS="standardText">
    <%=bundle.getString("helper_text_comment_reference_upload")%>
    <BR>
    </TD>
    </TR>
</TABLE>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
    <TD><B><%=lb_comments%>:</B><BR><%=sampleComments%></TD>
</TR>
</TABLE>
<P>
<FORM NAME="uploadForm" METHOD="POST" ACTION="<%=url_upload%>"
        ENCTYPE="multipart/form-data" onSubmit="return isReady(this)"
        CLASS="standardText">

<INPUT TYPE="HIDDEN" NAME="tmpdir" VALUE="<%=tmpDir%>"></INPUT>

<P>
      <SPAN class="standardTextBold"><%=lb_selectFile%></SPAN>
<BR>
      <INPUT TYPE="file" SIZE="60" NAME="filename">
      <BR>
        <INPUT TYPE="CHECKBOX" NAME="restricted" VALUE="true"> Restrict Access 
      <BR>

      <INPUT TYPE="SUBMIT" VALUE="<%=lb_upload%>" NAME="idSubmit">
      <INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
        ONCLICK="location.replace('<%=cancelUrl%>')">
</P>
</FORM>
<P></P>
</DIV>
</BODY>
</HTML>
