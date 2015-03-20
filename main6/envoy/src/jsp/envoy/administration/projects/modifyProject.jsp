<%@page import="java.text.NumberFormat"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
		         com.globalsight.everest.webapp.javabean.NavigationBean,
		         com.globalsight.util.resourcebundle.ResourceBundleConstants,
		         com.globalsight.util.resourcebundle.SystemResourceBundle,
		         com.globalsight.everest.usermgr.UserInfo,
		         com.globalsight.everest.util.comparator.UserInfoComparator,
		         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                  com.globalsight.everest.permission.Permission,
                  com.globalsight.everest.permission.PermissionSet,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.projecthandler.ProjectImpl,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.everest.foundation.User,
                  com.globalsight.util.GeneralException,
                  com.globalsight.cxe.entity.customAttribute.AttributeSet,
                  com.globalsight.everest.company.CompanyWrapper,
                  com.globalsight.everest.company.Company,
                  com.globalsight.cxe.entity.fileprofile.FileProfileImpl,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.List,
                  java.util.ArrayList,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String title= bundle.getString("lb_new_project") + " - " + bundle.getString("lb_basic_information");
    String editTitle= bundle.getString("msg_edit_project") + " - " + bundle.getString("lb_basic_information");
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    String lbnext = bundle.getString("lb_next");
    String lbusers = bundle.getString("lb_users")+"...";
    String warningMessage = bundle.getString("jsmsg_project_pm_update");
    List projectManagers = (List)request.getAttribute("pms");
    Vector qps = (Vector)request.getAttribute("qePersons");
    List termbases = (List)request.getAttribute("termbases");
    List projects = (List)sessionManager.getAttribute("projects");
    String action = request.getParameter("action");
    ProjectImpl project = (ProjectImpl)sessionManager.getAttribute("project");
    List<AttributeSet> allAttributeGroups = (List<AttributeSet>)request.getAttribute("allAttributeGroups");
    AttributeSet attributeSet = null;    
    
    String projectName = "";
    String pmName = (String)request.getAttribute("pm");
    String qpId = "";
    String termbase = "";
    String desc = "";
    String pmId = "";
    boolean edit = false;
    boolean isAdminEditing = false;
    String pmcost = "0";
    Company company = CompanyWrapper.getCurrentCompany();;
	boolean enableQAChecks = false;
    
    PermissionSet perms = (PermissionSet) session.getAttribute(
            WebAppConstants.PERMISSIONS);
    boolean viewAttributeGroup = perms.getPermissionFor(Permission.ATTRIBUTE_GROUP_VIEW);
    String saveUrl = save.getPageURL()+"&action=create";
    String selfUrl = self.getPageURL()+"&action=create&radioBtn="+project.getId();
    if (action.equals("edit"))
    {
        saveUrl = save.getPageURL()+"&action=modify";
        selfUrl = self.getPageURL()+"&action=edit&radioBtn="+project.getId();
        edit = true;
        
        isAdminEditing = perms.getPermissionFor(Permission.PROJECTS_EDIT_PM);
    }
    boolean isEnableDitaChecks = false;

    String checkOrNot = "checked";
    String translationAAChecked = "", translationASChecked = "";
    String reviewOnlyAAChecked = "", reviewOnlyASChecked = "";
    String reviewReportIncludeCompactTags = "";
    String autoAcceptPMTaskChecked = "";	
    String checkUnTransSeg = "";
    String saveTranslationsEditReport = "";
    String saveReviewersCommentsReport = "";
    String saveOfflineFiles = "";
    String allowManualQAChecks = "";
    String autoAcceptQATask = "";
    String autoSendQAReport = "";

    String manualRunDitaChecksChecked = "";
    String autoAcceptDitaQaTaskChecked = "";
    String autoSendDitaQaReportChecked = "";
    
    if (project != null)
    {
        projectName = project.getName()==null?projectName:project.getName();
        pmName = project.getProjectManager()==null?pmName:project.getProjectManager().getUserName();
        termbase = project.getTermbaseName()==null?termbase:project.getTermbaseName();
        desc = project.getDescription()==null?desc: project.getDescription();
        if (project.getQuotePersonId() != null)
        {
        	qpId = project.getQuotePersonId();
        }
        pmId = project.getProjectManager()==null?pmId:project.getProjectManager().getUserId();
        if (desc == null) desc = "";
        
        attributeSet = project.getAttributeSet()==null?attributeSet:project.getAttributeSet();
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        pmcost = String.valueOf(nf.format(project.getPMCost()*100));
        if(project.getPoRequired() == 0) {
              checkOrNot = "";
        }

        translationAAChecked = project.getAutoAcceptTrans()? "checked" : "";
        translationASChecked = project.getAutoSendTrans()? "checked" : "";
        reviewOnlyAAChecked = project.getReviewOnlyAutoAccept()? "checked" : "";
        reviewOnlyASChecked = project.getReviewOnlyAutoSend()? "checked" : "";
        reviewReportIncludeCompactTags = project.isReviewReportIncludeCompactTags() ? "checked" : "";
        autoAcceptPMTaskChecked = project.getAutoAcceptPMTask()? "checked" : "";
        checkUnTransSeg = project.isCheckUnTranslatedSegments()? "checked" : "";
        saveTranslationsEditReport = project.getSaveTranslationsEditReport() ? "checked" : "";
        saveReviewersCommentsReport = project.getSaveReviewersCommentsReport() ? "checked" : "";
        saveOfflineFiles = project.getSaveOfflineFiles() ? "checked" : "";
        allowManualQAChecks = project.getAllowManualQAChecks() ? "checked" : "";
        autoAcceptQATask = project.getAutoAcceptQATask() ? "checked" : "";
        autoSendQAReport = project.getAutoSendQAReport() ? "checked" : "";

        manualRunDitaChecksChecked = project.getManualRunDitaChecks() ? "checked" : "";
        autoAcceptDitaQaTaskChecked = project.getAutoAcceptDitaQaTask() ? "checked" : "";
        autoSendDitaQaReportChecked = project.getAutoSendDitaQaReport() ? "checked" : "";
    }
    
    enableQAChecks = company.getEnableQAChecks();
    isEnableDitaChecks = company.getEnableDitaChecks();

    ArrayList fileProfileTermList = new ArrayList();
    
    String errorString = bundle.getString("msg_set_no_terminology_project_warning");
    errorString = errorString + "<br>*** File Profiles ***";
    
    String isSetTerminologyApproval = "false";
    String cancelURL = cancel.getPageURL() + "&action=cancel";


    // Labels of the column titles
    String nameCol = bundle.getString("lb_name");
    String firstNameCol = bundle.getString("lb_first_name");
    String lastNameCol = bundle.getString("lb_last_name");

    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String saveButton = bundle.getString("lb_save");

    // Data for the page
    List defUsers = (List)request.getAttribute("defUsers");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute("pageNum")).intValue();

    int numPages = ((Integer)request.getAttribute("numPages")).intValue();

    int listSize = defUsers == null ? 0 : defUsers.size();
    int totalDefUsers = ((Integer)request.getAttribute("listSize")).intValue();

    int defUsersPerPage = ((Integer)request.getAttribute(
        "numPerPage")).intValue();
    int defUserPossibleTo = pageNum * defUsersPerPage;
    int defUserTo = defUserPossibleTo > totalDefUsers ? totalDefUsers : defUserPossibleTo;
    int defUserFrom = (defUserTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionManager.getAttribute("sorting");

    ArrayList<String> addedUsersIds = (ArrayList<String>)request.getAttribute("addedUsersIds");
    ArrayList possibleUsers = (ArrayList)request.getAttribute("possibleUsers"); 
    
    if(request.getAttribute("fileProfileTermList") != null) {
        fileProfileTermList = (ArrayList)request.getAttribute("fileProfileTermList"); 
        for(int i = 0 ;i < fileProfileTermList.size(); i++) {
    isSetTerminologyApproval = "true";
    FileProfileImpl fp = (FileProfileImpl) fileProfileTermList.get(i);
    errorString = errorString + "<br> " + fp.getName();
        }
    }
%>

<%@page import="com.globalsight.cxe.entity.customAttribute.Attribute"%>
<html>
<!-- envoy\administration\projects\modifyProject.jsp -->
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>

<script language="JavaScript">
var needWarning = false;
var objectName = "<%=bundle.getString("lb_projects")%>";
var guideNode="projects";
var helpFile = "<%=bundle.getString("help_projects_edit")%>";
var translationAAChecked = "<%=translationAAChecked%>";
var reviewOnlyAAChecked = "<%=reviewOnlyAAChecked%>";

$(document).ready(function() {
	// Inital value for Auto Accept Elements.
	if("checked" == translationAAChecked)
	{
		setDisableTR('translationASTRID', false);
	}
	else
	{
		setDisableTR('translationASTRID', true);
	}
	if("checked" == reviewOnlyAAChecked)
	{
		setDisableTR('reviewOnlyASTRID', false);
	}
	else
	{
		setDisableTR('reviewOnlyASTRID', true);
	}
	
	if("checked" == "<%=reviewOnlyASChecked%>")
	{
		setDisableTR('reviewReportIncludeCompactTagsTD', false);
	}
	else
	{
		setDisableTR('reviewReportIncludeCompactTagsTD', true);
	}
	
	/** 
	 * If click the checkbox "Auto-accept Translation Task", 
	 * then the "Auto-send Translation Edit Report" TD should be editable. 
	 */
	$("#translationAA").click(function(){
		if(this.checked)
		{
			setDisableTR('translationASTRID', false);
		}
		else
		{
			setDisableTR('translationASTRID', true);
			$("#translationAS").attr("checked",false);
		}
	});
	/** 
	 * If click the checkbox "Auto-accept Review Task", 
	 * then the "Auto-send Reviewers Comments Report" TD should be editable. 
	 */
	$("#reviewOnlyAA").click(function(){
		if(this.checked)
		{
			setDisableTR('reviewOnlyASTRID', false);
		}
		else
		{
			setDisableTR('reviewOnlyASTRID', true);
			$("#reviewOnlyAS").attr("checked",false);
			setDisableTR('reviewReportIncludeCompactTagsTD', true);
			$("#reviewReportIncludeCompactTags").attr("checked",false);
		}
	});

	// For "Include compact tags" checkbox
	$("#reviewOnlyAS").click(function(){
		if(this.checked)
		{
			setDisableTR('reviewReportIncludeCompactTagsTD', false);
		}
		else
		{
			setDisableTR('reviewReportIncludeCompactTagsTD', true);
			$("#reviewReportIncludeCompactTags").attr("checked",false);
		}
	});

    if("checked" == "<%=autoAcceptQATask%>")
	{
	    setDisableTR('autoSendQAReportTR', false);
	}
	else
	{
	    setDisableTR('autoSendQAReportTR', true);
	}

    $("#autoAcceptPMTask").click(function(){
		if(this.checked)
		{
			if(!confirm("<%=bundle.getString("jsmsg_project_autoSend")%>"))
			{
				$(this).attr("checked",false);
			}
		}
	});
	
	$("#autoAcceptQATask").click(function(){
		if(this.checked)
		{
			setDisableTR('autoSendQAReportTR', false);
		}
		else
		{
			setDisableTR('autoSendQAReportTR', true);
			$("#autoSendQAReport").attr("checked", false);
		}
	});

    if ("checked" == "<%=autoAcceptDitaQaTaskChecked%>")
    {
        setDisableTR('autoSendDitaQaReportTRID', false);
    }
    else
    {
        setDisableTR('autoSendDitaQaReportTRID', true);
    }

    // For "Auto-Send DITA QA Report" checkbox
    $("#autoAcceptDitaQaTask").click(function(){
        if(this.checked)
        {
            setDisableTR('autoSendDitaQaReportTRID', false);
        }
        else
        {
            setDisableTR('autoSendDitaQaReportTRID', true);
            $("#autoSendDitaQaReport").attr("checked", false);
        }
    });

});

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
            projectForm.action = "<%=cancelURL%>";
            projectForm.submit();
            return false;
          
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            projectForm.action = "<%=saveUrl%>";
            projectForm.submit();
        }
        return false;
    }
     projectForm.formAction.value = formAction;
     // Got here from sort or next/prev
     projectForm.action = formAction;
     projectForm.submit();
  
}
function isFloat(field)
{
    var j = 0;
    for (var i = 0; i < field.length; i++)
    {
        if ((field.charAt(i) < "0" || field.charAt(i) > "9") && field.charAt(i) != ".")
        {
            return false;
        }
        if  (field.charAt(i) == ".") {
            if (j ++ > 0) {
                return false;
            }    
        } 
    }
    return true;
}

