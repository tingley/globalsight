<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
             com.globalsight.everest.webapp.WebAppConstants,
             com.globalsight.util.GlobalSightLocale,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
             com.globalsight.everest.servlet.util.ServerProxy,
             com.globalsight.everest.projecthandler.Project,
             com.globalsight.everest.company.CompanyWrapper,
             com.globalsight.everest.costing.Currency,
             com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData,
       		 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
       		 java.util.Locale,
       		 java.util.ResourceBundle,
       		 java.text.MessageFormat"
          session="true"
%>
<%  
    ResourceBundle bundle = PageHandler.getBundle(session);
	String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS";
%>
<html>
<!-- This JSP is: /envoy/administration/reports/recentReports.jsp -->
<head>
<title><%=bundle.getString("lb_recent_reports")%></title>
<style type="text/css">
table td,table td * {
	vertical-align: top;
}

.btnCancel {
	width: 60px; font-size: smaller;
}
</style>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.cookie.js"></script>
<!-- JS Tree Plugin -->
<link href="/globalsight/jquery/dynatree-1.2.4/skin-vista/ui.dynatree.css" rel="stylesheet" type="text/css">
<!--[if lt IE 9]>
	<link href="/globalsight/jquery/dynatree-1.2.4/skin-vista/ui.dynatree.ie8.css" rel="stylesheet" type="text/css"/>
<![endif]-->
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.dynatree.min.js"></script>
<script type="text/javascript">
var inProgressStatus = "<%=ReportsData.STATUS_INPROGRESS%>";
var defaultWidth = "98%";
var defaultHeight = "60%";

// Adds JS endsWith function.
String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

$(document).ready(function () {
	// Set CSS Value
	$("#treeDIV").width(defaultWidth);
	$("#treeDIV").height(defaultHeight);
	$("#statusDIV").width(defaultWidth);	
	
    // --- Initialize Report Files tree
    $("#treeDIV").dynatree({
        title: "Lazy loading sample",
        persist: true,
        checkbox: true,
        selectMode: 3,
        initAjax: {
            url: "/globalsight/ControlServlet?activityName=recentReports&action=view"
        },
        onSelect: function(select, node) {
            // Get a list of all selected nodes, and convert to a key array:
            var selKeys = $.map(node.tree.getSelectedNodes(), function(node){
              return node.data.key;
            });

            // Get a list of all selected TOP nodes
            var selRootNodes = node.tree.getSelectedNodes(true);
            // ... and convert to a key array:
            var selRootKeys = $.map(selRootNodes, function(node){
              return node.data.key;
            });
        },
        onDblClick: function(node, event) {
            node.toggleSelect();
        },
        onKeydown: function(node, event) {
			if( event.which == 32 ) {
              node.toggleSelect();
              return false;
            }
        }
    });

    // Expand All Button
    $("#expandBtn").click(function () {
    	$("#treeDIV").dynatree("getRoot").visit(function (node) {
    	    node.expand(true);
    	});
    });
    
    // Collapse All Button
    $("#collapseBtn").click(function () {
    	$("#treeDIV").dynatree("getRoot").visit(function (node) {
    	    node.expand(false);
    	});
    });
});

function fnDownload(){
	var selNodes = $("#treeDIV").dynatree("getSelectedNodes");
	if(selNodes.length == 0)
		alert("<%=bundle.getString("msg_select_report")%>");
	var selectedReports = selNodes[0].data.key;
	for(var i=1; i<selNodes.length; i++){
		selectedReports += ("," + selNodes[i].data.key);
	}
		
	$("#selReports").val(selectedReports);
	recentReportsForm.action = "/globalsight/ControlServlet?activityName=recentReports&action=download";
	recentReportsForm.submit();
}

function fnDelete(){
	var selNodes = $("#treeDIV").dynatree("getSelectedNodes");
	if(selNodes.length == 0)
		alert("<%=bundle.getString("msg_select_report")%>");
	var selectedReports = selNodes[0].data.key;
	for(var i=1; i<selNodes.length; i++){
		selectedReports += ("," + selNodes[i].data.key);
	}
		
	//alert(selectedReports);
	$("#selReports").val(selectedReports);
	recentReportsForm.action = "/globalsight/ControlServlet?activityName=recentReports&action=delete";
	recentReportsForm.submit();
}

//The function for canceling the report.
function fnDoCancel(elemBtn) {
  $.ajax({
	type: 'POST',
	dataType: 'json',
	url: '<%=basicAction + "&action=" + ReportConstants.ACTION_CANCEL_REPORTS_FROMRECENTREPORTS%>',
	data: {'inputJobIDS': $(elemBtn).parent().parent().attr("reportJobIDS"),
		   'reportType': $(elemBtn).parent().parent().attr("reportTypeList")},
	success: function (data) {fnReload();}
  });
}

function fnSelectAll(){
	var isChecked = $("#control").is(":checked");
	$("#treeDIV").dynatree("getRoot").visit(function(node){
		node.select(isChecked);
	});
}

