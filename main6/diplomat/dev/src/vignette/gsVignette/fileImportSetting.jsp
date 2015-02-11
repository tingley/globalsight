<%@ include file="/vgn/jsp/include.jsp" %><%@ page contentType="text/html; charset=UTF-8"
        errorPage="/gs/Common/error.jsp"
        session="true"%>

<% 
String jobname = request.getParameter("jobname");
session.setAttribute ("jobname", jobname);
session.setAttribute ("versionflag", "false");
String fpid = request.getParameter("fpid");
session.setAttribute ("fpid", fpid);
String tgtprojmid = request.getParameter("tgtprojmid");
session.setAttribute ("tgtprojmid", tgtprojmid);
String returnstatus = request.getParameter("returnstatus");
session.setAttribute ("returnstatus", returnstatus);

%>
