<%-- @author Fan --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
	 com.globalsight.everest.foundation.BasicL10nProfile,
	 com.globalsight.everest.webapp.pagehandler.PageHandler,
	 com.globalsight.everest.servlet.util.SessionManager,
	 com.globalsight.everest.webapp.WebAppConstants,
     com.globalsight.everest.projecthandler.ProjectInfo,
     com.globalsight.everest.projecthandler.TranslationMemoryProfile,
	 com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants,
	 com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper,
	 com.globalsight.everest.util.comparator.TMProfileComparator,
	 com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
     com.globalsight.util.FormUtil,
	 com.globalsight.util.GlobalSightLocale,
	 com.globalsight.util.resourcebundle.ResourceBundleConstants,
	 com.globalsight.util.resourcebundle.SystemResourceBundle,
     com.globalsight.everest.util.system.SystemConfigParamNames,
     com.globalsight.everest.util.system.SystemConfiguration,
	 java.lang.Integer, java.util.Locale,
     com.globalsight.util.GeneralException,
	 java.util.ResourceBundle,
     java.util.Collections,
     com.globalsight.util.collections.HashtableValueOrderWalker,
     com.globalsight.everest.company.CompanyWrapper,
     com.globalsight.everest.company.Company,
     com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
     com.globalsight.everest.permission.Permission,
     com.globalsight.everest.permission.PermissionSet"
	 session="true" %>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	PermissionSet userPerms1 = (PermissionSet) session.getAttribute( 
        WebAppConstants.PERMISSIONS);
    boolean isSuperAdmin = UserUtil.isSuperAdmin(
        (String) request.getSession().getAttribute(WebAppConstants.USER_NAME));
	boolean isAdmin = UserUtil.isInPermissionGroup((String) request.getSession().getAttribute(WebAppConstants.USER_NAME), "Administrator");
    boolean isSuperProjectManager = UserUtil
            .isSuperPM((String) request.getSession().getAttribute(
                    WebAppConstants.USER_NAME));
    boolean isProjectManager = userPerms1.getPermissionFor(Permission.PROJECTS_MANAGE);
    boolean isEnableWorkflowStatePosts = false;
    Company company = CompanyWrapper.getCurrentCompany();
    isEnableWorkflowStatePosts = company.getEnableWorkflowStatePosts();
   
    String title, helperText;
    if (request.getAttribute("edit") != null)
    {
        title = bundle.getString("lb_edit") + " "
                + bundle.getString("lb_loc_profile");
        helperText = bundle.getString("helper_text_loc_profile_edit")
                + " " + bundle.getString("helper_text_refer_to_help");
    }
    else
    {
        title = bundle.getString("lb_new") + " "
                + bundle.getString("lb_loc_profile");
        helperText = bundle.getString("helper_text_loc_profile_enter")
                + " " + bundle.getString("helper_text_refer_to_help");
    }
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8">
<title><%=title%></title>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/radioButtons.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.js"></script>
<script type="text/javascript">
//variable:all page needed.
var needWarning = true;
var guideNode = "locProfiles";
var objectName = "<%= bundle.getString("lb_loc_profile") %>";
var helpFile = "<%=bundle.getString("help_localization_profiles_basic_info")%>";

var targetLocalesWorkflowsHtml = "";
var optionNone = "None";
var saveURL = "${save.pageURL}" + "&action=save";
var cancelURL = "${cancel.pageURL}" + "&action=cancel";

function sendAjaxForTargetLocales(){
	$("#Cancel").attr("disabled","disabled");
	$("#Save").attr("disabled","disabled");
	
	targetLocalesWorkflowsHtml = "";
	if ($("#LocProfileProjectId").get(0).selectedIndex == 0 || $("#SourceLocaleId").get(0).selectedIndex == 0)
	{
		$("#TargetLocalsWorkflows").html("");
		$("#Cancel").removeAttr("disabled");
		$("#Save").removeAttr("disabled");
		return;
	}
	var locProfileProjectId = $("#LocProfileProjectId").val();
	var sourceLocaleId = $("#SourceLocaleId").val();
	var url = "/globalsight/ControlServlet?linkName=new1&pageName=LP1&action=ajax&locProfileProjectId="+locProfileProjectId+"&sourceLocaleId="+sourceLocaleId+"&t="+new Date().getTime();
	$("#TargetLocals").show();
	$.getJSON(url, 
		function(data) {
			generateTargetLocalesWorkflowsHtml(data.targetLocalesWorkflows);
			$("#TargetLocalsWorkflows").html(targetLocalesWorkflowsHtml);
			
			<c:if test="${edit}">
			<c:forEach items="${WorkflowTemplateIdArr}" var="item" varStatus="status">
			$("#TargetLocalsWorkflows .first option").each(function(){
				if('${item}'==this.value){
					$(this).attr("selected","selected");
					var sib=$(this).parent().siblings(".second");
					if(this.value!=-1){
						sib.val('${mtIdArr[status.index]}');
						sib.attr("disabled",false)
					}					
				}
			})
			</c:forEach>
			</c:if>
			
			 $("#TargetLocalsWorkflows  select[class='first']").each(function(){
				 var sib=$(this).siblings();
				 if($(this).val()!=-1){
					 sib.attr("disabled",false)
					}
					$(this).change(function(){
						if($(this).val()!=-1){
							sib.attr("disabled",false)
						}else{
							sib.val("-1");
							sib.attr("disabled",true)
						}
					})
			 })
		}
	);
	$("#Cancel").removeAttr("disabled");
	$("#Save").removeAttr("disabled");
}

