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
ResourceBundle bundle = PageHandler.getBundle(session);
PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

String url_options = done.getPageURL();
String url_cancel    = cancel.getPageURL();

String lb_title = bundle.getString("lb_download_offline_files_options");
String lb_options = lb_title;
String lb_general_options = bundle.getString("lb_general_options");

String lb_ok = bundle.getString("lb_done");
String lb_cancel = bundle.getString("lb_cancel");

// Additional notes on the screen
String lb_colorNote = bundle.getString("msg_colors_apply_to_preview");
String lb_numericValue = bundle.getString("lb_numeric_value");


String downloadOptions = bundle.getString("lb_download_options");
// Parameter values
String encoding = bundle.getString("lb_character_encoding");
String placeholderFormat = bundle.getString("lb_placeholder_format");
String exactMatchesEditable = bundle.getString("lb_exact_matches_editable");
String noGlossaries = bundle.getString("lb_no_glossaries");
String noSTFFiles = bundle.getString("lb_no_secondary_target_files");
String noPriSrcFiles = bundle.getString("lb_no_primary_source_files");
String labelActivity = bundle.getString("lb_activity") + bundle.getString("lb_colon");
String labelJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");
String labelResInsertion = bundle.getString("lb_resource_linking");
String labelResInsertionNote = bundle.getString("lb_resource_linking_note");

String pagename = (String)request.getAttribute(UserParamNames.PAGENAME_DISPLAY);
String editor   = (String)request.getAttribute(UserParamNames.DOWNLOAD_OPTION_EDITOR);
String format = bundle.getString("lb_format") + bundle.getString("lb_colon");
// control name
String formatSelector = OfflineConstants.FORMAT_SELECTOR;
String editorSelector = OfflineConstants.EDITOR_SELECTOR;
String encodingSelector = OfflineConstants.ENCODING_SELECTOR;
String ptagSelector = OfflineConstants.PTAG_SELECTOR;
String editExactSelector = OfflineConstants.EDIT_EXACT_SELECTOR;
String resInsertionSelector = OfflineConstants.RES_INS_SELECTOR;
String pageCheckBoxes = OfflineConstants.PAGE_CHECKBOXES;
String glossaryCheckBoxes = OfflineConstants.GLOSSARY_CHECKBOXES;
String stfCheckBoxes = OfflineConstants.STF_CHECKBOXES;
String priSrcCheckBoxes = OfflineConstants.PRI_SOURCE_CHECKBOXES;

// Option value names
//   format selector
String formatValueRtfListView = OfflineConstants.FORMAT_RTF;
String formatValueRtfListViewTrados = OfflineConstants.FORMAT_RTF_TRADOS;
String formatValueRtfListViewTradosOptimized = OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED;
String formatValueTextListView = OfflineConstants.FORMAT_TEXT;
String formatValueRtfParaView = OfflineConstants.FORMAT_RTF_PARA_VIEW;

String formatXlfName12 = OfflineConstants.FORMAT_XLF_NAME_12;
String formatXlfValue12 = OfflineConstants.FORMAT_XLF_VALUE_12;

String formatTTXName = OfflineConstants.FORMAT_TTX_NAME;
String formatTTXValue = OfflineConstants.FORMAT_TTX_VALUE;

//   editor selector
String editorValueWin2000 = OfflineConstants.EDITOR_WIN2000;
String editorValueWin2000AndAbove = OfflineConstants.EDITOR_WIN2000_ANDABOVE;
String editorValueWin97 = OfflineConstants.EDITOR_WIN97;
String editorValueMac2001 = OfflineConstants.EDITOR_MAC2001;
String editorValueMac98 = OfflineConstants.EDITOR_MAC98;
String editorValueOther = OfflineConstants.EDITOR_OTHER;
String editorValueTradosTagEditor = OfflineConstants.EDITOR_TRADOS_TAGEDITOR;

String editorXlfName = OfflineConstants.EDITOR_XLF_NAME;
String editorXlfValue = OfflineConstants.EDITOR_XLF_VALUE;

//   ptag format selector
String ptagValueCompact = OfflineConstants.PTAG_COMPACT;
String ptagValueVerbose = OfflineConstants.PTAG_VERBOSE;
//   edit exact selector
String editExactValueNo = OfflineConstants.EDIT_EXACT_NO;
String editExactValueYes = OfflineConstants.EDIT_EXACT_YES;
//   resource link selector
String resInsertValueAtns = OfflineConstants.RES_INS_ATNS;
String resInsertValueSingleLink = OfflineConstants.RES_INS_LINK;
String resInsertValueNone = OfflineConstants.RES_INS_NONE;
String resValueTmxPlain = OfflineConstants.RES_INS_TMX_PLAIN;
String resValueTmx14b = OfflineConstants.RES_INS_TMX_14B;
String resValueTmxBoth = OfflineConstants.RES_INX_TMX_BOTH;
//   encoding selector
String encodingValueDefault = OfflineConstants.ENCODING_DEFAULT;


