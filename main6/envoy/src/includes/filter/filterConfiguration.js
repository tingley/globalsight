var filterConfigurations;
var existXmlRules = new Array();
var existBaseFilters;
var specialFiltersMap = new Object();
var companyId;
var allFilterDialogIds = new Array();
allFilterDialogIds[0] = "javaPropertiesFilterDialog";
allFilterDialogIds[1] = "javaScriptFilterDialog";
allFilterDialogIds[2] = "msOfficeDocFilterDialog";
allFilterDialogIds[3] = "xmlRuleFilterDialog";
allFilterDialogIds[4] = "htmlFilterDialog";
allFilterDialogIds[5] = "jspFilterDialog";
allFilterDialogIds[6] = "msOfficeExcelFilterDialog";
allFilterDialogIds[7] = "inddFilterDialog";
allFilterDialogIds[8] = "openofficeFilterDialog";
allFilterDialogIds[9] = "msOfficePPTFilterDialog";
allFilterDialogIds[10] = "msoffice2010FilterDialog";
allFilterDialogIds[11] = "poFilterDialog";
allFilterDialogIds[12] = "baseFilterDialog";
allFilterDialogIds[13] = "fmFilterDialog";
allFilterDialogIds[14] = "plaintextFilterDialog";
allFilterDialogIds[15] = "qaFilterDialog";
var isBaseFilterSelectChanged = false;

//For HTML
var checkedEmbeddableTags;
var checkedPairedTags;
var checkedUnPairedTags;
var checkedSwitchTagMaps;
var checkedWhitePreservingTags;
var checkedNonTranslatableMetaAttributes;
var checkedTranslatableAttributes;
var checkedLocalizableAttributeMap;
var checkedLocalizableInlineAttributes;

var checkAllTags = false;
var checkAll2010Styles = false;
var checkAllSpecialFilters = false;
var checkedSpecialFilters = new Array();
var checkedMap = new Object();

//For XML
var convertHtmlEntity = false;
var removeExitsFilterTableStyle = "width:340px;margin-left:5px;";

function loadFilterConfigurations()
{
	specialFiltersMap["java_properties_filter"] = new JavaPropertiesFilter();
	specialFiltersMap["java_script_filter"] = new JavaScriptFilter();
	specialFiltersMap["ms_office_doc_filter"] = new MSOfficeDocFilter();
	specialFiltersMap["xml_rule_filter"] = new XMLRuleFilter();
	specialFiltersMap["html_filter"] = new HtmlFilter();
	specialFiltersMap["jsp_filter"] = new JSPFilter();
	specialFiltersMap["ms_office_excel_filter"] = new MSExcelFilter();
	specialFiltersMap["indd_filter"] = new InddFilter();
	specialFiltersMap["openoffice_filter"] = new OpenOfficeFilter();
	specialFiltersMap["ms_office_ppt_filter"] = new MSPPTFilter();
	specialFiltersMap["office2010_filter"] = new MSOffice2010Filter();
	specialFiltersMap["po_filter"] = new POFilter();
	specialFiltersMap["base_filter"] = new BaseFilter();
	specialFiltersMap["frame_maker_filter"] = new FMFilter();
	specialFiltersMap["plain_text_filter"] = new PlainTextFilter();
	specialFiltersMap["qa_filter"] = new QAFilter();
	
	sendAjax(null, "loadFilterConfigurations", "loadFilterConfigurationsCallback");
}

function loadXmlRulesCallback(data)
{
	existXmlRules = eval(data);
}

