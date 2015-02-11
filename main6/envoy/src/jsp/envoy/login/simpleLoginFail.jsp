<%@ page contentType="text/html; charset=UTF-8" session="false" %><% 
  String en_aboutUrl = "About GlobalSight";
  String title= "GlobalSight";
  String aboutUrl = "/globalsight/envoy/about/about.jsp";
  String headerStyle = "warningText";
%><HTML>
<HEAD>
    <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
    <TITLE><%=title %></TITLE>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
    <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
</HEAD>
    <BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
    <DIV ID="header0" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
    <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
        <TR CLASS="header1">
            <TD WIDTH="253"><IMG SRC="/globalsight/images/logo_header.gif" HEIGHT="68" WIDTH="253"></TD>
            <TD WIDTH="451"><IMG SRC="/globalsight/images/globe_header.gif" HEIGHT="68" WIDTH="451"></TD>
            <TD ALIGN="RIGHT">
                <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
                    <TR>
                        <TD CLASS="header1" ALIGN="right">&nbsp;&nbsp;&nbsp;&nbsp;</TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <TD COLSPAN="3" CLASS="header2" HEIGHT="20" ALIGN="RIGHT"><A CLASS="header2" HREF="#" onClick="javascript:aboutWindow = window.open('<%=aboutUrl%>','about','HEIGHT=350,WIDTH=450,scrollbars'); return false;">
                <%= en_aboutUrl %></A>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        </TR>
    </TABLE>
    </DIV>

    <DIV ALIGN="CENTER" ID="contentLayer0" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 150px; LEFT: 0px;">
       <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
       <TR> <TD ALIGN="CENTER"> <SPAN CLASS="<%=headerStyle%>">Unable to perform login.</SPAN></TD></TR>
       <TR> <TD ALIGN="CENTER"> <SPAN CLASS="<%=headerStyle%>">Your account may not be set up properly.</SPAN></TD></TR>       
       <TR> <TD ALIGN="CENTER"> <SPAN CLASS="<%=headerStyle%>">Please notify your system administrator.</SPAN></TD></TR>
       </TABLE>
    </DIV>
    </BODY>
</HTML>
