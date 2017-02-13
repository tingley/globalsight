<%@page import="java.text.MessageFormat"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
     com.globalsight.util.edit.EditUtil,
     com.globalsight.util.GlobalSightLocale,
     com.globalsight.everest.webapp.WebAppConstants,
     com.globalsight.cxe.entity.blaise.BlaiseConnector,
	 java.util.*"
	session="true"%>
<%@ page import="com.globalsight.cxe.entity.fileprofile.FileProfileImpl" %>
<%@ page import="com.globalsight.cxe.entity.customAttribute.AttributeSet" %>
<%@ page import="com.globalsight.connector.blaise.form.BlaiseConnectorAttribute" %>

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
	long attributeGroupId = -1L;
	boolean isCombined = true;
	int wordCount = 600;
	String clientCoreVersion = "2.0";// default "2.0".
	long companyId = -1;
    boolean edit = false;
	int qaCount = 10;
	BlaiseConnector connector = (BlaiseConnector) request.getAttribute("blaise");
	ArrayList<FileProfileImpl> fps = (ArrayList<FileProfileImpl>) request.getAttribute("fileProfiles");
    List<AttributeSet> allAttributeSets = (List<AttributeSet>) request.getAttribute("allAttributeSets");
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
        pullDays = connector.getPullDays();
        pullHour = connector.getPullHour();
        isCombined = connector.isCombined();
        fileProfileId = connector.getDefaultFileProfileId();
        attributeGroupId = connector.getJobAttributeGroupId();
        wordCount = connector.getMinProcedureWords();
		qaCount = connector.getQaCount();
	}
	else
	{
		title = bundle.getString("lb_new_blaise_connector");
	}
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
	
	var $tmp = $("#minProcedureWords").val();
	if (!isAllDigits($tmp) || $tmp < 600)
	{
		alert("The minimum word count for procedure is incorrect value or less than 600.");
		$("#minProcedureWords").focus();
		return false;
	}
	
	var $anySelect = $("#anyAttrs select").val();
	var $hduSelect = $("#hduAttrs select").val();
	var $isheetSelect = $("#isheetAttrs select").val();
	if ($anySelect == $hduSelect || $hduSelect == $isheetSelect || $anySelect == $isheetSelect)
	{
		alert("Please set up different Falcon Product for each kind of automatic job.");
		return false;
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
            <td><input type="text" name="username" id="username" style="width: 360px;" value="<%=username%>" maxLength="200" autocomplete="off"></td>
        </tr>
        <tr>
            <td><%=bundle.getString("lb_password")%><span class="asterisk">*</span>:</td>
            <td><input type="password" name="password" id="password" style="width: 360px;" value="<%=password%>" maxLength="200" autocomplete="off"></td>
        </tr>

        <tr>
            <td class="standardText"><%= bundle.getString("lb_blaise_automatic")%>:</td>
            <td class="standardText">
                <input type="radio" name="automatic" value="false" <%=isAutomatic ? "" : "checked"%>  onclick="showAutoOptions(false);"/>No&nbsp;&nbsp;
                <input type="radio" name="automatic" value="true" <%=isAutomatic ? "checked" : ""%>  onclick="showAutoOptions(true);"/>Yes
            </td>
        </tr>
        <tr class="autoOption">
            <td width="180px" class="standardText"><%= bundle.getString("lb_blaise_pull_time")%>:</td>
            <td class="standardText">
                <ul style="display: inline-flex;list-style-type: none;margin-left: 0px;padding-left: 0px;">
                    <li><input type="checkbox" name="monday" value="1" <%=pullDays.contains("1") ? "checked" : ""%>>Monday</li>
                    <li><input type="checkbox" name="thursday" value="2" <%=pullDays.contains("2") ? "checked" : ""%>>Thursday</li>
                    <li><input type="checkbox" name="wednesday" value="3" <%=pullDays.contains("3") ? "checked" : ""%>>Wednesday</li>
                    <li><input type="checkbox" name="tuesday" value="4" <%=pullDays.contains("4") ? "checked" : ""%>>Tuesday</li>
                    <li><input type="checkbox" name="friday" value="5" <%=pullDays.contains("5") ? "checked" : ""%>>Friday</li>
                    <li><input type="checkbox" name="saturday" value="6" <%=pullDays.contains("6") ? "checked" : ""%>>Saturday</li>
                    <li><input type="checkbox" name="sunday" value="7" <%=pullDays.contains("7") ? "checked" : ""%>>Sunday</li>
                </ul>
            </td>
        </tr>
        <tr class="autoOption">
            <td>&nbsp;</td>
            <td class="standardText">
                <select id="pullHour" name="pullHour" class="standardText">
                    <%
                        for (int i=7;i<16;i++) {
                    %>
                    <option value="<%=i%>" <%=pullHour == i ? "selected" : ""%>><%=i%>:00</option>
                    <% } %>
                </select>
            </td>
        </tr>
        <tr class="autoOption">
            <td class="standardText">Combined by language:</td>
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
            <td class="standardText"><%= bundle.getString("lb_attribute_groups")%>:</td>
            <td class="standardText"><div id="attributeSetName"></div></td>
        </tr>
        <tr class="allowNone autoOption" id="anyAttrs">
			<td class="standardText">Any</td>
            <td class="standardText">
                <table name="ja" border="1" cellspacing="0" cellpadding="1" border="0" class="listborder standardText" style="width:600px;">
                    <thead>
                    <tr class="tableHeadingBasicTM">
                        <td><%= bundle.getString("lb_attributename")%></td>
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
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
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
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
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
                        <td><%= bundle.getString("lb_value")%></td>
                    </tr>
                    </thead>
                    <tbody id="isheetAttrData">
                    </tbody>
                </table>
            </td>
        </tr>
		<tr class="standardText autoOption">
		  <td class="standardText">Entry count(QA)</td>
		  <td><input type="text" id="qaCount" name="qaCount" size=10 value="<%=qaCount%>" /></td>
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
		$(".allowNone").show();
        $("#blaiseForm").ajaxSubmit({
            type: 'post',
            url: "<%=getAttributesURL%>",
            dataType:'json',
            timeout:100000000,
            success: function(data){
                $("#attributeSetName").empty().append(data.setName);
                var tdData = "";
                var tdType = "";
				$.each(data.attributes, function(i, item) {
					tdData += "<tr>";
					tdData += "<td class='standard'>" + item.displayName + "</td>";
					tdType = item.type;

					if (tdType == "choiceList")
						tdData += "<td class='standard'>Choice List</td>";
					else if (tdType == "text")
						tdData += "<td class='standard'>Text</td>";
					
					if (item.required)
						tdData += "<td class='standard'>Required</td>";
					else
						tdData += "<td class='standard'>--</td>";
					
					tdData += "<td class='standard'>";
					if (tdType == "choiceList") {
						var tmp = item.value;
						eles = tmp.split("@@");
						tdData += "<select id='anyAttr" + item.attrId + "' name='anyAttr" + item.attrId + "'>";
						for (var k=0;k<eles.length;k++) {
							items = eles[k].split("$$");
							tdData += "<option value='" +items[0] + "'>" + items[1] + "</option>";
						}
						tdData += "</select>";
					} else if (tdType = "text") {
						tdData += "<input type='text' name='anyAttr" + item.attrId + "' id='anyAttr" + item.attrId + "' size=10 />";
					}
					tdData += "</td>";
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
		if ($fpId != "") {
			fileProfileChanged();
		}
	});
	
	function showAutoOptions(flag) {
		if (flag)
			$(".autoOption").show();
		else
			$(".autoOption").hide();
	}
</script>
</body>
</html>