<%@ page contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.comment.IssueOptions,
            com.globalsight.everest.comment.Issue,
            com.globalsight.everest.edit.online.CommentView,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            java.util.Locale,
            java.util.ResourceBundle"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String lb_source = bundle.getString("lb_source");
SessionManager sessionMgr = (SessionManager)session.getAttribute(
        WebAppConstants.SESSION_MANAGER);
        
EditorState state =
    (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
  
long targetPageId   = state.getTargetPageId().longValue();
long tuId  = state.getTuId();
long tuvId = state.getTuvId();
long subId = state.getSubId();

CommentView commentView = state.getEditorManager()
    .getCommentView(-1, targetPageId, tuId, tuvId, subId);
       
String checked = "";

if(commentView.getComment() != null) {
    if(commentView.getComment().getStatus().equals("closed")) {
        checked = "checked disabled";
    }
    else {
        checked = "";
    }
}
else {
    checked = "disabled";
}

%>
<HTML>
<HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<link type="text/css" rel="StyleSheet" id="cssPtag"
  href="/globalsight/envoy/edit/online2/ptag.css">
<STYLE>
#idSourceCell {
  font-size: 10pt;
  font-family: Arial, Helvetica, sans-serif;
}
</STYLE>
<SCRIPT>
var isClosed = false;
function HilitePtags(bright)
{
    var css = document.styleSheets.item('cssPtag');
    var rule = (css.rules)?css.rules[0]:css.cssRules[0];
    rule.style.color = bright ? '#3366FF' : '#808080';
}

function imageError()
{
  idSourceImage.alt = '<%=bundle.getString("lb_load_image_failed") %>';
  event.returnValue = true;
}
function closeComment()
{
  isClosed = true;
}

function getIsClosedComment()
{
  return isClosed;
}

function getCheckBoxIsChanged() {

    var checkbox = document.getElementById("checkboxClose");

    if(checkbox.disabled == false && checkbox.checked == true) {
        return true;
    }
    
    return false;
    
}

</SCRIPT>
</HEAD>
<BODY>
<DIV STYLE="POSITION: ABSOLUTE; TOP: 0px; LEFT: 0px;">
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="470">
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="5" HEIGHT="1"></TD>
    <TD WIDTH="60%">
      <SPAN CLASS="standardTextBold" id="idLabel"><%=lb_source%></SPAN>
    </TD>

    <TD ALIGN="right">
      <input type="checkbox" name="isClosed" id="checkboxClose" <%=checked %> onclick = "closeComment();" >
      <SPAN CLASS="standardTextBold" id="idLabel"><%= bundle.getString("close_comment") %></SPAN>
    </TD>

  </TR>
</TABLE>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%"
  id="idSourceTable">
  <TR>
    <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="10" HEIGHT="1"></TD>
    <TD VALIGN="TOP" WIDTH="100%" id="idSourceCell">
      <SPAN CLASS="standardText" style="font-style: italic; color: gray; ">
      <%= bundle.getString("msg_loading") %></SPAN>
    </TD>
  </TR>
</TABLE>
</DIV>
</BODY>
</HTML>
