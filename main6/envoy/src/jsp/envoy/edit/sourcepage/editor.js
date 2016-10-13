//                           -*- Mode: Javascript -*-
//
// Copyright (c) 2005 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

/*
Note on GXML Editing:

The <translatable><segment>..</segment></translatable> hierarchy is
flattened in the UI's HTML. Each segment is turned into a span with
class=translatable and the combined TU and TUV attributes (blockId,
segmentId, type).

Whenever a segment gets edited (extracted, split, merged), all but the
type attribute must be cleared on ALL segments belonging to the
translatable (i.e., within the same paragraph) so the backend can
reconstruct the translatable correctly and recognize it as changed.

*/

// try with http://localhost:7001/globalsight/ControlServlet?linkName=sourceEditor&pageName=DTLS&sourcePageId=1001&jobId=1001



// GXML Header Variables
var g_gxmlLocale;
var g_gxmlVersion;
var g_gxmlDatatype;
var g_gxmlWordcount;

var g_dirty = false;

var w_preview = null;

function previewGxml()
{
    if (!verifyGsTags(idEditor))
    {
        alert("GS region tags are unbalanced, please fix.");
        return;
    }

    var xml = HtmlToGxml(idEditor).xml;

    xml = xml.replace(/&/g, "&amp;").replace(/"/g, "&quot;"); //"
    xml = xml.replace(/</g, "&lt;").replace(/>/g, "&gt;");
    //xml = xml.replace(/\n/g, "<BR>") // using <pre> now.

    w_preview = window.open('', 'gxmlPreview',
        'toolbar=no,scrollbars=yes,status=no,resizable=yes');
    var d = w_preview.document.open('text/html');
    d.write('<html><head><title>GXML Preview</title>');
    d.write('<style>* { font-family: Courier New; font-size: x-small; }</style>');
    d.write('<script>function doKeyDown() ');
    d.write('{ if (window.event.keyCode == 27) window.close(); } ');
    d.write('</script>');
    d.write('</head><body onkeydown="doKeyDown()">');
    d.write("<pre>" + xml + "</pre>");
    d.write('</body></html>');
    d.close();
}

// Merges the GXML to the original format and shows its text
// (same as what is shown in this editor minus the colors).
function previewOrigFormat()
{
    var tmp = idEditor.innerHTML.replace(/<BR>/ig, "\n");
    tmp = tmp.replace(/<[^>]*>/ig, "");
    tmp = tmp.replace(/&nbsp;/g, " ");
    //tmp = tmp.replace(/&lt;/g, "<").replace(/&gt;/g, ">");

    w_preview = window.open('', 'gxmlPreview',
        'toolbar=no,scrollbars=yes,status=no,resizable=yes');
    var d = w_preview.document.open('text/html');
    d.write('<html><head><title>Original Format Preview</title>');
    d.write('<style>* { font-family: Courier New; font-size: x-small; }</style>');
    d.write('<script>function doKeyDown() ');
    d.write('{ if (window.event.keyCode == 27) window.close(); } ');
    d.write('</script>');
    d.write('</head><body onkeydown="doKeyDown()">');
    d.write("<pre>" + tmp + "</pre>");
    d.write('</body></html>');
    d.close();
}

// Merges the GXML to the original format (HTML). Can only be
// previewed if datatype="html" because IE can only display HTML when
// using document.open() and document.write().
function previewSourceForDebug()
{
    if (!isHtmlDatatype(g_gxmlDatatype))
    {
        alert("Only HTML-based formats can be previewed.");
        return;
    }

    var tmp = idEditor.innerHTML.replace(/<BR>/ig, "\n");
    tmp = tmp.replace(/<[^>]*>/g, "");
    tmp = tmp.replace(/&nbsp;/g, " ");
    tmp = tmp.replace(/&amp;/g, "&").replace(/&quot;/g, "\"");
    tmp = tmp.replace(/&lt;/g, "<").replace(/&gt;/g, ">");

    w_preview = window.open('', 'gxmlPreview',
        'toolbar=yes,scrollbars=yes,status=no,resizable=yes');
    var d = w_preview.document.open('text/html');
    d.write(tmp);
    d.close();
}

function crlfToBr(p_arg)
{
    return p_arg.replace(/\r?\n/g, "<BR>");
}

function toJavascript(p_arg)
{
    if (!p_arg)
    {
        return p_arg;
    }

    var result = "";

    for (var i = 0, max = p_arg.length; i < max; i++)
    {
        var ch = p_arg.charAt(i);

        switch (ch)
        {
        case '\\': result += "\\\\"; break;
        case '"':  result += "\\\""; break;
        case '\'': result += "\\\'"; break;
        case '\b': result += "\\b";  break;       // backspace
        case '\u000b': result += "\\v";  break;   // vertical tab
        case '\u000c': result += "\\f";  break;   // form feed
        case '\n': result += "\\n";  break;
        case '\r': result += "\\r";  break;
        case '\t': result += "\\t";  break;
        case '\u00a0': result += "\\u00a0";  break; // nbsp
        default: result += ch; break;
        }
    }

    return result;
}

function GsTagParams(p_tag)
{
    if (p_tag)
    {
        this.name  = p_tag.name;
        this.add   = p_tag.add;
        this.added = p_tag.added;
        this.id    = p_tag.id;
        this.locales = p_tag.locales;

        this.type  = getGsTagType(p_tag);
    }
}

function getGsTagType(p_tag)
{
    if (p_tag.added) return 'added';
    else if (p_tag.add) return 'add';
    else if (p_tag._delete) return 'delete';
    else return 'gs';
}

function TranslatableParams(dataformat)
{
    this.dataformat = dataformat;
}

// ----------------------------------------------------------

function hideContextMenu()
{
    idBody.focus();
}

function contextForX()
{
    window.status = '';

    var o = event.srcElement;
    var rng = document.selection.createRange();

    // Text selection must select within a single element
    if (rng.parentElement && rng.parentElement() != o)
    {
        //window.status = "incorrect selection 1";
        //contextForMultipleElementSelection(o);
        return false;
    }
    // Selection of non text must select 1 element only - TODO
    else if (typeof(rng.length) != 'undefined' && rng.length != 1)
    {
        //window.status = "incorrect selection 2";
        //contextForInvalidTextSelection(o);
        return false;
    }

    if (o.tagName == 'SPAN')
    {
        switch (o.className)
        {
        case 'skeleton':     contextForSkeleton(o); break;
        case 'translatable': contextForTranslatable(o); break;
        case 'localizable':  contextForLocalizable(o); break;
        case 'gsdelete':     contextForGsTagDelete(o); break;
        case 'gs':           contextForGsTagEnd(o); break;
        case 'gsadd':        contextForGsTagAdd(o); break;
        case 'gsadded':      contextForGsTagAdded(o); break;
        }

        return false;
    }
}

function contextForInvalidTextSelection(obj)
{
    var popupoptions = [
        new ContextItem("Invalid text selection", null, true)
        ];
    ContextMenu.display(popupoptions);
}

function contextForMultipleElementSelection(obj)
{
    var popupoptions = [
        new ContextItem("Multiple elements selected", null, true)
        ];
    ContextMenu.display(popupoptions);
}

function contextForSkeleton(obj)
{
    // todo: ensure sel.parentElement is a span
    var haveSel = (document.selection.type == 'Text' &&
        document.selection.createRange().text != '');

    var rng = document.selection.createRange();

    var popupoptions = [
        new ContextItem("Translate",
            function() { extractTrans(obj, rng) }, !haveSel),
        new ContextItem("Localize",
            function() { extractLoc(obj, rng) }, !haveSel),
        new ContextSeperator(),
        new ContextItem("Insert Snippet Position",
            function() { addGsTagAdd(obj, rng) }, haveSel),
        new ContextItem("Insert Snippet",
            function() { addGsTagAdded(obj, rng) }, haveSel),
        new ContextItem("Insert Deletable Region Start",
            function() { addGsTagDelete(obj, rng) }, haveSel),
        new ContextItem("Insert Deletable Region End",
            function() { addGsTagEnd(obj, rng) }, haveSel)
        ];
    ContextMenu.display(popupoptions);
}

function contextForTranslatable(obj)
{
    var canMergePrev = isTranslatable(obj.previousSibling);
    var canMergeNext = isTranslatable(obj.nextSibling);

    var haveSel = (document.selection.type == 'Text' &&
        document.selection.createRange().text != '');

    var rng = document.selection.createRange();

    var popupoptions = [
        new ContextItem("Split Segment",
            function() { splitTrans(obj, rng) }, haveSel),
        new ContextItem("Merge with previous",
            function() { mergeTrans(obj, false) }, !canMergePrev),
        new ContextItem("Merge with next",
            function() { mergeTrans(obj, true) }, !canMergeNext),
        new ContextItem("Don't Extract",
            function() { unextractTransLoc(obj) }, false),
        new ContextSeperator(),
        new ContextItem("Delete",
            function() { deleteTransLoc(obj) }, false)
        ];
    ContextMenu.display(popupoptions);
}

function contextForLocalizable(obj)
{
    var popupoptions = [
        new ContextItem("Don't Extract",
            function() { unextractTransLoc(obj) }, false),
        new ContextSeperator(),
        new ContextItem("Delete",
            function() { deleteTransLoc(obj) }, false)
        ];
    ContextMenu.display(popupoptions);
}

function contextForGsTagAdd(obj)
{
    var rng = document.selection.createRange();

    var popupoptions = [
        new ContextItem("Edit",
            function() { editGsTagAdd(obj, rng) }, false),
        new ContextSeperator(),
        new ContextItem("Delete",
            function() { deleteGsTag(obj) }, false)
        ];
    ContextMenu.display(popupoptions);
}

function contextForGsTagAdded(obj)
{
    var rng = document.selection.createRange();

    var popupoptions = [
        new ContextItem("Edit",
            function() { editGsTagAdded(obj, rng) }, false),
        new ContextSeperator(),
        new ContextItem("Delete",
            function() { deleteGsTag(obj) }, false)
        ];
    ContextMenu.display(popupoptions);
}

function contextForGsTagDelete(obj)
{
    var rng = document.selection.createRange();

    var popupoptions = [
        new ContextItem("Edit",
            function() { editGsTagDelete(obj, rng) }, false),
        new ContextSeperator(),
        new ContextItem("Delete",
            function() { deleteGsTag(obj) }, false)
        ];
    ContextMenu.display(popupoptions);
}

function contextForGsTagEnd(obj)
{
    var rng = document.selection.createRange();

    var popupoptions = [
        new ContextItem("Delete",
            function() { deleteGsTag(obj) }, false)
        ];
    ContextMenu.display(popupoptions);
}

// ----------------------------------------------------------

function insertGsTag(p_obj, p_split, p_gstag)
{
    p_obj.innerText = p_split[0];
    var skel = createSkeleton("\n" + p_split[2]);
    p_obj.insertAdjacentElement('afterEnd', p_gstag);
    p_gstag.insertAdjacentElement('afterEnd', skel);

    g_dirty = true;
}

function addGsTagAdd(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);

    var params = new GsTagParams();
    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/gstagAdd.jsp', params,
        "dialogHeight:412px; dialogWidth:545px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res) // res = position name
    {
        var gstag = createGsAddTag(new String(res));
        insertGsTag(obj, split, gstag);
    }

    hideContextMenu();
}

