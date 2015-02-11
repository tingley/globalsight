<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="source" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="target" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="tm" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="mt" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="stages" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

String url_source = source.getPageURL();
String url_target = target.getPageURL();
String url_tm     = tm.getPageURL();
String url_mt     = mt.getPageURL();
String url_stages = stages.getPageURL();
String url_sourceImage = view.getSourceImageUrl();

String lb_source = bundle.getString("lb_source_image");
String lb_url = bundle.getString("lb_url")+bundle.getString("lb_colon");

String str_langAttr = EditUtil.getLanguageAttributes(state.getSourceLocale(),
    view.isLocalizable());

boolean show_in_editor = false;
try {
	String showInEditor = (String) sessionMgr.getAttribute("SHOW_IN_EDITOR");
	show_in_editor = (new Boolean(showInEditor)).booleanValue();
} catch (Exception e) { }
%>

<HTML>
<HEAD>
<SCRIPT>
function IsImageEditor()
{
  return true;
}

function ShowSourceSegment(text, whitepreserving)
{
    source.idLabel.innerText = "<%=EditUtil.toJavascript(lb_source)%>";
    source.idSourceCell.innerHTML =
      "<SPAN CLASS='standardText'><%=lb_url%></SPAN> " +
      "<SPAN CLASS='standardText'>" + text + "</SPAN>";

    var table = source.idSourceTable;
    var tr = table.insertRow();
    var td = tr.insertCell();
    td = tr.insertCell();
    td.innerHTML = "<IMG style='border: 1px solid black;' " +
      "id='idSourceImage' onerror='imageError()' src=\"<%=url_sourceImage%>\">";
}

function SetVerbosePTags(flag)
{
}

function SetTargetSegment(segment, changed, whitepreserving)
{
    target.SetSegment(segment, changed, whitepreserving);
}

function GetTargetSegment()
{
    var segment = target.GetSegment();
    return segment;
}

function CanClose()
{
    return target.CanClose();
}
</SCRIPT>
</HEAD>
<% if (show_in_editor) { %>
<FRAMESET ROWS="28%,32%,13%,13%,13%" FRAMEBORDER="no" BORDER="0" FRAMESCAPING="no">
  <FRAME NAME="source" SRC="<%=url_source%>" 
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="target" SRC="<%=url_target%>"
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="tm" SRC="<%=url_tm%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="mt" SRC="<%=url_mt%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">       
  <FRAME NAME="stages" SRC="<%=url_stages%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
<% } else { %>
<FRAMESET ROWS="30%,38%,16%,16%" FRAMEBORDER="no" BORDER="0" FRAMESCAPING="no">
  <FRAME NAME="source" SRC="<%=url_source%>" 
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="target" SRC="<%=url_target%>"
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="tm" SRC="<%=url_tm%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="stages" SRC="<%=url_stages%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
<% } %>
</HTML>
