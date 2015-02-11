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
String lb_configure = bundle.getString("lb_configure");
%>
/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

var columnId   = -1;
var columnName = "";

var aColumns = new Array();
var aColumn = new Array();

function Properties(id)
{
    this.id = id;
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
      	//this.importOptions = oImportOptions.XMLDocument;
    	//this.definition = oDefinition.XMLDocument;
    	var domOption=new ActiveXObject("Microsoft.XMLDOM");
        domOption.async="false";
        domOption.loadXML(xmlImportOptions);
        this.importOptions = domOption;
        
        var domDefinition=new ActiveXObject("Microsoft.XMLDOM");
        domDefinition.async="false";
        domDefinition.loadXML(xmlDefinition);
        this.definition = domDefinition;
    }
    else if(window.DOMParser)
    { 
      	var parser = new DOMParser();
      	this.importOptions = parser.parseFromString(xmlImportOptions,"text/xml");
    	this.definition = parser.parseFromString(xmlDefinition,"text/xml");
    }
    
    this.name				= null;
    this.type 			  	= null;
    this.associatedColumn 	= null;
    this.termLanguage		= null;
}


function Associate(event)
{
    //var elem = window.event.srcElement;//
    var elem = event.srcElement;
    while (elem.tagName.toUpperCase() != "TR")
    {
        elem = elem.parentElement||elem.parentNode;
    }
    var id = elem.getAttribute("id");
    var oProperties = new Properties(id);

    oProperties = window.showModalDialog(
      "/globalsight/envoy/terminology/management/associate.jsp", oProperties,
      "dialogHeight:325px; dialogWidth:400px; center:yes; " +
      "resizable:no; status:no;");

    if (oProperties != null)
    {
        idBody.cursor = "wait";
        
        var dom,nodes;
        if(window.navigator.userAgent.indexOf("MSIE")>0)
    	{
      		//dom = oImportOptions.XMLDocument;
      		dom=new ActiveXObject("Microsoft.XMLDOM");
        	dom.async="false";
        	dom.loadXML(xmlImportOptions);
    	}
    	else if(window.DOMParser)
    	{ 
      		var parser = new DOMParser();
      		dom = parser.parseFromString(xmlImportOptions,"text/xml");
    	}

        //node = dom.selectSingleNode("/importOptions/columnOptions/column[@id='" + id + "']");
        nodes = $(dom).find("importOptions columnOptions column");
        for(i=0;i < nodes.length;i++)
        {
        	var attrVlue = $(nodes[i]).attr("id");
        	if(attrVlue == id)
        	{
        		node = nodes[i]
        	}
        }

        var example = $(node).find("example").text();
        var encoding = $(node).find("encoding").text();
        var name 				= oProperties.name;
    	var type 				= oProperties.type;
    	var associatedColumn 	= oProperties.associatedColumn;
    	var termLanguage 		= oProperties.termLanguage;

        aColumns[id] = new Column(id, name, example, type,
            encoding, associatedColumn, termLanguage);

        showColumns();

        idBody.cursor = "auto";
    }
}

function showColumns()
{
   var tbody = document.getElementById("idColumnsBody");
   for (i = tbody.rows.length; i > 0; --i)
   {
      tbody.deleteRow(i-1);
   }

   for (var i = 0; i < aColumns.length; ++i)
   {
      var oColumn = aColumns[i];
	  var j=0;
      var row, cell;

      row = tbody.insertRow(i);//row = tbody.insertRow();
      row.setAttribute("id", oColumn.id);

      // Column Header
      cell = row.insertCell(j++);//cell = row.insertCell();
      cell.innerText = oColumn.name;

      // Data Sample
      cell = row.insertCell(j++);
      cell.innerText = oColumn.example;

      // Encoding
      // cell = row.insertCell();
      // cell.innerText = oColumn.encoding;

      // Field Type
      cell = row.insertCell(j++);
      cell.innerText = getImportFieldNameByType(oColumn.type);

      // Associated Column
      cell = row.insertCell(j++);
      if (oColumn.associatedColumn != -1)
      {
        cell.innerText = aColumns[oColumn.associatedColumn].name;
      }
      else
      {
        cell.innerText = "-";
      }

      // Term Language
      cell = row.insertCell(j++);
      if (oColumn.type == "term" && oColumn.termLanguage != "unknown")
      {
          cell.innerText = oColumn.termLanguage;
      }
      else
      {
          cell.innerText = "-";
      }

      // Properties Configuration Button
      cell = row.insertCell(j++);
      var prop = "propId" + (i+1).toString(10);

      cell.innerHTML = "<INPUT TYPE='BUTTON' name='" + prop +
       "' value='<%=lb_configure%>' onclick='Associate(event)'></INPUT>";
   }
}
