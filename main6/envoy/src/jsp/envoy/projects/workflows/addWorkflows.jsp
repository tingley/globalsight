<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.webapp.WebAppConstants,
         java.text.MessageFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    
    String jobId = (String)request.getAttribute(WebAppConstants.JOB_ID);
    String selfUrl = self.getPageURL();
    String saveUrl = save.getPageURL();
    String cancelUrl = cancel.getPageURL();
    if(jobId != null && jobId != ""){
    	selfUrl += "&"+ WebAppConstants.JOB_ID + "=" + jobId;
    	saveUrl += "&"+ WebAppConstants.JOB_ID + "=" + jobId;
    	cancelUrl += "&"+ WebAppConstants.JOB_ID + "=" + jobId;
    }
    String title= bundle.getString("lb_workflows");

    // Labels of the column titles
    String nameCol = bundle.getString("lb_name");
    String descCol = bundle.getString("lb_description");
    String localePairCol = bundle.getString("lb_locale_pair");
    String pmCol = bundle.getString("lb_project_manager");
    String projectCol = bundle.getString("lb_project"); 

    // Button names
    String addToJobButton = bundle.getString("lb_add_to_job");
    String cancelButton = bundle.getString("lb_cancel");

    // Data for the page
    List wfInfos = (List)request.getAttribute("wfInfoList");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute("pageNum")).intValue();

    int numPages = ((Integer)request.getAttribute("numPages")).intValue();

    int listSize = wfInfos == null ? 0 : wfInfos.size();
    int totalWfInfos = ((Integer)request.getAttribute("listSize")).intValue();

    int wfInfosPerPage = ((Integer)request.getAttribute(
        "numPerPage")).intValue();
    int wfInfosPossibleTo = pageNum * wfInfosPerPage;
    int wfInfosTo = wfInfosPossibleTo > totalWfInfos ? totalWfInfos : wfInfosPossibleTo;
    int wfInfosFrom = (wfInfosTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionManager.getAttribute("sorting");

%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_add_workflow")%>";

var localeArray = new Array();
<%
    for (int i=0; i < listSize; i++)
    {
        WorkflowTemplateInfo wf = (WorkflowTemplateInfo)wfInfos.get(i);
        String locale = wf.getTargetLocale().getDisplayName(uiLocale);
%>
        localeArray[<%=i%>] = "<%=locale%>";
<%  } %>
    
//
// Can't add workflows witht the same target locales.
//
function checkDup(checkedLocales, checked)
{
    for (i=0; i < checkedLocales.length; i++)
    {
        if (localeArray[checked] == checkedLocales[i])
        {
            return true;
        }
    }
    return false;
}

function submitForm(selectedButton)
{
    if (selectedButton == 'Cancel')
    {
        WFForm.action = "<%=cancelUrl %>";
        WFForm.submit();
        return;
    }
    var checkedWFs = "";
    var checkedLocales = new Array();
<%
    for (int i=0; i < listSize; i++)
    {
%>
        if (WFForm.cb<%=i%>.checked == true)
        {
            checkedWFs += WFForm.cb<%=i%>.value;
            checkedWFs += ",";
            if (checkDup(checkedLocales, "<%=i%>"))
            {
                msg = "<%=bundle.getString("jsmsg_add_workflow_to_job_dup")%>"
                        + " " + localeArray[<%=i%>];
                alert(msg);
                return;
            }
            checkedLocales[checkedLocales.length] = localeArray[<%=i%>];
        }
<%
    }
%>
    if (checkedWFs == "")
    {
        alert("<%= bundle.getString("jsmsg_wf_template_select") %>");
        return false;
    }

    if (selectedButton == 'Save')
    {
        if (!confirm("<%=bundle.getString("jsmsg_add_workflow_to_job")%>"))
            return;
        WFForm.action = "<%=saveUrl %>&<%=JobManagementHandler.ADD_WF_PARAM%>=" + checkedWFs;
    }
    WFForm.submit();
}

//for GBS-2599
function handleSelectAll() {
	if (WFForm && WFForm.selectAll) {
		if (WFForm.selectAll.checked) {
			checkAll('WFForm');
	    }
	    else {
			clearAll('WFForm'); 
	    }
	}
}
</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <SPAN CLASS="mainHeading">
    <%=title%>
    </SPAN>
    <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
        <TR VALIGN="TOP">
            <TD ALIGN="RIGHT">
        <%
        // Make the Paging widget
        if (listSize > 0)
        {
            Object[] args = {new Integer(wfInfosFrom), new Integer(wfInfosTo),
                     new Integer(totalWfInfos)};

            // "Displaying x to y of z"
            out.println(MessageFormat.format(
                    bundle.getString("lb_displaying_records"), args));

            out.println("<br>");
            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1) {
                // Don't hyperlink "Previous" if it's the first page
                out.print(bundle.getString("lb_previous"));
            }
            else
            {
%>
                <a href="<%=selfUrl%>&<%="pageNum"%>=<%=pageNum - 1%>&<%="sorting"%>=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A>
<%
            }

            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numPages; i++)
            {
                // Don't hyperlink the page you're on
                if (i == pageNum)
                {
                    out.print("<b>" + i + "</b>");
                }
                // Hyperlink the other pages
                else
                {
%>
                    <a href="<%=selfUrl%>&<%="pageNum"%>=<%=i%>&<%="sorting"%>=<%=sortChoice%>"><%=i%></A>
<%
                }
                out.print(" ");
            }
            // The "Next" link
            if (wfInfosTo >= totalWfInfos) {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
%>
                <a href="<%=selfUrl%>&<%="pageNum"%>=<%=pageNum + 1%>&<%="sorting"%>=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A>

<%
            }
            out.println(" &gt;");
        }
