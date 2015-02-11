<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            java.text.MessageFormat,
            java.util.Vector,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="archived" scope="request"
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
<jsp:useBean id="dtpprogress" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="changeWfMgr" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ready" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
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
    String pendingURL = pending.getPageURL()+ DEFAULT_PARAM;
    String progressURL = progress.getPageURL()+ startIndex + "&searchType=" + thisSearch;
    String dtpprogressURL = dtpprogress.getPageURL()+ startIndex + "&searchType=" + thisSearch;
    String completeURL = complete.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;

    // WF3 is used for In Progress tab (in EnvoyConfig.xml)
    String changeWfMgrURL = changeWfMgr.getPageURL() + DEFAULT_PARAM +
           "&" + JobManagementHandler.MY_JOBS_TAB + "=WF3";
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM;
    String modifyURL = modify.getPageURL();
    String detailsURL = jobDetails.getPageURL();
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    String searchURL = search.getPageURL() + "&action=search";
    String downloadURL = download.getPageURL();
    String title = bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_dtpinprogress");
    String lbPending= bundle.getString("lb_pending");
    String lbReady= bundle.getString("lb_ready");
    String lbInProgress= bundle.getString("lb_inprogress");
    String lbdtpInProgress= bundle.getString("lb_dtpinprogress");
    String lbLocalized= bundle.getString("lb_localized");
    String lbExported= bundle.getString("lb_exported");
    String lbArchived= bundle.getString("lb_archived");

    String refreshUrl = dtpprogressURL;
    boolean b_addDelete = false;
    boolean b_searchEnabled = false;
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        b_addDelete = sc.getBooleanParameter(SystemConfigParamNames.ADD_DELETE_ENABLED);
        b_searchEnabled =
               sc.getBooleanParameter(SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED);

    }
    catch (Exception ge)
    {
        // assume false
    }
    
    String helperText = bundle.getString("helper_text_job_dtpinprogress");
    
    /*
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
    */
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<%@ include file="/envoy/projects/workflows/myJobContextMenu.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_dtp_in_progress_tab")%>";
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
   if (transSelectedIndex.length == 0 && dtpSelectedIndex.length == 1)
   {
      if (document.JobForm.ChangeWFMgr)
          document.JobForm.ChangeWFMgr.disabled = false;
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = false;
          document.JobForm.Export.value = "<%=bundle.getString("lb_move_to_dtp")%>...";
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = false;
<% } %>
   }
   else if (transSelectedIndex.length == 1 && dtpSelectedIndex.length == 0)
   {
      if (document.JobForm.ChangeWFMgr)
          document.JobForm.ChangeWFMgr.disabled = false;
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = false;
          document.JobForm.Export.value = "<%=bundle.getString("lb_export")%>...";
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = false;
<% } %>
   }
   else
   {
      if (document.JobForm.ChangeWFMgr)
          document.JobForm.ChangeWFMgr.disabled = true;
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = true;
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = true;
<% } %>
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

   if(buttonClicked == "ExportForUpdate")
   {
      if (!confirm("<%=bundle.getString("jsmsg_warning")%>\n" +
          "<%=bundle.getString("jsmsg_export_source_warning")%>"))
      {
         return false;
      }
   }
   var valuesArray;
   var jobId = "";

   // If more than one radio button is displayed, loop
   // through the array to find the one checked
   // Note that valuesArray[1], the jobState is not used in this jsp page.
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
	         jobId += valuesArray[0];
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
	         jobId += valuesArray[0];
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

   if (buttonClicked == "Export" || buttonClicked == "ExportForUpdate")
   {
      ShowStatusMessage("<%=bundle.getString("jsmsg_preparing_for_export")%>");
      JobForm.action = "<%=request.getAttribute(JobManagementHandler.EXPORT_URL_PARAM)%>";
      jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
   }
   else if (buttonClicked == "Discard")
   {
      if ( !confirm("<%=bundle.getString("jsmsg_warning")%>\n\n" +
                    "<%=bundle.getString("jsmsg_discard_job")%>"))
      {
         return false;
      };

      ShowStatusMessage("<%=bundle.getString("jsmsg_discarding_selected_jobs")%>")
      JobForm.action = "<%=refreshUrl%>";
      jobActionParam = "<%=JobManagementHandler.DISCARD_JOB_PARAM%>";
   }
   else if (buttonClicked == "changeWFMgr")
   {
      JobForm.action = "<%=changeWfMgrURL%>";
      jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
   }
   else if (buttonClicked == "search")
   {
      JobForm.action = "<%=searchURL%>";
      jobActionParam = "search";
   }
   else if (buttonClicked == "Download")
   {
      JobForm.action = "<%=downloadURL%>&firstEntry=true&<%=DownloadFileHandler.PARAM_JOB_ID%>" +
                        "=" + jobId + "&<%=DownloadFileHandler.DOWNLOAD_FROM_JOB%>=true";
      JobForm.submit();
      return;
   }


   JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=<%=thisSearch%>";
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

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
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

