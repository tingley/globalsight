<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
		 com.globalsight.everest.webapp.pagehandler.PageHandler, 
		 com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.localemgr.CodeSet,
		 com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
         com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowStatePostConstants,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.servlet.EnvoyServletException,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.SortUtil,
         com.globalsight.everest.foundation.LeverageLocales,
         com.globalsight.everest.util.comparator.UserComparator,
         com.globalsight.everest.workflowmanager.WorkflowStatePosts,
         java.util.Collections,
         java.util.Iterator,                
         java.util.List,                
         java.util.Locale,
         java.util.HashMap,
         java.util.Vector,
         java.util.Enumeration,
         java.util.ResourceBundle"
	session="true"%>

<jsp:useBean id="cancel"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<jsp:useBean id="save"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
			SessionManager sessionMgr = (SessionManager) session
					.getAttribute(WebAppConstants.SESSION_MANAGER);
			List wfStatePostProfile = (List) request
					.getAttribute("allWfStatePost");
			String actionType = (String) request
					.getAttribute(WorkflowStatePostConstants.ACTION);
			Locale uiLocale = (Locale) session
					.getAttribute(WebAppConstants.UILOCALE);
			//names
			String nameField = WorkflowStatePostConstants.NAME_FIELD;
			String descriptionField = WorkflowStatePostConstants.DESCRIPTION_FIELD;
			String listenerURLField = WorkflowStatePostConstants.LISTENERURL_FIELD;
			String secretKeyField = WorkflowStatePostConstants.SECRETKEY_FIELD;
			String timeoutField = WorkflowStatePostConstants.TIMEOUT_FIELD;
			String retryTimeField = WorkflowStatePostConstants.RETRY_TIME_FIELD;
			String notifyEmailField = WorkflowStatePostConstants.NOTIFY_EMAIL_FIELD;

			//labels
			String labelName = bundle.getString("lb_name");
			String labelDescription = bundle.getString("lb_description");
			String labelListenerURL = bundle.getString("lb_listener_url");
			String labelSecretKey = bundle.getString("lb_secret_key");
			String labelTimeoutPeriod = bundle.getString("lb_timeout_period");
			String labelRetryTime = bundle.getString("lb_retry_time");
			String labelNotifyEmail = bundle.getString("lb_notification_email");

			// get the newly created or existing workflow state post info
			WorkflowStatePosts wfsp = (WorkflowStatePosts) request
					.getAttribute(WorkflowStatePostConstants.WF_STATE_POST_INFO);

			String jsmsg = "";
			long wfStatePostInfoId = -1;
			boolean isNew = (wfsp == null);
			if (!isNew) // edit
			{
				wfStatePostInfoId = (Long) request
						.getAttribute(WorkflowStatePostConstants.WORKFLOW_STATE_POST_ID);
			}

			String wfStatePostName = "";
			String wfStatePostDescription = "";
			String wfStatePostlistenerURL = "";
			String wfStatePostSecretKey = "";
			String wfStatePostTimeoutPeriod = String.valueOf(60);
			String wfStatePostRetryTime = String.valueOf(3);
			String wfStatePostNotifyEmail = "";
			if (wfsp != null) {
				// values to be populated in the UI fields
				wfStatePostName = wfsp.getName();
				wfStatePostDescription = wfsp.getDescription();
				wfStatePostlistenerURL = wfsp.getListenerURL();
				wfStatePostSecretKey = wfsp.getSecretKey();
				wfStatePostTimeoutPeriod = String.valueOf(wfsp
						.getTimeoutPeriod());
				wfStatePostRetryTime = String.valueOf(wfsp.getRetryNumber());
				wfStatePostNotifyEmail = wfsp.getNotifyEmail();
			}

			// links for the save and cancel buttons
			String cancelURL = cancel.getPageURL() + "&"
					+ WorkflowStatePostConstants.ACTION + "="
					+ WorkflowStatePostConstants.CANCEL_ACTION;
			String saveURL;
			if (actionType.equals("edit")) {
				saveURL = save.getPageURL() + "&"
						+ WorkflowStatePostConstants.ACTION + "="
						+ WorkflowStatePostConstants.MODIFY_ACTION
						+ "&wfStatePostId=" + wfStatePostInfoId;;
			} else {
				saveURL = save.getPageURL() + "&"
						+ WorkflowStatePostConstants.ACTION + "="
						+ WorkflowStatePostConstants.SAVE_ACTION;
			}
			// Titles                                 
			String newTitle = bundle.getString("msg_wf_state_post_new_title1");
			String modifyTitle = bundle
					.getString("msg_wf_state_post_edit_title1");
			String wizardTitle = wfsp == null ? newTitle : modifyTitle;
			String lbCancel = bundle.getString("lb_cancel");
			String lbSave = bundle.getString("lb_save");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%=wizardTitle%></TITLE>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT language="JavaScript">
