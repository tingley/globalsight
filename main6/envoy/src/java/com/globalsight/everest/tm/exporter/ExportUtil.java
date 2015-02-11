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

import java.io.File;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class ExportUtil
{
    private static final Logger CATEGORY = Logger.getLogger(ExportUtil.class);

    /** Cache of GlobalSightLocale and locale string and map. */
    static private Hashtable s_locale2name = new Hashtable();

    /** Static class, private constructor. */
    private ExportUtil()
    {
    }

    public static  String getExportDirectory()
    {
        String exportDir = "";
        try
        {
            String fsDirPath = AmbFileStoragePathUtils.getFileStorageDirPath();
            exportDir = fsDirPath + File.separator
                    + AmbFileStoragePathUtils.TM_EXPORT_FILE_SUB_DIR;
            exportDir = exportDir.replace("\\", "/").replace("/",
                    File.separator);
        }
        catch (Throwable e)
        {
            CATEGORY.error("cannot create directory " + exportDir, e);
        }

        return exportDir;
    }

    /**
     * A cache from GlobalSightLocale to printed locale representation as found
     * in TMX files.
     * 
     * @param p_locale
     *            : a GlobalSightLocale.
     */
    static public String getLocaleString(GlobalSightLocale p_locale)
    {
        String result = (String) s_locale2name.get(p_locale);

        if (result == null)
        {
            result = p_locale.getLanguageCode() + "-"
                    + p_locale.getCountryCode();

            result = result.toUpperCase();

            s_locale2name.put(p_locale, result);
        }

        return result;
    }

    /**
     * Maps a local string (en_US) to a GlobalSightLocale id.
     */
    static public long getLocaleId(String p_locale) throws Exception
    {
        LocaleManager mgr = ServerProxy.getLocaleManager();
        GlobalSightLocale locale = mgr.getLocaleByString(p_locale);

        return locale.getId();
    }

    /**
     * Good old getLocale method.
     */
    static public GlobalSightLocale getLocaleById(long p_id) throws Exception
    {
        return HibernateUtil.get(GlobalSightLocale.class, p_id);
    }

    /**
     * When export TM, create/delete file named "failed" or "inprogress" which
     * are used to indicate the TM exporting status.
     * 
     * @param identifyKey
     * @param status
     * @param isCreate
     */
    public static void handleTmExportFlagFile(String identifyKey,
            String status, boolean isCreate)
    {
        if (StringUtil.isNotEmpty(identifyKey))
        {
            String filePathName = getExportDirectory() + "/" + identifyKey
                    + "/" + status;
            if (isCreate)
            {
                new File(filePathName).mkdirs();
            }
            else
            {
                File file = new File(filePathName);
                FileUtil.deleteFile(file);
            }
        }
    }
}