function fnReload(){
	// Reload Reports Folder Tree.
	$("#treeDIV").dynatree("getTree").reload();
	
	// Reload Reports Status Div.
	if($("#statusDIV").is(':visible')){
		fnShowInProgressReports();
	}
}

function fnShowInProgressReportsWrapper(){
	if($("#statusDIV").is(':visible')){
		$("#statusDIV").css("display", "none");
		$("#showInProgressBtn").val("<%=bundle.getString("lb_show_inProgressReports")%>");
		window.resizeBy(0, -200);
		$("#treeDIV").height(defaultHeight);
	}else{
		fnShowInProgressReports();
		$("#showInProgressBtn").val("<%=bundle.getString("lb_hide_inProgressReports")%>");
		$("#treeDIV").height($("#treeDIV").height() + "px");
		window.resizeBy(0, 200);
	}
}

function fnShowInProgressReports(){
	var url = "/globalsight/ControlServlet?activityName=recentReports&action=getReportsData&date=" + new Date();
	$.getJSON(url, function (dataArr) {
		var html = "";
		if(dataArr == null){
			$("#statusDIV tbody").html("<tr><td colspan=3>No Data!</td></tr>");
			$("#statusDIV").css("display", "block");	
			return;
		}
		
		for(var i=0; i<dataArr.length; i++){
			var displayReportType = dataArr[i].reportTypeList;
			var displayReportJobID = dataArr[i].reportJobIDS;
			if(!displayReportType.endsWith("]")){
				displayReportType = displayReportType.substring(0, displayReportType.lastIndexOf(",")) + "...]";
			}
			if(!displayReportJobID.endsWith("]")){
				displayReportJobID = displayReportJobID.substring(0, displayReportJobID.lastIndexOf(",")) + "...]";
			}
			var statusHtml = dataArr[i].status;
			
			html += "<tr ";
			html += ("reportTypeList='" + dataArr[i].reportTypeList + "' ");
			html += ("reportJobIDS='" + dataArr[i].reportJobIDS + "' ");
			html += ">";
			html += ("<td>" + displayReportType + "</td>");
			html += ("<td>" + displayReportJobID + "</td>");
			html += ("<td></td>");
			if ("In Progress" == statusHtml)
			{
				statusHtml += ("&nbsp;&nbsp;<input type='button' value='Cancel' onClick='fnDoCancel(this);' class='btnCancel'>");
			}	
			html += ("<td>" + statusHtml + "</td>");
			html += "</tr>";
		}
		$("#statusDIV tbody").html(html);
		$("#statusDIV").css("display", "block");
	});
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="recentReportsForm" method="post" action="">
<input type="hidden" id="selReports" name="selReports">
</form>
    <TABLE><TR><TD class="standardText"><%=bundle.getString("lb_recent_reports_desc")%></TD></TR></TABLE>
<br/>
<table CELLSPACING="0" CELLPADDING="0" style="border:0px solid black">
<tr VALIGN="middle">
	<td style="border:1px solid black;width:50px;height:20px;background-color:#738eb5;" align="center">
		<input type="checkbox" id="control" title="Select/Deselect All" onClick="fnSelectAll();">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="expandBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/expand_all.gif')" title="Expand All">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="collapseBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/collapse_all.gif')" title="Collapse All">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="removeFile" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/trash.png')" title="Delete" onClick="fnDelete();">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="refreshFile" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/refresh.png')" title="Reload" onClick="fnReload();">
	</td>
</tr>
<tr><td style="height:3px" colspan="7"></td></tr>
</table>

<div id="treeDIV">
<!-- When using initAjax, it may be nice to put a throbber here, that spins until the initial content is loaded: -->
</div>
<div>&nbsp;</div>
<div align="center">
<input type="BUTTON" VALUE="<%=bundle.getString("lb_download")%>" onClick="fnDownload();">&nbsp;&nbsp;
<input type="BUTTON" VALUE="<%=bundle.getString("lb_close")%>" onClick="window.close();">&nbsp;&nbsp;
<input type="BUTTON" VALUE="<%=bundle.getString("lb_show_inProgressReports")%>" 
	   id="showInProgressBtn" onClick="fnShowInProgressReportsWrapper();">&nbsp;&nbsp;
</div>
<div id="statusDIV" style="display:none; overflow:auto; height:200px;">
<p/><p/>
<font color="red"><%=bundle.getString("lb_inProgressReports")%></font>
<table style="table-layout:fixed;word-wrap:break-word;width:100%;" class="standardText" cellspacing="0" cellpadding="4" border="0">
	<thead>
	  <tr>
		<td class="tableHeadingBasic" width="40%">Report Type List</td>
		<td class="tableHeadingBasic">Report Job ID List</td>
		<td class="tableHeadingBasic" width="1%"></td>
		<td class="tableHeadingBasic" width="25%">Report Status</td>
	  </tr>
	</thead>
	<tbody></tbody>
</table>
</div>
<body>
</HTML>
