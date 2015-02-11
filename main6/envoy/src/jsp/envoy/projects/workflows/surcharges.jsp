<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.costing.Cost,
            com.globalsight.everest.costing.Money,
            com.globalsight.everest.costing.Currency,     
            com.globalsight.everest.costing.CurrencyFormat, 
            com.globalsight.everest.costing.FlatSurcharge,
            com.globalsight.everest.costing.PercentageSurcharge,
            com.globalsight.everest.costing.Surcharge,
            com.globalsight.everest.costing.BigDecimalHelper,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            java.text.NumberFormat,
            java.util.ResourceBundle"
    session="true" %>

<jsp:useBean id="surcharges" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%    
          
    ResourceBundle bundle = PageHandler.getBundle(session);
    String surchargesURL = surcharges.getPageURL();
    String detailsURL = jobDetails.getPageURL();

    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String surchargesFor = (String)sessionMgr.getAttribute(JobManagementHandler.SURCHARGES_FOR);
    Cost cost = null;
    if(surchargesFor.equals(WebAppConstants.EXPENSES))
    {
        cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.COST_OBJECT);
    }
    else
    {
        cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.REVENUE_OBJECT);
    }
    String jobName = (String)request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET);
    String jobId = ((String)request.getAttribute(JobManagementHandler.JOB_ID)).toString();
    String title = bundle.getString("lb_surcharges_list") + " " + jobName;
    String surchargeType = (String)sessionMgr.getAttribute(JobManagementHandler.SURCHARGE_TYPE);
    
    // For "Additional functionality quotation" issue
    Job job = null;
    try{
       job = ServerProxy.getJobHandler().getJobById(new Long(jobId).longValue());
    }
    catch(Exception e)
   {
       System.out.println("Error while getting Job Comments");
       e.printStackTrace();
   }

    detailsURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
    surchargesURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
  var needWarning = false;
  var objectName = "";
  var guideNode = "myJobs";
  var helpFile = "<%=bundle.getString("help_surcharges")%>";

   function submitForm(action)
   {
      if (action == "remove" || action == "edit")
      {
         if ( !isRadioChecked(jobForm.surcharge)) return false; 
      }

      if (action == "edit")
      {
         var valuesArray;
         // If more than one checkbox button is displayed, loop
         // through the array to find the one checked
         if (jobForm.surcharge.length)
         {
            for (i = 0; i < jobForm.surcharge.length; i++) 
            {
               if (jobForm.surcharge[i].checked == true) 
               {
                  valuesArray = getCheckboxValues(jobForm.surcharge[i].value);
               }
            }
         }
         // If only one checkbox button is displayed, there is no checkbox button array, so
         // just check if the single radio button is checked
         else 
         {
            if (jobForm.surcharge.checked == true)
            {
               valuesArray = getCheckboxValues(jobForm.surcharge.value);
            }
         }
         jobForm.surchargeName.value = valuesArray[0];
         jobForm.surchargeType.value = valuesArray[1];
         jobForm.surchargeValue.value = valuesArray[2];
     }
     else if (action == "remove")
     {
        // To remove surcharges, we only need the name of the 
        // surcharge.
        if (jobForm.surcharge.length > 0)
        {
            for (i = 0; i < jobForm.surcharge.length; i++) 
            {
               valuesArray = getCheckboxValues(jobForm.surcharge[i].value);
               jobForm.surcharge[i].value = valuesArray[0];
            }
        }
        else
        {
           if (jobForm.surcharge.checked == true)
           {
              valuesArray = getCheckboxValues(jobForm.surcharge.value);
              jobForm.surcharge.value = valuesArray[0];
           }
        }
     }

     jobForm.formAction.value = action;
     jobForm.submit();
  }


  function getCheckboxValues(str) 
  {
        var checkboxValueRegex = /^surchargeName=(.*)&surchargeType=(\w*)&surchargeValue=(.*)$/;
        checkboxValueRegex.test(str);
        var surchargeName = RegExp.$1;
        var surchargeType= RegExp.$2;
        var surchargeValue = RegExp.$3;

        var valuesArray = [surchargeName, surchargeType, surchargeValue]
        return valuesArray;
  }


  function loadPage()
  {
     loadGuides();
  }

  function setButtonState()
  {
     j = 0;
     if (jobForm.surcharge.length)
     {
        for (i = 0; i < jobForm.surcharge.length; i++) 
        {
           if (jobForm.surcharge[i].checked == true) 
           {
              j++;
             if(j > 1)
             {
                jobForm.edit.disabled = true;
             }
             else
             {
               // For "Additional functionality quotation" issue
               var chexBoxValue = jobForm.surcharge[i].value;
               var surChargeNameStart = chexBoxValue.indexOf("=")+1;
               var surChargeNameEnd = chexBoxValue.indexOf("&");
               var surChargeName = chexBoxValue.substring(surChargeNameStart,surChargeNameEnd);

               if(surChargeName == "<%=SystemConfigParamNames.PER_FILE_CHARGE01_KEY%>")
               {
                    jobForm.edit.disabled = true;
               }
               else if (surChargeName == "<%=SystemConfigParamNames.PER_FILE_CHARGE02_KEY%>")
               {
                    jobForm.edit.disabled = true;
               }
               else if(surChargeName == "<%=SystemConfigParamNames.PER_JOB_CHARGE_KEY%>")
               {
                     jobForm.edit.disabled = true;
               }
               else
               {
                     jobForm.edit.disabled = false;
               }
        
           }
        }      
     }
 
     }
}


