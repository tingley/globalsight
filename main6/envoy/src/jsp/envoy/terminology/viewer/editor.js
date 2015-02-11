//                           -*- Mode: Javascript -*-
//
// Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
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

// Does viewer show an entry that can be edited?
var g_canEdit = false;
// Are we already editing an entry?
var g_editing = false;
var g_dirty = false;

// ConceptId and TermId of the currently edited entry
var g_NEWENTRY = 0;
var g_conceptId = 0;
var g_termId = 0;

// Lock cookie for the entry (a DOMDocument I believe)
var g_lock = null;
// Extracted name of owner of the lock
var g_lockOwner = null;
// optional: Extracted email address lock owner
var g_lockOwnerEmail = null;

var g_validationWindow = null;

var g_entry = null;              // pointer to editable entry
var g_fields = null;             // selectable fields object
var g_selected = null;           // currently selected node

var g_editorEntry = null;        // XML Document

// Array of language names and locales from the termbase definition
var g_termbaseLanguages = new Array();
// Array of fields defined in the termbase
var g_termbaseFields = new Array();

// flag if all languages are present in entry
var g_canAddLanguage = true;

function LangLoc(language, locale)
{
    this.language = language;
    this.locale = locale;
}

LangLoc.prototype.getLanguage = function ()
{
    return this.language;
}

LangLoc.prototype.getLocale = function ()
{
    return this.locale;
}

LangLoc.prototype.getTerm = function ()
{
    return this.term;
}

LangLoc.prototype.setTerm = function (term)
{
    this.term = new String(term);
}

LangLoc.prototype.toString = function ()
{
    return "[" + this.language + "," + this.locale + "]";
}


function FieldParameters()
{
    this.level = null;
    this.type = null;
    this.value = null;
    this.language = null;
    this.window = window;
    this.definedFields = g_termbaseFields;
    this.termbaseLanguages = g_termbaseLanguages;
}

FieldParameters.prototype.setLevel = function (param)
{
    this.level = new String(param);
}

FieldParameters.prototype.getLevel = function ()
{
    return this.level;
}

FieldParameters.prototype.setType = function (param)
{
    this.type = new String(param);
}

FieldParameters.prototype.getType = function ()
{
    return this.type;
}

FieldParameters.prototype.setValue = function (param)
{
    this.value = new String(param);
}

FieldParameters.prototype.getValue = function ()
{
    return this.value;
}

FieldParameters.prototype.setDefinedFields = function (param)
{
    this.definedFields = param;
}

FieldParameters.prototype.getDefinedFields = function ()
{
    return this.definedFields;
}

FieldParameters.prototype.setTermbaseLanguages = function (param)
{
    this.termbaseLanguages = param;
}

FieldParameters.prototype.getTermbaseLanguages = function ()
{
    return this.termbaseLanguages;
}

FieldParameters.prototype.setLanguage = function (param)
{
    this.language = param;
}

FieldParameters.prototype.getLanguage = function ()
{
    return this.language;
}

FieldParameters.prototype.toString = function ()
{
    return "[" + this.level + ": " + this.type + "=" + this.value + "]";
}


function EditTermParameters(language, term, termId)
{
    this.language = language;
    this.term = term;
    this.termId = termId;
}

EditTermParameters.prototype.getLanguage = function ()
{
    return this.language;
}

EditTermParameters.prototype.getTermId = function ()
{
    return this.termId;
}

EditTermParameters.prototype.setTerm = function (param)
{
    this.term = new String(param);
}

EditTermParameters.prototype.getTerm = function ()
{
    return this.term;
}

EditTermParameters.prototype.toString = function ()
{
    return "[" + this.language + ": " + this.term + "]";
}


function ApprovalParameters(status)
{
    this.status = status;
}

ApprovalParameters.prototype.setStatus = function (param)
{
    this.status = new String(param);
}

ApprovalParameters.prototype.getStatus = function ()
{
    return this.status;
}


function ValidationParameters(result)
{
    this.result = result;
    this.window = window;
}

ValidationParameters.prototype.getResult = function ()
{
    return this.result;
}

ValidationParameters.prototype.getWindow = function ()
{
    return this.window;
}


function PrintArgs(termbase, cid, entry)
{
    this.termbase = termbase;
    this.cids = new Array();
    this.cids.push(cid);
    this.entries = new Array();
    this.entries.push(entry);
}

PrintArgs.prototype.toString = function ()
{
    return "[PrintArgs " + this.termbase + "]";
}

function normalizeWS(s)
{
    return s.replace(/\s+/g, ' ');
}

// When retrieving data with node.innerHTML(), some characters
// come back as entities. Examples: soft-hyphen (&shy;, \u00ad).
function fixHtmlEntities(s)
{
    s = s.replace(/&shy;/g,  '\u00ad');
    s = s.replace(/&nbsp;/g, '\u00a0');

    return s;
}

// Some fields like terms (and attribute fields) contain tokens that
// should never contain soft hyphens.
function removeSpecialChars(s)
{
    s = s.replace(/&shy;/g,  '');
    s = s.replace(/\u00ad/g,  '');
    // This is questionable. NBSP *is* a valid char, e.g. "Mr.~T"
    // s = s.replace(/&nbsp;/g, ' ');
    // s = s.replace(/\00a0/g, ' ');

    return s;
}

function setSelectedItem()
{
    g_selected = g_fields.getSelectedItems();

    updateMenu();
}

