<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
     ResourceBundle bundle = PageHandler.getBundle(session);
     String urlTermSearch = self.getPageURL();
     String searchURL = urlTermSearch + "&" + WebAppConstants.TERMBASE_ACTION + "="+WebAppConstants.TERMBASE_ACTION_SEARCH;
     String pagingURL = urlTermSearch+ "&" + WebAppConstants.TERMBASE_ACTION + "="+WebAppConstants.TERMBASE_ACTION_TERM_SEARCH_PAGING;
     String orderingURL = urlTermSearch+ "&" + WebAppConstants.TERMBASE_ACTION + "="+WebAppConstants.TERMBASE_ACTION_TERM_SEARCH_ORDERING;
%>

<HTML>
<HEAD>
<TITLE><%=bundle.getString("permission.terminology.search") %></TITLE>
<STYLE>

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

.tableHeadingBasic {
    background: none repeat scroll 0 0 #0C1476;
    color: white;
    font-family: Arial,Helvetica,sans-serif;
    font-size: 8pt;
    font-weight: bold;
}

.mainHeading {
    color: #0C1476;
    font-size: 11pt;
    font-weight: bold;
}

.search_content {
    border-bottom: 1px solid white;
    border-right: 1px solid white;
}

#searchText
{
 width:570px;
}
#sourceLocale, #targetLocale
{
 width:270px;
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

.tbsDivPop {
margin-bottom: 3px;
display: none;
position: absolute;
background:#DEE3ED;
border:solid 1px #6e8bde;
}
.choose {
    background-image: url(images/btn_choose.png);
    background-repeat:no-repeat;
    background-position:bottom right;
    border: solid 1px #85b1de;
    cursor: pointer;
    font-family: Arial,Helvetica,sans-serif;
    font-size: 12px;
    width: 200px;
    text-align:left;
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
</STYLE>

<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script LANGUAGE="JavaScript"
	src="/globalsight/includes/dnd/DragAndDrop.js"></script>
<SCRIPT type="text/javascript">

var needWarning = false;
var objectName = "Termbase";
var guideNode = "terminology";
var helpFile = "${help_termbase_search_term}";
/**orderBy number(default 5):
5: Source ASC 6: Source DESC
9: Target ASC 10: Target DESC
1: TB ASC 2: TB DESC
**/
var orderBy = 5;

var companies;
var tbsList;
var sourceLocaleText; 
var targetLocaleText; 

var totalItemInPage=10; 
var totalPage;
var totalNum;
var loading = '<center><img src="images/ajax-loader.gif"></img></center>';

/*
 * Paging navigation
 */
function makePageNavigation(currentPage)
{
  var first='loadPage(1)';
  var last='loadPage('+totalPage+')';

  var html="";
  
  if(currentPage==1)
  {
      html='<a>${lb_first}</a>|';
	  html+='<a>${lb_previous}</a>|';
  }
  else
  {
      html='<a href="javascript:'+first+';">${lb_first}</a>|';
	  html+='<a href="javascript:loadPage('+(currentPage-1)+');">${lb_previous}</a>| ';
  }

  var pageNumberSum=3;
  var status=currentPage%3;
  var begin;
  var middle;
  var end;
  if(status==1)
  {
    begin=currentPage;
  }
  else if(status==2)
  {
    begin=currentPage-1;
  }
  else
  {
    begin=currentPage-2;
  }
  middle = begin+1;
  end=begin+2;
  if(begin==currentPage)
  {
    html+='<a>'+begin+'</a> ';
  }
  else
  {
    html+='<a href="javascript:loadPage('+begin+');">'+begin+'</a> ';
  }
	 
  if(middle<=totalPage)
  { 
    if(middle==currentPage)
    {
      html+='<a>'+middle+'</a> ';
    }
    else
    {
      html+='<a href="javascript:loadPage('+middle+');">'+middle+'</a> ';
    }
  }
  if(end<=totalPage)
  {
    if(end==currentPage)
	{
	  html+='<a>'+end+'</a> ';
	}
	else
	{
	  html+='<a href="javascript:loadPage('+end+');">'+end+'</a> ';
	}
  }

  if(currentPage==totalPage)
  {
     html+='|<a>${lb_next}</a>|';
     html+='<a>${lb_last}</a>';
  }
  else
  {
     html+='<a href="javascript:loadPage('+(currentPage+1)+')">${lb_next}</a>|';
	 html+='<a href="javascript:'+last+';">${lb_last}</a>';
  }
  return html;
}

function makePageStatus(currentPage)
{
	var recordBegin = (currentPage-1)*totalItemInPage+1;
	var recordEnd = currentPage*totalItemInPage;
	if(recordEnd>totalNum)
	{
		recordEnd = totalNum;
	}
	var pageStatus = "Displaying "+recordBegin+"-"+recordEnd+" of "+totalNum;
	return pageStatus;
}

/*
 * Load data of one page
 */
function loadPage(page)
{
	$("#pageNavigationHeader").html("");
	$("#searchResult").html("");
	$("#pageNavigationFooter").html("");
	$("#pageStatus").html("");
	$("#loading").html(loading);
	var searchParams = {"page": page};
	$.ajax({
		   type: "POST",
		   url: "<%=pagingURL%>",
		   dataType:'json',
		   cache:false,
		   data: searchParams,
		   success: function(json){
			   $("#loading").html("");
			   resultDisplay(page, json);
		   }
		});
}

/*
 * Order
 */
function loadOrder()
{
  $("#pageNavigationHeader").html("");
  $("#searchResult").html("");
  $("#pageNavigationFooter").html("");
  $("#pageStatus").html("");
  $("#loading").html(loading);
  var searchParams = {"orderBy": orderBy};
  $.ajax({
     type: "POST",
     url: "<%=orderingURL%>",
     dataType:'json',
     cache:false,
     data: searchParams,
		   success: function(json){
			   $("#loading").html("");
			   resultDisplay(1, json);
		   }
     });
}

function resultDisplay(page, result)
{
	var buf = new Array();
	buf.push('<table id="rData" cellspacing="0" cellpadding="3" class="standardTextNew" width="100%"  style="border: 1px solid #0C1476;background:#FFFFFF;">');
	buf.push('<tr class="tableHeadingBasic">');
	buf.push('<th width="40%" align="left"><span id="sourceTitle" style="cursor:pointer;">'+sourceLocaleText+'</span>');
	if(orderBy==5)
	{
		buf.push('<IMG SRC="/globalsight/images/sort-up.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>');
	}
	else if(orderBy==6)
	{
		buf.push('<IMG SRC="/globalsight/images/sort-down.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>');
	}
	buf.push('</th>');
	buf.push('<th width="40%" align="left"><span id="targetTitle" style="cursor:pointer;">'+targetLocaleText+'</span>');
	if(orderBy==9)
	{
		buf.push('<IMG SRC="/globalsight/images/sort-up.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>');
	}
	else if(orderBy==10)
	{
		buf.push('<IMG SRC="/globalsight/images/sort-down.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>');
	}
	buf.push('</th>');
	buf.push('<th width="20%" align="left"><span id="tbTitle" style="cursor:pointer;">${lb_termbase}</span>'); 
	if(orderBy==1)
	{
		buf.push('<IMG SRC="/globalsight/images/sort-up.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>');
	}
	else if(orderBy==2)
	{
		buf.push('<IMG SRC="/globalsight/images/sort-down.gif" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>');
	}
	buf.push('</th></tr>');
	for(var i=0;i<result.length;i++)
    {
	  var obj = result[i];
	  buf.push("<tr style='background:#DEE3ED;'>");
	  buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.src_term+"</td>");
	  buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.target_term+"</td>");
	  buf.push("<td style='border: #FFFFFF 1px solid;'>"+obj.tbname+"</td>");
	  buf.push("</tr>");
	}
	buf.push('</table>');
	$("#pageNavigationHeader").html(makePageNavigation(page));
	$("#searchResult").html(buf.join(""));
	$("#pageNavigationFooter").html(makePageNavigation(page));
	$("#pageStatus").html(makePageStatus(page));
}

