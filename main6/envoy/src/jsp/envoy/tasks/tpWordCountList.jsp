<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.page.PageWordCounts,
      com.globalsight.everest.page.PrimaryFile,
      com.globalsight.everest.page.TargetPage,
      com.globalsight.everest.page.UnextractedFile,
      com.globalsight.everest.util.system.SystemConfigParamNames,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.webapp.pagehandler.tasks.TPWordCountComparator, 
      com.globalsight.everest.webapp.pagehandler.tasks.WordCountHandler,
      com.globalsight.everest.webapp.WebAppConstants, 
      com.globalsight.everest.workflowmanager.Workflow,
      java.util.ArrayList,
      java.util.List,
      java.util.Locale,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="back" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="targetPages" scope="request"
 class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
 
 	String taskId = (String)request.getAttribute(WebAppConstants.TASK_ID);  
 	String taskState = (String)request.getAttribute(WebAppConstants.TASK_STATE);  
 	
    String title = bundle.getString("lb_detailed_word_counts");

    String action = back.getPageURL() + "&action=wcBack&"+WebAppConstants.TASK_ID+"="+taskId+
    		"&"+WebAppConstants.TASK_STATE+"="+taskState;

    TargetPage tp = null;
    Workflow wf = null;
    String jobName = null;
    int lmt = 75;
    if (targetPages.size() > 0)
    {
        tp = (TargetPage)targetPages.get(0);
        wf = tp.getWorkflowInstance();
        lmt = wf.getJob().getLeverageMatchThreshold();
        jobName = title + ": " + wf.getJob().getJobName();
    }
    
    String detailedStatistics = bundle.getString("lb_detailed_statistics");
    String summaryStatistics = bundle.getString("lb_summary_statistics");
    String leverageMatchThreshold = bundle.getString("lb_leverage_match_threshold") + 
      " = "+ lmt + "%";
    
    boolean noWordCountPermission = true;
    boolean isDell = ((Boolean)request.getAttribute(SystemConfigParamNames.IS_DELL)).booleanValue();
    boolean isUseInContext = ((Boolean)request.getAttribute(WebAppConstants.IS_USE_IN_CONTEXT)).booleanValue();
    boolean leverageExactOnly = ((Boolean)request.getAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY)).booleanValue();
    boolean isInContextMatch = ((Boolean)request.getAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH));
%>
<%!
private boolean isUseInContext(TargetPage tp){
    boolean isUseInContext = false;
    isUseInContext = tp.getSourcePage().getRequest().getJob().getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
    return isUseInContext;
}
private boolean isUseInContext(Workflow wf){
    boolean isUseInContext = false;
    isUseInContext = wf.getJob().getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
    return isUseInContext;
}

private String getMainFileName(String p_filename)
{
  int index = p_filename.indexOf(")");
  if (index > 0 && p_filename.startsWith("("))
  {
    index++;
    while (Character.isSpace(p_filename.charAt(index)))
    {
      index++;
    }

    return p_filename.substring(index, p_filename.length());
  }

  return p_filename;
}

private String getSubFileName(String p_filename)
{
  int index = p_filename.indexOf(")");
  if (index > 0 && p_filename.startsWith("("))
  {
    return p_filename.substring(0, p_filename.indexOf(")") + 1);
  }

  return null;
}
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myActivities";
var helpFile = "<%=bundle.getString("help_activity_wordcounts2")%>";
$(document).ready(function(){
	$("#wcForm a").each(function(){		
		if($(this).attr("href").indexOf("targetPageSorting")>=0)
		{
			$(this).attr("href",$(this).attr("href")+"&taskId="+<%=taskId%>);
		}
	});
});
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px; width:1000px;">

<amb:header title="<%=jobName%>" />
<amb:header title="<%=leverageMatchThreshold%>" helperText='<%=bundle.getString("msg_total_word_count")%>' />

