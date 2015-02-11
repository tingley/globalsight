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
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.SessionManager,
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

//file navigation (default unavailable)
String lb_prevFile = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextFile = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

//page navigation (default unavailable)
String lb_prevPage = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextPage = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

PaginateInfo pi = state.getPaginateInfo();

// Determine if the unlock button is VISIBLE or not, if it can be shown at all.
boolean b_showUnlockButton = true;
if ((state.isReadOnly() && !state.canEditAll()) ||
    (layout.isSinglePage() && layout.singlePageIsSource()))
{
    b_showUnlockButton = false;
}
%>
<HTML>
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
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
var w_search = null

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

function reviewMode(p_on)
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
        str_url += "&<%=WebAppConstants.REVIEW_MODE%>=" + p_on;

        showHourglass();
        parent.Refresh(str_url);
    }
}

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
            str_url  = "<%=url_refresh%>";
            str_url += "&editAll=<%= state.isEditAll() ?
             EditorConstants.EDIT_DEFAULT : EditorConstants.EDIT_ALL%>";

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
      "resizable=no,scrollbars=no,width=470,height=480");
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
	ShutdownForm.action = "/globalsight/envoy/administration/reports/LisaQACommentsAnalysisReport.jsp";
	ShutdownForm.submit();
}

function createCharacterCountReport()
{
    ShutdownForm.action = "/globalsight/envoy/administration/reports/characterCountReport.jsp";
    ShutdownForm.submit();
}

function showProgressWindow()
{
    var f = FindTargetFrame();
    if (!f)
    {
        // user must switch to target page first
        return;
    }

    if(document.recalc)
  {
  	if (!w_progress || w_progress.closed)
  	{
    	var args = { _opener: window, _data: false };

   		w_progress = showModelessDialog(
      		"/globalsight/envoy/edit/online2/progress.jsp", args,
      		"dialogWidth:280px; dialogHeight:400px; status:no; help:no;");
  	}
  	else
  	{
    	w_progress.focus();
  	}
  }
  else
  {
  		window.myAction=this;
  		window.myArguments=true;
  		var url = "/globalsight/envoy/edit/online2/progress.jsp";
    	w_progress = window.open(url,"","height=280px, width=400px,status=no,modal=yes"); 
  }
  
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
</SCRIPT>
</HEAD>
<BODY id="idBody" onload="init()" onbeforeunload="exit()">

<iframe id="idShutdown" name="idShutdown" src="about:blank" style="display:none"></iframe>
<FORM name="ShutdownForm" METHOD="POST" TARGET="idShutdown"
 ACTION="/globalsight/envoy/common/shutdownPopup.jsp">
<INPUT TYPE="hidden" NAME="a" VALUE="">
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
	    <%
	    // Determine if the unlock button is AVAILABLE at all, or not.
	    if (!state.getIsReviewActivity() &&
	       (!state.isReadOnly() && state.canEditAll()))
	    {
	      if (b_showUnlockButton)
	      {
	        out.print("<DIV id=unlock ");
	        out.print("STYLE='POSITION:relative; VISIBILITY:show'>");
	      }
	      else
	      {
	        out.print("<DIV id=unlock ");
	        out.print("STYLE='POSITION:relative; VISIBILITY:hidden'>");
	      }

	      out.print("<A HREF='#' onclick='editAll(); return false;'");
	      out.print(" CLASS=\"HREFBoldWhite\">");
	      if (state.isEditAll())
	      {
	        out.print(lb_lock);
	      }
	      else
	      {
	        out.print(lb_unlock);
	      }
	      out.print("</A> |&nbsp;");

	      out.print("</DIV>");
	    }
	    %>
	  </TD>
	  <TD>
	     <A href="#" onclick="searchByUserOrSid(); return false;"
	     CLASS="HREFBoldWhite" title="Search">Search</A> |
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
	    <A href="#" onclick="showProgressWindow(); return false;"
	     CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_progress_window_open") %>">
	    <%=bundle.getString("lb_progress") %></A> |
	    <% if (state.isReviewMode()) {
	         if (!state.getIsReviewActivity())
	         {
	    %>
	    <A href="#" onclick="reviewMode(false); return false;"
	     CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_editor_hide_segment_comments") %>">
	    <%=bundle.getString("lb_editor_hide_comments") %></A> |
	    <% } } else { %>
	    <A href="#" onclick="reviewMode(true); return false;"
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
