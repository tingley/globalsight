var xmlFilter = new XMLRuleFilter();
var checkAllTagsInXmlFilter = false;
	
function XMLRuleFilter()
{
	this.filterTableName = "xml_rule_filter";
	this.dialogId = "xmlRuleFilterDialog";
	
	this.optionEmbTags = "0";
	this.optionTransAttrTags = "1";
	this.optionContentInclTags = "2";
	this.optionEntities = "3";
	this.optionProcessIns = "4";
	this.optionPreserveWsTags = "5";
	this.optionCDataPostFilterTags = "6";
	
	this.operatorEqual = "equal";
	this.operatorNotEqual = "not equal";
	this.operatorMatch = "match";
	this.conditionTypeCdataContent = "cdatacontent";
	
	this.operatorNameMap = new Object();
	this.operatorNameMap[this.operatorEqual] = jsOperatorEqual;
	this.operatorNameMap[this.operatorNotEqual] = jsOperatorNotEqual;
	this.operatorNameMap[this.operatorMatch] = jsOperatorMatch;
	
	this.conditionTypeMap = new Object();
	this.conditionTypeMap[this.conditionTypeCdataContent] = jsConditionTypeCdataContent;
	
	this.optionNameMap = new Object();
	this.optionNameMap[this.optionEmbTags] = jsOptionEmbTags;
	this.optionNameMap[this.optionTransAttrTags] = jsOptionTransAttrTags;
	this.optionNameMap[this.optionContentInclTags] = jsOptionContentIncTags;
	this.optionNameMap[this.optionEntities] = jsOptionEntities;
	this.optionNameMap[this.optionProcessIns] = jsOptionProcessIns;
	this.optionNameMap[this.optionPreserveWsTags] = jsOptionPreserveWsTags;
	this.optionNameMap[this.optionCDataPostFilterTags] = jsCDataPostFilterTags;
	
	this.optionObjsMap = new Object();
	this.availableOptions = [this.optionEmbTags, this.optionPreserveWsTags, this.optionTransAttrTags,
	                         this.optionContentInclTags, this.optionCDataPostFilterTags,
	                         this.optionEntities, this.optionProcessIns];
	this.checkedItemIds = new Array();
	
	this.tagsEveryPage = 10;
	this.currentPage = 0;
	this.sortOrder = "asc";
	this.sortColumnIndex = 1;
	this.currentPage = 0;
	this.currentOption = this.optionEmbTags;
	this.editItemId = -1;
	this.editItemEnable = false;
	this.editItemAttributes = new Array();
	this.editItemTransAttributes = new Array();
	this.editItemTransAttrSegRule = 1;
	this.editItemCdataConditions = new Array();
	this.editItemPostFilterId = -1;
	this.editItemPostFilterTableName = "";
	this.editItemTranslatable = true;
	this.availablePostFilters = ["html_filter", "java_script_filter"];
}

XMLRuleFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

XMLRuleFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

