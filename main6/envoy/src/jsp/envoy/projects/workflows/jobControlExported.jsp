<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.costing.Currency,
            com.globalsight.everest.jobhandler.Job,
            java.util.Vector,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            java.text.MessageFormat,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="archived" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="changeCurr" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="complete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exported" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="progress" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ready" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    String DEFAULT_PARAM = "&jobListStart=0";
    Object param = request.getAttribute(JobManagementHandler.JOB_LIST_START_PARAM);
    String startIndex = param == null ? DEFAULT_PARAM : "&jobListStart="+param;
    String thisSearch = (String) request.getAttribute("searchType");
    if (thisSearch == null)
        thisSearch = (String) session.getAttribute("searchType");
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    String archivedURL = archived.getPageURL()+ DEFAULT_PARAM;
    String completeURL = complete.getPageURL()+ DEFAULT_PARAM;
    String detailsURL = jobDetails.getPageURL();
    String exportedURL = exported.getPageURL()+ startIndex + "&searchType=" + thisSearch;
    String modifyURL = modify.getPageURL();
    String pendingURL = pending.getPageURL()+ DEFAULT_PARAM;
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    String downloadURL = download.getPageURL();
    String title = bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_exported");
    String lbPending= bundle.getString("lb_pending");
    String lbReady= bundle.getString("lb_ready");
    String lbInProgress= bundle.getString("lb_inprogress");
    String lbLocalized= bundle.getString("lb_localized");
    String lbExported= bundle.getString("lb_exported");
    String lbArchived= bundle.getString("lb_archived");

    String refreshUrl = exportedURL;
    boolean b_addDelete = false;
    try
    {
       SystemConfiguration sc = SystemConfiguration.getInstance();
       b_addDelete = sc.getBooleanParameter(SystemConfigParamNames.ADD_DELETE_ENABLED);
    }
    catch (Throwable e)
    {
        // report error
    }
    
    String helperText = bundle.getString("helper_text_job_exported");
    int myJobsDaysRetrieved = 0;
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        myJobsDaysRetrieved =
               sc.getIntParameter(SystemConfigParamNames.MY_JOBS_DAYS_RETRIEVED);
       
        if ("stateOnly".equals(thisSearch) && myJobsDaysRetrieved > 0)
        {
            String[] messageArgs = {String.valueOf(myJobsDaysRetrieved)};
            helperText = helperText + MessageFormat.format(
               bundle.getString("helper_text_recent_jobs"), messageArgs);
        }

    }
    catch (Exception ge)
    {
        // assume 0
    }
%>                       
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_exported_tab")%>";
var jobActionParam = "";

function loadPage() 
{
   // Only show the download button if something is available to download
   if (JobForm.transCheckbox || JobForm.dtpCheckbox) 
   {
       document.all.ButtonLayer.style.visibility = "visible";
       document.all.CheckAllLayer.style.visibility = "visible";
   }
   // Load the Guide
   loadGuides();
}

function dtpSelectedIndex()
{
   var dtpSelectedIndex = new Array();
   
      var dtpCheckboxes = JobForm.dtpCheckbox;
		if (dtpCheckboxes != null) {
			if (dtpCheckboxes.length) {
				for (var i = 0; i < dtpCheckboxes.length; i++) {
					var checkbox = dtpCheckboxes[i];
					if (checkbox.checked) {
						dtpSelectedIndex.push(i);
					}
				}
			} else {
				if (dtpCheckboxes.checked) {
					dtpSelectedIndex.push(0);
				}
			}
		}
	return dtpSelectedIndex;
}

function transSelectedIndex() 
{
	var transSelectedIndex = new Array();
		
	var transCheckboxes = JobForm.transCheckbox;
	if (transCheckboxes != null) {
		if (transCheckboxes.length) {
			for (var i = 0; i < transCheckboxes.length; i++) {
				var checkbox = transCheckboxes[i];
				if (checkbox.checked) {
					transSelectedIndex.push(i);
				}
			}
		} else {
			if (transCheckboxes.checked) {
				transSelectedIndex.push(0);
			}
		}
	}
	return transSelectedIndex;
}

function updateButtonState(transSelectedIndex, dtpSelectedIndex)
{
   if (transSelectedIndex.length == 0 && dtpSelectedIndex.length == 1 || transSelectedIndex.length == 1 && dtpSelectedIndex.length == 0)
   {
      <% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
         document.JobForm.ExportForUpdate.disabled = false;
      <% } %>
      if (document.JobForm.ReExport)
          document.JobForm.ReExport.disabled = false;
      if (document.JobForm.ViewError)
          document.JobForm.ViewError.disabled = false;
      if (document.JobForm.Download)
          document.JobForm.Download.disabled = false;
   }
   else
   {
      <% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = true;
      <% } %>
      if (document.JobForm.ReExport)
          document.JobForm.ReExport.disabled = true;
      if (document.JobForm.ViewError)
          document.JobForm.ViewError.disabled = true;
      if (document.JobForm.Download)
          document.JobForm.Download.disabled = true;
   }
}

