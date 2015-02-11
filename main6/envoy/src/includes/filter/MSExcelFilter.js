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
	str.append("<td><input type='text' style='width:150' maxlength='"+maxFilterNameLength+"' id='excelFilterName' value='" + this.filter.filterName + "' disabled></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:150' id='excelDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsSecondaryFilter);
	str.append(":</lable></td><td>");
	str.append(this.getSecondaryFilterSelectForJP(this.filter));
	str.append("</td></tr></table>");
	
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
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:150' maxlength='"+maxFilterNameLength+"' id='excelFilterName' value='" + getFilterNameByTableName('ms_office_excel_filter')+"'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' style='width:150' id='excelDesc' name='desc'></textarea>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsSecondaryFilter);
	str.append(":</lable></td><td>");
	var filter = getFilterById(topFilterId);
	str.append(this.getSecondaryFilterSelectForJP(filter));
	str.append("</td></tr></table>");
	
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

    /*if(window.navigator.userAgent.indexOf("Firefox")>0) {
        document.getElementById("javaPropertiesFilterDialog").style.height = 280;
    }*/
}


function saveMsOfficeExcelFilter()
{
	var check = new Validate();
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
	
	var filterDesc = document.getElementById("excelDesc").value;
	
	var secondaryFilterIdAndTableName = document.getElementById("secondaryFilterSelect").value;
	var index = secondaryFilterIdAndTableName.indexOf("-");
	var secondFilterId = -2;
	var secondFilterTableName = "";
	if (index > 0)
	{
		secondFilterId = secondaryFilterIdAndTableName.substring(0,index);
		secondFilterTableName = secondaryFilterIdAndTableName.substring(index+1);
	}
	
	var obj = {filterTableName:"ms_office_excel_filter", filterName:filterName, filterDesc:filterDesc, companyId:companyId, secondFilterId:secondFilterId, secondFilterTableName:secondFilterTableName};
	if(saveMsOfficeExcelFilter.edit)
	{
		closePopupDialog("msOfficeExcelFilterDialog");
		sendAjax(obj, "updateMSOfficeExcelFilter", "updateExcelFilterCallback");
	}
	else
	{
		sendAjax(obj, "checkExist", "checkExistExcelCallback");
	}
	
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
		jpFilter.secondFilterId = checkExistExcelCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistExcelCallback.obj.secondFilterTableName;
		jpFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveMsOfficeExcelFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistExcelCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("msOfficeExcelFilterDialog");
		sendAjax(checkExistExcelCallback.obj, "saveMSOfficeExcelFilter", "saveExcelFilterCallback");
	}
	else
	{
		alert(existFilterName + checkExistExcelCallback.obj.filterName);
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
		jpFilter.companyId = companyId;
		jpFilter.secondFilterId = checkExistExcelCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistExcelCallback.obj.secondFilterTableName;
		
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

MSExcelFilter.prototype.getSecondaryFilterSelectForJP = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='secondaryFilterSelect' style='width:150' class='specialFilter_dialog_label'>");
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

