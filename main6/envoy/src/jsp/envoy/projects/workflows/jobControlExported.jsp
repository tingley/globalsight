<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.projecthandler.ProjectInfo,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.costing.Currency,
            com.globalsight.everest.jobhandler.Job,
            java.util.Vector,
            com.globalsight.everest.company.Company,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.foundation.User,
            com.globalsight.util.GlobalSightLocale,
            java.text.MessageFormat,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="allStatus" scope="request" 
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
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
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-store");
	response.setDateHeader("Expires", 0);

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
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM + "&searchType=" + thisSearch;
    String discardURL = exported.getPageURL()+ startIndex;
    String modifyURL = modify.getPageURL();
    String pendingURL = pending.getPageURL()+ DEFAULT_PARAM;
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String allStatusURL = allStatus.getPageURL()+ DEFAULT_PARAM;
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
    SessionManager sessMr= (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String badresults = (String)sessMr.getMyjobsAttribute("badresults");
    if(badresults == null)
    	badresults = "";
    sessMr.setMyjobsAttribute("badresults","");
    Company company = (Company)request.getAttribute("company");
    boolean enableQAChecks = company.getEnableQAChecks();
    boolean showButton = true;
    if(company.getId() == 1)
    {
    	showButton = false;
    }
%>                       
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<%@ include file="/envoy/projects/workflows/myJobContextMenu.jspIncl" %>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
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
   
   ContextMenu.intializeContextMenu();
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
   
   if (JobForm.jobIdHidden && JobForm.jobIdHidden.length)
   {
      for (i = 0; i < JobForm.jobIdHidden.length; i++)
      {
         if (JobForm.jobIdHidden[i].checked == true)
         {
            if( jobId != "" )
            {
               jobId += " "; // must add a [white space] delimiter
            }
            valuesArray = getRadioValues(JobForm.jobIdHidden[i].value);
            jobId += valuesArray[0];
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
   else if (buttonClicked == "ExportForUpdate" || buttonClicked == "ReExport")
   {
      JobForm.action = "<%=request.getAttribute(JobManagementHandler.EXPORT_URL_PARAM)%>";
      jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
      JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=" + "<%=thisSearch%>";
      if (buttonClicked == "ExportForUpdate")
	   {
	      JobForm.action += "&" + "<%=JobManagementHandler.EXPORT_FOR_UPDATE_PARAM%>" + "=true";
	   }
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "Archive")
   {
      ShowStatusMessage("<%=bundle.getString("jsmsg_archiving_selected_jobs")%>");
      JobForm.action = "<%=refreshUrl%>";
      jobActionParam = "archiveJob";
      JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=" + "<%=thisSearch%>";
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "Download")
   {
	  $("#downloadJobIds").val(jobId);
      JobForm.action = "<%=downloadURL%>&firstEntry=true&from=export"
                        + "&<%=DownloadFileHandler.DOWNLOAD_FROM_JOB%>=true";
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "Discard")
   {
      if ( !confirm("<%=bundle.getString("jsmsg_warning")%>\n\n" +
                    "<%=bundle.getString("jsmsg_discard_job")%>"))
      {
         return false;
      };

      ShowStatusMessage("<%=bundle.getString("jsmsg_discarding_selected_jobs")%>")
      JobForm.action = "<%=discardURL%>";
      jobActionParam = "<%=JobManagementHandler.DISCARD_JOB_PARAM%>";
      JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=" + "<%=thisSearch%>";
      JobForm.submit();
      return;
	}
   else if(buttonClicked == "downloadQAReport")
   {
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "<%=refreshUrl%>&action=checkDownloadQAReport",
		   data: "jobIds="+jobId,
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.download == "fail")
	          {
	      	   	 alert("<%=bundle.getString("lb_download_qa_reports_message")%>");
	          }
	          else if(returnData.download == "success")
	          {
		       	  JobForm.action = "<%=refreshUrl%>"+"&action=downloadQAReport";
		    	  JobForm.submit();
	          }
		   },
	   	   error:function(error)
	       {
          		alert(error.message);
           }
		});
   }
}

