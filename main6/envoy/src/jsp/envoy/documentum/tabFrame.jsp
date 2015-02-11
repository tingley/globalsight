<%@ page contentType="text/html; charset=UTF-8"
        errorPage="error.jsp" import="java.util.Enumeration, 
        java.util.*, 
        java.io.*,
		com.globalsight.vignette.*"
        session="true"%>
<jsp:useBean id="fileHandler" class="com.globalsight.vignette.FileHandler" scope="session"/>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
  <title>DCTM Browser</title>
  <LINK href="Includes/stylesIE.css" type=text/css rel="stylesheet">
  <link href="Includes/default.css" rel="stylesheet" type="text/css">
  <SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js"></SCRIPT>
</head>
<body>
<br>
<%
String optionspage = "fileImportOptions.jsp";
String header = "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Static Files Selected for Import";
String iframesrc = "fileHolder.jsp";
String importlabel = "Import Files";
String importsetuplabel = "Static File Profile";
String type = request.getParameter("type");
String status = request.getParameter("status");

   if(type!=null){
     if(type.equals("f"))
	 {
	   optionspage = "fileImportOptions.jsp";
	   header = "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Static Files Selected for Import";
	   iframesrc = "fileHolder.jsp";
   		importlabel = "Import Files";
	   importsetuplabel = "Static File Profile";

      if(status!=null){
        if(status.equals("emptyFileHandler")){
        String mid = request.getParameter("mid");
        fileHandler.emptyFileHandler();
          }
		  else if (status.equals("deleteItem")) {
        	String mid = request.getParameter("mid");
	        fileHandler.deleteItem(mid);
          }
		  else {
        String path = request.getParameter("path");
        String mid = request.getParameter("mid");
        Item newItem = new Item(mid, path, status);
        fileHandler.addItem(newItem);
       }}
    

     } else {
       optionspage = "templateImportOptions.jsp";
        importlabel = "Import Templates";
        importsetuplabel = "Template Profile";
   header = "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Templates Selected for Import";
   iframesrc = "templateHolder.jsp";
   }} 
%>
    <div class="tabMain">
      <h4 id="title" align="right">
      <div id="divTopMenu0" class="clTopMenu">
        <div id="divTopMenuBottom0" class="clTopMenuBottom"></div>
      </div>
<% if (fileHandler.size() > 0) { %>	  
	 <A CLASS="HREFBoldWhite" HREF="gsImporter.jsp">&nbsp;<%=importlabel%></A>
<% } else  { %>
	 &nbsp;
<% } %>	 
      </h4>
      <div class="tabIframeWrapper">
        <iframe class="tabContent" name="tabIframe2" src="<%=iframesrc%>" marginheight="0" marginwidth="0" frameborder="0"></iframe>
      </div>
    </div>
  </div>
 </body>
</html>

