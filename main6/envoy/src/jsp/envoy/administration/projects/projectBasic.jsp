<%@page import="java.text.NumberFormat"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.permission.Permission,
                  com.globalsight.everest.permission.PermissionSet,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.projecthandler.ProjectImpl,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.everest.foundation.User,
                  com.globalsight.util.GeneralException,
                  com.globalsight.cxe.entity.customAttribute.AttributeSet,
                  com.globalsight.everest.company.CompanyWrapper,
                  com.globalsight.cxe.entity.fileprofile.FileProfileImpl,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.List,
                  java.util.ArrayList,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="users" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
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
    List projectManagers = (List)sessionManager.getAttribute("pms");
    Vector qps = (Vector)sessionManager.getAttribute("qePersons");
    List termbases = (List)sessionManager.getAttribute("termbases");
    List projects = (List)sessionManager.getAttribute("projects");
    ProjectImpl project = (ProjectImpl)sessionManager.getAttribute("project");
    List<AttributeSet> allAttributeGroups = (List<AttributeSet>)sessionManager.getAttribute("allAttributeGroups");
    AttributeSet attributeSet = null;
    
    
    String projectName = "";
    String pmName = (String)sessionManager.getAttribute("pm");
    String qpId = "";
    String termbase = "";
    String desc = "";
    String pmId = "";
    boolean edit = false;
    boolean isAdminEditing = false;
    String pmcost = "0";
    
    PermissionSet perms = (PermissionSet) session.getAttribute(
            WebAppConstants.PERMISSIONS);
    boolean viewAttributeGroup = perms.getPermissionFor(Permission.ATTRIBUTE_GROUP_VIEW);
    if (sessionManager.getAttribute("edit") != null)
    {
        edit = true;
        
        isAdminEditing = perms.getPermissionFor(Permission.PROJECTS_EDIT_PM);
    }
    
    String checkOrNot = "checked";
    String reviewOnlyAAChecked = "", reviewOnlyASChecked = "";
    String autoAcceptPMTaskChecked = "";	
    if (project != null)
    {
        projectName = project.getName();
        pmName = project.getProjectManager().getUserName();
        termbase = project.getTermbaseName();
        desc = project.getDescription();
        if (project.getQuotePersonId() != null)
        {
        	qpId = project.getQuotePersonId();
        }
        pmId = project.getProjectManager().getUserId();
        if (desc == null) desc = "";
        
        attributeSet = project.getAttributeSet();
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        pmcost = String.valueOf(nf.format(project.getPMCost()*100));
        
        if(project.getPoRequired() == 0) {
            checkOrNot = "";
        }
        
        reviewOnlyAAChecked = project.getReviewOnlyAutoAccept()? "checked" : "";
        reviewOnlyASChecked = project.getReviewOnlyAutoSend()? "checked" : "";
        autoAcceptPMTaskChecked = project.getAutoAcceptPMTask()? "checked" : "";
    }
    
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String doneURL = done.getPageURL() + "&action=save";
    String usersURL = users.getPageURL();
    String nextURL = next.getPageURL();

    ArrayList fileProfileTermList = new ArrayList();
    
    String errorString = bundle.getString("msg_set_no_terminology_project_warning");
    errorString = errorString + "<br>*** File Profiles ***";
    
    String isSetTerminologyApproval = "false";
    
    if(sessionManager.getAttribute("fileProfileTermList") != null) {
        fileProfileTermList = (ArrayList)sessionManager.getAttribute("fileProfileTermList"); 
        for(int i = 0 ;i < fileProfileTermList.size(); i++) {
            isSetTerminologyApproval = "true";
            FileProfileImpl fp = (FileProfileImpl) fileProfileTermList.get(i);
            errorString = errorString + "<br> " + fp.getName();
        }
    }
%>

<%@page import="com.globalsight.cxe.entity.customAttribute.Attribute"%>
<html>
<!-- envoy\administration\projects\projectBasic.jsp -->
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>

<script language="JavaScript">
var needWarning = false;
var objectName = "<%=bundle.getString("lb_projects")%>";
var guideNode="projects";
var helpFile = "<%=bundle.getString("help_projects_edit")%>";
var reviewOnlyAAChecked = "<%=reviewOnlyAAChecked%>";

