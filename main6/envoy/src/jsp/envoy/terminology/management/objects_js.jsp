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

String lb_field_name_text = "Text Field";
String lb_field_expl_text = "User-defined text field.";
String lb_field_name_attr = "Attribute Field";
String lb_field_expl_attr = "User-defined attribute field";
String lb_field_name_status = "Status";
String lb_field_expl_status =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_concept_status"));
String lb_field_name_project = "Project";
String lb_field_expl_project = "Contains a project and version reference.";
String lb_field_name_definition = "Definition";
String lb_field_expl_definition = "Defines the meaning of a concept or a specific term.";
String lb_field_name_source = "Source";
String lb_field_expl_source = "Contains the source reference of the entry, term, or text field.";
String lb_field_name_note = "Note";
String lb_field_expl_note = "Contains additional notes.";
String lb_field_name_type = "Type";
String lb_field_expl_type = "The type of a term.";
String lb_field_name_context = "Context";
String lb_field_expl_context = "Contains a context sentence in which a term is normally used.";
String lb_field_name_example = "Example";
String lb_field_expl_example = "Contains an example sentence in which a term is used.";

String lb_field_name_term =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term"));
String lb_field_expl_term =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term"));
String lb_field_name_domain =
  EditUtil.toJavascript(bundle.getString("lb_field_name_domain"));
String lb_field_expl_domain =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_domain"));
String lb_field_name_term_usage =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_usage"));
String lb_field_expl_term_usage =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_usage"));
String lb_field_name_term_pos =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_pos"));
String lb_field_expl_term_pos =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_pos"));
String lb_field_name_term_gender =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_gender"));
String lb_field_expl_term_gender =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_gender"));
String lb_field_name_term_number =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_number"));
String lb_field_expl_term_number =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_number"));
%>
// For global objects defined in this file see below (aFieldTypes).

/**
 * Object holding arguments for the Editor's AddField/EditField dialog.
 */
function FieldDialogArgs(fields, field)
{
    this.fields = fields;
    this.field  = field;
}

/**
 * Object holding information about languages defined in a termbase.
 */
function Language(name, locale, hasterms, exists)
{
    this.name = name;
    this.locale = locale;
    this.hasterms = hasterms; // not visible on UI (but boolean)
    this.exists = exists;
}

/**
 * Object holding information about user-defined fields in a termbase.
 */
function Field(name, type, format, system, values)
{
    this.name = name;
    this.type = type;
    this.format = format;
    this.system = system; // boolean
    this.values = values;
}

Field.prototype.setDisplayName = function (param)
{
    this.name = new String(param);
}

Field.prototype.getDisplayName = function ()
{
    return this.name;
}

Field.prototype.setType = function (param)
{
    this.type = new String(param);
}

Field.prototype.getType = function ()
{
    return this.type;
}

Field.prototype.setFormat = function (param)
{
    this.format = new String(param);
}

Field.prototype.getFormat = function ()
{
    return this.format;
}

Field.prototype.setValues = function (param)
{
    this.values = new String(param);
}

Field.prototype.getValues = function ()
{
    return this.values;
}

Field.prototype.isAttribute = function ()
{
    return this.format == "attr";
}

Field.prototype.isText = function ()
{
    return this.format == "text";
}

Field.prototype.isUserDefinedAttribute = function ()
{
    return this.type.startsWith("attr");
}

Field.prototype.isUserDefinedText = function ()
{
    return this.type.startsWith("text");
}

Field.prototype.getDescription = function ()
{
    return getFieldDescriptionByType(this.type);
}

Field.prototype.toString = function ()
{
    return "Field name=" + this.name + " type=" + this.type +
        " format=" + this.format + " system=" + this.system +
        " values=" + this.values;
}

/**
 * Object holding information about well-known, or system-defined,
 * field types. These field types represent categories from ISO 12620
 * and their typical values.
 */
function FieldType(id, type, name, format, values, description,
    system, systemAllowsValues)
{
    this.id = id;
    this.type = type;
    this.name = name;
    this.format = format;
    this.values = values;
    this.description = description;
}

// Used in field.jsp to filter fields
FieldType.prototype.skipField = function ()
{
    return this.format == null;
}

FieldType.prototype.getType = function ()
{
    return this.type;
}