XMLRuleFilter.prototype.initOptionMap = function (filter)
{
	xmlFilter.checkedItemIds = new Array();
	checkAllTagsInXmlFilter = false;
	
	if (filter)
	{
		var obj0 = {tagName : "name1", enable : true, itemid : 1, attributes : [{itemid : 0, aName : "name1", aOp : "equal", aValue : "vvv1"}, {itemid : 1, aName : "name2", aOp : "equal", aValue : "vvv2"}]};
		var obj1 = {tagName : "name2", enable : true, itemid : 2, attributes : [{itemid : 0, aName : "name1", aOp : "equal", aValue : "vvv1"}, {itemid : 1, aName : "name2", aOp : "equal", aValue : "vvv2"}]};
		
		var objArray = [obj0, obj1,
		                {tagName:"name3",enable:true,itemid:3,attributes:[]},
		                {tagName:"name4",enable:true,itemid:4,attributes:[]},
		                {tagName:"name5",enable:true,itemid:5,attributes:[]},
		                {tagName:"name6",enable:true,itemid:6,attributes:[]},
		                {tagName:"name7",enable:true,itemid:7,attributes:[]},
		                {tagName:"name8",enable:true,itemid:8,attributes:[]},
		                {tagName:"name9",enable:true,itemid:9,attributes:[]},
		                {tagName:"name10",enable:true,itemid:10,attributes:[]},
		                {tagName:"name11",enable:true,itemid:11,attributes:[]},
		                {tagName:"name12",enable:true,itemid:12,attributes:[]},
		                {tagName:"name13",enable:true,itemid:13,attributes:[]},
		                {tagName:"name14",enable:true,itemid:14,attributes:[]}];
		
		// xmlFilter.optionObjsMap[this.optionPreserveWsTags] = objArray;
		var preserveWsTags = this.filter.preserveWsTags;
		// alert(preserveWsTags);
		xmlFilter.optionObjsMap[this.optionPreserveWsTags] = JSON.parse(preserveWsTags);
		
		var embTags = this.filter.embTags;
		xmlFilter.optionObjsMap[this.optionEmbTags] = JSON.parse(embTags);
		
		// transAttributes : [aName:"name",transAttrSegRule:1]
		var transAttrTags = this.filter.transAttrTags;
		xmlFilter.optionObjsMap[this.optionTransAttrTags] = JSON.parse(transAttrTags);
		
		// inclType : 1 / 2  content inclusion tags
		var contentInclTags = this.filter.contentInclTags;
		xmlFilter.optionObjsMap[this.optionContentInclTags] = JSON.parse(contentInclTags);
		
		//var cdataPostfilterTags = "[{aName:\"cdata1\",enable:true,itemid:10003,cdataConditions:[]," +
//				"postFilterId:1,postFilterTableName:\"html_filter\",translatable:true}]";
		var cdataPostfilterTags = this.filter.cdataPostfilterTags;
		xmlFilter.optionObjsMap[this.optionCDataPostFilterTags] = eval(cdataPostfilterTags);
		
		// entities
		var entities = this.filter.entities;
		xmlFilter.optionObjsMap[this.optionEntities] = JSON.parse(entities);
		
		// process instruction
		var processIns = this.filter.processIns;
		xmlFilter.optionObjsMap[this.optionProcessIns] = JSON.parse(processIns);
	}
	else
	{
		xmlFilter.optionObjsMap[this.optionPreserveWsTags] = new Array();
		xmlFilter.optionObjsMap[this.optionEmbTags] = new Array();
		xmlFilter.optionObjsMap[this.optionTransAttrTags] = new Array();
		xmlFilter.optionObjsMap[this.optionContentInclTags] = new Array();
		xmlFilter.optionObjsMap[this.optionCDataPostFilterTags] = new Array();
		xmlFilter.optionObjsMap[this.optionEntities] = new Array();
		xmlFilter.optionObjsMap[this.optionProcessIns] = new Array();
	}
	
	xmlFilter.refreshCheckedIds();
}

XMLRuleFilter.prototype.refreshCheckedIds = function()
{
	xmlFilter.checkedItemIds = new Array();
	var ruleObjs = xmlFilter.optionObjsMap[xmlFilter.currentOption];
	
	if (ruleObjs && ruleObjs.length > 0)
	{
		for(var i = 0; i<ruleObjs.length; i++)
		{
			var ruleObj = ruleObjs[i];
			if (ruleObj.enable)
			{
				var radioId = xmlFilter.getRadioId(ruleObj.itemid);
				xmlFilter.checkedItemIds.appendUniqueObj(radioId);
			}
		}
		
		//xmlFilter.alertObject(xmlFilter.checkedItemIds);
	}
}
function decodeExtendWhitespace()
{
	var extendedWhitespaceChars = document.getElementById("exSpaceChars").value;
	var temp = new StringBuffer(extendedWhitespaceChars);
	extendedWhitespaceChars = temp.trim();
	
	if (extendedWhitespaceChars != "")
	{	
		var obj = {textvalue : extendedWhitespaceChars, valuetype : "xml"};
		sendAjax(obj, "decodeTextvalue", "decodeExtendWhitespaceCallback");
	}
}

function decodeExtendWhitespaceCallback(data)
{
	//alert(data);
	document.getElementById("exSpaceChars").value = data;
}