function submitForm(formAction)
{
    projectForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=cancelURL%>";
           projectForm.submit();
       }
       else
       {
          return false;
       }
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            projectForm.action = "<%=doneURL%>";
            projectForm.submit();
        }
    }
    else if (formAction == "next")
    {
        if (confirmForm())
        {
            projectForm.action = "<%=nextURL%>";
            projectForm.submit();
        }
    }
    else if (formAction == "users")
    {
        if (confirmForm())
        {
            projectForm.action = "<%=usersURL%>";
            projectForm.submit();
        }
    }
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
// Check required fields and length of desc field.
// Check duplicate project name.
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
            if ("<%=proj.getName().toLowerCase()%>" ==  theName.toLowerCase() &&
                "<%=proj.getName()%>" != "<%=projectName%>")
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

$(document).ready(function() {
	if("checked" == reviewOnlyAAChecked)
	{
		setDisableTR('reviewOnlyASTRID', false);
	}
	else
	{
		setDisableTR('reviewOnlyASTRID', true);
	}
	
	$("#reviewOnlyAA").click(function(){
		if(this.checked)
		{
			setDisableTR('reviewOnlyASTRID', false);
		}
		else
		{
			setDisableTR('reviewOnlyASTRID', true);
			$("#reviewOnlyAS").attr("checked",false);
		}
	});
	
	$("#autoAcceptPMTask").click(function(){
		if(this.checked)
		{
			if(!confirm("<%=bundle.getString("jsmsg_project_autoSend")%>"))
			{
				$(this).attr("checked",false);
			}
		}
	});
});
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

    <div id="errorDiv"  class="errorMsg" style="z-index: 1;left: 120px; right: 20px;display:none" >
    </div>
    <br>
    <br>
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
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="textfield" name="nameField" maxlength="40" size="30" 
                value="<%=projectName%>">
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_project_manager")%><span class="asterisk">*</span>:
          </td>
          <td>
<%
            if ((edit == false && projectManagers != null) || isAdminEditing)
            {
%>
            <select name="pmField">
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
          <td>
            <%=bundle.getString("lb_termbase")%>:
          </td>
          <td>
            <select name="tbField">
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
          <td>
            <%=bundle.getString("lb_attribute_group")%>:
          </td>
          <td>
            <select name="attributeSet">
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
          <td valign="top">
            <%=bundle.getString("lb_description")%>:
          </td>
          <td>
            <textarea rows="3" cols="30" name="descField"><%=desc%></textarea>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_quote_email_to")%>:
          </td>
          <td>
            <select name="qpField">
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
          <td><input type="text" id="pmcost" name="pmcost" value="<%=pmcost %>" maxlength="5" size=5 onfocus="this.select()"/>%</td>
        </tr>
        <tr>
          <td><%=bundle.getString("PO_required")%>:</td>
          <td><INPUT TYPE=checkbox id="poRequired" name="poRequired" onfocus="this.select()" <%=checkOrNot%> ></td>
        </tr>
                
        <tr>
          <td><%=bundle.getString("lb_project_reviewOnlyAutoAccept")%>:</td>
          <td><INPUT TYPE=checkbox id="reviewOnlyAA" name="reviewOnlyAA" <%=reviewOnlyAAChecked%> >
          </td>
        </tr>
        <tr id="reviewOnlyASTRID">
          <td colspan="2">&nbsp;&nbsp;
          	<%=bundle.getString("lb_project_reviewOnlyAutoSend")%>:
          	<INPUT TYPE=checkbox id="reviewOnlyAS" name="reviewOnlyAS" <%=reviewOnlyASChecked%> >
          </td>
        </tr>
        <tr>
          <td><%=bundle.getString("lb_project_AutoAcceptPMTask")%>:</td>
          <td><INPUT TYPE=checkbox id="autoAcceptPMTask" name="autoAcceptPMTask" <%=autoAcceptPMTaskChecked%> >
          </td>
        </tr>
        
      <tr><td colspan="2">&nbsp;</td></tr>
      <tr>
        <td colspan="2">
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
<% if (edit == true) { %>
          <input type="button" name="<%=lbusers%>" value="<%=lbusers%>"
            onclick="submitForm('users')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
<% } else { %>
          <input type="button" name="<%=lbnext%>" value="<%=lbnext%>"
            onclick="submitForm('next')">
<% } %>
        </td>
      </tr>
    </table>
</form>

</body>
</html>
