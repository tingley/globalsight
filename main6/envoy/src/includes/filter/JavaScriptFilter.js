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
	var str = new StringBuffer("<table border=0 width='400px'><tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterName + ":");
	str.append("</td>");
	str.append("<td><input type='text' style='width:100%' maxlength='"+maxFilterNameLength+"' id='javaScriptFilterName' value='" + this.filter.filterName + "'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterDesc + ":");
	str.append("</td>");
	str.append("<td><textarea style='width:100%' id='javaScriptDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr></table>");
	
	str.append("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td>");
	str.append("<br />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsJSFunctionText + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='text' style='width:161px' id='javaScriptJsFunctionText' value='" + this.filter.jsFunctionText + "'></input>");
	str.append("</td>");
	str.append("</tr>");
	
	var isCheckEscape = (this.filter.enableUnicodeEscape) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsEnableUnicodeEscape + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='enableUnicodeEscape' type='checkbox' name='enableUnicodeEscape' value='"+this.filter.enableEscapeEntity+"' "+isCheckEscape+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName, this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
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
	var str = new StringBuffer("<table border=0 width='400px'><tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterName + ":");
	str.append("</td>");
	str.append("<td><input type='text' style='width:100%' maxlength='"+maxFilterNameLength+"' id='javaScriptFilterName' value='Java Script Filter'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterDesc + ":");
	str.append("</td>");
	str.append("<td><textarea style='width:100%' id='javaScriptDesc' name='desc'></textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr></table>");
	
	str.append("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td>");
	str.append("<br />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsJSFunctionText + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='text' style='width:161px' id='javaScriptJsFunctionText' value=''></input>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsEnableUnicodeEscape + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='enableUnicodeEscape' type='checkbox' name='enableUnicodeEscape' value='true'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
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
	var isNew = (saveJavaScript.edit) ? "false" : "true";
	var filterId = saveJavaScript.filterId;
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
	
	var baseFilterId = document.getElementById("java_script_filter_baseFilterSelect").value;
	var enableUnicodeEscape = document.getElementById("enableUnicodeEscape").checked;
	var obj = {isNew:isNew, 
			filterTableName:"java_script_filter", 
			filterId:filterId, 
			filterName:filterName, 
			filterDesc:filterDesc, 
			jsFunctionText:jsFunctionText, 
			companyId:companyId, 
			enableUnicodeEscape:enableUnicodeEscape,
			baseFilterId : baseFilterId
			};

	sendAjax(obj, "checkExist", "checkExistJavaScriptCallback");
	
	checkExistJavaScriptCallback.obj = obj;
}

function checkExistJavaScriptCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("javaScriptFilterDialog");
		if(saveJavaScript.edit)
		{
			sendAjax(checkExistJavaScriptCallback.obj, "updateJavaScriptFilter", "updateJavaScriptFilterCallback");
		}
		else
		{
			sendAjax(checkExistJavaScriptCallback.obj, "saveJavaScriptFilter", "saveJavaScriptFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("javaScriptFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
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