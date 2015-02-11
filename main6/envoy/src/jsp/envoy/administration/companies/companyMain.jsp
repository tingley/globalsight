<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.company.Company,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.webapp.pagehandler.PageHandler, 
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
      com.globalsight.everest.webapp.pagehandler.administration.company.CompanyMigration,
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.persistence.hibernate.HibernateUtil,
      com.globalsight.everest.webapp.tags.TableConstants,
      com.globalsight.everest.permission.PermissionSet,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.util.comparator.CompanyComparator, 
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
<jsp:useBean id="companies" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 
    String confirmRemove = bundle.getString("msg_confirm_company_removal");
    String notAllowedToRemoveSuperCompany = bundle.getString("msg_not_allowed_super_company_removal");
    String newURL = new1.getPageURL() + "&action=" + CompanyConstants.CREATE;
    String modifyURL = modify.getPageURL() + "&action=" + CompanyConstants.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + CompanyConstants.REMOVE;
    String convertURL = self.getPageURL() + "&action=" + CompanyConstants.CONVERT;
    String searchURL = self.getPageURL() + "&action=" + CompanyConstants.SEARCH;
    String getMigratePercentageURL = self.getPageURL() + "&action=" + CompanyConstants.GET_MIGRATE_PROCESSING;
    String title = bundle.getString("lb_companies");
    String helperText= bundle.getString("helper_text_companies");

    String deps = (String) sessionMgr.getAttribute(CompanyConstants.DEPENDENCIES);
    Long migrationCompanyId = CompanyMigration.getMigrationCompanyId();
    String migrationStatus = null;
    String migrationComName = "";
    Company migratingCompany = null;
    if (migrationCompanyId != null){
        migrationStatus = CompanyMigration.getMigrationStatus(migrationCompanyId);
        migratingCompany = ServerProxy.getJobHandler().getCompanyById(migrationCompanyId);
        migrationComName = migratingCompany.getCompanyName();
    }

    String filterNameValue = (String) sessionMgr.getAttribute("companyNameFilter");
    if (filterNameValue == null || filterNameValue.trim().length() == 0){
        filterNameValue = "";
    }
    
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    
    
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "companies";
var helpFile = "<%=bundle.getString("help_companies_main_screen")%>";
var isInDeleting = false;

$(document).ready(function()
{
<%
if (CompanyMigration.STOPPED.equals(migrationStatus)) {
	String msg = migratingCompany.getMigrateProcessing() + "% migration has been finished for "
		+ migratingCompany.getCompanyName() + ". You can click \"Migrate\" to migrate the rest.";
	msg = "<span style=\"color:#D60018;\">" + msg + "</span></br>";
%>
	$("#idMigratingMsg").html('<%=msg%>');
	$("#idMigratingMsg").show();
<%
	} else if (CompanyMigration.MIGRATING.equals(migrationStatus)) {
%>
	getMigratePercentage();
<%  } %>

// If a company is being migrated, disable its radio button.
$(":radio").each(function(){
	if ($(this).val() == '<%=migrationCompanyId%>'){
		$(this).attr("disabled", "disabled");
	}
});
	
function getMigratePercentage()
{
	// On IE, it the url is no change, seems it will not execute the url really, 
	// so add a fake parameter "fresh" to cheat it in IE.
	getPercentageUrl = '<%=getMigratePercentageURL%>' + "&fresh=" + Math.random();
	$.getJSON(getPercentageUrl, {companyId:<%=migrationCompanyId%>}, function(data) {
		var per = data.migrateProcessing;
		var processingMsg = "<span style=\"color:#D60018;\">Company " + "<%=migrationComName%>" + " is being migrated (" + per + "%)</span>";
		$("#idMigratingMsg").html(processingMsg);
		$("#idMigratingMsg").show();
		$("#idMigratingComStatus").html("Migrating (" + per + "%)");
		if (per < 100) {
			setTimeout(getMigratePercentage, 3000);
	    } else {
	    	var finishedMsg = "Migration has been finished for company " + "<%=migrationComName%>";
	    	setTimeout("alert('" + finishedMsg + "');", 1000);
	    	setTimeout(reloadCompanyList, 2000);
		}
    });
}
	
function reloadCompanyList(){
	companyForm.action = "<%=self.getPageURL()%>";
   	companyForm.submit();
}

});

