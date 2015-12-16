var baseFilter = new BaseFilter();
var checkAllTagsInBaseFilter = false;
var baseTagsContentTable = "<table id='baseFilterTagsContentTable' border=0 width='98%' class='standardText'>"; 
var fontTagS = "<font class='specialFilter_dialog_label'>";
var fontTagE = "</font>"
var imgYes = "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>";
var checkedId = "";
	
function BaseFilter()
{
	this.filterTableName = "base_filter";
	this.dialogId = "baseFilterDialog";
	
	this.optionInternalTexts = "0";
	this.optionEscapings = "1";
	this.optionNameMap = new Object();
	this.optionNameMap[this.optionInternalTexts] = jsInternalText;
	this.optionNameMap[this.optionEscapings] = jsEscaping;
	
	this.operatorEqual = "equal";
	this.operatorNotEqual = "not equal";
	this.operatorMatch = "match";	
	this.operatorNameMap = new Object();
	this.operatorNameMap[this.operatorEqual] = jsOperatorEqual;
	this.operatorNameMap[this.operatorNotEqual] = jsOperatorNotEqual;
	this.operatorNameMap[this.operatorMatch] = jsOperatorMatch;
	
	this.optionObjsMap = new Object();
	this.availableOptions = [this.optionInternalTexts, this.optionEscapings];
	this.checkedItemIds = new Array();
	
	this.tagsEveryPage = 10;
	this.currentPage = 0;
	this.sortOrder = "asc";
	this.sortColumnIndex = 1;
	this.currentPage = 0;
	this.currentOption = this.optionInternalTexts;
	this.editItemId = -1;
	this.editItemEnable = false;
}

BaseFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

BaseFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

BaseFilter.prototype.initOptionMap = function (filter)
{
	baseFilter.checkedItemIds = new Array();
	checkAllTagsInBaseFilter = false;
	
	if (filter)
	{
		var obj0 = {tagName : "name1", enable : true, itemid : 1, attributes : [{itemid : 0, aName : "name1", aOp : "equal", aValue : "vvv1"}, {itemid : 1, aName : "name2", aOp : "equal", aValue : "vvv2"}]};
		var obj1 = {tagName : "name2", enable : true, itemid : 2, attributes : [{itemid : 0, aName : "name1", aOp : "equal", aValue : "vvv1"}, {itemid : 1, aName : "name2", aOp : "equal", aValue : "vvv2"}]};
		
		var objArray = [obj0, obj1];
		
		// baseFilter.optionObjsMap[this.optionPreserveWsTags] = objArray;
		// alert(preserveWsTags);
		
		// internal text
		var internalTexts = this.filter.internalTexts;
		baseFilter.optionObjsMap[this.optionInternalTexts] = JSON.parse(internalTexts);
		
		var escapings = this.filter.escapings;
		baseFilter.optionObjsMap[this.optionEscapings] = JSON.parse(escapings);
	}
	else
	{
		baseFilter.optionObjsMap[this.optionInternalTexts] = new Array();
		baseFilter.optionObjsMap[this.optionEscapings] = new Array();
	}
	
	baseFilter.refreshCheckedIds();
}

BaseFilter.prototype.refreshCheckedIds = function()
{
	baseFilter.checkedItemIds = new Array();
	var ruleObjs = baseFilter.optionObjsMap[baseFilter.currentOption];
	
	if (ruleObjs && ruleObjs.length > 0)
	{
		for(var i = 0; i<ruleObjs.length; i++)
		{
			var ruleObj = ruleObjs[i];
			if (ruleObj.enable)
			{
				var radioId = baseFilter.getRadioId(ruleObj.itemid);
				baseFilter.checkedItemIds.appendUniqueObj(radioId);
			}
		}
		
		//baseFilter.alertObject(baseFilter.checkedItemIds);
	}
}

BaseFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
// alert("filterId :" + filterId + "\n" + "topFilterId :" + topFilterId);
	this.initOptionMap(this.filter);
	baseFilter.currentPage = 0;
	var str = new StringBuffer("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");	
	str.append("<td><input type='text' style='width:100%' id='baseFilterName' maxlength='"+maxFilterNameLength+"' value='" + this.filter.filterName + "'></input>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");	
	str.append("<td><textarea style='width:100%' rows='4' id='baseFilterDesc' name='desc'>"+this.filter.filterDescription+"</textarea>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='400px'>");	
	str.append("<tr>");
	str.append("<td>");
	str.append("<br /><br />");
	str.append("</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<div class='specialFilter_dialog_label' style='width:98%'>");
	str.append(this.generateTagsTable(this.filter));
	str.append("</div>");
	
	var dialogObj = document.getElementById('baseFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	
	saveBaseFilter.edit = true;
	saveBaseFilter.filterId = filterId;
	saveBaseFilter.color = color;
	saveBaseFilter.filter = this.filter;
	saveBaseFilter.specialFilters = specialFilters;
	saveBaseFilter.topFilterId = topFilterId;
}

