<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.costing.Cost,
            com.globalsight.everest.costing.CurrencyFormat,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.ResourceBundle,
            java.text.NumberFormat,
            java.util.Enumeration"
    session="true" %>

<jsp:useBean id="editFinalCost" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%    
          
    ResourceBundle bundle = PageHandler.getBundle(session);
    String editFinalCostURL = editFinalCost.getPageURL();
    String detailsURL = jobDetails.getPageURL();
    String lbFinalCost = "";
    String lbFinalOverride = "";

    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String surchargesFor = (String)request.getParameter(JobManagementHandler.SURCHARGES_FOR);
    
    Cost cost = null;
    if(surchargesFor.equals(WebAppConstants.EXPENSES))
    {
        cost = (Cost)request.getAttribute(JobManagementHandler.COST_OBJECT);
        editFinalCostURL += "&" + JobManagementHandler.SURCHARGES_FOR + "=" + WebAppConstants.EXPENSES;
        lbFinalCost = bundle.getString("lb_final_internal_costs");
        lbFinalOverride = bundle.getString("lb_final_expenses_override");
    }
    else
    {
        cost = (Cost)request.getAttribute(JobManagementHandler.REVENUE_OBJECT);
        editFinalCostURL += "&" + JobManagementHandler.SURCHARGES_FOR + "=" + WebAppConstants.REVENUE;
        lbFinalCost = bundle.getString("lb_final_revenue_costs");
        lbFinalOverride = bundle.getString("lb_final_revenue_override");
    }
    String formattedCost = cost.getFinalCost().getFormattedAmount();
    NumberFormat formatter = CurrencyFormat.getCurrencyFormat(cost.getCurrency());
    float finalCost = (formatter.parse(formattedCost)).floatValue();

    boolean isCostOverriden = false;
    isCostOverriden = cost.isOverriden();

    String jobName = (String)request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET);
    String jobId = ((String)request.getAttribute(JobManagementHandler.JOB_ID)).toString();
    String currentCurrency = (String)session.getAttribute(JobManagementHandler.CURRENCY);
    String title = bundle.getString("lb_enter_final_cost") + " " + jobName;  

    detailsURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
    editFinalCostURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
  var needWarning = false;
  var objectName = "";
  var guideNode = "myJobs";
  var helpFile = "<%=bundle.getString("help_final_cost")%>";

  function submitForm(action)
  {
     if (action == "cancel")
     {
        document.jobForm.formAction.value = "cancel";
        document.jobForm.submit();
     }
     else
     {
        document.jobForm.formAction.value = "save";

        // quotedate will be set default value when the submit is "save".
        reSetQuoteApprovedDateDefault();

        // Make them confirm Override Removal
        if (jobForm.<%=JobManagementHandler.REMOVE_OVERRIDE%>.checked)
        {
           if (!confirm('<%=bundle.getString("jsmsg_override_removal")%>')) 
           {
              return false;
           }
        }
        else
        {           
           // Make sure the Job Cost field is not null
           if (document.jobForm.finalCost.value == "")
           {
              alert("<%=bundle.getString("jsmsg_enter_final_cost")%>"); 
              jobForm.finalCost.focus();
              return false;
           }

           // Make sure the value entered is a number
           if (isNaN(jobForm.finalCost.value))
           {
              alert("<%=bundle.getString("jsmsg_enter_final_cost_number")%>"); 
              jobForm.finalCost.focus();
              return false;
           }
        }


        document.jobForm.submit();
     }
  }

  function toggleCostField() 
  {
     if (jobForm.<%=JobManagementHandler.FINAL_COST%>.disabled == true)
     {
        jobForm.<%=JobManagementHandler.FINAL_COST%>.disabled = false
     }
     else
     {
        jobForm.<%=JobManagementHandler.FINAL_COST%>.disabled = true
     }
  }

  function loadPage()
  {
     loadGuides();
     jobForm.finalCost.focus();
     removeOverrideDiv.disabled = <%=isCostOverriden%> ? false : true;
  }
  

function reSetQuoteApprovedDateDefault()
{
    var d = new Date();
  	var quoteEditDateDefaultValue = "0000";
	jobForm.<%= JobManagementHandler.QUOTE_APPROVED_DATE %>.value = quoteEditDateDefaultValue;
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()" CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_costing_final_cost")%>
</TD>
</TR>
</TABLE>
<P>

<FORM NAME="jobForm" ONSUBMIT="submitForm(); return false;" METHOD="POST" 
    ACTION="<%=editFinalCostURL%>">

<SPAN CLASS="standardText"><%=lbFinalCost%>: </SPAN>
<INPUT STYLE="text-align: right" TYPE="TEXT" SIZE=10 MAXLENGTH=10 NAME="<%=JobManagementHandler.FINAL_COST%>" VALUE="<%=finalCost%>"> <%=currentCurrency%> 
<% if (isCostOverriden) { %>
<BR>
<SPAN CLASS="standardTextGray">(<%=lbFinalOverride%>)</SPAN>
<%}%>
<P>

<P>
<DIV ID="removeOverrideDiv">
<INPUT TYPE="CHECKBOX" NAME="<%=JobManagementHandler.REMOVE_OVERRIDE%>" 
    ONCLICK="toggleCostField()"> <%=bundle.getString("lb_remove_override")%>
<P>
</DIV>

<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ONCLICK="submitForm('cancel')">
<INPUT TYPE="SUBMIT" VALUE="<%=bundle.getString("lb_save")%>">
<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
<INPUT TYPE="HIDDEN" NAME="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" VALUE="">
</FORM>

</DIV>
</BODY>
</HTML>
