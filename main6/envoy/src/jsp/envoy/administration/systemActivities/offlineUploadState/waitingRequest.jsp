<%@page import="com.globalsight.everest.util.comparator.OfflineUploadRequestComparator"%>
<%@page import="com.globalsight.everest.util.comparator.ExportRequestComparator"%>
<%@page import="com.globalsight.everest.foundation.SearchCriteriaParameters"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         java.util.ResourceBundle,
         java.util.Date,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.CreateRequestComparator,
         java.util.Enumeration"
         session="true"%>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="importing" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="exporting" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="loginBlock" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />
<jsp:useBean id="running" class="com.globalsight.everest.webapp.javabean.NavigationBean"
             scope="request" />             
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    String selfUrl = self.getPageURL();
    String importingUrl = importing.getPageURL();
    String exportingUrl = exporting.getPageURL();
    String jobCreationStatus = bundle
            .getString("lb_job_creation_status");
%>
<HTML>
    <HEAD>
        <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
        <TITLE><%=bundle.getString("lb_job_export_status")%></TITLE>
        <script SRC="/globalsight/includes/utilityScripts.js"></script>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/formvalidation.js"></SCRIPT>
        <script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
        <%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
        <%@ include file="/envoy/common/warning.jspIncl"%>
        <link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
        <script>
            var needWarning = false;
            var objectName = "";
            var guideNode = "import";
            var helpFile = '<%=bundle.getString("help_offline_upload_waiting_requests")%>';
            function handleSelectAll() {
                if (MyForm && MyForm.selectAll) {
                    if (MyForm.selectAll.checked) {
                        checkAll('MyForm');
                        setButtonState();
                    }
                    else {
                        clearAll('MyForm');
                        setButtonState();
                    }
                }
            }

            function cancelFiles() {
                var s = $('input:checkbox[name=key]:checked');
                if (s.length == 0)
                {
                    alert("<%=bundle.getString("jsmsg_please_select_a_row")%>");
                    return false;
                }

                if (confirm('<%=bundle.getString("msg_remove_creation_request")%>')) {
                    MyForm.action = "<%=selfUrl%>&action=remove";
                    MyForm.submit();
                }
            }
            
            function changePageSize(size) {
                MyForm.action = "<%=selfUrl%>&numOfPageSize=" + size;
                MyForm.submit();
            }
        </script>
    </HEAD>
    <BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
        <%@ include file="/envoy/common/header.jspIncl"%>
        <%@ include file="/envoy/common/navigation.jspIncl"%>
        <%@ include file="/envoy/wizards/guides.jspIncl"%>
        <STYLE>
            .list {
                border: 1px solid<%=skin.getProperty("skin.list.borderColor")%>;
            }

            .headerCell {
                padding-right: 10px;
                padding-top: 2px;
                padding-bottom: 2px;
            }
        </STYLE>
        <DIV ID="contentLayer"
             STYLE="POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
            <SPAN CLASS="mainHeading"> <%=bundle.getString("lb_system_activities")%></SPAN> <br> <span
                class="standardText"><br><%=bundle.getString("lb_system_activities_help")%></span>
            <div style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=importingUrl%>"> <%=bundle.getString("lb_job_creation_status")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=exporting.getPageURL()%>"> <%=bundle.getString("lb_job_export_status")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                         <td width="2"></td>
                        <td class="tableHeadingListOn">
                            <img src="/globalsight/images/tab_left_blue.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%= selfUrl%>"> <%=bundle.getString("lb_offline_upload_status")%></a> <img
                               src="/globalsight/images/tab_right_blue.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=loginBlock.getPageURL()%>"> <%=bundle.getString("lb_login_block_status")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                    </tr>
                </table>
            </div>
            <div style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <td id="jobWorkflowsTab" class="tableHeadingListOn">
                            <img src="/globalsight/images/tab_left_blue.gif" border="0" />
                            <a class="sortHREFWhite"
                               href="<%=selfUrl%>"> <%=bundle.getString("lb_job_create_wait")%></a> <img
                               src="/globalsight/images/tab_right_blue.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <td id="jobWorkflowsTab" class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite"
                               href="<%=running.getPageURL()%>"> <%=bundle.getString("lb_offline_upload_requests")%></a> <img
                               src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                    </tr>
                </table>
            </div>
            <div class="standardText">
                <amb:tableNav bean="requestDefine" key="requestDefineKey" pageUrl="self" />
                <div align='right'>
                    <c:out value="${tableNav}" escapeXml="false"></c:out>
                    </div>
                    <FORM NAME="MyForm" METHOD="POST">
                    <amb:table bean="requestDefine" id="requestVo"
                               key="requestDefineKey"
                               dataClass="com.globalsight.everest.webapp.pagehandler.administration.systemActivities.offlineUploadState.Vo"
                               pageUrl="self"
                               emptyTableMsg="msg_waiting_request_none" >

                        <amb:column label="lb_company" sortBy="<%=OfflineUploadRequestComparator.COMPANY%>" >
                            ${requestVo.company}
                        </amb:column>
                        <amb:column label="lb_file" sortBy="<%=OfflineUploadRequestComparator.FILE_NAME%>" style="text-align: left; word-break: break-all; word-wrap: break-word;">
                            ${requestVo.fileName}
                        </amb:column>
                         <amb:column label="lb_size" sortBy="<%=OfflineUploadRequestComparator.FILE_SIZE%>" >
                            ${requestVo.fileSize}
                        </amb:column>
                        <amb:column label="lb_username" sortBy="<%=OfflineUploadRequestComparator.USER%>" >
                            ${requestVo.user}
                        </amb:column>
                       
                    </amb:table>
                   
                   <div align='right' style="padding-top: 5px;">
                    <c:out value="${tableNav2}" escapeXml="false" ></c:out>
                    <c:out value="${tableNav}" escapeXml="false" ></c:out>
                     <amb:tableNav bean="requestDefine" key="requestDefineKey" pageUrl="self" scope="10,20,50" />
                  </div>
                </FORM>
            </DIV>
    </BODY>
</HTML>
