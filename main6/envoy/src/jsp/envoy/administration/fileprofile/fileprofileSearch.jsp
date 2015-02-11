<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,
                  com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,                 
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.util.List,
                  com.globalsight.everest.localemgr.CodeSet,
                  com.globalsight.cxe.entity.knownformattype.KnownFormatType,
                  com.globalsight.everest.webapp.pagehandler.administration.fileprofile.FileProfileConstants,
                  com.globalsight.util.collections.HashtableValueOrderWalker,
                  com.globalsight.cxe.entity.fileprofile.FileProfile"
                  session="true"
%>
<jsp:useBean id="fileprofilesSearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Labels, etc
    String title= bundle.getString("lb_file_profiles") + " - " + bundle.getString("lb_search");

    String searchButton = bundle.getString("lb_search");
    String lbsearch = bundle.getString("lb_search");
    String lbcancel = bundle.getString("lb_cancel");

    String searchUrl = search.getPageURL() + "&action=" + FileProfileConstants.ADV_SEARCH_ACTION;
    String cancelUrl = cancel.getPageURL() + "&action=" + FileProfileConstants.CANCEL_ACTION;
    String action = FileProfileConstants.ACTION;
 

    // Data                                                      
    
    HashtableValueOrderWalker locProfiles =
      (HashtableValueOrderWalker)request.getAttribute("locProfiles");
    ArrayList names = (ArrayList) request.getAttribute("names");
    Collection formatTypes = (Collection) request.getAttribute("formatTypes");
    FileProfile fp = (FileProfile) sessionMgr.getAttribute("fileprofile");
    String fpName = "";
    String desc = "";
    Long lpId = new Long(-1);
    long formatId = -1;
    Hashtable extensionHash = new Hashtable();
    boolean export = false;
     if (fp != null)
     {
        fpName = fp.getName();
        desc = fp.getDescription();
        if (desc == null) desc = "";
        lpId = new Long(fp.getL10nProfileId());
        formatId = fp.getKnownFormatTypeId();
     }   
%>
<HTML>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<script>
var needWarning = false;
var objectName = "<%= bundle.getString("lb_file_profile") %>";
var guideNode = "fileProfiles";
var helpFile = "<%=bundle.getString("help_file_profiles_basic_info")%>";


function submitForm(formAction)
{
    if (formAction == "search")
        searchForm.action = "<%=searchUrl%>";
    else
        searchForm.action = "<%=cancelUrl%>";
    searchForm.submit();
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
     <%
         String noresults = (String)request.getAttribute("noresults");
         if (noresults != null)
         {
            out.println("<div style='color:red'>");
            String searchType = request.getParameter("searchType");
            out.println(noresults);
            out.println("</div>");
         }
     %>
    
<form name="searchForm" method="post" action="">
<input type="hidden" name="extensions" value="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_file_profiles_name")%>:
          </td>
          <td class="standardText">
            <select name="nameOptions">
                <option value='<%=SearchCriteriaParameters.BEGINS_WITH%>'><%= bundle.getString("lb_begins_with") %></option>
                <option value='<%=SearchCriteriaParameters.ENDS_WITH%>'><%= bundle.getString("lb_ends_with") %></option>
                <option value='<%=SearchCriteriaParameters.CONTAINS%>'><%= bundle.getString("lb_contains") %></option>
            </select>
            <input type="text" size="30" name="nameField">
            </td>
            </tr>

        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_loc_profiles")%>:
          </td>
          <td>
            <select name="locprofiles">
              <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (int i = 0; i < locProfiles.size(); i++)
            {
                Long num = (Long)locProfiles.getKey(i);
                String value = (String)locProfiles.getValue(i);
                if (lpId.equals(num))
                    out.println("<option value=" + num + " selected>" + value + "</option>");
                else
                    out.println("<option value=" + num + ">" + value + "</option>");
            }
%>
            </select>
          </td>
        </tr>

        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_file_format")%>:
          </td>
          <td>
            <select name="srcFormat">
              <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            for (Iterator it = formatTypes.iterator(); it.hasNext();)
            {
                KnownFormatType type = (KnownFormatType)it.next();
                if (type.getId() == formatId)
		{
                    out.println("<option value='" + type.getId() + "' selected>" + type.getName() + "</option>");
		}
                else
		{
                    out.println("<option value='" + type.getId() + "'>" + type.getName() + "</option>");
		}
            }
%>
            </select>
          </td>
        </tr>
      <tr><td>&nbsp;</td></tr>
      <tr>
          <td>
            <input type="button" name="search" value="<%=lbsearch%>"
            onclick="submitForm('search')"/>
            <input type="button" name="cancel" value="<%=lbcancel%>"
            onclick="submitForm('cancel')"/>
          </td>
        </tr>

</table>

</form>
</DIV>
</BODY>
</HTML>