function getFieldType(div)
{
    var c = div.className;
    var res = c.split(' ');
    return res[0];
}

function updateMenu()
{
    var idRemoveLanguage = document.getElementById("idRemoveLanguage");
    var idRemoveTerm = document.getElementById("idRemoveTerm");
    var idRemoveField = document.getElementById("idRemoveField");
    var idEditField = document.getElementById("idEditField");
    var idAddFieldTo = document.getElementById("idAddFieldTo");
    var idAddFieldAfter = document.getElementById("idAddFieldAfter");
    var idAddLanguage = document.getElementById("idAddLanguage");
    
    if (g_selected && g_selected.length > 0)
    {
        var sel = g_selected[0];
        var fieldType = getFieldType(sel);

        if (fieldType == 'conceptGrp')
        {
            idRemoveLanguage.disabled = false;
            idRemoveLanguage.className = "menuItem";
            idRemoveTerm.disabled = true;
            idRemoveTerm.className = "menuItemD";
            idRemoveField.disabled = true;
            idRemoveField.className = "menuItemD";
            idEditField.disabled = true;
            idEditField.className = "menuItemD";
            idAddFieldTo.disabled = false;
            idAddFieldTo.className = "menuSubItem";
            idAddFieldAfter.disabled = true;
            idAddFieldAfter.className = "menuSubItemD";
        }
        else if (fieldType == 'fakeLanguageGrp' || fieldType == 'languageGrp')
        {
            idRemoveLanguage.disabled = false;
            idRemoveLanguage.className = "menuItem";
            idRemoveTerm.disabled = true;
            idRemoveTerm.className = "menuItemD";
            idRemoveField.disabled = true;
            idRemoveField.className = "menuItemD";
            idEditField.disabled = true;
            idEditField.className = "menuItemD";
            idAddFieldTo.disabled = false;
            idAddFieldTo.className = "menuSubItem";
            idAddFieldAfter.disabled = true;
            idAddFieldAfter.className = "menuSubItemD";
        }
        else if (fieldType == 'fakeTermGrp' || fieldType == 'termGrp')
        {
            idRemoveLanguage.disabled = true;
            idRemoveLanguage.className = "menuItemD";
            idRemoveTerm.disabled = false;
            idRemoveTerm.className = "menuItem";
            idRemoveField.disabled = true;
            idRemoveField.className = "menuItemD";
            idEditField.disabled = false;
            idEditField.className = "menuItem";
            idAddFieldTo.disabled = false;
            idAddFieldTo.className = "menuSubItem";
            idAddFieldAfter.disabled = true;
            idAddFieldAfter.className = "menuSubItemD";
        }
        else if (fieldType == 'fieldGrp')
        {
            idRemoveLanguage.disabled = true;
            idRemoveLanguage.className = "menuItemD";
            idRemoveTerm.disabled = true;
            idRemoveTerm.className = "menuItemD";
            idRemoveField.disabled = false;
            idRemoveField.className = "menuItem";
            idEditField.disabled = false;
            idEditField.className = "menuItem";
            idAddFieldAfter.disabled = false;
            idAddFieldAfter.className = "menuSubItem";

            if (sel.firstChild.type == 'note')
            {
                idAddFieldTo.disabled = true;
                idAddFieldTo.className = "menuSubItemD";
            }
            else
            {
                idAddFieldTo.disabled = false;
                idAddFieldTo.className = "menuSubItem";
            }
        }
        else {
            idAddFieldTo.disabled = true;
            idAddFieldAfter.disabled = true;
            idEditField.disabled = true;
            idRemoveField.disabled = true;
            idRemoveLanguage.disabled = true;
            idRemoveTerm.disabled = true;
        }
    }
    else
    {
        // standard menu
        idRemoveLanguage.disabled = true;
        idRemoveLanguage.className = "menuItemD";
        idRemoveTerm.disabled = true;
        idRemoveTerm.className = "menuItemD";
        idRemoveField.disabled = true;
        idRemoveField.className = "menuItemD";
        idEditField.disabled = true;
        idEditField.className = "menuItemD";
        idAddFieldTo.disabled = true;
        idAddFieldTo.className = "menuSubItemD";
        idAddFieldAfter.disabled = true;
        idAddFieldAfter.className = "menuSubItemD";
    }

    if (g_canAddLanguage)
    {
        idAddLanguage.disabled = false;
        idAddLanguage.className = "menuItem";
    }
    else
    {
        idAddLanguage.disabled = true;
        idAddLanguage.className = "menuItemD";
    }
    
    // Enable or Disable "Validate Entry" and "Approve Entry" Menu.
    updateValidateAndApproveEntry();
}

function updateViewerMenu(bShow)
{
    // bShow == true means viewer is showing an entry
    var idCreateButton = document.getElementById("idCreateButton");
    var idEditButton = document.getElementById("idEditButton");
    var idPrintViewerButton = document.getElementById("idPrintViewerButton");

    if (bShow){
        idPrintViewerButton.disabled = false;
    }

    if (g_editing)
    {
        g_canEdit = bShow;
    }
    else
    {
       if (idCreateButton != null) {
          idCreateButton.disabled = false;
       }

        if (bShow)
        {
            g_canEdit = true;
            if (idEditButton != null) {
               idEditButton.disabled = false;
            }
        }
        else
        {
            g_canEdit = false;

            if (idEditButton != null) {
               idEditButton.disabled = false;
            }
        }
    }
}

