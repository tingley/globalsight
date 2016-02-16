<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.edit.online.UIConstants,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.edit.online.PaginateInfo,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="changeSplit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="viewSource" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="viewTarget" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="pageInfo" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="resources" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="termbases" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="autoPropagate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <%@ include file="/envoy/common/installedModules.jspIncl" %>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
EditorState.Layout layout = state.getLayout();

String url_refresh     = refresh.getPageURL();
String url_viewSource  = viewSource.getPageURL();
String url_viewTarget  = viewTarget.getPageURL();
String url_changeSplit = changeSplit.getPageURL();
String url_pageInfo    = pageInfo.getPageURL();
String url_resources   = resources.getPageURL();
String url_termbases   = termbases.getPageURL();
String url_options     = options.getPageURL();
String url_search     = search.getPageURL();
String url_autoPropagate = autoPropagate.getPageURL() 
	+ "&action=default" + "&targetPageId=" + state.getTargetPageId();

String lb_close = bundle.getString("lb_close");
String lb_editLocaleContent = bundle.getString("lb_edit_locale_content");
String lb_help = bundle.getString("lb_help");
String lb_horizontalSplit = "/globalsight/images/editorHorzSplit.gif";
String lb_layoutModes = bundle.getString("lb_layoutModes");
String lb_noSplit = "/globalsight/images/editorNoSplit.gif";
String lb_options = bundle.getString("lb_options");
String lb_pageInfo = bundle.getString("lb_pageInfo");
String lb_fileNavigation = bundle.getString("lb_fileNavigation");
String lb_pageNavigation = bundle.getString("lb_pageNavigation");
String lb_showSupportFiles = bundle.getString("lb_showSupportFiles");
String lb_showTermbases = bundle.getString("lb_showTermbases");
String lb_snippetEditor = bundle.getString("lb_snippet_editor");
String lb_source = bundle.getString("lb_source");
String lb_supportFiles = bundle.getString("lb_supportFiles");
String lb_target = bundle.getString("lb_target");
String lb_termbases = bundle.getString("lb_termbases");
String lb_unlock = bundle.getString("lb_unlock");
String lb_lock = bundle.getString("lb_lock");
String lb_verticalSplit = "/globalsight/images/editorVertSplit.gif";
String lable = "";
String action = "";
String	unmarklable = bundle.getString("lb_unmark_pTag_segments");
String marklable = bundle.getString("lb_find_pTag_segments");
int targetViewMode = layout.getTargetViewMode();
int sourceViewMode = layout.getSourceViewMode();

String unReplable = bundle.getString("lb_unmark_repeated_segments");
String Replable = bundle.getString("lb_find_repeated_segments");
//file navigation (default unavailable)
String lb_prevFile = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextFile = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

//page navigation (default unavailable)
String lb_prevPage = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextPage = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

PaginateInfo pi = state.getPaginateInfo();

// Determine if the unlock button is VISIBLE or not.
boolean b_showUnlockButton = true;
if ((state.isReadOnly() && !state.canEditAll()) ||
    (layout.isSinglePage() && layout.singlePageIsSource()))
{
    b_showUnlockButton = false;
}

// Determine if show "Show Repeated" link
boolean b_showRepeatedLink = true;
if (state.isReadOnly()
        || (layout.isSinglePage() && layout.singlePageIsSource()) )
{
    b_showRepeatedLink = false;
}
// Determine if show "Auto-Propagate" link
boolean b_showAutoPropagateLink = true;
if (state.isReadOnly() || state.getIsReviewActivity()
        || (layout.isSinglePage() && layout.singlePageIsSource() ) )
{
    b_showAutoPropagateLink = false;
}

long jobId = Long.valueOf(sessionMgr.getAttribute(WebAppConstants.JOB_ID).toString());
String tgtIDS = sessionMgr.getAttribute(ReportConstants.TARGETLOCALE_LIST).toString();
%>
<HTML>
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT>
var b_singlePage = false;
var b_singlePageIsSource = false;

var b_canEditAll = eval("<%=state.canEditAll()%>");
var b_isReviewMode = eval("<%=state.isReviewMode()%>");
var b_isReviewActivity = eval("<%=state.getIsReviewActivity()%>");

var w_options = null;
var w_pageinfo = null;
var w_resources = null;
var w_termbases = null;
var w_progress = null;
var w_search = null;
var w_autoPropagate = null;

