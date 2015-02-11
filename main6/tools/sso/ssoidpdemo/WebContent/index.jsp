<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="com.globalsight.everest.foundation.sso.SSOIdPHelper,
    com.globalsight.everest.foundation.sso.SSOIdPHelperSaml,
    com.globalsight.everest.foundation.sso.SSOIdPHelperSimple"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
String responseData = null;
String gsLink = "http://localhost:8080/globalsight/";
String userId = "user1";
String companyName = "way";
boolean useSaml = true;

SSOIdPHelper helper = SSOIdPHelper.createInstance(useSaml);
responseData = helper.createLoginResponse(userId, companyName);

%>

<script type="text/javascript">
function submitSsoForm()
{
	document.ssoForm.submit();
}
</script>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>SSO IdP Demo Site</title>
</head>
<body>
<h4>SSO Logon Case 2 - assume "<%=userId %>" is logged on this system</h4>
<br />
Click <a href="#" onclick="submitSsoForm()">here</a> to access GlobalSight with "<%=userId %>", 
this user is belong to Company "<%= companyName %>" in GlobalSight (<%=gsLink %>).
<br />
<br />
<i>
Note:
<br />
Modify GlobalSight link, user id and company name directly in "index.jsp" for test purpose.
<br />
user1 password, user2 password, user3 password, user4 password

</i>

<FORM NAME="ssoForm" ACTION="<%=gsLink%>" METHOD="post">
<input type="hidden" name="ssoResponseData" value="<%= responseData %>" />
</FORM>

Data:
<%= "<pre>" + responseData + "</pre>" %>

</body>
</html>