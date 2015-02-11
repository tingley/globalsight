<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
				 com.globalsight.everest.webapp.pagehandler.PageHandler,
				 java.util.ResourceBundle" session="true" %>
<jsp:useBean id="ok" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="ready" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<% 
	ResourceBundle bundle = PageHandler.getBundle(session);
	String okURL = ok.getPageURL();
   	String cancelURL = cancel.getPageURL();
	String readyURL = ready.getPageURL();
	String subTitle = bundle.getString("lb_my_jobs");
	String title = bundle.getString("lb_edit_workflow_instance");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = true;
var objectName = "Workflow";
var helpFile = "<%=bundle.getString("help_main")%>";
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" WIDTH="100%">
<TR>
<TD ALIGN="<%=gridAlignment%>">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
	<TD>
	<applet name="ModifyWorkflowApplet" code="com.globalsight.everest.webapp.applet.admin.workflows.ModifyWorkflowApplet" width="538" height="380" codebase="/globalsight/classes/">
	<param name=rand value=<%=session.getAttribute("UID_" + session.getId())%>>
	<param name=servletUrl value="/globalsight/ControlServlet?linkName=modify&pageName=WF2&applet=true&rand=">
	<param name=grid value="com.globalsight.everest.webapp.applet.admin.workflows.ModifyWorkflowPanel">
	<param name=cancelURL value="<%=cancelURL%>&rand=">
	<param name=doneURL value="<%=okURL%>&rand=">	
	<param name=readyURL value="<%=readyURL%>&rand=">	
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
