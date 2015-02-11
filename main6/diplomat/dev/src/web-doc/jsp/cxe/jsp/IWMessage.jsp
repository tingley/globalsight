<%@ page import="java.util.*,java.util.Date,com.globalsight.diplomat.javabeans.MessageBean" %>

<jsp:useBean id="message" class="com.globalsight.diplomat.javabeans.MessageBean" scope="request" />
<%
        response.setContentType("text/html; charset=UTF-8");
%>
<HTML>
<!-- IWMessage.jsp -->
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<SCRIPT LANGUAGE="JavaScript" SRC="/includes/setStyleSheet.js"></SCRIPT>
<TITLE>GlobalSight Message</TITLE>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<DIV ID="header" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
        <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
            <TR>
                <TD><IMG SRC="/globalsight/images/logo_header.gif" HEIGHT="68" WIDTH="253"><BR>
                    <HR NOSHADE SIZE=1> 
                </TD>
            </TR>
        </TABLE>
</DIV>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 88px; LEFT: 20px;">
    <SPAN CLASS="mainHeading">GlobalSight Message</SPAN>
    <P>
    <SPAN CLASS="standardText">
    <jsp:getProperty name="message" property="messageText"/>
    </SPAN>
    
    <P>
<INPUT TYPE="BUTTON" NAME="OK" VALUE="OK" ONCLICK="window.close()">
</DIV>
</BODY>
</HTML>
