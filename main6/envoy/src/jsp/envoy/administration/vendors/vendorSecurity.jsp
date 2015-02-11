<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.vendormanagement.Vendor,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorComparator,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
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
    String nextUrl = next.getPageURL()+"&action=next";
    String createUrl = create.getPageURL()+"&action=next";
    String prevUrl = prev.getPageURL()+"&action=prev";
    String cancelUrl = null;

    if (edit != null)
        cancelUrl = cancelEdit.getPageURL() + "&action=cancelEdit";
    else
        cancelUrl = cancelNew.getPageURL() + "&action=cancelNew";
    String title = null;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor") +
                 " - " + bundle.getString("lb_field_level_access");
    }
    else
    {
        title= bundle.getString("lb_new") + " " + bundle.getString("lb_vendor") +
                 " - " + bundle.getString("lb_field_level_access");
    }

    // Labels of the column titles
    String fieldCol = bundle.getString("lb_field");
    String accessCol = bundle.getString("lb_access");

    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String nextButton = bundle.getString("lb_next");
    String doneButton = bundle.getString("lb_done");
    String saveButton = bundle.getString("lb_save");
    String prevButton = bundle.getString("lb_previous");

    // Labels
    String lbShared = bundle.getString("lb_shared");
    String lbLocked = bundle.getString("lb_locked");
    String lbHidden = bundle.getString("lb_hidden");

    ArrayList fieldLabels = (ArrayList)request.getAttribute("labelList");
    ArrayList fields = (ArrayList)request.getAttribute("fieldList");
    ArrayList fieldValues = (ArrayList)request.getAttribute("fieldValueList");

    Vendor vendor = (Vendor)sessionMgr.getAttribute("vendor");
%>

<html>
<!-- This JSP is envoy/administration/vendors/vendorSecurity.jsp -->
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" src="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = true;
    var objectName = "";
    var guideNode = "vendors";
    var helpFile = "<%=bundle.getString("help_vendors")%>";

function submitForm(selectedButton)
{
    if (selectedButton == "Next")
    {
        VendorForm.action = "<%=nextUrl%>";
    }
    else if (selectedButton == "Save")
    {
        VendorForm.action = "<%=createUrl%>";
    }
    else if (selectedButton == "Done")
    {
        VendorForm.action = "<%=doneUrl%>";
    }
    else if (selectedButton == "Cancel")
    {
        VendorForm.action = "<%=cancelUrl%>";
    }
    else if (selectedButton == "Prev")
    {
        VendorForm.action = "<%=prevUrl%>";
    }
    VendorForm.submit();
}

function markFields()
{
    obj = VendorForm.access;
    value = obj.options[obj.selectedIndex].value;
    if (value == "shared")
    {
<%
        for (int i=0; i < fields.size(); i++)
        {
            String field = (String)fields.get(i);
%>
            VendorForm.<%=field%>[0].checked = true;
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
            VendorForm.<%=field%>[1].checked = true;
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
            VendorForm.<%=field%>[2].checked = true;
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
<br>
<span class="mainHeading"> <%=title%> </span>
<p>
<span class="standardText" style="width:75%"><%=bundle.getString("msg_security_on_fields")%></span>
<p>

    <table cellpadding=0 cellspacing=0 border=0 bordercolor="red" class="standardText">
        <tr>
          <td>
<form name="VendorForm" method="post">
<!-- data table -->
<table border=0 bordercolor="green">
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
            if (value.equals("shared"))
                out.print("checked");
            out.print(">" + lbShared);
            out.println("<input type='radio' name='" + field + "' value='locked' ");
            if (value.equals("locked"))
                out.print("checked");
            out.print(">" + lbLocked);
            out.println("<input type='radio' name='" + field + "' value='hidden' ");
            if (value.equals("hidden"))
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
    <% if (vendor.getUserId() != null) { %>
        <INPUT TYPE="BUTTON" VALUE="<%=nextButton%>" onClick="submitForm('Next');">
    <% } else { %>
        <INPUT TYPE="BUTTON" VALUE="<%=nextButton%>" onClick="submitForm('Save');">
    <% } %>
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
