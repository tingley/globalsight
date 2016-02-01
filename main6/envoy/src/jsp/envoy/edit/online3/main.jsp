<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,java.io.File,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.SortUtil,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.ling.common.Text,
            com.globalsight.everest.comment.Issue,
            com.globalsight.everest.comment.IssueHistory,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.everest.edit.online.CommentView,
            com.globalsight.everest.edit.online.CommentThreadView,
            com.globalsight.everest.edit.online.UIConstants,
            com.globalsight.everest.edit.online.OnlineEditorConstants,
            com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.edit.online.PaginateInfo,
            com.globalsight.everest.taskmanager.Task,
            com.globalsight.config.UserParamNames,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
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
 <jsp:useBean id="segmentEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="commentEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="skin" scope="request"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
 <%@ include file="/envoy/common/installedModules.jspIncl" %>
 <%!
static private final int COL_ID = 1;
static private final int COL_USER = 2;
static private final int COL_TITLE = 3;
static private final int COL_DATE = 4;
static private final int COL_STATUS = 5;
static private final int COL_PRIO = 6;
static private final int COL_CATEGORY = 7;

static private final String SORT_NONE = "/globalsight/images/sort-blank.gif";
static private final String SORT_UP   = "/globalsight/images/sort-up.gif";
static private final String SORT_DOWN = "/globalsight/images/sort-down.gif";

static private String getStatusLabel(ResourceBundle p_bundle, Issue p_issue)
{
    return p_bundle.getString("issue.status." + p_issue.getStatus());
}

static private String getPriorityLabel(ResourceBundle p_bundle, Issue p_issue)
{
    return p_bundle.getString("issue.priority." + p_issue.getPriority());
}

static private String getCategoryLabel(ResourceBundle p_bundle, Issue p_issue)
{
    return p_issue.getCategory();
}

