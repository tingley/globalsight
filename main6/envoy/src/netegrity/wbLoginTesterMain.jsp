<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<% 
   response.setHeader("Pragma", "no-cache"); //HTTP 1.0
   response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
   response.addHeader("Cache-control", "no-store"); // tell proxy not to cache
   response.addHeader("Cache-control", "max-age=0"); // stale right away
%>
<!-- Simulates Netegrity Login Page -->
<HTML>
<HEAD>
<TITLE>System4 WorldBank/Netegrity Login Tester Page</TITLE>
<BODY>
<CENTER><H2>Netegrity Sign-In</H2>
    <FORM NAME="netegrityForm" ACTION="wbLoginTester.jsp" METHOD="post">
        <input type="text" name="netegrityUsername" value=""><BR>
        <input type="password" name="netegrityPassword" value="password">
        <INPUT type="SUBMIT" VALUE="Sign-In" tabIndex="3" NAME="login0">
    </FORM>
</CENTER>
</BODY>    
</HTML>

