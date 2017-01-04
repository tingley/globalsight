<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.administration.company.Select,
            com.globalsight.everest.workflow.ScorecardData,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.AddSourceHandler,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            java.text.MessageFormat,
            com.globalsight.util.StringUtil,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobComments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobReports" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowActivities" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exportError" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowImportError" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editPages" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="addWF" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rateVendor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="wordcountList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="assign" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="skip" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="estimatedCompletionDate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="estimatedTranslateCompletionDate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="addSourceFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="updateLeverage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCosts" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetailsPDFs" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%! 
	//colors to use for the table background
	private static final String WHITE_BG         = "#FFFFFF";
	private static final String LT_GREY_BG       = "#EEEEEE";
	// Toggles the background color of the rows used between WHITE and LT_GREY
	private static String toggleBgColor(int p_rowNumber)
	{
	    return p_rowNumber % 2 == 0 ? WHITE_BG : LT_GREY_BG;  
	}
%>
<%
	//jobSummary child page needed started.
   ResourceBundle bundle = PageHandler.getBundle(session);
   String jobCommentsURL = jobComments.getPageURL() + "&jobId=" + request.getAttribute("jobId");
   SessionManager sessionMgr =
	      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
   List<Select> categoryList = (List<Select>)sessionMgr.getAttribute("categoryList");
   List<ScorecardData> scorecardDataList = (List<ScorecardData>)sessionMgr.getAttribute("scorecardDataList");
   HashMap<String, String> tmpScoreMap = (HashMap<String, String>)sessionMgr.getAttribute("tmpScoreMap");
   HashMap<String, String> avgScoreMap = (HashMap<String, String>)sessionMgr.getAttribute("avgScoreMap");
   int rowNum = 0;
   
   PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
   String disableInput = "";
   if(!perms.getPermissionFor(Permission.EDIT_SCORECARD))
   {
	   disableInput = "disabled";
   }
//jobSummary child page needed end.
%>
<html>
<head>
<title><%=bundle.getString("lb_scorecard")%></title>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<title><%=bundle.getString("lb_workflows")%></title>
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

.comment {
  position: absolute;
  visibility: hidden;
  width: 400px;
  background-color: lightgrey;
  layer-background-color: lightgrey;
  border: 2px outset white;
}
</style>
<script   type="text/javascript">
$(document).ready(function(){
$("#lb_spelling_grammar").attr("title","<%=bundle.getString("lb_spelling_grammar_title")%>");
$("#lb_consistency").attr("title","<%=bundle.getString("lb_consistency_title")%>");
$("#lb_style").attr("title","<%=bundle.getString("lb_style_title")%>");
$("#lb_terminology").attr("title","<%=bundle.getString("lb_terminology_title")%>");
})
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()"; onunload="closeOpenedWindow();"; id="idBody"  class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer"  class="standardText" style="position: absolute; z-index: 9; top: 108px; left: 20px; right: 20px;">
<c:if test="${isUpdatingWordCounts}">
<!-- for UpdateWordCountsProgressBar -->
	<div id="fullbg"></div>
	<div id="container">
		<div id="updateWordCountsProgressBar"></div>
	</div>
</c:if>
<div id="includeSummaryTabs">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>
<p CLASS="standardText"><%=bundle.getString("lb_dqf_scorecard_allowed") %></p>
<form METHOD="post" name="scorecardForm" action="/globalsight/ControlServlet?linkName=jobScorecard&pageName=SCORECARD&jobId=${jobId}&action=updateScorecard">
<input type="hidden" name="savedWorkflowId" id="savedWorkflowId" value="">
<TABLE CLASS="standardText" CELLSPACING="0" CELLPADDING="2" style="border:solid 1px slategray;">
<TR CLASS="tableHeadingBasic">
    <TD style="border-right: #FFFFFF 1px solid;padding-top: 8px; padding-bottom: 8px;width:100px;text-align:center"><%=bundle.getString("lb_target_locale") %></TD>
    <% for(Select category: categoryList){%>
    <TD style="border-right: #FFFFFF 1px solid;text-align:center;max-width:150px;word-break: break-all"><div id=<%=category.getKey()%> ><%=category.getValue()%></div></TD>
    <%}%>
    <TD style="border-right: #FFFFFF 1px solid;width:50px;text-align:center;"><%=bundle.getString("lb_avg") %></TD>
    <TD style="border-right: #FFFFFF 1px solid;width:260px;text-align:center;"><%=bundle.getString("lb_dqf_comments") %></TD>
    <%if(disableInput.equals("")){ %>
    <TD></TD>
	<%} %>
