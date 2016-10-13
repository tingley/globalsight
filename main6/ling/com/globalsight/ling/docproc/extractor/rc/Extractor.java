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
package com.globalsight.ling.docproc.extractor.rc;

import java.io.Reader;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;

/**
 * <p>Extractor for Java code.  Can be used to extract statements,
 * expressions and class-level declarations (variables, methods,
 * initializers).  Use the rules mechanism (setRules(String)) to
 * specify which parse method to use.</p>
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
        setMainFormat(ExtractorRegistry.FORMAT_RC);

        ExtractionHandler handler =
          new ExtractionHandler (getInput(), getOutput(), this);

        Reader inputReader = readInput();

        Parser parser = new Parser (inputReader);
        parser.setHandler(handler);

        try
        {
            parser.Parse();
        }
        catch (ParseException e)
        {
            // e.printStackTrace();
            throw new ExtractorException (
              ExtractorExceptionConstants.RC_PARSE_ERROR, e.toString());
        }
        catch (Throwable e)
        {
            // e.printStackTrace();
            throw new ExtractorException (
              ExtractorExceptionConstants.INTERNAL_ERROR, e.toString());
        }
    }

    @Override
    public void loadRules() throws ExtractorException
    {
        // TODO Auto-generated method stub
        
    }
}
