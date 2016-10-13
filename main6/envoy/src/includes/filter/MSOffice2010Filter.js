var msoffice2010DocFilter = new MSOffice2010Filter();

function MSOffice2010Filter()
{
	this.filterTableName = "office2010_filter";
	
	this.paragraphStyles = "unextractableWordParagraphStyles";
	this.characterStyles = "unextractableWordCharacterStyles";
	this.excelCellStyles = "unextractableExcelCellStyles";
	this.wordInternalTextStyles = "selectedWordInternalTextStyles";
	this.excelInternalTextStyles = "selectedExcelInternalTextStyles";
	this.excelOrder = "n";
	
	this.defaultUnextractableWordParagraphStyles = "DONOTTRANSLATE_para,tw4winExternal";
	this.defaultUnextractableWordCharacterStyles = "DONOTTRANSLATE_char,tw4winInternal";
	this.defaultUnextractableExcelCellStyles = "tw4winExternal";

	this.defaultUnextractableWordCharacterStyles = "DONOTTRANSLATE_char,tw4winExternal";
	this.defaultSelectedWordInternalTextStyles = "tw4winInternal";
	this.defaultSelectedExcelInternalTextStyles = "tw4winInternal";
	
	this.selectTagsMap = new Object();
	this.selectTagsMap[this.paragraphStyles] = this.defaultUnextractableWordParagraphStyles;
	this.selectTagsMap[this.characterStyles] = this.defaultUnextractableWordCharacterStyles;
	this.selectTagsMap[this.excelCellStyles] = this.defaultUnextractableExcelCellStyles;
	this.selectTagsMap[this.wordInternalTextStyles] = this.defaultSelectedWordInternalTextStyles;
	this.selectTagsMap[this.excelInternalTextStyles] = this.defaultSelectedExcelInternalTextStyles;
	
    this.currentOption = "unextractableWordParagraphStyles";
    this.currentPage = 1;
	this.tagsEveryPage = 10;
	this.order = "asc";
    
	this.allSelectTagsOption = new Array();	
	this.allSelectTagsOption[this.paragraphStyles] = this.defaultUnextractableWordParagraphStyles;
    this.allSelectTagsOption[this.characterStyles] = this.defaultUnextractableWordCharacterStyles;
    this.allSelectTagsOption[this.excelCellStyles] = this.defaultUnextractableExcelCellStyles;
    this.allSelectTagsOption[this.wordInternalTextStyles] = this.defaultSelectedWordInternalTextStyles;
    this.allSelectTagsOption[this.excelInternalTextStyles] = this.defaultSelectedExcelInternalTextStyles;
}

MSOffice2010Filter.prototype.setFilter = function (filter)
{
	this.filter = filter;
	
	this.optionMap = new Object();
	this.optionMap[this.paragraphStyles] = jsUnextractableWordParagraphStyles;
    this.optionMap[this.characterStyles] = jsUnextractableWordCharacterStyles;
    this.optionMap[this.excelCellStyles] = jsUnextractableExcelCellStyles;
    this.optionMap[this.wordInternalTextStyles] = jsWordInternalTextCharacterStyles;
    this.optionMap[this.excelInternalTextStyles] = jsExcelInternalTextCellStyles;

}

MSOffice2010Filter.prototype.showStyleSelectBox = function(isDisabled) {
	document.getElementById("O2010UnextractableRule").disabled = isDisabled;
}

