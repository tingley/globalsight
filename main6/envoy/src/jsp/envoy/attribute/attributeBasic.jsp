<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.util.edit.EditUtil,
	com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant,
	com.globalsight.cxe.entity.customAttribute.Attribute,
	com.globalsight.cxe.entity.customAttribute.Condition,
	com.globalsight.cxe.entity.customAttribute.ListCondition,
	com.globalsight.cxe.entity.customAttribute.IntCondition,
	com.globalsight.cxe.entity.customAttribute.FloatCondition,
	com.globalsight.cxe.entity.customAttribute.TextCondition,
	com.globalsight.util.FormUtil,
	java.util.*"
	session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);

	String saveURL = save.getPageURL() + "&action="
			+ AttributeConstant.SAVE;
	String cancelURL = cancel.getPageURL() + "&action="
			+ AttributeConstant.CANCEL;
	String validateURL = self.getPageURL() + "&action="
			+ AttributeConstant.VALIDATE;

	Attribute attribute = (Attribute) request
			.getAttribute(AttributeConstant.ATTRIBUTE);

	String title = null;
	boolean edit = false;
	if (attribute != null) {
		edit = true;
		title = bundle.getString("lb_edit_attribute_template");
	} else {
		title = bundle.getString("lb_new_attribute_template");
	}
	String helper = bundle.getString("helper_text_attribute");

	String name = "";
	String displayName = "";
	String desc = "";
	String attributeId = "-1";
	String editable = "checked";
	String required = "";
	String visiable = "checked";
	String otherUrl = null;
	String type = "text";
	String isDisable = "";
	
	//text
	String textLength = "";
	
	//int
    String intMax = "";
    String intMin = "";
    String floatMax = "";
    String floatMin = "";
	
	//list
	String multipleChoice = "";	
	List<String> options = new ArrayList<String>();

	if (attribute != null) {

	    name = attribute.getName();
		if (name == null) {
		    name = "";
		}
		
		isDisable = name.startsWith(Attribute.PROTECT_NAME_PREFIX)? "disabled=\"disabled\"" : "";
		
		displayName = attribute.getDisplayName();
		if (displayName == null) {
		    displayName = "";
		}

		desc = attribute.getDescription();
		if (desc == null) {
			desc = "";
		}
		
		type = attribute.getType();
		attributeId = Long.toString(attribute.getId());
		editable = attribute.getEditable() ? "checked" : "";
		required = attribute.isRequired() ? "checked" : "";
		visiable = attribute.isVisible() ? "checked" : "";
		validateURL += "&id=" + attribute.getId();
		
		Condition condition = attribute.getCondition();
        if (condition instanceof ListCondition){
            ListCondition listCondition = (ListCondition) condition;
            options = listCondition.getOptions();
            multipleChoice = listCondition.isMultiple() ? "checked" : "";
        } else if (condition instanceof IntCondition){
            IntCondition intCondition = (IntCondition) condition;
            Integer maxValue = intCondition.getMax();
            Integer minValue = intCondition.getMin();
            intMax = maxValue != null ? Integer.toString(maxValue) : "";
            intMin = minValue != null ? Integer.toString(minValue) : "";
        } else if (condition instanceof FloatCondition){
            FloatCondition floatCondition = (FloatCondition) condition;
            Float maxValue = floatCondition.getMax();
            Float minValue = floatCondition.getMin();
            floatMax = maxValue != null ? Float.toString(maxValue) : "";
            floatMin = minValue != null ? Float.toString(minValue) : "";
        } else if (condition instanceof TextCondition){
            TextCondition textCondition = (TextCondition) condition;
            Integer maxLength = textCondition.getLength();
            textLength = maxLength != null ? Integer.toString(maxLength) : "";
        }
	}
%>

<html>
<head>
<title><%=title%></title>
<style type="text/css">
.detailDivClass {
    float: left;
    left: 410px;
    position: absolute;
    width: 400px;
}
</style>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js" ></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<script>
var needWarning = true;
var objectName = "";
var guideNode="attributes";
var helpFile = "<%=bundle.getString("help_attribte_basic_screen")%>";
var typeIndex = 0;
var types = new Array();
var validateURL = "<%=validateURL%>";

types[0] = "textDiv";
types[1] = "integerDiv";
types[2] = "floatDiv";
types[3] = "choiceListDiv";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        AttributeForm.action = "<%=cancelURL%>";
        AttributeForm.submit();
    }

    if (formAction == "save")
    {
        if (confirmForm())
        {
        	validateAll();
        }
    }
}

