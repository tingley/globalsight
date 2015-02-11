<%@page import="com.globalsight.everest.webapp.pagehandler.tasks.DownloadOfflineFilesConfigHandler"%>
<%@page import="com.globalsight.util.StringUtil"%>
<%@page import="com.globalsight.ling.common.URLEncoder"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.config.UserParamNames,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.edit.offline.OfflineEditManager,
            com.globalsight.config.UserParameter,
            com.globalsight.everest.edit.EditHelper,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.glossaries.GlossaryFile,
            com.globalsight.everest.page.Page,
	        com.globalsight.everest.page.PageWordCounts,
            com.globalsight.everest.page.PrimaryFile,
            com.globalsight.everest.page.SourcePage,
            com.globalsight.everest.page.TargetPage,
            com.globalsight.everest.page.UnextractedFile,
            com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.util.comparator.TargetPageComparator,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState,
            com.globalsight.everest.webapp.pagehandler.offline.download.DownloadPageHandler,
            com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.everest.workflowmanager.Workflow,
            com.globalsight.util.AmbFileStoragePathUtils,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.SortUtil,
            com.globalsight.everest.servlet.util.SessionManager,
            java.text.DateFormat,
            java.text.NumberFormat,
            org.json.JSONObject,
            java.io.File,
            java.util.*"
    session="true"