//String displayExactMatchNo = OfflineConstants.DISPLAY_EXACT_MATCH_NO;
//String displayExactMatchYes = OfflineConstants.DISPLAY_EXACT_MATCH_YES;
//String displayExactMatch = OfflineConstants.DISPLAY_EXACT_MATCH;

// Option text names
//   format selector
String formatStartHere = bundle.getString("lb_start_here");
String formatRtfListView = bundle.getString("lb_rtf_listview");
String formatRtfListViewTrados = bundle.getString("lb_rtf_trados");
String formatRtfListViewTradosOptimized = bundle.getString("lb_rtf_trados_optimized");
String formatTextListView = bundle.getString("lb_text");
String formatRtfParaView = bundle.getString("lb_rtf_paraview_1");

//   editor selector
String editorTextSelect = bundle.getString("lb_select_an_editor");
String editorTextWin2000 = bundle.getString("lb_win_2000");
String editorTextWin2000AndAbove = bundle.getString("lb_win_2000_and_above");
String editorTextWin97 = bundle.getString("lb_win_97");
String editorTextMac2001 = bundle.getString("lb_mac_2001");
String editorTextMac98 = bundle.getString("lb_mac_98");
String editorTextOther = bundle.getString("lb_other");
String editorTextTradosTagEditor = "Trados TagEditor";
//   encoding selector
String encodingTextDefault = bundle.getString("lb_default_encoding");
//   ptag format selector
String ptagTextCompact = bundle.getString("lb_compact");
String ptagTextVerbose = bundle.getString("lb_verbose");
//   edit exact selector
String editExactTextNo = bundle.getString("lb_no");
String editExactTextYes = bundle.getString("lb_yes");
//   resource link selector
String resInsertTextAtns = bundle.getString("lb_make_res_atns");
String resInsertTextSingleLink = bundle.getString("lb_make_res_link");
String resInsertTextNone = bundle.getString("lb_make_res_none");
String resTmxPlain = bundle.getString("lb_make_res_tmx_plain");
String resTmx14b = bundle.getString("lb_make_res_tmx_14b");
String resTmxBoth = bundle.getString("lb_make_res_tmx_both");
boolean editExact = true;
    
String cookieNameFileFormat = OfflineConstants.COOKIE_FILE_FORMAT;
String cookieNameEditor = OfflineConstants.COOKIE_EDITOR;
String cookieNameEncoding = OfflineConstants.COOKIE_ENCODING;
String cookieNamePtagFormat = OfflineConstants.COOKIE_PTAG_FORMAT;
String cookieNameEditExact = OfflineConstants.COOKIE_EDIT_EXACT;
String cookieNameResInsMode = OfflineConstants.COOKIE_RES_INS_MODE;

String labelTerminology = bundle.getString("lb_terminology");


%>
<HTML>
<!-- This JSP is: /envoy/tasks/downloadofflinefilesOption.jsp -->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/formValidation.js"></SCRIPT>
<SCRIPT LANGUAGE="Javascript" SRC="envoy/terminology/viewer/error.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>

<SCRIPT LANGUAGE="JavaScript"> 
var needWarning = false;
var objectName = "<%= bundle.getString("lb_account_information_my") %>";
var guideNode = "myAccount";
var helpFile = "<%=bundle.getString("help_my_account_options")%>";
</SCRIPT>
<SCRIPT LANGUAGE="JavaScript">

function showHourglass()
{
    downloadForm.apply.disabled = true;
    downloadForm.cancel.disabled = true;
    idBody.style.cursor = "wait";
}

function checkForm()
{
    showHourglass();

    return true;
}


function init()
{
    doOnLoad();

    window.focus();

    loadGuides();

}

function submitForm()
{
	needRemoveDownloadOptionCookie = false;
	saveUserOptions(downloadForm);
}
var needWarning = false;
var objectName = "";
var guideNode = "myActivitiesDownload";
var isMac = (navigator.appVersion.indexOf("Mac") != -1) ? true : false;
var helpFile = "<%=bundle.getString("help_download")%>";


var editorOptionNull = new Option(" ", "-");
var editorOptionSelect = new Option("<%= editorTextSelect %>", "-");
var editorOptionWin2000 = new Option("<%= editorTextWin2000 %>", "<%= editorValueWin2000 %>");
var editorOptionWin2000AndAbove = new Option("<%= editorTextWin2000AndAbove %>", "<%= editorValueWin2000AndAbove %>");
var editorOptionWin97 = new Option("<%= editorTextWin97 %>", "<%= editorValueWin97 %>");
var editorOptionMac2001 = new Option("<%= editorTextMac2001 %>", "<%= editorValueMac2001 %>");
var editorOptionMac98 = new Option("<%= editorTextMac98 %>", "<%= editorValueMac98 %>");
var editorOptionOther = new Option("<%= editorTextOther %>", "<%= editorValueOther %>");
var editorOptionTradosTagEditor = new Option("<%= editorTextTradosTagEditor %>", "<%= editorValueTradosTagEditor %>");

