<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.logs.ViewLogsMainHandler,
                com.globalsight.util.j2ee.AppServerWrapperFactory,
                com.globalsight.util.j2ee.AppServerWrapper,                
                java.util.ResourceBundle" session="true" 
%>
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title= bundle.getString("lb_logs");
    String docsDir = (String) request.getAttribute(ViewLogsMainHandler.CXE_DOCS_DIR);
    String j2eeVendor = AppServerWrapperFactory.getAppServerWrapper().getJ2EEServerName();
    int rowNum = 0;
%>
<%!
    //colors to use for the table background
    private static final String WHITE_BG         = "#FFFFFF";
    private static final String LT_GREY_BG       = "#EEEEEE";      
      
      /**
      * Toggles the background color of the rows used between WHITE and LT_GREY
      */
      private static String toggleBgColor(int p_rowNumber)
      {
         if (p_rowNumber % 2 == 0)
            return WHITE_BG;
         else
            return LT_GREY_BG;
      }
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "logs";
    var helpFile = "<%=bundle.getString("help_log_files")%>";
</SCRIPT>

<STYLE>
TR.standardText 
{
    vertical-align: top;
}
</STYLE>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=538>
<%=bundle.getString("helper_text_logs")%>
</TD>
</TR>
</TABLE>
<P>
    
    <TABLE BORDER="0" CELLPADDING="4" CELLSPACING="0" WIDTH=600 CLASS="standardText">
        <TR>
            <TD CLASS="tableHeadingBasic"><%=bundle.getString("log_table_header1")%></TD>
            <TD CLASS="tableHeadingBasic" ><%=bundle.getString("log_table_header2")%></TD>
        </TR>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD><A CLASS="standardHREF" HREF="/globalsight/system4_dir?file=GlobalSight.log&dummy=dummy.txt" target="_blank">GlobalSight.log </A></TD>
            <TD><%=bundle.getString("log_cap")%></TD>
        </TR>

        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD><A CLASS="standardHREF" HREF="/globalsight/system4_dir?file=GlobalSight.log&zip=true&dummy=GlobalSight.log.zip">GlobalSight.log.zip</A></TD>
            <TD><%=bundle.getString("log_cap_zip")%></TD>
        </TR>

        <% if (AppServerWrapperFactory.WEBLOGIC.equals(j2eeVendor)) { %>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD><A CLASS="standardHREF" HREF="/globalsight/system4_dir?file=weblogic.log&dummy=dummy.txt" target="_blank">weblogic.log
            </A></TD>
            <TD><%=bundle.getString("log_weblogic")%></TD>
        </TR>
        <% } %>
        
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
            <TD><A CLASS="standardHREF" HREF="/globalsight/system4_dir?file=term_audit.log&dummy=dummy.txt" target="_blank">term_audit.log </A></TD>
            <TD><%=bundle.getString("log_tb_audit")%></TD>
        </TR>
        
    </TABLE>
</DIV>
</BODY>
</HTML>