%>
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="detail" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="comment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="startdownload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadreport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadreport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager =
        (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
    HashMap optionsHash = (HashMap) sessionManager.getAttribute("optionsHash");
    JSONObject dwnldOpt=new JSONObject(optionsHash);

    String urlDone = done.getPageURL() +
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_DONE;

    String urlStartDownload = startdownload.getPageURL() +
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_START_DOWNLOAD;

    // links
    String detailUrl = detail.getPageURL();
    String downloadUrl = download.getPageURL();
    String uploadUrl = upload.getPageURL();
    String commentUrl = comment.getPageURL();
    String cancelUrl = cancel.getPageURL();
    
    String downloadReportUrl = downloadreport.getPageURL();
    String uploadReportUrl = uploadreport.getPageURL();

    // labels
    String lb_OK = bundle.getString("lb_ok");
    String pagetitle = bundle.getString("lb_globalsight")
                     + bundle.getString("lb_colon") + " "
                     + bundle.getString("lb_my_activities")
                     + bundle.getString("lb_colon") + " "
                     + bundle.getString("lb_activity_details")
                     + bundle.getString("lb_colon") + " "
                     + bundle.getString("lb_tab_download");
    String title = bundle.getString("lb_tab_download");
    String lbDetails = bundle.getString("lb_details");
    String lbComments = bundle.getString("lb_comments");
    String lbStartDownload = bundle.getString("lb_download_start");
    String lbWorkoffline = bundle.getString("lb_work_offline");
    String lbDownload = bundle.getString("lb_tab_download");
    String lbUpload = bundle.getString("lb_tab_upload");
    
    String lbDownloadReport = bundle.getString("lb_download_report");
    String lbUploadReport = bundle.getString("lb_upload_report");
    
    String labelAddComment = bundle.getString("action_add_comment");
    String lbCancel = bundle.getString("lb_cancel");
    String lb_refresh = bundle.getString("lb_refresh");
    String lb_downloading = bundle.getString("lb_downloading");
    String lb_search_msg = "Please wait. Downloading files...";
    String subTitle = bundle.getString("lb_my_activities")
                      + bundle.getString("lb_colon") + " "
                      + bundle.getString("lb_activity_details")
                      + bundle.getString("lb_colon") + " ";
    String activityContent = bundle.getString("lb_primary_target_files");
    String selectPTFiles = bundle.getString("lb_select_primary_target_files");
    String selectGlossaries = bundle.getString("lb_select_glossary_files");
    String selectSTFFiles = bundle.getString("lb_select_secondary_target_files");
    String selectPSFiles = bundle.getString("lb_select_primary_source_files");
    String checkAllLinkText = bundle.getString("lb_check_all");
    String clearAllLinkText = bundle.getString("lb_clear_all");

    String downloadOptions = bundle.getString("lb_download_options");
    String STFHeading = bundle.getString("lb_secondary_target_file_heading");
    String PSHeading = bundle.getString("lb_primary_source_file_heading");
    String glossaryHeading = bundle.getString("lb_glossary_heading");
    String lastModified = bundle.getString("lb_glossary_last_modified");
    String stfLastModifiedBy = bundle.getString("lb_stf_last_modified_by");
    String psLastModifiedBy = bundle.getString("lb_primary_source_last_modified_by");
    String format = bundle.getString("lb_format") +
                    bundle.getString("lb_colon");
    String placeholderFormat = bundle.getString("lb_placeholder_format");
    String exactMatchesEditable = bundle.getString("lb_exact_matches_editable");
    String noGlossaries = bundle.getString("lb_no_glossaries");
    String noSTFFiles = bundle.getString("lb_no_secondary_target_files");
    String noPriSrcFiles = bundle.getString("lb_no_primary_source_files");
    String labelActivity = bundle.getString("lb_activity") + bundle.getString("lb_colon");
    String labelJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");
    String labelResInsertion = bundle.getString("lb_resource_linking");
    String labelResInsertionNote = bundle.getString("lb_resource_linking_note");
    String labelTerminology = bundle.getString("lb_terminology");
    

    // error message
    String optionNotSelected = bundle.getString("jsmsg_option_not_selected");
    String pageNotSelected = bundle.getString("jsmsg_page_not_selected");

    // control name
    String formatSelector = OfflineConstants.FORMAT_SELECTOR;
    String editorSelector = OfflineConstants.EDITOR_SELECTOR;
    String encodingSelector = OfflineConstants.ENCODING_SELECTOR;
    String ptagSelector = OfflineConstants.PTAG_SELECTOR;
    String tmEditTypeSelector = OfflineConstants.TM_EDIT_TYPE;
    String resInsertionSelector = OfflineConstants.RES_INS_SELECTOR;
    String resTermSelector = OfflineConstants.TERM_SELECTOR;
    String pageCheckBoxes = OfflineConstants.PAGE_CHECKBOXES;
    String glossaryCheckBoxes = OfflineConstants.GLOSSARY_CHECKBOXES;
    String stfCheckBoxes = OfflineConstants.STF_CHECKBOXES;
    String priSrcCheckBoxes = OfflineConstants.PRI_SOURCE_CHECKBOXES;

    // Option value names
    //   format selector
    String formatValueRtfListView = OfflineConstants.FORMAT_RTF;
    String formatValueRtfListViewTradosOptimized = OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED;
	
    String formatXlfName12 = OfflineConstants.FORMAT_XLF_NAME_12;
	String formatXlfValue12 = OfflineConstants.FORMAT_XLF_VALUE_12;
	
	String formatTTXName = OfflineConstants.FORMAT_TTX_NAME;
	String formatTTXValue = OfflineConstants.FORMAT_TTX_VALUE;
	
	String formatOmegaTName = OfflineConstants.FORMAT_OMEGAT_NAME;
	String formatOmegaTValue = OfflineConstants.FORMAT_OMEGAT_VALUE;
    
    //   ptag format selector
    String ptagValueCompact = OfflineConstants.PTAG_COMPACT;
    String ptagValueVerbose = OfflineConstants.PTAG_VERBOSE;
    //   resource link selector
    String resInsertValueAtns = OfflineConstants.RES_INS_ATNS;
    String resInsertValueNone = OfflineConstants.RES_INS_NONE;
    String resValueTmx14b = OfflineConstants.RES_INS_TMX_14B;
    String resValueTmxBoth = OfflineConstants.RES_INX_TMX_BOTH;
    
    // Option text names
    //   format selector
    String formatStartHere = bundle.getString("lb_start_here");
    String formatRtfListView = bundle.getString("lb_rtf_listview");
    String formatRtfListViewTrados = bundle.getString("lb_rtf_trados");
    String formatRtfListViewTradosOptimized = bundle.getString("lb_rtf_trados_optimized");
    String formatTextListView = bundle.getString("lb_text");
    String formatRtfParaView = bundle.getString("lb_rtf_paraview_1");

    //   ptag format selector
    String ptagTextCompact = bundle.getString("lb_compact");
    String ptagTextVerbose = bundle.getString("lb_verbose");
    //   resource link selector
    String resInsertTextAtns = bundle.getString("lb_make_res_atns");
    String resInsertTextNone = bundle.getString("lb_make_res_none");
    String resTermTxt = "TEXT";
    String resTmx14b = bundle.getString("lb_make_res_tmx_14b");
    String resTmxBoth = bundle.getString("lb_make_res_tmx_both");

    
    // Glossaries  - list of all glossaries
    GlossaryState glossaryState
        = (GlossaryState)session.getAttribute(OfflineConstants.DOWNLOAD_GLOSSARY_STATE);
    ArrayList glossaryList = glossaryState.getGlossaries();

    // get date/time format
    // NOTE: The system4 standard is to **not** format date and time according
    // to the UILOCALE as in (Locale)session.getAttribute(WebAppConstants.UILOCALE)
    DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(
        DateFormat.SHORT, DateFormat.SHORT, Locale.US  );
    NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    numberFormat.setMaximumFractionDigits(1);

    Task task = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
    Workflow workflow = task.getWorkflow();

    // get the list of UnextractedPrimaryTargetFiles
    List U_PTFList = workflow.getTargetPages(PrimaryFile.UNEXTRACTED_FILE);
    SortUtil.sort(U_PTFList, new TargetPageComparator(TargetPageComparator.EXTERNALPAGEID, Locale.getDefault()));
    // get the list of ExtractedPrimaryTargetFiles
    List E_PTFList = workflow.getTargetPages(PrimaryFile.EXTRACTED_FILE);
    // get the list of SecondaryTargetFiles
    Set<SecondaryTargetFile> STFList = workflow.getSecondaryTargetFiles();


    // Get data for the Hints table
    String activityName = task.getTaskName();
    String jobName = task.getJobName();
    //boolean review_only = task.isType(Task.TYPE_REVIEW);

    // Get cookie names
    String cookieNameFileFormat = OfflineConstants.COOKIE_FILE_FORMAT;
    String cookieNamePtagFormat = OfflineConstants.COOKIE_PTAG_FORMAT;
    String cookieNameTMEditType = OfflineConstants.COOKIE_TM_EDIT_TYPE;
    String cookieNameResInsMode = OfflineConstants.COOKIE_RES_INS_MODE;

    boolean hasExtractedFiles = false;

    UserParameter param = PageHandler.getUserParameter(session,
        UserParamNames.PAGENAME_DISPLAY);
    boolean showShortNames =
        param.getValue().equals(UserParamNames.PAGENAME_DISPLAY_SHORT);
    
    String TMEditTypeString = (String)session.getAttribute(OfflineConstants.DOWNLOAD_TM_EDIT_TYPE);
    if (StringUtil.isEmpty(TMEditTypeString) || "no".equalsIgnoreCase(TMEditTypeString))
        TMEditTypeString = "0";
    int TMEditType = Integer.parseInt(TMEditTypeString);
    
    String userOptionOfTMEditString = (String)optionsHash.get(DownloadOfflineFilesConfigHandler.DOWNLOAD_OPTION_TM_EDIT_TYPE);
    int userOptionOfTMEdit = Integer.parseInt(userOptionOfTMEditString);
    
    String path = "";
%>
<HTML>
<HEAD>
<TITLE><%= pagetitle %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery/jquery-latest.min.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/downloadOpt.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "myActivitiesDownload";
var helpFile = "<%=bundle.getString("help_download")%>";
var dwnldOpt=<%=dwnldOpt%>;
function doCheckAll(checkBoxName)
{
    if (document.layers) {
        theForm = document.contentLayer.document.downloadForm;
    }
    else {
        theForm = document.all.downloadForm;
    }
    for (var i = 0; i < theForm.length; i++)
    {
        if (theForm.elements[i].type == "checkbox" &&
            theForm.elements[i].name == checkBoxName)
        {
            theForm.elements[i].checked = true;
        }
    }

    if (checkBoxName == "<%= pageCheckBoxes %>")
    {
        disablePTFOptions(false);
    }

    return false;
}

function doClearAll(checkBoxName)
{
    if (document.layers) {
        theForm = document.contentLayer.document.downloadForm;
    }
    else {
        theForm = document.all.downloadForm;
    }

    for (var i = 0; i < theForm.length; i++)
    {
        if (theForm.elements[i].type == "checkbox" &&
            theForm.elements[i].name == checkBoxName)
        {
            theForm.elements[i].checked = false;
        }
    }

    if (checkBoxName == "<%= pageCheckBoxes %>")
    {
        disablePTFOptions(true);
    }

    return false;
}


function updatePTFControlState()
{
    if (document.layers) {
        theForm = document.contentLayer.document.downloadForm;
    }
    else {
        theForm = document.all.downloadForm;
    }

    if(isPrimaryTargetChecked(theForm))
    {
        disablePTFOptions(false);
    }
    else
    {
       disablePTFOptions(true);
    }
}

function disablePTFOptions(p_state)
{
    state = p_state;

    if(hasExtractedFiles)
    {
        if (document.layers)
        {
            theForm = document.contentLayer.document.downloadForm;
        }
        else
        {
            theForm = document.all.downloadForm;
        }
        theForm.formatSelector.disabled = state;
    	var formatSelect = theForm.formatSelector;
    	if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>" ||
    	    formatSelect.options[formatSelect.selectedIndex].value == "<%= formatTTXValue %>" ||
    	    formatSelect.options[formatSelect.selectedIndex].value == "<%= formatOmegaTValue %>" )
    	{
    	    theForm.ptagSelector.disabled = true;
    	}
    	else
    	{
    	    theForm.ptagSelector.disabled = state;
    	}
        theForm.resInsertionSelector.disabled = state;
        
         document.getElementById("changeCreationIdForMT").disabled = state;
     
         theForm.tmEditTypeSelector.disabled = state;
         theForm.resTermSelector.disabled = state;

	}
}

