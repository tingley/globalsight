
function HighlightHtmlPreviewSegment(tuId, tuvId, subId)
{
    var o = getSegment(tuId, tuvId, subId);
    if (o != null)
    {
       $(".gsHighlight").removeClass("gsHighlight");
       $(o).addClass("gsHighlight");
       o.scrollIntoView(true);
    }
}

function HighlightSegmentInList()
{
	$(".gsHighlight").removeClass("gsHighlight");
}

function SETarget(tuId, tuvId, subId){
	HighlightHtmlPreviewSegment(tuId, tuvId, subId);
	if (typeof(window.parent.parent.parent.localData) != "undefined"
		  && typeof(window.parent.parent.parent.localData.source) != "undefined"
		  && typeof(window.parent.parent.parent.localData.target) != "undefined")
	  {
		  for(var i0 = 0; i0 < window.parent.parent.parent.localData.target.length; i0++)
		  {
			  var seg0 = window.parent.parent.parent.localData.target[i0];
			  if (seg0.tuId == tuId && seg0.subId == subId)
		      {
				  parent.parent.source.HighlightHtmlPreviewSegment(tuId, seg0.tuvId, subId);
				  parent.parent.source.HighlightHtmlPreviewSegment(tuId, window.parent.parent.parent.localData.source[i0].tuvId, subId);
				  break;
		      }
		  }
		  
		  for(var i0 = 0; i0 < window.parent.parent.parent.localData.source.length; i0++)
		  {
			  var seg0 = window.parent.parent.parent.localData.source[i0];
			  if (seg0.tuId == tuId && seg0.subId == subId)
		      {
				  parent.parent.target.HighlightHtmlPreviewSegment(tuId, seg0.tuvId, subId);
				  parent.parent.target.HighlightHtmlPreviewSegment(tuId, window.parent.parent.parent.localData.target[i0].tuvId, subId);
				  break;
		      }
		  }
	  }	  
}