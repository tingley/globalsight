
<%@page import="com.globalsight.cxe.entity.fileextension.FileExtensionImpl"%>
<%@page import="com.globalsight.cxe.entity.fileprofile.FileProfileImpl"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants"%>
<%@page import="com.globalsight.cxe.entity.fileprofile.FileProfileExtension"%><%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.tags.TableConstants,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.cvsconfig.*,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="select" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

    String cancelUrl = cancel.getPageURL() + "&action=cancel";
    String selectURL = select.getPageURL() + "&action=projectSelect";
    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    TreeMap<String, ArrayList<FileProfileImpl>> feis = null;
    CVSFileProfile cvsfp = null;
    long projectId = 0L, moduleId = 0L;
    String sourceLocale = "";
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + CVSConfigConstants.UPDATE;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_cvs_file_profile");
        cvsfp = (CVSFileProfile)sessionMgr.getAttribute(CVSConfigConstants.CVS_FILE_PROFILE_KEY);
        projectId = cvsfp.getProject().getId();
        moduleId = cvsfp.getModule().getId();
        sourceLocale = cvsfp.getSourceLocale();
    }
    else
    {
        saveURL +=  "&action=" + CVSConfigConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvs_file_profile");
        String projectTmp = (String)sessionMgr.getAttribute("projectSelect");
        if (projectTmp != null && !projectTmp.trim().equals(""))
        	projectId = Long.parseLong(projectTmp);
        sessionMgr.setAttribute("projectSelect", null);
    }
    feis = (TreeMap<String, ArrayList<FileProfileImpl>>)sessionMgr.getAttribute("fileExtensions");

    String projectLabel = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_LABEL);
    String projectJsMsg = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_JS_MSG);
    // Button names
    String saveButton = bundle.getString("lb_save");
    String cancelButton = bundle.getString("lb_cancel");

    // get list of projects to be displayed
    List projectInfos = (List)sessionMgr.getAttribute("projectInfos");
    int numOfProjects = projectInfos == null ? -1 : projectInfos.size();
    
    ArrayList<CVSServer> servers = (ArrayList)sessionMgr.getAttribute("cvsservers");
    Vector<GlobalSightLocale> sourceLocales = (Vector<GlobalSightLocale>)sessionMgr.getAttribute("sourceLocales");
%>
<html>
<head>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var guideNode = "myJobs";
var objectName = "";
var helpFile = "#";

function submitForm(selectedButton)
{
   if (selectedButton == "cancel")
   {
       customerForm.action = "<%=cancelUrl%>";
   }
    if (selectedButton == "save")
    {
        if (!validateForm())
        {
            return;
        }
        customerForm.action = "<%=saveURL%>"
    }
    customerForm.submit();
}

function validateForm()
{
    if (customerForm.projects.selectedIndex == 0)
    {
        alert("<%=EditUtil.toJavascript(projectJsMsg)%>");
        return false;
    }
    if (customerForm.servers.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_cvs_fp_select_module")%>");
        return false;
    }
	if (customerForm.srcLocales.selectedIndex == 0)
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_customer_src_locale"))%>");
        return false;
    }
    <%
    if (!edit) {
    %>
    var count = customerForm.fpsize.value;
    var canRun = false;
    for (var i=0;i<count;i++) {
      if (canRun)
        break;
      if (document.getElementById("fp" + i).value != "-1") {
        canRun = true;
      } else {
        canRun = false;
      }
    }
    
    if (count == 0) {
        alert("<%=bundle.getString("jsmsg_cvs_profile_no_file_profile") %>");
        return false;
    }
    if (!canRun) {
        alert("<%=bundle.getString("jsmsg_cvs_profile_select_file_profile") %>");
        return false;
    }
    <%
    }
    %>

    return true;
}

function doOnload()
{
    loadGuides();
}

function projectSelect() {
	if (customerForm.projects.selectedIndex >= 0) {
		customerForm.action = "<%=selectURL%>";
		customerForm.submit();
	}
}
</SCRIPT>
</head>
<!-- This DIV is for used for the default selection of locales -->
<DIV id="idPreferences" STYLE="behavior:url(#default#userData); display: none;" class="preferences"></DIV>

<body LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="doOnload();" >
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<div ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span CLASS="mainHeading">
    <%=title%>
    </span>

