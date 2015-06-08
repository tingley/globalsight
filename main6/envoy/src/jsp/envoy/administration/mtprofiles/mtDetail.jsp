<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,
	com.globalsight.everest.projecthandler.EngineEnum,
	com.globalsight.everest.webapp.javabean.NavigationBean,
	com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.everest.servlet.util.SessionManager,
	com.globalsight.everest.webapp.WebAppConstants,
	com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants,
	com.globalsight.everest.projecthandler.MachineTranslationProfile,
	com.globalsight.machineTranslation.MachineTranslator,
    com.globalsight.ling.common.XmlEntities,
	java.util.ResourceBundle"
	session="true"%>
<%@ include file="/envoy/common/header.jspIncl"%>

<jsp:useBean id="saveMTOptions" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelMTOptions" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="nextAsiaOnline" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
	class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    XmlEntities m_xmlEncoder = new XmlEntities();
    ResourceBundle bundle = PageHandler.getBundle(session);
    String lbNext = "get Info";
    String lbSave = bundle.getString("lb_save");
    String title = (String)request.getAttribute("title");
    
    String testUrl = self.getPageURL() + "&formAction=testHost";
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    //If return back from promt next page, refer to the changedmtProfile.
    MachineTranslationProfile mtProfile = (MachineTranslationProfile) sessionMgr
            .getAttribute(MTProfileConstants.MT_PROFILE);
    MachineTranslationProfile mtProfile4val = new MachineTranslationProfile();
    //Urls of the links on this page
    String saveMTOptionsUrl = saveMTOptions.getPageURL() + "&formAction=saveMTOptions";
    String cancelMTOptionsUrl = cancelMTOptions.getPageURL();
    String nextUrlForPromt = next.getPageURL();
    String nextUrlForAsiaOnline = nextAsiaOnline.getPageURL();

    String current_engine = mtProfile.getMtEngine();
    String jsonInfo = mtProfile.getJsonInfo();
    String srRSValue = null;
    String srYUValue = null;
    if ("MS_Translator".equalsIgnoreCase(current_engine))
    {
    	srRSValue = mtProfile.getPreferedLangForSr("sr_RS");
    	srYUValue = mtProfile.getPreferedLangForSr("sr_YU");
    }
    String exInfoVal = mtProfile.getExInfoVal();
    // For "MS_Translator" and "Asia_Online",need check if connection is available.
    String action = (String) request.getAttribute("action");
    String exceptionInfo = (String) request.getAttribute("ExceptionInfo");

	String saPasswd = (String) request.getAttribute(MTProfileConstants.MT_SAFA_PASSWORD);
%>
<HTML>

<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%=title%></TITLE>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>

