<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
             com.globalsight.everest.webapp.WebAppConstants,
             com.globalsight.util.GlobalSightLocale,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
             com.globalsight.everest.jobhandler.Job,com.globalsight.everest.jobhandler.JobSearchParameters,
             com.globalsight.everest.servlet.util.ServerProxy,
             com.globalsight.everest.projecthandler.Project,
             com.globalsight.everest.company.CompanyWrapper,
             com.globalsight.everest.costing.Currency,
       		 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
       		 java.util.Locale,
       		 java.util.ResourceBundle,
       		 java.text.MessageFormat"
          session="true"
%>
<%  String EMEA = CompanyWrapper.getCurrentCompanyName();
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
	{
    	uiLocale = Locale.US;
	}

    String formAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS"
        + "&action=" + ReportConstants.ACTION_GENERATE_SUMMARY_PERCENT;

    String lb_report_startDate = bundle.getString("lb_report_startDate");
    String lb_report_endDate = bundle.getString("lb_report_endDate");
    String lb_report_project = bundle.getString("lb_project");
    String lb_report_status = bundle.getString("lb_status");
    String lb_report_targetLang = bundle.getString("lb_target_language");
    String msg_NotNull = bundle.getString("msg_validate_null");
    String msg_startDate_NotNull = MessageFormat.format(msg_NotNull, lb_report_startDate);
    String msg_endDate_NotNull = MessageFormat.format(msg_NotNull, lb_report_endDate);
    String msg_project_NotNull = MessageFormat.format(msg_NotNull, lb_report_project);
    String msg_status_NotNull = MessageFormat.format(msg_NotNull, lb_report_status);
    String msg_targetLang_NotNull = MessageFormat.format(msg_NotNull, lb_report_targetLang);
    
    List<Project> projectList = 
            (ArrayList<Project>) request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
            request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
%>
<html>
<!-- This JSP is: /envoy/administration/reports/summaryReportWebForm.jsp -->
<head>
<title><%= EMEA%> <%=bundle.getString("report_summary_web_form")%></title>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	$("#startDate").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#endDate").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#endDate").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#startDate").datepicker( "option", "maxDate", selectedDate );
		}
	});
});
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY">
<script type="text/javascript">
// If user selected "now", then blank out the preceeding numeric field.
function checkNow(field, text)
{
    if (field.options[1].selected)
        text.value = "";
}

function isInteger(value)
{
    if (value == "") return true;
    return (parseInt(value) == value);
}

function validateForm()
{
    if ($("#startDate").val() == "")
        return ('<%=msg_startDate_NotNull%>');
    if ($("#endDate").val() == "")
        return ('<%=msg_endDate_NotNull%>');
    if ($("#projectId option:selected").length == 0)
    	return ('<%=msg_project_NotNull%>');
    if ($("#status option:selected").length == 0)
    	return ('<%=msg_status_NotNull%>');
    if ($("#targetLocalesList option:selected").length == 0)
    	return ('<%=msg_targetLang_NotNull%>');
    
    return "";
}

function submitForm()
{
	var msg = validateForm();
	if (msg != "")
   	{
    	alert(msg);
    	return;
   	}
   	else
    	searchForm.submit();
}

</script>
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%= EMEA%> <%=bundle.getString("report_summary_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="<%=formAction%>">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=lb_report_startDate%>:</td>
        <td class="standardText" VALIGN="BOTTOM"><input type="text" id="startDate" name="startDate"></td>
    </tr>

    <tr>
        <td class="standardText"><%=lb_report_endDate%>:</td>
        <td class="standardText" VALIGN="BOTTOM"><input type="text" id="endDate" name="endDate"></td>
    </tr>

    <tr>
        <td class="standardText"><%=lb_report_project%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="projectId" id="projectId" multiple size=4>
            <OPTION value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for (Project p : projectList)
            {
%>          <option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=lb_report_status%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="status" id="status" MULTIPLE size=4>
            <option value="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option value='<%=Job.READY_TO_BE_DISPATCHED%>'><%= bundle.getString("lb_ready") %></option>
            <option value='<%=Job.DISPATCHED%>'><%= bundle.getString("lb_inprogress") %></option>
            <option value='<%=Job.LOCALIZED%>'><%= bundle.getString("lb_localized") %></option>
            <option value='<%=Job.EXPORTED%>'><%= bundle.getString("lb_exported") %></option>
            <option value='<%=Job.EXPORT_FAIL%>'><%= bundle.getString("lb_exported_failed") %></option>
            <option value='<%=Job.ARCHIVED%>'><%= bundle.getString("lb_archived") %></option>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=lb_report_targetLang%>: &nbsp;&nbsp;&nbsp;</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLocalesList" id="targetLocalesList" multiple size=4>
            <OPTION value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for (GlobalSightLocale gsl : targetLocales)
            {
%>          <option VALUE="<%=gsl.getId()%>"><%=gsl.getDisplayName(uiLocale)%></option>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_currency")%>:</td>
        <td>
        <SELECT NAME="currency" id="currency">
<%
        Collection<?> currencies = ServerProxy.getCostingEngine().getCurrencies();
        Currency pivotCurrency = ServerProxy.getCostingEngine().getPivotCurrency();

        ArrayList<String> labeledCurrencies = new ArrayList<String>();
        ArrayList<String> valueCurrencies = new ArrayList<String>();
        Iterator iter = currencies.iterator();
        while ( iter.hasNext() ) 
        {
            Currency c = (Currency) iter.next();
            if (!labeledCurrencies.contains(c.getDisplayName())) 
            {
                labeledCurrencies.add(c.getDisplayName(uiLocale));
            	valueCurrencies.add(c.getDisplayName());
            }
        }
		
        for (int j = 0; j < labeledCurrencies.size(); j++)
        {
            String currencyLabel = labeledCurrencies.get(j);
            String currencyText = valueCurrencies.get(j);
%>
            <OPTION VALUE="<%=currencyText%>"><%=currencyLabel%> <%  }  %>
        </SELECT>
        </td>
    </tr>

    <tr>
        <td><input type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
        <td><input type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></td>
    </tr>
</table>
</form>
<body>
</HTML>
