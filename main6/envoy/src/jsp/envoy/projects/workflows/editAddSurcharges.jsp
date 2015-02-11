<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.costing.FlatSurcharge,
            com.globalsight.everest.costing.PercentageSurcharge,
            com.globalsight.everest.costing.Surcharge,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.costing.Cost,
            java.util.ResourceBundle,
            java.util.Enumeration"
    session="true" %>

<jsp:useBean id="editAddSurcharges" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="surcharges" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%    
          
    ResourceBundle bundle = PageHandler.getBundle(session);
    String surchargesURL = surcharges.getPageURL();

    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String curr = (String) sessionMgr.getAttribute(JobManagementHandler.CURRENCY);
    String surchargesFor = (String)sessionMgr.getAttribute(JobManagementHandler.SURCHARGES_FOR);
    String jobId = (String)request.getAttribute(JobManagementHandler.JOB_ID);
    String editAddSurchargesURL = editAddSurcharges.getPageURL() 
    										+"&" + JobManagementHandler.SURCHARGES_FOR + "=" + surchargesFor
    										+"&" + JobManagementHandler.JOB_ID + "=" + jobId
    										+"&" + JobManagementHandler.CURRENCY + "="+curr;
    surchargesURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
    Cost cost = null;
    if(surchargesFor.equals(WebAppConstants.EXPENSES))
    {
        cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.COST_OBJECT);
    }
    else
    {
        cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.REVENUE_OBJECT);
    }
    Collection surchargesAll = cost.getSurcharges();
    String currentCurrency = (String)session.getAttribute(JobManagementHandler.CURRENCY);
    String title = bundle.getString("lb_add_surcharge");
    String helperText = bundle.getString("help_text_surcharge_add");
    String selectedChoose = "SELECTED";
    String selectedFlat = null;
    String selectedPercentage = null;
    String costUnits = "";
    String surchargeType = (String)request.getAttribute(JobManagementHandler.SURCHARGE_TYPE);
    String surchargeName = (String)request.getAttribute(JobManagementHandler.SURCHARGE_NAME);
    String surchargeOldName = null;
    if (null == surchargeName)
    {
        surchargeName = "";
    }
    else
    {
        surchargeOldName = surchargeName;
    }
    String surchargeValue = (String)request.getAttribute(JobManagementHandler.SURCHARGE_VALUE);
    if (null == surchargeValue)
    {
        surchargeValue = "";
    }
    if (request.getAttribute(JobManagementHandler.SURCHARGE_ACTION).equals("edit"))
    {
        title = bundle.getString("lb_edit_surcharge");
        helperText = bundle.getString("help_text_surcharge_edit");

        selectedChoose = null;
        if (surchargeType.equals("FlatSurcharge"))
        {
            selectedFlat = "SELECTED";
            selectedPercentage = null;
            costUnits = currentCurrency;
        }
        else if (surchargeType.equals("PercentageSurcharge"))
        {
            selectedFlat = null;
            selectedPercentage = "SELECTED";
            costUnits = "%";
        }
    }                       
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
  var helpFile = "<%=bundle.getString("help_surcharges_edit")%>";

  function verifyName()
  {  
     var names;
	 <%for (Iterator it = surchargesAll.iterator(); it.hasNext();) 
	 {
		 Surcharge surcharge = (Surcharge)it.next(); %>
         if(document.jobForm.<%=JobManagementHandler.SURCHARGE_NAME%>.value == '<%=surcharge.getName()%>')
		 {
			if(document.jobForm.<%=JobManagementHandler.SURCHARGE_NAME%>.value == '<%=request.getAttribute(JobManagementHandler.SURCHARGE_NAME)%>')
			{
			   // do nothing
			}
            else
			{
			   alert("<%=bundle.getString("msg_duplicate_surcharge")%>"); 
			   return false;
			}
		 }
     <% } %>
	return true;
  }

  function submitForm(action)
  {
     if (action == "cancel")
     {
        jobForm.formAction.value = "cancel";
        jobForm.submit();
     }
     else if (action == "save") {
        jobForm.formAction.value = "save";
        if(!verifyName())
        {
           return false;
        }
        var tmp = jobForm.surchargeName.value;
        var partern = /[^A-Za-z0-9_ ]/;
        var patternSpace = /\s*\S+/;
        if (tmp == "" )
        {
           // Make sure the  Name is not null
           alert("<%=bundle.getString("jsmsg_enter_surcharge_name")%>"); 
           jobForm.surchargeName.focus();
           return false;
        }

		if(partern.test(tmp)||!patternSpace.test(tmp))
        {
            alert("<%=bundle.getString("lb_invalid_surcharge_name")%>"); 
           jobForm.surchargeName.focus();
           return false;
        }

        if (jobForm.surchargeType.selectedIndex == 0)
        {
           // Make sure they selected a Type
           alert("<%=bundle.getString("jsmsg_select_surcharge_type")%>"); 
           jobForm.surchargeType.focus();
           return false;
        }

        if (isNaN(jobForm.surchargeValue.value) || 
            jobForm.surchargeValue.value.replace(/(^\s*)|(\s*$)/g, "") == "")
        {
           // Make sure the value entered is a number
           alert("<%=bundle.getString("jsmsg_enter_surcharge_value")%>"); 
           jobForm.surchargeValue.focus();
           return false;
        }
        jobForm.submit();
     }
  }

  function setCostUnits()
  {
     if (jobForm.surchargeType.options[jobForm.surchargeType.selectedIndex].value == 0)
     {
        // 
        costUnits.innerHTML = "";
     }
     else if (jobForm.surchargeType.options[jobForm.surchargeType.selectedIndex].value == 1)
     {
        costUnits.innerHTML = "<%=currentCurrency%>";
     }
     else 
     {
        costUnits.innerHTML = "%";
     }
  }

  function loadPage()
  {
     loadGuides();
     jobForm.surchargeName.focus();
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
<%=helperText%>
</TD>
</TR>
</TABLE>
<P>

<FORM NAME="jobForm" METHOD="POST" ACTION="<%=editAddSurchargesURL%>">

<TABLE CELLPADDING=4 CELLSPACING=0 BORDER=0 CLASS=standardText>
    <TR>
        <TD>
            <%=bundle.getString("lb_name")%>:
        </TD>
        <TD>
            <INPUT TYPE="TEXT" SIZE=20 MAXLENGTH=40 NAME="<%=JobManagementHandler.SURCHARGE_NAME%>" 
                VALUE="<%=surchargeName%>">
        </TD>
    </TR>
    <TR>
        <TD>
            <%=bundle.getString("lb_type")%>:
        </TD>
        <TD>
            <SELECT NAME="<%=JobManagementHandler.SURCHARGE_TYPE%>" ONCHANGE="setCostUnits()">
                <OPTION VALUE="0" <%=selectedChoose%>><%=bundle.getString("lb_choose")%>
                <OPTION VALUE="1" <%=selectedFlat%>><%=bundle.getString("lb_flat")%>
                <OPTION VALUE="2" <%=selectedPercentage%>><%=bundle.getString("lb_percentage")%>
            </SELECT> 
        </TD>
    </TR>
    <TR>
        <TD>
            <%=bundle.getString("lb_value")%>:
        </TD>
        <TD>
            <INPUT STYLE="text-align: right"  TYPE="TEXT" SIZE=10 MAXLENGTH=10 NAME="<%=JobManagementHandler.SURCHARGE_VALUE%>" 
                VALUE="<%=surchargeValue%>"> 
            <SPAN ID="costUnits"><%=costUnits%></SPAN>
        </TD>
    </TR>
    <TR>
        <TD COLSPAN=2 ALIGN="RIGHT">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" 
                ONCLICK="submitForm('cancel')"> 
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" 
                ONCLICK="submitForm('save')"> 
            <INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
            <INPUT TYPE="HIDDEN" NAME="<%=JobManagementHandler.JOB_ID%>" VALUE="<%=jobId%>">
            <INPUT TYPE="HIDDEN" NAME="<%=JobManagementHandler.SURCHARGE%>" VALUE="<%=surchargeOldName%>">
            <INPUT TYPE="HIDDEN" NAME="<%=JobManagementHandler.SURCHARGE_ACTION%>" 
                VALUE="<%=request.getAttribute(JobManagementHandler.SURCHARGE_ACTION)%>">
            
        </TD>
    </TR>
</TABLE>

</FORM>

</DIV>
</BODY>
</HTML>