function generateTargetLocalesWorkflowsHtml(targetLocalesArr) {
	for(var i=0;i<targetLocalesArr.length;i++){
		targetLocalesWorkflowsHtml += "<div><label for=";
		targetLocalesWorkflowsHtml += targetLocalesArr[i].targetLocaleId;
		targetLocalesWorkflowsHtml += ">";
		targetLocalesWorkflowsHtml += targetLocalesArr[i].targetLocaleDisplayName;
		targetLocalesWorkflowsHtml += "</label>";
		targetLocalesWorkflowsHtml += "<select class='first' id=";
		targetLocalesWorkflowsHtml += targetLocalesArr[i].targetLocaleId;
		targetLocalesWorkflowsHtml += " name=";
		targetLocalesWorkflowsHtml += "TargetLocaleId_";
		targetLocalesWorkflowsHtml += targetLocalesArr[i].targetLocaleId;
		targetLocalesWorkflowsHtml += " style='width:24.6em'";
		targetLocalesWorkflowsHtml += ">";
		targetLocalesWorkflowsHtml += generateTargetLocaleWorkflowsOption(targetLocalesArr[i].targetLocaleWorkflows);
		targetLocalesWorkflowsHtml += "</select>";
		targetLocalesWorkflowsHtml += "<select disabled='true' class='second' id=";
		targetLocalesWorkflowsHtml += targetLocalesArr[i].targetLocaleId;
		targetLocalesWorkflowsHtml += " name=";
		targetLocalesWorkflowsHtml += "TargetLocaleId_";
		targetLocalesWorkflowsHtml += targetLocalesArr[i].targetLocaleId;
		targetLocalesWorkflowsHtml += " style='width:24.6em'";
		targetLocalesWorkflowsHtml += ">";
		targetLocalesWorkflowsHtml += generateMtOption(targetLocalesArr[i].mtProfiles);
		targetLocalesWorkflowsHtml += "</select></div>";
		
	}
}

function generateMtOption(targetLocaleWorkflowsArr){
	var optionHtml = "<option value='-1'>";
	optionHtml += optionNone;
	optionHtml += "</option>";
	for(var i=0;i<targetLocaleWorkflowsArr.length;i++){
		optionHtml += "<option value=";
		optionHtml += targetLocaleWorkflowsArr[i].mtId;
		optionHtml += ">";
		optionHtml += targetLocaleWorkflowsArr[i].mtName;
		optionHtml += "</option>";
	}
	return optionHtml;
}

function generateTargetLocaleWorkflowsOption(targetLocaleWorkflowsArr){
	var optionHtml = "<option value='-1'>";
	optionHtml += optionNone;
	optionHtml += "</option>";
	for(var i=0;i<targetLocaleWorkflowsArr.length;i++){
		optionHtml += "<option value=";
		optionHtml += targetLocaleWorkflowsArr[i].workflowId;
		optionHtml += ">";
		optionHtml += targetLocaleWorkflowsArr[i].workflowName;
		optionHtml += "</option>";
	}
	return optionHtml;
}

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
		$("#LocalizationPccrofiles").attr("action",cancelURL);
        $("#LocalizationPccrofiles").submit();
    }
    else if (formAction == "save")
    {
        if (confirmForm())
        {
        	$("#LocProfileProjectId").removeAttr("disabled");
        	$("#SourceLocaleId").removeAttr("disabled");
			$("#LocalizationPccrofiles").attr("action",saveURL);
			$("#LocalizationPccrofiles").submit();
        }
    }
}

