<%@ page contentType="text/html; charset=UTF-8"
        errorPage="error.jsp" import="java.util.Enumeration, com.vignette.cms.client.beans.*"
        session="true"%>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<HTML>
<HEAD>
<jsp:useBean id="fileHandler" class="com.globalsight.vignette.FileHandler" scope="session"/>
<%@ page import="com.globalsight.vignette.Item" %>
<TITLE></TITLE>
<script type="text/javascript" src="Includes/tabbed.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="Includes/tablesort.js"></script>

<link rel="STYLESHEET" type="text/css" href="Includes/tablesort.css">

</HEAD>
<BODY BGCOLOR="#FFFFFF">
				
<table width="100%" cellspacing="0" onclick="sortColumn(event)">
<thead>
<TR>
	<td style="width: 50%;" NOWRAP>Doc Root Path</td>
	<td style="width: 15%;" NOWRAP>Mgmt ID</td>
	<td style="width: 20%;" NOWRAP>Status</td>
	<td style="width; 15%" NOWRAP>Action</td>
</TR>
</thead>
<tbody>
<TR>
<% 
	int numFiles = 0;
	Enumeration items = fileHandler.getItems();
   while(items.hasMoreElements()){
   	 numFiles++;
     Item item = (Item)items.nextElement(); %>
    <TD WIDTH="50%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=item.getPath()%></SPAN></TD>
    <TD WIDTH="15%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=item.getMid()%></SPAN></TD>
    <TD WIDTH="20%" NOWRAP ALIGN="center"><SPAN CLASS="standardText" HREF="#"><%=item.getStatus()%></SPAN></TD>
    <TD WIDTH="15%" NOWRAP ALIGN="center"><A CLASS="standardHREF" HREF="tabFrame.jsp?type=f&status=deleteItem&mid=<%=item.getMid()%>" TARGET="fileList">Remove</A></TD>
</TR>
<% }
   %>
</tbody>
</table>

<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR>
<TD WIDTH="75%">&nbsp </TD>
<% if (numFiles > 0) {%>
	<TD WIDTH="25%" NOWRAP ALIGN="right"><A CLASS="alertHREF" HREF="tabFrame.jsp?type=f&status=emptyFileHandler" TARGET="fileList">Remove All</A></TD>
<% } %>	
</TR>
</TABLE>
</BODY>
</HTML>

