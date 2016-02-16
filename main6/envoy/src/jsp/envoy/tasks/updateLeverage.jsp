<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="
      java.util.*,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.everest.servlet.util.SessionManager"
   session="true"
%>

<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelToTaskDetail" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelToJobDetail" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    // labels
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_update_leverage_title");
    String description = bundle.getString("lb_update_leverage_des");
    String updateFromJobs = bundle.getString("lb_update_from_jobs");
    String inProgressTmPenalty = bundle.getString("lb_update_from_jobs_penalty");
    String reApplyReferenceTMs = bundle.getString("lb_reapply_reference_tms");
    String selectedJobsNumber = bundle.getString("msg_selected_jobs_number");
    String noOptionSelected = bundle.getString("msg_no_update_leverage_option_selected");
    String helpFile = bundle.getString("help_update_leverage");
    String reTryMT = bundle.getString("lb_retry_mt");
    // URLs
    String selfUrl = self.getPageURL();
    String updateUrl = selfUrl + "&action=updateLeverage";
    String cancelToTaskDetailUrl = cancelToTaskDetail.getPageURL();
    String cancelToJobDetailUrl = cancelToJobDetail.getPageURL();
    String cancelUrl = null;

    String currentUserId = TaskHelper.getUser(request.getSession()).getUserId();
    // currentJobId & CurrentJobName
    long currentJobId = -1;
    String currentJobName = null;
    String strWfIds = (String) request.getAttribute("wfIds");
	String strTaskId = (String) request.getAttribute(WebAppConstants.TASK_ID);
	String fromWhere = null;
	if (strTaskId != null && !"".equals(strTaskId.trim()))
	{
		Task task = ServerProxy.getTaskManager().getTask(Long.parseLong(strTaskId));
		fromWhere = "fromTaskDetail";
		currentJobId = task.getJobId();
        currentJobName = task.getJobName();
	}
	else if (strWfIds != null && !"".equals(strWfIds.trim())) 
	{
	    StringTokenizer tokenizer = new StringTokenizer(strWfIds);
        if (tokenizer.hasMoreTokens()) {
            String wfId = tokenizer.nextToken();
            fromWhere = "fromJobDetail";
            Workflow tmpWf = ServerProxy.getWorkflowManager().getWorkflowById(Long.parseLong(wfId));
            currentJobId = tmpWf.getJob().getId();
            currentJobName = tmpWf.getJob().getJobName();
        }
	}
	String percentageUrl = selfUrl + "&action=getPercentage&jobId=" + currentJobId;

	// updateUrl & cancelUrl
	if ("fromTaskDetail".equals(fromWhere))
	{
        updateUrl += "&" + WebAppConstants.TASK_ID + "=" + strTaskId;
        cancelUrl = cancelToTaskDetailUrl;
	}
	else if ("fromJobDetail".equals(fromWhere))
	{
        updateUrl += "&wfIds=" + strWfIds;
        cancelUrl = cancelToJobDetailUrl;
	}

	List availableJobs = (ArrayList) request.getAttribute("availableJobs");
	int avialableJobSize = (availableJobs==null ? 0 : availableJobs.size());
	String disabled = (avialableJobSize > 0 ? "" : "disabled");
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script src="/globalsight/jquery/jquery.progressbar.js"></script>
<SCRIPT>
var dirty = false;
var objectName = "";
var guideNode = "myActivities";
var needWarning = false;
var helpFile = "<%=helpFile%>";

var prePercentage = 0;
var getPercentageUrl = "<%=percentageUrl%>";
var selectedJobsForUpdate = "";
//the Hierarchy css changed function
function setDisableTR(trId, isDisabled) {
	var trElem = document.getElementById(trId);
	var color;
	if (isDisabled) {
		color = "gray";
	} else {
		color = "black";
	}
	trElem.style.color = color;

	// Operate select elements
	var elems = trElem.getElementsByTagName("select");
	for ( var i = 0; i < elems.length; i++) {
		elems[i].disabled = isDisabled;
		elems[i].style.color = color;
	}
	
	// Operate text elements
	elems = trElem.getElementsByTagName("input");
	for ( var i = 0; i < elems.length; i++) {
		if ("text" == elems[i].type) {
			elems[i].disabled = isDisabled;
			elems[i].style.color = color;
		}
	}
}


