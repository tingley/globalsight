<%@page import="com.globalsight.everest.coti.util.COTIConstants"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cotijob.CotiJobComparator"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cotijob.CotiJobsManagement"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.everest.servlet.util.SessionManager,
     com.globalsight.everest.permission.PermissionGroup, 
     com.globalsight.everest.util.comparator.StringComparator,
     com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
     com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserComparator,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,         
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.util.edit.EditUtil,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle, java.util.Vector"
         session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cotiJobDetail" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cotiUpload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="allStatus" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-store");
	response.setDateHeader("Expires", 0);

    String DEFAULT_PARAM = "&jobListStart=0";
    Object param = request.getAttribute(CotiJobsManagement.JOB_LIST_START_PARAM);
    String startIndex = param == null ? DEFAULT_PARAM : "&jobListStart="+param;
    String thisSearch = (String) request.getAttribute("searchType");
    if (thisSearch == null)
        thisSearch = (String) session.getAttribute("searchType");

    ResourceBundle bundle = PageHandler.getBundle(session);
    String allStatusURL = allStatus.getPageURL()+ DEFAULT_PARAM;
    String detailsURL = cotiJobDetail.getPageURL();
    String downloadURL = self.getPageURL() + "&action=download";
    String deleteURL = self.getPageURL() + "&action=delete";
    String selfURL = self.getPageURL()+ DEFAULT_PARAM;
    String uploadCotiURL = cotiUpload.getPageURL();
    
    String title = bundle.getString("lb_coti_jobs");

    String refreshUrl = selfURL;
    
    String helperText = bundle.getString("helper_text_cotijobs");
    String badresults = "";
    
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    int numPerPage = 10;
    if (sessionMgr.getMyjobsAttribute("cotinumPerPage") != null)
        numPerPage = (Integer) sessionMgr.getMyjobsAttribute("cotinumPerPage");
    String selectedIndex = "0";
    if (numPerPage == 20)
    {
        selectedIndex = "1";
    } else if (numPerPage == 50)
    {
        selectedIndex = "2";
    }
    else if (numPerPage == 100)
    {
        selectedIndex = "3";
    }
    else if (numPerPage == 200)
    {
        selectedIndex = "4";
    }
%>
<html>
<head>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<title><%= bundle.getString("lb_coti_job")%></title>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "";
var helpFile = "<%= bundle.getString("help_coti_job")%>";

var checkBoxValue = "";

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
   
   if (typeof(ContextMenu) != "undefined" && ContextMenu != null)
   {
       ContextMenu.intializeContextMenu();
   }
   
   setButtonState();
   
   document.getElementById("numPerPage").selectedIndex = <%= selectedIndex%>;
}

function updateButtonState()
{
	var transIndexes = transSelectedIndex();

	if (transIndexes.length == 1)
	{
		JobForm.Download.disabled = false;
		JobForm.Delete.disabled = false;
		
		if (checkBoxValue != null && typeof(checkBoxValue) != "undefined" 
				&& checkBoxValue.indexOf("<%=COTIConstants.project_status_started%>") > 0)
		{
			JobForm.Create.disabled = false;
		}
		else
		{
			JobForm.Create.disabled = true;
		}
	}
	else if(transIndexes.length > 1)
	{
		JobForm.Download.disabled = false;
		JobForm.Delete.disabled = false;
		JobForm.Create.disabled = true;
	}
	else
	{
		JobForm.Download.disabled = true;
		JobForm.Delete.disabled = true;
		JobForm.Create.disabled = true;
	}
}

function setButtonState()
{
   updateButtonState();
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
					checkBoxValue = checkbox.value;
				}
			}
		} else {
			if (transCheckboxes.checked) {
				transSelectedIndex.push(0);
				checkBoxValue = transCheckboxes.value;
			}
		}
	}
	return transSelectedIndex;
}

