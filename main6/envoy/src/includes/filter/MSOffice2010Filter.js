var msoffice2010DocFilter = new MSOffice2010Filter();

function MSOffice2010Filter()
{
	this.filterTableName = "office2010_filter";
	
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

MSOffice2010Filter.prototype.setFilter = function (filter)
{
	this.filter = filter;
	
	this.optionMap = new Object();
	this.optionMap[this.paragraphStyles] = jsUnextractableWordParagraphStyles;
    this.optionMap[this.characterStyles] = jsUnextractableWordCharacterStyles;
}

MSOffice2010Filter.prototype.showStyleSelectBox = function(isDisabled) {
	document.getElementById("O2010UnextractableRule").disabled = isDisabled;
}

MSOffice2010Filter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	msoffice2010DocFilter.selectTagsMap[this.paragraphStyles] = this.filter.unextractableWordParagraphStyles;
	msoffice2010DocFilter.selectTagsMap[this.characterStyles] = this.filter.unextractableWordCharacterStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.paragraphStyles] = this.filter.allParagraphStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.characterStyles] = this.filter.allCharacterStyles;
	
	msoffice2010DocFilter.init();
	
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' id='o2010FilterName' value='" + this.filter.filterName + "' disabled>");
	str.append("<br/>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	str.append("<textarea rows='3' style='width:340px' id='o2010FilterDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br/>");
	var isCheckHeaderTranslate = (this.filter.headerTranslate) ? "checked":"";
	var isCheckMasterTranslate = (this.filter.masterTranslate) ? "checked":"";
	
	str.append("<table border=0 width='408px'>");
	
	str.append("<tr style='display:none;'>");
	str.append("<td class='htmlFilter_left_td'>" + jsO2010XmlFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.getXmlFilterSelect(this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransHeader + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='headerTranslate' type='checkbox' name='headerTranslate' value='"+this.filter.headerTranslate+"' "+isCheckHeaderTranslate+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='masterTranslate' type='checkbox' name='masterTranslate' value='"+this.filter.masterTranslate+"' "+isCheckMasterTranslate+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	str.append("<br/>");
	
	str.append("<div style='width:408px'>");
	
	str.append("<div id='Office2010StyleContent' style='float:left'>")
	str.append("<table border=0 width=100%><tr valign=bottom><td>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='O2010UnextractableRule' onchange='msoffice2010DocFilter.switchTags(this)' class='specialFilter_dialog_label'>");
	str.append("<option value='unextractableWordParagraphStyles'>" + jsO2010UnextractableWordParagraphStyles + "</option>");
	str.append("<option value='unextractableWordCharacterStyles'>" + jsO2010UnextractableWordCharacterStyles + "</option>");
	str.append("</select>");

	str.append("</td><td nowrap align=right>");
	

	str.append("<input type='button' value='" + jsAdd + "' onclick='msoffice2010DocFilter.onAdd()'>");
	str.append("<input type='button' value='" + jsDelete + "' id='O2010DeleteButton' onclick='msoffice2010DocFilter.deleteTag()'>");
	str.append("</td></tr></table>");
	
	//str.append("<div><br>");
	var tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption].split(",");
	str.append("<div id='o2010StyleContent'>")
	str.append("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
	
	var tableContent = msoffice2010DocFilter.generateTableByPage(tags, 0);

	if(tableContent){
		str.append(tableContent);
	}
	else
	{
		str.append(msoffice2010DocFilter.genenrateEmptyTable());
	}
	str.append("</table>");
	str.append("</div>");
	
	var PagePropTotalSize = msoffice2010DocFilter.getPageSize();
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=msoffice2010DocFilter.prePage('"+tags+"')>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=msoffice2010DocFilter.nextPage('"+tags+"')>" + jsNext + "</a>|");
	str.append("<input id='pageCountO2010' size=3 type='text' value="+(msoffice2010DocFilter.currentPage+1)+" >");
	str.append(" / <span id='PageTotalSizeO2010' class='standardText'>" + PagePropTotalSize + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=msoffice2010DocFilter.goToPage('"+tags+"')>");
	
	var dialogObj = document.getElementById('msoffice2010FilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveMSOffice2010DocFilter.edit = true;
	saveMSOffice2010DocFilter.filterId = filterId;
	saveMSOffice2010DocFilter.color = color;
	saveMSOffice2010DocFilter.filter = this.filter;
	saveMSOffice2010DocFilter.specialFilters = specialFilters;
	saveMSOffice2010DocFilter.topFilterId = topFilterId;
	
	this.setDeleteButtonStyle();
}

MSOffice2010Filter.prototype.setPageValue = function()
{
	document.getElementById("pageCountO2010").value = this.currentPage + 1;
	document.getElementById("PageTotalSizeO2010").innerHTML = this.getPageSize();
}

MSOffice2010Filter.prototype.generateDiv = function (topFilterId, color)
{
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' id='o2010FilterName' value='MSOffice2010Filter'>");
	str.append("<br/>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	str.append("<textarea rows='3' style='width:340px' id='o2010FilterDesc' name='desc' ></textarea>");
	str.append("<br/>");
	
	str.append("<table border=0 width='408px'>");
	
	str.append("<tr style='display:none;'>");
	str.append("<td class='htmlFilter_left_td'>" + jsO2010XmlFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.getXmlFilterSelect());
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransHeader + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='headerTranslate' type='checkbox' name='headerTranslate' value='true' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='masterTranslate' type='checkbox' name='masterTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("</table>");
	str.append("<br/>");
	
	str.append("<div style='width:408px'>");
	
	str.append("<div id='Office2010StyleContent' style='float:left'>")
	str.append("<table border=0 width=100%><tr valign=bottom><td>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='O2010UnextractableRule' onchange='msoffice2010DocFilter.switchTags(this)' class='specialFilter_dialog_label'>");
	str.append("<option value='unextractableWordParagraphStyles'>" + jsO2010UnextractableWordParagraphStyles + "</option>");
	str.append("<option value='unextractableWordCharacterStyles'>" + jsO2010UnextractableWordCharacterStyles + "</option>");
	str.append("</select>");

	str.append("</td><td nowrap align=right>");

	str.append("<input type='button' value='" + jsAdd + "' onclick='msoffice2010DocFilter.onAdd()'>");
		
	str.append("<input type='button' value='" + jsDelete + "' id='O2010DeleteButton' onclick='msoffice2010DocFilter.deleteTag()' disabled>");
	str.append("</td></tr></table>");

	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.paragraphStyles] = "";
	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.characterStyles] = "";
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.paragraphStyles] = msoffice2010DocFilter.defaultUnextractableWordParagraphStyles;
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.characterStyles] = msoffice2010DocFilter.defaultUnextractableWordCharacterStyles;
	
	msoffice2010DocFilter.init();
	var tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption].split(",");
	str.append("<div id='o2010StyleContent'>")
	str.append("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");

	var tableContent = msoffice2010DocFilter.generateTableByPage(tags, 0);
	if(tableContent){
		str.append(tableContent);
	}
	else
	{
		str.append(msoffice2010DocFilter.genenrateEmptyTable());
	}
	str.append("</table>");
	str.append("</div>");
	
	var PagePropTotalSize = msoffice2010DocFilter.getPageSize();
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=msoffice2010DocFilter.prePage('"+tags+"')>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=msoffice2010DocFilter.nextPage('"+tags+"')>" + jsNext + "</a>|");
	str.append("<input id='pageCountO2010' size=3 type='text' value="+(msoffice2010DocFilter.currentPage+1)+" >");
	str.append(" / <span id='PageTotalSizeO2010' class='standardText'>" + PagePropTotalSize + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=msoffice2010DocFilter.goToPage('"+tags+"')>");
	
	var dialogObj = document.getElementById('msoffice2010FilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	
	saveMSOffice2010DocFilter.edit = false;
	saveMSOffice2010DocFilter.color = color;
	saveMSOffice2010DocFilter.topFilterId = topFilterId;
	
	this.setDeleteButtonStyle();
}

MSOffice2010Filter.prototype.prePage = function(tags)
{
	tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
    tags = msoffice2010DocFilter.removeEmptyElements(tags.split(","));
    if(tags.length == 0)
    {
       return;
    }
    if(msoffice2010DocFilter.currentPage > 0)
	{
    	msoffice2010DocFilter.currentPage --;
    	msoffice2010DocFilter.generateStyleContent(tags, msoffice2010DocFilter.currentPage);
    	msoffice2010DocFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

MSOffice2010Filter.prototype.getPageSize = function()
{
	var tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	tags = msoffice2010DocFilter.removeEmptyElements(tags.split(","));
	var itemsTotalCount = tags.length;
	var countPerPage = this.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

MSOffice2010Filter.prototype.nextPage = function(tags)
{
	tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	tags = msoffice2010DocFilter.removeEmptyElements(tags.split(","));
	if(tags.length == 0)
	{
	   return;
	}
	if(msoffice2010DocFilter.currentPage < Math.floor(tags.length/msoffice2010DocFilter.tagsEveryPage + 1))
	{
		msoffice2010DocFilter.currentPage ++;
		var tableContent = msoffice2010DocFilter.generateTableByPage(tags,msoffice2010DocFilter.currentPage);
		var validate = new Validate();
		if(!validate.isNumber(tableContent))
		{
			var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
			str.append(tableContent);
			str.append("</table>");
			document.getElementById("o2010StyleContent").innerHTML = str.toString();
			msoffice2010DocFilter.setPageValue();
		}
		else
		{
			msoffice2010DocFilter.currentPage --;
			msoffice2010DocFilter.setPageValue();
		}		

	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

MSOffice2010Filter.prototype.goToPage = function(tags)
{
	tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	tags = msoffice2010DocFilter.removeEmptyElements(tags.split(","));
	var pageValue = document.getElementById("pageCountO2010").value;
	var validate = new Validate();
	if(! validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	if(pageValue < 1 || pageValue > Math.floor(tags.length/msoffice2010DocFilter.tagsEveryPage + 1))
	{
		alert(invalidatePageValue.replace("%s", Math.floor(tags.length/msoffice2010DocFilter.tagsEveryPage + 1)));
		return;
	}

	msoffice2010DocFilter.currentPage = pageValue - 1;
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append(msoffice2010DocFilter.generateTableByPage(tags, msoffice2010DocFilter.currentPage));
	str.append("</table>");
	document.getElementById("o2010StyleContent").innerHTML = str.toString();

}

MSOffice2010Filter.prototype.addStyle = function()
{
	var validate = new Validate();
	var styleName = new StringBuffer(document.getElementById("o2010styleToAdd").value);

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
	
	var tagString = new StringBuffer("," + msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption] + ",");
	if(tagString.indexOf("," + styleName.toString() + ",") != -1 )
	{
		alert(existStyle + styleName.toString());
		return;
	}
	
	var allStyles = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	if (allStyles.length > 0 && allStyles.charAt(allStyles.length - 1) != ",")
	{
		allStyles += ","
	}
	allStyles += styleName.toString();
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption] = allStyles;
	
	msoffice2010DocFilter.generateStyleContent(allStyles.split(","), msoffice2010DocFilter.currentPage);	
	
	msoffice2010DocFilter.showStyleSelectBox(false);
	closePopupDialog('addO2010StyleDialog');
}

MSOffice2010Filter.prototype.setDeleteButtonStyle = function()
{
	var isDisabled = msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.paragraphStyles].length == 0
			&& msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.characterStyles].length == 0;

	document.getElementById("O2010DeleteButton").disabled = isDisabled;
}

MSOffice2010Filter.prototype.deleteTag = function()
{
	var hasTagsToDelete = msoffice2010DocFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteO2010StyleDialog");
	}
}

MSOffice2010Filter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsStyleType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<Label class='tagName_td'>" + jsStylesToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsStylesCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");
	var tagTypes = document.getElementById("O2010UnextractableRule");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var checkedTags = msoffice2010DocFilter.selectTagsMap[value];
		var checkedTagsArray = checkedTags.split(",");
		
		if(msoffice2010DocFilter.isNotEmptyArray(checkedTagsArray))
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
				if(checkedTag && checkedTag != "" && msoffice2010DocFilter.isCheckedTagInDefaultArray(value, checkedTag))
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
	str.append("<a href='#' class='specialfilter_a' onclick='msoffice2010DocFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='msoffice2010DocFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteO2010StylesDialogLable").innerHTML = jsRemoveStylesNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteO2010StyleTableContent").innerHTML = str.toString();
	return true;
}

MSOffice2010Filter.prototype.isCheckOrClearAll = function( checkOrClear )
{
    var checkBoxObjs = document.getElementsByName(msoffice2010DocFilter.paragraphStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
    
    checkBoxObjs = document.getElementsByName(msoffice2010DocFilter.characterStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
}

MSOffice2010Filter.prototype.checkAllTagsToDelete = function ()
{
	msoffice2010DocFilter.isCheckOrClearAll(true);
}

MSOffice2010Filter.prototype.clearAllTagsToDelete = function()
{
	msoffice2010DocFilter.isCheckOrClearAll(false);
}

MSOffice2010Filter.prototype.isNotEmptyArray = function (array)
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

MSOffice2010Filter.prototype.isCheckedTagInDefaultArray = function(value, checkedTag)
{
    var tagsStringByOption = msoffice2010DocFilter.getTagsStringByOption(value);
    return (","+tagsStringByOption+",").indexOf(","+checkedTag+",") != -1;
}

MSOffice2010Filter.prototype.getTagsStringByOption = function(currentOption)
{
    return msoffice2010DocFilter.selectTagsMap[currentOption];
}

MSOffice2010Filter.prototype.deleteStyles = function()
{
	var paragraphStylesToDelete = document.getElementsByName(msoffice2010DocFilter.paragraphStyles);
	var characterStylesToDelete = document.getElementsByName(msoffice2010DocFilter.characterStyles);
		
	msoffice2010DocFilter.buildTagsString(paragraphStylesToDelete);
	msoffice2010DocFilter.buildTagsString(characterStylesToDelete);
	
	closePopupDialog('deleteO2010StyleDialog');
}

MSOffice2010Filter.prototype.buildTagsString = function(array)
{
	for(var i = 0; i < array.length; i++)
	{
		var a = array[i];
		if(a && a.checked)
		{
			var tagType = a.getAttribute("tagType");
			var tagValue = a.getAttribute("tagValue");
			
			msoffice2010DocFilter.removeStyle(tagValue,tagType);
			
			var tagsString = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
			var tags = tagsString.split(",");
			
			var tagLength = tags.length;
			tagLength = (tagLength == 0) ? 1 : tagLength;
			var currentMaxPageNum = Math.floor((tagLength - 1) / msoffice2010DocFilter.tagsEveryPage);
			if(msoffice2010DocFilter.currentPage > currentMaxPageNum)
			{
				msoffice2010DocFilter.currentPage = currentMaxPageNum;
			}
			
			msoffice2010DocFilter.setPageValue();
			
			if(tags.length == 0)
			{
				msoffice2010DocFilter.generateTotalEmptyTable();
			}
			else
			{
				msoffice2010DocFilter.generateStyleContent(tags, msoffice2010DocFilter.currentPage);
			}
			
		}
	}
}

MSOffice2010Filter.prototype.onAdd = function()
{
    showPopupDialog("addO2010StyleDialog");
    document.getElementById("o2010styleToAdd").value = "";
    msoffice2010DocFilter.showStyleSelectBox(true);
}

MSOffice2010Filter.prototype.onDelete = function()
{
	tags = msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.currentOption];
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
			msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.currentOption]= styles;
			msoffice2010DocFilter.generateStyleContent(styles.split(","), msoffice2010DocFilter.currentPage);
		}
	}
	else
	{
		alert(noStylesToDelete);
	}
}

