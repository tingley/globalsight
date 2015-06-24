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
import com.globalsight.ling.docproc.extractor.xml.Extractor;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Special extractor for EBay's .prj files, which are XSL files with an invalid
 * XML declaration.
 *
 * <?xml version=\${XMLVERSION} encoding="\${\${COUNTRYPATH}CHARENCODING}" ?>
 */
public class PrjExtractor extends Extractor
{
    // WARNING - keep in sync with merger/ebay/PrjPostMergeProcessor
    public static final String XMLDECLARATION = "<?xml version=\"1.0\" ?>";

    private static final REProgram COMMENT_PATTERN = compilePattern("<comment (.*?)/>");

    private static final String COMMENT_SUBST_START = "<comment>";
    private static final String COMMENT_SUBST_END = "</comment>";

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

    public PrjExtractor()
    {
        super();
    }

    /**
     * Override format type of XML extractor.
     */
    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_EBAY_PRJ);
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
            // replace first line with standard XML decl
            line = reader.readLine();
            if (line != null)
            {
                writer.write(XMLDECLARATION);
                writer.write("\r\n");
            }

            while ((line = reader.readLine()) != null)
            {
                line = fixComment(line);

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

    private String fixComment(String p_line)
    {
        String result = p_line;

        RE re = new RE(COMMENT_PATTERN);

        int start = 0;
        while (re.match(p_line, start))
        {
            String subst = COMMENT_SUBST_START + re.getParen(1)
                    + COMMENT_SUBST_END;

            result = re.subst(result, subst, RE.REPLACE_FIRSTONLY);

            start = re.getParenEnd(0);
        }

        return result;
    }
}
