<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            java.io.*,
            java.util.*"
    session="true"
%>
<jsp:useBean id="content" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="sourceMenu" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="inctxrvPDF" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<%
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
String contentUrl = content.getPageURL();

int iViewMode = state.getLayout().getSourceViewMode();
String viewMode = "list";

%>
<HTML>
<HEAD>
<SCRIPT>

var mode = "<%=viewMode%>";
var isVisible = false; //is progress bar visible

function showProgressBar()
{
  try
  {
	   var div = content.document.getElementById('src_prograssbar');
	   isVisible = div.style.visibility == "visible";
	   if (!isVisible)
       {
		   content.showProgressBar();
       }
  }
  catch(e)
  {
  }
}

function reloadContent(modeId)
{
  try
  {
    content.document.location =
      "<%=contentUrl%>" + "&srcViewMode=" + modeId;
  }catch(e)
  {
     document.location= "/globalsight/ControlServlet?linkName=pane1&pageName=ED3&srcViewMode=" + modeId;
  }
}

function showList()
{
    mode = "list";
    reloadContent(<%=EditorConstants.VIEWMODE_DETAIL%>+"&reuseData=source");
}

function showPreview()
{
    mode = "preview";
    reloadContent(<%=EditorConstants.VIEWMODE_PREVIEW%>);
}
function showPDFPreview(pageName)
{
	  try
	  {
	      if(!isVisible)
	      {
	         content.document.location =
	           "<%=inctxrvPDF.getPageURL()%>" + "&action=previewSrc&file=" + encodeURIComponent(pageName);
	      }
	  }catch(e)
	  {
	  }
}
function showTargetPDFPreview(pageName)
{
	  try
	  {
	      if(!isVisible)
	      {
	         content.document.location =
	           "<%=inctxrvPDF.getPageURL()%>" + "&action=previewTar&file=" + encodeURIComponent(pageName);
	      }
	  }catch(e)
	  {
	  }
}

function HighlightSegment(){}
function UnhighlightSegment(){}
function RefreshTargetPane(){}
</SCRIPT>
<FRAMESET ROWS="*,25" BORDER="0">
  <FRAME SRC="<%=contentUrl%>" NAME="content" SCROLLING="auto"
   NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME SRC="<%=sourceMenu.getPageURL()%>" NAME="sourceMenu" SCROLLING="no"
   NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
</HEAD>
</HTML>
