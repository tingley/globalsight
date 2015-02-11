<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.tm.searchreplace.JobInfo,
         com.globalsight.everest.tm.searchreplace.TaskInfo,
         com.globalsight.everest.taskmanager.Task,
         com.globalsight.util.edit.PtagUtil,
         com.globalsight.util.edit.GxmlUtil,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.ling.docproc.IFormatNames,
         com.globalsight.ling.tw.PseudoConstants,
         java.text.MessageFormat,
         java.util.Hashtable,
         java.util.Locale,
         java.util.ResourceBundle" 
         session="true"
%>
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String searchUrl = search.getPageURL()+"&action=searchagain";
    String saveUrl = save.getPageURL()+"&action=save&" + WebAppConstants.TASK_STATE + "=" + Task.STATE_ACCEPTED;
    String selfUrl = self.getPageURL();

    String title= bundle.getString("lb_replace_results");


    // Button names
    String saveButton = bundle.getString("lb_save");
    String searchButton = bundle.getString("lb_search");

    // Labels of the column titles
    String jobCol = bundle.getString("lb_job");
    String localeCol = bundle.getString("lb_target_locale");
    String pageCol = bundle.getString("lb_target_page");
    String segmentCol = bundle.getString("lb_segment");

    // Data for the page
    List results =
         (List)request.getAttribute("results");
    ArrayList allResults =
         (ArrayList)sessionManager.getAttribute("replaceResults");
    Hashtable localeHash = (Hashtable)sessionManager.getAttribute("localeHash");
    String buf = (String)sessionManager.getAttribute("isCaseSensitive");
    boolean isCaseSensitive = new Boolean(buf).booleanValue();
    String newString = (String)request.getAttribute("newString");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute("pageNum")).intValue();
    int numPages = ((Integer)request.getAttribute("numPages")).intValue();
    int listSize = results == null ? 0 : results.size();
    int totalResults = ((Integer)request.getAttribute("listSize")).intValue();
    int resultsPerPage = ((Integer)request.getAttribute(
        "numPerPage")).intValue();
    int resultsPossibleTo = pageNum * resultsPerPage;
    int resultsTo = resultsPossibleTo > totalResults ? totalResults : resultsPossibleTo;
    int resultsFrom = (resultsTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionManager.getAttribute("sorting");
    int numOfPagesInGroup = WebAppConstants.NUMBER_OF_PAGES_IN_GROUP;
    int pagesOnLeftOrRight = numOfPagesInGroup/2;
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<style type="text/css">
.list {
    border: 1px solid #0C1476;
}
</style>
<script language="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_replace_result")%>";

function submitForm(formAction)
{
    if (formAction == "search")
    {
        replaceForm.action = "<%=searchUrl%>";
    }
    if (formAction == "save")
    {
        if (confirm("<%=bundle.getString("jsmsg_confirm_replace")%>"))
            replaceForm.action = "<%=saveUrl%>";
        else
            return;
    }
    replaceForm.submit();
}

