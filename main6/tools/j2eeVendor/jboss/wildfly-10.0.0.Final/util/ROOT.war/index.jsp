<%
    //response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/html;charset=utf-8");
    response.addHeader("X-Frame-Options", "SAMEORIGIN");
    response.addHeader("X-Content-Type-Options", "nosniff");
    response.addHeader("Strict-Transport-Security",
            "max-age=31536000; includeSubDomains");
%>

<%
    response.sendRedirect("/globalsight/");
%>