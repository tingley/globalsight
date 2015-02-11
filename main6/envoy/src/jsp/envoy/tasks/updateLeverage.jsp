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
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.everest.servlet.util.SessionManager"
   session="true"
%>

<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
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
    // URLs
    String cancelUrl = cancel.getPageURL();
    String updateUrl = done.getPageURL() + "&" + WebAppConstants.TASK_ACTION + 
        "=" + WebAppConstants.UPDATE_LEVERAGE;
    
	String strTaskId = (String) request.getAttribute(WebAppConstants.TASK_ID);
	Task task = ServerProxy.getTaskManager().getTask(Long.parseLong(strTaskId));
	List availableJobs = (ArrayList) request.getAttribute("availableJobs");
	int avialableJobSize = (availableJobs==null ? 0 : availableJobs.size());
	String disabled = (avialableJobSize > 0 ? "" : "disabled");
%>
<HTML>
<HEAD>
<!-- This JSP is envoy/tasks/taskDetail.jsp -->
<TITLE><%= title %></TITLE>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
var dirty = false;
var objectName = "";
var guideNode = "myActivities";
var needWarning = false;
var helpFile = "<%=helpFile%>";

function submitForm(formAction)
{
    if (formAction == "cancel") 
    {
        updateLeverageForm.action = '<%=cancelUrl%>';
        updateLeverageForm.submit();
    }
    else if (formAction == "update")
    {
        var ufj = document.getElementById("updateFromJobCheckBoxID").checked;
        var rrt = document.getElementById("reApplyReferenceTmsID").checked;
        if (ufj != true && rrt != true) {
            alert("<%=noOptionSelected%>");
            return false;
        }

        if (ufj == true) {
            // Selected jobs number should be 1-5.
            var selectedJobs = document.getElementById("selectJobs");
        	var intValue = 0;
            for(i=0; i<selectedJobs.length; i++) {
                if(selectedJobs.options[i].selected){
                	intValue += 1;
                }
            }
            if (intValue != 1) {
                alert("<%=selectedJobsNumber%>");
                return false;
            }

            // Tm penalty should be between [0,100)
            if (!checkInProgressTmPenalty())
            {
                return false;
            }
        }

        updateLeverageForm.action = '<%=updateUrl%>';
        updateLeverageForm.submit();
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
	function checkIsVaildPercent(percent) {
	    var submit = false;
		var i_percent = parseInt(percent);
		if(i_percent < 0 || i_percent >= 100) {
			submit = false;
		} else {
			submit = true;
		}
		
		return submit;
	}

}
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute; WIDTH: 800px; TOP: 108px">
<span class="mainHeading"><%=title%></span>
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
      <TD valign="top"><%=updateFromJobs%></TD>
      <TD>
          <input type="checkbox" id="updateFromJobCheckBoxID" name="updateFromJobCheckBoxName" class="standardText" <%=disabled%>/><br/>
          <select id="selectJobs"  name="selectJobs" style="width:300px" size="5" <%=disabled%>>
          <%
          long currentJobId = task.getJobId();
          String currentJobName = task.getJobName();
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
      </TD>
    </TR>
    <TR>
        <TD><%=inProgressTmPenalty%></TD>
        <TD><input type="text" value="0" size=1 maxlength=3 id="inProgressTmPenaltyID" name="inProgressTmPenaltyName" <%=disabled%>/></TD>
    </TR>
    <TR><TD colspan="2" height="6"></TD></TR>
    <TR>
      <TD><%=reApplyReferenceTMs%></TD>
      <TD><input type="checkbox" id="reApplyReferenceTmsID" name="reApplyReferenceTmsName" onclick="" class="standardText" />
    </TR>
  </TABLE>
  
  <BR/><INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ID="Cancel" onclick="submitForm('cancel');">&nbsp;
    <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_update")%>" ID="OK" onclick="submitForm('update');">  
</FORM>

</DIV>
</BODY>
</HTML>