var helpFile = "<%=bundle.getString("help_main_editor")%>";

function helpSwitch()
{
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function checkShutdown(interval)
{
  document.ShutdownForm.submit();
  setTimeout("checkShutdown(" + interval + ")", interval);
}

function exit()
{
    try { w_options.close();   } catch (ignore) {}
    try { w_pageinfo.close();  } catch (ignore) {}
    try { w_resources.close(); } catch (ignore) {}
    try { w_termbases.close(); } catch (ignore) {}
    try { w_progress.close();    } catch (ignore) {}
    try { w_search.close();    } catch (ignore) {}
    try { w_autoPropagate.close();    } catch (ignore) {}
}

function showRadioButtons()
{
    radiobuttons.style.visibility = "visible";
}

function hideRadioButtons()
{
    radiobuttons.style.visibility = "hidden";
}

function hideUnlockButton()
{
    var o = document.getElementById("unlock");
    if (o)
    {
        o.style.visibility = "hidden";
    }
}

function showUnlockButton()
{
    var o = document.getElementById("unlock");
    if (o)
    {
        o.style.visibility = "visible";
    }
}

function horizontalSplit()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
        return false;
    }
    else
    {
        var str_url;

        str_url  = "<%=url_changeSplit%>";
        str_url += "&singlePage=<%=EditorConstants.DUAL_PAGE%>";
        str_url += "&horizontal=<%=EditorConstants.SPLIT_HORIZONTALLY%>";

        hideRadioButtons();
        showUnlockButton();
        b_singlePage = false;

        parent.RefreshContentPane(str_url);

        CloseProgressWindow();
        CloseSearchWindow();
    }
}

function verticalSplit()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
        return false;
    }
    else
    {
        var str_url;

        str_url  = "<%=url_changeSplit%>";
        str_url += "&singlePage=<%=EditorConstants.DUAL_PAGE%>";
        str_url += "&horizontal=<%=EditorConstants.SPLIT_VERTICALLY%>";

        hideRadioButtons();
        showUnlockButton();
        b_singlePage = false;

        parent.RefreshContentPane(str_url);

        CloseProgressWindow();
        CloseSearchWindow();
    }
}

function noSplit()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
        return false;
    }
    else
    {
        var str_url;

        if (b_singlePageIsSource)
        {
            str_url  = "<%=url_viewSource%>";
            str_url += "&singlePage=<%=EditorConstants.SINGLE_PAGE%>";

            hideUnlockButton();
        }
        else
        {
            str_url  = "<%=url_viewTarget%>";
            str_url += "&singlePage=<%=EditorConstants.SINGLE_PAGE%>";

            showUnlockButton();
        }

        showRadioButtons()

        b_singlePage = true;

        parent.RefreshContentPane(str_url);

        CloseProgressWindow();
        CloseSearchWindow();
    }
}

function showSourcePage()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
        selectTargetRadio();
        return false;
    }
    else
    {
        b_singlePageIsSource = true;

        var str_url;

        str_url  = "<%=url_viewSource%>";
        str_url += "&singlePage=<%=EditorConstants.SINGLE_PAGE%>";
        str_url += "&singlePageSource=<%=EditorConstants.SINGLE_PAGE_IS_SOURCE%>";

        hideUnlockButton();

        parent.RefreshContentPane(str_url);

        CloseProgressWindow();
        CloseSearchWindow();

        return true;
    }
}

function showTargetPage()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
        selectSourceRadio();
        return false;
    }
    else
    {
        b_singlePageIsSource = false;

        var str_url;

        str_url  = "<%=url_viewTarget%>";
        str_url += "&singlePage=<%=EditorConstants.SINGLE_PAGE%>";
        str_url += "&singlePageSource=<%=EditorConstants.SINGLE_PAGE_IS_TARGET%>";

        showUnlockButton();

        parent.RefreshContentPane(str_url);

        CloseProgressWindow();
        CloseSearchWindow();

        return true;
    }
}

function selectSourceRadio()
{
    bob.display[0].checked = true;
}

function selectTargetRadio()
{
    bob.display[1].checked = true;
}

function showHourglass()
{
    showHourglassInDocument(document);
    showHourglassInFrame(findSourceMenuFrame());
    showHourglassInFrame(findTargetMenuFrame());

    // Changing all elements in target frame takes too long.
    // Could change the style for class "editorSegment"...
    var f = FindTargetFrame();
    if (f)
    {
      try
      {
        f.document.body.style.cursor = 'wait';
      }catch(e)
      {
      }
    }
    f = findSourceFrame();
    if (f)
    {
      try
      {
         f.document.body.style.cursor = 'wait';
      }catch(e){}
    }
}

