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

import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;

import com.globalsight.ling.docproc.extractor.ebay.PrjExtractor;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;

import com.globalsight.diplomat.util.Logger;

/**
 * This class post processes a merged eBay PRJ document.
 * Inserts the previous XML declaration:
 * <?xml version=\${XMLVERSION} encoding="\${\${COUNTRYPATH}CHARENCODING}" ?>
 */
public class PrjPostMergeProcessor
    implements PostMergeProcessor
{
    private static final String PROCESS_ERROR =
        "eBay PRJ post merge process error";

    private static final REProgram PATTERN =
        compilePattern("<\\?xml version=\"1.0\" \\?>");

    public static final String EBAY_XMLDECLARATION =
        "<?xml version=\\${XMLVERSION} encoding=\"\\${\\${COUNTRYPATH}CHARENCODING}\" ?>";

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
     * @see com.globalsight.ling.document.merger.PostMergeProcessor#process(java.lang.String, java.lang.String)
     */
    public String process(String p_content, String p_IanaEncoding)
        throws DiplomatMergerException
    {
        return rewriteXmlDecl(p_content, p_IanaEncoding);
    }


    private String rewriteXmlDecl(String p_content, String p_encoding)
    {
        RE re = new RE(PATTERN);

        return re.subst(p_content, EBAY_XMLDECLARATION, RE.REPLACE_FIRSTONLY);
    }
}
