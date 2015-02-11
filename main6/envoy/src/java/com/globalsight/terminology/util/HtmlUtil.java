/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.terminology.util;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.terminology.EntryUtils;

/**
 * This class mirrors the XmlToHtml conversions in the Termbase Viewer's
 * Javascript code (terminology/viewer/entry.js) to format an entry or entry
 * fragment as HTML. The MappingContext provides display strings based on the
 * database definition to the UI.
 */
public class HtmlUtil
{
    /**
     * Maps an XML entry to HTML. Internal field types are mapped to display
     * names using a context object. See management/objects_js.jsp.
     */
    static public String xmlToHtml(Element p_root, MappingContext p_context)
    {
        String root = p_root.getName();

        if (root.equals("conceptGrp"))
        {
            return xmlToHtmlConceptGrp(p_root, p_context);
        }

        ArrayList rootlist = new ArrayList();
        rootlist.add(p_root);

        if (root.equals("languageGrp"))
        {
            return xmlToHtmlLanguageGrp(rootlist, p_context);
        }
        else if (root.equals("termGrp"))
        {
            return xmlToHtmlTermGrp(rootlist, p_context);
        }

        return "<div style='background-color:red'>unknown fragment</div>";
    }

    static public String xmlToHtmlConceptGrp(Element p_node,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer(
                "<DIV class=\"vconceptGrp\"><SPAN class=\"vfakeConceptGrp\">");

        Element temp = (Element) p_node.selectSingleNode("concept");
        String id = temp != null ? temp.getText() : null;

        if (id != null && Integer.parseInt(id) > 0)
        {
            result.append("<SPAN class=\"vconceptlabel\">");
            result.append(p_context.mapEntry());
            result.append("</SPAN><SPAN class=\"vconcept\">" + id + "</SPAN>");
        }
        else
        {
            result.append("<SPAN class=\"vconceptlabel\">");
            result.append(p_context.mapNewEntry());
            result.append("</SPAN><SPAN class=\"vconcept\"></SPAN>");
        }

        result.append("</SPAN>");

        result.append(xmlToHtmlTransacGrp(p_node.selectNodes("transacGrp"),
                p_context));
        result.append(xmlToHtmlDescripGrp(p_node.selectNodes("descripGrp"),
                p_context));
        result.append(xmlToHtmlSourceGrp(p_node.selectNodes("sourceGrp"),
                p_context));
        result.append(xmlToHtmlNoteGrp(p_node.selectNodes("noteGrp"), p_context));
        result.append(xmlToHtmlLanguageGrp(p_node.selectNodes("languageGrp"),
                p_context));

        result.append("</DIV>");

        return result.toString();
    }

    static public String xmlToHtmlTransacGrp(List p_nodes,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nodes.size(); i < max; i++)
        {
            Element node = (Element) p_nodes.get(i);

            result.append("<SPAN class=\"vtransacGrp\" type=\"");
            result.append(node.selectSingleNode("transac/@type").getText());
            result.append("\" author=\"");
            result.append(node.selectSingleNode("transac").getText());
            result.append("\" date=\"");
            result.append(node.selectSingleNode("date").getText());
            result.append("\">");

            result.append("<SPAN CLASS=\"vtransaclabel\">");
            result.append(p_context.mapTransac(node.selectSingleNode(
                    "transac/@type").getText()));
            result.append("</SPAN>");

            result.append("<SPAN CLASS=\"vtransacvalue\">");
            result.append(node.selectSingleNode("date").getText());
            result.append(" (");
            result.append(UserUtil.getUserNameById(node.selectSingleNode(
                    "transac").getText()));
            result.append(")");
            result.append("</SPAN>");

            result.append("</SPAN>");
        }