function addGsTagAdded(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);

    var params = new GsTagParams();
    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/gstagAdded.jsp', params,
        "dialogHeight:450px; dialogWidth:545px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res) // res = obj { name, added, id }
    {
        var gstag = createGsAddedTag(new String(res.name),
           new String(res.added), res.id ? new String(res.id) : null);
        insertGsTag(obj, split, gstag);
    }

    hideContextMenu();
}

function addGsTagDelete(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);

    var params = new GsTagParams();
    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/gstagDelete.jsp', params,
        "dialogHeight:220px; dialogWidth:330px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res != null) // res = locales
    {
        var gstag = createGsRegionTag(new String(res));
        insertGsTag(obj, split, gstag);
    }

    hideContextMenu();
}

function addGsTagEnd(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);

    gstag = createGsEndTag();

    insertGsTag(obj, split, gstag);

    hideContextMenu();
}

function editGsTagAdd(obj, rng)
{
    var params = new GsTagParams(obj);
    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/gstagAdd.jsp', params,
        "dialogHeight:412px; dialogWidth:545px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res)
    {
        // res = position name
        var gstag = createGsAddTag(new String(res));
        obj.parentElement.replaceChild(gstag, obj);

        g_dirty = true;
    }

    hideContextMenu();
}

function editGsTagAdded(obj, rng)
{
    var params = new GsTagParams(obj);
    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/gstagAdded.jsp', params,
        "dialogHeight:450px; dialogWidth:545px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res)
    {
        // res = obj { name, added, id }
        var gstag = createGsAddedTag(new String(res.name),
            new String(res.added), res.id ? new String(res.id) : null);
        obj.parentElement.replaceChild(gstag, obj);

        g_dirty = true;
    }

    hideContextMenu();
}

