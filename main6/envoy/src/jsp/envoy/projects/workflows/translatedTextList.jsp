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
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_detailed_translated_text");
    String jobName = title + ": " + request.getAttribute("jobName");
%>

<!-- This JSP is envoy/projects/workflows/translatedTextList.jsp -->
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
$(document).ready(function(){
	var url ="${self.pageURL}&action=retrieveTranslatedText"
	$.getJSON(url,{
        jobId:${jobId}
	},function(data){
		$(data).each(function(i, item){
			var sourceId = item.sourceId;
    		var percent = item.percent;
    		 var obj = document.getElementById(sourceId);
       		if(percent<100){
       			obj.style.color = "red";
               	obj.innerHTML = percent + "%";
       		}
       		else{
       			obj.style.color = "black";
               	obj.innerHTML = percent + "%";
       		}
		})
	});
});
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0">
<br/>
<amb:header title="<%=jobName%>" />
<br/>
<%=bundle.getString("helper_text_detailed")%>

<form name="txForm" method="post">
<table cellpadding="2" cellspacing="0" border="0" style="min-width:1024px;width:80%;border:solid 1px slategray">
		<thead>
			<tr CLASS="tableHeadingBasic" VALIGN="BOTTOM" style="height:30px">
			    <td class="standardText" style="width:40%;text-align:left"><%=bundle.getString("lb_SourceFiles")%></td>
			    <c:forEach items="${workflows}" var="item">
			    	<td class="standardText" style="text-align:left">${item.targetLocale }</td>
			   	</c:forEach>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${sourcePages}" var="item">
				<tr><td>${item.shortPageName}</td>
				<c:forEach items="${workflows}" var="item2">
	   				<td class="standardText" style="text-align:left" id="${item.id}_${item2.targetLocale.id}"></td>
	   		</c:forEach></tr>
		</c:forEach>
	 	
		</tbody>
</TABLE>
</FORM>
</DIV>
</BODY>
