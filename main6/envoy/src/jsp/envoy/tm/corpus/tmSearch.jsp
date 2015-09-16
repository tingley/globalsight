<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
	        com.globalsight.everest.webapp.WebAppConstants"
	session="true"%>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="addEntry" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="editEntry" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String searchUrl = self.getPageURL() + "&action="
            + WebAppConstants.TM_ACTION_SEARCH;
    String refreshPageUrl = self.getPageURL() + "&action="
            + WebAppConstants.TM_ACTION_REFRESH_PAGE;
    String deleteEntriesUrl = self.getPageURL() + "&action="
            + WebAppConstants.TM_ACTION_DELETE_ENTRIES;
    String addEntryUrl = addEntry.getPageURL() + "&action="
            + WebAppConstants.TM_ACTION_ADD_ENTRY;
    String editEntryUrl = editEntry.getPageURL() + "&action="
            + WebAppConstants.TM_ACTION_EDIT_ENTRY;
    String applyReplacedUrl = self.getPageURL() + "&action="
            + WebAppConstants.TM_ACTION_APPLY_REPLACE;
    String cancelUrl = cancel.getPageURL();
%>
<html>
<head>
<STYLE>
.choose {
	background-image: url(images/btn_choose.png);
	background-repeat: no-repeat;
	background-position: bottom right;
	border: solid 1px #85b1de;
	cursor: pointer;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	width: 200px;
	text-align: left;
}

.button_out {
    background-color: #738EB5;
    background-position: center center;
    background-repeat: no-repeat;
    border: 0 solid;
    cursor: pointer;
    font-size: 12px;
    height: 20px;
    margin-left: 1px;
    padding: 0;
    width: 30px;
}
.button_out_hover {
    background-color: #78ACFF;
    background-position: center center;
    background-repeat: no-repeat;
    border: 0 solid;
    cursor: pointer;
    font-size: 12px;
    height: 20px;
    margin-left: 1px;
    padding: 0;
    width: 30px;
}
.choose {
	background-image: url(images/btn_choose.png);
	background-repeat: no-repeat;
	background-position: bottom right;
	border: solid 1px #85b1de;
	cursor: pointer;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	width: 200px;
	text-align: left;
}
.link{
    color: #00289C;
    text-decoration: none;
    cursor:pointer;
}
.link:hover{
    color: #CE9E08;
    text-decoration: underline;
}

.search_content {
    border-bottom: 1px solid white;
    border-right: 1px solid white;
}

#sourceLocale, #targetLocale
{
 width:270px;
}

.tableHeadingBasic {
    background: none repeat scroll 0 0 #0C1476;
    color: white;
    font-family: Arial,Helvetica,sans-serif;
    font-size: 8pt;
    font-weight: bold;
}

body { background: url(images/page_bg.png) no-repeat; }

.mainHeading {
    color: #0C1476;
    font-size: 11pt;
    font-weight: bold;
}
.tableLine
{
 background: #EEEEEE;
}

.tmsDivPop {
margin-bottom: 3px;
display: none;
position: absolute;
background:#DEE3ED;
border:solid 1px #6e8bde;
}

#mask {
    display:none;
    z-index:9998;
    position:absolute;
    left:0px;
    top:0px;
    filter:Alpha(Opacity=30);
    /* IE */
    -moz-opacity:0.4;
    /* Moz + FF */
    opacity: 0.4;
}
.calendar { 
	width:16px;
	height:15px;
	cursor:pointer;
	vertical-align:middle;
	}
</STYLE>

<title>${lb_tm_search2}</title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/envoy/administration/permission/tree.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<script LANGUAGE="JavaScript" SRC="/globalsight/includes/dnd/DragAndDrop.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<%@ include file="/envoy/common/paging.jspIncl"%>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery.xmlext.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_search")%>";
var editWindow;
var addWindow;

var companiesForTM;
var tmsList;
var tmProfilesList;
var hasAddEntriesPerm;
var hasDeleteEntriesPerm;
var hasEditEntriesPerm;
var hasAdvancedSearchPerm;
var hasReplacePerm;

var searchText;
var advancedSearch = false;
var searchIn = "source";
var replaceText;
var createStartDateOption;
var createStartDate;
var createEndDateOption;
var createEndDate;
var modifyStartDateOption;
var modifyStartDate;
var modifyEndDateOption;
var modifyEndDate;
var lastUsageStartDateOption;
var lastUsageStartDate;
var lastUsageEndDateOption;
var lastUsageEndDate;
var tuIds;
var sids;
var createUser;
var modifyUser;
var attributeName;
var attributeValue;
var isRegex;
var jobIds;

var result = new Array();
var searchType;
var sourceLocaleText; 
var targetLocaleText; 
var mouseX;
var mouseY;

var maxEntriesPerPage=50; 
var totalPage=0;
var totalNum=0;
var currentPage=1;
var loading = '<center><img src="images/ajax-loader.gif"></img></center>';

var allEntriesAllPages = false;

/*
 * Load data of one page
 */
function loadPage(page)
{
	$("#pageNavigationHeader").html("");
	$("#searchResult").html("");
	$("#pageNavigationFooter").html("");
	$("#pageStatus").html("");
	$("#information").html("");
	$("#loading").html(loading);
	var searchParams = {"page": page, "maxEntriesPerPage": maxEntriesPerPage};
	$.ajax({
		   type: "POST",
		   url: "<%=refreshPageUrl%>",
		   dataType:'json',
		   cache:false,
		   data: searchParams,
		   success: function(json){
			   $("#loading").html("");
			   result = json;
			   resultDisplay(page);
		   }
		});
	currentPage = page;
 }
 