var needWarning = false;
var objectName = "<%=bundle.getString("lb_workflow_state_post_profile")%>";
var guideNode = "workflow state post profile";
var helpFile = "<%=bundle.getString("help_workflow_state_post_information")%>";
function submitForm(formAction)
{
	if(formAction == "cancel")
	{
		wfStatePostForm.action = "<%=cancelURL%>";
		$("#wfStatePostForm").submit();
	}
	else if(formAction == "save")
	{
		if(confirmForm() && confirmTime())
		{
			wfStatePostForm.action = "<%=saveURL%>";
			$("#wfStatePostForm").submit();
		}
	}
}

function confirmForm()
{
	wfStatePostForm.<%=nameField%>.value = ATrim(wfStatePostForm.<%=nameField%>.value);  
	   if (isEmptyString(wfStatePostForm.<%=nameField%>.value)) {
	        alert("<%=bundle.getString("jsmsg_wf_state_post_name")%>");
	        wfStatePostForm.<%=nameField%>.value = "";
	        wfStatePostForm.<%=nameField%>.focus();
	        return false;
	    }
	   var lowerName = wfStatePostForm.<%=nameField%>.value.toLowerCase();
	   <%if (wfStatePostProfile != null) {
				for (int i = 0; i < wfStatePostProfile.size(); i++) {
					WorkflowStatePosts wfStatePost = (WorkflowStatePosts) wfStatePostProfile
							.get(i);%>
	           if ("<%=wfStatePost.getName().toLowerCase()%>" == lowerName )
	           {
	               alert(wfStatePostForm.<%=nameField%>.value + " is already existed. Please input another one.");
	               return false;
	           }
	  <%}
			}%>
	   if (!isNotLongerThan(wfStatePostForm.<%=descriptionField%>.value, 256)) {
			alert("<%=bundle.getString("jsmsg_description")%>");
			wfStatePostForm.<%=descriptionField%>.focus();
			return false;
		}
	   if (hasSpecialChars(wfStatePostForm.<%=nameField%>.value))
	    {
	        alert("<%=labelName%>" + "<%=bundle.getString("msg_invalid_entry")%>");
	        return false;
	    }
	   wfStatePostForm.<%=listenerURLField%>.value = ATrim(wfStatePostForm.<%=listenerURLField%>.value);
	    if (isEmptyString(wfStatePostForm.<%=listenerURLField%>.value)) {
	        alert("<%=bundle.getString("jsmsg_wf_state_post_listener_url")%>");
	        wfStatePostForm.<%=listenerURLField%>.value = "";
	        wfStatePostForm.<%=listenerURLField%>.focus();
	        return false;
	    }
	    
	    var notifyEmail = ATrim(wfStatePostForm.<%=notifyEmailField%>.value);  
	    if (!isEmptyString(notifyEmail) && !validEmail(notifyEmail)) 
	    {
	    	alert("<%=bundle.getString("jsmsg_email_invalid")%>");
	        return false;
	    }
	    return true;
}