//for GBS-2599
function handleSelectAll() {
	if (JobForm && JobForm.selectAll) {
		if (JobForm.selectAll.checked) {
			checkAllWithName('JobForm', 'transCheckbox');
			setButtonState();
	    }
	    else {
			clearAll('JobForm'); 
			setButtonState();
	    }
	}
}

function searchJob(fromRequest)
{
	var baseUrl = "";
	var state = $("#sto").val();
	if(state =="PENDING")
		baseUrl = "<%=pendingURL%>";
	else if(state =="READY_TO_BE_DISPATCHED")
		baseUrl = "<%=readyURL%>";
	else if(state =="DISPATCHED")
		baseUrl = "<%=progressURL%>";
	else if(state =="LOCALIZED")
		baseUrl = "<%=completeURL%>";
	else if(state =="EXPORTED")
		baseUrl = "<%=exportedURL%>"  + "&fromRequest=true";
	else if(state =="ARCHIVED")
		baseUrl = "<%=archivedURL%>";
	else if(state =="ALL_STATUS")
		baseUrl = "<%=allStatusURL%>";
	if(fromRequest && state != "EXPORTED")
	{
		window.location = baseUrl+"&fromRequest=true"+"&sto="+$("#sto").val()
		+"&csf="+$("#creationStartFilter").val()+"&cso="+$("#creationStartOptionsFilter").val()
		+"&cef="+$("#creationEndFilter").val()+"&ceo="+$("#creationEndOptionsFilter").val()
		+"&esf="+$("#completionStartFilter").val()+"&eso="+$("#completionStartOptionsFilter").val()
		+"&eef="+$("#completionEndFilter").val()+"&eeo="+$("#completionEndOptionsFilter").val()
		+"&edss="+$("#exportDateStartFilter").val()+"&edso="+$("#exportDateStartOptionsFilter").val()
		+"&edee="+$("#exportDateEndFilter").val()+"&edes="+$("#exportDateEndOptionsFilter").val()
		+"&advancedSearch="+advancedSearch;
	}
	else
	{
		window.location = baseUrl
			+ "&sto="+$("#sto").val()+"&nf="+$("#jobNameFilter").val()
			+"&idf="+$("#jobIdFilter").val()+"&idg="+$("#jobGroupIdFilter").val()+"&io="+$("#jobIdOption").val()+"&po="+$("#jobProjectFilter").val()
			+"&sl="+$("#sourceLocaleFilter").val()+"&npp="+$("#numPerPage").val()+"&pro="+$("#priorityFilter").val()
			+"&csf="+$("#creationStartFilter").val()+"&cso="+$("#creationStartOptionsFilter").val()
			+"&cef="+$("#creationEndFilter").val()+"&ceo="+$("#creationEndOptionsFilter").val()
			+"&esf="+$("#completionStartFilter").val()+"&eso="+$("#completionStartOptionsFilter").val()
			+"&eef="+$("#completionEndFilter").val()+"&eeo="+$("#completionEndOptionsFilter").val()
			+"&edss="+$("#exportDateStartFilter").val()+"&edso="+$("#exportDateStartOptionsFilter").val()
			+"&edee="+$("#exportDateEndFilter").val()+"&edes="+$("#exportDateEndOptionsFilter").val()
			+"&advancedSearch="+advancedSearch;
	}
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

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR VALIGN="TOP">
        <TD COLSPAN=2>
            <SPAN CLASS="mainHeading">
            <%=title%>
            </SPAN>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText>    
        <TD>
        <br>
        <%=helperText%>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText> 
        <TD>
        <span style="color:red"><%=badresults%></span>
        </TD>
    </TR>
</TABLE>
<%@ include file="miniSearch.jspIncl" %>
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
<TR><TD>
<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
<%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>   
</DIV>
</TD></TR>

<TR><TD>  
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
<FORM NAME="JobForm" METHOD="POST">
<input type="hidden" id="downloadJobIds" name="<%=DownloadFileHandler.PARAM_JOB_ID%>" value=""/>
    <TR>
        <TD COLSPAN=3>
        
<!-- Data Table  -->             
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" id="list" CLASS="list" width="100%">
<COL> <!-- Radio button -->
<COL> <!-- Priority -->
<COL> <!-- Job ID -->
<COL WIDTH=130> <!-- Job Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Word Count -->
<COL> <!-- Date Created -->
                
<thead>
<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell" WIDTH="1%"><input type="checkbox" onclick="handleSelectAll()" id="selectAll" name="selectAll"/></TD>
    <TD CLASS="headerCell" WIDTH="1%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>" onclick="return addFilters(this)"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
   <amb:permission name="<%=Permission.JOBS_GROUP%>" >
   <TD CLASS="headerCell" WIDTH="1%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_GROUP_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_group_id")%></A><%=jobGroupIdSortArrow%></TD>
    </amb:permission>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>" onclick="return addFilters(this)"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=exportedURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.EST_COMPLETION_DATE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_estimated_job_completion_date")%></A><%=jobEstCompletionDateSortArrow%></TD>
