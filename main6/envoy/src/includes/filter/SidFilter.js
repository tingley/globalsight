var sidFilter = new SidFilter();
var checkAllTagsInSidFilter = false;
var sidTagsContentTable = "<table id='sidFilterTagsContentTable' border=0 width='98%' class='standardText'>"; 
var fontTagS = "<font class='specialFilter_dialog_label'>";
var fontTagE = "</font>"
var imgYes = "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>";
var checkedId = "";
var hasSidFilter_InternalText = true;	
function SidFilter()
{
	this.filterTableName = "sid_filter";
	this.dialogId = "sidFilterDialog";
	this.currentPage1 = 0;
	this.currentPage2 = 0;
	this.tagsEveryPage = 10;
	this.editItemId = -1;
	this.editItemEnable = false;
	this.xmlXpathRules = new Array();
	this.txtRules = new Array();
}

SidFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

SidFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

SidFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	$(".sidFilterType").attr("disabled",true); 
	sidFilter.currentPage1 = 0;
	sidFilter.currentPage2 = 0;
	var dialogObj = $("#sidFilterPopupContent");
	dialogObj.find(".name").val(this.filter.filterName);
	dialogObj.find("#sidFilterDesc").val(this.filter.filterDescription);
	dialogObj.find(".sidFilterType").val(this.filter.filterType);
	if (this.filter.filterType == 1){
		sidFilter.xmlXpathRules = eval(this.filter.jsonText);
	} else if (this.filter.filterType == 2){
		sidFilter.txtRules = eval(this.filter.jsonText);
	}
	
	sidFilter.generateXmlDiv();
	sidFilter.generateTxtDiv();
	sidFilter.changeType($(".sidFilterType"));
	this.showDialog();
	
	saveSidFilter.edit = true;
	saveSidFilter.filterId = filterId;
	saveSidFilter.color = color;
	saveSidFilter.filter = this.filter;
	saveSidFilter.specialFilters = specialFilters;
	saveSidFilter.topFilterId = topFilterId;
}

SidFilter.prototype.generateXmlDiv = function()
{
	$(".xmlruletr").remove();
	sidFilter.changeType($(".sidFilterType"));

	if (sidFilter.xmlXpathRules.length > 0){
		var pre = $(".sidFilter_xml_emptyTable_tr");
		
		for (var i = sidFilter.currentPage1 * sidFilter.tagsEveryPage; i < sidFilter.xmlXpathRules.length && i < (sidFilter.currentPage1+1) * sidFilter.tagsEveryPage; i++){
			i
			var item = sidFilter.xmlXpathRules[i];
			var str = new StringBuffer("<tr style=\"background-color: rgb(223, 227, 238);\" class=\"xmlruletr\">");
			str.append("<td><input class=\"xpathItemCheckbox\" id=\"" + item.itemid + "\" onclick=\"sidFilter.checkXmlXpathItem(this)\" type=\"checkbox\" ");
			if (item.enable){
				str.append("checked");
			}		
			str.append("></td>");
			var encodedName = item.xpath.replace(/\</g,"&lt;").replace(/\>/g, "&gt;");
			
			str.append("<td class=\"tagValue_td\"><a onclick=\"sidFilter.openXmlRuleEdit('" + item.itemid +"')\" href=\"#\">" + encodedName + "</a></td>");
			str.append("<td class=\"tagValue_td\">" + item.priority +　"</td>");
			str.append("</tr>");
			pre.before(str.toString());
		}
	}
	
	this.setPageValue(1);
}

