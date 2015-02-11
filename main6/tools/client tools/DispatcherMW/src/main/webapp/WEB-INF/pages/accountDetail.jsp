<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<title>Account Detail Page</title>
<link rel="shortcut icon" href="../images/favicon_globalsight.PNG"/>
<link rel="stylesheet" href="../resources/css/demo_page.css" />
<link rel="stylesheet" href="../resources/css/demo_table.css" />
<link rel="stylesheet" href="../resources/css/demo_table_jui.css" />
<link rel="stylesheet" href="../resources/css/style.css" />
<link rel="stylesheet" href="../resources/jquery/jQueryUI.redmond.css" />
<!--[if lt IE 9]>
<script src="../resources/js/html5shiv.js"></script>
<![endif]-->
<script type="text/javascript" src="../resources/js/utilityScripts.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery.dataTables.min.js"></script>
<script type="text/javascript">
function fnCancel(){
	location.href= "../account/main.htm";
}

function fnSaveOrUpdate(){	
	var accountName = stripBlanks($("#accountName").val());
	var description = stripBlanks($("#description").val());
	var securityCode = stripBlanks($("#securityCode").val());
	
	if("" == accountName)
		return alert(msg_validate_inpute_name);
	if("" == securityCode)
		return alert(msg_validate_inpute_clientCode);
	
	$.ajax({
		// the URL for the request
		url: "../account/saveOrUpdate.json",
		// the data to send (will be converted to a query string)
		data: {
			  accountId : ${account.id}
			, accountName : accountName
			, securityCode : securityCode
			, description : description
		},
		// whether this is a POST or GET request
		type: "POST",
		// the type of data we expect back
		dataType : "json",
		// code to run if the request succeeds;
		// the response is passed to the function
		success: function( data ) {
			if(data.error == null){
				location.href = "../account/main.htm";
			}else{
				alert(data.error);
			}
		},
		// code to run if the request fails; the raw request and
		// status codes are passed to the function
		error: function( xhr, status ) {
			alert( "Sorry, there was a problem!" );
		}
	});
}

function fnGenerateSecurityCode(){
	$.getJSON("../account/getRandom.json", function( data ) {
		$("#securityCode").val(data.securityCode); 
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
			Account Detail
			<p />
			<p />
			<p />
			<p />
		</div>
		<div id="content">
		<table border="0" class="standardText" cellpadding="2">
			<tr>
				<td class="standardText">Name<span class="asterisk">*</span>:</td>
				<td>
				  <input type="text" id="accountName" name="accountName" 
				  	value="${account.accountName}" style="width:98%;">
				</td>
			</tr>
			<tr>
				<td class="standardText">Security Code<span class="asterisk">*</span>:</td>
				<td>
				  <input type="text" id="securityCode" name="securityCode" 
				  	value="${account.securityCode}" style="width:98%;" disabled>
				</td>
			</tr>
			<tr>
				<td class="standardText">Description:</td>
				<td>
				  <textarea id=description name="description" style="width:97%;">${account.description}</textarea>
				</td>
			</tr>
			
			<tr><td colspan="2">&nbsp;</td></tr>
      		<tr>
        		<td colspan="2">
          			<input type="button" name="Cancel" value="Cancel" onclick="fnCancel()">
          			<input type="button" name="Save" value="Save" onclick="fnSaveOrUpdate()">
        		</td>
      		</tr>
		</table>
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
	</DIV>
</body>
</html>