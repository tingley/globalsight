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

package com.globalsight.webservices;

import java.util.Random;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GlobalSightLocale;

/**
 * Utilities for web service APIs.
 * 
 * @author YorkJin
 * @since 2014-02-17
 * @version 8.5.3
 */
public class AmbassadorUtil
{
    private static final Logger logger = Logger.getLogger(AmbassadorUtil.class);

    /**
     * Get locale by locale name such as "zh_CN".
     * 
     * @param localeName
     *            -- locale name in "LANG_COUNTRY" style.
     * @return GlobalSightLocale
     * @throws WebServiceException
     */
    public static GlobalSightLocale getLocaleByName(String localeName)
            throws WebServiceException
    {
        localeName = ImportUtil.normalizeLocale(localeName.trim());
        try
        {
            return ImportUtil.getLocaleByName(localeName);
        }
        catch (Exception e)
        {
            logger.warn("getLocaleByName() : Fail to get GlobalSightLocale by locale name: '"
                    + localeName + "'");
            throw new WebServiceException("Unable to get locale by : "
                    + localeName);
        }
    }

    /**
     * If public URL is enabled, return the public URL, otherwise return the CAP
     * login URL.
     * 
     * @return String
     */
    public static String getCapLoginOrPublicUrl()
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        boolean usePublicUrl = "true".equalsIgnoreCase(config
                .getStringParameter("cap.public.url.enable"));
        if (usePublicUrl)
        {
            return config.getStringParameter("cap.public.url");
        }
        else
        {
            return config.getStringParameter("cap.login.url");
        }
    }

    /**
     * Get a random string
     * 
     * @return String
     */
    public static synchronized String getRandomFeed()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        String randomStr = String.valueOf((new Random()).nextInt(999999999));
        while (randomStr.length() < 9)
        {
            randomStr = "1" + randomStr;
        }
        return randomStr;
    }
}
