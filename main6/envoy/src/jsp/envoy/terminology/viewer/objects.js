/*
 * Copyright (c) 2003-2004 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

function EditorFieldType(level, type, name, format, values, description)
{
    this.level = level;                // class/language/term/field level
    this.type = type;                  // internal type stored in db
    this.name = name;                  // display name
    this.format = format;              // attribute or text (or term)
    this.values = values;              // predefined attribute values
    this.description = description;    // description for user
}

EditorFieldType.prototype.toString = function ()
{
    return "[EditorFieldType " + this.level + ", " + this.type + ", " +
        this.name + ", " + this.format + ", [" + this.values + "], " +
        this.description + "]";
};

EditorFieldType.prototype.isAttribute = function ()
{
    return this.format == "attr";
};

EditorFieldType.prototype.getDisplayName = function ()
{
    return this.name;
};

EditorFieldType.prototype.getDescription = function ()
{
    return this.description;
};

var g_conceptFields = new Array();
var g_languageFields = new Array();
var g_termFields = new Array();
var g_fieldFields = new Array();
var g_sourceFields = new Array();

// Keep display names (3rd field) in sync with EditorStylesheet.xsl.
// Move them to a property file once this is all JSP.

// ------ concepts

g_conceptFields.push(new EditorFieldType(
    "concept", "status", "Status", "attr",
    "proposed, reviewed, approved",
    "Indicates whether the entry is new, " +
    "has been reviewed, or approved for use in translations."));

g_conceptFields.push(new EditorFieldType(
    "concept", "domain", "Domain", "text", "",
    "The domain, or subject area, classifies concepts " +
    "into a domain classification tree."));

g_conceptFields.push(new EditorFieldType(
    "concept", "project", "Project", "text", "",
    "Project this concept belongs to."));

g_conceptFields.push(new EditorFieldType(
    "concept", "definition", "Definition", "text", "",
    "Defines the concept.  Use this field if you don't want to " +
    "maintain individual definitions at term level."));

g_conceptFields.push(new EditorFieldType(
    "concept", "source", "Source", "text", "",
    "Source reference of the concept."));

g_conceptFields.push(new EditorFieldType(
    "concept", "note", "Note", "text", "", "Any note."));

// ------ languages

g_languageFields.push(new EditorFieldType(
    "language", "source", "Source", "text", "",
    "Source reference for the language."));

g_languageFields.push(new EditorFieldType(
    "language", "note", "Note", "text", "", "Any note."));

// ------ terms

g_termFields.push(new EditorFieldType(
    "term", "type", "Term Type", "attr",
    "full form, short form, abbreviation, initialism, acronym, " +
    "international scientific term, common name, internationalism, " +
    "clipped term, variant, transliteration, transcription, symbol, " +
    "formula, phrase, collocation, boiler plate",
    "Terminological type of the term."));

g_termFields.push(new EditorFieldType(
    "term", "usage", "Usage", "attr",
    "preferred, admitted, deprecated",
    "Indicates if the term can be used in a translation or " +
    "if it should be avoided."));

g_termFields.push(new EditorFieldType(
    "term", "status", "Status", "attr",
    "proposed, reviewed, approved",
    "Indicates if the term is new, " +
    "has been reviewed, or approved for use in translations."));

g_termFields.push(new EditorFieldType(
    "term", "pos", "Part Of Speech", "attr",
    "noun, verb, adjective, adverb, other",
    "Part of speech class of the term."));

g_termFields.push(new EditorFieldType(
    "term", "gender", "Gender", "attr",
    "masculine, feminine, neuter, other",
    "Grammatical gender of a nominative term."));

g_termFields.push(new EditorFieldType(
    "term", "number", "Number", "attr",
    "singular, plural, dual, mass noun, other",
    "Grammatical number of a nominative term."));

g_termFields.push(new EditorFieldType(
    "term", "definition", "Definition", "text", "",
    "Defines the meaning of the term."));

g_termFields.push(new EditorFieldType(
    "term", "example", "Example", "text", "",
    "Example sentence showing the term's use."));

g_termFields.push(new EditorFieldType(
    "term", "source", "Source", "text", "",
    "Source reference of the term."));

g_termFields.push(new EditorFieldType(
    "term", "note", "Note", "text", "", "Any note."));

// ------ fields

g_fieldFields.push(new EditorFieldType(
    "field", "source", "Source", "text", "",
    "A source reference for the field."));

g_fieldFields.push(new EditorFieldType(
    "field", "note", "Note", "text", "", "Any note."));

// ------ sources

g_sourceFields.push(new EditorFieldType(
    "field", "note", "Note", "text", "", "Any note."));


// Should ensure only fields unknown at this level can be user-defined.
// This is here to allow us to deal with imported data that has
// user-defined fields created in a different TMS.
function getUserDefinedField(level, type)
{
    return new EditorFieldType(level, type, type, "text", "", "User-defined field.");
}
