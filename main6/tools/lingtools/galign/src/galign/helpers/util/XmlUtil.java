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
package galign.helpers.util;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

public class XmlUtil
{
    /**
     * Returns the inner text like Element.getText() but for all
     * embedded text nodes.
     */
    static public String getInnerText(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(node.getText());
            }
            else if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                result.append(getInnerText((Element)node));
            }
        }

        return result.toString();
    }

    /**
     * Returns the XML representation like Element.asXML() but without
     * the top-level tag.
     */
    static public String getInnerXml(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            // Work around a specific behaviour of DOM4J text nodes:
            // The text node asXML() returns the plain Unicode string,
            // so we need to encode entities manually.
            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(EditUtil.encodeXmlEntities(node.getText()));
            }
            else
            {
                // Element nodes write their text nodes correctly.
                result.append(node.asXML());
            }
        }

        return result.toString();
    }
}