function editGsTagDelete(obj, rng)
{
    var params = new GsTagParams(obj);
    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/gstagDelete.jsp', params,
        "dialogHeight:220px; dialogWidth:330px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res != null)
    {
        // res = locales
        var gstag = createGsRegionTag(new String(res));
        obj.parentElement.replaceChild(gstag, obj);

        g_dirty = true;
    }

    hideContextMenu();
}

function deleteGsTag(obj)
{
    // TODO: handle GS regions differently than GS add/added
    var parent = obj.parentElement;
    var before = obj.previousSibling;
    var after = obj.nextSibling;

    if (isSkeleton(before) && isSkeleton(after))
    {
        parent.removeChild(obj);
        parent.removeChild(after);
        before.innerText = before.innerText + after.innerText;
    }
    else
    {
        parent.removeChild(obj);
    }

    g_dirty = true;

    hideContextMenu();
}

function extractTrans(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);
    //debug("Before: `" + split[0] + "'\nSelection: `" + split[1] +
    //      "'\nAfter: `" + split[2] + "'");

    var parent = obj.parentElement;
    var before = obj.previousSibling;
    var after = obj.nextSibling;

    if (split[0] == '' && isTranslatable(before) &&
        split[2] == '' && isTranslatable(after))
    {
        // merge selection with neighbors, mark as modified
        before.innerText = before.innerText + split[1] + after.innerText;
        parent.removeChild(obj);
        parent.removeChild(after);

        while (isTranslatable(before))
        {
            setTransUpdated(before);
            before = before.previousSibling;
        }

        after = before.nextSibling;
        while (isTranslatable(after))
        {
            setTransUpdated(after);
            after = after.nextSibling;
        }

        g_dirty = true;
    }
    else if (split[0] == '' && isTranslatable(before))
    {
        // merge with previous, mark previous as modified
        before.innerText = before.innerText + split[1];
        obj.innerText = split[2];

        while (isTranslatable(before))
        {
            setTransUpdated(before);
            before = before.previousSibling;
        }

        g_dirty = true;
    }
    else if (split[2] == '' && isTranslatable(after))
    {
        // merge with next, mark next as modified
        obj.innerText = split[0];
        after.innerText = split[1] + after.innerText;

        while (isTranslatable(after))
        {
            setTransUpdated(after);
            after = after.nextSibling;
        }

        g_dirty = true;
    }
    else
    {
        // new standalone translatable

        rng.select();

        // Extract as '<dataformat>:text' (default) or 'javascript:string'.
        var params = new TranslatableParams(g_gxmlDatatype);
        var res = showModalDialog(
            '/globalsight/envoy/edit/sourcepage/translatable.jsp', params,
            "dialogHeight:112px; dialogWidth:350px; center:yes; " +
            "resizable:no; status:no; help:no;");

        if (res)
        {
            var temp = res.split(':');
            var format, type;

            // A segment's datatype is inherited from the GXML root.
            if (temp[0] != g_gxmlDatatype)
            {
                format = temp[0];
            }
            type = temp[1];

            var transloc = createTranslatable(split[1], format, type);

            obj.innerText = split[0];
            var skel = createSkeleton(split[2]);
            obj.insertAdjacentElement('afterEnd', transloc);
            transloc.insertAdjacentElement('afterEnd', skel);

            g_dirty = true;
        }
    }

    hideContextMenu();
}

