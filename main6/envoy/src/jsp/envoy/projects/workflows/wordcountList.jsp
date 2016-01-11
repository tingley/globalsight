<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.util.system.SystemConfigParamNames,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WordCountHandler, 
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowComparator,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WordCountHandler,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.permission.Permission,
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
<jsp:useBean id="wfs" scope="request"
 class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
 
    String jobId = (String)request.getAttribute(WebAppConstants.JOB_ID);
    String action = back.getPageURL() + "&action=wcBack";
    if(jobId != null && jobId != ""){
    	action += "&"+ WebAppConstants.JOB_ID + "=" + jobId;
    }
    
    String title = bundle.getString("lb_detailed_word_counts");
    String detailedStatistics = bundle.getString("lb_detailed_statistics");
    String summaryStatistics = bundle.getString("lb_summary_statistics");
    String leverageMatchThreshold = bundle.getString("lb_leverage_match_threshold") + 
      " = "+ sessionMgr.getAttribute(WordCountHandler.LMT) + "%";
    String jobName = title + ": " + sessionMgr.getAttribute(
      WordCountHandler.JOB_NAME);
    
    boolean noWordCountPermission = true;
    boolean isDell = ((Boolean)request.getAttribute(
      SystemConfigParamNames.IS_DELL)).booleanValue();
    
    boolean isUseInContext = ((Boolean)sessionMgr.getAttribute(WebAppConstants.IS_USE_IN_CONTEXT)).booleanValue();
    boolean leverageExactOnly = ((Boolean)sessionMgr.getAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY)).booleanValue();
    boolean isInContextMatch = (Boolean)sessionMgr.getAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH);
    int threshold = 75;
    try
    {
        threshold = Integer.parseInt((String) sessionMgr.getAttribute(WordCountHandler.LMT));
    }
    catch (NumberFormatException e)
    {
    }
%>

<!-- This JSP is envoy/projects/workflows/wordcountList.jsp -->
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_wordcounts")%>";
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px; width: 1000px;">

<amb:header title="<%=jobName%>" />
<amb:header title="<%=leverageMatchThreshold%>" />

<form name="wcForm" method="post" action="<%=action%>">
<% if (userPerms.getPermissionFor(Permission.JOB_WORKFLOWS_DETAIL_STATISTICS)){
   noWordCountPermission = false;
%>
<amb:header title="<%=detailedStatistics%>" />
<p class="standardText"><%=bundle.getString("helper_text_detailed_statistics")%></p>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="wfs" key="<%=WordCountHandler.WF_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="wfs" id="wf"
             key="<%=WordCountHandler.WF_KEY%>"
             dataClass="com.globalsight.everest.workflowmanager.Workflow"
             pageUrl="self" emptyTableMsg="">
        <amb:column label="lb_target_locale" width="200px"
             sortBy="<%=WorkflowComparator.TARG_LOCALE%>">
            <%= wf.getTargetLocale().getDisplayName(uiLocale) %>
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
        
        <%if(isInContextMatch){ %>
            <amb:column label="lb_100" width="60px"
                 sortBy="<%=WorkflowComparator.EXACT%>">
                <%= wf.getSegmentTmWordCount()%>
            </amb:column>
        <%} else {
        %>
            <amb:column label="lb_100" width="60px"
                 sortBy="<%=WorkflowComparator.TOTAL_EXACT%>">
                <%=wf.getTotalExactMatchWordCount()%>
            </amb:column>
        <%
            }
        %>
        <amb:column label="lb_95" width="60px"
             sortBy="<%=WorkflowComparator.BAND1%>">
            <%=wf.getHiFuzzyMatchWordCount()%>
        </amb:column>
        <amb:column label="lb_85" width="60px"
             sortBy="<%=WorkflowComparator.BAND2%>">
            <%=wf.getMedHiFuzzyMatchWordCount()%>
        </amb:column>
        <amb:column label="lb_75" width="60px"
             sortBy="<%=WorkflowComparator.BAND3%>">
            <%=wf.getMedFuzzyMatchWordCount()%>
        </amb:column>
        <amb:column label="lb_50" width="60px"
             sortBy="<%=WorkflowComparator.BAND4%>">
            <%=wf.getLowFuzzyMatchWordCount()%>
        </amb:column>
        <amb:column label="lb_no_match" width="60px"
             sortBy="<%=WorkflowComparator.NO_MATCH%>">
            <%=wf.getNoMatchWordCount()%>
        </amb:column>
        <amb:column label="lb_repetition_word_cnt" width="70px"
             sortBy="<%=WorkflowComparator.REPETITIONS%>">
            <%=wf.getRepetitionWordCount()%>
        </amb:column>
        <%
            if(isInContextMatch){
        %>
	        <amb:column label="lb_in_context_tm" width="100px"
	             sortBy="<%=WorkflowComparator.IN_CONTEXT%>">
	            <%=wf.getInContextMatchWordCount()%>
	        </amb:column>
        <%  } %>
        <amb:column label="lb_total" width="60px" sortBy="<%=WorkflowComparator.WC_TOTAL%>"><%=wf.getTotalWordCount()%></amb:column>
      </amb:table>
    </td>
  </tr>
</TABLE>

<BR><BR><BR>
<%
    }