function showHourglassInFrames(frames)
{
    for (var i = 0; i < frames.length; i++)
    {
        var doc = frames(i).self.document;
        if (doc.frames != null && doc.frames.length > 0)
        {
            showHourglassInFrames(doc.frames);
        }
        showHourglassInDocument(doc);
    }
}

function showHourglassInFrame(frame)
{
    if (!frame) return;

    showHourglassInDocument(frame.document);
}

function showHourglassInDocument(doc)
{
    if (!doc) return;

    var elements = doc.all;
    if(!elements) elements = doc.getElementsByTagName("*");
    for (var j = 0; j < elements.length; j++)
    {
      if(elements[j].style) elements[j].style.cursor = 'wait';
    }
}

function refresh(direction)
{
    var str_url;

    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
    }
    else
    {
        str_url  = "<%=url_refresh%>";
        str_url += "&refresh=" + direction;
        showHourglass();
        parent.Refresh(str_url);
    }
}

var comments=["<%=bundle.getString("lb_editor_hide_comments") %>","<%=bundle.getString("lb_editor_show_comments") %>"]

function reviewMode()
{
    var str_url;
    var isChrome = window.navigator.userAgent.indexOf("Chrome")>0;
    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
    } else{
    	 var lable=$("#reviewMode").text();
    	 lable=$.trim(lable);
    	 // isChrome 20.	Segment editor, color is incorrect if there are matches in termbase
    	 if(true){
    	    var action="true";
    	    if(comments[0]==lable){
    	    	action="false";
	    	}
    	    str_url  = "<%=url_refresh%>";
            str_url += "&<%=WebAppConstants.REVIEW_MODE%>=" + action;
            showHourglass();
            parent.Refresh(str_url);
    	 }else{
	        str_url  = "<%=url_refresh%>" +"&dataFormat=json";
	        str_url += "&<%=WebAppConstants.REVIEW_MODE%>=" + lable;
	          $.getJSON(str_url, 
	  				function(data) {
	          		 if(comments[0]==lable){
	        	    		$("#reviewMode").text(comments[1]);
	        	    	}else{
	        	    		$("#reviewMode").text(comments[0]);
	        	    	}
	          			comment=$("#reviewMode").text();
	        	    	
	        	    	 parent.toggleComments(comment);
	        	    	 try {parent.content.target.content.reviewMode(comment);   } catch (ignore) {parent.content.content.reviewMode(comment);}
	  		 	
	   		 });
    	 }
       
    }
}

var lockArray=['<%=lb_lock%>','<%=lb_unlock%>'];

function editAll()
{
    if (b_canEditAll)
    {
        if (!canCloseTarget())
        {
            cancelEvent();
            RaiseEditor();
        }
        else
        {
             var lable=$("#lockEditor").text();
             var editAllState=1;
             if(lockArray[0]==lable){
            	 editAllState=1;
   	    	}else{
   	    	 	editAllState=2;
   	    	}
            str_url  = "<%=url_refresh%>" +"&dataFormat=json";
            str_url += "&editAll="+editAllState+"&random="+Math.random();
             
             $.getJSON(str_url, 
     				function(data) {
	            		 if(lockArray[0]==lable){
	          	    		$("#lockEditor").text(lockArray[1]);
	          	    	}else{
	          	    		$("#lockEditor").text(lockArray[0]);
	          	    	}
	          			lable=$("#lockEditor").text();
	          			try {parent.content.target.content.editAll(lable); } catch (ignore) {parent.content.content.editAll(lable);}
     		 	
	     	 });
             
             return;
            showHourglass();
            parent.Refresh(str_url);
        }
    }
}

function FindTargetFrame()
{
  try
  {
    if (b_singlePage)
    {
        if (b_singlePageIsSource)
        {
            return null;
        }
        else
        {
            return parent.content.content;
        }
    }
    else
    {
        return parent.content.target.content;
    }
 }catch(e){}
    return null;
}

function findSourceFrame()
{
    if (b_singlePage)
    {
        if (b_singlePageIsSource)
        {
            return parent.content.content;
        }
        else
        {
            return null;
        }
    }
    else
    {
        return parent.content.source.content;
    }

    return null;
}