function extractLoc(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);
    //debug("Before: `" + split[0] + "'\nSelection: `" + split[1] +
    //      "'\nAfter: `" + split[2] + "'");

    rng.select();

    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/localizable.jsp', null,
        "dialogHeight:112px; dialogWidth:380px; center:yes; " +
        "resizable:no; status:no; help:no;");

    // TODO: double-check localizable is surrounded by skeleton
    if (res)
    {
        var type = res;

        obj.innerText = split[0];
        var transloc = createLocalizable(split[1], type);
        var skel = createSkeleton(split[2]);
        obj.insertAdjacentElement('afterEnd', transloc);
        transloc.insertAdjacentElement('afterEnd', skel);

        g_dirty = true;
    }

    hideContextMenu();
}

function unextractTransLoc(obj)
{
    var parent = obj.parentElement;
    var before = obj.previousSibling;
    var after = obj.nextSibling;
    var text = obj.innerText;

    if (isLocalizable(obj))
    {
        parent.removeChild(obj);
        parent.removeChild(after);
        before.innerText = before.innerText + text + after.innerText;
    }
    else
    {
        if (isSkeleton(before) && isSkeleton(after))
        {
            parent.removeChild(obj);
            parent.removeChild(after);
            before.innerText = before.innerText + text + after.innerText;
        }
        else if (isTranslatable(before) && isTranslatable(after))
        {
            parent.removeChild(obj);
            before.insertAdjacentElement('afterEnd', createSkeleton(text));
            setTransUpdated(before);
            setTransUpdated(after);
        }
        else if (isSkeleton(before))
        {
            parent.removeChild(obj);
            before.innerText = before.innerText + text;
            setTransUpdated(after);
        }
        else
        {
            parent.removeChild(obj);
            after.innerText = text + after.innerText;
            setTransUpdated(before);
        }
    }

    g_dirty = true;

    hideContextMenu();
}

