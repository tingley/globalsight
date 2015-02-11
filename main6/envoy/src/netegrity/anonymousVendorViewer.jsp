<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<% 
    //This page lives on the WB web server and is protected by netegrity

    //Edit below to set the base URL that can be used to connect
    //to GlobalSight. This includes the protocol, hostname, and port that
    //users (possibly outside users) use to access Ambassador.
    //an example value is "http://gsserver.worldbank.com:7001"
   String urlBase = "http://dragade:7001";


   /*** DO NOT EDIT BELOW THIS LINE ***/
      
   response.setHeader("Pragma", "no-cache"); //HTTP 1.0
   response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
   response.addHeader("Cache-control", "no-store"); // tell proxy not to cache
   response.addHeader("Cache-control", "max-age=0"); // stale right away
%>
<HTML>
<HEAD>
<TITLE>GlobalSight Anonymous Vendor Viewer Login Page</TITLE>
<BODY ONLOAD="loginForm.submit()">
    <FORM NAME="loginForm" ACTION="<%=urlBase%>/globalsight/ControlServlet?linkName=dummyLink&pageName=LOG1" METHOD="post">
        <input type="hidden" name="nameField" value="anonymous_vendor_viewer">
        <input TYPE="hidden" NAME="passwordField" VALUE="password">
        <input type="hidden" name="uiLocale" value="en_US">
</FORM>
<EM>Please wait. The system is logging you into GlobalSight's Vendor Viewer.</EM>
</BODY>    
</HTML>

