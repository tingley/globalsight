var globalExclusionFilter = new GlobalExclusionFilter();
var imgYes = "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>";
var checkedId = "";
function GlobalExclusionFilter()
{
	this.filterTableName = "global_exclusion_filter";
	this.dialogId = "globalExclusionFilterDialog";
	this.currentPage1 = 0;
	this.currentPage2 = 0;
	this.tagsEveryPage = 10;
	this.editItemId = -1;
	this.editItemEnable = false;
	this.sidRules = new Array();
}

GlobalExclusionFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

GlobalExclusionFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

GlobalExclusionFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	globalExclusionFilter.currentPage1 = 0;
	globalExclusionFilter.currentPage2 = 0;
	var dialogObj = $("#globalExclusionFilterPopupContent");
	dialogObj.find(".name").val(this.filter.filterName);
	dialogObj.find("#globalExclusionFilterDesc").val(this.filter.filterDescription);
	dialogObj.find(".globalExclusionFilterType").val(this.filter.filterType);
	if (this.filter.filterType == 1){
		globalExclusionFilter.sidRules = eval(this.filter.jsonText);
	} else if (this.filter.filterType == 2){
		globalExclusionFilter.txtRules = eval(this.filter.jsonText);
	}
	
	globalExclusionFilter.generateSidDiv();
	globalExclusionFilter.changeType($(".globalExclusionFilterType"));
	this.showDialog();
	
	saveGlobalExclusionFilter.edit = true;
	saveGlobalExclusionFilter.filterId = filterId;
	saveGlobalExclusionFilter.color = color;
	saveGlobalExclusionFilter.filter = this.filter;
	saveGlobalExclusionFilter.specialFilters = specialFilters;
	saveGlobalExclusionFilter.topFilterId = topFilterId;
}

GlobalExclusionFilter.prototype.generateSidDiv = function()
{
	$(".xmlruletr").remove();
	globalExclusionFilter.changeType($(".globalExclusionFilterType"));

	if (globalExclusionFilter.sidRules.length > 0){
		var pre = $(".globalExclusionFilter_xml_emptyTable_tr");
		
		for (var i = globalExclusionFilter.currentPage1 * globalExclusionFilter.tagsEveryPage; i < globalExclusionFilter.sidRules.length && i < (globalExclusionFilter.currentPage1+1) * globalExclusionFilter.tagsEveryPage; i++){
			i
			var item = globalExclusionFilter.sidRules[i];
			var str = new StringBuffer("<tr style=\"background-color: rgb(223, 227, 238);\" class=\"xmlruletr\">");
			str.append("<td><input class=\"globalExclusionSidItemCheckbox\" id=\"" + item.itemid + "\" onclick=\"globalExclusionFilter.checkSidItem(this)\" type=\"checkbox\" ");
			if (item.enable){
				str.append("checked");
			}		
			str.append("></td>");
			var encodedName = item.sid.replace(/\</g,"&lt;").replace(/\>/g, "&gt;");
			
			str.append("<td class=\"tagValue_td\"><a onclick=\"globalExclusionFilter.openSidEdit('" + item.itemid +"')\" href=\"#\">" + encodedName + "</a></td>");
			str.append("<td class='tagValue_td'>" + (item.sidIsRegEx?imgYes:"") + "</td>");
			str.append("</tr>");
			pre.before(str.toString());
		}
	}
	
	this.setPageValue(1);
}

GlobalExclusionFilter.prototype.generateDiv = function(topFilterId, color)
{
	globalExclusionFilter = new GlobalExclusionFilter();
	globalExclusionFilter.currentPage1 = 0;
	globalExclusionFilter.currentPage2 = 0;
	var dialogObj = $("#globalExclusionFilterPopupContent");
	dialogObj.find(".name").attr("maxlength",maxFilterNameLength).val("");
	dialogObj.find("#globalExclusionFilterDesc").val("");
	dialogObj.find(".globalExclusionFilterType").val("-1");
	globalExclusionFilter.generateSidDiv();
	this.showDialog();
	saveGlobalExclusionFilter.edit = false;
	saveGlobalExclusionFilter.topFilterId = topFilterId;
	saveGlobalExclusionFilter.color = color;
}

GlobalExclusionFilter.prototype.changeType = function(div)
{
	var v = $(div).val();
	$(".globalExclusionTypeDivs").hide();
	$("#div_globalExclusion_filter_type_" + v).show();
}

