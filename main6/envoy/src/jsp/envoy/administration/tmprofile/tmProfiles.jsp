<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.util.comparator.TMProfileComparator,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.util.GeneralException,
            java.text.MessageFormat,
            java.util.Date,
            java.util.Vector,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.foundation.BasicL10nProfile"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="tda_edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="tmProfiles" class="java.util.ArrayList" scope="request"/>

<%@ include file="/envoy/common/header.jspIncl" %>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String title= bundle.getString("lb_tm_profiles");
    String helperText = bundle.getString("helper_text_tm_profile_main");
    //Button names
    String newButton = bundle.getString("lb_new");
    String removeButton = bundle.getString("lb_remove");
    //Urls of the links on this page
    String action = TMProfileConstants.ACTION;
    String selfUrl = self.getPageURL();
    String newUrl = new1.getPageURL() + "&" + action + "=" + TMProfileConstants.NEW_ACTION;
    String modifyUrl = modify.getPageURL()+ "&" + action + "=" + TMProfileConstants.EDIT_ACTION;
    String removeUrl = remove.getPageURL() + "&" + action + "=" + TMProfileConstants.REMOVE_ACTION;
    String tdaEditUrl = tda_edit.getPageURL() + "&" + action + "=" + TMProfileConstants.MT_EDIT_ACTION;

    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    Collection l10nProfiles = null;
    try {
        l10nProfiles = ServerProxy.getProjectHandler().getAllL10nProfiles();
    } catch (Exception ex) {}

    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

    String filterNameValue = (String) sessionManager.getAttribute(TMProfileConstants.FILTER_NAME);
    if (filterNameValue == null || filterNameValue.trim().length() == 0){
        filterNameValue = "";
    }
    String filterStorageTmValue = (String) sessionManager.getAttribute(TMProfileConstants.FILTER_STORAGE_TM);
    if (filterStorageTmValue == null || filterStorageTmValue.trim().length() == 0) {
        filterStorageTmValue = "";
    }
    String filterCompanyValue = (String) sessionManager.getAttribute(TMProfileConstants.FILTER_COMPANY_NAME);
    if (filterCompanyValue == null || filterCompanyValue.trim().length() == 0) {
        filterCompanyValue = "";
    }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_tmProfile") %>";
var guideNode = "tmProfiles";
var helpFile = "<%=bundle.getString("help_tmprofile")%>";

function handleSelectAll() {
    var selectAll = $("#selectAll").is(":checked");
    $("input[name='checkboxBtn']").each(function() {
        if (selectAll == true){
            $(this).attr("checked", true);
        } else {
            $(this).attr("checked", false);
        }
    });

    buttonManagement();
}

function buttonManagement() {
    var count = $("input[name='checkboxBtn']:checked").length;

    // Only one TM profile is selected
    if (count == 1) {
        if (TMProfileForm.removeBtn) {
            TMProfileForm.removeBtn.disabled = false;
        }

        if (TMProfileForm.tdaEditBtn) {
            TMProfileForm.tdaEditBtn.disabled = false;
        }
    }
    // count == 0 or count > 1
    else {
        if (TMProfileForm.removeBtn) {
            TMProfileForm.removeBtn.disabled = true;
        }

        if (TMProfileForm.tdaEditBtn) {
            TMProfileForm.tdaEditBtn.disabled = true;
        }
    }
}

// Click "New" button
function newTmProfile()
{
    TMProfileForm.action = '<%=newUrl%>';
    TMProfileForm.submit();
}

// Edit
function modifyTMProfile(tmProfileId)
{
    TMProfileForm.action = "<%=modifyUrl%>&<%=TMProfileConstants.TM_PROFILE_ID%>=" + tmProfileId;
    TMProfileForm.submit();
}

// Click "Remove" button
function removeTmProfile()
{
    value = findSelectedTmProfiles();
    var rtnMsg = ifCanBeRemoved(value);
    if ( rtnMsg == "" ) {
        TMProfileForm.action = "<%=removeUrl%>&<%=TMProfileConstants.TM_PROFILE_ID%>=" + value;
        TMProfileForm.submit();
    } else {
        alert(rtnMsg);
        return false;
    }
}

// Click "TDA Options" button
function editTdaOptions()
{
    value = findSelectedTmProfiles();
    TMProfileForm.action = "<%=tdaEditUrl%>&<%=TMProfileConstants.TM_PROFILE_ID%>=" + value;
    TMProfileForm.submit();
}

//Find selected TM profiles' IDs.
//Actually there is only one ID in current implementation.
function findSelectedTmProfiles()
{
    var ids = "";
    $("input[name='checkboxBtn']:checked").each(function (){
        ids += $(this).val() + ",";
    });
    if (ids != "")
        ids = ids.substring(0, ids.length - 1);

    return ids;
}