if (userPerms.getPermissionFor(Permission.JOB_WORKFLOWS_SUMMARY_STATISTICS)){
   noWordCountPermission = false;
%>
<amb:header title="<%=summaryStatistics%>" />
<p class="standardText"><%=bundle.getString("helper_text_summary_statistics")%></p>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="wfs" key="<%=WordCountHandler.WF_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="wfs" id="wf"
             key="<%=WordCountHandler.WF_KEY%>"
             dataClass="com.globalsight.everest.workflowmanager.Workflow"
             pageUrl="self" emptyTableMsg="">
        <%
            int totalFuzzy = 0;
                if (isDell && wf != null)
                {
                   totalFuzzy = wf.getThresholdHiFuzzyWordCount() + 
                        wf.getThresholdMedHiFuzzyWordCount() + 
                        wf.getThresholdMedFuzzyWordCount() + 
                        wf.getThresholdLowFuzzyWordCount();
                }
        %>
        <amb:column label="lb_target_locale" width="200px"
             sortBy="<%=WorkflowComparator.TARG_LOCALE%>">
            <%=wf.getTargetLocale().getDisplayName(uiLocale)%>
        </amb:column>
        <amb:column label="lb_leverage_match_option" width="180px">
	      <%
	          if(isInContextMatch) {
	      		 out.print(bundle.getString("lb_leverage_in_context_matches"));
	      	  } else {
	        	  out.print(bundle.getString("lb_100_match_only"));
              }
	      %>
	    </amb:column>
        <%
            if(isInContextMatch) {
        %>
	        <amb:column label="lb_100" width="60px" sortBy="<%=WorkflowComparator.EXACT%>"><%=wf.getSegmentTmWordCount()%></amb:column>
        <%
            } else {
        %>
	        <amb:column label="lb_100" width="60px" sortBy="<%=WorkflowComparator.TOTAL_EXACT%>"><%=wf.getTotalExactMatchWordCount()%></amb:column>
		<% }%>
        <%if (isDell) {%>
        <amb:column label="lb_fuzzy_match" width="60px" sortBy="<%=WorkflowComparator.TOTAL_FUZZY%>"><%= totalFuzzy %></amb:column>
        <%} else {%>
        <amb:column label="lb_95" width="60px" sortBy="<%=WorkflowComparator.BAND1%>"><%= wf.getThresholdHiFuzzyWordCount()%></amb:column>
        <amb:column label="lb_85" width="60px" sortBy="<%=WorkflowComparator.BAND2%>"><%= wf.getThresholdMedHiFuzzyWordCount() %></amb:column>
        <amb:column label="lb_75" width="60px" sortBy="<%=WorkflowComparator.BAND3%>"><%= wf.getThresholdMedFuzzyWordCount() %></amb:column>
        <%}%>
        <%if (threshold < 75) {%>
        <amb:column label="lb_74_and_below" width="60px" sortBy="<%=WorkflowComparator.BAND4%>"><%= wf.getThresholdLowFuzzyWordCount() %></amb:column>
        <%}%>
        <amb:column label="lb_no_match" width="60px" sortBy="<%=WorkflowComparator.NO_MATCH%>"><%= wf.getThresholdNoMatchWordCount() %></amb:column>
        <amb:column label="lb_repetition_word_cnt" width="70px" sortBy="<%=WorkflowComparator.REPETITIONS%>"><%= wf.getRepetitionWordCount()%></amb:column>
        <%if (!isDell) { %>
	        <%if (isInContextMatch) { %>
		        <amb:column label="lb_in_context_tm" width="100px" sortBy="<%=WorkflowComparator.IN_CONTEXT%>"><%= wf.getInContextMatchWordCount() %></amb:column>
	        <%} %>
        <%}%>

        <amb:column label="lb_total" width="60px" sortBy="<%=WorkflowComparator.WC_TOTAL%>"><%= wf.getTotalWordCount() %></amb:column>
      </amb:table>
    </td>
  </tr>
<%}
if (noWordCountPermission)
{%>
<%=bundle.getString("lb_no_wordcount_statistic_permission")%>
<%}%>
</TABLE>
<BR><BR>      
<input type="submit" value='<%=bundle.getString("lb_back_to_job_details")%>'>
</FORM>
</DIV>
</BODY>
