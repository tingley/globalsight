<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,java.util.Iterator,
            java.util.Locale,
            java.util.Vector,
            java.util.ResourceBundle,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.vendormanagement.VendorManagementLocal,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserStateConstants,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
            com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,            
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.modules.Modules,
            com.globalsight.util.edit.EditUtil,
            java.util.ArrayList,
            com.globalsight.everest.foundation.SSOUserUtil"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
PermissionSet perms = (PermissionSet) session.getAttribute(
    WebAppConstants.PERMISSIONS);

String nextURL = next.getPageURL() + "&action=next";
String cancelURL = cancel.getPageURL() + "&action=cancel";

//Labels read from bundle
String title= bundle.getString("lb_new_user") + " - " +
  bundle.getString("lb_basic_information");

String lbUserName = bundle.getString("lb_user_name");
String lbSsoUserName = bundle.getString("lb_sso_username");
String lbPassword = bundle.getString("lb_password");
String lbPasswordRepeat = bundle.getString("lb_password_repeat");
String lbFirstName = bundle.getString("lb_first_name");
String lbLastName = bundle.getString("lb_last_name");
String lbAccessLevel = bundle.getString("lb_access_level");
String lbVendorAccessLevel = /*bundle.getString("lb_access_level")*/"Access Level for<BR>Vendor Management:";
String lbTitle = bundle.getString("lb_title");
String lbWssePassword = bundle.getString("lb_wsse_password");
String lbCompanyName = bundle.getString("lb_company_name");
String lbChoose = bundle.getString("lb_choose");
String lbUser = bundle.getString("lb_user");
String lbSave = bundle.getString("lb_save");
String lbCancel = bundle.getString("lb_cancel");
String lbNext = bundle.getString("lb_next");

//Messages
String jsmsgUserName = EditUtil.toJavascript(bundle.getString("jsmsg_users_user_name"));
String jsmsgPassword = EditUtil.toJavascript(bundle.getString("jsmsg_users_password"));
String jsmsgRepeatPassword = EditUtil.toJavascript(bundle.getString("jsmsg_users_repeat_password"));
String jsmsgFirstName = EditUtil.toJavascript(bundle.getString("jsmsg_users_first_name"));
String jsmsgLastName = EditUtil.toJavascript(bundle.getString("jsmsg_users_last_name"));
String jsmsgGroup = EditUtil.toJavascript(bundle.getString("jsmsg_group"));
String jsmsgSelectGroup = EditUtil.toJavascript("At least one group must be selected.");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

//available UI languages
String[] uiLocales = UserHandlerHelper.getUILocales();

//User UI locale
Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

// bring in "state" from session
CreateUserWrapper wrapper = (CreateUserWrapper)sessionMgr.getAttribute(
  UserConstants.CREATE_USER_WRAPPER);

String uiLocale = wrapper.getDefaultUILocale();
String userName = wrapper.getUserName();
String ssoUserName = null;
String ssoUserId = wrapper.getSsoUserId();
if (ssoUserId != null)
{
	ssoUserName = UserUtil.getUserNameById(ssoUserId);
}
String password = wrapper.getPassword();
String passwordConfirm = wrapper.getPassword();
String firstName = wrapper.getFirstName();
String lastName = wrapper.getLastName();
String userTitle = wrapper.getTitle();
String wssePassword = wrapper.getWssePassword();
String companyName = wrapper.getCompanyName();

if (userTitle == null) userTitle = "";
if (wssePassword == null) wssePassword = "";
if (companyName == null) companyName = "";

String[] companies = (String[])sessionMgr.getAttribute("companyNames");
boolean isCompanyKnown = false;
if (companies != null)
{
  for (int i = 0; i < companies.length; i++)
  {
    if (companies[i].equals(companyName))
    {
       isCompanyKnown = true;
    }
  }
}

String[] userNames = UserUtil.getAllUserNames();
int size = userNames == null ? 0 : userNames.length;

//if you can see all users, then you can change the company name, otherwise not
boolean canChangeCompanyName = UserUtil.isSuperAdmin((String)request.getSession().getAttribute(WebAppConstants.USER_NAME));
String myCompanyName = ((User) sessionMgr.getAttribute(WebAppConstants.USER)).getCompanyName();