$(document).ready(function() 
{
	//Hierarchy radio chaged
	var input = $("#selectJobsTable").find("input[type='radio']");
	 input.each(function(){
	 	$(this).click(function(){
	 		
	 		input.each(function(){setDisableTR($(this).val(),true)});
	 		setDisableTR($(this).val(),false);
	 	})
		    
	   } ) 
		
	
	$("#updateFromJobCheckBoxID").click(changeState);
	 //init state
	changeState();
	
	function changeState(){
		var isChecked = $("#updateFromJobCheckBoxID").attr("checked");
		if (isChecked && isChecked == 'checked') {
				input.attr("disabled",false);
				input.each(function(){setDisableTR($(this).val(),true); $(this).attr("checked",true);});
	 		setDisableTR("idTRJobNames",false);
		} else {
			
			input.attr("disabled",true);
			setDisableTR("idTRJobIds", true);
			setDisableTR("idTRJobNames", true);
		}
	}
	
	// Cilck "Update" button
    $("#update").click(function() {
    	selectedJobsForUpdate = "";
    	if (checkBeforeUpdate()) {
    		var updateFromJobsChecked = null;  
    		if ($("#updateFromJobCheckBoxID").attr("checked") == "checked"){
    			updateFromJobsChecked = "on";
    		}
    		var reApplyChecked = null;
			if ($("#reApplyReferenceTmsID").attr("checked") == "checked"){
    			reApplyChecked = "on";
    		}
			var reTryChecked = null;
			if($("#reTryMTID").attr("checked")=="checked"){
				reTryChecked = "on";
			}
    		var penalty = $("#inProgressTmPenaltyID").attr("value");
    		$.get('<%=updateUrl%>',
				{
    			  reApplyReferenceTmsName: reApplyChecked,
                  updateFromJobCheckBoxName: updateFromJobsChecked, 
				  selectJobs: selectedJobsForUpdate,
				  inProgressTmPenaltyName: penalty,
				  reTryMTName: reTryChecked,
				  userId: "<%=currentUserId%>"
				},
				function(data) {
					// Display the progress bar if is updating leverage.
					showHourglass(true);
					$("#updateLeverageProgressBar").progressBar();
					$("#updateLeverageProgressBar").show();
					setTimeout(udpateLeverageProgress, 500);
				}
    		);
    	}
    });
	
	// Click "Update from Jobs" checkbox 
	$("#updateFromJobCheckBoxID").click(function(){
		var isChecked = $("#updateFromJobCheckBoxID").attr("checked");
		if (isChecked && isChecked == 'checked') {
			$("#selectJobs").attr("disabled", false);
		} else {
			$("#selectJobs").attr("disabled", true);
		}
	});

	// Click "Cancel" button
	$("#cancel").click(function() {
		window.close();
	});

	function udpateLeverageProgress()
	{
		// On IE, it the url is no change, seems it will not execute the url really, 
		// so add a fake parameter "percentage" to cheat it in IE.
		var url = getPercentageUrl + "&fresh=" + Math.random();
		$.getJSON(url, function(data) {
			var per = data.updateLeveragePercentage;
			if (per != null && per > prePercentage) {
				prePercentage = per;
				$("#updateLeverageProgressBar").progressBar(per);
			}
			if (per < 100) {
				setTimeout(udpateLeverageProgress, 3000);
		    } else {
				prePercentage = 0;
				showHourglass(false);
	            setTimeout($("#cancel").attr("value", "Close"), 500);
			}
	    });
	}
	
	function showHourglass(flag)
	{
		if (true == flag) {
			$("#cancel").attr("disabled", true);
			$("#update").attr("disabled", true);
	        idBody.style.cursor = "wait";
		} else {
			$("#cancel").attr("disabled", false);
			$("#update").attr("disabled", false);
	        idBody.style.cursor = "default";
		}
	}
	
	function checkBeforeUpdate()
	{
	    var ufj = document.getElementById("updateFromJobCheckBoxID").checked;
	    var rrt = document.getElementById("reApplyReferenceTmsID").checked;
	    var rtm	= document.getElementById("reTryMTID").checked;
	    if (ufj != true && rrt != true && rtm != true) {
	        alert("<%=noOptionSelected%>");
	        return false;
	    }

	    if (ufj == true) {
	        var selectedJobs = document.getElementById("selectJobs");
	    	var intValue1 = 0;
	    	var intValue2 = 0;
	    	var idTag = false;
			selectedJobsForUpdate = "";//Initialize this to empty first
	        for(i=0; i<selectedJobs.length; i++) {
	            if(selectedJobs.options[i].selected){
	            	intValue1 += 1;
					if (selectedJobsForUpdate == ""){
						selectedJobsForUpdate = selectedJobs.options[i].value;
					} else {
						selectedJobsForUpdate += " " + selectedJobs.options[i].value;
					}
	            }
	        }
			//this just for add jobids and may be it will merge the up code
	        var list= $('input:radio[name="reportOn"]:checked').val();
	        if(list=="idTRJobIds") {
		        idTag =true;
    	 		selectedJobsForUpdate = "";
    	 		var uniqueTool=new Object();
    	 		var writJobs=$("#jobIds").val();
    	 		//very intrest if u note the 2 line beblow
	        	var arryJobs=writJobs.split(",");
	    		writJobs=jQuery.unique(arryJobs);
	    		
	    		for(i=0; i<selectedJobs.length; i++) {
		            $.each(writJobs, function(j,val) { 
							if(selectedJobs.options[i].value==val) {
								if(uniqueTool[val]) return true;
								uniqueTool[val]=true;
								intValue2 += 1;
								if (selectedJobsForUpdate == "") {
									selectedJobsForUpdate = val;
								} else {
									selectedJobsForUpdate += " " + val;
								}
							};
					});
					
		        }   	 		
    	 		
    	 	}
			
	        if (intValue1 < 1 && idTag == false) {
	            alert("<%=selectedJobsNumber%>");
	            return false;
	        }
	        if (intValue2 < 1&&idTag == true) {
		        alert("Invalid or empty Job ID(s).");
		        return false;
	        }

	        // Tm penalty should be between [0,100)
	        if (!checkInProgressTmPenalty())
	        {
	            return false;
	        }
	    }

	    return true;
	}

	// Check if TM penalty is valid.
	function checkInProgressTmPenalty()
	{
	    var penalty = document.getElementById('inProgressTmPenaltyID').value;

		if (penalty == null || ATrim(penalty)== "" 
		    || !isAllDigits(penalty) || !checkIsVaildPercent(penalty))
		{
			alert("<%=bundle.getString("msg_in_progress_tm_penalty_invalid")%>");
			return false;
		}

		return true;
	}

	// Tm penalty should be between [0,100)
	function checkIsVaildPercent(percent) 
	{
	    var submit = false;
		var i_percent = parseInt(percent);
		if(i_percent < 0 || i_percent >= 100) {
			submit = false;
		} else {
			submit = true;
		}
			
		return submit;
	}
});

