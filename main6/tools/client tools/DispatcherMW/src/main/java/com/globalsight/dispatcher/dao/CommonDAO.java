/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.bo.TranslateFileBO;
import com.globalsight.dispatcher.util.FileUtil;

/**
 * Dispatcher Common DAO.
 * 
 * @author Joey
 *
 */
public class CommonDAO implements AppConstants
{
    protected static final Logger logger = Logger.getLogger(CommonDAO.class);
    protected static List<GlobalSightLocale> allLocalesByDisplayName;
    protected static List<GlobalSightLocale> allLocalesById;
    protected static Map<String, GlobalSightLocale> allLocalesMap;        
    public static final String appDataRoot;
    
    static
    {
        String serverPath = System.getProperty("user.dir");
        serverPath = serverPath.substring(0, serverPath.lastIndexOf(File.separator));
        appDataRoot = serverPath + File.separator + PROJECT_NAME;
        System.setProperty("appDataRoot", appDataRoot);
    }
    
    /**
     * Get all locales, which is sorted by display name.
     */
    public static List<GlobalSightLocale> getAllGlobalSightLocale()
    {
        if (allLocalesByDisplayName == null)
        {
            initSupportedGlobalSightLocale();
        }

        return allLocalesByDisplayName;
    }
    
    public static GlobalSightLocale getGlobalSightLocaleById(long p_Id)
    {
        if (allLocalesById == null)
        {
            initSupportedGlobalSightLocale();
        }

        if (p_Id > 0 && p_Id <= allLocalesById.size())
        {
            return allLocalesById.get((int) p_Id - 1);
        }
        
        return null;
    }
    
    // Get GlobalSightLocale by short name, such as 'en_US'.
    public static GlobalSightLocale getGlobalSightLocaleByShortName(String p_name)
    {
        if (allLocalesMap == null)
        {
            if (allLocalesByDisplayName == null)
            {
                initSupportedGlobalSightLocale();
            }

            allLocalesMap = new HashMap<String, GlobalSightLocale>(); 
            for (GlobalSightLocale locale : allLocalesByDisplayName)
            {
                allLocalesMap.put(locale.toString().toLowerCase(), locale);
            }
        }
        
        if(p_name == null)
            return null;

        GlobalSightLocale result = allLocalesMap.get(p_name.toLowerCase());
        if (result != null)
        {
            return result;
        }
        else
        {
            return allLocalesMap.get(p_name.toLowerCase().replace("-", "_"));
        }
    }
    
