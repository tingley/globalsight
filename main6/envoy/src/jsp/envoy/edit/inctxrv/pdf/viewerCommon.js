function sortDivByOffset(divA, divB)
{
	var offsetTopA = divA.offsetTop;
	var offsetTopB = divB.offsetTop;
	
	var offsetLeftA = divA.offsetLeft;
	var offsetLeftB = divB.offsetLeft;
	
	if (offsetTopA == offsetTopB)
	{
		return offsetLeftA - offsetLeftB;
	}
	else
	{
		return offsetTopA - offsetTopB;
	}
}

function buildPageContent(pageNum, localData, isTarget)
{
	var segmentArray = isTarget ? localData.target : localData.source;
	
	var content = "";
	var segments = new Array();
	
	for (var i = 0; i < localData.source.length; i++)
	{
		var seg = isTarget ? localData.target[i] : localData.source[i];
		
		if (seg.pageNum && seg.pageNum == pageNum)
		{
			var segment = new Object();
			segment.tuId = seg.tuId;
			segment.subId = seg.subId;
			segment.srcTuvId = localData.source[i].tuvId;
			segment.srcSegment = localData.source[i].segment;
			segment.srcSegmentNoTag = handleSpecialChar(localData.source[i].segmentNoTag);
			
			segment.tgtTuvId = localData.target[i].tuvId;
			segment.tgtSegment = localData.target[i].segment;
			segment.tgtSegmentNoTag = handleSpecialChar(localData.target[i].segmentNoTag);
			
			segment.start = content.length;
			content = content + handleSpecialChar(seg.segmentNoTag) + " ";
			segment.end = content.length;
			
			segments[segments.length] = segment;
		}
	}
	
	var result = new Object();
	result.segments = segments;
	result.content = content;
	
	return result;
}

function handleSpecialChar(seg)
{
	var rrr = seg.replace(/^\s+|\s+$/g,'');
	
	rrr = rrr.replace('\t', ' ')
	
	return rrr;
}

function sendAjax(obj)
{
	dojo.xhrPost(
	{
		url:"/globalsight/ControlServlet?linkName=refreshSelf&pageName=inctxrvED8",
		content:obj,
		handleAs: "text", 
		load:function(data){
			alert("Segment is updated. ");
		},
		error:function(error)
		{
			alert(error.message);
		}
	});
}

function getSegment(pageContent, o, i, divArr)
{
	if (i == 0)
	{
		return pageContent.segments[0];
	}
	else if (i == (divArr.length - 1))
	{
		return pageContent.segments[pageContent.segments.length - 1];
	}
	else
	{
		var o_0 = divArr[i-1];
		var o_1 = o;
		var o_2 =  divArr[i+1];
		
		var o0text = o_0.textContent;
		var o1text = o_1.textContent;
		var o2text = o_2.textContent;
		
		// 1 find before current next
		var index = pageContent.content.indexOf(o1text);
		var index0, index2;
		if (index != -1)
		{
			index0 = pageContent.content.lastIndexOf(o0text, index);
			index2 = pageContent.content.indexOf(o2text, index);
		}
		
		while (index != -1)
		{
			if ((index == (index0 + o0text.length + 1) || index == (index0 + o0text.length )) 
				&&  ((index + o1text.length)  == index2 || (index + o1text.length + 1)  == index2 ))
			{
				break;
			}
			
			index = pageContent.content.indexOf(o1text, (index + o1text.length));
			if (index != -1)
			{
				index0 = pageContent.content.lastIndexOf(o0text, index);
				index2 = pageContent.content.indexOf(o2text, index);
			}
		}
		
		// 2 find before current
		if (index == -1)
		{
			index = pageContent.content.indexOf(o1text);
			if (index != -1)
			{
				index0 = pageContent.content.lastIndexOf(o0text, index);
			}
			
			while (index != -1)
			{
				if (index == (index0 + o0text.length + 1) || index == (index0 + o0text.length ))
				{
					break;
				}
				
				index = pageContent.content.indexOf(o1text, (index + o1text.length));
				if (index != -1)
				{
					index0 = pageContent.content.lastIndexOf(o0text, index);
				}
			}
		}
		
		// 3 find current next
		if (index == -1)
		{
			index = pageContent.content.indexOf(o1text);
			if (index != -1)
			{
				index2 = pageContent.content.indexOf(o2text, index);
			}
			
			while (index != -1)
			{
				if ((index + o1text.length)  == index2 || (index + o1text.length + 1)  == index2 )
				{
					break;
				}
				
				index = pageContent.content.indexOf(o1text, (index + o1text.length));
				if (index != -1)
				{
					index2 = pageContent.content.indexOf(o2text, index);
				}
			}
		}
		
		// 4 find current
		if (index == -1)
		{
			index = pageContent.content.indexOf(o1text);
		}
		
		if (index != -1)
		{
			for (var j = 0; j < pageContent.segments.length; j++)
			{
				var seg = pageContent.segments[j];
				if (index >= seg.start && index < seg.end)
				{
					return seg;
				}
			}
			
			
			return false;
		}
		else
		{
			return false;
		}
	}
}