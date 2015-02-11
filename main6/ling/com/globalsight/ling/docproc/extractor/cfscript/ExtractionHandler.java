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
package com.globalsight.ling.docproc.extractor.cfscript;

import com.globalsight.ling.docproc.extractor.cfscript.Extractor;

import com.globalsight.ling.common.EncodingChecker;
import com.globalsight.ling.common.CFEscapeSequence;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * <P>Implements IParseEvents and responds to the events fired by the
 * CFScript parser.</P>
 *
 * <p>The CFScript ExtractionHandler honors GSA comments (see the
 * {@link com.globalsight.ling.docproc.AbstractExtractor
 * AbstractExtractor} class) and lets extraction of strings be guided
 * by them.</p>
 */
// derived from JavaScript parser
public class ExtractionHandler
    implements IParseEvents, IFormatNames
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
     *
     * Note: in case of CFScript, quotes mustn't be escaped twice.
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
                    m_output.setTranslatableAttrs(FORMAT_CFSCRIPT, "string");
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
                    m_output.setTranslatableAttrs(FORMAT_CFSCRIPT, "string");
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
                m_extractor.readMetaMarkup(s.substring(3));
                break;
            case MULTI_LINE_COMMENT:
                m_extractor.readMetaMarkup(s.substring(3, s.length() - 3));
                break;
            case CRIPPLED_SINGLE_LINE_COMMENT:
                // fall through, followed by EOF
            default: // do nothing
                break;
            }
        }
        catch (ExtractorException ex)             // a RegExException, really
        {
            // Do nothing, means a regex has a typo in it.
            // Incorrect: these regexes are user-specified.
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
        // Donno if CFScript allows newlines inside strings.
        // s = Text.removeCRNL(s);

        // The HTML extractor cannot be called in embedded contexts,
        // and we don't do anything if GSA says text is not to be
        // extracted.
        if (isEmbeddedExtractor())
        {
            outputEmbeddedString(s);
            return;
        }

        int len = s.length();
        if (len <= 2 || m_extractor.exclude()) // empty string
        {
            m_output.addSkeleton(s);
            return;
        }

        String outerQuote = s.substring(0, 1);
        String text = s.substring(1, len - 1);

        if (Text.isBlank(text) || isCfVariable(text))
        {
            m_output.addSkeleton(s);
            return;
        }

        m_output.addSkeleton(outerQuote);

        // Tokenize the string to find out where "#exp#" expressions
        // appear. Then output this inside a placeholder.
        ArrayList tokens = tokenizeString(text);
        for (int i = 0; i < tokens.size(); ++i)
        {
            text = (String)tokens.get(i);

            if (isCfVariable(text))
            {
                m_output.addTranslatableTmx("<ph type=\"expression\">");
                m_output.addTranslatableTmx(text);
                m_output.addTranslatableTmx("</ph>");
                continue;
            }

            // Decode CFScript escaped (doubled) ', " and # chars.
            text = CFEscapeSequence.decodeString(text, outerQuote);

            // Guess if the string contains html code. Call the HtmlExtractor.
            int pos;
            if (((pos = text.indexOf('<')) != -1) && (text.indexOf('>') > pos))
            {
                try
                {
                    // System.err.println("HTML --> " + text);
                    switchToHtml(text, outerQuote);
                }
                catch (ExtractorException e)
                {
                    // ignore errors and add entire string as translatable
                    m_output.addTranslatable(text);

                    try
                    {
                        m_output.setTranslatableAttrs(FORMAT_CFSCRIPT, "string");
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
                    m_output.setTranslatableAttrs(FORMAT_CFSCRIPT, "string");
                }
                catch (DocumentElementException ignore)
                {
                }
            }
        }

        m_output.addSkeleton(outerQuote);
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

        //try
        {
            result = m_xmlEncoder.encodeStringBasic(
                /*m_parentEncoder.encode*/(s));
        }
        //catch (NativeEnDecoderException ex)
        {
            // TODO: ignore for now
            //  ex.printStackTrace();
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
        m_extractor.addToEmbeddedString(" datatype=\"");
        m_extractor.addToEmbeddedString(FORMAT_CFSCRIPT);
        m_extractor.addToEmbeddedString("\" type=\"string\">");
        m_extractor.addToEmbeddedString(m_xmlEncoder.encodeStringBasic(
            s.substring(1, len - 1)));
        m_extractor.addToEmbeddedString("</sub>");

        // ending quote
        m_extractor.addToEmbeddedString(
            makeEmbeddedString(s.substring(len - 1, len)));
    }


    /**
     * <p>Switches the extractor to HTML to handle a stretch of HTML
     * codes embedded in a CFScript string.</p>
     *
     * If skeleton is produced, quotes must be escaped (doubled).
     *
     * @param p_input: the input string that the extractor will parse.
     */
    private void switchToHtml(String p_input, String p_outerQuote)
        throws ExtractorException
    {
        Output output = m_extractor.switchExtractor(p_input, FORMAT_HTML);
        Iterator it = output.documentElementIterator();

        while (it.hasNext())
        {
            Segmentable o;
            DocumentElement element = (DocumentElement)it.next();
            switch (element.type())
            {
            case DocumentElement.SKELETON:
                SkeletonElement s = (SkeletonElement)element;
                m_output.addSkeletonTmx(
                    doubleQuotes(s.getSkeleton(), p_outerQuote));
                break;
            default:
                m_output.addDocumentElement(element);
                break;
            }
        }
    }

    /**
     * Fixes a string so it can be output in a CFScript skeleton: a
     * GXML skeleton (with &quot) is decoded to restore the original
     * quote ", then the quote is doubled, and finally the string is
     * re-encoded as XML.
     */
    private String doubleQuotes(String s, String p_outerQuote)
    {
        return m_xmlEncoder.encodeStringBasic(CFEscapeSequence.encodeString(
            m_xmlEncoder.decodeStringBasic(s), p_outerQuote));
    }

    /**
     * Returns true if the string contains a variable replacement and
     * nothing else: "#var#". The incoming string must have the outer
     * quotes removed.
     */
    private boolean isCfVariable(String s)
    {
        if (s.length() < 2) return false;

        int i = 0, max = s.length() - 1;

        // Recognizes the regexp "^#[^#]+#$".

        if (s.charAt(i++) != '#')
        {
            return false;
        }

        if (s.charAt(i++) == '#')
        {
            return false;
        }

        while (i < max && s.charAt(i++) != '#') { /* do nothing */ }

        if (i == max && s.charAt(max) != '#')
        {
            return false;
        }

        return true;
    }

    /**
     * <P>Extracts inline CF expressions "#var#" from a given string.
     * This function works very much like RE.split(), splitting a
     * string at regexp boundaries, except that it also returns the
     * boundaries (the CF expressions).</P>
     *
     * <P>Example: "A hash ## is doubled, a #variable# is ###not#."
     * Result:
     * 1) "A hash ## is doubled, a "
     * 2) "#variable#"
     * 3) " is ##"
     * 4) "#not#"
     * 5) "."
     *
     * @return an array of strings
     */
    private ArrayList tokenizeString(String s)
    {
        StringTokenizer tok = new StringTokenizer(s, "#", true);
        int max = tok.countTokens();

        ArrayList result = new ArrayList(max);

        if (max == 1)
        {
            result.add(s);
            return result;
        }

        StringBuffer temp = new StringBuffer();
        boolean seenHash = false;

        for (int i = 0; i < max; ++i)
        {
            String t = tok.nextToken();

            if (seenHash == true)
            {
                if (t.equals("#"))
                {
                    temp.append(t);
                    seenHash = false;
                }
                else
                {
                    temp.deleteCharAt(temp.length() - 1);
                    result.add(temp.toString());
                    temp.setLength(0);
                    temp.append("#");
                    temp.append(t);

                    if (tok.hasMoreTokens())
                    {
                        temp.append(tok.nextToken()); // must be "#"
                        ++i;
                    }

                    result.add(temp.toString());
                    temp.setLength(0);

                    seenHash = false;
                }
            }
            else
            {
                temp.append(t);

                if (t.equals("#"))
                {
                    seenHash = true;
                }
            }
        }

        result.add(temp.toString());

        /*
        for (int i = 0; i < result.size(); ++i)
        {
            System.err.println("tok " + i + " = " + (String)result.get(i));
        }
        */

        return result;
    }
}