function ifCanBeRemoved(selectedTmProfileId)
{
    var rtnMsg = "";
    <%
    if ( l10nProfiles != null && l10nProfiles.size() > 0 ) 
    {
        for (Iterator iter = l10nProfiles.iterator(); iter.hasNext();)
        {
            BasicL10nProfile l10nProfile = (BasicL10nProfile)iter.next(); 
            TranslationMemoryProfile tmProfile = l10nProfile.getTranslationMemoryProfile();
            long tmProfileId = -1;
            if (tmProfile != null) {
                tmProfileId = tmProfile.getId();
            }
            %>
            if ( '<%=tmProfileId%>' == selectedTmProfileId ) 
            {
                if ( rtnMsg == "" ) {
                    rtnMsg = "<%=bundle.getString("msg_tm_remove_tmp_lp") %>";
                    rtnMsg = rtnMsg + "\n   " + '<%=l10nProfile.getName()%>';
                } else {
                    rtnMsg = rtnMsg + "\n   " + '<%=l10nProfile.getName()%>';
                }
            }
    <%
        }
    }
    %>

    return rtnMsg;
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
        TMProfileForm.action = "<%=selfUrl%>";
        TMProfileForm.submit();
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<FORM NAME="TMProfileForm" id="TMProfileForm" METHOD="POST">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="tmProfiles" key="<%=TMProfileConstants.TMP_KEY%>" pageUrl="self" />
        </td>
    </tr>
    <tr>
        <td>
          <amb:table bean="tmProfiles" id="tmProfile" key="<%=TMProfileConstants.TMP_KEY%>"
                 dataClass="com.globalsight.everest.projecthandler.TranslationMemoryProfile"
                 pageUrl="self"
                 emptyTableMsg="msg_no_tm_profiles" hasFilter="true">
            <amb:column label="checkbox" width="2%">
                <input type="checkbox" id="checkboxBtn" name="checkboxBtn" value="<%=tmProfile.getId()%>" onclick="buttonManagement();">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=TMProfileComparator.NAME%>" width="11%" 
                filter="<%=TMProfileConstants.FILTER_NAME %>" filterValue="<%=filterNameValue%>">
            <%
                if (userPermissions.getPermissionFor(Permission.TMP_EDIT))
                    out.print("<a href='javascript:void(0)' onclick='modifyTMProfile(" + tmProfile.getId() + ");'>" + tmProfile.getName() + "</a>");
                else
                    out.print(tmProfile.getName());
            %>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=TMProfileComparator.DESCRIPTION%>" width="14%">
                <% out.print(tmProfile.getDescription() == null ? "" : tmProfile.getDescription()); %>
            </amb:column>
            <amb:column label="lb_srx" sortBy="<%=TMProfileComparator.SRX%>" width="11%">
                <%
                SegmentationRuleFile ruleFile = ServerProxy.getSegmentationRuleFilePersistenceManager().getSegmentationRuleFileByTmpid(String.valueOf(tmProfile.getId()));
                out.print(ruleFile != null? ruleFile.getName() : "Default");
                %>
            </amb:column>
            <amb:column label="lb_leverage_match_threshold2" sortBy="<%=TMProfileComparator.LEVERAGE_MATCH_THRESHOLD%>" width="11%">
                <% out.print(tmProfile.getFuzzyMatchThreshold()); %>
            </amb:column>
            <amb:column label="msg_save_to_project_tm" sortBy="<%=TMProfileComparator.STORAGE_TM%>" width="11%"
                filter="<%=TMProfileConstants.FILTER_STORAGE_TM %>" filterValue="<%=filterStorageTmValue%>">
                <% out.print(ServerProxy.getProjectHandler().getProjectTMById(tmProfile.getProjectTmIdForSave(), false).getName()); %>
            </amb:column>
            <amb:column label="msg_lev_project_tm" sortBy="<%=TMProfileComparator.REFERENCE_TMS%>" width="11%">
                <% out.print(tmProfile.getProjectTMNamesToLeverageFrom()); %>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=TMProfileComparator.ASC_COMPANY%>" 
                filter="<%=TMProfileConstants.FILTER_COMPANY_NAME%>" filterValue="<%=filterCompanyValue%>">
                <%
                    long companyId = ServerProxy.getProjectHandler().getProjectTMById(tmProfile.getProjectTmIdForSave(), false).getCompanyId();
                    String companyName = CompanyWrapper.getCompanyNameById(companyId);
                    out.print(companyName);
                %>
            </amb:column>
            <% } %>
          </amb:table>
        </td>
    </tr>
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="tmProfiles" key="<%=TMProfileConstants.TMP_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
        </td>
    </tr>
    <TR>
        <TD>
        <DIV ID="DownloadButtonLayer" ALIGN="left" STYLE="visibility: visible">
        <P>
        <amb:permission name="<%=Permission.TMP_NEW%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." id="idNewBtn" onclick="newTmProfile();" />
        </amb:permission>
        <amb:permission name="<%=Permission.TMP_REMOVE%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>..." id="idRemoveBtn" name="removeBtn" onclick="removeTmProfile();" disabled>
        </amb:permission>
        <amb:permission name="<%=Permission.TMP_NEW%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_tda_edit")%>..." id="idTdaEditBtn" name="tdaEditBtn" onclick="editTdaOptions();" disabled>
        </amb:permission>
        </DIV>
        </TD>
    </TR>
</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
