<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
				 import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
				 com.globalsight.everest.webapp.webnavigation.LinkHelper,
				 com.globalsight.util.resourcebundle.ResourceBundleConstants,
				 com.globalsight.util.resourcebundle.SystemResourceBundle,
				 com.globalsight.everest.servlet.util.SessionManager,
		 		 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.util.system.SystemConfiguration,                 
				 java.util.Locale, java.util.ResourceBundle"
				 session="true" %>
<%!
static boolean b_reports = false;
static 
{
    try
    {
    SystemConfiguration sc = SystemConfiguration.getInstance();
    b_reports = sc.getBooleanParameter(SystemConfigParamNames.REPORTS_ENABLED);
    }
    catch (Throwable e)
    {
       // in this case, no teamsite
    }
}
%>
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String title= bundle.getString("lb_target_branch");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "teamsiteBranches";
var helpFile = "<%=bundle.getString("help_teamsite_target_branches")%>";
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<!-- Don't use navigation.jsp because the nav pull-downs don't
     work with HTML frames! -->

<DIV ID="navigation" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 68px; LEFT: 0px;">

<!-- Navigation table -->
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
    <TR>
        <TD CLASS="header2" HEIGHT="20">&nbsp;&nbsp;&nbsp; 
            <A
            CLASS="header2"
            onClick="return confirmJump();"
            HREF="/globalsight/ControlServlet?"
            TARGET="_top"><%=bundle.getString("lb_setup")%></A> |
            
            <A
            CLASS="header2"
            onClick="return confirmJump();"
            HREF="/globalsight/ControlServlet?"
            TARGET="_top"><%=bundle.getString("lb_data_sources")%></A> |
            
            <A
            CLASS="header2"
            onClick="return confirmJump();"
            HREF="/globalsight/ControlServlet?"
            TARGET="_top"><%=bundle.getString("lb_guides")%></A> |

            <A
            CLASS="header2"
            onClick="return confirmJump();"
            HREF="/globalsight/ControlServlet?activityName=jobsInProgress"
            TARGET="_top"><%=bundle.getString("lb_my_jobs")%></A> |
            
            <A
            CLASS="header2"
            onClick="return confirmJump();"
            HREF="/globalsight/ControlServlet?activityName=myactivities"
            TARGET="_top"><%=bundle.getString("lb_my_activities")%></A>
            
       <% if (b_reports) { %>
           | <A
            CLASS="header2"
            onClick="return confirmJump();"
            HREF="/globalsight/ControlServlet?activityName=reports"
            TARGET="_top"> <%=bundle.getString("lb_reports")%></A>
       <% } %>

        </TD>
        <TD CLASS="header2" HEIGHT="20" ALIGN="RIGHT">
            <A
            CLASS="header2"
            onClick="javascript:aboutWindow = window.open('/globalsight/envoy/about/about.jsp','About','HEIGHT=350,WIDTH=450,scrollbars'); return(false);"
            HREF="#"
            TARGET="_top"><%=bundle.getString("lb_about_system4")%></A>&nbsp;&nbsp;
        </TD>
    </TR>
</TABLE>
</DIV>
<!-- End Navigation table -->
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer">
</DIV>
</BODY>
</HTML>
