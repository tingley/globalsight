<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         session="true"
%>
<html>
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><c:out value="${lb_job_attributes}"/></TITLE>
<style type="text/css">
@import url(/globalsight/dijit/themes/tundra/attribute.css);
.fileDiv{
	width:auto !important;
	min-width:50px;
	display:inline-block;
 	min-height:20px;
	cursor:pointer;
	border-width: 1px;
	border-style: solid;
	border-color: #C0C0C0 #C0C0C0 #9B9B9B;
	-moz-border-top-colors: none;
	-moz-border-right-colors: none;
	-moz-border-bottom-colors: none;
	-moz-border-left-colors: none;
	border-image: none;
	border-radius: 2px;
	padding: 0.1em 0.2em 0.2em;
	background: #FFF url("dijit/themes/tundra/images/buttonEnabled.png") repeat-x scroll left bottom;
 }

</style>
<link rel="stylesheet" type="text/css" href="/globalsight/jquery/jQueryUI.redmond.css"/>
<SCRIPT type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/includes/report/calendar.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT type="text/javascript">

$(document).ready(function() {
	$(".standardBtn_mouseout").mouseover(function(){
		$(this).removeClass("standardBtn_mouseout").addClass("standardBtn_mouseover");
	}).mouseout(function(){
		$(this).removeClass("standardBtn_mouseover").addClass("standardBtn_mouseout");
	});
	
	$("#closeWin").click(function() {
		window.close();
	});
	
	$("#saveWin").click(function() {
		// first validate required input
		var flag = true;
		$("[id$='_req']").each(function(){
			var id = $(this).attr("id");
			var reqVal = $(this).val();
			
			var thisInput = $("#" + id.substring(0, id.length - 4)).val();
			if (reqVal == "true" && thisInput == "") {
				alert("Some required attributes must be set.");
				flag = false;
				return flag;
			}
		});
		if (flag) {
			var arrayOfAttributeIds = new Array();
			var arrayOfInputValues = new Array();
			var arrayOfAttributeConditions = new Array();
			$("[name='attributeId']").each(function(){
				arrayOfAttributeIds.push($(this).val());
			});
			$("[name='inputValue']").each(function(){
				arrayOfInputValues.push($(this).val());
			});
			$("[name='attributeConditionType']").each(function(){
				arrayOfAttributeConditions.push($(this).val());
			});
			
			var attributeString = "";
			for (var i = 0; i < arrayOfAttributeIds.length; i++) {
				switch(parseInt(arrayOfAttributeConditions[i])) {
				case 6: // save file list
					var fileAttributeId = arrayOfAttributeIds[i];
					var fileAttributeName = $("#jobAtt" + fileAttributeId).val();
					var uploadedFileNames = $("#file" + fileAttributeName).html();
					attributeString += arrayOfAttributeConditions[i] + ",.," + fileAttributeId + ",.," + uploadedFileNames + ";.;";
					break;
				default:// save text, int, float, date and select input
					if (arrayOfInputValues[i] == "" || arrayOfInputValues[i] == null) {
						attributeString += arrayOfAttributeConditions[i] + ",.," + arrayOfAttributeIds[i] + ",.,;.;";
					} else if (isArray(arrayOfInputValues[i]) && arrayOfInputValues[i].length > 0) {
						attributeString += arrayOfAttributeConditions[i] + ",.," + arrayOfAttributeIds[i] + ",.," + arrayOfInputValues[i].join("#@#") + ";.;";
					} else {
						attributeString += arrayOfAttributeConditions[i] + ",.," + arrayOfAttributeIds[i] + ",.," + arrayOfInputValues[i] + ";.;";
					}
					break;
				}
			}
			
			if (attributeString.length > 0) {
				attributeString = attributeString.substring(0, attributeString.length - 3);
			}
			
			$("#attributeString", window.opener.document).val(attributeString);
			window.close();
		}
	});
});