</TR>
<%for(ScorecardData scorecardData: scorecardDataList) {%>
	<TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
	<TD align="center" valign="middle"><%=scorecardData.getLocaleDisplayname()%></TD>
	<% if(disableInput.equals("")) {%>
		<% for(Select category: categoryList){%>
	   <TD align="center" valign="middle">
	    <input class="scores hidden hidden<%=scorecardData.getWorkflowId()%>" style="width:25px" id="<%=scorecardData.getWorkflowId()%>.<%=category.getValue()%>" 
	    	name="<%=scorecardData.getWorkflowId()%>.<%=category.getValue() %>" value="<%=tmpScoreMap.get(scorecardData.getWorkflowId()+"."+category.getValue()) %>"/>
	    <span class="show<%=scorecardData.getWorkflowId()%>"><%=tmpScoreMap.get(scorecardData.getWorkflowId()+"."+category.getValue()) %></span>
	   </TD>
	   <%}%>
	   <TD align="center" valign="middle"><%=scorecardData.getAvgScore() %></TD>
	   <TD valign="middle" style="word-break: break-all" width="260px">
		   <TEXTAREA class="scoreComments hidden hidden<%=scorecardData.getWorkflowId()%>" maxlength="495" id="<%=scorecardData.getWorkflowId()%>.scoreComment" 
			 name="<%=scorecardData.getWorkflowId()%>.scoreComment" cols="29" style="resize: none;height:50px"><%=scorecardData.getScoreComment() %></TEXTAREA>
		   <span class="show<%=scorecardData.getWorkflowId()%>">
			<%if(scorecardData.getScoreComment().length() > 106){ %>
				<xmp style="white-space:pre-wrap; word-wrap:break-word;margin-bottom:1px"><%=scorecardData.getScoreComment().substring(0,105) %></xmp>
			 	<div onclick="javascript:showComment('comment<%=scorecardData.getWorkflowId()%>');" style="cursor:pointer">[more...]</div>
		        <div id="comment<%=scorecardData.getWorkflowId()%>" class="comment"><%=scorecardData.getScoreComment() %><div onclick="closeComment('comment<%=scorecardData.getWorkflowId()%>');"><span style="cursor: pointer; color:blue">[Close]</span></div></div>
			<%}else{ %>
				<xmp style="white-space:pre-wrap; word-wrap:break-word;"><%=scorecardData.getScoreComment()%></xmp>
			<%} %>
			</span>
		</TD>
		<TD>
		<input class="hidden hidden<%=scorecardData.getWorkflowId()%>" type="button" value="Save" id="save<%=scorecardData.getWorkflowId() %>" onclick="submitScorecardForm(<%=scorecardData.getWorkflowId() %>)"/>
		<input class="show<%=scorecardData.getWorkflowId()%>" type="button" value="Edit" onclick="editScore(<%=scorecardData.getWorkflowId() %>)"/>
		</TD>
	<%}else{ %>
		<% for(Select category: categoryList){%>
	    <TD align="center" valign="middle">
			<%=tmpScoreMap.get(scorecardData.getWorkflowId()+"."+category.getValue()) %>
	    </TD>
	    <%}%>
		<TD align="center" valign="middle"><%=scorecardData.getAvgScore() %></TD>
		<TD valign="middle" style="word-break: break-all" width="260px">
			<%if(scorecardData.getScoreComment().length() > 106){ %>
				 <xmp style="white-space:pre-wrap; word-wrap:break-word;margin-bottom:1px"><%=scorecardData.getScoreComment().substring(0,105) %></xmp>
				 <div onclick="javascript:showComment('comment<%=scorecardData.getWorkflowId()%>');" style="cursor:pointer">[more...]</div>
		         <div id="comment<%=scorecardData.getWorkflowId()%>" class="comment"><%=scorecardData.getScoreComment() %><div onclick="closeComment('comment<%=scorecardData.getWorkflowId()%>');"><span style="cursor: pointer; color:blue">[Close]</span></div></div>
			<%}else{ %>
				 <xmp style="white-space:pre-wrap; word-wrap:break-word;"><%=scorecardData.getScoreComment()%></xmp>
			<%} %>
		</TD>
	<%} %>
	</TR>
<%} %>
<TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
	<TD align="center" valign="middle" style="border-top: #0C1476 1px solid;"><%=bundle.getString("lb_avg") %></TD>
	<% for(Select category: categoryList){%>
    <TD align="center" valign="middle" style="border-top: #0C1476 1px solid;"><%=StringUtil.isEmpty(avgScoreMap.get(category.getValue())) ? "--" : avgScoreMap.get(category.getValue()) %></TD>
    <%}%>
	<TD align="center" valign="middle" style="border-top: #0C1476 1px solid;"><%=StringUtil.isEmpty(avgScoreMap.get("avgScore")) ? "--" : avgScoreMap.get("avgScore") %></TD>
	<TD align="center" valign="middle" style="border-top: #0C1476 1px solid;">&nbsp;</TD>
	<%if(disableInput.equals("")){ %>
    <TD align="center" valign="middle" style="border-top: #0C1476 1px solid;">&nbsp;</TD>
	<%} %>