function findSourceMenuFrame()
{
    if (b_singlePage)
    {
        if (!b_singlePageIsSource)
        {
            return null;
        }
        else
        {
            return parent.content.sourceMenu;
        }
    }
    else
    {
        return parent.content.source.sourceMenu;
    }

    return null;
}

function findTargetMenuFrame()
{
    if (b_singlePage)
    {
        if (b_singlePageIsSource)
        {
            return null;
        }
        else
        {
            return parent.content.targetMenu;
        }
    }
    else
    {
        return parent.content.target.targetMenu;
    }

    return null;
}

function CanClose()
{
    return canCloseTarget();
}

function canCloseTarget()
{
    var fr_target = FindTargetFrame();

    if (fr_target)
    {
      try
      {
        return fr_target.CanClose();
      }
      catch(e){}
    }

    return true;
}

function RaiseEditor()
{
    var fr_target = FindTargetFrame();

    if (fr_target)
    {
        fr_target.RaiseEditor();
    }
}

function ForceCloseEditor()
{
    var fr_target = FindTargetFrame();

    if (fr_target)
    {
        fr_target.ForceCloseEditor();
    }
}

function closeWindow()
{
    if (!canCloseTarget())
    {
      try
      {
        cancelEvent();
        RaiseEditor();
      }catch(e){}
    }
    else
    {
        parent.CloseThis();
    }
}

function cancelEvent()
{
    if (window.event != null)
    {
        window.event.returnValue = false;
        window.event.cancelBubble = true;
    }
}

function showOptions()
{
    w_options = window.open("<%=url_options%>", "MEOptions",
      "resizable=no,scrollbars=no,width=470,height=590");
}

function showPageInfo()
{
    w_pageinfo = window.open("<%=url_pageInfo%>", "MEPageInfo",
      "resizable,width=400,height=400");
}

function showSupportFiles()
{
    w_resources = window.open("<%=url_resources%>", "MESupportFiles",
       "height=400,width=500,resizable=yes,scrollbars=yes");
}

function showTermbases()
{
    w_termbases = window.open("<%=url_termbases%>", "METermbases",
       "height=400,width=500,resizable=yes,scrollbars=yes");
}

function createLisaQAReport()
{
	var action = ShutdownForm.action;
	ShutdownForm.action = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=generateReports";
	ShutdownForm.reportType.value = "CommentsAnalysisReport";
	ShutdownForm.submit();
	ShutdownForm.action = action;
	ShutdownForm.reportType.value = "";
}

function createCharacterCountReport()
{
	var action = ShutdownForm.action;
    ShutdownForm.action = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=generateReports";
    ShutdownForm.reportType.value = "CharacterCountReport";
    ShutdownForm.submit();
    ShutdownForm.action = action;
	ShutdownForm.reportType.value = "";
}

function CloseProgressWindow()
{
    try { w_progress.close(); } catch (ignore) {}
}

function CloseSearchWindow()
{
    try { w_search.close(); } catch (ignore) {}
}

function showSnippetEditor()
{
  if (!canCloseTarget())
  {
      cancelEvent();
      RaiseEditor();
      return false;
  }
  else
  {
    // Dialog arguments are: page id, current target locale, user role.
    // need to determine how to handle role.
    var args = new Object();
    args.pageId = "<%=state.getSourcePageId()%>";
    args.pageName = "<%=EditUtil.toJavascript(state.getSourcePageName())%>";
    args.locale = parent.g_targetViewLocale;
    args.displayLocale = parent.g_displayLocale;
    args.role = "_admin";

    idSnippetLibrary.args = args;

    var result = idSnippetLibrary.runModal();

    // reload this window - need to clear page cache on server
    showHourglass();
    parent.Refresh(parent.location);
  }
}

function HighlightTouched()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.HighlightTouched) != 'function')
        {
            // frame still loading, not ready to execute script
            return;
        }

        return f.HighlightTouched();
    }
}

function HighlightTranslated()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.HighlightTranslated) != 'function')
        {
            // frame still loading, not ready to execute script
            return;
        }

        return f.HighlightTranslated();
    }
}

function HighlightUntranslated()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.HighlightUntranslated) != 'function')
        {
            // frame still loading, not ready to execute script
            return;
        }

        return f.HighlightUntranslated();
    }
}

function HighlightNormal()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.HighlightNormal) != 'function')
        {
            // frame still loading, not ready to execute script
            return;
        }

        return f.HighlightNormal();
    }
}

