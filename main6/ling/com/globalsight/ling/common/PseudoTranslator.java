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
package com.globalsight.ling.common;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.Character;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

public final class PseudoTranslator
    implements DiplomatBasicHandler
{
    private boolean m_bInTag;
    private StringBuffer m_pseudo;
    private PseudoParameters m_parameters;
    private DiplomatBasicParser m_parser;

    private static final String SUB = DiplomatNames.Element.SUB;
    private static final long SEED = 1234567890;
    private static final Random c_randomizer = new Random(SEED);
    private static final Hashtable c_localeToPseudoParametersMap =
        mapLocaleToPseudoParameters();

    private static final Hashtable mapLocaleToPseudoParameters()
    {
        Hashtable h = new Hashtable();
        h.put(Locale.ENGLISH, new PseudoParameters(1.0, 0x007A, 0x0021, true));
        h.put(Locale.FRENCH, new PseudoParameters(1.2, 0x00FF, 0x0021, true));
        h.put(Locale.GERMAN, new PseudoParameters(1.5, 0x00FF, 0x0021, true));
        h.put(Locale.JAPANESE, new PseudoParameters(0.75, 0x9F9F, 0x31A1, false));
        h.put(Locale.KOREAN, new PseudoParameters(0.80, 0xD79F, 0xAC00, true));
        h.put(Locale.SIMPLIFIED_CHINESE, new PseudoParameters(0.5, 0x9F9F, 0x3400, false));
        h.put(Locale.TRADITIONAL_CHINESE, new PseudoParameters(0.6, 0x9F9F, 0x3400, false));
        h.put(new Locale("ar", ""), new PseudoParameters(0.7, 0x06FE, 0x0621, true)); // Arabic
        h.put(new Locale("iw", "IL"), new PseudoParameters(0.7, 0x05EA, 0x05D0, true)); // Hebrew
        h.put(new Locale("ru", "RU"), new PseudoParameters(1.2, 0x04F5, 0x0400, true)); // Russian
        return h;
    }

    //
    // Constructor
    //
    public PseudoTranslator()
    {
        m_parser = new DiplomatBasicParser(this);
    }

    //
    // Public Methods
    //
    public String makePseudoTranslation(String p_gxml, Locale p_locale)
        throws DiplomatBasicParserException
    {
        m_pseudo = new StringBuffer();
        m_parameters =
            (PseudoParameters)c_localeToPseudoParametersMap.get(p_locale);

        if (m_parameters == null) // locale not found, use default settings
        {
            m_parameters = new PseudoParameters(1.0, 0x0021, 0x00FF, true);
        }

        m_parser.parse(p_gxml);

        return m_pseudo.toString();
    }

    public void handleEndTag(String p_name, String p_originalTag)
        throws DiplomatBasicParserException
    {
        m_bInTag = (p_name.compareTo(SUB) == 0);
        m_pseudo.append(p_originalTag);
    }

    public void handleStart()
        throws DiplomatBasicParserException
    {
    }

    public void handleStop()
        throws DiplomatBasicParserException
    {
    }

    public void handleStartTag(String p_name, Properties p_atributes,
        String p_originalString)
        throws DiplomatBasicParserException
    {
        m_bInTag = (p_name.compareTo(SUB) != 0);
        m_pseudo.append(p_originalString);
    }

    public void handleText(String p_text)
        throws DiplomatBasicParserException
    {
        if (m_bInTag)
        {
            m_pseudo.append(p_text);
        }
        else
        {
            m_pseudo.append(pseudoize(p_text));
        }
    }

    private String pseudoize(String p_string)
    {
        StringWriter out = new StringWriter();
        int newLength = (int)(p_string.length() * m_parameters.m_growthFactor);
        int interval = m_parameters.m_rangeUp - m_parameters.m_rangeDown+1;
        boolean bHadOneChar = false;

        for (int i = 0; i < p_string.length(); i++)
        {
            if (Character.isLetter(p_string.charAt(i)))
            {
                double f = c_randomizer.nextDouble();
                bHadOneChar = true;
                while(f < m_parameters.m_growthFactor)
                {
                    out.write(m_parameters.m_rangeDown +
                        Math.abs(c_randomizer.nextInt()%interval));
                    f += 1;
                }
            }
            else if (m_parameters.m_bKeepSpace ||
                !Character.isWhitespace(p_string.charAt(i)))
            {
                out.write(p_string.charAt(i));
            }
        }

        // makes sure there is a least one char if the original string
        // wasn't empty...
        if (out.toString().length() == 0 && bHadOneChar)
        {
            out.write(m_parameters.m_rangeDown +
                Math.abs(c_randomizer.nextInt()%interval));
        }

        out.flush();
        return out.toString();
    }
}
