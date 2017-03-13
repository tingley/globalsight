<%@page import="java.text.MessageFormat"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
     com.globalsight.util.edit.EditUtil,
     com.globalsight.everest.webapp.WebAppConstants,
     com.globalsight.cxe.entity.blaise.BlaiseConnector,
	 java.util.*"
	session="true"%>
<%@ page import="com.globalsight.cxe.entity.fileprofile.FileProfileImpl" %>
<%@ page import="com.globalsight.cxe.entity.customAttribute.AttributeSet" %>
<%@ page import="com.globalsight.connector.blaise.form.BlaiseConnectorAttribute" %>
<%@ page import="com.globalsight.util.StringUtil" %>
<%@ page import="com.globalsight.everest.util.system.SystemConfiguration" %>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>

<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

	// URLs
	String saveURL = save.getPageURL() + "&action=save";
	String cancelURL = cancel.getPageURL() + "&action=cancel";
	String testURL = self.getPageURL() + "&action=test";
	String getAttributesURL = self.getPageURL() + "&action=getAttributes";

	// Labels
	String helper = bundle.getString("helper_text_blaise_connector_edit");
    String errorConnect = bundle.getString("error_blaise_connector");

    String title = null;
	String id = "-1";
	String name = "";
	String desc = "";
	String url = "";
	String username = "";
	String password = "";
	boolean isAutomatic = false;
	String pullDays = "";
	int pullHour = 7;
	long fileProfileId = -1L;
	boolean isCombined = true;
	int wordCount = 600;
	String clientCoreVersion = "2.0";// default "2.0".
	long companyId = -1;
    boolean edit = false;
	int qaCount = 0;
	int checkDuration = 0;
	String userCalendar = "";
	Calendar sysCal = Calendar.getInstance();
	userCalendar = sysCal.getTimeZone().getID();
	String userName = (String) request.getAttribute("currentUsername");
	BlaiseConnector connector = (BlaiseConnector) request.getAttribute("blaise");
	ArrayList<FileProfileImpl> fps = (ArrayList<FileProfileImpl>) request.getAttribute("fileProfiles");
	List<BlaiseConnectorAttribute> typeAttributes = (List<BlaiseConnectorAttribute>) request.getAttribute("typeAttributes");
	String attributeData = (String) request.getAttribute("attributeData");
	if (connector != null)
	{
		edit = true;
		title = bundle.getString("lb_edit_blaise_connector");
        id = Long.toString(connector.getId());
		name = connector.getName();
        username = connector.getUsername();
        password = connector.getPassword();
        url = connector.getUrl();
        companyId = connector.getCompanyId();
        desc = connector.getDescription();
        desc = desc == null ? "" : desc;
        clientCoreVersion = connector.getClientCoreVersion();
        isAutomatic = connector.isAutomatic();
        pullDays = connector.getUserPullDays();
        if (StringUtil.isEmpty(pullDays))
            pullDays = "";
        pullHour = connector.getUserPullHour();
        isCombined = connector.isCombined();
        fileProfileId = connector.getDefaultFileProfileId();
        wordCount = connector.getMinProcedureWords();
		qaCount = connector.getQaCount();
		checkDuration = connector.getCheckDuration();
		userName = connector.getLoginUser();
		String tmpUserCalendar = connector.getUserCalendar();
		if (StringUtil.isNotEmpty(tmpUserCalendar))
		    userCalendar = tmpUserCalendar;
	}
	else
	{
		title = bundle.getString("lb_new_blaise_connector");
	}
	boolean enableSetCheckDuration = false;
	String value = SystemConfiguration.getInstance().getStringParameter("blaise.check.duration.enabled");
	if ("true".equalsIgnoreCase(value))
	    enableSetCheckDuration = true;
%>

<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js" ></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/jquery.form.js"></SCRIPT>
<SCRIPT language="JavaScript1.2" SRC="/globalsight/includes/jquery.loadmask.min.js"></SCRIPT>
<link href="/globalsight/includes/css/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script>
var guideNode = "Blaise";
var needWarning = false;
var helpFile = "<%=bundle.getString("help_blaise_connector_basic")%>";

function cancel()
{
	$("#blaiseForm").attr("action", "<%=cancelURL%>").submit();
}

function save()
{
	if (confirmForm())
	{
		testConnect();
	}
}