function submitForm(buttonClicked, curJobId)
{
	if (buttonClicked == "UploadCoti")
	{
		JobForm.action = "<%= uploadCotiURL %>";
		JobForm.submit();
		return;
	}
	
   var transIndexes = transSelectedIndex();

   if (transIndexes.length == 0 && typeof curJobId == "undefined")
   {
      alert ("<%= bundle.getString("jsmsg_please_select_a_row") %>");
      return false;
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

   if (jobId == "" || jobId == " ")
   {
	   jobId = curJobId;
   }
   $("#checkedJobIds").val(jobId);
   
   if (buttonClicked == "Download")
   {
      JobForm.action = "<%=downloadURL%>&jobId=" + jobId;
      JobForm.submit();
      return;
   }
   else if (buttonClicked == "Delete")
   {
	   if (!confirm("Are you sure to delete all selected COTI Job(s)?"))
	   {
		   return;
	   }
	   JobForm.action = "<%=deleteURL%>&jobId=" + jobId;
	   JobForm.submit();
	   return;
   }
   else if (buttonClicked == "Create")
   {
	   JobForm.action = "/globalsight/ControlServlet?linkName=cotiJobDetail&pageName=cojs&jobId=" + jobId + "&fromJobs=true";
	   JobForm.submit();
	   return;
   }
   
   JobForm.submit();
}


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
	var baseUrl = "<%=allStatusURL%>";

	if(fromRequest)
	{
		window.location = baseUrl+"&fromRequest=true";
	}
	else
	{
		window.location = baseUrl
			+ "&npp="+$("#numPerPage").val();
	}
}

function onNumPerPageChanged()
{
	searchJob(false);
}

</script>

</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/administration/cotijob/cotiJobSort.jspIncl" %>
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

<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
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

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
<FORM NAME="JobForm" METHOD="POST">
<TR><TD>
    <DIV ID="PagingLayer" ALIGN="RIGHT" CLASS=standardText>
    <%=request.getAttribute(CotiJobsManagement.PAGING_SCRIPTLET)%>
    </DIV>
</TD></TR>

<TR><TD>
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" width="100%">
<input type="hidden" id="checkedJobIds" name="checkedJobIds" value=""/>
    <TR>
        <TD COLSPAN=3>

<!-- Data Table  -->
<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" id="list" CLASS="list" width="100%">
<!-- 
jobId
cotiProjectId
cotiProjectName
globalsightJobId
status
sourceLang
targetLang
creationDate
 -->
<COL> <!-- jobId -->
<COL> <!-- cotiProjectId -->
<COL WIDTH=130> <!-- cotiProjectName -->
<COL> <!-- globalsightJobId-->
<COL> <!-- status -->
<COL> <!-- sourceLang -->
<COL> <!-- targetLang -->
<COL> <!-- creationDate -->
<thead>
<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
<TD CLASS="headerCell" WIDTH="1%"><input type="checkbox" onclick="handleSelectAll()" id="selectAll" name="selectAll"/></TD>
    <TD CLASS="headerCell" WIDTH="4%"><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.JOB_ID%>" ><%=bundle.getString("lb_job_id")%></A><%=jobIdSortArrow%></TD>
    <TD CLASS="headerCell" width=7%><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.COTI_PORJECT_ID%>" ><%=bundle.getString("lb_COTI_project_id")%></A><%=cotiProjectIdSortArrow%></TD>
    <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.COTI_PORJECT_NAME%>" ><%=bundle.getString("lb_COTI_project_name")%></A><%=cotiProjectNameSortArrow%></TD>
    <TD CLASS="headerCell" width=15%><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.GLOBALSIGHT_JOB_ID%>" ><%=bundle.getString("lb_globalsight_job_id")%></A><%=globalsightJobIdSortArrow%></TD>
    <TD CLASS="headerCell" width=8%><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.STATUS%>" ><%=bundle.getString("lb_status")%></A><%=statusSortArrow%></TD>
    <TD CLASS="headerCell" width=15%><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.SOURCE_LOCALE%>" ><%=bundle.getString("lb_sourceLange")%></A><%=sourceLangSortArrow%></TD>
    <TD CLASS="headerCell" width=15%><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.TARGET_LOCALE%>" ><%=bundle.getString("lb_targetLange")%></A><%=targetLangSortArrow%></TD>
    <TD CLASS="headerCell" width=8%><A CLASS="sortHREFWhite" HREF="<%=selfURL + "&" + CotiJobsManagement.SORT_PARAM + "=" + CotiJobComparator.CREATION_DATE%>" ><%=bundle.getString("lb_creationDate")%></A><%=creationDateSortArrow%></TD>