function initialCheckedTagsForHtmlFilter()
{
	checkedEmbeddableTags = new StringBuffer(",");
	checkedPairedTags = new StringBuffer(",");
	checkedUnPairedTags = new StringBuffer(",");
	checkedSwitchTagMaps = new StringBuffer(",");
	checkedWhitePreservingTags = new StringBuffer(",");
	checkedNonTranslatableMetaAttributes = new StringBuffer(",");
	checkedTranslatableAttributes = new StringBuffer(",");
	checkedInternalTag = new StringBuffer(",");
	checkedLocalizableAttributeMap = new StringBuffer(",");
	checkedLocalizableInlineAttributes = new StringBuffer(",");
	
	checkedMap["embeddable_tags"] = checkedEmbeddableTags;
	checkedMap["paired_tags"] = checkedPairedTags;
	checkedMap["unpairedTag_tags"] = checkedUnPairedTags;
	checkedMap["switch_tag_map"] = checkedSwitchTagMaps;
	checkedMap["white_preserving_tag"] = checkedWhitePreservingTags;
	//checkedMap["non_translatable_meta_attribute"] = checkedNonTranslatableMetaAttributes;
	checkedMap["translatable_attribute"] = checkedTranslatableAttributes;
	checkedMap["internal_tag"] = checkedInternalTag;
	checkedMap["localizable_attribute_map"] = checkedLocalizableAttributeMap;
	checkedMap["localizable_inline_attributes"] = checkedLocalizableInlineAttributes;
}

function loadFilterConfigurationsCallback(data)
{
	filterConfigurations = eval(data);
	sendAjax(null, "loadXmlRules", "loadXmlRulesCallback");

	initialCheckedTagsForHtmlFilter();
	
	if(filterConfigurations.length > 0)
	{
		companyId = filterConfigurations[0].companyId;
	}
	generateFilterTable(filterConfigurations);
}

function generateFilterTable(filterConfigurations)
{
	var str = new StringBuffer("<table cellpadding='0' cellspacing='0' border='1' class='main_table'>");

	for(var i = 0; i < filterConfigurations.length; i++)
	{
		var filter = filterConfigurations[i];
		var color = 'white';
		var backgroundColor = 'white';
		if(i % 2 == 0)
		{
			str.append("<tr class='main_table_even_tr'>");
			color = "white";	
			backgroundColor = "#eeeeee";	
		}
		else
		{
			str.append("<tr class='main_table_odd_tr'>");
			color = "#eeeeee";	
			backgroundColor = "white";	
		}
		
		str.append("<td width='5%' class='main_table_head' align='center'>");
		str.append("<img id=img_"+filter.id+" src='/globalsight/images/enlarge.jpg'   onclick='expand(this)'></img>");
		str.append("</td>");
		
		str.append("<td width='25%' class='main_table_head'>");
		str.append("<Label class='main_table_tr_label'>");
		str.append(getFilterNameByTableName(filter.filterTableName));
		str.append("</Label>");
		str.append("</td>");
		
		str.append("<td width='15%' class='main_table_head'>");
		if(hasAddFilter == 'true')
		{
			str.append("<input type='button' id='" + filter.filterTableName + "_" + jsAdd + "' value='" + jsAdd + "' onclick='addSpecialFilter(\""+filter.filterTableName+"\",\""+filter.id+"\",\""+color+"\");'/>");
		}
		str.append("</td>");
		
		str.append("<td width='55%' class='main_table_head'>");
		str.append("<Label class='main_table_tr_label'>");
		str.append(getFilterDescriptionByTableName(filter.filterTableName));
		str.append("</Label>");
		str.append("</td>");
		str.append("</tr>");
		
		str.append("<tr style='padding:6px;border:1px;border-color:#0c1476;background:"+backgroundColor+"'>");
		str.append("<td width='5%' class='main_table_head'>");
		str.append("<Label class='main_table_tr_label'> ");
		str.append("</Label>");
		str.append("</td>");
		str.append("<td width='20%' COLSPAN='3' class='main_table_head'>");
		str.append(generateSpecialFiltersDiv(filter.id, filter.specialFilters, color));
		str.append("</td>");
		str.append("</tr>");
		
		if(i != filterConfigurations.length - 1)
		{
			str.append("<tr border=0 height='1'>");
			str.append("<td border=0 height='1' colspan='4'>");
			str.append("<div class='split_tr_div'>&nbsp;");
			str.append("</div>");
			str.append("</td>");
			str.append("</tr>");
		}
		
		if (filter.filterTableName == "base_filter")
		{
			existBaseFilters = filter.specialFilters;
		}
	}
	str.append("</table>");

	document.getElementById("filterConfigurationTable").innerHTML = str.toString();
}

