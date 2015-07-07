<%@page import="com.globalsight.util.edit.EditUtil"%>
<%@page
	import="com.globalsight.cxe.entity.systemActivity.LoginAttemptConfig"%>
<%@page
	import="com.globalsight.everest.foundation.SearchCriteriaParameters"%>
<%@page
	import="com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         java.util.ResourceBundle,
         java.util.Date,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.CreateRequestComparator,
         java.util.Enumeration"
	session="true"%>
<jsp:useBean id="self"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<jsp:useBean id="importing"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<jsp:useBean id="exporting"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<jsp:useBean id="offline"
	class="com.globalsight.everest.webapp.javabean.NavigationBean"
	scope="request" />
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();
    String importingUrl = importing.getPageURL();
    String exportingUrl = exporting.getPageURL();
    String jobCreationStatus = bundle
            .getString("lb_job_creation_status");
    String offlineUrl = offline.getPageURL();
    
	String saveURL = self.getPageURL() + "&action=save";
	String cancelURL = self.getPageURL() + "&action=cancel";
    
    
    LoginAttemptConfig loginConfig = (LoginAttemptConfig)request.getAttribute("config");
    List<String> blockedIps = (List<String>) request.getAttribute("blockedIps");
    List<String> exemptIps = (List<String>) request.getAttribute("exemptIps");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=bundle.getString("lb_login_block_status")%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript"
	SRC="/globalsight/includes/formvalidation.js"></SCRIPT>
<script type="text/javascript"
	src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<link rel="STYLESHEET" type="text/css"
	href="/globalsight/includes/taskList.css">
<script>
            var needWarning = false;
            var objectName = "";
            var guideNode = "import";
            var helpFile = '<%=bundle.getString("help_login_config_requests")%>';
            
            function cancelForm() 
            {
            	loginConfigForm.action = "<%=cancelURL%>";
            	loginConfigForm.submit();
            }
            
            function validateMaxTime()
            {
            	var tmp = $('#maxTime').val();
	       		tmp = $.trim(tmp);
	       		
	       		var re = /^[1-9]+[0-9]*]*$/; 
	            if (!re.test(tmp))
	            	return false;
	       		
	       		tmp=Number(tmp);
	       		if(!tmp || tmp<5 || tmp>100){	       			
            		return false;
	       		}
	       		
	       		return true;
            }
            
            
            function validateBlockTime()
            {
            	var tmp = $('#blockTime').val();
	       		tmp = $.trim(tmp);
	       		
	       		if (tmp == "0")
	       			return true;
	       		
	       		var re = /^[1-9]+[0-9]*]*$/;
	            if (!re.test(tmp))
	            	return false;
	       		
	       		
	       		tmp=Number(tmp);
       			if(!tmp || tmp<30 || tmp>999999){	       			
            		return false;
	       		}
            
                return true;
            }
            
            function saveForm()
            {
	       		if(!validateMaxTime()){
	       			alert("<%=EditUtil.toJavascript(bundle.getString("msg_max_error"))%>");
	       			loginConfigForm.maxTime.focus();
            		return;
	       		 }
	       		 
	       		if (!validateBlockTime()) {
	       			alert("<%=EditUtil.toJavascript(bundle.getString("msg_block_time_error"))%>");
	       			loginConfigForm.blockTime.focus();
            		return;
	       		}
            		
            	loginConfigForm.action = "<%=saveURL%>";
            	$("#exemptIps").find("option").attr("selected", true);
            	$("#blockIps").find("option").attr("selected", true);
            	
            	loginConfigForm.submit();
            }
            
            function deleteExemptIp() {
            	if ($("#exemptIps").find("option:selected").length == 0) {
            		alert("<%=EditUtil.toJavascript(bundle.getString("msg_select_exempt_delete"))%>");
            		return;
            	}
            	
            	if (confirm("<%=EditUtil.toJavascript(bundle.getString("msg_remove_exempt_ip"))%>")){
            		$("#exemptIps").find("option:selected").remove();  
            	}
            }

            function addExemptIp() {
            	var content = $("#exemptIp").val();
            	content = $.trim(content);
            	if(content == '') {
            		alert("<%=EditUtil.toJavascript(bundle.getString("msg_exempt_ip_null"))%>");
            		loginConfigForm.exemptIp.focus();
                    return;
            	}
            	
            	if (!validateIP(content)) {
            		alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_invalid_ip"))%>");
            		loginConfigForm.exemptIp.focus();
                    return;
            	}
            	
            	var ips =$("#exemptIps").find("option");
            	for (var i = 0; i < ips.length; i++) {
            		if (content == ips.get(i).text) {
            			alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_exist_ip"))%>");
            			loginConfigForm.exemptIp.focus();
            			return;
            		}
            	}
            	
            	$("#exemptIps").append("<option value='" + content + "'>" + content + "</option>");
            	$("#exemptIp").val("");
            	loginConfigForm.exemptIp.focus();
            }
            
            function updateEnable() {
            	if($("#enable").is(':checked')){
            	    $("#blockTime").removeAttr("disabled");
            	    $("#maxTime").removeAttr("disabled");
             	    $("#blockIps").removeAttr("disabled");
            	    $("#exemptIp").removeAttr("disabled");
            	    $("#addExempt").removeAttr("disabled");
            	    $("#exemptIps").removeAttr("disabled");
            	    $("#deleteExempt").removeAttr("disabled");
           		} else {
           			$("#blockTime").attr("disabled", "disabled");
            	    $("#maxTime").attr("disabled", "disabled");
            	    $("#blockIps").attr("disabled", "disabled");
            	    $("#exemptIp").attr("disabled", "disabled");
            	    $("#addExempt").attr("disabled", "disabled");
            	    $("#exemptIps").attr("disabled", "disabled");
            	    $("#deleteExempt").attr("disabled", "disabled");
               }
            }
            
            function validateIP(what) 
            {
            	if(what.search(/^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/) == -1)
            		return false;
                
            	return true;
            }
        </script>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
	MARGINHEIGHT="0" onload="updateEnable()">
	<%@ include file="/envoy/common/header.jspIncl"%>
	<%@ include file="/envoy/common/navigation.jspIncl"%>
	<%@ include file="/envoy/wizards/guides.jspIncl"%>
	<STYLE>
