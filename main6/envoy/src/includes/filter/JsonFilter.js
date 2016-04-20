var jsonFilter = new JsonFilter();

function JsonFilter() {
	this.filterTableName = "json_filter";

//	this.checkedItemIds = new Array();
//	this.optionInternalText = "0";
//	this.options = new Array();
//	this.options[0] = this.optionInternalText;
//
//	this.optionNameMap = new Object();
//	this.optionNameMap[this.optionInternalText] = jsInternalText;
//
//	this.optionObjsMap = new Object();
//	this.optionObjsMap[this.optionInternalText] = new Array();
//	this.checkedItemIds = new Array();
//
//	this.currentOption = this.optionInternalText;

	this.currentPage = 0;
	this.availablePostFilters = ["html_filter"];

}

//JsonFilter.prototype.initOptionMap = function(filter) {
//	jsonFilter.checkedItemIds = new Array();
//
//	if (filter) {
//		var internalTexts = this.filter.internalTexts;
//		if (internalTexts) {
//			if (internalTexts.content) {
//				var itemArray = new Array();
//				itemArray.push(internalTexts);
//				jsonFilter.optionObjsMap[this.optionInternalText] = itemArray;
//			} else {
//				jsonFilter.optionObjsMap[this.optionInternalText] = this
//						.removeEmptyTags(internalTexts);
//			}
//		} else {
//			jsonFilter.optionObjsMap[this.optionInternalText] = new Array();
//		}
//	} else {
//		jsonFilter.optionObjsMap[this.optionInternalText] = new Array();
//		var itemArray = new Array();
//		var item = new Object();
//		item.content = "\\{[^{]*?\\}";
//		item.isRegex = true;
//		itemArray.push(item);
//		jsonFilter.optionObjsMap[this.optionInternalText] = itemArray;
//	}
//}

JsonFilter.prototype.showDialog = function() {
	closeAllFilterDialogs();
	showPopupDialog("jsonFilterDialog");
}