function deleteTransLoc(obj)
{
    var parent = obj.parentElement;
    var before = obj.previousSibling;
    var after = obj.nextSibling;

    if (isLocalizable(obj))
    {
        parent.removeChild(obj);
        parent.removeChild(after);
        before.innerText = before.innerText + after.innerText;
    }
    else
    {
        if (isSkeleton(before) && isSkeleton(after))
        {
            parent.removeChild(obj);
            parent.removeChild(after);
            before.innerText = before.innerText + after.innerText;
        }
        else // trans before or after object
        {
            parent.removeChild(obj);
        }
    }

    g_dirty = true;

    hideContextMenu();
}

function splitTrans(obj, rng)
{
    var parent = rng.parentElement();
    if (parent != obj)
    {
        window.status = "selection not within a single element";
        hideContextMenu();
        return;
    }

    var split = splitElementSelection(rng, parent);
    if (split[0] != '' && split[1] == '' && split[2] != '')
    {
        obj.innerText = split[0];
        var transloc = createTranslatable(split[2],
            obj.getAttribute('datatype'), obj.getAttribute('type'));
        obj.insertAdjacentElement('afterEnd', transloc);
        setTransUpdated(obj);

        var before = obj.previousSibling;
        while (isTranslatable(before))
        {
            setTransUpdated(before);
            before = before.previousSibling;
        }

        var after = obj.nextSibling;
        while (isTranslatable(after))
        {
            setTransUpdated(after);
            after = after.nextSibling;
        }

        g_dirty = true;
    }

    hideContextMenu();
}

function mergeTrans(obj, p_next)
{
    var parent = obj.parentElement;
    var before = obj.previousSibling;
    var after = obj.nextSibling;
    var text = obj.innerText;

    if (p_next)
    {
        parent.removeChild(obj);
        after.innerText = text + after.innerText;
    }
    else
    {
        parent.removeChild(obj);
        before.innerText = before.innerText + text;
    }

    while (isTranslatable(before))
    {
        setTransUpdated(before);
        before = before.previousSibling;
    }

    while (isTranslatable(after))
    {
        setTransUpdated(after);
        after = after.nextSibling;
    }

    g_dirty = true;

    hideContextMenu();
}

function isSkeleton(obj)
{
    return obj && obj.className == 'skeleton';
}

function isTranslatable(obj)
{
    return obj && obj.className == 'translatable';
}

function isLocalizable(obj)
{
    return obj && obj.className == 'localizable';
}

function splitElementSelection(rng, parent)
{
    var result = new Array();

    var parentRng = document.body.createTextRange();
    parentRng.moveToElementText(parent);
    parentRng.collapse(true);

    while (parentRng.compareEndPoints('EndToStart', rng) != 0)
    {
        parentRng.moveEnd('character', 1);
    }

    result[0] = parentRng.text;

    result[1] = rng.text;

    parentRng.moveToElementText(parent);
    parentRng.collapse(false);

    while (parentRng.compareEndPoints('StartToEnd', rng) != 0)
    {
        parentRng.moveStart('character', -1);
    }

    result[2] = parentRng.text;

    return result;
}

// ----------------------------------------------------------

function createText(text)
{
    return document.createTextNode(text);
}

function createBr()
{
    return document.createElement('BR');
}

function appendText(p_obj, p_text)
{
    var text = '';
    for (var i = 0, max = p_text.length; i < max; i++)
    {
        var ch = p_text.charAt(i);

        if (ch == '\n')
        {
            if (text)
            {
                p_obj.appendChild(createText(text));
                text = '';
            }

            p_obj.appendChild(createBr());
            continue;
        }
        else if (ch == ' ')
        {
            text += '\u00a0';
            continue;
        }
        else if (ch == '\t')
        {
            text += '\u00a0\u00a0\u00a0\u00a0';
            continue;
        }

        text += ch;
    }

    if (text)
    {
        p_obj.appendChild(createText(text));
    }
}

function createSkeleton(p_text)
{
    var res = document.createElement('SPAN');
    res.className = 'skeleton';

    appendText(res, p_text);

    return res;
}

function createTranslatable(p_text, datatype, type)
{
    var res = document.createElement('SPAN');
    res.className = 'translatable';
    if (datatype)
    {
        res.datatype = datatype;
    }
    if (type)
    {
        res.type = type;
    }

    appendText(res, p_text);

    return res;
}

function createTranslatableExt(p_node, datatype, type)
{
    var res = document.createElement('SPAN');
    res.className = 'translatable';
    if (datatype)
    {
        res.datatype = datatype;
    }
    if (type)
    {
        res.type = type;
    }

    var children = p_node.childNodes;
    for (var i = 0, max = children.length; i < max; i++)
    {
        var node = children.item(i);

        switch (node.nodeType)
        {
        case 1: // element (bpt/ept/ph/it/ut)
            appendText(res, node.text);
            break;
        case 3: // text
            if (type == 'string')
            {
                appendText(res, encodeJs(node.text));
            }
            else
            {
                appendText(res, encodeHtmlEntities(node.text));
            }
            break;
        }
    }

    return res;
}