GlobalExclusionFilter.prototype.openPlainTextRule = function()
{
	globalExclusionFilter.editItemId = globalExclusionFilter.newItemId();
	globalExclusionFilter.enable = false;
	$("#globalExclusionFilter_customTextRule_startStr").val("");
	$("#globalExclusionFilter_customTextRule_startIs").attr("checked","checked");
	$("input[name='globalExclusionFilter_customTextRule_startOcc'][value=1]").attr("checked",true); 
	$("#globalExclusionFilter_customTextRule_finishStr").val("");
	$("#globalExclusionFilter_customTextRule_finishIs").attr("checked","checked");
	$("input[name='globalExclusionFilter_customTextRule_finishOcc'][value=1]").attr("checked",true); 
	$("#globalExclusionFilter_customTextRule_isMultiline").attr("checked","checked");
	$("#globalExclusionFilter_customTextRule_priority").val("");
	
	showPopupDialog("addSidTypeRulePlain");
}

function initCheckBox(id, isCheck)
{
	if (isCheck)
	{
		$("#"+id).attr("checked","checked");
	}
	else
	{
		$("#"+id).removeAttr("checked");
	}
}

GlobalExclusionFilter.prototype.openTxtRuleEdit = function(id)
{
	globalExclusionFilter.editItemId = id;
	var editItem = globalExclusionFilter.getTxtRuleById(id);
	
	$("#globalExclusionFilter_customTextRule_startStr").val(editItem.startString);
	initCheckBox("globalExclusionFilter_customTextRule_startIs", editItem.startIsRegEx);

	if ("FIRST" == editItem.startOccurrence){
		$("input[name='globalExclusionFilter_customTextRule_startOcc'][value=1]").attr("checked",true); 
	} else if ("LAST" == editItem.startOccurrence){
		$("input[name='globalExclusionFilter_customTextRule_startOcc'][value=2]").attr("checked",true); 
	} else {
		$("input[name='globalExclusionFilter_customTextRule_startOcc'][value=3]").attr("checked",true); 
		$("#globalExclusionFilter_customTextRule_startOccTimes").val(editItem.startOccurrence);
	}
	
	
	$("#globalExclusionFilter_customTextRule_finishStr").val(editItem.finishString);
	initCheckBox("globalExclusionFilter_customTextRule_finishIs", editItem.finishIsRegEx);
	
	if ("FIRST" == editItem.finishOccurrence){
		$("input[name='globalExclusionFilter_customTextRule_finishOcc'][value=1]").attr("checked",true); 
	} else if ("LAST" == editItem.finishOccurrence){
		$("input[name='globalExclusionFilter_customTextRule_finishOcc'][value=2]").attr("checked",true); 
	} else {
		$("input[name='globalExclusionFilter_customTextRule_finishOcc'][value=3]").attr("checked",true); 
		$("#globalExclusionFilter_customTextRule_finishOccTimes").val(editItem.startOccurrence);
	}

	initCheckBox("globalExclusionFilter_customTextRule_isMultiline", editItem.isMultiline);
	$("#globalExclusionFilter_customTextRule_priority").val(editItem.priority);
	showPopupDialog("addSidTypeRulePlain");
}

GlobalExclusionFilter.prototype.openSidRule = function()
{
	globalExclusionFilter.editItemId = globalExclusionFilter.newItemId();
	globalExclusionFilter.enable = false;
	globalExclusionFilter.editItemEnable = false;
	
	$("#globalExclusionFilterSid").val("");
	$("#globalExclusionFilterIsRegex").removeAttr("checked");
	
	showPopupDialog("addGlobalExclusionTypeRuleSid");
}

GlobalExclusionFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	return newid;
}

GlobalExclusionFilter.prototype.openSidEdit = function(id)
{
	globalExclusionFilter.editItemId = id;
	var editItem = globalExclusionFilter.getSidRuleById(id);
	globalExclusionFilter.editItemEnable = editItem.enable;
	$("#globalExclusionFilterSid").val(editItem.sid);
	if (editItem.sidIsRegEx){
		$("#globalExclusionFilterIsRegex").attr("checked",'true');
	} else {
		$("#globalExclusionFilterIsRegex").removeAttr("checked");
	}
	showPopupDialog("addGlobalExclusionTypeRuleSid");
}

GlobalExclusionFilter.prototype.getSidRuleById = function(id)
{
	for(var i = 0; i < globalExclusionFilter.sidRules.length; i++)
	{
		var oriItem = globalExclusionFilter.sidRules[i];
		if (oriItem.itemid == id)
		{
			return oriItem;
		}
	}
	
	return null;
}

