function findSegment(format, tuId, sourceSegment, targetSegment, donotMove, p_lnPn, p_repIndex)
{
	// check is source or target
	var segment = sourceSegment;
	try
	{
		var element = parent.sourceMenu.highlightedElement;
		var text = element.textContent;
		
		if ("Target PDF" == text)
		{
			segment = targetSegment;
		}
		
	} catch(exc) {}
	
	// navigate to page
	if (!donotMove)
	{
		var segObj = getSegmentByTuid(tuId);
		
		if (segObj)
		{
			PDFViewerApplication.pdfViewer.scrollPageIntoView(segObj.pageNum);
		}
		
		// navigate to dest
		/*
		var matchedDiv;
	    var dest = getGlobalSightDest(tuId);
	    if (dest == "")
	    {
		    for (var i = 1; i < 10;  i++)
		    {
		    	dest = getGlobalSightDest(tuId - i);
		    	
		    	if (dest != "")
		    	{
		    		break;
		    	}
		    }
	    }
	    
	    PDFViewerApplication.navigateTo(dest);
	    */
    }
    
    // clean other color
    var pages = PDFViewerApplication.pdfViewer._pages;
    for(var ii = 0; ii < pages.length; ii++)
  	{
  		var page = pages[ii];
  		
	  	var pageDiv1 = document.getElementById("pageContainer" + page.id);
	  	
	  	if (typeof(pageDiv1) == "undefined")
	  	{
	  		continue;
	  	}
	  	
	    var pageDivChildrens1 = pageDiv1.childNodes;
	    var textLayerChildrens1;
		var textLayerDiv1;
	    
	    if (pageDivChildrens1 && pageDivChildrens1.length > 0)
	    {
	  	  for(var i = 0; i < pageDivChildrens1.length; i++)
	  	  {
	  		  var divChild1 = pageDivChildrens1[i];
	  		  
	  		  if (divChild1.nodeName == "DIV" && "textLayer" == divChild1.className)
	  		  {
	  			  textLayerDiv1 = divChild1;
	  			  break;
	  		  }
	  	  }
	  	  
	  	  if (typeof(textLayerDiv1) == "undefined")
	  	  {
	  	      continue;
	  	  }
	  	  
	  	  textLayerChildrens1 = textLayerDiv1.childNodes;
	
	  	  for(var i = 0; i < textLayerChildrens1.length; i++)
	  	  {
	  		  var divChild = textLayerChildrens1[i];
	  		  var divContent = divChild.textContent;
	  		  
			divChild.innerHTML = divContent;
			
			var className = divChild.className;
			if (className.indexOf("highlight") != -1)
			{
				className = className.replace("highlight", "");
	  			divChild.className = className;
			}
	  	  }
	    }
  	}
    
    
    // find segment
    var isOfficeXml = ("office-xml" == format || "xml" == format);
    var find = false;
    var loPn = p_lnPn ? p_lnPn : PDFViewerApplication.pdfViewer._location.pageNumber;
    
    // find by GlobalSight
    var pageDiv = document.getElementById("pageContainer" + loPn);
    var pageDivChildrens = pageDiv.childNodes;
    var textLayerChildrens;
	var textLayerDiv;
    
    if (pageDivChildrens && pageDivChildrens.length > 0)
    {
  	  for(var i = 0; i < pageDivChildrens.length; i++)
  	  {
  		  var divChild = pageDivChildrens[i];
  		  
  		  if (divChild.nodeName == "DIV" && "textLayer" == divChild.className)
  		  {
  			  textLayerDiv = divChild;
  			  break;
  		  }
  	  }
  	  
  	  textLayerChildrens = textLayerDiv.childNodes;
  	  // 1 find extract match
  	  var repIndex = 1;
  	  for(var i = 0; i < textLayerChildrens.length; i++)
  	  {
  		  var divChild = textLayerChildrens[i];
  		  var divContent = divChild.textContent;
  		  var text = divContent.trim();
			
  		  if (segment == text && !find)
  		  {
	  		  if (repIndex == p_repIndex)
	  		  {
	  		  	find = true;
				matchedDiv = divChild;
				var textnode = document.createTextNode(divContent);
				var spannode = document.createElement('span');
				spannode.className = "highlight";
				spannode.appendChild(textnode);
				matchedDiv.innerHTML = spannode.outerHTML;
				matchedDiv.focus();
	  		  }
	  		  else
	  		  {
	  		  	repIndex = repIndex + 1;
	  		  	
	  		  	divChild.innerHTML = divContent;
	  			var className = divChild.className;
	  			if (className.indexOf("highlight") != -1)
	  			{
	  				className = className.replace("highlight", "");
	  	  			divChild.className = className;
	  			}
	  		  }
  		  }
  		  else // clean last match
  		  {
  			divChild.innerHTML = divContent;
  			
  			var className = divChild.className;
  			if (className.indexOf("highlight") != -1)
  			{
  				className = className.replace("highlight", "");
  	  			divChild.className = className;
  			}
  		  }
  	  }
    }
    
  if (!find)
  {
	 // find more
  	  var startMatch = false;
	  var endMatch = false;
  	  matchedDiv = new Array();
  	  var textLayerContent = textLayerDiv.textContent;
  	  var rnIndex = new Array();
  	  
  	  if (isOfficeXml)
  	  {
  	      textLayerContent = textLayerDiv.innerText;
  	      if (typeof(textLayerContent) == "undefined")
  	      {
  	          textLayerContent = $(textLayerDiv).text();
  	      }
  	      textLayerContent = handleStringForOffice(textLayerContent, rnIndex);
  	  }
  	  
  	  if (textLayerContent.indexOf(segment) != -1)
  	  {
  		  var index = textLayerContent.indexOf(segment);
  		  var segmentLen = segment.length;
  		  var count = 0;
  		  for(var i = 0; i < textLayerChildrens.length; i++)
  	      {// for
  		  var divChild = textLayerChildrens[i];
  		  var divContent = divChild.textContent;
		  var divContentLen = divContent.length;
		  var donotAddMe = false;
		  
		  var addRnCount = 0;
		  for(var rnI = 0; rnI < rnIndex.length; rnI++)
		  {
		      var _rnIndex = rnIndex[rnI];
		      
		      if (_rnIndex == count)
		      {
		          addRnCount = addRnCount + 1;
		      }
		      if (_rnIndex > count && _rnIndex < count + divContentLen)
		      {
		          addRnCount = addRnCount + 1;
		      }
		      if (_rnIndex == count + divContentLen)
		      {
		          addRnCount = addRnCount + 1;
		      }
		      
		      if (_rnIndex > count + divContentLen)
		      {
		          break;
		      }
		  }
		  
		  divContentLen = divContentLen + addRnCount;
		  
		  if (index < (count + divContentLen) && index >= count)
		  {
			  // find segment in one div
			  if ((index + segmentLen) <= (count + divContentLen))
			  {
				  var obj = new Object();
				  obj.div = divChild;
				  obj.start = index - count;
				  obj.end = index + segmentLen - count;
                  if (obj.start == 1)
				  {
				      obj.start = isOfficeXml ? ((segment.indexOf(divContent) == 0 || divContent.indexOf(segment) == 0) ? 0 : 1) : 1;
                      obj.end = isOfficeXml ? ((segment.indexOf(divContent) == 0 || divContent.indexOf(segment) == 0) ? obj.end - 1 : obj.end) : obj.end;
				  }
                  
				  matchedDiv.push(obj);
  				  break;
			  }
			  else
			  {
				  var obj = new Object();
				  obj.div = divChild;
				  obj.start = index - count;
				  
				  if (obj.start == 1)
				  {
				      obj.start = isOfficeXml ? ((segment.indexOf(divContent) == 0 || divContent.indexOf(segment) == 0) ? 0 : 1) : 1;
				  }
				  
				  obj.end = divContentLen;
				  matchedDiv.push(obj);
				  donotAddMe = true;
			  }
		  }
		  
		  if (matchedDiv.length > 0)
		  {
			  if ((index + segmentLen) <= (count + divContentLen))
			  {
				  var obj = new Object();
				  obj.div = divChild;
				  obj.start = 0;
				  obj.end = index + segmentLen - count;
				  matchedDiv.push(obj);
  				  break;
			  }
			  else
			  {
				  if (!donotAddMe)
				  {
					  var obj = new Object();
					  obj.div = divChild;
					  obj.start = 0;
					  obj.end = divContentLen;
					  matchedDiv.push(obj);
				  }
			  }
		  }
		  
		  count = count + divContentLen;
  	  }// for
  	  }// if
  	  
  	  if (matchedDiv.length > 0)
  	  {
  		  find = true;
  		  for(var i = 0; i < matchedDiv.length; i++)
  		  {
  			  var obj = matchedDiv[i];
  			  var cdiv = obj.div;
  			  var textContent = cdiv.textContent;
  			  var strStart = textContent.substr(0, obj.start);
  			  var strMid = textContent.substr(obj.start, obj.end <= textContent.length ? obj.end : textContent.length);
  			  var strEnd = obj.end < textContent.length ? textContent.substr(obj.end) : "";
  			  
  			  var newDiv = document.createElement('div');
  			  if (strStart.length > 0)
  			  {
  				var textnode = document.createTextNode(strStart);
  				newDiv.appendChild(textnode);
  			  }
  			  if (strMid.length > 0)
  			  {
  				var textnode = document.createTextNode(strMid);
  				var spannode = document.createElement('span');
  			  	spannode.className = "highlight";
  			  	spannode.appendChild(textnode);
  				newDiv.appendChild(spannode);
  			  }
  			  if (strEnd.length > 0)
  			  {
  				var textnode = document.createTextNode(strEnd);
  				newDiv.appendChild(textnode);
  			  }
  			 
  			  
  			cdiv.innerHTML = newDiv.innerHTML;
  		  }
  	  }
  }
    
    // find by PDF.js
    if (!find)
    {
	    var findStr = find ? "" : segment;
	    var event = document.createEvent('CustomEvent');
	    event.initCustomEvent('find', true, true, {
	      query: segment,
	      caseSensitive: true,
	      highlightAll: false,
	      findPrevious: ""
	    });
	    
	    PDFViewerApplication.pdfViewer.findController.dirtyMatch = true;
	    PDFViewerApplication.pdfViewer.findController.hadMatch = false;
	    PDFViewerApplication.pdfViewer.findController.pagesToSearch = loPn;
	    PDFViewerApplication.pdfViewer.findController.handleEvent(event);
	    
	    find = PDFViewerApplication.pdfViewer.findController.hadMatch;
    }
}