function format(val) {
	return Math.round(val * 10000) / 100;
}
		
function isValidRate(value) {
    buf = stripBlanks(value);
    if (!isFloat(buf) || isEmptyString(buf)){
      return false;
    }
    else {
      return true;
    }
}
//
//Check required fields and length of desc field.
//Check duplicate project name.
//
function confirmForm()
{
 if (isEmptyString(projectForm.nameField.value))
 {
     alert("<%= bundle.getString("jsmsg_project_name") %>");
     projectForm.nameField.value = "";
     projectForm.nameField.focus();
     return false;
 }
 if (hasSpecialChars(projectForm.nameField.value))
 {
     alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
     return false;
 }
 
 if(projectForm.tbField.value =='') {
     if('<%=isSetTerminologyApproval%>' == 'true') {
         var errorDiv = document.getElementById("errorDiv");
         errorDiv.innerHTML = '<%=errorString%>';
         errorDiv.style.display = "";
         return false;
     }
 }

<% if ((edit == false && projectManagers != null) || isAdminEditing) { %>


 if (projectForm.pmField.selectedIndex == 0)
 {
     alert("<%= bundle.getString("jsmsg_project_pm") %>");
     projectForm.pmField.focus();
     return false;
 }    
<% } 
if (edit && isAdminEditing) { %>
if ('<%=pmId%>' != projectForm.pmField.value && 
    !confirm('<%=warningMessage%>'))
 {
     return false;
 }
<% } 
%>
 theName = stripBlanks(projectForm.nameField.value);
<%
 if (projects != null)
 {
     for (int i = 0; i < projects.size(); i ++)
     {
         ProjectInfo proj = (ProjectInfo)projects.get(i);
%>
         if ("<%=proj.getName().toLowerCase()%>" ==  theName.toLowerCase())
         {
             alert("<%=bundle.getString("jsmsg_duplicate_project")%>");
             return false;
         }
<%
     }
 }
%>
 if (!isValidRate(projectForm.pmcost.value)) {
 	alert("<%=bundle.getString("lb_pmcost")%>" + "<%= bundle.getString("jsmsg_numeric") %>"); 
 	return false;
 }
 return true;
}

