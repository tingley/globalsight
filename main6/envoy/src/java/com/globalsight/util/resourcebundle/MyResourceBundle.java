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

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Wrap ResourceBundle For Customization.
 */
public class MyResourceBundle extends ResourceBundle
{
    public static final String CUSTOM_NAME = "_custom";
    // Base ResourceBuncle
    private ResourceBundle baseResources;
    // Custom ResourceBundle
    private ResourceBundle myResources;

    MyResourceBundle(String baseName, Locale locale)
    {
        baseResources = ResourceBundle.getBundle(baseName, locale);
        try
        {
            myResources = ResourceBundle.getBundle(baseName + CUSTOM_NAME, locale);
        }
        catch (MissingResourceException e)
        {
            // Do nothing for missing resource.
        }
    }

    @Override
    protected Object handleGetObject(String key)
    {
        if (myResources != null && myResources.containsKey(key))
            return myResources.getObject(key);
        return baseResources.getObject(key);
    }

    @Override
    public Enumeration<String> getKeys()
    {
        return baseResources.getKeys();
    }

}