function JumpFirstUntranslated()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.JumpFirstUntranslated) != 'function')
        {
            // frame still loading, not ready to execute script
            return;
        }

        return f.JumpFirstUntranslated();
    }
}

function JumpFirstFuzzy()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.JumpFirstFuzzy) != 'function')
        {
            // frame still loading, not ready to execute script
            return;
        }

        return f.JumpFirstFuzzy();
    }
}

function GetSegmentPreview()
{
    var f = FindTargetFrame();

    if (f)
    {
        if (typeof(f.GetSegmentPreview) != 'function')
        {
            // frame still loading, not ready to execute script
            return 'FRAME NOT LOADED ???';
        }

        return f.GetSegmentPreview();
    }
}

function init()
{
    //Check every 5 minutes for shutdown
    checkShutdown(1000 * 60 * 5);

    if ("<%=(layout.singlePageIsSource() ? 1 : 0)%>" == "1")
    {
        b_singlePageIsSource = true;
    }
    else
    {
        b_singlePageIsSource = false;
    }

    if ("<%=(layout.isSinglePage() ? 1 : 0)%>" == "1")
    {
        b_singlePage = true;
        showRadioButtons();
    }

    // update navigation arrow after onload.
    updateFileNavigationArrow();
    updatePageNavigationArrow();
    updateContentFlagMark();
}

// This is invoked after me_target.jsp is finished loading to avoid error 
// from quick file navigation.
function updateFileNavigationArrow()
{
	var fileNavPre, fileNavNext;

	var isFirstPage = '<%=state.isFirstPage()%>';
	if (isFirstPage == 'false')
	{
		fileNavPre = "<A HREF='#' onclick='refresh(-1); return false;' onfocus='this.blur()'>"
			+ "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
		document.getElementById("fileNavPre").innerHTML = fileNavPre;
	}

	var isLastPage = '<%=state.isLastPage()%>';
	if (isLastPage == 'false')
	{
        fileNavNext = "<A HREF='#' onclick='refresh(1); return false;' onfocus='this.blur()'>"
            + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
        document.getElementById("fileNavNext").innerHTML = fileNavNext;
	}
}

function updatePageNavigationArrow()
{
    var pageNavPre, pageNavNext;

    var isFirstBatch = '<%=state.isFirstBatch()%>';
    if (isFirstBatch == 'false')
    {
        pageNavPre = "<A HREF='#' onclick='refresh(-11); return false;' onfocus='this.blur()'>"
            + "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
        document.getElementById("pageNavPre").innerHTML = pageNavPre;
    }

    var isLastBatch = '<%=state.isLastBatch()%>';
	if (isLastBatch == 'false')
	{
        pageNavNext = "<A HREF='#' onclick='refresh(11); return false;' onfocus='this.blur()'>"
            + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
        document.getElementById("pageNavNext").innerHTML = pageNavNext;
	}
}

function EnterPress(e)
{
	var e = e || window.event;
	if(e.keyCode == 13)
	{ 
		var gotoPage = "0" + document.getElementById("gotoPageNav").value;
		var gotoPageNum = document.getElementById("gotoPageNav").value;
		var totalPageNum = <%=(pi.getTotalPageNum())%>;
		if(isNaN(gotoPageNum) || (gotoPageNum.indexOf(".")>0))
	    {
			alert("Invalid number !");
			return;
	    }
		else
		{
	    	if(parseInt(totalPageNum)>=parseInt(gotoPageNum)&&parseInt(gotoPageNum)>0)
	    	{
		    	refresh(gotoPage);
	     	}
	    	else
	    	{
		    	alert("The input number should be between 1 and maximum page number !");
		    	return;
		    }
	    }
	}
}

function searchByUserOrSid() {

    var f = FindTargetFrame();
    if (!f)
    {
        // user must switch to target page first
        return;
    }

      var url = "<%=url_search%>&search=true";
      
      if(document.recalc)
      {
          if (!w_search || w_search.closed)
      	  {
            	var args = { _opener: window, _data: false };
              w_search = window.open(url,"","height=480px, width=700px,status=no,resizable=yes,modal=yes,scrollbars=yes"); 		
              		
          }
          else
          {
    	        w_search.focus();
  	      }
      }
      else
      {
          if (!w_search || w_search.closed)
      	  {
      		window.myAction=this;
      		window.myArguments=true;
      		w_search = window.open(url,"","height=480px, width=700px,status=no,resizable=yes,modal=yes,scrollbars=yes");
      		}
      		else
          {
    	        w_search.focus();
  	      }
      }
  }

