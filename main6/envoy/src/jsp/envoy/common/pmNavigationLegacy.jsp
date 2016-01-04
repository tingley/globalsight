<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error_nav.jsp"
    import="java.util.*,com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.vignette.VignetteProperties,     
            java.util.ResourceBundle"
    session="true"
%>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
   ResourceBundle menuBundle = PageHandler.getBundle(session);

   //URL of the menu items
   String activityTypesUrl = LinkHelper.getWebActivityURL(request, "activities");
   String archivedJobsUrl = LinkHelper.getWebActivityURL(request, "archive");
   String calendarsUrl = LinkHelper.getWebActivityURL(request, "calendars");
   String corpusAlignerUrl = LinkHelper.getWebActivityURL(request, "aligner");
   String currencyUrl = LinkHelper.getWebActivityURL(request, "currency");
   String dbConnectionsUrl = LinkHelper.getWebActivityURL(request, "dbconnections");
   String dbImportSettingsUrl = LinkHelper.getWebActivityURL(request, "dbimportsettings");
   String dbPreviewRulesUrl = LinkHelper.getWebActivityURL(request, "dynamicURLs");
   String dbProfilesUrl = LinkHelper.getWebActivityURL(request, "dbprofiles");
   String exportLocationsUrl = LinkHelper.getWebActivityURL(request, "exportlocation");
   String fileExtensionsUrl = LinkHelper.getWebActivityURL(request, "fileextensions");
   String fileProfilesUrl = LinkHelper.getWebActivityURL(request, "fileprofiles");
   String glossariesUrl = LinkHelper.getWebActivityURL(request, "glossaries");
   String homeUrl = LinkHelper.getSystemHomeURL(request);
   String importUrl = LinkHelper.getWebActivityURL(request,"import");
   String locProfilesUrl = LinkHelper.getWebActivityURL(request, "locprofiles");
   String localePairsUrl = LinkHelper.getWebActivityURL(request, "locales");
   String logoutUrl = LinkHelper.getWebActivityURL(request, "login");
   String logsUrl = LinkHelper.getWebActivityURL(request, "viewLogs");
   String operationLogUrl = LinkHelper.getWebActivityURL(request, "operationLog");
   String myActivitiesUrl = LinkHelper.getWebActivityURL(request, "myactivities");
   String myJobsUrl = LinkHelper.getWebActivityURL(request, "workflows");
   String projectsUrl = LinkHelper.getWebActivityURL(request, "projects");
   String rateUrl = LinkHelper.getWebActivityURL(request, "rate");
   String reportsUrl = LinkHelper.getWebActivityURL(request, "reports");   
   String serviceWareUrl = LinkHelper.getWebActivityURL(request,"swimport");
   String snippetImportUrl = LinkHelper.getWebActivityURL(request, "snippetimport");
   String systemParametersUrl = LinkHelper.getWebActivityURL(request, "configuration");
   String templatesUrl = LinkHelper.getWebActivityURL(request, "templates");
   String terminologyUrl = LinkHelper.getWebActivityURL(request, "termbases");
   String tmUrl = LinkHelper.getWebActivityURL(request, "tm");
   String tmProfilesUrl = LinkHelper.getWebActivityURL(request,"tmProfiles");
   String mtProfilesUrl = LinkHelper.getWebActivityURL(request,"mtProfiles");
   String usersUrl = LinkHelper.getWebActivityURL(request, "users");
   String vmUrl = LinkHelper.getWebActivityURL(request, "vendors");
   String xmlRulesUrl = LinkHelper.getWebActivityURL(request, "xmlrules");
   String jobsPendingUrl = LinkHelper.getWebActivityURL(request, "jobsPending");
   String jobsReadyUrl =  LinkHelper.getWebActivityURL(request, "jobsReady");
   String jobsInProgressUrl =  LinkHelper.getWebActivityURL(request, "jobsInProgress");
   String jobsLocalizedUrl =  LinkHelper.getWebActivityURL(request, "jobsLocalized");
   String jobsExportedUrl =  LinkHelper.getWebActivityURL(request, "jobsExported");
   String jobsArchivedUrl =  LinkHelper.getWebActivityURL(request, "jobsArchived");
   String tasksAvailableUrl = myActivitiesUrl + "&" + WebAppConstants.TASK_STATE + 
           "=" + Task.STATE_ACTIVE;
   String tasksInProgressUrl = myActivitiesUrl + "&" + WebAppConstants.TASK_STATE +
           "=" + Task.STATE_ACCEPTED;
   String tasksFinishedUrl = myActivitiesUrl + "&" + WebAppConstants.TASK_STATE +
           "=" + Task.STATE_COMPLETED;
   String tasksRejectedUrl = myActivitiesUrl + "&" + WebAppConstants.TASK_STATE +
           "=" + Task.STATE_REJECTED;
   
   //this URL is not served by the ControlServlet. It is an external link
   String vignetteUrl = "";
   try 
   {
      VignetteProperties vp = VignetteProperties.getInstance();
      vignetteUrl = vp.uiURL;
   }
   catch (Exception e) {}
