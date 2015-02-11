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
package com.globalsight.everest.tm.searchreplace;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.Text;

import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.TextNode;

import java.util.Iterator;
import java.util.Locale;

public class GxmlElementSubstringReplace
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            GxmlElementSubstringReplace.class);

    private String m_old;
    private String m_new;
    private boolean m_caseSensitiveSearch;
    private Locale m_locale;
    private boolean m_didReplace;

    public GxmlElementSubstringReplace(String p_old, String p_new,
        boolean p_caseSensitiveSearch, Locale p_locale)
    {
        m_old = p_old;
        m_new = p_new;
        m_caseSensitiveSearch = p_caseSensitiveSearch;
        m_locale = p_locale;
        m_didReplace = false;
    }

    public boolean replace(GxmlElement p_gxmlElement)
    {
        m_didReplace = false;

        walkGxmlElement(p_gxmlElement);

        return m_didReplace;
    }

    private void walkGxmlElement(GxmlElement p_gxmlElement)
    {
        if (p_gxmlElement == null)
        {
            return;
        }

        if (p_gxmlElement.getType() == GxmlElement.TEXT_NODE)
        {
            replaceSubstringInTextNode((TextNode)p_gxmlElement);
        }
        else
        {
            Iterator it = p_gxmlElement.getChildElements().iterator();
            while (it.hasNext())
            {
                walkGxmlElement((GxmlElement)it.next());
            }
        }
    }

    private void replaceSubstringInTextNode(TextNode p_gxmlTextNode)
    {
        String currentText = p_gxmlTextNode.getTextValue();
        String replaced = null;

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Replacing " + m_old + " with " + m_new +
                " in `" + currentText + "'");
        }

        if (m_caseSensitiveSearch)
        {
            replaced = Text.replaceString(currentText, m_old, m_new);
        }
        else
        {
            replaced = Text.replaceStringIgnoreCase(currentText,
                m_old, m_new, m_locale);
        }

        if (!replaced.equals(currentText))
        {
            m_didReplace = true;
            p_gxmlTextNode.setTextBuffer(new StringBuffer(replaced));
        }
    }
}
