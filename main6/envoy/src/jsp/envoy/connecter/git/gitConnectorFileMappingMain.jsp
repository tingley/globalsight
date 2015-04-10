<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.cxe.entity.gitconnector.GitConnector,
      com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.util.comparator.GitConnectorFileMappingComparator, 
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
 <jsp:useBean id="back" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="gitConnectorFileMappingList" scope="request" class="java.util.ArrayList" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    GitConnector connector = (GitConnector) request.getAttribute("gitConnector");
    long gitConnectorId = connector.getId();

    // Lables
    String title= bundle.getString("lb_git_connector_file_mapping") + " (" + connector.getName() + ")";
    String helperText = bundle.getString("helper_text_git_connector_file_mapping");
    String confirmRemove = bundle.getString("msg_remove_git_connector_file_mapping");

    // URLs
    String newURL = new1.getPageURL() + "&action=new&gitConnectorId="+gitConnectorId;
    String editURL = modify.getPageURL() + "&action=edit&gitConnectorId="+gitConnectorId;
    String removeURL = remove.getPageURL() + "&action=remove&gitConnectorId="+gitConnectorId;
    String filterURL = self.getPageURL() + "&action=filter&gitConnectorId="+gitConnectorId;
    String backURL = back.getPageURL() + "&action=filter&gitConnectorId="+gitConnectorId;

    // Filters
    String sourceLocaleFilter = (String) request.getAttribute("sourceLocaleFilter");
    sourceLocaleFilter = sourceLocaleFilter == null ? "" : sourceLocaleFilter;
    String sourceMappingPathFilter = (String) request.getAttribute("sourceMappingPathFilter");
    sourceMappingPathFilter = sourceMappingPathFilter == null ? "" : sourceMappingPathFilter;
    String targetLocaleFilter = (String) request.getAttribute("targetLocaleFilter");
    targetLocaleFilter = targetLocaleFilter == null ? "" : targetLocaleFilter;
    String targetMappingPathFilter = (String) request.getAttribute("targetMappingPathFilter");
    targetMappingPathFilter = targetMappingPathFilter == null ? "" : targetMappingPathFilter;
    String companyNameFilter = (String) request.getAttribute("companyNameFilter");
    companyNameFilter = companyNameFilter == null ? "" : companyNameFilter;
    
    ArrayList<GitConnectorFileMapping> gitmsg = (ArrayList<GitConnectorFileMapping>)request.getAttribute("gitmsg");
%>
<HTML>
<!-- eloquaMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jQuery.md5.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var guideNode = "Git";
var needWarning = false;
var objectName = "";
var helpFile = "<%=bundle.getString("help_git_connector_file_mapping_main")%>";

function back()
{
	var url = "<%=backURL%>";
    $("#gitConnectorFileMappingForm").attr("action", url).submit();
}

function newMTConnector()
{
    window.location.href = "<%=newURL%>";
}

function removeMTConnctor()
{
	if (confirm('<%=confirmRemove%>'))
	{
		var selectedMtcId = findSelectedCheckboxes();
		var url = "<%=removeURL%>&gcfmIds=" + selectedMtcId;
	    $("#gitConnectorFileMappingForm").attr("action", url).submit();
	}
}

function setButtonState()
{
	var ids = findSelectedCheckboxes();
    if (ids == "")
    {
    	$("#removeBtn").attr("disabled", true);
    }
    else
    {
    	$("#removeBtn").attr("disabled", false);
    }
}

function findSelectedCheckboxes()
{
    var ids = "";
    $('input[type="checkbox"][name="gitConnectorFileMappingIds"]:checked').each(function (){
        ids += $(this).val() + ",";
    });
    if (ids != "")
      ids = ids.substring(0, ids.length - 1);
    return ids;
}

function handleSelectAll()
{
    if (gitConnectorFileMappingForm && gitConnectorFileMappingForm.selectAll) {
        if (gitConnectorFileMappingForm.selectAll.checked) {
            checkAllWithName('gitConnectorFileMappingForm', 'gitConnectorFileMappingIds');
            setButtonState();
        }
        else {
            clearAll('gitConnectorFileMappingForm');
            setButtonState();
        }
    }
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	gitConnectorFileMappingForm.action = "<%=filterURL%>";
    	gitConnectorFileMappingForm.submit();
    }
}
</SCRIPT>
</HEAD>
<BODY ID="idBody" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">

