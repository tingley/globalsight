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
         java.util.ArrayList, java.util.Locale, java.util.ResourceBundle,
         java.text.NumberFormat"
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
    String confirmRemove = bundle.getString("jsmsg_remove");

    String selfURL = self.getPageURL();
    String newURL = new1.getPageURL() + "&action=" + RateConstants.CREATE;
    String editURL = edit.getPageURL() + "&action=" + RateConstants.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + RateConstants.REMOVE;
    
    //Data
    String preReqData = (String)request.getAttribute("preReqData");
    
    NumberFormat numberFormat = NumberFormat.getInstance();
    numberFormat.setGroupingUsed(false);
    numberFormat.setMaximumFractionDigits(5);

    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String companyNameFilterValue = (String) sessionMgr.getAttribute(RateConstants.FILTER_RATE_COMPANY);
    if (companyNameFilterValue == null || companyNameFilterValue.trim().length() == 0)
    {
        companyNameFilterValue = "";
    }

    String rateNameFilterValue = (String) sessionMgr.getAttribute(RateConstants.FILTER_RATE_NAME);
    if (rateNameFilterValue == null || rateNameFilterValue.trim().length() == 0)
    {
        rateNameFilterValue = "";
    }

    String sourceLocaleFilterValue = (String) sessionMgr.getAttribute(RateConstants.FILTER_RATE_SOURCE_LOCALE);
    if (sourceLocaleFilterValue == null || sourceLocaleFilterValue.trim().length() == 0)
    {
        sourceLocaleFilterValue = "";
    }

    String targetLocaleFilterValue = (String) sessionMgr.getAttribute(RateConstants.FILTER_RATE_TARGET_LOCALE);
    if (targetLocaleFilterValue == null || targetLocaleFilterValue.trim().length() == 0)
    {
        targetLocaleFilterValue = "";
    }

%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "rate";
var helpFile = "<%=bundle.getString("help_rate_main_screen")%>";