function isListAttribute()
{
	return AttributeForm.type.value == "choiceList";
}

function validateName()
{
	var obj = {
		name : dojo.byId("name").value
	}

	validate(false, obj);
}

function validateTextLength()
{
	var obj = {
		textLength : dojo.byId("textLength").value
	}

	validate(false, obj, "detailMsg");
}

function validateIntMin()
{
	var obj = {
		intMin : dojo.byId("intMin").value
	}

	validate(false, obj, "detailMsg");
}

function validateIntMax()
{
	var obj = {
		intMax : dojo.byId("intMax").value
	}

	validate(false, obj, "detailMsg");
}

function validateFloatMin()
{
	var obj = {
		floatMin : dojo.byId("floatMin").value
	}

	validate(false, obj, "detailMsg");
}

function validateFloatMax()
{
	var obj = {
		floatMax : dojo.byId("floatMax").value
	}

	validate(false, obj, "detailMsg");
}

function validateDisplayName()
{
	var obj = {
		displayName : document.getElementById("displayName").value
	}

	validate(false, obj);
}

function validateAll()
{
	var obj = {
		name : dojo.byId("name").value,
		displayName : dojo.byId("displayName").value
	}

	var type = dojo.byId("type").value;

	if ("text" == type)
	{
		obj.textLength = dojo.byId("textLength").value;
	}
	else if ("integer" == type)
	{
		obj.intMin = dojo.byId("intMin").value;
		obj.intMax = dojo.byId("intMax").value;
	}
	else if ("float" == type)
	{
		obj.floatMin = dojo.byId("floatMin").value;
		obj.floatMax = dojo.byId("floatMax").value;
	}

	validate(true, obj);
}

function validate(isForm, obj, msgId)
{
	if (msgId == null)
	{
		msgId = "nameMsg";
	}
	
    dojo.xhrPost(
    {
        url:"<%=validateURL%>",
        handleAs: "text", 
        content:obj,
        load:function(data)
        {
            if (data=="")
            {
                if (isForm)
                {
                    if (isListAttribute())
                    {
                        var options =  document.getElementById("allItems").options;

                        if (options.length == 0)
                        {
                        	alert('<%=bundle.getString("msg_attribute_option_null")%>');
                        	return;
                        }
                        
                        for (var i = 0; i < options.length; i++)
                        {
                            options[i].selected = true;
                        }
                    }
                    
                    AttributeForm.action = "<%=saveURL%>";
                    AttributeForm.submit();
                }
            }
            else
            {
            	var returnData = eval(data);
                if (isForm)
                {
                    alert(returnData.error);
                }
                else
                {
                    document.getElementById(msgId).innerHTML=returnData.error;
                }
            }
        },
        error:function(error)
        {
            alert(error.message);
        }
    });
}

function confirmForm()
{
    if (isEmptyString(AttributeForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("msg_internal_name_null"))%>");
        AttributeForm.name.focus();
        return false;
    }
    
    if (hasSpecialChars(AttributeForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_internal_name"))%> " +
              "<%=EditUtil.toJavascript(bundle
							.getString("msg_invalid_entry"))%>");
        AttributeForm.displayName.focus();
        return false;
    }
    

    if (isEmptyString(AttributeForm.displayName.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("msg_display_name_null"))%>");
        AttributeForm.displayName.focus();
        return false;
    }

    if (hasHtmlSpecialChars(AttributeForm.displayName.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_display_name"))%> " +
              "<%=EditUtil.toJavascript(bundle
							.getString("msg_html_special_char"))%>");
        AttributeForm.displayName.focus();
        return false;
    }

    return true;
}

function initNameMsg()
{
    document.getElementById("nameMsg").innerHTML="";
}

function initDetailMsgMsg()
{
    document.getElementById("detailMsg").innerHTML="";
}

function addOption(box, name, value)
{
    var option = document.createElement("option");
    var selectBox = document.getElementById(box);
    option.appendChild(document.createTextNode(name));
    option.setAttribute("value", value);
    selectBox.appendChild(option);
}

function changeType()
{
	initDetailMsgMsg();
	displayAll();
	var type = document.getElementById("type").value;
	fadeIn(type + "Div");
}

