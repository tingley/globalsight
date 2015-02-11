<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.tm.searchreplace.JobInfo,
         com.globalsight.everest.tm.searchreplace.TaskInfo,
         com.globalsight.util.edit.PtagUtil,
         com.globalsight.util.edit.GxmlUtil,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.ling.docproc.IFormatNames,
         com.globalsight.ling.tw.PseudoConstants,
         java.text.MessageFormat,
         java.util.Iterator,
         java.util.Locale,
         java.util.Hashtable,
         java.util.TreeSet,
         java.util.ResourceBundle" 
         session="true"
%>
<jsp:useBean id="replace" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="searchlocales" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String replaceUrl = replace.getPageURL() + "&action=replace";
    String searchUrl = search.getPageURL() + "&action=searchagain";
    String searchlocalesUrl = searchlocales.getPageURL() + "&action=searchlocales";
    String cancelUrl = cancel.getPageURL() + "&action=cancel";
    String selfUrl = self.getPageURL() + "&action=self";

    String title= bundle.getString("lb_search_results_replace");


    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String searchButton = bundle.getString("lb_search");
    String goButton = bundle.getString("lb_go");

    // Labels of the column titles
    String jobCol = bundle.getString("lb_job");
    String localeCol = bundle.getString("lb_target_locale");
    String pageCol = bundle.getString("lb_target_page");
    String segmentCol = bundle.getString("lb_segment");

    // Data for the page
    List results = (List)request.getAttribute("results");
    ArrayList allResults =
         (ArrayList)sessionManager.getAttribute("searchResults");
    String queryString = (String)sessionManager.getAttribute("queryString");
    String buf = (String)sessionManager.getAttribute("isCaseSensitive");
    boolean isCaseSensitive = new Boolean(buf).booleanValue();
    Hashtable localeHash = (Hashtable)sessionManager.getAttribute("localeHash");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute("searchpageNum")).intValue();
    int numPages = ((Integer)request.getAttribute("searchnumPages")).intValue();
    int listSize = results == null ? 0 : results.size();
    int totalResults = ((Integer)request.getAttribute(
        "searchlistSize")).intValue();
    int resultsPerPage = ((Integer)request.getAttribute(
        "searchnumPerPage")).intValue();
    int resultsPossibleTo = pageNum * resultsPerPage;
    int resultsTo = resultsPossibleTo > totalResults ?
        totalResults : resultsPossibleTo;
    int resultsFrom = (resultsTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionManager.getAttribute("searchsorting");
    int numOfPagesInGroup = WebAppConstants.NUMBER_OF_PAGES_IN_GROUP;
    int pagesOnLeftOrRight = numOfPagesInGroup / 2;
%>
<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script SRC="/globalsight/includes/radioButtons.js"></script>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<style type="text/css">
A { color: blue; }
.list {
    border: 1px solid #0C1476;
}
</style>
<script>
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_search_result")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           replaceForm.action = "<%=cancelUrl%>";
           replaceForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "search")
    {
        if (replaceForm.queryString.value == "")
        {
            alert("<%=bundle.getString("jsmsg_search_string")%>");
            return;
        }
        replaceForm.action = "<%=searchUrl%>";
    }
    else if (formAction == "searchLocales")
    {
        replaceForm.action = "<%=searchlocalesUrl%>";
    }
    if (formAction == "replace")
    {
        if (replaceForm.oldString.value == "")
        {
            alert("<%=bundle.getString("jsmsg_replace_string")%>");
            return;
        }
        if (replaceForm.newString.value == "")
        {
            alert("<%=bundle.getString("jsmsg_replace_string2")%>");
            return;
        }
        
        // make sure at least one checkbox is checked
        var jobId = "";
        // If more than one checkbox is displayed, loop
        // through the array to find the ones checked
        if (replaceForm.job.length)
        {
            for (i = 0; i < replaceForm.job.length; i++)
            {
                if (replaceForm.job[i].checked == true)
                {
                    jobId += replaceForm.job[i].value;
                    jobId += " "; // must add a [white space] delimiter
                }
             }
        }
        // If only one radio button is displayed, there is no radio button 
        // array, so just check if the single radio button is checked
        else
        {
            if (replaceForm.job.checked == true)
            {
                jobId += replaceForm.job.value;
            }
        }
        if (jobId == "")
        {
            alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
            return;
        }
       if (replaceForm.jobHidden && replaceForm.jobHidden.length)
       {
          for (i = 0; i < replaceForm.jobHidden.length; i++)
          {
             if (replaceForm.jobHidden[i].checked == true)
             {
                if( jobId != "" )
                {
                    jobId += replaceForm.jobHidden[i].value;
                    jobId += " "; // must add a [white space] delimiter
                }
             }
           }
        }
        replaceForm.action = "<%=replaceUrl%>" + "&jobInfos=" + jobId;
    }
    replaceForm.submit();
}

