<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,
                 com.globalsight.everest.cvsconfig.*,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + CVSConfigConstants.UPDATE;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_cvsrepositroy");
    }
    else
    {
        saveURL +=  "&action=" + CVSConfigConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvsrepositroy");
    }
    
    String cancelURL = 
        cancel.getPageURL() + "&action=" + CVSConfigConstants.CANCEL;
    	ArrayList<CVSRepository> map = (ArrayList)request.getAttribute(CVSConfigConstants.CVS_REPOSITORY_LIST);
    CVSRepository cvsrepository = 
        (CVSRepository)sessionMgr.getAttribute(CVSConfigConstants.CVS_REPOSITORY);
    String name = "", repository = "", folderName = "", loginUser = "", loginPwd = "";
    long serverid = 0, repId = 0;
    boolean isReviewOnly = false;

    if (edit)
    {
    	repId = cvsrepository.getId();
        name = cvsrepository.getName();
        repository = cvsrepository.getRepository();
        folderName = cvsrepository.getFolderName();
        serverid = cvsrepository.getServer().getId();
        loginUser = cvsrepository.getLoginUser();
        loginPwd = cvsrepository.getLoginPwd();
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
var objectName = "<%=bundle.getString("lb_cvs_repository")%>";
var guideNode="cvsrepository";
var helpFile = "#";
function submitForm(formAction)
{
    if (formAction == "cancel")
    {
    	cvsserverForm.action = "<%=cancelURL%>";
    	cvsserverForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
        	cvsserverForm.action = "<%=saveURL%>";
        	cvsserverForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate activity name.
//
function confirmForm()
{
	if (!isSelectionMade(cvsserverForm.selectServer)) {
		alert("<%=bundle.getString("jsmsg_cvs_module_selected")%>");
		return false;
	}
    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_NAME%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_NAME%>.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_repository_name")%>");
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_NAME%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_NAME%>.focus();
        return false;
    }
<%
    if (map != null)
    {
    	for (CVSRepository c : map) {
    		if (repId == c.getId())
    			continue;
    		%>
            if ("<%=c.getServer().getId()%>" == cvsserverForm.selectServer.value 
                    && "<%=c.getRepository()%>" == cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.value
                    && "<%=c.getName()%>" == cvsserverForm.repositoryName.value)
                {
                    alert("<%=bundle.getString("jsmsg_duplicate_repository")%>");
                    return false;
                }
    		<%
    	}
    }
%>

    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_repository_cvs_name")%>");
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_CVS%>.focus();
        return false;
    }
    
    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_FOLDERNAME%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.REPOSITORY_FOLDERNAME%>.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_repository_folder")%>");
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_FOLDERNAME%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.REPOSITORY_FOLDERNAME%>.focus();
        return false;
    }

    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.value))
    {
        alert("<%=bundle.getString("jsmsg_cvs_repository_username")%>");
        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>.focus();
        return false;
    }
    
    if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value))
    {
    	alert("<%=bundle.getString("jsmsg_cvs_repository_password")%>");
        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.value = "";
        cvsserverForm.<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>.focus();
        return false;
    }

    return true;
}

function doOnload()
{
    loadGuides();
}
</script>

</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>
<form name="cvsserverForm" method="post" action="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_cvsservers")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="selectServer">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <%
                ArrayList array = 
                    (ArrayList)request.getAttribute(CVSConfigConstants.CVS_SERVER_LIST);
                String selected = "";
                for(int i = 0; i < array.size(); i++) {
                	  selected = "";
                    CVSServer cs = (CVSServer)array.get(i);
                    if(cs.getId() == serverid) {
                        selected = "selected";
                    }
            %>
              <option value="<%=cs.getId() %>" <%=selected %>>
                <%=cs.getName() %>
              </option>
            <%}%>
            </select>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_cvs_module_repository") %>
            <span class="asterisk">*</span>:
          </td>
          <td>
	        <input type="textfield" name="<%=CVSConfigConstants.REPOSITORY_NAME%>"
	         maxlength="40" size="30" value="<%=name%>" onblur="javascript:cvsserverForm.folderName.value=this.value;">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_cvs_name") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.REPOSITORY_CVS%>"
			    maxlength="40" size="30" value="<%=repository%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_foldername") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.REPOSITORY_FOLDERNAME%>"
			    maxlength="40" size="30" value="<%=folderName%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_username") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="textfield" name="<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER%>"
			    maxlength="40" size="30" value="<%=loginUser%>">
          </td>
        </tr>
        <tr>
          <td valign="top">
	        <%=bundle.getString("lb_cvs_repository_password") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		    <input type="password" name="<%=CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD%>"
			    maxlength="32" size="30" value="<%=loginPwd%>">
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
	<tr>
	  <td>
	    <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
	    onclick="submitForm('cancel')">
	    <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
	    onclick="submitForm('save')">
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</table>
</form>

</div>
</body>
</html>