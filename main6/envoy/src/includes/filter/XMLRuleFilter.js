var xmlFilter = new XMLRuleFilter();
var checkAllTagsInXmlFilter = false;
var xmlTagsContentTable = "<table id='xmlTagsContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>"; 
var fontTagS = "<font class='specialFilter_dialog_label'>";
var fontTagE = "</font>"
var imgYes = "<IMG SRC='/globalsight/images/checkmark.gif' HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3></IMG>";
	
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
	this.optionInternalTag = "7";
	this.optionSrcCmtXmlComment = "8";
	this.optionSrcCmtXmlTag = "9";
	
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
	this.optionNameMap[this.optionInternalTag] = jsInternalTag;
	this.optionNameMap[this.optionSrcCmtXmlComment] = jsSrcCmtXmlComment;
	this.optionNameMap[this.optionSrcCmtXmlTag] = jsSrcCmtXmlTag;
	
	this.optionObjsMap = new Object();
	this.availableOptions = [this.optionContentInclTags, this.optionEmbTags, this.optionTransAttrTags,
	                         this.optionPreserveWsTags, this.optionCDataPostFilterTags, this.optionEntities,
	                         this.optionProcessIns, this.optionInternalTag, this.optionSrcCmtXmlComment,
	                         this.optionSrcCmtXmlTag];
	this.checkedItemIds = new Array();
	
	this.tagsEveryPage = 10;
	this.currentPage = 0;
	this.sortOrder = "asc";
	this.sortColumnIndex = 1;
	this.currentPage = 0;
	this.currentOption = this.optionContentInclTags;
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
		
		// Internal Tag
		var internalTag = this.filter.internalTag;
		xmlFilter.optionObjsMap[this.optionInternalTag] = JSON.parse(internalTag);
		
		// Source Comment Type
		var srcCmtXmlComment = this.filter.srcCmtXmlComment;
		xmlFilter.optionObjsMap[this.optionSrcCmtXmlComment] = JSON.parse(srcCmtXmlComment);
		
		var srcCmtXmlTag = this.filter.srcCmtXmlTag;
		xmlFilter.optionObjsMap[this.optionSrcCmtXmlTag] = JSON.parse(srcCmtXmlTag);
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
		xmlFilter.optionObjsMap[this.optionInternalTag] = new Array();
		xmlFilter.optionObjsMap[this.optionSrcCmtXmlComment] = new Array();
		xmlFilter.optionObjsMap[this.optionSrcCmtXmlTag] = new Array();
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

XMLRuleFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
// alert("filterId :" + filterId + "\n" + "topFilterId :" + topFilterId);
	this.initOptionMap(this.filter);
	xmlFilter.currentPage=0;
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	
	str.append("<input type='text' style='width:340px' id='xmlRuleFilterName' maxlength='"+maxFilterNameLength+"' value='" + this.filter.filterName + "'></input>");
	str.append("<br/>");
	
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	
	str.append("<textarea rows='4' cols='40' id='xmlRuleDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br/>");
	
	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsXmlRule + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.generateRulesContent(this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsConvertHTMLEntity + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckConvertHtmlEntity = (this.filter.convertHtmlEntity) ? "checked":"";
	str.append("<input id='isEnableConvertHtmlEntity' type='checkbox' name='convertHtmlEntity' value='"+this.filter.convertHtmlEntity+"' "+isCheckConvertHtmlEntity+"></input>");
	str.append("</td>");
	str.append("</tr>");
	
	var tempV = xmlFilter.getFormattedExtendWhiteSpace(this.filter.extendedWhitespaceChars);
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExWhitespaceChars + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='exSpaceChars' type='text' name='exSpaceChars' value=\""+tempV+"\" onchange='decodeExtendWhitespace()'></input></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsPhConsolidation + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generatePhConsolidation(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsPhTrimming + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generatePhTrimming(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsNonasciiAs + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateNonasciiAs(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsWhitespaceHandling + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateWhitespaceHandling(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsEmptyTagFormat + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateEmptyTagFormat(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsElementPostFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateElementPostFilter(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsCDataPostFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateCdataPostFilter(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsSidSupport + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateSidSupport(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsCheckWellFormed + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateCheckWellFormed(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsGerateLangInfo + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateGeLangInfo(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName, this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(this.generateXmlRulesTable(this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('xmlRulePopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	saveXmlRuleFilter.edit = true;
	saveXmlRuleFilter.filterId = filterId;
	saveXmlRuleFilter.color = color;
	saveXmlRuleFilter.filter = this.filter;
	saveXmlRuleFilter.specialFilters = specialFilters;
	saveXmlRuleFilter.topFilterId = topFilterId;
	saveXmlRuleFilter.xmlRules = this.filter.xmlRules;
}