MSOffice2010Filter.prototype.edit = function(filterId, color, specialFilters, topFilterId)
{
	msoffice2010DocFilter.selectTagsMap[this.paragraphStyles] = this.filter.unextractableWordParagraphStyles;
	msoffice2010DocFilter.selectTagsMap[this.characterStyles] = this.filter.unextractableWordCharacterStyles;
	msoffice2010DocFilter.selectTagsMap[this.excelCellStyles] = this.filter.unextractableExcelCellStyles;
	msoffice2010DocFilter.selectTagsMap[this.wordInternalTextStyles] = this.filter.selectedWordInternalTextStyles;
	msoffice2010DocFilter.selectTagsMap[this.excelInternalTextStyles] = this.filter.selectedExcelInternalTextStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.paragraphStyles] = this.filter.allParagraphStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.characterStyles] = this.filter.allCharacterStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.excelCellStyles] = this.filter.allExcelCellStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.wordInternalTextStyles] = this.filter.allWordInternalTextStyles;
	msoffice2010DocFilter.allSelectTagsOption[this.excelInternalTextStyles] = this.filter.allExcelInternalTextStyles;
	
	msoffice2010DocFilter.init();
	
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' style='width:340px' maxlength='"+maxFilterNameLength+"' id='o2010FilterName' value='" + this.filter.filterName + "' >");
	str.append("<br/>");
	str.append("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterDesc + ":");
	str.append("</label>");
	str.append("<textarea rows='3' style='width:340px' id='o2010FilterDesc' name='desc' value='"+this.filter.filterDescription+"'>"+this.filter.filterDescription+"</textarea>");
	str.append("<br/>");
	
	str.append("<table border=0 width='408px'>");
	
	str.append("<tr style='display:none;'>");
	str.append("<td class='htmlFilter_left_td'>" + jsO2010XmlFilter + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.getXmlFilterSelect(this.filter));
	str.append("</td>");
	str.append("</tr>");
	
	var isChecked = (this.filter.headerTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransHeader + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='headerTranslate' type='checkbox' name='headerTranslate' value='"+this.filter.headerTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.footendnoteTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransFootEndNotes + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='footendnoteTranslate' type='checkbox' name='footendnoteTranslate' value='"+this.filter.footendnoteTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.notesTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTNotes + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='notesTranslate' type='checkbox' name='notesTranslate' value='"+this.filter.notesTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.masterTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='masterTranslate' type='checkbox' name='masterTranslate' value='"+this.filter.masterTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.pptlayoutTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTLayout + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='pptlayoutTranslate' type='checkbox' name='pptlayoutTranslate' value='"+this.filter.pptlayoutTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.notemasterTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTNoteMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='notemasterTranslate' type='checkbox' name='notemasterTranslate' value='"+this.filter.notemasterTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.handoutmasterTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTHandoutMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='handoutmasterTranslate' type='checkbox' name='handoutmasterTranslate' value='"+this.filter.handoutmasterTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.excelTabNamesTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractExcelTabNames + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='excelTabNamesTranslate' type='checkbox' name='excelTabNamesTranslate' value='"+this.filter.excelTabNamesTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.hiddenTextTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractHiddenText + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='hiddenTextTranslate' type='checkbox' name='hiddenTextTranslate' value='"+this.filter.hiddenTextTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.toolTipsTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractAlt + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='toolTipsTranslate' type='checkbox' name='toolTipsTranslate' value='"+this.filter.toolTipsTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.urlTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractUrl + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='urlTranslate' type='checkbox' name='urlTranslate' value='"+this.filter.urlTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.tableOfContentTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractToc + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='tableOfContentTranslate' type='checkbox' name='tableOfContentTranslate' value='"+this.filter.tableOfContentTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	isChecked = (this.filter.commentTranslate) ? "checked":"";
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractComment + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='commentTranslate' type='checkbox' name='commentTranslate' value='"+this.filter.commentTranslate+"' "+isChecked+"/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExcelOrder + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateExcelOrder(this.filter) + "</td>");
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
    str.append("<option value='unextractableExcelCellStyles'>" + jsUnextractableExcelCellStyles + "</option>");
	str.append("<option value='selectedWordInternalTextStyles'>" + jsWordInternalTextCharacterStyles + "</option>");
	str.append("<option value='selectedExcelInternalTextStyles'>" + jsExcelInternalTextCellStyles + "</option>");
	str.append("</select>");

	str.append("</td><td nowrap align=right>");
	

	str.append("<input type='button' value='" + jsAdd + "' onclick='msoffice2010DocFilter.onAdd()'>");
	str.append("<input type='button' value='" + jsDelete + "' id='O2010DeleteButton' onclick='msoffice2010DocFilter.deleteTag()'>");
	str.append("</td></tr></table>");
	checkAll2010Styles = false;
	
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

MSOffice2010Filter.prototype.generateExcelOrder = function (filter)
{
	var str = new StringBuffer("");
		
	str.append("<nobr><input value='n' type='radio' name='excelOrder'" + ((filter) ? (( filter.excelOrder == "n") ? " checked" : "") : " checked") + ">" 
				+ fontTagS + jsExcelOrderNo +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='r' type='radio' name='excelOrder'" + ((filter && filter.excelOrder == "r") ? " checked" : "") + ">" 
				+ fontTagS + jsExcelOrderRow +fontTagE);
	str.append("&nbsp;&nbsp;");
	str.append("<nobr><input value='c' type='radio' name='excelOrder'" + ((filter && filter.excelOrder == "c") ? " checked" : "") + ">" 
				+ fontTagS + jsExcelOrderColumn +fontTagE);
	return str.toString();
}

MSOffice2010Filter.prototype.setPageValue = function()
{
	document.getElementById("pageCountO2010").value = this.currentPage + 1;
	document.getElementById("PageTotalSizeO2010").innerHTML = this.getPageSize();
}

MSOffice2010Filter.prototype.generateDiv = function (topFilterId, color)
{
	var filter = getFilterById(topFilterId);
	
	var str = new StringBuffer("<label class='specialFilter_dialog_label'>");
	str.append(jsFilterName + ":");
	str.append("</label>");
	str.append("<input type='text' style='width:340px' maxlength='"+maxFilterNameLength+"' id='o2010FilterName' value='MSOffice2010Filter'>");
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
	str.append("<input id='headerTranslate' type='checkbox' name='headerTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransFootEndNotes + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='footendnoteTranslate' type='checkbox' name='footendnoteTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTNotes + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='notesTranslate' type='checkbox' name='notesTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + jsO2010TransMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='masterTranslate' type='checkbox' name='masterTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTLayout + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='pptlayoutTranslate' type='checkbox' name='pptlayoutTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTNoteMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='notemasterTranslate' type='checkbox' name='notemasterTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractPPTHandoutMaster + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='handoutmasterTranslate' type='checkbox' name='handoutmasterTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractExcelTabNames + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='excelTabNamesTranslate' type='checkbox' name='excelTabNamesTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractHiddenText + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='hiddenTextTranslate' type='checkbox' name='hiddenTextTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractAlt + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='toolTipsTranslate' type='checkbox' name='toolTipsTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractUrl + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='urlTranslate' type='checkbox' name='urlTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractToc + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='tableOfContentTranslate' type='checkbox' name='tableOfContentTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td' width='60%'>" + lbExtractComment + "</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append("<input id='commentTranslate' type='checkbox' name='commentTranslate' value='false' class='specialFilter_dialog_label'/>");
	str.append("</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>" + jsExcelOrder + "</td>");
	str.append("<td class='htmlFilter_right_td'>" + this.generateExcelOrder(this.filter) + "</td>");
	str.append("</tr>");
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append(jsContentPostFilter);
	str.append("</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(this.generateContentPostFilter(filter));
	str.append("</td>");
	str.append("</tr>");
	
	
	str.append("<tr>");
	str.append("<td class='htmlFilter_left_td'>");
	str.append(jsInternalTextPostFilter);
	str.append("</td>");
	str.append("<td class='htmlFilter_right_td'>");
	str.append(generateBaseFilterList(this.filterTableName));
	str.append("</td></tr>");
	
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
	str.append("<option value='unextractableExcelCellStyles'>" + jsUnextractableExcelCellStyles + "</option>");
	str.append("<option value='selectedWordInternalTextStyles'>" + jsWordInternalTextCharacterStyles + "</option>");
	str.append("<option value='selectedExcelInternalTextStyles'>" + jsExcelInternalTextCellStyles + "</option>");
	str.append("</select>");

	str.append("</td><td nowrap align=right>");

	str.append("<input type='button' value='" + jsAdd + "' onclick='msoffice2010DocFilter.onAdd()'>");
		
	str.append("<input type='button' value='" + jsDelete + "' id='O2010DeleteButton' onclick='msoffice2010DocFilter.deleteTag()' disabled>");
	str.append("</td></tr></table>");

	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.paragraphStyles] = "";
	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.characterStyles] = "";
	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.excelCellStyles] = "";
	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.wordInternalTextStyles] = "";
	msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.excelInternalTextStyles] = "";
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.paragraphStyles] = msoffice2010DocFilter.defaultUnextractableWordParagraphStyles;
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.characterStyles] = msoffice2010DocFilter.defaultUnextractableWordCharacterStyles;
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.excelCellStyles] = msoffice2010DocFilter.defaultUnextractableExcelCellStyles;
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.wordInternalTextStyles] = msoffice2010DocFilter.defaultSelectedWordInternalTextStyles;
	msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.excelInternalTextStyles] = msoffice2010DocFilter.defaultSelectedExcelInternalTextStyles;
	checkAll2010Styles = false;
	
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
			&& msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.characterStyles].length == 0
			&& msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.wordInternalTextStyles].length == 0
			&& msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.excelInternalTextStyles].length == 0
			&& msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.excelCellStyles].length == 0;

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

