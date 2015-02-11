<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.everest.util.comparator.TermbaseInfoComparator,
        com.globalsight.util.edit.EditUtil,
		com.globalsight.everest.company.CompanyWrapper,
        com.globalsight.everest.projecthandler.ProjectImpl,
        java.util.List,
         java.text.MessageFormat,
        java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="definition" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="delete" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_import" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="statistics" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="inputmodels" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="maintenance" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="indexes" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<!-- York added on 2009-03-19 //--> 
<jsp:useBean id="termSearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="namelist" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="users" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
  
String uiLocale = (String) sessionMgr.getAttribute("uiLocale");

String tbCompanyFilter = (String) sessionMgr.getAttribute("tbCompanyFilter");
if (tbCompanyFilter == null || tbCompanyFilter.trim().length() == 0)
{
    tbCompanyFilter = "";
}
String tbNameFilter = (String) sessionMgr.getAttribute("tbNameFilter");
if (tbNameFilter == null || tbNameFilter.trim().length() == 0)
{
    tbNameFilter = "";
}

PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

String urlSelf=self.getPageURL();
String urlDefinition = definition.getPageURL();
String urlDelete = delete.getPageURL();
String urlImport = _import.getPageURL();
String urlExport = _export.getPageURL();
String urlStatistics = statistics.getPageURL();
String urlInputModels = inputmodels.getPageURL();
String urlMaintenance = maintenance.getPageURL();
String urlIndexes = indexes.getPageURL();
String urlTermSearch = termSearch.getPageURL(); //York added on 2009-03-19
String urlUsers = users.getPageURL();

boolean isSuperAdmin = (Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN);
boolean isAdmin = (Boolean) sessionMgr.getAttribute("isAdmin");
boolean enableTBAccessControl = (Boolean) sessionMgr.getAttribute("enableTBAccessControl");

StringBuffer deps = new StringBuffer();

if(sessionMgr.getAttribute("projectsByTermbaseDepended") != null) {
    Object[] args = {bundle.getString("lb_termbases")};
    deps.append("<span class=\"errorMsg\">");
    deps.append(MessageFormat.format(bundle.getString("msg_dependency"), args));
    deps.append("<br>" + "<p>*** Project ****<br>");

    List projectsByTermbaseDepended = (List)sessionMgr.getAttribute("projectsByTermbaseDepended");
    
    for(int i = 0; i < projectsByTermbaseDepended.size(); i++) {
        ProjectImpl project = (ProjectImpl)projectsByTermbaseDepended.get(i);
        deps.append("<br>");
        deps.append(project.getName());
    }
    
    deps.append("</span>");
}

%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_terminology")%></TITLE>

<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script src="/globalsight/includes/xmlextras.js"></script>
<SCRIPT src="/globalsight/envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_terminology_main_screen")%>";

var name = "";

function enableButtons()
{
    if (termbaseForm.statBtn)
        termbaseForm.statBtn.disabled = false;
    if (termbaseForm.indexBtn)
        termbaseForm.indexBtn.disabled = false;
    if (termbaseForm.removeBtn)
        termbaseForm.removeBtn.disabled = false;
    if (termbaseForm.dupBtn)
        termbaseForm.dupBtn.disabled = false;
    if (termbaseForm.editBtn)
        termbaseForm.editBtn.disabled = false;
    if (termbaseForm.browseBtn)
        termbaseForm.browseBtn.disabled = false;
    if (termbaseForm.importBtn)
        termbaseForm.importBtn.disabled = false;
    if (termbaseForm.exportBtn)
        termbaseForm.exportBtn.disabled = false;
    if (termbaseForm.mainBtn)
        termbaseForm.mainBtn.disabled = false;
    if (termbaseForm.inputBtn)
        termbaseForm.inputBtn.disabled = false;
    if (termbaseForm.usersBtn)
    	termbaseForm.usersBtn.disabled = false;
}
function buttonManagement()
{
    var count = $("input[name='radioBtn']:checked").length;
    if (count == 1)
    {
        $("#idRemove").attr("disabled", false);
        $("#idStatistics").attr("disabled", false);
        $("#idIndexes").attr("disabled", false);
        $("#idClone").attr("disabled", false);
        $("#idBrowse").attr("disabled", false);
        $("#idImport").attr("disabled", false); 
        $("#idExport").attr("disabled", false); 
        $("#idMaintenance").attr("disabled", false); 
        $("#idInputModels").attr("disabled", false); 
        $("#idUsers").attr("disabled", false); 
    }
    else
    {
        $("#idRemove").attr("disabled", true);
        $("#idStatistics").attr("disabled", true);
        $("#idIndexes").attr("disabled", true);
        $("#idClone").attr("disabled", true);
        $("#idBrowse").attr("disabled", true);
        $("#idImport").attr("disabled", true); 
        $("#idExport").attr("disabled", true); 
        $("#idMaintenance").attr("disabled", true); 
        $("#idInputModels").attr("disabled", true); 
        $("#idUsers").attr("disabled", true); 
    }
}

