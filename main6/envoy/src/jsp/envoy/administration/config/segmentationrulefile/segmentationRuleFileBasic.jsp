<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
            com.globalsight.everest.webapp.pagehandler.administration.config.segmentationrulefile.SegmentationRuleConstant,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileType,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.GlobalSightLocale,
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
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);

    Vector locales = (Vector)request.getAttribute(LocalePairConstants.LOCALES);
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    String lbvalidate = bundle.getString("lb_validate");
    String lbtest = bundle.getString("lb_test");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String cancelURL = cancel.getPageURL() + "&action=" + SegmentationRuleConstant.CANCEL;
    String validateURL = validate.getPageURL() + "&action=" + SegmentationRuleConstant.VALIDATE;
    String testURL = test.getPageURL() + "&action=" + SegmentationRuleConstant.TEST;
    String uploadURL = upload.getPageURL() + "&action=" + SegmentationRuleConstant.UPLOAD;
    String title = null;
    if (sessionMgr.getAttribute("edit") != null)
    {
        edit = true;
        saveURL += "&action=" + SegmentationRuleConstant.EDIT;
        title = bundle.getString("lb_edit_segmentation_rule");
    }
    else
    {
        saveURL += "&action=" + SegmentationRuleConstant.NEW;
        title = bundle.getString("lb_new_segmentation_rule");
    }


    // Data
    String invalid = (String)request.getAttribute("invalid");
    String dup = (String)request.getAttribute("dup");
    ArrayList names = (ArrayList)request.getAttribute(SegmentationRuleConstant.NAMES);
    SegmentationRuleFileImpl segmentationRule = (SegmentationRuleFileImpl)request.getAttribute("tmpRule");
    if (segmentationRule == null)
    {
        segmentationRule = (SegmentationRuleFileImpl)sessionMgr.getAttribute(SegmentationRuleConstant.SEGMENTATIONRULE_KEY);
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
    String text = (String)request.getAttribute("ruleTextFromFile");
    GlobalSightLocale selectedLocale = (GlobalSightLocale)request.getAttribute("selectedLocale");
    

    String ruleName = "";
    String desc = "";
    if(text == null) text = "";
    int ruleType = 0;
    String companyName = "";
    if (segmentationRule != null)
    {
        ruleName = segmentationRule.getName();
        if (ruleName == null) ruleName = "";
        desc = segmentationRule.getDescription();
        if (desc == null) desc = "";
        text = segmentationRule.getRuleText();
        ruleType = segmentationRule.getType();
        long companyId = segmentationRule.getCompanyId();
        if(companyId != -1)
        {
        	companyName = CompanyWrapper.getCompanyNameById(companyId);
        }
    }
%>
<html>
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/StringBuffer.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_segmentation_rule")%>";
var guideNode="segmentationRules";
var helpFile = "<%=bundle.getString("help_segmentation_rules_basic_screen")%>";

function submitFileForm()
{
	var file_name = fileForm.filename.value;
	var file_extension = file_name.substring(file_name.lastIndexOf("."), file_name.length);
	var accept_extension = "*.xml,*.txt";
	if(accept_extension.indexOf(file_extension.toLowerCase()) == -1)
	{
		alert("<%=bundle.getString("lb_rules_files_extension")%>" + "\n" + accept_extension);
	}
	else
	{
		fileForm.submit();
	}
}

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        segmentationForm.action = "<%=cancelURL%>";
        segmentationForm.submit();
    }

    // <% if (!edit) { %>
    segmentationForm.saveRuleName.value = segmentationForm.nameField.value;
    // <% } %>

    if (formAction == "validate")
    {
        segmentationForm.action = "<%=validateURL%>";
        segmentationForm.submit();
    }
    
    if (formAction == "test")
    {
        segmentationForm.action = "<%=testURL%>";
        segmentationForm.submit();
    }
    
    if (formAction == "save")
    {
        if (confirmForm())
        {
            segmentationForm.action = "<%=saveURL%>";
            segmentationForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate name.
//
function confirmForm()
{
    if (!segmentationForm.nameField)
    {
        // can't change name on edit
        return true;
    }

    if (isEmptyString(segmentationForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_segmentationrulefile_empty"))%>");
        segmentationForm.nameField.value = "";
        segmentationForm.nameField.focus();
        return false;
    }

    if (hasSpecialChars(segmentationForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%> " +
              "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        segmentationForm.nameField.focus();
        return false;
    }

    var nameValue = new StringBuffer(segmentationForm.nameField.value);
    var nameTrimed = nameValue.trim();
    // check for dups
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String rulename = (String)names.get(i);
%>
            if ("<%=rulename%>" == nameTrimed)
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_duplicate_segmentationrulefile"))%>");
                segmentationForm.nameField.focus();
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
	document.getElementById("saveBtn").disabled = true;
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
<span class=errorMsg><% if (invalid != null && invalid.length() > 0) out.print(invalid); %></span>

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <form name="segmentationForm" method="post" action="">
  <input type=hidden name=saveRuleName value="<%=ruleName%>">
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2" width=500>
	<tr>
	  <td width=20>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <% if (edit) { %>
	        <%=ruleName%>
	    <% } else { %>
                <input type="textfield" name="nameField" maxlength="40" size="40"
                    value="<%=ruleName%>">
            <% } %>
          </td>
        </tr>
        <tr>
        <td valign="top">
          <%=bundle.getString("lb_type")%><span class="asterisk">*</span>:
        </td>
        <td>
        <select name="type">
          <%
          String [] typelist = SegmentationRuleFileType.getTypeList();
          for (int i = 0; i < typelist.length; i++)
		 {
			out.print("<option value=\"" + i + "\" ");
			if(ruleType == i) 
			{
				out.print("selected=\"selected\"");
			}
			out.print(">" + SegmentationRuleFileType.getTypeString(i) + "</option>");
		 }
          %>
        </select>
        </td>
      </tr>
      <%if (isSuperAdmin){%>
      <tr>
      <td valign="top">
        <%=bundle.getString("lb_company")%><span class="asterisk">*</span>:
      </td>
      <td>
      <%if(edit){%>
      <%=companyName%>
      <%} else {%>
      <select name="companyName">
        <%
        String[] companies = (String[])sessionMgr.getAttribute("companyNames");
       
        for (int i = 0; i < companies.length; i++)
        {
            out.println("<option value='"+companies[i]+"'");
            if (companies[i].equals(companyName))
            {
                out.println(" selected ");
            }
            out.println(">" + companies[i] + "</option>");
         }
		 
        %>
      </select>
      <%}%>
      </td>
    </tr>
    <%}%>
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
            <textarea rows="6" cols="40" name="textField" onchange="doInvalidate()"
	    ><%=text%></textarea>
          </td>
          <td>
              <input type="button" name="<%=lbvalidate%>" value="<%=lbvalidate%>"
                onclick="submitForm('validate')">
          </td>
        </tr>
        <tr>
        <td></td>
        <td colspan="2">
        <input type="button" name="cancelBtn" id="cancelBtn" value="<%=lbcancel%>"
          onclick="submitForm('cancel')">
<% if (invalid != null && invalid.equals("")) { 
      if (request.getAttribute("isValidate") != null)
      {
%>
        <script>
            function reportValid() { alert('<%=bundle.getString("jsmsg_segmentationrule_validation_noerror") %>'); }
            var oldOnload = window.onload;
            if (typeof oldOnload != 'function') { window.onload = reportValid; }
            else { window.onload = function() { oldOnload(); reportValid(); } }
        </script>
<%    } %>
        <input type="button" name="saveBtn" id="saveBtn" value="<%=lbsave%>" onclick="submitForm('save')">
<% } else { %>
        <input type="button" name="saveBtn" id="saveBtn" value="<%=lbsave%>" disabled="disabled">
<% } %>
      </td>
      </tr>
      </table>
    </td>
    <td>
    <%=bundle.getString("lb_segmentation_rule_test")%><br>
    <table border="0" class="standardText" cellpadding="2">
        <tr>
        <td>
        <select name="locale" class="standardText">
        <% 
            for (int i = 0; i < locales.size(); i++)
            {
                GlobalSightLocale locale = (GlobalSightLocale)locales.elementAt(i);
                out.print("<option value=\"" + locale.getId() + "\" ");
                if(selectedLocale != null)
                {
                	if (selectedLocale.equals(locale))
                	{
                		out.print("selected=\"selected\"");
                	}
                }
                else if(locale.isUiLocale()) 
                {
                	out.print("selected=\"selected\"");
                }
                out.print(">" + locale.getDisplayName(uiLocale) + "</option>");
            }
        %>
        </select>
        </td>
      </tr>
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
  </form>
</table>
</body>
</html>