    /**
     * Initial GlobalSight Locale, which comes from 'Locale' Table.
     */
    public static void initSupportedGlobalSightLocale()
    {
        List<GlobalSightLocale> allLocales = new ArrayList<GlobalSightLocale>();            
        allLocales.add(new GlobalSightLocale("ar", "AE", 1));
        allLocales.add(new GlobalSightLocale("ar", "BH", 2));
        allLocales.add(new GlobalSightLocale("ar", "DZ", 3));
        allLocales.add(new GlobalSightLocale("ar", "EG", 4));
        allLocales.add(new GlobalSightLocale("ar", "IQ", 5));
        allLocales.add(new GlobalSightLocale("ar", "JO", 6));
        allLocales.add(new GlobalSightLocale("ar", "KW", 7));
        allLocales.add(new GlobalSightLocale("ar", "LB", 8));
        allLocales.add(new GlobalSightLocale("ar", "LY", 9));
        allLocales.add(new GlobalSightLocale("ar", "MA", 10));
        allLocales.add(new GlobalSightLocale("ar", "OM", 11));
        allLocales.add(new GlobalSightLocale("ar", "QA", 12));
        allLocales.add(new GlobalSightLocale("ar", "SA", 13));
        allLocales.add(new GlobalSightLocale("ar", "SD", 14));
        allLocales.add(new GlobalSightLocale("ar", "SY", 15));
        allLocales.add(new GlobalSightLocale("ar", "TN", 16));
        allLocales.add(new GlobalSightLocale("ar", "YE", 17));
        allLocales.add(new GlobalSightLocale("az", "AZ", 91));
        allLocales.add(new GlobalSightLocale("be", "BY", 18));
        allLocales.add(new GlobalSightLocale("bg", "BG", 19));
        allLocales.add(new GlobalSightLocale("ca", "ES", 20));
        allLocales.add(new GlobalSightLocale("cs", "CZ", 21));
        allLocales.add(new GlobalSightLocale("da", "DK", 22));
        allLocales.add(new GlobalSightLocale("de", "AT", 23));
        allLocales.add(new GlobalSightLocale("de", "CH", 24));
        allLocales.add(new GlobalSightLocale("de", "DE", 25));
        allLocales.add(new GlobalSightLocale("el", "GR", 26));
        allLocales.add(new GlobalSightLocale("en", "AU", 27));
        allLocales.add(new GlobalSightLocale("en", "CA", 28));
        allLocales.add(new GlobalSightLocale("en", "GB", 29));
        allLocales.add(new GlobalSightLocale("en", "IE", 30));
        allLocales.add(new GlobalSightLocale("en", "NZ", 31));
        allLocales.add(new GlobalSightLocale("en", "US", 32));
        allLocales.add(new GlobalSightLocale("en", "ZA", 33));
        allLocales.add(new GlobalSightLocale("es", "AR", 34));
        allLocales.add(new GlobalSightLocale("es", "BO", 35));
        allLocales.add(new GlobalSightLocale("es", "CL", 36));
        allLocales.add(new GlobalSightLocale("es", "CO", 37));
        allLocales.add(new GlobalSightLocale("es", "CR", 38));
        allLocales.add(new GlobalSightLocale("es", "DO", 39));
        allLocales.add(new GlobalSightLocale("es", "EC", 40));
        allLocales.add(new GlobalSightLocale("es", "EI", 90));
        allLocales.add(new GlobalSightLocale("es", "EM", 96));
        allLocales.add(new GlobalSightLocale("es", "ES", 41));
        allLocales.add(new GlobalSightLocale("es", "GT", 42));
        allLocales.add(new GlobalSightLocale("es", "HN", 43));
        allLocales.add(new GlobalSightLocale("es", "LAS", 94));
        allLocales.add(new GlobalSightLocale("es", "LX", 95));
        allLocales.add(new GlobalSightLocale("es", "MX", 44));
        allLocales.add(new GlobalSightLocale("es", "NI", 45));
        allLocales.add(new GlobalSightLocale("es", "PA", 46));
        allLocales.add(new GlobalSightLocale("es", "PE", 47));
        allLocales.add(new GlobalSightLocale("es", "PR", 48));
        allLocales.add(new GlobalSightLocale("es", "PY", 49));
        allLocales.add(new GlobalSightLocale("es", "SV", 50));
        allLocales.add(new GlobalSightLocale("es", "UY", 51));
        allLocales.add(new GlobalSightLocale("es", "VE", 52));
        allLocales.add(new GlobalSightLocale("et", "EE", 53));
        allLocales.add(new GlobalSightLocale("fi", "FI", 54));
        allLocales.add(new GlobalSightLocale("fr", "CA", 55));
        allLocales.add(new GlobalSightLocale("fr", "CH", 56));
        allLocales.add(new GlobalSightLocale("fr", "FR", 57));
        allLocales.add(new GlobalSightLocale("he", "IL", 58));
        allLocales.add(new GlobalSightLocale("hi", "IN", 93));
        allLocales.add(new GlobalSightLocale("hr", "HR", 59));
        allLocales.add(new GlobalSightLocale("hu", "HU", 60));
        allLocales.add(new GlobalSightLocale("id", "ID", 61));
        allLocales.add(new GlobalSightLocale("is", "IS", 62));
        allLocales.add(new GlobalSightLocale("it", "CH", 63));
        allLocales.add(new GlobalSightLocale("it", "IT", 64));
        allLocales.add(new GlobalSightLocale("ja", "JP", 65));
        allLocales.add(new GlobalSightLocale("ko", "KR", 66));
        allLocales.add(new GlobalSightLocale("lt", "LT", 67));
        allLocales.add(new GlobalSightLocale("lv", "LV", 68));
        allLocales.add(new GlobalSightLocale("mk", "MK", 69));
        allLocales.add(new GlobalSightLocale("ms", "BN", 92));
        allLocales.add(new GlobalSightLocale("mt", "MT", 98));
        allLocales.add(new GlobalSightLocale("nl", "BE", 70));
        allLocales.add(new GlobalSightLocale("nl", "NL", 71));
        allLocales.add(new GlobalSightLocale("no", "NO", 72));
        allLocales.add(new GlobalSightLocale("pl", "PL", 73));
        allLocales.add(new GlobalSightLocale("pt", "BR", 74));
        allLocales.add(new GlobalSightLocale("pt", "PT", 75));
        allLocales.add(new GlobalSightLocale("ro", "RO", 76));
        allLocales.add(new GlobalSightLocale("ru", "RU", 77));
        allLocales.add(new GlobalSightLocale("sh", "YU", 78));
        allLocales.add(new GlobalSightLocale("sk", "SK", 79));
        allLocales.add(new GlobalSightLocale("sl", "SI", 80));
        allLocales.add(new GlobalSightLocale("sq", "AL", 81));
        allLocales.add(new GlobalSightLocale("sr", "YU", 82));
        allLocales.add(new GlobalSightLocale("sv", "SE", 83));
        allLocales.add(new GlobalSightLocale("th", "TH", 84));
        allLocales.add(new GlobalSightLocale("tr", "TR", 85));
        allLocales.add(new GlobalSightLocale("uk", "UA", 86));
        allLocales.add(new GlobalSightLocale("vi", "VN", 97));
        allLocales.add(new GlobalSightLocale("zh", "CN", 87));
        allLocales.add(new GlobalSightLocale("zh", "HK", 89));
        allLocales.add(new GlobalSightLocale("zh", "TW", 88));
        
        allLocalesByDisplayName = new ArrayList<GlobalSightLocale>(allLocales);
        Collections.sort(allLocalesByDisplayName, new Comparator<GlobalSightLocale>()
        {
            public int compare(GlobalSightLocale o1, GlobalSightLocale o2)
            {
                return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
            }
        });
        
        allLocalesById = new ArrayList<GlobalSightLocale>(allLocales);
        Collections.sort(allLocalesById, new Comparator<GlobalSightLocale>()
        {
            public int compare(GlobalSightLocale o1, GlobalSightLocale o2)
            {
                if (o1.getId() > o2.getId())
                {
                    return 1;
                }
                else if (o1.getId() == o2.getId())
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
        });
    }

    public static String getDataFolderPath()
    {
        return getDataFolder() + File.separator;
    }
    
    public static File getDataFolder()
    {
        return getFolder(new File(appDataRoot), FOLDER_DATA);
    }
    
    public static File getFileStorage()
    {
        return getFolder(new File(appDataRoot), FOLDER_File_Storage);
    }
    
    public static File getFolder(File p_parent, String p_child)
    {
        File file = new File(p_parent, p_child);
        if (!file.exists())
            file.mkdirs();
        return file;
    }
    
    public List<TranslateFileBO> getTranslateFile(Account p_account)
    {
        List<TranslateFileBO> result = new ArrayList<TranslateFileBO>();
        File accountFile = new File(getFileStorage(), p_account.getAccountName());
        if (!FileUtil.isExists(accountFile))
            return result;
        
        for (File jobFile : accountFile.listFiles())
        {
            String jobID = jobFile.getName();
            File srcFile = getFirstSubFile(new File(jobFile, XLF_SOURCE_FOLDER));
            File trgFile = getFirstSubFile(new File(jobFile, XLF_TARGET_FOLDER));
            if (FileUtil.isExists(srcFile) || FileUtil.isExists(trgFile))
                result.add(new TranslateFileBO(p_account.getId(), jobID, jobFile.lastModified(), srcFile, trgFile));

        }
        
        Collections.sort(result, new Comparator<TranslateFileBO>(){
            public int compare(TranslateFileBO o1, TranslateFileBO o2)
            {
                if (o1.getLastModifyDate().after(o2.getLastModifyDate()))
                    return -1;
                else if (o1.getLastModifyDate().before(o2.getLastModifyDate()))
                    return 1;
                else
                    return 0;
            }
        });
        
        return result;
    }
    
    // Get the first sub file of Parent File.
    private File getFirstSubFile(File p_parentFile)
    {
        if(p_parentFile == null || !p_parentFile.exists())
            return null;
        
        File subFiles[] = p_parentFile.listFiles();
        return (subFiles == null || subFiles.length == 0) ? null : subFiles[0];
    }
}
