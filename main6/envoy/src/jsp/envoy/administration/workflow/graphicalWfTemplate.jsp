<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler, 
                com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
                com.globalsight.calendar.CalendarManagerLocal,
                java.util.ResourceBundle"
    session="true"%>
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	ResourceBundle bundle = PageHandler.getBundle(session);

	SessionManager sessionMgr = (SessionManager) session
			.getAttribute(WebAppConstants.SESSION_MANAGER);
	Long workflowTemplateInfoId = (Long) sessionMgr
			.getAttribute(WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID);
	String iflowTemplateId = (String) sessionMgr
			.getAttribute(WorkflowTemplateConstants.TEMPLATE_ID);

	String actionType = (String) sessionMgr
			.getAttribute(WorkflowTemplateConstants.ACTION);
	boolean isEdit = actionType != null
			&& actionType.equals(WorkflowTemplateConstants.EDIT_ACTION);
	String actionParam = isEdit
			? ("&" + WorkflowTemplateConstants.ACTION + "=" + WorkflowTemplateConstants.EDIT_ACTION)
			: "";

	// button urls    
	String cancelURL = save.getPageURL() + "&"
			+ WorkflowTemplateConstants.ACTION + "="
			+ WorkflowTemplateConstants.CANCEL_ACTION;
	String previousURL = previous.getPageURL()
			+ (workflowTemplateInfoId == null ? "" : ("&"
					+ WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID
					+ "=" + workflowTemplateInfoId)) + actionParam;
	String modifyURL = save.getPageURL();

	String title = bundle.getString("lb_graphical_workflow");
	String lb_previous = bundle.getString("lb_previous");
	String lb_cancel = bundle.getString("lb_cancel");

	String wizardTitle = (String) bundle
			.getString("lb_graphical_workflow_link");

	SystemConfiguration sysConfig = SystemConfiguration.getInstance();
	boolean useSSL = sysConfig
			.getBooleanParameter(SystemConfigParamNames.USE_SSL);
	String httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
	if (useSSL == true) {
		httpProtocolToUse = WebAppConstants.PROTOCOL_HTTPS;
	} else {
		httpProtocolToUse = WebAppConstants.PROTOCOL_HTTP;
	}
	
	boolean isCalendarInstalled = CalendarManagerLocal.isInstalled();
%>
<HTML>
<!-- This JSP is: envoy/administration/workflow/graphicalWfTemplate.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>

<!-- for workflow -->
<%@ include file="/includes/workflow/l18n.js"%>
<%@ include file="/includes/workflow/template.js"%>

<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css" />
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/css/workflow/ui.css">

<SCRIPT SRC="/globalsight/jquery/jquery-1.6.4.min.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/mustache.js" type="text/javascript"></SCRIPT>

<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT SRC="/globalsight/includes/workflow/ajax.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/ui.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/line.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/menu.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/shape.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/Utils.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/toolbar.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/dialog.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/navigation.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/model.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/workflowData.js" type="text/javascript"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/workflow/workflow.js" type="text/javascript"></SCRIPT>



<!-- end -->

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_workflow")%>";
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_workflow_graphical")%>";

function cancelForm()
{
	if (confirmJump())
    {
    	if (document.layers) theForm = document.contentLayer.document.templateCancel;
    	else theForm = document.all.templateCancel;
    	theForm.submit();
    }
}

