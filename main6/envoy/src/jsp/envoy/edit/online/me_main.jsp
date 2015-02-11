<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
		    com.globalsight.everest.webapp.pagehandler.PageHandler,
		    com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
		    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
		    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
		    com.globalsight.everest.servlet.util.SessionManager,
		    com.globalsight.everest.webapp.WebAppConstants,
		    com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="menu" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="contentSrcTrg" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="contentSrc" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="contentTrg" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="contentReview" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String selfURL = self.getPageURL();
	String menuURL = menu.getPageURL();
	String contentSrcTrgURL = contentSrcTrg.getPageURL() ;
	String contentSrcURL = contentSrc.getPageURL();
	String contentTrgURL = contentTrg.getPageURL();
	String contentReviewURL = contentReview.getPageURL();

	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);
	EditorState state = (EditorState) sessionMgr
			.getAttribute(WebAppConstants.EDITORSTATE);

	String lb_title;

	// to debug review activities
	//state.setReviewMode();
	//state.setIsReviewActivity(true);

	if (state.isReviewMode())
	{
		lb_title = "Page Review";
	}
	else if (state.isReadOnly())
	{
		lb_title = bundle.getString("lb_viewer");
	}
	else
	{
		lb_title = bundle.getString("lb_main_editor");
	}

	lb_title = lb_title + " - " + state.getSimpleSourcePageName();

	String str_targetViewLocale = state.getTargetViewLocale()
			.toString();
	String str_displayLocale = state.getTargetViewLocale()
			.getDisplayName();

	String contentURL = null;
	EditorState.Layout layout = state.getLayout();

	if (layout.isSinglePage())
	{
		if (layout.singlePageIsSource())
		{
			contentURL = contentSrcURL;
		}
		else
		{
			contentURL = contentTrgURL;
		}
	}
	else
	{
		contentURL = contentSrcTrgURL;
	}
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT>
var g_refreshing = false;
var g_targetViewLocale = "<%=str_targetViewLocale%>";
var g_displayLocale = "<%=str_displayLocale%>";

window.focus();

function CloseThis()
{
    window.close();
}

function CloseProgressWindow()
{
    menu.CloseProgressWindow();
}

function CanClose()
{
    return CanCloseTarget() && CanCloseReview();
}

function CanCloseTarget()
{
    return menu.CanClose();
}

function CanCloseReview()
{
    try { return review.CanClose(); } catch (ex) { return true; }
}

// Public API for taskDetails, jobDetails and segmentComments.
function RaiseSegmentEditor()
{
    return menu.RaiseEditor();
}

// for me_target
function RaiseCommentEditor()
{
    try { review.RaiseCommentEditor(); } catch (ignore) {}
}

function ForceCloseEditor()
{
    menu.ForceCloseEditor();
}

function ForceCloseCommentEditor()
{
    try { review.ForceClose(); } catch (ignore) {}
}

function Refresh(p_url)
{
    g_refreshing = true;
    document.location = p_url;
}

function RefreshCommentPane()
{
    review.document.location = review.document.location;
}

function RefreshContentPane(p_url)
{
    content.location = p_url;
}

function RefreshTargetPane()
{
    content.RefreshTargetPane();
}

function SwitchTargetLocale(p_locale)
{
    Refresh("<%=selfURL%>&refresh=0&trgViewLocale=" + p_locale);
}

function SegmentFilter(p_segmentFilter)
{
    Refresh("<%=selfURL%>&refresh=0&segmentFilter=" + p_segmentFilter);
}

var localData;
var jsonUrl;
function getData(url){
	jsonUrl=url;
	$.getJSON(url+"&random="+Math.random(), function(data) {
		localData = data;
		showTargetList();
		showSourceList();
	});
}

function getDataByFrom(url,modeFrom){
	jsonUrl=url;
	$.getJSON(url+"&random="+Math.random(), function(data) {
		localData = data;
		if(modeFrom == "target")
		{
			showTargetList();
		}
		if(modeFrom == "source")
		{
			showSourceList();
		}
	});
}

function getRedata() {
	localData = null;
	getData(jsonUrl);
}

function showTargetList(){
    try {
        content.target.content.buildData(localData.target);
    } catch (ignore) {
        content.content.buildData(localData.target);
    }
}

function showSourceList(){
    try {
	    content.source.content.buildData(localData.source);
    } catch (ignore) {
    	content.content.buildData(localData.source);
    }
}

function SetTargetLocaleInfo(p_targetViewLocale, p_displayLocale)
{
    g_targetViewLocale = p_targetViewLocale;
    g_displayLocale = p_displayLocale;
}

function HighlightSegment(p_tuId, p_tuvId, p_subId)
{
    content.HighlightSegment(p_tuId, p_tuvId, p_subId);
}

function UnhighlightSegment(p_tuId, p_tuvId, p_subId)
{
    content.UnhighlightSegment(p_tuId, p_tuvId, p_subId);
}

function toggleComments(lable){
	if(lable!="Show Comments")
	{
		$("#mainSet").attr("ROWS","46,67%,*");
	}else{
		$("#mainSet").attr("ROWS","46,*");
	}

	if(!$("#review").attr("src"))
    {
        $("#review").attr("src","<%=contentReviewURL%>");
    }
}
</SCRIPT>
<!--SCRIPT FOR=window EVENT=onunload>
// Callback for segmentComments.jsp to refresh itself
// when new comments may have been added.
if (!g_refreshing)
{
    try { window.opener.RefreshComments(); } catch (ignore) {}
}
</SCRIPT-->
</HEAD>
<%
	if (state.isReviewMode())
	{
%>
  <FRAMESET ROWS="46,67%,*" FRAMEBORDER="yes" BORDER="4" framespacing="2" bordercolor="lightgrey" FRAMESPACING="0" id="mainSet">
    <FRAME NAME="menu" SCROLLING="no" MARGINHEIGHT="0" MARGINWIDTH="0"  NORESIZE SRC="<%=menuURL%>" >
    <FRAME NAME="content" SCROLLING="no" BORDER="1" MARGINHEIGHT="0" MARGINWIDTH="0" SRC="<%=contentURL%>" >
    <FRAME id="review" NAME="review" SCROLLING="yes" MARGINHEIGHT="0" MARGINWIDTH="0">
  </FRAMESET>
   <%
   	}
   	else
   	{
   %>
    <FRAMESET ROWS="46,*" FRAMEBORDER="yes" BORDER="4" framespacing="2" bordercolor="lightgrey" FRAMESPACING="0" id="mainSet">
    <FRAME NAME="menu" SCROLLING="no" MARGINHEIGHT="0" MARGINWIDTH="0" NORESIZE SRC="<%=menuURL%>" >
    <FRAME NAME="content" SCROLLING="no" BORDER="1" MARGINHEIGHT="0" MARGINWIDTH="0" SRC="<%=contentURL%>" >
    <FRAME id="review" NAME="review" SCROLLING="yes" MARGINHEIGHT="0" MARGINWIDTH="0" display="none">
  </FRAMESET>
   <%
   	}
   %>

</HTML>
