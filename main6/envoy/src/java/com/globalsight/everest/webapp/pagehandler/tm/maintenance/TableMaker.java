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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.tm.searchreplace.TmConcordanceResult;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PtagStringFormatter;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.modules.Modules;

/**
 * Creates a HTML table display of TUs found during a search in the TM.
 */
public class TableMaker
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TableMaker.class);

    private String m_sourceFindText = null;
    private boolean m_sourceCaseSensitive = true;
    private String m_targetFindText = null;
    private boolean m_targetCaseSensitive = true;
    private boolean m_makingSource = true;
    private ArrayList m_selections = null;

    // true if used for displaying corpus results
    private boolean m_isForCorpusDisplay = false;

    //
    // Constructor
    //
    public TableMaker()
    {
    }

    /**
     * Creates a TableMaker
     *
     * @param p_isForCorpusDisplay true if the TableMaker will make
     * results for corpus display
     */
    public TableMaker(boolean p_isForCorpusDisplay)
    {
        m_isForCorpusDisplay = p_isForCorpusDisplay;
    }

    /**
     * Converts the search results into table rows.
     */
    public String getTableRows(String p_sourceFindText,
            boolean p_sourceCaseSensitive, String p_targetFindText,
            boolean p_targetCaseSensitive, TmConcordanceResult p_tmResults,
            String [] p_selections)
            throws EnvoyServletException
    {
		return getTableRows(p_sourceFindText, p_sourceCaseSensitive,
				p_targetFindText, p_targetCaseSensitive, p_tmResults, null,
				p_selections);
    }
    
    /**
     * Converts the search results into table rows, sorting using the providing
     * comparator.
     */
    public String getTableRows(String p_sourceFindText,
        boolean p_sourceCaseSensitive, String p_targetFindText,
        boolean p_targetCaseSensitive, TmConcordanceResult p_tmResults,
        Comparator<SegmentTmTu> p_tuComparator,
        String [] p_selections)
        throws EnvoyServletException
    {
        m_sourceFindText = p_sourceFindText;
        m_sourceCaseSensitive = p_sourceCaseSensitive;
        m_targetFindText = p_targetFindText;
        m_targetCaseSensitive = p_targetCaseSensitive;
        Map<Long, String> tmIdName = p_tmResults.getMapIdName();
        StringBuffer sb = new StringBuffer();
        List<SegmentTmTu> tus = (p_tuComparator == null) ?
                p_tmResults.getTus() :
                p_tmResults.getTus(p_tuComparator);
        GlobalSightLocale srcLocale = p_tmResults.getSourceLocale();
        ArrayList<GlobalSightLocale> trgLocales = p_tmResults.getTargetLocales();
        // NOTE: for now we only display one target locale
        GlobalSightLocale trgLocale = (GlobalSightLocale)trgLocales.get(0);
        String rowColor, targetRowColor;

        if (p_selections != null)
        {
            m_selections = new ArrayList(Arrays.asList(p_selections));
        }

        try
        {
            for (int i = 0, max = tus.size(); i < max; i++)
            {
                SegmentTmTu tu = tus.get(i);

                rowColor = (i % 2 == 0) ? "#FFFFFF" : "#EEEEEE";

                BaseTmTuv srcTuv = tu.getFirstTuv(srcLocale);
                BaseTmTuv trgTuv;
                Collection targetTuvs = tu.getTuvList(trgLocale);
                boolean first = true;
                int count = 0;

                for (Iterator it = targetTuvs.iterator(); it.hasNext(); )
                {
                    trgTuv = (BaseTmTuv)it.next();

                    targetRowColor = (count++ % 2 == 0) ? rowColor : "#DDDDDD";
                    sb.append(makeTableRow(srcTuv, trgTuv, rowColor,
                        targetRowColor, first, tmIdName));

                    first = false;
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }

        return sb.toString();
    }

    /**
     * Just formats a table cell and returns the HTML for it.
     *
     * @param p_findText text to find
     * @param p_caseSensitive case sensitive?
     * @param p_tuv a TUV
     * @return String of HTML
     */
    public String getFormattedCell(String p_findText,
        boolean p_caseSensitive, BaseTmTuv p_tuv)
        throws Exception
    {
        m_makingSource = true;
        m_sourceCaseSensitive = p_caseSensitive;
        m_sourceFindText = p_findText;
        return makeParentLevel(p_tuv);
    }

    /**
     * Make a single table row for a search result.
     */
    private String makeTableRow(BaseTmTuv p_srcTuv, BaseTmTuv p_trgTuv,
        String p_rowColor, String p_targetRowColor, boolean p_first, Map<Long, String> tmIdName)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();

        // start row
        sb.append("<TR VALIGN=TOP BGCOLOR=\"");
        sb.append(p_rowColor);
        sb.append("\">");

        if (!m_isForCorpusDisplay)
        {
            makeCheckboxCells(sb, p_srcTuv, p_first);
        }

        if (Modules.isCorpusInstalled())
        {
            makeCorpusIconCell(sb, p_srcTuv, p_trgTuv, p_first);
        }

        // Make source segment cell (parent then subflows)
        // - Inner table is two cells wide
        // - parent seg spans both cells in first row
        // - remaining rows have subflowID and then seg text in separate cells
        sb.append("<TD CLASS=\"results\">\n");
        if (p_first)
        {
            sb.append("<TABLE CLASS=\"standardText\">\n");
            sb.append(makeSourceParentLevel(p_srcTuv));
            // sb.append(makeSubflowLevel(p_srcTuv));
            sb.append("</TABLE>\n");
        }
        else
        {
            sb.append("&nbsp;");
        }
        sb.append("</TD>\n");

        // Make target segment cell (parent then subflows)
        // - Inner table is two cells wide
        // - parent seg spans both cells in first row
        // - remaining rows have subflowID and then seg text in separate cells
        sb.append("<TD>\n");
        sb.append("<TABLE CLASS=\"standardText\" BGCOLOR=\"");
        sb.append(p_targetRowColor);
        sb.append("\">\n");
        sb.append(makeTargetParentLevel(p_trgTuv));
        // sb.append(makeSubflowLevel(p_trgTuv));
        sb.append("</TABLE>\n");
        sb.append("</TD>\n");
        
        sb.append("<TD>\n");
        if(p_first)
        {
        	sb.append("<TABLE CLASS=\"standardText\">\n");
        	sb.append(makeTMName(p_trgTuv.getTu().getTmId(), tmIdName));
        	sb.append("</TABLE>\n");
        }
        sb.append("</TD>\n");

        sb.append("<TD>\n");
       	sb.append("<TABLE CLASS=\"standardText\">\n");
        sb.append(makeSID(p_srcTuv.getSid()));
        sb.append("</TABLE>\n");
        sb.append("</TD>\n");
        // end row
        sb.append("</TR>\n\n");

        return sb.toString();
    }

    public Object makeTMName(long tmId, Map<Long, String> tmIdName) {
		StringBuilder sb = new StringBuilder("");
		sb.append("<tr>");
		sb.append("<td>");
		sb.append(tmIdName.get(tmId));
		sb.append("</td>");
		sb.append("</tr>");
		return sb.toString();
	}
    
    public Object makeSID(String sid) {
		StringBuilder sb = new StringBuilder("");
		sb.append("<tr>");
		sb.append("<td>");
		if (null == sid) {
			sid = "N/A";
		}
		sb.append(sid);
		sb.append("</td>");
		sb.append("</tr>");
		return sb.toString();
	}

	private void makeCorpusIconCell(StringBuffer sb, BaseTmTuv p_srcTuv,
        BaseTmTuv p_trgTuv, boolean p_first)
    {
        // Make segmentID cell
        sb.append("<TD CLASS=\"clickable\" STYLE=\"padding-top: 2px\">");

        if (p_first)
        {
            sb.append("<img src=\"/globalsight/images/corpus_icon.jpg\" ");
            sb.append("onclick=\"showCorpus(");
            sb.append(p_trgTuv.getId());
            sb.append(",");
            sb.append(p_srcTuv.getLocale().getId());
            sb.append(")\"></img>");
        }
        else
        {
            sb.append("&nbsp;");
        }

        sb.append("</TD>\n");
    }

    private void makeCheckboxCells(StringBuffer sb,BaseTmTuv p_srcTuv,
        boolean p_first)
    {
        // Make checkBox cell
        sb.append("<TD CLASS=results>");
        if (p_first)
        {
            sb.append("<INPUT TYPE=\"checkbox\" NAME=\"");
            sb.append(WebAppConstants.TM_REPLACE_SEGMENT_CHKBOX);
            sb.append("\" VALUE=\"");
            sb.append(p_srcTuv.getTu().getId());
            sb.append("\"");

            String checked = "";
            if (m_selections == null)
            {
                // default
                checked = " CHECKED";
            }
            else // refresh current selections
            {
                int idx = m_selections.indexOf(
                    String.valueOf(p_srcTuv.getTu().getId()));

                if (idx >= 0)
                {
                    checked = " CHECKED";

                    // speed up next search
                    m_selections.remove(idx);
                }
            }

            sb.append(checked);
            sb.append(">");
        }
        else
        {
            sb.append("&nbsp;");
        }
        sb.append("</TD>\n");

        // Make segmentID cell
        sb.append("<TD CLASS=results STYLE=\"padding-top: 2px\">");
        if (p_first)
            sb.append(p_srcTuv.getTu().getId());
        else
            sb.append("&nbsp;");
        sb.append("</TD>\n");
    }

    /** Make root segment HTML */
    private String makeSourceParentLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        m_makingSource = true;
        return makeParentLevel(p_tuv);
    }

    /** Make root segment HTML */
    private String makeTargetParentLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        m_makingSource = false;
        return makeParentLevel(p_tuv);
    }

    /** Make root segment HTML */
    private String makeParentLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        StringBuffer result = new StringBuffer();
        String gxml = null;
        String rawPtagStr = null;

        gxml = GxmlUtil.stripRootTag(p_tuv.getSegment());

        rawPtagStr = makePtagString(gxml, PseudoConstants.PSEUDO_COMPACT,
            p_tuv.getTu().getFormat());

        // build html
        result.append("<TR VALIGN=TOP>\n");
        result.append("<TD STYLE=\"word-wrap: break-word\" COLSPAN=2 WIDTH=300");
        if (EditUtil.isRTLLocale(p_tuv.getLocale()))
        {
            result.append(Text.containsBidiChar(rawPtagStr) ?
                " DIR=\"RTL\"" : " DIR=\"LTR\"" );
        }
        result.append(">\n");

        result.append(colorPtagString(rawPtagStr, p_tuv.getLocale()));
        result.append("</TD>\n");
        result.append("</TR>\n");

        return result.toString();
    }

    /** Make subflow HTML. */
    /* No subflows in 4.5 SegmentTM.

    private String makeSubflowLevel(BaseTmTuv p_tuv)
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        List parentsOfSubs = null;
        ListIterator parentsOfSubsIt = null;
        List subsUnderParent = null;
        ListIterator subsUnderParentIt = null;
        GxmlElement aSub = null;
        String gxml = null;
        String rawPtagStr = null;

        parentsOfSubs = p_tuv.getSubflowParentsAsGxmlElements();
        parentsOfSubsIt = parentsOfSubs.listIterator();
        while (parentsOfSubsIt.hasNext())
        {
            GxmlElement aParentOfSubs = (GxmlElement)parentsOfSubsIt.next();
            String rootId = makeSubflowRootId(p_tuv.getTu().getTuId(),
                aParentOfSubs);
            subsUnderParent = aParentOfSubs.getDescendantElements(
                GxmlElement.SUB_TYPE);
            subsUnderParentIt = subsUnderParent.listIterator();

            while (subsUnderParentIt.hasNext())
            {
                aSub = (GxmlElement) subsUnderParentIt.next();
                gxml = aSub.toGxmlExcludeTopTags();

                // *always* use the SUB's source datatype if present
                // if sub datatype is not present - we get it from the parent
                String srcDataType = aSub.getAttribute(GxmlNames.SUB_DATATYPE);
                if (srcDataType == null || srcDataType.length() <= 0)
                {
                    srcDataType = p_tuv.getDataType();
                }

                rawPtagStr = makePtagString(gxml,
                    PseudoConstants.PSEUDO_COMPACT, srcDataType);

                sb.append("<TR VALIGN=TOP>\n");
                sb.append("<TD WIDTH=80>\n");
                sb.append(rootId); // parent TuId and parent ptag identifier
                sb.append(aSub.getAttribute(GxmlNames.SUB_ID)); // subID
                sb.append("</TD>\n");
                sb.append("<TD WIDTH=220 STYLE=\"word-wrap: break-word\"");

                if (EditUtil.isRTLLocale(p_tuv.getLocale()))
                {
                    sb.append( Text.containsBidiChar(rawPtagStr) ?
                        " DIR=\"RTL\">\n" : " DIR=\"LTR\">\n" );
                }
                else
                {
                    sb.append(">\n");
                }

                sb.append(colorPtagString(rawPtagStr, p_tuv.getLocale()));

                sb.append("</TD>\n");
                sb.append("</TR>\n");
            }
        }

        return sb.toString();
    }
    */

    /** Convert a gxml string to a ptag string. */
    private String makePtagString(String p_gxml, int p_pTagDisplayMode,
        String p_dataType)
        throws Exception
    {
        PseudoData pTagData = null;
        TmxPseudo convertor = null;

        // create ptag resources
        pTagData = new PseudoData();
        convertor = new TmxPseudo();

        // convert gxml
        pTagData.setMode(p_pTagDisplayMode);
        pTagData.setAddables(p_dataType);

        convertor.tmx2Pseudo(p_gxml, pTagData);

        return pTagData.getPTagSourceString();
    }

    /** color code ptag strings*/
    private String colorPtagString(String p_rawPtagString,
        GlobalSightLocale p_locale)
        throws Exception
    {
        PtagStringFormatter format = new PtagStringFormatter();
        String findText = m_makingSource ? m_sourceFindText : m_targetFindText;

        return format.htmlLtrPtags(
            EditUtil.encodeHtmlEntities(p_rawPtagString), findText,
            m_makingSource ? m_sourceCaseSensitive : m_targetCaseSensitive,
            p_locale);
    }

    /**
     * Builds a subflow-root id.  The caller must append a segment id
     * to form a full offline id.
     * @param p_tuId the TU id of the target tuv.
     * @param p_gxml_element the elment used to from the placeholder Id
     * @return the subflow root id as a string
     */
    private String makeSubflowRootId(long p_tuId, GxmlElement p_element)
    {
        String parentPtagId = "";
        String tagName = p_element.getName();
        Hashtable attributes = new Hashtable();
        PseudoData placeHolder = new PseudoData();
        StringBuffer sb = new StringBuffer();

        attributes.put("type", p_element.getAttribute("type"));
        attributes.put("x",    p_element.getAttribute("x"));

        try // try to get the full name
        {
            placeHolder.setMode(PseudoConstants.PSEUDO_COMPACT);
            parentPtagId = placeHolder.makePseudoTagName(
                tagName, attributes, "" );
        }
        catch (Exception e1)
        {
            try // to get the x attribute only
            {
                parentPtagId = p_element.getAttribute("x");
            }
            catch (Exception e2)
            {
                // drop it
                parentPtagId = "";
            }
        }

        sb.append(Long.toString(p_tuId));
        sb.append(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER);
        sb.append("[");
        sb.append(parentPtagId);
        sb.append("]");
        sb.append(AmbassadorDwUpConstants.SEGMENT_ID_DELIMITER);

        return sb.toString();
    }
}
