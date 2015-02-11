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
  String newURL = new1.getPageURL() + "&action=" + XmlRuleConstant.NEW;
  String editURL = edit.getPageURL() + "&action=" + XmlRuleConstant.EDIT;
  String dupURL = dup.getPageURL() + "&action=" + XmlRuleConstant.DUPLICATE;
  String remURL = rem.getPageURL() + "&action=" + XmlRuleConstant.REMOVE;
  String title= bundle.getString("lb_xml_rules");
  String helperText = bundle.getString("helper_text_xml_rules");
  String confirmRemove = bundle.getString("msg_remove_xml_rule");
  
  String invalid = (String)request.getAttribute("invalid");
  
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<!-- xmlrulefile/xmlrulfileMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
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
        value = getRadioValue(xmlForm.radioBtn);

        if (button == "Edit")
        {
            xmlForm.action = "<%=editURL%>";
        }
        else if (button == "Dup")
        {
            xmlForm.action = "<%=dupURL%>";
        }
        else if (button == "Remove")
        {
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

function enableButtons()
{
    if (xmlForm.editBtn)
        xmlForm.editBtn.disabled = false;
    if (xmlForm.dupBtn)
        xmlForm.dupBtn.disabled = false;
    if (xmlForm.remBtn)
        xmlForm.remBtn.disabled = false;    
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
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
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
       emptyTableMsg="msg_no_xmlrulefile" >
        <amb:column label="">
          <input type="radio" name="radioBtn" value="<%=xmlRule.getId()%>"
           onclick="enableButtons()">
        </amb:column>
        <amb:column label="lb_name" sortBy="<%=XmlRuleFileComparator.NAME%>"
         width="150px">
         <%= xmlRule.getName() %>
        </amb:column>
        <amb:column label="lb_description" sortBy="<%=XmlRuleFileComparator.DESC%>"
         width="400px">
          <% out.print(xmlRule.getDescription() == null ? "" :
             xmlRule.getDescription()); %>
        </amb:column>
        <% if (isSuperAdmin) { %>
        <amb:column label="lb_company_name" sortBy="<%=XmlRuleFileComparator.ASC_COMPANY%>">
            <%=CompanyWrapper.getCompanyNameById(xmlRule.getCompanyId())%>
        </amb:column>
        <% } %>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.XMLRULE_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
      name="remBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.XMLRULE_DUP%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_duplicate")%>"
      name="dupBtn" disabled onclick="submitForm('Dup');">
    </amb:permission>
    <amb:permission name="<%=Permission.XMLRULE_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
      name="editBtn" disabled onclick="submitForm('Edit');">
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

