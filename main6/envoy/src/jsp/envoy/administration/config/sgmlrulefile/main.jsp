<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.ling.sgml.sgmlrules.SgmlRule,
        java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="create" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String urlCreate = create.getPageURL();
String urlUpload = upload.getPageURL();
String urlEdit = edit.getPageURL();
String urlSelf  = self.getPageURL();

String title = bundle.getString("lb_sgml_rules");
String lb_createBtn = bundle.getString("lb_create1") + " " + bundle.getString("lb_dtd");
String lb_uploadBtn = bundle.getString("lb_upload") + " " + bundle.getString("lb_dtd");
String lb_removeBtn = bundle.getString("lb_remove") + " " + bundle.getString("lb_dtd");
String lb_editBtn = bundle.getString("lb_edit") + " " + bundle.getString("lb_rules");

String xmlDtds = (String)sessionMgr.getAttribute("dtds");

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute("error");
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("msg_server_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement("error");

%>
<HTML>
<!-- This is envoy\src\jsp\envoy\administration\config\sgmlrulefile\main.jsp -->
<HEAD>
<TITLE><%=title %></TITLE>
<STYLE>
TD {
    font: 10pt arial;
}

#idRules {
    background-color: #ffffff;
    table-layout: fixed;
    behavior: url(/globalsight/includes/sort.htc) url(/globalsight/includes/rowover.htc);
}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT src="/globalsight/envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT src="/globalsight/includes/overlib.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "sgmlRules";
var helpFile = "<%=bundle.getString("help_sgml_rules_main")%>";
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
var xmlStr = "<%=xmlDtds.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "").trim()%>";

function findSelectedRadioButton()
{
   var id;

   // If there are TMs defined...
   if (TMForm.TMId)
   {
       // If more than one radio button is displayed, the length
       // attribute of the radio button array will be non-zero, so
       // find which one is checked
       if (TMForm.TMId.length)
       {
           for (i = 0; i < TMForm.TMId.length; i++)
           {
               if (TMForm.TMId[i].checked == true)
               {
                   id = TMForm.TMId[i].value;
                   break;
               }
           }
       }
       else
       {
           // If only one is displayed, there is no radio button
           // array, so just check if the single radio button is
           // checked
           if (TMForm.TMId.checked == true)
           {
               id = TMForm.TMId.value;
           }
       }
   }

   return id;
}

function uploadDTD()
{
    window.location.href = '<%=urlUpload%>';
}

function createDTD()
{
    window.location.href = '<%=urlCreate%>';
}


function removeDTD()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=bundle.getString("jsmsg_sgml_select_dtd_remove")%>");
  }
  else
  {
    var ok = confirm("<%=bundle.getString("jsmsg_sgml_remove_dtd")%>");
    if (ok)
    {
      var form = oFormDelete;
      form.publicid.value = id;
      form.submit();
    }
  }
}

function editRules()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=bundle.getString("jsmsg_sgml_select_dtd")%>");
    return;
  }

  var index = getStatusIndex(id);
  var status = statuses[index];

  if (status != "ok")
  {
    alert("<%=bundle.getString("jsmsg_sgml_not_parsed")%>");
    return;
  }

  var form = oFormEdit;
  form.publicid.value = id;
  form.submit();
}

function selectRow()
{
  var index = event.srcRow.rowIndex;

  if (index > 0)
  {
    var radios = TMForm.TMId;

    if (radios.length)
    {
      radios[index-1].checked = true;
    }
    else
    {
      radios.checked = true;
    }
  }
}


