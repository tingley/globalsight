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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.MissingResourceException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.download.DownloadHelper;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.tw.HtmlTableWriter;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.StringUtil;

/**
 * <p>
 * Reads a p-tag input file and generates an RTF LIst-View file that can be
 * translated.
 * </p>
 * 
 * <p>
 * <code># num</code> markers and other <code>#</code>-escapes are protected
 * from editing by the style <code>tw4winExternal</code>, and p-tags embedded by
 * <code>[...]</code> inside a segment are marked up as segment-internal tags (
 * <code>tw4winInternal</code>). Segment-internal tags cannot be translated but
 * are stored in the TM as part of the segment.
 * </p>
 * 
 * <p>
 * The input is read as characters from a InputStream object, the output is
 * written as ASCII characters to an OutputStream. Unicode or double-byte
 * characters are converted to the proper RTF codes as necessary. Note that RTF
 * files are always ASCII files.
 * </p>
 * 
 * <p>
 * <b>Background info:</b> Translator's Workbench marks up input files using the
 * character styles <code>tw4winInternal</code> and <code>tw4winExternal</code>.
 * (There are other styles but they are of no concern for the discussion here.)
 * Text marked up as <code>tw4winExternal</code> is not considered translatable
 * text, like a <code>&lt;skeleton&gt;</code> section in a Diplomat TMX file.
 * Text marked up as <code>tw4winInternal</code> is considered part of a
 * segment, but non-translatable, like Diplomat XML's
 * <code>&lt;bpt&gt;...&lt;ept&gt;</code> tags.
 * </p>
 */
public class ListViewWorkDocWriter extends RTFWriterUnicode
{
    static private final Logger c_logger = Logger
            .getLogger(ListViewWorkDocWriter.class);

    //
    // Public Constants
    //

    /**
     * A unique string that identifies this file format. This string is stored
     * in the RTF info section under "title". This string is used in regular
     * expressions to recognize our files programatically. See UploadPageHandler
     * where document recognition is performed.
     */
    static public final String WORK_DOC_TITLE = "GlobalSight Extracted List-View Export";

    //
    // Private Member Variables
    //

    private User m_user = null;
    private long m_segmentCounter = 0;
    private boolean m_TagEditorDownload = true;

    /** The parameter which enables Trados output. */
    private boolean m_isTradosRtf = false;
    // The parameter which controls the last segment
    // "External Tools Helper Segment - DO NOT TRANSLATE" output or not
    private boolean m_isTradosRtfOptimized = false;

    /** The parameter which drives resource insertion. */
    private int m_resInsOpt = AmbassadorDwUpConstants.MAKE_RES_ATNS;

    //
    // Constructors
    //

    /**
     * Constructor.
     */
    public ListViewWorkDocWriter()
    {
    }

    //
    // Public Methods
    //

    /**
     * Sets Trados output.
     */
    public void setTradosOutput()
    {
        m_isTradosRtf = true;
    }

    /**
     * Sets optimized Trados output.
     */
    public void setTradosOutputOptimized()
    {
        m_isTradosRtfOptimized = true;
    }

    /**
     * Sets resource insertion option.
     */
    public void setResInsertOption(int p_resInsOpt)
    {
        m_resInsOpt = p_resInsOpt;
    }

    /**
     * Sets the download user's name.
     */
    public void setUser(User p_user)
    {
        m_user = p_user;
    }

    protected String makeRTF_documentDefaults() throws AmbassadorDwUpException
    {
        // get basic document defaults
        String commonDefaults = "";

        try
        {
            commonDefaults = m_resource.getString("DocumentDefaults");
        }
        catch (MissingResourceException ex)
        {
            c_logger.warn("Could not load RTF document default view options",
                    ex);
        }

        StringBuffer sb = new StringBuffer();

        // default to normal style cs0
        sb.append("\\margl720\\margr720");
        sb.append(commonDefaults);

        return sb.toString();
    }

    protected String makeRTF_documentVariables() throws AmbassadorDwUpException
    {
        // nothing for now
        return "";
    }

    protected String makeRTF_infoSection() throws AmbassadorDwUpException
    {
        return generateInfoSection();
    }

    protected String makeRTF_templateAttachment()
            throws AmbassadorDwUpException
    {
        // nothing for now
        return "";
    }