var ptag;
var rep;
var comment;
function updateContentFlagMark(){
	rep=$("#findRepeatedSegments").text();
	ptag=$("#showPtags").text();
	comment=$.trim($("#reviewMode").text());
	parent.toggleComments(comment);
}
var Repeatedlable=["<%=unReplable%>","<%=Replable%>"];
// Find Repeated Segments/Unmark Repeated Segments
function findRepeatedSegments()
{
    var str_url;

    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
    }
    else
    {
    	var lable=$("#findRepeatedSegments").text();
    	 str_url  = "<%=url_refresh%>" +"&dataFormat=json"+"&<%=WebAppConstants.PROPAGATE_ACTION%>=" + lable+"&random="+Math.random();
    	 $.getJSON(str_url, 
    				function(data) {
		    		 	if(Repeatedlable[0]==lable){
		    	    		$("#findRepeatedSegments").text(Repeatedlable[1]);
		    	    		parent.showRepeated = false;
		    	    	}else{
		    	    		$("#findRepeatedSegments").text(Repeatedlable[0]);
		    	    		parent.showRepeated = true;
		    	    	}
		    		 	rep=$("#findRepeatedSegments").text();
		    		 	try {parent.content.target.content.findRepeatedSegments(rep);   } catch (ignore) {parent.content.content.findRepeatedSegments(rep);}
    	 });
    	 parent.getRedata();
    	return;

        showHourglass();
        parent.Refresh(str_url);
    }
}

<!-- Show/Unmark PTags -->

var PtagLable=["<%=marklable%>","<%=unmarklable%>"];

// Show/Unmark PTags
var targetModeId="<%=targetViewMode %>";
var sourceModeId="<%=sourceViewMode %>";
var targetJsonUrl=this.location+"&dataFormat=json"+"&trgViewMode=" + targetModeId+"&random="+Math.random();
var sourceJsonUrl=this.location+"&dataFormat=json"+"&srcViewMode=" + sourceModeId+"&random="+Math.random();
function showPtags()
{
    var str_url;

    if (!canCloseTarget())
    {
        cancelEvent();
        RaiseEditor();
    }
    else
    {
    	var lable=$("#showPtags").text();
        str_url  = "<%=url_refresh%>"+"&dataFormat=json"+ "&pTagsAction=" + lable+"&random="+Math.random();
	   	 $.getJSON(str_url, 
  				function(data) {
			    	if(PtagLable[0]==lable){
			    		$("#showPtags").text(PtagLable[1]);
			    		parent.showPtags = true;
			    	}else{
			    		$("#showPtags").text(PtagLable[0]);
			    		parent.showPtags = false;
			    	}
			    	ptag=$("#showPtags").text();
			    	//just for ininal text
			    	if(radiobuttons.style.visibility == "visible")
			    	{
				    	if(document.getElementById("idShowTarget").checked)
				    	{
				    		parent.getDataByFrom(targetJsonUrl,"target");
				    	}
				    	if(document.getElementById("idShowSource").checked)
				    	{
				    		parent.getDataByFrom(sourceJsonUrl,"source");
				    	}
					}
			    	else
			    	{
						parent.getRedata();
			    	}
	   	 });

    	return;

        showHourglass();
        parent.Refresh(str_url);
    }
}

function openAutoPropagate()
{
    w_autoPropagate = window.open("<%=url_autoPropagate%>", "AutoPropagate",
    	"resizable=yes,scrollbars=no,width=350,height=350");
}
</SCRIPT>
</HEAD>
<BODY id="idBody" onload="init()" onbeforeunload="exit()">

<iframe id="idShutdown" name="idShutdown" src="about:blank" style="display:none"></iframe>
<FORM name="ShutdownForm" METHOD="POST" TARGET="idShutdown"
 ACTION="/globalsight/envoy/common/shutdownPopup.jsp">
<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=jobId%>">
<input type="hidden" name="<%=ReportConstants.TARGETLOCALE_LIST%>" value="<%=tgtIDS%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="">
<input type="hidden" name="withCompactTagsCCR"  value="on">
</FORM>