var editorOptNames = new Array;
editorOptNames[0] = "<%= editorTextWin2000 %>";
editorOptNames[1] = "<%= editorTextWin97 %>";
editorOptNames[2] = "<%= editorTextMac2001 %>";
editorOptNames[3] = "<%= editorTextMac98 %>";
editorOptNames[4] = "<%= editorTextOther %>";
editorOptNames[5] = "<%= editorTextTradosTagEditor %>";
editorOptNames[6] = "<%= editorTextWin2000AndAbove %>";
editorOptNames[7] = "<%= editorXlfName %>";

var editorOptValues = new Array;
editorOptValues[0] = "<%= editorValueWin2000 %>";
editorOptValues[1] = "<%= editorValueWin97 %>";
editorOptValues[2] = "<%= editorValueMac2001 %>";
editorOptValues[3] = "<%= editorValueMac98 %>";
editorOptValues[4] = "<%= editorValueOther %>";
editorOptValues[5] = "<%= editorValueTradosTagEditor %>";
editorOptValues[6] = "<%= editorValueWin2000AndAbove %>";
editorOptNames[7] = "<%= editorXlfValue %>";

<%-- construct the encoding options --%>


var resourceOptNames = new Array;
resourceOptNames[0] = "<%= resInsertTextAtns %>";
resourceOptNames[1] = "<%= resInsertTextSingleLink %>";
resourceOptNames[2] = "<%= resTmxPlain %>";
resourceOptNames[3] = "<%= resTmx14b %>";
resourceOptNames[4] = "<%= resTmxBoth %>";
resourceOptNames[5] = "<%= resInsertTextNone %>";

var resourceOptValues = new Array;
resourceOptValues[0] = "<%= resInsertValueAtns %>";
resourceOptValues[1] = "<%= resInsertValueSingleLink %>";
resourceOptValues[2] = "<%= resValueTmxPlain %>";
resourceOptValues[3] = "<%= resValueTmx14b %>";
resourceOptValues[4] = "<%= resValueTmxBoth %>";
resourceOptValues[5] = "<%= resInsertValueNone %>";

var siteoptNames = new Array;
var siteoptValues = new Array;
siteoptNames[0] = 'ISO-8859-1';
siteoptNames[1] = 'ISO-8859-15';
siteoptNames[2] = 'MacRoman';
siteoptNames[3] = 'UTF-16BE';
siteoptNames[4] = 'UTF-16LE';
siteoptNames[5] = 'UTF-8';
siteoptNames[6] = 'Windows-1252';

siteoptValues[0] = 'ISO-8859-1';
siteoptValues[1] = 'ISO-8859-15';
siteoptValues[2] = 'MacRoman';
siteoptValues[3] = 'UTF-16BE';
siteoptValues[4] = 'UTF-16LE';
siteoptValues[5] = 'UTF-8';
siteoptValues[6] = 'Windows-1252';

function setDisplayExactMatchSelector(formSent)
{
    var formatSelect = formSent.<%= formatSelector %>;
	if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>")
    {
    	document.getElementById("displayExactMatchId").style.display = '';
    }
    else 
    {
        document.getElementById("displayExactMatchId").style.display = 'none';
    }
}

