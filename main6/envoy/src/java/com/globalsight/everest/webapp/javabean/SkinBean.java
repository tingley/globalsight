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
package com.globalsight.everest.webapp.javabean;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Bean to provide skin values, such as logo, colors, images, and fonts. The
 * skin values is stored in /properties/skin.properties.
 */
public class SkinBean
{
    public static Logger s_category = Logger.getLogger(SkinBean.class);
    private ResourceBundle m_skinProperties = null;

    /**
     * Creates a SkinBean
     */
    public SkinBean()
    {
        try
        {
            m_skinProperties = SystemResourceBundle.getInstance()
                    .getResourceBundle("properties/skin", Locale.ROOT);
        }
        catch (Exception e)
        {
            s_category.error("Could not load skin properties.", e);
        }
    }

    /**
     * Looks up a skin property value from skin.properties and handles any
     * exceptions
     * 
     * @param p_propertyName
     *            skin property to lookup
     * @return property value or "" for non-existant properties
     */
    public String getProperty(String p_propertyName)
    {
        String v = "";
        if (m_skinProperties != null)
        {
            try
            {
                v = m_skinProperties.getString(p_propertyName);
            }
            catch (Exception e)
            {
                s_category.error("Could not get skin property "
                        + p_propertyName, e);
            }
        }
        return v;
    }
}
