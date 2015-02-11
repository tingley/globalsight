<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
                 com.globalsight.everest.company.Company,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
	String lbnext = bundle.getString("lb_next");
    boolean edit = false;
    String nextURL = next.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        nextURL += "&action=" + CompanyConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_company");
    }
    else
    {
        nextURL += "&action=" + CompanyConstants.NEXT;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_company");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + CompanyConstants.CANCEL;

    // Data
    ArrayList names = (ArrayList)request.getAttribute(CompanyConstants.NAMES);
    Company company = (Company)sessionMgr.getAttribute(CompanyConstants.COMPANY);
    String companyName = "";
    String email = (String)request.getAttribute(CompanyConstants.EMAIL);
    String desc = "";
    String checked = "checked";//default
    String tmAccessControl = "";//default
    String tbAccessControl = "";//default
    String ssoChecked = "";//default
    String isSsoChecked = "false";//default
    String ssoIdpUrl = "";
    String sessionTime = "";
    boolean isReviewOnly = false;
    String enableTM3Checked = "";
    if (company != null)
    {
        companyName = company.getName();
        desc = company.getDescription();
        email = company.getEmail();
        sessionTime = company.getSessionTime();
        
        if (desc == null) desc = "";
        if (email == null) email = "";
        if (sessionTime==null) sessionTime="";
        
        boolean enableIPFilte = company.getEnableIPFilter();
        if (enableIPFilte==false) {
        	checked = "";
        }
        
        boolean enableTMAcessControl = company.getEnableTMAccessControl();
        if (enableTMAcessControl) {
            tmAccessControl = "checked";
        }
        
        boolean enableTBAcessControl = company.getEnableTBAccessControl();
        if (enableTBAcessControl) {
            tbAccessControl = "checked";
        }
          
        if (company.getEnableSSOLogin())
        {
            ssoChecked = "checked";
            isSsoChecked = "true";
        }
        
        ssoIdpUrl = company.getSsoIdpUrl();
        ssoIdpUrl = ssoIdpUrl == null ? "" : ssoIdpUrl;
        
        if (company.getTmVersion().getValue() == 3)
            enableTM3Checked = "checked";
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_company")%>";
var guideNode="companies";
var helpFile = "<%=bundle.getString("help_companies_basic_screen")%>";
function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        companyForm.action = "<%=cancelURL%>";
        companyForm.submit();
    }
    if (formAction == "next")
    {
		if (confirmForm() && confirmTime())
		{
			companyForm.action = "<%=nextURL%>";
            companyForm.submit();
		}
    }
}

//
// Check required fields(SSO, email, name).
// Check duplicate activity name.
//
function confirmForm()
{
	// check sso
	var ssoLogonElem = companyForm.enableSsoLogonField;
	if(ssoLogonElem!=null && ssoLogonElem.checked)
    {
        var idpUrl = companyForm.ssoIdpUrlField.value;
        if (isEmptyString(idpUrl))
        {
        	alert("<%=bundle.getString("msg_sso_input_valid_idpurl")%>");
            return false;
        }
    }
	
	// Check Email Field
	var emailElem = document.getElementById("emailId");
	var sysNotificationEnable = "<%=request.getAttribute(SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED)%>";
    if("true" == sysNotificationEnable)
    {
    	var email = stripBlanks(emailElem.value);
    	if(email.length > 0 && !validEmail(email))
    	{
    		alert("<%=bundle.getString("jsmsg_email_invalid")%>");
            return false;
    	}
    }
	
	// check name
    if (!companyForm.nameField) 
    {
        // can't change name on edit
        return true;
    }
    if (isEmptyString(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_company_name"))%>");
        companyForm.nameField.value = "";
        companyForm.nameField.focus();
        return false;
    }        
    if (hasSpecialChars(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }
    // check for dups 
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String comName = (String)names.get(i);
%>
            if ("<%=comName%>".toLowerCase() == companyForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company"))%>");
                return false;
            }
<%
        }
    }
%>

	return true;
}

