<%@page import="com.globalsight.everest.webapp.webnavigation.LinkHelper"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
	import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.servlet.util.SessionManager,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.cxe.entity.eloqua.EloquaConnector,
            com.globalsight.everest.util.comparator.PerplexityComparator,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.company.CompanyWrapper,java.util.*"
	session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>

<jsp:useBean id="new1" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

	String newURL = new1.getPageURL() + "&action=new";
	String editURL = modify.getPageURL() + "&action=edit";
	String remURL = remove.getPageURL() + "&action=remove";
	String selfUrl = self.getPageURL();
	String filterURL = self.getPageURL() + "&action=filter";
	String title = bundle.getString("lb_remote_service");
	String helperText = bundle.getString("lb_perplexity_service_help");
	String confirmRemove = bundle.getString("msg_remove_perplexity_service");
	String invalid = (String) request.getAttribute("invalid");
	boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

	String nameFilter = (String) request.getAttribute("nameFilter");
	String companyFilter = (String) request.getAttribute("companyFilter");
	String companyNameFilter = (String) request.getAttribute("companyNameFilter");
	String urlFilter = (String) request.getAttribute("urlFilter");
	String descriptionFilter = (String) request.getAttribute("descriptionFilter");

	nameFilter = nameFilter == null ? "" : nameFilter;
	companyFilter = companyFilter == null ? "" : companyFilter;
	companyNameFilter = companyNameFilter == null ? "" : companyNameFilter;
	urlFilter = urlFilter == null ? "" : urlFilter;
	descriptionFilter = descriptionFilter == null ? "" : descriptionFilter;
	PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
%>
<HTML>
<!-- perplexityMain.jsp -->
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
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT>
var guideNode = "mtProfiles";
var needWarning = false;
var objectName = "";
var helpFile = "<%=bundle.getString("help_perplexity_main")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        form.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(form.selectEloquaConnectorIds);

        if (button == "Edit")
        {
            form.action = "<%=editURL%>" + "&id=" + value;
        }
        if (button == "Connect")
        {
        	testConntect();
            return;
        }
        else if (button == "Remove")
        {
        	var referencedNames = "";
            var ids = document.getElementsByName("selectEloquaConnectorIds");
            isOk = confirm('<%=confirmRemove%>');        
            form.action = "<%=remURL%>";
        }
    }

    if (isOk)
    {
        form.submit();
    }
}

function enableButtons()
{
    if (form.editBtn)
        form.editBtn.disabled = false;
    if (form.dupBtn)
        form.dupBtn.disabled = false;
    if (form.remBtn)
        form.remBtn.disabled = false;    
}


function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = form.selectEloquaConnectorIds;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                if (checkbox.checked) 
                {
                    selectedIndex.push(i);
                }
            }
        } 
        else 
        {
            if (boxes.checked) 
            {
                selectedIndex.push(0);
            }
        }
    }

    if (selectedIndex.length != 1)
    {
        form.editBtn.disabled = true;
        form.connectBtn.disabled = true;        
    }
    else
    {
        form.editBtn.disabled = false;
        form.connectBtn.disabled = false;
    }

    if (selectedIndex.length > 0)
    {
        form.remBtn.disabled = false;
    }
    else
    {
        form.remBtn.disabled = true;
    }
}

