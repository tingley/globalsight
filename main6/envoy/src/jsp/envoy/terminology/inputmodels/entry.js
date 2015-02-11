
//
// HtmlToXml
//

function HtmlToXml(entry)
{
    var result = '<conceptGrp>\n';

    var children = entry.children;
    for (var i = 0; i < children.length; i++)
    {
        var child = children(i);

        if (child.className.indexOf('fakeConceptGrp') >= 0)
        {
            result += HtmlToXmlConceptGrp(child);
        }
        else if (child.className.indexOf('transacGrp') >= 0)
        {
            result += HtmlToXmlTransacGrp(child);
        }
        else if (child.className.indexOf('fieldGrp') >= 0)
        {
            result += HtmlToXmlFieldGrp(child);
        }
        else if (child.className.indexOf('languageGrp') >= 0)
        {
            result += HtmlToXmlLanguageGrp(child);
        }
    }

    result += '</conceptGrp>\n';

    return result;
}

function HtmlToXmlConceptGrp(node)
{
    var result = "<concept>";
    result += node.children(1).innerText;
    result += "</concept>\n";

    return result;
}

function HtmlToXmlTransacGrp(node)
{
    var result = "<transacGrp>\n";

    result += '<transac type="';
    result += node.type;
    result += '">';
    result += node.author;
    result += '</transac>\n';

    result += "<date>";
    result += node.date;
    result += "</date>\n";

    result += "</transacGrp>\n";

    return result;
}

function HtmlToXmlSourceGrp(node)
{
    var result = "";

    result += "<sourceGrp>";
    result += "<source>";
    result += node.children(1).innerHTML;
    result += "</source>\n";

    var children = node.children;
    for (var i = 2; i < children.length; i++)
    {
        var child = children(i);

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
    result += node.children(1).innerHTML;
    result += "</note>";
    result += "</noteGrp>\n";

    return result;
}

function HtmlToXmlFieldGrp(node)
{
    var result = "";

    var type = node.firstChild.type;
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
        result += node.children(1).innerHTML;
        result += '</descrip>\n';

        var children = node.children;
        for (var i = 2; i < children.length; i++)
        {
            var child = children(i);

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
        var child = children(i);

        if (child.className.indexOf('fakeLanguageGrp') >= 0)
        {
            result += '<language name="';
            result += child.children(1).innerHTML;
            result += '" locale="';
            result += child.children(1).locale;
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
        var child = children(i);

        if (child.className.indexOf('fakeTermGrp') >= 0)
        {
            result += '<term>';
            result += child.children(1).innerHTML;
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
// XmlToHtml
//

/**
 * Maps an XML entry to HTML. Internal field types are mapped to
 * display names using a context object. See management/objects_js.jsp.
 */
function XmlToHtml(dom, context)
{
    var result = XmlToHtmlConceptGrp(dom.selectSingleNode('conceptGrp'), context);

    return result;
}

function XmlToHtmlConceptGrp(node, context)
{
    var result = '<DIV class="conceptGrp"><SPAN class="fakeConceptGrp">' +
        '<SPAN class="conceptlabel">Input Model</SPAN> ' +
        '<SPAN class="concept"></SPAN></SPAN>\n';

    result += XmlToHtmlTransacGrp(node.selectNodes('transacGrp'), context);
    result += XmlToHtmlDescripGrp(node.selectNodes('descripGrp'), context);
    result += XmlToHtmlSourceGrp(node.selectNodes('sourceGrp'), context);
    result += XmlToHtmlNoteGrp(node.selectNodes('noteGrp'), context);
    result += XmlToHtmlLanguageGrp(node.selectNodes('languageGrp'), context);

    result += '</DIV>';

    return result;
}

function XmlToHtmlTransacGrp(nodes, context)
{
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes.item(i);

        result += '<SPAN class="transacGrp" type="';
        result += node.selectSingleNode("transac/@type").text;
        result += '" author="';
        result += node.selectSingleNode("transac").text;
        result += '" date="';
        result += node.selectSingleNode("date").text;
        result += '">\n';

        result += '<SPAN CLASS="transaclabel">';
        result += context.mapTransac(node.selectSingleNode("transac/@type").text);
        result += '</SPAN>\n';

        result += '<SPAN CLASS="transacvalue">';
        result += node.selectSingleNode("date").text;
        result += ' (';
        result += node.selectSingleNode("transac").text;
        result += ')';
        result += '</SPAN>\n';

        result += '</SPAN>\n';
    }

    return result;
}

function XmlToHtmlNoteGrp(nodes, context)
{
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes.item(i);

        result += '<DIV class="fieldGrp" ondblClick="doEdit(this);">\n';

        result += XmlToHtmlNote(node.selectSingleNode('note'), context);

        result += '</DIV>\n';
    }

    return result;
}

function XmlToHtmlNote(node, context)
{
    var result = '<SPAN CLASS="fieldlabel" unselectable="on" type="note">';
    result += context.mapNote('note');
    result += '</SPAN> ';

    result += '<SPAN CLASS="fieldvalue">' + node.text + '</SPAN>\n';

    return result;
}

function XmlToHtmlSourceGrp(nodes, context)
{
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes.item(i);

        result += '<DIV class="fieldGrp" ondblClick="doEdit(this);">\n';

        result += XmlToHtmlSource(node.selectSingleNode('source'), context);
        result += XmlToHtmlNoteGrp(node.selectNodes('noteGrp'), context);

        result += '</DIV>\n';
    }

    return result;
}

function XmlToHtmlSource(node, context)
{
    var result = '<SPAN CLASS="fieldlabel" unselectable="on" type="source">';
    result += context.mapSource('source');
    result += '</SPAN> ';

    result += '<SPAN CLASS="fieldvalue">' + node.text + '</SPAN>\n';

    return result;
}

function XmlToHtmlDescripGrp(nodes, context)
{
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes.item(i);

        result += '<DIV class="fieldGrp" ondblClick="doEdit(this);">\n';

        result += XmlToHtmlDescrip(node.selectSingleNode('descrip'), context);
        result += XmlToHtmlSourceGrp(node.selectNodes('sourceGrp'), context);
        result += XmlToHtmlNoteGrp(node.selectNodes('noteGrp'), context);

        result += '</DIV>\n';
    }

    return result;
}

function XmlToHtmlDescrip(node, context)
{
    var result = '<SPAN CLASS="fieldlabel" unselectable="on" type="';
    result += node.selectSingleNode('@type').text;
    result += '">';
    result += context.mapDescrip(node.selectSingleNode('@type').text);
    result += '</SPAN> ';

    result += '<SPAN CLASS="fieldvalue">' + node.text + '</SPAN>\n';

    return result;
}

function XmlToHtmlLanguageGrp(nodes, context)
{
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes.item(i);

        result += '<DIV class="languageGrp">';
        result += '<SPAN class="fakeLanguageGrp">';
        result += XmlToHtmlLanguage(node.selectSingleNode('language'), context);
        result += '</SPAN>\n';

        result += XmlToHtmlDescripGrp(node.selectNodes('descripGrp'), context);
        result += XmlToHtmlSourceGrp(node.selectNodes('sourceGrp'), context);
        result += XmlToHtmlNoteGrp(node.selectNodes('noteGrp'), context);

        result += XmlToHtmlTermGrp(node.selectNodes('termGrp'), context);

        result += '</DIV>\n';
    }

    return result;
}

