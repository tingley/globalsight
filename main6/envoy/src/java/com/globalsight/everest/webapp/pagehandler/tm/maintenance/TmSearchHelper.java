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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.ling.common.Text;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.util.comparator.LocaleComparator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * TmSearchHelper .
 */
public class TmSearchHelper
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            TmSearchHelper.class);

    // Static class, private constructor
    private TmSearchHelper()
    {
    }

//      /**
//       * Retrieve an object from the session manager.
//       */
//      public static Object retrieveObject(HttpSession p_httpSession,
//          String p_key)
//      {
//          SessionManager sessionMgr = (SessionManager)p_httpSession.
//              getAttribute(WebAppConstants.SESSION_MANAGER);

//          // -- print a list of objects within session manager
//          // System.out.println("SessionManager values: " + sessionMgr.toString());

//          return sessionMgr.getAttribute(p_key);
//      }

//      /**
//       * Store the object in the session manager.
//       */
//      public static void storeObject(HttpSession p_httpSession,
//          String  p_key, Object p_object)
//      {
//          SessionManager sessionMgr = (SessionManager)p_httpSession.
//              getAttribute(WebAppConstants.SESSION_MANAGER);
//          sessionMgr.setAttribute(p_key, p_object);

//          //-- print a list of objects within session manager
//          //System.out.println("SessionManager values: "+sessionMgr.toString());
//      }

    /**
     * Get all the available locales (GlobalSightLocale) supported by
     * the system.
     * @exception EnvoyServletException Component related exception.
     */
    public static Collection getSupportedLocales(Locale p_uiLocale)
        throws EnvoyServletException
    {
        try
        {
            List locales =
                (List)ServerProxy.getLocaleManager().getAvailableLocales();
            java.util.Collections.sort(
                locales, new LocaleComparator(2, p_uiLocale));

            return locales;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a GlobalSightLocale from the system.
     * <p>
     * @exception EnvoyServletException Component related exception.
     */
    public static GlobalSightLocale getLocaleById(Long p_id)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocaleById(p_id.longValue());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }
}

