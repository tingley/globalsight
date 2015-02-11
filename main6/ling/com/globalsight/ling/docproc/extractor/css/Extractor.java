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
package com.globalsight.ling.docproc.extractor.css;

import com.globalsight.ling.docproc.extractor.css.ExtractionHandler;
import com.globalsight.ling.docproc.extractor.css.ExtractionRules;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.io.Reader;


public class Extractor
    extends AbstractExtractor
{
    private ExtractionRules m_rules = null;

    //
    // Constructor
    //
    public Extractor()
    {
        super();

        m_rules = new ExtractionRules ();
    }

    //
    // Interface Implementation -- ExtractorInterface
    //

    public void extract()
        throws ExtractorException
    {
        if (isEmbedded())
        {
            throw new ExtractorException (
                ExtractorExceptionConstants.EMBEDDING_NOT_SUPPORTED,
                "The CSS extractor cannot be called in an embedded context.");
        }

        setMainFormat(ExtractorRegistry.FORMAT_CSS);

        ExtractionHandler handler =
            new ExtractionHandler(getInput(), getOutput(), this, m_rules);

        Reader inputReader = readInput();

        Parser parser = new Parser(inputReader);
        parser.setHandler(handler);

        try
        {
            parser.Parse();
        }
        catch (ParseException e)
        {
            // e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.CSS_PARSE_ERROR, e.toString());
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
        String str_rules = getInput().getRules();
        m_rules.loadRules(str_rules);
        m_rules.loadRules(getDynamicRules());
    }
}
