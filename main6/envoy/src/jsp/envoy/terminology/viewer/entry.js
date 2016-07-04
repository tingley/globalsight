//                           -*- Mode: Javascript -*-
//
// Copyright (c) 2000-2004 GlobalSight Corporation. All rights reserved.
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

var g_inputModelFeedback = '' /*'click to edit'*/;

//
// HtmlToXml
//

function HtmlToXml(entry)
{
    var result = new StringBuilder('<conceptGrp>\n');
    var children = entry.children;

    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (child.className.indexOf('fakeConceptGrp') >= 0)
        {
            result.append(HtmlToXmlConceptGrp(child));

        }
        else if (child.className.indexOf('transacGrp') >= 0)
        {
            result.append(HtmlToXmlTransacGrp(child));
        }
        else if (child.className.indexOf('fieldGrp') >= 0)
        {
            result.append(HtmlToXmlFieldGrp(child));
        }
        else if (child.className.indexOf('languageGrp') >= 0)
        {
            result.append(HtmlToXmlLanguageGrp(child));
        }
    }

    result.append('</conceptGrp>\n');

    return result.toString();
}

function HtmlToXmlConceptGrp(node)
{
    var result = "<concept>";

    result += node.children[1].innerText;
    result += "</concept>\n";

    return result;
}

function HtmlToXmlTransacGrp(node)
{
    var result = "<transacGrp>\n";

    result += '<transac type="';
    result += node.getAttribute("type");
    result += '">';
    result += node.getAttribute("author");
    result += '</transac>\n';

    result += "<date>";
    result += node.getAttribute("date");
    result += "</date>\n";

    result += "</transacGrp>\n";

    return result;
}

function HtmlToXmlSourceGrp(node)
{
    var result = "";

    result += "<sourceGrp>";
    result += "<source>";
    result += getXhtml(node.children[1]);
    result += "</source>\n";

    var children = node.children;
    for (var i = 2; i < children.length; i++)
    {
        var child = children[i];

        if (child.className.indexOf('fieldGrp') >= 0)
        {
            result += HtmlToXmlFieldGrp(child);
        }
    }

    result += "</sourceGrp>\n";

    return result;
}

function HtmlToXmlNoteGrp(node)
{
    var result = "";

    result += "<noteGrp>";
    result += "<note>";
    result += getXhtml(node.children[1]);
    result += "</note>";
    result += "</noteGrp>\n";

    return result;
}

function HtmlToXmlFieldGrp(node)
{
    var result = "";

    if (node.firstChild == null)
    	return result;
    
    var type = node.firstChild.getAttribute("type");
    var t = type.toLowerCase();

    if (t == "source")
    {
        result += HtmlToXmlSourceGrp(node);
    }
    else if (t == "note")
    {
        result += HtmlToXmlNoteGrp(node);
    }
    else
    {
        result += "<descripGrp>";
        result += '<descrip type="';
        result += type;
        result += '">';
        result += getXhtml(node.children[1]);
        result += '</descrip>\n';

        var children = node.children;
        for (var i = 2; i < children.length; i++)
        {
            var child = children[i];

            if (child.className.indexOf('fieldGrp') >= 0)
            {
                result += HtmlToXmlFieldGrp(child);
            }
        }

        result += "</descripGrp>\n";
    }

    return result;
}

// throws exception if language is empty
function HtmlToXmlLanguageGrp(node)
{
    var result = '<languageGrp>\n';

    var children = node.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (child.className.indexOf('fakeLanguageGrp') >= 0)
        {
            result += '<language name="';
            result += getXhtml(child.children[1]);
            result += '" locale="';
            result += child.children[1].getAttribute("locale");
            result += '"/>\n';
        }
        else if (child.className.indexOf('fieldGrp') >= 0)
        {
            result += HtmlToXmlFieldGrp(child);
        }
        else if (child.className.indexOf('termGrp') >= 0)
        {
            result += HtmlToXmlTermGrp(child);
        }
    }

    result += '</languageGrp>\n';

    return result;
}

// throws exception if term is empty
function HtmlToXmlTermGrp(node)
{
    var result = '<termGrp>\n';

    var children = node.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];

        if (child.className.indexOf('fakeTermGrp') >= 0)
        {
            
            var termId = child.children[1].getAttribute("termId");
            if(termId != null) {
                result += '<term termId="'+ termId +'">';
            }
            else 
            {
                result += '<term termId="-1000">';
            }
            
            result += getXhtml(child.children[1]);
            result += '</term>\n';
        }
        else if (child.className.indexOf('fieldGrp') >= 0)
        {
            result += HtmlToXmlFieldGrp(child);
        }
    }

    result += '</termGrp>\n';

    return result;
}

