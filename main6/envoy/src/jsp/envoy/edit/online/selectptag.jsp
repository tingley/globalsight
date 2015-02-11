<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_select_ptag = bundle.getString("lb_select_ptag");
String lb_select_ptag1 = bundle.getString("lb_select_ptag1");
String lb_no_ptags_in_segment = bundle.getString("lb_no_ptags_in_segment");
String lb_close = bundle.getString("lb_close");
%>
<HTML>
<HEAD>
<TITLE><%=lb_select_ptag%></TITLE>

<STYLE TYPE="text/css">
 BODY   { margin: 10px; background-color: white;
          font-family: Verdana; font-size: 12; 
        }
 BUTTON { width: 5em; }
 TABLE  { font-family: Courier; font-size: 12; }
 P      { }
 #idTags{ text-align: center; }
.title  { text-align: center; font-weight: bold; }
.link   { cursor: hand;cursor:pointer; color: blue; }
.ptag   { color: #3366FF; }
</STYLE>

<SCRIPT LANGUAGE=JavaScript>
var opener = null;
var data = null;

function doOnLoad()
{
  opener = window.dialogArguments._opener;
  data = window.dialogArguments._data;

  if (data.length > 0)
  {
    for (var i = 0; i < data.length; i += 2)
    {
      addRow(data[i], data[i+1]);
    }
  }
  else
  {
    idTags.innerHTML = "<%=lb_no_ptags_in_segment%>";
  }
}

function addRow(tag1, tag2)
{
  var table = idTable.tBodies(0);
  var row =   document.createElement("TR");
  var cell1 = document.createElement("TD");
  var cell2 = document.createElement("TD");

  cell1.value = tag1;
  cell1.innerHTML =
    "<SPAN class='link' onclick='doClick(this)'>" + tag1 + "</SPAN>";

  if (tag2)
  {
    cell2.value = tag2;
    cell2.innerHTML = 
      "<SPAN class='link' onclick='doClick(this)'>" + tag2 + "</SPAN>";
  }
  else
  {
    cell2.innerText = '\u00a0';
  }

  row.appendChild(cell1);
  row.appendChild(cell2);
  table.appendChild(row);
}

function doClick(elem)
{
  opener.InsertPTag(elem.parentNode.value);
}
</SCRIPT>
</HEAD>

<BODY onload="doOnLoad()">
<P class="title"><%=lb_select_ptag1%></P>

<DIV id="idTags">
<TABLE ALIGN="center" id="idTable" CELLSPACING="10"></TABLE>
</DIV>

<P ALIGN="center">
<BUTTON ONCLICK="window.close();"><%=lb_close%></BUTTON>
</P>

</BODY>
</HTML>
