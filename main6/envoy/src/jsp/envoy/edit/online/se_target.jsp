<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper,
        	com.globalsight.terminology.Hitlist,
        	com.globalsight.terminology.ITermbase,
        	com.globalsight.terminology.termleverager.TermLeverageMatchResult,
            com.globalsight.everest.edit.online.SegmentVersion,
            com.globalsight.everest.edit.online.SegmentView,
        	com.globalsight.everest.edit.online.SegmentMatchResult,
        	com.globalsight.everest.page.SourcePage,
        	com.globalsight.everest.servlet.util.ServerProxy,
        	com.globalsight.everest.persistence.tuv.SegmentTuUtil,
        	com.globalsight.ling.docproc.extractor.xliff.XliffAlt,
        	com.globalsight.ling.docproc.extractor.xliff.Extractor,
        	com.globalsight.util.edit.GxmlUtil,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.date.DateHelper,
            com.globalsight.ling.common.Text,
            com.globalsight.ling.tm.TuvBasicInfo,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            java.util.Collection,
            java.util.Iterator,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle,
            java.io.File,
            java.text.NumberFormat,
            com.globalsight.machineTranslation.MachineTranslator,
            com.globalsight.machineTranslation.MTHelper2,
            com.globalsight.everest.tuv.Tuv,
            java.text.DecimalFormat"
    session="true"
%>
<jsp:useBean id="mtTranslation" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String lb_select_ptag = bundle.getString("lb_select_ptag");
String lb_select_ptag1 = bundle.getString("lb_select_ptag1");
String lb_no_ptags_in_segment = bundle.getString("lb_no_ptags_in_segment");
String lb_close = bundle.getString("lb_close");
String lb_target = bundle.getString("lb_target");

// Get MT translation URL for dojo
String mtTranslationMessageURL = mtTranslation.getPageURL() 
    + "&action=" + MTHelper2.ACTION_GET_MT_TRANSLATION_MESSAGE;
		
String mtTranslationURL = mtTranslation.getPageURL() 
+ "&action=" + MTHelper2.ACTION_GET_MT_TRANSLATION;

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

String str_langAttr = EditUtil.getLanguageAttributes(
  state.getTargetLocale(), view.isLocalizable());
String str_sourceSegment = GxmlUtil.getInnerXml(view.getSourceSegment());
//
// TB Matches
//
String str_defaultTermbaseName = state.getDefaultTermbaseName();
long l_defaultTermbaseId = state.getDefaultTermbaseId();
boolean b_haveTermbase = (str_defaultTermbaseName != null);
boolean canAccessTB = state.isCanAccessTB();
String dataFormat = state.getPageFormat();

Collection tbMatches = view.getTbMatchResults();

NumberFormat percent = NumberFormat.getPercentInstance(
  (Locale)session.getAttribute(WebAppConstants.UILOCALE));

boolean b_rtl = EditUtil.isRTLLocale(state.getTargetLocale());
boolean b_source_rtl = EditUtil.isRTLLocale(state.getSourceLocale());

String lb_tbMatchResults = bundle.getString("lb_tb_match_results");
String lb_clickToCopy  = bundle.getString("action_click_copy");
String lb_noTerms      = bundle.getString("lb_tb_no_match_results");
String lb_clickToOpenTb = bundle.getString("lb_click_to_open_tb");
String lb_explanation = bundle.getString("lb_target_terms_tip");
String lb_matches_from_tb = bundle.getString("lb_matches_from_tb");

String lb_matchResults = bundle.getString("lb_mt_match_results");
String lb_noMTSegments   = bundle.getString("lb_no_mt_match_results");
String lb_SourceName = bundle.getString("lb_match_source");
String lb_TargetName = bundle.getString("lb_match_target");
//
// TM Matches
//
String lb_tmMatchResults = bundle.getString("lb_match_results");
String lb_noSegments = bundle.getString("lb_no_match_results");
String lb_details = bundle.getString("lb_details");

StringBuffer alt_segments = new StringBuffer();

StringBuffer term_segments = new StringBuffer();

List tmMatches = view.getTmMatchResults();

SourcePage sp = null;
try 
{
    sp = ServerProxy.getPageManager().getSourcePage(state.getSourcePageId());
} 
catch (Exception e) {}

long tuId = state.getTuId();
TuImpl tu = null;
try
{
	tu = SegmentTuUtil.getTuById(tuId, sp.getJobId());
}
catch (Exception e) {}

boolean isMtTranslate = tu.isXliffTranslationMT();
boolean isWSXlf = (TuImpl.FROM_WORLDSERVER.equals(tu.getGenerateFrom()));
float tm_score = -1;
try 
{
    tm_score = Float.parseFloat(((TuImpl) tu).getIwsScore());
} 
catch (Exception e) {}

StringBuffer stb_segments = new StringBuffer();

if (tmMatches != null)
{
    int i = 0;
    String locale = "en-US";

    if (!view.isLocalizable())
    {
      locale = EditUtil.toRFC1766(state.getTargetLocale());
    }

    for (Iterator it = tmMatches.iterator(); it.hasNext(); )
    {
        SegmentMatchResult p = (SegmentMatchResult)it.next();

        stb_segments.append("a_tmSegments[");
        stb_segments.append(i++);
        stb_segments.append("] = { data: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            p.getMatchContent())));
        stb_segments.append("\", text: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            EditUtil.stripTags(p.getMatchContent()))));		
        stb_segments.append("\", matchedSource: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
           p.getMatchContentSource())));
        stb_segments.append("\", label: \"");
        if (isWSXlf && p.getTmName() != null && p.getTmName().endsWith("_MT") && tm_score >= 0)
        {
            stb_segments.append(percent.format(Math.floor(tm_score*100)/10000.0));
        }
        else
        {
            stb_segments.append(percent.format(Math.floor(p.getMatchPercentage())*100/10000.0));
        }
        String tmName = p.getTmName();
        stb_segments.append("(");
        stb_segments.append(tmName);
        stb_segments.append(")");
        // Need extra info for MT translation for WS XLF file.
        // "translation_type='machine_translation_mt'" in source XLF file.
        if (isMtTranslate && "xliff".equalsIgnoreCase(tmName))
        {
           stb_segments.append(tu.getXliffTMAttributes());
        }
        // MT translation is from GlobalSight MT
        else if (isWSXlf && p.getTmName() != null && p.getTmName().endsWith("_MT"))
        {
            stb_segments.append(" (translation_type : machine_translation_mt)");
        }

        stb_segments.append("\", tuvId: \"");
        stb_segments.append(p.getTuvId());
        
        stb_segments.append("\", sid: \"");
        
        String sid = null;
        if("In Progress TM".equals(tmName))
        {
           sid = (p.getMatchedTuvBasicInfo()==null?"N/A":p.getMatchedTuvBasicInfo().getSid());
        }
        else
        {
           sid = p.getSid();
        }
        if (null == sid) {
            sid = "N/A";
        }
        stb_segments.append(EditUtil.encodeXmlEntities(EditUtil.toJavascript(EditUtil.xmlToHtml(
                sid))));
        stb_segments.append("\", srcLocaleId: \"");
        stb_segments.append(state.getSourceLocale().getId());
        stb_segments.append("\", lang: \"");
        stb_segments.append(locale);
        stb_segments.append("\", dir: \"");
        if (b_rtl)
        {
            stb_segments.append(
                Text.containsBidiChar(p.getMatchContent()) ? "RTL" : "LTR");
        }
        else
        {
            stb_segments.append("LTR");
        }
        
        stb_segments.append("\", srcDir: \"");
        if (b_source_rtl)
        {
            if(!(p.getMatchContentSource().equals("")))
            {
                stb_segments.append(
                        Text.containsBidiChar(p.getMatchContentSource()) ? "RTL" : "LTR");
            }
            else
            {
                stb_segments.append(
                        Text.containsBidiChar(str_sourceSegment) ? "RTL" : "LTR");
            }
            
        }
        else
        {
            stb_segments.append("LTR");
        }
        
        TuvBasicInfo matchedTuvBasicInfo = p.getMatchedTuvBasicInfo();
        String matchedTuvJobName = p.getMatchedTuvJobName()==null?"N/A":p.getMatchedTuvJobName();
        String creationUser = (matchedTuvBasicInfo==null)?"N/A":EditUtil.encodeXmlEntities(UserUtil.getUserNameById(matchedTuvBasicInfo.getCreationUser()));
        String creationDate  = (matchedTuvBasicInfo==null)?"N/A":DateHelper.getFormattedDateAndTime(matchedTuvBasicInfo.getCreationDate());
        String modifyUser = (matchedTuvBasicInfo==null||matchedTuvBasicInfo.getModifyUser()==null)?"N/A":EditUtil.encodeXmlEntities(UserUtil.getUserNameById(matchedTuvBasicInfo.getModifyUser()));
        String modifyDate  = (matchedTuvBasicInfo==null||modifyUser=="N/A")?"N/A":DateHelper.getFormattedDateAndTime(matchedTuvBasicInfo.getModifyDate());
		String lmMatchType = (p.getMatchType()==null?"N/A":p.getMatchType());
        
        stb_segments.append("\", creationDate: \"");
        stb_segments.append(creationDate);
        stb_segments.append("\", creationUser: \"");
        stb_segments.append(creationUser);
        stb_segments.append("\", modifyDate: \"");
        stb_segments.append(modifyDate);
        stb_segments.append("\", modifyUser: \"");
        stb_segments.append(modifyUser);
        stb_segments.append("\", matchedTuvJobName: \"");
        stb_segments.append(matchedTuvJobName);
        stb_segments.append("\", tmName: \"");
        stb_segments.append(tmName);
        stb_segments.append("\", matchType: \"");
        stb_segments.append(lmMatchType);

		stb_segments.append("\" };\n");
    }
}