function isExist(text)
{
	var selectBox = document.getElementById("allItems");
    var options = selectBox.options;
    for (var i = options.length-1; i>=0; i--)
    {
        if (options[i].value.toLowerCase() == text.toLowerCase())
        {
        	return true;
        }
    }
    
    return false;
}

function setOptionColor()
{
	var options =  document.getElementById("allItems").options;
	var flag = true;
    for (var i = 0; i<options.length; i++)
    {
		if (flag)
		{
		    options[i].className="row1";
			flag = false;
		}
        else
		{
			options[i].className="row2";
			flag = true;
		}
    }
}

function onAdd()
{
    var value =  document.getElementById("option").value;
    value = trim(value);

    if (value.length > 0)
    {
    	if (value.indexOf(",") > -1)
   		{
    		alert("Cannot input comma: " + value);
   		}
        if (isExist(value))
        {
            alert(value + " " + '<%=bundle.getString("msg_has_exist")%>');
            return;
        }
        
        addOption("allItems", value, value);
        document.getElementById("option").value = "";
        
        setOptionColor();
    }
}

function onDelete()
{
	var selectBox = document.getElementById("allItems");
    var options =  selectBox.options;
    for (var i = options.length-1; i>=0; i--)
    {
        if (options[i].selected)
        {
        	selectBox.remove(i);
        }
    }
    
    setOptionColor();
}

function displayAll()
{
	quickFadeOut("choiceListDiv");
	quickFadeOut("floatDiv");
	quickFadeOut("integerDiv");
	quickFadeOut("textDiv");
}

function init()
{
	initSelect();
	initDiv();
	setOptionColor();
}

function initSelect()
{
    var attributeType = "<%=type%>";
    
    var options =  document.getElementById("type").options;
    for (var i = options.length-1; i>=0; i--)
    {
        
        if (options[i].value == attributeType)
        {
        	options[i].selected = true;
        	typeIndex = i;
        }
    }
}

function initDiv()
{
	for (var i = 0; i < types.length; i++)
	{
		if (i != typeIndex)
		{
			quickFadeOut(types[i]);
		}
	}
}
</script>

<style type="text/css">
@import url(/globalsight/dijit/themes/tundra/attribute.css);
</style>
</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<amb:header title="<%=title%>" helperText="<%=helper%>" /> 
<span class=errorMsg></span>

<div style="float: left;">
<FORM name="AttributeForm" method="post" action="">
<input type="hidden" name="id" value="<%=attributeId%>"> 
<div style="float: left; width: 400;">
<table class="standardText">
     <tr>
        <td>&nbsp;</td>
        <td class="errorMsg" id="nameMsg">&nbsp;</td>
    </tr>
	<tr>
		<td><%=bundle.getString("lb_internal_name")%><span class="asterisk">*</span>:</td>
		<td><input type="text" name="name" id="name" style="width: 160px;" value="<%=name%>" <%=isDisable%> onfocus="initNameMsg()" onblur="validateName()" maxLength="40"></td>
	</tr>
	<tr>
		<td><%=bundle.getString("lb_display_name")%><span class="asterisk">*</span>:</td>
		<td><input type="text" name="displayName" id="displayName" style="width: 160px;" value="<%=displayName%>" onfocus="initNameMsg()" onblur="validateDisplayName()" maxLength="40"></td>
	</tr>
	<tr>
		<td><%=bundle.getString("lb_type")%><span class="asterisk">*</span>:</td>
		<td><select name="type" id="type" onchange="changeType()" style="width: 165px;" >
			<option value="text"><%=bundle.getString("lb_attribute_type_text")%></option>
			<option value="integer"><%=bundle.getString("lb_attribute_type_integer")%></option>
			<option value="float"><%=bundle.getString("lb_attribute_type_float")%></option>
			<option value="choiceList"><%=bundle.getString("lb_attribute_type_choiceList")%></option>
			<option value="date"><%=bundle.getString("lb_attribute_type_date")%></option>
			<option value="file"><%=bundle.getString("lb_attribute_type_file")%></option>
		</select></td>
	</tr>
	<tr>
		<td><%=bundle.getString("lb_visible")%>:</td>
		<td><input type="checkbox" name="visible" value="true" <%=visiable %>/></td>
	</tr>
	<tr>
		<td><%=bundle.getString("lb_can_be_edit_from_ui")%>:</td>
		<td><input type="checkbox" name="editable" value="true" <%=editable %>/></td>
	</tr>
	<tr>
		<td><%=bundle.getString("lb_required")%>:</td>
		<td><input type="checkbox" name="required" value="true" <%=required %>/></td>
	</tr>
	<tr>
		<td valign="top"><%=bundle.getString("lb_description")%>:</td>
		<td><textarea rows="8" style="width: 300px;" name="description"><%=desc%></textarea></td>
	</tr>
	<tr>
		<td colspan="2" align="left">
		    <input type="button" name="return" value="<%=bundle.getString("lb_cancel")%>" onclick="submitForm('cancel')"/> 
		    <input type="button" name="saveBtn" value="<%=bundle.getString("lb_save")%>" onclick="submitForm('save')">
		</td>
	</tr>
