<%@ page contentType="text/html; charset=UTF-8"
         import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.util.modules.Modules,
                 java.util.Calendar,
                 java.text.MessageFormat,
                 com.globalsight.util.ServerUtil,
                 java.util.ResourceBundle"
         session="true" 
%>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
    //locale bundle labels
    ResourceBundle bundle = PageHandler.getBundle(session);
    String lbAboutGlobalsightSystem = bundle.getString("lb_about_globalsight_system4");
	String lbVersion = bundle.getString("lb_version");
	Object[] args = {String.valueOf(Calendar.getInstance().get(Calendar.YEAR))};
    String lbCopyright = MessageFormat.format(bundle.getString("lb_copyright"), args);
    String lbTrademark = bundle.getString("lb_trademark");
    String lbVisitGlobalsight = bundle.getString("lb_visit_globalsight");
    String lbClose = bundle.getString("lb_close");
    String buildNumber = ServerUtil.getVersion();
%>
<HTML>
<HEAD>
<TITLE><%=lbAboutGlobalsightSystem%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<BODY BGCOLOR="#FFFFFF">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 0px; LEFT: 20px; RIGHT: 20px;">
<center>
<TABLE WIDTH="100%">
<TR>
<TD><IMG SRC="/globalsight/images/logo_header.gif"></TD>
<TD ALIGN="RIGHT"><IMG SRC="/globalsight/images/TMX.gif"/></TD>
</TR></TABLE>
</CENTER>

<TABLE WIDTH="100%">
<TR>
	<TD ALIGN="LEFT"><SPAN CLASS="standardText"><B><%=lbVersion%> <%=buildNumber%></B></SPAN></TD>
	<% if (b_catalyst) {%>
	<TD ALIGN="RIGHT"><IMG SRC="/globalsight/images/logo_alchemy.gif"/></TD>
	<% } %>
</TR>
</TABLE>


<FONT SIZE="-1">
<%=lbCopyright%><BR></BR>
<%=lbTrademark%><BR></BR>
<%=bundle.getString("lb_tmx_logo_text1")%><%=bundle.getString("lb_tmx_logo_text2")%>
</FONT>
<BR><BR><SPAN CLASS="standardText">
<B><%=lbVisitGlobalsight%></B>
<A CLASS="standardHREF" href="http://www.globalsight.com" TARGET="_blank">www.GlobalSight.com</A>
</SPAN>
<CENTER>
<BR>
<INPUT TYPE="BUTTON" NAME="OK" VALUE="<%=lbClose %>" ONCLICK="window.close()"> 
<INPUT TYPE="BUTTON" NAME="OK" VALUE="<%=bundle.getString("lb_installed_patches") %>" ONCLICK="location.replace('patches.jsp')"> 
</CENTER>
</DIV>
</BODY>
</HTML>
