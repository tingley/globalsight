<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler, 
                com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
                com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
                com.globalsight.everest.taskmanager.TaskAssignee,
                com.globalsight.everest.taskmanager.TaskInfo,
                com.globalsight.everest.workflow.WfTaskInfo,
                com.globalsight.everest.foundation.Timestamp,
                com.globalsight.calendar.CalendarManagerLocal,
                java.util.ResourceBundle,
                java.util.TimeZone,
                java.util.ArrayList"
		session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ready" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String workflowTemplateInfoId = 
      (String)sessionMgr.getAttribute(WorkflowTemplateConstants.TEMPLATE_ID);
    
    String workflowName = (String)sessionMgr.getAttribute(
                           WorkflowTemplateConstants.WF_INSTANCE_NAME);
    String wfIdParam = (String)request.getAttribute(JobManagementHandler.WF_ID);
    String jobIdParam = (String)request.getAttribute(JobManagementHandler.JOB_ID);
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(timeZone);
    boolean edit = true; // needed for activities.jspIncl
    
    // button urls    
   	String cancelURL = cancel.getPageURL();
    if(jobIdParam != null && jobIdParam != ""){
    	cancelURL += "&" + JobManagementHandler.JOB_ID + "=" + jobIdParam;
    }
    String viewUrl = self.getPageURL() + "&" +
                JobManagementHandler.WF_ID + "=" + wfIdParam + "&" +
                JobManagementHandler.JOB_ID + "=" + jobIdParam;
    String modifyURL = viewUrl;
	  String readyURL =  viewUrl;
    
    String title = bundle.getString("lb_graphical_workflow");
    String lb_previous = bundle.getString("lb_previous");
    String lb_close = bundle.getString("lb_close");

    StringBuffer sb = new StringBuffer();
    sb.append(bundle.getString("lb_graphical_workflow_link"));
    sb.append(":  ");
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
<!-- This JSP is: envoy/administration/workflow/graphicalWfInstance.jsp -->
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
var helpFile = "<%=bundle.getString("help_workflow_instance_graphical")%>";

function onClose() {
	var confirmed = true;
	
    if(GPUI.getIsModified()) {
        if(confirm('<%=bundle.getString("msg_wf_modify_confirm")%>')) {
        	confirmed = true;
        }
        else
        {
        	confirmed = false;
        }
    }
    if (confirmed)
   	{
    	location.replace('<%=cancelURL%>');
   	}
}
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
<SPAN CLASS="standardTextBold">
    <%=workflowName%>
</SPAN>
</TD>
</TR>
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD WIDTH="100%" HEIGHT="600" ALIGN="LEFT">                  
                 <!--"CONVERTED_APPLET"-->
				<!-- CONVERTER VERSION 1.3 -->
            <%
            boolean isIE = request.getHeader("User-Agent").indexOf("MSIE")!=-1;
            boolean isFirefox = request.getHeader("User-Agent").indexOf("Firefox")!=-1;
            %>
            <%if(isIE){%>
            <OBJECT classid="clsid:CAFEEFAC-0018-0000-0045-ABCDEFFEDCBA"
            WIDTH = 80% HEIGHT = 95% NAME = "GPUI"  id = "GPUI"
            codebase="<%=httpProtocolToUse%>://javadl.sun.com/webapps/download/AutoDL?BundleId=107109">            
            <PARAM NAME = CODE VALUE = "com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview.GVApplet.class" >
            <%}  else {%>
            <%=bundle.getString("applet_need_java_support")%>
            <BR>
            <APPLET type="application/x-java-applet;jpi-version=1.8.0_45" 
            		NAME="GPUI" id="GPUI" height=95% width=80% 
            		pluginspage="<%=httpProtocolToUse%>://www.java.com/en/download/manual.jsp" code="com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview.GVApplet.class">
            <%}%>
            <!-- PARAM NAME = CODEBASE VALUE = "classes/"-->
            <PARAM NAME = "cache_option" VALUE = "Plugin" >
            <PARAM NAME = "cache_archive" VALUE = "/globalsight/applet/lib/graphicalWf.jar">
            <PARAM NAME = NAME VALUE = "GPUI" >
            <PARAM NAME = "scriptable" VALUE="true">
            <PARAM NAME =  "rand" value=<%=session.getAttribute("UID_" + session.getId())%>>
            <PARAM NAME = "servletUrl" value="/globalsight/ControlServlet?linkName=modify&pageName=WF2&applet=true&rand=">
            <PARAM NAME = "grid" value="com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview.GVPane">
            <PARAM NAME = "cancelURL" value="<%=cancelURL%>&applet=yes&initial=true&rand=">
            <PARAM NAME = "modifyURL" value="<%=modifyURL%>&applet=yes&initial=true&rand=">
            <PARAM NAME = "readyURL" value="<%=readyURL%>&rand=">
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
<% if (b_calendaring) { %>
<%@ include file="../../projects/workflows/activities.jspIncl" %>
<% } %>
</TD>
</TR>
<TR>
<TD>
  <INPUT TYPE="BUTTON" NAME="<%=lb_close%>" VALUE="<%=lb_close%>" 
  onclick="onClose()"> 
</TD>
</TR>
</TABLE>

<form name="profileCancel" action="<%=cancelURL%>" method="post">
    <INPUT TYPE="HIDDEN" NAME="Cancel" value="Cancel">
</form>
	                                     
</BODY>
</HTML>