function onload() {
	var lastValue = $("#attributeString", window.opener.document).val();
	if (lastValue != "") {
		var attributeValues = lastValue.split(";.;");
		for(var i = 0; i < attributeValues.length; i++) {
			var value = attributeValues[i].split(",.,");
			var conditionType = value[0];
			var attributeId = value[1];
			var attributeValue = value[2];
			
			switch(parseInt(conditionType)) {
			case 6: 
				//var fileAttributeId = arrayOfAttributeIds[i];
				var fileAttributeName = $("#jobAtt" + attributeId).val();
				$("#input" + fileAttributeName).val(attributeValue);
				$("#file" + fileAttributeName).html(attributeValue);
				break;
			default:// save text, int, float, date and select input
				if (attributeValue.indexOf("#@#") != -1) {
					$("#input" + attributeId).val(attributeValue.split("#@#"));
				} else {
					$("#input" + attributeId).val(attributeValue);
				}
				break;
			}
		}
	}
}

function isArray(obj) {  
	return Object.prototype.toString.call(obj) === '[object Array]';   
}

function Trim(str)
{
	if(str=="") return str;
	var newStr = ""+str;
	RegularExp = /^\s+|\s+$/gi;
	return newStr.replace( RegularExp,"" );
}
	
function editIntValue(obj, attributeId)
{
   	var value = Trim(obj.value);
   	obj.value = value;
   	if (value != "") {
   		$.get("/globalsight/ControlServlet?pageName=SJA&linkName=ajax", 
		{"action":"editInt","value":value,"attributeId":attributeId,"no":Math.random()}, 
		function(data){
			if (data != "") {
				alert(data);
				obj.focus();
			}
		});
   	}
}

function editFloatValue(obj, attributeId)
{
	var value = Trim(obj.value);
	obj.value = value;
   	if (value != "") {
   		$.get("/globalsight/ControlServlet?pageName=SJA&linkName=ajax", 
		{"action":"editFloat","value":value,"attributeId":attributeId,"no":Math.random()}, 
		function(data){
			if (data != "") {
				alert(data);
				obj.focus();
			}
		});
   	}
}

function editDateValue(obj, attributeId)
{
	var value = Trim(obj.value);
	obj.value = value;
   	if (value != "") {
   		$.get("/globalsight/ControlServlet?pageName=SJA&linkName=ajax", 
		{"action":"editDate","value":value,"attributeId":attributeId,"no":Math.random()}, 
		function(data){
			if (data != "") {
				alert(data);
				obj.focus();
			}
		});
   	}
}

function editTextValue(obj, attributeId)
{
	var value = Trim(obj.value);
	obj.value = value;
   	if (value != "") {
		$.get("/globalsight/ControlServlet?pageName=SJA&linkName=ajax", 
		{"action":"editText","value":value,"attributeId":attributeId,"no":Math.random()}, 
		function(data){
			if (data != "") {
				alert(data);
				obj.focus();
			}
		});
   	}
}

function uploadFileMethod() 
{
	$("#uploadForm").submit();
	setTimeout("getAllFiles()", 1000);
}

function getAllFiles()
{
	   var attributeName = document.getElementById("attributeName").value;
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "/globalsight/ControlServlet?linkName=self&pageName=SET_ATTRIBUTE&action=getFiles",
		   data: "attributeName="+attributeName,
		   success: function(data){
			  var id = document.getElementById("attributeName").value;
		      var returnData = eval(data);
	   		  if (returnData.error)
	          {
	      	    alert(returnData.error);
	          }
	          else
	          {
	        	  document.getElementById("file" + id).innerHTML = returnData.label;
            	  updateFiles(returnData.files);
	          }
		   },
	   	   error:function(error)
	       {
               alert(error.message);
           }
		});
}

function showUploadfileDialog(attributeName)
{
   $.ajax({
	   type: "POST",
	   dataType : "text",
	   url: "/globalsight/ControlServlet?linkName=self&pageName=SET_ATTRIBUTE&action=getFiles",
	   data: "attributeName="+attributeName,
	   success: function(data){
	      var returnData = eval(data);
   		  if (returnData.error)
          {
      	    alert(returnData.error);
          }
          else
          {
        	  initFileDialog(attributeName, returnData);
        	  $("#uploadFormDiv").dialog({width: 600, height: 400, resizable:false});
          }
	   },
   	   error:function(error)
       {
         alert(error.message);
       }
	});
}

function updateFiles(files) {
	var selectBox = document.getElementById("allFiles");
	var options = selectBox.options;
	for ( var i = options.length - 1; i >= 0; i--) {
		selectBox.remove(i);
	}

	for ( var i = 0; i < files.length; i++) {
		addFile(files[i]);
	}
	setOptionColor();
}

