<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ page
        contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
        import="java.util.*,
                com.globalsight.everest.foundation.User,
                com.globalsight.calendar.CalendarManagerLocal,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,
                com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
                 com.globalsight.everest.permission.Permission,
         		com.globalsight.everest.permission.PermissionSet, 
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>

<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
	ResourceBundle bundle = PageHandler.getBundle(session);

    String doneURL = save.getPageURL() + "&" + WebAppConstants.USER_ACTION +
              "=" + WebAppConstants.USER_ACTION_MODIFY_USER_CONTACT;
	String cancelURL = cancel.getPageURL() + "&action=cancel";
	String title= bundle.getString("lb_edit") + " " +
                    bundle.getString("lb_my_account") + " - " +
                    bundle.getString("lb_contact_information");
	 PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
	//Labels
	String lbAddress = bundle.getString("lb_address");
	String lbHomePhone = bundle.getString("lb_home_phone");
	String lbWorkPhone = bundle.getString("lb_work_phone");
	String lbCellPhone = bundle.getString("lb_cell_phone");
	String lbFax = bundle.getString("lb_fax");
	String lbEmail = bundle.getString("lb_email");
	String lbCcEmail = bundle.getString("lb_cc_email");
	String lbBccEmail = bundle.getString("lb_bcc_email");
        String lbLocaleEmailDates = bundle.getString("lb_locale_email_dates");
	String lbChoose = bundle.getString("lb_choose");
	String lbDone = bundle.getString("lb_done");
	String lbCancel = bundle.getString("lb_cancel");

	//Messages
	String jsmsgAddress = bundle.getString("jsmsg_users_address");
	String jsmsgEmail = bundle.getString("jsmsg_users_email");
        String jsmsgUiLanguage = bundle.getString("jsmsg_users_ui_language");

	//available UI languages
	String[] uiLocales = UserHandlerHelper.getUILocales();

	//User UI locale 
	Locale userUiLocale = (Locale) session
			.getAttribute(WebAppConstants.UILOCALE);

	User user = (User) sessionMgr.getAttribute("myAccountUser");
	String homePhone = user.getHomePhoneNumber();
	if (homePhone == null||"null".equals(homePhone)) homePhone = "";
	String workPhone = user.getOfficePhoneNumber();
	if (workPhone == null||"null".equals(workPhone)) workPhone = "";
	String cellPhone = user.getCellPhoneNumber();
	if (cellPhone == null||"null".equals(cellPhone)) cellPhone = "";
	String fax = user.getFaxPhoneNumber();
	if (fax == null||"null".equals(fax)) fax = "";
	String address = user.getAddress();
	if (address == null||"null".equals(address)) address = "";
	String email = user.getEmail();
	if (email == null||"null".equals(email)) email = "";
	String ccEmail = user.getCCEmail();
	if (ccEmail == null||"null".equals(ccEmail)) ccEmail = "";
	String bccEmail = user.getBCCEmail();
	if (bccEmail == null||"null".equals(bccEmail)) bccEmail = "";
	String password = user.getPassword();
	String uiLanguage = user.getDefaultUILocale();
	User me=(User) sessionMgr
            .getAttribute(WebAppConstants.USER);
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script SRC="/globalsight/includes/filter/StringBuffer.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = true;
var objectName = "<%=bundle.getString("lb_user")%>";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_user_contactinfo")%>";

function submitForm(btnName) {
    if (document.layers) {
        theForm = document.layers.contentLayer.document.userForm;
    }
    else
    {
        theForm = document.all.userForm;
    }
    if (btnName == "done")
    {
        theForm.action = "<%=doneURL%>";
        if (confirmForm(theForm)) theForm.submit();
    }
}