<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
<%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>
</DIV>


<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR>
        <TD COLSPAN=3>

<FORM NAME="JobForm" METHOD="POST">

<!-- Data Table  -->
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" CLASS="list">
<COL> <!-- Radio button -->
<COL> <!-- Priority -->
<COL> <!-- Job ID -->
<COL WIDTH=130> <!-- Job Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Word Count -->
<COL> <!-- Date Created -->
<COL> <!-- Est Completion Created -->

<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell"></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.EST_COMPLETION_DATE%>"><%=bundle.getString("lb_estimated_completion_date")%></A><%=jobEstCompletionDateSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=dtpprogressURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PLANNED_DATE%>"><%=bundle.getString("lb_planned_completion_date")%></A><%=jobPlannedDateSortArrow%></TD>
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
                   HREF="javascript:checkAllWithName('JobForm', '<%=request.getAttribute(JobManagementHandler.CHECKBOX_NAME)%>'); setButtonState()"><%=bundle.getString("lb_check_all")%></A> |
<% if (b_searchEnabled) { %>
                <A CLASS="standardHREF"
                   HREF="javascript:checkAll('JobForm'); setButtonState()"><%=bundle.getString("lb_check_all_pages")%></A> |
<% } %>
                <A CLASS="standardHREF"
                   HREF="javascript:clearAll('JobForm');setButtonState();"><%=bundle.getString("lb_clear_all")%></A>
            </DIV>
         </TD>
     </TR>
</TABLE>

<DIV ID="ButtonLayer" ALIGN="RIGHT" STYLE="visibility: hidden">
<% if (b_searchEnabled) { %>
    <amb:permission name="<%=Permission.JOBS_SEARCH_REPLACE%>" >
        <INPUT TYPE="BUTTON" NAME=search VALUE="<%=bundle.getString("lb_search_replace")%>..." onClick="submitForm('search');">
    </amb:permission>
<% } %>
    <amb:permission name="<%=Permission.JOBS_CHANGE_WFM%>" >
        <INPUT TYPE="BUTTON" NAME=ChangeWFMgr VALUE="<%=bundle.getString("lb_change_workflow_manager")%>..." onClick="submitForm('changeWFMgr');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DISCARD%>" >
            <INPUT TYPE="BUTTON" NAME=Discard VALUE="<%=bundle.getString("lb_discard")%>" onClick="submitForm('Discard');">
    </amb:permission>
<% if (b_addDelete) { %>
    <amb:permission name="<%=Permission.JOBS_EXPORT_SOURCE%>" >
        <INPUT TYPE="BUTTON" NAME=ExportForUpdate VALUE="<%=bundle.getString("lb_export_source")%>..." onClick="submitForm('ExportForUpdate');">
    </amb:permission>
<% } %>
    <amb:permission name="<%=Permission.JOBS_EXPORT%>" >
        <INPUT TYPE="BUTTON" NAME=Export VALUE="<%=bundle.getString("lb_export")%>..." onClick="submitForm('Export');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DOWNLOAD%>" >
        <INPUT TYPE="BUTTON" NAME=Download VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');">
    </amb:permission>
</DIV>
<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</FORM>

</DIV>
</BODY>
</HTML>