%>
          </td>
        <tr>
          <td>
<form name="WFForm" method="post">
<!-- WorkflowInfos data table -->
  <table border="0" cellspacing="0" cellpadding="5" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td><input type="checkbox" onclick="handleSelectAll()" name="selectAll"/></td>
      <td style="padding-right: 10px;">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=WorkflowTemplateInfoComparator.NAME%>&doSort=true"> <%=nameCol%></a>
      </td>
      <td style="padding-right: 10px;">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=WorkflowTemplateInfoComparator.DESCRIPTION%>&doSort=true"> <%=descCol%></a>
      </td>
      <td style="padding-right: 10px;">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=WorkflowTemplateInfoComparator.LOCALEPAIR%>&doSort=true"> <%=localePairCol%></a>
      </td>
      <td style="padding-right: 10px;">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=WorkflowTemplateInfoComparator.PROJECTMGR%>&doSort=true"> <%=pmCol%></a>
      </td>
      <td style="padding-right: 10px;">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= "pageNum"%>=<%=pageNum%>&<%="sorting"%>=<%=WorkflowTemplateInfoComparator.PROJECT%>&doSort=true"> <%=projectCol%></a>
      </td>
    </tr>
<%
        if (listSize == 0)
        {
%>
        <tr>
          <td colspan=3 class='standardText'><%=bundle.getString("msg_no_workflows_to_add")%></td>
        </tr>
<%
        }
        else
        {
              for (int i=0; i < listSize; i++)
              {
                String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                WorkflowTemplateInfo wf = (WorkflowTemplateInfo)wfInfos.get(i);
                String desc =
                     (wf.getDescription() == null) ? "" : wf.getDescription();
                String localePair =
                     wf.getSourceLocale().getDisplayName(uiLocale) + " -> " +
                     wf.getTargetLocale().getDisplayName(uiLocale);
%>
                <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="<%=color%>">
                  <td>
                    <input type="checkbox" name="cb<%=i%>" value="<%=wf.getId()%>">
                  </td>
                  <td><span class="standardText">
                    <%=wf.getName()%>
                  </td>
                  <td><span class="standardText">
                    <%=desc%>
                  </td>
                  <td><span class="standardText">
                    <%=localePair%>
                  </td>
                  <td><span class="standardText">
                    <%=wf.getProjectManagerId()%>
                  </td>
                  <td><span class="standardText">
                    <%=wf.getProject().getName()%>
                  </td>
                </tr>
<%
              }
        }
%>
  </tbody>
  </table>
<!-- End Data Table -->
</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD>
<DIV ID="DownloadButtonLayer" ALIGN="LEFT" STYLE="visibility: visible">
    <P>

    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <INPUT TYPE="BUTTON" VALUE="<%=addToJobButton%>" onClick="submitForm('Save');">

</DIV>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>
