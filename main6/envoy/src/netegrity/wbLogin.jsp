<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<% 
    //This page lives on the WB web server and is protected by netegrity

    //Edit below to set the base URL that can be used to connect
    //to System4. This includes the protocol, hostname, and port that
    //users (possibly outside users) use to access System4.
    //an example value is "http://gsserver.worldbank.com:7001"
   String urlBase = "http://ragade:7001";


   /*** DO NOT EDIT BELOW THIS LINE ***/
      
   //the username is passed in through the HTTP header
   //from Netegrity using the header parameter "HTTP_SHORTNAME"
   String username = (String) request.getHeader("HTTP_SHORTNAME");
   
   response.setHeader("Pragma", "no-cache"); //HTTP 1.0
   response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
   response.addHeader("Cache-control", "no-store"); // tell proxy not to cache
   response.addHeader("Cache-control", "max-age=0"); // stale right away
%>
<HTML>
<HEAD>
<TITLE>System4 WorldBank-Netegrity Login Page</TITLE>
<BODY ONLOAD="loginForm.submit()">
    <FORM NAME="loginForm" ACTION="<%=urlBase%>/globalsight/ControlServlet?linkName=dummyLink&pageName=LOG1" METHOD="post">
        <input type="hidden" name="nameField" value="<%=username%>">
        <input type="hidden" name="uiLocale" value="en_US">
</FORM>
<EM>Please wait. The system is logging you into GlobalSight System4.</EM>
</BODY>    
</HTML>

