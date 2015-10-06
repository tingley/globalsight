<%@ page contentType="text/html; charset=UTF-8" errorPage="error.jsp"
	import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.taskmanager.TaskImpl,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
      com.globalsight.everest.jobhandler.JobImpl,
      com.globalsight.everest.workflowmanager.Workflow,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState.PagePair,
            com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFPageHandler,
            java.util.*"
	session="true"%>
<%
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    EditorState state = (EditorState) sessionMgr
            .getAttribute(WebAppConstants.EDITORSTATE);

    String curPageName = state.getCurrentPage().getPageName();
    long curPageId = state.getCurrentPage().getSourcePageId();

    Object obj = TaskHelper.retrieveObject(session,
            WebAppConstants.WORK_OBJECT);
    Task theTask = null;
    JobImpl theJob = null;
    if (obj instanceof TaskImpl)
    {
        TaskImpl taskImpl = (TaskImpl) obj;
        theTask = taskImpl;
    }
    else if (obj instanceof JobImpl)
    {
        theJob = (JobImpl) obj;
    }

    ArrayList<PagePair> pages = state.getPages();
%>

<html>
<head>
<meta charset="utf-8">
<title>file select</title>

<script type="text/javascript" src="/globalsight/jquery/jquery-1.10.2.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.11.2.js"></script>

<link rel="stylesheet" href="/globalsight/jquery/jquery-ui-1.11.2.css">
	
<link rel="stylesheet" href="filelist_style.css">

<style>
#selectable .ui-selecting {
	background: #FECA40;
}

#selectable .ui-selected {
	background: #A9A9F5;
	color: white;
}

#selectable {
	list-style-type: none;
	margin: 0;
	padding: 0;
	width: 100%;
}

#selectable li {
	margin: 3px;
	padding: 0.4em;
	font-size: 12px;
	height: auto;
}

body {
	font-family: "Trebuchet MS", "Helvetica", "Arial", "Verdana",
		"sans-serif";
	font-size: 14px;
}
</style>
<script>

var curPageName = "<%=curPageName.replace("\\", "\\\\")%>";
var pageNames = new Array();
var pageParas = new Array();

<%for (int i = 0; i < pages.size(); i++)
            {
                PagePair pagep = pages.get(i);
                if (PreviewPDFPageHandler.okForInContextReview(pagep))
                {
%>
                pageNames[pageNames.length] = "<%=pagep.getPageName().replace("\\", "\\\\")%>";
                
                <% if (theTask != null) { %>
                pageParas[pageParas.length] = "&sourcePageId=<%=pagep.getSourcePageId() %>&targetPageId=<%=pagep.getTargetPageId(state.getTargetLocale()) %>&taskId=<%= theTask.getId()%>";
                <%} else {%>
                pageParas[pageParas.length] = "&sourcePageId=<%=pagep.getSourcePageId() %>&targetPageId=<%=pagep.getTargetPageId(state.getTargetLocale()) %>&jobId=<%= theJob.getId()%>";
                <% } %>
<%}}%>
	$(function() {
		$("#selectable").selectable({
			stop : function() {
				$(".ui-selected", this).each(function() {
					var index = $("#selectable li").index(this);
					var selectPageName = pageNames[index];

					if (selectPageName != curPageName) {
						parent.parent.window.location = "/globalsight/ControlServlet?linkName=incontextreiview&pageName=TK2" + pageParas[index];
					}

				});
			}
		});
	});

	function displayFiles(show) {
		if (show) {
			document.getElementById("divFiles").style.display = "";
			document.getElementById("fileList").style.display = "";
			document.getElementById("hiddenFiles").style.display = "";
			document.getElementById("showFiles").style.display = "none";
			document.getElementById("fileListC").style.width = "150px";
			parent.document.getElementById("inctxrv_fset").cols = "12%,44%,44%";
		} else {
			document.getElementById("divFiles").style.display = "none";
			document.getElementById("fileList").style.display = "none";
			document.getElementById("hiddenFiles").style.display = "none";
			document.getElementById("showFiles").style.display = "";
			document.getElementById("fileListC").style.width = "20px";
			parent.document.getElementById("inctxrv_fset").cols = "2%,49%,49%";
		}

	}
</script>
</head>
<body>
	<div
		STYLE="width: 150px; word-break: break-all; POSITION: ABSOLUTE; Z-INDEX: 10; LEFT: 2px; RIGHT: 2px; TOP: 5px;" id="fileListC">
		<div style="float: left" id="divFiles">Files:</div>
		<div style="float: right">
			<a id="hiddenFiles" href="#" onclick="displayFiles(false)">&lt;&lt;</a><a
				id="showFiles" style="display: none;" href="#"
				onclick="displayFiles(true)">&gt;&gt;</a>
		</div>
		<div id="fileList">
			<br> <br>
			<ol id="selectable">

				<%
				    for (int i = 0; i < pages.size(); i++)
				    {
				        PagePair pagep = pages.get(i);

				        if (PreviewPDFPageHandler.okForInContextReview(pagep))
		                {
					        if (pagep.getSourcePageId() == curPageId)
					        {
					%>
					<li class="ui-widget-content ui-selected"><%=pagep.getPageName()%></li>
					<%
					    	}
					        else
					        {
					%>
					<li class="ui-widget-content"><%=pagep.getPageName()%></li>
					<%
					    	}
				    	}
				    }
				%>
			</ol>
		</div>
	</div>

</body>
</html>