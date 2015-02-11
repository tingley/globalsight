<%@page import="com.globalsight.everest.company.CompanyWrapper"%><%@page import="com.globalsight.everest.company.Company"%><%@page import="java.text.MessageFormat"%>


<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.projecthandler.ProjectTM,
        com.globalsight.everest.util.comparator.ProjectTMComparator,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.ServerProxy,
    java.util.ResourceBundle"
    session="true"
%>
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
<jsp:useBean id="tms" scope="request" class="java.util.ArrayList" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

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

boolean isSuperAdmin = (Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN);
boolean isAdmin = (Boolean)sessionMgr.getAttribute("isAdmin");
boolean enableTMAccessControl = (Boolean)sessionMgr.getAttribute("enableTMAccessControl");
Company company = CompanyWrapper.getCompanyById(CompanyWrapper.getCurrentCompanyId());
String superCompany = CompanyWrapper.getSuperCompanyName();
boolean isShowTM3 = false;
if (superCompany.equals(company.getCompanyName()) || company.getTmVersion().getValue() == 3)
    isShowTM3 = true;
MessageFormat format = new MessageFormat(bundle.getString("msg_convert_to_tm3_confirm"));
String selectedTmName = "";
sessionMgr.removeElement("convertStatus");
%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_tm_management")%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT src="envoy/tm/management/protocol.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm")%>";
var tmVersion = 2;
var tmName = "";

function enableButtons()
{
	var id = findSelectedRadioButton();
	var flag = true;
	var flagTm3 = false;
	var canReindex = true;
	tmName = "";
<%
    long tmId = -1;
	long tm3Id = -1;
	int tmVersion = 2;
	long tmpId = -1;
	String tmName = "";
    if (tms != null && tms.size() > 0)
    {
    	Iterator tmIter = tms.iterator();
    	while (tmIter.hasNext())
    	{
        	ProjectTM projectTm = (ProjectTM) tmIter.next();
			tmpId = projectTm.getId();
			tmName = projectTm.getName();
        	boolean isRemoteTm = projectTm.getIsRemoteTm();
        	boolean isTM3 = projectTm.getTm3Id() != null;
        	if (!isTM3) {
        	    String companyId = projectTm.getCompanyId();
        	    tmVersion = CompanyWrapper.getCompanyById(companyId).getTmVersion().getValue();
        	}
			
        	if (isRemoteTm)
        	{
        	    tmId = tmpId;
        	    tm3Id = tmpId;
        	}
			if (isTM3)
				tm3Id = tmpId;
%>
            if (<%=tmId%> == id) {
              flag = false;
            }
			if (<%=tm3Id%> ==  id) {
              flagTm3 = true; 
              canReindex = false;
			}
			if (<%=tmpId%> == id) {
		      <%
		      selectedTmName = tmName;
		      %>
		      tmVersion = <%=tmVersion%>;
		      tmName = "<%=tmName%>";
		      if (tmVersion == 2)
		    	  flagTm3 = true;
			}
<%
    	}
    }
%>
    	if(TMForm.statBtn)
	{
	  if (flag == true) 
	  {
	    TMForm.statBtn.disabled = false;
	    if (TMForm.convertBtn)
            TMForm.convertBtn.disabled = flagTm3;    
	  } 
	  else
	  {
	    TMForm.statBtn.disabled = true;    
	    if (TMForm.convertBtn && flagTm3)
            TMForm.convertBtn.disabled = true;
	  }
	}
	if (TMForm.reindexBtn)
            TMForm.reindexBtn.disabled = !canReindex;
	if(!TMForm.convertBtn){
	if(TMForm.mainBtn)
	{
	  if (flag == true) 
	  {
	    TMForm.mainBtn.disabled = false;    
	  } 
	  else
	  {
	    TMForm.mainBtn.disabled = true;    
	  }
	}
	if(TMForm.importBtn)
	{
	  if (flag == true) 
	  {
	    TMForm.importBtn.disabled = false;    
	  } 
	  else
	  {
	    TMForm.importBtn.disabled = true;    
	  }
	}
	if(TMForm.exportBtn)
	{
	  if (flag == true) 
	  {
	    TMForm.exportBtn.disabled = false;    
	  } 
	  else
	  {
	    TMForm.exportBtn.disabled = true;    
	  }
	}
	if(TMForm.usersBtn)
		TMForm.usersBtn.disabled = false;
    if (TMForm.dupBtn)
        TMForm.dupBtn.disabled = false;
    if (TMForm.editBtn)
        TMForm.editBtn.disabled = false;
    if (TMForm.deleteBtn)
        TMForm.deleteBtn.disabled = false;
}
}

