function InddFilter() {
	this.filterTableName = "indd_filter";
}

InddFilter.prototype.setFilter = function(filter) {
	this.filter = filter;
}

InddFilter.prototype.showDialog = function() {
	closeAllFilterDialogs();
	showPopupDialog("inddFilterDialog");
}

InddFilter.prototype.edit = function(filterId, color, specialFilters,
		topFilterId) {
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");

	str.append("<input type='text' style='' id='inddFilterName' maxlength='"
					+ maxFilterNameLength
					+ "' value='"
					+ this.filter.filterName + "' disabled></input>");
	str.append("<br/>");

	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");

	str.append("<textarea rows='4' cols='17' id='inddFilterDesc' name='desc' value='"
					+ this.filter.filterDescription
					+ "'>"
					+ this.filter.filterDescription + "</textarea>");
	str.append("<br/>");
	str.append("<br/>");

	str.append("<table border=0 width='250px'>");

	var selectStr = (this.filter && this.filter.translateHiddenLayer) ? "checked"
			: "";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTransHiddenLayer + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='transInddHiddenLayer' " + selectStr
			+ "></input>");
	str.append("</td>");
	str.append("</tr>");
	
	selectStr = (this.filter && this.filter.translateMasterLayer) ? "checked"
			: "";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTransMasterLayer + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='transInddMasterLayer' " + selectStr
			+ "></input>");
	str.append("</td>");
	str.append("</tr>");
	
	selectStr = (this.filter && this.filter.translateFileInfo) ? "checked"
			: "";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTransFileInfo + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='transInddFileInfo' " + selectStr
			+ "></input>");
	str.append("</td>");
	str.append("</tr>");
	
	selectStr = (this.filter && this.filter.extractLineBreak) ? ""
			: "checked";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsIgnoreLineBreak + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='ignoreLineBreak' " + selectStr
			+ "></input>");
	str.append("</td>");
	str.append("</tr>");
	
	selectStr = (this.filter && this.filter.replaceNonbreakingSpace) ? "checked"
			: "";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsReplaceNonbreakingSpace + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='replaceNonbreakingSpace' " + selectStr
			+ "></input>");
	str.append("</td>");
	str.append("</tr>");

	str.append("</table>");

	var dialogObj = document.getElementById('inddFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();

	saveInddFilter.edit = true;
	saveInddFilter.filterId = filterId;
	saveInddFilter.color = color;
	saveInddFilter.filter = this.filter;
	saveInddFilter.specialFilters = specialFilters;
	saveInddFilter.topFilterId = topFilterId;
}