function handleSelectAll() {
	if (form && form.selectAll) {
		if (form.selectAll.checked) {
			checkAllWithName('form', 'selectEloquaConnectorIds'); 
			setButtonState();
	    }
	    else {
			clearAll('form'); 
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
    	form.action = "<%=filterURL%>";
			form.submit();
		}
	}
</SCRIPT>
<style type="text/css">
@import url(/globalsight/includes/attribute.css);
</style>
<%@ include file="/envoy/common/shortcutIcon.jspIncl"%>
</HEAD>
<BODY ID="idBody" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">

	<%@ include file="/envoy/common/header.jspIncl"%>
	<%@ include file="/envoy/common/navigation.jspIncl"%>
	<%@ include file="/envoy/wizards/guides.jspIncl"%>
	<div id="idDiv" style="POSITION: ABSOLUTE; height: 100%; width: 100%; TOP: 0px; LEFT: 0px; RIGHT: 0px;">

		<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

			<amb:header title="<%=title%>" helperText="<%=helperText%>" />
			<span class=errorMsg>
				<%if (invalid != null && invalid.length() > 0)%>
			</span>
			
			<div style="width: 860px; border-bottom: 1px groove #0C1476; padding-top: 10px">
                <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                        <%if (userPermissions.getPermissionFor(Permission.MTP_VIEW)){%>
                        <td class="tableHeadingListOff">
                            <img src="/globalsight/images/tab_left_gray.gif" border="0" /> 
                            <a class="sortHREFWhite" href="<%=mtProfilesUrl%>">
                                <%=bundle.getString("lb_mt_profiles") %>
                            </a> 
                            <img src="/globalsight/images/tab_right_gray.gif" border="0" />
                        </td>
                        <td width="2"></td>
                        <%} %>
                        <td class="tableHeadingListOn">
                            <img src="/globalsight/images/tab_left_blue.gif" border="0" /> 
                            <a class="sortHREFWhite" href="<%=perplexityServiceUrl%>"> 
                                <%=bundle.getString("lb_perplexity_services") %>
                            </a>
                            <img src="/globalsight/images/tab_right_blue.gif" border="0" />
                        </td>
                    </tr>
                </table>
            </div>

			<form name="form" method="post">
				<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="margin-top: 10px;">
					<tr valign="top">
						<td align="right"><amb:tableNav bean="perplexityList" key="perplexityKey" pageUrl="self" /></td>
					</tr>
					<tr>
						<td><amb:table bean="perplexityList" id="perplexity" key="perplexityKey"
								dataClass="com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService"
								pageUrl="self" hasFilter="true" emptyTableMsg="msg_perplexity_service_none">
								<amb:column label="checkbox" width="2%">
									<input type="checkbox" name="selectPerplexityIds" value="<%=perplexity.getId()%>"
										displayName="<%=perplexity.getName()%>" onclick="setButtonState()">
								</amb:column>
								<amb:column label="lb_name" sortBy="<%=PerplexityComparator.NAME%>" filter="nameFilter"
									filterValue="<%=nameFilter%>" width="24%">
									
									 <%if (userPermissions.getPermissionFor(Permission.PS_EDIT)){
										 String url = editURL + "&id=" + perplexity.getId();
									 %>
									     <A name='nameLink' class='standardHREF' href="<%=url%>">
											<%=perplexity.getName()%>
										</A>
									 <%} else { 
										 out.print(perplexity.getName());
									 } %>
									 &nbsp;&nbsp;
                                </amb:column>
								<amb:column label="lb_description" sortBy="<%=PerplexityComparator.DESC%>" filter="descriptionFilter"
									filterValue="<%=descriptionFilter%>" width="24%">
									<%=perplexity.getDescription() == null ? "" : perplexity.getDescription()%>
								</amb:column>
								<amb:column label="lb_url" sortBy="<%=PerplexityComparator.URL%>" filter="urlFilter"
									filterValue="<%=urlFilter%>">
									<%=perplexity.getUrl()%>
								</amb:column>
								<%
									if (isSuperAdmin) {
								%>
								<amb:column label="lb_company_name" sortBy="<%=PerplexityComparator.GS_COMPANY_NAME%>"
									filter="companyNameFilter" filterValue="<%=companyNameFilter%>">
									<%=perplexity.getGsCompany()%>
								</amb:column>
								<%
									}
								%>
							</amb:table></td>
					</tr>
					<tr style="padding-top: 5px;">
						<td><amb:tableNav bean="perplexityList" key="perplexityKey" pageUrl="self" scope="10,20,50,All"
								showTotalCount="false" /></td>
					</tr>
					<tr>
						<td style="padding-top: 5px" align="left">
						    <amb:permission name="<%=Permission.PS_REMOVE%>" >
						        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>" name="remBtn" disabled onclick="submitForm('Remove');"> 
						    </amb:permission>
						    <amb:permission name="<%=Permission.PS_EDIT%>" >
						        <INPUT style="display: none" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..." name="editBtn" disabled onclick="submitForm('Edit');"> 
							</amb:permission>
							<amb:permission name="<%=Permission.PS_NEW%>" >
							    <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." onclick="submitForm('New');">
							</amb:permission>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</DIV>
</BODY>
</HTML>