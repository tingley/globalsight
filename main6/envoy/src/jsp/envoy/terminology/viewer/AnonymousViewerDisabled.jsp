<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/common/error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            java.text.MessageFormat,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="skin" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<%
//session.setAttribute(WebAppConstants.UILOCALE, Locale.US);

ResourceBundle bundle = PageHandler.getBundle(session);

SystemConfiguration sc = SystemConfiguration.getInstance();
String email = sc.getStringParameter(SystemConfigParamNames.ADMIN_EMAIL);

Object[] args = { email };
MessageFormat format = new MessageFormat(
  bundle.getString("msg_anonymous_termbase_access_not_allowed"));
String lb_helptext = format.format(args);
String lb_title = bundle.getString("lb_termbases");

String logoImage = skin.getProperty("skin.banner.logoImage");
String logoBackgroundImage = skin.getProperty("skin.banner.logoBackgroundImage");
boolean useOneLogoImage = false;
if (logoImage.equals(logoBackgroundImage))
{
    useOneLogoImage = true;
}
%>
<HTML >
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" oncontextmenu="return false">

<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
  <TR CLASS="header1">
    <% if (useOneLogoImage == true){ %>
    <TD WIDTH="704"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="704"></TD>
    <%} else {%>
    <TD WIDTH="253"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="253"></TD>
    <TD WIDTH="451"><IMG SRC="<%=logoBackgroundImage%>" HEIGHT="68" WIDTH="451"></TD>
    <%}%>            
    <TD ALIGN="RIGHT" CLASS="header1">&nbsp;</TD>
  </TR>
</TABLE>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px;">
<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="500">
  <TR>
    <TD><SPAN class="mainHeading"><%=lb_title%></SPAN></TD>
    <TD ALIGN="RIGHT">&nbsp;</TD>
  </TR>
  <TR><TD COLSPAN="3">&nbsp;</TD></TR>
  <TR>
    <TD COLSPAN="2"><SPAN class="standardHREF"><%=lb_helptext%></SPAN></TD>
  </TR>
</TABLE>
</DIV>

</BODY>
</HTML>
