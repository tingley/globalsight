<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
                 com.globalsight.everest.company.Company,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
	String lbnext = bundle.getString("lb_next");
    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL += "&action=" + CompanyConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_company");
    }
    else
    {
        saveURL += "&action=" + CompanyConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_company");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + CompanyConstants.CANCEL;

    // Data
    ArrayList names = (ArrayList)request.getAttribute(CompanyConstants.NAMES);
    Company company = (Company)sessionMgr.getAttribute(CompanyConstants.COMPANY);
    String companyName = "";
    String email = (String)request.getAttribute(CompanyConstants.EMAIL);
    String desc = "";
    String checked = "checked";//default
    String tmAccessControl = "";//default
    String tbAccessControl = "";//default
    String ssoChecked = "";//default
    String isSsoChecked = "false";//default
    String ssoIdpUrl = "";
    String sessionTime = "";
    boolean isReviewOnly = false;
    String useSeparateTablesPerJobChecked = "";
    String qaChecks = "";
    String enableDitaChecksChecked = "";
    String enableWorkflowStatePosts = "";
    
    String inCtxRvKeyIndd = (String) request.getAttribute("incontext_review_key_indd");
    String inCtxRvKeyOffice = (String) request.getAttribute("incontext_review_key_office");
    String inCtxRvKeyXML = (String) request.getAttribute("incontext_review_key_xml");
    String enableInCtxRvToolIndd = "true".equals(inCtxRvKeyIndd) ? "checked" : "";
    String enableInCtxRvToolOffice = "true".equals(inCtxRvKeyOffice) ? "checked" : "";
    String enableInCtxRvToolXML = "true".equals(inCtxRvKeyXML) ? "checked" : "";
    
    boolean isInDesignEnabled = PreviewPDFHelper.isInDesignEnabled();
    boolean isOfficeEnabled = PreviewPDFHelper.isOfficeEnabled();
    boolean isXMLEnabled = PreviewPDFHelper.isXMLEnabled();
    boolean showInContextReivew = (isInDesignEnabled ||  isOfficeEnabled || isXMLEnabled);
    
    if (company != null)
    {
        companyName = company.getName();
        desc = company.getDescription();
        email = company.getEmail();
        sessionTime = company.getSessionTime();
        
        if (desc == null) desc = "";
        if (email == null) email = "";
        if (sessionTime==null) sessionTime="";
        
        boolean enableIPFilte = company.getEnableIPFilter();
        if (enableIPFilte==false) {
        	checked = "";
        }
        
        boolean enableTMAcessControl = company.getEnableTMAccessControl();
        if (enableTMAcessControl) {
            tmAccessControl = "checked";
        }
        
        boolean enableTBAcessControl = company.getEnableTBAccessControl();
        if (enableTBAcessControl) {
            tbAccessControl = "checked";
        }
        
        boolean enableQAChecks = company.getEnableQAChecks();
        if (enableQAChecks) {
            qaChecks = "checked";
        }
          
        if (company.getEnableSSOLogin())
        {
            ssoChecked = "checked";
            isSsoChecked = "true";
        }
        
        ssoIdpUrl = company.getSsoIdpUrl();
        ssoIdpUrl = ssoIdpUrl == null ? "" : ssoIdpUrl;

        if (company.getBigDataStoreLevel() == 2) {
            useSeparateTablesPerJobChecked = "checked";
        }

        if (company.getEnableDitaChecks()) {
            enableDitaChecksChecked = "checked";
        }
        
        if(company.getEnableWorkflowStatePosts()){
            enableWorkflowStatePosts = "checked";
        }
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_company")%>";
var guideNode="companies";
var helpFile = "<%=bundle.getString("help_companies_basic_screen")%>";
function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        companyForm.action = "<%=cancelURL%>";
        companyForm.submit();
    }
    else if (formAction == "save")
    {
    	if (confirmForm() && confirmTime())
		{
    		var tbox = document.getElementById("to");
    		var stbox = document.getElementById("scorecardTo");
    		var qtbox = document.getElementById("qualityTo");
    		var mtbox = document.getElementById("marketTo");
    		var fbox = document.getElementById("from");
    		var sfbox = document.getElementById("scorecardFrom");
    		var qfbox = document.getElementById("qualityFrom");
    		var mfbox = document.getElementById("marketFrom");
    		if (tbox.options.length == 0 
    	    		|| stbox.options.length == 0 || qtbox.options.length == 0 || mtbox.options.length == 0)
    		{
    			alert("<c:out value='${alert}'/>");
    			return false;
    		}
    		for(var i=0;i<tbox.options.length;i++)
    		{
    			tbox.options[i].selected=true;
    		}
    		for(var i=0;i<stbox.options.length;i++)
    		{
    			stbox.options[i].selected=true;
    		}
    		for(var i=0;i<qtbox.options.length;i++)
    		{
    			qtbox.options[i].selected=true;
    		}
    		for(var i=0;i<mtbox.options.length;i++)
    		{
    			mtbox.options[i].selected=true;
    		}
    		for(var i=0;i<fbox.options.length;i++)
    		{
    			fbox.options[i].selected=true;
    		}
    		for(var i=0;i<sfbox.options.length;i++)
    		{
    			sfbox.options[i].selected=true;
    		}
    		for(var i=0;i<qfbox.options.length;i++)
    		{
    			qfbox.options[i].selected=true;
    		}
    		for(var i=0;i<mfbox.options.length;i++)
    		{
    			mfbox.options[i].selected=true;
    		}
            
        	companyForm.action = "<%=saveURL%>";
            companyForm.submit();
		}
    }
}

