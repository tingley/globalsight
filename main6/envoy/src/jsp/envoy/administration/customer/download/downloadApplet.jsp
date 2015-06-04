<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler, 
                com.globalsight.everest.foundation.Timestamp,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                java.util.ResourceBundle,
                java.util.TimeZone"
		session="true"
%>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="doneFromJob" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="doneFromTask" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="doneFromExportJobs" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="doneFromLocalizedJobs" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(timeZone);
    String taskId = (String)request.getAttribute(WebAppConstants.TASK_ID);
    String taskState = (String)request.getAttribute(WebAppConstants.TASK_STATE);
    // button urls    
    String selfURL = self.getPageURL();
    String doneURL = null;
    
    String fromJobDetail = request.getParameter("fromJobDetail");
    String fromTaskDetail = request.getParameter("fromTaskDetail");
    String from = request.getParameter("from");
    if ("true".equals(fromJobDetail))
    {
    	doneURL = doneFromJob.getPageURL();
    }
    else if ("true".equals(fromTaskDetail))
    {
        doneURL = doneFromTask.getPageURL();
    }
    else if ("export".equals(from))
    {
    	doneURL = doneFromExportJobs.getPageURL();
    }
    else if ("localized".equals(from))
    {
        doneURL = doneFromLocalizedJobs.getPageURL();
    }
    else
    {
        doneURL = done.getPageURL();
    }
    
    String title = "Customer Download";
    StringBuffer sb = new StringBuffer();
    sb.append(title);
    sb.append(":  ");
    sb.append("Download Progress");
    String wizardTitle = sb.toString();

    SystemConfiguration sysConfig = SystemConfiguration.getInstance();
    boolean useSSL = sysConfig.getBooleanParameter(SystemConfigParamNames.USE_SSL);
    String httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
    if (useSSL == true)
    {
        httpProtocolToUse = WebAppConstants.PROTOCOL_HTTPS;
    }
    else
    {
        httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
    }
%>
<HTML>
<!-- This JSP is: envoy/administration/customer/downloadApplet.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = true;
var objectName = "Workflow";
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_customer_download_progress")%>";

function submitPage()
{
   leaveForm.action = "<%=doneURL%>" + "&action=done&taskId="+<%=taskId%>+"&state="+<%=taskState%>;
   leaveForm.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<FORM NAME="leaveForm" METHOD="POST">
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
<TR>
<TD ALIGN="<%=gridAlignment%>"> 
<SPAN CLASS="mainHeading">
    <%=wizardTitle%>&nbsp;
</SPAN>
</TD>
</TR>
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD WIDTH="100%" HEIGHT="400" ALIGN="LEFT">
            <%
            boolean isIE = request.getHeader("User-Agent").indexOf("MSIE")!=-1;
            //boolean isFirefox = request.getHeader("User-Agent").indexOf("Firefox")!=-1;
            %>
            <%if(isIE){%>
            <OBJECT classid="clsid:CAFEEFAC-0018-0000-0045-ABCDEFFEDCBA"
            WIDTH = 80% HEIGHT = 100% NAME = "FSV"  
            codebase="<%=httpProtocolToUse%>://javadl.sun.com/webapps/download/AutoDL?BundleId=107109">
            <PARAM NAME = CODE VALUE = "com.globalsight.everest.webapp.applet.admin.customer.download.DownloadApplet.class" >
            <%} else {%>                                    
            <%=bundle.getString("applet_need_java_support")%>
            <BR>
            <APPLET type="application/x-java-applet;jpi-version=1.8.0_45"
              height=100% width=80% pluginspage="<%=httpProtocolToUse%>://www.java.com/en/download/manual.jsp" code="com.globalsight.everest.webapp.applet.admin.customer.download.DownloadApplet.class">
            <%}%>
            <!-- PARAM NAME = CODEBASE VALUE = "classes/"-->            
            <PARAM NAME = "cache_option" VALUE = "Plugin" >
            <PARAM NAME = "cache_archive" VALUE = "applet/lib/customer.jar, applet/lib/ant.jar">
            <PARAM NAME = NAME VALUE = "FSV" >
            <PARAM NAME = "scriptable" VALUE="true">
            <PARAM NAME =  "rand" value=<%=session.getAttribute("UID_" + session.getId())%>>
            <PARAM NAME = "servletUrl" value="/globalsight/ControlServlet?activityName=customerView&applet=true&rand=">
            <PARAM NAME = "grid" value="com.globalsight.everest.webapp.applet.admin.customer.download.DownloadPanel">
            <PARAM NAME = "addToApplet" value="MainAppletWillAddThis">
            <PARAM NAME = "selfURL" value="<%=selfURL%>&applet=yes&initial=true&rand=">            
            <PARAM NAME = "nextURL" value="<%=doneURL%>&rand=">
            <PARAM NAME="zipFileName" VALUE="<%=request.getAttribute("zipFileName")%>">
            <PARAM NAME="zipFileSize" VALUE="<%=request.getAttribute("zipFileSize")%>">
            <PARAM NAME="lastModifiedTimes" VALUE="<%=request.getAttribute("lastModifiedTimes")%>">
            <PARAM NAME="fileNames" VALUE="<%=request.getAttribute("fileNames")%>">
            <PARAM NAME="zipUrl" VALUE="<%=request.getAttribute("zipUrl")%>">
            <PARAM NAME="jobNames" VALUE="<%=request.getAttribute("jobNames")%>">
            <PARAM NAME="locale" VALUE="<%=request.getAttribute("locale")%>">
            
            <% if(isIE){%>
            </OBJECT>
            <%} else {%>
            </APPLET>
            <%}%>
            
            <!--"END_CONVERTED_APPLET"-->
            
</TD>
</TR>
<TR>
<TD>
&nbsp;&nbsp;&nbsp;&nbsp;
<input TYPE="BUTTON" VALUE="<%=bundle.getString("lb_ok")%>" NAME="ok" ONCLICK=submitPage();>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>

