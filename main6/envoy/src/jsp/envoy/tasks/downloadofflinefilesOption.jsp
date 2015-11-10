<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.util.modules.Modules,
            com.globalsight.config.UserParamNames,
            com.globalsight.everest.edit.EditHelper,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,            
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
	    	com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
            com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState,
            org.json.JSONObject,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
HashMap optionsHash = (HashMap)sessionMgr.getAttribute("optionsHash");
JSONObject dwnldOpt = new JSONObject(optionsHash);
ResourceBundle bundle = PageHandler.getBundle(session);
PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

String url_options = done.getPageURL();
String url_cancel    = cancel.getPageURL();

String lb_title = bundle.getString("lb_download_offline_files_options");
String lb_options = lb_title;
String lb_general_options = bundle.getString("lb_general_options");

String lb_ok = bundle.getString("lb_done");
String lb_cancel = bundle.getString("lb_cancel");



String downloadOptions = bundle.getString("lb_download_options");
// Parameter values
String placeholderFormat = bundle.getString("lb_placeholder_format");
String labelResInsertion = bundle.getString("lb_resource_linking");
String labelPenalizedReferenceTm = bundle.getString("lb_work_offline_option_penalized_reference_tm");
String labelResInsertionNote = bundle.getString("lb_resource_linking_note");
String exactMatchesEditable = bundle.getString("lb_exact_matches_editable");
String pagename = (String)request.getAttribute(UserParamNames.PAGENAME_DISPLAY);
String editor   = (String)request.getAttribute(UserParamNames.DOWNLOAD_OPTION_EDITOR);
String format = bundle.getString("lb_format") + bundle.getString("lb_colon");


// Option value names
//   format selector
String formatValueRtfListView = OfflineConstants.FORMAT_RTF;
String formatValueRtfListViewTradosOptimized = OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED;

String formatXlfName12 = OfflineConstants.FORMAT_XLF_NAME_12;
String formatXlfValue12 = OfflineConstants.FORMAT_XLF_VALUE_12;

String formatXlfValue20 = OfflineConstants.FORMAT_XLF_VALUE_20;

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
String formatRtfListView = bundle.getString("lb_rtf_listview");
String formatRtfListViewTrados = bundle.getString("lb_rtf_trados");
String formatRtfListViewTradosOptimized = bundle.getString("lb_rtf_trados_optimized");
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


String labelTerminology = bundle.getString("lb_terminology");


%>
<HTML>
<!-- This JSP is: /envoy/tasks/downloadofflinefilesOption.jsp -->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="Javascript" SRC="envoy/terminology/viewer/error.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/downloadOpt.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript"> 
var needWarning = false;
var objectName = "<%= bundle.getString("lb_account_information_my") %>";
var guideNode = "myAccount";
var helpFile = "<%=bundle.getString("help_download")%>";
var dwnldOpt = <%=dwnldOpt%>;
</SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

function dsubmit()
{
	downloadForm.submit();
}

function setWordCountDisplay()
{
	if($("#consolidateFileType").val() == "consolidateByWordCount")
	{
		$("#wordCountForDownload").show();
	}
	else
	{
		$("#wordCountForDownload").hide();
	}
}

