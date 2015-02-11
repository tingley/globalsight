<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="java.util.*,
             com.globalsight.everest.webapp.pagehandler.administration.config.teamsite.TeamSiteServerConstants,
             com.globalsight.everest.util.comparator.TeamSiteServerComparator,
             com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServerImpl,
             com.globalsight.cxe.entity.cms.teamsite.store.BackingStore,
             com.globalsight.everest.servlet.util.SessionManager,
             com.globalsight.everest.permission.Permission,
             com.globalsight.everest.webapp.WebAppConstants,
             com.globalsight.everest.webapp.javabean.NavigationBean,
             com.globalsight.everest.webapp.pagehandler.PageHandler,
             com.globalsight.util.resourcebundle.ResourceBundleConstants,
             com.globalsight.util.resourcebundle.SystemResourceBundle,
             com.globalsight.util.edit.EditUtil,
             com.globalsight.everest.webapp.webnavigation.LinkHelper,
             com.globalsight.everest.servlet.util.ServerProxy,
             com.globalsight.everest.servlet.EnvoyServletException,
             com.globalsight.everest.util.system.SystemConfigParamNames,
             com.globalsight.everest.util.system.SystemConfiguration,
             com.globalsight.util.GeneralException,
             java.text.MessageFormat,
             java.util.Vector,
             java.util.List,
             java.util.Locale,
             java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="create" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String title= bundle.getString("lb_teamsite_server_configuration");

    //Labels of the column titles
    String nameCol = bundle.getString("lb_teamsite_server_name");
    String osCol = bundle.getString("lb_teamsite_operating_system");
    String exportCol = bundle.getString("lb_teamsite_export_port");
    String importCol = bundle.getString("lb_teamsite_import_port");
    String proxyCol = bundle.getString("lb_teamsite_proxy_port");
    String mountCol = bundle.getString("lb_teamsite_mount");
    String homeCol = bundle.getString("lb_teamsite_home");
    String storeCol = bundle.getString("lb_teamsite_content_store");  
    String companyNameCol = bundle.getString("lb_company_name");
    String confirmRemove = bundle.getString("msg_confirm_ts_server_removal");

    //Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");
    String createButton = bundle.getString("lb_create");

    //Urls of the links on this page
    String action = TeamSiteServerConstants.ACTION;
    String selfUrl = self.getPageURL();
    String removeUrl = selfUrl + "&" + action + "=" + TeamSiteServerConstants.REMOVE_ACTION;
    String newUrl = new1.getPageURL() + "&" + action + "=" + TeamSiteServerConstants.NEW_ACTION;
    String modifyUrl = modify.getPageURL()+ "&" + action + "=" + TeamSiteServerConstants.EDIT_ACTION;
    String createUrl = create.getPageURL()+ "&" + action + "=" + TeamSiteServerConstants.CREATE_ACTION;

    // get servers, state and selected column passed by PageHandler
    List servers = (List)request.getAttribute(TeamSiteServerConstants.SERVERS); //"servers"
    int pageNum = ((Integer)request.getAttribute(TeamSiteServerConstants.PAGE_NUM)).intValue(); //"pageNum"
    int numOfPages = ((Integer)request.getAttribute(TeamSiteServerConstants.NUM_OF_PAGES)).intValue(); //"numOfPages"
    int totalServers = ((Integer)request.getAttribute(TeamSiteServerConstants.TOTAL_SERVERS)).intValue(); //"totalServers"

    //paging info
    int NUM_PER_PAGE = ((Integer)request.getAttribute(TeamSiteServerConstants.NUM_PER_PAGE)).intValue(); // "num_per_page"
    int possibleTo = pageNum * NUM_PER_PAGE;
    int to = possibleTo > totalServers ? totalServers : possibleTo;
    // total number of displayable objects.
    int listSize = servers == null ? 0 : servers.size();
    int from = (to - listSize) + 1;


    // messages
    String removeWarning = bundle.getString("jsmsg_ts_server_remove");
    //Integer sortChoice = (Integer)sessionManager.getAttribute(TeamSiteServerConstants.SORTING);
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<STYLE>
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "teamsiteServers";
var helpFile = "<%=bundle.getString("help_teamsite_new_branch")%>";

var removeUrls = new Array();
var editUrls = new Array();
var createUrls = new Array();

