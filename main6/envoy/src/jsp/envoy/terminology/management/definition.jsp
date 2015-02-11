    <%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
            java.util.ResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.terminology.ITermbaseManager,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.terminology.TermbaseInfo"
        session="true"
    %>
    <jsp:useBean id="ok" scope="request"
     class="com.globalsight.everest.webapp.javabean.NavigationBean" />
    <jsp:useBean id="cancel" scope="request"
     class="com.globalsight.everest.webapp.javabean.NavigationBean" />
    <%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(
        WebAppConstants.SESSION_MANAGER);

    String xmlDefinition =
      (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_DEFINITION);
    sessionMgr.removeElement(WebAppConstants.TERMBASE_DEFINITION);

    String str_tbid =
      (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);

    String str_tb_name = "";
    ITermbaseManager s_manager = ServerProxy.getTermbaseManager();
    if (str_tbid != null && !str_tbid.equals(""))
    	str_tb_name = s_manager.getTermbaseName(Long.parseLong(str_tbid)); 
    String urlCancel = cancel.getPageURL();
    String urlOK     = ok.getPageURL();

    // Perform error handling, then clear out session attribute.
    sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);
    
    Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
    ArrayList names = s_manager.getTermbaseList(uiLocale);
    
    List tbNameList = new ArrayList();
    if ( names != null && names.size() > 0 )
    {
    	for (int i=0; i<names.size(); i++)
    	{
	        TermbaseInfo tbInfo = (TermbaseInfo) names.get(i);
	        String tbName = tbInfo.getName();
	        tbNameList.add(tbName);
    	}

    }

    %>
    <HTML XMLNS:gs>
    <HEAD>
    <TITLE><%=bundle.getString("lb_manage_termbase_definition")%></TITLE>
    <STYLE>
    #idGeneral,
    #idFields    { margin-top: 5pt; }

    FORM         { display: inline; }

    TEXTAREA     { overflow: auto; }
    TD           { font: 9pt arial;}
    .header      { font: bold 9pt arial; color: black; }
.link        { color: blue; cursor: hand; margin-bottom: 2pt;
               text-decoration: underline;
             }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<script src="/globalsight/includes/xmlextras.js"></script>
<SCRIPT src="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT src="envoy/terminology/management/objects_js.jsp"></SCRIPT>
<SCRIPT src="envoy/terminology/management/definition_js.jsp"></SCRIPT>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<SCRIPT>
var needWarning = true;
var objectName = "Termbase";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_define")%>";

var tbid = "<%=str_tbid%>";
var isModify = false;

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doOK()
{  
    var result = buildDefinition();
     
    var isDuplicated = checkDuplicated();

    if ( isDuplicated==true )
    {
        var msg = "<%=bundle.getString("msg_terminology_name_exist")%>";
        alert(msg);
    }
    else
    {
        if (result.error == 0)
        {
            // alert(oDefinition.XMLDocument.xml);

            if (isModify)
            {
              try
              {
            	 sendTermbaseManagementRequest(
                          "<%=WebAppConstants.TERMBASE_ACTION_MODIFY%>", tbid,
                          getDomString(result.dom));
                 window.location.href = "<%=urlOK%>";
              }
              catch (error)
              {
                error.message = "<%=EditUtil.toJavascript(bundle.getString("lb_tb_modification_failed"))%>";
                showError(error);
              }
            }
            else
            {
              try
              {
            	  sendTermbaseManagementRequest(
                          "<%=WebAppConstants.TERMBASE_ACTION_NEW%>", -1, 
                          getDomString(result.dom));
                  window.location.href = "<%=urlOK%>";
              }
              catch (error)
              {
                error.message = "<%=EditUtil.toJavascript(bundle.getString("lb_tb_creation_failed"))%>";
                showError(error);
              }
            }
        }
        else
        {
            if (result.element != null)
            {
                result.element.focus();
            }

            alert(result.message);
        }
    }

}

//check the added or modified tb name is exsited
function checkDuplicated()
{
	var result = false;
	var name = Trim(idName.value);
	var action = "<%=request.getParameter("action")%>";
	if(action=="modify")
	{
		<% for (int i = 0 ; i < tbNameList.size(); i++) { %>
		
		    if ( '<%=(String)tbNameList.get(i)%>'.toLowerCase()==name.toLowerCase() && '<%=str_tb_name%>'.toLowerCase()!=name.toLowerCase())
		    {
			    result = true;
		    }
		<%}%> 
	}
	else
	{
		<% for (int i = 0 ; i < tbNameList.size(); i++) { %>
		
		    if ( '<%=(String)tbNameList.get(i)%>'.toLowerCase()==name.toLowerCase() )
		    {
			    result = true;
		    }
		<%}%> 
	}	

	return result;
}

