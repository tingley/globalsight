<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.config.xmlrulefile.XmlRuleConstant,
            com.globalsight.cxe.entity.xmlrulefile.XmlRuleFileImpl,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.FormUtil,
            com.globalsight.util.GeneralException,
            java.text.MessageFormat,
            java.util.*"
    session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="validate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="test" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    String lbvalidate = bundle.getString("lb_validate");
    String lbtest = bundle.getString("lb_test");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String cancelURL = cancel.getPageURL() + "&action=" + XmlRuleConstant.CANCEL;
    String validateURL = validate.getPageURL() + "&action=" + XmlRuleConstant.VALIDATE;
    String testURL = validate.getPageURL() + "&action=" + XmlRuleConstant.TEST;
    String title = null;
    String invalid = (String)request.getAttribute("invalid");
    if (sessionMgr.getAttribute("edit") != null)
    {
        edit = true;
        saveURL += "&action=" + XmlRuleConstant.EDIT;
        title = bundle.getString("lb_edit_xml_rule");
    }
    else
    {
        saveURL += "&action=" + XmlRuleConstant.NEW;
        title = bundle.getString("lb_new_xml_rule");
    }


    // Data
    String dup = (String)request.getAttribute("dup");
    ArrayList names = (ArrayList)request.getAttribute(XmlRuleConstant.NAMES);
    XmlRuleFileImpl xmlRule = (XmlRuleFileImpl)request.getAttribute("tmpRule");
    if (xmlRule == null)
    {
        xmlRule = (XmlRuleFileImpl)sessionMgr.getAttribute(XmlRuleConstant.XMLRULE_KEY);
    }

    String ruleName = "";
    String desc = "";
    String text = "";
    if (xmlRule != null)
    {
        ruleName = xmlRule.getName();
        if (ruleName == null) ruleName = "";
        desc = xmlRule.getDescription();
        if (desc == null) desc = "";
        text = xmlRule.getRuleText();
    }

    String testText = (String)request.getAttribute("testText");
    if(testText == null)
    {
    	testText = "";
    }
    String testResult = (String)request.getAttribute("testResult");
    if(testResult == null)
    {
    	testResult = "";
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_xml_rule")%>";
var guideNode="xmlRules";
var helpFile = "<%=bundle.getString("help_xml_rules_basic_screen")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        xmlForm.action = "<%=cancelURL%>";
        xmlForm.submit();
    }

    // <% if (!edit) { %>
    xmlForm.saveRuleName.value = xmlForm.nameField.value;
    // <% } %>

    if (formAction == "validate")
    {
        xmlForm.action = "<%=validateURL%>";
        xmlForm.submit();
    }

    if (formAction == "test")
    {
        xmlForm.action = "<%=testURL%>";
        xmlForm.submit();
    }

    if (formAction == "save")
    {
        if (confirmForm())
        {
            xmlForm.action = "<%=saveURL%>";
            xmlForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate name.
//
function confirmForm()
{
    if (!xmlForm.nameField)
    {
        // can't change name on edit
        return true;
    }

    if (isEmptyString(xmlForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_xmlrulefile_empty"))%>");
        xmlForm.nameField.value = "";
        xmlForm.nameField.focus();
        return false;
    }

    if (hasSpecialChars(xmlForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%> " +
              "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        xmlForm.nameField.focus();
        return false;
    }

    // check for dups
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String rulename = (String)names.get(i);
%>
            if ("<%=rulename%>".toLowerCase() == xmlForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_duplicate_xmlrulefile"))%>");
                xmlForm.nameField.focus();
                return false;
            }
<%
        }
    }
%>

    return true;
}

function doInvalidate()
{
    xmlForm.saveBtn.disabled = true;
}

</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
      onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>
<span class=errorMsg><% if (invalid != null) out.print(invalid); %></span>

<form name="xmlForm" method="post" action="">
<input type=hidden name=saveRuleName value="<%=ruleName%>">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td>  
			<table border="0" cellspacing="2" cellpadding="2" class="standardText">
			  <tr><td></td></tr>
			  <tr valign="top">
			    <td>
			      <table border="0" class="standardText" cellpadding="2">
				<tr>
				  <td>
			            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
			          </td>
			          <td>
			            <% if (edit) { %>
				        <%=ruleName%>
				    <% } else { %>
			                <input type="textfield" name="nameField" maxlength="40" size="30"
			                    value="<%=ruleName%>">
			            <% } %>
			          </td>
			        </tr>
			        <tr>
			          <td valign="top">
			            <%=bundle.getString("lb_description")%>:
			          </td>
			          <td>
			            <textarea rows="6" cols="40" name="descField"><%=desc%></textarea>
			          </td>
			        </tr>
			        <tr>
			          <td valign="top">
			            <%=bundle.getString("lb_rules")%><span class="asterisk">*</span>:
			          </td>
			          <td>
			            <textarea rows="6" cols="40" name="textField" 
			                onchange="doInvalidate()"><%=text%></textarea>
			          </td>
			          <td>
			              <input type="button" name="<%=lbvalidate%>" value="<%=lbvalidate%>"
			                onclick="submitForm('validate')">
			          </td>
			        <tr><td>&nbsp;</td></tr>
				<tr>
			          <td colspan="2">
			          <input type="button" name="cancelBtn" value="<%=lbcancel%>"
			            onclick="submitForm('cancel')">
			          <% if (invalid != null && invalid.equals("")) {
			              if (request.getAttribute("isValidate") != null)
                            {
                      %>
			         <script>
                        function reportValid() {
                           alert('<%=bundle.getString("jsmsg_xmlrulefile_validate_no_error") %>');
                        }
                        var oldOnload = window.onload;
                        if (typeof oldOnload != 'function') { 
                           window.onload = reportValid;
                        }
                        else { 
                           window.onload = function() { oldOnload(); reportValid(); } 
                        }
                    </script>
                    <% } %>
				    <input type="button" name="saveBtn" value="<%=lbsave%>"
				     onclick="submitForm('save')">
			        <% } else { %>
				    <input type="button" name="saveBtn" value="<%=lbsave%>"
				     disabled onclick="submitForm('save')">
			        <% } %>
				  </td>
				</tr>
			      </table>
			    </td>
			  </tr>
			</table>
  </td>
  <td>  
            <%=bundle.getString("lb_xml_rule_input_test") %><br>
		    <table border="0" class="standardText" cellpadding="2">
		        <tr>
		          <td>
		            <textarea rows="6" cols="40" name="testField"><%=testText%></textarea>
		          </td>
		        </tr>
		        <tr>
		        <td colspan="2" align="center">
		  	    <input type="button" name="testBtn" id="testBtn" value="<%=lbtest%>"
		  	     onclick="submitForm('test')">
		  	  </td>
		  	  </tr>
		        <tr>
		          <td>
		            <textarea rows="6" cols="40" name="resultField" readonly><%=testResult%></textarea>
		          </td>
		        </tr>
		      </table>
  </td>
  </tr>
</table>			  

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_XML_RULE); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

</form>
</body>
</html>