function previousForm() 
{
	if (confirmJump())
    {
    	location.replace('<%=previousURL%>');
	}
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="loadGuides()" class="standardText">
    <%@ include file="/envoy/common/header.jspIncl"%>
    <%@ include file="/envoy/common/navigation.jspIncl"%>
    <%@ include file="/envoy/wizards/guides.jspIncl"%>

    <DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
        <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
            <TR>
                <TD ALIGN="<%=gridAlignment%>">
                    <SPAN CLASS="mainHeading">
                        <%=wizardTitle%>
                    </SPAN>
                </TD>
            </TR>
            <TR>
                <TD>&nbsp;</TD>
            </TR>
            <TR>
        </table>
        <div STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 40px; LEFT: 20px; RIGHT: 20px;" class="standardText">
            <div id="info">
                 <table class="standardText">
                     <tr>
                         <td width="100"><b><%=bundle.getString("lb_name")%>:</b></td>
                         <td id="workflowName"></td>
                     </tr>
                     <tr>
                         <td><b><%=bundle.getString("lb_locale_pair")%>:</b></td>
                         <td id="workflowLocale"></td>
                     </tr>
                 </table>
            </div>
            <DIV id="bodyDiv">
                <table class="toolbar" id="toolbar">
                    <tr>
                        <td width="180px">
                            <DIV id="shapePanel">
                                <div id="shapeCanvasDiv" style="position: absolute; width: 50px; height: 50px; display: none">
                                    <CANVAS width="50" height="50" id="shapeCanvas"></CANVAS>
                                </div>


                                <div class='panel_box toolbar_button' id="button_activity" original-title='<%=bundle.getString("lb_add_activity")%>'>
                                    <canvas class='panel_item' width='50' height='50'></canvas>
                                </div>
                                <div class='panel_box toolbar_button' id="button_end" original-title='<%=bundle.getString("lb_add_end")%>'>
                                    <canvas class='panel_item' width='50' height='50'></canvas>
                                </div>
                                <div class='panel_box toolbar_button' id="button_condition" original-title='<%=bundle.getString("lb_add_condition")%>'>
                                    <canvas class='panel_item' width='50' height='50' ></canvas>
                                </div>
                            </DIV>
                        </td>
                        <td width="1px">
                            <div class="toolbar_small_devider"></div>
                        </td>
                        <td width="120px">
                            <div class='panel_box toolbar_button ' id="button_line" original-title="<%=bundle.getString("lb_add_line")%>">
                                <canvas class='panel_item' width='50' height='50'></canvas>
                            </div>
                            <div class='panel_box toolbar_button selected' id="button_point" original-title="<%=bundle.getString("lb_Select")%>">
                                <canvas class='panel_item' width='50' height='50'></canvas>
                            </div>
                        </td>
                        <td width="1px">
                            <div class="toolbar_small_devider"></div>
                        </td>
                        <td>
                            <input type="button" value='<%=bundle.getString("lb_save")%>' class="toolbar_button " style="width: 80px;" id="saveButton" />
                            <INPUT TYPE="BUTTON" NAME="<%=lb_cancel%>" VALUE="<%=lb_cancel%>" ONCLICK="cancelForm()" class="toolbar_button " style="width: 80px;">
                            <INPUT TYPE="BUTTON" NAME="<%=lb_previous%>" VALUE="<%=lb_previous%>" ONCLICK="previousForm()" class="toolbar_button " style="width: 80px;">
                        </td>
                        <td align="right" valign="top" class="">
                            <div id="navButton" class="nav ico " style="display: none"></div>
                        </td>
                    </tr>

                </table>
                <DIV id="viewport">
                    <DIV id="canvasDiv">
                        <CANVAS id="canvas" style="position: absolute;"></CANVAS>
                        <CANVAS id="snapLineCanvas" style="position: absolute;"></CANVAS>
                    </DIV>
                    <DIV class="menu" id="shape_thumb">
                        <CANVAS width="160" height="160"></CANVAS>
                        <div style="width: 160px;"></div>
                    </DIV>
                    <div id="creatingDiv" style="position: absolute; width: 450px; height: 450px; display: none;">
                        <CANVAS id="creatingCanvas"></CANVAS>
                    </div>
                </DIV>

            </DIV>

            <div id="propertiesDiv">
                <h2><%=bundle.getString("msg_edit_activity")%></h2>
                <table id="propertiesTable" class="standardText">
                    <tr class="tableRowOddTM">
                        <td width="35%">
                            <b><%=bundle.getString("lb_activity_type")%>:</b>
                        </td>
                        <td>
                            <select style="width: 100%" id="activityTypeSelect">
                                <option value="-1"><%=bundle.getString("lb_choose")%></option>
                            </select>
                        </td>
                    </tr>
                    <tr class="tableRowEvenTM">
                        <td>
                            <b><%=bundle.getString("lb_system_action")%>:</b>
                        </td>
                        <td>
                            <select style="width: 100%" id="systemActivitySelect">
                            </select>
                        </td>
                    </tr>
                    <tr class="tableRowOddTM">
                        <td>
                            <b><%=bundle.getString("lb_report_upload_check")%>:</b>
                        </td>
                        <td>
                            <input type="checkbox" id="uploadCheckbox">
                        </td>
                    </tr>
                    <tr class="tableRowEvenTM">
                        <td>
                            <b><%=bundle.getString("lb_time_accept")%>:</b>
                        </td>
                        <td>
                            <input type="text" class="inputTimeFirst" id="accept_d">
                            <%=bundle.getString("lb_abbreviation_day")%>
                            <input type="text" class="inputTime" id="accept_h">
                            <%=bundle.getString("lb_abbreviation_hour")%>
                            <input type="text" class="inputTime" id="accept_m">
                            <%=bundle.getString("lb_abbreviation_minute")%>
                        </td>
                    </tr>
                    <tr class="tableRowOddTM">
                        <td>
                            <b><%=bundle.getString("lb_time_complete")%>:</b>
                        </td>
                        <td>
                            <input type="text" class="inputTimeFirst" id="complete_d">
                            <%=bundle.getString("lb_abbreviation_day")%>
                            <input type="text" class="inputTime" id="complete_h">
                            <%=bundle.getString("lb_abbreviation_hour")%>
                            <input type="text" class="inputTime" id="complete_m">
                            <%=bundle.getString("lb_abbreviation_minute")%>
                        </td>
                    </tr>
                    <tr class="tableRowEvenTM">
                        <td>
                            <b><%=bundle.getString("lb_Overdue_PM")%>:</b>
                        </td>
                        <td>
                            <input type="text" class="inputTimeFirst" id="overduePM_d">
                            <%=bundle.getString("lb_abbreviation_day")%>
                            <input type="text" class="inputTime" id="overduePM_h">
                            <%=bundle.getString("lb_abbreviation_hour")%>
                            <input type="text" class="inputTime" id="overduePM_m">
                            <%=bundle.getString("lb_abbreviation_minute")%>
                        </td>
                    </tr>
                    <tr class="tableRowOddTM">
                        <td>
                            <b><%=bundle.getString("lb_Overdue_user")%>:</b>
                        </td>
                        <td>
                            <input type="text" class="inputTimeFirst" id="overdueUser_d">
                            <%=bundle.getString("lb_abbreviation_day")%>
                            <input type="text" class="inputTime" id="overdueUser_h">
                            <%=bundle.getString("lb_abbreviation_hour")%>
                            <input type="text" class="inputTime" id="overdueUser_m">
                            <%=bundle.getString("lb_abbreviation_minute")%>
                        </td>
                    </tr>
                    <tr class="tableRowEvenTM">
                        <td>
                            <b><%=bundle.getString("lb_participant")%>:</b>
                        </td>
                        <td>
                            <select style="width: 100%" id="dialogUserSelect">
                                <option value="0"><%=bundle.getString("lb_all_qualified_users")%></option>
                                <%if (isCalendarInstalled){ %>
                                <option value="-1"><%=bundle.getString("lb_users_completed")%></option>
                                <option value="-2"><%=bundle.getString("lb_users_earliest")%></option>
                                <%} %>>
                                <option value="1"><%=bundle.getString("lb_user_select")%></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <div width="100%" style="height: 180px; padding: 10px; overflow: auto; display: none" id="dialogUserTable">
                                <table width="100%" class="propertiesTable2 standardText" id="userTable">
                                    <tr class="tableHeadingBasicTM">
                                        <td>
                                            <input type="checkbox" id="selectAllUserCheckbox">
                                        </td>
                                        <td><%=bundle.getString("lb_first_name")%></td>
                                        <td><%=bundle.getString("lb_last_name")%></td>
                                        <td><%=bundle.getString("lb_user_name")%></td>
                                    </tr>
                                </table>
                            </div>
                        </td>
                    </tr>
                    <tr class="tableRowOddTM">
                        <td>
                            <b><%=bundle.getString("lb_internal_costing_rate_selection")%>:</b>
                        </td>
                        <td>
                            <input type="radio" name="internalCostCriteria" value="1">
                            <span class="standardText"><%=bundle.getString("lb_use_only_selected_rate")%></span>
                            <input type="radio" name="internalCostCriteria" value="2">
                           <%=bundle.getString("lb_use_selected_rate_until_acceptance")%> 
                        </td>
                    </tr>
                    <tr class="tableRowEvenTM">
                        <td>
                            <b> <%=bundle.getString("lb_expense_rate")%>:</b>
                        </td>
                        <td>
                            <select style="width: 50%" id="internalCostRate"></select>
                        </td>
                    </tr>
                    <tr class="tableRowOddTM">
                        <td>
                            <b><%=bundle.getString("lb_revenue_rate")%>:</b>
                        </td>
                        <td>
                            <select style="width: 50%" id="billingChargeRate"></select>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2" style="padding-top: 10px;"></td>
                    </tr>
                </table>
            </div>
            <div id="navDiv" style="display: none">
                <div id="navRectDiv"></div>
                <CANVAS id="navCanvas"></CANVAS>
            </div>




            <textarea rows="100" cols="600" style="display: none" id="xml"></textarea>
        </DIV>

        <form name="templateCancel" action="<%=cancelURL%>" method="post">
            <INPUT TYPE="HIDDEN" NAME="Cancel" value="Cancel">
        </form>



    </DIV>
    <UL class="menu list options_menu noico" id="node_menu">
        <LI id="menu_property">
            <DIV class="ico attribute"></DIV><%=bundle.getString("applet.resources.lb_properties")%>
            <DIV class="extend"><%=bundle.getString("lb_terminology_import_delimiter_space")%></DIV>
        </LI>

        <LI>
            <DIV class="ico remove"></DIV> <%=bundle.getString("permission.tm.delete")%>
            <DIV class="extend"><%=bundle.getString("permission.tm.delete")%></DIV>
        </LI>
    </UL>
    <UL id="condition_menu" class="menu list options_menu noico">
        <LI>
            <DIV class="ico attribute"></DIV><%=bundle.getString("lb_set_default")%>
            <DIV class="extend"><%=bundle.getString("lb_terminology_import_delimiter_space")%></DIV>
        </LI>
        <LI>
            <DIV class="ico remove"></DIV> <%=bundle.getString("permission.tm.delete")%>
            <DIV class="extend"><%=bundle.getString("permission.tm.delete")%></DIV>
        </LI>
    </UL>
</BODY>
</HTML>


