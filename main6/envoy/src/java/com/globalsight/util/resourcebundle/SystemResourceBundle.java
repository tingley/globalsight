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
package com.globalsight.util.resourcebundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * SystemResourceBundle, a singleton class wrapped around java
 * PropertyResourceBundle designed for Envoy to read locale texts from
 * LocaleResource property files. Used for UI, Exception, and Email texts.
 * 
 * @see java.util.ResourceBundle
 */
public class SystemResourceBundle
{
    // singleton instance of this class
    static private SystemResourceBundle m_instance = null;

    // the hash map for looking up the appropriate resource bundle.
    private HashMap m_map;

    // private constructor for singleton.
    private SystemResourceBundle()
    {
        m_map = new HashMap(ResourceBundleConstants.INITIAL_MAP_SIZE);
    }

    /**
     * Get the instance of the resource bundle.
     * 
     * @return SystemResourceBundle
     */
    public static SystemResourceBundle getInstance()
    {
        if (m_instance == null)
        {
            m_instance = new SystemResourceBundle();
        }

        return m_instance;
    }

    /**
     * Get the resource bundle based on the type and locale.
     * 
     * @param p_localeResource
     *            The resource type. Email, UI, or Exception.
     * @param p_locale
     *            The locale to obtain the appropriate resource bundle.
     * @return ResourceBundle
     */
    public ResourceBundle getResourceBundle(String p_localeResource,
            Locale p_locale)
    {
        String key = p_localeResource + "_" + p_locale.toString();
        
        if (!m_map.containsKey(key))
        {
            m_map.put(key, new MyResourceBundle(p_localeResource, p_locale));
        }
        
        return (ResourceBundle) m_map.get(key);
    }

    /**
     * Get the supported locales.
     * 
     * @return A Vector containing the supported locales.
     */
    public Vector getSupportedLocales()
    {
        Locale[] supportedLocales = ResourceBundleConstants.SUPPORTED_LOCALES;
        Vector vSupportedLocales = new Vector();

        for (int i = 0; i < supportedLocales.length; i++)
        {
            vSupportedLocales.addElement(supportedLocales[i]);
        }

        return vSupportedLocales;
    }
}