InddFilter.prototype.generateDiv = function(topFilterId, color) {
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");

	str.append("<td><input type='text' style='width:170px' id='inddFilterName' maxlength='"
					+ maxFilterNameLength + "' value='InDesign Filter'></input>");
	str.append("</td></tr>");

	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");

	str.append("<td><textarea rows='4' style='width:170px' id='inddFilterDesc' name='desc'></textarea>");
	str.append("</td></tr></table>");
	str.append("<br/>");
	
	str.append("<table border=0 width='250px'>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTransHiddenLayer + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='transInddHiddenLayer'></input>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTransMasterLayer + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='transInddMasterLayer' checked></input>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTransFileInfo + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='transInddFileInfo'></input>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsIgnoreLineBreak + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='ignoreLineBreak'></input>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsReplaceNonbreakingSpace + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input type='checkbox' id='replaceNonbreakingSpace' ></input>");
	str.append("</td>");
	str.append("</tr>");

	str.append("</table>");

	var dialogObj = document.getElementById('inddFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveInddFilter.edit = false;
	saveInddFilter.topFilterId = topFilterId;
	saveInddFilter.color = color;
}

function saveInddFilter() {
	var validate = new Validate();
	var filterName = document.getElementById("inddFilterName").value;
	if (validate.isEmptyStr(filterName)) {
		alert(emptyFilterName);
		return;
	}
	if (validate.containSpecialChar(filterName)) {
		alert(invalidFilterNameChar + invalidChars);
		return;
	}
	filterName = validate.trim(filterName);
	if (validate.isExceedMaxCount(filterName, maxFilterNameLength)) {
		alert(exceedFilterName + maxFilterNameLength);
		return;
	}

	var filterDesc = document.getElementById("inddFilterDesc").value;

	var translateHiddenLayer = document.getElementById("transInddHiddenLayer").checked;
	var translateMasterLayer = document.getElementById("transInddMasterLayer").checked;
	var translateFileInfo = document.getElementById("transInddFileInfo").checked;
	var extractLineBreak = document.getElementById("ignoreLineBreak").checked ? false : true;
	var replaceNonbreakingSpace = document.getElementById("replaceNonbreakingSpace").checked;
	var obj = {
		filterTableName : "indd_filter",
		filterName : filterName,
		filterDesc : filterDesc,
		companyId : companyId,
		translateHiddenLayer : translateHiddenLayer,
		translateMasterLayer : translateMasterLayer,
		translateFileInfo : translateFileInfo,
		extractLineBreak : extractLineBreak,
		replaceNonbreakingSpace : replaceNonbreakingSpace
	};
	if (saveInddFilter.edit) {
		closePopupDialog("inddFilterDialog");
		sendAjax(obj, "updateInddFilter", "updateInddFilterCallback");
	} else {
		sendAjax(obj, "checkExist", "checkExistInddFilterCallback");
	}

	checkExistInddFilterCallback.obj = obj;
}

function checkExistInddFilterCallback(data) {
	if (data == 'false') {
		closePopupDialog("inddFilterDialog");
		sendAjax(checkExistInddFilterCallback.obj, "saveInddFilter",
				"saveInddFilterCallback");
	} else {
		alert(existFilterName);
	}
}

function saveInddFilterCallback(data) {
	var color = saveInddFilter.color;
	var topFilterId = saveInddFilter.topFilterId;

	var filter = getFilterById(topFilterId);
	if (filter) {
		var jsFilter = new Object();
		jsFilter.id = data - 0;
		jsFilter.filterTableName = "indd_filter";
		jsFilter.filterName = checkExistInddFilterCallback.obj.filterName;
		jsFilter.filterDescription = checkExistInddFilterCallback.obj.filterDesc;
		jsFilter.translateHiddenLayer = checkExistInddFilterCallback.obj.translateHiddenLayer;
		jsFilter.translateMasterLayer = checkExistInddFilterCallback.obj.translateMasterLayer;
		jsFilter.translateFileInfo = checkExistInddFilterCallback.obj.translateFileInfo;
		jsFilter.extractLineBreak = checkExistInddFilterCallback.obj.extractLineBreak;
		jsFilter.replaceNonbreakingSpace = checkExistInddFilterCallback.obj.replaceNonbreakingSpace;
		jsFilter.companyId = companyId;
		filter.specialFilters.push(jsFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

function updateInddFilterCallback(data) {
	var color = saveInddFilter.color;
	var filterId = saveInddFilter.filterId;
	var filter = saveInddFilter.filter;
	var topFilterId = saveInddFilter.topFilterId;
	if (filter) {
		var jsFilter = new Object();
		jsFilter.id = filterId;
		jsFilter.filterTableName = "indd_filter";
		jsFilter.filterName = checkExistInddFilterCallback.obj.filterName;
		jsFilter.filterDescription = checkExistInddFilterCallback.obj.filterDesc;
		jsFilter.translateHiddenLayer = checkExistInddFilterCallback.obj.translateHiddenLayer;
		jsFilter.translateMasterLayer = checkExistInddFilterCallback.obj.translateMasterLayer;
		jsFilter.translateFileInfo = checkExistInddFilterCallback.obj.translateFileInfo;
		jsFilter.extractLineBreak = checkExistInddFilterCallback.obj.extractLineBreak;
		jsFilter.replaceNonbreakingSpace = checkExistInddFilterCallback.obj.replaceNonbreakingSpace;
		jsFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveInddFilter.specialFilters,
				jsFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}