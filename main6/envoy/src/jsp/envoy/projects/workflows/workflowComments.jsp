<%@ page 
        errorPage="/envoy/common/error.jsp"
        contentType="text/html; charset=UTF-8"
        import="java.util.*,java.util.ResourceBundle,
                java.util.List,
                java.util.ArrayList,
                java.util.Locale,
                java.util.Collection,
                java.util.Hashtable, 
                java.util.Iterator,
                java.util.TreeMap, 
                com.globalsight.everest.permission.Permission,
                com.globalsight.everest.permission.PermissionSet,                
                com.globalsight.everest.servlet.util.ServerProxy,
                com.globalsight.everest.comment.CommentManager,
                com.globalsight.everest.comment.CommentFile,
                com.globalsight.everest.comment.CommentUpload,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
                com.globalsight.everest.webapp.pagehandler.administration.comment.CommentState,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowCommentsHandler,
                com.globalsight.everest.workflow.WorkflowTaskInstance,
                com.globalsight.everest.comment.Comment,
                com.globalsight.everest.workflowmanager.Workflow,
                com.globalsight.util.edit.EditUtil,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.util.AmbFileStoragePathUtils,
                com.globalsight.util.SortUtil,
                com.globalsight.everest.taskmanager.Task"
        session="true" 
%>

