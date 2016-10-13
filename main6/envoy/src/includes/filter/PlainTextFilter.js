var plaintextFilter = new PlainTextFilter();
var checkAllTagsInPlainTextFilter = false;
var plaintextTagsContentTable = "<table id='plainTextFilterTagsContentTable' border=0 width='400px' class='standardText'>"; 
var fontTagS = "<font class='specialFilter_dialog_label'>";
var fontTagE = "</font>"
var imgYes = "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>";
var checkedId = "";

function PlainTextFilter()
{
	this.filterTableName = "plain_text_filter";
	this.dialogId = "plaintextFilterDialog";
	
	this.optionCustomTextRules = "0";
	this.optionCustomTextRuleSids = "1";
	this.optionNameMap = new Object();
	this.optionNameMap[this.optionCustomTextRules] = jsCustomTextRule;
	this.optionNameMap[this.optionCustomTextRuleSids] = jsCustomSidRule;
	
	this.operatorEqual = "equal";
	this.operatorNotEqual = "not equal";
	this.operatorMatch = "match";	
	this.operatorNameMap = new Object();
	this.operatorNameMap[this.operatorEqual] = jsOperatorEqual;
	this.operatorNameMap[this.operatorNotEqual] = jsOperatorNotEqual;
	this.operatorNameMap[this.operatorMatch] = jsOperatorMatch;
	
	this.optionObjsMap = new Object();
	this.availableOptions = [this.optionCustomTextRules, this.optionCustomTextRuleSids];
	this.checkedItemIds = new Array();
	
	this.tagsEveryPage = 10;
	this.currentPage = 0;
	this.sortOrder = "asc";
	this.sortColumnIndex = 1;
	this.currentPage = 0;
	this.currentOption = this.optionCustomTextRules;
	this.editItemId = -1;
	this.editItemEnable = false;
	this.availablePostFilters = ["html_filter"];
}

PlainTextFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

PlainTextFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

PlainTextFilter.prototype.initOptionMap = function (filter)
{
	plaintextFilter.checkedItemIds = new Array();
	checkAllTagsInPlainTextFilter = false;
	
	if (filter)
	{
		var customTextRules = this.filter.customTextRules;
		plaintextFilter.optionObjsMap[this.optionCustomTextRules] = JSON.parse(customTextRules);
		
		var customTextRuleSids = this.filter.customTextRuleSids;
		plaintextFilter.optionObjsMap[this.optionCustomTextRuleSids] = JSON.parse(customTextRuleSids);
	}
	else
	{
		plaintextFilter.optionObjsMap[this.optionCustomTextRules] = new Array();
		plaintextFilter.optionObjsMap[this.optionCustomTextRuleSids] = new Array();
	}
	
	plaintextFilter.refreshCheckedIds();
}

PlainTextFilter.prototype.refreshCheckedIds = function()
{
	plaintextFilter.checkedItemIds = new Array();
	var ruleObjs = plaintextFilter.optionObjsMap[plaintextFilter.currentOption];
	
	if (ruleObjs && ruleObjs.length > 0)
	{
		for(var i = 0; i<ruleObjs.length; i++)
		{
			var ruleObj = ruleObjs[i];
			if (ruleObj.enable)
			{
				var radioId = plaintextFilter.getRadioId(ruleObj.itemid);
				plaintextFilter.checkedItemIds.appendUniqueObj(radioId);
			}
		}
		
		//plaintextFilter.alertObject(plaintextFilter.checkedItemIds);
	}
}

PlainTextFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	this.initOptionMap(this.filter);
    plaintextFilter.currentPage = 0;
	var str = new StringBuffer("<div style='float:left;width:500px'><table border=0 width='500px'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");	
	str.append("<td><input type='text' style='width:100%' id='plaintextFilterName' maxlength='"+maxFilterNameLength+"' value='" + this.filter.filterName + "'></input>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");	
	str.append("<td><textarea style='width:100%' rows='4' id='plaintextFilterDesc' name='desc'>"+this.filter.filterDescription+"</textarea>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='500px'>");	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName, this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsElementPostFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.generateElementPostFilter(this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	str.append("<div class='specialFilter_dialog_label' style='width:500px'>");
	str.append(this.generateTagsTable(this.filter));
	str.append("</div></div><div style='float:right;width:350px'>");
	
	str.append("<table border=0 width='350px'>");	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' id='td_testDesc'>" + jsCustomTextTest + "</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td><textarea style='width:100%' rows='5' id='plaintextFilterTestSource' name='testSource'></textarea></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td align='center'><input type='button' value='" + jsTest + "' onclick='doTest()'/></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td><textarea style='width:100%' rows='5' id='plaintextFilterTestResult' name='testResult' readonly='readonly'></textarea></td>");
	str.append("</tr>");
	str.append("</table></div>");
	
	var dialogObj = document.getElementById('plaintextFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	plaintextFilter.switchStatus();
	this.showDialog();
	
	savePlainTextFilter.edit = true;
	savePlainTextFilter.filterId = filterId;
	savePlainTextFilter.color = color;
	savePlainTextFilter.filter = this.filter;
	savePlainTextFilter.specialFilters = specialFilters;
	savePlainTextFilter.topFilterId = topFilterId;
}

PlainTextFilter.prototype.generateDiv = function(topFilterId, color)
{
	this.initOptionMap();
	var defaultName = getFilterNameByTableName(this.filterTableName);
	var str = new StringBuffer("<div style='float:left;width:500px'><table border=0 width='500px'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");	
	str.append("<td><input type='text' style='width:100%' id='plaintextFilterName' maxlength='"+maxFilterNameLength+"' value='" + defaultName + "'></input>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");	
	str.append("<td><textarea style='width:100%' rows='4' id='plaintextFilterDesc' name='desc'></textarea>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='500px'>");	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsElementPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.generateElementPostFilter());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	str.append("<div class='specialFilter_dialog_label' style='width:500px'>");
	str.append(this.generateTagsTable());
	str.append("</div></div><div style='float:right;width:350px'>");
	
	str.append("<table border=0 width='350px'>");	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' id='td_testDesc'>" + jsCustomTextTest + "</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td><textarea style='width:100%' rows='5' id='plaintextFilterTestSource' name='testSource'></textarea></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td align='center'><input type='button' value='" + jsTest + "' onclick='doTest()'/></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td><textarea style='width:100%' rows='5' id='plaintextFilterTestResult' name='testResult' readonly='readonly'></textarea></td>");
	str.append("</tr>");
	str.append("</table></div>");
	
	var dialogObj = document.getElementById('plaintextFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	plaintextFilter.switchStatus();
	this.showDialog();
	savePlainTextFilter.edit = false;
	savePlainTextFilter.topFilterId = topFilterId;
	savePlainTextFilter.color = color;
}

function doTest()
{
	document.getElementById("plaintextFilterTestResult").value = "Generate testing result...";
	
	var ruleObjects = plaintextFilter.optionObjsMap[plaintextFilter.optionCustomTextRules];
	var rules = new Array();
	if (ruleObjects && ruleObjects.length > 0)
	{
		for(var i=0; i < ruleObjects.length; i++)
		{
			var ruleObj = ruleObjects[i];
			if (ruleObj.enable)
			{
				rules[rules.length] = ruleObj;
			}
		}
	}
	
	var sidruleObjects = plaintextFilter.optionObjsMap[plaintextFilter.optionCustomTextRuleSids];
	var sidrules = new Array();
	if (sidruleObjects && sidruleObjects.length > 0)
	{
		for(var i=0; i < sidruleObjects.length; i++)
		{
			var ruleObj = sidruleObjects[i];
			if (ruleObj.enable)
			{
				sidrules[sidrules.length] = ruleObj;
			}
		}
	}
	
	var isSid = (plaintextFilter.currentOption == plaintextFilter.optionCustomTextRuleSids);
	
	var source = document.getElementById("plaintextFilterTestSource").value;
	var obj = {customTextRules : JSON.stringify(rules), 
			customTextRuleSids : JSON.stringify(sidrules), 
			source : source, isSid : isSid};
	
	sendAjax(obj, "doTestCustomTextRule", "doTestCustomTextRuleCallback");
}

function doTestCustomTextRuleCallback(data)
{
	document.getElementById("plaintextFilterTestResult").value = data;
}

function savePlainTextFilter()
{
	var validate = new Validate();
	var filterId = savePlainTextFilter.filterId;
	var filterName = document.getElementById("plaintextFilterName").value;
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
    
    var isNew = (savePlainTextFilter.edit) ? "false" : "true";
	var filterDesc = document.getElementById("plaintextFilterDesc").value;
	var baseFilterId = document.getElementById("plain_text_filter_baseFilterSelect").value;
	var customTextRules = JSON.stringify(plaintextFilter.optionObjsMap[plaintextFilter.optionCustomTextRules]);
	var customTextRuleSids = JSON.stringify(plaintextFilter.optionObjsMap[plaintextFilter.optionCustomTextRuleSids]);
	var elementPostFilterIdTable = document.getElementById("elementPostFilter").value;
	var splitedElementPostIdTable = splitByFirstIndex(elementPostFilterIdTable, "-");
	var elementPostFilterId = (splitedElementPostIdTable) ? splitedElementPostIdTable[0] : "-1";
	var elementPostFilter = (splitedElementPostIdTable) ? splitedElementPostIdTable[1] : "-1";
	
	//alertUserBaseFilter(baseFilterId);

	var obj = {
		isNew : isNew,
		filterTableName : "plain_text_filter",
		filterId : filterId,
		filterName : filterName,
		filterDesc : filterDesc,
		filterId : savePlainTextFilter.filterId,
		companyId : companyId,
		baseFilterId : baseFilterId,
		customTextRules : customTextRules,
		customTextRuleSids : customTextRuleSids,
		elementPostFilter : elementPostFilter,
		elementPostFilterId : elementPostFilterId
	};

	// send for check
	sendAjax(obj, "isFilterValid", "isPlainTextFilterValidCallback");
	
	isFilterValidCallback.obj = obj;
}

function isPlainTextFilterValidCallback(data)
{
	if(data == 'true')
	{
		closePopupDialog("plaintextFilterDialog");
		if(savePlainTextFilter.edit)
		{
			sendAjax(isFilterValidCallback.obj, "updatePlainTextFilter", "updatePlainTextFilterCallback");
		}
		else
		{
			sendAjax(isFilterValidCallback.obj, "savePlainTextFilter", "savePlainTextFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("plaintextFilterDialog");
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

function updatePlainTextFilterCallback(data)
{
	var color = savePlainTextFilter.color;
	var filterId = savePlainTextFilter.filterId;
	var filter = savePlainTextFilter.filter;
	var topFilterId = savePlainTextFilter.topFilterId;
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = filterId;
		xrFilter.filterTableName = "plain_text_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.baseFilterId = isFilterValidCallback.obj.baseFilterId;
		xrFilter.customTextRules = isFilterValidCallback.obj.customTextRules;
		xrFilter.customTextRuleSids = isFilterValidCallback.obj.customTextRuleSids;
		xrFilter.elementPostFilter = isFilterValidCallback.obj.elementPostFilter;
		xrFilter.elementPostFilterId = isFilterValidCallback.obj.elementPostFilterId;
		xrFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(savePlainTextFilter.specialFilters, xrFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function savePlainTextFilterCallback(data)
{
	var color = savePlainTextFilter.color;
	var topFilterId = savePlainTextFilter.topFilterId;
	
	var filter = getFilterById(topFilterId);
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = data - 0;
		xrFilter.filterTableName = "plain_text_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.baseFilterId = isFilterValidCallback.obj.baseFilterId;
		xrFilter.customTextRules = isFilterValidCallback.obj.customTextRules;
		xrFilter.customTextRuleSids = isFilterValidCallback.obj.customTextRuleSids;
		xrFilter.elementPostFilter = isFilterValidCallback.obj.elementPostFilter;
		xrFilter.elementPostFilterId = isFilterValidCallback.obj.elementPostFilterId;
		xrFilter.companyId = companyId;
		filter.specialFilters.push(xrFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

PlainTextFilter.prototype.generateElementPostFilter = function (filter)
{
	var str = new StringBuffer("<select id='elementPostFilter' class='xml_filter_select'>");
	str.append("<option value='-1'" + ((filter && filter.elementPostFilter == "-1") ? " selected" : "") + ">" + jsChoose + "</option>");
	
	str.append(this.generateAvailableFilterOptions(filter, "elementPostFilterId", "elementPostFilter"));
	str.append("</select>");
	return str.toString();
}

PlainTextFilter.prototype.generateAvailableFilterOptions = function(filter, filterIdPara, filterTableNamePara)
{
	var str = new StringBuffer("");
	var _filterConfigurations = filterConfigurations;
	
	if (_filterConfigurations)
	{
	for(var i = 0; i < _filterConfigurations.length; i++)
	{
		var _filter = _filterConfigurations[i];
        if (this.availablePostFilters.contains(_filter.filterTableName))
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

PlainTextFilter.prototype.generateTagsTable = function (filter)
{
	var str = new StringBuffer("");
	str.append("<table border=0 width='450px' class='standardText'>");
	str.append("<tr><td>");
	str.append("<div id='plaintextFilterRulesContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='plaintextFilterRulesSection' onchange='plaintextFilter.switchRules(this)'>");
	for(var i = 0; i < plaintextFilter.availableOptions.length; i++)
	{
		var optionV = plaintextFilter.availableOptions[i];
		str.append("<option value='" + optionV + "'");
		str.append(optionV == plaintextFilter.currentOption ? " selected " : "");
		str.append(">" + this.optionNameMap[optionV] + "</option>");
	}
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "...' onclick='plaintextFilter.addTag()'></input>");
	str.append("<input type='button' value='" + jsDelete + "...' onclick='plaintextFilter.deleteTag()' id='cmdDeleteTag'></input>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	str.append("<div id='plaintextTagsContent'>");
	str.append(plaintextTagsContentTable);
	
	if (filter)
	{
		str.append(plaintextFilter.generateTagsContent(plaintextFilter.currentOption, 0));
	}
	else
	{
		str.append(plaintextFilter.generateTagsContent(plaintextFilter.currentOption));
	}
	
	str.append("</table>");
	str.append("</div>");
	str.append("<div>");
	
	var pageTotalSize = filter ? plaintextFilter.getPageSize(plaintextFilter.optionObjsMap[plaintextFilter.currentOption].length) : 1;
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=plaintextFilter.prePage()>" + jsPrevious + "</a>");
	str.append("|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=plaintextFilter.nextPage()>" + jsNext + "</a>");
	str.append("<input id='pageCountPlainTextFilter' size=2 type='text' value="+((filter) ? plaintextFilter.currentPage+1 : 1)+" ></input>");
	str.append(" / <span id='pageTotalSizePlainTextFilter' class='standardText'>" + pageTotalSize + "</span> ");
	str.append("<input type='button' value='" + jsGo + "' onclick=plaintextFilter.goToPage()></input>");
	str.append("</div>");
	
	return str.toString();
}

PlainTextFilter.prototype.closeAllTagPopup = function()
{
	closePopupDialog("plainTextFilter_CustomTextRule_Dialog");
	closePopupDialog("editPriorityPlainTextDialog");
	closePopupDialog("deletePlainTextTagDialog");
	closePopupDialog("plainTextFilter_CustomSidRule_Dialog");
}

PlainTextFilter.prototype.addTag = function(radioId)
{
	var dialogId = "plainTextFilter_CustomTextRule_Dialog";
	var isEdit = (radioId) ? true : false;
	var useCustomTextDiv = (plaintextFilter.currentOption == plaintextFilter.optionCustomTextRules);
	var useCustomSidDiv = (plaintextFilter.currentOption == plaintextFilter.optionCustomTextRuleSids);
	
	plaintextFilter.closeAllTagPopup();
	
	if (useCustomTextDiv)
	{
		dialogId = "plainTextFilter_CustomTextRule_Dialog";
	}
	
	if (useCustomSidDiv)
	{
		dialogId = "plainTextFilter_CustomSidRule_Dialog";
	}
	
	showPopupDialog(dialogId);
	
	var editId = isEdit ? plaintextFilter.getItemId(radioId) : -1;
	var editItem = isEdit ? plaintextFilter.getItemById(editId, plaintextFilter.currentOption) : new Object();
	plaintextFilter.editItemId = (isEdit? editItem.itemid : plaintextFilter.newItemId());
	plaintextFilter.editItemEnable = (isEdit? editItem.enable : false);
	
	if (useCustomTextDiv)
	{
		var startString = isEdit ? editItem.startString : "";
		var startIsRegEx = isEdit ? editItem.startIsRegEx : false;
		var startOccurrence = isEdit ? editItem.startOccurrence : "FIRST";
		var finishString = isEdit ? editItem.finishString : "";
		var finishIsRegEx = isEdit ? editItem.finishIsRegEx : false;
		var finishOccurrence = isEdit ? editItem.finishOccurrence : "FIRST";
		var isMultiline = isEdit ? editItem.isMultiline : false;
		
		var occid = "1";
		var occtimes = "";
		if ("FIRST" == finishOccurrence)
		{
			occid = "1";
		} else if ("LAST" == finishOccurrence)
		{
			occid = "2";
		}
		else
		{
			occid = "3";
			occtimes = finishOccurrence;
		}
		document.getElementById("plainTextFilter_customTextRule_finishStr").value = finishString;
		document.getElementById("plainTextFilter_customTextRule_finishIs").checked = finishIsRegEx;
		document.getElementById("plainTextFilter_customTextRule_finishOcc" + occid).checked = true;
		document.getElementById("plainTextFilter_customTextRule_finishOccTimes").value = occtimes;
		
		occid = "1";
		occtimes = "";
		if ("FIRST" == startOccurrence)
		{
			occid = "1";
		} else if ("LAST" == startOccurrence)
		{
			occid = "2";
		}
		else
		{
			occid = "3";
			occtimes = startOccurrence;
		}
		document.getElementById("plainTextFilter_customTextRule_startStr").value = startString;
		document.getElementById("plainTextFilter_customTextRule_startIs").checked = startIsRegEx;
		document.getElementById("plainTextFilter_customTextRule_startOcc" + occid).checked = true;
		document.getElementById("plainTextFilter_customTextRule_startOccTimes").value = occtimes;
		
		document.getElementById("plainTextFilter_customTextRule_priority").value = isEdit ? editItem.priority : "";
		document.getElementById("plainTextFilter_customTextRule_isMultiline").checked = isMultiline;
	}
	
	if (useCustomSidDiv)
	{
		var startString = isEdit ? editItem.startString : "";
		var startIsRegEx = isEdit ? editItem.startIsRegEx : false;
		var startOccurrence = isEdit ? editItem.startOccurrence : "FIRST";
		var finishString = isEdit ? editItem.finishString : "";
		var finishIsRegEx = isEdit ? editItem.finishIsRegEx : false;
		var finishOccurrence = isEdit ? editItem.finishOccurrence : "FIRST";
		
		var occid = "1";
		var occtimes = "";
		if ("FIRST" == finishOccurrence)
		{
			occid = "1";
		} else if ("LAST" == finishOccurrence)
		{
			occid = "2";
		}
		else
		{
			occid = "3";
			occtimes = finishOccurrence;
		}
		document.getElementById("plainTextFilter_customSidRule_finishStr").value = finishString;
		document.getElementById("plainTextFilter_customSidRule_finishIs").checked = finishIsRegEx;
		document.getElementById("plainTextFilter_customSidRule_finishOcc" + occid).checked = true;
		document.getElementById("plainTextFilter_customSidRule_finishOccTimes").value = occtimes;
		
		occid = "1";
		occtimes = "";
		if ("FIRST" == startOccurrence)
		{
			occid = "1";
		} else if ("LAST" == startOccurrence)
		{
			occid = "2";
		}
		else
		{
			occid = "3";
			occtimes = startOccurrence;
		}
		document.getElementById("plainTextFilter_customSidRule_startStr").value = startString;
		document.getElementById("plainTextFilter_customSidRule_startIs").checked = startIsRegEx;
		document.getElementById("plainTextFilter_customSidRule_startOcc" + occid).checked = true;
		document.getElementById("plainTextFilter_customSidRule_startOccTimes").value = occtimes;
	}
}

PlainTextFilter.prototype.cloneObject = function(oriObj)
{
	var txt = JSON.stringify(oriObj);
	return JSON.parse(txt);
}

PlainTextFilter.prototype.alertObject = function(obj)
{
	var txt = JSON.stringify(obj);
	return alert(txt);
}

PlainTextFilter.prototype.getItemById = function(itemId, optionValue)
{
	var item;
	var items = plaintextFilter.optionObjsMap[optionValue];
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

PlainTextFilter.prototype.saveCustomSidRule = function()
{
	var itemId = plaintextFilter.editItemId;
	var validate = new Validate();
	var startString = new StringBuffer(document.getElementById("plainTextFilter_customSidRule_startStr").value);
	var startIsRegEx = document.getElementById("plainTextFilter_customSidRule_startIs").checked;
	var startOccurrence = document.getElementById("plainTextFilter_customSidRule_startOcc1").checked ? 
			"FIRST" : (document.getElementById("plainTextFilter_customSidRule_startOcc2").checked ? 
					"LAST" : document.getElementById("plainTextFilter_customSidRule_startOccTimes").value);
	
	var finishString = new StringBuffer(document.getElementById("plainTextFilter_customSidRule_finishStr").value);
	var finishIsRegEx = document.getElementById("plainTextFilter_customSidRule_finishIs").checked;
	var finishOccurrence = document.getElementById("plainTextFilter_customSidRule_finishOcc1").checked ? 
			"FIRST" : (document.getElementById("plainTextFilter_customSidRule_finishOcc2").checked ? 
					"LAST" : document.getElementById("plainTextFilter_customSidRule_finishOccTimes").value);

	if(validate.isEmptyStr(startString))
	{
		document.getElementById("plainTextFilter_customSidRule_startStr").value = "";
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
	
	if(this.isCustomSidRuleExists(itemId, startString, finishString))
	{
		alert(jsCustomTextRuleExist);
		return;
	}
	
	var isFinishEmpty = validate.isEmptyStr(finishString);
	var dialogId = "plainTextFilter_CustomSidRule_Dialog";
	
	var enable = plaintextFilter.editItemEnable;
	var item = {itemid : itemId, enable : enable, startString : startString.toString(), startIsRegEx : startIsRegEx, startOccurrence : startOccurrence.toString(),
			finishString : (isFinishEmpty ? "" : finishString.toString()), finishIsRegEx : finishIsRegEx, finishOccurrence : finishOccurrence.toString()};
	
	plaintextFilter.addOneItemInCurrentOptions(item);
	plaintextFilter.closeTagDialog(dialogId);
}

PlainTextFilter.prototype.isCustomSidRuleExists = function(id, startStr, finishStr)
{
	var items = this.optionObjsMap[this.optionCustomTextRuleSids];
	for (var i = 0; i < items.length; i++)
	{
		var item = items[i];
		if (item.itemid != id && item.startString == startStr && item.finishString == finishStr)
		{
			return true;
		}
	}
	
	return false;
}

PlainTextFilter.prototype.saveCustomTextRule = function()
{
	var itemId = plaintextFilter.editItemId;
	var validate = new Validate();
	var startString = new StringBuffer(document.getElementById("plainTextFilter_customTextRule_startStr").value);
	var startIsRegEx = document.getElementById("plainTextFilter_customTextRule_startIs").checked;
	var startOccurrence = document.getElementById("plainTextFilter_customTextRule_startOcc1").checked ? 
			"FIRST" : (document.getElementById("plainTextFilter_customTextRule_startOcc2").checked ? 
					"LAST" : document.getElementById("plainTextFilter_customTextRule_startOccTimes").value);
	
	var finishString = new StringBuffer(document.getElementById("plainTextFilter_customTextRule_finishStr").value);
	var finishIsRegEx = document.getElementById("plainTextFilter_customTextRule_finishIs").checked;
	var finishOccurrence = document.getElementById("plainTextFilter_customTextRule_finishOcc1").checked ? 
			"FIRST" : (document.getElementById("plainTextFilter_customTextRule_finishOcc2").checked ? 
					"LAST" : document.getElementById("plainTextFilter_customTextRule_finishOccTimes").value);
	
	var isMultiline = document.getElementById("plainTextFilter_customTextRule_isMultiline").checked;
	
	var priority = document.getElementById("plainTextFilter_customTextRule_priority").value;

	if(validate.isEmptyStr(startString))
	{
		document.getElementById("plainTextFilter_customTextRule_startStr").value = "";
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
	
	if (!this.validatePriority(priority, itemId))
	{
		return;
	}
	
	if(this.isCustomTextRuleExists(itemId, startString, finishString, priority))
	{
		alert(jsCustomTextRuleExist);
		return;
	}
	
	var isFinishEmpty = validate.isEmptyStr(finishString);
	var dialogId = "plainTextFilter_CustomTextRule_Dialog";
	
	var enable = plaintextFilter.editItemEnable;
	var item = {itemid : itemId, enable : enable, startString : startString.toString(), startIsRegEx : startIsRegEx, startOccurrence : startOccurrence.toString(),
			finishString : (isFinishEmpty ? "" : finishString.toString()), finishIsRegEx : finishIsRegEx, finishOccurrence : finishOccurrence.toString(), 
			priority : priority, isMultiline : isMultiline};
	
	plaintextFilter.addOneItemInCurrentOptions(item);
	plaintextFilter.closeTagDialog(dialogId);
}

PlainTextFilter.prototype.isCustomTextRuleExists = function(id, startStr, finishStr, priority)
{
	var items = this.optionObjsMap[this.optionCustomTextRules];
	for (var i = 0; i < items.length; i++)
	{
		var item = items[i];
		if (item.itemid != id && item.startString == startStr && item.finishString == finishStr)
		{
			return true;
		}
	}
	
	return false;
}

PlainTextFilter.prototype.addOneItemInCurrentOptions = function(item)
{
	if (plaintextFilter.isDefined(item))
	{
		var i = 0;
		var objArray = plaintextFilter.optionObjsMap[plaintextFilter.currentOption];
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
			checkAllTagsInPlainTextFilter = false;
		}
		
		var maxPages = plaintextFilter.getPageSize(objArray.length);
		document.getElementById("pageTotalSizePlainTextFilter").innerText = maxPages;
		
		var content = plaintextFilter.generateTagsContent(plaintextFilter.currentOption, plaintextFilter.currentPage);
		plaintextFilter.refreshTagsContent(content);
	}
}

PlainTextFilter.prototype.closeTagDialog = function(dialogId)
{
	closePopupDialog(dialogId);	
	plaintextFilter.editItemId = -1;
}

PlainTextFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = plaintextFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deletePlainTextTagDialog");
	}
}

PlainTextFilter.prototype.deleteCheckedTags = function()
{
	for(var i = 0; i < plaintextFilter.availableOptions.length; i++)
    {
		var aoption = plaintextFilter.availableOptions[i];
		var ruleObjects = plaintextFilter.optionObjsMap[aoption];
        var checkBoxId = "delete_tags_" + aoption;
        var checkBoxObjs = document.getElementsByName(checkBoxId);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
        	var checkBoxObj = checkBoxObjs[j];
            if (checkBoxObj.checked)
            {
            	var itemId = checkBoxObj.getAttribute("tagValue");
            	var ruleObj = plaintextFilter.getItemById(itemId, aoption);
            	
            	if (ruleObj && ruleObjects)
            	{
            		ruleObjects.removeData(ruleObj);
            	}
            }
        }
    }
	
	plaintextFilter.setPageValue();
	
	closePopupDialog("deletePlainTextTagDialog");
	plaintextFilter.switchRules(document.getElementById("plaintextFilterRulesSection"));
}

//for gbs-2599
PlainTextFilter.prototype.selectAll_PlainTextFilter = function()
{
	var selectAll = document.getElementById("selectAll_PlainTextFilter")
	if(selectAll.checked) {
		this.checkAllTagsToDelete();
	} else {
		this.clearAllTagsToDelete();
	}
}

PlainTextFilter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsTagType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_PlainTextFilter' onclick='plaintextFilter.selectAll_PlainTextFilter()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsTagsToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsTagsCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	var tagTypes = document.getElementById("plaintextFilterRulesSection");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var ruleObjects = plaintextFilter.optionObjsMap[value];
		var checkboxId = "delete_tags_" + value;
		var doEncodeName = false;
		
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
					var aaaname = ruleObject.startString;
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
	str.append("<a href='#' class='specialfilter_a' onclick='plaintextFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='plaintextFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deletePlainTextTagsDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deletePlainTextTagTableContent").innerHTML = str.toString();
	return true;
}

PlainTextFilter.prototype.generatePriorityTagTableContent = function()
{
	checkedId = "";
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='530px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='100px'>");
	str.append("<Label class='tagName_td'>" + jsCurrentPriority + "</Label>");
	str.append("</td>");
	str.append("<td width='150px'>");
	str.append("<Label class='tagName_td'>" + jsStartString + "</Label>");
	str.append("</td>");
	str.append("<td width='150px'>");
	str.append("<Label class='tagName_td'>" + jsFinishString + "</Label>");
	str.append("</td>");
	str.append("<td width='130px'>");
	str.append("<Label class='tagName_td'>" + jsNewPriority + "</Label>");
	str.append("</td>");
	str.append("</tr>");
	
	var ruleObjects = plaintextFilter.optionObjsMap[plaintextFilter.currentOption];
	var inputIdPre = "tag_priority_";
	var doEncodeName = false;
	var isOdd = true;
	
	for(var j = 0; j < ruleObjects.length; j++)
	{
		var backColor = isOdd ? "#DFE3EE" : "#C7CEE0";
		isOdd = !isOdd;
				
		var ruleObject = ruleObjects[j];
		var aaaname = ruleObject.startString;
		var encodedName = doEncodeName ? encodeHtmlEntities(aaaname) : aaaname;
		str.append("<tr style='background-color:"+backColor+";padding:4px'>");
		str.append("<td>");
		str.append(ruleObject.priority);
		str.append("</td>");
		str.append("<td>");
		str.append(encodedName);
		str.append("</td>");
		str.append("<td>");
		str.append(doEncodeName ? encodeHtmlEntities(ruleObject.finishString) : ruleObject.finishString);
		str.append("</td>");
		str.append("<td>");
		var iddd = inputIdPre + ruleObject.itemid;
		//str.append("<input type='text' name='"+iddd+"' id='"+iddd+"' value='"+ruleObject.priority+"' onblur='plaintextFilter.checkPriorities()'></input>");
		str.append("<input type='text' name='"+iddd+"' id='"+iddd+"' value='"+ruleObject.priority+"'></input>");
		str.append("</td>");
		str.append("</tr>");
	}
	str.append("</table></center>");
	
	document.getElementById("editPriorityPlainTextTableContent").innerHTML = str.toString();
	showPopupDialog("editPriorityPlainTextDialog");
}

PlainTextFilter.prototype.checkPriorities = function()
{
	var ruleObjects = plaintextFilter.optionObjsMap[plaintextFilter.currentOption];
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
			alert(jsAlertPriorityValue + " - " + ruleObject.startString);
			document.getElementById(iddd).focus();
			return;
		}
		
		valuesArray[j] = newpriority;
	}
}

PlainTextFilter.prototype.validatePriority = function(newpriority, eid)
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

PlainTextFilter.prototype.savePriorities = function()
{
	var ruleObjects = plaintextFilter.optionObjsMap[plaintextFilter.currentOption];
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
	
	closePopupDialog("editPriorityPlainTextDialog");
	var content = plaintextFilter.generateTagsContent(plaintextFilter.currentOption, plaintextFilter.currentPage);
	plaintextFilter.refreshTagsContent(content);
}

PlainTextFilter.prototype.isDefined = function(objj)
{
	return (typeof(objj) != 'undefined');
}

PlainTextFilter.prototype.setCheckOrClearAll = function( /*boolean*/checkOrClear )
{
    for(var i = 0; i < plaintextFilter.availableOptions.length; i++)
    {
        var checkBoxId = "delete_tags_" + plaintextFilter.availableOptions[i];
        var checkBoxObjs = document.getElementsByName(checkBoxId);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
            checkBoxObjs[j].checked = checkOrClear;
        }
    }
}

PlainTextFilter.prototype.checkAllTagsToDelete = function ()
{
	plaintextFilter.setCheckOrClearAll(true);
}

PlainTextFilter.prototype.clearAllTagsToDelete = function()
{
	plaintextFilter.setCheckOrClearAll(false);
}

PlainTextFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	//var newid = (new Date()).getTime() + "" + Math.round(Math.random()*100);
	return newid;
}

PlainTextFilter.prototype.setPageValue = function()
{
	var curOption = plaintextFilter.currentOption;
	var maxPages = plaintextFilter.getPageSize(plaintextFilter.optionObjsMap[curOption].length);
	plaintextFilter.currentPage = (plaintextFilter.currentPage >= maxPages) ? maxPages - 1 : plaintextFilter.currentPage;
	document.getElementById("pageTotalSizePlainTextFilter").innerHTML = maxPages;
	
	document.getElementById("pageCountPlainTextFilter").value = plaintextFilter.currentPage + 1;
}

PlainTextFilter.prototype.prePage = function()
{
    if(plaintextFilter.currentPage > 0)
	{
    	plaintextFilter.currentPage --;
    	var content = plaintextFilter.generateTagsContent(plaintextFilter.currentOption, plaintextFilter.currentPage);
		plaintextFilter.refreshTagsContent(content);
    	plaintextFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

PlainTextFilter.prototype.nextPage = function()
{
	var arraysize = plaintextFilter.getCurrentObjectsSize();
	if(plaintextFilter.currentPage < plaintextFilter.getPageSize(arraysize) - 1)
	{
		plaintextFilter.currentPage ++;
		var content = plaintextFilter.generateTagsContent(plaintextFilter.currentOption, plaintextFilter.currentPage);
		plaintextFilter.refreshTagsContent(content);
		plaintextFilter.setPageValue();
	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

PlainTextFilter.prototype.getPageSize = function(itemsTotalCount)
{
	var countPerPage = plaintextFilter.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

PlainTextFilter.prototype.goToPage = function()
{
	var pageValue = document.getElementById("pageCountPlainTextFilter").value;
	var validate = new Validate();
	if(!validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	var arraysize = plaintextFilter.getCurrentObjectsSize();
	
	if(pageValue < 1 || pageValue > plaintextFilter.getPageSize(arraysize))
	{
		alert(invalidatePageValue.replace("%s", plaintextFilter.getPageSize(arraysize)));
		plaintextFilter.setPageValue();
		return;
	}

	plaintextFilter.currentPage = pageValue - 1;
	var content = plaintextFilter.generateTagsContent(plaintextFilter.currentOption, plaintextFilter.currentPage);
	plaintextFilter.refreshTagsContent(content);
}

PlainTextFilter.prototype.switchRules = function(plaintextFilterRulesSection)
{
	plaintextFilter.currentOption = plaintextFilterRulesSection.options[plaintextFilterRulesSection.selectedIndex].value;
	plaintextFilter.currentPage = 0;
	
	plaintextFilter.closeAllTagPopup();
	plaintextFilter.refreshCheckedIds();
	var content = plaintextFilter.generateTagsContent(plaintextFilter.currentOption, plaintextFilter.currentPage);
	plaintextFilter.refreshTagsContent(content);
	var checkAllEle = document.getElementById("checkAllPlainTextFilter");
	checkAllEle.checked = false;
	plaintextFilter.setPageValue();
	
	plaintextFilter.switchStatus();
}

PlainTextFilter.prototype.switchStatus = function()
{
	if (plaintextFilter.currentOption == plaintextFilter.optionCustomTextRuleSids)
	{
		//checkAllEle.disabled = "disabled";
		document.getElementById("plaintextFilter_editPriority").disabled = "disabled";
		document.getElementById("td_testDesc").innerText = "Input some Text to test Custom SID Rule";
	}
	else
	{
		//checkAllEle.disabled = "";
		document.getElementById("plaintextFilter_editPriority").disabled = "";
		document.getElementById("td_testDesc").innerText = jsCustomTextTest;
	}
	
}

PlainTextFilter.prototype.refreshTagsContent = function(content)
{
	var ccc = new StringBuffer(plaintextTagsContentTable);
	ccc.append(content);
	ccc.append("</table>");
	
	document.getElementById("plaintextTagsContent").innerHTML = ccc.toString();
}

PlainTextFilter.prototype.generateTagsContent = function(optionValue, pageIndex)
{
	// var objArray = plaintextFilter.parserXml(optionValue, ruleXml);
	var objArray = plaintextFilter.optionObjsMap[optionValue];
	var str = new StringBuffer("");
	str.append("<tr class='htmlFilter_emptyTable_tr'>");
	
	if (objArray && objArray.length > 0)
	{
		if (optionValue != plaintextFilter.optionCustomTextRuleSids)
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
		}
		
		var sortImgSrc = "/globalsight/images/sort-up.gif";
		if(plaintextFilter.sortOrder == 'asc')
		{
			sortImgSrc = "/globalsight/images/sort-up.gif";
		}
		else
		{
			sortImgSrc = "/globalsight/images/sort-down.gif";
		}
		
		//var checkAllStr = (plaintextFilter.checkedItemIds.length == plaintextFilter.getCurrentObjectsSize()) ? " checked " : "";
		var checkAllStr = (optionValue == plaintextFilter.optionCustomTextRuleSids) ? "disabled='disabled'" : "";
		str.append("<td width='5%'><input type='checkbox' onclick='plaintextFilter.checkAll()' id='checkAllPlainTextFilter'" + checkAllStr + "/></td>");

		if (optionValue == plaintextFilter.optionCustomTextRules)
		{
			str.append("<td width='13%' class='tagName_td'>" + jsStartString + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsStartIs + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsStartOcc + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsFinishString + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsFinishIs + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsFinishOcc + "</td>");
			str.append("<td width='12%' class='tagName_td'>" + jsMultiline + "</td>");
			str.append("<td class='tagName_td'>" + jsPriority + "</td>");
		}
		if (optionValue == plaintextFilter.optionCustomTextRuleSids)
		{
			str.append("<td width='20%' class='tagName_td'>" + jsStartString + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsStartIs + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsStartOcc + "</td>");
			str.append("<td width='20%' class='tagName_td'>" + jsFinishString + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsFinishIs + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsFinishOcc + "</td>");
		}
		str.append("</tr>");
		var startIndex = 0;
		startIndex = plaintextFilter.tagsEveryPage * pageIndex;
		if(startIndex >= objArray.length)
		{
			var maxPage = plaintextFilter.getPageSize(objArray.length);
			alert(maxPageNums + maxPage);
			return maxPage;
		}
		
		for(var i = 0; i < plaintextFilter.tagsEveryPage; i++)
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
				var radioId = plaintextFilter.getRadioId(ruleObj.itemid);
				var checkedStr = plaintextFilter.checkedItemIds.contains(radioId) ? " checked " : "";
				
				str.append("<tr style='background-color:"+backgroundColor+";'>");
				str.append("<td><input onclick='plaintextFilter.checkthis(this)' type='checkbox' id='"+radioId+"'" + checkedStr + "/></td>");
				
				if (optionValue == plaintextFilter.optionCustomTextRules)
				{
					var encodedName = ruleObj.startString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;");
					str.append("<td class='tagValue_td'><a href='#' onclick=\"plaintextFilter.addTag('" + radioId + "')\">"+encodedName+"</a></td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.startIsRegEx?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + ruleObj.startOccurrence + "</td>");
					str.append("<td class='tagValue_td'>" + ruleObj.finishString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;"));
					str.append("<td class='tagValue_td'>" + (ruleObj.finishIsRegEx?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + ruleObj.finishOccurrence + "</td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.isMultiline?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.priority ? ruleObj.priority : 9) + "</td>");
				}
				if (optionValue == plaintextFilter.optionCustomTextRuleSids)
				{
					var encodedName = ruleObj.startString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;");
					str.append("<td class='tagValue_td'><a href='#' onclick=\"plaintextFilter.addTag('" + radioId + "')\">"+encodedName+"</a></td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.startIsRegEx?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + ruleObj.startOccurrence + "</td>");
					str.append("<td class='tagValue_td'>" + ruleObj.finishString.toString().replace(/\</g,"&lt;").replace(/\>/g, "&gt;"));
					str.append("<td class='tagValue_td'>" + (ruleObj.finishIsRegEx?imgYes:"") + "</td>");
					str.append("<td class='tagValue_td'>" + ruleObj.finishOccurrence + "</td>");
				}
				str.append("</tr>");
			}
		}
	}
	else
	{
		str.append("<td width='5%'><input type='checkbox' id='checkAllPlainTextFilter'/></td>");
		
		if (optionValue == plaintextFilter.optionCustomTextRules)
		{
			str.append("<td width='13%' class='tagName_td'>" + jsStartString + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsStartIs + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsStartOcc + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsFinishString + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsFinishIs + "</td>");
			str.append("<td width='13%' class='tagName_td'>" + jsFinishOcc + "</td>");
			str.append("<td width='12%' class='tagName_td'>" + jsMultiline + "</td>");
			str.append("<td class='tagName_td'>" + jsPriority + "</td>");
		}
		if (optionValue == plaintextFilter.optionCustomTextRuleSids)
		{
			str.append("<td width='20%' class='tagName_td'>" + jsStartString + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsStartIs + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsStartOcc + "</td>");
			str.append("<td width='20%' class='tagName_td'>" + jsFinishString + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsFinishIs + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsFinishOcc + "</td>");
		}
		str.append("</tr>");
		str.append("<tr><td colspan='2'><p><br /></p></td></tr>");
	}
	
	return str.toString();
}

PlainTextFilter.prototype.getItemId = function(radioid)
{
	return radioid.replace("basetags_", "");
}

PlainTextFilter.prototype.getRadioId = function(itemid)
{
	return "basetags_" + itemid;
}

PlainTextFilter.prototype.checkthis = function(cobj)
{
	var oid = cobj.id;
	var checkAllObj = document.getElementById("checkAllPlainTextFilter");
	var itemId = plaintextFilter.getItemId(oid);
	var ruleObj = plaintextFilter.getItemById(itemId, plaintextFilter.currentOption);
	
	// only one rule can be enabled for custom sid rule
	if (plaintextFilter.currentOption == plaintextFilter.optionCustomTextRuleSids && cobj.checked)
	{
		var objArray = plaintextFilter.optionObjsMap[plaintextFilter.optionCustomTextRuleSids];
		if (objArray && objArray.length > 0)
		{
			for(var i = 0; i < objArray.length; i++)
			{
				var ruleone = objArray[i];
				
				if (ruleone.enable)
				{
					alert("Only one custom SID rule can be enabled!");
					cobj.checked = false;
					return;
				}
			}
		}
	}
	
	ruleObj.enable = cobj.checked;

	if (!cobj.checked)
	{
		checkAllTagsInPlainTextFilter = false;
		checkAllObj.checked = false;
		plaintextFilter.checkedItemIds.removeData(oid);
	}
	else
	{
		if (!plaintextFilter.checkedItemIds.contains(oid))
		{
			plaintextFilter.checkedItemIds[plaintextFilter.checkedItemIds.length] = oid;
		}
		
		if (plaintextFilter.checkedItemIds.length == plaintextFilter.getCurrentObjectsSize())
		{
			checkAllObj.checked = true;
		}
	}
}

PlainTextFilter.prototype.checkAll = function()
{
	var checkAllObj = document.getElementById("checkAllPlainTextFilter");
	checkAllTagsInPlainTextFilter = checkAllObj.checked;
	for(var i = 0; i < plaintextFilter.getCurrentObjectsSize(); i++)
	{
		var ruleObj = plaintextFilter.optionObjsMap[plaintextFilter.currentOption][i];
		var oid = plaintextFilter.getRadioId(ruleObj.itemid);
		
		if(document.getElementById(oid))
		{
			document.getElementById(oid).checked = checkAllTagsInPlainTextFilter;	
		}
		
		if (checkAllTagsInPlainTextFilter)
		{
			plaintextFilter.checkedItemIds.appendUniqueObj(oid);
		}
		
		ruleObj.enable = checkAllTagsInPlainTextFilter;
	}
	
	if (!checkAllTagsInPlainTextFilter)
	{
		plaintextFilter.checkedItemIds = new Array();
	}
}

PlainTextFilter.prototype.getCurrentObjectsSize = function()
{
	return plaintextFilter.optionObjsMap[plaintextFilter.currentOption].length;
}

PlainTextFilter.prototype.generateAvailableFilterOptions = function(filter, filterIdPara, filterTableNamePara)
{
	var str = new StringBuffer("");
	var _filterConfigurations = filterConfigurations;
	
	if (_filterConfigurations)
	{
	for(var i = 0; i < _filterConfigurations.length; i++)
	{
		var _filter = _filterConfigurations[i];
        if (plaintextFilter.availablePostFilters.contains(_filter.filterTableName))
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