function isPrimaryTargetChecked(formSent)
{
    var checked = false;
    if(formSent.<%= pageCheckBoxes %> != null)
    {
        if (formSent.<%= pageCheckBoxes %>.value)
        {
            if(formSent.<%= pageCheckBoxes %>.checked)
            {
                checked = true;
            }
        }
        else
        {
            for (var i = 0; i < formSent.<%= pageCheckBoxes %>.length; i++)
            {
                if (formSent.<%= pageCheckBoxes %>[i].checked == true)
                {
                    checked = true;
                    break;
                }
            }
        }
    }
    return checked;
}

function isPrimarySourceChecked(formSent)
{
    var checked = false;

    if(formSent.<%= priSrcCheckBoxes %> != null )
    {
        if (formSent.<%= priSrcCheckBoxes %>.value)
        {
            if(formSent.<%= priSrcCheckBoxes %>.checked)
            {
                checked = true;
            }
        }
        else
        {
            for (var i = 0; i < formSent.<%= priSrcCheckBoxes %>.length; i++)
            {
                if (formSent.<%= priSrcCheckBoxes %>[i].checked == true)
                {
                    checked = true;
                    break;
                }
            }
        }
    }
    return checked;
}

function isSupportFileChecked(formSent)
{
    var checked = false;

<%
    if (glossaryList != null && glossaryList.size() > 0)
    {
%>
    if(formSent.<%= glossaryCheckBoxes %> != null )
    {
        if (formSent.<%= glossaryCheckBoxes %>.value)
        {
            if(formSent.<%= glossaryCheckBoxes %>.checked)
            {
                checked = true;
            }
        }
        else
        {
            for (var i = 0; i < formSent.<%= glossaryCheckBoxes %>.length; i++)
            {
                if (formSent.<%= glossaryCheckBoxes %>[i].checked == true)
                {
                    checked = true;
                    break;
                }
            }
        }
     }
<%
    }
%>
    return checked;
}


function isSecondaryTargetChecked(formSent)
{
    var checked = false;

    if (formSent.<%= stfCheckBoxes %> != null)
    {
        if (formSent.<%= stfCheckBoxes %>.value)
        {
            if(formSent.<%= stfCheckBoxes %>.checked)
            {
                checked = true;
            }
        }
        else
        {
            for (var i = 0; i < formSent.<%= stfCheckBoxes %>.length; i++)
            {
                if (formSent.<%= stfCheckBoxes %>[i].checked == true)
                {
                    checked = true;
                    break;
                }
            }
        }
     }
     return checked;
}

