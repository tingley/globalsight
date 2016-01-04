<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.company.Company,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.foundation.User,
            java.util.ResourceBundle"
    session="true"%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_home");
    String aboutUrl = "/globalsight/envoy/about/about.jsp";

    SessionManager sessionMgrWelcome = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    User userWelcome = (User) sessionMgrWelcome
            .getAttribute(WebAppConstants.USER);

    String startUrl = LinkHelper.getWebActivityURL(request, "start");
    //For GBS-1302: Activity Dashboard
    Map<String, Long> dashboardMap = (Map) request
            .getAttribute(WebAppConstants.DASHBOARD_ACTIVITY);
    Integer exportingWorkflowNumber = (Integer) request
            .getAttribute(WebAppConstants.EXPORTING_WORKFLOW_NUMBER);
    Boolean isSuperAdmin = (Boolean) request
            .getAttribute(WebAppConstants.IS_SUPER_ADMIN);
    Boolean isAdmin = (Boolean) request
            .getAttribute(WebAppConstants.IS_ADMIN);
    Boolean isSuperProjectManager = (Boolean) request
            .getAttribute(WebAppConstants.SUPER_PM_NAME);
    Boolean isProjectManager = (Boolean) request
            .getAttribute(WebAppConstants.IS_PROJECT_MANAGER);
    Integer creatingJobsNum = (Integer) request
            .getAttribute("creatingJobsNum");
    String availableUrl = "&" + WebAppConstants.TASK_STATE + "="
            + Task.STATE_ACTIVE + "&listType=stateOnly";
    String inprogressUrl = "&" + WebAppConstants.TASK_STATE + "="
            + Task.STATE_ACCEPTED + "&listType=stateOnly";
    
    boolean isEnableWorkflowStatePosts = false;
    Company company = CompanyWrapper.getCurrentCompany();
    isEnableWorkflowStatePosts = company.getEnableWorkflowStatePosts();
%>
<HTML>
<!-- This JSP is envoy/login/welcome.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "welcome";
var helpFile = "<%=bundle.getString("help_main_admin")%>";