function getGlobalSightDest(tuId)
{
	var allAnchors = document.getElementsByTagName("a");
    var num = allAnchors.length;
	for(var iii = 0; iii < num; iii++)
	{
		var ele = allAnchors[iii];
		var gsid = "GlobalSight_" + tuId;
		
		if (ele.href.indexOf(gsid) != -1)
		{
			var index_s = ele.href.lastIndexOf("#");
			var dest = ele.href.substring(index_s + 1);
			dest = dest.replace(/%3A/g, ":");
			
			return dest;
		}
	}
	
	return "";
}

function navigateToDiv(pageNumber, left, top, curScale)
{
	var para = new Array();
	para[0] = "XYZ";
	var paraName = new Object();
	paraName.name = "XYZ";
	para[1] = paraName;
	para[2] = left;
	para[3] = top;
	para[4] = curScale;
	
	PDFViewerApplication.pdfViewer.scrollPageIntoView(pageNumber, para);
}

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

function getSegmentByTuid(tuId)
{
	var localDataForInCtxRv = window.parent.parent.parent.localDataForInCtxRv;

	for (var i = 0; i < localDataForInCtxRv.source.length; i++)
	{
		var seg = localDataForInCtxRv.source[i];
		
		if (seg.tuId && seg.tuId == tuId)
		{
		    if (seg.pageNum > 0)
		    {
			var segment = new Object();
			segment.tuId = seg.tuId;
			segment.subId = seg.subId;
			segment.srcTuvId = localDataForInCtxRv.source[i].tuvId;
			segment.srcSegment = localDataForInCtxRv.source[i].segment;
			segment.srcSegmentNoTag = handleSpecialChar(localDataForInCtxRv.source[i].segmentNoTag);
			
			segment.tgtTuvId = localDataForInCtxRv.target[i].tuvId;
			segment.tgtSegment = localDataForInCtxRv.target[i].segment;
			segment.tgtSegmentNoTag = handleSpecialChar(localDataForInCtxRv.target[i].segmentNoTag);
			
			segment.pageNum = seg.pageNum;
			
			return segment;
			}
			else
			{
			return false;
			}
		}
	}
	
	return false;
}