function findSelectedRadioButton()
{
   var id;
}

function findSelectedRadioButton()
{
   var id;

   // If there are TMs defined...
   if (TMForm.TMId)
   {
       // If more than one radio button is displayed, the length
       // attribute of the radio button array will be non-zero, so
       // find which one is checked
       if (TMForm.TMId.length)
       {
           for (i = 0; i < TMForm.TMId.length; i++)
           {
               if (TMForm.TMId[i].checked == true)
               {
                   id = TMForm.TMId[i].value;
                   break;
               }
           }
       }
       else
       {
           // If only one is displayed, there is no radio button
           // array, so just check if the single radio button is
           // checked
           if (TMForm.TMId.checked == true)
           {
               id = TMForm.TMId.value;
           }
       }
   }

   return id;
}


function newTM()
{
    window.location.href = '<%=urlNew +
      "&" + WebAppConstants.TM_ACTION +
      "=" + WebAppConstants.TM_ACTION_NEW%>';
}
function modifyUsers()
{
	var id = findSelectedRadioButton();
	TMForm.action = '<%=urlUsers +
		      "&" + WebAppConstants.TM_ACTION +
		      "=" + WebAppConstants.TM_ACTION_USERS%>';
	TMForm.submit();
}

function modifyTM()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_edit"))%>");
  }
  else
  {
	  TMForm.action = '<%=urlModify +
	      "&" + WebAppConstants.TM_ACTION +
	      "=" + WebAppConstants.TM_ACTION_MODIFY%>';
      TMForm.submit();
  }
}

function cloneTM()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_duplicate"))%>");
  }
  else
  {
	  TMForm.action = '<%=urlClone +
	      "&" + WebAppConstants.TM_ACTION +
	      "=" + WebAppConstants.TM_ACTION_CLONE%>';
      TMForm.submit();
  }
}

function removeTM()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_remove"))%>");
  }
  else
  {
	  TMForm.action = '<%=urlDelete +
	      "&" + WebAppConstants.TM_ACTION +
	      "=" + WebAppConstants.TM_ACTION_DELETE%>';
	  TMForm.submit();
//    var ok = confirm("<%=EditUtil.toJavascript(bundle.getString("jsmsg_removing_a_tm_may_be_harmful"))%>");
//    if (ok)
//    {
//    }
  }
}

function importTM()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_import"))%>");
  }
  else
  {
	  TMForm.action = '<%=urlImport +
	      "&" + WebAppConstants.TM_ACTION +
	      "=" + WebAppConstants.TM_ACTION_IMPORT%>';
      TMForm.submit();
  }
}

function exportTM()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_export"))%>");
  }
  else
  {
	  TMForm.action = '<%=urlExport +
	      "&" + WebAppConstants.TM_ACTION +
	      "=" + WebAppConstants.TM_ACTION_EXPORT%>';
      TMForm.submit();
  }
}

function reindexTM()
{
  var url = '<%= urlReindex + "&" + WebAppConstants.TM_ACTION +
    "=" + WebAppConstants.TM_ACTION_REINDEX %>';

  TMForm.action = url;
  TMForm.submit();
}

