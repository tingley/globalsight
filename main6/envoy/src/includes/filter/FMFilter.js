function FMFilter()
{
	this.filterTableName = "frame_maker_filter";
}

FMFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

FMFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:150' maxlength='"+maxFilterNameLength+"' id='fmFilterName' value='" + this.filter.filterName + "'></input>");
	str.append("<br/></td></tr>");
	str.append("<tr><td><label style='width:40' class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' cols='17' style='width:150' id='fmDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br/></td></tr></table>");
	str.append("<br/>");
	
	var isCheckLeftMasterPage = (this.filter.isExposeLeftMasterPage) ? "checked":"";
	var isCheckRightMasterPage = (this.filter.isExposeRightMasterPage) ? "checked":"";
	var isCheckOtherMasterPage = (this.filter.isExposeOtherMasterPage) ? "checked":"";
	
	str.append("<table border=0 width='250px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExposeLeftMasterPage + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='checkLeftMasterPage' type='checkbox' name='checkLeftMasterPage' value='"+this.filter.isExposeLeftMasterPage+"' "+isCheckLeftMasterPage+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExposeRightMasterPage + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='checkRightMasterPage' type='checkbox' name='checkRightMasterPage' value='"+this.filter.isExposeRightMasterPage+"' "+isCheckRightMasterPage+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExposeOtherMasterPage + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='checkOtherMasterPage' type='checkbox' name='checkOtherMasterPage' value='"+this.filter.isExposeOtherMasterPage+"' "+isCheckOtherMasterPage+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('fmFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveFMFilter.edit = true;
	saveFMFilter.filterId = filterId;
	saveFMFilter.color = color;
	saveFMFilter.filter = this.filter;
	saveFMFilter.specialFilters = specialFilters;
	saveFMFilter.topFilterId = topFilterId;
}

FMFilter.prototype.generateDiv = function (topFilterId, color)
{
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:150' maxlength='"+maxFilterNameLength+"' id='fmFilterName' value='FrameMaker 9 Filter'></input>");
	str.append("<br/></td></tr>");
	str.append("<tr><td><label style='width:40' class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea rows='4' cols='17' style='width:150' id='fmDesc' name='desc'></textarea>");
	str.append("<br/></td></tr></table>");
	str.append("<br/>");
	
	str.append("<table border=0 width='250px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExposeLeftMasterPage + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='checkLeftMasterPage' type='checkbox' name='checkLeftMasterPage' value='true'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExposeRightMasterPage + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='checkRightMasterPage' type='checkbox' name='checkRightMasterPage' value='true'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExposeOtherMasterPage + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='checkOtherMasterPage' type='checkbox' name='checkOtherMasterPage' value='true'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('fmFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveFMFilter.edit = false;
	saveFMFilter.topFilterId = topFilterId;
	saveFMFilter.color = color;
}

FMFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("fmFilterDialog");
}

function saveFMFilter()
{
	var check = new Validate();
	var filterName = document.getElementById("fmFilterName").value;
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
	var isNew = (saveFMFilter.edit) ? "false" : "true";
	var filterDesc = document.getElementById("fmDesc").value;
	//var checkFootNote = document.getElementById("checkFootNote").checked;
	var checkLeftMasterPage = document.getElementById("checkLeftMasterPage").checked;
	var checkRightMasterPage = document.getElementById("checkRightMasterPage").checked;
	var checkOtherMasterPage = document.getElementById("checkOtherMasterPage").checked;
	var obj = {isNew : isNew, filterId : saveFMFilter.filterId, filterTableName:"frame_maker_filter", filterName:filterName, 
			filterDesc:filterDesc, isExposeFootNote:true, isExposeLeftMasterPage:checkLeftMasterPage, 
			isExposeRightMasterPage:checkRightMasterPage, isExposeOtherMasterPage:checkOtherMasterPage, companyId:companyId};
		
	sendAjax(obj, "checkExist", "checkExistFMFilterCallback");
	
	checkExistFMFilterCallback.obj = obj;
}

function updateFMFilterCallback(data)
{
	var color = saveFMFilter.color;
	var filterId = saveFMFilter.filterId;
	var filter = saveFMFilter.filter;
	var topFilterId = saveFMFilter.topFilterId;
	if(filter)
	{
		var fmFilter = new Object();
		fmFilter.id = filterId;
		fmFilter.filterTableName = "frame_maker_filter";
		fmFilter.filterName = checkExistFMFilterCallback.obj.filterName;
		fmFilter.filterDescription = checkExistFMFilterCallback.obj.filterDesc;
		//fmFilter.isExposeFootNote = checkExistFMFilterCallback.obj.isExposeFootNote;
		fmFilter.isExposeLeftMasterPage = checkExistFMFilterCallback.obj.isExposeLeftMasterPage;
		fmFilter.isExposeRightMasterPage = checkExistFMFilterCallback.obj.isExposeRightMasterPage;
		fmFilter.isExposeOtherMasterPage = checkExistFMFilterCallback.obj.isExposeOtherMasterPage;
		fmFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveFMFilter.specialFilters, fmFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistFMFilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("fmFilterDialog");
		if(saveFMFilter.edit)
		{
			sendAjax(checkExistFMFilterCallback.obj, "updateFMFilter", "updateFMFilterCallback");
		}
		else
		{
			sendAjax(checkExistFMFilterCallback.obj, "saveFMFilter", "saveFMFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("fmFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}

function saveFMFilterCallback(data)
{
	var color = saveFMFilter.color;
	var topFilterId = saveFMFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{        
		var fmFilter = new Object();
		
		fmFilter.id = data - 0;
		fmFilter.filterTableName = "frame_maker_filter";
		fmFilter.filterName = checkExistFMFilterCallback.obj.filterName;
		fmFilter.filterDescription = checkExistFMFilterCallback.obj.filterDesc;
		//fmFilter.isExposeFootNote = checkExistFMFilterCallback.obj.isExposeFootNote;
		fmFilter.isExposeLeftMasterPage = checkExistFMFilterCallback.obj.isExposeLeftMasterPage;
		fmFilter.isExposeRightMasterPage = checkExistFMFilterCallback.obj.isExposeRightMasterPage;
		fmFilter.isExposeOtherMasterPage = checkExistFMFilterCallback.obj.isExposeOtherMasterPage;
		fmFilter.companyId = companyId;
		filter.specialFilters.push(fmFilter);  
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