//
// XmlToHtml (for editor)
//

/**
 * Maps an XML entry to HTML. Internal field types are mapped to
 * display names using a context object. See management/objects_js.jsp.
 *
 * Different results are returned for the Browser's viewer and editor(s).
 */
function XmlToHtml(objJson, context, inputmodel)
{
    var result;

    if (context.ui == "viewer")
    {
        result = VXmlToHtmlConceptGrp(objJson, context);
    }
    else
    {
        result = XmlToHtmlConceptGrp(objJson, context, inputmodel);
    }

    return result;
}

function XmlToHtmlConceptGrp(node, ctxt, inputmodel)
{
    var result = new StringBuilder(
        '<DIV class="conceptGrp"><SPAN class="fakeConceptGrp">');

    var id = node.concept;
    if (typeof(id) == "undefined"){
    	id == null;
    }
    
    if(inputmodel) {
        id = "";
    }

    if (id && parseInt(id) > 0)
    {
        result.append('<SPAN class="conceptlabel">&nbsp;&nbsp;');
        result.append(ctxt.mapEntry());
        result.append('</SPAN><SPAN class="concept">' + id + '</SPAN>');
    }
    else
    {
        result.append('<SPAN class="conceptlabel">&nbsp;&nbsp;');
        result.append(ctxt.mapNewEntry());
        result.append('</SPAN><SPAN class="concept"></SPAN>');
    }

    result.append('</SPAN>');

    result.append(XmlToHtmlTransacGrp(node.transacGrp, ctxt));
    result.append(XmlToHtmlDescripGrp(node.descripGrp, ctxt));
    result.append(XmlToHtmlSourceGrp(node.sourceGrp, ctxt));
    result.append(XmlToHtmlNoteGrp(node.noteGrp, ctxt));
    
    var languageGrp = toArray(node.languageGrp);

    result.append(XmlToHtmlLanguageGrp(getSourceLang(languageGrp), ctxt,inputmodel));
    result.append(XmlToHtmlLanguageGrp(getTargetLang(languageGrp), ctxt, inputmodel));
    result.append(XmlToHtmlLanguageGrp(getOtherLang(languageGrp), ctxt, inputmodel));

    result.append('</DIV>');

    return result.toString();
}

function XmlToHtmlTransacGrp(nodes, ctxt)
{
    var result = "";
    
    if (typeof(nodes) == "undefined")
		return "";

    nodes = toArray(nodes);
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result += '<div class="transacGrp" type="';
        result += node.transac["@type"];
        result += '" author="';
        result += node.transac["#text"];
        result += '" date="';
        result += node.date;
        result += '">';

        result += '<div CLASS="transaclabel">';
        result += ctxt.mapTransac(node.transac["@type"]);
        result += '</div>';

        result += '<div CLASS="transacvalue">';
        result += node.date;
        result += ' (';
        result += node.transac["#text"];
        result += ')';
        result += '</div>';

        result += '</div>';
    }

    return result;
}

function XmlToHtmlNoteGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
    var result = "";
    nodes = toArray(nodes);
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result += '<DIV class="fieldGrp" ondblclick="doEdit(this, event);">';

        result += XmlToHtmlNote(node.note, ctxt);

        result += '</DIV>';
    }

    return result;
}

function XmlToHtmlNote(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = '<SPAN CLASS="fieldlabel" unselectable="on" type="note">';
    result += ctxt.mapNote('note');
    result += '</SPAN>';

    result += '<SPAN CLASS="fieldvalue">' + node + '</SPAN>';

    return result;
}

function XmlToHtmlSourceGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
    var result = "";
    nodes = toArray(nodes);
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result += '<DIV class="fieldGrp" ondblclick="doEdit(this, event);">';

        result += XmlToHtmlSource(getSourceNode(node), ctxt);
        result += XmlToHtmlNoteGrp(node.noteGrp, ctxt);

        result += '</DIV>';
    }

    return result;
}

function getSourceNode(sourceGrp)
{
	if(typeof(sourceGrp.source) != "undefined")
		return sourceGrp.descrip;
	
	if (sourceGrp.length == 1)
		return sourceGrp[0];
	
	return sourceGrp;
}