function confirmTime()
{
	var sessionTime = companyForm.sessionTimeField.value;
	{
		if (sessionTime!='')
		{
			if(isNumeric(sessionTime))
			{
				sessionTime = parseInt(sessionTime)
				if (sessionTime > 480 || sessionTime < 30)
				{
					alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
					return false;
				}
			}
			else
			{
				alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
				return false;
			}
		}
	}
    return true;
}

function isNumeric(str){
	if (str.startsWith("0"))
		return false;
	return /^(-|\+)?\d+(\.\d+)?$/.test(str);
}

function onEnableSSOSwitch()
{
	onEnableSSO(companyForm.enableSsoLogonField.checked);
}

function onEnableSSO(checked)
{
	var ele = document.getElementById("ssoIdpUrlCC");
	var display = checked ? "" : "none";
	ele.style.display = display;
}

function doOnload()
{
    loadGuides();

    var edit = eval("<%=edit%>");
    if (edit)
    {
        companyForm.<%=CompanyConstants.DESC%>.focus();
    }
    else
    {
        companyForm.<%=CompanyConstants.NAME%>.focus();
    }

    var enableSSO = <%=request.getAttribute(SystemConfigParamNames.ENABLE_SSO)%>;
    if(!enableSSO)
    {
		document.getElementById("ssoCheck").style.display = "none";
		document.getElementById("ssoIdpUrlCC").style.display = "none";
    }
    else
    {
    	onEnableSSO(eval("<%=isSsoChecked%>"));
    }   
}
</script>

</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="companyForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
            <td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
            <td>
                <% if (edit) { %>
                    <%=companyName%>
                <% } else { %>
                    <input type="text" name="<%=CompanyConstants.NAME%>" maxlength="40" size="30" value="<%=companyName%>">
                <% } %>
            </td>
            <td valign="center">
                <% if (!edit) { %>
                    <%=bundle.getString("lb_valid_name")%>
                <% } %>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_description")%>:</td>
            <td colspan="2">
                <textarea rows="6" cols="40" style="width:350px;" name="<%=CompanyConstants.DESC%>"><%=desc%></textarea>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_email")%>:</td>
            <td colspan="2">
                <input type="text" style="width:350px;" name="<%=CompanyConstants.EMAIL%>" id="emailId" value="<%=email%>">
            </td>
        </tr>
        
        <tr>
        	<td valign="top"><%=bundle.getString("lb_session_timeout")%>&nbsp;(<%=bundle.getString("lb_minutes")%>):</td>
        	<td colspan="2">
                <input type="text" name="<%=CompanyConstants.SESSIONTIME%>" maxlength="3" size="20" value="<%=sessionTime%>">&nbsp;(30-480)
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableIPFilter")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableIPFilterId" name="<%=CompanyConstants.ENABLE_IP_FILTER%>" <%=checked%>/>
            </td>
        </tr>
        
        <tr id="ssoCheck">
            <td valign="top"><%=bundle.getString("lb_sso_enableSSO")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableSsoLogonId" onclick="onEnableSSOSwitch()" name="<%=CompanyConstants.ENABLE_SSO_LOGON%>" <%=ssoChecked%>/>
            </td>
        </tr>
        <tr id="ssoIdpUrlCC">
            <td valign="top"><%=bundle.getString("lb_sso_IdpUrl")%>:</td>
            <td colspan="2">
                <input type="textfield" name="<%=CompanyConstants.SSO_IDP_URL%>" maxlength="256" size="30" value="<%=ssoIdpUrl%>">
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_tm_tm3_enable")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="<%=CompanyConstants.TM3_VERSION%>" name="<%=CompanyConstants.TM3_VERSION%>" value="3" <%=enableTM3Checked%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTMAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTMAccessControl" name="<%=CompanyConstants.ENABLE_TM_ACCESS_CONTROL%>" <%=tmAccessControl%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTBAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTBAccessControl" name="<%=CompanyConstants.ENABLE_TB_ACCESS_CONTROL%>" <%=tbAccessControl%>/>
            </td>
        </tr>
        
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr>
            <td colspan="3">
                <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
                <input type="button" name="<%=lbnext%>" value="<%=lbnext%>" onclick="submitForm('next')">
            </td>
        </tr>

      </table>
    </td>
  </tr>
  
</table>
</form>
</div>
</body>
</html>
