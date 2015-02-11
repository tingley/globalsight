var plaintextFilter = new PlainTextFilter(); 
	
function PlainTextFilter()
{
	this.filterTableName = "plain_text_filter";
	this.dialogId = "plaintextFilterDialog";
}

PlainTextFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

PlainTextFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

PlainTextFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");	
	str.append("<td><input type='text' style='width:100%' id='plaintextFilterName' maxlength='"+maxFilterNameLength+"' value='" + this.filter.filterName + "'></input>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");	
	str.append("<td><textarea style='width:100%' rows='4' id='plaintextFilterDesc' name='desc'>"+this.filter.filterDescription+"</textarea>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='400px'>");	
	str.append("<tr>");
	str.append("<td>");
	str.append("<br />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName, this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('plaintextFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	savePlainTextFilter.edit = true;
	savePlainTextFilter.filterId = filterId;
	savePlainTextFilter.color = color;
	savePlainTextFilter.filter = this.filter;
	savePlainTextFilter.specialFilters = specialFilters;
	savePlainTextFilter.topFilterId = topFilterId;
}

PlainTextFilter.prototype.generateDiv = function(topFilterId, color)
{
	var defaultName = getFilterNameByTableName(this.filterTableName);
	var str = new StringBuffer("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");	
	str.append("<td><input type='text' style='width:100%' id='plaintextFilterName' maxlength='"+maxFilterNameLength+"' value='" + defaultName + "'></input>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");	
	str.append("<td><textarea style='width:100%' rows='4' id='plaintextFilterDesc' name='desc'></textarea>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='400px'>");	
	str.append("<tr>");
	str.append("<td>");
	str.append("<br />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('plaintextFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	savePlainTextFilter.edit = false;
	savePlainTextFilter.topFilterId = topFilterId;
	savePlainTextFilter.color = color;
}

function savePlainTextFilter()
{
	var validate = new Validate();
	var filterId = savePlainTextFilter.filterId;
	var filterName = document.getElementById("plaintextFilterName").value;
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
    
    var isNew = (savePlainTextFilter.edit) ? "false" : "true";
	var filterDesc = document.getElementById("plaintextFilterDesc").value;
	var baseFilterId = document.getElementById("plain_text_filter_baseFilterSelect").value;
	
	//alertUserBaseFilter(baseFilterId);

	var obj = {
		isNew : isNew,
		filterTableName : "plain_text_filter",
		filterId : filterId,
		filterName : filterName,
		filterDesc : filterDesc,
		filterId : savePlainTextFilter.filterId,
		companyId : companyId,
		baseFilterId : baseFilterId
	};

	// send for check
	sendAjax(obj, "isFilterValid", "isPlainTextFilterValidCallback");
	
	isFilterValidCallback.obj = obj;
}

function isPlainTextFilterValidCallback(data)
{
	if(data == 'true')
	{
		closePopupDialog("plaintextFilterDialog");
		if(savePlainTextFilter.edit)
		{
			sendAjax(isFilterValidCallback.obj, "updatePlainTextFilter", "updatePlainTextFilterCallback");
		}
		else
		{
			sendAjax(isFilterValidCallback.obj, "savePlainTextFilter", "savePlainTextFilterCallback");
		}
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

function updatePlainTextFilterCallback(data)
{
	var color = savePlainTextFilter.color;
	var filterId = savePlainTextFilter.filterId;
	var filter = savePlainTextFilter.filter;
	var topFilterId = savePlainTextFilter.topFilterId;
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = filterId;
		xrFilter.filterTableName = "plain_text_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.baseFilterId = isFilterValidCallback.obj.baseFilterId;
		xrFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(savePlainTextFilter.specialFilters, xrFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function savePlainTextFilterCallback(data)
{
	var color = savePlainTextFilter.color;
	var topFilterId = savePlainTextFilter.topFilterId;
	
	var filter = getFilterById(topFilterId);
	if(filter)
	{
		var xrFilter = new Object();
		xrFilter.id = data - 0;
		xrFilter.filterTableName = "plain_text_filter";
		xrFilter.filterName = isFilterValidCallback.obj.filterName;
		xrFilter.filterDescription = isFilterValidCallback.obj.filterDesc;
		xrFilter.baseFilterId = isFilterValidCallback.obj.baseFilterId;
		xrFilter.companyId = companyId;
		filter.specialFilters.push(xrFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}
