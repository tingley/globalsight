<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>File Management Page</title>
<link rel="shortcut icon" href="../images/favicon_globalsight.PNG"/>
<link rel="stylesheet" href="../resources/css/demo_page.css" />
<link rel="stylesheet" href="../resources/css/demo_table.css" />
<link rel="stylesheet" href="../resources/css/demo_table_jui.css" />
<link rel="stylesheet" href="../resources/css/style.css" />
<link rel="stylesheet" href="../resources/jquery/jQueryUI.redmond.css" />
<!--[if lt IE 9]>
<script src="../resources/js/html5shiv.js"></script>
<![endif]-->
<script type="text/javascript" src="../resources/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery.dataTables.min.js"></script>
<script type="text/javascript">
var MSG_NO_ACCOUNT_SELECT = "No Account is selected.";
var MSG_NO_JOB_SELECT = "No Job is selected.";


$(document).ready(function() {
	$("#dataTable").dataTable({
		"aaSorting": [],
		"aoColumnDefs": [ {"bSortable": false, "sWidth": "10px", "aTargets": [0]} ],
		"bJQueryUI": true,
        "sPaginationType": "full_numbers"
	});	
	
	$("#accounts").val("${selectAccount}");
	
	$("#checkAll").click(function(){
		$(":checkbox[name='dataBox']").prop("checked", this.checked);
	});
});

function fnShowData(){
	var accountID = $("#accounts").val();
	window.location.href = "../fileManagement/main.htm?accountId=" + accountID;
}

function fnDownloadXLF(elem, fileType){
	var accountID = $(elem).parent().parent().attr("accountID");
	var jobID = $(elem).parent().parent().find("td").eq(1).text();
	var params = "accountID=" + accountID;
	params += "&jobID=" + jobID;
	params += "&fileType=" + fileType;
	window.location.href = "../fileManagement/downloadXLF.htm?" + params;
}

function fnRemoveJobs(){
	var accountID = $("#accounts").val();
	var jobIDArray = new Array();
	$("input[name='dataBox']:checkbox").each(function () {
		if ($(this).is(":checked")) {
			jobIDArray.push($(this).parent().parent().find("td").eq(1).text());
		}
	});  

	if (accountID == null) {
		alert(MSG_NO_ACCOUNT_SELECT);
		return;
	}
	if (jobIDArray.length == 0) {
		alert(MSG_NO_JOB_SELECT);
		return;
	}

	$.ajax({
		url: "../fileManagement/removeJobs.json",
		data: {accountID:accountID, jobIDS:jobIDArray},
		type: "POST",
		dataType : "json",
		success: function( data ) {
			if(data.error == null){
				location.href = "../fileManagement/main.htm?accountId=" + accountID;
			}else{
				alert(data.error);
			}
		},
		error: function( xhr, status ) {
			alert( "Sorry, there was a problem!" );
		}
	});
}

function fnDownloadJobs(){
	var accountID = $("#accounts").val();
	var jobIDArray = new Array();
	$("input[name='dataBox']:checkbox").each(function () {
		if ($(this).is(":checked")) {
			jobIDArray.push($(this).parent().parent().find("td").eq(1).text());
		}
	});  

	if (accountID == null) {
		alert(MSG_NO_ACCOUNT_SELECT);
		return;
	}
	if (jobIDArray.length == 0) {
		alert(MSG_NO_JOB_SELECT);
		return;
	}
	
	var params = "accountID=" + accountID + "&jobIDS=" + jobIDArray.toString();
	window.location.href = "../fileManagement/downloadJobs.htm?" + params;
}
</script>
</head>
<body>
<!-- Form Element -->
<FORM NAME="dForm" id="dForm" METHOD="POST" ACTION="">
<input type="hidden" name="selectedID">
</FORM>

	<DIV>
		<%@ include file="/WEB-INF/pages/header.jspIncl" %>

		<div>
			<p />
			<p />
			Download or remove source and target files
			
			<span style="float:right;margin-right:10px;">Account:
			  <select id="accounts" onChange="fnShowData();">
			  	<c:forEach items="${allAccounts}" var="data">
			  	  <option value="${data.id}">${data.accountName}</option>
			  	</c:forEach>
			  </select>
			</span>
			
			<p />
			<p />
			<p />
			<p />
		</div>
		
		<div id="content">
			<table id="dataTable" style="width: 100%">
				<thead>
					<tr>
						<td><input type="checkbox" id="checkAll"></td>
						<td>Job ID</td>
						<td>Source File</td>
						<td>Target File</td>
						<td>Last Modify Date</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${allTranslateFiles}" var="data">
						<tr accountID="${data.accountID}">
							<td><input type="checkbox" name="dataBox"></td>						
							<td>${data.jobID}</td>
							<td><a onClick="fnDownloadXLF(this, 'source');" href="javascript:void(0)">${data.sourceFileName}</a></td>
							<td><a onClick="fnDownloadXLF(this, 'target');" href="javascript:void(0)">${data.targetFileName}</a></td>
							<td>${data.lastModifyDateStr}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>

			<br /> 
			<input type="button" id="btnRemove" value="Remove" onclick="fnRemoveJobs();"> &nbsp;&nbsp; 
			<input type="button" id="btnDownload" value="Download" onclick="fnDownloadJobs();"> &nbsp;&nbsp;
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
	</DIV>
</body>
</html>