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

package com.globalsight.everest.webapp.pagehandler;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;

/**
 * Factory class for all PageHandler objects. This class must import
 * all the packages where PageHandlers are defined.
 */
public class PageHandlerFactory
{
    private static final Logger CATEGORY =
        Logger.getLogger(
          PageHandlerFactory.class.getName());

    // Singleton instance
    // (Don't call instance, call the static method below.)
    static private PageHandlerFactory instance = new PageHandlerFactory();
    static private Map m_pageHandlers = new HashMap(50); 

    /**
     * This class used to be a singleton but is not anymore. Use the
     * static method {@link #getPageHandlerInstance(String)
     * getPageHandlerInstance()}.
     *
     * @deprecated
     */
    public static PageHandlerFactory instance()
    {
        return instance;
    }

    /**
     * Creates an instance of a page handler given its class name.
     *
     * @param thePageHandlerClassName the fully qualified class name
     * of a page handler
     *
     * @return an instance of a page handler if one is found, null
     * otherwise
     */
    static public PageHandler getPageHandlerInstance(String thePageHandlerClassName)
        throws EnvoyServletException
    {
        PageHandler thePageHandler = 
                (PageHandler)m_pageHandlers.get(thePageHandlerClassName);
        if (thePageHandler == null)
        {
            try
            {
                thePageHandler =
                (PageHandler)Class.forName(
                        thePageHandlerClassName).newInstance();
                        m_pageHandlers.put(thePageHandlerClassName,
                        thePageHandler); 
            }
            catch (Exception e) 
            {
                CATEGORY.error(e.getMessage(), e);
                throw new EnvoyServletException(e);
	    }
        } 
        return thePageHandler;
    }
}
