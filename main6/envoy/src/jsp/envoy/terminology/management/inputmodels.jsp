<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
	com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.terminology.IUserdataManager,
        com.globalsight.util.edit.EditUtil,
	java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_request" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);

String xmlNames =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_OBJECT_NAMELIST);
xmlNames = xmlNames.replaceAll("\n","");

sessionMgr.removeElement(WebAppConstants.TERMBASE_OBJECT_NAMELIST);

String urlDone = done.getPageURL();
String urlRequest = _request.getPageURL();
String urlEditor = editor.getPageURL();

// Perform error handling, then clear out session attribute.
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);

String title = bundle.getString("lb_termbase_manage_input_mods");
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=title %></TITLE>
<STYLE>
TD {
    font: 10pt arial;
}

FORM { display: inline; }

#idSystemNames,
#idUserNames {
    background-color: #ffffff;
    table-layout: fixed;
}
</STYLE>
<STYLE type="text/css" MEDIA="all">
@import url("/globalsight/includes/stylesMac.css");
</STYLE>
<script src="/globalsight/includes/library.js"></script>
<script src="/globalsight/includes/xmlextras.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="Javascript" src="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="envoy/terminology/management/userobjects.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="envoy/terminology/management/inputmodels_js.jsp"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_terminology_input_models")%>";
</SCRIPT>
<SCRIPT LANGUAGE="JAVASCRIPT">
var g_objectType = "<%=IUserdataManager.TYPE_INPUTMODEL%>";
var g_editorUrl  = "<%=urlEditor%>";

var g_checkmark = '<IMG SRC="/globalsight/images/checkmark.gif" ' +
  'HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3>' +
  '<SPAN style="visibility:hidden">1</SPAN>';

var g_noCheckmark = '<SPAN style="visibility:hidden">0</SPAN>';

function doDone()
{
    window.location.href = "<%=urlDone%>";
}

function doLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();

  var tbody, row, cell;

  var xmlDoc;
  
  if (window.ActiveXObject){
        xmlDoc = new ActiveXObject('Msxml2.DOMDocument');
    }
    else {
        xmlDoc = document.implementation.createDocument("", "", null);
  }

  xmlDoc.loadXML('<%=xmlNames%>');
  //var dom = oNames.XMLDocument;
 
  var nodes = xmlDoc.selectNodes("/names/name");

  for (i = 0; i < nodes.length; ++i)
  {
     var bg = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
    var node = nodes[i];

    var type = node.getAttribute("type");
    var username = node.getAttribute("user");
    var isDefault = node.getAttribute("isdefault");
    var name = node.text;

    aObjects.push(
      new UserObject(g_objectType, username, name, null, isDefault));

    if (type == "system")
    {
      tbody = idTBodySystem;
    }
    else
    {
      tbody = idTBodyUser;
    }

    row = tbody.insertRow(-1);
    row.style.background  = bg;
    cell = row.insertCell(-1);
    cell.innerHTML =
      "<INPUT TYPE=RADIO NAME=checkbox VALUE='" + (i + 1) + "' ID='id" + i +  "'>";

    if (type == "user")
    {
      cell = row.insertCell(-1);
      cell.innerHTML = "<label for=id" + i + ">" + username + "</label>";
    }

    cell = row.insertCell(-1);
    cell.innerHTML = "<label for=id" + i + ">" + name + "</label>";

    cell = row.insertCell(-1);
    if (isDefault == "true" && type == "system")
    {
      cell.innerHTML = g_checkmark;
    }
    else
    {
      cell.innerHTML = g_noCheckmark /*'\u00a0'*/;
    }
  }
}

if(!document.all){
    //  
    XMLDocument.prototype.loadXML = function(xmlString){  
        var childNodes = this.childNodes;  
        
        for (var i = childNodes.length - 1; i >= 0; i--){  
            this.removeChild(childNodes[i]);  
        }  

        var dp = new DOMParser();  
        var newDOM = dp.parseFromString(xmlString, "text/xml");  
        var newElt = this.importNode(newDOM.documentElement, true);  
        this.appendChild(newElt);  
    }  

     // prototying the XMLDocument  
     XMLDocument.prototype.selectNodes = function(cXPathString, xNode){  

         if( !xNode ) { xNode = this; } 
           
         var oNSResolver = this.createNSResolver(this.documentElement)  ;
         var aItems = this.evaluate(cXPathString, xNode, oNSResolver,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null) ; 
         var aResult = []; 
          
         for( var i = 0; i < aItems.snapshotLength; i++){  
             aResult[i] =   aItems.snapshotItem(i);  
         }
           
         return aResult;  
    }  
    // prototying the Element  
    Element.prototype.selectNodes = function(cXPathString){  
       if(this.ownerDocument.selectNodes){  
           return this.ownerDocument.selectNodes(cXPathString, this);  
       }else{throw "For XML Elements Only";}  
    } 
     
    XMLDocument.prototype.selectSingleNode = function(cXPathString, xNode) {
        if( !xNode ) { xNode = this; }
        
        var xItems = this.selectNodes(cXPathString, xNode);  

        if(xItems.length > 0){  
            return xItems[0];  
        }else{
            return null;  
        }  
    }  
    // prototying the Element  
    Element.prototype.selectSingleNode = function(cXPathString) {  
        if(this.ownerDocument.selectSingleNode){  
            return this.ownerDocument.selectSingleNode(cXPathString, this);  
        }else{throw "For XML Elements Only";}  
    } 
     
    //  
    Element.prototype.__defineGetter__( "text",  function(){  
        return this.textContent;
    }  
    );
    
    Element.prototype.__defineSetter__( "text",  function(s){  
        this.textContent = s;
    }  
    );
    
    Element.prototype.__defineGetter__( "innerText",  function(){  
        return this.textContent;
    }  
    ); 
    
    Element.prototype.__defineSetter__( "innerText",  function(s){
        this.textContent = s;
    }  
    );
    
    HTMLElement.prototype.__defineGetter__("innerText", function(){
        return this.textContent;  
    });  

    HTMLElement.prototype.__defineSetter__("innerText", function(s){  
        this.textContent = s;  
    });
}  
</SCRIPT>
</HEAD>

