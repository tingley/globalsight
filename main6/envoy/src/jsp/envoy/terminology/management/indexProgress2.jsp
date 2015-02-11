<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener2,
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

ProcessStatus2 m_status =
  (ProcessStatus2)sessionMgr.getAttribute(WebAppConstants.TERMBASE_STATUS);
boolean done    = m_status.getDone();
int counter     = m_status.getCounter();
int percentage  = m_status.getPercentage();
int percentage2 = m_status.getPercentage2();
String desc2    = m_status.getDescription2();
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

  parent.showProgress2('<%=EditUtil.toJavascript(desc2)%>',
    <%=percentage2%>);

  // refresh frame
  if ("<%=done%>" == "false")
  {
    window.setTimeout("doRefresh()", 5000, "JavaScript");
  }
  else
  {
    // when really done, refresh the last messages and percentages
    parent.showProgress(<%=m_status.getCounter()%>, <%=m_status.getPercentage()%>, "");
    parent.showProgress2('<%=EditUtil.toJavascript(m_status.getDescription2())%>',
      <%=m_status.getPercentage2()%>);
    parent.done();
  }
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()"></BODY>
</HTML>
