<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.costing.currency.CurrencyHandlerHelper,
                 com.globalsight.everest.costing.Currency,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.request.reimport.ActivePageReimporter,
                 java.util.Collection,
                 java.util.Iterator,
                 java.util.ResourceBundle"
         session="true" %>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%!
    private static final String CHECKED  = "CHECKED";
    private static final String SELECTED = "SELECTED";
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String title = bundle.getString("lb_admin_system_parameters");
String lbSave = bundle.getString("lb_save");
String lbCancel = bundle.getString("lb_cancel");

String systemLoggingPriority =
    (String)request.getAttribute(SystemConfigParamNames.SYSTEM_LOGGING_PRIORITY);

boolean isToplinkLogging =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.HIBERNATE_LOGGING)).booleanValue();
boolean isSysNotificationEnabled =
    Boolean.valueOf((String)request.getAttribute(
      SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED)).booleanValue();
      
String analyzeInterval = request.getAttribute(SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL).toString();
String[] analyzeIntervalItems = new String [] 
{
    "0:00 AM", "1:00 AM", "2:00 AM", "3:00 AM", "4:00 AM", "5:00 AM", "6:00 AM",
    "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 AM",
    "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM",
    "7:00 PM", "8:00 PM", "9:00 PM", "10:00 PM", "11:00 PM"
};
%>

<HTML>
<!-- This JSP is envoy/administration/config/adminConfigMain.jsp -->
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "systemParameter";
var helpFile = "<%=bundle.getString("help_system_parameters")%>";

function submitForm()
{
    if (document.layers)
    {
    }
    else
    {
        if (confirm("<%= bundle.getString("jsmsg_admin_system_parameters") %>"))
        {
            profileForm.submit();
        }
    }
}

function doCancel()
{
    //window.navigate("/globalsight/ControlServlet");
	window.location.href="/globalsight/ControlServlet";
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_admin_system_paramters")%>
</TD>
</TR>
</TABLE>
<P>

<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0">
<COL WIDTH=250>
<COL>
  <FORM name="profileForm" action="<%=save.getPageURL()%>" method="post">
  <TR VALIGN="TOP">
    <TD COLSPAN="2"><B>admin system parameters</B><hr></TD>
  </TR>

  <!--
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_system_logging_priority")%>:</SPAN></TD>
    <TD>
      <SELECT NAME="<%=SystemConfigParamNames.SYSTEM_LOGGING_PRIORITY%>" CLASS="standardText">
        <OPTION <%if ("DEBUG".equalsIgnoreCase(systemLoggingPriority)) out.print(SELECTED);%>>DEBUG</option>
        <OPTION <%if ("INFO".equalsIgnoreCase(systemLoggingPriority)) out.print(SELECTED);%>>INFO</option>
        <OPTION <%if ("WARN".equalsIgnoreCase(systemLoggingPriority)) out.print(SELECTED);%>>WARN</option>
        <OPTION <%if ("ERROR".equalsIgnoreCase(systemLoggingPriority)) out.print(SELECTED);%>>ERROR</option>
        <OPTION <%if ("FATAL".equalsIgnoreCase(systemLoggingPriority)) out.print(SELECTED);%>>FATAL</option>
      </SELECT>
    </TD>
  </TR>
  -->
  
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_mail_server")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="TEXT" NAME="<%=SystemConfigParamNames.MAIL_SERVER%>"
      SIZE="30" MAXLENGTH="30" CLASS="standardText"
      VALUE="<%=request.getAttribute(SystemConfigParamNames.MAIL_SERVER)%>"></SPAN>
    </TD>
  </TR>
  
  

  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_toplink_logging")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isToplinkLogging) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.HIBERNATE_LOGGING%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isToplinkLogging) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.HIBERNATE_LOGGING%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  
  <!--
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_sysNotification_enabled")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <INPUT TYPE="radio" <%if (isSysNotificationEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED%>" VALUE="true">
      <%=bundle.getString("lb_yes") %>
      <INPUT TYPE="radio" <%if (!isSysNotificationEnabled) out.print(CHECKED);%>
      NAME="<%=SystemConfigParamNames.SYSTEM_NOTIFICATION_ENABLED%>" VALUE="false">
      <%=bundle.getString("lb_no") %></SPAN>
    </TD>
  </TR>
  -->
  
      <INPUT TYPE="hidden" NAME="<%=SystemConfigParamNames.CUSTOMER_INSTALL_KEY%>" VALUE="true">
  <TR VALIGN="TOP">
    <TD><SPAN CLASS="standardText"><%=bundle.getString("lb_analyze_script_time_interval")%>:</SPAN></TD>
    <TD><SPAN CLASS="standardText">
      <SELECT NAME="<%=SystemConfigParamNames.ANALYZE_SCRIPT_INTERVAL%>" size="1" CLASS="standardText">
      <%
          for (int i = 0; i < analyzeIntervalItems.length; i++)
          {
              StringBuffer sb = new StringBuffer();
              String value = i + "/24";
              sb.append("<option value=\"").append(value).append("\"");
              if (analyzeInterval.equals(value))
              {
                  sb.append(" selected");
              }
						  sb.append(">").append(analyzeIntervalItems[i]).append("</option>");
              out.println(sb.toString());
          }
      %>
      </SELECT></SPAN>
    </TD>
  </TR>

  <TR VALIGN="TOP">
    <TD COLSPAN="2"></TD>
  </TR>
  <TR>
    <TD COLSPAN="2" ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
      onclick="doCancel()">
      <INPUT TYPE="BUTTON" NAME="<%=lbSave%>" VALUE="<%=lbSave%>"
      onclick="submitForm()">
    </TD>
  </TR>
  </FORM>
</TABLE>
</DIV>
</BODY>
</HTML>