function operateConsolidateTerm()
{
    var selector = document.getElementById("termTypeSelector");
    document.getElementById("consolidateTermCheckBox").disabled = (selector.value == 'termNone');
}
function setEditorSelector(formSent)
{
    var formatSelect   = formSent.<%= formatSelector %>;
    var editorSelect   = formSent.<%= editorSelector %>;
    var encodingSelect = formSent.<%= encodingSelector %>;
    var ptagSelect = formSent.<%= ptagSelector %>;
    ptagSelect.disabled = false;

    editorSelect.options.length = 0;
    if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListView %>")
    {
        editorSelect.options[0] = new Option(editorOptNames[0],editorOptValues[0]);
        editorSelect.options[1] = new Option(editorOptNames[1],editorOptValues[1]);
        editorSelect.options[2] = new Option(editorOptNames[2],editorOptValues[2]);
        editorSelect.options[3] = new Option(editorOptNames[3],editorOptValues[3]);
        editorSelect.options[4] = new Option(editorOptNames[5],editorOptValues[5]);
        editorSelect.selectedIndex = 1;
    }
    else if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTrados %>"
    		|| formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTradosOptimized %>")
    {
        editorSelect.options[0] = new Option(editorOptNames[0],editorOptValues[0]);
        editorSelect.options[1] = new Option(editorOptNames[1],editorOptValues[1]);
        editorSelect.options[2] = new Option(editorOptNames[5],editorOptValues[5]);
        editorSelect.selectedIndex = 1;
    }
    else if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfParaView %>")
    {
        editorSelect.options[0] = new Option(editorOptNames[6],editorOptValues[6]);
        editorSelect.selectedIndex = 0;
    }
    else if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueTextListView %>")
    {
        editorSelect.options[0] = new Option(editorOptNames[0],editorOptValues[0]);
        editorSelect.options[1] = new Option(editorOptNames[1],editorOptValues[1]);
        editorSelect.options[2] = new Option(editorOptNames[2],editorOptValues[2]);
        editorSelect.options[3] = new Option(editorOptNames[3],editorOptValues[3]);
        editorSelect.options[4] = new Option(editorOptNames[4],editorOptValues[4]);
        editorSelect.selectedIndex = 1;
    }
	else if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>")
	{
	    editorSelect.options[0] = new Option(editorOptNames[7],editorOptValues[7]);
		editorSelect.selectedIndex = 0;
		ptagSelect.selectedIndex = 0;
		ptagSelect.disabled = true;
	}
	else if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatTTXValue %>")
	{
        editorSelect.options[0] = new Option(editorOptNames[5],editorOptValues[5]);
        editorSelect.selectedIndex = 0;
        ptagSelect.selectedIndex = 0;
		ptagSelect.disabled = true;
	}
	
//    setDisplayExactMatchSelector(formSent);
    setCharsetSelector(formSent);
    setResourceSelector(formSent);
    setPlaceHolderSelector(formSent);
    setPopulates(formatSelect);
    setConsolidate(formatSelect);
    setRepetitions(formatSelect);
    return true;
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

        theForm.<%= formatSelector %>.disabled = state;
        theForm.<%= editorSelector %>.disabled = state;
        theForm.<%= encodingSelector %>.disabled = state;
        theForm.<%= ptagSelector %>.disabled = state;
        theForm.<%= resInsertionSelector %>.disabled = state;
<%  if(editExact)
    {%>
        theForm.<%= editExactSelector %>.disabled = state;
<%  }%>
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
    return true;
}

function setCharsetSelector(formSent)
{
    var word = formSent.editor;
    var cs = formSent.encoding;
    var resources = formSent.<%= resInsertionSelector %>;

    var idx = 0;
    cs.options.length = 0;

    if (word.options[word.selectedIndex].value == "<%= editorValueOther %>")
    {
        for(i = 0; i < siteoptNames.length; i++)
        {
             cs.options[i] = new Option(siteoptNames[i], siteoptValues[i]);
             if (cs.options[i].text == "UTF-8") idx = i;
        }
    }
    else
    {
        cs.options[0] = new Option("<%= encodingTextDefault %>","<%= encodingValueDefault %>");
    }

    cs.selectedIndex = idx;

    // hack for TagEditor
    setResourceSelector(formSent);
    //if(word.options[word.selectedIndex].value == "<%= editorValueTradosTagEditor %>")
    //{
    //    resources.selectedIndex = 1; //suggest links
    //}

    return true;
}

function setPopulates(formatSelect)
{
	var populatefuzzy = document.getElementById("populatefuzzy");
	
    if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTrados %>"
    		|| formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTradosOptimized %>")
    {
    	populatefuzzy.style.display = "";
    }
    else
    {
    	populatefuzzy.style.display = "none";
    }
}

function setConsolidate(formatSelect) {
	var consolidate = document.getElementById("needConsolidateBox");
	
    if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>"
    		|| formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTrados %>"
    		|| formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTradosOptimized %>")
    {
    	consolidate.style.display = "";
    }
    else
    {
    	consolidate.style.display = "none";
    }
}

function setRepetitions(formatSelect) {
	var includeRepetitionsObj = document.getElementById("includeRepetitionsBox");
	
    if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>"
    		|| formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTrados %>"
    		|| formatSelect.options[formatSelect.selectedIndex].value == "<%= formatValueRtfListViewTradosOptimized %>")
    {
    	includeRepetitionsObj.style.display = "";
    }
    else
    {
    	includeRepetitionsObj.style.display = "none";
    }
}

