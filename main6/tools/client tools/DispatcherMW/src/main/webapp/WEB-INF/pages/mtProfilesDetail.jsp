<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Machine Translation Profile Detail Page</title>
<link rel="shortcut icon" href="../images/favicon_globalsight.PNG"/>
<link rel="stylesheet" href="../resources/css/jquery.loadmask.css" />
<link rel="stylesheet" href="../resources/css/style.css" />
<!--[if lt IE 9]>
<script src="../resources/js/html5shiv.js"></script>
<![endif]-->
<script type="text/javascript" src="../resources/js/utilityScripts.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery.form.js"></script>
<script type="text/javascript" src="../resources/jquery/jquery.loadmask.min.js"></script>
<SCRIPT type="text/javascript">
//{ "ProMT", "MS_Translator", "Asia_Online", "Safaba" };
var needWarning = false;
var objectName = "MT Options";
var guideNode = "mtProfiles";
//var helpFile = "/globalsight/help/en_US/Setup/Translation_Memory_Profiles/Set_Machine_Translation_Options.htm";
var forceSave = false;
var current_engine = '${mtProfile.mtEngine}';
var jsonInfo = ${mtProfile.jsonInfo == null ? false : mtProfile.jsonInfo};
var exInfoVal = '${mtProfile.exInfoVal}';

$(document).ready(function() {
	fnInitialData();
});

$(function () {
    $("input").each(function () {
        var n = $.trim($(this).val());
        if(n == "null" || !n) {
            $(this).val("");
        }
    })
    $("#edit").click(function () {
        if(confirm("Edit the base info need re-test MT engine.")) {
            $("#MTOptionsForm").unmask("Connecting engine...");
            forceSave = false;
            $(this).hide();
            $("#OK").val("get Info");
            $("#optTable").empty();
        }
    })
    if(!exInfoVal) return;
    pushData(jsonInfo, current_engine);
    var exInfoVals = exInfoVal.split(",");
    var dirName = $(".dirName");
    if(!dirName.length) return;
    $.each(exInfoVals, function (i, val) {
        var key = "#" + val.split("@")[0];
        $(key).val(val);
    })
})

// Initial TM Profile Data on page.
function fnInitialData(){
	if(current_engine == null || current_engine == ''){
		$("#mtEngine").val('MS_Translator');
		contorlMTOptionShowing();
		return;
	}
	
	$("#mtEngine").val(current_engine);
	contorlMTOptionShowing();
	
	if ('IPTranslator' == current_engine){
		$("#idIPUrl").val('${mtProfile.url}');
		$("#idIPKey").val('${mtProfile.password}');
		// Extend Info
	}else if ('ProMT' == current_engine){
		$("#ptsurl").val('${mtProfile.url}');
		$("#username").val('${mtProfile.username}');
		$("#password").val('${mtProfile.password}');
	}else if ('Asia_Online' == current_engine){
		$("#idAoMtUrl").val('${mtProfile.url}');
		$("#idAoMtPort").val('${mtProfile.port}');
		$("#idAoMtUsername").val('${mtProfile.username}');
		$("#idAoMtPassword").val('${mtProfile.password}');
		$("#idAoMtAccountNumber").val('${mtProfile.accountinfo}');
		// Extend Info
	}else if ('Safaba' == current_engine){
		$("#idSafaMtHost").val('${mtProfile.url}');
		$("#idSafaMtPort").val('${mtProfile.port}');
		$("#idSafaMtCompanyName").val('${mtProfile.username}');
		$("#idSafaMtPassword").val('${mtProfile.password}');
		$("#idSafaMtClient").val('${mtProfile.accountinfo}');
	}else if ('MS_Translator' == current_engine){
		$("#idMsMtClientid").val('${mtProfile.username}');
		$("#idMsMtClientSecret").val('${mtProfile.password}');
		$("#idMsMtCategory").val('${mtProfile.category}');
	}else if('Google_Translate' == current_engine){
		$("#idAPIKey").val('${mtProfile.accountinfo}')
	}	
}

