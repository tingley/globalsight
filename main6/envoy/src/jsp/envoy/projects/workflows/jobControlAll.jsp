<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.projecthandler.ProjectInfo,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.costing.Currency,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.permission.Permission,
            java.util.Vector,
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
	
	String allStatusURL = allStatus.getPageURL()+ DEFAULT_PARAM + "&searchType=" + thisSearch; 
	String archivedURL = archived.getPageURL()+ DEFAULT_PARAM;
    String completeURL = complete.getPageURL()+ DEFAULT_PARAM;
    String detailsURL = jobDetails.getPageURL();
    String exportedURL = exported.getPageURL()+ DEFAULT_PARAM;
    String modifyURL = modify.getPageURL();
    String pendingURL = pending.getPageURL()+ DEFAULT_PARAM;
    String progressURL = progress.getPageURL()+ DEFAULT_PARAM;
    String readyURL = ready.getPageURL()+ DEFAULT_PARAM;
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    
    String title = bundle.getString("lb_my_jobs") + " - " + bundle.getString("lb_all_status");
    String lbPending= bundle.getString("lb_pending");
    String lbReady= bundle.getString("lb_ready");
    String lbInProgress= bundle.getString("lb_inprogress");
    String lbLocalized= bundle.getString("lb_localized");
    String lbExported= bundle.getString("lb_exported");
    String lbArchived= bundle.getString("lb_archived");
    String lbAllStatus= bundle.getString("lb_all_status");
    
    String refreshUrl = allStatusURL;
    String helperText = bundle.getString("helper_text_job_all_status");
    SessionManager sessMr= (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String badresults = (String)sessMr.getMyjobsAttribute("badresults");
    if(badresults == null)
    	badresults = "";
    sessMr.setMyjobsAttribute("badresults","");
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
var helpFile = "<%=bundle.getString("help_workflow_all_status_tab")%>";

function loadPage() 
{
   // Only show the download button if something is available to download
   //if (JobForm.jobId)
   //{
       //document.all.ButtonLayer.style.visibility = "visible";
       //document.all.CheckAllLayer.style.visibility = "visible";
   //}
   // Load the Guide
   loadGuides();
   
   ContextMenu.intializeContextMenu();
}

function setButtonState()
{
	// no buttons for this page for now
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
    // no buttons for this page for now
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
		baseUrl = "<%=exportedURL%>";
	else if(state =="ARCHIVED")
		baseUrl = "<%=archivedURL%>";
	else if(state =="ALL_STATUS")
		baseUrl = "<%=allStatusURL%>" + "&fromRequest=true";
	if(fromRequest && state != "ALL_STATUS")
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

<TABLE BORDER=0 CELLSPACING="0" CELLPADDING="0">
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

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0"  width="100%">
    <TR>
    	<TD ALIGN="RIGHT">
			<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
			<%=request.getAttribute(JobManagementHandler.PAGING_SCRIPTLET)%>   
			</DIV>
    	</TD>
    </TR>
<FORM NAME="JobForm" METHOD="POST">
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
<COL> <!-- Job Status -->
<thead>
<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell" WIDTH="1%"><input type="checkbox" onclick="handleSelectAll()" id="selectAll" name="selectAll"/></TD>
    <TD CLASS="headerCell" WIDTH="1%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PRIORITY%>" onclick="return addFilters(this)"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 BORDER=0 ALT="<%=bundle.getString("lb_priority")%>"></A><%=jobPrioritySortArrow%></TD>
    <amb:permission name="<%=Permission.JOBS_GROUP%>" >
    <TD CLASS="headerCell" WIDTH="1%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_GROUP_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_group_id")%></A><%=jobGroupIdSortArrow%></TD>
    </amb:permission>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_NAME%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_name")%></A><%=jobNameSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PROJECT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_project")%></A><%=jobProjectSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.SOURCE_LOCALE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_source_locale")%></A><%=jobSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.WORD_COUNT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_word_count")%></A><%=jobWordCountSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.DATE_CREATED%>" onclick="return addFilters(this)"><%=bundle.getString("lb_date_created")%></A><%=jobDateSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.PLANNED_DATE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_planned_completion_date")%></A><%=jobPlannedDateSortArrow%></TD>
    <TD CLASS="headerCell" WIDTH="7%"><A CLASS="sortHREFWhite" HREF="<%=allStatusURL + "&" + JobManagementHandler.SORT_PARAM + "=" + JobComparator.JOB_STATUS%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_status")%></A><%=jobStatusSortArrow%></TD>
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
    <TD CLASS="headerCell">&nbsp;</TD>
</TR>
</thead>
<tbody>
<c:forEach items="${jobVos}" var="jobVo" varStatus="i">
    <TR VALIGN=TOP STYLE=" ${jobVo.style} padding-top: 5px; padding-bottom: 5px;" BGCOLOR="#FFFFFF" CLASS=standardText>
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
	<TD STYLE="padding-right: 10px;" CLASS=${jobVo.textType} >${jobVo.displayStatues}</TD>
    </TR>
</c:forEach>
</tbody>
</TABLE>
<TABLE width="100%">
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
<!-- End Data Table  -->

<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</FORM>
 
</DIV>
</BODY>
</HTML>