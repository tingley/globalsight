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

package com.globalsight.everest.tuv;

import com.globalsight.util.GlobalSightLocale;

/**
 * Used to process the tuv from po file.
 * 
 * @author Walter.Xu
 * @since 2011-11-17
 * @version 8.2
 */

public class PoProcessor extends XliffProcessor
{
    public String getTargetLanguage(TuImpl p_tu,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
    {
        String targetLanguage = p_tu.getXliffTargetLanguage();

        targetLanguage = getPOTargetLanguage(targetLanguage,
                    p_sourceLocale, p_targetLocale);

        if (targetLanguage != null)
        {
            targetLanguage = targetLanguage.toLowerCase();
        }

        return targetLanguage;
    }
    /**
     * Gets PO Target Language, which will be used for creating target TUV. If
     * p_language equals with sourceLocale, then set the targetLocale.
     * 
     * @param p_language
     *            po target language, which comes from Extractor.
     * @param p_sourceLocale
     * @param p_targetLocale
     * @return
     */
    private String getPOTargetLanguage(String p_language,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
    {
        if (p_language == null)
            return "";

        if (p_language.length() == 2)
        {
            if (p_language.equalsIgnoreCase(p_sourceLocale.getLanguage()))
            {
                p_language = p_targetLocale.toString();
            }
            else
            {
                p_language = p_language + "_" + p_targetLocale.getCountry();
            }
        }
        else if (p_language.matches("zh-.."))
        {
            p_language = p_language.replace("-", "_");
            if (p_language.equalsIgnoreCase(p_sourceLocale.toString()))
            {
                p_language = p_targetLocale.toString();
            }
        }

        return p_language;
    }
}
