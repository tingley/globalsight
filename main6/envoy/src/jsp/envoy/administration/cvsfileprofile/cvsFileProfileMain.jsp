
<%@page import="com.globalsight.util.GlobalSightLocale"%>
<%@page import="com.globalsight.everest.projecthandler.ProjectInfo"%><%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.cvsconfig.*,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,  
      com.globalsight.everest.util.comparator.CVSModuleComparator, 
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="update" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<jsp:useBean id="cvsfileprofiles" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    
    //cvsfileprofiles = (ArrayList<CVSFileProfile>)sessionMgr.getAttribute(CVSConfigConstants.CVS_FILE_PROFILE_LIST);

    String newURL = new1.getPageURL() + "&action=" + CVSConfigConstants.CREATE;
    String modifyURL = update.getPageURL() + "&action=" + CVSConfigConstants.UPDATE;
    String removeURL = remove.getPageURL() + "&action=" + CVSConfigConstants.REMOVE;
    String searchURL = search.getPageURL() + "&action=search";
    String title = bundle.getString("lb_cvs_file_profiles");
    String helperText= "";
    
    // get list of projects to be displayed
    List projectInfos = (List)sessionMgr.getAttribute("projectInfos");
    int numOfProjects = projectInfos == null ? -1 : projectInfos.size();
    
    ArrayList<CVSServer> servers = (ArrayList)sessionMgr.getAttribute("cvsservers");
    Vector<GlobalSightLocale> sourceLocales = (Vector<GlobalSightLocale>)sessionMgr.getAttribute("sourceLocales");
    HashMap<String, String> searchParams = (HashMap<String, String>)sessionMgr.getAttribute("searchParams");
    String s_project = "", s_module = "", s_sourceLocale = "", s_fileExt = "";
    if (searchParams != null) {
    	s_project = searchParams.get("project");
    	s_module = searchParams.get("module");
    	s_sourceLocale = searchParams.get("sourceLocale");
    	s_fileExt = searchParams.get("fileExt");
    }
%>

<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "cvsserver";
var helpFile = "#";

function submitForm(button)
{
    if (button == "New")
    {
    	currentForm.action = "<%=newURL%>";
    } else if (button == "Search") {
        currentForm.action = "<%=searchURL%>";
    }
    else 
    {
        value = getRadioValue(currentForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
          
        if (button == "Edit")
        {
        	currentForm.action = "<%=modifyURL%>" + "&id=" + value;
        } else if (button=="Remove") {
            if (confirm("<%=bundle.getString("jsmsg_cvs_remove_fileprofile")%>"))
            	currentForm.action = "<%=removeURL%>" + "&id=" + value;
            else
                return;
        }
    } 
    
    currentForm.submit();
    return;
}

function enableButtons()
{
    if (currentForm.removeBtn)
    	currentForm.removeBtn.disabled = false;
    if (currentForm.editBtn)
    	currentForm.editBtn.disabled = false;
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
    <div align="left" width="60%">
    <li class="standardText"><%=bundle.getString("msg_cvs_profile_help_1") %></li>
    <li class="standardText"><%=bundle.getString("msg_cvs_profile_help_2") %></li>
    <br/>
    </B>
    </div>
    
<form name="currentForm" method="post">
<table>
    <tr>
      <td class="standardText">
        <%=bundle.getString("lb_project") %>:
      </td>
      <td class="standardText">
        <select name="s_project">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i =0; i < numOfProjects; i++)
            {
                ProjectInfo p = (ProjectInfo)projectInfos.get(i);
                long id = p.getProjectId();
                String name = p.getName();
                if (s_project.equals(String.valueOf(id)))
                    out.println("<option value='" + id + "' selected>" + name + "</option>");
                else
                    out.println("<option value='" + id + "'>" + name + "</option>");
            }
%>
        </select>
      </td>
      <td class="standardText">
        <%=bundle.getString("lb_cvs_module")%>:
      </td>
      <td class="standardText">
        <select name="s_module">
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
                      if (s_module.equals(String.valueOf(module.getId())))
                          out.println("<option value=\"" + module.getId() + "\" selected>" + server.getName()+"-"+server.getRepository()+"-"+module.getName() + "</option>");
                      else
                          out.println("<option value=\"" + module.getId() + "\">" + server.getName()+"-"+server.getRepository()+"-"+module.getName() + "</option>");
                  }
            }
%>
        </select>
      </td>
         <td align="right" class=standardText><%=bundle.getString("lb_cvs_mm_source_locale") %>:</td>
        <td align="left">
            <select name="s_sourceLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
                for (int i = 0; i < sourceLocales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)sourceLocales.elementAt(i);
                    
                    if (!locale.toString().equals(s_sourceLocale))
                        out.println("<option value=\"" + locale.toString() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
                    else
                        out.println("<option value=\"" + locale.toString() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
        </td>
        <td align="right" class=standardText><%=bundle.getString("lb_file_extension") %>:</td>
        <td align="left"><input type="text" name="s_fileExt" size="20" value="<%=s_fileExt == null ? "" : s_fileExt %>"/></td>
        <td align="center"><input type="button" name="searchBtn" value="<%=bundle.getString("lb_search") %>" onclick="submitForm('Search')" /></td>
    </tr>
</table>
<br/>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="cvsfileprofiles" key="cvsfileprofile"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="cvsfileprofiles" id="cvsfileprofile"
       key="cvsfileprofile"
       dataClass="com.globalsight.everest.cvsconfig.CVSFileProfile" pageUrl="self"
       emptyTableMsg="msg_no_cvs_file_profile" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=cvsfileprofile.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_project" sortBy="<%=CVSFileProfileComparator.PROJECT%>" width="150px">
        <%= cvsfileprofile.getProject().getName() %>
      </amb:column>
      <amb:column label="lb_cvs_module" sortBy="<%=CVSFileProfileComparator.CVS_MODULE%>" width="100px">
        <%=cvsfileprofile.getModule().getName() %>
      </amb:column>
      <amb:column label="lb_file_extension" sortBy="<%=CVSFileProfileComparator.FILE_EXT%>" width="100px">
        <%=cvsfileprofile.getFileExt() %>
      </amb:column>
      <amb:column label="lb_file_profile" sortBy="<%=CVSFileProfileComparator.FILE_PROFILE%>" width="100px">
        <%=cvsfileprofile.getFileProfile().getName() %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.CVS_FILE_PROFILES_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_FILE_PROFILES_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
       name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_FILE_PROFILES_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
<br/>
<div align="left">

</div>
</BODY>

</html>