function filterItems(e) {
	e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
	if (keyCode == 13) {
		var actionUrl = "<%=searchURL%>";
		$("#companyForm").attr("action", actionUrl).submit();
	}
}

function findSelectedCompanies(){
    var id = "";
	$("input[name='companyCheckboxBtn']:checked").each(function (){
	    id += $(this).val() + ",";
	});
	if (id != "")
	  id = id.substring(0, id.length - 1);

	return id;
}

function changeButtonState() {
	var count = $("input[name='companyCheckboxBtn']:checked").length;
	// No company is selected
	if (count == 0) {
		$("#removeBtn").attr("disabled", true);
		$("#migrateBtn").attr("disabled", true);
	}
	// Only one company is selected
	else if (count == 1) {
	    $("#removeBtn").attr("disabled", isInDeleting);
		
		var usingSeparatedTables = "";
		var selectCompanyId = findSelectedCompanies();
		$("input:[name='migratedCheckbox']").each(function(){
			if ($(this).val() == selectCompanyId && selectCompanyId != '1'){
				if ($(this).attr("checked") != "checked"){
					usingSeparatedTables = '0';
				}
			}
		});
		if (usingSeparatedTables == '0'){
			$("#migrateBtn").attr("disabled", false);
    	} else {
    		$("#migrateBtn").attr("disabled", true);
    	}
	}
	// More than one companies are selected
	else {
		$("#removeBtn").attr("disabled", true);
		$("#migrateBtn").attr("disabled", true);
	}
}

function handleSelectAll() {
	  var selectAll = $("#selectAll").is(":checked");
	  $("input[name='companyCheckboxBtn']").each(function(){
		  if (selectAll == true){
			  $(this).attr("checked", true);
		  } else {
			  $(this).attr("checked", false);
		  }
 	  });

	  changeButtonState();
}

function newCompany() {
	$("#companyForm").attr("action", "<%=newURL%>").submit();
}

function editCompany(companyId) {
    var url = "<%=modifyURL%>" + "&id=" + companyId;
    $("#companyForm").attr("action", url).submit();
}

function removeCompany() {
    value = findSelectedCompanies();
	var comIDs = value.split(",");
	for (i = 0; i <= comIDs.length; i++) {
    	if ("1" == comIDs[i]) {
    		alert("<%=notAllowedToRemoveSuperCompany%>");
    		return false;
    	}
	}

	if (!confirm('<%=confirmRemove%>')) {
    	return false;
    }

    var url = "<%=removeURL%>" + "&id=" + value;
    $("#companyForm").attr("action", url).submit();
}

function migrateCompany(){
    value = findSelectedCompanies();
	if ("1" == value){
		alert("<%=bundle.getString("msg_not_allowed_super_company_migrate")%>");
		return false;
	}

	var whoIsInMigration = "<%=CompanyMigration.getMigrationCompanyId()%>";
	if (whoIsInMigration != null && whoIsInMigration != "null"){
		alert("<%=bundle.getString("msg_not_allowed_migrate_now")%>");
		return false;
	}

	if (!confirm("<%=bundle.getString("msg_confirm_company_migrate")%>")){
		return false;
	}

	var url = "<%=convertURL%>" + "&id=" + value;
	$("#companyForm").attr("action", url).submit();
}