function testConnect()
{
    $("#idDiv").mask("<%=bundle.getString("msg_blaise_wait_connect")%>");
    $("#blaiseForm").ajaxSubmit({
        type: 'post',  
        url: "<%=testURL%>",
        dataType:'json',
        timeout:100000000,
        success: function(data){
        $("#idDiv").unmask("<%=bundle.getString("msg_blaise_wait_connect")%>");            
            if("" == data.error)
            {
            	$("#blaiseForm").attr("action", "<%=saveURL%>").submit();
            }
            else
            {
                alert(data.error);
            }
        },
        error: function(XmlHttpRequest, textStatus, errorThrown){
            $("#idDiv").unmask("<%=bundle.getString("msg_blaise_wait_connect")%>");
            alert("<%=errorConnect%>");
        }
    });
}

function confirmForm()
{
    <%String msgTemp = bundle.getString("msg_validate_text_empty");%>
    if (isEmptyString(blaiseForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_name")))%>");
        blaiseForm.name.focus();
        return false;
    }

    if (!validName())
    {
    	blaiseForm.name.focus();
        return false;
    }

    if (isEmptyString(blaiseForm.url.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_url")))%>");
        blaiseForm.url.focus();
        return false;
    }

    if (isEmptyString(blaiseForm.username.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_user_name")))%>");
        blaiseForm.username.focus();
        return false;
    }

    if (isEmptyString(blaiseForm.password.value))
    {
        alert("<%=EditUtil.toJavascript(MessageFormat.format(msgTemp, bundle.getString("lb_password")))%>");
        blaiseForm.password.focus();
        return false;
    }
	
    if ($("#automatic1").attr("checked") == "checked") {
		// Automatic setting is on
        var existUrls = "<c:out value='${urls}' />";
        var urlName = "$@$" + $("#url").val().trim() + "$@$";
        existUrls = existUrls.toLowerCase();
        if (existUrls.indexOf(urlName) != -1)
        {
            alert('<%=bundle.getString("msg_blaise_duplicate_automatic_connector")%>');
            blaiseForm.url.focus();
            return false;
        }
		
        var $tmp = $("#minProcedureWords").val();
        if (!isInteger($tmp) || $tmp < 600 || $tmp>1000000)
        {
            alert("<%=bundle.getString("msg_blaise_wrong_count")%>");
            $("#minProcedureWords").focus();
            return false;
        }
		
		if ($("#defaultFileProfileId").val() == "")
		{
			alert("<%=bundle.getString("msg_blaise_set_file_profile")%>");
			return false;
		}

        if ($.trim($("#attributeSetName").text()) != "")
        {
            var tmp = "", lastTmp = "", value = "";
			var result = true;
            $(".falconProduct").each(function() {
				value = $(this).val();
                if (tmp != "") 
				{
					if (value == tmp || value == lastTmp)
					{
						alert("<%=bundle.getString("msg_blaise_select_attributes")%>");
						result = false;
						return false;
					} else
						lastTmp = value;
				} else
					tmp = value;
            });
			
			$("input[name*=Attr][data-rule*=required]").each(function(){
				if ($(this).val() == "") {
					alert("Please input data into required fields first.");
					$(this).focus();
					result = false;
					return false;
				}
			});
			if (!result) return false;
			
			$("input[name*=Attr][data-rule*=integer]").each(function(){
			    if ($(this).val() != "") {
                    if (isAllDigits($(this).val())) {
						var tmp1 = $(this).attr("data-range");
						if (tmp1 == "" || tmp1.indexOf(",") == -1)
							return false;
						else {
							var tmpArray = tmp1.split(",");
							if ($(this).val() < eval(tmpArray[0]) || $(this).val() > eval(tmpArray[1])) {
								alert("The range of input integer value is from " + tmpArray[0] + " to " + tmpArray[1] + ".");
								$(this).focus();
								result = false;
								return false;
							}
						} 
					} else {
                        alert("Please input correct integer value into fields first.");
                        $(this).focus();
                        result = false;
                        return false;
                    }
                }
			});
			if (!result) return false;
			
			$("input[name*=Attr][data-rule*=range]").each(function(){
			    if ($(this).val() != "") {
                    if (isInteger($(this).val()) || isFloat($(this).val())) {
						var tmp1 = $(this).attr("data-range");
						if (tmp1 == "" || tmp1.indexOf(",") == -1)
							return false;
						else {
							var tmpArray = tmp1.split(",");
							if ($(this).val() < eval(tmpArray[0]) || $(this).val() > eval(tmpArray[1])) {
								alert("The range of input float value is from " + tmpArray[0] + " to " + tmpArray[1] + ".");
								$(this).focus();
								result = false;
								return false;
							}
						}
					} else {
						alert("Please input correct float value into fields first.");
							$(this).focus();
							result = false;
							return false;
					}
				}
			});

			var tmp = $("#qaCountString").val().trim().toLowerCase();
			if ("all" != tmp && (!isAllDigits(tmp) || tmp < 1 || tmp > 99999))
            {
                alert("Please input integer value [1 to 99999] or 'All' for Entry Count field.");
                $("#qaCountString").focus();
                result = false;
                return false;
            }
			if (!result) return false;
        }
    }

    return true;
}
														