function XmlToHtmlLanguage(node, context)
{
    var result = "";

    result += '<SPAN class="languagelabel">Language</SPAN> ';

    result += '<SPAN class="language" unselectable="on" locale="';
    result += node.selectSingleNode('@locale').text;
    result += '">';

    result += node.selectSingleNode('@name').text;

    result += '</SPAN>\n';

    return result;
}

function XmlToHtmlTermGrp(nodes, context)
{
    var result = "";

    for (var i = 0; i < nodes.length; i++)
    {
        var node = nodes.item(i);
        var isFirst = (i == 0);

        result += '<DIV class="termGrp">';
        result += '<DIV class="fakeTermGrp" ondblclick="doEdit(this);">';
        result += XmlToHtmlTerm(node.selectSingleNode('term'), isFirst, context);
        result += '</DIV>\n';

        result += XmlToHtmlDescripGrp(node.selectNodes('descripGrp'), context);
        result += XmlToHtmlSourceGrp(node.selectNodes('sourceGrp'), context);
        result += XmlToHtmlNoteGrp(node.selectNodes('noteGrp'), context);

        result += '</DIV>\n';
    }

    return result;
}

function XmlToHtmlTerm(node, isFirst, context)
{
    var result = '<SPAN class="termlabel">';
    result += context.mapTerm(isFirst);
    result += '</SPAN> ';

    result += '<SPAN class="term">' + node.text + '</SPAN>\n';

    return result;
}

//
// HTML fragment constructors
//

function getLanguageGrpDiv(language, locale, term)
{
    var result =
        '<DIV class="languageGrp"><SPAN class="fakeLanguageGrp"><SPAN class="languagelabel">Language</SPAN><SPAN class="language" unselectable="on" locale="' + locale + '">' + language + '</SPAN></SPAN>' + getTermGrpDiv(term, true) + '</DIV>';

    return result;
}

function getTermGrpDiv(term, isFirst)
{
    var result = '<DIV class="termGrp"><DIV class="fakeTermGrp" ondblclick="doEdit(this);"><SPAN class="termlabel">' + (isFirst ? 'Main Term' : 'Synonym') + '</SPAN><SPAN class="term">' + term + '</SPAN></DIV></DIV>';

    return result;
}

function getFieldGrpDiv(name, type, value)
{
    var result = '<DIV CLASS="fieldGrp" ondblclick="doEdit(this);"><SPAN CLASS="fieldlabel" unselectable="on" type="' + type + '">' + name + ' </SPAN><SPAN CLASS="fieldvalue">' + value + '</SPAN></DIV>';

    return result;
}