function XmlToHtmlSource(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = '<SPAN CLASS="fieldlabel" unselectable="on" type="source">';
    result += ctxt.mapSource('source');
    result += '</SPAN>';

    result += '<SPAN CLASS="fieldvalue">' + node + '</SPAN>';

    return result;
}

function XmlToHtmlDescripGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
    var result = "";
    nodes = toArray(nodes);
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result += '<DIV class="fieldGrp" ondblclick="doEdit(this, event);">';

        result += XmlToHtmlDescrip(getDescripNode(node), ctxt);
        result += XmlToHtmlSourceGrp(node.sourceGrp, ctxt);
        result += XmlToHtmlNoteGrp(node.noteGrp, ctxt);

        result += '</DIV>';
    }

    return result;
}

function getDescripNode(descripGrp)
{
	if(typeof(descripGrp.descrip) != "undefined")
		return descripGrp.descrip;
	
	if (descripGrp.length == 1)
		return descripGrp[0];
	
	return descripGrp;
}

function XmlToHtmlDescrip(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = '<SPAN CLASS="fieldlabel" unselectable="on" type="';
    result += node['@type'];
    result += '">';
    result += ctxt.mapDescrip(node['@type'].toLowerCase());
    result += '</SPAN>';

    result += '<SPAN CLASS="fieldvalue">' + node['#text'] + '</SPAN>';

    return result;
}

function XmlToHtmlLanguageGrp(nodes, ctxt, inputmodel)
{
	if (typeof(nodes) == "undefined")
		return "";
	
    var result = "";
    nodes = toArray(nodes);
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result += '<DIV class="languageGrp">';
        result += '<SPAN class="fakeLanguageGrp">';
        result += XmlToHtmlLanguage(node.language, ctxt);
        result += '</SPAN>';

        result += XmlToHtmlDescripGrp(node.descripGrp, ctxt);
        result += XmlToHtmlSourceGrp(node.sourceGrp, ctxt);
        result += XmlToHtmlNoteGrp(node.noteGrp, ctxt);

        result += XmlToHtmlTermGrp(node.termGrp, ctxt, inputmodel);

        result += '</DIV>';
    }

    return result;
}

function XmlToHtmlLanguage(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = "";

    result += '<SPAN class="languagelabel">Language</SPAN>';

    result += '<SPAN class="language" unselectable="on" locale="';
    result += node['@locale'];
    
    result += '">';
    result += node['@name'];

    result += '</SPAN>';

    return result;
}

function XmlToHtmlTermGrp(nodes, ctxt, inputmodel)
{
	if (typeof(nodes) == "undefined")
		return "";
	
    var result = "";
    nodes = toArray(nodes);
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];
        var isFirst = (i == 0);

        result += '<DIV class="termGrp">';
        result += '<DIV class="fakeTermGrp" ondblclick="doEdit(this, event);">';
        result += XmlToHtmlTerm(getTermNode(node), ctxt, inputmodel, isFirst);
        result += '</DIV>';

        result += XmlToHtmlDescripGrp(node.descripGrp, ctxt);
        result += XmlToHtmlSourceGrp(node.sourceGrp, ctxt);
        result += XmlToHtmlNoteGrp(node.noteGrp, ctxt);

        result += '</DIV>';
    }

    return result;
}

function getTermNode(termGrp)
{
	if(typeof(termGrp.term) != "undefined")
		return termGrp.term;
	
	if (termGrp.length == 1)
		return termGrp[0];
	
	return termGrp;
}

function XmlToHtmlTerm(node, ctxt, inputmodel, isFirst)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = '<SPAN class="termlabel">';
    
    var termId;
    
    if(typeof(node['@termId']) != "undefined") {
        termId = node['@termId'];
    }

    if(inputmodel) {
         result += ctxt.mapTerm(true, isFirst);
    }
    else {
        result += ctxt.mapTerm(false);
    }

    result += '</SPAN>';

    result += '<SPAN class="term" termId="' + termId +'">' + node['#text'] + '</SPAN>';

    return result;
}

//
// XmlToHtml (for viewer)
//

