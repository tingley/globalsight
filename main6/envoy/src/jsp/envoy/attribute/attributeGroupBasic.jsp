<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/activityError.jsp"
	import="com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.util.edit.EditUtil,
	com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant,
    com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile.AttributeComparator,
	com.globalsight.cxe.entity.customAttribute.Attribute,
	com.globalsight.cxe.entity.customAttribute.AttributeSet,
	com.globalsight.cxe.entity.customAttribute.Condition,
	com.globalsight.cxe.entity.customAttribute.ListCondition,
	com.globalsight.cxe.entity.customAttribute.IntCondition,
	com.globalsight.everest.company.CompanyWrapper,
	com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager,
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

	AttributeSet attributeSet = (AttributeSet) request
			.getAttribute(AttributeConstant.ATTRIBUTE_SET);

	String title = null;
	boolean edit = false;
	if (attributeSet != null) {
		edit = true;
		title = bundle.getString("lb_edit_attribute_group");
	} else {
		title = bundle.getString("lb_new_attribute_group");
	}
	String helper = bundle.getString("helper_text_attribute_group_basic");
	
	List<Attribute> selectedAttributes = (List<Attribute>)request.getAttribute("selectedAttributes");
	List<Attribute> allAttributes = (List<Attribute>)request.getAttribute("allAttributes");

	String name = "";
	String desc = "";
	String attributeSetId = "-1";
	String otherUrl = null;
	
	if (attributeSet != null)
	{
	    attributeSetId = Long.toString(attributeSet.getId());;
	    name = attributeSet.getName();
	    desc = attributeSet.getDescription();
	    validateURL += "&id=" + attributeSetId;
	}
%>

<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/dojo/dojo.js" djConfig="parseOnLoad: true"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/util.js" ></SCRIPT>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>

<script language="JavaScript" type="text/javascript"> 
var needWarning = true;
var objectName = "<%=bundle.getString("lb_attribute_group")%>";
var guideNode="attributeGroups";
var helpFile = "<%=bundle.getString("help_attribte_group_basic_screen")%>";

dojo.require("dojo.dnd.Container");
dojo.require("dojo.dnd.Manager");
dojo.require("dojo.dnd.Source");

function submitAttributeForm(formAction)
{
    if (formAction == "cancel")
    {
        AttributeForm.action = "<%=cancelURL%>";
        AttributeForm.submit();
    }

    if (formAction == "save")
    {
        if (confirmAttributeForm())
        {
        	validateAndSubmit();
        }
    }
}

function validateAndSubmit()
{
	var obj = {
		name : document.getElementById("name").value
	}

	validate(true, obj);
}

function validateName()
{
	var obj = {
		name : document.getElementById("name").value
	}

	validate(false, obj);
}

function updateAttribute()
{
	var allItems = document.getElementById("allItems");
    var options =  allItems.options;
    for (var i = options.length-1; i>=0; i--)
    {
    	allItems.remove(i);       
    }
	
    var selectedDiv = document.getElementById("selected");
    var nodes = selectedDiv.childNodes;
    
    for (var i=0; i < nodes.length; i++)
    {
        var nodeId = nodes[i].id;
        if (nodeId)
        {
            var option = document.createElement("option");
            option.appendChild(document.createTextNode(nodeId));
            option.setAttribute("value", nodeId);
            option.setAttribute("selected", true);
            allItems.appendChild(option);
        }
    }
    
}

function validate(isForm, obj)
{
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
                	updateAttribute();
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
                    document.getElementById('nameMsg').innerHTML=returnData.error;
                }
            }
        },
        error:function(error)
        {
            alert(error.message);
        }
    });
}

function confirmAttributeForm()
{
    if (isEmptyString(AttributeForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("msg_name_null"))%>");
        AttributeForm.name.value = "";
        AttributeForm.name.focus();
        return false;
    }

    if (hasSpecialChars(AttributeForm.name.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%> " +
              "<%=EditUtil.toJavascript(bundle
							.getString("msg_invalid_entry"))%>");
        AttributeForm.name.focus();
        return false;
    }

    return true;
}

function initNameMsg()
{
    document.getElementById("nameMsg").innerHTML="";
}

function checkName()
{
    validateName(false);
}
</script>

<style type="text/css">
@import url(/globalsight/includes/dojo.css);
@import url(/globalsight/includes/attribute.css);
</style>


</head>
<body id="idBody" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<amb:header title="<%=title%>" helperText="<%=helper%>" /> 
<span class=errorMsg></span>

<div style="float: left">
<FORM name="AttributeForm" method="post" action="">
<input type="hidden" name="id" value="<%=attributeSetId%>"> 
<div style="float: left">
<table class="standardText">
     <tr>
        <td>&nbsp;</td>
        <td class="errorMsg" id="nameMsg">&nbsp;</td>
    </tr>
	<tr>
		<td><%=bundle.getString("lb_name")%><span class="asterisk">*</span>:</td>
		<td><input type="text" name="name" id="name" style="width: 160px;" value="<%=name%>" onfocus="initNameMsg()" onblur="checkName()" maxLength="40"></td>
	</tr>
	<tr>
		<td valign="top"><%=bundle.getString("lb_description")%>:</td>
		<td><textarea rows="8" style="width: 300px;" name="description"><%=desc%></textarea></td>
	</tr>
	</tr>
	<tr>
		<td colspan="2" align="left">
		<input type="button" name="return" value="<%=bundle.getString("lb_cancel")%>" onclick="submitAttributeForm('cancel')"/> 
		<input type="button" name="saveBtn" value="<%=bundle.getString("lb_save")%>" onclick="submitAttributeForm('save')">
		</td>
	</tr>
</table>
</div>
<div style="display:none">
<select name="allItems" multiple="multiple" id="allItems">
</select>
</div>
<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_ATTRIBUTE_GROUP); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />
</FORM>
</div>
	<div id="dragLists" class="standardText">
		  <div class="attributeTopDiv">
		    <div><%=bundle.getString("lb_selected_attributes")%>:</div>
		    <div class="wrap1">
			    <div dojoType="dojo.dnd.Source"  id="selected" class="dndContainer">
			    <%for (Attribute att : selectedAttributes){
			        boolean isSuperAtt = 1 == att.getCompanyId();
			    %>
			       <div class="dojoDndItem" id='<%=att.getId()%>'>
			            <%if (isSuperAtt) {%>
		                <div class="superAttribute">
		                <%} %>
		                <%=att.getDisplayName()%>
		                <%if (isSuperAtt) {%>
		                </div>
		                <%} %>
			       </div>	           
				<%}%>
			    </div>
		    </div>
		  </div>
		  <div class="attributeTopDiv">
		    <div><%=bundle.getString("lb_unselected_attributes")%>:</div>
		    <div class="wrap1">
			    <div dojoType="dojo.dnd.Source" class="dndContainer">
			    <%for (Attribute att : allAttributes){
			          boolean isSuperAtt = 1 == att.getCompanyId();
			          if (!selectedAttributes.contains(att)){%>
			       		<div class="dojoDndItem" id='<%=att.getId()%>'>
				       		<%if (isSuperAtt) {%>
			                <div class="superAttribute">
			                <%} %>
			                <%=att.getDisplayName()%>
			                <%if (isSuperAtt) {%>
			                </div>
			                <%} %>
			       		</div>	           
				<%}}%>
			    </div>
			</div>
		  </div>	  
	</div>
</div>


</body>
</html>