function popupDiv(e)
{
  var winWidth = $(window).width();
  var winHeight = $(window).height();
  $("#tbsInput").pageY
  $("#tbsDiv").css({"position": "absolute", "z-index": "9999"})
  .animate({left: e.pageX+10, top: e.pageY-70, opacity: "show" }, "fast");
  $("#mask").width(winWidth).height(winHeight).show();
}

function hideDiv()
{
  $("#mask").hide();
  $("#tbsDiv").animate({left: 0, top: 0, opacity: "hide" }, "fast");
}

document.onkeydown = function (e) {
	var theEvent = window.event || e;
	var code = theEvent.keyCode || theEvent.which;
	if (code == 13) 
	{
		$("#search").click();
	}
}

function getTBsHtml(company)
{
	var bufTBS = new Array();
	bufTBS.push("<table cellpadding=4 cellspacing=0>");
	bufTBS.push("<tr class='tableHeadingBasic' style='cursor:pointer;' onmousedown=\"DragAndDrop(document.getElementById('tbsDiv'),document.getElementById('contentLayer'))\"><td><input type='checkbox' id='tbsAll'/>${lb_termbase}</td><td align='right'><span id='okTbsDiv' style='cursor:pointer;'>[${lb_ok}]</span></td></tr>");
    
	var tbsCompany =[];
	if(company==null)
	{
		tbsCompany = tbsList;
	}
	else
	{
		bufTBS.push("<tr><td class='standardTextNew'>${lb_company}</td><td><select id='company'>");
		for(var i=0;i<companies.length;i++)
		{
			if(company==companies[i])
			{
				bufTBS.push("<option value='"+companies[i]+"' SELECTED>"+companies[i]+"</option>");
			}
			else
			{
				bufTBS.push("<option value='"+companies[i]+"'>"+companies[i]+"</option>");
			}
		}
		bufTBS.push("</select></td></tr>");
		for(var i=0;i<tbsList.length;i++)
		{
			var obj = tbsList[i];
			if(company==obj.company)
			{
				tbsCompany.push(obj);
			}
		}
	}
	
	if(tbsCompany.length==0)
    {
    	bufTBS.push("<tr>");
    	bufTBS.push("<td class='standardTextNew'>No TBs can be searched.</td>");
    	bufTBS.push("</tr>");
    }
    else
    {   
    	var count = tbsCompany.length;
    	var rest =  count % 3;
	    var height = (rest == 0 ? count/3 : ((count-rest)/3 + 1)) * 25;
    	if(height>125)
    	{
    		height = 125;
    	}
    	height = height+"px";
    	bufTBS.push("<tr><td colspan=2>");
        bufTBS.push("<div class='standardTextNew' style='height:"+height+"; overflow-x:hidden;overflow-y:auto;'>");
        bufTBS.push("<table border='0' style='padding-right: 20px;'>");
        for(var i=0;i<tbsCompany.length;i=i+3)
    	{
    	   var obj1 = tbsCompany[i];
    	   var obj2 = tbsCompany[i+1]
    	   var obj3 = tbsCompany[i+2]
    	   bufTBS.push("<tr>");
    	   bufTBS.push("<td class='standardTextNew' nowrap><input type='checkbox' name='tbs' value ='"+obj1.name+"'/>"+obj1.name+"</td>");
    	   if(obj2)
    	   {
    		   bufTBS.push("<td class='standardTextNew' nowrap><input type='checkbox' name='tbs' value ='"+obj2.name+"'/>"+obj2.name+"</td>");
    	   }
    	   else
    	   {
    		   bufTBS.push("<td></td>");
    	   }
    	   if(obj3)
    	   {
    		   bufTBS.push("<td class='standardTextNew' nowrap><input type='checkbox' name='tbs' value ='"+obj3.name+"'/>"+obj3.name+"</td>");
    	   }
    	   else
    	   {
    		   bufTBS.push("<td></td>");
    	   }
    	   bufTBS.push("</tr>");
    	}
        bufTBS.push("</table>");
    	bufTBS.push("</div>");
    	bufTBS.push("</td><tr>");
    }
    
    bufTBS.push("</table>");
    return bufTBS;
}