function handleSelectAll()
{
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

function buttonManagement()
{
    var count = $("input[name='checkboxBtn']:checked").length;
    if (count == 1)
    {
        $("#removeBtn").attr("disabled", false);
    }
    else
    {
        $("#removeBtn").attr("disabled", true);
    }
}

function removeRate()
{
    if (!confirm('<%=confirmRemove%>'))
    {
        return false;
    }
    // Actually there is only one (remove one by one)
    var value = findSelectedRate();
    rateForm.action = "<%=removeURL%>&<%=RateConstants.RATE_ID%>=" + value;
    rateForm.submit();
}

function editRate(rateId)
{
    rateForm.action = "<%=editURL%>&<%=RateConstants.RATE_ID%>=" + rateId;
    rateForm.submit();
}

function findSelectedRate()
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

function newRate()
{
<%  if (preReqData != null)
    {
%>
        alert("<%=preReqData%>");
        return;
<%
    }
%>
    rateForm.action = "<%=newURL%>";
    rateForm.submit();
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
        rateForm.action = "<%=selfURL%>";
        rateForm.submit();
    }
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="rateForm" id="rateForm" method="post">
    <table name="dummy" border=0 width="1024px">
      <tr><td align="center"></td></tr>
    </table>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="rates" key="<%=RateConstants.RATE_KEY%>" pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="rates" id="rate"
                     key="<%=RateConstants.RATE_KEY%>"
                     dataClass="com.globalsight.everest.costing.Rate" pageUrl="self"
                     emptyTableMsg="msg_no_rates" hasFilter="true" >
                <amb:column label="checkbox" width="2%">
                    <input type="checkbox" name="checkboxBtn" id="checkboxBtn" value="<%=rate.getId()%>" onclick='buttonManagement()'>
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=RateComparator.NAME%>" width="10%" 
                  filter="<%=RateConstants.FILTER_RATE_NAME%>" filterValue="<%=rateNameFilterValue%>" >
                  <amb:permission name="<%=Permission.RATES_EDIT%>" ><a title="Edit Rate" onclick="editRate('<%=rate.getId()%>');" href="javascript:void(0);"></amb:permission>
                      <%=rate.getName()%>
                  <amb:permission name="<%=Permission.RATES_EDIT%>" ></a></amb:permission>
                </amb:column>
                <amb:column label="lb_activity_type"  sortBy="<%=RateComparator.ACTIVITY%>" width="10%">
                    <%= rate.getActivity().getDisplayName()%>
                </amb:column>
                <amb:column label="lb_source_locale" sortBy="<%=RateComparator.SOURCE_LOCALE%>" 
                    filter="<%=RateConstants.FILTER_RATE_SOURCE_LOCALE %>" filterValue="<%=sourceLocaleFilterValue %>" width="15%">
                    <%=rate.getLocalePair().getSource().getDisplayName(uiLocale) %>
                </amb:column>
                <amb:column label="lb_target_locale" sortBy="<%=RateComparator.TARGET_LOCALE%>" 
                    filter="<%=RateConstants.FILTER_RATE_TARGET_LOCALE %>" filterValue="<%=targetLocaleFilterValue %>" width="15%">
                    <%=rate.getLocalePair().getTarget().getDisplayName(uiLocale)%>
                </amb:column>
                <amb:column label="lb_currency" sortBy="<%=RateComparator.CURRENCY%>" width="10%">
                    <%= rate.getCurrency().getDisplayName(uiLocale)%>
                </amb:column>
                <amb:column label="lb_rate_type" sortBy="<%=RateComparator.RATE_TYPE%>" width="10%">
                    <%
                        Integer type = rate.getRateType();
                        switch (type.intValue())
                        {
                            case 1:
                                out.print(bundle.getString("lb_rate_type_1"));
                                break;
                            case 2:
                                out.print(bundle.getString("lb_rate_type_2"));
                                break;
                            case 3:
                                out.print(bundle.getString("lb_rate_type_3"));
                                break;
                            case 4:
                                out.print(bundle.getString("lb_rate_type_4"));
                                break;
                            case 5:
                                out.print(bundle.getString("lb_rate_type_5"));
                                break;
                        }
                    %>
                </amb:column>
                <amb:column label="lb_rate">
                    <%
                        Integer type = rate.getRateType();
                        switch (type.intValue())
                        {
                            case 1:
                            case 2:
                            case 3:
                                out.print(rate.getUnitRate());
                                break;
                            case 4:
                                // Word count details
                                StringBuilder wc = new StringBuilder();
                                wc.append(bundle.getString("lb_in_context")).append(" : ").append(numberFormat.format(rate.getInContextMatchRate())).append("<br/>");
                                wc.append(bundle.getString("lb_repetition_word_cnt")).append(" : ").append(numberFormat.format(rate.getRepetitionRate())).append("<br/>");
                                wc.append(bundle.getString("lb_100")).append(" : ").append(numberFormat.format(rate.getSegmentTmRate())).append("<br/>");
                                wc.append(bundle.getString("lb_95")).append(" : ").append(numberFormat.format(rate.getHiFuzzyMatchRate())).append("<br/>");
                                wc.append(bundle.getString("lb_85")).append(" : ").append(numberFormat.format(rate.getMedHiFuzzyMatchRate())).append("<br/>");
                                wc.append(bundle.getString("lb_75")).append(" : ").append(numberFormat.format(rate.getMedFuzzyMatchRate())).append("<br/>");
                                wc.append(bundle.getString("lb_50")).append(" : ").append(numberFormat.format(rate.getLowFuzzyMatchRate())).append("<br/>");
                                wc.append(bundle.getString("lb_no_match")).append(" : ").append(numberFormat.format(rate.getNoMatchRate()));
                                out.print(wc.toString());
                                break;
                            case 5:
                                StringBuilder wc2 = new StringBuilder();
                                wc2.append(bundle.getString("lb_in_context")).append(" : ").append(numberFormat.format(rate.getInContextMatchRate())).append(" (").append(numberFormat.format(rate.getInContextMatchRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_repetition_word_cnt")).append(" : ").append(numberFormat.format(rate.getRepetitionRate())).append(" (").append(numberFormat.format(rate.getRepetitionRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_100")).append(" : ").append(numberFormat.format(rate.getSegmentTmRate())).append(" (").append(numberFormat.format(rate.getSegmentTmRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_95")).append(" : ").append(numberFormat.format(rate.getHiFuzzyMatchRate())).append(" (").append(numberFormat.format(rate.getHiFuzzyMatchRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_85")).append(" : ").append(numberFormat.format(rate.getMedHiFuzzyMatchRate())).append(" (").append(numberFormat.format(rate.getMedHiFuzzyMatchRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_75")).append(" : ").append(numberFormat.format(rate.getMedFuzzyMatchRate())).append(" (").append(numberFormat.format(rate.getMedFuzzyMatchRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_50")).append(" : ").append(numberFormat.format(rate.getLowFuzzyMatchRate())).append(" (").append(numberFormat.format(rate.getLowFuzzyMatchRatePer())).append("%)<br/>");
                                wc2.append(bundle.getString("lb_rate_base")).append(" : ").append(numberFormat.format(rate.getNoMatchRate()));
                                out.print(wc2.toString());
                                break;
                        }
                    %>
                </amb:column>
                 <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" width="150" sortBy="<%=RateComparator.ASC_COMPANY%>" 
                    filter="<%=RateConstants.FILTER_RATE_COMPANY%>" filterValue="<%=companyNameFilterValue%>" >
                    <%=CompanyWrapper.getCompanyNameById(rate.getActivity().getCompanyId())%>
                </amb:column>
                 <% } %>
              </amb:table>
            </td>
        </tr>
        <tr valign="top">
            <td align="right">
                <amb:tableNav bean="rates" key="<%=RateConstants.RATE_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
            </td>
        </tr>
        <tr>
            <td style="padding-top:5px" align="left">
              <amb:permission name="<%=Permission.RATES_EDIT%>" >
                <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" name="removeBtn" id="removeBtn" onClick="removeRate();" disabled>
              </amb:permission>
              <amb:permission name="<%=Permission.RATES_NEW%>" >
                <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." name="newBtn" id="newBtn" onClick="newRate();">
              </amb:permission>
            </td>
        </TR>
</TABLE>
</FORM>
</BODY>
</HTML>