function setButtonState()
{
   updateButtonState(transSelectedIndex(), dtpSelectedIndex());
}

function ShowStatusMessage(p_msg)
{
    if (document.layers)
    {
        document.menu.document.statusMessage.innerHTML = p_msg;
    }
    else
    {
       statusMessage.innerHTML = p_msg;
    }
} 

function submitForm(buttonClicked) 
{
   var dtpIndexes = dtpSelectedIndex();
   var transIndexes = transSelectedIndex();

   if (dtpIndexes.length == 0 && transIndexes.length == 0)
   {
      alert ("<%= bundle.getString("jsmsg_please_select_a_row") %>");
      return false;
   }

   var valuesArray;
   var jobId = "";
   var erroredJobSelected;

   // If more than one radio button is displayed, loop
   // through the array to find the one checked
   if (transIndexes.length > 0)
   {
	   if (JobForm.transCheckbox.length)
	   {
	      for (var i = 0; i < JobForm.transCheckbox.length; i++) 
	      {
	         if (JobForm.transCheckbox[i].checked == true) 
	         {
	            if( jobId != "" ) 
	            { 
	               jobId += " "; // must add a [white space] delimiter
	            }
	            valuesArray = getRadioValues(JobForm.transCheckbox[i].value);
	            jobId += valuesArray[0];
	            // check job state
	            if (valuesArray[1] == "JOB_EXPORT_IN_PROGRESS")
	            {
	               alert("<%=bundle.getString("jsmsg_exporting_in_progress")%>: " + jobId);
	               return false;                                                    
	            }
	            else if (valuesArray[1] == "EXPORT_FAILED")
	            {
	                erroredJobSelected = true;
	            }
	         }
	      }
	   }
	   // If only one radio button is displayed, there is no radio button array, so
	   // just check if the single radio button is checked
	   else 
	   {
	      if (JobForm.transCheckbox.checked == true)
	      {
	         valuesArray = getRadioValues(JobForm.transCheckbox.value);
	         jobId = valuesArray[0];
	         // check job state
	         if (valuesArray[1] == "JOB_EXPORT_IN_PROGRESS")
	         {
	            alert("<%=bundle.getString("jsmsg_exporting_in_progress")%>");
	            return false;
	         }
	         else if (valuesArray[1] == "EXPORT_FAILED")
	         {
	             erroredJobSelected = true;
	         }
	      }
	   }
   }
   if (dtpIndexes.length > 0)
   {
	   if (JobForm.dtpCheckbox.length)
	   {
	      for (var i = 0; i < JobForm.dtpCheckbox.length; i++) 
	      {
	         if (JobForm.dtpCheckbox[i].checked == true) 
	         {
	            if( jobId != "" ) 
	            { 
	               jobId += " "; // must add a [white space] delimiter
	            }
	            valuesArray = getRadioValues(JobForm.dtpCheckbox[i].value);
	            jobId += valuesArray[0];
	            // check job state
	            if (valuesArray[1] == "JOB_EXPORT_IN_PROGRESS")
	            {
	               alert("<%=bundle.getString("jsmsg_exporting_in_progress")%>: " + jobId);
	               return false;                                                    
	            }
	            else if (valuesArray[1] == "EXPORT_FAILED")
	            {
	                erroredJobSelected = true;
	            }
	         }
	      }
	   }
	   // If only one radio button is displayed, there is no radio button array, so
	   // just check if the single radio button is checked
	   else 
	   {
	      if (JobForm.dtpCheckbox.checked == true)
	      {
	         valuesArray = getRadioValues(JobForm.dtpCheckbox.value);
	         jobId = valuesArray[0];
	         // check job state
	         if (valuesArray[1] == "JOB_EXPORT_IN_PROGRESS")
	         {
	            alert("<%=bundle.getString("jsmsg_exporting_in_progress")%>");
	            return false;
	         }
	         else if (valuesArray[1] == "EXPORT_FAILED")
	         {
	             erroredJobSelected = true;
	         }
	      }
	   }
   }

   if (buttonClicked == "ViewError")
   {
      if (!erroredJobSelected)
      {
         alert("<%=bundle.getString("jsmsg_job_has_no_errors")%>");
         return false;
      }
      else
      {
         if (confirm("<%=bundle.getString("jsmsg_go_to_job_details")%>"))
         {
            JobForm.action = "<%=detailsURL%>";
            jobActionParam = "<%=JobManagementHandler.JOB_ID%>";
         }
         else
         {
            return false;
         }
      }
   }
   else if (buttonClicked == "ExportForUpdate" || 
            buttonClicked == "ReExport")
   {
      JobForm.action = "<%=request.getAttribute(JobManagementHandler.EXPORT_URL_PARAM)%>";
      jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
   }
   else if (buttonClicked == "Archive")
   {
      ShowStatusMessage("<%=bundle.getString("jsmsg_archiving_selected_jobs")%>");
      JobForm.action = "<%=refreshUrl%>";
      jobActionParam = "archiveJob";
   }
   else if (buttonClicked == "Download")
   {
      JobForm.action = "<%=downloadURL%>&firstEntry=true&<%=DownloadFileHandler.PARAM_JOB_ID%>" +
                        "=" + jobId + "&<%=DownloadFileHandler.DOWNLOAD_FROM_JOB%>=true";
      JobForm.submit();
      return;
   }

   JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=" + "<%=thisSearch%>";
   if (buttonClicked == "ExportForUpdate")
   {
      JobForm.action += "&" + "<%=JobManagementHandler.EXPORT_FOR_UPDATE_PARAM%>" + "=true";
   }
   JobForm.submit();
}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/projects/workflows/jobSort.jspIncl" %>
<STYLE>
<%--
This stylesheet should be in the HEAD element but the skin bean
is defined in header.jspIncl which must be included in the body.
--%>
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
.headerCell {
    padding-right: 10px; 
    padding-top: 2px; 
    padding-bottom: 2px;
}
</STYLE>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<TABLE BORDER=0>
    <TR VALIGN="TOP">
        <TD COLSPAN=2>
            <SPAN CLASS="mainHeading">
            <%=title%>
            </SPAN>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText>    
        <TD WIDTH=500><%=helperText%>
        </TD>
        <TD ALIGN="RIGHT">
        </TD>
    </TR>
