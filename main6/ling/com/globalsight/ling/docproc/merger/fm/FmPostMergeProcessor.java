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
package com.globalsight.ling.docproc.merger.fm;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.extractor.fm.Parser;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.DiplomatMergerException;

/**
 * This class post processes a merged FM/mif document
 */
public class FmPostMergeProcessor implements PostMergeProcessor
{
    private static Logger c_category = Logger
            .getLogger(FmPostMergeProcessor.class);

    private String sourceLocale = null;
    private String targetLocale = null;

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String,
     *      java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding) throws DiplomatMergerException
    {
        FontMappingHelper helper = new FontMappingHelper();
        
        if (targetLocale == null || !helper.isLocaleWithFonts(targetLocale))
        {
            return p_content;
        }
        
        String defaultFont = helper.getDefaultMappingFont(targetLocale);

        if (c_category.isDebugEnabled())
        {
            c_category.debug("targetLocale : " + targetLocale);
            c_category.debug("FontMapping list : " + helper.getFontMappingList());
            c_category.debug("defaultFont : " + defaultFont);
        }

        StringBuffer result = new StringBuffer();

        Reader r = new StringReader(p_content);
        Parser mifParser = new Parser(r);
        List<String> lines = mifParser.getLineList();

        // <FPlatformName `W.Times New Roman.R.400'>
        // <FFamily `Times New Roman'>
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            String trimmed = line.trim();
            String sourceFont = null;
            if (trimmed.startsWith("<FPlatformName "))
            {
                int index1 = trimmed.indexOf(".");
                int index2 = trimmed.indexOf(".", index1 + 1);
                sourceFont = trimmed.substring(index1 + 1, index2);
            }
            else if (trimmed.startsWith("<FFamily "))
            {
                int index1 = trimmed.indexOf("`");
                int index2 = trimmed.indexOf("'", index1 + 1);
                sourceFont = trimmed.substring(index1 + 1, index2);
            }

            if (sourceFont != null)
            {
                String targetFont = helper.getMappingFont(sourceFont, targetLocale);
                
                if (targetFont != null)
                {
                    line = line.replace(sourceFont, targetFont);
                }
                else if (defaultFont != null)
                {
                    line = line.replace(sourceFont, defaultFont);
                }
            }

            result.append(line).append("\n");
        }

        return result.toString();
    }

    public void setSourceLocale(String sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }
}
