<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.vendormanagement.VendorInfo,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorComparator,
         com.globalsight.everest.foundation.User,
         java.text.MessageFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="details" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ratings" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="vendors" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();
    String detailsUrl = details.getPageURL()+"&action=details";
    String newUrl = new1.getPageURL()+"&action=new";
    String editUrl = new1.getPageURL()+"&action=edit";
    String removeUrl = remove.getPageURL()+"&action=remove";
    String ratingsUrl = ratings.getPageURL()+"&action=ratings";
    String searchUrl = search.getPageURL()+"&action=search";

    String title= bundle.getString("lb_vendors");

    String confidential = "[" + bundle.getString("lb_confidential") + "]";

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String detailsButton = bundle.getString("lb_details");
    String removeButton = bundle.getString("lb_remove");
    String ratingsButton = bundle.getString("lb_ratings");
    String searchButton = bundle.getString("lb_search");
    ArrayList securities = (ArrayList)sessionMgr.getAttribute("securities");
%>

<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "vendors";
    var helpFile = "<%=bundle.getString("help_vendors")%>";

function enableButtons()
{
    if (VendorForm.removeBtn)
        VendorForm.removeBtn.disabled = false;
    if (VendorForm.ratingsBtn)
        VendorForm.ratingsBtn.disabled = false;
    if (VendorForm.editBtn)
        VendorForm.editBtn.disabled = false;
    if (VendorForm.detailsBtn)
        VendorForm.detailsBtn.disabled = false;
}

function submitForm(selectedButton)
{
    var checked = false;
    var vendorId = null;
    var userId = null;
    if (VendorForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (VendorForm.radioBtn.length)
        {
            for (i = 0; !checked && i < VendorForm.radioBtn.length; i++)
            {
                if (VendorForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selected = VendorForm.radioBtn[i].value;
                    idx = selected.indexOf(":");
                    vendorId = selected.substring(0, idx);
                    userId = selected.substring(idx+1, selected.length);
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (VendorForm.radioBtn.checked == true)
            {
                checked = true;
                selected = VendorForm.radioBtn.value;
                idx = selected.indexOf(":");
                vendorId = selected.substring(0, idx);
                userId = selected.substring(idx+1, selected.length);
            }
        }
    }
    // otherwise do the following
    if (selectedButton == 'New')
    {
        VendorForm.action = "<%=newUrl%>";
        VendorForm.submit();
        return;
    }
    else if (selectedButton == "Search")
    {
        VendorForm.action = "<%=searchUrl%>";
        VendorForm.submit();
        return;
    }
    else if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_vendor") %>");
        return false;
    }
    if (selectedButton == "Details")
    {
        VendorForm.action = "<%=detailsUrl%>&id=" + vendorId;
    }
    else if (selectedButton == "Edit")
    {
        VendorForm.action = "<%=editUrl%>&id=" + vendorId;
    }
    else if (selectedButton == "Remove")
    {
        if (userId != "null")
        {
            if (confirm("<%=bundle.getString("msg_confirm_vendor_removal2")%>"))
            {
                VendorForm.action = "<%=removeUrl%>&id=" + vendorId;
            }
            else
            {
                return;
            }
        }
        else
        {
            if (confirm("<%=bundle.getString("msg_confirm_vendor_removal")%>"))
            {
                VendorForm.action = "<%=removeUrl%>&id=" + vendorId;
            }
            else
            {
                return;
            }
        }
    }
    else if (selectedButton == "Ratings")
    {
        VendorForm.action = "<%=ratingsUrl%>&id=" + vendorId;
    }

    VendorForm.submit();
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading">
    <%=title%>
    </span>
<form name="VendorForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="vendors" key="<%=VendorConstants.VENDOR_KEY%>"
                 pageUrl="self" />
          </td>
        <tr>
          <td>
  <% int i = 0; %>
  <amb:table bean="vendors" id="vendor" key="<%=VendorConstants.VENDOR_KEY%>"
         dataClass="com.globalsight.everest.vendormanagement.VendorInfo" pageUrl="self"
         emptyTableMsg="msg_no_vendors" >
    <amb:column label="" width="20px">
        <input type="radio" name="radioBtn" onclick="enableButtons()"
         value="<%=vendor.getId() + ":" + vendor.getUserId()%>">
    </amb:column>
    <amb:column label="lb_name" sortBy="<%=VendorComparator.NAME%>">
    <%
        FieldSecurity security = (FieldSecurity)securities.get(i);
        String access1 = (String)security.get("firstName");
        String access2 = (String)security.get("lastName");
        String name;
        if (access1.equals("hidden") || access2.equals("hidden"))
            name = confidential;
        else 
            name = vendor.getFirstName() + " " + vendor.getLastName();
        out.print(name);
    %>
    </amb:column>
    <amb:column label="lb_alias" sortBy="<%=VendorComparator.ALIAS%>">
        <%=vendor.getPseudonym()%>
    </amb:column>
    <amb:column label="lb_company_name" sortBy="<%=VendorComparator.COMPANYNAME%>">
    <%
        FieldSecurity security = (FieldSecurity)securities.get(i++);
        String company = vendor.getCompanyName();
        String access = (String)security.get("companyName");
        if (access.equals("hidden"))
                company = confidential;
        out.print(company);
    %>
    </amb:column>
  </amb:table>

</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD align="right">
    <P>
    <INPUT TYPE="BUTTON" VALUE="<%=searchButton%>..." onClick="submitForm('Search');">
    <amb:permission name="<%=Permission.VENDORS_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" onClick="submitForm('Remove');"
        name="removeBtn" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.VENDORS_RATING_VIEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=ratingsButton%>..." onClick="submitForm('Ratings');"
        name="ratingsBtn" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.VENDORS_DETAILS%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=detailsButton%>..." onClick="submitForm('Details');"
        name="detailsBtn" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.VENDORS_EDIT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..." onClick="submitForm('Edit');"
        name="editBtn" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.VENDORS_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    </amb:permission>

</TD>
</TR>
</TABLE>
</form>
</body>
</html>
