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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.upload.UploadPageSaverException;
import com.globalsight.ling.rtf.RtfAnnotation;
import com.globalsight.ling.rtf.RtfAnnotationBookmark;
import com.globalsight.ling.rtf.RtfBookmark;
import com.globalsight.ling.rtf.RtfCell;
import com.globalsight.ling.rtf.RtfCompoundObject;
import com.globalsight.ling.rtf.RtfControl;
import com.globalsight.ling.rtf.RtfData;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.ling.rtf.RtfFieldInstance;
import com.globalsight.ling.rtf.RtfFootnote;
import com.globalsight.ling.rtf.RtfHyperLink;
import com.globalsight.ling.rtf.RtfLineBreak;
import com.globalsight.ling.rtf.RtfMarker;
import com.globalsight.ling.rtf.RtfObject;
import com.globalsight.ling.rtf.RtfPageBreak;
import com.globalsight.ling.rtf.RtfParagraph;
import com.globalsight.ling.rtf.RtfPicture;
import com.globalsight.ling.rtf.RtfRow;
import com.globalsight.ling.rtf.RtfShape;
import com.globalsight.ling.rtf.RtfShapePicture;
import com.globalsight.ling.rtf.RtfShapeText;
import com.globalsight.ling.rtf.RtfSymbol;
import com.globalsight.ling.rtf.RtfTab;
import com.globalsight.ling.rtf.RtfText;
import com.globalsight.ling.rtf.RtfTextProperties;
import com.globalsight.ling.rtf.RtfVariable;
import com.globalsight.ling.rtf.RtfVariables;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoOverrideMapItem;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * This class is responsible for reading an Offline file of type
 * Paragragh-View-1 (which is RTF) and loading it into an OfflinePageData
 * p_object.
 *
 * This class is similar (in purpose) to the loadOfflineTextFile() method of the
 * OfflinePageData class itself - only this method populates the specified
 * OfflinePageData p_object from an RTF-paragraph-view file instead.
 */
