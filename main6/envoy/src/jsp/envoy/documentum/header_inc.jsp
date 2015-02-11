<%@ page contentType="text/html; charset=UTF-8"
		 errorPage="error.jsp"
                 session="true"
                 import="java.util.*, java.io.*" %>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<%
String title = "Welocalize - Documentum CMS Browser";
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js"></SCRIPT>
<%@ include file="warning.jsp" %>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">

<DIV ID="header" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
    <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
        <TR CLASS="header1">
            <TD><IMG SRC="Images/logo_header.gif" HEIGHT="68" WIDTH="334"></TD>
            <TD ALIGN="left" VALIGN="TOP">
                <IMG SRC="Images/spacer.gif" HEIGHT=5 WIDTH=1><BR>
                &nbsp;&nbsp;<BR>
                <IMG SRC="Images/spacer.gif" HEIGHT=7 WIDTH=1><BR>
            </TD>
        </TR>
         <TR>
                <TD COLSPAN="2" CLASS="header2" HEIGHT="20"></TD>
            </TR>
    </TABLE>
</DIV>
</BODY>
</HTML>

