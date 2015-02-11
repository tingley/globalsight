<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.vendormanagement.Rating,
         com.globalsight.everest.vendormanagement.Vendor,
         com.globalsight.everest.taskmanager.Task,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.RatingComparator,
         com.globalsight.everest.foundation.Timestamp,
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
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ratings" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(Timestamp.DATE, timeZone);
    ts.setLocale(uiLocale);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();
    String newUrl = new1.getPageURL()+"&action=new";
    String editUrl = edit.getPageURL()+"&action=edit";
    String removeUrl = remove.getPageURL()+"&action=remove";
    String cancelUrl = cancel.getPageURL()+"&action=cancel";

    // Labels of the column titles

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");
    String cancelButton = bundle.getString("lb_ok");

    // Data for the page
    Vendor vendor = (Vendor)sessionMgr.getAttribute(VendorConstants.VENDOR);

    String title= bundle.getString("lb_vendor") +  " " +
                  bundle.getString("lb_ratings") +  ": " +
                  vendor.getFirstName() + " " + vendor.getLastName();
    String average = (String)request.getAttribute("average_rating");


%>

<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<br>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "vendors";
    var helpFile = "<%=bundle.getString("help_vendors_ratings")%>";

function submitForm(selectedButton)
{
    if (selectedButton == 'New')
    {
        VendorForm.action = "<%=newUrl%>";
        VendorForm.submit();
        return;
    }
    else if (selectedButton == 'Cancel')
    {
        VendorForm.action = "<%=cancelUrl%>";
        VendorForm.submit();
        return;
    }
    var checked = false;
    var selectedRadioBtn = null;
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
                    selectedRadioBtn = VendorForm.radioBtn[i].value;
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
                selectedRadioBtn = VendorForm.radioBtn.value;
            }
        }
    }
    if (!checked)
    {
        alert("<%=bundle.getString("jsmsg_select_rating")%>");
        return false;
    } 
    if (selectedButton == 'Edit')
    {
        VendorForm.action = "<%=editUrl%>&rateId=" + selectedRadioBtn;
    }
    else if (selectedButton == 'Remove')
    {
        VendorForm.action = "<%=removeUrl%>&rateId=" + selectedRadioBtn;
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
    <span class="standardText">
    <% if (average != null) {
        out.println("<br>" + bundle.getString("lb_average_rating") + ": " + average);
    } %>
    </span>
    <p>
<form name="VendorForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="ratings" key="<%=VendorConstants.RATE_KEY%>"
                     pageUrl="self" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="ratings" id="rating" key="<%=VendorConstants.RATE_KEY%>"
             dataClass="com.globalsight.everest.vendormanagement.Rating" pageUrl="self"
             emptyTableMsg="msg_no_ratings" >
            <amb:column label="">
                <input type="radio" name="radioBtn" value="<%=rating.getId()%>">
            </amb:column>
            <amb:column label="lb_rating" sortBy="<%=RatingComparator.RATING%>">
                <%= rating.getValue() %>
            </amb:column>
            <amb:column label="lb_rater" sortBy="<%=RatingComparator.RATER%>">
                <%= rating.getRaterUserId() %>
            </amb:column>
            <amb:column label="lb_activity" sortBy="<%=RatingComparator.ACTIVITY%>">
                <%
                    Task task = rating.getTask();
                    if (task != null) out.print(task.getTaskName());
                %>
            </amb:column>
            <amb:column label="lb_job_name" sortBy="<%=RatingComparator.JOBNAME%>">
                <%
                    Task task = rating.getTask();
                    if (task != null) out.print(task.getJobName());
                %>
            </amb:column>
            <amb:column label="lb_job_id" sortBy="<%=RatingComparator.JOBID%>">
                <%
                    Task task = rating.getTask();
                    if (task != null) out.print(task.getJobId());
                %>
            </amb:column>
            <amb:column label="lb_source_locale" sortBy="<%=RatingComparator.SRCLOCALE%>">
                <%
                    Task task = rating.getTask();
                    if (task != null) out.print(task.getSourceLocale().getDisplayName(uiLocale));
                %>
            </amb:column>
            <amb:column label="lb_target_locale" sortBy="<%=RatingComparator.TARGLOCALE%>">
                <%
                    Task task = rating.getTask();
                    if (task != null) out.print(task.getTargetLocale().getDisplayName(uiLocale));
                %>
            </amb:column>
            <amb:column label="lb_date" sortBy="<%=RatingComparator.DATE%>">
                <%
                    ts.setDate(rating.getModifiedDate());
                    out.print(ts.toString());
                %>
            </amb:column>
            <amb:column label="lb_comment" sortBy="<%=RatingComparator.COMMENT%>">
                <% out.print(rating.getComment() == null ? "" : rating.getComment()); %>
            </amb:column>
          </amb:table>


</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD align="right">
    <P>
    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <amb:permission name="<%=Permission.VENDORS_RATING_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" onClick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.VENDORS_RATING_EDIT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..." onClick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.VENDORS_RATING_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    </amb:permission>

</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</html>