</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement(CompanyConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>
<div id="idMigratingMsg" style="display:none"></div>

<form id="companyForm" name="companyForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="companies" key="<%=CompanyConstants.COMPANY_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
    <% boolean isCompanyDeleting = false;%>
      <amb:table bean="companies" id="company"
       key="<%=CompanyConstants.COMPANY_KEY%>"
       dataClass="com.globalsight.everest.company.Company" pageUrl="self"
       emptyTableMsg="msg_no_companies" hasFilter="true">
      <amb:column label="checkbox" width="25px">
      <input type="checkbox" name="companyCheckboxBtn" value="<%=company.getId()%>"
      <% if (company.getState() != null && Company.STATE_DELETING.equals(company.getState())) {
      %> 
    	 disabled>
    	 <script language="javascript">
    	   isInDeleting = true;
    	 </script>
      <%  isCompanyDeleting = true;
      } else {
    	  isCompanyDeleting = false;
      %>
    	 onclick="changeButtonState();">
      <%} 
      %>
      </amb:column>
      <% if (isCompanyDeleting) {
      %>
      <amb:column label="lb_name" sortBy="<%=CompanyComparator.NAME%>" 
          filter="<%=CompanyConstants.FILTER_NAME %>" filterValue="<%=filterNameValue%>"
          width="150px" style="color:gray">
      <%=company.getName() + " (Deleting)"%>
      </amb:column>
      <%} else {
      %>
      <amb:column label="lb_name" sortBy="<%=CompanyComparator.NAME%>" 
          filter="<%=CompanyConstants.FILTER_NAME %>" filterValue="<%=filterNameValue%>" 
          width="150px" >
      <%
        String companyName = company.getName();
		if (userPermissions.getPermissionFor(Permission.COMPANY_EDIT))
		  out.print("<a href='javascript:void(0);' onclick='editCompany(" + company.getId() + ");'>" + companyName + "</a>");
		else
		  out.print(companyName);
		%>
      </amb:column>
      <%} %>

      <% if (isCompanyDeleting) {
      %>
      <amb:column label="lb_description" sortBy="<%=CompanyComparator.DESC%>"
       width="1000px" style="color:gray">
       <% out.print(company.getDescription() == null ? "" : company.getDescription()); %>
      </amb:column>
      <%} else {
      %>
      <amb:column label="lb_description" sortBy="<%=CompanyComparator.DESC%>" width="1000p" >
      <% out.print(company.getDescription() == null ? "" : company.getDescription()); %>
      </amb:column>
      <%} %>
      <amb:column label="lb_migrated" width="10px" >
      <% String migrated = ""; 
      if (company.getBigDataStoreLevel() == 0) {
          migrated = "<input type='checkbox' " + "name='migratedCheckbox' value=" + company.getId() + " disabled>";
      } else {
          migrated = "<input type='checkbox' " + "name='migratedCheckbox' value=" + company.getId() + " disabled checked>";
      }
      out.print(migrated);
      %>
      </amb:column>
      <amb:column label="">
      <% out.print(migrationCompanyId != null && company.getId() == migrationCompanyId ? "<span id=\"idMigratingComStatus\" style=\"color:#D60018;\"></span>" : "&nbsp;&nbsp;&nbsp;"); %>
      </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="companies" key="<%=CompanyConstants.COMPANY_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
    </td>
  </tr>
  <tr>
    <td style="padding-top:10px" align="left">
    <amb:permission name="<%=Permission.COMPANY_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
       id="removeBtn" name="removeBtn" disabled onclick="removeCompany();">
    </amb:permission>
    <amb:permission name="<%=Permission.COMPANY_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="newCompany();">
    </amb:permission>
    <amb:permission name="<%=Permission.COMPANY_MIGRATE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_migrate")%>..." 
       id="migrateBtn" name="migrateBtn" disabled onclick="migrateCompany();">
    </amb:permission>
    </td>
  </tr>
  
  <tr><td>&nbsp;</td></tr>
  
</table>
</form>
</div>
</BODY>
</HTML>