function init()
{
  var locales = $.parseJSON('${locales}');
  tbsList = $.parseJSON('${tbs}');
  companies = $.parseJSON('${companies}');
    
  //Permssion
  var hasTMSearchPermission = ${hasTMSearchPermission};
  if(!hasTMSearchPermission)
  {
	  $("#tmSearchTD").hide();
  }
  //Set tbs 
  var bufTBS;
  if(companies!=null)
  {
	bufTBS = getTBsHtml(companies[0]);
  }
  else
  {
	bufTBS = getTBsHtml(null);
  }
  $("#tbsDiv").html(bufTBS.join(""));
  
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
}

function directTMSearchPage(pageUrl)
{
  var searchText = $("#searchText").val()
  var directTo = pageUrl
	  +"&fromTermSearchPage=fromTermSearchPage"
	  +"&sourceLocale="+$("#sourceLocale").val()
	  +"&targetLocale="+$("#targetLocale").val()
	  +"&searchText="+searchText;
  window.location=directTo;
}

function shareConditionTMAndTermSearch()
{
  if('${fromTMSearchPage}')
  {
	$("#sourceLocale").val('${sourceLocale}');
	$("#targetLocale").val('${targetLocale}');
	$("#searchText").val('${searchText}');
  }
}

$(document).ready(function(){
  
  loadGuides(); 
  init();
  shareConditionTMAndTermSearch();
  
  $("#tbsInput").click(function(e){
	   popupDiv(e);
  })
  
  $("#tbsInput").hover(function(e){
    	 if($("#tbsInput").val()!="")
    	 {
    		 var buf="";
    		 var tbs ="";
        	 $("[name='tbs']:checked").each(function(){ 
        		 buf=buf+$(this).val()+"<br>"
        	 }) 
        	 buf = buf.substring(0, buf.length-4);
    		 $("#tbsTitle").html(buf); 
    		 var excursion = 20;
    		 if($.browser.mozilla)
    			 excursion=10;
    		 var left = $("#tbsInput").offset().left+$("#tbsInput").width()-excursion;
    		 var top = $("#tbsInput").offset().top-90; 
    		 $("#tbsTitle").css({"z-index":9999,"top":(top)+"px","left":(left)+"px"});
    		 $("#tbsTitle").show();
    	 }
     }).mouseout(function(){
	         $('#tbsTitle').hide();
     })
  

  $("#tbsAll").live("click",function(){
		 if($("#tbsAll").attr("checked")) 
		 {
			 $("input[name='tbs']").attr("checked","true");
		 }
		 else
		 {
			 $("input[name='tbs']").removeAttr("checked"); 
		 }
     });
  
  $("#company").live("change", function(){
 	 var bufTBS = getTBsHtml($("#company").val());
 	 $("#tbsDiv").html(bufTBS.join(""));
  })
  
  $("#okTbsDiv").live("click",function(){
	 var tbs ="";
	 $("[name='tbs']:checked").each(function(){ 
		 tbs+=$(this).val()+",";
	 }) 
	 if(tbs!="")
	 {
		 tbs = tbs.substring(0, tbs.length-1);
		 var tbsStr = tbs;
		 if(tbsStr.length>23)
   	     {
   		   var temp = tbsStr.substring(0,23);
   	       $("#tbsInput").attr("value", temp+"...");
   	     }
   	     else
   	     {
   		   $("#tbsInput").attr("value", tbsStr);
   	     }
	 }
	 else
	 {
		 $("#tbsInput").attr("value", "");
		 $("#tbsInput").attr("value", "");
	 }
	 hideDiv();
  })
  
  $("#search").click(function(){
 	 var searchText = $("#searchText").val();
 	 var sourceLocale = $("#sourceLocale").val();
 	 sourceLocaleText=$("#sourceLocale").find("option:selected").text(); 
 	 var targetLocale = $("#targetLocale").val();
 	 targetLocaleText=$("#targetLocale").find("option:selected").text(); 

 	 var tbs ="";
 	 $("[name='tbs']:checked").each(function(){ 
 		 tbs+=$(this).val()+",";
 	 }) 

 	 if(searchText=="")
 	 {
 		 alert("${jsmsg_tb_maintenance_search_string_empty}");
 		 return;
     }
 		 
 	 if(sourceLocale==-1)
      {
 		 alert("${jsmsg_tb_maintenance_search_srclocale_empty}");
 		 return;
      }
 	 if(targetLocale==-1)
 	 {
 		 alert("${jsmsg_tb_maintenance_search_tgtlocale_empty}");
 		 return;
 	 }
 	 
 	
 	 if(tbs=="")
     {
 	    alert("${jsmsg_tb_maintenance_search_tm_empty}");
 	    return;
     } 
 	 tbs = tbs.substring(0, tbs.length-1);
 	 
 	 var matchType=$("#matchType").val();
 	 var searchParams={"matchType": matchType,
				       "searchText": searchText,
			           "sourceLocale": sourceLocale,
			           "targetLocale": targetLocale,
			           "tbs": tbs};
 	 
 	 $("#pageNavigationHeader").html("");
 	 $("#searchResult").html("");
 	 $("#pageNavigationFooter").html("");
 	 $("#pageStatus").html("");
 	 
 	 $("#loading").html(loading);
 	 
 	 $.ajax({
		   type: "POST",
		   url: "<%=searchURL%>",
		   dataType : 'json',
		   cache : false,
		   data : searchParams,
		   success : function(json) {
			 if (json != null && json.totalNum != 0) 
			 {
			   totalNum = json.totalNum;
			   totalPage = Math.ceil(totalNum/totalItemInPage);
			   $("#loading").html("");
			   resultDisplay(1, json.result);
			 } 
			 else 
			 {
		       $("#loading").html("${lb_no_termbase_data_matches}"+"<p>");
			 }
		   }
		 });
	});
  
    $("#sourceTitle").live("click",function(){
	   if(orderBy==5)
	   {
		   orderBy=6;
	   }
	   else if(orderBy==6)
	   {
		   orderBy=5;
	   }
	   else
	   {
		   orderBy=5;
	   }
	   loadOrder();
    });
    
    $("#targetTitle").live("click",function(){
       if(orderBy==9)
 	   {
 		   orderBy=10;
 	   }
 	   else if(orderBy==10)
 	   {
 		   orderBy=9;
 	   }
 	   else
       {
 		   orderBy=9;
       }
       loadOrder();
    });
    
    $("#tbTitle").live("click", function(){
       if(orderBy==1)
 	   {
 		   orderBy=2;
 	   }
 	   else if(orderBy==2)
 	   {
 		   orderBy=1;
 	   }
 	   else
 	   {
 		  orderBy=1;
 	   }
       loadOrder();
    });
})