function showStatistics()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_for_statistics"))%>");
  }
  else
  {
    var url = '<%=urlStatistics +
      "&" + WebAppConstants.TM_ACTION +
      "=" + WebAppConstants.TM_ACTION_STATISTICS + 
      "&" + WebAppConstants.RADIO_TM_ID + "=" %>' + id;

    window.showModalDialog(url, null,
        'menubar:no;location:no;resizable:yes;center:yes;toolbar:no;' +
        'status:no;dialogHeight:400px;dialogWidth:400px;');
  }
}

function maintainTM()
{
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_maintain"))%>");
  }
  else
  {
	  TMForm.action = '<%=urlSearch +
	      "&" + WebAppConstants.TM_ACTION +
	      "=" + WebAppConstants.TM_ACTION_MAINTENANCE%>';
      TMForm.submit();
  }
}

function convertToTm3() {
  var id = findSelectedRadioButton();
  if (!id)
  {
    alert("<%=EditUtil.toJavascript(bundle.getString("lb_tm_select_tm_to_convert"))%>");
  }
  else
  {
    <% 
    if (tms != null && tms.size() > 0)
    {
    	Iterator tmIter = tms.iterator();
        ProjectTM projectTm = null;
    	while (tmIter.hasNext())
    	{
        	projectTm = (ProjectTM) tmIter.next();
			tmpId = projectTm.getId();
			tmName = projectTm.getName();
    %>
      if (<%=tmpId%> == id) {
          if (confirm("<%=EditUtil.toJavascript(format.format(new String[]{tmName}))%>")) {
              TMForm.action = '<%=urlConvert +
                  "&" + WebAppConstants.TM_ACTION +
                  "=" + WebAppConstants.TM_ACTION_CONVERT%>';
              TMForm.submit();
          }
      }
    <%
        }
    }
    %>
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
</SCRIPT>
</HEAD>

<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONUNLOAD="doOnUnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=bundle.getString("lb_tm_management")%></SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538>
      <%=bundle.getString("helper_text_tm_main")%>
    </TD>
  </TR>
</TABLE>



<SPAN CLASS="standardText"><B><%=bundle.getString("lb_tms")%></B></SPAN>
<FORM NAME=TMForm method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="tms" key="<%=WebAppConstants.TM_KEY%>" pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="tms" id="tm" key="<%=WebAppConstants.TM_KEY%>"
      dataClass="com.globalsight.everest.projecthandler.ProjectTM" pageUrl="self"
      emptyTableMsg="msg_no_tms" >
      <amb:column label="" width="20px">
      <input type="radio" name="<%=WebAppConstants.RADIO_TM_ID%>" value="<%=tm.getId()%>" 
      onclick="enableButtons()">
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=ProjectTMComparator.NAME%>">
      <%=tm.getName()%>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=ProjectTMComparator.DESC%>">
      <% out.print(tm.getDescription() == null ? "" : tm.getDescription()); %>
      </amb:column>
      <amb:column label="lb_domain" sortBy="<%=ProjectTMComparator.DOMAIN%>">
      <% out.print(tm.getDomain() == null ? "" : tm.getDomain()); %>
      </amb:column>
      <amb:column label="lb_organization" sortBy="<%=ProjectTMComparator.ORG%>">
      <% out.print(tm.getOrganization() == null ? "" : tm.getOrganization()); %>
      </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=ProjectTMComparator.ASC_COMPANY%>">
      <%=ServerProxy.getJobHandler().getCompanyById(Long.parseLong(tm.getCompanyId())).getCompanyName()%>
      </amb:column>
      <% } %>
      <%
      if (isShowTM3) {
      %>
      <amb:column label="lb_tm_tm3">
      <% out.print(tm.getTm3Id() == null ? "<input type='checkbox' disabled>" : "<input type='checkbox' disabled checked>"); %>
      </amb:column>
      <% } %>
      </amb:table>
    </TD>
  </TR>
  <TR><TD>&nbsp;</TD></TR>
  <TR>
    <TD>
      <P>
      <DIV ALIGN="left">
      <% if (b_corpus) { %>
    <amb:permission name="<%=Permission.TM_BROWSER%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON"
      VALUE="<%=bundle.getString("lb_corpus_browser")%>"
      ID="idCorpusBrowser" onclick="showCorpusBrowser()"
      TITLE="<%=bundle.getString("lb_corpus_browser")%>">
    </amb:permission>
      <% } %>

    <amb:permission name="<%=Permission.TM_STATS%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="statBtn" disabled
         VALUE="<%=bundle.getString("lb_statistics")%>"
         ID="idStatistics" onclick="showStatistics()"
         TITLE="<%=bundle.getString("helper_text_tm_show_statistics")%>">
    </amb:permission>

    <amb:permission name="<%=Permission.TM_MAINTENANCE%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="mainBtn" disabled
         VALUE="<%=bundle.getString("lb_maintenance")%>"
         ID="idMaintenance" onclick="maintainTM()"
         TITLE="<%=bundle.getString("helper_text_tm_maintenence")%>">
    </amb:permission>

      <% if (isSuperAdmin) { %>
	      <INPUT CLASS="standardText" TYPE="BUTTON" name="convertBtn" disabled  
	      VALUE="<%=bundle.getString("lb_tm_convert_tm3")%>"
	      ID="idConvert" onclick="convertToTm3()"
	      TITLE="<%=bundle.getString("lb_tm_convert_tm3")%>">
      <% } %>

    <amb:permission name="<%=Permission.TM_IMPORT%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="importBtn" disabled
         VALUE="<%=bundle.getString("lb_import1")%>"
         ID="idImport" onclick="importTM()"
         TITLE="<%=bundle.getString("helper_text_tm_import_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_EXPORT%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="exportBtn" disabled
         VALUE="<%=bundle.getString("lb_export1")%>"
         ID="idExport" onclick="exportTM()"
         TITLE="<%=bundle.getString("helper_text_tm_export_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_REINDEX%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="reindexBtn"
         VALUE="<%=bundle.getString("lb_reindex")%>"
         ID="idReindex" onclick="reindexTM()"
         TITLE="<%=bundle.getString("helper_text_tm_reindex_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_DUPLICATE%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="dupBtn" disabled
         VALUE="<%=bundle.getString("lb_duplicate1")%>"
         ID="idClone" onclick="cloneTM()"
         TITLE="<%=bundle.getString("helper_text_tm_clone_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_EDIT%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="editBtn" disabled
         VALUE="<%=bundle.getString("lb_edit1")%>"
         ID="idModify" onclick="modifyTM()"
         TITLE="<%=bundle.getString("helper_text_tm_modify_tm")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_DELETE%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON" name="deleteBtn" disabled
         VALUE="<%=bundle.getString("lb_tm_delete")%>"
         ID="idRemove" onclick="removeTM()"
         TITLE="<%=bundle.getString("helper_text_tm_remove")%>">
    </amb:permission>
    <amb:permission name="<%=Permission.TM_NEW%>" >
      <INPUT CLASS="standardText" TYPE="BUTTON"
         VALUE="<%=bundle.getString("lb_new1")%>"
         ID="idNew" onclick="newTM()"
         TITLE="<%=bundle.getString("helper_text_tm_new_tm")%>">
    </amb:permission>
    <%if(enableTMAccessControl&&isAdmin){%>
         <INPUT CLASS="standardText" TYPE="BUTTON" name="usersBtn" disabled
         VALUE="<%=bundle.getString("lb_users")+"..."%>"
         ID="idUsers" onclick="modifyUsers()"
         TITLE="<%=bundle.getString("helper_text_tm_users")%>">
    <%}%>
      </DIV>
    </TD>
  </TR>
</TABLE>
</FORM>
</DIV>
</BODY>
</HTML>