function VXmlToHtmlConceptGrp(objJson, ctxt)
{
    var result = new StringBuilder(
        '<DIV class="vconceptGrp"><SPAN class="vfakeConceptGrp">');

    var id = objJson.concept;
    
    if (typeof(id) == "undefined"){
    	id == null;
    }

    if (id && parseInt(id) > 0)
    {
        result.append('<SPAN class="vconceptlabel">&nbsp;&nbsp;');
        result.append(ctxt.mapEntry());
        result.append('</SPAN><SPAN class="vconcept">' + id + '</SPAN>');
    }
    else
    {
        result.append('<SPAN class="vconceptlabel">');
        result.append(ctxt.mapNewEntry());
        result.append('</SPAN><SPAN class="vconcept"></SPAN>');
    }

    result.append('</SPAN>');
    result.append(VXmlToHtmlTransacGrp(objJson.transacGrp, ctxt));
    
    //I don't know why there is a descrip
    //result.append(VXmlToHtmlDefinitionGrp(node.selectNodes('descrip'), ctxt));
    // in tbx files, definition may be used to modify concept
    result.append(VXmlToHtmlDescripGrp(objJson.descripGrp, ctxt));
    result.append(VXmlToHtmlSourceGrp(objJson.sourceGrp, ctxt));
    result.append(VXmlToHtmlNoteGrp(objJson.noteGrp, ctxt));
    
    var languageGrp = toArray(objJson.languageGrp);
    
    result.append(VXmlToHtmlLanguageGrp(getSourceLang(languageGrp), ctxt));
    result.append(VXmlToHtmlLanguageGrp(getTargetLang(languageGrp), ctxt));
    result.append(VXmlToHtmlLanguageGrp(getOtherLang(languageGrp), ctxt));

    result.append('</DIV>');

    return result.toString();
}

function getSourceLang(languageGrp)
{
	var lang = new Array();
	
	for (var i = 0; i < languageGrp.length; i++)
	{
		var language = languageGrp[i];

		if (typeof(language["source-lang"]) != "undefined")
		{
			lang.push(language);
		}	
	}
	
	return lang;
}

function getTargetLang(languageGrp)
{
	var lang = new Array();
	
	for (var i = 0; i < languageGrp.length; i++)
	{
		var language = languageGrp[i];

		if (typeof(language["target-lang"]) != "undefined")
		{
			lang.push(language);
		}	
	}
	
	return lang;
}

function getOtherLang(languageGrp)
{
	var lang = new Array();
	
	for (var i = 0; i < languageGrp.length; i++)
	{
		var language = languageGrp[i];

		if (typeof(language["source-lang"]) == "undefined" &&
				typeof(language["target-lang"]) == "undefined")
		{
			lang.push(language);
		}	
	}
	
	return lang;
}


function VXmlToHtmlDefinitionGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
	var result = new StringBuilder();

	for (var i = 0; i < nodes.length; i++)
	{
		var node = nodes[i];

		result.append('<DIV class="vfieldGrp">');
		
		result.append(VXmlToHtmlDescrip(node, ctxt));

		result.append('</DIV>');
	}
	return result.toString();
}

function VXmlToHtmlTransacGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
    var result = new StringBuilder();

    for (var i = 0; i < nodes.length; i++)
    {
        //var node = nodes.item[i];
        var node = nodes[i];

        result.append('<div class="vtransacGrp" type="');
        result.append(node.transac["@type"]);
     
        result.append('" author="');
        result.append(node.transac["#text"]);
        result.append('" date="');
        result.append(node.date);
        result.append('">');

        result.append('<div CLASS="vtransaclabel">');
        result.append(ctxt.mapTransac(node.transac["@type"]));

        result.append('</div>');
 
        result.append('<div CLASS="vtransacvalue">');
        result.append(node.date);
        result.append(' (');
        result.append(node.transac["#text"]);
        result.append(')'); 
        result.append('</div>');
        result.append('</div>');
    }

    return result.toString();
}

function VXmlToHtmlNoteGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result += '<DIV class="vfieldGrp">';

        result += VXmlToHtmlNote(node.note, ctxt);

        result += '</DIV>';
    }

    return result;
}

function VXmlToHtmlNote(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = new StringBuilder();

    result.append('<SPAN class="vfieldlabel" unselectable="on" type="note">');
    result.append(ctxt.mapNote('note'));
    result.append('</SPAN>');

    result.append('<SPAN class="vfieldvalue">' + node + '</SPAN>');

    return result.toString();
}

