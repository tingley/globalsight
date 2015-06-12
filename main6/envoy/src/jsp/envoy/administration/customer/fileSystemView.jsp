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
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(timeZone);
    
    // button urls    
    String cancelURL = cancel.getPageURL();
    String doneURL = done.getPageURL();
    String previousURL = previous.getPageURL();
    
    String title = bundle.getString("lb_customer_upload");
    StringBuffer sb = new StringBuffer();
    sb.append(title);
    sb.append(":  ");
    sb.append(bundle.getString("lb_select_files_to_upload"));
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
<!-- This JSP is: envoy/administration/customer/fileSystemView.jsp -->
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
var objectName = "Selected Files";
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_customer_upload_select")%>";


</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
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
<TD WIDTH="100%" HEIGHT="470" ALIGN="LEFT">
            <%
            boolean isIE = request.getHeader("User-Agent").indexOf("MSIE")!=-1;
            boolean isFirefox = request.getHeader("User-Agent").indexOf("Firefox")!=-1;
            %>
            <%if(isIE){%>
            <OBJECT classid="clsid:CAFEEFAC-0018-0000-0045-ABCDEFFEDCBA"
            WIDTH = 90% HEIGHT = 95% NAME = "FSV"  
            codebase="<%=httpProtocolToUse%>://javadl.sun.com/webapps/download/AutoDL?BundleId=107109">
            <PARAM NAME = "CODE" VALUE = "com.globalsight.everest.webapp.applet.admin.customer.FileSystemApplet" >
            <%}  else {%>                                    
            <%=bundle.getString("applet_need_java_support")%>
            <BR>
            <APPLET type="application/x-java-applet;jpi-version=1.8.0_45"
              height=95% width=90% pluginspage="<%=httpProtocolToUse%>://www.java.com/en/download/manual.jsp" code="com.globalsight.everest.webapp.applet.admin.customer.FileSystemApplet.class">
            <%}%>
            <!-- PARAM NAME = CODEBASE VALUE = "classes/"-->
            <PARAM NAME = "cache_option" VALUE = "Plugin" >
            <PARAM NAME = "cache_archive" VALUE = "applet/lib/customer.jar,applet/lib/commons-codec-1.3.jar,applet/lib/commons-httpclient-3.0-rc2.jar,applet/lib/commons-logging.jar,applet/lib/ant.jar">
            <PARAM NAME = NAME VALUE = "FSV" >
            <PARAM NAME = "scriptable" VALUE="true">
            <PARAM NAME =  "rand" value=<%=session.getAttribute("UID_" + session.getId())%>>
            <PARAM NAME = "servletUrl" value="/globalsight/ControlServlet?linkName=next&pageName=CUSTOMERUP&applet=true&rand=">
            <PARAM NAME = "grid" value="com.globalsight.everest.webapp.applet.admin.customer.FileSystemPanel">
            <PARAM NAME = "addToApplet" value="MainAppletWillAddThis">
            <PARAM NAME = "uploadURL" value="<%=doneURL%>&rand=">
            <PARAM NAME = "cancelURL" value="<%=cancelURL%>&rand=">
            <PARAM NAME = "previousURL" value="<%=previousURL%>&rand=">
            <% if(isIE){%>
            </OBJECT>
            <%} else {%>
            </APPLET>
            <%}%>
            <!--"END_CONVERTED_APPLET"-->

</TD>
</TR>
</TABLE>
</BODY>
</HTML>
