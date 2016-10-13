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

package com.globalsight.everest.edit.offline.download.BinaryResource;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.download.WriterInterface;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.rtf.RTFEditor;
import com.globalsight.everest.edit.offline.rtf.RTFWriterUnicode;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.ling.tw.HtmlTableWriter;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * <p>Generates an binary resource file from an OfflinePageData object.</p>
 *
 * <p>The output is written as ASCII characters to the provided
 * OutputStream.  Unicode or double-byte characters are converted to
 * the proper RTF codes as necessary.  Note that RTF files are always
 * ASCII files.</p>
 *
 */
public class ParaViewResWriter
    extends RTFWriterUnicode
    implements WriterInterface
{
    static private final Logger c_logger =
        Logger.getLogger(
            ParaViewResWriter.class);

    //
    // Public Constants
    //

    /**
     * A unique string that identifies this file format.
     *
     * This string is stored in the RTF info section under "title".
     * This string is used by regular expressions to recognize our
     * files programatically.  See UploadPageHandler where document
     * recognition is performed.
     */
    static public final String DOCUMENTTITLE =
        "GlobalSight Extracted Paragraph view - TM Resources";

    /** Must be synchronized with client code. */
    static private final String DELIM_IDX_1 = ":";
    /** Must be synchronized with client code. */
    static private final String DELIM_IDX_2 = ",";
    /** Must be synchronized with client code. */
    static private final String DELIM_TM_LIST_REC = "@:@";
    /** Must be synchronized with client code. */
    static private final String DELIM_TM_LIST_PAIR = "@,@";
    /** Must be synchronized with client code. */
    static private final String DELIM_TERM_LIST_REC = "@:@";
    /** Must be synchronized with client code. */
    static private final String DELIM_TERM_LIST_TERM = "@,@";
    /** Must be synchronized with client code. */
    static private final String DELIM_TAG_LIST_REC = "[*1]";
    /** Must be synchronized with client code. */
    static private final String DELIM_TAG_LIST_PAIR = "[*2]";
    /** Must be synchronized with client code. */
    static private final String REC_RES_INFO_RECNAME = "resInfo";
    /** Must be synchronized with client code. */
    static private final String REC_RTF_RECNAME = "rtfSnippets";

    // Segment States

    /** Must be synchronized with client code. */
    static private final int ATTR_STATE_UNKNOWN = 0;
    /** Must be synchronized with client code. */
    static private final int ATTR_STATE_EXACT = 1;
    /** Must be synchronized with client code. */
    static private final int ATTR_STATE_FUZZY = 2;
    /** Must be synchronized with client code. */
    static private final int ATTR_STATE_NOMATCH = 3;
    /** Must be synchronized with client code. */
    static private final int ATTR_STATE_UNVERIFIED_EXACT = 4;

    //
    // Private Member Variables and some constants
    //

    private OutputStream m_outputStream = null;

    private long m_offset = 1;
    private StringBuffer m_index = null;
    private int m_paraCnt = 0;
    private Hashtable subflows = new Hashtable();

    private String m_wrkDocFname = null;
    private String m_idxFname = null;

    /* Mon Sep 26 22:04:03 2005 CvdL: Apparently this is all old code.

    // NOTE: This resource key and inner class structure must be
    // snychronized with the globalsight clients' resource reader (VBA)
    static private final String m_idxkey_resInfo = "resInfo";

    private class resourceInfo
    {
        String title = null;
        String resOwnerFname = null;
        String idxFname = null;
        String delimTmListRecord = null;
        String delimTmListPair = null;
        String delimTermListRecord = null;
        String delimTermListTerm = null;
        String delimTagListRecord = null;
        String delimTagListPair = null;
        String editorPlaceholder1 = null;
    }

    // NOTE: This resource key and inner class structure must be
    //       snychronized with the globalsight clients' resource reader (VBA)
    private final String m_idxkey_rtfSnippet = "rtfSnippets";

    private class rtfSnippets
    {
        String docStart = null;      // doc: opening rtf
        String docHeader = null;     // doc: rtf header
        String docStyleSheet = null; // doc: rtf stylesheet
        String docColorTable = null; // doc: rtf color table
        String docFontTable = null;  // doc: rtf font table
        String docEnd = null;        // doc: closing rtf
        String edStart = null;       // editor: opening rtf
        String edSrcBox = null;      // editor: source box
        String edInfoBox = null;     // editor: info box
        String edTrgBox =null;       // editor: target box
        String edMergeIndicator = null;
        String edEnd = null;         // editor: closing rtf
    }

    // NOTE: This inner class structure must be snychronized with the
    //       globalsight clients' resource reader (VBA)
    // NOTE: The index key for a segRecord is the segment id
    private class segRecord
    {
        String segId = null;
        String sourceText = null;
        String targetText = null;
        String fuzzyList = null;
        String termList = null;
        String tagList = null;
    }
    */

    //
    // Constructors
    //

    /**
     * Constructs an RTFWriterUnicode without setting the OfflinePageData.
     * You must call setOfflinePageData() before writing can begin.
     */
    public ParaViewResWriter(String p_wrkDocFname, String p_trgFname,
        String p_idxFname)
    {
        super();

        m_wrkDocFname = p_wrkDocFname;
        m_idxFname = p_idxFname;
    }

    //
    // Public Methods
    //

    /**
     * This method overrides the normal write method that is called
     * for the RTF document writers.  This write method sets a new
     * OfflinePageData and then writes the resulting RTF to a binary
     * file format instead of an RTF document.
     * @param p_page the OfflinePageData object that will be written as RTF.
     * @param p_outputStream the stream to write to
     * @param p_uiLocale the locale used to write the Header and other
     * non-translatable instructions
     */
    public void write(OfflinePageData p_page, OutputStream p_outputStream,
        Locale p_uiLocale)
        throws IOException,
               AmbassadorDwUpException
    {
        m_outputStream = p_outputStream;
        m_index = new StringBuffer();
        m_page = p_page;
        m_uiLocale = p_uiLocale;

        setDefaults();

        writeResourceInfoRecord();
        writeRTFSnippetRecord();

        // Note: Sub segment Ids never change or get merged in the
        // client but parents segments can be split or merged (and
        // thus vanish and reappear) in the client editor.
        //
        // To make the resources available to all cases, we needed two
        // methods: One to write all parent segments (regardless of
        // merge state) and one to write all subflows using their
        // current Id (which may be compound merged subID).
        //
        // If the S/M state of a parent-of-a-sub changes it does not
        // affect the sub while its in a given offline session. Upon
        // upload, all sub ids (compound or not) get resolved and
        // stored appropriately.
        //
        // Note: unfortunately, due to this change, the order of the
        // index file no longer matches the target file. So it cannot
        // be used as means to walk the file in order.

        // writeSegmentRecords();
        writeParentRecordsToBin();
        writeSubSegmentRecordsToBin();

        m_outputStream.flush();
    }

    public String getIndex()
    {
        return m_index.toString();
    }

    /**
     * Classes that extend RTFWriterUnicode should extend this method
     * to generate an RTF {\info} block with creation date, document
     * title etc. The info section becomes part of the encoded RTF
     * header.
     */
    protected String makeRTF_infoSection()
        throws AmbassadorDwUpException
    {
        // not used by binary writer
        return null;
    }

    /**
     * Classes that extend RTFWriterUnicode should extend this method
     * to create docment variables. The doc variables section is part
     * of the encoded RTF header.
     */
    protected String makeRTF_documentVariables()
        throws AmbassadorDwUpException
    {
        // not used by binary writer
        return null;
    }

    /**
     * Classes that extend RTFWriterUnicode should extend this method
     * to create the template attachment section. The template section
     * is part of the encoded RTF header.
     */
    protected  String makeRTF_templateAttachment()
        throws AmbassadorDwUpException
    {
        // not used by binary writer
        return null;
    }

    /**
     * Classes that extend RTFWriterUnicode should extend this method
     * to create the document default view section. The doc defaults
     * section is part of the encoded RTF header.
     */
    protected String makeRTF_documentDefaults()
        throws AmbassadorDwUpException
    {
        // not used by binary writer
        return null;
    }

    /**
     * Classes that extend RTFWriterUnicode should extend this method
     * to write a visible globalsight file header, a section that the
     * user sees before any segments.
     */
    protected void writeRTF_docHeader()
        throws IOException, AmbassadorDwUpException
    {
        // not used by binary writer
    }


    /**
     * Extend this method to write the main body of the document.
     * Classes that extend RTFWriterUnicode should use this method to
     * create the desired segment presentation format.
     */
    protected void writeRTF_docBody()
        throws IOException, AmbassadorDwUpException
    {
        // not used by binary writer
    }

    //
    // Private Methods
    //

    /**
     * Writes the resourceInfo record.
     */
    private void writeResourceInfoRecord()
        throws IOException
    {
        createIndexEntry(REC_RES_INFO_RECNAME, null);

        writeStr(DOCUMENTTITLE);
        writeStr(m_wrkDocFname);
        writeStr(m_idxFname);
        writeStr(DELIM_TM_LIST_REC);
        writeStr(DELIM_TM_LIST_PAIR);
        writeStr(DELIM_TERM_LIST_REC);
        writeStr(DELIM_TERM_LIST_TERM);
        writeStr(DELIM_TAG_LIST_REC);
        writeStr(DELIM_TAG_LIST_PAIR);
        writeStr(RTFEditor.PLACEHOLDER);
    }

    /**
     * Writes the rtfSnippets record.
     */
    private void writeRTFSnippetRecord()
        throws IOException
    {
        RTFEditor rtf = new RTFEditor(m_targetIsRtlLang);

        createIndexEntry(REC_RTF_RECNAME, null);

        // Must write the record in this order

        //   String docStart;
        writeStr("{");
        //   String docHeader = null;     // doc: rtf header
        writeStr(RTF_HEADER_START);
        //   String docStyleSheet = null; // doc: rtf stylesheet
        writeStr(makeStyleSheet());
        //   String docColorTable = null; // doc: rtf color table
        writeStr(COLOR_TABLE);
        //   String docFontTable = null;  // doc: rtf font table
        writeStr(RTF_FONT_TABLE_START + RTF_FONT_TABLE_END);
        //   String docEnd = null;        // doc: closing rtf
        writeStr("}");
        //   String edStart = null;       // editor: opening rtf
        writeStr(rtf.getEditorStart());
        //   String edSrcBox = null;      // editor: source box
        writeStr(rtf.getSrcBox());
        //   String edInfoBox = null;     // editor: info box
        writeStr(rtf.getInfoBox());
        //   String edTrgBox =null;       // editor: target box
        writeStr(rtf.getTrgBox());
        //   String edMergeIndicator = null;
        writeStr(rtf.getMergeIndicator());
        //   String edEnd = null;         // editor: closing rtf
        writeStr(rtf.getEditorEnd());
    }


    /**
     * Writes the segment records to binary file.
     */
    // private void writeSegmentRecords()
    //     throws IOException
    // {
    //     OfflineSegmentData osd_sub = null;
    //     Iterator segIt = m_page.getAllUnmergedSegmentIdIterator();
    //
    //     while (segIt.hasNext())
    //     {
    //         OfflineSegmentData offlineTu = null;
    //
    //         offlineTu = m_page.getResourceByDisplayId((String)segIt.next());
    //
    //         // Write index
    //         if (offlineTu.isSubflowSegment())
    //         {
    //             // accumulate subs for later writing to the index,
    //             // in order to create a proper para id
    //             subflows.put(new Long(m_offset), offlineTu);
    //         }
    //         else
    //         {
    //             // now write the subflow index entries (if any)
    //             // for the previous paragraph
    //             if (offlineTu.isStartOfNewPara())
    //             {
    //                writeSubflowIndexEntries();
    //             }
    //             createIndexEntry(offlineTu.getDisplaySegmentID(), offlineTu);
    //         }
    //
    //         // Write Binary record file
    //         // Note: MUST be written in the following order
    //
    //         // String segId = null;
    //         writeStr(offlineTu.getDisplaySegmentID());
    //         // String sourceText = null;
    //         writeStr(makeRTF_docSegment(
    //             null, offlineTu.getDisplaySourceText(),
    //             m_strSourceTextStyle, m_strDefaultPtagStyle,
    //             m_targetIsRtlLang, true,
    //             offlineTu.getDisplaySegmentFormat(), true));
    //
    //         // String targetText = null;
    //         // writeStr(makeRTF_docSegment(
    //         //   null, offlineTu.getDisplayTargetText(),
    //         //   m_strTargetTextStyle, m_strDefaultPtagStyle,
    //         //   m_targetIsRtlLang, true,
    //         //   offlineTu.getDisplaySegmentFormat(), true));
    //
    //         // currently, there is no need to record the downloaded target
    //         writeStr("{ trg_not_dwld }");
    //         //                String fuzzyList = null;
    //         writeStr(make_tmResources(offlineTu));
    //         //                String termList = null;
    //         writeStr(make_TermResources(offlineTu));
    //         //                String tagList = null;
    //         writeStr(makeTagResource(offlineTu));
    //     }
    //
    //     // now write the subflow index entries (if any)
    //     // for the previous paragragh
    //     writeSubflowIndexEntries();
    //}

    /**
     * Writes the parent segment records to binary file.
     */
    private void writeParentRecordsToBin()
        throws IOException
    {
        OfflineSegmentData osd_sub = null;
        Iterator segIt = m_page.getAllUnmergedSegmentIdIterator();

        while (segIt.hasNext())
        {
            OfflineSegmentData offlineTu = null;

            offlineTu = m_page.getResourceByDisplayId((String)segIt.next());

            // Write index
            if (!offlineTu.isSubflowSegment())
            {
                createIndexEntry(offlineTu.getDisplaySegmentID(), offlineTu);

                // Write Binary record file
                // Note: MUST be written in the following order

                // String segId = null;
                writeStr(offlineTu.getDisplaySegmentID());
                // String sourceText = null;
                writeStr(makeRTF_docSegment(
                    null, offlineTu.getDisplaySourceText(),
                    m_strSourceTextStyle, m_strDefaultPtagStyle,
                    m_targetIsRtlLang, true, offlineTu.getDisplaySegmentFormat(),
                    true, null, null));

                // String targetText = null;
                // writeStr(makeRTF_docSegment(
                //   null, offlineTu.getDisplayTargetText(),
                //   m_strTargetTextStyle, m_strDefaultPtagStyle,
                //   m_targetIsRtlLang, true, offlineTu.getDisplaySegmentFormat(),
                //   true));

                // currently, there is no need to record the downloaded target
                writeStr("{ trg_not_dwld }");
                // String fuzzyList = null;
                writeStr(make_tmResources(offlineTu));
                // String termList = null;
                writeStr(make_TermResources(offlineTu));
                // String tagList = null;
                writeStr(makeTagResource(offlineTu));
            }
        }
    }

    /**
     * Writes the subflow segment records.
     */
    private void writeSubSegmentRecordsToBin()
        throws IOException
    {
        OfflineSegmentData osd_sub = null;
        Iterator segIt = m_page.getSegmentIterator();

        while (segIt.hasNext())
        {
            OfflineSegmentData offlineTu = (OfflineSegmentData)segIt.next();

            // Write index
            if (offlineTu.isSubflowSegment())
            {
                createIndexEntry(offlineTu.getDisplaySegmentID(), offlineTu);

                // Write Binary record file
                // Note: MUST be written in the following order

                // String segId = null;
                writeStr(offlineTu.getDisplaySegmentID());
                // String sourceText = null;
                writeStr(makeRTF_docSegment(
                    null, offlineTu.getDisplaySourceText(),
                    m_strSourceTextStyle, m_strDefaultPtagStyle,
                    m_targetIsRtlLang, true, offlineTu.getDisplaySegmentFormat(),
                    true, null, null));

                // String targetText = null;
                // writeStr(makeRTF_docSegment(
                //   null, offlineTu.getDisplayTargetText(),
                //   m_strTargetTextStyle, m_strDefaultPtagStyle,
                //   m_targetIsRtlLang, true, offlineTu.getDisplaySegmentFormat(),
                //   true));

                // currently, there is no need to record the downloaded target
                writeStr("{ trg_not_dwld }");
                // String fuzzyList = null;
                writeStr(make_tmResources(offlineTu));
                // String termList = null;
                writeStr(make_TermResources(offlineTu));
                // String tagList = null;
                writeStr(makeTagResource(offlineTu));
            }
        }

        // Now write the subflow index entries (if any) for the
        // previous paragraph.

        // writeSubflowIndexEntries();
    }

    private void writeSubflowIndexEntries()
    {
        Set keys = subflows.keySet();
        Iterator it = keys.iterator();
        Long offsetKey = null;
        OfflineSegmentData osd = null;

        while (it.hasNext())
        {
            offsetKey = (Long)it.next();
            osd = (OfflineSegmentData)subflows.get(offsetKey);
            createIndexEntry(offsetKey.longValue(),
                osd.getDisplaySegmentID(), osd);
        }

        subflows.clear();
    }

    private String make_tmResources(OfflineSegmentData p_osd)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        sb.append(makeDefaultInfoLine(p_osd));
        sb.append(DELIM_TM_LIST_PAIR);

        if (p_osd.hasTMMatches())
        {
            Iterator fuzDisplayIt = p_osd.getDisplayFuzzyMatchList().iterator();
            Iterator origLmIt = p_osd.getOriginalFuzzyLeverageMatchList().iterator();
            LeverageMatch origLm = null;
            int totalNumFuz = p_osd.getDisplayFuzzyMatchList().size();
            int cnt = 0;
            StringBuffer infoText = new StringBuffer();
            
            while (fuzDisplayIt.hasNext())
            {
                cnt++;
                sb.append(DELIM_TM_LIST_REC);

                // create info text
                infoText.append("Fuzzy match ");
                infoText.append(Integer.toString(cnt));
                infoText.append(" of ");
                infoText.append(Integer.toString(totalNumFuz));
                infoText.append("      ");
                infoText.append("Score ");

                origLm = (LeverageMatch)origLmIt.next();

                infoText.append(origLm != null ?
                    StringUtil.formatPercent(origLm.getScoreNum(), 2) : "");

                if (p_osd.hasTerminology())
                {
                    infoText.append("       \\ul");
                    infoText.append(makeRTF_field(false, false, false, true,
                        " MACROBUTTON getTerms Click here for Terms", null));
                }

                // show segment format if different from the original main doc format
                if (p_osd.getDisplaySegmentFormat().toLowerCase().equals("html") &&
                    !p_osd.getSegmentType().equals("text"))
                {
                    infoText.append(",    Item type=");
                    infoText.append(p_osd.getSegmentType()); // is item type
                }
                else if (!p_osd.getDisplaySegmentFormat().toLowerCase().equals(
                    m_page.getDocumentFormat()))
                {
                    infoText.append(",    Embedded format=");
                    infoText.append(p_osd.getSegmentType()); // is format type
                }

                // add the info entry
                // Example: Fuzzy match 1 of 3      Score 90%      Click here for Terms
                sb.append("{");
                sb.append(m_strEditorInfoTextStyle);
                sb.append(infoText.toString());
                sb.append("}" );

                sb.append(DELIM_TM_LIST_PAIR);

                // add the fuzzy match
                sb.append(makeRTF_docSegment(null, (String)fuzDisplayIt.next(),
                    m_strTargetTextStyle, m_strDefaultPtagStyle, m_targetIsRtlLang,
                    true, p_osd.getDisplaySegmentFormat(), true, null, null));

                infoText.setLength(0);
            }
        }

        return sb.toString();
    }

    private String make_TermResources(OfflineSegmentData p_osd)
    {
        StringBuffer sb = new StringBuffer();

        if (p_osd.hasTerminology())
        {
            Iterator it = p_osd.getTermLeverageMatchList().iterator();

            // add terms
            while (it.hasNext())
            {
                sb.append(formatTerm((TermLeverageMatchResult)it.next()));

                if (it.hasNext())
                {
                    sb.append(DELIM_TERM_LIST_REC);
                }
            }
        }

        return sb.toString();
    }

    private String formatTerm(TermLeverageMatchResult p_tlm)
    {
        StringBuffer sb = new StringBuffer();
        String nextTarget = null;

        // format source term
        sb.append("{" + m_strSourceLcid + FONT_SOURCE + SIZE_12PT + COLOR_GREY + " ");
        sb.append(encodeText(p_tlm.getSourceTerm()));
        sb.append("}");

        // format target terms for this source term
        sb.append("{" + m_strTargetLcid);
        sb.append(FONT_TARGET + SIZE_12PT + COLOR_BLACK + DELIM_TERM_LIST_TERM);
        sb.append(encodeText(p_tlm.getFirstTargetTerm()));

        while ((nextTarget = p_tlm.getNextTargetTerm()) != null)
        {
            sb.append(encodeText(DELIM_TERM_LIST_TERM + nextTarget));
        }

        sb.append("}");

        return sb.toString();
    }


    private String makeTagResource(OfflineSegmentData p_offlineTu)
        throws IOException
    {
        StringBuffer  sb = new StringBuffer();
        String pTag = null;
        String nativeTag = null;

        // Sort, format and add each ptag/native pair.
        Map tagMap = p_offlineTu.getPTag2NativeMap();
        if (tagMap != null && tagMap.size() > 0)
        {
            String keys[] = HtmlTableWriter.getSortedPtagKeys((Hashtable)tagMap);

            for (int i = 0; i < keys.length; )
            {
                pTag = keys[i];
                nativeTag = (String)tagMap.get(pTag);
                sb.append(formatTagMap(pTag, nativeTag));

                i++;

                if (i < keys.length)
                {
                    sb.append(DELIM_TAG_LIST_REC);
                }
            }
        }

        return sb.toString();
    }

    private String formatTagMap(String p_pTag, String p_nativeTag)
    {
        StringBuffer sb = new StringBuffer();

        // Word 2003 has new behavior for initializing ActiveX controls.

        // Users would encounter messages about unsafe initialization,
        // so we just stick to plain text and a list control instead
        // of RTF control.

        // http://support.microsoft.com/kb/817112/
        // http://support.microsoft.com/default.aspx?scid=kb;en-us;827742

        // We need to use RTF to encode characters in unicode and then
        // let Word do the conversion text same as we do for terms.
        // Since we end up with plain text for now lets remove all RTF
        // formatting except for character encoding.  If we use an RTF
        // control later we can enable the formatting.

        // sb.append("{");
        // sb.append(m_strSourceLcid);
        // sb.append(FONT_SOURCE);
        // sb.append(SIZE_12PT);
        // sb.append(COLOR_BLUE);
        // sb.append(" ");
        sb.append(encodeText(p_pTag));
        sb.append(DELIM_TAG_LIST_PAIR);
        sb.append(encodeText(p_nativeTag));
        // sb.append("}");

        return sb.toString();
    }

    private String makeDefaultInfoLine(OfflineSegmentData p_osd)
        throws IOException
    {

        StringBuffer infoText = new StringBuffer();
        String fuzCnt = "0";

        // write current document text info line
        // Example: Current document text  Fuzzy matches (ALT+ N) = 3,  Click here for Terms
        infoText.append("{ ");
        infoText.append(m_strEditorInfoTextStyle);

        // We build a default info line now and append this on the client side.
        // infoText.append(p_osd.isWriteAsProtectedSegment() ?
        //   " Locked Segment      " : " urrent  document text      ");

        switch (p_osd.getMatchTypeId())
        {
        case MATCH_TYPE_EXACT:
            infoText.append("Exact Match");

            if (p_osd.hasTMMatches())
            {
                fuzCnt = Integer.toString(
                    p_osd.getOriginalFuzzyLeverageMatchList().size());

                infoText.append(" :  Alternates (ALT+N)=");
                infoText.append(fuzCnt);
            }
            break;

        case MATCH_TYPE_UNVERIFIED_EXACT:
            infoText.append("Unverified Exact Match");

            if (p_osd.hasTMMatches())
            {
                fuzCnt = Integer.toString(
                    p_osd.getOriginalFuzzyLeverageMatchList().size());

                infoText.append(" :  Alternates (ALT+N)=");
                infoText.append(fuzCnt);
            }
            break;

        case MATCH_TYPE_FUZZY:
            if (p_osd.hasTMMatches())
            {
                fuzCnt = Integer.toString(
                    p_osd.getOriginalFuzzyLeverageMatchList().size());
            }

            infoText.append("Has fuzzy match (ALT+N)=");
            infoText.append(fuzCnt);
            break;

        default: // fall through
        case MATCH_TYPE_NOMATCH:
            infoText.append("Has no match");
            break;
        }

        // show segment format if different from the original main doc format
        if (p_osd.getDisplaySegmentFormat().toLowerCase().equals("html") &&
            !p_osd.getSegmentType().equals("text"))
        {
            infoText.append(",    Item type=");
            infoText.append(p_osd.getSegmentType()); // show item type instead
        }
        else if (!p_osd.getDisplaySegmentFormat().toLowerCase().equals(
            m_page.getDocumentFormat()))
        {
            infoText.append(",    Item type=");
            infoText.append(p_osd.getSegmentType());
        }

        if (p_osd.hasTerminology())
        {
            infoText.append(",      \\ul");
            infoText.append(makeRTF_field(false, false, false, true,
                " MACROBUTTON getTerms Click here for Terms", null));
        }

        infoText.append("}");

        return infoText.toString();
    }


    /**
     * Writes a string with leading two byte length so VBA can read it.
     * @param p_str a unicode RTF string (unicode rtf is all ascii no
     * matter what the lang).
     */
    private void writeStr(String p_str)
        throws IOException
    {
        ByteBuffer b1 = ByteBuffer.allocate(10); // defaults to BIG_ENDIAN
        b1.order(ByteOrder.LITTLE_ENDIAN);
        byte[] b2;

        b2 = p_str.getBytes("ASCII");
        int len = b2.length;
        b1.putShort((short)len);
        m_outputStream.write(b1.get(0)); // first byte of VBA two byte string length
        m_outputStream.write(b1.get(1)); // second byte of VBA two byte string length
        m_outputStream.write(b2);        // the actual string
        m_offset += 2 + b2.length;
    }

    /**
     * Appends a new entry to the index using the current offest value.
     *
     * Index record = [key][,][offset][,][paraId][,][locked][,][isSubflow][,][matchTypeId][,][addableCat]
     **/
    private void createIndexEntry( String p_key, OfflineSegmentData p_osd)
    {
        createIndexEntry(m_offset, p_key, p_osd);
    }

    /**
     * Appends a new entry to the index using the specified offest value.
     *
     * Index record = [key][,][offset][,][paraId][,][locked][,][isSubflow][,][matchTypeId][,][addableCat]
     **/
    private void createIndexEntry(long offset, String p_key,
        OfflineSegmentData p_osd)
    {
        // write key/offest pair
        m_index.append(p_key);
        m_index.append(DELIM_IDX_1);
        m_index.append(Long.toString(offset));

        // now if this is a segment record entry, append the attributes
        if (p_osd != null)
        {
            // Att: para id
            m_index.append(DELIM_IDX_2);

            if (p_osd.isStartOfNewPara())
            {
                m_paraCnt += 1;
            }

            m_index.append(Integer.toString(m_paraCnt));

            // Att: locked ( VBA Boolean True==-1, False==0 )
            m_index.append(DELIM_IDX_2);
            m_index.append(p_osd.isWriteAsProtectedSegment() ? "-1" : "0");

            // Att: is subflow ( VBA Boolean True==-1, False==0 )
            m_index.append(DELIM_IDX_2);
            m_index.append(p_osd.isSubflowSegment() ? "-1" : "0");

            // Att: state
            m_index.append(DELIM_IDX_2);
            m_index.append(getStateIdAsString(p_osd));

            // Att: abbadles category
            m_index.append(DELIM_IDX_2);
            m_index.append(getAddablesDataTypeId(p_osd));

            // Att: touched
            m_index.append(DELIM_IDX_2);
            m_index.append(p_osd.isTouched() ? "-1" : "0");
        }

        m_index.append("\r\n");
    }

    private String getAddablesDataTypeId(OfflineSegmentData p_osd)
    {
        if (p_osd.getDisplaySegmentFormat().toLowerCase().equals("html") &&
            p_osd.getSegmentType().toLowerCase().equals("text"))
        {
            return "html-text";
        }
        else
        {
            return p_osd.getDisplaySegmentFormat().toLowerCase();
        }
    }

    private String getStateIdAsString(OfflineSegmentData p_osd)
    {
        return Integer.toString(getStateId(p_osd));
    }

    private int getStateId(OfflineSegmentData p_osd)
    {
        int res = 0;

        switch (p_osd.getMatchTypeId())
        {
        case AmbassadorDwUpConstants.MATCH_TYPE_EXACT:
            res = ATTR_STATE_EXACT;
            break;
        case AmbassadorDwUpConstants.MATCH_TYPE_FUZZY:
            res = ATTR_STATE_FUZZY;
            break;
        case AmbassadorDwUpConstants.MATCH_TYPE_NOMATCH:
            res = ATTR_STATE_NOMATCH;
            break;
        case AmbassadorDwUpConstants.MATCH_TYPE_UNVERIFIED_EXACT:
            res = ATTR_STATE_UNVERIFIED_EXACT;
            break;
        default:
            res = ATTR_STATE_UNKNOWN;
            break;
        }

        return res;
    }
}