        return result.toString();
    }

    static public String xmlToHtmlNoteGrp(List p_nodes, MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nodes.size(); i < max; i++)
        {
            Element node = (Element) p_nodes.get(i);

            result.append("<DIV class=\"vfieldGrp\">");

            result.append(xmlToHtmlNote(
                    (Element) node.selectSingleNode("note"), p_context));

            result.append("</DIV>");
        }

        return result.toString();
    }

    static public String xmlToHtmlNote(Element p_node, MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        result.append("<SPAN class=\"vfieldlabel\" unselectable=\"on\" "
                + "type=\"note\">");
        result.append(p_context.mapNote("note"));
        result.append("</SPAN>");

        result.append("<SPAN class=\"vfieldvalue\">");
        result.append(EntryUtils.getInnerHtml(p_node));
        result.append("</SPAN>");

        return result.toString();
    }

    static public String xmlToHtmlSourceGrp(List p_nodes,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nodes.size(); i < max; i++)
        {
            Element node = (Element) p_nodes.get(i);

            result.append("<DIV class=\"vfieldGrp\">");

            result.append(xmlToHtmlSource(
                    (Element) node.selectSingleNode("source"), p_context));
            result.append(xmlToHtmlNoteGrp(node.selectNodes("noteGrp"),
                    p_context));

            result.append("</DIV>");
        }

        return result.toString();
    }

    static public String xmlToHtmlSource(Element p_node,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        result.append("<SPAN class=\"vfieldlabel\" unselectable=\"on\" "
                + "type=\"source\">");
        result.append(p_context.mapSource("source"));
        result.append("</SPAN>");

        result.append("<SPAN class=\"vfieldvalue\">");
        result.append(EntryUtils.getInnerHtml(p_node));
        result.append("</SPAN>");

        return result.toString();
    }

    static public String xmlToHtmlDescripGrp(List p_nodes,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nodes.size(); i < max; i++)
        {
            Element node = (Element) p_nodes.get(i);

            result.append("<DIV class=\"vfieldGrp\">");

            result.append(xmlToHtmlDescrip(
                    (Element) node.selectSingleNode("descrip"), p_context));
            result.append(xmlToHtmlSourceGrp(node.selectNodes("sourceGrp"),
                    p_context));
            result.append(xmlToHtmlNoteGrp(node.selectNodes("noteGrp"),
                    p_context));

            result.append("</DIV>");
        }

        return result.toString();
    }

    static public String xmlToHtmlDescrip(Element p_node,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        result.append("<SPAN class=\"vfieldlabel\" unselectable=\"on\" "
                + "type=\"");
        result.append(p_node.selectSingleNode("@type").getText());
        result.append("\">");
        result.append(p_context.mapDescrip(p_node.selectSingleNode("@type")
                .getText()));
        result.append("</SPAN>");

        result.append("<SPAN class=\"vfieldvalue\">");
        result.append(EntryUtils.getInnerHtml(p_node));
        result.append("</SPAN>");

        return result.toString();
    }

    static public String xmlToHtmlLanguageGrp(List p_nodes,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nodes.size(); i < max; i++)
        {
            Element node = (Element) p_nodes.get(i);

            result.append("<DIV class=\"vlanguageGrp\">");
            result.append("<SPAN class=\"vfakeLanguageGrp\">");
            result.append(xmlToHtmlLanguage(
                    (Element) node.selectSingleNode("language"), p_context));
            result.append("</SPAN>");

            result.append(xmlToHtmlDescripGrp(node.selectNodes("descripGrp"),
                    p_context));
            result.append(xmlToHtmlSourceGrp(node.selectNodes("sourceGrp"),
                    p_context));
            result.append(xmlToHtmlNoteGrp(node.selectNodes("noteGrp"),
                    p_context));

            result.append(xmlToHtmlTermGrp(
                    node.selectNodes("termGrp[term/@search-term]"), p_context));
            result.append(xmlToHtmlTermGrp(
                    node.selectNodes("termGrp[not(term/@search-term)]"),
                    p_context));

            result.append("</DIV>");
        }

        return result.toString();
    }

    static public String xmlToHtmlLanguage(Element p_node,
            MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        result.append("<SPAN class=\"vlanguagelabel\">Language</SPAN>");

        result.append("<SPAN class=\"vlanguage\" unselectable=\"on\" "
                + "locale=\"");
        result.append(p_node.selectSingleNode("@locale").getText());
        result.append("\">");

        result.append(p_node.selectSingleNode("@name").getText());

        result.append("</SPAN>");

        return result.toString();
    }

    static public String xmlToHtmlTermGrp(List p_nodes, MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_nodes.size(); i < max; i++)
        {
            Element node = (Element) p_nodes.get(i);
            boolean isFirst = (i == 0);

            result.append("<DIV class=\"vtermGrp\">");
            result.append("<DIV class=\"vfakeTermGrp\">");
            result.append(xmlToHtmlTerm(
                    (Element) node.selectSingleNode("term"), p_context));
            result.append("</DIV>");

            result.append(xmlToHtmlDescripGrp(node.selectNodes("descripGrp"),
                    p_context));
            result.append(xmlToHtmlSourceGrp(node.selectNodes("sourceGrp"),
                    p_context));
            result.append(xmlToHtmlNoteGrp(node.selectNodes("noteGrp"),
                    p_context));

            result.append("</DIV>");
        }

        return result.toString();
    }

    static public String xmlToHtmlTerm(Element p_node, MappingContext p_context)
    {
        StringBuffer result = new StringBuffer();

        result.append("<SPAN class=\"vtermlabel\">");
        result.append(p_context.mapTerm(null));
        result.append("</SPAN>");

        result.append("<SPAN class=\"vterm\">");
        result.append(p_node.getText());
        result.append("</SPAN>");

        return result.toString();
    }
}
