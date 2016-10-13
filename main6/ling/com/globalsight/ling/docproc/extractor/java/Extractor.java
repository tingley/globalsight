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
package com.globalsight.ling.docproc.extractor.java;

import com.globalsight.ling.docproc.extractor.java.ExtractionHandler;
import com.globalsight.ling.docproc.extractor.java.ParseException;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.io.Reader;

/**
 * <p>Extractor for Java code.  Can be used to extract statements,
 * expressions and class-level declarations (variables, methods,
 * initializers).  Use the rules mechanism (setRules(String)) to
 * specify which parse method to use.</p>
 */
public class Extractor
    extends AbstractExtractor
{
    private static final int JAVA_EVERYTHING = 0;
    private static final int JAVA_STATEMENTS = 1;
    private static final int JAVA_EXPRESSION = 2;
    private static final int JAVA_CLASSLEVEL = 3;

    /** Default is to extract entire Java files */
    private int m_parseMode = JAVA_EVERYTHING;

    /**
     * Extractor constructor.
     */
    public Extractor()
    {
        super();
    }

    //
    // Interface Implementation -- ExtractorInterface
    //

    public void extract()
        throws ExtractorException
    {
        setMainFormat(ExtractorRegistry.FORMAT_JAVA);

        ExtractionHandler handler =
          new ExtractionHandler (getInput(), getOutput(), this);

        Reader inputReader = readInput();

        Parser parser = new Parser (inputReader);
        parser.setHandler(handler);

        try
        {
            switch (m_parseMode)
            {
            case JAVA_CLASSLEVEL:
                parser.parseClassLevel();
                break;
            case JAVA_EXPRESSION:
                parser.parseExpression();
                break;
            case JAVA_STATEMENTS:
                parser.parseStatements();
                break;
            case JAVA_EVERYTHING: // fallthru
            default:
                parser.parseFile();
                break;
            }
        }
        catch (ParseException e)
        {
            // e.printStackTrace();
            throw new ExtractorException (
              ExtractorExceptionConstants.JAVA_PARSE_ERROR, e.toString());
        }
        catch (Throwable e)
        {
            // e.printStackTrace();
            throw new ExtractorException (
              ExtractorExceptionConstants.INTERNAL_ERROR, e.toString());
        }
    }

    public void loadRules()
        throws ExtractorException
    {
        // System.err.println("java loadrules: " + getInput().getRules());

        String parseMode = getInput().getRules();

        if (parseMode == null || parseMode.length() == 0)
        {
            return;
        }

        if (parseMode.equalsIgnoreCase("code"))   // JHTML types
        {
            m_parseMode = JAVA_STATEMENTS;
        }
        else if (parseMode.equalsIgnoreCase("class"))
        {
            m_parseMode = JAVA_CLASSLEVEL;
        }
        else if (parseMode.equalsIgnoreCase("print"))
        {
            m_parseMode = JAVA_EXPRESSION;
        }
        else if (parseMode.equalsIgnoreCase("file")) // for Java files
        {
            m_parseMode = JAVA_EVERYTHING;
        }
    }
}