</TR>
<TR CLASS="tableHeadingFilter" VALIGN="BOTTOM">
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">
    	<select id="priorityFilter" class="filterSelect">
	        <option value='-1'></option>
	        <option value='1'>1</option>
	        <option value='2'>2</option>
	        <option value='3'>3</option>
	        <option value='4'>4</option>
	        <option value='5'>5</option>
        </select>
    </TD>
    <amb:permission name="<%=Permission.JOBS_GROUP%>" >
    <TD CLASS="headerCell"  style="" nowrap>
    	<input class="standardText" style="width:80px" type="text" id="jobGroupIdFilter" name="jobGroupIdFilter" value="<%=jobGroupIdFilter%>"/>
    </TD>
    </amb:permission>
    <TD CLASS="headerCell"  style="width:150px" nowrap>
    	<select id="jobIdOption">
	        <option value='<%=SearchCriteriaParameters.EQUALS%>'>=</option>
	        <option value='<%=SearchCriteriaParameters.GREATER_THAN%>'>&gt;</option>
	        <option value='<%=SearchCriteriaParameters.LESS_THAN%>'>&lt;</option>
        </select>
    	<input class="standardText" style="width:80px" type="text" id="jobIdFilter" name="jobIdFilter" value="<%=jobIdFilter %>"/>
    </TD>
    <TD CLASS="headerCell"><input class="standardText" type="text" id="jobNameFilter" name="jobNameFilter" value="<%=jobNameFilter %>"/></TD>
    <TD CLASS="headerCell">
    <select name="<%=JobSearchConstants.PROJECT_OPTIONS%>" id="jobProjectFilter" class="filterSelect">
        <option value="-1">Choose...</option>
	    <%
			if (projects != null)
			{
			    for (int i=0; i < projects.size(); i++)
			    {
			        ProjectInfo p = (ProjectInfo)projects.get(i);
			        String projectName = p.getName();
			        long projectId = p.getProjectId();
			        String option = "<option value='" + projectId + "'>" +projectName + "</option>";
			        out.println(option);
			    }
			}
		%>
	</select>
    </TD>
    <TD CLASS="headerCell">
    <select name="<%=JobSearchConstants.SRC_LOCALE%>" class="standardText filterSelect" id="sourceLocaleFilter">
		<option value="-1">Choose...</option>
	    <%
		if (srcLocales != null)
		{
		    for (int i = 0; i < srcLocales.size();  i++)
		    {
		        GlobalSightLocale locale = (GlobalSightLocale)srcLocales.get(i);
		        String disp = locale.getDisplayName(uiLocale);
		        long lpId = locale.getId();
		        String option = "<option value='" + lpId + "'>" + disp + "</option>";
		        out.println(option);
		    }
		}
		%>
	</select>
    </TD>
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">&nbsp;</TD>
    <TD CLASS="headerCell">&nbsp;</TD>
