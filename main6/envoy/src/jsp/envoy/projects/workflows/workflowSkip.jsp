<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="com.globalsight.everest.servlet.util.SessionManager,com.globalsight.util.FormUtil,com.globalsight.everest.taskmanager.Task,com.globalsight.everest.taskmanager.TaskAssignee,com.globalsight.everest.webapp.WebAppConstants,com.globalsight.everest.webapp.javabean.NavigationBean,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,com.globalsight.everest.usermgr.UserInfo,com.globalsight.everest.util.system.SystemConfigParamNames,com.globalsight.everest.util.system.SystemConfiguration,java.util.Hashtable,java.util.Enumeration,java.util.ResourceBundle"
    session="true" 
%>

<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="skinbean" scope="request"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />

 <%
 	ResourceBundle bundle = PageHandler.getBundle(session);
 	SessionManager sessionMgr = (SessionManager) session
 			.getAttribute(WebAppConstants.SESSION_MANAGER);
 	String jobId = (String)request.getAttribute(JobManagementHandler.JOB_ID);
 	boolean b_isDell = false;
 	try {
 		SystemConfiguration sc = SystemConfiguration.getInstance();
 		b_isDell = sc
 				.getBooleanParameter(SystemConfigParamNames.IS_DELL);
 	} catch (Exception ge) {
 	}
 	String title = bundle.getString("lb_skip_full");
 	if (b_isDell)
 		title = bundle.getString("lb_saveAll");

 	//Button names
 	String saveBtn = bundle.getString("lb_save");
 	String cancelBtn = bundle.getString("lb_cancel");

 	//Urls of the links on this page
 	String doneUrl = save.getPageURL() + "&"
 			+ JobManagementHandler.SKIP_PARAM + "=skipActivities";
 	String cancelUrl = cancel.getPageURL();
 	if(jobId != null && jobId != ""){
 		doneUrl += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
 		cancelUrl += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
 	}

 	//DataskipActivities
 %>

<!-- This JSP is: envoy/projects/workflows/workflowSkip.jsp -->
<HTML>
<HEAD>
  <TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = false;
var helpFile = "<%=bundle.getString("help_workflows_skip")%>";
var guideNode = "myJobs";

function $(name) {
   return document.getElementById(name);
}

function $N(name) {
  return document.getElementsByName(name);
}

function verify(name) {
  var ch = $N(name);
  if(checked(ch)){
  	return true;
  }else {
	return false;
  }
}

function checked(obj) {
  for(var i = 0 ; i <  obj.length ; i++ ) {
	if(obj[i].checked) {
	  return true;
	}
  }

  return false;
}

function doOnload()
{
    loadGuides();

}

function getSelectedCheckBox()
{
	var selectedCheckBox = new Array();
	var allCheckbox = $N("workflowId");
	for(var i = 0; i < allCheckbox.length; i++)
	{
		if(allCheckbox[i].checked)
		{
			//Save the workflow id to the array
			selectedCheckBox[selectedCheckBox.length] = allCheckbox[i].id.substring(1);
		}
	}
	return selectedCheckBox;
}

function buildParams(selCheckBox)
{
	var params = "&";
	for(var i = 0; i < selCheckBox.length; i++)
	{
		var selectBoxName = "activity" + selCheckBox[i];
		params += "activity_" + selCheckBox[i] + "=";
		var obj = $N(selectBoxName)[0];
		var index = obj.selectedIndex;
		params += index;
//		params += "&";
//		params += "skipToActivity_" + selCheckBox[i] + "=";
//		var selectedObj = obj.options[index].value;
//		params += selectedObj;
		if(i != selCheckBox.length - 1 )
		{
			params += "&";
		}
	}
	return params;
}

function submitPage(button)
{
       if (button == "save")
       {
           if(verify('workflowId')){
	     if(confirm('<%=bundle.getString("jsmsg_workflow_skip_activity")%>')) {
	       var selectedCheckBox = getSelectedCheckBox();
	       var params = buildParams(selectedCheckBox);
	       workflowSkipForm.action = "<%=doneUrl%>" + params;
	       workflowSkipForm.submit();
	     }
	   }else {
	     alert('<%=bundle.getString("jsmsg_workflow_skip_activity_select")%>');
	     return;
	   }

       }
       else
       {
           workflowSkipForm.action = "<%=cancelUrl%>";
           workflowSkipForm.submit();
       }
}

function checkAll()
{
	var form = document.workflowSkipForm;
	var checkBoxObj = document.getElementById("wsfCheckAll");
	for (var i = 0; i < form.elements.length; i++)
	{
		if (form.elements[i].type == "checkbox" && !form.elements[i].disabled)
		{
			form.elements[i].checked = checkBoxObj.checked;
		}
	}
}

</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
 ONLOAD="doOnload();">

<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" />
<FORM NAME="workflowSkipForm" METHOD="POST"  >
<table border="0">
<tr>
<td>
  <TABLE border="0" cellspacing="0" cellpadding="5" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td nowrap width="5%"><input type="checkbox" id="wsfCheckAll" onclick="checkAll();">
      </td>
      <td nowrap width="40%">
	 <%=bundle.getString("lb_target_locale")%>
      </td>
      <td style="padding-left:7px" nowrap>
	 <%=bundle.getString("lb_activities_default_path")%>
      </td>
    </tr>

    <c:forEach var="vo" items="${skiplist}" varStatus="rowCounter">
      <c:choose>
      <c:when test="${rowCounter.count%2==0}">
      <tr style="padding-bottom:5px; padding-top:5px;" valign=top BGCOLOR="#EEEEEE" class="standardText">
      </c:when>
      <c:otherwise>
      <tr style="padding-bottom:5px; padding-top:5px;" valign=top BGCOLOR="#FFFFFF" class="standardText">
      </c:otherwise>
      </c:choose>
	<td nowrap>
	  <input type="checkbox" id="_${vo.workflowId}" name="workflowId" value="${vo.workflowId}" />
	</td>
	<td nowrap>
	  ${vo.targetLocale}
	</td>
	<td nowrap>
	  <select name="activity${vo.workflowId}" style="width:120">
	    <c:forEach var="activity" items="${vo.list}">
	      <option value="${activity.value}">${activity.key}</option>
	    </c:forEach>
	  </select>
	</td>
      </tr>
    </c:forEach>
    </table>
    </td>
    </tr>
	<tr>
      <td>
      <p>
	<INPUT TYPE="button" name="saveBtn" VALUE="<%=cancelBtn%>" onclick="submitPage('cancel');" >
	<INPUT TYPE="button" name="cancelBtn" VALUE="<%=saveBtn%>" onclick="submitPage('save');" >
	<p>
      </td>
    </tr>
    </table>
<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.SKIP_ACTIVITIES); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
</FORM>
</BODY>
</HTML>