/**
 * Disable/Enable TR element
 * 
 * @param trId
 *            The id of TR item
 * @param isDisabled
 *            Disable/Enable flag
 */
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
		if ("checkbox" == elems[i].type) 
		{
			elems[i].disabled = isDisabled;
			elems[i].style.color = color;
		}
	}
}

var first = true;
function addUser()
{
    var from = projectForm.from;
    var to = projectForm.to;
    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_user_select") %>");
        return;
    }
    for (var i = 0; i < from.length; i++)
    {
        if (from.options[i].selected)
        {
            if (userInList(from.options[i].value))
            {
                continue;
            }
            if (first == true)
            {
<%
                if (addedUsersIds.isEmpty())
                {
%>
                    to.options[0] = null;
<%
                }
%>
                first = false;
            }
            var len = to.options.length;
            to.options[len] = new Option(from.options[i].text, from.options[i].value);

			//for GBS-1995,by fan
		    //set the selected element of left list is empty
		    from.options[i] = null;
            i--;
        }
    }
    saveUserIds();
}

function userInList(id)
{
    var to = projectForm.to;
    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].value == id)
        {
            return true;
        }
    }
    return false;
}

function removeUser()
{
	var from = projectForm.from;
    var to = projectForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_user_select") %>");
        return;
    }
    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].selected)
        {
			
			//for GBS-1995,by fan
		    //add selected element to left list
		    var len = from.options.length;
            from.options[len] = new Option(to.options[i].text, to.options[i].value);

            to.options[i] = null;
            i--;
        }
    }
    saveUserIds();
}
function saveUserIds()
{
    var to = projectForm.to;
    var options_string = "";
    var first = true;
    // Save userids in a comma separated string
    for (loop=0; loop < to.options.length; loop++)
    {
        if (first)
        {
            first = false;
        }
        else
        {
            options_string += ",";
        }
        options_string += to.options[loop].value;
    }
    projectForm.toField.value = options_string;
}

