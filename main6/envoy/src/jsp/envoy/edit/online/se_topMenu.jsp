<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.permission.Permission,
   			com.globalsight.everest.permission.PermissionSet,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="skin" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String url_save = save.getPageURL();

String lb_compactTags = bundle.getString("lb_editor_compact_tags");
String lb_verboseTags = bundle.getString("lb_editor_verbose_tags");
String lb_tm_search   = bundle.getString("lb_tm_search");
String lb_revert  = bundle.getString("lb_revert");
String lb_details = bundle.getString("lb_details");
String lb_options = bundle.getString("lb_options");
String lb_close   = bundle.getString("lb_close");
String lb_help    = bundle.getString("lb_help");

boolean b_ptagsVerbose =
  state.getPTagFormat().equals(EditorConstants.PTAGS_VERBOSE);


PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
boolean b_tmsearch=true;
if(state.isReadOnly()||(!perms.getPermissionFor(Permission.ACTIVITIES_TM_SEARCH)&&!perms.getPermissionFor(Permission.TM_SEARCH))){
        b_tmsearch=false;
}
%>
<HTML>
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
var helpFile = "<%=bundle.getString("help_segment_editor_(text)")%>";

function PostLoad()
{
  if (parent.IsImageEditor())
  {
    helpFile = "<%=bundle.getString("help_segment_editor_(image)")%>";
  }
}

function helpSwitch() 
{  
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function HidePTagBox()
{
    pTagBox.style.visibility = "hidden";
}

function ShowPTagBox()
{
    pTagBox.style.visibility = "visible";
}
</SCRIPT>
</HEAD>
<BODY BGCOLOR="<%=skin.getProperty("skin.editor.bgColor")%>">
<DIV ID="menu"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 0px; LEFT: 5px; RIGHT: 5px;">
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
  <TR CLASS="tableHeadingBasic">
    <TD>
      <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
	<TR CLASS="tableHeadingBasic">
	  <TD>
	    <DIV ID="pTagBox" CLASS="standardText"
	    STYLE="POSITION: relative; font-family:Arial Unicode MS; font-size: 8pt; VISIBILITY: hidden">
	    <FORM NAME="form">
	    <IMG SRC="/globalsight/images/spacer.gif" HEIGHT="7"><BR>

	    <SELECT ID="PTagBox" NAME="PTagBox" CLASS="standardText"
	      onchange="parent.DoChangePTags(this)">
<%
  if (b_ptagsVerbose)
  { 
    out.print("<OPTION name=compact>");
    out.print(lb_compactTags);
    out.print("</OPTION>");
    out.print("<OPTION name=verbose selected>");
    out.print(lb_verboseTags);
    out.print("</OPTION>");
  }
  else
  {
    out.print("<OPTION name=compact selected>");
    out.print(lb_compactTags);
    out.print("</OPTION>");
    out.print("<OPTION name=verbose>");
    out.print(lb_verboseTags);
    out.print("</OPTION>");
  }
%>
             </SELECT>
	     </FORM>
	     </DIV>
	  </TD>
	</TR>
      </TABLE>
    </TD>
    <TD ALIGN="RIGHT" VALIGN="TOP">
      <IMG SRC="/globalsight/images/spacer.gif" HEIGHT="12"><BR>
      <%if(b_tmsearch) { %>
      <A CLASS="HREFBoldWhite" HREF="#" onfocus="this.blur();"
       onclick="parent.dotmSearch(); return false;" ><%=lb_tm_search%></A> |
      <%} %>
      <A CLASS="HREFBoldWhite" HREF="#" onfocus="this.blur();"
       onclick="parent.doRevert(); return false;" ><%=lb_revert%></A> |
      <A CLASS="HREFBoldWhite" HREF="#" onfocus="this.blur();"
       onclick="parent.doDetails(); return false;"><%=lb_details%></A> |
      <A CLASS="HREFBoldWhite" HREF="#" onfocus="this.blur();"
       onclick="parent.doOptions(); return false;"><%=lb_options%></A> |
      <A CLASS="HREFBoldWhite" HREF="#" onfocus="this.blur();"
       onclick="parent.doClose(); return false;"><%=lb_close%></A> |
      <A CLASS="HREFBoldWhite" HREF="#" onfocus="this.blur();"
       onclick="helpSwitch(); return false;"><%=lb_help%></A>
    </TD>
  </TR>
</TABLE>
<FORM name="Save" METHOD="POST" ACTION="<%=url_save%>" TARGET="_parent">
<INPUT TYPE="hidden" NAME="save" VALUE="">
<INPUT TYPE="hidden" NAME="refresh" VALUE="">
<INPUT TYPE="hidden" NAME="releverage" VALUE="false">
<INPUT TYPE="hidden" NAME="tuId" VALUE="">
<INPUT TYPE="hidden" NAME="tuvId" VALUE="">
<INPUT TYPE="hidden" NAME="subId" VALUE="">
<INPUT TYPE="hidden" NAME="ptags" VALUE="">
<INPUT TYPE="hidden" NAME="isClosedComment" VALUE="">
</FORM>
<FORM name="Refresh" METHOD="POST" ACTION="<%=url_save%>" TARGET="_parent">
<INPUT TYPE="hidden" NAME="refresh" VALUE="">
<INPUT TYPE="hidden" NAME="releverage" VALUE="false">
<INPUT TYPE="hidden" NAME="tuId" VALUE="">
<INPUT TYPE="hidden" NAME="tuvId" VALUE="">
<INPUT TYPE="hidden" NAME="subId" VALUE="">
<INPUT TYPE="hidden" NAME="ptags" VALUE="">
<INPUT TYPE="hidden" NAME="isClosedComment" VALUE="">
</FORM>
</DIV>
</BODY>
</HTML>