</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading"><%=title%></span>
<form name="replaceForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
<%
    // Make the Paging widget
    if (listSize > 0)
    {
        Object[] args = {new Integer(resultsFrom), new Integer(resultsTo),
                 new Integer(totalResults)};

        // "Displaying x to y of z"
        out.println(MessageFormat.format(
                bundle.getString("lb_displaying_records"), args));

        out.println("<br>");

        // The "First" link
        if (pageNum == 1) {
            // Don't hyperlink "First" if it's the first page
            out.print("<SPAN CLASS=standardTextGray>" + bundle.getString("lb_first") + "</SPAN> | ");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&pageNum=1&sorting=<%=sortChoice%>"><%=bundle.getString("lb_first")%></A> |
<%
        }

        // The "Previous" link
        if (pageNum == 1)
        {
            // Don't hyperlink "Previous" if it's the first page
            out.print("<SPAN CLASS=standardTextGray>" + bundle.getString("lb_previous") + "</SPAN> ");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&<%="pageNum"%>=<%=pageNum -1%>&<%="sorting"%>=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A> 
<%
        }

        out.print(" ");
        int curPage = resultsFrom;
        // Print out the paging numbers
        for (int i = 1; i <= numPages; i++)
        {
            int topResult = (resultsPerPage * i) - resultsPerPage;
            if (
               ((curPage <= pagesOnLeftOrRight)
                && (i <= numOfPagesInGroup))
               ||
               (((numPages - curPage) <= pagesOnLeftOrRight)
                && (i > (numPages - numOfPagesInGroup)))
               ||
               ((i<=(curPage + pagesOnLeftOrRight))
                && (i>=(curPage - pagesOnLeftOrRight)))
               )
            {
                if (resultsFrom == topResult)
                {
                    // Don't hyperlink this page if it's current
                    out.print("<b>" + i + "</b>");
                }
                else
                {
%>
                    <a href="<%=selfUrl%>&pageNum=<%=i%>&sorting=<%=sortChoice%>"><%=i%></A>
<%
                }
            }
        }
        // The "Next" link
        if (resultsTo >= totalResults) {
            // Don't hyperlink "Next" if it's the last page
            out.print("<span class=standardTextGray>" +bundle.getString("lb_next") + "</span> | ");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&<%="pageNum"%>=<%=pageNum + 1%>&<%="sorting"%>=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A> | 

<%
        }
        // The "Last" link
        int last = totalResults / resultsPerPage;
        int mod = totalResults % resultsPerPage;
        if (mod != 0) last++;
        if (pageNum == last)
        {
            // Don't hyperlink "Last" if it's the Last page
            out.print("<SPAN CLASS=standardTextGray>" + bundle.getString("lb_last") + "</SPAN>");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&pageNum=<%=last%>&sorting=<%=sortChoice%>"><%=bundle.getString("lb_last")%></A>
<%
        }
    }
%>
      </td>
    </tr>
    <tr>
      <td colspan="2">
<!-- results data table -->
    <table border="0" cellspacing="0" cellpadding="5" class="list">
<%
    if (results != null)
    {
%>
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td style="padding-right: 10px;" nowrap>
        <%=jobCol%>
      </td>
      <td style="padding-right: 10px;" nowrap>
        <%=localeCol%>
      </td>
      <td style="padding-right: 10px;" width="10px" nowrap>
        <%=pageCol%>
      </td>
      <td style="padding-right: 10px;" width="10px">
        <%=segmentCol%>
      </td>
    </tr>
<%
    }
%>
<%
    if (results == null)
    {
%>
        <tr>
          <td colspan=3 class='standardText'><%=bundle.getString("lb_no_matches"
)%></td>
        </tr>
<%
    }
    else
    {
        for (int i=0; i < results.size(); i++)
        {
            JobInfo info = (JobInfo) results.get(i);
            String fullName = info.getTargetPageInfo().getName();
            int index1 = fullName.lastIndexOf('\\');
            int index2 = fullName.lastIndexOf('/');
            int index = index1;
            if (index1 < index2) index = index2;
            String baseName = fullName.substring(index+1);

            String stripgxml = GxmlUtil.stripRootTag(info.getTuvInfo().getGxml()
);
            String ptagString = PtagUtil.makePtagString(stripgxml,
                 PseudoConstants.PSEUDO_COMPACT,
                 info.getTuvInfo().getDataType());
            GlobalSightLocale gsl = (GlobalSightLocale)localeHash.get(
                new Long(info.getTargetLocaleInfo().getId()));
            String displayString = PtagUtil.colorPtagString(
                ptagString, newString, isCaseSensitive, gsl);
            String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
%>
            <tr style="padding-bottom:5px; padding-top:5px;"
              valign=top bgcolor="<%=color%>">
              <td><span class="standardText">
                <%=info.getJobName()%>
                <% if (info instanceof TaskInfo) { %>
                     <div class="smallText">
                     <%=bundle.getString("lb_activity")%>:
                     <%=((TaskInfo)info).getTaskName() %> 
                     </div>
                <% } %>
              </td>
              <td><span class="standardText">
                <%=info.getTargetLocaleInfo().getName()%>
              </td>
              <td><span class="standardText">
                <div title="<%=fullName%>"><%=baseName%></div>
              </td>
              <td><span class="standardText">
                <img src="/globalsight/images/checkmark.gif" border=0>
                <%=displayString%>
              </td>
            </tr>
<%
        }
    }
%>
</table>
<!-- End Data Table -->

  <tr>
    <td style="padding-top:10px">
      <input type="button" name="<%=searchButton %>" value="<%=searchButton %>"
            onclick="submitForm('search')">
      <input type="button" name="<%=saveButton %>" value="<%=saveButton %>"
            onclick="submitForm('save')">
    </td>
  </tr>
</form>
</body>
</html>
