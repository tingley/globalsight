<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.comment.Comment,
         com.globalsight.everest.comment.Issue,
         com.globalsight.everest.comment.IssueHistory,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.tags.TableConstants,
         com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
         com.globalsight.everest.webapp.pagehandler.administration.comment.IssueComparator,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsComparator,
         com.globalsight.everest.webapp.pagehandler.administration.comment.PageCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="comments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="segmentComments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="segmentCommentList" scope="request"
 class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String editorReviewUrl = editor.getPageURL() +
       "&" + WebAppConstants.REVIEW_MODE + "=true";

    String title= bundle.getString("lb_comments");


    String labelJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");
    String userName = bundle.getString("lb_user_name");
    String date = bundle.getString("lb_date");

    // For task comments table
    ArrayList statusList = (ArrayList)sessionMgr.getAttribute("statusList");
    String selectedStatus = (String)request.getAttribute("segmentSelectedStatus");
    String pageName = (String)sessionMgr.getAttribute(WebAppConstants.TARGET_PAGE_NAME);
    String sourcePageId = (String)sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID);
    String targetPageId = (String)sessionMgr.getAttribute(WebAppConstants.TARGET_PAGE_ID);
    String jobId = (String)sessionMgr.getAttribute(WebAppConstants.JOB_ID);
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<style>
.comment {
  position: absolute;
  visibility: hidden;
  width: 600px;
  background-color: lightgrey;
  layer-background-color: lightgrey;
  border: 2px outset white;
}
</style>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var guideNode = "myJobs";
var objectName = "";
var helpFile = "<%=bundle.getString("help_customer_view_comments")%>";

// Callback for segment editor.
function RefreshComments()
{
    //window.location.reload();
    window.location = "<%=segmentComments.getPageURL()%>" + "&setFilter=" +
         CommentForm.showSelect.options[CommentForm.showSelect.selectedIndex].value;
}

function submitForm()
{
    CommentForm.action = "<%=comments.getPageURL()%>" + "&action=back";
    CommentForm.submit();
}

function showComment(id)
{
    elem = document.getElementById(id);
    elem.style.visibility = "visible";
}

function closeComment(id)
{
    elem = document.getElementById(id);
    elem.style.visibility = "hidden";
}

var lastSelected; 
function updateSegmentTable()
{
    var value = CommentForm.showSelect.options[CommentForm.showSelect.selectedIndex].value;
    document.getElementById(value).style.display = "block";

    lastSelected.style.display = "none";
    lastSelected = document.getElementById(value);
}

function initSelection()
{
<% if (selectedStatus == null) { %>
    lastSelected = document.getElementById("allStatus");
<% } else { %>
    lastSelected = document.getElementById(CommentForm.showSelect.options[CommentForm.showSelect.selectedIndex].value);
<% } %>
}

var w_editor = null;
function cancelEvent()
{
    if (window.event != null)
    {
        window.event.returnValue = false;
        window.event.cancelBubble = true;
    }
}