<DIV id="idSnippetLibrary"
  style="position: absolute; top: 0; left: 0; display: none;
  behavior: url('/globalsight/envoy/edit/snippets/SnippetLibrary.htc');"></DIV>

<DIV ID="main" STYLE="POSITION: ABSOLUTE; Z-INDEX: 10; LEFT: 0px; TOP: 0px; width:100%">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" WIDTH="100%">
  <TR CLASS="tableHeadingBasic">
    <TD VALIGN="TOP">
      <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
	<TR CLASS="tableHeadingBasic">
	  <TD WIDTH="20">&nbsp;</TD>
	  <TD NOWRAP VALIGN="TOP" ALIGN="CENTER"><%=lb_layoutModes%><BR>
	    <A HREF="#" onClick="horizontalSplit(); return false;"
	    onFocus="this.blur();"><IMG SRC="<%=lb_horizontalSplit%>"
	    BORDER="0" VSPACE="4"></A>
	    <A HREF="#" onClick="verticalSplit(); return false;"
	    onFocus="this.blur();"><IMG SRC="<%=lb_verticalSplit%>"
	    BORDER="0" HSPACE="5" VSPACE="4"></A>
	    <A HREF="#" onClick="noSplit(); return false;"
	    onFocus="this.blur();"><IMG SRC="<%=lb_noSplit%>"
	    BORDER="0" VSPACE="4"></A>
	  </TD>
	  <!-- File Navigation -->
	  <TD WIDTH="20">&nbsp;</TD>
	  <TD NOWRAP VALIGN="TOP" ALIGN="CENTER"><%=lb_fileNavigation%><BR>
	    <label id="fileNavPre"><%=lb_prevFile%></label>
	    <label id="fileNavNext"><%=lb_nextFile%></label>
	  </TD>
	  <!-- Page Navigation -->
	  <TD WIDTH="20">&nbsp;</TD>
	  <TD NOWRAP VALIGN="TOP" ALIGN="CENTER"><%=lb_pageNavigation%>&nbsp;
	    (<%=pi.getCurrentPageNum()%> of <%=pi.getTotalPageNum()%>)<BR/>
	    <label id="pageNavPre"><%=lb_prevPage%></label>
	    <label id="pageNavNext"><%=lb_nextPage%></label> 
	    <label style="position: relative; bottom: 6px; left: 8px; hight: 1px">Goto
			<input type="text" id="gotoPageNav"
			onkeypress="EnterPress(event)" style="height: 18px; width: 30px" value="" />
		</label>
	  </TD>
	  <TD WIDTH="20">&nbsp;</TD>
	  <TD VALIGN="TOP">
	    <DIV id='radiobuttons'
	     STYLE='POSITION: relative; VISIBILITY: hidden'>
	    <FORM NAME='bob'>
	    <INPUT TYPE='radio' NAME='display' id='idShowSource'
	     onfocus='blur()' onclick='return showSourcePage();'
	    <% if (layout.singlePageIsSource()) { out.print(" CHECKED"); } %>
	    ><label for='idShowSource'><%=lb_source%></label><BR>
	    <INPUT TYPE='radio' NAME='display' id='idShowTarget'
	     onfocus='blur()' onclick='return showTargetPage();'
	    <% if (!layout.singlePageIsSource()) { out.print(" CHECKED"); } %>
	    ><label for='idShowTarget'><%=lb_target%></label>
	    </FORM>
	    </DIV>
	  </TD>
	</TR>
      </TABLE>
    </TD>
    <TD ALIGN="RIGHT" VALIGN="TOP">
      <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
	<TR>
	  <TD HEIGHT="12">
	    <IMG SRC="/globalsight/images/spacer.gif" HEIGHT="10" WIDTH="1">
	  </TD>
	</TR>
	<TR CLASS="tableHeadingBasic">
	  <TD>
	  <!-- Unlock -->
      <%
	    if (!state.getIsReviewActivity() && !state.isReadOnly() 
	            && state.canEditAll() && b_showUnlockButton)
	    {
	        StringBuffer unlockSB = new StringBuffer();
	        unlockSB.append("<A HREF='#' id='lockEditor' onclick='editAll(); return false;'");
            unlockSB.append(" CLASS=\"HREFBoldWhite\">");
            if (state.isEditAll()) {
		        unlockSB.append(lb_lock);
            } else {
		        unlockSB.append(lb_unlock);
		    }
            unlockSB.append("</A> |&nbsp;");
		    out.print(unlockSB.toString());
	    }
	  %>
	  
	  <!-- Show/Unmark PTags -->
      <% if (state.getNeedShowPTags()) {
			lable = bundle.getString("lb_unmark_pTag_segments");
            action = WebAppConstants.PTAGS_ACTION_UNMARK;
         } else {
            lable = bundle.getString("lb_find_pTag_segments");
            action = WebAppConstants.PTAGS_ACTION_FIND;
         }
      %>
	  <A href="#" onclick="showPtags();" id="showPtags"
	     CLASS="HREFBoldWhite" title="<%=lable%>"><%=lable%></A> |
	  
   	  <!-- Show/Unmark Repeated -->
      <% if (b_showRepeatedLink) {
           if (state.getNeedFindRepeatedSegments()) {
               lable = bundle.getString("lb_unmark_repeated_segments");
               action = WebAppConstants.PROPAGATE_ACTION_UNMARK;
           } else {
               lable = bundle.getString("lb_find_repeated_segments");
               action = WebAppConstants.PROPAGATE_ACTION_FIND;
           }
      %>
	  <A href="#" onclick="findRepeatedSegments();" id="findRepeatedSegments" 
	     CLASS="HREFBoldWhite" title="<%=lable %>"><%=lable%></A> |
	  <% } %>
	  
	  <!-- Auto-Propagate -->
      <% if (b_showAutoPropagateLink) { %>
	  <A href="#" onclick="openAutoPropagate();"
	     CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_automatic_propagate")%>">
	     <%=bundle.getString("lb_automatic_propagate")%></A> |
	  <% } %>

	  <!-- Search -->
	  <A href="#" onclick="searchByUserOrSid(); return false;"
	     CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_search")%>"><%=bundle.getString("lb_search")%></A> |
   	  <!--  Comments Analysis -->
	  <amb:permission name="<%=Permission.REPORTS_COMMENTS_ANALYSIS%>">
	  	<A href="#" onclick="createLisaQAReport(); return false;"
	     CLASS="HREFBoldWhite" title="Create Comments Analysis Report">
	    Comments Analysis</A> |
      </amb:permission>
      <amb:permission name="<%=Permission.REPORTS_CHARACTER_COUNT%>">
        <A href="#" onclick="createCharacterCountReport(); return false;"
         CLASS="HREFBoldWhite" title="Create Character Count Report">
        Character Count</A> |
	  </amb:permission>
	    <% if (state.isReviewMode()) {
	         if (!state.getIsReviewActivity())
	         {
	    %>
	    <A href="#" onclick="reviewMode(); return false;" id="reviewMode"
	     CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_editor_hide_segment_comments") %>">
	    <%=bundle.getString("lb_editor_hide_comments") %></A> |
	    <% } } else { %>
	    <A href="#" onclick="reviewMode(); return false;" id="reviewMode"
	     CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_editor_show_segment_comments") %>">
	    <%=bundle.getString("lb_editor_show_comments") %></A> |
	    <% } %>
	    <A href="#" onclick="showSupportFiles(); return false;"
	     CLASS="HREFBoldWhite" title="<%=lb_showSupportFiles%>">
	    <%=lb_supportFiles%></A> |
	    <A href="#" onclick="showTermbases(); return false;"
	     CLASS="HREFBoldWhite" title="<%=lb_showTermbases%>">
	    <%=lb_termbases%></A> |
	    <%
	    if (b_snippets)
	    {
	      if (!state.getIsReviewActivity() && state.canEditSnippets())
	      {
	    %>
	    <A href="#" onclick="showSnippetEditor(); return false;"
	     CLASS="HREFBoldWhite" title="<%=lb_editLocaleContent%>">
	     <%=lb_snippetEditor%></A> |
	    <%
	      }
	    }
	    %>
	    <A HREF="#" onclick="showPageInfo(); return false;"
	    CLASS="HREFBoldWhite"><%=lb_pageInfo%></A> |
	    <A HREF="#" onclick="showOptions(); return false;"
	    CLASS="HREFBoldWhite"><%=lb_options%></A> |
	    <A HREF="#" onclick="closeWindow(); return false;"
	    CLASS="HREFBoldWhite"><%=lb_close%></A> |
	    <A HREF="#" onclick="helpSwitch(); return false;"
	    CLASS="HREFBoldWhite"><%=lb_help%></A>&nbsp;
	  </TD>
	</TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>

</DIV>
</BODY>
</HTML>
