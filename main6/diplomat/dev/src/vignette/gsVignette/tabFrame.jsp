<%@ page contentType="text/html; charset=UTF-8"
        errorPage="error.jsp" import="java.util.Enumeration, 
        java.util.*, 
        java.io.*,
		com.vignette.cms.client.beans.CMS,
		com.globalsight.vignette.*"
        session="true"%>
<jsp:useBean
		id="cms" class="com.vignette.cms.client.beans.CMS"
		scope = "session" />
<jsp:useBean id="fileHandler" class="com.globalsight.vignette.FileHandler" scope="session"/>
<jsp:useBean id="recordHandler" class="com.globalsight.vignette.RecordHandler" scope="session"/>
<jsp:useBean id="templateHandler" class="com.globalsight.vignette.TemplateHandler" scope="session"/>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
  <title>Vignette Browser</title>
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
    

     } else if(type.equals("r")) {
   optionspage = "recordImportOptions.jsp";
   header = "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Records Selected for Import";
   iframesrc = "recordHolder.jsp";
   importlabel = "Import Records";
   importsetuplabel = "Record Profile";

         if(status!=null){
         if(status.equals("emptyRecordHandler")){
           String mid = request.getParameter("mid");
           recordHandler.emptyRecordHandler();
         } else if(status.equals("deleteRecord")) {
           String mid = request.getParameter("mid");
           recordHandler.deleteRecord(mid);
         } else {
       String server = request.getParameter("server");
           String database = request.getParameter("database");
       String table = request.getParameter("table");
           String key = request.getParameter("key");
       String keyid = request.getParameter("keyid");
           String name = request.getParameter("name");
           String mid = request.getParameter("mid");
           Record newRecord = new Record(server, database, table, key, keyid, name, mid, status);
           recordHandler.addRecord(newRecord);
     }}


         } else if(type.equals("t")) {
   optionspage = "templateImportOptions.jsp";
   header = "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Templates Selected for Import";
   iframesrc = "templateHolder.jsp";
   importlabel = "Import Templates";
   importsetuplabel = "Template Profile";
         if(status!=null){
         if(status.equals("emptyTemplateHandler")){
           String mid = request.getParameter("mid");
           templateHandler.emptyTemplateHandler();
         } else if(status.equals("deleteTemplate")) {
           String mid = request.getParameter("mid");
			templateHandler.deleteTemplate(mid);

         } else {
           String path = request.getParameter("path");
           String name = request.getParameter("name");
           String tmplid = request.getParameter("tmplid");
           String tmpltype = request.getParameter("tmpltype");
           String ext = request.getParameter("ext");
           String table = request.getParameter("table");
           String mid = request.getParameter("mid");
           Template newTemplate = new Template(path, name, tmplid, tmpltype, ext, table, mid, status);
           templateHandler.addTemplate(newTemplate);
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