function toArray(nodes)
{
	if (typeof(nodes) == "undefined"){
		return new Array();
	}
	
	if (typeof(nodes.length) != "undefined"){
		return nodes;
	}
	
	var n = new Array();
	n.push(nodes);
	return n;
}

function VXmlToHtmlSourceGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
    var result = new StringBuilder();

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result.append('<DIV class="vfieldGrp">');

        result.append(VXmlToHtmlSource(node.source, ctxt));
        result.append(VXmlToHtmlNoteGrp(node.noteGrp, ctxt));

        result.append('</DIV>');
    }

    return result.toString();
}

function VXmlToHtmlSource(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = new StringBuilder();

    result.append('<SPAN class="vfieldlabel" unselectable="on" type="source">');
    result.append(ctxt.mapSource('source'));
    result.append('</SPAN>');

    result.append('<SPAN class="vfieldvalue">' + node + '</SPAN>');

    return result.toString();
}

function VXmlToHtmlDescripGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
    var result = new StringBuilder();

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result.append('<DIV class="vfieldGrp">');

        result.append(VXmlToHtmlDescrip(node.descrip, ctxt));
        result.append(VXmlToHtmlSourceGrp(node.sourceGrp, ctxt));
        result.append(VXmlToHtmlNoteGrp(node.noteGrp, ctxt));

        result.append('</DIV>');
    }

    return result.toString();
}

function VXmlToHtmlDescrip(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
    var result = new StringBuilder();

    result.append('<SPAN class="vfieldlabel" unselectable="on" type="');
    
    result.append(node['@type']);
    result.append('">');
    result.append(ctxt.mapDescrip(node['@type'].toLowerCase()));
    result.append('</SPAN>');

    result.append('<SPAN class="vfieldvalue">' + node['#text'] + '</SPAN>');

    return result.toString();
}

function VXmlToHtmlLanguageGrp(nodes, ctxt)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
    var result = new StringBuilder();

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        result.append('<DIV class="');

        if (typeof(node.language["source-lang"]) != "undefined")
        {
            result.append('vsourceLanguageGrp');
        }
        else if (typeof(node.language["target-lang"]) != "undefined")
        {
            result.append('vtargetLanguageGrp');
        }
        else
        {
            result.append('vlanguageGrp');
        }

        result.append('">');
        result.append('<SPAN class="vfakeLanguageGrp">');
        result.append(
            VXmlToHtmlLanguage(node.language, ctxt));
        result.append('</SPAN>');

        result.append(VXmlToHtmlDescripGrp(node.descripGrp, ctxt));
        result.append(VXmlToHtmlSourceGrp(node.sourceGrp, ctxt));
        result.append(VXmlToHtmlNoteGrp(node.noteGrp, ctxt));

        var langNode = node.language;
        var currentLocale = langNode['@locale'];
     
        if(!document.all) {
           currentLocale = langNode['@locale'];
        }

        var terms = toArray(node.termGrp);
        
        result.append(VXmlToHtmlTermGrp(
        		getSearchTerm(terms), ctxt, currentLocale));
        result.append(VXmlToHtmlTermGrp(
        		getNotSearchTerm(terms), ctxt, currentLocale));

        result.append('</DIV>');
    }

    return result.toString();
}

function getSearchTerm(terms)
{
	var ts = new Array();
	for (var i = 0; i < terms.length; i++)
    {
		var term = terms[i];
		if (typeof(term["@search-term"]) != "undefined")
			ts.push(term);
    }
	
	return ts;
}

function getNotSearchTerm(terms)
{
	var ts = new Array();
	for (var i = 0; i < terms.length; i++)
    {
		var term = terms[i];
		if (typeof(term["@search-term"]) == "undefined")
			ts.push(term);
    }
	
	return ts;
}

function VXmlToHtmlLanguage(node, ctxt)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = new StringBuilder();

    result.append('<SPAN class="vlanguagelabel">Language</SPAN>');

    result.append('<SPAN class="vlanguage" unselectable="on" locale="');
    
    result.append(node['@locale']);
    
    result.append('">');
    result.append(node['@name']);

    result.append('</SPAN>');

    return result.toString();
}

