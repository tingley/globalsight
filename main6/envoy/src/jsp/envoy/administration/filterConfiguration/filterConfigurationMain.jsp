<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
errorPage="/envoy/common/error.jsp"
import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
com.globalsight.everest.permission.Permission,
com.globalsight.everest.permission.PermissionSet,
com.globalsight.everest.webapp.WebAppConstants,
com.globalsight.util.resourcebundle.ResourceBundleConstants,
com.globalsight.util.resourcebundle.SystemResourceBundle,
com.globalsight.everest.webapp.pagehandler.PageHandler,
com.globalsight.everest.webapp.pagehandler.administration.filterConfiguration.FilterConfigurationConstants,
java.util.ArrayList,
java.util.Locale, java.util.Hashtable, java.util.ResourceBundle"
session="true" %>
<jsp:useBean id="new1" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" /><jsp:useBean id="edit" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" /><jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" /><jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" /><jsp:useBean id="fileprofiles" scope="request" class="java.util.ArrayList" /><jsp:useBean id="search" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" /><jsp:useBean id="advsearch" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" /><jsp:useBean id="filterConfiguration" scope="request" class="java.util.ArrayList" /><%
ResourceBundle bundle = PageHandler.getBundle(session);
String title= bundle.getString("lb_filter_configuration");
String helperText = bundle.getString("helper_text_filter_configuration");
PermissionSet perms = (PermissionSet) session.getAttribute(
WebAppConstants.PERMISSIONS);
boolean hasAddFilter = perms.getPermissionFor(Permission.FILTER_CONFIGURATION_ADD_FILTER);
boolean hasEditFilter = perms.getPermissionFor(Permission.FILTER_CONFIGURATION_EDIT_FILTER); %>
<HTML>
<!-- This is envoy\administration\filterConfiguration\filterConfigurationMain.jsp -->
    <HEAD>
        <META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
        <TITLE><%= title %></TITLE>
        
        <%@ include file="/includes/filter/Constants.jspIncl" %>
        
        <link rel="stylesheet" type="text/css" href="/globalsight/envoy/administration/filterConfiguration/filter.css">
        </link>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/Ajax.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/Array.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/json2.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dnd/DragAndDrop.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/filterConfiguration.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/JavaPropertiesFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/JavaScriptFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/MSOfficeDocFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/xmlFilter/Entity.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/XMLRuleFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/HtmlFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/WordCountFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/JSPFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/Validate.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/StringBuffer.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/MSExcelFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/InddFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/OpenOfficeFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/MSPPTFilter.js">
        </SCRIPT>
        <SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/filter/MSOffice2010Filter.js">
        </SCRIPT>
        <script type="text/javascript" src="/globalsight/includes/filter/POFilter.js"></script>
        
        <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
        <%@ include file="/envoy/common/warning.jspIncl" %>
        <SCRIPT LANGUAGE="JavaScript">
            var needWarning = false; 
            var objectName = "";
            var guideNode = "filterConfiguration";
            var helpFile = "<%=bundle.getString("help_file_profiles_main_screen")%>";
            var hasAddFilter = "<%=hasAddFilter%>";
            var hasEditFilter = "<%=hasEditFilter%>";
            
        </SCRIPT>
    </HEAD>
    <BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides();loadFilterConfigurations();">
        <%@ include file="/envoy/common/header.jspIncl" %>
        <%@ include file="/envoy/common/navigation.jspIncl" %>
        <%@ include file="/envoy/wizards/guides.jspIncl" %>
        <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
            <amb:header title="<%=title%>" helperText="<%=helperText%>" />
            <form name="fpForm" method="post">
                <div>
                    <div>
                        <amb:permission name="<%=Permission.FILTER_CONFIGURATION_REMOVE_FILTERS%>">
                            <input type='button' value='<%= bundle.getString("lb_remove") %>' onclick='removeCheckedFilters()'/>
                        </amb:permission><input type='button' id='expandAllFilters' value='<%= bundle.getString("lb_expand_all") %>' onclick='expandAllSpecialFilters();'/><input type='button' id='collapseAllFilters' value='<%= bundle.getString("lb_collapse_all") %>' onclick='collapseAllSpecialFilters();'/>
                    </div>
                </div>
                <p>
                    <table cellpadding=0 cellspacing=0 border=0 style="width:800px" class="standardText">
                        <tr style="background-color:#0c1476; padding:6px">
                            <td width="5%">
                                <input type='checkbox' onclick="checkAllSpecialFiltersToDelete(this)">
                                </input>
                            </td>
                            <td width="25%">
                                <Label style="color:white;font-family:Arial,Helvetica,sans-serif;font-size:8pt">
                                    <b><%= bundle.getString("lb_name") %></b>
                                </Label>
                            </td>
                            <td width="15%">
                            </td>
                            <td width="55%">
                                <Label style="color:white;font-family:Arial,Helvetica,sans-serif;font-size:8pt">
                                    <b><%= bundle.getString("lb_description") %></b>
                                </Label>
                            </td>
                        </tr>
                    </table>
                    <span id="filterConfigurationTable">
                        <table cellpadding=0 cellspacing=0 border=1 style="width:575px;border-color:#0c1476" class="standardText">
                        </table>
                    </span>
                    <span id="java_properties_filter_content">
                        <div id='javaPropertiesFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:400px;position:absolute;top:100px;z-index:21'>
                            <div id='javaPropertiesFilterDialogT' onmousedown="DragAndDrop(document.getElementById('javaPropertiesFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;*width:110%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_javapropertiesfilter") %>
                                </label>
                            </div>
                            <div id='javaPropertiesFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_java_properties_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveJavaProperties()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('javaPropertiesFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="java_properties_filter_internal_text">
                        <div id='javaPropertiesInternalDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:400px;position:absolute;top:100px;z-index:22'>
                            <div id='javaPropertiesInternalDialogT' onmousedown="DragAndDrop(document.getElementById('javaPropertiesInternalDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                     <%= bundle.getString("lb_internal_text") %>
                                </label>
                            </div>
                            <div style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                <table width="360px" cellpadding="3" border="0" cellspacing="1">
                                    <tr>
                                        <td class='htmlFilter_left_td'><%= bundle.getString("lb_content") %>:</td>
                                        <td class='htmlFilter_right_td'><input type='text' id='internalContent'></td>
                                    </tr>               
                                    <tr>
                                        <td class='htmlFilter_left_td'><%= bundle.getString("lb_is_regex") %>:</td>
                                        <td class='htmlFilter_right_td'><input type='checkbox' id='isRegex'></input></td>
                                    </tr>                       
                                </table>
                            </div>
                            <div id="div_button_xml_rule_filter_configured_tag" style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='propertiesFilter.saveInternalText()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('javaPropertiesInternalDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="java_script_filter_content">
                        <div id='javaScriptFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;position:absolute;top:100px;z-index:21'>
                            <div id='javaScriptFilterDialogT' onmousedown="DragAndDrop(document.getElementById('javaScriptFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_javascriptfilter") %>
                                </label>
                            </div>
                            <div id='javaScriptFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_java_script_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveJavaScript()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('javaScriptFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="ms_office_doc_filter_content">
                        <div id='msOfficeDocFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:450px;position:absolute;top:100px;z-index:21'>
                            <div id='msOfficeDocFilterDialogT' onmousedown="DragAndDrop(document.getElementById('msOfficeDocFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_msdocfilter") %>
                                </label>
                            </div>
                            <div id='msOfficeDocFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_java_script_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveMsOfficeDocFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('msOfficeDocFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="ms_office_excel_filter_content">
                        <div id='msOfficeExcelFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;position:absolute;top:100px;z-index:21'>
                            <div id='msOfficeExcelFilterDialogT' onmousedown="DragAndDrop(document.getElementById('msOfficeExcelFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_msexcelfilter") %>
                                </label>
                            </div>
                            <div id='msOfficeExcelFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_excel_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveMsOfficeExcelFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('msOfficeExcelFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="ms_office_PPT_filter_content">
                        <div id='msOfficePPTFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:370px;position:absolute;top:100px;z-index:21'>
                            <div id='msOfficePPTFilterDialogT' onmousedown="DragAndDrop(document.getElementById('msOfficePPTFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_mspptfilter") %>
                                </label>
                            </div>
                            <div id='msOfficePPTFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_PPT_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveMSPPTFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('msOfficePPTFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="po_filter_content">
                        <div id='poFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:370px;position:absolute;top:100px;z-index:21'>
                            <div id='poFilterDialogT' onmousedown="DragAndDrop(document.getElementById('poFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_pofilter") %>
                                </label>
                            </div>
                            <div id='poFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_po_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' onclick='savePOFilter()' value='<%= bundle.getString("lb_save") %>' />
                                    <input type='button' onclick="closePopupDialog('poFilterDialog')" id='exit' style='margin-left:5px' value='<%= bundle.getString("lb_cancel") %>'/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="openoffice_filter_content">
                        <div id='openofficeFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:500px;position:absolute;top:100px;z-index:21'>
                            <div id='openofficeFilterDialogT' onmousedown="DragAndDrop(document.getElementById('openofficeFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_ooFilter") %>
                                </label>
                            </div>
                            <div id='openofficeFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_openoffice_filter" style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveOpenOfficeDocFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('openofficeFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="msoffice2010_filter_content">
                        <div id='msoffice2010FilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:500px;position:absolute;top:100px;z-index:21'>
                            <div id='msoffice2010FilterDialogT' onmousedown="DragAndDrop(document.getElementById('msoffice2010FilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_o2010Filter") %>
                                </label>
                            </div>
                            <div id='msoffice2010FilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                            </div>
                            <div id="div_button_msoffice2010_filter" style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveMSOffice2010DocFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('msoffice2010FilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="xml_rule_filter_content">
                        <div id='xmlRuleFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:580px;position:absolute;top:-15px;z-index:21'>
                            <div id='xmlRuleFilterDialogT' onmousedown="DragAndDrop(document.getElementById('xmlRuleFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_xmlfilter") %>
                                </label>
                            </div>
                            <div id='xmlRulePopupContent' style='margin:20px;margin-top:20px;margin-bottom:10px;margin-left:10px'>
                            </div>
                            <div id="div_button_xml_rule_filter" style="margin-left:50px;margin-right:50px;text-align: center;margin-bottom:10px;">
                                <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveXmlRuleFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('xmlRuleFilterDialog');xmlFilter.closeConfiguredTagDialog('xmlRuleFilter_configured_tag_Dialog');closePopupDialog('deleteXmlTagDialog')"/>
                            </div>
                        </div>
                    </span>
                    <span id="xml_rule_filter_configured_tag">
                        <div id='xmlRuleFilter_configured_tag_Dialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:630px;position:absolute;top:100px;z-index:22'>
                            <div id='xmlRuleFilter_configured_tag_DialogT' onmousedown="DragAndDrop(document.getElementById('xmlRuleFilter_configured_tag_Dialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold' id='xmlRuleConfiguredTag_title'>
                                    <%=bundle.getString("lb_filter_EditConfiguredTag") %>
                                </label>
                            </div>
                            <div id='xmlRuleConfiguredTagPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                <table width="580px" cellpadding="3" border="0" cellspacing="1">
                                    <tr>
                                        <td class='htmlFilter_left_td'><%=bundle.getString("lb_tagname") %>:</td>
                                        <td class='htmlFilter_right_td'>
                                        <table cellpadding="4" border="0" cellspacing="0">
                                            <tr>
                                                <td><input value="" maxlength="255" type="text"
                                                    onkeypress="if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}"
                                                    id="xmlRuleConfiguredTag_tag_name"></td>
                                            </tr>
                                        </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_ConditionalAttributes") %>:</td>
                                        <td class='htmlFilter_right_td'>
                                        <table cellpadding="4" border="0" cellspacing="0">
                                            <tr>
                                                <td><input value="" maxlength="1024" type="text"
                                                    onkeypress="if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}"
                                                    size="15" id="xmlRuleConfiguredTag_cond_attributes_item"></td>
                                                <td><select name='xmlRuleConfiguredTag_cond_attributes_Operation' id='xmlRuleConfiguredTag_cond_attributes_Operation'>
                                                    <option value='equal'><%=bundle.getString("lb_filter_condition_equal") %></option>
                                                    <option value='not equal'><%=bundle.getString("lb_filter_condition_notequal") %></option>
                                                    <option value='match'><%=bundle.getString("lb_filter_condition_match") %></option>
                                                </select></td>
                                                <td><input value="" maxlength="1024" type="text"
                                                    onkeypress="if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}"
                                                    size="15" name="xmlRuleConfiguredTag_cond_attributes_res" id="xmlRuleConfiguredTag_cond_attributes_res"></td>
                                                <td valign='top'><input value="<%= bundle.getString("lb_add") %>" type="button"
                                                    style="width: 100%"
                                                    onclick="xmlFilter.handleConfiguredTagAdd()"
                                                    id="xmlRuleConfiguredTag_add_item"></td>
                                            </tr>
                                            <tr>
                                                <td colspan='3'>
                                                	<select name='xmlRuleConfiguredTag_cond_attributes' 
                                                			id='xmlRuleConfiguredTag_cond_attributes'
                                                			multiple style="width: 100%">
                                                	</select>
                                                </td>
                                                <td valign='top'><input value=" <%= bundle.getString("lb_remove") %> " type="button"
                                                    style="width: 100%"
                                                    onclick="xmlFilter.handleConfiguredTagRemove()"
                                                    name="xmlRuleConfiguredTag_remove_item"></td>
                                            </tr>
                                        </table></td>
                                    </tr>
                                    <tr id="xmlRuleConfiguredTag_trans_attr_0" style="display:none">
                                        <td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_TranslatableAttributes") %>:</td>
                                        <td class='htmlFilter_right_td'>
                                        <table cellpadding="4" border="0" cellspacing="0">
                                            <tr>
                                                <td><input value="" maxlength="1024" type="text"
                                                    onkeypress="if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}"
                                                    size="30" id="xmlRuleConfiguredTag_trans_attribute"></td>
                                                <td valign='top'><input value="<%= bundle.getString("lb_add") %>" type="button"
                                                    style="width: 100%"
                                                    onclick="xmlFilter.handleTransAttrAdd()"
                                                    id="xmlRuleConfiguredTag_add_TransAttr"></td>
                                            </tr>
                                            <tr>
                                                <td>
                                                	<select name='xmlRuleConfiguredTag_trans_attributes' 
                                                			id='xmlRuleConfiguredTag_trans_attributes'
                                                			multiple style="width: 100%">
                                                	</select>
                                                </td>
                                                <td valign='top'><input value=" <%= bundle.getString("lb_remove") %> " type="button"
                                                    style="width: 100%"
                                                    onclick="xmlFilter.handleTransAttrRemove()"
                                                    id="xmlRuleConfiguredTag_remove_TransAttr"></td>
                                            </tr>
                                        </table></td>
                                    </tr>
                                    <tr id="xmlRuleConfiguredTag_trans_attr_1" style="display:none">
                                    	<td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_SegmentationRule") %>:</td>
                                        <td class='htmlFilter_right_td'>
                                        	<nobr><input value='1' type='radio' name='xmlRuleConfiguredTag_segRule' id="xmlRuleConfiguredTag_segRule_1" /><font class='specialFilter_dialog_label'><%=bundle.getString("lb_filter_EmbedAttrValue") %></font></nobr>
                                        	<br />
                                        	<nobr><input value='2' type='radio' name='xmlRuleConfiguredTag_segRule' id="xmlRuleConfiguredTag_segRule_2" /><font class='specialFilter_dialog_label'><%=bundle.getString("lb_filter_TreatAttrValue") %></font></nobr>
                                        </td>
                                    </tr>
                                    <tr id="xmlRuleConfiguredTag_content_incl_0" style="display:none">
                                    	<td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_Type") %>:</td>
                                        <td class='htmlFilter_right_td'>
                                        	<nobr><input value='1' type='radio' name='xmlRuleConfiguredTag_inclType' id="xmlRuleConfiguredTag_inclType_1" /><font class='specialFilter_dialog_label'><%=bundle.getString("lb_filter_Include") %></font></nobr>
                                        	<nobr><input value='2' type='radio' name='xmlRuleConfiguredTag_inclType' id="xmlRuleConfiguredTag_inclType_2" /><font class='specialFilter_dialog_label'><%=bundle.getString("lb_filter_Exclude") %></font></nobr>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <div id="div_button_xml_rule_filter_configured_tag" style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='xmlFilter.saveConfiguredTag()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="xmlFilter.closeConfiguredTagDialog('xmlRuleFilter_configured_tag_Dialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="xmlRuleFilter_cdatapostfilter_Dialog_content">
                        <div id='xmlRuleFilter_cdatapostfilter_Dialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:530px;height:385px;position:absolute;top:100px;z-index:22'>
                            <div id='xmlRuleFilter_cdatapostfilter_DialogT' onmousedown="DragAndDrop(document.getElementById('xmlRuleFilter_cdatapostfilter_Dialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold' id='xmlRuleCdataPostFilter_title'>
                                </label>
                            </div>
                            <table id='xmlRuleAddCdatapostFilterContent' style='margin: 20px; margin-top: 20px; margin-bottom: 20px; margin-left: 20px'>
                                <tr>
                                    <td class='htmlFilter_left_td'><%= bundle.getString("lb_filter_NNNName") %>:</td>
                                    <td class='htmlFilter_right_td'>
                                        <input type='text' value='' id='xmlFilterCdatapostFilterName'>
                                        </input>
                                    </td>
                                </tr>
                                <tr>
                                    <td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_CdataCondition") %>:</td>
                                    <td class='htmlFilter_right_td'>
                                        <table cellpadding="4" border="0" cellspacing="0">
                                            <tr>
                                                <td><select name='xmlFilterCdatapostFilter_cond_type' id='xmlFilterCdatapostFilter_cond_type'>
                                                    <option value='cdatacontent'><%=bundle.getString("lb_filter_ConditionTypeCdataContent") %></option>
                                                </select></td>
                                                <td><select name='xmlFilterCdatapostFilter_cond_Operation' id='xmlFilterCdatapostFilter_cond_Operation'>
                                                    <option value='match'><%=bundle.getString("lb_filter_condition_match") %></option>
                                                </select></td>
                                                <td><input value="" maxlength="1024" type="text"
                                                    onkeypress="if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}"
                                                    size="15" name="xmlFilterCdatapostFilter_cond_res" id="xmlFilterCdatapostFilter_cond_res"></td>
                                                <td valign='top'><input value="<%= bundle.getString("lb_add") %>" type="button"
                                                    style="width: 100%"
                                                    onclick="xmlFilter.handleCdataPostFilterAdd()"
                                                    id="xmlFilterCdatapostFilter_add_item"></td>
                                            </tr>
                                            <tr>
                                                <td colspan='3'>
                                                	<select name='xmlFilterCdatapostFilter_cond_items' 
                                                			id='xmlFilterCdatapostFilter_cond_items'
                                                			multiple style="width: 100%">
                                                	</select>
                                                </td>
                                                <td valign='top'><input value=" <%= bundle.getString("lb_remove") %> " type="button"
                                                    style="width: 100%"
                                                    onclick="xmlFilter.handleCdataPostFilterRemove()"
                                                    name="xmlFilterCdatapostFilter_remove_item"></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_PostFilter") %>:</td>
                                    <td class='htmlFilter_right_td' name='xmlFilterCdatapostFilter_filter_c' id='xmlFilterCdatapostFilter_filter_c'>
                                    </td>
                                </tr>
                                <tr>
                                    <td class='htmlFilter_left_td'><%=bundle.getString("lb_filter_Translatable") %>:</td>
                                    <td class='htmlFilter_right_td'>
                                    	<input type='checkbox' id="xmlFilterCdatapostFilter_trans" />
                                    </td>
                                </tr>
                            </table>
                            <div id="div_button_add_cdatapostfilter" style="margin-left:50px;margin-right:50px;margin-top:10px">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='xmlFilter.saveCdataPostFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('xmlRuleFilter_cdatapostfilter_Dialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="xmlRuleFilter_configuredentity_content">
                        <div id='xmlRuleFilter_configuredentity_Dialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:185px;position:absolute;top:100px;z-index:22'>
                            <div id='xmlRuleFilter_configuredentity_DialogT' onmousedown="DragAndDrop(document.getElementById('xmlRuleFilter_configuredentity_Dialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold' id='xmlRuleFilter_configuredentity_title'>
                                    <%= bundle.getString("lb_filter_AddConfiguredEntity") %>
                                </label>
                            </div>
                            <table id='xmlRuleFilter_configuredentity_AddEntityContent' style='margin: 20px; margin-top: 20px; margin-bottom: 20px; margin-left: 20px'>
                                <tr>
                                    <td class='htmlFilter_left_td'>
                                        <%= bundle.getString("lb_filter_Entity_Name") %>:
                                    </td>
                                    <td class='htmlFilter_right_td'>
                                        <input type='text' value='' maxlength="20" id='xmlRuleFilter_configuredentity_EntityName'>
                                        </input>
                                    </td>
                                </tr>
                                <tr>
                                    <td class='htmlFilter_left_td'>
                                        <%= bundle.getString("lb_filter_Entity_TreatAs") %>:
                                    </td>
                                    <td class='htmlFilter_right_td'>
                                        <select id='xmlRuleFilter_configuredentity_Type' onchange="xmlFilter.onEntityTypeChange()">
                                            <option value='0'><%= bundle.getString("lb_text") %></option>
                                            <option value='1'><%= bundle.getString("lb_placeholder") %></option>
                                        </select>
                                    </td>
                                </tr>
                                <tr id="xmlRuleFilter_configuredentity_txt_0">
                                    <td class='htmlFilter_left_td'>
                                        <%= bundle.getString("lb_filter_Entity_EntityCode") %>:
                                    </td>
                                    <td class='htmlFilter_right_td'>
                                        <input type='text' value='' maxlength="10" id='xmlRuleFilter_configuredentity_EntityCode'>
                                        </input>
                                    </td>
                                </tr>
                                <tr id="xmlRuleFilter_configuredentity_txt_1">
                                    <td class='htmlFilter_left_td'>
                                        <%= bundle.getString("lb_filter_Entity_SaveAs") %>:
                                    </td>
                                    <td class='htmlFilter_right_td'>
                                        <input type='radio' value='0' name='xmlRuleFilter_configuredentity_SaveAs' id='xmlRuleFilter_configuredentity_SaveAs_0'/><font class='specialFilter_dialog_label'><%= bundle.getString("lb_entity") %></font>&nbsp;&nbsp;
                                        <input type='radio' value='1' name='xmlRuleFilter_configuredentity_SaveAs' id='xmlRuleFilter_configuredentity_SaveAs_1'/><font class='specialFilter_dialog_label'><%= bundle.getString("lb_character") %></font>
                                    </td>
                                </tr>
                            </table>
                            <div id="div_button_xmlRuleFilter_configuredentity_add" style="float:left;margin-left:100px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='xmlFilter.saveConfiguredEntity()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('xmlRuleFilter_configuredentity_Dialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="xmlRuleFilter_pi_content">
                        <div id='xmlRuleFilter_pi_Dialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:185px;position:absolute;top:100px;z-index:22'>
                            <div id='xmlRuleFilter_pi_DialogT' onmousedown="DragAndDrop(document.getElementById('xmlRuleFilter_pi_Dialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold' id='xmlRuleFilter_pi_title'>
                                    <%= bundle.getString("lb_filter_AddPI") %>
                                </label>
                            </div>
                            <table id='xmlRuleFilter_pi_AddContent' style='margin: 20px; margin-top: 20px; margin-bottom: 20px; margin-left: 20px'>
                                <tr>
                                    <td class='htmlFilter_left_td'>
                                        <%= bundle.getString("lb_filter_PI_Name") %>:
                                    </td>
                                    <td class='htmlFilter_right_td'>
                                        <input type='text' value='' maxlength="20" id='xmlRuleFilter_pi_name'>
                                        </input>
                                    </td>
                                </tr>
                                <tr>
                                    <td class='htmlFilter_left_td'>
                                        <%= bundle.getString("lb_filter_PI_HandMode") %>:
                                    </td>
                                    <td class='htmlFilter_right_td'>
                                        <select id='xmlRuleFilter_pi_Type'>
                                            <option value='0'><%= bundle.getString("lb_filter_PI_ModeMarkup") %></option>
                                            <option value='1'><%= bundle.getString("lb_filter_PI_EmbMarkup") %></option>
                                            <option value='2'><%= bundle.getString("lb_filter_PI_remove") %></option>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                            <div id="div_button_xmlRuleFilter_pi_add" style="float:left;margin-left:100px;margin-top:10px">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='xmlFilter.saveProcessIns()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('xmlRuleFilter_pi_Dialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="xml_rule_filter_add_entity_content">
                        <div id='xmlRuleFilter_add_entity_Dialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:185px;position:absolute;top:100px;z-index:22'>
                            <div id='xmlRuleFilter_add_entity_DialogT' onmousedown="DragAndDrop(document.getElementById('xmlRuleFilter_add_entity_Dialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_AddConfiguredEntity") %>
                                </label>
                            </div>
                            <table id='xmlRuleAddEntityContent' style='margin: 20px; margin-top: 20px; margin-bottom: 20px; margin-left: 20px'>
                                <tr>
                                    <td>
                                        <span class='specialFilter_dialog_label'><%= bundle.getString("lb_entity") %>:</span>
                                    </td>
                                    <td>
                                        <input type='text' value='' id='xmlFilterEntityName'>
                                        </input>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <span class='specialFilter_dialog_label'><%= bundle.getString("lb_TreatAs") %>:</span>
                                    </td>
                                    <td>
                                        <select id='xmlFilterType'>
                                            <option value='Text'><%= bundle.getString("lb_text") %></option>
                                            <option value='PlaceHolder'><%= bundle.getString("lb_placeholder") %></option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <span class='specialFilter_dialog_label'><%= bundle.getString("lb_entitycode") %>:</span>
                                    </td>
                                    <td>
                                        <input type='text' value='' id='xmlFilterEntityCode'>
                                        </input>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <span class='specialFilter_dialog_label'><%= bundle.getString("lb_SaveAs") %>:</span>
                                    </td>
                                    <td>
                                        <input type='radio' value='Entity' name='xmlFilterSaveAs' checked/><span class='specialFilter_dialog_label'><%= bundle.getString("lb_entity") %></span>
                                        <input type='radio' value='Character' name='xmlFilterSaveAs' /><span class='specialFilter_dialog_label'><%= bundle.getString("lb_character") %></span>
                                    </td>
                                </tr>
                            </table>
                            <div id="div_button_add_entity" style="float:left;margin-left:100px;margin-top:10px">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='entity.addEntity()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('xmlRuleFilter_add_entity_Dialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="delete_tag_content_xml_filter">
                        <div id='deleteXmlTagDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:550px;position:absolute;top:5px;z-index:22'>
                            <div id='deleteXmlTagDialogT' onmousedown="DragAndDrop(document.getElementById('deleteXmlTagDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_DeleteTags") %>
                                </label>
                            </div>
                            <Label class="specialFilter_dialog_label" id='deleteXmlTagsDialogLable' style="margin-top:10px; margin-bottom:10px">
                                <%= bundle.getString("lb_filter_DeleteTagsNote") %>
                            </Label>
                            <hr align='left' width=80%/>
                            <span id='deleteXmlTagTableContent'></span>
                            <div id="div_button_delete_tag_xml" style="float:left;margin-left:100px;margin-right:120px;margin-top:10px;margin-bottom:20px">
                                <input type='button' style='float:left' value='<%= bundle.getString("lb_save") %>' onclick='xmlFilter.deleteCheckedTags()'/><input id='exit' style='margin-left:5px;float:right' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('deleteXmlTagDialog')"/>
                            </div>
                        </div>
                    </span>
                    <span id="html_filter_content">
                        <div id='htmlFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:500px;position:absolute;top:-15px;z-index:21'>
                            <div id='htmlFilterDialogT' onmousedown="DragAndDrop(document.getElementById('htmlFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_htmlfilter") %>
                                </label>
                            </div>
                            <div id='htmlFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:10px;margin-left:30px'>
                            </div>
                            <div id="div_button_html_filter" style="float:left;margin-left:178px;margin-bottom:10px;">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveHtmlFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('htmlFilterDialog')"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="add_single_tag_content">
                        <div id='addSingleTagDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:180px;position:absolute;top:100px;z-index:22'>
                            <div id='addSingleTagDialogT' onmousedown="DragAndDrop(document.getElementById('addSingleTagDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_AddSingleTag") %>
                                </label>
                            </div>
                            <div id='addSingleTagContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                <Label class='specialFilter_dialog_label'>
                                    <%= bundle.getString("lb_tagname") %>:
                                </Label>
                                <input type='text' id='singleTagNameToAdd' value='' autocomplete = 'off'>
                                </input>
                            </div>
                            <div id="div_button_add_single_tag" style="float:left;margin-left:100px;margin-top:10px">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='htmlFilter.addSingleTag()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addSingleTagDialog');htmlFilter.showTranslateRuleSelectBox('');"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="add_internal_tag_content">
                        <div id='addInternalTagDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:180px;position:absolute;top:100px;z-index:22'>
                            <div id='addInternalTagDialogT' onmousedown="DragAndDrop(document.getElementById('addInternalTagDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_AddInternalTag") %>
                                </label>
                            </div>
                            <div id='addInternalTagContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                <Label class='specialFilter_dialog_label'>
                                    <%= bundle.getString("lb_internal_tag") %>:
                                </Label>
                                <input type='text' id='InternalTagToAdd' value='' autocomplete = 'off'>
                                </input>
                            </div>
                            <div id="div_button_add_single_tag" style="float:left;margin-left:100px;margin-top:10px">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='htmlFilter.addInternalTag()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addInternalTagDialog');htmlFilter.showTranslateRuleSelectBox('');"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="add_map_tag_content">
                        <div id='addMapTagDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:180px;position:absolute;top:100px;z-index:22'>
                            <div id='addMapTagDialogT' onmousedown="DragAndDrop(document.getElementById('addMapTagDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                <label class='whiteBold'>
                                    <%= bundle.getString("lb_filter_AddMapTag") %>
                                </label>
                            </div>
                            <div id='addMapTagContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                <Label class='specialFilter_dialog_label'>
                                    <%= bundle.getString("lb_tagkey") %>:
                                </Label>
                                <input style='margin-left:11px;' type='text' id='tagKeyToAdd' value='' autocomplete = 'off'>
                                </input>
                                <br/>
                                <Label class='specialFilter_dialog_label'>
                                    <%= bundle.getString("lb_tagvalue") %>:
                                </Label>
                                <input style='margin-top:2px;' type='text' id='tagValueToAdd' value='' autocomplete = 'off'>
                                </input>
                            </div>
                            <div id="div_button_add_tag" style="float:left;margin-left:100px;margin-top:10px">
                                <center>
                                    <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='htmlFilter.addMapTag()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addMapTagDialog');htmlFilter.showTranslateRuleSelectBox('');"/>
                                </center>
                            </div>
                        </div>
                    </span>
                    <span id="add_single_attribute_content">
                            <div id='addSingleAttributeDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;height:180px;position:absolute;top:100px;z-index:22'>
                                <div id='addSingleAttributeDialogT' onmousedown="DragAndDrop(document.getElementById('addSingleAttributeDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_AddSingleAttribute") %>
                                    </label>
                                </div>
                                <div id='addSingleAttributeContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                    <Label class='specialFilter_dialog_label'>
                                        <%= bundle.getString("lb_attributename") %>:
                                    </Label>
                                    <input type='text' id='singleAttributeNameToAdd' value='' autocomplete = 'off'>
                                    </input>
                                </div>
                                <div id="div_button_add_single_tag" style="float:left;margin-left:100px;margin-top:10px">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='htmlFilter.addSingleAttribute()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addSingleAttributeDialog');htmlFilter.showTranslateRuleSelectBox('');"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="add_styles">
                            <div id='addStylesDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:400px;width:300px;position:absolute;top:140px;z-index:22'>
                                <div onmousedown="DragAndDrop(document.getElementById('addStylesDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_addstyle") %>
                                    </label>
                                </div>
                                <div style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                    <Label class='specialFilter_dialog_label'>
                                        <%= bundle.getString("lb_style") %>:
                                    </Label>
                                    <input type='text' id='styleToAdd' value='' autocomplete = 'off'>
                                    </input>
                                </div>
                                <div style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='officeDocFilter.addStyle()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addStylesDialog');officeDocFilter.showStyleSelectBox(false);"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="add_oostyles">
                            <div id='addOOStyleDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:400px;width:300px;position:absolute;top:140px;z-index:22'>
                                <div onmousedown="DragAndDrop(document.getElementById('addOOStyleDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_addstyle") %>
                                    </label>
                                </div>
                                <div style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                    <Label class='specialFilter_dialog_label'>
                                        <%= bundle.getString("lb_style") %>:
                                    </Label>
                                    <input type='text' id='oostyleToAdd' value='' autocomplete = 'off'>
                                    </input>
                                </div>
                                <div style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='openofficeDocFilter.addStyle()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addOOStyleDialog');openofficeDocFilter.showStyleSelectBox(false);"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="add_o2010styles">
                            <div id='addO2010StyleDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:400px;width:300px;position:absolute;top:140px;z-index:22'>
                                <div onmousedown="DragAndDrop(document.getElementById('addO2010StyleDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_addstyle") %>
                                    </label>
                                </div>
                                <div style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                    <Label class='specialFilter_dialog_label'>
                                        <%= bundle.getString("lb_style") %>:
                                    </Label>
                                    <input type='text' id='o2010styleToAdd' value='' autocomplete = 'off'>
                                    </input>
                                </div>
                                <div style="float:left;margin-left:100px;margin-top:10px;margin-bottom:10px;">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='msoffice2010DocFilter.addStyle()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('addO2010StyleDialog');msoffice2010DocFilter.showStyleSelectBox(false);"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="delete_tag_content_properties">
                            <div id='propertiesDeleteTagDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:550px;position:absolute;top:5px;z-index:22'>
                                <div id='propertiesDeleteTagDialogT' onmousedown="DragAndDrop(document.getElementById('propertiesDeleteTagDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_DeleteTags") %>
                                    </label>
                                </div>
                                <Label class="specialFilter_dialog_label" id='propddeleteTagsDialogLable' style="margin-top:10px; margin-bottom:10px">
                                    <%= bundle.getString("lb_filter_DeleteTagsNote") %>
                                </Label>
                                <hr align='left' width=80%/><span id='propDeleteTagTableContent'></span>
                                <div id="div_prop_button_delete_tag" style="float:left;margin-left:100px;margin-right:120px;margin-top:10px;margin-bottom:20px">
                                    <input type='button' style='float:left' value='<%= bundle.getString("lb_save") %>' onclick='propertiesFilter.deleteTags()'/><input id='exit' style='margin-left:5px;float:right' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('propertiesDeleteTagDialog')"/>
                                </div>
                            </div>
                        </span>
                        <span id="delete_tag_content">
                            <div id='deleteTagDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:550px;position:absolute;top:5px;z-index:22'>
                                <div id='deleteTagDialogT' onmousedown="DragAndDrop(document.getElementById('deleteTagDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_DeleteTags") %>
                                    </label>
                                </div>
                                <Label class="specialFilter_dialog_label" id='deleteTagsDialogLable' style="margin-top:10px; margin-bottom:10px">
                                    <%= bundle.getString("lb_filter_DeleteTagsNote") %>
                                </Label>
                                <hr align='left' width=80%/><span id='deleteTagTableContent'></span>
                                <div id="div_button_delete_tag" style="float:left;margin-left:100px;margin-right:120px;margin-top:10px;margin-bottom:20px">
                                    <input type='button' style='float:left' value='<%= bundle.getString("lb_save") %>' onclick='htmlFilter.deleteTags()'/><input id='exit' style='margin-left:5px;float:right' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('deleteTagDialog')"/>
                                </div>
                            </div>
                        </span>
                        <span id="delete_style_content">
                              <div id='deleteStyleDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:550px;position:absolute;top:5px;z-index:22'>
                                <div id='deleteStyleDialogT' onmousedown="DragAndDrop(document.getElementById('deleteStyleDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_deletestyle") %>
                                    </label>
                                </div>
                                <Label class="specialFilter_dialog_label" id='deleteStylesDialogLable' style="margin-top:10px; margin-bottom:10px">
                                    <%= bundle.getString("lb_filter_deletestyleNote") %>
                                </Label>
                                <hr align='left' width=80%/>
                                <span id='deleteStyleTableContent'>    </span>                                
                                <div id="div_button_delete_style_tag" style="float:left;margin-left:100px;margin-right:120px;margin-top:10px;margin-bottom:20px">
                                    
                                        <input type='button' style='float:left' value='<%= bundle.getString("lb_save") %>' onclick='officeDocFilter.deleteStyles()'/>
                                        <input id='exit' style='margin-left:5px;float:right' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('deleteStyleDialog')"/>
                                </div>
                            </div>
                        </span>
                        <span id="delete_oo_style_content">
                              <div id='deleteOOStyleDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:550px;position:absolute;top:5px;z-index:22'>
                                <div id='deleteOOStyleDialogT' onmousedown="DragAndDrop(document.getElementById('deleteOOStyleDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_deletestyle") %>
                                    </label>
                                </div>
                                <Label class="specialFilter_dialog_label" id='deleteOOStylesDialogLable' style="margin-top:10px; margin-bottom:10px">
                                    <%= bundle.getString("lb_filter_deletestyleNote") %>
                                </Label>
                                <hr align='left' width=80%/>
                                <span id='deleteOOStyleTableContent'>    </span>                                
                                <div id="div_button_delete_oo_style_tag" style="float:left;margin-left:100px;margin-right:120px;margin-top:10px;margin-bottom:20px">
                                    
                                        <input type='button' style='float:left' value='<%= bundle.getString("lb_save") %>' onclick='openofficeDocFilter.deleteStyles()'/>
                                        <input id='exit' style='margin-left:5px;float:right' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('deleteOOStyleDialog')"/>
                                </div>
                            </div>
                        </span>
                        <span id="delete_o2010_style_content">
                              <div id='deleteO2010StyleDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:550px;position:absolute;top:5px;z-index:22'>
                                <div id='deleteO2010StyleDialogT' onmousedown="DragAndDrop(document.getElementById('deleteO2010StyleDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_deletestyle") %>
                                    </label>
                                </div>
                                <Label class="specialFilter_dialog_label" id='deleteO2010StylesDialogLable' style="margin-top:10px; margin-bottom:10px">
                                    <%= bundle.getString("lb_filter_deletestyleNote") %>
                                </Label>
                                <hr align='left' width=80%/>
                                <span id='deleteO2010StyleTableContent'>    </span>                                
                                <div id="div_button_delete_o2010_style_tag" style="float:left;margin-left:100px;margin-right:120px;margin-top:10px;margin-bottom:20px">
                                    
                                        <input type='button' style='float:left' value='<%= bundle.getString("lb_save") %>' onclick='msoffice2010DocFilter.deleteStyles()'/>
                                        <input id='exit' style='margin-left:5px;float:right' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('deleteO2010StyleDialog')"/>
                                </div>
                            </div>
                        </span>
                        <div style='display:none;border:1px #0c1476 solid' id='showDiv'>
                        </div>
                        <span id="remove_filters_exists_in_fileprofile_content">
                            <div id='removeExistsFiltersDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;*width:350px;position:absolute;top:100px;z-index:23'>
                                <div id='removeExistsFiltersDialogT' onmousedown="DragAndDrop(document.getElementById('removeExistsFiltersDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_FiltersToDelete") %>
                                    </label>
                                </div>
                                <div class="specialFilter_dialog_label" id='removeExistsFiltersLable' style="margin:5px;">
                                    <%= bundle.getString("lb_filter_FiltersToDeleteNote") %>
                                </div>
                                <hr align='left' width=100%/>
                                <span id='removeExistsFiltersTableContent'></span>
                                <div id="div_button_delete_tag" style="float:left;width:50px;margin-left:150px;margin-right:20px;margin-top:5px;margin-bottom:20px">
                                    <input id='exit' style='margin-left:5px;margin-bottom:10px;float:right' type='button' value='<%= bundle.getString("lb_return") %>' onclick="closePopupDialog('removeExistsFiltersDialog')"/>
                                </div>
                            </div>
                        </span>
                        <span id="wordcount_filter_content">
                            <div id='wordcountFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;position:absolute;top:-15px;z-index:21'>
                                <div id='wordcountFilterDialogT' onmousedown="DragAndDrop(document.getElementById('wordcountFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_REFilter") %>
                                    </label>
                                </div>
                                <div id='wordCountFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                </div>
                                <div id="div_button_wordcount_filter" style="float:left;margin-left:100px;margin-top:10px">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveWordcountFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('wordcountFilterDialog')"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="wordcount_filter_content_add_regex">
                            <div id='wordcountFilterAddRegexDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;position:absolute;top:-15px;z-index:22'>
                                <div id='wordcountFilterAddRegexDialogT' onmousedown="DragAndDrop(document.getElementById('wordcountFilterAddRegexDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_AddRegex") %>
                                    </label>
                                </div>
                                <div id='wordCountFilterAddRegexPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                    <Label class='specialFilter_dialog_label'>
                                        <%= bundle.getString("lb_filter_Regex") %>:
                                    </Label>
                                    <input id='addRegexValue' type='text' value=''/>
                                </div>
                                <div id="div_button_wordcount_filter_AddRegex" style="float:left;margin-left:100px;margin-top:10px">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='wordCountFilter.addRegex()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('wordcountFilterAddRegexDialog')"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="jsp_filter_content">
                            <div id='jspFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;position:absolute;top:100px;z-index:21'>
                                <div id='jspFilterDialogT' onmousedown="DragAndDrop(document.getElementById('jspFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_jspfilter") %>
                                    </label>
                                </div>
                                <div id='jspFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                </div>
                                <div id="div_button_jsp_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveJSPFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('jspFilterDialog')"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                        <span id="indd_filter_content">
                            <div id='inddFilterDialog' style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;display:none;left:300px;width:300px;position:absolute;top:100px;z-index:21'>
                                <div id='inddFilterDialogT' onmousedown="DragAndDrop(document.getElementById('inddFilterDialog'),document.getElementById('contentLayer'))" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:pointer'>
                                    <label class='whiteBold'>
                                        <%= bundle.getString("lb_filter_inddFilter") %>
                                    </label>
                                </div>
                                <div id='inddFilterPopupContent' style='margin:20px;margin-top:20px;margin-bottom:20px;margin-left:20px'>
                                </div>
                                <div id="div_button_indd_filter" style="margin-left:50px;margin-right:50px;margin-top:10px;margin-bottom:10px;">
                                    <center>
                                        <input type='button' value='<%= bundle.getString("lb_save") %>' onclick='saveInddFilter()'/><input id='exit' style='margin-left:5px' type='button' value='<%= bundle.getString("lb_cancel") %>' onclick="closePopupDialog('inddFilterDialog')"/>
                                    </center>
                                </div>
                            </div>
                        </span>
                   </FORM>
            </DIV>
    </BODY>
</HTML>