function confirmForm()
{
	LocalizationPccrofiles.LocProfileName.value = ATrim(LocalizationPccrofiles.LocProfileName.value);
    if (isEmptyString($("#LocProfileName").val())) {
        alert("<%= bundle.getString("jsmsg_loc_profiles_name") %>");
        $("#LocProfileName").focus();
        return false;
    }
    if (hasSpecialChars($("#LocProfileName").val()))
    {
        alert("<%= bundle.getString("lb_name")%>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
	<c:forEach items="${names}" var="item">
		if("false"=="${LocProfileName==item}"){//edit but don't change name
			if($("#LocProfileName").get(0).value.toLowerCase() == "${item}".toLowerCase()){
	            alert('<%= bundle.getString("jsmsg_duplicate_loc_profile")%>');
	            return false;
			}
		}
	</c:forEach>
    if (!isNotLongerThan($("#LocProfileDescription").val(), 256)) {
        alert("<%= bundle.getString("jsmsg_description") %>");
        $("#LocProfileDescription").focus();
        return false;
    }
    if ($("#locTMProfileId").val() == -1) 
    {
        alert("<%= bundle.getString("jsmsg_loc_tm_profiles") %>");
        return false;
    }
    if ($("#LocProfileProjectId").val() == -1) 
    {
        alert("<%= bundle.getString("jsmsg_loc_profiles_project") %>");
        return false;
    }
    if ($("#SourceLocaleId").val() == -1) 
    {
        alert("<%= bundle.getString("jsmsg_loc_profiles_source_locale") %>");
        return false;
    }
    var noneWorkflows = true;
    $("#TargetLocalsWorkflows  select").each(function(){if(this.value!=-1) noneWorkflows=false;});
    if (noneWorkflows) {
    	alert("<%= bundle.getString("jsmsg_attach_workflow") %>");
    	return false;
    }
    return true;
}

<c:if test="${edit}">
$(document).ready(function editPage(){
	sendAjaxForTargetLocales();
});
</c:if>
</script>
<style>
#MainHelperText{
	margin-bottom: 1.5em;
}

#Main label{
  	display:inline-block;
	width: 15em;
}

#Main input,textarea,select{
	margin:3px;
	padding:2px;
}

#TargetLocals{
	display:none;
}

#MainTitle,#TargetLocalsTitle,#FormButton{
	margin-top:1em;
	margin-bottom:1em;
}

#TargetLocalsWorkflows label{
  	display:inline-block;
	width: 15em;
}