function optionTest(formSent)
{
    var pageChecked = isPrimaryTargetChecked(formSent);
    <%
    if((STFList != null) && (STFList.size() > 0))
    {
    %>
    pageChecked = pageChecked || isSecondaryTargetChecked(formSent);
    <%
    }
    if((U_PTFList != null) && (U_PTFList.size() > 0))
    {
    %>
    pageChecked = pageChecked || isPrimarySourceChecked(formSent);
    <%
    }
    if (glossaryList != null && glossaryList.size() > 0)
    {
    %>
    pageChecked = pageChecked || isSupportFileChecked(formSent);
    <%
    }
    %>

    if(!pageChecked ) //&& !supportChecked && !stfChecked && !priSrcFiles)
    {
        alert("<%= pageNotSelected %>");
        return(false);
    }
    else
    {
        return(true);
    }

}

function dsubmit()
{
    if (document.layers)
    {
        theForm = document.contentLayer.document.downloadForm;
        theImage = document.contentLayer.document.selectAll;
    }
    else
    {
        theForm = document.all.downloadForm;
        theImage = document.all.selectAll;
    }

    if (optionTest(theForm))
    {
        theForm.submit();
    }
}








//not sure where it is used
function doFinished(urlSent) {
    if (confirm("<%= bundle.getString("jsmsg_my_activities_finished") %>")) document.location.replace(urlSent);
}

//not sure where it is used
function doOk()
{
    window.location.href = "<%=urlDone%>";
}


//for GBS-2599
function handleSelectAll(selectAll,theBoxes) {
		if (selectAll.checked) {
			doCheckAll(theBoxes);
	    }
	    else {
			doClearAll(theBoxes); 
	    }
}
</SCRIPT>
</HEAD>

<BODY onload="init()" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=labelJobName%> <%=jobName%></SPAN>
<BR>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=550>
      <%=bundle.getString("helper_text_download")%>
    </TD>
  </TR>
</TABLE>
<P>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=detailUrl%>"><%=lbDetails%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
        <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_VIEW%>">
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=commentUrl%>"><%=lbComments%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
        </amb:permission>
        <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=downloadUrl%>"><%=lbWorkoffline%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
</TR>
</TABLE>
<!-- End Tabs table -->
<p>
<!-- Second Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
        <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=downloadUrl%>"><%=lbDownload%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=uploadUrl%>"><%=lbUpload%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <amb:permission name="<%=Permission.REPORTS_TRANSLATIONS_EDIT%>">
        <TD WIDTH="2"></TD>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=downloadReportUrl%>"><%=lbDownloadReport%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=uploadReportUrl%>"><%=lbUploadReport%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        </amb:permission>
</TR>
</TABLE>
<!-- End Second Tabs table -->
</TD>



<TD ALIGN="RIGHT" VALIGN="BOTTOM" NOWRAP></TD>
</TR>

<TR>
<TD CLASS="tableHeadingBasic" COLSPAN="2" HEIGHT=1><IMG SRC="/globalsight/images/spacer.gif" HEIGHT="1" WIDTH="1"></TD>
</TR>

<TR>
<TD COLSPAN="2">&nbsp;</TD>
</TR>

<TR>
<TD COLSPAN="2">

<!-- Lower table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD VALIGN="TOP">


<P>

  <FORM NAME="downloadForm" ACTION="<%=urlStartDownload%>" METHOD="POST" >
    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width="100%" >
      <TR>
        <TD CLASS="tableHeadingBasic" style="width:1630px; *width:auto;" COLSPAN="3"><input type="checkbox" onclick="handleSelectAll(this,'<%=pageCheckBoxes%>')" checked="true"/>&nbsp;<%= activityContent %></TD>
      </TR>
      <TR>
        <TD VALIGN="TOP" width="700px">
        <%-- // ****************************
             // TABLE: PRIMARY TARGET FILES
             // **************************** --%>
          <TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" CLASS="standardText" width="700px">
            <TR>
                <TD COLSPAN="3" NOWRAP>
                <SPAN CLASS="standardTextBold"><%= selectPTFiles %></SPAN>
                </TD>
            </TR>
<%
    // *** LIST: EXTRACTED Primary targets ************************* //
    Iterator it = E_PTFList.iterator();
    while(it.hasNext())
    {
        hasExtractedFiles = true;
        TargetPage tp = (TargetPage)it.next();
		
		PageWordCounts pageWordCountsObj = tp.getWordCount();
		int totalWords = pageWordCountsObj.getTotalWordCount();
		if(totalWords == 0)
		{
			continue;
		}
        String name = tp.getExternalPageId();
        if(showShortNames)
        {
            name = DownloadPageHandler.getFileNameShort(name);
        }
        else
        {
        	name = DownloadPageHandler.getFileName(name);
        }
%>
            <TR VALIGN="TOP">
              <TD>
                <%-- NOTE: For both Extracted and Unextracted downloads, we
                           use the source pageId and the target locale to get
                           either the TUVs from the TuvManager or the
                           unextracted TargetPage from the PageManager.
                           (DownloadApi handles these conversions). --%>
                  <INPUT TYPE="checkbox" CHECKED="true"
                    NAME="<%= pageCheckBoxes %>" CLASS="formFields"
                    VALUE="<%= tp.getSourcePage().getIdAsLong() %>"
                    ID="page" onclick="updatePTFControlState();">
              </TD>
              <TD>
                   <IMG SRC="/globalsight/images/file_extracted.gif" ALT="<%= bundle.getString("lb_file_extracted") %>" WIDTH=13 HEIGHT=15>
              </TD>
              <TD style="width:700px;word-wrap:break-word;word-break:break-all">
<%				out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:700px\'>\");}</SCRIPT>"); %>
                  <SPAN CLASS="standardText" TITLE="<%= tp.getExternalPageId() %>"><%= name %></SPAN>
<%				out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>"); %>
              </TD>
            </TR>
<%
    }