function expand(obj)
{
	var id = "filterName_" + obj.id.split("_")[1];
	if(obj.src.indexOf('enlarge.jpg') != -1)
	{
		obj.src = "/globalsight/images/ecllapse.jpg";
		document.getElementById(id).style.display = "block";
	}
	else
	{
		obj.src = "/globalsight/images/enlarge.jpg";
		document.getElementById(id).style.display = "none";
	}
	
}

function expandAllSpecialFilters()
{
	for(var i = 0; i < filterConfigurations.length; i++)
	{
		var filterConfiguration = filterConfigurations[i];
		var topFilterId = filterConfiguration.id;
		var expandImg = document.getElementById("img_" + topFilterId);
		expandImg.src = "/globalsight/images/enlarge.jpg";
		expand(expandImg);
	}
}
 
function collapseAllSpecialFilters()
{
	for(var i = 0; i < filterConfigurations.length; i++)
	{
		var filterConfiguration = filterConfigurations[i];
		var topFilterId = filterConfiguration.id;
		var expandImg = document.getElementById("img_" + topFilterId);
		expandImg.src = "/globalsight/images/ecllapse.jpg";
		expand(expandImg);
	}
}

function removeZeroElementFromArray(array)
{
	var newArray = new Array();
	for(var i = 0; i < array.length; i++)
	{
		if(array[i] && array[i] != "" && array[i] != '0')
		{
			newArray.push(array[i]);
		}
	}
	return newArray;
}

function generateSpecialFiltersTable(filterId, specialFilters, color)
{
	specialFilters = removeZeroElementFromArray(specialFilters);
	var str = new StringBuffer("<table width='100%' >");
	var backgroundColor;
	if(color == 'white')
	{
		backgroundColor = "#eeeeee";
	}
	else
	{
		backgroundColor = "white";
	}
	var imgSrc = '/globalsight/images/delete(white).PNG';
	if(backgroundColor == '#eeeeee')
	{
		imgSrc = '/globalsight/images/delete(gray).PNG';
	}
	for(var i = 0; i < specialFilters.length; i++)
	{
		var specialFilter = specialFilters[i];
		if(specialFilter == '0')
		{
			continue;
		}
		str.append("<tr style='background:"+backgroundColor+"'>");
		str.append("<td class='specialFilter_td'>");
		str.append("<input type='checkbox' id='checkbox_" + filterId + "_" + specialFilter.id + "' topFilterId='"+filterId+"' specialFilterId='"+specialFilter.id+"' filterTable='"+specialFilter.filterTableName+"' firstColor='"+color+"' onclick='checkSpecialFilterToDel(this)'></input>");
		if(hasEditFilter == 'true')
		{
			str.append("<a href='#' class='specialfilter_a' onclick='editFilter(\""+specialFilter.id+"\",\""+specialFilter.filterTableName+"\",\""+color+"\",\""+filterId+"\")' >")
			str.append(specialFilter.filterName);
			str.append("</a>");
		}
		else
		{
			str.append("<Label style='color:#810081'>");
			str.append(specialFilter.filterName);
			str.append("</Label>");
		}

		str.append("<img style='display:none' id=delete_"+specialFilter.id+" src='"+imgSrc+"' onclick='deleteFilter(\""+specialFilter.id+"\",\""+specialFilter.filterTableName+"\",\""+color+"\",\""+filterId+"\")'></img>");
		str.append("</td>");
		
		str.append("</tr>");
		if(i != specialFilters.length - 1)
		{
			str.append("<tr height='1px' style='background:"+color+"'>");
			str.append("<td style='background:"+color+"'>");
			str.append("</td>");
			str.append("</tr>");
		}
	}
	str.append("</table>");
	return str.toString();
}
function generateSpecialFiltersDiv(filterId, specialFilters, color)
{
	var str = new StringBuffer("<div id='filterName_" + filterId + "' class='specialFilter_div'>");
	str.append(generateSpecialFiltersTable(filterId, specialFilters, color));
	str.append("</div>");
	return str.toString();
}

function addSpecialFilter(filterTableName, topFilterId, color)
{
	var specialFilter = specialFiltersMap[filterTableName];
	specialFilter.generateDiv(topFilterId, color);
}

