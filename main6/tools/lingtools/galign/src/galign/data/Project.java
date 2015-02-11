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

package galign.data;

import galign.helpers.AlignmentPackage;
import galign.helpers.util.LocaleUtil;

import java.util.Locale;

/**
 * A Project object holds the name/path from where a project was
 * loaded, source &amp; target locales, and a reference to the
 * AlignmentPackage (GAP) descriptor.
 */
public class Project
{
    //
    // Members
    //
    private String m_name;
    private String m_path;

    private Locale m_sourceLocale = null;
    private Locale m_targetLocale = null;

    private AlignmentPackage m_package;

    //
    // Constructor
    //
    public Project()
    {
    }

    public Project(String p_path, String p_name, AlignmentPackage p_package)
    {
        m_path = p_path;
        m_name = p_name;
        m_package = p_package;
    }

    //
    // Public Methods
    //

    public String getName()
    {
        return m_name;
    }

    public void setName(String p_arg)
    {
        m_name = p_arg;
    }

    public String getPath()
    {
        return m_path;
    }

    public void setPath(String p_arg)
    {
        m_path = p_arg;
    }

    public Locale getSourceLocale()
    {
        if (m_sourceLocale == null)
        {
            m_sourceLocale =
                LocaleUtil.makeLocale(m_package.getSourceLocale());
        }

        return m_sourceLocale;
    }

    public Locale getTargetLocale()
    {
        if (m_targetLocale == null)
        {
            m_targetLocale =
                LocaleUtil.makeLocale(m_package.getTargetLocale());
        }

        return m_targetLocale;
    }

    public AlignmentPackage getAlignmentPackage()
    {
        return m_package;
    }

    public void setAlignmentPackage(AlignmentPackage p_arg)
    {
        m_package = p_arg;
    }
}
