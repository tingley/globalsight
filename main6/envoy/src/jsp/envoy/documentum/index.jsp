<%@ page contentType="text/html; charset=UTF-8"
		 errorPage="error.jsp"
         session="true"
%>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<HTML>
<HEAD>
<TITLE>Select Files to Import from Documentum</TITLE>
</HEAD>
<frameset cols="40%,60%" frameborder="no" border="0" framespacing="0">;
	<frame src="folderListing.jsp" name="folderList" scrolling="auto" marginheight="0" marginwidth="0">
	<frame src="tabFrame.jsp" name="fileList" scrolling="auto" marginheight="0" marginwidth="0">
</frameset>

<noframes>
<body bgcolor="white" text="#000000" alink="#000000" vlink="#000000"
link="#000000">
To view the Welocalize - Documentum 5 Integration, please use a browser that supports
frames.
</body>
</noframes>

</HTML>

