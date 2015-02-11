<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.everest.jobhandler.Job,
                  java.util.ResourceBundle,
                  java.util.List"
          session="true"
%>
<jsp:useBean id="next" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="projectList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="targetLocaleList" scope="request" class="java.util.ArrayList" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
    	uiLocale = Locale.ENGLISH;
    }
    // Field names
    String creationStart = JobSearchConstants.CREATION_START;
    String creationEnd = JobSearchConstants.CREATION_END;

    String nextUrl = next.getPageURL() + "&action=" + WebAppConstants.ACTION_JOB_RANGE;
%>

<html>
<!-- This is customizeReportsJobRangeParam.jsp -->
<head>
<title><%=bundle.getString("lb_customize_reports_params_title")%></title>
</head>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY">
<SCRIPT LANGUAGE="JAVASCRIPT">
$(document).ready(function(){
	$("#csf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#cef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#cef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#csf").datepicker( "option", "maxDate", selectedDate );
		}
	});
});
function submitForm()
{
    searchForm.action="<%=nextUrl%>";
    searchForm.submit();
}

</script>

<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("lb_customize_reports")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="jobId" MULTIPLE size="6" style="width:300px">
            <option value="*" SELECTED><B>&lt;<%=bundle.getString("lb_all")%>&gt;</B></OPTION>
<%
            ReportJobInfo job = null;
            for (Iterator iter = jobList.iterator(); iter.hasNext();)
            {
                job = (ReportJobInfo) iter.next();
 %>             <option title="<%=job.getJobName()%>" VALUE="<%=job.getJobId()%>"><%=job.getJobName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_project")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="projectId" MULTIPLE size=4>
            <option VALUE="*" SELECTED>&lt;<%=bundle.getString("lb_all")%>&gt;</OPTION>
<%
            Project project = null;
            for (Iterator iter = projectList.iterator(); iter.hasNext();)
            {
                project = (Project) iter.next();
%>          <option VALUE="<%=project.getId()%>"><%=project.getName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_status")%><span class="asterisk">*</span>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="status" MULTIPLE size=4>
            <option value="*" SELECTED>&lt;<%=bundle.getString("lb_all")%>&gt;</OPTION>
            <option value="<%=Job.READY_TO_BE_DISPATCHED%>"><%= bundle.getString("lb_ready") %></option>
            <option value="<%=Job.DISPATCHED%>"><%= bundle.getString("lb_inprogress") %></option>
            <option value="<%=Job.LOCALIZED%>"><%= bundle.getString("lb_localized") %></option>
            <option value="<%=Job.EXPORTED%>"><%= bundle.getString("lb_exported") %></option>
            <option value="<%=Job.ARCHIVED%>"><%= bundle.getString("lb_archived") %></option>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_locales")%><span class="asterisk">*</span>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLocale" MULTIPLE size=4>
            <option VALUE="*" SELECTED>&lt;<%=bundle.getString("lb_all")%>&gt;</OPTION>
<%          GlobalSightLocale targetLocale = null;
            for (Iterator iter = targetLocaleList.iterator(); iter.hasNext();)
            {
                targetLocale = (GlobalSightLocale) iter.next();
%>          <option VALUE="<%=targetLocale.getId()%>"><%=targetLocale.getDisplayName(uiLocale)%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText" colspan=2><%=bundle.getString("lb_creation_date_range")%>:</td>
    </tr>
    <tr>
        <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
            <%=bundle.getString("lb_starts")%>:
            <input type="text" id="csf" name="<%=creationStart%>" >
            <%=bundle.getString("lb_ends")%>:
            <input type="text" id="cef" name="<%=creationEnd%>" >
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("date_display_format")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="dateFormat">
<%
            String dateFormats[] = new String[4];
            int i=0;
            dateFormats[i++] = "MM/dd/yy hh:mm:ss a z";
            dateFormats[i++] = "MM/dd/yy HH:mm:ss z";
            dateFormats[i++] = "yyyy/MM/dd HH:mm:ss z";
            dateFormats[i++] = "yyyy/MM/dd hh:mm:ss a z";
            for (i=0;i<dateFormats.length;i++) {
 %>
            <OPTION VALUE="<%=dateFormats[i]%>"><%=dateFormats[i]%></OPTION>
<%         }
%>
        </select>
        </td>
    </tr>
    <tr>
        <td><input type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></td>
        <TD><INPUT type="BUTTON" VALUE=<%=bundle.getString("lb_next")%> onClick="submitForm()"></TD>
    </tr>
</table>
</form>
</body>
</html>
