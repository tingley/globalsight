<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
     com.globalsight.everest.webapp.pagehandler.PageHandler, 
         java.util.Locale, java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="next3" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel3" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="pre3" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%  ResourceBundle bundle = PageHandler.getBundle(session);
    String next3URL = next3.getPageURL();
  String cancel3URL = cancel3.getPageURL();
  String pre3URL = pre3.getPageURL();
  String title = bundle.getString("lb_db_column_profiles");
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_db_profile") %>";
var guideNode = "dbProfiles";
var helpFile = "<%=bundle.getString("help_columns")%>";

function cancelForm()
{
   profileCancel.submit();
}
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px">

    <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
        <TR>
            <TD ALIGN="<%=gridAlignment%>">

                <SPAN CLASS="mainHeading">
                <%=title%>
                </SPAN>
                
                <P>
                <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
                <TR>
                <TD WIDTH=538>
                <%=bundle.getString("helper_text_db_profile_column")%>                
                </TD>
                </TR>
                </TABLE>
                <P>

                <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
                    <TR>
                        <TD>
                            <applet name="DBColumnApplet" code="com.globalsight.everest.webapp.applet.admin.dbprofile.DBColumnApplet" width="538" height="385" codebase="/globalsight/classes/">
                                <param name="rand" value=<%=session.getAttribute("UID_" + session.getId())%>>
                                <param name="servletUrl" value="/globalsight/ControlServlet?linkName=next2&pageName=DBPN1&applet=true&rand=">
                                <param name="grid" value="com.globalsight.everest.webapp.applet.admin.dbprofile.DBColumnPanel">
                                <param name=next3URL value="<%=next3URL%>&applet=yes&initial=true&rand=">
                                <param name=cancel3URL value="<%=cancel3URL%>&Cancel=Cancel&applet=yes&initial=true&rand=">
                                <param name=pre3URL value="<%=pre3URL%>&applet=yes&initial=true&rand=">
                            </applet>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
    </TABLE>
</DIV>
</BODY>
</HTML>