XMLRuleFilter.prototype.generateDiv = function(topFilterId, color)
{
	this.initOptionMap();
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	
	str.append("<td><input type='text' style='width:450px' id='xmlRuleFilterName' maxlength='"+maxFilterNameLength+"' value='Xml Filter'></input>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	
	str.append("<td><textarea style='width:450px' rows='4' id='xmlRuleDesc' name='desc'></textarea>");
	str.append("</td></tr></table>");
	
	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsXmlRule + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var filter = getFilterById(topFilterId);
	var xmlRules = null;
	if(filter.specialFilters && filter.specialFilters.length > 0)
	{
		xmlRules = filter.specialFilters[0].xmlRules;
	}
	else
	{
		var tmpFilter = new Object();
		tmpFilter.xmlRules = new Array();
		xmlRules = tmpFilter.xmlRules;
	}
	str.append(this.generateRulesContent());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsConvertHTMLEntity + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='isEnableConvertHtmlEntity' type='checkbox' name='convertHtmlEntity' value='false'"+"></input>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExWhitespaceChars + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='exSpaceChars' type='text' name='exSpaceChars' onchange='decodeExtendWhitespace()'></input></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsPhConsolidation + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generatePhConsolidation() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsPhTrimming + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generatePhTrimming() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsNonasciiAs + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateNonasciiAs() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsWhitespaceHandling + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateWhitespaceHandling() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsEmptyTagFormat + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateEmptyTagFormat() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsElementPostFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateElementPostFilter() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsCDataPostFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateCdataPostFilter() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsSidSupport + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateSidSupport() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsCheckWellFormed + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateCheckWellFormed() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsGerateLangInfo + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateGeLangInfo() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(this.generateXmlRulesTable());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('xmlRulePopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveXmlRuleFilter.edit = false;
	saveXmlRuleFilter.topFilterId = topFilterId;
	saveXmlRuleFilter.color = color;
	saveXmlRuleFilter.xmlRules = xmlRules;
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
	var internalTag = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionInternalTag]);
	var baseFilterId = document.getElementById("xml_rule_filter_baseFilterSelect").value;
	var srcCmtXmlComment = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionSrcCmtXmlComment]);
	var srcCmtXmlTag = JSON.stringify(xmlFilter.optionObjsMap[xmlFilter.optionSrcCmtXmlTag]);
	
	alertUserBaseFilter(baseFilterId);

	var obj = {
		isNew : isNew,
		filterTableName : "xml_rule_filter",
		filterName : filterName,
		filterDesc : filterDesc,
		xmlRuleId : xmlRuleId,
		filterId : saveXmlRuleFilter.filterId,
		companyId : companyId,
		convertHtmlEntity : convertHtmlEntity,
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
		processIns : processIns,
		internalTag : internalTag,
		srcCmtXmlComment : srcCmtXmlComment,
		srcCmtXmlTag : srcCmtXmlTag,
		baseFilterId : baseFilterId
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
	else if(data == 'failed')
	{
		closePopupDialog("xmlRuleFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
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
		xrFilter.internalTag = isFilterValidCallback.obj.internalTag;
		xrFilter.srcCmtXmlComment = isFilterValidCallback.obj.srcCmtXmlComment;
		xrFilter.srcCmtXmlTag = isFilterValidCallback.obj.srcCmtXmlTag;
		xrFilter.baseFilterId = isFilterValidCallback.obj.baseFilterId;
		xrFilter.xmlRuleId = isFilterValidCallback.obj.xmlRuleId;
		xrFilter.xmlRules = saveXmlRuleFilter.xmlRules;
		xrFilter.companyId = companyId;
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
		xrFilter.internalTag = isFilterValidCallback.obj.internalTag;
		xrFilter.srcCmtXmlComment = isFilterValidCallback.obj.srcCmtXmlComment;
		xrFilter.srcCmtXmlTag = isFilterValidCallback.obj.srcCmtXmlTag;
		xrFilter.baseFilterId = isFilterValidCallback.obj.baseFilterId;
		xrFilter.xmlRuleId = isFilterValidCallback.obj.xmlRuleId;
		xrFilter.companyId = companyId;
		xrFilter.xmlRules = saveXmlRuleFilter.xmlRules;
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

XMLRuleFilter.prototype.generateRulesContent = function (filter)
{
	var xmlRules = existXmlRules;
	var str = new StringBuffer("<select id='xmlRuleSelect' class='xml_filter_select'>");
	str.append("<option value='-1'>" + jsChoose + "</option>");
	if(xmlRules){
		for(var i = 0; i < xmlRules.length; i++)
		{
			var xmlRule = xmlRules[i];
			var selected = "";
			if(filter && filter.xmlRuleId == xmlRule.xmlRuleId)
			{
				selected = "selected";
			}
			str.append("<option value='"+xmlRule.xmlRuleId+"' "+selected+">"+xmlRule.xmlRuleName+"</option>");
		}
	}
	str.append("</select>");
	return str.toString();
}

XMLRuleFilter.prototype.generatePhConsolidation = function (filter)
{
	var str = new StringBuffer("<select id='phConsolidateMode' class='xml_filter_select'>");
	str.append("<option value='1'" + ((filter && filter.phConsolidationMode == "1") ? " selected" : "") + ">" + jsDonotConsolidation + "</option>");
	str.append("<option value='2'" + ((filter && filter.phConsolidationMode == "2") ? " selected" : "") + ">" + jsDoPhConsolidation + "</option>");
	str.append("<option value='3'" + ((filter && filter.phConsolidationMode == "3") ? " selected" : "") + ">" + jsDoPhConsolidationIgnoreSpace + "</option>");
	str.append("</select>");
	return str.toString();
}

XMLRuleFilter.prototype.generatePhTrimming = function (filter)
{
	var str = new StringBuffer("<select id='phTrimMode' class='xml_filter_select'>");
	str.append("<option value='1'" + ((filter && filter.phTrimMode == "1") ? " selected" : "") + ">" + jsPhTrimDonot + "</option>");
	str.append("<option value='2'" + ((filter && filter.phTrimMode == "2") ? " selected" : "") + ">" + jsPhTrimDo + "</option>");
	str.append("</select>");
	return str.toString();
}

XMLRuleFilter.prototype.generateNonasciiAs = function (filter)
{
	var str = new StringBuffer("");
		
	str.append("<nobr><input value='1' type='radio' name='nonasciiAs'" + ((filter) ? (( filter.nonasciiAs == "1") ? " checked" : "") : " checked") + ">" 
				+ fontTagS + jsNonasciiAsCharacter +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='2' type='radio' name='nonasciiAs'" + ((filter && filter.nonasciiAs == "2") ? " checked" : "") + ">" 
				+ fontTagS + jsNonasciiAsEntity +fontTagE);
	return str.toString();
}

XMLRuleFilter.prototype.generateWhitespaceHandling = function (filter)
{
	var str = new StringBuffer("");
	str.append("<nobr><input value='1' type='radio' name='wsHandleMode'" + ((filter && filter.wsHandleMode == "1") ? " checked" : "") + ">" 
			+ fontTagS + jsWsHandlingCollapse +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='2' type='radio' name='wsHandleMode'" + ((filter) ? (( filter.wsHandleMode == "2") ? " checked" : "") : " checked") + ">" 
			+ fontTagS + jsWsHandlingPreserve +fontTagE);
	return str.toString();
}

XMLRuleFilter.prototype.generateEmptyTagFormat = function (filter)
{
	var str = new StringBuffer("");
	str.append("<nobr><input value='1' type='radio' name='emptyTagFormat'" + ((filter && filter.emptyTagFormat == "1") ? " checked" : "") + ">" 
			+ fontTagS + jsEmptyTagFormatOpen +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='2' type='radio' name='emptyTagFormat'" + ((filter && filter.emptyTagFormat == "2") ? " checked" : "") + ">"  
			+ fontTagS + jsEmptyTagFormatClose +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='0' type='radio' name='emptyTagFormat'" + ((filter) ? (( filter.emptyTagFormat == "0") ? " checked" : "") : " checked") + ">" 
			+ fontTagS + jsEmptyTagFormatPreserve +fontTagE);
	return str.toString();
}

XMLRuleFilter.prototype.generateElementPostFilter = function (filter)
{
	var str = new StringBuffer("<select id='elementPostFilter' class='xml_filter_select'>");
	str.append("<option value='-1'" + ((filter && filter.elementPostFilter == "-1") ? " selected" : "") + ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>");
	
	str.append(xmlFilter.generateAvailableFilterOptions(filter, "elementPostFilterId", "elementPostFilter"));
	str.append("</select>");
	return str.toString();
}

XMLRuleFilter.prototype.generateCdataPostFilter = function (filter)
{
	var str = new StringBuffer("<select id='cdataPostFilter' class='xml_filter_select'>");
	str.append("<option value='-1'" + ((filter && filter.elementPostFilter == "-1") ? " selected" : "") + ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>");
	
	str.append(xmlFilter.generateAvailableFilterOptions(filter, "cdataPostFilterId", "cdataPostFilter"));
	str.append("</select>");
	return str.toString();
}

XMLRuleFilter.prototype.generateSidSupport = function (filter)
{
	var str = new StringBuffer("<table><tr><td>");
	str.append(fontTagS + jsSidSupportTagName + fontTagE + ":");
	str.append("</td><td><input maxlength='64'  type='text' onkeypress='if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}' id='sidSupportTagNameEle' value='");
	str.append((filter && filter.sidSupportTagName)? filter.sidSupportTagName : "");
	str.append("'></td></tr><tr><td>");
	str.append(fontTagS + jsSidSupportAttName + fontTagE + ":");
	str.append("</td><td><input maxlength='64'  type='text' onkeypress='if (event.keyCode == 13) { event.cancelBubble=true; event.returnValue=false; return false;}' id='sidSupportAttNameEle' value='");
	str.append((filter && filter.sidSupportAttName)? filter.sidSupportAttName : "");
	str.append("'></td></tr></table>");
	return str.toString();
}

XMLRuleFilter.prototype.generateCheckWellFormed = function (filter)
{
	var str = new StringBuffer("");
	var checkedStr = (filter && filter.isCheckWellFormed) ? "checked":"";
	str.append("<input id='isEnableCheckWellFormed' type='checkbox' name='checkWellFormed' "+checkedStr+"></input>");
	return str.toString();
}

XMLRuleFilter.prototype.generateGeLangInfo = function (filter)
{
	var str = new StringBuffer("");
	var checkedStr = (filter && filter.isGerateLangInfo) ? "checked":"";
	str.append("<input id='isEnableGerateLangInfo' type='checkbox' name='gerateLangInfo' "+checkedStr+"></input>");
	return str.toString();
}

XMLRuleFilter.prototype.generateXmlRulesTable = function (filter)
{
	var str = new StringBuffer("");
	str.append("<table cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append("<tr><td>");
	str.append("<div id='xmlFilterRulesContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='xmlFilterRulesSection' onchange='xmlFilter.switchRules(this)'>");
	for(var i = 0; i < xmlFilter.availableOptions.length; i++)
	{
		var optionV = xmlFilter.availableOptions[i];
		str.append("<option value='" + optionV + "'");
		str.append(optionV == xmlFilter.currentOption ? " selected " : "");
		str.append(">" + this.optionNameMap[optionV] + "</option>");
	}
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "...' onclick='xmlFilter.addTag()'></input>");
	str.append("<input type='button' value='" + jsDelete + "...' onclick='xmlFilter.deleteTag()' id='cmdDeleteTag'></input>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	str.append("<div id='xmlTagsContent'>");
	str.append(xmlTagsContentTable);
	
	if (filter)
	{
		str.append(xmlFilter.generateXmlRulesContent(xmlFilter.currentOption, 0));
	}
	else
	{
		str.append(xmlFilter.generateXmlRulesContent(xmlFilter.currentOption));
	}
	
	str.append("</table>");
	str.append("</div>");
	str.append("<div>");
	
	var pageTotalSize = filter ? xmlFilter.getPageSize(xmlFilter.optionObjsMap[xmlFilter.currentOption].length) : 1;
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=xmlFilter.prePage()>" + jsPrevious + "</a>");
	str.append("|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=xmlFilter.nextPage()>" + jsNext + "</a>");
	str.append("<input id='pageCountXmlFilter' size=2 type='text' value="+((filter) ? xmlFilter.currentPage+1 : 1)+" ></input>");
	str.append(" / <span id='pageTotalSize' class='standardText'>" + pageTotalSize + "</span> ");
	str.append("<input type='button' value='" + jsGo + "' onclick=xmlFilter.goToPage()></input>");
	str.append("</div>");
	
	return str.toString();
}

XMLRuleFilter.prototype.closeAllTagPopup = function()
{
	closePopupDialog("xmlRuleFilter_configured_tag_Dialog");
	closePopupDialog("xmlRuleFilter_cdatapostfilter_Dialog");
	closePopupDialog("xmlRuleFilter_configuredentity_Dialog");
	closePopupDialog("xmlRuleFilter_pi_Dialog");
}

XMLRuleFilter.prototype.addTag = function(radioId)
{
	var dialogId = "xmlRuleFilter_configured_tag_Dialog";
	var isEdit = (radioId) ? true : false;
	var useTagsDiv = false;
	
	if (xmlFilter.currentOption == xmlFilter.optionPreserveWsTags
			|| xmlFilter.currentOption == xmlFilter.optionEmbTags
			|| xmlFilter.currentOption == xmlFilter.optionTransAttrTags
			|| xmlFilter.currentOption == xmlFilter.optionContentInclTags
			|| xmlFilter.currentOption == xmlFilter.optionInternalTag
			|| xmlFilter.currentOption == xmlFilter.optionSrcCmtXmlTag)
	{
		useTagsDiv = true;
	}
	
	var useCdataPostFilter = (xmlFilter.currentOption == xmlFilter.optionCDataPostFilterTags);
	var useEntityDiv = (xmlFilter.currentOption == xmlFilter.optionEntities);
	var usePIDiv = (xmlFilter.currentOption == xmlFilter.optionProcessIns);
	var useInternalTagDiv = (xmlFilter.currentOption == xmlFilter.optionInternalTag);
	var useSrcCmtXmlCommentDiv = (xmlFilter.currentOption == xmlFilter.optionSrcCmtXmlComment);
	
	xmlFilter.closeAllTagPopup();
	if(useTagsDiv)
	{
		dialogId = "xmlRuleFilter_configured_tag_Dialog";
		document.getElementById("xmlRuleConfiguredTag_title").innerHTML = (isEdit ? jsEditConfiguredTag : jsAddConfiguredTag);
	}
	
	if (useCdataPostFilter)
	{
		dialogId = "xmlRuleFilter_cdatapostfilter_Dialog";
		document.getElementById("xmlRuleCdataPostFilter_title").innerHTML = (isEdit ? jsEditCdataPostFilter : jsAddCdataPostFilter);
	}
	
	if (useEntityDiv)
	{
		dialogId = "xmlRuleFilter_configuredentity_Dialog";
		document.getElementById("xmlRuleFilter_configuredentity_title").innerHTML = (isEdit ? jsEditConfiguredEntity : jsAddConfiguredEntity);
	}
	
	if (usePIDiv)
	{
		dialogId = "xmlRuleFilter_pi_Dialog";
		document.getElementById("xmlRuleFilter_pi_title").innerHTML = (isEdit ? jsEditProcessIns : jsAddProcessIns);
	}
	
	if (useSrcCmtXmlCommentDiv)
	{
		dialogId = "xmlRuleFilter_srcCmtXmlComment_Dialog";
		document.getElementById("xmlRuleFilter_srcCmtXmlComment_title").innerHTML = jsSrcCmtXmlComment;
	}
	
	showPopupDialog(dialogId);
	xmlFilter.displayElement("xmlRuleConfiguredTag_trans_attr_0", false);
	xmlFilter.displayElement("xmlRuleConfiguredTag_trans_attr_1", false);
	xmlFilter.displayElement("xmlRuleConfiguredTag_content_incl_0", false);
	xmlFilter.displayElement("xmlRuleConfiguredTag_content_from_attribute", false);
	
	var editId = isEdit ? xmlFilter.getItemId(radioId) : -1;
	var editItem = isEdit ? xmlFilter.getItemById(editId, xmlFilter.currentOption) : new Object();
	xmlFilter.editItemId = (isEdit? editItem.itemid : xmlFilter.newItemId());
	xmlFilter.editItemEnable = (isEdit? editItem.enable : false);
	
	if (useTagsDiv)
	{
		var tName = isEdit ? editItem.tagName : "";
		
		xmlFilter.editItemAttributes = ((isEdit && editItem.attributes) ? xmlFilter.cloneObject(editItem.attributes) : new Array());
		xmlFilter.appendAttributeOptions(document.getElementById("xmlRuleConfiguredTag_cond_attributes"), isEdit ? editItem.attributes : new Array(), true);
		document.getElementById("xmlRuleConfiguredTag_tag_name").value = tName;
		
		if (xmlFilter.currentOption == xmlFilter.optionTransAttrTags)
		{
			xmlFilter.displayElement("xmlRuleConfiguredTag_trans_attr_0", true);
			//xmlFilter.displayElement("xmlRuleConfiguredTag_trans_attr_1", true);
			xmlFilter.editItemTransAttributes = ((isEdit && editItem.transAttributes) ? xmlFilter.cloneObject(editItem.transAttributes) : new Array());
			xmlFilter.editItemTransAttrSegRule = ((isEdit && editItem.transAttrSegRule) ? xmlFilter.cloneObject(editItem.transAttrSegRule) : 2);
			
			xmlFilter.appendAttributeOptions(document.getElementById("xmlRuleConfiguredTag_trans_attributes"), isEdit ? editItem.transAttributes : new Array(), true);
			eval("document.getElementById('xmlRuleConfiguredTag_segRule_" + xmlFilter.editItemTransAttrSegRule + "').checked = true;");
		}
		
		if (xmlFilter.currentOption == xmlFilter.optionContentInclTags)
		{
			xmlFilter.displayElement("xmlRuleConfiguredTag_content_incl_0", true);
			xmlFilter.editItemInclType = ((isEdit && editItem.inclType) ? xmlFilter.cloneObject(editItem.inclType) : 1);
			eval("document.getElementById('xmlRuleConfiguredTag_inclType_" + xmlFilter.editItemInclType + "').checked = true;");
		}
		
		if (xmlFilter.currentOption == xmlFilter.optionSrcCmtXmlTag)
		{
			xmlFilter.displayElement("xmlRuleConfiguredTag_content_from_attribute", true);
			xmlFilter.editItemFromAttribute = (isEdit && editItem.fromAttribute) ? true : false;
			document.getElementById('xmlRuleConfiguredTag_attributeName').value = "";
			if (xmlFilter.editItemFromAttribute)
			{
				document.getElementById('xmlRuleConfiguredTag_fromAttribute').checked = true;
				document.getElementById('xmlRuleConfiguredTag_fromTagContent').checked = false;
				document.getElementById('xmlRuleConfiguredTag_attributeName').value = editItem.attributeName;
			}
			else
			{
				document.getElementById('xmlRuleConfiguredTag_fromAttribute').checked = false;
				document.getElementById('xmlRuleConfiguredTag_fromTagContent').checked = true;
			}
			
			xmlFilter.onFromAttributeClick();
		}
	}
	
	if (useCdataPostFilter)
	{
		var aName = isEdit ? editItem.aName : "";
		document.getElementById("xmlFilterCdatapostFilterName").value = aName;
		xmlFilter.editItemCdataConditions = ((isEdit && editItem.cdataConditions) ? xmlFilter.cloneObject(editItem.cdataConditions) : new Array());
		xmlFilter.appendCdataConditions(document.getElementById("xmlFilterCdatapostFilter_cond_items"), isEdit ? xmlFilter.editItemCdataConditions : new Array(), true);
		xmlFilter.editItemPostFilterId  = ((isEdit && xmlFilter.isDefined(editItem.postFilterId)) ? xmlFilter.cloneObject(editItem.postFilterId) : -1);
		xmlFilter.editItemPostFilterTableName  = ((isEdit && editItem.postFilterTableName) ? xmlFilter.cloneObject(editItem.postFilterTableName) : "");
		xmlFilter.editItemTranslatable = ((isEdit && xmlFilter.isDefined(editItem.translatable)) ? editItem.translatable : true);
		
		xmlFilter.generatePostFiltersForPopup(xmlFilter.editItemPostFilterId, xmlFilter.editItemPostFilterTableName);
		document.getElementById("xmlFilterCdatapostFilter_trans").checked = xmlFilter.editItemTranslatable;
	}
	
	if (useEntityDiv)
	{
		var aName = isEdit ? editItem.aName : "";
		document.getElementById("xmlRuleFilter_configuredentity_EntityName").value = aName;
		var entityType = isEdit ? editItem.entityType : 0;
		var isTxt = (entityType == 0);
		xmlFilter.displayElement("xmlRuleFilter_configuredentity_txt_0", isTxt);
		xmlFilter.displayElement("xmlRuleFilter_configuredentity_txt_1", isTxt);
		document.getElementById("xmlRuleFilter_configuredentity_Type").value = entityType;
		
		document.getElementById("xmlRuleFilter_configuredentity_EntityCode").value = (isTxt && isEdit) ? editItem.entityCode : "";
		var saveAs = (isTxt && isEdit) ? editItem.saveAs : 0;
		eval("document.getElementById('xmlRuleFilter_configuredentity_SaveAs_" + saveAs + "').checked = true;");
	}
	
	if (usePIDiv)
	{
		var aName = isEdit ? editItem.aName : "";
		document.getElementById("xmlRuleFilter_pi_name").value = aName;
		var handleType =  isEdit ? editItem.handleType : 0;
		document.getElementById("xmlRuleFilter_pi_Type").value = handleType;
		
		xmlFilter.editItemPiTransAttributes = ((isEdit && editItem.piTransAttributes) ? xmlFilter.cloneObject(editItem.piTransAttributes) : new Array());
		xmlFilter.appendAttributeOptions(document.getElementById("xmlRuleFilter_pi_trans_attributes"), isEdit ? editItem.piTransAttributes : new Array(), true);
		xmlFilter.handlePiTypeChange();
	}
	
	if (useSrcCmtXmlCommentDiv)
	{
		var aName = isEdit ? editItem.aName : "";
		document.getElementById("xmlRuleFilter_srcCmtXmlComment_name").value = aName;
		var isChecked =  isEdit ? editItem.isRE : false;
		document.getElementById("xmlRuleFilter_srcCmtXmlComment_isRE").checked = isChecked;
	}
}

XMLRuleFilter.prototype.onFromAttributeClick = function()
{
	if (document.getElementById('xmlRuleConfiguredTag_fromAttribute').checked)
	{
		document.getElementById('xmlRuleConfiguredTag_attributeName').disabled = false;
	}
	else
	{
		document.getElementById('xmlRuleConfiguredTag_attributeName').value = "";
		document.getElementById('xmlRuleConfiguredTag_attributeName').disabled = true;
	}
}

XMLRuleFilter.prototype.generatePostFiltersForPopup = function(filterId, filterTableName)
{
	var _filter = new Object();
	_filter.postFilterId = filterId;
	_filter.postFilterTableName = filterTableName;
	var str = new StringBuffer("<select id='cdataPostFilter_filter' class='xml_filter_select'>");
	str.append("<option value='-1'" + ((filterId && filterId.cdataPostFilter == "-1") ? " selected" : "") + ">" + jsPostFilterNone + "</option>");
	str.append(xmlFilter.generateAvailableFilterOptions(_filter, "postFilterId", "postFilterTableName"));
	str.append("</select>");
	var selectFilters = document.getElementById("xmlFilterCdatapostFilter_filter_c");
	selectFilters.innerHTML = str.toString();
}

XMLRuleFilter.prototype.displayElement = function(elemId, display)
{
	if (elemId)
	{
		var element = document.getElementById(elemId);
		if (element)
		{
			element.style.display = (display) ? "" : "none";
		}
	}
}

XMLRuleFilter.prototype.cloneObject = function(oriObj)
{
	var txt = JSON.stringify(oriObj);
	return JSON.parse(txt);
}

XMLRuleFilter.prototype.alertObject = function(obj)
{
	var txt = JSON.stringify(obj);
	return alert(txt);
}

XMLRuleFilter.prototype.appendCdataConditions = function(selectElement, conditions, cleanFirst)
{
	if (xmlFilter.isDefined(selectElement))
	{
		if (xmlFilter.isDefined(cleanFirst) && cleanFirst)
		{
			selectElement.innerHTML = "";
		}

		if (xmlFilter.isDefined(conditions) && conditions.length > 0)
		{
		for(var i = 0; i < conditions.length; i++)
		{
			var cond = conditions[i];
			var elOptNew = document.createElement('option');
			elOptNew.text = xmlFilter.getConditionString(cond);
			elOptNew.value = cond.itemid;
			
			try
			{
				selectElement.add(elOptNew, null);
			}
			catch(ex)
			{
				selectElement.add(elOptNew);
			}
		}
		}
	}
}

XMLRuleFilter.prototype.appendAttributeOptions = function(selectElement, attributes, cleanFirst)
{
	if (selectElement)
	{
		if (cleanFirst)
		{
			selectElement.innerHTML = "";
		}
		
		if (attributes && attributes.length > 0)
		{
			for(var i = 0; i < attributes.length; i++)
			{
				var attr = attributes[i];
				var elOptNew = document.createElement('option');
				elOptNew.text = (attr.aOp)? xmlFilter.getAttributeString(attr) : attr.aName;
				elOptNew.value = attr.itemid;
				
				try
				{
					selectElement.add(elOptNew, null);
				}
				catch(ex)
				{
					selectElement.add(elOptNew);
				}
			}
		}
	}
}

XMLRuleFilter.prototype.getItemById = function(itemId, optionValue)
{
	var item;
	var items = xmlFilter.optionObjsMap[optionValue];
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

XMLRuleFilter.prototype.handleConfiguredTagAdd = function()
{
	var attName = new StringBuffer(document.getElementById("xmlRuleConfiguredTag_cond_attributes_item").value);
	var trimName = attName.trim();
	var attOp = document.getElementById("xmlRuleConfiguredTag_cond_attributes_Operation").value;
	var attValue = document.getElementById("xmlRuleConfiguredTag_cond_attributes_res").value;
	
	if (trimName == "")
	{
		document.getElementById("xmlRuleConfiguredTag_cond_attributes_item").value = "";
		return;
	}
	
	var isExisted = false;
	for(var i = 0; i < xmlFilter.editItemAttributes.length; i++)
	{
		var existAtt = xmlFilter.editItemAttributes[i];
		if (existAtt.aName == trimName && existAtt.aOp == attOp && existAtt.aValue == attValue)
		{
			isExisted = true;
			break;
		}
	}
	
	if (!isExisted)
	{
		var attId = xmlFilter.newItemId();
		var attris = new Array();
		var att = {itemid : attId, aName : trimName, aOp : attOp, aValue : attValue};
		attris[0] = att;
		
		xmlFilter.appendAttributeOptions(document.getElementById("xmlRuleConfiguredTag_cond_attributes"), attris);
		xmlFilter.editItemAttributes[xmlFilter.editItemAttributes.length] = att;
	}
	
	document.getElementById("xmlRuleConfiguredTag_cond_attributes_item").value = "";
	document.getElementById("xmlRuleConfiguredTag_cond_attributes_Operation").selectedIndex = 0;
	document.getElementById("xmlRuleConfiguredTag_cond_attributes_res").value = "";
}

XMLRuleFilter.prototype.handleConfiguredTagRemove = function()
{
	var selectObj = document.getElementById("xmlRuleConfiguredTag_cond_attributes");
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
	
	var newItemAttrs = new Array();
	idIndex = 0;
	for(var i = 0; i < xmlFilter.editItemAttributes.length; i++)
	{
		if (!idArray.contains(xmlFilter.editItemAttributes[i].itemid))
		{
			newItemAttrs[idIndex++] = xmlFilter.editItemAttributes[i];
		}
	}
	
	xmlFilter.editItemAttributes = newItemAttrs;
}

XMLRuleFilter.prototype.handlePiTypeChange = function()
{
	var eType = document.getElementById("xmlRuleFilter_pi_Type").value;
	var isTrans = (eType == "3");
	xmlFilter.displayElement("xmlRuleFilter_pi_trans_attr_0", isTrans);
}

XMLRuleFilter.prototype.handlePiTransAttrAdd = function(oriObj)
{
	var attNameElement = document.getElementById("xmlRuleFilter_pi_trans_attribute");
	var attName = new StringBuffer(attNameElement.value);
	var trimName = attName.trim();
	
	if (trimName == "")
	{
		attNameElement.value = "";
		return;
	}
	
	var isExisted = false;
	for(var i = 0; i < xmlFilter.editItemPiTransAttributes.length; i++)
	{
		var existAtt = xmlFilter.editItemPiTransAttributes[i];
		if (existAtt.aName == trimName)
		{
			isExisted = true;
			break;
		}
	}
	
	if (!isExisted)
	{
		var attId = xmlFilter.newItemId();
		var attris = new Array();
		var att = {itemid : attId, aName : trimName};
		attris[0] = att;
		
		xmlFilter.appendAttributeOptions(document.getElementById("xmlRuleFilter_pi_trans_attributes"), attris);
		xmlFilter.editItemPiTransAttributes[xmlFilter.editItemPiTransAttributes.length] = att;
	}
	
	attNameElement.value = "";
}

XMLRuleFilter.prototype.handlePiTransAttrRemove = function(oriObj)
{
	var selectObj = document.getElementById("xmlRuleFilter_pi_trans_attributes");
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
	
	var newItemAttrs = new Array();
	idIndex = 0;
	for(var i = 0; i < xmlFilter.editItemPiTransAttributes.length; i++)
	{
		if (!idArray.contains(xmlFilter.editItemPiTransAttributes[i].itemid))
		{
			newItemAttrs[idIndex++] = xmlFilter.editItemPiTransAttributes[i];
		}
	}
	
	xmlFilter.editItemPiTransAttributes = newItemAttrs;
}

XMLRuleFilter.prototype.handleTransAttrAdd = function(oriObj)
{
	var attNameElement = document.getElementById("xmlRuleConfiguredTag_trans_attribute");
	var attName = new StringBuffer(attNameElement.value);
	var trimName = attName.trim();
	
	if (trimName == "")
	{
		attNameElement.value = "";
		return;
	}
	
	var isExisted = false;
	for(var i = 0; i < xmlFilter.editItemTransAttributes.length; i++)
	{
		var existAtt = xmlFilter.editItemTransAttributes[i];
		if (existAtt.aName == trimName)
		{
			isExisted = true;
			break;
		}
	}
	
	if (!isExisted)
	{
		var attId = xmlFilter.newItemId();
		var attris = new Array();
		var att = {itemid : attId, aName : trimName};
		attris[0] = att;
		
		xmlFilter.appendAttributeOptions(document.getElementById("xmlRuleConfiguredTag_trans_attributes"), attris);
		xmlFilter.editItemTransAttributes[xmlFilter.editItemTransAttributes.length] = att;
	}
	
	attNameElement.value = "";
}

XMLRuleFilter.prototype.handleTransAttrRemove = function(oriObj)
{
	var selectObj = document.getElementById("xmlRuleConfiguredTag_trans_attributes");
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
	
	var newItemAttrs = new Array();
	idIndex = 0;
	for(var i = 0; i < xmlFilter.editItemTransAttributes.length; i++)
	{
		if (!idArray.contains(xmlFilter.editItemTransAttributes[i].itemid))
		{
			newItemAttrs[idIndex++] = xmlFilter.editItemTransAttributes[i];
		}
	}
	
	xmlFilter.editItemTransAttributes = newItemAttrs;
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
	
	var isExisted = false;
	for(var i = 0; i < xmlFilter.editItemCdataConditions.length; i++)
	{
		var existCond = xmlFilter.editItemCdataConditions[i];
		if (existCond.aType == aType && existCond.aOp == aOp && existCond.aValue == resV.trim())
		{
			isExisted = true;
			break;
		}
	}
	
	if (!isExisted)
	{
		var objj = {itemid : itemId, aType : aType, aOp : aOp, aValue : resV.trim()};
		arrs[0] = objj;
		
		xmlFilter.appendCdataConditions(document.getElementById("xmlFilterCdatapostFilter_cond_items"), arrs);
		xmlFilter.editItemCdataConditions[xmlFilter.editItemCdataConditions.length] = objj;
	}
	
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
	
	var attributeName;
	var fromAttribute = false;
	if (xmlFilter.currentOption == xmlFilter.optionSrcCmtXmlTag)
	{
		fromAttribute = ("1" == getRadioValue(fpForm.xmlRuleConfiguredTag_from));
		attributeName = new StringBuffer(document.getElementById("xmlRuleConfiguredTag_attributeName").value);
		attributeName = attributeName.trim();
		
		if (fromAttribute)
		{
			if (validate.isEmptyStr(attributeName))
			{
				alert(jsNonAttr);
				return;
			}
		}
		else
		{
			attributeName = "";
		}
	}
	
	var dialogId = "xmlRuleFilter_configured_tag_Dialog";
	var itemId = xmlFilter.editItemId;
	var attributes = xmlFilter.editItemAttributes;
	var enable = xmlFilter.editItemEnable;
	var transAttributes = xmlFilter.editItemTransAttributes;
	
	var item;
	
	if (xmlFilter.currentOption == xmlFilter.optionPreserveWsTags
			|| xmlFilter.currentOption == xmlFilter.optionEmbTags
			|| xmlFilter.currentOption == xmlFilter.optionInternalTag)
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
	
	if (xmlFilter.currentOption == xmlFilter.optionSrcCmtXmlTag)
	{
		item = {itemid : itemId, enable : enable, tagName : tagName.trim(), attributes : attributes,
				fromAttribute : fromAttribute, attributeName : attributeName};
	}
	
	var objArray = xmlFilter.optionObjsMap[xmlFilter.currentOption];
	var nameExists = false;
	var itemname = item.tagName;
	
	// check tag name
	for(i = 0; i < objArray.length; i++)
	{
		var oriItem = objArray[i];
		// edit this tag
		if (oriItem.itemid == itemId)
		{
			continue;
		}
		// tag name exists
		if (oriItem.tagName == itemname)
		{
			nameExists = true;
			break;
		}
	}
	
	if (nameExists)
	{
		alert(existTagName);
	}
	else
	{
		xmlFilter.addOneItemInCurrentOptions(item);
		xmlFilter.closeConfiguredTagDialog(dialogId);
	}
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
	var piTransAttributes = xmlFilter.editItemPiTransAttributes;
	
	var item;
	item = {itemid : itemId, enable : enable, aName : aName.trim(), handleType : handleType, piTransAttributes : piTransAttributes};
	
	xmlFilter.addOneItemInCurrentOptions(item);
	xmlFilter.closeConfiguredTagDialog(dialogId);
}

XMLRuleFilter.prototype.saveSrcCmtXmlComment = function()
{
	var validate = new Validate();
	var aName = new StringBuffer(document.getElementById("xmlRuleFilter_srcCmtXmlComment_name").value);

	if(validate.isEmptyStr(aName.trim()))
	{
		document.getElementById("xmlRuleFilter_srcCmtXmlComment_name").value = "";
		alert(emptyTagName);
		return;
	}
	
	var dialogId = "xmlRuleFilter_srcCmtXmlComment_Dialog";
	var itemId = xmlFilter.editItemId;
	var enable = xmlFilter.editItemEnable;
	var isRE = document.getElementById("xmlRuleFilter_srcCmtXmlComment_isRE").checked;
	
	var item;
	item = {itemid : itemId, enable : enable, aName : aName.trim(), isRE : isRE};
	
	xmlFilter.addOneItemInCurrentOptions(item);
	xmlFilter.closeConfiguredTagDialog(dialogId);
}

XMLRuleFilter.prototype.addOneItemInCurrentOptions = function(item)
{
	if (xmlFilter.isDefined(item))
	{
		var i = 0;
		var objArray = xmlFilter.optionObjsMap[xmlFilter.currentOption];
		var isEditMode = false;
		var itemId = item.itemid;
		
		for(; i < objArray.length; i++)
		{
			var oriItem = objArray[i];
			if (oriItem.itemid == itemId)
			{
				objArray[i] = item;
				isEditMode = true;
				break;
			}
		}
		
		if (!isEditMode)
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

//for gbs-2599
XMLRuleFilter.prototype.selectAll_XMLRuleFilter = function()
{
	var selectAll = document.getElementById("selectAll_XMLRuleFilter")
	if(selectAll.checked) {
		this.checkAllTagsToDelete();
	} else {
		this.clearAllTagsToDelete();
	}
}

XMLRuleFilter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsTagType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_XMLRuleFilter' onclick='xmlFilter.selectAll_XMLRuleFilter()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsTagsToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsTagsCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	var tagTypes = document.getElementById("xmlFilterRulesSection");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var ruleObjects = xmlFilter.optionObjsMap[value];
		var checkboxId = "delete_tags_" + value;
		
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
					str.append("<input type='checkbox' name='"+checkboxId+"' tagType='" + value + "' tagValue='"+ruleObject.itemid+"' checked>");
					str.append(xmlFilter.isDefined(ruleObject.tagName)?ruleObject.tagName:ruleObject.aName);
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
	/*for gbs-2599
	str.append("<a href='#' class='specialfilter_a' onclick='xmlFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='xmlFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteXmlTagsDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteXmlTagTableContent").innerHTML = str.toString();
	return true;
}

XMLRuleFilter.prototype.isDefined = function(objj)
{
	return (typeof(objj) != 'undefined');
}

XMLRuleFilter.prototype.setCheckOrClearAll = function( /*boolean*/checkOrClear )
{
    for(var i = 0; i < xmlFilter.availableOptions.length; i++)
    {
        var checkBoxId = "delete_tags_" + xmlFilter.availableOptions[i];
        var checkBoxObjs = document.getElementsByName(checkBoxId);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
            checkBoxObjs[j].checked = checkOrClear;
        }
    }
}

XMLRuleFilter.prototype.checkAllTagsToDelete = function ()
{
	xmlFilter.setCheckOrClearAll(true);
}

XMLRuleFilter.prototype.clearAllTagsToDelete = function()
{
	xmlFilter.setCheckOrClearAll(false);
}

XMLRuleFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	//var newid = (new Date()).getTime() + "" + Math.round(Math.random()*100);
	return newid;
}

XMLRuleFilter.prototype.setPageValue = function()
{
	var curOption = xmlFilter.currentOption;
	var maxPages = xmlFilter.getPageSize(xmlFilter.optionObjsMap[curOption].length);
	xmlFilter.currentPage = (xmlFilter.currentPage >= maxPages) ? maxPages - 1 : xmlFilter.currentPage;
	document.getElementById("pageTotalSize").innerHTML = maxPages;
   	document.getElementById("pageCountXmlFilter").value = xmlFilter.currentPage + 1;
}

XMLRuleFilter.prototype.prePage = function()
{
    if(xmlFilter.currentPage > 0)
	{
    	xmlFilter.currentPage --;
    	var content = xmlFilter.generateXmlRulesContent(xmlFilter.currentOption, xmlFilter.currentPage);
		xmlFilter.refreshTagsContent(content);
    	xmlFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

XMLRuleFilter.prototype.nextPage = function()
{
	var arraysize = xmlFilter.getCurrentObjectsSize();
	if(xmlFilter.currentPage < xmlFilter.getPageSize(arraysize) - 1)
	{
		xmlFilter.currentPage ++;
		var content = xmlFilter.generateXmlRulesContent(xmlFilter.currentOption, xmlFilter.currentPage);
		xmlFilter.refreshTagsContent(content);
		xmlFilter.setPageValue();
	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

XMLRuleFilter.prototype.getPageSize = function(itemsTotalCount)
{
	var countPerPage = xmlFilter.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

XMLRuleFilter.prototype.goToPage = function()
{
	var pageValue = document.getElementById("pageCountXmlFilter").value;
	var validate = new Validate();
	if(!validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	var arraysize = xmlFilter.getCurrentObjectsSize();
	
	if(pageValue < 1 || pageValue > xmlFilter.getPageSize(arraysize))
	{
		alert(invalidatePageValue.replace("%s", xmlFilter.getPageSize(arraysize)));
		xmlFilter.setPageValue();
		return;
	}

	xmlFilter.currentPage = pageValue - 1;
	var content = xmlFilter.generateXmlRulesContent(xmlFilter.currentOption, xmlFilter.currentPage);
	xmlFilter.refreshTagsContent(content);
}

XMLRuleFilter.prototype.switchRules = function(xmlFilterRulesSection)
{
	xmlFilter.currentOption = xmlFilterRulesSection.options[xmlFilterRulesSection.selectedIndex].value;
	xmlFilter.currentPage = 0;
	
	xmlFilter.closeAllTagPopup();
	xmlFilter.refreshCheckedIds();
	var content = xmlFilter.generateXmlRulesContent(xmlFilter.currentOption, xmlFilter.currentPage);
	xmlFilter.refreshTagsContent(content);
	document.getElementById("checkAllXmlFilter").checked = false;
	xmlFilter.setPageValue();
}

XMLRuleFilter.prototype.refreshTagsContent = function(content)
{
	var ccc = new StringBuffer(xmlTagsContentTable);
	ccc.append(content);
	ccc.append("</table>");
	
	document.getElementById("xmlTagsContent").innerHTML = ccc.toString();
}

XMLRuleFilter.prototype.generateXmlRulesContent = function(optionValue, pageIndex)
{
	// var objArray = xmlFilter.parserXml(optionValue, ruleXml);
	var objArray = xmlFilter.optionObjsMap[optionValue];
	var str = new StringBuffer("");
	str.append("<tr class='htmlFilter_emptyTable_tr'>");
	
	if (objArray && objArray.length > 0)
	{
		var sortImgSrc = "/globalsight/images/sort-up.gif";
		if(xmlFilter.sortOrder == 'asc')
		{
			sortImgSrc = "/globalsight/images/sort-up.gif";
		}
		else
		{
			sortImgSrc = "/globalsight/images/sort-down.gif";
		}
		
		//var checkAllStr = (xmlFilter.checkedItemIds.length == xmlFilter.getCurrentObjectsSize()) ? " checked " : "";
		var checkAllStr = "";
		str.append("<td width='5%'><input type='checkbox' onclick='xmlFilter.checkAll()' id='checkAllXmlFilter'" + checkAllStr + "/></td>");

		if (optionValue == xmlFilter.optionPreserveWsTags
				|| optionValue == xmlFilter.optionEmbTags
				|| optionValue == xmlFilter.optionTransAttrTags
				|| optionValue == xmlFilter.optionContentInclTags
				|| optionValue == xmlFilter.optionInternalTag
				|| optionValue == xmlFilter.optionSrcCmtXmlTag)
		{
			// TODO add sort function later
			// str.append("<td width='35%' class='tagName_td'><a href='#'
			// class='tagName_td' onmouseenter=mouseEnter('sort_img')
			// onmouseout=mouseOut('sort_img')
			// onclick='xmlFilter.sortTable(1)'>" + jsTagName + "</a>");
			// if (xmlFilter.sortColumnIndex == 1) str.append("<img
			// class='not_display' id='sort_img' src='" + sortImgSrc +
			// "'></img>");
			
			str.append("<td width='25%' class='tagName_td'>" + jsTagName + "</td>");
			str.append("<td class='tagName_td'>" + jsConditionalAttr + "&nbsp;&nbsp;&nbsp;&nbsp;</td>");
			if (optionValue == xmlFilter.optionTransAttrTags)
			{
				str.append("<td width='30%' class='tagName_td'>" + jsTranslateableAttr + "</td>");
			}
			if (optionValue == xmlFilter.optionContentInclTags)
			{
				str.append("<td width='30%' class='tagName_td'>" + jsContentIncludeExclude + "</td>");
			}
			if (optionValue == xmlFilter.optionSrcCmtXmlTag)
			{
				str.append("<td class='tagName_td'>" + jsFromAttr + "&nbsp;&nbsp;&nbsp;&nbsp;</td>");
				str.append("<td class='tagName_td'>" + jsAttrName + "</td>");
			}
		}
		if (optionValue == xmlFilter.optionCDataPostFilterTags)
		{
			str.append("<td width='25%' class='tagName_td'>" + jsNNNName + "</td>");
			str.append("<td class='tagName_td'>" + jsCdataCondition + "</td>");
			str.append("<td width='25%' class='tagName_td'>" + jsPostFilter + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsTranslatable + "</td>");
		}
		if (optionValue == xmlFilter.optionEntities)
		{
			str.append("<td width='30%' class='tagName_td'>" + jsAnEntity + "</td>");
			str.append("<td class='tagName_td'>" + jsEntityType + "</td>");
			str.append("<td class='tagName_td'>" + jsEntityCode + "</td>");
			str.append("<td class='tagName_td'>" + jsEntityChar + "</td>");
			str.append("<td class='tagName_td'>" + jsEntitySaveAs + "</td>");
		}
		if (optionValue == xmlFilter.optionProcessIns)
		{
			str.append("<td width='25%' class='tagName_td'>" + jsPIName + "</td>");
			str.append("<td width='30%' class='tagName_td'>" + jsPIHandling + "</td>");
			str.append("<td class='tagName_td'>" + jsTranslateableAttr + "</td>");
		}
		if (optionValue == xmlFilter.optionSrcCmtXmlComment)
		{
			str.append("<td width='50%' class='tagName_td'>" + jsInternalContent + "</td>");
			str.append("<td class='tagName_td'>" + jsInternalIsRegex + "</td>");
		}
		str.append("</tr>");
		var startIndex = 0;
		startIndex = xmlFilter.tagsEveryPage * pageIndex;
		if(startIndex >= objArray.length)
		{
			var maxPage = xmlFilter.getPageSize(objArray.length);
			alert(maxPageNums + maxPage);
			return maxPage;
		}
		
		for(var i = 0; i < xmlFilter.tagsEveryPage; i++)
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
				var radioId = xmlFilter.getRadioId(ruleObj.itemid);
				var checkedStr = xmlFilter.checkedItemIds.contains(radioId) ? " checked " : "";
				
				str.append("<tr style='background-color:"+backgroundColor+";'>");
				str.append("<td><input onclick='xmlFilter.checkthis(this)' type='checkbox' id='"+radioId+"'" + checkedStr + "/></td>");
				
				if (optionValue == xmlFilter.optionPreserveWsTags
						|| optionValue == xmlFilter.optionEmbTags
						|| optionValue == xmlFilter.optionTransAttrTags
						|| optionValue == xmlFilter.optionContentInclTags
						|| optionValue == xmlFilter.optionInternalTag
						|| optionValue == xmlFilter.optionSrcCmtXmlTag)
				{
					str.append("<td class='tagValue_td'><a href='#' onclick=\"xmlFilter.addTag('" + radioId + "')\">"+ruleObj.tagName+"</a></td>");
					str.append("<td class='tagValue_td'>"+xmlFilter.getAttributesString(ruleObj)+"</td>");
					if (optionValue == xmlFilter.optionTransAttrTags)
					{
						str.append("<td class='tagValue_td'>"+xmlFilter.getTransAttributesString(ruleObj)+"</td>");
					}
					if (optionValue == xmlFilter.optionContentInclTags)
					{
						str.append("<td class='tagValue_td'>"+(1==ruleObj.inclType?jsContentInclude:jsContentExclude)+"</td>");
					}
					if (optionValue == xmlFilter.optionSrcCmtXmlTag)
					{
						str.append("<td class='tagValue_td'>"+(ruleObj.fromAttribute?imgYes:"")+"</td>");
						str.append("<td class='tagValue_td'>"+(ruleObj.fromAttribute?ruleObj.attributeName:"")+"</td>");
					}
				}
				if (optionValue == xmlFilter.optionCDataPostFilterTags)
				{
					str.append("<td class='tagValue_td'><a href='#' onclick=\"xmlFilter.addTag('" + radioId + "')\">"+ruleObj.aName+"</a></td>");
					str.append("<td class='tagValue_td'>"+xmlFilter.getObjConditionsString(ruleObj)+"</td>");
					str.append("<td class='tagValue_td'>"+xmlFilter.getFilterName(ruleObj.postFilterId, ruleObj.postFilterTableName)+"</td>");
					str.append("<td class='tagValue_td'>"+(ruleObj.translatable?imgYes:"")+"</td>");
				}
				if (optionValue == xmlFilter.optionEntities)
				{
					str.append("<td class='tagValue_td'><a href='#' onclick=\"xmlFilter.addTag('" + radioId + "')\">"+ruleObj.aName+"</a></td>");
					var isTxt = !(1==ruleObj.entityType);
					str.append("<td class='tagValue_td'>" + (isTxt?jsEntityTypeText:jsEntityTypePlaceHolder) + "</td>");
					str.append("<td class='tagValue_td'>" + (isTxt?ruleObj.entityCode : "") + "</td>");
					var realChar = isTxt ? "&#" + ruleObj.entityCode + ";" : "";
					str.append("<td class='tagValue_td'>" + realChar + "</td>");
					var saveAsStr = (isTxt? (ruleObj.saveAs == 1 ? jsEntitySaveAsChar : jsEntitySaveAsEntity) : "");
					str.append("<td class='tagValue_td'>" + saveAsStr + "</td>");
				}
				if (optionValue == xmlFilter.optionProcessIns)
				{
					str.append("<td class='tagValue_td'><a href='#' onclick=\"xmlFilter.addTag('" + radioId + "')\">"+ruleObj.aName+"</a></td>");
					var hType = (ruleObj.handleType == 3 ? jsPIExtract : (ruleObj.handleType == 2 ? jsPIRemove : (ruleObj.handleType == 1? jsPIEmbMarkUp : jsPIMarkup)));
					
					str.append("<td class='tagValue_td'>" + hType + "</td>");
					str.append("<td class='tagValue_td'>" + xmlFilter.getTransAttributesString(ruleObj) + "</td>");
				}
				if (optionValue == xmlFilter.optionSrcCmtXmlComment)
				{					
					var encodedName = encodeHtmlEntities(ruleObj.aName);
					str.append("<td class='tagValue_td'><a href='#' onclick=\"xmlFilter.addTag('" + radioId + "')\">"+encodedName+"</a></td>");
					str.append("<td class='tagValue_td'>" + (ruleObj.isRE?imgYes:"") + "</td>");
				}
				str.append("</tr>");
			}
		}
	}
	else
	{
		str.append("<td width='5%'><input type='checkbox' id='checkAllXmlFilter'/></td>");
		if (optionValue == xmlFilter.optionPreserveWsTags
				|| optionValue == xmlFilter.optionEmbTags
				|| optionValue == xmlFilter.optionTransAttrTags
				|| optionValue == xmlFilter.optionContentInclTags
				|| optionValue == xmlFilter.optionInternalTag
				|| optionValue == xmlFilter.optionSrcCmtXmlTag)
		{
			str.append("<td width='25%' class='tagName_td'>" + jsTagName + "</td>");
			str.append("<td class='tagName_td'>" + jsConditionalAttr + "</td>");
			if (optionValue == xmlFilter.optionTransAttrTags)
			{
				str.append("<td class='tagName_td'>" + jsTranslateableAttr + "</td>");
			}
			if (optionValue == xmlFilter.optionContentInclTags)
			{
				str.append("<td width='30%' class='tagName_td'>" + jsContentIncludeExclude + "</td>");
			}
			if (optionValue == xmlFilter.optionSrcCmtXmlTag)
			{
				str.append("<td class='tagName_td'>" + jsFromAttr + "</td>");
				str.append("<td width='30%' class='tagName_td'>" + jsAttrName + "</td>");
			}
		}
		if (optionValue == xmlFilter.optionCDataPostFilterTags)
		{			
			str.append("<td width='25%' class='tagName_td'>" + jsNNNName + "</td>");
			str.append("<td class='tagName_td'>" + jsCdataCondition + "</td>");
			str.append("<td width='25%' class='tagName_td'>" + jsPostFilter + "</td>");
			str.append("<td width='15%' class='tagName_td'>" + jsTranslatable + "</td>");
		}
		if (optionValue == xmlFilter.optionEntities)
		{			
			str.append("<td width='30%' class='tagName_td'>" + jsAnEntity + "</td>");
			str.append("<td class='tagName_td'>" + jsEntityType + "</td>");
			str.append("<td class='tagName_td'>" + jsEntityCode + "</td>");
			str.append("<td class='tagName_td'>" + jsEntityChar + "</td>");
			str.append("<td class='tagName_td'>" + jsEntitySaveAs + "</td>");
		}
		if (optionValue == xmlFilter.optionProcessIns)
		{
			str.append("<td width='25%' class='tagName_td'>" + jsPIName + "</td>");
			str.append("<td width='30%' class='tagName_td'>" + jsPIHandling + "</td>");
			str.append("<td class='tagName_td'>" + jsTranslateableAttr + "</td>");
		}
		if (optionValue == xmlFilter.optionSrcCmtXmlComment)
		{
			str.append("<td width='50%' class='tagName_td'>" + jsInternalContent + "</td>");
			str.append("<td class='tagName_td'>" + jsInternalIsRegex + "</td>");
		}
		str.append("</tr>");
		str.append("<tr><td colspan='2'><p><br /></p></td></tr>");
	}
	
	return str.toString();
}

XMLRuleFilter.prototype.getItemId = function(radioid)
{
	return radioid.replace("xmltags_", "");
}

XMLRuleFilter.prototype.getRadioId = function(itemid)
{
	return "xmltags_" + itemid;
}

XMLRuleFilter.prototype.checkthis = function(cobj)
{
	var oid = cobj.id;
	var checkAllObj = document.getElementById("checkAllXmlFilter");
	var itemId = xmlFilter.getItemId(oid);
	var ruleObj = xmlFilter.getItemById(itemId, xmlFilter.currentOption);
	ruleObj.enable = cobj.checked;

	if (!cobj.checked)
	{
		checkAllTagsInXmlFilter = false;
		checkAllObj.checked = false;
		xmlFilter.checkedItemIds.removeData(oid);
		
	}
	else
	{
		if (!xmlFilter.checkedItemIds.contains(oid))
		{
			xmlFilter.checkedItemIds[xmlFilter.checkedItemIds.length] = oid;
		}
		
		if (xmlFilter.checkedItemIds.length == xmlFilter.getCurrentObjectsSize())
		{
			checkAllObj.checked = true;
		}
	}
}

XMLRuleFilter.prototype.checkAll = function()
{
	var checkAllObj = document.getElementById("checkAllXmlFilter");
	checkAllTagsInXmlFilter = checkAllObj.checked;
	for(var i = 0; i < xmlFilter.getCurrentObjectsSize(); i++)
	{
		var ruleObj = xmlFilter.optionObjsMap[xmlFilter.currentOption][i];
		var oid = xmlFilter.getRadioId(ruleObj.itemid);
		
		if(document.getElementById(oid))
		{
			document.getElementById(oid).checked = checkAllTagsInXmlFilter;	
		}
		
		if (checkAllTagsInXmlFilter)
		{
			xmlFilter.checkedItemIds.appendUniqueObj(oid);
		}
		
		ruleObj.enable = checkAllTagsInXmlFilter;
	}
	
	if (!checkAllTagsInXmlFilter)
	{
		xmlFilter.checkedItemIds = new Array();
	}
}

XMLRuleFilter.prototype.getCurrentObjectsSize = function()
{
	return xmlFilter.optionObjsMap[xmlFilter.currentOption].length;
}

// attributes : [{aName : "name1", aOp : "equal", aValue : "vvv1"}]
XMLRuleFilter.prototype.getAttributesString = function (ruleObj)
{
	var str = new StringBuffer("");
	
	if (ruleObj && ruleObj.attributes)
	{
		var attrslen = ruleObj.attributes.length;
		for(var i=0; i<attrslen; i++)
		{
			str.append(xmlFilter.getAttributeString(ruleObj.attributes[i]));
			if (i != (attrslen - 1))
			{
				str.append(", <br />");
			}
		}
	}
	
	return str.toString();
}

XMLRuleFilter.prototype.getTransAttributesString = function (ruleObj)
{
	var str = new StringBuffer("");
	
	if (ruleObj)
	{
		if (ruleObj.transAttributes)
		{
			var attrslen = ruleObj.transAttributes.length;
			for(var i=0; i<attrslen; i++)
			{
				str.append(ruleObj.transAttributes[i].aName);
				if (i != (attrslen - 1))
				{
					str.append(", ");
				}
			}
		}
		else if (ruleObj.piTransAttributes)
		{
			var attrslen = ruleObj.piTransAttributes.length;
			for(var i=0; i<attrslen; i++)
			{
				str.append(ruleObj.piTransAttributes[i].aName);
				if (i != (attrslen - 1))
				{
					str.append(", ");
				}
			}
		}
	}
		
	
	return str.toString();
}

XMLRuleFilter.prototype.getAttributeString = function(attrObj)
{
	var str = new StringBuffer("");
	
	if (attrObj)
	{
		str.append(attrObj.aName + " " + xmlFilter.operatorNameMap[attrObj.aOp] + " " + attrObj.aValue);
	}
	
	return str.toString();
}

XMLRuleFilter.prototype.getObjConditionsString = function (ruleObj)
{
	var str = new StringBuffer("");
	
	if (ruleObj && ruleObj.cdataConditions)
	{
		var leng = ruleObj.cdataConditions.length;
		for(var i=0; i<leng; i++)
		{
			var condObj = ruleObj.cdataConditions[i];
			str.append(xmlFilter.getConditionString(condObj));
			if (i != (leng - 1))
			{
				str.append("<br />");
			}
		}
	}
	
	return str.toString();
}

XMLRuleFilter.prototype.getConditionString = function(condObj)
{
	var str = new StringBuffer("");
	
	if (condObj)
	{
		str.append(xmlFilter.conditionTypeMap[condObj.aType] + " " + xmlFilter.operatorNameMap[condObj.aOp] + " " + condObj.aValue);
	}
	
	return str.toString();
}

XMLRuleFilter.prototype.getFilterName = function(filterId, filterTableName)
{
	if (filterId > 0 && filterTableName && filterTableName != "")
	{
		var _filter = getSpecialFilterByIdAndFilterTableName(filterId, filterTableName);
		if (_filter)
		{
			return _filter.filterName;
		}
	}
	
	return jsPostFilterNone;
}

XMLRuleFilter.prototype.generateAvailableFilterOptions = function(filter, filterIdPara, filterTableNamePara)
{
	var str = new StringBuffer("");
	var _filterConfigurations = filterConfigurations;
	
	if (_filterConfigurations)
	{
	for(var i = 0; i < _filterConfigurations.length; i++)
	{
		var _filter = _filterConfigurations[i];
        if (xmlFilter.availablePostFilters.contains(_filter.filterTableName))
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