String str_tmSegments = stb_segments.toString();

List xliffAltSet = view.getXliffAlt();

boolean hasXliff = false;
if (xliffAltSet != null && xliffAltSet.size() > 0)
{
    hasXliff = true;
    int i = 0;
    
    for (Iterator it = xliffAltSet.iterator(); it.hasNext(); )
    {
        XliffAlt altTrans= (XliffAlt)it.next();
        alt_segments.append("a_AltSegments[");
        alt_segments.append(i++);
        alt_segments.append("] = { data: \"");
        alt_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            GxmlUtil.getInnerXml(altTrans.getGxmlElement()))));
        alt_segments.append("\", text: \"");
        alt_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
                GxmlUtil.getInnerXml(altTrans.getGxmlElement()))));
        alt_segments.append("\",matchedSource:\"");
        alt_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
                GxmlUtil.getInnerXml(altTrans.getGxmlElementSource()))));
        alt_segments.append("\", label: \"");

        if(altTrans.getQuality() != null) {
            DecimalFormat df = new DecimalFormat("0.00"); 
            double qua = Double.parseDouble(altTrans.getQuality());
            String str = df.format(qua) + "%";
            alt_segments.append(str);
        }
        else {
            alt_segments.append("");
        }
        
        alt_segments.append("\" };\n");
    }
}

String str_altSegments = alt_segments.toString();

if (tbMatches != null && tbMatches.size() > 0){
    int i = 0;
 
    for (Iterator it = tbMatches.iterator(); it.hasNext(); ) {
        TermLeverageMatchResult p = (TermLeverageMatchResult)it.next();
        Hitlist.Hit sourceHit = p.getSourceHit();
        
        String str_target;
        long l_conceptId;
        long l_termId;
    
        for (Iterator it1 = p.getTargetHitIterator(); it1.hasNext(); )
        {
            Hitlist.Hit hit = (Hitlist.Hit)it1.next();
    
            str_target = hit.getTerm();
    
            if (b_rtl)
            {
                str_target = "<SPAN DIR=rtl>" + str_target + "</SPAN>";
            }
    
            l_conceptId = hit.getConceptId();
            l_termId = hit.getTermId();
            // i_score = hit.getScore();
          
            //for show terminology image
            String termImgLink = "";
          
            String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
            File parentFilePath = new File(termImgPath.toString());
            File[] files = parentFilePath.listFiles();
                    
            if (files != null && files.length > 0) {
                for (int j = 0; j<files.length; j++) {
                    File file = files[j];
                    String fileName = file.getName();
                        
                    if(fileName.lastIndexOf(".") > 0) {
                        String tempName= fileName.substring(0, fileName.lastIndexOf("."));

                        if(tempName.equals("tb_"+Long.toString(l_termId))) {
                           termImgLink = "<a href=\"#\" onClick=\"termImgShow('" + fileName + "');\">" 
                               + " <img src=images\\image.gif border=0 width=20 height=25 align=\"absmiddle\">" + "</a>";
                        }
                   }
                }
             }

            term_segments.append("a_TermSegments[");
            term_segments.append(i++);
            term_segments.append("] = { data: \"");
            term_segments.append(EditUtil.toJavascript(hit.getTerm()));
            term_segments.append("\", text: \"");
            term_segments.append(EditUtil.toJavascript(EditUtil.encodeXmlEntities(sourceHit.getTerm())));
            term_segments.append("<br><div class='targetTerm' cid='" + l_conceptId +"' tid='");
            term_segments.append(l_termId+"'><b>&nbsp;&nbsp;" + EditUtil.toJavascript(EditUtil.encodeXmlEntities(str_target)));
            term_segments.append("</b>&nbsp;&nbsp;"+ EditUtil.toJavascript(termImgLink ) +"<div>");
            term_segments.append("\", label: \"");
            term_segments.append("");
            
            term_segments.append("\" };\n");
         }
     }
}

String str_termSegments = term_segments.toString();

//
// Segment Versions
//
String lb_segmentVersions = bundle.getString("lb_segment_versions");
String lb_noVersions      = bundle.getString("lb_no_segment_versions");
String lb_task_unknown    = bundle.getString("lb_task_unknown");

List stages = view.getSegmentVersions();

stb_segments.setLength(0);
if (stages != null)
{
    int i = 0;
    String locale = "en-US";

    if (!view.isLocalizable())
    {
      locale = EditUtil.toRFC1766(state.getTargetLocale());
    }

    for (Iterator it = stages.iterator(); it.hasNext(); )
    {
        SegmentVersion v = (SegmentVersion)it.next();

        stb_segments.append("a_stageSegments[");
        stb_segments.append(i++);
        stb_segments.append("] = { data: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            v.getSegment())));
        stb_segments.append("\", text: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            EditUtil.stripTags(v.getSegment()))));
        stb_segments.append("\", label: \"");

        String lb_stage = v.getTaskName();
        if (lb_stage.length() == 0) lb_stage = lb_task_unknown;

        stb_segments.append("Task Name:");
        stb_segments.append(lb_stage);
        stb_segments.append("&nbsp;&nbsp;");
        stb_segments.append(bundle.getString("lb_last_modify_user"));
        stb_segments.append(":");
		
		    String lastModifyUser = UserUtil.getUserNameById(v.getLastModifyUser());
		    
        if(lastModifyUser != null) {
           stb_segments.append(lastModifyUser);
        }
        else {
            stb_segments.append(bundle.getString("msg_no_user_modify")); 
        }
            
        stb_segments.append("\", lang: \"");
        stb_segments.append(locale);
        stb_segments.append("\", dir: \"");
        if (b_rtl)
        {
            stb_segments.append(
                Text.containsBidiChar(v.getSegment()) ? "RTL" : "LTR");
        }
        else
        {
            stb_segments.append("LTR");
        }
        stb_segments.append("\" };\n");
    }
}

String str_stageSegments = stb_segments.toString();

//if show mt in segment editor
boolean show_in_editor = false;
boolean show_MT = false;
try 
{
	  String showMachineTranslation = (String) sessionMgr.getAttribute("showMachineTranslation");
	  show_MT = (new Boolean(showMachineTranslation)).booleanValue();
	  
    String showInEditor = (String) sessionMgr.getAttribute(MTHelper2.SHOW_IN_EDITOR);
    show_in_editor = (new Boolean(showInEditor)).booleanValue();
} 
catch (Exception e) { }
%>

<%@page import="com.globalsight.everest.tuv.TuImpl"%>
<%@page import="com.globalsight.persistence.hibernate.HibernateUtil"%><HTML>
<!-- This is envoy\edit\online\se_target.jsp -->
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script SRC="/globalsight/includes/spellcheck.js"></script>
<script SRC="/globalsight/spellchecker/jsp/spellcheck.js"></script>
<script SRC="/globalsight/includes/filter/StringBuffer.js"></script>
<script SRC="/globalsight/xdespellchecker/noapplet/SpellCheckNoApplet.js"></script>
<SCRIPT SRC="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dnd/DragAndDrop.js"></SCRIPT>
<script src="/globalsight/includes/ajaxJquery/online.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<link type="text/css" rel="StyleSheet" id="cssPtag"
  href="/globalsight/envoy/edit/online2/ptag.css">
<link type="text/css" rel="StyleSheet"
  href="/globalsight/includes/menu/skins/winclassic.css">
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<script src="/globalsight/includes/menu/js/menu4.js"></script>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>

<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none}

#textarea { font-size: 10pt; font-family: Courier; }

body {  border: 0; margin: 0; overflow:auto ; overflow-x:hidden}

iframe {
    margin-top: 5px;
    width: 95%;
    height: 100%;
    border: 2px inset;
}

td.coolButton {
    font-family: Arial;
    font-size: 14px;
    width: 16px;
    height: 16px;
    font: menu;
}
  
.link      { color: blue; cursor: hand; cursor:pointer;}
.clickable { font-family:Arial, Helvetica, sans-serif; font-size: 9pt;
         color: blue; cursor: hand; cursor:pointer;}
</STYLE>

<script src="/globalsight/envoy/edit/online/stringbuilder.js"></script>
<script src="/globalsight/envoy/edit/online/richedit.js"></script>
<script src="/globalsight/envoy/edit/online/coolbuttons.js"></script>
<script type="text/javascript" src="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></script>
<SCRIPT type="text/JavaScript">
var isRtl = "<%=b_rtl%>";

var a_tmSegments = new Array();
<%=str_tmSegments%>

var a_AltSegments = new Array();
<%=str_altSegments%>

var a_TermSegments = new Array();
<%=str_termSegments%>


var a_stageSegments = new Array();
<%=str_stageSegments%>

var g_segmentIndex = 0;
var g_altIndex = 0;
var g_termIndex = 0;
var g_stageIndex = 0;

var g_ptagsverbose =
  "<%=state.getPTagFormat()%>" == "<%=EditorConstants.PTAGS_VERBOSE%>";

var g_defaultTermbaseId = "<%=l_defaultTermbaseId%>";
var g_haveTermbase = eval("<%=b_haveTermbase%>");
var g_selectedTerm = null;

var g_SC_GSA = new SC_GSA_Parameters();
var g_SC_XDE = new SC_XDE_Parameters();
var g_canSpellcheck = true;
var w_scwin = null;

var w_ptags = null;
var w_corpus = null;

var o_textbox = null;
var b_inited = false;

var tmp;

var lb_copy_to_editor;
var lb_insert_selection_in_editor;
var lb_corpus_context;
var isIE 		= window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox 	= window.navigator.userAgent.indexOf("Firefox")>0;
var b_canEditInSameWindow = true;
var fontGray1 = "<font color='gray'>";
var fontGray2 = "</font>";

// Menus

Menu.prototype.cssFile = "/globalsight/includes/menu/skins/winclassic.css";
Menu.prototype.mouseHoverDisabled = false;
Menu.prototype.showTimeout = 5;
Menu.prototype.closeTimeout = 5;
MenuButton.prototype.subMenuDirection = "horizontal";
// Menu.keyboardAccelKey = -1;
// Menu.keyboardAccelProperty = "";

// Alt Context Menu
var altMenu = new Menu();
altMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_copy_to_editor") %>", copyAltToEditor));