function escapeEntities(s)
{
  return String(s).replace(/\&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\"/g, "&quot;");
}

var names = new Array();
var statuses = new Array();

function getStatusIndex(id)
{
  for (var i = 0; i < names.length; i++)
  {
    if (names[i] == id) return i;
  }
}

function showerror(i)
{
  var error = statuses[i];
  return overlib(error, STICKY, WIDTH, 200, LEFT, OFFSETY, -110,
    OFFSETX, 130, SNAPX, 10, SNAPY, 10, CAPTION, "DTD Parse Error");
}

function doLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();

  eval("<%=errorScript%>");
  
  var tbody = document.getElementById("idTBody");
  var row, cell;

  for (i = tbody.rows.length; i > 0; --i)
  {
    tbody.deleteRow(i-1);
  }

  var dom;
  if(isIE)
  {
    dom = oDTDs.XMLDocument;
  }
  else if(window.DOMParser)
  { 
    var parser = new DOMParser();
    dom = parser.parseFromString(xmlStr,"text/xml");
  }
  var nodes = dom.selectNodes("/dtds/dtd");

  
  for (i = 0; i < nodes.length; ++i)
  {
    var node = nodes[i];//var node = nodes.item(i);
    var publicid = node.selectSingleNode("publicid").text;
    var systemid = node.selectSingleNode("systemid").text;
    var status = node.selectSingleNode("status").text;

    names.push(publicid);
    statuses.push(status);

    var bgColor = "#eeeeee";
    if(i%2==0)
    {
    	bgColor = "white";
    }

    row = tbody.insertRow(i);
    var j = 0;
    cell = row.insertCell(j++);
    cell.innerHTML =
      "<INPUT TYPE=RADIO NAME=TMId VALUE=\"" + publicid + "\" ID=db" + i + ">";
    cell.style.fontSize = 10 + "pt";
    cell.style.background = bgColor;

    cell = row.insertCell(j++);
    cell.innerText = publicid;
    cell.style.fontSize = 10 + "pt";
    cell.style.background = bgColor;    

    cell = row.insertCell(j++);
    cell.innerText = systemid;
    cell.style.fontSize = 10 + "pt";
    cell.style.background = bgColor;

    cell = row.insertCell(j++);
    cell.innerHTML = (status == "ok") ?
      "<span style='background-color: green'>&nbsp;OK&nbsp;</span>" :
      "<span style='background-color: red' onmouseover='return showerror(" + i +
      ")' onmouseout='return nd();'>&nbsp;Error&nbsp;</span>";
    cell.style.fontSize = 10 + "pt";
    cell.style.background = bgColor;
  }

  idRules.Format();
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<XML id="oDTDs" style="display:none"><%=xmlDtds%></XML>
<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title %></SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538>
        <%=bundle.getString("msg_sgml_main_text")%>
    </TD>
  </TR>
</TABLE>

<P>
<FORM NAME=TMForm>
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="2">
  <TR>
    <TD><SPAN CLASS="standardTextBold"><%=bundle.getString("lb_sgml_rules_known") %>:</SPAN></TD>
  </TR>
  <TR>
    <TD valign="top" WIDTH="725">
    <!-- Border table -->
    <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1" WIDTH="100%">
      <TR>
        <TD BGCOLOR="#0C1476" ALIGN="CENTER" width="725">
          <!-- Data table -->
          <TABLE id="idRules" BORDER="0" CELLSPACING="0" CELLPADDING="2"
            STRIPED="true" SELECTABLE="true" onrowselect="selectRow()">
            <THEAD>
              <TR>
                <TD WIDTH="25" class="tableHeadingBasic">&nbsp;</TD>
                <TD WIDTH="450" class="tableHeadingBasic"><%=bundle.getString("lb_public_id")%></TD>
                <TD WIDTH="200" class="tableHeadingBasic"><%=bundle.getString("lb_system_id")%></TD>
                <TD WIDTH="50" class="tableHeadingBasic"><%=bundle.getString("lb_status")%></TD>
              </TR>
            </THEAD>
            <TBODY id="idTBody"/>
            </TABLE>
            <!-- End Data table -->
          </TD>
        </TR>
      </TABLE>
      <!-- End Border table -->
    </TD>
  </TR>
</TABLE>
</FORM>

<DIV>
<amb:permission name="<%=Permission.SGMLRULE_UPLOAD%>" >
<INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_uploadBtn%>"
 ID="idUpload" onclick="uploadDTD()">
</amb:permission>

<amb:permission name="<%=Permission.SGMLRULE_CREATE%>" >
<INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_createBtn%>"
 ID="idCreate" onclick="createDTD()">
</amb:permission>

<amb:permission name="<%=Permission.SGMLRULE_REMOVE%>" >
<INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_removeBtn%>"
 ID="idRemove" onclick="removeDTD()">
</amb:permission>
&nbsp;
<amb:permission name="<%=Permission.SGMLRULE_EDIT%>" >
<INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=lb_editBtn%>"
 ID="idEdit" onclick="editRules()">
</amb:permission>
</DIV>
<BR>

<form name="oFormDelete" action="<%=urlSelf%>" method="post">
<input type="hidden" name="action" value="delete">
<input type="hidden" name="publicid" value="">
</form>

<form name="oFormEdit" action="<%=urlEdit%>" method="post">
<input type="hidden" name="action" value="edit">
<input type="hidden" name="publicid" value="">
</form>

<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>

</DIV>
</BODY>
</HTML>
