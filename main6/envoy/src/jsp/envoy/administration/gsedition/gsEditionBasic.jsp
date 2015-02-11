<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.gsedition.*,
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
        saveURL +=  "&action=modify";
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_gsedition");
    }
    else
    {
        saveURL +=  "&action=create";
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_gsedition");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    ArrayList names = (ArrayList)request.getAttribute("allGSEditionNames");
    GSEdition gsEdition = (GSEdition)request.getAttribute("gsEdition");
    String name = "", hostName = "", hostPort = "",userName = "",password = "",description = "";
    long id = 0;
    boolean enableHttps = false;
    boolean isReviewOnly = false;

    if (edit)
    {
    	  id = gsEdition.getId();
        name = gsEdition.getName();
        hostName = gsEdition.getHostName();
        hostPort = gsEdition.getHostPort();
        userName = gsEdition.getUserName();
        password = gsEdition.getPassword();
        description = gsEdition.getDescription();
        enableHttps = gsEdition.getEnableHttps();
        saveURL =  saveURL + "&gsEditionID=" + id;
    }
    
    if(request.getParameter("name") !=null) {
        name = request.getParameter("name");
    }
    
    if(request.getParameter("hostName") !=null) {
        hostName = request.getParameter("hostName");
    }
    
    if(request.getParameter("hostPort") !=null) {
        hostPort = request.getParameter("hostPort");
    }
    
    if(request.getParameter("userName") !=null) {
        userName = request.getParameter("userName");
    }
    
    if(request.getParameter("password") !=null) {
        password = request.getParameter("password");
    }
    
    if(request.getParameter("description") !=null) {
        description = request.getParameter("description");
    }
    
	String errorInfo = request.getParameter("AmbassadorLoginError");
%>
<html>
<head>
<title><%=title%></title>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "";
var guideNode = "gsEdition";
var helpFile = "<%=bundle.getString("help_gsEdition_configBasic")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
    	actionsForm.action = "<%=cancelURL%>";
    	actionsForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
        	actionsForm.action = "<%=saveURL%>";
        	actionsForm.submit();
        }
    }
}

function confirmForm()
{
    if (isEmptyString(actionsForm.name.value))
    {
        alert("<%=bundle.getString("jsmsg_gsedition_name_warning")%>");
        actionsForm.name.value = "";
        actionsForm.name.focus();
        return false;
    }
    if (hasSpecialChars(actionsForm.name.value))
    {
        alert("<%=bundle.getString("lb_name")%>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    
    if (isEmptyString(actionsForm.hostName.value))
    {
        alert("<%=bundle.getString("jsmsg_gsedition_host_name_warning")%>");
        actionsForm.hostName.value = "";
        actionsForm.hostName.focus();
        return false;
    }
    
    if (isEmptyString(actionsForm.hostPort.value))
    {
        alert("<%=bundle.getString("jsmsg_gsedition_host_port_warning")%>");
        actionsForm.hostPort.value = "";
        actionsForm.hostPort.focus();
        return false;
    }
    
    if (isEmptyString(actionsForm.userName.value))
    {
        alert("<%=bundle.getString("jsmsg_db_connection_username")%>");
        actionsForm.userName.value = "";
        actionsForm.userName.focus();
        return false;
    }
    
    if (isEmptyString(actionsForm.password.value))
    {
        alert("<%=bundle.getString("jsmsg_db_connection_password")%>");
        actionsForm.password.value = "";
        actionsForm.password.focus();
        return false;
    }
    
    if (!isAllDigits(actionsForm.hostPort.value)) {
        alert("<%=bundle.getString("jsmsg_gsedition_num_warning")%>");
        actionsForm.email.focus();
        return false;
    }

    if (hasSpecialChars(actionsForm.description.value))
    {
        alert("<%=bundle.getString("lb_description")%>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }

<%
    if (names != null)
    {
    	for (int i = 0; i < names.size(); i++ ) {
    		%>
            if ("<%=name%>" != actionsForm.name.value && "<%=names.get(i)%>".toLowerCase() == actionsForm.name.value.toLowerCase()) {
                alert("<%=bundle.getString("jsmsg_duplicate_gs_edition_name")%>");
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
    <%  if (errorInfo != null && errorInfo.trim().length() > 0) { %>
            alert("<%=errorInfo%>");
    <%  }%>
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
<form name="actionsForm" method="post" action="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name") %>
            <span class="asterisk">*</span>:
          </td>
          <td>
	        <input type="textfield" name="name" style="width:215px;"
	         maxlength="40" size="30" value="<%=name%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_host_name") %><span class="asterisk">*</span>:
          </td>
          <td>
		    <input type="textfield" name="hostName" style="width:215px;"
			    maxlength="40" size="30" value="<%=hostName%>" <%if (edit) out.println("disabled");%>>
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_enable_https") %>:
          </td>
          <td>
		    <INPUT CLASS="standardText" NAME="enableHttps" TYPE="checkbox" <%=enableHttps?"CHECKED":""%> 
		        <%if (edit) out.println("disabled");%>/>
          </td>
        </tr>        
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_host_port") %><span class="asterisk">*</span>:
          </td>
          <td>
		    <input type="textfield" name="hostPort" style="width:215px;"
			    maxlength="40" size="30" value="<%=hostPort%>" <%if (edit) out.println("disabled");%>>
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_user_name") %><span class="asterisk">*</span>:
          </td>
          <td>
		    <input type="textfield" name="userName" style="width:215px;"
			    maxlength="40" size="30" value="<%=userName%>">
          </td>
        </tr>
        <tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_password") %><span class="asterisk">*</span>:
          </td>
          <td>
		    <input type="password" name="password" style="width:215px;"
			    maxlength="40" size="30" value="<%=password%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_description") %>:
          </td>
          <td>
		    <input type="textfield" name="description" style="width:215px;" maxlength="40" size="30" value="<%=description%>">
          </td>
        </tr>
        <tr><td colspan="2">&nbsp;</td></tr>
	<tr>
	  <td colspan="2">
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
<script>
    <%
    if(request.getParameter("noPermission") != null) {
    %>
        alert("The user has no 'Upload Files' permission to create job.");
    <%
    }
    %>
</script>
</body>
</html>