function resultDisplay(page)
{
	allEntriesAllPages = false;
	
	var buf = new Array();
	buf.push('<table id="rData" cellspacing="0" cellpadding="3" class="standardTextNew" width="100%"  style="border: 1px solid #0C1476;background:#FFFFFF;">');
	if(searchType=="matchSearch")
	{
	  buf.push('<tr class="tableHeadingBasic"><th style="border-left: #0C1476 1px solid;border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;color:#0C1476"><input type="checkbox" id="checkAllEntries"></input></th><th width="10%" align="left" style="border-right: #FFFFFF 1px solid;border-right: #FFFFFF 1px solid;">${lb_percentage} (%)</th><th width="32%" align="left" style="border-right: #FFFFFF 1px solid;border-right: #FFFFFF 1px solid;">'+sourceLocaleText+'</th><th width="32%" align="left" style="border-right: #FFFFFF 1px solid;border-right: #FFFFFF 1px solid;">'+targetLocaleText+'</th><th width="14%" align="left" style="border-right: #FFFFFF 1px solid;border-right: #FFFFFF 1px solid;">${lb_sid}</th><th width="12%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_tm_name}</th></tr>'); 
	  for(var i=0;i<result.length;i++)
	  {
		 var obj = result[i];
	     var score = obj.score;
	     
	     buf.push("<tr style='background:#DEE3ED;'>");
	     buf.push("<td style='border: #FFFFFF 1px solid;'><input name='entries' type='checkbox' value='"+i+"'></input></td>");
	     buf.push("<td style='border: #FFFFFF 1px solid;' align='left'>"+score.substring(0,score.indexOf("%"))+"</td>");
	     if(hasEditEntriesPerm)
	     {
	    	 buf.push("<td style='border: #FFFFFF 1px solid;' dir='"+obj.source.dir+"'><a href=\"javascript:editEntry('"+obj.tmId+"','"+ obj.tuId+"','"+obj.sourceLocale+"','"+obj.sourceTuvId+"','"+obj.targetLocale+"','"+obj.targetTuvId+"');\" class='link'>"+obj.source.content+"</a></td>");
	     }
	     else
	     {
	    	 buf.push("<td style='border: #FFFFFF 1px solid;' dir='"+obj.source.dir+"'>"+obj.source.content+"</td>");
	     } 
	     
	     buf.push("<td style='border: #FFFFFF 1px solid;' dir='"+obj.target.dir+"'>"+obj.target.content+"</td>");
	     buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.sid+"</td>");
	     buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.tm+"</td>");
	     buf.push("</tr>");
	  }
	}
	else
	{
	  buf.push('<tr class="tableHeadingBasic"><th style="border-left: #0C1476 1px solid;border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;color:#0C1476"><input type="checkbox" id="checkAllEntries"></input></th><th width="37%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">'+sourceLocaleText+'</th><th width="37%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">'+targetLocaleText+'</th><th width="14%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_sid}</th><th width="12%" align="left" style="border-bottom: #FFFFFF 1px solid;">${lb_tm_name}</th></tr>');
	  for(var i=0;i<result.length;i++)
	  {
		 var obj = result[i];
	     buf.push("<tr style='background:#DEE3ED;'>");
	     buf.push("<td style='border: #FFFFFF 1px solid;'><input id='11' name='entries' type='checkbox' value='"+i+"'></input></td>");
	     if(hasEditEntriesPerm)
	     {
	    	 buf.push("<td style='border: #FFFFFF 1px solid;' dir='"+obj.source.dir+"'><a href=\"javascript:editEntry('"+obj.tmId+"','"+ obj.tuId+"','"+obj.sourceLocale+"','"+obj.sourceTuvId+"','"+obj.targetLocale+"','"+obj.targetTuvId+"');\" class='link'>"+obj.source.content+"</a></td>");
	     }
	     else
	     {
	    	 buf.push("<td style='border: #FFFFFF 1px solid;' dir='"+obj.source.dir+"'>"+obj.source.content+"</td>");
	     } 
		 buf.push("<td style='border: #FFFFFF 1px solid;' dir='"+obj.target.dir+"'>"+obj.target.content+"</td>");
		 buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.sid+"</td>");
		 buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.tm+"</td>");
		 buf.push("</tr>");
	  }
	}
	buf.push('</table>');
	var pageNavigation = makePageNavigation(page, totalNum, maxEntriesPerPage, 3);
	$("#pageNavigationHeader").html(pageNavigation);
	$("#searchResult").html(buf.join(""));
	$("#pageNavigationFooter").html(pageNavigation);
	$("#pageStatus").html(makePageStatus(page, maxEntriesPerPage, totalNum));
}

function makeButtons()
{
	var buf = new Array();
	buf.push('<table cellspacing="0" class="standardTextNew" cellpadding="0" style="border:0px solid black"><tr valign="middle">');
	if(hasDeleteEntriesPerm)
    {
		buf.push('<td style="border:1px solid black;background-color:#738eb5;"><input id="deleteEntryBtn" class="button_out" type="button" title="Delete" style="background-image:url(\'/globalsight/images/trash.png\')"></td>');
		buf.push('<td style="width:1px"></td>');
    }
    if(hasAddEntriesPerm)
	{
    	buf.push('<td style="border:1px solid black;background-color:#738eb5;"><input id="addEntryBtn" class="button_out" type="button" title="Add" style="background-image:url(\'/globalsight/images/add.png\');"></td>');
    	buf.push('<td style="width:1px"></td>');
	}
    buf.push('<td id="applyTD" style="border:1px solid black;background-color:#738eb5;display:none"><input id="applyBtn" class="button_out" type="button" title="Apply" //style="background-image:url(\'/globalsight/images/apply.png\');"></td>');
	buf.push('<td style="width:1px"></td>');
    buf.push('</tr></table>');
	return buf.join("");
}

function getTMsHtml(company)
{
	var bufTMS = new Array();
    bufTMS.push("<table cellpadding=4 cellspacing=0>");
    bufTMS.push("<tr class='tableHeadingBasic' style='cursor:pointer;' onmousedown=\"DragAndDrop(document.getElementById('tmsDiv'),document.getElementById('contentLayer'))\"><td><input type='checkbox' id='tmsAll'/>${lb_tm}</td><td align='right'><span id='okTmsDiv' style='cursor:pointer;'>[${lb_ok}]</span></td></tr>");
	var tmsCompany =[];
	if(company==null)
	{
		tmsCompany = tmsList;
	}
	else
	{
		bufTMS.push("<tr><td class='standardTextNew'>${lb_company}</td><td><select id='company'>");
		for(var i=0;i<companiesForTM.length;i++)
		{
			if(company==companiesForTM[i])
			{
				bufTMS.push("<option value='"+companiesForTM[i]+"' SELECTED>"+companiesForTM[i]+"</option>");
			}
			else
			{
				bufTMS.push("<option value='"+companiesForTM[i]+"'>"+companiesForTM[i]+"</option>");
			}
		}
		bufTMS.push("</select></td></tr>");
		for(var i=0;i<tmsList.length;i++)
		{
			var obj = tmsList[i];
			if(company==obj.company)
			{
				tmsCompany.push(obj);
			}
		}
	}
    if(tmsCompany.length==0)
    {
    	bufTMS.push("<tr>");
 	    bufTMS.push("<td class='standardTextNew'>No TMs can be searched.</td>");
 	    bufTMS.push("</tr>");
    }
    else
    {   
    	var count = tmsCompany.length;
    	var rest =  count % 3;
	    var height = (rest == 0 ? count/3 : ((count-rest)/3 + 1)) * 30;
    	if(height>125)
    	{
    		height = 125;
    	}
    	height = height+"px";
    	bufTMS.push("<tr><td colspan=2>");
        bufTMS.push("<div class='standardTextNew' style='height:"+height+"; overflow-x:hidden;overflow-y:auto;'>");
        bufTMS.push("<table border='0' style='padding-right: 20px;'>");
        for(var i=0;i<tmsCompany.length;i=i+3)
    	{
    	   var obj1 = tmsCompany[i];
    	   var obj2 = tmsCompany[i+1]
    	   var obj3 = tmsCompany[i+2]
    	   bufTMS.push("<tr>");
    	   bufTMS.push("<td class='standardTextNew' nowrap><input type='checkbox' name='tms' value ='"+obj1.id+"'/>"+obj1.name+"</td>");
    	   if(obj2)
    	   {
    		   bufTMS.push("<td class='standardTextNew' nowrap><input type='checkbox' name='tms' value ='"+obj2.id+"'/>"+obj2.name+"</td>");
    	   }
    	   else
    	   {
    		   bufTMS.push("<td></td>");
    	   }
    	   if(obj3)
    	   {
    		   bufTMS.push("<td class='standardTextNew' nowrap><input type='checkbox' name='tms' value ='"+obj3.id+"'/>"+obj3.name+"</td>");
    	   }
    	   else
    	   {
    		   bufTMS.push("<td></td>");
    	   }
    	   bufTMS.push("</tr>");
    	}
        bufTMS.push("</table>");
    	bufTMS.push("</div>");
    	bufTMS.push("</td><tr>");
    }
    
    bufTMS.push("</table>");
    return bufTMS;
}

