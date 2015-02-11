<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.permission.Permission,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.everest.company.CompanyWrapper,
                  com.globalsight.everest.costing.Currency,
                  java.util.Calendar,
                  java.util.GregorianCalendar,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%  String EMEA = CompanyWrapper.getCurrentCompanyName();
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);    
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;
    String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS";
    String formAction = basicAction + "&action=" + ReportConstants.GENERATE_REPORTS;

    List<Project> projectList = (ArrayList<Project>)
            request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
            request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
%>
<html>
<!-- This JSP is: /envoy/administration/reports/onlineJobsReportForIPTranslator.jsp  -->
<head>
<title><%= EMEA%> <%=bundle.getString("online_jobs_for_ip_translator_report")%></title>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript">
var inProgressStatus = "<%=ReportsData.STATUS_INPROGRESS%>";

$(document).ready(function(){
	$("#csf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#cef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#cef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#csf").datepicker( "option", "maxDate", selectedDate );
		}
	});
});

function validateForm()
{
	if (searchForm.projectId.value == "")
		return ('<%=bundle.getString("jsmsg_select_project")%>');
	if (searchForm.status.value == "")
		return ('<%=bundle.getString("jsmsg_select_status")%>');
	if (searchForm.targetLocalesList.value == "")
		return ('<%=bundle.getString("jsmsg_local_pair_select_target")%>');
    return "";
}

// The function for canceling the report.Online Jobs for IPTranslator
function fnDoCancel() {
  var jobIDArr = new Array();
  $.ajax({
    type: 'POST',
    dataType: 'json',
    url: '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
    data: {
      'inputJobIDS': jobIDArr.toString(),
      'reportType': $("input[name='reportType']").val()
    },
    success: function(data) {
      if (data != null && data.status == inProgressStatus) {
        if (confirm("<%=bundle.getString("msg_cancel_report")%>")) {
        	$.ajax({
                type: 'POST',
                dataType: 'json',
                url: '<%=basicAction + "&action=" + ReportConstants.ACTION_CANCEL_REPORTS%>',
                data: {
                  'inputJobIDS': jobIDArr.toString(),
                  'reportType': $("input[name='reportType']").val()
                },
                success: function (data) {}
            });
        } else {
          return;
        }
      }
      else
      {
    	 window.close();
      }
    }
  });
}

function submitForm() {
  var msg = validateForm();
  if (msg != "") {
    alert(msg);
    return;
  } else {
    // Submit the Form, if possible(No report is generating.)
    $.ajax({
      type: 'POST',
      dataType: 'json',
      url: '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
      data: {
        'reportType': $("input[name='reportType']").val()
      },
      success: function(data) {
        if (data != null && data.status == inProgressStatus) {
          alert("<%=bundle.getString("msg_duplilcate_report")%>");
        } else {
          $("form[name='searchForm']").submit();
        }
      }
    });
  }
}

function checkThis(obj) {
  document.getElementById("detailReport").checked = false;
  document.getElementById("yearReport").checked = false;
  obj.checked = true;
}
</script>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
    <TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%= EMEA%> <%=bundle.getString("online_jobs_for_ip_translator_report")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
    <TR><TD><SPAN CLASS="smallText"><%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN></TD></TR>
</TABLE>

<form name="searchForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.ONLINE_JOBS_REPORT_FOR_IPTRANSLATOR%>">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=bundle.getString("lb_project")%>*:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="projectId" multiple="true" size=4>
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</option>
<%
            for (Project p : projectList)
            {
%>          <option value="<%=p.getId()%>"><%=p.getName()%></option>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_status")%>*:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="status" multiple="true" size=4>
            <option value="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option value='<%=Job.PENDING%>'><%= bundle.getString("lb_pending") %></option>
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
        <td class="standardText"><%=bundle.getString("lb_target_locales")%>*:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLocalesList" multiple="true" size=4>
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for(GlobalSightLocale gsLocale : targetLocales)
            {
%>          <option VALUE="<%=gsLocale.getId()%>"><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <TR>
        <TD><%=bundle.getString("re_calculate_costs")%>?<br></TD>
        <TD>
            <SELECT NAME="recalc">
                <OPTION name="false" VALUE="false" SELECTED><%=bundle.getString("lb_false")%></OPTION>
                <OPTION name="true" VALUE="true"><%=bundle.getString("lb_true")%></OPTION>
            </SELECT>
        </TD>
    </TR>
    <TR>
        <TD><%=bundle.getString("display_job_id")%>?<br></TD>
        <TD>
            <SELECT id="jobIdVisible" NAME="jobIdVisible">
                <OPTION name="true" VALUE="true" SELECTED><%=bundle.getString("lb_true")%></OPTION>
                <OPTION name="false" VALUE="false" ><%=bundle.getString("lb_false")%></OPTION>
            </SELECT>
        </TD>
    </TR>

    <TR>
        <TD><input id="yearReport" type="radio" onclick="checkThis(this)" name="reportForThisYear" checked><%=bundle.getString("run_the_report_for_this_year")%>: <br></TD>
        <TD><SELECT NAME="year">
        <% 
            Calendar calendar = new GregorianCalendar();
            int year = calendar.get(Calendar.YEAR);
    
            for (int i = 2005; i < year; i++)
            {
                %>
                <OPTION name="<%=i%>" VALUE="<%=i%>"><%=i%></OPTION>
                <%
            }
        %>
        <OPTION name="<%=year%>" VALUE="<%=year%>" SELECTED><%=year%></OPTION>
        </SELECT>
        </TD>
    </TR>

    <tr>
        <td class="standardText" colspan=2>
            <input id="detailReport" onclick="checkThis(this)" type="radio" name="reportForDetail">
            <%=bundle.getString("lb_creation_date_range")%>:
        </td>
    </tr>
    <tr>
        <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
            <%=bundle.getString("lb_starts")%>:
            <input type="text" id="csf" name="<%=creationStart%>">
            <%=bundle.getString("lb_ends")%>:
            <input type="text" id="cef" name="<%=creationEnd%>">
        </td>
    </tr>

	<tr>
		<td class="standardText"><%=bundle.getString("date_display_format")%>:</td>
		<td class="standardText" VALIGN="BOTTOM"><select
			name="dateFormat">
			<%
             String dateFormats[] = new String[4];
             int i=0;
             dateFormats[i++] = "MM/dd/yy hh:mm:ss a z";
             dateFormats[i++] = "MM/dd/yy HH:mm:ss z";
             dateFormats[i++] = "yyyy/MM/dd HH:mm:ss z";
             dateFormats[i++] = "yyyy/MM/dd hh:mm:ss a z";
             for (i=0;i<dateFormats.length;i++) {
                %>
			<OPTION VALUE="<%=dateFormats[i]%>"><%=dateFormats[i]%></OPTION>
			<%
             }
            %>
		</select></td>
	</tr>
	<tr>
		<td class="standardText"><%=bundle.getString("lb_currency")%>:</td>
		<td><SELECT NAME="currency">
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
		</SELECT></td>
	</tr>
	<tr style="display:none">
    	<td><%=bundle.getString("lb_online_report_matches")%>:</td>
	    <td>
    	    <input type="checkbox" name="reportStyle" value="trados" checked><%=bundle.getString("lb_online_report_trados")%>
    	</td>
	</tr>
	<tr>
		<TD><INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>"
				id="submitButton" name="submitButton" onClick="submitForm()"></TD>
		<TD><INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" 
				onClick="fnDoCancel();"></TD>
	</tr>
</table>

<div id="reportDialog"></div>
</form>
</body>
</HTML>