%>
<%
    // ** LIST: UN-EXTRACTED Primary targets *************************//
    it = U_PTFList.iterator();
    while(it.hasNext())
    {
        TargetPage tp = (TargetPage)it.next();
        UnextractedFile uf = (UnextractedFile)tp.getPrimaryFile();
        StringBuffer info = new StringBuffer();
        StringBuffer url = new StringBuffer();
        url.append(WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING);
        url.append(uf.getStoragePath());
        String name = uf.getStoragePath();
        if(showShortNames)
        {
            name = DownloadPageHandler.getFileNameShort(name);
        }
        else
        {
        	name = DownloadPageHandler.getFileName(name);
        }
        String id = tp.getIdAsLong().toString();
        Date date = uf.getLastModifiedDate();
        String dateStr = (dateTimeFormat.format(date));
        String lastModifierUserName = null;
        try
        {
            User user = ServerProxy.getUserManager().getUser(uf.getLastModifiedBy());
            lastModifierUserName = user.getUserName();
        }
        catch(Exception e)
        {
            lastModifierUserName = "unknown";
        }
        long size = uf.getLength();
        size = size < 3 ? 0 : size; // adjust
        if(size != 0)
        {
            long r = size%1024;
            size = (r!=0) ? ((size/1024)+1) : size;  //round up
        }
        String sizeStr = numberFormat.format(size);
        info.append(lastModified);
        info.append(" ");
        info.append(dateStr);
        info.append(" - ");
        info.append(sizeStr);
        info.append("k");
        
%>
            <TR VALIGN="TOP">
              <TD>
                <%-- NOTE: For both Extracted and Unextracted downloads, we
                           use the source pageId and the target locale to get
                           either the TUVs from the TuvManager or the
                           unextracted TargetPage from the PageManager.
                           (DownloadApi handles these conversions). --%>
                  <INPUT TYPE="checkbox" CHECKED="true"
                    NAME="<%= pageCheckBoxes %>" CLASS="formFields"
                    VALUE="<%= tp.getSourcePage().getIdAsLong() %>"
                    ID="page" onclick="updatePTFControlState();" >
              </TD>
              <TD>
                  <IMG SRC="/globalsight/images/file_unextracted.gif" ALT="<%= bundle.getString("lb_file_unextracted") %>" WIDTH=13 HEIGHT=15>
              </TD>
              <TD >
                  <A CLASS="standardHREF" target="_blank" href="<%= URLEncoder.encodeUrlStr(url.toString()) %>">
                   <SPAN CLASS="standardText" TITLE="<%=uf.getStoragePath()%>"><%= name %></SPAN></A>
                  <BR><SPAN CLASS="glossaryDateSize">
                  <%= info.toString() %><BR><%=stfLastModifiedBy%> <%= lastModifierUserName %></SPAN>
              </TD>
            </TR>
<%
    }
%>
            <!--for gbs-2599
			TR>
            <TD COLSPAN=3><A CLASS="standardHREF" HREF="#"
                            onClick="doCheckAll('<%= pageCheckBoxes %>'); return false;"
                            onFocus="this.blur();"><%= checkAllLinkText %></A> |
                        <A CLASS="standardHREF" HREF="#"
                            onClick="doClearAll('<%= pageCheckBoxes %>'); return false;"
                            onFocus="this.blur();"><%= clearAllLinkText %></A>
            </TD>
            </TR-->
          </TABLE><%--! END: PRIMARY TARGET FILE LIST ************************* --%>
