<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.securitymgr.UserSecureFields,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         com.globalsight.everest.util.comparator.RoleComparator,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.foundation.Role,
         com.globalsight.everest.costing.Rate,
         com.globalsight.util.GlobalSightLocale,
         java.text.MessageFormat,
         java.util.Set,
         java.util.Iterator,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="roles" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String selfUrl = self.getPageURL();

    // Field level security
    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute("security");
    String access = (String)hash.get(UserSecureFields.ROLES);

    String lbRoles = bundle.getString("lb_roles");
%>
<!-- This JSP is envoy/administration/users/viewRoles.jsp -->
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <p>
<table cellspacing="0" cellpadding="1" border="0" borderColor="red" class="detailText" width="100%">
  <tr>
     <td bgcolor="D6CFB2">
        <table cellpadding=4 cellspacing=0 border=0
                    class=detailText bgcolor="WHITE" width="100%">
        <tr valign="TOP">
            <td style="background:D6CFB2; font-weight:bold; font-size:larger" colspan=2><%=lbRoles%></td>
        </tr>
        <tr>
            <td>
<%
    if (access.equals("hidden"))
    {
%>
        </td></tr><tr><td>&nbsp;
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
            <amb:tableNav bean="roles" key="<%=UserConstants.ROLE_KEY%>"
                     pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
          <amb:table bean="roles" id="role" key="<%=UserConstants.ROLE_KEY%>"
             dataClass="com.globalsight.everest.foundation.Role" pageUrl="self"
             emptyTableMsg="msg_no_roles" >
            <amb:column label="lb_activity" sortBy="<%=RoleComparator.ACTIVITY%>">
                <%=role.getActivity().getDisplayName()%>
            </amb:column>
            <amb:column label="lb_source_locale" sortBy="<%=RoleComparator.SRC%>">
                <%
                   String language = role.getSourceLocale().substring(0,2);
                   String country  = role.getSourceLocale().substring(3,5);
                   GlobalSightLocale srcLocale =
                               new GlobalSightLocale(language, country, false);
                    out.print(srcLocale.getDisplayName(uiLocale));
                %>
            </amb:column>
            <amb:column label="lb_target_locale" sortBy="<%=RoleComparator.TARG%>">
                <%
                   String language = role.getTargetLocale().substring(0,2);
                   String country  = role.getTargetLocale().substring(3,5);
                   GlobalSightLocale targLocale =
                               new GlobalSightLocale(language, country, false);
                    out.print(targLocale.getDisplayName(uiLocale));
                %>
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