    protected void writeRTF_docBody() throws IOException,
            AmbassadorDwUpException
    {
        m_segmentCounter = 0;

        OfflineSegmentData osd = null;

        for (ListIterator it = m_page.getSegmentIterator(); it.hasNext();)
        {
            m_segmentCounter++;
            osd = (OfflineSegmentData) it.next();

            try
            {
                if (!it.hasNext())
                {
                    // indicates this is the last segment
                    osd.setLastSegment(true);
                }
                writeTranslationUnit(osd);
            }
            catch (Exception ex)
            {
                throw new AmbassadorDwUpException(ex);
            }
        }

        writeEndSignature();

        // When a segment is untranslated, we write the source and
        // target segments using the source lang attributes. If the
        // last segment is untranslated, TagEditor cannot determine
        // the target language and therefore does not allow TM
        // interaction. To fix this, we check the TU we write. If it
        // is a copy of the source, and we are downloading a Trados
        // bilingual RTF for TagEditor, then we write a bogus TU to
        // the end of the file using the source and target lang
        // attributes to enable TagEditor to identify the target
        // language. Since we write the bogus TU after the Globalsight
        // END signature, upload will ignore it.

        if (m_isTradosRtf && !m_isTradosRtfOptimized && m_TagEditorDownload
                && osd != null && osd.isCopyOfSource())
        {
            writeTradosTU("External Tools Helper Segment - DO NOT TRANSLATE",
                    "External Tools Helper Segment - DO NOT TRANSLATE", 0,
                    false, false, osd.getDisplaySegmentFormat(),
                    null, false);
        }

        m_outputStream.write("}");
        m_outputStream.write(m_strEOL);
    }

    protected void writeRTF_docHeader() throws IOException,
            AmbassadorDwUpException
    {
        writeDocumentHeader();
    }

    //
    // Private Support Methods
    //

    /**
     * Writes an entire segment to the output document.
     * 
     * TRADOS Note: We had a problem in TagEditor where the tw4winexternal style
     * bled into the TRADOS OPEN segment box when opening a segment and typing
     * at the first character position. To fix this, we moved "\\par\\pard"
     * outside of the group in all possible paragraphs that may proceed our
     * segment paragraph. This prevented the style from bleeding.
     */
    private void writeTranslationUnit(OfflineSegmentData p_osd)
            throws IOException, RegExException
    {
        String srcSegment;
        String trgSegment;
        String dataType = p_osd.getDisplaySegmentFormat();
        // We must use int type for offline download rtf file.
        float floatMatchValue = p_osd.getMatchValue();
        int matchValue = (new Float(floatMatchValue)).intValue();

        // Special treatment for HTML.
        if (dataType.equalsIgnoreCase("html"))
        {
            if (!dataType.equals("text"))
            {
                dataType = p_osd.getSegmentType();
            }
        }

        // S.Y. 3/5/01 decided not to normalize white space
        // // optionally normalize source segment
        // if (dataType == null)
        // {
        // dataType = m_page.getOriginalFormat();
        // }
        // if (dataType.equalsIgnoreCase("html") ||
        // dataType.equalsIgnoreCase("xml"))
        // {
        // // replace newlines and multiple spaces with one single space
        // // (does preserve trailing space!)
        // srcSegment = Text.normalizeWhiteSpaces(tu.getDisplaySourceText());
        // }
        // else
        // {
        srcSegment = p_osd.getDisplaySourceTextWithNewLinebreaks(String
                .valueOf(NORMALIZED_LINEBREAK));
        // }

        // S.Y. 3/5/01 decided not to normalize white space
        // // optionally normalize target segment
        // if (dataType.equalsIgnoreCase("html") ||
        // dataType.equalsIgnoreCase("xml"))
        // {
        // // replace newlines and multiple spaces with one single space
        // // (does preserve trailing space!)
        // trgSegment = Text.normalizeWhiteSpaces(tu.getDisplayTargetText());
        // }
        // else
        // {
        trgSegment = p_osd.getDisplayTargetTextWithNewLineBreaks(String
                .valueOf(NORMALIZED_LINEBREAK));
        // }

        // write ID
        if (p_osd.getDisplaySegmentID() != null)
        {
            // See Trados Note in header (\\par).
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.SEGMENT_ID_KEY
                    + p_osd.getDisplaySegmentID() + "}\\par");
            m_outputStream.write(m_strEOL);
        }
        