<% if(E_PTFList != null || E_PTFList.size() > 0 )
{
%>
        </TD>
        <TD WIDTH="30">&nbsp;</TD>
        <TD VALIGN="TOP">
        <%-- // *************************
             // TABLE: PRIMARY FILE DOWNLOAD OPTIONS
             // ************************* --%>
          <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" >
            <TR>
              <TD COLSPAN="2" NOWRAP><SPAN CLASS="standardTextBold"><%= downloadOptions %></SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= format %></SPAN></TD>
              <TD ><SPAN CLASS="standardText">
                <SELECT NAME="formatSelector" CLASS="standardText" >
                  <OPTION VALUE="<%= formatOmegaTValue %>" title="For OmegaT"><%=formatOmegaTName %></OPTION>
                  <OPTION VALUE="<%= formatValueRtfListViewTradosOptimized %>" title="For Trados 7 and SDL Trados 2007"><%= formatRtfListViewTradosOptimized %></OPTION>
                  <OPTION VALUE="<%= formatValueRtfListView %>" title="For SDL Trados 2009 and 2011"><%= formatRtfListView %></OPTION>
                  <OPTION VALUE="<%= formatXlfName12 %>" title="For Xliff 1.2"><%=formatXlfValue12 %></OPTION>
                  <OPTION VALUE="<%= formatTTXValue %>" title="For Trados 7 and SDL Trados 2007"><%=formatTTXName %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
			<TR>
              <TD><SPAN CLASS="standardText"><%= placeholderFormat %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT NAME="ptagSelector" id="ptagSelector" CLASS="standardText">
                  <OPTION VALUE="<%= ptagValueCompact %>" SELECTED><%= ptagTextCompact %></OPTION>
                  <OPTION VALUE="<%= ptagValueVerbose %>"><%= ptagTextVerbose %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= labelResInsertion %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT id="tmxTypeSelector" NAME="resInsertionSelector" CLASS="standardText" >
                  <OPTION VALUE="<%= resInsertValueAtns %>" class="TTX" SELECTED><%= resInsertTextAtns %></OPTION>
                  <OPTION VALUE="<%= resValueTmx14b %>"  SELECTED><%= resTmx14b %></OPTION>
                  <OPTION VALUE="<%= resValueTmxBoth %>" class="TTX"><%= resTmxBoth %></OPTION>
                  <OPTION VALUE="<%= resInsertValueNone %>"><%= resInsertTextNone %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
  			<TR>
              <TD></TD>
			  <TD><SPAN CLASS="standardText tmxTypeSelector"><input type="checkbox" id="changeCreationIdForMT" name="<%=UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT %>"/><%=bundle.getString("lb_tm_export_change_creationid_for_mt")%></SPAN></TD>
			</TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= labelTerminology %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT id="resTermSelector" NAME="termSelector" CLASS="standardText" >
                  <OPTION VALUE="<%= OfflineConstants.TERM_HTML %>" class="unOmegaT"  SELECTED><%=bundle.getString("lb_terminology_html")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TBX %>"><%=bundle.getString("lb_terminology_import_format_tbx")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TRADOS %>" class="unOmegaT" ><%=bundle.getString("lb_terminology_multiterm_ix_format")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TXT %>" class="OmegaT"><%= resTermTxt %></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_NONE %>"><%= resInsertTextNone %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            
            <%
            if (TMEditType > 1) {
            %>
             <TR>
              <TD><SPAN CLASS="standardText"><%= exactMatchesEditable %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT NAME="TMEditType" CLASS="standardText">
                  <option value="1" <%=userOptionOfTMEdit == 1 ? "selected" : "" %>><%=bundle.getString("lb_l10nprofile_tm_edit_type_both") %></option>
                  <option value="2" <%=userOptionOfTMEdit == 2 ? "selected" : "" %>><%=bundle.getString("lb_l10nprofile_tm_edit_type_ice") %></option>
                  <option value="3" <%=userOptionOfTMEdit == 3 ? "selected" : "" %>><%=bundle.getString("lb_l10nprofile_tm_edit_type_100") %></option>
                  <option value="4" <%=userOptionOfTMEdit == 4 ? "selected" : "" %>><%=bundle.getString("lb_l10nprofile_tm_edit_type_deny") %></option>
                </SELECT>
              </SPAN></TD>
            </TR>   
            <%
            }
            %>
            
            <TR id="populate100" class="formatAcces">
                <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_populate_100_target_segment") %></SPAN></TD>
                <TD><SPAN CLASS="standardText">
                <input id="populate100CheckBox" type="checkbox" name="<%=OfflineConstants.POPULATE_100%>" checked="checked" value="true"/></SPAN>
                </TD>
            </TR>
             <TR id="populatefuzzy" class="formatAcces">
                <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_populate_fuzzy_target_segment") %></SPAN></TD>
                <TD><SPAN CLASS="standardText">
                <input id="populatefuzzyCheckBox" type="checkbox" name="<%=OfflineConstants.POPULATE_FUZZY%>" checked="checked" value="true"/></SPAN>
                </TD>
            </TR>
            <TR id="needConsolidateBox" class="formatAcces">
            	<TD><SPAN CLASS="standardText"><%=bundle.getString("lb_download_consolate") %></SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                      <input type="checkbox" id="needConsolidate" name="needConsolidate" value="true" checked="checked">
                    </SPAN>
                </TD>
            </TR>
            <TR id="includeRepetitionsBox" class="formatAcces">
            	<TD><SPAN CLASS="standardText"><%=bundle.getString("lb_download_repetition") %></SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                      <input type="checkbox" id="includeRepetitions" name="includeRepetitions" value="true" checked="checked">
                    </SPAN>
                </TD>
            </TR>
            <TR>
                    <TD COLSPAN="3"> &nbsp;  </TD>
            </TR>
            <TR>
                    <TD  COLSPAN="3"><SPAN CLASS="smallText"><%= labelResInsertionNote %></SPAN></TD>
            </TR>
          </TABLE>
        </TD>
    </TR>
<%
    }// END: Download options
