<%@ page contentType="text/xml; charset=UTF-8"
         import="com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.company.CompanyWrapper,
                 com.globalsight.everest.webapp.pagehandler.administration.shutdown.ShutdownMainHandler,
                 com.globalsight.everest.workflow.EventNotificationHelper,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.util.system.SystemConfiguration,
                 com.globalsight.everest.company.CompanyThreadLocal" session="true"
%><?xml version="1.0" encoding="UTF-8"?>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<!-- The PermissionXml describes all the permissions in the system -->
<!-- and their logical hierarchy -->
<!-- This is only used by the UI to display permissions -->
<%
    String companyId = request.getParameter("companyId");
    CompanyThreadLocal.getInstance().setIdValue(companyId);
    boolean b_addDelete = false;
    boolean b_searchEnabled = false;
    boolean b_dupsEnabled = false;
    boolean b_isDell = false;
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        b_addDelete = sc.getBooleanParameter(SystemConfigParamNames.ADD_DELETE_ENABLED);
        b_searchEnabled =
            sc.getBooleanParameter(SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED);
        b_dupsEnabled =
            sc.getBooleanParameter(SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED);
        b_isDell = sc.getBooleanParameter(SystemConfigParamNames.IS_DELL);
    }
    catch (Exception ge)
    {
        // assume false
    }
%>

<permissionXml>
<category id="lb_setup">
<% if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId)) { %>
  <permission id="<%=Permission.COMPANY_VIEW%>">
     <permission id="<%=Permission.COMPANY_REMOVE%>"/>
     <permission id="<%=Permission.COMPANY_EDIT%>"/>
     <permission id="<%=Permission.COMPANY_NEW%>"/>
     <permission id="<%=Permission.COMPANY_MIGRATE%>"/>
  </permission>
<% } %>
  <permission id="<%=Permission.ATTRIBUTE_VIEW%>">
     <permission id="<%=Permission.ATTRIBUTE_NEW%>"/>
     <permission id="<%=Permission.ATTRIBUTE_EDIT%>"/>
     <permission id="<%=Permission.ATTRIBUTE_REMOVE%>"/>
  </permission>
  <permission id="<%=Permission.ATTRIBUTE_GROUP_VIEW%>">
     <permission id="<%=Permission.ATTRIBUTE_GROUP_NEW%>"/>
     <permission id="<%=Permission.ATTRIBUTE_GROUP_EDIT%>"/>
     <permission id="<%=Permission.ATTRIBUTE_GROUP_REMOVE%>"/>
  </permission>
  <permission id="<%=Permission.LOCALE_PAIRS_VIEW%>">
     <permission id="<%=Permission.LOCALE_PAIRS_REMOVE%>"/>
     <permission id="<%=Permission.LOCALE_PAIRS_NEW%>"/>
     <permission id="<%=Permission.LOCALE_PAIRS_EXPORT%>"/>
     <permission id="<%=Permission.LOCALE_PAIRS_IMPORT%>"/>
<% if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId)) { %>
     <permission id="<%=Permission.LOCALE_NEW%>"/>
<% } %>
  </permission>
  <permission id="<%=Permission.GSEDITION_VIEW%>">
     <permission id="<%=Permission.GSEDITION_REMOVE%>"/>
     <permission id="<%=Permission.GSEDITION_EDIT%>"/> 
     <permission id="<%=Permission.GSEDITION_NEW%>"/>
  </permission>
  <permission id="<%=Permission.ACTIVITY_TYPES_VIEW%>">
     <permission id="<%=Permission.ACTIVITY_TYPES_REMOVE%>"/>
     <permission id="<%=Permission.ACTIVITY_TYPES_EDIT%>"/>
     <permission id="<%=Permission.ACTIVITY_TYPES_NEW%>"/>
  </permission>
