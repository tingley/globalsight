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
        java.util.ArrayList,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        java.io.File,
        java.io.IOException"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

int percentage = 0;
boolean done   = false;
boolean errorOccur = false;
int counter    = 0;
ArrayList messages = null;
File result  = null;
boolean canDownload = false;
String onload="";

ProcessStatus status = null;
if (sessionMgr != null)
{
	status = (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.DOWNLOAD_STATUS);
}

if (status !=null)
{
    if (status.isMultiTasks())
    {
        percentage = status.getTaskPercentage();
    }
    else
    {
 		percentage = status.getPercentage();
    }
    
    done = percentage >= 100;
    messages = status.giveMessages();
    errorOccur = status.isErrorOccured();
    counter = status.getCounter();
    if (counter>1)
      	 counter--;
    result = status.isMultiTasks() ? null : (File)status.getResults();
    canDownload = (result != null && result.exists());
    onload="onload='doLoad()'";
}
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

  <% if (errorOccur) { %>
  	parent.done(eval("<%= canDownload %>"));
  <% } %>

  // refresh frame
  if ("<%=done%>" == "false" || "<%=canDownload%>" == "false")
  {
    window.setTimeout("doRefresh()", 5000, "JavaScript");
  }
  else
  {
    parent.done(eval("<%= canDownload %>"));
  }
}
</SCRIPT>
</HEAD>
<BODY <%=onload%>></BODY>
</HTML>

