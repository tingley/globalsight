<%@ page 
     	contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="com.globalsight.everest.foundation.User,com.globalsight.everest.permission.Permission,
				 com.globalsight.everest.permission.PermissionSet,
				 com.globalsight.everest.servlet.util.SessionManager,
				 com.globalsight.everest.webapp.WebAppConstants,
				 com.globalsight.everest.webapp.pagehandler.PageHandler,
				 com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
                 com.globalsight.everest.workflow.EventNotificationHelper,
                 java.util.Locale,
                 java.util.ResourceBundle"
		session="true" 
%>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="calendar" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="contact" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="notification" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="downloadOption" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<% 
    PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    boolean calPerm = perms.getPermissionFor(Permission.USER_CAL_EDIT_YOURS) ||
                            perms.getPermissionFor(Permission.USER_CAL_EDIT);
    boolean pwdPerm = perms.getPermissionFor(Permission.CHANGE_OWN_PASSWORD);
	ResourceBundle bundle = PageHandler.getBundle(session);		
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
	String title= bundle.getString("lb_edit") + " " +
                    bundle.getString("lb_my_account") + " - " +
                    bundle.getString("lb_basic_information");

	//Urls of the links on this page
	String saveUrl = save.getPageURL() + "&" + WebAppConstants.TASK_ACTION + 
		"=" + WebAppConstants.TASK_ACTION_MODIFY_ACCOUNT;
	String optionsUrl = options.getPageURL();			
	String contactUrl = contact.getPageURL();			
	String calendarUrl = calendar.getPageURL() + "&action=" +
        CalendarConstants.EDIT_ACTION;			
    String notificationUrl = notification.getPageURL();
	String downloadOptionUrl = downloadOption.getPageURL();
	//labels
	String lbUserName = bundle.getString("lb_user_name"); 
	String lbFirstName = bundle.getString("lb_first_name"); 
	String lbLastName = bundle.getString("lb_last_name");  
	String lbPassword = bundle.getString("lb_password");  
	String lbRepeatPassword = bundle.getString("lb_password_repeat"); 
	String lbUiLanguage = bundle.getString("lb_ui_language"); 
	String lbCalendar = bundle.getString("lb_calendar"); 
	String lbOptions = bundle.getString("lb_account_options");
    String lbNotification = bundle.getString("lb_notification_options");
    String lbTitle = bundle.getString("lb_title");
    String lbCompanyName = bundle.getString("lb_company_name");

	String lbSave = bundle.getString("lb_save");
	String lbContact = bundle.getString("lb_contact_info");
	String lbDownloadOptions = bundle.getString("lb_account_download_options");
	
    // determines whether the system-wide notification is enabled
    boolean systemNotificationEnabled = 
        EventNotificationHelper.systemNotificationEnabled();
	//Retrieve User info
	User user = (User)sessionMgr.getAttribute("myAccountUser");
	String username = user.getUserName();
	String firstName = user.getFirstName();
    String lastName = user.getLastName();
	String password = user.getPassword();
    String userTitle = user.getTitle();
    String companyName = user.getCompanyName();
    if (userTitle == null||"null".equals(userTitle)) userTitle = "";
    if (companyName == null) companyName = "";
    String[] companies = (String[])sessionMgr.getAttribute("companyNames");
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String needStrongPassword = (String) sessionMgr.getAttribute("needStrongPassword");
%>
<HTML>
<!-- This JSP is: /envoy/tasks/accountInfo.jsp --> 
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.11.3.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/gscommon.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_account_information_my") %>";
var guideNode = "myAccount";
var helpFile = "<%=bundle.getString("help_my_account")%>";
</SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
{
var today = new Date();
var expires = new Date(today.getTime() + (365 * 86400000));
var company;


}

function updateNeedWarning()
{
    needWarning = true;
}

function submitForm(button) {
	if (document.layers) {
		theForm = document.layers.contentLayer.document.accountForm;
	}
	else {
		theForm = document.all.accountForm;
	}
    if (button == "save")
    {
        theForm.action = "<%=saveUrl%>";
    } 
    else if (button == "contact")
    {
        theForm.action = "<%=contactUrl%>";
    } 
    else if (button == "options")
    {
        theForm.action = "<%=optionsUrl%>";
    } 
    else if (button == "calendar")
    {
        theForm.action = "<%=calendarUrl%>";
    } 
    else if (button == "notification")
    {
        theForm.action = "<%=notificationUrl%>&action=notification";
    } 
    else if (button == 'downloadOption')
    {
    	theForm.action = "<%=downloadOptionUrl%>";
    }
    if (confirmForm(theForm))
    {
        if (company)
            accountForm.company.value="true";
        else
            accountForm.company.value="false";
        if (button == "save")
            needWarning = false;
        theForm.submit();
    }
}


