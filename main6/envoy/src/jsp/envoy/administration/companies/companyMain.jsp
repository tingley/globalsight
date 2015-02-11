<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.company.Company,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,  
      com.globalsight.everest.util.comparator.CompanyComparator, 
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="companies" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 
    String confirmRemove = bundle.getString("msg_confirm_company_removal");
    String newURL = new1.getPageURL() + "&action=" + CompanyConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + CompanyConstants.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + CompanyConstants.REMOVE;
    String title = bundle.getString("lb_companies");
    String helperText= bundle.getString("helper_text_companies");

    String deps = (String)sessionMgr.getAttribute(CompanyConstants.DEPENDENCIES);
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "companies";
var helpFile = "<%=bundle.getString("help_companies_main_screen")%>";

function submitForm(button)
{
    if (button == "New")
    {
        companyForm.action = "<%=newURL%>";
    }
    else 
    {
        value = getRadioValue(companyForm.radioBtn);
        if (button == "Edit")
        {
            companyForm.action = "<%=modifyURL%>" + "&name=" + value;
        }
        else if (button == "Remove")
        {
            if (!confirm('<%=confirmRemove%>')) return false;
            companyForm.action = "<%=removeURL%>" + "&name=" + value;
        }
    }
    companyForm.submit();
    return;
}

function enableButtons()
{
    if (companyForm.removeBtn)
        companyForm.removeBtn.disabled = false;
    if (companyForm.editBtn)
        companyForm.editBtn.disabled = false;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement(CompanyConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>

<form name="companyForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="companies" key="<%=CompanyConstants.COMPANY_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="companies" id="company"
       key="<%=CompanyConstants.COMPANY_KEY%>"
       dataClass="com.globalsight.everest.company.Company" pageUrl="self"
       emptyTableMsg="msg_no_companies" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=company.getName()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=CompanyComparator.NAME%>"
       width="150px">
      <%= company.getName() %>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=CompanyComparator.DESC%>"
       width="400px">
      <% out.print(company.getDescription() == null ?
       "" : company.getDescription()); %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <!-- Currently remove company operation is not supported
    <amb:permission name="<%=Permission.COMPANY_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
       name="removeBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    -->
    <amb:permission name="<%=Permission.COMPANY_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.COMPANY_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>
</HTML>

