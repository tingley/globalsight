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

import com.globalsight.ling.common.CssEscapeSequence;
import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.XmlEntities;

import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistryException;
import com.globalsight.ling.docproc.Output;

import java.io.UnsupportedEncodingException;

/**
 * <P>ExtractionHandler implements IParseEvents and responds to the
 * events fired by the CSS parser.  This class only extracts styles
 * from a &lt;tag style="..."&gt; style attribute and can be used with
 * an embedded extractor.</P>
 *
 * <P>If the extractor is embedded, this class will not produce
 * &lt;skeleton&gt; tags and wrap all loclizable pieces in &lt;sub
 * locType=localizable&gt tags.  If the extractor is not embedded, the
 * standard skeleton/localizable tags are generated.</P>
 */
public class StyleExtractionHandler
    implements IParseEvents
{
    //
    // Private Transient Member Variables
    //

    private Output m_output = null;
    private EFInputData m_input = null;
    private StyleExtractor m_extractor = null;
    private ExtractionRules m_rules = null;
    private boolean b_isLocalizable = false;
    private boolean b_whiteToLocalizable = false;
    private XmlEntities m_xmlEncoder = new XmlEntities ();

    /**
     * <p>The encoder for skeleton inside embedded pieces.  When an
     * embedded extractor writes skeleton, it outputs to a string that
     * the parent extractor adds to the content of a &lt;bpt&gt; tag -
     * without further escaping.  So an embedded extractor must first
     * use the parent's encoder to escape special parent chars, and
     * then call the standard XML encoder.</p>
     */
    private NativeEnDecoder m_parentEncoder = null;

    /**
     * <P>Holds the style type when outputting style values and the
     * current style is localizable.  This is output as the
     * &lt;localizable type="..."&gt; or &lt;sub type="..."&gt;
     * attribute.</P>
     */
    private String m_localizationType = null;

    //
    // Constructors
    //

    /**
     * <P>Returns a new handler that knows about its input data, the
     * output object, and the extractor that created it.</P>
     */
    StyleExtractionHandler (EFInputData p_input, Output p_output,
      StyleExtractor p_extractor, ExtractionRules p_rules)
        throws ExtractorException
    {
        super();

        m_input = p_input;
        m_output = p_output;
        m_extractor = p_extractor;
        m_rules = p_rules;

        if (isEmbeddedExtractor())
        {
            m_parentEncoder =
                m_extractor.getEncoder(m_extractor.getParentType());
        }
    }


    //
    // Interface Implementation -- IParseEvents
    //

    public void handleStart() {}
    public void handleFinish() {}
    public void handleCDO(String s) {}
    public void handleCDC(String s) {}
    public void handleStartCharset(String s) {}
    public void handleCharset(String s) { }
    public void handleEndCharset(String s) { }
    public void handleStartFontFace(String s) {}
    public void handleEndFontFace(String s) {}
    public void handleStartImport(String s) {}
    public void handleImport(String s) {}
    public void handleImportURI(String s) {}
    public void handleEndImport(String s) {}
    public void handleStartMedia(String s) {}
    public void handleMedia(String s) {}
    public void handleEndMedia(String s) {}
    public void handleStartAtRule(String s) {}
    public void handleStartBlock(String s) {}
    public void handleEndBlock(String s) {}
    public void handleStartDeclarations(String s) {}
    public void handleEndDeclarations(String s) {}

    public void handleWhite(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
        {
            if (b_whiteToLocalizable)
            {
                m_output.addLocalizable(s);       // nothing to encode
            }
            else
            {
                m_output.addSkeleton(s);
            }
        }
    }

    public void handleEndOfLine(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
        {
            if (b_whiteToLocalizable)
            {
                m_output.addLocalizable(s);       // nothing to encode
            }
            else
            {
                m_output.addSkeleton(s);
            }
        }
    }

    public void handleComment(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleToken(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
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
    }

    public void handleDelimiter(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
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
    }

    public void handleFunction(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
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

        if (b_isLocalizable)
        {
            b_whiteToLocalizable = true;
        }
    }

    public void handleStyle(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
        {
            m_output.addSkeleton(s);
        }

        if (m_rules.canLocalize(s) && m_rules.isLocalizable(s))
        {
            m_localizationType = m_rules.getLocalizationType(s);
            b_isLocalizable = true;
        }
    }

    public void handleStartValues(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));

            if (b_isLocalizable)
            {
                m_extractor.addToEmbeddedString(
                  "<sub locType=\"localizable\" datatype=\"" +
                  ExtractorRegistry.FORMAT_CSS_STYLE + "\"");
                m_extractor.addToEmbeddedString(
                  " type=\"" + m_localizationType + "\">");
            }
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleEndValues()
    {
        if (isEmbeddedExtractor() && b_isLocalizable)
        {
            m_extractor.addToEmbeddedString("</sub>");
        }
        else if (b_isLocalizable)
        {
            try
            {
                m_output.setLocalizableAttrs(
                  ExtractorRegistry.FORMAT_CSS_STYLE, m_localizationType);
            }
            catch (Throwable ex)
            {
                // should not fail
            }
        }

        b_isLocalizable = false;
        b_whiteToLocalizable = false;
    }


    //
    // Private Methods
    //

    /**
     * @return <code>true</code> if this instance is an embedded
     * extractor, <code>false</code> if it is a top-level, standalone
     * extractor.
     */
    private boolean isEmbeddedExtractor()
    {
        return m_extractor.isEmbedded();
    }

    /**
     * Embedded strings go into a BPT tag of the parent extractor and
     * must be encoded according to the parent format, and then
     * according to the XML format.
     */
    private String makeEmbeddedString(String s)
    {
        String result = s;

        try
        {
            result = m_xmlEncoder.encodeStringBasic(m_parentEncoder.encode(s));
        }
        catch (NativeEnDecoderException ex)
        {
            // TODO: ignore for now
            ex.printStackTrace();
        }

        return result;
    }
}