//Enable or Disable "Validate Entry" and "Approve Entry" Menu, depends on g_entry.
function updateValidateAndApproveEntry()
{
    var idValidateEntry = document.getElementById("idValidateEntry");
    var idApproveEntry = document.getElementById("idApproveEntry");
    
    if(getLangLocsInEntry().length == 0)
    {
        // Disable idValidateEntry and idApproveEntry, when new Entry.
        idValidateEntry.disabled = true;
        idValidateEntry.className = "menuItemD";
        idApproveEntry.disabled = true;
        idApproveEntry.className = "menuItemD";
    }
    else
    {
    	// Enable idValidateEntry and idApproveEntry.
        idValidateEntry.disabled = false;
        idValidateEntry.className = "menuItem";
        idApproveEntry.disabled = false;
        idApproveEntry.className = "menuItem";
    }
}

function doEdit(field, event)
{
    var fieldType = getFieldType(field);

    if (fieldType == 'conceptGrp' || fieldType == 'languageGrp')
    {
        return;
    }

    if (fieldType == 'termGrp')
    {
        field = field.firstChild; // select the fakeTermGrp
        fieldType = getFieldType(field);
    }

    var type;
    // var value = field.children(1).innerHTML; // returns expanded urls
    var value = getXhtml(field.children[1]);
    var newValue = null;

    value = fixHtmlEntities(value);

    if (fieldType == 'fakeTermGrp')
    {
        if(document.all) {
            objField =  field.parentElement.parentElement;
        }
        else {
            objField = field.parentNode.parentNode;
        }
        var language = objField.firstChild.children[1].innerText;
        
        var termId = field.children[1].getAttribute("termId");

        var params = new EditTermParameters(language, value);
        
        if(termId != null) {
            params = new EditTermParameters(language, value, termId);
        }

        var res = window.showModalDialog(
            '/globalsight/envoy/terminology/viewer/EditTerm.html', params,
            "dialogHeight:200px; dialogWidth:350px; center:yes; " +
            "resizable:no; status:no; help:no;");

        if (res != null)
        {
            newValue = removeSpecialChars(normalizeWS(res.getTerm()));
        }
    }
    else if (fieldType == 'fieldGrp')
    {
        type = field.firstChild.getAttribute("type");

        var level = getParentLevel(field);

        // Concept status is handled special (subject to permissions).
        if (level == 'concept' && type == 'status')
        {
            ApproveEntry();
            return cancelEvent(event);
        }

        var language = getParentLanguage(field);

        var params = new FieldParameters();
        params.setLevel(level);
        params.setType(type);
        params.setValue(value);
        params.setLanguage(language);

        // EditField.html: 400h x 370w
        var res = window.showModalDialog(
            '/globalsight/envoy/terminology/viewer/EditField2.html', params,
            "dialogHeight:600px; dialogWidth:520px; center:yes; " +
            "resizable:yes; status:no; help:no; ");

        if (res != null)
        {
            // debug(res.getValue());
            newValue = normalizeWS(res.getValue());
        }
    }
    else
    {
        // debug("Fieldtype=" + fieldType + " - level=" + level);
        type = field.firstChild.innerText;

        var newValue = prompt("Enter new value for " + type + ":", value);
    }

    if (newValue != null)
    {
        field.children[1].innerHTML = newValue;
        g_dirty = true;
    }

    return cancelEvent(event);
}

function doDelete(field)
{
    if (getFieldType(field).indexOf('fake') == 0)
    {
        field = field.parentElement || field.parentNode;
    }

    var type = getFieldType(field);

    if (type == 'termGrp')
    {
        RemoveTerm();
    }
    else if (type == 'languageGrp')
    {
        RemoveLanguage();
    }
    else if (type == 'conceptGrp')
    {
        // do nothing, can't delete entry
        // alert("The entry cannot be deleted.");
    }
    else
    {
        RemoveField();
    }
}

