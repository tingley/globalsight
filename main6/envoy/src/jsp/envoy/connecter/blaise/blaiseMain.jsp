<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.util.comparator.BlaiseConnectorComparator,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="connect" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

    // Lables
    String title= bundle.getString("lb_blaise_connector");
    String helperText = bundle.getString("helper_text_blaise_connector");
    String confirmRemove = bundle.getString("msg_remove_blaise_connector");

    // URLs
    String newURL = new1.getPageURL() + "&action=new";
    String editURL = edit.getPageURL() + "&action=edit";
    String removeURL = remove.getPageURL() + "&action=remove";
    String filterURL = self.getPageURL() + "&action=filter";
    String connectURL = connect.getPageURL() + "&action=connect";

    // Filters
    String nameFilter = (String) request.getAttribute("nameFilter");
    nameFilter = nameFilter == null ? "" : nameFilter;
    String urlFilter = (String) request.getAttribute("urlFilter");
    urlFilter = urlFilter == null ? "" : urlFilter;
    String usernameFilter = (String) request.getAttribute("usernameFilter");
    usernameFilter = usernameFilter == null ? "" : usernameFilter;
    String companyNameFilter = (String) request.getAttribute("companyNameFilter");
    companyNameFilter = companyNameFilter == null ? "" : companyNameFilter;
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jQuery.md5.js"></script>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language=JavaScript1.2 SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var guideNode = "Blaise";
var needWarning = false;
var objectName = "";
var helpFile = "<%=bundle.getString("help_blaise_connector_main")%>";

function connect()
{
	var selectedIds = findSelectedCheckboxes();
	var url = "<%=connectURL%>&blcId=" + selectedIds;
    $("#blaiseForm").attr("action", url).submit();
}

function newBlaiseConnector()
{
    window.location.href = "<%=newURL%>";
}

function removeBlaiseConnctor()
{
	if (confirm('<%=confirmRemove%>'))
	{
	    $("#blaiseForm").attr("action", "<%=removeURL%>").submit();
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
	}
	else
	{
        $("#connectBtn").attr("disabled", false);
	}
}

function findSelectedCheckboxes()
{
    var ids = "";
    $('input[type="checkbox"][name="blaiseConnectorIds"]:checked').each(function (){
        ids += $(this).val() + ",";
    });
    if (ids != "")
      ids = ids.substring(0, ids.length - 1);
    return ids;
}

function handleSelectAll()
{
    if (blaiseForm && blaiseForm.selectAll) {
        if (blaiseForm.selectAll.checked) {
            checkAllWithName('blaiseForm', 'blaiseConnectorIds');
            setButtonState();
        }
        else {
            clearAll('blaiseForm');
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
    	blaiseForm.action = "<%=filterURL%>";
    	blaiseForm.submit();
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
<form name="blaiseForm" id="blaiseForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;" >
    <tr valign="top">
        <td align="right">
            <amb:tableNav bean="blaiseConnectorList" key="blaiseConnectorKey" pageUrl="self" />
        </td>
    </tr>
    <tr>
        <td>
        <amb:table bean="blaiseConnectorList" id="blaiseConnector" key="blaiseConnectorKey" 
        	dataClass="com.globalsight.cxe.entity.blaise.BlaiseConnector" pageUrl="self" hasFilter="true" emptyTableMsg="msg_blaise_connector_none">

            <amb:column label="checkbox" width="2%">
                <input type="checkbox" name="blaiseConnectorIds" value="<%=blaiseConnector.getId()%>" onclick="setButtonState();">
            </amb:column>

            <amb:column label="lb_name" sortBy="<%=BlaiseConnectorComparator.NAME%>" filter="nameFilter" filterValue="<%=nameFilter%>" width="10%">
                <%String url = editURL + "&id=" + blaiseConnector.getId();%>
                <A name='nameLink' class='standardHREF' href="<%=url%>"><%=blaiseConnector.getName()%></A>&nbsp;
            </amb:column>

            <amb:column label="lb_description" sortBy="<%=BlaiseConnectorComparator.DESC%>" width="25%">
                <%=blaiseConnector.getDescription() == null ? "" : blaiseConnector.getDescription()%>
            </amb:column>

            <amb:column label="lb_url" sortBy="<%=BlaiseConnectorComparator.URL%>" filter="urlFilter" filterValue="<%=urlFilter%>" width="20%">
                <A class='standardHREF' target="_blank" href="<%=blaiseConnector.getUrl()%>"><%=blaiseConnector.getUrl()%></A>
            </amb:column>

            <amb:column label="lb_username" sortBy="<%=BlaiseConnectorComparator.USER_NAME%>" filter="usernameFilter" filterValue="<%=usernameFilter%>" width="40%">
                <%=blaiseConnector.getUsername()%>
            </amb:column>

            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=BlaiseConnectorComparator.COMPANY_NAME%>"  filter="companyNameFilter" filterValue="<%=companyNameFilter%>">
                    <%=blaiseConnector.getCompanyName()%>
                </amb:column>
            <% } %>

        </amb:table>
        </td>
    </tr>
    <tr style="padding-top: 5px;">
        <td><amb:tableNav  bean="blaiseConnectorList" key="blaiseConnectorKey" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/></td>
    </tr>
    <tr>
        <td style="padding-top:5px" align="left">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_connect")%>" name="connectBtn" id="connectBtn" disabled onclick="connect();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"  name="removeBtn" id="removeBtn" disabled onclick="removeBlaiseConnctor();">
            <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."  name="newBtn" onclick="newBlaiseConnector();">
        </td>
    </tr>
</table>
</form>
</DIV>

</DIV>
</BODY>
</HTML>