</TABLE>
<%@ include file="miniSearch.jspIncl" %>
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR><TD>
<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
<%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>   
</DIV>
</TD></TR>

<TR><TD>  
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR>
        <TD COLSPAN=3>
        
<FORM NAME="JobForm" METHOD="POST">
        
<!-- Data Table  -->             
<TABLE BORDER="0" CELLPADDING="6" CELLSPACING="0" CLASS="list">
<TBODY>
<COL> <!-- Radio button -->
<COL> <!-- Priority -->
<COL> <!-- Job ID -->
<COL WIDTH=130> <!-- Job Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Word Count -->
<COL> <!-- Date Created -->
                
<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell"></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.EST_COMPLETION_DATE%>"><%=bundle.getString("lb_estimated_job_completion_date")%></A><%=jobEstCompletionDateSortArrow%></TD>
</TR>
<%=request.getAttribute(JobManagementHandler.JOB_SCRIPTLET)%>  
</TABLE>
<!-- End Data Table  -->          
        </TD>
     </TR>
     <TR>
        <TD CLASS="standardText">
            <DIV ID="CheckAllLayer" STYLE="visibility: hidden">
                <A CLASS="standardHREF" 
                   HREF="javascript:checkAll('JobForm');setButtonState();"><%=bundle.getString("lb_check_all")%></A> | 
                <A CLASS="standardHREF" 
                   HREF="javascript:clearAll('JobForm');setButtonState();"><%=bundle.getString("lb_clear_all")%></A>
            </DIV>
         </TD>
     </TR>
</TABLE>
</TD></TR>

<TR><TD>              
<DIV ID="ButtonLayer" ALIGN="RIGHT" STYLE="visibility: hidden">
        <amb:permission name="<%=Permission.JOBS_VIEW_ERROR%>" >
        <INPUT TYPE="BUTTON" NAME=ViewError VALUE="<%=bundle.getString("action_view_error")%>..." onClick="submitForm('ViewError');">
        </amb:permission>
        <amb:permission name="<%=Permission.JOBS_REEXPORT%>" >
        <INPUT TYPE="BUTTON" NAME=ReExport VALUE="<%=bundle.getString("action_reexport")%>..." onClick="submitForm('ReExport');">
        </amb:permission>
<% if (b_addDelete) { %>        
        <amb:permission name="<%=Permission.JOBS_EXPORT_SOURCE%>" >
        <INPUT TYPE="BUTTON" NAME=ExportForUpdate VALUE="<%=bundle.getString("lb_export_source")%>..." onClick="submitForm('ExportForUpdate');">
        </amb:permission>
<% } %>        
        <amb:permission name="<%=Permission.JOBS_ARCHIVE%>" >
        <INPUT TYPE="BUTTON" NAME=Archive VALUE="<%=bundle.getString("action_archive")%>" onClick="submitForm('Archive');">
        </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DOWNLOAD%>" >
        <INPUT TYPE="BUTTON" NAME=Download VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');">
    </amb:permission>
</DIV>
</TD></TR>
</TABLE>
<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</FORM>

</DIV>
</BODY>
</HTML>
