<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.foundation.User,
                com.globalsight.calendar.CalendarManagerLocal,
                com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.securitymgr.UserSecureFields,
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
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="prev" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);   
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    String saveURL = done.getPageURL() + "&" + WebAppConstants.USER_ACTION +
              "=" + WebAppConstants.USER_ACTION_MODIFY_USER_CONTACT;
    String prevURL = prev.getPageURL() + "&action=previous";
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String nextURL = null;
    String title = null;
    if (sessionMgr.getAttribute("editUser") != null)
    {
        title= bundle.getString("lb_edit_user") + " - " +
                    bundle.getString("lb_contact_information");
    }
    else
    {
        title= bundle.getString("lb_new_user") + " - " +
                    bundle.getString("lb_contact_information");
        nextURL = next.getPageURL() + "&action=next";
    }
    
    //Labels
    String lbAddress = bundle.getString("lb_address");
    String lbHomePhone = bundle.getString("lb_home_phone");
    String lbWorkPhone = bundle.getString("lb_work_phone");
    String lbCellPhone = bundle.getString("lb_cell_phone");
    String lbFax = bundle.getString("lb_fax");
    String lbEmail = bundle.getString("lb_email");
    String lbccEmail = bundle.getString("lb_cc_email");
    String lbbccEmail = bundle.getString("lb_bcc_email");
    String lbLocaleEmail = bundle.getString("lb_locale_email_dates");
    String lbChoose = bundle.getString("lb_choose");
    String lbDone = bundle.getString("lb_done"); 
    String lbPrev = bundle.getString("lb_previous"); 
    String lbNext = bundle.getString("lb_next"); 
    String lbCancel = bundle.getString("lb_cancel");
    String lbUserName = bundle.getString("lb_user_name");
        
    //Messages
    String jsmsgAddress = bundle.getString("jsmsg_users_address");
    String jsmsgEmail = bundle.getString("jsmsg_users_email");
    String jsmsgUiLanguage = bundle.getString("jsmsg_users_ui_language");

	//available UI languages
	String[] uiLocales = UserHandlerHelper.getUILocales();

	//User UI locale 
	Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	

    // Get the user wrapper off the session manager.
    User user = TaskHelper.getUser(session);
    CreateUserWrapper wrapper;
    if (sessionMgr.getAttribute("editUser") != null)
    {
        wrapper = (CreateUserWrapper)
             sessionMgr.getAttribute(WebAppConstants.MODIFY_USER_WRAPPER);
    } 
    else
    {
        wrapper = (CreateUserWrapper)
             sessionMgr.getAttribute(WebAppConstants.CREATE_USER_WRAPPER);
    } 
    String homePhone = wrapper.getHomePhoneNumber();
    if (homePhone == null||"null".equals(homePhone)) homePhone = "";
    String workPhone = wrapper.getOfficePhoneNumber();
    if (workPhone == null||"null".equals(workPhone)) workPhone = "";
    String cellPhone = wrapper.getCellPhoneNumber();
    if (cellPhone == null||"null".equals(cellPhone)) cellPhone = "";
    String fax = wrapper.getFaxPhoneNumber();
    if (fax == null||"null".equals(fax)) fax = "";
    String address = wrapper.getAddress();
    if (address == null||"null".equals(address)) address = "";
    String email = wrapper.getEmail();
    if (email == null||"null".equals(email)) email = "";
    String ccEmail = wrapper.getCCEmail();
    if (ccEmail == null||"null".equals(ccEmail)) ccEmail = "";
    String bccEmail = wrapper.getBCCEmail();
    if (bccEmail == null||"null".equals(bccEmail)) bccEmail = "";
    String password = user.getPassword();
    if (password == null) password = "";
    String uiLanguage = wrapper.getDefaultUILocale();
    if (uiLanguage == null) uiLanguage = "";
    boolean promptIsActive = wrapper.promptIsActive();
    boolean isActive = false;
    if (wrapper.getUser() != null)
    {
        isActive = wrapper.getUser().isActive();
    }
    String userName = wrapper.getUserName();
    FieldSecurity hash = (FieldSecurity) sessionMgr.getAttribute("securitiesHash");
%>
<HTML>
<!-- This JSP is: /envoy/administration/users/userContact.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<script SRC="/globalsight/includes/filter/StringBuffer.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = true;
var objectName = "<%= bundle.getString("lb_user") %>";
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
    if (btnName == "prev")
    {
        theForm.action = "<%=prevURL %>";
    }
    if (btnName == "done")
    {
        theForm.action = "<%=saveURL %>";
    }
    else if (btnName == "next")
    {
        theForm.action = "<%=nextURL %>";
    }
    if (confirmForm(theForm)) theForm.submit();
}