</SCRIPT>
</HEAD>

<BODY onload="" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
  <table cellspacing="0" cellpadding="0" border="0" class="standardTextNew">
	<tr>
      <td id="tmSearchTD" style="background: none repeat scroll 0 0 #708EB3;boder:0;color: white;">
	    <img border="0" src="/globalsight/images/tab_left_gray.gif">
	    <a class="sortHREFWhite" href="javascript:directTMSearchPage('<%=tmSearchUrl%>')">${lb_tm_search}</a>
	    <img border="0" src="/globalsight/images/tab_right_gray.gif">
	  </td>
	  <td width="2"></td>
	  	<td style="background: none repeat scroll 0 0 #0C1476;color: white;">
	    <img border="0" src="/globalsight/images/tab_left_blue.gif">
        <span style="font-family:Arial, Helvetica, sans-serif;font-size: 8pt;font-weight:bold;color:white;">${lb_terminology_search_entries}</span>
        <img border="0" src="/globalsight/images/tab_right_blue.gif">
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
               <tr>
                 <td>
	               <table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
	                 <tr>
	                   <td class="search_content" nowrap align="left">${lb_match_type}:
                         <select id="matchType" style="width:200px">
                           <option value="fuzzy">${lb_fuzzy_match}</option>
	                       <option value="exact">${lb_exact_match}</option>
	                     </select>
	                   </td>
	                   <td class="search_content" nowrap align="left" nowrap>
	                     ${lb_search_for}: 
	                     <input type="text" size="35" name="searchText" style="width:570px" id="searchText" onkeypress="if(event.keyCode==13||event.which==13){return false;}">
	                   </td>
	                   <td class="search_content" width=100%>
	                     <table cellspacing="0" cellpadding="0" style="border:0px solid black">
						   <tr valign="middle">
						     <td>
						       <input id="search" type="button" class="button_out" title="Search" style="width: 60px;background-image: url(images/search.png); "></input>
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
                  <table cellspacing="0" cellpadding="4" border="0" class="standardTextNew">
                    <tr>
	                  <td class="search_content" nowrap>${lb_termbase_select_tms}:
	                    <input id="tbsInput" class="choose" type="button" value="" title="" ></input>
	                  </td>
	                  <td class="search_content" nowrap>${lb_source_locale}: <select id="sourceLocale"></select></td>
					  <td class="search_content" width=100%>${lb_target_locale}: <select id="targetLocale"></select></td>
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

<tr id="searchResultDiv">
  <td>
	<table class="standardTextNew" width=100%>
	  <tr>
		<td id="pageStatus" align="left" width=30%></td>
		<td width=30% align="center"></td>
		<td id="pageNavigationHeader" align="right" width=30%></td>
	  </tr>
      <tr>
        <td colspan=3 id="searchResult"></td>
      </tr>
      <tr>
        <td colspan=3 id="pageNavigationFooter" align="right"></td>
	  </tr>
	</table>
  </td>
</tr>
</table>
<div id="loading"></div>
<div id="tbsDiv" class="tbsDivPop"></div>
<div id='mask'></div>
<div id="tbsTitle" class="tip"></div>
</div>
</body>
</html>
