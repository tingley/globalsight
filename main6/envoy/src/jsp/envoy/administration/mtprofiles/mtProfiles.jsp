<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.util.comparator.MTProfileComparator,
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
 <jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="mtpImport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="mtProfiles" class="java.util.ArrayList" scope="request"/>

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String title= bundle.getString("lb_remote_service");
    String helperText = bundle.getString("helper_text_mt_profile_main");
    String confirm = bundle.getString("mt_remove_confirm");
    String newButton = bundle.getString("lb_new");
    String removeButton = bundle.getString("lb_remove");

    //Urls of the links on this page
    String action = MTProfileConstants.ACTION;
    String selfUrl = self.getPageURL();
    String newUrl = new1.getPageURL() + "&" + action + "=" + MTProfileConstants.NEW_ACTION;
    String modifyUrl = modify.getPageURL();
    String removeUrl = remove.getPageURL() + "&" + action + "=" + MTProfileConstants.REMOVE_ACTION;
	String exportUrl = export.getPageURL() + "&" + action + "=" + MTProfileConstants.EXPORT_ACTION;
	String mtpImportUrl = mtpImport.getPageURL() + "&" + action + "=" + MTProfileConstants.IMPORT_ACTION;

	boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

    String filterNameValue = (String) sessionManager.getAttribute(MTProfileConstants.FILTER_NAME);
    if (filterNameValue == null || filterNameValue.trim().length() == 0){
        filterNameValue = "";
    }
    String filterCompanyValue = (String) sessionManager.getAttribute(MTProfileConstants.FILTER_COMPANY_NAME);
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
String.prototype.replaceAll = stringReplaceAll;

function stringReplaceAll(AFindText,ARepText){
raRegExp = new RegExp(AFindText,"g");
return this.replace(raRegExp,ARepText)
}

var needWarning = false;
var guideNode = "mtProfiles";
var helpFile = "<%=bundle.getString("help_mtprofile")%>";
var msgAlert="<%=request.getAttribute("exception")%>";
var confirmRemove="<%=confirm%>";
$(function(){
	if(msgAlert!="null"){
		var msg=msgAlert.replaceAll(",","\n");
		alert(msg);
	}
})

function handleSelectAll()
{
    var selectAll = $("#selectAll").is(":checked");
    $("input[name='checkboxBtn']").each(function()
    {
        if (selectAll == true){
            $(this).attr("checked", true);
        } else {
            $(this).attr("checked", false);
        }
    });

    buttonManagement();
}

function buttonManagement()
{
    var count = $("input[name='checkboxBtn']:checked").length;

    // Only one TM profile is selected
    if (count == 1)
    {
        if (mtProfileForm.removeBtn)
        {
            mtProfileForm.removeBtn.disabled = false;
        }
    }
    // count == 0 or count > 1
    else
    {
        if (mtProfileForm.removeBtn)
        {
            mtProfileForm.removeBtn.disabled = true;
        }
    }
}

// Click "New" button
function newTmProfile()
{
    mtProfileForm.action = '<%=newUrl%>';
    mtProfileForm.submit();
}

// Edit
function modifyTMProfile(mtProfileId)
{
    mtProfileForm.action = "<%=modifyUrl%>&<%=MTProfileConstants.MT_PROFILE_ID%>=" + mtProfileId;
    mtProfileForm.submit();
}

// Click "Remove" button
function removeTmProfile()
{
    value = findSelectedTmProfiles();
    if (confirm(confirmRemove))
    {
        mtProfileForm.action = "<%=removeUrl%>&<%=MTProfileConstants.MT_PROFILE_ID%>=" + value;
        mtProfileForm.submit();
    } 
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

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	mtProfileForm.action = "<%=selfUrl%>";
    	mtProfileForm.submit();
    }
}

function exportTMProfile()
{
	var value = findSelectedMTP();
	if(value.length == 0)
	{
		alert("Please select at least one machine translation profile!");
		return;
	}
	mtProfileForm.action = "<%=exportUrl%>" + "&id=" + value;
	mtProfileForm.submit();
}

function importMTProfile()
{
	mtProfileForm.action = "<%=mtpImportUrl%>";
	mtProfileForm.submit();
}

