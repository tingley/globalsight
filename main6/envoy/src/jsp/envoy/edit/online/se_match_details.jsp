<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
	import="java.util.*,
	  com.globalsight.everest.webapp.pagehandler.PageHandler,
	  java.util.ResourceBundle"
    session="true"
%>
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String lb_close = bundle.getString("lb_close");
	String lb_match_details = bundle.getString("lb_match_details");
	String lb_origin = bundle.getString("lb_origin");
	String lb_original_job = bundle.getString("lb_original_job");
	String lb_created_by = bundle.getString("lb_created_by");
	String lb_created_on = bundle.getString("lb_created_on");
	String lb_modified_by = bundle.getString("lb_modified_by");
	String lb_modified_on = bundle.getString("lb_modified_on");
	
%>
<HTML>
<HEAD>
<TITLE><%=lb_match_details%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<BODY oncontextmenu="return false">
<SPAN CLASS="mainHeading"><%=lb_match_details%></SPAN>
<br>
<TABLE cellpadding="2" cellspacing="2" BORDER="0">
  <TR class="standardText">
    <TD><B><%=lb_origin%>:</B></TD>
    <TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("tmName").value);</SCRIPT></TD>
  </TR>
  
  <SCRIPT language="Javascript">
  var originalJob = window.opener.document.getElementById("matchedTuvJobName").value;
  if(originalJob!="N/A")
  {
    document.write("<TR class='standardText'><TD><b><%=lb_original_job%>:</b></TD><TD>"+originalJob+"</TD></TR>");
  }
  </script>

  <TR class="standardText">
  	<TD><B><%=lb_created_by %>:</B></TD>
  	<TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("creationUser").value);</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_created_on %>:</B></TD>
    <TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("creationDate").value);</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_modified_by %>:</B></TD>
    <TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("modifyUser").value);</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_modified_on %>:</B></TD>
    <TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("modifyDate").value);</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=bundle.getString("lb_sid")%>:</B></TD>
    <TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("sid").value);</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B>Match Type:</B></TD>
    <TD><SCRIPT language="Javascript">document.write(window.opener.document.getElementById("matchType").value);</SCRIPT></TD>
  </TR>
</TABLE>
<P>
<CENTER>
<INPUT TYPE="BUTTON" VALUE="<%=lb_close%>" ONCLICK="window.close()">
</CENTER>
</BODY>
</HTML>
