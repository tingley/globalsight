<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.vendormanagement.Rating,
         com.globalsight.everest.vendormanagement.Vendor,
         com.globalsight.everest.taskmanager.TaskImpl,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.RatingComparator,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.foundation.Timestamp,
         com.globalsight.everest.foundation.User,
         java.text.MessageFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="tasks" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(Timestamp.DATE, timeZone);
    ts.setLocale(uiLocale);
    String selfUrl = self.getPageURL();
    String rateUrl = rate.getPageURL()+"&action=rate";

    // Button names
    String rateButton = bundle.getString("lb_rating");
    String closeButton = bundle.getString("lb_close");

    // Data for the page

    String title= bundle.getString("lb_activities") +  "/" +
                  bundle.getString("lb_vendor") +  " " + bundle.getString("lb_ratings");


%>

<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<br>
<script language="JavaScript" SRC="/globalsight/includes/modalDialog.js"></script>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>

<script language="JavaScript">
function submitForm(selectedButton)
{
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
        alert("<%=bundle.getString("jsmsg_select_activity")%>");
        return false;
    } 
    if (selectedButton == 'Rate')
    {
        VendorForm.action = "<%=rateUrl%>&taskId=" + selectedRadioBtn;
    }
    VendorForm.submit();
}

</script>
</head>
<body leftmargin="5" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" >

    <span class="mainHeading">
    <%=title%>
    </span>
    <p>
    <%=bundle.getString("helper_text_vendor_task_rating")%>
    <p>
<form name="VendorForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="99%">
        <tr valign="top">
         <td align="right">
            <amb:tableNav bean="tasks" key="<%=VendorConstants.RATE_KEY%>"
                     pageUrl="self" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="tasks" id="task" key="<%=VendorConstants.RATE_KEY%>"
             dataClass="com.globalsight.everest.taskmanager.TaskImpl" pageUrl="self"
             emptyTableMsg="msg_no_completed_tasks" >
            <amb:column label="">
                <input type="radio" name="radioBtn" value="<%=task.getId()%>">
            </amb:column>
            <amb:column label="lb_rating" sortBy="<%=RatingComparator.RATING%>">
                <%
                    List ratings = task.getRatings();
                    if (ratings != null && ratings.size() > 0)
                    {
                        Rating rating = (Rating)ratings.get(0);
                        if (rating.getValue() != 0) out.print(rating.getValue());
                    }
                %>
            </amb:column>
            <amb:column label="lb_rater" sortBy="<%=RatingComparator.RATER%>">
                <%
                    List ratings = task.getRatings();
                    if (ratings != null && ratings.size() > 0)
                    {
                        Rating rating = (Rating)ratings.get(0);
                        if (rating.getRaterUserId() != null) out.print(rating.getRaterUserId());
                    }
                %>
            </amb:column>
            <amb:column label="lb_vendor" sortBy="<%=RatingComparator.RATER%>">
                <%= task.getAcceptor() %>
            </amb:column>
            <amb:column label="lb_activity" sortBy="<%=RatingComparator.ACTIVITY%>">
                <%= task.getTaskName() %>
            </amb:column>
            <amb:column label="lb_job_name" sortBy="<%=RatingComparator.JOBNAME%>">
                <%= task.getJobName() %>
            </amb:column>
            <amb:column label="lb_job_id" sortBy="<%=RatingComparator.JOBID%>">
                <%= task.getJobId() %>
            </amb:column>
            <amb:column label="lb_source_locale" sortBy="<%=RatingComparator.SRCLOCALE%>">
                <%= task.getSourceLocale().getDisplayName(uiLocale) %>
            </amb:column>
            <amb:column label="lb_target_locale" sortBy="<%=RatingComparator.TARGLOCALE%>">
                <%= task.getTargetLocale().getDisplayName(uiLocale) %>
            </amb:column>
            <amb:column label="lb_date" sortBy="<%=RatingComparator.DATE%>">
                <%
                    List ratings = task.getRatings();
                    if (ratings != null && ratings.size() > 0)
                    {
                        Rating rating = (Rating)ratings.get(0);
                        ts.setDate(rating.getModifiedDate());
                        out.print(ts.toString());
                    }
                %>
            </amb:column>
            <amb:column label="lb_comment" sortBy="<%=RatingComparator.COMMENT%>">
                <%
                    List ratings = task.getRatings();
                    if (ratings != null && ratings.size() > 0)
                    {
                        Rating rating = (Rating)ratings.get(0);
                        out.print(rating.getComment());
                    }
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
    <INPUT TYPE="BUTTON" VALUE="<%=closeButton%>" onClick="window.close();">
    <INPUT TYPE="BUTTON" VALUE="<%=rateButton%>..." onClick="submitForm('Rate');">

</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</html>