%>



<DIV ID="navigation" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 68px; LEFT: 0px;">

<SCRIPT language=JavaScript1.2>

// Setup Menu 
arMenu2 = new Array(
    113, // Menu Width
    <%= menuBundle.getString("nav_displacement_setup") %>, // Distance from the left edge
    85,  // Distance from the top of the screen

    "","",

    "","",

    "","",

    "<%= menuBundle.getString("lb_locale_pairs") %>",
    "<%= localePairsUrl %>",
    0,

    "<%= menuBundle.getString("lb_activity_types") %>",
    "<%= activityTypesUrl %>",
    0,

    <% if(b_costing) { %>
    "<%= menuBundle.getString("lb_currency") %>",
    "<%= currencyUrl %>",
    0,

    "<%= menuBundle.getString("lb_rates") %>",
    "<%= rateUrl %>",
    0,
    <%}%>

    <% if (b_calendaring) { %>
    "<%= menuBundle.getString("lb_calendars_holidays") %>",
    "<%= calendarsUrl %>",
    0,
    <%}%>

    "<%= menuBundle.getString("lb_users") %>",
    "<%= usersUrl %>",
    0,

    "<%= menuBundle.getString("lb_tm") %>",
    "<%= tmUrl %>",
    0,

    "<%= menuBundle.getString("lb_tmProfile") %>",
    "<%= tmProfilesUrl %>",
    0,
	
	 "<%= menuBundle.getString("lb_mtProfile") %>",
    "<%= mtProfilesUrl %>",
    0,

    "<%= menuBundle.getString("lb_terminology") %>",
    "<%= terminologyUrl %>",
    0,

    "<%= menuBundle.getString("lb_projects") %>",
    "<%= projectsUrl %>",
    0,
    
    "<%= menuBundle.getString("lb_workflows") %>",
    "<%= templatesUrl %>",
    0,

    "<%= menuBundle.getString("lb_loc_profiles") %>",
    "<%= locProfilesUrl %>",
    0,

    "<%= menuBundle.getString("lb_logs_operation") %>",
    "<%= operationLogUrl %>",
    0,
    
    "<%= menuBundle.getString("lb_supportFiles") %>",
    "<%= glossariesUrl %>",
    0
    
    <% if(b_snippets) { %>
    ,
    "<%= menuBundle.getString("lb_snippet_import") %>",
    "<%= snippetImportUrl %>",
    0
    <%}%>
);

