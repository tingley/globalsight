<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.webapp.pagehandler.PageHandler, 
            com.globalsight.everest.webapp.pagehandler.administration.config.xmlrulefile.XmlRuleConstant, 
            com.globalsight.everest.util.comparator.XmlRuleFileComparator, 
            com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.*"
    session="true"
%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>

<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="dup" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rem" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<jsp:useBean id="xmlRules" scope="request"
 class="java.util.ArrayList" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String selfUrl = self.getPageURL();
  String newURL = new1.getPageURL() + "&action=" + XmlRuleConstant.NEW;
  String editURL = edit.getPageURL() + "&action=" + XmlRuleConstant.EDIT;
  String dupURL = dup.getPageURL() + "&action=" + XmlRuleConstant.DUPLICATE;
  String remURL = rem.getPageURL() + "&action=" + XmlRuleConstant.REMOVE;
  String title= bundle.getString("lb_xml_rules");
  String helperText = bundle.getString("helper_text_xml_rules");
  String confirmRemove = bundle.getString("msg_remove_xml_rule"); 
  SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
  String xmlruleName = (String) sessionMgr.getAttribute("xmlruleName");
  xmlruleName = xmlruleName == null ? "" : xmlruleName;
  String xmlruleCompName = (String) sessionMgr.getAttribute("xmlruleCompName");
  xmlruleCompName = xmlruleCompName == null ? "" : xmlruleCompName;
  String invalid = (String)request.getAttribute("invalid");
  
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<!-- xmlrulefile/xmlrulfileMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "xmlRules";
var helpFile = "<%=bundle.getString("help_xml_rules_main_screen")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        xmlForm.action = "<%=newURL%>";
    }
    else
    {
        var  ch=document.getElementsByName("radioBtn");
        for(i=0;i<ch.length;i++){
        	if(ch[i].checked==true){
        		break;
        	}
        }
        value=ch[i].value;
        if (button == "Dup")
        {
            xmlForm.action = "<%=dupURL%>";
        }
        else if (button == "Remove")
        {
		    var rv="";
		    $(":checkbox:checked").each(
		        function(i){
		        	rv+=$(this).val()+" ";
		        }		
		    )
		    $(":checkbox:checked").each(
		        function(i){
		        	$(this).val(rv);
		        }		
		    )
        	if (!confirm('<%=confirmRemove%>'))
        	{
        		isOk = false;
        	}
            xmlForm.action = "<%=remURL%>";
        }
    }

    if (isOk)
    {
    	xmlForm.submit();
    }
}


function handleSelectAll() {
	var ch = $("#selectAll").attr("checked");
	if (ch == "checked") {
		$("[name='radioBtn']").attr("checked", true);
	} else {
		$("[name='radioBtn']").attr("checked", false);
	}
	buttonManagement();
}

function buttonManagement()
{
    var count = $("input[name='radioBtn']:checked").length;
    if (count==1)
    {
        $("#removeBtn").attr("disabled", false);  
        $("#dupBtn").attr("disabled",false);
        $("#editBtn").attr("disabled",false);
    }
    else 
    {
        $("#removeBtn").attr("disabled", false);  
        $("#dupBtn").attr("disabled",true);
        $("#editBtn").attr("disabled",true);
    }
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	xmlForm.action = "<%=selfUrl%>";
    	xmlForm.submit();
    }
}

function modifyuser(name){
	
	var url = "<%=editURL%>&radioBtn=" + name;
	xmlForm.action = url;

	xmlForm.submit();
	
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
      onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<span class=errorMsg><% if (invalid != null && invalid.length() > 0) out.print(invalid); %></span>
<form name="xmlForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width=100%>
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="xmlRules" key="<%=XmlRuleConstant.XMLRULE_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="xmlRules" id="xmlRule"
       key="<%=XmlRuleConstant.XMLRULE_KEY%>"
       dataClass="com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl"
       pageUrl="self"
       emptyTableMsg="msg_no_xmlrulefile"  hasFilter="true">
        <amb:column label="checkbox"  width="2%">
          <input type="checkbox" name="radioBtn" value="<%=xmlRule.getId()%>"
           onclick="buttonManagement()" >
        </amb:column>
        <amb:column label="lb_name" sortBy="<%=XmlRuleFileComparator.NAME%>"
        filter="xmlruleName" filterValue="<%=xmlruleName%>"     width="22%">
        <amb:permission name="<%=Permission.XMLRULE_EDIT%>" > <a href='javascript:void(0)' title='Edit xmlRule' onclick="modifyuser('<%=xmlRule.getId()%>')"> </amb:permission>
               <%= xmlRule.getName() %>
        <amb:permission name="<%=Permission.XMLRULE_EDIT%>" > </a> </amb:permission> 
        </amb:column>
        <amb:column label="lb_description" sortBy="<%=XmlRuleFileComparator.DESC%>"
         width="22%">
          <% out.print(xmlRule.getDescription() == null ? "" :
             xmlRule.getDescription()); %>
        </amb:column>
        <amb:column label="" sortBy=""  width="10%">
         &nbsp;
        </amb:column>
        <% if (isSuperAdmin) { %>
        <amb:column label="lb_company_name" sortBy="<%=XmlRuleFileComparator.ASC_COMPANY%>"
        filter="xmlruleCompName" filterValue="<%=xmlruleCompName%>">
            <%=CompanyWrapper.getCompanyNameById(xmlRule.getCompanyId())%>
        </amb:column>
        <% } %>
      </amb:table>
    </td>
  </tr>
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="xmlRules" key="<%=XmlRuleConstant.XMLRULE_KEY%>"
       pageUrl="self"  scope="10,20,50,All"  showTotalCount="false"/>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="left">
    <amb:permission name="<%=Permission.XMLRULE_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
      name="removeBtn"  id="removeBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.XMLRULE_DUP%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_duplicate")%>"
      name="dupBtn" id="dupBtn" disabled onclick="submitForm('Dup');">
    </amb:permission>
    <amb:permission name="<%=Permission.XMLRULE_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</BODY>
</HTML>