<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<%--
<names><name type='system'>TEST</name><name type='system'>TEST2</name><name type='user' user='cvdl'>My model</name><name type='user' user='cvdl'>My model 2</name></names>
--%>
<XML id="oNames"><%=xmlNames%></XML>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading"><%=title %>&nbsp;:&nbsp;&nbsp;<%=termbaseName%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538><%=bundle.getString("helper_text_tb_input_models_main") %>
    </TD>
  </TR>
</TABLE>

<BR>

<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="2">
  <TR>
    <TD><SPAN CLASS="standardText"><B><%=bundle.getString("lb_input_models") %></B></SPAN></TD>
  </TR>
  <TR>
    <TD valign="top" WIDTH="400">

    <!-- Border table -->
    <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1" WIDTH="100%">
    <TR>
    <TD BGCOLOR="#0C1476" ALIGN="CENTER">

      <FORM NAME="systemForm">
      <!-- Data table -->
      <TABLE id="idSystemNames" BORDER="0" CELLSPACING="0" CELLPADDING="2">
	      <THEAD>
	        <TR>
	          <TD WIDTH="25" class="tableHeadingBasic">&nbsp;</TD>
	          <TD WIDTH="250" class="tableHeadingBasic"><%=bundle.getString("lb_name") %></TD>
	          <TD WIDTH="125" class="tableHeadingBasic"><%=bundle.getString("lb_is_default") %></TD>
	        </TR>
	     </THEAD>
	     <TBODY id="idTBodySystem"/>
     </TABLE>
      <!-- End Data table -->
      </FORM>
    </TD>
    </TR>
    </TABLE>
    <!-- End Border table -->
    </TD>
  </TR>
  <TR>
    <TD>
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_add")%>"
      ID="idAdd" onclick="newObject(true)" TYPE="BUTTON">
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_edit")%>"
      ID="idModify" onclick="modifyObject()" TYPE="BUTTON">
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_remove")%>"
      ID="idRemove" onclick="removeObject()" TYPE="BUTTON">
      &nbsp;&nbsp;
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_make_default")%>"
      ID="idMakeDefault" onclick="makeDefaultObject()" TYPE="BUTTON">
      &nbsp;&nbsp;
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_unset_default")%>"
      ID="idUnsetDefault" onclick="unsetDefaultObject()" TYPE="BUTTON">
    </TD>
  </TR>

  <TR><TD>&nbsp;</TD></TR>

  <TR style="display:none">
    <TD><SPAN CLASS="standardText"><B><%=bundle.getString("lb_termbase_user_input_mods") %></B></SPAN></TD>
  </TR>
  <TR style="display:none">
    <TD valign="top" WIDTH="450">

    <!-- Border table -->
    <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1" WIDTH="100%">
    <TR>
    <TD BGCOLOR="#0C1476" ALIGN="CENTER">

      <FORM NAME="userForm">
      <!-- Data table -->
      <TABLE id="idSystemNames" BORDER="0" CELLSPACING="0" CELLPADDING="2">
	<THEAD>
	  <TR>
	    <TD WIDTH="25" class="tableHeadingBasic">&nbsp;</TD>
	    <TD WIDTH="175" class="tableHeadingBasic"><%=bundle.getString("lb_user") %></TD>
	    <TD WIDTH="250" class="tableHeadingBasic"><%=bundle.getString("lb_name") %></TD>
	  </TR>
	</THEAD>
	<TBODY id="idTBodyUser"/>
      </TABLE>
      <!-- End Data table -->
      </FORM>
    </TD>
    </TR>
    </TABLE>
    <!-- End Border table -->
    </TD>
  </TR>
  <TR style="display:none">
    <TD>
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_add")%>"
      ID="idAdd" onclick="newObject(false)" TYPE="BUTTON">
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_edit")%>"
      ID="idModify" onclick="modifyObject()" TYPE="BUTTON">
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_remove")%>"
      ID="idRemove" onclick="removeObject()" TYPE="BUTTON">
    </TD>
  </TR>
  <TR style="display:none">
    <TD>&nbsp;</TD>
  </TR>
  <TR>
    <TD>
      <INPUT CLASS="standardText" VALUE="<%=bundle.getString("lb_previous")%>"
      ID="idDone" onclick="doDone()" TYPE="BUTTON">
    </TD>
  </TR>
  <TR><TD>&nbsp;</TD></TR>
</TABLE>

<form name="saveForm" action="<%=urlRequest%>">
<input type="hidden" name="<%=WebAppConstants.TERMBASE_ACTION%>" value="mlah!">
<input type="hidden" name="<%=WebAppConstants.TERMBASE_OBJECT_TYPE%>" value="">
<input type="hidden" name="<%=WebAppConstants.TERMBASE_OBJECT_USER%>" value="">
<input type="hidden" name="<%=WebAppConstants.TERMBASE_OBJECT_NAME%>" value="">
<input type="hidden" name="<%=WebAppConstants.TERMBASE_OBJECT_VALUE%>" value="">
</form>

</DIV>
</BODY>
</HTML>
