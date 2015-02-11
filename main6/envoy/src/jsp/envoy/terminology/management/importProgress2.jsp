<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.util.progress.ProcessStatus2,
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
 ProcessStatus2 m_status_reindex =
  (ProcessStatus2)sessionMgr.getAttribute(WebAppConstants.TERMBASE_REINDEX_STATUS);
 String warning = (String)request.getParameter("warning");
boolean done   = m_status.getPercentage() >= 100;
int counter    = m_status.getCounter();
int percentage = m_status.getPercentage();
ArrayList messages = m_status.giveMessages();

boolean done_reindex = m_status_reindex.getDone();
int counter_reindex = m_status_reindex.getCounter();
int percentage_reindex = m_status_reindex.getPercentage();
int percentage2_reindex = m_status_reindex.getPercentage2();
String desc2_reindex = m_status_reindex.getDescription2();
ArrayList messages_reindex = m_status_reindex.giveMessages();
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
if("<%=done%>" != "false")
{
  parent.importProgressHidden();
  parent.indexingProgressDisplay();
<%
   if (messages_reindex != null)
   {
     for (int i = 0, max = messages_reindex.size(); i < max; i++)
     {
       String msg = (String)messages_reindex.get(i);
       out.print("parent.showProgressReindex(");
       out.print(counter_reindex);
       out.print(",");
       out.print(percentage_reindex);
       out.print(",'");
       out.print(EditUtil.toJavascript(msg));
       out.println("');");
     }
   }
   else
   {
%>
   parent.showProgressReindex(<%=counter_reindex%>, <%=percentage_reindex%>, "");
<%
   }
%>

   parent.showProgress2Reindex('<%=EditUtil.toJavascript(desc2_reindex)%>',
    <%=percentage2_reindex%>);
}
  // refresh frame
  if ("<%=done_reindex%>" == "false" && "<%=warning%>" == "false")
  {
    window.setTimeout(document.location.reload(), 5000);
  }
  else
  {
    // when really done, refresh the last messages and percentages
    parent.showProgressReindex(<%=m_status_reindex.getCounter()%>, <%=m_status_reindex.getPercentage()%>, "");
    parent.showProgress2Reindex('<%=EditUtil.toJavascript(m_status_reindex.getDescription2())%>',
      <%=m_status_reindex.getPercentage2()%>);
    parent.done();
  }
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()"></BODY>
</HTML>