function confirmForm(formSent) {
	var theAddress = formSent.address.value;
        theAddress = stripBlanks(theAddress);
	var theEmail = formSent.email.value;
        theEmail = stripBlanks(theEmail);

        if (isEmptyString(theEmail)) {
          alert(" <%=jsmsgEmail%>");
          formSent.email.value = "";
          formSent.email.focus();
          return false;
        } else 
        { 
        	if ((theEmail !="@") && (!isValidEmail(theEmail,"<%=bundle.getString("jsmsg_email_invalid")%>"))) {
                formSent.email.focus();
                return false;
            }
        }
	if (formSent.homePhone)
	{
		var theHomephone = formSent.homePhone.value;
		theHomephone = stripBlanks(theHomephone);
		
		if (!isValidPhone(theHomephone,"<%=bundle.getString("jsmsg_homephone_invalid")%>")) {
            formSent.homePhone.focus();
            return false;
		}
	}
	if (formSent.workPhone)
	{
		var theWorkPhone = formSent.workPhone.value;
		theWorkPhone = stripBlanks(theWorkPhone);
		
		if (!isValidPhone(theWorkPhone,"<%=bundle.getString("jsmsg_workphone_invalid")%>")) {
            formSent.workPhone.focus();
            return false;
		}
	}
	if (formSent.cellPhone)
	{
		var theCellPhone = formSent.cellPhone.value;
		theCellPhone = stripBlanks(theCellPhone);
		
		if (!isValidPhone(theCellPhone,"<%=bundle.getString("jsmsg_cellphone_invalid")%>")) {
            formSent.cellPhone.focus();
            return false;
		}
	}
	if (formSent.fax)
	{
		var thefax = formSent.fax.value;
		thefax = stripBlanks(thefax);
		
		if (!isValidPhone(thefax,"<%=bundle.getString("jsmsg_fax_invalid")%>")) {
            formSent.fax.focus();
            return false;
		}
	}
    
    if(formSent.ccEmail)
    {	
        var ccEmail = formSent.ccEmail.value;

        if(!isEmptyString(ccEmail))
        {
          if(!isValidEmail(ccEmail,"<%=bundle.getString("jsmsg_ccemail_invalid")%>"))
	      {
	    	formSent.ccEmail.focus();
	        return false;
	      }
	  	}
  	}
    if(formSent.bccEmail)
    {
       var bccEmail = formSent.bccEmail.value;

       if(!isEmptyString(bccEmail))
       {
         if(!isValidEmail(bccEmail,"<%=bundle.getString("jsmsg_bccemail_invalid")%>"))
	     { 
	        formSent.bccEmail.focus();
	        return false;
	     }
	   	}
 	}
    return true;
}

function isValidPhone(phone,msg)
{
	var regm = /[^a-zA-Z0-9()-]/;
    for (var i = 0; i < phone.length; i++) {
        if (regm.test(phone)) {
        	alert(msg);
        	return false;
        }
    }
    return true;
}
function isValidPhone(phone,msg)
{
	var regm = /[^a-zA-Z0-9()-]/;
    for (var i = 0; i < phone.length; i++) {
        if (regm.test(phone)) {
        	alert(msg);
        	return false;
        }
    }
    return true;
}

