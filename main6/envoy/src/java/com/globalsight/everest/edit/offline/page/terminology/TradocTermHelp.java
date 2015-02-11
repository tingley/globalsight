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
 * <code>TradocTermHelp</code> is a concrete class for <code>TermHelp</code>.
 * It used tradoc pattern to format the terminology.
 */
public class TradocTermHelp extends TermHelp implements TerminologyHelp
{
    private static final String START = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><mtf>";
    private static final String END = "</mtf>";

    /**
     * The message pattern of conceptGrp. The following parameters are needed. *
     * 1. origination <br>
     * 2. date <br>
     * 3. languageGrp
     * 
     * <p>
     * The pattern is
     * 
     * <pre>
     * &lt;conceptGrp&gt;
     *     &lt;transacGrp&gt;
     *     &lt;transac type=&quot;origination&quot;&gt;{0}&lt;/transac&gt;
     *     &lt;date&gt;{1}&lt;/date&gt;
     *     &lt;/transacGrp&gt;
     *     {2} 
     * &lt;/conceptGrp&gt;
     * </pre>
     */
    private static final String CONCEPT_GRP = "<conceptGrp>"
            + "<transacGrp><transac type=\"origination\">{0}</transac>"
            + "<date>{1}</date></transacGrp>{2}</conceptGrp>";

    /**
     * The message pattern of languageGrp. The following parameters are needed.
     * 
     * 1. language, "English" for example<br>
     * 2. short language, "en" for example <br>
     * 3. terminology<br>
     * 
     * <p>
     * The pattern is
     * 
     * <pre>
     *   &lt;languageGrp&gt;
     *     &lt;language type=&quot;{0}&quot; lang=&quot;{1}&quot;/&gt;
     *     &lt;termGrp&gt;
     *       &lt;term&gt;{2}&lt;/term&gt;
     *     &lt;/termGrp&gt;
     *   &lt;/languageGrp&gt;
     * </pre>
     */
    private static final String LANGUAGE_GRP = "<languageGrp><language type=\"{0}\" lang=\"{1}\"/>"
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

    // @Override
    // public String getLanguagePattern()
    // {
    // return LANGUAGE_GRP;
    // }

    @Override
    public String getConcept(String languageGrp, long conceptId)
    {
        return MessageFormat.format(CONCEPT_GRP, getUserName(), UTC
                .valueOf(new Date()), languageGrp);
    }

    @Override
    public String getLanguage(Locale locale, String terminology, String descXML)
    {
        return MessageFormat.format(LANGUAGE_GRP, locale
                .getDisplayLanguage(LOCALE),
                locale.getLanguage().toUpperCase(), terminology, descXML);
    }
}
