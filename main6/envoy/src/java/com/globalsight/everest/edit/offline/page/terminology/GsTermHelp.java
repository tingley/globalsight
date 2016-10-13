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

package com.globalsight.everest.edit.offline.page.terminology;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import com.globalsight.everest.edit.offline.page.TerminologyHelp;
import com.globalsight.util.UTC;

/**
 * 
 * <code>GsTermHelp</code> is a concrete class for <code>TermHelp</code>,
 * using GlobalSigh pattern to format terminology.
 * 
 */
public class GsTermHelp extends TermHelp implements TerminologyHelp
{
    private static final String START = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
            + "<entries>";
    private static final String END = "</entries>";

    /**
     * The message pattern of conceptGrp. The following parameters are needed.<br>
     * 1. concept id <br>
     * 2. origination <br>
     * 3. date <br>
     * 4. languageGrp
     */
    private static final String CONCEPT_GRP = "<conceptGrp>"
            + "<concept>{0}</concept>"
            + "<transacGrp><transac type=\"origination\">{1}</transac>"
            + "<date>{2}</date></transacGrp>{3}</conceptGrp>";

    /**
     * The message pattern of languageGrp. The following parameters are needed.
     * 
     * 1. language, "English" for example<br>
     * 2. country, "en" for example <br>
     * 3. terminology<br>
     */
    private static final String LANGUAGE_GRP = "<languageGrp><language "
            + "name=\"{0}\" locale=\"{1}\"/>"
            + "<termGrp><term>{2}</term>{3}</termGrp></languageGrp>";

    @Override
    public String getEnd()
    {
        return END;
    }

    @Override
    public String getHead()
    {
        return START;
    }

    @Override
    public String getConcept(String languageGrp, long conceptId)
    {
        return MessageFormat.format(CONCEPT_GRP, conceptId, getUserName(), UTC
                .valueOf(new Date()), languageGrp);
    }

    @Override
    public String getLanguage(Locale locale, String terminology, String xml)
    {
        return MessageFormat.format(LANGUAGE_GRP, locale
                .getDisplayLanguage(LOCALE), locale.getLanguage(), terminology, xml);
    }
}