// Data Sources Menu 
arMenu1 = new Array (

    113, // Menu Width
    <%= menuBundle.getString("nav_displacement_datasources") %>, // Distance from the left edge
    85,  // Distance from the top of the screen

    "","",

    "","",

    "","",

    "<%= menuBundle.getString("lb_file_profiles") %>",
    "<%= fileProfilesUrl %>",
    0,

    "<%= menuBundle.getString("lb_file_extensions") %>",
    "<%= fileExtensionsUrl %>",
    0,

    "<%= menuBundle.getString("lb_xml_rules") %>",
    "<%= xmlRulesUrl %>",
    0,

    "<%= menuBundle.getString("lb_import") %>",
    "<%= importUrl %>",
    0<%if (b_serviceware) {%>
     ,
     "<%=menuBundle.getString("lb_serviceware_import") %>",
     "<%=serviceWareUrl%>",
    0<%}%><%if (b_database){%>
    ,
    "<%= menuBundle.getString("lb_db_connections2") %>",
    "<%= dbConnectionsUrl %>",
    0,

    "<%= menuBundle.getString("lb_db_profiles2") %>",
    "<%= dbProfilesUrl %>",
    0,

    "<%= menuBundle.getString("lb_db_import_settings2") %>",
    "<%= dbImportSettingsUrl %>",
    0,

    "<%= menuBundle.getString("lb_db_preview") %>",
    "<%= dbPreviewRulesUrl %>",
    0
    <%}%>
    <%if (b_vignette){%>
    ,
    "<%= menuBundle.getString("lb_vignette_import") %>",
    "javascript: window.open('<%= vignetteUrl %>','vignetteWindow')",
    0            
    <%}%>
    ,
    "<%= menuBundle.getString("lb_export_locations") %>",
    "<%= exportLocationsUrl %>",
    0
    <%if (b_corpusAligner){%>
    ,
    "Corpus Aligner",
    "<%= corpusAlignerUrl%>",
    0
    <%}%>
);

// Guides
arMenu3 = new Array(
    113, // Menu Width
    <%= menuBundle.getString("nav_displacement_guides") %>, // Distance from the left edge
    85,  // Distance from the top of the screen

    "","",

    "","",

    "","",

    "<%= menuBundle.getString("lb_filesystem") %>",
    "javascript: showGuide('fileSystem');",
    0<%if (b_database) {%>
    ,
    "<%= menuBundle.getString("lb_database") %>",
    "javascript: showGuide('database');",
    0
    <%}%>
);
      

// My Jobs
arMenu4 = new Array(
    113, // Menu Width
    <%= menuBundle.getString("nav_displacement_myjobs") %>, // Distance from the left edge
    85,  // Distance from the top of the screen

    "","",

    "","",

    "","",

    "<%= menuBundle.getString("lb_pending") %>",
    "<%= jobsPendingUrl %>",
    0,

    "<%= menuBundle.getString("lb_ready") %>",
    "<%= jobsReadyUrl %>",
    0,

    "<%= menuBundle.getString("lb_inprogress") %>",
    "<%= jobsInProgressUrl %>",
    0,

    "<%= menuBundle.getString("lb_localized") %>",
    "<%= jobsLocalizedUrl %>",
    0,

    "<%= menuBundle.getString("lb_exported") %>",
    "<%= jobsExportedUrl %>",
    0,

    "<%= menuBundle.getString("lb_archived") %>",
    "<%= jobsArchivedUrl %>",
    0
);

