var propertiesFilter = new JavaPropertiesFilter();
var tagsContentTable = "<table id='tagsContentTable' border=0 width='98%' class='standardText'>"; 

function JavaPropertiesFilter()
{
	this.filterTableName = "java_properties_filter";
	this.optionInternalText = "0";
	
	this.options = new Array();
	this.options[0] = this.optionInternalText;
	
	this.optionNameMap = new Object();
	this.optionNameMap[this.optionInternalText] = jsInternalText;

	
	this.optionObjsMap = new Object();
	this.optionObjsMap[this.optionInternalText] = new Array();
	this.checkedItemIds = new Array();
	
	this.sortOrders = new Array();
	this.sortOrders[0] = "asc";
	this.sortOrders[1] = "asc";
	
	this.tagsEveryPage = 10;
	this.currentPage = 0;
	this.sortColumnIndex = 1;
	this.currentPage = 0;
	this.currentOption = this.optionInternalText;
	this.editItemId = -1;
	this.editItemAttributes = new Array();
}

JavaPropertiesFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

JavaPropertiesFilter.prototype.initOptionMap = function (filter)
{
	propertiesFilter.checkedItemIds = new Array();
	
	if (filter)
	{
		var internalTexts = this.filter.internalTexts;
		if (internalTexts)
		{
			if (internalTexts.content)
			{
				var itemArray = new Array();
				itemArray.push(internalTexts);
				propertiesFilter.optionObjsMap[this.optionInternalText] = itemArray;
			}
			else
			{
				propertiesFilter.optionObjsMap[this.optionInternalText] = this.removeEmptyTags(internalTexts);
			}
		}
		else
		{
			propertiesFilter.optionObjsMap[this.optionInternalText] = new Array();
		}
	}
	else
	{
		propertiesFilter.optionObjsMap[this.optionInternalText] = new Array();
		var itemArray = new Array();		
		var item = new Object();
		item.content = "\\{[^{]*?\\}";
		item.isRegex = true;
		itemArray.push(item);
		propertiesFilter.optionObjsMap[this.optionInternalText] = itemArray;
	}
}

JavaPropertiesFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	propertiesFilter.currentPage = 0;
	this.initOptionMap(this.filter);
	
	var str = new StringBuffer("<table border=0 width='100%'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");
	str.append("<td ><input type='text' maxlength='"+maxFilterNameLength+"' style='width:100%' id='javaPropertiesFilterName' value='" + this.filter.filterName + "'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");
	str.append("<td><textarea rows='4' cols='17' style='width:100%' id='javaPropertiesDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	var isCheckSid = (this.filter.enableSidSupport) ? "checked":"";
	var isCheckEscape = (this.filter.enableUnicodeEscape) ? "checked":"";
	var isPreserveSpaces = (this.filter.enablePreserveSpaces) ? "checked":"";
	str.append("<table border=0 width='100%'>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsEnableSIDSupport + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isSupportSid' type='checkbox' name='supportSid' value='"+this.filter.enableSidSupport+"' "+isCheckSid+"/></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsEnableUnicodeEscape + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isUnicodeEscape' type='checkbox' name='unicodeEscape' value='"+this.filter.enableUnicodeEscape+"' "+isCheckEscape+"/></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsPreserveTrailingSpaces + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isPreserveSpaces' type='checkbox' name='preserveSpaces' value='"+this.filter.enablePreserveSpaces+"' "+isPreserveSpaces+"/></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsSecondaryFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.getSecondaryFilterSelectForJP(this.filter) + "</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>" + generateBaseFilterList(this.filterTableName, this.filter) + "</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<div><br/><br/></div>");

	//str.append("<div class='specialFilter_dialog_label'>");
	//str.append(this.generateProprtiesTable(this.filter));
	//str.append("</div>");
	
	var dialogObj = document.getElementById('javaPropertiesFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveJavaProperties.edit = true;
	saveJavaProperties.filterId = filterId;
	saveJavaProperties.color = color;
	saveJavaProperties.filter = this.filter;
	saveJavaProperties.specialFilters = specialFilters;
	saveJavaProperties.topFilterId = topFilterId;
}

JavaPropertiesFilter.prototype.generateProprtiesTable = function (filter)
{
	var str = new StringBuffer("");
	str.append("<table border=0 width='98%' class='standardText'>");
	str.append("<tr><td>");
	str.append("<div id='propFilterInternalContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='propFilterInternalSection' onchange='propertiesFilter.switchOption(this)'>");
	str.append("<option value='" + this.optionInternalText + "'>" + this.optionNameMap[this.optionInternalText] + "</option>");
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "...' onclick='propertiesFilter.addTag()'></input>");
	str.append("<input type='button' value='" + jsDelete + "...' onclick='propertiesFilter.deleteTag()' id='propDeleteTag'></input>");
	str.append("</div>");
	str.append("</td></tr></table>");
	
	str.append("<div id='propertiesTagsContent'>");
	str.append(tagsContentTable);
	
	if (filter)
	{
		str.append(propertiesFilter.generateOptionContent(this.optionInternalText, 0));
	}
	else
	{
		str.append(propertiesFilter.generateOptionContent(this.optionInternalText));
	}
	
	str.append("</table>");
	str.append("</div>");
	str.append("<div>");
	
	var PagePropTotalSize = filter ? propertiesFilter.getPageSize(propertiesFilter.optionObjsMap[propertiesFilter.optionInternalText].length) : 1;
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=propertiesFilter.prePage()>" + jsPrevious + "</a>");
	str.append("|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=propertiesFilter.nextPage()>" + jsNext + "</a>");
	str.append("<input id='pageCountPropFilter' size=2 type='text' value="+((filter) ? propertiesFilter.currentPage+1 : 1)+" ></input>");
	str.append(" / <span id='PagePropTotalSize' class='standardText'>" + PagePropTotalSize + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=propertiesFilter.goToPage()></input>");
	str.append("</div>");
	
	return str.toString();
}

JavaPropertiesFilter.prototype.goToPage = function()
{
	var pageValue = document.getElementById("pageCountPropFilter").value;
	var validate = new Validate();
	if(!validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	var arraysize = propertiesFilter.getCurrentObjectsSize();
	
	if(pageValue < 1 || pageValue > propertiesFilter.getPageSize(arraysize))
	{
		alert(invalidatePageValue.replace("%s", propertiesFilter.getPageSize(arraysize)));
		propertiesFilter.setPageValue();
		return;
	}

	propertiesFilter.currentPage = pageValue - 1;
	var content = propertiesFilter.generateOptionContent(propertiesFilter.currentOption, propertiesFilter.currentPage);
	propertiesFilter.refreshTagsContent(content);
}

JavaPropertiesFilter.prototype.prePage = function()
{
    if(propertiesFilter.currentPage > 0)
	{
    	propertiesFilter.currentPage --;
    	var content = propertiesFilter.generateOptionContent(propertiesFilter.currentOption, propertiesFilter.currentPage);
    	propertiesFilter.refreshTagsContent(content);
    	propertiesFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

JavaPropertiesFilter.prototype.nextPage = function()
{
	var arraysize = propertiesFilter.getCurrentObjectsSize();
	if(propertiesFilter.currentPage < propertiesFilter.getPageSize(arraysize) - 1)
	{
		propertiesFilter.currentPage ++;
		var content = propertiesFilter.generateOptionContent(propertiesFilter.currentOption, propertiesFilter.currentPage);
		propertiesFilter.refreshTagsContent(content);
		propertiesFilter.setPageValue();
	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

JavaPropertiesFilter.prototype.isInternalTextExist = function(content, isRegex)
{
	var internalTexts = this.optionObjsMap[this.optionInternalText];
	for (var i = 0; i < internalTexts.length; i++)
	{
		var item = internalTexts[i];
		if (item.content == content && item.isRegex == isRegex)
		{
			return true;
		}
	}
	
	return false;
}

JavaPropertiesFilter.prototype.setPageValue = function()
{
	document.getElementById("pageCountPropFilter").value = propertiesFilter.currentPage + 1;
}

JavaPropertiesFilter.prototype.addInternalText = function (content, isRegex)
{
	var addObj = new Object();
	addObj.content = content;
	addObj.isRegex = isRegex;
	this.optionObjsMap[this.optionInternalText].push(addObj);
}

JavaPropertiesFilter.prototype.getPageSize = function(itemsTotalCount)
{
	var countPerPage = propertiesFilter.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

JavaPropertiesFilter.prototype.addTag = function(radioId)
{
	var dialogId = "javaPropertiesInternalDialog";
	if(propertiesFilter.currentOption == propertiesFilter.optionInternalText)
	{
		dialogId = "javaPropertiesInternalDialog";
		document.getElementById("internalContent").value = "";
	}
	
	showPopupDialog(dialogId);
}

JavaPropertiesFilter.prototype.appendAttributeOptions = function(selectElement, attributes, cleanFirst)
{
	if (selectElement && attributes && attributes.length > 0)
	{
		if (cleanFirst)
		{
			selectElement.innerHTML = "";
		}
		
		for(var i = 0; i < attributes.length; i++)
		{
			var attr = attributes[i];
			var elOptNew = document.createElement('option');
			elOptNew.text = propertiesFilter.getAttributeString(attr);
			elOptNew.value = attr.itemid;
			
			try
			{
				selectElement.add(elOptNew, null);
			}
			catch(ex)
			{
				selectElement.add(elOptNew);
			}
		}
	}
}

JavaPropertiesFilter.prototype.newItemId = function()
{
	var newid = (new Date()).getTime();
	return newid;
}

JavaPropertiesFilter.prototype.switchOption = function(propFilterInternalSection)
{
	propertiesFilter.currentOption = propFilterInternalSection.options[propertiesFilter.selectedIndex].value;
	propertiesFilter.currentPage = 0;
	
	propertiesFilter.checkedItemIds = new Array();
	var content = propertiesFilter.generateOptionContent(propertiesFilter.currentOption, propertiesFilter.currentPage);
	propertiesFilter.refreshTagsContent(content);
	document.getElementById("checkAllPropFilter").checked = false;
}


JavaPropertiesFilter.prototype.refreshTagsContent = function(content)
{
	var ccc = new StringBuffer(tagsContentTable);
	ccc.append(content);
	ccc.append("</table>");
	
	document.getElementById("propertiesTagsContent").innerHTML = ccc.toString();
}

JavaPropertiesFilter.prototype.getCurrentObjectsSize = function()
{
	return propertiesFilter.optionObjsMap[propertiesFilter.currentOption].length;
}

JavaPropertiesFilter.prototype.checkAll = function()
{
	var checkBoxObj = document.getElementById("checkAllPropFilter");
	var isSelect = checkBoxObj.checked;
	var objs = this.optionObjsMap[this.currentOption];
	for(var i = 0; i < objs.length; i++)
	{
		if(document.getElementById(i))
		{
			document.getElementById(i).checked = isSelect;	
		}
		
		objs[i].isSelected = isSelect;
	}
}

JavaPropertiesFilter.prototype.checkthis = function(cobj)
{
	var oid = cobj.id;
	propertiesFilter.optionObjsMap[propertiesFilter.currentOption][oid].isSelected = cobj.checked;
}

JavaPropertiesFilter.prototype.encodingHtml = function (s)
{
	if (s)
	{
		s = "" + s;
		return s.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/\"/g,"&quot;");	
	}
	
	return "";
}


JavaPropertiesFilter.prototype.isNotEmptyArray = function (array)
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


JavaPropertiesFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = propertiesFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("propertiesDeleteTagDialog");
	}
}

JavaPropertiesFilter.prototype.generateDeleteTagTableContent = function()
{
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsTagType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<Label class='tagName_td'>" + jsTagsToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsTagsCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	
	var count = 0;
	var sum = 0;
	var isOdd = true;
	for (var i = 0; i < this.options.length; i++)
	{
		count = 0;
		var option = this.options[i];
		var array =  this.optionObjsMap[option];
		
		if(propertiesFilter.isNotEmptyArray(array))
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
			str.append(this.optionNameMap[option]);
			str.append("</td>");	
			str.append("<td width='400px'>");
			
			for(var j = 0; j < array.length; j++)
			{
				var obj = array[j];
				if(obj.isSelected)
				{
					str.append("<input type='checkbox' name='delete_"+option+"' index='" + j + "' checked>");
					str.append(propertiesFilter.encodingHtml(obj.content));
					str.append("</input>");
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
	str.append("<a href='#' class='specialfilter_a' onclick='propertiesFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='propertiesFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("propddeleteTagsDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("propDeleteTagTableContent").innerHTML = str.toString();
	return true;
}

JavaPropertiesFilter.prototype.checkAllTagsToDelete = function ()
{
    this.isCheckOrClearAll(true);
}

JavaPropertiesFilter.prototype.clearAllTagsToDelete = function()
{
	this.isCheckOrClearAll(false);
}

JavaPropertiesFilter.prototype.isCheckOrClearAll = function(checkOrClear )
{
	for (var i = 0; i < this.options.length; i++)
	{
		count = 0;
		var option = this.options[i];
		var tagsToDelete = document.getElementsByName("delete_" + option);
		var tags = this.optionObjsMap[option];

		for(var j = 0; j < tagsToDelete.length; j++)
		{
			var a = tagsToDelete[j];
			if(a)
			{
				a.checked = checkOrClear;
			}
		}
	}
}

JavaPropertiesFilter.prototype.deleteTags = function()
{
	for (var i = 0; i < this.options.length; i++)
	{
		count = 0;
		var option = this.options[i];
		var tagsToDelete = document.getElementsByName("delete_" + option);
		var tags = this.optionObjsMap[option];

		for(var j = 0; j < tagsToDelete.length; j++)
		{
			var a = tagsToDelete[j];
			if(a && a.checked)
			{
				var index = a.getAttribute("index");
				tags[index] = null;
			}
		}
		
		this.optionObjsMap[option] = this.removeEmptyTags(tags);
	}
	
	
	closePopupDialog('propertiesDeleteTagDialog');
	
	var maxPages = this.getPageSize(this.optionObjsMap[this.currentOption]);
	this.currentPage = (this.currentPage >= maxPages) ? maxPages - 1 : this.currentPage;
	document.getElementById("PagePropTotalSize").innerText = maxPages;
	
	var content = this.generateOptionContent(this.currentOption, this.currentPage);
	this.refreshTagsContent(content);
	this.setPageValue();
}

JavaPropertiesFilter.prototype.removeEmptyTags = function(tags)
{
	var newArray = new Array();
	for (var i = 0; i < tags.length; i++)
	{
		if (tags[i] && tags[i] != null)
		{
			newArray.push(tags[i]);
		}
	}
	
	return newArray;
}

function propInternalSortByContentAsc(a, b)
{
	return a.content.toString().localeCompare(b.content.toString());
}

function propInternalSortByContentDesc(a, b)
{
	return b.content.toString().localeCompare(a.content.toString());
}

function propInternalSortByRegexAsc(a, b)
{
	return a.isRegex.toString().localeCompare(b.isRegex.toString());
}

function propInternalSortByRegexDesc(a, b)
{
	return b.isRegex.toString().localeCompare(a.isRegex.toString());
}

JavaPropertiesFilter.prototype.sortTable = function(index)
{
	if(this.currentOption == this.optionInternalText)
	{
		var order = this.sortOrders[index];
		
		if(order == 'asc')
		{
			this.sortOrders[index] = 'desc';
			if (index == 0)
			{
				this.optionObjsMap[this.optionInternalText].sort(propInternalSortByContentAsc);
			}
			else if (index == 1)
			{
				this.optionObjsMap[this.optionInternalText].sort(propInternalSortByRegexAsc);
			}
		}
		else
		{
			this.sortOrders[index] = 'asc';
			if (index == 0)
			{
				this.optionObjsMap[this.optionInternalText].sort(propInternalSortByContentDesc);
			}
			else if (index == 1)
			{
				this.optionObjsMap[this.optionInternalText].sort(propInternalSortByRegexDesc);
			}
		}
	}
	
	var content = this.generateOptionContent(this.currentOption, this.currentPage);
	this.refreshTagsContent(content);
}

JavaPropertiesFilter.prototype.getSortImg  = function(columnIndex)
{
	var sortOrder = this.sortOrders[columnIndex];	
	if (!sortOrder)
	{
		sortOrder = 'asc';
		this.sortOrders[columnIndex] = 'asc';
	}
	
	var img = "/globalsight/images/sort-up.gif";
	if (sortOrder == "desc")
	{
		var img = "/globalsight/images/sort-down.gif";
	}
	
	return img;
}

JavaPropertiesFilter.prototype.generateOptionContent = function(optionValue, pageIndex)
{
	var objArray = propertiesFilter.optionObjsMap[optionValue];
	var str = new StringBuffer("");
	str.append("<tr class='htmlFilter_emptyTable_tr'>");
	
	if (objArray && objArray.length > 0)
	{
		var sortImgSrc = "/globalsight/images/sort-up.gif";
		if(propertiesFilter.sortOrder1 == 'asc')
		{
			sortImgSrc = "/globalsight/images/sort-up.gif";
		}
		else
		{
			sortImgSrc = "/globalsight/images/sort-down.gif";
		}
		
		var checkAllStr = (propertiesFilter.checkedItemIds.length == propertiesFilter.getCurrentObjectsSize()) ? "checked" : "";
		
		str.append("<td width='5%'><input type='checkbox' onclick=propertiesFilter.checkAll() id='checkAllPropFilter' " + checkAllStr + "></input></td>");

		if (optionValue == propertiesFilter.optionInternalText)
		{
			str.append("<td width='55%' class='tagName_td'><a href='#' class='tagName_td' onmouseenter=mouseEnter('sort_img_content') onmouseout=mouseOut('sort_img_content') onclick='propertiesFilter.sortTable(0)'>" + jsInternalContent + "</a>");
			str.append("<img class='not_display' id='sort_img_content' src='" + propertiesFilter.getSortImg(0) + "'></img>");
			str.append("</td>");
			str.append("<td class='tagName_td'><a href='#' class='tagName_td' onmouseenter=mouseEnter('sort_img_isRegex') onmouseout=mouseOut('sort_img_isRegex') onclick='propertiesFilter.sortTable(1)'>" + jsInternalIsRegex + "</a>");
			str.append("<img class='not_display' id='sort_img_isRegex' src='" + propertiesFilter.getSortImg(1) + "'></img>");
			str.append("</td>");
			str.append("</tr>");
		}
		
		var startIndex = 0;
		startIndex = propertiesFilter.tagsEveryPage * pageIndex;
		if(startIndex >= objArray.length)
		{
			var maxPage = propertiesFilter.getPageSize(objArray.length);
			alert(maxPageNums + maxPage);
			return maxPage;
		}
		
		for(var i = 0; i < propertiesFilter.tagsEveryPage; i++)
		{
			var realIndex = startIndex + i;
			var ruleObj = objArray[realIndex];
			
			if(ruleObj && ruleObj.content)
			{
				var backgroundColor = "#C7CEE0";
				if(i % 2 == 0)
				{
					backgroundColor = "#DFE3EE";
				}
				
				if (optionValue == propertiesFilter.optionInternalText)
				{
					var check = "";
					if(ruleObj.isSelected)
					{
						check = "checked";
					}
					
					str.append("<tr style='background-color:"+backgroundColor+";'>");
					str.append("<td width='5%'><input onclick='propertiesFilter.checkthis(this)' type='checkbox' id='" + realIndex + "' " + check + " ></input>");
					str.append("<td>" + propertiesFilter.encodingHtml(ruleObj.content) + "</td>");
					str.append("<td>" + ruleObj.isRegex + "</td>");
					str.append("</tr>");
				}
			}
		}
	}
	else
	{
		if (optionValue == propertiesFilter.optionInternalText)
		{
			str.append("<td width='5%'><input type='checkbox' id='checkAllPropFilter'/>");
			str.append("</td>");
			str.append("<td width='35%' class='tagName_td'>" + jsInternalContent);
			str.append("</td>");
			str.append("<td class='tagName_td'>" + jsInternalIsRegex);
			str.append("</td>");
		}
		str.append("</tr>");
		str.append("<tr><td colspan='2'><p><br /></p></td></tr>");
	}
	
	return str.toString();
}

JavaPropertiesFilter.prototype.generateDiv = function (topFilterId, color)
{
	this.initOptionMap();
	var str = new StringBuffer("<table border=0 width='100%'>");
	str.append("<tr>");
	str.append("<td class='specialFilter_dialog_label' width='80px;'>" + jsFilterName + ":</td>");
	str.append("<td ><input type='text' style='width:100%' maxlength='"+maxFilterNameLength+"' id='javaPropertiesFilterName' value='Java Properties Filter'></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<td class='specialFilter_dialog_label' VALIGN='bottom'>" + jsFilterDesc + ":</td>");
	str.append("<td><textarea rows='4' style='width:100%' cols='17' id='javaPropertiesDesc' name='desc'></textarea></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<table border=0 width='100%'>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsEnableSIDSupport + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isSupportSid' type='checkbox' name='supportSid' value='true'/></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsEnableUnicodeEscape + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isUnicodeEscape' type='checkbox' name='unicodeEscape' value='true'/></td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>" + jsPreserveTrailingSpaces + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='isPreserveSpaces' type='checkbox' name='preserveSpaces' value='false'></td>");
	str.append("</tr>");
	
	var filter = getFilterById(topFilterId);
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsSecondaryFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.getSecondaryFilterSelectForJP(filter) + "</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>" + generateBaseFilterList(this.filterTableName) + "</td>");
	str.append("</tr>");
	str.append("</table>");
	
	str.append("<div><br/><br/></div>");

	//str.append("<div class='specialFilter_dialog_label'>");
	//str.append(this.generateProprtiesTable(filter));
	//str.append("</div>");
	
	var dialogObj = document.getElementById('javaPropertiesFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	this.showDialog();
	saveJavaProperties.edit = false;
	saveJavaProperties.topFilterId = topFilterId;
	saveJavaProperties.color = color;
}

JavaPropertiesFilter.prototype.saveInternalText = function()
{
	var validate = new Validate();
	var content = document.getElementById("internalContent").value;
	var isRegex = document.getElementById("isRegex").checked;
	
	content = validate.trim(content);
	
	if(validate.isEmptyStr(content))
	{
		alert(jsInternalEmpty);
		return;
	}
	
	if(this.isInternalTextExist(content, isRegex))
	{
		alert(jsInternalExist);
		return;
	}
	
	propertiesFilter.addInternalText(content, isRegex);
	
	var content = propertiesFilter.generateOptionContent(propertiesFilter.currentOption, propertiesFilter.currentPage);
	propertiesFilter.refreshTagsContent(content);
	
	var objArray = propertiesFilter.optionObjsMap[propertiesFilter.currentOption];
	var maxPages = propertiesFilter.getPageSize(objArray.length);
	propertiesFilter.currentPage = (propertiesFilter.currentPage >= maxPages) ? maxPages - 1 : propertiesFilter.currentPage;
	document.getElementById("PagePropTotalSize").innerText = maxPages;

	closePopupDialog("javaPropertiesInternalDialog");
}

JavaPropertiesFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog("javaPropertiesFilterDialog");
}

function saveJavaProperties()
{
	var check = new Validate();
	var filterName = document.getElementById("javaPropertiesFilterName").value;
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
	var isNew = (saveJavaProperties.edit) ? "false" : "true";
	var filterId = saveJavaProperties.filterId;
	var filterDesc = document.getElementById("javaPropertiesDesc").value;
	var isSupportSid = document.getElementById("isSupportSid").checked;
	var isUnicodeEscape = document.getElementById("isUnicodeEscape").checked;
	var isPreserveSpaces = document.getElementById("isPreserveSpaces").checked;
	
	var secondaryFilterIdAndTableName = document.getElementById("secondaryFilterSelect").value;
	var index = secondaryFilterIdAndTableName.indexOf("-");
	var secondFilterId = -2;
	var secondFilterTableName = "";
	if (index > 0)
	{
		secondFilterId = secondaryFilterIdAndTableName.substring(0,index);
		secondFilterTableName = secondaryFilterIdAndTableName.substring(index+1);
	}
	
	var internalTexts = propertiesFilter.optionObjsMap[propertiesFilter.optionInternalText];
	if (internalTexts.length == 0)
	{
		internalTexts = new Array();
	}
	
	var baseFilterId = document.getElementById("java_properties_filter_baseFilterSelect").value;
	
	alertUserBaseFilter(baseFilterId);
	
	var obj = {
			isNew : isNew,
			filterTableName:"java_properties_filter",
			filterId:filterId,
			filterName:filterName, filterDesc:filterDesc, 
			isSupportSid:isSupportSid, isUnicodeEscape:isUnicodeEscape, 
			isPreserveSpaces:isPreserveSpaces, 
			companyId:companyId, 
			secondFilterId:secondFilterId, 
			secondFilterTableName:secondFilterTableName,
			"internalTexts":JSON.stringify(internalTexts),
			baseFilterId:baseFilterId
			};

	sendAjax(obj, "checkExist", "checkExistJavaPropertiesCallback");
	
	checkExistJavaPropertiesCallback.obj = obj;
}

function updateJavaPropertiesFilterCallback(data)
{
	var color = saveJavaProperties.color;
	var filterId = saveJavaProperties.filterId;
	var filter = saveJavaProperties.filter;
	var topFilterId = saveJavaProperties.topFilterId;
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = filterId;
		jpFilter.filterTableName = "java_properties_filter";
		jpFilter.filterName = checkExistJavaPropertiesCallback.obj.filterName;
		jpFilter.filterDescription = checkExistJavaPropertiesCallback.obj.filterDesc;
		jpFilter.enableSidSupport = checkExistJavaPropertiesCallback.obj.isSupportSid;
		jpFilter.enableUnicodeEscape = checkExistJavaPropertiesCallback.obj.isUnicodeEscape;
		jpFilter.enablePreserveSpaces = checkExistJavaPropertiesCallback.obj.isPreserveSpaces;
		jpFilter.secondFilterId = checkExistJavaPropertiesCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistJavaPropertiesCallback.obj.secondFilterTableName;
		jpFilter.baseFilterId = checkExistJavaPropertiesCallback.obj.baseFilterId;
		jpFilter.companyId = companyId;
		jpFilter.internalTexts = JSON.parse(checkExistJavaPropertiesCallback.obj.internalTexts);
		var specialFilters = updateSpecialFilter(saveJavaProperties.specialFilters, jpFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function checkExistJavaPropertiesCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("javaPropertiesFilterDialog");
		if(saveJavaProperties.edit)
		{
			sendAjax(checkExistJavaPropertiesCallback.obj, "updateJavaPropertiesFilter", "updateJavaPropertiesFilterCallback");
		}
		else
		{
			sendAjax(checkExistJavaPropertiesCallback.obj, "saveJavaPropertiesFilter", "saveJavaPropertiesFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("javaPropertiesFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}
function saveJavaPropertiesFilterCallback(data)
{
	var color = saveJavaProperties.color;
	var topFilterId = saveJavaProperties.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var jpFilter = new Object();
		jpFilter.id = data - 0;
		jpFilter.filterTableName = "java_properties_filter";
		jpFilter.filterName = checkExistJavaPropertiesCallback.obj.filterName;
		jpFilter.filterDescription = checkExistJavaPropertiesCallback.obj.filterDesc;
		jpFilter.enableSidSupport = checkExistJavaPropertiesCallback.obj.isSupportSid;
		jpFilter.enableUnicodeEscape = checkExistJavaPropertiesCallback.obj.isUnicodeEscape;
		jpFilter.enablePreserveSpaces = checkExistJavaPropertiesCallback.obj.isPreserveSpaces;
		jpFilter.companyId = companyId;
		jpFilter.secondFilterId = checkExistJavaPropertiesCallback.obj.secondFilterId;
		jpFilter.secondFilterTableName = checkExistJavaPropertiesCallback.obj.secondFilterTableName;
		jpFilter.baseFilterId = checkExistJavaPropertiesCallback.obj.baseFilterId;
		jpFilter.internalTexts = JSON.parse(checkExistJavaPropertiesCallback.obj.internalTexts);
		
		filter.specialFilters.push(jpFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}

JavaPropertiesFilter.prototype.getSecondaryFilterSelectForJP = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='secondaryFilterSelect' class='specialFilter_dialog_label' style='width:200px'>");
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
	
	return str.toString();
}