function testHost(formAction) {
    if(checkOptions(formAction)) {
        $("#baseInfo").mask("Connecting engine...");
        $("#MTOptionsForm").ajaxSubmit({
                type: 'post',
                url: '../mtProfiles/testHost.htm',
                dataType: 'json',
                timeout: 100000000,
                success: function (data) {
                    $("#baseInfo").unmask("Connecting engine...");
                    $("#OK").attr("disabled", false);
                    if(data) {
                        if(data.ExceptionInfo) {
                            var vl = data.ExceptionInfo + "";
                            //var s = vl.indexOf(":");
                            //vl = vl.substr(s + 1);
                            alert(vl);
                        } else if(data.Info) {
                           //self.location.href = '../mtProfiles/refresh.htm';
                        	dForm.submit();
                        } else {
                            pushData(data, formAction);
                        }
                    } else {
                        alert("The machine translation engine works well.");
                    }
                },
                error: function (XmlHttpRequest, textStatus, errorThrown) {
                    $("#baseInfo").unmask("Connecting engine...");
                    alert("error" + textStatus);
                    $("#OK").attr("disabled", false);
                }
            });
    } else {
        $("#OK").attr("disabled", false);
    }
}

function pushData(jsonData, formAction) {
    if(!jsonData) return;
    $("#optTable").empty();
    var cont = "";
    var info;
    if("ProMT" == formAction) {
        info = $("#promtinfo").clone(true);
        for(var key in jsonData) {
            var json = (jsonData[key] + "").split(",");
            var optStr = "";
            for(var i = 0; i < json.length; i++) {
                optStr += "<option value='" + key + "@" + json[i] + "' >" + json[i] + " </option>";
            }
            cont += "<tr> <td align='left' >" + key.split("@")[0] + "</td> <td><select id='" + key.split("@")[0] + "' class='dirName' name='dirName'>" + optStr + "</select> </td> </tr>";
        }
    } else {
        info = $("#asiainfo").clone(true);
        for(var key in jsonData) {
            var json = (jsonData[key] + "").split(",");
            var optStr = "";
            for(var i = 0; i < json.length; i++) {
                var valshow = json[i].split("@");
                optStr += "<option value='" + key.split("#")[0] + valshow[0] + "' >" + valshow[1] + " </option>";
            }
            cont += "<tr> <td align='left' >" + key.split("#")[1] + "</td> <td><select id='" + key.split("#")[0].split("@")[0] + "' class='dirName' name='dirName'>" + optStr + "</select> </td> </tr>";
        }
    }
    $("#baseInfo").mask("Please select locale pairs and save.");
    info.show();
    $("#optTable").append(info);
    $("#optTable").append(cont);
    forceSave = true;
    $("#edit").show();
    $("#OK").val("Save");
}

function checkOptions(formAction) {
    if(formAction == "MS_Translator") {
        var msTranslatorUrl = document.getElementById('idMsMtUrl').value;
        var msTranslatorClientID = document.getElementById('idMsMtClientid').value;
        var msTranslatorClientSecret = document.getElementById('idMsMtClientSecret').value;
        if(msTranslatorUrl == null || trim(msTranslatorUrl) == "") {
            alert("Machine Translation engine url can't be empty or null.");
            return false;
        }
        if(msTranslatorClientID == null || trim(msTranslatorClientID) == "") {
            alert("Machine Translation engine client ID can't be empty or null.");
            return false;
        }
        if(msTranslatorClientSecret == null || trim(msTranslatorClientSecret) == "") {
            alert("Machine Translation engine client secret can't be empty or null.");
            return false;
        }
    } else if(formAction == "Safaba") {
        var safaHostName = trim($("#idSafaMtHost").val());
        var safaPort = trim($("#idSafaMtPort").val());
        var safaCompanyName = trim($("#idSafaMtCompanyName").val());
        var safaPassword = trim($("#idSafaMtPassword").val());
        var safaClient = trim($("#idSafaMtClient").val());
        if(safaHostName == "" || safaPort == "" || safaCompanyName == "" || safaPassword == "" || safaClient == "") {
            alert("Some of the required attributes have not been set.");
            return false;
        }
    } else if(formAction == "Asia_Online") {
        var canGoOn = checkAoOptions();
        if(canGoOn != true) {
            return false
        }
    } else if(formAction == "ProMT") {
        var ptsurl = document.getElementById('ptsurl').value;
        MTOptionsForm.action = '/globalsight/ControlServlet?linkName=&pageName=';
        if(ptsurl != null && trim(ptsurl) != "") {
            var httpIndex = ptsurl.indexOf('http');
            if(httpIndex != 0) {
                alert("The protocol of PTS url is not 'http'.");
                return false;
            }
        } else {
            alert("Machine Translation engine url can't be empty or null.");
            return false;
        }
    }else if(formAction == "Google_Translate") 
	{
		var apiKey = $.trim($("#idAPIKey").val());
		if (apiKey ==null || apiKey == "") 
		{
            alert("Machine Translation engine API Key can't be empty or null!");
            return false;                	
        }
	}
    return true;
}

