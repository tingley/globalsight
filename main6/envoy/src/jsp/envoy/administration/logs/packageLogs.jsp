<%@ page language="java" contentType="text/html; charset=UTF-8"
        pageEncoding="UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.pagehandler.administration.logs.ViewLogsHelper,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                java.util.ArrayList,
                java.io.File" session="true" 
    %>

<%
String downloadOption = request.getParameter("downloadOption");
String systemLogDirectory = SystemConfiguration.getInstance().getStringParameter(
        SystemConfigParamNames.SYSTEM_LOGGING_DIRECTORY);
systemLogDirectory += File.separator;
ViewLogsHelper helper = new ViewLogsHelper(request, response);
ArrayList<String> logs = new ArrayList<String>();
String tmp = "";
tmp = request.getParameter("globalsight");
if (tmp != null && !tmp.trim().equals(""))
    logs.add(tmp);
tmp = request.getParameter("activity");
if (tmp != null && !tmp.trim().equals(""))
    logs.add(tmp);

if ("zipLogs".equals(downloadOption)) {
    String days = request.getParameter("logDays");
    helper.packageLogs(systemLogDirectory, logs, days);
    
    out.print("<script language=\"JavaScript\">");
    out.println("alert('Old log files are packaged successfully.');");
    out.print("</script>");
}
%>