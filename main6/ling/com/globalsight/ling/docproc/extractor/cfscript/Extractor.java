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
package com.globalsight.ling.docproc.extractor.cfscript;

import com.globalsight.ling.docproc.extractor.cfscript.ExtractionHandler;
import com.globalsight.ling.docproc.extractor.cfscript.ParseException;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.io.Reader;

/**
 * An Extractor for Allaire's ColdFusion Script language.
 */
public class Extractor
    extends AbstractExtractor
{
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
        setMainFormat(ExtractorRegistry.FORMAT_CFSCRIPT);

        ExtractionHandler handler =
          new ExtractionHandler (getInput(), getOutput(), this);

        Reader inputReader = readInput();

        Parser parser = new Parser (inputReader);
        parser.setHandler(handler);

        try
        {
            parser.parse();
        }
        catch (ParseException e)
        {
            // e.printStackTrace();
            throw new ExtractorException (
              ExtractorExceptionConstants.CF_PARSE_ERROR, e.toString());
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
    }

}