function checkMtConfidenceScoreValid() {
    var mtConfidenceScore = document.getElementById('mtConfidenceScore').value;
    if(mtConfidenceScore == null || trim(mtConfidenceScore) == "") {
        alert("MT Confidence Score can't be null or empty.");
        return false;
    } else if(!isAllDigits(mtConfidenceScore)) {
        alert("MT Confidence Score : Invalid entry. Please enter a numeric value.");
        return false;
    } else if(!checkIsVaildPercent(mtConfidenceScore)) {
        return false;
    }
    return true;
}

function safabaInputChanged() {
    var formerHost = "";
    var currentHost = $("#idSafaMtHost").val();
    var formerPort = "";
    var currentSafaPort = $("#idSafaMtPort").val();
    var formerCompany = "";
    var currentSafaCompanyName = $("#idSafaMtCompanyName").val();
    var formerPassword = "";
    var currentSafaPassword = $("#idSafaMtPassword").val();
    var formerClient = "";
    var currentSafaClient = $("#idSafaMtClient").val();
    if(formerHost != currentHost || formerPort != currentSafaPort || formerCompany != currentSafaCompanyName || formerClient != currentSafaClient) {
        return true;
    } else if(!isValidPassswd(currentSafaPassword) && formerPassword != currentSafaPassword.hashCode()) {
        return true;
    }
    return false;
}

function checkIsVaildPercent(percent) {
    var submit = false;
    var i_percent = parseInt(percent);
    if(i_percent > 100 || i_percent < 0) {
        alert("The number you input should between 0 and 100!");
        submit = false;
    } else {
        submit = true;
    }
    return submit;
}

function contorlMTOptionShowing() {
    //1.Control MT DIVs display
    var ptsDiv = document.getElementById("ptsDiv");
    var msMtDiv = document.getElementById("msMtDiv");
    var aoMtDiv = document.getElementById("aoMtDiv");
    var safaMtDiv = document.getElementById("safaMtDiv");
    var IPTranslatorDiv = document.getElementById("IPTranslatorDiv");
    var googleDiv = document.getElementById("googleDiv");
    var engineSelect = document.getElementById("mtEngine");
    var selectedEngineName = engineSelect.options[engineSelect.selectedIndex].value;
    // hide them all first
    ptsDiv.style.display = 'none';
    msMtDiv.style.display = 'none';
    aoMtDiv.style.display = 'none';
    safaMtDiv.style.display = 'none';
    IPTranslatorDiv.style.display = 'none';
    googleDiv.style.display='none';
    // display corresponding div by selected engine name.
    if(selectedEngineName.toLowerCase() == "google_translate") {
    	googleDiv.style.display='block';
    } else if(selectedEngineName.toLowerCase() == "promt") {
        ptsDiv.style.display = 'block';
    } else if(selectedEngineName.toLowerCase() == "ms_translator") {
        msMtDiv.style.display = 'block';
    } else if(selectedEngineName.toLowerCase() == "asia_online") {
        aoMtDiv.style.display = 'block';
    } else if(selectedEngineName.toLowerCase() == "safaba") {
        safaMtDiv.style.display = 'block';
    } else if(selectedEngineName == "IPTranslator") {
        IPTranslatorDiv.style.display = 'block';
    }
    //2.Display "Save" button or "Next" button
    var okBtn = document.getElementById("OK");
    if(selectedEngineName.toLowerCase() == "google_translate" || selectedEngineName.toLowerCase() == "ms_translator" || selectedEngineName.toLowerCase() == "safaba" || selectedEngineName == "IPTranslator") {
        okBtn.value = "Save";
    } else {
        okBtn.value = "get Info";
    }
}

