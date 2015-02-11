<%@ page errorPage="/errorpage.jsp" %>
<%@ page language="java"%>
<%
String inputHTML=null;
inputHTML=request.getParameter("textfieldforServer");
%>
<html>
<head>
<meta NAME="GENERATOR" Content="Microsoft FrontPage 4.0">
<title>HTML Display</title>
<meta name="Microsoft Border" content="b, default">
</head>
<body>
<%=inputHTML%>
</body>
</html>

