<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.util.SortUtil,
                  com.globalsight.everest.company.CompanyWrapper,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  com.globalsight.everest.company.CompanyThreadLocal,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfoComparator,
                  com.globalsight.everest.usermgr.UserLdapHelper,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.jobhandler.JobImpl,
                  com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                  com.globalsight.everest.workflowmanager.Workflow"
          session="true"
%>
<%  
    String EMEA = CompanyWrapper.getCurrentCompanyName();
    //Multi-Company: get current user's company from the session  

    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

    List<ReportJobInfo> reportJobInfoList = 
            (List<ReportJobInfo>) request.getAttribute(ReportConstants.REPORTJOBINFO_LIST);
    ArrayList<GlobalSightLocale> targetLocales =
            (ArrayList<GlobalSightLocale>) request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
    List<Project> projectList =
            (ArrayList<Project>) request.getAttribute(ReportConstants.PROJECT_LIST);

    // Field names
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;
    String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS";
    String formAction = basicAction + "&action=" + ReportConstants.GENERATE_REPORTS;
%>
<html>
<!--  This JSP is: /envoy/administration/reports/fileListReportWebForm.jsp-->
<head>
<title><%= EMEA%> <%=bundle.getString("file_list_report_web_form")%></title>
<script type="text/javascript" src="/globalsight/envoy/administration/reports/report.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT LANGUAGE="JAVASCRIPT">
// If user selected "now", then blank out the preceeding numeric field.
function checkNow(field, text)
{
    if (field.options[1].selected)
        text.value = "";
}

function isInteger(value)
{
    if (value == "") return true;
    return (parseInt(value) == value);
}

function validateForm()
{
    if(searchForm.reportOnJobId.checked)
    {
    	
        var jobIDArr =  searchForm.jobIds.value.split(",");
        var idInput=$("#jobNameList").find("option");
		var idArray=new Array();
		idInput.each(function(){
			idArray.push({"jobId":$(this).val()});
		})
        if(!validateIDS(jobIDArr, idArray))
        {
        	$("#jobNameList").attr("selected", true);
           return ('<%=bundle.getString("lb_invalid_jobid")%>');
        }
    }
    if(searchForm.reportOnJobName.checked){
	    var len=$("#jobNameList").find("option:selected").length;
	    if(len==0){
	    	var ops=$("#jobNameList").children();
	    	if(ops.length==0){
	    		return ('<%=bundle.getString("msg_invalid_jobName")%>');
	    	}else{
	    		ops.attr("selected", true);
	    	}
	    	
	    }
	}
    return "";
}

// Check the status before close the page.
function doClose()
{
	var jobIDArr = new Array();
	
	$.ajax({
		type: 'POST',
		url:  '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
		data: {'inputJobIDS' : jobIDArr.toString(),
			   'reportType'  : $("input[name='reportType']").val()},
		success: function(data) {
					if(data != null && data.status == "inProgress")
					{
						if(!confirm('<%=bundle.getString("msg_cancel_report")%>'))
						{
							return;
						}
						else
						{
							$.getJSON("<%=basicAction + "&action=" + ReportConstants.ACTION_CANCEL_REPORTS%>", 
									{"inputJobIDS":jobIDArr.toString(), "reportType":$("input[name='reportType']").val()},
									function(data) {});
							window.close();
						}
					}
			
					window.close();
    			 },
		dataType: 'json'
	});

}

function submitForm()
{
   var msg = validateForm();
   if (msg != "")
   {
    alert(msg);
    return;
   }
   else
   {
	// Submit the Form, if possible(No report is generating.)
	$.ajax({
			type: 'POST',
	   		url:  '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
	   		data: {'reportType'  : $("input[name='reportType']").val()},
	   		success: function(data) {
	   					if(data == null || data.status != "inProgress")
	   					{
	   						$("form[name='searchForm']").submit();
	   					}
	   	    		 },
	   		dataType: 'json'
	});
   }
}

