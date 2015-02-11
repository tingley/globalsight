<%@ page 
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.util.progress.ProcessMonitor,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
ProcessMonitor monitor =
  (ProcessMonitor)sessionMgr.getAttribute(WebAppConstants.TM_REINDEXER);

int counter    = 0;
int percentage = 0;
String message = "";
boolean done = false;
boolean error = false;
if (monitor != null)
{
    counter = monitor.getCounter();
    percentage = monitor.getPercentage();
    done = monitor.hasFinished();
    error = monitor.isError();
    message = monitor.getReplacingMessage();
}
%>
<progress>
  <counter><%=counter%></counter>
  <percentage><%=percentage%></percentage>
  <done><%=done%></done>
  <error><%=error%></error>
  <replacingMessage><%=message%></replacingMessage>
</progress>