</TR>
</thead>
<tbody>
<c:forEach items="${jobVos}" var="jobVo" varStatus="i">
    <TR VALIGN=TOP STYLE="padding-top: 5px; padding-bottom: 5px;" BGCOLOR="#FFFFFF" CLASS=standardText>
    <TD><INPUT onclick="setButtonState()" TYPE=checkbox NAME=transCheckbox VALUE="jobId=${jobVo.id}&jobState=${jobVo.statues}"></TD>
	<TD CLASS=standardText >${jobVo.priority}</TD>
	<amb:permission name="<%=Permission.JOBS_GROUP%>" >
	<TD CLASS=standardText style="text-align: center;">${jobVo.groupId}</TD>
	</amb:permission>
	<TD CLASS=standardText style="text-align: center;">${jobVo.id}</TD>
	<TD CLASS=standardText style="word-break:break-all" >	
	    <SCRIPT language="javascript">
	    if (navigator.userAgent.indexOf('Firefox') >= 0){
		    document.write("<DIV>");
		    }</SCRIPT>
		    <c:choose>
		    <c:when  test="${jobVo.hasDetail}">
		<B><A  CLASS="${jobVo.textType.replace("Text","HREF")}"  HREF="/globalsight/ControlServlet?linkName=jobDetails&pageName=ALLS&jobId=${jobVo.id}&fromJobs=true" oncontextmenu="contextForTab('${jobVo.id}',event)">${jobVo.name}</A></B>
		    </c:when >
		    <c:otherwise>${jobVo.name}</c:otherwise>
		    </c:choose>
		<SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("</DIV>")}</SCRIPT></TD>	 
	<TD CLASS=${jobVo.textType} >${jobVo.project}</TD>
	<TD CLASS=${jobVo.textType} >${jobVo.sourceLocale}</TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >${jobVo.wordcount}</TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >${jobVo.createDate}</TD>
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >${jobVo.plannedCompletionDate}</TD>
    </TR>
</c:forEach>
</tbody>
</TABLE>
<!-- End Data Table  -->          
        </TD>
     </TR>
     <TR>
        <TD CLASS="standardText">
            <DIV ID="CheckAllLayer"  ALIGN="RIGHT" style="padding-top: 5px">
            	Display #:
		        <select id="numPerPage" class="filterSelect">
		           <option value="10">10</option>
		           <option value="20">20</option>
		           <option value="50">50</option>
                   <option value="100">100</option>
		           <option value="200">200</option>
		        </select>
		        &nbsp;
                <%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>
            </DIV>
         </TD>
     </TR>
</TABLE>
</TD></TR>

<TR><TD>              
<DIV ID="ButtonLayer" ALIGN="LEFT" STYLE="visibility: hidden">
		<br>
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
		<amb:permission name="<%=Permission.JOBS_DISCARD%>" >
	            <INPUT TYPE="BUTTON" NAME=Discard VALUE="<%=bundle.getString("lb_discard")%>" onClick="submitForm('Discard');">
	    </amb:permission>
        <amb:permission name="<%=Permission.JOBS_ARCHIVE%>" >
        <INPUT TYPE="BUTTON" NAME=Archive VALUE="<%=bundle.getString("action_archive")%>" onClick="submitForm('Archive');">
        </amb:permission>
	    <amb:permission name="<%=Permission.JOBS_DOWNLOAD%>" >
	        <INPUT TYPE="BUTTON" NAME=Download VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');">
	    </amb:permission>
	    <%if(enableQAChecks && showButton){ %>
  		<INPUT TYPE="BUTTON" NAME=downloadQAReport VALUE="<%=bundle.getString("lb_download_qa_reports")%>" onClick="submitForm('downloadQAReport');">
    <% } %>
</DIV>
</TD></TR>
</TABLE>
<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</FORM>

</DIV>
</BODY>
</HTML>
