var menu=parent.parent.parent.parent.menu;
var main=parent.parent.parent.parent;
var localData;

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

var url;
function getPtags(p_action)
{
	url=jsonUrl;
	main.getData(url);
}

function showPtags(p_action)
{
}

function findRepeatedSegments(p_action)
{
	// "lbid" means tuid on list view UI.
	var lbid=$(".lbid");
	if(!p_action||!lbid.length)return;
	if(p_action=='Show Repeated'){
		$(".repstyle").remove();
	}else{
		$(".repstyle").remove();
		$("#editorId").after("<COL WIDTH='1%' VALIGH='TOP' class='repstyle' NOWRAP>");
		lbid.after("<TD class='repstyle'>Rep</TD>");
		
		$.each(localData, function(i, item) {
			if(item.mainstyle.match("colorRepetition")){
				$(".firsttd").eq(i).after("<td class='repstyle' bgcolor='#FF0000'></td>");
			}else if(item.mainstyle.match("colorRepeated")){
				$(".firsttd").eq(i).after("<td class='repstyle' bgcolor='#575757'></td>");
			}else{
				$(".firsttd").eq(i).after("<td class='repstyle'></td>");
			}
		});
	};
}

function reviewMode(p_action){
	var lbid=$(".lcid");
	if(!p_action||!lbid.length)return;
	if(p_action=="Show Comments"){
		//16 open comment editor
		inner_reviewMode=g_reviewMode=false;
		
	}else{
		inner_reviewMode=g_reviewMode=true;
	}
	buildData(main.localData["target"]);

}

function initreviewMode(p_action)
{
	var lbid=$(".lcid");
	if((!p_action||!lbid.length) && !isReviwMode)return;
	// click "Hide Comments".
	$(".commentstyle").remove();
	if(p_action=="Show Comments" && !isReviwMode)
	{}
	else
	{
		$(".editorText").before("<COL WIDTH='1%' VALIGH='TOP' class='commentstyle' NOWRAP>");
		lbid.before("<TD class='commentstyle'><img src='/globalsight/images/comment-transparent.gif'></TD>");

		recursionComment(localData,0);
	};
}

function recursionComment(data,beginIndex){
	var _count = 0;
	for(var i = beginIndex;_count <lazy && i <max; i++){
		_count++;
		addCommentColumn(data[i],i);
	}
	
	if(i<max){
		setTimeout(function(){
			recursionComment(data,i);
		}, 100);
	}
}

function addCommentColumn(item,i)
{
	var name=item.tuId+","+item.tuvId+","+item.subId;
	if(item.mainstyle.match("editorComment")){
		$(".segtd").eq(i).before("<td class='commentstyle'><img class='editorComment' src='/globalsight/images/comment.gif' onclick='SE("+name+")'></td>");
	}else{
		$(".segtd").eq(i).before("<td class='commentstyle'></td>");
	}
}

function editAll(p_action)
{
	var lbid=$(".lbid");
	
	var donotReturn = false;
	if (typeof inViewerPage != "undefined" && inViewerPage)
	{
		donotReturn = true;
	}
	
	if((!p_action||!lbid.length) && !donotReturn) return;
	main.getRedata();
}

var trnode=$("<tr class='ul'><td class='firsttd'></td><td class='segtd'></td></tr>");
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
$(
	function(){
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

		// Click "List" on "me_source.jsp" or "me_target.jsp".
		if(main.localData && reuseData)
		{
			main.showList = true;
			buildData(main.localData[reuseData]);
		}
		// General from "me_source.jsp" and "me_target.jsp".
		else if(main.localData && !searchBySid && !searchByUser && !setToNormal && (typeof inViewerPage == "undefined" || !inViewerPage))
		{
			buildData(main.localData[pageName]);
		}
		else
		{
			if($(".sourceTempClass").length || lbid.length || setToNormal || reuseData)
			{
				url=jsonUrl;
				main.getDataByFrom(url,modeFrom);
			}
		}
	}
); 

function buildData(data, se_able){
	showFinish = false;
	if(modeId!=3){
		var conhtml=$.trim($("#idPageHtml").text());
		if(srcViewMode||conhtml){
			return;
		}else{
			window.location.href=window.location.href+"&srcViewMode="+modeId;
		}
	}
	var idPageHtml=$("#idPageHtml");
	idPageHtml.html("");
	$(".repstyle").remove();
	$(".commentstyle").remove();
	localData=data;
	max = localData.length;
	recursion(localData,0, se_able);
}

function recursion(data,beginIndex, se_able){
	 var _count = 0;
	for(var i = beginIndex;_count <lazy && i <max; i++){
		_count++;
		renderHtml(data[i], se_able);
	}
	
	if(i<max){
		setTimeout(function(){
			recursion(data,i, se_able);
		}, 100);
	}else{
		initUI();
		main.setShow(modeFrom);
		showFinish = true;
	}
}

function renderHtml(item, se_able){
	var idPageHtml=$("#idPageHtml");
	var temp=trnode.clone(true);
	var displayId = true;
	if(modeFrom == "target")
	{
		temp.attr("id","tr_target_"+item.tuId);
		
		if (isIncontextReview)
		{
			displayId = false;
		}
	}
	else if(modeFrom == "source")
	{
		temp.attr("id","tr_source_"+item.tuId);
	}
	temp.children('td').eq(0).attr("id","seg"+item.tuId);
	temp.children('td').eq(0).text(displayId? item.tuId : "");

	temp.children('td').eq(1).attr("id","seg"+item.tuId+"_"+item.tuvId+"_"+item.subId);

	if(item.mainstyle.match("isHighLight")){
		temp.children('td').eq(1).attr("bgColor","yellow");
	}

	//18  Incorrect view of right to left language segments.
	if(item.mainstyle.match("rtl")){
		temp.children('td').eq(1).attr("dir","rtl");
	}
	
	var htmlcontent=getNodeByClass(item, se_able);
	if(item.subArray){
		temp.children('td').eq(1).html("<table width='100%' cellspacing='0' cellpadding='2'>"+htmlcontent.html()+"</table>");
	}else{
		temp.children('td').eq(1).append(htmlcontent);
	}
	
	if(item.mainstyle.match("rtl")){
		temp.children('td').eq(1).children("a").children("span").attr("dir","rtl");
	}

	idPageHtml.append(temp);
}

function initUI(){
	$(".ul:even").addClass("alt");
  	showPtags(menu.ptag);
	findRepeatedSegments(menu.rep);
	initreviewMode(menu.comment);
}

function getNodeByClass(item, se_able){
	var temp;
	// if "mainstyle.match("SE ")", it should display right click context menus.
	var scriptFlag = inner_reviewMode || item.mainstyle.match("SE ") || se_able;
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
		// if this segment has comment,its mainstyle will have "editorComment", 
		// this will result in right click menu invisible, need remove it.
		// if "Search" to highlight, there will be "isHighLight" class, remove it.
		temp.addClass(item.mainstyle.replace("editorComment", "").replace("isHighLight", ""));
	}

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
			span.html(this.segment);
			sub.children('td').eq(1).attr("id","seg"+item.tuId+"_"+item.tuvId+"_"+this.subId);
			sub.children('td').eq(1).append(span);
			stable.append(sub);
		});
		return stable;
	}
	return temp;
}