<% if (b_costing) { %>
  <permission id="<%=Permission.CURRENCY_VIEW%>">
     <permission id="<%=Permission.CURRENCY_EDIT%>"/>
     <permission id="<%=Permission.CURRENCY_NEW%>"/>
  </permission>
  <permission id="<%=Permission.RATES_VIEW%>">
     <permission id="<%=Permission.RATES_EDIT%>"/>
     <permission id="<%=Permission.RATES_NEW%>"/>
  </permission>
<% } %>
<% if (b_calendaring) { %>
  <permission id="<%=Permission.SYS_CAL_VIEW%>">
     <permission id="<%=Permission.SYS_CAL_DUP%>"/>
     <permission id="<%=Permission.SYS_CAL_DEFAULT%>"/>
     <permission id="<%=Permission.SYS_CAL_REMOVE%>"/>
     <permission id="<%=Permission.SYS_CAL_EDIT%>"/>
     <permission id="<%=Permission.SYS_CAL_NEW%>"/>
  </permission>
  <permission id="<%=Permission.USER_CAL_VIEW%>">
     <permission id="<%=Permission.USER_CAL_EDIT%>"/>
     <permission id="<%=Permission.USER_CAL_EDIT_YOURS%>"/>
  </permission>
  <permission id="<%=Permission.HOLIDAY_VIEW%>">
     <permission id="<%=Permission.HOLIDAY_REMOVE%>"/>
     <permission id="<%=Permission.HOLIDAY_EDIT%>"/>
     <permission id="<%=Permission.HOLIDAY_NEW%>"/>
  </permission>
<% } %>
  <permission id="<%=Permission.PERMGROUPS_VIEW%>">
      <permission id="<%=Permission.PERMGROUPS_DETAILS%>"/>
      <permission id="<%=Permission.PERMGROUPS_REMOVE%>"/>
      <permission id="<%=Permission.PERMGROUPS_EDIT%>"/>
      <permission id="<%=Permission.PERMGROUPS_NEW%>"/>
  </permission>
  <permission id="<%=Permission.USERS_VIEW%>">
     <permission id="<%=Permission.USERS_REMOVE%>"/>
     <permission id="<%=Permission.USERS_EDIT%>">
        <permission id="<%=Permission.USERS_EDIT_ASSIGN_ANY_PERMGROUPS%>"/>
        <permission id="<%=Permission.USERS_ACCESS_CCEMAIL%>"/>
        <permission id="<%=Permission.USERS_ACCESS_BCCEMAIL%>"/>
        <% if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId)) { %>
        <permission id="<%=Permission.SET_DEFAULT_ROLES%>"/>
        <% } %>
     </permission>
     <permission id="<%=Permission.USERS_NEW%>"/>
     <permission id="<%=Permission.USERS_PROJECT_MEMBERSHIP%>"/>
     <permission id="<%=Permission.CHANGE_OWN_PASSWORD%>"/>
     <permission id="<%=Permission.CHANGE_OWN_EMAIL%>"/>
     <permission id="<%=Permission.USERS_IMPORT%>"/>
     <permission id="<%=Permission.USERS_EXPORT%>"/>
  </permission>
  <permission id="<%=Permission.TM_VIEW%>">
     <permission id="<%=Permission.TM_STATS%>"/>
     <permission id="<%=Permission.TM_MAINTENANCE%>"/>
     <permission id="<%=Permission.TM_IMPORT%>"/>
     <permission id="<%=Permission.TM_EXPORT%>"/>
     <permission id="<%=Permission.TM_REINDEX%>"/>
     <permission id="<%=Permission.TM_DUPLICATE%>"/>
     <permission id="<%=Permission.TM_DELETE%>"/>
     <permission id="<%=Permission.TM_EDIT%>"/>
     <permission id="<%=Permission.TM_NEW%>"/>
     <permission id="<%=Permission.TM_SEARCH%>">
       <permission id="<%=Permission.TM_ADD_ENTRY%>"/>
       <permission id="<%=Permission.TM_EDIT_ENTRY%>"/>
       <permission id="<%=Permission.TM_DELETE_ENTRY%>"/>
       <permission id="<%=Permission.TM_SEARCH_ADVANCED%>"/>
     </permission>
     <permission id="<%=Permission.TM_ENABLE_TM_ATTRIBUTES%>"/>
  </permission>
  <permission id="<%=Permission.TMP_VIEW%>">
     <permission id="<%=Permission.TMP_EDIT%>"/>
     <permission id="<%=Permission.TMP_NEW%>"/>
     <permission id="<%=Permission.TMP_REMOVE%>"/>
     <permission id="<%=Permission.IN_CONTEXT_MATCH%>"/>
     <permission id="<%=Permission.SERVICE_TM_GET_ALL_TMPROFILES%>"/>
  </permission>
   <permission id="<%=Permission.MTP_VIEW%>">
     <permission id="<%=Permission.MTP_EDIT%>"/>
     <permission id="<%=Permission.MTP_NEW%>"/>
     <permission id="<%=Permission.MTP_REMOVE%>"/>
     <permission id="<%=Permission.MTP_EXPORT%>"/>
     <permission id="<%=Permission.MTP_IMPORT%>"/>
  </permission>
  <permission id="<%=Permission.TERMINOLOGY_VIEW%>">
     <permission id="<%=Permission.TERMINOLOGY_STATS%>"/>
     <permission id="<%=Permission.TERMINOLOGY_INDEXES%>"/>
     <permission id="<%=Permission.TERMINOLOGY_REMOVE%>"/>
     <permission id="<%=Permission.TERMINOLOGY_DUPLICATE%>"/>
     <permission id="<%=Permission.TERMINOLOGY_EDIT%>"/>
     <permission id="<%=Permission.TERMINOLOGY_NEW%>"/>
     <permission id="<%=Permission.TERMINOLOGY_BROWSE%>"/>
     <permission id="<%=Permission.TERMINOLOGY_IMPORT%>"/>
     <permission id="<%=Permission.TERMINOLOGY_EXPORT%>"/>
     <permission id="<%=Permission.TERMINOLOGY_MAINTENANCE%>"/>
     <permission id="<%=Permission.TERMINOLOGY_INPUT_MODELS%>"/>
     <permission id="<%=Permission.TERMINOLOGY_SEARCH%>"/>
     <permission id="<%=Permission.SERVICE_TB_CREATE_ENTRY%>"/>
     <permission id="<%=Permission.SERVICE_TB_SEARCH_ENTRY%>"/>
     <permission id="<%=Permission.SERVICE_TB_EDIT_ENTRY%>"/>
     <permission id="<%=Permission.SERVICE_TB_GET_ALL_TB%>"/>
  </permission>
  <permission id="<%=Permission.PROJECTS_VIEW%>">
    <% if (b_calendaring) { %>
     <permission id="<%=Permission.PROJECTS_IMPORT%>"/>
     <permission id="<%=Permission.PROJECTS_EXPORT%>"/>
    <% } %>
     <permission id="<%=Permission.PROJECTS_EDIT%>">
         <permission id="<%=Permission.PROJECTS_EDIT_PM%>"/>
     </permission>
     <permission id="<%=Permission.PROJECTS_NEW%>"/>
     <permission id="<%=Permission.PROJECTS_REMOVE%>"/>
  </permission>
  <permission id="<%=Permission.WORKFLOWS_VIEW%>">
     <% if (b_dupsEnabled) { %>
       <permission id="<%=Permission.WORKFLOWS_DUPLICATE%>"/>
     <% } %>
     <permission id="<%=Permission.WORKFLOWS_REMOVE%>"/>
     <permission id="<%=Permission.WORKFLOWS_EDIT%>"/>
     <permission id="<%=Permission.WORKFLOWS_NEW%>"/>
  </permission>
  <permission id="<%=Permission.LOCPROFILES_VIEW%>">
     <permission id="<%=Permission.LOCPROFILES_REMOVE%>"/>
     <% if (b_dupsEnabled) { %>
       <permission id="<%=Permission.LOCPROFILES_DUP%>"/>
     <% } %>
     <permission id="<%=Permission.LOCPROFILES_DETAILS%>"/>
     <permission id="<%=Permission.LOCPROFILES_EDIT%>"/>
     <permission id="<%=Permission.LOCPROFILES_NEW%>"/>
  </permission>
  <permission id="<%=Permission.SUPPORT_FILES_VIEW%>">
     <permission id="<%=Permission.SUPPORT_FILES_REMOVE%>"/>
     <permission id="<%=Permission.SUPPORT_FILES_UPLOAD%>"/>
  </permission>
