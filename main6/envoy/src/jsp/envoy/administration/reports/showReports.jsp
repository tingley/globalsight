<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.foundation.User,
                com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsMainHandler,
                com.globalsight.everest.webapp.pagehandler.administration.reports.CustomExternalReportInfoBean,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.permission.Permission,
                com.globalsight.everest.permission.PermissionSet,
                com.globalsight.everest.company.CompanyWrapper,
                java.util.Date,
                java.util.ResourceBundle,
                java.util.ArrayList,
                java.util.Iterator" session="true" 
%>
<%!
    //colors to use for the table background
    private static final String WHITE_BG         = "#FFFFFF";
    private static final String LT_GREY_BG       = "#EEEEEE";

    //  for "Dell Reviewer PO Report older Issue" "Dell_Review"; "reports.activity"
    private static final String REPORTS_ACTIVITY = SystemConfiguration.getInstance().getStringParameter(SystemConfigParamNames.REPORTS_ACTIVITY);

    // Toggles the background color of the rows used between WHITE and LT_GREY
    private static String toggleBgColor(int p_rowNumber)
    {
        return p_rowNumber % 2 == 0 ? WHITE_BG : LT_GREY_BG;  
    }
%>
<%
    String EMEA = CompanyWrapper.getCurrentCompanyName() + " ";
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title= bundle.getString("lb_reports");
    String pagetitle= bundle.getString("lb_globalsight") 
            + bundle.getString("lb_colon") + " " + title;
    int rowNum = 0;
    Locale theUiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
	//well if the user not have the permission of REPORTS_MAIN,he can not come in ,so the flag will be removed
    //boolean hasAtLeastOneReport = false;
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<style type="text/css">
TR.standardText 
{
    vertical-align: top;
}
</style>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "reports";
    var helpFile = "<%=bundle.getString("help_reports_main_screen")%>";
    var companyJson = "<%=request.getAttribute("companyJson")%>";
    var windownum = 1;
    var oSheng;
    function popup(url, target)
    {
       target = target + parent.windx + windownum++;
       parent.windx++;
       var newurl = url+'&target=' + target+"&companyName="+oSheng.val(); 
       window.open(newurl,target,
       'height=500,width=700,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    };

    function popup2(url, target)
    {
       target = target + parent.windx + windownum++;
       parent.windx++;
       var newurl = url+'&target=' + target+"&companyName="+oSheng.val(); 
       window.open(newurl,target,
       'height=880,width=800,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    };

    function popupExternal(url, target)
    {
       target = target + parent.windx + windownum++;
       parent.windx++;
       var newurl=url+"&companyName="+oSheng.val();
       window.open(newurl,target,
       'height=710,width=700,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    }
    
    $(function(){
   		oSheng = $("#companySel");
    	if(companyJson && companyJson!="null"){
	    	var array=companyJson.split(",");
	    	for(var i=0;i<array.length;i++){
	    		var opt=$("<option value='" + array[i] + "'>" + array[i] + "</option>");
	    		oSheng.append(opt);
	        }
	    	$("#companySpan").show();
    	}
    });   
    
    function fnOpenRecentReports(){    	
  		var url = "/globalsight/ControlServlet?activityName=recentReports";
    	var specs = "width=600,height=490,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no";
    	specs += ",left="+ (screen.width)/20  + ",top=" + (screen.width)/20;
    	controlWindow = window.open(url, "RecentReports", specs);
    	controlWindow.focus(); 
    }
</script>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR><TD WIDTH=850><%=bundle.getString("helper_text_reports")%></TD></TR>
<TR><TD WIDTH=850>&nbsp</TD></TR>
<TR><TD>
<span id="companySpan" style="display:none;"><%=bundle.getString("lb_current_company")%>&nbsp<select id='companySel'><option value=' '>ALL</option></select></span>
<input type="button" style="float:right;" value="Recent Reports" onClick="fnOpenRecentReports();">
</TD></TR>
</TABLE>
<P>
    <TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" WIDTH=850 CLASS="standardText" id="contentTable">
    <% if (userPerms.getPermissionFor(Permission.REPORTS_MAIN)) {%>
        <TR>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("reportName")%></TD>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("reportDesc")%></TD>
        </TR>

    <% if (userPerms.getPermissionFor(Permission.REPORTS_TM)) {
    %>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
                <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=TmReport&act=Create","TmReport")'
                onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=TmReport&act=Create'; return true"><%=bundle.getString("tm_report")%></A>
            </TD>            
            <TD><%=bundle.getString("desc_tmReport")%>
            </TD>
        </TR>
        <% } %>
    <% if (userPerms.getPermissionFor(Permission.REPORTS_WF_STATUS)) { 
    %>        
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD NOWRAP>
                <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=Prepare","WorkflowStatus")'
                onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=WorkflowStatus&act=Prepare'; return true"><%=bundle.getString("workflow_status")%></A>
            </TD>
            <TD><%=bundle.getString("desc_workflowStatus")%>
            </TD>
        </TR>
        <% } %>
    <% if (userPerms.getPermissionFor(Permission.REPORTS_JOB_DETAILS)) { 
    %>                
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
                <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=JobDetails&act=Prepare","JobDetails")'
                onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=JobDetails&act=Prepare'; return true"><%=bundle.getString("job_details")%></A></TD>
            <TD><%=bundle.getString("desc_jobDetails")%>
            </TD>
        </TR>
        <% } %>
    <% if (userPerms.getPermissionFor(Permission.REPORTS_AVG_PER_COMP)) { 
    %>                        
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
                <A CLASS=standardHREF HREF='javascript: popup("/globalsight//TranswareReports?reportPageName=AvgPerComp&act=Prepare","AvgPerComp")'
                onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=AvgPerComp&act=Prepare'; return true"><%=bundle.getString("avg_per_comp")%></A>
            </TD>
            <TD><%=bundle.getString("desc_avgPerComp")%>
            </TD>
        </TR>
        <% } %>        
    <% if (userPerms.getPermissionFor(Permission.REPORTS_MISSING_TERMS)) { 
    %> 
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
                <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=MissingTerms&act=Prepare","MissingTerms")'
                onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=MissingTerms&act=Prepare'; return true"><%=bundle.getString("missing_terms")%></A>
            </TD>
            <TD><%=bundle.getString("desc_missingTerms")%>
            </TD>
        </TR>
        <% } %>        
    <% if (userPerms.getPermissionFor(Permission.REPORTS_TERM_AUDIT)) { 
    %>                                        
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
                <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=TermAudit&act=Prepare","TermAudit")'
                onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=TermAudit&act=Prepare'; return true"><%=bundle.getString("term_audit")%></A>
            </TD>
            <TD><%=bundle.getString("desc_termAudit")%>
            </TD>
        </TR>
        <% } %>

<%
	String reportUrl="";
	String reportName="";
	String reportDesc="";
	String reportWindowName="";
%>

<% if (userPerms.getPermissionFor(Permission.REPORTS_COMMENT)) {
        reportUrl="/globalsight/ControlServlet?activityName=xlsReportComment";
        reportWindowName="CommentsReport";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=bundle.getString("comments")%>
             </A>
            </TD>
            <TD><%=bundle.getString("comments_desc")%></TD>
        </TR>
        <% } %>
    <% if (userPerms.getPermissionFor(Permission.REPORTS_DELL_JOB_STATUS)) {
    
        reportUrl="/globalsight/ControlServlet?activityName=xlsReportJobStatus";
        reportName= EMEA + bundle.getString("job_status");
        reportWindowName= "JobStatus";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=bundle.getString("job_status_desc")%></TD>
        </TR>
        <% } %>      
        
    <% if (userPerms.getPermissionFor(Permission.REPORTS_DELL_ACT_DUR)) {
    %>
<%
            reportUrl="/globalsight/ControlServlet?activityName=xlsReportActivityDuration";
            reportName=EMEA + bundle.getString("activity_duration");
            reportWindowName="ActivityDuration";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=bundle.getString("activity_duration_desc")%></TD>
        </TR>
        <% } %>        
    <% if (userPerms.getPermissionFor(Permission.REPORTS_DELL_ONLINE_JOBS)) {
    %>
<%
            reportUrl="/globalsight/ControlServlet?activityName=xlsReportOnlineJobs";
            reportName=EMEA + bundle.getString("online_jobs");
            reportWindowName="OnlineJobs";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=bundle.getString("online_jobs_desc")%></TD>
        </TR>
<% } %>
    <% if (userPerms.getPermissionFor(Permission.REPORTS_DELL_ONLINE_JOBS_FOR_IP_TRANSLATOR)) {
        %>
    <%
                reportUrl="/globalsight/ControlServlet?activityName=xlsReportOnlineJobsForIPTranslator";
                reportName=EMEA + bundle.getString("online_jobs_for_ip_translator");
                reportWindowName="OnlineJobsForIPTranslator";
    %>
            <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
                <TD>
              <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
                 onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
                 </A>
                </TD>
                <TD><%=bundle.getString("online_jobs_for_ip_translator_desc")%></TD>
            </TR>
    <% }
    if (userPerms.getPermissionFor(Permission.REPORTS_DELL_ONLINE_REVIEW_STATUS))
    {

        reportUrl="/globalsight/ControlServlet?activityName=xlsReportOnlineRevStatus";
        reportName=EMEA + bundle.getString("online_review_status");
        reportWindowName="OnlineRevStatus";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=bundle.getString("online_review_status_desc")%></TD>
        </TR>
        
<% }

    // GBS-576, add "File List" report
    if (userPerms.getPermissionFor(Permission.REPORTS_DELL_FILE_LIST))
    {

        reportUrl="/globalsight/ControlServlet?activityName=xlsReportFileList";
        reportName=EMEA + bundle.getString("file_list_report");
        reportWindowName="FileList";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=bundle.getString("file_list_report_desc")%></TD>
        </TR>        
        
<% }
    if (userPerms.getPermissionFor(Permission.REPORTS_DELL_VENDOR_PO))
    {

        reportUrl="/globalsight/ControlServlet?activityName=xlsReportVendorPO";
        reportName=EMEA + bundle.getString("vendor_po");
        reportDesc=EMEA + bundle.getString("vendor_po_desc");
        reportWindowName="VendorPO";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=reportDesc%></TD>
        </TR>
<% }

    // Old "Dell_Review" Report
    if (userPerms.getPermissionFor(Permission.REPORTS_DELL_REVIEWER_VENDOR_PO) 
            && REPORTS_ACTIVITY != null && REPORTS_ACTIVITY.trim().length() > 1)
    {

        reportUrl="/globalsight/ControlServlet?activityName=xlsReportReviewerVendorPO";
        reportName=EMEA + bundle.getString("reviewer_vendor_po");
        reportDesc=EMEA + bundle.getString("reviewer_vendor_po_desc");
        reportWindowName="ReviewerVendorPO";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=reportName%>
             </A>
            </TD>
            <TD><%=reportDesc%></TD>
        </TR>
		<% } %>

		<!-- Summary Reports -->
		<amb:permission name="<%=Permission.REPORTS_SUMMARY%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportSummary","SummaryReport")'
             	onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportSummary'; return true"><%=EMEA + bundle.getString("report_summary_title")%>
             </A>
            </TD>
            <TD><%=bundle.getString("report_summary_desc")%></TD>
        </TR>
		</amb:permission>

		<amb:permission name="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportLanguageSignOff","LanguageSignOff")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportLanguageSignOff'; return true"><%=bundle.getString("review_reviewers_comments")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_reviewers_comments_desc")%></TD>
        </TR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF_SIMPLE%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportLanguageSignOffSimple","LanguageSignOffSimple")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportLanguageSignOffSimple'; return true"><%=bundle.getString("review_reviewers_comments_simple")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_reviewers_comments_simple_desc")%></TD>
        </TR>
        </amb:permission>

		<amb:permission name="<%=Permission.REPORTS_COMMENTS_ANALYSIS%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportCommentsAnalysis","CommentsAnalysis")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportCommentsAnalysis'; return true"><%=bundle.getString("review_comments")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_comments_desc")%></TD>
        </TR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.REPORTS_SCORECARD%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportScorecard","Scorecard")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportScorecard'; return true"><%=bundle.getString("scorecard_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("scorecard_report_desc")%></TD>
        </TR>
        </amb:permission>
        
        <!--    Character count report Start-->
        <amb:permission name="<%=Permission.REPORTS_CHARACTER_COUNT%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
             <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportCharacterCount","CharacterCount")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportCharacterCount'; return true"><%=bundle.getString("character_count_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("character_count_report_desc")%></TD>
        </TR>
        </amb:permission>
        <!--    Character count report End-->
        <amb:permission name="<%=Permission.REPORTS_POST_REVIEW_QA%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
           	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportPostReviewQA","PostReviewQA")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportPostReviewQA'; return true"><%=bundle.getString("review_post_review_QA_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_post_review_QA_report_desc")%></TD>
        </TR>
        </amb:permission>
        
		<amb:permission name="<%=Permission.REPORTS_TRANSLATIONS_EDIT%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
           	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportTranslationsEdit","TranslationsEdit")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportTranslationsEdit'; return true"><%=bundle.getString("review_translations_edit_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_translations_edit_report_desc")%></TD>
        </TR>
        </amb:permission>
        
        	<amb:permission name="<%=Permission.REPORTS_TRANSLATIONS_VERIFICATION%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
           	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportTranslationVerification","TranslationVerificationReport")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportNew'; return true"><%=bundle.getString("translation_verification_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_translation_verification_report_desc")%></TD>
        </TR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.REPORTS_TRANSLATION_PROGRESS%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
           	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=xlsReportTranslationProgress","TranslationProgress")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=xlsReportTranslationProgress'; return true"><%=bundle.getString("review_translation_progress_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("review_translation_progress_report_desc")%></TD>
        </TR>
        </amb:permission>
                

    <% if (userPerms.getPermissionFor(Permission.REPORTS_SLA)) {
    %>
<%
            reportUrl="/globalsight/ControlServlet?activityName=xlsReportSlaPerformance";
            reportWindowName="TranslationSLAPerformance";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=reportUrl%>","<%=reportWindowName%>")'
             onMouseOver="window.status='<%=reportUrl%>'; return true"><%=bundle.getString("translation_sla_performance")%>
             </A>
            </TD>
            <TD><%=bundle.getString("translation_sla_performance_desc")%></TD>
        </TR>
        <% } %>
        
<!--  Customize Reports -->
    <% if (userPerms.getPermissionFor(Permission.REPORTS_CUSTOMIZE)) {
    %>
<%
        String customizeReportUrl="/globalsight/ControlServlet?activityName=customizeReports";
        String customizeReportWindowName="CustomizeReports";
%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=customizeReportUrl%>","<%=customizeReportWindowName%>")'
             onMouseOver="window.status='<%=customizeReportUrl%>'; return true"><%=bundle.getString("customize_reports_implementation")%>
             </A>
            </TD>
            <TD><%=bundle.getString("customize_reports_implementation_desc")%></TD>
        </TR>    
    <% } %>     
 <!-- Implemented Commented Check --> 
    <amb:permission name="<%=Permission.REPORTS_IMPLEMENTED_COMMENTS_CHECK%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
           	 <A CLASS=standardHREF HREF='javascript: popupExternal("/globalsight/ControlServlet?activityName=implementedCommentsCheck","ImplementedCommentsCheck")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=implementedCommentsCheck'; return true"><%=bundle.getString("implemented_comments_check_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("implemented_comments_check_report_desc")%></TD>
        </TR>
     </amb:permission>
     
     <amb:permission name="<%=Permission.JOB_ATTRIBUTE_REPORT%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
           	 <A CLASS=standardHREF HREF='javascript: popup2("/globalsight/ControlServlet?activityName=jobAttributeReport&action=create","JobAttributeReport")'
             onMouseOver="window.status='/globalsight/ControlServlet?activityName=jobAttributeReport'; return true"><%=bundle.getString("lb_job_attribute_report")%>
             </A>
            </TD>
            <TD><%=bundle.getString("lb_job_attribute_report_desc")%></TD>
        </TR>
     </amb:permission>
<% } %>

<% if (userPerms.getPermissionFor(Permission.REPORTS_CUSTOM)) {
%>
        <TR>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("customReportName")%></TD>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("customReportAlias")%></TD>
        </TR>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=CostsByLocaleReport&act=Create","customReport")'
             onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=CostsByLocaleReport&act=Create'; return true"><%=bundle.getString("standard_reports_costs_Locale")%>
             </A>
            </TD>
            <TD><%=bundle.getString("standard_reports_costs_Locale_desc")%></TD>
        </TR>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popup("/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=Create","customReport")'
             onMouseOver="window.status='/globalsight/TranswareReports?reportPageName=TaskDurationReport&act=Create'; return true"><%=bundle.getString("standard_reports_task_duration")%>
             </A>
            </TD>
            <TD><%=bundle.getString("standard_reports_task_duration_desc")%></TD>
        </TR>
<% } %>

<% if (userPerms.getPermissionFor(Permission.REPORTS_CUSTOM_EXTERNAL)) {%>
<!-- Custom External Reports (URLs) -->
<%
ArrayList customExternalReports = (ArrayList) request.getAttribute(
    ReportsMainHandler.ATTR_CUSTOM_EXTERNAL_REPORTS);

if (customExternalReports.size() > 0)
{
%>
        <TR>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("customExternalReportName")%></TD>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("customExternalReportDesc")%></TD>
        </TR>
<%
    rowNum = 0;
    Iterator iter = customExternalReports.iterator();
    while (iter.hasNext())
    {
        CustomExternalReportInfoBean info = (CustomExternalReportInfoBean) iter.next();
        String url = info.getUrl();
        %>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD>
          <A CLASS=standardHREF HREF='javascript: popupExternal("<%=url%>","custom<%=info.getNumber()%>")'
             onMouseOver="window.status='<%=url%>'; return true"><%=info.getName()%>
             </A>
            </TD>
            <TD><%=info.getDesc()%></TD>
        </TR>
<%
    }
%>
<%}%>

</TABLE>
<% }%>


</DIV>
</BODY>
</HTML>