function doKeydown(event,entry)
{
    var evt = event ? event : (window.event ? window.event : null);
    var key = evt.keyCode;

    // window.status = "Keycode: " + key;

    // Viewer mode key bindings
    if (event.ctrlKey && !event.altKey)
    {
        if (key == 69) // Letter E
        {
            // StartEditing();
            return cancelEvent(event); // cancel IE's system dialog
        }
        else if (key == 72) // Letter H
        {
            return cancelEvent(event); // cancel IE's system dialog
        }
        else if (key == 73) // Letter I
        {
            return cancelEvent(event); // cancel IE's system dialog
        }
        else if (key == 78) // Letter N
        {
            return cancelEvent(event); // cancel IE's system dialog
        }
        else if (key == 79) // Letter O
        {
            event.keyCode = 0;
            return cancelEvent(event); // cancel IE's system dialog
        }
        else if (key == 82) // Letter R
        {
            return cancelEvent(event); // cancel IE's reload
        }
    }

    // Editor mode bindings not active when editor not active
    if (!g_editing)
    {
        if (key == 13) { // Return
            return false;
        }
    }

    if (key == 35) // End
    {
        doSelectLast();
        return cancelEvent(event);
    }
    else if (key == 36) // Home
    {
        doSelectFirst();
        return cancelEvent(event);
    }
    else if (key == 38) // Arrow Up
    {
        doSelectPrevious();
        return cancelEvent(event);
    }
    else if (key == 40) // Arrow Down
    {
        doSelectNext();
        return cancelEvent(event);
    }
    else if (key == 13) // Return
    {
        if (g_selected)
        {
            doEdit(g_selected[0],event);
        }
        return cancelEvent(event);
    }
    else if (key == 46) // Delete
    {
        if (g_selected)
        {
            doDelete(g_selected[0]);
        }
        return cancelEvent(event);
    }

    if (event.ctrlKey && !event.altKey)
    {
        if (key == 65) // Letter A
        {
            if (idAddFieldAfter.disabled == false)
            {
                AddFieldAfterCurrent(event);
            }
            else
            {
                AddFieldToCurrent(event);
            }
            return cancelEvent(event);
        }
        else if (key == 68) // Letter D
        {
            AddFieldToCurrent(event);
            return cancelEvent(event);
        }
        else if (key == 70) // Letter F
        {
            AddFieldToCurrent(event);
            return cancelEvent(event);
        }
        else if (key == 76) // Letter L
        {
            AddLanguage(event);
            return cancelEvent(event);
        }
        else if (key == 81) // Letter Q
        {
            ValidateEntry(event);
            return cancelEvent(event);
        }
        else if (key == 83) // Letter S
        {
            SaveEntry(event);
            return cancelEvent(event);
        }
        else if (key == 84) // Letter T
        {
            AddTerm(event);
            return cancelEvent(event);
        }
    }
}

function doSelectFirst()
{
    var item = g_entry;

    if (item)
    {
        fileClick(item);
        item.scrollIntoView(true);
    }
}

function doSelectLast()
{
    var item = g_fields._getLastDescendant(g_entry);

    if (item)
    {
        fileClick(item);
        item.scrollIntoView(false);
    }
}

function doSelectNext()
{
    if (g_selected == null)
    {
        return doSelectFirst();
    }

    var item = g_selected[0];
    item = g_fields.getNext(item);

    if (item != null)
    {
        //item.click();
        fileClick(item);
        item.scrollIntoView(false);
    }
}

function doSelectPrevious()
{
    if (g_selected == null)
    {
        return doSelectLast();
    }

    var item = g_selected[0];
    item = g_fields.getPrevious(item);

    if (item != null)
    {
        //item.click();
        fileClick(item);
        item.scrollIntoView(true);
    }
}

function TermbaseError(strMessage, bFatal)
{
    ShowError(strMessage);
}

function compareLangLocs(a,b)
{
    var aname = a.getLanguage();
    var bname = b.getLanguage();

    // Only in JScript 5.5
    try
    {
        return aname.localeCompare(bname);
    }
    catch (e)
    {
        if (aname == bname) return 0;
        if (aname >  bname) return 1;
        return -1;
    }
}

function setTermbaseLanguages(p_definition)
{
    // compute cached array of allowed languages
    //var nodes = p_definition.selectNodes("//language");
	var nodes = $(p_definition).find("language");

    for (i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        //var langloc = new LangLoc(node.selectSingleNode("name").text,
            //node.selectSingleNode("locale").text);
        var langloc = new LangLoc($(node).find("name").text(),$(node).find("locale").text());

        g_termbaseLanguages.push(langloc);
    }

    g_termbaseLanguages.sort(compareLangLocs);
}

function setTermbaseFields(p_definition)
{
    // compute cached array of known fields
    //var nodes = p_definition.selectNodes("/definition/fields/field");
	var nodes = $(p_definition).find("definition fields field");
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        var name = $(node).find("name").text();
        var type = $(node).find("type").text();
        var system =
          ($(node).find("system").text() == "true" ? true : false);
        var values = $(node).find("values").text();
        var format = getFieldFormatByType(type);

        var field = new Field(name, type, format, system, values);

        g_termbaseFields.push(field);
    }
    //debug(g_termbaseFields);
}

function setEditorEntry2(xmlDocument)
{
    g_editorEntry = xmlDocument;

    try
    {
        var strHTML = XmlToHtml(g_editorEntry,
            new MappingContext(g_termbaseFields, aFieldTypes, "editor"));

        document.getElementById("idEditorEntry").innerHTML = strHTML;
    }
    catch (ex)
    {
        TermbaseError("Cannot format entry: " + ex + ".\n" +
            ex.description, false);

        return;
    }
}

// After entry has been set, see if it contains all languages or not
function setCanAddLanguage()
{
    var langs = getLangLocsInEntry();

    if (langs.length == g_termbaseLanguages.length)
    {
        g_canAddLanguage = false;
    }
    else
    {
        g_canAddLanguage = true;
    }
}

function ApproveEntry()
{
    var xml = HtmlToXml(g_entry);
    var emptyData = "<conceptGrp><concept></concept></conceptGrp>";
    if(xml==null || xml.replace(/\n/g,'')==emptyData)
    {
        return;
    }
    
    var params = new ApprovalParameters('');

    var res = window.showModalDialog(
        '/globalsight/envoy/terminology/viewer/Approve.html', params,
        "dialogHeight:200px; dialogWidth:350px; center:yes; " +
        "resizable:no; status:no; help:no;"); 

    if (res != null && res.getStatus() != '')
    {
        setApprovalStatus(res.getStatus());
    }
}

function CancelEditing()
{
    StopEditing(true);
}