function initFileDialog(attributeName, data)
{
   document.getElementById("attributeName").value = attributeName;
   updateFiles(data.files);
}

function addFile(file) {
	var option = document.createElement("option");
	option.appendChild(document.createTextNode(file));
	option.setAttribute("value", file);
	document.getElementById("allFiles").appendChild(option);
}

function deleteSelectFiles() {
	var selectFiles = new Array();
	var selectBox = document.getElementById("allFiles");
	var options = selectBox.options;

	for ( var i = options.length - 1; i >= 0; i--) {
		if (options[i].selected) {
			selectFiles.push(options[i].value);
		}
	}

	if (selectFiles.length < 1) {
		return;
	}

	var attributeName = document.getElementById("attributeName").value;

   $.ajax({
	   type: "POST",
	   dataType : "text",
	   url: "/globalsight/ControlServlet?linkName=self&pageName=SET_ATTRIBUTE&action=deleteFiles",
	   data: "attributeName="+attributeName+"&deleteFiles="+selectFiles,
	   success: function(data){
	      var returnData = eval(data);
   		  if (returnData.error)
          {
      	    alert(returnData.error);
          }
          else
          {
        	  updateFiles(returnData.files);
        	  document.getElementById("file" + attributeName).innerHTML = returnData.label;
        	  document.getElementById("input" + attributeName).value = returnData.label;
          }
	   },
   	   error:function(error)
       {
         alert(error.message);
       }
	});
}

function setOptionColor() {
	var options = document.getElementById("allFiles").options;
	var flag = true;
	for ( var i = 0; i < options.length; i++) {
		if (flag) {
			options[i].className = "row1";
			flag = false;
		} else {
			options[i].className = "row2";
			flag = true;
		}
	}
}

function showCalendar(attributeId) {
	var cal1 = new calendar2(document.getElementById("input" + attributeId));
	cal1.year_scroll = true;
	cal1.time_comp = true;
	cal1.popup();
}