<p>
<form name="customerForm" method="post" >
<table cellspacing="0" cellpadding="4" border="0" class="standardText">
  <tr>
      <td class="standardText">
        <%=projectLabel%><span class="asterisk">*</span>:
      </td>
      <td class="standardText">
        <select name="projects" onchange="projectSelect()" <%= edit ? "disabled" : "" %>>
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i =0; i < numOfProjects; i++)
            {
                ProjectInfo p = (ProjectInfo)projectInfos.get(i);
                long id = p.getProjectId();
                String name = p.getName();
                if (projectId == id)
                    out.println("<option value='" + id + "' selected>" + name + "</option>");
                else
                	out.println("<option value='" + id + "'>" + name + "</option>");
            }
%>
        </select>
      </td>
    </tr>
  <tr>
      <td class="standardText">
        <%=bundle.getString("lb_cvs_module")%><span class="asterisk">*</span>:
      </td>
      <td class="standardText">
        <select name="servers">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            Set<CVSModule> modules = null;
            for (CVSServer server : servers)
            {
                if (!server.isActive())
                    continue;
                  modules = server.getModuleSet();
                  for (CVSModule module : modules) {
                      if (!module.isActive())
                          continue;
                      if (module.getId() == moduleId)
                    	  out.println("<option value=\"" + module.getId() + "\" selected>" + server.getName()+"-"+server.getRepository()+"-"+module.getName() + "</option>");
                      else
                    	  out.println("<option value=\"" + module.getId() + "\">" + server.getName()+"-"+server.getRepository()+"-"+module.getName() + "</option>");
                  }
            }
%>
        </select>
      </td>
    </tr>
  <tr>
      <td class="standardText">
        <%=bundle.getString("lb_source_locale")%><span class="asterisk">*</span>:
      </td>
      <td class="standardText">
        <select name="srcLocales">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (GlobalSightLocale locale : sourceLocales) {
            	if (locale.toString().equals(sourceLocale))
            	    out.println("<option value='" + locale.toString() + "' selected>" + locale.getDisplayName(uiLocale) + "</option>");
            	else
            		out.println("<option value='" + locale.toString() + "'>" + locale.getDisplayName(uiLocale) + "</option>");
            }
%>
        </select>
      </td>
    </tr>
  <%
  ArrayList<FileProfileImpl> values = null;
  int index = 0;

  if (edit) {
	  String fileExt = cvsfp.getFileExt();
  %>
      <tr>
      <td class="standardText">
        <%=bundle.getString("lb_file_extension") %>:
      </td>
      <td class="standardText">
        <input type="text" name="labelext" value="<%=fileExt %>" size="20" disabled/>&nbsp;&nbsp;
        <input type="hidden" name="fileext" value="<%=fileExt %>"/>
        <%=bundle.getString("lb_default_file_profile") %>:
        <select name="fp">
          <option value="-1"><%=bundle.getString("lb_choose")%></option>
          <%
          values = feis.get(fileExt);
          for (FileProfileImpl f : values) {
        	  if (f.getId() == cvsfp.getFileProfile().getId())
        		  out.println("<option value='" + f.getId() + "' selected>" + f.getName() + "</option>");
        	  else
        		  out.println("<option value='" + f.getId() + "'>" + f.getName() + "</option>");
          }
          %>
        </select> 
      </td>
    </tr>
  <input type="hidden" name="fpsize" value="-1"/>
  <%
  } else {
	  String tmp = "";
	  
	  if (feis != null && feis.size()>0) {
	      Iterator keys = feis.keySet().iterator();
	      String key = "";
	      
	      while (keys.hasNext()) {
	          key = (String)keys.next();
	          %>
	    <tr>
	      <td class="standardText">
	        <%=bundle.getString("lb_file_extension") %>:
	      </td>
	      <td class="standardText">
	        <input type="text" name="labelext<%=index %>" value="<%=key %>" size="20" disabled/>&nbsp;&nbsp;
	        <input type="hidden" name="fileext<%=index %>" value="<%=key %>"/>
	        <%=bundle.getString("lb_default_file_profile") %>:
	        <select id="fp<%=index %>" name="fp<%=index %>">
	          <option value="-1"><%=bundle.getString("lb_choose")%></option>
	          <%
	          values = feis.get(key);
	          for (FileProfileImpl f : values) {
	        	  out.println("<option value='" + f.getId() + "'>" + f.getName() + "</option>");
	          }
	          %>
	        </select> 
	      </td>
	    </tr>
			  <%
			       index++;
			  }
	  }
	  %>
      <input type="hidden" name="fpsize" value="<%=index %>"/>
	  <%
  }
  %>
  <tr>
    <td style="padding-top:5px">
        <input type=button name="cancelBtn" value="<%=cancelButton%>"
         onclick="submitForm('cancel')">
        <input type=button name="saveBtn" value="<%=saveButton%>"
         onclick="submitForm('save')">
    </td>
  </tr>
</table>
</form>
</div>
</body>
</html>