function changeSelectWidth(selected){
    if(selected.options[selected.selectedIndex].text.length*7 >= 220)  selected.style.width=selected.options[selected.selectedIndex].text.length*7 + 'px';
    else selected.style.width=200;
}
</script>
<style type="text/css">
@import url(/globalsight/includes/attribute.css);
</style>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">

    <span class="mainHeading">
<% if (edit == false) { %>
        <%=title%>
<% } else { %>
        <%=editTitle%>
<% } %>
    </span>

    <div id="errorDiv"  class="errorMsg" style="z-index: 1;left: 120px; right: 20px;display:none" ></div>
    <br/><br/>
    <table cellspacing="0" cellpadding="0" border=0 class="standardText" >
      <tr>
        <td width=500>
<% if (edit == false) { %>
            <%=bundle.getString("helper_text_project")%>
<% } else { %>
            <%=bundle.getString("helper_text_project2")%>
<% } %>
        </td>
      </tr>
    </table>
    <br>

<form name="projectForm" method="post" action="">
<input type="hidden" name="formAction" value="">

<table border="0" class="standardText" cellpadding="2">
    <tr>
        <td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
        <td>
            <input type="textfield" name="nameField" maxlength="40" size="30" value="<%=projectName%>" class="standardText">
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_manager")%><span class="asterisk">*</span>:</td>
        <td>