function popupDiv(e)
{
  var winWidth = $(window).width();
  var winHeight = $(window).height();
  
  $("#tmsDiv").css({"position": "absolute", "z-index": "9999"})
  .animate({left: e.pageX, top: e.pageY-70, opacity: "show" }, "fast");
  $("#mask").width(winWidth).height(winHeight).show();
}

function hideDiv()
{
  $("#mask").hide();
  $("#tmsDiv").animate({left: 0, top: 0, opacity: "hide" }, "fast");
}

document.onkeydown = function (e) {
	var theEvent = window.event || e;
	var code = theEvent.keyCode || theEvent.which;
	if (code == 13) 
	{
	  $("#search").click();
	}
} 

/**
 * Edit entry
 */
function editEntry(tmId, tuId, sourceLocale, sourceTuvId, targetLocale, targetTuvId)
{
	if(editWindow)
	{
		editWindow.close();
		editWindow=null;
	}
	var editEntry = "<%=editEntryUrl%>"+"&tmId=" + tmId + "&tuId=" + tuId+"&sourceLocale="+sourceLocale+"&sourceTuvId="+sourceTuvId+"&targetLocale="+targetLocale+"&targetTuvId=" + targetTuvId;
	editWindow = window.open(editEntry, "EditEntry", "resizable,width=700,height=690, scrollbars" + ",top=200,left=300");
}

function showNoResults()
{
	result = new Array();
    var buf = new Array();
	buf.push('<table id="rData" cellspacing="0" cellpadding="3" class="standardTextNew" width="100%">');
	if(searchType=="matchSearch")
	{
		buf.push('<tr class="tableHeadingBasic">');
		buf.push('<th style="border-left: #0C1476 1px solid;border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;color:#0C1476"><input type="checkbox"></input></th>');
		buf.push('<th width="10%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_percentage} (%)</th>');
		buf.push('<th width="32%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">'+sourceLocaleText+'</th>');
		buf.push('<th width="32%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">'+targetLocaleText+'</th>');
		buf.push('<th width="14%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_sid}</th>');
		buf.push('<th width="12%" align="left" style="border-bottom: #FFFFFF 1px solid;">${lb_tm_name}</th></tr>');
	}
	else
	{
	  buf.push('<tr class="tableHeadingBasic">');
	  buf.push('<th style="border-left: #0C1476 1px solid;border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;color:#0C1476"><input type="checkbox" id="checkAllEntries"></input></th>');
	  buf.push('<th width="37%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">'+sourceLocaleText+'</th>');
	  buf.push('<th width="37%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">'+targetLocaleText+'</th>');
	  buf.push('<th width="14%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_sid}</th>');
	  buf.push('<th width="12%" align="left" style="border-bottom: #FFFFFF 1px solid;">${lb_tm_name}</th></tr>');
	}
	buf.push('</table>');
	buf.push("${msg_search_results_nothing_found}");
	buf.push(' If "Search in Target", please ensure target has been indexed ("Translation Memory" >> "ReIndex...").<p>');
	$("#searchResult").html(buf.join(""));
	$("#loading").html("");
}

/*
 * Delete entries
 */
function deleteEntries()
{
	var entries="";
	$("[name='entries']:checked").each(function(){
	    entries+=$(this).val()+",";
	});
	
	if(""==entries)
	{
		alert("${msg_tm_search_no_entry_selected}");
		return;
	}
	
	if(!confirm("${msg_tm_search_confirm_deleted}"))
	{
		return;
	}
	
	$("#pageNavigationHeader").html("");
	$("#searchResult").html("");
	$("#pageNavigationFooter").html("");
	$("#pageStatus").html("");
	$("#information").html("");
	$("#loading").html(loading);
	
	var searchParams = {"entries":entries,
		                "currentPage":currentPage,
		                "maxEntriesPerPage": maxEntriesPerPage, 
		                "allEntriesAllPages": allEntriesAllPages};
	$.ajax({
		   type: "POST",
		   url: "<%=deleteEntriesUrl%>",
		   dataType:'json',
		   cache:false,
		   data: searchParams,
		   success: function(json){
			   if (json != null && json.totalNum != 0) 
			   {
				 totalNum = json.totalNum;
				 totalPage = Math.ceil(totalNum/maxEntriesPerPage);
				 $("#loading").html("");
				 result = json.result;
				 resultDisplay(1);
			   } 
			   else 
			   {
				   showNoResults();
	           }
		   }
	});
}
 
function applyReplaced()
{
	var entries="";
	$("[name='entries']:checked").each(function(){
	    entries+=$(this).val()+",";
	});
	
	if(""==replaceText)
	{
		alert("${msg_tm_search_with_replace_text}");
		return;
	}
	
	if(""==entries)
	{
		alert("${msg_tm_search_no_entry_selected}");
		return;
	}
	
	if(!confirm("${msg_tm_search_confirm_replaced}"))
	{
		return;
	}
	
	$("#pageNavigationHeader").html("");
	$("#searchResult").html("");
	$("#pageNavigationFooter").html("");
	$("#pageStatus").html("");
	$("#information").html("");
	$("#loading").html(loading);
	
	var searchParams = {"entries":entries,
		                "currentPage":currentPage,
		                "maxEntriesPerPage": maxEntriesPerPage, 
		                "allEntriesAllPages": allEntriesAllPages,
		                "searchText": searchText,
		                "searchIn": searchIn,
		                "replaceText": replaceText};
	$.ajax({
		   type: "POST",
		   url: "<%=applyReplacedUrl%>",
		   dataType:'text',
		   cache:false,
		   data: searchParams,
		   success: function(text){
			   searchClick();
			   $("#information").html(text);
		   }
	});
 }

function addEntry()
{
	if(addWindow)
	{
		addWindow.close();
		addWindow=null;
	}
	var addEntry = "<%=addEntryUrl%>"+ "&sourceLocaleId="+$("#sourceLocale").val()+"&targetLocaleId="+$("#targetLocale").val();
	addWindow = window.open(addEntry, "addEntry", "resizable,width=550,height=630, scrollbars" + ",top=200,left=300");
}

function searchClick()
{
	if(result.length>0)
	{
		$("#search").click();	
	}
}

/**
 * Init
 */
function init()
{
  var locales = $.parseJSON('${locales}');
  companiesForTM = $.parseJSON('${companiesForTM}');
  tmsList = $.parseJSON('${tms}');
  tmProfilesList = $.parseJSON('${tmProfiles}');
    
  //Set permissions
  hasAddEntriesPerm = ${hasAddEntriesPerm};
  hasDeleteEntriesPerm = ${hasDeleteEntriesPerm};
  hasEditEntriesPerm = ${hasEditEntriesPerm};
  hasAdvancedSearchPerm = ${hasAdvancedSearchPerm};
  var hasTermSearchPermission = ${hasTermSearchPermission};
  if(!hasTermSearchPermission)
  {
	  $("#termSearchTD").hide();
  }
  if(hasAdvancedSearchPerm)
  {
	  $("#advancedLinkTD").show();  
	  $("#searchBtnTD").css({"width":"5%"});
  }  
  //Set source and target locales
  var bufLocales = new Array();
  bufLocales.push('<option value="-1">&nbsp;</option>');
  for(var i=0;i<locales.length;i++)
  {
	var obj = locales[i];
	var contentHtml='<option value="'+obj.id+'">'+obj.displayName+'</option>';
	bufLocales.push(contentHtml);
  }
  $("#sourceLocale").html(bufLocales.join(""));
  $("#sourceLocale").attr("value", "32");
  $("#targetLocale").html(bufLocales.join(""));
    
  //Set tms 
  var bufTMS;
  if(companiesForTM!=null)
  {
    bufTMS = getTMsHtml(companiesForTM[0]);
  }
  else
  {
    bufTMS = getTMsHtml(null);
  }
  $("#tmsDiv").html(bufTMS.join(""));
    
  //Set TM Profiles
  var bufTMPS = new Array();
  bufTMPS.push('<option value="-1">&nbsp;</option>');
  for(var i=0;i<tmProfilesList.length;i++)
  {
   var obj = tmProfilesList[i];
   var contentHtml='<option value="'+obj.id+'">'+obj.name+'</option>';
   bufTMPS.push(contentHtml);
  }
  $("#tmps").html(bufTMPS.join("")); 
  
  //Set buttons
  $("#buttons").html(makeButtons());
}

