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
package com.globalsight.everest.corpus;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * Holds some static methods needed by envoy classes.
 */
public class CorpusTm
{
    private static final Logger c_logger = Logger
            .getLogger(CorpusTm.class);

    /** Whether CorpusTM is installed */
    private static boolean s_isInstalled = false;

    /**
     * If the installation key for corpus TM is correct then true is returned,
     * otherwise false
     * 
     * @return true | false
     */
    public static boolean isInstalled()
    {
        String expectedKey = "CTM-" + "GS".hashCode() + "-"
                + "tmcorpus".hashCode();
        s_isInstalled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.CORPUS_INSTALL_KEY);

        return s_isInstalled;
    }

    /**
     * Looks up the value of corpus.storeNativeFormat from the system
     * parameters. Default value is true. If there is an error, then true is
     * returned.
     * 
     * @return
     */
    public static boolean isStoringNativeFormatDocs()
    {
        boolean defaultValue = true;
        try
        {
            String v = ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(
                            SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT)
                    .getValue();
            defaultValue = Boolean.valueOf(v).booleanValue();

        }
        catch (Exception e)
        {
            c_logger.error("Could not read system parameter "
                    + SystemConfigParamNames.CORPUS_STORE_NATIVE_FORMAT, e);
        }
        return defaultValue;
    }
}
