<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> 
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.permission.Permission,
    com.globalsight.everest.webapp.pagehandler.PageHandler,
    com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.everest.webapp.WebAppConstants,
    com.globalsight.util.GlobalSightLocale,
    com.globalsight.everest.projecthandler.ProjectImpl,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
    com.globalsight.everest.jobhandler.JobGroup,
    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobGroupComparator"
    session="true"
%>
<jsp:useBean id="create" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="delete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	String DEFAULT_PARAM = "&jobGroupListStart=0";
	Object param = request.getAttribute(WebAppConstants.JOB_GROUP_LIST_START);
	String startIndex = param == null ? DEFAULT_PARAM : "&jobGroupListStart="+param;
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-store");
	response.setDateHeader("Expires", 0);
	ResourceBundle bundle = PageHandler.getBundle(session);
	List projects = (List)request.getAttribute("projects");
	List sourceLocales = (List)request.getAttribute("sourceLocales");
    String groupTitle = bundle.getString("lb_my_groups");
    String helperText = bundle.getString("helper_text_all_groups");
    String newJobGroupUrl = create.getPageURL();
    String deleteUrl = delete.getPageURL();
    String selfUrl = self.getPageURL()+ DEFAULT_PARAM;
    List<JobGroup> allGroupsList = (List<JobGroup>)request.getAttribute("allGroups");
    boolean empty = false;
    if(allGroupsList.size() == 0)
    {
    	empty = true;
    }
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= groupTitle %></TITLE>
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
var guideNode = "myGroups";
var helpFile = "<%=bundle.getString("help_job_group_list")%>";

function loadPage() 
{
   loadGuides();
   ContextMenu.intializeContextMenu();
}

//for GBS-2599
function handleSelectAll() {
	if (JobGroupForm && JobGroupForm.selectAll) {
		if (JobGroupForm.selectAll.checked) {
			checkAll('JobGroupForm');
	    }
	    else {
			clearAll('JobGroupForm'); 
	    }
	}
}

function searchJobGroup(fromRequest)
{
	window.location = "<%=selfUrl%>"+"&fromRequest=true"
		+"&groupId="+$("#groupIdFilter").val()
		+"&groupName="+$("#groupNameFilter").val()
		+"&groupProject="+$("#groupProjectFilter").val()
		+"&groupLocale="+$("#sourceLocaleFilter").val()
		+"&npp="+$("#numPerPage").val();
}

function submitForm(param)
{
	if(param == "new")
   {
		window.location.href = '<%=newJobGroupUrl + "&" + WebAppConstants.ACTION_STRING + "=" + WebAppConstants.ACTION_NEW%>';
   }
   else if(param == "remove")
   {
	   var selectedIds = getSelectRadioBtn();
	   if(selectedIds.length == 0){
		   alert("<%=bundle.getString("lb_select_jobGroup_to_remove")%>");
		   return false;
	   }
	   var url = "<%=selfUrl+"&"+WebAppConstants.ACTION_STRING +"="+WebAppConstants.JOB_GROUP_CHECK%>&selectedIds="+selectedIds;
	   $.getJSON(url, function(data) {
		   	var deleteId = data.right;
		   var wrong = data.wrong;
		   if(wrong != "needRemove"){
			   alert("The job groups are in use,(group id : "+wrong+") can not be deleted.");
		   }
		   if(deleteId != "noNeedRemove"){
			   var deleteUrl = '<%=deleteUrl + "&" + WebAppConstants.ACTION_STRING + "=" + WebAppConstants.ACTION_REMOVE%>';
			   deleteUrl += "&selectedIds="+deleteId;
			   window.location = deleteUrl ;
		   }
		   else
		   {
			   window.location = "<%=selfUrl%>"
		   }
	   });
   }
}