function openWizardWindow(url)
{
  w_editor = window.open(url,'setupWindow',
    'resizable=yes,scrollbars=yes,top=150,left=50,height=200,width=700');
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
 onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" class="welcomePageContentLayer">

<P CLASS="welcomePageMainHeading"><%=title%></P>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText">
  <TR>
    <TD>
      <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" CLASS="standardText" width="900">
    <TR>
      <TD>
        <p class="helloText">
        <SCRIPT LANGUAGE="JAVASCRIPT">
            printf("<%=bundle.getString("helper_text_welcome")%>", "<%=userHeader.getUserName()%>");
        </SCRIPT>
        </p>
        <P class="helloTextDetail">
        <%=bundle.getString("helper_text_world_leading")%>
        <%
            if (hasGuidesMenu) {
        %>
        <P class="helloTextDetail">
        <%=bundle.getString("helper_text_use_links")%> 
        <P>
        <%
            }
        %>
      </TD>
    </TR>
      </TABLE>
      
    <%
            if (hasGuidesMenu) {
          %>
      <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width="900">
    <TR CLASS="welcomePageTableHeadingBasic" >
      <TD width="20"><img src="images/title.png"/></TD>
      <TD><A NAME="guides"><%=bundle.getString("lb_getting_started")%></A></TD>
    </TR>
    <TR CLASS="tableBodyHome">
      <td width="20"></td>
      <TD width="99%">
        <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" href="javascript: showGuide('fileSystem');location.replace('<%=startUrl%>');">
        <%=bundle.getString("lb_filesystem_guide")%></A><BR>
        <%
            if (b_database) {
        %> 
        <amb:permission name="<%=Permission.DATABASE_INTEGRATION%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" href="javascript: showGuide('database');location.replace('<%=startUrl%>');">
        <%=bundle.getString("lb_database_guide")%></A><BR>
        </amb:permission>
        <%
            }
        %>
      </TD>
    </TR>
          </TABLE>
      
    <%
            }
          %>
      <br>
      <div style="border:solid #ccc; border-width:1px 0 0 0;width:900px"></div>
      <amb:permission name="<%=Permission.ACTIVITY_DASHBOARD_VIEW%>">
            <div>
            <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width="900">
                <TR CLASS="activityDashboardText">
                    <TD><%=bundle.getString("lb_my_activities")%> <%=bundle.getString("lb_available")%>:
                    <A CLASS="activityDashboardNumber"
                        HREF="<%=myActivitiesUrl + availableUrl%>"><%=dashboardMap.get(Task.STATE_ACTIVE_STR)%></A></TD>
                </TR>
                <TR CLASS=activityDashboardText>
                    <TD><%=bundle.getString("lb_my_activities")%> <%=bundle.getString("lb_inprogress")%>:
                    <A CLASS="activityDashboardNumber"
                        HREF="<%=myActivitiesUrl + inprogressUrl%>"><%=dashboardMap.get(Task.STATE_ACCEPTED_STR)%></A></TD>
                </TR>
            </TABLE>
            </div>
            <% if(!isSuperAdmin && !isAdmin && !isProjectManager){%>
                <br>
            <%}%>
      </amb:permission>
      <% if(isSuperAdmin || isAdmin || isProjectManager){%>
          <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width="900">
          <TR CLASS=activityDashboardText>
            <TD><SPAN STYLE="color:red"><%=bundle.getString("lb_locales_exporting")%>: <%=exportingWorkflowNumber%></SPAN></TD>
          </TR>
          <TR CLASS=activityDashboardText>
            <TD><SPAN STYLE="color:red"><%=bundle.getString("lb_job_creating")%>: <%=creatingJobsNum %> job(s) creating</SPAN></TD>
          </TR>
          </TABLE>
          <BR>
      <%}%>
      <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width="900">
    <TR>
      <%
        int colspanCounter = 0;
        if ((hasSetupMenu))
            colspanCounter++;
        if (hasDataSourceMenu)
            colspanCounter++;
        if (hasMyJobsMenu || hasMyActivitiesMenu || hasReportsMenu
                || hasContentManagerMenu || hasVendorMenu)
            colspanCounter++;
      %>
      <TD<%if (colspanCounter > 1) {%> colspan="<%=colspanCounter%>"<%}%>>
        <TABLE  CELLSPACING="0" CELLPADDING="0" BORDER="0">
          <TR CLASS="welcomePageTableHeadingBasic">
            <TD width="20"><img src="images/title.png"/></TD>
            <TD><%=bundle.getString("lb_quick_links")%></TD>
          </TR>
        </TABLE>
      </TD>
    </TR>
    <TR CLASS="welcomePageTableHeadingBasicBlack">
    <%
        if (hasSetupMenu) {
    %>
      <TD><%=bundle.getString("lb_setup")%></TD>
    <%
        }
    %>
    <%
        if (hasDataSourceMenu) {
    %>
      <TD<%if (colspanCounter > 1) {%> style="border:solid #ccc;border-width:0 0 0 1px;padding-left: 10px"<%}%>><%=bundle.getString("lb_data_sources")%></TD>
    <%
        }
    %>
    <%
        if (hasMyJobsMenu || hasMyActivitiesMenu || hasReportsMenu
                || hasContentManagerMenu || hasVendorMenu) {
    %>
      <TD<%if (colspanCounter > 1) {%> style="border:solid #ccc;border-width:0 0 0 1px;padding-left: 10px"<%}%>><%=bundle.getString("lb_common")%></TD>
    <%
        }
    %>
    </TR>
    <%
        if (hasSetupMenu) {
    %>
    <TR CLASS="tableBodyHome">
      <TD width="32%">
<%
    if (isSuperAdmin1) {
%>
        <amb:permission name="<%=Permission.COMPANY_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=companyUrl%>"><%=bundle.getString("lb_companies")%></A><BR>
        </amb:permission>
<%
    }
%>
        <amb:permission name="<%=Permission.ATTRIBUTE_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=attributeUrl%>"><%=bundle.getString("lb_define_attribute")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.ATTRIBUTE_GROUP_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=attributeGroupUrl%>"><%=bundle.getString("lb_attribute_groups")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.LOCALE_PAIRS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=localePairsUrl%>"><%=bundle.getString("lb_locale_pairs")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.GSEDITION_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=gsEditionUrl%>"><%=bundle.getString("lb_gseditions")%></A><BR> 
        </amb:permission>
        <amb:permission name="<%=Permission.ACTIVITY_TYPES_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=activityTypesUrl%>"><%=bundle.getString("lb_activity_types")%></A><BR>
        </amb:permission>
        <%
            if (b_costing) {
        %>
        <amb:permission name="<%=Permission.CURRENCY_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=currencyUrl%>"><%=bundle.getString("lb_currency")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.RATES_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=rateUrl%>"><%=bundle.getString("lb_rates")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <%
            if (b_calendaring) {
        %>
        <%
            if (userPerms.getPermissionFor(Permission.SYS_CAL_VIEW)
                            || userPerms
                                    .getPermissionFor(Permission.USER_CAL_VIEW)
                            || userPerms
                                    .getPermissionFor(Permission.HOLIDAY_VIEW)) {
        %>
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=calendarsUrl%>"><%=bundle.getString("lb_calendars_holidays")%></A><BR>
        <%
            }
        %>
        <%
            }
        %>
        <amb:permission name="<%=Permission.PERMGROUPS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=permissionGroupsUrl%>"><%=bundle.getString("lb_permission_groups")%></A><BR>
        </amb:permission>
        <%
            if (userPerms.getPermissionFor(Permission.USERS_VIEW)
                        && (userPerms.getPermissionFor(Permission.USERS_EDIT)
                                || userPerms.getPermissionFor(Permission.USERS_REMOVE)
                                || userPerms.getPermissionFor(Permission.USERS_NEW)
                                || userPerms.getPermissionFor(Permission.USERS_IMPORT)
                                || userPerms.getPermissionFor(Permission.USERS_EXPORT))) {
        %>
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=usersUrl%>"><%=bundle.getString("lb_users")%></A><BR>
        <%
            }
        %>
        <amb:permission name="<%=Permission.TM_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=tmUrl%>"><%=bundle.getString("lb_tm")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.TMP_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=tmProfilesUrl%>"><%=bundle.getString("lb_tm_profiles")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.MTP_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=mtProfilesUrl%>"><%=bundle.getString("lb_mt_profiles")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.TERMINOLOGY_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=terminologyUrl%>"><%=bundle.getString("lb_terminology")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.PROJECTS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=projectsUrl%>"><%=bundle.getString("lb_projects")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.WORKFLOWS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=templatesUrl%>"><%=bundle.getString("lb_workflows")%></A><BR>
        </amb:permission>
        <%
        if(isEnableWorkflowStatePosts && (isSuperAdmin || isAdmin || isSuperProjectManager || isProjectManager)){
        %>
        <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=workflowStatePostUrl%>"><%=bundle.getString("lb_workflow_state_post_profiles")%></A><BR>
        <%} %>
        <amb:permission name="<%=Permission.LOCPROFILES_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=locProfilesUrl%>"><%=bundle.getString("lb_loc_profiles")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.SUPPORT_FILES_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=glossariesUrl%>"><%=bundle.getString("lb_supportFiles")%></A><BR>
        </amb:permission>
        <%
            if (b_snippets) {
        %>
        <amb:permission name="<%=Permission.SNIPPET_IMPORT%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=snippetImportUrl%>"><%=bundle.getString("lb_snippet_import")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <amb:permission name="<%=Permission.CVS_Servers%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=cvsServerUrl%>"><%=bundle.getString("lb_cvsservers")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.CVS_MODULES%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=cvsModuleUrl%>"><%=bundle.getString("lb_cvsmodules")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.CVS_MODULE_MAPPING%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=cvsModuleMappingUrl%>"><%=bundle.getString("lb_cvs_module_mappings")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.CVS_FILE_PROFILES%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=cvsFileProfileUrl%>"><%=bundle.getString("lb_cvs_file_profiles")%></A><BR>
        </amb:permission>

<%
    if (isSuperAdmin1) {
%>
        <amb:permission name="<%=Permission.SYSTEM_PARAMS%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=systemParametersUrl%>"><%=bundle
                                                .getString("lb_system_parameters")%></A><BR>
        </amb:permission>

        <amb:permission name="<%=Permission.UILOCALE_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=uiLocaleUrl%>"><%=bundle.getString("lb_uilocale_title")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.SYSTEM_PARAMS%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=remoteIpUrl%>"><%=bundle.getString("lb_remote_ip_webservices")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.LOGS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=logsUrl%>"><%=bundle.getString("lb_logs")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.OPERATION_LOG_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=operationLogUrl%>"><%=bundle.getString("lb_logs_operation")%></A><BR>
        </amb:permission>
        
        <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=systemActivitiesUrl%>"><%=bundle.getString("lb_system_activities")%></A>
<%
    }
%>
      </TD>
    <%
        }
    %>
    <%
        if (hasDataSourceMenu) {
    %>
      <TD width="33%"<%if (colspanCounter > 1) {%> style="border:solid #ccc;border-width:0 0 0 1px;padding-left: 10px"<%}%>>
        <amb:permission name="<%=Permission.FILE_PROFILES_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=fileProfilesUrl%>"><%=bundle.getString("lb_file_profiles")%></A><BR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.FILTER_CONFIGURATION_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=filterConfigurationUrl%>"><%=bundle.getString("lb_filter_configuration")%></A><BR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.FILE_EXT_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=fileExtensionsUrl%>"><%=bundle.getString("lb_file_extensions")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.XMLRULE_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=xmlRulesUrl%>"><%=bundle.getString("lb_xml_rules")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.XMLDTD_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=xmlDtdsUrl%>"><%=bundle.getString("lb_xml_dtds")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.SEGMENTATIONRULE_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=segmentationRulesUrl%>"><%=bundle.getString("lb_segmentation_rules")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.SGMLRULE_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=sgmlRulesUrl%>"><%=bundle.getString("lb_sgml_rules") /*"SGML Rules"*/%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.IMPORT%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=importUrl%>"><%=bundle.getString("lb_import")%></A><BR>
        </amb:permission>
        <%
            if (b_customerAccessGroup) {
        %>
        <amb:permission name="<%=Permission.CUSTOMER_UPLOAD%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=customerUploadUrl%>"><%=bundle.getString("lb_upload")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <%
            if (b_database) {
        %>
        <amb:permission name="<%=Permission.DATABASE_INTEGRATION%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=dbConnectionsUrl%>"><%=bundle.getString("lb_db_connections")%></A><BR>
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=dbProfilesUrl%>"><%=bundle.getString("lb_db_profiles")%></A><BR>
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=dbImportSettingsUrl%>"><%=bundle.getString("lb_db_import_settings")%></A><BR>
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=dbPreviewRulesUrl%>"><%=bundle.getString("lb_db_preview")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <amb:permission name="<%=Permission.EXPORT_LOC_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=exportLocationsUrl%>"><%=bundle.getString("lb_export_locations")%></A><BR>
        </amb:permission>
        <%
            if (b_vignette) {
        %>
        <amb:permission name="<%=Permission.VIGNETTE_IMPORT%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=vignetteUrl%>" target="vignetteWindow"><%=bundle.getString("lb_vignette_import")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <%
            if (b_corpusAligner) {
        %>
        <amb:permission name="<%=Permission.CORPUS_ALIGNER_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=corpusAlignerUrl%>"><%=bundle.getString("lb_corpus_aligner")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <amb:permission name="<%=Permission.CVS_OPERATE%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=cvsJobUrl%>"><%=bundle.getString("lb_cvs_job")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.RSS_READER%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=rssReaderUrl%>"><%=bundle.getString("lb_rss_reader")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.CREATE_JOB%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=createJobUrl%>"><%=bundle.getString("lb_create_job")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.CREATE_JOB_NO_APPLET%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=createZipJobUrl%>"><%=bundle.getString("lb_create_job_without_java")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.COTI_JOB%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=createCotiJobUrl%>"><%=bundle.getString("lb_coti_job")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.ELOQUA%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=eloquaUrl%>"><%=bundle.getString("lb_eloqua")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.MIND_TOUCH%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=mindtouchUrl%>"><%=bundle.getString("lb_mindtouch")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.GIT_CONNECTOR%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=gitconnectorUrl%>"><%=bundle.getString("lb_git_connector")%></A><BR>
      	</amb:permission>
        <amb:permission name="<%=Permission.BLAISE_CONNECTOR%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=blaiseConnectorUrl%>"><%=bundle.getString("lb_blaise_connector")%></A><BR>
      	</amb:permission>
      </TD>
    <%
        }
    %>
    <%
        if (hasMyJobsMenu || hasMyActivitiesMenu || hasReportsMenu
                || hasContentManagerMenu || hasVendorMenu||hasTMTBSearchMenu) {
    %>
      <TD width="33%"<%if (colspanCounter > 1) {%> style="border:solid #ccc;border-width:0 0 0 1px;padding-left: 10px"<%}%>>
        <amb:permission name="<%=Permission.JOBS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=jobsSearchUrl%>"><%=bundle.getString("lb_my_jobs")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.JOBS_GROUP%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=jobGroupsUrl%>"><%=bundle.getString("lb_my_groups")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.ACTIVITIES_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=tasksSearchUrl%>&state=-10"><%=bundle.getString("lb_my_activities")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=simpleOfflineUploadUrl%>"><%=bundle.getString("lb_offline_upload")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.ACTIVITIES_TM_SEARCH%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=tmSearchUrl%>"><%=bundle.getString("lb_tm_search2")%></A><BR>
        </amb:permission>
        <amb:permission name="<%=Permission.ACTIVITIES_TB_SEARCH%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=tbSearchUrl%>"><%=bundle.getString("permission.terminology.search")%></A><BR>
        </amb:permission>
        
        <%
            if (b_reports) {
        %>
        <%
            if (hasReportsMenu) {
        %>
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=reportsUrl%>"><%=bundle.getString("lb_reports")%></A><BR>
        <%
            }
        %>
        <%
            }
        %>
        <%
            if (b_cms) {
        %>
        <amb:permission name="<%=Permission.CONTENT_MANAGER%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="#" ONCLICK="javascript:openCms()"><%=bundle.getString("lb_cms")%></A><BR>
        </amb:permission>
        <%
            }
        %>
        <%
            if (b_vendorManagement) {
        %>
        <amb:permission name="<%=Permission.VENDORS_VIEW%>" >
          <span class="navPoint">&#183;</span> <A CLASS="welcomePageLink" HREF="<%=vmUrl%>"><%=bundle
                                                .getString("lb_vendor_management")%></A><BR>
        </amb:permission>
        <%
            }
        %>
      </TD>
    <%
        }
    %>
    </TR>
      </TABLE>
    </TD>
  </TR>
  <TR><TD>&nbsp;</TD></TR>
</TABLE>
</DIV>
</BODY>
</HTML>
