
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
        com.globalsight.everest.company.Company,
        com.globalsight.everest.company.CompanyWrapper,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.projecthandler.ProjectTM,
		com.globalsight.everest.permission.PermissionSet,
        com.globalsight.everest.company.CompanyWrapper,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.util.comparator.ProjectTMComparator,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.pagehandler.tm.management.Tm3ConvertProcess,
        com.globalsight.everest.webapp.pagehandler.tm.management.Tm3ConvertHelper,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.util.StringUtil"
    session="true" %>

<jsp:useBean id="_new" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="clone" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="delete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_import" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="statistics" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="reindex" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="convert" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="users" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="tmSearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="tms" scope="request" class="java.util.ArrayList" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("msg_server_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);

String urlNew    = _new.getPageURL();
String urlModify = modify.getPageURL();
String urlClone  = clone.getPageURL();
String urlDelete = delete.getPageURL();
String urlSearch = search.getPageURL();
String urlImport = _import.getPageURL();
String urlExport = _export.getPageURL();
String urlStatistics = statistics.getPageURL();
String urlReindex = reindex.getPageURL();
String urlConvert = convert.getPageURL();
String urlUsers = users.getPageURL();
String urlTMSearch = tmSearch.getPageURL();
String urlSelf = self.getPageURL();

boolean isSuperAdmin = (Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN);
boolean isAdmin = (Boolean)sessionMgr.getAttribute("isAdmin");
boolean enableTMAccessControl = (Boolean)sessionMgr.getAttribute("enableTMAccessControl");
Company company = CompanyWrapper.getCompanyById(CompanyWrapper.getCurrentCompanyId());
String superCompany = CompanyWrapper.getSuperCompanyName();
boolean isShowTM3 = false;
if (superCompany.equals(company.getCompanyName()) || company.getTmVersion().getValue() == 3)
    isShowTM3 = true;
sessionMgr.removeElement("convertStatus");

String tm3Tms = (String) sessionMgr.getAttribute("tm3Tms");
String convertingTms = (String) sessionMgr.getAttribute("convertingTms");
String remoteTms = (String) sessionMgr.getAttribute("remoteTms");
HashMap<Long, String> tmIdStatusMap = (HashMap<Long, String>) sessionMgr.getAttribute("tmIdStatusMap");

PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
String tmName = "";

String tmNameFilter = (String) sessionMgr.getAttribute("tmNameFilter");
String tmCompanyFilter = (String) sessionMgr.getAttribute("tmCompanyFilter");
%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_tm")%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT src="envoy/tm/management/protocol.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "", tmName = "", guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm")%>";
var tmVersion = 2;
var status = "";
var tm3tms = "<%=tm3Tms%>";
var remoteTms = "<%=remoteTms%>";
var convertingTms = "<%=convertingTms%>";

<%if (isSuperAdmin) {%>
var timerId;
var tmpIndex = 0;
var isShowed = true;
var isReloaded = true;

$(function() {
	timerId = setInterval('getConvertingRate()', 5000);
});

function getConvertingRate() {
    $.ajax({
        url: "/globalsight/envoy/tm/management/convertRate.jsp?id=" + (tmpIndex++),
		cache: false
      }).done(function(data) {
    	  var returnData = $.trim(data);
		  if (returnData != "") {
    		  var arr = returnData.split(",");
    		  var tm2Id = $.trim(arr[0]);
    		  var convertRate = eval(arr[1]);
			  status = arr[2];
    		  if (status == "Converting" && (convertRate > 0 && convertRate < 100)) {
                  $("#msg").html(arr[4]).show();
                  $("#status"+tm2Id).html("<font color='red'><%=bundle.getString("lb_tm_converting") %> (" + convertRate + "%)</font>");
    		  } else if (status == "Cancelling") {
                  $("#status"+tm2Id).html("<font color='red'>Cancelling...</font>");
    		  } else {
    			  $("#msg").hide();
    			  if (status == '')
    				   $("#status"+tm2Id).html("&nbsp;");
				  if ((status == "Cancelled" || convertRate > 1) && isShowed) {
                      if (status != "null" && convertRate == 100) {
                          alert(arr[3] + " conversion is done successfully");
                          $("#TMForm").attr("action", "<%=urlSelf %>").submit();
                      }
                      else if (status != "null")
                          alert(arr[3] + " conversion is " + status);
                      isShowed = false;
				  }
    		  }
    	  }
    	  else
    		$("#msg").hide();
      });
}

function clearTimer() {
   window.clearInterval(timerId);
}
<%}%>