<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="gitConnectorFileMappingForm" id="gitConnectorFileMappingForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" >
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="gitConnectorFileMappingList" key="gitConnectorFileMappingKey" pageUrl="self" />
        </td>
    </tr>
    <tr>
        <td>
        <amb:table bean="gitConnectorFileMappingList" id="gitConnectorFileMapping"
        key="gitConnectorFileMappingKey"
        dataClass="com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping"
        pageUrl="self"
        hasFilter="true"
        emptyTableMsg="msg_no_git_connector_file_mapping">
            <amb:column label="checkbox" width="2%">
                <input type="checkbox" name="gitConnectorFileMappingIds" value="<%=gitConnectorFileMapping.getId()%>" onclick="setButtonState();">
            </amb:column>

            <amb:column label="lb_source_locale" sortBy="<%=GitConnectorFileMappingComparator.SOURCE_LOCALE%>" filter="sourceLocaleFilter" filterValue="<%=sourceLocaleFilter%>"  width="15%">
                <%String url = editURL + "&id=" + gitConnectorFileMapping.getId();%>
                <A name='nameLink' class='standardHREF' href="<%=url%>"><%=gitConnectorFileMapping.getSourceLocale()%></A>&nbsp;
            </amb:column>

            <amb:column label="lb_source_mapping_path" sortBy="<%=GitConnectorFileMappingComparator.SOURCE_MAPPING_PATH%>" filter="sourceMappingPathFilter" filterValue="<%=sourceMappingPathFilter%>" width="30%">
                <%=gitConnectorFileMapping.getSourceMappingPath()%>
            </amb:column>

            <amb:column label="lb_target_locale" sortBy="<%=GitConnectorFileMappingComparator.TARGET_LOCALE%>" filter="targetLocaleFilter" filterValue="<%=targetLocaleFilter%>"  width="15%">
                <%=gitConnectorFileMapping.getTargetLocale()%>
            </amb:column>

            <amb:column label="lb_target_mapping_path" sortBy="<%=GitConnectorFileMappingComparator.TARGET_MAPPING_PATH%>" filter="targetMappingPathFilter" filterValue="<%=targetMappingPathFilter%>" width="30%">
                <%=gitConnectorFileMapping.getTargetMappingPath()%>
            </amb:column>

            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=GitConnectorFileMappingComparator.COMPANY_NAME%>"  filter="companyNameFilter" filterValue="<%=companyNameFilter%>" width="10%">
                    <%=gitConnectorFileMapping.getCompanyName()%>
                </amb:column>
            <% } %>

        </amb:table>
        </td>
    </tr>
    <tr style="padding-top: 5px;">
        <td><amb:tableNav  bean="gitConnectorFileMappingList" key="gitConnectorFileMappingKey"
           pageUrl="self" scope="10,20,50,All" showTotalCount="false"/></td>
    </tr>
    <tr>
        <td style="padding-top:5px" align="left">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"  name="removeBtn" id="removeBtn" disabled onclick="removeMTConnctor();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."  name="newBtn" onclick="newMTConnector();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_back")%>"  name="newBtn" onclick="back();">
        </td>
    </tr>
</table>
</form>
</DIV>

</DIV>
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
if (gitmsg != null && gitmsg.size()>0) {
	StringBuilder sb = new StringBuilder("Following file mapping rules have already configured in the server.\\n");
	for (GitConnectorFileMapping m : gitmsg) {
		sb.append(replace(m.getSourceMappingPath()) + "[" + m.getSourceLocale() + "] <--> " + replace(m.getTargetMappingPath()) + "[" + m.getTargetLocale() + "] \\n");
	}
		%>
	    alert("<%=sb.toString()%>");		
		<%
}
%>
function changePageSize(value) 
{
	window.location='/globalsight/ControlServlet?linkName=self&pageName=GCFMM&gitConnectorKeyPageNum=1&gitConnectorKeySorting=0&numOfPageSize=' + value + '&gitConnectorId=' + <%=gitConnectorId%>
}

$('a').click(function(){
	var href = $(this).attr('href');
	if(href.indexOf("GCFMM") > 0)
	{
		var tempHref = href + "&action=filter&gitConnectorId=<%=gitConnectorId%>";
		
		if($("#sourceLocaleFilter").val() != "")
		{
			tempHref = tempHref + "&sourceLocaleFilter=" + $("#sourceLocaleFilter").val();
		}
		if($("#sourceMappingPathFilter").val() != "")
		{
			tempHref = tempHref + "&sourceMappingPathFilter=" + $("#sourceMappingPathFilter").val();
		}
		if($("#targetLocaleFilter").val() != "")
		{
			tempHref = tempHref + "&targetLocaleFilter=" + $("#targetLocaleFilter").val();
		}
		if($("#targetMappingPathFilter").val() != "")
		{
			tempHref = tempHref + "&targetMappingPathFilter=" + $("#targetMappingPathFilter").val();
		}
		<% if (isSuperAdmin) { %>
		if($("#companyNameFilter").val() != "")
		{
			tempHref = tempHref + "&companyNameFilter=" + $("#companyNameFilter").val();
		}
		<%}%>
		
    	$(this).attr('href',tempHref);
	}
});
</script>
</HTML>