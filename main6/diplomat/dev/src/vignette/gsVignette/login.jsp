<%--
#   TEMPLATE NAME
#   -------------
# 	login
#
#   DESCRIPTION
#   -----------
# 	This template takes the form data from loginDialog and actually creates
#   the connection to the CMS.  The user is then redirected to the template that
#   invoked the login process.
#
#############################################################################
--%>
<%@ page errorPage="error.jsp"
	import="java.util.*, java.io.*, com.vignette.cms.client.beans.*"
%>
<jsp:useBean
		id="cms" class="com.vignette.cms.client.beans.CMS"
		scope = "session" />
<jsp:useBean
		id="sec" class="com.vignette.cms.client.beans.CMSSecurity"
		scope="session"/>
<%
	Properties connectionProp = (Properties) session.getAttribute("properties");
	String redirectURL = request.getParameter("redirectURL");
    if ( !cms.isConnected() )
    {
		cms.disconnect();
	}
    
	String host = request.getParameter("host");
    String portstr = request.getParameter("port");
    String user = request.getParameter("user");
    String pass = request.getParameter("pass");
	int port = Integer.parseInt(portstr);		
    try {
			   	cms.connect(sec, host, port, user, pass); 
		}
	catch (Exception e) {
		        session.setAttribute("msg","Login to Vignette Failed: " + e.getMessage());
				redirectURL = connectionProp.getProperty("login_page");
	}
			
	if (redirectURL == null)
	{
         session.setAttribute("msg","Redirect URL not provided");
  		 redirectURL = connectionProp.getProperty("login_page");
    }

	response.sendRedirect(redirectURL);
%>