function isValidEmail(mail,msg)
{
	if ((mail.indexOf("..") != -1) || (mail.indexOf("--") != -1) || (mail.indexOf("__") != -1))
	{
		alert(msg);
	    return false;
	}
	
	var lastChar = mail.charAt(mail.length - 1);
	if (lastChar == ",")
	{
		alert(msg);
	    return false;
	}
	
	var regm = /^[a-zA-Z0-9]+([a-zA-Z0-9-_.]+)*@([a-zA-Z0-9-_.]+[.])+[a-zA-Z0-9]{2,5}$/;  
	var result = mail.split(",");
	for(var i=0;i<result.length;i++)
	{
		var mailOne = result[i].trim();
		if (!regm.test(mailOne))
		{
			alert(msg);
		    return false;
		}
	}
    return true;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<DIV ID="contentLayer"
	STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"> <%=title%> </SPAN>
<P></P>

<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
	<FORM NAME="userForm" METHOD="post">
	<TR>
		<TD NOWRAP VALIGN="TOP"><%=lbAddress%>:</TD>
		<TD><TEXTAREA CLASS="standardText" NAME="address" COLS="30"
			ROWS="4" WRAP><%=address%></TEXTAREA></TD>
	</TR>
	<TR>
		<TD VALIGN="TOP"><%=lbHomePhone%>:</TD>
		<TD><INPUT TYPE="text" SIZE="40" NAME="homePhone"
			VALUE="<%=homePhone%>"></TD>
	</TR>
	<TR>
		<TD VALIGN="TOP"><%=lbWorkPhone%>:</TD>
		<TD><INPUT TYPE="text" SIZE="40" NAME="workPhone"
			VALUE="<%=workPhone%>" ></TD>
	</TR>
	<TR>
		<TD VALIGN="TOP"><%=lbCellPhone%>:</TD>
		<TD><INPUT TYPE="text" SIZE="40" NAME="cellPhone"
			VALUE="<%=cellPhone%>" ></TD>
	</TR>
	<TR>
		<TD VALIGN="TOP"><%=lbFax%>:</TD>
		<TD><INPUT TYPE="text" SIZE="40" NAME="fax"
			VALUE="<%=fax%>" ></TD>
	</TR>
	<TR>
		<TD VALIGN="TOP"><%=lbEmail%><SPAN CLASS="asterisk">*</SPAN>:</TD>
		<TD>
			<%
			    if (!userPermissions.getPermissionFor(Permission.CHANGE_OWN_EMAIL)&&me.getUserId().equals(user.getUserId()))
						       out.print("<INPUT TYPE='text' SIZE='40' NAME='email' READONLY='readonly'	VALUE='"+email+"' >");
					  	   else
						       out.print("<INPUT TYPE='text' SIZE='40' NAME='email' VALUE='"+email+"' >");
			%>
		
		</TD>
	</TR>
	<amb:permission name="<%=Permission.USERS_ACCESS_CCEMAIL%>">
		<TR>
			<TD VALIGN="TOP"><%=lbCcEmail%>:</TD>
			<TD><INPUT TYPE="text" SIZE="40" NAME="ccEmail"
				VALUE="<%=ccEmail%>" ></TD>
		</TR>
	</amb:permission>
	<amb:permission name="<%=Permission.USERS_ACCESS_BCCEMAIL%>">
		<TR>
			<TD VALIGN="TOP"><%=lbBccEmail%>:</TD>
			<TD><INPUT TYPE="text" SIZE="40" NAME="bccEmail"
				VALUE="<%=bccEmail%>" ></TD>
		</TR>
	</amb:permission>
	<TR>
		<TD VALIGN="TOP" width=15%><%=lbLocaleEmailDates%><SPAN
			CLASS="asterisk">*</SPAN>:</TD>
		<TD><SELECT NAME="uiLocale">
			<%
				if (uiLocales != null) {
					for (int i = 0; i < uiLocales.length; i++) {
						Locale locale = PageHandler.getUILocale(uiLocales[i]);
						String locString = locale.toString();
						String display = locale.getDisplayLanguage(userUiLocale)
								+ " (" + locale.getDisplayCountry(userUiLocale)
								+ ")";

						String selected = "";
						if (locString.equals(uiLanguage)) {
							selected = " SELECTED";
						}
						out.println("<option value=\"" + locString + "\""
								+ selected + ">" + display + "</option>");
					}
				}
			%>
		</SELECT></TD>
		</TD>

	</TR>

	</FORM>
	<TR>
		<TD COLSPAN="2">&nbsp;</TD>
	</TR>
	<TR>
		<TD COLSPAN="2"><INPUT TYPE="BUTTON" NAME="<%=lbCancel%>"
			VALUE="<%=lbCancel%>" ONCLICK="location.replace('<%=cancelURL%>')">
		<INPUT TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>"
			ONCLICK="javascript: submitForm('done')"></TD>
	</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