// Ensure the name has no special chars and not an existed one already.
function validName()
{
    var name = allTrim(blaiseForm.name.value);
    if (hasSpecialChars(name))
    {
        alert("<%=bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }

    var existNames = "<c:out value='${names}'/>";
    var lowerName = name.toLowerCase();
    existNames = existNames.toLowerCase();
    if (existNames.indexOf("," + lowerName + ",") != -1)
    {
        alert('<%=bundle.getString("msg_duplicate_name")%>');
        blaiseForm.name.value.focus();
        return false;
    }

    blaiseForm.name.value = name;

    return true;
}

</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="idDiv" style="POSITION: ABSOLUTE;height:100%;width: 100%;  TOP: 0px; LEFT: 0px; RIGHT: 0px;">
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helper%>" />

    <FORM name="blaiseForm" id="blaiseForm" method="post" action="">
    <input type="hidden" name="id" value="<%=id%>" />
	<input type="hidden" name="clientCoreVersion" value="<%=clientCoreVersion%>" />
	<input type="hidden" id="attributeData" name="attributeData" value="<%=attributeData%>" />
    <%if(edit) {%>
    <input type="hidden" name="companyId" value="<%=companyId%>" />
    <%} %>
    <table class="standardText">
    	<tr>
    		<td width="180px"><%=bundle.getString("lb_name")%> <span class="asterisk">*</span>:</td>
    		<td><input type="text" name="name" id="name" value="<%=name%>"  maxlength="40" size="30"></td>
    	</tr>
        <tr>
            <td valign="top"><%=bundle.getString("lb_description")%>:</td>
            <td><textarea rows="4" cols="40" name="description"><%=desc%></textarea></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_url")%><span class="asterisk">*</span>:</td>
            <td><input type="text" name="url" id="url" style="width: 360px;" value="<%=url%>" maxLength="200"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_user_name")%><span class="asterisk">*</span>:</td>
            <td><input type="text" name="username" id="username" style="width: 360px;" value="<%=username%>" maxLength="200" autocomplete="off" data-rule="required"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_password")%><span class="asterisk">*</span>:</td>
            <td><input type="password" name="password" id="password" style="width: 360px;" value="<%=password%>" maxLength="200" autocomplete="off"></td>
        </tr>

        <tr>
            <td class="standardText"><%= bundle.getString("lb_blaise_automatic")%>:</td>
            <td class="standardText">
                <input type="radio" id="automatic" name="automatic" value="false" <%=isAutomatic ? "" : "checked"%>  onclick="showAutoOptions(false);"/>No&nbsp;&nbsp;
                <input type="radio" id="automatic1" name="automatic" value="true" <%=isAutomatic ? "checked" : ""%>  onclick="showAutoOptions(true);"/>Yes
            </td>
        </tr>
        <tr class="autoOption">
            <td width="180px" class="standardText"><%= bundle.getString("lb_blaise_pull_time")%>:</td>
            <td class="standardText">
                <ul style="display: inline-flex;list-style-type: none;margin-left: 0px;padding-left: 0px;">
                    <li><input type="checkbox" name="monday" value="2" <%=pullDays.contains("2") ? "checked" : ""%>>Monday</li>
                    <li><input type="checkbox" name="tuesday" value="3" <%=pullDays.contains("3") ? "checked" : ""%>>Tuesday</li>
                    <li><input type="checkbox" name="wednesday" value="4" <%=pullDays.contains("4") ? "checked" : ""%>>Wednesday</li>
                    <li><input type="checkbox" name="thursday" value="5" <%=pullDays.contains("5") ? "checked" : ""%>>Thursday</li>
                    <li><input type="checkbox" name="friday" value="6" <%=pullDays.contains("6") ? "checked" : ""%>>Friday</li>
                    <li><input type="checkbox" name="saturday" value="7" <%=pullDays.contains("7") ? "checked" : ""%>>Saturday</li>
                    <li><input type="checkbox" name="sunday" value="1" <%=pullDays.contains("1") ? "checked" : ""%>>Sunday</li>
                </ul>
            </td>
        </tr>
        <tr class="autoOption">
            <td>&nbsp;</td>
            <td class="standardText">
                <select name="userTimeZone">
                    <%
                        List list = (ArrayList)request.getAttribute("tzs");
                        for (int i = 0; i < list.size(); i++)
                        {
                            String timeZoneId = (String)list.get(i);

                            out.println("<option value='" + timeZoneId + "'");
                            if (TimeZone.getTimeZone(timeZoneId).getOffset(0) == TimeZone.getTimeZone(userCalendar).getOffset(0))               {
                                out.println(" selected ");
                            }
                            out.println(">" + timeZoneId + "  " + bundle.getString(timeZoneId) + "</option>");
                        }
                    %>
                </select>
                &nbsp;
                <select id="pullHour" name="pullHour" class="standardText">
                    <%
                        for (int i=0;i<24;i++) {
                    %>
                    <option value="<%=i%>" <%=pullHour == i ? "selected" : ""%>><%=i%>:00</option>
                    <% } %>
                </select>
            </td>
        </tr>
        <tr class="autoOption">
            <td class="standardText"><%=bundle.getString("lb_blaise_owner")%>:</td>
            <td class="standardText"><%=userName%></td>
        </tr>
        <tr class="autoOption">
            <td class="standardText"><%=bundle.getString("lb_blaise_combine_by_language")%>:</td>
            <td class="standardText"><input type="checkbox" id="combined" name="combined" value="true" <%=isCombined ? "checked" : ""%>/></td>
        </tr>
        <tr class="autoOption">
            <td width="180px" class="standardText"><%= bundle.getString("lb_blaise_min_procedure_words")%>:</td>
            <td class="standardText">
                <input type="text" id="minProcedureWords" name="minProcedureWords" value="<%=wordCount%>" class="standardText" />
            </td>
        </tr>
        <tr class="autoOption">
            <td class="standardText"><%= bundle.getString("lb_default_file_profile")%>:</td>
            <td class="standardText">
                <select id="defaultFileProfileId" name="defaultFileProfileId" class="standardText" onchange="fileProfileChanged();">
                    <option value=""><%=bundle.getString("lb_choose") %></option>
                    <%
                        if (fps != null && fps.size() > 0) {
                            for (FileProfileImpl fp : fps) {
                                if (fp.getId() == fileProfileId)
                                    out.println("<option value=" + fp.getId() + " selected>" + fp.getName() + "</option>");
                                else
                                    out.println("<option value=" + fp.getId() + ">" + fp.getName() + "</option>");
                            }
                        }
                    %>
                </select>
            </td>
        </tr>
        <tr class="allowNone autoOption">
            <td class="standardText"><%= bundle.getString("lb_attribute_group")%>:</td>
            <td class="standardText"><div id="attributeSetName"></div></td>
        </tr>
        <tr class="allowNone autoOption" id="anyAttrs">
			<td class="standardText">Any</td>
            <td class="standardText">
                <table name="ja" border="1" cellspacing="0" cellpadding="1" border="0" class="listborder standardText" style="width:600px;">
                    <thead>
                    <tr class="tableHeadingBasicTM">
                        <td><%= bundle.getString("lb_attributename")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_value")%></td>
                    </tr>
                    </thead>
                    <tbody id="anyAttrData">
                    </tbody>
                </table>
            </td>
        </tr>
        <tr class="allowNone autoOption" id="hduAttrs">
			<td class="standardText">HDU Workbook</td>
            <td class="standardText">
                <table name="ja" border="1" cellspacing="0" cellpadding="1" border="0" class="listborder standardText" style="width:600px;">
                    <thead>
                    <tr class="tableHeadingBasicTM">
                        <td><%= bundle.getString("lb_attributename")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_value")%></td>
                    </tr>
                    </thead>
                    <tbody id="hduAttrData">
                    </tbody>
                </table>
            </td>
        </tr>
        <tr class="allowNone autoOption" id="isheetAttrs">
			<td class="standardText">ISheet</td>
            <td class="standardText">
                <table name="ja" border="1" cellspacing="0" cellpadding="1" border="0" class="listborder standardText" style="width:600px;">
                    <thead>
                    <tr class="tableHeadingBasicTM">
                        <td><%= bundle.getString("lb_attributename")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_value")%></td>
                    </tr>
                    </thead>
                    <tbody id="isheetAttrData">
                    </tbody>
                </table>
            </td>
        </tr>
        <%
            if (enableSetCheckDuration) {
        %>
        <tr class="standardText autoOption">
            <td class="standardText">Check Duration</td>
            <td>
            <select id="checkDuration" name="checkDuration" value="<%=checkDuration == 0 ? 60 : checkDuration %>">
                <%
                int n = checkDuration == 0 ? 60 : checkDuration;
                for (int i = 1; i < 61; i++){ %>
                     <option value="<%=i%>" <%if(n == i) {%>selected="selected"<%}%>><%=i%></option>
                <%} %>
            </select>
             <%=bundle.getString("lb_blaise_automatic_time_unit")%></td>
        </tr>
        <%
            }
        %>
		<tr class="standardText autoOption">
		  <td class="standardText">Entry Count</td>
		  <td><input type="text" id="qaCountString" name="qaCountString" size=10 value="<%=qaCount == 0 ? "All" : qaCount%>" /></td>
		</tr>
        <tr>
    		<td colspan="2" align="left">
                <input type="button" name="return" value="<%=bundle.getString("lb_cancel")%>" onclick="cancel();"/>
    		    <input type="button" name="saveBtn" value="<%=bundle.getString("lb_save")%>" onclick="save();"/>
    		</td>
    	</tr>
    </table>

    </FORM>
</div>
</div>
<script>
    function fileProfileChanged()
    {
        if ($("#defaultFileProfileId").val() == "") {
			$("#attributeSetName").empty();
			$("#jobAttributes").empty();
			$(".allowNone").hide();
            return;
		}
		
        $("#blaiseForm").ajaxSubmit({
            type: 'post',
            url: "<%=getAttributesURL%>",
            dataType:'json',
            timeout:100000000,
            success: function(data){
				if ($.trim(data) != "")
				{
					$(".allowNone").show();
					$("#attributeSetName").empty().append(data.setName);
					var tdData = "";
					var tdType = "";
					$.each(data.attributes, function(i, item) {
					    var isFalconProduct = false;
						tdData += "<tr class='allowNone autoOption'>";
						if (item.displayName == "Falcon Product")
						    isFalconProduct = true;
                        tdData += "<td class='standardText'>" + item.displayName + "</td>";
						tdType = item.type;

                        if (item.required)
                            tdData += "<td class='standardText'>Required</td>";
                        else
                            tdData += "<td class='standardText'>--</td>";

                        var tmp = item.value;
						if (tdType == "choiceList") {
                            tdData += "<td class='standardText'>Choice List</td>";
                            tdData += "<td class='standardText'>";
                            eles = tmp.split("@@");
                            tdData += "<select ";
                            if (isFalconProduct)
                                tdData += "class='falconProduct' ";
                            if (item.required)
                                tdData += "data-rule=required ";
                            tdData += "id='anyAttr" + item.attrId + "' name='anyAttr" + item.attrId + "'>";
                            for (var k=0;k<eles.length;k++) {
                                items = eles[k].split("$$");
                                tdData += "<option value='" +items[0] + "'>" + items[1] + "</option>";
                            }
                            tdData += "</select></td>";
                        }
						else if (tdType == "text") {
                            tdData += "<td class='standardText'>Text</td>";
                            tdData += "<td class='standardText'>";
                            tdData += "<input class='standardText' type='text' name='anyAttr" + item.attrId + "' id='anyAttr" + item.attrId + "' size=10 ";
                            if ($.trim(tmp) != "")
                                tdData += " maxlength=" + tmp;
							if (item.required)
								tdData += " data-rule=required";
                            tdData += " /></td>";
                        }
						else if (tdType == "integer") {
                            tdData += "<td class='standardText'>Integer</td>";
                            tdData += "<td class='standardText'>";
                            tdData += "<input class='standardText' type='text' name='anyAttr" + item.attrId + "' id='anyAttr" + item.attrId + "' size=10 data-rule=integer";
                            if (item.required)
                                tdData += ";required";
                            if ($.trim(tmp) != "") {
                                var tmpArray = tmp.split(",");
                                var min = 0, max = 9999999;
                                if (tmpArray.length == 2) {
                                    min = tmpArray[0] == "" ? min : tmpArray[0];
                                    max = tmpArray[1] == "" ? max : tmpArray[1];
									tdData += " data-range=" + min + "," + max;
                                }
                            }
                            tdData += " /></td>";
                        }
                        else if (tdType == "float") {
                            tdData += "<td class='standardText'>Float</td>";
                            tdData += "<td class='standardText'>";
                            tdData += "<input class='standardText' type='text' name='anyAttr" + item.attrId + "' id='anyAttr" + item.attrId + "' size=10 data-rule=range";
							
                            if (item.required)
                                tdData += ";required";
                            if ($.trim(tmp) != "") {
                                var tmpArray = tmp.split(",");
                                var min = 0, max = 9999999;
                                if (tmpArray.length == 2) {
                                    min = tmpArray[0] == "" ? min : tmpArray[0];
                                    max = tmpArray[1] == "" ? max : tmpArray[1];
									tdData += " data-range=" + min + "," + max; 
                                }
                            }
                            tdData += " /></td>";
                        }
                        /**
                        else if (tdType == "date")
                            tdData += "<td class='standardText'>Date</td>";
                        else if (tdType == "file")
                            tdData += "<td class='standardText'>File</td>";

						tdData += "<td class='standardText'>";
						if (tdType == "choiceList") {
							var tmp = item.value;
							eles = tmp.split("@@");
							tdData += "<select ";
							if (isFalconProduct)
							    tdData += "class='falconProduct' ";
							tdData += "id='anyAttr" + item.attrId + "' name='anyAttr" + item.attrId + "'>";
							for (var k=0;k<eles.length;k++) {
								items = eles[k].split("$$");
								tdData += "<option value='" +items[0] + "'>" + items[1] + "</option>";
							}
							tdData += "</select>";
						} else {
							tdData += "<input class='standardText' type='text' name='anyAttr" + item.attrId + "' id='anyAttr" + item.attrId + "' size=10 />";
						}
						tdData += "</td>";
                         */
						tdData += "</tr>";
					});
					$("#anyAttrData").empty().append(tdData);
					var tdData1 = tdData.replace(/anyAttr/g, 'hduAttr');
					$("#hduAttrData").empty().append(tdData1);
					tdData1 = tdData.replace(/anyAttr/g, 'isheetAttr');
					$("#isheetAttrData").empty().append(tdData1);
					<%
					if (typeAttributes != null) {
						BlaiseConnectorAttribute typeAttr;
						for (int i=0,size=typeAttributes.size();i<size;i++) {
							typeAttr = typeAttributes.get(i);
							if (typeAttr.getBlaiseJobType().equals("A")) {
								%>
								$("#anyAttr<%=typeAttr.getAttributeId()%>").val("<%=typeAttr.getAttributeValue()%>");
								<%
							} else if (typeAttr.getBlaiseJobType().equals("H")) {
								%>
								$("#hduAttr<%=typeAttr.getAttributeId()%>").val("<%=typeAttr.getAttributeValue()%>");
								<%
							} else if (typeAttr.getBlaiseJobType().equals("I")) {							
								%>
								$("#isheetAttr<%=typeAttr.getAttributeId()%>").val("<%=typeAttr.getAttributeValue()%>");
								<%
							}
						}
					}
					%>
				}
            },
            error: function(XmlHttpRequest, textStatus, errorThrown){
            }
        });
    }
	
	$().ready(function() {
		$(".allowNone").hide();
		$(".autoOption").hide();
		
		showAutoOptions(<%=isAutomatic%>);
		var $fpId = $("#defaultFileProfileId").val();
		if ($fpId != "" && <%=isAutomatic%>) {
			fileProfileChanged();
		}
	});
	
	function showAutoOptions(flag) {
		if (flag) {
			$(".autoOption").show();
			var $fpId = $("#defaultFileProfileId").val();
			<%
				isAutomatic = true;
			%>
			if ($fpId != "" && <%=isAutomatic%>) {
				fileProfileChanged();
			}
		}
		else {
			$(".autoOption").hide();
			<%
				isAutomatic = false;
			%>
		}
		$(".allowNone").hide();
	}
</script>
</body>
</html>