function reSetQuoteApprovedDateDefault()
{
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
<%@ include file="/envoy/common/constants.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_surcharges")%>
</TD>
</TR>
</TABLE>
<P>

<FORM NAME="jobForm" METHOD="POST" ACTION="<%=surchargesURL%>">

<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
<TR>
<TD>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText" WIDTH="100%"
    STYLE="border: 1px solid #0C1476" >
    <TR CLASS="tableHeadingBasic">
        <TD>&nbsp;</TD>
        <TD><%=bundle.getString("lb_surcharge")%></TD>
        <TD><%=bundle.getString("lb_type")%></TD>
        <TD><%=bundle.getString("lb_amount")%></TD>
    </TR>
    
<%
    ArrayList surchargesAll =  null;
    if(surchargesFor.equals(WebAppConstants.EXPENSES))
    {
        surchargesAll = 
            (ArrayList)request.getAttribute(JobManagementHandler.SURCHARGES_ALL);
    }
    else
    {
        surchargesAll =(ArrayList)request.getAttribute(JobManagementHandler.REVENUE_SURCHARGES_ALL);
    }
    Currency currencyObj = 
        (Currency)sessionMgr.getAttribute(JobManagementHandler.CURRENCY_OBJECT);
    int count = 0;
    
    for (Iterator it = surchargesAll.iterator(); it.hasNext();) 
    {
        String bgcolor = (count % 2 == 0) ? "WHITE" : "#EEEEEE";
        Surcharge surcharge = (Surcharge)it.next();
        out.println("<TR BGCOLOR=\"" + bgcolor + "\">");
        if (surcharge.getType().equals("FlatSurcharge"))
        {
            FlatSurcharge flatSurcharge = (FlatSurcharge)surcharge;
            String formattedAmount = flatSurcharge.getAmount().getFormattedAmount();
            NumberFormat formatter = CurrencyFormat.getCurrencyFormat(flatSurcharge.getAmount().getCurrency());
            float surchargeAmount = (formatter.parse(formattedAmount)).floatValue();
            
            // For "Additional functionality quotation" issue
            String flatSurchargeLocalName =  flatSurcharge.getName();
            float perSurcharge = 0.0f;
            float fileCounts = job.getSourcePages().size();
            if(flatSurchargeLocalName.equals(SystemConfigParamNames.PER_FILE_CHARGE01_KEY))
            {
               perSurcharge = BigDecimalHelper.divide(surchargeAmount,fileCounts);
               flatSurchargeLocalName = bundle.getString("lb_per_file_charge_01_detail") + "<font color='BLUE'>&nbsp;&nbsp;$"+perSurcharge+"</font>";
            }else if(flatSurchargeLocalName.equals(SystemConfigParamNames.PER_FILE_CHARGE02_KEY))
            {
               perSurcharge = BigDecimalHelper.divide(surchargeAmount,fileCounts);
               flatSurchargeLocalName = bundle.getString("lb_per_file_charge_02_detail") + "<font color='BLUE'>&nbsp;&nbsp;$"+perSurcharge+"</font>";
             }else if(flatSurchargeLocalName.equals(SystemConfigParamNames.PER_JOB_CHARGE_KEY)){
               flatSurchargeLocalName = bundle.getString("lb_per_job_charge_detail");
             }

            out.println("<TD><INPUT TYPE=CHECKBOX NAME=" +  JobManagementHandler.SURCHARGE  + 
                        " VALUE=\"" + 
                        JobManagementHandler.SURCHARGE_NAME + "=" + surcharge.getName() + "&" + 
                        JobManagementHandler.SURCHARGE_TYPE + "=" + surcharge.getType() + "&"  +
                        JobManagementHandler.SURCHARGE_VALUE + "=" + surchargeAmount + "\"" +   
                        " ONCLICK=\"setButtonState()\"></TD>");
            out.println("<TD>" + flatSurchargeLocalName + "</TD>" +
                        "<TD>" + bundle.getString("lb_flat") + "</TD>" + 
                        "<TD ALIGN=RIGHT>" + 
                        formattedAmount +  
                        "</TD>");
        }
        else 
        {
            PercentageSurcharge percentageSurcharge = (PercentageSurcharge)surcharge;
            float percentage = Money.roundOff(percentageSurcharge.getPercentage() * 100);
            float percentageAmount = 
                percentageSurcharge.surchargeAmount(cost.getActualCost()).getAmount();
            out.println("<TD><INPUT TYPE=CHECKBOX NAME=" +  JobManagementHandler.SURCHARGE  + 
                        " VALUE=\"" + 
                        JobManagementHandler.SURCHARGE_NAME + "=" + surcharge.getName() + "&" + 
                        JobManagementHandler.SURCHARGE_TYPE + "=" + surcharge.getType() + "&" + 
                        JobManagementHandler.SURCHARGE_VALUE + "=" + percentage + "\"" +   
                        " ONCLICK=\"setButtonState()\"></TD>");
            out.println("<TD>" + percentageSurcharge.getName() + "</TD>" + 
                        "<TD>" + bundle.getString("lb_percentage") + "</TD>" + 
                        "<TD ALIGN=RIGHT>" + percentage + "%</TD>");
        }
        out.println("</TR>");
        count++;
    }
