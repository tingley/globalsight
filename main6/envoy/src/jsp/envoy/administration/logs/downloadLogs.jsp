
<%@page import="com.globalsight.util.StringUtil"%>
<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="com.globalsight.everest.webapp.WebAppConstants,
		        com.globalsight.everest.webapp.pagehandler.administration.logs.ViewLogsHelper,
				com.globalsight.everest.util.system.SystemConfiguration,
				com.globalsight.everest.util.system.SystemConfigParamNames,
				java.util.ArrayList,
				java.io.File" session="true" 
%><%

String downloadOption = request.getParameter("downloadOption");
String systemLogDirectory = SystemConfiguration.getInstance().getStringParameter(
        SystemConfigParamNames.SYSTEM_LOGGING_DIRECTORY);
systemLogDirectory += File.separator;
ViewLogsHelper helper = new ViewLogsHelper(request, response);
ArrayList<String> logs = new ArrayList<String>();
String tmp = "";
tmp = request.getParameter("globalsight");
if (!StringUtil.isEmpty(tmp))
    logs.add(tmp);
tmp = request.getParameter("activity");
if (!StringUtil.isEmpty(tmp))
    logs.add(tmp);
tmp = request.getParameter("operation");
if (!StringUtil.isEmpty(tmp))
    logs.add(tmp);
tmp = request.getParameter("webservices");
if (!StringUtil.isEmpty(tmp))
    logs.add(tmp);
tmp = request.getParameter("jboss");
if (!StringUtil.isEmpty(tmp))
    logs.add(tmp);

response.setHeader("Content-Disposition","attachment; filename=GSSystemLogs.zip" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public");
HttpSession userSession = request.getSession(false);

if ("full".equals(downloadOption)) {
    helper.doDownloadFullLogs(systemLogDirectory, logs);
} else if ("part".equals(downloadOption)) {
    //download part of log files with specified time range
    String start = request.getParameter("fromDate") + " " + request.getParameter("fromHour") + ":" + request.getParameter("fromMinute");
    String end = request.getParameter("toDate") + " " + request.getParameter("toHour") + ":" + request.getParameter("toMinute");
    helper.doDownloadPartLogs(systemLogDirectory, logs, start, end);
} else if ("days".equals(downloadOption)) {
    String days = request.getParameter("days");
    helper.doDownloadFullLogs(systemLogDirectory, logs, days);
}   

out.clear();
out = pageContext.pushBody();
%>