function filterJob()
{
   if(searchForm.reportOnJobId.checked)
   {
       return;   
   }
   searchForm.jobNameList.options.length=0;
   
   //selected job status
   var currSelectValueJobStatus = new Array();
   for(i=0;i<searchForm.jobStatus.length;i++)
   {
      var op= searchForm.jobStatus.options[i];
      if(op.selected)
      {
          currSelectValueJobStatus.push(op.value);
      }
   } 
   
   //selected target locales
   var currSelectValueTargetLocale = new Array();
   for(i=0;i<searchForm.targetLocalesList.length;i++)
   {
      var op= searchForm.targetLocalesList.options[i];
      if(op.selected)
      {
          currSelectValueTargetLocale.push(op.value);
      }
   }
   
   //selected project
   var currSelectValueProject = new Array();
   for(i=0;i<searchForm.projectNameList.length;i++)
   {
      var op= searchForm.projectNameList.options[i];
      if(op.selected)
      {
          currSelectValueProject.push(op.value);
      }
   }
   
   <%
     Iterator<ReportJobInfo> it = reportJobInfoList.iterator();
     while (it.hasNext())
     {
         ReportJobInfo j = it.next();
          %>
          if(contains(currSelectValueProject, "<%=j.getProjectId()%>"))
          {
            if(contains(currSelectValueJobStatus, "<%=j.getJobState()%>"))
            {
               var isLocaleFlag = "false";
               <%
               List<String> jobLocales = j.getTargetLocales();
               for(int i=0;i<jobLocales.size();i++)
               {
                   String locale = jobLocales.get(i);
                   %>
                   if(contains(currSelectValueTargetLocale,"<%=locale%>"))
                   {
                      isLocaleFlag = "true";
                   }
                   <%
                }
                %>
                if(isLocaleFlag=="true")
                {
                   var varItem = new Option("<%=j.getJobName()%>", "<%=j.getJobId()%>");
                   varItem.setAttribute("title","<%=j.getJobName()%>");
                   searchForm.jobNameList.options.add(varItem);
                }
           }
         }
   <%
     }
   %>
   if(searchForm.jobNameList.options.length==0)
   {
     searchForm.submitButton.disabled=true;
   }
   else
   {
     searchForm.submitButton.disabled=false;
   }
}

// Select JobIds or Job Name. 
function setDisableTRWrapper(trid)
{
    if(trid == "idTRJobIds")
    {
        filterJob();
        setDisableTR("idTRJobIds", true);
        setDisableTR("idTRJobNames", false);
        setDisableTR("idTRProject", false);
        setDisableTR("idTRJobStatus", false);
    }
    else if(trid == "idTRJobNames")
    {
        searchForm.submitButton.disabled=false;
        setDisableTR("idTRJobIds", false);
        setDisableTR("idTRJobNames", true);
        setDisableTR("idTRProject", true);
        setDisableTR("idTRJobStatus", true);
    }
}