BaseFilter.prototype.generateDiv = function(topFilterId, color)
{
	this.initOptionMap();
	var str = new StringBuffer("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");	
	str.append("<td><input type='text' style='width:100%' id='baseFilterName' maxlength='"+maxFilterNameLength+"' value='Base Text Filter'></input>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");	
	str.append("<td><textarea style='width:100%' rows='4' id='baseFilterDesc' name='desc'></textarea>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td>");
	str.append("<br /><br />");
	str.append("</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<div class='specialFilter_dialog_label' style='width:98%'>");
	str.append(this.generateTagsTable());
	str.append("</div>");
	
	var dialogObj = document.getElementById('baseFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveBaseFilter.edit = false;
	saveBaseFilter.topFilterId = topFilterId;
	saveBaseFilter.color = color;
}

function saveBaseFilter()
{
	var validate = new Validate();
	var filterName = document.getElementById("baseFilterName").value;
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
    
    var isNew = (saveBaseFilter.edit) ? "false" : "true";
	var filterDesc = document.getElementById("baseFilterDesc").value;
	
	var internalTexts = JSON.stringify(baseFilter.optionObjsMap[baseFilter.optionInternalTexts]);
	var escapings = JSON.stringify(baseFilter.optionObjsMap[baseFilter.optionEscapings]);

	var obj = {
		isNew : isNew,
		filterTableName : "base_filter",
		filterName : filterName,
		filterDesc : filterDesc,
		filterId : saveBaseFilter.filterId,
		companyId : companyId,
		internalTexts : internalTexts,
		escapings : escapings
	};
	// send for check
	sendAjax(obj, "isFilterValid", "isBaseFilterValidCallback");
	
	isFilterValidCallback.obj = obj;
}

function isBaseFilterValidCallback(data)
{
	if(data == 'true')
	{
		closePopupDialog("baseFilterDialog");
		if(saveBaseFilter.edit)
		{
			sendAjax(isFilterValidCallback.obj, "updateBaseFilter", "updateBaseFilterCallback");
		}
		else
		{
			sendAjax(isFilterValidCallback.obj, "saveBaseFilter", "saveBaseFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("baseFilterDialog");
		parent.location.reload();
	}
	else if (data == 'name_exists')
	{
		alert(existFilterName);
	}
	else
	{
		alert(data);
	}
}

function updateBaseFilterCallback(data)
{
	var color = saveBaseFilter.color;
	var filterId = saveBaseFilter.filterId;
	var filter = saveBaseFilter.filter;
	var topFilterId = saveBaseFilter.topFilterId;
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = filterId;
		xrFilter.filterTableName = "base_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.internalTexts = isFilterValidCallback.obj.internalTexts;
		xrFilter.escapings = isFilterValidCallback.obj.escapings;
		xrFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveBaseFilter.specialFilters, xrFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveBaseFilterCallback(data)
{
	var color = saveBaseFilter.color;
	var topFilterId = saveBaseFilter.topFilterId;
	
	var filter = getFilterById(topFilterId);
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = data - 0;
		xrFilter.filterTableName = "base_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.internalTexts = isFilterValidCallback.obj.internalTexts;
		xrFilter.escapings = isFilterValidCallback.obj.escapings;
		xrFilter.companyId = companyId;
		filter.specialFilters.push(xrFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

BaseFilter.prototype.generateTagsTable = function (filter)
{
	var str = new StringBuffer("");
	str.append("<table border=0 width='98%' class='standardText'>");
	str.append("<tr><td>");
	str.append("<div id='baseFilterRulesContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='baseFilterRulesSection' onchange='baseFilter.switchRules(this)'>");
	for(var i = 0; i < baseFilter.availableOptions.length; i++)
	{
		var optionV = baseFilter.availableOptions[i];
		str.append("<option value='" + optionV + "'");
		str.append(optionV == baseFilter.currentOption ? " selected " : "");
		str.append(">" + this.optionNameMap[optionV] + "</option>");
	}
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "...' onclick='baseFilter.addTag()'></input>");
	str.append("<input type='button' value='" + jsDelete + "...' onclick='baseFilter.deleteTag()' id='cmdDeleteTag'></input>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	str.append("<div id='baseTagsContent'>");
	str.append(baseTagsContentTable);
	
	if (filter)
	{
		str.append(baseFilter.generateTagsContent(baseFilter.currentOption, 0));
	}
	else
	{
		str.append(baseFilter.generateTagsContent(baseFilter.currentOption));
	}
	
	str.append("</table>");
	str.append("</div>");
	str.append("<div>");
	
	var pageTotalSize = filter ? baseFilter.getPageSize(baseFilter.optionObjsMap[baseFilter.currentOption].length) : 1;
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=baseFilter.prePage()>" + jsPrevious + "</a>");
	str.append("|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=baseFilter.nextPage()>" + jsNext + "</a>");
	str.append("<input id='pageCountBaseFilter' size=2 type='text' value="+((filter) ? baseFilter.currentPage+1 : 1)+" ></input>");
	str.append(" / <span id='pageTotalSizeBaseFilter' class='standardText'>" + pageTotalSize + "</span> ");
	str.append("<input type='button' value='" + jsGo + "' onclick=baseFilter.goToPage()></input>");
	str.append("</div>");
	
	return str.toString();
}

BaseFilter.prototype.closeAllTagPopup = function()
{
	closePopupDialog("baseFilter_InternalText_Dialog");
	closePopupDialog("editPriorityDialog");
	closePopupDialog("deleteBaseTagDialog");	
	closePopupDialog("baseFilter_Escaping_Dialog");
}

BaseFilter.prototype.addTag = function(radioId)
{
	var dialogId = "baseFilter_InternalText_Dialog";
	var isEdit = (radioId) ? true : false;
	var useInternalTextDiv = (baseFilter.currentOption == baseFilter.optionInternalTexts);
	var useEscapingDiv = (baseFilter.currentOption == baseFilter.optionEscapings);
	
	baseFilter.closeAllTagPopup();
	
	if (useInternalTextDiv)
	{
		dialogId = "baseFilter_InternalText_Dialog";
		//document.getElementById("baseFilter_pi_title").innerHTML = (isEdit ? jsEditProcessIns : jsAddProcessIns);
	}
	else if (useEscapingDiv)
	{
		dialogId = "baseFilter_Escaping_Dialog";
	}
	
	showPopupDialog(dialogId);
	
	var editId = isEdit ? baseFilter.getItemId(radioId) : -1;
	var editItem = isEdit ? baseFilter.getItemById(editId, baseFilter.currentOption) : new Object();
	baseFilter.editItemId = (isEdit? editItem.itemid : baseFilter.newItemId());
	baseFilter.editItemEnable = (isEdit? editItem.enable : false);
	
	if (useInternalTextDiv)
	{
		var aName = isEdit ? editItem.aName : "";
		document.getElementById("baseFilter_InternalText").value = aName;
		document.getElementById("baseFilter_InternalText_isRE").checked = editItem.isRE;
		document.getElementById("baseFilter_InternalText_priority").value = isEdit ? editItem.priority : "";
	}
	else if (useEscapingDiv)
	{
		var aName = isEdit ? editItem.aName : "";
		document.getElementById("baseFilter_escaping_char").value = aName;
		document.getElementById("baseFilter_escaping_import").checked = editItem.unEscapeOnImport;
		document.getElementById("baseFilter_escaping_export").checked = editItem.reEscapeOnExport;
		document.getElementById("baseFilter_escaping_priority").value = isEdit ? editItem.priority : "";
	}
}

BaseFilter.prototype.cloneObject = function(oriObj)
{
	var txt = JSON.stringify(oriObj);
	return JSON.parse(txt);
}

BaseFilter.prototype.alertObject = function(obj)
{
	var txt = JSON.stringify(obj);
	return alert(txt);
}

BaseFilter.prototype.getItemById = function(itemId, optionValue)
{
	var item;
	var items = baseFilter.optionObjsMap[optionValue];
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

BaseFilter.prototype.saveInternalText = function()
{
	var itemId = baseFilter.editItemId;
	var validate = new Validate();
	var aName = new StringBuffer(document.getElementById("baseFilter_InternalText").value);
	var isRE = document.getElementById("baseFilter_InternalText_isRE").checked;
	var priority = document.getElementById("baseFilter_InternalText_priority").value;

	if(validate.isEmptyStr(aName.trim()))
	{
		document.getElementById("baseFilter_InternalText").value = "";
		alert(jsInternalEmpty);
		return;
	}
	
	if (!this.validatePriority(priority, itemId))
	{
		return;
	}
	
	if(this.isInternalTextExist(aName, isRE, priority))
	{
		alert(jsInternalExist);
		return;
	}
	
	var dialogId = "baseFilter_InternalText_Dialog";
	
	var enable = baseFilter.editItemEnable;
	var item = {itemid : itemId, enable : enable, aName : aName.trim(), isRE : isRE, priority : priority};
	
	baseFilter.addOneItemInCurrentOptions(item);
	baseFilter.closeTagDialog(dialogId);
}

BaseFilter.prototype.saveEscaping = function()
{
	var itemId = baseFilter.editItemId;
	var validate = new Validate();
	var aName = new StringBuffer(document.getElementById("baseFilter_escaping_char").value);
	var unEscapeOnImport = document.getElementById("baseFilter_escaping_import").checked;
	var reEscapeOnExport = document.getElementById("baseFilter_escaping_export").checked;
	var priority = document.getElementById("baseFilter_escaping_priority").value;

	if(validate.isEmptyStr(aName.trim()))
	{
		document.getElementById("baseFilter_escaping_char").value = "";
		alert(jsEscapingEmpty);
		return;
	}
	
	if (!this.validatePriority(priority, itemId))
	{
		return;
	}
	
	if(this.isEscapingExist(itemId, aName, unEscapeOnImport, reEscapeOnExport, priority))
	{
		alert(jsEscapingExist);
		return;
	}
	
	var dialogId = "baseFilter_Escaping_Dialog";
	
	var enable = baseFilter.editItemEnable;
	var item = {itemid : itemId, enable : enable, aName : aName.trim(), unEscapeOnImport : unEscapeOnImport, reEscapeOnExport : reEscapeOnExport, priority : priority};
	
	baseFilter.addOneItemInCurrentOptions(item);
	baseFilter.closeTagDialog(dialogId);
}

BaseFilter.prototype.isEscapingExist = function(id, content, unEscapeOnImport, reEscapeOnExport, priority)
{
	var items = this.optionObjsMap[this.optionEscapings];
	for (var i = 0; i < items.length; i++)
	{
		var item = items[i];
		if (item.itemid != id && item.aName == content)
		{
			return true;
		}
	}
	
	return false;
}

BaseFilter.prototype.isInternalTextExist = function(content, isRegex, priority)
{
	var internalTexts = this.optionObjsMap[this.optionInternalTexts];
	for (var i = 0; i < internalTexts.length; i++)
	{
		var item = internalTexts[i];
		if (item.aName == content && item.isRE == isRegex && item.priority == priority)
		{
			return true;
		}
	}
	
	return false;
}

BaseFilter.prototype.addOneItemInCurrentOptions = function(item)
{
	if (baseFilter.isDefined(item))
	{
		var i = 0;
		var objArray = baseFilter.optionObjsMap[baseFilter.currentOption];
		var added = false;
		var itemId = item.itemid;
		
		for(; i < objArray.length; i++)
		{
			var oriItem = objArray[i];
			if (oriItem.itemid == itemId)
			{
				objArray[i] = item;
				added = true;
				break;
			}
		}
		
		if (!added)
		{
			objArray[i] = item;
			checkAllTagsInBaseFilter = false;
		}
		
		var maxPages = baseFilter.getPageSize(objArray.length);
		document.getElementById("pageTotalSizeBaseFilter").innerText = maxPages;
		
		var content = baseFilter.generateTagsContent(baseFilter.currentOption, baseFilter.currentPage);
		baseFilter.refreshTagsContent(content);
	}
}

BaseFilter.prototype.closeTagDialog = function(dialogId)
{
	closePopupDialog(dialogId);	
	baseFilter.editItemId = -1;
}

BaseFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = baseFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteBaseTagDialog");
	}
}

BaseFilter.prototype.deleteCheckedTags = function()
{
	for(var i = 0; i < baseFilter.availableOptions.length; i++)
    {
		var aoption = baseFilter.availableOptions[i];
		var ruleObjects = baseFilter.optionObjsMap[aoption];
        var checkBoxId = "delete_tags_" + aoption;
        var checkBoxObjs = document.getElementsByName(checkBoxId);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
        	var checkBoxObj = checkBoxObjs[j];
            if (checkBoxObj.checked)
            {
            	var itemId = checkBoxObj.getAttribute("tagValue");
            	var ruleObj = baseFilter.getItemById(itemId, aoption);
            	
            	if (ruleObj && ruleObjects)
            	{
            		ruleObjects.removeData(ruleObj);
            	}
            }
        }
    }
	
	baseFilter.setPageValue();
	
	closePopupDialog("deleteBaseTagDialog");
	baseFilter.switchRules(document.getElementById("baseFilterRulesSection"));
}

//for gbs-2599
BaseFilter.prototype.selectAll_BaseFilter = function()
{
	var selectAll = document.getElementById("selectAll_BaseFilter")
	if(selectAll.checked) {
		this.checkAllTagsToDelete();
	} else {
		this.clearAllTagsToDelete();
	}
}

BaseFilter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsTagType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_BaseFilter' onclick='baseFilter.selectAll_BaseFilter()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsTagsToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsTagsCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	var tagTypes = document.getElementById("baseFilterRulesSection");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var ruleObjects = baseFilter.optionObjsMap[value];
		var checkboxId = "delete_tags_" + value;
		var doEncodeName = (value == baseFilter.optionInternalTexts);
		
		var showCurrent = false;
		if(ruleObjects && ruleObjects.length > 0)
		{
			for(var j = 0; j < ruleObjects.length; j++)
			{
				var ruleObject = ruleObjects[j];
				if(ruleObject.enable)
				{
					showCurrent = true;
					break;
				}
			}
		}
		if (showCurrent)
		{
			var backColor = isOdd ? "#DFE3EE" : "#C7CEE0";
			isOdd = !isOdd;
			
			str.append("<tr style='background-color:"+backColor+";padding:4px'>");
			str.append("<td width='108px'>");
			str.append(text);
			str.append("</td>");	
			str.append("<td width='400px'>");
			
			for(var j = 0; j < ruleObjects.length; j++)
			{
				var ruleObject = ruleObjects[j];
				if(ruleObject.enable)
				{
					var aaaname = baseFilter.isDefined(ruleObject.tagName)?ruleObject.tagName:ruleObject.aName;
					var encodedName = doEncodeName ? encodeHtmlEntities(aaaname) : aaaname;
					str.append("<input type='checkbox' name='"+checkboxId+"' tagType='" + value + "' tagValue='"+ruleObject.itemid+"' checked>");
					str.append(encodedName);
					str.append("</input>");
					count ++;
				}
			}
			sum += count;
			str.append("</td>");
			str.append("<td width='22px'>");
			str.append(count);
			str.append("</td>");
			str.append("</tr>");	
		}
	} 
	str.append("</table></center>");
	/*gbs-2599
	str.append("<a href='#' class='specialfilter_a' onclick='baseFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='baseFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteBaseTagsDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteBaseTagTableContent").innerHTML = str.toString();
	return true;
}

BaseFilter.prototype.generatePriorityTagTableContent = function()
{
	checkedId = "";
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='500px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='100px'>");
	str.append("<Label class='tagName_td'>" + jsCurrentPriority + "</Label>");
	str.append("</td>");
	str.append("<td width='200px'>");
	str.append("<Label class='tagName_td'>" + jsInternalContent + "</Label>");
	str.append("</td>");
	str.append("<td width='200px'>");
	str.append("<Label class='tagName_td'>" + jsNewPriority + "</Label>");
	str.append("</td>");
	str.append("</tr>");
	
	var ruleObjects = baseFilter.optionObjsMap[baseFilter.currentOption];
	var inputIdPre = "tag_priority_";
	var doEncodeName = (baseFilter.currentOption == baseFilter.optionInternalTexts);
	var isOdd = true;
	
	for(var j = 0; j < ruleObjects.length; j++)
	{
		var backColor = isOdd ? "#DFE3EE" : "#C7CEE0";
		isOdd = !isOdd;
				
		var ruleObject = ruleObjects[j];
		var aaaname = ruleObject.aName;
		var encodedName = doEncodeName ? encodeHtmlEntities(aaaname) : aaaname;
		str.append("<tr style='background-color:"+backColor+";padding:4px'>");
		str.append("<td>");
		str.append(ruleObject.priority);
		str.append("</td>");
		str.append("<td>");
		str.append(encodedName);
		str.append("</td>");
		str.append("<td>");
		var iddd = inputIdPre + ruleObject.itemid;
		//str.append("<input type='text' name='"+iddd+"' id='"+iddd+"' value='"+ruleObject.priority+"' onblur='baseFilter.checkPriorities()'></input>");
		str.append("<input type='text' name='"+iddd+"' id='"+iddd+"' value='"+ruleObject.priority+"'></input>");
		str.append("</td>");
		str.append("</tr>");
	}
	str.append("</table></center>");
	
	document.getElementById("editPriorityTableContent").innerHTML = str.toString();
	showPopupDialog("editPriorityDialog");
}

BaseFilter.prototype.checkPriorities = function()
{
	var ruleObjects = baseFilter.optionObjsMap[baseFilter.currentOption];
	var inputIdPre = "tag_priority_";
	var valuesArray = new Array();
	var validate = new Validate();
	
	for(var j = 0; j < ruleObjects.length; j++)
	{
		var ruleObject = ruleObjects[j];
		var iddd = inputIdPre + ruleObject.itemid;
		var newpriority = document.getElementById(iddd).value;
		
		if ((!validate.isPositiveInteger(newpriority)
				|| newpriority < 1 || newpriority > 255)
			&& iddd != checkedId)
		{
			checkedId = iddd;
			alert(jsAlertPriorityValue + " - " + ruleObject.aName);
			document.getElementById(iddd).focus();
			return;
		}
		
		valuesArray[j] = newpriority;
	}
}

BaseFilter.prototype.validatePriority = function(newpriority, eid)
{
	var validate = new Validate();
		
	if (!validate.isPositiveInteger(newpriority)
			|| newpriority < 1 || newpriority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	var ruleObjects = baseFilter.optionObjsMap[baseFilter.currentOption];
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

BaseFilter.prototype.savePriorities = function()
{
	var ruleObjects = baseFilter.optionObjsMap[baseFilter.currentOption];
	var inputIdPre = "tag_priority_";
	var valuesArray = new Array();
	var validate = new Validate();
	
	for(var j = 0; j < ruleObjects.length; j++)
	{
		var ruleObject = ruleObjects[j];
		var iddd = inputIdPre + ruleObject.itemid;
		var newpriority = document.getElementById(iddd).value;
		
		if (!validate.isPositiveInteger(newpriority)
				|| newpriority < 1 || newpriority > 255)
		{
			alert(jsAlertPriorityValue + " - " + ruleObject.aName);
			return;
		}
		
		valuesArray[j] = newpriority;
	}
	
	for(var j = 0; j < valuesArray.length; j++)
	{
		var newpriority = valuesArray[j];
		for (var i = j + 1; i < valuesArray.length; i++)
		{
			var new2 = valuesArray[i];
			
			if (newpriority == new2)
			{
				alert(jsAlertSamePriority);
				return;
			}
		}
	}
	
	for(var j = 0; j < ruleObjects.length; j++)
	{
		var ruleObject = ruleObjects[j];
		ruleObject.priority = valuesArray[j];
	}
	
	closePopupDialog("editPriorityDialog");
	var content = baseFilter.generateTagsContent(baseFilter.currentOption, baseFilter.currentPage);
	baseFilter.refreshTagsContent(content);
	
}

BaseFilter.prototype.isDefined = function(objj)
{
	return (typeof(objj) != 'undefined');
}

BaseFilter.prototype.setCheckOrClearAll = function( /*boolean*/checkOrClear )
{
    for(var i = 0; i < baseFilter.availableOptions.length; i++)
    {
        var checkBoxId = "delete_tags_" + baseFilter.availableOptions[i];
        var checkBoxObjs = document.getElementsByName(checkBoxId);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
            checkBoxObjs[j].checked = checkOrClear;
        }
    }
}

BaseFilter.prototype.checkAllTagsToDelete = function ()
{
	baseFilter.setCheckOrClearAll(true);
}

BaseFilter.prototype.clearAllTagsToDelete = function()
{
	baseFilter.setCheckOrClearAll(false);
}

BaseFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	//var newid = (new Date()).getTime() + "" + Math.round(Math.random()*100);
	return newid;
}

