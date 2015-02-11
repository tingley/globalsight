<%@ page isErrorPage="true"
    contentType="text/html; charset=UTF-8"
    import="com.globalsight.everest.servlet.ControlServlet"
    
%>
<%@ page import="java.io.*"%>
<CENTER><H1>Error:</H1></CENTER>
<pre>
<%
  ControlServlet.handleJSPException(exception);

  // Process Error Message
  String errorMsg = (String) request.getAttribute("JSPErrorMsg");
  if (errorMsg != null)
  {
	out.print("<b>Error occurred:</b>  " + errorMsg);
  } 
  
  // Caught Exception Stack
  Exception e = (Exception) request.getAttribute("JSPCaughtException");
  if (e != null) 
  {
    out.print("&nbsp<br>&nbsp<br><b>Exception Stack:</b><br>");
	e.printStackTrace(new PrintWriter(out)); 
  }
  
  // Un-Caught Exception Stack
  if (exception != null) 
  {
    exception.printStackTrace(new PrintWriter(out)); 
  }
%>
</pre>
