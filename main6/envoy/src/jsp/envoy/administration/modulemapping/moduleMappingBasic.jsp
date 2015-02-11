<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.util.ResourceBundle,
                  com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping.ModuleMappingConstants,
      			  com.globalsight.everest.cvsconfig.modulemapping.*,
      			  com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
      			  com.globalsight.util.GlobalSightLocale"
          session="true" 
%>
<%@page import="com.globalsight.everest.cvsconfig.CVSServer"%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="createRename" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="updateRename" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="removeRename" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String title = "";
    boolean edit = false;
    String saveURL = save.getPageURL();
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + CVSConfigConstants.UPDATE;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_cvs_module_mapping");
    }
    else
    {
        saveURL +=  "&action=" + CVSConfigConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvs_module_mapping");
    }
    String cancelURL = cancel.getPageURL() + "&action=" + ModuleMappingConstants.CANCEL;
    String createURL = createRename.getPageURL() + "&action=" + ModuleMappingConstants.CREATE_RENAME;
    String updateURL = updateRename.getPageURL() + "&action=" + ModuleMappingConstants.UPDATE_RENAME;
    String removeURL = removeRename.getPageURL() + "&action=" + ModuleMappingConstants.REMOVE_RENAME;

    //Set data
    Vector sourceLocales = (Vector)request.getAttribute(ModuleMappingConstants.SOURCE_LOCALE_PAIRS);
    Vector targetLocales = (Vector)request.getAttribute(ModuleMappingConstants.TARGET_LOCALE_PAIRS);
    
    boolean isEdit = false;
    
    ModuleMapping mm = (ModuleMapping)sessionMgr.getAttribute(ModuleMappingConstants.MODULE_MAPPING_KEY);
    Set<ModuleMappingRename> renames = (Set)sessionMgr.getAttribute(ModuleMappingConstants.MODULE_MAPPING_RENAME_LIST);
    ArrayList<CVSServer> servers = (ArrayList)sessionMgr.getAttribute(CVSConfigConstants.CVS_SERVER_LIST);
    String sourceLocale = "", sourceLocaleLong = "", sourceModule = "", targetLocale = "", targetLocaleLong = "", targetModule = "";
    long serverId = -1L;
    Set<ModuleMappingRename> fileRenames = null;
    ModuleMappingRename fileRename = null;
    if (mm != null) {
    	sourceLocale = mm.getSourceLocale();
    	sourceLocaleLong = mm.getSourceLocaleLong();
    	sourceModule = mm.getSourceModule();
    	targetLocale = mm.getTargetLocale();
    	targetLocaleLong = mm.getTargetLocaleLong();
    	targetModule = mm.getTargetModule();
    	serverId = mm.getModuleId();
    	fileRenames = mm.getFileRenames();
    	
    	isEdit = true;
    }
%>



<%@page import="com.globalsight.util.GlobalSightLocale"%>
<%@page import="com.globalsight.everest.foundation.LocalePair"%>
<%@page import="com.globalsight.everest.cvsconfig.CVSModule"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants"%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>

	<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>

<style>
SELECT { behavior: url(/globalsight/includes/SmartSelect.htc); }
</style>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_cvs_module_mapping")%>";
var guideNode="rename";
var helpFile = "#";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        currentForm.action = "<%=cancelURL%>";
        currentForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
        	currentForm.action = "<%=saveURL%>";
        	currentForm.submit();
        }
    }
    if (formAction == "New")
    {
        currentForm.action = "<%=createURL%>";
        currentForm.submit();
    }
    if (formAction == "Edit")
    {
        value = getRadioValue(currentForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        
       	currentForm.action = "<%=updateURL%>" + "&id=" + value;
        currentForm.submit();
    }
    if (formAction == "Remove") 
    {
        value = getRadioValue(currentForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        if (confirm("<%=bundle.getString("jsmsg_remove_confirm")%>")) 
       		currentForm.action = "<%=removeURL%>" + "&id=" + value;
   		else
       		return;
        currentForm.submit();
    }
	return;
}

//
// Check required fields.
//
function confirmForm()
{
	if (!isSelectionMade(currentForm.cvsServer)) {
		alert("<%=bundle.getString("jsmsg_cvs_mm_select_module_first") %>");
		return false;
	}
    if (currentForm.sourceLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_cvs_mm_source_locale")%>");
        return false;
    }
    if (currentForm.sourceModule.value == "") {
		alert("<%=bundle.getString("jsmsg_cvs_mm_source_module")%>");
		return false;
    }
    if (currentForm.targetLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_cvs_mm_target_locale")%>");
        return false;
    }
    if (currentForm.targetModule.value == "") {
        alert("<%=bundle.getString("jsmsg_cvs_mm_target_module")%>");
        return false;
    }
    var sF, tF;
    var sM, tM;
    sM = currentForm.sourceModule.value;
    tM = currentForm.targetModule.value;
    if (sM.lastIndexOf("\\") < sM.lastIndexOf(".")) {
        sF = true;
    } else {
        sF = false;
    }
    if (tM.lastIndexOf("\\") < tM.lastIndexOf(".")) {
        tF = true;
    } else {
        tF = false;
    }
    if (!sF && tF) {
        alert("<%=bundle.getString("jsmsg_cvs_mm_dir_map_file") %>");
        return false;
    }
    return true;
}