//
// Check required fields(SSO, email, name).
// Check duplicate activity name.
//
function confirmForm()
{
	// check sso
	var ssoLogonElem = companyForm.enableSsoLogonField;
	if(ssoLogonElem!=null && ssoLogonElem.checked)
    {
        var idpUrl = companyForm.ssoIdpUrlField.value;
        if (isEmptyString(idpUrl))
        {
        	alert("<%=bundle.getString("msg_sso_input_valid_idpurl")%>");
            return false;
        }
    }
	
	// Check Email Field
	var emailElem = document.getElementById("emailId");
	var sysNotificationEnable = "<%=request.getAttribute(SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED)%>";
    if("true" == sysNotificationEnable)
    {
    	var email = stripBlanks(emailElem.value);
    	if(email.length > 0 && !validEmail(email))
    	{
    		alert("<%=bundle.getString("jsmsg_email_invalid")%>");
            return false;
    	}
    }
	
	// check name
    if (!companyForm.nameField) 
    {
        // can't change name on edit
        return true;
    }
    if (isEmptyString(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_company_name"))%>");
        companyForm.nameField.value = "";
        companyForm.nameField.focus();
        return false;
    }   
    
    //Check if the company name is one of key words
    var companyName = ATrim(companyForm.nameField.value).toLowerCase();
	var words = new Array("com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "con", "prn", "aux", "nul", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9");
	var tmp = "", tmpPrefix = "";
	for (x in words) {
	  tmp = words[x];
	  tmpPrefix = tmp + ".";
	  if (companyName == tmp || companyName.indexOf(tmpPrefix) == 0) {
		alert("<%=EditUtil.toJavascript(bundle.getString("msg_invalid_company_name"))%>");
		return false;
	  }
	}
    
    if (hasSpecialChars(companyForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }
    // check for dups 
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String comName = (String)names.get(i);
%>
            if ("<%=comName%>".toLowerCase() == companyForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company"))%>");
                return false;
            }
<%
        }
    }
%>

	return true;
}

function confirmTime()
{
	var sessionTime = companyForm.sessionTimeField.value;
	{
		if (sessionTime!='')
		{
			if(isNumeric(sessionTime))
			{
				sessionTime = parseInt(sessionTime)
				if (sessionTime > 480 || sessionTime < 30)
				{
					alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
					return false;
				}
			}
			else
			{
				alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_company_time"))%>");
				return false;
			}
		}
	}
    return true;
}