<form name="wcForm" id="wcForm" method="post" action="<%=action%>">
<% if (userPerms.getPermissionFor(Permission.ACTIVITIES_DETAIL_STATISTICS)){
   noWordCountPermission = false;
%>
<amb:header title="<%=detailedStatistics%>" />
<p class="standardText"><%=bundle.getString("helper_text_detailed_statistics")%></p>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="targetPages" key="<%=WordCountHandler.TP_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
    <%int n = 0; %>
      <amb:table bean="targetPages" id="targetPage"
      key="<%=WordCountHandler.TP_KEY%>"
      dataClass="com.globalsight.everest.page.TargetPage" pageUrl="self"
      emptyTableMsg="">
      <amb:column label="lb_primary_target_file" width="250px"
      sortBy="<%=TPWordCountComparator.FILE_NAME%>">
      <%
      boolean isExtracted = targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE;
      if (isExtracted)
      {
        String pageName = getMainFileName(targetPage.getExternalPageId());
        String subName = getSubFileName(targetPage.getExternalPageId());

        if (subName != null)
        {
            pageName = pageName + " " + subName;
        }
        out.print(pageName);
      }
      else
      {
        UnextractedFile unextractedFile = (UnextractedFile)targetPage.getPrimaryFile();
        out.print(unextractedFile.getStoragePath());
      }
      %>
      </amb:column>
      <amb:column label="lb_leverage_match_option" width="180px">
      <% if(isInContextMatch) {
		  	out.print(bundle.getString("lb_leverage_in_context_matches"));
	     } else {
    		out.print(bundle.getString("lb_100_match_only"));
	     }
      %>
      </amb:column>
      <%if (isInContextMatch) { %>
        <amb:column label="lb_100" width="60px" sortBy="<%=TPWordCountComparator.EXACT%>"><%= targetPage.getWordCount().getSegmentTmWordCount() %></amb:column>
      <%} else { %>
        <amb:column label="lb_100" width="60px" sortBy="<%=TPWordCountComparator.TOTAL_EXACT%>"><%=targetPage.getWordCount().getTotalExactMatchWordCount()%></amb:column>
      <%} %>
      <amb:column label="lb_95" width="60px" sortBy="<%=TPWordCountComparator.BAND1%>"><%=targetPage.getWordCount().getHiFuzzyWordCount()%></amb:column>
      <amb:column label="lb_85" width="60px" sortBy="<%=TPWordCountComparator.BAND2%>"><%=targetPage.getWordCount().getMedHiFuzzyWordCount()%></amb:column>
      <amb:column label="lb_75" width="60px" sortBy="<%=TPWordCountComparator.BAND3%>"><%=targetPage.getWordCount().getMedFuzzyWordCount()%></amb:column>
      <amb:column label="lb_50" width="60px" sortBy="<%=TPWordCountComparator.BAND4%>"><%=targetPage.getWordCount().getLowFuzzyWordCount()%></amb:column>
      <amb:column label="lb_no_match" width="60px" sortBy="<%=TPWordCountComparator.NO_MATCH%>"><%=targetPage.getWordCount().getNoMatchWordCount()%></amb:column>
      <amb:column label="lb_repetition_word_cnt" width="70px" sortBy="<%=TPWordCountComparator.REPETITIONS%>"><%=targetPage.getWordCount().getRepetitionWordCount()%></amb:column>
      <% if(isInContextMatch) { %>
	    <amb:column label="lb_in_context_tm" width="100px" sortBy="<%=TPWordCountComparator.IN_CONTEXT%>"><%=targetPage.getWordCount().getInContextWordCount()%></amb:column>
	  <% } %>

	 <% if (n++ == targetPages.size()) { %>
	  <tr><td colspan="100" style="height:1px; background:#0C1476"></td></tr>
	  <tr>
	      <td width="250px" height="25px" class="standardText"><%=bundle.getString("lb_totals_from_all_pages")%>:</td>
          <td width="180px" class = standardText>
	  	<%
	  	  if(isInContextMatch) {
              out.print(bundle.getString("lb_leverage_in_context_matches"));
          } else {
              out.print(bundle.getString("lb_100_match_only"));
          }
	  	%>
          </td>
      <%
	     if (isInContextMatch) {
	  %>
		  <td width="60px" class=standardText><%=wf.getSegmentTmWordCount()%></td>
	  <%
         } else {
	  %>
		  <td width="60px" class=standardText><%=wf.getTotalExactMatchWordCount()%></td>
	  <%
         }
	  %>
	      <td width="60px" class=standardText><%=wf.getHiFuzzyMatchWordCount()%></td>
          <td width="60px" class=standardText><%=wf.getMedHiFuzzyMatchWordCount()%></td>
          <td width="60px" class=standardText><%=wf.getMedFuzzyMatchWordCount()%></td>
          <td width="60px" class=standardText><%=wf.getLowFuzzyMatchWordCount()%></td>
          <td width="60px" class=standardText><%=wf.getNoMatchWordCount()%></td>
          <td width="70px" class=standardText><%=wf.getRepetitionWordCount()%></td>
	    <% if (isInContextMatch) { %>
          <td width="100px" class=standardText><%=wf.getInContextMatchWordCount()%></td>
	    <% } %>
	  </tr>
	<%
	    }
	%>
      </amb:table>
    </td>
  </tr>
</TABLE>

<BR><BR>
<!-- Summary table -->
<%
    }