function confirmForm(formSent) {
    if (formSent.password) {
        var thePassword = formSent.password.value;
        var theRepeat = formSent.passwordConfirm.value;
        if (pwdChanged) {
            thePassword = stripBlanks(thePassword);
            if (thePassword != "") {
                if ("1" == "<%=needStrongPassword%>" && !passCheck) {
                    alert("<%=bundle.getString("jsmsg_account_weak_password")%>");
                    return false;
                }
                theRepeat = stripBlanks(theRepeat);
            }
        } else {
            formSent.password.value = "";
            formSent.passwordConfirm.value = "";
        }
        // Make sure the repeated password matches the first
        if (theRepeat != thePassword) {
            alert("<%= bundle.getString("jsmsg_users_repeat_password") %>");
            return false;
        }
    }

	var theFirst = accountForm.firstName.value;
	theFirst = stripBlanks(theFirst);
	// Make sure the first Name is given
	if (isEmptyString(theFirst)) {
		alert("<%= bundle.getString("jsmsg_users_first_name") %>");
		accountForm.firstName.value = "";
		accountForm.firstName.focus();
		return false;
	}
	
	var theLast = accountForm.lastName.value;
	theLast = stripBlanks(theLast);
	// Make sure the last Name is given
	if (isEmptyString(theLast)) {
		alert("<%= bundle.getString("jsmsg_users_last_name") %>");
		accountForm.lastName.value = "";
		accountForm.lastName.focus();
		return false;
	}
	
	return true;
}

var passCheck = false;
var pwdChanged = false;

$(document).ready(function(){
    $("#password1").keyup(function() {
        passCheck = passwordChecking($(this).val());
		pwdChanged = true;
    });
});
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
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

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%= bundle.getString("helper_text_acc_info") %>
</TD>
</TR>
</TABLE>
<P>
<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
  <form name="accountForm" method="post">
  <input type="hidden" name="company">
  <TR>
	<TD NOWRAP style="width:150px;"><%= lbUserName %>:</TD>
	<TD><%=username%></TD>
  </TR>
  <TR>
	<TD NOWRAP style="width:150px;"><%= lbFirstName %><SPAN CLASS="asterisk">*</SPAN>:</TD>
	<TD><input type="text" name="firstName" maxlength="40" value="<%= firstName %>" CLASS="standardText width100" onkeydown="updateNeedWarning()"></TD>
  </TR>
  <TR>
	<TD style="width:150px;"><%= lbLastName %><SPAN CLASS="asterisk">*</SPAN>:</TD>
	<TD><input type="text" name="lastName" maxlength="40" value="<%= lastName %>" CLASS="standardText width100" onkeydown="updateNeedWarning()"></TD>
  </TR>
  <% if (pwdPerm) {%>
  <TR>
	<TD style="width:150px;"><%= lbPassword %><SPAN CLASS="asterisk">*</SPAN>:</TD>
	<TD>
        <% if ("1".equals(needStrongPassword)) { %>
        <div class="vali_pass">
            <input type="password" name="password" id="password1" maxlength="40" value="" class="standardText width100" onkeydown="updateNeedWarning()">
            <div class="vali_pass_progress" style="display:inline-flex;">
                <span class="vali_pass_inner_progress"></span><span class="standardText" id="pvText"></span>
            </div>
        </div>
        <% } else { %>
            <input type="password" name="password" id="password1" maxlength="40" value="" class="standardText width100" onkeydown="updateNeedWarning()">
        <% } %>
    </TD>
  </TR>
  <TR>
	<TD  style="width:150px;"><%= lbRepeatPassword %><SPAN CLASS="asterisk">*</SPAN>:</TD>
	<TD>
        <div class="vali_pass">
            <input type="password" name="passwordConfirm" id="passwordConfirm" maxlength="40" value="" class="standardText width100" onkeydown="updateNeedWarning()">
        </div>
    </TD>
  </TR>
  <% }%>
  <TR>
	<TD style="width:150px;"><%= lbTitle %>:</TD>
	<TD><input type="text" name="title" value="<%= userTitle %>" CLASS="standardText width100" onkeydown="updateNeedWarning()"></TD>
  </TR>
  <TR>
    <TD VALIGN="TOP"><%= lbCompanyName %>:</TD><TD>
<INPUT TYPE='hidden' NAME='companyName' VALUE='<%=companyName%>'>
<INPUT TYPE='hidden' NAME='company' VALUE='true'>
<B><%=companyName%></B>
      </TD>
  </TR>
  <!-- en_US is always the UI language starting in 4.1 -->
  <INPUT TYPE="HIDDEN" NAME="uiLocale" VALUE="en_US">
  <TR>
	<TD COLSPAN="2">&nbsp;</TD>
  </TR>
  <TR>
	<TD COLSPAN="2" >
	  <INPUT TYPE="BUTTON" VALUE="<%=lbContact%>..." 
	  onclick="submitForm('contact')"> 
      <% if (b_calendaring) { %>
          <% if (calPerm) { %>
            <INPUT TYPE="BUTTON" VALUE="<%=lbCalendar%>..." 
            onclick="submitForm('calendar')">
          <% } %>
      <% } %>
      <% if (systemNotificationEnabled) { %>
          <INPUT TYPE="BUTTON" VALUE="<%=lbNotification%>..." 
          onclick="submitForm('notification')">
      <% } %>
          <INPUT TYPE="BUTTON" VALUE="<%=lbOptions%>..." 
          onclick="submitForm('options')"> 
      <input type="BUTTON" value="<%=lbDownloadOptions%>..." onclick="submitForm('downloadOption')">
	  <INPUT TYPE="BUTTON" VALUE="<%=lbSave%>" onclick="submitForm('save')">  
	</TD>
  </TR>
  </FORM>
</TABLE>
</DIV>
</BODY>
</HTML>
