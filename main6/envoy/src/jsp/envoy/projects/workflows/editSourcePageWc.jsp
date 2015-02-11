<%@ page contentType="text/html; charset=UTF-8"   
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.SourcePageInfo,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.ArrayList,
            java.util.ResourceBundle"
    session="true" %>

<jsp:useBean id="editSourcePageWc" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%    
          
    ResourceBundle bundle = PageHandler.getBundle(session);
    String editSourcePageWcURL = editSourcePageWc.getPageURL();
    String detailsURL = jobDetails.getPageURL();

    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String jobName = (String)request.getAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET);
    
    Object jobIdObject = request.getAttribute(JobManagementHandler.JOB_ID);
    String jobId = null;
    if (jobIdObject instanceof Long)
    {
        jobId = ((Long)jobIdObject).toString();
    }
    else
    {
        jobId = jobIdObject.toString();
    }

    String wordCount = null;
    String fileName = null;
    String spId = null;
    String check = "yes";
    String uncheck = "no";
    String enableCheckbox = check;
    // the word count for all pages in the job
    String totalWordCount = (String)request.getAttribute(JobManagementHandler.TOTAL_SOURCE_PAGE_WC);
    boolean isTotalWcOverriden = ((Boolean)request.getAttribute(
           JobManagementHandler.TOTAL_WC_OVERRIDEN)).booleanValue();  

    // info about each page in the job
    ArrayList spInfos = 
          (ArrayList)request.getAttribute(JobManagementHandler.SOURCE_PAGE_WC);
    int numOfPages = spInfos.size();

    String title = bundle.getString("lb_word_count_update_title");
    detailsURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
    editSourcePageWcURL += "&" + JobManagementHandler.JOB_ID + "=" + jobId;
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
  var helpFile = "<%=bundle.getString("help_edit_source_page_wc")%>";
  
  function submitForm(action)
  {
     if (action == "cancel")
     {
        document.jobForm.formAction.value = "cancel";
        document.jobForm.submit();
     }
     else if (action == "save")
     {
        document.jobForm.formAction.value = "save";

        if (jobForm.<%=JobManagementHandler.REMOVE_TOTAL_WC_OVERRIDEN%>.checked)
        {
            if (!confirm('<%=bundle.getString("jsmsg_word_count_override_removal")%>')) 
            {
               return false;
            }
        }
        else
        {   
            // Make sure the total word count is not null
            if (document.jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.value == "")
            {
               alert("<%=bundle.getString("jsmsg_enter_word_count")%>"); 
               jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.focus();
               return false;
            }
            // Make sure the total value entered is a number
            if (isNaN(jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.value))
            {
               alert("<%=bundle.getString("jsmsg_enter_word_count_number")%>"); 
               jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.focus();
               return false;
            }
        }

        // Make sure the word counts aren't null
 <%
        for (int ix=0 ; ix < numOfPages ; ix++)
        {
 %>
         if (jobForm.<%=JobManagementHandler.WORDCOUNT%><%=ix%>.value == "")
         {
           alert("<%=bundle.getString("jsmsg_enter_word_count")%>"); 
           jobForm.<%=JobManagementHandler.WORDCOUNT%><%=ix%>.focus();
           return false;
         }
         // Make sure the value entered is a number
         if (isNaN(jobForm.<%=JobManagementHandler.WORDCOUNT%><%=ix%>.value))
         {
            alert("<%=bundle.getString("jsmsg_enter_word_count_number")%>"); 
            jobForm.<%=JobManagementHandler.WORDCOUNT%><%=ix%>.focus();
            return false;
         }
 <%
        }
 %>
         document.jobForm.submit();
     }
  }

  function toggleOverrideTotalWordCountField() 
  {
     if (jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.disabled == true)
     {
        jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.disabled = false
     }
     else
     {
        jobForm.<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>.disabled = true
     }
  } 

  function toggleOverrideWordCountField()
  {
<%
   for (int ckbi = 0 ; ckbi < numOfPages ; ckbi++)
   {
%>
      if (jobForm.<%=JobManagementHandler.REMOVE_OVERRIDE%><%=ckbi%>.checked)
      {
         jobForm.<%=JobManagementHandler.WORDCOUNT%><%=ckbi%>.disabled = true
      }
      else
      {
         jobForm.<%=JobManagementHandler.WORDCOUNT%><%=ckbi%>.disabled = false;
      }
 <%
   }
 %>
  }
 
  function loadPage()
  {
     loadGuides();
     jobForm.<%=JobManagementHandler.WORDCOUNT%>0.focus();
   }

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=bundle.getString("lb_word_count_update")%> <%=jobName%>
</SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=600>
<%=bundle.getString("helper_text_enter_word_count")%></TD>
</TR>
</TABLE>
<P>