function confirmForm(formSent)
{
	if (formSent.address)
	{
		var address = formSent.address.value;
		if (address.length > 1000)
		{
			alert("Please enter an address that the length less than 1000");
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
    if (formSent.email)
    {
        var theEmail = formSent.email.value;
        theEmail = stripBlanks(theEmail);
        
        if (isEmptyString(theEmail))
        {
            <% if (isActive) { %>
                alert(" <%= jsmsgEmail %>");
                formSent.email.value = "";
                formSent.email.focus();
                return false;
            <% } else if (promptIsActive) { %>
                if (confirm("<%=bundle.getString("jsmsg_users_active2")%>"))
                {
                    return true;
                }
                return false;
            <% } %>
        } else 
        {
        	if ((theEmail !="@") && (!isValidEmail(theEmail,"<%=bundle.getString("jsmsg_email_invalid")%>"))) {
                formSent.email.focus();
                return false;
            }
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
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P></P>

     <TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
        <TR>
            <TD NOWRAP VALIGN="TOP"><%= lbUserName %><SPAN CLASS="asterisk">*</SPAN>:</TD>
            <TD ><%= userName %></TD>            
        </TR>
        <FORM NAME="userForm" METHOD="post">
        <TR>
            <TD NOWRAP VALIGN="TOP"><%= lbAddress %>:</TD>
            <TD >
            <textarea name="address" cols="30" rows="4" ><%= address %></textarea>
            </TD>            
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbHomePhone%>:</TD>
            <TD>
            <amb:textfield maxlength="200"  size="40" name="homePhone" value="<%= homePhone %>" access='<%=(String)hash.get(UserSecureFields.HOME_PHONE)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbWorkPhone%>:</TD>
            <TD>
            <amb:textfield  maxlength="200" size="40" name="workPhone" value="<%= workPhone %>" access='<%=(String)hash.get(UserSecureFields.WORK_PHONE)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbCellPhone%>:</TD>
            <TD>
            <amb:textfield  maxlength="200" size="40" name="cellPhone" value="<%= cellPhone %>" access='<%=(String)hash.get(UserSecureFields.CELL_PHONE)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbFax%>:</TD>
            <TD>
            <amb:textfield  maxlength="200" size="40" name="fax" value="<%= fax %>" access='<%=(String)hash.get(UserSecureFields.FAX)%>' />
            </TD>
        </TR>
        <TR>
            <TD VALIGN="TOP"><%= lbEmail %>
            <% if (isActive) { %>
                <SPAN CLASS="asterisk">*</SPAN>
            <% } %>
            :</TD>
            <TD >
        
		    <INPUT TYPE='text' maxlength="1000" SIZE='40' NAME='email' VALUE='<%=email%>' >
         
            </TD>
        </TR>
        <amb:permission  name="<%=Permission.USERS_ACCESS_CCEMAIL%>" >
	        <TR>
	        	<TD VALIGN="TOP"><%= lbccEmail %>:</TD>
	        	<TD>
	        	<amb:textfield maxlength="1000" size="40" name="ccEmail" value="<%= ccEmail %>" access='<%=(String)hash.get(UserSecureFields.CC_EMAIL_ADDRESS)%>' />
	        	</TD>
	        </TR>
        </amb:permission>
        <amb:permission  name="<%=Permission.USERS_ACCESS_BCCEMAIL%>" >
	        <TR>
	        	<TD VALIGN="TOP"><%= lbbccEmail %>:</TD>
	        	<TD>
	        	<amb:textfield maxlength="1000" size="40" name="bccEmail" value="<%= bccEmail %>" access='<%=(String)hash.get(UserSecureFields.BCC_EMAIL_ADDRESS)%>' />
	        	</TD>
	        </TR>
        </amb:permission>
        <TR>
            <TD VALIGN="TOP" width=15%><%= lbLocaleEmail %><SPAN CLASS="asterisk">*</SPAN>:</TD>
            <TD> 
            <amb:select name="uiLocale" access='<%=(String)hash.get(UserSecureFields.EMAIL_LANGUAGE)%>'>
            <%                   
            if (uiLocales != null)
            {
                for (int i = 0; i < uiLocales.length; i++)
                {
                    Locale locale = PageHandler.getUILocale(uiLocales[i]);
                    String locString = locale.toString();
                    String display = locale.getDisplayLanguage(userUiLocale) +
                                     " (" + locale.getDisplayCountry(userUiLocale) + ")";
                    
                    boolean selected = false;
                    if (locString.equals(uiLanguage))
                    {
                        selected = true;
                    }
              %>
                    <amb:selectOption value="<%=locString%>"
                         selected="<%=Boolean.toString(selected)%>"
                         displayedText="<%=display%>" />
             <%
                }
            }
            %>
            </amb:select>
          </TD>
        </TR>

        </FORM>
        <TR>
        <TD COLSPAN="2">&nbsp;
        </TD>
        </TR>
        <TR>
            <TD COLSPAN="2">
                <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
                    ONCLICK="location.replace('<%=cancelURL%>')">
<%
                if (sessionMgr.getAttribute("editUser") != null)
                {
%>
                    <INPUT TYPE="BUTTON" NAME="<%=lbDone%>" VALUE="<%=lbDone%>" 
                        ONCLICK="javascript: submitForm('done')">
<%              } else {
%>
                    <INPUT TYPE="BUTTON" NAME="<%=lbPrev%>" VALUE="<%=lbPrev%>" 
                        ONCLICK="javascript: submitForm('prev')">
            
                    <INPUT TYPE="BUTTON" NAME="<%=lbNext%>" VALUE="<%=lbNext%>" 
                        ONCLICK="javascript: submitForm('next')">
<%              }
%>
            </TD>
        </TR>
        </TABLE>
</DIV>
</BODY>
</HTML>
