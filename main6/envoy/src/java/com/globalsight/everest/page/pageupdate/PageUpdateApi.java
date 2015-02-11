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
package com.globalsight.everest.page.pageupdate;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.pageupdate.ExtractedFileValidation;

import java.util.ArrayList;

/**
 * API for validating and updating extracted files.
 * This API is for classes external to the package.
 */
public class PageUpdateApi
{
    // name used for the category when logging
    public static final String LOGGER_CATEGORY = "EditSourcePage";

    /**
     * For source page editing: validates the new GXML of the source page.
     * @return list of error messages.
     */
    static public ArrayList validateSourcePageGxml(String p_gxml)
    {
        ExtractedFileValidation validator = new ExtractedFileValidation(p_gxml);
        return validator.validateSourcePageGxml();
    }

    static public ArrayList updateSourcePageGxml(SourcePage p_sourcepage,
        String p_gxml)
    {
        ExtractedFileUpdater updater = new ExtractedFileUpdater(
            p_sourcepage, p_gxml);
        return updater.updateSourcePageGxml();
    }
}
