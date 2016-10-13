var qaFilter = new QAFilter();
var checkAllRulesInQAFilter = false;
var rulesContentTable = "<table id='qaRuleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%'>";
var exceptionsContentTable = "<table id='exceptionContentTable' cellpadding=0 cellspacing=0 border=0 width='100%'>";
var imgYes = "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>";
	
function QAFilter()
{
	this.filterTableName = "qa_filter";
	this.dialogId = "qaFilterDialog";
	
	this.optionQARules = "0";
	this.optionObjsMap = new Object();
	this.optionDefaultObjsMap = new Object();
	this.optionRuleExceptionsMap = new Object();
	this.checkedItemIds = new Array();
	this.rulesPerPage = 10;
	this.exceptionsPerPage = 10;
	this.currentPage = 0;
	this.currentOption = this.optionQARules;
	this.editRuleId = -1;
	this.editExceptionId = -1;
	this.editRuleEnable = false;
	this.editRuleExceptions = new Array();
}

QAFilter.prototype.setFilter = function(filter)
{
	this.filter = filter;
}

QAFilter.prototype.showDialog = function()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

QAFilter.prototype.initOptionMap = function(filter)
{
	checkAllRulesInQAFilter = false;
	if (filter)
	{
		var rules = this.filter.rules;
		var defaultRules = this.filter.defaultRules;
		qaFilter.optionObjsMap[this.optionQARules] = JSON.parse(rules);
		qaFilter.optionDefaultObjsMap[this.optionQARules] = JSON.parse(defaultRules);
	}
	else
	{
		qaFilter.optionObjsMap[this.optionQARules] = new Array();
		qaFilter.initDefaultOptionMap();
	}
	
	qaFilter.refreshCheckedIds();
}

QAFilter.prototype.initDefaultOptionMap = function()
{
	qaFilter.optionDefaultObjsMap[this.optionQARules] = new Array();
	
	var ruleId = qaFilter.newItemId();
	var enable = false;
	var check = jsQAFilterRuleSourceEqualTarget;
	
	var item1 = {ruleId : ruleId, enable : enable, check : check};
	
	qaFilter.optionDefaultObjsMap[this.optionQARules].appendUniqueObj(item1);
	
	var targetExpansion = "20";
	check = jsQAFilterRuleTargetExpansion.replace("%s", targetExpansion);
	ruleId = qaFilter.newItemId() - 1000;
	
	var item2 = {ruleId : ruleId, enable : enable, check : check, targetExpansion : targetExpansion};
	
	qaFilter.optionDefaultObjsMap[this.optionQARules].appendUniqueObj(item2);
}

QAFilter.prototype.refreshCheckedIds = function()
{
	qaFilter.checkedItemIds = new Array();
	var ruleObjs = qaFilter.optionObjsMap[qaFilter.currentOption];
	var defaultRuleObjs = qaFilter.optionDefaultObjsMap[qaFilter.currentOption];
	
	if (ruleObjs && ruleObjs.length > 0)
	{
		for(var i = 0; i < ruleObjs.length; i++)
		{
			var ruleObj = ruleObjs[i];
			if (ruleObj.enable)
			{
				var radioId = qaFilter.getRadioId(ruleObj.ruleId);
				qaFilter.checkedItemIds.appendUniqueObj(radioId);
			}
		}
	}
	if (defaultRuleObjs && defaultRuleObjs.length > 0)
	{
		for(var i = 0; i < defaultRuleObjs.length; i++)
		{
			var ruleObj = defaultRuleObjs[i];
			if (ruleObj.enable)
			{
				var radioId = qaFilter.getRadioId(ruleObj.ruleId);
				qaFilter.checkedItemIds.appendUniqueObj(radioId);
			}
		}
	}
}

QAFilter.prototype.generateEmptyTable = function()
{
	var str = new StringBuffer("");
	
	str.append("<table>");
	str.append("<tr>");
	str.append("<td>");
	str.append("<br /><br />");
	str.append("</td>");
	str.append("</tr>");
	str.append("</table>");
	
	return str.toString();
}

QAFilter.prototype.generateEmptyRows = function()
{
	var str = new StringBuffer("");
	
	str.append("<tr>");
	str.append("<td>");
	str.append("<br /><br />");
	str.append("</td>");
	str.append("</tr>");
	
	return str.toString();
}

QAFilter.prototype.generateDefaultRulesTable = function(optionValue)
{
	var str = new StringBuffer("");
	
	var objArrayDefault = qaFilter.optionDefaultObjsMap[optionValue];
	if (objArrayDefault && objArrayDefault.length > 0)
	{
		for(var i = 0; i < objArrayDefault.length; i++)
		{
			var ruleObj = objArrayDefault[i];
			if(ruleObj)
			{
				var radioId = qaFilter.getRadioId(ruleObj.ruleId);
				var checkedStr = qaFilter.checkedItemIds.contains(radioId) ? " checked " : "";
				var targetExpansion = ruleObj.targetExpansion;
				
				str.append("<tr>");
				str.append("<td><input onclick='qaFilter.checkthis(this)' type='checkbox' id='" + radioId + "'" + checkedStr + "/></td>");
				str.append("<td colspan='4' class='specialFilter_dialog_label'>");
				if (targetExpansion)
				{
					var input = "<input type='text' style='width:30px' class='specialFilter_dialog_label' id='qafilter_rule_target_expansion' value='" + targetExpansion + "'> ";
					str.append(jsQAFilterRuleTargetExpansion.replace("%s", input));
				}
				else
				{
					str.append(ruleObj.check);
				}
				str.append("</td>");
				str.append("</tr>");
			}
		}
	}
	
	return str.toString();
}

QAFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	this.initOptionMap(this.filter);
	qaFilter.currentPage=0;
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' class='specialFilter_dialog_label' id='qaFilterName' maxlength='" + maxFilterNameLength + "' value='" + this.filter.filterName + "'></input>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	
	str.append("<td><textarea style='width:450px' rows='4' class='specialFilter_dialog_label' id='qaFilterDesc' name='description'>" + this.filter.filterDescription + "</textarea>");
	str.append("</td></tr></table>");

	str.append(qaFilter.generateEmptyTable());
	
	str.append("<table border=0 width='530px'>");
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(qaFilter.generateRulesTable(this.filter));
	str.append("</td>");
	str.append("</tr>");
	str.append("</table>");
	
	var dialogObj = document.getElementById('qaFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	saveQAFilter.edit = true;
	saveQAFilter.filterId = filterId;
	saveQAFilter.color = color;
	saveQAFilter.filter = this.filter;
	saveQAFilter.specialFilters = specialFilters;
	saveQAFilter.topFilterId = topFilterId;
}

QAFilter.prototype.generateDiv = function(topFilterId, color)
{
	this.initOptionMap();
	
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' class='specialFilter_dialog_label' id='qaFilterName' maxlength='" + maxFilterNameLength + "' value='" + getFilterNameByTableName('qa_filter') + "'></input>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	
	str.append("<td><textarea style='width:450px' rows='4' class='specialFilter_dialog_label' id='qaFilterDesc' name='description'></textarea>");
	str.append("</td></tr></table>");

	str.append(qaFilter.generateEmptyTable());
	
	str.append("<table border=0 width='530px'>");
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(qaFilter.generateRulesTable());
	str.append("</td>");
	str.append("</tr>");
	str.append("</table>");
	
	var dialogObj = document.getElementById('qaFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveQAFilter.edit = false;
	saveQAFilter.topFilterId = topFilterId;
	saveQAFilter.color = color;
}

function saveQAFilter()
{
	var validate = new Validate();
	var filterName = document.getElementById("qaFilterName").value;
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
    
	if (!qaFilter.addDefaultRulesInCurrentOption())
	{
		return;
	}
    
    var isNew = (saveQAFilter.edit) ? "false" : "true";
	var filterDesc = document.getElementById("qaFilterDesc").value;
	var rules = JSON.stringify(qaFilter.optionObjsMap[qaFilter.currentOption]);
	var defaultRules = JSON.stringify(qaFilter.optionDefaultObjsMap[qaFilter.currentOption]);
	
	var obj = {
		isNew : isNew,
		filterTableName : "qa_filter",
		filterName : filterName,
		filterDesc : filterDesc,
		filterId : saveQAFilter.filterId,
		rules : rules,
		defaultRules : defaultRules,
		companyId : companyId
	};
	
	sendAjax(obj, "isFilterValid", "isQAFilterValidCallback");
	
	isFilterValidCallback.obj = obj;
}

function isQAFilterValidCallback(data)
{
	if(data == 'true')
	{
		qaFilter.closeQAFilterDialog();
		if(saveQAFilter.edit)
		{
			sendAjax(isFilterValidCallback.obj, "updateQAFilter", "updateQAFilterCallback");
		}
		else
		{
			sendAjax(isFilterValidCallback.obj, "saveQAFilter", "saveQAFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		qaFilter.closeQAFilterDialog();
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

function updateQAFilterCallback(data)
{
	var color = saveQAFilter.color;
	var filterId = saveQAFilter.filterId;
	var filter = saveQAFilter.filter;
	var topFilterId = saveQAFilter.topFilterId;
	if(filter)
	{
		var qaFilter = new Object();
		qaFilter.id = filterId;
		qaFilter.filterTableName = "qa_filter";
		qaFilter.filterName = isFilterValidCallback.obj.filterName;
		qaFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		qaFilter.rules = isFilterValidCallback.obj.rules;
		qaFilter.defaultRules = isFilterValidCallback.obj.defaultRules;
		qaFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveQAFilter.specialFilters, qaFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveQAFilterCallback(data)
{
	var color = saveQAFilter.color;
	var topFilterId = saveQAFilter.topFilterId;
	
	var filter = getFilterById(topFilterId);
	if(filter)
	{
		var qaFilter = new Object();
		qaFilter.id = data - 0;
		qaFilter.filterTableName = "qa_filter";
		qaFilter.filterName = isFilterValidCallback.obj.filterName;
		qaFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		qaFilter.rules = isFilterValidCallback.obj.rules;
		qaFilter.defaultRules = isFilterValidCallback.obj.defaultRules;
		qaFilter.companyId = companyId;
		filter.specialFilters.push(qaFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

QAFilter.prototype.generateAddExceptionTable = function()
{
	var str = new StringBuffer("");
	str.append("<table width='440px' cellpadding='3' border='0' cellspacing='1'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRuleException + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='text' style='width:240px' class='specialFilter_dialog_label' id='qaFilter_rule_exception' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRuleExceptionIsRegEx + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='qaFilter_rule_exception_is_regex' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRuleLanguage + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<select style='width:240px' class='specialFilter_dialog_label' id='qaFilter_rule_language'>");
	for (var i = 0; i < localeIds.length; i++)
    {
		var localeId = localeIds[i];
		var localeDisplayName = localeIdDisplayNames[localeId];
		str.append("<option value=\"" + localeId + "\">" + localeDisplayName + "</option>");
    }
	str.append("</select>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	var dialogObj = document.getElementById("qaFilterRuleExceptionPopupContent");
	dialogObj.innerHTML = str.toString();
}

QAFilter.prototype.generateAddRuleTable = function()
{
	var str = new StringBuffer("");
	str.append("<table width='460px' cellpadding='3' border='0' cellspacing='1'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRuleCheck + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='text' style='width:250px' class='specialFilter_dialog_label' id='qaFilter_rule_check' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRuleCheckIsRegEx + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='qaFilter_rule_isRE' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRuleDesc + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='text' style='width:250px' class='specialFilter_dialog_label' id='qaFilter_rule_desc' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>"  + jsQAFilterRulePriority + "</td>")
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='text' style='width:250px' class='specialFilter_dialog_label' id='qaFilter_rule_priority' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	str.append("<p>");
	str.append("<table width='460px' border='0'>");
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(qaFilter.generateExceptionsTable());
	str.append("</td>");
	str.append("</tr>");
	str.append("</table>");
	
	var dialogObj = document.getElementById("qaFilterRulePopupContent");
	dialogObj.innerHTML = str.toString();
}

QAFilter.prototype.generateExceptionsTable = function()
{
	var str = new StringBuffer("");
	str.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
	str.append("<tr><td>");
	str.append("<div style='float:left'>");
	str.append("<input type='button' class='specialFilter_dialog_label' value='" + jsQAFilterRuleAddException + "...'  onclick='qaFilter.addException()'>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	str.append("<p>");
	
	str.append("<div id='exceptionsContent'>");
	str.append(exceptionsContentTable);
	
	str.append(qaFilter.generateExceptionsContent());
	
	str.append("</table>");
	str.append("</div>");
	
	return str.toString();
}

QAFilter.prototype.generateRulesTable = function(filter)
{
	var str = new StringBuffer("");
	str.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'>");
	str.append("<tr><td>");
	str.append("<div style='float:right'>")
	str.append("<input type='button' class='specialFilter_dialog_label' value='" + jsAdd + "...'  onclick='qaFilter.addRule()'>");
	str.append("<input type='button' class='specialFilter_dialog_label' value='" + jsDelete + "...' onclick='qaFilter.deleteRules()'>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	str.append("<div id='qaRulesContent'>");
	str.append(rulesContentTable);
	
	if (filter)
	{
		str.append(qaFilter.generateRulesContent(qaFilter.currentOption, 0));
	}
	else
	{
		str.append(qaFilter.generateRulesContent(qaFilter.currentOption));
	}
	
	str.append("</table>");
	str.append("</div>");
	
	str.append("<div>");
	
	var pageTotalSize = filter ? qaFilter.getPageSize(qaFilter.optionObjsMap[qaFilter.currentOption].length) : 1;
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=qaFilter.prePage()>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=qaFilter.nextPage()>" + jsNext + "</a>|");
	str.append("<input id='pageCountQAFilter' size=3 type='text' class='specialFilter_dialog_label' value="+((filter) ? qaFilter.currentPage+1 : 1)+">");
	str.append(" / <span id='pageTotalSizeQAFilter' class='standardText'>" + pageTotalSize + " </span>");
	str.append("<input type='button' class='specialFilter_dialog_label' value='" + jsGo + "' onclick=qaFilter.goToPage()>");
	
	str.append("</div>");
	
	return str.toString();
}

QAFilter.prototype.generateEditPrioritiesTable = function()
{
	var ruleObjects = qaFilter.optionObjsMap[qaFilter.currentOption];
	if (ruleObjects.length == 0)
	{
		alert(jsQAFilterMsgEditPrioritiesNoRules);
		return;
	}
	
	qaFilter.closeAllPopup();
	var str = new StringBuffer("<table border=0 width='400px'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='30%' class='tagName_td'>");
	str.append(jsCurrentPriority);
	str.append("</td>");
	str.append("<td width='40%' class='tagName_td'>");
	str.append(jsQAFilterRuleCheck);
	str.append("</td>");
	str.append("<td width='30%' class='tagName_td'>");
	str.append(jsNewPriority);
	str.append("</td>");
	str.append("</tr>");
	
	for(var i = 0; i < ruleObjects.length; i++)
	{
		var ruleObject = ruleObjects[i];
		if (ruleObject)
		{
			var backgroundColor = "#C7CEE0";
			if(i % 2 == 0)
			{
				backgroundColor = "#DFE3EE";
			}
			var encodedCheck = encodeHtmlEntities(ruleObject.check);
			var editPriorityId = qaFilter.getEditPriorityId(ruleObject.ruleId);
			str.append("<tr style='background-color:" + backgroundColor + "'>");
			str.append("<td class='tagValue_td'>");
			str.append(ruleObject.priority);
			str.append("</td>");
			str.append("<td class='tagValue_td'>");
			str.append(encodedCheck);
			str.append("</td>");
			str.append("<td>");
			str.append("<input type='text' class='specialFilter_dialog_label' id='" + editPriorityId + "' value='" + ruleObject.priority + "'></input>");
			str.append("</td>");
			str.append("</tr>");
		}
	}
	str.append("</table>");
	
	document.getElementById("qaFilterEditPrioritiesPopupContent").innerHTML = str.toString();
	showPopupDialog("qaFilter_Edit_Priorities_Dialog");
}

QAFilter.prototype.savePriorities = function()
{
	var validate = new Validate();
	
	var ruleObjects = qaFilter.optionObjsMap[qaFilter.currentOption];
	var newPriorities = new Array();
	
	for(var i = 0; i < ruleObjects.length; i++)
	{
		var ruleObject = ruleObjects[i];
		var editPriorityId = qaFilter.getEditPriorityId(ruleObject.ruleId);
		var editPriority = document.getElementById(editPriorityId).value;
		
		if (!validate.isPositiveInteger(editPriority)
				|| editPriority < 1 || editPriority > 255)
		{
			alert("[" + ruleObject.check + "]: " + jsAlertPriorityValue);
			return;
		}
		
		newPriorities[i] = editPriority;
	}
	
	for(var i = 0; i < newPriorities.length; i++)
	{
		var ruleObject = ruleObjects[i];
		var newPriority = newPriorities[i];
		for (var j = i + 1; j < newPriorities.length; j++)
		{
			var otherOne = newPriorities[j];
			if (newPriority == otherOne)
			{
				alert("[" + ruleObject.check + "]: " + jsAlertSamePriority);
				return;
			}
		}
	}
	
	for(var i = 0; i < ruleObjects.length; i++)
	{
		var ruleObject = ruleObjects[i];
		ruleObject.priority = newPriorities[i];
	}
	
	closePopupDialog("qaFilter_Edit_Priorities_Dialog");
	var content = qaFilter.generateRulesContent(qaFilter.currentOption, qaFilter.currentPage);
	qaFilter.refreshRulesContent(content);
}

QAFilter.prototype.closeAllPopup = function()
{
	closePopupDialog("qaFilter_Rule_Dialog");
    closePopupDialog("qaFilter_Rule_Exception_Dialog");
    closePopupDialog("qaFilter_Delete_Rules_Dialog");
    closePopupDialog("qaFilter_Edit_Priorities_Dialog");
}

QAFilter.prototype.closeQAFilterDialog = function()
{
	closePopupDialog("qaFilterDialog");
	qaFilter.closeAllPopup();
	qaFilter.currentPage = 0;
}

QAFilter.prototype.closeRuleDialog = function()
{
	qaFilter.closeRuleExceptionDialog();
	closePopupDialog("qaFilter_Rule_Dialog");	
	qaFilter.editRuleId = -1;
	qaFilter.editRuleExceptions = new Array();
}

QAFilter.prototype.closeRuleExceptionDialog = function()
{
	closePopupDialog("qaFilter_Rule_Exception_Dialog");	
	qaFilter.editExceptionId = -1;
}

QAFilter.prototype.isDefined = function(objj)
{
	return (typeof(objj) != 'undefined');
}

QAFilter.prototype.addDefaultRulesInCurrentOption = function()
{
	var expansionValue = document.getElementById("qafilter_rule_target_expansion").value;
	var validate = new Validate();
	if(!validate.isNumber(expansionValue))
	{
		alert(jsQAFilterMsgTargetExpansionIsNotNumber);
		return false;
	}
	var objArrayDefault = qaFilter.optionDefaultObjsMap[qaFilter.currentOption];
	for(var i = 0; i < objArrayDefault.length; i++)
	{
		var ruleObj = objArrayDefault[i];
		var radioId = qaFilter.getRadioId(ruleObj.ruleId);
		var radio = document.getElementById(radioId);
		ruleObj.enable = radio.checked;
		var targetExpansion = ruleObj.targetExpansion;
		if (targetExpansion)
		{
			ruleObj.targetExpansion = expansionValue;
			ruleObj.check = jsQAFilterRuleTargetExpansion.replace("%s", expansionValue);
		}
		objArrayDefault[i] = ruleObj;
	}
	return true;
}

QAFilter.prototype.addExceptionInCurrentRule = function(exception)
{
	if (qaFilter.isDefined(exception))
	{
		var i = 0;
		var exceptions = qaFilter.editRuleExceptions;
		var added = false;
		var exceptionId = exception.exceptionId;
		
		for(; i < exceptions.length; i++)
		{
			var oriException = exceptions[i];
			if (oriException.exceptionId == exceptionId)
			{
				exceptions[i] = exception;
				added = true;
				break;
			}
		}
		
		if (!added)
		{
			exceptions[i] = exception;
		}
		var content = qaFilter.generateExceptionsContent();
		qaFilter.refreshExceptionsContent(content);
	}
}

QAFilter.prototype.addOneItemInCurrentOption = function(rule)
{
	if (qaFilter.isDefined(rule))
	{
		var i = 0;
		var objArray = qaFilter.optionObjsMap[qaFilter.currentOption];
		var added = false;
		var ruleId = rule.ruleId;
		
		for(; i < objArray.length; i++)
		{
			var oriRule = objArray[i];
			if (oriRule.ruleId == ruleId)
			{
				objArray[i] = rule;
				added = true;
				break;
			}
		}
		
		if (!added)
		{
			objArray[i] = rule;
			checkAllRulesInQAFilter = false;
		}
		
		var maxPages = qaFilter.getPageSize(objArray.length);
		document.getElementById("pageTotalSizeQAFilter").innerText = maxPages;
		
		var content = qaFilter.generateRulesContent(qaFilter.currentOption, qaFilter.currentPage);
		qaFilter.refreshRulesContent(content);
	}
}

QAFilter.prototype.addException = function(exceptionId)
{
	qaFilter.generateAddExceptionTable();
	var isEdit = (exceptionId) ? true : false;
	var useRuleDiv = (qaFilter.currentOption == qaFilter.optionQARules);
	showPopupDialog("qaFilter_Rule_Exception_Dialog");
	
	var exception = isEdit ? qaFilter.getExceptionById(exceptionId) : new Object();
	qaFilter.editExceptionId = isEdit ? exception.exceptionId : qaFilter.newItemId();
	
	if (useRuleDiv)
	{
		if (isEdit)
		{
			var languageSelect = document.getElementById("qaFilter_rule_language");
			var language = exception.language;
			document.getElementById("qaFilter_rule_exception").value = exception.exceptionContent;
			document.getElementById("qaFilter_rule_exception_is_regex").checked = exception.exceptionIsRE;
			for (var i = 0; i < languageSelect.length; i++)
			{
				if (languageSelect[i].value == language)
				{
					languageSelect[i].selected = "selected";
				}
			}
		}
	}
}

QAFilter.prototype.addRule = function(radioId)
{
	qaFilter.closeAllPopup();
	var isEdit = (radioId) ? true : false;
	var useRuleDiv = (qaFilter.currentOption == qaFilter.optionQARules);
	
	var editId = isEdit ? qaFilter.getRuleId(radioId) : -1;
	var editRule = isEdit ? qaFilter.getRuleById(editId, qaFilter.currentOption) : new Object();
	qaFilter.editRuleId = isEdit? editRule.ruleId : qaFilter.newItemId();
	qaFilter.editRuleEnable = isEdit? editRule.enable : false;
	qaFilter.editRuleExceptions = new Array();
	if (isEdit)
	{
		var exceptions = editRule.exceptions;
		if (qaFilter.isDefined(exceptions))
		{
			if (qaFilter.isDefined(exceptions.length))
			{
				for (var i = 0; i < exceptions.length; i++)
				{
					qaFilter.editRuleExceptions.appendUniqueObj(exceptions[i]);
				}
			}
			else
			{
				qaFilter.editRuleExceptions.appendUniqueObj(exceptions);
			}
		}
	}
	qaFilter.generateAddRuleTable();
	showPopupDialog("qaFilter_Rule_Dialog");
	
	if (useRuleDiv)
	{
		if (isEdit)
		{
			document.getElementById("qaFilter_rule_check").value = editRule.check;
			document.getElementById("qaFilter_rule_isRE").checked = editRule.isRE;
			document.getElementById("qaFilter_rule_desc").value = editRule.description;
			document.getElementById("qaFilter_rule_priority").value = editRule.priority;
		}
	}
}

QAFilter.prototype.getExceptionById = function(exceptionId)
{
	var item;
	var exceptions = qaFilter.editRuleExceptions;
	if (exceptions && exceptions.length > 0)
	{
		for(var i = 0; i < exceptions.length; i++)
		{
			var exception = exceptions[i];
			if (exception.exceptionId == exceptionId)
			{
				return exception;
			}
		}
	}
	
	return item;
}

QAFilter.prototype.getRuleById = function(ruleId, optionValue)
{
	var item;
	var rules = qaFilter.optionObjsMap[optionValue];
	for(var i = 0; i < rules.length; i++)
	{
		var rule = rules[i];
		if (rule.ruleId == ruleId)
		{
			return rule;
		}
	}
	
	rules = qaFilter.optionDefaultObjsMap[optionValue];
	for(var i = 0; i < rules.length; i++)
	{
		var rule = rules[i];
		if (rule.ruleId == ruleId)
		{
			return rule;
		}
	}
	return item;
}

QAFilter.prototype.saveRuleException = function()
{
	var exceptionId = qaFilter.editExceptionId;
	var validate = new Validate();
	var exceptionContent = new StringBuffer(document.getElementById("qaFilter_rule_exception").value);
	var exceptionIsRE = document.getElementById("qaFilter_rule_exception_is_regex").checked;
	var languageSelect = document.getElementById("qaFilter_rule_language");
	var localeId = languageSelect.options[languageSelect.selectedIndex].value;
	var language = localeId;

	if(validate.isEmptyStr(exceptionContent.trim()))
	{
		document.getElementById("qaFilter_rule_exception").value = "";
		alert(jsQAFilterMsgRequiredException);
		return;
	}
	if(qaFilter.isRuleExceptionExist(exceptionContent, exceptionIsRE, language))
	{
		alert(jsQAFilterMsgExistException);
		return;
	}
	
	var exception = {exceptionId : exceptionId, exceptionContent : exceptionContent.trim(), exceptionIsRE : exceptionIsRE, language : language};
	qaFilter.addExceptionInCurrentRule(exception);
	qaFilter.closeRuleExceptionDialog();
}

QAFilter.prototype.saveRule = function()
{
	var ruleId = qaFilter.editRuleId;
	var validate = new Validate();
	var check = new StringBuffer(document.getElementById("qaFilter_rule_check").value);
	var isRE = document.getElementById("qaFilter_rule_isRE").checked;
	var description = document.getElementById("qaFilter_rule_desc").value;
	var priority = document.getElementById("qaFilter_rule_priority").value;
	var exceptions = qaFilter.editRuleExceptions;

	if(validate.isEmptyStr(check.trim()))
	{
		document.getElementById("qaFilter_rule_check").value = "";
		alert(jsQAFilterMsgRequiredCheck);
		return;
	}
	
	if(validate.isEmptyStr(description.trim()))
	{
		document.getElementById("qaFilter_rule_desc").value = "";
		alert(jsQAFilterMsgRequiredDesc);
		return;
	}
	
	if (!qaFilter.validatePriority(priority))
	{
		return;
	}
	
	if(qaFilter.isRuleExist(check, isRE, description, exceptions))
	{
		alert(jsQAFilterMsgExistRule);
		return;
	}
	
	var enable = qaFilter.editRuleEnable;
	var rule = {ruleId : ruleId, enable : enable, check : check.trim(), isRE : isRE, description : description, priority : priority, exceptions : exceptions};
	
	qaFilter.addOneItemInCurrentOption(rule);
	qaFilter.closeRuleDialog();
}

QAFilter.prototype.isRuleExist = function(check, isRE, description, exceptions)
{
	var editRuleId = qaFilter.editRuleId;
	var rules = qaFilter.optionObjsMap[qaFilter.optionQARules];
	for (var i = 0; i < rules.length; i++)
	{
		var rule = rules[i];
		if (rule.ruleId == editRuleId)
		{
			continue;
		}
		if (rule.check == check && rule.isRE == isRE && rule.description == description)
		{
			var existingExceptions = rule.exceptions;
			if (existingExceptions.length == exceptions.length)
			{
				for (var j = 0; j < exceptions.length; j++)
				{
					var exception = exceptions[j];
					if (!qaFilter.isExceptionExist(
							exception.exceptionContent, exception.exceptionIsRE, exception.language, existingExceptions))
					{
						return false;
					}
				}
				return true;
			}
		}
	}
	
	return false;
}

QAFilter.prototype.isRuleExceptionExist = function(exceptionContent, exceptionIsRE, language)
{
	var exceptions = qaFilter.editRuleExceptions;
	
	return qaFilter.isExceptionExist(exceptionContent, exceptionIsRE, language, exceptions);
}

QAFilter.prototype.isExceptionExist = function(exceptionContent, exceptionIsRE, language, existingExceptions)
{
	var editExceptionId = qaFilter.editExceptionId;
	for (var i = 0; i < existingExceptions.length; i++)
	{
		var existingException = existingExceptions[i];
		if (existingException.exceptionId == editExceptionId)
		{
			continue;
		}
		if (existingException.exceptionContent == exceptionContent 
				&& existingException.exceptionIsRE == exceptionIsRE && existingException.language == language)
		{
			return true;
		}
	}
	
	return false;
}

QAFilter.prototype.validatePriority = function(priority)
{
	var editRuleId = qaFilter.editRuleId;
	var validate = new Validate();
		
	if(validate.isEmptyStr(priority.trim()))
	{
		document.getElementById("qaFilter_rule_priority").value = "";
		alert(jsQAFilterMsgRequiredPriority);
		return false;
	}
	
	if (!validate.isPositiveInteger(priority)
			|| priority < 1 || priority > 255)
	{
		alert(jsAlertPriorityValue);
		return false;
	}
	
	var ruleObjects = qaFilter.optionObjsMap[qaFilter.currentOption];
	for(var i = 0; i < ruleObjects.length; i++)
	{
		var ruleObject = ruleObjects[i];
		if (priority == ruleObject.priority && editRuleId != ruleObject.ruleId)
		{
			alert(jsAlertSamePriority);
			return false;
		}
	}
		
	return true;
}

QAFilter.prototype.isDefined = function(objj)
{
	return (typeof(objj) != 'undefined');
}

QAFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	return newid;
}

QAFilter.prototype.setPageValue = function()
{
	var curOption = qaFilter.currentOption;
	var maxPages = qaFilter.getPageSize(qaFilter.optionObjsMap[curOption].length);
	qaFilter.currentPage = (qaFilter.currentPage >= maxPages) ? maxPages - 1 : qaFilter.currentPage;
	document.getElementById("pageTotalSizeQAFilter").innerHTML = maxPages;
	
	document.getElementById("pageCountQAFilter").value = qaFilter.currentPage + 1;
}

QAFilter.prototype.prePage = function()
{
    if(qaFilter.currentPage > 0)
	{
    	qaFilter.currentPage --;
    	var content = qaFilter.generateRulesContent(qaFilter.currentOption, qaFilter.currentPage);
		qaFilter.refreshRulesContent(content);
    	qaFilter.setPageValue();
	}
    else
    {
    	return;
    }
}

QAFilter.prototype.nextPage = function()
{
	var arraysize = qaFilter.getCurrentObjectsSize();
	if(qaFilter.currentPage < qaFilter.getPageSize(arraysize) - 1)
	{
		qaFilter.currentPage ++;
		var content = qaFilter.generateRulesContent(qaFilter.currentOption, qaFilter.currentPage);
		qaFilter.refreshRulesContent(content);
		qaFilter.setPageValue();
	}
	else
    {
    	return;
    }
}

QAFilter.prototype.getCurrentObjectsSize = function()
{
	return qaFilter.optionObjsMap[qaFilter.currentOption].length;
}

QAFilter.prototype.getCurrentDefaultObjectsSize = function()
{
	return qaFilter.optionDefaultObjsMap[qaFilter.currentOption].length;
}

QAFilter.prototype.getPageSize = function(itemsTotalCount)
{
	var countPerPage = qaFilter.rulesPerPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

QAFilter.prototype.goToPage = function()
{
	var pageValue = document.getElementById("pageCountQAFilter").value;
	var validate = new Validate();
	if(!validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	var arraysize = qaFilter.getCurrentObjectsSize();
	
	if(pageValue < 1 || pageValue > qaFilter.getPageSize(arraysize))
	{
		alert(invalidatePageValue.replace("%s", qaFilter.getPageSize(arraysize)));
		qaFilter.setPageValue();
		return;
	}

	qaFilter.currentPage = pageValue - 1;
	var content = qaFilter.generateRulesContent(qaFilter.currentOption, qaFilter.currentPage);
	qaFilter.refreshRulesContent(content);
}

QAFilter.prototype.refreshRulesContent = function(content)
{
	var ccc = new StringBuffer(rulesContentTable);
	ccc.append(content);
	ccc.append("</table>");
	
	document.getElementById("qaRulesContent").innerHTML = ccc.toString();
}

QAFilter.prototype.refreshExceptionsContent = function(content)
{
	var ccc = new StringBuffer(exceptionsContentTable);
	ccc.append(content);
	ccc.append("</table>");
	
	document.getElementById("exceptionsContent").innerHTML = ccc.toString();
}

QAFilter.prototype.generateExceptionsContent = function()
{
	var exceptions = qaFilter.editRuleExceptions;
	
	var str = new StringBuffer("");
	str.append("<tr class='htmlFilter_emptyTable_tr'>");
	
	if (exceptions && exceptions.length > 0)
	{
	    str.append("<td width='45%' class='tagName_td'>" + jsQAFilterRuleException + "</td>");
		str.append("<td width='25%' class='tagName_td'>" + jsQAFilterRuleLanguage + "</td>");
		str.append("<td class='tagName_td'>" + jsQAFilterRuleExceptionIsRegEx + "</td>");
		
		str.append("</tr>");
		
		for(var i = 0; i < exceptions.length; i++)
		{
			var exception = exceptions[i];
			if(exception)
			{
				var backgroundColor = "#C7CEE0";
				if(i % 2 == 0)
				{
					backgroundColor = "#DFE3EE";
				}
				var encodedExceptionContent = encodeHtmlEntities(exception.exceptionContent);
				var localeId = exception.language;
				str.append("<tr style='background-color:" + backgroundColor + "'>");
				str.append("<td class='tagValue_td'><a href='#' onclick=\"qaFilter.addException('" + exception.exceptionId + "')\">" + encodedExceptionContent + "</a></td>");
				str.append("<td class='tagValue_td'><a href='#' onclick=\"qaFilter.addException('" + exception.exceptionId + "')\">" + localeIdCodes[localeId] + "</a></td>");
				str.append("<td class='tagValue_td'>" + (exception.exceptionIsRE ? imgYes : "") + "</td>");
				
				str.append("</tr>");
			}
		}
	}
	else
	{
		str.append("<td width='45%' class='tagName_td'>" + jsQAFilterRuleException + "</td>");
		str.append("<td width='25%' class='tagName_td'>" + jsQAFilterRuleLanguage + "</td>");
		str.append("<td class='tagName_td'>" + jsQAFilterRuleExceptionIsRegEx + "</td>");
		
		str.append("</tr>");
	}
	
	return str.toString();
}

QAFilter.prototype.generateRulesContent = function(optionValue, pageIndex)
{
	var objArray = qaFilter.optionObjsMap[optionValue];
	var str = new StringBuffer("");
	str.append("<tr class='htmlFilter_emptyTable_tr'>");
	
	if (objArray && objArray.length > 0)
	{
		for(var i = 0; i < objArray.length; i++)
		{
			var rule = objArray[i];
			if (!qaFilter.isDefined(rule.priority))
			{
				ruleone.priority = i + 1;
			}
		}
		objArray.sort(sortPriority);
		
		str.append("<td width='5%'><input type='checkbox' onclick='qaFilter.checkAll()' id='checkAllRules'/></td>");

		if (optionValue == qaFilter.optionQARules)
		{
			str.append("<td width='35%' class='tagName_td'>" + jsQAFilterRuleCheck + "</td>");
			str.append("<td class='tagName_td'>" + jsQAFilterRuleCheckIsRegEx + "</td>");
			str.append("<td width='25%' class='tagName_td'>" + jsQAFilterRuleDesc + "</td>");
			str.append("<td class='tagName_td'>" + jsQAFilterRulePriority + "</td>");
		}
		str.append("</tr>");
		
		var startIndex = 0;
		startIndex = qaFilter.rulesPerPage * pageIndex;
		
		for(var i = 0; i < qaFilter.rulesPerPage; i++)
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
				var radioId = qaFilter.getRadioId(ruleObj.ruleId);
				var checkedStr = qaFilter.checkedItemIds.contains(radioId) ? " checked " : "";
				var encodedCheck = encodeHtmlEntities(ruleObj.check);
				var encodedDesc = encodeHtmlEntities(ruleObj.description);
				
				str.append("<tr style='background-color:" + backgroundColor + "'>");
				str.append("<td><input onclick='qaFilter.checkthis(this)' type='checkbox' id='" + radioId + "'" + checkedStr + "/></td>");
				str.append("<td class='tagValue_td'><a href='#' onclick=\"qaFilter.addRule('" + radioId + "')\">" + encodedCheck + "</a></td>");
				str.append("<td class='tagValue_td'>" + (ruleObj.isRE ? imgYes : "") + "</td>");
				str.append("<td class='tagValue_td'>" + encodedDesc + "</td>");
				str.append("<td class='tagValue_td'>" + (ruleObj.priority ? ruleObj.priority : 9) + "</td>");
				
				str.append("</tr>");
			}
		}
	}
	else
	{
		str.append("<td width='5%'><input type='checkbox' onclick='qaFilter.checkAll()' id='checkAllRules'/></td>");
		if (optionValue == qaFilter.optionQARules)
		{
			str.append("<td width='35%' class='tagName_td'>" + jsQAFilterRuleCheck + "</td>");
			str.append("<td class='tagName_td'>" + jsQAFilterRuleCheckIsRegEx + "</td>");
			str.append("<td width='25%' class='tagName_td'>" + jsQAFilterRuleDesc + "</td>");
			str.append("<td class='tagName_td'>" + jsQAFilterRulePriority + "</td>");
		}
		str.append("</tr>");
	}
	str.append(qaFilter.generateEmptyRows());
	str.append(qaFilter.generateDefaultRulesTable(optionValue));
	str.append("<tr><td><br /></td></tr>");
	
	return str.toString();
}

QAFilter.prototype.generateDeleteRulesTableContent = function()
{
	var ruleObjects = qaFilter.optionObjsMap[qaFilter.currentOption];
	var isOdd = true;
	var count = 0;
	var showCurrent = false;
	if(ruleObjects && ruleObjects.length > 0)
	{
		for(var i = 0; i < ruleObjects.length; i++)
		{
			var ruleObject = ruleObjects[i];
			if(ruleObject.enable)
			{
				showCurrent = true;
				break;
			}
		}
	}
	if (showCurrent)
	{
		var str = new StringBuffer("");
		str.append("<table>");
		str.append("<tr><td><label class='specialFilter_dialog_label'>");
		str.append(jsQAFilterDeleteRulesNote + ":");
		str.append("</label></td></tr>");
		str.append("</table>");
	    
		str.append("<p>");
		
		str.append("<table border=0 width='420px'>");
		str.append("<tr class='deleteTagsDialog_header'>");
		str.append("<td width='70%' class='tagName_td'>");
		str.append("<input type='checkbox' checked='true' id='checkAllDeleteRules' onclick='qaFilter.checkAllRulesToDelete()'/>");
		str.append("<label class='tagName_td'>" + jsQAFilterDeleteRulesToDelete + "</label>");
		str.append("</td>");
		str.append("<td width='30%' class='tagName_td'>" + jsQAFilterDeleteRulesCount + "</td>");
		str.append("</tr>");
		
		var backgroundColor = isOdd ? "#DFE3EE" : "#C7CEE0";
		
		str.append("<tr style='background-color:" + backgroundColor + "'>");
		str.append("<td class='tagValue_td'>");
		
		for(var i = 0; i < ruleObjects.length; i++)
		{
			var ruleObject = ruleObjects[i];
			if(ruleObject.enable)
			{
				var encodedCheck = encodeHtmlEntities(ruleObject.check);
				var checkboxId = qaFilter.getCheckboxId(ruleObject.ruleId);
				str.append("<input type='checkbox' id='" + checkboxId + "' checked>");
				str.append(encodedCheck);
				str.append("</input>");
				count++;
			}
		}
		str.append("</td>");
		str.append("<td class='tagValue_td'>");
		str.append(count);
		str.append("</td>");
		str.append("</tr>");
		
		str.append("</table>");
		
		var dialogObj = document.getElementById("qaFilterDeleteRulesPopupContent");
		dialogObj.innerHTML = str.toString();
	}
	else
	{
		var defaultRulesChecked = false;
		var defaultRuleObjects = qaFilter.optionDefaultObjsMap[qaFilter.currentOption];
		if(defaultRuleObjects && defaultRuleObjects.length > 0)
		{
			for(var i = 0; i < defaultRuleObjects.length; i++)
			{
				var defaultRuleObject = defaultRuleObjects[i];
				if(defaultRuleObject.enable)
				{
					defaultRulesChecked = true;
					alert(jsQAFilterMsgDeleteRulesDefaultRulesNotAllowed);
					return false;
				}
			}
		}
		if (!defaultRulesChecked)
		{
			alert(jsQAFilterMsgDeleteRulesNoRulesSelected);
			return false;
		}
	}
	
	return true;
}

QAFilter.prototype.checkAllRulesToDelete = function()
{
	var checkAll = document.getElementById("checkAllDeleteRules");
	var ruleObjects = qaFilter.optionObjsMap[qaFilter.currentOption];
    for(var i = 0; i < ruleObjects.length; i++)
    {
    	var ruleObj = ruleObjects[i];
    	if (!ruleObj.enable)
    	{
    		continue;
    	}
    	var checkboxId = qaFilter.getCheckboxId(ruleObj.ruleId);
    	var checkboxObj = document.getElementById(checkboxId);
    	if (qaFilter.isDefined(checkboxObj))
    	{
    		checkboxObj.checked = checkAll.checked;
    	}
    }
}

QAFilter.prototype.deleteRules = function()
{
	var hasRulesToDelete = qaFilter.generateDeleteRulesTableContent();
	if(hasRulesToDelete)
	{
		qaFilter.closeAllPopup();
		showPopupDialog("qaFilter_Delete_Rules_Dialog");
	}
}

QAFilter.prototype.deleteCheckedRules = function()
{
	var deleted = false;
	var ruleObjects = qaFilter.optionObjsMap[qaFilter.currentOption];
    for(var i = 0; i < ruleObjects.length; i++)
    {
    	var ruleObj = ruleObjects[i];
    	if (!ruleObj.enable)
    	{
    		continue;
    	}
    	var checkboxId = qaFilter.getCheckboxId(ruleObj.ruleId);
    	var checkboxObj = document.getElementById(checkboxId);
        if (checkboxObj.checked)
        {
        	ruleObjects.removeData(ruleObj);
        	i--;
        	deleted = true;
        }
    }
    if (!deleted)
    {
    	alert(jsQAFilterMsgDeleteRulesNoRulesSelected);
    	return;
    }
	closePopupDialog("qaFilter_Delete_Rules_Dialog");
	qaFilter.refreshRulesTable();
}

QAFilter.prototype.refreshRulesTable = function()
{
	qaFilter.currentPage = 0;
	qaFilter.closeAllPopup();
	qaFilter.refreshCheckedIds();
	var content = qaFilter.generateRulesContent(qaFilter.currentOption, qaFilter.currentPage);
	qaFilter.refreshRulesContent(content);
	document.getElementById("checkAllQAFilter").checked = false;
	qaFilter.setPageValue();
}

QAFilter.prototype.getRuleId = function(radioid)
{
	return radioid.replace("qa_rule_", "");
}

QAFilter.prototype.getRadioId = function(ruleId)
{
	return "qa_rule_" + ruleId;
}

QAFilter.prototype.getCheckboxId = function(ruleId)
{
	return "delete_rule_" + ruleId;
}

QAFilter.prototype.getEditPriorityId = function(ruleId)
{
	return "edit_priority_" + ruleId;
}

QAFilter.prototype.checkthis = function(cobj)
{
	var oid = cobj.id;
	var checkAllObj = document.getElementById("checkAllQAFilter");
	var ruleId = qaFilter.getRuleId(oid);
	var ruleObj = qaFilter.getRuleById(ruleId, qaFilter.currentOption);
	ruleObj.enable = cobj.checked;

	if (!cobj.checked)
	{
		checkAllRulesInQAFilter = false;
		checkAllObj.checked = false;
		qaFilter.checkedItemIds.removeData(oid);
	}
	else
	{
		if (!qaFilter.checkedItemIds.contains(oid))
		{
			qaFilter.checkedItemIds[qaFilter.checkedItemIds.length] = oid;
		}
		
		if (qaFilter.checkedItemIds.length == qaFilter.getCurrentObjectsSize() + qaFilter.getCurrentDefaultObjectsSize())
		{
			checkAllObj.checked = true;
		}
	}
}

QAFilter.prototype.checkAll = function()
{
	var checkAllObj = document.getElementById("checkAllRules");
	checkAllRulesInQAFilter = checkAllObj.checked;
	for(var i = 0; i < qaFilter.getCurrentObjectsSize(); i++)
	{
		var ruleObj = qaFilter.optionObjsMap[qaFilter.currentOption][i];
		var oid = qaFilter.getRadioId(ruleObj.ruleId);
		
		if(document.getElementById(oid))
		{
			document.getElementById(oid).checked = checkAllRulesInQAFilter;	
		}
		
		if (checkAllRulesInQAFilter)
		{
			qaFilter.checkedItemIds.appendUniqueObj(oid);
		}
		
		ruleObj.enable = checkAllRulesInQAFilter;
	}
	
    var defaultRuleObjs = qaFilter.optionDefaultObjsMap[qaFilter.currentOption];
	if (defaultRuleObjs && defaultRuleObjs.length > 0)
	{
		for(var i = 0; i < defaultRuleObjs.length; i++)
		{
			var ruleObj = defaultRuleObjs[i];
			var oid = qaFilter.getRadioId(ruleObj.ruleId);
			
			if(document.getElementById(oid))
			{
				document.getElementById(oid).checked = checkAllRulesInQAFilter;
			}
			
			if (checkAllRulesInQAFilter)
			{
				qaFilter.checkedItemIds.appendUniqueObj(oid);
			}
			
			ruleObj.enable = checkAllRulesInQAFilter;
		}
	}
	
	if (!checkAllRulesInQAFilter)
	{
		qaFilter.checkedItemIds = new Array();
	}
}

function encodeHtmlEntities(p_text)
{
	if (!qaFilter.isDefined(p_text) || typeof(p_text) == "number")
	{
		return p_text;
	}
    return p_text.replace(/&/g, "&amp;").replace(/</g, "&lt;").
        replace(/>/g, "&gt;").replace(/\"/g, "&quot;");
}

function sortPriority(x, y)
{
    var p1 = parseInt(x.priority);
    var p2 = parseInt(y.priority);
    var check1 = "" + x.check;
    var check2 = "" + y.check;
    
    if (p1 == p2)
    {
        if (check1 == check2)
        {
            return 0;
        }
        else
        {
            return check1 > check2 ? 1 : -1;
        }
    }
    else
    {
        return p1 > p2 ? 1 : -1;
    }
}
