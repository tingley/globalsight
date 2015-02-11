<%@ page isErrorPage="true" 
    contentType="text/html; charset=UTF-8"
    import="com.globalsight.everest.servlet.ControlServlet,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.Locale,
            java.util.ResourceBundle"
    session="false"
%>
<jsp:useBean id="error" scope="request"
 class="com.globalsight.everest.webapp.javabean.ErrorBean" />
<%
    ControlServlet.handleJSPException(exception);

    ResourceBundle bundle = 
      SystemResourceBundle.getInstance().getResourceBundle(
        ResourceBundleConstants.LOCALE_RESOURCE_NAME, Locale.US);

    String msg = error.getMessage();
    if (msg == null || msg.trim().length() == 0)
    {
    	msg = bundle.getString("msg_no_exception_message");
    }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%=bundle.getString("lb_system_error")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
    
<!-- Header table -->
<DIV ID="header" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 20px; RIGHT: 20px;">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR CLASS="header1">
        <TD WIDTH="253"><IMG SRC="/globalsight/images/logo_header.gif" HEIGHT="68" WIDTH="253"></TD>
        <TD WIDTH="451"><IMG SRC="/globalsight/images/globe_header.gif" HEIGHT="68" WIDTH="451"></TD>
        <TD ALIGN="RIGHT">
            <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
                <TR>
                    <TD CLASS="header1" ALIGN="right"><%=bundle.getString("lb_error")%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
                </TR>
            </TABLE>
        </TD>
    </TR>
    <TR>
        <TD COLSPAN="3" CLASS="header2" HEIGHT="20" ALIGN="RIGHT"></TD>
    </TR>
</TABLE>
</DIV>
<!-- End Header table-->

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px;">
<P CLASS="mainHeading"><%=bundle.getString("lb_system_error")%></P>
<P CLASS="standardText"><PRE style="color:red"><%=msg%></PRE></P>
</DIV>
</BODY>
</HTML>