<% if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId)) { %>
  <permission id="<%=Permission.UILOCALE_VIEW%>">
     <permission id="<%=Permission.UILOCALE_REMOVE%>"/>
     <permission id="<%=Permission.UILOCALE_DOWNLOAD_RES%>"/>
     <permission id="<%=Permission.UILOCALE_UPLOAD_RES%>"/>
     <permission id="<%=Permission.UILOCALE_SET_DEFAULT%>"/>
     <permission id="<%=Permission.UILOCALE_NEW%>"/>     
  </permission>
<%  }%>
<% if(b_snippets) { %>
  <permission id="<%=Permission.SNIPPET_IMPORT%>"/>
<%  }%>
<% if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId)) { %>
  <permission id="<%=Permission.SYSTEM_PARAMS%>"/>
  <permission id="<%=Permission.LOGS_VIEW%>"/>
  <!--  <permission id="<%=Permission.OPERATION_LOG_VIEW%>"/>-->
<% if (ShutdownMainHandler.shutdownUserInterfaceEnabled()) { %>
  <permission id="<%=Permission.SHUTDOWN_SYSTEM%>"/>
<% } %>
<% } %>
</category>
<category id="lb_project_management">
    <permission id="<%=Permission.PROJECTS_MANAGE%>"/>
    <permission id="<%=Permission.PROJECTS_MANAGE_WORKFLOWS%>"/>
    <permission id="<%=Permission.GET_ALL_PROJECTS%>"/>
    <permission id="<%=Permission.GET_PROJECTS_I_MANAGE%>"/>
    <permission id="<%=Permission.GET_PROJECTS_I_BELONG%>"/>