BaseFilter.prototype.setPageValue = function()
{
	var curOption = baseFilter.currentOption;
	var maxPages = baseFilter.getPageSize(baseFilter.optionObjsMap[curOption].length);
	baseFilter.currentPage = (baseFilter.currentPage >= maxPages) ? maxPages - 1 : baseFilter.currentPage;
	document.getElementById("pageTotalSizeBaseFilter").innerHTML = maxPages;
	
	document.getElementById("pageCountBaseFilter").value = baseFilter.currentPage + 1;
}

BaseFilter.prototype.prePage = function()

{
    if(baseFilter.currentPage > 0)
	{
    	baseFilter.currentPage --;
    	var content = baseFilter.generateTagsContent(baseFilter.currentOption, baseFilter.currentPage);
		baseFilter.refreshTagsContent(content);
    	baseFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

BaseFilter.prototype.nextPage = function()
{
	var arraysize = baseFilter.getCurrentObjectsSize();
	if(baseFilter.currentPage < baseFilter.getPageSize(arraysize) - 1)
	{
		baseFilter.currentPage ++;
		var content = baseFilter.generateTagsContent(baseFilter.currentOption, baseFilter.currentPage);
		baseFilter.refreshTagsContent(content);
		baseFilter.setPageValue();
	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

BaseFilter.prototype.getPageSize = function(itemsTotalCount)
{
	var countPerPage = baseFilter.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

BaseFilter.prototype.goToPage = function()
{
	var pageValue = document.getElementById("pageCountBaseFilter").value;
	var validate = new Validate();
	if(!validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	var arraysize = baseFilter.getCurrentObjectsSize();
	
	if(pageValue < 1 || pageValue > baseFilter.getPageSize(arraysize))
	{
		alert(invalidatePageValue.replace("%s", baseFilter.getPageSize(arraysize)));
		baseFilter.setPageValue();
		return;
	}

	baseFilter.currentPage = pageValue - 1;
	var content = baseFilter.generateTagsContent(baseFilter.currentOption, baseFilter.currentPage);
	baseFilter.refreshTagsContent(content);
}

BaseFilter.prototype.switchRules = function(baseFilterRulesSection)
{
	baseFilter.currentOption = baseFilterRulesSection.options[baseFilterRulesSection.selectedIndex].value;
	baseFilter.currentPage = 0;
	
	baseFilter.closeAllTagPopup();
	baseFilter.refreshCheckedIds();
	var content = baseFilter.generateTagsContent(baseFilter.currentOption, baseFilter.currentPage);
	baseFilter.refreshTagsContent(content);
	document.getElementById("checkAllBaseFilter").checked = false;
	baseFilter.setPageValue();
}

BaseFilter.prototype.refreshTagsContent = function(content)
{
	var ccc = new StringBuffer(baseTagsContentTable);
	ccc.append(content);
	ccc.append("</table>");
	
	document.getElementById("baseTagsContent").innerHTML = ccc.toString();
}

BaseFilter.prototype.generateTagsContent = function(optionValue, pageIndex)
{
	// var objArray = baseFilter.parserXml(optionValue, ruleXml);
	var objArray = baseFilter.optionObjsMap[optionValue];
	var str = new StringBuffer("");
	str.append("<tr class='htmlFilter_emptyTable_tr'>");
	
	if (objArray && objArray.length > 0)
	{
		for(var i = 0; i < objArray.length; i++)
		{
			var ruleone = objArray[i];
			if (typeof(ruleone.priority) == "undefined")
			{
				ruleone.priority = i + 1;
			}
		}
		
		objArray.sort(sortPriority);
		
		var sortImgSrc = "/globalsight/images/sort-up.gif";
		if(baseFilter.sortOrder == 'asc')
		{
			sortImgSrc = "/globalsight/images/sort-up.gif";
		}
		else
		{
			sortImgSrc = "/globalsight/images/sort-down.gif";
		}
		
		//var checkAllStr = (baseFilter.checkedItemIds.length == baseFilter.getCurrentObjectsSize()) ? " checked " : "";
		var checkAllStr = "";
		str.append("<td width='5%'><input type='checkbox' onclick='baseFilter.checkAll()' id='checkAllBaseFilter'" + checkAllStr + "/></td>");

		if (optionValue == baseFilter.optionInternalTexts)
		{
			str.append("<td width='50%' class='tagName_td'>" + jsInternalContent + "</td>");
			str.append("<td class='tagName_td'>" + jsInternalIsRegex + "</td>");
			str.append("<td class='tagName_td'>" + jsPriority + "</td>");
		}
		else if (optionValue == baseFilter.optionEscapings)
		{
			str.append("<td width='30%' class='tagName_td'>" + jsCharacter + "</td>");
			str.append("<td class='tagName_td'>" + jsEscapeImport + "</td>");
			str.append("<td class='tagName_td'>" + jsEscapeExport + "</td>");
			str.append("<td class='tagName_td'>" + jsPriority + "</td>");
		}
		str.append("</tr>");
		var startIndex = 0;
		startIndex = baseFilter.tagsEveryPage * pageIndex;
		if(startIndex >= objArray.length)
		{
			var maxPage = baseFilter.getPageSize(objArray.length);
			alert(maxPageNums + maxPage);
			return maxPage;
		}
		
		for(var i = 0; i < baseFilter.tagsEveryPage; i++)
		{
			var realIndex = startIndex + i;
			var ruleObj = objArray[realIndex];
			if(ruleObj)
			{
				var backgroundColor = "#C7CEE0";
				if(i % 2 == 0)
				{
					backgroundColor = "#DFE3EE";
				}
				var radioId = baseFilter.getRadioId(ruleObj.itemid);
				var checkedStr = baseFilter.checkedItemIds.contains(radioId) ? " checked " : "";
				
				str.append("<tr style='background-color:"+backgroundColor+";'>");
				str.append("<td><input onclick='baseFilter.checkthis(this)' type='checkbox' id='"+radioId+"'" + checkedStr + "/></td>");
				
				if (optionValue == baseFilter.optionInternalTexts)
				{
					var encodedName = encodeHtmlEntities(ruleObj.aName);
					str.append("<td class='tagValue_td'><a href='#' onclick=\"baseFilter.addTag('" + radioId + "')\">"+encodedName+"</a></td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.isRE?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.priority ? ruleObj.priority : 9) + "</td>");
				}
				else if (optionValue == baseFilter.optionEscapings)
				{
					var encodedName = ruleObj.aName;
					str.append("<td class='tagValue_td'><a href='#' onclick=\"baseFilter.addTag('" + radioId + "')\">"+encodedName+"</a></td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.unEscapeOnImport?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.reEscapeOnExport?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.priority ? ruleObj.priority : 9) + "</td>");
				}
				str.append("</tr>");
			}
		}
	}
	else
	{
		str.append("<td width='5%'><input type='checkbox' id='checkAllBaseFilter'/></td>");
		
		if (optionValue == baseFilter.optionInternalTexts)
		{			
			str.append("<td width='50%' class='tagName_td'>" + jsInternalContent + "</td>");
			str.append("<td class='tagName_td'>" + jsInternalIsRegex + "</td>");
			str.append("<td class='tagName_td'>" + jsPriority + "</td>");
		}
		else if (optionValue == baseFilter.optionEscapings)
		{
			str.append("<td width='30%' class='tagName_td'>" + jsCharacter + "</td>");
			str.append("<td class='tagName_td'>" + jsEscapeImport + "</td>");
			str.append("<td class='tagName_td'>" + jsEscapeExport + "</td>");
			str.append("<td class='tagName_td'>" + jsPriority + "</td>");
		}
		str.append("</tr>");
		str.append("<tr><td colspan='2'><p><br /></p></td></tr>");
	}
	
	return str.toString();
}

BaseFilter.prototype.getItemId = function(radioid)
{
	return radioid.replace("basetags_", "");
}

BaseFilter.prototype.getRadioId = function(itemid)
{
	return "basetags_" + itemid;
}

BaseFilter.prototype.checkthis = function(cobj)
{
	var oid = cobj.id;
	var checkAllObj = document.getElementById("checkAllBaseFilter");
	var itemId = baseFilter.getItemId(oid);
	var ruleObj = baseFilter.getItemById(itemId, baseFilter.currentOption);
	ruleObj.enable = cobj.checked;

	if (!cobj.checked)
	{
		checkAllTagsInBaseFilter = false;
		checkAllObj.checked = false;
		baseFilter.checkedItemIds.removeData(oid);
	}
	else
	{
		if (!baseFilter.checkedItemIds.contains(oid))
		{
			baseFilter.checkedItemIds[baseFilter.checkedItemIds.length] = oid;
		}
		
		if (baseFilter.checkedItemIds.length == baseFilter.getCurrentObjectsSize())
		{
			checkAllObj.checked = true;
		}
	}
}

BaseFilter.prototype.checkAll = function()
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

BaseFilter.prototype.getCurrentObjectsSize = function()
{
	return baseFilter.optionObjsMap[baseFilter.currentOption].length;
}

BaseFilter.prototype.generateAvailableFilterOptions = function(filter, filterIdPara, filterTableNamePara)
{
	var str = new StringBuffer("");
	var _filterConfigurations = filterConfigurations;
	
	if (_filterConfigurations)
	{
	for(var i = 0; i < _filterConfigurations.length; i++)
	{
		var _filter = _filterConfigurations[i];
        if (baseFilter.availablePostFilters.contains(_filter.filterTableName))
        {
        	var _htmlSpecialFilters = _filter.specialFilters;
        	if (_htmlSpecialFilters)
        	{
	        	for (var j = 0; j < _htmlSpecialFilters.length; j++)
	        	{
	        		var _htmlSpecialFilter = _htmlSpecialFilters[j];
	        		var _filterTableName = _htmlSpecialFilter.filterTableName;
	        		var _filterName = _htmlSpecialFilter.filterName;

	        		var _id = _htmlSpecialFilter.id;
	        		var theFiterId = (filter) ? eval("filter." + filterIdPara) : -2;
	        		var theFilterTableName = (filter) ? eval("filter." + filterTableNamePara) : "";
		        		
	        		var selected = ""; 
	        		if (_id == theFiterId && _filterTableName == theFilterTableName)
	        		{
	        			selected = "selected";
	        		}
	        		var id_filterTableName = _id + "-" + _filterTableName;
	        		if (_filterTableName != undefined && _filterName != undefined && _id != undefined)
	        		{
	        		    str.append("<option value='" + id_filterTableName + "' " + selected + ">" + _filterName + "</option>");	
	        		}

	        	}
        	}
        }
	}
	}
	return str.toString();
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
