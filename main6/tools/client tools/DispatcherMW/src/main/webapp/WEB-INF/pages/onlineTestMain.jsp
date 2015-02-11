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

.ajax_loader {background: url("../images/spinner_squares_circle.gif") no-repeat center center transparent;width:100%;height:100%;}
.blue-loader .ajax_loader {background: url("../images/ajax-loader_blue.gif") no-repeat center center transparent;}
</style>
<!--[if lt IE 9]>
<script src="../resources/js/html5shiv.js"></script>
<![endif]-->
<script type="text/javascript" src="../resources/js/utilityScripts.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="../resources/jquery/script.js"></script>
<script type="text/javascript">
var translateXLFLoader;
var basicURl = '${basicURL}';
var msg_validate_inpute_account = "Please select the Account.";
var msg_validate_inpute_srcLocale = "Please select the Source Locale.";
var msg_validate_inpute_trgLocale = "Please select the Target Locale.";
var msg_validate_inpute_srcText = "The Source Text is empty.";
var msg_validate_inpute_file = "Please select a XLIFF file.";

$(document).ready(function() {
	$("#tabs").tabs();
	$("#srcLocale").select(function(){fnGetSourceLocales();});
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

// Translate XLF File.
function fnTranslateXLF(){
	var securityCode = $("#account2").find(":selected").attr("securityCode");
	var fileName = $("#xlfFile").val();
	if(isEmptyString(securityCode))
		return alert(msg_validate_inpute_account);
	if(isEmptyString(fileName))
		return alert(msg_validate_inpute_file);

	translateXLFLoader = new ajaxLoader($("#tabs"), {classOveride: 'blue-loader', bgColor: '#000'});
	
	$.ajax({
		url: "../translateXLF/upload?securityCode=" + securityCode,
		data: new FormData($("#translateXLFForm")[0]),
		type: "POST",
		dataType : "json",
		processData: false, 
		contentType: false,
		success: function( data ) {
			if(data.status == "failed"){
				alert(data.errorMsg);
				fnTranslateXLFDone();
				return;
			}
			
			if(data.jobID != null && data.jobID > 0){
				fnCheckJobStatusForDownload(data.jobID, securityCode);
			}
		},
		error: function( xhr, status ) {
			alert("Translate XLF File Error: " + $("#xlfFile").val());
			console.log(xhr);
			fnTranslateXLFDone();
		}
	});
}

// Check Job Status for Download.
function fnCheckJobStatusForDownload(jobID, securityCode){
	$.ajax({
        type: 'post',
        url: '../translateXLF/checkStatus?',
        dataType: 'json',
        data: {
			"securityCode" : securityCode,
			"jobID" : jobID
		},
        success: function (data) {
        	if (data == null || data.jobID == null || data.jobID < 0) {
        		alert(data);
        		fnTranslateXLFDone();
        		return;
        	}
        	
            if ("completed" == data.status) {
            	var downloadURL = '../translateXLF/download?securityCode=' + securityCode;
            	downloadURL += '&jobID=' + jobID;
                window.location.href = downloadURL;
                fnTranslateXLFDone();
            } else if ("queued" == data.status || "running" == data.status) {
            	fnSleep(10);
            	fnCheckJobStatusForDownload(jobID, securityCode);
            } else {
            	alert("The job status is error. Break!");
        		fnTranslateXLFDone();
        		return;
            }
        },
        error: function () {
        	alert("Translate XLF File Error(Check Job Status Process)");
        	fnTranslateXLFDone();
        }
    });
}

// Initial XLF button, and clear file.
function fnTranslateXLFDone(){
	$("#xlfFile").val("");
    if(translateXLFLoader) translateXLFLoader.remove();
}

// JS Sleep Function.
function fnSleep(seconds){
	var sleepMillis = seconds * 1000;
	var start = new Date().getTime();
    while (true) if (new Date().getTime() - start > sleepMillis) break;
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
		
		<div id="tabs">
		 <ul>
			<li><a href="#tabs-1">Translate Text</a></li>
			<li><a href="#tabs-2">Translate XLIFF File</a></li>
		 </ul>
		 <div id="tabs-1">
		  <table border="0" class="standardText" cellpadding="2">
		    <tr>
				<td class="standardText" style="width:85px;">Account</td>
				<td>
					<select id="account" class="standardText" onChange="fnGetSourceLocales();" style="width:240px;">
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
					<input type="button" id="transBtn" value="Translate" onclick="fnTranslate();">&nbsp;&nbsp;&nbsp;					
				</td>
			</tr>
		  </table>
		</div>
		
		<div id="tabs-2" style="height:200px;">
			<table border="0" class="standardText" cellpadding="2">
		    	<tr>
					<td class="standardText" style="width:85px;">Account</td>
					<td>
						<select id="account2" class="standardText" onChange="fnGetSourceLocales();" style="width:240px;">
							<option value="-1">&nbsp;</option>
							<c:forEach items="${allAccounts}" var="data">
							<option value="${data.id}" securityCode="${data.securityCode}">${data.accountName}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td class="standardText">Select File</td>
					<td>
						<form action="../translateXLF/upload" method="post" enctype="multipart/form-data" id="translateXLFForm">
						<input type="file" name="xlfFile" size="50" id="xlfFile"/>&nbsp;&nbsp;&nbsp;
						</form>
					</td>
				</tr>
				<tr><td colspan="2">&nbsp;</td></tr>
				<tr>
					<td colspan="2">
						<input type="button" id="transXLFBtn" value="Translate XLIFF File" onclick="fnTranslateXLF();">
					</td>
				</tr>
			</table>
		</div>
		</div>
		
		<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
</DIV>
</body>
</html>