</category>
<category id="lb_notification_options">
  <% if (EventNotificationHelper.systemNotificationEnabled()) { %>
  <permission id="<%=Permission.ACCOUNT_NOTIFICATION_SYSTEM%>"/>
  <% } %>
  <permission id="<%=Permission.ACCOUNT_NOTIFICATION_WFMGMT%>"/>
  <permission id="<%=Permission.ACCOUNT_NOTIFICATION_GENERAL%>"/>
  <permission id="<%=Permission.ACCOUNT_NOTIFICATION_NOMATCHES%>"/>
  <permission id="<%=Permission.ACCOUNT_NOTIFICATION_REPETITIONS%>"/>
</category>
<category id="lb_data_sources">
  <permission id="<%=Permission.FILE_PROFILES_VIEW%>">
     <permission id="<%=Permission.FILE_PROFILES_REMOVE%>"/>
     <permission id="<%=Permission.FILE_PROFILES_EDIT%>"/>
     <permission id="<%=Permission.FILE_PROFILES_NEW%>"/>
     <permission id="<%=Permission.FILE_PROFILES_SEE_ALL%>"/>
  </permission>
  <permission id="<%=Permission.FILTER_CONFIGURATION_VIEW%>">
     <permission id="<%=Permission.FILTER_CONFIGURATION_REMOVE_FILTERS%>"/>
     <permission id="<%=Permission.FILTER_CONFIGURATION_ADD_FILTER%>"/>
     <permission id="<%=Permission.FILTER_CONFIGURATION_EDIT_FILTER%>"/>
     <permission id="<%=Permission.FILTER_CONFIGURATION_EXPORT_FILTERS%>"/>
     <permission id="<%=Permission.FILTER_CONFIGURATION_IMPORT_FILTERS%>"/>
  </permission>
  <permission id="<%=Permission.FILE_EXT_VIEW%>">
     <permission id="<%=Permission.FILE_EXT_NEW%>"/>
     <permission id="<%=Permission.FILE_EXT_REMOVE%>"/>
  </permission>
  <permission id="<%=Permission.XMLRULE_VIEW%>">
     <permission id="<%=Permission.XMLRULE_DUP%>"/>
     <permission id="<%=Permission.XMLRULE_EDIT%>"/>
     <permission id="<%=Permission.XMLRULE_REMOVE%>"/>
     <permission id="<%=Permission.XMLRULE_NEW%>"/>
  </permission>
  <permission id="<%=Permission.XMLDTD_VIEW%>">
     <permission id="<%=Permission.XMLDTD_EDIT%>"/>
     <permission id="<%=Permission.XMLDTD_REMOVE%>"/>
     <permission id="<%=Permission.XMLDTD_NEW%>"/>
  </permission>
  <permission id="<%=Permission.SEGMENTATIONRULE_VIEW%>">
    <permission id="<%=Permission.SEGMENTATIONRULE_NEW%>"/>
    <permission id="<%=Permission.SEGMENTATIONRULE_EDIT%>"/>
    <permission id="<%=Permission.SEGMENTATIONRULE_EXPORT%>"/>
    <permission id="<%=Permission.SEGMENTATIONRULE_REMOVE%>"/>
    <permission id="<%=Permission.SEGMENTATIONRULE_DUP%>"/>
  </permission>
  <permission id="<%=Permission.SGMLRULE_VIEW%>">
     <permission id="<%=Permission.SGMLRULE_UPLOAD%>"/>
     <permission id="<%=Permission.SGMLRULE_CREATE%>"/>
     <permission id="<%=Permission.SGMLRULE_REMOVE%>"/>
     <permission id="<%=Permission.SGMLRULE_EDIT%>"/>
  </permission>
  <permission id="<%=Permission.EXPORT_LOC_VIEW%>">
     <permission id="<%=Permission.EXPORT_LOC_REMOVE%>"/>
     <permission id="<%=Permission.EXPORT_LOC_DEFAULT%>"/>
     <permission id="<%=Permission.EXPORT_LOC_EDIT%>"/>
     <permission id="<%=Permission.EXPORT_LOC_NEW%>"/>
  </permission>
  <permission id="<%=Permission.IMPORT%>"/>