function checkAoOptions() {
    var aoMtUrl = $.trim($('#idAoMtUrl').val());
    var aoMtPort = $.trim($('#idAoMtPort').val());
    var aoMtUserName = $.trim($('#idAoMtUsername').val());
    var aoMtPassword = $.trim($('#idAoMtPassword').val());
    var aoMtAccountNumber = $.trim($('#idAoMtAccountNumber').val());
	
    if(!aoMtUrl) {
        alert("Asia Online URL can not be empty!");
        return false;
    } else if(!aoMtPort || !isAllDigits(aoMtPort)) {
        alert("Asia Online Port can not be empty or invalid number!");
        return false;
    } else if(!aoMtUserName) {
        alert("Username can not be empty!");
        return false;
    } else if(!aoMtPassword) {
        alert("Password can not be empty!");
        return false;
    } else if(!aoMtAccountNumber || !isAllDigits(aoMtAccountNumber)) {
        alert("Account Number can not be empty or invalid number!");
        return false;
    }
    return true;
}
//remove all whitespace on left and right

function trim(str) {
    return str.replace(/(^\s*)|(\s*$)/g, '');
}

function ltrim(str) {
    return str.replace(/(^\s*)/g, '');
}

function rtrim(str) {
    return str.replace(/(\s*$)/g, '');
}

function isValidPassswd(p_str) {
    return p_str == "***************************";
}

String.prototype.hashCode = function () {
    for(var ret = 0, i = 0, len = this.length; i < len; i++) {
        ret = (31 * ret + this.charCodeAt(i)) << 0;
    }
    return ret;
};

function fnCancel(){
	location.href= "../mtProfiles/main.htm";
}

function fnSaveOrUpdate() {
    var name = $("#MtProfileName").val();
    if(!name) {
        alert("Please check name");
        return;
    }
    $("#OK").attr("disabled", true);
    //var isShowInEditor = $("#idShowInEditor").is(":checked");
    var engine_name = document.getElementById('mtEngine').value;

    if(forceSave) {
        MTOptionsForm.action = '../mtProfiles/saveOrUpdate.htm';
        MTOptionsForm.submit();
    } else {
        testHost(engine_name);
    }
}
</SCRIPT>
</head>
<body>
<FORM NAME="dForm" id="dForm" METHOD="POST" ACTION="../mtProfiles/main.htm">
<input type="hidden" name="mtProfileID">
</FORM>

<DIV>
		<%@ include file="/WEB-INF/pages/header.jspIncl" %>	

		<div>
			<p />
			<p />
			MT Profile Detail
			<p />
			<p />
			<p />
			<p />
		</div>
		
		<div id="content">
		<form action="" method="POST" id="MTOptionsForm" name="MTOptionsForm">
			<input type="HIDDEN" value="" name="formAction"> 
			<input type="HIDDEN" value="" name="radioBtn"> 
			<input type="HIDDEN" value="" id="idURL_flag" name="ms_mt_url_flag">
			<input type="HIDDEN" value="${mtProfile.id}" name="mtProfileId"> 
			<div id="baseInfo">
				<table cellspacing="2" cellpadding="2" border="0" class="standardText">
					<thead>
					</thead>
					<colgroup>
						<col valign="top" align="right" class="standardText">
						<col valign="top" align="left" class="standardText">
					</colgroup>
					<tbody>
						<tr>
							<td align="LEFT">
								Name<font color="red">*</font>:
							</td>
							<td>
								<input type="text" size="30" maxlength="50" value="${mtProfile.mtProfileName}"
								name="MtProfileName" id="MtProfileName" class="standardText">
							</td>
						</tr>
						<tr>
							<td align="LEFT">Description:</td>
							<td>
								<textarea maxlength="150" name="description" id="description" 
									cols="40" rows="6" class="standardText">${mtProfile.description}</textarea>
							</td>
						</tr>
						<tr>
							<td align="left">MT Engine:</td>
							<td>
							<select onchange="contorlMTOptionShowing(this.value)"
								name="machineTranslation.engine" id="mtEngine"
								class="standardText">
									<option value="Asia_Online">Asia_Online</option>
									<option value="IPTranslator">IPTranslator</option>
									<option value="MS_Translator">MS_Translator</option>
									<option value="ProMT">ProMT</option>
									<option value="Safaba">Safaba</option>	
									<option value="Google_Translate">Google_Translate</option>														
							</select>
							</td>
						</tr>

						<tr>
							<td align="LEFT" style="vertical-align: middle">MT Confidence Score:
							</td>
							<td>
								<input value="${mtProfile.mtConfidenceScore}" maxlength="3" size="1"
								name="mtConfidenceScore" id="mtConfidenceScore">%
							</td>
						</tr>
<!--
						<tr>
							<td align="left">Show In Editor:</td>
							<td><input type="checkbox" id="idShowInEditor"
								name="machineTranslation.showInEditor" class="standardText">
							</td>
						</tr>
