<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch,
            com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer,
            com.globalsight.cxe.entity.cms.teamsite.store.BackingStore,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.webapp.pagehandler.PageHandler, 
            com.globalsight.everest.webapp.pagehandler.administration.config.teamsite.TeamSiteBranchMainHandler, 
            com.globalsight.everest.util.comparator.TeamSiteBranchComparator,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.util.GlobalSightLocale,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="tsProfiles" scope="request"
 class="java.util.ArrayList" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String newURL = new1.getPageURL();
String removeURL = remove.getPageURL() + "&action=remove";
String title= bundle.getString("lb_teamsite_branches");
String helperText= bundle.getString("helper_text_teamsite_main");
String preReqData = (String)request.getAttribute("preReqData");

boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "teamsiteBranches";
var helpFile = "<%=bundle.getString("help_teamsite_new_branch")%>";

function submitForm(button)
{
    if (button == "New")
    {
<%
        if (preReqData != null)
        {
%>
            alert("<%=preReqData%>");
            return;
<%
        }
%>
        tsForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(tsForm.radioBtn);
        tsForm.action = "<%=removeURL%>" + "&id=" + value;
    }
    tsForm.submit();
    return;

}

function enableButtons()
{
    tsForm.removeBtn.disabled = false;
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="tsForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="tsProfiles"
                 key="<%=TeamSiteBranchMainHandler.TS_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="tsProfiles" id="tsProfile"
                     key="<%=TeamSiteBranchMainHandler.TS_KEY%>"
                     dataClass="com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch"
                     pageUrl="self"
                     emptyTableMsg="msg_no_ts_profiles" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" value="<%=tsProfile.getId()%>"
                        onclick="enableButtons()" >
                </amb:column>
                <amb:column label="lb_source_branch"
                    sortBy="<%=TeamSiteBranchComparator.BRANCH%>"
                    width="250px">
                    <%= tsProfile.toString() %>
                </amb:column>
                <amb:column label="lb_target_locale"
                    sortBy="<%=TeamSiteBranchComparator.TARG_LOCALE%>"
                    width="150">
                <%
                    GlobalSightLocale targ_locale = ServerProxy.getLocaleManager().
                                        getLocaleById(tsProfile.getBranchLanguage());
                    out.print(targ_locale.getDisplayLanguage() + " / " +
                              targ_locale.getDisplayCountry());
                %>
                </amb:column>
                <amb:column label="lb_target_branch"
                    sortBy="<%=TeamSiteBranchComparator.TARG_BRANCH%>"
                    width="150">
                    <% out.print(tsProfile.getBranchTarget()); %>
                </amb:column>
                <amb:column label="lb_teamsite_ts_server"
                    sortBy="<%=TeamSiteBranchComparator.SERVER%>"
                    width="100">
                <%
                    TeamSiteServer ts = ServerProxy.getTeamSiteServerPersistenceManager()
                                    .readTeamSiteServer(tsProfile.getTeamSiteServerId());
                    out.print(ts.getName());
                %>
                </amb:column>
                <amb:column label="lb_teamsite_content_store"
                    sortBy="<%=TeamSiteBranchComparator.STORE%>"
                    width="150">
                <%
                    BackingStore store = ServerProxy.getTeamSiteServerPersistenceManager()
                                        .readBackingStore(tsProfile.getTeamSiteStoreId());
                    out.print(store.getName());
                %>
                </amb:column>
                
                <% if (isSuperAdmin) { %>
                    <amb:column label="lb_company_name"
                         sortBy="<%=TeamSiteBranchComparator.COMPANY%>"
                         width="150">
                    <%
                        TeamSiteServer ts = ServerProxy.getTeamSiteServerPersistenceManager()
                                        .readTeamSiteServer(tsProfile.getTeamSiteServerId());
                            out.print(CompanyWrapper.getCompanyNameById(ts.getCompanyId()));
                    %>
                </amb:column>
                <% } %>
                
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.TEAMSITE_PROFILES_REMOVE%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
            name="removeBtn" disabled onClick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.TEAMSITE_PROFILES_NEW%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
            onClick="submitForm('New');">
    </amb:permission>
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>
