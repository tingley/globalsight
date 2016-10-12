/**
 *  Copyright 2016 Welocalize, Inc. 
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
public class MyEmailResourceBundle extends ResourceBundle
{
    // Base ResourceBuncle
    private ResourceBundle baseResources;
    // Custom ResourceBundle
    private ResourceBundle myEmailResources;

    MyEmailResourceBundle(String baseName, Locale locale, String companyName)
    {
        baseResources = ResourceBundle.getBundle(baseName, locale);
        try
        {
            myEmailResources = ResourceBundle.getBundle(baseName + "_" + companyName, locale);
        }
        catch (MissingResourceException e)
        {
            // Do nothing for missing resource.
        }
    }

    @Override
    protected Object handleGetObject(String key)
    {
        if (myEmailResources != null && myEmailResources.containsKey(key))
            return myEmailResources.getObject(key);
        return baseResources.getObject(key);
    }

    @Override
    public Enumeration<String> getKeys()
    {
        return baseResources.getKeys();
    }

}
