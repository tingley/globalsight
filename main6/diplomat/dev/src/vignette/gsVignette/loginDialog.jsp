<%--
#   TEMPLATE NAME
#   -------------
# 	loginDialog
#
#   DESCRIPTION
#   -----------
# 	This template provides a form for establishing a connection to the CMS.
#   The form also verifies that blank fields are not entered before directing
#   the data to the login template.
#
#############################################################################
--%>
<%@ page contentType="text/html; charset=UTF-8"
		 errorPage="error.jsp"
         session="true"
         import="java.util.*, java.io.*, java.net.*" 
%>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<%
	InputStream is = application.getResourceAsStream("connection.properties");
	Properties connectionProp = new Properties();
	connectionProp.load(is);
	is.close();
    String host = connectionProp.getProperty("host");
    String port = connectionProp.getProperty("port");
    String sys4_host = connectionProp.getProperty("sys4_host");
	String main_ui_url = connectionProp.getProperty("main_ui_url");
	
	//put the properties in the session so other pages don't have to reread it
	session.setAttribute("properties",connectionProp);
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
    <TITLE>GlobalSight System4 Vignette Browser Login</TITLE>
<SCRIPT LANGUAGE="JavaScript">
if (top.location != location) top.location.href = location.href;
</SCRIPT>
    <SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js">
    </SCRIPT>
    <SCRIPT LANGUAGE="JavaScript" SRC="Includes/utilityScripts.js">
    </SCRIPT>
    <SCRIPT LANGUAGE="JavaScript">
    function confirmForm(formSent) {
       // Make sure a hostname has been given
       if (isEmptyString(formSent.host.value))
       {
          alert("You must enter a Hostname to log in.");
          formSent.host.value = "";
          formSent.host.focus();
          return false;
       }

       // Make sure a port number has been given
       if (isEmptyString(formSent.port.value))
       {
          alert("You must enter a port number to log in.");
          formSent.port.value = "";
          formSent.port.focus();
          return false;
       }

       // Make sure a name has been given
       if (isEmptyString(formSent.user.value))
       {
          alert("You must enter a Username to log in.");
          formSent.user.value = "";
          formSent.user.focus();
          return false;
       }

       // Make sure a password has been given
       if (isEmptyString(formSent.pass.value))
       {
          alert("You must enter a password to log in.");
          formSent.pass.value = "";
          formSent.pass.focus();
          return false;
       }



       return true;
    }
    </SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
    <DIV ID="header" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 0px; LEFT: 0px;">
        <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
            <TR>
                <TD CLASS="header1"><IMG SRC="Images/logo_header.gif" HEIGHT="68" WIDTH="334"></TD>
                <TD CLASS="header1" ALIGN="right"></TD>
            </TR>
            <TR>
                <TD COLSPAN="2" CLASS="header2" HEIGHT="20"></TD>
            </TR>
        </TABLE>
    </DIV>
    <DIV ALIGN="CENTER" ID="contentLayer0" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 150px; LEFT: 0px;">
	<center><b>Vignette 6 CMS Browser</center><center>For GlobalSight System4</b></center><br>

        <TABLE WIDTH="100%">
            <TR>
                <TD ALIGN="CENTER">
                    <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
                        <TR>
                            <TD WIDTH=250>
                                <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
                                <form name="menu" action="login.jsp" method="get" onSubmit="return confirmForm(this)">
  

									<input type=hidden name=redirectURL value="<%=main_ui_url%>">
                                    <TR>
                                        <TD>
                                            <SPAN CLASS="standardText">
                                            Host:&nbsp;&nbsp;
                                            </SPAN>
                                        </TD>
                                        <TD>
                                            <SPAN CLASS="standardText">
											<input type="hidden" name="host" value="<%=host%>">
											<%= host %>
                                            </SPAN>                                        
                                        </TD>
                                    </TR>
                                    <TR>
                                        <TD HEIGHT="5"></TD>
                                    </TR>

                                    <TR>
                                        <TD>
                                            <SPAN CLASS="standardText">
                                            Port:&nbsp;&nbsp;
                                            </SPAN>
                                        </TD>
                                        <TD>
                                            <SPAN CLASS="standardText">
											<input type="hidden" name="port" value="<%=port%>">
											<%=port%>
                                            </SPAN>
                                        </TD>
                                    </TR>
                                    <TR>
                                        <TD HEIGHT="5"></TD>
                                    </TR>

                                    <TR>
                                        <TD>
                                            <SPAN CLASS="standardText">
                                            Username:&nbsp;&nbsp;
                                            </SPAN>
                                        </TD>
                                        <TD>
                                            <SPAN CLASS="standardText">
                                            <input type="text" name="user" size="18" tabIndex="1">
                                            </SPAN>
                                                                                          
                                        </TD>
                                    </TR>
                                    <TR>
                                        <TD HEIGHT="5"></TD>
                                    </TR>
                                    <TR>
                                        <TD>
                                            <SPAN CLASS="standardText">
                                            Password:&nbsp;&nbsp;
                                            </SPAN>
                                        </TD>
                                        <TD>
                                            <SPAN CLASS="standardText">
                                            <input type="password" name="pass" size="18" tabIndex="2">
                                            </SPAN>
                                        </TD>
                                    </TR>
                                   <TR>
                                        <TR>
                                        <TD HEIGHT="20"></TD>
                                    </TR>
                                        <TD COLSPAN="3" ALIGN="LEFT">
                                <SPAN CLASS="standardText">
                                <input type="image" BORDER="0" src="Images/loginButton.gif" tabIndex="3" NAME="Login">
                                </SPAN>
                                </TD> </TR>
                                 </TABLE>
								</TD></TR>								 
                                </FORM>
                                </SPAN>
                            </TD>
                        </TR>
                    </TABLE>
                </TD>
            </TR>
        </TABLE>
<br><br>
<center><font size="+1" color="red"> 
<%
  String msg = (String) session.getAttribute("msg");
  session.removeAttribute("msg");
  if (msg != null)
  {
    %><i><%=msg%></i><%
  }
%>
</font></center>
	
    </DIV>
    </BODY>
</HTML>
