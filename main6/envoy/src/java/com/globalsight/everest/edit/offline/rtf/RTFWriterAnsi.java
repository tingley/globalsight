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

package com.globalsight.everest.edit.offline.rtf;

import com.globalsight.ling.tm.LeverageType;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.ling.common.LCID;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.Text;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.lang.StringBuffer;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * <p>Takes an OfflinePageData input object (in p-tag format) and generates an
 * RTF file that can betranslated using TRADOS Translator's Workbench.</p>
 *
 * <p><code># num</code> markers and other <code>#</code>-escapes are
 * protected from editing by the style <code>tw4winExternal</code>,
 * and p-tags embedded by <code>[...]</code> inside a segment are
 * marked up as segment-internal tags (<code>tw4winInternal</code>).
 * Segment-internal tags cannot be translated but are stored in the TM
 * as part of the segment.</p>
 *
 * <p>Input is obtained from the OfflinePageData object, the
 * output is written as ASCII characters to an OutputStream.  Unicode
 * or double-byte characters are converted to the proper RTF codes
 * as necessary.  Note that RTF files are always ASCII files.</p>
 *
 * <p><b>Background info:</b> Translator's Workbench marks up input
 * files using the character styles <code>tw4winInternal</code> and
 * <code>tw4winExternal</code>.  (There are other styles but they are
 * of no concern for the discussion here.)  Text marked up as
 * <code>tw4winExternal</code> is not considered translatable text,
 * like a <code>&lt;skeleton&gt;</code> section in a Diplomat TMX
 * file.  Text marked up as <code>tw4winInternal</code> is considered
 * part of a segment, but non-translatable, like Diplomat XML's
 * <code>&lt;bpt&gt;...&lt;ept&gt;</code> tags.</p>
 */
public class RTFWriterAnsi
{
    //
    // Public Constants
    //

    /**
     * A string identifying this product.  Used in the info section of
     * the generated RTF file.
     */
    public static final String PRODUCTNAME = "GlobalSight System 4";
    public static final String DOCUMENTTITLE = "System 4 RTF Export";

    //
    // Private & Protected Constants
    //

    private static final String RTF_HEADER_START =
        "\\rtf1\\ansi\\ansicpg1252\\uc0\\deflang1033\\deflangfe1033\\deff0";

    private static final String RTF_FONT_TABLE_START =
        "{\\fonttbl";

    // f0 and f1 are source lang font and target lang font
    private static final String RTF_FONT_TABLE_END =
        "{\\f2\\fmodern\\fcharset0\\fprq1{\\*\\panose 02070309020205020404}Courier New;}" +
        "{\\f3\\fswiss\\fcharset0\\fprq2 Arial;}" +
        "{\\f4\\froman\\fcharset0\\fprq2{\\*\\panose 02020603050405020304} Times New Roman;}" +
        "{\\f5\\fswiss\\fcharset128\\fprq2{\\*\\panose 020b0604020202020204} Arial Unicode MS;}" +
        "}";

    private static final String RTF_STYLESHEET_START =
        "{\\stylesheet" +
        "{\\ql \\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 ";

    private static final String RTF_STYLESHEET_END =
        " \\cgrid\\loch\\f1\\hich\\af0\\dbch\\af16\\fs24 \\snext0 Normal;}" +
        "{\\*\\cs10 \\additive Default Paragraph Font;}" +
        "{\\*\\cs1 \\additive \\v\\f3\\fs24\\sub\\cf11 tw4winMark;}" +
        "{\\*\\cs2 \\additive \\cf3\\fs40\\f3 tw4winError;}" +
        "{\\*\\cs3 \\additive \\f3\\cf10\\lang1024 tw4winPopup;}" +
        "{\\*\\cs4 \\additive \\f3\\cf9\\lang1024 tw4winJump;}" +
        "{\\*\\cs5 \\additive \\f3\\fs24\\cf15\\lang1024 tw4winExternal;}" +
        "{\\*\\cs6 \\additive \\f3\\fs24\\cf6\\lang1024 tw4winInternal;}" +
        "{\\*\\cs7 \\additive \\cf1 tw4winTerm;}" +
        "{\\*\\cs8 \\additive \\f3\\cf12\\lang1024 DO_NOT_TRANSLATE;}" +
        "{\\*\\cs9 \\additive \\fs16 \\sbasedon10 annotation ref;}" +
        "{\\s10 \\ql\\fs20\\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0  annotation txt}" +
        "}";

