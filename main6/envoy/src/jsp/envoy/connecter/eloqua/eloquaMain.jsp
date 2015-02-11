<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.servlet.util.SessionManager,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.cxe.entity.eloqua.EloquaConnector,
            com.globalsight.everest.util.comparator.EloquaConnectorComparator,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.company.CompanyWrapper,java.util.*"
    session="true"
%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>

<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="connect" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER); 

  String newURL = new1.getPageURL() + "&action=new";
  String editURL = edit.getPageURL() + "&action=edit";
  String remURL = remove.getPageURL() + "&action=remove";
  String connectURL = connect.getPageURL() + "&action=connect";
  String testURL = self.getPageURL() + "&action=test";
  String filterURL = self.getPageURL() + "&action=filter";
  String title= bundle.getString("lb_eloqua");
  String helperText = bundle.getString("helper_text_eloqua_connector");
  String confirmRemove = bundle.getString("msg_remove_eloqua_connector");
  String invalid = (String)request.getAttribute("invalid"); 
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
  
  String nameFilter = (String) request.getAttribute("nameFilter");
  String companyFilter = (String) request.getAttribute("companyFilter");
  String companyNameFilter = (String) request.getAttribute("companyNameFilter");
  String urlFilter = (String) request.getAttribute("urlFilter");
  String descriptionFilter = (String) request.getAttribute("descriptionFilter");
  
  nameFilter = nameFilter == null ? "" : nameFilter;
  companyFilter = companyFilter == null ? "" : companyFilter;
  companyNameFilter = companyNameFilter == null ? "" : companyNameFilter;
  urlFilter = urlFilter == null ? "" : urlFilter;
  descriptionFilter = descriptionFilter == null ? "" : descriptionFilter;
%>
<HTML>
<!-- eloquaMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jQuery.md5.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var guideNode = "createEloquaJob";
var needWarning = false;
var objectName = "";
var helpFile = "<%=bundle.getString("help_eloqua_connector_main")%>";

function testConntect()
{
	$("#idDiv").mask("<%=bundle.getString("msg_eloqua_wait_connect")%>");   
    var value = $("input:checked[name='selectEloquaConnectorIds']")[0].value;
	$.post("<%=testURL%>", 
            {"id":value}, 
            function(returnData){       	 
            	     	
                if (returnData.canUse)
                {
                	eloquaForm.action = "<%=connectURL%>" + "&id=" + value;
                	eloquaForm.submit();
                }
                else
                {
                	$("#idDiv").unmask("<%=bundle.getString("msg_eloqua_wait_connect")%>");           	
                	alert(returnData.error);               
                };               
    }, "json");
}

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        eloquaForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(eloquaForm.selectEloquaConnectorIds);

        if (button == "Edit")
        {
            eloquaForm.action = "<%=editURL%>" + "&id=" + value;
        }
        if (button == "Connect")
        {
        	testConntect();
            return;
        }
        else if (button == "Remove")
        {
        	var referencedNames = "";
            var ids = document.getElementsByName("selectEloquaConnectorIds");
            isOk = confirm('<%=confirmRemove%>');        
            eloquaForm.action = "<%=remURL%>";
        }
    }

    if (isOk)
    {
        eloquaForm.submit();
    }
}

function enableButtons()
{
    if (eloquaForm.editBtn)
        eloquaForm.editBtn.disabled = false;
    if (eloquaForm.dupBtn)
        eloquaForm.dupBtn.disabled = false;
    if (eloquaForm.remBtn)
        eloquaForm.remBtn.disabled = false;    
}


function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = eloquaForm.selectEloquaConnectorIds;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                if (checkbox.checked) 
                {
                    selectedIndex.push(i);
                }
            }
        } 
        else 
        {
            if (boxes.checked) 
            {
                selectedIndex.push(0);
            }
        }
    }

    if (selectedIndex.length != 1)
    {
        eloquaForm.editBtn.disabled = true;
        eloquaForm.connectBtn.disabled = true;        
    }
    else
    {
        eloquaForm.editBtn.disabled = false;
        eloquaForm.connectBtn.disabled = false;
    }

    if (selectedIndex.length > 0)
    {
        eloquaForm.remBtn.disabled = false;
    }
    else
    {
        eloquaForm.remBtn.disabled = true;
    }
}

