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

package com.globalsight.everest.tm.exporter;

import com.globalsight.exporter.IExportManager;

import com.globalsight.everest.util.system.SystemConfiguration;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.localemgr.LocaleManager;

import java.io.*;
import java.util.*;

import org.hibernate.Session;

public class ExportUtil
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            ExportUtil.class);

    public static String EXPORT_BASE_DIRECTORY = "/";
    static {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc.getStringParameter(
                SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            EXPORT_BASE_DIRECTORY = root + IExportManager.EXPORT_DIRECTORY;

            if (!(EXPORT_BASE_DIRECTORY.endsWith("/") ||
                  EXPORT_BASE_DIRECTORY.endsWith("\\")))
            {
                EXPORT_BASE_DIRECTORY = EXPORT_BASE_DIRECTORY + "/";
            }

            File temp = new File(EXPORT_BASE_DIRECTORY);
            temp.mkdirs();
        }
        catch (Throwable e)
        {
            CATEGORY.error(
                "cannot create directory " + EXPORT_BASE_DIRECTORY, e);
        }
    }

    /** Cache of GlobalSightLocale and locale string and map. */
    static private Hashtable s_locale2name = new Hashtable();

    /** Static class, private constructor. */
    private ExportUtil ()
    {
    }

    //
    // Public Methods
    //

    static public String getExportDirectory()
    {
        return EXPORT_BASE_DIRECTORY;
    }

    /**
     * A cache from GlobalSightLocale to printed locale representation
     * as found in TMX files.
     *
     * @param p_locale: a GlobalSightLocale.
     */
    static public String getLocaleString(GlobalSightLocale p_locale)
    {
        String result = (String)s_locale2name.get(p_locale);

        if (result == null)
        {
            result = p_locale.getLanguageCode() + "-" +
                p_locale.getCountryCode();

            result = result.toUpperCase();

            s_locale2name.put(p_locale, result);
        }

        return result;
    }

    /**
     * Maps a local string (en_US) to a GlobalSightLocale id.
     */
    static public long getLocaleId(String p_locale)
        throws Exception
    {
        LocaleManager mgr = ServerProxy.getLocaleManager();
        GlobalSightLocale locale = mgr.getLocaleByString(p_locale);

        return locale.getId();
    }

    /**
     * Good old getLocale method.
     */
    static public GlobalSightLocale getLocaleById(Session p_session, long p_id)
        throws Exception
    {
        return (GlobalSightLocale)p_session.get(GlobalSightLocale.class, p_id);
    }
}
