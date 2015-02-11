<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.exportlocation.ExportLocationPageHandler,
            com.globalsight.everest.webapp.pagehandler.exportlocation.CreateExportLocationPageHandler,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManager,
            com.globalsight.cxe.entity.exportlocation.ExportLocationImpl,
            com.globalsight.cxe.entity.exportlocation.ExportLocation,
            com.globalsight.util.GeneralException,
            com.globalsight.everest.util.system.SystemConfiguration,
            java.util.Collection,
            javax.naming.NamingException,
            java.util.Vector,
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

    String title= bundle.getString("lb_edit_export_location");
    String disable = "";

    String locationId =
      (String)request.getAttribute(WebAppConstants.EXPORT_LOCATION_MODIFY_ID);
    int id = Integer.parseInt(locationId);
    if(id == 1)
    {
       disable = "DISABLED";
    }
    else
    {
       disable = "";
    }
    String locationName = "";
    String locationDir = "";
    String locationDescription = "";
    String nameWarning= bundle.getString("jsmsg_export_location_name");
    String locationWarning= bundle.getString("jsmsg_export_location_dir");

    try
    {
        ExportLocationPersistenceManager mgr =
          ServerProxy.getExportLocationPersistenceManager();

        ExportLocation el = mgr.readExportLocation(id);

        locationName = el.getName();
        locationDir = el.getLocation();
        locationDescription = el.getDescription();
    }
    catch(GeneralException ge)
    {
      System.out.println("General Exception " + ge.getMessage());
    }
    catch(NamingException ne)
    {
      System.out.println("NamingException during set up.");
    }
    
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
  var originalName = "<%= locationName %>"
  var theName = stripBlanks(ModifyExportLocationForm.name.value);
  var theLocation = ModifyExportLocationForm.filebrowser.value;
  var theDescription = ModifyExportLocationForm.description.value;
  
  if (buttonClicked == "ok")
  {
    if (isEmptyString(theName))
    {
      alert('<%=nameWarning%>');
      return false; 
    }
    if (isEmptyString(theLocation))
    { 
      alert('<%=locationWarning%>');
      return false; 
    }

    <% for (int i=0; i<size; i++){ %>
	   if(theName.toLowerCase() == "<%= ((ExportLocation)locs[i]).getName().toLowerCase() %>" && 
          originalName != "<%= ((ExportLocation)locs[i]).getName() %>")
	   {
          alert('<%=bundle.getString("jsmsg_duplicate_export_location")%>');
          return false;
	   }
	<% } %>
    
    ModifyExportLocationForm.action = "<%=okURL%>"; 
    CreateExportActionParam = "modify";
    ModifyExportLocationForm.action +=
      "&" + "<%=WebAppConstants.EXPORT_LOCATION_ACTION%>"
      + "=" + CreateExportActionParam + 
      "&" + "<%=WebAppConstants.EXPORT_LOCATION_MODIFY_NAME%>"
      + "=" + theName + 
      "&" + "<%=WebAppConstants.EXPORT_LOCATION_MODIFY_ID%>"
      + "=" + <%=locationId%> + 
      "&" + "<%=WebAppConstants.EXPORT_LOCATION_MODIFY_LOCATION%>"
      + "=" + theLocation + 
      "&" + "<%=WebAppConstants.EXPORT_LOCATION_MODIFY_DESCRIPTION%>"
      + "=" + theDescription ;
   }
   else if (buttonClicked == "cancel")
   {
      ModifyExportLocationForm.action = "<%=cancelURL%>";
      CreateExportActionParam = "cancel";
   }
   ModifyExportLocationForm.submit();
}

function doLoad()
{
  loadGuides();
  ModifyExportLocationForm.name.focus();
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
<FORM NAME="ModifyExportLocationForm" METHOD="POST">
<TABLE CELLPADDING=4 CELLSPACING=4 BORDER=0 CLASS=standardText>
  <TR>
    <TD><%=bundle.getString("lb_name")%>:</TD>
    <TD><INPUT ID=name VALUE="<%=locationName%>" <%=disable%>></TD>
  </TR>
  <TR>
    <TD><%=bundle.getString("lb_export_directory_location")%>:</TD>
    <TD><INPUT ID=filebrowser VALUE="<%=locationDir%>"<%=disable%>></TD>
  </TR>
  <TR>
    <TD><%=bundle.getString("lb_description")%>:</TD>
    <TD><INPUT ID=description VALUE="<%=locationDescription%>"<%=disable%>></TD>
  </TR>
</TABLE>
<P></P>
<INPUT ID=id TYPE="HIDDEN" STYLE="left: 35px ; width: 165px ;" VALUE="<%=locationId%>">
<INPUT CLASS=cancelbutton ID=cancel TYPE=button
 VALUE=<%=bundle.getString("lb_cancel")%>  onClick="submitForm('cancel');">
<INPUT CLASS=okbutton ID=ok TYPE=button
 VALUE=<%=bundle.getString("lb_ok")%>  onClick="submitForm('ok');">
</FORM>
</BODY>
</HTML>
