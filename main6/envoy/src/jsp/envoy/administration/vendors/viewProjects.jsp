<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.securitymgr.VendorSecureFields,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.projecthandler.Project,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.everest.vendormanagement.Vendor,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator,
         com.globalsight.everest.foundation.User,
         java.text.MessageFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="projects" scope="request"
 class="java.util.ArrayList" />


<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();

    // Field level security
    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.PROJECTS);

    // Labels of the column titles
    String lbProjects = bundle.getString("lb_projects");

%>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<table cellspacing="0" cellpadding="1" border="0" class="detailText" width="100%">
  <tr>
     <td bgcolor="D6CFB2">
        <table cellpadding=4 cellspacing=0 border=0
                    class=detailText bgcolor="WHITE" width="100%">
        <tr valign="TOP">
            <td style="background:D6CFB2; font-weight:bold; font-size:larger" colspan=2><%=lbProjects%></td>
        </tr>
        <tr>
            <td>
<%
    if (access.equals("hidden"))
    {
%>
        </td><tr><td>&nbsp;
        <div class="standardTextBold">
         <span class="confidential">[<%=bundle.getString("lb_confidential")%>]
         </span> <p>
        </div>
<%
    }
    else
    {
%>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
      <tr valign="top">
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
            <amb:column label="lb_name" sortBy="<%=ProjectComparator.NAME%>">
              <%=project.getName()%>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=ProjectComparator.DESC%>">
              <% out.print(project.getDescription() == null ? "" :
                     project.getDescription()); %>
            </amb:column>
          </amb:table>
        </td>
      </tr>
<%
    } // end if access
%>
    </table>
  </td>
 </tr>
</table>
</td>
</tr>
</table>
