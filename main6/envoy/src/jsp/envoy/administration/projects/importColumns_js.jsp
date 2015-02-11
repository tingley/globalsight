// -*- mode: javascript -*-
<%@ page
    contentType="text/javascript; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
%>


var columnId   = -1;
var columnName = "";

var aColumns = new Array();
var aColumn = new Array();

function Properties(id)
{
    this.id = id;
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
    	this.importOptions = oImportOptions.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      	var parser = new DOMParser();
      	this.importOptions = parser.parseFromString(xmlImportOptions,"text/xml");
    }
}


function Associate(event)
{
    var elem = event.srcElement;
    while (elem.tagName.toUpperCase() != "TR")
    {
        //elem = elem.parentElement;
        elem = elem.parentElement||elem.parentNode;
    }
    var id = elem.getAttribute("id");

    var oProperties = new Properties(id);

    oProperties = window.showModalDialog(
      "/globalsight/envoy/administration/projects/associate.jsp", oProperties,
      "dialogHeight:325px; dialogWidth:400px; center:yes; " +
      "resizable:no; status:no;");

    if (oProperties != null)
    {
        idBody.cursor = "wait";
        var dom;
        if(window.navigator.userAgent.indexOf("MSIE")>0)
    	{
      		dom = oImportOptions.XMLDocument;
    	}
    	else if(window.DOMParser)
    	{ 
      		//var parser = new DOMParser();
      		//dom = parser.parseFromString(xmlImportOptions,"text/xml");
      		dom = oProperties.importOptions;
    	}
		var node;
 		var nodes = $(dom).find("importOptions columnOptions column");
        for(i=0;i < nodes.length;i++)
        {
        	var attrVlue = $(nodes[i]).attr("id");
        	if(attrVlue == id)
        	{
        		node = nodes[i]
        	}
        }

        name = $(node).find("name").text();
        example = $(node).find("example").text();
        type = $(node).find("type").text();
        subtype = $(node).find("subtype").text();

        aColumns[id] = new Column(id, name, example, type, subtype);

        showColumns();

        idBody.cursor = "auto";
    }
}

function showColumns()
{
   var tbody = idColumnsBody;
   for (i = tbody.rows.length; i > 0; --i)
   {
      tbody.deleteRow(i-1);
   }

   for (i = 0; i < aColumns.length; ++i)
   {
      var oColumn = aColumns[i];

      var row, cell;

      row = tbody.insertRow(i);//row = tbody.insertRow();
      row.setAttribute("id", oColumn.id);

      // Column Header
      var j=0;
      cell = row.insertCell(j++);//cell = row.insertCell();
      cell.innerText = oColumn.name;

      // Data Sample
      cell = row.insertCell(j++);//cell = row.insertCell();
      cell.innerText = oColumn.example;

      // Field Type
      cell = row.insertCell(j++);
      cell.innerText = oColumn.type;

      // Field Sub Type
      cell = row.insertCell(j++);
      cell.innerText = oColumn.subtype;

      // Properties Configuration Button
      cell = row.insertCell(j++);
      var prop = "propId" + (i+1).toString(10);

      cell.innerHTML = "<INPUT TYPE='BUTTON' name='" + prop +
         "' value='" + "<%=bundle.getString("lb_configure")%>" +
         "' onclick='Associate(event)'></INPUT>";
   }
}
