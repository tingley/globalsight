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
package com.globalsight.ling.docproc.extractor.xliff;

import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.ling.docproc.IFormatNames;

public class XliffAltUtil
{
    /**
     * Xliff/XLZ/Passolo files are using "XLF" extractor, they will store their
     * "alt_trans" data into "xliff_alt" table. So only these formats need load
     * xliff alts data.
     * <p>
     * Note that xlz also use "xlf" as data type.
     * </p>
     */
    public static boolean isGenerateXliffAlt(SourcePage p_sourcePage)
    {
        try
        {
            if (p_sourcePage.getPrimaryFileType() == ExtractedFile.EXTRACTED_FILE)
            {
                ExtractedFile ef = (ExtractedFile) p_sourcePage
                        .getPrimaryFile();
                String dataType = ((ExtractedSourceFile) ef).getDataType();
                if (IFormatNames.FORMAT_XLIFF.equalsIgnoreCase(dataType)
                        || IFormatNames.FORMAT_PASSOLO
                                .equalsIgnoreCase(dataType))
                {
                    return true;
                }
            }
        }
        catch (Exception ignore)
        {
            return false;
        }

        return false;
    }
}
