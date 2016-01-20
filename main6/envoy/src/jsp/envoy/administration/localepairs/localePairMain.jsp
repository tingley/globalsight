<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants, 
      com.globalsight.everest.util.comparator.LocalePairComparator,
      com.globalsight.everest.company.CompanyWrapper,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.servlet.util.SessionManager,
      java.lang.Boolean,
      java.util.ArrayList,
      java.util.Locale,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="newLocale" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="remove" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="lps" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="export" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="localePairsimport" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String confirmRemove = bundle.getString("msg_confirm_locale_pair_removal");
 
    String newURL = new1.getPageURL() + "&action=" + LocalePairConstants.CREATE;
    String newLocaleURL = newLocale.getPageURL() + "&action=" + LocalePairConstants.CREATE;
    String removeURL = remove.getPageURL() + "&action=" + LocalePairConstants.REMOVE;
    String exportURL = export.getPageURL() + "&action=" + LocalePairConstants.EXPORT;
    String importsUrl = localePairsimport.getPageURL() + "&action=import";
    String filterURL = self.getPageURL() + "&action=filter";
    String title = bundle.getString("lb_locale_pairs");
    String helperText = bundle.getString("helper_text_locale_pair");

    String deps = (String)sessionMgr.getAttribute(LocalePairConstants.DEPENDENCIES);
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

    String companyFilterValue = (String) sessionMgr.getAttribute(LocalePairConstants.FILTER_COMPANY);
    if (companyFilterValue == null || companyFilterValue.trim().length() == 0)
    {
        companyFilterValue = "";
    }
    String sourceFilterValue = (String) sessionMgr.getAttribute(LocalePairConstants.FILTER_SOURCELOCALE);
    if (sourceFilterValue == null || sourceFilterValue.trim().length() == 0)
    {
    	sourceFilterValue = "";
    }
    String targetFilterValue = (String) sessionMgr.getAttribute(LocalePairConstants.FILTER_TARGETLOCALE);
    if (targetFilterValue == null || targetFilterValue.trim().length() == 0)
    {
    	targetFilterValue = "";
    }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">

var needWarning = false;
var objectName = "";
var guideNode = "localePairs";
var helpFile = "<%=bundle.getString("help_locale_pairs_main_screen")%>";

function newLocalePair()
{
    lpForm.action = "<%=newURL%>";
    lpForm.submit();
}

function addLocale()
{
    lpForm.action = "<%=newLocaleURL%>";
    lpForm.submit();
}

function removeLocalePair()
{
    if (!confirm('<%=confirmRemove%>'))
    {
        return false;
    }
    // Actually there is only one (remove one by one)
    var value = findSelectedLocalePair();
    lpForm.action = "<%=removeURL%>" + "&id=" + value;
    lpForm.submit();
}

function exportLocalePair(){
	var value = findSelectedLocalePair();
	if(value.length == 0){
		alert("Please select at least one locale pair!");
		return;
	}
	lpForm.action = "<%=exportURL%>" + "&id=" + value;
	lpForm.submit();
}

function importLocalePair(){
	lpForm.action = "<%=importsUrl%>";
	lpForm.submit();
}
// Find selected locale pair Ids
function findSelectedLocalePair()
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

function buttonManagement()
{
    var count = $("input[name='checkboxBtn']:checked").length;
    if (count != 0)
    {
        $("#removeBtn").attr("disabled", false);
    }
    else
    {
        $("#removeBtn").attr("disabled", true);
    }
}

function handleSelectAll() {
    var ch = $("#selectAll").attr("checked");
    if (ch == "checked")
    {
        $("[name='checkboxBtn']").attr("checked", true);
    }
    else
    {
        $("[name='checkboxBtn']").attr("checked", false);
    }

    buttonManagement();
}

function filterItems(e){
	e = e ? e : window.event;
	var keyCode = e.which ? e.which : e.keyCode;
	if (keyCode == 13){
		lpForm.action = "<%=filterURL%>";
		lpForm.submit();
	}
}
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement(LocalePairConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>

<form name="lpForm" method="post">
    <table name="test" border=0 width="1024px">
      <tr><td align="center"></td></tr>
    </table>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="lps" key="<%=LocalePairConstants.LP_KEY%>" pageUrl="self" />
          </td>
        </tr>
        <tr>
            <td>
                <amb:table bean="lps" id="lp" key="<%=LocalePairConstants.LP_KEY%>" 
                    dataClass="com.globalsight.everest.foundation.LocalePair" 
                    pageUrl="self" hasFilter="true"
                    emptyTableMsg="msg_no_locale_pairs">
                    <amb:column label="checkbox" width="2%">
                        <input type="checkbox" name="checkboxBtn" id="checkboxBtn" onClick="buttonManagement()" value="<%=lp.getId()%>">
                    </amb:column>
                    <amb:column label="lb_source_locale" width="22%" sortBy="<%=LocalePairComparator.SRC%>" filter="<%=LocalePairConstants.FILTER_SOURCELOCALE%>" filterValue="<%=sourceFilterValue%>">
                    	<%= lp.getSource().getDisplayName(uiLocale) %>
                    </amb:column>
                    <amb:column label="lb_target_locale" width="23%" sortBy="<%=LocalePairComparator.TARG%>" filter="<%=LocalePairConstants.FILTER_TARGETLOCALE%>" filterValue="<%=targetFilterValue%>">
                        <%= lp.getTarget().getDisplayName(uiLocale) %>
                    </amb:column>
                    <% if (isSuperAdmin) { %>
                    <amb:column label="lb_company_name" width="120" sortBy="<%=LocalePairComparator.ASC_COMPANY%>"
                        filter="<%=LocalePairConstants.FILTER_COMPANY%>" filterValue="<%=companyFilterValue%>">
                        <%=CompanyWrapper.getCompanyNameById(lp.getCompanyId())%>
                    </amb:column>
                    <% } %>
                    <amb:column label="" width="55%" sortBy="">&nbsp;</amb:column>
                </amb:table>
            </td>
        </tr>
        <tr valign="top">
            <td align="right">
                <amb:tableNav bean="lps" key="<%=LocalePairConstants.LP_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
            </td>
        </tr>
        <tr>
            <td style="padding-top:5px" align="left">
                <amb:permission name="<%=Permission.LOCALE_NEW%>" >
                    <input type="button" value="<%=bundle.getString("lb_new_locale")%>" 
                    name="newLocale" id="newLocale" onClick="addLocale();">
                </amb:permission>
                <amb:permission name="<%=Permission.LOCALE_PAIRS_REMOVE%>" >
                    <input type="button" value="<%=bundle.getString("lb_remove")%>" 
                    name="removeBtn" id="removeBtn" disabled onClick="removeLocalePair();">
                </amb:permission>
                <amb:permission name="<%=Permission.LOCALE_PAIRS_NEW%>" >
                    <input type="button" value="<%=bundle.getString("lb_new")%>..." 
                    name="newBtn" id="newBtn" onClick="newLocalePair();">
                </amb:permission>
                   <amb:permission name="<%=Permission.LOCALE_PAIRS_EXPORT%>" >
                    <input type="button" value="<%=bundle.getString("lb_export")%>..." 
                    name="exportBtn" id="exportBtn" onClick="exportLocalePair();">
                </amb:permission>
                   <amb:permission name="<%=Permission.LOCALE_PAIRS_IMPORT%>" >
                    <input type="button" value="<%=bundle.getString("lb_import")%>..." 
                    name="importBtn" id="importBtn" onClick="importLocalePair();">
                </amb:permission>
            </td>
        </TR>
    </TABLE>
</FORM>
</DIV>
</BODY>
