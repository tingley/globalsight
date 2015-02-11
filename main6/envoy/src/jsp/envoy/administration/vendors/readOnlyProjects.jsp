<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.securitymgr.VendorSecureFields,
         com.globalsight.everest.usermgr.UserInfo,
         com.globalsight.everest.util.comparator.UserComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.projecthandler.Project,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.everest.vendormanagement.Vendor,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.List,
         java.util.Iterator,
         java.util.Locale,
         java.util.Set,
         java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="prev" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelEdit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancelNew" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="projects" scope="request" class="java.util.ArrayList" />


<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    boolean edit = true;
    if (sessionMgr.getAttribute("editUser") == null)
        edit = false;

    String nextUrl = next.getPageURL()+"&action=next";
    String doneUrl = done.getPageURL()+"&action=" + WebAppConstants.USER_ACTION_MODIFY_USER_PROJECTS;
    String prevUrl = prev.getPageURL()+"&action=previous";
    String cancelUrl = null;
    if (edit)
        cancelUrl = cancelEdit.getPageURL()+"&action=cancelEdit";
    else
        cancelUrl = cancelNew.getPageURL()+"&action=cancelNew";
    String selfUrl = self.getPageURL();


    String title;
    if (edit)
    {
        title =  bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor");
    } 
    else
    {
        title =  bundle.getString("lb_new") + " " + bundle.getString("lb_vendor");
    }
    title = title +  " - " + bundle.getString("lb_projects");


    // Button names
    String okButton = bundle.getString("lb_ok");
    String cancelButton = bundle.getString("lb_cancel");
    String prevButton = bundle.getString("lb_previous");
    String nextButton = bundle.getString("lb_next");
    String doneButton = bundle.getString("lb_done");


    // Field level security
    FieldSecurity hash = (FieldSecurity)sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.PROJECTS);
    if (access == null) access = "shared";

%>
<HTML>
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
    var helpFile = "<%=bundle.getString("help_vendors_projects")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=cancelUrl%>";
           projectForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "prev")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=prevUrl%>";
           projectForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "next")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=nextUrl%>";
           projectForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "saveUsers")
    {
        // From edit
        projectForm.action = "<%=doneUrl%>";
    }
    else
    {
        projectForm.action = formAction;
    }
    projectForm.submit();
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
<table border="0" bordercolor="green" cellpadding="0" cellspacing="0" >
<%
  if (access.equals("hidden")) {
%>
    <tr><td style="padding-right:10px"><%=bundle.getString("lb_projects")%>:</td>
    <td class="confidential">[<%=bundle.getString("lb_confidential")%>]</td>
    </tr>
<% } else {
%>
      <tr><td colspan="3"><%=bundle.getString("lb_project_default_list")%></td></tr>
        <tr>
        <td colspan=3>
        <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
           <tr Vvalign="top">
            <td align="right">
             <amb:tableNav bean="projects" key="<%=VendorConstants.PROJECT_KEY%>"
                    pageUrl="self" />
             </td>
           </tr>
           <tr>
              <td>
         <amb:table bean="projects" id="project" key="<%=VendorConstants.PROJECT_KEY%>"
            dataClass="com.globalsight.everest.projecthandler.Project" pageUrl="self"
            emptyTableMsg="msg_no_projects" >
           <amb:column label="lb_name" wrap="false" width="50px">
             <%=project.getName()%>
           </amb:column>
           <amb:column label="lb_project_manager" wrap="false" width="50px">
                <%
                    User pm = project.getProjectManager();
                    out.print(pm.getFirstName() + " " + pm.getLastName());
                %>
           </amb:column>
           <amb:column label="lb_description" width="250px">
             <% out.print(project.getDescription() == null ? "" :
                    project.getDescription()); %>
           </amb:column>
         </amb:table>
        </td>
      </tr>
<%
    } // end if access
%>
      <tr>
        <td colspan="3" style="padding-top:10px">
          <input type="button" name="<%=cancelButton %>" value="<%=cancelButton %>"
            onclick="submitForm('cancel')">
    <% if (!edit) { %>
          <input type="button" name="<%=prevButton %>" value="<%=prevButton %>"
            onclick="submitForm('prev')">
          <input type="button" name="<%=nextButton %>" value="<%=nextButton %>"
            onclick="submitForm('next')">
    <% } else { %>
          <input type="button" name="<%=doneButton %>" value="<%=doneButton %>"
            onclick="submitForm('saveUsers')">
    <% } %>
        </td>
      </tr>
</form>
</BODY>
</HTML>
