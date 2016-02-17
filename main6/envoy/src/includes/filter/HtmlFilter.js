var htmlFilter = new HtmlFilter();

function HtmlFilter()
{
	this.filterTableName = "html_filter";
	this.dialogId = "htmlFilterDialog";
	//This only use for add html filter
	this.embeddableOption = "embeddable_tags";
	this.pairedOption = "paired_tags";
	this.unPairedOption = "unpairedTag_tags";
	this.switchTagMap = "switch_tag_map";
	this.whitePreservingTag = "white_preserving_tag";
	this.translatableAttribute = "translatable_attribute";
	this.internalTag = "internal_tag";

	this.defaultEmbeddableTags = "a,abbr,acronym,b,basefont,bdo,big,blink,br,cite,code,del,dfn,em,font,i,img,ins,kbd,nobr,q,s,samp,small,span,strike,strong,sub,sup,tt,u,var,wbr";
	this.defaultPairedTags = "a,abbr,acronym,b,bdo,big,blink,button,cite,code,del,dfn,em,font,i,ins,kbd,label,nobr,plaintext,q,ruby,s,samp,select,small,span,strike,strong,sub,sup,textarea,tt,u,var,xmp";
	this.defaultUnPairedTags = "br,hr,img,input,rt,wbr";
	this.defaultSwitchTagMaps = "script:javascript,style:css-styles,xml:xml";
	this.defaultWhitePreservingTags = "listing,pre";
	this.defaultTranslatableAttributes = "abbr,accesskey,char,label,prompt,standby,summary,title";
	this.defaultInternalTag = "";
	
	this.selectTagsMap = new Object();

	this.selectTagsMap[this.embeddableOption] = this.defaultEmbeddableTags;
	this.selectTagsMap[this.pairedOption] = this.defaultPairedTags;
	this.selectTagsMap[this.unPairedOption] = this.defaultUnPairedTags;
	this.selectTagsMap[this.switchTagMap] = this.defaultSwitchTagMaps;
	this.selectTagsMap[this.whitePreservingTag] = this.defaultWhitePreservingTags;
	this.selectTagsMap[this.translatableAttribute] = this.defaultTranslatableAttributes;
	this.selectTagsMap[this.internalTag] = "";
	
	this.allSelectTagsOption = new Array();
	this.allSelectTagsOption[0] = this.embeddableOption;
    this.allSelectTagsOption[1] = this.pairedOption;
    this.allSelectTagsOption[2] = this.unPairedOption;
    this.allSelectTagsOption[3] = this.switchTagMap;
    this.allSelectTagsOption[4] = this.whitePreservingTag;
    this.allSelectTagsOption[5] = this.translatableAttribute;
    this.allSelectTagsOption[6] = this.internalTag;
	
	this.optionMap = new Object();
	this.optionMap[this.embeddableOption] = lbEmbeddableTags;
    this.optionMap[this.pairedOption] = lbPairedTags;
    this.optionMap[this.unPairedOption] = lbUnpairedTags;
    this.optionMap[this.switchTagMap] = lbSwitchTagMap;
    this.optionMap[this.whitePreservingTag] = lbWhitePreservingTags;
    this.optionMap[this.translatableAttribute] = lbTranslatableAttribute;
    this.optionMap[this.internalTag] = lbInternaTag;
	
	this.tagsEveryPage = 10;
	this.currentPage = 0;
	this.currentOption = "embeddable_tags";
	this.order = "asc";
}

HtmlFilter.prototype.setFilter = function (filter)
{
	this.filter = filter;
}

HtmlFilter.prototype.showDialog = function ()
{
	closeAllFilterDialogs();
	showPopupDialog(this.dialogId);
}

HtmlFilter.prototype.genenrateEmptyTable = function()
{
	this.setPageValue();
	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	str.append("<td width='5%'><input type='checkbox' id='checkAll'/>");
	str.append("</td>");
	str.append("<td width='95%' class='tagName_td'>" + jsTagName);
	str.append("</td>");
	str.append("</tr>");
	return str.toString();
}

HtmlFilter.prototype.generateTotalEmptyTable = function()
{
	var str = new StringBuffer("<table id='tagsContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
	str.append(htmlFilter.genenrateEmptyTable());
	str.append("</table>");
	document.getElementById("tagsContent").innerHTML = str.toString();
}
HtmlFilter.prototype.checkAll = function(tags)
{
	tags = htmlFilter.getTagsString();
	tags = tags.split(",");
	var checkBoxObj = document.getElementById("checkAll");
	checkAllTags = checkBoxObj.checked;
	for(var i = 0; i < tags.length; i++)
	{
		if(document.getElementById("tags_"+i))
		{
			document.getElementById("tags_"+i).checked = checkAllTags;	
		}
		
		if(checkAllTags)
		{
			this.addTagToCheckedTags(tags[i]);
		}
		else
		{
			this.removeTagFromCheckedTags(tags[i]);
		}
	}
}

HtmlFilter.prototype.addTagToCheckedTags = function(tagName)
{
	var checkedTags = htmlFilter.getCheckedTags();
	if(checkedTags.indexOf(","+tagName+",") == -1)
	{
		checkedTags.append(",");
		checkedTags.append(tagName);
		checkedTags.append(",");
	}
}

HtmlFilter.prototype.removeTagFromCheckedTags = function(tagName)
{
	var checkedTags = htmlFilter.getCheckedTags();
	checkedTags.replace(","+tagName+",", ",");
}

function encodingHtml(s)
{
	return s.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/\"/g,"&quot;");	
}

HtmlFilter.prototype.checkthis = function(currentCheckBox)
{
	var tag = currentCheckBox.name;
	
	if (htmlFilter.currentOption == htmlFilter.internalTag)
	{
		tag = encodingHtml(tag);
	}
	
	if(currentCheckBox.checked)
	{
		this.addTagToCheckedTags(tag);
	}
	else
	{
		this.removeTagFromCheckedTags(tag);
	}
}

HtmlFilter.prototype.isCheckAll = function()
{
	var check = "";
	if(checkAllTags)
	{
		check = "checked";
	}
	return check;
}

HtmlFilter.prototype.removeEmptyElements = function(array)
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

HtmlFilter.prototype.sortTable = function()
{
	var tags = htmlFilter.getTagsString();
	tags = tags.split(",");
	tags = htmlFilter.removeEmptyElements(tags);
	tags.sort();
	if(htmlFilter.order == 'asc')
	{
		htmlFilter.order = 'desc';
	}
	else
	{
		htmlFilter.order = 'asc';
		tags.reverse();
	}
	htmlFilter.generateTagsContent(tags, htmlFilter.currentPage);
	htmlFilter.selectTagsMap[htmlFilter.currentOption] = htmlFilter.generateArrayToString(tags);
	htmlFilter.setDefaultTagString();
}

HtmlFilter.prototype.generateArrayToString = function(tagsArray)
{
	var str = new StringBuffer(",");
	for(var i = 0; i < tagsArray.length; i++)
	{
		var tag = tagsArray[i];
		if(tag && "" != tag)
		{
			str.append(","+tag+",");
		}
	}
	return str.toString();
}

HtmlFilter.prototype.generateTableByPage = function(tags, currentPageNum)
{
	tags = htmlFilter.removeEmptyElements(tags);
	if(tags.length == 0)
	{
	   return null;
	}
	
	var title = jsTagName;
	if (htmlFilter.currentOption == htmlFilter.internalTag)
	{
		title = jsInternalTag;
	}
	
	var str = new StringBuffer("<tr class='htmlFilter_emptyTable_tr'>");
	var check = checkAllTags ? "checked" : "";
	str.append("<td width='5%'><input type='checkbox' onclick=\"htmlFilter.checkAll('"+tags+"')\" id='checkAll' "+check+"></input>");
	str.append("</td>");
	str.append("<td width='95%' class='tagName_td'><a href='#' class='tagName_td' onmouseenter=mouseEnter('sort_img') onmouseout=mouseOut('sort_img') onclick='htmlFilter.sortTable()'>" + title + "</a>");
	
	var sortImgSrc = "/globalsight/images/sort-up.gif";
	if(htmlFilter.order == 'asc')
	{
		sortImgSrc = "/globalsight/images/sort-up.gif";
	}
	else
	{
		sortImgSrc = "/globalsight/images/sort-down.gif";
	}
	str.append("<img class='not_display' id='sort_img' src='" + sortImgSrc + "'></img>");
	str.append("</td>");
	str.append("</tr>");
	var startIndex = 0;

	startIndex = htmlFilter.tagsEveryPage * currentPageNum;
	if(startIndex >= tags.length)
	{
		var maxPage = Math.floor(tags.length/htmlFilter.tagsEveryPage + 1);
		alert(maxPageNums + maxPage);
		return maxPage;
	}
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
			if(i >= startIndex && i < startIndex + htmlFilter.tagsEveryPage)
			{
				display='';
			}
			str.append("<tr style='background-color:"+backgroundColor+";display:"+display+"'>");
			var checkedTags = htmlFilter.getCheckedTags();
			if(checkedTags.indexOf(","+tags[i]+",") > -1)
			{
				check = "checked";
			}
			else
			{
				check = "";
			}
			str.append("<td width='5%'><input onclick='htmlFilter.checkthis(this)' type='checkbox' name= '"+tags[i]+"' id='tags_"+i+"' "+check+"></input>");
			str.append("<td width='95%' class='tagValue_td'>"+tags[i]+"</td>");
			str.append("</tr>");
		}
		
	}
	
	return str.toString();
		
}