function saveXmlRuleFilter()
{
	var validate = new Validate();
	var filterName = document.getElementById("xmlRuleFilterName").value;
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
    
    var isNew = (saveXmlRuleFilter.edit) ? "false" : "true";
	var filterDesc = document.getElementById("xmlRuleDesc").value;
	var xmlRuleId = document.getElementById("xmlRuleSelect").value;
	var convertHtmlEntity = document.getElementById("isEnableConvertHtmlEntity").checked;
	
	var secondaryFilterIdAndTableName = document.getElementById("secondaryFilterSelect").value;
	var splitedSecIdTable = splitByFirstIndex(secondaryFilterIdAndTableName, "-");
	var secondFilterId = (splitedSecIdTable) ? splitedSecIdTable[0] : -2;
	var secondFilterTableName = (splitedSecIdTable) ? splitedSecIdTable[1] : "";

	var elementPostFilterIdTable = document.getElementById("elementPostFilter").value;
	var splitedElementPostIdTable = splitByFirstIndex(elementPostFilterIdTable, "-");
	var elementPostFilterId = (splitedElementPostIdTable) ? splitedElementPostIdTable[0] : "-1";
	var elementPostFilter = (splitedElementPostIdTable) ? splitedElementPostIdTable[1] : "-1";
	
	var cdataIdTable = document.getElementById("cdataPostFilter").value;
	var splitedCdataPostIdTable = splitByFirstIndex(cdataIdTable, "-");
	var cdataPostFilterId = (splitedCdataPostIdTable) ? splitedCdataPostIdTable[0] : "-1";
	var cdataPostFilter = (splitedCdataPostIdTable) ? splitedCdataPostIdTable[1] : "-1";
	
	// TODO use xml rule will be used after all function is added.
	var useXmlRule = "true";
	var extendedWhitespaceChars = document.getElementById("exSpaceChars").value;
	var temp = new StringBuffer(extendedWhitespaceChars);
	extendedWhitespaceChars = temp.trim();
	var phConsolidationMode = document.getElementById("phConsolidateMode").value;
	var phTrimMode = document.getElementById("phTrimMode").value;
	var nonasciiAs = getRadioValue(fpForm.nonasciiAs);
	var wsHandleMode = getRadioValue(fpForm.wsHandleMode);
	var emptyTagFormat = getRadioValue(fpForm.emptyTagFormat);
	var tempStr = new StringBuffer("");
	tempStr.append(document.getElementById("sidSupportTagNameEle").value);
	var sidSupportTagName = tempStr.trim();
	tempStr = new StringBuffer("");
	tempStr.append(document.getElementById("sidSupportAttNameEle").value);
	var sidSupportAttName = tempStr.trim();
	var isCheckWellFormed = document.getElementById("isEnableCheckWellFormed").checked;
	var isGerateLangInfo = document.getElementById("isEnableGerateLangInfo").checked;
	var preserveWsTags = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionPreserveWsTags]);
	var embTags = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionEmbTags]);
	var transAttrTags = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionTransAttrTags]);
	var contentInclTags = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionContentInclTags]);
	var cdataPostfilterTags = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionCDataPostFilterTags]);
	var entities = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionEntities]);
	var processIns = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionProcessIns]);

	var obj = {
		isNew : isNew,
		filterTableName : "xml_rule_filter",
		filterName : filterName,
		filterDesc : filterDesc,
		xmlRuleId : xmlRuleId,
		filterId : saveXmlRuleFilter.filterId,
		companyId : companyId,
		convertHtmlEntity : convertHtmlEntity,
		secondFilterId : secondFilterId,
		secondFilterTableName : secondFilterTableName,
		useXmlRule : useXmlRule,
		extendedWhitespaceChars : extendedWhitespaceChars,
		phConsolidationMode : phConsolidationMode,
		phTrimMode : phTrimMode,
		nonasciiAs : nonasciiAs,
		wsHandleMode : wsHandleMode,
		emptyTagFormat : emptyTagFormat,
		elementPostFilter : elementPostFilter,
		elementPostFilterId : elementPostFilterId,
		cdataPostFilter : cdataPostFilter,
		cdataPostFilterId : cdataPostFilterId,
		sidSupportTagName : sidSupportTagName,
		sidSupportAttName : sidSupportAttName,
		isCheckWellFormed : isCheckWellFormed,
		isGerateLangInfo : isGerateLangInfo,
		preserveWsTags : preserveWsTags,
		embTags : embTags,
		transAttrTags : transAttrTags,
		contentInclTags : contentInclTags,
		cdataPostfilterTags : cdataPostfilterTags,
		entities : entities,
		processIns : processIns
	};
	// send for check
	sendAjax(obj, "isFilterValid", "isFilterValidCallback");
	
	isFilterValidCallback.obj = obj;
}