function buildPageContent(pageNum, localDataForInCtxRv, isTarget)
{
	var content = "";
	var segments = new Array();
	var format1 = "";
	
	for (var i = 0; i < localDataForInCtxRv.source.length; i++)
	{
		var seg = isTarget ? localDataForInCtxRv.target[i] : localDataForInCtxRv.source[i];
		var isOfficeXml = ("office-xml" == seg.format || "xml" == seg.format);
		format1 = seg.format;
		
		if (isOfficeXml || (seg.pageNum && seg.pageNum == pageNum))
		{
			var segment = new Object();
			segment.format = seg.format;
			segment.tuId = seg.tuId;
			segment.subId = seg.subId;
			segment.srcTuvId = localDataForInCtxRv.source[i].tuvId;
			segment.srcSegment = localDataForInCtxRv.source[i].segment;
			segment.srcSegmentNoTag = handleSpecialChar(localDataForInCtxRv.source[i].segmentNoTag);
			
			segment.tgtTuvId = localDataForInCtxRv.target[i].tuvId;
			segment.tgtSegment = localDataForInCtxRv.target[i].segment;
			segment.tgtSegmentNoTag = handleSpecialChar(localDataForInCtxRv.target[i].segmentNoTag);
			
			segment.pageNum = isOfficeXml ? 1 : seg.pageNum;
			
			segment.start = content.length;
			content = content + handleSpecialChar(seg.segmentNoTag) + " ";
			segment.end = content.length;
			
			segments[segments.length] = segment;
		}
	}
	
	var result = new Object();
	result.segments = segments;
	result.content = content;
	result.format = format1;
	
	return result;
}