<%
        if ( projectManagers != null || isAdminEditing)
        {
%>
        <select name="pmField" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i=0; i < projectManagers.size(); i++) 
            {
                User pm = (User)projectManagers.get(i);
                out.println("<option value='" + pm.getUserId() + "'");
                if (pmId.equals(pm.getUserId()))
                     out.println(" selected ");
                out.println(">" +  pm.getFirstName() + " " + pm.getLastName() + "</option>");
            }
%>
        </select>
<%
        }
        else
        {
            out.println(pmName);
        }
%>
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_termbase")%>:</td>
        <td>
            <select name="tbField" class="standardText">
                <option value=""><%=bundle.getString("lb_no_termbase")%></option>
<%
                boolean flag = false;
                for (int i=0; i < termbases.size(); i++) 
                {
                    String tb = (String)termbases.get(i);
                    out.println("<option value='" + tb + "'");
                    if (tb.equals(termbase))
                    {
                        out.println(" selected ");
                        flag = true;
                    }
                    out.println(">" +  tb + "</option>");
                }
                if(!flag&&!termbase.equals(""))
                {
                    out.println("<option value='" + termbase + "' selected>"+termbase+
                            "</option>");
                }
%>
            </select>
        </td>
    </tr>
    <%if (viewAttributeGroup) {%>
    <tr>
        <td><%=bundle.getString("lb_attribute_group")%>:</td>
        <td>
            <select name="attributeSet" class="standardText">
                <option value="-1"><%=bundle.getString("lb_no_attribute_group_selected")%></option>
				<%
				    for (AttributeSet attribute : allAttributeGroups)
				    {
				        out.println("<option value='" + attribute.getId() + "'");
				        if (attributeSet != null && attributeSet.getId() == attribute.getId())
				        {
				            out.println(" selected ");
				        }
				        
				        if (1 == attribute.getCompanyId())
				        {
				            out.println(" class=\"superAttribute\" ");
				        }
				        
				        out.println(">" +  attribute.getName() + "</option>");
				    }
				%>
            </select>
        </td>
    </tr>
        <%}%>
    <tr>
        <td valign="top"><%=bundle.getString("lb_description")%>:</td>
        <td><textarea rows="3" cols="30" name="descField" class="standardText"><%=desc%></textarea></td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_quote_email_to")%>:</td>
        <td>
            <select name="qpField" class="standardText">
                <option value="-1"><%=bundle.getString("lb_no_quote")%></option>
