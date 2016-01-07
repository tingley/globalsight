//main.js
var g_refreshing = false;
var showTarget = false;
var showSource = false;
var showList = false;
var showPtags = false;
var showRepeated = false;	
var w_editor;
var postReviewEditor = "postReviewEditor";
window.focus();

function helpSwitch()
{
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function SegmentFilter(p_segmentFilter)
{
    document.location = url_self+"&refresh=0&segmentFilter=" + p_segmentFilter;
}

function switchTargetLocale(p_locale)
{
	document.location = url_self+"&refresh=0&trgViewLocale=" + p_locale;
}

var comments=["Hide Comments","Show Comments"]

function reviewMode()
{
	var lable=$("#reviewMode").text();
	lable=$.trim(lable);
	
    var action="true";
    if(comments[0]==lable)
    {
    	action="false";
	}
    document.location = url_refresh+"&"+reviewModeText+"=" + action;
}

function closeWindow()
{
	window.close();
}

function CanClose()
{
	if(w_editor)
    {
    	w_editor.close();
    }
	return true;
}

function exit()
{
    try { w_pageinfo.close();  } catch (ignore) {}
    try { w_resources.close(); } catch (ignore) {}
    try { w_termbases.close(); } catch (ignore) {}
}

function refresh(direction)
{
	if(w_editor)
    {
    	w_editor.close();
    }
	document.location = url_self + "&action=refresh&refresh=" + direction+"&random="+Math.random();
}

function showPageInfo()
{
    w_pageinfo = window.open(url_pageInfo, "MEPageInfo",
      "resizable,width=400,height=400");
}

function showSupportFiles()
{
    w_resources = window.open(urlResourcesPath, "MESupportFiles",
       "height=400,width=500,resizable=yes,scrollbars=yes");
}

function showTermbases()
{
    w_termbases = window.open(url_termbases, "METermbases",
       "height=400,width=500,resizable=yes,scrollbars=yes");
}

function createLisaQAReport()
{
	var action = ShutdownForm.action;
	ShutdownForm.action = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=generateReports";
	ShutdownForm.reportType.value = "CommentsAnalysisReport";
	ShutdownForm.submit();
	ShutdownForm.action = action;
	ShutdownForm.reportType.value = "";
}

function createCharacterCountReport()
{
	var action = ShutdownForm.action;
    ShutdownForm.action = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=generateReports";
    ShutdownForm.reportType.value = "CharacterCountReport";
    ShutdownForm.submit();
    ShutdownForm.action = action;
	ShutdownForm.reportType.value = "";
}

// This is invoked after me_target.jsp is finished loading to avoid error 
// from quick file navigation.
function updateFileNavigationArrow()
{
	if (isFirstPage == 'false')
	{
		fileNavPre = "<A HREF='#' onclick='refresh(-1); return false;' onfocus='this.blur()'>"
			+ "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
		document.getElementById("fileNavPre").innerHTML = fileNavPre;
	}

	if (isLastPage == 'false')
	{
        fileNavNext = "<A HREF='#' onclick='refresh(1); return false;' onfocus='this.blur()'>"
            + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
        document.getElementById("fileNavNext").innerHTML = fileNavNext;
	}
}

function updatePageNavigationArrow()
{
    if (isFirstBatch == 'false' || isFirstBatch == false)
    {
        pageNavPre = "<A HREF='#' onclick='refresh(-11); return false;' onfocus='this.blur()'>"
            + "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
        document.getElementById("pageNavPre").innerHTML = pageNavPre;
    }
    
	if (isLastBatch == 'false' || isLastBatch == false)
	{
        pageNavNext = "<A HREF='#' onclick='refresh(11); return false;' onfocus='this.blur()'>"
            + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
        document.getElementById("pageNavNext").innerHTML = pageNavNext;
	}
}

function EnterPress(e)
{
	var e = e || window.event;
	if(e.keyCode == 13)
	{ 
		var gotoPage = "0" + document.getElementById("gotoPageNav").value;
		var gotoPageNum = document.getElementById("gotoPageNav").value;
		var totalPageNum = tempTotalPageNum;
		if(isNaN(gotoPageNum) || (gotoPageNum.indexOf(".")>0))
	    {
			alert("Invalid number !");
			return;
			
	    }
		else
		{
	    	if(parseInt(totalPageNum)>=parseInt(gotoPageNum)&&parseInt(gotoPageNum)>0)
	    	{
		    	refresh(gotoPage);
	     	}
	    	else
	    	{
		    	alert("The input number should be between 1 and maximum page number !");
		    	return;
		    }
	    }
	}
}

var parseQueryString = function(_query,match) {
    var args = new Object();
    var pairs = _query.split(match);
    for (var i = 0; i < pairs.length; i++) {
        var pos = pairs[i].indexOf("=");
        if (pos == -1) continue;
        var argname = pairs[i].substring(0, pos);
        var value = pairs[i].substring(pos + 1);
        args[argname] = value;
    }
    return args;
}

var PtagLable=[marklable,unmarklable];
function showPtagsTest()
{
	var lable=$("#showPtags").text();
	if(PtagLable[0]==lable)
	{
		$("#showPtags").text(PtagLable[1]);
		showPtags = true;
	}
	else
	{
		$("#showPtags").text(PtagLable[0]);
		showPtags = false;;
	}
	getDataByFrom(url + "&pTagsAction=" + lable);
}

function checkAll()
{
	if($("#checkAll").is(":checked"))
	{
		$("input[name='approveCheckBox']").attr("checked","true"); 
	}
	else
	{
		$("input[name='approveCheckBox']").removeAttr("checked"); 		
	}
}

function approve()
{
	var approveIds = "";
	$("input[name='approveCheckBox']").each(function(){
		if($(this).is(":checked"))
		{
			approveIds = approveIds + $(this).attr("id") + ",";
		}
	});
	
	if(approveIds == "")
	{
		alert("Please select some segments.");
		return false;
	}
	
	$.getJSON(url_self, 
	{
		action:"approve",
		approveIds:approveIds,
		random:Math.random()
	}, function(data){
		alert("Approval is done.");
		setInterval(getDataByFrom(url),1000);
	});
}

function unapprove()
{
	var unApproveIds = "";
	$("input[name='approveCheckBox']").each(function(){
		if($(this).is(":checked"))
		{
			unApproveIds = unApproveIds + $(this).attr("id") + ",";
		}
	});
	
	if(unApproveIds == "")
	{
		alert("Please select some segments.");
		return false;
	}
	
	$.getJSON(url_self, 
	{
		action:"unapprove",
		unApproveIds:unApproveIds,
		random:Math.random()
	}, function(data){
		alert("Un-approval is done.");
		setInterval(getDataByFrom(url),1000);
	});
}

function revert()
{
	var revertIds = "";
	$("input[name='approveCheckBox']").each(function(){
		if($(this).is(":checked"))
		{
			revertIds = revertIds + $(this).attr("id") + ",";
		}
	});
	
	if(revertIds == "")
	{
		alert("Please select some segments.");
		return false;
	}
	
	$.getJSON(url_self, 
	{
		action:"revert",
		revertIds:revertIds,
		random:Math.random()
	}, function(data){
		alert("Reversion is done.");
		setInterval(getDataByFrom(url),1000);
	});
}

var modeId="";
var modeFrom = "source";
var segFilter="";
var jsonUrl=url_self+"&dataFormat=json"+"&srcViewMode=" + modeId+"&random="+Math.random();
var localData;
var isReviwMode;
var url;
var trnode=$("<tr class='ul'><td width='25'></td><td class='segtd segmentTd'></td><td class='segtd segmentTd'></td><td class='segtd segmentTd'></td></tr>");
if(approveAction == "true")
{
	trnode=$("<tr class='ul'><td width='25'></td><td class='segtd segmentTd'></td><td class='segtd segmentTd'></td><td class='segtd segmentTd'></td><td width='50'></td></tr>");
}
var repNode=$("<td class='rep'></td>");
var subnode=$("<tr><td style='font-size: 10pt' nowrap=''></td><td></td></tr>")
var subtable=$("<table width='100%' cellspacing='0' cellpadding='2'><colgroup><col width='1%' valign='TOP' class='editorId'><col width='99%' valign='TOP'></colgroup><tbody></tbody></table>")
var SEnode=$("<a></a>")
var spanNode=$("<span class='editorText'></span>");
var lazy=100;
var max=100;
var idPageHtml;
var lbid;
var srcViewMode;
var inner_reviewMode=false;
var searchBySid;
var searchByUser;
var setToNormal;
var isIncontextReview = false;
var init = true;
$(
	function()
	{
		lbid=$(".lbid");
		if(lbid.length)
		{
			inner_reviewMode= g_reviewMode;
		}
		idPageHtml=$("#idPageHtml");
		args= parseQueryString(window.location.href,"&");
		reuseData=args.reuseData;
		srcViewMode=args.srcViewMode;
		searchBySid=args.searchBySid;
		searchByUser = args.searchByUser;
		setToNormal = args.setToNormal;
		
		var isNull = false;
		if (typeof(args.pageName) == "undefined")
		{
			isNull = true;
		}
		
		isIncontextReview = isNull ? false : (args.pageName.indexOf("inctxrv") == 0);
		var pageName=isNull ? "target"  : ((args.pageName.indexOf("ED4") >= 0)?"source":"target");
		
		updateFileNavigationArrow();
		
		url=jsonUrl;
		getDataByFrom(url);
	}
); 

function getDataByFrom(url){
	jsonUrl=url;
	$.getJSON(url+"&action=getData&random="+Math.random(), function(data) {
		localData = data;
		buildData(localData);
	});
}

function buildData(data){
	var idPageHtml=$("#idPageHtml");
	idPageHtml.html("");
	if(data == "null" || data == null)
	{
		$("#currentPageNum").html(1);
		$("#totalPageNum").html(1);
		tempTotalPageNum = 1;
		isFirstBatch = true;
		isLastBatch = true;
	}
	else
	{
		$("#currentPageNum").html(data.currentPageNum);
		$("#totalPageNum").html(data.totalPageNum);
		tempTotalPageNum = data.totalPageNum;
		isFirstBatch = data.isFirstBatch;
		isLastBatch = data.isLastBatch;
	}
	updatePageNavigationArrow();
	$(".repstyle").remove();
	$(".commentstyle").remove();
	localData=data;
	max = localData.source.length;
	recursion(localData,0);
}

function recursion(data,beginIndex){
	var sourceData = data.source;
	var targetData = data.target;
	var approveData = data.approve;
	var originalTargetData = data.original;
	var _count = 0;
	for(var i = beginIndex;_count <lazy && i <max; i++){
		_count++;
		renderHtml(sourceData[i], originalTargetData[i], targetData[i], approveData[i]);
	}
	
	if(i<max){
		setTimeout(function(){
			recursion(data,i);
		}, 100);
	}else{
		$(".ul:even").addClass("alt");
	}
}

function renderHtml(sourceData, originalTargetData, targetData, approveData){
	var idPageHtml=$("#idPageHtml");
	var temp=trnode.clone(true);
	var displayId = true;
	
	//id
	temp.children('td').eq(0).attr("id","seg"+sourceData.tuId);
	temp.children('td').eq(0).text(displayId? sourceData.tuId : "");

	//source
	temp.children('td').eq(1).attr("id","seg"+sourceData.tuId+"_"+sourceData.tuvId+"_"+sourceData.subId);

	if(sourceData.mainstyle.match("isHighLight")){
		temp.children('td').eq(1).attr("bgColor","yellow");
	}

	if(sourceData.mainstyle.match("rtl")){
		temp.children('td').eq(1).attr("dir","rtl");
	}
	
	var htmlcontent=getNodeByClass(sourceData, "source");
	if(sourceData.subArray){
		temp.children('td').eq(1).html("<table width='100%' cellspacing='0' cellpadding='2'>"+htmlcontent.html()+"</table>");
	}else{
		temp.children('td').eq(1).append(htmlcontent);
	}
	
	if(sourceData.mainstyle.match("rtl")){
		temp.children('td').eq(1).children("a").children("span").attr("dir","rtl");
	}
	
	//Original
	htmlcontent=getNodeByClass2(originalTargetData, "");
	if(htmlcontent.html() != "-")
	{
		var otd = document.getElementById("previous_translation");
		otd.style.width = "30%";
	}
	if(originalTargetData.subArray){
		temp.children('td').eq(2).html("<table width='100%' cellspacing='0' cellpadding='2'>"+htmlcontent.html()+"</table>");
	}else{
		temp.children('td').eq(2).append(htmlcontent);
	}
	
	if(originalTargetData.mainstyle.match("rtl")){
		temp.children('td').eq(2).attr("dir","rtl");
	}
	
	if(originalTargetData.originalTarget == "")
	{
		temp.children('td').eq(2).addClass("center");
	}
	
	//target
	temp.children('td').eq(3).attr("id","seg"+targetData.tuId+"_"+targetData.tuvId+"_"+targetData.subId);

	if(targetData.mainstyle.match("isHighLight")){
		temp.children('td').eq(3).attr("bgColor","yellow");
	}

	if(targetData.mainstyle.match("rtl")){
		temp.children('td').eq(3).attr("dir","rtl");
	}
	
	htmlcontent=getNodeByClass(targetData, "target");
	if(targetData.subArray){
		temp.children('td').eq(3).html("<table width='100%' cellspacing='0' cellpadding='2'>"+htmlcontent.html()+"</table>");
	}else{
		temp.children('td').eq(3).append(htmlcontent);
	}
	
	if(targetData.mainstyle.match("rtl")){
		temp.children('td').eq(3).children("a").children("span").attr("dir","rtl");
	}
	
	if(approveAction == "true")
	{
		var approveId = targetData.tuId+"_"+targetData.tuvId+"_"+targetData.subId;
		temp.children('td').eq(4).html("<input id='" + approveId + "' name='approveCheckBox' type='checkbox'>");
	}

	idPageHtml.append(temp);
}

function getNodeByClass(item, se_able){
	var temp;
	// if "mainstyle.match("SE ")", it should display right click context menus.
	var scriptFlag = inner_reviewMode || item.mainstyle.match("SE ");

	if(scriptFlag){
		temp=SEnode.clone(true);
		var funP="javascript:SE("+item.tuId+","+item.tuvId+","+item.subId+")";
		temp.attr("href",funP);
	}else{
		temp=spanNode.clone(true);
	}

	temp.html(item.segment);
	//just for 
	if(segFilter.match("FilterICE"))
	{
		temp.addClass("segmentContext");
	}
	else if(segFilter.match("Filter100") 
			&& !(item.mainstyle.match("editorSegmentMT")) 
			&& !(item.mainstyle.match("editorSegmentExact"))
			&& !(item.mainstyle.match("editorSegmentUpdated")))
	{
		temp.addClass("editorSegmentLocked");
	}
	else
	{
		temp.addClass(item.mainstyle.replace("editorComment", "").replace("isHighLight", ""));
	}
	
	temp.addClass("noUnderline");

	if(item.subArray){
		var stable=subtable.clone(true);
		stable.append(temp);
		$.each(item.subArray,function(j){
			var sub=subnode.clone(true);
			sub.children('td').eq(0).text(item.tuId+"."+this.subId);
			var span;
			if(scriptFlag){
				span=SEnode.clone(true);
				var funP="javascript:SE("+item.tuId+","+item.tuvId+","+this.subId+")";
				span.attr("href",funP);
			}else{
				span=spanNode.clone(true);
			}

			span.addClass(this.subclass);
			span.addClass("noUnderline");
			span.html(this.segment);
			sub.children('td').eq(1).attr("id","seg"+item.tuId+"_"+item.tuvId+"_"+this.subId);
			sub.children('td').eq(1).append(span);
			stable.append(sub);
		});
		return stable;
	}
	return temp;
}

function getNodeByClass2(item, se_able){
	var temp=spanNode.clone(true);
	if(item.originalTarget != "")
	{
		temp.html(item.originalTarget);
	}
	else
	{
		temp.html("-");
	}
	if(item.subArray){
		var stable=subtable.clone(true);
		stable.append(temp);
		$.each(item.subArray,function(j){
			var sub=subnode.clone(true);
			sub.children('td').eq(0).text(item.tuId+"."+this.subId);
			var span=spanNode.clone(true);

			span.html(this.segment);
			sub.children('td').eq(1).append(span);
			stable.append(sub);
		});
		return stable;
	}
	return temp;
}

var str_color = "#9932CC";
var o_currentSegment = null;
var o_oldColor = null;
function unhighlightSegment(o)
{
    if (o != null)
    {
        o.style.border = "none";
    }
}

function highlightSegment(o)
{
    if (o != null)
    {
        o.style.border = "2px solid " + str_color;
        o.scrollIntoView(true);
    }
}

function HighlightSegment(tuId, tuvId, subId)
{
    var o = getSegment(tuId, tuvId, subId);

    if (o_currentSegment != null)
    {
        unhighlightSegment(o_currentSegment);
    }

    if (o != null)
    {
        highlightSegment(o);
    }

    o_currentSegment = o;
}

function getSegment(tuId, tuvId, subId)
{
    var id = "seg" + tuId + "_" + tuvId + "_" + subId;

    return document.getElementById(id);
}

function editAll(p_action)
{
	getDataByFrom(url);
}

function SE(tuId, tuvId, subId, p_forceComment)
{
    if (g_disableLink)
    {
        return;
    }

    if (g_reviewMode || p_forceComment)
    {
        editComment(tuId, tuvId, subId);
    }
    else
    {
        editSegment(tuId, tuvId, subId);
    }
}

var segmentEditorHeight = "540";

if (screen.availHeight > 600)
{
    segmentEditorHeight = screen.availHeight - 60;
}

function editSegment(tuId, tuvId, subId)
{
    var str_url = url_segmentEditor +
      "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId +
      "&refresh=0&releverage=false";

    if(w_editor)
    {
    	w_editor.close();
    }
    w_editor = window.open(str_url, "SegmentEditor",
      "resizable,width=560,height=" + segmentEditorHeight +
       ",top=0,left=0");
}

function editComment(tuId, tuvId, subId)
{
    var str_url = url_commentEditor +
      "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId + "&refresh=0";

    if(w_editor)
    {
    	w_editor.close();
    }
    w_editor = window.open(str_url, "CommentEditor",
      "width=550,height=750,top=100,left=100");
}

function doEditComment(key, commentId)
{
    var parts = key.split("_");

    var tuId  = parts[0];
    var tuvId = parts[1];
    var subId = parts[2];

    var str_url = url_commentEditor +
      "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId +
      "&commentId=" + commentId + "&refresh=0";

    w_cmtEditor = window.open(str_url, "CommentEditor",
      "width=700,height=650,top=100,left=100");
}

function SaveSegment(tuId, tuvId, subId, segment, ptagFormat)
{
    var o_form = document.SaveForm;

    o_form.save.value    = segment;
    o_form.refresh.value = "0";
    o_form.tuId.value    = tuId;
    o_form.tuvId.value   = tuvId;
    o_form.subId.value   = subId;
    o_form.ptags.value   = ptagFormat;
    localData=null;
    o_form.submit();
}

function SaveComment2(tuId, tuvId, subId, action, title, comment, priority, status, category, share, overwrite)
{
	var o_form = document.CommentForm;

    o_form.tuId.value = tuId;
    o_form.tuvId.value = tuvId;
    o_form.subId.value = subId;
    o_form.cmtAction.value = action;
    o_form.cmtTitle.value = title;
    o_form.cmtComment.value = comment;
    o_form.cmtPriority.value = priority;
    o_form.cmtStatus.value = status;
    o_form.cmtCategory.value = category;
    o_form.cmtShare.value = share;
    o_form.cmtOverwrite.value = overwrite;
    localData=null;
    o_form.submit();   
}

function contextForX(e)
{
    if(!e) e = window.event;

    var o;
    if(window.event)
    {
    o = e.srcElement;
    }
    else
    {
    o = e.target;
    while(o.nodeType != o.ELEMENT_NODE)
	o = o.parentNode;
    }

    o = getEditableSegment(o);

    if (o)
    {
        if (o.className == 'editorSegmentLocked'||o.className == 'segmentContext')
        {
            contextForReadOnly(o, e);
        }
        else
        {
            contextForSegment(o, e);
        }
    }
    else
    {
        //contextForReadOnly();
    }
}

function isEditableSegment(obj)
{
    return (obj.tagName == 'A' && ((obj.className.indexOf('editorSegment') != -1)||(obj.className.indexOf('segmentContext')!=-1)));
}

function getEditableSegment(obj)
{
    while (obj && !isEditableSegment(obj))
    {
        obj = obj.parentElement || obj.parentNode;//Added for Firefox
    }

    return obj;
}

function contextForSegment(obj, e)
{
    var ids = getSegmentIdFromHref(obj.href);

    var popupoptions;
    // When in a review activity or in viewer mode, only comments are editable.
    if (g_isReviewActivity || g_readOnly)
    {
      popupoptions = [
        new ContextItem("<B>Add/edit comment</B>",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    }
    else if (g_reviewMode)
    {
      popupoptions = [
        new ContextItem("Edit segment",
          function(){editSegment(ids[0], ids[1], ids[2])}),
        new ContextItem("<B>Add/edit comment</B>",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    }
    else
    {
      popupoptions = [
        new ContextItem("<B>Edit segment</B>",
          function(){editSegment(ids[0], ids[1], ids[2])}),
        new ContextItem("Add/edit comment",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    }
    
    ContextMenu.display(popupoptions, e);
}

function contextForReadOnly(obj, e)
{
    var ids = getSegmentIdFromHref(obj.href);

    var popupoptions = [
        new ContextItem("<B>Add/edit comment</B>",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    ContextMenu.display(popupoptions, e);
}

function getSegmentIdFromHref(href)
{
    href = href.substring(href.indexOf('(') + 1);
    href = href.substring(0, href.indexOf(')'));
    return href.split(',');l
}

$(document).ready(function(){
	$("#idComments tr:nth-child(even)").addClass("stripe");			//comment stripe
	
	$("#idComments tr").dblclick(function(){						//double click
		var key = $(this).attr("key");
		var commentId = $(this).attr("commentId");
		
		if(!g_disableComment){
			$("#idComments tr:nth-child(even)").addClass("stripe");
			$("#idComments tr").removeClass("highlight");			
			$(this).removeClass("stripe").addClass("highlight");	//comment highlight
			doEditComment(key, commentId);							//comment edit
		}
	});

	$("#idComments tr").click(function(){							//click
		$("#idComments tr:nth-child(even)").addClass("stripe");
		$("#idComments tr").removeClass("highlight");				
		$(this).removeClass("stripe").addClass("highlight");		//comment highlight
	});	
});

function closeAllComments()
{
    if (currentIssuesSize > 0)
    {
        if (confirm(closeeAllCommentWarning))
        {
            CommentForm.cmtAction.value = "closeAllComments";
            CommentForm.submit();
        }
    }
}

function sortComments(arg)
{
  document.location = url_self+"&sortComments=" + arg;
}
