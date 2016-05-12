/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.restful;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;

import com.globalsight.util.PropertiesFactory;
import com.globalsight.util.StringUtil;

@ApplicationPath("/restfulServices")
public class RestApplicationConfig extends Application implements RestConstants
{
    private static final Logger logger = Logger.getLogger(RestApplicationConfig.class);

    private Set<Object> singletons = new HashSet<Object>();

    private static Properties resourceProperties = null;
    private static List<String> resources = new ArrayList<String>();

    // All implemented resources are registered here...
    static
    {
        loadResourceProperties();

        // If no "RestResources.properties", use this.
        // version 1.0 resources. 
        resources.add("com.globalsight.restful.version1_0.tm.TmResource");
        resources.add("com.globalsight.restful.version1_0.tmprofile.TmProfileResource");
    }

    // Register your resource classes here
    public RestApplicationConfig()
    {
        List<String> classNames = getResourceClasses(resourceProperties);
        if (classNames == null || classNames.size() == 0)
        {
            classNames = resources;
        }

        for (String className : classNames)
        {
            try
            {
                singletons.add(Class.forName(className).newInstance());
            }
            catch (Exception e)
            {
                logger.warn("Fail to register restful resource: " + className + ": "
                        + e.getMessage());
            }
        }
    }

    @Override
    public Set<Object> getSingletons()
    {
        return singletons;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> getResourceClasses(Properties p)
    {
        List<String> classNames = new ArrayList<String>();
        if (p != null)
        {
            Iterator valueItr = p.values().iterator();
            while (valueItr.hasNext())
            {
                String resources = (String) valueItr.next();
                if (StringUtil.isNotEmpty(resources))
                {
                    for (String className : resources.split(","))
                    {
                        if (StringUtil.isNotEmpty(className))
                        {
                            classNames.add(className.trim());                            
                        }
                    }
                }
            }
        }
        return classNames;
    }

    private static void loadResourceProperties()
    {
        try
        {
            resourceProperties = (new PropertiesFactory())
                    .getProperties("/properties/RestResources.properties");
        }
        catch (Exception e)
        {
            logger.warn(e.getMessage());
        }
    }
}
