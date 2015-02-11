<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.AddSourceHandler,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            java.text.MessageFormat,
            java.util.*"
    session="true"
%>
<jsp:useBean id="jobDetails" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetailsPDFs" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobComments" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editPages" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="surcharges" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editFinalCost" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
//jobSummary child page needed started.
   ResourceBundle bundle = PageHandler.getBundle(session);
   String jobCommentsURL = jobComments.getPageURL() + "&jobId=" + request.getAttribute("jobId");
   String surchargesURL = surcharges.getPageURL() + "&jobId=" + request.getAttribute("jobId");
   String editFinalCostURL = editFinalCost.getPageURL() + "&jobId=" + request.getAttribute("jobId");
   String jobCostsURL = jobCosts.getPageURL() + "&jobId=" + request.getAttribute("jobId");
//jobSummary child page needed end.
%>
<html>
<head>
<title><%=bundle.getString("lb_costs")%></title>
<script src="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script src="/globalsight/jquery/jquery.progressbar.js"></script>
<script src="/globalsight/envoy/projects/workflows/jobDetails.js"></script>
<%@ include file="/envoy/common/warning.jspIncl" %>
<title><%=bundle.getString("lb_workflows")%></title>
<style>
#jobCostBlock table {
	width:400px;
	border:1px solid #D6CFB2;
	float:left;
	margin-right:50px;
	margin-bottom:20px;
}

#jobCostBlock table td{
	border:none;
	padding:1px 5px;
}

#jobQuoteBlock table {
	width:400px;
	border:1px solid #D6CFB2;
	margin-bottom:20px;
}

#jobQuoteBlock table td{
	padding:5px 5px;
}

.jobQuoteStatus{
	white-space:nowrap;
	font-weight:bold;
}
</style>
<script>
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_costs")%>";