MSOffice2010Filter.prototype.switchTags = function(tagsSelectBox)
{
	msoffice2010DocFilter.currentOption = tagsSelectBox.options[tagsSelectBox.selectedIndex].value;
	var tagsString = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	
	var tags = msoffice2010DocFilter.removeEmptyElements(tagsString.split(","));
	msoffice2010DocFilter.currentPage = 0;
	if(tags.length != 0)
	{
		msoffice2010DocFilter.generateStyleContent(tags, msoffice2010DocFilter.currentPage);
	}
	else
	{
		msoffice2010DocFilter.generateTotalEmptyTable();
	}
	msoffice2010DocFilter.setPageValue();
	checkAllTags = false;
	
	if (document.getElementById("o2010CheckAll"))
	{
		document.getElementById("o2010CheckAll").checked = false;
	}

	msoffice2010DocFilter.setDeleteButtonStyle();
}

MSOffice2010Filter.prototype.generateTotalEmptyTable = function()
{
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	str.append(msoffice2010DocFilter.genenrateEmptyTable());
	str.append("</table>");
	document.getElementById("o2010StyleContent").innerHTML = str.toString();
}

MSOffice2010Filter.prototype.checkAll = function(tags)
{
	tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	tags = tags.split(",");
	var checkBoxObj = document.getElementById("o2010CheckAll");
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
	msoffice2010DocFilter.setDeleteButtonStyle();
}

