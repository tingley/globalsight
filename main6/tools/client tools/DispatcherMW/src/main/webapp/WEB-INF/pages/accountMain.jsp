<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Account Main Page</title>
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
$(document).ready(function() {
	$('#dataTable').dataTable({
		"aaSorting": [],
		"aoColumnDefs": [ {"bSortable": false, "sWidth": "10px", "aTargets": [0]} ],
		"bJQueryUI": true,
        "sPaginationType": "full_numbers"
	});		
});

function fnView(id){
	var dForm = $("#dForm");
	dForm.attr("action", "../account/viewDetail.htm");
	$("input[name='selectedID']").val(id);
	dForm.submit();
}

function fnRemove(){
	var ids = "";
	$("input[name='dataBox']:checkbox").each(function () {
		if ($(this).attr("checked")) {
			ids = ids + $(this).parent().parent().attr("id") + ",";
		}
	});  

	if (ids == "") {
		alert("No data selected.");
		return;
	}

	$.ajax({
		url: "../account/remove.json",
		data: {selectedIDS:ids},
		type: "POST",
		dataType : "json",
		success: function( data ) {
			if(data.error == null){
				location.href = "../account/main.htm";
			}else{
				alert(data.error);
			}
		},
		error: function( xhr, status ) {
			alert( "Sorry, there was a problem!" );
		}
	});
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
			Account Information
			<p />
			<p />
			<p />
			<p />
		</div>
		
		<div id="content">
			<table id="dataTable" style="width: 100%">
				<thead>
					<tr>
						<td></td>
						<td>Name</td>
						<td>Description</td>
						<td>Security Code</td>
						<td>Operations</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${allAccounts}" var="data">
						<tr id="${data.id}">
							<td><input type="checkbox" name="dataBox"></td>
							<td><a onClick="fnView(${data.id});" href="javascript:void(0)">${data.accountName}</a></td>
							<td>${data.description}</td>
							<td>${data.securityCode}</td>
							<td><a href="../fileManagement/main.htm?accountId=${data.id}" target="_blank">Manage Files</a></td>							
						</tr>
					</c:forEach>
				</tbody>
			</table>

			<br /> 
			<input type="button" id="btnRemove" value="Remove" onclick="fnRemove();"> &nbsp;&nbsp; 
			<input type="button" id="btnNew" value="New..." onclick="fnView(-1);"> &nbsp;&nbsp;
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
	</DIV>
</body>
</html>