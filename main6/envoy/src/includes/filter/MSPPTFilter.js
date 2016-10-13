function MSPPTFilter()
{
	this.filterTableName = "ms_office_ppt_filter";
	this.filterDialogId = "msOfficePPTFilterDialog";
	this.filterPopupContentId = "msOfficePPTFilterPopupContent";
	this.filterNameId = "pptFilterName";
	this.filterDesId = "pptFilterDes";
	this.textWidth = getWidthNum(this.filterDialogId)-150;//subWidth(this.filterDialogId, 150);
}

MSPPTFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

MSPPTFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' maxlength='"+maxFilterNameLength+"' id='"+this.filterNameId+"' value='" + this.filter.filterName + "'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:450px' id='"+this.filterDesId+"' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("</td></tr></table>");
	
	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckExtractAlt = (this.filter.altTranslate) ? "checked":"";
	str.append("<input id='pptAltTranslate' type='checkbox' name='pptAltTranslate' value='"+this.filter.altTranslate+"' "+isCheckExtractAlt+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractNotes + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var notesTranslate = (this.filter.notesTranslate) ? "checked":"";
	str.append("<input id='pptNotesTranslate' type='checkbox' name='pptNotesTranslate' value='"+this.filter.notesTranslate+"' "+notesTranslate+"/>");
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
	
	var dialogObj = document.getElementById(this.filterPopupContentId);
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMSPPTFilter.edit = true;
	saveMSPPTFilter.filterId = filterId;
	saveMSPPTFilter.color = color;
	saveMSPPTFilter.filter = this.filter;
	saveMSPPTFilter.specialFilters = specialFilters;
	saveMSPPTFilter.topFilterId = topFilterId;
}

MSPPTFilter.prototype.generateDiv = function (topFilterId, color)
{
	var filter = getFilterById(topFilterId);
	var defaultName = getFilterNameByTableName(this.filterTableName);
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' maxlength='"+maxFilterNameLength+"' id='"+this.filterNameId+"' value='" + defaultName +"'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:450px' id='"+this.filterDesId+"' name='desc'></textarea>");
	str.append("</td></tr></table>");
	
	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='pptAltTranslate' type='checkbox' name='pptAltTranslate' value='false' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractNotes + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='pptNotesTranslate' type='checkbox' name='pptNotesTranslate' value='false' />");
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
	
	var dialogObj = document.getElementById(this.filterPopupContentId);
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMSPPTFilter.edit = false;
	saveMSPPTFilter.topFilterId = topFilterId;
	saveMSPPTFilter.color = color;
}

MSPPTFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.filterDialogId);
}

function saveMSPPTFilter()
{
	var check = new Validate();
	MSPPTFilter();
	var isNew = (saveMSPPTFilter.edit) ? "false" : "true";
	var filterName = document.getElementById(this.filterNameId).value;
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
	
	var filterId = saveMSPPTFilter.filterId;
	var filterDesc = document.getElementById(this.filterDesId).value;
	var altTranslate = document.getElementById("pptAltTranslate").checked;
	var notesTranslate = document.getElementById("pptNotesTranslate").checked;
	
	var contentPostFilterIdAndTableName = document.getElementById("pptContentPostFilterSelect").value;
	var contentPostFilterIndex = contentPostFilterIdAndTableName.indexOf("-");
	var contentPostFilterId = -2;
	var contentPostFilterTableName = "";
	if (contentPostFilterIndex > 0)
	{
		contentPostFilterId = contentPostFilterIdAndTableName.substring(0,contentPostFilterIndex);
		contentPostFilterTableName = contentPostFilterIdAndTableName.substring(contentPostFilterIndex+1);
	}
	var baseFilterId = document.getElementById(this.filterTableName + "_baseFilterSelect").value;
	
	alertUserBaseFilter(baseFilterId);
	
	var obj = {
		filterTableName : this.filterTableName,
		isNew : isNew,
		filterId : filterId,
		filterName : filterName,
		filterDesc : filterDesc,
		companyId : companyId,
		altTranslate : altTranslate,
		notesTranslate : notesTranslate,
		contentPostFilterId : contentPostFilterId,
		contentPostFilterTableName : contentPostFilterTableName,
		baseFilterId:baseFilterId
	};
	sendAjax(obj, "checkExist", "checkExistPPTCallback");
	
	checkExistPPTCallback.obj = obj;
}

function updatePPTFilterCallback(data)
{
	var color = saveMSPPTFilter.color;
	var filterId = saveMSPPTFilter.filterId;
	var filter = saveMSPPTFilter.filter;
	var topFilterId = saveMSPPTFilter.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = this.filterTableName;
		jpFilter.filterName = checkExistPPTCallback.obj.filterName;
		jpFilter.filterDescription = checkExistPPTCallback.obj.filterDesc;
		jpFilter.contentPostFilterId = checkExistPPTCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistPPTCallback.obj.contentPostFilterTableName;
		jpFilter.companyId = companyId;
		jpFilter.altTranslate = checkExistPPTCallback.obj.altTranslate;
		jpFilter.notesTranslate = checkExistPPTCallback.obj.notesTranslate;
		jpFilter.baseFilterId = checkExistPPTCallback.obj.baseFilterId;
		
		var specialFilters = updateSpecialFilter(saveMSPPTFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistPPTCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog(this.filterDialogId);
		if(saveMSPPTFilter.edit)
		{
			sendAjax(checkExistPPTCallback.obj, "updateMSOfficePPTFilter", "updatePPTFilterCallback");
		}
		else
		{
			sendAjax(checkExistPPTCallback.obj, "saveMSOfficePPTFilter", "saveMSOfficePPTFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog(this.filterDialogId);
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}

function saveMSOfficePPTFilterCallback(data)
{
	var color = saveMSPPTFilter.color;
	var topFilterId = saveMSPPTFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = this.filterTableName;
		jpFilter.filterName = checkExistPPTCallback.obj.filterName;
		jpFilter.filterDescription = checkExistPPTCallback.obj.filterDesc;
		jpFilter.companyId = companyId;
		jpFilter.contentPostFilterId = checkExistPPTCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistPPTCallback.obj.contentPostFilterTableName;
		jpFilter.altTranslate = checkExistPPTCallback.obj.altTranslate;
		jpFilter.notesTranslate = checkExistPPTCallback.obj.notesTranslate;
		jpFilter.baseFilterId = checkExistPPTCallback.obj.baseFilterId;
		
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

MSPPTFilter.prototype.generateContentPostFilter = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='pptContentPostFilterSelect' class='xml_filter_select'>");
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

function getWidthNum(id)
{
	var result = document.getElementById(id).style.width;
	var index = result.indexOf("px");
	if(index>0)
	{
		result = result.substring(0,index);
	}
	
	return result;
}

