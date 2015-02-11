<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.util.GlobalSightLocale,
        com.globalsight.everest.projecthandler.ProjectInfo,
        com.globalsight.util.FormUtil,
        com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants"
    session="true"
%>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancle" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
    WebAppConstants.SESSION_MANAGER);
String choose = bundle.getString("lb_choose");
List projectInfos = (List)request.getAttribute(WebAppConstants.CUSTOMIZE_REPORTS_PROJECT_LIST);
Map<String, List<GlobalSightLocale>> map = (Map<String, List<GlobalSightLocale>>)request.getAttribute(LocalePairConstants.LOCALES);
String saveUrl = save.getPageURL();
String cancleUrl = cancle.getPageURL();
%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_new_job_group")%></TITLE>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<link rel="stylesheet" type="text/css" href="/globalsight/envoy/tm/management/tm.css"/>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<script src="/globalsight/jquery/jquery-1.6.4.min.js" type="text/javascript"></script>
<script src="/globalsight/includes/jquery-ui-custom.min.js" type="text/javascript"></script>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "myGroups";
var helpFile = "<%=bundle.getString("help_job_group_new")%>";

function doCancel()
{
    window.location.href = "<%=cancleUrl%>";
}

function doOK()
{
    if (hasSpecialChars(groupForm.<%=WebAppConstants.JOB_GROUP_NAME%>.value))
    {
        alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }
    
    var result = buildDefinition();
    if (result)
    {
    	if(!validName()){
    		return false;
    	}
    }
    else
    {
    	groupForm.<%=WebAppConstants.JOB_GROUP_NAME%>.focus();
    	return false;
    }
    
    if (groupForm.<%=WebAppConstants.JOB_GROUP_PROJECT%>.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_select_project")%>");
        return false;
    }
    if (groupForm.<%=WebAppConstants.JOB_GROUP_SOURCELOCAL%>.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_local_pair_select_source")%>");
        return false;
    }
    groupForm.submit();
}

function buildDefinition()
{
    var name = Trim(groupForm.<%=WebAppConstants.JOB_GROUP_NAME%>.value);
    if (name == "")
    {
    	alert("Please enter a name.");
    	return false;
    }
   	return true;
}

function validName() {
	    var name = allTrim(groupForm.<%=WebAppConstants.JOB_GROUP_NAME%>.value);
	    var existNames = "<%=(String) request.getAttribute(WebAppConstants.JOB_GROUP_EXISTNAMES) %>";

	    var lowerName = name.toLowerCase();
	    existNames = existNames.toLowerCase();

	    if (existNames.indexOf("," + lowerName + ",") != -1) {
	    	alert(name + " is already existed. Please input another one.");
	        return false;
	    }
	    else
	    	groupForm.<%=WebAppConstants.JOB_GROUP_NAME%>.value = name;
	        return true;
}

function doOnLoad()
{
    // This loads the guides in guides.js and the
    loadGuides();

}

function setSourceLocale()
{
	  var sel = groupForm.<%=WebAppConstants.JOB_GROUP_PROJECT%>;
	  var selectLocale = document.getElementById("<%=WebAppConstants.JOB_GROUP_SOURCELOCAL%>");    
	  if (sel.options.length > 0)
	  {
		  var project="";
		  for(var i = 0;i < sel.options.length;i++ )
		  {
		  	 if(sel.options[i].selected)
		  	 {
		  		project = sel.options[i].value;
		  	 }  
		  }
		  if(project != "" && project != -1){
			  var keyVar = "";
			  <%
			  	Set<String> keySet = map.keySet();
			  	for(String key : keySet)
			  	{
			  		List<GlobalSightLocale> localeList = map.get(key);
			  	%>
			  		keyVar = "<%=key%>";
				  	if(project == keyVar)
				  	{
				  		selectLocale.options[0] = new Option("Choose...","-1");  
				  		<%
				  		 long localeId = -1;
						 String localeName = null;
			  			 for(int i =0; i < localeList.size();i++){
			  					GlobalSightLocale locale = localeList.get(i);
			                    localeId = locale.getId();
			                    localeName = locale.getDisplayName(uiLocale);
			              %>
			             	 selectLocale.options[<%=i+1%>] = new Option("<%=localeName%>","<%=localeId%>");  
			              <%
			  			 }
			  		%>
				  	}
			  	<%
			  }
			  %>
			  selectLocale.disabled = false;
		  }
		  else if(project != "" && project == -1)
		  {
			  selectLocale.options.length=0;
			  var option="<option value=\"-1\">Choose...</option>";
              selectLocale.innerHTML=option;
              selectLocale.disabled = true;
		  }
	  }
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
      TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE="POSITION: absolute; Z-INDEX: 0; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" id="idHeading"><%=bundle.getString("lb_new_job_group")%></DIV><br>
<FORM NAME="groupForm" METHOD="POST" action="<%=saveUrl%>">
<INPUT TYPE="hidden" NAME="<%=WebAppConstants.ACTION_STRING%>"
 VALUE="<%=WebAppConstants.JOB_GROUP_SAVE%>">
 
<TABLE CELLSPACING="2" CELLPADDING="2" BORDER="0">
  <THEAD>
    <COL align="right" valign="top" CLASS="standardText">
    <COL align="left"  valign="top" CLASS="standardText">
  </THEAD>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_name")%><span class="asterisk">*</span>: </TD>
    <TD CLASS="standardText"><INPUT NAME="<%=WebAppConstants.JOB_GROUP_NAME%>"
      TYPE="text" MAXLENGTH="100" SIZE="40"></TD>
  </TR>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_project")%><span class="asterisk">*</span>: </TD>
    <TD CLASS="standardText">
    	<SELECT NAME="<%=WebAppConstants.JOB_GROUP_PROJECT%>" onchange="setSourceLocale();"  CLASS="standardText" style="width:200px;">
	          <OPTION VALUE="-1"><%= choose %></OPTION>
	          <%
	          long projectId = -1;
	          String projectName = null;
	          if (projectInfos != null)
	          {
	              int pSize = projectInfos.size();                        
	              for (int i=0; i<pSize; i++)
	              {
	                 ProjectInfo p = (ProjectInfo)projectInfos.get(i);
	                 projectName = p.getName();
	                 projectId = p.getProjectId();
	            %>
               <OPTION VALUE="<%= projectId%>"><%= projectName %></OPTION>
          <%  }
          }%>
        </SELECT>
    </TD>
  </TR>
  <TR>
    <TD CLASS="standardText"><%=bundle.getString("lb_source_locale")%><span class="asterisk">*</span>:</TD>
    <TD CLASS="standardText">
    	<select name="<%=WebAppConstants.JOB_GROUP_SOURCELOCAL%>" id="<%=WebAppConstants.JOB_GROUP_SOURCELOCAL%>"  disabled class="standardText"  style="width:200px;">
    		<OPTION VALUE="-1"><%= choose %></OPTION>
    	 </select>
    </TD>
  </TR>
</TABLE>
<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_JOB_GROUP); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
</FORM>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" ID="Cancel" onclick="doCancel();">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_save")%>" ID="OK" onclick="doOK();">
</DIV>
</BODY>
</HTML>
