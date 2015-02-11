<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            java.text.MessageFormat,
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
<jsp:useBean id="ready" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
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
    String pendingURL = pending.getPageURL()+ startIndex + "&searchType=" + thisSearch;
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM;
    String completeURL = complete.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM;
    String modifyURL = modify.getPageURL();
    String detailsURL = jobDetails.getPageURL();
    String title = bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_pending");
    String lbPending = bundle.getString("lb_pending");
    String lbReady = bundle.getString("lb_ready");
    String lbInProgress = bundle.getString("lb_inprogress");
    String lbLocalized = bundle.getString("lb_localized");
    String lbExported = bundle.getString("lb_exported");
    String lbArchived = bundle.getString("lb_archived");
    String refreshUrl = pendingURL;
    
    String helperText = bundle.getString("helper_text_job_pending");
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
var helpFile = "<%=bundle.getString("help_workflow_pending_tab")%>";

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
      document.JobForm.Error.disabled = false;
   }
   else
   {
      document.JobForm.Error.disabled = true;
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

   var erroredJobSelected;
   var jobId = "";
   var valuesArray;

   // If more than one radio button is displayed, loop
   // through the array to find the one checked
   if (transIndexes.length > 0)
   {
	   if (JobForm.transCheckbox.length)
	   {
	      for (var i = 0, j = 0; i < JobForm.transCheckbox.length; i++) 
	      {
	         if (JobForm.transCheckbox[i].checked == true) 
	         {
	            if( jobId != "" ) 
	            { 
	               jobId += " "; // add a [white space] delimiter
	            }
	            valuesArray = getRadioValues(JobForm.transCheckbox[i].value);
	            jobId += valuesArray[0];
	            if( valuesArray[1] == "IMPORT_FAILED" )
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
	         jobId += valuesArray[0];
	         if( valuesArray[1] == "IMPORT_FAILED" )
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
	      for (var i = 0, j = 0; i < JobForm.dtpCheckbox.length; i++) 
	      {
	         if (JobForm.dtpCheckbox[i].checked == true) 
	         {
	            if( jobId != "" ) 
	            { 
	               jobId += " "; // add a [white space] delimiter
	            }
	            valuesArray = getRadioValues(JobForm.dtpCheckbox[i].value);
	            jobId += valuesArray[0];
	            if( valuesArray[1] == "IMPORT_FAILED" )
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
	         jobId += valuesArray[0];
	         if( valuesArray[1] == "IMPORT_FAILED" )
	         {
	             erroredJobSelected = true;
	         }
	      }
	   }
   }
   
   if (buttonClicked == "Error")
   {
      if (!erroredJobSelected)
      {
         alert("This job has no errors.");
         return false;
      }
      else
      {
         JobForm.action = "<%=request.getAttribute(JobManagementHandler.ERROR_URL_PARAM)%>";
         jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";
      }
   }
   else if (buttonClicked == "Discard")
   {
      if ( !confirm("<%=bundle.getString("jsmsg_warning")%>\n\n" + 
                    "<%=bundle.getString("jsmsg_discard_job")%>"))
      {
         return false;
      };

      ShowStatusMessage("<%=bundle.getString("jsmsg_discarding_selected_jobs")%>");
      JobForm.action = "<%=refreshUrl%>";
      jobActionParam = "<%=request.getAttribute(JobManagementHandler.DISCARD_JOB_PARAM)%>";
   }
   else if (buttonClicked == "MakeReady")
   {
      if (erroredJobSelected)
      {
         alert("<%=bundle.getString("jsmsg_selected_job_error")%>");
         return false;
      }
      else
      {
         ShowStatusMessage("<%=bundle.getString("jsmsg_moving_selected_jobs_to_ready")%>");
         JobForm.action = "<%=refreshUrl%>";
         jobActionParam = "<%=request.getAttribute(JobManagementHandler.MAKE_READY_JOB_PARAM)%>";
      }
   }

   JobForm.action += "&" + jobActionParam + "=" + jobId + "&searchType=" + "<%=thisSearch%>";
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
        <TD COLSPAN=0>
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
</TD></TR>

<TR><TD COLSPAN=0>
<%@ include file="miniSearch.jspIncl" %>

<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
<%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>   
</DIV>
</TD></TR>

<TR><TD COLSPAN=0>
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR>
        <TD COLSPAN=3>
        
<FORM NAME="JobForm" METHOD="POST">

<!-- Data Table  -->             
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0"  CLASS="list">
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
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=pendingURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PLANNED_DATE%>"><%=bundle.getString("lb_planned_completion_date")%></A><%=jobPlannedDateSortArrow%></TD>
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
<TR><TD COLSPAN=0>        
<DIV ID="ButtonLayer" ALIGN="RIGHT" STYLE="visibility: hidden">
    <amb:permission name="<%=Permission.JOBS_VIEW_ERROR%>" >
        <INPUT TYPE="BUTTON" NAME=Error VALUE="<%=bundle.getString("action_view_error")%>" onClick="submitForm('Error');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_DISCARD%>" >
        <INPUT TYPE="BUTTON" NAME=Discard VALUE="<%=bundle.getString("lb_discard")%>" onClick="submitForm('Discard');">
    </amb:permission>
    <amb:permission name="<%=Permission.JOBS_MAKE_READY%>" >
        <INPUT TYPE="BUTTON" NAME=MakeReady VALUE="<%=bundle.getString("action_make_ready")%>" onClick="submitForm('MakeReady');">
    </amb:permission>
</DIV>
</TD></TR>
</TABLE> 
<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</FORM>
 
</DIV>
</BODY>
</HTML>

