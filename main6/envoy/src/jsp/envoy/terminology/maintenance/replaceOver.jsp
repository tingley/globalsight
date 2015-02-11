<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,java.util.ResourceBundle,
    com.globalsight.everest.webapp.pagehandler.PageHandler"
    
    session="true"
%>
<html>
    <head>
<link type="text/css" rel="StyleSheet" href="/globalsight/includes/stylesIE.jsp" />
<style type="text/css">
    .warning {
    	color:	red;
    }
</style>
</head>

<body>
<div class="standardText"><div class="warning">
<%
ResourceBundle bundle = PageHandler.getBundle(session);
ArrayList array = (ArrayList) request.getAttribute("failedReplace");

if(array == null || array.size() == 0) {
    out.println(bundle.getString("msg_maintance_ssuccess"));
}else {
    out.println("Such below fields replace failed:");
    
    for(int i = 0; i < array.size(); i++ ) {
    
        out.println(array.get(i));
        out.println("<br>");
    }
}
%>
</div></div>
<script>
    parent.document.getElementById("replaceButton").disabled = true;
</script>

</body>
</html>