function getPageNumberFromParentId(o)
{
	var pa = o.parentElement;
	while (pa)
	{
		if ("page" == pa.className && pa.id && pa.id.indexOf("pageContainer") != -1)
		{
			var num = pa.id.substr(13);
			return num;
		}
		
		pa = pa.parentElement;
	}
	
	return -1;
}

function handleStringForOffice(sss, rnIndex)
{
	var result = sss.replace(/\r\n /g, " ");
	result = result.replace(/ \r\n/g, " ");
	
	var index = result.indexOf("\r\n");
	
	while(index != -1)
	{
		rnIndex[rnIndex.length] = index;
		index = result.indexOf("\r\n", index + 1);
	}
	
	
	result = result.replace(/\r\n/g, " ");
	
	return result;
}

function handleSpecialChar(seg)
{
	var rrr = seg.replace(/^\s+|\s+$/g,'');
	
	rrr = rrr.replace('\t', ' ')
	
	var result = "";
	for (var i = 0; i < rrr.length; i++)
    {
        var ccc = rrr.charAt(i);
        var cccCode = rrr.charCodeAt(i);

        if (cccCode == 9632)
        {
            continue;
        }

        if (i > 0)
        {
            var lastChar = result.length > 0 ? result.charAt(result.length - 1)
                    : 'N';
            // ignore tab
            if (ccc == '\t' && lastChar == ' ')
            {
                continue;
            }
        }

        result = result + ccc;
    }
	
	
	return result;
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

function getSegment(isTarget, pageContent, o, i, divArr, divLeft, divWidth, clickX)
{
    var isOfficeXml = ("office-xml" == pageContent.format || "xml" == pageContent.format);
    
	if (i == 0 && !isOfficeXml)
	{
		return pageContent.segments[0];
	}
	
	if (i == (divArr.length - 1) && !isOfficeXml )
	{
		return pageContent.segments[pageContent.segments.length - 1];
	}
	
		var o_0 = i == 0 ? false : divArr[i-1];
		var o_1 = o;
		var o_2 = i == (divArr.length - 1)? false : divArr[i+1];
		
		var o0text = o_0 ? o_0.textContent : "you can not find me 1314151617181910";
		var o1text = o_1.textContent;
		var o2text = o_2 ? o_2.textContent : "you can not find me 1314151617181910";
		
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
                var segContent = isTarget ? seg.tgtSegmentNoTag : seg.srcSegmentNoTag;
				if (index >= seg.start && index < seg.end)
				{
                    if (o1text == segContent){
                        return seg;
                    }
                    else{
                        var segArray = [];
                        var textLen = o1text.length;
                        var lll = segContent.length;
                        var j1 = j + 1;
                        segArray.push({seg: seg, length : segContent.length});
                        while(lll < textLen && j1 < pageContent.segments.length){
                            var seg1 = pageContent.segments[j1];
                            var segContent1 = isTarget ? seg1.tgtSegmentNoTag : seg1.srcSegmentNoTag;
                            var lll_old = lll;
                            lll = lll + segContent1.length;
                            j1 = j1 + 1;
                            
                            if (lll > textLen){
                                segArray.push({seg: seg1, length : textLen - lll_old});
                            }
                            else{
                                segArray.push({seg: seg1, length : segContent1.length});
                            }
                        }
                        var clickLen = clickX - divLeft;
                        var whereIs = clickLen / divWidth;
                        
                        var whereIs2_start = 0;
                        var whereIs2_end = 0;
                        var _start = 0;
                        var _end = 0;
                        for(var iii = 0; iii < segArray.length; iii++){
                            var segLen = segArray[iii];
                            _end = _end + segLen.length;
                            whereIs2_start = _start / o1text.length;
                            whereIs2_end = _end / o1text.length;
                            if(whereIs > whereIs2_start && whereIs <= whereIs2_end){
                                return segLen.seg;
                            }
                            
                            _start = _start + segLen.length;
                        }
                        return pageContent.segments[j];
                    }
				}
			}
			
			
			return false;
		}
		else
		{
			return false;
		}
}

function getElementPosition(e) {
            var x = 0, y = 0;
            while (e != null) {
                x += e.offsetLeft;
                y += e.offsetTop;
                e = e.offsetParent;
            }
            return { x: x, y: y };
}