FieldType.prototype.getDisplayName = function ()
{
    return this.name;
}

FieldType.prototype.getDescription = function ()
{
    return this.description;
}

FieldType.prototype.getValues = function ()
{
    return this.values;
}

FieldType.prototype.isAttribute = function ()
{
    return this.format == "attr";
}

FieldType.prototype.isText = function ()
{
    return this.format == "text";
}

FieldType.prototype.isUserDefinedAttribute = function ()
{
    return this.type == "attr";
}

FieldType.prototype.isUserDefinedText = function ()
{
    return this.type == "text";
}

FieldType.prototype.isTerm = function ()
{
    return this.format == "term";
}

FieldType.prototype.isSystemField = function ()
{
    return this.system;
}

FieldType.prototype.systemFieldAllowsValues = function ()
{
    return this.systemAllowsValues;
}

FieldType.prototype.toString = function ()
{
    return "FieldType id=" + this.id + " type=" + this.type +
        " name=" + this.name + " format=" + this.format +
        " values=[" + this.values + "] description=" + this.description +
        " system=" + this.system +
        " systemAllowsValues=" + this.systemAllowsValues;
}

function getFieldByType(type, definedFields)
{
    if (typeof(definedFields) != "undefined")
    {
        for (var i = 0; i < definedFields.length; i++)
        {
            var oField = definedFields[i];
            if (oField.getType() == type)
            {
                return oField;
            }
        }
    }

    return null;
}

function getFieldNameByType(type, definedFields)
{
    // definedFields holds user-defined fields

    if (typeof(definedFields) != "undefined")
    {
        for (var i = 0; i < definedFields.length; i++)
        {
            var oField = definedFields[i];

            if (oField.getType() == type)
            {
                return oField.getDisplayName();
            }
        }
    }

    if (type.startsWith("attr"))
    {
        type = "attr";
    }
    else if (type.startsWith("text"))
    {
        type = "text";
    }

    for (var i = 0; i < aFieldTypes.length; i++)
    {
        var oFieldType = aFieldTypes[i];

        if (oFieldType.type == type)
        {
            return oFieldType.name;
        }
    }

    return "<unknown type " + type + ">";
}

function getFieldFormatByType(type)
{
    if (type.startsWith("attr"))
    {
        type = "attr";
    }
    else if (type.startsWith("text"))
    {
        type = "text";
    }

    for (i = 0; i < aFieldTypes.length; ++i)
    {
        var oFieldType = aFieldTypes[i];

        if (oFieldType.type == type)
        {
            return oFieldType.format;
        }
    }
}

function getFieldDescriptionByType(type)
{
    if (type.startsWith("attr"))
    {
        type = "attr";
    }
    else if (type.startsWith("text"))
    {
        type = "text";
    }

    for (i = 0; i < aFieldTypes.length; ++i)
    {
        var oFieldType = aFieldTypes[i];

        if (oFieldType.type.toLowerCase() == type.toLowerCase())
        {
            return oFieldType.description;
        }
    }

    return "<unknown description>";
}

// Internal method to construct the g_XXXFields arrays below.
function getFieldTypeByType(type)
{
    for (i = 0; i < aFieldTypes.length; ++i)
    {
        var oFieldType = aFieldTypes[i];

        if (oFieldType.type == type)
        {
            return oFieldType;
        }
    }
}

/**
 * A fallback method for fields that are unknown to this software, and
 * undefined in the termbase, because they were imported from foreign
 * data. We treat all unknown fields as text.
 */
function getUserDefinedField(level, type)
{
    return new FieldType(-1, type, type, "text", "",
        "User-defined field.", false, false);
}

/**
 * Provides a mapping of field types to display labels based on the
 * termbase definition and the well-known field types. This object is
 * intended to act like a closure of all other objects in this (and
 * other) files to ease display routines (see entry.js).
 *
 * Note this object could also take a locale into account.
 *
 * @param definedFields - Array of Field objects
 * @param defaultFieldTypes - Array of FieldType objects
 * @param ui - string indicating for which UI this object is being used.
 * Allowed values: "inputmodels", "editor", "viewer".
 */
