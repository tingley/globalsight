<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState,
            com.globalsight.everest.glossaries.GlossaryFile,
            com.globalsight.everest.glossaries.GlossaryUpload,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.AmbFileStoragePathUtils,
            java.util.*"
    session="true"
%><%@page import="com.globalsight.ling.common.URLEncoder"%>

<%!
static private String getGlossaryPath(GlossaryFile p_file)
{
    StringBuffer result = new StringBuffer(32);

    if (p_file.isForAnySourceLocale())
    {
        result.append(p_file.getGlobalSourceLocaleName());
    }
    else
    {
        result.append(p_file.getSourceLocale().toString());
    }

    result.append("/");

    if (p_file.isForAnyTargetLocale())
    {
        result.append(p_file.getGlobalTargetLocaleName());
    }
    else
    {
        result.append(p_file.getTargetLocale().toString());
    }

    result.append("/");

    result.append(p_file.getFilename());

    return result.toString();
}
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String xmlTermbases = state.getTermbaseNames();
String defaultTermbase = state.getDefaultTermbaseName();

// list of glossaries and termbases
GlossaryState glossaryState =
  (GlossaryState)sessionMgr.getAttribute(WebAppConstants.GLOSSARYSTATE);
Collection glossaryList = (glossaryState != null) ?
  glossaryState.getGlossaries() : null;

String lb_clickToOpen = bundle.getString("lb_click_to_open");
String lb_close = bundle.getString("lb_close");
String lb_help = bundle.getString("lb_help");
String lb_no_glossaries = bundle.getString("lb_no_glossaries");
String lb_supportFiles = bundle.getString("lb_supportFiles");
String lb_title = bundle.getString("lb_supportFiles");

%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<!-- filename: me_resources.jsp -->
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
var helpFile = "<%=bundle.getString("help_main_editor_supportfiles")%>";

function helpSwitch() 
{  
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function closeThis()
{
    window.close();
}
</SCRIPT>
</HEAD>
<BODY onload="window.focus()">
<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
  <TR>
    <TD><SPAN class="mainHeading"><%=lb_title%></SPAN></TD>
    <TD ALIGN="RIGHT">
      <SPAN class="HREFBold">
      <A CLASS="HREFBold" HREF="javascript:helpSwitch();"><%=lb_help%></A> |
      <A CLASS="HREFBold" HREF="javascript:closeThis();"><%=lb_close%></A>
      </SPAN>
    </TD>
  </TR>
</TABLE>
<P></P>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="0">
  <TR>
    <TD CLASS="tableHeadingBasic"><%=lb_supportFiles%></TD>
  </TR>
  <%
  if (glossaryList != null && glossaryList.size() > 0)
  {
    String color;
    String urlBase, url;
    int i = 0;

    urlBase = "/globalsight/" + AmbFileStoragePathUtils.SUPPORT_FILES_SUB_DIRECTORY + "/";

    for (Iterator it = glossaryList.iterator(); it.hasNext(); ++i)
    {
      GlossaryFile file = (GlossaryFile)it.next();
      if (i % 2 == 0)
      {
        color = "#FFFFFF";
      }
      else
      {
        color = "#EEEEEE";
      }

      url = urlBase + getGlossaryPath(file);
      
  %>
  <TR BGCOLOR="<%=color%>">
    <TD>
      <A CLASS="standardHREF" TITLE="<%=lb_clickToOpen%>" href="<%=URLEncoder.encodeUrlStr(url)%>"
      target="_blank"><%=EditUtil.encodeHtmlEntities(file.getFilename())%></A>
    </TD>
  </TR>
  <%
    }
  }
  else
  {
  %>
  <TR>
    <TD>
      <SPAN CLASS="standardText"><%=lb_no_glossaries%></SPAN>
    </TD>
  </TR>
  <%
  }
  %>
</TABLE>
</BODY>
</HTML>
