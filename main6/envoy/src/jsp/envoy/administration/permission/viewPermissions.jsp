<%@ page contentType="text/xml; charset=UTF-8"
    import="com.globalsight.everest.webapp.WebAppConstants,
    com.globalsight.everest.servlet.util.SessionManager,
    java.util.regex.*"
    session="true"
    %><%
response.setHeader("Pragma", "no-cache"); //HTTP 1.0
response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
response.addHeader("Cache-Control", "no-store"); // tell proxy not to cache
response.addHeader("Cache-Control", "max-age=0"); // stale right away
    
SessionManager sessionMgr =
  (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
String xml = (String)sessionMgr.getAttribute("permissionXML");
//replace the \" with "
Pattern p = Pattern.compile("\\\\\"");
Matcher m = p.matcher(xml);
String x = m.replaceAll("\"");
//replace the \r\n with real newlines
Pattern p2 = Pattern.compile("\\\\" + "r" + "\\\\" + "n");
Matcher m2 = p2.matcher(x);
String newXml = m2.replaceAll("\r\n");

Pattern p3 = Pattern.compile("\\\\" + "t" );
Matcher m3 = p3.matcher(newXml);
String newXml2 = m3.replaceAll("\t");
%><%=newXml2%>