function Result(message, errorFlag, element, dom)
{
    this.message = message;
    this.error   = errorFlag;
    this.element = element;
    this.dom = dom;
}

function buildDefinition()
{
    var xmlStr = document.getElementById("ttt").value;
    var dom = $.parseXML(xmlStr);    
    var result = new Result("", 0, null,null);

    var node;
    var name = Trim(idName.value);
    var description = idDescription.value;

    if (name == "")
    {
        return new Result(
          "<%=EditUtil.toJavascript(bundle.getString("lb_tb_enter_name"))%>",
          1, idName, null);
    }
    
    if (name.length > 100)
    {
        return new Result(
          "<%=EditUtil.toJavascript(bundle.getString("lb_tb_invalid_name_length"))%>",
          1, idName, null);
    }

    // Can't send % savely to server through url.
    // Can't browse if name contains "-".
    
    if (hasSpecialChars(idName.value) || name.indexOf("-") >= 0)
    {
        return new Result(
          "<%=EditUtil.toJavascript(bundle.getString("lb_tb_invalid_name"))%>",
          1, idName, null);
    }

    
    node = $(dom).find("definition").children("name");
    node.text(name);
    node = $(dom).find("definition description");
    node.text(description);
    
    node = $(dom).find("definition languages");
    var len = node.children().length;
    while(len){
    		node.children().eq(0).remove();
    		len = node.children().length;
    }
    
    node = $(dom).find("definition languages");
    var elem,name,locale,hasterms;
    for (i = 0; i < aLanguages.length; ++i)
    {
        var oLanguage = aLanguages[i];

        elem = dom.createElement("language");
        node.append(elem);
        var len = $(dom).find("definition languages language").length;
        elem = $(dom).find("definition languages language").eq(len-1);
        
        name = dom.createElement("name");
        locale = dom.createElement("locale");
        hasterms = dom.createElement("hasterms");
        elem.append(name);
        elem.append(locale);
        elem.append(hasterms);
        
        name = $(elem).find("name");
        locale = $(elem).find("locale");
        hasterms = $(elem).find("hasterms");
        
        name.text(oLanguage.name);
        locale.text(oLanguage.locale);
        hasterms.text( (oLanguage.hasterms == true) ? "true" : "false");
    }
    
    node = $(dom).find("definition fields");
    var len = node.children().length;
    while(len){
    		node.children().eq(0).remove();
    		len = node.children().length;
    }

    for (i = 0; i < aFields.length; ++i)
    {
        var oField = aFields[i];

        var elem = dom.createElement("field");
        node.append(elem);
        var len = $(dom).find("definition fields field").length;
        elem = $(dom).find("definition fields field").eq(len-1);
        
        name = dom.createElement("name");
        type = dom.createElement("type");
        system = dom.createElement("system");
        values = dom.createElement("values");
        elem.append(name);
        elem.append(type);
        elem.append(system);
        elem.append(values);
        
        name = $(elem).find("name");
        type = $(elem).find("type");
        system = $(elem).find("system");
        values = $(elem).find("values");
        
        name.text(oField.name);
        type.text(oField.type);
        system.text((oField.system == true) ? "true" : "false");
        values.text(oField.values);
    }
    
    node = $(dom).find("definition indexes");
    
    if(node.length > 0) {
    	var len = node.children().length;
        while (len)
        {
        	node.children().eq(0).remove();
    		len = node.children().length;
        }
    } else {
        node = dom.createElement("indexes");
        var root = $(dom).find("definition");
        root.append(node);
        node = $(dom).find("definition indexes");
    }
    var elem0 = dom.createElement("index");
    node.append(elem0);
    
    elem0 = $(dom).find("definition indexes index"); 
    var langname0 = dom.createElement("languagename");
    var locale0 = dom.createElement("locale");
    var langtype0 = dom.createElement("type");
    
    elem0.append(langname0);
    elem0.append(locale0);
    elem0.append(langtype0);
    
    langname0 = $(elem0).find("languagename");
    locale0 = $(elem0).find("locale");
    langtype0 = $(elem0).find("type");
    
    if(window.navigator.userAgent.indexOf("MSIE")>0){
    	
	    langtype0.text("fulltext");
    }else{
    	
	    locale0.text("en");
	    langtype0.text("fulltext");
    }
    
    for (i = 0; i < aLanguages.length; ++i)
    {
        var oLanguage = aLanguages[i];
        
        for (j = 0; j < 2; ++j) {
            var elem = dom.createElement("index");
            node.append(elem);
            var len = $(dom).find("definition indexes index").length
            elem = $(dom).find("definition indexes index").eq(len-1); 
            
            var langname = dom.createElement("languagename");
            var locale = dom.createElement("locale");
            var langtype = dom.createElement("type");
            
            elem.append(langname);
            elem.append(locale);
            elem.append(langtype);
            
            langname = $(elem).find("languagename");
            locale = $(elem).find("locale");
            langtype = $(elem).find("type");
            
            var lan_type ="";
            
            if(j == 0){
                lan_type = "fuzzy";
            }
            else if(j == 1) {
                lan_type = "fulltext";
            }
            
            langname.text(oLanguage.name);
            locale.text(oLanguage.locale);
            langtype.text(lan_type);
        }
    }
    
    result.dom = dom;

    return result;
}

