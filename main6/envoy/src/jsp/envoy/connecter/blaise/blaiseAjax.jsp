<%@ page import="com.globalsight.util.StringUtil" %>
<%@ page import="com.globalsight.connector.blaise.util.BlaiseAutoHelper" %>
<%@ page contentType="text/html; charset=UTF-8" session="true"%>
<%
    String action = request.getParameter("action");
    String returnValue = "";
    if ("getAttributes".equals(action)) {
        String fpId = request.getParameter("fpId");
        returnValue = BlaiseAutoHelper.getInstance().getJobAttributes(fpId);
    }
    if (StringUtil.isEmpty(action))
        out.print("");

%>