<%
				out.println("<option value='0'");
				if ("0".equals(qpId))
				{
					out.println(" selected");
				}
				out.println(">" + bundle.getString("lb_quote_submitter") + "</option>");
%>              
<%
                for (int i=0; i < qps.size(); i++) 
                {
                    User qp = (User)qps.get(i);
                    out.println("<option value='" + qp.getUserId() + "'");
                    if (qpId.equals(qp.getUserId()))
                        out.println(" selected ");
                    out.println(">" +  qp.getFirstName() + " " + qp.getLastName() + "</option>");
                }
%>
            </select>
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_pmcost")%>:</td>
        <td><input type="text" class="standardText" id="pmcost" name="pmcost" value="<%=pmcost %>" maxlength="5" size=5 onfocus="this.select()"/>%</td>
    </tr>
    <tr>
        <td><%=bundle.getString("PO_required")%>:</td>
        <td><INPUT TYPE=checkbox id="poRequired" name="poRequired" onfocus="this.select()" <%=checkOrNot%> ></td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_translationAutoAccept")%>:</td>
        <td><INPUT TYPE=checkbox id="translationAA" name="translationAA" <%=translationAAChecked%> ></td>
    </tr>
    <tr id="translationASTRID">
        <td colspan="2">&nbsp;&nbsp;
          	<%=bundle.getString("lb_project_translationAutoSend")%>:
          	<INPUT TYPE=checkbox id="translationAS" name="translationAS" <%=translationASChecked%> >
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_reviewOnlyAutoAccept")%>:</td>
        <td><INPUT TYPE=checkbox id="reviewOnlyAA" name="reviewOnlyAA" <%=reviewOnlyAAChecked%> ></td>
    </tr>
    <tr id="reviewOnlyASTRID">
        <td colspan="2">&nbsp;&nbsp;
          	<%=bundle.getString("lb_project_reviewOnlyAutoSend")%>:
          	<INPUT TYPE=checkbox id="reviewOnlyAS" name="reviewOnlyAS" <%=reviewOnlyASChecked%> >
        </td>
    </tr>
    <tr id="reviewReportIncludeCompactTagsTD">
        <td colspan="2">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          	<%=bundle.getString("with_compact_tags")%>:
          	<INPUT TYPE=checkbox id="reviewReportIncludeCompactTags" name="reviewReportIncludeCompactTags" <%=reviewReportIncludeCompactTags%>>
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_AutoAcceptPMTask")%>:</td>
        <td><INPUT TYPE=checkbox id="autoAcceptPMTask" name="autoAcceptPMTask" <%=autoAcceptPMTaskChecked%> ></td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_checkUnTransSeg")%>:</td>
        <td><INPUT TYPE=checkbox id="checkUnTransSeg" name="checkUnTransSeg" <%=checkUnTransSeg%> >
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_saveTranslationsEditReport")%>:</td>
        <td><INPUT TYPE=checkbox id="saveTranslationsEditReport" name="saveTranslationsEditReport" <%=saveTranslationsEditReport%> >
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_saveReviewersCommentsReport")%>:</td>
        <td><INPUT TYPE=checkbox id="saveReviewersCommentsReport" name="saveReviewersCommentsReport" <%=saveReviewersCommentsReport%> >
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_saveOfflineFiles")%>:</td>
        <td><INPUT TYPE=checkbox id="saveOfflineFiles" name="saveOfflineFiles" <%=saveOfflineFiles%> >
        </td>
    </tr>
    <% if (enableQAChecks) {%>
    <tr>
        <td><%=bundle.getString("lb_project_allowManualQAChecks")%>:</td>
        <td><INPUT TYPE=checkbox id="allowManualQAChecks" name="allowManualQAChecks" <%=allowManualQAChecks%> >
        </td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_project_autoAcceptQATask")%>:</td>
        <td><INPUT TYPE=checkbox id="autoAcceptQATask" name="autoAcceptQATask" <%=autoAcceptQATask%> >
        </td>
    </tr>
    <tr id="autoSendQAReportTR">
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_project_autoSendQAReport")%>:
        <INPUT TYPE=checkbox id="autoSendQAReport" name="autoSendQAReport" <%=autoSendQAReport%> >
        </td>
    </tr>
    <%} %>
    <% if (isEnableDitaChecks) { %>
    <tr>
        <td><%=bundle.getString("lb_manual_run_dita_checks")%>:</td>
        <td><input type="checkbox" name="manualRunDitaQAChecks" <%=manualRunDitaChecksChecked%> /></td>
    </tr>
    <tr>
        <td><%=bundle.getString("lb_auto_accept_dita_qa_task")%>:</td>
        <td><input type="checkbox" id="autoAcceptDitaQaTask" name="autoAcceptDitaQaTask" <%=autoAcceptDitaQaTaskChecked%> /></td>
    </tr>
    <tr id="autoSendDitaQaReportTRID">
        <td colspan="2">&nbsp;&nbsp;<%=bundle.getString("lb_auto_send_dita_qa_report")%>:
        <input type="checkbox" id="autoSendDitaQaReport" name="autoSendDitaQaReport" <%=autoSendDitaQaReportChecked%> /></td>
    </tr>
    <% } %>
