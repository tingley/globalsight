<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.ling.common.Text,
            com.globalsight.everest.comment.Issue,
            com.globalsight.everest.comment.IssueHistory,
            com.globalsight.everest.edit.online.CommentView,
            com.globalsight.everest.edit.online.CommentThreadView,
            com.globalsight.everest.edit.online.PageInfo,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.page.ExtractedFile,
            com.globalsight.everest.page.TargetPage,
            com.globalsight.everest.page.pageexport.ExportConstants,
            com.globalsight.everest.persistence.PersistentObject,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.workflowmanager.Workflow,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.config.UserParamNames,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="commentEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="closeComment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="skin" scope="request"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
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
%><%
ResourceBundle bundle = PageHandler.getBundle(session);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
CommentThreadView view = state.getCommentThreads();
String taskStatus = (String)sessionMgr.getAttribute("taskStatus");
Job job;
    try
    {
        if (taskStatus == "")
        {
            long jobId = (Long)sessionMgr.getAttribute(WebAppConstants.JOB_ID);
            job = ServerProxy.getJobHandler().getJobById(jobId);
            for (Workflow wf : job.getWorkflows())
            {
                if (state.getTargetLocale().getDisplayName()
                        .equals(wf.getTargetLocale().getDisplayName()))
                {
                    Collection tasks = ServerProxy.getTaskManager().getCurrentTasks(wf.getId());
                    if (tasks != null)
                    {
                        for (Iterator it = tasks.iterator(); it.hasNext();)
                        {
                            Task task = (Task) it.next();
                            taskStatus = String.valueOf(task.getState());
                        }
                    }
                }
            }
        }
    }
    catch (Exception e)
    {
    }
boolean isActive = false;
if ((taskStatus != "") && (Integer.valueOf(taskStatus).intValue() == Task.STATE_ACTIVE))
   isActive = true;
// keep all issues for set all status to closed
ArrayList currentIssues = view.getIssues();
int currentIssuesSize = currentIssues.size();
session.setAttribute("currentIssues",currentIssues);
String enableCloseAllComments = PageHandler.getUserParameter(
                session, UserParamNames.EDITOR_SHOW_CLOSEALLCOMMENT).getValue();

PageInfo pageInfo = state.getPageInfo();

long pageId = state.getTargetPageId().longValue();

String url_self = self.getPageURL();
String url_commentEditor = commentEditor.getPageURL();
String url_closeComment = closeComment.getPageURL();

boolean b_refreshOther = (request.getAttribute("cmtRefreshOtherPane") != null);
boolean b_rtl = EditUtil.isRTLLocale(state.getTargetLocale());

Boolean assigneeValue = (Boolean)TaskHelper.retrieveObject(
   session, WebAppConstants.IS_ASSIGNEE);
boolean isAssignee = assigneeValue == null ? true :
   assigneeValue.booleanValue();
boolean disableComment = isAssignee && state.isReadOnly();

%>
<HTML>
<!-- This is envoy\edit\online\me_comments.jsp -->
<HEAD>
<STYLE>
BODY { behavior: url(#default#userdata); }
#idCommentTable { font-family: verdana; font-size: 10pt;
                  border-bottom: 1px solid black;
                }
#idCommentTable TH {
    font-size: 9pt;
    color: <%=skin.getProperty("skin.tableHeading.fgColor")%>;
    background-color: <%=skin.getProperty("skin.tableHeading.bgColor")%>;
}
#idCommentTable TH SPAN { cursor: hand;  cursor:pointer;}

#idCommentTable P { margin-top: 0px; margin-bottom: 0px; }
.editorId   { font-weight: bold; }

/* Background colors for issue status, class name is the status token. */
.open   { background-color: red !important; }
.closed { background-color: lawngreen !important; }

