<%@ page 
     	contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.taskmanager.Task,
                com.globalsight.everest.comment.Comment,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
                com.globalsight.everest.workflowmanager.Workflow,
                com.globalsight.util.resourcebundle.ResourceBundleConstants,
                com.globalsight.util.resourcebundle.SystemResourceBundle,
                java.util.ResourceBundle" 
		session="true" 
%>
				 
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%
	Task task = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
    // links
    String doneUrl = done.getPageURL() + "&" + WebAppConstants.TASK_ACTION + 
		"=" + WebAppConstants.TASK_ACTION_REJECT
		//GBS-2913
		+"&" + WebAppConstants.TASK_ID + "=" + task.getId()
		+"&" + WebAppConstants.TASK_STATE + "=" + task.getState();
	String cancelUrl = cancel.getPageURL()
			//GBS-2913
			+"&" + WebAppConstants.TASK_ID + "=" + task.getId()
			+"&" + WebAppConstants.TASK_STATE + "=" + task.getState();
	String textAreaName = WebAppConstants.TASK_COMMENT;
	
	//locale bundle labels
	ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_my_activities_reject");
	String msgDlgHeading = bundle.getString("msg_my_activities_reject");
    String lbCancel = bundle.getString("lb_cancel");
    String lbDone = bundle.getString("lb_done");
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
var helpFile = "<%=bundle.getString("help_reject_activity")%>";

function validateForm() {
    theForm = document.dialogForm;

	if (!isEmptyString(theForm.<%=textAreaName%>.value))
	{
		if (!isNotLongerThan(theForm.<%=textAreaName%>.value, 3999))
		{
			alert("<%= bundle.getString("jsmsg_comment_must_be_less") %>");
			theForm.<%=textAreaName%>.focus();
		}
		else
		{
			theForm.submit();
		}
	}
	else
	{
		alert("<%=bundle.getString("jsmsg_my_activities_reject")%>");
		theForm.<%=textAreaName%>.focus();
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

<FORM ACTION="<%= doneUrl %>" NAME="dialogForm" METHOD="post">

<SPAN CLASS="standardText">
<textarea name="<%=textAreaName%>" cols="55" rows="10" 
onPaste="return limitText(this, window.clipboardData.getData('Text').length);" 
onKeyPress="return limitText(this,0);" 
wrap="virtual" 
CLASS="standardText"></textarea>
</FORM>
<P>
<INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
    ONCLICK="location.replace('<%=cancelUrl%>')">  
<INPUT TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>" 
    ONCLICK="validateForm()">  

</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