function directTermSearchPage(pageUrl)
{
  var searchText = $("#searchText").val();
  var directTo = pageUrl
	  +"&fromTMSearchPage=fromTMSearchPage"
	  +"&sourceLocale="+$("#sourceLocale").val()
	  +"&targetLocale="+$("#targetLocale").val()
	  +"&searchText="+searchText;
  window.location = directTo;
}

function shareConditionTMAndTermSearch()
{
  if('${fromTermSearchPage}')
  {
	$("#sourceLocale").val('${sourceLocale}');
	$("#targetLocale").val('${targetLocale}');
	$("#searchText").val('${searchText}');
  }
}

function showCalendar(id) {
    var cal1 = new calendar2(document.getElementById(id));
    cal1.year_scroll = true;
    cal1.time_comp = false;
    cal1.popup();
}

/*
 * Events
 */
$(document).ready(function(){
	$("#csf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#cef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#cef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#csf").datepicker( "option", "maxDate", selectedDate );
		}
	});
	
	$("#msf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#mef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#mef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#msf").datepicker( "option", "maxDate", selectedDate );
		}
	});
	
	$("#lusf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#luef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#luef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#lusf").datepicker( "option", "maxDate", selectedDate );
		}
	});
	 loadGuides();  
	 init();
	 shareConditionTMAndTermSearch();
	 
	 $("#maxEntriesPerPage").change(function(){
		 
		 var newMax = $("#maxEntriesPerPage").val();
		 if(maxEntriesPerPage==newMax)
		 {
			 return;
	     }
		 
		 if(totalNum==0)
		 {
			 maxEntriesPerPage=newMax; 
			 return;
		 }
		 else
		 {
			 if("All"==newMax)
			 {
				 newMax = totalNum;
			 }
		 }
		 
         if(maxEntriesPerPage<newMax)
		 {
			 //20->50
			 if(totalPage>1)
		     {
				 maxEntriesPerPage=newMax;
				 totalPage = Math.ceil(totalNum/maxEntriesPerPage);
				 loadPage(1);
		     }
		 }
		 else
		 {
			 //50->20
			 if(totalNum>newMax)
		     {
				 maxEntriesPerPage=newMax;
				 totalPage = Math.ceil(totalNum/maxEntriesPerPage);
				 loadPage(1);
		     }
		 }
		 maxEntriesPerPage=newMax; 
	 });
	 
	 $("#tmsAll").live("click", function(){
		 if($("#tmsAll").attr("checked")) 
		 {
			 $("input[name='tms']").attr("checked","true");
		 }
		 else
		 {
			 $("input[name='tms']").removeAttr("checked"); 
		 }
     });
	 
	 $("#checkAllEntries").live("click", function(){
		 if($("#checkAllEntries").attr("checked")) 
		 {
			 $("input[name='entries']").attr("checked","true");
			 var count=0;
			 $("[name='entries']:checked").each(function(){
				 count++;
			 });
			 if(count==totalNum)
			 {
				 var buf = new Array();
				 buf.push("All "+count+" entries have been selected.");
				 $("#information").html(buf.join(""));
			 }
			 else
			 {
				 var buf = new Array();
				 buf.push("All "+count+" entries have been selected in current page, select ");
				 buf.push("<a id='allEntriesLink' class='link' href='#'><b>All entries</b></a>");
				 buf.push(" in all pages.");
				 $("#information").html(buf.join(""));
			 }
		 }
		 else
		 {
			 $("input[name='entries']").removeAttr("checked"); 
			 $("#information").html("");
			 allEntriesAllPages = false;
		 }
     });
	 
	 $("#allEntriesLink").live("click", function(){
		 allEntriesAllPages = true;
		 $("#information").html("All "+totalNum+" entries have been selected in all pages");
     });
	 
     $("#searchType").change(function(){
    	 if("matchSearch"==$("#searchType").val())
         {
        	 
        	 $("#tmLabel").hide();
        	 $("#tmSelect").hide();
          	 $("#tpLabel").show();
          	 $("#tpSelect").show();
         }
         else
         {
        	 $("#tmLabel").show();
        	 $("#tmSelect").show();
             $("#tpLabel").hide();
             $("#tpSelect").hide();
         }
     });
     
     $("#tmsInput").click(function(e){
    	 popupDiv(e);
     })
     
     $("#okTmsDiv").live("click",function(){
    	 var tms ="";
    	 $("[name='tms']:checked").each(function(){ 
    		 tms+=$(this).parent().text()+",";
    	 }) 
    	 if(tms!="")
    	 {
    		 tms = tms.substring(0, tms.length-1);
    		 var tmsStr = tms;
    		 if(tmsStr.length>23)
        	 {
        		 var temp = tmsStr.substring(0,23);
        		 $("#tmsInput").attr("value", temp+"...");
        	 }
        	 else
        	 {
        		 $("#tmsInput").attr("value", tmsStr);
        	 }
    	 }
    	 else
    	 {
    		 $("#tmsInput").attr("value", "");
    		 $("#tmsInput").attr("value", "");
    	 }
    	 hideDiv();
     })
     
     $("#company").live("change", function(){
    	 var bufTMS = getTMsHtml($("#company").val());
    	 $("#tmsDiv").html(bufTMS.join(""));
     })
     
     $("#tmsInput").hover(function(e){
    	 if($("#tmsInput").val()!="")
    	 {
    		 var buf="";
    		 var tms ="";
        	 $("[name='tms']:checked").each(function(){ 
        		 buf=buf+$(this).parent().text()+"<br>"
        	 }) 
        	 buf = buf.substring(0, buf.length-4);
    		 $("#tmsTitle").html(buf); 
    		 var excursion = 20;
    		 if($.browser.mozilla)
    			 excursion=10;
    		 var left = $("#tmsInput").offset().left+$("#tmsInput").width()-excursion;
    		 var top = $("#tmsInput").offset().top-90; 
    		 $("#tmsTitle").css({"top":(top)+"px","left":(left)+"px"});
    		 $("#tmsTitle").show();
    	 }
     }).mouseout(function(){
	         $('#tmsTitle').hide();
     })
     
     $("#deleteEntryBtn").live("click", function(){
   	     deleteEntries();
     })
     
     $("#applyBtn").live("click", function(){
    	 applyReplaced();
     })
     
     $("#addEntryBtn").live("click", function(){
   	     addEntry();
     })
     
     $("#advanced").click(function(){
    	 $("#simple").show();
    	 $("#advanced").hide();
    	 $("#replaceCondition").show();
   	     $("#searchText").width("240px");
   	     $("#replaceText").width("240px"); 
   	     $("#applyTD").show();
   	     $("#revertTD").show();
   	     $("#searchInTD").show();
   	     $("#information").html("${lb_tm_search_hint2}");
   	     $("#advancedSearch").show();
   	  	 createStartDateOption = $("#createStartDateOption").val();
	  	 if(createStartDateOption == "eq")
	  	 {
	  	 	 $("#createEndDateOption").attr("disabled",true);
	  		 $("#cef").attr("disabled",true);
	  	 }
	  	 else if(createStartDateOption == "neq")
		 {
			 $("#createEndDateOption").attr("disabled",true);
			 $("#cef").attr("disabled",true);
		 }
	  	 modifyStartDateOption = $("#modifyStartDateOption").val();
	  	 if(modifyStartDateOption == "eq")
	  	 {
	  		 $("#modifyEndDateOption").attr("disabled",true);
	  		 $("#mef").attr("disabled",true);
	  	 }
	  	 else if(modifyStartDateOption == "neq")
		 {
			 $("#modifyEndDateOption").attr("disabled",true);
			 $("#mef").attr("disabled",true);
		 }
	  	lastUsageStartDateOption = $("#lastUsageStartDateOption").val();
	  	 if(lastUsageStartDateOption == "eq")
	  	 {
	  		 $("#lastUsageEndDateOption").attr("disabled",true);
	  		 $("#luef").attr("disabled",true);
	  	 }
	  	 else if(lastUsageStartDateOption == "neq")
		 {
			 $("#lastUsageEndDateOption").attr("disabled",true);
			 $("#luef").attr("disabled",true);
		 }
   	     advancedSearch = true;
   	     searchClick();
     })
     $("#simple").click(function(){
    	 $("#advanced").show();
    	 $("#simple").hide();
    	 $("#replaceCondition").hide();
    	 $("#searchText").width("590px");
    	 $("#applyTD").hide();
   	     $("#revertTD").hide();
   	     $("#searchInTD").hide();
   	     $("#information").html("");
   	 	 $("#advancedSearch").hide();
   	 	 
   	     advancedSearch = false;
   	     searchClick();
     })
     
     $("#addEntryBtn, #deleteEntryBtn, #applyBtn, #search").hover(function() {
	    $(this).addClass("button_out_hover");
	}, function() {
		 $(this).removeClass("button_out_hover");
	});
     
     $("#search").click(function(){
    	 searchType=$("#searchType").val();
    	 searchText = $("#searchText").val();
    	 var sourceLocale = $("#sourceLocale").val();
    	 sourceLocaleText=$("#sourceLocale").find("option:selected").text(); 
    	 var targetLocale = $("#targetLocale").val();
    	 targetLocaleText=$("#targetLocale").find("option:selected").text(); 
    	 var tmps = $("#tmps").val();
    	 var tms ="";
    	 $("[name='tms']:checked").each(function(){ 
    		 tms+=$(this).val()+",";
    	 }) 
    	 searchIn = "source";
    	 if(advancedSearch)
    	 {
    		 //search in source or target can be allowed for AdvancedSearch
    		 searchIn = $("#searchIn").val();
    		 replaceText = $("#replaceText").val();
    		 tuIds = $("#tuIds").val();
   			 if(tuIds.length > 0)
   		     {
   				if(tuIds.indexOf(",") >0)
   				{
   					temp = tuIds.split(",");
   					for(i=0; i<temp.length; i++)
   					{
   						if(temp[i].indexOf("-") > 0)
	   	   				{
	   	   					temp2 = temp[i].split("-");
	   	   					if(temp2.length != 2)
	   	   					{
	   	   					alert("TU ID" + "<%=bundle.getString("msg_invalid_entry6")%>");
	   	   						return false;
	   	   					}
	   	   					else
	   	   					{
	   	   						var min = temp2[0];
	   	   						var max = temp2[1];
	   	   						if(!isInteger(min) || !isInteger(max))
	   	   						{
	   	   							alert("TU ID" + "<%=bundle.getString("msg_invalid_entry5")%>");
	   	   							return false;
	   	   						}
	   	   						else if(Number(min) > Number(max))
	   	   						{
	   	   						alert("TU ID" + "<%=bundle.getString("msg_invalid_entry7")%>");
	   	   							return false;
	   	   						}
	   	   					}
	   	   				}
   						else if(!isInteger(temp[i]))
   						{
   							alert("TU ID" + "<%=bundle.getString("msg_invalid_entry5")%>");
   							return false;
   						}
   					}
   				}
   				else if (tuIds.indexOf("-") > 0)
   				{
   					temp2 = tuIds.split("-");
   					if(temp2.length != 2)
   					{
   						alert("TU ID" + "<%=bundle.getString("msg_invalid_entry6")%>");
   						return false;
   					}
   					else
   					{
   						var min = temp2[0];
   						var max = temp2[1];
   						if(!isInteger(min) || !isInteger(max))
   						{
   							alert("TU ID" + "<%=bundle.getString("msg_invalid_entry5")%>");
   							return false;
   						}
   						else if(Number(min) > Number(max))
   						{
   							alert("TU ID" + "<%=bundle.getString("msg_invalid_entry7")%>");
   							return false;
   						}
   					}
   				}
   				else if(!isInteger(tuIds))
   				{
   					alert("TU ID" + "<%=bundle.getString("msg_invalid_entry5")%>");
   					return false;
   				}
   		     }
    		 sids = $("#sids").val();
    		 if(searchText=="" && sids=="" )
	    	 {
	    		 alert("${msg_tm_search_search_text_and_sid}");
	    		 return;
	         }
	    	 else if(searchText=="*")
	    	 {
	    		 alert("${msg_tm_search_text_invalid}");
	    		 return;
	    	 }
	    	 else if(searchText=="\"*\"")
	    	 {
	    		 alert("${msg_tm_search_text_invalid2}");
	    		 return;
	    	 }
    		 
    		 isRegex = document.getElementById("isRegex").checked;
    		 createUser = $("#createUser").val();
    		 if(hasSomeSpecialChars(createUser))
    		 {
    			 alert("<%=EditUtil.toJavascript(bundle.getString("lb_export_create_user"))%> " +
   		              "<%=EditUtil.toJavascript(bundle
   									.getString("msg_invalid_entry3"))%>");
    		 }
    		 modifyUser = $("#modifyUser").val();
    		 if(hasSomeSpecialChars(modifyUser))
    		 {
    			 alert("<%=EditUtil.toJavascript(bundle.getString("lb_modify"))%> " +
    				  "<%=EditUtil.toJavascript(bundle.getString("lb_user"))%> " +
   		              "<%=EditUtil.toJavascript(bundle
   									.getString("msg_invalid_entry3"))%>");
    		     return false; 
    		 }
    		 attributeName = $("#attributeName").val();
    		 if(hasHtmlSpecialChars(attributeName))
    		 {
    			 alert("<%=EditUtil.toJavascript(bundle.getString("lb_attributename"))%> " +
    		              "<%=EditUtil.toJavascript(bundle
    									.getString("msg_html_special_char"))%>");
    			 return false;
    		 }
    		 attributeValue = $("#attributeValue").val();
    		 
    		 jobIds = $("#jobIds").val();
    		 if(checkSomeSpecialChar(jobIds))
    		 {
    			 alert("<%=EditUtil.toJavascript(bundle.getString("lb_job_id"))%> " +
   		              "<%=EditUtil.toJavascript(bundle
							.getString("msg_invalid_entry8"))%>");
    			 return false;
    		 }
    		 if(jobIds.trim() != "" && jobIds.trim() != null)
    		 {
	    		 var temp=/^\d+(\.\d+)?$/;
	    		 var jobIdArr = jobIds.trim().split(",");
	    		 for(var i =0;i<jobIdArr.length;i++)
	    		 {
	    			 if(temp.test(jobIdArr[i])==false)
	    			 {
	    				 alert("<%=EditUtil.toJavascript(bundle.getString("lb_job_id"))%> " +
	    			              "<%=EditUtil.toJavascript(bundle
	    							.getString("msg_invalid_entry8"))%>");
	    				 return false;
	    			 }
	    		 }
    		 }
    		 
    		 createStartDateOption = $("#createStartDateOption").val();
    		 createStartDate = $("#csf").val();
    		 createEndDateOption = $("#createEndDateOption").val();
    		 createEndDate = $("#cef").val();
    		 if(createStartDateOption == "gt" && createEndDateOption == "lt"){
    			if(createStartDate == createEndDate){
    				alert("${lb_tm_check_date_greater_less}");
    				return;
    			}
    		}
    		 if(createStartDateOption == "gt" && createEndDateOption == "lteq"){
     			if(createStartDate == createEndDate){
     				alert("${lb_tm_check_date_greater_less_equal}");
     				return;
     			}
     		}
    		 if(createStartDateOption == "gteq" && createEndDateOption == "lt"){
     			if(createStartDate == createEndDate){
     				alert("${lb_tm_check_date_greater_equal_less}");
     				return;
     			}
     		}
    		 
    		 modifyStartDateOption = $("#modifyStartDateOption").val();
    		 modifyStartDate = $("#msf").val();
    		 modifyEndDateOption = $("#modifyEndDateOption").val();
    		 modifyEndDate = $("#mef").val();
    		 if(modifyStartDateOption == "gt" && modifyEndDateOption == "lt"){
    			if(modifyStartDate == modifyEndDate){
    				alert("${lb_tm_check_date_greater_less}");
    				return;
    			}
    		}
    		 if(modifyStartDateOption == "gt" && modifyEndDateOption == "lteq"){
     			if(modifyStartDate == modifyEndDate){
     				alert("${lb_tm_check_date_greater_less_equal}");
     				return;
     			}
     		}
    		 if(modifyStartDateOption == "gteq" && modifyEndDateOption == "lt"){
     			if(modifyStartDate == modifyEndDate){
     				alert("${lb_tm_check_date_greater_equal_less}");
     				return;
     			}
     		}
    		 
       	     lastUsageStartDateOption = $("#lastUsageStartDateOption").val();
    		 lastUsageStartDate = $("#lusf").val();
    		 lastUsageEndDateOption = $("#lastUsageEndDateOption").val();
    		 lastUsageEndDate = $("#luef").val();
    		 if(lastUsageStartDateOption == "gt" && lastUsageEndDateOption == "lt"){
    			if(lastUsageStartDate == lastUsageEndDate){
    				alert("${lb_tm_check_date_greater_less}");
    				return;
    			}
    		}
    		 if(lastUsageStartDateOption == "gt" && lastUsageEndDateOption == "lteq"){
     			if(lastUsageStartDate == lastUsageEndDate){
     				alert("${lb_tm_check_date_greater_less_equal}");
     				return;
     			}
     		}
    		 if(lastUsageStartDateOption == "gteq" && lastUsageEndDateOption == "lt"){
     			if(lastUsageStartDate == lastUsageEndDate){
     				alert("${lb_tm_check_date_greater_equal_less}");
     				return;
     			}
     		}
       	 }

    	 var searchParams;
    	 if(!advancedSearch)
    	 {
	    	 if(searchText=="")
	    	 {
	    		 alert("${msg_tm_search_search_text}");
	    		 return;
	         }
	    	 else if(searchText=="*")
	    	 {
	    		 alert("${msg_tm_search_text_invalid}");
	    		 return;
	    	 }
	    	 else if(searchText=="\"*\"")
	    	 {
	    		 alert("${msg_tm_search_text_invalid2}");
	    		 return;
	    	 }
    	 }
    		 
    	 if(sourceLocale==-1)
         {
    		 alert("${msg_tm_search_source}");
    		 return;
         }
    	 if(targetLocale==-1)
    	 {
    		 alert("${msg_tm_search_target}");
    		 return;
    	 }
    	 
    	 
    	 if(searchType=="matchSearch")
    	 {
    		 if(tmps=="-1")
    	     {
    			 alert("${msg_tm_search_tm_profile}");
    			 return;
    	     }
    	 }
    	 else
         {
    		 if(tms=="")
        	 {
    			 alert("${msg_tm_search_tms}");
    			 return;
        	 } 
    		 tms = tms.substring(0, tms.length-1);
         }
    	 
    	 maxEntriesPerPage = $("#maxEntriesPerPage").val();
    	 
    	 if(searchType=="matchSearch")
    	 {
    		 searchParams={"searchType": searchType,
				       "searchText": searchText,
			           "sourceLocale": sourceLocale,
			           "targetLocale": targetLocale,
			           "tmps": tmps,
			           "maxEntriesPerPage":maxEntriesPerPage,
			           "searchIn":searchIn,
			           "advancedSearch":advancedSearch,
			           "replaceText":replaceText,
			           "createStartDateOption":createStartDateOption,
			           "createStartDate":createStartDate,
			           "createEndDateOption":createEndDateOption,
			           "createEndDate":createEndDate,
			           "modifyStartDateOption":modifyStartDateOption,
			           "modifyStartDate":modifyStartDate,
			           "modifyEndDateOption":modifyEndDateOption,
			           "modifyEndDate":modifyEndDate,
			           "lastUsageStartDateOption":lastUsageStartDateOption,
			           "lastUsageStartDate":lastUsageStartDate,
			           "lastUsageEndDateOption":lastUsageEndDateOption,
			           "lastUsageEndDate":lastUsageEndDate,
			           "jobIds":jobIds,
			           "tuIds":tuIds,
			           "sids":sids,
			           "isRegex":isRegex,
			           "createUser":createUser,
			           "modifyUser":modifyUser,
			           "attributeName":attributeName,
			           "attributeValue":attributeValue};
    	 }
    	 else
         {
    		 searchParams={"searchType": searchType,
				       "searchText": searchText,
			           "sourceLocale": sourceLocale,
			           "targetLocale": targetLocale,
			           "tms": '"'+tms+'"',
			           "maxEntriesPerPage":maxEntriesPerPage,
			           "searchIn":searchIn,
			           "advancedSearch":advancedSearch,
			           "replaceText":replaceText,
			           "createStartDateOption":createStartDateOption,
			           "createStartDate":createStartDate,
			           "createEndDateOption":createEndDateOption,
			           "createEndDate":createEndDate,
			           "modifyStartDateOption":modifyStartDateOption,
			           "modifyStartDate":modifyStartDate,
			           "modifyEndDateOption":modifyEndDateOption,
			           "modifyEndDate":modifyEndDate,
			           "lastUsageStartDateOption":lastUsageStartDateOption,
			           "lastUsageStartDate":lastUsageStartDate,
			           "lastUsageEndDateOption":lastUsageEndDateOption,
			           "lastUsageEndDate":lastUsageEndDate,
			           "jobIds":jobIds,
			           "tuIds":tuIds,
			           "sids":sids,
			           "isRegex":isRegex,
			           "createUser":createUser,
			           "modifyUser":modifyUser,
			           "attributeName":attributeName,
			           "attributeValue":attributeValue};
    	 }
    	 
    	 $("#pageNavigationHeader").html("");
    	 $("#searchResult").html("");
    	 $("#pageNavigationFooter").html("");
    	 $("#pageStatus").html("");
    	 $("#information").html("");
    	 $("#loading").html(loading);
    	 
    	 $.ajax({
  		   type: "POST",
  		   url: "<%=searchUrl%>",
		   dataType : 'json',
		   cache : false,
		   data : searchParams,
		   success : function(json) {
			 if (json != null && json.totalNum != 0) 
			 {
			   totalNum = json.totalNum;
			   if("All"==maxEntriesPerPage)
			   {
				   totalPage = 1;
			   }
			   else
			   {
				   totalPage = Math.ceil(totalNum/maxEntriesPerPage);
			   }
			   $("#loading").html("");
			   result = json.result;
			   resultDisplay(1);
			 } 
			 else 
			 {
			   showNoResults();
			 }
		   }
		 });
	});
});
function isInteger(obj)
{
    return Math.floor(obj) == obj;
}
$(window).unload(function(){
	if(editWindow)
	{
		editWindow.close();
	}
	if(addWindow)
	{
		addWindow.close();
	}
});
function disableCreateOption(){
	createStartDateOption = $("#createStartDateOption").val();
	if(createStartDateOption == "eq")
	{
		$("#createEndDateOption").attr("disabled",true);
		$("#cef").attr("value","");
		$("#cef").attr("disabled",true);
	}
	else if(createStartDateOption == "neq")
	{
		$("#createEndDateOption").attr("disabled",true);
		$("#cef").attr("value","");
		$("#cef").attr("disabled",true);
	}
	else
	{
		$("#createEndDateOption").attr("disabled",false);
		$("#cef").attr("value","");
		$("#cef").attr("disabled",false);
	}
}
function disableModifyOption(){
	modifyStartDateOption = $("#modifyStartDateOption").val();
	if(modifyStartDateOption == "eq")
	{
		$("#modifyEndDateOption").attr("disabled",true);
		$("#mef").attr("value","");
		$("#mef").attr("disabled",true);
	}
	else if(modifyStartDateOption == "neq")
	{
		$("#modifyEndDateOption").attr("disabled",true);
		$("#mef").attr("value","");
		$("#mef").attr("disabled",true);
	}
	else
	{
		$("#modifyEndDateOption").attr("disabled",false);
		$("#mef").attr("value","");
		$("#mef").attr("disabled",false);
	}
}