function findSelectedRadioButton()
{
   var id;

   if (termbaseForm.radioBtn)
   {
       // If more than one radio button is displayed, the length
       // attribute of the radio button array will be non-zero, so
       // find which one is checked
       if (termbaseForm.radioBtn.length)
       {
           for (i = 0; i < termbaseForm.radioBtn.length; i++)
           {
               if (termbaseForm.radioBtn[i].checked == true)
               {
                   id = termbaseForm.radioBtn[i].value;
                   break;
               }
           }
       }
       else
       {
           // If only one is displayed, there is no radio button
           // array, so just check if the single radio button is
           // checked
           if (termbaseForm.radioBtn.checked == true)
           {
               id = termbaseForm.radioBtn.value;
           }
       }
   }
   return id;
}

function handleSelectAll()
{
    var ch = $("#selectAll").attr("checked");
    if (ch == "checked")
    {
        $("[name='radioBtn']").attr("checked", true);
    }
    else
    {
        $("[name='radioBtn']").attr("checked", false);
    }

    buttonManagement();
}
function newTermbase()
{
    window.location.href = '<%=urlDefinition +
      "&" + WebAppConstants.TERMBASE_ACTION +
      "=" + WebAppConstants.TERMBASE_ACTION_NEW%>';
}

function modifyUsers()
{
	var id = findSelectedRadioButton();
	termbaseForm.action = '<%=urlUsers +
		      "&" + WebAppConstants.TERMBASE_ACTION +
		      "=" + WebAppConstants.TERMBASE_ACTION_USERS%>';
    termbaseForm.submit();
}

function modifyTermbase(termbaseId)
{
    termbaseForm.action = '<%=urlDefinition + 
            "&" + WebAppConstants.TERMBASE_ACTION + 
            "=" + WebAppConstants.TERMBASE_ACTION_MODIFY + 
            "&radioBtn="%>' + termbaseId;
      termbaseForm.submit();
}

function cloneTermbase()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase_to_duplicate"))%>");
  }
  else
  {
	  termbaseForm.action = '<%=urlDefinition +
	      "&" + WebAppConstants.TERMBASE_ACTION +
	      "=" + WebAppConstants.TERMBASE_ACTION_CLONE%>';
      termbaseForm.submit();
  }
}
     
function removeTermbase()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase_to_remove"))%>");
  }
  else
  {
    var ok = confirm("<%=EditUtil.toJavascript(bundle.getString("jsmsg_removing_a_termbase_may_be_harmful"))%>");
    if (ok)
    {
        termbaseForm.action = '<%=urlDelete +
            "&" + WebAppConstants.TERMBASE_ACTION +
            "=" + WebAppConstants.TERMBASE_ACTION_DELETE%>';
        termbaseForm.submit();
    }
  }
}

