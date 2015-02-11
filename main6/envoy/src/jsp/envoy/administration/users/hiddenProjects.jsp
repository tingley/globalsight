<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.usermgr.UserInfo,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.projecthandler.Project,
         com.globalsight.everest.projecthandler.ProjectInfo,
         java.text.MessageFormat,
         java.util.Locale,
         java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelEdit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String doneUrl = done.getPageURL()+"&action=" + WebAppConstants.USER_ACTION_MODIFY_USER_PROJECTS;
    String cancelUrl = cancelEdit.getPageURL()+"&action=cancelEdit";


    String title =  bundle.getString("lb_edit") + " " + bundle.getString("lb_user") +
                        " - " + bundle.getString("lb_projects");


    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");

%>
<HTML>
<!-- This JSP is envoy/administration/users/hiddenProjects.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "projects";
    var helpFile = "<%=bundle.getString("help_user_projects")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       projectForm.action = "<%=cancelUrl%>";
       projectForm.submit();
    }
}


</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
    <p>
<form name="projectForm" method="post">
    <div class="standarText">
    <%= bundle.getString("lb_projects") %>: &nbsp;
    <span class="confidential">[<%=bundle.getString("lb_confidential")%>]</span>
    </div>
    <p>
          <input type="button" name="<%=cancelButton %>" value="<%=cancelButton %>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=doneButton %>" value="<%=doneButton %>"
            onclick="submitForm('cancel')">
</form>
</BODY>
</HTML>
