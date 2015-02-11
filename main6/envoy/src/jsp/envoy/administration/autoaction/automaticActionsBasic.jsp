<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.autoactions.*,
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
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_automatic_actions");
    }
    else
    {
        saveURL +=  "&action=create";
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_automatic_actions");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    ArrayList names = (ArrayList)request.getAttribute("allActionNames");
    AutoAction autoAction = (AutoAction)request.getAttribute("autoAction");
    String name = "", email = "", description = "";
    long id = 0;
    boolean isReviewOnly = false;

    if (edit)
    {
    	  id = autoAction.getId();
        name = autoAction.getName();
        email = autoAction.getEmail();
        description = autoAction.getDescription();
        saveURL =  saveURL + "&actionID=" + id;
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
var objectName = "";
var guideNode = "autoAction";
var helpFile = "#";
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

function isValidEmail(mail)
{
    var regm = '@';
    for (var i = 0; i < mail.length; i++) {
        if (mail.charAt(i) == '@') {
            return true;
        }
    }
    
    alert("<%=bundle.getString("lb_email_invalid")%>");
    return false;
}

//
// Check required fields.
// Check duplicate activity name.
//
function confirmForm()
{
    if (isEmptyString(actionsForm.name.value))
    {
        alert("<%=bundle.getString("jsmsg_autoActions_name_warning")%>");
        actionsForm.name.value = "";
        actionsForm.name.focus();
        return false;
    }
    
    if (isEmptyString(actionsForm.email.value))
    {
        alert("<%=bundle.getString("jsmsg_autoActions_email_warning")%>");
        actionsForm.email.value = "";
        actionsForm.email.focus();
        return false;
    }
    
    if (!isValidEmail(actionsForm.email.value)) {
        actionsForm.email.focus();
        return false;
    }

<%
    if (names != null)
    {
    	for (int i = 0; i < names.size(); i++ ) {
    		%>
            if ("<%=name%>" != actionsForm.name.value && "<%=names.get(i)%>".toLowerCase() == actionsForm.name.value.toLowerCase()) {
                alert("<%=bundle.getString("jsmsg_duplicate_names")%>");
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
	        <input type="textfield" name="name"
	         maxlength="40" size="30" value="<%=name%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_email") %><span class="asterisk">*</span>:
          </td>
          <td>
		    <input type="textfield" name="email"
			    maxlength="40" size="30" value="<%=email%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_description") %>:
          </td>
          <td>
		    <input type="textfield" name="description" maxlength="40" size="30" value="<%=description%>">
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
</body>
</html>