-->
					</tbody>
				</table>

					<!-- prevent Chrome filling the form automatically. For GBS-1209-->
					<div style="display: none">
						<input type="password">
					</div>
					

					<!-- **************** Promt MT Options : Start ************************* -->
					<div style="display: none;" id="ptsDiv">
					<p>
					<table cellspacing="2" cellpadding="2" border="0" class="standardText">
						<tbody>
							<tr>
								<td colspan="2"><b>Set web service provider URL. For example:
										http://&lt;server_name:port&gt;/pts8/services/ptservice.asmx(?wsdl)
								</b></td>
							</tr>
							<tr>
								<td align="LEFT">PTS URL<font color="red">*</font>:</td>
								<td><input type="text" size="99" maxlength="99" value=" "
									name="ptsurl" id="ptsurl" class="standardText"></td>
							</tr>
							<tr>
								<td align="LEFT">User Name:</td>
								<td><input type="text" size="20" maxlength="99" value=" "
									name="username" id="username" class="standardText"> &nbsp;(If anonymous access is allowed, username is not needed and will be ignored.)
								</td>
							</tr>
							<tr>
								<td align="LEFT">Password:</td>
								<td><input type="password" size="20" maxlength="99" value="null " 
									name="password" id="password" class="standardText"> &nbsp;(If anonymous access is allowed, password is not needed and will be ignored.)
								</td>
							</tr>							

							<!-- ----------------*******************extend info -->
							<tr style="display: none;" id="promtinfo">
								<td align="left"><b>Local Pair Name</b></td>
								<td><b>Topic Template</b></td>
							</tr>

						</tbody>
					</table>
					<p></p>
					</div>
					<!-- **************** Promt MT Options : End *************************** -->
					

					<!-- **************** MS Translator MT Options : Start ***************** -->
					<div style="display: block;" id="msMtDiv">
						
							<p>
							<table width="82%" cellspacing="2" cellpadding="2" border="0" class="standardText">
								<tbody><tr>
									<td colspan="3"><b>Set web service provider URL.</b></td>
								</tr>
								<tr>
									<td align="LEFT">MS Translator URL<font color="red">*</font>:</td>
									<td><input type="text" size="90" maxlength="99" value="http://api.microsofttranslator.com/V2/Soap.svc" name="ms_mt_url" id="idMsMtUrl" class="standardText"></td>
								</tr>
								<tr>
									<td align="LEFT">Client ID<font color="red">*</font>:</td>
									<td><input type="text" size="60" maxlength="100" value=" " name="ms_mt_client_id" id="idMsMtClientid" class="standardText"> <a target="_blank" href="http://go.microsoft.com/?linkid=9782667">(Get one)</a>
									</td>
								</tr>
								<tr>
									<td align="LEFT">Client Secret<font color="red">*</font>:</td>
									<td><input type="password" size="60" maxlength="100" value="null " name="ms_mt_client_secret" id="idMsMtClientSecret" class="standardText"></td>
								</tr>
								<tr>
									<td align="LEFT">Category:
									</td>
									<td colspan="2"><input type="text" size="60" maxlength="50" value="general" name="ms_mt_category" id="idMsMtCategory" class="standardText"> 
									</td>
								</tr>
								
							</tbody></table>
							</p><p>
						</p>
					</div>
					<!-- **************** MS Translator MT Options : End ******************* -->					
					
					
					<!-- **************** Asia Online MT Options : Start ****************** -->
                    <div id="aoMtDiv" style="display: none;">
                        <p></p>
                        <table border="0" cellpadding="2" cellspacing="2" class="standardText" width="88%">
                            <tbody>
                                <tr>
                                    <td colspan="2"><strong>Settings for Asia Online machine translation engine.</strong></td>
                                </tr>
                                <tr>
                                    <td align="left">Asia Online URL<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idAoMtUrl" maxlength="99" name="ao_mt_url" size="90" type="text" value=" "></td>
                                </tr>
                                <tr>
                                    <td align="left">Asia Online Port<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idAoMtPort" maxlength="5" name="ao_mt_port" size="20" type="text" value="80 "></td>
                                </tr>
                                <tr>
                                    <td align="left">User Name<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idAoMtUsername" maxlength="99" name="ao_mt_username" size="20" type="text" value=" "></td>
                                </tr>
                                <tr>
                                    <td align="left">Password<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idAoMtPassword" maxlength="99" name="ao_mt_password" size="20" type="password" value="null "></td>
                                </tr>
                                <tr>
                                    <td align="left">Account Number<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idAoMtAccountNumber" maxlength="10" name="ao_mt_account_number" size="20" type="text" value=" "></td>
                                </tr>
                                <tr>
                                    <td colspan="2">&nbsp;</td>
                                </tr>

                                <tr id="asiainfo" style="display: none;">
                                    <td align="left"><strong>Locale Pair Name</strong></td>
                                    <td><strong>Domain Combination</strong></td>
                                </tr>
                            </tbody>
                        </table>
                        <p>
                        <p></p>
                    </div>
                    <!-- **************** Asia Online MT Options : End  ****************** -->					
					

					<!-- **************** Safaba MT Options : Start 	****************** -->
                    <div id="safaMtDiv" style="display: none;">
                        <p></p>

                        <table border="0" cellpadding="2" cellspacing="2" class="standardText" width="62%">
                            <tbody>
                                <tr>
                                    <td colspan="3"><strong>Settings for Safaba machine translation engine.</strong></td>
                                </tr>
                                <tr>
                                    <td align="left" width="25%">Host Name<font color="red">*</font>:</td>
                                    <td width="60px"><input class="standardText" id="idSafaMtHost" maxlength="99" name="safa_mt_host" size="50" type="text" value=""></td>
                                </tr>
                                <tr>
                                    <td align="left">Port<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idSafaMtPort" maxlength="5" name="safa_mt_port" size="50" type="text" value="80"></td>
                                </tr>
                                <tr>
                                    <td align="left">User Name<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idSafaMtCompanyName" maxlength="99" name="safa_mt_company_name" size="50" type="text" value=""></td>
                                </tr>
                                <tr>
                                    <td align="left">Password<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idSafaMtPassword" maxlength="99" name="safa_mt_password" size="50" type="password" value="null"></td>
                                </tr>
                                <tr>
                                    <td align="left">Account Name<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idSafaMtClient" maxlength="99" name="safaba_client" size="50" type="text" value=""></td>
                                </tr>
                            </tbody>
                        </table>

                        <p></p>
                    </div>
                    <p>
                    <!-- **************** Safaba MT Options : End 		  ****************** -->				
				
				
					<!-- **************** IPTranslator MT Options : Start ****************** -->
                    </p>
                    <div id="IPTranslatorDiv" style="display: none;">
                        <table border="0" cellpadding="2" cellspacing="2" class="standardText" width="90%">
                            <tbody>
                                <tr>
                                    <td colspan="3"><strong>Settings for IPTranslator machine translation engine.</strong></td>
                                </tr>
                                <tr>
                                    <td align="left">IPTranslator URL<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idIPUrl" maxlength="99" name="mt_ip_url" size="90" type="text" value=" "></td>
                                </tr>
                                <tr>
                                    <td align="left">IPTranslator KEY<font color="red">*</font>:</td>
                                    <td><input class="standardText" id="idIPKey" maxlength="99" name="mt_ip_key" size="20" type="password" value="null "></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <!-- **************** IPTranslator MT Options : End ****************** -->	
                    
                    
                    <!-- **************** Google MT Options : Start ****************** -->
					<div id="googleDiv" style="display: none;">
						<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0"
							class="standardText" WIDTH="90%">
							<tr>
								<td colspan="3"><b>Settings for Google machine
										translation engine. Google uses fixed URL:
										https://www.googleapis.com</b></td>
							</tr>
							<tr>
								<td ALIGN="LEFT">Google API Key<font color="red">*</font>:
								</td>
								<td><INPUT CLASS="standardText" ID="idAPIKey"
									NAME="mt_google_api_key" value="" TYPE="text" MAXLENGTH="80"
									SIZE="80" /></td>
							</tr>
						</TABLE>
					</div>
					<!-- **************** Google MT Options : End ******************** -->			
				
			</div>
			<div>
				<table width="88%" cellspacing="2" cellpadding="2" border="0" id="optTable" class="standardText">
				</table>
			</div>
		</form>

		<input type="BUTTON" onclick="fnCancel();" id="Cancel" value="Cancel"> 
		<input type="BUTTON" onclick="fnSaveOrUpdate();" id="OK" value="Save"> 
		<input type="BUTTON" style="display: none" id="edit" value="Edit Base Info">
	</div>
	
	<%@ include file="/WEB-INF/pages/footer.jspIncl" %>
</DIV>
</body>
</html>