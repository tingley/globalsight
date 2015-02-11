<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Test Page</title>
<link rel="shortcut icon" href="../images/favicon_globalsight.PNG"/>
<link rel="stylesheet" href="../resources/css/style.css" />
<link rel="stylesheet" href="../resources/jquery/jQueryUI.redmond.css" />
<style type="text/css">
select {
	width: 100%;
}

textarea {
    width: 98%;
}
</style>
<!--[if lt IE 9]>
<script src="../resources/js/html5shiv.js"></script>
<![endif]-->
<script type="text/javascript" src="../resources/js/utilityScripts.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript">
var basicURl = '${basicURL}';
var msg_validate_inpute_account = "Please select the Account.";
var msg_validate_inpute_srcLocale = "Please select the Source Locale.";
var msg_validate_inpute_trgLocale = "Please select the Target Locale.";
var msg_validate_inpute_srcText = "The Source Text is empty.";

$(document).ready(function() {
	$('#srcLocale').select(function(){fnGetSourceLocales();});		
});

function fnTranslate(){
	var securityCode = $("#account").find(":selected").attr("securityCode");
	var srcLang = $("#srcLocale").val();
	var trgLang = $("#trgLocale").val();
	var src = $("#srcText").val();	
	
	if(isEmptyString(securityCode))
		return alert(msg_validate_inpute_account);
	if("-1" == srcLang)
		return alert(msg_validate_inpute_srcLocale);
	if("-1" == trgLang)
		return alert(msg_validate_inpute_trgLocale);
	if(isEmptyString(src))
		return alert(msg_validate_inpute_srcText);
	
	var params = "";
	params += "&securityCode=" + securityCode;
	params += "&srcLang=" + srcLang;
	params += "&trgLang=" + trgLang;
	params += "&src=" + encodeURIComponent(src);
	
	$.ajax({
        type: 'post',
        url: '../translate/?' + params,
        dataType: 'json',
     //   timeout: 100000000,
        success: function (data) {
            if("success" == data.status) {
                $("#trgText").val(data.trg);
            } else {
                alert("Translation fails. " + data.errorMsg);
            }
        },
        error: function () {
            alert("No Response.");
        }
    });
}

function fnGetSourceLocales(){
	var accountID = $("#account").val();	
	if("-1" == accountID)
		return;
	
	$.ajax({
		url: "../onlineTest/getSrcLocales.json",
		data: {
			"accountID" : accountID
		},
		type: "GET",
		dataType : "json",
		success: function( data ) {
			var locales = data.srcLocales;
			if(locales != null && locales.length > 0){
				$("#srcLocale").empty();
				$("#srcLocale").append("<option value='-1'></option>");
				for(i=0; i<locales.length; i++){
					var shortName = locales[i].language + "_" + locales[i].country;
					$("#srcLocale").append("<option value='"+shortName+"'>"+locales[i].displayName+"</option>");
				}
			}
		},
		error: function( xhr, status ) {
			alert( "Sorry, there was a problem!" );
		}
	});
}

function fnGetTargetLocales(){
	var accountID = $("#account").val();
	var srcLocale = $("#srcLocale").val();	
	if("-1" == accountID || "-1" == srcLocale)
		return;
	
	$.ajax({
		url: "../onlineTest/getTrgLocales.json",
		data: {
			"accountID" : accountID,
			"srcLocale" : srcLocale
		},
		type: "GET",
		dataType : "json",
		success: function( data ) {
			var trgLocales = data.trgLocales;
			if(trgLocales != null && trgLocales.length > 0){
				$("#trgLocale").empty();
				for(i=0; i<trgLocales.length; i++){
					var shortName = trgLocales[i].language + "_" + trgLocales[i].country;
					$("#trgLocale").append("<option value='"+shortName+"'>"+trgLocales[i].displayName+"</option>");
				}
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
<DIV>
		<%@ include file="/WEB-INF/pages/header.jspIncl" %>		

		<div>
			<p />
			<p />
			Test page
			<p />
			<p />
			<p />
			<p />
		</div>
		<div id="content">
		  <table border="0" class="standardText" cellpadding="2">
		    <tr>
				<td class="standardText" style="width:85px;">Account</td>
				<td>
					<select id="account" class="standardText" onChange="fnGetSourceLocales();">
					<option value="-1">&nbsp;</option>
					<c:forEach items="${allAccounts}" var="data">
						<option value="${data.id}" securityCode="${data.securityCode}">${data.accountName}</option>
					</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td class="standardText" style="width:85px;">Source Locale</td>
				<td>
					<select id="srcLocale" class="standardText" onChange="fnGetTargetLocales();">
					<option value="-1">&nbsp;</option>
					<c:forEach items="${allGlobalSightLocale}" var="locale">
						<option value="${locale}">${locale.displayName}</option>
					</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td class="standardText">Target Locale</td>
				<td>
					<select id="trgLocale" class="standardText">
					<option value="-1">&nbsp;</option>
					<c:forEach items="${allGlobalSightLocale}" var="locale">
						<option value="${locale}">${locale.displayName}</option>
					</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td class="standardText">Source Text</td>
				<td>
					<textarea id="srcText"></textarea>
				</td>
			</tr>
			<tr>
				<td class="standardText">Target Text</td>
				<td>
					<textarea id="trgText"></textarea>
				</td>
			</tr>
			<tr><td colspan="2">&nbsp;</td></tr>
			<tr>
				<td colspan="2">
					<input type="button" id="transBtn" value="Translate" onclick="fnTranslate();">
				</td>
			</tr>
		  </table>
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
</DIV>
</body>
</html>