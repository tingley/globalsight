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

String lb_field_no_suggestion =
  EditUtil.toJavascript(bundle.getString("lb_field_no_suggestion"));
String lb_field_name_skip_this_column =
  EditUtil.toJavascript(bundle.getString("lb_field_name_skip_this_column"));
String lb_field_name_term =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term"));
String lb_field_expl_term =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term"));
String lb_field_name_concept_status =
  EditUtil.toJavascript(bundle.getString("lb_field_name_concept_status"));
String lb_field_expl_concept_status =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_concept_status"));
String lb_field_name_domain =
  EditUtil.toJavascript(bundle.getString("lb_field_name_domain_for_import"));
String lb_field_expl_domain =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_domain"));
String lb_field_name_concept_definition =
  EditUtil.toJavascript(bundle.getString("lb_field_name_concept_definition"));
String lb_field_expl_concept_definition =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_concept_definition"));
String lb_field_name_concept_source =
  EditUtil.toJavascript(bundle.getString("lb_field_name_concept_source"));
String lb_field_expl_concept_source =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_concept_source"));
String lb_field_name_term_status =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_status"));
String lb_field_expl_term_status =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_status"));
String lb_field_name_term_usage =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_usage_for_import"));
String lb_field_expl_term_usage =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_usage"));
String lb_field_name_term_type =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_type"));
String lb_field_expl_term_type =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_type"));
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
String lb_field_name_term_definition =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_definition"));
String lb_field_expl_term_definition =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_definition"));
String lb_field_name_term_example =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_example"));
String lb_field_expl_term_example =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_example"));
String lb_field_name_term_source =
  EditUtil.toJavascript(bundle.getString("lb_field_name_term_source"));
String lb_field_expl_term_source =
  EditUtil.toJavascript(bundle.getString("lb_field_expl_term_source"));