var termMenu = new Menu();
termMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_in_editor") %>", InsertTerm));
termMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_browse_term") %>", browseTerm));
                       
// Segment Match Context Menu
var tmMatchContextMenu = new Menu();
tmMatchContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_copy_to_editor") %>",
                       copyMatchToEditor));
tmp.mnemonic = "c";
tmMatchContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_selection_in_editor") %>",
                       copySelectionToEditor));
tmp.mnemonic = "s";
var copyTmMatchSelectionMni = tmp;
<% if (b_corpus) { %>
tmMatchContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_corpus_context") %>", showCorpus));
tmp.mnemonic = "x";
<% } %>

// Segment Version Context Menu
var tmVersionContextMenu = new Menu();
tmVersionContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_copy_to_editor") %>",
                         copyVersionToEditor));
tmp.mnemonic = "c";
tmVersionContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_selection_in_editor") %>",
                         copySelectionToEditor));
tmp.mnemonic = "s";
var copyTmVersionSelectionMni = tmp;

// Termbase Context Menu
var tbContextMenu = new Menu();
tbContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_in_editor") %>", InsertTerm));
tmp.mnemonic = "c";
tbContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_browse_term") %>", browseTerm));
tmp.mnemonic = "b";

// Context Menu

var lastKeyCode = 0;

function rememberKeyCode()
{
  //lastKeyCode = window.event.keyCode;
  lastKeyCode = getEvent().keyCode;	  
}

function showContextMenu()
{
  g_selectedTerm = null;
  var event,selection;

  // Has the user right-clicked the segment match?
  var el;
  if(isIE)
  {
	event = window.event;
	el = event.srcElement;
	selection =	document.selection;  
  }
  else
  {
	event = getEvent();
	el = event.target;
	selection =	window.getSelection();
  }
	  
  while (el != null && el.tagName != "TD") { el = el.parentNode; }
  var showTmMatchMenu = (el != null && el.id == 'idSegmentText');
  var showTmVersionMenu = (el != null && el.id == 'idStageText');
  var showAltMenu = (el != null && el.id == 'idAltSegmentText');
  var showTermMenu = (el != null && el.id == 'idTermSegmentText');

  if(isIE)
  {
	el = event.srcElement;
  }
  else
  {
	el = event.target;
  }
  
  while (el != null && el.tagName != "DIV") { el = el.parentNode; };
  var showTbMenu = (el != null && el.className == 'targetTerm');

  if (showTbMenu)
  {
    g_selectedTerm = el;
  }
  
  if (showAltMenu && a_AltSegments.length > 0) {
    // find left and top
    var left = event.screenX || event.pageX;
    var top  = event.screenY || event.pageY;
    
    altMenu.invalidate();
    altMenu.show(left, top);
  }
  
  if (showTbMenu && a_TermSegments.length > 0) {
    // find left and top
    var left = event.screenX || event.pageX;
    var top  = event.screenY || event.pageY;
    
    termMenu.invalidate();
    termMenu.show(left, top);
  }

  if ((showTmMatchMenu && a_tmSegments.length > 0) ||
      (showTmVersionMenu && a_stageSegments.length > 0))
  {
    // find left and top
    var left = event.screenX || event.pageX;
    var top  = event.screenY || event.pageY;

    if (showTmMatchMenu)
    {
      if (selection.type == 'None')
      {
        copyTmMatchSelectionMni.disabled = true;
      }
      else
      {
        copyTmMatchSelectionMni.disabled = false;
      }
    }
    else if (showTmVersionMenu)
    {
      if (selection.type == 'None')
      {
        copyTmVersionSelectionMni.disabled = true;
      }
      else
      {
        copyTmVersionSelectionMni.disabled = false;
      }
    }

    if (showTmMatchMenu)
    {
      tmMatchContextMenu.invalidate();
      tmMatchContextMenu.show(left, top);
    }
    else if (showTmVersionMenu)
    {
      tmVersionContextMenu.invalidate();
      tmVersionContextMenu.show(left, top);
    }
    
  }

  event.returnValue = false;
  lastKeyCode = 0
};
if(document.recalc)     
{
	document.attachEvent("oncontextmenu", showContextMenu);
	document.attachEvent("onkeyup", rememberKeyCode);
}
else
{
	document.addEventListener("contextmenu", showContextMenu,false);
	document.addEventListener("keyup", rememberKeyCode,false);
}

     
// End Context Menu
function convertMatches()
{
  try
  {
    var format = g_ptagsverbose ? "<%=EditorConstants.PTAGS_VERBOSE%>" :
      "<%=EditorConstants.PTAGS_COMPACT%>";

    for (var i = 0; i < a_tmSegments.length; i++)
    {
      var match = a_tmSegments[i];
	  var tmContentSource="";
	  if(parent.parent.GetPTagString(match.matchedSource,format))
      {   
          tmContentSource = parent.parent.GetPTagString(match.matchedSource,format);
	  }
	  else
      {
	      tmContentSource = parent.parent.GetPTagString(parent.parent.source_segment,format);
	  };
      var tmContent = parent.parent.GetPTagString(match.data, format);

	  match.ptagstring = tmContent;
	  match.ptagSourceString = tmContentSource;
    }
    for (var i = 0; i < a_AltSegments.length; i++)
    {
      var match = a_AltSegments[i];
      var tmContent = parent.parent.GetPTagString(match.data, format);
      var tmSourceContent = parent.parent.GetPTagString(match.matchedSource, format);
      match.ptagstring = tmContent;
      match.ptagstringSource = tmSourceContent;
    }
    
    for (var i = 0; i < a_stageSegments.length; i++)
    {
      var match = a_stageSegments[i];
      match.ptagstring = parent.parent.GetPTagString(match.data, format);
    }
  }
  finally
  {
    parent.parent.EndGetPTagStrings();
  }
}

function initSegmentTerm()
{
  if (a_TermSegments.length > 0)
  {
    showTermSegmentData(0);
  }
}