function setResourceSelector(formSent)
{
    var editor = formSent.editor;
    var resources = formSent.<%= resInsertionSelector %>;
    var formatSelect = formSent.<%= formatSelector %>;

    if (editor.options[editor.selectedIndex].value == "<%= editorValueTradosTagEditor %>")
    {
        resources.options.length = 0;
        resources.options[0] = new Option(resourceOptNames[2],resourceOptValues[2]);
        resources.options[1] = new Option(resourceOptNames[3],resourceOptValues[3]);
        resources.options[2] = new Option(resourceOptNames[4],resourceOptValues[4]);
		resources.options[3] = new Option(resourceOptNames[5],resourceOptValues[5]);
		resources.selectedIndex = 3;
    }
	else if (formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>")
	{
	    resources.options.length=0;
		resources.options[0] = new Option(resourceOptNames[0],resourceOptValues[0]);
	    resources.options[1] = new Option(resourceOptNames[2],resourceOptValues[2]);
        resources.options[2] = new Option(resourceOptNames[3],resourceOptValues[3]);
        resources.options[3] = new Option(resourceOptNames[4],resourceOptValues[4]);
		resources.options[4] = new Option(resourceOptNames[5],resourceOptValues[5]);	
	}
	else if(resources.options.length != 3)
    { 
        resources.options[0] = new Option(resourceOptNames[0],resourceOptValues[0]);
        resources.options[1] = new Option(resourceOptNames[1],resourceOptValues[1]);
        resources.options[2] = new Option(resourceOptNames[2],resourceOptValues[2]);
		resources.options[3] = new Option(resourceOptNames[3],resourceOptValues[3]);
		resources.options[4] = new Option(resourceOptNames[4],resourceOptValues[4]);
		resources.options[5] = new Option(resourceOptNames[5],resourceOptValues[5]);
    }
    return true;
}

function setPlaceHolderSelector(formSent)
{
	var formatSelect = formSent.<%= formatSelector %>;
    var ptagSelect = formSent.<%= ptagSelector %>;
    if(formatSelect.options[formatSelect.selectedIndex].value == "<%= formatXlfName12 %>"
      || formatSelect.options[formatSelect.selectedIndex].value == "<%= formatTTXValue %>" )
    {
    	ptagSelect.selectedIndex = 0;
    	ptagSelect.disabled = true;
	}
	else 
	{
		ptagSelect.disabled = false;
	}	
    return true;
}

    // Constructor
    // Creates a default download options object by reading the client coookies.
    // If cookie does not exist, its value is set to false.
    function ClientDownloadOptions()
    {
    	
   		this.fileFormat = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_FORMAT) :
   						optionsHash.get(UserParamNames.DOWNLOAD_OPTION_FORMAT)%>";
   		
	    
	    this.editor = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_EDITOR) :
	    				optionsHash.get(UserParamNames.DOWNLOAD_OPTION_EDITOR)%>";
	    				
	    this.encoding = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_ENCODING) :
	    				optionsHash.get(UserParamNames.DOWNLOAD_OPTION_ENCODING)%>";
	    				
	    this.ptagFormat = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER) :
	    					optionsHash.get(UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER)%>";
	    					
	    this.resInsMode = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT) :
	    					optionsHash.get(UserParamNames.DOWNLOAD_OPTION_RESINSSELECT)%>";

	    this.consolidate = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX) :
	                        optionsHash.get(UserParamNames.DOWNLOAD_OPTION_CONSOLIDATE_TMX)%>";

	    this.changeCreationIdForMT = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT) :
            				optionsHash.get(UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT)%>";
	                        
	    this.editExact = "<%=(optionsHash == null) ? request.getAttribute(UserParamNames.DOWNLOAD_OPTION_EDITEXACT) :
	    					optionsHash.get(UserParamNames.DOWNLOAD_OPTION_EDITEXACT)%>";
                            
        this.terminology = "<%=(optionsHash == null) ? request.getAttribute("termSelector") :
			optionsHash.get("termSelector")%>";
			
		this.consolidateTerm = "<%=(optionsHash == null) ? request.getAttribute("consolidateTerm") :
			optionsHash.get("consolidateTerm")%>";  

	    this.populate100 = "<%=(optionsHash == null) ? request.getAttribute("populate100") :
			optionsHash.get("populate100")%>";
	    this.populatefuzzy = "<%=(optionsHash == null) ? request.getAttribute("populatefuzzy") :
			optionsHash.get("populatefuzzy")%>"; 
		this.needConsolidate = "<%=(optionsHash == null) ? request.getAttribute("needConsolidate") :
			optionsHash.get("needConsolidate")%>";
		this.includeRepetitions = "<%=(optionsHash == null) ? request.getAttribute("includeRepetitions") :
			optionsHash.get("includeRepetitions")%>";
    }

