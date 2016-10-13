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
package com.globalsight.ling.docproc.extractor.java;

import com.globalsight.ling.docproc.extractor.java.Extractor;

import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.JSEscapeSequence;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistryException;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;

import java.io.UnsupportedEncodingException;

import java.util.Iterator;

/**
 * <P>Implements IParseEvents and responds to the events fired by the
 * Java parser.</P>
 *
 * <p>The Java ExtractionHandler honors GSA comments (see the
 * {@link com.globalsight.ling.docproc.AbstractExtractor
 * AbstractExtractor} class) and lets extraction of strings be guided
 * by them.</p>
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
    private boolean m_whiteToTranslatable = false;
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

    //
    // Constructors
    //

    /**
     * <P>Returns a new handler that knows about its input data, the
     * output object, and the extractor that created it.<P>
     */
    ExtractionHandler (EFInputData p_input, Output p_output,
        Extractor p_extractor)
        throws ExtractorException
    {
        super();

        m_input = p_input;
        m_output = p_output;
        m_extractor = p_extractor;

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

    public void handleWhite(String s)
    {
        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
        {
            if (m_whiteToTranslatable && !m_extractor.exclude())
            {
                m_output.addTranslatable(s);
                try
                {
                    m_output.setTranslatableAttrs(
                        ExtractorRegistry.FORMAT_JAVA, "string");
                }
                catch (DocumentElementException ignore)
                {
                }
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
            if (m_whiteToTranslatable && !m_extractor.exclude())
            {
                m_output.addTranslatable(s);
                try
                {
                    m_output.setTranslatableAttrs(
                        ExtractorRegistry.FORMAT_JAVA, "string");
                }
                catch (DocumentElementException ignore)
                {
                }
            }
            else
            {
                m_output.addSkeleton(s);
            }
        }
    }

    public void handleComment(String s, int commentType)
    {
        try
        {
            switch (commentType)
            {
            case SINGLE_LINE_COMMENT:
                m_extractor.readMetaMarkup(s.substring(2));
                break;
            case MULTI_LINE_COMMENT:
                m_extractor.readMetaMarkup(s.substring(2, s.length() - 2));
                break;
            case CRIPPLED_SINGLE_LINE_COMMENT:
                // fall through, followed by EOF
            default: // do nothing
                break;
            }
        }
        catch (ExtractorException ex)             // a RegExException, really
        {
            // do nothing, means a regex has a typo in it
        }

        if (isEmbeddedExtractor())
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
        }
        else
        {
            m_output.addSkeleton(s);
        }
    }

    public void handleCDO(String s)
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

    public void handleCDC(String s)
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

    public void handleLiteral(String s)
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

    public void handleString(String s)
    {
        // Bug 2133: DAE's Javascript strings contain illegal
        // newlines and we cannot make DAE change them.
        // So instead the grammar reads them and here we delete
        // any newlines in the string. (Use \n for real newlines.)
        s = Text.removeCRNL(s);

        // Decode Java character escapes (\n etc), but don't decode
        // embedded protected \' and \" chars!!
        s = JSEscapeSequence.decodeString(s);

        // We don't do anything if GS_XXX says text is not to be
        // extracted.
        int len = s.length();
        if (m_extractor.exclude() || len <= 2)
        {
            m_output.addSkeleton(s);
            return;
        }

        // The HTML extractor cannot be called in embedded contexts
        if (isEmbeddedExtractor())
        {
            outputEmbeddedString(s);
            return;
        }

        m_output.addSkeleton(s.substring(0, 1));

        String text = s.substring(1, len - 1);

        // Guess if the string contains html code.  Call the HtmlExtractor.
        int pos;
        if (((pos = text.indexOf('<')) != -1) && (text.indexOf('>') > pos))
        {
            try
            {
                // System.err.println("HTML --> " + s);
                switchToHtml(text);
            }
            catch (ExtractorException e)
            {
                // ignore errors and add entire string as translatable
                m_output.addTranslatable(text);

                try
                {
                    m_output.setTranslatableAttrs(
                        ExtractorRegistry.FORMAT_JAVA, "string");
                }
                catch (DocumentElementException ignore)
                {
                }
            }
        }
        else
        {
            m_output.addTranslatable(text);

            try
            {
                m_output.setTranslatableAttrs(
                    ExtractorRegistry.FORMAT_JAVA, "string");
            }
            catch (DocumentElementException ignore)
            {
            }
        }

        m_output.addSkeleton(s.substring(len - 1, len));
    }

    public void handleKeyword(String s)
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

    public void handleOperator(String s)
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


    /**
     * <p>Output a string if the extractor is embedded by
     * encapsulating it in a &lt;sub&gt; TMX tag.</p>
     */
    private void outputEmbeddedString(String s)
    {
        int len = s.length();

        if (len <= 2)                             // empty string
        {
            m_extractor.addToEmbeddedString(makeEmbeddedString(s));
            return;
        }

        // starting quote
        m_extractor.addToEmbeddedString(
            makeEmbeddedString(s.substring(0, 1)));

        // sub tag
        m_extractor.addToEmbeddedString("<sub locType=\"translatable\"");
        m_extractor.addToEmbeddedString(" datatype=\"" +
            ExtractorRegistry.FORMAT_JAVA + "\" type=\"string\">");
        m_extractor.addToEmbeddedString(m_xmlEncoder.encodeStringBasic(
            s.substring(1, len - 1)));
        m_extractor.addToEmbeddedString("</sub>");

        // ending quote
        m_extractor.addToEmbeddedString(
            makeEmbeddedString(s.substring(len - 1, len)));
    }


    /**
     * <p>Switches the extractor to HTML to handle a stretch of HTML
     * codes embedded in the Java input.
     *
     * @param p_input: the input string that the extractor will parse
     */
    private void switchToHtml(String p_input)
        throws ExtractorException
    {
        Output output = m_extractor.switchExtractor(p_input, "html");
        Iterator it = output.documentElementIterator();

        while (it.hasNext())
        {
            DocumentElement element = (DocumentElement)it.next();
            switch (element.type())
            {
            case DocumentElement.SKELETON:
                SkeletonElement s = (SkeletonElement)element;
                m_output.addSkeletonTmx(s.getSkeleton());
                break;
            case DocumentElement.TRANSLATABLE: // fall through
            case DocumentElement.LOCALIZABLE:
                Segmentable o = (Segmentable)element;
                o.setDataType("html");
                // fall through
            default:
                m_output.addDocumentElement(element);
                break;
            }
        }
    }
}