function helpSwitch() 
{  
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}
</SCRIPT>

</HEAD>
<BODY id="idBody" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute;padding:12px 0px;">
	<span class="mainHeading"><%=title%></span>
	<span class="HREFBold" style="float: right;">
	  <a href="javascript:helpSwitch();"><%=bundle.getString("lb_help")%></a> |
	  <a href="javascript:window.close();"><%=bundle.getString("lb_close")%></a> 
	</span>
<BR/><BR/>
<table class="standardText" cellspacing="0" cellpadding="0" border="0">
<tbody><tr><td width="600"><%=description%></td></tr></tbody>
</table>
<br>

<FORM NAME="updateLeverageForm" METHOD="POST" action="">
  <input type="hidden" name="<%=WebAppConstants.TASK_ID%>" value="<%=strTaskId%>" />
  <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
    <THEAD>
      <COL align="right" valign="top" CLASS="standardText">
      <COL align="left"  valign="top" CLASS="standardText">
    </THEAD>
    <TR>
      <TD valign="top" align="LEFT"><%=updateFromJobs%></TD>
      <TD>
          <input type="checkbox" id="updateFromJobCheckBoxID" name="updateFromJobCheckBoxName" class="standardText" <%=disabled%>/><br/>
      </TD>
      <td class="standardText" vAlign="bottom">
       <tbody name="selectJobsTable" id="selectJobsTable">
     	
     
  		<tr id="idTRJobIds" style="color: black;">
       <td class="standardText" vAlign="bottom" style="padding-right: 25px;">
        <input name="reportOn" type="radio" CHECKED="" value="idTRJobIds" />Job IDs: 
       </td>
       <td>
        <input name="jobIds" id="jobIds" style="color: black;" type="text" />(e.g. ID1,ID2,ID3) 
       </td>
      </tr>

     <tr id="idTRJobNames">
     	<td  class="standardText">
        	<input name="reportOn" type="radio" CHECKED="" value="idTRJobNames" />Job Names: 
       	</td>
     	<td>
          
          <select id="selectJobs"  name="selectJobs" style="width:500px" size="10" multiple disabled>
          <%
          String tip = currentJobName + "(" + currentJobId + ")";
          String displayName = currentJobName + " (current job)";
          %>
              <option value="<%=currentJobId %>" title="<%=tip%>"><%=displayName%></option>
          <%
            if (availableJobs != null && availableJobs.size() > 0) 
            {
                Iterator jobsIter = availableJobs.iterator();
                while (jobsIter.hasNext())
                {
                    Job job = (Job) jobsIter.next();
                    String jobName = job.getJobName();
                    long jobId = job.getJobId();
                    String jobNameIDAsTitle = jobName + "(" + jobId + ")";
                    if (jobId != currentJobId) {
          %>
                    <option value="<%=jobId%>" title="<%=jobNameIDAsTitle%>"><%=jobName%></option>
          <%
                    }
                }
            }
          %>
          </select>
      </td>
     	 
     </tr>
     </tbody>
   	<td>
    <TR>
        <TD align="LEFT"><%=inProgressTmPenalty%></TD>
        <TD><input type="text" value="0" size=1 maxlength=3 id="inProgressTmPenaltyID" name="inProgressTmPenaltyName" <%=disabled%>/>%</TD>
    </TR>
    <TR><TD colspan="2" style="border:solid #ccc;border-width:0 0 1px 0;">&nbsp;</TD></TR>
     <TR>
      <TD style="padding-top: 15px; padding-bottom: 15px"><%=reApplyReferenceTMs%></TD>
      <TD style="padding-top: 15px; padding-bottom: 15px"><input type="checkbox" id="reApplyReferenceTmsID" name="reApplyReferenceTmsName" onclick="" class="standardText" />
    </TR>
     <TR><TD colspan="2" style="border:solid #ccc;border-width:0 0 1px 0;">&nbsp;</TD></TR>
       <TR>
      <TD style="padding-top: 15px; padding-bottom: 15px"><%=reTryMT%></TD>
      <TD style="padding-top: 15px; padding-bottom: 15px"><input type="checkbox" id="reTryMTID" name="reTryMTName" onclick="" class="standardText" />
    </TR>
  </TABLE>
  
  <div id="updateLeverageProgressBar" style="display:none"></div><BR/>

  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ID="cancel">&nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_update")%>" ID="update">  
</FORM>

</DIV>
</BODY>
</HTML>