%>
/*
 * Copyright (c) 2000-2004 GlobalSight Corporation. All rights reserved.
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

function Column(id, name, example, type, encoding,
    associatedColumn, termLanguage)
{
    this.id = id;
    this.name = name;
    this.example = example;
    this.type = type;
    this.encoding = encoding;
    this.associatedColumn = associatedColumn;
    this.termLanguage = termLanguage;
}

function Language(name, locale, hasterms, exists)
{
    this.name = name;
    this.locale = locale;
    this.hasterms = hasterms; // not visible on UI (but boolean)
    this.exists = exists;
}

function Field(name, type, format, system, values)
{
    this.name = name;
    this.type = type;
    this.format = format;
    this.system = system; // boolean
    this.values = values;
}

// id is unused
function ImportFieldType(type, name, format, values, description,
    needLanguage, needAssociatedColumn, userDefined)
{
    this.type = type;
    this.name = name;
    this.format = format;
    this.values = values;
    this.description = description;
    this.needLanguage = needLanguage;
    this.needAssociatedColumn = needAssociatedColumn;
    this.userDefined = userDefined;
}

// Used in field.jsp to filter fields
ImportFieldType.prototype.skipField = function ()
{
    return this.format == null;
}

ImportFieldType.prototype.isAttribute = function ()
{
    return this.format == "attr";
}

ImportFieldType.prototype.isTerm = function ()
{
    return this.format == "term";
}

ImportFieldType.prototype.isUserDefined = function ()
{
    // type.startsWith("text-") || type.startsWith("attr-");
    return this.userDefined;
}

ImportFieldType.prototype.toString = function()
{
    return "{ImportFieldType " + " type=" + this.type + " name=" + this.name +
        " format=" + this.format + " values=" + this.values +
        " desc=" + this.description + " needLanguage=" + this.needLanguage +
        " needAssociatedColumn=" + this.needAssociatedColumn +
        (this.userDefined ? "user-defined" : "system-defined") + "}";
}

var aImportFieldTypes = new Array();

aImportFieldTypes.push(new ImportFieldType(
    "conceptdefinition", "<%=lb_field_name_concept_definition%>", "text",
    "", "<%=lb_field_expl_concept_definition%>", false, false, false));

aImportFieldTypes.push(new ImportFieldType(
    "conceptdomain", "<%=lb_field_name_domain%>", "attr",
    "<%=lb_field_no_suggestion%>",
    "<%=lb_field_expl_domain%>", false, false, false));

aImportFieldTypes.push(new ImportFieldType(
    "conceptsource", "<%=lb_field_name_concept_source%>", "text", "",
    "<%=lb_field_expl_concept_source%>", false, false, false));

aImportFieldTypes.push(new ImportFieldType(
    "conceptstatus", "<%=lb_field_name_concept_status%>", "attr",
    "proposed, reviewed, approved",
    "<%=lb_field_expl_concept_status%>",
    false, false, false));

aImportFieldTypes.push(new ImportFieldType(
    "termgender", "<%=lb_field_name_term_gender%>", "attr",
    "masculine, feminine, neuter, other",
    "<%=lb_field_expl_term_gender%>", false, true, false));

aImportFieldTypes.push(new ImportFieldType(
    "termnumber", "<%=lb_field_name_term_number%>", "attr",
    "singular, plural, dual, mass noun, other",
    "<%=lb_field_expl_term_number%>", false, true, false));

aImportFieldTypes.push(new ImportFieldType(
    "termpos", "<%=lb_field_name_term_pos%>", "attr",
    "noun, verb, adjective, adverb, other",
    "<%=lb_field_expl_term_pos%>", false, true, false));

aImportFieldTypes.push(new ImportFieldType(
    "skip", "<%=lb_field_name_skip_this_column%>", null, "",
    "", true, false, false));
    
aImportFieldTypes.push(new ImportFieldType(
    "source", "<%=lb_field_name_term_source%>", "text", "",
    "<%=lb_field_expl_term_source%>", false, true, false));

aImportFieldTypes.push(new ImportFieldType(
    "term", "<%=lb_field_name_term%>", null, "",
    "<%=lb_field_expl_term%>", true, false, false));
    
aImportFieldTypes.push(new ImportFieldType(
    "termdefinition", "<%=lb_field_name_term_definition%>", "text", "",
    "<%=lb_field_expl_term_definition%>", false, true, false));

aImportFieldTypes.push(new ImportFieldType(
    "termexample", "<%=lb_field_name_term_example%>", "text", "",
    "<%=lb_field_expl_term_example%>", false, true, false));

aImportFieldTypes.push(new ImportFieldType(
    "termstatus", "<%=lb_field_name_term_status%>", "attr",
    "proposed, reviewed, approved",
    "<%=lb_field_expl_term_status%>", false, false, false));

aImportFieldTypes.push(new ImportFieldType(
    "termtype", "<%=lb_field_name_term_type%>", "attr",
    "international scientific term, common name, internationalism, " +
    "full form, short form, abbreviation, initialism, acronym, " +
    "clipped term, variant, transliteration, transcription, symbol, " +
    "formula, phrase, collocation, boiler plate",
    "<%=lb_field_expl_term_type%>", false, true, false));
    
aImportFieldTypes.push(new ImportFieldType(
    "termusage", "<%=lb_field_name_term_usage%>", "attr",
    "preferred, admitted, deprecated",
    "<%=lb_field_expl_term_usage%>", false, true, false));


function getImportFieldFormat(type)
{
    for (i = 0; i < aImportFieldTypes.length; ++i)
    {
        var oImportFieldType = aImportFieldTypes[i];

        if (oImportFieldType.type == type)
        {
            return oImportFieldType.format;
        }
    }
}

function getImportFieldNameByType(type)
{
    for (var oIndex in aImportFieldTypes)
    {
        var oImportFieldType = aImportFieldTypes[oIndex];

        if (oImportFieldType.type.toLowerCase() == type.toLowerCase())
        {
            return oImportFieldType.name;
        }
    }

    return null;
}

function getCustomFieldFormatByType(type)
{
    if (type.startsWith("attr"))
    {
        type = "attr";
    }
    else if (type.startsWith("text"))
    {
        type = "text";
    }
}

function addCustomFields(p_definition)
{
    // Add custom fields to aImportFieldTypes

    // For the import UI, all types have an artificial prefix of
    // "concept" or "term" that guides the importer backend where to
    // put the field. The importer strips the prefix so that data in
    // the termbase uses clean type names.
    var nodes = $(p_definition).find("definition fields field");
    
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        var name = $(node).find("name").text();
        var type = $(node).find("type").text();
        var system =
          ($(node).find("system").text() == "true" ? true : false);
        var values = $(node).find("values").text();
        var format = getCustomFieldFormatByType(type);

        var importfield1 = new ImportFieldType(
            "concept" + type, name + " (on concept)", format, values,
            "user-defined field", false, true, true);
        var importfield2 = new ImportFieldType(
            "term" + type, name + " (on term)", format, values,
            "user-defined field", false, true, true);
            
        aImportFieldTypes.push(importfield1);
        aImportFieldTypes.push(importfield2);
    }

    //alert(p_definition.xml);
    //alert(aImportFieldTypes);
}
