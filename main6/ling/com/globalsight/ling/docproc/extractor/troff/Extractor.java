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
package com.globalsight.ling.docproc.extractor.troff;

import com.globalsight.ling.docproc.extractor.troff.ExtractionHandler;
import com.globalsight.ling.docproc.extractor.troff.Parser;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.io.Reader;

public class Extractor
    extends AbstractExtractor
{
    //
    // Private Variables
    //

    private Parser parser;

    //
    // Constructors
    //

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
        setMainFormat(ExtractorRegistry.FORMAT_TROFF_MAN);

        ExtractionHandler handler =
            new ExtractionHandler (getInput(), getOutput(), this);

        Reader inputReader = readInput();

        parser = new Parser(inputReader, handler);

        try
        {
            parser.parse();
        }
        /***
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.TROFF_PARSE_ERROR, e.getMessage());
        }
        ***/
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.INTERNAL_ERROR, e.toString());
        }

        /***
        String strError = extractor.checkError();
        if (strError != null)
        {
            throw new ExtractorException (
                ExtractorExceptionConstants.TROFF_PARSE_ERROR, strError);
        }
        ***/
    }

    public void loadRules()
        throws ExtractorException
    {
    }
}