String ccname = canChangeCompanyName ? companyName : myCompanyName;
boolean enableSSO = SSOUserUtil.isCompanyEnableSSO(ccname);
%>

<HTML>
<!-- This JSP is envoy/administratin/users/new1.jsp-->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = true;
var objectName = "<%= lbUser %>";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_user_information")%>";
var company;

function submitForm()
{
  var theForm;

  if (document.layers)
  {
    theForm = document.layers.contentLayer.document.userForm;
  }
  else
  {
    theForm = document.all.userForm;
  }

  if (confirmForm(theForm))
  {
    if (company)
      userForm.company.value = "true";
    else
      userForm.company.value = "false";

    // issue warning if password not entered
    if (checkPassword())
    {
        theForm.submit();
    }
  }
}

function checkPassword()
{
    var thePassword = userForm.password.value;
    thePassword = stripBlanks(thePassword);

    if (isEmptyString(thePassword))
    {
        if (!confirm("<%=bundle.getString("jsmsg_users_active1")%>"))
        {
            userForm.password.value = "";
            userForm.password.focus();
            return false;
        }
        return true;
    }
    else
    {
        // check that password and repeat are the same

        var theRepeat = userForm.passwordConfirm.value;
        theRepeat = stripBlanks(theRepeat);

        var samePassword = (thePassword == theRepeat);
        if (!samePassword)
        {
            alert("<%= jsmsgRepeatPassword %>");
            userForm.passwordConfirm.value = "";
            userForm.password.value = "";
            userForm.password.focus();
            return false;
        }
    }
    return true;
}