%>
<%
    // *** LIST: UNEXTRACTED PRIMARY SOURCE FILES *************************
    if((U_PTFList != null) && (U_PTFList.size() > 0))
    {
%>
    <TR>
            <TD COLSPAN="3"> &nbsp;  </TD>
    </TR>
    <TR>
            <TD CLASS="tableHeadingBasic" COLSPAN="3"><input type="checkbox" onclick="handleSelectAll(this,'<%=priSrcCheckBoxes%>')" checked="true"/>&nbsp;<%= PSHeading %></TD>
    </TR>
    <TR>
            <TD VALIGN="TOP" >
                <%-- // *************************
                     // TABLE: UN-EXTRACTED SOURCE FILES
                     // ************************* --%>
                    <TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0">
                    <TR valign="top">
                        <TD COLSPAN="3" NOWRAP>
                          <SPAN CLASS="standardTextBold"><%= selectPSFiles %></SPAN>
                        </TD>
                    </TR>
<%
        for(int i=0; i < U_PTFList.size(); i++ )
        {
            TargetPage tp = (TargetPage)U_PTFList.get(i);
            SourcePage sp = tp.getSourcePage();
            UnextractedFile uf = (UnextractedFile)sp.getPrimaryFile();
            StringBuffer info = new StringBuffer();
            StringBuffer url = new StringBuffer();
            url.append(WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING);
            url.append(uf.getStoragePath());
            String name = uf.getStoragePath();
            if(showShortNames)
            {
                name = DownloadPageHandler.getFileNameShort(name);
            }
            else
            {
            	name = DownloadPageHandler.getFileName(name);
            }
            name = EditUtil.encodeHtmlEntities(name);
            String id = sp.getIdAsLong().toString();
            Date date = uf.getLastModifiedDate();
            String dateStr = (dateTimeFormat.format(date));
            String lastModifierUserName = null;
            try
            {
                User user = ServerProxy.getUserManager().getUser(uf.getLastModifiedBy());
                lastModifierUserName = user.getUserName();
            }
            catch(Exception e)
            {
                lastModifierUserName = "unknown";
            }
            long size = uf.getLength();
            size = size < 3 ? 0 : size; // adjust
            if(size != 0)
            {
                long r = size%1024;
                size = (r!=0) ? ((size/1024)+1) : size;  //round up
            }
            String sizeStr = numberFormat.format(size);
            info.append(lastModified);
            info.append(" ");
            info.append(dateStr);
            info.append(" - ");
            info.append(sizeStr);
            info.append("k");
%>
                    <TR VALIGN="TOP">
                      <TD>
                          <INPUT TYPE="checkbox" CHECKED="true" NAME="<%=priSrcCheckBoxes %>" CLASS="standardText" VALUE="<%= id %>" ID="SecondaryTargetFile">
                      </TD>
                      <TD>
                            <IMG SRC="/globalsight/images/file_unextracted.gif" ALT="<%= bundle.getString("lb_file_unextracted") %>" WIDTH=13 HEIGHT=15>
                      </TD>
                      <TD>
                          <A CLASS="standardHREF" target="_blank" href="<%= URLEncoder.encodeUrlStr(url.toString()) %>" TITLE="<%=uf.getStoragePath()%>">
                            <%= name %> </A><BR>
                            <SPAN CLASS="glossaryDateSize">
                            <%= info.toString() %><BR><%=stfLastModifiedBy%> <%=lastModifierUserName%>
                            </SPAN>
                      </TD>
                    </TR>
<%
        }
%>
                    <!--for gbs-2599
					TR>
                        <TD COLSPAN=3><A CLASS="standardHREF" HREF="#"
                            onClick="doCheckAll('<%= priSrcCheckBoxes %>'); return false;"
                            onFocus="this.blur();"><%= checkAllLinkText %></A> |
                        <A CLASS="standardHREF" HREF="#"
                            onClick="doClearAll('<%= priSrcCheckBoxes %>'); return false;"
                            onFocus="this.blur();"><%= clearAllLinkText %></A>
                        </TD>
                    </TR-->
                    </TABLE>
            </TD>
    </TR><!-- end unextracted primary source files section -->
<%
    }
%>
<%
    // *** LIST: SECONDARY TARGET FILES *************************
    if((STFList != null) && (STFList.size() > 0))
    {
%>
    <TR>
            <TD COLSPAN="3"> &nbsp;  </TD>
    </TR>
    <TR>
            <TD CLASS="tableHeadingBasic" COLSPAN="3"><input type="checkbox" onclick="handleSelectAll(this,'<%=stfCheckBoxes%>')" checked="true"/>&nbsp;<%= STFHeading %></TD>
    </TR>
    <TR>
            <TD VALIGN="TOP" >
                <%-- // *************************
                     // TABLE: SECONDARY TARGETS
                     // ************************* --%>
                    <TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0">
                    <TR valign="top">
                        <TD COLSPAN="3" NOWRAP>
                          <SPAN CLASS="standardTextBold"><%= selectSTFFiles %></SPAN>
                        </TD>
                    </TR>
<%
        for(SecondaryTargetFile stf : STFList)
        {
            StringBuffer info = new StringBuffer();
            StringBuffer url = new StringBuffer();
            url.append(WebAppConstants.STF_FILES_URL_MAPPING);
            url.append(stf.getStoragePath());
            String name = stf.getStoragePath();
            if(showShortNames)
            {
                name = DownloadPageHandler.getFileNameShort(name);
            }
            else
            {
            	name = DownloadPageHandler.getFileName(name);
            }
            name = EditUtil.encodeHtmlEntities(name);
            String id = stf.getIdAsLong().toString();
            Date date = new Date(stf.getLastUpdatedTime());
            String dateStr = (dateTimeFormat.format(date));
            String lastModifierUserName = null;
            try
            {
                User user = ServerProxy.getUserManager().getUser(stf.getModifierUserId());
                lastModifierUserName = user.getUserName();
            }
            catch(Exception e)
            {
                lastModifierUserName = "unknown";
            }
            long size = stf.getFileSize();
            size = size < 3 ? 0 : size; // adjust
            if(size != 0)
            {
                long r = size%1024;
                size = (r!=0) ? ((size/1024)+1) : size;  //round up
            }
            String sizeStr = numberFormat.format(size);
            info.append(lastModified);
            info.append(" ");
            info.append(dateStr);
            info.append(" - ");
            info.append(sizeStr);
            info.append("k");
%>
                    <TR VALIGN="TOP">
                      <TD>
                          <INPUT TYPE="checkbox" CHECKED="true" NAME="<%=stfCheckBoxes %>" CLASS="standardText" VALUE="<%= id %>" ID="SecondaryTargetFile">
                      </TD>
                      <TD>
                          <IMG SRC="/globalsight/images/file_unextracted.gif" ALT="<%= bundle.getString("lb_file_unextracted") %>" WIDTH=13 HEIGHT=15>
                      </TD>
                      <TD style="width:700px;word-wrap:break-word;word-break:break-all">
<%				out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:700px\'>\");}</SCRIPT>"); %>
                          <A CLASS="standardHREF" target="_blank" href="<%= URLEncoder.encodeUrlStr(url.toString()) %>" TITLE="<%=stf.getStoragePath()%>">
                            <%= name %> </A><BR>
                            <SPAN CLASS="glossaryDateSize">
                            <%= info.toString() %><BR><%=stfLastModifiedBy%> <%=lastModifierUserName%>
                            </SPAN>
<%				out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\");}</SCRIPT>"); %>
                      </TD>
                    </TR>
<%
        }