HtmlFilter.prototype.prePage = function(tags)
{
	tags = htmlFilter.getTagsString();
    tags = htmlFilter.removeEmptyElements(tags.split(","));
    if(tags.length == 0)
    {
       return;
    }
    if(htmlFilter.currentPage > 0)
	{
		htmlFilter.currentPage --;
		htmlFilter.generateTagsContent(tags, htmlFilter.currentPage);
		htmlFilter.setPageValue();
	}
	else
	{
		alert(canNotPrePage);
		return;
	}
}

HtmlFilter.prototype.setPageValue = function()
{
	document.getElementById("pageCountHtml").value = this.currentPage + 1;
	document.getElementById("PageHtmlTotalSize").innerHTML = this.getPageSize();
}

HtmlFilter.prototype.generateTagsContent = function (tags, currentPage)
{
	var str = new StringBuffer("<table id='tagsContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
	str.append(htmlFilter.generateTableByPage(tags, currentPage));
	str.append("</table>");
	document.getElementById("tagsContent").innerHTML = str.toString();
}

HtmlFilter.prototype.getPageSize = function()
{
	var tags = this.getTagsString();
	tags = this.removeEmptyElements(tags.split(","));
	var itemsTotalCount = tags.length;
	var countPerPage = this.tagsEveryPage;
	var pagesize = Math.floor(itemsTotalCount/countPerPage +  (itemsTotalCount%countPerPage == 0 ? 0 : 1));
	pagesize = (pagesize == 0)? 1: pagesize;
	return pagesize;
}

HtmlFilter.prototype.nextPage = function(tags)
{
	tags = htmlFilter.getTagsString();
	tags = htmlFilter.removeEmptyElements(tags.split(","));
	if(tags.length == 0)
	{
	   return;
	}
	if(htmlFilter.currentPage < Math.floor(tags.length/htmlFilter.tagsEveryPage + 1))
	{
		htmlFilter.currentPage ++;
		var tableContent = htmlFilter.generateTableByPage(tags,htmlFilter.currentPage);
		var validate = new Validate();
		if(!validate.isNumber(tableContent))
		{
			var str = new StringBuffer("<table id='tagsContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
			str.append(tableContent);
			str.append("</table>");
			document.getElementById("tagsContent").innerHTML = str.toString();
			htmlFilter.setPageValue();
		}
		else
		{
			htmlFilter.currentPage --;
			htmlFilter.setPageValue();
		}		

	}
	else
	{
		alert(canNotNextPage);
		return;
	}
}

HtmlFilter.prototype.goToPage = function(tags)
{
	tags = htmlFilter.getTagsString();
	tags = htmlFilter.removeEmptyElements(tags.split(","));
	if(tags.length == 0)
	{
		htmlFilter.generateTotalEmptyTable();
		alert(noTagsForThisType + htmlFilter.getTagInnerText());
		return;
	}
	var pageValue = document.getElementById("pageCountHtml").value;
	var validate = new Validate();
	if(! validate.isNumber(pageValue))
	{
		alert(pageValueIsNotNumber);
		return;
	}
	pageValue = pageValue - 0;
	if(pageValue < 1 || pageValue > Math.floor(tags.length/htmlFilter.tagsEveryPage + 1))
	{
		alert(invalidatePageValue.replace("%s", Math.floor(tags.length/officeDocFilter.tagsEveryPage + 1)));
		return;
	}

	htmlFilter.currentPage = pageValue - 1;
	var str = new StringBuffer("<table id='tagsContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
	str.append(htmlFilter.generateTableByPage(tags, htmlFilter.currentPage));
	str.append("</table>");
	document.getElementById("tagsContent").innerHTML = str.toString();

}

HtmlFilter.prototype.getTagsStringByOption = function(currentOption)
{
    return htmlFilter.selectTagsMap[currentOption];
}

HtmlFilter.prototype.getTagsString = function()
{
	return htmlFilter.selectTagsMap[htmlFilter.currentOption];
}

HtmlFilter.prototype.getCheckedTags = function()
{
	return checkedMap[htmlFilter.currentOption];
}

HtmlFilter.prototype.switchTags = function(tagsSelectBox)
{
	htmlFilter.currentOption = tagsSelectBox.options[tagsSelectBox.selectedIndex].value;
	var tagsString = htmlFilter.selectTagsMap[htmlFilter.currentOption];
	var tags = htmlFilter.removeEmptyElements(tagsString.split(","));
		
	htmlFilter.currentPage = 0;

	if(tags.length != 0)
	{
		htmlFilter.generateTagsContent(tags, htmlFilter.currentPage);
	}
	else
	{
		htmlFilter.generateTotalEmptyTable();
	}
	
	htmlFilter.setPageValue();
	checkAllTags = false;
	document.getElementById("checkAll").checked = false;
}

HtmlFilter.prototype.setDefaultTagString = function()
{
	htmlFilter.defaultEmbeddableTags = htmlFilter.selectTagsMap[htmlFilter.embeddableOption];
	htmlFilter.defaultPairedTags = htmlFilter.selectTagsMap[htmlFilter.pairedOption];
	htmlFilter.defaultUnPairedTags = htmlFilter.selectTagsMap[htmlFilter.unPairedOption];
	htmlFilter.defaultSwitchTagMaps = htmlFilter.selectTagsMap[htmlFilter.switchTagMap];
	htmlFilter.defaultWhitePreservingTags = htmlFilter.selectTagsMap[htmlFilter.whitePreservingTag];
	htmlFilter.defaultTranslatableAttributes = htmlFilter.selectTagsMap[htmlFilter.translatableAttribute];
	htmlFilter.defaultInternalTag = htmlFilter.selectTagsMap[htmlFilter.internalTag];
}

function validateHtmlInternalTagCallback(data)
{
	var result = eval(data);
	var error = result.error;
	if (error != "")
	{
		alert(error);
		return;
	}
	
	document.getElementById("InternalTagToAdd").value = "";
	var returnTag = result.tag;
	
	var tagString = new StringBuffer("," + htmlFilter.getTagsString() + ",");
	if(htmlFilter.isTagExist(tagString, returnTag))
	{
		alert(existTagName);
		return;
	}
	
	htmlFilter.selectTagsMap[htmlFilter.currentOption] += "," + returnTag + ",";
	htmlFilter.setDefaultTagString();
	htmlFilter.generateTagsContent(htmlFilter.getTagsString().split(","), htmlFilter.currentPage);
	closePopupDialog('addInternalTagDialog');
	htmlFilter.showTranslateRuleSelectBox('');
}

HtmlFilter.prototype.addInternalTag = function()
{
	var validate = new Validate();
	var tagName = new StringBuffer(document.getElementById("InternalTagToAdd").value);
	var obj = {
			filterId : saveHtmlFilter.filterId,
			internalTag : tagName
		}
	sendAjax(obj, "validateHtmlInternalTag", "validateHtmlInternalTagCallback");	
}
	
HtmlFilter.prototype.addSingleTag = function()
{

	var validate = new Validate();
	var tagName = new StringBuffer(document.getElementById("singleTagNameToAdd").value);
	if(validate.containsWhitespace(tagName))
	{
		alert(jsTagName + " (" + tagName + ")"+ canNotContainWhiteSpace);
		return;
	}
	if(validate.isEmptyStr(tagName.trim()))
	{
		alert(emptyTagName);
		return;
	}
	
	if(validate.containSpecialChar(tagName.trim()))
	{
	    alert(invalidTagNameChar + invalidChars);
	    return;
	}
	var tagString = new StringBuffer("," + htmlFilter.getTagsString() + ",");
	if(htmlFilter.isTagExist(tagString, tagName.trim()))
	{
		alert(existTagName);
		return;
	}
	htmlFilter.selectTagsMap[htmlFilter.currentOption] += "," + tagName.trim().toString() + ",";
	htmlFilter.setDefaultTagString();
	htmlFilter.generateTagsContent(htmlFilter.getTagsString().split(","), htmlFilter.currentPage);
	closePopupDialog('addSingleTagDialog');
	htmlFilter.showTranslateRuleSelectBox('');
}

HtmlFilter.prototype.addSingleAttribute = function()
{
	var validate = new Validate();
	var tagName = new StringBuffer(document.getElementById("singleAttributeNameToAdd").value);

	if(validate.isEmptyStr(tagName.trim()))
	{
		alert(emptyTagName);
		return;
	}
	
	if(validate.containSpecialChar(tagName.trim()))
	{
	    alert(invalidTagNameChar + invalidChars);
	    return;
	}
	
	var tagString = new StringBuffer("," + htmlFilter.getTagsString() + ",");
	if(htmlFilter.isTagExist(tagString, tagName.trim()))
	{
		alert(existTagName);
		return;
	}
	
	htmlFilter.selectTagsMap[htmlFilter.currentOption] += "," + tagName.trim().toString() + ",";
	htmlFilter.setDefaultTagString();
	htmlFilter.generateTagsContent(htmlFilter.getTagsString().split(","), htmlFilter.currentPage);
	closePopupDialog('addSingleAttributeDialog');
	htmlFilter.showTranslateRuleSelectBox('');
}

HtmlFilter.prototype.addMapTag = function()
{
	var validate = new Validate();
	var tagKey = new StringBuffer(document.getElementById("tagKeyToAdd").value).trim();
	if(validate.isEmptyStr(tagKey))
	{
		alert(emptyTagKey);
		return;
	}
	if(validate.containsWhitespace(tagKey))
	{
		alert(jsTagKey + " (" + tagKey + ")"+ canNotContainWhiteSpace);
		return;
	}
	if(validate.containSpecialChar(tagKey))
    {
        alert(invalideTagKeyChar + invalidChars);
        return;
    }
	
	var tagValue = new StringBuffer(document.getElementById("tagValueToAdd").value).trim();
	if(validate.isEmptyStr(tagValue))
	{
		alert(emptyTagValue);
		return;
	}
	if(validate.containsWhitespace(tagValue))
	{
		alert(jsTagValue + " (" + tagValue + ")"+ canNotContainWhiteSpace);
		return;
	}
    if(validate.containSpecialChar(tagValue))
    {
        alert(invalideTagValueChar + invalidChars);
        return;
    }
	var tagName = new StringBuffer(tagKey);
	tagName.append(":");
	tagName.append(tagValue.toString());
	var tagString = new StringBuffer("," + htmlFilter.getTagsString() + ",");
	
	if(htmlFilter.isTagExist(tagString, tagName))
	{
		alert(existTagName);
		return;
	}
	htmlFilter.selectTagsMap[htmlFilter.currentOption] += "," + tagName.toString() + ",";
	htmlFilter.setDefaultTagString();
	htmlFilter.generateTagsContent(htmlFilter.getTagsString().split(","), htmlFilter.currentPage);
	closePopupDialog('addMapTagDialog');
	htmlFilter.showTranslateRuleSelectBox('');
}

HtmlFilter.prototype.isTagExist = function(tags, tag)
{
	var tagStringL = tags.toString().toLowerCase();
	var tagNameL = tag.toString().toLowerCase();
	if(tagStringL.indexOf("," + tagNameL + ",") != -1 )
		return true;
	else
		return false;
}

HtmlFilter.prototype.disableTranslateRuleSelectBox = function()
{
    var htmlFilterTranslateRuleContent = document.getElementById("htmlFilterTranslateRuleContent");
    var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
    str.append(jsChoose + ":");
    str.append("</label>"); 
    str.append("<input type='text' value='"+htmlFilter.optionMap[htmlFilter.currentOption]+"' disabled></input>");
    htmlFilterTranslateRuleContent.innerHTML = str.toString();    
}

HtmlFilter.prototype.showTranslateRuleSelectBox = function(isDisabled)
{
    var htmlFilterTranslateRuleContent = document.getElementById("htmlFilterTranslateRuleContent");
    var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
    str.append(jsChoose + ":");
    str.append("</label>"); 

    str.append("<select id='htmlTranslateRule' onchange='htmlFilter.switchTags(this)' "+isDisabled+">");
    for(var i = 0; i < htmlFilter.allSelectTagsOption.length; i++)
    {
        var optionObj = htmlFilter.allSelectTagsOption[i];
        if(htmlFilter.currentOption == optionObj)
        {
            str.append("<option value='"+optionObj+"' selected>" + htmlFilter.optionMap[optionObj]+"</option>")
        }
        else
        {
            str.append("<option value='"+optionObj+"'>" + htmlFilter.optionMap[optionObj]+"</option>");
        }
    }
    str.append("</select>");
    htmlFilterTranslateRuleContent.innerHTML = str.toString();    
}

HtmlFilter.prototype.addTag = function()
{
	if(htmlFilter.currentOption == htmlFilter.switchTagMap)
	{
		showPopupDialog("addMapTagDialog");
	}
	else if (htmlFilter.currentOption == htmlFilter.internalTag)
	{
		showPopupDialog("addInternalTagDialog");
	}
	else
	{
		showPopupDialog("addSingleTagDialog");
	}
	document.getElementById("singleTagNameToAdd").value = "";
    document.getElementById("tagKeyToAdd").value = "";
    document.getElementById("tagValueToAdd").value = "";
    var version = getIEVersion();
    if(version == 6)
    {
        htmlFilter.disableTranslateRuleSelectBox();
        document.getElementById("div_button_add_single_tag").style.marginLeft = 50;
        document.getElementById("div_button_add_tag").style.marginLeft = 50;
    }
    else
    {
        htmlFilter.showTranslateRuleSelectBox("disabled");
    }
}

HtmlFilter.prototype.getArrayLength = function(array)
{
	var length = 0; 
	for(var i = 0; i < array.length; i++)
	{
		if(array[i] && "" != array[i])
		{
			length ++;
		}
	}
	return length;
}

HtmlFilter.prototype.buildTagsString = function(array)
{
	for(var i = 0; i < array.length; i++)
	{
		var a = array[i];
		if(a && a.checked)
		{
			var tagType = a.getAttribute("tagType");
			var tagValue = a.getAttribute("tagValue");
			
			if (tagType == htmlFilter.internalTag)
			{
				tagValue = encodingHtml(tagValue);
			}
			
			htmlFilter.selectTagsMap[tagType] = 
				(","+htmlFilter.selectTagsMap[tagType]+",").replace("," + tagValue + ",", ",");
			htmlFilter.setDefaultTagString();
			checkedMap[tagType] = new StringBuffer((","+checkedMap[tagType]+",").replace("," + tagValue + ",", ","));
			var tags = htmlFilter.removeEmptyElements(htmlFilter.getTagsString().split(","));
			var tagLength = htmlFilter.getArrayLength(tags);
			tagLength = (tagLength == 0) ? 1 : tagLength;
			var currentMaxPageNum = Math.floor((tagLength - 1) / htmlFilter.tagsEveryPage);
			if(htmlFilter.currentPage > currentMaxPageNum)
			{
				htmlFilter.currentPage = currentMaxPageNum;
			}
			
			htmlFilter.setPageValue();
			if(tags.length == 0)
			{
				htmlFilter.generateTotalEmptyTable();
			}
			else
			{
				htmlFilter.generateTagsContent(tags, htmlFilter.currentPage);
			}
			
		}
	}
}

HtmlFilter.prototype.deleteTags = function()
{
	var embeddableTagsToDelete = document.getElementsByName(htmlFilter.embeddableOption);
	var pairedTagsToDelete = document.getElementsByName(htmlFilter.pairedOption);
	var unPairedTagsToDelete = document.getElementsByName(htmlFilter.unPairedOption);
	var switchTagMapsToDelete = document.getElementsByName(htmlFilter.switchTagMap);
	var whitePreservingTagsToDelete = document.getElementsByName(htmlFilter.whitePreservingTag);
	var translatableAttributesTagsToDelete = document.getElementsByName(htmlFilter.translatableAttribute);
	var internalTagToDelete = document.getElementsByName(htmlFilter.internalTag);
	
	htmlFilter.buildTagsString(embeddableTagsToDelete);
	htmlFilter.buildTagsString(pairedTagsToDelete);
	htmlFilter.buildTagsString(unPairedTagsToDelete);
	htmlFilter.buildTagsString(switchTagMapsToDelete);
	htmlFilter.buildTagsString(whitePreservingTagsToDelete);
	htmlFilter.buildTagsString(translatableAttributesTagsToDelete);
	htmlFilter.buildTagsString(internalTagToDelete);
	
	checkedEmbeddableTags = checkedMap["embeddable_tags"];
    checkedPairedTags = checkedMap["paired_tags"];
    checkedUnPairedTags = checkedMap["unpairedTag_tags"];
    checkedSwitchTagMaps = checkedMap["switch_tag_map"];
    checkedWhitePreservingTags = checkedMap["white_preserving_tag"];
    checkedTranslatableAttributes = checkedMap["translatable_attribute"];
    checkedInternalTag = checkedMap["internal_tag"];
	
	closePopupDialog('deleteTagDialog');
}

HtmlFilter.prototype.clearAllTags = function()
{
	var tagTypeSelectBox = document.getElementById("htmlTranslateRule");
	for(var i = 0; i < tagTypeSelectBox.options.length; i++)
	{
		var optionValue = tagTypeSelectBox.options[i].value;
		document.getElementById(optionValue).style.backgroundColor = "white";
	}
}

HtmlFilter.prototype.showCurrentDeleteTags = function(tabObj)
{
	htmlFilter.clearAllTags();
	tabObj.style.backgroundColor = "#738EB5";
	var str = new StringBuffer("");
	var checkedTags = checkedMap[tabObj.id];
	var checkedTagsArray = checkedTags.toString().split(",");
	for(var i = 0; i < checkedTagsArray.length; i++)
	{
		var checkedTag = checkedTagsArray[i];
		if(checkedTag && checkedTag != "")
		{
			str.append("<input type='checkbox' name='"+tabObj.id+"' tagType='" + tabObj.id + "' tagValue = '"+checkedTag+"' checked>");
			str.append(checkedTag);
			str.append("</input>");
		}
	}
	document.getElementById("deleteTagContent_div").innerHTML = str.toString();	
}

HtmlFilter.prototype.isNotEmptyArray = function (array)
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

HtmlFilter.prototype.showAllTagsToDelete = function(tagsToDelete, moreInfoObj)
{
	var showDivId = moreInfoObj.getAttribute("tagType");
	tagsToDelete = htmlFilter.removeEmptyElements(tagsToDelete.split(","));
	var str = new StringBuffer("");
	for(var i = 0; i < tagsToDelete.length; i++)
	{
		var checkedTag = tagsToDelete[i];
		if(checkedTag && checkedTag != "")
		{
			str.append("<input type='checkbox' name='"+showDivId+"' tagType='" + showDivId + "' tagValue = '"+checkedTag+"' checked>");
			str.append(checkedTag);
			str.append("</input>");
		}
	}
	var showDivObj = document.getElementById("showDiv");
	showDivObj.style.top =  "200px";
	showDivObj.style.left = "300px";
	showDivObj.innerHTML = str.toString();
	alert(showDivObj.style.top)
	if(showDivObj.style.display == "block")
	{
		showDivObj.style.display == "none";
	}
	else
	{
		showDivObj.style.display == "block";
	}
}

//for gbs-2599
HtmlFilter.prototype.selectAll_HtmlFilter = function()
{
	var selectAll = document.getElementById("selectAll_HtmlFilter")
	if(selectAll.checked) {
		this.checkAllTagsToDelete();
	} else {
		this.clearAllTagsToDelete();
	}
}

HtmlFilter.prototype.generateDeleteTagTableContent = function()
{
/**
	var str = new StringBuffer("");
	var tagTypes = document.getElementById("htmlTranslateRule");
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		str.append("<div class='div_show' id='"+value+"' onclick='htmlFilter.showCurrentDeleteTags(this)'><a href='#' class='specialFilter_dialog_label'>");
		str.append(text);
		str.append("</a></div>");
	}
	document.getElementById("deleteTagContent").innerHTML = str.toString();
	*/
	var str = new StringBuffer("<center><table cellpadding=0 cellspacing=0 border=0 width='540px' class='standardText'>");
	str.append("<tr class='deleteTagsDialog_header'>");
	str.append("<td width='108px'>");
	str.append("<Label class='tagName_td'>" + jsTagType + "</Label>");
	str.append("</td>");
	str.append("<td width='400px'>");
	str.append("<input type='checkbox' checked='true' id='selectAll_HtmlFilter' onclick='htmlFilter.selectAll_HtmlFilter()'/>");//for gbs-2599
	str.append("<Label class='tagName_td'>" + jsTagsToDeleted + "</Label>");
	str.append("</td>");
	str.append("<td width='22px'>");
	str.append("<Label class='tagName_td'>" + jsTagsCount + "</Label>");
	str.append("</td>");
	str.append("</tr>");	
	var tagTypes = document.getElementById("htmlTranslateRule");
	
	var isOdd = true;
	var count = 0;
	var sum = 0;
	for(var i = 0; i < tagTypes.options.length; i++)
	{
		count = 0;
		var tagType = tagTypes.options[i];
		var value = tagType.value;
		var text = tagType.innerHTML;
		var checkedTags = checkedMap[value];
		var checkedTagsArray = checkedTags.toString().split(",");
		
		if(htmlFilter.isNotEmptyArray(checkedTagsArray))
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
				if(checkedTag && checkedTag != "" && htmlFilter.isCheckedTagInDefaultArray(value, checkedTag))
				{
					str.append("<input type='checkbox' name='"+value+"' tagType='" + value + "' tagValue = '"+checkedTag+"' checked>");
					str.append(checkedTag);
					str.append("</input>");
					count ++;
				}
				/**
				if(count == 8)
				{
					str.append("<br/><span tagType='" + value + "' onclick='htmlFilter.showAllTagsToDelete(\""+checkedTagsArray+"\", this)' class='deleteTagsMoreInfo'>[more]</span>");
					break;
				}
				*/
			}
			sum += count;
//			str.append("<div style='border:1px #0c1476 solid' id='showDiv_" + value + "'>");
//			str.append("</div>")
			str.append("</td>");
			str.append("<td width='22px'>");
			str.append(count);
			str.append("</td>");
			str.append("</tr>");	
		}
	} 
	str.append("</table></center>");
	/* for gbs-2599
	str.append("<a href='#' class='specialfilter_a' onclick='htmlFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='htmlFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
	if(sum <= 0)
	{
		alert(noTagsToChoose);
		return false;
	}
	
	document.getElementById("deleteTagsDialogLable").innerHTML = jsRemoveTagsNote.replace("%s", "<span style='color:red;font-size:10pt'><b>"+sum+"</b></span>");
	document.getElementById("deleteTagTableContent").innerHTML = str.toString();
	return true;
}

HtmlFilter.prototype.isCheckOrClearAll = function( /*boolean*/checkOrClear )
{
    for(var i = 0; i < htmlFilter.allSelectTagsOption.length; i++)
    {
        var selectTagsOption = htmlFilter.allSelectTagsOption[i];
        var checkBoxObjs = document.getElementsByName(selectTagsOption);
        for(var j = 0; j < checkBoxObjs.length; j++)
        {
            checkBoxObjs[j].checked = checkOrClear;
        }
    }
}

HtmlFilter.prototype.checkAllTagsToDelete = function ()
{
    htmlFilter.isCheckOrClearAll(true);
}

HtmlFilter.prototype.clearAllTagsToDelete = function()
{
    htmlFilter.isCheckOrClearAll(false);
}

HtmlFilter.prototype.isCheckedTagInDefaultArray = function(value, checkedTag)
{
    var tagsStringByOption = htmlFilter.getTagsStringByOption(value);
    return (","+tagsStringByOption+",").indexOf(","+checkedTag+",") != -1;
}

HtmlFilter.prototype.deleteTag = function()
{
	var hasTagsToDelete = htmlFilter.generateDeleteTagTableContent();
	if(hasTagsToDelete)
	{
		showPopupDialog("deleteTagDialog");
	}
}

HtmlFilter.prototype.generateDiv = function(topFilterId, color)
{
	htmlFilter = new HtmlFilter();
	initialCheckedTagsForHtmlFilter();
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' class='filterName_big_dialog' id='htmlFilterName' value='HTML Filter'></input>");
	str.append("<br/>");
	
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>"); 
	
	str.append("<textarea rows='3' style='width:340px' id='htmlFilterDesc' name='desc'></textarea>");
	str.append("<br/>");
	
	str.append("<table border=0 width='418px'>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsConvertHTMLEntity + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='convertHtmlEntry' type='checkbox' name='convertHtmlEntry'></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsIgnoreInvalidHTMLTags + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='ignoreInvalideHtmlTags' type='checkbox' name='ignoreInvalideHtmlTags' checked></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsAddRtlDirectionality + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='addRtlDirectionality' type='checkbox' name='addRtlDirectionality'></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsWhitespaceHandling + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateWhitespaceHandling() + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsLocalizeFunction + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='localizeFunction' type='text' name='localizeFunction'></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>" + generateBaseFilterList(this.filterTableName) + "</td>");
	str.append("</tr>");
	
	/**
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append("PlaceHolder Trimming");
	str.append("</td>");
	str.append("<td  class='htmlFilter_right_td'>");
	str.append("<select id='htmlPlaceHolderTrim'>");
	str.append("<option value='donottrim'>Do not trim</option>");
	str.append("<option value='embeddable_tags'>Embeddable Tags</option>");
	str.append("</select>");
	str.append("</td>");
	str.append("</tr>");
	*/
	
	str.append("</table>");
	
//	str.append("<div style='width:408px'>");
	str.append("<table cellpadding=0 cellspacing=0 border=0 width='408px'><tr><td>");
	str.append("<div id='htmlFilterTranslateRuleContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='htmlTranslateRule' onchange='htmlFilter.switchTags(this)'>");
	str.append("<option value='embeddable_tags'>" + this.optionMap["embeddable_tags"] + "</option>");
	str.append("<option value='internal_tag'>" + this.optionMap["internal_tag"] + "</option>");
	str.append("<option value='paired_tags'>" + this.optionMap["paired_tags"] + "</option>");
	str.append("<option value='switch_tag_map'>" + this.optionMap["switch_tag_map"] + "</option>");
	str.append("<option value='translatable_attribute'>" + this.optionMap["translatable_attribute"] + "</option>");
	str.append("<option value='unpairedTag_tags'>" + this.optionMap["unpairedTag_tags"] + "</option>");
	str.append("<option value='white_preserving_tag'>" + this.optionMap["white_preserving_tag"] + "</option>");
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "' onclick='htmlFilter.addTag()'></input>");
	str.append("<input type='button' value='" + jsDelete + "' onclick='htmlFilter.deleteTag()'></input>");
	str.append("</div>");
	str.append("</td></tr>");
//	str.append("</div>");
	str.append("<td><tr>");
	str.append("</td></tr></table>");
	var embeddableTags = htmlFilter.defaultEmbeddableTags.split(",");
	str.append("<div id='tagsContent'>")
	str.append("<table id='tagsContentTable' cellpadding=0 cellspacing=0 border=0 width='408px' class='standardText'>");
	var tableContent = htmlFilter.generateTableByPage(embeddableTags, 0);

	if(tableContent){
		str.append(tableContent);
	}
	else
	{
		str.append(htmlFilter.genenrateEmptyTable());
	}
	str.append("</table>");
	str.append("</div>");
	

	str.append("<div>");
	var tags = new Array();
	if(htmlFilter.currentOption == 'embeddable_tags')
	{
		tags = embeddableTags;
	}
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=htmlFilter.prePage('"+tags+"')>" + jsPrevious+ "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=htmlFilter.nextPage('"+tags+"')>" + jsNext + "</a>|");
	str.append("<input id='pageCountHtml' size=3 type='text' value="+(htmlFilter.currentPage+1)+" ></input>");
	str.append(" / <span id='PageHtmlTotalSize' class='standardText'>" + htmlFilter.getPageSize() + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=htmlFilter.goToPage('"+tags+"')></input>");
	str.append("</div>");
	var dialogObj = document.getElementById('htmlFilterPopupContent');
	dialogObj.innerHTML = str.toString();
	htmlFilter.showDialog();
	htmlFilter.checkHtmlTranslateRule();

	saveHtmlFilter.edit = false;
	saveHtmlFilter.topFilterId = topFilterId;
	saveHtmlFilter.color = color;
	saveHtmlFilter.htmlFilter = htmlFilter;
}

HtmlFilter.prototype.generateWhitespaceHandling = function (filter)
{
	var str = new StringBuffer("");
	str.append("<nobr><input value='1' type='radio' name='wsHandleModeHTML'" + ((filter) ? ((filter && !filter.whitespacePreserve) ? " checked" : "") : " checked") + ">" 
			+ fontTagS + "Collapse" +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='2' type='radio' name='wsHandleModeHTML'" + ((filter) ? (( filter.whitespacePreserve) ? " checked" : "") : "") + ">" 
			+ fontTagS + jsWsHandlingPreserve +fontTagE);
	return str.toString();
}

HtmlFilter.prototype.init = function()
{
	this.currentOption = "embeddable_tags";
	this.currentPage = 0;
}

HtmlFilter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	htmlFilter.init();
	//Fix for GBS-1599 
	htmlFilter.defaultEmbeddableTags = this.filter.defaultEmbeddableTags;
	htmlFilter.defaultPairedTags = this.filter.defaultPairedTags;
	htmlFilter.defaultUnPairedTags = this.filter.defaultUnpairedTags;
	htmlFilter.defaultSwitchTagMaps = this.filter.defaultSwitchTagMaps;
	htmlFilter.defaultWhitePreservingTags = this.filter.defaultWhitePreservingTags;
	htmlFilter.defaultTranslatableAttributes = this.filter.defaultTranslatableAttributes;
	htmlFilter.defaultTranslatableAttributes = this.filter.defaultTranslatableAttributes;
	htmlFilter.defaultInternalTag = this.filter.defaultInternalTag;
	
	htmlFilter.selectTagsMap[htmlFilter.embeddableOption] = htmlFilter.defaultEmbeddableTags;
	htmlFilter.selectTagsMap[htmlFilter.pairedOption] = htmlFilter.defaultPairedTags;
	htmlFilter.selectTagsMap[htmlFilter.unPairedOption] = htmlFilter.defaultUnPairedTags;
	htmlFilter.selectTagsMap[htmlFilter.switchTagMap] = htmlFilter.defaultSwitchTagMaps;
	htmlFilter.selectTagsMap[htmlFilter.whitePreservingTag] = htmlFilter.defaultWhitePreservingTags;
	htmlFilter.selectTagsMap[htmlFilter.translatableAttribute] = htmlFilter.defaultTranslatableAttributes;
	htmlFilter.selectTagsMap[htmlFilter.internalTag] = this.filter.defaultInternalTag;
	
	checkedEmbeddableTags = new StringBuffer("," + this.filter.embeddableTags +",");
	checkedPairedTags = new StringBuffer("," + this.filter.pairedTags +",");
	checkedUnPairedTags = new StringBuffer("," + this.filter.unpairedTags +",");
	checkedSwitchTagMaps = new StringBuffer("," + this.filter.switchTagMaps +",");
	checkedWhitePreservingTags = new StringBuffer("," + this.filter.whitePreservingTags +",");
	checkedTranslatableAttributes = new StringBuffer("," + this.filter.translatableAttributes +",");
	checkedInternalTag = new StringBuffer("," + this.filter.internalTag +",");
	 
	checkedMap["embeddable_tags"] = checkedEmbeddableTags;
	checkedMap["paired_tags"] = checkedPairedTags;
	checkedMap["unpairedTag_tags"] = checkedUnPairedTags;
	checkedMap["switch_tag_map"] = checkedSwitchTagMaps;
	checkedMap["white_preserving_tag"] = checkedWhitePreservingTags;
	checkedMap["translatable_attribute"] = checkedTranslatableAttributes;
	checkedMap["internal_tag"] = checkedInternalTag;
		 
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' maxlength='"+maxFilterNameLength+"' style='width:345px' id='htmlFilterName' value='"+this.filter.filterName+"'></input>");
	str.append("<br>");
	
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>"); 
	
	str.append("<textarea rows='3' style='width:345px' id='htmlFilterDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br>");
	
	var isCheckHtmlEntry = (this.filter.convertHtmlEntry) ? "checked":"";
	var isCheckIgnoreInvalideHtmlTags = (this.filter.ignoreInvalideHtmlTags) ? "checked":"";
	var isCheckAddRtlDirectionality = (this.filter.addRtlDirectionality) ? "checked":"";
	str.append("<table border=0 width='425px'>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsConvertHTMLEntity + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='convertHtmlEntry' type='checkbox' name='convertHtmlEntry' "+isCheckHtmlEntry+"></input></td>");
	str.append("<td width='1px' class='htmlFilter_split_tr'>&nbsp;</td>");
	str.append("</tr>");
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsIgnoreInvalidHTMLTags + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='ignoreInvalideHtmlTags' type='checkbox' name='ignoreInvalideHtmlTags' "+isCheckIgnoreInvalideHtmlTags+"></input></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsAddRtlDirectionality + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='addRtlDirectionality' type='checkbox' name='addRtlDirectionality' "+isCheckAddRtlDirectionality+"></input></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsWhitespaceHandling + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateWhitespaceHandling(this.filter) + "</td>");
	str.append("</tr>");

	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsLocalizeFunction + "</td>");
	str.append("<td class='htmlFilter_right_td'><input id='localizeFunction' type='text' name='localizeFunction' value='"+this.filter.jsFunctionText+"'></input></td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' nowrap>");
	str.append(jsInternalTextPostFilter);
	str.append(":</td>");
	str.append("<td class='htmlFilter_right_td'>" + generateBaseFilterList(this.filterTableName, this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("</table>");
	str.append("<table border=0 width='417px'><tr><td>");
	//str.append("<div style='width:408px'>");
	str.append("<div id='htmlFilterTranslateRuleContent' style='float:left'>")
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsChoose + ":");
	str.append("</label>"); 
	str.append("<select id='htmlTranslateRule' onchange='htmlFilter.switchTags(this)'>");
	str.append("<option value='embeddable_tags'>" + this.optionMap["embeddable_tags"] + "</option>");
	str.append("<option value='internal_tag'>" + this.optionMap["internal_tag"] + "</option>");
	str.append("<option value='paired_tags'>" + this.optionMap["paired_tags"] + "</option>");
	str.append("<option value='switch_tag_map'>" + this.optionMap["switch_tag_map"] + "</option>");
	str.append("<option value='translatable_attribute'>" + this.optionMap["translatable_attribute"] + "</option>");
	str.append("<option value='unpairedTag_tags'>" + this.optionMap["unpairedTag_tags"] + "</option>");
	str.append("<option value='white_preserving_tag'>" + this.optionMap["white_preserving_tag"] + "</option>");
	str.append("</select>");
	str.append("</div>");
	
	str.append("<div style='float:right'>")
	str.append("<input type='button' value='" + jsAdd + "' onclick='htmlFilter.addTag()'></input>");
	str.append("<input type='button' value='" + jsDelete + "' onclick='htmlFilter.deleteTag()'></input>");
	str.append("</div>");
	//str.append("</div>");
	str.append("</td></tr>");
	str.append("<tr><td>");
	//Added for edit style
	var tags = htmlFilter.selectTagsMap[htmlFilter.currentOption].split(",");
	str.append("<span id='tagsContent'>")
	str.append("<table id='tagsContentTable' cellpadding=0 cellspacing=0 border=0 width='100%' class='standardText'>");
	var tableContent = htmlFilter.generateTableByPage(tags, 0);
	if(tableContent){
		str.append(tableContent);
	}
	else
	{
		str.append(htmlFilter.genenrateEmptyTable());
	}
	str.append("</table>");
	str.append("</span>");
	str.append("</td></tr></table>");
	str.append("<div>");
	var tags = new Array();
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=htmlFilter.prePage('"+tags+"')>" + jsPrevious + "</a>|");
	str.append("<a href='#' class='specialFilter_dialog_label' onclick=htmlFilter.nextPage('"+tags+"')>" + jsNext + "</a>|");
	str.append("<input id='pageCountHtml' size=3 type='text' value="+(htmlFilter.currentPage+1)+" ></input>");
	str.append(" / <span id='PageHtmlTotalSize' class='standardText'>" + htmlFilter.getPageSize() + " </span>");
	str.append("<input type='button' value='" + jsGo + "' onclick=htmlFilter.goToPage('"+tags+"')></input>");
	str.append("</div>");
	
	var dialogObj = document.getElementById('htmlFilterPopupContent');
	dialogObj.innerHTML = str.toString();	
	htmlFilter.showDialog();
	htmlFilter.setPageValue();
	htmlFilter.checkHtmlTranslateRule();
	
	saveHtmlFilter.edit = true;
	saveHtmlFilter.filterId = filterId;
	saveHtmlFilter.color = color;
	saveHtmlFilter.filter = this.filter;
	saveHtmlFilter.specialFilters = specialFilters;
	saveHtmlFilter.topFilterId = topFilterId;
}

