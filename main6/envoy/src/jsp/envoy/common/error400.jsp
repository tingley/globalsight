<%@ page isErrorPage="true" 
    contentType="text/html; charset=UTF-8"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.javabean.ErrorBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.Locale,
            java.util.ResourceBundle"
    session="false"
%>
<%@ include file="/envoy/common/headerResponse.jspIncl" %>
<%
    ResourceBundle bundle = 
      SystemResourceBundle.getInstance().getResourceBundle(
        ResourceBundleConstants.LOCALE_RESOURCE_NAME, Locale.US);

    String title = "400 -- Bad Request";
    String reason = "The requested page does not exist.";
    String returnToSys4 = bundle.getString("msg_return_to_sys4");
    String lbError= "400 Error Page";
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
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
                    <TD CLASS="header1" ALIGN="right"><%=lbError%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
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

<P CLASS="mainHeading"><%=title%></P>
<P CLASS="standardText"><%=reason%></P>
<P CLASS="standardText"><A CLASS=standardHREF HREF="/globalsight/wl"><%=returnToSys4 %></A></P>

<P></P>
</DIV>
</BODY>
</HTML>

