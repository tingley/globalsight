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
package com.globalsight.util.gxml;

import org.xml.sax.Attributes;

/**
 * <p>A helper class which hold some static methods used by GxmlReader
 * and PrsReader to parse Gxml and PRS using SAX.</p>
 *
 * @author Bethany Wang
 */
/*
 * MODIFIED    MM/DD/YYYY
 * BWang       3/30/2001   Initial version.
 */
public final class GxmlSaxHelper
{
    static final int START_TAG_STRING_BUFFER_LENGTH = 100;
    static final int END_TAG_STRING_BUFFER_LENGTH = 16;

    /**
     * Fill attributes for a any element
     */
    static void fillAttributes(GxmlElement p_element, Attributes p_attrs)
    {
        int length = p_attrs.getLength();

        for (int i = 0; i < length; i++)
        {
            String name = p_attrs.getQName(i);
            p_element.setAttribute(name, p_attrs.getValue(name));
        }
    }

    /**
     * Convert a XML starting tag to a String
     */
    static String convertStartTagToString(String p_localName,
        Attributes p_attrs)
    {
        StringBuffer output = new StringBuffer(
            START_TAG_STRING_BUFFER_LENGTH);

        output.append("<");
        output.append(p_localName);

        if (p_attrs != null)
        {
            int size = p_attrs.getLength();
            for (int i = 0; i < size; i++)
            {
                output.append(" ");
                output.append(p_attrs.getQName(i));
                output.append("=\"");
                output.append(p_attrs.getValue(i));
                output.append("\"");
            }
        }

        output.append(">");

        return output.toString();
    }

    /**
     * Convert a XML end tag to a String
     */
    static String convertEndTagToString(String p_localName)
    {
        return "</" + p_localName + ">";
    }
}