MSOffice2010Filter.prototype.generateStyleContent = function (tags, currentPage)
{
	var str = new StringBuffer("<table id='styleContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");

	str.append(msoffice2010DocFilter.generateTableByPage(tags, currentPage));
	str.append("</table>");
	document.getElementById("o2010StyleContent").innerHTML = str.toString();
	this.setPageValue();
}

MSOffice2010Filter.prototype.init = function()
{
 	msoffice2010DocFilter.currentOption = "unextractableWordParagraphStyles";
	msoffice2010DocFilter.currentPage = 1;
	msoffice2010DocFilter.order = "asc";
}

MSOffice2010Filter.prototype.genenrateEmptyTable = function()
{
	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	str.append("<td width='5%'><input type='checkbox' id='o2010CheckAll'/>");
	str.append("</td>");
	str.append("<td width='95%' class='tagName_td'>" + jsValue);
	str.append("</td>");
	str.append("</tr>");
	return str.toString();
}

MSOffice2010Filter.prototype.removeEmptyElements = function(array)
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

MSOffice2010Filter.prototype.generateTableByPage = function(tags, currentPageNum)
{
	tags = msoffice2010DocFilter.removeEmptyElements(tags);
	var totalpageNum = Math.floor((tags.length + 1) / msoffice2010DocFilter.tagsEveryPage) + 1;

	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	var check = checkAllTags ? "checked" : "";
	str.append("<td width='5%'><input type='checkbox' onclick=msoffice2010DocFilter.checkAll('"+tags+"') id='o2010CheckAll' "+check+">");
	str.append("</td>");
	str.append("<td width='80%' class='tagName_td'><a href='#' class='tagName_td' onmouseenter=mouseEnter('sort_img') onmouseout=mouseOut('sort_img') onclick='msoffice2010DocFilter.sortTable()'>" + jsValue + "</a>");
	
	var sortImgSrc = "/globalsight/images/sort-up.gif";
	if(msoffice2010DocFilter.order == 'asc')
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
//	str.append(msoffice2010DocFilter.currentPage);
//	str.append("/");
//	str.append(totalpageNum);
	str.append("</td>");
	str.append("</tr>");
	var startIndex = 0;
	startIndex = msoffice2010DocFilter.tagsEveryPage * currentPageNum;
	while (startIndex >= tags.length && tags.length != 0)
	{
		var maxPage = Math.floor(tags.length/htmlFilter.tagsEveryPage + 1);
		alert(maxPageNums + maxPage);
		return maxPage;
	}
	
	msoffice2010DocFilter.currentPage = currentPageNum;
	
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
			if(i >= startIndex && i < startIndex + msoffice2010DocFilter.tagsEveryPage)
			{
				display='';
			}

			str.append("<tr style='background-color:"+backgroundColor+";display:"+display+"'>");
			
            check = "";
            
			var checkedTags = msoffice2010DocFilter.getCheckedTags();
			if(msoffice2010DocFilter.isSelectedStyle(tags[i], checkedTags))
			{
				check = "checked";
			}
			else
			{
				check = "";
			}
			
			str.append("<td><input onclick='msoffice2010DocFilter.checkthis(this)' type='checkbox' name= '"+tags[i]+"' id='styles_"+i+"' "+check+"></td>");
			str.append("<td colspan='2' class='tagValue_td' width=100%>"+tags[i]+"</td><td></td>");
			str.append("</tr>");
		}
	}
	return str.toString();
		
}