function StartEditing(p_createNew)
{
    if (bAnonymous == "true")
    {
        alert("You are logged in as anonymous user " +
            "or your session has expired.\n" +
            "You are not allowed to edit or create entries.");
        return;
    }

    if (!g_editing && (g_canEdit || p_createNew))
    {
        if (!p_createNew)
        {
            if (!g_canEditMath)
            {
                var xmlDoc = GetEntryXml(); 
                var xml = xmlDoc.xml;
                if (xml.match(/<m:math[^>]*>/))
                {
                    if (confirm("This entry contains mathematical formulas.\n" +
                        "Do you want to install MathPlayer required to edit them?"))
                    {
                        window.open("http://www.dessci.com/en/products/mathplayer/download.htm", "_blank");
                    }

                    return;
                }
            }
            
            LockEntry(p_createNew);
        }
        else 
        {
            EditingSet(p_createNew);
        }
    }
    else if (g_editing)
    {
        // probably user wants to see editor?
        showEditor();
    }
}

function EditingSet(p_createNew) {
    g_editing = true;

    var idCreateButton = document.getElementById("idCreateButton");
    var idEditButton = document.getElementById("idEditButton");

    if (idCreateButton != null) {
        idCreateButton.disabled = true;
    }

    if (idEditButton != null) {
        idEditButton.disabled = true;
    }
     
    initEditor(p_createNew);

    showEditor();
}

function StopEditingForDelete()
{
    UnlockEntry();

    g_editing = false;
    g_dirty = false;
    g_canEdit = false;
    hideEditor();

    var idCreateButton = document.getElementById("idCreateButton");
    var idEditButton = document.getElementById("idEditButton");
    var idPrintViewerButton = document.getElementById("idPrintViewerButton");
    idCreateButton.disabled = false;
    idEditButton.disabled = true;
    idPrintViewerButton.disabled = true;
}

function StopEditing(reload)
{
    if (g_dirty && !confirm("Discard changes?"))
    {
        return;
    }

    try
    {
        g_validationWindow.close();
    }
    catch (ignore) {}

    UnlockEntry();

    g_editing = false;
    g_dirty = false;

    hideEditor();

    updateViewerMenu(g_canEdit);
    
    // if wished, reload the viewer's entry
    if (reload && g_conceptId != g_NEWENTRY)
    {
        GetEntry(g_conceptId, g_termId);
    }
}

// Tries to lock an entry; the call returns an XML object with
// a cookie inside if successful, or the lock object of a different
// user without the cookie if not successful. Sets the user id
// of the user holding the lock as a side effect.
function LockEntry(p_createNew)
{
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"LockEntry", steal:false, conceptId:nCid},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     
        	     if (result == 'error')
               {
                   TermbaseError("lock entry failed", false);
               }
               else {
                   g_lock = StrToXML(result);
                   
                   if (g_lock != null)
                    {
                        g_lockOwner = g_lock.selectSingleNode('//who').text;
                        g_lockOwnerEmail = g_lock.selectSingleNode('//email').text;
                    }
                    else
                    {
                        g_lockOwner = "unknown";
                        g_lockOwnerEmail = "unknown";
                    }

                    if (g_lock == null || g_lock.selectSingleNode('//cookie') == null)
                    {
                        if (confirm("This entry is being edited by: " + g_lockOwner +
                            ".\nDo you want to override this user's lock?"))
                       {
                            LockEntry2(p_createNew);
                       }
                    }
                    else {
                        EditingSet(p_createNew);
                    }
               }
           }
       },
       error:function(error)
       {
       }
   }); 
}

function LockEntry2(p_createNew)
{
    dojo.xhrPost(
    {
       url:ControllerURL,
       handleAs: "text",
       content: {action:"LockEntry", steal:true, conceptId:nCid},
       load:function(data)
       {
           var returnData = eval(data);

           if (returnData.error)
           {
        	   alert(returnData.error);
           }
           else
           {
        	     var result = returnData.result;
        	     
        	     if (result == 'error')
               {
                   TermbaseError("lock entry failed", false);
               }
               else {
                   g_lock = StrToXML(result);
                   
                   if (g_lock != null)
                    {
                        g_lockOwner = g_lock.selectSingleNode('//who').text;
                        g_lockOwnerEmail = g_lock.selectSingleNode('//email').text;
                    }
                    else
                    {
                        g_lockOwner = "unknown";
                        g_lockOwnerEmail = "unknown";
                    }
                
                    if (g_lock == null || g_lock.selectSingleNode('//cookie') == null)
                    {
                        alert("The lock held by " + g_lockOwner +
                            " cannot be overridden.");
                    }
                    else {
                        EditingSet(p_createNew);
                    }
               }
           }
       },
       error:function(error)
       {
       }
   }); 
}

function UnlockEntry()
{
    // New entries created in the editor have no lock.
    if (g_lock != null)
    {
        dojo.xhrPost(
        {
           url:ControllerURL,
           handleAs: "text",
           content: {action:"UnLockEntry", lockStr:g_lock.xml, conceptId:nCid},
           load:function(data)
           {
               var returnData = eval(data);
               if (result == 'error')
               {
                   TermbaseError("unlock entry failed", false);
               }
           },
           error:function(error)
           {
           }
       }); 
       
        g_lock = null;
    }
}

