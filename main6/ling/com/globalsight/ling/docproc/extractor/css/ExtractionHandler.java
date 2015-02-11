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
package com.globalsight.ling.docproc.extractor.css;

import com.globalsight.ling.docproc.extractor.css.ExtractionRules;

import com.globalsight.ling.common.CssEscapeSequence;
import com.globalsight.ling.common.XmlEntities;

import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistryException;
import com.globalsight.ling.docproc.Output;

/**
 * <P>ExtractionHandler implements IParseEvents and responds to the
 * events fired by the CSS parser.  This class is not prepared to be
 * called from an embedded Extractor, it will always generate output
 * containing &lt;skeleton&gt; and &lt;localizable&gt; tags.  Use the
 * StyleExtractor and StyleExtractionHandler class for styles embedded
 * in a &lt;tag style="..."&gt; style attribute.
 *
 * <P>For translation and localization purposes, the following parts
 * of a CSS stylesheet will be added to the "localizable" section:
 * <UL>
 * <LI>names of imported files in @import sections
 * <LI>font names in @font-face sections
 * <LI>selectors that select a locale (e.g., P:lang(fr))<BR>
 *     note that such selectors might have to be introduced as well
 * <LI>values of declarations that select locale-dependent values
 *     for their attributes, e.g.,
 *     "HTML:lang(de) { quotes: '»' '«' '\2039' '\203A' }"
 * </UL>
 *
 * <P>Everything else is treated as skeleton.
 *
 * @see StyleExtractor
 * @see StyleExtractionHandler
 */
public class ExtractionHandler
    implements IParseEvents
{
    //
    // Private Transient Member Variables
    //

    private Output m_output = null;
    private EFInputData m_input = null;
    private Extractor m_extractor = null;
    private ExtractionRules m_rules = null;
    private boolean b_isLocalizable = false;
    private boolean b_whiteToLocalizable = false;
    private String m_localizationType = null;

    //
    // Constructors
    //

    /**
     * <P>Returns a new handler that knows about its input data, the
     * output object, and the extractor that created it.
     */
    ExtractionHandler (EFInputData p_input, Output p_output,
        Extractor p_extractor, ExtractionRules p_rules)
    {
        super();

        m_input = p_input;
        m_output = p_output;
        m_extractor = p_extractor;
        m_rules = p_rules;
    }


    //
    // Interface Implementation -- IParseEvents
    //

    public void handleStart() {}
    public void handleFinish() {}

    public void handleWhite(String s)
    {
        if (b_whiteToLocalizable)
        {
            m_output.addLocalizable(s);           // nothing to encode
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleEndOfLine(String s)
    {
        if (b_whiteToLocalizable)
        {
            m_output.addLocalizable(s);           // nothing to encode
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleComment(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleCDO(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleCDC(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartCharset(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleCharset(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleEndCharset(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartFontFace(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleEndFontFace(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartImport(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleImport(String s)
    {
        s = CssEscapeSequence.cleanupStringToken(s);

        m_output.addSkeleton(s.substring(0, 1));

        int len = s.length();
        String text = s.substring(1, len - 1);

        m_output.addLocalizable(CssEscapeSequence.decodeString(text));

        try
        {
            m_output.setLocalizableAttrs(
              ExtractorRegistry.FORMAT_CSS, "style-url");
        }
        catch (Throwable ex) {}

        m_output.addSkeleton(s.substring(len - 1, len));
    }

    public void handleImportURI(String s)
    {
        m_output.addLocalizable(CssEscapeSequence.decodeString(s));

        try
        {
            m_output.setLocalizableAttrs(
              ExtractorRegistry.FORMAT_CSS, "style-url");
        }
        catch (Throwable ex) {}
    }

    public void handleEndImport(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartMedia(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleMedia(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleEndMedia(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartAtRule(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartBlock(String s)
    {
        if (b_isLocalizable)
        {
            try
            {
                m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_CSS,
                  m_localizationType);
            }
            catch (Throwable ex)
            {
            }
        }

        m_output.addSkeleton(s);

        b_isLocalizable = false;
        b_whiteToLocalizable = false;
    }

    public void handleEndBlock(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleStartDeclarations(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleEndDeclarations(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleToken(String s)
    {
        if (b_isLocalizable)
        {
            s = CssEscapeSequence.cleanupStringToken(s);
            m_output.addLocalizable(CssEscapeSequence.decodeString(s));
            b_whiteToLocalizable = true;
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleDelimiter(String s)
    {
        if (b_isLocalizable)
        {
            m_output.addLocalizable(CssEscapeSequence.decodeString(s));
            b_whiteToLocalizable = true;
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleFunction(String s)
    {
        if (b_isLocalizable)
        {
            m_output.addLocalizable(CssEscapeSequence.decodeString(s));
            b_whiteToLocalizable = true;
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleStyle(String s)
    {
        m_output.addSkeleton(s);

        if (m_rules.canLocalize(s) && m_rules.isLocalizable(s))
        {
            m_localizationType = m_rules.getLocalizationType(s);
            b_isLocalizable = true;
        }
    }

    public void handleStartValues(String s)
    {
        m_output.addSkeleton(s);
    }

    public void handleEndValues()
    {
        if (b_isLocalizable)
        {
            try
            {
                m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_CSS,
                  m_localizationType);
            }
            catch (Throwable ex)
            {
            }
        }

        b_isLocalizable = false;
        b_whiteToLocalizable = false;
    }
}