        // write file path
        String pagename = p_osd.getDisplayPageName();
        if (pagename != null)
        {
            if (pagename.contains("\\"))
            {
                pagename = pagename.replace("\\", "\\\\");
            }
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.SEGMENT_FILE_PATH_KEY + " "
                    + pagename + "}\\par");
            m_outputStream.write(m_strEOL);
        }

        // write SID for reference
        String sid = p_osd.getSourceTuv().getSid();
        if (sid != null && sid.length() > 0)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.SEGMENT_SID_KEY + " "
                    + sid + "}\\par");
            m_outputStream.write(m_strEOL);
        }

        // Indicate a native-format switch.
        // NOTE: We decided to NOT show this on all segments.
        // We only show this when the format != document format.
        if (!dataType.equals(m_page.getDocumentFormat()))
        {
            // See Trados Note in header (\\par).
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.SEGMENT_FORMAT_KEY + " "
                    + dataType + "}\\par");
            m_outputStream.write(m_strEOL);
        }

        // Indicate the match type and score.
        // This is required for upload error checking
        String tmp = p_osd.getDisplayMatchType();
        if (p_osd.isWriteAsProtectedSegment())
            tmp = "DO NOT TRANSLATE OR MODIFY (Locked).";
        if (tmp != null && tmp.length() > 0)
        {
            // See Trados Note in header.
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.SEGMENT_MATCH_TYPE_KEY + " "
                    + tmp + "}\\par");
            m_outputStream.write(m_strEOL);
        }

        // Write Resource links/annotations.
        writeResources(p_osd);

        List notCountTags = p_osd.getNotCountTags();
        if (notCountTags.size() > 0)
        {
            String tags = notCountTags.toString();
            tags = tags.substring(1, tags.length() - 1);
            String message = AmbassadorDwUpConstants.SEGMENT_NOT_COUNT_KEY;
            message = MessageFormat.format(message, new String[]
            { tags });
            m_outputStream.write("{" + m_strExternalStyle + message + "}\\par");
            m_outputStream.write(m_strEOL);
        }

        // segment text
        if (m_isTradosRtf)
        {
            writeTradosTU(srcSegment, trgSegment, matchValue,
                    p_osd.isWriteAsProtectedSegment(), p_osd.isCopyOfSource(),
                    p_osd.getDisplaySegmentFormat(), p_osd.getSegmentIssue(),
                    p_osd.isLastSegment());
        }
        else
        {
            writeNormalTU(srcSegment, trgSegment, matchValue,
                    p_osd.isWriteAsProtectedSegment(), p_osd.isCopyOfSource(),
                    p_osd.getDisplaySegmentFormat(), p_osd.getSegmentIssue());
        }
    }

    private void writeNormalTU(String p_srcSegment, String p_trgSegment,
            int p_matchValue, boolean p_isProtected, boolean p_isCopyOfSource,
            String p_segNativeFormat, Issue p_issue) throws IOException
    {
        // boolean protect = false;

        // write out the target segment
        m_outputStream.write("{");

        if (p_isCopyOfSource)
        {
            // This is actually a copy of the source segment.
            // m_outputStream.write(m_strSourceTextStyle);
            // writeSegmentText(m_outputStream, p_trgSegment,
            // m_strInternalStyle,
            // m_sourceIsRtlLang);
            writeRTF_docSegment(null, p_trgSegment, m_strSourceTextStyle,
                    m_strInternalStyle, m_sourceIsRtlLang, false,
                    p_segNativeFormat, false, p_issue, m_user);
        }
        else
        {
            // Special treatment for exact matches (if requested)
            if (p_isProtected)
            {
                // m_outputStream.write(m_strExternalStyleWithTargetFont);
                // writeSegmentText(m_outputStream, p_trgSegment,
                // m_strExternalStyle, m_targetIsRtlLang);
                writeRTF_docSegment(null, p_trgSegment,
                        m_strExternalStyleWithTargetFont, m_strInternalStyle,
                        m_sourceIsRtlLang, false, p_segNativeFormat, false,
                        p_issue, m_user);
            }
            else
            {
                // m_outputStream.write(m_strTargetTextStyle);
                // writeSegmentText(m_outputStream, p_trgSegment,
                // m_strInternalStyle, m_targetIsRtlLang);
                writeRTF_docSegment(null, p_trgSegment, m_strTargetTextStyle,
                        m_strInternalStyle, m_sourceIsRtlLang, false,
                        p_segNativeFormat, false, p_issue, m_user);
            }
        }

        // Terminate the segment with a paragraph and close it.
        m_outputStream.write("\\par\\pard}");
        m_outputStream.write(m_strEOL);

        // And add an empty line to make it look good (?).
        m_outputStream.write("{" + m_strExternalStyle + "\\par\\pard}");
        m_outputStream.write(m_strEOL);
    }

    private void writeTradosTU(String p_srcSegment, String p_trgSegment,
            int p_matchValue, boolean p_isProtected, boolean p_isCopyOfSource,
            String p_segNativeFormat, Issue p_issue, boolean p_isLastSegment)
            throws IOException
    {
        // Special treatment for exact matches
        if (p_isProtected)
        {
            // This will write a normal segment - protected but
            // without segment markup. This was thought to be best
            // for protected segments in a Trados file.
            writeNormalTU(p_srcSegment, p_trgSegment, p_matchValue,
                    p_isProtected, p_isCopyOfSource, p_segNativeFormat, p_issue);

            return;
        }

        // Opening Trados marker.
        m_outputStream.write("{" + m_strMarkerStyle + "\\{0>}");

        // Write out the source segment.
        // m_outputStream.write("{");
        // m_outputStream.write(m_strSourceTextStyle + HIDDEN + " ");
        // writeSegmentText(m_outputStream, p_srcSegment, m_strInternalStyle,
        // m_sourceIsRtlLang);
        writeRTF_docSegment(null, p_srcSegment, m_strSourceTextStyle + HIDDEN
                + " ", m_strInternalStyle, m_sourceIsRtlLang, false,
                p_segNativeFormat, false, p_issue, m_user);
        // m_outputStream.write("}");

        // Middle Trados marker
        m_outputStream.write("{" + m_strMarkerStyle + "<\\}"
                + (p_matchValue >= 0 ? p_matchValue : 0) + "\\{>}");

        // write out the target segment
        if (p_isCopyOfSource)
        {
            // This is actually a copy of the source segment.
            // m_outputStream.write("{");
            // m_outputStream.write(m_strSourceTextStyle);
            // writeSegmentText(m_outputStream, p_trgSegment,
            // m_strInternalStyle,
            // m_sourceIsRtlLang);
            // m_outputStream.write("}");
            writeRTF_docSegment(null, p_trgSegment, m_isTradosRtfOptimized
                    && p_isLastSegment ? m_strTargetTextStyle
                    : m_strSourceTextStyle, m_strInternalStyle,
                    m_sourceIsRtlLang, false, p_segNativeFormat, false, null,
                    null);
        }
        else
        {
            // Target segment has been translated before, assume
            // it *is* in target language and not in source language.
            // m_outputStream.write("{");
            // m_outputStream.write(m_strTargetTextStyle);
            // writeSegmentText(m_outputStream, p_trgSegment,
            // m_strInternalStyle,
            // m_targetIsRtlLang);
            // m_outputStream.write("}");
            writeRTF_docSegment(null, p_trgSegment, m_strTargetTextStyle,
                    m_strInternalStyle, m_targetIsRtlLang, false,
                    p_segNativeFormat, false, null, null);
        }

        // Closing Trados marker
        m_outputStream.write("{" + m_strMarkerStyle + "<0\\}}");

        // Terminate the segment with a paragraph and close it.
        m_outputStream.write("{\\f0\\par}");
        m_outputStream.write(m_strEOL);

        // And add an empty line to make it look good (?).
        m_outputStream.write("{" + m_strExternalStyle + "\\par\\pard}");
        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes the resource links and/or annotations
     * 
     * TRADOS Note: We had a problem in TagEditor where the tw4winexternal style
     * bled into the TRADOS OPEN segment box when opening a segment and typing
     * at the first character position. To fix this, we moved "\\par\\pard"
     * outside of the group in all possible paragraphs that may proceed our
     * segment paragraph. This prevented the style from bleeding.
     */
    private void writeResources(OfflineSegmentData p_osd) throws IOException
    {
        if (m_resInsOpt == AmbassadorDwUpConstants.MAKE_RES_NONE
                || !hasResources(p_osd))
        {
            return;
        }

        String src_atn = "";
        boolean has_src_atn = false;
        boolean atnEnabled = (m_segmentCounter <= m_page
                .getAnnotationThreshold());

        m_outputStream.write("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.SEGMENT_RESOURCE_KEY + " ");

        if (atnEnabled
                && (m_resInsOpt == AmbassadorDwUpConstants.MAKE_RES_ATNS || m_resInsOpt == AmbassadorDwUpConstants.MAKE_RES_TMX_BOTH))
        {
            // Source atn.
            // The source is embedded in a Trados document so we don't
            // make a source annotation for Trados.
            if (!m_isTradosRtf)
            {
                src_atn = makeSourceAnnotation(
                        AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_SOURCE,
                        AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_SOURCE,
                        p_osd);
                m_outputStream.write(src_atn + m_strEOL);
                has_src_atn = (src_atn.length() > 0);
            }

            // Tm and Term atn(s).
            // These are only made for unprotected parent segments.
            if (!p_osd.isWriteAsProtectedSegment())
            {
                String tm_atn = null;
                String term_atn = null;
                boolean has_term_atn = false;

                tm_atn = makeTMAnnotation(
                        AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_TM,
                        AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_TM, p_osd);
                boolean has_tm_atn = (tm_atn.length() > 0);

                if (p_osd.hasTerminology())
                {
                    term_atn = makeTermAnnotation(
                            AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_TERM,
                            AmbassadorDwUpConstants.LABEL_ANNOTATION_ID_TERM,
                            p_osd);
                    has_term_atn = (term_atn.length() > 0);
                }

                if (has_tm_atn)
                {
                    m_outputStream
                            .write((has_src_atn ? ", " + tm_atn : tm_atn));
                    m_outputStream.write(m_strEOL);
                }

                if (has_term_atn)
                {
                    m_outputStream.write((has_src_atn || has_tm_atn ? ", "
                            + term_atn : term_atn));
                    m_outputStream.write(m_strEOL);
                }
            }

            // See Trados Note in header.
            m_outputStream.write("}\\par" + m_strEOL);
        }
        else
        // insert single hyperlink to resource page
        {
            StringBuffer sb = new StringBuffer();

            // make link name
            if (!m_isTradosRtf)
            {
                sb.append(AmbassadorDwUpConstants.LABEL_LINK_SOURCE);
            }

            if (p_osd.hasTMMatches())
            {
                sb.append(!m_isTradosRtf ? ", "
                        + AmbassadorDwUpConstants.LABEL_LINK_TM
                        : AmbassadorDwUpConstants.LABEL_LINK_TM);
            }

            if (p_osd.hasTerminology())
            {
                sb.append(p_osd.hasTMMatches() ? ", "
                        + AmbassadorDwUpConstants.LABEL_LINK_TERM
                        : AmbassadorDwUpConstants.LABEL_LINK_TERM);
            }

            if (p_osd.hasMTMatches())
            {
            	sb.append(p_osd.hasTMMatches() ? ", "
                        + AmbassadorDwUpConstants.LABEL_LINK_MT
                        : AmbassadorDwUpConstants.LABEL_LINK_MT);
            }

            String pageid = p_osd.getPageId() == -1 ? m_page.getPageId() : "" + p_osd.getPageId();
            // escaped for rtf
            String url;
            if(m_page.isPreserveSourceFolder())
            {
            	String fullPageName = m_page.getFullPageName();
            	Set<String> set = StringUtil.split(fullPageName, "\\\\");
            	url = DownloadHelper.makeMSWordResParentPath(set.size())
            			+ pageid + "\\\\\\\\" + pageid + ".html";
            }
            else
            {
            	url = DownloadHelper.makeMSWordResParentPath(3)
    			+ pageid + "\\\\\\\\" + pageid + ".html";
            }

            m_outputStream.write(makeMsWordHyperLink(url,
                    p_osd.getDisplaySegmentID(),
                    AmbassadorDwUpConstants.LINK_TIP_RESPAGE, sb.toString(),
                    m_strExternalStyle, COLOR_GREY));
            // See Trados Note in header
            m_outputStream.write("}\\par" + m_strEOL);
        }
    }

    /**
     * Generates an RTF {\info} block with creation date, document title etc.
     */
    private String generateInfoSection()
    {
        StringBuffer sb = new StringBuffer();

        String title = WORK_DOC_TITLE;
        String author = PRODUCTNAME;
        Calendar dt_creationDate = GregorianCalendar.getInstance();

        sb.append("{\\info");

        sb.append("{\\title ");
        sb.append(title);
        sb.append("}");
        sb.append(m_strEOL);

        sb.append("{\\author ");
        sb.append(author);
        sb.append("}");
        sb.append(m_strEOL);

        sb.append("{\\creatim ");
        sb.append("\\yr" + dt_creationDate.get(Calendar.YEAR));
        sb.append("\\mo" + dt_creationDate.get(Calendar.MONTH));
        sb.append("\\dy" + dt_creationDate.get(Calendar.DAY_OF_MONTH));
        sb.append("\\hr" + dt_creationDate.get(Calendar.HOUR_OF_DAY));
        sb.append("\\min" + dt_creationDate.get(Calendar.MINUTE));
        sb.append("}");
        sb.append(m_strEOL);

        sb.append("}");

        return sb.toString();
    }

    /**
     * Writes the document header - encoding, formats, languages etc.
     */
    private void writeDocumentHeader() throws IOException,
            AmbassadorDwUpException
    {
        m_page.verifyHeader();

        m_outputStream.write(m_strEOL);
        m_outputStream.write("{" + CSSTYLE_HEADER_BORDER + " ");
        writeStartSignature();

        if (m_page.getEncoding() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_ENCODING_KEY + " " +
                    // Bug 2127
                    // To make the encoding consistent in txt and rtf.
                    m_page.getEncoding() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getDocumentFormat() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_ORIGFMT_KEY + " "
                    + m_page.getDocumentFormat() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getPlaceholderFormat() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_CURFMT_KEY + " "
                    + m_page.getPlaceholderFormat() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getSourceLocaleName() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_SRCLOCALE_KEY + " "
                    + m_page.getSourceLocaleName() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getTargetLocaleName() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_TRGLOCALE_KEY + " "
                    + m_page.getTargetLocaleName() + "\\par}");
            m_outputStream.write(m_strEOL);
        }
        
        // job id and job name requested by Don
        if (m_page.getAllJobNames() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_JOB_ID + " "
                    + m_page.getAllJobIds() + "\\par}");
            m_outputStream.write(m_strEOL);
            
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_JOB_NAME + " "
                    + m_page.getAllJobNames() + "\\par}");
            m_outputStream.write(m_strEOL);
        }
        else if (m_page.getJobName() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_JOB_ID + " "
                    + m_page.getJobId() + "\\par}");
            m_outputStream.write(m_strEOL);
            
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_JOB_NAME + " "
                    + m_page.getJobName() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        /*
         * DISABLED Page Name - it is optional and the upload parser currently
         * cannot handle extended characters or a simple exclamation point like
         * in Yahoo!.html if (m_page.getFullPageName() != null) { sb.append("{"
         * + m_strExternalStyle + AmbassadorDwUpConstants.HEADER_PAGENAME_KEY +
         * " " + escapeFileName(m_page.getFullPageName()) + "\\par}");
         * sb.append(m_strEOL); }
         */

        if (m_page.getPageId() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_PAGEID_KEY + " "
                    + m_page.getPageId() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getWorkflowId() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_JOBID_KEY + " "
                    + m_page.getWorkflowId() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        if (m_page.getTaskIds() != null && m_page.getTaskIds().size() > 1)
        {
            StringBuffer temp = new StringBuffer();
            List<Long> taskIds = m_page.getTaskIds();
            for (int i = 0; i < taskIds.size(); i++)
            {
                Long taskIdd = taskIds.get(i);
                temp.append(taskIdd).append(",");
            }
            
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_STAGEID_KEY + " "
                    + temp.substring(0, temp.length() - 1) + "\\par}");
            m_outputStream.write(m_strEOL);
        }
        else if (m_page.getTaskId() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_STAGEID_KEY + " "
                    + m_page.getTaskId() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        // word counts
        m_outputStream.write("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.HEADER_EXACT_COUNT_KEY + " "
                + m_page.getExactMatchWordCount() + "\\par}");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.HEADER_FUZZY_COUNT_KEY + " "
                + m_page.getFuzzyMatchWordCount() + "\\par}");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.HEADER_NOMATCH_COUNT_KEY + " "
                + m_page.getNoMatchWordCount() + "\\par}");
        m_outputStream.write(m_strEOL);

        // Populate 100% Target Segments
        m_outputStream.write("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.HEADER_POPULATE_100_SEGMENTS + " "
                + (m_page.isPopulate100() ? "YES":"NO") + "\\par}");
        m_outputStream.write(m_strEOL);

        // Server Instance ID
        if (m_page.getServerInstanceID() != null)
        {
            m_outputStream.write("{" + m_strExternalStyle
                    + AmbassadorDwUpConstants.HEADER_SERVER_INSTANCEID_KEY + " "
                    + m_page.getServerInstanceID() + "\\par}");
            m_outputStream.write(m_strEOL);
        }

        // Help links
        m_outputStream.write("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.HEADER_HELP_KEY + " ");
        m_outputStream.write(makeMsWordHyperLink(
                AmbassadorDwUpConstants.FILE_MAIN_HELP, null,
                AmbassadorDwUpConstants.LINK_TIP_MAIN_HELP,
                AmbassadorDwUpConstants.LINK_NAME_MAIN_HELP,
                m_strExternalStyle, COLOR_BLUE));
        m_outputStream.write("}\\par");
        m_outputStream.write(m_strEOL);

        // m_outputStream.write("{" + m_strExternalStyle +
        // AmbassadorDwUpConstants.HEADER_HELP_KEY + " " );
        // m_outputStream.write(
        // makeMsWordHyperLink(AmbassadorDwUpConstants.FILE_UPLOAD_HELP,
        // "Anchor", AmbassadorDwUpConstants.LINK_TIP_UPLOAD_HELP,
        // AmbassadorDwUpConstants.LINK_NAME_UPLOAD_HELP,
        // m_strExternalStyle, COLOR_BLUE));
        // m_outputStream.write("}\\par");
        // m_outputStream.write(m_strEOL);

        // Add note: Amb comment template instructions
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE1));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE2));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE3));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE4));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE5));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE6));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE7));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE8));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE9));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE10));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE11));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE12));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE13));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE14));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE15));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE16));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE17));
//        m_outputStream.write(makeRTF_listViewHeaderNote(HEADER_CMT_TEMPLATE18));

        // add note - pound sign instructions
        m_outputStream.write(makeRTF_listViewHeaderNote(LINK_NAME_NOTES));

        // must set pard only here to end bordering
        m_outputStream.write("\\pard");
        m_outputStream.write(m_strEOL);

        // end CSSTYLE_HEADER_BORDER
        m_outputStream.write("}");
        m_outputStream.write(m_strEOL);

        // part of grammer - space after header
        m_outputStream.write("{" + m_strExternalStyle + "\\par\\pard}");

        // must reset to \plain to satisfy TagEditor
        m_outputStream.write("\\plain");
        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes the start signature to the document.
     */
    private void writeStartSignature() throws IOException
    {
        m_outputStream.write("{" + m_strExternalStyle
                + m_page.getStartSignature() + "\\par}");

        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes the end signature to the document.
     */
    private void writeEndSignature() throws IOException
    {
        m_outputStream.write("{" + CSSTYLE_HEADER_BORDER + " ");
        m_outputStream.write("{" + m_strExternalStyle);
        m_outputStream.write(m_page.getEndSignature() + "\\par}");

        // Must use \plain because we sometimes write a bogus TU for
        // TagEditor downloads after this.
        m_outputStream.write("}\\pard\\plain");

        m_outputStream.write(m_strEOL);
    }

    /**
     * Creates a hyperlink compatible with MS-Word.
     * 
     * @param p_url
     *            - the url for the link
     * @param p_anchorName
     *            - the target link within the url
     * @param p_screenTip
     *            - the screen tip text
     * @param p_linkText
     *            - visible link text
     * @param p_style
     *            - the underlying format style of the link
     * @param p_color
     *            - the color of the link
     */
    private String makeMsWordHyperLink(String p_url, String p_anchorName,
            String p_screenTip, String p_linkText, String p_style,
            String p_color)
    {
        StringBuffer sb = new StringBuffer();

        if (p_url != null && p_linkText != null
                && p_style != null && p_color != null)
        {
            sb.append("{");
            sb.append("\\field{\\*\\fldinst HYPERLINK ");
            sb.append("\"");
            sb.append(p_url);
            sb.append("\" ");
            if (p_anchorName != null && p_anchorName.trim().length() > 0)
            {
                sb.append("\\\\l \"");
                sb.append(p_anchorName);
                sb.append("\" ");
            }
            sb.append("\\\\o \"");
            sb.append((p_screenTip == null ? p_linkText : p_screenTip));
            sb.append("\" ");
            sb.append("}");
            sb.append("{\\fldrslt ");
            sb.append(p_style);
            sb.append(" \\ul ");
            sb.append("{");
            sb.append(p_color);
            sb.append(p_linkText);
            sb.append("}}}");
        }

        return sb.toString();
    }

    /**
     * Creates a source text annotation field.
     * 
     * @param p_atnId
     *            - the name of the comment (which appears in document)
     * @param p_atnAuthor
     *            - name of the person who inserted the comment
     * @param p_segData
     *            - reference to OfflineSegmentData
     */
    private String makeSourceAnnotation(String p_atnId, String p_atnAuthor,
            OfflineSegmentData p_segData)
    {
        StringBuffer sb = new StringBuffer();

        if (p_atnId != null && p_atnAuthor != null && p_segData != null
                && p_segData.getDisplaySourceText() != null
                && p_segData.getDisplaySourceText().length() > 0)
        {
            // initialization groups
            sb.append(makeRTF_annotationIdandAuthor(p_atnId, p_atnAuthor));

            // open the annotation group
            sb.append("{");
            sb.append("\\*\\annotation \\pard\\plain ");
            sb.append(PSTYLE_ANNOTATION_TXT);
            sb.append(" ");
            // sb.append("{" + m_strDefaultLcid + " " + ANNOTATION_REF + " " +
            // p_segData.getDisplaySegmentID() + "    }");
            sb.append("{");
            sb.append(CSTYLE_ANNOTATION_REF);
            sb.append("\\chatn }\\par"); // atnid - annotation frame

            sb.append(formatSourceAtnEntry(p_segData));
            sb.append("\\par");

            sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Creates a segment details annotation field.
     * 
     * @param p_atnId
     *            - the name of the comment (which appears in document)
     * @param p_atnAuthor
     *            - name of the person who inserted the comment
     * @param p_segData
     *            - reference to OfflineSegmentData
     */
    private String makeSegmentDetailsAnnotation(String p_atnId,
            String p_atnAuthor, OfflineSegmentData p_segData)
    {
        StringBuffer sb = new StringBuffer();

        if (p_atnId != null && p_atnAuthor != null && p_segData != null)
        {
            // initialization groups
            sb.append(makeRTF_annotationIdandAuthor(p_atnId, p_atnAuthor));

            // open the annotation group
            sb.append("{");
            sb.append("\\*\\annotation \\pard\\plain ");
            sb.append(PSTYLE_ANNOTATION_TXT);
            sb.append(" ");
            // sb.append("{" + m_strDefaultLcid + " " + ANNOTATION_REF + " " +
            // p_segData.getDisplaySegmentID() + "    }");
            sb.append("{");
            sb.append(CSTYLE_ANNOTATION_REF);
            sb.append("\\chatn }\\par"); // atnid - annotation frame

            sb.append(formatSegmentDetailsAtnEntry(p_segData));
            sb.append("\\par");

            sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Creates a fuzzy match annotation field (also known as TM annotation).
     * 
     * @param p_atnId
     *            - the name of the comment (which appears in document)
     * @param p_atnAuthor
     *            - name of the person who inserted the comment
     * @param p_segData
     *            - reference to OfflineSegmentData
     */
    private String makeTMAnnotation(String p_atnId, String p_atnAuthor,
            OfflineSegmentData p_segData)
    {
        StringBuffer sb = new StringBuffer();

        if (p_atnId != null && p_atnAuthor != null && p_segData != null
                && p_segData.getDisplayFuzzyMatchList() != null
                && p_segData.getDisplayFuzzyMatchList().size() > 0)
        {
            // initialization groups
            sb.append(makeRTF_annotationIdandAuthor(p_atnId, p_atnAuthor));

            // open the annotation group
            sb.append("{");
            sb.append("\\*\\annotation \\pard\\plain ");
            sb.append(PSTYLE_ANNOTATION_TXT);
            sb.append(" ");
            // sb.append("{" + m_strDefaultLcid + " " + ANNOTATION_REF + " " +
            // p_segData.getDisplaySegmentID() + "    }");
            sb.append("{");
            sb.append(CSTYLE_ANNOTATION_REF);
            sb.append("\\chatn }\\par"); // atnid - annotation frame

            sb.append(formatTMAtnEntry(p_segData));
            sb.append("\\par");

            sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Creates a terminology annotation field.
     * 
     * @param p_atnId
     *            - the name of the comment (which appears in document)
     * @param p_atnAuthor
     *            - name of the person who inserted the comment
     * @param p_segData
     *            - reference to OfflineSegmentData
     */
    private String makeTermAnnotation(String p_atnId, String p_atnAuthor,
            OfflineSegmentData p_segData)
    {
        StringBuffer sb = new StringBuffer();
        List terms = null;

        if (p_atnId != null && p_atnAuthor != null && p_segData != null
                && (terms = p_segData.getTermLeverageMatchList()) != null
                && (p_segData.getTermLeverageMatchList().size() > 0))
        {
            Iterator it = terms.iterator();
            if (it.hasNext())
            {
                // initialization groups
                sb.append(makeRTF_annotationIdandAuthor(p_atnId, p_atnAuthor));

                // open the annotation group
                sb.append("{");
                sb.append("\\*\\annotation \\pard\\plain ");
                sb.append(PSTYLE_ANNOTATION_TXT);
                sb.append(" ");
                // sb.append("{" + m_strDefaultLcid + " " + ANNOTATION_REF + " "
                // +
                // p_segData.getDisplaySegmentID() + "    }");
                sb.append("{");
                sb.append(CSTYLE_ANNOTATION_REF);
                sb.append("\\chatn }\\par"); // atnid - annotation frame

                // add term entries
                while (it.hasNext())
                {
                    sb.append(formatTermAtnEntry((TermLeverageMatchResult) it
                            .next()));
                    sb.append("\\par");
                }

                // close the annotation group
                sb.append("}");
            }
        }

        return sb.toString();
    }

    /**
     * Helper function: returns source text formatted for a source-text
     * annotation.
     */
    private String formatSourceAtnEntry(OfflineSegmentData p_segData)
    {
        StringBuffer sb = new StringBuffer();

        // format source text
        sb.append("{" + m_strSourceLcid + FONT_SOURCE + SIZE_12PT + " ");
        sb.append(formatSegmentText(p_segData.getDisplaySourceText(),
                m_strInternalStyle, m_sourceIsRtlLang, false,
                p_segData.getDisplaySegmentFormat(), false));
        sb.append("}");

        return sb.toString();
    }

    /**
     * Helper function: returns segment details formatted for a segment details
     * annotation.
     */
    private String formatSegmentDetailsAtnEntry(OfflineSegmentData p_osd)
    {
        StringBuffer sb = new StringBuffer();

        // format segment-format
        sb.append("{" + m_strSourceLcid + FONT_SOURCE + SIZE_12PT + " ");
        sb.append(encodeText(AmbassadorDwUpConstants.LABEL_SEGMENT_FORMAT
                + ": "));
        sb.append(p_osd.getDisplaySegmentFormat());
        sb.append(" \\par\\pard ");

        // Format segment mapping (entries are always in the source locale).
        String[] keys = HtmlTableWriter.getSortedPtagKeys(p_osd
                .getPTag2NativeMap());
        if (keys.length > 0)
        {
            sb.append(encodeText(AmbassadorDwUpConstants.LABEL_MAPPING_TABLE
                    + ":"));
            sb.append(" \\par ");
            for (int i = 0; i < keys.length; i++)
            {
                sb.append(formatSegmentText(keys[i] + "  "
                        + p_osd.getPTag2NativeMap().get(keys[i]),
                        m_strInternalStyle, m_sourceIsRtlLang, false,
                        p_osd.getDisplaySegmentFormat(), false));
                sb.append("\\par");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Helper function: returns a TM entry formatted to fit within a TM
     * annotation.
     * 
     * Note: Caller should determine if the fuzzy match list is null.
     */
    private String formatTMAtnEntry(OfflineSegmentData p_osd)
    {
        StringBuffer sb = new StringBuffer();

        // format all fuzzy matches
        sb.append("{" + m_strTargetLcid + FONT_TARGET + SIZE_12PT + " ");

        List l1 = p_osd.getOriginalFuzzyLeverageMatchList();
        List l2 = p_osd.getDisplayFuzzyMatchList();

        for (int i = 0; i < l2.size(); i++)
        {
            LeverageMatch lm = (LeverageMatch) l1.get(i);

            sb.append((lm == null) ? "\\i [??] \\i0   " : "\\i ["
                    + StringUtil.formatPercent(lm.getScoreNum(), 2)
                    + "%]\\i0   ");

            sb.append(formatSegmentText((String) l2.get(i), m_strInternalStyle,
                    m_targetIsRtlLang, false, p_osd.getDisplaySegmentFormat(),
                    false));

            sb.append("\\par");
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Helper function: returns a term entry formatted to fit within a term
     * annotation.
     */
    private String formatTermAtnEntry(TermLeverageMatchResult p_tlm)
    {
        StringBuffer sb = new StringBuffer();
        String nextTarget = null;
        // format source term
        sb.append("{" + m_strSourceLcid + FONT_SOURCE + SIZE_12PT + COLOR_GREY
                + " " + encodeText(p_tlm.getSourceTerm()) + "}");

        // format target terms for this source term
        sb.append("     {" + m_strTargetLcid + FONT_TARGET + SIZE_12PT
                + COLOR_BLACK + " ");
        sb.append(encodeText(p_tlm.getFirstTargetTerm()));

        while ((nextTarget = p_tlm.getNextTargetTerm()) != null)
        {
            sb.append(encodeText(", " + nextTarget));
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Determine if we need to write the resource label.
     * 
     * @return true if there will be resources, false otherwise.
     */
    private boolean hasResources(OfflineSegmentData p_osd)
    {
        // We always write the source annotation if it is not a Trados
        // billingual file.
		if (!m_isTradosRtf || p_osd.hasTMMatches() || p_osd.hasTerminology()
				|| p_osd.hasMTMatches())
        {
            return true;
        }

        return false;
    }
}