function doOnload()
{
    // Set the jobIds as default check. 
    setDisableTRWrapper("idTRJobNames");
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY" onLoad="doOnload()">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
    <TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>

<span class="mainHeading"><B><%= EMEA%> <%=bundle.getString("file_list_report_web_form")%></B></span><BR><BR>
<TABLE WIDTH="85%">
    <TR><TD><SPAN CLASS="smallText">
        <%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN>
    </TD></TR>
</TABLE>

<form name="searchForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.DETAILED_WORDCOUNTS_REPORT%>">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<tr>
    <td class="standardText"><%=bundle.getString("lb_report_on")%></td>
    <td class="standardText" VALIGN="BOTTOM">
        <table cellspacing=0>
            <tr id="idTRJobIds">
                <td><input type="radio" id="reportOnJobId" name="reportOn" checked onclick="setDisableTRWrapper('idTRJobNames');" value="jobIds"/><%=bundle.getString("lb_job_ids")%></td>
                <td><input type="text" id="jobIds" name="jobIds" value=""><%=bundle.getString("lb_job_ids_description")%></td>
            </tr>
            <tr id="idTRJobNames">
                <td><input type="radio" id="reportOnJobName" name="reportOn" onclick="setDisableTRWrapper('idTRJobIds');" value="jobNames"/><%=bundle.getString("lb_job_name")%>:</td>
                <td>
                <select id = "jobNameList" name="jobNameList" MULTIPLE size="6" style="width:300px;min-height:90px;" disabled>
<%
                 Iterator<ReportJobInfo> iterator = reportJobInfoList.iterator();
                 while(iterator.hasNext())
                 {
                     ReportJobInfo j = iterator.next();
%>
                <option title="<%=j.getJobName()%>" VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></OPTION>
<%
                 }
%>
                </select>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr id="idTRProject">
    <td class="standardText"><%=bundle.getString("lb_project")%>:</td>
    <td class="standardText" VALIGN="BOTTOM">
    <select id="projectNameList" name="projectNameList" MULTIPLE size=4 onChange="filterJob()" disabled>
        <option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
        Iterator iterProject = projectList.iterator();
        while (iterProject.hasNext())
        {
            Project p = (Project) iterProject.next();
%>
        <option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%
        }
%>
    </select>
    </td>
</tr>

<tr id="idTRJobStatus">
    <td class="standardText"><%=bundle.getString("lb_job_status")%>:</td>
    <td class="standardText" VALIGN="BOTTOM">
        <select id="jobStatus" name="jobStatus" multiple="true" size=4 onChange="filterJob()" disabled>
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option VALUE="<%=Job.READY_TO_BE_DISPATCHED%>"><%=bundle.getString("lb_ready")%></OPTION>
            <option VALUE="<%=Job.DISPATCHED%>"><%=bundle.getString("lb_inprogress")%></OPTION>
            <option VALUE="<%=Job.LOCALIZED%>"><%=bundle.getString("lb_localized")%></OPTION>
            <option VALUE="<%=Job.EXPORTED%>"><%=bundle.getString("lb_exported")%></OPTION>
            <option VALUE="<%=Job.EXPORT_FAIL%>"><%=bundle.getString("lb_exported_failed")%></OPTION>
            <option VALUE="<%=Job.ARCHIVED%>"><%=bundle.getString("lb_archived")%></OPTION>
        </select>
</tr>

<tr>
    <td class="standardText"><%=bundle.getString("lb_target_locales")%>:</td>
    <td class="standardText" VALIGN="BOTTOM">
        <select id="targetLocalesList" name="targetLocalesList" multiple="true" size=4 onChange="filterJob()">
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            SortUtil.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
            Iterator iterLocale = targetLocales.iterator();
            while(iterLocale.hasNext())
            {
                GlobalSightLocale gsLocale = (GlobalSightLocale) iterLocale.next();
%>
                <option VALUE="<%=gsLocale.getId()%>"><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
<%
            }
%>
        </select>
    </td>
</tr>

<!-- Canceled Date Range Option, GBS-2259-->
<tr style="display:none">
    <td class="standardText" colspan=2><%=bundle.getString("lb_creation_date_range")%>:</td>
</tr>
<tr style="display:none">
    <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
        <%=bundle.getString("lb_starts")%>:
        <input type="text" name="<%=creationStart%>" size="3" maxlength="9">
        <select name="<%=creationStartOptions%>">
            <option value='-1'></option>
            <option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
            <option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
            <option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
            <option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
        </select>
        <%=bundle.getString("lb_ends")%>:
        <input type="text" name="<%=creationEnd%>" size="3" maxlength="9">
        <select name="<%=creationEndOptions%>" onChange="checkNow(this, searchForm.<%=creationEnd%>)">
            <option value='-1'></option>
            <option value='<%=SearchCriteriaParameters.NOW%>'><%=bundle.getString("lb_now")%></option>
            <option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
            <option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
            <option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
            <option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
        </select>
    </td>
</tr>
<!-- Finished Date Range Option-->
 
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
<%}%>
    </select>
    </td>
</tr>

<tr>
    <td><%=bundle.getString("lb_export_as")%></td>
    <td>
        <input type="radio" name="exportFormat" value="xlsx" checked>XLSX<br>
        <input type="radio" name="exportFormat" value="csv">CSV
    </td>
</tr>
<tr>
    <td>Include MT column:</td>
    <td><input id="includeMtColumn" name="includeMtColumn" type="checkbox"></td>
</tr>

<tr>
	<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" 
		id="submitButton" name="submitButton" onClick="submitForm()"></TD>
	<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" 
		onClick="doClose();"></TD>
</tr>
</table>
</form>
<BODY>
</HTML>
