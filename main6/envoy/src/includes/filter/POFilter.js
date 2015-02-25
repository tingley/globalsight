function POFilter()
{
	this.filterTableName = "po_filter";
	this.filterDialogId = "poFilterDialog";
	this.filterPopupContentId = "poFilterPopupContent";
	this.filterNameId = "poFilterName";
	this.filterDesId = "poFilterDes";
	this.textWidth = getWidthNum(this.filterDialogId)-200;
}

POFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

POFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:"+this.textWidth+"' maxlength='"+maxFilterNameLength+"' id='"+this.filterNameId+"' value='" + this.filter.filterName + "'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:"+this.textWidth+"' id='"+this.filterDesId+"' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsSecondaryFilter);
	str.append(":</lable></td><td>");
	str.append(this.getSecondaryFilterSelect(this.filter));
	str.append("</td></tr>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='specialFilter_dialog_label'>" +  generateBaseFilterList(this.filterTableName, this.filter) + "</td>");
	str.append("</tr></table>");
	
	var dialogObj = document.getElementById(this.filterPopupContentId);
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	savePOFilter.edit = true;
	savePOFilter.filterId = filterId;
	savePOFilter.color = color;
	savePOFilter.filter = this.filter;
	savePOFilter.specialFilters = specialFilters;
	savePOFilter.topFilterId = topFilterId;
}

POFilter.prototype.generateDiv = function (topFilterId, color)
{
	var defaultName = getFilterNameByTableName(this.filterTableName);
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:"+this.textWidth+"' maxlength='"+maxFilterNameLength+"' id='"+this.filterNameId+"' value='" + defaultName +"'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:"+this.textWidth+"' id='"+this.filterDesId+"' name='desc'></textarea>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsSecondaryFilter);
	str.append(":</lable></td><td>");
	var filter = getFilterById(topFilterId);
	str.append(this.getSecondaryFilterSelect(filter));
	str.append("</td></tr>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='specialFilter_dialog_label'>" + generateBaseFilterList(this.filterTableName) + "</td>");
	str.append("</tr>");
	str.append("</table>");
	
	var dialogObj = document.getElementById(this.filterPopupContentId);
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	savePOFilter.edit = false;
	savePOFilter.topFilterId = topFilterId;
	savePOFilter.color = color;
}

POFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.filterDialogId);
}

function savePOFilter()
{
	var check = new Validate();
	POFilter();
	var isNew = (savePOFilter.edit) ? "false" : "true";
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
	var filterId = savePOFilter.filterId;
	var filterDesc = document.getElementById(this.filterDesId).value;
	
	var secondaryFilterIdAndTableName = document.getElementById("secondaryFilterSelect").value;
	var index = secondaryFilterIdAndTableName.indexOf("-");
	var secondFilterId = -2;
	var secondFilterTableName = "";
	if (index > 0)
	{
		secondFilterId = secondaryFilterIdAndTableName.substring(0,index);
		secondFilterTableName = secondaryFilterIdAndTableName.substring(index+1);
	}
	
	var baseFilterId = document.getElementById("po_filter_baseFilterSelect").value;
	alertUserBaseFilter(baseFilterId);
	
	var obj = {filterTableName:this.filterTableName, isNew:isNew, filterName:filterName, filterId:filterId, filterDesc:filterDesc, companyId:companyId, secondFilterId:secondFilterId, secondFilterTableName:secondFilterTableName,baseFilterId:baseFilterId};

	sendAjax(obj, "checkExist", "checkExistPOFilterCallback");
	
	checkExistPOFilterCallback.obj = obj;
}

function updatePOFilterCallback(data)
{
	var color = savePOFilter.color;
	var filterId = savePOFilter.filterId;
	var filter = savePOFilter.filter;
	var topFilterId = savePOFilter.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = this.filterTableName;
		jpFilter.filterName = checkExistPOFilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistPOFilterCallback.obj.filterDesc;
		jpFilter.secondFilterId = checkExistPOFilterCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistPOFilterCallback.obj.secondFilterTableName;
		jpFilter.companyId = companyId;
		jpFilter.baseFilterId = checkExistPOFilterCallback.obj.baseFilterId;
		
		var specialFilters = updateSpecialFilter(savePOFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistPOFilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog(this.filterDialogId);
		if(savePOFilter.edit)
		{
			sendAjax(checkExistPOFilterCallback.obj, "updatePOFilter", "updatePOFilterCallback");
		}
		else
		{
			sendAjax(checkExistPOFilterCallback.obj, "savePOFilter", "savePOFilterCallback");
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

function savePOFilterCallback(data)
{
	var color = savePOFilter.color;
	var topFilterId = savePOFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = this.filterTableName;
		jpFilter.filterName = checkExistPOFilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistPOFilterCallback.obj.filterDesc;
		jpFilter.companyId = companyId;
		jpFilter.secondFilterId = checkExistPOFilterCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistPOFilterCallback.obj.secondFilterTableName;
		jpFilter.baseFilterId = checkExistPOFilterCallback.obj.baseFilterId;
		
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

POFilter.prototype.getSecondaryFilterSelect = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='secondaryFilterSelect' style='width:"+this.textWidth+"' class='specialFilter_dialog_label'>");
	str.append("<option value='-1'>" + jsChoose + "</option>");

	if(_filterConfigurations)
	{
		for(var i = 0; i < _filterConfigurations.length; i++)
		{
			var _filter = _filterConfigurations[i];
	        if (_filter.filterTableName == "html_filter"
	        	|| _filter.filterTableName == "xml_rule_filter")
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
		        		
		        		var secondFiterId = filter.secondFilterId;
		        		var secondFilterTableName = filter.secondFilterTableName;
		        		var id = filter.id;
		        		
		        		var selected = ""; 
		        		if (_id == secondFiterId && _filterTableName == secondFilterTableName)
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
//	alert("str :" + str);
	
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