function createLocalizable(p_text, p_type)
{
    var res = document.createElement('SPAN');
    res.className = 'localizable';
    res.type = p_type;
    res.title = p_type;

    appendText(res, p_text);

    return res;
}

function createGsAddTag(name)
{
    var res = document.createElement('SPAN');
    res.className = 'gsadd';
    res.contentEditable = false;
    res.add = name;
    res.innerText = '<GS add="' + name + '"/>';

    return res;
}

function createGsAddedTag(name, added, id)
{
    var res = document.createElement('SPAN');
    res.className = 'gsadded';
    res.contentEditable = false;
    res.name = name;
    res.added = added;
    if (id)
    {
        res.id = id;
        res.innerText = '<GS name="' + name + '" added="' + added +
            '" id="' + id + '"/>';
    }
    else
    {
        res.innerText = '<GS name="' + name + '" added="' + added + '"/>';
    }

    return res;
}

function createGsRegionTag(locales)
{
    var res = document.createElement('SPAN');
    res.className = 'gsdelete';
    res.contentEditable = false;
    res._delete = 'yes';
    res.locales = locales;
    res.innerText = '<GS delete="yes" deleted="' + locales + '">';

    return res;
}

function createGsEndTag()
{
    var res = document.createElement('SPAN');
    res.className = 'gs';
    res.contentEditable = false;
    res.innerText = '</GS>';

    return res;
}

function setTransUpdated(p_trans)
{
    p_trans.removeAttribute('blockId');
    p_trans.removeAttribute('segmentId');
    p_trans.removeAttribute('wordcount');
}

// ----------------------------------------------------------

function findLine(p_container, p_line, p_col)
{
    var cnt = 0;

    var line = 1, col = 1;
    var result, node, node2;
    var children = p_container.childNodes;

    // first level consists of <span>s only
    for (var i = 0, maxi = children.length; i < maxi && !result; i++)
    {
        node = children.item(i);

        switch (node.nodeType)
        {
        case 3: // text (doesn't occur)
            break;
        case 1: // element

            if (line == p_line)
            {
                result = node.firstChild;
                continue;
            }

            // each span contains text nodes and a <BR> node at the end
            var children2 = node.childNodes;
            for (var j = 0, maxj = children2.length; j < maxj && !result; j++)
            {
                node2 = children2.item(j);

                switch (node2.nodeType)
                {
                case 1: // element
                    if (node2.nodeName == 'BR')
                    {
                        ++line;
                    }

                    if (line >= p_line)
                    {
                        if (!(result = node2.nextSibling))
                        {
                            result = node.nextSibling.firstChild;
                        }
                    }

                    break;
                case 3: // text - ignore
                    break;
                }
            }
            break;
        }
    }

    // Find the column
    if (result)
    {
        col = result.nodeValue.length;
        while (col < p_col)
        {
            if (!result.nextSibling)
            {
                // switch from skeleton to translatable or so and stop
                // looking for more if at EOF
                if (result.parentNode.nextSibling)
                {
                    result = result.parentNode.nextSibling.firstChild;
                }
                else
                {
                    break;
                }
            }
            else
            {
                var nextElem = result.nextSibling;

                // Running over EOL, return last element in line.
                if (!nextElem || nextElem.nodeName == 'BR')
                {
                    break;
                }

                result = nextElem;
            }

            col += result.nodeValue.length;
        }
    }

    return result;
}

function gotoLine(p_line, p_col)
{
    if (p_line == 0)
    {
        return;
    }

    var node = findLine(document.getElementById('idEditor'), p_line, p_col);

    if (node)
    {
        // alert("found node " + node.nodeValue);

        // node.scrollIntoView();
        var rng = document.body.createTextRange();

        if (node.nodeType == 1)
        {
            rng.moveToElementText(node);
            rng.select();
        }
        else
        {
            rng.moveToElementText(node.parentNode);
            rng.findText(node.nodeValue.replace(/\u00a0/g, " "));
            rng.select();
        }
    }
}

// ----------------------------------------------------------

function initEditorFromGxml(p_dom, p_container)
{
    // debug(p_dom.xml);
    var root = p_dom.selectSingleNode("/diplomat");
    g_gxmlLocale = root.selectSingleNode("@locale").text;
    g_gxmlVersion = root.selectSingleNode("@version").text;
    g_gxmlDatatype = root.selectSingleNode("@datatype").text;
    var node = root.selectSingleNode("@wordcount");
    g_gxmlWordcount = node ? node.text : "0";

    p_container.innerHTML = '';

    GxmlToHtml(root, p_container);

    if (!isEditableDatatype(g_gxmlDatatype))
    {
        alert("Warning: only HTML files can be edited.\n\n" +
            "Your changes will not be saved!");
    }
}

