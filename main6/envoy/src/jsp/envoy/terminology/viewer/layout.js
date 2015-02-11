var viewerHidden = false;
var editorHidden = true;

function showViewer()
{
    if (viewerHidden || true)
    {
        splitterLeft.style.posLeft = Math.max(125, idBody.clientWidth / 8);
        splitterRight.style.posLeft = idBody.clientWidth / 2;
    }

    idViewer.style.display = '';

    viewerHidden = false;
}

function hideEditor()
{
    if (editorHidden)
    {
        return;
    }
 
    document.getElementById("idEditor").style.display = 'none';
    document.getElementById("idCloseWindow").style.display = '';

    viewerHidden = false;
    editorHidden = true;
    idViewerChange();
}

function showEditor()
{
    document.getElementById("idEditor").style.display = '';
    document.getElementById("idEditorHeader").style.display = '';
    document.getElementById("idCloseWindow").style.display = 'none';
    
    if (editorHidden || true)
    {
        splitterLeft.style.left = Math.max(125, idBody.clientWidth / 8);
        splitterRight.style.left = idBody.clientWidth / 2;
        commonPositionChange();
    }

    editorHidden = false;
}
