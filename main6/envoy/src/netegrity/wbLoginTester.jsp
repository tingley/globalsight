<%@ page session="false" import="java.net.*,java.io.*" %><% 
   String loginPage = "http://dragade:8080/wb/wbLogin.jsp";
        URL u = new URL(loginPage);
        URLConnection conn = u.openConnection();
        conn.setDoOutput(false);
        String user = request.getParameter("netegrityUsername");
        System.out.println("Assuming user '"+user+"' is now authenticated through netegrity. Proxying to " + loginPage);
        conn.setRequestProperty("HTTP_SHORTNAME",user);
        String l = null;
        StringBuffer reply = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((l = rd.readLine()) != null)
        {
            System.out.println(">"+l);
            reply.append(l).append("\r\n");
        }
        rd.close();
   response.setHeader("Pragma", "no-cache"); //HTTP 1.0
   response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
   response.addHeader("Cache-control", "no-store"); // tell proxy not to cache
   response.addHeader("Cache-control", "max-age=0"); // stale right away
%>
<%=reply.toString()%>