<% if (b_serviceware) { %>
  <permission id="<%=Permission.SERVICEWARE_IMPORT%>"/>
<% } %>
<% if (b_vignette) { %>
     <permission id="<%=Permission.VIGNETTE_IMPORT%>"/>
<% } %>
<% if (b_database) { %>
  <permission id="<%=Permission.DATABASE_INTEGRATION%>"/>
<% } %>
<% if (b_documentum) { %>
     <permission id="<%=Permission.DOCUMENTUM_IMPORT%>"/>
<% } %>
<% if (b_corpusAligner) { %>
  <permission id="<%=Permission.CORPUS_ALIGNER_VIEW%>">
     <permission id="<%=Permission.CORPUS_ALIGNER_CREATE%>"/>
     <permission id="<%=Permission.CORPUS_ALIGNER_DOWNLOAD%>"/>
     <permission id="<%=Permission.CORPUS_ALIGNER_UPLOAD%>"/>
  </permission>
<% } %>
<% if (b_customerAccessGroup) { %>
  <permission id="<%=Permission.CUSTOMER_UPLOAD%>"/>
  <permission id="<%=Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE%>"/>
  <% } %>
  <permission id="<%=Permission.CREATE_JOB%>"/>
  <permission id="<%=Permission.CREATE_JOB_NO_APPLET%>"/>
</category>
<category id="lb_connectors">
  <permission id="<%=Permission.BLAISE_CONNECTOR%>"/>
  <permission id="<%=Permission.COTI_JOB%>"/>
  <permission id="<%=Permission.GIT_CONNECTOR%>"/>
  <permission id="<%=Permission.MIND_TOUCH%>"/>
  <permission id="<%=Permission.ELOQUA%>"/>
  <permission id="<%=Permission.RSS_READER%>"/>
  <category id="lb_cvs">
      <permission id="<%=Permission.CVS_Servers%>">
          <permission id="<%=Permission.CVS_Servers_NEW%>"/>
          <permission id="<%=Permission.CVS_Servers_EDIT%>"/>
          <permission id="<%=Permission.CVS_Servers_REMOVE%>"/>
      </permission>
      <permission id="<%=Permission.CVS_MODULES%>">
          <permission id="<%=Permission.CVS_MODULES_NEW%>"/>
          <permission id="<%=Permission.CVS_MODULES_EDIT%>"/>
          <permission id="<%=Permission.CVS_MODULES_REMOVE%>"/>
          <permission id="<%=Permission.CVS_MODULES_CHECKOUT%>"/>
      </permission>
      <permission id="<%=Permission.CVS_MODULE_MAPPING%>">
          <permission id="<%=Permission.CVS_MODULE_MAPPING_NEW%>"/>
          <permission id="<%=Permission.CVS_MODULE_MAPPING_EDIT%>"/>
          <permission id="<%=Permission.CVS_MODULE_MAPPING_REMOVE%>"/>
      </permission>
      <permission id="<%=Permission.CVS_FILE_PROFILES%>">
          <permission id="<%=Permission.CVS_FILE_PROFILES_NEW%>"/>
          <permission id="<%=Permission.CVS_FILE_PROFILES_EDIT%>"/>
          <permission id="<%=Permission.CVS_FILE_PROFILES_REMOVE%>"/>
      </permission>
      <permission id="<%=Permission.CONNECT_TO_CVS%>"/>
      <permission id="<%=Permission.CVS_OPERATE%>"/>
  </category>
</category>
<category id="lb_job_scope">
  <permission id="<%=Permission.JOB_SCOPE_ALL%>"/>
  <permission id="<%=Permission.JOB_SCOPE_MYPROJECTS%>"/>
