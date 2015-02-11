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

package com.globalsight.util.edit;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

import com.globalsight.ling.common.Text;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoOverrideItemException;
import com.globalsight.ling.tw.PtagStringFormatter;
import com.globalsight.ling.tw.TmxPseudo;


public class PtagUtil
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            PtagUtil.class.getName());

    /** Convert a gxml string to a ptag string. */
    public static String makePtagString(String p_gxml, 
                                        int p_pTagDisplayMode,
                                        String p_dataType)
        throws Exception
    {
        PseudoData pTagData = null;
        TmxPseudo convertor = null;

        // create ptag resources
        pTagData = new PseudoData();
        convertor = new TmxPseudo();

        // convert gxml
        pTagData.setMode(p_pTagDisplayMode);
        pTagData.setAddables(p_dataType);

        convertor.tmx2Pseudo(p_gxml, pTagData);

        return pTagData.getPTagSourceString();
    }

    /** color code ptag strings*/
    public static String colorPtagString(String p_rawPtagString,
                                         String p_searchPattern,
                                         boolean p_caseSensitive,
                                         GlobalSightLocale p_locale)
        throws Exception
    {
        PtagStringFormatter format = new PtagStringFormatter();
        return format.htmlLtrPtags(
            EditUtil.encodeHtmlEntities(p_rawPtagString), 
                                        p_searchPattern,
                                        p_caseSensitive,
                                        p_locale);
    }
}
