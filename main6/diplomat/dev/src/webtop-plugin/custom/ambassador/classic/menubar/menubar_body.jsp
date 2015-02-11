<%
//
%>
<%@ page import="com.documentum.web.form.Form,
java.util.Enumeration" %>
<%@ page import="com.documentum.web.form.Control" %>
<%@ page import="com.documentum.web.common.ArgumentList" %>
<html>
<head>
<dmf:webform/>
<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
<script>
function onClickHelp()
{
fireClientEvent("InvokeHelp");
}
</script>
</head>
<%
Form form = (Form)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
boolean bAccessibleParam = form.isAccessible();
String strBodyOptions = null;
String strMenugroupTableOptions = null;
if (!bAccessibleParam)
{
strBodyOptions = "class='menubarBackground' marginheight='1' marginwidth='8' leftmargin='8' rightmargin='0' topmargin='1' bottommargin='0'";
strMenugroupTableOptions = "height='100%' border='0' cellpadding='0' cellspacing='0'";
}
else
{
strBodyOptions = "class='contentBackground' marginheight='0' marginwidth='0' topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'";
strMenugroupTableOptions = "width='100%' class='contentBackground' border='0' cellpadding='0' cellspacing='10'";
}
%>
<body <%=strBodyOptions%> >
<dmf:form>
<dmf:panel name='accessheaderpanel'>
<table width='100%' border='0' cellpadding='0' cellspacing='0'>
<tr class=headerBackground>
<td height=40 colspan=2>
<table cellspacing=0 cellpadding=0 border=0>
<tr>
<td align=left>
<dmf:label name='location' cssclass='webcomponentBreadcrumb'/>
</td>
</tr>
<tr>
<td align=left>
<dmf:label name='title_label' cssclass='webcomponentTitle'/>
</td>
</tr>
</table>
</td>
</tr>
<tr class=headerBackground>
<td>
<table cellspacing=2 cellpadding=2 border=0>
<tr>
<td width='1' class='doclistHeader'><dmfx:docbaseicon size='32' name="icon"/></td>
<td>
<table cellspacing=0 cellpadding=0 border=0>
<tr><td><dmf:label name='object_name' cssclass='doclistHeader' />&nbsp;<dmf:bookmarklink name="bookmark" /></td></tr>
<tr><td><dmf:label nlsid='MSG_OBJ_TYPE' cssclass='doclistHeader' />&nbsp;<dmf:label name='r_object_type' cssclass='doclistHeader'/></td></tr>
<tr><td><dmf:label nlsid='MSG_CONTENT_TYPE' cssclass='doclistHeader' />&nbsp;<dmf:label name='a_content_type' cssclass='doclistHeader'/></td></tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
<tr height='4'><td></td></tr>
</table>
</dmf:panel>
<dmf:panel name='menubarpanel'>
<dmf:menugroup name='menugroup' imagefolder='images/menu' accessible='<%=new Boolean(bAccessibleParam).toString()%>'>
<table <%=strMenugroupTableOptions%> >
<dmf:panel name='accessheaderlinkspanel'>
<tr>
<td align='left' colspan='3'>
<dmf:label name='tableTitleLabel' nlsid='MSG_TABLE_TITLE'/>
</td>
<td align='right'>
<table border="0">
<tr>
<td>
<dmf:link name='cancelLink' nlsid='MSG_CANCEL' onclick='onCancel'/>
</td>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<td width=5>
</td>
<td>
<dmf:link name='helpLink' nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'/>
</td>
</dmfx:clientenvpanel>
</tr>
</table>
</td>
</tr>
</dmf:panel>
<tr>
<td>&nbsp;</td>
<td nowrap>
<dmf:menu name='file_menu' nlsid='MSG_FILE' width='50' >
<dmf:menu name='file_new_menu' nlsid='MSG_NEW'>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newdocument' nlsid='MSG_NEW_DOCUMENT' action='newdocument' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newnotepage' nlsid='MSG_NEW_NOTEPAGE' action='newnotepage' showifinvalid='false'/>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newprocess' nlsid='MSG_NEW_PROCESS' action='newprocess' showifinvalid='true'/>
</dmfx:clientenvpanel>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newfolder' nlsid='MSG_NEW_FOLDER' action='newfolder' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newroom' nlsid='MSG_NEW_ROOM' action='newroom' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newcabinet' nlsid='MSG_NEW_CABINET' action='newcabinet' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newuser' nlsid='MSG_NEW_USER' action='newuser' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newgroup' nlsid='MSG_NEW_GROUP' action='newgroup' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newrole' nlsid='MSG_NEW_ROLE' action='newrole' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='newpermissionset' nlsid='MSG_NEW_PERMISSION_SET' action='newacl' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newxforms' nlsid='MSG_NEW_XFORMS' action='newxforms' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newworkqueuecategory' nlsid='MSG_NEW_WORKQUEUE_CATEGORY' action='newWorkQueueCategory' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newworkqueue' nlsid='MSG_NEW_WORKQUEUE' action='newWorkQueue' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newworkqueuepolicy' nlsid='MSG_NEW_WORKQUEUE_POLICY' action='newWorkQueuePolicy' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_newworkqueuedocprofile' nlsid='MSG_NEW_WORKQUEUE_DOC_PROFILE' action='newWorkQueueDocProfile' showifinvalid='false'/>
</dmf:menu>
<dmf:menuseparator name='file_sep1'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='userimport' nlsid='MSG_USER_IMPORT' action='userimport' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_adduserorgroup' nlsid='MSG_ADD_USER_OR_GROUP' action='adduserorgroup' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_addworkqueuemember' nlsid='MSG_ADD_USER_OR_GROUP' action='addworkqueuemember' showifinvalid='false' showifdisabled = 'false'/>
<dmf:menuseparator name='file_sep2'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_refresh' nlsid='MSG_REFRESH' action='refresh' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_edit' nlsid='MSG_EDIT_FILE' action='editfile' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' id='file_view' name='file_view' nlsid='MSG_VIEW_FILE' action='view' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' id='file_saveas' name='file_saveas' nlsid='MSG_SAVE_AS' action='saveasxforms' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_checkin' nlsid='MSG_CHECKIN' action='checkin' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_checkout' nlsid='MSG_CHECKOUT' action='checkout' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_cancelcheckout' nlsid='MSG_CANCEL_CHECKOUT' action='cancelcheckout' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_importrendition' nlsid='MSG_IMPORT_RENDITION' action='importrendition' showifinvalid='true' showifdisabled='false'/>
<dmf:menuseparator name='file_sep3'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='file_import' nlsid='MSG_IMPORT' action='import' showifinvalid='false' showifdisabled = 'true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_import_externalsource' nlsid='MSG_SAVE_TO_REPOSITORY' action='importexternalresult' showifinvalid='false' showifdisabled='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_export' nlsid='MSG_EXPORT' action='export' showifinvalid='false' showifdisabled="true" />
<dmfx:actionmenuitem dynamic='singleselect' name='file_exportrendition' nlsid='MSG_EXPORT' action='exportrendition' showifinvalid='false' showifdisabled="true" />
<dmfx:actionmenuitem dynamic='multiselect' name='file_delete' nlsid='MSG_DELETE' action='delete' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='removeuserorgroup' nlsid='MSG_REMOVE_USER_OR_GROUP' action='removeuserorgroup' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='removeworkqueuemember' nlsid='MSG_REMOVE_USER_OR_GROUP' action='removeworkqueuemember' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_send_locator' nlsid='MSG_SEND_LOCATOR' action='sendlocator' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='file_find_target' nlsid='MSG_FIND_TARGET' action='findtarget' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_govern' nlsid='MSG_ADD_TO_ROOM' action='govern' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='file_ungovern' nlsid='MSG_REMOVE_FROM_ROOM' action='ungovern' showifinvalid='false'/>
<dmf:menuseparator name='file_sep4'/>
<dmfx:actionmenuitem dynamic='multiselect' name='mark_discussion_read' nlsid='MSG_DISCUSSION_MARK_READ' action='markread' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='mark_discussion_unread' nlsid='MSG_DISCUSSION_MARK_UNREAD' action='markunread' showifinvalid='false'/>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<dmf:menuseparator name='file_sep5'/>
<dmf:menuitem name='file_help' nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'/>
<dmfx:actionmenuitem name='file_about' nlsid='MSG_ABOUT' action='about' showifinvalid='true'/>
<dmf:menuseparator name='file_sep6'/>
<dmfx:actionmenuitem name='file_logout' nlsid='MSG_LOGOUT' action='logout' showifinvalid='true'/>
</dmfx:clientenvpanel>
</dmf:menu>
</td>
<td nowrap>
<dmf:menu name='edit_menu' nlsid='MSG_EDIT' width='50'>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_addtoclip' nlsid='MSG_ADD_TO_CLIPBOARD' action='addtoclipboard' showifinvalid='true'/>
<dmf:menuseparator name='edit_sep1'/>
<dmfx:actionmenuitem dynamic='generic' name='tools_move' nlsid='MSG_MOVE_FILE' action='move' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='generic' name='tools_copy' nlsid='MSG_COPY_FILE' action='copy' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='generic' name='tools_link' nlsid='MSG_LINK_FILE' action='link' showifinvalid='true'/>
<dmf:menuseparator name='edit_sep2'/>
<dmfx:actionmenuitem dynamic='generic' name='tools_viewclipboard' nlsid='MSG_VIEW_CLIP' action='viewclipboard' showifinvalid='true'/>
</dmf:menu>
</td>
<td nowrap>
<dmf:menu name='view_menu' nlsid='MSG_VIEW' width='50'>
<dmf:menu name='view_properties_menu' nlsid='MSG_PROPERTIES'>
<dmfx:actionmenuitem dynamic='singleselect' name='view_info' nlsid='MSG_INFO' action='attributes' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_permissions' nlsid='MSG_PERMISSIONS' action='permissions' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_options' nlsid='MSG_ROOM_MEMBERSHIP' action='roommembers' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_options' nlsid='MSG_OPTIONS' action='options' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_history' nlsid='MSG_HISTORY' action='history' showifinvalid='true'/>
</dmf:menu>
<dmf:menuseparator name='view_sep1'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='userrenamelog' nlsid='MSG_USER_RENAME_LOG' action='userrenamelog' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='changehomedblog' nlsid='MSG_USER_CHANGE_HOME_DB_LOG' action='changehomedblog' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='grouprenamelog' nlsid='MSG_GROUP_RENAME_LOG' action='grouprenamelog' showifinvalid='false' showifdisabled = 'false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_versions' nlsid='MSG_VERSIONS' action='versions' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_relationships' nlsid='MSG_RELATIONSHIPS' action='relationships' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_renditions' nlsid='MSG_RENDITIONS' action='renditions' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_locations' nlsid='MSG_LOCATIONS' action='locations' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_assemblies' nlsid='MSG_ASSEMBLIES' action='viewassemblies' showifinvalid='true'/>
<dmf:menuseparator name='view_sep2'/>
<dmfx:actionmenuitem dynamic='singleselect' name='view_discussion' nlsid='MSG_DISCUSSION' action='showtopicaction' showifinvalid='false'/>
</dmf:menu>
</td>
<td nowrap>
<dmf:menu name='tools_menu' nlsid='MSG_TOOLS' width='50'>
<dmf:menu name='doc_lifecycle' nlsid='MSG_LIFECYCLE'>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_promotelifecycle' nlsid='MSG_PROMOTE_LIFECYCLE' action='promote' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_demotelifecycle' nlsid='MSG_DEMOTE_LIFECYCLE' action='demote' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_applylifecycle' nlsid='MSG_APPLY_LIFECYCLE' action='applylifecycle' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_removelifecycle' nlsid='MSG_REMOVE_LIFECYCLE' action='detachlifecycle' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_suspendlifecycle' nlsid='MSG_SUSPEND_LIFECYCLE' action='suspendlifecycle' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_resumelifecycle' nlsid='MSG_RESUME_LIFECYCLE' action='restorelifecycle' showifinvalid='true'/>
</dmf:menu>
<dmf:menu name='doc_vdm' nlsid='MSG_VDM'>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_tovd' nlsid='MSG_VDM_MAKE_VIRTUAL' action='makevirtual' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_todoc' nlsid='MSG_VDM_MAKE_SIMPLE' action='makesimple' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_view' nlsid='MSG_VDM_VIEW' action='viewvirtualdoc' showifinvalid='true'/>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_set_binding_rule' nlsid='MSG_VDMTOOLS_SET_BINDING_RULE' action='setbindingrule' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_modify_version_labels' nlsid='MSG_VDMTOOLS_MODIFY_VERSION_LABELS' action='modifyversionlabels' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_save_changes' nlsid='MSG_VDMTOOLS_SAVE_CHANGES' action='savechanges' showifinvalid='true' />
<dmf:menuseparator name='vdm_sep1'/>
<dmf:menu name='doc_vdm_addcomponent' nlsid='MSG_VDM_ADDCOMPONENT'>
<dmfx:actionmenuitem dynamic='generic' name='doc_vdm_addcomponent_from_clipboard' nlsid='MSG_FROM_CLIPBOARD' action='addcomponentfromclipboard' showifinvalid='false' showifdisabled='true'/>
<dmfx:actionmenuitem dynamic='generic' name='doc_vdm_addcomponent_from_selector' nlsid='MSG_FROM_FILE_SELECTOR' action='addcomponentfromfileselector' showifinvalid='false' showifdisabled='true'/>
<dmfx:actionmenuitem dynamic='generic' name='doc_vdm_addcomponent_using_new_document' nlsid='MSG_USING_NEW_DOCUMENT' action='addnewvirtualdocumentnode' showifinvalid='false' showifdisabled='true'/>
</dmf:menu>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_vdm_removecomponent' nlsid='MSG_VDM_REMOVECOMPONENT' action='removevirtualdocumentnode' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_reordercomponents' nlsid='MSG_VDMTOOLS_REORDERCOMPONENTS' action='reordervirtualdocumentnodes' showifinvalid='true'/>
<dmf:menuseparator name='vdm_sep2'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_new_assembly' nlsid='MSG_VDMTOOLS_NEW_ASSEMBLY' action='newassembly' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_freeze_assembly' nlsid='MSG_VDMTOOLS_FREEZE_ASSEMBLY' action='freezeassembly' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='doc_vdm_unfreeze_assembly' nlsid='MSG_VDMTOOLS_UNFREEZE_ASSEMBLY' action='unfreezeassembly' showifinvalid='true'/>
</dmfx:clientenvpanel>
</dmf:menu>
<dmf:menu name='tools_workflow' nlsid='MSG_WORKFLOW'>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_startworkflownotemplate' nlsid='MSG_START_WORKFLOW'  action='startworkflownotemplate' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_startworkflowfromdoc' nlsid='MSG_START_WORKFLOW_FROM_DOC'  action='startworkflowfromdoc' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_startworkflow' nlsid='MSG_START_WORKFLOW' action='startworkflow' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_abortworkflow' nlsid='MSG_STOP_WORKFLOW' action='abortworkflow' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_haltworkflow' nlsid='MSG_PAUSE_WORKFLOW' action='haltworkflow' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_resumeworkflow' nlsid='MSG_RESUME_WORKFLOW' action='resumeworkflow' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_sendtodistributionlist' nlsid='MSG_WORKFLOW_QUICKFLOW' action='sendtodistributionlist' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_workflowstatus' nlsid='MSG_WORKFLOW_STATUS' action='workflowstatusclassic' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_workflowreportmain' nlsid='MSG_WORKFLOW_REPORTING' action='reportmainclassic' showifinvalid='true'/>
<dmf:menu name='tools_workflow_report_details' nlsid='MSG_WORKFLOW_REPORTING_DETAILS'>
<dmfx:actionmenuitem dynamic='singleselect' name='tools_workflowreportdetailssummary' nlsid='MSG_WORKFLOW_REPORT_DETAILS_SUMMARY' action='reportdetailssummarylist' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='singleselect' name='tools_workflowreportdetailsaudit' nlsid='MSG_WORKFLOW_REPORT_DETAILS_AUDIT' action='reportdetailsauditclassic' showifinvalid='true'/>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<dmfx:actionmenuitem dynamic='singleselect' name='tools_workflowreportdetailsmap' nlsid='MSG_WORKFLOW_REPORT_DETAILS_MAP' action='reportdetailsmap' showifinvalid='true'/>
</dmfx:clientenvpanel>
</dmf:menu>
<dmf:menu name='tools_workflow_historical_report' nlsid='MSG_WORKFLOW_HISTORICAL_REPORT'>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_workflow_historicalreport_process' nlsid='MSG_WORKFLOW_HISTORICAL_PROCESS_REPORT' action='historicalprocessreport' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='tools_workflow_historicalreport_user' nlsid='MSG_WORKFLOW_HISTORICAL_USER_REPORT' action='historicaluserreport' showifinvalid='true'/>
</dmf:menu>
</dmf:menu>
<dmf:menu name='tools_queue_management' nlsid='MSG_QUEUE_MANAGEMENT'>
<dmfx:actionmenuitem dynamic='genericnoselect' name='queuemonitorlist' action='queuemonitorlist' nlsid='MSG_QUEUE_MONITOR' showifdisabled='false' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='managequeueinbox' action='managequeueinbox' nlsid='MSG_SHOW_TASKS' showifdisabled='true' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='usersworkload' action='processorworkloadinbox' nlsid='MSG_SHOW_TASKS' showifdisabled='true' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='genericnoselect' name='get_work' action='get_work' nlsid='MSG_GET_WORK' showifdisabled='true' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='assign_queued_task' action='assign_queued_task' nlsid='MSG_ASSIGN_QUEUED_TASK' showifdisabled='false' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='unassign_queued_task' action='unassign_queued_task' nlsid='MSG_UNASSIGN_QUEUED_TASK' showifdisabled='true' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='reassign_queued_task' action='reassign_queued_task' nlsid='MSG_REASSIGN_QUEUED_TASK' showifdisabled='true' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='move_queued_task' action='move_queued_task' nlsid='MSG_MOVE_QUEUED_TASK' showifdisabled='false' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='suspend_queued_task' action='suspend_queued_task' nlsid='MSG_SUSPEND_QUEUED_TASK' showifdisabled='true' showifinvalid='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='unsuspend_queued_task' action='unsuspend_queued_task' nlsid='MSG_UNSUSPEND_QUEUED_TASK' showifdisabled='true' showifinvalid='false'/>
</dmf:menu>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_subscribe' nlsid='MSG_SUBSCRIBE' action='subscribe' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_unsubscribe' nlsid='MSG_UNSUBSCRIBE' action='unsubscribe' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_registerevents' nlsid='MSG_TURN_ON_NOTIFICATION' action='registerevents' showifinvalid='false' showifdisabled='true'/>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_unregisterevents' nlsid='MSG_TURN_OFF_NOTIFICATION' action='unregisterevents' showifinvalid='false' showifdisabled='true'/>
<dmf:menuseparator name='tools_sep1'/>
<dmfx:actionmenuitem dynamic='multiselect' name='userchangestate' nlsid='MSG_USER_CHANGE_STATE' action='userchangestate' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='userchangehomedocbase' nlsid='MSG_USER_CHANGE_HOME_DOCBASE' action='changehomedocbase' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='reassignuser' nlsid='MSG_REASSIGN_USER' action='reassignuser' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='grouprename' nlsid='MSG_GROUP_REASSIGN' action='groupreassign' showifinvalid='false' showifdisabled='false'/>
<dmf:menu name='doc_create_renderition' nlsid='MSG_TRANSFORM'>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_createpdfrendition' nlsid='MSG_CREATE_PDF_RENDITION' action='createpdfrendition' showifinvalid='true' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_createhtmlrendition' nlsid='MSG_CREATE_HTML_RENDITION' action='createhtmlrendition' showifinvalid='true' showifdisabled='false'/>
<dmf:menuseparator name='file_sep3'/>
<dmfx:actionmenuitem dynamic='multiselect' name='doc_transformation' nlsid='MSG_MEDIA_TRANSFORM' action='transformation' showifinvalid='false' showifdisabled='false'/>
<dmfx:actionmenuitem dynamic='singleselect' name='rendition_transformation' nlsid='MSG_MEDIA_TRANSFORM' action='rendition_transformation' showifinvalid='false' showifdisabled="true" />
</dmf:menu>
<dmfx:actionmenuitem dynamic='multiselect'  name='tools_submitforcategorization' nlsid='MSG_SUBMIT_FOR_CATEGORIZATION' action='submitforcategorization' showifinvalid='true'/>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<dmfx:actionmenuitem dynamic='generic'  name='tools_newwindow' nlsid='MSG_NEWWINDOW' action='newwindow' showifinvalid='true'/>
</dmfx:clientenvpanel>
</dmf:menu>
</td>
<td nowrap>
<dmf:menu name='globalsight_menu' nlsid='MSG_GLOBALSIGHT' width='50'>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_translate' nlsid='MSG_TRANSLATE' action='translate' showifinvalid='true'/>
<dmfx:actionmenuitem dynamic='multiselect' name='tools_canceltranslation' nlsid='MSG_CANCEL_TRANSLATION' action='canceltranslation' showifinvalid='true'/>
</dmf:menu>
</td>
</tr>
</table>
<dmf:panel name='accessfooterpanel'>
<table class='contentBackground' border='0' cellpadding='0' cellspacing='0'>
<tr>
<td width=5>
</td>
<td>
<dmf:button name='cancelButton' cssclass='buttonLink' nlsid='MSG_CANCEL' onclick='onCancel'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_CANCEL_TIP'/>
</td>
<td width=5>
</td>
<td>
<dmfx:clientenvpanel environment='portal' reversevisible='true'>
<dmf:button name='helpButton' cssclass="buttonLink" nlsid='MSG_HELP' onclick='onClickHelp' runatclient='true'
height='16' imagefolder='images/dialogbutton' tooltipnlsid='MSG_HELP_TIP'/>
</dmfx:clientenvpanel>
</td>
</tr>
</table>
</dmf:panel>
</dmf:menugroup>
</dmf:panel>
</dmf:form>
</body>
</html>
