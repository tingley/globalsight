<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.securitymgr.VendorSecureFields,
         com.globalsight.everest.util.comparator.VendorRoleComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.vendormanagement.Vendor,
         com.globalsight.everest.vendormanagement.VendorRole,
         java.text.MessageFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true"
%>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="roles" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String newUrl = new1.getPageURL() + "&action=new";
    String editUrl = edit.getPageURL()+ "&action=edit";
    String doneUrl = done.getPageURL() + "&action=doneRoles";
    String cancelUrl = cancel.getPageURL() + "&action=cancelRoles";
    String selfUrl = self.getPageURL() + "&action=self";

    String lbRoles = bundle.getString("lb_roles");;
    String title= bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor") + " - " + lbRoles;

    // Labels of the column titles
    String sourceCol = bundle.getString("lb_source_locale");
    String targetCol = bundle.getString("lb_target_locale");
    String activityCol = bundle.getString("lb_activity");

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");

    // Field level security
    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.ROLES);

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_user")%>";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_vendors_roles_list")%>";

function submitForm(selectedButton)
{
    if (selectedButton == 'New')
    {
        RolesForm.action = "<%=newUrl %>";
        RolesForm.submit();
        return;
    }
    if (selectedButton == 'Cancel')
    {
        RolesForm.action = "<%=cancelUrl %>";
        RolesForm.submit();
        return;
    }

    var checked = false;
    var selectedRadioBtn = null;
    if (RolesForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (RolesForm.radioBtn.length)
        {
            for (i = 0; !checked && i < RolesForm.radioBtn.length; i++)
            {
                if (RolesForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = RolesForm.radioBtn[i].value;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (RolesForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = RolesForm.radioBtn.value;
            }
        }
    }
    // otherwise do the following
    if (selectedButton == 'Done')
    {
        RolesForm.action = "<%=doneUrl %>";
        RolesForm.submit();
        return;
    }
    else if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_role") %>");
        return false;
    }

    if (selectedButton == 'Edit')
    {
        RolesForm.action = "<%=editUrl %>";
        values = selectedRadioBtn.split(",");
        RolesForm.sourceLocale.value = values[0];
        RolesForm.targetLocale.value = values[1];
    }
    RolesForm.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <SPAN CLASS="mainHeading">
    <%=title%>
    </SPAN>
<form name="RolesForm" method="post">
  <input type="hidden" name="sourceLocale">
  <input type="hidden" name="targetLocale">
    <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
      <TR VALIGN="TOP">
        <TD ALIGN="RIGHT">
<%
    if (access.equals("hidden"))
    {
%>
        </td><tr><td>&nbsp;
        <div class="standardText">
        <%= bundle.getString("lb_roles") %>: <span class="confidential">[<%=bundle.getString("lb_confidential")%>]
</span> <p>
        </div>
<%
    }
    else
    {
%>
          <amb:tableNav bean="roles" key="<%=VendorConstants.ROLE_KEY%>" pageUrl="self" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="roles" id="role" key="<%=VendorConstants.ROLE_KEY%>"
             dataClass="com.globalsight.everest.vendormanagement.VendorRole" pageUrl="self"
             emptyTableMsg="msg_no_roles" >
            <amb:column label="" >
                <%
                    LocalePair lp = role.getLocalePair();
                    String value = lp.getSource() + "," + lp.getTarget();
                    out.print("<input type=\"radio\" name=\"radioBtn\" value=\""
                              + value + "\">");
                %>
            </amb:column>
            <amb:column label="lb_source_locale" sortBy="<%=VendorRoleComparator.SRC%>">
                <%
                    LocalePair lp = role.getLocalePair();
                    out.print(lp.getSource().getDisplayName());
                %>
            </amb:column>
            <amb:column label="lb_target_locale" sortBy="<%=VendorRoleComparator.TARG%>">
                <%
                    LocalePair lp = role.getLocalePair();
                    out.print(lp.getTarget().getDisplayName());
                %>
            </amb:column>
            <amb:column label="lb_activity" sortBy="<%=VendorRoleComparator.ACTIVITY%>">
                <%
                    out.print(role.getActivity().getActivityName());
                %>
            </amb:column>
          </amb:table>
        </td>
      </tr>

<% } //end if access %>
<!-- End Data Table -->
</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD align="right">
    <P>

    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
<% if (access.equals("shared")) { %>
    <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..." onClick="submitForm('Edit');">
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    <INPUT TYPE="BUTTON" VALUE="<%=doneButton%>" onClick="submitForm('Done');">
<% } %>

</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>
