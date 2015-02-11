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
     com.globalsight.util.collections.HashtableValueOrderWalker"
	 session="true" %>

<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="nextEdit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
   ResourceBundle bundle = PageHandler.getBundle(session); 
   // bring in "state" from session
   SessionManager sessionMgr = (SessionManager) request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
   Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);


    String nextURL = next.getPageURL();
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String helperText;

    boolean edit = false;
    String title = null;
    if (sessionMgr.getAttribute("edit") != null)
    {
        edit = true;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_loc_profile");
        helperText = bundle.getString("helper_text_loc_profile_edit") + " " +
                    bundle.getString("helper_text_refer_to_help");
        nextURL = nextEdit.getPageURL();
    }
    else
    {
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_loc_profile");
        helperText = bundle.getString("helper_text_loc_profile_enter") + " " +
                   bundle.getString("helper_text_refer_to_help");
    }

    String labelWorkFlowDispatch = bundle.getString("lb_workflow_dispatch");
    String lbcancel = bundle.getString("lb_cancel");
    String lbnext = bundle.getString("lb_next");
    String choiceAutomatic = bundle.getString("lb_automatic");
    String choiceManual = bundle.getString("lb_manual");
    String choiceBatch = bundle.getString("lb_by_batch");
    String choiceYes = bundle.getString("lb_yes");
    String choiceNo = bundle.getString("lb_no");

    // Data
    Object[] names = (Object[])sessionMgr.getAttribute("names");
    List tmProfiles = (List)sessionMgr.getAttribute("tmProfiles");
    List projects = (List)sessionMgr.getAttribute("projects");
    List locales = (List)sessionMgr.getAttribute("srcLocales");
    Integer buf = (Integer)sessionMgr.getAttribute("maxPriority");
    int maxPriority = buf.intValue();

    String lpName = (String)sessionMgr.getAttribute(LocProfileStateConstants.LOC_PROFILE_NAME);
    if (lpName == null) lpName = "";
    String desc = (String)sessionMgr.getAttribute(LocProfileStateConstants.LOC_PROFILE_DESCRIPTION);
    if (desc == null) desc = "";
    String optScript = (String)sessionMgr.getAttribute(LocProfileStateConstants.LOC_PROFILE_SQL_SCRIPT);
    if (optScript == null) optScript = "";
    long tmProfileId = -1;
    String id = (String)sessionMgr.getAttribute(LocProfileStateConstants.LOC_TM_PROFILE_ID);
    if (id != null) tmProfileId = Long.parseLong(id);
    long projectId = -1;
    id = (String)sessionMgr.getAttribute(LocProfileStateConstants.LOC_PROFILE_PROJECT_ID);
    if (id != null) projectId = Long.parseLong(id);
    id = (String)sessionMgr.getAttribute(LocProfileStateConstants.JOB_PRIORITY);
    int priority;
    if (id != null) 
        priority = Integer.parseInt(id);
    else
    {
        Integer def = (Integer)sessionMgr.getAttribute("defaultPriority");
        priority = def.intValue();
    }
    id = (String)sessionMgr.getAttribute(LocProfileStateConstants.SOURCE_LOCALE_ID);
    long srcLocaleId = -1;
    if (id != null) srcLocaleId = Long.parseLong(id);
    id = (String)sessionMgr.getAttribute(LocProfileStateConstants.LOC_PROFILE_TM_USAGE_ID);
    int tmChoice = -1;
    if (id != null) tmChoice = Integer.parseInt(id);
    id = (String)sessionMgr.getAttribute(LocProfileStateConstants.AUTOMATIC_DISPATCH);
    boolean autoDispatch = true;
    if (id != null && id.equals("false")) autoDispatch = false;

    
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_loc_profile") %>";
var guideNode = "locProfiles";
var helpFile = "<%=bundle.getString("help_localization_profiles_basic_info")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        locprofileForm.action = "<%=cancelURL%>";
        locprofileForm.submit();
    }
    if (formAction == "next")
    {
        if (confirmForm())
        {
            locprofileForm.action = "<%=nextURL%>";
            locprofileForm.submit();
        }
    }
}

