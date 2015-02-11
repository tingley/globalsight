
<%@page import="com.globalsight.util.GlobalSightLocale"%><%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping.ModuleMappingConstants,
      com.globalsight.everest.cvsconfig.modulemapping.*,
      java.util.ArrayList,
      java.util.ResourceBundle"
    session="true"
%>

<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<!-- Set data of table -->
<jsp:useBean id="moduleMappings" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    
    moduleMappings = (ArrayList)sessionMgr.getAttribute(ModuleMappingConstants.MODULE_MAPPING_LIST);
    String newURL = new1.getPageURL() + "&action=" + ModuleMappingConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + ModuleMappingConstants.UPDATE;
    String removeURL = remove.getPageURL() + "&action=" + ModuleMappingConstants.REMOVE;
    String searchURL = search.getPageURL() + "&action=" + ModuleMappingConstants.SEARCH;
    //Set title of this web page
    String title = bundle.getString("lb_cvs_module_mappings");
    String helperText= bundle.getString("helper_text_cvsmodulemapping");
    
    //Get source and target locales
    Vector sourceLocales = (Vector)request.getAttribute(ModuleMappingConstants.SOURCE_LOCALE_PAIRS);
    Vector targetLocales = (Vector)request.getAttribute(ModuleMappingConstants.TARGET_LOCALE_PAIRS);

    HashMap<String, String> params = (HashMap<String, String>)session.getAttribute("mmSearchParam");
    
    ArrayList<ModuleMapping> cvsmsg = (ArrayList<ModuleMapping>)sessionMgr.getAttribute("cvsmsg");
    sessionMgr.setAttribute("cvsmsg", null);
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
var guideNode = "moduleMapping";
var helpFile = "#";

function submitForm(button)
{
    if (button == "New")
    {
        currentForm.action = "<%=newURL%>";
    } else if (button == "Search") {
        currentForm.action = "<%=searchURL%>";
    } else 
    {
        value = getRadioValue(currentForm.radioBtn);
        if (value == null) {
            alert("<%=bundle.getString("jsmsg_need_selected")%>");
            return;
        }
        
        if (button == "Edit")
        {
        	currentForm.action = "<%=modifyURL%>" + "&id=" + value;
        } else {
        	if (confirm("<%=bundle.getString("jsmsg_cvs_remove_modulemapping")%>"))
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

<form name="currentForm" method="post">
<table>
    <tr>
        <td align="right" class=standardText><b><%=bundle.getString("lb_cvs_mm_source_locale") %></b></td>
        <td align="left">
            <select name="s_srcLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
                for (int i = 0; i < sourceLocales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)sourceLocales.elementAt(i);
                    
                    if (!locale.getDisplayName().equals(params.get("sourceLocale")))
                        out.println("<option value=\"" + locale.toString() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
                    else
                        out.println("<option value=\"" + locale.toString() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
        </td>
        <td align="right" class=standardText><b><%=bundle.getString("lb_cvs_mm_target_locale") %></b></td>
        <td align="left">
            <select name="s_tarLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
                for (int i = 0; i < targetLocales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)targetLocales.elementAt(i);
                    
                    if (!locale.getDisplayName().equals(params.get("targetLocale")))
                        out.println("<option value=\"" + locale.toString() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
                    else
                        out.println("<option value=\"" + locale.toString() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
        </td>
        <td align="right" class=standardText><b><%=bundle.getString("lb_cvs_module_name") %></b></td>
        <td align="left"><input type="text" name="s_moduleName" size="20" value="<%=params.get("moduleName") == null ? "" : params.get("moduleName") %>"/></td>
        <td align="center"><input type="button" name="searchBtn" value="<%=bundle.getString("lb_search") %>" onclick="submitForm('Search')" /></td>
    </tr>
</table>
<br/>
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="<%=ModuleMappingConstants.MODULE_MAPPING_LIST %>" key="<%=ModuleMappingConstants.MODULE_MAPPING_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="<%=ModuleMappingConstants.MODULE_MAPPING_LIST %>" id="moduleMapping"
       key="<%=ModuleMappingConstants.MODULE_MAPPING_KEY%>"
       dataClass="com.globalsight.everest.cvsconfig.modulemapping.ModuleMapping" pageUrl="self"
       emptyTableMsg="msg_no_cvsmodulemapping" >
      <amb:column label="">
      <input type="radio" name="radioBtn" value="<%=moduleMapping.getId()%>"
       onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_cvs_mm_source_locale" sortBy="<%=ModuleMappingComparator.SOURCE_LOCALE%>"
       width="150px">
      <%= moduleMapping.getSourceLocale() %>
      </amb:column>
      <amb:column label="lb_cvs_mm_source_module" width="250px">
      <%= moduleMapping.getSourceModule() %>
      </amb:column>
      <amb:column label="lb_cvs_mm_target_locale" sortBy="<%=ModuleMappingComparator.TARGET_LOCALE%>"
       width="150px">
      <%= moduleMapping.getTargetLocale() %>
      </amb:column>
      <amb:column label="lb_cvs_mm_target_module" width="250px">
      <%= moduleMapping.getTargetModule() %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.CVS_MODULE_MAPPING_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
       name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_MODULE_MAPPING_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>..."
       name="removeBtn" onclick="submitForm('Remove');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.CVS_MODULE_MAPPING_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>

<%!
private String replace(String str) {
	if (str == null || str.trim().equals(""))
		return "";
	return str.replace("\\", "\\\\");
}
%>
<script language="javascript">
<%
if (cvsmsg != null && cvsmsg.size()>0) {
	StringBuilder sb = new StringBuilder("These module mapping have existed in the server.\\n");
	for (ModuleMapping m : cvsmsg) {
		sb.append(replace(m.getSourceModule()) + "[" + m.getSourceLocale() + "] <--> " + replace(m.getTargetModule()) + "[" + m.getTargetLocale() + "] \\n");
	}
		%>
	    alert("<%=sb.toString()%>");		
		<%
}
%>
</script>





