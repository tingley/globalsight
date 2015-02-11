<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
	    	com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
	    	com.globalsight.everest.webapp.pagehandler.PageHandler,
	    	com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.foundation.LocalePair,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.localemgr.CodeSet,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.projecthandler.TranslationMemoryProfile,
            com.globalsight.everest.projecthandler.ProjectTM,
            com.globalsight.everest.projecthandler.LeverageProjectTM,
            com.globalsight.everest.util.comparator.TmComparator,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl,
            com.globalsight.everest.util.comparator.SegmentationRuleFileComparator,
            com.globalsight.everest.foundation.TDATM,
            java.util.Collections,
            java.util.List,
            java.util.Locale,
            java.util.HashMap,
            java.util.Vector,
            java.util.Iterator,
            java.util.Enumeration,
            java.util.ResourceBundle"
    session="true" %>
<%@ include file="/envoy/common/header.jspIncl" %>

<jsp:useBean id="saveTDAOptions" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelTDAOptions" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="main" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    String checkMessage = (String) request.getAttribute("checkInfo");
    
    if(checkMessage != null && checkMessage.equals("ture")) {
        response.sendRedirect(main.getPageURL());
    }
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_tda_options_edit");

    SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
    TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) sessionMgr.getAttribute(TMProfileConstants.TM_PROFILE);
   
   //Urls of the links on this page
   String saveTDAOptionsUrl = saveTDAOptions.getPageURL();
   String cancelTDAOptionsUrl = cancelTDAOptions.getPageURL();
   
   String enableTda = "";
   String hostName = "http://www.tausdata.org/api";
   String password = "";
   String userName = "";
   String description = "";
   
   if(tmProfile.getTdatm() != null) {
       TDATM tdatm = tmProfile.getTdatm();
       
       if(tdatm.getEnable() == 1) {
           enableTda = "checked";
       }
       
       hostName = tdatm.getHostName();
       userName = tdatm.getUserName();
       password = tdatm.getPassword();
       //description = tdatm.getDescription();   
   }
%>
<HTML>
<HEAD>
	<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<TITLE><%=title %></TITLE>
	<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
	<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
	<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
	<%@ include file="/envoy/common/warning.jspIncl" %>
	
<SCRIPT language="JavaScript">
	var needWarning = false;
	var objectName = "TDA Options";
	var guideNode = "tmProfiles";
	var helpFile = "<%=bundle.getString("help_tmprofile_tda_options")%>"; 

	function submitForm(formAction)
	{
		if (formAction == "cancelTDAOptions") 
		{
			if (confirmJump()) {
				document.OptionsForm.action = '<%=cancelTDAOptionsUrl%>';
				document.OptionsForm.submit();
			} else {
				return false;
			}
		}
		else if (formAction == "saveTDAOptions")
		{
			var enableTda = document.getElementById("enableTda").checked;
			var hostName = document.getElementById("hostName").value;
			var userName = document.getElementById('userName').value;
			var password = document.getElementById('password').value;

			if (hostName == null || trim(hostName)== "" ) {
			    alert("<%=bundle.getString("jsmsg_tda_host_name_warning") %>");
			    return false;
			}
			else if (userName == null || trim(userName)== "" ) {
			    alert("<%=bundle.getString("jsmsg_tda_username_warning") %>");
			    return false;
			}
			else if (password == null || trim(password)== "" ) {
			    alert("<%=bundle.getString("jsmsg_tda_password_warning") %>");
			    return false;
			}
			else if(hostName.length > 50) {
			    alert("<%=bundle.getString("jsmsg_tda_url_length_warning") %>");
			    return false;
			}
			else if(userName.length > 50) {
			    alert("<%=bundle.getString("jsmsg_tda_username_length_warning")%>");
			    return false;
			}
			else if(password.length > 50) {
			    alert("<%=bundle.getString("jsmsg_tda_password_length_warning")%>");
			    return false;
			}

			document.OptionsForm.action = "<%=saveTDAOptionsUrl%>" + "&action=modify";
			document.OptionsForm.submit();
		}
		
	}

	//remove all whitespace on left and right
	function trim(str)
	{
	     return str.replace(/(^\s*)|(\s*$)/g, '');
	}

	function ltrim(str)
	{
	     return str.replace(/(^\s*)/g,'');
	}

	function rtrim(str)
	{
	     return str.replace(/(\s*$)/g,'');
	}
	
	function clickCheckbox()
	{
	    var tdaBox = document.getElementById("enableTda");

	    if(!tdaBox.checked) {
	        document.getElementById("hostName").disabled = true;
	        document.getElementById("userName").disabled = true;
	        document.getElementById("password").disabled = true;
	        //document.getElementById("description").disabled = true;
	    }
	    else {
	        document.getElementById("hostName").disabled = false;
	        document.getElementById("userName").disabled = false;
	        document.getElementById("password").disabled = false;  
	        //document.getElementById("description").disabled = false;
	    }
	}

</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides();clickCheckbox()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="Z-INDEX: 9; RIGHT: 20px; LEFT: 20px; POSITION: absolute; WIDTH: 800px; TOP: 108px">

<DIV CLASS="mainHeading" id="idHeading"><%=title %><%=tmProfile.getName()==null?"":(" : " + tmProfile.getName())%></DIV>

<FORM NAME="OptionsForm" METHOD="POST" action="">
<INPUT TYPE="HIDDEN" NAME="<%=TMProfileConstants.TM_PROFILE_ID%>" VALUE="<%=tmProfile.getId()%>"/>

	<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0" class="standardText">
	  <THEAD>
	    <COL align="right" valign="top" CLASS="standardText">
	    <COL align="left"  valign="top" CLASS="standardText">
	  </THEAD>
	  
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_enable_tda") %>: </TD>
	    <TD>
        <input CLASS="standardText" type="checkbox" id="enableTda" name="enableTda" onClick="clickCheckbox()" <%=enableTda%>>
	    </TD>
	  </TR>
	  
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_url") %>:</TD>
	    <TD>
        <input type="text" id="hostName" name="hostName" value="<%=hostName%>" style="width:300px" maxLength=50>
	    </TD>
	  </TR>
	  
      <TR>
        <TD ALIGN="LEFT" STYLE="vertical-align: middle"><%=bundle.getString("lb_user_name") %>:</TD>
        <TD>
          <input type="text" id="userName" name="userName" value="<%=userName%>" style="width:300px" maxLength=50>
        </TD>
      </TR>
      
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_password") %>:</TD>
	    <TD>
        <input type="password" id="password" name="password" value="<%=password%>" style="width:300px" maxLength=50>
	    </TD>
	  </TR>
	  <!-- temp hiddden the description
	  <TR>
	    <TD align="left"><%=bundle.getString("lb_description") %>:</TD>
	    <TD>
        <TEXTAREA  id="description" name="description" style="width:300px"><%=description%></TEXTAREA>
	    </TD>
	  </TR-->

	</TABLE>

  </FORM>

	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel") %>" ID="Cancel" onclick="submitForm('cancelTDAOptions');">
	<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" ID="OK" onclick="submitForm('saveTDAOptions');">

</DIV>
<script>
    <%
        if(checkMessage != null && !checkMessage.equals("")) {
            out.println("alert('" + checkMessage + "');");
        }
    %>
</script>
</BODY>
</HTML>