function submitForm(selectedButton)
{
   var checked = false;
   var selectedRadioBtn = null;
   if (TsServerForm.RadioBtn != null)
   {
      // If more than one radio button is displayed, the length attribute of the
      // radio button array will be non-zero, so find which
      // one is checked
      if (TsServerForm.RadioBtn.length)
      {
          for (i = 0; !checked && i < TsServerForm.RadioBtn.length; i++)
          {
              if (TsServerForm.RadioBtn[i].checked == true)
              {
                  checked = true;
                  selectedRadioBtn = TsServerForm.RadioBtn[i].value;
              }
          }
      }
      // If only one is displayed, there is no radio button array, so
      // just check if the single radio button is checked
      else
      {
          if (TsServerForm.RadioBtn.checked == true)
          {
              checked = true;
              selectedRadioBtn = TsServerForm.RadioBtn.value;
          }
      }
   }

   // otherwise do the following
   if (selectedButton != 'New' && !checked)
   {
       alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_ts_server_select"))%>");
       return false;
   }
   else
   {
      if (selectedButton=='Remove')
      {
         if (!confirm('<%=EditUtil.toJavascript(confirmRemove)%>')) return false;
         TsServerForm.action = removeUrls[selectedRadioBtn];
      }
      else if (selectedButton=='Edit')
      {
         TsServerForm.action = editUrls[selectedRadioBtn];
      }
      else if (selectedButton=='New')
      {
         TsServerForm.action = "<%=newUrl%>";
      }
      else if (selectedButton=='Create')
      {
         TsServerForm.action = createUrls[selectedRadioBtn];
      }
      TsServerForm.submit();
   }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
 onload="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading"><%=title%></SPAN>
<br>
<br>
<table cellspacing="0" cellpadding="0" border="0" class="standardText">
  <tr>
    <td width=500>
      <%=bundle.getString("helper_text_teamsite_servers")%>
    </td>
  </tr>