<FORM NAME="jobForm" ONSUBMIT="submitForm(); return false;" METHOD="POST" 
    ACTION="<%=editSourcePageWcURL%>">
    
<TABLE CELLSPACING="0" CELLPADDING="3" BORDER="0" 
    STYLE="table-layout: fixed; width: 600">
    <COL WIDTH=380>   <!-- Pages -->
    <COL WIDTH=106 >  <!-- Source Word Count -->
    <COL WIDTH=100 >  <! -- Word Count Override -->
    <TR CLASS="tableHeadingBasic" VALIGN="BOTTOM">
        <TD><%=bundle.getString("lb_primary_source_files")%></TD>
        <TD><%=bundle.getString("lb_source_word_count")%></TD>
        <TD><%=bundle.getString("lb_remove_word_count_override")%></TD>
    </TR>
    
<% for (int pi = 0 ; pi < numOfPages; pi++)
{
    SourcePageInfo spi = (SourcePageInfo)spInfos.get(pi);
    fileName = spi.getPageName();
    wordCount = Integer.toString(spi.getWordCount());
    spId = Long.toString(spi.getId());
    boolean overriden = spi.isWordCountOverriden();
%>
<TR>
<input type="hidden" name="<%=JobManagementHandler.SOURCE_PAGE_ID%><%=pi%>" value="<%=spId%>">
<TD>
<SPAN STYLE="table-layout: fixed; word-break:break-all" CLASS="standardText"><%=fileName%>: </SPAN>
</TD>
<TD>
<INPUT STYLE="text-align: right" TYPE="TEXT" SIZE=4 MAXLENGTH=10 NAME="<%=JobManagementHandler.WORDCOUNT%><%=pi%>" VALUE="<%=wordCount%>"><P>
</TD>
<TD ALIGN="center">
<DIV ID="removeOverrideDiv<%=pi%>">
<INPUT TYPE="CHECKBOX" NAME="<%=JobManagementHandler.REMOVE_OVERRIDE%><%=pi%>" 
<%
    if (overriden)
    {
        out.print(" value = \"");
        out.print(check);
        out.println("\"");
    }
    else
    {
        out.print(" value = \"");
        out.print(uncheck);
        out.println("\" disabled ");
    }
%>
    ONCLICK="toggleOverrideWordCountField()"> 
</DIV>                        
</TD>
</TR>
<% }%>
<P>
<TR VALIGN=\"TOP\" >
<TD COLSPAN=3><hr COLOR=\"000000\" height=1></TD>
</TR>
<TR>
<P>
<TD>
<SPAN CLASS="standardText"><%=bundle.getString("lb_total_word_count")%></SPAN>
</TD>
<TD>
<INPUT style="text-align: right" TYPE="TEXT" SIZE=4 MAXLENGTH=10 NAME="<%=JobManagementHandler.TOTAL_SOURCE_PAGE_WC%>" VALUE="<%=totalWordCount%>" >
</TD>
<TD ALIGN="center">
<DIV ID="removeTotalOverrideDiv">
<INPUT TYPE="CHECKBOX" NAME="<%=JobManagementHandler.REMOVE_TOTAL_WC_OVERRIDEN%>" 
<%
     if (isTotalWcOverriden)
     {
        out.print(" value = \"");
        out.print(check);
        out.println("\"");
    }
    else
    {
        out.print(" value = \"");
        out.print(uncheck);
        out.println("\" disabled ");
    }                               
%>
    ONCLICK="toggleOverrideTotalWordCountField()"> 
</DIV>                        
</TD>
</TABLE>

<INPUT TYPE="hidden" NAME="<%=JobManagementHandler.NUM_OF_PAGES_IN_JOB%>" VALUE="<%=numOfPages%>" >
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ONCLICK="submitForm('cancel')">
<INPUT TYPE="SUBMIT" VALUE="<%=bundle.getString("lb_save")%>" onclick="submitForm('save')">
<INPUT TYPE="HIDDEN" NAME="formAction" VALUE="">
</FORM>

</DIV>
</BODY>
</HTML>