</category>
<category id="lb_my_jobs">
  <permission id="<%=Permission.JOBS_VIEW%>">
      <% if (b_searchEnabled) { %>
      <permission id="<%=Permission.JOBS_SEARCH_REPLACE%>"/>
      <% } %>
      <permission id="<%=Permission.JOB_CHANGE_NAME%>"/>
      <permission id="<%=Permission.JOBS_CHANGE_WFM%>"/>
      <permission id="<%=Permission.JOBS_DISCARD%>"/>
      <permission id="<%=Permission.JOB_UPDATE_LEVERAGE%>"/>
      <permission id="<%=Permission.JOB_UPDATE_WORD_COUNTS%>"/>
      <permission id="<%=Permission.JOBS_DISPATCH%>"/>
      <% if (b_addDelete) { %>
      <permission id="<%=Permission.JOBS_EXPORT_SOURCE%>"/>
      <% } %>
      <permission id="<%=Permission.JOBS_EXPORT%>"/>
      <permission id="<%=Permission.JOBS_REEXPORT%>"/>
      <permission id="<%=Permission.JOBS_DOWNLOAD%>"/>
      <permission id="<%=Permission.JOBS_EXPORT_DOWNLOAD%>"/>
      <permission id="<%=Permission.JOBS_VIEW_ERROR%>">
          <permission id="<%=Permission.JOBS_CLEAR_ERRORS%>"/>
      </permission>
      <permission id="<%=Permission.JOBS_ARCHIVE%>"/>
      <permission id="<%=Permission.JOBS_MAKE_READY%>"/>
      <permission id="<%=Permission.JOBS_RECREATE%>"/>
      <permission id="<%=Permission.JOBS_ESTIMATEDCOMPDATE%>"/>
      <permission id="<%=Permission.JOBS_ESTIMATEDTRANSLATECOMPDATE%>"/>
      <permission id="<%=Permission.JOBS_DETAILS%>">
          <permission id="<%=Permission.JOB_FILES_VIEW%>">
              <permission id="<%=Permission.JOB_FILES_EDIT%>"/>
              <permission id="<%=Permission.JOB_FILES_DOWNLOAD%>"/>
              <permission id="<%=Permission.ADD_SOURCE_FILES%>"/>
              <permission id="<%=Permission.DELETE_SOURCE_FILES%>"/>
              <permission id="<%=Permission.EDIT_SOURCE_FILES%>"/>
          </permission>
          <permission id="<%=Permission.JOB_SOURCE_WORDCOUNT_TOTAL%>"/>
          <permission id="<%=Permission.JOB_COMMENTS_VIEW%>">
              <permission id="<%=Permission.JOB_COMMENTS_EDIT%>"/>
              <permission id="<%=Permission.JOB_COMMENTS_NEW%>"/>
          </permission>
          <permission id="<%=Permission.JOB_ATTRIBUTE_VIEW%>">
              <permission id="<%=Permission.JOB_ATTRIBUTE_EDIT%>"/>
          </permission>
          <permission id="<%=Permission.JOB_COSTING_VIEW%>">
              <permission id="<%=Permission.JOB_COSTING_EDIT%>"/>
              <permission id="<%=Permission.JOB_COSTING_REEDIT%>"/>
              <permission id="<%=Permission.JOB_COSTING_REPORT%>"/>
              <permission id="<%=Permission.COSTING_EXPENSE_VIEW%>"/>
              <permission id="<%=Permission.COSTING_REVENUE_VIEW%>"/>
          </permission>
          <permission id="<%=Permission.JOB_QUOTE_VIEW%>">
              <permission id="<%=Permission.JOB_QUOTE_SEND%>"/>
              <permission id="<%=Permission.JOB_QUOTE_STATUS_VIEW%>"/>
              <permission id="<%=Permission.JOB_QUOTE_PONUMBER_VIEW%>"/>
              <permission id="<%=Permission.JOB_QUOTE_PONUMBER_EDIT%>"/>
              <permission id="<%=Permission.JOB_QUOTE_APPROVE%>"/>
          </permission>
          <permission id="<%=Permission.JOB_WORKFLOWS_VIEW%>">
          <% if (b_isDell) { %>
              <permission id="<%=Permission.JOB_WORKFLOWS_ESTREVIEWSTART%>"/>
          <% } %>
              <permission id="<%=Permission.JOB_WORKFLOWS_DISCARD%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_VIEW_ERROR%>"/>
    <% if (b_vendorManagement) { %>
              <permission id="<%=Permission.JOB_WORKFLOWS_RATEVENDOR%>"/>
    <% } %>
              <permission id="<%=Permission.JOB_WORKFLOWS_TRANSLATED_TEXT%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_ARCHIVE%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_DETAILS%>">
                  <permission id="<%=Permission.JOB_WORKFLOWS_ESTCOMPDATE%>"/>
              </permission>
              <permission id="<%=Permission.JOB_WORKFLOWS_WORDCOUNT%>">
                  <permission id="<%=Permission.JOB_WORKFLOWS_DETAIL_STATISTICS%>"/>
                  <permission id="<%=Permission.JOB_WORKFLOWS_SUMMARY_STATISTICS%>"/>
              </permission>
              <permission id="<%=Permission.JOB_WORKFLOWS_EXPORT%>">
                  <permission id="<%=Permission.JOB_WORKFLOWS_EDITEXPORTLOC%>"/>
              </permission>
              <permission id="<%=Permission.JOB_WORKFLOWS_EXPORT_DOWNLOAD%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_ADD%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_EDIT%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_DISPATCH%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_REASSIGN%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_SKIP%>"/>
              <permission id="<%=Permission.JOB_WORKFLOWS_PRIORITY%>"/>
          </permission>
          <permission id="<%=Permission.VIEW_SCORECARD%>">
              <permission id="<%=Permission.EDIT_SCORECARD%>"/>
          </permission>
      </permission>
  </permission>
  <permission id="<%=Permission.JOBS_GROUP%>">
      <permission id="<%=Permission.JOBS_NEWGROUP%>"/>
      <permission id="<%=Permission.JOBS_REMOVEGROUP%>"/>
      <permission id="<%=Permission.JOBS_ADDJOBTOGROUP%>"/>
      <permission id="<%=Permission.JOBS_REMOVEJOBFROMGROUP%>"/>
  </permission>