GlobalExclusionFilter.prototype.getTxtRuleById = function(id)
{
	for(var i = 0; i < globalExclusionFilter.txtRules.length; i++)
	{
		var oriItem = globalExclusionFilter.txtRules[i];
		if (oriItem.itemid == id)
		{
			return oriItem;
		}
	}
	
	return null;
}


GlobalExclusionFilter.prototype.addSidRule = function()
{
	var itemId = globalExclusionFilter.editItemId;
	var validate = new Validate();
	var sid = new StringBuffer(document.getElementById("globalExclusionFilterSid").value);
	var sidIsRegEx = document.getElementById("globalExclusionFilterIsRegex").checked;
	

	if(validate.isEmptyStr(sid))
	{
		document.getElementById("globalExclusionFilterSid").value = "";
		alert(jsGlobalExclusionSid);
		return;
	}
	
	if(this.isCustomSidRuleExists(itemId, sid, sidIsRegEx))
	{
		alert(jsCustomTextRuleExist);
		return;
	}
	
	var enable = globalExclusionFilter.editItemEnable;
	var item = {itemid : itemId, enable : enable, sid : sid.toString(), sidIsRegEx : sidIsRegEx};
	
	globalExclusionFilter.addOneItemToSidRule(item);
	globalExclusionFilter.closeTagDialog("addGlobalExclusionTypeRuleSid");
	globalExclusionFilter.generateSidDiv();
}

