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
xml = m.replaceAll("\"");
//replace the \r\n with real newlines
p = Pattern.compile("\\\\" + "r" + "\\\\" + "n");
m = p.matcher(xml);
xml = m.replaceAll("\r\n");

p = Pattern.compile("\\\\" + "t" );
m = p.matcher(xml);
xml = m.replaceAll("\t");

p = Pattern.compile("\\\\" + "n" );
m = p.matcher(xml);
xml = m.replaceAll("\n");
%><%=xml%>
