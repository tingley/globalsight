<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.util.edit.EditUtil,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        java.io.IOException"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

ProcessStatus m_status =
  (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.TERMBASE_STATUS);
boolean done   = m_status.getPercentage() >= 100;
int counter    = m_status.getCounter();
int percentage = m_status.getPercentage();
ArrayList messages = m_status.giveMessages();
%>
<HTML>
<HEAD>
<SCRIPT>
function doRefresh()
{
  window.location = window.location;
}

function doLoad()
{
<%
   if (messages != null)
   {
     for (int i = 0, max = messages.size(); i < max; i++)
     {
       String msg = (String)messages.get(i);
       out.print("parent.showProgress(");
       out.print(counter);
       out.print(",");
       out.print(percentage);
       out.print(",'");
       out.print(EditUtil.toJavascript(msg));
       out.println("');");
     }
   }
   else
   {
%>
  parent.showProgress(<%=counter%>, <%=percentage%>, "");
<%
   }
%>

  // refresh frame
  if ("<%=done%>" == "false")
  {
    window.setTimeout("doRefresh()", 5000, "JavaScript");
  }
  else
  {
    parent.done();
  }
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()"></BODY>
</HTML>