//Find selected Machine Translation Profiles Ids
function findSelectedMTP()
{
    var ids = "";
    $("input[name='checkboxBtn']:checked").each(function ()
    {
        ids += $(this).val() + ",";
    });
    if (ids != "")
        ids = ids.substring(0, ids.length - 1);

    return ids;
}
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
  <div style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
       <table cellpadding="0" cellspacing="0" border="0">
           <tr>
               <td class="tableHeadingListOn">
                   <img src="/globalsight/images/tab_left_blue.gif" border="0" /> 
                   <a class="sortHREFWhite" href="<%=selfUrl%>"> 
                      <%=bundle.getString("lb_mt_profiles") %>
                    </a> 
                  <img src="/globalsight/images/tab_right_blue.gif" border="0" />
               </td>
               <%if (userPermissions.getPermissionFor(Permission.PS_VIEW)){%>
               <td width="2"></td>
               <td class="tableHeadingListOff">
                   <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                   <a class="sortHREFWhite" href="<%=perplexityServiceUrl%>"> 
                     <%=bundle.getString("lb_perplexity_services") %>
                   </a> 
                   <img src="/globalsight/images/tab_right_gray.gif" border="0" />
               </td>
               <%} %>
           </tr>
       </table>
   </div>
            
<FORM NAME="mtProfileForm" id="mtProfileForm" METHOD="POST">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px; margin-top: 10px;">
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="mtProfiles" key="<%=MTProfileConstants.MTP_KEY%>" pageUrl="self" />
        </td>
    </tr>
    <tr>
        <td>
          <amb:table bean="mtProfiles" id="mtProfile" key="<%=MTProfileConstants.MTP_KEY%>"
                 dataClass="com.globalsight.everest.projecthandler.MachineTranslationProfile"
                 pageUrl="self"
                 emptyTableMsg="msg_no_mt_profiles" hasFilter="true">
            <amb:column label="checkbox" width="2%">
                <input type="checkbox" id="checkboxBtn" name="checkboxBtn" value="<%=mtProfile.getId()%>" onclick="buttonManagement();">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=MTProfileComparator.NAME%>" width="11%" 
                filter="<%=MTProfileConstants.FILTER_NAME %>" filterValue="<%=filterNameValue%>">
            <%
                if (userPermissions.getPermissionFor(Permission.MTP_EDIT))
                    out.print("<a href='javascript:void(0)' onclick='modifyTMProfile(" + mtProfile.getId() + ");'>" + mtProfile.getMtProfileName() + "</a>");
                else
                    out.print(mtProfile.getMtProfileName());
            %>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=MTProfileComparator.DESCRIPTION%>" width="14%">
                <% out.print(mtProfile.getDescription() == null ? "" : mtProfile.getDescription()); %>
            </amb:column>
            
           
           
            <amb:column label="lb_tm_mt_engine" sortBy="<%=MTProfileComparator.MT_ENGINE%>" width="8%">
                <%
                    out.print(mtProfile.getMtEngine());
                %>
            </amb:column>
             <amb:column label="lb_tm_mt_threshold_level2" sortBy="<%=MTProfileComparator.MT_THRESHOLD%>" width="11%">
                <% out.print(mtProfile.getMtThreshold()); %>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=MTProfileComparator.ASC_COMPANY%>" 
                filter="<%=MTProfileConstants.FILTER_COMPANY_NAME%>" filterValue="<%=filterCompanyValue%>">
                <%
                    String companyName = CompanyWrapper.getCompanyNameById(mtProfile.getCompanyid());
                    out.print(companyName);
                %>
            </amb:column>
            <% } %>
          </amb:table>
        </td>
    </tr>
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="mtProfiles" key="<%=MTProfileConstants.MTP_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
        </td>
    </tr>
    <TR>
        <TD>
        <DIV ID="DownloadButtonLayer" ALIGN="left" STYLE="visibility: visible">
        <P>
       
        <amb:permission name="<%=Permission.MTP_REMOVE%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" id="idRemoveBtn" name="removeBtn" onclick="removeTmProfile();" disabled>
        </amb:permission>
        <amb:permission name="<%=Permission.MTP_NEW%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." id="idNewBtn" onclick="newTmProfile();" />
        </amb:permission>
        <amb:permission name="<%=Permission.MTP_EXPORT%>" >
	         <input type="button" value="<%=bundle.getString("lb_export")%>..." 
	         name="exportBtn" id="exportBtn" onClick="exportTMProfile();">
     	</amb:permission>
        <amb:permission name="<%=Permission.MTP_IMPORT%>" >
	         <input type="button" value="<%=bundle.getString("lb_import")%>..." 
	         name="importBtn" id="importBtn" onClick="importMTProfile();">
     	</amb:permission>
        </DIV>
        </TD>
    </TR>
</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
