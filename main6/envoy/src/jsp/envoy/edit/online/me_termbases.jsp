<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.glossaries.GlossaryFile,
            com.globalsight.everest.glossaries.GlossaryUpload,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.*"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String xmlTermbases = state.getTermbaseNames();
String defaultTermbase = state.getDefaultTermbaseName();

String lb_clickToOpenTb = bundle.getString("lb_click_to_open_tb");
String lb_close = bundle.getString("lb_close");
String lb_noTermbases = bundle.getString("lb_no_termbase_links");
String lb_termbaseDescHeading = bundle.getString("lb_termbase_desc_heading");
String lb_termbaseHeading = bundle.getString("lb_termbase_link_heading"); 
String lb_termbase = bundle.getString("lb_termbase");
String lb_termbases = bundle.getString("lb_termbases");
String lb_title = bundle.getString("lb_termbases");
String lb_help = bundle.getString("lb_help");
%>
<HTML>
<!-- This JSP is envoy/edit/online/me_termbases.jsp -->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>

<SCRIPT>
var defaultTermbase = "<%=defaultTermbase%>";
var helpFile = "<%=bundle.getString("help_main_editor_termbases")%>";
var xmlTermbases = 
	"<%=xmlTermbases.replace("\\", "\\\\").replace("\r","").replace("\n","").trim()%>";
	
function helpSwitch() 
{  
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function doLoad()
{
  var dom = $.parseXML(xmlTermbases);

  var nodes = $(dom).find("termbases termbase");  
  if (nodes.length > 0)
  {
    for (i = tbody.rows.length; i > 0; --i)
    {
      tbody.deleteRow(i-1);
    }

    for (i = 0; i < nodes.length; ++i)
    {
      var node = nodes[i];//var node = nodes.item(i);
      var id = $(node).attr("id");
      var name = $(node).find("name").text();
      var desc = $(node).find("description").text();
      
      row = tbody.insertRow(-1);//row = tbody.insertRow();
      cell = row.insertCell(-1);//cell = row.insertCell();
      cell.setAttribute("id", id.toString(10));

      var s = "<SPAN CLASS='standardHREF' " +
        "STYLE='cursor: hand; cursor:pointer; text-decoration: underline;'" + 
        "TITLE='" + "<%=lb_clickToOpenTb%>" +
        "' onclick='ShowTermbase(" + id + ")'>";

      if (name == defaultTermbase)
      {
        s += "<B>" + name + "</B>";
      }
      else
      {
        s += name;
      }
      s += "</SPAN>";

      cell.innerHTML = s;
     
      cell = row.insertCell(-1);//cell = row.insertCell();

      s = "<SPAN CLASS='standardText'>"
      s += (desc == "") ? "\u00a0" : desc;
      s += "</SPAN>";
      cell.innerHTML = s;
    }
  }

  window.focus();
}

function closeThis()
{
    window.close();
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()" oncontextmenu="return false">
<DIV style="display:none">
<XML id="oTermbases"><%=xmlTermbases%></XML>
</DIV>

<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
  <TR>
    <TD><SPAN class="mainHeading"><%=lb_title%></SPAN></TD>
    <TD ALIGN="RIGHT">
      <SPAN class="HREFBold">
        <A CLASS="HREFBold" HREF="javascript:helpSwitch();"><%=lb_help%></A> |
        <A CLASS="HREFBold" HREF="javascript:closeThis();"><%=lb_close%></A> 
      </SPAN>
    </TD>
  </TR>
</TABLE>
<P></P>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="0">
  <THEAD>
    <TR CLASS="tableHeadingBasic">
      <TD><%=lb_termbase%></TD>
      <TD><%=lb_termbaseDescHeading%></TD>
    </TR>
  </THEAD>
  <TBODY id="tbody">
    <TR>
      <TD COLSPAN="2"><SPAN CLASS="standardText"><%=lb_noTermbases%>
      </TD>
    </TR>
  </TBODY>
</TABLE>
</BODY>
</HTML>