.list {
	border: 1px solid<%=skin.getProperty("skin.list.borderColor")%>;
}

.headerCell {
	padding-right: 10px;
	padding-top: 2px;
	padding-bottom: 2px;
}
</STYLE>
	<DIV ID="contentLayer"
		STYLE="POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
		<SPAN CLASS="mainHeading"> <%=bundle.getString("lb_system_activities")%></SPAN>
		<br> <span class="standardText"><br><%=bundle.getString("lb_system_activities_help")%></span>
		<div
			style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
			<table cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="tableHeadingListOff"><img
						src="/globalsight/images/tab_left_gray.gif" border="0" /> <a
						class="sortHREFWhite" href="<%=importingUrl%>"> <%=bundle.getString("lb_job_creation_status")%></a>
						<img src="/globalsight/images/tab_right_gray.gif" border="0" /></td>
					<td width="2"></td>
					<td class="tableHeadingListOff"><img
						src="/globalsight/images/tab_left_gray.gif" border="0" /> <a
						class="sortHREFWhite" href="<%=exportingUrl%>"> <%=bundle.getString("lb_job_export_status")%></a>
						<img src="/globalsight/images/tab_right_gray.gif" border="0" /></td>
					<td width="2"></td>
					<td class="tableHeadingListOff"><img
						src="/globalsight/images/tab_left_gray.gif" border="0" /> <a
						class="sortHREFWhite" href="<%=offlineUrl%>"> <%=bundle.getString("lb_offline_upload_status")%></a>
						<img src="/globalsight/images/tab_right_gray.gif" border="0" /></td>
					<td width="2"></td>
					<td class="tableHeadingListOn"><img
						src="/globalsight/images/tab_left_blue.gif" border="0" /> <a
						class="sortHREFWhite" href="<%=selfUrl%>"> <%=bundle.getString("lb_login_block_status")%></a>
						<img src="/globalsight/images/tab_right_blue.gif" border="0" /></td>
				</tr>
			</table>
		</div>

		<div class="standardText">
			<form name="loginConfigForm" method="post" action="">
				<div style="padding-top: 10px;">
					<input type="checkbox" name="enable" id="enable" value="true"
						<%=loginConfig.isEnable() ? "checked" : ""%>
						onclick="updateEnable()" />
					<%=bundle.getString("lb_login_config_enable")%>
				</div>
				<div style="padding: 20px;">
					<%=bundle.getString("lb_login_config_max")%><input type="text"
						name="maxTime" id="maxTime" value="<%=loginConfig.getMaxTime()%>">
					<br>
					<br>
					<%=bundle.getString("lb_login_config_block")%>
					<input type="text" name="blockTime" id="blockTime"
						value="<%=loginConfig.getBlockTime()%>">
					<%=bundle.getString("lb_minutes_login")%>
				</div>

				<div style="padding: 20px; width: 400px; float: left">
					<%=bundle.getString("lb_block_ip")%><br> 
					<select
						name="blockIps" multiple="multiple" id="blockIps" size="15"
						style="width: 100%; height: 332px; margin-top: 10px;">
						<%
							for (String ip : blockedIps) {
						%>
						<option value='<%=ip%>'><%=ip%></option>
						<%
							}
						%>
					</select>
				</div>

				<div style="padding: 20px; width: 400px; float: left">
					<%=bundle.getString("lb_exempt_ip")%>
					<span class="errorMsg" id="ipMsg">&nbsp;</span><br> <input
						type="text" id="exemptIp" name="exemptIp"
						style="margin-top: 10px; width: 330px;"> <input
						type="button" style="margin-left: 10px;"
						value=" <%=bundle.getString("lb_add")%>" id="addExempt"
						onclick="addExemptIp()"> 
					<select name="exemptIps"
						multiple="multiple" id="exemptIps" size="15"
						style="width: 100%; height: 300px; margin-top: 10px; margin-bottom: 10px;">
						<%
							for (String ip : exemptIps) {
						%>
						<option value='<%=ip%>'><%=ip%></option>
						<%
							}
						%>
					</select> <input type="button" id="deleteExempt"
						value=" <%=bundle.getString("lb_delete")%>"
						onclick="deleteExemptIp()">
				</div>

				<div style="clear: both;">
					<input type="button" value=" <%=bundle.getString("lb_save")%>"
						onclick="saveForm()"> <input type="button"
						value=" <%=bundle.getString("lb_cancel")%>" onclick="cancelForm()">
				</div>
			</form>
		</DIV>
</BODY>
</HTML>
