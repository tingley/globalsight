<%@ page contentType="text/html; charset=UTF-8"
		 errorPage="error.jsp"
         session="true"
%>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<jsp:useBean
		id="cms" class="com.vignette.cms.client.beans.CMS"
		scope = "session" />
<jsp:useBean
		id="sec" class="com.vignette.cms.client.beans.CMSSecurity"
		scope="session"/>
<%
    if ( !cms.isConnected() )
    {
        pageContext.forward("loginDialog.jsp");
	}
	else {
%>
<HTML>
<HEAD>
<TITLE>Select Files to Import</TITLE>

</HEAD>

<frameset rows="108,*" frameborder="no" border="0" framespacing="0">;	
	<frame src="header_inc.jsp" name="header" scrolling="no" noresize marginheight="0" marginwidth="0">
<frameset cols="40%,60%" frameborder="no" border="0" framespacing="0">;
	<frame src="folderListing.jsp" name="folderList" scrolling="auto" marginheight="0" marginwidth="0">
	<frame src="tabFrame.jsp" name="fileList" scrolling="auto" marginheight="0" marginwidth="0">
</frameset>
</frameset>

<noframes>
<body bgcolor="white" text="#000000" alink="#000000" vlink="#000000"
link="#000000">
To view the GlobalSight - Vignette 6 Application, please use a browser that supports
frames.
</body>
</noframes>

</HTML>
<% } %>