/*
* Reads the users settings from a locale cookie
* If cookies values are not found, we default to the values selected by the HTML
*/
function setClientDwnldOptions(formSent)
{
    // get client download options
    var dwnldOpt = new ClientDownloadOptions();

    // get selectors
    var formatSelect = formSent.<%= formatSelector %>;
    var editorSelect = formSent.<%= editorSelector %>;
    var encodingSelect = formSent.<%= encodingSelector %>;
    var ptagSelect = formSent.<%= ptagSelector %>;
    var resInsModeSelect = formSent.<%= resInsertionSelector %>;
    // Set user defaults
	var terminologySelect = formSent.termTypeSelector;

    // We only set format/editor/encoding if we have all three values
    if (dwnldOpt.fileFormat.length > 0 && dwnldOpt.editor.length > 0 &&
        dwnldOpt.encoding.length > 0)
    {
	// Set FileFormat
	for(i = 0; i < formatSelect.length; i++)
	{
            if (formatSelect.options[i].value == dwnldOpt.fileFormat)
            {
                formatSelect.selectedIndex = i;
				break;
            }
	}
	// Set Editor: first, establish editor selections based on
    // FileFormat set above
	setEditorSelector(formSent);
	for (i = 0; i < editorSelect.length; i++)
	{
            if (editorSelect.options[i].value == dwnldOpt.editor)
            {
                editorSelect.selectedIndex = i;
		break;
            }
	}
	// Set encoding: first, establish encoding selections based on
        // editor set above
	setCharsetSelector(formSent);
	for (i = 0; i < encodingSelect.length; i++)
	{
            if (encodingSelect.options[i].value == dwnldOpt.encoding)
            {
                encodingSelect.selectedIndex = i;
		break;
            }
	}
    }
    // Set Placeholder
    if (dwnldOpt.ptagFormat)
    {
      for(i = 0; i < ptagSelect.length; i++)
      {
         if (ptagSelect.options[i].value == dwnldOpt.ptagFormat)
         {
            ptagSelect.selectedIndex = i;
            break;
         }
      }
    }
    // Set resource insertion mode
    if (dwnldOpt.resInsMode)
    {
      for(i = 0; i < resInsModeSelect.length; i++)
      {
            if (resInsModeSelect.options[i].value == dwnldOpt.resInsMode)
            {
                resInsModeSelect.selectedIndex = i;
                break;
            }
      }
    }
    
    if (dwnldOpt.consolidate == 'yes')
    {
        document.getElementById("consolidateCheckBox").checked = true;
    }

    if (dwnldOpt.changeCreationIdForMT == 'yes')
    {
    	document.getElementById("changeCreationIdForMT").checked = true;
    }
    
	if(dwnldOpt.consolidateTerm == 'true' || dwnldOpt.consolidateTerm == 'yes')
	{
		document.getElementById("consolidateTermCheckBox").checked = true;
	}
	
<%  if (editExact) {%>
    var editExactSelect = formSent.<%= editExactSelector %>;

    // Set EditExact
    if(dwnldOpt.editExact)
    {
        for(i = 0; i < editExactSelect.length; i++)
	{
            if (editExactSelect.options[i].value == dwnldOpt.editExact)
            {
                editExactSelect.selectedIndex = i;
		break;
            }
        }
    }
<% } %>

	if(dwnldOpt.terminology)
	{
		for(var i = 0; i < terminologySelect.length; i++)
		{
			if(terminologySelect.options[i].value == dwnldOpt.terminology)
			{
				terminologySelect.selectedIndex = i; 
				break;
			}
		}
	}
    //disablePTFOptions(false); // overriden if there are no extracted files
    operateConsolidate();
    operateConsolidateTerm();

	if (dwnldOpt.populate100)
	{
		if(dwnldOpt.populate100 == 'false' || dwnldOpt.populate100 == 'no')
		{
			document.getElementById("populate100CheckBox").checked = false;
		}
	}

	if (dwnldOpt.populatefuzzy)
	{
		if(dwnldOpt.populatefuzzy == 'false' || dwnldOpt.populatefuzzy == 'no')
		{
			document.getElementById("populatefuzzyCheckBox").checked = false;
		}
	}
	
	if (dwnldOpt.needConsolidate)
	{
		if(dwnldOpt.needConsolidate == 'false' || dwnldOpt.needConsolidate == 'no')
		{
			document.getElementById("needConsolidate").checked = false;
		}
	}
	
	if (dwnldOpt.includeRepetitions)
	{
		if(dwnldOpt.includeRepetitions == 'false' || dwnldOpt.includeRepetitions == 'no')
		{
			document.getElementById("includeRepetitions").checked = false;
		}
	}
}

