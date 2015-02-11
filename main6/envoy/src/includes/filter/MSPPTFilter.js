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
	var isCheckExtractAlt = (this.filter.extractAlt) ? "checked":"";
	
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:"+this.textWidth+"' maxlength='"+maxFilterNameLength+"' id='"+this.filterNameId+"' value='" + this.filter.filterName + "' disabled></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:"+this.textWidth+"' id='"+this.filterDesId+"' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("</td></tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='ExtractAlt' type='checkbox' name='extractAlt' " + isCheckExtractAlt +"></input></td>");
	str.append("</tr>");
	
	str.append("<tr><td class='htmlFilter_left_td'>");
	str.append(jsSecondaryFilter);
	str.append(":</td><td class='htmlFilter_right_td'>");
	str.append(this.getSecondaryFilterSelectForJP(this.filter));
	str.append("</td></tr></table>");
	
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
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='ExtractAlt' type='checkbox' name='extractAlt' ></input></td>");
	str.append("</tr>");
	
	str.append("<tr><td class='htmlFilter_left_td'>");
	str.append(jsSecondaryFilter);
	str.append(":</td><td class='htmlFilter_right_td'>");
	var filter = getFilterById(topFilterId);
	str.append(this.getSecondaryFilterSelectForJP(filter));
	str.append("</td></tr></table>");
	
	var dialogObj = document.getElementById(this.filterPopupContentId);
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMSPPTFilter.edit = false;
	saveMSPPTFilter.topFilterId = topFilterId;
	saveMSPPTFilter.color = color;
}

/*
genarateHTML(defaultName, this.filterNameId, maxFilterNameLength,
		     jsFilterDesc, null, this.filterDesId, this.textWidth);

function genarateHTML(nameValue, nameValueID, nameValueLength,
					  desText, desValue, desValueID, width)
{
	var nameText = jsFilterName;
	var nameValueID = this.filterNameId;
	
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(nameText + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:"+width+"' maxlength='"+nameValueLength+"' id='"+nameValueID+"' value='" + nameValue +"'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(desText + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:"+width+"' id='"+this.filterDesId+"' name='desc' value='"+desValue+"'></textarea>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsSecondaryFilter);
	str.append(":</lable></td><td>");
	var filter = getFilterById(topFilterId);
	str.append(this.getSecondaryFilterSelectForJP(filter));
	str.append("</td></tr></table>");
	
	return str;
}
*/

MSPPTFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.filterDialogId);
}

function saveMSPPTFilter()
{
	var check = new Validate();
	MSPPTFilter();
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
	
	var extractAlt = document.getElementById("ExtractAlt").checked;
	
	var obj = {
		filterTableName : this.filterTableName,
		filterName : filterName,
		filterDesc : filterDesc,
		companyId : companyId,
		extractAlt : extractAlt,
		secondFilterId : secondFilterId,
		secondFilterTableName : secondFilterTableName
	};
	
	if(saveMSPPTFilter.edit)
	{
		closePopupDialog(this.filterDialogId);
		sendAjax(obj, "updateMSOfficePPTFilter", "updatePPTFilterCallback");
	}
	else
	{
		sendAjax(obj, "checkExist", "checkExistPPTCallback");
	}
	
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
		jpFilter.secondFilterId = checkExistPPTCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistPPTCallback.obj.secondFilterTableName;
		jpFilter.companyId = companyId;
		jpFilter.extractAlt = checkExistPPTCallback.obj.extractAlt;
		var specialFilters = updateSpecialFilter(saveMSPPTFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistPPTCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog(this.filterDialogId);
		sendAjax(checkExistPPTCallback.obj, "saveMSOfficePPTFilter", "saveMSOfficePPTFilterCallback");
	}
	else
	{
		alert(existFilterName + checkExistPPTCallback.obj.filterName);
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
		jpFilter.secondFilterId = checkExistPPTCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistPPTCallback.obj.secondFilterTableName;
		jpFilter.extractAlt = checkExistPPTCallback.obj.extractAlt;
		
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

MSPPTFilter.prototype.getSecondaryFilterSelectForJP = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='secondaryFilterSelect' style='width:"+this.textWidth+"' class='specialFilter_dialog_label'>");
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

