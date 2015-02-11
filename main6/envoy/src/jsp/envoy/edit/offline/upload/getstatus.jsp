<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.util.edit.EditUtil,
        java.util.ArrayList,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        java.io.IOException"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

ProcessStatus m_status =
  (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.UPLOAD_STATUS);

boolean initDone = false;
Boolean done = (Boolean)sessionMgr.getAttribute(OfflineConstants.UPLOAD_SUCCESS);
int counter = 0;
int percentage = 0;
ArrayList messages = null; 
int upldState = 1;

if(m_status != null)
{
     initDone   = m_status.getPercentage() >= 100;
     counter    = m_status.getCounter();
     percentage = m_status.getPercentage();
     messages = m_status.giveMessages();
}

 if (initDone)
 {
    upldState = (m_status.getResults() == null) ? 2 : 3;
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
            if(done == null) 
            {
%>
                parent.showProgress(<%=counter%>, <%=percentage%>, "");
<%
            }
       }
%>

<% if(done == null) 
   {
        // Call parent.done() once. This results in the UploadPageHandler 
        // setting the final pass/fail result in the session. At that point we 
        // are really done and should not call parent.done() again.
%>  
      // refresh frame
      if ("<%=initDone%>" == "false")
      {
        window.setTimeout("doRefresh()", 5000, "JavaScript");
      }
      else // init done
      {
        parent.done(<%=upldState%>);
      }     
<%
  }  
%>

}

</SCRIPT>
</HEAD>
<BODY onload="doLoad()"></BODY>
</HTML>
