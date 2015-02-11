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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.edit.offline.page.TerminologyHelp;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Hitlist;
import com.globalsight.terminology.exporter.HtmlWriter;
import com.globalsight.util.UTC;
import com.globalsight.util.XmlParser;

/**
 * <code>TxtTermHelp</code> is a concrete class for <code>TermHelp</code>, using
 * TEXT pattern to format terminology.
 */
public class TxtTermHelp extends TermHelp implements TerminologyHelp
{
    private static final String SEPARATOR = "\t";
    private static final String NEW_LINE = "\n";

    /**
     * Generates the content between the body tag.
     * <p>
     * The content will be formated as xml, so the content was wraped before
     * formating and unwraped after formating.
     */
    @Override
    public String getContent()
    {
        StringBuilder concepts = new StringBuilder();

        for (Hitlist.Hit src : getSortedSource())
        {
            String srcTerm = src.getTerm();
            HashSet<String> targets = getEntries().get(src);

            for (String target : targets)
            {
                concepts.append(srcTerm).append(SEPARATOR).append(target)
                        .append(NEW_LINE);
            }
        }

        return concepts.toString();
    }

    @Override
    public String getEnd()
    {
        return "";
    }

    @Override
    public String getHead()
    {
        return "";
    }

    @Override
    public String getConcept(String languageGrp, long conceptId)
    {
        return null;
    }

    public TxtTermHelp()
    {
        super();
        setFormat(false);
    }

    @Override
    public String getLanguage(Locale locale, String terminology, String xml)
    {
        return locale.toString();
    }
}
