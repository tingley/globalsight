<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler, 
                com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
                java.util.ResourceBundle"
		session="true"
%>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);      
    Long workflowTemplateInfoId = 
      (Long)sessionMgr.getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID);      
    String iflowTemplateId = 
      (String)sessionMgr.getAttribute(WorkflowTemplateConstants.TEMPLATE_ID);
    
    String actionType = (String)sessionMgr.getAttribute(WorkflowTemplateConstants.ACTION);
    boolean isEdit = actionType != null && 
      actionType.equals(WorkflowTemplateConstants.EDIT_ACTION);
    String actionParam = isEdit ? 
    ("&" + WorkflowTemplateConstants.ACTION + "=" 
    + WorkflowTemplateConstants.EDIT_ACTION) :
    "";
    
    // button urls    
   	String cancelURL = save.getPageURL() + "&" 
                     + WorkflowTemplateConstants.ACTION 
                     + "=" + WorkflowTemplateConstants.CANCEL_ACTION;
    String previousURL = previous.getPageURL() + (workflowTemplateInfoId == null ? "" : 
                     ("&" + WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID + "=" 
                     + workflowTemplateInfoId)) + actionParam;
    String modifyURL = save.getPageURL();
    
    String title = bundle.getString("lb_graphical_workflow");
    String lb_previous = bundle.getString("lb_previous");
    String lb_cancel = bundle.getString("lb_cancel");
    
	String wizardTitle = (String)bundle.getString("lb_graphical_workflow_link");    

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
<!-- This JSP is: envoy/administration/workflow/graphicalWfTemplate.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_workflow") %>";
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_workflow_graphical")%>";

function cancelForm()
{
    if(GPUI.getIsModified()) {
        if(confirm('<%=bundle.getString("msg_wf_modify_confirm")%>')) {
            GPUI.saveWorkflow();
        }
    }

    if (confirmJump())
    {
    	if (document.layers) theForm = document.contentLayer.document.templateCancel;
    	else theForm = document.all.templateCancel;
    	theForm.submit();
    }

}

function previousForm() {
    if(GPUI.getIsModified()) {
        if(confirm('<%=bundle.getString("msg_wf_modify_confirm")%>')) {
            GPUI.saveWorkflow();
        }
    }
    
    if (confirmJump())
    {
    	location.replace('<%=previousURL%>');
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
    <%=wizardTitle%>
</SPAN>
</TD>
</TR>
<TR>
<TD>&nbsp;</TD>
</TR>
<TR>
<TD WIDTH="100%" HEIGHT="600" ALIGN="CENTER">                
                 <!--"CONVERTED_APPLET"-->
				<!-- CONVERTER VERSION 1.3 -->
            <%
            boolean isIE = request.getHeader("User-Agent").indexOf("MSIE")!=-1;
            boolean isFirefox = request.getHeader("User-Agent").indexOf("Firefox")!=-1;
            %>
            <%if(isIE){%>
            <OBJECT classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
            WIDTH = 80% HEIGHT = 80% NAME = "GPUI"  id="GPUI"
            codebase="<%=httpProtocolToUse%>://java.sun.com/update/1.6.0/jinstall-6-windows-i586.cab#Version=1,6">
            <%}  else {%>           
            <%=bundle.getString("applet_need_java_support")%>
            <BR>
            <APPLET type="application/x-java-applet;jpi-version=1.6" id="GPUI"
              height=80% width=80% pluginspage="http://java.sun.com/products/plugin/index.html#download">
            <%}%>
            <PARAM NAME = CODE VALUE = "com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview.GVApplet.class" >
            <!--PARAM NAME = CODEBASE VALUE = "classes/"-->
            
            <PARAM NAME = "cache_option" VALUE = "Plugin" >
            <PARAM NAME = "cache_archive" VALUE = "/globalsight/applet/lib/graphicalWf.jar,/globalsight/applet/lib/CoffeeTableAWT.jar">
            <PARAM NAME = NAME VALUE = "GPUI" >
            <PARAM NAME = "type" VALUE="application/x-java-applet;version=1.6">
            <PARAM NAME = "scriptable" VALUE="true">
            <PARAM NAME =  "rand" value=<%=session.getAttribute("UID_" + session.getId())%>>
            <PARAM NAME = "servletUrl" value="/globalsight/ControlServlet?linkName=nextPage&pageName=NBWFT&applet=true&rand=">
            <PARAM NAME = "grid" value="com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview.GVPane">
            <PARAM NAME = "cancelURL" value="<%=cancelURL%>&applet=yes&initial=true&rand=">
            <PARAM NAME = "previousURL" value="<%=previousURL%>&applet=yes&initial=true&rand=">
            <PARAM NAME = "modifyURL" value="<%=modifyURL%>&applet=yes&initial=true&rand=">
            <% if(isIE){%>
            </OBJECT>
            <%} else {%>
            </APPLET>
            <%}%>
            
            <!--"END_CONVERTED_APPLET"-->
            <P>
            
            <INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" 
                ONCLICK="cancelForm()">  
            <INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" 
                ONCLICK="previousForm()">   

</TD>
</TR>
</TABLE>

<form name="templateCancel" action="<%=cancelURL%>" method="post">
<INPUT TYPE="HIDDEN" NAME="Cancel" value="Cancel">
</form>

            
	                               
 </DIV>                           
	                                     
</BODY>
</HTML>