static private String getSortIcon(CommentThreadView p_view, int p_col)
{
    String sortedBy = p_view.getSortedBy();
    String img = null;

    switch (p_col)
    {
    case COL_ID:
      if (CommentThreadView.SORT_SEGMENT_ASC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      else if (CommentThreadView.SORT_SEGMENT_DESC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      break;

    case COL_USER:
      if (CommentThreadView.SORT_USER_ASC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      else if (CommentThreadView.SORT_USER_DESC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      break;

    case COL_TITLE:
      if (CommentThreadView.SORT_TITLE_ASC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      else if (CommentThreadView.SORT_TITLE_DESC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      break;

    case COL_DATE:
      if (CommentThreadView.SORT_DATE_ASC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      else if (CommentThreadView.SORT_DATE_DESC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      break;

    case COL_STATUS:
      if (CommentThreadView.SORT_STATUS_ASC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      else if (CommentThreadView.SORT_STATUS_DESC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      break;

    case COL_PRIO:
      if (CommentThreadView.SORT_PRIO_ASC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      else if (CommentThreadView.SORT_PRIO_DESC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      break;
     
    case COL_CATEGORY:
      if (CommentThreadView.SORT_CATEGORY_ASC.equals(sortedBy))
      {
        img = SORT_UP;
      }
      else if (CommentThreadView.SORT_CATEGORY_DESC.equals(sortedBy))
      {
        img = SORT_DOWN;
      }
      break;

    default: break;
    }

    if (img == null)
    {
        return "";
    }

    return "<IMG src='" + img + "'>";
}

static private String getSortCondition(CommentThreadView p_view, int p_col)
{
    String sortedBy = p_view.getSortedBy();

    switch (p_col)
    {
    case COL_ID:
      if (CommentThreadView.SORT_SEGMENT_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_SEGMENT_DESC;
      }

      return CommentThreadView.SORT_SEGMENT_ASC;

    case COL_USER:
      if (CommentThreadView.SORT_USER_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_USER_DESC;
      }

      return CommentThreadView.SORT_USER_ASC;

    case COL_TITLE:
      if (CommentThreadView.SORT_TITLE_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_TITLE_DESC;
      }

      return CommentThreadView.SORT_TITLE_ASC;

    case COL_DATE:
      if (CommentThreadView.SORT_DATE_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_DATE_DESC;
      }

      return CommentThreadView.SORT_DATE_ASC;

    case COL_STATUS:
      if (CommentThreadView.SORT_STATUS_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_STATUS_DESC;
      }

      return CommentThreadView.SORT_STATUS_ASC;

    case COL_PRIO:
      if (CommentThreadView.SORT_PRIO_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_PRIO_DESC;
      }

      return CommentThreadView.SORT_PRIO_ASC;
      
    case COL_CATEGORY:
      if (CommentThreadView.SORT_CATEGORY_ASC.equals(sortedBy))
      {
        return CommentThreadView.SORT_CATEGORY_DESC;
      }

      return CommentThreadView.SORT_CATEGORY_ASC;
    }

    return "";
}
%>
<%
//main.jsp
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager) session
		.getAttribute(WebAppConstants.SESSION_MANAGER);
EditorState state = (EditorState) sessionMgr
		.getAttribute(WebAppConstants.EDITORSTATE);

String lb_title;
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
	lb_title = "Post Review Editor";
}
lb_title = lb_title + " - " + state.getSimpleSourcePageName();

String url_self = self.getPageURL();
String url_refresh     = refresh.getPageURL();
String url_pageInfo    = pageInfo.getPageURL();
String url_resources   = resources.getPageURL();
String url_termbases   = termbases.getPageURL();
String url_options     = options.getPageURL();
String url_search     = search.getPageURL();
String url_segmentEditor = segmentEditor.getPageURL();
String url_commentEditor = commentEditor.getPageURL();

String lb_close = bundle.getString("lb_close");
String lb_help = bundle.getString("lb_help");
String lb_options = bundle.getString("lb_options");
String lb_pageInfo = bundle.getString("lb_pageInfo");
String lb_fileNavigation = bundle.getString("lb_fileNavigation");
String lb_pageNavigation = bundle.getString("lb_pageNavigation");
String lb_showSupportFiles = bundle.getString("lb_showSupportFiles");
String lb_showTermbases = bundle.getString("lb_showTermbases");
String lb_source = bundle.getString("lb_source");
String lb_source_segment = bundle.getString("lb_source_segment");
String lb_previous_translation = bundle.getString("lb_previous_translation");
String lb_current_translation = bundle.getString("lb_current_translation");
String lb_supportFiles = bundle.getString("lb_supportFiles");
String lb_target = bundle.getString("lb_target");
String lb_termbases = bundle.getString("lb_termbases");
String lb_id = bundle.getString("lb_id");
String lb_segment = bundle.getString("lb_segment");
String lable = "";
String action = "";
String unmarklable = bundle.getString("lb_unmark_pTag_segments");
String marklable = bundle.getString("lb_find_pTag_segments");

String lb_prevFile = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextFile = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

String lb_prevPage = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextPage = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

PaginateInfo pi = state.getPaginateInfo();

long jobId = Long.valueOf(sessionMgr.getAttribute(WebAppConstants.JOB_ID).toString());
String tgtIDS = sessionMgr.getAttribute(ReportConstants.TARGETLOCALE_LIST).toString();

Boolean assigneeValue = (Boolean)TaskHelper.retrieveObject(
		   session, WebAppConstants.IS_ASSIGNEE);
boolean isAssignee = assigneeValue == null ? true :
		   assigneeValue.booleanValue();
boolean b_readOnly = state.isReadOnly();
boolean b_isReviewActivity = state.getIsReviewActivity();
boolean disableComment = isAssignee && b_readOnly;
long lastTuId  = state.getTuId();
long lastTuvId = state.getTuvId();
long lastSubId = state.getSubId();

String lb_sourceLocale = bundle.getString("lb_source_locale");
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
String str_sourceLocale = state.getSourceLocale().getDisplayName(uiLocale);
String lb_targetLocale  = bundle.getString("lb_target_locale");
GlobalSightLocale locale = (GlobalSightLocale)sessionMgr.getAttribute("targetLocale");
StringBuffer str_targetLocale = new StringBuffer();
if (state.isViewerMode() || (state.isReviewMode() && state.getUserIsPm()))
{
	str_targetLocale.append("<select name='tarLocales' onchange='switchTargetLocale(this[this.selectedIndex].value)' style='font-size: 8pt;'>");
	Vector targetLocales = state.getJobTargetLocales();
	SortUtil.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
	GlobalSightLocale trg = null;
	for (int i = 0, max = targetLocales.size(); i < max; i++)
	{
	    trg = (GlobalSightLocale)targetLocales.get(i);
	
	    str_targetLocale.append("<option ");
	
		if(locale != null)
		{
			 if (trg.equals(locale))
		        {
		            str_targetLocale.append("selected ");
		            sessionMgr.removeElement("targetLocale");
		        }
		}
		else
		{
			if (trg.equals(state.getTargetLocale()))
	        {
	            str_targetLocale.append("selected ");
	        }
		}
	    str_targetLocale.append("value='").append(trg.toString()).append("'>");
	    str_targetLocale.append(trg.getDisplayName(uiLocale));
	    str_targetLocale.append("</option>");
	}
	str_targetLocale.append("</select>");
}
else
{
	str_targetLocale.append(state.getTargetLocale().getDisplayName(uiLocale));
}
//segmentFilter
String selSegFilter = (String)request.getAttribute("segmentFilter");
StringBuffer str_segmengFilter = new StringBuffer();
str_segmengFilter.append(bundle.getString("segment_filter")).append(":&nbsp;&nbsp;");
str_segmengFilter.append("<select id='segmentFilter' ");
str_segmengFilter.append("onchange='SegmentFilter(this[this.selectedIndex].value)' ");
str_segmengFilter.append("style='font-size: 8pt;'>");
for(String segFilter : OnlineEditorConstants.SEGMENT_FILTERS)
{
    str_segmengFilter.append("<option ");
    if (segFilter.equals(selSegFilter))
    {
        str_segmengFilter.append("selected ");
    }
    str_segmengFilter.append("value=\""+segFilter+"\">")
    				 .append(bundle.getString(segFilter))
    				 .append("</option>");
}
str_segmengFilter.append("</select>");

StringBuffer filters = new StringBuffer();
filters.append("<table WIDTH='100%' CELLSPACING='0' class='tableHeadingBasic'>");
filters.append("<tr><td align='right'>");
filters.append(str_segmengFilter);
filters.append("</td></tr></table>");

String navBottom = "";
String footBottom = "";
String commentDisplay = "";
if(state.isReviewMode())
{
	navBottom = "300px";
	footBottom = "270px";
}
else
{
	navBottom = "30px";
	footBottom = "0";
	commentDisplay = "display:none";
}
CommentThreadView view = state.getCommentThreads();
int currentIssuesSize = 0;
if(view != null)
{
	currentIssuesSize = view.getIssues().size();
}
boolean b_rtl = EditUtil.isRTLLocale(state.getTargetLocale());
String enableCloseAllComments = PageHandler.getUserParameter(
        session, UserParamNames.EDITOR_SHOW_CLOSEALLCOMMENT).getValue();
boolean isActive = false;
if (sessionMgr.getAttribute("taskStatus") != null)
{
    String taskStatus = (String) sessionMgr.getAttribute("taskStatus");
    if (Task.STATE_ACTIVE_STR.equalsIgnoreCase(taskStatus)) {
        isActive = true;
     }
}

String closeeAllCommentWarning = bundle.getString("jsmsg_editor_close_all_comments_warning");
String approveAction = (String)sessionMgr.getAttribute("approveAction");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
.alt { background:#EEEEEE;}
.top {width:100%;position:absolute;Z-INDEX: 10; LEFT: 0px;top:0;}
.nav {width:100%;position:absolute;Z-INDEX: 10; LEFT: 0px;top:42px; bottom:<%=navBottom%>; height:auto;}
.foot {width:100%;position:absolute;Z-INDEX: 10; LEFT: 0px;bottom:<%=footBottom%>;}
.commentDiv{width:100%;position:absolute;Z-INDEX: 10; LEFT: 0px;bottom:0;height:270px;}

.segmentTd{word-break: keep-all;word-wrap : break-word; overflow:hidden;}
.center{text-align:center;}

#idCommentTable { font-family: verdana; font-size: 10pt;border-bottom: 1px solid black;}
#idCommentTable TH {font-size: 9pt;color: <%=skin.getProperty("skin.tableHeading.fgColor")%>;background-color: <%=skin.getProperty("skin.tableHeading.bgColor")%>;}
#idCommentTable TH SPAN { cursor: hand;  cursor:pointer;}

#idCommentTable P { margin-top: 0px; margin-bottom: 0px; }
.editorId   { font-weight: bold;}

/* Background colors for issue status, class name is the status token. */
.open   { background-color: red !important; }
.closed { background-color: lawngreen !important; }

.noUnderline{text-decoration:none;}

.clickable    	{ cursor: hand; cursor:pointer; }
.commentTitle 	{ font-weight: bold; }
.commentBy    	{ font-weight: bold; }
.commentDate  	{ font-style: italic; font-size: smaller; }
.comment      	{ margin-left: 20px; width: 100%; word-wrap: break-word; }
.highlight	  	{ background-color: lightskyblue;}
.stripe			{ background-color: #eeeeee;}

ul									{ list-style: none;}
ul.dropdown                         { position: relative;padding-left:20px}
ul.dropdown li                      { font-weight: bold;}
ul.dropdown li a                    { display: block; padding: 4px 8px;color: #222; }
ul.dropdown ul 						{ visibility: hidden; position: absolute; top: 100%; left: -70px; }
<%if(approveAction.equals("true")){%>
ul.dropdown li:hover > ul 		    { visibility: visible;}
<%}%>
.actionli{background:white;border-top:solid 1px black;border-left:solid 1px black;border-right:solid 1px black; }
</STYLE>
<link type="text/css" rel="StyleSheet" id="cssEditor" href="/globalsight/envoy/edit/online3/editor.css">
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script>
var tempTotalPageNum = <%=(pi.getTotalPageNum())%>;
var g_disableLink = eval("<%=disableComment%>");
var urlResourcesPath = "<%=url_resources%>";
var g_reviewMode = eval("<%=state.isReviewMode()%>");
var marklable = "<%=marklable%>";
var unmarklable = "<%=unmarklable%>";
var url_segmentEditor = "<%=url_segmentEditor%>";
var url_commentEditor = "<%=url_commentEditor%>";
var url_self = "<%=url_self%>";
var url_refresh = "<%=url_refresh%>";
var url_search = "<%=url_search%>";
var url_termbases = "<%=url_termbases%>";
var url_options = "<%=url_options%>";
var url_pageInfo = "<%=url_pageInfo%>";
var isFirstPage = '<%=state.isFirstPage()%>';
var isLastPage = '<%=state.isLastPage()%>';
var isFirstBatch = '<%=state.isFirstBatch()%>';
var isLastBatch = '<%=state.isLastBatch()%>';
var g_isReviewActivity = eval("<%=b_isReviewActivity%>");
var g_readOnly = eval("<%=b_readOnly%>");
var currentIssuesSize = "<%=currentIssuesSize%>";
var closeeAllCommentWarning = "<%=closeeAllCommentWarning%>";
var g_disableComment = eval("<%=disableComment%>");
var helpFile = "<%=bundle.getString("help_main_editor2")%>";
var reviewModeText = "<%=WebAppConstants.REVIEW_MODE%>";
var approveAction = "<%=approveAction%>";
</script>
</HEAD>
<BODY id="idBody" oncontextmenu="contextForX(event)" onbeforeunload="exit()">
<FORM name="ShutdownForm" METHOD="POST" TARGET="idShutdown" ACTION="/globalsight/envoy/common/shutdownPopup.jsp">
<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=jobId%>">
<input type="hidden" name="<%=ReportConstants.TARGETLOCALE_LIST%>" value="<%=tgtIDS%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="">
<input type="hidden" name="withCompactTagsCCR"  value="on">
</FORM>
<FORM name="SaveForm" METHOD="POST" ACTION="<%=url_refresh%>&action=segment">
<INPUT TYPE="hidden" NAME="save" VALUE="">
<INPUT TYPE="hidden" NAME="refresh" VALUE="0">
<INPUT TYPE="hidden" NAME="tuId" VALUE="">
<INPUT TYPE="hidden" NAME="tuvId" VALUE="">
<INPUT TYPE="hidden" NAME="subId" VALUE="">
<INPUT TYPE="hidden" NAME="ptags" VALUE="">
</FORM>
<FORM name="CommentForm" METHOD="POST" action="<%=url_refresh%>&action=comment">
<input type="hidden" name="tuId"        value="">
<input type="hidden" name="tuvId"       value="">
<input type="hidden" name="subId"       value="">
<input type="hidden" name="cmtAction"   value="">
<input type="hidden" name="cmtTitle"    value="">
<input type="hidden" name="cmtComment"  value="">
<input type="hidden" name="cmtPriority" value="">
<input type="hidden" name="cmtStatus"   value="">
<input type="hidden" name="cmtCategory"   value="">
<input type="hidden" name="cmtShare"   value="">
<input type="hidden" name="cmtOverwrite"   value="">
</FORM>
<FORM name="SwitchTargetLocaleForm" METHOD="POST" ACTION="<%=url_refresh%>">
<INPUT TYPE="hidden" NAME="refresh" VALUE="0">
<INPUT type="hidden" NAME="trgViewLocale" VALUE="">
</FORM>

<DIV id="idSnippetLibrary" style="position: absolute; top: 0; left: 0; display: none;
  behavior: url('/globalsight/envoy/edit/snippets/SnippetLibrary.htc');"></DIV>

<div class="top">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" WIDTH="100%">
  <TR CLASS="tableHeadingBasic">
    <TD VALIGN="TOP">
      <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
		<TR CLASS="tableHeadingBasic">
		  <TD WIDTH="20">&nbsp;</TD>
		  <TD WIDTH="20">&nbsp;</TD>
		  <TD NOWRAP VALIGN="TOP" ALIGN="CENTER"><%=lb_fileNavigation%><BR>
		    <label id="fileNavPre"><%=lb_prevFile%></label>
		    <label id="fileNavNext"><%=lb_nextFile%></label>
		  </TD>
		  <TD WIDTH="20">&nbsp;</TD>
		  <TD NOWRAP VALIGN="TOP" ALIGN="CENTER"><%=lb_pageNavigation%>&nbsp;(<span id="currentPageNum"><%=pi.getCurrentPageNum()%></span> of <span id="totalPageNum"><%=pi.getTotalPageNum()%></span>)<BR/>
		    <label id="pageNavPre"><%=lb_prevPage%></label>
		    <label id="pageNavNext"><%=lb_nextPage%></label> 
		    <label style="position: relative; bottom: 6px; left: 8px; hight: 1px">Goto
				<input type="text" id="gotoPageNav"onkeypress="EnterPress(event)" style="height: 18px; width: 30px" value="" />
			</label>
		  </TD>
		  <TD WIDTH="20">&nbsp;</TD>
		  <TD NOWRAP ALIGN="CENTER"><%=filters.toString()%></TD>
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
		  <!-- Show/Unmark PTags -->
	      <% if (state.getNeedShowPTags()) {
				lable = bundle.getString("lb_unmark_pTag_segments");
	            action = WebAppConstants.PTAGS_ACTION_UNMARK;
	         } else {
	            lable = bundle.getString("lb_find_pTag_segments");
	            action = WebAppConstants.PTAGS_ACTION_FIND;
	         }
	      %>
		  <A href="#" onclick="showPtagsTest();" id="showPtags" CLASS="HREFBoldWhite" title="<%=lable%>"><%=lable%></A> |
		  <amb:permission name="<%=Permission.REPORTS_COMMENTS_ANALYSIS%>">
		  	<A href="#" onclick="createLisaQAReport(); return false;" CLASS="HREFBoldWhite" title="Create Comments Analysis Report"> Comments Analysis</A> |
	      </amb:permission>
	      <amb:permission name="<%=Permission.REPORTS_CHARACTER_COUNT%>">
	        <A href="#" onclick="createCharacterCountReport(); return false;" CLASS="HREFBoldWhite" title="Create Character Count Report"> Character Count</A> |
		  </amb:permission>
		    <% if (state.isReviewMode()) {
		         if (!state.getIsReviewActivity())
		         {
		    %>
		    <A href="#" onclick="reviewMode(); return false;" id="reviewMode"CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_editor_hide_segment_comments") %>"><%=bundle.getString("lb_editor_hide_comments") %></A> |
		    <% } } else { %>
		    <A href="#" onclick="reviewMode(); return false;" id="reviewMode"CLASS="HREFBoldWhite" title="<%=bundle.getString("lb_editor_show_segment_comments") %>"><%=bundle.getString("lb_editor_show_comments") %></A> |
		    <% } %>
		    <A href="#" onclick="showSupportFiles(); return false;" CLASS="HREFBoldWhite" title="<%=lb_showSupportFiles%>"><%=lb_supportFiles%></A> |
		    <A href="#" onclick="showTermbases(); return false;" CLASS="HREFBoldWhite" title="<%=lb_showTermbases%>"><%=lb_termbases%></A> |
		    <A HREF="#" onclick="showPageInfo(); return false;" CLASS="HREFBoldWhite"><%=lb_pageInfo%></A> |
		    <A HREF="#" onclick="closeWindow(); return false;" CLASS="HREFBoldWhite"><%=lb_close%></A> |
		    <A HREF="#" onclick="helpSwitch(); return false;" CLASS="HREFBoldWhite"><%=lb_help%></A>&nbsp;
		  </TD>
		</TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>
</div>
<div id="nav" class="nav" style="overflow:auto;">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="1"style="border-color: lightgrey; border-collapse: collapse; border-style: solid; border-width: 1px;font-family: Arial, Helvetica, sans-serif;font-size: 10pt;table-layout:fixed;">
  <THEAD>
    <TR CLASS="tableHeadingGray" style="height:19pt;">
      <TD ALIGN="CENTER" class="sourceTempClass"  width='5%'><%=lb_id%></TD>
      <TD ALIGN="LEFT"   width='30%'><%=lb_source_segment%></TD>
      <TD  id="previous_translation"   ALIGN="LEFT"   width='5%'><%=lb_previous_translation%></TD>
      <TD ALIGN="LEFT"   width='30%'><%=lb_current_translation%></TD>
      <%if(approveAction.equals("true")){%>
      <TD ALIGN="LEFT" width='5%'>
        <ul class="dropdown" style="padding: 0px;margin: 0px;" >
        	<li style="background:#708EB3;"><input type="checkbox" id="checkAll" style="float:left;margin-top:5px" onclick="checkAll()"/><a href="#" style="padding: 0px;"><img src="/globalsight/envoy/edit/online3/action.gif"></a>
        		<ul class="sub_menu">
        			 <li class="actionli"><a href="#" class="noUnderline" onclick="approve();">Approve</a></li>
        			 <li class="actionli"><a href="#" class="noUnderline" onclick="unapprove();">Unapprove</a></li>
        			 <li class="actionli" style="border-bottom:solid 1px black"><a href="#" class="noUnderline" onclick="revert();">Revert</a></li>
        		</ul>
        	</li>
        </ul>
      </TD>
      <%}%>
    </TR>
  </THEAD>
  <TBODY id="idPageHtml">
  </TBODY>
</TABLE>
</div>
<div class="foot">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
  <TR CLASS="tableHeadingBasic" style="height:30px">
    <TD width="50%"><%=lb_sourceLocale%>: <%=str_sourceLocale%></TD>
    <TD bgcolor="lightgrey" style="width:2px">&nbsp;</TD>
    <TD width="50%">&nbsp;&nbsp;<%=lb_targetLocale%>: <%=str_targetLocale.toString()%></TD>
  </TR>
</TABLE>
</div>
<div id="commentDiv" class="commentDiv" style="overflow:auto;<%=commentDisplay%>">
<DIV style="height:2px;"></DIV>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0"
  style="*padding-left: 3px; *padding-right: 3px;">
  <TR CLASS="tableHeadingBasic" style="height: 24px;">
    <TD><%=bundle.getString("lb_comments") %>:
      <!--<span class="clickable" onclick='filterComments()'>Filter...</span>-->
    </TD>
	<%if (enableCloseAllComments!= null && enableCloseAllComments.equals("1") && !isActive) {%>
    <TD ALIGN="RIGHT">
      <A id="idCloseAllComments" CLASS="HREFBoldWhite" HREF="#"
      onclick="closeAllComments(); return false;"
      onfocus="this.blur();"><%=bundle.getString("lb_comment_close_all") %></A>
    </TD>
	<%}%>
  </TR>
</TABLE>
<DIV style="height:2px;"></DIV>
<%if(view != null){ %>
<TABLE id="idCommentTable" WIDTH="100%" CELLSPACING="0" CELLPADDING="5px" BORDER="0"
  style="*padding-left: 3px; *padding-right: 3px; behavior: url(/globalsight/includes/rowover.htc); ro--selected-color: black; ro--selected-background: lightskyblue; "
  selectable="true" striped="true" selection="true"
  onrowselect="selectComment()" onrowdblclick="editComment()">
  <COL WIDTH="2%"  VALIGN="TOP" ALIGN="LEFT" class="editorText">
  <COL WIDTH="2%"  VALIGN="TOP" ALIGN="LEFT" class="editorText">
  <COL WIDTH="80%" VALIGN="TOP" ALIGN="LEFT" class="editorText">
  <COL WIDTH="2%"  VALIGN="TOP" ALIGN="LEFT" class="editorText" NOWRAP>
  <COL WIDTH="80px"  VALIGN="TOP" ALIGN="CENTER" class="editorText" NOWRAP>
  <COL WIDTH="70px"  VALIGN="TOP" ALIGN="CENTER" class="editorText" NOWRAP>
  <COL WIDTH="80px"  VALIGN="TOP" ALIGN="CENTER" class="editorText" NOWRAP>
  <THEAD unselectable="on">
    <TR>
      <TH ALIGN="CENTER" NOWRAP>
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_ID)%>'); return false;"><%=bundle.getString("lb_id") %> <%=getSortIcon(view, COL_ID)%></span>
      </TH>
      <TH NOWRAP ALIGN="LEFT">
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_USER)%>'); return false;"><%=bundle.getString("lb_user") %> <%=getSortIcon(view, COL_USER)%></span>
      </TH>
      <TH NOWRAP ALIGN="LEFT">
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_TITLE)%>'); return false;"><%=bundle.getString("lb_comment") %> <%=getSortIcon(view, COL_TITLE)%></span>
      </TH>
      <TH NOWRAP ALIGN="LEFT">
		&nbsp;
      </TH>
      <TH ALIGN="CENTER" NOWRAP>
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_DATE)%>'); return false;"><%=bundle.getString("lb_date") %> <%=getSortIcon(view, COL_DATE)%></span>
      </TH>
      <TH ALIGN="CENTER" NOWRAP>
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_STATUS)%>'); return false;"><%=bundle.getString("lb_status") %> <%=getSortIcon(view, COL_STATUS)%></span>
      </TH>
      <TH ALIGN="CENTER" NOWRAP>
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_PRIO)%>'); return false;"><%=bundle.getString("lb_priority") %> <%=getSortIcon(view, COL_PRIO)%></span>
      </TH>
      <TH ALIGN="CENTER" NOWRAP>
		<span unselectable="on" onclick="sortComments('<%=getSortCondition(view, COL_CATEGORY)%>'); return false;"><%=bundle.getString("lb_category") %> <%=getSortIcon(view, COL_CATEGORY)%></span>
      </TH>
    </TR>
  </THEAD>

  <TBODY id="idComments">
<%  ArrayList issues = view.getIssues();
    for (int i = 0, maxi = issues.size(); i < maxi; i++)
    {
      Issue issue = (Issue)issues.get(i);

      // Cell is align=right if the target language is bidi _or_ the
      // issue's title is bidi (assuming that the entire issue is a 
      // bidi conversation).
      // Title and comments are RTL if their text is bidi.
      // Comments will be aligned right if their comment is bidi, or the
      // entire issue was flagged as bidi.
      // The rest is just LTR in an align=right or left context.
      boolean b_titleRtl = Text.containsBidiChar(issue.getTitle());
      boolean b_issueRtl = b_rtl || b_titleRtl;
      String titleDir = b_titleRtl ? "RTL" : "LTR";
      String issueAlign = b_issueRtl ? "right" : "left";
    
      String logKey = issue.getLogicalKey();
      // strip off leading targetpage id
      String key = logKey.substring(logKey.indexOf('_') + 1);
%>
  <TR key="<%=key%>" commentId="<%=issue.getId()%>">
    <TD><%=key.substring(0, key.indexOf('_'))%></TD>
    <TD><%=UserUtil.getUserNameById(issue.getCreatorId())%></TD>
    <TD align="<%=issueAlign%>">
      <DIV class="commentTitle" dir="<%=titleDir%>">
        <%=EditUtil.encodeHtmlEntities(issue.getTitle())%>
      </DIV>
<%
      List histories = issue.getHistory();
      for (int j = 0, maxj = histories.size(); j < maxj; j++)
      {
       IssueHistory history = (IssueHistory)histories.get(j);
       boolean b_commentRtl = Text.containsBidiChar(history.getComment());
       String commentAlign = b_commentRtl || b_issueRtl ? "right" : "left";
       String commentDir = b_commentRtl ? "RTL" : "LTR";
%>
      <DIV style="width:100%" align="<%=commentAlign%>">
       	<SPAN class="commentBy">
        	<%=EditUtil.encodeHtmlEntities(UserUtil.getUserNameById(history.reportedBy()))%>
       	</SPAN>
      	<SPAN class="commentDate"><%=history.dateReported()%></SPAN>
      	<DIV class="comment" dir="<%=commentDir%>">
        	<%=EditUtil.encodeHtmlEntities(history.getComment())%>
      	</DIV>
      </DIV>
<%
     }
%>
    </TD>
    <TD>&nbsp;</TD>
    <TD nowrap><%=issue.getCreatedDate()%></TD>
    <TD><SPAN class="<%=issue.getStatus()%>">
      <%=getStatusLabel(bundle, issue)%></SPAN>      
    </TD>
    <TD><%=getPriorityLabel(bundle, issue)%></TD>
    <TD><%=getCategoryLabel(bundle, issue)%></TD>
  </TR>
<% } %>
  </TBODY>
<%}%>
</TABLE>
</div>
</BODY>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/envoy/edit/online3/main.js"></script>
<script src="/globalsight/includes/ContextMenu.js"></script>
<script>
ContextMenu.intializeContextMenu();

var g_lastTuId  = "<%=lastTuId%>";
var g_lastTuvId = "<%=lastTuvId%>";
var g_lastSubId = "<%=lastSubId%>";
HighlightSegment(g_lastTuId, g_lastTuvId, g_lastSubId);
var updatePopupEditorFlag = "<%=state.getNeedUpdatePopUpEditor()%>";
if (updatePopupEditorFlag != null && updatePopupEditorFlag != "null"
    && g_lastTuId != null && g_lastTuId != "0"
    && g_lastTuvId != null && g_lastTuvId != "0" )
{
    <%state.setNeedUpdatePopUpEditor(null);%>
    editSegment(g_lastTuId, g_lastTuvId, g_lastSubId);
}
</script>
</HTML>
