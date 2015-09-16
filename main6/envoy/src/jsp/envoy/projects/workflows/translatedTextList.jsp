<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.util.system.SystemConfigParamNames,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WordCountHandler, 
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowComparator,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WordCountHandler,
      com.globalsight.everest.webapp.WebAppConstants,
      java.util.ArrayList,
      java.util.List,
      java.util.Locale,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="back" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
ResourceBundle bundle = PageHandler.getBundle(session);
String title = bundle.getString("lb_detailed_translated_text");
String jobName = title + ": " + request.getAttribute("jobName");
String jobId = (String)request.getAttribute(WebAppConstants.JOB_ID);
String action = back.getPageURL() + "&action=wcBack";
if(jobId != null && jobId != ""){
	action += "&"+ WebAppConstants.JOB_ID + "=" + jobId;
}
%>

<!-- This JSP is envoy/projects/workflows/translatedTextList.jsp -->
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<style>
.myTableHeading{
	padding:12px 2px 2px 2px;
}

#fullbg {
    background-color: Gray;
    display:none;
    z-index:1;
    position:absolute;
    left:0px;
    top:0px;
    filter:Alpha(Opacity=30);
    /* IE */
    -moz-opacity:0.4;
    /* Moz + FF */
    opacity: 0.4;
}

#container {
    position:absolute;
    display: none;
    z-index: 2;
}
</style>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_translated_text")%>";
$(document).ready(function(){
	$("#translatedTextList tr:odd").css("background-color","#EEEEEE");
	var url ="${self.pageURL}&action=retrieveTranslatedText"
	$.getJSON(url,{
        jobId:${jobId}
	},function(data){
		$(data).each(function(i, item){
			var sourceId = item.sourceId;
    		var percent = item.percent;
    		 var obj = document.getElementById(sourceId);
    		 if(obj !=null){
       		if(percent<100){
       			obj.style.color = "red";
               	obj.innerHTML = percent + "%";
       		}
       		else{
       			obj.style.color = "black";
               	obj.innerHTML = percent + "%";
       		}}
		})
	});
});
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<amb:header title="<%=jobName%>" />


<p class="standardText"><%=bundle.getString("helper_text_detailed")%></p>
<form name="txForm" method="post" action="<%=action%>"> 
<table cellpadding="2" cellspacing="0" border="0" style="border:solid 1px slategray">
	<thead>
		<tr>
		    <td class="tableHeadingBasic myTableHeading" style="width:540px"><span class="whiteBold"><%=bundle.getString("lb_SourceFiles")%>&nbsp;&nbsp;&nbsp;&nbsp;</span></td>
		    <c:forEach items="${workflows}" var="item">
		   <td class="wordCountHeadingWhite myTableHeading" style="text-align:left;width:100px"><span class="whiteBold">${item.targetLocale }&nbsp;&nbsp;&nbsp;</span></td>
		   	</c:forEach>
		</tr>
	</thead>
	<tbody id="translatedTextList">
	<c:forEach items="${sourcePages}" var="item">
		<tr>
		<c:if test="${shortOrFullPageNameDisplay == 'full'}">
			<td style="width:540px" class="standardText">${item.displayPageName}</td>
		</c:if>
		<c:if test="${shortOrFullPageNameDisplay == 'short'}">
			<td style="width:540px" class="standardText">${item.shortPageName}</td>
		</c:if>
		
		<c:forEach items="${workflows}" var="item2">
			<td style="text-align:left">
			<span class="standardText" id="${item.id}_${item2.targetLocale.id}" style = "font-weight:600"></span>
			</td>
   		</c:forEach>
   		</tr>
	</c:forEach>
	</tbody>
</TABLE>

<BR><BR>  
   
<input type="submit" value='<%=bundle.getString("lb_back_to_job_details")%>'>
</FORM>
</DIV>
</BODY>