function saveUserOptions(formSent)
{
    //var duration = 12; // months
    var formatSelect = formSent.<%= formatSelector %>;
    var editorSelect = formSent.<%= editorSelector %>;
    var encodingSelect = formSent.<%= encodingSelector %>;
    var ptagSelect = formSent.<%= ptagSelector %>;
    var resInsModeSelect = formSent.<%= resInsertionSelector %>;

var terminologySelect = formSent.termTypeSelector;

    var cookieValFileFormat = formatSelect.options[formatSelect.selectedIndex].value;
    var cookieValEditor = editorSelect.options[editorSelect.selectedIndex].value;
    if(cookieValFileFormat == 'xlf12')
    {
    	cookieValEditor = "<%=editorXlfValue%>";
    }
    
    var cookieValEncoding = encodingSelect.options[encodingSelect.selectedIndex].value;
    var cookieValPtagFormat = ptagSelect.options[ptagSelect.selectedIndex].value;
    var cookieValResInsMode = resInsModeSelect.options[resInsModeSelect.selectedIndex].value;
    var cookieValTerm = terminologySelect.options[terminologySelect.selectedIndex].value;
    
    
    var cookieValEditExact = null; // set below - if enabled

<% if(editExact)
{%>
    var editExactSelect = formSent.<%= editExactSelector %>;
    cookieValEditExact = editExactSelect.options[editExactSelect.selectedIndex].value;
<%}%>

	downloadForm.action += buildParams(cookieValFileFormat, cookieValEditor, cookieValEncoding, cookieValPtagFormat, cookieValResInsMode, cookieValEditExact, cookieValTerm);
    downloadForm.submit();
}

function buildParams(cookieValFileFormat, cookieValEditor, cookieValEncoding, cookieValPtagFormat, cookieValResInsMode, cookieValEditExact, cookieValTerm)
{
	var str = "&";
	str += "format=" + cookieValFileFormat;
	str += "&editor=" + cookieValEditor;
	str += "&encoding=" + cookieValEncoding;
	str += "&placeholder=" + cookieValPtagFormat;
	str += "&resInsSelector=" + cookieValResInsMode;
	str += "&editExact" + cookieValEditExact;
	str += "&termSelector" + cookieValTerm;
	return str;
}

function doOnLoad()
{
    loadGuides();
    setClientDwnldOptions(document.downloadForm);
}