</table>

<br/><br/>

<TABLE CELLPADDING="0" CELLSPACING=0 BORDER=0 CLASS="standardText">
    <TR><TD><%=bundle.getString("msg_default_users")%>:</TD></TR>
    <TR><TD height="10px">&nbsp;</TD></TR>
    <TR VALIGN="TOP">
        <TD ALIGN="RIGHT">
        <%
        // Make the Paging widget
        if (listSize > 0)
        {
            Object[] args = {new Integer(defUserFrom), new Integer(defUserTo),
                     new Integer(totalDefUsers)};

            // "Displaying x to y of z"
            out.println(MessageFormat.format(
                    bundle.getString("lb_displaying_records"), args));

            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1) {
                // Don't hyperlink "Previous" if it's the first page
                out.print(bundle.getString("lb_previous"));
            }
            else
            {
                int num = pageNum - 1;
                String prevStr = selfUrl + "&pageNum=" + num +
                                 "&sorting=" + sortChoice;
%>
                <a href="javascript:submitForm('<%=prevStr%>')"><%=bundle.getString("lb_previous")%></A>
<%
            }

            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numPages; i++)
            {
                // Don't hyperlink the page you're on
                if (i == pageNum)
                {
                    out.print("<b>" + i + "</b>");
                }
                // Hyperlink the other pages
                else
                {
                    String nextStr = selfUrl + "&pageNum=" + i + "&sorting=" + sortChoice;
%>
                    <a href="javascript:submitForm('<%=nextStr%>')"><%=i%></A>
<%
                }
                out.print(" ");
            }
            // The "Next" link
            if (defUserTo >= totalDefUsers) {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
                int num = pageNum + 1;
                String nextStr = selfUrl + "&pageNum=" + num +
                                 "&sorting=" + sortChoice;
%>
                <a href="javascript:submitForm('<%=nextStr%>')"><%=bundle.getString("lb_next")%></A>

<%
            }
            out.println(" &gt;");
        }
