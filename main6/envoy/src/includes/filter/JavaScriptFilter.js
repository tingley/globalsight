function JavaScriptFilter()
{
	this.filterTableName = "java_script_filter";
}

JavaScriptFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

JavaScriptFilter.prototype.showDialog = function ()        
{
	closeAllFilterDialogs();
	showPopupDialog("javaScriptFilterDialog");
}

JavaScriptFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' class='filterName_small_dialog' id='javaScriptFilterName' value='" + this.filter.filterName + "' disabled></input>");
	str.append("<br/>");
	
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	
	str.append("<textarea rows='4' cols='20' id='javaScriptDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br/>");
	
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsJSFunctionText + ":");
	str.append("</label>");
	
	str.append("<input type='text' style='width:161px' id='javaScriptJsFunctionText' value='" + this.filter.jsFunctionText + "'></input>");
	str.append("<br/>");
	
	var isCheckEscape = (this.filter.enableUnicodeEscape) ? "checked":"";
	str.append("<input id='enableUnicodeEscape' type='checkbox' name='enableUnicodeEscape' value='"+this.filter.enableEscapeEntity+"' "+isCheckEscape+"/>");
	str.append("<label class='specialFilter_dialog_label'>" + jsEnableUnicodeEscape + "</label>");
	str.append("<br/>");
	
	var dialogObj = document.getElementById('javaScriptFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	saveJavaScript.edit = true;
	saveJavaScript.filterId = filterId;
	saveJavaScript.color = color;
	saveJavaScript.filter = this.filter;
	saveJavaScript.specialFilters = specialFilters;
	saveJavaScript.topFilterId = topFilterId;
}

JavaScriptFilter.prototype.generateDiv = function(topFilterId, color)
{
	var str = new StringBuffer("<table><tr><td><label style='width:40px'class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	
	str.append("<td><input type='text' style='width:161px' maxlength='"+maxFilterNameLength+"' id='javaScriptFilterName' value='Java Script Filter'></input></td>");
	
	str.append("<tr><td><label style='width:40px' class='specialFilter_dialog_label filterName_small_dialog'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	
	str.append("<td><textarea style='width:161px' id='javaScriptDesc' name='desc'></textarea>");
	str.append("<br/></td></tr>");
	  
	str.append("<tr><td><label style='width:40px' class='specialFilter_dialog_label'>");
	str.append(jsJSFunctionText + ":");
	str.append("</label></td>");
	
	str.append("<td><input type='text' style='width:161px' id='javaScriptJsFunctionText' value=''></input>");
	str.append("<br/></td><tr>");
	
	str.append("<tr><td colspan=2><input id='enableUnicodeEscape' type='checkbox' name='enableUnicodeEscape' value='true'/>");
	str.append("<label class='specialFilter_dialog_label'>" + jsEnableUnicodeEscape + "</label>");
	str.append("<br/></td></tr></table>");
	
	var dialogObj = document.getElementById('javaScriptFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveJavaScript.edit = false;
	saveJavaScript.topFilterId = topFilterId;
	saveJavaScript.color = color;
}

function saveJavaScript()
{
	var validate = new Validate();
	var filterName = document.getElementById("javaScriptFilterName").value;
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
	
	var filterDesc = document.getElementById("javaScriptDesc").value;
	var jsFunctionText = document.getElementById("javaScriptJsFunctionText").value;
	if(validate.isEmptyStr(jsFunctionText))
	{
		alert(emptyJsFunctionText);
		return;
	}
	
	if(validate.containSpecialChar(jsFunctionText))
    {
        alert(invalidL10nChar + invalidChars);
        return;
    }  
	var enableUnicodeEscape = document.getElementById("enableUnicodeEscape").checked;
	var obj = {filterTableName:"java_script_filter", filterName:filterName, filterDesc:filterDesc, jsFunctionText:jsFunctionText, companyId:companyId, enableUnicodeEscape:enableUnicodeEscape};
	if(saveJavaScript.edit)
	{
		closePopupDialog("javaScriptFilterDialog");
		sendAjax(obj, "updateJavaScriptFilter", "updateJavaScriptFilterCallback");
	}
	else
	{
		sendAjax(obj, "checkExist", "checkExistJavaScriptCallback");
	}
	
	checkExistJavaScriptCallback.obj = obj;
}

function checkExistJavaScriptCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("javaScriptFilterDialog");
		sendAjax(checkExistJavaScriptCallback.obj, "saveJavaScriptFilter", "saveJavaScriptFilterCallback");
	}
	else
	{
		alert(existFilterName + checkExistJavaScriptCallback.obj.filterName);
	}
}

function saveJavaScriptFilterCallback(data)
{
	var color = saveJavaScript.color;
	var topFilterId = saveJavaScript.topFilterId;
	
	var filter = getFilterById(topFilterId);
	if(filter)
	{
		var jsFilter = new Object();
		jsFilter.id = data - 0;
		jsFilter.filterTableName = "java_script_filter";
		jsFilter.filterName = checkExistJavaScriptCallback.obj.filterName;
		jsFilter.filterDescription = checkExistJavaScriptCallback.obj.filterDesc;
		jsFilter.jsFunctionText = checkExistJavaScriptCallback.obj.jsFunctionText;
		jsFilter.enableUnicodeEscape = checkExistJavaScriptCallback.obj.enableUnicodeEscape;
		jsFilter.companyId = companyId;
		filter.specialFilters.push(jsFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

function updateJavaScriptFilterCallback(data)
{
	var color = saveJavaScript.color;
	var filterId = saveJavaScript.filterId;
	var filter = saveJavaScript.filter;
	var topFilterId = saveJavaScript.topFilterId;
	if(filter)
	{
		var jsFilter = new Object();
		jsFilter.id = filterId;
		jsFilter.filterTableName = "java_script_filter";
		jsFilter.filterName = checkExistJavaScriptCallback.obj.filterName;
		jsFilter.filterDescription = checkExistJavaScriptCallback.obj.filterDesc;
		jsFilter.jsFunctionText = checkExistJavaScriptCallback.obj.jsFunctionText;
		jsFilter.enableUnicodeEscape = checkExistJavaScriptCallback.obj.enableUnicodeEscape;
		jsFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveJavaScript.specialFilters, jsFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}