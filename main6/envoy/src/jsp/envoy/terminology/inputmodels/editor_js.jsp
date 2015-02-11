// -*- mode: javascript -*-
<%@ page
    contentType="text/javascript; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
%>
strBaseUrl = "/globalsight/envoy/terminology/inputmodels";
var ie55 = /MSIE ((5\.[56789])|([6789]))/.test( navigator.userAgent ) &&
            navigator.platform == "Win32";

// Are we already editing an entry?
var g_editing = false;
var g_dirty = false;

var g_NEWENTRY = true;

var g_entry = null;              // pointer to editable entry
var g_fields = null;             // selectable fields object
var g_selected = null;           // currently selected node

var g_definition = null;         // XML Document
var g_editorEntry = null;        // XML Document

// Array of language names and locales from the termbase definition
var g_termbaseLanguages = new Array();
// Array of fields defined in the termbase
var g_termbaseFields = new Array();

// flag if all languages are present in entry
var g_canAddLanguage = true;
//var g_canRemoveLanguage = false;

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

/* defined in ../management/objects_js.jsp
function Field(name, type, format, system, values)
{
    this.name = name;
    this.type = type;
    this.format = format;
    this.system = system; // boolean
    this.values = values;
}
*/

function FieldParameters()
{
    this.level = null;
    this.type = null;
    this.value = null;
    this.definedFields = null;
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

FieldParameters.prototype.toString = function ()
{
    return "[" + this.level + ": " + this.type + "=" + this.value + "]";
}


function EditTermParameters(language, term, mainTerm)
{
    this.language = language;
    this.term = term;
    this.mainTerm = mainTerm;
}

EditTermParameters.prototype.getLanguage = function ()
{
    return this.language;
}

EditTermParameters.prototype.setTerm = function (param)
{
    this.term = new String(param);
}

EditTermParameters.prototype.isMainTerm = function ()
{
    return this.mainTerm;
}

EditTermParameters.prototype.getTerm = function ()
{
    return this.term;
}

EditTermParameters.prototype.toString = function ()
{
    return "[" + this.language + ": " + this.term + "]";
}


function EditFieldParameters(level, type, value)
{
    this.level = level;
    this.type = type;
    this.value = value;
    this.definedFields = null;
}

EditFieldParameters.prototype.getLevel = function ()
{
    return this.level;
}

EditFieldParameters.prototype.getType = function ()
{
    return this.type;
}

EditFieldParameters.prototype.setValue = function (param)
{
    this.value = new String(param);
}

EditFieldParameters.prototype.getValue = function ()
{
    return this.value;
}

EditFieldParameters.prototype.setDefinedFields = function (param)
{
    this.definedFields = param;
}

EditFieldParameters.prototype.getDefinedFields = function ()
{
    return this.definedFields;
}

EditFieldParameters.prototype.toString = function ()
{
    return "[" + this.type + ": " + this.value + "]";
}


function normalizeWS(s)
{
    return s.replace(/\s+/g, ' ');
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
    if (g_selected && g_selected.length > 0)
    {
        var sel = g_selected[0];
        var fieldType = getFieldType(sel);

        if (fieldType == 'conceptGrp')
        {
            idRemoveLanguage.disabled = true;
            idRemoveLanguage.className = "menuItemD";
            idAddTerm.disabled = true;
            idAddTerm.className = "menuItemD";
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
            var numterms = getNumberOfTerms(sel);

            idRemoveLanguage.disabled = false;
            idRemoveLanguage.className = "menuItem";
            idAddTerm.disabled = (numterms == 2);
            idAddTerm.className = "menuItem" + (numterms == 2 ? "D" : "");
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
            var numterms = getNumberOfTerms(sel);
            var firstTerm = isFirstTerm(sel);
            var secondTerm = isSecondTerm(sel);

            idRemoveLanguage.disabled = true;
            idRemoveLanguage.className = "menuItemD";
            idAddTerm.disabled = (numterms == 2);
            idAddTerm.className = "menuItem" + (numterms == 2 ? "D" : "");
            idRemoveTerm.disabled = firstTerm;
            idRemoveTerm.className = "menuItem" + (firstTerm ? "D" : "");
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
			var selType = sel.firstChild.type;
			if(selType != null && selType != 'undefined')
			{
				if (selType.toLowerCase() == 'note')
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
            
        }
    }
    else
    {
        // standard menu
        idRemoveLanguage.disabled = true;
        idRemoveLanguage.className = "menuItemD";
        idAddTerm.disabled = true;
        idAddTerm.className = "menuItemD";
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

    /*
    if (g_canRemoveLanguage)
    {
        idRemoveLanguage.disabled = false;
        idRemoveLanguage.className = "menuItem";
    }
    else
    {
        idRemoveLanguage.disabled = true;
        idRemoveLanguage.className = "menuItemD";
    }
    */
}

function doEdit(field)
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

    var mainTerm = isFirstTerm(field);
    var type;
    var value = field.children[1].innerText;
    var newValue = null;

    if (fieldType == 'fakeTermGrp')
    {
        var language =
            field.parentElement.parentElement.firstChild.children[1].innerText;

        var params = new EditTermParameters(language, value, mainTerm);

        var res = window.showModalDialog(
            strBaseUrl + '/EditTerm.jsp', params,
            "dialogHeight:200px; dialogWidth:350px; center:yes; " +
            "resizable:no; status:no; help:no;");

        if (res != null)
        {
            newValue = normalizeWS(res.getTerm());
        }
    }
    else if (fieldType == 'fieldGrp')
    {
        type = field.firstChild.getAttribute("type").toLowerCase();

        var level = getParentLevel(field);

        /*
        // Concept status is handled special (subject to permissions).
        if (level == 'concept' && type == 'Status')
        {
            return cancelEvent();
        }
        */

        var params = new EditFieldParameters(level, type, value);
        params.setDefinedFields(g_termbaseFields);

        var res = window.showModalDialog(
            strBaseUrl + '/EditField.jsp', params,
            "dialogHeight:300px; dialogWidth:370px; center:yes; " +
            "resizable:no; status:no; help:no; ");

        if (res != null)
        {
            newValue = normalizeWS(res.getValue());
        }
    }
    else
    {
        // alert("Fieldtype=" + fieldType + " - level=" + level);
        type = field.firstChild.innerText;

        var newValue = prompt("<%=bundle.getString("jsmsg_tb_input_model_enter_new_value") %>".replace("%1", type) + ":", value);
    }

    if (newValue != null)
    {
        field.children[1].innerText = newValue;

        g_dirty = true;
    }

    cancelEvent();
}

function doDelete(field, event)
{
    if (getFieldType(field).indexOf('fake') == 0)
    {
        field = field.parentElement;
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
        RemoveField(event);
    }
}

function isKeydown(event, entry)
{
    var event = event ? event : (window.event ? window.event : null);
    var key = event.keyCode; 

    // window.status = "Keycode: " + key;

    // Viewer mode key bindings
    if (event.ctrlKey && !event.altKey)
    {
        if (key == 69) // Letter E
        {
            // StartEditing();
            return cancelEvent(); // cancel IE's system dialog
        }
        else if (key == 72) // Letter H
        {
            return cancelEvent(); // cancel IE's system dialog
        }
        else if (key == 73) // Letter I
        {
            return cancelEvent(); // cancel IE's system dialog
        }
        else if (key == 78) // Letter N
        {
            return cancelEvent(); // cancel IE's system dialog
        }
        else if (key == 79) // Letter O
        {
            return cancelEvent(); // cancel IE's system dialog
        }
        else if (key == 82) // Letter R
        {
            return cancelEvent(); // cancel IE's reload
        }
    }

    // Editor mode bindings not active when editor not active
    if (!g_editing)
    {
        return true;
    }

    if (key == 35) // End
    {
        doSelectLast();
        return cancelEvent();
    }
    else if (key == 36) // Home
    {
        doSelectFirst();
        return cancelEvent();
    }
    else if (key == 38) // Arrow Up
    {
        doSelectPrevious();
        return cancelEvent();
    }
    else if (key == 40) // Arrow Down
    {
        doSelectNext();
        return cancelEvent();
    }
    else if (key == 13) // Return
    {
        if (g_selected)
        {
            doEdit(g_selected[0]);
        }
        return cancelEvent();
    }
    else if (key == 46) // Delete
    {
        if (g_selected)
        {
            doDelete(g_selected[0],event);
        }
        return cancelEvent();
    }
    if (event.ctrlKey && !event.altKey)
    {
        if (key == 65) // Letter A
        {
            if (idAddFieldAfter.disabled == false)
            {
                AddFieldAfterCurrent();
            }
            else
            {
                AddFieldToCurrent();
            }
            return cancelEvent();
        }
        else if (key == 68) // Letter D
        {
            AddFieldToCurrent();
            return cancelEvent();
        }
        else if (key == 70) // Letter F
        {
            AddFieldToCurrent();
            return cancelEvent();
        }
        else if (key == 76) // Letter L
        {
            AddLanguage();
            return cancelEvent();
        }
        else if (key == 81) // Letter Q
        {
            CancelEditing();
            return cancelEvent();
        }
        else if (key == 83) // Letter S
        {
            SaveEntry();
            return cancelEvent();
        }
        else if (key == 84) // Letter T
        {
            AddTerm();
            return cancelEvent();
        }
    }
}

function doSelectFirst()
{
    var item = g_entry; // g_fields._getFirstDescendant(g_entry);

    if (item)
    {
        //item.click();
        fileClick(item);
        item.scrollIntoView(true);
    }
}

function doSelectLast()
{
    var item = g_fields._getLastDescendant(g_entry);

    if (item)
    {
        //item.click();
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
    // g_termbaseLanguages = new Array();

    var nodes = p_definition.selectNodes("/definition/languages/language");
    for (i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        var langloc = new LangLoc(node.selectSingleNode("name").text,
            node.selectSingleNode("locale").text);

        g_termbaseLanguages.push(langloc);
    }

    g_termbaseLanguages.sort(compareLangLocs);
}

function setTermbaseFields(p_definition)
{
    // compute cached array of known fields
    // g_termbaseFields = new Array();

    var nodes = p_definition.selectNodes("/definition/fields/field");
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        var name = node.selectSingleNode("name").text;
        var type = node.selectSingleNode("type").text;
        var system =
          (node.selectSingleNode("system").text == "true" ? true : false);
        var values = node.selectSingleNode("values").text;
        var format = getFieldFormatByType(type);

        var field = new Field(name, type, format, system, values);

        g_termbaseFields.push(field);
    }

    // alert(g_termbaseFields);
}

function SetEditiorDefinition()
{
    g_definition = loadXML('/globalsight/envoy/terminology/viewer/definition.jsp');
    // alert(g_definition.xml);

    setTermbaseLanguages(g_definition);
    setTermbaseFields(g_definition);
}

function setEditorEntry2(xmlDocument)
{
    g_editorEntry = xmlDocument;

    try
    {
        var strHTML = XmlToHtml(g_editorEntry,
            new MappingContext(g_termbaseFields, aFieldTypes), "inputmodels");

        idEditorEntry.innerHTML = strHTML;
    }
    catch (ex)
    {
        TermbaseError("Cannot format entry: " + ex.description + ".\n",
            false);
        return;
    }
}

/*
function SetEditorEntry(obj)
{
    if (obj.readyState != 'complete')
    {
        return;
    }

    idBody.style.cursor = 'auto';

    g_editorEntry = obj.XMLDocument;

    if (obj.parseError.errorCode != 0)
    {
        TermbaseError("Error in entry", false);
        idEditorEntry.innerText = '';
        return;
    }

    if (g_editorEntry.documentElement.nodeName == 'exception')
    {
        TermbaseError(g_editorEntry.documentElement.text, false);
        idEditorEntry.innerText = '';
        return;
    }

    try
    {
        var strHTML = // XmlToHtml(g_editorEntry);
        g_editorEntry.transformNode(g_editorStylesheet);

        // alert(strHTML);

        idEditorEntry.innerHTML = strHTML;
    }
    catch (ex)
    {
        TermbaseError("Cannot format entry: " + ex + ".\n" +
            ex.description, false);
        return;
    }
}
*/

// After entry has been set, see if it contains all languages or not
function setCanAddLanguage()
{
    var langs = getLangLocsInEntry();

    /*
    if (langs.length == 0)
    {
        g_canRemoveLanguage = false;
    }
    else
    {
        g_canRemoveLanguage = true;
    }
    */

    if (langs.length == g_termbaseLanguages.length)
    {
        g_canAddLanguage = false;
    }
    else
    {
        g_canAddLanguage = true;
    }
}

//
// Operational methods (save, cancel)
//
function SaveEntry()
{
    var name = idEntryName.value;
    if (!name)
    {
        alert("<%=bundle.getString("jsmsg_tb_input_model_enter_a_name") %>");
        idEntryName.focus();
        return;
    }

    var langs = getLanguageNamesInEntry();
    if (langs.length == 0)
    {
        alert("<%=bundle.getString("jsmsg_tb_input_model_save_no_term") %>");
        return;
    }

    var xml = HtmlToXml(g_entry);

    SetResultValue(xml);
}

function CancelEditing()
{
    StopEditing();
}

function StartEditing(p_value)
{
    if (idEditor.Admin == "true")
    {
        // We are the admin user.
    }

    if (!g_editing)
    {
        g_editing = true;

        // idCreateButton.setEnabled(false);
        // idEditButton.setEnabled(false);

        // Undo the conversion in SaveEntry()
        if (p_value)
        {
            p_value = p_value.replace(/&lt;/g, "<").replace(/&gt;/g, ">").
                replace(/&quot;/g, "\"").replace(/&amp;/g, "&");
        }

        initEditor(p_value);
    }
    else if (g_editing)
    {
        // probably user wants to see editor?
    }
}

function StopEditing()
{
    if (g_dirty && !confirm("<%=bundle.getString("jsmsg_confirm_discard_unsaved_changes") %>"))
    {
        return;
    }

    g_editing = false;

    if (true)
    {
        // idEditButton.setEnabled(true);

        // idEditButton.innerText = "Edit Entry";
        // idEditButton.className = 'notEditing';
        // idEditButton.title = "Click to start editing this entry.";
    }
    else
    {
        // idEditButton.setEnabled(false);

        // idEditButton.className = '';
        // idEditButton.innerText = '';
        // idEditButton.title = '';
    }

    // idCreateButton.setEnabled(true);

    SetResultValue(null);
}

// public methods for structural entry editing

function AddLanguage()
{
    var availLangLocs = getLangLocsNotInEntry();

    if (availLangLocs.length == 0)
    {
        return;
    }

    // alert("avail: " + availLangLocs);

    // Show dialog to select one of the languages and enter a term
    var res = window.showModalDialog(
        strBaseUrl + '/AddLanguage.jsp', availLangLocs,
        "dialogHeight:200px; dialogWidth:350px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res != null && res.getTerm() != '')
    {
        insertLanguage(res.getLanguage(), res.getLocale(), res.getTerm());
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
            sel = sel.parentElement;
            fieldType = getFieldType(sel);
        }

        if (getFieldType(sel) == 'languageGrp' &&
            confirm("<%=bundle.getString("jsmsg_tb_input_model_confirm_remove_lang") %>"))
        {
            var nextSel = sel.nextSibling;
            if (!nextSel) nextSel = sel.previousSibling;
            if (!nextSel) nextSel = sel.parentElement;

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

// TODO: check main term + synonym, show only current language
function AddTerm()
{
    var language = null;
    var term = null;

    var langlocs = getLangLocsInEntry();

    if (langlocs.length == 0)
    {
        // Entry has no language yet: delegate to AddLanguage().
        return /*AddLanguage()*/;
    }

    // Find the default language for the term.
    if (g_selected)
    {
        // derive language from selection
        var sel = g_selected[0];

        while (sel != g_entry && getFieldType(sel) != 'languageGrp')
        {
            sel = sel.parentElement;
        }

        if (sel != g_entry)
        {
            if (getNumberOfTerms(sel) == 2)
            {
                return;
            }

            language = sel.children[0].children[1].innerText;
        }
    }

    // put only current language in langlocs
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

        langlocs = new Array();
        langlocs.push(langloc);
    }
    else
    {
        return;
    }

    var res = window.showModalDialog(
        strBaseUrl + '/AddTerm.jsp', langlocs,
        "dialogHeight:200px; dialogWidth:350px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res == null)
    {
        return;
    }

    language = res.getLanguage();
    term = normalizeWS(res.getTerm());

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
            sel = sel.parentElement;
            fieldType = getFieldType(sel);
        }

        // Menu is disabled, silently return for first term.
        if (isFirstTerm(sel))
        {
            //sel.parentElement.click();
            fileClick(sel.parentElement);
            RemoveLanguage();
            return;
        }

        if (fieldType == 'termGrp' &&
            confirm("<%=bundle.getString("jsmsg_tb_input_model_confirm_remove_term") %>"))
        {
            var nextSel = sel.nextSibling;
            if (!nextSel) nextSel = sel.previousSibling;
            if (!nextSel) nextSel = sel.parentElement;

            if (nextSel)
            {
                //nextSel.click();
                fileClick(nextSel);
                nextSel.scrollIntoView(false);
            }

            // need to unlink after setting the next selection (11475)
            sel.removeNode(true);
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
        type = sel.firstChild.type.toLowerCase();
    }
    else if (level == 'term' && getFieldType(sel) == 'fakeTermGrp')
    {
        sel = sel.parentElement;
    }

    var params = new FieldParameters();
    params.setLevel(level);
    params.setType(type);
    params.setDefinedFields(g_termbaseFields);

    var res = window.showModalDialog(
        strBaseUrl + '/AddField.jsp', params,
        "dialogHeight:400px; dialogWidth:370px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res == null)
    {
        return;
    }

    // TODO: check for presence of field.

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

    var params = new FieldParameters();
    params.setLevel(level);
    params.setDefinedFields(g_termbaseFields);

    var res = window.showModalDialog(
        strBaseUrl + '/AddField.jsp', params,
        "dialogHeight:400px; dialogWidth:370px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res == null)
    {
        return;
    }

    // TODO: check for presence of field.

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
            confirm("<%=bundle.getString("jsmsg_tb_input_model_confirm_remove_field") %>"))
        {
            var nextSel = sel.nextSibling;
            if (!nextSel || !g_fields.isItem(nextSel)) nextSel = sel.previousSibling;
            if (!nextSel || !g_fields.isItem(nextSel)) nextSel = sel.parentElement;

            sel.removeNode(true);
            sel = null;

            if (nextSel)
            {
                //nextSel.click();
                fileClick(nextSel);
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
    var div = getLanguageGrpDiv(language, locale, term, 'inputmodel');

    //g_entry.insertAdjacentHTML('beforeEnd', div);
    insertHtml('beforeEnd',g_entry,div);

    initSelection();

    var node = g_entry.lastChild;
    node.scrollIntoView(false);
    //node.click();
    fileClick(node);

    g_dirty = true;
}

function insertTerm(language, term)
{
    var langGrp = findLanguageGrp(language);
    var div = getTermGrpDivInputModel(term, false);

    insertHtml('beforeEnd',langGrp,div);

    initSelection();

    var node = langGrp.lastChild;
    node.scrollIntoView(false);
    
    //node.click();
    fileClick(node);

    g_dirty = true;
}

function insertFieldAfterCurrent(node, name, type, value)
{
    var div = getFieldGrpDiv(name, type, value);
    //node.insertAdjacentHTML('afterEnd', div);
    insertHtml('afterEnd', node, div);

    initSelection();

    node = node.nextSibling;
    node.scrollIntoView(false);
    //node.click();
    fileClick(node);

    g_dirty = true;
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
        //pos.insertAdjacentHTML('afterEnd', div);
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
        pos = node.children(1);
        insertHtml('afterEnd', pos, div);
        node = pos.nextSibling;
    }

    initSelection();

    node.scrollIntoView(false);
    //node.click();
    fileClick(node);

    g_dirty = true;
}

// Serves as example on how to populate an input model with fields
function setApprovalStatus(status)
{
    var node = getConceptStatus();

    if (node)
    {
        node.children(1).innerText = status;
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
        if (node.firstChild.type.toLowerCase() == 'source')
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

    node = node.parentElement;
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
        if (node.firstChild.type.toLowerCase() == 'source')
        {
            return 'source';
        }

        return 'field';
    }
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
            if (langGrp.children[0].children[1].innerText == language)
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
            if (cnode.firstChild.type.toLowerCase() == 'status')
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

// Selection p_sel must be a langGrp, termGrp or fakeTermGrp div.
function getNumberOfTerms(p_sel)
{
    var result = 0;
    var fieldType = getFieldType(p_sel);
    var langGrp = p_sel;

    if (fieldType == 'fakeTermGrp')
    {
        langGrp = p_sel.parentElement.parentElement;
    }
    else if (fieldType == 'fakeLanguageGrp' || fieldType == 'termGrp')
    {
        langGrp = p_sel.parentElement;
    }

    // child 0 is the fakeLanguageGrp
    for (var i = 1; i < langGrp.children.length; i++)
    {
        var node = langGrp.children[i];

        if (getFieldType(node) == 'termGrp')
        {
            result++;
        }
    }

    return result;
}

function isFirstTerm(p_sel)
{

    if (getFieldType(p_sel) == 'fakeTermGrp')
    {
        p_sel = p_sel.parentElement;
    }

    return getFieldType(p_sel.previousSibling) != 'termGrp';
}

function isSecondTerm(p_sel)
{
    if (getFieldType(p_sel) == 'fakeTermGrp')
    {
        p_sel = p_sel.parentElement;
    }

    return getFieldType(p_sel.previousSibling) == 'termGrp';
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
    g_fields = new SelectableFields(g_entry, false, true);
    g_fields.onchange = setSelectedItem;

    setCanAddLanguage();
    updateMenu();
}

function initEditor(p_value)
{
    var xmlDoc;

    if (window.ActiveXObject){
        xmlDoc = new ActiveXObject('Msxml2.DOMDocument');
    }
    else {
        xmlDoc = document.implementation.createDocument("", "", null);
    }

    if (!p_value)
    {
        g_NEWENTRY = true;

        //g_canRemoveLanguage = false;

        xmlDoc.async = false;
        xmlDoc.loadXML('<conceptGrp><concept>1</concept></conceptGrp>');
    }
    else
    {
        g_NEWENTRY = false;

        //g_canRemoveLanguage = true;

        // Grab the entry's XMLDocument from the viewer
        xmlDoc.async = false;
        xmlDoc.loadXML(p_value);
    }

    setEditorEntry2(xmlDoc);

    // idEditorEntry contains a <div class="conceptGrp">
    g_entry = idEditorEntry.firstChild;

    initSelection();
}