function isNumeric(str){
	if (str.startsWith("0"))
		return false;
	return /^(-|\+)?\d+(\.\d+)?$/.test(str);
}

function onEnableSSOSwitch()
{
	onEnableSSO(companyForm.enableSsoLogonField.checked);
}

function onEnableSSO(checked)
{
	var ele = document.getElementById("ssoIdpUrlCC");
	var display = checked ? "" : "none";
	ele.style.display = display;
}

function doOnload()
{
    loadGuides();

    var edit = eval("<%=edit%>");
    if (edit)
    {
        companyForm.<%=CompanyConstants.DESC%>.focus();
    }
    else
    {
        companyForm.<%=CompanyConstants.NAME%>.focus();
    }

    var enableSSO = <%=request.getAttribute(SystemConfigParamNames.ENABLE_SSO)%>;
    if(!enableSSO)
    {
		document.getElementById("ssoCheck").style.display = "none";
		document.getElementById("ssoIdpUrlCC").style.display = "none";
    }
    else
    {
    	onEnableSSO(eval("<%=isSsoChecked%>"));
    }
}

function move(f,t) {
	var fbox = document.getElementById(f);
	var tbox = document.getElementById(t);
	for(var i=0; i<fbox.options.length; i++) {
		if(fbox.options[i].selected && fbox.options[i].value != "") {
			var no = new Option();
			no.value = fbox.options[i].value;
			no.text = fbox.options[i].text;
			no.title = fbox.options[i].title;
			tbox.options[tbox.options.length] = no;
			fbox.options[i].value = "";
			fbox.options[i].text = "";
			fbox.options[i].title = "";
   		}
	}
	BumpUp(fbox);
	SortD(tbox);
}

function BumpUp(box)  {
	for(var i=0; i<box.options.length; i++) {
		if(box.options[i].value == "")  {
			for(var j=i; j<box.options.length-1; j++)  {
				box.options[j].value = box.options[j+1].value;
				box.options[j].text = box.options[j+1].text;
				box.options[j].title = box.options[j+1].title;
			}
			var ln = i;
			break;
		}
	}
	if(ln < box.options.length)  {
		box.options.length -= 1;
		BumpUp(box);
   	}
}

function SortD(box){
	var temp_opts = new Array();
	var temp = new Object();
	for(var i=0; i<box.options.length; i++){
		temp_opts[i] = box.options[i];
	}

	for(var x=0; x<temp_opts.length-1; x++){
		for(var y=(x+1); y<temp_opts.length; y++){
			if(temp_opts[x].text.toLowerCase() > temp_opts[y].text.toLowerCase()){
				temp = temp_opts[x].text;
				temp_opts[x].text = temp_opts[y].text;
	      		temp_opts[y].text = temp;
	      		
	      		temp = temp_opts[x].value;
	      		temp_opts[x].value = temp_opts[y].value;
	      		temp_opts[y].value = temp;

	      		temp = temp_opts[x].title;
	      		temp_opts[x].title = temp_opts[y].title;
	      		temp_opts[y].title = temp;
	      	}
	   	}
	}

	for(var j=0; j<box.options.length; j++){
		box.options[j].value = temp_opts[j].value;
		box.options[j].text = temp_opts[j].text;
		box.options[j].title = temp_opts[j].title;
	}
}

function isLetterAndNumber(str){
	var reg = new RegExp("^[A-Za-z0-9 _,.-]+$");
	return (reg.test(str));
}

function isChinese(str){
	return str.match(/[\u4e00-\u9fa5]/g);
}

