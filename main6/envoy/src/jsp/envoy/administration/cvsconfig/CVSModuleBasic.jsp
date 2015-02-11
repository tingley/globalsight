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
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_cvsmodule");
    }
    else
    {
        saveURL +=  "&action=" + CVSConfigConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvsmodule");
    }
    
    String cancelURL = 
        cancel.getPageURL() + "&action=" + CVSConfigConstants.CANCEL;

    // Data
    ArrayList<CVSServer> servers = (ArrayList<CVSServer>)request.getAttribute(CVSConfigConstants.CVS_SERVER_LIST);
    CVSModule cvsmodule = 
        (CVSModule)sessionMgr.getAttribute(CVSConfigConstants.CVS_MODULE);

    String name = "", modulename = "", branchName = "", serverName = "";
    long serverID = 0, moduleId = 0;
    boolean isReviewOnly = false;
    boolean isCheckedOut = false;

    if (cvsmodule != null && edit) {
    	moduleId = cvsmodule.getId();
        name = cvsmodule.getName();
        modulename = cvsmodule.getModulename();
        branchName = cvsmodule.getBranch();
        serverID = cvsmodule.getServer().getId();
        serverName = cvsmodule.getServer().getSandbox();
        isCheckedOut = CVSUtil.isCheckedOut(serverName, name);
    }
%>
<html>
<head>
<title><%=title%></title>
	<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/tree/css/dhtmlXTree.css">
	<script  src="/globalsight/includes/tree/js/dhtmlXCommon.js"></script>
	<script  src="/globalsight/includes/tree/js/dhtmlXTree.js"></script>		
	<script  src="/globalsight/includes/tree/js/dhtmlXTreeExtend.js"></script>

<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_cvs_server")%>";
var guideNode="cvsserver";
var helpFile = "<%=bundle.getString("helper_text_cvsserver")%>";
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
            var length = cvsserverForm.moduleList.length;
            var names = "";
            for (var i=0; i<length-1;i++) {
                names += cvsserverForm.moduleList.options[i].text + ",";
            }
            names += cvsserverForm.moduleList.options[length-1].text;
            cvsserverForm.moduleNames.value = names;
                    
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

	if (isEmptyString(cvsserverForm.<%=CVSConfigConstants.MODULE_NAME%>.value) || !validInput(cvsserverForm.<%=CVSConfigConstants.MODULE_NAME%>.value))
    {
        alert("<%=bundle.getString("jsmsg_module_gs_name")%>");
        cvsserverForm.<%=CVSConfigConstants.MODULE_NAME%>.focus();
        return false;
    }
    
    if (cvsserverForm.moduleList.length == 0) {
        alert("<%=bundle.getString("jsmsg_cvs_module_need_module_name")%>");
        cvsserverForm.<%=CVSConfigConstants.MODULE_MODULENAME%>.focus();
        return false;
    }
    // check for dups 
<%
	for (CVSServer server : servers) {
		Set<CVSModule> modules = server.getModuleSet();
		for (CVSModule m : modules) {
			if (!m.isActive() || m.getId() == moduleId)
				continue;
%>
            if ("<%=m.getName()%>".toLowerCase() == cvsserverForm.selfname.value.toLowerCase())
            {
                alert("<%=bundle.getString("jsmsg_duplicate_module")%>");
                return false;
            }
<%
        }
    }
%>

    return true;
}

function doOnload()
{
    loadGuides();
}

function selectModule() {
	if (!isSelectionMade(cvsserverForm.selectServer)) {
		alert("<%=bundle.getString("jsmsg_cvs_module_selected")%>");
		return false;
	}

	window.open("/globalsight/envoy/administration/cvsconfig/fileSelect.jsp?serverId="+cvsserverForm.selectServer.value, "Module", "height=600px,width=500px, resizable=yes, scrollbars=yes");
}