function showTermSegmentData(index)
{

  var o = a_TermSegments[index];
  idTermSegmentText.innerHTML  = o.text;
  
  
  if(document.recalc)
  {
  	  idTermSegmentLabel.innerText = o.label;
  }
  else
  {
  	  idTermSegmentLabel.textContent = o.label;
  }
  
  if (index == 0)
  {
    idTermPrev.style.visibility = 'hidden';
  }
  else
  {
    idTermPrev.style.visibility = 'visible';
  }
  
  if (index == a_TermSegments.length - 1)
  {
    idTermNext.style.visibility = 'hidden';
  }
  else
  {
    idTermNext.style.visibility = 'visible';
  }
}

function initSegmentMatches()
{
  if (a_tmSegments.length > 0)
  {
    showSegmentData(0);
  }
}

function initSegmentAlt()
{
  if (a_AltSegments.length > 0)
  {
    showAltSegmentData(0);
  }
}

function showAltSegmentData(index)
{
  var o = a_AltSegments[index];
  idAltSourceText.innerHTML = o.ptagstringSource? o.ptagstringSource: o.matchedSource;
  xlfAltSourceName.innerHTML = "<%=lb_SourceName%>";
  xlfAltTargetName.innerHTML = "<%=lb_TargetName%>";  
  idAltSegmentText.innerHTML  = o.ptagstring ? o.ptagstring : o.text;
  
  var sourceCell = parent.source.document.getElementById("idSourceCell");
  if(sourceCell)
  {
     markDiff(idAltSourceText.innerHTML, sourceCell.innerHTML, idAltSourceText);
     //markDiff(sourceCell.innerHTML, idAltSourceText.innerHTML, sourceCell);
  }
  
  if(document.recalc)
  {
  	  idAltSegmentLabel.innerText = o.label;
  }
  else
  {
  	  idAltSegmentLabel.textContent = o.label;
  }
  
  if (index == 0)
  {
    idAltPrev.style.visibility = 'hidden';
  }
  else
  {
    idAltPrev.style.visibility = 'visible';
  }

  if (index == a_AltSegments.length - 1)
  {
    idAltNext.style.visibility = 'hidden';
  }
  else
  {
    idAltNext.style.visibility = 'visible';
  }
}