</TR>
</TABLE>
<br>
</form>
<div class="standardText"><%=bundle.getString("lb_dqf_evaluation") %></div>
<br>
<form id="dqfForm" name="dqfForm" method="post" action="/globalsight/ControlServlet?linkName=jobScorecard&pageName=SCORECARD&jobId=${jobId}&action=updateDQF">
  <input type="hidden" name="currentWfId" id="currentWfId" value="">
  <table id="dqfTable" name="dqfTable" CELLSPACING="0" CELLPADDING="2" style="border:solid 1px slategray;" class="standardText">
    <tr CLASS="tableHeadingBasic">
      <td style="text-align:center;border-right: #FFFFFF 1px solid;padding-top: 8px; padding-bottom: 8px;width:100px;"><%=bundle.getString("lb_target_locale") %></td>
      <td style="text-align:center;border-right: #FFFFFF 1px solid;padding-top: 8px; padding-bottom: 8px;width:100px;"><%=bundle.getString("lb_dqf_fluency_only") %></td>
      <td style="text-align:center;border-right: #FFFFFF 1px solid;padding-top: 8px; padding-bottom: 8px;width:100px;"><%=bundle.getString("lb_dqf_adequacy_only") %></td>
      <td style="text-align:center;border-right: #FFFFFF 1px solid;padding-top: 8px; padding-bottom: 8px;width:100px;"><%=bundle.getString("lb_dqf_comments") %></td>
      <td style="text-align:center;border-right: #FFFFFF 1px solid;padding-top: 8px; padding-bottom: 8px;width:100px;"></td>
    </tr>
    <tbody>
      <c:forEach var="data" items="${dqfData}">
        <tr BGCOLOR="<%=toggleBgColor(rowNum++)%>">
          <td style="text-align:center" class="standardText">
            <c:out value="${data.targetLocale}" />
          </td>
          <td style="text-align:left;width:150px;" class="standardText">
            <input type="hidden" id="fluency${data.workflowId}" name="fluency${data.workflowId}" value="${data.fluency}" />
            <div id="fluencySet${data.workflowId}">
                <c:out value="${data.fluency}" />
            </div>
          </td>
          <td style="text-align:left;width:100px;" class="standardText">
            <input type="hidden" id="adequacy${data.workflowId}" name="adequacy${data.workflowId}" value="${data.adequacy}" />
            <div id="adequacySet${data.workflowId}">
                <c:out value="${data.adequacy}" />
            </div>
          </td>
          <td style="text-align:left;width:450px;" class="standardText">
            <input type="hidden" id="dqfCommentData${data.workflowId}" name="dqfCommentData${data.workflowId}" value="${data.comment}" />
            <div id="dqfComment${data.workflowId}"">
              <c:out value="${data.comment}" />
            </div>
          </td>
          <td style="text-align:center">
		    <input id="saveDQFBtn${data.workflowId}" type="button" value="Save" onclick="saveDQF(${data.workflowId})" style="text-align:center;vertical-align:middle;display:none;"/>
		    <input id="editDQFBtn${data.workflowId}" type="button" value="Edit" onclick="editDQF(${data.workflowId})"/>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</form>