function addNewModule() {
	var value = stripBlanks(cvsserverForm.moduleName.value);
	var isExisted = false;
	var length = cvsserverForm.moduleList.length;
	for (var i=0; i<length; i++) {
            var text = cvsserverForm.moduleList.options[i].text;
            if (text == value) {
               isExisted = true;
               alert("<%=bundle.getString("jsmsg_cvs_module_name_existed") %>".replace("%1", value));
               break;
            }
	}

    if (!isEmptyString(value) && !isExisted) {
		cvsserverForm.moduleList.length++;
		cvsserverForm.moduleList.options[cvsserverForm.moduleList.length - 1].text = value;
		cvsserverForm.moduleList.options[cvsserverForm.moduleList.length - 1].value = cvsserverForm.moduleList.length;
    }
}

function removeNewModule() {
	var length = cvsserverForm.moduleList.length;
	for (var i=length-1;i>=0;i--) {
		if (cvsserverForm.moduleList.options[i].selected)
			cvsserverForm.moduleList.remove(i);
	}
}

function removeAllNewModules() {
    var length = cvsserverForm.moduleList.length;
    for (var i=length-1;i>=0;i--) {
        cvsserverForm.moduleList.remove(i);
    }
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
            <%=bundle.getString("lb_cvsservers")+" "+bundle.getString("lb_name") %><span class="asterisk">*</span>:
          </td>
          <td colspan="2">
            <select name="selectServer" style="width:300px">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <%
            ArrayList array = (ArrayList)request.getAttribute(CVSConfigConstants.CVS_SERVER_LIST);
            String selected = "";
           
            for(int i = 0; i < array.size(); i++) {
                CVSServer cs = (CVSServer)array.get(i);

                if(cs.getId() == serverID) {
                    selected = "selected";
                } else 
                	selected = "";
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
            <%=bundle.getString("lb_cvs_module_gs_name") %>
            <span class="asterisk">*</span>:
          </td>
          <td colspan="2">
          <% if (edit) { %>
	           <input type="textfield" style="width:300px" maxlength="100" size="44" value="<%=name%>" disabled />
	           <input type="hidden" name="<%=CVSConfigConstants.MODULE_NAME%>" value="<%=name%>" />
	      <% } else { %>
  	           <input type="textfield" name="<%=CVSConfigConstants.MODULE_NAME%>" style="width:300px" maxlength="100" size="44" value="<%=name%>"/>
  	      <% } %>
          </td>
        </tr>
        <tr>
          <td valign="top">
          <%=bundle.getString("lb_cvs_module_cvs_name") %>
	           <span class="asterisk">*</span>:
          </td>
          <td colspan="2">
	         <input type="textfield" name="<%=CVSConfigConstants.MODULE_MODULENAME%>" style="width:300px"
		       maxlength="100" size="44" value="">&nbsp;&nbsp;
		     <input type="button" name="addModule" value="<%=bundle.getString("lb_add_module") %>" <%=isCheckedOut?"disabled":"" %> onclick="addNewModule()" />
	     </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_module_name_list") %>:
          </td>
          <td>
             <select name="moduleList" multiple="multiple" size="6" style="width:300px">
               <%
               if (modulename != "") {
            	   String[] names = modulename.split(",");
            	   for (int i=0;i<names.length;i++) {
            		   out.println("<option value='" + i + "'>" + names[i] + "</option>");
            	   }
               }
               %>
             </select>
           </td>
           <td>
           	<div valign="top">
                <input type="button" name="removeModule" value="<%=bundle.getString("lb_remove_module") %>" <%=isCheckedOut?"disabled":"" %> onclick="removeNewModule()" /><br/><br/>
                <input type="button" name="removeAllModule" value="<%=bundle.getString("lb_remove_all_module") %>" <%=isCheckedOut?"disabled":"" %> onclick="removeAllNewModules()" />
              </div>
         </td>
        </tr>
        <input type="hidden" name="moduleNames" value="" />
        <tr>
          <td valign="top">
          <%=bundle.getString("lb_cvs_module_branch") %>
	           <span class="asterisk">*</span>:
          </td>
          <td colspan="2">
		         <input type="textfield" name="branchName" style="width:300px"
			       maxlength="100" size="44" value="<%=(branchName == null || branchName.equals("")) ? "HEAD" : branchName%>">
          </td>
        </tr>
        <tr><td colspan="3">&nbsp;</td></tr>
	<tr>
	  <td colspan="3">
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