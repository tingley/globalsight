<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.tags.TableConstants,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.cvsconfig.*,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String nextUrl = next.getPageURL() + "&action=next";
    String cancelUrl = cancel.getPageURL() + "&action=cancel";

    String title= bundle.getString("lb_rss_job");

    String projectLabel = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_LABEL);
    String projectJsMsg = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_JS_MSG);
    // Button names
    String nextButton = bundle.getString("lb_next");
    String cancelButton = bundle.getString("lb_cancel");

    // Get data (if they hit the back button)
    String jobName = (String)sessionMgr.getAttribute("jobName");
    if (jobName == null) jobName = "";
    String notes = (String)sessionMgr.getAttribute("notes");
    if (notes == null) notes = "";
    String pId = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_ID);
    long projectId = pId == null ? -1 : Long.parseLong(pId);
    
    // get list of projects to be displayed
    List projectInfos = (List)sessionMgr.getAttribute("projectInfos");
    int numOfProjects = projectInfos == null ? -1 : projectInfos.size();
    
%>
<html>
<head>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "myJobs";
var objectName = "";
var helpFile = "<%=bundle.getString("help_rss_job")%>";

function submitForm(selectedButton)
{
   if (selectedButton == "cancel")
   {
       customerForm.action = "<%=cancelUrl%>";
   }
    if (selectedButton == "next")
    {
        if (!validateForm())
        {
            return;
        }
        customerForm.action = "<%=nextUrl%>";
    }
    customerForm.jobField.value = encodeURIComponent(customerForm.jobName.value);
    customerForm.submit();
}

function validateForm()
{
    //Remove the leading spaces, trailing spaces and any number of spaces in the middle.
    customerForm.jobName.value = trimMutipleSpaces(customerForm.jobName.value);

    // check required fields
    if (isEmptyString(customerForm.jobName.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_customer_job_name"))%>");
        customerForm.jobName.value = "";
        customerForm.jobName.focus();
        return false;
    }
    
    // Do not allow "\",  "/", ":" and other characters in the job name
    // that are not valid in Windows (or Unix) filenames.
    var jobNameRegex = /[\\/:;\*\?\|\"<>&%]/;
    if (jobNameRegex.test(customerForm.jobName.value))
    {
       alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_invalid_job_name"))%>");
       return false;
    }

    if (customerForm.srcLocales.selectedIndex == 0)
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_customer_src_locale"))%>");
        return false;
    }
    if (customerForm.projects.selectedIndex == 0)
    {
        alert("<%=EditUtil.toJavascript(projectJsMsg)%>");
        return false;
    }
    if (isEmptyString(customerForm.notesField.value))
    {
        customerForm.notesField.value = " ";
    }
    return true;
}

//Remove the leading spaces, trailing spaces and any number of spaces in the middle.
function trimMutipleSpaces(strText)
{
    strText = strText.replace(/(\s{2,})/g, " ");
    strText = strText.replace(/(^\s*)|(\s*$)/g, "");
    return strText;
}

function doOnload()
{
    loadGuides();
}

</SCRIPT>
</head>
<!-- This DIV is for used for the default selection of locales -->
<DIV id="idPreferences" STYLE="behavior:url(#default#userData); display: none;" class="preferences"></DIV>

<body LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="doOnload();" >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<div ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span CLASS="mainHeading">
    <%=title%>
    </span>

<p>
<form name="customerForm" method="post" >
<input type="hidden" name="selectFiles" value="rssJob"/>
<table cellspacing="0" cellpadding="4" border="0" class="standardText">
  <tr>
    <td>
        <%=bundle.getString("lb_job")%> <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
    </td>
    <td>
        <input type="text" name="jobName" size=30 value="<%=jobName%>" maxlength="120">
        <input type="hidden" name="jobField" size=30 value="<%=jobName%>" maxlength="120">
    </td>
  </tr>
  <jsp:include page="localeDropDowns.jsp"/>
  <tr>
      <td class="standardText">
        <%=projectLabel%><span class="asterisk">*</span>:
      </td>
      <td class="standardText">
        <select name="projects">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i =0; i < numOfProjects; i++)
            {
                ProjectInfo p = (ProjectInfo)projectInfos.get(i);
                long id = p.getProjectId();
                String name = p.getName();
                out.println("<option value=\"" + id + "," + name + "\"");
                if (id == projectId)
                {
                    out.print(" selected ");
                }
                out.println(">" + name + "</option>");                
            }
%>
        </select>
      </td>
    </tr>
  <tr>
    <td valign=top>
        <%=bundle.getString("lb_cvs_job_notes")%>:
    </td>
    <td>
        <textarea name="notesField" rows=5 cols=40><%=notes%></textarea>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px">
        <input type=button name="cancelBtn" value="<%=cancelButton%>"
         onclick="submitForm('cancel')">
        <input type=button name="nextBtn" value="<%=nextButton%>"
         onclick="submitForm('next')">
    </td>
  </tr>
</table>
</form>
</div>
</body>
</html>
