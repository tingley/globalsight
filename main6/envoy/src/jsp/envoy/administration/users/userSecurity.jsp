<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,
         com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="request"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="create" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelNew" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelEdit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String edit = (String) sessionMgr.getAttribute("edit");
    String doneUrl = done.getPageURL()+"&action=doneSecurity";
    String createUrl = create.getPageURL()+"&action=" + WebAppConstants.USER_ACTION_CREATE_USER;
    String prevUrl = prev.getPageURL()+"&action=prev";
    String nextUrl = next.getPageURL()+"&action=next";
    String cancelUrl = null;

    if (edit != null)
        cancelUrl = cancelEdit.getPageURL() + "&action=cancelEdit";
    else
        cancelUrl = cancelNew.getPageURL() + "&action=cancelNew";
    String title = null;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " + bundle.getString("lb_user") +
                 " - " + bundle.getString("lb_field_level_access");
    }
    else
    {
        title= bundle.getString("lb_new") + " " + bundle.getString("lb_user") +
                 " - " + bundle.getString("lb_field_level_access");
    }
    String helperText = bundle.getString("msg_security_on_fields");

    // Labels of the column titles
    String fieldCol = bundle.getString("lb_field");
    String accessCol = bundle.getString("lb_access");

    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");
    String saveButton = bundle.getString("lb_save");
    String prevButton = bundle.getString("lb_previous");
    String nextButton = bundle.getString("lb_next");

    // Labels
    String lbShared = bundle.getString("lb_shared");
    String lbLocked = bundle.getString("lb_locked");
    String lbHidden = bundle.getString("lb_hidden");
    
    String lbUserName = bundle.getString("lb_user_name");

    ArrayList fieldLabels = (ArrayList)request.getAttribute("labelList");
    ArrayList fields = (ArrayList)request.getAttribute("fieldList");
    ArrayList fieldValues = (ArrayList)request.getAttribute("fieldValueList");
    
    String userName = "";
    if (edit != null)
    {
        ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(
          UserConstants.MODIFY_USER_WRAPPER);
        userName = wrapper.getUserName();
    } else
    {
        CreateUserWrapper wrapper = (CreateUserWrapper)sessionMgr.getAttribute(
          UserConstants.CREATE_USER_WRAPPER);
        userName = wrapper.getUserName();
    }
%>

<html>
<!-- This JSP is envoy/administration/users/userSecurity.jsp -->
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" src="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "users";
    var helpFile = "<%=bundle.getString("help_user_security")%>";

function submitForm(selectedButton)
{
    if (selectedButton == "Done")
    {
        UserForm.action = "<%=doneUrl%>";
    }
    else if (selectedButton == "Save")
    {
        UserForm.action = "<%=createUrl%>";
    }
    else if (selectedButton == "Cancel")
    {
        UserForm.action = "<%=cancelUrl%>";
    }
    else if (selectedButton == "Prev")
    {
        UserForm.action = "<%=prevUrl%>";
    }
    else if (selectedButton == "Next")
    {
        UserForm.action = "<%=nextUrl%>";
    }
    UserForm.submit();
}

function markFields()
{
    obj = UserForm.access;
    value = obj.options[obj.selectedIndex].value;
    if (value == "shared")
    {
<%
        for (int i=0; i < fields.size(); i++)
        {
            String field = (String)fields.get(i);
%>
            UserForm.<%=field%>[0].checked = true;
<%
        }
%>
    }
    else if (value == "locked")
    {
<%
        for (int i=0; i < fields.size(); i++)
        {
            String field = (String)fields.get(i);
%>
            UserForm.<%=field%>[1].checked = true;
<%
        }
%>
    }
    else if (value == "hidden")
    {
<%
        for (int i=0; i < fields.size(); i++)
        {
            String field = (String)fields.get(i);
%>
            UserForm.<%=field%>[2].checked = true;
<%
        }
%>
    }
}

</script>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

    <table cellpadding=0 cellspacing=0 border=0 bordercolor="red" class="standardText">
        <tr>
          <td>
<form name="UserForm" method="post">
<!-- data table -->
<table border=0 bordercolor="green">
    <tr>
    <td class="standardText" nowrap>
      <b><%= lbUserName %>:</b>&nbsp;<%= userName %>
    </td>
    <td class="standardText">
    </td>
    </tr>
    <tr>
    <td class="standardText">
    </td>
    <td class="standardText">
    </td>
    </tr>
    <tr>
        <td align="right" class="standardText">
            <%=bundle.getString("lb_mark_fields")%>&nbsp;
            <select name="access" onchange="javascript:markFields()">
                <option value="nochange">
                    
                </option>
                <option value="shared">
                    <%=bundle.getString("lb_shared")%>
                </option>
                <option value="locked">
                    <%=bundle.getString("lb_locked")%>
                </option>
                <option value = "hidden">
                    <%=bundle.getString("lb_hidden")%>
                </option>
            </select>
        </td>
    </tr>
  <tr>
    <td>
  <table border="0" cellspacing="0" cellpadding="5" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td nowrap width="30%">
        <%=fieldCol%>
      </td>
      <td style="padding-left:7px"><%=accessCol%>
      </td>
    </tr>
<%  for (int i=0; i < fields.size(); i++)
    {
        String field = (String)fields.get(i);
        String label = (String)fieldLabels.get(i);
        String value = (String)fieldValues.get(i);
        String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
%>
        <tr style="padding-bottom:5px; padding-top:5px;"
              valign=top bgcolor="<%=color%>">
          <td class="standardText" nowrap>
            <%=label%>
          </td>
          <td class="standardText">
<%
            out.println("<input type='radio' name='" + field + "' value='shared' ");
            if ("shared".equals(value))
                out.print("checked");
            out.print(">" + lbShared);
            out.println("<input type='radio' name='" + field + "' value='locked' ");
            if ("locked".equals(value))
                out.print("checked");
            out.print(">" + lbLocked);
            out.println("<input type='radio' name='" + field + "' value='hidden' ");
            if ("hidden".equals(value))
                out.print("checked");
            out.print(">" + lbHidden);
%>
          </td>
        </tr>
<%
    }
%>
  </table>
</td>
</tr>
<!-- End Data Table -->
</TD>
</TR>
</DIV>

<TR>
<TD>
    <P>
    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <% if (edit == null) { %>
    <INPUT TYPE="BUTTON" VALUE="<%=prevButton%>" onClick="submitForm('Prev');">
    <INPUT TYPE="BUTTON" VALUE="<%=nextButton%>" onClick="submitForm('Next');">
    <% } else { %>
    <INPUT TYPE="BUTTON" VALUE="<%=doneButton%>" onClick="submitForm('Done');">
    <% } %>
<p>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</html>