</SCRIPT>
</head>
<body LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" class="tundra" onload="onload()" style="padding:10px">
<table CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText"><tr><td width="100%"><c:out value="${helper_text_set_attribute}"/></td></tr></table>
<br>
<form name="createJobForm" method="post" action="/globalsight/ControlServlet?pageName=CJ&linkName=createJob">
<input type="hidden" id="attributeString" value="">
<table cellspacing="0" cellpadding="6" border="0" class="listborder" width="100%">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
    	<td width="20%" style="border:0;padding-left:20px" height="30px" align="left" valign="middle"><span class="titletext"><c:out value="${lb_name}"/></span></td>
        <td width="20%" style="border:0;padding-left:20px" align="left" valign="middle"><span class="titletext"><c:out value="${lb_type}"/></span></td>
        <td width="20%" style="border:0;padding-left:20px" align="left" valign="middle"><span class="titletext"><c:out value="${lb_required}"/></span></td>
        <td width="40%" style="border:0;padding-left:20px" align="left" valign="middle"><span class="titletext"><c:out value="${lb_values}"/></span></td>
    </tr>
    <c:forEach items="${jobAttributesList}" var="ja" varStatus="no">
    	<c:if test="${no.index % 2 == 0}">
    		<c:set var="td_css" value="tableRowOdd" scope="request" />
    	</c:if>
    	<c:if test="${no.index % 2 == 1}">
    		<c:set var="td_css" value="tableRowEven" scope="request" />
    	</c:if>
    	
    	<tr class="${td_css}">
    		<td class="standardText" height="35px" align="left" valign="middle" style="padding-left:20px"><c:out value="${ja.attribute.displayName}"/></td>  	<!-- Name -->
    		<!--------------------------------------------------------------------------------------------------------------------->
    		<c:if test="${ja.attribute.type eq 'text'}">
	    		<c:set var="ja_type" value="${lb_attribute_type_text}" scope="request" />
	    	</c:if>
	    	<c:if test="${ja.attribute.type eq 'float'}">
	    		<c:set var="ja_type" value="${lb_attribute_type_float}" scope="request" />
	    	</c:if>
	    	<c:if test="${ja.attribute.type eq 'date'}">
	    		<c:set var="ja_type" value="${lb_attribute_type_date}" scope="request" />
	    	</c:if>
	    	<c:if test="${ja.attribute.type eq 'integer'}">
	    		<c:set var="ja_type" value="${lb_attribute_type_integer}" scope="request" />
	    	</c:if>
	    	<c:if test="${ja.attribute.type eq 'choiceList'}">
	    		<c:set var="ja_type" value="${lb_attribute_type_choiceList}" scope="request" />
	    	</c:if>
	    	<c:if test="${ja.attribute.type eq 'file'}">
	    		<c:set var="ja_type" value="${lb_attribute_type_file}" scope="request" />
	    	</c:if>
    		<td class="standardText" align="left" valign="middle" style="padding-left:20px"><c:out value="${ja_type}"/></td>						<!-- Type -->
    		<!--------------------------------------------------------------------------------------------------------------------->
    		<c:if test="${ja.attribute.required == true}">
	    		<c:set var="ja_required" value="${lb_yes}" scope="request" />
	    	</c:if>
	    	<c:if test="${ja.attribute.required == false}">
	    		<c:set var="ja_required" value="${lb_no}" scope="request" />
	    	</c:if>
    		<td class="standardText" align="left" valign="middle" style="padding-left:20px"><c:out value="${ja_required}"/></td>					<!-- required -->
    		<!--------------------------------------------------------------------------------------------------------------------->
    		<c:choose>
	    		<c:when test="${ja.attribute.type eq 'text'}">
		    		<td class="standardText" align="left" valign="middle">
		    			<input type="hidden" name="attributeConditionType" value="1">
		    			<input type="hidden" name="attributeId" value="${ja.attribute.id}">
		    			<input type="hidden" id="input${ja.attribute.id}_req" value="${ja.attribute.required}">
						<input type="text" id="input${ja.attribute.id}" name="inputValue" style="width:200px" onblur="javascript:editTextValue(this, '${ja.attribute.id}')">
		    		</td>
		    	</c:when>
		    	<c:when test="${ja.attribute.type eq 'float'}">
		    		<td class="standardText" align="left" valign="middle">
		    			<input type="hidden" name="attributeConditionType" value="2">
		    			<input type="hidden" name="attributeId" value="${ja.attribute.id}">
		    			<input type="hidden" id="input${ja.attribute.id}_req" value="${ja.attribute.required}">
	   					<input type="text" id="input${ja.attribute.id}" name="inputValue" style="width:200px" onblur="javascript:editFloatValue(this, '${ja.attribute.id}')">
		    		</td>
		    	</c:when>
		    	<c:when test="${ja.attribute.type eq 'date'}">
		    		<td class="standardText" align="left" valign="middle">
		    			<input type="hidden" name="attributeConditionType" value="3">
		    			<input type="hidden" name="attributeId" value="${ja.attribute.id}">
		    			<input type="hidden" id="input${ja.attribute.id}_req" value="${ja.attribute.required}">
	   					<input type="text" id="input${ja.attribute.id}" name="inputValue" style="width:180px" onblur="javascript:editDateValue(this, '${ja.attribute.id}')">
	   					<IMG style='cursor:pointer' border=0 src="/globalsight/includes/Calendar.gif" onclick="showCalendar('${ja.attribute.id}')">
		    		</td>
		    	</c:when>
		    	<c:when test="${ja.attribute.type eq 'integer'}">
		    		<td class="standardText" align="left" valign="middle">
		    			<input type="hidden" name="attributeConditionType" value="4">
		    			<input type="hidden" name="attributeId" value="${ja.attribute.id}">
		    			<input type="hidden" id="input${ja.attribute.id}_req" value="${ja.attribute.required}">
						<input type="text" id="input${ja.attribute.id}" name="inputValue" style="width:200px" onblur="javascript:editIntValue(this, '${ja.attribute.id}')">
		    		</td>
		    	</c:when>
		    	<c:when test="${ja.attribute.type eq 'choiceList'}">
		    		<td class="standardText" align="left" valign="middle">
		    			<input type="hidden" name="attributeConditionType" value="5">
		    			<input type="hidden" name="attributeId" value="${ja.attribute.id}">
		    			<input type="hidden" id="input${ja.attribute.id}_req" value="${ja.attribute.required}">
				        <select id="input${ja.attribute.id}" name="inputValue" style="min-width:200px;width:auto;" <c:out value="${ja.multi}"/>>
				        	<c:if test="${ja.multi eq ''}">
				        		<option value=""></option>
				        	</c:if>
							<c:forEach items="${ja.allOptions}" var="jaos">
								<c:forEach items="${ja.jobAttribute.optionValues}" var="opvs">
									<c:if test="${opvs eq jaos.value}">
										<c:set var="jao_selected" value="selected" scope="request" />
									</c:if>
								</c:forEach>
								<option value="<c:out value='${jaos.value}'/>" title="<c:out value='${jaos.value}'/>" <c:out value="${jao_selected}"/>><c:out value='${jaos.value}'/></option>
							</c:forEach>
	    				</select>
		    		</td>
		    	</c:when>
		    	<c:when test="${ja.attribute.type eq 'file'}">
		    		<td class="standardText" align="left" valign="middle">
		    			<input type="hidden" name="attributeConditionType" value="6">
		    			<input type="hidden" name="attributeId" value="${ja.attribute.id}">
						<input type="hidden" id="jobAtt${ja.attribute.id}" name="attributeName" value="${ja.attribute.name}">
						<input type="hidden" id="input${ja.attribute.id}_req" value="${ja.attribute.required}">
						<input type="hidden" id="input${ja.attribute.name}" name="inputValue" value="">
                       	<div class="fileDiv"  onclick="showUploadfileDialog('${ja.attribute.name}')" style="margin:3px; text-align:left;">
                            <div id="file${ja.attribute.name}">${ja.fileLabel}</div>
							<div style="width:35px;">&nbsp;</div>
                        </div>	    		
		    		</td>
		    	</c:when>
    		</c:choose>
    	</tr>
    </c:forEach>