function editFilter(specialFilterId, filterTableName, color, topFilterId)
{
	var specialFilter = specialFiltersMap[filterTableName];
	var specialFilters = getFilterById(topFilterId).specialFilters;
	var filter = getSpecialFilterById(specialFilters, specialFilterId);
	specialFilter.setFilter(filter);
	specialFilter.edit(specialFilterId, color, specialFilters, topFilterId);
}

function checkAllSpecialFiltersToDelete(obj)
{
	for(var i = 0; i < filterConfigurations.length; i++)
	{
		var filter = filterConfigurations[i];
		var topFilterId = filter.id;
		var specialFilters = filter.specialFilters;
		for(var j = 0; j < specialFilters.length; j++)
		{
			var specialFilterId = specialFilters[j].id;
			var specialFilterCheckBox = document.getElementById("checkbox_" + topFilterId + "_" + specialFilterId);
			if(specialFilterCheckBox)
			{
				specialFilterCheckBox.checked = obj.checked;
				checkSpecialFilterToDel(specialFilterCheckBox);
			}
		}
	}
}

function checkSpecialFilterToDel(obj)
{
	var specialFilter = new Object();
	specialFilter.topFilterId = obj.getAttribute("topFilterId");
	specialFilter.specialFilterId = obj.getAttribute("specialFilterId");
	specialFilter.filterTable = obj.getAttribute("filterTable");
	specialFilter.firstColor = obj.getAttribute("firstColor");
	if(obj.checked)
	{
		checkedSpecialFilters = addToArray(checkedSpecialFilters, specialFilter);
	}
	else
	{
		checkedSpecialFilters = removeFromArray(checkedSpecialFilters, specialFilter);
	}
}

function contains(array, obj)
{
	for(var i = 0; i < array.length; i++)
	{
		if(array[i])
		{
			if(array[i].topFilterId == obj.topFilterId
				&& array[i].specialFilterId == obj.specialFilterId)
			{
				return true;
			}
		}
	}
	return false;
}

function addToArray(array, obj)
{
	if(! contains(array, obj))
	{
		array.push(obj);
	}
	return array;
}

function removeFromArray(array, obj)
{
	for(var i = 0; i < array.length; i++)
	{
		if(array[i])
		{
			if(array[i].topFilterId == obj.topFilterId
				&& array[i].specialFilterId == obj.specialFilterId)
			{
				array[i] = null;
			}
		}
	}
	return array;
}

function removeCheckedFilters()
{
	if(isChooseOne())
	{
		var specialFiltersStr = buildSpecialFiltersStr();
		if(confirm(confirmDeleteFilter))
		{
			var obj = {checkedSpecialFilters : specialFiltersStr};
			sendAjax(obj, "deleteSpecialFilters", "deleteSpecialFiltersCallback");
		}
	}
	else
	{
		alert(shouldChooseOneFilter);
		return;
	}
	deleteSpecialFiltersCallback.checkedSpecialFilters = checkedSpecialFilters;
}

function exportFilters(){
	if(isChooseOne()){
		var specialFiltersStr = buildExportSpecialFiltersStr();
		fpForm.action = exportUrl +"&param="+specialFiltersStr;
		fpForm.submit();
	}else{
		alert("Please select at least one filter!");
	}
}

function importFilters(){
	fpForm.action = importsUrl;
	fpForm.submit();
}

function isChooseOne()
{
	for(var i = 0; i < checkedSpecialFilters.length; i++)
	{
		if(checkedSpecialFilters[i])
		{
			return true;
		}
	}
	return false;
}

function buildExportSpecialFiltersStr()
{
	var str = new StringBuffer(":");
	for(var i = 0; i < checkedSpecialFilters.length; i++)
	{
		var specialFilter = checkedSpecialFilters[i];
		if(specialFilter)
		{
			str.append(specialFilter.topFilterId);
			str.append(",");
			str.append(specialFilter.specialFilterId);
			str.append(",");
			str.append(specialFilter.filterTable);
			str.append(":");
		}
	}
	return str.toString();
}