// My Activities
arMenu5 = new Array(
    113, // Menu Width
    <%= menuBundle.getString("nav_displacement_myactivities") %>, // Distance from the left edge
    85,  // Distance from the top of the screen

    "","",

    "","",

    "","",

    "<%= menuBundle.getString("lb_available") %>",
    "<%= tasksAvailableUrl %>",
    0,

    "<%= menuBundle.getString("lb_inprogress") %>",
    "<%= tasksInProgressUrl %>",
    0,

    "<%= menuBundle.getString("lb_finished") %>",
    "<%= tasksFinishedUrl %>",
    0,

    "<%= menuBundle.getString("lb_rejected") %>",
    "<%= tasksRejectedUrl %>",
    0
);
</SCRIPT>


    <TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
        <TR CLASS="header2">
            <TD HEIGHT="20">
                <!-- Nav links table -->
                <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
                    <TR>
                        <TD HEIGHT="20">&nbsp;&nbsp;&nbsp;</TD>
                        <TD HEIGHT="20"><A CLASS="header2" onMouseOver="popUp('elMenu2',event); return true;" onMouseOut="popDown('elMenu2'); return true;" HREF="#"><%= menuBundle.getString("lb_setup") %></A></TD>
                        <TD HEIGHT="20"><IMG SRC="/globalsight/images/line_vert_white.gif" HEIGHT="20" WIDTH="1" HSPACE="5"></TD>
                        <TD HEIGHT="20"><A CLASS="header2" onMouseOver="popUp('elMenu1',event); return true;" onMouseOut="popDown('elMenu1'); return true;" HREF="#"><%= menuBundle.getString("lb_data_sources") %></A></TD>
                        <TD HEIGHT="20"><IMG SRC="/globalsight/images/line_vert_white.gif" HEIGHT="20" WIDTH="1" HSPACE="5"></TD>
                        <TD HEIGHT="20"><A CLASS="header2" onMouseOver="popUp('elMenu3',event); return true;" onMouseOut="popDown('elMenu3'); return true;" HREF="#"><%= menuBundle.getString("lb_guides") %></A></TD>
                        <TD HEIGHT="20"><IMG SRC="/globalsight/images/line_vert_white.gif" HEIGHT="20" WIDTH="1" HSPACE="5"></TD>
                        <TD HEIGHT="20"><A CLASS="header2" onMouseOver="popUp('elMenu4',event); return true;" onMouseOut="popDown('elMenu4'); return true;" HREF="<%=jobsInProgressUrl%>"><%= menuBundle.getString("lb_my_jobs") %></A></TD>
                        <TD HEIGHT="20"><IMG SRC="/globalsight/images/line_vert_white.gif" HEIGHT="20" WIDTH="1" HSPACE="5"></TD>
                        <TD HEIGHT="20"><A CLASS="header2" onMouseOver="popUp('elMenu5',event); return true;" onMouseOut="popDown('elMenu5'); return true;" HREF="<%=tasksInProgressUrl%>"><%= menuBundle.getString("lb_my_activities") %></A></TD>

                        <% if (b_reports) { %>
                        <TD HEIGHT="20"><IMG SRC="/globalsight/images/line_vert_white.gif" HEIGHT="20" WIDTH="1" HSPACE="5"></TD>
                        <TD HEIGHT="20"><A CLASS="header2" onClick="return confirmJump();" HREF="<%= reportsUrl %>" TARGET="_top"><%= menuBundle.getString("lb_reports") %></A></TD>
                        <% } %>
                        <% if (b_vendorManagement) { %>
                        <TD HEIGHT="20"><IMG SRC="/globalsight/images/line_vert_white.gif" HEIGHT="20" WIDTH="1" HSPACE="5"></TD>
                        <TD HEIGHT="20"><A CLASS="header2" onClick="return confirmJump();" HREF="<%= vmUrl %>" TARGET="_top"><%= menuBundle.getString("lb_vendor_management") %></A></TD>
                        <% } %>
                    </TR>
                    </TR>
                </TABLE>
                <!-- End nav links table --></TD>

            <TD CLASS="header2" HEIGHT="20" ALIGN="RIGHT"><A
            CLASS="header2"
            onClick="javascript:aboutWindow = window.open('/globalsight/envoy/about/about.jsp','About','HEIGHT=350,WIDTH=450,scrollbars'); return(false);"
            HREF="#"
            TARGET="_top"><%= menuBundle.getString("lb_about_system4") %></A>&nbsp;&nbsp;</TD>
        </TR>
    </TABLE>

</DIV>
