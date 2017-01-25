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
	BlaiseConnector connector = (BlaiseConnector) request.getAttribute("blaise");
	ArrayList<FileProfileImpl> fps = (ArrayList<FileProfileImpl>) request.getAttribute("fileProfiles");
    List<AttributeSet> allAttributeSets = (List<AttributeSet>) request.getAttribute("allAttributeSets");
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
    <%if(edit) {%>
    <input type="hidden" name="companyId" value="<%=companyId%>" />
    <%} %>
    <table class="standardText">
    	<tr>
    		<td width="120px"><%=bundle.getString("lb_name")%> <span class="asterisk">*</span>:</td>
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
                <input type="radio" name="automatic" value="false" <%=isAutomatic ? "" : "checked"%> />No&nbsp;&nbsp;
                <input type="radio" name="automatic" value="true" <%=isAutomatic ? "checked" : ""%> />Yes
            </td>
        </tr>
        <tr>
            <td class="standardText"><%= bundle.getString("lb_blaise_pull_time")%>:</td>
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
        <tr>
            <td>&nbsp;</td>
            <td class="standardText">
                <select id="pullHour" class="standardText">
                    <%
                        for (int i=7;i<16;i++) {
                    %>
                    <option value="<%=i%>" <%=pullHour == i ? "selected" : ""%>><%=i%>:00</option>
                    <% } %>
                </select>
            </td>
        </tr>
        <tr>
            <td class="standardText">Combined by language:</td>
            <td class="standardText"><input type="checkbox" id="combined" name="combined" value="true" <%=isCombined ? "checked" : ""%>/></td>
        </tr>
        <tr>
            <td class="standardText"><%= bundle.getString("lb_blaise_min_procedure_words")%>:</td>
            <td class="standardText">
                <input type="text" id="minProcedureWords" name="minProcedureWords" value="<%=wordCount%>" class="standardText" />
            </td>
        </tr>
        <tr>
            <td class="standardText"><%= bundle.getString("lb_default_file_profile")%>:</td>
            <td class="standardText">
                <select id="defaultFileProfileId" name="defaultFileProfileId" class="standardText">
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
        <tr>
            <td class="standardText"><%= bundle.getString("lb_attribute_groups")%>:</td>
            <td class="standardText">
                <select id="jobAttributeGroupId" name="jobAttributeGroupId" class="standardText">
                    <option value="-1">Choose...</option>
                    <%
                        if (allAttributeSets != null && allAttributeSets.size() > 0) {
                            for (AttributeSet as : allAttributeSets) {
                                if (as.getId() == attributeGroupId)
                                    out.println("<option value=" + as.getId() + " selected>" + as.getName() + "</option>");
                                else
                                    out.println("<option value=" + as.getId() + ">" + as.getName() + "</option>");
                            }
                        }
                    %>
                </select>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <table id="ja" name="ja" border="1" cellspacing="0" cellpadding="1" border="0" class="listborder standardText" style="width:600px;">
                    <thead>
                    <tr class="tableHeadingBasicTM">
                        <td><%= bundle.getString("lb_attributename")%></td>
                        <td><%= bundle.getString("lb_type")%></td>
                        <td><%= bundle.getString("lb_required")%></td>
                        <td><%= bundle.getString("lb_value")%></td>
                    </tr>
                    </thead>
                    <tbody id="jobAttributes">
                    <tr>
                        <td>Falcon Product</td>
                        <td>Choise List</td>
                        <td>Yes</td>
                        <td>HDU CBT</td>
                    </tr>
                    <tr>
                        <td>Falcon Target Choise</td>
                        <td>Choise List</td>
                        <td>No</td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td>Falcon Target Value</td>
                        <td>Text</td>
                        <td>No</td>
                        <td>&nbsp;</td>
                    </tr>
                    </tbody>
                </table>
            </td>
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
    function changeAttributeGroup()
    {
        if ($("#jobAttributeGroupId").val() == "-1")
            return;
        $.ajax({
            url:'blaiseAjax.jsp',
            type:'POST', //GET
            async:true,    //或false,是否异步
            data:{
                type:'jag'
            },
            timeout:5000,    //超时时间
            dataType:'text',    //返回的数据格式：json/xml/html/script/jsonp/text
            success:function(data,textStatus,jqXHR){
                if (data != "") {
                    var tableContent = "";
                    var attributes = data.split(";");
                    for (var i=0;i<attributes.length;i++) {
                        var attrData = attributes[i].split(",");
                        tableContent += "<tr><td class='standardText'>" + attrData[1] + "</td><td>" + attrData[2] + "</td>";
                        tableContent += "<td>";
                        if (attrData[3] == "Y")
                            tableContent += "Yes";
                        else
                            tableContent += "No";
                    }
                }
            },
            error:function(xhr,textStatus){
            },
            complete:function(){
                console.log('结束')
            }
        })

    }
</script>
</body>
</html>