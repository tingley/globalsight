var officeDocFilter = new MSOfficeDocFilter();

function MSOfficeDocFilter()
{
	this.filterTableName = "ms_office_doc_filter";
	
	this.paragraphStyles = "unextractableWordParagraphStyles";
	this.characterStyles = "unextractableWordCharacterStyles";
	this.internalTextStyles = "selectedInternalTextStyles";
	
	this.defaultUnextractableWordParagraphStyles = "DONOTTRANSLATE_para,tw4winExternal";
	this.defaultUnextractableWordCharacterStyles = "DONOTTRANSLATE_char,tw4winExternal";
	this.defaultSelectedInternalTextStyles = "tw4winInternal";
	
	this.selectTagsMap = new Object();
	this.selectTagsMap[this.paragraphStyles] = this.defaultUnextractableWordParagraphStyles;
	this.selectTagsMap[this.characterStyles] = this.defaultUnextractableWordCharacterStyles;
	this.selectTagsMap[this.internalTextStyles] = this.defaultSelectedInternalTextStyles;
	
    this.currentOption = "unextractableWordParagraphStyles";
    this.currentPage = 1;
	this.tagsEveryPage = 10;
	this.order = "asc";
    
	this.allSelectTagsOption = new Array();	
	this.allSelectTagsOption[this.paragraphStyles] = this.defaultUnextractableWordParagraphStyles;
	this.allSelectTagsOption[this.characterStyles] = this.defaultUnextractableWordCharacterStyles;
	this.allSelectTagsOption[this.internalTextStyles] = this.defaultSelectedInternalTextStyles;
}

MSOfficeDocFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
	
	this.optionMap = new Object();
	this.optionMap[this.paragraphStyles] = jsUnextractableWordParagraphStyles;
	this.optionMap[this.characterStyles] = jsUnextractableWordCharacterStyles;
	this.optionMap[this.internalTextStyles] = jsInternalTextCharacterStyles;
}

MSOfficeDocFilter.prototype.showStyleSelectBox = function(isDisabled) {
	document.getElementById("DocUnextractableRule").disabled = isDisabled;
}

MSOfficeDocFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	officeDocFilter.selectTagsMap[this.paragraphStyles] = this.filter.unextractableWordParagraphStyles;
	officeDocFilter.selectTagsMap[this.characterStyles] = this.filter.unextractableWordCharacterStyles;
	officeDocFilter.allSelectTagsOption[this.paragraphStyles] = this.filter.allParagraphStyles;
	officeDocFilter.allSelectTagsOption[this.characterStyles] = this.filter.allCharacterStyles;
	officeDocFilter.selectTagsMap[this.internalTextStyles] = this.filter.selectedInternalTextStyles;
	officeDocFilter.allSelectTagsOption[this.internalTextStyles] = this.filter.allInternalTextStyles;
	
	officeDocFilter.init();
	
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' maxlength='" + maxFilterNameLength + "' id='docFilterName' value='" + this.filter.filterName + "'/>");
	str.append("</td></tr>");
	
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	str.append("<td><textarea style='width:450px' rows='4' id='docDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("</td></tr></table>");
	
	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTranslateHeaderInformation + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckHeaderTranslate = (this.filter.headerTranslate) ? "checked":"";
	str.append("<input id='docHeaderTranslate' type='checkbox' name='docHeaderTranslate' value='"+this.filter.headerTranslate+"' "+isCheckHeaderTranslate+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckExtractAlt = (this.filter.altTranslate) ? "checked":"";
	str.append("<input id='docAltTranslate' type='checkbox' name='docAltTranslate' value='"+this.filter.altTranslate+"' "+isCheckExtractAlt+"/>");
	str.append("</td>");
	str.append("</tr>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractToc + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	var isCheckTocTranslate = (this.filter.TOCTranslate) ? "checked":"";
	str.append("<input id='TOCTranslate' type='checkbox' name='TOCTranslate' value='"+this.filter.TOCTranslate+"' "+isCheckTocTranslate+"/>");
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
	
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(this.generateConfigTable());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('msOfficeDocFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMsOfficeDocFilter.edit = true;
	saveMsOfficeDocFilter.filterId = filterId;
	saveMsOfficeDocFilter.color = color;
	saveMsOfficeDocFilter.filter = this.filter;
	saveMsOfficeDocFilter.specialFilters = specialFilters;
	saveMsOfficeDocFilter.topFilterId = topFilterId;
	
	this.setDeleteButtonStyle();
}

MSOfficeDocFilter.prototype.generateConfigTable = function ()
{
	var str = new StringBuffer("");
	str.append("<table cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append("<tr><td>");
	str.append("<div id='MSOfficeStyleContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='DocUnextractableRule' onchange='officeDocFilter.switchTags(this)'>");
	str.append("<option value='unextractableWordParagraphStyles'>" + jsUnextractableWordParagraphStyles + "</option>");
	str.append("<option value='unextractableWordCharacterStyles'>" + jsUnextractableWordCharacterStyles + "</option>");
	str.append("<option value='selectedInternalTextStyles'>" + jsInternalTextCharacterStyles + "</option>");
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "' onclick='officeDocFilter.onAdd()'>");
	str.append("<input type='button' value='" + jsDelete + "' id='MSDeleteButton' onclick='officeDocFilter.deleteTag()'>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	var tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption].split(",");
	str.append("<div id='styleContent'>");
	str.append("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	var tableContent = officeDocFilter.generateTableByPage(tags, 0);

	if(tableContent)
	{
		str.append(tableContent);
	}
	else
	{
		str.append(officeDocFilter.genenrateEmptyTable());
	}
	
	str.append("</table>");
	str.append("</div>");
	
	str.append("<div>");
	
	var PagePropTotalSize = officeDocFilter.getPageSize();
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=officeDocFilter.prePage()>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=officeDocFilter.nextPage()>" + jsNext + "</a>|");
	str.append("<input id='pageCountMs' size=3 type='text' value="+(officeDocFilter.currentPage+1)+" >");
	str.append(" / <span id='PageMsTotalSize' class='standardText'>" + PagePropTotalSize + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=officeDocFilter.goToPage()>");
	
	str.append("</div>");
	
	return str.toString();
}

MSOfficeDocFilter.prototype.setPageValue = function()
{
	document.getElementById("pageCountMs").value = this.currentPage + 1;
	document.getElementById("PageMsTotalSize").innerHTML = this.getPageSize();
}

MSOfficeDocFilter.prototype.generateDiv = function (topFilterId, color)
{
	officeDocFilter.selectTagsMap[officeDocFilter.paragraphStyles] = "";
	officeDocFilter.selectTagsMap[officeDocFilter.characterStyles] = "";
	officeDocFilter.allSelectTagsOption[officeDocFilter.paragraphStyles] = officeDocFilter.defaultUnextractableWordParagraphStyles;
	officeDocFilter.allSelectTagsOption[officeDocFilter.characterStyles] = officeDocFilter.defaultUnextractableWordCharacterStyles;
	officeDocFilter.selectTagsMap[officeDocFilter.internalTextStyles] = "";
	officeDocFilter.allSelectTagsOption[officeDocFilter.internalTextStyles] = officeDocFilter.defaultSelectedInternalTextStyles;
	
	officeDocFilter.init();
	var filter = getFilterById(topFilterId);
	
	var str = new StringBuffer("<table><tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label></td>");
	str.append("<td><input type='text' style='width:450px' id='docFilterName' maxlength='"+maxFilterNameLength+"' value='" + getFilterNameByTableName('ms_office_doc_filter')+"'></input>");
	str.append("</td></tr>");
	str.append("<tr><td><label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label></td>");
	
	str.append("<td><textarea style='width:450px' rows='4' id='docDesc' name='desc'></textarea>");
	str.append("</td></tr></table>");

	str.append("<table border=0 width='530px'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsTranslateHeaderInformation + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='docHeaderTranslate' type='checkbox' name='docHeaderTranslate' value='false' />");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractAlt + "" + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='docAltTranslate' type='checkbox' name='docAltTranslate' value='false' />");
	str.append("</td>");
	str.append("</tr>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + lbExtractToc + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='TOCTranslate' type='checkbox' name='TOCTranslate' value='false' />");
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
	
	str.append("<tr>");
	str.append("<td colspan='2'>");
	str.append(this.generateConfigTable());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	
	var dialogObj = document.getElementById('msOfficeDocFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	saveMsOfficeDocFilter.edit = false;
	saveMsOfficeDocFilter.color = color;
	saveMsOfficeDocFilter.topFilterId = topFilterId;
	
	this.setDeleteButtonStyle();
}

MSOfficeDocFilter.prototype.prePage = function()
{
	tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
    tags = officeDocFilter.removeEmptyElements(tags.split(","));
    if(tags.length == 0)
    {
       return;
    }
    if(officeDocFilter.currentPage > 0)
	{
    	officeDocFilter.currentPage --;
    	officeDocFilter.generateStyleContent(tags, officeDocFilter.currentPage);
    	officeDocFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

MSOfficeDocFilter.prototype.getPageSize = function()
{
	var tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	tags = officeDocFilter.removeEmptyElements(tags.split(","));
	var itemsTotalCount = tags.length;
	var countPerPage = this.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

MSOfficeDocFilter.prototype.nextPage = function()
{
	tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	tags = officeDocFilter.removeEmptyElements(tags.split(","));
	if(tags.length == 0)
	{
	   return;
	}
	if(officeDocFilter.currentPage < Math.floor(tags.length/officeDocFilter.tagsEveryPage + 1))
	{
		officeDocFilter.currentPage ++;
		var tableContent = officeDocFilter.generateTableByPage(tags,officeDocFilter.currentPage);
		var validate = new Validate();
		if(!validate.isNumber(tableContent))
		{
			var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
			str.append(tableContent);
			str.append("</table>");
			document.getElementById("styleContent").innerHTML = str.toString();
			officeDocFilter.setPageValue();
		}
		else
		{
			officeDocFilter.currentPage --;
			officeDocFilter.setPageValue();
		}		

	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

MSOfficeDocFilter.prototype.goToPage = function()
{
	tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	tags = officeDocFilter.removeEmptyElements(tags.split(","));
	var pageValue = document.getElementById("pageCountMs").value;
	var validate = new Validate();
	if(! validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	if(pageValue < 1 || pageValue > Math.floor(tags.length/officeDocFilter.tagsEveryPage + 1))
	{
		alert(invalidatePageValue.replace("%s", Math.floor(tags.length/officeDocFilter.tagsEveryPage + 1)));
		return;
	}

	officeDocFilter.currentPage = pageValue - 1;
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append(officeDocFilter.generateTableByPage(tags, officeDocFilter.currentPage));
	str.append("</table>");
	document.getElementById("styleContent").innerHTML = str.toString();

}

MSOfficeDocFilter.prototype.addStyle = function()
{
	var validate = new Validate();
	var styleName = new StringBuffer(document.getElementById("styleToAdd").value);

	if(validate.isEmptyStr(styleName.trim()))
	{
		alert(emptyStyle);
		return;
	}
	
	styleName = styleName.trim();
	
	if(validate.containSpecialChars(styleName, "&<>'\","))
	{
	    alert(invalidStyleChar + "& < > ' \" ,");
	    return;
	}
	
	var tagString = new StringBuffer("," + officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption] + ",");
	if(tagString.indexOf("," + styleName.toString() + ",") != -1 )
	{
		alert(existStyle + styleName.toString());
		return;
	}
	
	var allStyles = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	if (allStyles.length > 0 && allStyles.charAt(allStyles.length - 1) != ",")
	{
		allStyles += ","
	}
	allStyles += styleName.toString();
	officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption] = allStyles;
	
	officeDocFilter.generateStyleContent(allStyles.split(","), officeDocFilter.currentPage);	
	
	officeDocFilter.showStyleSelectBox(false);
	closePopupDialog('addStylesDialog');
}

MSOfficeDocFilter.prototype.setDeleteButtonStyle = function() {
	var isDisabled = officeDocFilter.selectTagsMap[officeDocFilter.paragraphStyles].length == 0
			&& officeDocFilter.selectTagsMap[officeDocFilter.characterStyles].length == 0
			&& officeDocFilter.selectTagsMap[officeDocFilter.internalTextStyles].length == 0;

	document.getElementById("MSDeleteButton").disabled = isDisabled;
}

MSOfficeDocFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = officeDocFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteStyleDialog");
	}
}

//for gbs-2599
MSOfficeDocFilter.prototype.selectAll_MSOfficeDocFilter = function()
{
	var selectAll = document.getElementById("selectAll_MSOfficeDocFilter")
	if(selectAll.checked) {
		this.checkAllTagsToDelete();
	} else {
		this.clearAllTagsToDelete();
	}
}

MSOfficeDocFilter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsStyleType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_MSOfficeDocFilter' onclick='officeDocFilter.selectAll_MSOfficeDocFilter()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsStylesToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsStylesCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	var tagTypes = document.getElementById("DocUnextractableRule");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var checkedTags = officeDocFilter.selectTagsMap[value];
		var checkedTagsArray = checkedTags.split(",");
		
		if(officeDocFilter.isNotEmptyArray(checkedTagsArray))
		{
			var backColor = "#DFE3EE";
			if(isOdd)
			{
				isOdd = false;
			}
			else
			{
				backColor = "#C7CEE0";
				isOdd = true;
			}
			
			str.append("<tr style='background-color:"+backColor+";padding:4px'>");
			str.append("<td width='108px'>");
			str.append(text);
			str.append("</td>");	
			str.append("<td width='400px'>");
			
			for(var j = 0; j < checkedTagsArray.length; j++)
			{
				var checkedTag = checkedTagsArray[j];
				if(checkedTag && checkedTag != "" && officeDocFilter.isCheckedTagInDefaultArray(value, checkedTag))
				{
					str.append("<input type='checkbox' name='"+value+"' tagType='" + value + "' tagValue = '"+checkedTag+"' checked>");
					str.append(checkedTag);
					str.append("");
					count ++;
				}
			}
			sum += count;
			str.append("</td>");
			str.append("<td width='22px'>");
			str.append(count);
			str.append("</td>");
			str.append("</tr>");	
		}
	} 
	str.append("</table></center>");
	/*for gbs-2599
	str.append("<a href='#' class='specialfilter_a' onclick='officeDocFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='officeDocFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteStylesDialogLable").innerHTML = jsRemoveStylesNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteStyleTableContent").innerHTML = str.toString();
	return true;
}

MSOfficeDocFilter.prototype.isCheckOrClearAll = function( checkOrClear )
{
    var checkBoxObjs = document.getElementsByName(officeDocFilter.paragraphStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
    
    checkBoxObjs = document.getElementsByName(officeDocFilter.characterStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
    
    checkBoxObjs = document.getElementsByName(officeDocFilter.internalTextStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
}

MSOfficeDocFilter.prototype.checkAllTagsToDelete = function ()
{
	officeDocFilter.isCheckOrClearAll(true);
}

MSOfficeDocFilter.prototype.clearAllTagsToDelete = function()
{
	officeDocFilter.isCheckOrClearAll(false);
}

MSOfficeDocFilter.prototype.isNotEmptyArray = function (array)
{
	for(var i = 0; i < array.length; i++)
	{
		if(array[i] && array[i] != "")
		{
			return true;
		}
	}
	return false;
}

MSOfficeDocFilter.prototype.isCheckedTagInDefaultArray = function(value, checkedTag)
{
    var tagsStringByOption = officeDocFilter.getTagsStringByOption(value);
    return (","+tagsStringByOption+",").indexOf(","+checkedTag+",") != -1;
}

MSOfficeDocFilter.prototype.getTagsStringByOption = function(currentOption)
{
    return officeDocFilter.selectTagsMap[currentOption];
}

MSOfficeDocFilter.prototype.deleteStyles = function()
{
	var paragraphStylesToDelete = document.getElementsByName(officeDocFilter.paragraphStyles);
	var characterStylesToDelete = document.getElementsByName(officeDocFilter.characterStyles);
	var internalTextStylesToDelete = document.getElementsByName(officeDocFilter.internalTextStyles);
		
	officeDocFilter.buildTagsString(paragraphStylesToDelete);
	officeDocFilter.buildTagsString(characterStylesToDelete);
	officeDocFilter.buildTagsString(internalTextStylesToDelete);
	
	closePopupDialog('deleteStyleDialog');
}

MSOfficeDocFilter.prototype.buildTagsString = function(array)
{
	for(var i = 0; i < array.length; i++)
	{
		var a = array[i];
		if(a && a.checked)
		{
			var tagType = a.getAttribute("tagType");
			var tagValue = a.getAttribute("tagValue");
			
			officeDocFilter.removeStyle(tagValue,tagType);
			
			var tagsString = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
			var tags = tagsString.split(",");
			
			var tagLength = tags.length;
			tagLength = (tagLength == 0) ? 1 : tagLength;
			var currentMaxPageNum = Math.floor((tagLength - 1) / officeDocFilter.tagsEveryPage);
			if(officeDocFilter.currentPage > currentMaxPageNum)
			{
				officeDocFilter.currentPage = currentMaxPageNum;
			}
			
			officeDocFilter.setPageValue();
			
			if(tags.length == 0)
			{
				officeDocFilter.generateTotalEmptyTable();
			}
			else
			{
				officeDocFilter.generateStyleContent(tags, officeDocFilter.currentPage);
			}
			
		}
	}
}

MSOfficeDocFilter.prototype.onAdd = function()
{
    showPopupDialog("addStylesDialog");
    document.getElementById("styleToAdd").value = "";
    officeDocFilter.showStyleSelectBox(true);
}

MSOfficeDocFilter.prototype.onDelete = function()
{
	tags = officeDocFilter.selectTagsMap[officeDocFilter.currentOption];
	tags = tags.split(",");
	var selected = false;
	var styles = "";
	for(var i = 0; i < tags.length; i++)
	{
		if(document.getElementById("styles_"+i))
		{
			if (document.getElementById("styles_"+i).checked)
			{
				selected = true;
			}
			else
			{
				if (styles.length > 0)
				{
					styles += ",";
				}
				
				styles += tags[i];
			}
		}		
	}
	
	if (selected)
	{
		if (confirm(confirmDeleteStyles))
		{
			officeDocFilter.selectTagsMap[officeDocFilter.currentOption]= styles;
			officeDocFilter.generateStyleContent(styles.split(","), officeDocFilter.currentPage);
		}
	}
	else
	{
		alert(noStylesToDelete);
	}
}

MSOfficeDocFilter.prototype.switchTags = function(tagsSelectBox)
{
	officeDocFilter.currentOption = tagsSelectBox.options[tagsSelectBox.selectedIndex].value;
	var tagsString = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	
	var tags = officeDocFilter.removeEmptyElements(tagsString.split(","));
	officeDocFilter.currentPage = 0;
	if(tags.length != 0)
	{
		officeDocFilter.generateStyleContent(tags, officeDocFilter.currentPage);
	}
	else
	{
		officeDocFilter.generateTotalEmptyTable();
	}
	officeDocFilter.setPageValue();
	checkAllTags = false;
	
	if (document.getElementById("MsCheckAll"))
	{
		document.getElementById("MsCheckAll").checked = false;
	}

	officeDocFilter.setDeleteButtonStyle();
}

MSOfficeDocFilter.prototype.generateTotalEmptyTable = function()
{
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append(officeDocFilter.genenrateEmptyTable());
	str.append("</table>");
	document.getElementById("styleContent").innerHTML = str.toString();
}

MSOfficeDocFilter.prototype.checkAll = function()
{
	tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	tags = tags.split(",");
	var checkBoxObj = document.getElementById("MsCheckAll");
	checkAllTags = checkBoxObj.checked;
	for(var i = 0; i < tags.length; i++)
	{
		if(document.getElementById("styles_"+i))
		{
			document.getElementById("styles_"+i).checked = checkAllTags;	
		}
		
		if(checkAllTags)
		{
			this.addThisTag(tags[i]);
		}
		else
		{
			this.removeThisTag(tags[i]);
		}
	}
	officeDocFilter.setDeleteButtonStyle();
}

MSOfficeDocFilter.prototype.generateStyleContent = function (tags, currentPage)
{
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");

	str.append(officeDocFilter.generateTableByPage(tags, currentPage));
	str.append("</table>");
	document.getElementById("styleContent").innerHTML = str.toString();
	this.setPageValue();
}

MSOfficeDocFilter.prototype.init = function()
{
 	officeDocFilter.currentOption = "unextractableWordParagraphStyles";
	officeDocFilter.currentPage = 1;
	officeDocFilter.order = "asc";
}

MSOfficeDocFilter.prototype.genenrateEmptyTable = function()
{
	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	str.append("<td width='5%'><input type='checkbox' id='MsCheckAll'/>");
	str.append("</td>");
	str.append("<td width='95%' class='tagName_td'>" + jsValue);
	str.append("</td>");
	str.append("</tr>");
	return str.toString();
}

MSOfficeDocFilter.prototype.removeEmptyElements = function(array)
{
	var newArray = new Array();
	for(var i = 0; i < array.length; i++)
	{
		if(array[i] && "" != array[i])
		{
			newArray.push(array[i]);
		}
	}
	return newArray;
}

MSOfficeDocFilter.prototype.generateTableByPage = function(tags, currentPageNum)
{
	tags = officeDocFilter.removeEmptyElements(tags);
	var totalpageNum = Math.floor((tags.length + 1) / officeDocFilter.tagsEveryPage) + 1;

	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	var check = checkAllTags ? "checked" : "";
	str.append("<td width='5%'><input type='checkbox' onclick=officeDocFilter.checkAll() id='MsCheckAll' "+check+" />");
	str.append("</td>");
	str.append("<td width='80%' class='tagName_td'><a href='#' class='tagName_td' onmouseenter=mouseEnter('sort_img') onmouseout=mouseOut('sort_img') onclick='officeDocFilter.sortTable()'>" + jsValue + "</a>");
	
	var sortImgSrc = "/globalsight/images/sort-up.gif";
	if(officeDocFilter.order == 'asc')
	{
		sortImgSrc = "/globalsight/images/sort-up.gif";
	}
	else
	{
		sortImgSrc = "/globalsight/images/sort-down.gif";
	}
	str.append("<img class='not_display' id='sort_img' src='" + sortImgSrc + "'></img>");
	str.append("</td>");
	str.append("<td class='tagName_td' align='right'>");
//	str.append(officeDocFilter.currentPage);
//	str.append("/");
//	str.append(totalpageNum);
	str.append("</td>");
	str.append("</tr>");
	var startIndex = 0;
	startIndex = officeDocFilter.tagsEveryPage * currentPageNum;
	while (startIndex >= tags.length && tags.length != 0)
	{
		var maxPage = Math.floor(tags.length/htmlFilter.tagsEveryPage + 1);
		alert(maxPageNums + maxPage);
		return maxPage;
	}
	
	officeDocFilter.currentPage = currentPageNum;
	
	for(var i = 0; i < tags.length; i++)
	{
		if(tags[i] && "" != tags[i])
		{
			var backgroundColor = "#DFE3EE";
			if(i % 2 == 0)
			{
				backgroundColor = "#C7CEE0";
			}
			var display='none';
			if(i >= startIndex && i < startIndex + officeDocFilter.tagsEveryPage)
			{
				display='';
			}

			str.append("<tr style='background-color:"+backgroundColor+";display:"+display+"'>");
			
            check = "";
            
			var checkedTags = officeDocFilter.getCheckedTags();
			if(officeDocFilter.isSelectedStyle(tags[i], checkedTags))
			{
				check = "checked";
			}
			else
			{
				check = "";
			}
			
			str.append("<td><input onclick='officeDocFilter.checkthis(this)' type='checkbox' name= '"+tags[i]+"' id='styles_"+i+"' "+check+" /></td>");
			str.append("<td colspan='2' class='tagValue_td' width=100%>"+tags[i]+"</td><td></td>");
			str.append("</tr>");
		}
	}
	return str.toString();
}

MSOfficeDocFilter.prototype.checkthis = function(currentCheckBox)
{
	var tag = currentCheckBox.name;
	if(currentCheckBox.checked)
	{
		this.addThisTag(tag);
	}
	else
	{
		this.removeThisTag(tag);
	}
	
	officeDocFilter.setDeleteButtonStyle();
}

MSOfficeDocFilter.prototype.addThisTag = function(tagName)
{
	var checkedTags = officeDocFilter.getCheckedTags();
	var tags = ",".concat(checkedTags).concat(",");
	if(tags.indexOf(","+tagName+",") == -1)
	{
		if (checkedTags.length > 0)
		{
			checkedTags = checkedTags.concat(",");
		}
		
		checkedTags = checkedTags.concat(tagName);
		this.setCheckedTags(checkedTags);
	}
}

MSOfficeDocFilter.prototype.removeThisTag = function(tagName)
{
	var checkedTags = officeDocFilter.getCheckedTags();
	var tags = checkedTags.split(",");
	for (var i = 0; i < tags.length; i++)
	{
		if (tags[i] == tagName)
		{
			tags[i] = "";
		}
	}
	
	this.setCheckedTags(this.generateArrayToString(tags));
}

MSOfficeDocFilter.prototype.removeStyle = function(style, styleType)
{
	var allStyle = officeDocFilter.selectTagsMap[styleType];
	var styles = allStyle.split(",");
	for (var i = 0; i < styles.length; i++)
	{
		if (styles[i] == style)
		{
			styles[i] = "";
		}
	}
	
	officeDocFilter.selectTagsMap[styleType] = this.generateArrayToString(styles);
	
	allStyle = officeDocFilter.allSelectTagsOption[styleType];
	styles = allStyle.split(",");
	for (var i = 0; i < styles.length; i++)
	{
		if (styles[i] == style)
		{
			styles[i] = "";
		}
	}
	
	officeDocFilter.allSelectTagsOption[styleType] = this.generateArrayToString(styles);
}

MSOfficeDocFilter.prototype.getCheckedTags = function()
{
	return officeDocFilter.selectTagsMap[officeDocFilter.currentOption];
}

MSOfficeDocFilter.prototype.setCheckedTags = function(tags)
{
	officeDocFilter.selectTagsMap[officeDocFilter.currentOption] = tags;
}

MSOfficeDocFilter.prototype.isSelectedStyle = function(style, allStyle)
{
	var s = ",".concat(allStyle).concat(",");
	var testS = ",".concat(style).concat(",");
	
	return s.indexOf(testS) > -1;
}

MSOfficeDocFilter.prototype.generateArrayToString = function(tagsArray)
{
	var str = "";
	for(var i = 0; i < tagsArray.length; i++)
	{
		var tag = tagsArray[i];
		if(tag && "" != tag)
		{
			if (str.length > 0)
			{
				str = str.concat(",");
			}
			
			str = str.concat(tag);
		}
	}
	return str;
}

MSOfficeDocFilter.prototype.sortTable = function()
{
	var tags = officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption];
	tags = tags.split(",");
	tags = officeDocFilter.removeEmptyElements(tags);
	tags.sort();
	if(officeDocFilter.order == 'asc')
	{
		officeDocFilter.order = 'desc';
	}
	else
	{
		officeDocFilter.order = 'asc';
		tags.reverse();
	}
	officeDocFilter.generateStyleContent(tags, officeDocFilter.currentPage);
	officeDocFilter.allSelectTagsOption[officeDocFilter.currentOption] = officeDocFilter.generateArrayToString(tags);
}

MSOfficeDocFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("msOfficeDocFilterDialog");
}

MSOfficeDocFilter.prototype.generateContentPostFilter = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='docContentPostFilterSelect' class='xml_filter_select'>");
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

function saveMsOfficeDocFilter()
{
	var check = new Validate();
	var filterName = document.getElementById("docFilterName").value;
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
    var isNew = (saveMsOfficeDocFilter.edit) ? "false" : "true";
	var filterId = saveMsOfficeDocFilter.filterId;
	var filterDesc = document.getElementById("docDesc").value;
	var headerTranslate = document.getElementById("docHeaderTranslate").checked;
	var altTranslate = document.getElementById("docAltTranslate").checked;
	var TOCTranslate = document.getElementById("TOCTranslate").checked;
	var contentPostFilterIdAndTableName = document.getElementById("docContentPostFilterSelect").value;
	var contentPostFilterIndex = contentPostFilterIdAndTableName.indexOf("-");
	var contentPostFilterId = -2;
	var contentPostFilterTableName = "";
	if (contentPostFilterIndex > 0)
	{
		contentPostFilterId = contentPostFilterIdAndTableName.substring(0,contentPostFilterIndex);
		contentPostFilterTableName = contentPostFilterIdAndTableName.substring(contentPostFilterIndex+1);
	}
	var baseFilterId = document.getElementById("ms_office_doc_filter_baseFilterSelect").value;
		
	alertUserBaseFilter(baseFilterId);
	
	var obj = {
			isNew:isNew,
			filterTableName:"ms_office_doc_filter", 
			filterId:filterId,
			filterName:filterName, 
			filterDesc:filterDesc, 
			headerTranslate:headerTranslate,
			altTranslate:altTranslate,
			TOCTranslate:TOCTranslate,
			unextractableWordParagraphStyles:officeDocFilter.selectTagsMap[officeDocFilter.paragraphStyles],
			unextractableWordCharacterStyles:officeDocFilter.selectTagsMap[officeDocFilter.characterStyles],
			allParagraphStyles:officeDocFilter.allSelectTagsOption[officeDocFilter.paragraphStyles],
			allCharacterStyles:officeDocFilter.allSelectTagsOption[officeDocFilter.characterStyles],
			selectedInternalTextStyles:officeDocFilter.selectTagsMap[officeDocFilter.internalTextStyles],
			allInternalTextStyles:officeDocFilter.allSelectTagsOption[officeDocFilter.internalTextStyles],
			companyId:companyId,
			contentPostFilterId:contentPostFilterId, 
			contentPostFilterTableName:contentPostFilterTableName,
			baseFilterId:baseFilterId
			};
		sendAjax(obj, "checkExist", "checkExistMSOfficeDocFilterCallback");
	
		checkExistMSOfficeDocFilterCallback.obj = obj;
}

function checkExistMSOfficeDocFilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("msOfficeDocFilterDialog");
		if(saveMsOfficeDocFilter.edit)
		{
			sendAjax(checkExistMSOfficeDocFilterCallback.obj, "updateMSOfficeDocFilter", "updateMSOfficeDocFilterCallback");
		}
		else
		{
			sendAjax(checkExistMSOfficeDocFilterCallback.obj, "saveMSOfficeDocFilter", "saveMSOfficeDocFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("msOfficeDocFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}

function updateMSOfficeDocFilterCallback(data)
{
	var color = saveMsOfficeDocFilter.color;
	var filterId = saveMsOfficeDocFilter.filterId;
	var filter = saveMsOfficeDocFilter.filter;
	var topFilterId = saveMsOfficeDocFilter.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = "ms_office_doc_filter";
		jpFilter.filterName = checkExistMSOfficeDocFilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistMSOfficeDocFilterCallback.obj.filterDesc;
		jpFilter.headerTranslate = checkExistMSOfficeDocFilterCallback.obj.headerTranslate;
		jpFilter.altTranslate = checkExistMSOfficeDocFilterCallback.obj.altTranslate;
		jpFilter.TOCTranslate = checkExistMSOfficeDocFilterCallback.obj.TOCTranslate;
		jpFilter.unextractableWordParagraphStyles = checkExistMSOfficeDocFilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistMSOfficeDocFilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.allParagraphStyles = checkExistMSOfficeDocFilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistMSOfficeDocFilterCallback.obj.allCharacterStyles;
		jpFilter.selectedInternalTextStyles = checkExistMSOfficeDocFilterCallback.obj.selectedInternalTextStyles;
		jpFilter.allInternalTextStyles = checkExistMSOfficeDocFilterCallback.obj.allInternalTextStyles;
		jpFilter.companyId = companyId;
		jpFilter.contentPostFilterId = checkExistMSOfficeDocFilterCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistMSOfficeDocFilterCallback.obj.contentPostFilterTableName;
		jpFilter.baseFilterId = checkExistMSOfficeDocFilterCallback.obj.baseFilterId;
		
		var specialFilters = updateSpecialFilter(saveMsOfficeDocFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveMSOfficeDocFilterCallback(data)
{
	var color = saveMsOfficeDocFilter.color;
	var topFilterId = saveMsOfficeDocFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = "ms_office_doc_filter";
		jpFilter.filterName = checkExistMSOfficeDocFilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistMSOfficeDocFilterCallback.obj.filterDesc;
		jpFilter.headerTranslate = checkExistMSOfficeDocFilterCallback.obj.headerTranslate;
		jpFilter.unextractableWordParagraphStyles = checkExistMSOfficeDocFilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistMSOfficeDocFilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.allParagraphStyles = checkExistMSOfficeDocFilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistMSOfficeDocFilterCallback.obj.allCharacterStyles;
		jpFilter.selectedInternalTextStyles = checkExistMSOfficeDocFilterCallback.obj.selectedInternalTextStyles;
		jpFilter.allInternalTextStyles = checkExistMSOfficeDocFilterCallback.obj.allInternalTextStyles;
		jpFilter.companyId = companyId;
		jpFilter.contentPostFilterId = checkExistMSOfficeDocFilterCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistMSOfficeDocFilterCallback.obj.contentPostFilterTableName;
		jpFilter.altTranslate = checkExistMSOfficeDocFilterCallback.obj.altTranslate;
		jpFilter.TOCTranslate = checkExistMSOfficeDocFilterCallback.obj.TOCTranslate;
		jpFilter.baseFilterId = checkExistMSOfficeDocFilterCallback.obj.baseFilterId;
		
		filter.specialFilters.push(jpFilter);
	    reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}