if (userPerms.getPermissionFor(Permission.ACTIVITIES_SUMMARY_STATISTICS)){
   noWordCountPermission = false;
%>
<p class="standardText"><amb:header title="<%=summaryStatistics%>" /></p>
<p class="standardText"><%=bundle.getString("helper_text_summary_statistics")%></p>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="targetPages" key="<%=WordCountHandler.TP_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
    <%
        int m = 0;
    %>
      <amb:table bean="targetPages" id="targetPage"
      key="<%=WordCountHandler.TP_KEY%>"
      dataClass="com.globalsight.everest.page.TargetPage" pageUrl="self"
      emptyTableMsg="">
      <%
          int totalFuzzy = 0;
            if (isDell)
            {
               totalFuzzy = targetPage.getWordCount().getThresholdHiFuzzyWordCount() + 
               				targetPage.getWordCount().getThresholdMedHiFuzzyWordCount() +
               				targetPage.getWordCount().getThresholdMedFuzzyWordCount() +
               				targetPage.getWordCount().getThresholdLowFuzzyWordCount();
            }
      %>
        
      <amb:column label="lb_primary_target_file" width="250px" sortBy="<%=TPWordCountComparator.FILE_NAME%>">
      <%
          boolean isExtracted = targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE;
          if (isExtracted)
          {
            String pageName = getMainFileName(targetPage.getExternalPageId());
            String subName = getSubFileName(targetPage.getExternalPageId());

            if (subName != null)
            {
              pageName = pageName + " " + subName;
            }
            out.print(pageName);
          }
          else
          {
            UnextractedFile unextractedFile =
              (UnextractedFile)targetPage.getPrimaryFile();
            out.print(unextractedFile.getStoragePath());
          }
      %>
      </amb:column>
      <amb:column label="lb_leverage_match_option" width="180px">
      <%
          if(isInContextMatch){
            		  	out.print(bundle.getString("lb_leverage_in_context_matches"));
            	      }else{
          	    		  out.print(bundle.getString("lb_100_match_only"));
            	      }
      %>
      </amb:column>
      
      <%
                if(isInContextMatch){
            %>
          <amb:column label="lb_100" width="60px"
              sortBy="<%=TPWordCountComparator.EXACT%>">
              <%=targetPage.getWordCount().getSegmentTmWordCount()%>
          </amb:column>
      <%
          } else {
       %>
          <amb:column label="lb_100" width="60px"
              sortBy="<%=TPWordCountComparator.TOTAL_EXACT%>">
              <%=targetPage.getWordCount().getTotalExactMatchWordCount()%>
          </amb:column>
       <%
           }
       %>
      
      <% if (isDell) { %>
        <amb:column label="lb_fuzzy_match" width="60px" sortBy="<%=TPWordCountComparator.TOTAL_FUZZY%>"><%=totalFuzzy%></amb:column>
      <% } else { %>
      <amb:column label="lb_95" width="60px" sortBy="<%=TPWordCountComparator.BAND1%>"><%=targetPage.getWordCount().getThresholdHiFuzzyWordCount()%></amb:column>
      <amb:column label="lb_85" width="60px" sortBy="<%=TPWordCountComparator.BAND2%>"><%=targetPage.getWordCount().getThresholdMedHiFuzzyWordCount()%></amb:column>
      <amb:column label="lb_75" width="60px" sortBy="<%=TPWordCountComparator.BAND3%>"><%=targetPage.getWordCount().getThresholdMedFuzzyWordCount()%></amb:column>
      <% } %>
      <% if (lmt < 75) { %>
        <amb:column label="lb_74_and_below" width="60px" sortBy="<%=TPWordCountComparator.BAND4%>"><%=targetPage.getWordCount().getThresholdLowFuzzyWordCount()%></amb:column>
      <% } %>
      <amb:column label="lb_no_match" width="60px" sortBy="<%=TPWordCountComparator.NO_MATCH%>"><%=targetPage.getWordCount().getThresholdNoMatchWordCount()%></amb:column>
      <amb:column label="lb_repetition_word_cnt" width="70px" sortBy="<%=TPWordCountComparator.REPETITIONS%>"><%=targetPage.getWordCount().getRepetitionWordCount()%></amb:column>
      <% if (isInContextMatch) { %>
        <amb:column label="lb_in_context_tm" width="100px" sortBy="<%=TPWordCountComparator.IN_CONTEXT%>"><%=targetPage.getWordCount().getInContextWordCount()%></amb:column>
      <% } %>

      <%
          if (m++ == targetPages.size()) {
      %>
	  <tr>
		<td colspan="100" style="height:1px; background:#0C1476"></td>
	  </tr>
	  <tr>
	  <td width="250px" height="25px" class="standardText"><%=bundle.getString("lb_totals_from_all_pages")%>:</td>
      <%
          int fuzzy = 0;
              if (isDell)
              {
                 fuzzy = wf.getThresholdHiFuzzyWordCount() +
                 wf.getThresholdMedHiFuzzyWordCount() + 
                 wf.getThresholdMedFuzzyWordCount() + 
                 wf.getThresholdLowFuzzyWordCount();
              }
      %>
        <td width="60px" class = standardText>
	  	<%
	  	    if(isInContextMatch){
	  				  	out.print(bundle.getString("lb_leverage_in_context_matches"));
	  			      }else{
  			    		  out.print(bundle.getString("lb_100_match_only"));
	  			      }
	  	%>
	  	</td>
	  
        <% if(isInContextMatch) { %>
		  <td width="60px" class=standardText><%=wf.getSegmentTmWordCount()%></td>
        <% } else { %>
		  <td width="60px" class=standardText><%=wf.getTotalExactMatchWordCount()%></td>
        <% } %>
        <% if (isDell) { %>
          <td width="60px" class=standardText><%= fuzzy %></td>      
        <% } else { %>
          <td width="60px" class=standardText><%=wf.getThresholdHiFuzzyWordCount()%></td>
          <td width="60px" class=standardText><%=wf.getThresholdMedHiFuzzyWordCount()%></td>
          <td width="60px" class=standardText><%=wf.getThresholdMedFuzzyWordCount()%></td>
        <% } %>

        <% if (lmt < 75) { %>
          <td width="60px" class=standardText><%=wf.getThresholdLowFuzzyWordCount()%></td>
        <% } %>
      
        <td width="60px" class=standardText><%=wf.getThresholdNoMatchWordCount()%></td>
        <td width="70px" class=standardText><%=wf.getRepetitionWordCount()%></td>
        <% if (isInContextMatch) { %>
	      <td width="100px" class=standardText><%=wf.getInContextMatchWordCount()%></td>
        <% } %>
	</tr>
	<% } %>
      </amb:table>
    </td>
  </tr>
   
</TABLE>
<%}
if (noWordCountPermission)
{%>
<%=bundle.getString("lb_no_wordcount_statistic_permission")%>
<%}%>
<BR>

<table cellpadding=0 cellspacing=0 border=0 class="standardText">
<tr>
    <td align=right style="padding-top:8px">
      <input type="submit" value='<%=bundle.getString("lb_back_to_activities")%>'>
    </td>
  </TR>
</TABLE>
</FORM>
</DIV>
</BODY>