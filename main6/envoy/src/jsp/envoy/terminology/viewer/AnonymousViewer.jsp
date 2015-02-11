<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/common/error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<%
//session.setAttribute(WebAppConstants.UILOCALE, Locale.US);

ResourceBundle bundle = PageHandler.getBundle(session);
String xmlTermbases =
  (String)request.getAttribute(WebAppConstants.TERMBASE_TB_NAMELIST);

String lb_clickToOpenTb = bundle.getString("lb_click_to_open_tb");
String lb_close = bundle.getString("lb_close");
String lb_noTermbases = bundle.getString("lb_no_termbase_links");
String lb_termbaseDescHeading = bundle.getString("lb_termbase_desc_heading");
String lb_termbaseHeading = bundle.getString("lb_termbase_link_heading"); 
String lb_termbase = bundle.getString("lb_termbase");
String lb_termbases = bundle.getString("lb_termbases");
String lb_title = bundle.getString("lb_termbases");
String lb_help = bundle.getString("lb_help");
String lb_helptext = bundle.getString("helper_text_tb_anonymous");

String logoImage = skin.getProperty("skin.banner.logoImage");
String logoBackgroundImage = skin.getProperty("skin.banner.logoBackgroundImage");
boolean useOneLogoImage = false;
if (logoImage.equals(logoBackgroundImage))
{
    useOneLogoImage = true;
}
%>
<HTML >
<HEAD>
<TITLE><%=lb_title%></TITLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
var helpFile = "<%=bundle.getString("help_terminology_anonymous_main_screen")%>";
var xmlStr = "<%=xmlTermbases%>";

function showHelp() 
{  
    var helpWindow = window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function doLoad()
{
  var dom;
  if(isIE)
  {
    dom = oTermbases.XMLDocument;
  }
  else if(window.DOMParser)
  { 
    var parser = new DOMParser();
    dom = parser.parseFromString(xmlStr,"text/xml");
  }
  var nodes = dom.selectNodes("/termbases/termbase");

  if (nodes.length > 0)
  {
    for (i = tbody.rows.length; i > 0; --i)
    {
      tbody.deleteRow(i-1);
    }

    for (i = 0; i < nodes.length; ++i)
    {
      var bg = (i%2 == 0) ? "white" : "#EEEEEE"; 
      var node = nodes[i];//nodes.item(i);
      var id = node.getAttribute("id");
      var name = node.selectSingleNode("name").text;
      var desc = node.selectSingleNode("description").text;

      row = tbody.insertRow(-1);
      row.style.background = bg;
      cell = row.insertCell(-1);
      cell.setAttribute("id", id.toString(10));

      var s = "<SPAN CLASS='standardHREF' " +
        "STYLE='cursor: pointer; text-decoration: underline;'" + 
        "TITLE='" + "<%=lb_clickToOpenTb%>" +
        "' onclick='ShowTermbase(" + id + ")'>" + name + "</SPAN>";

      cell.innerHTML = s;
     
      cell = row.insertCell(-1);

      s = "<SPAN CLASS='standardText'>"
      s += (desc == "") ? "\u00a0" : desc;
      s += "</SPAN>";
      cell.innerHTML = s;
    }
  }

  window.focus();
}
</SCRIPT>
</HEAD>
<BODY  LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="doLoad()" oncontextmenu="return false">
<DIV style="display:none;">
<XML id="oTermbases"><%=xmlTermbases%></XML>
</DIV>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
  <TR CLASS="header1">
    <% if (useOneLogoImage == true){ %>
    <TD WIDTH="704"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="704"></TD>
    <%} else {%>
    <TD WIDTH="253"><IMG SRC="<%=logoImage%>" HEIGHT="68" WIDTH="253"></TD>
    <TD WIDTH="451"><IMG SRC="<%=logoBackgroundImage%>" HEIGHT="68" WIDTH="451"></TD>
    <%}%>            
    <TD ALIGN="RIGHT" CLASS="header1">&nbsp;</TD>
  </TR>
</TABLE>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px;width:100%">
<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="95%">
  <TR>
    <TD><SPAN class="mainHeading"><%=lb_title%></SPAN></TD>
    <TD ALIGN="RIGHT">
      <SPAN class="HREFBold">
      <A CLASS="HREFBold" HREF="javascript:showHelp();"><%=lb_help%></A>
      </SPAN>
    </TD>
  </TR>
  <TR><TD COLSPAN="2">&nbsp;</TD></TR>
  <TR>
    <TD COLSPAN="2"><SPAN class="standardHREF"><%=lb_helptext%></SPAN></TD>
  </TR>
</TABLE>
<P></P>

<!-- Border table -->
<TABLE CELLSPACING="0" CELLPADDING="1" BORDER="0" WIDTH="650">
  <TR>
    <TD BGCOLOR="#0C1476" ALIGN="CENTER" width="650">

      <!-- Data table -->
      <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="2"
	style="behavior: url(/globalsight/includes/sort.htc)">
	<THEAD>
	  <TR CLASS="tableHeadingBasic">
	    <TD width="150"><%=lb_termbase%></TD>
	    <TD width="500"><%=lb_termbaseDescHeading%></TD>
	  </TR>
	</THEAD>
	<TBODY id="tbody">
	  <TR>
	    <TD COLSPAN="2"><SPAN CLASS="standardText"><%=lb_noTermbases%></TD>
	  </TR>
	</TBODY>
      </TABLE>
      <!-- End Data table -->
      
    </TD>
  </TR>
</TABLE>
<!-- End Border table -->

</DIV>
</BODY>
</HTML>
