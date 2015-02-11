<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,
             com.globalsight.everest.servlet.util.SessionManager,                 
             com.globalsight.everest.webapp.WebAppConstants,
             com.globalsight.everest.webapp.javabean.NavigationBean,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
             com.globalsight.everest.webapp.pagehandler.administration.customer.MyJobComparator,
             com.globalsight.everest.webapp.pagehandler.administration.customer.MyJobsHandler,
             com.globalsight.util.resourcebundle.ResourceBundleConstants,
             com.globalsight.everest.webapp.webnavigation.LinkHelper,
             com.globalsight.everest.servlet.util.ServerProxy,
             com.globalsight.everest.servlet.EnvoyServletException,
             com.globalsight.everest.util.system.SystemConfigParamNames,
             com.globalsight.everest.util.system.SystemConfiguration,
             com.globalsight.everest.foundation.SearchCriteriaParameters,
             com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
             com.globalsight.util.GeneralException,
             java.util.ArrayList,
             java.util.Date,
             java.util.Vector,
             java.util.List,
             java.util.Locale, 
             java.util.ResourceBundle"
    session="true" 
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="view" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="comments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="assign" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="myjobs" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    String title= bundle.getString("lb_my_jobs");
                                 
    //Button names
    String downloadButton = bundle.getString("lb_download");
    String viewButton = bundle.getString("lb_view_file_list");
    String assignButton = bundle.getString("lb_assign");
    String commentsButton = bundle.getString("lb_comments");
    String searchButton = bundle.getString("lb_search");

    //Urls of the links on this page
    String selfUrl = self.getPageURL();
    String downloadUrl = download.getPageURL();
    String assignUrl = assign.getPageURL();
    String viewUrl = view.getPageURL();
    String commentsUrl = comments.getPageURL() + "&action=comments";
    String searchUrl = selfUrl + "&action=search";
    
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var helpFile = "<%=bundle.getString("help_customer_myjobs")%>";

function enableButtons()
{
    myjobsForm.viewBtn.disabled = false;
    myjobsForm.commentsBtn.disabled = false;
    myjobsForm.assignBtn.disabled = false;
    myjobsForm.downloadBtn.disabled = false;
}

function submitForm(selectedButton) 
{
    if (selectedButton=='search')
    {
        myjobsForm.action = "<%=searchUrl%>";
        myjobsForm.submit();
        return;
    }

    var value = getRadioValue(myjobsForm.radioBtn);
    if (selectedButton=='view')
    {          
        myjobsForm.action = "<%=viewUrl%>" + "&value=" + value;
    }
    else if (selectedButton=='comments')
    {
        myjobsForm.action = "<%=commentsUrl%>" + "&value=" + value;
    }
    else if (selectedButton=='assign')
    {
        myjobsForm.action = "<%=assignUrl%>" + "&value=" + value;
    }
    else if (selectedButton=='download')
    {
        var toks = value.split(",");
        myjobsForm.action = "<%=downloadUrl%>" +
                            "&<%=DownloadFileHandler.PARAM_UPLOAD_NAME%>=" + toks[0] +
                            "&<%=DownloadFileHandler.PARAM_LOCALE%>=" + toks[1] +
                            "&<%=DownloadFileHandler.PARAM_FIRST_ENTRY%>=true";

    }
    myjobsForm.submit();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
 ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" />

<FORM NAME="myjobsForm" METHOD="POST">
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_job_name")%>:
          </td>
          <td>
            <input type="text" size="30" name="nameField">
          </td>
          <td>
            <input type="button" value="<%=searchButton%>..." onClick="submitForm('search'
);">
          </td>
        </tr>
      </table>
<p>
    <table cellpadding=0 cellspacing=0 border=0 CLASS="standardText">
      <tr valign="top">    
         <td align="right">
            <amb:tableNav bean="myjobs" key="<%=MyJobsHandler.MYJOB_KEY%>"
                 pageUrl="self" />
         </td>
      </tr>
        <tr>
          <td>
            <amb:table bean="myjobs" id="job"
                 key="<%=MyJobsHandler.MYJOB_KEY%>"
                 dataClass="com.globalsight.everest.webapp.pagehandler.administration.customer.MyJob"
                 pageUrl="self" 
                 emptyTableMsg="msg_customer_jobs_empty" >
              <amb:column label="" width="20px">
                 <input type=radio name=radioBtn
                   value="<%
                     out.print(job.getJobName() + ",");
                     out.print(job.getTargetLocale().toString() + "," +
                             job.getSourceLocale().toString());
                     List jobIds = job.getJobIds();
                     for (int i=0; i < jobIds.size(); i++)
                     {
                         out.print("," + jobIds.get(i));
                     }
                         %>"
                   onclick="enableButtons()" >
              </amb:column>
              <amb:column label="lb_name" width="180px" sortBy="<%=MyJobComparator.NAME%>">
                  <%= job.getJobName() %>
              </amb:column>
              <amb:column label="lb_source_locale" width="100px" sortBy="<%=MyJobComparator.SRC_LOCALE%>">
                <% out.print(job.getSourceLocale().toString()); %>
              </amb:column>
              <amb:column label="lb_target_locale" width="100px" sortBy="<%=MyJobComparator.TARG_LOCALE%>">
                <% out.print(job.getTargetLocale().toString()); %>
              </amb:column>
              <amb:column label="lb_source_word_count" width="50" sortBy="<%=MyJobComparator.WORD_CNT%>" align="center">
                  <%= job.getWordCount() %>
              </amb:column>
              <amb:column label="lb_creation_date" width="200px" sortBy="<%=MyJobComparator.CREATE_DATE%>">
                  <%= job.getCreateDate() %>
              </amb:column>
              <amb:column label="lb_planned_completion_date" width="200px" sortBy="<%=MyJobComparator.PLANNED_DATE%>">
                  <%= job.getPlannedDate() %>
              </amb:column>
            </amb:table>
          </td>
        </tr>
        <tr>
                      

<td style="padding-top:5px">
<DIV ID="DownloadButtonLayer" ALIGN="RIGHT" STYLE="visibility: visible">
    <INPUT TYPE="BUTTON" name="downloadBtn" VALUE="<%=downloadButton%>..."
        disabled onClick="submitForm('download');">
    <INPUT TYPE="BUTTON" name="viewBtn" VALUE="<%=viewButton%>..."
        disabled onClick="submitForm('view');">
    <INPUT TYPE="BUTTON" name="commentsBtn" VALUE="<%=commentsButton%>..."
        disabled onClick="submitForm('comments');">
    <INPUT TYPE="BUTTON" name="assignBtn" VALUE="<%=assignButton%>..."
        disabled onClick="submitForm('assign');">
</DIV>
</TD>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>