function MappingContext(definedFields, defaultFieldTypes, ui)
{
    // UI can be one of "inputmodels", "editor", "viewer".
    this.ui = ui;

    this.fields = definedFields;
    this.fieldTypes = defaultFieldTypes;

    if (defaultFieldTypes == null)
    {
        this.fieldTypes = aFieldTypes;
    }
}

MappingContext.prototype.mapEntry = function ()
{
    return "Entry";
}

MappingContext.prototype.mapNewEntry = function ()
{
    return "New Entry";
}

MappingContext.prototype.mapLanguage = function (language)
{
    return language;
}

MappingContext.prototype.mapTransac = function (type)
{
    if (type == "origination")
    {
        return "Creation Date";
    }
    else if (type == "modification")
    {
        return "Modification Date";
    }
    else
    {
        return type;
    }
}

MappingContext.prototype.mapNote = function (type)
{
    return "Note";
}

MappingContext.prototype.mapSource = function (type)
{
    return "Source";
}

MappingContext.prototype.mapDescrip = function (type)
{
    if (this.fields)
    {
        for (var i = 0; i < this.fields.length; i++)
        {
            var oField = this.fields[i];

            if (oField.getType() == type)
            {
                return oField.getDisplayName();
            }
        }
    }

    if (this.fieldTypes)
    {
        if (type.startsWith("attr"))
        {
            type = "attr";
        }
        else if (type.startsWith("text"))
        {
            type = "text";
        }

        for (var i = 0; i < this.fieldTypes.length; i++)
        {
            var oFieldType = this.fieldTypes[i];

            if (oFieldType.type == type)
            {
                return oFieldType.getDisplayName();
            }
        }
    }

    return type;
}

// This provides a term label for UIs that need to distinguish main
// terms from synonyms (like input models). The viewer and editor
// normally don't even show the label.
MappingContext.prototype.mapTerm = function (isImputModel, isFirst)
{
    if (isImputModel)
    {
        if (isFirst)
        {
            return "Main Term";
        }
        else
        {
            return "Synonyms";
        }
    }

    return "Term";
}

/**
 * Entry filters consist of filter conditions.
 *
 * @param level     - the level of the filter: either '_concept_'
 *                    or a language name.
 * @param field     - the field TYPE to filter on, or '_all_' (NIY)
 * @param operator  - the operator to use for comparisons:
 *                     'contains', 'containsnot', 'equals', 'equalsnot',
 *                     'lessthan', 'greaterthan', 'exists', 'existsnot'
 * @param value     - the value for the comparison
 * @param matchcase - case-sensitive flag
 */
function FilterCondition(level, field, operator, value, matchcase)
{
    this.level = level;
    this.field = field;
    this.operator = operator;
    this.value = value;
    this.matchcase = matchcase;
}

FilterCondition.prototype.getLevel = function ()
{
    return this.level;
}

FilterCondition.prototype.getField = function ()
{
    return this.field;
}

FilterCondition.prototype.getOperator = function ()
{
    return this.operator;
}

FilterCondition.prototype.getValue = function ()
{
    return this.value;
}

FilterCondition.prototype.getMatchCase = function ()
{
    return this.matchcase;
}

FilterCondition.prototype.toString = function ()
{
    return "[FilterCondition " + "level=" + this.level +
        " field=" + this.field + " operator=" + this.operator +
        " value=" + this.value + " matchcase=" + this.matchcase + "]";
}

/**
 * The actual definition of well-known field types from ISO 12620.
 */
var aFieldTypes = new Array();
var g_entryFields = new Array();
var g_conceptFields = new Array();
var g_languageFields = new Array();
var g_termFields = new Array();
var g_fieldFields = new Array();
var g_sourceFields = new Array();

var i = 0;

// id, type, name, format, values, description, system, systemAllowsValues

aFieldTypes.push(new FieldType(
    i++, "text", "<%=lb_field_name_text%>", "text", "",
    "<%=lb_field_expl_text%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "attr", "<%=lb_field_name_attr%>", "attr", "",
    "<%=lb_field_expl_attr%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "status", "<%=lb_field_name_status%>", "attr",
    "proposed, reviewed, approved",
    "<%=lb_field_expl_status%>", true, false));

aFieldTypes.push(new FieldType(
    i++, "domain", "<%=lb_field_name_domain%>", "attr", "",
    "<%=lb_field_expl_domain%>", true, true));

