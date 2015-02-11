/**
 *  Copyright 2014 Welocalize, Inc. 
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
package com.globalsight.dispatcher.util;

import java.util.HashMap;

import com.globalsight.dispatcher.bo.MachineTranslationProfile;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslator;

public class TranslateUtil
{
    /**
     * Get Machine Translator and do configuration.
     * @param p_mtProfile
     * @return
     */
    public static MachineTranslator getMachineTranslator(MachineTranslationProfile p_mtProfile)
    {
        MachineTranslator translator = MTHelper.initMachineTranslator(p_mtProfile.getMtEngine());
        HashMap parameterMap = p_mtProfile.getParamHM();
        parameterMap.put(MachineTranslator.NEED_SPECAIL_PROCESSING_XLF_SEGS, "false");
        parameterMap.put(MachineTranslator.DATA_TYPE, "xlf");
        translator.setMtParameterMap(parameterMap);
        return translator;
    }

}
