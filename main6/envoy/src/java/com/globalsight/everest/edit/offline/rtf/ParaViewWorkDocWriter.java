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

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.download.DownloadHelper;
import com.globalsight.ling.common.LCID;
import com.globalsight.ling.common.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.StringBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * <p>Generates an RTF file from an OfflinePageData object.</p>
 *
 * <p>The output is written as ASCII characters to the provided
 * OutputStream.  Unicode or double-byte characters are converted to
 * the proper RTF codes as necessary.  Note that RTF files are always
 * ASCII files.</p>
 *
 */
public class ParaViewWorkDocWriter
    extends RTFWriterUnicode
    implements ParaViewWorkDocConstants
{
    static private final Logger c_logger =
        Logger.getLogger(
            ParaViewWorkDocWriter.class);

    private static final boolean _ENABLE_DEBUG_OF_DOWNLOAD_ABORT = false;

    /** Name of the variable that holds the target LCID. */
    public static final String DOCVAR_TRG_LCID = "trgLCID";
    /** Name of the variable that holds the source LCID. */
    public static final String DOCVAR_SRC_LCID = "srcLCID";

    //
    // Private Member Variables
    //

    private String m_uniqueBinResFname = null;
    private String m_uniqueResIdxFname = null;
    // private String m_uniqueSrcDocName = null;
    // private String m_uniqueTmDocName = null;
    // private String m_uniqueTagDocName = null;
    // private String m_uniqueTermDocName = null;

    private User m_user = null;

    //
    // Constructors
    //

    /**
     * Constructs an ParaViewWorkDocWriter. A ParaViewWorkDoc is an
     * RTF file which is pre-attached to an offline MSWord templated.
     * Together with the template, this file is designed to funtion as
     * an embedded MSWord application.  Users can access TM and Term
     * resources from within MSWord using the template macros.
     *
     * @param p_uniqueBinResfname the name of the associated binary
     * resource file.
     * @param p_uniqueREsIdxName the name of the associated binary
     * index file.
     */
    public ParaViewWorkDocWriter(String p_uniqueBinResFname,
        String p_uniqueResIdxFname, User p_user)
    {
        super();

        m_uniqueBinResFname = p_uniqueBinResFname;
        m_uniqueResIdxFname = p_uniqueResIdxFname;
        m_user = p_user;
    }


    //
    // Public Methods
    //


    /**
     * Writes the main body of the document.  Classes that extend
     * RTFWriterUnicode method should overide this method to create
     * the document of choice.
     */
    public void writeRTF_docBody()
        throws IOException
    {
        ListIterator it = m_page.getSegmentIterator();
        OfflineSegmentData offlineTu = null;
        ArrayList subflows = new ArrayList();

        if (_ENABLE_DEBUG_OF_DOWNLOAD_ABORT)
        {
            // Used to test download abort messages:
            // Word limit is 16379 bookmarks. We allow up to 16369 to
            // reserve a few for runtime creation in word.  The word
            // limit for fields is 32000.
            //debug_writeRTF_dummyBookmarks(16369);
            debug_writeRTF_dummyFields(32000);
        }
        else
        {
            while (it.hasNext())
            {
                offlineTu = (OfflineSegmentData)it.next();

                if (offlineTu.isSubflowSegment())
                {
                    subflows.add(offlineTu);
                }
                else
                {
                    if (offlineTu.isStartOfNewPara())
                    {
                        // write the subs for the previous paragragh
                        write_docSubflows(subflows);
                        subflows.clear();
                    }

                    write_docSegment(offlineTu);
                }
            }

            // write the subs for the last paragragh
            write_docSubflows(subflows);
        }
    }

    /**
     * Generates an RTF {\info} block with creation date, document
     * title etc.
     */
    public String makeRTF_infoSection()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{\\info");
        sb.append("{\\title ");
        sb.append(WORK_DOC_TITLE);
        sb.append("}");

        sb.append("{\\author ");
        sb.append(PRODUCTNAME);
        sb.append("}");

        // sb.append("{\\*\\company ");
        // sb.append(str_company);
        // sb.append("}");
        // sb.append(m_strEOL);

        Calendar dt_creationDate = GregorianCalendar.getInstance();
        sb.append("{\\creatim ");
        sb.append("\\yr").append(dt_creationDate.get(Calendar.YEAR));
        sb.append("\\mo").append(dt_creationDate.get(Calendar.MONTH));
        sb.append("\\dy").append(dt_creationDate.get(Calendar.DAY_OF_MONTH));
        sb.append("\\hr").append(dt_creationDate.get(Calendar.HOUR_OF_DAY));
        sb.append("\\min").append(dt_creationDate.get(Calendar.MINUTE));
        sb.append("}");

        sb.append("}");

        return sb.toString();
    }

    /**
     * Creates docment variables.
     */
    public String makeRTF_documentVariables()
    {
        StringBuffer sb = new StringBuffer();

        // standard page data - some required for upload
        sb.append(makeRTF_docVar(DOCVAR_NAME_DOCVERSION, WC_VERSION));
        //sb.append(makeRTF_docVar(DOCVAR_NAME_SRCDOC, m_uniqueSrcDocName));
        //sb.append(makeRTF_docVar(DOCVAR_NAME_TERMDOC, m_uniqueTermDocName));
        //sb.append(makeRTF_docVar(DOCVAR_NAME_TMDOC, m_uniqueTmDocName));
        //sb.append(makeRTF_docVar(DOCVAR_NAME_TAGDOC, m_uniqueTagDocName));
        sb.append(makeRTF_docVar(DOCVAR_NAME_BINRES, m_uniqueBinResFname));
        sb.append(makeRTF_docVar(DOCVAR_NAME_RESIDX, m_uniqueResIdxFname));
        sb.append(makeRTF_docVar(DOCVAR_NAME_SRCLOCALE,
            m_page.getSourceLocaleName()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_TRGLOCALE,
            m_page.getTargetLocaleName()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_ENCODING, RTF_VER));
        sb.append(makeRTF_docVar(DOCVAR_NAME_EXACTCNT,
            m_page.getExactMatchWordCountAsString()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_FUZZYCNT,
            m_page.getFuzzyMatchWordCountAsString()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_NOMATCHCNT,
            m_page.getNoMatchWordCountAsString()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_NATIVEDOCFMT,
            m_page.getDocumentFormat()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_WORKFLOWID,
            m_page.getWorkflowId()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_TASKID, m_page.getTaskId()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_PAGEID, m_page.getPageId()));
        sb.append(makeRTF_docVar(DOCVAR_NAME_PTAGFMT,
            m_page.getPlaceholderFormat()));

        // client UI options
        sb.append(makeRTF_docVar(DOCVAR_NAME_SHOWSRCPARA,"False"));
        sb.append(makeRTF_docVar(DOCVAR_NAME_BMKPOPUPWARNINGS,"False"));
        sb.append(makeRTF_docVar(DOCVAR_NAME_COLOREXACT,"False"));
        sb.append(makeRTF_docVar(DOCVAR_NAME_COLORFUZZY,"False"));
        sb.append(makeRTF_docVar(DOCVAR_NAME_PTAGPRESENTATION,"dim"));

        // client page parameters
        sb.append(makeRTF_docVar(DOCVAR_NAME_MERGEDATA,
            makeDocVarValueMergeData()));

        sb.append(makeRTF_docVar(DOCVAR_TRG_LCID,
            Integer.toString(m_targetLcid)));
        sb.append(makeRTF_docVar(DOCVAR_SRC_LCID,
            Integer.toString(m_sourceLcid)));

        return sb.toString();
    }

    public String makeRTF_templateAttachment()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{\\*\\template ");
        sb.append(MSWORD_TRANS_TEMPLATE);
        sb.append("}");

        return sb.toString();
    }

    public String makeRTF_documentDefaults()
    {
        // get basic document defaults
        String str_commonDefaults = "";
        try
        {
            str_commonDefaults = m_resource.getString("DocumentDefaults");
        }
        catch (MissingResourceException ex)
        {
            c_logger.warn("Could not load RTF document default view options", ex);
        }

        StringBuffer sb = new StringBuffer();

        sb.append("\\margl720\\margr720");        //default to normal style cs0
        sb.append(str_commonDefaults);

        return sb.toString();
    }


    /**
     * Writes the document's User header - encoding, formats, languages etc.
     */
    public void writeRTF_docHeader()
        throws IOException, AmbassadorDwUpException
    {
        StringBuffer fldInst = new StringBuffer();

        m_outputStream.write("\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(" {\\b ");
        m_outputStream.write(m_strLabelAndHeaderTextStyle);
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline1"));
        m_outputStream.write("\\b0  ");
        m_outputStream.write(m_strEOL);

        // macro button - toggle header
        fldInst.append("{\\ul ");
        fldInst.append(m_strLabelAndHeaderTextStyle);
        fldInst.append("MACROBUTTON toggleHeader ");
        fldInst.append(m_localeRes.getString("RTFWorkDoc1Headerline2"));
        fldInst.append("}");
        m_outputStream.write(makeRTF_field(false,true,false,true, fldInst.toString(), ""));
        m_outputStream.write(m_strEOL);

        m_outputStream.write("\\par");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(" {\\qr ");
        m_outputStream.write(SIZE_3PT);
        m_outputStream.write(" ");
        m_outputStream.write(makeRTF_bookmark(BM_OPEN, "header"));
        m_outputStream.write("\\par}\\par ");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline3"));
        m_outputStream.write("\\par\\par \\b ");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline4"));
        m_outputStream.write("\\par \\b0 ");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline5"));
        m_outputStream.write("\\par ");
        m_outputStream.write(m_strEOL);

        //macro button - toggle template attatchment instructions
        fldInst = new StringBuffer();
        fldInst.append("{ \\ul ");
        fldInst.append(m_strLabelAndHeaderTextStyle);
        fldInst.append("MACROBUTTON toggleTemplateHelp ");
        fldInst.append(m_localeRes.getString("RTFWorkDoc1Headerline6"));
        fldInst.append("}");
        m_outputStream.write(makeRTF_field(false,true,false,true, fldInst.toString(), ""));
        m_outputStream.write(m_strEOL);
        m_outputStream.write("\\par {\\qr ");
        m_outputStream.write(SIZE_3PT);
        m_outputStream.write(" ");
        m_outputStream.write(makeRTF_bookmark(BM_OPEN, "tmplInst"));
        m_outputStream.write("\\par} ");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("{");
        m_outputStream.write(HIDDEN);
        m_outputStream.write(" ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst1"));
        m_outputStream.write("\\par \\tab 1) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst2"));
        m_outputStream.write("\\par \\tab\\tab { \\i ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst3"));
        m_outputStream.write("}\\par \\tab 2) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst4"));
        m_outputStream.write("\\par \\tab 3) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst5"));
        m_outputStream.write("\\par \\tab 4) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst6"));
        m_outputStream.write("\\par \\tab 5) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst7"));
        m_outputStream.write("\\par \\tab 6) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst8"));
        m_outputStream.write("\\par \\tab 7) ");
        m_outputStream.write(m_localeRes.getString("RTFTemplateConnectInst9"));
        m_outputStream.write("\\par { \\qr ");
        m_outputStream.write(SIZE_3PT);
        m_outputStream.write(makeRTF_bookmark(BM_CLOSE, "tmplInst"));
        m_outputStream.write(" \\par} }");
        m_outputStream.write(m_strEOL);

        m_outputStream.write("\\par ");
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline7"));
        m_outputStream.write("\\par ");
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline8"));
        m_outputStream.write("\\par\\par ");
        m_outputStream.write(m_strEOL);

        //macro button - open job details dialog
        fldInst = new StringBuffer();
        fldInst.append("{ \\ul ");
        fldInst.append(m_strLabelAndHeaderTextStyle);
        fldInst.append("MACROBUTTON showJobInfo ");
        fldInst.append(m_localeRes.getString("RTFWorkDoc1Headerline9"));
        fldInst.append("}");
        m_outputStream.write(makeRTF_field(false,true,false,true, fldInst.toString(), ""));
        m_outputStream.write(m_strEOL);
        m_outputStream.write("\\par { \\qr ");
        m_outputStream.write(SIZE_3PT);
        m_outputStream.write(makeRTF_bookmark(BM_CLOSE, "header"));
        m_outputStream.write("  \\par} ");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_localeRes.getString("RTFWorkDoc1Headerline10"));

        m_outputStream.write("} \\cs0\\par ");    //default to normal style cs0
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
    }


    //
    // Private Methods
    //


    private void write_docSegment(OfflineSegmentData p_osd)
        throws IOException
    {
        boolean protect = false;
        String trgText = p_osd.getDisplayTargetTextWithNewLineBreaks(
            String.valueOf(NORMALIZED_LINEBREAK));

        if (trgText.length() == 0)
        {
            trgText = WC_EMPTY_SEG_PLACEHOLDER_TEXT;
        }

        if (p_osd.isStartOfNewPara() && !p_osd.isSubflowSegment())
        {
            m_outputStream.write("\\par\\par");
            m_outputStream.write(m_strEOL);
        }

        if (p_osd.isCopyOfSource())
        {
            // This is actually a copy of the source segment.
            writeRTF_docSegment(newMakeBookmarkName(p_osd), trgText,
                p_osd.getMatchTypeId() == MATCH_TYPE_FUZZY ?
                m_strSourceTextFuzzyStyle : m_strSourceTextStyle,
                m_strDefaultPtagStyle, m_sourceIsRtlLang, true,
                p_osd.getDisplaySegmentFormat(), true,
                p_osd.getSegmentIssue(), m_user);
        }
        else
        {
            StringBuffer textStyle = new StringBuffer();
            if (p_osd.isWriteAsProtectedSegment())
            {
                // Special treatment for protected exact matches (if requested)
                textStyle.append(m_strTargetTextLockedExactStyle);
                textStyle.append(p_osd.isTouched()? BG_PATTERN_PROGRESS : "");

                writeRTF_docSegment(newMakeBookmarkName(p_osd), trgText,
                    textStyle.toString(), m_strDefaultPtagStyle,
                    m_targetIsRtlLang, true, p_osd.getDisplaySegmentFormat(),
                    true, p_osd.getSegmentIssue(), m_user);
            }
            else
            {
                switch (p_osd.getMatchTypeId())
                {
                case MATCH_TYPE_EXACT:
                    textStyle.append(m_strTargetTextExactStyle);
                    break;
                case MATCH_TYPE_UNVERIFIED_EXACT:
                    textStyle.append(m_strTargetTextUnverifiedExactStyle);
                    break;
                case MATCH_TYPE_FUZZY:
                    textStyle.append(m_strTargetTextFuzzyStyle);
                    break;
                default:
                    textStyle.append(m_strTargetTextStyle);
                    break;
                }

                textStyle.append(p_osd.isTouched() ? BG_PATTERN_PROGRESS : "");

                writeRTF_docSegment(newMakeBookmarkName(p_osd), trgText,
                    textStyle.toString(), m_strDefaultPtagStyle,
                    m_targetIsRtlLang, true, p_osd.getDisplaySegmentFormat(),
                    true, p_osd.getSegmentIssue(), m_user);
            }
        }
    }

    private void write_docSubflows(Collection p_subflows)
        throws IOException
    {
        if (p_subflows == null)
        {
            return;
        }

        OfflineSegmentData osd = null;
        StringBuffer sb = new StringBuffer();

        Iterator it = p_subflows.iterator();

        if (it.hasNext())
        {
            m_outputStream.write("\\par");
            m_outputStream.write(m_strEOL);
        }

        while (it.hasNext())
        {
            osd = (OfflineSegmentData)it.next();

            sb.append(" MACROBUTTON sub Subflow ");
            sb.append(osd.getSubflowParentTagName());
            sb.append(": ");

            m_outputStream.write("\\par");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("{");
            m_outputStream.write(m_strLabelAndHeaderTextStyle);
            m_outputStream.write("    ");
            m_outputStream.write(
                makeRTF_field(false, false, false, true, sb.toString(), ""));
            m_outputStream.write("}");
            m_outputStream.write(m_strEOL);
            write_docSegment(osd);
            m_outputStream.write(m_strEOL);

            sb.setLength(0);
        }
    }


    /**
     * Returns the complete listing of merged segments in a form that
     * is ready to be written as a document variable.
     */
    private String makeDocVarValueMergeData()
    {
        StringBuffer sb = new StringBuffer();
        boolean firstRecord = true;
        boolean firstEntryInNewRec;

        for (Iterator it = m_page.getSegmentIterator(); it.hasNext(); )
        {
            firstEntryInNewRec = true;

            OfflineSegmentData parentOSD = (OfflineSegmentData)it.next();

            if (parentOSD.isMerged())
            {
                if (!firstRecord)
                {
                    sb.append(MERGE_RECORD_DELIM);
                }
                else
                {
                    firstRecord = false;
                }

                // add record entries
                List l = parentOSD.getMergedIds();

                for (Iterator it2 = l.iterator(); it2.hasNext(); )
                {
                    Long id = (Long)it2.next();

                    OfflineSegmentData mergedOSD =
                        (OfflineSegmentData)m_page.getResourceByDisplayId(id.toString());

                    if (firstEntryInNewRec)
                    {
                        firstEntryInNewRec = false;
                    }
                    else
                    {
                        sb.append(" ");
                    }

                    sb.append(mergedOSD.getDisplaySegmentID());
                }
            }
        }

        return sb.length() <= 0 ? NO_MERGE_RECORDS : sb.toString();
    }
}