function importTermbase()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase_to_import"))%>");
  }
  else
  {
	  termbaseForm.action = '<%=urlImport +
	      "&" + WebAppConstants.TERMBASE_ACTION +
	      "=" + WebAppConstants.TERMBASE_ACTION_IMPORT%>';
      termbaseForm.submit();
  }
}

function exportTermbase()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase_to_export"))%>");
  }
  else
  {
	  termbaseForm.action = '<%=urlExport +
	      "&" + WebAppConstants.TERMBASE_ACTION +
	      "=" + WebAppConstants.TERMBASE_ACTION_EXPORT%>';
      termbaseForm.submit();
  }
}

function browseTermbase()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase_to_browse"))%>");
  }
  else
  {
    ShowTermbase(id);
  }
}

function showStatistics()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase"))%>");
  }
  else
  {
    var url = '<%=urlStatistics +
      "&" + WebAppConstants.TERMBASE_ACTION +
      "=" + WebAppConstants.TERMBASE_ACTION_STATISTICS +
      "&" + WebAppConstants.RADIO_BUTTON + "=" %>' + id;
      if(navigator.userAgent.indexOf("Chrome") >0 )
      {
      	window.open(url, null,
          'width = 400,height = 400,status = no,center = yes,left = 300,top = 100');
      }else{
    	window.showModalDialog(url, null,
        	'menubar:no;location:no;resizable:yes;center:yes;toolbar:no;' +
        	'status:no;dialogHeight:400px;dialogWidth:400px;');
      }
  }
}

function showIndexes()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_terminology_select_termbase"))%>");
  }
  else
  {
	  termbaseForm.action = '<%=urlIndexes%>';
	  termbaseForm.submit();
  }
}

function manageInputModels()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("Please select a termbase.");
  }
  else
  {
	  window.location.href  = '<%=urlInputModels +
	      "&" + WebAppConstants.TERMBASE_ACTION +
	      "=" + WebAppConstants.TERMBASE_ACTION_INPUT_MODELS%>' + '&radioBtn=' + id;
  }
}

function maintainTermbase()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("Please select a termbase.");
  }
  else
  {
	  termbaseForm.action = '<%=urlMaintenance +
	      "&" + WebAppConstants.TERMBASE_ACTION +
	      "=" + WebAppConstants.TERMBASE_ACTION_MAINTENANCE%>';
      termbaseForm.submit();
  }
}

