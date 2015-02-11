<%@ page errorPage="error.jsp" %>
<%
	session.invalidate();
     pageContext.forward("loginDialog.jsp");
%>