</table>
<br>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
  <TR VALIGN="TOP">
    <TD ALIGN="RIGHT">
        <%
        // Make the Paging widget
        if (listSize > 0)
        {
            Integer sortChoice = (Integer)sessionManager.getAttribute(TeamSiteServerConstants.SORTING);
            Object[] args = {new Integer(from), new Integer(to), new Integer(totalServers)};

            // "Displaying x to y of z"
            out.println(MessageFormat.format(bundle.getString("lb_displaying_records"), args));

            out.println("<BR>");
            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1) {
                // Don't hyperlink "Previous" if it's the first page
                out.print(bundle.getString("lb_previous"));
            }
            else
            {
%>
            <A HREF="<%=selfUrl%>&pageNum=<%=pageNum - 1%>&<%=TeamSiteServerConstants.SORTING%>=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A>
<%
            }

            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numOfPages; i++)
            {
                // Don't hyperlink the page you're on
                if (i == pageNum)
                {
                    out.print("<B>" + i + "</B>");
                }
                // Hyperlink the other pages
                else
                {
%>
            <A HREF="<%=selfUrl%>&pageNum=<%=i%>&<%=TeamSiteServerConstants.SORTING%>=<%=sortChoice%>"><%=i%></A>
<%
                }
                out.print(" ");
            }

            // The "Next" link
            if (to >= totalServers) {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
%>
            <A HREF="<%=selfUrl%>&pageNum=<%=pageNum + 1%>&<%=TeamSiteServerConstants.SORTING%>=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A>
<%
            }
            out.println(" &gt;");
        }
        else
        {
            out.print(bundle.getString("msg_wf_template_displaying_zero_records")+ "<BR>&nbsp;");
        }
        %>
    </TD>
  </TR>
  <TR>
    <TD>
      <FORM NAME="TsServerForm" METHOD="POST">
      <!-- Data Table -->
      <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="5" CLASS="list">
	<TR CLASS="tableHeadingBasic" VALIGN="BOTTOM"
	  STYLE="padding-bottom: 3px;">
	  <TD>&nbsp;</TD>
	  <TD STYLE="padding-right: 10px;"><A CLASS="sortHREFWhite"
	    HREF="<%=selfUrl%>&<%=TeamSiteServerConstants.SORTING%>=<%=TeamSiteServerComparator.NAME%>">
            <%=nameCol%></A>
	  </TD>
	  <TD STYLE="padding-right: 10px;"><A CLASS="sortHREFWhite"
            HREF="<%=selfUrl%>&<%=TeamSiteServerConstants.SORTING%>=<%=TeamSiteServerComparator.OS%>">
            <%=osCol%></A>
	  </TD>
	  <TD STYLE="padding-right: 10px;"><%=exportCol%></A></TD>
	  <TD STYLE="padding-right: 10px;"><%=importCol%></A></TD>
	  <TD STYLE="padding-right: 10px;"><%=proxyCol%></A></TD>
	  <TD STYLE="padding-right: 10px;"><A CLASS="sortHREFWhite"
	    HREF="<%=selfUrl%>&<%=TeamSiteServerConstants.SORTING%>=<%=TeamSiteServerComparator.MOUNT%>">
	    <%=mountCol%></A>
	  </TD>
	  <TD STYLE="padding-right: 10px;"><A CLASS="sortHREFWhite"
	    HREF="<%=selfUrl%>&<%=TeamSiteServerConstants.SORTING%>=<%=TeamSiteServerComparator.HOME%>">
            <%=homeCol%></A>
	  </TD>
	  <TD STYLE="padding-right: 10px;"><%=storeCol%></A></TD>

      <% if (isSuperAdmin) { %>
	  <TD STYLE="padding-right: 10px;"><A CLASS="sortHREFWhite"
	    HREF="<%=selfUrl%>&<%=TeamSiteServerConstants.SORTING%>=<%=TeamSiteServerComparator.COMPANY%>">
            <%=companyNameCol%></A>
	  </TD>
	  <% } %>

	</TR>

        <%
           int i;
           int javascript_array_index = 0;
           String tsServerId = TeamSiteServerConstants.SERVER_ID;
           for (i = 0; i < listSize; i++, javascript_array_index++)
           {
              String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
              TeamSiteServerImpl tss = (TeamSiteServerImpl)servers.get(i);
              String tsName = tss.getName();
              String desc = (tss.getDescription() == null) ? "" :
	        tss.getDescription();
              String os = tss.getOS();
              int exportPort = tss.getExportPort();
              int importPort = tss.getImportPort();
              int proxyPort = tss.getProxyPort();
              String mountDir = tss.getMount();
              String homeDir = tss.getHome();
              Vector store = new Vector(ServerProxy
                  .getTeamSiteServerPersistenceManager()
                  .getBackingStoresByTeamSiteServer(tss));

              String companyName =
                  ServerProxy.getJobHandler()
                             .getCompanyById(tss.getCompanyId())
                             .getCompanyName();

              String editURL = modifyUrl + "&" + tsServerId + "=" + tss.getId();
              String removeURL = removeUrl + "&" + tsServerId + "=" + tss.getId();
              String createURL = createUrl + "&" + tsServerId + "=" + tss.getId();
        %>
	<TR STYLE="padding-bottom: 5px; padding-top: 5px;" VALIGN=TOP
	  BGCOLOR="<%=color%>">
	  <TD>
	    <INPUT TYPE=RADIO NAME=RadioBtn VALUE="<%=javascript_array_index%>">
	  </TD>
	  <SCRIPT>
	  removeUrls[<%=javascript_array_index%>] = "<%=removeURL%>";
	  editUrls[<%=javascript_array_index%>] = "<%=editURL%>";
	  createUrls[<%=javascript_array_index%>] = "<%=createURL%>";
	  </SCRIPT>
	  <TD><SPAN CLASS=standardText><%=tsName%></SPAN></TD>
	  <TD><SPAN CLASS=standardText><%=os%></SPAN></TD>
	  <TD><SPAN CLASS=standardText><%=exportPort%></SPAN></TD>
	  <TD><SPAN CLASS=standardText><%=importPort%></SPAN></TD>
	  <TD><SPAN CLASS=standardText><%=proxyPort%></SPAN></TD>
	  <TD><SPAN CLASS=standardText><%=mountDir%></SPAN></TD>
	  <TD><SPAN CLASS=standardText><%=homeDir%></SPAN></TD>
	  <TD>
	    <SELECT NAME="sField" CLASS="standardText">
	      <%
	      for (int z = 0; z < store.size(); z++)
	      {
	        BackingStore bs = (BackingStore)store.elementAt(z);
	      %>
	      <OPTION VALUE="<%=bs.getId()%>"><%=bs.getName()%></OPTION>
	      <%}%>
	    </SELECT>
	  </TD>
	  <% if (isSuperAdmin) { %>
	  <TD><SPAN CLASS=standardText><%=companyName%></SPAN></TD>
	  <% } %>
	</TR>
	<%
	}
	%>
      </TABLE>
      <!-- End Data Table -->
    </TD>
  </TR>

  <TR><TD>&nbsp;</TD></TR>

  <TR>
    <TD>
    <amb:permission name="<%=Permission.TEAMSITE_SERVER_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>"
      onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.TEAMSITE_SERVER_CREATE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=createButton%>"
      onclick="submitForm('Create');">
    </amb:permission>
    <amb:permission name="<%=Permission.TEAMSITE_SERVER_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..."
      onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.TEAMSITE_SERVER_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..."
      onclick="submitForm('New');">
    </amb:permission>
    </TD>
  </TR>
</TABLE>
</FORM>
</BODY>
</HTML>
