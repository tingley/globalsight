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
package com.globalsight.ling.tm2;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.servlet.util.ServerProxy;

import java.util.Hashtable;

/**
 * GlobalSightLocalePool class miantains a cache of GlobalSightLocale
 * objects. Tm modules use GlobalSightLocale a lot. The cache will
 * speed up the process.
 */

public class GlobalSightLocalePool
{
    // Cache of locale id and GlobalSightLocale map
    private static Hashtable s_localeCache = new Hashtable();

    static public GlobalSightLocale getLocaleById(long p_localeId)
        throws Exception
    {
        Long localeId = new Long(p_localeId);
        GlobalSightLocale loc = (GlobalSightLocale)s_localeCache.get(localeId);

        if (loc == null)
        {
            // query LocaleManager
            loc = ServerProxy.getLocaleManager().getLocaleById(p_localeId);
            s_localeCache.put(localeId, loc);
        }

        return loc;
    }

}
