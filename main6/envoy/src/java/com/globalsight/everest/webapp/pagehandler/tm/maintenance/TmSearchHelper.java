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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * TmSearchHelper .
 */
public class TmSearchHelper
{
    private static final Logger CATEGORY = Logger
            .getLogger(TmSearchHelper.class);

    // Static class, private constructor
    private TmSearchHelper()
    {
    }

    /**
     * Get all the available locales (GlobalSightLocale) supported by the
     * system.
     * 
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Collection getSupportedLocales(Locale p_uiLocale)
            throws EnvoyServletException
    {
        try
        {
            List locales = (List) ServerProxy.getLocaleManager()
                    .getAvailableLocales();
            SortUtil.sort(locales, new LocaleComparator(2, p_uiLocale));

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
     * 
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static GlobalSightLocale getLocaleById(Long p_id)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocaleById(
                    p_id.longValue());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }
}
