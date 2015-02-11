<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
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
    stateList.add(Job.READY_TO_BE_DISPATCHED);
    stateList.add(Job.EXPORT_FAIL);
    stateList.add(Job.ARCHIVED);

    // get jobs by a state list
    Collection jobs = ServerProxy.getJobHandler().getJobsByStateList(stateList);
   	List jobList = null;
   	if (jobs != null && !jobs.isEmpty())
   	{
	   	jobList = new ArrayList(jobs);
	    Collections.sort(jobList, new JobComparator(JobComparator.NAME,uiLocale));
    }
   	ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
<!-- This JSP is: envoy\administration\reports\ImplementedCommentsCheckReportWebForm.jsp-->
<head>
<title><%=bundle.getString("implemented_comments_check_report_web_form")%></title>
<script language="javascript">
var jobIdArray = new Array();
var jobLocale = new Array();
var jobLocaleLable = new Array();

function displayLocale()
{
  var jobIdOption = document.getElementById("jobId");//var jobIdOption = document.all.jobId;
  var jobId = jobIdOption.options[jobIdOption.selectedIndex].value;
  var languageOption = document.getElementById("targetLang");//var languageOption = document.all.targetLang;

  while(languageOption.length > 1)
  {
        languageOption.remove(1);
  }

  for(var n = 0; n < jobIdArray.length; n++)
  {
       if(jobIdArray[n] == jobId)
       {
           var localeArr = jobLocale[n];
           var localeLableArr = jobLocaleLable[n];
//remove all items existed
           for(var count = 0; count < localeArr.length; count++)
           {
                var option = document.createElement("option");
                var localePair = localeArr[count].split(",");
                option.text = localeLableArr[count];
                option.value = localePair[0];
                languageOption.options.add(option);//languageOption.add(option);
           }
       }
  }
}

function submitForm()
{
  var jobIdOption = document.getElementById("jobId");
  var jobId = jobIdOption.options[jobIdOption.selectedIndex].value;
  var languageOption = document.getElementById("targetLang");//document.all.targetLang;
  var localeId = languageOption.options[languageOption.selectedIndex].value;
  if(localeId == 0 || jobId == 0)
  {
      alert("<%=bundle.getString("msg_report_select_name_language")%>");
  }
  else
  {
      ImplementedChkForm.submit();
  }
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("implemented_comments_check_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_select_a_job")%></SPAN>
</TD></TR></TABLE>

<form name="ImplementedChkForm" method="post" action="/globalsight/envoy/administration/reports/ImplementedCommentsCheckReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<tr>
<td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="jobId" id="jobId" onChange="displayLocale()" style="width:300px">
<%  
	if (jobList == null)
	{
%>
	<option VALUE="*"><%=bundle.getString("no_job")%></option>
<%
	}
	else
	{
%>
	<option VALUE="0"><%=bundle.getString("please_select")%></option>
	<script language="javascript">
	   var i = 0;
	</script>
<%
	Iterator iter = jobList.iterator();
        while (iter.hasNext())
        {
            Job job = (Job) iter.next();
            GlobalSightLocale[] locales = job.getL10nProfile().getTargetLocales();
%>
	<option title="<%=job.getJobName()%>" VALUE="<%=job.getJobId()%>"><%=job.getJobName()%></option>
	<script language="javascript">
	    jobIdArray.push("<%=job.getJobId()%>");
	    i++;
	    jobLocale[i - 1] = new Array();
	    jobLocaleLable[i - 1] = new Array();
	</script>
<%
            for(int j = 0; j < locales.length; j++)
            {
                 String localeStr = locales[j].getDisplayName() + "," + String.valueOf(locales[j].getId());
                 String localeLabel = locales[j].getDisplayName(uiLocale);
%>
         <script language="javascript">
             jobLocale[i - 1].push("<%=localeStr%>");
             jobLocaleLable[i - 1].push("<%=localeLabel%>");
         </script>
<%
            }
         }
	}
%>
</select>
</td>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_target_locales")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="targetLang" id="targetLang">
<%
	if (jobList == null)
	{
%>
	<option VALUE="*"><%=bundle.getString("no_job")%></option>
<%
	}
	else
	{
%>
         <option value="0"><%=bundle.getString("please_select")%></option>
<%
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
<td><input type="button" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
</tr>
</table>
</form>
<BODY>
</HTML>