function operateConsolidate()
{
    var selector = document.getElementById("tmxTypeSelector");
    var text = selector.options[selector.selectedIndex].value;

    if (text == "<%= resValueTmxPlain %>" || text == "<%= resValueTmx14b %>" || text == "<%= resValueTmxBoth %>")
    {
       document.getElementById("consolidateCheckBox").disabled=false;
       document.getElementById("changeCreationIdForMT").disabled=false;
    }
    else
    {
       document.getElementById("consolidateCheckBox").disabled=true;
       document.getElementById("changeCreationIdForMT").disabled=true;
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
                <SELECT onChange="setEditorSelector(this.form);" NAME="<%= formatSelector %>" CLASS="standardText" >
                  <!-- <OPTION VALUE="-"><%= formatStartHere %></OPTION> -->
                  <OPTION VALUE="<%= formatValueRtfListViewTrados %>"><%= formatRtfListViewTrados %></OPTION>
                  <OPTION VALUE="<%= formatValueRtfListViewTradosOptimized %>"><%= formatRtfListViewTradosOptimized %></OPTION>
                  <OPTION VALUE="<%= formatValueTextListView %>"><%= formatTextListView %></OPTION>
                   <% if(EditHelper.isParagraphEditorInstalled())
                  {
                  %>
                  <!-- <OPTION VALUE="<%= formatValueRtfParaView %>" ><%= formatRtfParaView %></OPTION> -->
                  <%
                  }
                  %>
                  <OPTION VALUE="<%= formatValueRtfListView %>"><%= formatRtfListView %></OPTION>
                  <OPTION VALUE="<%= formatXlfName12 %>"><%=formatXlfValue12 %></OPTION>
                  <OPTION VALUE="<%= formatTTXValue %>"><%=formatTTXName %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText">Editor:</SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT onChange="setCharsetSelector(this.form);" NAME="<%= editorSelector %>" CLASS="standardText">
                  <!-- <OPTION VALUE="-">-----------------------------</OPTION> -->
                  <OPTION VALUE="<%= editorValueWin2000 %>"><%= editorTextWin2000%></OPTION>

                  <OPTION VALUE="<%= editorValueWin97 %>" SELECTED><%= editorTextWin97 %></OPTION>

                  <OPTION VALUE="<%= editorValueMac2001 %>"><%= editorTextMac2001 %></OPTION>

                  <OPTION VALUE="<%= editorValueMac98 %>"><%= editorTextMac98 %></OPTION>

                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= encoding %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT NAME="<%= encodingSelector %>" CLASS="standardText">
                  <OPTION VALUE="-" SELECTED>-----------------------------</OPTION>
                  <OPTION VALUE="<%= encodingValueDefault %>" SELECTED><%= encodingTextDefault %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= placeholderFormat %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT NAME="<%= ptagSelector %>" CLASS="standardText">
                  <OPTION VALUE="<%= ptagValueCompact %>" SELECTED><%= ptagTextCompact %></OPTION>
                  <OPTION VALUE="<%= ptagValueVerbose %>"><%= ptagTextVerbose %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= labelResInsertion %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT id="tmxTypeSelector" NAME="<%= resInsertionSelector %>" CLASS="standardText" onchange="operateConsolidate()">
                  <OPTION VALUE="<%= resInsertValueAtns %>" SELECTED><%= resInsertTextAtns %></OPTION>
                  <OPTION VALUE="<%= resInsertValueSingleLink %>"><%= resInsertTextSingleLink %></OPTION>
                  <OPTION  VALUE="<%= resValueTmxPlain %>"><%= resTmxPlain %></OPTION>
                  <OPTION VALUE="<%= resValueTmx14b %>"><%= resTmx14b %></OPTION>
                  <OPTION VALUE="<%= resValueTmxBoth %>"><%= resTmxBoth %></OPTION>
                  <OPTION VALUE="<%= resInsertValueNone %>"><%= resInsertTextNone %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD></TD>
              <TD><SPAN CLASS="standardText"><input id="consolidateCheckBox" type="checkbox" name="<%=OfflineConstants.CONSOLIDATE_TMX %>" value="true"/><%=bundle.getString("lb_consolidate_tmx_files") %></SPAN></TD>
            </TR>
  			<TR>
              <TD></TD>
			  <TD><SPAN CLASS="standardText"><input type="checkbox" id="changeCreationIdForMT" name="<%=UserParamNames.DOWNLOAD_OPTION_CHANGE_CREATIONID_FOR_MT %>"/><%=bundle.getString("lb_tm_export_change_creationid_for_mt")%></SPAN></TD>
			</TR>
            <TR>
              <TD><SPAN CLASS="standardText"><%= labelTerminology %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT id="termTypeSelector" NAME="<%= OfflineConstants.TERM_SELECTOR %>" CLASS="standardText" onchange="operateConsolidateTerm()">
                  <OPTION VALUE="<%= OfflineConstants.TERM_NONE %>" SELECTED><%= resInsertTextNone %></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_GLOBALSIGHT %>"><%=bundle.getString("lb_terminology_globalsight_format")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TRADOS %>"><%=bundle.getString("lb_terminology_multiterm_ix_format")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_HTML %>"><%=bundle.getString("lb_terminology_html")%></OPTION>
                  <OPTION VALUE="<%= OfflineConstants.TERM_TBX %>"><%=bundle.getString("lb_terminology_import_format_tbx")%></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
            <TR>
              <TD></TD>
              <TD><SPAN CLASS="standardText"><input id="consolidateTermCheckBox" type="checkbox" name="<%=OfflineConstants.CONSOLIDATE_TERM %>" value="true" disabled="disabled" /><%=bundle.getString("lb_consolidate_term_files") %></SPAN></TD>
            </TR>
<%
    if(editExact)
    {
%>
            <TR>
              <TD><SPAN CLASS="standardText"><%= exactMatchesEditable %></SPAN></TD>
              <TD><SPAN CLASS="standardText">
                <SELECT NAME="<%= editExactSelector %>" CLASS="standardText">
                  <OPTION VALUE="<%= editExactValueNo %>" SELECTED><%= editExactTextNo %></OPTION>
                  <OPTION VALUE="<%= editExactValueYes %>"><%= editExactTextYes %></OPTION>
                </SELECT>
              </SPAN></TD>
            </TR>
<%
}
%>
            <TR id="populate100">
                <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_populate_100_target_segment") %></SPAN></TD>
                <TD><SPAN CLASS="standardText">
                <input id="populate100CheckBox" type="checkbox" name="<%=OfflineConstants.POPULATE_100%>" checked="checked" value="true"/></SPAN>
                </TD>
            </TR>
             <TR id="populatefuzzy">
                <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_populate_fuzzy_target_segment") %></SPAN></TD>
                <TD><SPAN CLASS="standardText">
                <input id="populatefuzzyCheckBox" type="checkbox" name="<%=OfflineConstants.POPULATE_FUZZY%>" checked="checked" value="true"/></SPAN>
                </TD>
            </TR>
            <TR id="needConsolidateBox">
            	<TD><SPAN CLASS="standardText"><%=bundle.getString("lb_download_consolate") %></SPAN></TD>
                <TD>
                    <SPAN CLASS="standardText">
                      <input type="checkbox" id="needConsolidate" name="needConsolidate" value="true" checked="checked">
                    </SPAN>
                </TD>
            </TR>
            <TR id="includeRepetitionsBox">
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

     <INPUT type="button" name="cancel" value="<%=lb_cancel%>"
      onclick="location.replace('<%=url_cancel%>')">
&nbsp;&nbsp;
      <INPUT type="submit" name="apply" value="<%=lb_ok%>" onclick="submitForm()"> 
</FORM>
</DIV>
</BODY>
</HTML>

