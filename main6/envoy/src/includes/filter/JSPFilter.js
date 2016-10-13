function JSPFilter()
{
	this.filterTableName = "jsp_filter";
}

JSPFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

JSPFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<table border=0 width='400px'><tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterName + ":");
	str.append("</td>");
	str.append("<td><input type='text' style='width:100%' maxlength='"+maxFilterNameLength+"' id='jspFilterName' value='" + this.filter.filterName + "'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterDesc + ":");
	str.append("</td>");
	str.append("<td><textarea style='width:100%' id='jspDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr></table>");
	
	
	var isCheckHeadAdded = (this.filter.isAdditionalHeadAdded) ? "checked":"";
	var isCheckEscape = (this.filter.isEscapeEntity) ? "checked":"";
	
	str.append("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td>");
	str.append("<br />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='70%'>" + jsAddAdditionalHead + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='addAdditionalHead' type='checkbox' name='addAdditionalHead' value='"+this.filter.isAdditionalHeadAdded+"' "+isCheckHeadAdded+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='70%'>" + jsEnableEntityEscape + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='isEscapeEntity' type='checkbox' name='isEscapeEntity' value='"+this.filter.enableEscapeEntity+"' "+isCheckEscape+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName, this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('jspFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveJSPFilter.edit = true;
	saveJSPFilter.filterId = filterId;
	saveJSPFilter.color = color;
	saveJSPFilter.filter = this.filter;
	saveJSPFilter.specialFilters = specialFilters;
	saveJSPFilter.topFilterId = topFilterId;
}

JSPFilter.prototype.generateDiv = function (topFilterId, color)
{
	var str = new StringBuffer("<table border=0 width='400px'><tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterName + ":");
	str.append("</td>");
	str.append("<td><input type='text' style='width:100%' maxlength='"+maxFilterNameLength+"' id='jspFilterName' value='JSP Filter'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	
	str.append("<tr><td class='specialFilter_dialog_label' width='80px;'>");
	str.append(jsFilterDesc + ":");
	str.append("</td>");
	str.append("<td><textarea style='width:100%' id='jspDesc' name='desc'></textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr></table>");
	
	str.append("<table border=0 width='400px'>");
	str.append("<tr>");
	str.append("<td>");
	str.append("<br />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='70%'>" + jsAddAdditionalHead + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='addAdditionalHead' type='checkbox' name='addAdditionalHead' value='true'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='70%'>" + jsEnableEntityEscape + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='isEscapeEntity' type='checkbox' name='isEscapeEntity' value='true'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsInternalTextPostFilter + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('jspFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveJSPFilter.edit = false;
	saveJSPFilter.topFilterId = topFilterId;
	saveJSPFilter.color = color;
}

JSPFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("jspFilterDialog");
	
  if(window.navigator.userAgent.indexOf("Firefox")>0) {
      document.getElementById("jspFilterDialog").style.height = 280;
  }
}

function saveJSPFilter()
{
	var check = new Validate();
	var isNew = (saveJSPFilter.edit) ? "false" : "true";
	var filterName = document.getElementById("jspFilterName").value;
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
	
	var filterDesc = document.getElementById("jspDesc").value;
	var addAdditionalHead = document.getElementById("addAdditionalHead").checked;
	var isEscapeEntity = document.getElementById("isEscapeEntity").checked;
	var baseFilterId = document.getElementById("jsp_filter_baseFilterSelect").value;
	var obj = {isNew : isNew, 
			filterId : saveJSPFilter.filterId, 
			filterTableName:"jsp_filter", 
			filterName:filterName, 
			filterDesc:filterDesc, 
			isAdditionalHeadAdded:addAdditionalHead, 
			isEscapeEntity:isEscapeEntity, 
			companyId:companyId,
			baseFilterId : baseFilterId
			};

	sendAjax(obj, "checkExist", "checkExistJSPFilterCallback");
	
	checkExistJSPFilterCallback.obj = obj;
}

function updateJSPFilterCallback(data)
{
	var color = saveJSPFilter.color;
	var filterId = saveJSPFilter.filterId;
	var filter = saveJSPFilter.filter;
	var topFilterId = saveJSPFilter.topFilterId;
	if(filter)
	{
		var jspFilter = new Object();
		jspFilter.id = filterId;
		jspFilter.filterTableName = "jsp_filter";
		jspFilter.filterName = checkExistJSPFilterCallback.obj.filterName;
		jspFilter.filterDescription = checkExistJSPFilterCallback.obj.filterDesc;
		jspFilter.isAdditionalHeadAdded = checkExistJSPFilterCallback.obj.isAdditionalHeadAdded;
		jspFilter.isEscapeEntity = checkExistJSPFilterCallback.obj.isEscapeEntity;
		jspFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveJSPFilter.specialFilters, jspFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistJSPFilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("jspFilterDialog");
		if(saveJSPFilter.edit)
		{
			sendAjax(checkExistJSPFilterCallback.obj, "updateJSPFilter", "updateJSPFilterCallback");
		}
		else
		{
			sendAjax(checkExistJSPFilterCallback.obj, "saveJSPFilter", "saveJSPFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("jspFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}
function saveJSPFilterCallback(data)
{
	var color = saveJSPFilter.color;
	var topFilterId = saveJSPFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{        
		var jspFilter = new Object();
		
		jspFilter.id = data - 0;
		jspFilter.filterTableName = "jsp_filter";
		jspFilter.filterName = checkExistJSPFilterCallback.obj.filterName;
		jspFilter.filterDescription = checkExistJSPFilterCallback.obj.filterDesc;
		jspFilter.isAdditionalHeadAdded = checkExistJSPFilterCallback.obj.isAdditionalHeadAdded;
		jspFilter.isEscapeEntity = checkExistJSPFilterCallback.obj.isEscapeEntity;
		jspFilter.companyId = companyId;
		filter.specialFilters.push(jspFilter);  
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}
