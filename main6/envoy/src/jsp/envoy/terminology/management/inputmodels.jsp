<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
	com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.terminology.IUserdataManager,
        com.globalsight.terminology.java.InputModel,
        com.globalsight.util.edit.EditUtil,
        java.util.*,
	      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_request" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="inputmodelAction" 
    class="com.globalsight.everest.webapp.javabean.NavigationBean" 
    scope="request"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String userId = PageHandler.getUser(session).getUserId();
String jsmsg_select = bundle.getString("jsmsg_please_select_a_row");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);

/*
String xmlNames =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_OBJECT_NAMELIST);
xmlNames = xmlNames.replaceAll("\n","");
*/
List list = (List)sessionMgr.getAttribute(WebAppConstants.TERMBASE_OBJECT_NAMELIST);
sessionMgr.removeElement(WebAppConstants.TERMBASE_OBJECT_NAMELIST);

String urlDone = done.getPageURL();
String urlRequest = _request.getPageURL();
String urlEditor = editor.getPageURL();

// Perform error handling, then clear out session attribute.
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);

String title = bundle.getString("lb_termbase_manage_input_mods");
String g_checkmark = "<IMG SRC=\"/globalsight/images/checkmark.gif\" " +
  "HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3>" +
  "<SPAN style=\"visibility:hidden\">1</SPAN>";

String g_noCheckmark = "<SPAN style=\"visibility:hidden\">0</SPAN>";
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
<SCRIPT LANGUAGE="JavaScript" SRC="envoy/terminology/management/inputmodel.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/Ajax.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_terminology_input_models")%>";

var ControllerURL ="<%=inputmodelAction.getPageURL()%>";
var loadAction ="<%=WebAppConstants.TERMBASE_ACTION_LOAD_OBJECT%>";
var createAction ="<%=WebAppConstants.TERMBASE_ACTION_CREATE_OBJECT%>";
var modifyModelAction = "<%=WebAppConstants.TERMBASE_ACTION_MODIFY_OBJECT%>";
var removeAction = "<%=WebAppConstants.TERMBASE_ACTION_REMOVE_OBJECT%>";
var makeDefaultAction = "<%=WebAppConstants.TERMBASE_ACTION_MAKE_DEFAULT_OBJECT%>";
var unsetDefaultAction = "<%=WebAppConstants.TERMBASE_ACTION_UNSET_DEFAULT_OBJECT%>";
var g_objectType = "<%=IUserdataManager.TYPE_INPUTMODEL%>";
var g_editorUrl  = "<%=urlEditor%>";
var userId = "<%=userId%>";
var jsmsg_select = "<%=jsmsg_select%>"; 
var jsmsg_object_exists = "<%=bundle.getString("jsmsg_object_exists")%>"; 
var jsmsg_remove_object_confirm = "<%=bundle.getString("jsmsg_remove_object_confirm")%>"; 



function doDone()
{
    window.location.href = "<%=urlDone%>";
}

function doLoad() {
    // This loads the guides in guides.js and the
  loadGuides();
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
    <TD valign="top" WIDTH="500">

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
	          <TD WIDTH="350" class="tableHeadingBasic"><%=bundle.getString("lb_name") %></TD>
	          <TD WIDTH="125" class="tableHeadingBasic"><%=bundle.getString("lb_is_default") %></TD>
	        </TR>
	     </THEAD>
	     <TBODY id="idTBodySystem">
	        <%
	            String isHasSetDefault = "false";
	            for(int i = 0; i < list.size(); i++) {
	                InputModel model = (InputModel)list.get(i);
	                String bg = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
	                String checkmark;
	                if (model.getIsDefault().equals("Y"))
                  {
                      checkmark = g_checkmark;
                      isHasSetDefault = "true";
                  }
                  else
                  {
                      checkmark = g_noCheckmark ;
                  }
	        %>
	        <TR bgColor="<%=bg%>">
	          <TD WIDTH="25" ><INPUT TYPE=RADIO NAME=checkbox VALUE="<%=model.getId()%>" onclick="setDefaultButton('<%=model.getId()%>')"></TD>
	          <TD WIDTH="250" ><%=model.getName()%></TD>
	          <TD WIDTH="125" ><%=checkmark%>
	            <input type="hidden" id="checkmark<%=model.getId()%>" value="<%=model.getIsDefault()%>">
	          </TD>
	        </TR>
	        <%
	            }
	        %>
	        <input type="hidden" id="isHasSetDefault" value="<%=isHasSetDefault%>">
	     </TBODY>
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