function VXmlToHtmlTermGrp(nodes, ctxt, currentLocale)
{
	if (typeof(nodes) == "undefined")
		return "";
	
	nodes = toArray(nodes);
	
    var result = new StringBuilder();

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];
        var isFirst = (i == 0);

        result.append('<DIV class="vtermGrp">');
      	if (currentLocale.substring(0,2) == 'ar' || currentLocale.substring(0,2) == 'he'
    	      || currentLocale.substring(0,2) == 'fa' || currentLocale.substring(0,2) == 'ur' )
        {
            result.append('<DIV class="vfakeTermGrp" style="margin-right: 5px;" align="right">');	
        }
        else 
        {
	          result.append('<DIV class="vfakeTermGrp">');	
	      }
	      
        var termId = node.term["@termId"];
        
        result.append(VXmlToHtmlTerm(node.term, ctxt, currentLocale, termId));
        result.append('</DIV>');

        result.append(VXmlToHtmlDescripGrp(node.descripGrp, ctxt));
        result.append(VXmlToHtmlSourceGrp(node.sourceGrp, ctxt));
        result.append(VXmlToHtmlNoteGrp(node.noteGrp, ctxt));

        result.append('</DIV>');
    }

    return result.toString();
}

function VXmlToHtmlTerm(node, ctxt, currentLocale, termId)
{
	if (typeof(node) == "undefined")
		return "";
	
    var result = new StringBuilder();

    result.append('<SPAN class="vtermlabel">');
    result.append(ctxt.mapTerm(null));
    result.append('</SPAN>');

    if (typeof(node["@search-term"]))
    {
    	if (currentLocale.substring(0,2) == 'ar' || currentLocale.substring(0,2) == 'he'
    	      || currentLocale.substring(0,2) == 'fa' || currentLocale.substring(0,2) == 'ur')
    	{

            result.append('<SPAN style="direction: rtl; unicode-bidi: embed;" class="vsearchterm" termId="' + termId +'">' + node["#text"] + '</SPAN>');
    	}
        else
		{
	            result.append('<SPAN class="vsearchterm" termId="' + termId +'">' + node["#text"] + '</SPAN>');		
		}
    }
    else
    {
    	if (currentLocale.substring(0,2) == 'ar' || currentLocale.substring(0,2) == 'he'
    	      || currentLocale.substring(0,2) == 'fa' || currentLocale.substring(0,2) == 'ur')
    	{
            result.append('<SPAN style="direction: rtl; unicode-bidi: embed;" class="vterm" termId="' + termId +'">' + node["#text"] + '</SPAN>');
    	}
        else
		{
	            result.append('<SPAN class="vterm" termId="' + termId +'">' + node["#text"] + '</SPAN>');		
		}
    }

    return result.toString();
}

//
// HTML fragment constructors
//

function getLanguageGrpDiv(language, locale, term, inputmodel)
{
    var result = '<DIV class="languageGrp"><SPAN class="fakeLanguageGrp"><SPAN class="languagelabel">Language</SPAN><SPAN class="language" unselectable="on" locale="' + locale + '">' + language + '</SPAN></SPAN>' + (inputmodel? getTermGrpDivInputModel(term, true): getTermGrpDiv(term)) + '</DIV>';

    return result;
}

function getTermGrpDiv(term)
{
    var result = '<DIV class="termGrp"><DIV class="fakeTermGrp" ondblclick="doEdit(this, event);"><SPAN class="termlabel">Term</SPAN><SPAN class="term">' + term + '</SPAN></DIV></DIV>';

    return result;
}

function getTermGrpDivInputModel(term, isFirst)
{
    var result = '<DIV class="termGrp"><DIV class="fakeTermGrp" ondblclick="doEdit(this, event);"><SPAN class="termlabel">' + (isFirst ? 'Main Term' : 'Synonym') + '</SPAN><SPAN class="term">' + term + '</SPAN></DIV></DIV>';

    return result;
}

function getFieldGrpDiv(name, type, value)
{
    var result = '<DIV class="fieldGrp" ondblclick="doEdit(this, event);"><SPAN class="fieldlabel" unselectable="on" type="' + type + '">' + name + '</SPAN><SPAN class="fieldvalue">' + value + '</SPAN></DIV>';

    return result;
}

//
// Input Model to XML Entry
//

function ImToXml(dom)
{
    ImToXmlConceptGrp(dom.selectSingleNode('conceptGrp'));
}