</table>
<br>
<table CELLSPACING="0" CELLPADDING="0" BORDER="0" CLASS="standardText" STYLE="width:100%">
    <TR VALIGN="TOP">
		<td colspan="4" height="25px" align="center">
			<input id="saveWin" type="button" class="standardBtn_mouseout" style="width:100px" value="<c:out value='${lb_save}'/>" title="<c:out value='${lb_save}'/>">&nbsp;&nbsp;&nbsp;
			<input id="closeWin" type="button" class="standardBtn_mouseout" style="width:100px" value="<c:out value='${lb_close}'/>" title="<c:out value='${lb_close}'/>">
		</td>
	</TR>
</table>
</form>

<div id="uploadFormDiv" title="<c:out value='${lb_update_job_attributes}'/>"  execute="" style="display:none;">
  <form name="uploadForm" method="post" action="/globalsight/ControlServlet?linkName=self&pageName=SET_ATTRIBUTE&action=editFile" enctype="multipart/form-data" id="uploadForm"  target="ajaxUpload">
	  <input type="hidden" id="attributeName" name="attributeName" value="-1">
	  <table style="width:97%;" class="standardText">
	    <tr>
	      <td colspan="2">
	          <c:out value="${lb_all_files}"/>:
	          <select name="allFiles" multiple="multiple" id="allFiles" size="15" style="width: 100%;">
			  </select>
			  <div align="right">
			  <input type="button" name="deleteFiles" id="deleteFiles" onclick="deleteSelectFiles()" value="<c:out value="${lb_delete}"/>">
			  </div>
		  </td>
	    </tr>
	    <tr>
	      <td colspan="2">&nbsp;</td>     
	    </tr>
	    <tr>
	    <td align="left" valign="middle">
	    		<c:out value="${lb_file}"/>:
	          <input type="file" name="uploadFile" size="60" id="fileUploadDialog" style="height:24px;width:300px">
	    	</td>
	      <td align="right" valign="middle">
	          <input type="button" onclick="uploadFileMethod()" value="<c:out value="${lb_upload}"/>">
	          <input type="button" onclick="$('#uploadFormDiv').dialog('close')" value="<c:out value="${lb_close}"/>">
	      </td>
	    </tr>
	  </table>
  </form>
</div>
<!-- for uploading file asynchronous -->
<iframe id="ajaxUpload" name="ajaxUpload" style="display:none"></iframe>

</body>
</html>