</div>

<script src="/globalsight/jquery/jquery.progressbar.js"></script>
<script src="/globalsight/envoy/projects/workflows/jobDetails.js"></script>
<script type="text/javascript">
var notSave = false;
var needWarning = false;
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_scorecard")%>";

$(document).ready(function(){
	$("#jobScorecardTab").removeClass("tableHeadingListOff");
	$("#jobScorecardTab").addClass("tableHeadingListOn");
	$("#jobScorecardTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobScorecardTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");

	$(".scores").each(function()
	{
		var str=$(this).val();
		if (str != "1" && str != "2" && str != "3" && str != "4" && str != "5") 
		{
			$(this).val("");
		}
	})

	$(".hidden").each(function(){
		$(this).hide();
	})
})

function editDQF(index) {
    if (notSave) {
        alert("Please save current DQF data and then edit another one.");
        return false;
    }
    var $fluency = $("#fluencySet" + index);
    
    var str = "<select id='fluencyScore' name='fluencyScore'>";
    <c:forEach var="fluency" items="${fluencyCategories}">
      str += "<option value='${fluency}'>${fluency}</option>";
    </c:forEach>
    str += "</select>";
    $fluency.html("").append(str);
    $("#fluencyScore").val($("#fluency" + index).val());
    
    var $adequacy = $("#adequacySet" + index);
    str = "<select id='adequacyScore' name='adequacyScore'>";
    <c:forEach var="adequacy" items="${adequacyCategories}">
      str += "<option value='${adequacy}'>${adequacy}</option>";
    </c:forEach>
    str += "</select>";
    $adequacy.html("").append(str);
    $("#adequacyScore").val($("#adequacy" + index).val());
    
    var $dqfComment = $("#dqfComment" + index);
    $dqfComment.html("");
    $dqfComment.append("<textarea id='dqfComment' name='dqfComment' rows=3 cols=50>" + $("#dqfCommentData" + index).val() + "</textarea>");
    
    $("#saveDQFBtn" + index).css("display", "");
    $("#editDQFBtn" + index).css("display", "none");
    
    notSave = true;
}

function saveDQF(index) {
    notSave = false;
    $("#currentWfId").val(index);
    $("#dqfForm").submit();
}

function editScore(workflowId)
{
	$(".hidden"+workflowId).each(function(){
		$(this).show();
	})

	$(".show"+workflowId).each(function(){
		$(this).hide();
	})
}

function showComment(id)
{
    elem = document.getElementById(id);
    elem.style.visibility = "visible";
}
function closeComment(id)
{
    elem = document.getElementById(id);
    elem.style.visibility = "hidden";
}

function submitScorecardForm(workflowId)
{
	$("#savedWorkflowId").val(workflowId);
	var correctScoreInput = true;
	var correctCommentInput = true;
	var length = (workflowId+"").length + 1;
	$(".scores").each(function(){
		var str=$(this).val();
		var id = $(this).attr("id");
		if(id.substr(0,length) == workflowId + ".")
		{
			if (str != "1" && str != "2" && str != "3" && str != "4" && str != "5") {
				correctScoreInput = false;
			}
		}
	})
	if(!correctScoreInput)
	{
		alert("Invalid score, only 1, 2, 3, 4, 5 are allowed.");
		return;
	}

	$(".scoreComments").each(function(){
		var str=$(this).val();
		var id = $(this).attr("id");
		if(id.substr(0,length) == workflowId + ".")
		{
			if(str == "")
			{
				correctCommentInput = false;
			}
		}
	})

	if(!correctCommentInput)
	{
		alert("Please fill in scorecard comment first.");
		return;
	}

	scorecardForm.submit();
}
</script>
</body>
</html>