function buildSpecialFiltersStr()
{
	var str = new StringBuffer(":");
	for(var i = 0; i < checkedSpecialFilters.length; i++)
	{
		var specialFilter = checkedSpecialFilters[i];
		if(specialFilter)
		{
			str.append(specialFilter.topFilterId);
			str.append(",");
			str.append(specialFilter.specialFilterId);
			str.append(",");
			str.append(specialFilter.filterTable);
			str.append(",");
			str.append(specialFilter.firstColor);
			str.append(":");
		}
	}
	return str.toString();
}

function buildRemoveExistsFiltersForFP(filterInfos)
{
	var str = new StringBuffer("<table style="+removeExitsFilterTableStyle+">");
	str.append("<tr class='main_table_head_tr'><td class='main_table_head_label' width='40%'>" + jsFilterName 
			+ "</td><td class='main_table_head_label'>" + lbReferencedFileProfileName + "</td><tr>");
	var isOdd = true;
	for(var i = 0; i < filterInfos.length; i++)
	{
		var backColor = "#DFE3EE";
		if(isOdd)
		{
			isOdd = false;
		}
		else
		{
			backColor = "#C7CEE0";
			isOdd = true;
		}
		var filterInfo = filterInfos[i];
		var filterId = filterInfo.filterId;
		var filterTableName = filterInfo.filterTableName;
		var specialFilter = getSpecialFilterByIdAndFilterTableName(filterId, filterTableName);
		str.append("<tr style='background-color:"+backColor+";padding:4px'>");
		str.append("<td class='main_table_tr_label'>");
		str.append(specialFilter.filterName);
		str.append("</td>");
		str.append("<td class='main_table_tr_label'>");
		str.append(filterInfo.fileProfileName);
		str.append("</td>");
		str.append("</tr>");
	}
	str.append("</table>");
	return str.toString();
}

function buildRemoveExistsFiltersForUsedJobs(usedJobs)
{
	var str = new StringBuffer("<table style="+removeExitsFilterTableStyle+">");
	str.append("<tr class='main_table_head_tr'><td class='main_table_head_label' width='40%'>" + jsFilterName 
			+ "</td><td class='main_table_head_label'>" + lbReferencedJobName + "</td><tr>");
	var isOdd = true;
	for(var i = 0; i < usedJobs.length; i++)
	{
		var backColor = "#DFE3EE";
		if(isOdd)
		{
			isOdd = false;
		}
		else
		{
			backColor = "#C7CEE0";
			isOdd = true;
		}
		var filterInfo = usedJobs[i];
		var filterId = filterInfo.filterId;
		var filterTableName = filterInfo.filterTableName;
		var specialFilter = getSpecialFilterByIdAndFilterTableName(filterId, filterTableName);
		var usedJobName = filterInfo.jobName;
		str.append("<tr style='background-color:"+backColor+";padding:4px'>");
		str.append("<td class='main_table_tr_label'>");
		str.append(specialFilter.filterName);
		str.append("</td>");
		str.append("<td class='main_table_tr_label'>");
		str.append(usedJobName);
		str.append("</td>");
		str.append("</tr>");
	}
	str.append("</table>");
	return str.toString();
}

function buildRemoveExistsFiltersForUsedFilters(usedFilters)
{
	var str = new StringBuffer("<table style="+removeExitsFilterTableStyle+">");
	str.append("<tr class='main_table_head_tr'><td class='main_table_head_label' width='40%'>" + jsFilterName 
			+ "</td><td class='main_table_head_label'>" + lbReferencedFilterName + "</td><tr>");
	var isOdd = true;
	for(var i = 0; i < usedFilters.length; i++)
	{
		var backColor = "#DFE3EE";
		if(isOdd)
		{
			isOdd = false;
		}
		else
		{
			backColor = "#C7CEE0";
			isOdd = true;
		}
		var filterInfo = usedFilters[i];
		var filterId = filterInfo.filterId;
		var filterTableName = filterInfo.filterTableName;
		var specialFilter = getSpecialFilterByIdAndFilterTableName(filterId, filterTableName);
		var usedFilterID = filterInfo.usedFilterID;
		var usedFilterTableName = filterInfo.usedFilterTableName;
		var usedFilter = getSpecialFilterByIdAndFilterTableName(usedFilterID, usedFilterTableName);
		str.append("<tr style='background-color:"+backColor+";padding:4px'>");
		str.append("<td class='main_table_tr_label'>");
		str.append(specialFilter.filterName);
		str.append("</td>");
		str.append("<td class='main_table_tr_label'>");
		str.append(usedFilter.filterName);
		str.append("</td>");
		str.append("</tr>");
	}
	str.append("</table>");
	return str.toString();
}