function ImToXmlConceptGrp(node)
{
    var temp = node.selectSingleNode('concept');
    var id = temp ? temp.text : null;

    if (!id || id != '0')
    {
        // insert or overwrite a <concept> node at the beginning
    }

    ImToXmlTransacGrp(node.selectNodes('transacGrp'));
    ImToXmlDescripGrp(node.selectNodes('descripGrp'));
    ImToXmlSourceGrp(node.selectNodes('sourceGrp'));
    ImToXmlNoteGrp(node.selectNodes('noteGrp'));
    ImToXmlLanguageGrp(node.selectNodes('languageGrp'));
}

function ImToXmlTransacGrp(nodes)
{
    // IM does not contain transacGrp
}

function ImToXmlNoteGrp(nodes)
{
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        ImToXmlNote(node.selectSingleNode('note'));
    }
}

function ImToXmlNote(node)
{
    node.text = g_inputModelFeedback;
}

function ImToXmlSourceGrp(nodes)
{
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        ImToXmlSource(node.selectSingleNode('source'));
        ImToXmlNoteGrp(node.selectNodes('noteGrp'));
    }
}

function ImToXmlSource(node)
{
    node.text = g_inputModelFeedback;
}

function ImToXmlDescripGrp(nodes)
{
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        ImToXmlDescrip(node.selectSingleNode('descrip'));
        ImToXmlSourceGrp(node.selectNodes('sourceGrp'));
        ImToXmlNoteGrp(node.selectNodes('noteGrp'));
    }
}

function ImToXmlDescrip(node)
{
    node.text = g_inputModelFeedback;
}

function ImToXmlLanguageGrp(nodes)
{
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        ImToXmlDescripGrp(node.selectNodes('descripGrp'));
        ImToXmlSourceGrp(node.selectNodes('sourceGrp'));
        ImToXmlNoteGrp(node.selectNodes('noteGrp'));
        ImToXmlTermGrp(node.selectNodes('termGrp'));
    }
}

function ImToXmlLanguage(node)
{
    // do nothing
}

function ImToXmlTermGrp(nodes)
{
    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes[i];

        ImToXmlTerm(node.selectSingleNode('term'));
        ImToXmlDescripGrp(node.selectNodes('descripGrp'));
        ImToXmlSourceGrp(node.selectNodes('sourceGrp'));
        ImToXmlNoteGrp(node.selectNodes('noteGrp'));
    }
}

function ImToXmlTerm(node)
{
    node.text = g_inputModelFeedback;
}

//
// Helpers
//

/**
 * Returns the XML representation like node.xml but without the
 * top-level tag.
 */
function getInnerXml(p_node)
{
    var result = "";

    var nodes = p_node.childNodes;
    for (var i = 0, max = nodes.length; i < max; i++)
    {
        var node = nodes[i];

        if (node.nodeType == 3 || node.nodeType == 4)
        {
            // NODE_TEXT and NODE_CDATA_SECTION.
            // Don't use node.text, it strips whitespace.
            result += node.nodeValue;
        }
        else
        {
            result += node.xml;
        }
    }

    return result;
}

/*
 * fck_xhtml.js - Part 1: String Builder 1.02
 * A class that allows more efficient building of strings than concatenation.
 * For further information: http://webfx.eae.net
 * License: GPL - The GNU General Public License
 *
 * Authors:
 *   Erik Arvidsson (http://webfx.eae.net/contact.html#erik)
 */
function StringBuilder(sString)
{
    // public
    this.length = 0;

    this.append = function (sString)
    {
        // append argument
        this.length += (this._parts[this._current++] = String(sString)).length;

        // reset cache
        this._string = null;
        return this;
    };

    this.toString = function ()
    {
        if (this._string != null)
        {
            return this._string;
        }

        var s = this._parts.join("");
        this._parts = [s];
        this._current = 1;
        this.length = s.length;

        return this._string = s;
    };

    // private
    this._current   = 0;
    this._parts     = [];
    this._string    = null; // used to cache the string

    // init
    if (sString != null)
    {
        this.append(sString);
    }
}

/*
 * fck_xhtml.js - Part 2: Get XHTML for IE 1.03
 * Serilizes an IE HTML DOM tree to a well formed XHTML string.
 * For further information: http://webfx.eae.net/dhtml/richedit/js/getxhtml.js
 * License: GPL - The GNU General Public License
 *
 * Authors:
 *   Erik Arvidsson (http://webfx.eae.net/contact.html#erik)
 */