HtmlFilter.prototype.checkHtmlTranslateRule = function()
{
	var htmlTranslateRule = document.getElementById("htmlTranslateRule");
	for(var i = 0; i < htmlTranslateRule.options.length; i++)
	{
		var option = htmlTranslateRule.options[i];
		if(option.value == htmlFilter.currentOption)
		{
			option.selected = true;
		}
	}
}

HtmlFilter.prototype.getTagInnerText = function()
{
	var htmlTranslateRule = document.getElementById("htmlTranslateRule");
	for(var i = 0; i < htmlTranslateRule.options.length; i++)
	{
		var option = htmlTranslateRule.options[i];
		if(option.value == htmlFilter.currentOption)
		{
			return option.innerHTML;
		}
	}
}

function checkAllTagsIsEmpty(obj, validate)
{
	var str = new StringBuffer(obj.embeddableTags);
	str.append(obj.pairedTags);
	str.append(obj.unpairedTags);
	str.append(obj.switchTagMaps);
	str.append(obj.whitePreservingTags);
	str.append(obj.translatableAttributes);
	str.append(obj.internalTag);
	var array = str.toString().split(",");
	if(validate.isEmptyArray(array))
	{
		return false;
	}
	return true;
}
function saveHtmlFilter()
{
	var validate = new Validate();
	var filterName = document.getElementById("htmlFilterName").value;
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
	var isNew = (saveHtmlFilter.edit) ? "false" : "true";
	var filterId = saveHtmlFilter.filterId;
	var filterDesc = document.getElementById("htmlFilterDesc").value;
	var convertHtmlEntry = document.getElementById("convertHtmlEntry").checked;
	var ignoreInvalideHtmlTags = document.getElementById("ignoreInvalideHtmlTags").checked;
	var addRtlDirectionality = document.getElementById("addRtlDirectionality").checked;
//	var extractCharset = document.getElementById("extractCharset").checked;
	var localizeFunction = document.getElementById("localizeFunction").value;
	var baseFilterId = document.getElementById("html_filter_baseFilterSelect").value;
	
	var wsHandleModeHtml = getRadioValue(fpForm.wsHandleModeHTML);
	var whitespacePreserve = (wsHandleModeHtml == 2);
	
	// do not add this alert as there is no secondary or post filter in html filter
	//alertUserBaseFilter(baseFilterId);
	
	var obj = {
		isNew : isNew,
		filterId : filterId,
		filterTableName : "html_filter",
		filterName : filterName,
		filterDesc : filterDesc,
		convertHtmlEntry : convertHtmlEntry,
		ignoreInvalideHtmlTags : ignoreInvalideHtmlTags,
		addRtlDirectionality : addRtlDirectionality,
		whitespacePreserve : whitespacePreserve,
//		extractCharset : extractCharset,
		localizeFunction : localizeFunction,
		defaultEmbeddableTags : htmlFilter.defaultEmbeddableTags,
		embeddableTags : checkedEmbeddableTags.toString(),
		defaultPairedTags : htmlFilter.defaultPairedTags,
		pairedTags : checkedPairedTags.toString(),
		defaultUnPairedTags : htmlFilter.defaultUnPairedTags,
		unpairedTags : checkedUnPairedTags.toString(),
		defaultSwitchTagMaps : htmlFilter.defaultSwitchTagMaps,
		switchTagMaps : checkedSwitchTagMaps.toString(),
		defaultWhitePreservingTags : htmlFilter.defaultWhitePreservingTags,
		whitePreservingTags : checkedWhitePreservingTags.toString(),
		defaultInternalTag : htmlFilter.defaultInternalTag,
		internalTag : checkedInternalTag.toString(),
		defaultTranslatableAttributes : htmlFilter.defaultTranslatableAttributes,
		translatableAttributes : checkedTranslatableAttributes.toString(),
		baseFilterId : baseFilterId
	}
    if(validate.containSpecialChar(obj.localizeFunction))
    {
        alert(invalidL10nChar + invalidChars);
        return;
    }  
	
	if(checkAllTagsIsEmpty(obj, validate))
	{
		alert(noTagsToChoose);
		return;
	}

	sendAjax(obj, "checkExist", "checkExistHtmlFilterCallback");
	
	checkExistHtmlFilterCallback.obj = obj;

}

function checkExistHtmlFilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("htmlFilterDialog");
		if(saveHtmlFilter.edit)
		{
			sendAjax(checkExistHtmlFilterCallback.obj, "updateHtmlFilter", "updateHtmlFilterCallback")
		}
		else
		{
			sendAjax(checkExistHtmlFilterCallback.obj, "saveHtmlFilter", "saveHtmlFilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("htmlFilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
	}
}

function updateHtmlFilterCallback(data)
{
	var color = saveHtmlFilter.color;
	var filterId = saveHtmlFilter.filterId;
	var filter = saveHtmlFilter.filter;
	var topFilterId = saveHtmlFilter.topFilterId;
	
	if(filter)
	{
		var htFilter = new Object();
		htFilter.id = filterId;
		htFilter.filterTableName = "html_filter";
		htFilter.filterName = checkExistHtmlFilterCallback.obj.filterName;
		htFilter.filterDescription = checkExistHtmlFilterCallback.obj.filterDesc;
		//htFilter.placeHolderTrim = checkExistHtmlFilterCallback.obj.
		htFilter.convertHtmlEntry = checkExistHtmlFilterCallback.obj.convertHtmlEntry;
		htFilter.ignoreInvalideHtmlTags = checkExistHtmlFilterCallback.obj.ignoreInvalideHtmlTags;
		htFilter.addRtlDirectionality = checkExistHtmlFilterCallback.obj.addRtlDirectionality;
		htFilter.whitespacePreserve = checkExistHtmlFilterCallback.obj.whitespacePreserve;
//		htFilter.extractCharset = checkExistHtmlFilterCallback.obj.extractCharset;
		htFilter.jsFunctionText = checkExistHtmlFilterCallback.obj.localizeFunction;
		
		htFilter.defaultEmbeddableTags = checkExistHtmlFilterCallback.obj.defaultEmbeddableTags;
		htFilter.embeddableTags = checkExistHtmlFilterCallback.obj.embeddableTags;
		
		htFilter.defaultInternalTag = checkExistHtmlFilterCallback.obj.defaultInternalTag;
		htFilter.internalTag = checkExistHtmlFilterCallback.obj.internalTag;
		
		htFilter.defaultPairedTags = checkExistHtmlFilterCallback.obj.defaultPairedTags;
		htFilter.pairedTags = checkExistHtmlFilterCallback.obj.pairedTags;
		
		htFilter.defaultUnpairedTags = checkExistHtmlFilterCallback.obj.defaultUnPairedTags;
		htFilter.unpairedTags = checkExistHtmlFilterCallback.obj.unpairedTags;
		
		htFilter.defaultSwitchTagMaps = checkExistHtmlFilterCallback.obj.defaultSwitchTagMaps;
		htFilter.switchTagMaps = checkExistHtmlFilterCallback.obj.switchTagMaps;
		
		htFilter.defaultWhitePreservingTags = checkExistHtmlFilterCallback.obj.defaultWhitePreservingTags;
		htFilter.whitePreservingTags = checkExistHtmlFilterCallback.obj.whitePreservingTags;
		
		htFilter.defaultTranslatableAttributes = checkExistHtmlFilterCallback.obj.defaultTranslatableAttributes;
		htFilter.translatableAttributes = checkExistHtmlFilterCallback.obj.translatableAttributes;
		htFilter.baseFilterId = checkExistHtmlFilterCallback.obj.baseFilterId;
		
		htFilter.companyId = companyId;
		var specialFilters = updateSpecialFilter(saveHtmlFilter.specialFilters, htFilter);
		reGenerateFilterList(topFilterId, specialFilters, color);
	}
}