function deleteSpecialFiltersCallback(data)
{
	var rmInfos = eval(data);
	var isExistInFileProfile = rmInfos[0].isExistInFileProfile;
	var isUsedInJobs = rmInfos[0].isUsedInJob;
	var isUsedByFilters = rmInfos[0].isUsedByFilters;
	if(rmInfos && rmInfos.length > 0 && rmInfos[0].isDeleted == 'false')
	{
		alert(deleteFilterFailure);
		return;
	}
	if(rmInfos && rmInfos.length > 0 && (isExistInFileProfile || isUsedByFilters || isUsedInJobs))
	{
		var tableHtml = "",temp, len=0;
		if(isExistInFileProfile)
		{
			temp = buildRemoveExistsFiltersForFP(rmInfos[0].filterInfos);
			tableHtml=tableHtml+temp+"<br/>";
			len = len+rmInfos[0].filterInfos.length;
		}
		if(isUsedByFilters)
		{
			temp = buildRemoveExistsFiltersForUsedFilters(rmInfos[0].usedFilters);
			tableHtml=tableHtml+temp+"<br/>";
			len = len+rmInfos[0].usedFilters.length;
		}
		if (isUsedInJobs)
		{
			temp = buildRemoveExistsFiltersForUsedJobs(rmInfos[0].usedJobs);
			tableHtml=tableHtml+temp+"<br/>";
			len = len+rmInfos[0].usedJobs.length;
		}
		document.getElementById("removeExistsFiltersLable").innerHTML = removeExistsFiltersLableMsg;
		document.getElementById("removeExistsFiltersTableContent").innerHTML = tableHtml.toString();
		showPopupDialog("removeExistsFiltersDialog");
		//alert(deleteFiltersExistInFileProfile);
		return;
	}
	var deletedSpecialFilters = deleteSpecialFiltersCallback.checkedSpecialFilters;
	for(var i = 0; i < deletedSpecialFilters.length; i++)
	{
		var specialFilter = deletedSpecialFilters[i];
		if(specialFilter)
		{
			var specialFilters = getFilterById(specialFilter.topFilterId).specialFilters;
			var filter = getSpecialFilterById(specialFilters, specialFilter.specialFilterId);
			specialFilters = deleteFilterById(specialFilters, specialFilter.specialFilterId);
			reGenerateFilterList(specialFilter.topFilterId, specialFilters, specialFilter.firstColor);
		}
	}
	//Clear all the checked special filters.
	alert(deleteFilterSuccessful);
	checkedSpecialFilters = new Array();
}

function deleteFilter(specialFilterId, filterTableName, color, topFilterId)
{
	if(confirm(confirmDeleteFilter)){
		var specialFilter = specialFiltersMap[filterTableName];
		var specialFilters = getFilterById(topFilterId).specialFilters;
		var filter = getSpecialFilterById(specialFilters, specialFilterId);
		specialFilter.setFilter(filter);
		//specialFilter.deleteFilter(specialFilterId, color, specialFilters, topFilterId);
		var obj = {filterTableName:filterTableName, filterId:specialFilterId};
		sendAjax(obj, "deleteFilter", "deleteFilterCallback");
		deleteFilterCallback.filterId = specialFilterId;
		deleteFilterCallback.specialFilters = specialFilters;
		deleteFilterCallback.topFilterId = topFilterId;
		deleteFilterCallback.color = color;
	}
}

function deleteFilterCallback(data)
{
	if(data == 'false')
	{
		alert(deleteFilterFailure);
		return;
	}
	if(data == 'deleteFilterExistInFileProfile')
	{
		alert(deleteFilterExistInFileProfile);
		return;
	}
	alert(deleteFilterSuccessful);
	var specialFilters = deleteFilterById(deleteFilterCallback.specialFilters, deleteFilterCallback.filterId);
	reGenerateFilterList(deleteFilterCallback.topFilterId, specialFilters, deleteFilterCallback.color);
}

