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
package com.globalsight.everest.page.pageimport;

import java.io.IOException;
import java.io.ObjectOutputStream;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * TemplateFormat is a Java data object to map the 'template_format' table in
 * database.
 */
public class TemplateFormat extends PersistentObject
{
    private static final long serialVersionUID = 1713744736501145738L;

    private String m_name;
    private String m_templateType;
    private String m_sourceType;
    private String m_text;

    public TemplateFormat()
    {
    }

    public TemplateFormat(String p_name, String p_tmplType, String p_srcType,
            String p_text)
    {
        m_name = p_name;
        m_templateType = p_tmplType;
        m_sourceType = p_srcType;
        m_text = p_text;
    }

    protected void writeObject(ObjectOutputStream p_oos) throws IOException
    {
    }

    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    public String getTemplateType()
    {
        return m_templateType;
    }

    public void setTemplateType(String templateType)
    {
        m_templateType = templateType;
    }

    public String getSourceType()
    {
        return m_sourceType;
    }

    public void setSourceType(String sourceType)
    {
        m_sourceType = sourceType;
    }

    public String getText()
    {
        return m_text;
    }

    public void setText(String text)
    {
        m_text = text;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object paramObject)
    {
        return super.equals(paramObject);
    }
}
