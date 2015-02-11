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
package com.globalsight.ling.docproc.merger.xptag;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;

/**
 * This class post processes a merged XPTag document.
 */
public class XptagPostMergeProcessor
    implements PostMergeProcessor
{
    private static Logger c_category =
        Logger.getLogger(
            XptagPostMergeProcessor.class);

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String, java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
        throws DiplomatMergerException
    {
        String processed = null;

        try
        {
            RegExMatchInterface match = RegEx.matchSubstring(p_content,
                "<e(\\d)>");

            // encoding = <e0> Mac OS, <e1> Windows ANSI, <e2> ISO Latin 1
            if (match != null)
            {
                // overwrite the encoding tag
                String encoding = match.group(1);
                String newEncoding = getNewEncoding(encoding, p_IanaEncoding);

                if (newEncoding != null)
                {
                    processed = RegEx.substituteAll(p_content,
                        "<e\\d>", "<e" + newEncoding + ">");
                }
            }
        }
        catch (RegExException e)
        {
            // shouldn't happen
            c_category.error(e.getMessage(), e);
        }

        return processed;
    }

    // encoding = <e0> Mac OS, <e1> Windows ANSI, <e2> ISO Latin 1
    private String getNewEncoding(String p_old, String p_ianaEncoding)
    {
        String result = null;

        if (p_ianaEncoding.equalsIgnoreCase("MacRoman"))
        {
            result = "0";
        }
        else if (p_ianaEncoding.equalsIgnoreCase("Windows-1252"))
        {
            result = "1";
        }
        else if (p_ianaEncoding.equalsIgnoreCase("ISO-8859-1"))
        {
            result = "2";
        }

        if (p_old.equals(result))
        {
            return null;
        }

        return result;
    }
}