function isFilterValidCallback(data)
{
	if(data == 'true')
	{
		closePopupDialog("xmlRuleFilterDialog");
		if(saveXmlRuleFilter.edit)
		{
			sendAjax(isFilterValidCallback.obj, "updateXmlRuleFilter", "updateXmlRuleFilterCallback");
		}
		else
		{
			sendAjax(isFilterValidCallback.obj, "saveXmlRuleFilter", "saveXmlRuleFilterCallback");
		}
	}
	else if (data == 'name_exists')
	{
		alert(existFilterName + isFilterValidCallback.obj.filterName);
	}
	else
	{
		alert(data);
	}
}

function updateXmlRuleFilterCallback(data)
{
	var color = saveXmlRuleFilter.color;
	var filterId = saveXmlRuleFilter.filterId;
	var filter = saveXmlRuleFilter.filter;
	var topFilterId = saveXmlRuleFilter.topFilterId;
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = filterId;
		xrFilter.filterTableName = "xml_rule_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.convertHtmlEntity = isFilterValidCallback.obj.convertHtmlEntity;
		xrFilter.useXmlRule = isFilterValidCallback.obj.useXmlRule;
		xrFilter.extendedWhitespaceChars = isFilterValidCallback.obj.extendedWhitespaceChars;
		xrFilter.phConsolidationMode = isFilterValidCallback.obj.phConsolidationMode;
		xrFilter.phTrimMode = isFilterValidCallback.obj.phTrimMode;
		xrFilter.nonasciiAs = isFilterValidCallback.obj.nonasciiAs;
		xrFilter.wsHandleMode = isFilterValidCallback.obj.wsHandleMode;
		xrFilter.emptyTagFormat = isFilterValidCallback.obj.emptyTagFormat;
		xrFilter.preserveWsTags = isFilterValidCallback.obj.preserveWsTags;
		xrFilter.embTags = isFilterValidCallback.obj.embTags;
		xrFilter.transAttrTags = isFilterValidCallback.obj.transAttrTags;
		xrFilter.contentInclTags = isFilterValidCallback.obj.contentInclTags;
		xrFilter.cdataPostfilterTags = isFilterValidCallback.obj.cdataPostfilterTags;
		xrFilter.elementPostFilter = isFilterValidCallback.obj.elementPostFilter;
		xrFilter.elementPostFilterId = isFilterValidCallback.obj.elementPostFilterId;
		xrFilter.cdataPostFilter = isFilterValidCallback.obj.cdataPostFilter;
		xrFilter.cdataPostFilterId = isFilterValidCallback.obj.cdataPostFilterId;
		xrFilter.sidSupportTagName = isFilterValidCallback.obj.sidSupportTagName;
		xrFilter.sidSupportAttName = isFilterValidCallback.obj.sidSupportAttName;
		xrFilter.isCheckWellFormed = isFilterValidCallback.obj.isCheckWellFormed;
		xrFilter.isGerateLangInfo = isFilterValidCallback.obj.isGerateLangInfo;
		xrFilter.entities = isFilterValidCallback.obj.entities;
		xrFilter.processIns = isFilterValidCallback.obj.processIns;
		xrFilter.xmlRuleId = isFilterValidCallback.obj.xmlRuleId;
		xrFilter.xmlRules = saveXmlRuleFilter.xmlRules;
		xrFilter.companyId = companyId;
		xrFilter.secondFilterId = isFilterValidCallback.obj.secondFilterId;
		xrFilter.secondFilterTableName = isFilterValidCallback.obj.secondFilterTableName;
		var specialFilters = updateSpecialFilter(saveXmlRuleFilter.specialFilters, xrFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveXmlRuleFilterCallback(data)
{
	var color = saveXmlRuleFilter.color;
	var topFilterId = saveXmlRuleFilter.topFilterId;
	
	var filter = getFilterById(topFilterId);
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = data - 0;
		xrFilter.filterTableName = "xml_rule_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.convertHtmlEntity = isFilterValidCallback.obj.convertHtmlEntity;
		xrFilter.useXmlRule = isFilterValidCallback.obj.useXmlRule;
		xrFilter.extendedWhitespaceChars = isFilterValidCallback.obj.extendedWhitespaceChars;
		xrFilter.phConsolidationMode = isFilterValidCallback.obj.phConsolidationMode;
		xrFilter.phTrimMode = isFilterValidCallback.obj.phTrimMode;
		xrFilter.nonasciiAs = isFilterValidCallback.obj.nonasciiAs;
		xrFilter.wsHandleMode = isFilterValidCallback.obj.wsHandleMode;
		xrFilter.emptyTagFormat = isFilterValidCallback.obj.emptyTagFormat;
		xrFilter.preserveWsTags = isFilterValidCallback.obj.preserveWsTags;
		xrFilter.embTags = isFilterValidCallback.obj.embTags;
		xrFilter.transAttrTags = isFilterValidCallback.obj.transAttrTags;
		xrFilter.contentInclTags = isFilterValidCallback.obj.contentInclTags;
		xrFilter.cdataPostfilterTags = isFilterValidCallback.obj.cdataPostfilterTags;
		xrFilter.elementPostFilter = isFilterValidCallback.obj.elementPostFilter;
		xrFilter.elementPostFilterId = isFilterValidCallback.obj.elementPostFilterId;
		xrFilter.cdataPostFilter = isFilterValidCallback.obj.cdataPostFilter;
		xrFilter.cdataPostFilterId = isFilterValidCallback.obj.cdataPostFilterId;
		xrFilter.sidSupportTagName = isFilterValidCallback.obj.sidSupportTagName;
		xrFilter.sidSupportAttName = isFilterValidCallback.obj.sidSupportAttName;
		xrFilter.isCheckWellFormed = isFilterValidCallback.obj.isCheckWellFormed;
		xrFilter.isGerateLangInfo = isFilterValidCallback.obj.isGerateLangInfo;
		xrFilter.entities = isFilterValidCallback.obj.entities;
		xrFilter.processIns = isFilterValidCallback.obj.processIns;
		xrFilter.xmlRuleId = isFilterValidCallback.obj.xmlRuleId;
		xrFilter.companyId = companyId;
		xrFilter.xmlRules = saveXmlRuleFilter.xmlRules;
		xrFilter.secondFilterId = isFilterValidCallback.obj.secondFilterId;
		xrFilter.secondFilterTableName = isFilterValidCallback.obj.secondFilterTableName;
		filter.specialFilters.push(xrFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

XMLRuleFilter.prototype.getFormattedExtendWhiteSpace = function(oriStr)
{
	if (xmlFilter.isDefined(oriStr))
	{
		return oriStr.replace("\"", "&#34;");
	}
	
	return "";
}

XMLRuleFilter.prototype.saveConfiguredTag = function()
{
	var validate = new Validate();
	var tagName = new StringBuffer(document.getElementById("xmlRuleConfiguredTag_tag_name").value);
	if(validate.containsWhitespace(tagName) || tagName.trim() != tagName)
	{
		alert(jsTagName + " (" + tagName + ")"+ canNotContainWhiteSpace);
		return;
	}
	if(validate.isEmptyStr(tagName.trim()))
	{
		alert(emptyTagName);
		return;
	}
	
	if(validate.containSpecialChar(tagName.trim()))
	{
	    alert(invalidTagNameChar + invalidChars);
	    return;
	}
	
	if (xmlFilter.currentOption == xmlFilter.optionTransAttrTags
			&& (!xmlFilter.editItemTransAttributes || xmlFilter.editItemTransAttributes.length == 0))
	{
		alert(jsNonTransAttr);
		return;
	}
	
	var dialogId = "xmlRuleFilter_configured_tag_Dialog";
	var itemId = xmlFilter.editItemId;
	var attributes = xmlFilter.editItemAttributes;
	var enable = xmlFilter.editItemEnable;
	var transAttributes = xmlFilter.editItemTransAttributes;
	
	var item;
	
	if (xmlFilter.currentOption == xmlFilter.optionPreserveWsTags
			|| xmlFilter.currentOption == xmlFilter.optionEmbTags)
	{
		item = {itemid : itemId, enable : enable, tagName : tagName.trim(), attributes : attributes};
	}
	
	if (xmlFilter.currentOption == xmlFilter.optionTransAttrTags)
	{
		var transAttrSegRule = getRadioValue(fpForm.xmlRuleConfiguredTag_segRule);
		item = {itemid : itemId, enable : enable, tagName : tagName.trim(), attributes : attributes,
				transAttributes : transAttributes, transAttrSegRule : transAttrSegRule};
	}
	
	if (xmlFilter.currentOption == xmlFilter.optionContentInclTags)
	{
		var inclType = getRadioValue(fpForm.xmlRuleConfiguredTag_inclType);
		item = {itemid : itemId, enable : enable, tagName : tagName.trim(), attributes : attributes,
				inclType : inclType};
	}
	
	xmlFilter.addOneItemInCurrentOptions(item);
	xmlFilter.closeConfiguredTagDialog(dialogId);
}

XMLRuleFilter.prototype.handleCdataPostFilterAdd = function(oriObj)
{
	var resVelement = document.getElementById("xmlFilterCdatapostFilter_cond_res");
	var resV = new StringBuffer(resVelement.value);
	
	if (resV.trim() == "")
	{
		resVelement.value = "";
		return;
	}
	
	var itemId = xmlFilter.newItemId();
	var arrs = new Array();
	var aType = document.getElementById("xmlFilterCdatapostFilter_cond_type").value;
	var aOp = document.getElementById("xmlFilterCdatapostFilter_cond_Operation").value;
	
	var objj = {itemid : itemId, aType : aType, aOp : aOp, aValue : resV.trim()};
	arrs[0] = objj;
	
	xmlFilter.appendCdataConditions(document.getElementById("xmlFilterCdatapostFilter_cond_items"), arrs);
	xmlFilter.editItemCdataConditions[xmlFilter.editItemCdataConditions.length] = objj;
	
	resVelement.value = "";
}

XMLRuleFilter.prototype.handleCdataPostFilterRemove = function(oriObj)
{
	var selectObj = document.getElementById("xmlFilterCdatapostFilter_cond_items");
	var idArray = new Array();
	var idIndex = 0;
	for (var i = selectObj.length - 1; i>=0; i--)
	{
	    if (selectObj.options[i].selected)
	    {
	    	idArray[idIndex++] = selectObj.options[i].value;
	    	selectObj.remove(i);
	    }
	}
	
	var newItems = new Array();
	idIndex = 0;
	for(var i = 0; i < xmlFilter.editItemCdataConditions.length; i++)
	{
		if (!idArray.contains(xmlFilter.editItemCdataConditions[i].itemid))
		{
			newItems[idIndex++] = xmlFilter.editItemCdataConditions[i];
		}
	}
	
	xmlFilter.editItemCdataConditions = newItems;
}

XMLRuleFilter.prototype.saveCdataPostFilter = function()
{
	var validate = new Validate();
	var aName = new StringBuffer(document.getElementById("xmlFilterCdatapostFilterName").value);

	if(validate.isEmptyStr(aName.trim()))
	{
		document.getElementById("xmlFilterCdatapostFilterName").value = "";
		alert(emptyTagName);
		return;
	}
	
	var dialogId = "xmlRuleFilter_cdatapostfilter_Dialog";
	var itemId = xmlFilter.editItemId;
	var enable = xmlFilter.editItemEnable;
	var cdataConditions = xmlFilter.editItemCdataConditions;
	var _id_filtertable = document.getElementById("cdataPostFilter_filter").value;
	var _splitedIdTable = splitByFirstIndex(_id_filtertable, "-");
	var postFilterId = (_splitedIdTable) ? _splitedIdTable[0] : -2;
	var postFilterTableName = (_splitedIdTable) ? _splitedIdTable[1] : "";
	var translatable = document.getElementById("xmlFilterCdatapostFilter_trans").checked;
	
	var item;
	
	item = {itemid : itemId, enable : enable, aName : aName.trim(), cdataConditions : cdataConditions,
			postFilterId : postFilterId, postFilterTableName : postFilterTableName, translatable : translatable};
	
	xmlFilter.addOneItemInCurrentOptions(item);
	xmlFilter.closeConfiguredTagDialog(dialogId);
}

XMLRuleFilter.prototype.onEntityTypeChange = function()
{
	var eType = document.getElementById("xmlRuleFilter_configuredentity_Type").value;
	var isTxt = (eType == "0");
	xmlFilter.displayElement("xmlRuleFilter_configuredentity_txt_0", isTxt);
	xmlFilter.displayElement("xmlRuleFilter_configuredentity_txt_1", isTxt);
}

XMLRuleFilter.prototype.saveConfiguredEntity = function()
{
	var validate = new Validate();
	var aName = new StringBuffer(document.getElementById("xmlRuleFilter_configuredentity_EntityName").value);

	if(validate.isEmptyStr(aName.trim()))
	{
		document.getElementById("xmlRuleFilter_configuredentity_EntityName").value = "";
		alert(emptyTagName);
		return;
	}
	
	var dialogId = "xmlRuleFilter_configuredentity_Dialog";
	var itemId = xmlFilter.editItemId;
	var enable = xmlFilter.editItemEnable;
	var entityType = document.getElementById("xmlRuleFilter_configuredentity_Type").value;
	var isTxt = (entityType == "0");
	var entityCode = (isTxt) ? document.getElementById("xmlRuleFilter_configuredentity_EntityCode").value : -1;
	var saveAs = (isTxt) ? getRadioValue(fpForm.xmlRuleFilter_configuredentity_SaveAs) : -1;
	
	if (isTxt && !validate.isPositiveInteger(entityCode))
	{
		document.getElementById("xmlRuleFilter_configuredentity_EntityCode").value = entityCode;
		alert(jsInvalidEntityCode);
		return;
	}
	
	var item;
	item = {itemid : itemId, enable : enable, aName : aName.trim(), entityType : entityType,
			entityCode : entityCode, saveAs : saveAs};
	
	xmlFilter.addOneItemInCurrentOptions(item);
	xmlFilter.closeConfiguredTagDialog(dialogId);
}

XMLRuleFilter.prototype.saveProcessIns = function()
{
	var validate = new Validate();
	var aName = new StringBuffer(document.getElementById("xmlRuleFilter_pi_name").value);

	if(validate.isEmptyStr(aName.trim()))
	{
		document.getElementById("xmlRuleFilter_pi_name").value = "";
		alert(emptyTagName);
		return;
	}
	
	var dialogId = "xmlRuleFilter_pi_Dialog";
	var itemId = xmlFilter.editItemId;
	var enable = xmlFilter.editItemEnable;
	var handleType = document.getElementById("xmlRuleFilter_pi_Type").value;
	
	var item;
	item = {itemid : itemId, enable : enable, aName : aName.trim(), handleType : handleType};
	
	xmlFilter.addOneItemInCurrentOptions(item);
	xmlFilter.closeConfiguredTagDialog(dialogId);
}

XMLRuleFilter.prototype.addOneItemInCurrentOptions = function(item)
{
	if (xmlFilter.isDefined(item))
	{
		var i = 0;
		var objArray = xmlFilter.optionObjsMap[xmlFilter.currentOption];
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
			checkAllTagsInXmlFilter = false;
		}
		
		var maxPages = xmlFilter.getPageSize(objArray.length);
		document.getElementById("pageTotalSize").innerText = maxPages;
		
		var content = xmlFilter.generateXmlRulesContent(xmlFilter.currentOption, xmlFilter.currentPage);
		xmlFilter.refreshTagsContent(content);
	}
}

XMLRuleFilter.prototype.closeConfiguredTagDialog = function(dialogId)
{
	closePopupDialog(dialogId);
	document.getElementById("xmlRuleConfiguredTag_tag_name").value = "";
	document.getElementById("xmlRuleConfiguredTag_cond_attributes").innerHTML = "";
	document.getElementById("xmlRuleConfiguredTag_cond_attributes_item").value = "";
	document.getElementById("xmlRuleConfiguredTag_cond_attributes_Operation").selectedIndex = 0;
	document.getElementById("xmlRuleConfiguredTag_cond_attributes_res").value = "";
	
	document.getElementById("xmlRuleConfiguredTag_trans_attributes").innerHTML = "";
	document.getElementById("xmlRuleConfiguredTag_trans_attribute").value = "";
	
	xmlFilter.editItemId = -1;
	xmlFilter.editItemAttributes = new Array();
}

XMLRuleFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = xmlFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteXmlTagDialog");
	}
}

XMLRuleFilter.prototype.deleteCheckedTags = function()
{
	for(var i = 0; i < xmlFilter.availableOptions.length; i++)
    {
		var aoption = xmlFilter.availableOptions[i];
		var ruleObjects = xmlFilter.optionObjsMap[aoption];
        var checkBoxId = "delete_tags_" + aoption;
        var checkBoxObjs = document.getElementsByName(checkBoxId);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
        	var checkBoxObj = checkBoxObjs[j];
            if (checkBoxObj.checked)
            {
            	var itemId = checkBoxObj.getAttribute("tagValue");
            	var ruleObj = xmlFilter.getItemById(itemId, aoption);
            	
            	if (ruleObj && ruleObjects)
            	{
            		ruleObjects.removeData(ruleObj);
            	}
            }
        }
    }
	
	xmlFilter.setPageValue();
	
	closePopupDialog("deleteXmlTagDialog");
	xmlFilter.switchRules(document.getElementById("xmlFilterRulesSection"));
}