function moduleSelect(mode) {
	var value = currentForm.cvsServer.value;
	if (value == "-1") {
		alert("<%=bundle.getString("jsmsg_cvs_module_selected")%>");
		return;
	}
	window.open("/globalsight/envoy/administration/modulemapping/fileSelect.jsp?type=module&mode="+mode+"&serverId="+value, mode, "height=500px,width=500px, resizable=yes, scrollbars=yes");
}

function enableButtons()
{
    if (currentForm.removeBtn)
    	currentForm.removeBtn.disabled = false;
    if (currentForm.editBtn)
    	currentForm.editBtn.disabled = false;
}

</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
    <br>
    <br>

<form name="currentForm" method="post" action="">

<table border="0" cellspacing="4" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
      	<tr>
          <td class="standardText"><%=bundle.getString("lb_cvs_server") %><span class="asterisk">*</span>:
          </td>
          <td colspan="4" align="left">
            <select name="cvsServer" class="standardText">
            <% 
                StringBuilder sb = null;
                String serverName, repositoryName, content;
                for (CVSServer s : servers)
                {
                    serverName = s.getName();
                    repositoryName = s.getRepository();
                    content = serverName + "-" + repositoryName;
                    if (s.getId() != serverId)
                        out.println("<option value=\"" + s.getId() + "\">" + content + "</option>");
                    else
                        out.println("<option value=\"" + s.getId() + "\" selected>" + content + "</option>");
                }
            %>
            </select>
          </td>
      	</tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_cvs_mm_source_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="sourceLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
                for (int i = 0; i < sourceLocales.size(); i++)
                {
                	GlobalSightLocale locale = (GlobalSightLocale)sourceLocales.elementAt(i);
                	if (!locale.getDisplayName().equals(sourceLocaleLong))
                    	out.println("<option value=\"" + locale.getId() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
                	else
                		out.println("<option value=\"" + locale.getId() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_cvs_mm_source_module")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="sourceModule" value="<%=sourceModule %>" size="40" editable="false" />&nbsp;&nbsp;
            <input type="button" name="sourceSelect" value="<%=bundle.getString("lb_select")%>"
            onclick="moduleSelect('Source')">
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_cvs_mm_target_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="targetLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
	            for (int i = 0; i < targetLocales.size(); i++)
	            {
	            	GlobalSightLocale locale = (GlobalSightLocale)targetLocales.elementAt(i);
	            	if (!locale.getDisplayName().equals(targetLocaleLong))
	                	out.println("<option value=\"" + locale.getId() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
	            	else
	            		out.println("<option value=\"" + locale.getId() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
	            }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_cvs_mm_target_module")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="targetModule" value="<%=targetModule %>" size="40"  editable="false" />&nbsp;&nbsp;
            <input type="button" name="targetSelect" value="<%=bundle.getString("lb_select") %>"
            onclick="moduleSelect('Target')">
          </td>
        </tr>
        <tr>
          <td class="standardText">
            &nbsp;
          </td>
          <td>
            <input type="checkbox" name="subfolder" value="1" <%=mm.getSubFolderMapped().equals("1")?"checked":"" %>><%=bundle.getString("msg_cvs_mm_auto_create_subfolder") %>
          </td>
        </tr>
      </table></td></tr>
      <tr><td>&nbsp;</td></tr>
      <tr><td>
      <% if (isEdit) { 
           %>
		    <span class="mainHeading">
		        <%=bundle.getString("msg_cvs_mm_renaming") %>
		    </span>
		<table cellpadding=0 cellspacing=0 border=0 class="standardText">
		  <tr valign="top">
		    <td align="right">
		      <amb:tableNav bean="<%=ModuleMappingConstants.MODULE_MAPPING_RENAME_LIST %>" key="<%=ModuleMappingConstants.MODULE_MAPPING_RENAME_KEY%>"
		       pageUrl="removeRename" />
		    </td>
		  </tr>
		  <tr>
		    <td>
	          <table cellspacing="0" cellpadding="6" border="0" class="listborder" width="100%">
  				<tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
		          <td>&nbsp;</td>
		          <td><%=bundle.getString("lb_cvs_mm_source_name")%></td>
		          <td>
        			<%=bundle.getString("lb_cvs_mm_target_name")%>
    			  </td>
		        </tr>
				  <%
				  int index = 0;
				  for (ModuleMappingRename rename : renames) {
					  if (index % 2 == 0)
						  out.println("<tr class=\"tableRowOdd\">");
					  else
						  out.println("<tr class=\"tableRowEven\">");
				  %>
		          <td class="standardText" valign=top align=left>
			        <input type="radio" name="radioBtn" value="<%=rename.getId() %>" onclick="enableButtons()">
          		  </td>
		          <td class="standardText" valign=top align=left><%=rename.getSourceName() %></td>
		          <td class="standardText" valign=top align=left><%=rename.getTargetName() %></td>
		        </tr>
				<%
				}
				%>
			</table>
			</td>
		  </tr>
		</table>
	  <% } %>
      </td></tr>
      <tr>
        <td>
         <% if (isEdit) { %>
		    <amb:permission name="<%=Permission.CVS_MODULE_MAPPING_EDIT%>" >
		      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
		       name="editBtn" disabled onclick="submitForm('Edit');">
		      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
		       name="removeBtn" disabled onclick="submitForm('Remove');">
		      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
		       name="newBtn" onclick="submitForm('New');">
		    </amb:permission>
		    <br/>
		    <br/>
		    <B><%=bundle.getString("msg_cvs_mm_edit_notes") %>
		    <br/>
		    </B>
            <br/>
            <br/>
		 <% } %>
		 <div align="right">
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
         </div>
        </td>
      </tr>
    </table>
</form>

