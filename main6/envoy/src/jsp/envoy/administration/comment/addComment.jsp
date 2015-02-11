<%@ page
     	contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
				 com.globalsight.everest.taskmanager.Task, 
                 com.globalsight.everest.comment.Comment,
				 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
				 com.globalsight.everest.webapp.pagehandler.PageHandler,
				 com.globalsight.util.resourcebundle.ResourceBundleConstants,
				 com.globalsight.util.resourcebundle.SystemResourceBundle,
                 com.globalsight.everest.jobhandler.Job,
                 com.globalsight.everest.taskmanager.Task,
                 com.globalsight.everest.workflowmanager.Workflow,
                 com.globalsight.everest.foundation.WorkObject,
                 com.globalsight.everest.taskmanager.Task,
                 com.globalsight.everest.foundation.WorkObject,
                 com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
				 java.util.List,
				 java.util.Locale, 
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants"
		session="true" 
%>
				 
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="attach" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="attachExisting" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobCommentsDone" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobCommentsCancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%
    //locale bundle labels
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Comment comment = (Comment)sessionMgr.getAttribute("comment");
    String title = bundle.getString("action_add_comment");
    String lbDone = bundle.getString("lb_done");
    String lbCancel = bundle.getString("lb_cancel");
    String lbAttach = bundle.getString("lb_attach");
    String lbJobName = bundle.getString("lb_job_name");
    WorkObject wo = (WorkObject)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
    String doneUrl = "";
    String cancelUrl = "";
    String msgDlgHeading = "";
    String helpFile = "";
    String saveCommStatus;
    String jobName = "";
    String attachStr = "";
    boolean isTaskComment = true; 
    String commentStr = (String)sessionMgr.getAttribute("taskComment");
    if (commentStr == null)
        commentStr = "";
    else
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_comment");
    
    if(wo != null)
    {
        long companyId = -1;
        if( wo instanceof Task )
        {
            doneUrl = done.getPageURL()+ 
               "&" + WebAppConstants.TASK_ACTION + 
               "=" + WebAppConstants.TASK_ACTION_SAVECOMMENT+
               //GBS-2913 enable tabbed browsing,add taskID and taskState
               "&" + WebAppConstants.TASK_ID+
               "=" + ((Task)wo).getId() +
               "&" + WebAppConstants.TASK_STATE +
               "=" + ((Task)wo).getState();
            
            cancelUrl = cancel.getPageURL()+
            		"&" + WebAppConstants.TASK_ID+
            		"=" + ((Task)wo).getId()+
            		//GBS-2913 enable tabbed browsing,add taskID and taskState
            		"&" + WebAppConstants.TASK_STATE+
            		"=" + ((Task)wo).getState()+
            		"&toTask=ture";
            
          //GBS-2913 enable tabbed browsing,add taskID and taskState
            attachStr = "&" + WebAppConstants.TASK_ID+
		                    "=" + ((Task)wo).getId() +
		                    "&" + WebAppConstants.TASK_STATE +
		                    "=" + ((Task)wo).getState() +
		                    "&toTask=ture";
          if(comment != null){
        	  attachStr += "&commentId="+comment.getId();
          }
          
            if (commentStr.equals(""))
                msgDlgHeading = bundle.getString("msg_my_activities_add");
            else
                msgDlgHeading = bundle.getString("msg_my_activities_edit");        	
            helpFile = bundle.getString("help_activity_comment_add");
            
            jobName =  ((Task)wo).getJobName();
            companyId = ((Task)wo).getCompanyId();
        }
        else if( wo instanceof Job )
        {
            doneUrl = jobCommentsDone.getPageURL() + 
                "&" + WebAppConstants.TASK_ACTION + 
                "=" + WebAppConstants.TASK_ACTION_SAVECOMMENT +
                //GBS-2913 enable tabbed browsing,add jobID
                "&"+WebAppConstants.JOB_ID +
                "="+((Job)wo).getJobId() +
                "&toJob=ture";
            
            cancelUrl = jobCommentsCancel.getPageURL() +
                    //GBS-2913 enable tabbed browsing,add jobID
                    "&" + WebAppConstants.JOB_ID +
                    "=" + ((Job)wo).getJobId() +
                    "&toJob=ture";
            
          //GBS-2913 enable tabbed browsing,add jobID
            attachStr =  "&" + WebAppConstants.JOB_ID +
		                     "=" + ((Job)wo).getJobId() +
		                     "&toJob=ture";
            if(comment != null){
          	  attachStr += "&commentId="+comment.getId();
            }
            
            if (commentStr.equals(""))
                msgDlgHeading = bundle.getString("msg_my_jobs_add");
            else
                msgDlgHeading = bundle.getString("msg_my_jobs_edit");        	
            helpFile = bundle.getString("help_job_comment_add");
            
            jobName = ((Job)wo).getJobName();
            companyId = ((Job)wo).getCompanyId();
        }
        else if( wo instanceof Workflow )
        {
            doneUrl = done.getPageURL() + 
                "&" + WebAppConstants.TASK_ACTION + 
                "=" + WebAppConstants.TASK_ACTION_SAVECOMMENT;
            
            cancelUrl = cancel.getPageURL();
            if (commentStr.equals(""))
                msgDlgHeading = bundle.getString("msg_my_activities_add");
            else
                msgDlgHeading = bundle.getString("msg_my_activities_edit");        	
            helpFile = bundle.getString("help_activity_comment_add");

            jobName = ((Workflow)wo).getJob().getJobName();
            companyId = ((Workflow)wo).getCompanyId();
        }

        CompanyThreadLocal.getInstance().setIdValue(String.valueOf(companyId));
    }

    String attachUrl = attach.getPageURL();
    if (sessionMgr.getAttribute("comment") != null)
        attachUrl = attachExisting.getPageURL();

    StringBuffer attachBuffer = new StringBuffer();
    attachBuffer.append(attachUrl);
    attachBuffer.append(attachStr);
    
    String textAreaName = "taskComment";
    
    if (comment != null) {
	    WorkObject commentWo = (WorkObject)comment.getWorkObject();
	    if(commentWo != null)
	    {
	        if (commentWo instanceof Task )
	        {
	            if (commentStr.equals(""))
	                msgDlgHeading = bundle.getString("msg_my_activities_add");
	            else
	                msgDlgHeading = bundle.getString("msg_my_activities_edit");        	
	        }       
	        else if(commentWo instanceof Job )
	        {
	            if (commentStr.equals(""))
	                msgDlgHeading = bundle.getString("msg_my_jobs_add");
	            else
	                msgDlgHeading = bundle.getString("msg_my_jobs_edit");        	
	        }
	        else if(commentWo instanceof Workflow )
	        {
	            if (commentStr.equals(""))
	                msgDlgHeading = bundle.getString("msg_my_activities_add");
	            else
	                msgDlgHeading = bundle.getString("msg_my_activities_edit");        	
	        }
	    }    
    }
    
    //GBS-1012: Added for edit job comment from Task/Activity
    saveCommStatus = (String) request.getParameter("saveCommStatus");
    if(null!=saveCommStatus&&saveCommStatus.length()>0)
    {
    	isTaskComment = false;
    	if(saveCommStatus.equals(CommentConstants.SAVE_COMMENT_STATUS_JT))
    	{
    		msgDlgHeading = bundle.getString("msg_my_jobs_add");
    	}
    }
     
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var w_editor = null;
var guideNode = "myActivitiesComments";
var helpFile = "<%=helpFile%>";