function deleteFilterById(filters, filterId)
{
	for(var i = 0; i < filters.length; i++)
	{
		if(filters[i].id == filterId)
		{
			filters[i] = '0';
		}
	}
	return filters;
}

function updateSpecialFilter(specialFilters, filter)
{
	for(var i = 0; i < specialFilters.length; i++)
	{
		var specialFilter = specialFilters[i];
		if(specialFilter.id == filter.id)
		{
			specialFilters[i] = filter;
		}
	}
	return specialFilters;
}

function closePopupDialog(dialogId)
{
	document.getElementById(dialogId).style.display = 'none';
}

function showPopupDialog(dialogId)
{
	document.getElementById(dialogId).style.display = "";
}

function getSpecialFilterById(filters, filterId)
{
	for(var i = 0; i < filters.length; i++)
	{
		var filter = filters[i];
		if(filter.id == filterId)
		{
			return filter;
		}
	}
	
}

function getFilterById(filterId)
{
	return getSpecialFilterById(filterConfigurations, filterId);
}

function getMaxSpecialFilterId(specialfilters)
{
	var max = 0;
	for(var i = 0; i < specialfilters.length; i++)
	{
		var filter = specialfilters[i];
		if(filter.id > max)
		{
			max = filter.id;
		}
	}
	return max;
}

function getTopFilterByFilterTableName(filterTableName)
{
	for(var i = 0; i < filterConfigurations.length; i++)
	{
		var filter = filterConfigurations[i];
		if(filter.filterTableName == filterTableName)
		{
			return filter;
		}
	}
}

function getSpecialFilterByIdAndFilterTableName(specialFilterId, filterTableName)
{
	var topFilter = getTopFilterByFilterTableName(filterTableName);
	return getSpecialFilterById(topFilter.specialFilters, specialFilterId);
}

function reGenerateFilterList(topFilterId, specialFilters, color)
{
	document.getElementById("filterName_" + topFilterId).innerHTML = 
		generateSpecialFiltersTable(topFilterId, specialFilters, color);
}

function closeAllFilterDialogs()
{
	for(var i = 0; i < allFilterDialogIds.length; i++)
	{
		var dialogId = allFilterDialogIds[i];
		closePopupDialog(dialogId);
	}
}

function mouseEnter(objId)
{
	document.getElementById(objId).style.display = "inline";
}

function mouseOut(objId)
{
	document.getElementById(objId).style.display = "none";
}

function getIEVersion()
{
    var version = 0; 
    if(navigator.appName.indexOf("Internet Explorer") != -1)
    {
        var tmp = navigator.appVersion.split("MSIE");
        version = parseFloat(tmp[1]);
    }
    return version;
}

function generateBaseFilterList(filterTableName, filter)
{
	//baseFilter.alertObject(filterTableName);
	//baseFilter.alertObject(filter);
	isBaseFilterSelectChanged = false;
	var bfs = existBaseFilters;
	var str = new StringBuffer("<select id='" + filterTableName + "_baseFilterSelect' ");
	
	if (filterTableName == "java_properties_filter" || filterTableName == "po_filter")
	{
		str.append("style='width:200px'");
	}
	else
	{
		str.append("class='xml_filter_select'");
	}
	
	str.append(" onchange='javascript:isBaseFilterSelectChanged = true;'>");
	str.append("<option value='-2'>" + jsChoose + "</option>");
	if(bfs){
		for(var i = 0; i < bfs.length; i++)
		{
			var bf = bfs[i];
			
			if (!baseFilter.isDefined(bf)
					|| !baseFilter.isDefined(bf.filterName)
					|| !baseFilter.isDefined(bf.id))
			{
				continue;
			}
			
			var selected = "";
			if(filter && filter.baseFilterId == bf.id)
			{
				selected = "selected";
			}
			str.append("<option value='"+bf.id+"' "+selected+">"+bf.filterName+"</option>");
		}
	}
	str.append("</select>");
	return str.toString();
}

function alertUserBaseFilter(baseFilterId)
{
	if (isBaseFilterSelectChanged && baseFilterId && baseFilterId > 0)
	{
		alert(jsAlertBaseFilter);
	}	
}