function selectRow()
{
  var index = event.srcRow.rowIndex;

  if (index > 0)
  {
    var radios = termbaseForm.radioBtn;

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

function doLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();

}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
        termbaseForm.action = "<%=urlSelf%>";
        termbaseForm.submit();
    }
}
//York added on 2009-03-19
function searchTerms(){
	var url = '<%=urlTermSearch +
        "&" + WebAppConstants.TERMBASE_ACTION +
        "=" + WebAppConstants.TERMBASE_ACTION_TERM_SEARCH%>';

//    alert("searchTerms() url: " + url);
    window.location.href = url;
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>


<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=bundle.getString("lb_terminology")%></SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=100%><div>
        <%
        if(deps != null&&deps.length()>0) {
            out.println(deps);
            sessionMgr.removeElement("projectsByTermbaseDepended");
        }
        else {
            out.println(bundle.getString("helper_text_terminology_main"));
        }
        %>
    <div></TD>
  </TR>
</TABLE>
<FORM NAME=termbaseForm method="post">
        <table cellpadding=0 cellspacing=0 border=0 width="100%" class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="namelist" key="<%=WebAppConstants.TERMBASE_TB_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
          <amb:table bean="namelist" id="tb" key="<%=WebAppConstants.TERMBASE_TB_KEY%>"
                 dataClass="com.globalsight.terminology.TermbaseInfo" pageUrl="self"
                 emptyTableMsg="msg_no_termbases" hasFilter="true">                
            <amb:column label="checkbox"  width="20px">
                <input type="checkbox" name="radioBtn" value="<%=tb.getTermbaseId()%>"
                    onclick="enableButtons();buttonManagement()">
            </amb:column>     
            <amb:column label="lb_name" sortBy="<%=TermbaseInfoComparator.NAME%>" filter="tbNameFilter"  filterValue="<%=tbNameFilter%>" width="10%">  
            <%
                    if (userPermissions.getPermissionFor(Permission.TERMINOLOGY_EDIT)) { %>        
            <a href='javascript:void(0);' title="<%=bundle.getString("helper_text_tb_modify_termbase")%>"  ID="idModify" onclick="modifyTermbase('<%=tb.getTermbaseId()%>')"><%=tb.getName()%></a>
            <%  } else { %>
                    <%=tb.getName()%>
                <% } %>
            </amb:column>       
            <amb:column label="lb_description" sortBy="<%=TermbaseInfoComparator.DESC%>">
                <%=tb.getDescription()%>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=TermbaseInfoComparator.ASC_COMPANY%>" filter="tbCompanyFilter" filterValue="<%=tbCompanyFilter%>">
                <%=CompanyWrapper.getCompanyNameById(tb.getCompanyId())%>
            </amb:column>
            <% } %>
          </amb:table>

    </TD>
    </TR>
    <tr valign="top">
          <td align="right">
            <amb:tableNav bean="namelist" key="<%=WebAppConstants.TERMBASE_TB_KEY%>" pageUrl="self"  scope="10,20,50,All" showTotalCount="false"/>
          </td>
        </tr>
    </DIV>
    <TR><TD>&nbsp;</TD></TR>

    <TR>
    <TD>

    <DIV ALIGN="left">
    <amb:permission name="<%=Permission.TERMINOLOGY_STATS%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_statistics")%>"
     ID="idStatistics" onclick="showStatistics()" name="statBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_show_statistics")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_INDEXES%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_indexes") %>"
     ID="idIndexes" onclick="showIndexes()" name="indexBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_indexes")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_REMOVE%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
     ID="idRemove" onclick="removeTermbase()" name="removeBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_remove_termbase")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_DUPLICATE%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_duplicate1")%>"
     ID="idClone" onclick="cloneTermbase()" name="dupBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_clone_termbase")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_NEW%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new1")%>"
     ID="idNew" onclick="newTermbase()"
     TITLE="<%=bundle.getString("helper_text_tb_new_termbase")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_BROWSE%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_browse1")%>"
     ID="idBrowse" onclick="browseTermbase()" name="browseBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_browse_termbase")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_IMPORT%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_import1")%>"
     ID="idImport" onclick="importTermbase()" name="importBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_import_termbase")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_EXPORT%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_export1")%>"
     ID="idExport" onclick="exportTermbase()" name="exportBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_export_termbase")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_MAINTENANCE%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_maintenance") %>"
     ID="idMaintenance" onclick="maintainTermbase()" name="mainBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_maintenance") %>">
    </amb:permission>
    <amb:permission name="<%=Permission.TERMINOLOGY_INPUT_MODELS%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_input_models") %>..."
     ID="idInputModels" onclick="manageInputModels()" name="inputBtn" disabled
     TITLE="<%=bundle.getString("helper_text_tb_input_models") %>">
    </amb:permission>
    <!-- York added on 2009-03-19  //-->
    <%if(enableTBAccessControl&&isAdmin)
    {%>
    <INPUT CLASS="standardText" TYPE="BUTTON" name="usersBtn" disabled
         VALUE="<%=bundle.getString("lb_users")%>..."
         ID="idUsers" onclick="modifyUsers()"
         TITLE="<%=bundle.getString("helper_text_tb_users")%>">
    <%}%>
    <amb:permission name="<%=Permission.TERMINOLOGY_SEARCH%>" >
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_search") %>..."
     ID="idSearchTerm" onclick="searchTerms()" name="searchTermBtn"
     TITLE="<%=bundle.getString("lb_search") %>">
    </amb:permission>
    </DIV>
    </TD>
    </DIV>
  </TR>
</TABLE>
</FORM>

</BODY>
</HTML>