<% 
    final String WHITE_BG = "#FFFFFF";
    final String LT_GREY_BG = "#EEEEEE";
    ResourceBundle bundle = PageHandler.getBundle(session);
    PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

    //Labels of the page
    String labelComments = bundle.getString("lb_comments");
    String labelActivity = bundle.getString("lb_activity");
    String sortComments =
        (String)session.getAttribute(SystemConfigParamNames.COMMENTS_SORTING);
    String title = labelComments;
    Vector tasks = (Vector)request.getAttribute(
                  WorkflowCommentsHandler.TASKS_ATTRIBUTE);
    Vector actNames = (Vector)request.getAttribute(
              WorkflowCommentsHandler.WORKFLOW_TASKS_ATTRIBUTE);

    List comments = null;
    Comment aComment = null;
    StringBuffer theTaskComments = new StringBuffer();
    int j = 0;
    WorkflowTaskInstance wti = null;
    Task t = null;
    int rownum = 0;
        String access = "";
    if (perms.getPermissionFor(Permission.COMMENT_ACCESS_RESTRICTED))
    {
        access = "Restricted";
    }
    else
    {
        access = "General";
    }

    String color = "";
    List allComments = new ArrayList();
    int count = 0;
    boolean atleastOneComment = false;
    Hashtable commentActivityMap = new Hashtable();
    for (int n = 0; n < tasks.size(); n++)
    {
        t= (Task)tasks.elementAt(n);
        long taskId = t.getId();
        String activityName = (String)actNames.elementAt(n);
        List taskComments = t.getTaskComments();
        if(taskComments != null)
        {
            for (int u = 0; u < taskComments.size(); u++)
            { 
                Comment tComment = null;
                tComment = (Comment)taskComments.get(u);
                commentActivityMap.put(tComment, activityName);
            }
            allComments.addAll(taskComments);
        }
    }
    if("desc".equals(sortComments))
    {
        Comparator r = Collections.reverseOrder();
        SortUtil.sort(allComments, r);
    }
    else if("asc".equals(sortComments))
    {
        SortUtil.sort(allComments);
    }

    if (allComments != null && allComments.size() > 0)
    {
        atleastOneComment = true;
        for (j = 0; j < allComments.size(); j++)
        { 
            if (count % 2 == 0)
            {
                color = "#FFFFFF";
            }
            else
            {
                color = "#EEEEEE";
            }
            theTaskComments.append ("<TR BGCOLOR=\"" + color + "\">\n");
            aComment = (Comment)allComments.get(j);
            theTaskComments.append("<TD CLASS=\"standardText\" COLSPAN=\"2\">\n");
            theTaskComments.append("<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=detailText>\n");
            theTaskComments.append("<COL STYLE=\"padding-right: 10px\">\n");
            theTaskComments.append("<COL>\n");
            theTaskComments.append("<TR>\n");
            theTaskComments.append("<TD><B>" + bundle.getString("lb_comment_creator") + ":</B></TD>\n");
            theTaskComments.append("<TD>" + aComment.getCreatorId() + "</TD>\n");
            theTaskComments.append("</TR>\n");
            theTaskComments.append("<TR>\n");
            theTaskComments.append("<TD><B>" + bundle.getString("lb_activity") + ":</B></TD>"); 
            theTaskComments.append("<TD>" + commentActivityMap.get(aComment) + "</TD>\n");
            theTaskComments.append("</TR>\n");
            theTaskComments.append("<TR>\n");
            theTaskComments.append("<TD><B>" + bundle.getString("lb_comment_id") + ":</B></TD>\n");
            theTaskComments.append("<TD>" + aComment.getId() + "</TD>\n");
            theTaskComments.append("</TR>\n");
            theTaskComments.append("<TR>\n");
            theTaskComments.append("<TD><B>" + bundle.getString("lb_date_created") + ":</B></TD>\n");
            theTaskComments.append("<TD>" + aComment.getCreatedDate() + "</TD>\n");
            theTaskComments.append("</TR>\n");
            theTaskComments.append("</TABLE>\n");
            theTaskComments.append(aComment.getComment());
            theTaskComments.append("<BR><BR></TD>");
            theTaskComments.append("</TR>");
            String commentId = (new Long(aComment.getId())).toString();
            ArrayList commentReferences = null;
            CommentManager mgr = null;
            try
            {
                mgr = ServerProxy.getCommentManager();
                commentReferences = mgr.getCommentReferences(commentId , access);
            }
            catch(Exception  e)
            {
                //nothing 
            }

            if (commentReferences != null)
            {
                int i=0;
                for (Iterator it = commentReferences.iterator(); it.hasNext(); ++i)
                {
                    CommentFile file = (CommentFile)it.next();

                    theTaskComments.append ("<TR BGCOLOR=\"" + color + "\">");
                    theTaskComments.append("<TD CLASS=standardText COLSPAN=2><IMG SRC=\"/globalsight/images/file.gif\" ALT=\"Reference File\" HEIGHT=15 WIDTH=13> ");

                    theTaskComments.append("<A class=\"standardHREF\" target=\"_blank\" href=\"/globalsight/");
                    theTaskComments.append(AmbFileStoragePathUtils.COMMENT_REFERENCE_SUB_DIR).append("/");
                    theTaskComments.append(commentId);
                    theTaskComments.append("/" + file.getFileAccess() + "/");
                    theTaskComments.append(file.getFilename());
                    theTaskComments.append("\">");
                    theTaskComments.append(EditUtil.encodeHtmlEntities(file.getFilename()));
                    theTaskComments.append("</A> ");
                    theTaskComments.append("<SPAN CLASS=smallText>");
                    theTaskComments.append(file.getFileSize()/1024 + "KB ");
                    if(file.getFileAccess().equals("Restricted"))
                    {
                        theTaskComments.append("<SPAN STYLE=\"color: red\">(" + bundle.getString("lb_restricted") + ")</SPAN>");
                    }
                    theTaskComments.append("</SPAN>");
                    theTaskComments.append("</TD>\n");
                    theTaskComments.append("</TR>\n");
                }
            }
            theTaskComments.append("<TR><TD COLSPAN=2 BGCOLOR=\"" + color + "\">&nbsp;</TD></TR>\n");
            count++;
        }
    }
    else
    {
        //no comments for this task
    }
    if(!atleastOneComment)
    {
        theTaskComments.append(
            "<TD VALIGN=\"TOP\"><SPAN CLASS=\"standardText\">\n<i>");
        theTaskComments.append(bundle.getString(
                                   "msg_comments_none_for_activity"));
        theTaskComments.append("</i></td>\n");
    }
    
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<BODY>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" STYLE="border: 1px solid navy">
    <TR CLASS="tableHeadingBasic">
        <TD ALIGN="LEFT" COLSPAN=2 WIDTH=700><%=labelComments %></TD>
    </TR>
    
<%=theTaskComments.toString()%> 

</TABLE>
</BODY>
</HTML>
