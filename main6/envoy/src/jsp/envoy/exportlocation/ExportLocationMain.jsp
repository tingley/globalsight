<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.exportlocation.ExportLocationPageHandler,
            com.globalsight.everest.webapp.pagehandler.exportlocation.ExportLocationComparator,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.ServerProxy,
            java.util.Vector,
            com.globalsight.everest.util.system.SystemConfiguration,
            java.util.ResourceBundle" 
    session="true" %>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="makedefault" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="newone" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="base" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String removeURL = remove.getPageURL();
    String makeDefaultURL = makedefault.getPageURL();
    String modifyURL = modify.getPageURL();
    String newURL = newone.getPageURL();
    String baseURL = base.getPageURL();
    String title = bundle.getString("lb_export_locations");
%>                       
<HTML>
<!-- This is envoy\exportlocation\ExportLocationMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "export";
var ExportActionParam = "";
var helpFile = "<%=bundle.getString("help_export_locations_main_screen")%>";

function submitForm(buttonClicked) 
{
   if (buttonClicked == "Remove")
   {
	  if ( !isRadioChecked(ExportLocationForm.locId) ) return false; 
      if ( !confirm("<%=bundle.getString("jsmsg_warning")%>\n\n" + 
                    "<%=bundle.getString("jsmsg_remove_export_location")%>"))
      {
         return false;
      };

      ExportLocationForm.action = "<%=removeURL%>";
      ExportActionParam = "remove";
   }
   else if (buttonClicked == "MakeDefault")
   {
	  if ( !isRadioChecked(ExportLocationForm.locId) ) return false; 
      ExportLocationForm.action = "<%=makeDefaultURL%>"; 
      ExportActionParam = "makeDefault";
   }
   else if (buttonClicked == "Modify")
   {
	  if ( !isRadioChecked(ExportLocationForm.locId) ) return false; 
      ExportLocationForm.action = "<%=modifyURL%>";
      ExportActionParam  = "modify";
   }
   else if (buttonClicked == "New")
   {
      ExportLocationForm.action = "<%=newURL%>";
      ExportActionParam = "new";
   }

   // If they click New, just submit the form
   // and don't get the ID
   if (buttonClicked != "New")
   {
      var id = getLocId(buttonClicked);
      if ((id == 1) && (buttonClicked == "Remove"))
      {
         alert("<%=bundle.getString("jsmsg_remove_default_location")%>");
         return false;
      }
	else if ((id == 1) && (buttonClicked == "Modify"))
      {
         alert("<%=bundle.getString("jsmsg_edit_default_location")%>");
         return false;
      }
      else if ((id == false) && (buttonClicked == "Remove"))
      {
         alert("<%=bundle.getString("jsmsg_remove_only_location")%>");
         return false; 
      }
      ExportLocationForm.action += "&" + ExportActionParam + "=" + id ;
      ExportLocationForm.action += "&" + "<%=WebAppConstants.EXPORT_LOCATION_MODIFY_ID%>" 
         + "=" + id ;

      ExportLocationForm.action += "&" + "<%=WebAppConstants.EXPORT_LOCATION_ACTION%>" + 
         "=" + ExportActionParam;
   }
   ExportLocationForm.submit();
}

function getLocId(buttonClicked)
{
   // If more than one radio button is displayed, the length attribute of the 
   // radio button array will be non-zero, so find which 
   // one is checked
   if (ExportLocationForm.locId.length)
   {
      for (i = 0; i < ExportLocationForm.locId.length; i++) 
      {
         if (ExportLocationForm.locId[i].checked == true) 
         {
            locId = ExportLocationForm.locId[i].value;
            break;
         }
       }
    }
    else {
      // If only one is displayed, there is no radio button array, so
      // just check if the single radio button is checked
      if (ExportLocationForm.locId.checked == true)
      {
         if (buttonClicked == "Remove")
         {
            // You can't remove this entry. 
            // You must have at least one export location.
            return false;
         }
         locId = ExportLocationForm.locId.value;
      }
    }
   return locId;
}    
</SCRIPT>
<STYLE type="text/css">
.list {
	position: relative; 
	width: 600; 
	top: -2px; 
	overflow-y: auto; 
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
	padding: 0px;
}
</STYLE>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=538>
<%=bundle.getString("helper_text_export_location")%>
</TD>
</TR>
</TABLE>

<P>

<FORM NAME="ExportLocationForm" METHOD="POST">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR>
        <TD COLSPAN=5>
<!-- Layer for scrolling table -->
<DIV class="list">
    <TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="600" STYLE="table-layout: fixed;">
            <COL WIDTH=20>  <!-- Radio button -->
            <COL WIDTH=100> <!-- Name -->
            <COL WIDTH=200> <!-- Export Location -->
            <COL WIDTH=200> <!-- Description -->
            <COL WIDTH=80>  <!-- Default -->
            <TR CLASS="tableHeadingBasic">
                <TD HEIGHT="20">&nbsp;</TD>
                <TD HEIGHT="20"><A CLASS="sortHREFWhite" HREF="<%=baseURL + "&" + ExportLocationPageHandler.SORT_PARAM + "=" + ExportLocationComparator.NAME%>">
                <%=bundle.getString("lb_name")%></A></TD>
                <TD HEIGHT="20"><A CLASS="sortHREFWhite" HREF="<%=baseURL + "&" + ExportLocationPageHandler.SORT_PARAM + "=" + ExportLocationComparator.LOCATION%>">
                <%=bundle.getString("lb_export_location")%></A></TD>
                <TD HEIGHT="20"><A CLASS="sortHREFWhite" HREF="<%=baseURL + "&" + ExportLocationPageHandler.SORT_PARAM + "=" + ExportLocationComparator.DESCRIPTION%>">
                <%=bundle.getString("lb_description")%></A></TD>
                <TD HEIGHT="20"><%=bundle.getString("lb_default")%></TD>
            </TR>
            <%=request.getAttribute(ExportLocationPageHandler.EXPORT_LOCATION_SCRIPTLET)%>
     </TABLE>
</DIV>
        </TD>
     </TR>
     <TR ALIGN="RIGHT">
     <TD>
<amb:permission name="<%=Permission.EXPORT_LOC_REMOVE%>" >
<INPUT TYPE="BUTTON" NAME=Remove VALUE="<%=bundle.getString("lb_remove")%>" onClick="submitForm('Remove');">
</amb:permission>
<amb:permission name="<%=Permission.EXPORT_LOC_DEFAULT%>" >
<INPUT TYPE="BUTTON" NAME=MakeDefault VALUE="<%=bundle.getString("lb_make_default")%>" onClick="submitForm('MakeDefault');">
</amb:permission>
<amb:permission name="<%=Permission.EXPORT_LOC_EDIT%>" >
<INPUT TYPE="BUTTON" NAME=Modify VALUE="<%=bundle.getString("lb_edit")%>..." onClick="submitForm('Modify');">
</amb:permission>
<amb:permission name="<%=Permission.EXPORT_LOC_NEW%>" >
<INPUT TYPE="BUTTON" NAME=New VALUE="<%=bundle.getString("lb_new")%>..." onClick="submitForm('New');">
</amb:permission>
     </TD>
     </TR>
</TABLE>
</FORM>

</DIV>
</BODY>
</HTML>