function enableButtons()
{
	var id = findSelectedRadioButton();
	var selectMode = "single";
	if (id == "")
	  selectMode = "none";
	else if (id.indexOf(",") > 0)
	  selectMode = "multiple";

	var flag = false;
	var flagTm3 = false;
	var disableReindex = false;
	var canRemove = false;

	var tmpId = "";
	var tm3tms2 = "," + tm3tms;
	var remoteTms2 = "," + remoteTms;
	var convertingTms2 = "," + convertingTms;
	if (selectMode == "single")
	{
		tmpId = "," + id + ",";
        if (remoteTms2.indexOf(tmpId) > -1) {
            disableReindex = true;
        }
		if (tm3tms2.indexOf(tmpId) > -1 || remoteTms2.indexOf(tmpId) > -1) {
			flagTm3 = true;
		}
		if (convertingTms2.indexOf(tmpId) > -1) {
			flag = true;
			disableReindex = true;
		}
		if ($.trim($("#tmversion"+id).text()) == "2")
			flagTm3 = true;
	}
	else
	{
		flag = true;
		flagTm3 = true;
		if (selectMode == "none")
		{
	        canRemove = true;
		}
	}

	setButtonStatus(flag, flagTm3, disableReindex);

	$("#deleteBtn").attr("disabled", canRemove);
	
	<%
	if (isSuperAdmin) {
	%>
	   $("#mainBtn").attr("disabled", true);
	   $("#importBtn").attr("disabled", true);
	   $("#exportBtn").attr("disabled", true);
	   $("#dupBtn").attr("disabled", true);
	   $("#deleteBtn").attr("disabled", true);
	<%
	}
	%>
}

function setButtonStatus(flag, flagTm3, disableReindex) {
	$(":button[id='statBtn']").each(function(){
		$(this).attr("disabled", flag);
	});
	$(":button[id='mainBtn']").each(function(){
		$(this).attr("disabled", flag);
	});
	$(":button[id='importBtn']").each(function(){
		$(this).attr("disabled", flag);
	});
	$(":button[id='exportBtn']").each(function(){
		$(this).attr("disabled", flag);
	});
	$(":button[id='userBtn']").each(function(){
		$(this).attr("disabled", flag);
	});
	$(":button[id='dupBtn']").each(function(){
		$(this).attr("disabled", flag);
	});
	$(":button[id='convertBtn']").each(function(){
		$(this).attr("disabled", flagTm3);
	});
	$(":button[id='reindexBtn']").each(function(){
		$(this).attr("disabled", disableReindex);
	});
}

function findSelectedRadioButton() {
    var id = "";
    $('input[type="checkbox"][name="TMId"]:checked').each(function (){
	    id += $(this).val() + ",";
	});
    if (id != "")
	  id = id.substring(0, id.length - 1);
	return id;
}


function newTM() {
    window.location.href = '<%=urlNew + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_NEW%>';
}

function modifyUsers() {
	var id = findSelectedRadioButton();
	$("#TMForm").attr("action", '<%=urlUsers + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_USERS%>')
	            .submit();
}

function searchTM() {
	window.location.href = '<%=urlTMSearch+"&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_TM_SEARCH%>';
}

function modifyTM(id) {
    var url = "<%=urlModify%>&action=<%=WebAppConstants.TM_ACTION_MODIFY%>&TMId=" + id;
    $("#TMForm").attr("action", url).submit();
}

function cloneTM() {
    var id = findSelectedRadioButton();
    if (!id) {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_duplicate"))%>");
    } else
	    $("#TMForm").attr("action", '<%=urlClone + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_CLONE%>')
                    .submit();
}

function removeTM() {
    $("#TMForm").attr("action", '<%=urlDelete + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_DELETE%>')
                .submit();
}

