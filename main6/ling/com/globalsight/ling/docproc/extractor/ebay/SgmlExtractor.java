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
package com.globalsight.ling.docproc.extractor.ebay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;

import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.extractor.html.Extractor;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Special extractor for EBay's .sgml files which massages the <plumber>
 * preprocessor instructions.
 */
public class SgmlExtractor extends Extractor
{
    private static final REProgram PLUMBER_PATTERN = compilePattern("<plumber[ \t]+perlvar=\"([^\"]*)\"");

    // WARNING - keep in sync with merger/ebay/SgmlPostMergeProcessor
    private static final String PLUMBER_SUBST_START = "<plumber perlvar='";
    private static final String PLUMBER_SUBST_END = "'";

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

    //
    // Constructors
    //

    public SgmlExtractor()
    {
        super();
    }

    /**
     * Override format type of HTML extractor.
     */
    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_EBAY_SGML);
    }

    /**
     * Preprocess the input:
     */
    public void extract() throws ExtractorException
    {
        preprocessInput();

        super.extract();
    }

    private void preprocessInput() throws ExtractorException
    {
        BufferedReader reader = new BufferedReader(readInput());
        StringWriter writer = new StringWriter(4096);

        String line = null;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                line = fixPlumber(line);

                writer.write(line);
                writer.write("\r\n");
            }

            reader.close();
            writer.close();

            EFInputData newInput = new EFInputData();
            newInput.setType(getInput().getType());
            newInput.setLocale(getInput().getLocale());
            newInput.setRules(getInput().getRules());
            newInput.setUnicodeInput(writer.toString());

            super.init(newInput, getOutput());
        }
        catch (IOException ex)
        {
            System.err.println("can't preprocess input: " + ex);

            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_SOURCE, ex);
        }
    }

    private String fixPlumber(String p_line)
    {
        String result = p_line;

        RE re = new RE(PLUMBER_PATTERN);

        int start = 0;
        while (re.match(p_line, start))
        {
            String subst = PLUMBER_SUBST_START + re.getParen(1)
                    + PLUMBER_SUBST_END;

            result = re.subst(result, subst, RE.REPLACE_FIRSTONLY);

            start = re.getParenEnd(0);
        }

        return result;
    }
}