</style>
</head>
<body onload="loadGuides()">
	<%@ include file="/envoy/common/header.jspIncl" %>
	<%@ include file="/envoy/common/navigation.jspIncl" %>
	<%@ include file="/envoy/wizards/guides.jspIncl" %>
	<div id="contentLayer" style="position:ABSOLUTE;z-index:9;top:108px;left:20px;right:20px;"><!-- move down for page head -->
	<form id="LocalizationPccrofiles" name="LocalizationPccrofiles" method="post" class="standardText">
		<c:if test="${edit}">
			<input type="hidden" name="Edit" value="true"/>
			<input type="hidden" name="EditLocProfileId" value="${radioBtn}"/>
		</c:if>
		<div id="Main">
			<div id="MainTitle" class="mainHeading">
				<%=title%>
			</div>
			<div id="MainHelperText" class="standardText" style="width:500px">
				<%=helperText%>
			</div>
			
			<div>
				<label for="LocProfileName"><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</label>
				<input type="text" id="LocProfileName" name="LocProfileName" maxlength="40" size="30" value="${LocProfileName}"/>
			</div>
			<div>
				<label for="LocProfileDescription" style="vertical-align:top"><%=bundle.getString("lb_description")%>:</label>
				<textarea id="LocProfileDescription" rows="6" cols="40" name="LocProfileDescription">${LocProfileDescription}</textarea>
			</div>
			<div>
				<label for="LocProfileSQLScript"><%=bundle.getString("lb_sql_script_option")%>:</label>
				<input type="text" id="LocProfileSQLScript" name="LocProfileSQLScript" maxlength="40" size="30" value="${LocProfileSQLScript}"/>
			</div>
			<div>
				<label for="locTMProfileId"><%=bundle.getString("lb_tm_profiles")%><span class="asterisk">*</span>:</label>
				<select id="locTMProfileId" name="locTMProfileId">
					<option value="-1"><%=bundle.getString("lb_choose")%></option>
					<c:forEach items="${tmProfiles}" var="item">
						<option value="${item.id}" <c:if test="${locTMProfileId==item.id}">selected="selected"</c:if>>${item.name}</option>
					</c:forEach>
				</select>
			</div>
			<div>
				<label for="LocProfileProjectId"><%=bundle.getString("lb_project")%><span class="asterisk">*</span>:</label>
				<select id="LocProfileProjectId" name="LocProfileProjectId" onchange="sendAjaxForTargetLocales()" <c:if test="${edit}">disabled="disabled"</c:if>>
					<option value="-1"><%=bundle.getString("lb_choose")%></option>
					<c:forEach items="${projects}" var="item">
						<option value="${item.projectId}" <c:if test="${LocProfileProjectId==item.projectId}">selected="selected"</c:if>>${item.name}</option>
					</c:forEach>
				</select>
			</div>
			<div>
				<label for="JobPriority"><%=bundle.getString("lb_priority")%><span class="asterisk">*</span>:</label>
				<select id="JobPriority" name="JobPriority">
					<c:forEach var="item" begin="1" end="${maxPriority}" step="1">
						<option value="${item}" 
						<c:if test="${edit}">
							<c:if test="${item==JobPriority}">selected="selected"</c:if>
						</c:if>
						<c:if test="${!edit}">
							<c:if test="${item==defaultPriority}">selected="selected"</c:if>
						</c:if>>
						${item}
						</option>
					</c:forEach>					
				</select>
			</div>
			<div>
				<label for="SourceLocaleId"><%=bundle.getString("lb_source_locale")%><span class="asterisk">*</span>:</label>
				<select id="SourceLocaleId" name="SourceLocaleId" onchange="sendAjaxForTargetLocales()" <c:if test="${edit}">disabled="disabled"</c:if>>
					<option value="-1"><%=bundle.getString("lb_choose")%></option>
					<c:forEach items="${srcLocales}" var="item">
						<option value="${item.id}" <c:if test="${SourceLocaleId==item.id}">selected="selected"</c:if>>${item.displayName}</option>
					</c:forEach>
				</select>
			</div>
			<div>
				<label for="LocProfileTMUsageId"><%=bundle.getString("lb_use_tm")%><span class="asterisk">*</span>:</label>
				<select id="LocProfileTMUsageId" name="LocProfileTMUsageId">
					<c:set var="ALLOW_EDIT_TM_USAGE" scope="request" value="<%=LocProfileStateConstants.ALLOW_EDIT_TM_USAGE%>"/>
					<c:set var="DENY_EDIT_TM_USAGE" scope="request" value="<%=LocProfileStateConstants.DENY_EDIT_TM_USAGE%>"/>
					<c:set var="NO_TM_USAGE" scope="request" value="<%=LocProfileStateConstants.NO_TM_USAGE%>"/>
					<option value="${ALLOW_EDIT_TM_USAGE}" <c:if test="${LocProfileTMUsageId==ALLOW_EDIT_TM_USAGE}">selected="selected"</c:if>><%=bundle.getString("lb_regular_tm")%></option>
					<option value="${DENY_EDIT_TM_USAGE}" <c:if test="${LocProfileTMUsageId==DENY_EDIT_TM_USAGE}">selected="selected"</c:if>><%=bundle.getString("lb_regular_and_page_tm")%></option>
					<option value="${NO_TM_USAGE}" <c:if test="${LocProfileTMUsageId==NO_TM_USAGE}">selected="selected"</c:if>><%=bundle.getString("lb_no")%></option>
				</select>
			</div>
			<div>
				<label for="AutomaticDispatch"><%=bundle.getString("lb_workflow_dispatch")%><span class="asterisk">*</span>:</label>
				<select id="AutomaticDispatch" name="AutomaticDispatch">
					<option value="true" <c:if test="${AutomaticDispatch==true}">selected="selected"</c:if>><%=bundle.getString("lb_automatic")%></option>
					<option value="false" <c:if test="${AutomaticDispatch==false}">selected="selected"</c:if>><%=bundle.getString("lb_manual")%></option>
				</select>
			</div>
			<%if (isEnableWorkflowStatePosts && (isSuperAdmin || isAdmin  || isSuperProjectManager || isProjectManager)){%>
				<div>
					<label for="wfStatePostProfileId"><%= bundle.getString("lb_workflow_state_post_profile") %></label>
					<select id="wfStatePostProfileId" name="wfStatePostProfileId" >
					<option value="-1"><%=bundle.getString("lb_choose")%></option>
					<c:forEach items="${wfStatePost}" var="item">
						<option value="${item.id}" <c:if test="${wfStatePostProfileId==item.id}">selected="selected"</c:if>>${item.name}</option>
					</c:forEach>
				</select>
				</div>
			<%}%>
			</div>
		
		<div id="TargetLocals">
			<div id="TargetLocalsTitle" class="mainHeading" style="width:1000px">
			<div style="float: left;padding-right: 405px">
				<%=bundle.getString("lb_attach_workflow")%>
				</div>
				<div style="">
				<%=bundle.getString("lb_mt_translation")%>
				</div>
			</div>
			<div id="TargetLocalsWorkflows" style="width:1000px"></div>
		</div>
		<div id='FormButton'>
			<input type="button" id="Cancel" value="Cancel" onclick="submitForm('cancel')"/>
			<input type="button" id="Save" value="Save" onclick="submitForm('save')"/>
		</div>
	</form>
	</div>
</body>
</html>
