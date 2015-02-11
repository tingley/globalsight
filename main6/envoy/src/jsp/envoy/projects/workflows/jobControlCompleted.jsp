<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            java.text.MessageFormat,
            java.util.ResourceBundle"
    session="true" %>
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
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM;
    String completeURL = complete.getPageURL()+ startIndex + "&searchType=" + thisSearch;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM;
    String modifyURL = modify.getPageURL();
    String detailsURL = jobDetails.getPageURL();
    String downloadURL = download.getPageURL() + "&from=localized";
    String title= bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_localized");
    String lbPending= bundle.getString("lb_pending");
    String lbReady= bundle.getString("lb_ready");
    String lbInProgress= bundle.getString("lb_inprogress");
    String lbLocalized= bundle.getString("lb_localized");
    String lbExported= bundle.getString("lb_exported");
    String lbArchived= bundle.getString("lb_archived");
    String refreshUrl = completeURL;
    boolean b_addDelete = false;
    try
    {
       SystemConfiguration sc = SystemConfiguration.getInstance();
       b_addDelete = sc.getBooleanParameter(SystemConfigParamNames.ADD_DELETE_ENABLED);
    }
    catch (Throwable e)
    {
    }
    
    String helperText = bundle.getString("helper_text_job_completed");
    
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
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_localized_tab")%>";

function loadPage()
{
   // Only show the download button if something is available to download
   if (JobForm.transCheckbox || JobForm.dtpCheckbox)
   {
       document.all.ButtonLayer.style.visibility = "visible";
   }
   // Load the Guide
   loadGuides();
   
   ContextMenu.intializeContextMenu();
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
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = false;
          document.JobForm.Export.value = "<%=bundle.getString("lb_move_to_dtp")%>...";
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = false;
<% } %>
      if (document.JobForm.Download)
          document.JobForm.Download.disabled = false;
   }
   else if (transSelectedIndex.length == 1 && dtpSelectedIndex.length == 0)
   {
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = false;
          document.JobForm.Export.value = "<%=bundle.getString("lb_export")%>...";
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = false;
<% } %>
      if (document.JobForm.Download)
          document.JobForm.Download.disabled = false;
   }
   else
   {
      if (document.JobForm.Export)
          document.JobForm.Export.disabled = true;
<% if (b_addDelete) { %>
      if (document.JobForm.ExportForUpdate)
          document.JobForm.ExportForUpdate.disabled = true;
<% } %>
      if (document.JobForm.Download)
          document.JobForm.Download.disabled = true;
   }
}

function setButtonState()
{
   updateButtonState(transSelectedIndex(), dtpSelectedIndex());
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

   ShowStatusMessage("<%=bundle.getString("jsmsg_preparing_for_export")%>");
   JobForm.action = "<%=request.getAttribute(JobManagementHandler.EXPORT_URL_PARAM)%>";
   jobActionParam = "<%=request.getAttribute(JobManagementHandler.JOB_ID)%>";

   var valuesArray;
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
	            valuesArray = getRadioValues(JobForm.transCheckbox[i].value);
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
               valuesArray = getRadioValues(JobForm.dtpCheckbox[i].value);
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
	      }
	   }
   }
   var jobId = valuesArray[0];
   var jobState = valuesArray[1];

   if (buttonClicked == "Download")
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

//for GBS-2599
function handleSelectAll() {
	if (JobForm && JobForm.selectAll) {
		if (JobForm.selectAll.checked) {
			checkAll('JobForm');
			setButtonState();
	    }
	    else {
			clearAll('JobForm'); 
			setButtonState();
	    }
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
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" CLASS="list">
<COL> <!-- Radio button -->
<COL> <!-- Priority -->
<COL> <!-- Job ID -->
<COL WIDTH=130> <!-- Job Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Word Count -->
<COL> <!-- Date Created -->

<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell"><input type="checkbox" onclick="handleSelectAll()" name="selectAll"/></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=completeURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.EST_COMPLETION_DATE%>"><%=bundle.getString("lb_estimated_job_completion_date")%></A><%=jobEstCompletionDateSortArrow%></TD>
</TR>
<c:forEach items="${jobVos}" var="jobVo" varStatus="i">
    <TR VALIGN=TOP STYLE="padding-top: 5px; padding-bottom: 5px;" BGCOLOR="#FFFFFF" CLASS=standardText>
    <TD><INPUT onclick="setButtonState()" TYPE=checkbox NAME=transCheckbox VALUE="jobId=${jobVo.id}&jobState=${jobVo.statues}"></TD>
	<TD CLASS=standardText >${jobVo.priority}</TD>
	<TD CLASS=standardText >${jobVo.id}</TD>
	<TD CLASS=standardText width="210px" style="word-wrap:break-word;word-break:break-all" >	
	    <SCRIPT language="javascript">
	    if (navigator.userAgent.indexOf('Firefox') >= 0){
		    document.write("<DIV style='width:200px'>");
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

</TABLE>
<!-- End Data Table  -->
</TABLE>
</TD></TR>

<TR><TD>
<DIV ID="ButtonLayer" ALIGN="RIGHT" STYLE="visibility: hidden">
        <amb:permission name="<%=Permission.JOBS_EXPORT%>" >
        <INPUT TYPE="BUTTON" NAME=Export VALUE="<%=bundle.getString("lb_export")%>..." onClick="submitForm('Export');">
        </amb:permission>
<% if (b_addDelete) { %>
        <amb:permission name="<%=Permission.JOBS_EXPORT_SOURCE%>" >
        <INPUT TYPE="BUTTON" NAME=ExportForUpdate VALUE="<%=bundle.getString("lb_export_source")%>..." onClick="submitForm('ExportForUpdate');">
        </amb:permission>
<% } %>
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