public class ParaViewOneWorkDocLoader implements ParaViewWorkDocConstants,
        AmbassadorDwUpConstants, PseudoConstants
{
    static private final Logger c_logger = Logger
            .getLogger(ParaViewOneWorkDocLoader.class);

    //
    // Private & Protected Constants
    //

    static private final String regx_baseId = "(([:digit:]+)("
            + BOOKMARK_SEG_ID_DELIM + "([:digit:]+))?)";

    /**
     * Identifies segment ids contained in the target documents merge record.
     */
    static private final REProgram m_gsMergeRecSegIdSignature = createProgram(regx_baseId);

    /**
     * Identifies our bookmark signature in the body of the target document.
     *
     * Note: we allow user modified endings to be added. This allows uses to tag
     * segments with their own macros without interfering with Ambassador.
     */
    static private final REProgram m_gsBookmarkSignature = createProgram("^(gs)"
            + regx_baseId + "[.]*");

    // bookmark signature
    static private final int PAREN_FULLMATCH = 0;
    static private final int PAREN_SEGPREFIX = 1;
    static private final int PAREN_SEGID = 2;
    static private final int PAREN_PARENTID = 3;
    static private final int PAREN_SUBID_WITHPREFIX = 4;
    static private final int PAREN_SUBID = 5;

    /** Identifies our ptag fields. */
    static private final REProgram m_gsPtagSignature = createProgram("ptag[:space:]+(\\[.*\\])");

    // ptag signature
    static private final int PAREN_PTAG_NAME = 1;

    static private REProgram createProgram(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException ex)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(ex.getMessage());
        }

        return pattern;
    }

    //
    // Private Member Variables
    //

    /** The OfflinePageData into which the file will be read */
    private OfflinePageData m_opd = null;
    private RtfDocument m_rtfDoc = null;
    /** Flag - true when reading a segment. */
    private boolean m_inSegment = false;
    /** The RTF segment name of the current segment being read (gs1234). */
    private String m_curSegName = null;
    /**
     * The Ambassador segment ID of the current segment being read (123_45)
     * (TU_SUB).
     */
    private String m_curSegId = null;

    /**
     * Flag - true when we encountered an embedded segment bookmark while still
     * reading the previous segment.
     */
    private boolean m_inEmbeddedSegment = false;
    /**
     * The segment ID of the next segment - only valid if
     * m_inEmbeddedNextSegment == true.
     */
    private String m_embeddedSegmentName = null;
    private StringBuffer m_segmentRun = null;
    private HashMap m_documentMergeData = null;

    private boolean m_inBoldRun = false;
    private boolean m_inItalicRun = false;
    private boolean m_inUnderlinedRun = false;

    private String m_gsBoldOn = null;
    private String m_gsBoldOff = null;
    private String m_gsItalicOn = null;
    private String m_gsItalicOff = null;
    private String m_gsUnderlinedOn = null;
    private String m_gsUnderlinedOff = null;
    private String m_gsNbsp = null;

    //
    // Constructors
    //

    /** Creates a new instance of ParaViewOneWorkDocLoader. */
    public ParaViewOneWorkDocLoader(RtfDocument p_rtfDoc, OfflinePageData p_opd)
    {
        m_rtfDoc = p_rtfDoc;
        m_opd = p_opd;

        // get valid addable data
        PseudoData PD = new PseudoData();
        PseudoOverrideMapItem POMI = null;
        String POMIkey = null;
        StringBuffer sb = null;
        StringBuffer emptyTag = new StringBuffer();
        emptyTag.append(PSEUDO_OPEN_TAG);
        emptyTag.append(PSEUDO_CLOSE_TAG);

        if ((POMIkey = PD.isAddableInFormat("b", "rtf")) != null)
        {
            POMI = PD.getOverrideMapItem(POMIkey);
            sb = new StringBuffer(emptyTag.toString());
            sb.insert(1, POMI.m_strCompact);
            m_gsBoldOn = sb.toString();
            sb.insert(1, PSEUDO_END_TAG_MARKER);
            m_gsBoldOff = sb.toString();
        }

        if ((POMIkey = PD.isAddableInFormat("i", "rtf")) != null)
        {
            POMI = PD.getOverrideMapItem(POMIkey);
            sb = new StringBuffer(emptyTag.toString());
            sb.insert(1, POMI.m_strCompact);
            m_gsItalicOn = sb.toString();
            sb.insert(1, PSEUDO_END_TAG_MARKER);
            m_gsItalicOff = sb.toString();
        }

        if ((POMIkey = PD.isAddableInFormat("u", "rtf")) != null)
        {
            POMI = PD.getOverrideMapItem(POMIkey);
            sb = new StringBuffer(emptyTag.toString());
            sb.insert(1, POMI.m_strCompact);
            m_gsUnderlinedOn = sb.toString();
            sb.insert(1, PSEUDO_END_TAG_MARKER);
            m_gsUnderlinedOff = sb.toString();
        }

        if ((POMIkey = PD.isAddableInFormat("nbsp", "rtf")) != null)
        {
            POMI = PD.getOverrideMapItem(POMIkey);
            sb = new StringBuffer(emptyTag.toString());
            sb.insert(1, POMI.m_strCompact);
            m_gsNbsp = sb.toString();
        }
    }

    //
    // Public Methods
    //

    public void parse() throws Exception
    {
        loadVariables(m_rtfDoc);
        loadSegments(m_rtfDoc);
    }

    //
    // Private Methods
    //

    private void loadVariables(RtfDocument p_doc) throws Exception
    {
        boolean encFound = false;
        boolean mrgFound = false;
        boolean pageIdFound = false;
        boolean ptagFormatFound = false;
        boolean taskIdFound = false;
        boolean wkfIdFound = false;
        boolean srcLocaleFound = false;
        boolean trgLocaleFound = false;

        RtfVariables vars = p_doc.getVariables();

        for (int i = 0, max = vars.count(); i < max; i++)
        {
            RtfVariable var = vars.getVariable(i);
            String name = var.getName();
            String value = var.getValue();

            if (!srcLocaleFound && name.equals(DOCVAR_NAME_SRCLOCALE))
            {
                m_opd.setSourceLocaleName(value);
                srcLocaleFound = true;
            }
            else if (!trgLocaleFound && name.equals(DOCVAR_NAME_TRGLOCALE))
            {
                m_opd.setTargetLocaleName(value);
                trgLocaleFound = true;
            }
            else if (!encFound && name.equals(DOCVAR_NAME_ENCODING))
            {
                m_opd.setEncoding(value);
                encFound = true;
            }
            else if (!pageIdFound && name.equals(DOCVAR_NAME_PAGEID))
            {
                m_opd.setPageId(value);
                pageIdFound = true;
            }
            else if (!ptagFormatFound && name.equals(DOCVAR_NAME_PTAGFMT))
            {
                m_opd.setPlaceholderFormat(value);
                ptagFormatFound = true;
            }
            else if (!taskIdFound && name.equals(DOCVAR_NAME_TASKID))
            {
                m_opd.setTaskId(value);
                taskIdFound = true;
            }
            else if (!wkfIdFound && name.equals(DOCVAR_NAME_WORKFLOWID))
            {
                m_opd.setWorkflowId(value);
                wkfIdFound = true;
            }
            else if (!mrgFound && name.equals(DOCVAR_NAME_MERGEDATA))
            {
                m_documentMergeData = parseMergeData(value);
                mrgFound = true;
            }
        }

        // error check
        if (!srcLocaleFound || !trgLocaleFound || !encFound || !mrgFound
                || !pageIdFound || !ptagFormatFound || !taskIdFound
                || !wkfIdFound)
        {
            String args[] =
            {
                    DOCVAR_NAME_SRCLOCALE,
                    srcLocaleFound ? m_opd.getSourceLocaleName() : "missing!",
                    DOCVAR_NAME_TRGLOCALE,
                    trgLocaleFound ? m_opd.getTargetLocaleName() : "missing!",
                    DOCVAR_NAME_ENCODING,
                    encFound ? m_opd.getEncoding() : "missing!",
                    DOCVAR_NAME_MERGEDATA,
                    mrgFound ? m_opd.getSegmentMergeMap().toString()
                            : "missing!",
                    DOCVAR_NAME_PAGEID,
                    pageIdFound ? m_opd.getPageId() : "missing!",
                    DOCVAR_NAME_PTAGFMT,
                    ptagFormatFound ? m_opd.getPlaceholderFormat() : "missing!",
                    DOCVAR_NAME_TASKID,
                    taskIdFound ? m_opd.getTaskId() : "missing!",
                    DOCVAR_NAME_WORKFLOWID,
                    wkfIdFound ? m_opd.getWorkflowId() : "missing!" };

            throw new UploadPageSaverException(
                    UploadPageSaverException.MSG_FAILED_TO_GET_PV1_DOC_VARS,
                    args, null);
        }
    }

    private void loadSegments(RtfDocument p_doc) throws Exception
    {
        for (int i = 0, max = p_doc.size(); i < max; i++)
        {
            load(p_doc.getObject(i));
        }
    }

    private void load(RtfObject p_object) throws Exception
    {
        if (p_object instanceof RtfAnnotation)
        {
            load((RtfAnnotation) p_object);
        }
        else if (p_object instanceof RtfAnnotationBookmark)
        {
            load((RtfAnnotationBookmark) p_object);
        }
        else if (p_object instanceof RtfFootnote)
        {
            load((RtfFootnote) p_object);
        }
        else if (p_object instanceof RtfMarker)
        {
            load((RtfMarker) p_object);
        }
        else if (p_object instanceof RtfControl)
        {
            load((RtfControl) p_object);
        }
        else if (p_object instanceof RtfText)
        {
            load((RtfText) p_object);
        }
        else if (p_object instanceof RtfParagraph)
        {
            load((RtfParagraph) p_object);
        }
        else if (p_object instanceof RtfData)
        {
            load((RtfData) p_object);
        }
        else if (p_object instanceof RtfLineBreak)
        {
            load((RtfLineBreak) p_object);
        }
        else if (p_object instanceof RtfPageBreak)
        {
            load((RtfPageBreak) p_object);
        }
        else if (p_object instanceof RtfFieldInstance)
        {
            load((RtfFieldInstance) p_object);
        }
        else if (p_object instanceof RtfSymbol)
        {
            load((RtfSymbol) p_object);
        }
        else if (p_object instanceof RtfTab)
        {
            load((RtfTab) p_object);
        }
        else if (p_object instanceof RtfHyperLink)
        {
            load((RtfHyperLink) p_object);
        }
        else if (p_object instanceof RtfShape)
        {
            load((RtfShape) p_object);
        }
        else if (p_object instanceof RtfShapeText)
        {
            load((RtfShapeText) p_object);
        }
        else if (p_object instanceof RtfShapePicture)
        {
            load((RtfShapePicture) p_object);
        }
        else if (p_object instanceof RtfPicture)
        {
            load((RtfPicture) p_object);
        }
        else if (p_object instanceof RtfBookmark)
        {
            load((RtfBookmark) p_object);
        }
        else if (p_object instanceof RtfRow)
        {
            load((RtfRow) p_object);
        }
        else if (p_object instanceof RtfCell)
        {
            load((RtfCell) p_object);
        }
        else if (p_object instanceof RtfCompoundObject)
        {
            load((RtfCompoundObject) p_object);
        }
        else
        {
            c_logger.info("load(RtfObject): unknown object=" + p_object);
        }
    }

    private void load(RtfCompoundObject p_object) throws Exception
    {
        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            load(p_object.getObject(i));
        }
    }

    // Currently we load row properties before *and* after the
    // cells as simple controls. We write them out as they come.
    private void load(RtfRow p_object) throws Exception
    {
        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            load(p_object.getObject(i));
        }
    }

    private void load(RtfCell p_object) throws Exception
    {
        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            load(p_object.getObject(i));
        }
    }

    private void load(RtfAnnotation p_object) throws Exception
    {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        p_object.toTextSpecial(pw);
        String annotation = writer.toString().trim();

        if (c_logger.isDebugEnabled())
        {
            System.out.println("RtfAnnotation for " + m_curSegId + " (in "
                    + m_curSegName + ")");
            System.out.println("  getId=" + p_object.getId());
            System.out.println("  getRef=" + p_object.getRef());
            System.out.println("  toText=" + annotation);
        }

        // Filter out accidental annotations not associated with a segment.
        if (m_curSegId != null)
        {
            // Skip "gs" prefix so segname looks like "TUID" or
            // "TUID_SUB" so we can look up the associated segment
            // (OSD) later by display string.
            String segName = m_curSegName.substring(2);

            IssueLoader.handleUploadIssue(m_opd, m_curSegId, segName,
                    annotation);
        }
    }

    private void load(RtfAnnotationBookmark p_object) throws Exception
    {
        if (c_logger.isDebugEnabled())
        {
            System.out.println("RtfAnnotationBookmark");
            System.out.println("  getName=" + p_object.getName());
            System.out.println("  isStart=" + p_object.isStart());
            System.out.println("  isEnd=" + p_object.isEnd());
        }
    }

    // Footnotes always appear inside paragraphs.
    private void load(RtfFootnote p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfFootnote");
            System.out.println("  getData=" + p_object.getData());
            System.out.println("  getType=" + p_object.getType());
        }
    }

    private void load(RtfControl p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfControl");
            System.out.println("  getName=" + p_object.getName());
            System.out.println("  getData=" + p_object.getData());
            System.out.println("  getParam=" + p_object.getParam());
            System.out.println("  getType=" + p_object.getType());
        }
    }

    private void load(RtfLineBreak p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfLineBreak");
            System.out.println("  getData=" + p_object.getData());
            System.out.println("  getType=" + p_object.getType());
        }
    }

    private void load(RtfPageBreak p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfPageBreak");
        }
    }

    private void load(RtfFieldInstance p_object) throws Exception
    {
        // process ptag fields found within a segment - ignore all other fields
        RE matcher = new RE(m_gsPtagSignature);
        if (m_inSegment && matcher.match(p_object.getParameters()))
        {
            loadStyleStartTags(p_object.getProperties());

            m_segmentRun.append(matcher.getParen(PAREN_PTAG_NAME));
        }
    }

    private void load(RtfSymbol p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfSymbol:");
            System.out.println("  getData=" + p_object.getData());
            System.out.println("  getParam=" + p_object.getProperties());
            System.out.println("  getType=" + p_object.getType());
        }
    }

    private void load(RtfParagraph p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfParagraph:");
            System.out.println("  getData=" + p_object.getData());
            System.out.println("  getParam=" + p_object.getProperties());
            System.out.println("  getType=" + p_object.getType());
        }

        for (int i = 0, max = p_object.size(); i < max; i++)
        {
            RtfObject o = (RtfObject) p_object.getObject(i);

            if (o instanceof RtfAnnotation)
            {
                // Load annotations out of the normal document flow.
                load((RtfAnnotation) o);
            }
            else
            {
                load(o);
            }
        }
    }

    private void load(RtfText p_object) throws Exception
    {
        if (m_inEmbeddedSegment)
        {
            String[] args =
            { m_embeddedSegmentName, m_curSegName };

            throw new UploadPageSaverException(
                    UploadPageSaverException.MSG_EMBEDDED_BOOKMARKS, args, null);
        }

        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfText - properties follow="
                    + p_object.getData());
        }

        RtfTextProperties props = p_object.getProperties();
        loadStyleStartTags(props);

        if (p_object.getData() != null && m_inSegment)
        {
            m_segmentRun.append(processNbsp(p_object.getData()));
        }
    }

    private void load(RtfMarker p_object) throws Exception
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("RtfMarker");
            System.out.println("  getName=" + p_object.getName());
            System.out.println("  getData=" + p_object.getData());
            System.out.println("  getParam=" + p_object.getProperties());
            System.out.println("  getType=" + p_object.getType());
        }
    }

    private void loadStyleStartTags(RtfTextProperties p_props)
    {
        if (false && c_logger.isDebugEnabled())
        {
            System.out.println("loadStyleStartTags::RtfTextProperties");
            System.out.println("  isBold=" + p_props.isBold());
            System.out.println("  isBoldSet=" + p_props.isBoldSet());
            System.out.println("  isUnderlined=" + p_props.isUnderlined());
            System.out
                    .println("  isUnderlinedSet=" + p_props.isUnderlinedSet());
            System.out.println("  isItalic=" + p_props.isItalic());
            System.out.println("  isItalicSet=" + p_props.isItalicSet());
            System.out.println("  m_segmentRun=" + m_segmentRun);
        }

        if (m_inSegment)
        {
            processBold(p_props.isBoldSet());
            processItalic(p_props.isItalicSet());
            processUnderlined(p_props.isUnderlinedSet());
        }
    }

    /**
     * Hyperlinks, which are really fields in RTF.
     */
    private void load(RtfHyperLink p_object) throws Exception
    {
        if (c_logger.isDebugEnabled())
        {
            // System.out.println("RtfHyperLink");
            // System.out.println("  getAltText=" + p_object.getAltText());
            // System.out.println("  getRefid=" + p_object.getRefid());
            // System.out.println("  getText=" + p_object.getText());
            // System.out.println("  getUrl=" + p_object.getUrl());
        }

        // TODO: inject the hyperlink's text into the segment.
    }

    /**
     * Shapes.
     */
    private void load(RtfShape p_object) throws Exception
    {
    }

    /**
     * Shape paragraphs embedded in shapes.
     */
    private void load(RtfShapeText p_object) throws Exception
    {
    }

    /**
     * Embedded Pictures.
     */
    private void load(RtfShapePicture p_object) throws Exception
    {
    }

    private void load(RtfPicture p_object) throws Exception
    {
    }

    private void load(RtfBookmark p_object) throws Exception
    {
        RE matcher = new RE(m_gsBookmarkSignature);

        if (matcher.match(p_object.getName()))
        {
            m_curSegId = cvtBmkSegIdToInternalId(matcher);

            // Detect embedded segment bookmarks.
            if (m_inSegment
                    && !m_curSegName.equalsIgnoreCase(p_object.getName()))
            {
                // We have detected a posssible error:
                // We are in error if text within this next segment
                // begins before the current segment's closing
                // bookmark is encountered. It must immediately follow
                // this bookmark.
                // See the load method for the RtfText object where
                // these flags are checked. These flags are cleared
                // below if the next item is the closing bookmark.
                if (p_object.isStart())
                {
                    m_inEmbeddedSegment = true;
                    m_embeddedSegmentName = p_object.getName();
                    return;
                }
            }

            if (p_object.isStart())
            {
                m_segmentRun = new StringBuffer();
                m_inSegment = true;
                m_curSegName = p_object.getName();

                processBold(false);
                processItalic(false);
                processUnderlined(false);
            }
            else
            {
                processBold(false);
                processItalic(false);
                processUnderlined(false);

                // Add segment
                OfflineSegmentData osd = new OfflineSegmentData(m_curSegId);
                ArrayList md = null;

                // Detect and replace the empty segment placeholder.
                String trgText = m_segmentRun.toString();
                if (trgText
                        .trim()
                        .toLowerCase()
                        .equals(AmbassadorDwUpConstants.WC_EMPTY_SEG_PLACEHOLDER_TEXT_LC))
                {
                    trgText = "";
                }

                osd.setDisplayTargetText(trgText);

                // Note: if parent, must also check for and copy merge data.
                if (!OfflineEditHelper.isSubflowSegmentId(m_curSegId))
                {
                    md = (ArrayList) m_documentMergeData.get(new Long(
                            m_curSegId));
                    osd.setMergedIds(md);
                }
                m_opd.addSegment(osd); // standard upload add.

                // clean up
                if (m_inEmbeddedSegment)
                {
                    // transition to valid overlapping (adjacent) bookmarks
                    m_curSegName = m_embeddedSegmentName;
                    m_segmentRun = new StringBuffer();
                    m_inEmbeddedSegment = false;
                    m_embeddedSegmentName = "";
                }
                else
                {
                    // normal transition
                    m_inSegment = false;
                    m_curSegName = "";
                    m_curSegId = null;
                    m_segmentRun = null;
                }
            }
        }
    }

    private void load(RtfData p_object) throws Exception
    {
        if (c_logger.isDebugEnabled())
        {
            // System.out.println("RtfData:");
            // System.out.println("  toString=" + p_object.toString());
            // System.out.println("  getData=" + p_object.getData());
        }
    }

    private void load(RtfTab p_object) throws Exception
    {
        if (m_inSegment)
        {
            m_segmentRun.append("\t");
        }
    }

    private void processBold(boolean p_isBoldSet)
    {
        if (p_isBoldSet && !m_inBoldRun && m_gsBoldOn != null)
        {
            m_segmentRun.append(m_gsBoldOn);
            m_inBoldRun = true;
        }
        else if (!p_isBoldSet && m_inBoldRun && m_gsBoldOff != null)
        {
            m_segmentRun.append(m_gsBoldOff);
            m_inBoldRun = false;
        }
    }

    private void processItalic(boolean p_isItalicSet)
    {
        if (p_isItalicSet && !m_inItalicRun && m_gsItalicOn != null)
        {
            m_segmentRun.append(m_gsItalicOn);
            m_inItalicRun = true;
        }
        else if (!p_isItalicSet && m_inItalicRun && m_gsItalicOff != null)
        {
            m_segmentRun.append(m_gsItalicOff);
            m_inItalicRun = false;
        }
    }

    private void processUnderlined(boolean p_isUnderlinedSet)
    {
        if (p_isUnderlinedSet && !m_inUnderlinedRun && m_gsUnderlinedOn != null)
        {
            m_segmentRun.append(m_gsUnderlinedOn);
            m_inUnderlinedRun = true;
        }
        else if (!p_isUnderlinedSet && m_inUnderlinedRun
                && m_gsUnderlinedOff != null)
        {
            m_segmentRun.append(m_gsUnderlinedOff);
            m_inUnderlinedRun = false;
        }
    }

    private String processNbsp(String p_text)
    {
        if (m_gsNbsp != null)
        {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < p_text.length(); i++)
            {
                char ch = p_text.charAt(i);

                if (ch == '\u00a0')
                {
                    sb.append(m_gsNbsp);
                }
                else
                {
                    sb.append(ch);
                }
            }

            return sb.toString();
        }

        return "";
    }

    /**
     * Converts the document merge record into a hashmap.
     *
     * The map is keyed by the parent Id under which the merge occured.
     *
     * The value of each map entry is a list of ids in the same order as read
     * from the document merge record. The first id of each record is also
     * always the parent Id under which the merge occured.
     *
     * @param p_docVarMergeValue
     *            the document merge record (as read from the document
     *            variable).
     * @return HashMap of merge records - keyed by the parent Id under which the
     *         merge occured.
     */
    private HashMap parseMergeData(String p_docVarMergeValue) throws Exception
    {
        HashMap result = new HashMap();

        if (p_docVarMergeValue != null
                && p_docVarMergeValue.length() > 0
                && !p_docVarMergeValue.trim().toLowerCase()
                        .equals(NO_MERGE_RECORDS))
        {
            StringTokenizer t1 = new StringTokenizer(p_docVarMergeValue,
                    String.valueOf(AmbassadorDwUpConstants.MERGE_RECORD_DELIM));

            while (t1.hasMoreElements())
            {
                String token = null;
                Long parentID = null;
                boolean firstRecId = true;
                ArrayList record = new ArrayList();
                StringTokenizer t2 = new StringTokenizer(t1.nextToken());

                while (t2.hasMoreElements())
                {
                    token = t2.nextToken();
                    RE matcher = new RE(m_gsMergeRecSegIdSignature);

                    if (matcher.match(token))
                    {
                        // note: complex seg Ids (subflows) can never be merged
                        Long idAsLong = new Long(
                                matcher.getParen(PAREN_FULLMATCH));
                        record.add(idAsLong);

                        if (firstRecId)
                        {
                            parentID = idAsLong;
                            firstRecId = false;
                        }
                    }
                    else
                    {
                        String args[] =
                        { token };
                        throw new UploadPageSaverException(
                                UploadPageSaverException.MSG_INVALID_MERGE_ID,
                                args, null);
                    }
                }

                result.put(parentID, record);
            }
        }

        return result;
    }

    /**
     * Converts bookmark segment names to orginal internal segment id (TU_SUB).
     */
    private String cvtBmkSegIdToInternalId(RE p_gsBookmarkSignature)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_gsBookmarkSignature.getParen(PAREN_PARENTID));
        // String tmp = p_gsBookmarkSignature.getParen(PAREN_FULLMATCH);
        String subid = p_gsBookmarkSignature.getParen(PAREN_SUBID);

        if (subid != null)
        {
            sb.append(BOOKMARK_SEG_ID_DELIM);
            sb.append(subid);
        }

        return sb.toString();
    }
}