function confirmTime()
{
	var sessionTime =  wfStatePostForm.<%=timeoutField%>.value;
		if(sessionTime != '')
		{
			if(!isNumeric(sessionTime))
			{
			alert("<%=bundle.getString("msg_listenertime_retrynumber_time")%>");
					return false;
			} 
			else 
			{
				sessionTime = parseInt(sessionTime)
				if (sessionTime > 600 || sessionTime < 10) {
					alert("<%=bundle.getString("msg_timeout_range")%>");
					return false;
				}
			}
		}else{
			alert("<%=bundle.getString("msg_listenertime_retrynumber_time")%>");
			return false;
		}
	
		var retryNumber = wfStatePostForm.<%=retryTimeField%>.value;
		if(retryNumber != '')
		{
			if(!isNumeric(retryNumber))
			{
			alert("<%=bundle.getString("msg_listenertime_retrynumber_time")%>");
			return false;
			}else
			{
				retryNumber = parseInt(retryNumber)
				if (retryNumber > 10 || retryNumber < 0) {
					alert("<%=bundle.getString("msg_number_of_retries_range")%>");
					return false;
				}
			}
		}else{
			alert("<%=bundle.getString("msg_listenertime_retrynumber_time")%>");
			return false;
		}
		return true;
	}

	function isNumeric(str) {
		return /^(-|\+)?\d+(\.\d+)?$/.test(str);
	}
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0" ONLOAD="loadGuides()">
	<%@ include file="/envoy/common/header.jspIncl"%>
	<%@ include file="/envoy/common/navigation.jspIncl"%>
	<%@ include file="/envoy/wizards/guides.jspIncl"%>
	<DIV ID="contentLayer"
		STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">


		<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
			<TR>
				<TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%=wizardTitle%></TD>
			</TR>
			<TR>
				<TD VALIGN="TOP">
					<!-- left table -->
					<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0"
						CLASS="standardText">
						<form name="wfStatePostForm" method="post" id="wfStatePostForm">
							<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="wfStatePostForm">
							<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="wfStatePostForm">
							<TR>
								<TD><%=labelName%><SPAN CLASS="asterisk">*</SPAN>:</TD>
								<TD><INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="40"
									NAME="<%=nameField%>" CLASS="standardText"
									<%if (wfStatePostName != null)
            						{%>
									VALUE="<%=wfStatePostName%>" <%}%>></INPUT></TD>
							</TR>
							<TR>
								<TD><%=labelDescription%>:</TD>
								<TD>
								<TEXTAREA rows="6" cols="50" name="<%=descriptionField%>" 
								class="standardText"><%=wfStatePostDescription%></TEXTAREA>
								</TD>
							</TR>
							<TR>
								<TD><%=labelListenerURL%><SPAN CLASS="asterisk">*</SPAN>:</TD>
								<TD><INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="100"
									NAME="<%=listenerURLField%>" CLASS="standardText"
									<%if (wfStatePostlistenerURL != null)
            {%>
									VALUE="<%=wfStatePostlistenerURL%>" <%}%>></INPUT>
								</TD>
							</TR>
							<TR>
								<TD><%=labelSecretKey%>:</TD>
								<TD><INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="100"
									NAME="<%=secretKeyField%>" CLASS="standardText"
									<%if (wfStatePostSecretKey != null)
            {%>
									VALUE="<%=wfStatePostSecretKey%>" <%}%>></INPUT>
								</TD>
							</TR>
							<TR>
								<TD><%=labelTimeoutPeriod%>:</TD>
								<TD><INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="3"
									NAME="<%=timeoutField%>" CLASS="standardText"
									<%if (wfStatePostTimeoutPeriod != null)
            {%>
									VALUE="<%=wfStatePostTimeoutPeriod%>" <%}%>></INPUT>
								</TD>
							</TR>
							<TR>
								<TD><%=labelRetryTime%>:</TD>
								<TD><INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="3"
									NAME="<%=retryTimeField%>" CLASS="standardText"
									<%if (wfStatePostRetryTime != null)
            {%>
									VALUE="<%=wfStatePostRetryTime%>" <%}%>></INPUT>
								</TD>
							</TR>
							<TR>
								<TD><%=labelNotifyEmail%>:</TD>
								<TD><INPUT TYPE="TEXT" SIZE="40" MAXLENGTH="100"
									NAME="<%=notifyEmailField%>" CLASS="standardText"
									<%if (wfStatePostNotifyEmail != null)
            {%>
									VALUE="<%=wfStatePostNotifyEmail%>" <%}%>></INPUT>
								</TD>
							</TR>
							<TR>
							</TR>
					</TABLE> <!-- end left table -->
				</TD>
				<TD WIDTH="50">&nbsp;</TD>
				<TD VALIGN="TOP">
					</form>
				</TD>
			</TR>
			</TD>
			</TR>


			<TR>
				<TD COLSPAN="3">&nbsp;</TD>
			</TR>
			<TR>
				<TD CLASS="HREFBold" COLSPAN="2"><INPUT TYPE="BUTTON"
					NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
					ONCLICK="submitForm('cancel')">&nbsp;&nbsp;&nbsp;&nbsp; <INPUT
					TYPE="BUTTON" NAME="<%=lbSave%>" VALUE="<%=lbSave%>"
					ONCLICK="submitForm('save')"></TD>
			</TR>
		</TABLE>
	</DIV>
</BODY>
</HTML>