MSOffice2010Filter.prototype.checkthis = function(currentCheckBox)
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
	
	msoffice2010DocFilter.setDeleteButtonStyle();
}

MSOffice2010Filter.prototype.addThisTag = function(tagName)
{
	var checkedTags = msoffice2010DocFilter.getCheckedTags();
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

MSOffice2010Filter.prototype.removeThisTag = function(tagName)
{
	var checkedTags = msoffice2010DocFilter.getCheckedTags();
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

MSOffice2010Filter.prototype.removeStyle = function(style, styleType)
{
	var allStyle = msoffice2010DocFilter.selectTagsMap[styleType];
	var styles = allStyle.split(",");
	for (var i = 0; i < styles.length; i++)
	{
		if (styles[i] == style)
		{
			styles[i] = "";
		}
	}
	
	msoffice2010DocFilter.selectTagsMap[styleType] = this.generateArrayToString(styles);
	
	allStyle = msoffice2010DocFilter.allSelectTagsOption[styleType];
	styles = allStyle.split(",");
	for (var i = 0; i < styles.length; i++)
	{
		if (styles[i] == style)
		{
			styles[i] = "";
		}
	}
	
	msoffice2010DocFilter.allSelectTagsOption[styleType] = this.generateArrayToString(styles);
}

MSOffice2010Filter.prototype.getCheckedTags = function()
{
	return msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.currentOption];
}

MSOffice2010Filter.prototype.setCheckedTags = function(tags)
{
	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.currentOption] = tags;
}

