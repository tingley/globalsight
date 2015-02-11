<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.cvsconfig.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,  
      com.globalsight.everest.util.comparator.CVSModuleComparator, 
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
<jsp:useBean id="checkout" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String newURL = new1.getPageURL() + "&action=" + CVSConfigConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + CVSConfigConstants.UPDATE;
    String removeURL = remove.getPageURL() + "&action=" + CVSConfigConstants.REMOVE;
    String checkoutURL = checkout.getPageURL() + "&action=checkout";
    String title = bundle.getString("lb_cvsmodules");
    String helperText= bundle.getString("helper_text_cvsmodule");
    String cvsMsg = (String)sessionMgr.getAttribute("cvsmsg");
    sessionMgr.setAttribute("cvsmsg", null);
%>

<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "cvsserver";
var helpFile = "#";

function submitForm(button)
{
    if (button == "New")
    {
        cvsserverForm.action = "<%=newURL%>";
    }
    else 
    {
        value = getRadioValue(cvsserverForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
          
        if (button == "Edit")
        {
            cvsserverForm.action = "<%=modifyURL%>" + "&id=" + value;
        } else if (button=="Remove") {
            if (confirm("<%=bundle.getString("jsmsg_remove_module")%>"))
            	cvsserverForm.action = "<%=removeURL%>" + "&id=" + value;
           	else
               	return;
        } else {
            var msg = "";
            if (cvsserverForm.checkoutBtn.value=="<%=bundle.getString("lb_cvs_checkout")%>") {
	        	if (confirm("<%=bundle.getString("jsmsg_checkout_confirm")%>"))
	            	cvsserverForm.action = "<%=checkoutURL%>&id=" + value;
            	else
	            	return;
            } else {
            	cvsserverForm.action = "<%=checkoutURL%>&id=" + value;
            }
        }
    } 
    
    cvsserverForm.submit();
    return;
}

function enableButtons(lastCheckout)
{
    if (cvsserverForm.removeBtn)
    	cvsserverForm.removeBtn.disabled = false;
    if (cvsserverForm.editBtn)
    {	
        cvsserverForm.editBtn.disabled = false;
    }
    if (lastCheckout != "") 
    {
		if(cvsserverForm.checkoutBtn)
		{
			cvsserverForm.checkoutBtn.value = "<%=bundle.getString("lb_cvs_update") %>";
		}
		cvsserverForm.editBtn.disabled = true;
	}
	else
    {
		if(cvsserverForm.checkoutBtn)
		{
		cvsserverForm.checkoutBtn.value = "<%=bundle.getString("lb_cvs_checkout") %>";
		}
    }
	if (cvsserverForm.checkoutBtn)
		cvsserverForm.checkoutBtn.disabled = false;
	
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />

<form name="cvsserverForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="<%=CVSConfigConstants.CVS_MODULE_LIST%>" 
       key="<%=CVSConfigConstants.CVS_MODULE_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="<%=CVSConfigConstants.CVS_MODULE_LIST%>" id="module"
       key="<%=CVSConfigConstants.CVS_MODULE_KEY%>"
       dataClass="com.globalsight.everest.cvsconfig.CVSModule" pageUrl="self"
       emptyTableMsg="msg_no_cvsmodule" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=module.getId()%>"
       onclick="enableButtons('<%=module.getLastCheckout() %>')">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=CVSModuleComparator.NAME%>"
       width="150px">
      <%= module.getName() %>
      </amb:column>
      <amb:column label="lb_cvs_module_name" width="300px">
      <%
          String moduleNames = module.getModulename();
          String[] names = moduleNames.split(",");
          for (String n : names) {
        	  out.println(n + "<br>");
          }
      %>
      </amb:column>
      <amb:column label="lb_cvs_server" sortBy="<%=CVSModuleComparator.SERVER%>"
        width="100px">
      <% out.print(module.getServer().getName()); %>
      </amb:column>
      <amb:column label="lb_cvs_module_branch" sortBy="<%=CVSModuleComparator.BRANCH%>"
        width="200px">
      <% out.print(module.getBranch()); %>
      </amb:column>
      <amb:column label="lb_cvs_module_lastCheckout" sortBy="<%=CVSModuleComparator.LAST_CHECKOUT%>"
        width="200px">
      <%
      String tmp = module.getLastCheckout();
      if (tmp == null || tmp.equals("") || tmp.equals("null"))
    	  out.print("&nbsp;");
      else
    	  out.print(tmp);
       %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.CVS_MODULES_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_MODULES_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
       name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_MODULES_CHECKOUT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cvs_checkout") %>"
       name="checkoutBtn" onclick="submitForm('checkout');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_MODULES_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>

<script language="JavaScript">
if ("<%=cvsMsg%>" != "null")
	alert("<%=cvsMsg%>");
</script>
</html>