</category>
<category id="lb_my_activities">
  <permission id="<%=Permission.ACTIVITY_DASHBOARD_VIEW%>"/>
  <permission id="<%=Permission.ACTIVITIES_VIEW%>">
      <permission id="<%=Permission.ACTIVITIES_ACCEPT%>"/>
      <permission id="<%=Permission.ACTIVITIES_ACCEPT_ALL%>"/>
      <permission id="<%=Permission.ACTIVITIES_UPDATE_LEVERAGE%>"/>
      <permission id="<%=Permission.ACTIVITIES_BATCH_COMPLETE_ACTIVITY%>"/>
      <permission id="<%=Permission.ACTIVITIES_BATCH_COMPLETE_WORKFLOW%>"/>
      <permission id="<%=Permission.ACTIVITIES_DOWNLOAD_ALL%>">
          <permission id="<%=Permission.ACTIVITIES_DOWNLOAD_COMBINED%>"/>
      </permission>
      <permission id="<%=Permission.ACTIVITIES_REJECT_BEFORE_ACCEPTING%>"/>
      <permission id="<%=Permission.ACTIVITIES_REJECT_AFTER_ACCEPTING%>"/>
      <permission id="<%=Permission.ACTIVITIES_EXPORT%>"/>
      <permission id="<%=Permission.ACTIVITIES_EXPORT_INPROGRESS%>"/>
      <permission id="<%=Permission.ACTIVITIES_EXPORT_DOWNLOAD%>"/>
      <permission id="<%=Permission.ACTIVITIES_DOWNLOAD%>"/>
      <permission id="<%=Permission.ACTIVITIES_WORKOFFLINE%>"/>
      <permission id="<%=Permission.ACTIVITIES_UPLOAD_SUPPORT_FILES%>"/>
      <% if (b_searchEnabled) { %>
      <permission id="<%=Permission.ACTIVITIES_SEARCHREPLACE%>"/>
      <% } %>
      <permission id="<%=Permission.ACTIVITIES_FILES_VIEW%>">
          <permission id="<%=Permission.ACTIVITIES_FILES_EDIT%>"/>
      </permission>
      <permission id="<%=Permission.ACTIVITIES_JOB_COMMENTS_VIEW%>">
          <permission id="<%=Permission.ACTIVITIES_JOB_COMMENTS_EDIT%>"/>
          <permission id="<%=Permission.ACTIVITIES_JOB_COMMENTS_NEW%>"/>
          <permission id="<%=Permission.ACTIVITIES_JOB_COMMENTS_DOWNLOAD%>"/>
      </permission>
      <permission id="<%=Permission.ACTIVITIES_COMMENTS_VIEW%>">
          <permission id="<%=Permission.ACTIVITIES_COMMENTS_EDIT%>"/>
          <permission id="<%=Permission.ACTIVITIES_COMMENTS_NEW%>"/>
          <permission id="<%=Permission.ACTIVITIES_COMMENTS_JOB%>"/>  
          <permission id="<%=Permission.ACTIVITIES_COMMENTS_DOWNLOAD%>"/>
      </permission>
      <permission id="<%=Permission.ACTIVITIES_DETAIL_STATISTICS%>"/>
      <permission id="<%=Permission.ACTIVITIES_SUMMARY_STATISTICS%>"/>
      <permission id="<%=Permission.ACTIVITIES_SECONDARYTARGETFILE%>"/>
      <permission id="<%=Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY%>"/>
  </permission>
  <permission id="<%=Permission.ACTIVITIES_TM_SEARCH%>"/>
  <permission id="<%=Permission.ACTIVITIES_TB_SEARCH%>"/>
  <permission id="<%=Permission.SOURCE_PAGE_EDIT%>"/>
  <permission id="<%=Permission.COMMENT_ACCESS_RESTRICTED%>"/>
