<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,
                 com.globalsight.everest.cvsconfig.*,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true" 
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + CVSConfigConstants.UPDATE;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_cvsserver");
    }
    else
    {
        saveURL +=  "&action=" + CVSConfigConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvsserver");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + CVSConfigConstants.CANCEL;

    // Data
    ArrayList<CVSServer> servers = (ArrayList)request.getAttribute(CVSConfigConstants.CVS_SERVER_LIST);
    CVSServer cvsserver = (CVSServer)sessionMgr.getAttribute(CVSConfigConstants.CVS_SERVER);
    String serverName = "", hostIP = "", hostPort = "2401", sandbox = "", repository = "", loginUser = "", loginPwd = "";
    int protocol = 0;
    long serverId = 0;
    boolean isReviewOnly = false;
    if (edit)
    {
    	serverId = cvsserver.getId();
        serverName = cvsserver.getName();
        hostIP = cvsserver.getHostIP();
        hostPort = String.valueOf(cvsserver.getHostPort());
        protocol = cvsserver.getProtocol();
        sandbox = cvsserver.getSandbox() == null ? "" : cvsserver.getSandbox();
        repository = cvsserver.getRepository();
        loginUser = cvsserver.getLoginUser();
        loginPwd = cvsserver.getLoginPwd();
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_cvs_server")%>";
var guideNode="cvsserver";
var helpFile = "#";
function submitForm(formAction)
{
    if (formAction == "cancel")
    {
    	cvsserverForm.action = "<%=cancelURL%>";
    	cvsserverForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
        	cvsserverForm.action = "<%=saveURL%>";
        	cvsserverForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate activity name.
//
function confirmForm()
{
    if (isEmptyString(cvsserverForm.servername.value) || !validInput(cvsserverForm.servername.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_server_name")%>");
        cvsserverForm.servername.value = "";
        cvsserverForm.servername.focus();
        return false;
    }
    if (isEmptyString(cvsserverForm.hostIP.value)) {
        alert("<%=bundle.getString("jsmsg_cvs_server_hostip")%>");
        cvsserverForm.hostIP.focus();
        return false;
    }
    if (!isAllDigits(cvsserverForm.hostPort.value)) {
        alert("<%=bundle.getString("jsmsg_cvs_server_port")%>");
        return false;
    }        
    if (isEmptyString(cvsserverForm.sandbox.value) || !validInput(cvsserverForm.sandbox.value)) {
        alert("<%=bundle.getString("jsmsg_cvs_server_sandbox")%>");
        return false;
    }
    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_repository_cvs_name")%>");
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.focus();
        return false;
    }
    
    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_repository_username")%>");
        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.focus();
        return false;
    }
    
    if (cvsserverForm.protocol.value == 0) {
        if (cvsserverForm.loginPwd.value == cvsserverForm.loginPwdCfm.value) {
            if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value)) {
		    	alert("<%=bundle.getString("jsmsg_cvs_repository_password")%>");
		        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value = "";
	            cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD_CONFIRM%>.value = "";
		        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.focus();
		        return false;
            }
        } else {
            alert("<%=bundle.getString("jsmsg_cvs_repository_password_cfm")%>");
            cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value = "";
            cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD_CONFIRM%>.value = "";
            cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.focus();
            return false;
        }
    }
    
    // check for dups 
<%
    if (servers != null)
    {
        for (CVSServer c : servers)
        {
            if (c.getId() == serverId)
            	continue;
%>
            if ("<%=c.getName()%>".toLowerCase() == cvsserverForm.servername.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_cvsserver"))%>");
                return false;
            }
<%
        }
    }
%>

    return true;
}

function doOnload()
{
    loadGuides();
}

function setPort() {
	var s = cvsserverForm.protocol.value;
	if (s == 0)
		cvsserverForm.hostPort.value = 2401;
	else
		cvsserverForm.hostPort.value = 22;
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

<form name="cvsserverForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_cvsservers") + " " + bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
				    <input type="textfield" name="<%=CVSConfigConstants.SERVER_NAME%>"
				    maxlength="40" size="30" value="<%=serverName%>">
          </td>
          <td valign="center">(<%=bundle.getString("msg_cvs_server_name_format") %>)</td>
        </tr>
        <tr>
          <td valign="top">
	    <%=bundle.getString("lb_cvs_protocol")%><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    		<select name="protocol" onchange="setPort();">
		    		  <option value="1" <%=protocol==1?"selected":"" %>>Ext</option>
		    		  <option value="0" <%=protocol==0?"selected":"" %>>Pserver</option>
		    		</select>
          </td>
        </tr>
        <tr>
          <td valign="top">
	    <%=bundle.getString("lb_cvs_hostip")%><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.HOST_IP%>"
			    maxlength="100" size="30" value="<%=hostIP%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	    <%=bundle.getString("lb_cvs_host_port")%>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.HOST_PORT%>"
			    maxlength="10" size="30" value="<%=hostPort%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_cvs_name") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.REPOSITORY_CVS%>"
			    maxlength="200" size="30" value="<%=repository%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_username") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>"
			    maxlength="100" size="30" value="<%=loginUser%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_password") %>:
          </td>
          <td colspan="2">
		    <input type="password" name="<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>"
			    maxlength="32" size="30" value="<%=loginPwd%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_cvs_repository_password_cfm") %>:
          </td>
          <td colspan="2">
            <input type="password" name="<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD_CONFIRM%>"
                maxlength="32" size="30" value="<%=loginPwd%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	    <%=bundle.getString("lb_cvs_sandbox")%><span class="asterisk">*</span>:
          </td>
          <td>
          <% if (edit) { %>
		    <input type="textfield" maxlength="100" size="30" value="<%=sandbox%>" disabled />
			<input type="hidden" name="<%=CVSConfigConstants.SANDBOX%>" maxlength="100" size="30" value="<%=sandbox%>" />
	      <% } else { %>
 	        <input type="textfield" name="<%=CVSConfigConstants.SANDBOX%>" maxlength="100" size="30" value="<%=sandbox%>"/>
 	      <% } %>
          </td>
          <td valign="center">(<%=bundle.getString("msg_cvs_server_name_format") %>)</td>
        </tr>
        <tr><td>&nbsp;</td></tr>
	<tr>
	  <td>
	    <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
	    onclick="submitForm('cancel')">
	    <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
	    onclick="submitForm('save')">
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