</table>
</div>

<div id="detailDiv" class="detailDivClass">
     <div class="errorMsg" id="detailMsg">&nbsp;</div>
     
	<div id="textDiv" class="detailItemDiv">
	<table class="standardText">
		<tr>
			<td><%=bundle.getString("lb_max_length")%>:</td>
			<td><input type="text" name="textLength" id="textLength" value="<%=textLength%>" validchars="-+0123456789" onkeypress="return allowChars(this, event)" onfocus="initDetailMsgMsg()" onblur="validateTextLength()"/></td>
		</tr>
	</table>
	</div>

	<div id="integerDiv" class="detailItemDiv">
	<table class="standardText">
		<tr>
			<td><%=bundle.getString("lb_min_value")%>:</td>
			<td><input type="text" name="intMin" id="intMin" value="<%=intMin %>" validchars="-+0123456789" onkeypress="return allowChars(this, event)" onfocus="initDetailMsgMsg()" onblur="validateIntMin()"/></td>
		</tr>
		<tr>
			<td><%=bundle.getString("lb_max_value")%>:</label></td>
			<td><input type="text" name="intMax" id="intMax" value="<%=intMax %>" validchars="-+0123456789" onkeypress="return allowChars(this, event)" onfocus="initDetailMsgMsg()" onblur="validateIntMax()"/></td>
		</tr>
	</table>
	</div>
	
	<div id="floatDiv" class="detailItemDiv">
	<table class="standardText">
		<tr>
			<td><%=bundle.getString("lb_min_value")%>:</td>
			<td><input type="text" name="floatMin" id="floatMin"  value="<%=floatMin%>" validchars="-+0123456789." onkeypress="return allowChars(this, event)" onfocus="initDetailMsgMsg()" onblur="validateFloatMin()"/></td>
		</tr>
		<tr>
			<td><%=bundle.getString("lb_max_value")%>:</td>
			<td><input type="text" name="floatMax" id="floatMax" value="<%=floatMax%>" validchars="-+0123456789." onkeypress="return allowChars(this, event)" onfocus="initDetailMsgMsg()" onblur="validateFloatMax()"/></td>
		</tr>
		<!-- 
		 <tr>
			<td>Definition:</td>
			<td><input type="text" name="definition" id="definition" /></td>
		</tr>
		 -->
	</table>
	</div>

	<div id="choiceListDiv" class="detailItemDiv">
	<table class="standardText">
		<tr>
			<td width="100px;"><%=bundle.getString("lb_option")%>:</td>
			<td style="width:300px;"><input type="text" name="option" id="option" style="width:100%;"  maxLength="50"/></td>
			<td><input type="button" name="add" id="add" value="<%=bundle.getString("lb_add")%>" onclick="onAdd()" /></td>
			<td><input type="button" name="delete" id="delete" value="<%=bundle.getString("lb_delete")%>" onclick="onDelete()" /></td>
		</tr>
		<tr>
			<td><%=bundle.getString("lb_multiple_choice")%>:</td>
			<td colspan="3"><input type="checkbox" name="multipleChoice" <%=multipleChoice%>/></td>
		</tr>
		<tr>
			<td valign="top"><%=bundle.getString("lb_all_options")%>:</td>
			<td colspan="3">
			<select name="allItems" multiple="multiple" id="allItems" size="10" style="width: 100%;">
			<%
			for (String option : options)
			{
			    %>
			    <option value="<%=EditUtil.encodeHtmlEntities(option)%>"><%=EditUtil.encodeHtmlEntities(option)%></option>
			    <% 
			}
			%>
			</select></td>
		</tr>
	</table>
	<script type="text/javascript">init();</script>
	</div>
</div>
<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_ATTRIBUTE); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
</FORM>
</div>
</div>
</body>
</html>