<%@ include file="/vgn/jsp/include.jsp" %><%@ page contentType="text/html; charset=UTF-8"
        errorPage="/gs/Common/error.jsp" import="java.util.Enumeration"
        session="true"%>

<%@ page import="com.vignette.cms.client.beans.*" %>

<HTML>
<HEAD>
<jsp:useBean id="recordHandler" class="com.globalsight.vignette.RecordHandler" scope="session"/>
<%@ page import="com.globalsight.vignette.Record" %>
<TITLE></TITLE>
<script type="text/javascript" src="/gs/Common/Includes/tabbed.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/gs/Common/Includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/gs/Common/Includes/tablesort.js"></script>
<link rel="STYLESHEET" type="text/css" href="/gs/Common/Includes/tablesort.css">

</HEAD>
<BODY BGCOLOR="#FFFFFF">				
<table width="100%" cellspacing="0" onclick="sortColumn(event)">
<thead>
<TR>
	<td style="width: 5%;" NOWRAP>ID</td>
	<td style="width: 15%;" NOWRAP>Name</td>
      <td style="width: 10%;" NOWRAP>Server</td>
	<td style="width: 10%;" NOWRAP>Database</td>
	<td style="width: 15%;" NOWRAP>Table</td>
	<td style="width: 10%;" NOWRAP>Mgmt ID</td>
	<td style="width: 20%;" NOWRAP>Status</td>
	<td WIDTH="15%" NOWRAP></td>
</TR>
</thead>
<tbody>
<TR>
<% Enumeration records = recordHandler.getRecords();
   while(records.hasMoreElements()){
     Record record = (Record)records.nextElement(); %>

    <TD WIDTH="5%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=record.getKeyid()%></SPAN></TD>
    <TD WIDTH="15%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=record.getName()%></SPAN></TD>
    <TD WIDTH="10%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=record.getServer()%></SPAN></TD>
    <TD WIDTH="10%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=record.getDatabase()%></SPAN></TD>
    <TD WIDTH="15%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=record.getTable()%></SPAN></TD>
    <TD WIDTH="10%" NOWRAP ALIGN="left"><SPAN CLASS="standardText" HREF="#"><%=record.getMid()%></SPAN></TD>
    <TD WIDTH="20%" NOWRAP ALIGN="center"><SPAN CLASS="standardText" HREF="#"><%=record.getStatus()%></SPAN></TD>
    <TD WIDTH="15%" NOWRAP ALIGN="center"><A CLASS="standardHREF" HREF="/gs/cms/tabFrame?type=r&status=deleteRecord&mid=<%=record.getMid()%>" TARGET="fileList">Remove</A></TD>
</TR>
<% }
   %>
</tbody>
</table>

<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR>
<TD WIDTH="75%">&nbsp </TD>

<TD WIDTH="25%" NOWRAP ALIGN="right"><A CLASS="alertHREF" HREF="/gs/cms/tabFrame?type=r&status=emptyRecordHandler" TARGET="fileList">Remove All</A></TD>
</TR>
</TABLE>

</BODY>
</HTML>