function getSelectRadioBtn()
{
	var selectedIds = "";
	 if (JobGroupForm.RadioBtn.length)
	 {
		 for (i = 0; i < JobGroupForm.RadioBtn.length; i++) 
	     {
			 if(JobGroupForm.RadioBtn[i].checked == true)
			 {
				 selectedIds += JobGroupForm.RadioBtn[i].value + ",";
			 }
	     } 
	 }
	 else
	 {
		 if(JobGroupForm.RadioBtn.checked == true)
		 {
			 selectedIds = JobGroupForm.RadioBtn.value+",";
		 }
	 }
	 selectedIds = selectedIds.substring(0,selectedIds.lastIndexOf(","));
	 return selectedIds;
}
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/projects/workflows/jobGroupSort.jspIncl" %>
<STYLE>
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
            <SPAN CLASS="mainHeading"> <%=groupTitle%> </SPAN>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText>    
        <TD><br><%=helperText%></TD>
    </TR>
</TABLE>

<%@ include file="jobGroupMiniSearch.jspIncl" %>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0"  width="100%">
    <TR>
    	<TD ALIGN="RIGHT">
			<DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
			<%=request.getAttribute(WebAppConstants.JOB_GROUP_PAGING_SCRIPTLET)%>   
			</DIV>
    	</TD>
    </TR>
<FORM NAME="JobGroupForm" METHOD="POST">
    <TR><TD COLSPAN=3>
<!-- Data Table  -->             
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" id="list" CLASS="list" width="100%">
<COL> <!-- Radio button -->
<COL> <!-- GROUP ID -->
<COL WIDTH=130> <!-- GROUP Name-->
<COL> <!-- Project -->
<COL> <!-- Source Locale -->
<COL> <!-- Date Created -->
<thead>
<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
    <TD CLASS="headerCell" style="width:20px;" ><input type="checkbox" onclick="handleSelectAll()" id="selectAll" name="selectAll"/></TD>
    <TD CLASS="headerCell" style="width:80px;" ><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_ID%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_group_id")%></A><%= jobGroupIdSortArrow%></TD>
    <TD CLASS="headerCell" style="width:25%;"><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_NAME%>" onclick="return addFilters(this)"><%=bundle.getString("lb_job_group_name")%></A><%= jobGroupNameSortArrow%></TD>
    <TD CLASS="headerCell" style="width:150px;"><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_PROJECT%>" onclick="return addFilters(this)"><%=bundle.getString("lb_project")%></A><%= jobGroupProjectSortArrow%></TD>
    <TD CLASS="headerCell" style="width:150px;"><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_SOURCE_LOCALE%>" onclick="return addFilters(this)"><%=bundle.getString("lb_source_locale")%></A><%= jobGroupSourceLocaleSortArrow%></TD>
    <TD CLASS="headerCell" style="width:150px;"><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_CREATED_USER%>" onclick="return addFilters(this)"><%=bundle.getString("lb_create_user")%></A><%= jobGroupCreateUserSortArrow%></TD>
    <TD CLASS="headerCell" style="width:25%;"><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_DATE_CREATED%>" onclick="return addFilters(this)"><%=bundle.getString("lb_date_created")%></A><%= jobGroupDateSortArrow%></TD>
	<% if(isSuperAdmin){%>
		<TD CLASS="headerCell" style="width:150px;"><A CLASS="sortHREFWhite" HREF="<%=selfUrl + "&" + WebAppConstants.JOB_GROUP_SORT_PARAM + "="+JobGroupComparator.JOB_GROUP_CONPANY_NAME%>" onclick="return addFilters(this)"><%=bundle.getString("lb_company_name")%></A><%= jobGroupCompanyNameSortArrow%></TD>
	<%}%>