function importTM() {
    var id = findSelectedRadioButton();
    if (!id) {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_import"))%>");
    } else
	    $("#TMForm").attr("action", '<%=urlImport + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_IMPORT%>')
                    .submit();
}

function exportTM() {
    var id = findSelectedRadioButton();
    if (!id) {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_export"))%>");
    } else
  	    $("#TMForm").attr("action", '<%=urlExport + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_EXPORT%>')
                    .submit();
}

function reindexTM() {
	var id = findSelectedRadioButton();
    var url = '<%=urlReindex%>&<%=WebAppConstants.TM_ACTION%>=<%=WebAppConstants.TM_ACTION_REINDEX%>&TMId=' + id;
    $("#TMForm").attr("action", url).submit();
}

function showStatistics() {
    var id = findSelectedRadioButton();
    if (!id) {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_for_statistics"))%>");
    } else {
      var url = '<%=urlStatistics + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_STATISTICS +
                "&" + WebAppConstants.RADIO_TM_ID + "=" %>' + id;
        if(navigator.userAgent.indexOf("Chrome") >0 )
        {
        	window.open(url, null,
            'width = 400,height = 400,status = no,center = yes,left = 300,top = 100');
        }
        else
        {
     	window.showModalDialog(url, null,
          'menubar:no;location:no;resizable:yes;center:yes;toolbar:no;' +
          'status:no;dialogHeight:400px;dialogWidth:400px;'); 
     	}
    }
}

function maintainTM() {
    var id = findSelectedRadioButton();
    if (!id) {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_maintain"))%>");
    } else
	    $("#TMForm").attr("action", '<%=urlSearch + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_MAINTENANCE%>')
                    .submit();
}

function convertToTm3() {
    var id = findSelectedRadioButton();
    if (!id) {
      alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_convert"))%>");
    } else {
    	<%
    	Tm3ConvertProcess tm3ConvertProcess = Tm3ConvertProcess.getInstance();
    	if ("Converting".equals(tm3ConvertProcess.getStatus())) {
    	%>
    	  alert("A conversion is running now. Please try this operation later.");
    	  return;
    	<%
    	}
    	%>
      var actionUrl = '<%=urlConvert + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_CONVERT %>';
      if (confirm("<%=bundle.getString("msg_tm3_conversion_confirm") %>")) {
       	$("#TMForm").attr("action", actionUrl).submit();
      }
    }
}

function cancelConvert() {
	if (confirm("Are you sure that you need to cancel current TM3 conversion?")) {
		$("#TMForm").attr("action", "<%=urlConvert + "&" + WebAppConstants.TM_ACTION + "=" + WebAppConstants.TM_ACTION_CANCEL_CONVERT %>")
		            .submit();
	}
}

function selectRow()
{
  var index = event.srcRow.rowIndex;

  if (index > 0)
  {
    var radios = TMForm.TMId;

    if (radios.length)
    {
      radios[index-1].checked = true;
    }
    else
    {
      radios.checked = true;
    }
  }
}

//show the corpus browser for concordance searches
var w_corpusBrowser = null;
function showCorpusBrowser()
{
   var url = "/globalsight/ControlServlet?activityName=browseCorpus&pagename=CTMB&fromEditor=false";
   w_corpusBrowser = window.open(url, "<%=bundle.getString("lb_concordance")%>",
   'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
}

function doOnUnload()
{
    try { w_corpusBrowser.close(); } catch (ignore) {}
}

function doLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();

  eval("<%=errorScript%>");
}

function continueToConvert(id) {
    var actionUrl = "<%=urlConvert%>&TMId=" + id + "&<%=WebAppConstants.TM_ACTION%>=<%=WebAppConstants.TM_ACTION_CONVERT %>";
    if (confirm("<%=bundle.getString("msg_tm3_conversion_confirm") %>")) {
        $("#TMForm").attr("action", actionUrl).submit();
    }
}

function handleSelectAll() {
  var selectAll = $("#selectAll").is(":checked");
  $("#TMForm :checkbox:not('#showTM3')").attr("checked", selectAll);
  setButtonStatus(true, true, false);
  $("#deleteBtn").attr("disabled", !selectAll);
}

