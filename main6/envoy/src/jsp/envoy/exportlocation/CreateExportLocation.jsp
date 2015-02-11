<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.exportlocation.ExportLocationPageHandler,
            com.globalsight.everest.webapp.pagehandler.exportlocation.CreateExportLocationPageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.cxe.entity.exportlocation.ExportLocation,
            java.util.Vector,
            com.globalsight.everest.util.system.SystemConfiguration,
            java.util.ResourceBundle" session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String okURL = ok.getPageURL();
    String cancelURL = cancel.getPageURL();
    String title= bundle.getString("lb_create_export_location");
    String nameWarning= bundle.getString("jsmsg_export_location_name");
    String locationWarning= bundle.getString("jsmsg_export_location_dir");
    
    // get a list of all export locations.
    SessionManager sm = (SessionManager)session.getAttribute(
        WebAppConstants.SESSION_MANAGER);
    Object[] locs = (Object[])sm.getAttribute(ExportLocationPageHandler.EXPORT_LOCATIONS);
    int size = locs.length;
%>
<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"> </SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "export";
var CreateExportActionParam = "";
var helpFile = "<%=bundle.getString("help_export_locations_create_edit")%>";

function submitForm(buttonClicked)
{
   var theName = stripBlanks(CreateExportLocationForm.name.value);
   var theLocation = CreateExportLocationForm.filebrowser.value;
   var theDescription = CreateExportLocationForm.description.value;
   
   if (buttonClicked == "ok")
   {
     if (isEmptyString(theName))
     {
       alert('<%=nameWarning%>');
       return false;
     }
     if (hasSpecialChars(theName))
     {
        alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
     }
     if (isEmptyString(theLocation))
     {
       alert('<%=locationWarning%>');
       return false;
     }

     <% for (int i=0; i<size; i++){ %>
	   if(theName.toLowerCase() == "<%= ((ExportLocation)locs[i]).getName().toLowerCase() %>")
	   {
          alert('<%=bundle.getString("jsmsg_duplicate_export_location")%>');
          return false;
	   }
	<% } %>
    
     CreateExportLocationForm.action = "<%=okURL%>";
     CreateExportActionParam = "new";
     CreateExportLocationForm.action +=
       "&" + "<%=WebAppConstants.EXPORT_LOCATION_ACTION%>"
       + "=" + CreateExportActionParam +
       "&" + "<%=WebAppConstants.EXPORT_LOCATION_NEW_NAME%>"
       + "=" + theName +
       "&" + "<%=WebAppConstants.EXPORT_LOCATION_NEW_LOCATION%>"
       + "=" + theLocation +
       "&" + "<%=WebAppConstants.EXPORT_LOCATION_NEW_DESCRIPTION%>"
       + "=" + theDescription ;
   }
   else if (buttonClicked == "cancel")
   {
      CreateExportLocationForm.action = "<%=cancelURL%>";
      CreateExportActionParam = "cancel";
   }
   CreateExportLocationForm.submit();
}

function doLoad()
{
  loadGuides();
  CreateExportLocationForm.name.focus();  
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="doLoad();">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
  <SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538>
      <%=bundle.getString("helper_text_create_export_location")%>
    </TD>
  </TR>
</TABLE>

<P></P>
<FORM NAME="CreateExportLocationForm" METHOD="POST">
<TABLE CELLPADDING=4 CELLSPACING=4 BORDER=0 CLASS=standardText>
  <TR>
    <TD><%=bundle.getString("lb_name")%>:</TD>
    <TD><INPUT ID=name MAXLENGTH="40"></TD>
  </TR>
  <TR>
    <TD><%=bundle.getString("lb_export_directory_location")%>:</TD>
    <TD><INPUT ID=filebrowser MAXLENGTH="4000"></TD>
  </TR>
  <TR>
    <TD><%=bundle.getString("lb_description")%>:</TD>
    <TD><INPUT ID=description MAXLENGTH="4000" ></TD>
  </TR>
</TABLE>
<P></P>
<INPUT CLASS=cancelbutton ID=cancel TYPE=button
 VALUE=<%=bundle.getString("lb_cancel")%> onClick="submitForm('cancel');">
<INPUT CLASS=okbutton ID=ok TYPE=button
 VALUE=<%=bundle.getString("lb_ok")%> onClick="submitForm('ok');">
</FORM>
</BODY>
</HTML>
