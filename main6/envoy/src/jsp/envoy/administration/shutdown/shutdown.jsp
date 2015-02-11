<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.shutdown.ShutdownMainHandler,
            java.util.ResourceBundle,
            java.util.Date,
            java.util.Enumeration"
    session="true" %>
<%  
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    boolean isShuttingDown = ((Boolean) request.getAttribute(
        ShutdownMainHandler.ATTR_SHUTDOWN_STATE)).booleanValue();
    Date shutdownTime = (Date) request.getAttribute(
        ShutdownMainHandler.ATTR_SHUTDOWN_TIME);
%><HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=bundle.getString("lb_shutdownTitle")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/formvalidation.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
  var needWarning = false;
  var objectName = "";
  var guideNode = "import";
  var helpFile = "<%=bundle.getString("help_shutdown")%>";
  var hasRadioButton = false;

  function goHome()
  {
     document.location="/globalsight/ControlServlet";
  }

  //validate the form for correct inputs
  function validateForm(form)
  {
     var doCheck = true;
     if (hasRadioButton==true) {
        var rb = form.elements["<%=ShutdownMainHandler.PARAM_CHOICE%>"];
        if (rb[0].checked==true)
        {
           doCheck=true;
        }
        else
        {
           doCheck=false;
        }
     }
     else
     {
        doCheck=true;
     }

     if (doCheck==true) {
        var hours = form.elements["<%=ShutdownMainHandler.PARAM_HOUR%>"];
        var mins = form.elements["<%=ShutdownMainHandler.PARAM_MIN%>"];
        var msg = form.elements["<%=ShutdownMainHandler.PARAM_MSG%>"];
        if (hours.value.length == 0 || mins.value.length==0 || msg.value.length==0)
        {
           alert ("<%=bundle.getString("jsmsg_shutdown_noblanks")%>");
           return false;
        }
        
        if (isNumber(hours)==false || isNumber(mins)==false)
        {
           alert ("<%=bundle.getString("jsmsg_shutdown_badnumber")%>");
           return false;
        }
     }

     return true;
  }
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading">
<%=bundle.getString("lb_shutdownTitle")%>
</SPAN>
<P><%=bundle.getString("lb_shutdownCurTime")%>&nbsp;<%=new Date()%><BR>
<% if (isShuttingDown==true) { %>
<%=bundle.getString("lb_shutdownAtTime")%>&nbsp;<%=shutdownTime.toString()%>
<%} else { %>
<%=bundle.getString("lb_shutdownNone")%>
<% } %>
<FORM ACTION="<%=LinkHelper.getWebActivityURL(request, "shutdown")%>" METHOD="POST" onsubmit="return validateForm(this)" >
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS="standardText">
<TR><TD ALIGN="TOP">
<% if (isShuttingDown==true) { %>
<SCRIPT LANGUAGE="JavaScript">
   hasRadioButton=true;
</SCRIPT>
<INPUT TYPE="RADIO" NAME="<%=ShutdownMainHandler.PARAM_CHOICE%>" VALUE="<%=ShutdownMainHandler.PARAM_CHOICE_SHUTDOWN%>">
<%=bundle.getString("lb_shutdownChange")%>
<% } else { %>
<INPUT TYPE="HIDDEN" NAME="<%=ShutdownMainHandler.PARAM_CHOICE%>" VALUE="<%=ShutdownMainHandler.PARAM_CHOICE_SHUTDOWN%>">
<%=bundle.getString("lb_shutdownSet")%>
<% } %></TD>
<TD><TABLE CELLPADDING=0 CELLSPACING=2 BORDER=0 CLASS=standardText>
<TR><TD><%=bundle.getString("lb_shutdownHours")%></TD><TD><INPUT  SIZE="1" TYPE="TEXT" NAME="<%=ShutdownMainHandler.PARAM_HOUR%>" VALUE="0"/></TD></TR>
<TR><TD><%=bundle.getString("lb_shutdownMinutes")%></TD><TD><INPUT  SIZE="1" TYPE="TEXT" NAME="<%=ShutdownMainHandler.PARAM_MIN%>" VALUE="30"/></TD></TR>
<TR><TD><%=bundle.getString("lb_shutdownMsg")%></TD><TD><TEXTAREA  COLS="60" NAME="<%=ShutdownMainHandler.PARAM_MSG%>"><%=ShutdownMainHandler.getShutdownMessage(false)%></TEXTAREA></TD></TR>
</TABLE></TD></TR>

<% if (isShuttingDown==true) { %>
<TR><TD COLSPAN=2>
<INPUT TYPE="RADIO" NAME="<%=ShutdownMainHandler.PARAM_CHOICE%>" VALUE="<%=ShutdownMainHandler.PARAM_CHOICE_CANCEL%>" CHECKED><%=bundle.getString("lb_shutdownCancel")%>
</TD></TR>
<%}%>
</TABLE>
<P>
<INPUT TYPE="SUBMIT" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>">&nbsp;&nbsp;<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_shutdownHome")%>" ONCLICK="javascript:goHome()">
</FORM>
</DIV>
</BODY>
</HTML>