function parseDefinition()
{
    var dom;
    var xmlStr = document.getElementById("ttt").value;
	dom = $.parseXML(xmlStr);
    
    var nodes, node;
    
	idName.value = $(dom).find("name").first().text();
	
	idDescription.value = $(dom).find("description").text();
    nodes = $(dom).find("definition languages language");
	$(nodes).each(function(){
		var name = $(this).find("name").text();
		var locale = $(this).find("locale").text();
		var hasterms = $(this).find("hasterms").text();
        hasterms = (hasterms == "true" ? true : false);
        var exists = true;
        aLanguages.push(new Language(name, locale, hasterms, exists));
	})
    showLanguages();

    nodes = $(dom).find("definition fields field");
	$(nodes).each(function(){
		var name = $(this).find("name").text();
		var type = $(this).find("type").text();
		var system = ($(this).find("system").text() == "true" ? true : false);
		var values = $(this).find("values").text();

        var format = getFieldFormatByType(type);
        aFields.push(new Field(name, type, format, system, values));
	})
    showFields();
}

function doOnLoad()
{
   // This loads the guides in guides.js and the 
   loadGuides();

   parseDefinition();
   
    var dom;
    var xmlStr = document.getElementById("ttt").value;
    dom = $.parseXML(xmlStr);
    
    var nameNode = $(dom).find("definition").children("name");

    var toModify = false; 
	toModify =  (nameNode != null && $(nameNode).text() != "");
    if (toModify)
    {
      isModify = true;
      idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_modify"))%>" + " " + $(nameNode).text();
      idStep1.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_modify_name_desc"))%>";
      idStep2.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_modify_lang"))%>";
      idStep3.innerHTML = "Modify Fields";
      idStep4.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_save_modified"))%>";
      //idName.select();
      idName.focus();
    }
    else
    {
      idHeading.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_define_new"))%>";
      idStep1.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_enter_name_desc"))%>";
      idStep2.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_define_langs"))%>";
      idStep3.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("lb_define_fields"))%>";
      idStep4.innerHTML = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_save_new"))%>";
      idName.select();
      idName.focus();
    }
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad();" LEFTMARGIN="0" RIGHTMARGIN="0" 
      TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE="POSITION: absolute; Z-INDEX: 0; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<P CLASS="mainHeading" id="idHeading"></P>

<%-- An empty definition for your perusal.
<XML id="oDefinition">
<definition><name></name><description></description><languages></languages><fields></fields></definition>
</XML>
--%>

<XML id="oDefinition" style="display:none;"><%=xmlDefinition%></XML>

<TABLE  CELLSPACING="0" CELLPADDING="0" BORDER=0>
<!-- Step 1 -->
<TR>
  <TD VALIGN="MIDDLE">
    <IMG SRC="/globalsight/images/1.gif" ALT="Step 1" HEIGHT=23 WIDTH=23>
    &nbsp;&nbsp;&nbsp;
</TD>
<TD VALIGN="MIDDLE"><SPAN ID="idStep1" CLASS="standardTextBold"></SPAN></TD>
</TR>
<TR>
<TD>&nbsp;</TD>
<TD>

<TABLE id="idGeneral">
  <TR VALIGN="TOP">
    <TD>
      <LABEL FOR="idName" ACCESSKEY="N"><%=bundle.getString("lb_tb_name")%></LABEL>
    </TD>
    <TD>
      <INPUT TYPE="text" ID="idName" SIZE="40" VALUE="" MAXLENGTH="100">
    </TD>
  </TR>
  <TR VALIGN="TOP">
    <TD >
      <LABEL FOR="idDescription" ACCESSKEY="D"><%=bundle.getString("lb_tb_description")%></LABEL>
    </TD>
    <TD>
      <TEXTAREA ID="idDescription" COLS="34" ROWS="3"></TEXTAREA>
    </TD>
  </TR>