%>
                    <!--for gbs-2599
					TR>
                        <TD COLSPAN=3><A CLASS="standardHREF" HREF="#"
                            onClick="doCheckAll('<%= stfCheckBoxes %>'); return false;"
                            onFocus="this.blur();"><%= checkAllLinkText %></A> |
                        <A CLASS="standardHREF" HREF="#"
                            onClick="doClearAll('<%= stfCheckBoxes %>'); return false;"
                            onFocus="this.blur();"><%= clearAllLinkText %></A>
                        </TD>
                    </TR-->
                    </TABLE>
            </TD>
    </TR><!-- end secondary target section -->
<%
    }
%>
    <TR>
            <TD COLSPAN="3"> &nbsp;  </TD>
    </TR>
    <TR>
            <TD CLASS="tableHeadingBasic" COLSPAN="3"><input type="checkbox" onclick="handleSelectAll(this,'<%=glossaryCheckBoxes%>')" checked="true"/>&nbsp;<%= glossaryHeading %></TD>
    </TR>
    <TR>
            <TD VALIGN="TOP" >
                    <%-- //********************
                         // TABLE: SUPPORT FILES
                         //******************** --%>
                    <TABLE CELLPADDING="5" CELLSPACING="0" BORDER="0">
                    <TR valign="top">
                        <TD COLSPAN="2" NOWRAP>
                          <SPAN CLASS="standardTextBold"><%= selectGlossaries %></SPAN>
                        </TD>
                    </TR>
<%
    if((glossaryList == null) || (glossaryList.size() == 0))
    {
%>
                    <TR>
                      <TD>
                        <SPAN CLASS="standardText"><%= noGlossaries %></SPAN>
                      </TD>
                    </TR>
<%
    }
    else
    {
        for(int i=0; i < glossaryList.size(); i++ )
        {
            GlossaryFile file = (GlossaryFile)glossaryList.get(i);
            StringBuffer url = new StringBuffer();
            url.append("/globalsight/");
	          url.append(AmbFileStoragePathUtils.SUPPORT_FILES_SUB_DIRECTORY);
	          url.append("/");
	          if (file.isForAnySourceLocale())
            {
                url.append(file.getGlobalSourceLocaleName());
            }
            else
            {
                url.append(file.getSourceLocale().toString());
            }
            url.append("/");
            if (file.isForAnyTargetLocale())
            {
                url.append(file.getGlobalTargetLocaleName());
            }
            else
            {
                url.append(file.getTargetLocale().toString());
            }
            url.append("/");
            url.append(file.getFilename());
            String name = EditUtil.encodeHtmlEntities(file.getFilename());
            Date date = file.getLastModified();
            String dateStr = (date != null ? dateTimeFormat.format(date) : "null");

            // round size to nearest 1024bytes (1k) - like win-explorer.
            long size = file.getFileSize() < 3 ? 0 : file.getFileSize(); // adjust for empty file
            if(size != 0)
            {
                size = (size%1024!=0) ? ((size/1024)+1)/*round up*/ : size/1024;
            }
            String sizeStr = numberFormat.format(size);
%>
                    <TR VALIGN="TOP">
                      <TD>
                          <INPUT TYPE="checkbox" CHECKED="true" NAME="<%=glossaryCheckBoxes %>" CLASS="standardText" VALUE="<%= i %>" ID="glossary">
                      </TD>
                      <TD>
                          <A CLASS="standardHREF" target="_blank" href="<%= URLEncoder.encodeUrlStr(url.toString()) %>">
                            <%= name %> </A><BR>
                            <SPAN CLASS="glossaryDateSize">
                            <%= lastModified + " " + dateStr + " - " + sizeStr + "k" %>
                            </SPAN>
                      </TD>
                    </TR>
<%
        }
%>
                    <!--for gbs-2599
					TR>
                        <TD COLSPAN=2><A CLASS="standardHREF" HREF="#"
                            onClick="doCheckAll('<%= glossaryCheckBoxes%>'); return false;"
                            onFocus="this.blur();"><%= checkAllLinkText %></A> |
                        <A CLASS="standardHREF" HREF="#"
                            onClick="doClearAll('<%= glossaryCheckBoxes%>'); return false;"
                            onFocus="this.blur();"><%= clearAllLinkText %></A>
                        </TD>
                    </TR-->
<%
    }
%>
                    </TABLE>
                    </FORM>
            </TD>
    </TR>
    <TR>
      <TD>
	<INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
	onclick="location.replace('<%=cancelUrl%>')">
	<INPUT TYPE="BUTTON" NAME="<%=lbStartDownload%>"
	VALUE="<%=lbStartDownload%>" onclick="submitForm()">
      </TD>
    </TR>
    </TABLE>
</TD>
</TR>
</TABLE>

</TD>
</TR>
</TABLE>

</TD>
</TR>
</TABLE>

</TD>
</TR>
</TABLE>

</TD>
</TR>
</TABLE>

</DIV>
<SCRIPT LANGUAGE="JavaScript">
<%-- this global javascript variable must be updated last --%>
var hasExtractedFiles = <%=hasExtractedFiles%>;
</SCRIPT>
</BODY>
</HTML>
