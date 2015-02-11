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

package com.globalsight.cxe.adapter.passolo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;

public class PassoloUtil
{
    static private final Logger logger = Logger.getLogger(PassoloUtil.class);
    private static String PATH = "/properties/Passolo.properties";
    public static Properties PROPERTIES = new Properties();
    public static Map<String, List<String>> LOCALE_G2P = new HashMap<String, List<String>>();
    public static Map<String, Integer> EXPORTING_PAGES = new HashMap<String, Integer>();

    static
    {
        try
        {
            PROPERTIES.load(PassoloUtil.class.getResourceAsStream(PATH));

            Set<Object> keys = PROPERTIES.keySet();
            for (Object o : keys)
            {
                String key = (String) o;
                String value = PROPERTIES.getProperty(key);

                List<String> ls = LOCALE_G2P.get(value);
                if (ls == null)
                {
                    ls = new ArrayList<String>();
                    LOCALE_G2P.put(value, ls);
                }

                ls.add(key);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getLocale(String path)
    {
        String temp = path.toLowerCase().replace("\\", "/");

        int i = temp.indexOf("/passolo/");

        int beginIndex = temp.indexOf(".lpu/", i);
        if (beginIndex > 0)
        {
            int endIndex = temp.indexOf("/", beginIndex + 5);
            if (endIndex > 0)
            {
                return path.substring(beginIndex + 5, endIndex);
            }
        }

        throw new IllegalArgumentException(
                "Can not find the locale from the path: " + path);
    }

    public static String getMappingLocales(String locale)
    {
        return PROPERTIES.getProperty(locale, locale.replace("-", "_"));
    }

    public static List<String> getMappingLocalesG2P(String locale)
    {
        return LOCALE_G2P.get(locale);
    }

    public static String getKey(String baseHref, String displayName,
            long exportBatchId)
    {
        displayName = displayName.replace("\\", "/");
        int index = displayName.indexOf("/", baseHref.length() + 2);
        String key = exportBatchId + displayName.substring(0, index);

        return key;
    }

    public static void addExportingPage(TargetPage targetPage,
            long exportBatchId)
    {
        SourcePage sourcePage = targetPage.getSourcePage();
        if (isPassoloFile(sourcePage))
        {
            ExtractedFile f = sourcePage.getExtractedFile();
            if (f != null && f instanceof ExtractedSourceFile)
            {
                ExtractedSourceFile sf = (ExtractedSourceFile) f;
                String href = sf.getExternalBaseHref();
                String path = sourcePage.getExternalPageId();

                String key = getKey(href, path, exportBatchId);

                Integer num = EXPORTING_PAGES.get(key);
                if (num == null)
                    num = 0;

                EXPORTING_PAGES.put(key, num + 1);
            }
        }
    }

    public static boolean isPassoloFile(SourcePage sourcePage)
    {
        ExtractedFile f = sourcePage.getExtractedFile();
        if (f != null && f instanceof ExtractedSourceFile)
        {
            ExtractedSourceFile sf = (ExtractedSourceFile) f;
            return "passolo".equals(sf.getDataType());
        }

        return false;
    }
}
