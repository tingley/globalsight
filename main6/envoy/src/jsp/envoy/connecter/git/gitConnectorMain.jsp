<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.cxe.entity.gitconnector.GitConnector,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.util.comparator.GitConnectorComparator, 
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
<jsp:useBean id="connect" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="fileMapping" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="gitConnectorList" scope="request" class="java.util.ArrayList" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

    // Lables
    String title= bundle.getString("lb_git_connector");
    String helperText = bundle.getString("helper_text_git_connector");
    String confirmRemove = bundle.getString("msg_remove_git_connector");

    // URLs
    String newURL = new1.getPageURL() + "&action=new";
    String editURL = modify.getPageURL() + "&action=edit";
    String removeURL = remove.getPageURL() + "&action=remove";
    String filterURL = self.getPageURL() + "&action=filter";
    String testURL = self.getPageURL() + "&action=test";
    String connectURL = connect.getPageURL() + "&action=connect";
    String fileMappingURL = fileMapping.getPageURL();

    // Filters
    String nameFilter = (String) request.getAttribute("nameFilter");
    nameFilter = nameFilter == null ? "" : nameFilter;
    String urlFilter = (String) request.getAttribute("urlFilter");
    urlFilter = urlFilter == null ? "" : urlFilter;
    String branchFilter = (String) request.getAttribute("branchFilter");
    branchFilter = branchFilter == null ? "" : branchFilter;
    String usernameFilter = (String) request.getAttribute("usernameFilter");
    usernameFilter = usernameFilter == null ? "" : usernameFilter;
    String emailFilter = (String) request.getAttribute("emailFilter");
    emailFilter = emailFilter == null?"" : emailFilter;
    String companyNameFilter = (String) request.getAttribute("companyNameFilter");
    companyNameFilter = companyNameFilter == null ? "" : companyNameFilter;
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
var guideNode = "GitConnector";
var needWarning = false;
var objectName = "";
var helpFile = "<%=bundle.getString("help_git_connector_main")%>";

function connect()
{
	var selectedMtcId = findSelectedCheckboxes();
	var url = "<%=connectURL%>&gcId=" + selectedMtcId;
    $("#gitForm").attr("action", url).submit();
}

function fileMapping()
{
	var selectedMtcId = findSelectedCheckboxes();
	var url = "<%=fileMappingURL%>&gitConnectorId=" + selectedMtcId;
    $("#gitForm").attr("action", url).submit();
}

function newMTConnector()
{
    window.location.href = "<%=newURL%>";
}

function removeMTConnctor()
{
	if (confirm('<%=confirmRemove%>'))
	{
	    $("#gitForm").attr("action", "<%=removeURL%>").submit();
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

    var idsArr = ids.split(",");
	if (ids == "" || idsArr.length > 1)
	{
	    $("#connectBtn").attr("disabled", true);
	    $("#fileMappingBtn").attr("disabled", true);
	}
	else
	{
        $("#connectBtn").attr("disabled", false);
        $("#fileMappingBtn").attr("disabled", false);
	}
}

function findSelectedCheckboxes()
{
    var ids = "";
    $('input[type="checkbox"][name="gitConnectorIds"]:checked').each(function (){
        ids += $(this).val() + ",";
    });
    if (ids != "")
      ids = ids.substring(0, ids.length - 1);
    return ids;
}

function handleSelectAll()
{
    if (gitForm && gitForm.selectAll) {
        if (gitForm.selectAll.checked) {
            checkAllWithName('gitForm', 'gitConnectorIds');
            setButtonState();
        }
        else {
            clearAll('gitForm');
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
    	gitForm.action = "<%=filterURL%>";
    	gitForm.submit();
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
<form name="gitForm" id="gitForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" >
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="gitConnectorList" key="gitConnectorKey" pageUrl="self" />
        </td>
    </tr>
    <tr>
        <td>
        <amb:table bean="gitConnectorList" id="gitConnector"
        key="gitConnectorKey"
        dataClass="com.globalsight.cxe.entity.gitconnector.GitConnector"
        pageUrl="self"
        hasFilter="true"
        emptyTableMsg="msg_no_git_connector">
            <amb:column label="checkbox" width="2%">
                <input type="checkbox" name="gitConnectorIds" value="<%=gitConnector.getId()%>" onclick="setButtonState();">
            </amb:column>

            <amb:column label="lb_name" sortBy="<%=GitConnectorComparator.NAME%>" filter="nameFilter" filterValue="<%=nameFilter%>"  width="10%">
                <%String url = editURL + "&id=" + gitConnector.getId();%>
                <A name='nameLink' class='standardHREF' href="<%=url%>"><%=gitConnector.getName()%></A>&nbsp;
            </amb:column>

            <amb:column label="lb_description" sortBy="<%=GitConnectorComparator.DESC%>" width="20%">
                <%=gitConnector.getDescription() == null ? "" : gitConnector.getDescription()%>
            </amb:column>

            <amb:column label="lb_git_repository_url" sortBy="<%=GitConnectorComparator.URL%>" filter="urlFilter" filterValue="<%=urlFilter%>" width="30%">
                <%=gitConnector.getUrl()%>
            </amb:column>
            
            <amb:column label="lb_branch" sortBy="<%=GitConnectorComparator.BRANCH%>" filter="branchFilter" filterValue="<%=branchFilter%>">
                <%=gitConnector.getBranch()%>
            </amb:column>

            <amb:column label="lb_username" sortBy="<%=GitConnectorComparator.USER_NAME%>" filter="usernameFilter" filterValue="<%=usernameFilter%>" >
                <%=gitConnector.getUsername()%>
            </amb:column>
            
            <amb:column label="lb_email" sortBy="<%=GitConnectorComparator.EMAIL%>" filter="emailFilter" filterValue="<%=emailFilter%>" >
                <%=gitConnector.getEmail()%>
            </amb:column>
            

            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=GitConnectorComparator.COMPANY_NAME%>"  filter="companyNameFilter" filterValue="<%=companyNameFilter%>">
                    <%=gitConnector.getCompanyName()%>
                </amb:column>
            <% } %>

        </amb:table>
        </td>
    </tr>
    <tr style="padding-top: 5px;">
        <td><amb:tableNav  bean="gitConnectorList" key="gitConnectorKey"
           pageUrl="self" scope="10,20,50,All" showTotalCount="false"/></td>
    </tr>
    <tr>
        <td style="padding-top:5px" align="left">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_connect")%>" name="connectBtn" id="connectBtn" disabled onclick="connect();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"  name="removeBtn" id="removeBtn" disabled onclick="removeMTConnctor();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_file_mapping")%>..." name="fileMappingBtn" id="fileMappingBtn" disabled onclick="fileMapping();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."  name="newBtn" onclick="newMTConnector();">
        </td>
    </tr>
</table>
</form>
</DIV>

</DIV>
</BODY>
</HTML>