function saveHtmlFilterCallback(data)
{
	var color = saveHtmlFilter.color;
	var topFilterId = saveHtmlFilter.topFilterId;
	var filter = getFilterById(topFilterId);
	
	if(filter)
	{
		var htFilter = new Object();
		htFilter.id = data - 0;
		htFilter.filterTableName = "html_filter";
		htFilter.filterName = checkExistHtmlFilterCallback.obj.filterName;
		htFilter.filterDescription = checkExistHtmlFilterCallback.obj.filterDesc;
		//htFilter.placeHolderTrim = checkExistHtmlFilterCallback.obj.
		htFilter.convertHtmlEntry = checkExistHtmlFilterCallback.obj.convertHtmlEntry;
		htFilter.ignoreInvalideHtmlTags = checkExistHtmlFilterCallback.obj.ignoreInvalideHtmlTags;
		htFilter.addRtlDirectionality = checkExistHtmlFilterCallback.obj.addRtlDirectionality;
		htFilter.whitespacePreserve = checkExistHtmlFilterCallback.obj.whitespacePreserve;
		htFilter.jsFunctionText = checkExistHtmlFilterCallback.obj.localizeFunction;
		
		htFilter.defaultEmbeddableTags = checkExistHtmlFilterCallback.obj.defaultEmbeddableTags;
		htFilter.embeddableTags = checkExistHtmlFilterCallback.obj.embeddableTags;
		
		htFilter.defaultPairedTags = checkExistHtmlFilterCallback.obj.defaultPairedTags;
		htFilter.pairedTags = checkExistHtmlFilterCallback.obj.pairedTags;
		
		htFilter.defaultUnpairedTags = checkExistHtmlFilterCallback.obj.defaultUnPairedTags;
		htFilter.unpairedTags = checkExistHtmlFilterCallback.obj.unpairedTags;
		
		htFilter.defaultInternalTag = checkExistHtmlFilterCallback.obj.defaultInternalTag;
		htFilter.internalTag = checkExistHtmlFilterCallback.obj.internalTag;
		
		htFilter.defaultSwitchTagMaps = checkExistHtmlFilterCallback.obj.defaultSwitchTagMaps;
		htFilter.switchTagMaps = checkExistHtmlFilterCallback.obj.switchTagMaps;
		
		htFilter.defaultWhitePreservingTags = checkExistHtmlFilterCallback.obj.defaultWhitePreservingTags;
		htFilter.whitePreservingTags = checkExistHtmlFilterCallback.obj.whitePreservingTags;
		
		htFilter.defaultTranslatableAttributes = checkExistHtmlFilterCallback.obj.defaultTranslatableAttributes;
		htFilter.translatableAttributes = checkExistHtmlFilterCallback.obj.translatableAttributes;
		htFilter.baseFilterId = checkExistHtmlFilterCallback.obj.baseFilterId;
		
		htFilter.companyId = companyId;
		filter.specialFilters.push(htFilter);
		reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}