</TABLE>

</TD>
</TR>
<!-- End Step 1 -->

<!-- Step 2 -->
<!-- Spacer row -->
<TR>
<TD COLSPAN=2 HEIGHT=15>&nbsp;</TD>
</TR>

<TR>
<TD VALIGN="MIDDLE">
  <IMG SRC="/globalsight/images/2.gif" ALT="Step 2" HEIGHT=23 WIDTH=23>
  &nbsp;&nbsp;&nbsp;
</TD>
<TD VALIGN="MIDDLE"><SPAN ID="idStep2" CLASS="standardTextBold"></SPAN></TD>
</TR>
<TR>
<TD>&nbsp;</TD>
<TD>
<FORM NAME="languagesForm">
<TABLE id="idLanguages" BORDER="0" CELLSPACING="0" CELLPADDING="2"
  style="border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;">
  <COL width="25px"/>
  <COL width="150px"/>
  <COL width="150px"/>
  <THEAD>
    <TR>
      <TD class="header1" valign="middle">&nbsp;</TD>
      <TD class="header1" valign="middle"><%=bundle.getString("lb_tb_language")%></TD>
      <TD class="header1" valign="middle"><%=bundle.getString("lb_tb_sortorder")%></TD>
    </TR>
  </THEAD>
  <TBODY id="idLanguagesBody"></TBODY>
</TABLE>
  
<BR> 
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" onclick="removeLanguage()">
<%--
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_modify")%>" onclick="modifyLanguage()">
--%>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_add")%>" NAME="idAdd" onclick="newLanguage()">  
</FORM>
</TD>
</TR> 
<!-- End Step 2 -->

<!-- Step 3 -->
<!-- Spacer row -->
<TR>
<TD COLSPAN=2 HEIGHT=15>&nbsp;</TD>
</TR>

<TR>
  <TD VALIGN="MIDDLE">
    <IMG SRC="/globalsight/images/3.gif" ALT="Step 3" HEIGHT=23 WIDTH=23>
    &nbsp;&nbsp;&nbsp;
  </TD>
  <TD VALIGN="MIDDLE"><SPAN ID="idStep3" CLASS="standardTextBold"></SPAN></TD>
</TR>
<TR>
<TD>&nbsp;</TD>
<TD>
<FORM NAME="fieldsForm">
<TABLE id="idFields" BORDER="0" CELLSPACING="0" CELLPADDING="2"
  style="border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;">
  <COL width="25px" valign="top"/>
  <COL width="150px" valign="top"/>
  <COL width="150px" valign="top"/>
  <COL width="150px" valign="top"/>
  <THEAD>
    <TR>
      <TD class="header1" valign="middle">&nbsp;</TD>
      <TD class="header1" valign="middle"><%=bundle.getString("lb_name") %></TD>
      <TD class="header1" valign="middle"><%=bundle.getString("lb_type") %></TD>
      <TD class="header1" valign="middle"><%=bundle.getString("lb_values") %></TD>
    </TR>
  </THEAD>
  <TBODY id="idFieldsBody"></TBODY>
</TABLE>
  
<BR>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
 onclick="removeField()">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_modify")%>"
 onclick="modifyField()">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_add")%>" NAME="idAdd"
 onclick="newField()">  
</FORM>
</TD>
</TR> 
<!-- End Step 3 -->

<!-- Step 4 -->
<!-- Spacer row -->
<TR>
<TD COLSPAN=2 HEIGHT=15>&nbsp;</TD>
</TR>

<TR>
  <TD VALIGN="MIDDLE">
    <IMG SRC="/globalsight/images/4.gif" ALT="Step 4" HEIGHT=23 WIDTH=23>
    &nbsp;&nbsp;&nbsp;
  </TD>
  <TD VALIGN="MIDDLE"><SPAN ID="idStep4" CLASS="standardTextBold"></SPAN></TD>
</TR>
<TR>
<TD>&nbsp;</TD>
<TD>
<DIV CLASS="standardText"><%=bundle.getString("lb_tb_click_to_save")%></DIV>
<BR>
  
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
  ID="Cancel" TABINDEX="0" onclick="doCancel();">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>"
  ID="OK" TABINDEX="0" onclick="doOK();">
<div style="display:none">
<TEXTAREA ID="ttt" COLS="34" ROWS="3"><%=xmlDefinition%></TEXTAREA>
</div>
</TD>
</TR>
<!-- End Step 4 -->
<!-- Spacer row -->
<TR>
<TD COLSPAN=2 HEIGHT=20>&nbsp;</TD>
</TR>
</TABLE>

</DIV>
     
</BODY>
</HTML>
