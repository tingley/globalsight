<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.pagehandler.administration.costing.rate.RateConstants,
         com.globalsight.everest.util.comparator.RateComparator,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.company.CompanyWrapper,
         java.util.ArrayList, java.util.Locale, java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="edit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="remove" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="rates" scope="request" class="java.util.ArrayList" />


<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String title= bundle.getString("lb_rates");
    String helperText = bundle.getString("helper_text_rates");

    String newURL = new1.getPageURL() + "&action=" + RateConstants.CREATE;
    String editURL = edit.getPageURL() + "&action=" + RateConstants.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + RateConstants.REMOVE;
    
    //Data
    String fixed =  bundle.getString("lb_rate_type_1");
    String hourly =  bundle.getString("lb_rate_type_2");
    String pageRate =  bundle.getString("lb_rate_type_3");
    String wc =  bundle.getString("lb_rate_type_4");
    String preReqData = (String)request.getAttribute("preReqData");
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "rate";
    var helpFile = "<%=bundle.getString("help_rate_main_screen")%>";

function buttonCheck(radio)
{
    if (rateForm.edit)
        rateForm.edit.disabled = false;
    if (rateForm.remove)
        rateForm.remove.disabled = false;
}

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
        rateForm.action = "<%=newURL%>";
    }
    else
    {
        if (button == "Edit") {
            rateForm.action = "<%=editURL%>";
        } else if (button == "Remove") {
            rateForm.action = "<%=removeURL%>";
        }
    }
    rateForm.submit();
    return;

}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="rateForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="rates" key="<%=RateConstants.RATE_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="rates" id="rate"
                     key="<%=RateConstants.RATE_KEY%>"
                     dataClass="com.globalsight.everest.costing.Rate" pageUrl="self"
                     emptyTableMsg="msg_no_rates" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" value="<%=rate.getId()%>"
                        onclick='buttonCheck(this)'>
                </amb:column>
                <amb:column label="lb_name" 
                     sortBy="<%=RateComparator.NAME%>">
                    <%= rate.getName()%>
                </amb:column>
                <amb:column label="lb_activity_type" 
                     sortBy="<%=RateComparator.ACTIVITY%>">
                    <%= rate.getActivity().getDisplayName()%>
                </amb:column>
                <amb:column label="lb_locale_pair" 
                     sortBy="<%=RateComparator.LP%>">
                    <%= rate.getLocalePair().getSource().getDisplayName(uiLocale) +
                    " &#x2192; " + rate.getLocalePair().getTarget().getDisplayName(uiLocale) %>
                </amb:column>
                <amb:column label="lb_currency" 
                     sortBy="<%=RateComparator.CURRENCY%>">
                    <%= rate.getCurrency().getDisplayName(uiLocale)%>
                </amb:column>
                <amb:column label="lb_rate_type" 
                     sortBy="<%=RateComparator.RATE_TYPE%>">
                    <%
                        Integer type = rate.getRateType();
                        switch (type.intValue())
                        {
                            case 1:
                                out.print(fixed);
                                break;
                            case 2:
                                out.print(hourly);
                                break;
                            case 3:
                                out.print(pageRate);
                                break;
                            case 4:
                                out.print(wc);
                                break;
                            case 5:
                                out.print(bundle.getString("lb_rate_type_5"));
                                break;
                        }
                    %>
                </amb:column>
                 <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" width="120"
                     sortBy="<%=RateComparator.ASC_COMPANY%>">
                    <%=CompanyWrapper.getCompanyNameById(rate.getActivity().getCompanyId())%>
                </amb:column>
                 <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.RATES_EDIT%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" name="remove"
             onClick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.RATES_EDIT%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>" name="edit"
             onClick="submitForm('Edit');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.RATES_NEW%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." onClick="submitForm('New');">
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
