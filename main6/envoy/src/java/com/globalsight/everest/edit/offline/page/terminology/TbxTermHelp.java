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
import java.util.List;
import java.util.Locale;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.edit.offline.page.TerminologyHelp;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;

public class TbxTermHelp extends TermHelp implements TerminologyHelp
{
	
	private static final String START = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
			"<!DOCTYPE martif PUBLIC 'ISO 12200:1999A//DTD MARTIF core (DXFcdV04)//EN' 'TBXcdv04.dtd'>\n" +
			"<martif type=\"TBX\" xml:lang=\"" + LOCALE.getDisplayLanguage(LOCALE) + "\">\n" +
			"<martifHeader>\n<fileDesc>\n<sourceDesc>\n<p>from GlobalSight termBase</p>\n" +
			"</sourceDesc>\n</fileDesc>\n<encodingDesc>\n" +
	        "<p type=\"DCSName\">SYSTEM 'xcs.xml'</p>" +
			"</encodingDesc>\n</martifHeader>\n<text>\n<body>\n";
	
	private static final String END = "</body>\n</text>\n</martif>";
	
	private static final String ENTRY_GRP = "<termEntry id=\"{0}\">{1}</termEntry>";
	
	private static final String LANGUAGE_GRP = "<langSet xml:lang=\"{0}\">\n<ntig>\n<termGrp>\n<term>{1}</term>\n</termGrp>\n</ntig>\n</langSet>";

	public String getConcept(String languageGrp, long conceptId) {
		return MessageFormat.format(ENTRY_GRP, conceptId, languageGrp);
	}

	public String getEnd() {
		return END;
	}

	public String getHead() {
		return START;
	}

	public String getLanguage(Locale locale, String terminology, String xml) {
		return MessageFormat.format(LANGUAGE_GRP, locale.getLanguage(), terminology);
	}

    public String convert(List<TermLeverageMatchResult> terms,
            Locale srcLocale, Locale trgLocale)
    {
        init(terms, srcLocale, trgLocale);

        StringBuffer content = new StringBuffer();
        content.append(getHead());
        content.append(getContent());
        content.append(getEnd());

        String result = content.toString();
        if (super.getFormat()) {
            result = XmlUtil.format(result, XmlUtil.getNullEntityResolver());
        }

        return result;
    }
}
