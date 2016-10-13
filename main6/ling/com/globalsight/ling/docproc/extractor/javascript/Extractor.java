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
package com.globalsight.ling.docproc.extractor.javascript;

import java.io.Reader;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.IFormatNames;

public class Extractor
    extends AbstractExtractor
{
    private ExtractionRules m_rules = null;
    private String JsFilterRegex = null;
    private boolean noUseJsFunction = false;
    public Extractor()
    {
        super();

        m_rules = new ExtractionRules();
    }

    public void extract()
        throws ExtractorException
    {
        setMainFormat(IFormatNames.FORMAT_JAVASCRIPT);

        ExtractionHandler handler =
            new ExtractionHandler (getInput(), getOutput(), this, m_rules);
        handler.setJsFilterRegex(getJsFilterRegex());
        Reader inputReader = readInput();

        Parser parser = new Parser (inputReader);
        parser.setHandler(handler);

        try
        {
            parser.parse();
        }
        catch (ParseException e)
        {
            throw new ExtractorException (
                ExtractorExceptionConstants.JS_PARSE_ERROR, e.toString());
        }
        catch (Throwable e)
        {
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

    public String getJsFilterRegex()
    {
        return JsFilterRegex;
    }

    public void setJsFilterRegex(String jsFilterRegex)
    {
        JsFilterRegex = jsFilterRegex;
    }

    public void setNoUseJsFunction(String string)
    {
        if(string == null)
        {
            return;
        }
        noUseJsFunction = Boolean.parseBoolean(string);
    }
    public boolean getNoUseJsFunction()
    {
        return noUseJsFunction;
    }
}