%>
    <TR>
    <TD COLSPAN=4 ALIGN="RIGHT">
    </TD>
    </TR>
</TABLE>

</TD>
</TR>
<TR>
<TD ALIGN="RIGHT">
        <INPUT TYPE="BUTTON" NAME="remove" VALUE="<%=bundle.getString("lb_remove")%>" ONCLICK="submitForm('remove')">
        <INPUT TYPE="BUTTON" NAME="edit" VALUE="<%=bundle.getString("lb_edit")%>..." ONCLICK="submitForm('edit')"> 
        <INPUT TYPE="BUTTON" NAME="add" VALUE="<%=bundle.getString("lb_add")%>..." ONCLICK="submitForm('add')">
        <INPUT TYPE="BUTTON" NAME="ok" VALUE="<%=bundle.getString("lb_ok")%>" ONCLICK="reSetQuoteApprovedDateDefault();submitForm('ok')">
        <INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
        <INPUT TYPE="HIDDEN" NAME="surchargeName" VALUE="">
        <INPUT TYPE="HIDDEN" NAME="surchargeValue" VALUE="">
        <INPUT TYPE="HIDDEN" NAME="surchargeType" VALUE="">
        <INPUT TYPE="HIDDEN" NAME="<%= JobManagementHandler.QUOTE_APPROVED_DATE %>" VALUE="">
</TD>
</TR>
</TABLE>

</FORM>

</DIV>
</BODY>
</HTML>