function switchRadio(penalizedReferenceTmRadio)
{
    if ("penalizedReferenceTmPre" == penalizedReferenceTmRadio.id)
    {
        $("#penalizedReferenceTmPer").attr("checked", false);
    }
    else
    {
        $("#penalizedReferenceTmPre").attr("checked", false);
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="init();" id="idBody" >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<P CLASS="mainHeading"><%=lb_options%></P>

<FORM ACTION="<%=url_options%>" METHOD="post" name="downloadForm"
 onsubmit="return checkForm();">
<INPUT type="hidden" name="__save" VALUE="yes">
<INPUT type="hidden" name="<%=UserParamNames.PAGENAME_DISPLAY%>" value="<%=pagename%>">

<P class="header2" style="width:630px; *width:auto;">&nbsp;<%=lb_general_options%></P>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
            <TR>
              <TD COLSPAN="2" NOWRAP><SPAN CLASS="standardTextBold"><%= downloadOptions %></SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= format %></SPAN></TD>
              <TD ><SPAN CLASS="standardText">
                <SELECT NAME="formatSelector" CLASS="standardText" >
                  <OPTION VALUE="<%= formatOmegaTValue %>" title="For OmegaT"><%=formatOmegaTName %></OPTION>
                  <OPTION VALUE="<%= formatValueRtfListViewTradosOptimized %>" title="for Trados 7 and SDL Trados 2007"><%= formatRtfListViewTradosOptimized %></OPTION>
                  <OPTION VALUE="<%= formatValueRtfListView %>" title="For SDL Trados 2009 and 2011"><%= formatRtfListView %></OPTION>
                  <OPTION VALUE="<%= formatXlfName12 %>" title="For Xliff 1.2"><%=formatXlfValue12 %></OPTION>
                  <OPTION VALUE="<%= formatXlfValue20 %>" title="For Xliff 2.0"><%=formatXlfValue20 %></OPTION>
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
			  <TD><SPAN CLASS="standardText tmxTypeSelector">
			         <!--   <input type="checkbox" id="changeCreationIdForMT" name="<%=UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT %>"/><%=bundle.getString("lb_tm_export_change_creationid_for_mt")%> -->
			       <input type="hidden" name="<%=UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT %>" value="true"/>
			  </SPAN></TD>
			</TR>
			<!-- GBS-3831 -->
			<TR>
              <TD></TD>
			  <TD id="separateTMfileTD"><SPAN CLASS="standardText tmxTypeSelector"><input type="checkbox" id="separateTMfile" name="<%=UserParamNames.DOWNLOAD_OPTION_SEPARATE_TM_FILE %>"/><%=bundle.getString("lb_mt_matches_into_separate_tm_file")%></SPAN></TD>
			</TR>
			<TR id="penalizedReferenceTm">
              <TD><SPAN CLASS="standardText"><%=labelPenalizedReferenceTm%>:</SPAN></TD>
              <TD><SPAN CLASS="standardText">
              <input type="radio" id="penalizedReferenceTmPre" name="<%=UserParamNames.DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PRE%>" checked="checked" onclick="switchRadio(this);"/><%=bundle.getString("lb_work_offline_option_penalized_reference_tm_pre")%>
              <br>
              <input type="radio" id="penalizedReferenceTmPer" name="<%=UserParamNames.DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PER%>" onclick="switchRadio(this);"/><%=bundle.getString("lb_work_offline_option_penalized_reference_tm_per")%>
              </SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= labelTerminology %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT id="resTermSelector" NAME="termSelector" CLASS="standardText" onchange="operateConsolidateTerm()">
                  <OPTION VALUE="<%= OfflineConstants.TERM_HTML %>"  class="unOmegaT"  SELECTED><%=bundle.getString("lb_terminology_html")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TBX %>"><%=bundle.getString("lb_terminology_import_format_tbx")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TRADOS %>" class="unOmegaT"><%=bundle.getString("lb_terminology_multiterm_ix_format")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TXT %>" class="OmegaT"><%= resTermTxt %></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_NONE %>"><%= resInsertTextNone %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            
             <TR>
              <TD><SPAN CLASS="standardText"><%= exactMatchesEditable %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT NAME="TMEditType" CLASS="standardText">
                  <option value="1"><%=bundle.getString("lb_l10nprofile_tm_edit_type_both") %></option>
                  <option value="2"><%=bundle.getString("lb_l10nprofile_tm_edit_type_ice") %></option>
                  <option value="3"><%=bundle.getString("lb_l10nprofile_tm_edit_type_100") %></option>
                  <option value="4"><%=bundle.getString("lb_l10nprofile_tm_edit_type_deny") %></option>
                </SELECT>
              </SPAN></TD>
            </TR>   
            
            
            
            <TR id="populate100" class="formatAcces">
                <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_populate_100_target_segment") %></SPAN></TD>
                <TD><SPAN CLASS="standardText">
                <input id="populate100CheckBox" type="checkbox" name="<%=OfflineConstants.POPULATE_100%>" checked="checked" value="true"/></SPAN>
                </TD>
            </TR>
             <TR id="populatefuzzy" class="formatAcces">
                <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_populate_fuzzy_target_segment") %></SPAN></TD>
                <TD><SPAN CLASS="standardText">
                <input id="populatefuzzyCheckBox" type="checkbox" name="<%=OfflineConstants.POPULATE_FUZZY%>"  value="true"/></SPAN>
                </TD>
            </TR>
            <TR id="preserveSourceFolderBox" class="standardText">
            	<TD><SPAN CLASS="standardText"><%=bundle.getString("lb_preserve_source_folder_structure") %></SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                      <input type="checkbox" id="preserveSourceFolder" name="preserveSourceFolder" value="true" checked="checked" onclick="uniquenessCheck('preserveSourceFolder')">
                    </SPAN>
                </TD>
            </TR>
            <TR id="needConsolidateBox" class="standardText">
            	<TD><SPAN CLASS="standardText">Consolidate/Split Type</SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                    <select name="consolidateFileType" id="consolidateFileType" onchange="setWordCountDisplay();uniquenessCheck('needConsolidate')" CLASS="standardText">
                    	<option value="consolidate">Consolidate All Files</option>
                    	<option value="notConsolidate">File by File</option>
                    	<option value="consolidateByWordCount">Split File per Word Count</option>
                    </select>
                    <input name="wordCountForDownload" id="wordCountForDownload" style="display:none;width:60px;height:19px" class="standardText"/>
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
            <TR id="excludeFullyLeveragedFilesBox" class="standardText">
            	<TD><SPAN CLASS="standardText"><%=bundle.getString("lb_download_exclude_fully_leveraged_files") %></SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                      <input type="checkbox" id="excludeFullyLeveragedFiles" name="excludeFullyLeveragedFiles" value="true" checked="checked"/>
                    </SPAN>
                </TD>
            </TR>
            <TR id="includeXmlNodeContextInformationBox" class="standardText">
            	<TD><SPAN CLASS="standardText"><%=bundle.getString("lb_include_xml_node_context_information") %></SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                      <input type="checkbox" id="includeXmlNodeContextInformation" name="includeXmlNodeContextInformation" value="true" checked="checked"/>
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

     <INPUT type="button" name="cancel" value="<%=lb_cancel%>"
      onclick="location.replace('<%=url_cancel%>')">
&nbsp;&nbsp;
      <INPUT type="button" name="apply" value="<%=lb_ok%>" onclick="submitForm()"> 
</FORM>
</DIV>
</BODY>
</HTML>