function GxmlToHtml(p_root, p_container)
{
    var elem;

    var children = p_root.childNodes;
    for (var i = 0, max = children.length; i < max; i++)
    {
        var node = children.item(i);

        if (node.nodeType != 1) continue;

        switch (node.nodeName)
        {
        case 'skeleton':
            elem = createSkeleton(node.text);

            p_container.appendChild(elem);
            break;

        case 'localizable':
            elem = createLocalizable(node.text,
                node.selectSingleNode("@type").text);

            //elem.type = node.selectSingleNode("@type").text;
            elem.blockId = node.selectSingleNode("@blockId").text;
            elem.wordcount = node.selectSingleNode("@wordcount").text;

            p_container.appendChild(elem);
            break;

        case 'translatable':
            GxmlToHtmlTrans(node, p_container);
            break;

        case 'gs':
            GxmlToHtmlGS(node, p_container);
            break;
        }
    }
}

function GxmlToHtmlTrans(p_node, p_container)
{
    var elem;

    // Calculated from segment word counts
    //var wordcount = node.selectSingleNode("@wordcount").text;
    var blockId = p_node.selectSingleNode("@blockId").text;
    var type = "text";
    if (elem = p_node.selectSingleNode("@type"))
    {
        type = elem.text;
    }
    var datatype;
    if (elem = p_node.selectSingleNode("@datatype"))
    {
        datatype = elem.text;
    }

    var children = p_node.childNodes;
    for (var i = 0, max = children.length; i < max; i++)
    {
        var node = children.item(i);

        if (node.nodeType != 1) continue;

        elem = createTranslatableExt(node, datatype, type);
        elem.blockId = blockId;
        elem.segmentId = node.selectSingleNode("@segmentId").text;
        var tmp = node.selectSingleNode("@wordcount");
        if (tmp)
        {
            elem.wordcount = tmp.text;
        }

        p_container.appendChild(elem);
    }
}

function GxmlToHtmlGS(p_node, p_container)
{
    var elem;
    var attr;

    if (attr = p_node.selectSingleNode("@added"))
    {
        var id = p_node.selectSingleNode("@id");
        elem = createGsAddedTag(
            p_node.selectSingleNode("@name").text,
            p_node.selectSingleNode("@added").text,
            id ? id.text : null);

        p_container.appendChild(elem);
        return;
    }
    else if (attr = p_node.selectSingleNode("@add"))
    {
        elem = createGsAddTag(p_node.selectSingleNode("@add").text);

        p_container.appendChild(elem);
        return;
    }
    else
    {
        var locales = "";
        var tmp = p_node.selectSingleNode("@deleted");
        if (tmp)
        {
            locales = tmp.text;
        }
        elem = createGsRegionTag(locales);
        p_container.appendChild(elem);

        GxmlToHtml(p_node, p_container);

        elem = createGsEndTag();
        p_container.appendChild(elem);
    }
}

// ----------------------------------------------------------

// Verifies GS tag structure (maybe repair GXML structure later?).
function verifyGsTags(p_container)
{
    var count = 0;

    var children = p_container.childNodes;

    for (var i = 0, max = children.length; i < max; i++)
    {
        var node = children.item(i);

        switch (node.className)
        {
        case 'gsdelete': ++count; break;
        case 'gs':       --count; break;
        }

        if (count < 0)
        {
            // GS open and close tags out of order.
            return false;
        }
    }

    return (count == 0);
}

// ----------------------------------------------------------

function HtmlToGxml(p_container)
{
    var result = new XmlDocument.create();
    result.preserveWhiteSpace = true;

    var root = result.createElement('diplomat');
    root.setAttribute('locale', g_gxmlLocale);
    root.setAttribute('version', g_gxmlVersion);
    root.setAttribute('datatype', g_gxmlDatatype);
    // needs to be recomputed on server.
    root.setAttribute('wordcount', g_gxmlWordcount);

    root.appendChild(result.createTextNode('\n'));

    result.documentElement = root;

    var decl = result.createProcessingInstruction("xml", "version=\"1.0\"");
    result.insertBefore(decl, result.childNodes.item(0));

    HtmlToGxml2(result, root, p_container);

    return result;
}