function handleSelectAll() {
	if (eloquaForm && eloquaForm.selectAll) {
		if (eloquaForm.selectAll.checked) {
			checkAllWithName('eloquaForm', 'selectEloquaConnectorIds'); 
			setButtonState();
	    }
	    else {
			clearAll('eloquaForm'); 
			setButtonState();
	    }
	}
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	eloquaForm.action = "<%=filterURL%>";
        eloquaForm.submit();
    }
}
</SCRIPT>
<style type="text/css">
@import url(/globalsight/includes/attribute.css);
</style>
</HEAD>
<BODY ID="idBody" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
      onload="loadGuides()">

<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<span class=errorMsg><%
    if (invalid != null && invalid.length() > 0) out.print(invalid);
%></span>


<form name="eloquaForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" >
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="eloquaConnectList" key="eloquaConnectKey"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="eloquaConnectList" id="eloquaConnector"
       key="eloquaConnectKey"
       dataClass="com.globalsight.cxe.entity.eloqua.EloquaConnector"
       pageUrl="self"
       hasFilter="true"
       emptyTableMsg="msg_eloqua_connector_none">
        <amb:column label="checkbox" width="2%">
       
          <input type="checkbox" name="selectEloquaConnectorIds" value="<%=eloquaConnector.getId()%>" 
              displayName="<%=eloquaConnector.getName()%>" 
              onclick="setButtonState()" > 
        
        </amb:column>
        
        <amb:column label="lb_name" sortBy="<%=EloquaConnectorComparator.NAME%>" filter="nameFilter" filterValue="<%=nameFilter%>"  width="24%">
            <%String url = editURL + "&id=" + eloquaConnector.getId();%>          
	        <A name='nameLink' class='standardHREF' href="<%=url%>">
	        <%=eloquaConnector.getName()%>
	        </A> 
	       &nbsp;&nbsp;
        </amb:column>
        <amb:column label="lb_description" sortBy="<%=EloquaConnectorComparator.DESC%>" filter="descriptionFilter" filterValue="<%=descriptionFilter%>"
                   width="24%">
            <%=eloquaConnector.getDescription() == null ? "" : eloquaConnector.getDescription()%>
        </amb:column>
        <amb:column label="lb_company" sortBy="<%=EloquaConnectorComparator.COMPANY%>" filter="companyFilter" filterValue="<%=companyFilter%>" width="24%" >            
            <%=eloquaConnector.getCompany()%>     
            &nbsp;&nbsp;               
        </amb:column>
       
        <amb:column label="lb_url" sortBy="<%=EloquaConnectorComparator.URL%>" filter="urlFilter" filterValue="<%=urlFilter%>">           
            <%=eloquaConnector.getUrl()%>                    
        </amb:column>
        
        <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=EloquaConnectorComparator.GS_COMPANY_NAME%>"  filter="companyNameFilter" filterValue="<%=companyNameFilter%>">
                    <%=eloquaConnector.getGsCompany()%>
                </amb:column>
        <% } %>
      </amb:table>
    </td>
  </tr>
  
  <tr style="padding-top: 5px;">
   <td><amb:tableNav  bean="eloquaConnectList" key="eloquaConnectKey"
       pageUrl="self" scope="10,20,50,All" showTotalCount="false"/></td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="left">
    
    <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_connect")%>"
      name="connectBtn" disabled onclick="testConntect();">
      
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
      name="remBtn" disabled onclick="submitForm('Remove');">
    
   
      <INPUT style="display: none" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
      name="editBtn" disabled onclick="submitForm('Edit');">
   
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      onclick="submitForm('New');">
    
      
    
    </td>
  </tr>
</table>
</form>
</div>
</DIV>
</BODY>
</HTML>