    private static final String COLOR_TABLE =
        "{\\colortbl;\r\n" +
        "\\red0\\green0\\blue0;\r\n" +
        "\\red0\\green0\\blue255;\r\n" +
        "\\red0\\green255\\blue255;\r\n" +
        "\\red0\\green255\\blue0;\r\n" +
        "\\red255\\green0\\blue255;\r\n" +
        "\\red255\\green0\\blue0;\r\n" +
        "\\red255\\green255\\blue0;\r\n" +
        "\\red255\\green255\\blue255;\r\n" +
        "\\red0\\green0\\blue128;\r\n" +
        "\\red0\\green128\\blue128;\r\n" +
        "\\red0\\green128\\blue0;\r\n" +
        "\\red128\\green0\\blue128;\r\n" +
        "\\red128\\green0\\blue0;\r\n" +
        "\\red128\\green128\\blue0;\r\n" +
        "\\red128\\green128\\blue128;\r\n" +
        "\\red192\\green192\\blue192;\r\n" +
        "}";

    private static final String TW4WIN_MARK     = "\\cs1";
    private static final String TW4WIN_EXTERNAL = "\\cs5";
    private static final String TW4WIN_INTERNAL = "\\cs6";
    private static final String TW4WIN_DO_NOT_TRANSLATE = "\\cs8";
    private static final String ANNOTATION_REF = "\\cs9";
    private static final String ANNOTATION_TXT = "\\s10";


    private static final String FONT_SOURCE  = "\\f0";
    private static final String FONT_TARGET  = "\\f1";
    private static final String FONT_COURIER = "\\f2";
    private static final String FONT_ARIAL   = "\\f3";
    private static final String FONT_TIMES   = "\\f4";
    private static final String FONT_ARIAL_UNICODE = "\\f5";

    private static final String COLOR_BLACK      = "\\cf1";
    private static final String COLOR_BLUE       = "\\cf2";
    private static final String COLOR_GREEN      = "\\cf4";
    private static final String COLOR_RED        = "\\cf6";
    private static final String COLOR_DARK_GREEN = "\\cf11";
    private static final String COLOR_GREY       = "\\cf15";

    private static final String SIZE_10PT = "\\fs20";
    private static final String SIZE_12PT = "\\fs24";

    private static final String NO_PROOF  = "\\noproof";
    private static final String HIDDEN  = "\\v";

    private static final char NORMALIZED_LINEBREAK = '\n';

    //
    // Private Member Variables
    //

    /**
     * A resource bundle that contains our RTF strings.
     */
    ResourceBundle res;

    /**
     * The info section of the RTF header.  Specifies document title,
     * author, creation and revision dates etc.
     */
    private String m_strRTFInfoGroup;
    private String m_strEOL = "\r\n";

    private int m_iSourceLcid = 1033;
    private int m_iTargetLcid = 1033;

    private boolean m_sourceIsRtl = false;
    private boolean m_targetIsRtl = false;

    // language of "# id" markers etc, always en_US
    private final String m_strDefaultLcid =
        "\\lang1033\\langfe1033\\cgrid\\langnp1033\\langfenp1033";
    // language of source text
    private String m_strSourceLcid  = m_strDefaultLcid;
    // language of target text
    private String m_strTargetLcid  = m_strDefaultLcid;

    /** Shorthand for the default document style */
    private String m_strDefaultStyle;
    /** Shorthand for the style of normal text (source segments). */
    private String m_strSourceTextStyle;
    /** Shorthand for the style of normal text (target segments). */
    private String m_strTargetTextStyle;
    /** Shorthand for the style of internal tags (tw4winInternal). */
    private String m_strInternalStyle;
    /** Shorthand for the style of external tags (tw4winExternal). */
    private String m_strExternalStyle;
    /** Shorthand for Workbench segment markers (tw4winMark). */
    private String m_strMarkerStyle;

    private String m_sourceEncoding;
    private final String m_ISOEncoding =
        CodesetMapper.getJavaEncoding("iso-8859-1");

    /**
     * The parameter which enables Trados output.
     */
    private boolean m_isTradosRtf;

    /**
     * The input page object that we convert.
     */
    private OfflinePageData m_page;

    /**
     * The output stream to which we write the result.
     */
    private OutputStreamWriter m_outputStream;

    //
    // Constructors
    //

    /**
     * Initializes an RTFWriter with an OfflinePageData object.
     */
    public RTFWriterAnsi ()
    {
        res = ResourceBundle.getBundle(
          "com.globalsight.everest.edit.offline.rtf.AnsiRTF");
    }

    //
    // Public Methods
    //

    /**
     * Sets the page object to convert.
     * Call {@link #writeRTF(OutputStream)} to run the conversion.
     */
    public void setOfflinePageData(OfflinePageData page, boolean isTradosRtf)
    {
        m_page = page;
        m_isTradosRtf = isTradosRtf;
    }

    /**
     * Runs the conversion and writes RTF to the output stream.  The
     * page must have been initialized by {@link
     * #setOfflinePageData(OfflinePageData) setOfflinePageData()}.
     */
    public void writeRTF(OutputStream outputStream)
        throws IOException,
               RegExException,
               AmbassadorDwUpException
    {
        m_outputStream = new OutputStreamWriter (outputStream, "ASCII");

        writeRTF();

        m_outputStream.flush();
    }

