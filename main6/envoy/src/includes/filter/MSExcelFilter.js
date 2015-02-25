function MSExcelFilter()
{
	this.filterTableName = "ms_office_excel_filter";
}

MSExcelFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

MSExcelFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' maxlength='" + maxFilterNameLength + "' id='excelFilterName' value='" + this.filter.filterName + "'  />");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea style='width:450px' rows='4' id='excelDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("</td></tr></table>");
	
	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckedExtractAlt = (this.filter.altTranslate) ? "checked":"";
	str.append("<input id='excelAltTranslate' type='checkbox' name='excelAltTranslate' value='"+this.filter.altTranslate+"' "+isCheckedExtractAlt+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractExcelTabNames + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckedExtractTabNames = (this.filter.tabNamesTranslate) ? "checked":"";
	str.append("<input id='excelTabNamesTranslate' type='checkbox' name='excelTabNamesTranslate' value='"+this.filter.tabNamesTranslate+"' "+isCheckedExtractTabNames+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append(jsContentPostFilter);
	str.append("</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.generateContentPostFilter(this.filter));
	str.append("</td></tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append(jsInternalTextPostFilter);
	str.append("</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName, this.filter));
	str.append("</td></tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('msOfficeExcelFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMsOfficeExcelFilter.edit = true;
	saveMsOfficeExcelFilter.filterId = filterId;
	saveMsOfficeExcelFilter.color = color;
	saveMsOfficeExcelFilter.filter = this.filter;
	saveMsOfficeExcelFilter.specialFilters = specialFilters;
	saveMsOfficeExcelFilter.topFilterId = topFilterId;
}

MSExcelFilter.prototype.generateDiv = function (topFilterId, color)
{
	var filter = getFilterById(topFilterId);
	
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' id='excelFilterName' maxlength='"+maxFilterNameLength+"' value='" + getFilterNameByTableName('ms_office_excel_filter')+"'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	
	str.append("<td><textarea style='width:450px' rows='4' id='excelDesc' name='desc'></textarea>");
	str.append("</td></tr></table>");

	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='excelAltTranslate' type='checkbox' name='excelAltTranslate' value='false' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractExcelTabNames + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='excelTabNamesTranslate' type='checkbox' name='excelTabNamesTranslate' value='false' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append(jsContentPostFilter);
	str.append("</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.generateContentPostFilter(filter));
	str.append("</td></tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append(jsInternalTextPostFilter);
	str.append("</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td></tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('msOfficeExcelFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMsOfficeExcelFilter.edit = false;
	saveMsOfficeExcelFilter.topFilterId = topFilterId;
	saveMsOfficeExcelFilter.color = color;
}

MSExcelFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("msOfficeExcelFilterDialog");
}


function saveMsOfficeExcelFilter()
{
	var check = new Validate();
	var isNew = (saveMsOfficeExcelFilter.edit) ? "false" : "true";
	var filterName = document.getElementById("excelFilterName").value;
	if(check.isEmptyStr(filterName))
	{
		alert(emptyFilterName);
		return;
	}
	if(check.containSpecialChar(filterName))
    {
        alert(invalidFilterNameChar + invalidChars);
        return;
    }  
	filterName = check.trim(filterName);
    if(check.isExceedMaxCount(filterName, maxFilterNameLength))
    {
        alert(exceedFilterName + maxFilterNameLength);
        return;
    }
	var filterId = saveMsOfficeExcelFilter.filterId;
	var filterDesc = document.getElementById("excelDesc").value;
	var altTranslate = document.getElementById("excelAltTranslate").checked;
	var tabNamesTranslate = document.getElementById("excelTabNamesTranslate").checked;
	
	var contentPostFilterIdAndTableName = document.getElementById("excelContentPostFilterSelect").value;
	var contentPostFilterIndex = contentPostFilterIdAndTableName.indexOf("-");
	var contentPostFilterId = -2;
	var contentPostFilterTableName = "";
	if (contentPostFilterIndex > 0)
	{
		contentPostFilterId = contentPostFilterIdAndTableName.substring(0,contentPostFilterIndex);
		contentPostFilterTableName = contentPostFilterIdAndTableName.substring(contentPostFilterIndex+1);
	}
	var baseFilterId = document.getElementById("ms_office_excel_filter_baseFilterSelect").value;
	
	alertUserBaseFilter(baseFilterId);
	
	var obj = {
			filterTableName:"ms_office_excel_filter", 
			isNew:isNew,
			filterId:filterId,
			filterName:filterName, 
			filterDesc:filterDesc, 
			companyId:companyId,
			altTranslate:altTranslate,
			tabNamesTranslate:tabNamesTranslate,
			contentPostFilterId:contentPostFilterId,
			contentPostFilterTableName:contentPostFilterTableName,
			baseFilterId:baseFilterId
			};
	
		sendAjax(obj, "checkExist", "checkExistExcelCallback");
	
		checkExistExcelCallback.obj = obj;
}

function updateExcelFilterCallback(data)
{
	var color = saveMsOfficeExcelFilter.color;
	var filterId = saveMsOfficeExcelFilter.filterId;
	var filter = saveMsOfficeExcelFilter.filter;
	var topFilterId = saveMsOfficeExcelFilter.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = "ms_office_excel_filter";
		jpFilter.filterName = checkExistExcelCallback.obj.filterName;
		jpFilter.filterDescription = checkExistExcelCallback.obj.filterDesc;
		jpFilter.altTranslate = checkExistExcelCallback.obj.altTranslate;
		jpFilter.tabNamesTranslate = checkExistExcelCallback.obj.tabNamesTranslate;
		jpFilter.contentPostFilterId = checkExistExcelCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistExcelCallback.obj.contentPostFilterTableName;
		jpFilter.companyId = companyId;
		jpFilter.baseFilterId = checkExistExcelCallback.obj.baseFilterId;
		
		var specialFilters = updateSpecialFilter(saveMsOfficeExcelFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistExcelCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("msOfficeExcelFilterDialog");
		if(saveMsOfficeExcelFilter.edit)
		{
			sendAjax(checkExistExcelCallback.obj, "updateMSOfficeExcelFilter", "updateExcelFilterCallback");
		}
		else
		{
			sendAjax(checkExistExcelCallback.obj, "saveMSOfficeExcelFilter", "saveExcelFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("msOfficeExcelFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}
function saveExcelFilterCallback(data)
{
	var color = saveMsOfficeExcelFilter.color;
	var topFilterId = saveMsOfficeExcelFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = "ms_office_excel_filter";
		jpFilter.filterName = checkExistExcelCallback.obj.filterName;
		jpFilter.filterDescription = checkExistExcelCallback.obj.filterDesc;
		jpFilter.altTranslate = checkExistExcelCallback.obj.altTranslate;
		jpFilter.tabNamesTranslate = checkExistExcelCallback.obj.tabNamesTranslate;
		jpFilter.companyId = companyId;
		jpFilter.contentPostFilterId = checkExistExcelCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistExcelCallback.obj.contentPostFilterTableName;
		jpFilter.baseFilterId = checkExistExcelCallback.obj.baseFilterId;
		
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

MSExcelFilter.prototype.generateContentPostFilter = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='excelContentPostFilterSelect' class='xml_filter_select'>");
	str.append("<option value='-1'>" + jsChoose + "</option>");

	if(_filterConfigurations)
	{
		for(var i = 0; i < _filterConfigurations.length; i++)
		{
			var _filter = _filterConfigurations[i];
	        if (_filter.filterTableName == "html_filter")
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
		        		
		        		var contentPostFilterId = filter.contentPostFilterId;
		        		var contentPostFilterTableName = filter.contentPostFilterTableName;
		        		var id = filter.id;
		        		
		        		var selected = ""; 
		        		if (_id == contentPostFilterId && _filterTableName == contentPostFilterTableName)
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
	str.append("</select>");
	
	return str.toString();
}