function confirmForm(formSent)
{
    var theName = formSent.userName.value;
    theName = stripBlanks(theName);
    if (isEmptyString(theName))
    {
        alert("<%= jsmsgUserName %>");
        formSent.userName.value = "";
        formSent.userName.focus();
        return false;
    }
    <% for (int i=0; i<size; i++){ %>
       if(theName.toLowerCase() == "<%= userNames[i].toLowerCase() %>")
       {
          alert('<%=bundle.getString("jsmsg_duplicate_users")%>');
          return false;
       }
    <% } %>

    if (hasSomeSpecialChars(theName))
    {
        alert("<%= lbUserName %>" + "<%= bundle.getString("msg_invalid_entry3") %>");
        return false;
    }

    var theFirst = formSent.firstName.value;
    theFirst = stripBlanks(theFirst);

    if (isEmptyString(theFirst))
    {
        alert("<%= jsmsgFirstName %>");
        formSent.firstName.value = "";
        formSent.firstName.focus();
        return false;
    }
    if (hasSpecialChars(theFirst))
    {
        alert("<%= lbFirstName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }


    var theLast = formSent.lastName.value;
    theLast = stripBlanks(theLast);

    if (isEmptyString(theLast))
    {
        alert("<%= jsmsgLastName %>");
        formSent.lastName.value = "";
        formSent.lastName.focus();
        return false;
    }
    if (hasSpecialChars(theLast))
    {
        alert("<%= lbLastName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }

    return true;
}

<% if (canChangeCompanyName) {%>
function companiesRadioOn()
{
    company = false;
}
<%}%>
function doLoad()
{
<% if (canChangeCompanyName) {%>
    if ("<%=isCompanyKnown%>" == "true")
    {
        userForm.companyName.value = "";
    }
<%}%>

    if ("<%= (userName == null)%>" == "true")
    {
      userForm.userName.focus();
    }

    loadGuides();
}

function removeTailBlank(str)
{
   if(str != null && str != "")
   {
        return str.replace( /\s*$/, "");
   }
   else
   {
		return "";
   }
}

function fnUserNameBlur(elem)
{
	elem.value=removeTailBlank(elem.value);

	var ffUser = document.getElementById("ffUserId");
	ffUser.value = elem.value;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0"
 MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="doLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_users_new")%>
</TD>
</TR>
</TABLE>

<P></P>

<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
        <FORM NAME="userForm" ACTION="<%=nextURL%>" METHOD="post">
        <input type="hidden" name="company">
        <TR>
            <TD><%= lbUserName %><SPAN CLASS="asterisk">*</SPAN>:
            </TD>
            <TD><%
            if(userName != null)
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="userName" VALUE="<%= userName %>" onBlur="fnUserNameBlur(this);"><%
            }
            else
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="userName" onBlur="fnUserNameBlur(this);"><%
            }
            %></TD>
        </TR>
        <% if (enableSSO) {%>
        <TR>
            <TD><%= lbSsoUserName %>:
            </TD>
            <TD><%
            if(ssoUserName != null)
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="ssoUserName" VALUE="<%= ssoUserName %>" /><%
            }
            else
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="ssoUserName" /><%
            }
            %></TD>
        </TR>
        <% } %>
        <TR>
            <TD><%= lbFirstName %><SPAN CLASS="asterisk">*</SPAN>:</TD>
            <TD><%
            if(firstName != null)
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="firstName" VALUE="<%= firstName %>"><%
            }
            else
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="firstName"><%
            }
            %></TD>
        </TR>
        <TR>
            <TD><%= lbLastName%><SPAN CLASS="asterisk">*</SPAN>:</TD>
            <TD><%
            if(lastName != null)
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="lastName" VALUE="<%= lastName %>"><%
            }
            else
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="lastName"><%
            }
            %></TD>
        </TR>
        
		<!-- Added for alias name, for remember password in Firefox  -->
  		<TR style="display:none;">
    		<TD></TD>
    		<TD>
      			<input type="text" id="ffUserId" value="<%= userName %>"/>
    		</TD>
  		</TR>
  		<!-- End Added for alias name,...  -->
        
        <TR>
            <TD><%= lbPassword %></SPAN>:</TD>
            <TD><%
            if(password != null)
            {
                %><INPUT TYPE="password" SIZE="20" NAME="password" VALUE="<%=password%>"><%
            }
            else
            {
                %><INPUT TYPE="password" SIZE="20" NAME="password"><%
            }
            %></TD>
        </TR>
        <TR>
            <TD><%= lbPasswordRepeat%></SPAN>:</TD>
            <TD><%
            if(passwordConfirm != null)
            {
                %><INPUT TYPE="password" SIZE="20" NAME="passwordConfirm" VALUE="<%= passwordConfirm %>"><%
            }
            else
            {
                %><INPUT TYPE="password" SIZE="20" NAME="passwordConfirm"><%
            }
            %></TD>
        </TR>
        <TR>
            <TD><%= lbTitle%>:</TD>
            <TD><%
            if(userTitle != null)
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="20" NAME="title" VALUE="<%= userTitle %>"><%
            }
            else
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="42" NAME="title"><%
            }
            %></TD>
        </TR>
        <TR>
            <TD><%= lbWssePassword%>:</TD>
            <TD><%
            if(wssePassword != null)
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="wssePassword" VALUE="<%= wssePassword %>"><%
            }
            else
            {
                %><INPUT TYPE="text" MAXLENGTH="40" SIZE="40" NAME="wssePassword"><%
            }
            %></TD>
        </TR>
<%          //If is super admin
            if (canChangeCompanyName)
            {
                out.println("<TR>");
                out.println("<TD>" + lbCompanyName + ":</TD>");
                out.println("<TD>");
                out.println("<select name='companies' onchange='companiesRadioOn()'>");
                for (int i = 0; i < companies.length; i++)
                {
                    out.println("<option value='"+companies[i]+"'");
                    if (companies[i].equals(companyName))
                    {
                        out.println(" selected ");
                    }
                    out.println(">" + companies[i] + "</option>");
                }
                out.println("</select>");
                out.println("</TD>");
                out.println("</TR>");
            }
%>
        </FORM>

        <TR>
        <TD COLSPAN="2">&nbsp;</TD>
        </TR>
        <TR>
            <TD COLSPAN="2">
                <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
                    onclick="location.replace('<%=cancelURL%>')">
                <INPUT TYPE="BUTTON" NAME="<%=lbNext%>" VALUE="<%=lbNext%>"
                    onclick="submitForm()">
            </TD>
        </TR>
      </TABLE>
</DIV>
</BODY>
</HTML>
