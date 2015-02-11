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
<jsp:useBean id="tb" scope="request"
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

//if show mt in segment editor
boolean show_in_editor = false;
try {
	String showInEditor = (String) sessionMgr.getAttribute("SHOW_IN_EDITOR");
	show_in_editor = (new Boolean(showInEditor)).booleanValue();
} catch (Exception e) { }

String url_source = source.getPageURL();
String url_target = target.getPageURL();
String url_tb     = tb.getPageURL();
String url_tm     = tm.getPageURL();
String url_mt     = mt.getPageURL();
String url_stages = stages.getPageURL();

String lb_source = bundle.getString("lb_source");

// Locale class keeps interned strings
String lang;

lang = state.getSourceLocale().getLocale().getLanguage();
boolean b_srcIsBidi = (lang == "he" || lang == "ar");

lang = state.getTargetLocale().getLocale().getLanguage();
boolean b_trgIsBidi = (lang == "he" || lang == "ar");

// The source frame should have proper LANG and DIR settings.
// For strings visible in HTML this will be the source locale
// and the native direction of the source language.
// For non-visible strings (all localizables), the language
// is en-US with LTR.

String str_langAttr = EditUtil.getLanguageAttributes(
  state.getSourceLocale(), view.isLocalizable());

%>
<HTML>
<HEAD>
<SCRIPT>
function IsImageEditor()
{
  return false;
}

function ShowSourceSegment(text, whitepreserving)
{
    if (whitepreserving)
    {
      //var re = new RegExp("\r\n?|\n", "g");
      //text = text.replace(re, "<BR>");
      text = "<pre>" + text + "</pre>";
    }
    if(source.document.getElementById("idSourceCell"))
	{
      source.document.getElementById("idSourceCell").innerHTML =
      "<SPAN CLASS='standardText' <%=str_langAttr%>>" + text + "</SPAN>";
	}
}

function SetVerbosePTags(flag)
{
}

function SetTargetSegment(segment, changed)
{
    target.SetSegment(segment, changed);
}

function GetTargetSegment()
{
    var segment = target.GetSegment();
    return segment;
}

function InsertTerm(term)
{
    target.InsertTerm(term);
}

function CanClose()
{
    return target.CanClose();
}
</SCRIPT>
</HEAD>
<% if (show_in_editor) { %>
<FRAMESET ROWS="15%,25%,15%,15%,15%,15%"
  FRAMEBORDER="no" BORDER="0" FRAMESCAPING="no">
  <FRAME NAME="source" SRC="<%=url_source%>" 
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="target" SRC="<%=url_target%>"
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="tb" SRC="<%=url_tb%>"
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="tm" SRC="<%=url_tm%>" 
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="mt" SRC="<%=url_mt%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="stages" SRC="<%=url_stages%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
<% } else { %>
<FRAMESET ROWS="15%,32%,18%,18%,17%"
  FRAMEBORDER="no" BORDER="0" FRAMESCAPING="no">
  <FRAME NAME="source" SRC="<%=url_source%>" 
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="target" SRC="<%=url_target%>"
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="tb" SRC="<%=url_tb%>"
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="tm" SRC="<%=url_tm%>" 
         SCROLLING="auto" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
  <FRAME NAME="stages" SRC="<%=url_stages%>" 
         SCROLLING="no" NORESIZE MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
<% } %>
</HTML>