//for gbs-2599
MSOffice2010Filter.prototype.selectAll_MSOffice2010Filte = function()
{
	var selectAll = document.getElementById("selectAll_MSOffice2010Filte")
	if(selectAll.checked) {
		msoffice2010DocFilter.checkAllTagsToDelete();
	} else {
		msoffice2010DocFilter.clearAllTagsToDelete();
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
	str.append("<input type='checkbox' checked='true' id='selectAll_MSOffice2010Filte' onclick='msoffice2010DocFilter.selectAll_MSOffice2010Filte()'/>");//for gbs-2599
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
	/*for gbs-2599
	str.append("<a href='#' class='specialfilter_a' onclick='msoffice2010DocFilter.checkAllTagsToDelete()'>");
	str.append(jsCheckAll);
	str.append("</a>");
	str.append("&nbsp;|&nbsp;");
    str.append("<a href='#' class='specialfilter_a' onclick='msoffice2010DocFilter.clearAllTagsToDelete()'>");
    str.append(jsClearAll);
    str.append("</a>");
	*/
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
    
    checkBoxObjs = document.getElementsByName(msoffice2010DocFilter.wordInternalTextStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
    
    checkBoxObjs = document.getElementsByName(msoffice2010DocFilter.excelInternalTextStyles);
    for(var j = 0; j < checkBoxObjs.length; j++)
    {
        checkBoxObjs[j].checked = checkOrClear;
    }
    
    checkBoxObjs = document.getElementsByName(msoffice2010DocFilter.excelCellStyles);
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
	var wordInternalTextStylesToDelete = document.getElementsByName(msoffice2010DocFilter.wordInternalTextStyles);
	var excelInternalTextStylesToDelete = document.getElementsByName(msoffice2010DocFilter.excelInternalTextStyles);
	var excelCellStylesToDelete = document.getElementsByName(msoffice2010DocFilter.excelCellStyles);
		
	msoffice2010DocFilter.buildTagsString(paragraphStylesToDelete);
	msoffice2010DocFilter.buildTagsString(characterStylesToDelete);
	msoffice2010DocFilter.buildTagsString(wordInternalTextStylesToDelete);
	msoffice2010DocFilter.buildTagsString(excelInternalTextStylesToDelete);
	msoffice2010DocFilter.buildTagsString(excelCellStylesToDelete);
	
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
		if(document.getElementById("styles2010_"+i))
		{
			if (document.getElementById("styles2010_"+i).checked)
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
	checkAll2010Styles = false;
	
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
	checkAll2010Styles = checkBoxObj.checked;
	for(var i = 0; i < tags.length; i++)
	{
		if(document.getElementById("styles2010_"+i))
		{
			document.getElementById("styles2010_"+i).checked = checkAll2010Styles;	
		}
		
		if(checkAll2010Styles)
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
	var check = checkAll2010Styles ? "checked" : "";
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
			
			str.append("<td><input onclick='msoffice2010DocFilter.checkthis(this)' type='checkbox' name= '"+tags[i]+"' id='styles2010_"+i+"' "+check+"></td>");
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

MSOffice2010Filter.prototype.generateContentPostFilter = function (filter)
{
	var _filterConfigurations = filterConfigurations;
	var str = new StringBuffer("<select id='office2010ContentPostFilterSelect' class='xml_filter_select'>");
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
    var isNew = (saveMSOffice2010DocFilter.edit) ? "false" : "true";
	var filterId = saveMSOffice2010DocFilter.filterId;
	var filterDesc = document.getElementById("o2010FilterDesc").value;
	var headerTranslate = document.getElementById("headerTranslate").checked;
	var footendnoteTranslate = document.getElementById("footendnoteTranslate").checked;
	var masterTranslate = document.getElementById("masterTranslate").checked;
	var notesTranslate = document.getElementById("notesTranslate").checked;
	var pptlayoutTranslate = document.getElementById("pptlayoutTranslate").checked;
	var notemasterTranslate = document.getElementById("notemasterTranslate").checked;
	var handoutmasterTranslate = document.getElementById("handoutmasterTranslate").checked;
	var excelTabNamesTranslate = document.getElementById("excelTabNamesTranslate").checked;
	var hiddenTextTranslate = document.getElementById("hiddenTextTranslate").checked;
	var toolTipsTranslate = document.getElementById("toolTipsTranslate").checked;
	var urlTranslate = document.getElementById("urlTranslate").checked;
	var tableOfContentTranslate = document.getElementById("tableOfContentTranslate").checked;
	var commentTranslate = document.getElementById("commentTranslate").checked;
	var xmlFilterIdAndTableName = document.getElementById("xmlFilterSelect").value;
	var splitedXmlIdTable = splitByFirstIndex(xmlFilterIdAndTableName, "-");
	var xmlFilterId = (splitedXmlIdTable) ? splitedXmlIdTable[0] : -2;
	var excelOrder = getRadioValue(fpForm.excelOrder);
	var contentPostFilterIdAndTableName = document.getElementById("office2010ContentPostFilterSelect").value;
	var contentPostFilterIndex = contentPostFilterIdAndTableName.indexOf("-");
	var contentPostFilterId = -2;
	var contentPostFilterTableName = "";
	if (contentPostFilterIndex > 0)
	{
		contentPostFilterId = contentPostFilterIdAndTableName.substring(0,contentPostFilterIndex);
		contentPostFilterTableName = contentPostFilterIdAndTableName.substring(contentPostFilterIndex+1);
	}
	var baseFilterId = document.getElementById("office2010_filter_baseFilterSelect").value;
	
	alertUserBaseFilter(baseFilterId);
	
	var obj = {
			isNew : isNew,
			filterTableName:"office2010_filter",
			filterId:filterId,
			filterName:filterName,
			filterDesc:filterDesc,
			headerTranslate:headerTranslate,
			footendnoteTranslate:footendnoteTranslate,
			masterTranslate:masterTranslate,
			notesTranslate:notesTranslate,
			pptlayoutTranslate:pptlayoutTranslate,
			notemasterTranslate:notemasterTranslate,
			handoutmasterTranslate:handoutmasterTranslate,
			excelTabNamesTranslate:excelTabNamesTranslate,
			hiddenTextTranslate:hiddenTextTranslate,
			toolTipsTranslate:toolTipsTranslate,
			urlTranslate:urlTranslate,
			tableOfContentTranslate:tableOfContentTranslate,
			commentTranslate:commentTranslate,
			xmlFilterId:-2,
			excelOrder:excelOrder,
			unextractableWordParagraphStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.paragraphStyles],
			unextractableWordCharacterStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.characterStyles],
			unextractableExcelCellStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.excelCellStyles],
			allParagraphStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.paragraphStyles],
			allCharacterStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.characterStyles],
			allExcelCellStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.excelCellStyles],
			selectedWordInternalTextStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.wordInternalTextStyles],
			selectedExcelInternalTextStyles:msoffice2010DocFilter.selectTagsMap[msoffice2010DocFilter.excelInternalTextStyles],
			allWordInternalTextStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.wordInternalTextStyles],
			allExcelInternalTextStyles:msoffice2010DocFilter.allSelectTagsOption[msoffice2010DocFilter.excelInternalTextStyles],
			companyId:companyId,
			contentPostFilterId:contentPostFilterId,
			contentPostFilterTableName:contentPostFilterTableName,
			baseFilterId:baseFilterId
			};
	
		sendAjax(obj, "checkExist", "checkExistMSOffice2010FilterCallback");
	
		checkExistMSOffice2010FilterCallback.obj = obj;
}

