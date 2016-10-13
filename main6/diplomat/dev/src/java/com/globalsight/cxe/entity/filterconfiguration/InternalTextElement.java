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

package com.globalsight.cxe.entity.filterconfiguration;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.globalsight.ling.docproc.extractor.xml.GsDOMParser;
import com.globalsight.util.XmlTransformer;

public class InternalTextElement
{
    public static int TYPE_TEXT = 0;
    public static int TYPE_TAG = 1;

    private int elementType = TYPE_TEXT;
    private String content = null;
    private String tagName = null;
    private boolean isInternalText = false;
    private boolean isIntlTagText = false;

    public InternalTextElement(int eleType, String content)
    {
        this.elementType = eleType;
        this.content = content;
    }

    public int getElementType()
    {
        return elementType;
    }

    public void setElementType(int elementType)
    {
        this.elementType = elementType;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public boolean isInternalText()
    {
        return isInternalText;
    }

    public void setInternalText(boolean isInternalText)
    {
        this.isInternalText = isInternalText;
    }

    public boolean isInternalTagText()
    {
        return isIntlTagText;
    }

    public void setInternalTagText(boolean isIntlTagText)
    {
        this.isIntlTagText = isIntlTagText;
    }

    public boolean doInternalText()
    {
        if (this.elementType == TYPE_TAG)
        {
            return false;
        }

        if (this.isInternalText)
        {
            return false;
        }

        if (this.isIntlTagText)
        {
            return false;
        }

        if (this.content == null || this.content.length() == 0)
        {
            return false;
        }

        return true;
    }

    public boolean isTag()
    {
        return this.elementType == TYPE_TAG;
    }

    public String toString()
    {
        return content;
    }

    public static List<InternalTextElement> parse(String oriStr)
            throws Exception
    {
        List<InternalTextElement> result = new ArrayList<InternalTextElement>();
        boolean isInternalTagText = false;
        int deep = 0;

        String xml = "<gsroot>" + oriStr + "</gsroot>";
        GsDOMParser p = new GsDOMParser();
        InputSource is = new InputSource(new StringReader(xml));
        Document wdoc = p.parse(is);
        NodeList nodes = wdoc.getDocumentElement().getChildNodes();

        if (nodes == null)
        {
            result.add(InternalTextElement.newText(oriStr, false));
        }
        else
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                String nodexml = XmlTransformer.convertNodeToString(node, "UTF-8");

                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element ele = (Element) node;
                    if (ele.getNodeName().equals("bpt")
                            && nodexml.endsWith("/>"))
                    {
                        nodexml = nodexml.substring(0, nodexml.length() - 2)
                                + "></bpt>";
                    }
                    else if (ele.getNodeName().equals("ept")
                            && nodexml.endsWith("/>"))
                    {
                        nodexml = nodexml.substring(0, nodexml.length() - 2)
                                + "></ept>";
                    }
                    result.add(InternalTextElement.newTag(nodexml));

                    // check if it is internal tag
                    if (ele.getNodeName().equals("bpt")
                            && nodexml.contains("internal=\"yes\""))
                    {
                        isInternalTagText = true;
                        deep++;
                    }
                    else if (isInternalTagText
                            && ele.getNodeName().equals("bpt"))
                    {
                        deep++;
                    }
                    else if (isInternalTagText
                            && ele.getNodeName().equals("ept"))
                    {
                        deep--;

                        if (deep == 0)
                        {
                            isInternalTagText = false;
                        }
                    }
                }
                else if (node.getNodeType() == Node.TEXT_NODE)
                {
                    InternalTextElement ite = InternalTextElement.newText(
                            nodexml, false);
                    ite.setInternalTagText(isInternalTagText);
                    result.add(ite);
                }
                else
                {
                    result.add(InternalTextElement.newTag(nodexml));
                }
            }
        }

        return result;
    }

    public static InternalTextElement newText(String content,
            boolean isInternalText)
    {
        InternalTextElement ee = new InternalTextElement(TYPE_TEXT, content);
        ee.setInternalText(isInternalText);
        return ee;
    }

    public static InternalTextElement newTag(String content)
    {
        InternalTextElement ee = new InternalTextElement(TYPE_TAG, content);
        return ee;
    }
}