function HtmlToGxml2(p_document, p_root, p_container)
{
    var temp, elem, lastNode = new Object();
    var stack = new Array();
    var children = p_container.childNodes;

    for (var i = 0, max = children.length; i < max; i++)
    {
        var node = children.item(i);

        // Close previous translatable/segment section
        if (lastNode.className == 'translatable' &&
            node.className != 'translatable')
        {
            elem = p_root;
            p_root = stack.pop();
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
        }

        switch (node.className)
        {
        case 'skeleton':
            elem = p_document.createElement('skeleton');
            elem.text = getInnerText(node);
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        case 'translatable':
            var type = "text";
            // Start a new translatable section if necessary
            if (lastNode.className != 'translatable')
            {
                elem = p_document.createElement('translatable');
                if (temp = node.blockId)
                {
                    elem.setAttribute('blockId', temp);
                }
                if (temp = node.type)
                {
                    type = temp;
                    elem.setAttribute('type', temp);
                }
                if (temp = node.datatype)
                {
                    elem.setAttribute('datatype', temp);
                }

                stack.push(p_root);
                p_root = elem;
                p_root.appendChild(p_document.createTextNode('\n'));
            }

            elem = p_document.createElement('segment');
            if (temp = node.segmentId)
            {
                elem.setAttribute('segmentId', temp);
            }
            if (temp = node.wordcount)
            {
                elem.setAttribute('wordcount', temp);
            }
            if (type == "string")
            {
                elem.text = decodeJs(getInnerText(node));
            }
            else
            {
                elem.text = getInnerText(node);
            }
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        case 'localizable':
            elem = p_document.createElement('localizable');
            if (temp = node.blockId)
            {
                elem.setAttribute('blockId', temp);
            }
            if (temp = node.type)
            {
                elem.setAttribute('type', temp);
            }
            if (temp = node.wordcount)
            {
                elem.setAttribute('wordcount', temp);
            }
            elem.text = getInnerText(node);
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        case 'gsdelete':
            elem = p_document.createElement('gs');
            elem.setAttribute('delete', 'yes');
            elem.setAttribute('deleted', node.locales);

            stack.push(p_root);
            p_root = elem;
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        case 'gs':
            elem = p_root;
            p_root = stack.pop();
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        case 'gsadd':
            elem = p_document.createElement('gs');
            elem.setAttribute('add', node.add);
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        case 'gsadded':
            elem = p_document.createElement('gs');
            elem.setAttribute('name', node.name);
            elem.setAttribute('added', node.added);
            if (node.id)
            {
                elem.setAttribute('id', node.id);
            }
            p_root.appendChild(elem);
            p_root.appendChild(p_document.createTextNode('\n'));
            break;

        default:
            if (node.tagName != 'BR')
            {
                debug("unknown node " + node.tagName + ": " + node.innerHTML);
            }
            break;
        }

        lastNode = node;
    }
}

function getInnerText(p_node)
{
    var result = p_node.innerHTML;

    // For HP segments consisting of a single nbsp.
    if (result.match(/^&nbsp;$/))
    {
        return "\u00a0";
    }

    result = result.replace(/<br>/gi, "\n");
    // Because of using innerHTML, need to undo encoding.
    result = result.replace(/<[^>]*>/ig, "");
    result = result.replace(/&nbsp;/g, " ");
    result = result.replace(/&lt;/g, "<").replace(/&gt;/g, ">");
    result = result.replace(/&quot;/g, "\"").replace(/&amp;/g, "&"); // "

    // alert(p_node.innerHTML + " ---> " + result);

    return result;
}

function encodeHtmlEntities(p_text)
{
    return p_text.replace(/&/g, "&amp;").replace(/</g, "&lt;").
        replace(/>/g, "&gt;").replace(/\"/g, "&quot;"); //"
}

// For displaying JS strings in HTML, encode \r\n
function encodeJs(p_text)
{
    return p_text.replace(/\n/g, "\\n").replace(/\r/g, "\\r").
        replace(/\t/g, "\\t").replace(/\u00a0/g, "\\u00a0");
}

// For sending JS strings back in GXML, we don't decode \r\n - the
// backend will do the right thing (tm). Maybe \t doesn't need to be
// decoded either.
function decodeJs(p_text)
{
    return p_text./*replace(/\\n/g, "\n").replace(/\\r/g, "\r").*/
        replace(/\\t/g, "\t").replace(/\\u00a0/g, "\u00a0");
}

function isEditableDatatype(p_type)
{
    switch (p_type)
    {
    case "html":
    case "word-html":
    case "excel-html":
    case "powerpoint-html":
    case "cfm":
    case "asp":
    case "jsp":
    case "jhtml":
        return true;
    }

    return false;
}

function isHtmlDatatype(p_type)
{
    switch (p_type)
    {
    case "html":
    case "word-html":
    case "excel-html":
    case "powerpoint-html":
        return true;
    }

    return false;
}