<SCRIPT language="JavaScript">
	var needWarning = false;
	var objectName = "MT Options";
	var guideNode = "mtProfiles";
	var helpFile = "<%=bundle.getString("help_tmprofile_mt_options")%>"; 
	var forceSave=false;
	var current_engine='<%=current_engine%>';
	var jsonInfo=<%=jsonInfo%>;
	var exInfoVal='<%=exInfoVal%>';
	
	$(document).ready(function() {
		if(<%=mtProfile.isIncludeMTIdentifiers()%>)
		{
		    setDisableTR('mtIdentifierLeadingTR', false);
		    setDisableTR('mtIdentifierTrailingTR', false);
		}
		else
		{
		    setDisableTR('mtIdentifierLeadingTR', true);
		    setDisableTR('mtIdentifierTrailingTR', true);
		}
		$("#includeMTIdentifiers").click(function(){
			if(this.checked)
			{
				setDisableTR('mtIdentifierLeadingTR', false);
			    setDisableTR('mtIdentifierTrailingTR', false);
			}
			else
			{
				setDisableTR('mtIdentifierLeadingTR', true);
			    setDisableTR('mtIdentifierTrailingTR', true);
			}
		});
	});
	  
	$(function(){
		contorlMTOptionShowing();
		$("input").each(function(){
			var n=$.trim($(this).val());
			if(n=="null"||!n){
				$(this).val("");
			}
		});
		$("#edit").click(function(){
			if(confirm("Edit the base info need re-test MT engine.")){
				$("#MTOptionsForm").unmask("Connecting engine...");
				forceSave=false;
				$(this).hide();
				$("#OK").val("<%=lbNext%>");
				$("#optTable").empty();
			}
		});

		if(!exInfoVal)return;
		 pushData(jsonInfo,current_engine);
		 var exInfoVals=exInfoVal.split(",");
		 var dirName=$(".dirName");
		 if(!dirName.length)return;
		 $.each(exInfoVals,function(i,val){
		 	var key="#"+val.split("@")[0];
		 	$(key).val(val);
		 });
		
	})
	
	function submitForm(formAction)
	{
		var name=$("#MtProfileName").val();
		
		if (formAction == "cancelMTOptions") 
		{
			if (confirmJump())
			{
				MTOptionsForm.action = '<%=cancelMTOptionsUrl%>';
				MTOptionsForm.submit();
			}
			else 
			{
				return false;
			}
		}
		else if (formAction == "saveMTOptions") 
		{
			if(!name){
				alert("Please check name");
				return;
			}
			var mtIdentifiersRegex = /[\"\'<>&]/;
			var mtIdentiferLeading = $.trim($("#mtIdentifierLeading").val());
			var mtIdentiferTrailing = $.trim($("#mtIdentifierTrailing").val());
			if (mtIdentifiersRegex.test(mtIdentiferLeading))
			{
				alert("<%=bundle.getString("jsmsg_mt_invalid_mt_identifiers")%>");
	            $("#mtIdentifierLeading").focus();
	            return;
			}
			if (mtIdentifiersRegex.test(mtIdentiferTrailing))
			{
				alert("<%=bundle.getString("jsmsg_mt_invalid_mt_identifiers")%>");
	            $("#mtIdentifierTrailing").focus();
	            return;
			}
			$("#OK").attr("disabled",true);
		    var isShowInEditor = $("#idShowInEditor").is(":checked");
			var engine_name = document.getElementById('mtEngine').value;
			if(forceSave){
				MTOptionsForm.action = '<%=saveMTOptionsUrl%>';
				MTOptionsForm.submit();
			} else {
				testHost(engine_name);
			}
		}
	}
	
	function testHost(formAction){
		if(checkOptions(formAction)){
			$("#baseInfo").mask("Connecting engine...");
			 $("#MTOptionsForm").ajaxSubmit({  
		            type: 'post',  
		            url: "<%=testUrl%>" , 
		            dataType:'json',
		            timeout:100000000,
		            success: function(data){  
		            	$("#baseInfo").unmask("Connecting engine...");
		            	$("#OK").attr("disabled",false);
		                if(data){
		                	if(data.ExceptionInfo){
			                	var vl=data.ExceptionInfo+"";
			                	//var s=vl.indexOf(":");
								//vl=vl.substr(s+1);
								alert(vl);
								
		                	}else if(data.Info){
		                		 self.location.href='<%=cancelMTOptionsUrl%>'; 
		                	}else{
		                		 pushData(data,formAction);
		                	}
		                }else{
		                	alert("<%=bundle.getString("lb_tm_mt_engine_work_well")%>");
		                }
		                
		            },  
		            error: function(XmlHttpRequest, textStatus, errorThrown){  
		            	$("#baseInfo").unmask("Connecting engine...");
		                alert( "error "+textStatus);  
		                $("#OK").attr("disabled",false);
		            }  
		        }); 
		}else{
			$("#OK").attr("disabled",false);
		}
	}
	
	function pushData(jsonData,formAction){
		if(!jsonData)return;
		$("#optTable").empty();
		var cont="";
		var info;
		if("ProMT"==formAction){
			info=$("#promtinfo").clone(true);
			for(var key in jsonData){  
			   	var json=(jsonData[key]+"").split(",");
			      var optStr="";
			      for(var i=0;i<json.length;i++){  
				      	optStr +="<option value='"+key+"@"+json[i]+"' >"+json[i]+" </option>";
				  }   
			    
		   		cont+="<tr>  <td align='left' >"+key.split("@")[0]+"</td> <td><select id='"+key.split("@")[0]+"' class='dirName' name='dirName'>"+optStr+"</select> </td> </tr>";
    		}
		}else{		
			info=$("#asiainfo").clone(true);
    		for(var key in jsonData){  
			   	var json=(jsonData[key]+"").split(",");
			      var optStr="";
			      for(var i=0;i<json.length;i++){  
			    		var valshow=json[i].split("@");
				      	optStr +="<option value='"+key.split("#")[0]+valshow[0]+"' >"+valshow[1]+" </option>";
				  }   
			    
		   		cont+="<tr>  <td align='left' >"+key.split("#")[1]+"</td> <td><select id='"+key.split("#")[0].split("@")[0]+"' class='dirName' name='dirName'>"+optStr+"</select> </td> </tr>";
    		}
		}
		$("#baseInfo").mask("Please select locale pairs and save.");
		info.show();
		$("#optTable").append(info); 
	   	$("#optTable").append(cont);  
	   	forceSave=true;
	    $("#edit").show();
	   	$("#OK").val("<%=lbSave%>");
	}

	function checkOptions(formAction)
	{
		if (formAction == "MS_Translator")
		{
			var msTranslatorUrl = $.trim($("#idMsMtUrl").val());
			var msTranslatorClientID = $.trim($("#idMsMtClientid").val());
			var msTranslatorClientSecret = $.trim($("#idMsMtClientSecret").val());
			if (msTranslatorUrl == null || msTranslatorUrl == "") 
			{
				alert("<%=bundle.getString("msg_tm_mt_url_empty")%>");
				return false;
			}
			if (msTranslatorClientID == null || msTranslatorClientID == "") 
			{
				alert("<%=bundle.getString("msg_tm_mt_clientid_empty")%>");
				return false;
			}
			if (msTranslatorClientSecret == null || msTranslatorClientSecret == "") 
			{
				alert("<%=bundle.getString("msg_tm_mt_client_secret_empty")%>");
				return false;
			}
		}
		else if (formAction == "Safaba")
		{
			var safaHostName = $.trim($("#idSafaMtHost").val());
			var safaPort = $.trim($("#idSafaMtPort").val());
			var safaCompanyName = $.trim($("#idSafaMtCompanyName").val());
			var safaPassword = $.trim($("#idSafaMtPassword").val());
			var safaClient = $.trim($("#idSafaMtClient").val());
			if (safaHostName == "" || safaPort == "" || safaCompanyName == "" 
					|| safaPassword == "" || safaClient == "")
			{
				alert("<%=bundle.getString("msg_required_attribute_lost")%>");
				return false;
			}
		}
		else if (formAction == "Asia_Online")
		{
			var canGoOn = checkAoOptions();
			if (canGoOn != true)
			{
	           	return false
	        }
		}
		else if (formAction == "ProMT") 
		{
            MTOptionsForm.action = '<%=nextUrlForPromt%>';
			var ptsurl = $.trim($("#ptsurl").val());
			if (ptsurl != null && ptsurl != "") {
				if (ptsurl.indexOf('http') != 0) {
					alert("<%=bundle.getString("msg_tm_mt_url_format")%>");
					return false;
				}
			} else {
				alert("<%=bundle.getString("msg_tm_mt_url_empty")%>");
				return false;
			}
		}
		else if (formAction == "IPTranslator") 
		{
			var ipurl = $.trim($("#idIPUrl").val());
			if (ipurl != null && ipurl != "") {
				if (ipurl.indexOf('http') != 0) {
					alert("Missing or invalid protocol in IP Translator URL!");
					return false;
				}
			} else {
				alert("Machine Translation engine url can't be empty or null!");
				return false;
			}
		}
		else if (formAction == "DoMT")
		{
			var doMtUrl = $.trim($("#idDoMtUrl").val());
			if (doMtUrl != null && doMtUrl != "") {
                if (doMtUrl.indexOf('http') != 0) {
                    alert("Missing or invalid protocol in DoMT URL!");
                    return false;
                }
			} else {
                alert("Machine Translation engine url can't be empty or null!");
                return false;
			}
			var doMtEngineName = $.trim($("#idDoMtEngineName").val());
			if (doMtEngineName == null || doMtEngineName == "") {
				alert("Invalid DoMT engine name!");
                return false;
			}
		}
		else if (formAction == "Google_Translate") 
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

	function checkMtConfidenceScoreValid()
	{
        var mtConfidenceScore = document.getElementById('mtConfidenceScore').value;

        if (mtConfidenceScore == null || trim(mtConfidenceScore)== "" ) 
        {
            alert("<%=bundle.getString("msg_tm_mt_confidence_score_null")%>");
            return false;
        }
        else if (!isAllDigits(mtConfidenceScore)) 
        {
            alert("<%=bundle.getString("msg_tm_mt_confidence_score_invalid")%>");
            return false;
        }
        else if (!checkIsVaildPercent(mtConfidenceScore)) 
        {
           return false;
        }

        return true;
	}

	function safabaInputChanged()
	{
		var formerHost = "<c:out value='${safa_mt_host}'/>";
		var currentHost = $("#idSafaMtHost").val();
		
		var formerPort = "<c:out value='${safa_mt_port}'/>";
		var currentSafaPort = $("#idSafaMtPort").val();
		
		var formerCompany = "<c:out value='${safa_mt_company_name}'/>";
		var currentSafaCompanyName = $("#idSafaMtCompanyName").val();
		
		var formerPassword = "<%=saPasswd == null ? "" : saPasswd.hashCode()%>";
		var currentSafaPassword = $("#idSafaMtPassword").val();
		
		var formerClient = "<c:out value='${safaba_client}'/>";
		var currentSafaClient = $("#idSafaMtClient").val();
		
		if (formerHost != currentHost || formerPort != currentSafaPort 
				|| formerCompany != currentSafaCompanyName 
				|| formerClient != currentSafaClient) {
			return true;
		}
		else if(!isValidPassswd(currentSafaPassword) && formerPassword != currentSafaPassword.hashCode()) 
		{
			return true;
		}
		
		return false;
	}


	function checkIsVaildPercent(percent)
	{
	    var submit = false;
		var i_percent = parseInt(percent);
		if(i_percent > 100 || i_percent < 0)
		{
			alert("<%=bundle.getString("msg_tm_number_scope_0_100")%>");
			submit = false;
		}
		else
		{
			submit = true;
		}

		return submit;
	}
	

	function contorlMTOptionShowing()
	{
        //1.Control MT DIVs display
        var ptsDiv = document.getElementById("ptsDiv");
        var msMtDiv = document.getElementById("msMtDiv");
        var aoMtDiv = document.getElementById("aoMtDiv");
        var safaMtDiv = document.getElementById("safaMtDiv");
        var IPTranslatorDiv=document.getElementById("IPTranslatorDiv");
        var doMtDiv=document.getElementById("doMtDiv");
		var googleDiv = document.getElementById("googleDiv");
        
        var engineSelect = document.getElementById("mtEngine");
        var selectedEngineName = engineSelect.options[engineSelect.selectedIndex].value;
        // hide them all first
        ptsDiv.style.display='none';
        msMtDiv.style.display='none';
        aoMtDiv.style.display='none';
        safaMtDiv.style.display='none';
        IPTranslatorDiv.style.display='none';
        doMtDiv.style.display='none';
        googleDiv.style.display='none';

        // display corresponding div by selected engine name.
	    if (selectedEngineName.toLowerCase() == "google_translate") 
		{
           googleDiv.style.display='block';
	    }
	    else if (selectedEngineName.toLowerCase() == "promt") 
		{
            ptsDiv.style.display='block';
	    }
	    else if (selectedEngineName.toLowerCase() == "ms_translator") 
		{
            msMtDiv.style.display='block';
	    }
	    else if(selectedEngineName.toLowerCase() == "asia_online")
	    {
            aoMtDiv.style.display='block';
	    }
	    else if(selectedEngineName.toLowerCase() == "safaba")
	    {
	    	safaMtDiv.style.display='block';
	    }
	    else if(selectedEngineName == "IPTranslator")
	    {
	    	IPTranslatorDiv.style.display='block';
	    }
        else if(selectedEngineName == "DoMT")
        {
        	doMtDiv.style.display='block';
        }

        //2.Display "Save" button or "Next" button
        var okBtn = document.getElementById("OK");
	    if (selectedEngineName.toLowerCase() == "google_translate"
		    || selectedEngineName.toLowerCase() == "ms_translator" 
		    || selectedEngineName.toLowerCase() == "safaba"
		    || selectedEngineName == "IPTranslator"
		    || selectedEngineName == "DoMT")
	    {
	    	okBtn.value = "<%=lbSave%>";
	    }
	    else
	    {
	    	okBtn.value = "<%=lbNext%>";
	    }
	}

	function checkAoOptions()
	{
        var aoMtUrl = $.trim($('#idAoMtUrl').val());
        var aoMtPort = $.trim($('#idAoMtPort').val());
        var aoMtUserName = $.trim($('#idAoMtUsername').val());
        var aoMtPassword = $.trim($('#idAoMtPassword').val());
        var aoMtAccountNumber = $.trim($('#idAoMtAccountNumber').val());
        //var jobNameRegex = /[\\/:;\*\?\|\"\'<>&%]/;
   	    //if (jobNameRegex.test(customerForm.jobName.value))

        if (!aoMtUrl)
        {
            alert("<%=bundle.getString("lb_tm_ao_mt_url_empty")%>");
            return false;
        }
   	  
        else if (!aoMtPort||!isAllDigits(aoMtPort))
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_port_empty_invalid")%>");
            return false;
        }
        else if (!aoMtUserName)
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_username_empty")%>");
            return false;
        }
        else if (!aoMtPassword )
        {
        	alert("<%=bundle.getString("lb_tm_ao_mt_password_empty")%>");
            return false;
        }
        else if (!aoMtAccountNumber||!isAllDigits(aoMtAccountNumber))
        {
        	alert("<%=bundle
					.getString("lb_tm_ao_mt_account_number_empty_invalid")%>");            
            return false;
        }

        return true;
	}
	
	//remove all whitespace on left and right
	function trim(str)
	{
	     return	str.replace(/(^\s*)|(\s*$)/g, '');
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

	String.prototype.hashCode = function() {
		for ( var ret = 0, i = 0, len = this.length; i < len; i++) {
			ret = (31 * ret + this.charCodeAt(i)) << 0;
		}
		return ret;
	};
	
	function setDisableTR(trId, isDisabled) 
	{
		var trElem = document.getElementById(trId);
		var color;
		if (isDisabled) 
		{
			color = "gray";
		} 
		else 
		{
			color = "black";
		}
		trElem.style.color = color;
		
		// Operate text elements
		elems = trElem.getElementsByTagName("input");
		for ( var i = 0; i < elems.length; i++) 
		{
			if ("text" == elems[i].type) 
			{
				elems[i].readOnly = isDisabled;
				elems[i].style.color = color;
			}
		}
	}
	
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

	<DIV ID="contentLayer"
		STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute; WIDTH: 850px; TOP: 108px">

		<DIV CLASS="mainHeading" id="idHeading"><%=title%></DIV>

		<FORM NAME="MTOptionsForm" id="MTOptionsForm" METHOD="POST" action="">
			<INPUT TYPE="HIDDEN" NAME="formAction" VALUE=""/>
			<INPUT TYPE="HIDDEN" NAME="radioBtn" VALUE="" />
			<INPUT TYPE="HIDDEN" NAME="ms_mt_url_flag" id="idURL_flag" VALUE="" />
			<div id="baseInfo">
				<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
					<THEAD>
    					<COL align="right" valign="top" CLASS="standardText">
    					<COL align="left" valign="top" CLASS="standardText">
					</THEAD>
					<tr>
						<td ALIGN="LEFT">Name<font color="red">*</font>:</td>
						<td><INPUT CLASS="standardText" id="MtProfileName"
							name="MtProfileName" value="<%=mtProfile.getMtProfileName()%>"
							TYPE="text" MAXLENGTH="50" SIZE="30" /></td>
					</tr>
					<tr>
						<td ALIGN="LEFT"><%=bundle.getString("lb_description")%>:</td>
						<td><textarea CLASS="standardText" rows="6" cols="40" id="description" name="description" MAXLENGTH="150"
								value=""><%=mtProfile.getDescription()%></textarea></td>
					</tr>
					<TR>
						<TD align="left"><%=bundle.getString("lb_tm_mt_engine")%>:</TD>
						<TD><SELECT CLASS="standardText" ID="mtEngine"
							NAME="<%=MTProfileConstants.MT_ENGINE%>"
							onchange="contorlMTOptionShowing(this.value)">
								<%EngineEnum[] engines = EngineEnum.values();
							    for (int i = 0; i < engines.length; i++) {
							        String _engine = engines[i].name();
							        // DoMT is only for internal usage for now(8.5.2).
							        if ("domt".equalsIgnoreCase(_engine))
							        {
//							            continue;
							        }
							        String isSelected = "";

							        if (_engine.equalsIgnoreCase(current_engine)) {
							            isSelected = " selected";
							        }%>
								<OPTION VALUE="<%=_engine%>" <%=isSelected%>><%=_engine%></OPTION>
								<%}%>
						</SELECT></TD>
					</TR>

					<TR>
						<TD ALIGN="LEFT" STYLE="vertical-align: middle"><%=bundle.getString("lb_tm_mt_confidence_score")%>:</TD>
						<TD><INPUT CLASS="standardText" ID="mtConfidenceScore" NAME="mtConfidenceScore"
							SIZE="1" MAXLENGTH="3"
							VALUE="<%=mtProfile.getMtConfidenceScore()%>">%</TD>
					</TR>

					<TR>
						<TD align="left"><%=bundle.getString("lb_show_in_editor")%>:</TD>
						<TD><INPUT CLASS="standardText"
							NAME="<%=MTProfileConstants.MT_SHOW_IN_EDITOR%>"
							id="idShowInEditor"
							<%=mtProfile.isShowInEditor() ? "checked" : ""%> TYPE="checkbox" />
						</TD>
					</TR>
					
					<TR>
						<TD align="left"><%=bundle.getString("lb_mt_include_mt_identifiers")%>:</TD>
						<TD><INPUT CLASS="standardText" TYPE="checkbox" ID="includeMTIdentifiers" NAME="<%=MTProfileConstants.MT_INCLUDE_MT_IDENTIFIERS%>" <%=mtProfile.isIncludeMTIdentifiers() ? "checked" : ""%> />
						</TD>
					</TR>
					<tr id="mtIdentifierLeadingTR">
					    <td></td>
                        <td><%=bundle.getString("lb_mt_mt_identifier_leading")%>:
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT CLASS="standardTextBold" TYPE="text" MAXLENGTH="20" SIZE="6" ID="mtIdentifierLeading" NAME="<%=MTProfileConstants.MT_MT_IDENTIFIER_LEADING%>" VALUE="<%=mtProfile.getMtIdentifierLeading() != null ? mtProfile.getMtIdentifierLeading() : ""%>" />
                        </td>
                    </tr>
                    <tr id="mtIdentifierTrailingTR">
                        <td></td>
                        <td><%=bundle.getString("lb_mt_mt_identifier_trailing")%>:
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<INPUT CLASS="standardTextBold" TYPE="text" MAXLENGTH="20" SIZE="6" ID="mtIdentifierTrailing" NAME="<%=MTProfileConstants.MT_MT_IDENTIFIER_TRAILING%>" VALUE="<%=mtProfile.getMtIdentifierTrailing() != null ? mtProfile.getMtIdentifierTrailing() : ""%>" />
                        </td>
                    </tr>
				</TABLE>

				<!-- prevent Chrome filling the form automatically. For GBS-1209-->
				<div style="display: none"><input type="password" /></div>
            </div>

				<!-- **************** Promt MT Options : Start ************************* -->
				<%if ("ProMT".equalsIgnoreCase(current_engine))
				{
				    mtProfile4val = mtProfile;%>
    				<div id="ptsDiv" style="display: block;">
				<%} else {
				    mtProfile4val = new MachineTranslationProfile();%>
					<div id="ptsDiv" style="display: none;">
				<%}%>
						<p>
						<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0"
							class="standardText" WIDTH="145%">
							<tr>
								<td colspan="2"><b><%=bundle.getString("lb_tm_pts_title")%>:
										http://&lt;server_name:port&gt;/pts8/services/ptservice.asmx(?wsdl)
								</b></td>
							</tr>
							<tr>
								<td ALIGN="LEFT"><%=bundle.getString("lb_tm_pts_url")%><font
									color="red">*</font>:</td>
								<td><INPUT CLASS="standardText" ID="ptsurl"
									NAME="<%=MTProfileConstants.MT_PTSURL%>"
									value="<%=mtProfile4val.getUrl()%>" TYPE="text"
									MAXLENGTH="99" SIZE="99" /></td>
							</tr>
							<tr>
								<td ALIGN="LEFT"><%=bundle.getString("lb_user_name")%>:</td>
								<td><INPUT CLASS="standardText" ID="username"
									NAME="<%=MTProfileConstants.MT_PTS_USERNAME%>"
									value="<%=mtProfile4val.getUsername()%>" TYPE="text"
									MAXLENGTH="99" SIZE="20" /> &nbsp;(<%=bundle.getString("msg_tm_mt_anonymous_username")%>)
								</td>
							</tr>
							<tr>
								<td ALIGN="LEFT"><%=bundle.getString("lb_password")%>:</td>
								<td><INPUT CLASS="standardText" ID="password"
									NAME="<%=MTProfileConstants.MT_PTS_PASSWORD%>"
									value="<%=mtProfile4val.getPassword()%>" TYPE="password"
									MAXLENGTH="99" SIZE="20" /> &nbsp;(<%=bundle.getString("msg_tm_mt_anonymous_pwd")%>)
								</td>
							</tr>

							<%if (exceptionInfo != null && !"".equals(exceptionInfo.trim())
							        && action != null && "previous".equals(action)) {%>
							<tr>
								<td colspan="2">&nbsp;</td>
							</tr>
							<tr>
								<td align="left" colspan="2"><font color="red"><%=bundle.getString("msg_tm_mt_exception_title")%>
										:&nbsp;<b><%=exceptionInfo%></b></font></td>
							</tr>
							<%if (exceptionInfo.indexOf("404") != -1) {%>
							<tr>
								<td align="left" colspan="2"><%=bundle.getString("msg_tm_mt_exception_404")%></td>
							</tr>
							<%}
				if (exceptionInfo.indexOf("401") != -1) {%>
							<tr>
								<td align="left" colspan="2"><%=bundle.getString("msg_tm_mt_exception_401")%></td>
							</tr>
							<%}
			}%>
							<!-- ----------------*******************extend info -->
							<tr id="promtinfo" style="display: none;">
								<td align="left"><b><%=bundle.getString("lb_tm_locale_pair_name")%></b></td>
								<td><b><%=bundle.getString("lb_tm_topic_template")%></b></td>
							</tr>

						</TABLE>
						<p>
					</div>
					<!-- **************** Promt MT Options : End *************************** -->

					<!-- **************** MS Translator MT Options : Start ***************** -->
					<%if ("MS_Translator".equalsIgnoreCase(current_engine))
					{
					    mtProfile4val = mtProfile;%>
					    <div id="msMtDiv" style="display: block;">
					<%} else {
					    mtProfile4val = new MachineTranslationProfile();%>
						<div id="msMtDiv" style="display: none;">
					<%}%>
							<p>
							<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="100%">
								<tr>
									<td colspan="3"><b><%=bundle.getString("lb_tm_ms_mt_title")%></b></td>
								</tr>
								<tr>
									<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ms_mt_url")%><font
										color="red">*</font>:</td>
									<td colspan="2"><INPUT CLASS="standardText" ID="idMsMtUrl"
										NAME="<%=MTProfileConstants.MT_MS_URL%>"
										value="<%=mtProfile4val.getUrl()%>" TYPE="text"
										MAXLENGTH="99" SIZE="90" /></td>
								</tr>
								<tr>
									<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ms_mt_client_id")%><font
										color="red">*</font>:</td>
									<td colspan="2"><INPUT CLASS="standardText" ID="idMsMtClientid"
										NAME="<%=MTProfileConstants.MT_MS_CLIENT_ID%>"
										value="<%=mtProfile4val.getUsername()%>" TYPE="text"
										MAXLENGTH="100" SIZE="90" /> <a
										href="http://go.microsoft.com/?linkid=9782667" target="_blank"><%=bundle.getString("lb_tm_ms_mt_appid_tag")%></a>
									</td>
								</tr>
								<tr>
									<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ms_mt_client_secret")%><font
										color="red">*</font>:</td>
									<td colspan="2"><INPUT CLASS="standardText" ID="idMsMtClientSecret"
										NAME="<%=MTProfileConstants.MT_MS_CLIENT_SECRET%>"
										value="<%=mtProfile4val.getPassword()%>" TYPE="password"
										MAXLENGTH="100" SIZE="90" /></td>
								</tr>
								<tr>
									<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ms_mt_category")%>:
									</td>
									<td colspan="2"><INPUT CLASS="standardText"
										ID="idMsMtCategory"
										NAME="<%=MTProfileConstants.MT_MS_CATEGORY%>"
										value="<%=(mtProfile4val.getCategory()==null||"".equals(mtProfile4val.getCategory())) ? "general": mtProfile4val.getCategory() %>"
										TYPE="text"	MAXLENGTH="128" SIZE="90" />
									</td>
								</tr>
								<tr>
								    <td align="left">Serbian (Serbia) [sr_RS]:</td>
								    <td colspan="2">
									    <SELECT CLASS="standardText" ID="sr_RS" NAME="sr_RS">
									    <%
									    	String isCyrlSelected = "sr-Cyrl".equals(srRSValue) ? "selected" : "";
									    %>
											<OPTION VALUE="sr-Cyrl" <%=isCyrlSelected%> >sr-Cyrl</OPTION>
											<OPTION VALUE="sr-Latn" <%="".equals(isCyrlSelected) ? "selected" : "" %> >sr-Latn</OPTION>
										</SELECT>
								    </td>
								</tr>
								<tr>
								    <td align="left">Serbian (YU) [sr_YU]:</td>
								    <td colspan="2">
									    <SELECT CLASS="standardText" ID="sr_YU" NAME="sr_YU">
									    <%
									    	isCyrlSelected = "sr-Cyrl".equals(srYUValue) ? "selected" : "";
									    %>
											<OPTION VALUE="sr-Cyrl" <%=isCyrlSelected%> >sr-Cyrl</OPTION>
											<OPTION VALUE="sr-Latn" <%="".equals(isCyrlSelected) ? "selected" : "" %> >sr-Latn</OPTION>
										</SELECT>
								    </td>
								</tr>
								<%if (action != null && "testMSHost".equals(action)) {
								    if (exceptionInfo != null && !"".equals(exceptionInfo.trim())) {%>
								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" colspan="2"><font color="red"> <%=bundle.getString("lb_tm_mt_ms_engine_error")%>
											:&nbsp;<b><%=exceptionInfo%></b></font></td>
								</tr>
    								<%} else {%>
								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>
								<tr>
									<td align="left" colspan="3"><font color="green"><b><%=bundle.getString("lb_tm_mt_engine_work_well")%></b></font></td>
								</tr>
								<% }
							    } %>
							</TABLE>
							<p>
						</div>
						<!-- **************** MS Translator MT Options : End ******************* -->

						<!-- **************** Asia Online MT Options : Start ****************** -->
						<%if ("Asia_Online".equalsIgnoreCase(current_engine))
						{
						    mtProfile4val = mtProfile;%>
						    <div id="aoMtDiv" style="display: block;">
						<%} else {
							mtProfile4val = new MachineTranslationProfile();%>
						    <div id="aoMtDiv" style="display: none;">
						<%}%>
								<p>
								<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="88%">
									<tr>
										<td colspan="2"><b><%=bundle.getString("lb_tm_ao_mt_title")%></b></td>
									</tr>
									<tr>
										<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_url")%><font
											color="red">*</font>:</td>
										<td><INPUT CLASS="standardText" ID="idAoMtUrl"
											NAME="<%=MTProfileConstants.MT_AO_URL%>"
											value="<%=mtProfile4val.getUrl()%>" TYPE="text"
											MAXLENGTH="99" SIZE="90" /></td>
									</tr>

									<tr>
										<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_port")%><font
											color="red">*</font>:</td>
										<td><INPUT CLASS="standardText" ID="idAoMtPort"
											NAME="<%=MTProfileConstants.MT_AO_PORT%>"
											value="<%=mtProfile4val.getPort() == null ? "" : mtProfile4val.getPort()%>"
											TYPE="text" MAXLENGTH="5" SIZE="20" /></td>
									</tr>

									<tr>
										<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_username")%><font
											color="red">*</font>:</td>
										<td><INPUT CLASS="standardText" ID="idAoMtUsername"
											NAME="<%=MTProfileConstants.MT_AO_USERNAME%>"
											value="<%=mtProfile4val.getUsername()%>" TYPE="text"
											MAXLENGTH="99" SIZE="20" /></td>
									</tr>

									<tr>
										<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_password")%><font
											color="red">*</font>:</td>
										<td><INPUT CLASS="standardText" ID="idAoMtPassword"
											NAME="<%=MTProfileConstants.MT_AO_PASSWORD%>"
											value="<%=mtProfile4val.getPassword()%>" TYPE="password"
											MAXLENGTH="99" SIZE="20" /></td>
									</tr>

									<tr>
										<td ALIGN="LEFT"><%=bundle.getString("lb_tm_ao_mt_account_number")%><font
											color="red">*</font>:</td>
										<td><INPUT CLASS="standardText" ID="idAoMtAccountNumber"
											NAME="<%=MTProfileConstants.MT_AO_ACCOUNT_NUMBER%>"
											value="<%=mtProfile4val.getAccountinfo()%>" TYPE="text"
											MAXLENGTH="10" SIZE="20" /></td>
									</tr>

									<tr>
										<td colspan="2">&nbsp;</td>
									</tr>

									<tr id="asiainfo" style="display: none;">
										<td align="left"><b><%=bundle.getString("lb_tm_ao_mt_locale_pair_name")%></b></td>										
										<td><b><%=bundle.getString("lb_tm_ao_mt_domain_combination")%></b></td>
									</tr>
								</TABLE>

								<p>
							</div>
							<!-- **************** Asia Online MT Options : End ******************** -->

							<!-- **************** Safaba MT Options : Start ****************** -->
							<%if ("Safaba".equalsIgnoreCase(current_engine))
							{
							    mtProfile4val = mtProfile;%>
							    <div id="safaMtDiv" style="display: block;">
							<%} else {
								    mtProfile4val = new MachineTranslationProfile();%>
								<div id="safaMtDiv" style="display: none;">
							<%}%>
									<p>
									<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0"
										class="standardText" WIDTH="62%">
										<tr>
											<td colspan="3"><b><%=bundle.getString("lb_tm_safaba_mt_title")%></b></td>
										</tr>
										<tr>
											<td width="25%" ALIGN="LEFT"><%=bundle.getString("lb_tm_safaba_mt_hostname")%><font
												color="red">*</font>:</td>
											<td width="60px"><INPUT CLASS="standardText"
												ID="idSafaMtHost"
												NAME="<%=MTProfileConstants.MT_SAFA_HOST%>"
												value="<%=mtProfile4val.getUrl()%>" TYPE="text"
												MAXLENGTH="99" SIZE="50" /></td>
										</tr>

										<tr>
											<td ALIGN="LEFT"><%=bundle.getString("lb_tm_safaba_mt_port")%><font
												color="red">*</font>:</td>
											<td><INPUT CLASS="standardText" ID="idSafaMtPort"
												NAME="<%=MTProfileConstants.MT_SAFA_PORT%>"
												value="<%=mtProfile4val.getPort() == null ? "" : mtProfile4val.getPort()%>"
												TYPE="text" MAXLENGTH="5" SIZE="50" /></td>
										</tr>

										<tr>
											<td ALIGN="LEFT"><%=bundle.getString("lb_tm_safaba_mt_username")%><font
												color="red">*</font>:</td>
											<td><INPUT CLASS="standardText" ID="idSafaMtCompanyName"
												NAME="<%=MTProfileConstants.MT_SAFA_COMPANY_NAME%>"
												value="<%=mtProfile4val.getUsername()%>" TYPE="text"
												MAXLENGTH="99" SIZE="50" /></td>
										</tr>

										<tr>
											<td ALIGN="LEFT"><%=bundle.getString("lb_tm_safaba_mt_password")%><font
												color="red">*</font>:</td>
											<td><INPUT CLASS="standardText" ID="idSafaMtPassword"
												NAME="<%=MTProfileConstants.MT_SAFA_PASSWORD%>"
												value="<%=mtProfile4val.getPassword()%>" TYPE="password"
												MAXLENGTH="99" SIZE="50" /></td>
										</tr>

										<tr>
											<td ALIGN="LEFT"><%=bundle.getString("lb_tm_safaba_mt_account_name")%><font
												color="red">*</font>:</td>
											<td><INPUT CLASS="standardText" ID="idSafaMtClient"
												NAME="<%=MTProfileConstants.MT_SAFA_CLIENT%>"
												value="<%=mtProfile4val.getAccountinfo()%>" TYPE="text"
												MAXLENGTH="99" SIZE="50" /></td>
										</tr>

									</TABLE>
								</div>
								<p>
							<!-- **************** Safaba MT Options : End ******************** -->

							<!-- **************** IPTranslator MT Options : Start ****************** -->
							<%if ("IPTranslator".equalsIgnoreCase(current_engine))
							{
							    mtProfile4val = mtProfile;%>
							    <div id="IPTranslatorDiv" style="display: block;">
							<%} else {
							    mtProfile4val = new MachineTranslationProfile();%>
								<div id="IPTranslatorDiv" style="display: none;">
							<%}%>
									<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="90%">
										<tr>
											<td colspan="3"><b><%=bundle.getString("lb_tm_iptranslator_mt_title")%></b></td>
										</tr>
										<tr>
											<td ALIGN="LEFT"><%=bundle.getString("lb_tm_iptranslator_mt_url")%><font
												color="red">*</font>:</td>
											<td><INPUT CLASS="standardText" ID="idIPUrl"
												NAME="<%=MTProfileConstants.MT_IP_URL%>"
												value="<%=mtProfile4val.getUrl()%>" TYPE="text"
												MAXLENGTH="99" SIZE="90" /></td>
										</tr>
										<tr>
											<td ALIGN="LEFT"><%=bundle.getString("lb_tm_iptranslator_mt_username")%><font
												color="red">*</font>:</td>
											<td><INPUT CLASS="standardText" ID="idIPKey"
												NAME="<%=MTProfileConstants.MT_IP_KEY%>"
												value="<%=mtProfile4val.getPassword()%>" TYPE="password"
												MAXLENGTH="99" SIZE="20" /></td>
										</tr>
									</TABLE>
							    </div>
						<div>
							<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="88%" id="optTable"></TABLE>
						</div>
						<!-- **************** Safaba MT Options : End ******************** -->

                        <!-- **************** DoMT MT Options : Start ****************** -->
                            <%if ("DoMT".equalsIgnoreCase(current_engine))
                            {
                                mtProfile4val = mtProfile;%>
                                <div id="doMtDiv" style="display: block;">
                            <%} else {
                                mtProfile4val = new MachineTranslationProfile();%>
                                <div id="doMtDiv" style="display: none;">
                            <%}%>
                                    <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="90%">
                                        <tr>
                                            <td colspan="3"><b>Settings for DoMT machine translation engine. For example: http://host_name:port/RPC2</b></td>
                                        </tr>
                                        <tr>
                                            <td ALIGN="LEFT">DoMT URL<font color="red">*</font>:</td>
                                            <td><INPUT CLASS="standardText" ID="idDoMtUrl"
                                                NAME="<%=MTProfileConstants.MT_DOMT_URL%>"
                                                value="<%=mtProfile4val.getUrl()%>" TYPE="text"
                                                MAXLENGTH="99" SIZE="90" /></td>
                                        </tr>
                                        <tr>
                                            <td ALIGN="LEFT">DoMT Engine Name<font color="red">*</font>:</td>
                                            <td><INPUT CLASS="standardText" ID="idDoMtEngineName"
                                                NAME="<%=MTProfileConstants.MT_DOMT_ENGINE_NAME%>"
                                                value="<%=mtProfile4val.getCategory()%>" TYPE="text"
                                                MAXLENGTH="40" SIZE="50" /></td>
                                        </tr>
                                    </TABLE>
                                </div>
                        <!-- **************** DoMT MT Options : End ******************** -->
                        
                        <!-- **************** Google MT Options : Start ****************** -->
                        	 <%if ("Google_Translate".equalsIgnoreCase(current_engine))
                        	  {
                                 mtProfile4val = mtProfile;%>
                                 <div id="googleDiv" style="display: block;">
                             <%} else {
                                 mtProfile4val = new MachineTranslationProfile();%>
                                 <div id="googleDiv" style="display: none;">
                             <%}%>
                             	
                             	 <TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText" WIDTH="90%">
                             	 	 <tr>
                                           <td colspan="3"><b>Settings for Google machine translation engine. Google uses fixed URL: https://www.googleapis.com</b></td>
                                        </tr>
                             	 	<tr>
                             	 		 <td ALIGN="LEFT">Google API Key<font color="red">*</font>:</td>
                             	 		 <td><INPUT CLASS="standardText" ID="idAPIKey"
                                                NAME="<%=MTProfileConstants.MT_GOOGLE_API_KEY%>"
                                                value="<%=mtProfile4val.getAccountinfo()%>" TYPE="text"
                                                MAXLENGTH="80" SIZE="80" /></td>
                             	 	</tr>
                             	</TABLE>
                             </div>
                        <!-- **************** Google MT Options : End ******************** -->

        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
            ID="Cancel" onclick="submitForm('cancelMTOptions');"/>
        <INPUT TYPE="BUTTON" VALUE="<%=("promt".equalsIgnoreCase(current_engine) || "asia_online"
                    .equalsIgnoreCase(current_engine)) ? lbNext : lbSave%>"
            ID="OK" onclick="submitForm('saveMTOptions');"/>
        <input type='BUTTON' value='Edit Base Info' id='edit' style="display: none"/>

		</FORM>

	</DIV>

</BODY>
</HTML>
