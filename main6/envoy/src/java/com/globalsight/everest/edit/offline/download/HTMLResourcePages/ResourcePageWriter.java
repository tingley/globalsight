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
package com.globalsight.everest.edit.offline.download.HTMLResourcePages;

import java.text.MessageFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.upload.UploadPageSaverException;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.tw.HtmlTableWriter;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.ling.tw.PtagStringFormatter;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * Generates a segment resource page in html.
 * 
 * A resource page consists of one or more of the following: 1. Segment id
 * (parent and/or subflow) 2. Source segment in p-tag format. 3. P-tag to Native
 * mapping table. 4. Optional Fuzzy TM matches. 5. Optional Term matches.
 */
public class ResourcePageWriter extends DownloadWriter implements
        DownloadWriterInterface
{
    static private final Logger CATEGORY = Logger
            .getLogger(ResourcePageWriter.class);

    static private final String RESOURCE_PAGE_START = "ResourcePageStart";
    static private final String RESOURCE_PAGE_HEADER = "ResourcePageHeader";
    static private final String SEGMENT = "Segment";
    static private final String RESOURCE_PAGE_END = "ResourcePageEnd";
    static private final int SEG_PARAM_COUNT = 16;
    static private final String PTAG_COLOR_START = "<font color=\"red\">";
    static private final String PTAG_COLOR_END = "</font>";
    static private final String LABEL_PAGE_TITLE = "PageTitle";

    static private final String LABEL_PMAP_TITLE = "MappingTableTitle";
    static private final String LABEL_PMAPCOL_PLACEHOLDER = "MappingTablePlaceholderCol";
    static private final String LABEL_PMAPCOL_CONTENT = "MappingTableContentCol";
    static private final String HTML_TEMPLATE_MAPPING_TABLE = "MappingTable";

    static private final String LABEL_FUZZY_TITLE = "FuzzyTableTitle";
    static private final String LABEL_FUZZYCOL_SCORE = "FuzzyTableScoreCol";
    static private final String LABEL_FUZZYCOL_CONTENT = "FuzzyTableContentCol";
    static private final String HTML_TEMPLATE_FUZZY_ROW = "FuzzyRow";
    static private final String HTML_TEMPLATE_FUZZY_TABLE = "FuzzyTable";

    static private final String LABEL_TERM_TITLE = "TermTableTitle";
    static private final String LABEL_TERMCOL_SOURCE = "TermTableScoreCol";
    static private final String LABEL_TERMCOL_TARGET = "TermTableContentCol";
    static private final String HTML_TEMPLATE_TERM_ROW = "TermRow";
    static private final String HTML_TEMPLATE_TERM_TABLE = "TermTable";

    private StringBuffer m_segments = null;
    private ResourceBundle m_resource = null;
    private boolean m_rtlSourceLocale = false;
    private boolean m_rtlTargetLocale = false;
    private GlobalSightLocale m_sourceLocale = null;
    private GlobalSightLocale m_targetLocale = null;
    private PtagStringFormatter m_PtagFormat = null;
    private String m_uiLocale = "en_US"; // default

    /**
     * Constructor.
     */
    public ResourcePageWriter() throws AmbassadorDwUpException
    {
        super();

        m_segments = new StringBuffer();
        m_PtagFormat = new PtagStringFormatter();
    }

    public void reset()
    {
        m_segments.setLength(0);
    }

    public void setUiLocale(String p_uiLocale)
    {
        m_uiLocale = p_uiLocale;
    }

    /**
     * Builds the final html page from the cumulative segment data pre-formatted
     * in HTML.
     * 
     * @return the full page in a StringBuffer.
     */
    protected StringBuffer buildPage() throws AmbassadorDwUpException
    {
        StringBuffer page = new StringBuffer();
        page.append(m_resource.getString(RESOURCE_PAGE_START));

        // Format = <BODY>\r\n<H1>[Title]</H1>
        // For now, the page is always generaly LTR
        // page.append( m_rtlTargetLocale ? "<BODY DIR=\"RTL\">\r\n" :
        // "<BODY>\r\n");
        page.append("<BODY>\r\n");
        page.append("<H1>");
        page.append(m_resource.getString(LABEL_PAGE_TITLE));
        page.append("</H1>");
        page.append(m_segments);
        page.append(m_resource.getString(RESOURCE_PAGE_END));

        return page;
    }

    /**
     * Extracts segment data from the page object, formats the data as an HTML
     * snippet and then stores it to be used later to build the final page.
     * 
     * @param p_page
     *            com.globalsight.everest.edit.offline.OfflinePageData
     */
    public void processOfflinePageData(OfflinePageData p_page)
            throws AmbassadorDwUpException
    {
        long jobId = p_page.getJobId();
        setPageLocales(p_page.getSourceLocaleName(),
                p_page.getTargetLocaleName());
        m_rtlSourceLocale = EditUtil.isRTLLocale(m_sourceLocale);
        m_rtlTargetLocale = EditUtil.isRTLLocale(m_targetLocale);

        m_resource = loadProperties(getClass().getName(), getLocale(m_uiLocale));

        for (ListIterator it = p_page.getSegmentIterator(); it.hasNext();)
        {
            OfflineSegmentData segment = (OfflineSegmentData) it.next();

            if (!areParamsValid(segment))
            {
                throw new AmbassadorDwUpException(
                        AmbassadorDwUpExceptionConstants.WRITER_INVALID_PARAMETER,
                        this.getClass().getName());
            }

            // A single segment entry includes id, native map, tm matches and
            // terms
            // Format = <P><SPAN CLASS="number"><A
            // NAME="[LinkId]">[SegId]</A></SPAN><BR>[SrcSeg]</P>[AllTables]*/
            String[] args = makeParamList(segment, jobId);
            boolean containsBidiChar = false;

            StringBuffer sb = new StringBuffer();
            sb.append("<P><SPAN CLASS=\"number\"><A NAME=\"");
            sb.append(args[1]);
            sb.append("\">");
            sb.append(args[2]);
            sb.append("</A></SPAN><BR>");

            if (m_rtlSourceLocale)
            {
                // Source should be tested in each of these cases:
                // bidi -> English, English -> bidi, bidi -> bidi
                containsBidiChar = Text.containsBidiChar(args[3]);
                sb.append(containsBidiChar ? "<DIV DIR=\"RTL\">" : "");
            }

            sb.append(args[3]);

            if (m_rtlSourceLocale)
            {
                sb.append(containsBidiChar ? "</DIV>" : "");
            }

            sb.append("<P>");
            sb.append(args[4]);

            m_segments.append(sb.toString());
        }
    }

    private boolean areParamsValid(OfflineSegmentData p_segment)
    {
        if (p_segment == null)
        {
            CATEGORY.error("OfflineSegmentData is null.");
            return false;
        }

        if (p_segment.getDisplaySegmentID() == null)
        {
            CATEGORY.error("SegmentID is null.");
            return false;
        }

        if (p_segment.getDisplaySourceText() == null)
        {
            CATEGORY.error("SourceText is null. SEGID="
                    + p_segment.getDisplaySegmentID());
            return false;
        }

        if (p_segment.getPTag2NativeMap() == null)
        {
            CATEGORY.error("PTagMap is null. SEGID="
                    + p_segment.getDisplaySegmentID());
            return false;
        }

        return true;
    }

    /**
     * Positions in the array are as follows: {1} : Segment id hyperlink name
     * {2} : Segment id {3} : The segment text {4} : Mapping Table title {5} :
     * Column name "placeholder" {6} : Column name "native content" {7} :
     * Mapping table {8} : TM table title {9} : TM Column name "score" {10} : TM
     * Column name "fuzzy segment" {11} : TM table {12} : Term table tile {13} :
     * Term Column name "source term" {14} : Term Column name "target term" {15}
     * : Term table
     * 
     * @return java.lang.String[]
     */
    private String[] makeParamList(OfflineSegmentData p_segment, long p_jobId)
            throws AmbassadorDwUpException
    {
        StringBuffer sb = new StringBuffer();
        boolean needTitle = false;
        Object[] args =
        { "-", "-", "-", "-", "-" };
        String[] params = new String[SEG_PARAM_COUNT];
        String id = "";
        try
        {
            // source text
            id = p_segment.getDisplaySegmentID();

            params[1] = id;
            params[2] = id;

            String sourceStr = EditUtil.encodeHtmlEntities(p_segment
                    .getDisplaySourceText());

            // If the job is created by xliff file, need restore the source
            // content
            if (p_segment.getSourceTuv() != null)
            {
                Tuv sourceTuv = p_segment.getSourceTuv();

                if (sourceTuv.getTu(p_jobId).getDataType()
                        .startsWith(IFormatNames.FORMAT_XLIFF))
                {
                    sourceStr = EditUtil.encodeHtmlEntities(SegmentUtil
                            .restoreSegment(sourceStr,
                                    m_sourceLocale.getLocaleCode()));
                }
            }

            if (m_rtlSourceLocale)
            {
                // The Div that encloses this handles text direction
                params[3] = m_PtagFormat.htmlLtrPtags(sourceStr);
            }
            else
            {
                params[3] = m_PtagFormat.htmlPlain(sourceStr);
            }

            // Build all resources

            // native map
            String map = HtmlTableWriter.getSortedHtmlRows(p_segment
                    .getPTag2NativeMap());
            if (map != null && map.length() > 0)
            {
                args[1] = m_resource.getString(LABEL_PMAP_TITLE);
                args[2] = m_resource.getString(LABEL_PMAPCOL_PLACEHOLDER);
                args[3] = m_resource.getString(LABEL_PMAPCOL_CONTENT);
                args[4] = m_PtagFormat.htmlPlain(map);

                sb.append(MessageFormat.format(
                        m_resource.getString(HTML_TEMPLATE_FUZZY_TABLE), args));
            }
        }
        catch (PseudoParserException ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(ex);
        }

        // Get/format term matches.
        String tmp = getTermMatchTableRows(p_segment);
        if (tmp.length() > 0)
        {
            args[1] = m_resource.getString(LABEL_TERM_TITLE);
            args[2] = m_resource.getString(LABEL_TERMCOL_SOURCE);
            args[3] = m_resource.getString(LABEL_TERMCOL_TARGET);
            args[4] = tmp;

            sb.append(MessageFormat.format(
                    m_resource.getString(HTML_TEMPLATE_TERM_TABLE), args));
        }

        // Get/format fuzzy matches.
        // The ptag fuzzzy list is created from the orgFuzzyList when the
        // PtagPageGenerator is run on the OfflinePageData object.
        tmp = getFuzzyMatchTableRows(p_segment);
        if (tmp.length() > 0)
        {
            args[1] = m_resource.getString(LABEL_FUZZY_TITLE);
            args[2] = m_resource.getString(LABEL_FUZZYCOL_SCORE);
            args[3] = m_resource.getString(LABEL_FUZZYCOL_CONTENT);
            args[4] = tmp;

            sb.append(MessageFormat.format(
                    m_resource.getString(HTML_TEMPLATE_FUZZY_TABLE), args));
        }

        params[4] = sb.toString();

        return params;
    }

    /**
     * Returns TM matches as html table rows.
     * 
     * @return String - HTML table rows, caller should wrap the rows in a table.
     */
    public String getFuzzyMatchTableRows(OfflineSegmentData p_segData)
            throws AmbassadorDwUpException
    {
        StringBuffer sb = new StringBuffer();

        String[] args =
        { "---", "---" };

        List l1 = p_segData.getOriginalFuzzyLeverageMatchList();
        List l2 = p_segData.getDisplayFuzzyMatchList();

        for (int i = 0; l1 != null && i < l1.size(); i++)
        {
            LeverageMatch lm = (LeverageMatch) l1.get(i);

            args[0] = (lm == null) ? "??" : ""
                    + String.valueOf(Math.floor(lm.getScoreNum())).substring(
                            0,
                            String.valueOf(Math.floor(lm.getScoreNum()))
                                    .indexOf(".")) + "%";

            try
            {
                if (m_rtlTargetLocale)
                {
                    args[1] = (l2.get(i) == null) ? "---" : m_PtagFormat
                            .htmlLtrPtags(EditUtil
                                    .encodeHtmlEntities((String) l2.get(i)));
                }
                else
                {
                    args[1] = (l2.get(i) == null) ? "---" : m_PtagFormat
                            .htmlPlain(EditUtil.encodeHtmlEntities((String) l2
                                    .get(i)));
                }
            }
            catch (PseudoParserException ex)
            {
                CATEGORY.error(ex.getMessage(), ex);
                throw new AmbassadorDwUpException(ex);
            }

            // Format FuzzyRow = <TR><TD>[Score]</TD><TD>[Segment]</TD></TR>
            sb.append("<TR><TD>");
            sb.append(args[0]);
            sb.append("</TD>");

            if (m_rtlTargetLocale)
            {
                sb.append(Text.containsBidiChar(args[1]) ? "<TD DIR=\"RTL\">"
                        : "<TD DIR=\"LTR\">");
            }
            else
            {
                sb.append("<TD>");
            }

            sb.append(args[1]);
            sb.append("</TD></TR>");
        }

        return sb.toString();
    }

    /**
     * Returns Term matches as html table rows.
     * 
     * @return String - HTML table rows, caller should wrap the rows in a table.
     */
    public String getTermMatchTableRows(OfflineSegmentData p_segment)
    {
        StringBuffer sbResult = new StringBuffer();
        StringBuffer sbTargets = new StringBuffer();
        String[] args =
        { "---", "---" };
        String nextTarget = null;
        TermLeverageMatchResult tlm = null;
        List matchList = p_segment.getTermLeverageMatchList();

        if (matchList != null && matchList.size() > 0)
        {
            for (int i = 0; i < matchList.size(); i++)
            {
                tlm = (TermLeverageMatchResult) matchList.get(i);

                args[0] = tlm.getSourceTerm();
                sbTargets.append(tlm.getFirstTargetTerm());

                while ((nextTarget = tlm.getNextTargetTerm()) != null)
                {
                    sbTargets.append(", " + nextTarget);
                }

                args[1] = EditUtil.encodeHtmlEntities(sbTargets.toString());
                sbTargets.delete(0, sbTargets.length());

                // Format of TermRow = <TR><TD align="center">[src
                // term]</TD><TD>[trg term(s)]</TD></TR>
                sbResult.append("<TR>");
                sbResult.append(m_rtlSourceLocale ? "<TD align=\"center\" DIR=\"RTL\">"
                        : "<TD>");
                sbResult.append(args[0]);
                sbResult.append("</TD>");
                sbResult.append(m_rtlTargetLocale ? "<TD align=\"center\" DIR=\"RTL\">"
                        : "<TD>");
                sbResult.append(args[1]);
                sbResult.append("</TD>");
                sbResult.append("</TR>");
            }
        }

        return sbResult.toString();
    }

    /**
     * Sets Globalsight Locale objects.
     * 
     * @exception UploadPageSaverException
     */
    private void setPageLocales(String p_sourceLocale, String p_targetLocale)
            throws AmbassadorDwUpException
    {
        LocaleManager mgr = null;

        try
        {
            mgr = ServerProxy.getLocaleManager();

            m_sourceLocale = mgr.getLocaleByString(p_sourceLocale);
            m_targetLocale = mgr.getLocaleByString(p_targetLocale);
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new AmbassadorDwUpException(ex);
        }
    }
}