function PrintEntry(b_fromEditor)
{
    var termbase = strTermbase;
    var cid;
    var dom;

    if (b_fromEditor)
    {
        cid = g_conceptId;
        dom = g_editorEntry;
    }
    else
    {
        cid = nCid;
        dom = GetEntryXml();
    }

    var xml = XmlToHtml(dom, new MappingContext(
        g_termbaseFields, aFieldTypes, "viewer"));

    // debug("Printing entry " + xml);

    var printargs = new PrintArgs(termbase, cid, xml);

    window.showModalDialog(
        '/globalsight/envoy/terminology/viewer/print.html', printargs,
        "dialogHeight:500px; dialogWidth:600px; center:yes; " +
        "resizable:yes; status:no; help:no;");
}

// public methods for structural entry editing

function AddLanguage()
{
    var availLangLocs = getLangLocsNotInEntry();

    if (availLangLocs.length == 0)
    {
        return;
    }

    //debug("avail: " + availLangLocs);

    // Show dialog to select one of the languages and enter a term
    var res = window.showModalDialog(
        '/globalsight/envoy/terminology/viewer/AddLanguage.html', availLangLocs,
        "dialogHeight:200px; dialogWidth:350px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res != null && res.getTerm() != '')
    {
        var newValue = removeSpecialChars(normalizeWS(res.getTerm()));

        insertLanguage(res.getLanguage(), res.getLocale(), newValue);
    }
}

function RemoveLanguage()
{
    var sel = g_selected ? g_selected[0] : null;

    if (sel)
    {
        var fieldType = getFieldType(sel);

        if (fieldType == 'fakeLanguageGrp')
        {
            sel = sel.parentElement || sel.parentNode;
            fieldType = getFieldType(sel);
        }

        if (getFieldType(sel) == 'languageGrp' &&
            confirm("Removing this language causes all terms and dependent fields to be deleted.\nDo you want to continue?"))
        {
            var nextSel = sel.nextSibling;
            if (!nextSel) nextSel = sel.previousSibling;
            if (!nextSel) nextSel = sel.parentElement || sel.parentNode;

            sel.removeNode(true);

            if (nextSel)
            {
                //nextSel.click();
                fileClick(nextSel);
                nextSel.scrollIntoView(false);
            }

            setCanAddLanguage();
        }
    }

    g_dirty = true;

    // g_selected = null;
    updateMenu();
}

function AddTerm(inputmodel)
{
    var language = null;
    var term = null;

    var langlocs = getLangLocsInEntry();

    if (langlocs.length == 0)
    {
        // Entry has no language yet: delegate to AddLanguage().
        return AddLanguage();
    }

    // Find the default language for the term.
    if (g_selected)
    {
        // derive language from selection
        var sel = g_selected[0];

        while (sel != g_entry && getFieldType(sel) != 'languageGrp')
        {
            sel = sel.parentElement || sel.parentNode;
        }

        if (sel != g_entry)
        {
            language = sel.firstChild.children[1].innerText;
        }
    }

    // put default language first in langlocs
    if (language != null)
    {
        var langloc;

        for (var i = 0; i < langlocs.length; i++)
        {
            langloc = langlocs[i];

            if (language == langloc.getLanguage())
            {
                break;
            }
        }

        langlocs.splice(i, 1);
        langlocs.unshift(langloc);
    }

    var res = window.showModalDialog(
        '/globalsight/envoy/terminology/viewer/AddTerm.html', langlocs,
        "dialogHeight:200px; dialogWidth:350px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res == null)
    {
        return;
    }

    language = res.getLanguage();
    
    term = removeSpecialChars(normalizeWS(res.getTerm()));

    if (term != null && term != '')
    {
        insertTerm(language, term);
    }
}

function RemoveTerm()
{
    var sel = g_selected ? g_selected[0] : null;

    if (sel)
    {
        
        var fieldType = getFieldType(sel);

        if (fieldType == 'fakeTermGrp')
        {
            sel = sel.parentElement || sel.parentNode;
            fieldType = getFieldType(sel);
        }

        if (fieldType == 'termGrp' &&
            confirm("Removing this term causes all dependent fields to be deleted.\nDo you want to continue?"))
        {
            var nextSel = sel.nextSibling;
            if (!nextSel) nextSel = sel.previousSibling;
            if (!nextSel) nextSel = sel.parentElement || sel.parentNode;

            sel.removeNode(true);
            sel = null;

            if (nextSel)
            {
                //nextSel.click();
                nextSel.scrollIntoView(false);
            }
        }
    }

    g_dirty = true;

    // g_selected = null;
    updateMenu();
}

function AddFieldToCurrent()
{
    if (g_selected == null || idAddFieldTo.disabled == true)
    {
        return;
    }

    // Determine where in the structure we are
    var sel = g_selected[0];
    var level = getLevel(sel);
    var type = null;

    if (level == 'field')
    {
        type = sel.firstChild.getAttribute("type");
    }
    else if (level == 'term' && getFieldType(sel) == 'fakeTermGrp')
    {
        sel = sel.parentElement || sel.parentNode;
    }

    var language = getParentLanguage(sel);

    var params = new FieldParameters();
    params.setLevel(level);
    params.setType(type);
    params.setLanguage(language);

    // AddField.html: 400hx370w
    var res = window.showModalDialog(
        '/globalsight/envoy/terminology/viewer/AddField2.html', params,
        "dialogHeight:600px; dialogWidth:520px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res == null)
    {
        return;
    }

    var name = getFieldNameByType(res.getType(), g_termbaseFields);

    insertFieldInCurrent(sel, level, name, res.getType(), res.getValue());
}

// Situations:
// 1) concept level field selected, add at concept level
// 2) language level field selected, add at language level
// 3) term level field selected, add at term level
// 4) embedded in fieldGrp (must be source, note (transac)), add in field
function AddFieldAfterCurrent()
{
    if (g_selected == null || idAddFieldAfter.disabled == true)
    {
        return;
    }

    // Determine where in the structure we are
    var sel = g_selected[0];
    var level = getParentLevel(sel);

    var language = getParentLanguage(sel);

    var params = new FieldParameters();
    params.setLevel(level);
    params.setLanguage(language);

    // AddField.html: 400hx370w
    var res = window.showModalDialog(
        '/globalsight/envoy/terminology/viewer/AddField2.html', params,
        "dialogHeight:600px; dialogWidth:500px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res == null)
    {
        return;
    }

    var name = getFieldNameByType(res.getType(), g_termbaseFields);

    insertFieldAfterCurrent(sel, name, res.getType(), res.getValue());
}

function EditField()
{
    if (g_selected == null)
    {
        return;
    }

    var sel = g_selected[0];

    return doEdit(sel);
}

function RemoveField(event)
{
    cancelEvent(event);

    var sel = g_selected ? g_selected[0] : null;

    if (sel)
    {
        var fieldType = getFieldType(sel);

        if (fieldType == 'fieldGrp' &&
            confirm("Removing this field causes all dependent fields to be deleted.\nDo you want to continue?"))
        {
            var nextSel = sel.nextSibling;
            if (!nextSel || !g_fields.isItem(nextSel)) nextSel = sel.previousSibling;
            if (!nextSel || !g_fields.isItem(nextSel)) nextSel = sel.parentElement || sel.parentNode;

            sel.removeNode(true);
            sel = null;

            if (nextSel)
            {
                nextSel.click();
                nextSel.scrollIntoView(false);
            }
        }
    }

    g_dirty = true;

    updateMenu();
}

//
// helper methods for structural entry editing
//

function insertLanguage(language, locale, term)
{
    var div = getLanguageGrpDiv(language, locale, term);

    insertHtml('beforeEnd', g_entry, div);
    //g_entry.insertAdjacentHTML('beforeEnd', div);

    g_dirty = true;

    initSelection();

    var node = g_entry.lastChild;
    //node.click();
    fileClick(node);
    node.scrollIntoView(false);
}

function insertTerm(language, term)
{
    var langGrp = findLanguageGrp(language);
    var div = getTermGrpDiv(term);
    insertHtml('beforeEnd',langGrp, div);
    //langGrp.insertAdjacentHTML('beforeEnd', div);

    g_dirty = true;

    initSelection();

    var node = langGrp.lastChild;
    fileClick(node);
    node.scrollIntoView(false);
}

function insertFieldAfterCurrent(node, name, type, value)
{
    var div = getFieldGrpDiv(name, type, value);
    insertHtml('afterEnd', node, div);

    g_dirty = true;

    initSelection();

    node = node.nextSibling;
    fileClick(node);
    node.scrollIntoView(false);
}

function insertFieldInCurrent(node, level, name, type, value)
{
    var div = getFieldGrpDiv(name, type, value);
    var pos;

    // level == concept: <span class=fakeConceptGrp>
    // level == language: <span class=fakeLanguageGrp>
    // level == term: <div class=fakeTermGrp>
    // level == field: <span class=fieldlabel><span class=fieldvalue>
    // level == source: <span class=fieldlabel><span class=fieldvalue>

    if (level == 'concept')
    {
        pos = node.firstChild;

        while (pos.nextSibling && getFieldType(pos.nextSibling) == 'transacGrp')
        {
            pos = pos.nextSibling;
        }

        //pos.insertAdjacentHTML('afterEnd', div);
        insertHtml('afterEnd', pos, div);
        node = pos.nextSibling;
    }
    else if (level == 'language')
    {
        pos = node.firstChild;
        insertHtml('afterEnd', pos, div);
        node = pos.nextSibling;
    }
    else if (level == 'term')
    {
        pos = node.firstChild;
        insertHtml('afterEnd', pos, div);
        node = pos.nextSibling;
    }
    else
    {
        pos = node.children[1];
        insertHtml('afterEnd', pos, div);
        node = pos.nextSibling;
    }

    g_dirty = true;

    initSelection();

    fileClick(node);
    node.scrollIntoView(false);
}

function setApprovalStatus(status)
{
    var node = getConceptStatus();

    if (node)
    {
        node.children[1].innerText = status;
    }
    else
    {
        insertFieldInCurrent(g_entry, getLevel(g_entry), "Status",
            'status', status);
    }

    g_dirty = true;
}

// input is a selected field (descrip, source, note), not term etc.
function getLevel(node)
{
    var fieldType = getFieldType(node);

    if (fieldType == 'conceptGrp')
    {
        return 'concept';
    }
    else if (fieldType == 'languageGrp' || fieldType == 'fakeLanguageGrp')
    {
        return 'language';
    }
    else if (fieldType == 'termGrp' || fieldType == 'fakeTermGrp')
    {
        return 'term';
    }
    else if (fieldType == 'fieldGrp')
    {
        if (node.firstChild.getAttribute("type") == 'source')
        {
            return 'source';
        }

        return 'field';
    }
}

// input is a selected field (descrip, source, note), not term etc.
function getParentLevel(node)
{
    var fieldType = getFieldType(node);

    // catch error case
    if (fieldType == 'conceptGrp')
    {
        return 'concept';
    }

    node = node.parentElement || node.parentNode;
    fieldType = getFieldType(node);

    if (fieldType == 'conceptGrp')
    {
        return 'concept';
    }
    else if (fieldType == 'languageGrp' || fieldType == 'fakeLanguageGrp')
    {
        return 'language';
    }
    else if (fieldType == 'termGrp' || fieldType == 'fakeTermGrp')
    {
        return 'term';
    }
    else if (fieldType == 'fieldGrp')
    {
        if (node.firstChild.getAttribute("type") == 'source')
        {
            return 'source';
        }

        return 'field';
    }
}

function getParentLanguage(field)
{
    // Find the language for a field by going up the hierarchy.
    // If a concept-level field was selected, return null;

    while (field != g_entry && getFieldType(field) != 'languageGrp')
    {
        field = field.parentElement || field.parentNode;
    }

    if (field != g_entry)
    {
        return field.firstChild.children[1].innerText;
    }

    return null;
}

function findLanguageGrp(language)
{
    var langGrps = g_entry.children;

    for (var i = 0; i < langGrps.length; i++)
    {
        var langGrp = langGrps[i];
        var fieldType = getFieldType(langGrp);
        if (fieldType == 'languageGrp')
        {
            if (langGrp.firstChild.children[1].innerText == language)
            {
                return langGrp;
            }
        }
    }

    return null;
}

function getConceptStatus()
{
    var cnodes = g_entry.children;

    for (var i = 0; i < cnodes.length; i++)
    {
        var cnode = cnodes[i];

        var fieldType = getFieldType(cnode);

        if (fieldType == 'fieldGrp')
        {
            if (cnode.firstChild.getAttribute("type") == 'status')
            {
                return cnode;
            }
        }
    }

    return null;
}

function getLanguageNamesInEntry()
{
    var result = new Array();
    var conceptFields = g_entry.children;

    for (var i = 0; i < conceptFields.length; i++)
    {
        var field = conceptFields[i];
        var fieldType = getFieldType(field);
        if (fieldType == 'languageGrp')
        {
            result.push(field.firstChild.children[1].innerText);
        }
    }

    return result;
}

function getLangLocsInEntry()
{
    var result = new Array();
    var conceptFields = g_entry.children;

    for (var i = 0; i < conceptFields.length; i++)
    {
        var field = conceptFields[i];
        var fieldType = getFieldType(field);

        if (fieldType == 'languageGrp')
        {
            var language = field.firstChild.children[1].innerText;

            for (var j = 0; j < g_termbaseLanguages.length; j++)
            {
                var langloc = g_termbaseLanguages[j];

                if (langloc.getLanguage() == language)
                {
                    result.push(langloc);
                    break;
                }
            }
        }
    }

    return result;
}

function getLangLocsNotInEntry()
{
    // Get all languages and locales from TbDefinition and subtract
    // languages already in entry
    var result = new Array();

    var allLanguages = g_termbaseLanguages;
    var usedLanguages = getLangLocsInEntry();

    for (var i = 0; i < allLanguages.length; i++)
    {
        var l1 = allLanguages[i].getLanguage();

        var found = false;
        for (var j = 0; j < usedLanguages.length; j++)
        {
            var l2 = usedLanguages[j].getLanguage();

            if (l1 == l2)
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            result.push(allLanguages[i]);
        }
    }

    return result;
}

// general helpers

function cancelEvent(event)
{
    var evt = event ? event : (window.event ? window.event : null);
    evt.cancelBubble = true;
    evt.returnValue = false;
    return false;
}

function initSelection()
{
    g_selected = null;
    g_fields = new SelectableFields(g_entry, false, true);
    g_fields.onchange = setSelectedItem;

    setCanAddLanguage();
    updateMenu();
}

function getEmptyEntry(model)
{
    var result = XmlDocument.create();
    result.async = false;

    if (!model)
    {
        result.loadXML('<conceptGrp><concept>0</concept></conceptGrp>');
    }
    else
    {
        result.loadXML(model.xml);

        // Convert Input Model instructions to display values
        ImToXml(result);
    }
    return result;
}

function loadInputModel()
{
    try
    {
        while(!g_getInputModelOver) {
            window.setTimeout("", 1000);
        }

        if ($(g_inputmodel).find("noresult"))
        {
            g_inputmodel = null;
        }
        else {
            alert("This termbase uses a mandatory input model.\n" +
                "The edited entry will be checked against the input model " +
                "when saving.");
        }
    }
    catch (error)
    {
        g_inputmodel = null;
    }
}

function initEditor(p_createNew)
{
    var xmlDoc;

    loadInputModel();

    if (p_createNew)
    {
        g_conceptId = 0;
        g_termId = 0;

        xmlDoc = getEmptyEntry(g_inputmodel);
    }
    else
    {
        g_conceptId = nCid;
        g_termId = nTid;

        // Grab the entry's XMLDocument from the viewer.
        xmlDoc = GetEntryXml();
    }

    setEditorEntry2(xmlDoc);

    // idEditorEntry contains a <div class="conceptGrp">
    g_entry = idEditorEntry.firstChild;

    initSelection();
}

function debug(s)
{
    alert(s);
}
