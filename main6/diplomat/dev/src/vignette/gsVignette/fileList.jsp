<%@ page contentType="text/html; charset=UTF-8"
		errorPage="error.jsp"
		 import="java.util.*,java.io.*"
                 session="true" %>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<jsp:useBean id="FileManager" scope="session" class="sessions.FileManager" />
<jsp:setProperty name="FileManager" property="*" />
<%
	FileManager.processRequest(request);
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
function submitForm()
{
	if (document.layers)
	{
		formToSubmit = document.contentLayer.document.fileListForm;
	}
	else
	{
		formToSubmit = document.fileListForm;
	}
	if (true)
	{
		var tooLong = true;
		jobName = prompt("Please enter a name for the resulting job:","DEFAULTNAME");
		if (jobName != null)
		{
			if (jobName.length < 81) tooLong = false;
        		while (tooLong)
			{
				jobName = prompt("The Job Name you have entered has more than 80 characters.  Please enter a shorter Job Name.:",jobName);
				if (jobName != null)
				{
					if (jobName.length < 81) tooLong = false;
				}
				else tooLong = false;
			}
		}
		if (jobName != null)
		{
			if(jobName.length == 0) jobName = "DEFAULTNAME";
        		formToSubmit.jobName.value = jobName;
			formToSubmit.submit();
		}
	}
	else formToSubmit.submit();
}
</script>
</HEAD>
<BODY BGCOLOR="#FFFFFF">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 20px; LEFT: 20px;">
<FORM METHOD="post" ACTION="needsAurl" NAME="fileListForm" TARGET="_top">
<INPUT TYPE="HIDDEN" NAME="jobName" VALUE="">
<INPUT TYPE="HIDDEN" NAME="import" VALUE="positive">
</FORM>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR>
	<TD WIDTH="100%" VALIGN="TOP">
		<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
		<TR>
			<TD WIDTH="100%" VALIGN="TOP">
				<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="0">
				<TR>
					<TD CLASS="tableHeadingBasic" NOWRAP>Files Selected For Import</TD>
					<TD CLASS="tableHeadingBasic" ALIGN="RIGHT" NOWRAP>
<%
        String[] items = FileManager.getItems();
	if (items.length > 0)
		out.println("<A HREF=\"javascript:submitForm();\">" +
                    "<IMG SRC=\"/gs/images/yellow_arrow.gif\" BORDER=0 WIDTH=9 HEIGHT=13 ALIGN=middle></A>" +
                    "&nbsp;" +
                    "<A CLASS=\"HREFBoldWhite\" HREF=\"javascript:submitForm();\">Import</A>");
%>
					</TD>
				</TR>
				</TABLE>
				<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="2" BORDER="0" BGCOLOR="#FFFFFF">


<%
	for (int i=0; i<items.length; i++) {
%>
<li> <%= items[i] %>
<%
	}
%>


				</TABLE>
			</TD>
		</TR>
		</TABLE>
	</TD>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
