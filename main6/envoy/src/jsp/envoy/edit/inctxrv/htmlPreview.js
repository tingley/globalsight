
function HighlightHtmlPreviewSegment(tuId, tuvId, subId)
{
    var o = getSegment(tuId, tuvId, subId);

    if (o_currentSegment != null)
    {
    	unhighlightHtmlPreviewSegment(o_currentSegment);
    }

    if (o != null)
    {
    	highlightHtmlPreviewSegment(o);
    }

    o_currentSegment = o;
}

function highlightHtmlPreviewSegment(o)
{
    if (o != null)
    {
        o.className = "highlight";
        o.scrollIntoView(true);
    }
}

function UnhighlightHtmlPreviewSegment(tuId, tuvId, subId)
{
    var o = getSegment(tuId, tuvId, subId);

    unhighlightHtmlPreviewSegment(o);

    o_currentSegment = o;
}

function unhighlightHtmlPreviewSegment(o)
{
    if (o != null)
    {
        o.className = "";
    }
}