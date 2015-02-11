<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="com.globalsight.everest.foundation.sso.SSOIdPHelper,
    com.globalsight.demo.AccountManagerDemo,
    com.globalsight.everest.foundation.sso.SSOIdPHelperSaml,
    com.globalsight.everest.foundation.sso.SSOIdPHelperSimple"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
String useSamlStr = request.getParameter("useSaml");
String action = request.getParameter("action");
String requestData = request.getParameter("ssoRequestData");
String responseData = null;
boolean sendToSp = false;
String backTo = "";

boolean useSaml = useSamlStr == null ? true : useSamlStr.equalsIgnoreCase("true");
if (action == null)
{
if (requestData != null)
{
    SSOIdPHelper helper = SSOIdPHelper.createInstance(useSaml);
    AccountManagerDemo accountManager = new AccountManagerDemo();
    
    String[] result = helper.handleSSORequest(requestData, accountManager);
    
    responseData = result[0];
    backTo = result[1];
    sendToSp = true;
}
}
else if ("checkUserExists".equals(action))
{
    AccountManagerDemo accountManager = new AccountManagerDemo();
    boolean exists = accountManager.checkUserExists(requestData);
    response.getWriter().write(exists + "");
}

%>

<script type="text/javascript">
function init()
{
	<% if (sendToSp) { %>
	document.ssoForm.submit();
	<% } %>
}
</script>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>SSO IdP Demo Site</title>
</head>
<body onload="init()">

<h4>SSO Logon Case 1</h4>
<br />
This page is used to handle SSO request from GlobalSight.
<br />

<FORM NAME="ssoForm" ACTION="<%=backTo%>" METHOD="post">
<input type="hidden" name="ssoResponseData" value="<%= responseData %>" />
</FORM>

Data:
<%= "<pre>" + responseData + "</pre>" %>

</BODY>
</body>
</html>