// Changed by FredCK
function getXhtml(oNode)
{ 
    var sb = new StringBuilder;
    var cs = oNode.childNodes;
    var l = cs.length;
    for (var i = 0; i < l; i++)
    {
        _appendNodeXHTML(cs[i], sb);
    }

    return sb.toString();
}

function _fixAttribute(s)
{
    return String(s).replace(/\&/g, "&amp;").replace(/</g, "&lt;").replace(/\"/g, "&quot;");
}

function _fixText(s)
{
    return String(s).replace(/\&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function _getAttributeValue(oAttrNode, oElementNode, sb)
{
    if (!oAttrNode.specified)
    {
        return;
    }

    var name = oAttrNode.expando ? oAttrNode.nodeName :
        oAttrNode.nodeName.toLowerCase() ;      // FredCK: changed
    var value = oAttrNode.nodeValue;

    // CvdL: need to fix absolute URLs.
    if ((oElementNode.tagName == "IMG" && name == "src") ||
        (oElementNode.tagName == "A" && name == "href"))
    {
        value = oElementNode.getAttribute(name, 2);
        value = value.replace(
            /^https?:\/\/[^\/]+\/ambassador\/terminology\/images\//g,
            '/globalsight/terminology/images/');
        value = value.replace(
            /^https?:\/\/[^\/]+\/ambassador\/terminology\/media\//g,
            '/globalsight/terminology/media/');

        sb.append(" " + name + "=\"" + _fixAttribute(value) + "\"");
    }
    else if (name != "style" )
    {
        if (!isNaN(value) || name == "src" || name == "href")
        {
            // IE5.x bugs for number values
            // FredCK: force to get the correct href or source
            // FredCK: gets the attributes values as is (2 option)
            value = oElementNode.getAttribute(name, 2);
        }

        sb.append(" " + name + "=\"" + _fixAttribute(value) + "\"");
    }
    else
    {
        sb.append(" style=\"" + _fixAttribute(oElementNode.style.cssText) + "\"");
    }
}

function _appendNodeXHTML(node, sb)
{
    switch (node.nodeType) {
        case 1: // ELEMENT

            if (node.nodeName == "!")
            {
                // IE5.0 and IE5.5 are weird
                sb.append(node.text);
                break;
            }

            var name = node.nodeName;
            if (node.scopeName == "HTML" || node.scopeName == "m")
            {
                name = name.toLowerCase();
            }

            if (node.scopeName == "HTML")
            {
                sb.append("<" + name);
            }
            else
            {
                sb.append("<" + node.scopeName + ":" + name);
            }

            // attributes
            var attrs = node.attributes;
            var l = attrs.length;
            for (var i = 0; i < l; i++)
            {
                _getAttributeValue(attrs[i], node, sb);
            }

            if (name == "input" && node.value)
            {
                sb.append(" value=\"" + _fixAttribute(node.value) + "\"");
            }

            if (node.canHaveChildren || node.hasChildNodes())
            {
                sb.append(">");

                // childNodes
                var cs = node.childNodes;
                l = cs.length;
                for (var i = 0; i < l; i++)
                    _appendNodeXHTML(cs[i], sb);

                if (node.scopeName == "HTML")
                {
                    sb.append("</" + name + ">");
                }
                else
                {
                    sb.append("</" + node.scopeName + ":" + name + ">");
                }
            }
            else if (name == "script")
            {
                sb.append(">" + node.text + "</" + name + ">");
            }
            else if (name == "title" || name == "style" || name == "comment")
            {
                sb.append(">" + node.innerHTML + "</" + name + ">");
            }
            else
            {
                sb.append(" />");
            }

            break;

        case 3: // TEXT
            sb.append( _fixText(node.nodeValue) );
            break;

        case 4:
            sb.append("<![CDA" + "TA[\n" + node.nodeValue + "\n]" + "]>");
            break;

        case 8:
            //sb.append("<!--" + node.nodeValue + "-->");
            sb.append(node.text);
            if (/(^<\?xml)|(^<\!DOCTYPE)/.test(node.text) )
                sb.append("\n");
            break;

        case 9: // DOCUMENT
            // childNodes
            var cs = node.childNodes;
            l = cs.length;
            for (var i = 0; i < l; i++)
            {
                _appendNodeXHTML(cs[i], sb);
            }
            break;

        default:
            sb.append("<!--\nNot Supported:\n\n" + "nodeType: " +
                node.nodeType + "\nnodeName: " + node.nodeName + "\n-->");
    }
}