function doLoad()
{
    loadGuides();
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<span class="mainHeading"><%=title%></span>
<div class="standardText">
<%=bundle.getString("msg_replace_checked")%>
</div>
<form name="replaceForm" method="post">
<table border="0" cellpadding="0" cellspacing="0" width="50%">
<%
  if (results != null && listSize != 0)
  {
%>
  <tr>
    <td class="standardText" nowrap>
      <%=bundle.getString("lb_replace")%>
      <input type="text" name="oldString" value="<%=queryString%>" size="30" >
      <%=bundle.getString("lb_with")%>
      <input type="text" name="newString" size="30" >
      <input type="button" name="<%=goButton %>" value="<%=goButton %>"
      onclick="submitForm('replace')">
    </td>
  </tr>
<%
  }
%>
  <tr>
    <td class="standardText" nowrap>
      <%=bundle.getString("lb_search")%>
      <input type="text" name="queryString" size="30" >
      <input type="button" name="<%=goButton %>" value="<%=goButton %>"
      onclick="submitForm('search')"> &nbsp;
      <a class="standardHREF" href="javascript:submitForm('searchLocales')">
      <%=bundle.getString("lb_search_other_locales")%></a>
    </td>
  </tr>
</table>
<p>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="bottom">
<%
    if (results != null && listSize != 0)
    {
%>
    <td class="standardText">
      <a class="standardHREF"
      href="javascript:checkAllWithName('replaceForm', 'job')"><%=bundle.getString("lb_check_all")%></A> |
      <a class="standardHREF"
      href="javascript:checkAll('replaceForm')"><%=bundle.getString("lb_check_all_pages")%></A> |
      <a class="standardHREF"
      href="javascript:clearAll('replaceForm')"><%=bundle.getString("lb_clear_all")%></A>
      &nbsp;&nbsp;&nbsp;
    </td>
<%
    }
%>
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
        if (pageNum == 1)
        {
            // Don't hyperlink "First" if it's the first page
            out.print("<SPAN CLASS=standardTextGray>" + bundle.getString("lb_first") + "</SPAN> | ");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&searchpageNum=1&searchsorting=<%=sortChoice%>"><%=bundle.getString("lb_first")%></A> |
<%
        }

        // The "Previous" link
        if (pageNum == 1) {
            // Don't hyperlink "Previous" if it's the first page
            out.print("<span class=standardTextGray>" + bundle.getString("lb_previous") + "</span>");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&searchpageNum=<%=pageNum - 1%>&searchsorting=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A>
<%
        }

        out.print(" ");
        // Print out the paging numbers
        int curPage = resultsFrom/resultsPerPage + 1;
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
                    <a href="<%=selfUrl%>&searchpageNum=<%=i%>&searchsorting=<%=sortChoice%>"><%=i%></A>
<%
                }
            }
            out.print(" ");
        }
        // The "Next" link
        if (resultsTo >= totalResults) {
            // Don't hyperlink "Next" if it's the last page
            out.print("<SPAN CLASS=standardTextGray>" +bundle.getString("lb_next") + "</span> | ");
        }
        else
        {
%>
            <a href="<%=selfUrl%>&searchpageNum=<%=pageNum + 1%>&searchsorting=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A> | 

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
            <a href="<%=selfUrl%>&searchpageNum=<%=last%>&searchsorting=<%=sortChoice%>"><%=bundle.getString("lb_last")%></A>
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
    if (results != null && listSize != 0)
    {
%>
	<tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
	  <td>&nbsp;</td>
	  <td style="padding-right: 10px;" nowrap><%=jobCol%></td>
	  <td style="padding-right: 10px;" nowrap><%=localeCol%></td>
	  <td style="padding-right: 10px;" nowrap><%=pageCol%></td>
	  <td style="padding-right: 10px;" width="10px"><%=segmentCol%></td>
	</tr>
<%
    }
%>
<%
    if (results == null || listSize == 0)
    {
%>
	<tr>
	  <td colspan=3 class='standardText'>
	    <%=bundle.getString("lb_no_matches")%>
	  </td>
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

            String stripgxml = GxmlUtil.stripRootTag(info.getTuvInfo().getGxml());
            String ptagString = PtagUtil.makePtagString(stripgxml,
                 PseudoConstants.PSEUDO_COMPACT, 
                 info.getTuvInfo().getDataType());
            GlobalSightLocale gsl = (GlobalSightLocale)localeHash.get(
                new Long(info.getTargetLocaleInfo().getId())); 
            String displayString = PtagUtil.colorPtagString(
                ptagString, queryString,
                isCaseSensitive, gsl);
            String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
%>
	<tr style="padding-bottom:5px; padding-top:5px;"
	  valign=top bgcolor="<%=color%>">
	  <td>
	    <input type="checkbox" name="job" value="<%=resultsFrom-1 + i%>">
	  </td>
	  <td><span class="standardText">
	    <%=info.getJobName()%>
	    <% if (info instanceof TaskInfo) { %>
	    <div class="smallText"><%=bundle.getString("lb_activity")%>:
	    <%=((TaskInfo)info).getTaskName() %> 
	    </div>
	    <% } %>
	  </td>
	  <td>
	    <span class="standardText">
	    <%=info.getTargetLocaleInfo().getName()%>
	    </span>
	  </td>
	  <td>
	    <span class="standardText" title="<%=fullName%>"><%=baseName%></span>
	  </td>
	  <td>
	    <span class="standardText"><%=displayString%></span>
	  </td>
	</tr>
<%
        }
    }
