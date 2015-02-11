<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.GlobalSightLocale,
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
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            java.util.*"
    session="true"
%>
<jsp:useBean id="skin" scope="request"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
EditorState.Layout layout = state.getLayout();
PageInfo pageInfo = state.getPageInfo();

long pageId = state.getTargetPageId().longValue();

// LABELS
String lb_targetLocale = bundle.getString("lb_target_locale");
%>
<HTML>
<HEAD>
<STYLE>
BODY { behavior: url(#default#userdata); }
.editorId { font-weight: bold; }
.clickable { cursor: hand; cursor:pointer; }
.comment {}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
function findTargetFrame()
{
    return parent.content;
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

function Comment(id, tuId, comment)
{
    this.id = id;
    this.tuId = tuId;
    this.comment = comment;
}

Comment.prototype.toString = function()
{
    return this.id + "\u0001" + this.tuId + "\u0001" + this.comment;
}

Comment.prototype.parseComment = function(s)
{
    var parts = s.split("\u0001");
    this.id = parts[0];
    this.tuId = parts[1];
    this.comment = parts[2];
}

function tableToComments(res)
{
    var rows = idComments.rows;

    for (var i = 0; i < rows.length; i++)
    {
        var row = rows.item(i);
        var cells = row.cells;

        var id = row.id;
        var tuId = cells.item(0).innerText;
        var comment = cells.item(2).firstChild.innerHTML;

        res.push(new Comment(id, tuId, comment));
    }
}

function commentsToTable(comments)
{
    var tbody = idComments;

    for (var i = tbody.rows.length; i > 0; --i)
    {
        tbody.deleteRow(i-1);
    }

    for (var i = 0; i < comments.length; i++)
    {
        var comment = comments[i];

        addCommentToTable(comment);
    }
}

function addCommentToTable(comment)
{
    var tbody = idComments;
    var row, cell;

    row = tbody.insertRow();
    row.setAttribute("id", comment.id);

    cell = row.insertCell();
    cell.innerText = comment.tuId;
    cell.onclick = highlight;

    cell = row.insertCell();
    cell.innerText = '\u00a0\u00a0';

    cell = row.insertCell();
    cell.innerHTML =
        '<DIV class="comment" contenteditable="true">' +  comment.comment + '</DIV>';
}

function getComments()
{
    var comments = new Array();

    tableToComments(comments);

    var res = "";
    for (var i = 0; i < comments.length; i++)
    {
      var comment = comments[i];

      res = res + "\u0002" + comment.toString();
    }

    return res;
}

function addComment()
{
}

function addComment1()
{
    var tuId = prompt("Enter segment ID: ", "");
    if (!tuId) return;

    var id = findId();
    // if ID not found, return;

    var comment = prompt("Enter comment: ", "");
    if (!comment) return;

    var c = new Comment(id, tuId, comment);
    addCommentToTable(c);
}

function removeComment()
{
}

function saveComments()
{
    var temp = getComments();

    idBody.setAttribute("comments", temp);
    idBody.save("comments_<%=pageId%>");
}

function loadComments()
{
    //alert("Loading comments for page <%=pageId%>");
    idBody.load("comments_<%=pageId%>");

    var temp = idBody.getAttribute("comments");

    if (temp)
    {
        var comments = new Array();

        var parts = temp.split("\u0002");
        for (var i = 1; i < parts.length; i++)
        {
            var part = parts[i];

            var c = new Comment();
            c.parseComment(part);

            comments.push(c);
        }

        commentsToTable(comments);
    }
}

function highlight()
{
    var o = event.srcElement;

    while (o.nodeName != 'TR')
    {
        o = o.parentElement;
    }

    var id = o.id;
    var parts = id.split("_");

    var tuId  = parts[0];
    var tuvId = parts[1];
    var subId = parts[2];

    parent.content.HighlightSegment(tuId, tuvId, subId);
}

function doOnload()
{
    //loadComments();
}

function doOnUnload()
{
    saveComments();
}
</SCRIPT>
</HEAD>

<BODY id="idBody" BGCOLOR="lightgrey" onload="doOnload()" onbeforeunload="doOnUnload()">
<DIV ID="main" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; LEFT: 0px; RIGHT: 0px; TOP: 0px;">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0"
  style="padding-left: 3px; padding-right: 3px;">
  <TR CLASS="tableHeadingBasic" style="height: 24px;">
    <TD>Comments &nbsp;&nbsp;&nbsp;
      <span class="clickable" onclick='addComment()'>Add</span> |
      <span class="clickable" onclick='removeComment()'>Delete</span>
    </TD>
    <TD align="right">
      <span class="clickable">Close</span>&nbsp;
    </TD>
  </TR>
</TABLE>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0"
  style="padding-left: 3px; padding-right: 3px;">
  <COL WIDTH="1%"  VALIGN="TOP" ALIGN="right" class="editorId" NOWRAP>
  <COL WIDTH="20"  VALIGN="TOP" ALIGN="CENTER" NOWRAP>
  <COL WIDTH="99%" VALIGN="TOP" ALIGN="LEFT" class="editorText">

  <TBODY id="idComments">
  <TR id="2531_7049_0">
    <TD onclick="highlight()">2531</TD>
    <TD>&nbsp;&nbsp;</TD>
    <TD>
      <DIV class="comment" contenteditable="true">I wouldn't call it "Bandwidth"</DIV>
    </TD>
  </TR>
  <TR id="2532_7118_0">
    <TD onclick="highlight()">2532</TD>
    <TD>&nbsp;&nbsp;</TD>
    <TD>
      <DIV class="comment" contenteditable="true">This is just technical mumbo jumbo.</DIV>
    </TD>
  </TR>
  </TBODY>
</TABLE>
</DIV>
</BODY>
</HTML>
