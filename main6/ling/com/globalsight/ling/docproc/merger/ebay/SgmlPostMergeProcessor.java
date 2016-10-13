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
package com.globalsight.ling.docproc.merger.ebay;

import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * This class post processes a merged eBay PRJ document. Rstores the non-XML
 * <plumber> processing instructions.
 */
public class SgmlPostMergeProcessor implements PostMergeProcessor
{
    private static final String PROCESS_ERROR = "eBay SGML post merge process error";

    private static final REProgram PLUMBER_PATTERN1 = compilePattern("&lt;plumber perlvar='([^']*?)'/&gt;");
    private static final REProgram PLUMBER_PATTERN2 = compilePattern("<plumber perlvar='([^']*?)'/>");

    private static final String PLUMBER_SUBST_START = "<plumber perlvar=\"";
    private static final String PLUMBER_SUBST_END = "\"/>";

    private static REProgram compilePattern(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }

        return pattern;
    }

    /**
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String,
     *      java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
            throws DiplomatMergerException
    {
        return rewritePlumbers(p_content, p_IanaEncoding);
    }

    private String rewritePlumbers(String p_content, String p_encoding)
    {
        String result = p_content;

        // Variation: &lt;plumber/&gt;
        RE re = new RE(PLUMBER_PATTERN1);
        int start = 0;
        while (re.match(p_content, start))
        {
            String subst = PLUMBER_SUBST_START + re.getParen(1)
                    + PLUMBER_SUBST_END;

            result = re.subst(result, subst, RE.REPLACE_FIRSTONLY);

            start = re.getParenEnd(0);
        }

        /*
         * // Variation: <plumber/> re = new RE(PLUMBER_PATTERN2); start = 0;
         * while (re.match(p_content, start)) { String subst =
         * PLUMBER_SUBST_START + re.getParen(1) + PLUMBER_SUBST_END;
         * 
         * result = re.subst(result, subst, RE.REPLACE_FIRSTONLY);
         * 
         * start = re.getParenEnd(0); }
         */
        return result;
    }
}
