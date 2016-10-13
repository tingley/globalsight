var openofficeDocFilter = new OpenOfficeFilter();

function OpenOfficeFilter()
{
	this.filterTableName = "openoffice_filter";
	
	this.paragraphStyles = "unextractableWordParagraphStyles";
	this.characterStyles = "unextractableWordCharacterStyles";
	
	this.defaultUnextractableWordParagraphStyles = "DONOTTRANSLATE_para,tw4winExternal";
	this.defaultUnextractableWordCharacterStyles = "DONOTTRANSLATE_char,tw4winInternal";
	
	this.selectTagsMap = new Object();
	this.selectTagsMap[this.paragraphStyles] = this.defaultUnextractableWordParagraphStyles;
	this.selectTagsMap[this.characterStyles] = this.defaultUnextractableWordCharacterStyles;
	
    this.currentOption = "unextractableWordParagraphStyles";
    this.currentPage = 1;
	this.tagsEveryPage = 10;
	this.order = "asc";
    
	this.allSelectTagsOption = new Array();	
	this.allSelectTagsOption[this.paragraphStyles] = this.defaultUnextractableWordParagraphStyles;
    this.allSelectTagsOption[this.characterStyles] = this.defaultUnextractableWordCharacterStyles;
}

OpenOfficeFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
	
	this.optionMap = new Object();
	this.optionMap[this.paragraphStyles] = jsUnextractableWordParagraphStyles;
    this.optionMap[this.characterStyles] = jsUnextractableWordCharacterStyles;
}

OpenOfficeFilter.prototype.showStyleSelectBox = function(isDisabled) {
	document.getElementById("OOUnextractableRule").disabled = isDisabled;
}

OpenOfficeFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	openofficeDocFilter.selectTagsMap[this.paragraphStyles] = this.filter.unextractableWordParagraphStyles;
	openofficeDocFilter.selectTagsMap[this.characterStyles] = this.filter.unextractableWordCharacterStyles;
	openofficeDocFilter.allSelectTagsOption[this.paragraphStyles] = this.filter.allParagraphStyles;
	openofficeDocFilter.allSelectTagsOption[this.characterStyles] = this.filter.allCharacterStyles;
	
	openofficeDocFilter.init();
	
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' id='ooFilterName' value='" + this.filter.filterName + "' >");
	str.append("<br/>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	str.append("<textarea rows='3' style='width:340px' id='ooFilterDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br/>");
	var isCheckHeaderTranslate = (this.filter.headerTranslate) ? "checked":"";
	
	str.append("<table border=0 width='408px'>");
	
	str.append("<tr style='display:none;'>");
	str.append("<td class='htmlFilter_left_td'>" + jsOOXmlFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.getXmlFilterSelect(this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsOOTransHeader + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='headerTranslate' type='checkbox' name='headerTranslate' value='"+this.filter.headerTranslate+"' "+isCheckHeaderTranslate+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	str.append("<br/>");
	
	str.append("<div style='width:408px'>");
	
	str.append("<div id='OpenOfficeStyleContent' style='float:left'>")
	str.append("<table border=0 width=100%><tr valign=bottom><td>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='OOUnextractableRule' onchange='openofficeDocFilter.switchTags(this)' class='specialFilter_dialog_label'>");
	str.append("<option value='unextractableWordParagraphStyles'>" + jsOOUnextractableWordParagraphStyles + "</option>");
	str.append("<option value='unextractableWordCharacterStyles'>" + jsOOUnextractableWordCharacterStyles + "</option>");
	str.append("</select>");

	str.append("</td><td nowrap align=right>");
	

	str.append("<input type='button' value='" + jsAdd + "' onclick='openofficeDocFilter.onAdd()'>");
	str.append("<input type='button' value='" + jsDelete + "' id='OODeleteButton' onclick='openofficeDocFilter.deleteTag()'>");
	str.append("</td></tr></table>");
	
	//str.append("<div><br>");
	var tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption].split(",");
	str.append("<div id='ooStyleContent'>")
	str.append("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
	
	var tableContent = openofficeDocFilter.generateTableByPage(tags, 0);

	if(tableContent){
		str.append(tableContent);
	}
	else
	{
		str.append(openofficeDocFilter.genenrateEmptyTable());
	}
	str.append("</table>");
	str.append("</div>");
	
	var PagePropTotalSize = openofficeDocFilter.getPageSize();
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=openofficeDocFilter.prePage('"+tags+"')>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=openofficeDocFilter.nextPage('"+tags+"')>" + jsNext + "</a>|");
	str.append("<input id='pageCountOO' size=3 type='text' value="+(openofficeDocFilter.currentPage+1)+" >");
	str.append(" / <span id='PageTotalSizeOO' class='standardText'>" + PagePropTotalSize + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=openofficeDocFilter.goToPage('"+tags+"')>");
	
	var dialogObj = document.getElementById('openofficeFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveOpenOfficeDocFilter.edit = true;
	saveOpenOfficeDocFilter.filterId = filterId;
	saveOpenOfficeDocFilter.color = color;
	saveOpenOfficeDocFilter.filter = this.filter;
	saveOpenOfficeDocFilter.specialFilters = specialFilters;
	saveOpenOfficeDocFilter.topFilterId = topFilterId;
	
	this.setDeleteButtonStyle();
}

OpenOfficeFilter.prototype.setPageValue = function()
{
	document.getElementById("pageCountOO").value = this.currentPage + 1;
	document.getElementById("PageTotalSizeOO").innerHTML = this.getPageSize();
}

OpenOfficeFilter.prototype.generateDiv = function (topFilterId, color)
{
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' id='ooFilterName' value='Open Office Filter'>");
	str.append("<br/>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	str.append("<textarea rows='3' style='width:340px' id='ooFilterDesc' name='desc' ></textarea>");
	str.append("<br/>");
	
	str.append("<table border=0 width='408px'>");
	
	str.append("<tr style='display:none;'>");
	str.append("<td class='htmlFilter_left_td'>" + jsOOXmlFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.getXmlFilterSelect());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsOOTransHeader + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='headerTranslate' type='checkbox' name='headerTranslate' value='true' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	str.append("<br/>");
	
	str.append("<div style='width:408px'>");
	
	str.append("<div id='OpenOfficeStyleContent' style='float:left'>")
	str.append("<table border=0 width=100%><tr valign=bottom><td>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='OOUnextractableRule' onchange='openofficeDocFilter.switchTags(this)' class='specialFilter_dialog_label'>");
	str.append("<option value='unextractableWordParagraphStyles'>" + jsOOUnextractableWordParagraphStyles + "</option>");
	str.append("<option value='unextractableWordCharacterStyles'>" + jsOOUnextractableWordCharacterStyles + "</option>");
	str.append("</select>");

	str.append("</td><td nowrap align=right>");

	str.append("<input type='button' value='" + jsAdd + "' onclick='openofficeDocFilter.onAdd()'>");
		
	str.append("<input type='button' value='" + jsDelete + "' id='OODeleteButton' onclick='openofficeDocFilter.deleteTag()' disabled>");
	str.append("</td></tr></table>");

	openofficeDocFilter.selectTagsMap[openofficeDocFilter.paragraphStyles] = "";
	openofficeDocFilter.selectTagsMap[openofficeDocFilter.characterStyles] = "";
	openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.paragraphStyles] = openofficeDocFilter.defaultUnextractableWordParagraphStyles;
	openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.characterStyles] = openofficeDocFilter.defaultUnextractableWordCharacterStyles;
	
	openofficeDocFilter.init();
	var tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption].split(",");
	str.append("<div id='ooStyleContent'>")
	str.append("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");

	var tableContent = openofficeDocFilter.generateTableByPage(tags, 0);
	if(tableContent){
		str.append(tableContent);
	}
	else
	{
		str.append(openofficeDocFilter.genenrateEmptyTable());
	}
	str.append("</table>");
	str.append("</div>");
	
	var PagePropTotalSize = openofficeDocFilter.getPageSize();
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=openofficeDocFilter.prePage('"+tags+"')>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=openofficeDocFilter.nextPage('"+tags+"')>" + jsNext + "</a>|");
	str.append("<input id='pageCountOO' size=3 type='text' value="+(openofficeDocFilter.currentPage+1)+" >");
	str.append(" / <span id='PageTotalSizeOO' class='standardText'>" + PagePropTotalSize + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=openofficeDocFilter.goToPage('"+tags+"')>");
	
	var dialogObj = document.getElementById('openofficeFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	saveOpenOfficeDocFilter.edit = false;
	saveOpenOfficeDocFilter.color = color;
	saveOpenOfficeDocFilter.topFilterId = topFilterId;
	
	this.setDeleteButtonStyle();
}

OpenOfficeFilter.prototype.prePage = function(tags)
{
	tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
    tags = openofficeDocFilter.removeEmptyElements(tags.split(","));
    if(tags.length == 0)
    {
       return;
    }
    if(openofficeDocFilter.currentPage > 0)
	{
    	openofficeDocFilter.currentPage --;
    	openofficeDocFilter.generateStyleContent(tags, openofficeDocFilter.currentPage);
    	openofficeDocFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

OpenOfficeFilter.prototype.getPageSize = function()
{
	var tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	tags = openofficeDocFilter.removeEmptyElements(tags.split(","));
	var itemsTotalCount = tags.length;
	var countPerPage = this.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

OpenOfficeFilter.prototype.nextPage = function(tags)
{
	tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	tags = openofficeDocFilter.removeEmptyElements(tags.split(","));
	if(tags.length == 0)
	{
	   return;
	}
	if(openofficeDocFilter.currentPage < Math.floor(tags.length/openofficeDocFilter.tagsEveryPage + 1))
	{
		openofficeDocFilter.currentPage ++;
		var tableContent = openofficeDocFilter.generateTableByPage(tags,openofficeDocFilter.currentPage);
		var validate = new Validate();
		if(!validate.isNumber(tableContent))
		{
			var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
			str.append(tableContent);
			str.append("</table>");
			document.getElementById("ooStyleContent").innerHTML = str.toString();
			openofficeDocFilter.setPageValue();
		}
		else
		{
			openofficeDocFilter.currentPage --;
			openofficeDocFilter.setPageValue();
		}		

	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

OpenOfficeFilter.prototype.goToPage = function(tags)
{
	tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	tags = openofficeDocFilter.removeEmptyElements(tags.split(","));
	var pageValue = document.getElementById("pageCountOO").value;
	var validate = new Validate();
	if(! validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	if(pageValue < 1 || pageValue > Math.floor(tags.length/openofficeDocFilter.tagsEveryPage + 1))
	{
		alert(invalidatePageValue.replace("%s", Math.floor(tags.length/openofficeDocFilter.tagsEveryPage + 1)));
		return;
	}

	openofficeDocFilter.currentPage = pageValue - 1;
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append(openofficeDocFilter.generateTableByPage(tags, openofficeDocFilter.currentPage));
	str.append("</table>");
	document.getElementById("ooStyleContent").innerHTML = str.toString();

}

OpenOfficeFilter.prototype.addStyle = function()
{
	var validate = new Validate();
	var styleName = new StringBuffer(document.getElementById("oostyleToAdd").value);

	if(validate.isEmptyStr(styleName.trim()))
	{
		alert(emptyStyle);
		return;
	}
	
	styleName = styleName.trim();
	
	if(validate.containsWhitespace(styleName))
	{
		alert(jsStyle + "(" + styleName + ")"+ canNotContainWhiteSpace);
		return;
	}

	if(validate.containSpecialChars(styleName, "&<>'\","))
	{
	    alert(invalidStyleChar + "& < > ' \" ,");
	    return;
	}
	
	var tagString = new StringBuffer("," + openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption] + ",");
	if(tagString.indexOf("," + styleName.toString() + ",") != -1 )
	{
		alert(existStyle + styleName.toString());
		return;
	}
	
	var allStyles = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	if (allStyles.length > 0 && allStyles.charAt(allStyles.length - 1) != ",")
	{
		allStyles += ","
	}
	allStyles += styleName.toString();
	openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption] = allStyles;
	
	openofficeDocFilter.generateStyleContent(allStyles.split(","), openofficeDocFilter.currentPage);	
	
	openofficeDocFilter.showStyleSelectBox(false);
	closePopupDialog('addOOStyleDialog');
}

OpenOfficeFilter.prototype.setDeleteButtonStyle = function()
{
	var isDisabled = openofficeDocFilter.selectTagsMap[openofficeDocFilter.paragraphStyles].length == 0
			&& openofficeDocFilter.selectTagsMap[openofficeDocFilter.characterStyles].length == 0;

	document.getElementById("OODeleteButton").disabled = isDisabled;
}

OpenOfficeFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = openofficeDocFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteOOStyleDialog");
	}
}

//for gbs-2599
OpenOfficeFilter.prototype.selectAll_OpenOfficeFilter = function()
{
	var selectAll = document.getElementById("selectAll_OpenOfficeFilter")
	if(selectAll.checked) {
		this.checkAllTagsToDelete();
	} else {
		this.clearAllTagsToDelete();
	}
}

OpenOfficeFilter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsStyleType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_OpenOfficeFilter' onclick='openofficeDocFilter.selectAll_OpenOfficeFilter()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsStylesToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsStylesCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");
	var tagTypes = document.getElementById("OOUnextractableRule");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var checkedTags = openofficeDocFilter.selectTagsMap[value];
		var checkedTagsArray = checkedTags.split(",");
		
		if(openofficeDocFilter.isNotEmptyArray(checkedTagsArray))
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
				if(checkedTag && checkedTag != "" && openofficeDocFilter.isCheckedTagInDefaultArray(value, checkedTag))
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
	/* for gbs-2599
	str.append("<a href='#' class='specialfilter_a' onclick='openofficeDocFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='openofficeDocFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteOOStylesDialogLable").innerHTML = jsRemoveStylesNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteOOStyleTableContent").innerHTML = str.toString();
	return true;
}

OpenOfficeFilter.prototype.isCheckOrClearAll = function( checkOrClear )
{
    var checkBoxObjs = document.getElementsByName(openofficeDocFilter.paragraphStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
    
    checkBoxObjs = document.getElementsByName(openofficeDocFilter.characterStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
}

OpenOfficeFilter.prototype.checkAllTagsToDelete = function ()
{
	openofficeDocFilter.isCheckOrClearAll(true);
}

OpenOfficeFilter.prototype.clearAllTagsToDelete = function()
{
	openofficeDocFilter.isCheckOrClearAll(false);
}

OpenOfficeFilter.prototype.isNotEmptyArray = function (array)
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

OpenOfficeFilter.prototype.isCheckedTagInDefaultArray = function(value, checkedTag)
{
    var tagsStringByOption = openofficeDocFilter.getTagsStringByOption(value);
    return (","+tagsStringByOption+",").indexOf(","+checkedTag+",") != -1;
}

OpenOfficeFilter.prototype.getTagsStringByOption = function(currentOption)
{
    return openofficeDocFilter.selectTagsMap[currentOption];
}

OpenOfficeFilter.prototype.deleteStyles = function()
{
	var paragraphStylesToDelete = document.getElementsByName(openofficeDocFilter.paragraphStyles);
	var characterStylesToDelete = document.getElementsByName(openofficeDocFilter.characterStyles);
		
	openofficeDocFilter.buildTagsString(paragraphStylesToDelete);
	openofficeDocFilter.buildTagsString(characterStylesToDelete);
	
	closePopupDialog('deleteOOStyleDialog');
}

OpenOfficeFilter.prototype.buildTagsString = function(array)
{
	for(var i = 0; i < array.length; i++)
	{
		var a = array[i];
		if(a && a.checked)
		{
			var tagType = a.getAttribute("tagType");
			var tagValue = a.getAttribute("tagValue");
			
			openofficeDocFilter.removeStyle(tagValue,tagType);
			
			var tagsString = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
			var tags = tagsString.split(",");
			
			var tagLength = tags.length;
			tagLength = (tagLength == 0) ? 1 : tagLength;
			var currentMaxPageNum = Math.floor((tagLength - 1) / openofficeDocFilter.tagsEveryPage);
			if(openofficeDocFilter.currentPage > currentMaxPageNum)
			{
				openofficeDocFilter.currentPage = currentMaxPageNum;
			}
			
			openofficeDocFilter.setPageValue();
			
			if(tags.length == 0)
			{
				openofficeDocFilter.generateTotalEmptyTable();
			}
			else
			{
				openofficeDocFilter.generateStyleContent(tags, openofficeDocFilter.currentPage);
			}
			
		}
	}
}

OpenOfficeFilter.prototype.onAdd = function()
{
    showPopupDialog("addOOStyleDialog");
    document.getElementById("oostyleToAdd").value = "";
    openofficeDocFilter.showStyleSelectBox(true);
}

OpenOfficeFilter.prototype.onDelete = function()
{
	tags = openofficeDocFilter.selectTagsMap[openofficeDocFilter.currentOption];
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
			openofficeDocFilter.selectTagsMap[openofficeDocFilter.currentOption]= styles;
			openofficeDocFilter.generateStyleContent(styles.split(","), openofficeDocFilter.currentPage);
		}
	}
	else
	{
		alert(noStylesToDelete);
	}
}

OpenOfficeFilter.prototype.switchTags = function(tagsSelectBox)
{
	openofficeDocFilter.currentOption = tagsSelectBox.options[tagsSelectBox.selectedIndex].value;
	var tagsString = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	
	var tags = openofficeDocFilter.removeEmptyElements(tagsString.split(","));
	openofficeDocFilter.currentPage = 0;
	if(tags.length != 0)
	{
		openofficeDocFilter.generateStyleContent(tags, openofficeDocFilter.currentPage);
	}
	else
	{
		openofficeDocFilter.generateTotalEmptyTable();
	}
	openofficeDocFilter.setPageValue();
	checkAllTags = false;
	
	if (document.getElementById("ooCheckAll"))
	{
		document.getElementById("ooCheckAll").checked = false;
	}

	openofficeDocFilter.setDeleteButtonStyle();
}

OpenOfficeFilter.prototype.generateTotalEmptyTable = function()
{
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append(openofficeDocFilter.genenrateEmptyTable());
	str.append("</table>");
	document.getElementById("ooStyleContent").innerHTML = str.toString();
}

OpenOfficeFilter.prototype.checkAll = function(tags)
{
	tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	tags = tags.split(",");
	var checkBoxObj = document.getElementById("ooCheckAll");
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
	openofficeDocFilter.setDeleteButtonStyle();
}

OpenOfficeFilter.prototype.generateStyleContent = function (tags, currentPage)
{
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");

	str.append(openofficeDocFilter.generateTableByPage(tags, currentPage));
	str.append("</table>");
	document.getElementById("ooStyleContent").innerHTML = str.toString();
	this.setPageValue();
}

OpenOfficeFilter.prototype.init = function()
{
 	openofficeDocFilter.currentOption = "unextractableWordParagraphStyles";
	openofficeDocFilter.currentPage = 1;
	openofficeDocFilter.order = "asc";
}

OpenOfficeFilter.prototype.genenrateEmptyTable = function()
{
	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	str.append("<td width='5%'><input type='checkbox' id='ooCheckAll'/>");
	str.append("</td>");
	str.append("<td width='95%' class='tagName_td'>" + jsValue);
	str.append("</td>");
	str.append("</tr>");
	return str.toString();
}

OpenOfficeFilter.prototype.removeEmptyElements = function(array)
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

OpenOfficeFilter.prototype.generateTableByPage = function(tags, currentPageNum)
{
	tags = openofficeDocFilter.removeEmptyElements(tags);
	var totalpageNum = Math.floor((tags.length + 1) / openofficeDocFilter.tagsEveryPage) + 1;

	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	var check = checkAllTags ? "checked" : "";
	str.append("<td width='5%'><input type='checkbox' onclick=openofficeDocFilter.checkAll('"+tags+"') id='ooCheckAll' "+check+">");
	str.append("</td>");
	str.append("<td width='80%' class='tagName_td'><a href='#' class='tagName_td' onmouseenter=mouseEnter('sort_img') onmouseout=mouseOut('sort_img') onclick='openofficeDocFilter.sortTable()'>" + jsValue + "</a>");
	
	var sortImgSrc = "/globalsight/images/sort-up.gif";
	if(openofficeDocFilter.order == 'asc')
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
//	str.append(openofficeDocFilter.currentPage);
//	str.append("/");
//	str.append(totalpageNum);
	str.append("</td>");
	str.append("</tr>");
	var startIndex = 0;
	startIndex = openofficeDocFilter.tagsEveryPage * currentPageNum;
	while (startIndex >= tags.length && tags.length != 0)
	{
		var maxPage = Math.floor(tags.length/htmlFilter.tagsEveryPage + 1);
		alert(maxPageNums + maxPage);
		return maxPage;
	}
	
	openofficeDocFilter.currentPage = currentPageNum;
	
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
			if(i >= startIndex && i < startIndex + openofficeDocFilter.tagsEveryPage)
			{
				display='';
			}

			str.append("<tr style='background-color:"+backgroundColor+";display:"+display+"'>");
			
            check = "";
            
			var checkedTags = openofficeDocFilter.getCheckedTags();
			if(openofficeDocFilter.isSelectedStyle(tags[i], checkedTags))
			{
				check = "checked";
			}
			else
			{
				check = "";
			}
			
			str.append("<td><input onclick='openofficeDocFilter.checkthis(this)' type='checkbox' name= '"+tags[i]+"' id='styles_"+i+"' "+check+"></td>");
			str.append("<td colspan='2' class='tagValue_td' width=100%>"+tags[i]+"</td><td></td>");
			str.append("</tr>");
		}
	}
	return str.toString();
		
}

OpenOfficeFilter.prototype.checkthis = function(currentCheckBox)
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
	
	openofficeDocFilter.setDeleteButtonStyle();
}

OpenOfficeFilter.prototype.addThisTag = function(tagName)
{
	var checkedTags = openofficeDocFilter.getCheckedTags();
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

OpenOfficeFilter.prototype.removeThisTag = function(tagName)
{
	var checkedTags = openofficeDocFilter.getCheckedTags();
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

OpenOfficeFilter.prototype.removeStyle = function(style, styleType)
{
	var allStyle = openofficeDocFilter.selectTagsMap[styleType];
	var styles = allStyle.split(",");
	for (var i = 0; i < styles.length; i++)
	{
		if (styles[i] == style)
		{
			styles[i] = "";
		}
	}
	
	openofficeDocFilter.selectTagsMap[styleType] = this.generateArrayToString(styles);
	
	allStyle = openofficeDocFilter.allSelectTagsOption[styleType];
	styles = allStyle.split(",");
	for (var i = 0; i < styles.length; i++)
	{
		if (styles[i] == style)
		{
			styles[i] = "";
		}
	}
	
	openofficeDocFilter.allSelectTagsOption[styleType] = this.generateArrayToString(styles);
}

OpenOfficeFilter.prototype.getCheckedTags = function()
{
	return openofficeDocFilter.selectTagsMap[openofficeDocFilter.currentOption];
}

OpenOfficeFilter.prototype.setCheckedTags = function(tags)
{
	openofficeDocFilter.selectTagsMap[openofficeDocFilter.currentOption] = tags;
}

OpenOfficeFilter.prototype.isSelectedStyle = function(style, allStyle)
{
	var s = ",".concat(allStyle).concat(",");
	var testS = ",".concat(style).concat(",");
	
	return s.indexOf(testS) > -1;
}

OpenOfficeFilter.prototype.generateArrayToString = function(tagsArray)
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

OpenOfficeFilter.prototype.sortTable = function()
{
	var tags = openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption];
	tags = tags.split(",");
	tags = openofficeDocFilter.removeEmptyElements(tags);
	tags.sort();
	if(openofficeDocFilter.order == 'asc')
	{
		openofficeDocFilter.order = 'desc';
	}
	else
	{
		openofficeDocFilter.order = 'asc';
		tags.reverse();
	}
	openofficeDocFilter.generateStyleContent(tags, openofficeDocFilter.currentPage);
	openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.currentOption] = openofficeDocFilter.generateArrayToString(tags);
}

OpenOfficeFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("openofficeFilterDialog");
}

OpenOfficeFilter.prototype.getXmlFilterSelect = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='xmlFilterSelect' class='specialFilter_dialog_label' style='margin-left:11px'>");
	str.append("<option value='-1'>" + jsChoose + "</option>");

	if(_filterConfigurations)
	{
		for(var i = 0; i < _filterConfigurations.length; i++)
		{
			var _filter = _filterConfigurations[i];
	        if (_filter.filterTableName == "xml_rule_filter")
	        {
	        	var _xmlSpecialFilters = _filter.specialFilters;
	        	if (_xmlSpecialFilters)
	        	{
		        	for (var j = 0; j < _xmlSpecialFilters.length; j++)
		        	{
		        		var _xmlSpecialFilter = _xmlSpecialFilters[j];
		        		var _filterTableName = _xmlSpecialFilter.filterTableName;
		        		var _filterName = _xmlSpecialFilter.filterName;
		        		try {
		        			if (_filterName.length > 20) {
		        				_filterName = _filterName.substring(0,20) + "...";
		        			}
		        		} catch (err) {
		        			
		        		}
		        		var _id = _xmlSpecialFilter.id;
		        		
		        		var xmlFiterId = filter ? filter.xmlFilterId : -2;
		        		var xmlFilterTableName = "xml_rule_filter";
		        		var id = filter ? filter.id : -2;
		        		
		        		var selected = ""; 
		        		if (_id == xmlFiterId && _filterTableName == xmlFilterTableName)
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

function saveOpenOfficeDocFilter()
{
	var check = new Validate();
	var isNew = (saveOpenOfficeDocFilter.edit) ? "false" : "true";
	var filterName = document.getElementById("ooFilterName").value;
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
    
	var filterId = saveOpenOfficeDocFilter.filterId;
	var filterDesc = document.getElementById("ooFilterDesc").value;
	var headerTranslate = document.getElementById("headerTranslate").checked;
	var xmlFilterIdAndTableName = document.getElementById("xmlFilterSelect").value;
	var splitedXmlIdTable = splitByFirstIndex(xmlFilterIdAndTableName, "-");
	var xmlFilterId = (splitedXmlIdTable) ? splitedXmlIdTable[0] : -2;
	
	var obj = {
			filterTableName:"openoffice_filter",
			isNew:isNew,
			filterId:filterId,
			filterName:filterName,
			filterDesc:filterDesc,
			headerTranslate:headerTranslate,
			xmlFilterId:-2,
			unextractableWordParagraphStyles:openofficeDocFilter.selectTagsMap[openofficeDocFilter.paragraphStyles],
			unextractableWordCharacterStyles:openofficeDocFilter.selectTagsMap[openofficeDocFilter.characterStyles],
			allParagraphStyles:openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.paragraphStyles],
			allCharacterStyles:openofficeDocFilter.allSelectTagsOption[openofficeDocFilter.characterStyles],
			companyId:companyId
			};

		sendAjax(obj, "checkExist", "checkExistOpenOfficeFilterCallback");
	
		checkExistOpenOfficeFilterCallback.obj = obj;
}

function checkExistOpenOfficeFilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("openofficeFilterDialog");
		if(saveOpenOfficeDocFilter.edit)
		{
			sendAjax(checkExistOpenOfficeFilterCallback.obj, "updateOpenOfficeFilter", "updateOpenOfficeFilterCallback");
		}
		else
		{
			sendAjax(checkExistOpenOfficeFilterCallback.obj, "saveOpenOfficeFilter", "saveOpenOfficeFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("openofficeFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}

function updateOpenOfficeFilterCallback(data)
{
	var color = saveOpenOfficeDocFilter.color;
	var filterId = saveOpenOfficeDocFilter.filterId;
	var filter = saveOpenOfficeDocFilter.filter;
	var topFilterId = saveOpenOfficeDocFilter.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = "openoffice_filter";
		jpFilter.filterName = checkExistOpenOfficeFilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistOpenOfficeFilterCallback.obj.filterDesc;
		jpFilter.headerTranslate = checkExistOpenOfficeFilterCallback.obj.headerTranslate;
		jpFilter.xmlFilterId = checkExistOpenOfficeFilterCallback.obj.xmlFilterId;
		jpFilter.unextractableWordParagraphStyles = checkExistOpenOfficeFilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistOpenOfficeFilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.allParagraphStyles = checkExistOpenOfficeFilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistOpenOfficeFilterCallback.obj.allCharacterStyles;
		jpFilter.companyId = companyId;

		var specialFilters = updateSpecialFilter(saveOpenOfficeDocFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveOpenOfficeFilterCallback(data)
{
	var color = saveOpenOfficeDocFilter.color;
	var topFilterId = saveOpenOfficeDocFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = "openoffice_filter";
		jpFilter.filterName = checkExistOpenOfficeFilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistOpenOfficeFilterCallback.obj.filterDesc;
		jpFilter.headerTranslate = checkExistOpenOfficeFilterCallback.obj.headerTranslate;
		jpFilter.xmlFilterId = checkExistOpenOfficeFilterCallback.obj.xmlFilterId;
		jpFilter.unextractableWordParagraphStyles = checkExistOpenOfficeFilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistOpenOfficeFilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.allParagraphStyles = checkExistOpenOfficeFilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistOpenOfficeFilterCallback.obj.allCharacterStyles;
		jpFilter.companyId = companyId;
		filter.specialFilters.push(jpFilter);
	    reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}