aFieldTypes.push(new FieldType(
    i++, "project", "<%=lb_field_name_project%>", "attr", "",
    "<%=lb_field_expl_project%>", true, true));

aFieldTypes.push(new FieldType(
    i++, "usage", "<%=lb_field_name_term_usage%>", "attr",
    "preferred, admitted, deprecated",
    "<%=lb_field_expl_term_usage%>", true, false));

aFieldTypes.push(new FieldType(
    i++, "type", "<%=lb_field_name_type%>", "attr",
    "international scientific term, common name, internationalism, " +
    "full form, short form, abbreviation, initialism, acronym, " +
    "clipped term, variant, transliteration, transcription, symbol, " +
    "formula, phrase, collocation, boiler plate",
    "<%=lb_field_expl_type%>", true, true));

aFieldTypes.push(new FieldType(
    i++, "pos", "<%=lb_field_name_term_pos%>", "attr",
    "noun, verb, adjective, adverb, other",
    "<%=lb_field_expl_term_pos%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "gender", "<%=lb_field_name_term_gender%>", "attr",
    "masculine, feminine, neuter, other",
    "<%=lb_field_expl_term_gender%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "number", "<%=lb_field_name_term_number%>", "attr",
    "singular, plural, dual, mass noun, other",
    "<%=lb_field_expl_term_number%>", false, false));

// also a kind of system field, may be removed
aFieldTypes.push(new FieldType(
    i++, "source", "<%=lb_field_name_source%>", "text", "",
    "<%=lb_field_expl_source%>", false, false));

// also a kind of system field, may be removed
aFieldTypes.push(new FieldType(
    i++, "note", "<%=lb_field_name_note%>", "text", "",
    "<%=lb_field_expl_note%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "definition", "<%=lb_field_name_definition%>", "text", "",
    "<%=lb_field_expl_definition%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "context", "<%=lb_field_name_context%>", "text", "",
    "<%=lb_field_expl_context%>", false, false));

aFieldTypes.push(new FieldType(
    i++, "example", "<%=lb_field_name_example%>", "text", "",
    "<%=lb_field_expl_example%>", false, false));

//
// Additional arrays for editor
//

g_entryFields.push(getFieldTypeByType("domain"));
g_entryFields.push(getFieldTypeByType("project"));
g_entryFields.push(getFieldTypeByType("definition"));
g_entryFields.push(getFieldTypeByType("type"));
g_entryFields.push(getFieldTypeByType("usage"));
g_entryFields.push(getFieldTypeByType("pos"));
g_entryFields.push(getFieldTypeByType("gender"));
g_entryFields.push(getFieldTypeByType("number"));
g_entryFields.push(getFieldTypeByType("context"));
g_entryFields.push(getFieldTypeByType("example"));
g_entryFields.push(getFieldTypeByType("source"));
g_entryFields.push(getFieldTypeByType("note"));

// Status is a PM field that not every user can add/set.
// g_conceptFields.push(getFieldTypeByType("status"));
g_conceptFields.push(getFieldTypeByType("domain"));
g_conceptFields.push(getFieldTypeByType("project"));
g_conceptFields.push(getFieldTypeByType("definition"));
g_conceptFields.push(getFieldTypeByType("source"));
g_conceptFields.push(getFieldTypeByType("note"));

g_languageFields.push(getFieldTypeByType("source"));
g_languageFields.push(getFieldTypeByType("note"));

g_termFields.push(getFieldTypeByType("type"));
g_termFields.push(getFieldTypeByType("usage"));
g_termFields.push(getFieldTypeByType("status"));
g_termFields.push(getFieldTypeByType("pos"));
g_termFields.push(getFieldTypeByType("gender"));
g_termFields.push(getFieldTypeByType("number"));
g_termFields.push(getFieldTypeByType("definition"));
g_termFields.push(getFieldTypeByType("context"));
g_termFields.push(getFieldTypeByType("example"));
g_termFields.push(getFieldTypeByType("source"));
g_termFields.push(getFieldTypeByType("note"));

g_fieldFields.push(getFieldTypeByType("source"));
g_fieldFields.push(getFieldTypeByType("note"));

g_sourceFields.push(getFieldTypeByType("note"));