function openReviewEditor(key)
{
    if (!canClose())
    {
        cancelEvent();
        raiseSegmentEditor();
    }
    else
    {
        var ids = key.split("_");
        var url = "&<%=WebAppConstants.SOURCE_PAGE_ID%>=<%=sourcePageId%>" +
                  "&<%=WebAppConstants.TARGET_PAGE_ID%>=<%=targetPageId%>" +
                  "&<%=WebAppConstants.JOB_ID%>=<%=jobId%>" +
                  "&curTuId=" + ids[1] +
                  "&curTuvId=" + ids[2] +
                  "&curSubId=" + ids[3];
        w_editor = window.open("<%=editorReviewUrl%>" + url, 'MainEditor',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

function canClose()
{
    if (w_editor != null && !w_editor.closed)
    {
        if (!w_editor.CanClose())
        {
            return false;
        }
    }

    return true;
}

function raiseSegmentEditor()
{
    if (w_editor != null && !w_editor.closed)
    {
        w_editor.RaiseSegmentEditor();
    }
}

function doUnload()
{
    if (w_editor != null && !w_editor.closed)
    {
        w_editor.close();
    }
    w_editor = null;
}
</SCRIPT>
</HEAD>
<body LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="loadGuides(); initSelection();" onunload="doUnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <SPAN CLASS="mainHeading">
    <%=bundle.getString("lb_segment")%> <%=bundle.getString("lb_comments")%> for <%=pageName%>
    </SPAN>

<p>

<form name="CommentForm" method="post">
<!-- Comments data table -->
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr>
            <td style="padding-bottom: 3px"><%=bundle.getString("lb_show")%>:
            <select name=showSelect onchange="updateSegmentTable()">
            <%
                out.print("<option value=allStatus");
                if ("allStatus".equals(selectedStatus))
                    out.print(" selected ");
                out.println(">" + bundle.getString("lb_all") + "</option>");
                out.print("<option value=" + Issue.STATUS_OPEN);
                if (Issue.STATUS_OPEN.equals(selectedStatus))
                    out.print(" selected ");
                out.println(">" + bundle.getString("lb_open") + "</option>");
                out.print("<option value=" + Issue.STATUS_CLOSED);
                if (Issue.STATUS_CLOSED.equals(selectedStatus))
                    out.print(" selected ");
                out.println(">" + bundle.getString("lb_closed") + "</option>");
            %>
            </select>
        </tr>
        <tr>
        <% if (selectedStatus == null) { %>
        <td id="allStatus" style="display:block">
        <% } else { %>
        <td id="allStatus" style="display:none">
        <% } %>
          <amb:table bean="segmentCommentList" id="issue"
             key="<%=CommentConstants.SEGMENT_COMMENT_KEY%>"
             dataClass="com.globalsight.everest.comment.Issue"
             pageUrl="segmentComments"
             emptyTableMsg="msg_comments_none_for_page" >
            <amb:column label="lb_status" sortBy="<%=IssueComparator.STATE%>">
                <%=issue.getStatus()%>
            </amb:column>
            <amb:column label="lb_priority" sortBy="<%=IssueComparator.PRIORITY%>">
                <%=issue.getPriority()%>
            </amb:column>
            <amb:column label="lb_comments" sortBy="<%=IssueComparator.TITLE%>">
                <b><%=EditUtil.encodeHtmlEntities(issue.getTitle())%></b><br>
                <%
                 List histories = issue.getHistory();
                 for (int i = 0; i < histories.size(); i++)
                 {
                    if (i != 0)
	            {
                        out.println("<hr>");
	            }

                    IssueHistory history = (IssueHistory)histories.get(i);
                    out.println(userName + ": " +
	              EditUtil.encodeHtmlEntities(history.reportedBy()) + "<br>");
                    out.println(date + ": " + history.dateReported() + "<br>");

                    String com = history.getComment();
                    if (com.length() > 250)
                    {
                        int idx = com.indexOf(' ', 250);
                        if (idx > 0)
                            com = com.substring(0, idx);
                        out.println(EditUtil.encodeHtmlEntities(com));
                        out.println("<div onclick=\"showComment('allt" + history.getId() + "');\" style=\"cursor:hand; color: blue;\">[more...]</div>");
                        out.println("<div id=allt" + history.getId() + " class=\"comment\">" + EditUtil.encodeHtmlEntities(history.getComment()) + "<div onclick=closeComment('allt" + history.getId() + "');><span style=\"cursor: hand; color:blue\">[Close]</span></div></div>");
                    }
                    else
                    {
                        out.println(EditUtil.encodeHtmlEntities(history.getComment()));
                    }
                 }
                %>
            </amb:column>
            <amb:column label="lb_view_page" align="center">
                <a href="#" onclick="openReviewEditor('<%=issue.getLogicalKey()%>'); return false;" onfocus="blur()">
                    <img src=/globalsight/images/file.gif border=0>
                </a>
            </amb:column>
          </amb:table>
<% for (int i=0; i < statusList.size(); i++) {
        String status = (String)statusList.get(i);
        if (status.equals(selectedStatus))
            out.println("<td id=\"" + status  + "\" style=\"display:block\">");
        else
            out.println("<td id=\"" + status + "\" style=\"display:none\">");
%>
          <amb:table bean="segmentCommentList" id="issue"
             key="<%=CommentConstants.SEGMENT_COMMENT_KEY%>"
             dataClass="com.globalsight.everest.comment.Issue"
             filterMethod="getStatus"
             filterData="<%=status%>"
             filterSelection="<%=status%>"
             pageUrl="segmentComments"
             emptyTableMsg="msg_comments_none_for_page" >
            <amb:column label="lb_status" sortBy="<%=IssueComparator.STATE%>">
                <%=issue.getStatus()%>
            </amb:column>
            <amb:column label="lb_priority" sortBy="<%=IssueComparator.PRIORITY%>">
                <%=issue.getPriority()%>
            </amb:column>
            <amb:column label="lb_comments" sortBy="<%=IssueComparator.TITLE%>">
                <b><%=EditUtil.encodeHtmlEntities(issue.getTitle())%></b><br>
                <%
                 List histories = issue.getHistory();
                 for (int j = 0; j < histories.size(); j++)
                 {
                    if (j != 0)
                        out.println("<hr>");

                    IssueHistory history = (IssueHistory)histories.get(j);
                    out.println(userName + ": " +
	              EditUtil.encodeHtmlEntities(history.reportedBy()) + "<br>");
                    out.println(date + ": " + history.dateReported() + "<br>");

                    String com = history.getComment();
                    if (com.length() > 250)
                    {
                        int idx = com.indexOf(' ', 250);
                        if (idx > 0)
                            com = com.substring(0, idx);
                        out.println(EditUtil.encodeHtmlEntities(com));
                        out.println("<div onclick=\"showComment('" + i + "t" + history.getId() + "');\" style=\"cursor:hand; color: blue;\">[more...]</div>");
                        out.println("<div id=" + i + "t" + history.getId() + " class=\"comment\">" + EditUtil.encodeHtmlEntities(history.getComment()) + "<div onclick=closeComment('" + i + "t" + history.getId() + "');><span style=\"cursor: hand; color:blue\">[Close]</span></div></div>");
                    }
                    else
                        out.println(EditUtil.encodeHtmlEntities(history.getComment()));

                 }
                %>
            </amb:column>
            <amb:column label="lb_view_page" align="center">
                <a href="#" onclick="openReviewEditor('<%=issue.getLogicalKey()%>'); return false;"
                 onfocus="blur()">
                    <img src=/globalsight/images/file.gif border=0>
                </a>
            </amb:column>
          </amb:table>
        </td>
<% } %>

    </tr>
    <tr>
        <td style="padding-top:5px">
            <input type="button" value="<%=bundle.getString("lb_ok")%>"
        onclick="submitForm();">
        </td>
    </tr>
</table>
<br>
</form>
</div>
</body>
</html>