MSOffice2010Filter.prototype.isSelectedStyle = function(style, allStyle)
{
	var s = ",".concat(allStyle).concat(",");
	var testS = ",".concat(style).concat(",");
	
	return s.indexOf(testS) > -1;
}

MSOffice2010Filter.prototype.generateArrayToString = function(tagsArray)
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

MSOffice2010Filter.prototype.sortTable = function()
{
	var tags = msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption];
	tags = tags.split(",");
	tags = msoffice2010DocFilter.removeEmptyElements(tags);
	tags.sort();
	if(msoffice2010DocFilter.order == 'asc')
	{
		msoffice2010DocFilter.order = 'desc';
	}
	else
	{
		msoffice2010DocFilter.order = 'asc';
		tags.reverse();
	}
	msoffice2010DocFilter.generateStyleContent(tags, msoffice2010DocFilter.currentPage);
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.currentOption] = msoffice2010DocFilter.generateArrayToString(tags);
}

MSOffice2010Filter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("msoffice2010FilterDialog");
}

MSOffice2010Filter.prototype.getXmlFilterSelect = function (filter)
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

function saveMSOffice2010DocFilter()
{
	var check = new Validate();
	var filterName = document.getElementById("o2010FilterName").value;
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
	
	var filterDesc = document.getElementById("o2010FilterDesc").value;
	var headerTranslate = document.getElementById("headerTranslate").checked;
	var masterTranslate = document.getElementById("masterTranslate").checked;
	var xmlFilterIdAndTableName = document.getElementById("xmlFilterSelect").value;
	var splitedXmlIdTable = splitByFirstIndex(xmlFilterIdAndTableName, "-");
	var xmlFilterId = (splitedXmlIdTable) ? splitedXmlIdTable[0] : -2;
	
	var obj = {
			filterTableName:"office2010_filter",
			filterName:filterName,
			filterDesc:filterDesc,
			headerTranslate:headerTranslate,
			masterTranslate:masterTranslate,
			xmlFilterId:-2,
			unextractableWordParagraphStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.paragraphStyles],
			unextractableWordCharacterStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.characterStyles],
			allParagraphStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.paragraphStyles],
			allCharacterStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.characterStyles],
			companyId:companyId
			};
	
	if(saveMSOffice2010DocFilter.edit)
	{
		closePopupDialog("msoffice2010FilterDialog");
		sendAjax(obj, "updateMSOffice2010Filter", "updateMSOffice2010FilterCallback");
	}
	else
	{
		sendAjax(obj, "checkExist", "checkExistMSOffice2010FilterCallback");
	}
	
	checkExistMSOffice2010FilterCallback.obj = obj;
}