function addTo()
{
	var txt = document.getElementById("newCategory").value;
	if(txt.indexOf(",")>0)
	{
		alert("<%=bundle.getString("msg_company_category_invalid") %>");
		return;
	}
	if(Trim(txt) != "")
	{
		txt = Trim(txt);
		if (!isLetterAndNumber(txt) && !isChinese(txt))
		{
			alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
			return false;
		}

		if(checkForCommentCategory(txt))
		{
			alert("You cannot add " + txt + " manually.");
			return false;
		}
		
		var toBox = document.getElementById("to");
		var fromBox = document.getElementById("from");
		for (var i=0;i<toBox.options.length;i++)
		{
			if(toBox.options[i].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		for (var j=0;j<fromBox.options.length;j++)
		{
			if(fromBox.options[j].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		var op = new Option();
		op.value = txt;
		op.text = txt;
		op.title = txt;
		toBox.options[toBox.options.length] = op;
		document.getElementById("newCategory").value = "";

		SortD(toBox);
	}
}

function checkForCommentCategory(txt)
{
	if(txt == "lb_conflicts_glossary_guide" || txt == "lb_formatting_error" 
		||	txt ==  "lb_mistranslated" || txt ==  "lb_omission_of_text" 
		|| txt == "lb_spelling_grammar_punctuation_error")
	{
		return true;
	}

	return false;
}

function addScorecardTo()
{
	var txt = document.getElementById("newScorecardCategory").value;
	if(txt.indexOf(",")>0)
	{
		alert("<%=bundle.getString("msg_company_category_invalid") %>");
		return;
	}

	if(checkForScorecardCategory(txt))
	{
		alert("You cannot add " + txt + "manually.");
		return false;
	}
	
	if(Trim(txt) != "")
	{
		txt = Trim(txt);
		if (!isLetterAndNumber(txt) && !isChinese(txt))
		{
			alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
			return false;
		}
		
		var toBox = document.getElementById("scorecardTo");
		var fromBox = document.getElementById("scorecardFrom");
		for (var i=0;i<toBox.options.length;i++)
		{
			if(toBox.options[i].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		for (var j=0;j<fromBox.options.length;j++)
		{
			if(fromBox.options[j].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		var op = new Option();
		op.value = txt;
		op.text = txt;
		op.title = txt;
		toBox.options[toBox.options.length] = op;
		document.getElementById("newScorecardCategory").value = "";

		SortD(toBox);
	}
}

function checkForScorecardCategory(txt)
{
	if(txt == "lb_spelling_grammar" || txt == "lb_consistency" 
		||	txt ==  "lb_style" || txt ==  "lb_terminology")
	{
		return true;
	}

	return false;
}

function Trim(str)
{
	if(str=="") return str;
	var newStr = ""+str;
	RegularExp = /^\s+|\s+$/gi;
	return newStr.replace( RegularExp,"" );
}

function addQualityTo()
{
	var txt = document.getElementById("newQualityCategory").value;
	if(txt.indexOf(",")>0)
	{
		alert("<%=bundle.getString("msg_company_category_invalid") %>");
		return;
	}

	if(checkForQualityCategory(txt))
	{
		alert("You cannot add " + txt + "manually.");
		return false;
	}
	
	if(Trim(txt) != "")
	{
		txt = Trim(txt);
		if (!isLetterAndNumber(txt) && !isChinese(txt))
		{
			alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
			return false;
		}
		
		var toBox = document.getElementById("qualityTo");
		var fromBox = document.getElementById("qualityFrom");
		for (var i=0;i<toBox.options.length;i++)
		{
			if(toBox.options[i].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		for (var j=0;j<fromBox.options.length;j++)
		{
			if(fromBox.options[j].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		var op = new Option();
		op.value = txt;
		op.text = txt;
		op.title = txt;
		toBox.options[toBox.options.length] = op;
		document.getElementById("newQualityCategory").value = "";

		SortD(toBox);
	}
	}
	
	function checkForQualityCategory(txt)
	{
		if(txt == "lb_good" || txt == "lb_acceptable" 
			||	txt ==  "lb_poor")
		{
			return true;
		}

		return false;
	}
	
	function addMarketTo()
	{
		var txt = document.getElementById("newMarketCategory").value;
		if(txt.indexOf(",")>0)
		{
			alert("<%=bundle.getString("msg_company_category_invalid") %>");
			return;
		}

		if(checkForMarketCategory(txt))
		{
			alert("You cannot add " + txt + "manually.");
			return false;
		}
		
		if(Trim(txt) != "")
		{
			txt = Trim(txt);
			if (!isLetterAndNumber(txt) && !isChinese(txt))
			{
				alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
				return false;
			}
			
			var toBox = document.getElementById("marketTo");
			var fromBox = document.getElementById("marketFrom");
			for (var i=0;i<toBox.options.length;i++)
			{
				if(toBox.options[i].text.toLowerCase()==txt.toLowerCase())
				{
					alert("<c:out value='${alert_same}'/>");
					return false;
				}
			}
			for (var j=0;j<fromBox.options.length;j++)
			{
				if(fromBox.options[j].text.toLowerCase()==txt.toLowerCase())
				{
					alert("<c:out value='${alert_same}'/>");
					return false;
				}
			}
			var op = new Option();
			op.value = txt;
			op.text = txt;
			op.title = txt;
			toBox.options[toBox.options.length] = op;
			document.getElementById("newMarketCategory").value = "";

			SortD(toBox);
		}}
		
		function checkForMarketCategory(txt)
		{
			if(txt == "lb_suitable_fluent" || txt == "lb_literal_at_times" 
				||	txt ==  "lb_unsuitable")
			{
				return true;
			}

			return false;
		} 
</script>

</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="companyForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
            <td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
            <td>
                <% if (edit) { %>
                    <%=companyName%>
                <% } else { %>
                    <input type="text" name="<%=CompanyConstants.NAME%>" maxlength="40" size="30" value="<%=companyName%>">
                <% } %>
            </td>
            <td valign="center">
                <% if (!edit) { %>
                    <%=bundle.getString("lb_valid_name")%>
                <% } %>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_description")%>:</td>
            <td colspan="2">
                <textarea rows="6" cols="40" style="width:350px;" name="<%=CompanyConstants.DESC%>"><%=desc%></textarea>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_email")%>:</td>
            <td colspan="2">
                <input type="text" style="width:350px;" name="<%=CompanyConstants.EMAIL%>" id="emailId" value="<%=email%>">
            </td>
        </tr>
        
        <tr>
        	<td valign="top"><%=bundle.getString("lb_session_timeout")%>&nbsp;(<%=bundle.getString("lb_minutes")%>):</td>
        	<td colspan="2">
                <input type="text" name="<%=CompanyConstants.SESSIONTIME%>" maxlength="3" size="20" value="<%=sessionTime%>">&nbsp;(30-480)
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableIPFilter")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableIPFilterId" name="<%=CompanyConstants.ENABLE_IP_FILTER%>" <%=checked%>/>
            </td>
        </tr>
        
        <tr id="ssoCheck">
            <td valign="top"><%=bundle.getString("lb_sso_enableSSO")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableSsoLogonId" onclick="onEnableSSOSwitch()" name="<%=CompanyConstants.ENABLE_SSO_LOGON%>" <%=ssoChecked%>/>
            </td>
        </tr>
        <tr id="ssoIdpUrlCC">
            <td valign="top"><%=bundle.getString("lb_sso_IdpUrl")%>:</td>
            <td colspan="2">
                <input type="text" style="width:350px;" name="<%=CompanyConstants.SSO_IDP_URL%>" maxlength="256" value="<%=ssoIdpUrl%>">
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTMAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTMAccessControl" name="<%=CompanyConstants.ENABLE_TM_ACCESS_CONTROL%>" <%=tmAccessControl%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_enableTBAccessControl")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableTBAccessControl" name="<%=CompanyConstants.ENABLE_TB_ACCESS_CONTROL%>" <%=tbAccessControl%>/>
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_use_separate_tables_per_job")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" name="<%=CompanyConstants.BIG_DATA_STORE_LEVEL%>" <%=useSeparateTablesPerJobChecked%>/>
            </td>
        </tr>
        
        <tr>
            <td valign="top"><%=bundle.getString("lb_company_enable_qachecks")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableQAChecks" name="<%=CompanyConstants.ENABLE_QA_CHECKS%>" <%=qaChecks%>/>
            </td>
        </tr>

        <tr>
            <td valign="top"><%=bundle.getString("lb_enable_dita_checks")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" name="<%=CompanyConstants.ENABLE_DITA_CHECKS%>" <%=enableDitaChecksChecked%>/>
            </td>
        </tr>
        
        <tr>
        <td valign="top"><%=bundle.getString("lb_enable_workflow_state_posts") %>:</td>
        <td>
        	<input class="standardText" type="checkbox" name="<%=CompanyConstants.ENABLE_WORKFLOW_STATE_POSTS%>" <%=enableWorkflowStatePosts%>/>
        </td>
        </tr>
        
        <tr id="inctxrvCheck" <% if (!showInContextReivew) {%>style="display:none;" <%}%> >
            <td valign="top"><%=bundle.getString("lb_incontext_review")%>:</td>
            <td colspan="2">
            </td>
        </tr>
        
        <tr id="inctxrvCheckIndd" <% if (!isInDesignEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_indesign")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolInddId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_INDD%>" <%=enableInCtxRvToolIndd%>/>
            </td>
        </tr>
        
        <tr id="inctxrvCheckOffice" <% if (!isOfficeEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_office2010")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolOfficeId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_OFFICE%>" <%=enableInCtxRvToolOffice%>/>
            </td>
        </tr>
        
        <tr id="inctxrvCheckXML" <% if (!isXMLEnabled) {%>style="display:none;" <%}%> >
            <td valign="top">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_xml")%>:</td>
            <td colspan="2">
                <input class="standardText" type="checkbox" id="enableInCtxRvToolXMLId" name="<%=CompanyConstants.ENABLE_INCTXRV_TOOL_XML%>" <%=enableInCtxRvToolXML%>/>
            </td>
        </tr>

        <tr valign="top">
    		<td colspan=3>
    			<br/><div class="standardText"><c:out value="${helpMsg}"/>:</div>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="from" name="from" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${fromList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('from','to')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('to','from')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="to" name="to" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${toList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="newCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addTo()">
        			</td>
        		</tr>
      			</table>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
    			<br/><div class="standardText"><c:out value="${scorecardHelpMsg}"/>:</div>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="scorecardFrom" name="scorecardFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${scorecardFromList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('scorecardFrom','scorecardTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('scorecardTo','scorecardFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="scorecardTo" name="scorecardTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${scorecardToList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="newScorecardCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addScorecardTo()">
        			</td>
        		</tr>
      			</table>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
    			<br/><div class="standardText"><c:out value="${qualityHelpMsg}"/>:</div>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="qualityFrom" name="qualityFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${qualityFromList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('qualityFrom','qualityTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('qualityTo','qualityFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="qualityTo" name="qualityTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${qualityToList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="newQualityCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addQualityTo()">
        			</td>
        		</tr>
      			</table>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
    			<br/><div class="standardText"><c:out value="${marketHelpMsg}"/>:</div>
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><c:out value="${labelForLeftTable}"/>
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><c:out value="${labelForRightTable}"/>
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="marketFrom" name="marketFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${marketFromList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('marketFrom','marketTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('marketTo','marketFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="marketTo" name="marketTo" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${marketToList}">
	      					<option title="${op.value}" value="${op.key}">${op.value}</option>
	    				</c:forEach>
        				</select>
        			</td>
        		</tr>
				</table>
				<table border="0" class="standardText" cellpadding="2">
        		<tr>
        			<td>
	        			<span><c:out value="${label}"/></span> :
        			</td>
        			<td>
        				<input id="newMarketCategory" size="40" maxlength="100">
        				<input style="display:none">
        			</td>
        			<td>
        				<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addMarketTo()">
        			</td>
        		</tr>
      			</table>
    		</td>
  		</tr>
        
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr>
            <td colspan="3">
                <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
                <input type="button" name="<%=lbsave%>" value="<%=lbsave%>" onclick="submitForm('save')">
            </td>
        </tr>

      </table>
    </td>
  </tr>
  
</table>
</form>
</div>
</body>
</html>
