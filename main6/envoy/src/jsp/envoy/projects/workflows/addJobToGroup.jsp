<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
    com.globalsight.everest.webapp.WebAppConstants,
    com.globalsight.everest.servlet.util.ServerProxy,
    com.globalsight.everest.jobhandler.JobGroup,
    com.globalsight.everest.servlet.util.SessionManager"
   session="true"
%>

<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancleToJobInProgress" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancleToJobReady" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	// labels
	ResourceBundle bundle = PageHandler.getBundle(session);
	String title = bundle.getString("lb_add_job_to_group");
	String description = bundle.getString("lb_add_job_to_group_des");
	String helpFile = bundle.getString("help_update_leverage");

	Date date = new Date();
	request.setAttribute("date", date);
	String pageState = (String) request.getAttribute("pageState");
	String errorProject = (String) request.getAttribute("errorProject");
	String errorSource = (String) request.getAttribute("errorSource");
	String jobListStart = (String) request.getAttribute("jobListStart");
	String jobIds = (String) request.getAttribute("jobIds");
	List<JobGroup> jobGroupList = (List<JobGroup>) request
			.getAttribute("jobGoupList");
	String selfUrl = self.getPageURL();
	// URLs
	String cancelUrl = null;
	if ("inprogress".equals(pageState))
	{
		cancelUrl = cancleToJobInProgress.getPageURL()+"&jobListStart="+jobListStart;
	}
	else if ("ready".equals(pageState))
	{
		cancelUrl = cancleToJobReady.getPageURL()+"&jobListStart="+jobListStart;
	}
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script src="/globalsight/jquery/jquery.progressbar.js"></script>
<SCRIPT>
var dirty = false;
var objectName = "";
var guideNode = "addJobToGroup";
var needWarning = false;
var helpFile = "<%=helpFile%>";

$(document).ready(function() 
{
	 $("#save").click(function() {
		var jobGroupId="";
		 var sel = addJobToGroupForm.jobGroupSelect;
		  if (sel.options.length > 0)
		  {
			  var count = 0;
			  for(var i = 0;i < sel.options.length;i++ )
			  {
			  	 if(sel.options[i].selected)
			  	 {
					 count++;
					 if(jobGroupId != ""){
						 jobGroupId += ",";
					 }
			  		jobGroupId += sel.options[i].value;
			  	 }  
			  }
			  if(count == 0)
			  {
				alert("<%=bundle.getString("lb_add_job_to_group_message")%>");
				return false;
			 }
		  }
		  else
		  {
			  alert("<%=bundle.getString("lb_add_job_to_group_no_data")%>");
			  return false;
		  }
		  getMessage(jobGroupId);
	    });
	 
	 $("#cancel").click(function() {
		window.opener.location.href="<%=cancelUrl%>";
		window.close();
	});
	 
	 function getMessage(jobGroupId){
		 var jobGroupSelect = document.getElementById("jobGroupSelect");
		 var saveButton =  document.getElementById("save");
		 var url = "<%=selfUrl%>&action=saveJobToGroup&jobIds=<%=jobIds%>&jobGroupId="+jobGroupId;
		$.getJSON(url, function(data) {
			var message = data.message;
			if(message == "success")
			{
				saveSuccess.style.display =  'block';
				saveFailed.style.display =  'none';
				jobGroupSelect.disabled = true;
				saveButton.disabled = true;
			}
			else if(message == "failed")
			{
				saveFailed.style.display =  'block';
				saveSuccess.style.display =  'none';
				jobGroupSelect.disabled = false;
				saveButton.disabled = false;
			}
		});
	 }
});
</SCRIPT>

</HEAD>
<BODY id="idBody" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute;padding:12px 0px;">
	<span class="mainHeading"><%=title%></span>
<BR/><BR/>
<table class="standardText" cellspacing="0" cellpadding="0" border="0">
	<tbody><tr><td width="600"><%=description%></td></tr></tbody>
</table>
<br>

<FORM NAME="addJobToGroupForm" METHOD="POST" action="">
  <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
    <THEAD>
      <COL align="right" valign="top" CLASS="standardText">
      <COL align="left"  valign="top" CLASS="standardText">
    </THEAD>
    <tbody name="selectGroup" id="selectGroup">
		<%
			if(errorProject != null || errorSource != null){
			%>
			<tr id="errorMessage" class="standardText" style="color: black;width:200px;">
	       		<td class="standardText" vAlign="top" style="padding-right: 25px;color:red;width:50px;"><b>Error :</b></td>
	       		<td class="standardText">
	       		<%
	       		if (errorProject != null)
	       		{
	       		%> 
	       			<p style="color: red;"><%=errorProject%></p> <%
 				}
		 		if (errorSource != null)
		 		{
 				%> 
 					<p style="color: red;"><%=errorSource%></p> <%
 				}
 				%>
 			</td>
      	</tr>
			<%
			}
		%>
    	
      <%
      	if(jobGroupList != null && jobGroupList.size() > 0){
      		%>
      		<tr id="jobGroupMessage" class="standardText" style="width:240px;">
      			<td class="standardText" vAlign="top" style="padding-right: 25px;width:70px;"><b>Job Group :</b></td>
	       		<td class="standardText" vAlign="bottom" style="padding-right: 25px;">
	       		<select name="jobGroupSelect" id="jobGroupSelect" size="6" style="width:140px;">
      	<%
      		for(JobGroup group : jobGroupList){
      			String name = group.getName();
      			long id = group.getId();
      		%>
			      <option value="<%= id%>" title="<%= name%>"><%= name%></option>
			  <%
      		}
      	%>
      	</select></td></tr>
      	<%
      	}else if(errorProject == null && errorSource == null && jobGroupList.size() == 0){
   		%>
   		<tr class="standardText" style="width:200px;">
   			<td class="standardText" vAlign="top" style="padding-right: 25px;color:red;width:50px;"><b>Message :</b></td>
   			<td class="standardText" vAlign="top" style="padding-right: 25px;color:red;">No group meet the conditions !</td>
   		</tr>
   		<%
      	}
      %>
    </tbody>
  </TABLE>
  <div id="message" >
  	<table>
	 <tr class="standardText" id="saveSuccess" name="saveSuccess" style="display:none;">
   	 	<td class="standardText" vAlign="top" style="padding-right: 25px;color:#0000CC;width:70px;"><b>Message :</b></td>
		 <td class="standardText" vAlign="top" style="padding-right: 25px;color:#0000CC;"><b>Saved Successfully !</b></td>
      </tr>
      <tr class="standardText" id="saveFailed" name="saveFailed" style="display:none;">
      	 <td class="standardText" vAlign="top" style="padding-right: 25px;color:red;width:70px;"><b>Message :</b></td>
   		 <td class="standardText" vAlign="top" style="padding-right: 25px;color:red;"><b>Save Failed !</b></td>
      </tr>
  	</table>
  </div>
  <BR/><BR/>

  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_close")%>" ID="cancel">&nbsp;
  <% 
  	if(errorProject == null && errorSource == null && jobGroupList != null && jobGroupList.size() > 0){
 %>
 	 <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" ID="save">  
 <%
  	}
  %>
</FORM>

</DIV>
</BODY>
</HTML>