JsonFilter.prototype.generateDiv = function(topFilterId, color) {
//	this.initOptionMap();
	var str = new StringBuffer("<table border=0 width='100%'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>"
			+ jsFilterName + ":</td>");
	str.append("<td ><input type='text' style='width:100%' maxlength='"
			+ maxFilterNameLength
			+ "' id='jsonFilterName' value='Json Filter'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>"+ jsFilterDesc + ":</td>");
	str.append("<td><textarea rows='4' style='width:100%' cols='17' id='jsonDesc' name='desc'></textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");

	str.append("<table border=0 width='100%'>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsEnableSIDSupport+ "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isSupportSid' type='checkbox' name='supportSid' value='true'/></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");

	var filter = getFilterById(topFilterId);
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>"+ generateBaseFilterList(this.filterTableName) + "</td>");
	str.append("</tr>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsElementPostFilter+ "</td>");
	str.append("<td class='htmlFilter_right_td'>"+ this.generateElementPostFilter() + "</td>");
	str.append("</tr>");

	str.append("</table>");

	str.append("<div><br/><br/></div>");

	var dialogObj = document.getElementById('jsonFilterPopupcontent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();

	saveJson.edit = false;
	saveJson.color = color;
	saveJson.topFilterId = topFilterId;
}

JsonFilter.prototype.generateElementPostFilter = function(filter) {
	var str = new StringBuffer("<select id='elementPostFilter' class='xml_filter_select'>");
	str.append("<option value='-1'"+ ((filter && filter.elementPostFilter == "-1") ? " selected" : "")
			+ ">" + jsChoose + "</option>");

	str.append(jsonFilter.generateAvailableFilterOptions(filter,
			"elementPostFilterId"));
	str.append("</select>");
	return str.toString();
}

JsonFilter.prototype.generateAvailableFilterOptions = function(filter, filterIdPara)
{
	var str = new StringBuffer("");
	var _filterConfigurations = filterConfigurations;
	
	if (_filterConfigurations)
	{
	for(var i = 0; i < _filterConfigurations.length; i++)
	{
		var _filter = _filterConfigurations[i];
        if (jsonFilter.availablePostFilters.contains(_filter.filterTableName))
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

	        		var selected = ""; 
	        		if (_id == theFiterId)
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

function saveJson() {
	var check = new Validate();
	var filterName = document.getElementById("jsonFilterName").value;
	if (check.isEmptyStr(filterName)) {
		alert(emptyFilterName);
		return;
	}
	if (check.containSpecialChar(filterName)) {
		alert(invalidFilterNameChar + invalidChars);
		return;
	}
	filterName = check.trim(filterName);
	if (check.isExceedMaxCount(filterName, maxFilterNameLength)) {
		return;
	}
	var isNew = (saveJson.edit) ? "false" : "true";
	var filterId = saveJson.filterId;
	var filterDesc = document.getElementById("jsonDesc").value;
	var isSupportSid = document.getElementById("isSupportSid").checked;

	var baseFilterSelect = document.getElementById("json_filter_baseFilterSelect");
	var indexBase = baseFilterSelect.selectedIndex;
	var baseFilterId = baseFilterSelect.options[indexBase].value;

	var elementPostFilters = document.getElementById("elementPostFilter");
	var indexElement = elementPostFilters.selectedIndex;
	var elementPostFilters = elementPostFilters.options[indexElement].value;

	ch = new Array; 
	ch = elementPostFilters.split("-"); 
	var elementPostFilterId = ch[0];
	var elementPostFilterTableName = ch[1];
	
	var obj = {
		isNew : isNew,
		filterTableName : "json_filter",
		filterId : filterId,
		filterName : filterName,
		filterDesc : filterDesc,
		isSupportSid : isSupportSid,
		companyId : companyId,
		baseFilterId : baseFilterId,
		elementPostFilterId : elementPostFilterId,
		elementPostFilterTableName : elementPostFilterTableName
	};
	sendAjax(obj, "checkExist", "checkExistJsonCallback");


	checkExistJsonCallback.obj = obj;
}

function checkExistJsonCallback(data) {
	if (data == 'false') {
		closePopupDialog("jsonFilterDialog");
		if (saveJson.edit) {
			sendAjax(checkExistJsonCallback.obj, "updateJsonFilter",
					"updateJsonFilterCallback");
		} else {
			sendAjax(checkExistJsonCallback.obj, "saveJsonFilter",
					"saveJsonFilterCallback");
		}
	} else if (data == 'failed') {
		closePopupDialog("jsonFilterDialog");
		parent.location.reload();
	} else {
		alert(existFilterName);
	}
}

function saveJsonFilterCallback(data) {
	var color = saveJson.color;
	var topFilterId = saveJson.topFilterId;
	var filter = getFilterById(topFilterId);

	if (filter) {
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = "json_filter";
		jpFilter.filterName = checkExistJsonCallback.obj.filterName;
		jpFilter.filterDescription = checkExistJsonCallback.obj.filterDesc;
		jpFilter.enableSidSupport = checkExistJsonCallback.obj.isSupportSid;
		jpFilter.companyId = companyId;
		jpFilter.baseFilterId = checkExistJsonCallback.obj.baseFilterId;
		jpFilter.elementPostFilterId = checkExistJsonCallback.obj.elementPostFilterId;
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

function updateJsonFilterCallback(data)
{
	var color = saveJson.color;
	var filterId = saveJson.filterId;
	var filter = saveJson.filter;
	var topFilterId = saveJson.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = "json_filter";
		jpFilter.filterName = checkExistJsonCallback.obj.filterName;
		jpFilter.filterDescription = checkExistJsonCallback.obj.filterDesc;
		jpFilter.enableSidSupport = checkExistJsonCallback.obj.isSupportSid;
		jpFilter.companyId = companyId;
		jpFilter.baseFilterId = checkExistJsonCallback.obj.baseFilterId;
		jpFilter.elementPostFilterId = checkExistJsonCallback.obj.elementPostFilterId;
		
		var specialFilters = updateSpecialFilter(saveJson.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

JsonFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

JsonFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId) {

	var str = new StringBuffer("<table border=0 width='100%'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>"+ jsFilterName + ":</td>");
	str.append("<td ><input type='text' maxlength='" + maxFilterNameLength 
			+ "' style='width:100%' id='jsonFilterName' value='"+ this.filter.filterName + "'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>"+ jsFilterDesc + ":</td>");
	str.append("<td><textarea rows='4' cols='17' style='width:100%' id='jsonDesc' name='desc' value='"
			+ this.filter.filterDescription+ "'>"+ this.filter.filterDescription + "</textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");

	var isCheckSid = (this.filter.enableSidSupport) ? "checked" : "";

	str.append("<table border=0 width='100%'>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsEnableSIDSupport+ "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isSupportSid' type='checkbox' name='supportSid' value='"
				+ this.filter.enableSidSupport+ "' "+ isCheckSid+ "/></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>"+ generateBaseFilterList(this.filterTableName, this.filter)+ "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsElementPostFilter+ "</td>");
	str.append("<td class='htmlFilter_right_td'>"+ this.generateElementPostFilter(this.filter) + "</td>");
	str.append("</tr>");
		
	str.append("</table>");

	str.append("<div><br/><br/></div>");

	var dialogObj = document.getElementById('jsonFilterPopupcontent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveJson.edit = true;
	saveJson.filterId = filterId;
	saveJson.color = color;
	saveJson.filter = this.filter;
	saveJson.specialFilters = specialFilters;
	saveJson.topFilterId = topFilterId;
}

