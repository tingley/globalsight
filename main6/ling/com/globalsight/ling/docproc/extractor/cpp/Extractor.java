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
package com.globalsight.ling.docproc.extractor.cpp;

import com.globalsight.ling.docproc.extractor.cpp.ExtractionHandler;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

import java.io.Reader;

/**
 * <p>Extractor for C++ code.</p>
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
        setMainFormat(ExtractorRegistry.FORMAT_CPP);

        ExtractionHandler handler =
            new ExtractionHandler (getInput(), getOutput(), this);

        Reader inputReader = readInput();

        try
        {
            ParserTokenManager parser = new ParserTokenManager(
                new SimpleCharStream(inputReader, 1, 1));

            parser.setHandler(handler);

            Token t;
            do
            {
                t = parser.getNextToken();
            }
            while (t.kind != parser.EOF);

            parser.finish();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.CPP_PARSE_ERROR, e.toString());
        }
    }

    public void loadRules()
        throws ExtractorException
    {
    }
}