SidFilter.prototype.generateTxtDiv = function()
{
	$(".sidTxttr").remove();

	if (sidFilter.txtRules.length > 0){
		var pre = $(".sidFilter_txt_emptyTable_tr");
		
		for (var i = sidFilter.currentPage2 * sidFilter.tagsEveryPage; i < sidFilter.txtRules.length && i < (sidFilter.currentPage2+1)* sidFilter.tagsEveryPage; i++){
			var item = sidFilter.txtRules[i];
			if (typeof(item.priority) == "undefined"){
				item.priority = 1;
			}
			
			var backgroundColor = "#C7CEE0";
			if(i % 2 == 0)
			{
				backgroundColor = "#DFE3EE";
			}
			var checkedStr = item.enable ? " checked " : "";
			var str = new StringBuffer("");
			str.append("<tr style='background-color:"+backgroundColor+";'  class=\"sidTxttr\">");
			str.append("<td><input onclick='sidFilter.checkTxtItem(this)' class=\"txtItemCheckbox\"  type='checkbox' id='"+item.itemid+"'" + checkedStr + "/></td>");
			var encodedName = item.startString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;");
			str.append("<td class='tagValue_td'><a href='#' onclick=\"sidFilter.openTxtRuleEdit('" + item.itemid + "')\">"+encodedName+"</a></td>");
			str.append("<td class='tagValue_td'>" + (item.startIsRegEx?imgYes:"") + "</td>");
			str.append("<td class='tagValue_td'>" + item.startOccurrence + "</td>");
			str.append("<td class='tagValue_td'>" + item.finishString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;"));
			str.append("<td class='tagValue_td'>" + (item.finishIsRegEx?imgYes:"") + "</td>");
			str.append("<td class='tagValue_td'>" + item.finishOccurrence + "</td>");
			str.append("<td class='tagValue_td'>" + item.priority + "</td>");
			str.append("</tr>");
			pre.before(str.toString());
		}
	}
	
	this.setPageValue(2);
}

SidFilter.prototype.generateDiv = function(topFilterId, color)
{
	$(".sidFilterType").attr("disabled",false); 
	sidFilter = new SidFilter();
	sidFilter.currentPage1 = 0;
	sidFilter.currentPage2 = 0;
	var dialogObj = $("#sidFilterPopupContent");
	dialogObj.find(".name").attr("maxlength",maxFilterNameLength).val("");
	dialogObj.find("#sidFilterDesc").val("");
	dialogObj.find(".sidFilterType").val("-1");
	sidFilter.generateXmlDiv();
	sidFilter.generateTxtDiv();
	this.showDialog();
	saveSidFilter.edit = false;
	saveSidFilter.topFilterId = topFilterId;
	saveSidFilter.color = color;
}

SidFilter.prototype.changeType = function(div)
{
	var v = $(div).val();
	$(".sidTypeDivs").hide();
	$("#div_sid_filter_type_" + v).show();
}

SidFilter.prototype.openPlainTextRule = function()
{
	sidFilter.editItemId = sidFilter.newItemId();
	sidFilter.editItemEnable = false;
	$("#sidFilter_customTextRule_startStr").val("");
	$("#sidFilter_customTextRule_startIs").removeAttr("checked");
	$("input[name='sidFilter_customTextRule_startOcc'][value=1]").attr("checked",true); 
	$("#sidFilter_customTextRule_finishStr").val("");
	$("#sidFilter_customTextRule_finishIs").removeAttr("checked");
	$("input[name='sidFilter_customTextRule_finishOcc'][value=1]").attr("checked",true); 
	$("#sidFilter_customTextRule_priority").val("");
	
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

SidFilter.prototype.selectAll_sidFilterXmlTag = function()
{
	var checked = $("#selectAll_sidFilterXmlTag")[0].checked;
	var checkBoxObjs = document.getElementsByName("sidFilterDeleteXml");
    for(var i = 0; i < checkBoxObjs.length; i++)
    {
    	var checkBoxObj = checkBoxObjs[i];
    	checkBoxObj.checked = checked;
    }
}

SidFilter.prototype.selectAll_sidFilterTxt = function()
{
	var checked = $("#selectAll_sidFilterTxt")[0].checked;
	var checkBoxObjs = document.getElementsByName("sidFilterDeleteTxt");
    for(var i = 0; i < checkBoxObjs.length; i++)
    {
    	var checkBoxObj = checkBoxObjs[i];
    	checkBoxObj.checked = checked;
    }
}

SidFilter.prototype.openTxtRuleEdit = function(id)
{
	sidFilter.editItemId = id;
	var editItem = sidFilter.getTxtRuleById(id);
	sidFilter.editItemEnable = editItem.enable;
	
	$("#sidFilter_customTextRule_startStr").val(editItem.startString);
	initCheckBox("sidFilter_customTextRule_startIs", editItem.startIsRegEx);

	if ("FIRST" == editItem.startOccurrence){
		$("input[name='sidFilter_customTextRule_startOcc'][value=1]").attr("checked",true); 
	} else if ("LAST" == editItem.startOccurrence){
		$("input[name='sidFilter_customTextRule_startOcc'][value=2]").attr("checked",true); 
	} else {
		$("input[name='sidFilter_customTextRule_startOcc'][value=3]").attr("checked",true); 
		$("#sidFilter_customTextRule_startOccTimes").val(editItem.startOccurrence);
	}
	
	
	$("#sidFilter_customTextRule_finishStr").val(editItem.finishString);
	initCheckBox("sidFilter_customTextRule_finishIs", editItem.finishIsRegEx);
	
	if ("FIRST" == editItem.finishOccurrence){
		$("input[name='sidFilter_customTextRule_finishOcc'][value=1]").attr("checked",true); 
	} else if ("LAST" == editItem.finishOccurrence){
		$("input[name='sidFilter_customTextRule_finishOcc'][value=2]").attr("checked",true); 
	} else {
		$("input[name='sidFilter_customTextRule_finishOcc'][value=3]").attr("checked",true); 
		$("#sidFilter_customTextRule_finishOccTimes").val(editItem.finishOccurrence);
	}

	$("#sidFilter_customTextRule_priority").val(editItem.priority);
	showPopupDialog("addSidTypeRulePlain");
}

SidFilter.prototype.openXmlRule = function()
{
	sidFilter.editItemId = sidFilter.newItemId();
	sidFilter.enable = false;
	sidFilter.editItemEnable = false;
	
	$("#sidFilterXmlXpath").val("");
	$("#sidFilterXmlPriority").val("");
	
	showPopupDialog("addSidTypeRuleXml");
}

SidFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	return newid;
}

SidFilter.prototype.openXmlRuleEdit = function(id)
{
	sidFilter.editItemId = id;
	var editItem = sidFilter.getXmlRuleById(id);
	sidFilter.editItemEnable = editItem.enable;
	
	$("#sidFilterXmlXpath").val(editItem.xpath);
	$("#sidFilterXmlPriority").val(editItem.priority);
	showPopupDialog("addSidTypeRuleXml");
}

SidFilter.prototype.getXmlRuleById = function(id)
{
	for(var i = 0; i < sidFilter.xmlXpathRules.length; i++)
	{
		var oriItem = sidFilter.xmlXpathRules[i];
		if (oriItem.itemid == id)
		{
			return oriItem;
		}
	}
	
	return null;
}

SidFilter.prototype.getTxtRuleById = function(id)
{
	for(var i = 0; i < sidFilter.txtRules.length; i++)
	{
		var oriItem = sidFilter.txtRules[i];
		if (oriItem.itemid == id)
		{
			return oriItem;
		}
	}
	
	return null;
}


SidFilter.prototype.addTxtRule = function()
{
	var itemId = sidFilter.editItemId;
	var validate = new Validate();
	var startString = new StringBuffer(document.getElementById("sidFilter_customTextRule_startStr").value);
	var startIsRegEx = document.getElementById("sidFilter_customTextRule_startIs").checked;
	var startOccurrence = document.getElementById("sidFilter_customTextRule_startOcc1").checked ? 
			"FIRST" : (document.getElementById("sidFilter_customTextRule_startOcc2").checked ? 
					"LAST" : document.getElementById("sidFilter_customTextRule_startOccTimes").value);
	
	var finishString = new StringBuffer(document.getElementById("sidFilter_customTextRule_finishStr").value);
	var finishIsRegEx = document.getElementById("sidFilter_customTextRule_finishIs").checked;
	var finishOccurrence = document.getElementById("sidFilter_customTextRule_finishOcc1").checked ? 
			"FIRST" : (document.getElementById("sidFilter_customTextRule_finishOcc2").checked ? 
					"LAST" : document.getElementById("sidFilter_customTextRule_finishOccTimes").value);
	
	var priority = document.getElementById("sidFilter_customTextRule_priority").value;

	if(validate.isEmptyStr(startString))
	{
		document.getElementById("sidFilter_customTextRule_startStr").value = "";
		alert(jsStartStringEmpty);
		return;
	}
	
	if("FIRST" != startOccurrence && "LAST" != startOccurrence && (!validate.isPositiveInteger(startOccurrence) || startOccurrence < 1))
	{
		alert(jsStartOccAlert);
		return;
	}
	
	if("FIRST" != finishOccurrence && "LAST" != finishOccurrence && (!validate.isPositiveInteger(finishOccurrence) || finishOccurrence < 1))
	{
		alert(jsFinishOccAlert);
		return;
	}
	
	if (!this.validateTxtPriority(priority, itemId))
	{
		return;
	}
	
	if(this.isCustomTextRuleExists(itemId, startString, finishString, priority))
	{
		alert(jsCustomTextRuleExist);
		return;
	}
	
	var isFinishEmpty = validate.isEmptyStr(finishString);
	var dialogId = "sidFilter_CustomTextRule_Dialog";
	
	var enable = sidFilter.editItemEnable;
	var item = {itemid : itemId, enable : enable, startString : startString.toString(), startIsRegEx : startIsRegEx, startOccurrence : startOccurrence.toString(),
			finishString : (isFinishEmpty ? "" : finishString.toString()), finishIsRegEx : finishIsRegEx, finishOccurrence : finishOccurrence.toString(), 
			priority : priority};
	
	sidFilter.addOneItemToTxtRule(item);
	sidFilter.closeTagDialog("addSidTypeRulePlain");
	sidFilter.generateTxtDiv();
}

SidFilter.prototype.validatePriority = function(newpriority, eid)
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

SidFilter.prototype.addOneItemToTxtRule = function(item)
{
	var added = false;
	for(var i = 0; i < sidFilter.txtRules.length; i++)
	{
		var oriItem = sidFilter.txtRules[i];
		if (oriItem.itemid == item.itemid)
		{
			sidFilter.txtRules[i] = item;
			added = true;
			break;
		}
	}
	
	if (!added)
	{
		sidFilter.txtRules[i] = item;
	}
	
	sidFilter.txtRules.sort(function(a,b){
        return a.priority-b.priority
   });
}

SidFilter.prototype.addXmlXpathRule = function()
{
	var itemId = sidFilter.editItemId;
	var validate = new Validate();
	var xpath = new StringBuffer(document.getElementById("sidFilterXmlXpath").value);
	var priority = document.getElementById("sidFilterXmlPriority").value;
	
	if(validate.isEmptyStr(xpath.trim()))
	{
		document.getElementById("sidFilterXmlXpath").value = "";
		alert(jsXpathEmpty);
		return;
	}
	
	if (!this.validateXmlXpathPriority(priority, itemId))
	{
		return;
	}
	
	var item = {itemid : itemId, enable : sidFilter.editItemEnable, xpath : xpath.trim(), priority : priority};
	
	sidFilter.addOneItemToXpathRule(item);
	sidFilter.closeTagDialog("addSidTypeRuleXml");
	sidFilter.generateXmlDiv();
}

SidFilter.prototype.addOneItemToXpathRule = function(item)
{
	var sortFilters =  new Array();
	var added = false;
	for(var i = 0; i < sidFilter.xmlXpathRules.length; i++)
	{
		var oriItem = sidFilter.xmlXpathRules[i];
		if (oriItem.itemid == item.itemid)
		{
			sidFilter.xmlXpathRules[i] = item;
			added = true;
			break;
		}
	}
	
	if (!added)
	{
		sidFilter.xmlXpathRules[i] = item;
	}
	
	sidFilter.xmlXpathRules.sort(function(a,b){
         return a.priority-b.priority
    });
}

SidFilter.prototype.validateTxtPriority = function(newpriority, eid)
{
	var validate = new Validate();
		
	if (!validate.isPositiveInteger(newpriority)
			|| newpriority < 1 || newpriority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	for(var j = 0; j < sidFilter.txtRules.length; j++)
	{
		var ruleObject = sidFilter.txtRules[j];
		if (newpriority == ruleObject.priority && eid != ruleObject.itemid)
		{
			alert(jsAlertSamePriority);
			return false;
		}
	}
		
	return true;
}

SidFilter.prototype.isCustomTextRuleExists = function(id, startStr, finishStr, priority)
{
	for (var i = 0; i < sidFilter.txtRules.length; i++)
	{
		var item = sidFilter.txtRules[i];
		if (item.itemid != id && item.startString == startStr && item.finishString == finishStr)
		{
			return true;
		}
	}
	
	return false;
}

SidFilter.prototype.validateXmlXpathPriority = function(newpriority, eid)
{
	var validate = new Validate();
		
	if (!validate.isPositiveInteger(newpriority)
			|| newpriority < 1 || newpriority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	for(var j = 0; j < sidFilter.xmlXpathRules.length; j++)
	{
		var ruleObject = sidFilter.xmlXpathRules[j];
		if (newpriority == ruleObject.priority && eid != ruleObject.itemid)
		{
			alert(jsAlertSamePriority);
			return false;
		}
	}
		
	return true;
}

SidFilter.prototype.checkXmlXpathItem = function(cobj)
{
	var id = cobj.id;
	var item0;
	for(var j = 0; j < sidFilter.xmlXpathRules.length; j++)
	{
		var item = sidFilter.xmlXpathRules[j];
		if (item.itemid == id)
		{
			item0 = item;
			break;
		}
	}
	
	item0.enable = cobj.checked;
}

SidFilter.prototype.checkTxtItem = function(cobj)
{
	var id = cobj.id;
	var item0;
	for(var j = 0; j < sidFilter.txtRules.length; j++)
	{
		var item = sidFilter.txtRules[j];
		if (item.itemid == id)
		{
			item0 = item;
			break;
		}
	}
	item0.enable = cobj.checked;
}

SidFilter.prototype.checkAll = function()
{
	var checkAllObj = document.getElementById("checkAllBaseFilter");
	checkAllTagsInBaseFilter = checkAllObj.checked;
	for(var i = 0; i < baseFilter.getCurrentObjectsSize(); i++)
	{
		var ruleObj = baseFilter.optionObjsMap[baseFilter.currentOption][i];
		var oid = baseFilter.getRadioId(ruleObj.itemid);
		
		if(document.getElementById(oid))
		{
			document.getElementById(oid).checked = checkAllTagsInBaseFilter;	
		}
		
		if (checkAllTagsInBaseFilter)
		{
			baseFilter.checkedItemIds.appendUniqueObj(oid);
		}
		
		ruleObj.enable = checkAllTagsInBaseFilter;
	}
	
	if (!checkAllTagsInBaseFilter)
	{
		baseFilter.checkedItemIds = new Array();
	}
}


function saveSidFilter()
{
	var validate = new Validate();
	var filterName = document.getElementById("sidFilterName").value;
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
    
    var filterType = $(".sidFilterType").val();
    if (filterType == -1)
    {
    	alert("Please select the SID Filter Type.");
    	return;
    }
    
	var filterDesc = document.getElementById("sidFilterDesc").value;
	
	var rule = null;
	if (filterType == 1){
		rule = JSON.stringify(sidFilter.xmlXpathRules);
	} else if (filterType == 2){
		rule = JSON.stringify(sidFilter.txtRules);
	}
	
	var obj = {
		filterName : filterName,
		filterDesc : filterDesc,
		companyId : companyId,
		filterType : filterType,
		rule : rule
	};
	
	if (saveSidFilter.edit){
		obj.filterId = saveSidFilter.filterId
	}
	// send for check
	sendAjax(obj, "saveOrUpdateSidFilter", "saveOrUpdateSidFilterCallback");
	
	saveSidFilter.obj = obj;
}

function saveOrUpdateSidFilterCallback(data)
{
	if (data == 'name_exists')
	{
		alert(existFilterName);
	}
	else
	{
		var color = saveSidFilter.color;
		var topFilterId = saveSidFilter.topFilterId;
		var filter = getFilterById(topFilterId);
		if(filter)
		{
			var xrFilter = new SidFilter();
			xrFilter.id = data - 0;
			xrFilter.filterTableName = "sid_filter";
			xrFilter.filterName = saveSidFilter.obj.filterName;
			xrFilter.filterDescription = saveSidFilter.obj.filterDesc;
			xrFilter.filterType = saveSidFilter.obj.filterType;
			xrFilter.companyId = companyId;
			xrFilter.jsonText = saveSidFilter.obj.rule;
			
			if (!saveSidFilter.filterId){
				filter.specialFilters.push(xrFilter);
				reGenerateFilterList(topFilterId, filter.specialFilters, color);
			} else {
				var specialFilters = updateSpecialFilter(filter.specialFilters, xrFilter);
				reGenerateFilterList(topFilterId, specialFilters, color);
			}
		}
		closePopupDialog("sidFilterDialog");
	}
}

SidFilter.prototype.closeAllTagPopup = function()
{
	closePopupDialog("addSidTypeRuleXml");
	closePopupDialog("addSidTypeRulePlain");
	closePopupDialog("sidFilterDialog");	
}

SidFilter.prototype.prePage = function(type)
{
	if (type == 1){
		if (this.currentPage1 > 0){
			this.currentPage1 --;
			this.generateXmlDiv();
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

SidFilter.prototype.nextPage = function(type)
{
	if (type == 1){
		var arraysize = sidFilter.xmlXpathRules.length;
		if(this.currentPage1 < this.getPageSize(arraysize) - 1)
		{
			this.currentPage1 ++;
			this.generateXmlDiv();
		}else{
			alert(canNotNextPage);
			return;
		}
	} else if (type == 2){
		var arraysize = sidFilter.txtRules.length;
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

SidFilter.prototype.goToPage = function(type)
{
	if (type == 1){
		var pageValue = document.getElementById("pageCountSidXmlFilter").value;
		var validate = new Validate();
		if(!validate.isNumber(pageValue))
		{
			alert(pageValueIsNotNumber);
			return;
		}
		
		pageValue = pageValue - 0;
		var arraysize = this.xmlXpathRules.length;
		
		if(pageValue < 1 || pageValue > this.getPageSize(arraysize))
		{
			alert(invalidatePageValue.replace("%s", this.getPageSize(arraysize)));
			this.setPageValue(type);
			return;
		}
		
		this.currentPage1 = pageValue - 1;
		this.generateXmlDiv();
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

SidFilter.prototype.setPageValue = function(type)
{
	if (type == 1){
		$("#pageCountSidXmlFilter").val(this.currentPage1 + 1);
		$("#pageTotalSizeSidXmlFilter").html(this.getPageSize(sidFilter.xmlXpathRules.length));
	} else if (type == 2){
		$("#pageCountSidTxtFilter").val(this.currentPage2 + 1);
		$("#pageTotalSizeSidTxtFilter").html(this.getPageSize(sidFilter.txtRules.length));
	}
}

SidFilter.prototype.getCurrentPage = function(type)
{
	if (type == 1)
		return currentPage1;
	
	if (type == 2)
		return currentPage2;
}

SidFilter.prototype.getPageSize = function (totle)
{
	var countPerPage = sidFilter.tagsEveryPage;
	var pagesize = Math.floor(totle/countPerPage +  (totle%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

SidFilter.prototype.deleteXmlRule = function ()
{
	var hasTagsToDelete = sidFilter.generateDeleteXmlTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteSidxmlTagDialog");
	}
}

SidFilter.prototype.generateDeleteXmlTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='500px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_sidFilterXmlTag' onclick='sidFilter.selectAll_sidFilterXmlTag()'/>");//for gbs-2599
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
	
	for(var j = 0; j < sidFilter.xmlXpathRules.length; j++)
	{
		var ruleObject = sidFilter.xmlXpathRules[j];
		if(ruleObject.enable)
		{
			str.append("<input type='checkbox' name='sidFilterDeleteXml' tagValue='"+ruleObject.itemid+"' checked>");
			str.append(ruleObject.xpath);
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
	
	document.getElementById("deleteSidxmlTagDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteSidxmlTagTableContent").innerHTML = str.toString();
	return true;
}

SidFilter.prototype.deleteCheckedXmlTags = function()
{
	var aoption = baseFilter.availableOptions[i];
	var ruleObjects = baseFilter.optionObjsMap[aoption];
    var checkBoxId = "delete_tags_" + aoption;
    var checkBoxObjs = document.getElementsByName("sidFilterDeleteXml");
    for(var i = 0; i < checkBoxObjs.length; i++)
    {
    	var checkBoxObj = checkBoxObjs[i];
        if (checkBoxObj.checked)
        {
        	var itemId = checkBoxObj.getAttribute("tagValue");
        	
        	for(var j = 0; j < sidFilter.xmlXpathRules.length; j++)
        	{
        		var ruleObject = sidFilter.xmlXpathRules[j];
        		if(ruleObject.itemid == itemId)
        		{
        			sidFilter.xmlXpathRules.removeData(ruleObject);
        			break;
        		}
        	}
        }
    }

	
	this.generateXmlDiv();
	closePopupDialog("deleteSidxmlTagDialog");
}

SidFilter.prototype.deletePlainTextRule = function ()
{
	var hasTagsToDelete = sidFilter.generateDeleteTxtTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteSidTxtTagDialog");
	}
}

SidFilter.prototype.generateDeleteTxtTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='500px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_sidFilterTxt' onclick='sidFilter.selectAll_sidFilterTxt()'/>");//for gbs-2599
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
	
	for(var j = 0; j < sidFilter.txtRules.length; j++)
	{
		var ruleObject = sidFilter.txtRules[j];
		if(ruleObject.enable)
		{
			var encodedName = ruleObject.startString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;");
			str.append("<input type='checkbox' name='sidFilterDeleteTxt' tagValue='"+ruleObject.itemid+"' checked>");
			str.append(encodedName);
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
	
	document.getElementById("deleteSidTxtTagDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteSidTxtTagTableContent").innerHTML = str.toString();
	return true;
}

SidFilter.prototype.deleteCheckedTxtTags = function()
{
    var checkBoxObjs = document.getElementsByName("sidFilterDeleteTxt");
    for(var i = 0; i < checkBoxObjs.length; i++)
    {
    	var checkBoxObj = checkBoxObjs[i];
        if (checkBoxObj.checked)
        {
        	var itemId = checkBoxObj.getAttribute("tagValue");
        	
        	for(var j = 0; j < sidFilter.txtRules.length; j++)
        	{
        		var ruleObject = sidFilter.txtRules[j];
        		if(ruleObject.itemid == itemId)
        		{
        			sidFilter.txtRules.removeData(ruleObject);
        			break;
        		}
        	}
        }
    }
	
	this.generateTxtDiv();
	closePopupDialog("deleteSidTxtTagDialog");
}

SidFilter.prototype.checkAllSidFilterXpath = function()
{
	if ($("#checkAllSidFilterXpath")[0].checked){
		$(".xpathItemCheckbox").each(function(){   
			if (!$(this).is(':checked')) {
				this.checked = true;
				sidFilter.checkXmlXpathItem(this);
			}
		});
	} else {
		$(".xpathItemCheckbox").each(function(){   
			if ($(this).is(':checked')) {
				this.checked = false;
				sidFilter.checkXmlXpathItem(this);
			}
		});
	}
}

SidFilter.prototype.checkAllSidFilterTxt = function(){
	if ($("#checkAllSidFilterTxt")[0].checked){
		$(".txtItemCheckbox").each(function(){   
			if (!$(this).is(':checked')) {
				this.checked = true;
				sidFilter.checkTxtItem(this);
			}
		});
	} else {
		$(".txtItemCheckbox").each(function(){   
			if ($(this).is(':checked')) {
				this.checked = false;
				sidFilter.checkTxtItem(this);
			}
		});
	}
}

function isEmptyObject(e) {  
	var t;  
	for (t in e)  
       return !1;
	
    return !0  
}  

SidFilter.prototype.cloneObject = function(oriObj)
{
	var txt = JSON.stringify(oriObj);
	return JSON.parse(txt);
}

SidFilter.prototype.alertObject = function(obj)
{
	var txt = JSON.stringify(obj);
	return alert(txt);
}

SidFilter.prototype.getItemById = function(itemId, optionValue)
{
	var item;
	var items = sidFilter.optionObjsMap[optionValue];
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

SidFilter.prototype.closeTagDialog = function(dialogId)
{
	closePopupDialog(dialogId);	
	sidFilter.editItemId = -1;
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
