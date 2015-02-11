<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Machine Translation Profile Main Page</title>
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
	$('#mtProfilesTable').dataTable({
		"aaSorting": [],
		"aoColumnDefs": [ {"bSortable": false, "sWidth": "10px", "aTargets": [0]} ],
		"bJQueryUI": true,
        "sPaginationType": "full_numbers"
	});		
	//$('#mtProfilesTable').dataTable();
});

// View MT Profile by MT Profile ID
function fnViewMTProfile(id)
{
	var dForm = $("#dForm");
	dForm.attr("action", "../mtProfiles/viewDetail.htm");
	$("input[name='mtProfileID']").val(id);
	dForm.submit();
}

// Remove MT Profile
function fnRemoveMTProfile(id)
{
	var ids = "";
	$("input[name='mtProfileCheckBox']:checkbox").each(function () {
		if ($(this).attr("checked")) {
			ids = ids + $(this).parent().parent().attr("id") + ",";
		}
	});  
	
	if (ids == "") {
		alert("No data selected.");
		return;
	}
	
	$.ajax({
		url: "../mtProfiles/remove.json",
		data: {mtProfileID:ids},
		type: "POST",
		dataType : "json",
		success: function( data ) {
			if(data.error == null){
				location.href = "../mtProfiles/main.htm";
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
<input type="hidden" name="mtProfileID">
</FORM>

	<DIV>
		<%@ include file="/WEB-INF/pages/header.jspIncl" %>

		<div>
			<p />
			<p />
			MT Profile
			<p />
			<p />
			<p />
			<p />
		</div>
		
		<div id="content">
			<table id="mtProfilesTable" style="width: 100%">
				<thead>
					<tr>
						<td></td>
						<td>Name</td>
						<td>Description</td>
						<td>MT Engine</td>
						<td>MT Confidence Score (%)</td>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${mtProfiles}" var="mtProfile">
						<tr id="${mtProfile.id}">
							<td><input type="checkbox" name="mtProfileCheckBox"></td>
							<td><a onClick="fnViewMTProfile(${mtProfile.id});" href="javascript:void(0)">${mtProfile.mtProfileName}</a></td>
							<td>${mtProfile.description}</td>
							<td>${mtProfile.mtEngine}</td>
							<td>${mtProfile.mtConfidenceScore}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>

			<br /> 
			<input type="button" id="btnRemove" value="Remove" onclick="fnRemoveMTProfile();"> &nbsp;&nbsp; 
			<input type="button" id="btnNew" value="New..." onclick="fnViewMTProfile(-1);"> &nbsp;&nbsp;
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
	</DIV>
</body>
</html>