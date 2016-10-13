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

package com.globalsight.terminology.searchreplace;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

public abstract class TbMaintance implements ITermbaseMaintance
{
    protected SearchReplaceParams rp;
    protected Termbase m_termbase;

    public TbMaintance(SearchReplaceParams rp, Termbase m_termbase)
    {
        this.rp = rp;
        this.m_termbase = m_termbase;
    }

    protected void searchField(ArrayList list, String xml, long conceptId,
            long levelId)
    {
        xml = "<root>" + xml + "</root>";

        if (!quickMatch(xml))
        {
            return;
        }

        Document dom = parseXml(xml);
        searchNode(dom.getRootElement(), list, conceptId, levelId);
    }

    private void searchNode(Element node, ArrayList array, long conceptId,
            long levelId)
    {
        String field = node.getText();
        String searchText = rp.getSearchText();
        String fieldType = rp.getSearchType();
        
        if (!field.isEmpty())
        {
            //String name = node.getName();
            boolean flag = false;

            if (rp.isCaseInsensitive())
            {
                if (rp.isWholeWord())
                {
                    if (field.equals(searchText))
                    {
                        flag = true;
                    }
                }
                else
                {
                    if (field.indexOf(searchText) > -1)
                    {
                        flag = true;
                    }
                }
            }
            else
            {
                if (rp.isWholeWord())
                {
                    if (field.toLowerCase().equals(searchText.toLowerCase()))
                    {
                        flag = true;
                    }
                }
                else
                {
                    if (field.toLowerCase().indexOf(searchText.toLowerCase()) > -1)
                    {
                        flag = true;
                    }
                }
            }

            if (flag)
            {
                boolean judge = false;
                String typeValue = node.attributeValue("type");
                
                if (typeValue == null)
                {
                    //if attribute type is null, get the node name to judge
                    String nodeName = node.getName();
                    typeValue = nodeName;
                }

                if (fieldType == null || fieldType.trim().equals(""))
                {
                    //if no select field type, select all
                    judge = true;
                }
                else
                {
                    if (typeValue != null)
                    {
                        //if attribute type is not null, judge the type
                        if (typeValue.toLowerCase().trim().indexOf(
                                fieldType.toLowerCase().trim()) > -1)
                        {
                            judge = true;
                        }
                    }
                }

                if (judge)
                {
                    SearchResult p_result = new SearchResult();
                    p_result.setConceptId(conceptId);
                    p_result.setLevelId(levelId);
                    p_result.setFiled(field);
                    p_result.setType(typeValue);
                    array.add(p_result);
                }
            }
        }
        else
        {
            List children = node.elements();

            for (int i = 0, max = children.size(); i < max; i++)
            {
                Element child = (Element) children.get(i);
                searchNode(child, array, conceptId, levelId);
            }
        }
    }

    /**
     * Performs a quick check on the raw XML string if it may contain the search
     * string.
     */
    private boolean quickMatch(String p_xml)
    {
        if (p_xml == null || p_xml.length() == 0)
        {
            return false;
        }

        // Do a quick check on the full XML string (may be incorrect
        // due to embedded HTML tags but helps performance).
        String search = rp.getSearchText();
        search = EditUtil.encodeXmlEntities(search);

        if (rp.isCaseInsensitive())
        {
            if (p_xml.indexOf(search) == -1)
            {
                return false;
            }
        }
        else
        {
            if (p_xml.toLowerCase().indexOf(search.toLowerCase()) == -1)
            {
                return false;
            }
        }

        return true;
    }

    protected String replaceField(String xml, String oldFieldText,
            String replaceText)
    {
        xml = "<root>" + xml + "</root>";
        Document dom = parseXml(xml);
        doNodeVistor(dom.getRootElement(), oldFieldText, replaceText);
        List children = dom.getRootElement().elements();
        String newXml = new String();

        for (int i = 0, max = children.size(); i < max; i++)
        {
            Element child = (Element) children.get(i);
            newXml = newXml + child.asXML();
        }

        return newXml;
    }

    private void doNodeVistor(Element node, String oldFieldText,
            String replaceText)
    {
        String field = node.getText();

        if (!field.isEmpty())
        {
            if (field.indexOf(oldFieldText) > -1)
            {
                node.setText(replaceText);
            }
        }
        else
        {
            List children = node.elements();

            for (int i = 0, max = children.size(); i < max; i++)
            {
                Element child = (Element) children.get(i);
                doNodeVistor(child, oldFieldText, replaceText);
            }
        }
    }

    private Document parseXml(String p_xml) throws TermbaseException
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml(p_xml);
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    protected boolean entryIsLocked(long conceptId)
    {
        return m_termbase.isLocked(conceptId);
    }

}