.clickable    	{ cursor: hand; cursor:pointer; }
.commentTitle 	{ font-weight: bold; }
.commentBy    	{ font-weight: bold; }
.commentDate  	{ font-style: italic; font-size: smaller; }
.comment      	{ margin-left: 20px; width: 100%; word-wrap: break-word; }
.highlight	  	{ background-color: lightskyblue;}
.stripe			{ background-color: #eeeeee;}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT>
var g_currentRow = 0;
var w_cmtEditor = null;
var g_disableComment = eval("<%=disableComment%>");

if (eval("<%=b_refreshOther%>"))
{
    try { parent.RefreshTargetPane(); } catch (ignore) {}
}

function cancelEvent()
{
    if (window.event != null)
    {
        window.event.returnValue = false;
        window.event.cancelBubble = true;
    }

    return false;
}

function CanClose()
{
    return CanCloseCommentEditor();
}

function CanCloseSegmentEditor()
{
    return parent.CanCloseTarget();
}

function CanCloseCommentEditor()
{
    if (w_cmtEditor != null && !w_cmtEditor.closed)
    {
        if(w_cmtEditor.CanClose) {
            if (!w_cmtEditor.CanClose())
            {
                return false;
            }
        } 
    }

    return true;
}

function ForceClose()
{
    if (w_cmtEditor != null && !w_cmtEditor.closed)
    {
        try { w_cmtEditor.close(); } catch (ignore) {}
    }
}

function ForceCloseSegmentEditor()
{
    parent.ForceCloseEditor();
}

function RaiseCommentEditor()
{
    if (w_cmtEditor != null && !w_cmtEditor.RaiseEditor)
    {
        w_cmtEditor.RaiseEditor();
    }
}

function RaiseSegmentEditor()
{
    parent.RaiseSegmentEditor();
}

function doEditComment(key, commentId)
{
    if (!CanCloseSegmentEditor())
    {
        cancelEvent();
        RaiseSegmentEditor();
    }
    else if (!CanCloseCommentEditor())
    {
        cancelEvent();
        RaiseCommentEditor();
    }
    else
    {
        var parts = key.split("_");

        var tuId  = parts[0];
        var tuvId = parts[1];
        var subId = parts[2];

        var str_url = "<%=url_commentEditor%>" +
          "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId +
          "&commentId=" + commentId + "&refresh=0";

        ForceCloseSegmentEditor();

        w_cmtEditor = window.open(str_url, "CommentEditor",
          "width=700,height=650,top=100,left=100");
    }
}

function highlightComment(id)
{
    var parts = id.split("_");

    var tuId  = parts[0];
    var tuvId = parts[1];
    var subId = parts[2];

    parent.HighlightSegment(tuId, tuvId, subId);
}

function unhighlightComment(id)
{
    var parts = id.split("_");

    var tuId  = parts[0];
    var tuvId = parts[1];
    var subId = parts[2];

    parent.UnhighlightSegment(tuId, tuvId, subId);
}

function selectComment()
{
  g_currentRow = event.srcRow;
  if (g_currentRow.rowIndex > 0)
  {
    highlightComment(g_currentRow.key);
  }
}

function unselectComment()
{
  if (g_currentRow.rowIndex > 0)
  {
    unhighlightComment(g_currentRow.key);
  }

  g_currentRow = 0;
}

function editComment()
{
  // dblclick is funny
  g_currentRow = event.srcRow;
  // if the user is assignee and has not accepted, don't open the comment editor
  if ((g_currentRow && g_currentRow.rowIndex > 0) && !g_disableComment)
  {
    highlightComment(g_currentRow.key);
    doEditComment(g_currentRow.key, g_currentRow.commentId);
  }
}

function sortComments(arg)
{
  var url = "<%=url_self%>&sortComments=" + arg;

  document.location = url;
}

function SaveComment2(tuId, tuvId, subId, action, title, comment, priority, status, category, share, overwrite)
{
	var o_form = document.CommentForm;

    o_form.tuId.value = tuId;
    o_form.tuvId.value = tuvId;
    o_form.subId.value = subId;
    o_form.cmtAction.value = action;
    o_form.cmtTitle.value = title;
    o_form.cmtComment.value = comment;
    o_form.cmtPriority.value = priority;
    o_form.cmtStatus.value = status;
    o_form.cmtCategory.value = category;
    o_form.cmtShare.value = share;
    o_form.cmtOverwrite.value = overwrite;
var main=parent.parent.parent.parent;
   main.localData=null;
    o_form.submit();
}

function SaveComment(tuId, tuvId, subId, action, title, comment, priority, status, category)
{
    var o_form = document.CommentForm;
    
    o_form.tuId.value = tuId;
    o_form.tuvId.value = tuvId;
    o_form.subId.value = subId;
    o_form.cmtAction.value = action;
    o_form.cmtTitle.value = title;
    o_form.cmtComment.value = comment;
    o_form.cmtPriority.value = priority;
    o_form.cmtStatus.value = status;
    o_form.cmtCategory.value = category;
    o_form.cmtShare.value = "";
    o_form.cmtOverwrite.value = "";

    o_form.submit();
}

function doOnload()
{
    // Show pumpkins on Halloween and snow on Christmas.
}

function doOnUnload()
{
    if (w_cmtEditor != null && !w_cmtEditor.closed)
    {
        try { w_cmtEditor.close(); } catch (ignore) {}
    }
}

function closeAllComments()
{
    if (<%=currentIssuesSize%> > 0)
    {
    	if(eval("<%=isActive%>"))
    	{
    		alert("Cannot close all comments.");
    		return;
    	}
        var msg = "Do you want to close all comments?";
        if (confirm(msg))
        {
            CommentForm.cmtAction.value = "closeAllComments";
            CommentForm.submit();
        }
    }
}

// Added for firefox issue: comment edit and highlight
$(document).ready(function(){
	$("#idComments tr:nth-child(even)").addClass("stripe");			//comment stripe
	
	$("#idComments tr").dblclick(function(){						//double click
		var key = $(this).attr("key");
		var commentId = $(this).attr("commentId");
		
		if(!g_disableComment){
			$("#idComments tr:nth-child(even)").addClass("stripe");
			$("#idComments tr").removeClass("highlight");			
			$(this).removeClass("stripe").addClass("highlight");	//comment highlight
			doEditComment(key, commentId);							//comment edit
		}
	});

	$("#idComments tr").click(function(){							//click
		$("#idComments tr:nth-child(even)").addClass("stripe");
		$("#idComments tr").removeClass("highlight");				
		$(this).removeClass("stripe").addClass("highlight");		//comment highlight
	});	
});
</SCRIPT>
</HEAD>

<%-- The cursor style visualizes that the comment frame can be resized. --%>
<BODY id="idBody" onload="doOnload()" onbeforeunload="doOnUnload()">
<DIV style="position:absolute; top:0; left:0; width:100%; height:0px; background-color: lightgrey; cursor: N-resize;" unselectable="on"></DIV>

<DIV style="position:absolute; top:0; left:0; width:100%;">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0"
  style="*padding-left: 3px; *padding-right: 3px;">
  <TR CLASS="tableHeadingBasic" style="height: 24px;">
    <TD><%=bundle.getString("lb_comments") %>:
      <!--<span class="clickable" onclick='filterComments()'>Filter...</span>-->
    </TD>
<%
    if (enableCloseAllComments!= null && enableCloseAllComments.equals("1")) {
%>
    <TD ALIGN="RIGHT">
      <A id="idCloseAllComments" CLASS="HREFBoldWhite" HREF="#"
      onclick="closeAllComments(); return false;"
      onfocus="this.blur();"><%=bundle.getString("lb_comment_close_all") %></A>
    </TD>
<%  
    }
%>
  </TR>
</TABLE>
</DIV>

<DIV style="position:absolute; top:28; left:0; width:100%;
 height: (idBody.clientHeight - 28);">
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
</TABLE>
</DIV>

<FORM name="CommentForm" METHOD="POST" action="<%=url_self%>">
<input type="hidden" name="tuId"        value="">
<input type="hidden" name="tuvId"       value="">
<input type="hidden" name="subId"       value="">
<input type="hidden" name="cmtAction"   value="">
<input type="hidden" name="cmtTitle"    value="">
<input type="hidden" name="cmtComment"  value="">
<input type="hidden" name="cmtPriority" value="">
<input type="hidden" name="cmtStatus"   value="">
<input type="hidden" name="cmtCategory" value="">
<input type="hidden" name="cmtShare"   value="">
<input type="hidden" name="cmtOverwrite"   value="">
</FORM>

</BODY>
</HTML>