function confirmForm()
{
    if (isEmptyString(locprofileForm.LocProfileName.value)) {
        alert("<%= bundle.getString("jsmsg_loc_profiles_name") %>");
        locprofileForm.LocProfileName.value = "";
        locprofileForm.LocProfileName.focus = "";
        return false;
    }
    if (hasSpecialChars(locprofileForm.LocProfileName.value))
    {
        alert("<%= bundle.getString("lb_name")%>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    <% 
      
        for (int i = 0; i < names.length; i++)
        {
            String name = (String)names[i];
    %>
            if (locprofileForm.LocProfileName.value.toLowerCase() == "<%=name%>".toLowerCase() &&
                locprofileForm.LocProfileName.value != "<%=lpName%>")
            {
                alert('<%= bundle.getString("jsmsg_duplicate_loc_profile")%>');
                return false;
            }
    <%
        }
    %>
    if (!isNotLongerThan(locprofileForm.LocProfileDescription.value, 256)) {
        alert("<%= bundle.getString("jsmsg_description") %>");
        locprofileForm.LocProfileDescription.focus();
        return false;
    }
    if (locprofileForm.locTMProfileId.selectedIndex == 0) 
    {
        alert("<%= bundle.getString("jsmsg_loc_tm_profiles") %>");
        return false;
    }
    if (locprofileForm.LocProfileProjectId.selectedIndex == 0) 
    {
        alert("<%= bundle.getString("jsmsg_loc_profiles_project") %>");
        return false;
    }
    <% if (!edit) { %>
    if (locprofileForm.SourceLocaleId.selectedIndex == 0) 
    {
        alert("<%= bundle.getString("jsmsg_loc_profiles_source_locale") %>");
        return false;
    }
    <% } %>
    return true;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
<TR>
<TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%=title%></TD>
</TR>
<TR>

<TR>
<TD COLSPAN="3" CLASS="standardText" STYLE="padding-left: 10px" WIDTH=500>
<%=helperText%>
</TD>
</TR>

<P>
<TD VALIGN="TOP">
<form name="locprofileForm" method="post" action="">

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
            <input type="textfield" name="LocProfileName" maxlength="40" size="30"
                    value="<%=lpName%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_description")%>:
          </td>
          <td>
            <textarea rows="6" cols="40" name="LocProfileDescription"><%=desc%></textarea>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_sql_script_option")%>:
          </td>
          <td>
            <input type="textfield" name="LocProfileSQLScript" maxlength="40" size="30"
                    value="<%=optScript%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_tm_profiles")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="locTMProfileId">
               <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i = 0; i < tmProfiles.size(); i++)
            {
                TranslationMemoryProfile tmp = (TranslationMemoryProfile)
                    tmProfiles.get(i);
                if (tmp.getId() == tmProfileId)
                    out.println("<option value=" + tmp.getId() + " selected>" +
                                 tmp.getName() + "</option>");
                else
                    out.println("<option value=" + tmp.getId() + ">" + tmp.getName() +
                                "</option>");

                
            }
%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_project")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="LocProfileProjectId" <%=edit?"disabled":"" %>>
               <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i = 0; i < projects.size(); i++)
            {
                ProjectInfo pi = (ProjectInfo) projects.get(i);
                if (pi.getProjectId() == projectId)
                    out.println("<option value=" + pi.getProjectId() + " selected>" +
                                 pi.getName() + "</option>");
                else
                    out.println("<option value=" + pi.getProjectId() + ">" + pi.getName() +
                                "</option>");

            }
%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_priority")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="JobPriority">
<%
            for (int i = 1; i <= maxPriority; i++)
            {
                if (priority == i)
                    out.println("<option value=" + i + " selected>" + i + "</option>");
                else
                    out.println("<option value=" + i + ">" + i + "</option>");
            }
%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_source_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="SourceLocaleId">
<%
            if (!edit)
               out.println("<option value=-1>" +bundle.getString("lb_choose") +"</option>");
            for (int i = 0; i < locales.size(); i++)
            {
                GlobalSightLocale locale = (GlobalSightLocale) locales.get(i);
                if (!edit)
                {
                    if (locale.getId() == srcLocaleId)
                        out.println("<option value=" + locale.getId() + " selected>" +
                                 locale.getDisplayName(uiLocale) + "</option>");
                    else
                        out.println("<option value=" + locale.getId() + ">" +
                                 locale.getDisplayName(uiLocale) + "</option>");
                }
                else if (locale.getId() == srcLocaleId)
                {
                    // Only 1 item in drop down for edit
                    out.println("<option value=" + locale.getId() + ">" +
                                 locale.getDisplayName(uiLocale) + "</option>");
                    break;
                }
            }
%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_use_tm")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="LocProfileTMUsageId">
<%
            out.print("<option value=" + LocProfileStateConstants.ALLOW_EDIT_TM_USAGE);
            if (tmChoice == LocProfileStateConstants.ALLOW_EDIT_TM_USAGE)
                out.print(" selected");
            out.print(">" + bundle.getString("lb_regular_tm") + "</option>");
            out.print("<option value=" + LocProfileStateConstants.DENY_EDIT_TM_USAGE);
            if (tmChoice == LocProfileStateConstants.DENY_EDIT_TM_USAGE)
                out.print(" selected");
            out.print(">" + bundle.getString("lb_regular_and_page_tm") + "</option>");
            out.print("<option value=" + LocProfileStateConstants.NO_TM_USAGE);
            if (tmChoice == LocProfileStateConstants.NO_TM_USAGE)
                out.print(" selected");
            out.print(">" + bundle.getString("lb_no") + "</option>");
%>
            </select>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_workflow_dispatch")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="AutomaticDispatch">
<%
            out.print("<option value=true");
            if (autoDispatch)
                out.print(" selected");
            out.print(">" + bundle.getString("lb_automatic") + "</option>");
            out.print("<option value=false");
            if (!autoDispatch)
                out.print(" selected");
            out.print(">" + bundle.getString("lb_manual") + "</option>");
%>
            </select>
          </td>
        </tr>
        <tr><td colspan="2">&nbsp;</td></tr>
      <tr>
        <td colspan="2">
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbnext%>" value="<%=lbnext%>"
            onclick="submitForm('next')">
        </td>
      </tr>
    </table>
</form>
</body>
</html>