%>
      </table>
    </td>
  </tr>
<%
    if (results != null && listSize != 0)
    {
%>
  <tr>
    <td class="standardText">
      <a class="standardHREF"
      href="javascript:checkAllWithName('replaceForm', 'job');"><%=bundle.getString("lb_check_all")%></A> |
      <a class="standardHREF"
      href="javascript:checkAll('replaceForm');"><%=bundle.getString("lb_check_all_pages")%></A> |
      <a class="standardHREF"
      href="javascript:clearAll('replaceForm');"><%=bundle.getString("lb_clear_all")%></A>
    </td>
  </tr>
  <tr>
    <td>
      <div id='restofjobs' style="display:none">
<%
        for (int i=0; i < resultsFrom-1; i++)
        {
            JobInfo jobInfo = (JobInfo) allResults.get(i);
            out.println("<input type=checkbox name=jobHidden value=" + i + ">");
        }
        for (int i=resultsTo; i < totalResults; i++)
        {
            JobInfo jobInfo = (JobInfo) allResults.get(i);
            out.println("<input type=checkbox name=jobHidden value=" + i + ">");
        }
%>
      </div>
    </td>
  </tr>
<%
    }
%>
  <!-- End Data Table -->
  <tr>
    <td style="padding-top:10px">
      <input type="button" name="<%=cancelButton %>"
      value="<%=cancelButton %>" onclick="submitForm('cancel')">
    </td>
  </tr>
</table>
</form>
</body>
</html>