function markDiff(s1, s2, diff) {
  var splitChar = "";
  var ori = s1;
  
  if (s1 == "") {
    diff.innerHTML = s2;
    return;
  } else if (s2 == "") {
    diff.innerHTML = s1;
    return;
  }
  s1 = s1.replace(/<[^>].*?>/g,"").replace(/\[(x|ph)\d+\]|&(gt|lt)?;|[,.=\-:%~\|<>\?()\'\"]/g, " ");
  s2 = s2.replace(/<[^>].*?>/g,"").replace(/\[(x|ph)\d+\]|&(gt|lt)?;|[,.=\-:%~\|<>\?()\'\"]/g, " ");
  
  s1 = s1.replace(/\[/g, "").replace(/\]/g, "");
  s2 = s2.replace(/\[/g, "").replace(/\]/g, "");
  
  var a1 = s1.split(" ");
  var a2 = s2.split(" ");
  var length = a1.length;
  
  var i = 0;
  /**
  if (length <= 1) {
    //Used to process string like zh-CN, which don't be splitted by space.
    for (i=0;i<s1.length;i++)
        a1[i] = s1.charAt(i);
    for (i=0;i<s2.length;i++)
        a2[i] = s2.charAt(i);
  } else {
    splitChar = " ";
  }
  length = a1.length;
  */
  var diffStr = "";
  for (var i=0;i<length;i++) {
    if (a1[i] != "" && s2.indexOf(a1[i]) == -1) {
        ori = replaceString(ori, a1[i], "<span style='background-color:yellow;'>" + a1[i] + "</span>");
    }
  }
  diff.innerHTML = ori;
}

function replaceString(s1, s2, s3) {
  if (s1.indexOf(s2) == -1)
    return s1;
  if (s2 == "" || s3 == "")
    return s1;
    
  var re = new RegExp("\\b" + s2.replace(/{/g, "\\{") + "\\b", "g");
  var arr = s1.split(re);
  var len = arr.length;
  var tmp = "";
  //There is a problem that len will be 1 if there is no matching string or 
  //the s2 includes special characters such as @, $, *, # etc.
  //So it needs to use validateContent to identify if there includes special
  //characters
  if (len <= 1) {
    if (!validateContent(s2)) {
        var fi = 0;
        var index = 0;
        while ((fi = s1.indexOf(s2, index)) != -1 && index < s1.length) {
          bs = s1.substring(index, fi);
          tmp += bs + s3;
          index = fi + s2.length;
        }
        tmp += s1.substring(index);
    } else {
        fi = s1.indexOf(s2);
        bs = s1.substring(0, fi);
        es = s1.substring(fi + s2.length);
        tmp = bs + s3 + es;
    }
  } else {
    tmp = arr[0];
    for (i=1;i<arr.length;i++) {
      tmp += s3 + arr[i];
    }
  }
  return tmp;
}

function validateContent(str) {
  var disallowChars = "!@#$&*";
  if (str == null || str.length == 0)
    return true;
  for (var i=0;i<str.length;i++) {
    if (disallowChars.indexOf(str.charAt(i)) != -1) {
        return false;
    }
  }
  return true;
}
	
function showSegmentData(index)
{
  try { parent.parent.match_details.close(); } catch (ignore) {} 
  var o = a_tmSegments[index];
  idSegmentLabel.innerHTML = o.label;
  idSegmentText.innerHTML  = o.ptagstring ? o.ptagstring : o.text;
  idTargetName.innerHTML = "<%=lb_TargetName%>";
  idSourceContent.innerHTML = o.ptagSourceString;
  idSourceName.innerHTML = "<%=lb_SourceName%>";
  
  creationDate.value=o.creationDate;
  creationUser.value=o.creationUser;
  modifyDate.value=o.modifyDate;
  modifyUser.value=o.modifyUser;
  matchedTuvJobName.value=o.matchedTuvJobName;
  tmName.value=o.tmName;
  sid.value=o.sid;
  matchType.value=o.matchType;
  
  var sourceCell = parent.source.document.getElementById("idSourceCell");
  if(sourceCell)
  {
     markDiff(idSourceContent.innerHTML, sourceCell.innerHTML, idSourceContent);
     //markDiff(sourceCell.innerHTML, idSourceContent.innerHTML, sourceCell); 
  }

  if (o.lang)
  {
    idSegmentText.lang = o.lang;
  }

  if(o.srcDir)
  {
    idSourceContent.dir = o.srcDir;
  }
  
  if (o.dir)
  {
    idSegmentText.dir = o.dir;
  }
  

  if (index == 0)
  {
    idTmMatchesPrev.style.visibility = 'hidden';
  }
  else
  {
    idTmMatchesPrev.style.visibility = 'visible';
  }

  if (index == a_tmSegments.length - 1)
  {
    idTmMatchesNext.style.visibility = 'hidden';
  }
  else
  {
    idTmMatchesNext.style.visibility = 'visible';
  }
}

function goLeftTerm()
{
  if (a_TermSegments.length > 0)
  {
    if (g_termIndex == 0)
    {
      g_termIndex = a_TermSegments.length - 1;
    }
    else
    {
      --g_termIndex;
    }
  }
  
  showTermSegmentData(g_termIndex);
}

function goRightTerm()
{
  if (a_TermSegments.length > 0)
  {
    if (g_termIndex == (a_TermSegments.length - 1))
    {
      g_termIndex = 0;
    }
    else
    {
      ++g_termIndex;
    }
  }
  
  showTermSegmentData(g_termIndex);
}

function goLeftAlt()
{
  if (a_AltSegments.length > 0)
  {
    if (g_altIndex == 0)
    {
      g_altIndex = a_AltSegments.length - 1;
    }
    else
    {
      --g_altIndex;
    }
  }
  
  showAltSegmentData(g_altIndex);
}

function goRightAlt()
{
  if (a_AltSegments.length > 0)
  {
    if (g_altIndex == (a_AltSegments.length - 1))
    {
      g_altIndex = 0;
    }
    else
    {
      ++g_altIndex;
    }
  }
  
  showAltSegmentData(g_altIndex);
}

function goLeftSegment()
{
  if (a_tmSegments.length > 0)
  {
    if (g_segmentIndex == 0)
    {
      g_segmentIndex = a_tmSegments.length - 1;
    }
    else
    {
      --g_segmentIndex;
    }
  }
  showSegmentData(g_segmentIndex);
}

function goRightSegment()
{
  if (a_tmSegments.length > 0)
  {
    if (g_segmentIndex == (a_tmSegments.length - 1))
    {
      g_segmentIndex = 0;
    }
    else
    {
      ++g_segmentIndex;
    }
  }
  showSegmentData(g_segmentIndex);
}

<%--
function doSegmentClick()
{
  parent.parent.SetSegment(a_tmSegments[g_segmentIndex].data, true);
}
--%>

function initStages()
{
  if (a_stageSegments.length > 0)
  {
    showStageData(0);
  }
}

function showStageData(index)
{
  var o = a_stageSegments[index];

  idStageLabel.innerHTML = o.label;
  idStageText.innerHTML  = o.ptagstring ? o.ptagstring : o.text;
  if (o.lang)
  {
    idStageText.lang = o.lang;
  }
  if (o.dir)
  {
    idStageText.dir = o.dir;
  }

  if (index == 0)
  {
    idStagesPrev.style.visibility = 'hidden';
  }
  else
  {
    idStagesPrev.style.visibility = 'visible';
  }

  if (index == a_stageSegments.length - 1)
  {
    idStagesNext.style.visibility = 'hidden';
  }
  else
  {
    idStagesNext.style.visibility = 'visible';
  }
}

function goLeftStage()
{
  if (a_stageSegments.length > 0)
  {
    if (g_stageIndex == 0)
    {
      g_stageIndex = a_stageSegments.length - 1;
    }
    else
    {
      --g_stageIndex;
    }
  }
  showStageData(g_stageIndex);
}

function goRightStage()
{
  if (a_stageSegments.length > 0)
  {
    if (g_stageIndex == (a_stageSegments.length - 1))
    {
      g_stageIndex = 0;
    }
    else
    {
      ++g_stageIndex;
    }
  }
  showStageData(g_stageIndex);
}

<%--
function doStageClick()
{
  parent.parent.SetSegment(a_stageSegments[g_stageIndex].data, true);
}
--%>

function showCorpus()
{
   if (a_tmSegments.length == 0) return;

   var tuvId = a_tmSegments[g_segmentIndex].tuvId;
   var srcLocaleId = a_tmSegments[g_segmentIndex].srcLocaleId;
   var url = "/globalsight/ControlServlet?activityName=viewCorpusMatches&tuvId=" +
     tuvId + "&localeDbId=" + srcLocaleId;
   w_corpus = window.open(url, "corpus",
     'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
}

var sc_customDict = null;
var sc_dict;
var sc_uiLang;

function spellCheck()
{
    if (g_SC_XDE.isLanguageSupported(parent.parent.targetlocale))
    {
        // XDE spell checking

        if (!sc_customDict)
        {
          sc_dict = g_SC_XDE.getSystemDict(parent.parent.targetlocale);
          sc_customDict = g_SC_XDE.getCustomDict(
            parent.parent.userId, parent.parent.targetlocale);
          sc_uiLang = g_SC_XDE.getUiLanguage(parent.parent.uilocale);

          frmSC.language.value = sc_dict;
        }

        //alert("XDE spell checking using dict `" + sc_dict +
        //  "', customdict `" + sc_customDict + "'");

        w_scwin = doSpell(this, 'frmSC.language', 'edit&typectrl=richedit',
          false, sc_customDict, sc_uiLang);
    }
    else
    {
        // GlobalSight spell checking (supports all languages)

        if (!sc_customDict)
        {
          sc_dict = g_SC_GSA.getSystemDict(parent.parent.targetlocale);
          sc_customDict = g_SC_GSA.getCustomDict(
            parent.parent.userId, parent.parent.targetlocale);
        }

        //alert("GlobalSight spell checking using dict `" + sc_dict +
        //  "', customdict `" + sc_customDict + "'");

        w_scwin = scSpell(this, 'edit&typectrl=richedit',
          parent.parent.targetlocale, parent.parent.uilocale,
          sc_dict, sc_customDict);
    }
}

function debug()
{
  var edit = document.all.edit;
  alert("HTML:\n\n" + edit.getHTML() +
    "\n\nText:\n\n'" + edit.getText() + "'");
  edit.focus();
}

function deactivateButtons()
{
  if (!parent.parent.HasFormattingTags())
  {
    disable(idBold);
    disable(idItalic);
    disable(idUnderline);
	if(o_textbox.enableFormattingKeys)
	{
		o_textbox.enableFormattingKeys(false);
	}
  }

  if (!parent.parent.HasPTags())
  {
    disable(idBr);
    disable(idNbsp);
    
    if (!parent.parent.openPtags())
    {
    	disable(idPtag);
    }
  }
  
  if (!parent.parent.HasOfficeTags())
  {
    disable(idSub);
    disable(idSup);
  }
}

function fixSize()
{
    o_textbox = document.all.edit;

    o_textbox.style.top = toolBar.offsetHeight + 10;
    o_textbox.style.height = idBody.clientHeight / 4;
    o_textbox.style.width = Math.max(50, document.body.offsetWidth - 20);
}

window.onresize = fixSize;

// Callback for onload() of se_main.jsp.
function PostLoad()
{
  convertMatches();
  initSegmentMatches();
  initSegmentAlt();
  initSegmentTerm();
  initStages();
}

function init()
{
  if (!b_inited)
  {
    b_inited = true;
	if(document.recalc)
	{
		o_textbox = document.all.edit;
	}
	else
	{
		o_textbox = document.getElementById("edit");
	}

    fixSize();
	if(document.recalc)
	{
		var all = document.all;
	}
	else
	{
		var all = document.getElementsByTagName("*");
	}
    
    var l = all.length;
    for (var i = 0; i < l; i++)
    {
      var o = all[i];

      if (o.tagName != "INPUT" && o.tagName != "TEXTAREA" &&
          o.id != "idSegmentText" && o.id != "idStageText")
      {
        o.unselectable = "on";
      }
    }
  }
  else
  {
      o_textbox.focus();
  }
  deactivateButtons();
}

function doOnBeforeUnload()
{
  try { w_scwin.close(); } catch (ignore) {};
  try { w_ptags.close(); } catch (ignore) {}
  try { w_corpus.close(); } catch (ignore) {}
}

var flag = false;
function showPTags()
{
  if(document.recalc)
  {
  	if (w_ptags == null || w_ptags.closed)
  	{
    	var ptags = getPtagString();
    	var args = { _opener: window, _data: ptags.split(",") };

    	w_ptags = showModelessDialog("/globalsight/envoy/edit/online/selectptag.jsp",
      	 args, "font-family:Courier; font-size:12; " +
      	 "dialogWidth:20em; dialogHeight:25em; status:no;");
  	}
  	else
  	{
    	w_ptags.focus();
  	}
  }
  else
  {
  	var ptags = getPtagString();
  	var data = ptags.split(",");
  	if (ptags.length > 0 )
  	{
    	if(!flag)
    	{
    		for (var i = 0; i < data.length; i += 2)
    		{
      			addRow(data[i], data[i+1]);
    		}
    	}
  	}
  	else
  	{
    	idTags.innerHTML = "<%=lb_no_ptags_in_segment%>";
  	}
  	flag = true;
  	var divElement = document.getElementById("selectPtag");		 
	divElement.style.visibility='visible';	
  }
}
function addRow(tag1, tag2)
{
  var table = document.getElementById('idTable');
  var row =   document.createElement("TR");
  var cell1 = document.createElement("TD");
  var cell2 = document.createElement("TD");

  cell1.value = tag1;
  cell1.innerHTML =
    "<SPAN class='link' onclick='doPtagClick(this)' style='font-family:Courier; font-size:12;'>" + tag1 + "</SPAN>";

  if (tag2)
  {
    cell2.value = tag2;
    cell2.innerHTML = 
      "<SPAN class='link' onclick='doPtagClick(this)' style='font-family:Courier; font-size:12;'>" + tag2 + "</SPAN>";
  }
  else
  {
    cell2.innerText = '\u00a0';
  }

  row.appendChild(cell1);
  row.appendChild(cell2);
  table.appendChild(row);
}

function doPtagClick(elem)
{
 InsertPTag(elem.parentNode.value);
}
  
function hideSelectPtag()
{
	var flag = false;
	var divElement = document.getElementById("selectPtag");		
	divElement.style.visibility='hidden';	
}  		
function addLre()
{
  o_textbox.addLre();
  o_textbox.frameWindow.focus();
}

function addPdf()
{
  o_textbox.addPdf();
  o_textbox.frameWindow.focus();
}

function addCr()
{
  o_textbox.addCR();
  o_textbox.frameWindow.focus();
}
function HilitePtags(bright)
{
    var css = document.styleSheets.item('cssPtag');
    var rule = (css.rules)?css.rules[0]:css.cssRules[0];
    rule.style.color = bright ? '#3366FF' : '#808080';
    if(o_textbox)
	{
		o_textbox.setPtagColor(bright ? '#3366FF' : '#808080');
	} 
}

function InsertPTag(tag)
{
  o_textbox.insertPTag(tag);
}

function SetVerbosePTags(flag)
{
  if(o_textbox)
  {
	  o_textbox.setVerbosePTags(flag);
  }
  if (g_ptagsverbose != flag)
  {
    g_ptagsverbose = flag;

    convertMatches();
    if (a_tmSegments.length > 0)
    {
      showSegmentData(g_segmentIndex);
    }
    if (a_stageSegments.length > 0)
    {
      showStageData(g_stageIndex);
    }
  }
}

function SetSegment(s, changed, preserveWS)
{
  if(!document.recalu)
  {
  	initRichEdit(o_textbox);
  }
  
  if (preserveWS)
  {
	  s = "<pre>" + s + "</pre>";
  }
  else
  {
	  s = FixLeftSpace(s, "<%=dataFormat %>");
  }
  if(o_textbox)
  {
	  if(isRtl == "true" && changed)
	  {
		  s = s.replaceAll("DIR=ltr","DIR=rtl");
	  }
	  o_textbox.setHTML(s);
      o_textbox.focus();
  }
  if (!changed)
  {
      idSourceSegment.innerHTML = s;
  }
}

String.prototype.replaceAll = function(reallyDo, replaceWith, ignoreCase) {  
    if (!RegExp.prototype.isPrototypeOf(reallyDo)) {  
        return this.replace(new RegExp(reallyDo, (ignoreCase ? "gi": "g")), replaceWith);  
    } else {  
        return this.replace(reallyDo, replaceWith);  
    }  
}  

function FixLeftSpace(p_s, p_format)
{
	if ("xml" == p_format)
	{
		var sbb = new StringBuffer(p_s);
		var ltrimed = sbb.ltrim();
		if (p_s != ltrimed)
		{
			return "&nbsp;" + ltrimed;
		}
	}

	return p_s;
}

function GetSegment()
{
  var res = o_textbox.getText();
  return res;
}

function SetFocus()
{
  o_textbox.focus();
}

function IsChanged()
{
  var s_oldSegment = "";
  if (isFirefox)
  {
	  s_oldSegment = idSourceSegment.textContent; 
  }
  else
  {
	  s_oldSegment = idSourceSegment.innerText;; 
  }
  var s_newSegment = GetSegment();
  if (s_oldSegment != s_newSegment)
  {
    // alert("Segments differ:\n\n" + "'" + s_oldSegment +
    //   "'\n\n'" + s_newSegment + "'");
    return true;
  }

  return false;
}

function CanClose()
{
  return !IsChanged();
}

function InsertTerm(term)
{
  if(isIE)
  {
	if (!g_selectedTerm) return;
  	o_textbox.focus();
  	o_textbox.insertText(g_selectedTerm.innerText);
  }
  else
  {
	o_textbox = document.getElementById("edit");
	o_textbox.focus();
	var text = a_TermSegments[g_termIndex].data;    
	var original = o_textbox.contentDocument.getElementById("idBody");
	original.textContent = text + original.textContent;
  }
}

function showTermbase()
{
  if (g_haveTermbase)
  {
    ShowTermbase(g_defaultTermbaseId);
  }
}

function browseTerm(event)
{
  if (!g_selectedTerm) return;
  var conceptid = g_selectedTerm.getAttribute("cid");
  var termid = g_selectedTerm.getAttribute("tid");

  ShowTermbaseConceptTerm(g_defaultTermbaseId, conceptid, termid,event);
}

function copyMatchToEditor()
{
  parent.parent.SetSegment(a_tmSegments[g_segmentIndex].data, true);
}

function copyAltToEditor()
{
  parent.parent.SetSegment(a_AltSegments[g_altIndex].data, true);
}

function copyTermToEditor()
{
  parent.parent.SetSegment(a_TermSegments[g_termIndex].data, true);
}

function copyVersionToEditor()
{
  parent.parent.SetSegment(a_stageSegments[g_stageIndex].data, true);
}

function copySelectionToEditor()
{
  var range = document.selection.createRange();
  var text = range.text;

  o_textbox.focus();
  o_textbox.insertText(text);
}

function doClick()
{
	var dd = document.getElementById('<%=MTHelper2.MT_TRANSLATION_DIV%>').innerHTML;
	copyToTarget(dd);
}

function copyToTarget(value)
{
    parent.parent.SetSegment(value);
}

function openParaEditor(url, e)
{
    if (!canClose())
    {
        cancelEvent(e);
        raiseSegmentEditor();
    }
    else
    {
      // For issue "(AMB-115) Toolbar behaviour for the Inline editor" to 
      // resolve the problem which the scroll jump up fist (no good UE) after closed inline editor
      var inlineUrl =  "/globalsight/ControlServlet?linkName=editorSameWindow&pageName=TK2" + url;
      window.open(inlineUrl, 'topFrame',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

function openListEditor(url, e)
{
    if (!canClose())
    {
        cancelEvent(e);
        raiseSegmentEditor();
    }
    else
    {
        hideContextMenu();

        if (b_isReviewActivity)
        {
          url = "/globalsight/ControlServlet?linkName=editor&pageName=TK2&reviewMode=true" + url;
        }
        else
        {
           url = "/globalsight/ControlServlet?linkName=editor&pageName=TK2" + url;
        }

        w_editor = window.open(url, 'MainEditor',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

//Add menu function for Firefox
function contextForTBmatches(url, e)
{
    var b_canEditInSameWindow = true;
	
    if(isIE) 
	  {
		    return;
	  }
	  else {
	      e.preventDefault();
        e.stopPropagation();
        
        var el = e.target;

        while (el != null && el.tagName != "DIV") { el = el.parentNode; };
        var showTbMenu = (el != null && el.className == 'targetTerm');

        if (showTbMenu)
        {
            g_selectedTerm = el;
        }
        else {
            return;
        }

    }
    
    var popupoptions;    
    if (b_canEditInSameWindow)
    {    	    	
		popupoptions = [
			new ContextItem("<%=bundle.getString("lb_insert_in_editor") %>",
							function(){ InsertTerm();}),
			new ContextItem("<%=bundle.getString("lb_browse_term") %>",
							function(){ browseTerm(e);})
    	];    	
    }
    
    ContextMenu.display(popupoptions, e); 
}

function contextForTMMatches(url, e)
{
	var b_canEditInSameWindow = true;
	var fontGray1 = "<font color='gray'>";
	var fontGray2 = "</font>";
	
	if(isIE) 
	{
		return;
	}
	else
    {
    	e.preventDefault();
    	e.stopPropagation();
    }
    
    var popupoptions;    
    if (b_canEditInSameWindow)
    {
    	lb_copy_to_editor 				= "<%=bundle.getString("lb_copy_to_editor")%>";
    	lb_insert_selection_in_editor 	= "<%=bundle.getString("lb_insert_selection_in_editor")%>";
    	lb_corpus_context 				= "<%=bundle.getString("lb_corpus_context")%>";

    	lb_insert_selection_in_editor = 
        	fontGray1+lb_insert_selection_in_editor+fontGray2;
    	
    	<% if (b_corpus) { %>
    		popupoptions = [
				new ContextItem(lb_copy_to_editor,
							function(){ copyMatchToEditor();}),
    	        new ContextItem(lb_insert_selection_in_editor,
    	                  	function(){ copySelectionToEditor();}),
    	        new ContextItem(lb_corpus_context,
    	                    function(){ showCorpus();})
    	    ];
    	<% }else{ %>
    		popupoptions = [
    			new ContextItem(lb_copy_to_editor,
    								function(){ copyMatchToEditor();}),
    	    	new ContextItem(lb_insert_selection_in_editor,
    	    	                  	function(){ copySelectionToEditor();})
    	    ];
        <% } %>
    }
    
    ContextMenu.display(popupoptions, e); 
}

function contextForStages(url, e)
{
	if(isIE) 
	{
		return;
	}
	else
    {
    	e.preventDefault();
    	e.stopPropagation();
    }
    
    var popupoptions;    
    if (b_canEditInSameWindow)
    {
    	lb_copy_to_editor 			  = "<%=bundle.getString("lb_copy_to_editor")%>";
        lb_insert_selection_in_editor = "<%=bundle.getString("lb_insert_selection_in_editor")%>";
    	lb_insert_selection_in_editor = 
        	fontGray1+lb_insert_selection_in_editor+fontGray2;
    	
    	popupoptions = [
    			new ContextItem(lb_copy_to_editor,
    								function(){ copyVersionToEditor();}),
    	    	new ContextItem(lb_insert_selection_in_editor,
    	    	                  	function(){ copySelectionToEditor();})
    	];
    }
    
    ContextMenu.display(popupoptions, e); 
}

function contextForXliffTrans(url, e)
{
	if(isIE) 
	{
		return;
	}
	else
    {
    	e.preventDefault();
    	e.stopPropagation();
    }
    
    var popupoptions;    
    if (b_canEditInSameWindow)
    {
    	lb_copy_to_editor = "<%=bundle.getString("lb_copy_to_editor")%>";
    	
    	popupoptions = [
    			new ContextItem(lb_copy_to_editor,
    								function(){ copyAltToEditor();})
    			,new ContextItem("", null)
    	];
    }
    
    ContextMenu.display(popupoptions, e); 
}

function doLoad()
{
	ContextMenu.intializeContextMenu();

	// Try to get MT translation after page is loaded.
    if (<%=show_MT%>)
    {
    	getMtTranslationMessage();
    }
}

function getMtTranslationMessage()
{
    dojo.xhrPost(
    {
       url:"<%=mtTranslationMessageURL%>",
       handleAs: "text",
       load:function(data)
       {
           var returnData = eval(data);
           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	   //for "'" in IE, should be "&#39" instead of "&apos"
        	   var rData = returnData.mtMatch.replace(/&apos;/g,"&#39;");
        	   document.getElementById("mtTranslation").innerHTML = rData;
           }
       },
       error:function(error)
       {
           // do not display XHR error
           //alert(error.message);
       }
   });
}

function mtTranslation()
{
	dojo.xhrPost(
    {
       url:"<%=mtTranslationURL%>",
       handleAs: "text",
       load:function(data)
       {
           var returnData = eval(data);
           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	   var tranReplaced = returnData.translatedString_replaced;
        	   var tranContents = returnData.translatedString;
        	   document.getElementById('blankLine').style.display = "none";
        	   document.getElementById('<%=MTHelper2.MT_TRANSLATION_DIV%>').innerHTML=tranReplaced;
        	   document.getElementById('idMtContents').innerHTML = tranContents;
        	   
           }
       },
       error:function(error)
       {
       }
   });
}

//for terminology image show or hide.
function termImgShow(divName) 
{
    window.open ('terminologyImg/'+ divName,'newwindow','top=0,left=0,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;
}

function showMatchdetailInfo()
{
    parent.parent.match_details = window.open("envoy/edit/online/se_match_details.jsp","MatchDetail","resizable,scrollbars=yes,width=400,height=400");
}

</SCRIPT>
</HEAD>
<BODY id="idBody" onbeforeunload="doOnBeforeUnload()" onLoad="doLoad();">
<div id="idSourceSegment" style="display:none"></div>
<HR style="position: relative; top: 0; left: 0;" COLOR="#0C1476" WIDTH="95%">
<!-- Target -->
<div id="idEditor" STYLE="position: relative; height: expression(idBody.clientHeight * 0.25);">
<table cellspacing="0" id="toolBar" width="98%">
  <tr>
    <td align="left">
      &nbsp;<SPAN CLASS="standardTextBold"><%=lb_target%></SPAN>
    </td>
    <td width="100%">&nbsp;</td>
    <td align="right">
      <table cellspacing="0">
    <tr>
      <td class="coolButton" id="idSpellCheck" onclick="spellCheck()">
        <img src="/globalsight/envoy/edit/online2/Spellcheck2.gif">
      </td>
      <td class="coolButton" id="idSub"
	    onclick="o_textbox.makeSub(); o_textbox.frameWindow.focus();">
	    <img src="/globalsight/envoy/edit/online2/subscript.gif">
	  </td>
      <td class="coolButton" id="idSup"
	    onclick="o_textbox.makeSup(); o_textbox.frameWindow.focus();">
	    <img src="/globalsight/envoy/edit/online2/superscript.gif">
	  </td>
      <td class="coolButton" id="idBold"
        onclick="o_textbox.makeBold(); o_textbox.frameWindow.focus();">
        &nbsp;<B>[bold]</B>&nbsp;
      </td>
      <td class="coolButton" id="idItalic"
        onclick="o_textbox.makeItalic(); o_textbox.frameWindow.focus();">
        &nbsp;<i>[italic]</i>&nbsp;
      </td>
      <td class="coolButton" id="idUnderline"
        onclick="o_textbox.makeUnderline(); o_textbox.frameWindow.focus();">
        &nbsp;<u>[underline]</u>&nbsp;
      </td>
      <td class="coolButton" id="idBr"
        onclick="o_textbox.addBr(); o_textbox.frameWindow.focus();">
        &nbsp;[br]&nbsp;
      </td>
      <td class="coolButton" id="idNbsp"
        onclick="o_textbox.addNbsp(); o_textbox.frameWindow.focus();">
        &nbsp;[nbsp]&nbsp;
      </td>
      <td class="coolButton" id="idPtag" onclick="showPTags()">
        &nbsp;[ptag...]&nbsp;
      </td>
      <td class="coolButton" id="idBrackets"
        onclick="o_textbox.addBrackets(); o_textbox.frameWindow.focus();">
        &nbsp;[[]]&nbsp;
      </td>
      <td class="coolButton" id="idButCr" onclick="addCr()">
        &nbsp;[cr]&nbsp;
      </td>
      <td class="coolButton" id="idButLtr" onclick="addLre()">
        &nbsp;[LRE]&nbsp;
      </td>
      <td class="coolButton" id="idButRtl" onclick="addPdf()">
        &nbsp;[PDF]&nbsp;
      </td>
    </tr>
      </table>
    </td>
  </tr>
</table>
<iframe frameborder="0" id="edit" src="about:blank" class="richEdit" align="center"
  usebr="true" <%=str_langAttr%> onblur="return false"  onload="init()" ></iframe>
</div>

<!-- TB Matches -->
<HR style="position: relative; top: 0; left: 0;" COLOR="#0C1476" WIDTH="95%">
<% if (show_in_editor && hasXliff) { %>
<DIV id="idTbMatches" STYLE="position: relative; margin-left: 5px;height:auto!important;height:85px; min-height:85px;">
<% } else if ( (show_in_editor && !hasXliff) || (!show_in_editor && hasXliff) ) { %>
<DIV id="idTbMatches" STYLE="position: relative; margin-left: 5px;height:auto!important;height:85px; min-height:85px;">
<% } else {%>
<DIV id="idTbMatches" STYLE="position: relative; margin-left: 5px;height:auto!important;height:85px; min-height:85px;">
<% }%>
  <NOBR>
  <SPAN CLASS="standardTextBold">
    <% if (b_haveTermbase)
    {    
    if(canAccessTB)
    {%>
    <%= lb_matches_from_tb %>
      <SPAN ID="idTermbase" CLASS="link" TITLE="<%=lb_clickToOpenTb%>"
      onclick="showTermbase()" oncontextmenu="showTermbase()">
      <%=str_defaultTermbaseName%></SPAN>
    <%}
    else
    {%>
    <%= lb_matches_from_tb %>
      <SPAN ID="idTermbase" TITLE="<%=lb_clickToOpenTb%>">
      <%=str_defaultTermbaseName%></SPAN>
    <%}
    }
    else
    {
    %><%=lb_tbMatchResults%><%
    }
    %>
  </SPAN>
  <SPAN CLASS="standardTextItalic"><%=bundle.getString("lb_right_click_for_actions") %></SPAN>
  </NOBR>
  
  <DIV STYLE="position: relative; margin-left: 10px;
    height: expression(idTbMatches.style.posHeight * 0.8);">
  <%
  if (tbMatches == null || tbMatches.size() == 0)
  {
  %>
  <span class="standardTextItalic"><%=lb_noTerms%></span>
  <%
  }
  else
  {
  %>
    <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" ALIGN="LEFT">
    <TR>
       <TD VALIGN="TOP" >
          <IMG SRC="/globalsight/images/previousMatchArrow.gif" height=12 width=6
            id="idTermPrev"
            class="clickable" onclick="goLeftTerm(); return false;">
      </TD>
      <TD VALIGN="TOP" >
         <SPAN id="idTermSegmentLabel" class="standardTextBold"></SPAN>
      </TD>
      <TD VALIGN="TOP" id="idTermSegmentText" class="editorStandardText" oncontextmenu="contextForTBmatches(null,event);"></TD>
      <TD VALIGN="TOP" >
        <IMG SRC="/globalsight/images/nextMatchArrow.gif" height=12 width=6
          id="idTermNext"
          class="clickable" onclick="goRightTerm(); return false;"> 
      </TD>
    </TR>
  </TABLE>
  <% } %>
  </DIV>
</DIV>

<!-- Xliff alt-trans -->
<%
  if (xliffAltSet != null && xliffAltSet.size() > 0)
  {
%>
<HR style="position: relative; top: 0; left: 0;" COLOR="#0C1476" WIDTH="95%">
<% if (show_in_editor) { %>
<DIV id="idXliffTrans" STYLE="position: relative; margin-left: 5px;overflow: hidden;">
<% } else {%>
<DIV id="idXliffTrans" STYLE="position: relative; margin-left: 5px;overflow: hidden;">
<% }%>
<nobr>
  <SPAN CLASS="standardTextBold">Xliff Alt</SPAN>
  <SPAN CLASS="standardTextItalic">right-click for actions</SPAN>
</nobr>

<DIV STYLE="position: relative; *height:100%; margin-left: 10px;">
    <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" ALIGN="LEFT">
    <TR>
       <TD VALIGN="TOP" WIDTH="8px">
          <IMG SRC="/globalsight/images/previousMatchArrow.gif" height=12 width=6
            id="idAltPrev"
            class="clickable" onclick="goLeftAlt(); return false;">
      </TD>
      <TD VALIGN="TOP" WIDTH="1%">
         <SPAN id="idAltSegmentLabel" class="standardTextBold"></SPAN>
      </TD>
      <TD VALIGN="TOP" WIDTH="8px">
        <IMG SRC="/globalsight/images/nextMatchArrow.gif" height=12 width=6
          id="idAltNext"
          class="clickable" onclick="goRightAlt(); return false;">
      </TD>
      <TD></TD>
    </TR>
    <TR>
        <TD></TD>
        <TD id="xlfAltSourceName" colspan="2" valign="TOP" class="editorStandardText"></TD>
        <TD id="idAltSourceText" class="editorStandardText"></TD>
        <TD></TD>
    </TR>
    <TR>
        <TD></TD>
        <TD id="xlfAltTargetName" colspan = "2" valign="TOP" class="editorStandardText"></TD>
        <TD id="idAltSegmentText" class="editorStandardText" oncontextmenu="contextForXliffTrans(null,event);"></TD>
    </TR>
  </TABLE>
</DIV>
</DIV>
<%
   }
%>

<!-- TM Matches -->
<HR style="position: relative; top: 0; left: 0;" COLOR="#0C1476" WIDTH="95%">
<% if (show_in_editor && hasXliff) { %>
<DIV id="idTmMatches" STYLE="position: relative; margin-left: 5px; height:auto!important;height:85px; min-height:85px;">
<% } else if ( (show_in_editor && !hasXliff) || (!show_in_editor && hasXliff) ) { %>
<DIV id="idTmMatches" STYLE="position: relative; margin-left: 5px; height:auto!important;height:85px; min-height:85px;">
<% } else { %>
<DIV id="idTmMatches" STYLE="position: relative; margin-left: 5px; height:auto!important;height:85px; min-height:85px;">
<% } %>

  <nobr>
  <SPAN CLASS="standardTextBold"><%=lb_tmMatchResults%></SPAN>
  <SPAN CLASS="standardTextItalic"><%=bundle.getString("lb_right_click_for_actions") %></SPAN>
  </nobr>
  
<DIV STYLE="position: relative; *height:100%; overflow: hidden; margin-left: 10px;">
<%
  if (tmMatches == null || tmMatches.size() == 0)
  {
%>
  <SPAN class="standardTextItalic"><%=lb_noSegments%></SPAN>
<%
  }
  else
  {
%>
   <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" ALIGN="LEFT" WIDTH="100%">
    <TR VALIGN=TOP>
      <TD WIDTH="8px">
    	<IMG SRC="/globalsight/images/previousMatchArrow.gif" id="idTmMatchesPrev" 
		               class="clickable" onclick="goLeftSegment(); return false;">
      </TD>
	  <TD VALIGN="TOP" nowrap style="width:1%;">
		<SPAN id="idSegmentLabel" class="standardTextBold"></SPAN></TD>
	  <TD VALIGN="TOP" align="right" WIDTH="10px">
      <IMG SRC="/globalsight/images/nextMatchArrow.gif" height=12 width=6
					id="idTmMatchesNext" class="clickable"
					onclick="goRightSegment(); return false;"></TD>
	  <TD VALIGN="TOP" align="right" class="standardTextBold">
			<!--
			<SPAN class="standardTextBold"><%=bundle.getString("lb_sid")%>: </SPAN> 
			<SPAN id="idSID" class="standardText"></SPAN>
			 -->
			<SPAN class="link" TITLE="Click to see match detail info" 
			 onclick="showMatchdetailInfo()" oncontextmenu="showMatchdetailInfo()">
			 <font style="text-decoration:underline;"><%=lb_details%></font>
			 </SPAN>
	  </TD>
	</TR>
	<TR>
	  <TD></TD>
	  <TD COLSPAN=3>
	  <TABLE width="100%">
          <TR>
		  <TD id="idSourceName" valign="TOP" class="editorStandardText" 
	  		style="width: 50px;">
          </TD>
	      <TD id="idSourceContent" class="editorStandardText">
          </TD>
	      </TR>
	      
		  <TR>
	      <TD id="idTargetName" valign="TOP" class="editorStandardText">
          </TD>
	      <TD VALIGN="TOP" id="idSegmentText" class="editorStandardText" 
      		oncontextmenu="contextForTMMatches(null,event);">
          </TD>
          </TR>
	  </TABLE>
	  </TD>
  </TABLE>
<%
  }
%>
</DIV>
</DIV>

<!-- Used for save match details info -->
<input type= 'hidden' id='creationDate'/> 
<input type= 'hidden' id='creationUser'/> 
<input type= 'hidden' id='modifyDate'/> 
<input type= 'hidden' id='modifyUser'/> 
<input type= 'hidden' id='matchedTuvJobName'/> 
<input type= 'hidden' id='tmName'/> 
<input type= 'hidden' id='sid'/> 
<input type= 'hidden' id='matchType'/> 

<!-- MT Results -->
<div id="mtTranslation" style="display:block;"></div>

<!-- Stages -->
<HR style="position: relative; top: 0; left: 0;" COLOR="#0C1476" WIDTH="95%">
<% if (show_in_editor && hasXliff) { %>
<DIV id="idStages" STYLE="position: relative; margin-left: 5px;height:auto!important;height:85px; min-height:85px;">
<% } else if ( (show_in_editor && !hasXliff) || (!show_in_editor && hasXliff) ) { %>
<DIV id="idStages" STYLE="position: relative; margin-left: 5px;height:auto!important;height:85px; min-height:85px;">
<% } else {%>
<DIV id="idStages" STYLE="position: relative; margin-left: 5px;height:auto!important;height:85px; min-height:85px;">
<% } %>

  <nobr>
  <SPAN CLASS="standardTextBold"><%=lb_segmentVersions%></SPAN>
  <SPAN CLASS="standardTextItalic"><%=bundle.getString("lb_right_click_for_actions") %></SPAN>
  </nobr>

<DIV STYLE="position: relative; overflow: hidden; margin-left: 10px;">
<%
if (stages == null || stages.size() == 0)
{
%>
  <SPAN class="standardTextItalic"><%=lb_noVersions%></SPAN>
<%
  }
  else
  {
%>
  <TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0 width="100%">
    <TR>
      <TD VALIGN="TOP" WIDTH="8px" align="left" >
    	<IMG SRC="/globalsight/images/previousMatchArrow.gif" height=12 width=6px
    		 id="idStagesPrev" class="clickable" 
    		 onclick="goLeftStage(); return false;">
      </TD>
      <TD VALIGN="TOP" WIDTH="85%">
    	<SPAN id="idStageLabel" class="standardTextBold"></SPAN>
      </TD>
      <TD VALIGN="TOP" >
    	<IMG SRC="/globalsight/images/nextMatchArrow.gif" height=12 width=6px
    		 id="idStagesNext" class="clickable" 
    		 onclick="goRightStage(); return false;">
      </TD>
      </TR>
    <TR>  
        <TD WIDTH>&nbsp;</TD> 
      <TD VALIGN="TOP" id="idStageText" class="editorStandardText"
      	  oncontextmenu="contextForStages(null,event);">
      </TD>
      <TD WIDTH>&nbsp;</TD> 
    </TR>
  </TABLE>
<%
  }
%>
</DIV>
</DIV>
 <div id = "selectPtag" style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;left:100px;width:300px;height:260px;position:absolute;top:100px;display:block;z-index:1000; visibility:hidden;'>
    	<div id='selectPtagDialog' onmousedown="Drag.init('selectPtag', '1000')" onmouseup ="Drag.release()" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:hand;cursor:pointer;'>
           <label class='whiteBold'>
                               <%=lb_select_ptag1%>
           </label>
        </div>
		<DIV id="idTags">
			<TABLE ALIGN="center" id="idTable" CELLSPACING="10"></TABLE>
		</DIV>
		<P ALIGN="center" style = "margin-top:30px;">
				<BUTTON ONCLICK="hideSelectPtag();"><%=lb_close%></BUTTON>
		</P>
 	</div>
<form name="frmSC"><input type="hidden" name="language" value=""></form>

</BODY>

<script>
parent.parent.finishFrame();
</script>

</HTML>