function checkExistMSOffice2010FilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("msoffice2010FilterDialog");
		sendAjax(checkExistMSOffice2010FilterCallback.obj, "saveMSOffice2010Filter", "saveMSOffice2010FilterCallback");
	}
	else
	{
		alert(existFilterName + checkExistMSOffice2010FilterCallback.obj.filterName);
	}
}

function updateMSOffice2010FilterCallback(data)
{
	var color = saveMSOffice2010DocFilter.color;
	var filterId = saveMSOffice2010DocFilter.filterId;
	var filter = saveMSOffice2010DocFilter.filter;
	var topFilterId = saveMSOffice2010DocFilter.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = "office2010_filter";
		jpFilter.filterName = checkExistMSOffice2010FilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistMSOffice2010FilterCallback.obj.filterDesc;
		jpFilter.headerTranslate = checkExistMSOffice2010FilterCallback.obj.headerTranslate;
		jpFilter.masterTranslate = checkExistMSOffice2010FilterCallback.obj.masterTranslate;
		jpFilter.xmlFilterId = checkExistMSOffice2010FilterCallback.obj.xmlFilterId;
		jpFilter.unextractableWordParagraphStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.allParagraphStyles = checkExistMSOffice2010FilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistMSOffice2010FilterCallback.obj.allCharacterStyles;
		jpFilter.companyId = companyId;

		var specialFilters = updateSpecialFilter(saveMSOffice2010DocFilter.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveMSOffice2010FilterCallback(data)
{
	var color = saveMSOffice2010DocFilter.color;
	var topFilterId = saveMSOffice2010DocFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = "office2010_filter";
		jpFilter.filterName = checkExistMSOffice2010FilterCallback.obj.filterName;
		jpFilter.filterDescription = checkExistMSOffice2010FilterCallback.obj.filterDesc;
		jpFilter.headerTranslate = checkExistMSOffice2010FilterCallback.obj.headerTranslate;
		jpFilter.masterTranslate = checkExistMSOffice2010FilterCallback.obj.masterTranslate;
		jpFilter.xmlFilterId = checkExistMSOffice2010FilterCallback.obj.xmlFilterId;
		jpFilter.unextractableWordParagraphStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.allParagraphStyles = checkExistMSOffice2010FilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistMSOffice2010FilterCallback.obj.allCharacterStyles;
		jpFilter.companyId = companyId;
		filter.specialFilters.push(jpFilter);
	    reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}