function filterItems(e) {
	e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
	if (keyCode == 222) {
		alert("Invalid character \"\'\" is input.");
		return false;
	}
	if (keyCode == 13) {
		var actionUrl = "<%=urlSelf%>&action=filterSearch";
		$("#TMForm").attr("action", actionUrl).submit();
	}
}

</SCRIPT>
</HEAD>

<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONUNLOAD="doOnUnload()" width="100%" align="center">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<div align="left"> <SPAN CLASS="mainHeading"><%=bundle.getString("lb_tm")%></SPAN></div>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD>
      <%=bundle.getString("helper_text_tm_main")%>
    </TD>
  </TR>
</TABLE>
<% if (isSuperAdmin) { %>
<div id="msg" align="left" class="standardText"></div><br>
<% } %>
<FORM id="TMForm" NAME="TMForm" method="post">
<table name="test" border=0 width="1024px">
  <tr><td align="center"></td></tr>
</table>
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
  <tr valign="top">
    <td>
      <amb:tableNav bean="tms" key="<%=WebAppConstants.TM_KEY%>" pageUrl="self"/>
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="tms" id="tm" key="<%=WebAppConstants.TM_KEY%>"
      dataClass="com.globalsight.everest.projecthandler.ProjectTM" pageUrl="self"
      emptyTableMsg="msg_no_tms" hasFilter="true">
      <amb:column label="checkbox" width="2%">
      <input type="checkbox" id="<%=WebAppConstants.RADIO_TM_ID%>" name="<%=WebAppConstants.RADIO_TM_ID%>" value="<%=tm.getId()%>"
      onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=ProjectTMComparator.NAME%>" filter="tmNameFilter" filterValue="<%=tmNameFilter %>" width="10%">
	    <%
		tmName = tm.getName();
		if (userPermissions.getPermissionFor(Permission.TM_EDIT) && !isSuperAdmin)
		  out.print("<a href='javascript:void(0)' title='Edit translation memory' onclick='modifyTM(" + tm.getId() + ");'>" + tmName + "</a>");
		else
		  out.print(tmName);
		%>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=ProjectTMComparator.DESC%>" width="40%">
      <% out.print(tm.getDescription() == null ? "" : tm.getDescription()); %>
      </amb:column>
      <amb:column label="lb_domain" sortBy="<%=ProjectTMComparator.DOMAIN%>" width="10%">
      <% out.print(tm.getDomain() == null ? "" : tm.getDomain()); %>
      </amb:column>
      <amb:column label="lb_organization" sortBy="<%=ProjectTMComparator.ORG%>" width="10%">
      <% out.print(tm.getOrganization() == null ? "" : tm.getOrganization()); %>
      </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=ProjectTMComparator.ASC_COMPANY%>" filter="tmCompanyFilter" filterValue="<%=tmCompanyFilter %>" width="10%">
      <%=CompanyWrapper.getCompanyNameById(tm.getCompanyId())%>
      </amb:column>
      <% } %>
      <%
      if (isShowTM3) {
      %>
	      <amb:column label="lb_tm_tm3">
	      <% out.print(tm.getTm3Id() == null ? "<input type='checkbox' id='showTM3' disabled>" : "<input type='checkbox' id='showTM3' disabled checked>"); %>
	      </amb:column>
	      <% if (isSuperAdmin) { %>
		      <amb:column label="lb_status">
		          <span id="status<%=tm.getId()%>">
		          <%
		          int convertRate = tm.getConvertRate();
		          String status = tmIdStatusMap.get(tm.getId());
		          if (convertRate > 1 && convertRate < 100 && status != null)
		          {
		              if ("Cancelling".equalsIgnoreCase(status))
		              {
		                  out.println("<font color='red'>" + status + "...</font>");
		              }
		              else
		              {
	                      out.println(status + " (" + convertRate + "%) <a href='#' onclick='continueToConvert("
                               + tm.getConvertedTM3Id() + ");'><img src='/globalsight/images/refresh.png' width=14px height=14px border=0 title='"
                               + bundle.getString("msg_tm_convert_continue") + "'/></a>");
		              }
		          }
		          %>
		          </span>
		          <span id="tmversion<%=tm.getId()%>" style="display:none;">
		          <%
		          long companyId = tm.getCompanyId();
		          company = CompanyWrapper.getCompanyById(companyId);
		          out.print(company.getTmVersion().getValue());
		          %>
		          </span>
		      </amb:column>
	      <% } %>
      <% } %>
      </amb:table>
    </TD>
  </TR>
  <TR>
    <td>
      <amb:tableNav bean="tms" key="<%=WebAppConstants.TM_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
    </td>
  </TR>
  <TR><TD>&nbsp;</TD></TR>
  <TR>
    <TD>
      <P>
      <DIV ALIGN="left">
      <!-- hide "Coupus Browser" button -->
      <% if (false) { %>
    <amb:permission name="<%=Permission.TM_BROWSER%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON"
      VALUE="<%=bundle.getString("lb_corpus_browser")%>"
      ID="corpusBtn" onclick="showCorpusBrowser()"
      TITLE="<%=bundle.getString("lb_corpus_browser")%>">
    </amb:permission>
      <% } %>

    <amb:permission name="<%=Permission.TM_STATS%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="statBtn" disabled
         VALUE="<%=bundle.getString("lb_statistics")%>"
         ID="statBtn" onclick="showStatistics()"
         TITLE="<%=bundle.getString("helper_text_tm_show_statistics")%>">
    </amb:permission>

    <amb:permission name="<%=Permission.TM_MAINTENANCE%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="mainBtn" disabled
         VALUE="<%=bundle.getString("lb_maintenance")%>"
         ID="mainBtn" onclick="maintainTM()"
         TITLE="<%=bundle.getString("helper_text_tm_maintenence")%>">
    </amb:permission>

      <% if (isSuperAdmin) { %>
	      <INPUT CLASS="standardText" TYPE="BUTTON" name="convertBtn" disabled
	      VALUE="<%=bundle.getString("lb_tm_convert_tm3")%>"
	      ID="convertBtn" onclick="convertToTm3()"
	      TITLE="<%=bundle.getString("lb_tm_convert_tm3")%>">
      <% } %>

    <amb:permission name="<%=Permission.TM_IMPORT%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="importBtn" disabled
         VALUE="<%=bundle.getString("lb_import1")%>"
         ID="importBtn" onclick="importTM()"
         TITLE="<%=bundle.getString("helper_text_tm_import_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_EXPORT%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="exportBtn" disabled
         VALUE="<%=bundle.getString("lb_export1")%>"
         ID="exportBtn" onclick="exportTM()"
         TITLE="<%=bundle.getString("helper_text_tm_export_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_REINDEX%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="reindexBtn"
         VALUE="<%=bundle.getString("lb_reindex")%>"
         ID="reindexBtn" onclick="reindexTM()"
         TITLE="<%=bundle.getString("helper_text_tm_reindex_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_DUPLICATE%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="dupBtn" disabled
         VALUE="<%=bundle.getString("lb_duplicate1")%>"
         ID="dupBtn" onclick="cloneTM()"
         TITLE="<%=bundle.getString("helper_text_tm_clone_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_DELETE%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="deleteBtn" disabled 
         VALUE="<%=bundle.getString("lb_tm_delete")%>"
         ID="deleteBtn" onclick="removeTM()"
         TITLE="<%=bundle.getString("helper_text_tm_remove")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_NEW%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON"
         VALUE="<%=bundle.getString("lb_new1")%>"
         ID="newBtn" onclick="newTM()"
         TITLE="<%=bundle.getString("helper_text_tm_new_tm")%>">
    </amb:permission>
    <%if(enableTMAccessControl&&isAdmin){%>
         <INPUT CLASS="standardText" TYPE="BUTTON" name="usersBtn" disabled
         VALUE="<%=bundle.getString("lb_users")+"..."%>"
         ID="userBtn" onclick="modifyUsers()"
         TITLE="<%=bundle.getString("helper_text_tm_users")%>">
    <%}%>
    <amb:permission name="<%=Permission.TM_SEARCH%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON"
         VALUE="<%=bundle.getString("lb_search")%>..."
         ID="idTMSearch" onclick="searchTM()"
         TITLE="<%=bundle.getString("lb_search")%>">
    </amb:permission>

     </DIV>
    </TD>
  </TR>
</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