</TR>
</thead>
<tbody>
<c:forEach items="${cotiJobVos}" var="jobVo" varStatus="i">
    <TR VALIGN=TOP STYLE="padding-top: 5px; padding-bottom: 5px;" BGCOLOR="<c:choose><c:when test="${i.count % 2 == 0}">#EEEEEE</c:when><c:otherwise>#FFFFFF</c:otherwise></c:choose>" CLASS=standardText>
    <TD><INPUT onclick="setButtonState()" TYPE=checkbox NAME=transCheckbox VALUE="jobId=${jobVo.jobId}&jobState=${jobVo.status}"></TD>
	<TD CLASS=standardText style="text-align: center;">${jobVo.jobId}</TD>
	<TD CLASS=standardText style="text-align: center;">${jobVo.cotiProjectId}</TD>
	<TD CLASS=standardText style="word-break:break-all" >	
	    <SCRIPT language="javascript">
	    if (navigator.userAgent.indexOf('Firefox') >= 0){
		    document.write("<DIV>");
		    }</SCRIPT>
		<B><A  CLASS="${jobVo.textType.replace("Text","HREF")}"  HREF="/globalsight/ControlServlet?linkName=cotiJobDetail&pageName=cojs&jobId=${jobVo.jobId}&fromJobs=true">${jobVo.cotiProjectName}</A></B>
		<SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("</DIV>")}</SCRIPT></TD>	 
	<TD CLASS=${jobVo.textType} >
	<SCRIPT language="javascript">
	    if (navigator.userAgent.indexOf('Firefox') >= 0){
		    document.write("<DIV>");
		    }
	    var gsjobId = "${jobVo.globalsightJobId}";
	    if (gsjobId == "N/A")
	    {
	    	document.write("N/A");
	    }
	    else
	    {
	    	document.write("<B><A CLASS=\"${jobVo.textType.replace("Text","HREF")}\"  " 
	    			+ "HREF=\"ControlServlet?linkName=jobDetails&pageName=ALLS&jobId=" 
	    					+ gsjobId + "&fromJobs=true\">" + gsjobId + " (${jobVo.globalsightJobStatus})</A></B>");
	    }
	    
	    if (navigator.userAgent.indexOf('Firefox') >= 0)
	    {document.write("</DIV>")}
	    </SCRIPT>
	</TD>
	<TD CLASS=${jobVo.textType} >${jobVo.status}</TD>
	<TD CLASS=${jobVo.textType} >${jobVo.sourceLang}</TD>
	<TD CLASS=${jobVo.textType} >${jobVo.targetLang}</TD>
	<TD CLASS=${jobVo.textType} >${jobVo.creationDate}</TD>
    </TR>
</c:forEach>
</tbody>
<tr><td><div id='restofjobs' style="display:none">
<c:forEach items="${otherJobIds}" var="otherJobId" varStatus="i">
<input type='checkbox' name=jobIdHidden value="jobId=${otherJobId}&jobState=notused">
</c:forEach>
</div></td></tr>
</TABLE>
<!-- End Data Table  -->
        </TD>
     </TR>
     <TR>
        <TD CLASS="standardText">
            <DIV ID="CheckAllLayer"  ALIGN="RIGHT" style="padding-top: 5px">
            	Display #:
		        <select id="numPerPage" class="filterSelect" onchange="onNumPerPageChanged()">
		           <option value="10">10</option>
		           <option value="20">20</option>
		           <option value="50">50</option>
                   <option value="100">100</option>
		           <option value="200">200</option>
		        </select>
		        &nbsp;
                <%=request.getAttribute(CotiJobsManagement.PAGING_SCRIPTLET)%>
            </DIV>
         </TD>
     </TR>
</TABLE>
</TD></TR>

<TR><TD>
<DIV ID="ButtonLayer" ALIGN="LEFT">
<br>
<INPUT TYPE="BUTTON" NAME=UploadCoti id="UploadCoti" VALUE="Upload COTI package..." onClick="submitForm('UploadCoti');">
<INPUT TYPE="BUTTON" NAME=Create id="Create" VALUE="<%=bundle.getString("lb_create_job")%>" onClick="submitForm('Create');">
<INPUT TYPE="BUTTON" NAME=Download id="Download" VALUE="<%=bundle.getString("lb_download")%>" onClick="submitForm('Download');">
<INPUT TYPE="BUTTON" NAME=Delete id="Delete" VALUE="<%=bundle.getString("lb_delete")%>" onClick="submitForm('Delete');">
</DIV>
<P id="statusMessage" CLASS="standardText" >&nbsp;</P>

</TD></TR>
</FORM>
</TABLE>

</div>
</body>
</html>