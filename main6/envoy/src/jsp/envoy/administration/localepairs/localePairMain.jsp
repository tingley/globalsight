<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants, 
      com.globalsight.everest.util.comparator.LocalePairComparator,
      com.globalsight.everest.company.CompanyWrapper,
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
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String confirmRemove = bundle.getString("msg_confirm_locale_pair_removal");
 
    String newURL = new1.getPageURL() + "&action=" + LocalePairConstants.CREATE;
    String newLocaleURL = newLocale.getPageURL() + "&action=" + LocalePairConstants.CREATE;
    String removeURL = remove.getPageURL() + "&action=" + LocalePairConstants.REMOVE;
    String title = bundle.getString("lb_locale_pairs");
    String helperText = bundle.getString("helper_text_locale_pair");

    String deps = (String)sessionMgr.getAttribute(LocalePairConstants.DEPENDENCIES);
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">

    var needWarning = false;
    var objectName = "";
    var guideNode = "localePairs";
    var helpFile = "<%=bundle.getString("help_locale_pairs_main_screen")%>";

function submitForm(button)
{
    if (button == "New")
    {
        lpForm.action = "<%=newURL%>";
    }
    else if (button == "newLocale")
    {
        lpForm.action = "<%=newLocaleURL%>"
    }
    else 
    {
        if (!confirm('<%=confirmRemove%>')) return false;
        value = getRadioValue(lpForm.radioBtn);
        lpForm.action = "<%=removeURL%>" + "&id=" + value;
    }
    lpForm.submit();
    return;
    
}

function enableRemove()
{
    if (lpForm.removeBtn)
        lpForm.removeBtn.disabled = false;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
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
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="lps" key="<%=LocalePairConstants.LP_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="lps" id="lp"
                     key="<%=LocalePairConstants.LP_KEY%>"
                     dataClass="com.globalsight.everest.foundation.LocalePair" pageUrl="self"
                     emptyTableMsg="msg_no_locale_pairs" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" onclick="enableRemove()"
                        value="<%=lp.getId()%>">
                </amb:column>
                <amb:column label="lb_source_locale" width="250"
                     sortBy="<%=LocalePairComparator.SRC%>">
                    <%= lp.getSource().getDisplayName(uiLocale) %>
                </amb:column>
                <amb:column label="lb_target_locale" width="250px"
                     sortBy="<%=LocalePairComparator.TARG%>">
                    <%= lp.getTarget().getDisplayName(uiLocale) %>
                </amb:column>
                <% if (isSuperAdmin) { %>
    					  <amb:column label="lb_company_name" width="120" 
    					       sortBy="<%=LocalePairComparator.ASC_COMPANY%>">
				 <%=CompanyWrapper.getCompanyNameById(lp.getCompanyId())%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.LOCALE_NEW%>" >
        <input type="button" value="<%=bundle.getString("lb_new_locale")%>"
            name="newLocale" onClick="submitForm('newLocale');">
    </amb:permission>
    <amb:permission name="<%=Permission.LOCALE_PAIRS_REMOVE%>" >
        <input type="button" value="<%=bundle.getString("lb_remove")%>"
            name="removeBtn" disabled onClick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.LOCALE_PAIRS_NEW%>" >
        <input type="button" value="<%=bundle.getString("lb_new")%>..."     
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