</TR>
<TR CLASS="tableHeadingFilter" VALIGN="BOTTOM">
    <TD CLASS="headerCell" style="width:20px;">&nbsp;</TD>
    <TD CLASS="headerCell"  style="width:80px;" nowrap>
    	<input class="standardText" style="width:80px" type="text" id="groupIdFilter" name="groupIdFilter" value="<%=groupIdFilter %>"/>
    </TD>
    <TD CLASS="headerCell" style="width:150px;">
    	<input class="standardText" type="text" id="groupNameFilter" name="groupNameFilter" value="<%=groupNameFilter %>"/>
    </TD>
    <TD CLASS="headerCell" style="width:150px;">
	    <select name="<%=JobSearchConstants.PROJECT_OPTIONS%>" id="groupProjectFilter" class="filterSelect">
	        <option value="-1">Choose...</option>
	        <%
			if (projects != null)
			{
			    for (int i=0; i < projects.size(); i++)
			    {
			    	ProjectImpl p = (ProjectImpl)projects.get(i);
			        String projectName = p.getName();
			        long projectId = p.getId();
			        %>
			        <option value="<%= projectId%>"><%= projectName%></option>
			   <%
			    }
			}
		%>
		</select>
    </TD>
    <TD CLASS="headerCell" style="width:150px;">
	    <select name="<%=JobSearchConstants.SRC_LOCALE%>" class="standardText filterSelect" id="sourceLocaleFilter">
			<option value="-1">Choose...</option>
			<%
			if (sourceLocales != null)
			{
			    for (int i=0; i < sourceLocales.size(); i++)
			    {
			    	GlobalSightLocale locale = (GlobalSightLocale)sourceLocales.get(i);
			    	String disp = locale.getDisplayName();
			        long lpId = locale.getId();
			     %>
			      <option value="<%= lpId%>"><%= disp%></option>
			   <%
			    }
			}
		%>
		</select>
    </TD>
    <TD CLASS="headerCell" style="width:150px;">&nbsp;</TD>
    <TD CLASS="headerCell" style="width:150px;">&nbsp;</TD>
    <% if(isSuperAdmin){%>
	<TD CLASS="headerCell" style="width:150px;">&nbsp;</TD>
	<%}%>
</TR>
</thead>
<tbody>
<%
	if(empty){
	%>
		<TR>
			<td colspan="8" CLASS=standardText style="text-align: left;"><%=bundle.getString("msg_no_job_groups")%></td>
		</TR>
	<%
	}
%>
<c:forEach items="${allGroups}" var="group" varStatus="i">
	<TR>
	    <TD style="width:20px;"><INPUT onclick="" TYPE=checkbox NAME="RadioBtn" VALUE="${group.id}"></TD>
		<TD CLASS=standardText style="text-align: left;width:80px">${group.id}</TD>
		<TD CLASS=standardText style="text-align: left;word-break:break-all;width:150px" >
			<A HREF="/globalsight/ControlServlet?linkName=jobList&pageName=GROUPS&jobGroupId=${group.id}" oncontextmenu="contextForTab('${group.id}',event)">${group.name}</A></TD>
		<TD CLASS= standardText style="text-align: left;width:150px" >${group.project.getName()}</TD>
		<TD CLASS=standardText style="text-align: left;width:150px"} >${group.sourceLocale.getDisplayName()}</TD>
		<TD CLASS=standardText style="text-align: left;width:150px"} >${group.getCreateUser().getUserName()}</TD>
		<TD STYLE="padding-right: 10px;width:150px" CLASS=standardText ><fmt:formatDate value="${group.createDate}" pattern="MM/dd/yy"/></TD>
		<% if(isSuperAdmin){%>
		<TD CLASS=standardText style="text-align: left;width:150px"} >${group.getCreateUser().getCompanyName()}</TD>
		<%}%>
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
                <%=request.getAttribute(WebAppConstants.JOB_GROUP_PAGING_SCRIPTLET)%>
            </DIV>
         </TD>
     </TR>
</TABLE>
<!-- End Data Table  -->
</TD>
</TR>
</FORM>
<TR>
	<TD>
		<DIV ID="ButtonLayer" ALIGN="LEFT" >
			<br>
			    <amb:permission name="<%=Permission.JOBS_NEWGROUP%>" >
			        <INPUT TYPE="BUTTON" NAME=new VALUE="<%=bundle.getString("lb_new")%>..." onClick="submitForm('new');">
			    </amb:permission>
			    <amb:permission name="<%=Permission.JOBS_REMOVEGROUP%>" >
			        <INPUT TYPE="BUTTON" NAME=remove VALUE="<%=bundle.getString("lb_remove")%>" onClick="submitForm('remove');">
			    </amb:permission>
		</DIV>
	</TD>
</TR>
</BODY>
</HTML>