    /**
     * Runs the conversion and writes RTF to the output stream.
     */
    public void writeRTF(OfflinePageData page, OutputStream outputStream,
        boolean isTradosRtf )
        throws IOException,
               RegExException,
               AmbassadorDwUpException
    {
        m_outputStream = new OutputStreamWriter (outputStream, "ASCII");
        m_page = page;
        m_isTradosRtf = isTradosRtf;

        writeRTF();

        m_outputStream.flush();
    }

    //
    // Private Support Methods
    //

    /**
     * Runs the conversion and writes RTF to the output stream.
     */
    private void writeRTF()
        throws IOException,
               RegExException,
               AmbassadorDwUpException
    {
        String str_sourceLocale = m_page.getSourceLocaleName().trim();
        String str_targetLocale = m_page.getTargetLocaleName().trim();

        String str_sourceLang = m_page.getSourceLocaleName().trim();
        String str_primaryLang = str_sourceLang.substring(0,2);

        // set up the parameters we need
        try
        {
            m_iSourceLcid = LCID.getLocaleId(m_page.getSourceLocaleName().trim());
            m_strSourceLcid = "\\lang" + m_iSourceLcid;

            m_iTargetLcid = LCID.getLocaleId(m_page.getTargetLocaleName().trim());
            m_strTargetLcid = "\\lang" + m_iTargetLcid;
        }
        catch (MissingResourceException ex)
        {
            throw new AmbassadorDwUpException (
              AmbassadorDwUpExceptionConstants.RTF_LOCALE_NOT_SUPPORTED, ex);
        }
/*
        if (str_sourceLocale.startsWith("he") ||
            str_sourceLocale.startsWith("ar"))
        {
            m_sourceIsRtl = true;
        }

        if (str_targetLocale.startsWith("he") ||
            str_targetLocale.startsWith("ar"))
        {
            m_targetIsRtl = true;
        }
*/
        m_strSourceLcid = "\\lang" + m_iSourceLcid + " \\langfe" + m_iSourceLcid +
            "\\cgrid\\langnp" + m_iSourceLcid + "\\langfenp" + m_iSourceLcid;
        m_strTargetLcid = "\\lang" + m_iTargetLcid + " \\langfe" + m_iTargetLcid +
            "\\cgrid\\langnp" + m_iTargetLcid + "\\langfenp" + m_iTargetLcid;

        try
        {
            String str_temp = res.getString("RTFFontEncoding." + str_primaryLang);
            m_sourceEncoding = CodesetMapper.getJavaEncoding(str_temp);
        }
        catch (MissingResourceException ex)
        {
            throw new AmbassadorDwUpException (
              AmbassadorDwUpExceptionConstants.RTF_MISSING_FONT_ENCODING, ex);
        }

        // set up shortcuts for the RTF controls we need
        m_strDefaultStyle = FONT_ARIAL + SIZE_12PT + m_strDefaultLcid + " ";

        // style for source segments (text still in source language)
        m_strSourceTextStyle = FONT_SOURCE + SIZE_12PT +
          m_strSourceLcid + NO_PROOF + " ";

        if (m_sourceIsRtl)
        {
            m_strSourceTextStyle = "\\rtlch" + m_strSourceTextStyle;
        }

        // style for target segments (text already in target language)
        m_strTargetTextStyle = FONT_TARGET + SIZE_12PT +
          m_strTargetLcid + " ";

        if (m_targetIsRtl)
        {
            m_strTargetTextStyle = "\\rtlch" + m_strTargetTextStyle;
        }

        // Trados styles
        m_strInternalStyle = TW4WIN_INTERNAL + COLOR_RED + FONT_ARIAL +
            SIZE_12PT /*+ m_strDefaultLcid */ + " ";
        m_strExternalStyle = TW4WIN_EXTERNAL + COLOR_GREY + FONT_COURIER +
            SIZE_10PT /*+ m_strDefaultLcid */ + " ";
        m_strMarkerStyle = TW4WIN_MARK + COLOR_DARK_GREEN + FONT_ARIAL +
            "\\sub\\v ";

        m_outputStream.write("{");

        writeRTFHeader();

        m_outputStream.write(m_strDefaultStyle);

        writeStartSignature();
        writeDocumentHeader();

        ListIterator it = m_page.getSegmentIterator();
        while (it.hasNext())
        {
            OfflineSegmentData segment = (OfflineSegmentData)it.next();

            writeTranslationUnit(segment);
        }

        writeEndSignature();

        m_outputStream.write("}");
        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes an entire segment to the output document.
     */
    private void writeTranslationUnit(OfflineSegmentData tu)
        throws IOException, RegExException, UnsupportedEncodingException
    {
        String str_srcSegment;
        String str_trgSegment;
        String str_dataType = tu.getDisplaySegmentFormat();
        float i_matchValue = tu.getMatchValue();

        // S.Y. 3/5/01 decided not to normalize white space
        //   if (str_dataType == null)
        //{
        //    str_dataType = m_page.getOriginalFormat();
        //}
        //  // optionally normalize source segment
        //  if (str_dataType.equalsIgnoreCase("html") ||
        //    str_dataType.equalsIgnoreCase("xml"))
        //  {
        //      // replace newlines and multiple spaces with one single space
        //      // (does preserve trailing space!)
        //      str_srcSegment = Text.normalizeWhiteSpaces(tu.getDisplaySourceText());
        //  }
        //  else
        //  {
                str_srcSegment = tu.getDisplaySourceTextWithNewLinebreaks(
                    "" + NORMALIZED_LINEBREAK);
        //  }

        // S.Y. 3/5/01 decided not to normalize white space
        //  // optionally normalize target segment
        //  if (str_dataType.equalsIgnoreCase("html") ||
        //    str_dataType.equalsIgnoreCase("xml"))
        //  {
        //      // replace newlines and multiple spaces with one single space
        //      // (does preserve trailing space!)
        //      str_trgSegment = Text.normalizeWhiteSpaces(tu.getDisplayTargetText());
        //  }
        //  else
        //  {
                str_trgSegment = tu.getDisplayTargetTextWithNewLineBreaks(
                    "" + NORMALIZED_LINEBREAK);
        //  }

        // seg ID
        if (tu.getDisplaySegmentID() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
                AmbassadorDwUpConstants.SEGMENT_ID_KEY +
                tu.getDisplaySegmentID() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        // indicate a native-format switch
        // NOTE: We decided to NOT show this on all segments.
        //       We only show this when the format != document format.
        if( !str_dataType.equals(m_page.getDocumentFormat()) )
        {
            m_outputStream.write("{" + m_strExternalStyle +
                AmbassadorDwUpConstants.SEGMENT_FORMAT_KEY +
                " " +
                str_dataType + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        // Indicate the match type and score
        // This is required for upload error checking
        String tmp = tu.getDisplayMatchType();
        if( (tmp != null) && (tmp.length() != 0)  )
        {
            m_outputStream.write( "{" + m_strExternalStyle +
                AmbassadorDwUpConstants.SEGMENT_MATCH_TYPE_KEY +
                " " +
                tmp +
                "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        // Resources
        if (!tu.isSubflowSegment() && tu.hasTerminology())
        {
            m_outputStream.write( "{" + m_strExternalStyle +
                AmbassadorDwUpConstants.SEGMENT_RESOURCE_KEY + " " +
                    makeTermAnnotation(
                        AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_TERM,
                        AmbassadorDwUpConstants.LABEL_ANNOTATION_AUTHOR, tu) + 
                "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }
        
        // Not count Tags.
        List notCountTags = tu.getNotCountTags();
        if (notCountTags.size() > 0)
        {
            String tags = notCountTags.toString();
            tags = tags.substring(1, tags.length() -1);
            String message = AmbassadorDwUpConstants.SEGMENT_NOT_COUNT_KEY;
            message = MessageFormat.format(message, new String[]{tags});
            m_outputStream.write( "{" + m_strExternalStyle + message + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }
        
        // Segment
        if (m_isTradosRtf)
        {
            writeTradosTU(str_srcSegment, str_trgSegment,
                          i_matchValue, tu.isWriteAsProtectedSegment() );
        }
        else
        {
            writeNormalTU(str_srcSegment, str_trgSegment,
                          i_matchValue, tu.isWriteAsProtectedSegment() );
        }
    }

    private void writeNormalTU(String p_srcSegment, String p_trgSegment,
                                float p_matchValue , boolean p_isProtected)
         throws IOException
    {

      // open the translation unit - source hidden
    // m_outputStream.write("{");

        // write out the source segment
        // m_outputStream.write(m_strSourceTextStyle +
    //     COLOR_GREY + SIZE_10PT + "\\v ");
        // writeSegmentText(m_outputStream, p_srcSegment,
        //     m_strInternalStyle + SIZE_10PT + " ");

        //// terminate the segment with a paragraph and close it
        //   m_outputStream.write("\\par\\pard}");
        //   m_outputStream.write(m_strEOL);

        // write out the target segment
        m_outputStream.write("{");

        if (p_matchValue < 0)
        {
            // This is actually a copy of the source segment
            m_outputStream.write(m_strSourceTextStyle);
            writeSegmentText(m_outputStream, p_trgSegment, m_strInternalStyle);
        }
        else
        {
            // special treatment for protected segments
            if( p_isProtected )
            {
                m_outputStream.write(m_strExternalStyle);
                writeSegmentText(m_outputStream, p_trgSegment, m_strExternalStyle);
            }
            else
            {
                m_outputStream.write(m_strTargetTextStyle);
                writeSegmentText(m_outputStream, p_trgSegment, m_strInternalStyle);
            }
        }

        // terminate the segment with a paragraph and close it
        m_outputStream.write("\\par\\pard}");
        m_outputStream.write(m_strEOL);

        // and add an empty line to make it look good (?)
        m_outputStream.write("{" + m_strExternalStyle + "\\par\\pard}");
        m_outputStream.write(m_strEOL);
    }

    private void writeTradosTU(String p_srcSegment, String p_trgSegment,
                               float p_matchValue, boolean p_isProtected)
        throws IOException
    {
        // special treatment for protected (non-editable) segments
        if( p_isProtected )
        {
            writeNormalTU(p_srcSegment, p_trgSegment, p_matchValue, p_isProtected );
            return;
        }

        // Opening Trados marker
        m_outputStream.write("{" + m_strMarkerStyle + "\\{0>}");

        // write out the source segment
        m_outputStream.write("{");
        m_outputStream.write(m_strSourceTextStyle + HIDDEN + " " );
        writeSegmentText(m_outputStream, p_srcSegment, m_strInternalStyle);
        m_outputStream.write("}");

        // Middle Trados marker
        m_outputStream.write("{" + m_strMarkerStyle + "<\\}" +
          (p_matchValue >= 0 ? p_matchValue : 0) + "\\{>}");

        // write out the target segment
        if (p_matchValue < 0)
        {
            // This is actually a copy of the source segment
            m_outputStream.write("{");
            m_outputStream.write(m_strSourceTextStyle);
            writeSegmentText(m_outputStream, p_trgSegment, m_strInternalStyle);
            m_outputStream.write("}");
        }
        else
        {
            // Target segment has been translated before, assume
            // it *is* in target language and not in source language.
            m_outputStream.write("{");
            m_outputStream.write(m_strTargetTextStyle);
            writeSegmentText(m_outputStream, p_trgSegment, m_strInternalStyle);
            m_outputStream.write("}");
        }

        // Closing Trados marker
        m_outputStream.write("{" + m_strMarkerStyle + "<0\\}}");

        // terminate the segment with a paragraph and close it
        m_outputStream.write("{\\f0\\par}");
        m_outputStream.write(m_strEOL);

        // and add an empty line to make it look good (?)
        m_outputStream.write("{" + m_strExternalStyle + "\\par\\pard}");
        m_outputStream.write(m_strEOL);
    }

    /**
     * Wraps all p-tags "[...]" in a segment inside the style
     * tw4winInternal but leaves "[[" untouched.  Output string is
     * written to the stream.
     */
    private void writeSegmentText(OutputStreamWriter p_stream,
      String p_segment, String p_style)
        throws IOException
    {

        final int MODE_TEXT = 0;
        final int MODE_PTAG = 1;
        int mode = MODE_TEXT;
        StringBuffer run = new StringBuffer ();

        int i_at = 0;
        int i_max = p_segment.length();

        while (i_at < i_max)
        {
            char ch = p_segment.charAt(i_at);

            if (ch == '[')
            {
                // we can check i+1 because if there's a '[' character
                // at the end of the segment it must be escaped.
                if (p_segment.charAt(i_at + 1) == ch)
                {
                    run.append(ch);
                    run.append(ch);
                    ++i_at;
                    ++i_at;
                    continue;
                }
                else
                {
                    p_stream.write(run.toString());
                    run.setLength(0);

                    // Regardless of the languages in this file, ptags
                    // are always English, ASCII and left-to-right. So
                    // make this decision a no-brainer.
                    run.append("{\\ltrch");
                    run.append(p_style);
                    run.append(ch);

                    mode = MODE_PTAG;
                    ++i_at;
                    continue;
                }
            }
            else if (ch == ']')
            {
                // end of ptag found?
                if (mode == MODE_PTAG)
                {
                    run.append(ch);
                    run.append('}');
                    p_stream.write(run.toString());
                    run.setLength(0);

                    mode = MODE_TEXT;
                    ++i_at;
                    continue;
                }
            }
            else if (ch == NORMALIZED_LINEBREAK )
            {
                run.append("\\line\r\n");

                // Note: By this time, the string has been converted to ptag.
                //       Unprotected newlines were normalized for Rtf when we
                //       got the target text in writeTranslationUnit().
                ++i_at;
                continue;
            }

            run.append(encodeChar(ch));
            ++i_at;
        }

        p_stream.write(run.toString());
    }

    /**
     * Encodes a Unicode char to a series of RTF escaped chars in the
     * source language's natural encoding.  E.g.: Japanese char
     * &x5510; (5510 hex, 21776 dec) --shift-jis-->&gt; 0x93 0x82
     * --rtf-escape--&gt; \'93\'82.
     */
    private String encodeChar(char ch)
        throws UnsupportedEncodingException
    {
        short code = (short)ch;

        if ((0x00 <= code && code < 0x20) ||
          (code == 0x5c || code == 0x7b || code == 0x7d) || // {,\,}
          (0x80 <= code && code <= 0xff))
        {
            return escapeChar(ch);
        }
        else if (0x20 <= code && code < 0x80)
        {
            return String.valueOf(ch);
        }

        // All chars > 255 are converted to the locale's (or source
        // font's) default encoding and then escaped

        // get bytes in source encoding
        byte[] temp = String.valueOf(ch).getBytes(m_sourceEncoding);

        // get bytes as ISO characters
        String str_encoded = new String (temp, m_ISOEncoding);

        // then print each byte in \'XX notation
        StringBuffer str_result = new StringBuffer ();
        for (int i_at = 0; i_at < str_encoded.length(); ++i_at)
        {
            if (str_encoded.charAt(i_at) > 255)
            {
                System.err.println("RTFWriter Warning: char is not ANSI");
            }

            str_result.append(escapeChar(str_encoded.charAt(i_at)));
        }

        // System.err.println("char " + (int)ch + " --> " + str_encoded +
        //   " --> " + str_result);

        return str_result.toString();
    }

    /**
     * Escapes the char RTF-style: \'XX.  Used by {@link
     * encodeChar(char) encodeChar()}.  Char must be in the range
     * 0-255, all other chars must be encoded by {@link
     * encodeChar(char) encodeChar()} prior to escaping.
     */
    private String escapeChar(char ch)
    {
        short code = (short)ch;

        if (0x00 <= code && code < 0x10)
        {
            return new String ("\\'0" + Integer.toString(code, 16));
        }
        else if (0x10 <= code && code <= 0xff)
        {
            return new String ("\\'" + Integer.toString(code, 16));
        }
        else
        {
            System.err.println("escapeChar() called on invalid char");
            return "?";
            // throw new Exception ("escapeChar() called on invalid char");
        }
    }

    /**
     * <p>Writes an RTF header to the output stream.</p>
     *
     * @throws AmbassadorDwUpException with a code of
     * AmbassadorDwUpExceptionConstants.RTF_MISSING_FONTSPEC when a
     * RTFDefaultFont.locale property cannot be found in
     * UnicodeRTF.properties.
     *
     * @throws AmbassadorDwUpException with a code of
     * AmbassadorDwUpExceptionConstants.RTF_MISSING_RESOURCE when
     * other properties like <code>RTFHeaderStart</code> cannot be
     * found.
     */
    private void writeRTFHeader()
        throws IOException,
               AmbassadorDwUpException
    {
        Exception error = null;
        String str_sourceFont = null;
        String str_targetFont = null;
        String str_srcLocale = m_page.getSourceLocaleName().toLowerCase();
        String str_trgLocale = m_page.getTargetLocaleName().toLowerCase();

        if (str_srcLocale.length() > 2 && str_srcLocale.charAt(2) != '-')
        {
            str_srcLocale = str_srcLocale.substring(0,2) + "-" +
              str_srcLocale.substring(3);
        }
        if (str_trgLocale.length() > 2 && str_trgLocale.charAt(2) != '-')
        {
            str_trgLocale = str_trgLocale.substring(0,2) + "-" +
              str_trgLocale.substring(3);
        }

        // carefully find a default font in which to output text
        try
        {
            str_sourceFont = res.getString("RTFDefaultFont." + str_srcLocale);
            str_targetFont = res.getString("RTFDefaultFont." + str_trgLocale);
        }
        catch (MissingResourceException ex)
        {
            error = ex;
        }

        try
        {
            if (str_sourceFont == null)
            {
                str_srcLocale = str_srcLocale.substring(0,2);
                str_sourceFont = res.getString("RTFDefaultFont." + str_srcLocale);
            }
            if (str_targetFont == null)
            {
                str_trgLocale = str_trgLocale.substring(0,2);
                str_targetFont = res.getString("RTFDefaultFont." + str_trgLocale);
            }
        }
        catch (MissingResourceException ex)
        {
            // ignore and choose default font
        }

        try
        {
            if (str_sourceFont == null)
            {
                System.err.println("RTFWriter: no font for locale " +
                  str_srcLocale + ", using Arial");

                str_sourceFont = res.getString("RTFDefaultFont");
            }
            if (str_targetFont == null)
            {
                System.err.println("RTFWriter: no font for locale " +
                  str_trgLocale + ", using Arial");

                str_targetFont = res.getString("RTFDefaultFont");
            }
        }
        catch (MissingResourceException ex)
        {
            // throw original exception, which is more meaningful to user
            throw new AmbassadorDwUpException (
              AmbassadorDwUpExceptionConstants.RTF_MISSING_FONTSPEC, error);
        }

        // sets document defaults after the header
        String str_DocumentDefaults;
        try
        {
            str_DocumentDefaults = res.getString("DocumentDefaults");
        }
        catch (MissingResourceException ex)
        {
            throw new AmbassadorDwUpException (
              AmbassadorDwUpExceptionConstants.RTF_MISSING_RESOURCE, ex);
        }

        m_outputStream.write(RTF_HEADER_START);
        m_outputStream.write(RTF_FONT_TABLE_START);
        m_outputStream.write("{\\f0" + str_sourceFont + "}");
        m_outputStream.write("{\\f1" + str_targetFont + "}");
        m_outputStream.write(RTF_FONT_TABLE_END);
        m_outputStream.write(COLOR_TABLE);
        m_outputStream.write(RTF_STYLESHEET_START);
        // default document style ("Normal") is in target language,
        // so you can hit ^space (normal style) and start overwriting
        // text in the target language
        m_outputStream.write("\\lang" + m_iTargetLcid + "\\langfe" + m_iTargetLcid +
          "\\langnp" + m_iTargetLcid + "\\langfenp" + m_iTargetLcid);
        m_outputStream.write(RTF_STYLESHEET_END);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(generateInfoSection());
        m_outputStream.write(m_strEOL);
        m_outputStream.write(str_DocumentDefaults);

        // Switch the writing direction of the document when the
        // target language is Hebrew or Arabic.
        if (m_targetIsRtl)
        {
            m_outputStream.write("\\rtlpar");
        }

        m_outputStream.write(m_strEOL);
    }

    /**
     * Generates an RTF {\info} block with creation date, document
     * title etc.
     */
    private String generateInfoSection()
    {
        StringBuffer str_result = new StringBuffer ();

        String str_title = DOCUMENTTITLE;
        String str_author = PRODUCTNAME;
        Calendar dt_creationDate = GregorianCalendar.getInstance();

        str_result.append("{\\info");

        str_result.append("{\\title ");
        str_result.append(str_title);
        str_result.append("}");
        str_result.append(m_strEOL);

        str_result.append("{\\author ");
        str_result.append(str_author);
        str_result.append("}");
        str_result.append(m_strEOL);

        // str_result.append("{\\*\\company ");
        // str_result.append(str_company);
        // str_result.append("}");
        // str_result.append(m_strEOL);

        str_result.append("{\\creatim ");
        str_result.append("\\yr" +  dt_creationDate.get(Calendar.YEAR));
        str_result.append("\\mo" +  dt_creationDate.get(Calendar.MONTH));
        str_result.append("\\dy" +  dt_creationDate.get(Calendar.DAY_OF_MONTH));
        str_result.append("\\hr" +  dt_creationDate.get(Calendar.HOUR_OF_DAY));
        str_result.append("\\min" + dt_creationDate.get(Calendar.MINUTE));
        str_result.append("}");
        str_result.append(m_strEOL);

        str_result.append("}");                   // close {\info

        return str_result.toString();
    }

    /**
     * Writes the document header - encoding, formats, languages etc.
     */
    private void writeDocumentHeader()
        throws IOException, AmbassadorDwUpException
    {
        m_page.verifyHeader();
        
        if (m_page.getEncoding() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_ENCODING_KEY + " " +
              "UTF-8"  /*m_page.getEncoding()*/
              + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getDocumentFormat() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_ORIGFMT_KEY + " " +
              m_page.getDocumentFormat() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getPlaceholderFormat() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_CURFMT_KEY + " " +
              m_page.getPlaceholderFormat() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getSourceLocaleName() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_SRCLOCALE_KEY + " " +
              m_page.getSourceLocaleName() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getTargetLocaleName() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_TRGLOCALE_KEY + " " +
              m_page.getTargetLocaleName() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

       /* DISABLED Page Name - it is optional and the upload parser 
       currently cannot handle extended characters or a simple
       excalmaton point like in Yahoo!.html
        if (m_page.getFullPageName() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
                AmbassadorDwUpConstants.HEADER_PAGENAME_KEY + " " +
                escapeFileName(m_page.getFullPageName()) + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }*/

        if (m_page.getPageId() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_PAGEID_KEY + " " +
              m_page.getPageId() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getWorkflowId() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_JOBID_KEY + " " +
              m_page.getWorkflowId() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getTaskId() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_STAGEID_KEY + " " +
              m_page.getTaskId() + "\\par\\pard}");
            m_outputStream.write(m_strEOL);
        }

        // word counts
        m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_EXACT_COUNT_KEY + " " +
              m_page.getExactMatchWordCount() + "\\par\\pard}");
              m_outputStream.write(m_strEOL);
        m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_FUZZY_COUNT_KEY + " " +
              m_page.getFuzzyMatchWordCount() + "\\par\\pard}");
              m_outputStream.write(m_strEOL);
        m_outputStream.write("{" + m_strExternalStyle +
              AmbassadorDwUpConstants.HEADER_NOMATCH_COUNT_KEY + " " +
              m_page.getNoMatchWordCount() + "\\par\\pard}");
              m_outputStream.write(m_strEOL);

        m_outputStream.write("{" + m_strExternalStyle + "\\par\\pard}");

        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes the start signature to the document.
     */
    private void writeStartSignature()
        throws IOException
    {
        m_outputStream.write("{" + m_strExternalStyle +
          m_page.getStartSignature() + "\\par\\pard}");

        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes the end signature to the document.
     */
    private void writeEndSignature()
        throws IOException
    {
        m_outputStream.write("{" + m_strExternalStyle +
          m_page.getEndSignature() + "\\par\\pard}");

        m_outputStream.write(m_strEOL);
    }

    /**
     * Escapes the file name path for rtf.
     */
    private String escapeFileName(String p_filename)
    {
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < p_filename.length(); i++)
        {
            if(p_filename.charAt(i) =='\\')   
            {
                sb.append("\\\\");
            }
            else
            {
                sb.append(p_filename.charAt(i));
            }
        }
        return sb.toString();
    }
    
        /*
     * Creates a hyperlink compatible to MS-Word.
     *
     * @param p_url - the url for the link
     * @param p_anchorName - the link name
     * @param p_anchorName - the screen tip text
     * @param p_linkText - visible link text
     * @param p_style - the underlying format style of the link
     */
    private String makeMsWordHyperLink(String p_url, String p_anchorName, 
        String p_screenTip, String p_linkText, String p_style)
    {
        StringBuffer sb = new StringBuffer();        
        sb.append("{");
        sb.append("\\field{\\*\\fldinst HYPERLINK ");
        sb.append("\"" + p_url + "\" ");
        sb.append("\\\\l \"" + p_anchorName + "\" "); 
        sb.append("\\\\o \"" + p_screenTip + "\" ");
        sb.append("}");
        sb.append("{\\fldrslt " + p_style + " \\ul ");
        sb.append(p_linkText); 
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Helper function to make the intial annotation groups for Id and Author.
     * This method is not to be called directly. It is called internally by 
     * the various annotation creation methods such as makeTermAnnotation.
     *
     * @param p_atnId - the name of the comment (which appears in document)
     * @param p_atnAuthor - name of the person who inserted the comment
     */
    private String makeAnnotationIdandAuthor(String p_atnId, String p_atnAuthor)
    {
        StringBuffer sb = new StringBuffer();
        
        // initialization groups
        sb.append("{\\*\\atnid " + p_atnId + "}");
        sb.append("{\\*\\atnauthor " + p_atnAuthor + "}");        
        sb.append("\\chatn"); // atnid - document body
        
        return sb.toString();
    }

    /*
     * Creates an annotation field.
     *
     * @param p_atnId - the name of the comment (which appears in document)
     * @param p_atnAuthor - name of the person who inserted the comment
     * @param p_segData - reference to OfflineSegmentData 
     */
    private String makeTermAnnotation(String p_atnId, String p_atnAuthor,
         OfflineSegmentData p_segData)
         throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        /*ListIterator it = p_segData.getTermLeverageMatchIterator();
        
        // initialization groups       
        sb.append(makeAnnotationIdandAuthor(p_atnId, p_atnAuthor));
        
        // open the annotation group
        sb.append("{");
        sb.append("\\*\\annotation \\pard\\plain " + ANNOTATION_TXT + " ");
        sb.append("{" + m_strDefaultLcid + " " + ANNOTATION_REF + " " +
            p_segData.getDisplaySegmentID() + "    }");
        sb.append("{" + ANNOTATION_REF + "\\chatn }\\par"); // atnid - annotation frame
        
        // add term entries 
        while(it.hasNext())
        {
            sb.append(
                formatTermEntry((OfflineSegmentData.TermLeverageMatch)it.next())
                + "\\par");
        }        
        
        // close the annotation group
        sb.append("}");
        */
        return sb.toString();
    }
    
    /* 
     * Creates and formats a single term entry
     */
    private String formatTermEntry(TermLeverageMatchResult p_tlm)
        throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        String nextTarget = null;
        
        // format source term
        sb.append( "{" + m_strSourceLcid + SIZE_12PT + COLOR_GREY + " " +
            encodeText(p_tlm.getSourceTerm()) + "}");
        
        // format target terms for this source term
        sb.append( "     {" + m_strTargetLcid + COLOR_BLACK + " " );
        sb.append(p_tlm.getFirstTargetTerm());
        while( (nextTarget=p_tlm.getNextTargetTerm()) != null)
        {
            sb.append(encodeText(", " + nextTarget));
        }
        sb.append("}"); 
        
        return sb.toString();
    }
 
    /**
     * encode a run of text
     */
    private String encodeText(String p_text)
        throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i=0; i < p_text.length(); i++)
        {
            sb.append(encodeChar(p_text.charAt(i)));            
        }             
        return sb.toString();
    }

}