</category>
<% if (b_reports) { %>
<category id="lb_reports">
  <permission id="<%=Permission.REPORTS_MAIN%>">
    <permission id="<%=Permission.REPORTS_TM%>"/>
    <permission id="<%=Permission.REPORTS_WORD_COUNT%>"/>
    <permission id="<%=Permission.REPORTS_WF_STATUS%>"/>
    <permission id="<%=Permission.REPORTS_JOB_DETAILS%>"/>
    <permission id="<%=Permission.REPORTS_AVG_PER_COMP%>"/>
    <permission id="<%=Permission.REPORTS_MISSING_TERMS%>"/>
    <permission id="<%=Permission.REPORTS_TERM_AUDIT%>"/>
    <permission id="<%=Permission.REPORTS_COMMENTS_ANALYSIS%>"/>
    <permission id="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF%>"/>
    <permission id="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF_SIMPLE%>"/>
    <permission id="<%=Permission.REPORTS_POST_REVIEW_QA%>"/>
    <permission id="<%=Permission.REPORTS_TRANSLATIONS_EDIT%>"/>
    <permission id="<%=Permission.REPORTS_TRANSLATIONS_VERIFICATION%>"/>
    <permission id="<%=Permission.REPORTS_SCORECARD%>"/>
    <permission id="<%=Permission.REPORTS_CHARACTER_COUNT%>"/>
    <permission id="<%=Permission.REPORTS_CUSTOMIZE%>"/>
    <permission id="<%=Permission.REPORTS_COMMENT%>"/>
    <permission id="<%=Permission.REPORTS_SLA%>"/>
    <permission id="<%=Permission.REPORTS_TRANSLATION_PROGRESS%>"/>
    <permission id="<%=Permission.REPORTS_IMPLEMENTED_COMMENTS_CHECK%>"/> 
    <permission id="<%=Permission.JOB_ATTRIBUTE_REPORT%>"/>
    <category id="lb_reports_company_specific">
        <permission id="<%=Permission.REPORTS_DELL_JOB_STATUS%>"/>
        <permission id="<%=Permission.REPORTS_DELL_ACT_DUR%>"/>
        <permission id="<%=Permission.REPORTS_DELL_FILE_LIST%>"/>
        <permission id="<%=Permission.REPORTS_DELL_ONLINE_JOBS%>">
            <permission id="<%=Permission.REPORTS_DELL_ONLINE_REVIEW_STATUS%>"/>
            <permission id="<%=Permission.REPORTS_DELL_ONLINE_JOBS_RECALC%>"/>
            <permission id="<%=Permission.REPORTS_DELL_ONLINE_JOBS_ID%>"/>
        </permission>
        <permission id="<%=Permission.REPORTS_DELL_ONLINE_JOBS_FOR_IP_TRANSLATOR%>"/>
        <permission id="<%=Permission.REPORTS_DELL_VENDOR_PO%>"/>
        <permission id="<%=Permission.REPORTS_DELL_REVIEWER_VENDOR_PO%>"/>
        <permission id="<%=Permission.REPORTS_SUMMARY%>"/>
    </category>
  </permission>
  <permission id="<%=Permission.REPORTS_CUSTOM%>"/>  
  <permission id="<%=Permission.REPORTS_CUSTOM_EXTERNAL%>"/>
</category>
<% } %>
<% if (b_vendorManagement) { %>
<category id="lb_vendors">
  <permission id="<%=Permission.VENDORS_VIEW%>">
      <permission id="<%=Permission.VENDORS_NEW%>"/>
      <permission id="<%=Permission.VENDORS_EDIT%>"/>
      <permission id="<%=Permission.VENDORS_REMOVE%>"/>
      <permission id="<%=Permission.VENDORS_DETAILS%>"/>
      <permission id="<%=Permission.VENDORS_RATING_VIEW%>">
          <permission id="<%=Permission.VENDORS_RATING_EDIT%>"/>
          <permission id="<%=Permission.VENDORS_RATING_NEW%>"/>
          <permission id="<%=Permission.VENDORS_RATING_REMOVE%>"/>
      </permission>
  </permission>
  <permission id="<%=Permission.VENDORS_CUSTOMFORM%>"/>
</category>
<% } %>
<% if (b_cms) { %>
<category id="lb_cms">
  <permission id="<%=Permission.CONTENT_MANAGER%>"/>
</category>
<% } %>
</permissionXml>