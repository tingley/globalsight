
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