GlobalExclusionFilter.prototype.validatePriority = function(newpriority, eid)
{
	var validate = new Validate();
		
	if (!validate.isPositiveInteger(newpriority)
			|| newpriority < 1 || newpriority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	var ruleObjects = plaintextFilter.optionObjsMap[plaintextFilter.currentOption];
	for(var j = 0; j < ruleObjects.length; j++)
	{
		var ruleObject = ruleObjects[j];
		if (newpriority == ruleObject.priority && eid != ruleObject.itemid)
		{
			alert(jsAlertSamePriority);
			return false;
		}
	}
		
	return true;
}

GlobalExclusionFilter.prototype.addOneItemToTxtRule = function(item)
{
	var added = false;
	for(var i = 0; i < globalExclusionFilter.txtRules.length; i++)
	{
		var oriItem = globalExclusionFilter.txtRules[i];
		if (oriItem.itemid == item.itemid)
		{
			globalExclusionFilter.txtRules[i] = item;
			added = true;
			break;
		}
	}
	
	if (!added)
	{
		globalExclusionFilter.txtRules[i] = item;
	}
}

GlobalExclusionFilter.prototype.addXmlXpathRule = function()
{
	var itemId = globalExclusionFilter.editItemId;
	var validate = new Validate();
	var xpath = new StringBuffer(document.getElementById("globalExclusionFilterXmlXpath").value);
	var priority = document.getElementById("globalExclusionFilterXmlPriority").value;
	
	if(validate.isEmptyStr(xpath.trim()))
	{
		document.getElementById("globalExclusionFilterXmlXpath").value = "";
		alert(jsXpathEmpty);
		return;
	}
	
	if (!this.validateXmlXpathPriority(priority, itemId))
	{
		return;
	}
	
	var item = {itemid : itemId, enable : globalExclusionFilter.enable, xpath : xpath.trim(), priority : priority};
	
	globalExclusionFilter.addOneItemToXpathRule(item);
	globalExclusionFilter.closeTagDialog("addSidTypeRuleXml");
	globalExclusionFilter.generateSidDiv();
}

GlobalExclusionFilter.prototype.addOneItemToSidRule = function(item)
{
	var added = false;
	for(var i = 0; i < globalExclusionFilter.sidRules.length; i++)
	{
		var oriItem = globalExclusionFilter.sidRules[i];
		if (oriItem.itemid == item.itemid)
		{
			globalExclusionFilter.sidRules[i] = item;
			added = true;
			break;
		}
	}
	
	if (!added)
	{
		globalExclusionFilter.sidRules[i] = item;
	}
}

GlobalExclusionFilter.prototype.validateTxtPriority = function(newpriority, eid)
{
	var validate = new Validate();
		
	if (!validate.isPositiveInteger(newpriority)
			|| newpriority < 1 || newpriority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	for(var j = 0; j < globalExclusionFilter.txtRules.length; j++)
	{
		var ruleObject = globalExclusionFilter.txtRules[j];
		if (newpriority == ruleObject.priority && eid != ruleObject.itemid)
		{
			alert(jsAlertSamePriority);
			return false;
		}
	}
		
	return true;
}

GlobalExclusionFilter.prototype.isCustomSidRuleExists = function(id, sid, sidIsRegEx)
{
	for (var i = 0; i < globalExclusionFilter.sidRules.length; i++)
	{
		var item = globalExclusionFilter.sidRules[i];
		if (item.itemid != id && item.sid == sid && item.sidIsRegEx == sidIsRegEx)
		{
			return true;
		}
	}
	
	return false;
}

GlobalExclusionFilter.prototype.validateXmlXpathPriority = function(newpriority, eid)
{
	var validate = new Validate();
		
	if (!validate.isPositiveInteger(newpriority)
			|| newpriority < 1 || newpriority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	for(var j = 0; j < globalExclusionFilter.sidRules.length; j++)
	{
		var ruleObject = globalExclusionFilter.sidRules[j];
		if (newpriority == ruleObject.priority && eid != ruleObject.itemid)
		{
			alert(jsAlertSamePriority);
			return false;
		}
	}
		
	return true;
}

GlobalExclusionFilter.prototype.checkSidItem = function(cobj)
{
	var id = cobj.id;
	var item0;
	for(var j = 0; j < globalExclusionFilter.sidRules.length; j++)
	{
		var item = globalExclusionFilter.sidRules[j];
		if (item.itemid == id)
		{
			item0 = item;
			break;
		}
	}
	
	item0.enable = cobj.checked;
}

GlobalExclusionFilter.prototype.checkTxtItem = function(cobj)
{
	var id = cobj.id;
	var item0;
	for(var j = 0; j < globalExclusionFilter.txtRules.length; j++)
	{
		var item = globalExclusionFilter.txtRules[j];
		if (item.itemid == id)
		{
			item0 = item;
			break;
		}
	}
	item0.enable = cobj.checked;
}

GlobalExclusionFilter.prototype.checkAllGlobalExclusionFilterSid = function()
{
	if ($("#checkAllGlobalExclusionFilterSid")[0].checked){
		$(".globalExclusionSidItemCheckbox").each(function(){   
			if (!$(this).is(':checked')) {
				this.checked = true;
				globalExclusionFilter.checkSidItem(this);
			}
		});
	} else {
		$(".globalExclusionSidItemCheckbox").each(function(){   
			if ($(this).is(':checked')) {
				this.checked = false;
				globalExclusionFilter.checkSidItem(this);
			}
		});
	}
}

function saveGlobalExclusionFilter()
{
	var validate = new Validate();
	var filterName = document.getElementById("globalExclusionFilterName").value;
	if(validate.isEmptyStr(filterName))
	{
		alert(emptyFilterName);
		return;
	}
	
	if(validate.containSpecialChar(filterName))
    {
        alert(invalidFilterNameChar + invalidChars);
        return;
    }
    
    filterName = validate.trim(filterName);
    if(validate.isExceedMaxCount(filterName, maxFilterNameLength))
    {
        alert(exceedFilterName + maxFilterNameLength);
        return;
    }
    
    var filterType = $(".globalExclusionFilterType").val();
    if (filterType == -1)
    {
    	alert("Please select the Global Exclusion Based On.");
    	return;
    }
    
	var filterDesc = document.getElementById("globalExclusionFilterDesc").value;
	
	var rule = null;
	if (filterType == 1){
		rule = JSON.stringify(globalExclusionFilter.sidRules);
	} else if (filterType == 2){
		rule = JSON.stringify(globalExclusionFilter.txtRules);
	}
	
	var obj = {
		filterName : filterName,
		filterDesc : filterDesc,
		companyId : companyId,
		filterType : filterType,
		rule : rule
	};
	
	if (saveGlobalExclusionFilter.edit){
		obj.filterId = saveGlobalExclusionFilter.filterId;
	}
	// send for check
	sendAjax(obj, "saveOrUpdateGlobalExclusionFilter", "saveOrUpdateGlobalExclusionFilterCallback");
	
	saveGlobalExclusionFilter.obj = obj;
}

function saveOrUpdateGlobalExclusionFilterCallback(data)
{
	if (data == 'name_exists')
	{
		alert(existFilterName);
	}
	else
	{
		var color = saveGlobalExclusionFilter.color;
		var topFilterId = saveGlobalExclusionFilter.topFilterId;
		var filter = getFilterById(topFilterId);
		if(filter)
		{
			var xrFilter = new GlobalExclusionFilter();
			xrFilter.id = data - 0;
			xrFilter.filterTableName = "global_exclusion_filter";
			xrFilter.filterName = saveGlobalExclusionFilter.obj.filterName;
			xrFilter.filterDescription = saveGlobalExclusionFilter.obj.filterDesc;
			xrFilter.filterType = saveGlobalExclusionFilter.obj.filterType;
			xrFilter.companyId = companyId;
			xrFilter.jsonText = saveGlobalExclusionFilter.obj.rule;
			
			if (!saveGlobalExclusionFilter.filterId){
				filter.specialFilters.push(xrFilter);
				reGenerateFilterList(topFilterId, filter.specialFilters, color);
			} else {
				var specialFilters = updateSpecialFilter(filter.specialFilters, xrFilter);
				reGenerateFilterList(topFilterId, specialFilters, color);
			}
		}
		closePopupDialog("globalExclusionFilterDialog");
	}
}

GlobalExclusionFilter.prototype.closeAllTagPopup = function()
{
	closePopupDialog("addSidTypeRuleXml");
	closePopupDialog("addSidTypeRulePlain");
	closePopupDialog("globalExclusionFilterDialog");	
}

GlobalExclusionFilter.prototype.prePage = function(type)
{
	if (type == 1){
		if (this.currentPage1 > 0){
			this.currentPage1 --;
			this.generateSidDiv();
		} else {
			alert(canNotPrePage);
			return;
		}
	} else if (type == 2){
		if (this.currentPage2 > 0){
			this.currentPage2 --;
			this.generateTxtDiv();
		} else {
			alert(canNotPrePage);
			return;
		}
	}
}

GlobalExclusionFilter.prototype.nextPage = function(type)
{
	if (type == 1){
		var arraysize = globalExclusionFilter.sidRules.length;
		if(this.currentPage1 < this.getPageSize(arraysize) - 1)
		{
			this.currentPage1 ++;
			this.generateSidDiv();
		}else{
			alert(canNotNextPage);
			return;
		}
	} else if (type == 2){
		var arraysize = globalExclusionFilter.txtRules.length;
		if(this.currentPage2 < this.getPageSize(arraysize) - 1)
		{
			this.currentPage2 ++;
			this.generateTxtDiv();
		}else{
			alert(canNotNextPage);
			return;
		}
	}
}

GlobalExclusionFilter.prototype.goToPage = function(type)
{
	if (type == 1){
		var pageValue = document.getElementById("pageCountGlobalExclusionFilterSid").value;
		var validate = new Validate();
		if(!validate.isNumber(pageValue))
		{
			alert(pageValueIsNotNumber);
			return;
		}
		
		pageValue = pageValue - 0;
		var arraysize = this.sidRules.length;
		
		if(pageValue < 1 || pageValue > this.getPageSize(arraysize))
		{
			alert(invalidatePageValue.replace("%s", this.getPageSize(arraysize)));
			this.setPageValue(type);
			return;
		}
		
		this.currentPage1 = pageValue - 1;
		this.generateSidDiv();
	} else if (type == 2){
		var pageValue = document.getElementById("pageCountSidTxtFilter").value;
		var validate = new Validate();
		if(!validate.isNumber(pageValue))
		{
			alert(pageValueIsNotNumber);
			return;
		}
		
		pageValue = pageValue - 0;
		var arraysize = this.txtRules.length;
		
		if(pageValue < 1 || pageValue > this.getPageSize(arraysize))
		{
			alert(invalidatePageValue.replace("%s", this.getPageSize(arraysize)));
			this.setPageValue(type);
			return;
		}
		
		this.currentPage2 = pageValue - 1;
		this.generateTxtDiv();
	}
}

GlobalExclusionFilter.prototype.setPageValue = function(type)
{
	if (type == 1){
		$("#pageCountGlobalExclusionFilterSid").val(this.currentPage1 + 1);
		$("#pageTotalSizeGlobalExclusionFilterSid").html(this.getPageSize(globalExclusionFilter.sidRules.length));
	} 
}

GlobalExclusionFilter.prototype.getCurrentPage = function(type)
{
	if (type == 1)
		return currentPage1;
	
	if (type == 2)
		return currentPage2;
}

GlobalExclusionFilter.prototype.getPageSize = function (totle)
{
	var countPerPage = globalExclusionFilter.tagsEveryPage;
	var pagesize = Math.floor(totle/countPerPage +  (totle%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

GlobalExclusionFilter.prototype.deleteXmlRule = function ()
{
	var hasTagsToDelete = globalExclusionFilter.generateDeleteXmlTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteGlobalExclusionSidTagDialog");
	}
}

GlobalExclusionFilter.prototype.generateDeleteXmlTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='500px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_globalExclusionFilterDelete' onclick='globalExclusionFilter.selectAll_globalExclusionFilterDelete()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsTagsToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td >");
	str.append("<Label class='tagName_td'>" + jsTagsCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
		
	var backColor = isOdd ? "#DFE3EE" : "#C7CEE0";
	isOdd = !isOdd;
	
	str.append("<tr style='background-color:"+backColor+";padding:4px'>");
	str.append("<td>");
	
	for(var j = 0; j < globalExclusionFilter.sidRules.length; j++)
	{
		var ruleObject = globalExclusionFilter.sidRules[j];
		if(ruleObject.enable)
		{
			str.append("<input type='checkbox' name='globalExclusionFilterDeleteXml' tagValue='"+ruleObject.itemid+"' checked>");
			str.append(ruleObject.sid);
			str.append("</input>");
			count ++;
		}
	}
	sum += count;
	str.append("</td>");
	str.append("<td>");
	str.append(count);
	str.append("</td>");
	str.append("</tr>");	
	str.append("</table></center>");

	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteGlobalExclusionSidTagDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteGlobalExclusionSidTagTableContent").innerHTML = str.toString();
	return true;
}

GlobalExclusionFilter.prototype.selectAll_globalExclusionFilterDelete = function()
{
	var checked = $("#selectAll_globalExclusionFilterDelete")[0].checked;
	var checkBoxObjs = document.getElementsByName("globalExclusionFilterDeleteXml");
    for(var i = 0; i < checkBoxObjs.length; i++)
    {
    	var checkBoxObj = checkBoxObjs[i];
    	checkBoxObj.checked = checked;
    }
}

GlobalExclusionFilter.prototype.deleteCheckedSidTags = function()
{
	var aoption = baseFilter.availableOptions[i];
	var ruleObjects = baseFilter.optionObjsMap[aoption];
    var checkBoxId = "delete_tags_" + aoption;
    var checkBoxObjs = document.getElementsByName("globalExclusionFilterDeleteXml");
    for(var i = 0; i < checkBoxObjs.length; i++)
    {
    	var checkBoxObj = checkBoxObjs[i];
        if (checkBoxObj.checked)
        {
        	var itemId = checkBoxObj.getAttribute("tagValue");
        	
        	for(var j = 0; j < globalExclusionFilter.sidRules.length; j++)
        	{
        		var ruleObject = globalExclusionFilter.sidRules[j];
        		if(ruleObject.itemid == itemId)
        		{
        			globalExclusionFilter.sidRules.removeData(ruleObject);
        			break;
        		}
        	}
        }
    }
	
	this.generateSidDiv();
	closePopupDialog("deleteGlobalExclusionSidTagDialog");
}

GlobalExclusionFilter.prototype.deletePlainTextRule = function ()
{
	var hasTagsToDelete = globalExclusionFilter.generateDeleteTxtTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteSidTxtTagDialog");
	}
}

function isEmptyObject(e) {  
	var t;  
	for (t in e)  
       return !1;
	
    return !0  
}  

GlobalExclusionFilter.prototype.cloneObject = function(oriObj)
{
	var txt = JSON.stringify(oriObj);
	return JSON.parse(txt);
}

GlobalExclusionFilter.prototype.alertObject = function(obj)
{
	var txt = JSON.stringify(obj);
	return alert(txt);
}

GlobalExclusionFilter.prototype.getItemById = function(itemId, optionValue)
{
	var item;
	var items = globalExclusionFilter.optionObjsMap[optionValue];
	if (items && items.length > 0)
	{
		for(var i = 0; i < items.length; i++)
		{
			var oneitem = items[i];
			if (oneitem.itemid == itemId)
			{
				return oneitem;
			}
		}
	}
	
	return item;
}

GlobalExclusionFilter.prototype.closeTagDialog = function(dialogId)
{
	closePopupDialog(dialogId);	
	globalExclusionFilter.editItemId = -1;
}

function encodeHtmlEntities(p_text)
{
	if (typeof(p_text) == "undefined" || typeof(p_text) == "number")
	{
		return p_text;
	}
    return p_text.replace(/&/g, "&amp;").replace(/</g, "&lt;").
        replace(/>/g, "&gt;").replace(/\"/g, "&quot;");
}