function checkExistMSOffice2010FilterCallback(data)
{
	if(data == 'false')
	{
		closePopupDialog("msoffice2010FilterDialog");
		if(saveMSOffice2010DocFilter.edit)
		{
			sendAjax(checkExistMSOffice2010FilterCallback.obj, "updateMSOffice2010Filter", "updateMSOffice2010FilterCallback");
		}
		else
		{
			sendAjax(checkExistMSOffice2010FilterCallback.obj, "saveMSOffice2010Filter", "saveMSOffice2010FilterCallback");
		}
	}
	else if(data == 'failed')
	{
		closePopupDialog("msoffice2010FilterDialog");
		parent.location.reload();
	}
	else
	{
		alert(existFilterName);
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
		jpFilter.footendnoteTranslate = checkExistMSOffice2010FilterCallback.obj.footendnoteTranslate;
		jpFilter.masterTranslate = checkExistMSOffice2010FilterCallback.obj.masterTranslate;
		jpFilter.notesTranslate = checkExistMSOffice2010FilterCallback.obj.notesTranslate;
		jpFilter.pptlayoutTranslate = checkExistMSOffice2010FilterCallback.obj.pptlayoutTranslate;
		jpFilter.notemasterTranslate = checkExistMSOffice2010FilterCallback.obj.notemasterTranslate;
		jpFilter.handoutmasterTranslate = checkExistMSOffice2010FilterCallback.obj.handoutmasterTranslate;
		jpFilter.toolTipsTranslate = checkExistMSOffice2010FilterCallback.obj.toolTipsTranslate;
		jpFilter.excelTabNamesTranslate = checkExistMSOffice2010FilterCallback.obj.excelTabNamesTranslate;
		jpFilter.hiddenTextTranslate = checkExistMSOffice2010FilterCallback.obj.hiddenTextTranslate;
		jpFilter.urlTranslate = checkExistMSOffice2010FilterCallback.obj.urlTranslate;
		jpFilter.tableOfContentTranslate = checkExistMSOffice2010FilterCallback.obj.tableOfContentTranslate;
		jpFilter.commentTranslate = checkExistMSOffice2010FilterCallback.obj.commentTranslate;
		jpFilter.xmlFilterId = checkExistMSOffice2010FilterCallback.obj.xmlFilterId;
		jpFilter.unextractableWordParagraphStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.unextractableExcelCellStyles = checkExistMSOffice2010FilterCallback.obj.unextractableExcelCellStyles;
		jpFilter.allParagraphStyles = checkExistMSOffice2010FilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistMSOffice2010FilterCallback.obj.allCharacterStyles;
		jpFilter.allExcelCellStyles = checkExistMSOffice2010FilterCallback.obj.allExcelCellStyles;
		jpFilter.selectedWordInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.selectedWordInternalTextStyles;
		jpFilter.selectedExcelInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.selectedExcelInternalTextStyles;
		jpFilter.allWordInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.allWordInternalTextStyles;
		jpFilter.allExcelInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.allExcelInternalTextStyles;
		jpFilter.companyId = companyId;
		jpFilter.contentPostFilterId = checkExistMSOffice2010FilterCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistMSOffice2010FilterCallback.obj.contentPostFilterTableName;
		jpFilter.baseFilterId = checkExistMSOffice2010FilterCallback.obj.baseFilterId;
		jpFilter.excelOrder = checkExistMSOffice2010FilterCallback.obj.excelOrder;

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
		jpFilter.footendnoteTranslate = checkExistMSOffice2010FilterCallback.obj.footendnoteTranslate;
		jpFilter.masterTranslate = checkExistMSOffice2010FilterCallback.obj.masterTranslate;
		jpFilter.notesTranslate = checkExistMSOffice2010FilterCallback.obj.notesTranslate;
		jpFilter.pptlayoutTranslate = checkExistMSOffice2010FilterCallback.obj.pptlayoutTranslate;
		jpFilter.notemasterTranslate = checkExistMSOffice2010FilterCallback.obj.notemasterTranslate;
		jpFilter.handoutmasterTranslate = checkExistMSOffice2010FilterCallback.obj.handoutmasterTranslate;
		jpFilter.excelTabNamesTranslate = checkExistMSOffice2010FilterCallback.obj.excelTabNamesTranslate;
		jpFilter.hiddenTextTranslate = checkExistMSOffice2010FilterCallback.obj.hiddenTextTranslate;
		jpFilter.urlTranslate = checkExistMSOffice2010FilterCallback.obj.urlTranslate;
		jpFilter.tableOfContentTranslate = checkExistMSOffice2010FilterCallback.obj.tableOfContentTranslate;
		jpFilter.commentTranslate = checkExistMSOffice2010FilterCallback.obj.commentTranslate;
		jpFilter.xmlFilterId = checkExistMSOffice2010FilterCallback.obj.xmlFilterId;
		jpFilter.unextractableWordParagraphStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordParagraphStyles;
		jpFilter.unextractableWordCharacterStyles = checkExistMSOffice2010FilterCallback.obj.unextractableWordCharacterStyles;
		jpFilter.unextractableExcelCellStyles = checkExistMSOffice2010FilterCallback.obj.unextractableExcelCellStyles;
		jpFilter.allParagraphStyles = checkExistMSOffice2010FilterCallback.obj.allParagraphStyles;
		jpFilter.allCharacterStyles = checkExistMSOffice2010FilterCallback.obj.allCharacterStyles;
		jpFilter.allExcelCellStyles = checkExistMSOffice2010FilterCallback.obj.allExcelCellStyles;
		jpFilter.selectedWordInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.selectedWordInternalTextStyles;
		jpFilter.selectedExcelInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.selectedExcelInternalTextStyles;
		jpFilter.allWordInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.allWordInternalTextStyles;
		jpFilter.allExcelInternalTextStyles = checkExistMSOffice2010FilterCallback.obj.allExcelInternalTextStyles;
		jpFilter.companyId = companyId;
		jpFilter.contentPostFilterId = checkExistMSOffice2010FilterCallback.obj.contentPostFilterId;
		jpFilter.contentPostFilterTableName = checkExistMSOffice2010FilterCallback.obj.contentPostFilterTableName;
		jpFilter.baseFilterId = checkExistMSOffice2010FilterCallback.obj.baseFilterId;
		jpFilter.excelOrder = checkExistMSOffice2010FilterCallback.obj.excelOrder;
		
		filter.specialFilters.push(jpFilter);
	    reGenerateFilterList(topFilterId, filter.specialFilters, color);
	}
}
