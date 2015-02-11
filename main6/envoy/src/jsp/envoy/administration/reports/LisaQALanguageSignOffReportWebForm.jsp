<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.workflowmanager.Workflow,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.util.comparator.JobComparator,
                  com.globalsight.everest.util.comparator.LocaleComparator,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.util.GlobalSightLocale,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
        uiLocale = Locale.ENGLISH;
    }
    
    Vector stateList = new Vector();
    stateList.add(Job.DISPATCHED);
    stateList.add(Job.LOCALIZED);
    stateList.add(Job.EXPORTED);
    // get jobs by a state list
    Collection jobs = ServerProxy.getJobHandler().getJobsByStateList(stateList);
    List jobList = null;
    if (jobs != null && !jobs.isEmpty())
    {
        jobList = new ArrayList(jobs);
        Collections.sort(jobList, new JobComparator(JobComparator.NAME,uiLocale));
    }
    String[] allJobIds = new String[jobList.size()];
    String[] allJobValues = new String[jobList.size()];
    Iterator iterator = jobList.iterator();
    int index = 0; 
    while(iterator.hasNext()){
        Job job = (Job)iterator.next();
        allJobIds[index] = (job.getId())+"";
        allJobValues[index] = job.getJobName();
        index ++;
    }
    ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
<!-- This JSP is: /envoy/administration/reports/LisaQALanguageSignOffReportWebForm.jsp-->
<head>
<title><%=bundle.getString("review_reviewers_comments")%></title>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<script type="text/javascript">
<!--

function getKeyUpValue(jobName,eve){
    var jobValue = jobName.value;
    var allJobs = new Array();
    var allIds = new Array();
    <%
    for(int i = 0; i < allJobValues.length; i ++){
    %>
        
        allJobs[<%=i%>] = "<%=allJobValues[i]%>";
        allIds[<%=i%>] = "<%=allJobIds[i]%>";
    <%
    }
    %>
    
    var jobValues = new Array();
    var jobIds = new Array();
    for(var i = 0; i < allJobs.length; i ++){
            jobValues[jobValues.length] = allJobs[i];
            jobIds[jobIds.length] = allIds[i];
    }
    renderJobList(jobValues,jobIds,jobValue);
}   

function renderJobList(/*array*/jobValues,/*array*/jobIds,/*string*/jobValue){
    var inputLength = jobValue.length;
    var str = "<select id='jobId' name='jobId'>";
    for(var i = 0; i < jobValues.length; i ++){
        if(jobValues[i].substr(0,inputLength) == jobValue){
            str = str.concat("<option value='"+jobIds[i]+"'>"+jobValues[i]+"</option>");
        }
    }
    if(str.indexOf("</option>")==-1){
        //No match option
        str = str.concat("<option VALUE='*'>NO JOB</option>")
    }
    str = str.concat("</select>");
    
    document.getElementById("jobList").innerHTML = str;
}
-->
</script>
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("reviewers_comments_report_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_select_a_job")%></SPAN>
</TD></TR></TABLE>

<form name="lisaQAForm" method="post" action="/globalsight/envoy/administration/reports/LisaQALanguageSignOffReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<tr>
<td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
<td id = "jobList" class="standardText" VALIGN="BOTTOM">
<select id="jobId" name="jobId">
<%  
    if (jobList == null)
    {
%>
    <option VALUE="*"><%=bundle.getString("no_job")%></option>
<%  
    }
    else
    {
        Iterator iter = jobList.iterator();
        while (iter.hasNext())
        {
            Job j = (Job) iter.next();
%>
    <option VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></option>
<%
         }
    }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_target_language")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="targetLang">
<%
    if (jobList == null)
    {
%>
    <option VALUE="*">NO JOB</option>
<%
    }
    else
    {
        Vector targetLocales = ServerProxy.getLocaleManager().getAllTargetLocales();
        int sortColumn = 1;
        LocaleComparator localeComparator = new LocaleComparator(sortColumn, uiLocale);
        Collections.sort(targetLocales, localeComparator);
        Iterator it = targetLocales.iterator();
        while (it.hasNext())
        {
            GlobalSightLocale gsl = (GlobalSightLocale) it.next();
            %><option VALUE="<%=gsl.getDisplayName()%>"><%=gsl.getDisplayName(uiLocale)%></option><%
        }
    }
%>
</select>
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
<%}%>
</select>
</td>
</tr>
<tr>
<td><input type="submit" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>"></td>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
</tr>
</table>
</form>
<BODY>
</HTML>