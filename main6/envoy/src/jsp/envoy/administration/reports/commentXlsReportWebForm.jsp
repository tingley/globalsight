<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.company.CompanyThreadLocal,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.everest.comment.IssueOptions,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null) uiLocale = Locale.ENGLISH;
    //Set current company id.
    String companyId = CompanyThreadLocal.getInstance().getValue();
    session.setAttribute("current_company_id", companyId);
    
    // Field names
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;

    List<ReportJobInfo> jobList = (ArrayList<ReportJobInfo>)
        request.getAttribute(ReportConstants.REPORTJOBINFO_LIST);
    List<Project> projectList = (ArrayList<Project>)
        request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
        request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
%>
<html>
<!-- This JSP is: /envoy/administration/reports/commentXlsReportWebForm.jsp-->
<head>
<title><%=bundle.getString("comments_report_web_form")%></title>
</head>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
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
    searchForm.submit();
}
</script>
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("segment_comments_retrieval_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="/globalsight/envoy/administration/reports/commentXlsReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=bundle.getString("job_name")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="jobId" MULTIPLE size="6" style="width:300px">
            <option value="*" SELECTED><B>&lt;<%=bundle.getString("all")%>&gt;</B></OPTION>
<%
            for (ReportJobInfo j : jobList)
            {
%>          <option title="<%=j.getJobName()%>" VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_project")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="projectId" MULTIPLE size="4">
            <option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%          for (Project p : projectList)
            {
%>          <option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_status")%><span class="asterisk">*</span>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="status" MULTIPLE size="4">
            <option value="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option value='<%=Job.READY_TO_BE_DISPATCHED%>'><%= bundle.getString("lb_ready") %></option>
            <option value='<%=Job.DISPATCHED%>'><%= bundle.getString("lb_inprogress") %></option>
            <option value='<%=Job.LOCALIZED%>'><%= bundle.getString("lb_localized") %></option>
            <option value='<%=Job.EXPORTED%>'><%= bundle.getString("lb_exported") %></option>
            <option value='<%=Job.EXPORT_FAIL%>'><%= bundle.getString("lb_exported_failed") %></option>
            <option value='<%=Job.ARCHIVED%>'><%= bundle.getString("lb_archived") %></option>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%= bundle.getString("lb_target_language") %><span class="asterisk">*</span>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLang" multiple="true" size="4">
            <OPTION value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%          for (GlobalSightLocale gsl : targetLocales)
            {
%>          <option VALUE="<%=gsl.getId()%>"><%=gsl.getDisplayName(uiLocale)%></OPTION><%
		    }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("comments_options")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
            <table border="0.5" cellspacing="2" cellpadding="2" class="standardText" >
                <tr>
                    <td>
                        <input type="checkbox" name="commentType_Job" value="<%=bundle.getString("lb_job")%>"> <%=bundle.getString("include_job_comments")%><br>
                        <input type="checkbox" name="commentType_Activity" value="<%=bundle.getString("lb_activity")%>"> <%=bundle.getString("include_activity_comments")%><br>
                        <input type="checkbox" name="commentPriority_On" value="on"> <%=bundle.getString("include_segment_priority")%><br>
                        <input type="checkbox" name="commentCategory_On" value="on"> <%=bundle.getString("include_segment_category")%><br>
<%                      List statusList = IssueOptions.getAllStatus();
                        for (int i = 0 ; i < statusList.size() ; i++)
                        {
                            String status = (String)statusList.get(i);
%>                      <input type="checkbox" name="commenStatus_<%=status%>" value="<%=status%>">
                        <%=bundle.getString("lb_include_segment_status")%> <%=bundle.getString("issue.status." + status)%><br>
<%                      }
%>
						<input type="checkbox" name="show_SourceAndTarget" value="on"> <%=bundle.getString("include_source_target")%><br>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

    <tr>
        <td class="standardText" colspan=2><%=bundle.getString("lb_creation_date_range")%>:</td>
    </tr>
    <tr>
        <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
            <%=bundle.getString("lb_starts")%>:
            <input type="text" id="csf" name="<%=creationStart%>">
            <%=bundle.getString("lb_ends")%>:
            <input type="text" id="cef" name="<%=creationEnd%>">
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
 %>        <OPTION VALUE="<%=dateFormats[i]%>"><%=dateFormats[i]%></OPTION>
<%      }
%>      </select>
        </td>
    </tr>
    <tr>
        <td><input type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
    </tr>
</table>
</form>
<BODY>
</HTML>
