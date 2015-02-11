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
package com.globalsight.ling.docproc;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Properties;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.common.XmlEntities;


public class GsaStartElement
    implements DocumentElement
{
    private XmlEntities m_escaper = null;

    private boolean m_empty = false;
    private boolean m_delete = false;
    private String m_extract = null;
    private String m_description = null;
    private String m_locale = null;
    private String m_add = null;
    private String m_added = null;
    private String m_deleted = null;
    private String m_snippetName = null;
    private String m_snippetId = null;

    public GsaStartElement(boolean p_empty)
    {
        super();

        m_empty = p_empty;

        m_escaper = new XmlEntities();
    }

    public void toDiplomatString(DiplomatAttribute diplomatAttribute,
        XmlWriter writer)
    {
        Properties attribs = new Properties();

        if (m_extract != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.EXTRACT,
                m_escaper.encodeStringBasic(m_extract));
        }

        if (m_description != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.DESCRIPTION,
                m_escaper.encodeStringBasic(m_description));
        }

        if (m_locale != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.LOCALE,
                m_escaper.encodeStringBasic(m_locale));
        }

        if (m_add != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.ADD,
                m_escaper.encodeStringBasic(m_add));
        }

        if (m_delete)
        {
            attribs.setProperty(DiplomatNames.Attribute.DELETE, "yes");
        }

        if (m_added != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.ADDED,
                m_escaper.encodeStringBasic(m_added));
        }

        if (m_deleted != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.DELETED,
                m_escaper.encodeStringBasic(m_deleted));
        }

        if (m_snippetName != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.NAME,
                m_escaper.encodeStringBasic(m_snippetName));
        }

        if (m_snippetId != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.ID,
                m_escaper.encodeStringBasic(m_snippetId));
        }

        if (m_empty)
        {
            writer.emptyElement(DiplomatNames.Element.GSA, attribs);
        }
        else
        {
            writer.startElement(DiplomatNames.Element.GSA, attribs);
        }
    }


    public int type()
    {
        return GSA_START;
    }


    public void setExtract(String p_value)
    {
        m_extract = p_value;
    }

    public String getExtract()
    {
        return m_extract;
    }


    public void setDescription(String p_value)
    {
        m_description = p_value;
    }

    public String getDescription()
    {
        return m_description;
    }


    public void setLocale(String p_value)
    {
        m_locale = p_value;
    }

    public String getLocale()
    {
        return m_locale;
    }


    public void setAdd(String p_value)
    {
        m_add = p_value;
    }

    public String getAdd()
    {
        return m_add;
    }


    public void setDeletable()
    {
        m_delete = true;
    }

    public boolean isDeletable()
    {
        return m_delete;
    }


    public void setAdded(String p_value)
    {
        m_added = p_value;
    }

    public String getAdded()
    {
        return m_added;
    }


    public void setDeleted(String p_value)
    {
        m_deleted = p_value;
    }

    public String getDeleted()
    {
        return m_deleted;
    }


    public void setSnippetName(String p_value)
    {
        m_snippetName = p_value;
    }

    public String getSnippetName()
    {
        return m_snippetName;
    }


    public void setSnippetId(String p_value)
    {
        m_snippetId = p_value;
    }

    public String getSnippetId()
    {
        return m_snippetId;
    }


    public void setEmpty()
    {
        m_empty = true;
    }

    public boolean getEmpty()
    {
        return m_empty;
    }


    /**
     * Validates the correctness of the GS attributes. Returns true if
     * the attributes are ok, else false.
     */
    public boolean validate()
    {
        // ADD Positions: <GS ADD=x />
        if (m_add != null && m_add.length() > 0)
        {
            if (m_added != null || m_delete || m_deleted != null ||
                m_snippetName != null || m_snippetId != null ||
                m_extract != null || m_description != null ||
                m_locale != null)
            {
                return false;
            }

            return true;
        }
        // ADDED Snippet <GS ADDED=loc NAME=x [ID=id] />
        else if (m_added != null && m_added.length() > 0)
        {
            if (m_add != null || m_delete || m_deleted != null ||
                m_snippetName == null || m_extract != null ||
                m_description != null || m_locale != null)
            {
                return false;
            }

            return true;
        }
        else if (m_delete)
        {
            // Deletable content <GS DELETE[=x] [DELETED=loc]>...</GS>
            if (m_add != null || m_added != null || m_snippetName != null ||
                m_snippetId != null || m_extract != null ||
                m_description != null || m_locale != null)
            {
                return false;
            }

            return true;
        }
        else if (m_extract != null && m_extract.length() > 0)
        {
            // Snippets <GS EXTRACT=x [LOCALE=x] [DESCRIPTION=x]>...</GS>
            if (m_add != null || m_added != null || m_delete ||
                m_deleted != null || m_snippetName != null ||
                m_snippetId != null)
            {
                return false;
            }

            return true;
        }

        return false;
    }

    /** Print routine for GS-tagged source pages. GS tags print themselves. */
    public String getText()
    {
        return toString();
    }

    public String toString()
    {
        StringBuffer res = new StringBuffer();

        res.append("<");
        res.append(DiplomatNames.Element.GSA);

        if (m_delete)
        {
            res.append(" ");
            res.append(DiplomatNames.Attribute.DELETE);
            res.append("=\"yes\"");
        }

        appendAttribute(res, DiplomatNames.Attribute.EXTRACT, m_extract);
        appendAttribute(res, DiplomatNames.Attribute.DESCRIPTION, m_description);
        appendAttribute(res, DiplomatNames.Attribute.LOCALE, m_locale);
        appendAttribute(res, DiplomatNames.Attribute.ADD, m_add);
        appendAttribute(res, DiplomatNames.Attribute.ADDED, m_added);
        appendAttribute(res, DiplomatNames.Attribute.DELETED, m_deleted);
        appendAttribute(res, DiplomatNames.Attribute.NAME, m_snippetName);
        appendAttribute(res, DiplomatNames.Attribute.ID, m_snippetId);

        if (m_empty)
        {
            res.append("/>");
        }
        else
        {
            res.append(">");
        }

        return res.toString();
    }

    private void appendAttribute(StringBuffer p_result,
        String p_attr, String p_value)
    {
        if (p_value != null)
        {
            p_result.append(" ");
            p_result.append(p_attr);
            p_result.append("=\"");
            p_result.append(m_escaper.encodeStringBasic(p_value));
            p_result.append("\"");
        }
    }
}
