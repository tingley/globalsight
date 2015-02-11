<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.sql.*,
                 java.util.ArrayList,
                 java.util.HashMap,
                 java.util.Iterator,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.Constants" session="true"
%>
    <%
    ResourceBundle bundle = PageHandler.getBundle(session);
    %>
<html>
    <head>
        <title><%=bundle.getString("report_parameters")%></title>
    </head>
    <body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
        <table border="0" cellspacing="0" cellpadding="5" height="543" width="100%">
        <tr bgcolor="#ABB0D3" valign="top"> 
        <td height="40" colspan="2"><b><font face="Verdana, Arial, Helvetica, sans-serif"><%=bundle.getString("report")%></font></b>: 
        <font face="Verdana, Arial, Helvetica, sans-serif"><%=bundle.getString("missing_terms")%></font></td>
        </tr>
        <tr bgcolor="ccffff"> 
        <td height="6" colspan="2"></td>
        </tr>
        <tr> 
        <td height="452" valign="top" align="left" width="30%" background="/globalsight/images/parambar.jpg"><font face="Verdana, Arial, Helvetica, sans-serif" size="2"></font></td>
        <td height="452" valign="top" align="left" width="70%" bgcolor="#E9E9E9"><font face="Verdana, Arial, Helvetica, sans-serif" size="3">
            <form name=request action="/globalsight/TranswareReports?reportPageName=MissingTerms&act=Create" method=POST>
               <center>
                  <table border=0 cellspacing=10>
                  <tr><td align=right><%=(String)request.getAttribute(Constants.TERMBASE_LABEL)%></td>
                  <td>
                  <select name="termbaseName">
                  <% 
                  	ArrayList termbaseNames = (ArrayList)request.getAttribute(Constants.TERMBASE_NAME);
                  	for (int i=0; i < termbaseNames.size(); i++) {
                  		if(i == 0) {
                  %>
            			<option value="<%=(String)termbaseNames.get(i)%>" selected><%=(String)termbaseNames.get(i)%></option>
            	  <%
            			}
            			else {
            	  %>
            				<option value="<%=(String)termbaseNames.get(i)%>"><%=(String)termbaseNames.get(i)%></option>
            	  <%
            			}
            		}
            	  %>
            		</select>
                  </td>
                  </tr>
                  <tr><td align=right><%=(String)request.getAttribute(Constants.TERMBASELANG_LABEL)%></td>
                  <td>
                  <select name="termbaseLangs">
                  <% 
                  	ArrayList termbaseLangs = (ArrayList)request.getAttribute(Constants.TERMBASE_LANGS);
                    ArrayList termbaseLangLables = (ArrayList)request.getAttribute(Constants.TERMBASE_LANG_LABLES);
                  
                  	for (int i=0; i < termbaseLangs.size(); i++) {
                  %>
                  		<option value="<%=(String)termbaseLangs.get(i)%>"><%=(String)termbaseLangLables.get(i)%></option>
                  <%
                  	}
				  %>
                  	</select>
                  </td>
                  </tr>
                  <tr><td colspan=2>
                  <center>
                  <input type=Submit value="<%=bundle.getString("lb_shutdownSubmit")%>"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <input type=button value="<%=bundle.getString("lb_cancel")%>" onclick="window.close()"/>
                  </center>
                  </td></tr></table>
              </center>
          </form>
          </font></td></tr>
          <tr><td width="27%">&nbsp;</td>
          <td width="73%">&nbsp;</td>
          </tr></table>
      </body>
  </html>