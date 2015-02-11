<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         java.text.SimpleDateFormat,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.util.system.LogInfo,
         com.globalsight.everest.webapp.pagehandler.administration.logs.OperationLogMainHandler,
         com.globalsight.everest.util.comparator.LogInfoComparator,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.company.CompanyWrapper,
         java.util.ArrayList, java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="operationLog" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfURL = self.getPageURL();
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String title= bundle.getString("lb_logs_operation");
    String helperText = bundle.getString("helper_text_operation");
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

    String objectTypeFilterValue = (String) sessionMgr.getAttribute(OperationLogMainHandler.FILTER_OBJECT_TYPE);
    if (objectTypeFilterValue == null || objectTypeFilterValue.trim().length() == 0)
    {
        objectTypeFilterValue = "";
    }
    
    String eventTypeFilterValue = (String) sessionMgr.getAttribute(OperationLogMainHandler.FILTER_EVENT_TYPE);
    if (eventTypeFilterValue == null || eventTypeFilterValue.trim().length() == 0)
    {
        eventTypeFilterValue = "";
    }
    
    String operatorFilterValue = (String) sessionMgr.getAttribute(OperationLogMainHandler.FILTER_OPERATOR);
    if (operatorFilterValue == null || operatorFilterValue.trim().length() == 0)
    {
        operatorFilterValue = "";
    }
    
    String messageFilterValue = (String) sessionMgr.getAttribute(OperationLogMainHandler.FILTER_MESSAGE);
    if (messageFilterValue == null || messageFilterValue.trim().length() == 0)
    {
        messageFilterValue = "";
    }
    
    String companyNameFilterValue = (String) sessionMgr.getAttribute(OperationLogMainHandler.FILTER_COMPANY_NAME);
    if (companyNameFilterValue == null || companyNameFilterValue.trim().length() == 0)
    {
        companyNameFilterValue = "";
    }
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "operationLog";
var helpFile = "<%=bundle.getString("help_operation_log_main_screen")%>";

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	operationLogForm.action = "<%=selfURL%>";
    	operationLogForm.submit();
    }
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="operationLogForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 width="100%" class="standardText">
				<tr valign="top">
					<td align="right"><amb:tableNav bean="operationLog"
							key="<%=OperationLogMainHandler.LOGINFO_KEY%>" pageUrl="self" /></td>
				</tr>
				<tr>
					<td><amb:table bean="operationLog" id="logInfo"
							key="<%=OperationLogMainHandler.LOGINFO_KEY%>"
							dataClass="com.globalsight.util.system.LogInfo" pageUrl="self"
							hasFilter="true" emptyTableMsg="msg_no_message">
							<amb:column label="" width="1%"
							sortBy="<%=LogInfoComparator.ID%>">
							</amb:column>
							<amb:column label="lb_log_object_type" width="15%"
								sortBy="<%=LogInfoComparator.OBJECTTYPE%>" filter="<%=OperationLogMainHandler.FILTER_OBJECT_TYPE%>" filterValue="<%=objectTypeFilterValue%>">
							   	<%=logInfo.getObjectType()%>
							</amb:column>
							<amb:column label="lb_log_event_type" width="12%"
								sortBy="<%=LogInfoComparator.EVENTTYPE%>" filter="<%=OperationLogMainHandler.FILTER_EVENT_TYPE%>" filterValue="<%=eventTypeFilterValue%>">
							    <%=logInfo.getEventType()%>
							</amb:column>
							<amb:column label="lb_log_object_id" width="10%"
								sortBy="<%=LogInfoComparator.OBJECTID%>">
							    <%=logInfo.getObjectId()%>
							</amb:column>
							<amb:column label="lb_log_operator" width="13%"
								sortBy="<%=LogInfoComparator.OPERATOR%>" filter="<%=OperationLogMainHandler.FILTER_OPERATOR%>" filterValue="<%=operatorFilterValue%>">
							   	<%=logInfo.getOperator()%>
							</amb:column>
							<amb:column label="lb_log_operate_time" width="15%"
								sortBy="<%=LogInfoComparator.OPERATETIME%>">
								<%=(new SimpleDateFormat("M/d/yy")).format(logInfo.getOperateTime())%>
							</amb:column>
							<amb:column label="lb_log_message" width="24%"
								sortBy="<%=LogInfoComparator.MESSAGE%>" filter="<%=OperationLogMainHandler.FILTER_MESSAGE%>" filterValue="<%=messageFilterValue%>">
								<%=logInfo.getMessage()%>
							</amb:column>
							<% if (isSuperAdmin) { %>
							<amb:column label="lb_company_name" width="10%"
								sortBy="<%=LogInfoComparator.COMPANY_NAME%>" filter="<%=OperationLogMainHandler.FILTER_COMPANY_NAME%>" filterValue="<%=companyNameFilterValue%>">
								<%=CompanyWrapper.getCompanyNameById(logInfo.getCompanyId())%>
							</amb:column>
							<% } %>
						</amb:table></td>
				</tr>
				<tr valign="top">
					<td align="right"><amb:tableNav bean="operationLog"
							key="<%=OperationLogMainHandler.LOGINFO_KEY%>" pageUrl="self"
							scope="20,50,100,200" showTotalCount="false" /></td>
				</tr>
			</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