function disableLastUsageOption(){
	lastUsageStartDateOption = $("#lastUsageStartDateOption").val();
	if(lastUsageStartDateOption == "eq")
	{
		$("#lastUsageEndDateOption").attr("disabled",true);
		$("#luef").attr("value","");
		$("#luef").attr("disabled",true);
	}
	else if(lastUsageStartDateOption == "neq")
	{
		$("#lastUsageEndDateOption").attr("disabled",true);
		$("#luef").attr("value","");
		$("#luef").attr("disabled",true);
	}
	else
	{
		$("#lastUsageEndDateOption").attr("disabled",false);
		$("#luef").attr("value","");
		$("#luef").attr("disabled",false);
	}
}
</script>
</head>
<body>
	<%@ include file="/envoy/common/header.jspIncl"%>
	<%@ include file="/envoy/common/navigation.jspIncl"%>
	<%@ include file="/envoy/wizards/guides.jspIncl"%>
	<div id="contentLayer"
		style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
		<table cellspacing="0" cellpadding="0" border="0" class="standardTextNew">
		<tr>
		  <td style="background: none repeat scroll 0 0 #0C1476;color: white;">
		    <img border="0" src="/globalsight/images/tab_left_blue.gif">
		    <span style="font-family:Arial, Helvetica, sans-serif;font-size: 8pt;font-weight:bold;color:white;">${lb_tm_search}</span>
		    <img border="0" src="/globalsight/images/tab_right_blue.gif">	
		  </td>
		  <td width="2"></td>
		  <td id="termSearchTD" style="background: none repeat scroll 0 0 #708EB3;boder:0;color: white;">
		   <img border="0" src="/globalsight/images/tab_left_gray.gif">
           <a class="sortHREFWhite" href="javascript:directTermSearchPage('<%=tbSearchUrl %>')">${lb_terminology_search_entries}</a>
           <img border="0" src="/globalsight/images/tab_right_gray.gif">	
		  </td>
		</tr>
		</table>
		<table width=100%>
		  <tr>
			  <td>
			    <table cellspacing="0" cellpadding="1" border="0" style="background-color:#738EB5;width:100%">
				  <tr>
				    <td>
					  <table cellspacing="0" cellpadding="0" border="0" style="background:#DEE3ED;width:100%">
					    <tr style="background: none repeat scroll 0 0 #DEE3ED;">
						  <td>
							<table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
							  <tr>
							    <td class="search_content" nowrap >${lb_tm_search_type}:
							      <select id="searchType">
								    <option value="fullTextSearch">${lb_corpus_searchFT}</option>
									<option value="matchSearch">${lb_corpus_searchFZ}</option>
							      </select>
							    </td>
							    <td id="searchInTD" class="search_content" nowrap style="display:none">${lb_search_in}:
							      <select id="searchIn">
								    <option value="source" selected>${lb_source}</option>
									<option value="target">${lb_target}</option>
							      </select>
							    </td>
							    <td id="searchCondition" class="search_content" nowrap>
							        ${lb_tm_search_text}:<input id="searchText" type="text" style="width:590px"></input>
							    </td>
							    <td id="replaceCondition" style="display: none;" class="search_content" nowrap>
							       ${lb_replace_with}: <input id="replaceText" type="text"></input>
							    </td>
							    <td id="searchBtnTD" class="search_content" nowrap width="100%">
							      <table cellspacing="0" cellpadding="0" style="border:0px solid black">
							        <tr valign="middle">
							          <td>
							            <input id="search" type="button" class="button_out" title="Search" style="width: 60px;background-image: url(images/search.png); "></input>
							          </td>
							        </tr>
							      </table>
								</td>
								<td id="advancedLinkTD" class="search_content" width="100%" nowrap style="display:none">
								  <a id="advanced" class="link" href="#" >${lb_advanced}...</a>
								  <a id="simple" class="link" style="display: none;" href="#">${lb_simple}...</a>
							  </tr>
						    </table>
						  </td>
						</tr>
						<tr style="background: none repeat scroll 0 0 #DEE3ED;">
						  <td>
							<table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
							  <tr>
							    <td class="search_content" id="tpSelect" style="display: none;" nowrap>${lb_corpus_tmprofile}:
							       <select id="tmps" style="width:200px"></select>
							    </td>
							    <td class="search_content" id="tmSelect" nowrap>${lb_tms}:
							     <input id="tmsInput" class="choose" type="button" value="" title=""></input>
							    </td>
							    <td class="search_content" nowrap>${lb_source_locale}: <select id="sourceLocale"></select></td>
								<td class="search_content" nowrap width="100%">${lb_target_locale}: <select id="targetLocale"></select></td>
							  </tr>
							</table>
						  </td>
						</tr>
						<tr style="background: none repeat scroll 0 0 #DEE3ED;display: none;" id ="advancedSearch">
						  <td>
							<table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
							  <tr>
							    <td class="search_content" nowrap>
							    	${lb_export_sid}: <input type="text" id="sids" name="sids" value=""/>&nbsp;&nbsp;<input type="checkbox" id="isRegex" name="isRegex"/>${lb_export_regex}
      							</td>
							    <td class="search_content"nowrap>
							       ${lb_export_tu_id}: <input type="text" id="tuIds" name="tuIds" value=""/>
							    </td>
      							<td class="search_content" nowrap>
							     	Attribute Name: <input type="text" id="attributeName" name="attributeName" value=""/>
      							</td>
      							<td class="search_content" width="100%" nowrap>
							     	Attribute Value: <input type="text" id="attributeValue" name="attributeValue" value=""/>
      							</td>
							  </tr>
							 </table>
							 <table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
							  <tr>
							  	<td class="search_content" nowrap>
							     	${lb_export_create_user}: <input type="text" id="createUser" name="createUser" value=""/>
      							</td>
      							<td class="search_content"   nowrap>
							     	${lb_export_modify_user}: <input type="text" id="modifyUser" name="modifyUser" value=""/>
      							</td>
      							<td class="search_content"  width="100%" nowrap >
							     	${lb_job_id}: <input type="text" id="jobIds" name="jobIds" value=""/>
      							</td>
							  </tr>
							 </table>
							 <table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
							  <tr>
							    <td class="search_content" nowrap>
							    	${lb_created_on }
							    </td>
							    <td class="search_content" nowrap>${lb_report_startDate}: 
							    	<select id="createStartDateOption" onchange="disableCreateOption()">
							       		<option value="eq" id="eqStart">${lb_equal_to}</option>
							       		<option value="neq">${lb_not_equal_to}</option>
							       		<option value="gt"  id="gtStart">${lb_greater_than}</option>
							       		<option value="gteq"  id="gteqStart">${lb_greater_than_or_equal_to}</option>
							       </select>
							    </td>
							    <td class="search_content">
							   	    <input type="text" name="csf" id="csf">
      							</td>
							    <td class="search_content" nowrap>${lb_report_endDate}: 
							   		<select id="createEndDateOption">
							       		<option value="lt" id="ltEnd">${lb_less_than}</option>
							       		<option value="lteq" id="lteqEnd">${lb_less_than_or_equal_to}</option>
							       </select>
							    </td>
							    <td class="search_content">
							    	<input type="text" name="cef"  id="cef"">
      							</td>
      							<td class="search_content"  width="100%">
      								<span class='info'>(MM/DD/YYYY)</span>
      							</td>
							  </tr>
							  <tr>
							  	<td class="search_content" nowrap>
							  		${lb_modified_on}
							    </td>
							    <td class="search_content" nowrap>${lb_report_startDate}: 
							    	<select id="modifyStartDateOption" onchange="disableModifyOption()">
							       		<option value="eq" id="eqStart">${lb_equal_to}</option>
							       		<option value="neq">${lb_not_equal_to}</option>
							       		<option value="gt"  id="gtStart">${lb_greater_than}</option>
							       		<option value="gteq"  id="gteqStart">${lb_greater_than_or_equal_to}</option>
							       </select>
							    </td>
							    <td class="search_content">
							   	    <input type="text" name="msf" id="msf">
      							</td>
							    <td class="search_content" nowrap>${lb_report_endDate}: 
							   		<select id="modifyEndDateOption">
							       		<option value="lt" id="ltEnd">${lb_less_than}</option>
							       		<option value="lteq" id="lteqEnd">${lb_less_than_or_equal_to}</option>
							       </select>
							    </td>
							    <td class="search_content">
							    	<input type="text" name="mef"  id="mef"">
      							</td>
      							<td class="search_content"  width="100%">
      								<span class='info'>(MM/DD/YYYY)</span>
      							</td>
							  </tr>
							  <tr>
							  	<td class="search_content" nowrap>
							  		${lb_last_usage_date}
							    </td>
							    <td class="search_content" nowrap>${lb_report_startDate}: 
							    	<select id="lastUsageStartDateOption" onchange="disableLastUsageOption()">
							       		<option value="eq" id="eqStart">${lb_equal_to}</option>
							       		<option value="neq">${lb_not_equal_to}</option>
							       		<option value="gt"  id="gtStart">${lb_greater_than}</option>
							       		<option value="gteq"  id="gteqStart">${lb_greater_than_or_equal_to}</option>
							       </select>
							    </td>
							    <td class="search_content">
							   	    <input type="text" name="lusf" id="lusf">
      							</td>
							    <td class="search_content" nowrap>${lb_report_endDate}: 
							   		<select id="lastUsageEndDateOption">
							       		<option value="lt" id="ltEnd">${lb_less_than}</option>
							       		<option value="lteq" id="lteqEnd">${lb_less_than_or_equal_to}</option>
							       </select>
							    </td>
							    <td class="search_content">
							    	<input type="text" name="luef"  id="luef"">
      							</td>
      							<td class="search_content"  width="100%">
      								<span class='info'>(MM/DD/YYYY)</span>
      							</td>
							  </tr>
							</table>
						  </td>
						</tr>
					  </table>
				    </td>
			      </tr>
			    </table>
			  </td>
			</tr>
			<tr>
			  <td>
			    <table class="standardTextNew" width=100%>
			      <tr>
					<td id="buttons" align="left"></td>
                    <td>&nbsp;<span id="information" width=100%></span></td>
					<td align="right" nowarp>
					  <span id="pageStatus"></span>&nbsp;&nbsp;&nbsp;&nbsp;<span id="pageNavigationHeader"></span>
					</td>
				  </tr>
			    </table>
			  </td>
			</tr>
			<tr id="searchResultDiv">
				<td>
					<table class="standardTextNew" width=100%>
						<tr>
							<td colspan=3 id="searchResult">
							<table id="rData" cellspacing="0" cellpadding="3" class="standardTextNew" width="100%">
							  <tr class="tableHeadingBasic">
							    <th style="border-left: #0C1476 1px solid;border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;color:#0C1476"><input type="checkbox"></input></th>
							    <th width="37%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_source}</th>
							    <th width="37%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_target}</th>
							    <th width="14%" align="left" style="border-right: #FFFFFF 1px solid;border-bottom: #FFFFFF 1px solid;">${lb_sid}</th>
							    <th width="12%" align="left" style="border-bottom: #FFFFFF 1px solid;">${lb_tm_name}</th>
							  </tr>
							</table>
							${lb_tm_search_hint}
							</td>
						</tr>
						<tr>
						    <td colspan=3 align="right">
						    ${lb_tm_search_display} #:
						    <select id="maxEntriesPerPage">
						      <option>10</option>
						      <option>20</option>
						      <option selected>50</option>
						      <option>100</option>
						      <option>All</option>
						    </select>
						    &nbsp;&nbsp;&nbsp;&nbsp;
						    <span id="pageNavigationFooter"></span>
						</tr>
					</table></td>
			</tr>
		</table>
		<div id="loading"></div>
		<div id="tmsDiv" class="tmsDivPop"></div>
		<div id='mask'></div>
		<div id="tmsTitle" class="tip"></div>
	</div>
</body>
</html>