function validateForm() {
    if (document.layers)
    {
        theForm = document.contentLayer.document.dialogForm;
    }
    else
    {
        theForm = document.all.dialogForm;
    }

	if (!isEmptyString(theForm.<%=textAreaName%>.value))
	{
		if (!isNotLongerThan(theForm.<%=textAreaName%>.value, 3999))
		{
			alert("<%= bundle.getString("jsmsg_comment_must_be_less") %>");
			theForm.<%=textAreaName%>.focus();
            return false;
		}
		else
		{
           return true;
		}
	}
	else
	{
		alert("<%=bundle.getString("jsmsg_comment_must_enter")%>");
		theForm.<%=textAreaName%>.focus();
	}
    return false;
}
function submitForm(p_action)
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.dialogForm;
    }
    else
    {
        theForm = document.all.dialogForm;
    }
	if (p_action == "done")
	{
       dialogForm.action = "<%=doneUrl%>";
	}
	if (p_action == "attach")
	{
       dialogForm.action = "<%=attachBuffer.toString()%>";
	}
    if (validateForm(theForm))
    {
        dialogForm.submit();
    }
}

// Function to limit text entered into textarea to be less than 4000
// since it's saved into a varchar(4000) field 
function limitText(textArea, pastedLength) 
{
    if (textArea.value.length + pastedLength> 3999) 
    {
        alert("<%= bundle.getString("jsmsg_comment_must_be_less") %>");
        return false;
    }
    return true;
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides();dialogForm.<%=textAreaName%>.focus()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=lbJobName%>: <%=jobName%> 
<%  if(wo instanceof Task && isTaskComment)
{ %>
  <br><br>
<%
  out.println(bundle.getString("lb_target_language"));%>: 
<% 
  out.println(((Task)wo).getTargetLocale().getDisplayName());
}%>
</SPAN>

<p>
<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>

<P>  

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%= msgDlgHeading %>
</TD>
</TR>
</TABLE>
<P>

<FORM NAME="dialogForm" METHOD="post">
<INPUT TYPE="HIDDEN" NAME="saveCommStatus" VALUE="<%=saveCommStatus%>"></INPUT>

<SPAN CLASS="standardText">
<textarea name="<%=textAreaName%>" cols="55" rows="10" 
onPaste="return limitText(this, window.clipboardData.getData('Text').length);" 
onKeyPress="return limitText(this,0);" 
wrap="virtual" 
CLASS="standardText"><%=commentStr%></textarea>
<P>
<INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
    ONCLICK="location.replace('<%=cancelUrl%>')">   
<INPUT TYPE="BUTTON" NAME="<%=lbAttach%>" VALUE="<%=lbAttach%>" onclick="submitForm('attach')"> 
<INPUT TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>" 
    ONCLICK="submitForm('done');">  
</FORM>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