%>
        </td>
    </TR>
    <tr>
        <td>
        <input type="hidden" name="toField" value="<%=(String)request.getAttribute("toField")%>">
            <!-- Project data table -->
            <table border="0" cellspacing="0" cellpadding="4" class="listborder" width="100%">
                <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
                  <td style="padding-right: 90px;">
                <% String col1 = selfUrl + "&pageNum=" + pageNum + "&sorting=" + UserInfoComparator.USERID + "&doSort=true"; %>
                    <a class="sortHREFWhite" href="javascript:submitForm('<%=col1%>')"> <%=nameCol%></a>
                  </td>
                  <td style="padding-right: 10px;" width="90px" nowrap>
                <% String col2 = selfUrl + "&pageNum=" + pageNum + "&sorting=" + UserInfoComparator.FIRSTNAME + "&doSort=true"; %>
                    <a class="sortHREFWhite" href="javascript:submitForm('<%=col2%>')"> <%=firstNameCol%></a>
                  </td>
                  <td style="padding-right: 10px;">
                <% String col3 = selfUrl + "&pageNum=" + pageNum + "&sorting=" + UserInfoComparator.LASTNAME + "&doSort=true"; %>
                    <a class="sortHREFWhite" href="javascript:submitForm('<%=col3%>')"> <%=lastNameCol%></a>
                  </td>
                </tr>
                <% if (listSize == 0) { %>
                <tr>
                  <td colspan=3 class='standardText'><%=bundle.getString("msg_no_default_users")%></td>
                </tr>
                <% } else {
                    for (int i=0; i < listSize; i++)
                    {
                        String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                        UserInfo userInfo = (UserInfo)defUsers.get(i);
                %>
                <tr style="padding-bottom:5px; padding-top:5px;" valign=top bgcolor="<%=color%>">
                  <td><span class="standardText"><%=userInfo.getUserName()%></span></td>
                  <td><span class="standardText"><%=userInfo.getFirstName()%></span></td>
                  <td><span class="standardText"><%=userInfo.getLastName()%></span></td>
                </tr>
                <% }
                } %>
              </tbody>
            </table>
        </TD>
    </TR>
</TABLE>

<br/>
<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="3" class="standardText" colspan="3"><%=bundle.getString("msg_add_remove_users")%></td>
    </tr>
    <tr><td colspan="3" height="10px">&nbsp;</td></tr>
    <tr>
        <td class="standardText"><%=bundle.getString("lb_available")%>:</td>
        <td>&nbsp;</td>
        <td class="standardText"><%=bundle.getString("lb_added")%>:</td>
    </tr>
    <tr>
        <td height="100%">
        <select name="from" multiple class="standardText" size=15 style="width:200px;" onchange="changeSelectWidth(this)">
<%
		if (possibleUsers != null)
		{
			for (int i = 0; i < possibleUsers.size(); i++)
			{
				UserInfo userInfo = (UserInfo)possibleUsers.get(i);

				//for GBS-1995,by fan
				//don't display the element in the left list ,if the the element is existed in the right list.
				if (!addedUsersIds.isEmpty())
				{
					boolean isExist = false;  //if the user is existed in the right list, return true.

					Iterator<String> iter = addedUsersIds.iterator();

					while(iter.hasNext())
					{
						String userId = iter.next();
						if(userId.equals(userInfo.getUserId())) isExist = true;

					}
					if(!isExist)
					{		
%>
						<option value="<%=userInfo.getUserId()%>" ><%=userInfo.getUserName()%></option>
<%
					}

				}
				else
				{
%>
						<option value="<%=userInfo.getUserId()%>" ><%=userInfo.getUserName()%></option>
<%
				}
			}
		}
%>
        </select>
        </td>
        <td align="center">
          <table>
            <tr>
              <td><input type="button" name="addButton" value=" >> " onclick="addUser()"><br></td>
            </tr>
            <tr><td>&nbsp;</td></tr>
            <tr>
                <td><input type="button" name="removedButton" value=" << " onclick="removeUser()"></td>
            </tr>
          </table>
        </td>
        <td height="100%">
            <select name="to" multiple class="standardText" size=15 style="width:200px;" onchange="changeSelectWidth(this)">
<%
                if (!addedUsersIds.isEmpty())
                {
                    Iterator<String> iter = addedUsersIds.iterator();
                    while (iter.hasNext())
                    {
                       String userId = iter.next();

%>
                       <option value="<%=userId%>" ><%=UserUtil.getUserNameById(userId)%></option>
<%
                    }
                }
                else
                {
%>
                   <option>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
<%
                }
%>
            </select>
          </td>
      </tr>
</table>

</form>

<div>
    <input type="button" class="standardText" name="<%=lbcancel%>" value="<%=lbcancel%>" onclick="submitForm('cancel')">
    <input type="button" class="standardText" name="<%=lbsave%>" value="<%=lbsave%>" onclick="submitForm('save')">
</div>

</body>
</html>