//jobSummary child page needed started
$(document).ready(function(){
	$("#jobCostsTab").removeClass("tableHeadingListOff");
	$("#jobCostsTab").addClass("tableHeadingListOn");
	$("#jobCostsTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobCostsTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
	
	<amb:permission name="<%=Permission.JOB_QUOTE_STATUS_VIEW%>" >
		<c:if test="${Job.quoteApprovedDate != null}">
			$("#quoteTitle").css("background-color","#99DD00");
		</c:if>
		<c:if test="${Job.quoteApprovedDate == null}">
			$("#quoteTitle").css("background-color","red");
		</c:if>
	</amb:permission>
})
//jobSummary child page needed end.

function editCurrency(){
	$("#currencyIsoCodeDisplay").replaceWith($("#selectCurrency"));
	$("#selectCurrency").css("display","inline");
}

function changeTempCurrency(element){
	var url = "<%=jobCostsURL%>&currencyIsoCode=" + element.value;
	window.location.href = url;
}

function invokeSaveQuotePoNumber(){
	var quotePoNumber = document.getElementById("POnumber").value;
	var quoteForm = document.getElementById("quoteForm"); 
	if(quotePoNumber == '${Job.quotePoNumber}'){
		alert('<%=bundle.getString("msg_validate_po_number")%>');
	}else{
		if(confirm('<%=bundle.getString("msg_save_po_number_confirm")%>'))
		{
			quoteForm.action = "<%=jobCostsURL%>&<%=JobManagementHandler.QUOTE_PO_NUMBER%>=" + URLencode(quotePoNumber);
			quoteForm.submit();
		}
	}
}

function confirmApproved(){
	if(confirm('<%=bundle.getString("msg_quote_approve_confirm")%>')){
		var date = new Date();
		var myDate = getMyDate(date);
		var url = "<%=jobCostsURL%>";
		var quoteForm = document.getElementById("quoteForm");
		document.getElementById('<%=JobManagementHandler.QUOTE_APPROVED_DATE%>').value = myDate;
		document.getElementById('<%= JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG %>').value = true;
		//submitEmail();
		<amb:permission name="<%=Permission.JOB_WORKFLOWS_DISPATCH%>" >
			url += addWorkflowsDispathParam(); 
		</amb:permission>
		quoteForm.action = url;
		quoteForm.submit();
	}
}

<amb:permission name="<%=Permission.JOB_WORKFLOWS_DISPATCH%>" >
	function addWorkflowsDispathParam(){
		var hasSetCostCenter = '${jobHasSetCostCenter}';
		if ("false" == hasSetCostCenter) 
		{
			alert('<%=bundle.getString("msg_cost_center_empty")%>');
		} 
		if("${hasReadyWorkflow}" == "true" && confirm('<%=bundle.getString("msg_dispatch_all_workflow_confirm")%>')){
			return "&<%=JobManagementHandler.DISPATCH_ALL_WF_PARAM%>=true&<%=JobManagementHandler.ALL_READY_WORKFLOW_IDS%>=${allreadyWorkfowIds}";
		}
		else {
			return "";
		}
	}
</amb:permission>

function confirmCostChange()
{
<c:if test="${isFinshedJob}">
	 if (confirm("<%=bundle.getString("jsmsg_costing_lockdown")%>"))
	 {
	    return true;
	 }
	 else
	 {
	    return false;
	 }
</c:if>
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()"; id="idBody"  class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer"  class="standardText" style="position: absolute; z-index: 9; top: 108px; left: 20px; right: 20px;">
	<div id="includeSummaryTabs">
		<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
	</div>
	
	<div style="clear:both;margin:0.5em 0">&nbsp;</div>
	
	<div id="jobCostBlock">
		<c:if test="${isCostingEnabled}">
			<amb:permission name="<%=Permission.JOB_COSTING_VIEW%>" >
				<table class="detailText">
					<!-- hidden drop box -->
					<select id="selectCurrency" style="display:none;text-align:right" onchange="changeTempCurrency(this)">
						<c:forEach items="${CurrencyMap}" var="currency">
						   <!-- In JobCostHandler,JobManagementHandler.CURRENCY === idCurrency -->
						   <option value="${currency.key}" <c:if test="${currency.key == idCurrency}">selected="selected"</c:if>  >${currency.value}</option>
						</c:forEach>
					</select>
					
					<tr style="font-weight:bold;background-color:#D6CFB2;height:2em">
						<td colspan="2">
							<%=bundle.getString("lb_job_cost")%>
						</td>
					</tr>
					<tr>
						<td style="font-weight:bold">
							<%=bundle.getString("lb_currency")%>
							<amb:permission  name="<%=Permission.JOB_COSTING_REEDIT%>" >
								(<a href="javascript:editCurrency()">Edit</a>):
							</amb:permission>
						</td>
						<td style="text-align:right">
						    <!-- In JobCostHandler,JobManagementHandler.CURRENCY === idCurrency -->
							<span id="currencyIsoCodeDisplay">${idCurrency}</span>
						</td>
					</tr>
					<tr>
						<td style="background-color:#D6CFB2" colspan="2"></td>
					</tr>
					<amb:permission  name="<%=Permission.COSTING_EXPENSE_VIEW%>" >
						<tr>
							<td style="font-weight:bold;white-space:nowrap">
								<%=bundle.getString("lb_estimated_internal_costs")%>:
							</td>
							<td style="text-align:right">
								${estimatedCost}
							</td>
						</tr>					
						<tr>
							<td style="font-weight:bold;white-space:nowrap">
								<%=bundle.getString("lb_actual_internal_costs")%>:
							</td>
							<td style="text-align:right">
								${actualCost}
							</td>
						</tr>
						<tr>
							<td style="font-weight:bold;white-space:nowrap">
								<%=bundle.getString("lb_surcharges")%>
								<amb:permission  name="<%=Permission.JOB_COSTING_REEDIT%>" >
									(<a href="<%=surchargesURL %>&surchargesFor=expenses" onclick="return confirmCostChange()">Edit</a>):
								</amb:permission>
							</td>
							<td style="text-align:right">
							</td>
						</tr>
						<!-- Surcharges -->
             				<c:forEach items="${SurchargesFlatMap}" var="surchargeFlat">
						   		<tr>
						   			<td style="padding-left:10px">${surchargeFlat.key}</td>
						   			<td style="text-align:right">${surchargeFlat.value}</td>
						   		</tr>
							</c:forEach>	
             				<c:forEach items="${SurchargesPercentageMap}" var="surchargePercent">
						   		<tr>
						   			<td style="padding-left:10px">${surchargePercent.key}</td>
						   			<td style="text-align:right">${surchargePercent.value}</td>
						   		</tr>
							</c:forEach>	
                        <!-- End Surcharges -->
						<tr>
							<td style="font-weight:bold;vertical-align:top;white-space:nowrap">
								<%=bundle.getString("lb_final_internal_costs")%>
								<amb:permission  name="<%=Permission.JOB_COSTING_REEDIT%>" >
									(<a href="<%=editFinalCostURL %>&surchargesFor=expenses" onclick="return confirmCostChange()">Edit</a>):
								</amb:permission>
							</td>
							<td style="text-align:right">
								${finalCost}
								<c:if test="${isCostOverriden}">
									<br/><span class="smallTextGray">(<%=bundle.getString("lb_final_expenses_override")%>)</span>
								</c:if>
							</td>
						</tr>					
					</amb:permission>
					
					<!-- Revenue -->
					<c:if test="${isRevenueEnabled}">
						<amb:permission  name="<%=Permission.COSTING_REVENUE_VIEW%>" >
							<tr>
								<td style="background-color:#D6CFB2" colspan="2"></td>
							</tr>
							<tr>
								<td style="font-weight:bold;white-space:nowrap">
									<%=bundle.getString("lb_actual_billing_charges")%>:
								</td>
								<td style="text-align:right">
									${estimatedRevenue}
								</td>
							</tr>
							<tr>
								<td style="font-weight:bold;white-space:nowrap">
									<%=bundle.getString("lb_surcharges")%>
									<amb:permission  name="<%=Permission.JOB_COSTING_REEDIT%>" >
										(<a href="<%=surchargesURL %>&surchargesFor=revenue" onclick="return confirmCostChange()">Edit</a>):
									</amb:permission>
								</td>
								<td style="text-align:right">
								</td>
							</tr>
							<!-- Surcharges -->
	             				<c:forEach items="${SurchargesRevenueFlatMap}" var="surchargeRevenueFlat">
							   		<tr>
							   			<td style="padding-left:10px">${surchargeRevenueFlat.key}</td>
							   			<td style="text-align:right">${surchargeRevenueFlat.value}</td>
							   		</tr>
								</c:forEach>	
	             				<c:forEach items="${SurchargesRevenuePercentageMap}" var="surchargeRevenuePercent">
							   		<tr>
							   			<td style="padding-left:10px">${surchargeRevenuePercent.key}</td>
							   			<td style="text-align:right">${surchargeRevenuePercent.value}</td>
							   		</tr>
								</c:forEach>	
	                        <!-- End Surcharges -->
							<tr>
								<td style="font-weight:bold;vertical-align:top;white-space:nowrap">
									<%=bundle.getString("lb_final_revenue_costs")%>
									<amb:permission  name="<%=Permission.JOB_COSTING_REEDIT%>" >
										(<a href="<%=editFinalCostURL %>&surchargesFor=revenue" onclick="return confirmCostChange()">Edit</a>):
									</amb:permission>
								</td>
								<td style="text-align:right">
									${finalRevenue}
									<c:if test="${isRevenueOverriden}">
										<br/><span class="smallTextGray">(<%=bundle.getString("lb_final_revenue_override")%>)</span>
									</c:if>
								</td>
							</tr>		
						</amb:permission>
					</c:if>
					<amb:permission  name="<%=Permission.JOB_COSTING_REPORT%>" >
						<tr>
							<td colspan="2" style="text-align:right">
								<input type="button" value="<%=bundle.getString("lb_cost_report")%>" name="costReport"
									onclick="popup('/globalsight/TranswareReports?reportPageName=CostingReport&act=Create&jobid=<%=request.getAttribute(JobManagementHandler.JOB_ID)%>','<%=bundle.getString("lb_costing")%>')">
							</td>
						</tr>
					</amb:permission>
				</table>
			</amb:permission>
		</c:if>
	</div>
	
	<div id="jobQuoteBlock">
		<table class="detailText">
			<form id="quoteForm" name="quoteForm" method="post">
				<tr id="quoteTitle" style="font-weight:bold;background-color:#D6CFB2;height:2em">
					<td colspan="3">
						<%=bundle.getString("lb_Quote")%>
					</td>
				</tr>
				
				<amb:permission name="<%=Permission.JOB_QUOTE_SEND%>" >
					<tr style="height:4em">
						<td colspan="2" style="font-weight:bold">
							<input type="checkbox" name="quoteOK" onclick="updateButtonStateByCheckBox('sendEmailId',this)" 
								<c:if test="${Job.quoteApprovedDate != null}"> disabled="true"</c:if>
							/>
							<%=bundle.getString("lb_quote_ready")%>
						</td>
						<td>
							<input type="button" name="sendEmail" id="sendEmailId" value="<%=bundle.getString("lb_send_email")%>" disabled="true" 
								onclick="send_email('<%=bundle.getString("msg_quote_ready_confirm")%>', '<%=jobCostsURL%>');"/>
						</td>
					</tr>
				</amb:permission>
				
				<!-- For Quote process webEx-->
	            <amb:permission name="<%=Permission.JOB_QUOTE_PONUMBER_VIEW%>" >
		          	<tr>
						<td style="padding:0" colspan="3"><hr/></td>
					</tr>
		            <tr style="height:4em">
						<td colspan="2" style="font-weight:bold">
							<%=bundle.getString("lb_po_number")%>
							<input type="text" name="POnumber" id="POnumber" disabled="true" size="15" value="${Job.quotePoNumber}"/>
						</td>
						<td>
							<amb:permission name="<%=Permission.JOB_QUOTE_PONUMBER_EDIT%>" >
								<input class="standardText" type="button" name="PONumberSave" id="PONumberSave" value="<%=bundle.getString("lb_po_number_save")%>" 
									onclick="invokeSaveQuotePoNumber();"/>
								<script>$("#POnumber").removeAttr("disabled");</script>
							</amb:permission>
						</td>
					</tr>
				</amb:permission>
				
				<!-- For Quote process webEx issue-->
				<amb:permission name="<%=Permission.JOB_QUOTE_APPROVE%>" >
		          	<tr>
						<td style="padding:0" colspan="3"><hr/></td>
					</tr>
		            <tr style="height:4em">
						<td colspan="2" style="font-weight:bold">
							<input type="checkbox" name="ApproveBox" id="ApproveBox" disabled="true" onclick="updateButtonStateByCheckBox('confirmApproveId',this)"/>
							<%=bundle.getString("lb_quote_approve")%>
						</td>
						<script>
							<c:if test="${Job.project.poRequired == 0}">
								document.getElementById("ApproveBox").disabled = false;
							</c:if>
							<c:if test="${Job.project.poRequired == 1 && Job.quotePoNumber != null && Job.quotePoNumber != ''}">
								document.getElementById("ApproveBox").disabled = false;
							</c:if>
						</script>
						<td>
							<input class="standardText" type="button" name="confirmApprove" id="confirmApproveId" value="<%=bundle.getString("lb_quote_approve_confirm")%>" 
								 disabled="true" onclick="confirmApproved()"/>
						</td>
					</tr>
				</amb:permission>
				
				<amb:permission name="<%=Permission.JOB_QUOTE_STATUS_VIEW%>" >
					<tr style="height:6em">
						<td class="jobQuoteStatus">
							<c:if test="${Job.user != null && Job.user != ''}">
								<%=bundle.getString("lb_send_authoriser")%><br/>${Job.user.firstName}&nbsp;${Job.user.lastName}
							</c:if>
						</td>
						<td class="jobQuoteStatus">
							<c:if test="${Job.quoteDate != null && Job.quoteDate != ''}">
								<%=bundle.getString("lb_quote_ready_email_sent")%><br/>${Job.quoteDate}
							</c:if>
						</td>
						<td class="jobQuoteStatus">
							<c:if test="${Job.quoteApprovedDate != null && Job.quoteApprovedDate != ''}">
								<%=bundle.getString("lb_quote_approved_email_sent")%><br/>${Job.quoteApprovedDate}
							</c:if>
						</td>
					</tr>
				</amb:permission>
				<input type="hidden" name="<%= JobManagementHandler.QUOTE_DATE %>" id="<%= JobManagementHandler.QUOTE_DATE %>" value="">
				<!-- For Quote process webEx issue-->
				<input type="hidden" id ="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" NAME="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" value="">
				<input type="hidden" id="<%= JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG %>" name="<%= JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG %>" value="false">
			</form>
		</table>
			
	</div>
</div>
</body>
</html>
