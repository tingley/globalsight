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
package com.globalsight.ling.docproc.extractor.sgml;

import com.globalsight.ling.docproc.extractor.sgml.ExtractionHandler;
import com.globalsight.ling.docproc.extractor.sgml.ExtractionRules;
import com.globalsight.ling.docproc.extractor.sgml.Parser;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.io.Reader;


public class Extractor
    extends AbstractExtractor
    implements ISgmlConstants
{
    //
    // Private Variables
    //

    private Parser parser;
    protected ExtractionRules m_rules = null;

    //
    // Constructors
    //

    public Extractor()
    {
        super();

        m_rules = new ExtractionRules ();
    }

    //
    // Public Methods
    //
    public Parser getParser()
    {
        return parser;
    }

    //
    // Will be overwritten in classes derived from HTML extractor (CF,
    // JHTML).
    //
    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_SGML);
    }

    //
    // Interface Implementation -- ExtractorInterface
    //

    public void extract()
        throws ExtractorException
    {
        // Set the main format depending on which (derived) class
        // we're called in.
        setFormat();

        ExtractionHandler extractor =
            new ExtractionHandler (getInput(), getOutput(), this, m_rules,
                ISgmlConstants.EC_JAVASCRIPT);

        Reader inputReader = readInput();

        parser = new Parser(inputReader);
        parser.setHandler(extractor);

        try
        {
            parser.parse();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.HTML_PARSE_ERROR, e.getMessage());
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.INTERNAL_ERROR, e.toString());
        }

        String strError = extractor.checkError();
        if (strError != null)
        {
            throw new ExtractorException (
                ExtractorExceptionConstants.HTML_PARSE_ERROR, strError);
        }
    }

    public void loadRules()
        throws ExtractorException
    {
        String str_rules = getInput().getRules();
        m_rules.loadRules(str_rules);
        m_rules.loadRules(getDynamicRules());
    }
}
