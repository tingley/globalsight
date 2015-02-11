<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Languages Main Page</title>
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

// View MT Profile by MT Profile ID
function fnView(id)
{
	var transferForm = $("#transferForm");
	$("input[name='mtpLangID']").val(id);
	transferForm.attr("action", "../mtpLanguages/viewDetail.htm");	
	transferForm.submit();
}

// Remove MT Profile
function fnRemove(id)
{
	var transferForm = $("#transferForm");
	var ids = "";
	$("input[name='elemCheckBox']:checkbox").each(function () {
		if ($(this).attr("checked")) {
			ids = ids + $(this).parent().parent().attr("id") + ",";
		}
	});  
	
	if (ids == "") {
		alert("No data selected.");
		return;
	}
	
	$("input[name='mtpLangID']").val(ids);
	transferForm.attr("action", "../mtpLanguages/remove.htm");	
	transferForm.submit();
}
</script>
</head>
<body>
<!-- Form Element -->
<FORM NAME="transferForm" id="transferForm" METHOD="POST" ACTION="">
<input type="hidden" name="mtpLangID">
</FORM>

	<DIV>
		<%@ include file="/WEB-INF/pages/header.jspIncl" %>	

		<div>
			<p />
			<p />
			Languages
			<p />
			<p />
			<p />
			<p />
		</div>
		<div id="content">
			<table id="dataTable" style="width: 95%">
				<thead>
					<tr>
						<td></td>
						<td>Name</td>						
						<td>Source Locale</td>
						<td>Target Locale</td>	
						<td>MT Profile Name</td>					
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${mtpLanguages}" var="mtpLanguage">
						<tr id="${mtpLanguage.id}">
							<td><input type="checkbox" name="elemCheckBox"></td>
							<td><a onClick="fnView(${mtpLanguage.id});" href="javascript:void(0)">${mtpLanguage.name}</a></td>
							<td>${mtpLanguage.srcLocale.displayName}</td>
							<td>${mtpLanguage.trgLocale.displayName}</td>
							<td>${mtpLanguage.mtProfile.mtProfileName}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>

			<br /> 
			<input type="button" id="btnRemove" value="Remove" onclick="fnRemove();"> &nbsp;&nbsp; 
			<input type="button" id="btnNew" value="New..." onclick="fnView(-1);"> &nbsp;&nbsp;
		</div>
	</DIV>
	
	<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
</body>
</html>